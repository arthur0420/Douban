package arthur.loganalyses;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Nohup {
	
	public static void main(String[] args) throws Exception {
		String path ="C:/Users/ouyangyasi/Desktop/nohup.out";
		File file = new File(path);
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line= null;
		while((line = br.readLine())!=null){
			int indexOf = line.indexOf("ConnectionUtils.java:529");
			if(indexOf!=-1)
				System.out.println(line);
		}
	}
}
