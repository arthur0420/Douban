package arthur.douban.queue.mq;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import arthur.douban.event.GroupEvent;
import arthur.douban.event.TopicEvent;
import arthur.douban.queue.GroupQueue;
import arthur.douban.queue.TopicQueue;

class ServerThread extends Thread {  
	private static Logger log = Logger.getLogger(ServerThread.class);
    private Socket client = null;
    private long  heartbeat = 0l;
    boolean flag =true;
    public ServerThread(Socket client){  
        this.client = client;  
        heartbeat = System.currentTimeMillis();
    }
    @Override  
    public void run() {  
        try{  
        	//输出
        	BufferedOutputStream bos = new BufferedOutputStream(client.getOutputStream());  
        	//输入
        	BufferedInputStream bis = new BufferedInputStream(client.getInputStream());  
              
            while(flag){  
            	byte[] buff = new byte[1024];
            	int read = bis.read(buff);
            	String command= new String(buff);
            	
            	if(read>=5){
            		log.error("error command:"+command);
            	}
            	command = command.substring(0, read);
            	log.info("command:"+command);
            	switch (command) {
				case "getE":
					getEvent(bos);
					break;
				case "setG":
					setGroup(bis);
					break;
				case "setT":
					setTopic(bis);
					break;
				case "setH":
					heartBeat();
					break;
				case "clz": //关闭当前线程
					flag = false;
					break;
				default:
					log.error("error command:"+command);
					break;
				} 
            	heartbeat = System.currentTimeMillis();
            }
            client.close();
            log.info("正常关闭");
        }catch(Exception e){  
            e.printStackTrace();  
        }  
    }
    private void getEvent(BufferedOutputStream bos) throws Exception{
    	byte[] returnByte = null;
    	GroupEvent oneEvent = GroupQueue.getOneEvent();
    	
		if(oneEvent !=null){
			returnByte = DataFormat.getByteArray(oneEvent);
		}else{
			TopicEvent topicEvent = TopicQueue.getOneEvent();
			if(topicEvent !=null){
				returnByte = DataFormat.getByteArray(topicEvent);
			}
		}
		if(returnByte == null){
			returnByte = "nodata".getBytes(); 
		}
		bos.write(returnByte);
		bos.flush();
    }
    private void setGroup(BufferedInputStream bis) throws Exception{
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
    	Object objectByByteArray = DataFormat.getObjectByByteArray(buff);
    	GroupQueue.addOneEvent((GroupEvent)objectByByteArray);
    }
    private void setTopic(BufferedInputStream bis)throws Exception{
    	int available =bis.available();
    	while(true){ // 等待有数据。
    		if(available >0){
    			break;
    		}else{
    			Thread.sleep(100);
    		}
    		available = bis.available();
    	}
    	byte[] buff = new byte[available];
    	bis.read(buff);
    	Object objectByByteArray = DataFormat.getObjectByByteArray(buff);
    	TopicQueue.addOneEvent((TopicEvent)objectByByteArray);
    }
    private void heartBeat(){
    	heartbeat = System.currentTimeMillis();
    }
    public boolean alive(){
    	long interval =  System.currentTimeMillis() - heartbeat;
    	if(interval >10 *60 *1000){
    		flag = false;
    		return false;
    	}else{
    		return true;
    	}
    }
}  