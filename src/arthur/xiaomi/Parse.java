package arthur.xiaomi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class Parse {
	static String dir = "C:/Users/ouyangyasi/Desktop/20171211";
	static String outputFileName = "parsed1211.csv";
	static int lineCount =0 ; 
	static int errorCount = 0;
	static BufferedWriter bw = null;
	public static void main(String[] args) throws Exception {
		bw = new BufferedWriter(new FileWriter(new File("C:/Users/ouyangyasi/Desktop/"+outputFileName)));
		bw.write("时间,资金账号,ip地址,手机品牌,系统版本,app版本,错误的行");
		bw.newLine();
		File dirFile = new File(dir);
		File[] listFiles = dirFile.listFiles();
		for(int i = 0 ; i< listFiles.length ; i++){
			File file = listFiles[i];
			parseFile(file);
		}
		String overStr = lineCount+"lineCount,"+errorCount+"errorCount";
		System.out.println(overStr);
		bw.write(overStr);
		bw.close();
	}
	public static void parseFile(File f) throws Exception{
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line= "";
		while((line = br.readLine())!=null){
			if(line.equals(""))continue;
			try {
				parseLine(line);
			} catch (Exception e) {
				System.err.println(line);
				System.exit(0);
			}
			lineCount++;
		}
		bw.flush();
		br.close();
	}
	public static void parseLine (String line) throws Exception{
		String errorLine = "";
		String time = line.substring(0,23);
		String[] split = line.split(",");
		String netaddrStr = split[4];
		String netaddr = "";
		String appVersion = "";
		String phoneType = "";
		String systemVersion = "";
		String financialAccount = split[10];
		financialAccount = financialAccount.substring(8);
		try {
			int blankIndex = netaddrStr.indexOf(" ");
			if(blankIndex!=-1){
				netaddr = netaddrStr.substring(8, blankIndex);
				netaddrStr = netaddrStr.substring(blankIndex);
				int cnFlag = 0;
				if(netaddrStr.indexOf("／")!=-1){
					cnFlag = 1;
				}
				String[] netSplit = netaddrStr.split("/");
				systemVersion =  netSplit[3-cnFlag];
				appVersion = netSplit[4-cnFlag];
				phoneType = netSplit[5-cnFlag];
			}
		} catch (Exception e) {
			errorCount++;
			errorLine = line;
		}
		// 时间,资金账号,ip地址,手机品牌,系统版本,app版本,错误的行
		String dataLine = time+","+financialAccount+","+netaddr+","+phoneType+","+systemVersion+","+appVersion+","+errorLine.replaceAll(",", "|");
		bw.write(dataLine);
		bw.newLine();
	}
}
