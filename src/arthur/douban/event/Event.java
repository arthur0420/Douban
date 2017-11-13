package arthur.douban.event;

import java.io.Serializable;

public interface Event extends Serializable{
	public Event CallBack(String reponseStr)throws Exception;
	public String getUrl();
}
