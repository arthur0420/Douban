package arthur.douban;

import java.io.IOException;
import java.util.Timer;

import arthur.douban.entity.Group;
import arthur.douban.process.GroupProcess;
import arthur.douban.process.GroupTimerTask;

public class Main {
	
	public static void main(String[] args) throws IOException {
		init();
	}
	public static void startScan(){
		Timer timer = new Timer();
		timer.schedule(new GroupTimerTask(),3000, 5*60*1000);
	}
	public static void init(){
		GroupProcess groupProcess = new GroupProcess(new Group("1","shenzhen","https://www.douban.com/group/shenzhen/" , 0));
		groupProcess.start();
	}
}
