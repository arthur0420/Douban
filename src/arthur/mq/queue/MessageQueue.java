package arthur.mq.queue;


import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import arthur.douban.event.Event;
import arthur.mq.message.MessageWrapper;

public class MessageQueue  {
	static Logger log = Logger.getLogger(MessageQueue.class);
	private static MessageQueue single = null;
	private static HashMap<String, MessageQueue> TopicMapperQueue = new HashMap<String, MessageQueue>();
	LinkedBlockingQueue<MessageWrapper> queue = new LinkedBlockingQueue<MessageWrapper>();
	HashMap<Long,MessageWrapper> willConsumer =  new HashMap<Long,MessageWrapper>();
	Long messageId = 0L;
	Long consumerTimeout = 24*60*60*1000l; // 超时间隔时间
	
	private MessageQueue(){}
	
	public  void add(MessageWrapper e){
		queue.add(e);
	}
	public static void addOneMessage(MessageWrapper e){
		String topic = e.getTopic();
		MessageQueue q = queue(topic);
		q.add(e);
	}
	
	public  MessageWrapper getOneMessage(){
		MessageWrapper poll = queue.poll();
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
	public static MessageQueue queue(String topic){
		MessageQueue queue = TopicMapperQueue.get(topic);
		if(queue == null){
			MessageQueue eventQueue = new MessageQueue();
			TopicMapperQueue.put(topic, eventQueue);
			queue =eventQueue;
		}
		return queue;
	}
	public static MessageQueue singleInstance(){
		if(single == null){
			single = new MessageQueue();
		}
		return single;
	}
}
