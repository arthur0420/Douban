package arthur.mq.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

import arthur.douban.event.Event;
import arthur.douban.event.GroupEvent;
import arthur.douban.event.TopicEvent;
import arthur.mq.message.Command;
import arthur.mq.message.CommandDefine;
import arthur.mq.message.MessageWrapper;
import arthur.mq.queue.MessageQueue;
import arthur.mq.utils.DataFormat;

class ServerThread extends Thread {  
	private static Logger log = Logger.getLogger(ServerThread.class);
    private Socket client = null;
    private long  heartbeat = 0l;
    private BufferedInputStream bis;
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
        	bis = new BufferedInputStream(client.getInputStream());  
            
            while(flag){
            	byte[] buff = new byte[1024];
            	int read = bis.read(buff);
            	Command command = (Command)DataFormat.getObjectByByteArray(buff,0,read);
            	
            	log.info("command:"+command);
            	switch (command.getCommand()) {
				case CommandDefine.CLOSESERVER:
					flag = false;
					break;
				case CommandDefine.GETMESSAGE:
					getMessage(bos, command);
					break;
				case CommandDefine.SETMESSAGE:
					setMessage(command);
					break;
				case CommandDefine.HEARTBEAT:
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
            log.error("接受命令错误异常关闭", e);
        }
    }
    private void getMessage(BufferedOutputStream bos,Command command) throws Exception{
    	byte[] returnByte = null;
    	String topic = command.getTopic();
		MessageWrapper mw = MessageQueue.queue(topic).getOneMessage();
		if(mw !=null){
			returnByte = DataFormat.getByteArray(mw);
		}
		if(returnByte == null){
			returnByte = DataFormat.getByteArray(new MessageWrapper(null, null, null, null));
		}
		bos.write(returnByte);
		bos.flush();
    }
    private void setMessage(Command c) throws Exception{
    	
    }
    public boolean alive() {
    	long interval =  System.currentTimeMillis() - heartbeat;
    	if(interval >10 *60 *1000){
    		flag = false;//多余的， client.关闭会导致，阻塞read，throw SocketException。
    		try {
				client.close();
			} catch (IOException e) {
				try {
					bis.close();
				} catch (IOException e1) {
					log.error("关闭 sockt 失败。" );
					System.exit(-9);
				}
			}
    		return false;
    	}else{
    		return true;
    	}
    }
}  