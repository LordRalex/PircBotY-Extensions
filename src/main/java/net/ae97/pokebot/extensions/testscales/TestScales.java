/*
 * Copyright (C) 2015 Joshua
 *
 * This file is a part of pokebot-extensions
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.ae97.pokebot.extensions.testscales;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;
import net.ae97.pircboty.api.events.CommandEvent;
import net.ae97.pokebot.PokeBot;
import net.ae97.pokebot.api.CommandExecutor;
import net.ae97.pokebot.extension.Extension;
import net.ae97.pokebot.extension.ExtensionLoadFailedException;
import sun.security.x509.X500Name;

/**
 *
 * @author Joshua
 */
public class TestScales extends Extension implements CommandExecutor {

    private final TrustManager[] trustAllCerts;

    public TestScales() {
        trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };
    }

    @Override
    public void load() throws ExtensionLoadFailedException {
        PokeBot.getExtensionManager().addCommandExecutor(this);
    }

    @Override
    public String getName() {
        return "TestScales";
    }

    @Override
    public void runEvent(CommandEvent ce) {
        if (ce.getArgs().length == 0 || ce.getArgs().length > 2) {
            ce.respond("Usage: ``testscales <ip/host> [port]");
            return;
        }
        String host = ce.getArgs()[0];
        int port = 5656;
        if (ce.getArgs().length == 2) {
            port = Integer.parseInt(ce.getArgs()[1]);
        }
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HostnameVerifier verifier = new HostbasedNameVerifier(host);
            URL url = new URL("https://" + host + ":" + port);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setHostnameVerifier(verifier);
            conn.connect();
            Certificate[] certs = conn.getServerCertificates();
            X509Certificate mainCert = (X509Certificate) certs[0];
            //System.out.println(mainCert);
            mainCert.checkValidity();

            X500Principal hosts = mainCert.getSubjectX500Principal();
            String cn = new X500Name(hosts.getName()).getCommonName();
            if (cn.equalsIgnoreCase(host)) {
                ce.respond(url.toString() + " was reachable and the cert seems valid");
            } else {
                ce.respond("Connection successful, but the cert is issued to " + cn + " instead of " + host);
            }

        } catch (IOException | CertificateExpiredException | CertificateNotYetValidException | NoSuchAlgorithmException | KeyManagementException ex) {
            ce.respond(ex.getClass().getName() + ": " + ex.getMessage());
        }
    }

    @Override
    public String[] getAliases() {
        return new String[]{"testscales"};
    }

    private class HostbasedNameVerifier implements HostnameVerifier {

        private final String host;

        public HostbasedNameVerifier(String host) {
            this.host = host;
        }

        @Override
        public boolean verify(String string, SSLSession ssls) {
            return string.equals(host);
        }

    }

}
