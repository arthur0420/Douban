package arthur.douban.httpUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.log4j.Logger;

public class CaptchaSolution {
	private static final Logger log = Logger.getLogger(CaptchaSolution.class);
	public static String getCaptchaFromFile(){
		File file = new File("captcha.txt");
		file.deleteOnExit();
		
		try {
			while(!file.exists()){
				Thread.sleep(1000);
				log.info("waiting for captcha code");
			}
			FileReader fr = new FileReader(file);
			BufferedReader bis = new BufferedReader(fr);
			String readLine = bis.readLine();
			fr.close();
			bis.close();
			return readLine;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return "";
	}
}
