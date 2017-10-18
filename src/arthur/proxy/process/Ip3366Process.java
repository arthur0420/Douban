package arthur.proxy.process;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import arthur.douban.dataUtils.ConnectionUtils;
import arthur.proxy.entity.Proxy;
import arthur.proxy.httpUtils.UHttpClient;


public class Ip3366Process extends ProcessBasic{
	Logger log = Logger.getLogger(Ip3366Process.class);
	String url = "https://www.hide-my-ip.com/proxylist.shtml";
	ProxyProcess p;
	@Override
	public void run() {
		p = new  ProxyProcess();
//		p.start();
		String string = UHttpClient.get(url);
		parse(string);
		p.close();
	}
	private void parse(String str){
		Pattern pa = Pattern.compile("json = .+/]");
		Matcher matcher = pa.matcher(str);
		boolean find = matcher.find();
		String group2 = matcher.group(0);
		System.out.println(group2);
	/*	for(int i = 0 ; i<elementsByTag.size() ;i++){
			Element e = elementsByTag.get(i);
			Elements tags = e.getElementsByTag("td");
			if(tags.size() ==0)continue;
			String ip =tags.get(0).html();
			String port = tags.get(1).html();
			try {
				Proxy proxy = new Proxy(ip,Integer.parseInt(port) , 0l);
				p.add(proxy);
			} catch (Exception e2) {
			}
		}*/
	}
	public static void main(String[] args) {
		new Ip3366Process().start();
	}
}
