package arthur.douban.producer;

import java.util.TimerTask;

import arthur.proxy.process.XiciProcess;

public class ProxySsspiderTask extends TimerTask {

	@Override
	public void run() {
		XiciProcess xp = new XiciProcess();
		xp.start();
	}

}
