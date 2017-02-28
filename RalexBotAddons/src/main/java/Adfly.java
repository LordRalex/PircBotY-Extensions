import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A class that can be used to deadfly adf.ly links.
 *
 * @author Lord_Ralex (original)
 * @author Mesmashh (idea)
 * @author Nouish (improvements)
 */
public final class Adfly {

	/**
	 * The raw adf.ly link we looked up.
	 */
	private String adflyLink = null;

	/**
	 * A direct link to the site we looked up.
	 */
	private String directLink = null;

	/**
	 * An adf.ly redirect link to the site we looked up.
	 */
	private String redirectLink = null;

	/**
	 * When this method is called the links will be parsed and stored.
	 *
	 * @param adflyLink An adf.ly link
	 */
	public void update(String adflyLink)
	{
		this.adflyLink = adflyLink;

		try
		{
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(new URL(adflyLink).openStream()));

			String line;
			while ((line = reader.readLine()) != null)
			{
				line = line.replace("\t", "");

				if (line.contains("var url ="))
				{
					redirectLink = line.substring(line.indexOf("'") + 1,
							line.lastIndexOf("'"));

					if (redirectLink.startsWith("https://adf.ly/"))
					{
						redirectLink = "http" + redirectLink.substring(5);
					}
					else
					{
						redirectLink = "http://adf.ly/" + redirectLink;
					}

					redirectLink = redirectLink.replace(" ", "%20");

					HttpURLConnection connection = (HttpURLConnection) new URL(
							redirectLink).openConnection();
					connection.setReadTimeout(5000);
					connection.getInputStream();
					connection.disconnect();

					directLink = connection.getURL().toString();
					directLink = directLink.replace(" ", "%20");
				}
			}
		}
		catch (FileNotFoundException e)
		{
			if (e.getMessage().startsWith("http"))
			{
				redirectLink = e.getMessage();
			}
			else
			{
				e.printStackTrace();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Reset all data stored in this object.
	 */
	public void reset()
	{
		adflyLink = null;
		directLink = null;
		redirectLink = null;
	}

	/**
	 * Get the most appropiate link content link available of the two,
	 * the <code>directLink</code> will be selected over the adf.ly
	 * <code>redirectLink</code>.
	 * </p>
	 * An <code>IllegalStateException</code> will be thrown in a scenario
	 * where we don't have either link available.
	 *
	 * @return The most preferred content link available.
	 */
	public String getResult()
	{
		if (directLink == null && redirectLink == null)
		{
			throw new IllegalStateException("please submit an adf.ly url first!");
		}
		return directLink != null ? directLink : redirectLink;
	}

	/**
	 * Gets the raw adf.ly link. This is convenient in a scenario where you
	 * don't know what this object is currently updated to represent.
	 * 
	 * @return The raw adf.ly link.
	 */
	public String getAdflyLink()
	{
		if (adflyLink == null)
		{
			throw new IllegalStateException("there is currently no adf.ly link available!");
		}
		return adflyLink;
	}

	/**
	 * Gets the the direct link, being the link that someone have adf.ly-ed.
	 * 
	 * @return The direct link.
	 */
	public String getDirectLink()
	{
		if (directLink == null)
		{
			throw new IllegalStateException("there is currently no direct link available!");
		}
		return directLink;
	}

	/**
	 * Gets the adf.ly redirect link, this url redirects to the direct link.
	 * 
	 * @return The adf.ly redirect link.
	 */
	public String getRedirectLink()
	{
		if (redirectLink == null)
		{
			throw new IllegalStateException("there is currently no redirect link available!");
		}
		return redirectLink;
	}

	@Override
	public String toString()
	{
		return Adfly.class.getName()
				+ " [adflyLink=" + adflyLink +
				", directLink=" + directLink +
				", redirectLink=" + redirectLink + "]";
	}

}
