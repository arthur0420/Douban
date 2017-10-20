package arthur.douban;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Timer;

import org.apache.commons.lang.Validate;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

import arthur.config.Config;
import arthur.douban.dataUtils.ConnectionUtils;
import arthur.douban.entity.Group;
import arthur.douban.process.EventProcess;
import arthur.douban.process.GroupProcess;
import arthur.douban.task.GroupTimerTask;
import arthur.douban.task.ProxySsspiderTask;
import arthur.douban.task.TopicTimerTask;
import arthur.proxy.process.EnableSannerProcess;

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
	}
	public static void startScan(){
	/*	Timer proxySanner = new Timer();
		proxySanner.schedule(new ProxySsspiderTask(),2*60*1000, 10*60*1000);
		Timer validateProxyEnableTimer = new Timer();
		validateProxyEnableTimer.schedule(new EnableSannerProcess(), 6 * 60 * 1000, 10*60*1000);*/
		int groupScannerInterval = 30;
		try {
			String config = Config.getConfig("groupScannerInterval");
			groupScannerInterval = Integer.parseInt(config);
		} catch (Exception e) {   
		}   
		Timer groupSanner = new Timer();
		groupSanner.schedule(new GroupTimerTask(),1000, groupScannerInterval*60*1000);
		
		Timer topicSanner = new Timer();
		topicSanner.schedule(new TopicTimerTask(), 3000, 5*60*1000);
	}
	public static void init(){
		// event池处理线程
		EventProcess eventProcess  = new EventProcess();
		eventProcess.start();
	}
}
