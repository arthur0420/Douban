package arthur.douban.entity;

import java.io.Serializable;

import arthur.douban.dataUtils.Entity;
import arthur.douban.dataUtils.Field;

@Entity(tableName="`group`")
public class Group  implements Serializable{
	/**
	 * 
	 */
	@Field(ignore=true)
	private static final long serialVersionUID = 1L;
	
	
	String id ;
	String name ; // 名字
	String url ; // 
	long breakpoint = 0 ;
	public Group(String id,String name,String url,long breakpoint){
		this.id = id;
		this.name = name;
		this.url = url;
		this.breakpoint = breakpoint;
	}
	public Group(){
		
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getBreakpoint() {
		return breakpoint;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setBreakpoint(long breakpoint) {
		this.breakpoint = breakpoint;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUrl() {
		return url;
	}
}
