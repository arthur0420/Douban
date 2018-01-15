package arthur.douban.producer;

import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import arthur.douban.dataUtils.ConnectionUtils;
import arthur.douban.entity.Topic;
import arthur.douban.event.MTopicEvent;
import arthur.douban.event.TopicEvent;
import arthur.mq.message.MessageWrapper;
import arthur.mq.queue.MessageQueue;
import arthur.mq.utils.DataFormat;

public class MTopicTimerTask extends TimerTask{
	static Logger log = Logger.getLogger(MTopicTimerTask.class);
	@Override
	public void run() {
		int queueSize = MessageQueue.queue("topic").getQueueSize();
		if(queueSize>0){
			log.info("topic event queue is not empty，size:"+queueSize);
			return ;
		}
		List<Topic> entities = ConnectionUtils.getEntitiesCondition(Topic.class, " last_reply_num >flush_reply_num and last_reply_time>flush_time ", " last_reply_num - flush_reply_num desc   limit 0, 1000");
		for(int i = 0 ; i< entities.size() ;i++){
			Topic t = entities.get(i);
			
			int last_reply_num = t.getLast_reply_num();
			int flush_reply_num = t.getFlush_reply_num();
			long publish_time = t.getPublish_time();
			String topicId = t.getId();
			long flush_time = t.getFlush_time();
			String group_name = t.getGroup_name();
			
			int start = flush_reply_num/25;  
			int end = last_reply_num/25;
			if(publish_time == 0){
				MTopicEvent topicEvent = new MTopicEvent(topicId, flush_time, group_name, -1, end);
				byte[] byteArray = null;
				try {
					byteArray = DataFormat.getByteArray(topicEvent);
				} catch (Exception e) {
					log.error("format data error ",e);
					continue;
				}
				MessageWrapper mw = new MessageWrapper("topic", null, null, byteArray);
				MessageQueue.addOneMessage(mw);
			}
			for(;start <=end; start++ ){
				MTopicEvent topicEvent = new MTopicEvent(topicId, flush_time, group_name, start, end);
				byte[] byteArray = null;
				try {
					byteArray = DataFormat.getByteArray(topicEvent);
				} catch (Exception e) {
					log.error("format data error ",e);
					continue;
				}
				MessageWrapper mw = new MessageWrapper("topic", null, null, byteArray);
				MessageQueue.addOneMessage(mw);
			}
		}
	}
	public static void main(String[] args) {
		List<Topic> entities = ConnectionUtils.getEntitiesCondition(Topic.class, " last_reply_num >flush_reply_num", " last_reply_num - flush_reply_num desc  limit 0, 10000");
		System.out.println(entities.size());
	}
}
