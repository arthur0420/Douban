package arthur.proxy.process;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import arthur.douban.dataUtils.ConnectionUtils;
import arthur.douban.httpUtils.ProxyPool;
import arthur.proxy.entity.HttpProxy;
import arthur.proxy.entity.Proxy;

//扫描数据库中现有的proxy ，是否有效。
public class EnableSannerProcess extends TimerTask {
	@Override
	public void run() {
		ProxyProcess  pp = new ProxyProcess();
		
		List<Proxy> entities = ConnectionUtils.getEntities(Proxy.class);
		for (int i = 0; i < entities.size(); i++) {
			Proxy one = entities.get(i);
			pp.add(one);
		}
		pp.start();
		pp.close();
		ProxyPool.initList();
	}
	public static void main(String[] args) {
		new EnableSannerProcess().run();
	}
}
