package arthur.proxy.entity;

import java.net.Proxy;

import arthur.config.Config;
import arthur.douban.httpUtils.ProxyPool;

public class HttpProxy {
	long time ;
	String  ip;
	int port;
	boolean valid;
	
	public HttpProxy(long time, String ip, int port, boolean valid) {
		super();
		this.time = time;
		this.ip = ip;
		this.port = port;
		this.valid = valid;
	}
	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	@Override
	public String toString() {
		return "HttpProxy [time=" + time + ", ip=" + ip + ", port=" + port
				+ ", valid=" + valid + "]";
	}
	public boolean isValid(long now){
		int proxyRequestInterval = Integer.parseInt(Config.getConfig("proxyRequestInterval"));
		if((now - time)> proxyRequestInterval && valid){
			return true;
		}
		return false;
	}
	public void close(){
		valid = true;
		ProxyPool.setBack(this);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj.getClass().getSimpleName().equals("HttpProxy")){
			HttpProxy a = (HttpProxy)obj;
			return ip.equals(a.getIp());
		}else{
			return false;
		}
	}
}
