package jp.rsn.meganecase;

import java.io.IOException;
import java.net.URLEncoder;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

public class Bitly {

    public static final String API_BASE = "http://api.bit.ly";
    public static final String API_METHOD_SHORTEN = "/v3/shorten";
    public static final String API_METHOD_EXPAND = "/v3/expand";
    public static final String API_LOGIN = "login=xxxxxxxx";
    public static final String API_KEY = "apiKey=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    public static final String API_FORMAT = "format=txt";

    public static final String shorten(String longUrl) {
        StringBuffer callUrl = new StringBuffer(API_BASE);
        callUrl.append(API_METHOD_SHORTEN);
        callUrl.append("?").append(API_LOGIN);
        callUrl.append("&").append(API_KEY);
        callUrl.append("&").append(API_FORMAT);
        callUrl.append("&longUrl=").append(URLEncoder.encode(longUrl));
        String result = call(callUrl.toString());
        if (result != null) {
            result = result.trim();
        }
        return result;
    }

    public static final String expand(String shortUrl) {
        StringBuffer callUrl = new StringBuffer(API_BASE);
        callUrl.append(API_METHOD_EXPAND);
        callUrl.append("?").append(API_LOGIN);
        callUrl.append("&").append(API_KEY);
        callUrl.append("&").append(API_FORMAT);
        callUrl.append("&shortUrl=").append(URLEncoder.encode(shortUrl));
        String result = call(callUrl.toString());
        if (result != null) {
            result = result.trim();
        }
        return result;
    }

    private static final String call(String url) {
        SchemeRegistry schreg = new SchemeRegistry();
        schreg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schreg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        HttpParams params = new BasicHttpParams();
        ThreadSafeClientConnManager connManager = new ThreadSafeClientConnManager(params, schreg);
        DefaultHttpClient httpClient = new DefaultHttpClient(connManager, params);
        HttpGet request = new HttpGet(url);
        try {
            return httpClient.execute(request, new BasicResponseHandler());
        }
        catch (ClientProtocolException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            httpClient.getConnectionManager().shutdown();
        }
        return null;
    }
}
