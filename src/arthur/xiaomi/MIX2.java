package arthur.xiaomi;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;

import javax.net.ssl.SSLSession;










import arthur.douban.httpUtils.UHttpClient;

public class MIX2 {
	private static final Logger log = Logger.getLogger(UHttpClient.class);
	BasicCookieStore cookieStore = new BasicCookieStore();
	 
    CloseableHttpClient httpclient = null;
    public MIX2() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException{
    	HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
            	System.out.println(urlHostName);
                return true;
            }
        };
        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(new File(""), "nopassword".toCharArray(),
                        new TrustSelfSignedStrategy())
                .build();
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[] { "TLSv1" },
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
         httpclient =  HttpClients.custom()
         .setDefaultCookieStore(cookieStore)
         .setSSLSocketFactory(sslsf)
         .build();
    }
    public static void main(String[] args) {
    	
    	try {
    		final MIX2 mix2 = new MIX2();
    		for(int i =0; i<1000 ; i++){
    			new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							mix2.getProductPage();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).start();
    			Thread.sleep(1000);
    		}
//			mix2.login();
//			
//			mix2.getProductPage();
//			Thread.sleep(3000);
//			mix2.getProductPage();
//    		JSONObject jsonLogin = mix2.getJsonLogin();
//    		System.out.println(jsonLogin);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    public void getProductPage() throws IOException{
    	  HttpGet httpget = new HttpGet("https://kyfw.12306.cn/passport/captcha/captcha-image");
          httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
          httpget.setHeader("Host", "kyfw.12306.cn");
          CloseableHttpResponse response1 = httpclient.execute(httpget);
          try {
              HttpEntity entity = response1.getEntity();
              String string = UUID.randomUUID().toString();
              
              FileOutputStream fos = new FileOutputStream(new File("d://12306/"+string+".jpg"));
              entity.writeTo(fos);
              System.out.println("Login form get: " + response1.getStatusLine());
              EntityUtils.consume(entity);
              
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
    }
	public void login() throws Exception{
		
		   try {
			   	JSONObject loginJson = null;
			   	
	            HttpGet httpget = new HttpGet("https://account.xiaomi.com/pass/serviceLogin");
	            httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
	            httpget.setHeader("Host", "account.xiaomi.com");
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
	            loginJson = getJsonLogin();
	            RequestBuilder post = RequestBuilder.post();
	            HttpUriRequest login = null;
	            
	            post.setUri(new URI("https://account.xiaomi.com/pass/serviceLoginAuth2"));
	            Iterator<String> keys = loginJson.keys();
	            while(keys.hasNext()){
	            	String key = keys.next();
	            	String value = loginJson.getString(key);
//	            	if(key.equals("callback")){
//	            		post.addParameter("callback","https%3A%2F%2Faccount.xiaomi.com");
//	            		continue;
//	            	}
	            	post.addParameter(key,value);
	            }
	            
	            
	            login = post.addParameter("user", "18610598770")
	            		.addParameter("hash", "4EA55CECEF5A0FE9A8317340CDADE0F1")
	            		.build();
	            login.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
	            login.setHeader("Host", "account.xiaomi.com");
	            CloseableHttpResponse response2 = httpclient.execute(login);
	            try {
	                HttpEntity entity = response2.getEntity();
	                System.out.println("Login form get: " + response2.getStatusLine());
	                ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                
	                FileOutputStream fos = new FileOutputStream(new File("d://xiaomi.txt"));
	                entity.writeTo(fos);
	                EntityUtils.consume(entity);
	                
	                System.out.println("Post logon cookies:");
	                List<Cookie> cookies = cookieStore.getCookies();
	                if (cookies.isEmpty()) {
	                    System.out.println("None");
	                } else {
	                    for (int i = 0; i < cookies.size(); i++) {
	                        System.out.println("- " + cookies.get(i).toString());
	                    }
	                }
	            } finally {
	                response2.close();
	            }
	        } finally {
//	            httpclient.close();
	        }
		}
	
	private JSONObject getJsonLogin() throws IOException, JSONException{
		BufferedReader br = new BufferedReader(new FileReader(new File("d://loginPage.txt")));
		String jsonStr = "";
		String line = null;
		boolean plusFlag = false;
		while((line = br.readLine())!=null){
			if(line.indexOf("var JSP_VAR")!=-1){
				plusFlag = true;
				continue;
			}
			if(plusFlag){
				if(line.equals("};")){
					plusFlag = false;
				}
			}
			if(plusFlag){
				jsonStr += line;
			}
		}
		System.out.println(jsonStr);
		JSONObject  json = new JSONObject("{"+jsonStr+"}"); 
		return json;
	}
}
