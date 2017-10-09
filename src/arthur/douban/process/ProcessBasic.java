package arthur.douban.process;

 public abstract class ProcessBasic extends Thread {
	int pageSize = 25;
	abstract void parseHtml(String str);
	abstract void end();
}
