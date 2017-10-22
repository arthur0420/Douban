package arthur.douban.process;


import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import arthur.douban.event.GroupEvent;

public class GroupProcess  {
	static Logger log = Logger.getLogger(GroupProcess.class);
	static LinkedBlockingQueue<GroupEvent> queue = new LinkedBlockingQueue<GroupEvent>();
	
	public static int getQueueSize(){
		return queue.size();
	}
	public static GroupEvent getOneEvent(){
		GroupEvent poll = queue.poll();
		return poll;
	}
	
	public static void addOneEvent(GroupEvent e){
		queue.add(e);
	}
}
