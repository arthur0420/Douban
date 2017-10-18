package arthur.proxy.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import arthur.douban.dataUtils.ConnectionUtils;
import arthur.proxy.entity.Proxy;
import arthur.proxy.httpUtils.UHttpClient;
/**
 * 
 * 一个验证 proxy 是不是有效，并进行一系列操作的 线程。
 * @author ouyangyasi
 *
 */

public class ProxyProcess extends ProcessBasic{
	static Logger log = Logger.getLogger(ProxyProcess.class);
	private Queue<Proxy> q = new LinkedBlockingQueue<>();
	private List<TestThread> l = new ArrayList<TestThread>();
	boolean runFlag = true;
	public  void add(Proxy p) {
		q.add(p);
	}
	public void close(){
		runFlag = false;
	}
	@Override
	public void run() {
		while(q.size()!=0 || runFlag){
			for(int i = 0 ; i<l.size(); i++){
				TestThread thread = l.get(i);
				if(thread.getState().equals(Thread.State.TERMINATED)){ // 线程终结
					l.remove(thread);
					
				}else if(thread.isTimeout()){ // 线程超时
					log.info(thread.p.toString()+",timeout");
					thread.interrupt();
					l.remove(thread);
				}
			}
			if(l.size()>30){ //　线程池　31个。
				try {
					log.info("thread heap is full , wait....... queue.size:"+q.size());
					Thread.sleep(5000);
				} catch (Exception e) {
				}
				continue;
			}
			if(q.size() == 0){
				try {
					log.info("thread heap is empty , wait.......");
					Thread.sleep(5000);
				} catch (Exception e) {
				}
				continue;
			}
			Proxy p = q.remove();
			TestThread test = new TestThread(p);
			l.add(test);
			test.start();
		}
	}
}
class TestThread extends Thread{
	long time ;
	Proxy p;
	TestThread(Proxy p){
		this.p = p;
		time = System.currentTimeMillis();
	}
	@Override
	public void run() {
		boolean testProxyIp = UHttpClient.testProxyIp(p.getId(), p.getPort());
		if(testProxyIp){
			p.setFlash_time(System.currentTimeMillis());
			ConnectionUtils.insertEntity(p);
		}else{
			ConnectionUtils.deleteEntity(p);
		}
	}
	public boolean isTimeout() {
		long t =  System.currentTimeMillis() - time;
		if(t/1000 == 10){
			return true;
		}else{
			return false;
		}
	}
}