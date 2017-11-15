package arthur.douban.httpUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import arthur.config.Config;
import arthur.proxy.entity.HttpProxy;


public class UHttpClient {	
	private static final Logger log = Logger.getLogger(UHttpClient.class);
	private static BasicCookieStore cookieStore ;
	private static PoolingHttpClientConnectionManager phcm ;
	private static CloseableHttpClient httpclient ;
	private static DefaultConnectionKeepAliveStrategy myStrategy ;
	private static RequestConfig rc ;
	private static String USERNAME ;
	private static String PASSWORD;
	public static void init() throws Exception{
		
		rc = RequestConfig.custom().setConnectionRequestTimeout(5000).setSocketTimeout(5000).setConnectTimeout(5000).build();
		myStrategy =  new DefaultConnectionKeepAliveStrategy(){
			@Override
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				long keepAlive = super.getKeepAliveDuration(response, context);
				if(keepAlive !=-1){
					return keepAlive;
				}
				return 10* 1000;
			}
		};
		cookieStore = new BasicCookieStore();
		phcm = new PoolingHttpClientConnectionManager();
		phcm.setMaxTotal(200);
		phcm.setDefaultMaxPerRoute(20);
		
		HttpClientBuilder custom = HttpClients.custom();
		httpclient = custom.
//				setConnectionManager(phcm). // 连接池
				setRetryHandler(new DefaultHttpRequestRetryHandler(3, true)). // 重试 handler
//				setKeepAliveStrategy(myStrategy). // 长连接 策略
				setDefaultCookieStore(cookieStore).build(); // cookiestore*/
		
		//登录
		USERNAME = Config.getConfig("username");
		PASSWORD = Config.getConfig("password");
		
		
		login();
	}
	public static  String  get(String url){
		String returnStr =null;
		CloseableHttpResponse response = null ;
		HttpProxy proxy = null;
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
            	log.info("请求成功 ，url:"+url);
            }else{
            	log.info("请求失败 code:"+statusCode+",url:"+url);
            	returnStr = "-1";
            }
        }catch(Exception e){
        	log.error(e);
        	returnStr = "-1";
        }finally{
        	if(proxy!=null){
        		if(returnStr!=null && returnStr.equals("-1")){
        			ProxyPool.removeProxy(proxy);
        		}else{
        			proxy.close();
        		}
        	}
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
			UHttpClient.init();
			String string = get("https://www.douban.com/group/topic/92740168");
//			log.info(string);
			Document html = Jsoup.parse(string);
//			System.out.println(html);
			Element comments = html.getElementById("comments");
			Elements lis = comments.getElementsByTag("li");
			System.out.println(lis.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void login() throws Exception{
		   try {
	            HttpGet httpget = new HttpGet("https://www.douban.com/");
	            httpget.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.79 Safari/537.36 Edge/14.14393");
	            CloseableHttpResponse response1 = httpclient.execute(httpget);
	            String captcha_id = null; // captcha-id
                String captcha_solution = null; //captcha-solution 
                HttpEntity entity = response1.getEntity();
                
                ByteArrayOutputStream baos = (new ByteArrayOutputStream());
                entity.writeTo(baos);
                byte[] byteArray = baos.toByteArray();
                baos.close();
                EntityUtils.consume(entity);
                
                Document doc = Jsoup.parse(new String(byteArray,"UTF-8"));
                Element captcha_image = doc.getElementById("captcha_image"); // 如果有验证码。
                
                if(captcha_image != null){ // 验证码 相关
                	Elements elementsByAttributeValue = doc.getElementsByAttributeValue("name", "captcha-id");
                	Element captcha_id_element = elementsByAttributeValue.get(0);
                	captcha_id = captcha_id_element.attr("value");
                	
                	String imageUrl = captcha_image.attr("src");
                	log.info("captchaImageUrl:"+imageUrl+",   captcha_id:"+captcha_id);
                	
                	HttpGet httpGet = new HttpGet(imageUrl);
                	CloseableHttpResponse imageResponse = httpclient.execute(httpGet);
                	HttpEntity imageEntity = imageResponse.getEntity();
                	File file = new File("captcha.jpg");
                	file.deleteOnExit();
                	FileOutputStream ffos = new FileOutputStream(file,true);
                	imageEntity.writeTo(ffos);
                	ffos.close();
                	EntityUtils.consume(imageEntity);
                	captcha_solution = CaptchaSolution.getCaptchaFromFile();
                }
	            RequestBuilder post = RequestBuilder.post();
	            HttpUriRequest login = null;
	            
	            post.setUri(new URI("https://www.douban.com/accounts/login"));
	            
	            post.addParameter("form_email", USERNAME)
	            	.addParameter("form_password", PASSWORD);
	            if(captcha_id !=null){
	            	post.addParameter("captcha-id", captcha_id)
	            		.addParameter("captcha-solution", captcha_solution);
	            }
	            login = post.build();
	            login.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
	            
	            CloseableHttpResponse response2 = httpclient.execute(login);
	            try {
	                HttpEntity entity2 = response2.getEntity();
	             	log.info("Login form get: " + response2.getStatusLine());
	                FileOutputStream fos = new FileOutputStream(new File("homePage.html"));
	                entity2.writeTo(fos);
	                EntityUtils.consume(entity2);
	            } finally {
	                response2.close();
	            }
	        } finally {
	        }
		}
	
}
