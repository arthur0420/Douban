package arthur.douban;

import java.io.IOException;
import java.io.InputStream;
import java.security.Signer;
import java.util.Properties;
import java.util.Timer;

import org.apache.log4j.PropertyConfigurator;

import arthur.config.Config;
import arthur.douban.process.EventProcess;
import arthur.douban.task.GroupTimerTask;

public class Main {
	
	public static void main(String[] args) throws IOException {
		InputStream resourceAsStream = Main.class.getClassLoader().getResourceAsStream("log4j.properties");
		Properties pp = new Properties();
		pp.load(resourceAsStream);
		PropertyConfigurator.configure(pp);
		String path = Main.class.getClassLoader().getResource("c3p0-config.xml").getPath();
		System.setProperty("com.mchange.v2.c3p0.cfg.xml",path);
		
		startScan();
		init();
		registerSinal();
	}
	public static void startScan(){
		int groupScannerInterval = 30;
		try {
			String config = Config.getConfig("groupScannerInterval");
			groupScannerInterval = Integer.parseInt(config);
		} catch (Exception e) {   
		}   
		if(groupScannerInterval!=0){
			Timer groupSanner = new Timer();
			groupSanner.schedule(new GroupTimerTask(),1000, groupScannerInterval*60*1000);
		}
		Timer topicSanner = new Timer();
//		topicSanner.schedule(new TopicTimerTask(), 3000, 5*60*1000);
	}
	public static void init(){
		// event池处理线程
		EventProcess eventProcess  = new EventProcess();
		eventProcess.start();
	}
	public static void registerSinal(){
		Runtime.getRuntime().addShutdownHook(new Thread() { 
            public void run() { 
            	Main.releaseResource();
            }
		});
	}
	public static void releaseResource(){
		//TODO 释放资源。
	}
}
