package arthur.douban.process;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import arthur.config.Config;
import arthur.douban.dataUtils.ConnectionUtils;
import arthur.douban.entity.Group;
import arthur.douban.entity.Topic;
import arthur.douban.event.GroupEvent;

public class GroupProcess extends ProcessBasic {
	static Logger log = Logger.getLogger(GroupProcess.class);
	static LinkedBlockingQueue<GroupEvent> queue = new LinkedBlockingQueue<GroupEvent>();
	Group entity ;
	long nowBreakpoint = 0;
	long firstTime = 0; // 第一页的 第一个帖子的，最新回复时间 ，将成为下一个   断点。
	int tryAgainTime = 0;
	int year = 0;
	public int loadPageNum = 10; //初始化，每次加载十页
	boolean eventExcuteFlag = true;
	
	
	public static GroupEvent getOneEvent(){
		GroupEvent poll = queue.poll();
		return poll;
	}
	
	
	String group_name ;
	public GroupProcess(Group entity){
		this.entity = entity;
		nowBreakpoint = entity.getBreakpoint();
		group_name = entity.getName();
		Calendar c =Calendar.getInstance();
		year =c.get(Calendar.YEAR); 
		try {
			loadPageNum =  Integer.parseInt(Config.getConfig("loadPageNum"));
		} catch (Exception e) {
			log.error("config 格式化失败", e);
		}
	}
	@Override
	public void run() {
		String basicUrl = entity.getUrl()+"discussion";
		int pageIndex = 0;
		while(pageIndex<loadPageNum){
			String realUrl = basicUrl + "?start="+(pageIndex*25);
			GroupEvent groupEvent = new GroupEvent(realUrl, this,pageIndex);
			queue.add(groupEvent);
			/*	String responseStr= UHttpClient.get(realUrl);
			 	if(responseStr.equals("-1")){
				log.info("responseStr error");
				if(tryAgainTime ==5){
					tryAgainTime = 0;
					log.error("重试五次，页面加载失败,name:"+entity.getName()+",pageIndex:"+pageIndex);
					break;
				}else{
					tryAgainTime++;
					try {
						Thread.sleep(3*tryAgainTime* 1000);
					} catch (Exception e) {
					}
					continue;
				}
			}
			parseHtml(responseStr); // 解析这一页的html
			*/
			log.info(entity.getName()+",page:"+pageIndex);
			pageIndex++;
		}
	}
	// 解析一页
	@Override
	public void parseHtml(String str) {
		// if has no topic then throws a exception.
		Document html = Jsoup.parse(str);
		Elements elements = html.getElementsByAttributeValue("class", "olt");
		if(elements.size() == 0){
			log.info("elements null; html:"+str);
			return ;
		}
		Element table = elements.get(0);
		Elements trs = table.getElementsByTag("tr");
		
		for(int i = 1 ; i< trs.size(); i++ ){
			
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
			if(firstTime == 0){
				System.err.println(new Date(parseTime));
				firstTime = parseTime;
			}
			if(parseTime < nowBreakpoint){
				eventExcuteFlag = false;
				end();
				break;
			}
			String[] split = topicUrl.split("/");
			String id = split[split.length-1];
			String[] split2 = authorUrl.split("/");
			String author_id = split2[split2.length-1];
			
			Topic topic = new Topic(id, titleText, author_id, parseTime, 0, "", 0, group_name, 0, last_reply_num); 
			ConnectionUtils.insertEntity(topic);
		}
		/*if(trs.size()<25){
			log.info("size:"+trs.size());
			newBreakpoint = firstTime;
		}*/
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
	public boolean getEventExcuteFlag(){
		return eventExcuteFlag;
	}
	@Override
	public void end() {
		entity.setBreakpoint(firstTime);
		ConnectionUtils.updateEntity(entity);
	}
	/*public static void main(String[] args) {
		String string = UHttpClient.get("https://www.douban.com/group/shenzhen/discussion?start=1000");
		GroupProcess groupProcess = new GroupProcess(new Group("1","shenzhen","https://www.douban.com/group/shenzhen/" , 0));
		groupProcess.parseHtml(string);
	}*/
}
