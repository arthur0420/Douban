package arthur.douban.process;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.security.auth.login.LoginContext;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import arthur.douban.dataUtils.ConnectionUtils;
import arthur.douban.entity.Group;
import arthur.douban.entity.Topic;
import arthur.douban.httpUtils.UHttpClient;

public class GroupProcess extends ProcessBasic {
	static Logger log = Logger.getLogger(GroupProcess.class);

	Group entity ;
	long newBreakpoint = 0;
	long nowBreakpoint = 0;
	long firstTime = 0; // 第一页的 第一个帖子的，最新回复时间 ，将成为下一个   断点。
	int tryAgainTime = 0;
	public GroupProcess(Group entity){
		this.entity = entity;
		nowBreakpoint = entity.getBreakpoint();
	}
	@Override
	public void run() {
		String basicUrl = entity.getUrl()+"discussion";
		int pageIndex = 0;
		while(true){
			String realUrl = basicUrl + "?start="+(pageIndex*25);
			String responseStr= UHttpClient.get(realUrl);
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
			if(newBreakpoint != 0 ){
				break;
			}
			log.info(entity.getName()+",page:"+pageIndex);
			pageIndex++;
			try {
				Thread.sleep(500);
			} catch (Exception e) {
			}
		}
		end();
	}
	// 解析一页
	@Override
	void parseHtml(String str) {
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
			//to topicProcess
			
			
			Element author = tds.get(1).getElementsByTag("a").get(0);
			String authorName = author.text();
			String authorUrl =  author.attr("href");
			// to userProcess
			
			Element time = tds.get(3);
			String timeText = time.text();
			long parseTime = parseTime(timeText);
			if(firstTime == 0){
				firstTime = parseTime;
			}
			if(parseTime < nowBreakpoint){
				newBreakpoint = firstTime;
				break;
			}
			String[] split = topicUrl.split("/");
			String id = split[split.length-1];
			String[] split2 = authorUrl.split("/");
			String author_id = split2[split2.length-1];
			Topic topic = new Topic(id, titleText, author_id, parseTime, 0, "");
			ConnectionUtils.insertEntity(topic);
		}
		if(trs.size()<25){
			log.info("size:"+trs.size());
			newBreakpoint = firstTime;
		}
	}
	long parseTime(String str){
		long time = 0;
		try {
			SimpleDateFormat sdf =null;
			if(str.length() == 11){
				sdf = new SimpleDateFormat("MM-dd HH:mm");
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
	@Override
	void end() {
		entity.setBreakpoint(firstTime);
		ConnectionUtils.updateEntity(entity);
	}
}
