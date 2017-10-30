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

public class TopicEvent implements Event {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3713819137514739892L;
	private static Logger log = Logger.getLogger(TopicEvent.class);
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	String baseUrl = "https://www.douban.com/group/topic/"; ///?start=100
	Topic entity = null;
	
	int start ;
	int end ; 
	String topicId;
	String id;
	String url;
	
	
	
	
	public TopicEvent(Topic entity) {
		this.entity = entity;
		topicId = entity.getId();
		int last_reply_num = entity.getLast_reply_num();
		int flush_reply_num = entity.getFlush_reply_num();
		start = flush_reply_num/100;  
		end = last_reply_num/100;
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
		if(start != end){  // 每次加载一页， 一页加载完成之后，   start +1，把自己添加到  队列里待执行。
			int flush_reply_num = entity.getFlush_reply_num();
			entity.setFlush_reply_num(flush_reply_num+100); // 下一页
//			TopicQueue.addOneEvent();
			Consumer.setMessage(new TopicEvent(entity));
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
			Long parseTime = parseTime(timeStr);
			entity.setPublish_time(parseTime);
		} catch (Exception e) {
		}
		processDom(html);
	}
	private void processDom(Document html) throws Exception{
		Element comments = html.getElementById("comments");
		Elements lis = comments.getElementsByTag("li");
		log.info("comment size:"+lis.size());
		int sum = 0;
		long flush_time = 0;
		List<Comment> cl = new ArrayList<Comment>();
		for(int i = 0 ; i<lis.size() ;i++){
			Element li =  lis.get(i);
			String timeStr = li.getElementsByClass("pubtime").get(0).text();
			Long pubtime = parseTime(timeStr); // 发布时间
			if(pubtime > entity.getFlush_time()){
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
				
				Comment comment = new Comment(id, entity.getId(), author, content, pubtime, entity.getGroup_name());
				cl.add(comment);
				sum++;
				flush_time = pubtime;
			}
		}
		if(sum>0){
			int flush_reply_num = entity.getFlush_reply_num();
			entity.setFlush_reply_num(flush_reply_num+sum);
			entity.setFlush_time(flush_time);
			try {
				Connection connection = ConnectionUtils.getConnection();
				connection.setAutoCommit(false);
				ConnectionUtils.batchInsert(cl,connection);
				ConnectionUtils.updateEntity(entity,connection);
				connection.commit();
				connection.setAutoCommit(true);
				connection.close();
			} catch (Exception e) {
				throw e;
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
		entity.setFlush_reply_num(Integer.MAX_VALUE);
		ConnectionUtils.updateEntity(entity);
	}
}
