package arthur.douban.queue.mq;

import java.util.LinkedList;
import java.util.TimerTask;

public class CheckAliveTask extends TimerTask {
	private static LinkedList<ServerThread> list = new LinkedList<ServerThread>(); 
	@Override
	public void run() {
		for(int i = 0 ; i< list.size() ;i ++){
			ServerThread one = list.get(i);
			boolean alive = one.alive();
			if(!alive){
				list.remove(i);
			}
		}
	}
	public static void addServerThread(ServerThread one){
		 list.add(one);
	}
}
