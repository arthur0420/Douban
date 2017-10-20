package arthur.douban.task;

import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import arthur.douban.dataUtils.ConnectionUtils;
import arthur.douban.entity.Group;
import arthur.douban.entity.Topic;
import arthur.douban.process.GroupProcess;
import arthur.douban.process.TopicProcess;

public class TopicTimerTask extends TimerTask{
	static Logger log = Logger.getLogger(TopicTimerTask.class);
	@Override
	public void run() {
		int queueSize = TopicProcess.getQueueSize();
		   
		if(queueSize>0){
			log.info("topic event queue is not empty，siez:"+queueSize);
			return ;
		}
		List<Topic> entities = ConnectionUtils.getEntitiesCondition(Topic.class, " last_reply_num >flush_reply_num", " last_reply_num - flush_reply_num desc");
		for(int i = 0 ; i< entities.size() ;i++){
			Topic t = entities.get(i);
			TopicProcess tp = new TopicProcess(t);
			tp.start();
		}
	}
	public static void main(String[] args) {
		List<Topic> entities = ConnectionUtils.getEntitiesCondition(Topic.class, " last_reply_num >flush_reply_num", " last_reply_num - flush_reply_num desc");
		System.out.println(entities.size());
	}
}
