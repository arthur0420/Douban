package arthur.douban.event;

import arthur.douban.process.GroupProcess;

public class GroupEvent implements Event {
	public int index ;
	public String url;
	public GroupProcess pb;
	public GroupEvent(String url, GroupProcess pb,int index) {
		this.url = url;
		this.pb = pb;
		this.index = index;
	}
	@Override
	public void CallBack(String reponseStr) {
		pb.parseHtml(reponseStr);
		if(index == pb.loadPageNum-1){
			pb.end();
		}
	}
}
