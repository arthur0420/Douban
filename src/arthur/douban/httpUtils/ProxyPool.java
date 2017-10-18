package arthur.douban.httpUtils;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import arthur.douban.dataUtils.ConnectionUtils;
import arthur.proxy.entity.HttpProxy;
import arthur.proxy.entity.Proxy;
// 稳定，新。

public class ProxyPool {
	static Logger log = Logger.getLogger(ProxyPool.class);
	static BlockingQueue<HttpProxy> q = null;
	static long flashTime = 0;
	public  static HttpProxy getProxy(){
		if(q == null || q.size() == 0){
			initList();
		}
		long currentTimeMillis = System.currentTimeMillis();
		while(true){
			HttpProxy p = q.poll();
			if(p != null){
				boolean valid = p.isValid(currentTimeMillis);
				if(valid){
					p.setTime(currentTimeMillis);
					p.setValid(false);
					return p;
				}else{
					q.add(p);
					try {
						Thread.sleep(50);
					} catch (Exception e) {
					}
					continue;
				}
			}else{
				log.info("proxyPool is empty");
			}
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				
			}
		}
	}
	public static void setBack(HttpProxy p){
		q.add(p);
	}
	public static void removeProxy(HttpProxy hp){
		log.info("remove httpproxy, queue.size = "+q.size());
		q.remove(hp);
		ConnectionUtils.deleteEntity(new Proxy(hp.getIp(),hp.getPort(),0));
	}
	
	//
	public static void  initList(){
		List<Proxy> entities = ConnectionUtils.getEntities(Proxy.class);
		BlockingQueue<HttpProxy> tempq= new LinkedBlockingQueue<HttpProxy>();
		for (int i = 0; i < entities.size(); i++) {
			Proxy one = entities.get(i);
			HttpProxy p = new HttpProxy(0, one.getId(), one.getPort() , true);
			tempq.add(p);
		}
		tempq.add(new HttpProxy(0,"127.0.0.1",0, true));
		q =  tempq;
	}
	public static void main(String[] args) {
		initList();
		System.out.println(q.size());
		HttpProxy proxy = getProxy();
		System.out.println(proxy);
	}
}


