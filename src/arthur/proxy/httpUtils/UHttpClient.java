package arthur.proxy.httpUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONObject;


public class UHttpClient {	
	private static final Logger log = Logger.getLogger(UHttpClient.class);
	private static  CloseableHttpClient httpclient = null;
	private static PoolingHttpClientConnectionManager phcm = new PoolingHttpClientConnectionManager();
	static{
		phcm.setMaxTotal(200);
		phcm.setDefaultMaxPerRoute(20);
		httpclient = HttpClients.custom().setConnectionManager(phcm).build();
	}
	public static  String get(String url){
		String returnStr ;
		CloseableHttpResponse response = null ;
        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393");
            response  = httpclient.execute(httpGet);
            StatusLine httpStatus = response.getStatusLine();
            int statusCode = httpStatus.getStatusCode();
            if( statusCode== 200){
            	HttpEntity entity = response.getEntity();
            	ByteArrayOutputStream bao = new ByteArrayOutputStream();
            	entity.writeTo(bao);
            	EntityUtils.consume(entity);
            	returnStr = new String(bao.toByteArray(),"UTF-8");
            }else{
            	log.info("«Î«Û ß∞‹ code:"+statusCode+",url:"+url);
            	returnStr = "-1";
            }
        }catch(Exception e){
        	log.error(e);
        	returnStr = "-1";
        }finally{
        	if(response!=null){
        		try {
					response.close();
				} catch (IOException e) {
				}
        	}
        }
        return returnStr;
	}
	public static boolean testProxyIp(String ip ,int port){
		try {
			HttpHost proxy = new HttpHost(ip,(port));
			HttpGet g = new HttpGet("https://www.douban.com");
			g.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393");
			RequestConfig rc = RequestConfig.custom().setConnectionRequestTimeout(5000).setSocketTimeout(5000).setConnectTimeout(5000).setProxy(proxy).build();
			g.setConfig(rc);
			CloseableHttpResponse execute = httpclient.execute(g);
			int statusCode = execute.getStatusLine().getStatusCode();
			execute.close();
			if(statusCode == 200)
				return true;
			else return false;
		} catch (Exception e) {
			return false;
		}
	}
	public static void main(String[] args) {
		try {
			boolean testProxyIp = testProxyIp("120.132.71.212",80);
			System.out.println(testProxyIp);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
