package arthur.douban.process;

 public abstract class ProcessBasic extends Thread {
	int pageSize = 25;
	public abstract void parseHtml(String str);
	public  abstract void end();
}
