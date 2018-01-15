package arthur.xiaomi;


import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;



public class TestLogin {
	private static final Logger log = Logger.getLogger(TestLogin.class);
	private static BasicCookieStore cookieStore ;
	private static CloseableHttpClient httpclient ;
	private static String USERNAME ;
	private static String PASSWORD;
	public static void init() throws Exception{
		cookieStore = new BasicCookieStore();
		HttpClientBuilder custom = HttpClients.custom();
		httpclient = custom.
//				setConnectionManager(phcm). // 连接池
//				setRetryHandler(new DefaultHttpRequestRetryHandler(3, true)). // 重试 handler
//				setKeepAliveStrategy(myStrategy). // 长连接 策略
				setDefaultCookieStore(cookieStore).build(); // cookiestore*/
		//登录
		USERNAME = "ex-ouyangyasi";
		PASSWORD = "A+12345678";
//		login();
		
	}
	
	public static void main(String[] args) {
		try {
			TestLogin.init();
			login();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void login() throws Exception{
		   try {
	            HttpGet httpget = new HttpGet("https://oa.fcsc.com/third/pda/login.jsp");
	            httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393");
	            CloseableHttpResponse response1 = httpclient.execute(httpget);
	            try {
	                HttpEntity entity = response1.getEntity();
	                FileOutputStream fos = new FileOutputStream(new File("loginPage.html"));
	                entity.writeTo(fos);
	                log.info("Login form get: " + response1.getStatusLine());
	                EntityUtils.consume(entity);

	                log.info("Initial set of cookies:");
	                List<Cookie> cookies = cookieStore.getCookies();
	                if (cookies.isEmpty()) {
	                	log.info("None cookies");
	                } else {
	                    for (int i = 0; i < cookies.size(); i++) {
	                      log.info("- " + cookies.get(i).toString());
	                    }
	                }
	            } finally {
	                response1.close();
	            }
	            RequestBuilder post = RequestBuilder.post();
	            HttpUriRequest login = null;
	            
	            post.setUri(new URI("https://oa.fcsc.com/j_acegi_security_check"));   // 发送登录请求
	            
	            login = post.addParameter("j_username", USERNAME)
	            		.addParameter("j_password", PASSWORD)
	            		.build();
	            login.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
	            
	            CloseableHttpResponse response2 = httpclient.execute(login);
	            try {
	             	log.info("Login form get: " + response2.getStatusLine());
	            } finally {
	                response2.close();
	            }
	            List<Cookie> cookies = cookieStore.getCookies();
	            for (int i = 0; i < cookies.size(); i++) {
                    log.info("- " + cookies.get(i).toString());
                  }
	        } finally {
	        }
		   
		   
		   HttpGet  httpget = new HttpGet("https://oa.fcsc.com/third/pda/index.jsp");
           httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393");
           CloseableHttpResponse response1 = httpclient.execute(httpget);
           try {
               HttpEntity entity = response1.getEntity();
               FileOutputStream fos = new FileOutputStream(new File("homePage.html"));
               entity.writeTo(fos);
               EntityUtils.consume(entity);
           } finally {
               response1.close();
           }
           
           List<Cookie> cookies = cookieStore.getCookies();
           for (int i = 0; i < cookies.size(); i++) {
               log.info("- " + cookies.get(i).toString());
           }
		}
}
