package logic;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class Request {

    public static String getResultFromURL(String url) throws MalformedURLException, IOException {

        //String proxy = "wi-proxy1.wi1.uni-muenster.de";
        //String port = "8080";
        URL server = new URL(url);
        Properties systemProperties = System.getProperties();
        //systemProperties.setProperty("http.proxyHost", proxy);
        //systemProperties.setProperty("http.proxyPort", port);
        HttpURLConnection connection = (HttpURLConnection) server
                .openConnection();
        connection.connect();
        InputStream in = connection.getInputStream();

        return readString(in);
    }

    private static String readString(InputStream is) throws IOException {
        char[] buf = new char[2048];
        Reader r = new InputStreamReader(is, "UTF-8");
        StringBuilder s = new StringBuilder();
        while (true) {
            int n = r.read(buf);
            if (n < 0) {
                break;
            }
            s.append(buf, 0, n);
        }
        return s.toString();
    }
}
