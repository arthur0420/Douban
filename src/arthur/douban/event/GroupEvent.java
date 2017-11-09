package arthur.douban.event;

import java.text.SimpleDateFormat;
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
import arthur.douban.queue.EventQueue;

public class GroupEvent extends MessageWrapper implements Event {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2364828785256179054L;
	private static Logger log = Logger.getLogger(GroupEvent.class);
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
	
	public GroupEvent(int index ,Group group,long firstTime) {
		
		this.index = index;
		entity = group;
		this.firstTime = firstTime;
		
		String basicUrl = entity.getUrl();
		url = basicUrl + "discussion?start="+(index*25);
		
		nowBreakpoint = entity.getBreakpoint();
		
		Calendar c =Calendar.getInstance();
		year =c.get(Calendar.YEAR); 
	}
	public String getUrl(){
		return url;
	}
	@Override
	public void CallBack(String reponseStr) {
		if(reponseStr.equals("-1")){  // 请求失败的情况。
			log.info("group over by request_error id:"+entity.getId());
//			end();
			return ;
		}
		parseHtml(reponseStr);
		if(index == loadPageNum-1){ // 超过了 配置文件中设定的，一次扫描页数，停止扫描。 index 从0开始。
			log.info("group over by index == loadPageNum id:"+entity.getId()
					+" ,index:"+ index+"== loadPageNum:"+loadPageNum);
			end();
			return ;
		}
		if(nowBreakpoint == -1){
			log.info("group over by nowBreakpoint == -1 id:"+entity.getId());
			end();
			return ;
		}
		EventQueue.singleInstance().addOneEvent(new GroupEvent(index+1, entity, firstTime));
	}
	// 解析一页
	public void parseHtml(String str) {
		// if has no topic then throws a exception.
		Document html = Jsoup.parse(str);
		Elements elements = html.getElementsByAttributeValue("class", "olt");
		if(elements.size() == 0){
			nowBreakpoint = -1;
			log.info("elements null; html:"+str);
			return ;
		}
		Element table = elements.get(0);
		Elements trs = table.getElementsByTag("tr");
		int size = trs.size();
		for(int i = 1 ; i< size; i++ ){
			
			Element element = trs.get(i);
			Elements tds = element.getElementsByTag("td");
			Element title = tds.get(0).getElementsByTag("a").get(0);
			String titleText = title.attr("title");
			String topicUrl = title.attr("href");
			
			Element author = tds.get(1).getElementsByTag("a").get(0);
			String authorUrl =  author.attr("href");
			
			Element reply = tds.get(2);
			String text = reply.text();
			int last_reply_num = text.equals("")?0:Integer.parseInt(text);
			
			Element time = tds.get(3);
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
			ConnectionUtils.insertEntity(topic);
		}
		if(size <25){ // 最后一页，停止扫描。
			log.info("size<25    id:"+entity.getId());
			nowBreakpoint = -1;
		}
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
	
}
