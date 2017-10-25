package arthur.douban.process;



import org.apache.log4j.Logger;

import arthur.config.Config;
import arthur.douban.event.GroupEvent;
import arthur.douban.event.TopicEvent;
import arthur.douban.httpUtils.UHttpClient;
import arthur.douban.queue.GroupQueue;
import arthur.douban.queue.TopicQueue;

public class EventProcess extends Thread {
	static Logger log = Logger.getLogger(EventProcess.class);
	long requestInterval = 500;
	int tryAgainTime = 0;
	public EventProcess(){
		try {
			requestInterval = Long.parseLong(Config.getConfig("requestInterval"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		while(true){
			try {
				GroupEvent oneEvent = GroupQueue.getOneEvent();
				if(oneEvent !=null){
					String excute = excute(oneEvent.getUrl());
					oneEvent.CallBack(excute);
				}else{
					TopicEvent topicEvent = TopicQueue.getOneEvent();
					if(topicEvent !=null){
						String excute = excute(topicEvent.getUrl());
						topicEvent.CallBack(excute);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error(e);
			}
			try {
				Thread.sleep(requestInterval);
			} catch (Exception e) {
			}
		}
	}
	public String excute(String url){
		while(true){
			String responseStr= UHttpClient.get(url);
		 	if(responseStr.equals("-1")){
				log.info("responseStr error");
				if(tryAgainTime ==5){
					tryAgainTime = 0;
					log.error("重试五次，页面加载失败,url:"+url);
					return "-1";
				}else{
					tryAgainTime++;
					try {
						Thread.sleep(tryAgainTime* 1000);
					} catch (Exception e) {
					}
					continue;
				}
			}else{
				return responseStr;
			}
		}
	}
}
