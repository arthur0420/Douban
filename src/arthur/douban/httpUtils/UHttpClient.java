package arthur.douban.httpUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
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
	private static  BasicCookieStore cookieStore = new BasicCookieStore();
	private static PoolingHttpClientConnectionManager phcm = new PoolingHttpClientConnectionManager();
	
	private static  CloseableHttpClient httpclient = null;
	static{
		// TODO　 配置化。
		phcm.setMaxTotal(200);
		phcm.setDefaultMaxPerRoute(20);
		httpclient = HttpClients.custom().setConnectionManager(phcm).setDefaultCookieStore(cookieStore).build();
	}
	public static  String get(String url){
		String returnStr ;
		CloseableHttpResponse response = null ;
        try {
            HttpGet httpGet = new HttpGet(url);
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
            	log.info("请求失败 code:"+statusCode+",url:"+url);
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
	
	public static void main(String[] args) {
		try {
//			login();
			
			for(int i = 0 ; i< 999; i++){
				new Thread(new Runnable() {
					@Override
					public void run() {
						String string = get("https://www.douban.com");
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).start();
			}
			String string = get("https://www.zhihu.com/question/47464143");
			log.info(string);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void login() throws Exception{
		   try {
	            HttpGet httpget = new HttpGet("https://www.douban.com/");
	            httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393");
//	            httpget.setHeader("Host", "www.douban.com/");
	            CloseableHttpResponse response1 = httpclient.execute(httpget);
	            try {
	                HttpEntity entity = response1.getEntity();
	                FileOutputStream fos = new FileOutputStream(new File("d://loginPage.txt"));
	                entity.writeTo(fos);
	                System.out.println("Login form get: " + response1.getStatusLine());
	                EntityUtils.consume(entity);

	                System.out.println("Initial set of cookies:");
	                List<Cookie> cookies = cookieStore.getCookies();
	                if (cookies.isEmpty()) {
	                    System.out.println("None");
	                } else {
	                    for (int i = 0; i < cookies.size(); i++) {
	                        System.out.println("- " + cookies.get(i).toString());
	                    }
	                }
	            } finally {
	                response1.close();
	            }
	            RequestBuilder post = RequestBuilder.post();
	            HttpUriRequest login = null;
	            
	            post.setUri(new URI("https://www.douban.com/accounts/login"));
	            
	            login = post.addParameter("form_email", "a18795428457@163.com")
	            		.addParameter("form_password", "loveislie")
	            		.build();
	            login.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
//	            login.setHeader("Host", "www.douban.com/");
//	            login.setHeader("Referer","https://www.douban.com/");
//	            login.setHeader("Content-Type","application/x-www-form-urlencoded");
	            
	            CloseableHttpResponse response2 = httpclient.execute(login);
	            try {
	                HttpEntity entity = response2.getEntity();
	                System.out.println("Login form get: " + response2.getStatusLine());
	                ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                FileOutputStream fos = new FileOutputStream(new File("d://xiaomi.txt"));
	                entity.writeTo(fos);
	                EntityUtils.consume(entity);
	            } finally {
	                response2.close();
	            }
	        } finally {
	        }
		}
	
	
}
