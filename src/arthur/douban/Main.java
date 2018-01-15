package arthur.douban;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Signer;
import java.util.Properties;
import java.util.Timer;

import org.apache.log4j.PropertyConfigurator;

import arthur.config.Config;
import arthur.douban.httpUtils.UHttpClient;
import arthur.douban.process.EventProcess;
import arthur.douban.producer.GroupTimerTask;
import arthur.douban.producer.MGroupTimerTask;
import arthur.douban.producer.MTopicTimerTask;
import arthur.douban.producer.TopicTimerTask;
import arthur.mq.client.Consumer;
import arthur.mq.server.ServerHold;

public class Main {
	public static void main(String[] args) throws IOException {
		InputStream resourceAsStream = Main.class.getClassLoader().getResourceAsStream("log4j.properties");
		Properties pp = new Properties();
		pp.load(resourceAsStream);
		PropertyConfigurator.configure(pp);
		String path = Main.class.getClassLoader().getResource("c3p0-config.xml").getPath();
		System.setProperty("com.mchange.v2.c3p0.cfg.xml",path);
		
		try {
			String role = Config.getConfig("role");
			if(role.equals("s")){
				server(); // server
			}else{
				client(); // client
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	public static void server() throws Exception{ // server 端
		int groupScannerInterval = 30;
		int topicScannerInterval = 1;
		String groupInterval = Config.getConfig("groupScannerInterval");
		String topicInterval = Config.getConfig("topicScannerInterval");
		
		groupScannerInterval = Integer.parseInt(groupInterval);
		topicScannerInterval = Integer.parseInt(topicInterval);
		
		Timer groupSanner = new Timer();
		groupSanner.schedule(new MGroupTimerTask(),1000, groupScannerInterval*60*1000);
		
		Timer topicSanner = new Timer();
		topicSanner.schedule(new MTopicTimerTask(), 3000,topicScannerInterval *60*1000);
		
		ServerHold.init();
	}
	public static void client() throws Exception{ // 服务端
		UHttpClient.init();// http 线程池
		
		Consumer.init();  // 和MQserver的连接
		
		EventProcess eventProcess  = new EventProcess();  // 执行
		eventProcess.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread() {  // 关闭 钩子
            public void run() { 
            	Main.releaseClientResource();
            }
		});
	}
	public static void releaseServerResource(){
		try {
			//持久化
			Thread.sleep(1000);
		} catch (Exception e) {
		}
		System.out.println("!!!!!!!!!!!!!!!");
		System.out.println("!!!!!!!!!!!!!!!");
		System.out.println("!!!!!!!!!!!!!!!");
		System.out.println("释放资源，关闭");
	}
	public static void releaseClientResource(){
		try {
			EventProcess.stopRun();
			Consumer.end();
			Thread.sleep(1000);
		} catch (Exception e) {
		}
		System.out.println("!!!!!!!!!!!!!!!");
		System.out.println("!!!!!!!!!!!!!!!");
		System.out.println("!!!!!!!!!!!!!!!");
		System.out.println("释放资源，关闭");
	}
}
