package arthur.douban.queue;


import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import arthur.douban.event.Event;

public class EventQueue  {
	static Logger log = Logger.getLogger(EventQueue.class);
	private static EventQueue single = null;
	private static HashMap<String, EventQueue> TopicMapperQueue = new HashMap<String, EventQueue>();
	LinkedBlockingQueue<Event> queue = new LinkedBlockingQueue<Event>();
	HashMap<Long,Event> willConsumer =  new HashMap<Long,Event>();
	Long messageId = 0L;
	Long consumerTimeout = 24*60*60*1000l; // 超时间隔时间
	
	private EventQueue(){}
	
	public  void addOneEvent(Event e){
		queue.add(e);
	}
	
	public  Event getOneEvent(){
		Event poll = queue.poll();
		return poll;
	}
	
	public  int getQueueSize(){
		int size = queue.size();
		return size;
	}
	
	public   long getMessageId(){
		synchronized (messageId) {
			return messageId++;
		}
	}
	public  void consumerOneEvent(long messageId){
		willConsumer.remove(messageId);
	}
	public static EventQueue queue(String topic){
		EventQueue queue = TopicMapperQueue.get(topic);
		if(queue == null){
			EventQueue eventQueue = new EventQueue();
			TopicMapperQueue.put(topic, eventQueue);
			queue =eventQueue;
		}
		return queue;
	}
	public static EventQueue singleInstance(){
		if(single == null){
			single = new EventQueue();
		}
		return single;
	}
}
