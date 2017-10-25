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
			TopicQueue.addOneEvent(new TopicEvent(t));
		}
	}
	public static void main(String[] args) {
		List<Topic> entities = ConnectionUtils.getEntitiesCondition(Topic.class, " last_reply_num >flush_reply_num", " last_reply_num - flush_reply_num desc");
		System.out.println(entities.size());
	}
}
