package net.ae97.pokebot.extensions.dxdiag;

import com.google.gson.Gson;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.api.Listener;
import net.ae97.pokebot.extensions.dxdiag.download.*;
import nu.xom.Builder;
import nu.xom.Elements;
import nu.xom.ParsingException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Level;

/**
 * Created by urielsalis on 1/26/2017
 */
public class DxdiagListener implements Listener, CommandExecutor {
    private final DxdiagParser core;
    private String apiKey;
    static HashMap<String, Integer> PRODUCT_TYPES = new HashMap<>();
    Intel intel = new Intel("1.0", "intel");


    public DxdiagListener(DxdiagParser system) {
        core = system;
        DownloadMain.core = system;
        core.getConfig().getString("arkAPIKey");
        PokeBot.getScheduler().scheduleTask(new Runnable() {
            @Override
            public void run() {
                downloadDrivers();
            }
        }, 7, TimeUnit.DAYS);
    }

    private void downloadDrivers() {
        DownloadMain.prepare();
        intelFullUpdate();
        amdFullUpdate();
        nvidiaFullUpdate();
    }

    private void amdFullUpdate() {
        try {
            nu.xom.Document document = new Builder().build("http://support.amd.com/drivers/xml/driver_selector_09_us.xml");
            nu.xom.Element root = document.getRootElement();
            Elements platforms = root.getChildElements("platform");
            for (int i = 0; i < platforms.size(); i++) {
                nu.xom.Element platformElement = platforms.get(i);
                String platformName = platformElement.getAttributeValue("name");
                String platformID = platformElement.getAttributeValue("value");
                AMD.Platform platform = new AMD.Platform(platformName, platformID);
                Elements productFamilies = platformElement.getChildElements("productfamily");
                for (int j = 0; j < productFamilies.size(); j++) {
                    nu.xom.Element productFamilyElement = productFamilies.get(j);
                    String productFamilyName = productFamilyElement.getAttributeValue("name");
                    String productFamilyID = productFamilyElement.getAttributeValue("value");
                    if(productFamilyID.equals("autodetect")) continue;
                    AMD.Platform.ProductFamily productFamily = new AMD.Platform.ProductFamily(productFamilyName, productFamilyID);
                    Elements products = productFamilyElement.getChildElements("product");
                    for (int k = 0; k < products.size(); k++) {
                        nu.xom.Element productElement = products.get(k);
                        String productName = productElement.getAttributeValue("label");
                        String productID = productElement.getAttributeValue("value");
                        if(productID.equals("autodetect") || productID.equals("not_sure")) continue;
                        AMD.Platform.ProductFamily.Product product = new AMD.Platform.ProductFamily.Product(productName, productID);
                        Config.GPU gpu = new Config.GPU(productName);
                        Elements versions = productElement.getChildElements("version");
                        for (int l = 0; l < versions.size(); l++) {
                            nu.xom.Element versionElement = versions.get(l);
                            String type = versionElement.getAttributeValue("type");
                            String number = versionElement.getAttributeValue("number"); //version
                            Elements downloads = versionElement.getChildElements();
                            AMD.Platform.ProductFamily.Product.Version version = new AMD.Platform.ProductFamily.Product.Version(type, number, downloads);
                            if(version.shouldDownload) {
                                product.versions.add(version);
                                gpu.addAMD(version);
                            }
                        }
                        //productFamily.products.add(product);
                        DownloadMain.add(gpu, "AMD");
                    }
                    //platform.productFamilies.add(productFamily);
                }
                //config.amd.platforms.add(platform);
            }
        } catch (ParsingException | IOException e) {
            e.printStackTrace();
        }

    }

    private void intelFullUpdate() {
        try {
            List<Callable<Object>> callables = new ArrayList<>();
            Document document = Jsoup.connect("http://www.intel.com/content/www/us/en/support/graphics-drivers.html").userAgent("UrielsalisBot for auto-dxdiag parsing/github.com/urielsalads-reboot/uriel@urielsalis.me/Jsoup").get();
            Element tableMain = document.getElementById("productSelector-1").getAllElements().first().getElementsByClass("blade-expand-secondary").first();
            org.jsoup.select.Elements blades = tableMain.getElementsByClass("blade-group").first().getElementsByClass("blade");
            for(Element blade: blades) {
                org.jsoup.select.Elements divs = blade.getElementsByClass("container").first().select("div").first().select("div");
                for(Element div: divs) {
                    org.jsoup.select.Elements uls = div.select("ul");
                    for(Element ul: uls) {
                        Element a = ul.select("li").first().select("a").first();
                        final String name = Util.removeSpecialChars(a.text());
                        final String href = "http://www.intel.com/" + a.attr("href");
                        callables.add(new Callable<Object>() {
                            @Override
                            public Object call() throws Exception {
                                String[] html = Jsoup.connect(href).userAgent("UrielsalisBot for auto-dxdiag parsing/github.com/urielsalads-reboot/uriel@urielsalis.me/Jsoup").get().html().split("[\\r\\n]+");
                                //var epmid = "81498";
                                int epmID = 0;
                                for(String str: html) {
                                    if(str.trim().startsWith("var epmid = ")) {
                                        epmID = Integer.parseInt(str.substring(str.indexOf("\"")+1, str.lastIndexOf("\"")));
                                        break;
                                    }
                                }
                                if(epmID==0) {
                                    System.out.println("Error processing " + href);
                                }
                                System.out.println("Thread " + Thread.currentThread().getName() + " of " +  Thread.activeCount() + ": " + name + " - " + epmID);
                                Intel.Driver driver = new Intel.Driver(name, epmID);
                                intel.driver.add(driver);
                                return null;
                            }
                        });
                    }
                }
            }
            ExecutorService service = Executors.newFixedThreadPool(8);
            try {
                service.invokeAll(callables);
                while(service.awaitTermination(1, TimeUnit.SECONDS)) { //Wait till all threads finished
                    service.awaitTermination(1, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            intelPartialUpdate();
            System.out.println("done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void intelPartialUpdate() {
        List<Callable<PartialUpdateData>> callables2 = new ArrayList<>();

        for(final Intel.Driver driver: intel.driver) {
            callables2.add(new Callable<PartialUpdateData>() {
                @Override
                public PartialUpdateData call() throws Exception {
                    return new PartialUpdateData(driver, fillDownload(driver));
                }
            });
        }

        ExecutorService service = Executors.newFixedThreadPool(8);
        try {
            List<Future<PartialUpdateData>> futures = service.invokeAll(callables2);
            while(service.awaitTermination(1, TimeUnit.SECONDS)) { //Wait till all threads finished
                service.awaitTermination(1, TimeUnit.SECONDS);
            }
            intel.driver.clear();
            for(Future<PartialUpdateData> future: futures) {
                if(future.isDone()) {
                    PartialUpdateData data = future.get();
                    data.driver.download.addAll(data.downloads);
                    //DownloadMain.add(data.driver); Already added when creating Download
                } else {
                    System.err.println("A future didnt finish in time!!!!");
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


    }

    private ArrayList<Download> fillDownload(Intel.Driver driver) {
        try {
            URL url = new URL("https://downloadcenter.intel.com/json/pageresults?pageNumber=1&&productId=" + driver.epmID);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.connect();
            ArrayList<Download> downloads = new ArrayList<>();
            EPMIdResults results = new Gson().fromJson(new InputStreamReader((InputStream) request.getContent()), EPMIdResults.class);
            for (EPMIdResults.ResultsForDisplayImpl display : results.ResultsForDisplay) {
                downloads.add(new Download(display, driver));
            }
            return downloads;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private synchronized void fillDownload(Intel.Driver driver, EPMIdResults.ResultsForDisplayImpl display) {
        driver.download.add(new Download(display, driver));
    }

    private synchronized void addDriver(Intel.Driver driver) {
        intel.driver.add(driver);
    }

    private void nvidiaFullUpdate() {
        PRODUCT_TYPES.put("GeForce", 1);
        //PRODUCT_TYPES.put("nForce", 2);
        PRODUCT_TYPES.put("Quadro", 3);
        PRODUCT_TYPES.put("Legacy", 4);
        PRODUCT_TYPES.put("3D Vision", 5);
        PRODUCT_TYPES.put("ION", 6);
        PRODUCT_TYPES.put("Tesla", 7);
        PRODUCT_TYPES.put("NVS", 8);
        PRODUCT_TYPES.put("GRID", 9);
        NvidiaDriverGrabber driverSearch = new NvidiaDriverGrabber("http://www.nvidia.com/Download/API/lookupValueSearch.aspx", "http://www.nvidia.com/Download/processDriver.aspx", "en-us", 1, 5);
        driverSearch.parse();
    }

    private static class NvidiaDriverGrabber {
        public String lookupUrl;
        public String processUrl;
        public String locale;
        public int language;
        public ArrayList<String> errors;
        public int throttle = 5;
        Builder parser = new Builder();

        public NvidiaDriverGrabber(String lookupUrl, String processUrl, String locale, int language, int throttle) {
            this.lookupUrl = lookupUrl;
            this.processUrl = processUrl;
            this.locale = locale;
            this.language = language;
            this.throttle = throttle;
            this.errors = new ArrayList<>();
        }

        public nu.xom.Document lookupRequest(int step, int value) {
            String args = "?TypeID=" + step + "&ParentID=" + value;
            System.out.println("--> " + this.lookupUrl + args);
            try {
                URL url = new URL(lookupUrl+args);
                InputStream stream = url.openStream();
                return parser.build(stream);
            } catch (ParsingException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public String processRequest(int ProductSeriesID, int ProductFamilyID, int RPF, int OperatingSystemID, int LanguageID, String Locale, int CUDAToolkit) {
            String args = "?psid="+ProductSeriesID+"&pfid="+ProductFamilyID+"&rpf="+RPF+"&osid="+OperatingSystemID+"&lid="+LanguageID+"&lang="+Locale+"&ctk="+CUDAToolkit;
            System.out.println("==> " + this.processUrl + args);
            try {
                URLConnection conn = new URL(processUrl + args).openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder builder = new StringBuilder();
                String aux = "";

                while ((aux = reader.readLine()) != null) {
                    builder.append(aux);
                }

                return builder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        public void parse() {
            try {
                for (Map.Entry<String, Integer> entry : PRODUCT_TYPES.entrySet()) {
                    Nvidia.ProductType productType = new Nvidia.ProductType(entry.getKey()); //start step 1
                    //start step 2
                    {
                        nu.xom.Document documentStep2 = lookupRequest(2, entry.getValue());
                        if (documentStep2 == null) {
                            System.out.println("Sleeping for 80 secs and trying again");
                            TimeUnit.SECONDS.sleep(80);
                            documentStep2 = lookupRequest(2, entry.getValue());
                            if (documentStep2 == null) {
                                System.out.println("Failed");
                                System.exit(1);
                            }
                        }
                        Elements lookupValuesStep2 = documentStep2.getRootElement().getFirstChildElement("LookupValues").getChildElements();
                        for (int i = 0; i < lookupValuesStep2.size(); i++) {
                            nu.xom.Element lookupValue2 = lookupValuesStep2.get(i);

                            Nvidia.Series series = new Nvidia.Series(lookupValue2);
                            //start step 3
                            {
                                nu.xom.Document documentStep3 = lookupRequest(3, series.id);
                                if (documentStep3 == null) {
                                    System.out.println("Sleeping for 80 secs and trying again");
                                    TimeUnit.SECONDS.sleep(80);
                                    documentStep3 = lookupRequest(3, series.id);
                                    if (documentStep3 == null) {
                                        System.out.println("Failed");
                                        System.exit(1);
                                    }
                                }
                                Elements lookupValuesStep3 = documentStep3.getRootElement().getFirstChildElement("LookupValues").getChildElements();
                                for (int j = 0; j < lookupValuesStep3.size(); j++) {
                                    nu.xom.Element lookupValue3 = lookupValuesStep3.get(j);
                                    Nvidia.Series.Product product = new Nvidia.Series.Product(lookupValue3);
                                    {
                                        //start step 4
                                        nu.xom.Document documentStep4= lookupRequest(4, series.id);
                                        if (documentStep4 == null) {
                                            System.out.println("Sleeping for 80 secs and trying again");
                                            TimeUnit.SECONDS.sleep(80);
                                            documentStep4 = lookupRequest(4, entry.getValue());
                                            if (documentStep4 == null) {
                                                System.out.println("Failed");
                                                System.exit(1);
                                            }
                                        }
                                        Elements lookupValuesStep4 = documentStep4.getRootElement().getFirstChildElement("LookupValues").getChildElements();
                                        Config.GPU gpu = new Config.GPU(product.name);

                                        for (int e = 0; e < lookupValuesStep4.size(); e++) {
                                            nu.xom.Element lookupValue4 = lookupValuesStep4.get(e);
                                            Nvidia.Series.Product.OS os = new Nvidia.Series.Product.OS(lookupValue4);
                                            if(os.shouldDownload) {
                                                //start step 5
                                                //String  String RPF, String OperatingSystemID, String LanguageID, String Locale, String CUDAToolkit) {
                                                String downloadLink = processRequest(series.id, product.id, 1, os.id, language, locale, 0);
                                                os.downloadLink = downloadLink;
                                                int arch = os.is64? 64:32;
                                                gpu.addDownload(os.minified,arch, downloadLink);
                                                //end step 5
                                            }
                                            //end step 4
                                            product.os.add(os);

                                        }
                                        DownloadMain.add(gpu, "Nvidia");
                                    }
                                    //end step 3
                                    //series.products.add(product);
                                }



                            }
                            //end step 2
                            //productType.series.add(series);
                        }
                    }
                    //config.nvidia.productTypes.add(productType); //end step 1
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void runEvent(CommandEvent event) {
        if(event.getCommand().equals("dx")) {
            if (event.getArgs().length == 0) {
                event.respond("Usage: dx <link>");
                return;
            }
            event.respond(parseDxdiag(event.getArgs()[0]));
        } else if(event.getCommand().equals("fullUpdate")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    downloadDrivers();
                }
            }).start();
        }

    }

    private String findCPU(String tmp, String minified, boolean is64) {
        //ark.intel.com
        String[] strs = tmp.split("\\s+");
        String cpu = null;
        for(String str: strs) {
            if(Character.isLetter(str.charAt(0)) && Character.isDigit(str.charAt(1))) {
                cpu = str;
                break;
            }
        }
        try {
            if(cpu != null) {
                InputStreamReader reader = new InputStreamReader(new URL("http://odata.intel.com/API/v1_0/Products/Processors()?api_key="+apiKey+"&$select=ProductId,CodeNameEPMId,GraphicsModel&$filter=substringof(%27"+cpu+"%27,ProductName)&$format=json").openStream());
                Ark ark = new Gson().fromJson(reader, Ark.class);
                boolean showMessage = true;
                for(Ark.CPU cpu2: ark.d) {
                    if(cpu2.GraphicsModel != null) {
                        //search in database
                        String message = findDriver(cpu2.GraphicsModel, minified, is64);
                        if(showMessage)
                            return "Ark: " + message;
                        showMessage = false;
                        break;
                    }
                }
                if(showMessage)
                    return "Cant find "+cpu+" in ark";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Cant find "+cpu+" in ark";
    }

    private String parseDxdiag(String s) {
        String minified = "";
        boolean is64 = false;
        StringBuffer result = new StringBuffer();
        for (String str : s.trim().split(" ")) {
            if (str.contains("paste.ubuntu.com")) {
                try {
                    Document document = Jsoup.parse(new URL(str), 10000);
                    Element code = document.select(".code").first();
                    String value = code.select(".paste").first().select("pre").first().text();
                    String[] lines2 = value.split("\n");
                    boolean showedCpu = false;
                    for (String line2 : lines2) {
                        if (line2.contains("Operating System")) {
                            if (line2.contains("64")) is64 = true;
                            String[] split = line2.trim().split(" ");
                            minified = split[3];
                        } else if (line2.contains("Card name")) {
                            String card = line2.trim().split(":")[1];
                            result.append("\n" + findDriver(card, minified, is64));
                        } else if (!showedCpu && line2.contains("Processor: ") && !line2.contains("Video")) {
                            result.append("\n" + findCPU(line2.trim().split(":")[1].trim(), minified, is64));
                            showedCpu = true;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
        return result.substring(1);
    }

    public String findDriver(String name, String os, boolean is64) {
        if (!name.contains("Standard VGA") && !name.contains("Microsoft")) {
            name = name.replace("NVIDIA ", "").replace("(R)", "").replace("AMD ", "").replace("®", "").toLowerCase().trim();
            if(name.equals("intel hd graphics")) return "Do Manual search https://www-ssl.intel.com/content/www/us/en/support/graphics-drivers/000005526.html & https://www-ssl.intel.com/content/www/us/en/support/graphics-drivers/000005538.html";
            try (Connection connection = openConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT link FROM Dxdiag where os = ? AND arch = ? AND Name like ?")) {
                    statement.setString(1, os);
                    statement.setString(2, is64 ? "64" : "32");
                    statement.setString(3, "%" + Util.removeSpecialChars(name.toLowerCase().trim()) + "%");
                    ResultSet set = statement.executeQuery();
                    while (set.next()) {
                        return set.getString("link");
                    }
                    return "Not found";
                }
            } catch (SQLException e) {
                core.getLogger().log(Level.SEVERE, "Error inserting hjt", e);
                return "SQL error";
            }
        }
        return "Not found";
    }

    @Override
    public String[] getAliases() {
        return new String[]{"dx", "fullUpdate"};
    }

    private Connection openConnection() throws SQLException {
        String host = core.getConfig().getString("host");
        int port = core.getConfig().getInt("port");
        String mysqlUser = core.getConfig().getString("user");
        String pass = core.getConfig().getString("pass");
        String database = core.getConfig().getString("database");
        return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database, mysqlUser, pass);
    }
}
