package arthur.douban;

import java.io.IOException;
import java.io.InputStream;
import java.security.Signer;
import java.util.Properties;
import java.util.Timer;

import org.apache.log4j.PropertyConfigurator;

import arthur.config.Config;
import arthur.douban.httpUtils.UHttpClient;
import arthur.douban.process.EventProcess;
import arthur.douban.queue.mq.Consumer;
import arthur.douban.queue.mq.ServerHold;
import arthur.douban.task.GroupTimerTask;
import arthur.douban.task.TopicTimerTask;

public class Main {
	public static void main(String[] args) throws IOException {
		InputStream resourceAsStream = Main.class.getClassLoader().getResourceAsStream("log4j.properties");
		Properties pp = new Properties();
		pp.load(resourceAsStream);
		PropertyConfigurator.configure(pp);
		String path = Main.class.getClassLoader().getResource("c3p0-config.xml").getPath();
		System.setProperty("com.mchange.v2.c3p0.cfg.xml",path);
		
		try {
			String s = Config.getConfig("server");
			String c = Config.getConfig("client");
			if(s.equals("true")){
				server(); // server
			}
			if(c.equals("true")){
				client(); // client
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		registerSinal();
	}
	public static void server() throws Exception{ // server 端
		int groupScannerInterval = 30;
		String config = Config.getConfig("groupScannerInterval");
		groupScannerInterval = Integer.parseInt(config);
		if(groupScannerInterval!=0){
			Timer groupSanner = new Timer();
			groupSanner.schedule(new GroupTimerTask(),1000, groupScannerInterval*60*1000);
		}
		Timer topicSanner = new Timer();
		topicSanner.schedule(new TopicTimerTask(), 3000, 30*1000);
		
		ServerHold.init();
	}
	public static void client() throws Exception{ // 服务端
		UHttpClient.init();
		// event池处理线程
		EventProcess eventProcess  = new EventProcess();
		eventProcess.start();
		Consumer.init();
	}
	public static void registerSinal(){
		Runtime.getRuntime().addShutdownHook(new Thread() { 
            public void run() { 
            	Main.releaseResource();
            }
		});
	}
	public static void releaseResource(){
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
		}
		System.out.println("!!!!!!!!!!!!!!!");
		System.out.println("!!!!!!!!!!!!!!!");
		System.out.println("!!!!!!!!!!!!!!!");
		System.out.println("释放资源，关闭");
	}
}
