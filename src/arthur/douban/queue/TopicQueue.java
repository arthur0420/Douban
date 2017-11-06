package arthur.douban.queue;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import arthur.douban.event.GroupEvent;
import arthur.douban.event.TopicEvent;


public class TopicQueue  {
	
	static Logger log = Logger.getLogger(TopicQueue.class);
	static LinkedBlockingQueue<TopicEvent> queue = new LinkedBlockingQueue<TopicEvent>();
	static HashMap<Long,GroupEvent> willConsumer =  new HashMap<Long,GroupEvent>();
	static Long messageId = 0L;
	static Long consumerTimeout = 1*60*1000l; // 超时间隔时间
	public static void addOneEvent(TopicEvent e){
		queue.add(e);
	}
	
	public static TopicEvent getOneEvent(){
		TopicEvent poll = queue.poll();
		return poll;
	}
	
	public static int getQueueSize(){
		int size = queue.size();
		return size;
	}

	public  static long getMessageId(){
		synchronized (messageId) {
			return messageId++;
		}
	}
	
	public static void consumerOneEvent(long messageId){
		willConsumer.remove(messageId);
	}
}
