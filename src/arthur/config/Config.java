package arthur.config;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;

public class Config {
	static Properties config = null ;
	
	public static boolean init(){
		try {
			config = new Properties();
			config.load(Config.class.getClassLoader().getResourceAsStream("config.proteties"));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	public static String getConfig(String key){
		if(config == null) init();
		String value = "";
		if(config.containsKey(key)){
			return config.getProperty(key);
		}
		return value;
	}
}
