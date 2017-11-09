package arthur.douban.task;

import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import arthur.douban.dataUtils.ConnectionUtils;
import arthur.douban.entity.Group;
import arthur.douban.event.GroupEvent;
import arthur.douban.queue.EventQueue;

public class GroupTimerTask extends TimerTask{
	static Logger log = Logger.getLogger(GroupTimerTask.class);
	@Override
	public void run() {
		int queueSize = EventQueue.queue("group").getQueueSize();
		if(queueSize>0){
			log.info("group event queue is not empty，size:"+queueSize);
			return ;
		}
		List<Group> entities = ConnectionUtils.getEntities(Group.class);
		for(int i = 0 ; i< entities.size() ;i++){
			Group g = entities.get(i);
			EventQueue.queue("group").addOneEvent(new GroupEvent(0, g, 0));
		}
	}
}
