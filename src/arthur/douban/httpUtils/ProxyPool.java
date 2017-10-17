package arthur.douban.httpUtils;

import java.beans.Customizer;
import java.net.Proxy.Type;
import java.util.ArrayList;




import java.util.List;

import arthur.douban.dataUtils.ConnectionUtils;
import arthur.proxy.entity.HttpProxy;
import arthur.proxy.entity.Proxy;
// 稳定，新。

public class ProxyPool {
	static ArrayList<HttpProxy> l = null;
	static long flashTime = 0;
	public synchronized static HttpProxy getProxy(){
		long currentTimeMillis = System.currentTimeMillis();
		while(true){
			for(int i =0 ; i<l.size(); i++){
				HttpProxy p = l.get(i);
				boolean valid = p.isValid(currentTimeMillis);
				if(valid){
					p.setTime(currentTimeMillis);
					p.setValid(false);
					return p;
				}
			}
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			}
		}
	}
	private void  initList(){
		List<Proxy> entities = ConnectionUtils.getEntities(Proxy.class);
		ArrayList<HttpProxy> templ= new ArrayList<HttpProxy>();
		for (int i = 0; i < entities.size(); i++) {
			Proxy one = entities.get(i);
			HttpProxy p = new HttpProxy(0, one.getId(), one.getPort() , true);
			templ.add(p);
		}
		l =  templ;
	}
}


