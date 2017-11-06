package arthur.douban.queue;


import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import arthur.douban.event.GroupEvent;

public class GroupQueue  {
	static Logger log = Logger.getLogger(GroupQueue.class);
	static LinkedBlockingQueue<GroupEvent> queue = new LinkedBlockingQueue<GroupEvent>();
	static HashMap<Long,GroupEvent> willConsumer =  new HashMap<Long,GroupEvent>();
	static Long messageId = 0L;
	static Long consumerTimeout = 24*60*60*1000l; // 超时间隔时间
	public static void addOneEvent(GroupEvent e){
		queue.add(e);
	}
	
	public static GroupEvent getOneEvent(){
		GroupEvent poll = queue.poll();
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
