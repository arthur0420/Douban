package arthur.douban.event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import arthur.config.Config;
import arthur.douban.dataUtils.ConnectionUtils;
import arthur.douban.entity.Group;
import arthur.douban.entity.Topic;
import arthur.douban.process.EventProcess;

public class MGroupEvent implements Event {
	/**
	 * 
	 */
	private static final long serialVersionUID = 236482878525617905L;
	private static Logger log = Logger.getLogger(MGroupEvent.class);
	private static int loadPageNum = 10; //初始化，每次加载十页
	
	{
		try {
			loadPageNum =  Integer.parseInt(Config.getConfig("loadPageNum"));
		} catch (Exception e) {
			log.error("config 格式化失败", e);
		}
	}
	
	int index ;
	String url;
	Group entity;
	long nowBreakpoint = 0;
	long firstTime = 0; // 第一页的 第一个帖子的，最新回复时间 ，将成为下一个   断点。
	int year = 0;
	
	public MGroupEvent(int index ,Group group,long firstTime) {
		
		this.index = index;
		entity = group;
		this.firstTime = firstTime;
		
		String basicUrl = entity.getUrl();
		url = basicUrl + "?start="+(index*25);
		
		nowBreakpoint = entity.getBreakpoint();
		
		Calendar c =Calendar.getInstance();
		year =c.get(Calendar.YEAR); 
	}
	public String getUrl(){
		return url;
	}
	@Override
	public Event CallBack(String reponseStr) {
		if(reponseStr.equals("-1")){  // 请求失败的情况。
			log.info("group over by request_error id:"+entity.getName());
			return null;
		}
		final ArrayList<Topic> topicList = parseHtml(reponseStr);
		if(topicList!=null)
			EventProcess.addTask(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < topicList.size(); i++) {
						Topic topic = topicList.get(i);
						ConnectionUtils.insertEntity(topic);
					}
				}
			});
		
		if(index == loadPageNum-1){ // 超过了 配置文件中设定的，一次扫描页数，停止扫描。 index 从0开始。
			log.info("group over by index == loadPageNum id:"+entity.getId()
					+" ,index:"+ index+"== loadPageNum:"+loadPageNum);
			end();
			return null;
		}
		if(nowBreakpoint == -1){
			log.info("group over by nowBreakpoint == -1 id:"+entity.getId());
			end();
			return null;
		}
		return new MGroupEvent(index+1, entity, firstTime);
	}
	// 解析一页
	public ArrayList<Topic> parseHtml(String str) {
		// if has no topic then throws a exception.
		Document html = Jsoup.parse(str);
		Elements elements = html.getElementsByClass("topic-list");
		if(elements.size() == 0){
			nowBreakpoint = -1;
			log.info("elements null; html:"+str);
			return null;
		}
		Element list = elements.get(0);
		Elements lis = list.getElementsByTag("li");
		ArrayList<Topic> insertTopicList = new ArrayList<Topic>();
		int size = lis.size();
		for(int i = 1 ; i< size; i++ ){
			
			Element element = lis.get(i);
			Elements tds = element.getElementsByTag("a");
			
			Element author = tds.get(0);
			String authorUrl =  author.attr("href");
			
			Element title = tds.get(1);
			String titleText = title.attr("title");
			String topicUrl = title.attr("href");
			
			
			
			
			Element reply = element.getElementsByClass("left").get(0);
			String text = reply.text();
			text = text.replace("回应", "");
			int last_reply_num = Integer.parseInt(text);
			
			Element time =element.getElementsByClass("right").get(0);
			String timeText = time.text();
			
			long parseTime = parseTime(timeText);
			if(firstTime == 0){ // 第一个帖子的回复时间，就是 breakpoint of group , finished scan 
				firstTime = parseTime;
			}
			if(parseTime < nowBreakpoint){  // 扫描 间断点 时间戳 超过，停止扫描
				log.info("parseTime > nowBreakpoint    parseTime:"+parseTime +",nowBreakpoint"+ nowBreakpoint+", id:"+entity.getId());
				nowBreakpoint = -1;
				break;
			}
			String[] split = topicUrl.split("/");
			String id = split[split.length-1];
			String[] split2 = authorUrl.split("/");
			String author_id = split2[split2.length-1];
			
			Topic topic = new Topic(); //id, titleText, author_id, parseTime, 0, "", 0, group_name, 0, last_reply_num
			topic.setId(id);
			topic.setTitle(titleText);
			topic.setAuthor_id(author_id);
			topic.setLast_reply_time(parseTime);
			topic.setGroup_name(entity.getName());
			topic.setLast_reply_num(last_reply_num);
			insertTopicList.add(topic);
		}
		if(size <24){ // 最后一页，停止扫描。
			log.info("size<25    id:"+entity.getId());
			nowBreakpoint = -1;
		}
		return insertTopicList;
	}
	long parseTime(String str){
		long time = 0;
		try {
			SimpleDateFormat sdf =null;
			if(str.length() == 11){
				str = year+"-"+str;
				sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			}else{
				sdf = new SimpleDateFormat("yyyy-MM-dd");
			}
			Date parse;
			parse = sdf.parse(str);
			time = parse.getTime();
		} catch (Exception e) {
			log.error("时间解析错误",e);
		}
		return time;
	}
	
	public void end() {
		entity.setBreakpoint(firstTime);
		ConnectionUtils.updateEntity(entity);
	}
	@Override
	public String toString() {
		return "GroupEvent [index=" + index + ", url=" + url + ", entity="
				+ entity + ", nowBreakpoint=" + nowBreakpoint + ", firstTime="
				+ firstTime + ", year=" + year + "]";
	}
	
}
