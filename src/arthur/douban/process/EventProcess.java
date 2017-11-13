package arthur.douban.process;



import javax.security.auth.callback.Callback;

import org.apache.log4j.Logger;

import arthur.config.Config;
import arthur.douban.event.Event;
import arthur.douban.event.GroupEvent;
import arthur.douban.event.TopicEvent;
import arthur.douban.httpUtils.UHttpClient;
import arthur.mq.client.Consumer;
import arthur.mq.client.MessageExecuter;
import arthur.mq.message.Command;
import arthur.mq.message.CommandDefine;
import arthur.mq.message.MessageWrapper;
import arthur.mq.queue.MessageQueue;
import arthur.mq.utils.DataFormat;

public class EventProcess extends Thread {
	static Logger log = Logger.getLogger(EventProcess.class);
	private static boolean loopFlag = true;  //　 是否继续执行的标志
	
	long requestInterval = 500;
	int tryAgainTime = 0;
	String[] consumerType = null;
	
	public EventProcess(){
		try {
			requestInterval = Long.parseLong(Config.getConfig("requestInterval"));
			consumerType = Config.getConfig("consumerType").split(",");
			if(consumerType== null || consumerType.length == 0){
				log.info("consumerType is null   exit!!");
				System.exit(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		while(loopFlag){
				try {
					for(int i = 0 ; i<consumerType.length ; i++){
						String topic =  consumerType[i];
						Command command = new Command(CommandDefine.GETMESSAGE, topic, null, null, null);
						boolean message = Consumer.getMessage(command, new MessageExecuter() {
							@Override
							public void run() {
								Event event = null;
								try {
									MessageWrapper m = getMessage();
									byte[] data = m.getData();
									event = (Event)DataFormat.getObjectByByteArray(data);
									while(event != null && EventProcess.loopFlag){
										event= excute(event);
									}
								} catch (Exception e) {
									log.error(event, e);
								}
							}
						});
						if(message)break;
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
				try {
					if(loopFlag)
						Thread.sleep(requestInterval);
				} catch (Exception e) {
				}
		}
	}
	
	public  Event excute(Event event) throws Exception{
		String url = event.getUrl();
		String responseStr=null;
		
		while(true){// 五次重试
			responseStr = UHttpClient.get(url);
		 	if(responseStr.equals("-1")){
				log.info("responseStr error");
				if(tryAgainTime ==3){
					tryAgainTime = 0;
					log.error("重试五次，页面加载失败,url:"+url);
					break;
				}else{
					tryAgainTime++;
					try {
						Thread.sleep(tryAgainTime* 1000);
					} catch (Exception e) {
					}
					continue;
				}
			}else{
				break;
			}
		}
		
		if(responseStr !=null){
			Event callBack = event.CallBack(responseStr);
			return callBack;
		}
		return null;
	}
	public static void stopRun() throws Exception{
		loopFlag = false; // 停止循环。
		log.info("stopRun");
	}
}
