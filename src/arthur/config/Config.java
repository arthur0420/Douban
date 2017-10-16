package arthur.config;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Properties;

public class Config {
	static Properties config = null ;
	
	public static boolean init(){
		try {
			config = new Properties();
			config.load(new FileInputStream(new File("config.proteties")));
			return true;
		} catch (Exception e) {
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
