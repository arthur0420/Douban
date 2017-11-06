package arthur.douban.event;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import arthur.douban.dataUtils.ConnectionUtils;
import arthur.douban.entity.Comment;
import arthur.douban.entity.Topic;
import arthur.douban.queue.TopicQueue;
import arthur.douban.queue.mq.Consumer;

public class TopicEvent extends MessageWrapper implements Event {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3713819137514739892L;
	private static Logger log = Logger.getLogger(TopicEvent.class);
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	String baseUrl = "https://www.douban.com/group/topic/"; ///?start=100
	
	
	String topicId;
	long flush_time ;
	String group_name;
	
	
	int start ;
	int end ; 
	String url;
	
	
	long topicPublishTime ;
	
	/*public TopicEvent(Topic entity ) {
		this.entity = entity;
		topicId = entity.getId();
		int last_reply_num = entity.getLast_reply_num();
		int flush_reply_num = entity.getFlush_reply_num();
		start = flush_reply_num/100;  
		end = last_reply_num/100;
		url = baseUrl+topicId+"/?start="+start*100;
	}*/
	public TopicEvent(String topicId,long flush_time,String group_name,int start,int end) {
		this.topicId = topicId;
		this.flush_time = flush_time;
		this.group_name = group_name;
		this.start = start;
		this.end = end;
		url = baseUrl+topicId+"/?start="+start*100;
	}
	public String getUrl(){
		return url;
	}
	@Override
	public void CallBack(String reponseStr) throws Exception {
		if(reponseStr.equals("-1")){
			end();
			return ;
		}
		if(start == 0){
			parseHtmlFirst(reponseStr);
		}else{
			parseHtml(reponseStr);
		}
	}
	public  void parseHtml(String str) throws Exception {
		Document html = Jsoup.parse(str);
		processDom(html);
	}
	public void parseHtmlFirst(String str) throws Exception{
		Document html = Jsoup.parse(str);
		try {
			Elements elements = html.getElementsByAttributeValue("class", "color-green");
			Element element = elements.get(0);
			String timeStr = element.text();
			topicPublishTime = parseTime(timeStr);
			Topic topic = new Topic();
			topic.setId(topicId);
			topic.setPublish_time(topicPublishTime);
			ConnectionUtils.updateEntity(topic);
		} catch (Exception e) {
		}
		processDom(html);
	}
	private void processDom(Document html) throws Exception{
		Element comments = html.getElementById("comments");
		Elements lis = comments.getElementsByTag("li");
		log.info("comment size:"+lis.size());
		int sum = 0;
		long comment_lastReply_time = 0;
		List<Comment> cl = new ArrayList<Comment>();
		for(int i = 0 ; i<lis.size() ;i++){
			Element li =  lis.get(i);
			String timeStr = li.getElementsByClass("pubtime").get(0).text();
			Long pubtime = parseTime(timeStr); // 单条发布时间
			if(pubtime > flush_time){
				Elements as = li.getElementsByTag("a");
				Element a = as.get(0);
				String authorHref =a.attr("href");
				String[] split = authorHref.split("/");
				String author = split[split.length-1]; //作者
				
				String content = li.getElementsByTag("p").get(0).text();
				Elements shorts = li.getElementsByClass("short");
				String quoteContent = "";
				if(shorts!=null && shorts.size()>0){
					quoteContent = shorts.get(0).text(); 
					content =quoteContent +"<p>main</p>"+content; //内容
				}
				String id = li.attr("id");
				Comment comment = new Comment(id, topicId, author, content, pubtime, group_name);
				cl.add(comment);
				sum++; 
				comment_lastReply_time  = pubtime;
			}
		}
		if(sum>0){
			Connection connection =null;
			try {
				connection = ConnectionUtils.getConnection();
				connection.setAutoCommit(false);
				ConnectionUtils.batchInsert(cl,connection);
				if(start == end ){
					ConnectionUtils.updateFlushTopic(topicId,comment_lastReply_time ,sum,connection);
				}else{
					ConnectionUtils.updateFlushTopic(topicId,sum,connection);
				}
			} catch (Exception e) {
				if(connection!=null){
					connection.rollback();
					connection.setAutoCommit(true);
					connection.close();
					connection = null;
				}
				throw e;
			}finally{
				if(connection!=null){
					connection.commit();
					connection.setAutoCommit(true);
					connection.close();
				}
			}
		}
	}
	private Long parseTime(	String str){
		Date parse;
		try {
			parse = sdf.parse(str);
			return parse.getTime();
		} catch (ParseException e) {
			return 0l;
		}
	}
	public void end() {
//		TODO 错误情况。
		Topic topic = new Topic();
		topic.setId(topicId);
		topic.setFlush_reply_num(Integer.MAX_VALUE);
		ConnectionUtils.updateEntity(topic);
	}
}
