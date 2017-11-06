package arthur.douban.task;

import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import arthur.douban.dataUtils.ConnectionUtils;
import arthur.douban.entity.Topic;
import arthur.douban.event.TopicEvent;
import arthur.douban.queue.TopicQueue;

public class TopicTimerTask extends TimerTask{
	static Logger log = Logger.getLogger(TopicTimerTask.class);
	@Override
	public void run() {
		int queueSize = TopicQueue.getQueueSize();
		if(queueSize>0){
			log.info("topic event queue is not empty，size:"+queueSize);
			return ;
		}
		List<Topic> entities = ConnectionUtils.getEntitiesCondition(Topic.class, " last_reply_num >flush_reply_num and last_reply_time>flush_time ", " last_reply_num - flush_reply_num desc");
		for(int i = 0 ; i< entities.size() ;i++){
			Topic t = entities.get(i);
			
			int last_reply_num = t.getLast_reply_num();
			int flush_reply_num = t.getFlush_reply_num();
			
			String topicId = t.getId();
			long flush_time = t.getFlush_time();
			String group_name = t.getGroup_name();
			int start = flush_reply_num/100;  
			int end = last_reply_num/100;
			for(;start <=end; start++ ){
				log.info(topicId+","+start+","+end);
				TopicQueue.addOneEvent(new TopicEvent(topicId, flush_time, group_name, start, end));
			}
		}
		System.out.println(123);
	}
	public static void main(String[] args) {
		List<Topic> entities = ConnectionUtils.getEntitiesCondition(Topic.class, " last_reply_num >flush_reply_num", " last_reply_num - flush_reply_num desc");
		System.out.println(entities.size());
	}
}
