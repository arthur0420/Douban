package arthur.douban.task;

import java.util.List;
import java.util.TimerTask;

import arthur.douban.dataUtils.ConnectionUtils;
import arthur.douban.entity.Group;
import arthur.douban.process.GroupProcess;

public class GroupTimerTask extends TimerTask{
	@Override
	public void run() {
		List<Group> entities = ConnectionUtils.getEntities(Group.class);
		for(int i = 0 ; i< entities.size() ;i++){
			Group g = entities.get(i);
			GroupProcess groupProcess = new GroupProcess(g);
			groupProcess.start();
		}
	}
}
