package arthur.douban.producer;

import java.util.List;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import arthur.douban.dataUtils.ConnectionUtils;
import arthur.douban.entity.Group;
import arthur.douban.event.GroupEvent;
import arthur.douban.event.MGroupEvent;
import arthur.mq.message.MessageWrapper;
import arthur.mq.queue.MessageQueue;
import arthur.mq.utils.DataFormat;

public class MGroupTimerTask extends TimerTask{
	static Logger log = Logger.getLogger(MGroupTimerTask.class);
	@Override
	public void run() {
		int queueSize = MessageQueue.queue("group").getQueueSize();
		if(queueSize>0){
			log.info("group event queue is not empty，size:"+queueSize);
			return ;
		}
		List<Group> entities = ConnectionUtils.getEntities(Group.class);
		for(int i = 0 ; i< entities.size() ;i++){
			Group g = entities.get(i);
			MGroupEvent groupEvent = new MGroupEvent(0, g, 0);
			byte[] byteArray = null;
			try {
				byteArray = DataFormat.getByteArray(groupEvent);
			} catch (Exception e) {
				log.error("format data error ",e);
				continue;
			}
			MessageWrapper mw = new MessageWrapper("group", null, null, byteArray);
			MessageQueue.addOneMessage(mw);
		}
		System.err.println(123);
	}
	
	public static void main(String[] args) {
		new MGroupTimerTask().run();
	}
}
