package arthur.mq.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import arthur.config.Config;
import arthur.douban.event.Event;
import arthur.douban.event.GroupEvent;
import arthur.douban.event.TopicEvent;
import arthur.mq.message.Command;
import arthur.mq.message.CommandDefine;
import arthur.mq.message.MessageWrapper;
import arthur.mq.queue.MessageQueue;
import arthur.mq.utils.DataFormat;

public class Consumer  extends Thread{
	private static Logger log = Logger.getLogger(Consumer.class);
	private static ExecutorService threadPool = Executors.newCachedThreadPool();  
	private static BufferedOutputStream bos = null;
	private static BufferedInputStream bis = null; 
	private static Socket client = null;
	private static boolean runFlag = true;
	private static long heartBeat = 0l;
	public synchronized static void init() throws Exception  {
		String ip =  Config.getConfig("ip");
		int port = Integer.parseInt(Config.getConfig("port")) ;
		client = new Socket(ip, port);  
        client.setSoTimeout(10000); // 读取超时  
        
        bos= new BufferedOutputStream(client.getOutputStream());
        bis= new BufferedInputStream(client.getInputStream());
        heartBeat = System.currentTimeMillis();
        new Consumer().start();
	}
	/**
	 * 结束 consumer   1.结束线程池 2.告诉 server 结束 3.关闭 socket  4.结束heartbeat线程。
	 * @throws Exception
	 */
	public synchronized  static void end() throws Exception{
		threadPool.shutdown();
		while(true){
			boolean terminated = threadPool.isTerminated();
			if(terminated)break;
			log.info("wait for all thread to be over ");
			Thread.sleep(1000);
		}
		Command command = new Command(CommandDefine.CLOSESERVER, null, null, null, null);
		byte[] byteArray = DataFormat.getByteArray(command);
		bos.write(byteArray);
		bos.flush();
		Thread.sleep(100);
		if(client !=null)
			client.close();
		runFlag = false;
	}
	public synchronized static boolean getMessage(Command command,MessageExecuter executer) throws Exception{
		heartBeat = System.currentTimeMillis();
		byte[] commandByteArray = DataFormat.getByteArray(command);
		bos.write(commandByteArray);
		bos.flush();
		int available =bis.available();
    	while(true){
    		if(available >0){
    			break;
    		}else{
    			Thread.sleep(100);
    		}
    		available = bis.available();
    	}
    	byte[] buff = new byte[available];
    	bis.read(buff);
    	MessageWrapper message = (MessageWrapper)DataFormat.getObjectByByteArray(buff);
    	if(message.getData() == null){
    		return false;
    	}
    	executer.setMessage(message);
    	threadPool.execute(executer);
    	return true;
	}
	public synchronized  static void setMessage(GroupEvent e) throws Exception{
	}
	public synchronized static void heartBeat() throws Exception{
		Command command = new Command(CommandDefine.HEARTBEAT, null, null, null, null);
		byte[] byteArray = DataFormat.getByteArray(command);
		bos.write(byteArray);
		bos.flush();
		heartBeat = System.currentTimeMillis();
	}
	@Override
	public void run() {
		while(runFlag){
			try {
				long h =  System.currentTimeMillis() -  heartBeat;
				if(h > 5 * 60 *1000){
					Consumer.heartBeat();
				}
				Thread.sleep(5 * 60* 1000);
			} catch (Exception e) {
				log.error("发送心跳异常，重建连接",e);
				try {
					Consumer.init();
					break;
				} catch (Exception e1) {
					log.error("初始化异常，退出",e);
					System.exit(0);
				}
			}
		}
	}
}
