package arthur.proxy.process;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import arthur.douban.dataUtils.ConnectionUtils;
import arthur.proxy.entity.Proxy;
import arthur.proxy.httpUtils.UHttpClient;


public class XiciProcess extends ProcessBasic{
	Logger log = Logger.getLogger(XiciProcess.class);
	String url = "http://www.xicidaili.com/nn/";
	int num = 10;
	ProxyProcess p;
	@Override
	public void run() {
		p = new  ProxyProcess();
		p.start();
		for(int i = 1; i<num ; i++){
			String u = url;
			if(i!=1){
				u= u+i;
			}
			log.info("catch url:"+u);
			String string = UHttpClient.get(u);
			parse(string);
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
			}
		}
		p.close();
	}
	private void parse(String str){
		Document html = Jsoup.parse(str);
		Element elementById = html.getElementById("ip_list");
		Elements elementsByTag = elementById.getElementsByTag("tr");
		for(int i = 0 ; i<elementsByTag.size() ;i++){
			Element e = elementsByTag.get(i);
			Elements tags = e.getElementsByTag("td");
			if(tags.size() ==0)continue;
			String ip =tags.get(1).html();
			String port = tags.get(2).html();
			try {
				Proxy proxy = new Proxy(ip,Integer.parseInt(port) , 0l);
//				log.info(ip+":"+port+",set to test");
				p.add(proxy);
			} catch (Exception e2) {
			}
		}
	}
	public static void main(String[] args) {
		new XiciProcess().start();
	}
}
