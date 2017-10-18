package arthur.proxy.entity;

import arthur.douban.dataUtils.Entity;
import arthur.douban.dataUtils.Field;

@Entity(tableName="proxy")
public class Proxy {
	@Field(fiedlName="ip")
	String id ;
	int port;
	long flash_time;
	public Proxy(){}
	public Proxy(String id, int port, long flash_time) {
		this.id = id;
		this.port = port;
		this.flash_time = flash_time;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getPort() {
		return port;
	}
	@Override
	public String toString() {
		return "Proxy [id=" + id + ", port=" + port + ", flash_time="
				+ flash_time + "]";
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	public long getFlash_time() {
		return flash_time;
	}
	public void setFlash_time(long flash_time) {
		this.flash_time = flash_time;
	}
	
}
