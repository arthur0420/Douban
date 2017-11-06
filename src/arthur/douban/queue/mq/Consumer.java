package arthur.douban.queue.mq;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import javax.lang.model.util.SimpleAnnotationValueVisitor6;

import org.apache.log4j.Logger;

import arthur.config.Config;
import arthur.douban.event.GroupEvent;
import arthur.douban.event.TopicEvent;
import arthur.douban.queue.GroupQueue;
import arthur.douban.queue.TopicQueue;

public class Consumer  extends Thread{
	private static Logger log = Logger.getLogger(Consumer.class);
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
	public synchronized  static void end() throws Exception{
		if(client !=null)
			client.close();
		runFlag = false;
	}
	public synchronized static boolean getMessage() throws Exception{
		heartBeat = System.currentTimeMillis();
		bos.write("getE".getBytes());
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
    	int read = bis.read(buff);
    	if(read == 5){
    		log.error("错误:"+new String(buff));
    		return false;
    	}
    	Object objectByByteArray = DataFormat.getObjectByByteArray(buff);
    	String simpleName = objectByByteArray.getClass().getSimpleName();
    	if(simpleName.equals("TopicEvent")){
    		TopicQueue.addOneEvent((TopicEvent)objectByByteArray);
    	}else{
    		GroupQueue.addOneEvent((GroupEvent)objectByByteArray);
    	}
    	return true;
	}
	public synchronized  static void setMessage(GroupEvent e) throws Exception{
		bos.write("setG".getBytes());
		bos.flush();
		byte[] byteArray = DataFormat.getByteArray(e);
		bos.write(byteArray);
		bos.flush();
		heartBeat = System.currentTimeMillis();
	}
	public  synchronized static void setMessage(TopicEvent e) throws Exception{
		bos.write("setT".getBytes());
		bos.flush();
		byte[] byteArray = DataFormat.getByteArray(e);
		bos.write(byteArray);
		bos.flush();
		
		heartBeat = System.currentTimeMillis();
	}
	public synchronized static void heartBeat() throws IOException{
		bos.write("setH".getBytes());
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
