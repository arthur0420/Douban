package arthur.douban.process;


import org.apache.log4j.Logger;

import arthur.config.Config;
import arthur.douban.event.GroupEvent;
import arthur.douban.httpUtils.UHttpClient;

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
			GroupEvent oneEvent = GroupProcess.getOneEvent();
			if(oneEvent !=null){
				if(!oneEvent.pb.getEventExcuteFlag()){ //后续的不执行。
					continue;
				}
				String excute = excute(oneEvent.url);
				oneEvent.CallBack(excute);
			}else{
				
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
						Thread.sleep(3*tryAgainTime* 1000);
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
