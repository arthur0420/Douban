package arthur.douban.event;

import arthur.douban.process.TopicProcess;

public class TopicEvent implements Event {
	String baseUrl = "https://www.douban.com/group/topic/"; ///?start=100
	public int start ;
	public int end ; 
	public String topicId;
	public String id;
	public TopicProcess tp;
	public String url;
	
	public TopicEvent(String topicId, TopicProcess tp,int start,int end) {
		this.url = baseUrl+topicId+"/?start="+start*100;
		this.tp = tp;
		this.start = start;
		this.end = end;
		this.topicId = topicId;
	}
	@Override
	public void CallBack(String reponseStr) throws Exception {
		if(start == 0){
			tp.parseHtmlFirst(reponseStr);
		}else{
			tp.parseHtml(reponseStr);
		}
		if(start != end){  // 每次加载一页， 一页加载完成之后，   start +1，把自己添加到  队列里待执行。
			TopicEvent topicEvent = new TopicEvent(topicId, tp, start+1, end);
			TopicProcess.addOneEvent(topicEvent);
		}
	}
}
