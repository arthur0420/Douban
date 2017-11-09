package arthur.douban.process;



import org.apache.log4j.Logger;

import arthur.config.Config;
import arthur.douban.event.Event;
import arthur.douban.event.GroupEvent;
import arthur.douban.event.TopicEvent;
import arthur.douban.httpUtils.UHttpClient;
import arthur.douban.queue.EventQueue;
import arthur.douban.queue.mq.Consumer;

public class EventProcess extends Thread {
	static Logger log = Logger.getLogger(EventProcess.class);
	private static boolean loopFlag = true;  //　 是否继续执行的标志
	
	long requestInterval = 500;
	int tryAgainTime = 0;
	String consumerType ="";
	
	 
	
	public EventProcess(){
		try {
			requestInterval = Long.parseLong(Config.getConfig("requestInterval"));
			consumerType = Config.getConfig("consumerType");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		while(loopFlag){
			synchronized (EventProcess.class) {
				Event oneEvent = null;
				try {
					oneEvent = EventQueue.singleInstance().getOneEvent();
					if(oneEvent !=null){
						String excute = excute(oneEvent.getUrl());
						oneEvent.CallBack(excute);
					}else{
						boolean message = false;
						if(consumerType.equals("g")){
							message= Consumer.getMessage();
						}else if(consumerType.equals("t")){
							message= Consumer.getTopicMessage();
						}
						if(message){
							continue;
						}
					}
				}catch (Exception e) {
					e.printStackTrace();
					log.error("url:"+oneEvent.getUrl(),e);
				}
				try {
					if(loopFlag)
						Thread.sleep(requestInterval);
				} catch (Exception e) {
				}
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
	public static void stopRun() throws Exception{
		loopFlag = false; // 停止循环。
		log.info("stopRun");
		synchronized (EventProcess.class) { // 等锁       
			Event oneEvent = EventQueue.singleInstance().getOneEvent();
			log.info("stopRun  oneEvent is null:"+(oneEvent == null));
			if(oneEvent!=null){
				String simpleName = oneEvent.getClass().getSimpleName();
				if(simpleName.equals("GroupEvent"))
					Consumer.setMessage((GroupEvent)oneEvent);
				else if(simpleName.equals("TopicEvent"))
					Consumer.setMessage((TopicEvent)oneEvent);
			}
		}
	}
}
