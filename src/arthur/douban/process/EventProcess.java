package arthur.douban.process;



import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import arthur.config.Config;
import arthur.douban.event.Event;
import arthur.douban.httpUtils.UHttpClient;
import arthur.mq.client.Consumer;
import arthur.mq.message.Command;
import arthur.mq.message.CommandDefine;
import arthur.mq.message.MessageWrapper;
import arthur.mq.utils.DataFormat;

public class EventProcess extends Thread {
	static Logger log = Logger.getLogger(EventProcess.class);
	private static boolean loopFlag = true;  //　 是否继续执行的标志
	private static ExecutorService threadPool = Executors.newFixedThreadPool(20);
	
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
		long timestamp = 0;
		while(loopFlag){
			timestamp = System.currentTimeMillis();
			try {
				for(int i = 0 ; i<consumerType.length ; i++){
					String topic =  consumerType[i];
					Command command = new Command(CommandDefine.GETMESSAGE, topic, null, null, null);
					MessageWrapper message = Consumer.getMessage(command);
					byte[] data = message.getData();
					if(data ==null && i==consumerType.length-1){ // 当前循环，最后一个topic没有数据，睡5秒
						Thread.sleep(5000);
						log.info("has no data waiting 5 second");
						break;
					}
					if(data == null)continue;  // 当前循环，没有数据，下一个topic。
					Event event = (Event)DataFormat.getObjectByByteArray(data);
					
					if(topic.equals("group")){ // 业务代码
						while(event!=null && EventProcess.loopFlag){
							timestamp = System.currentTimeMillis();  // 用自己的时间， 一个message可能有多次执行。
							String responseStr = excute(event);
							event = event.CallBack(responseStr);
							long waitTime =requestInterval + timestamp - System.currentTimeMillis();
							if(waitTime > 0l ){
								Thread.sleep(waitTime);
							}
						}
						break;
					}else if(topic.equals("topic")){
						
						final String responseStr = excute(event);
						final Event finalEvent = event;
						threadPool.execute(new Runnable() {
							@Override
							public void run() {
								try {
									finalEvent.CallBack(responseStr);
								} catch (Exception e) {
									log.error("帖子解析错误", e);
								}
							}
						});
						long waitTime =requestInterval + timestamp - System.currentTimeMillis(); // 使用 TOP循环的时间。 每次只取一个。
						if(waitTime > 0l ){
							Thread.sleep(waitTime);
						}
						break;
					}else{
						Thread.sleep(requestInterval);
						log.error("错误的topic值");
						break;
					}
				}
			}catch (Exception e) {
				log.error("EventProcess execute error",e);
			}
		}
	}
	
	public  String excute(Event event) throws Exception{
		String url = event.getUrl();
		String responseStr=null;
		
		while(true){// 五次重试
			responseStr = UHttpClient.get(url);
		 	if(responseStr.equals("-1")){
				log.info("responseStr error");
				if(tryAgainTime ==3){
					tryAgainTime = 0;
					log.error("重试三次，页面加载失败,url:"+url);
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
		return responseStr;
	}
	
	public static void stopRun() throws Exception{
		
		loopFlag = false; // 停止循环。
		log.info("stopRun,wait 3 second, for unfinished event");
		Thread.sleep(3000);
		threadPool.shutdown(); 
		while(true){
			boolean terminated = threadPool.isTerminated();
			if(terminated)break;
			log.info("wait for all thread to be over ");
			Thread.sleep(1000);
		}
		
	}
	public static void addTask(Runnable task){
		threadPool.execute(task);
	}
}
