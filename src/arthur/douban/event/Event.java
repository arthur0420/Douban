package arthur.douban.event;

import java.io.Serializable;

public interface Event extends Serializable{
	public void CallBack(String reponseStr)throws Exception;
}
