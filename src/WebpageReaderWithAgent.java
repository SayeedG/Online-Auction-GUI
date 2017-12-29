// Author: Sayeed Gulmahamad
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class WebpageReaderWithAgent {

	private static String webpage = null;
	public static final String USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2) Gecko/20100115 Firefox/3.6";

	public static InputStream getURLInputStream(String sURL) throws Exception {
		URLConnection oConnection = (new URL(sURL)).openConnection();
		oConnection.setRequestProperty("User-Agent", USER_AGENT);
		return oConnection.getInputStream();
	}

	public static BufferedReader read(String url) throws Exception {
		// InputStream content = (InputStream)uc.getInputStream();
		// BufferedReader in = new BufferedReader (new InputStreamReader
		// (content));
		InputStream content = (InputStream) getURLInputStream(url);
		return new BufferedReader(new InputStreamReader(content));
	} // read

	public static BufferedReader read2(String url) throws Exception {
		return new BufferedReader(new InputStreamReader(new URL(url).openStream()));
	} // read

	public static String readHtml(String url) throws Exception {
		// Create a StringBuffer object
		// String Builder object to have a mutable sequence of characters/String
		StringBuilder sb = new StringBuilder();
		//read URL and store in bufferreader
		BufferedReader reader = read(url);
		String line = null;
		//iterate
		while ((line = reader.readLine()) != null) {
			// appending line to StringBuffer object
			sb.append(line);
		}
		//Retrn the String value
		return sb.toString();

	} // main
	/*public static void main(String[] args) {
		*try {
			*String res = readHtml("https://www.shopgoodwill.com/Listings?st=phone&sg=&c=&s=&lp=0&hp=999999&sbn=false&spo=false&snpo=false&socs=false&sd=false&sca=false&caed=11/29/2017&cadb=7&scs=false&sis=false&col=1&p=1&ps=40&desc=false&ss=0&UseBuyerPrefs=true");

			*res = res.substring(res.indexOf("<span class=\"data-container\">") + 1);
		*} catch (Exception e) {
		*	// TODO Auto-generated catch block
		*	e.printStackTrace();
		*}
	*}
*/

} // WebpageReaderWithAgent
