package arthur.douban.process;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;

import arthur.douban.dataUtils.ConnectionUtils;
import arthur.douban.entity.Comment;
import arthur.douban.entity.Topic;
import arthur.douban.event.TopicEvent;


public class TopicProcess  extends ProcessBasic{
	static Logger log = Logger.getLogger(TopicProcess.class);
	static LinkedBlockingQueue<TopicEvent> queue = new LinkedBlockingQueue<TopicEvent>();
	Topic entity = null;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public TopicProcess(Topic t){
		entity = t;
	}
	public static TopicEvent getOneEvent(){
		TopicEvent poll = queue.poll();
		return poll;
	}
	public static void addOneEvent(TopicEvent e){
		queue.add(e);
	}
	public static int getQueueSize(){
		return queue.size();
	}
	public static void clearEvent(){
		queue.clear();
	}
	@Override
	public void run() {
		int last_reply_num = entity.getLast_reply_num();
		int flush_reply_num = entity.getFlush_reply_num();
		int startPage = flush_reply_num/100;  
		int endPage = last_reply_num/100;
		TopicEvent topicEvent = new TopicEvent(entity.getId(), this,startPage,endPage);
		queue.add(topicEvent);
	}
	
	@Override
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
	@Override
	public void end() {
	}
}
