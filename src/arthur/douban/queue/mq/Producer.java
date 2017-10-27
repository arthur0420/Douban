package arthur.douban.queue.mq;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.apache.log4j.Logger;

public class Producer {
	private static Logger log = Logger.getLogger(Producer.class);
	public static void init() throws Exception {
        ServerSocket server = new ServerSocket(20006);  
        Socket client = null;  
        boolean f = true;  
        while(f){  
            client = server.accept();  
            System.out.println("get an client");  
            new Thread(new ServerThread(client)).start();  
        }  
        server.close();  
	}
	public static void end(){
		
	}
	public static void main(String[] args) throws Exception {
		init();
	}
}
class ServerThread implements Runnable {  

    private Socket client = null;
    private long  heartbeat = 0l;
    public ServerThread(Socket client){  
        this.client = client;  
    }
    public void test(){
    	
    }
    @Override  
    public void run() {  
        try{  
        	//输出
        	BufferedOutputStream bos = new BufferedOutputStream(client.getOutputStream());  
            
        	//输入
        	BufferedInputStream bis = new BufferedInputStream(client.getInputStream());  
            boolean flag =true;  
            while(flag){  
            	byte[] buff = new byte[1024];
            	int read = bis.read(buff);
            	for(int i = 0 ; i<read ; i++){
            		System.out.println(buff[i]);
            	}
            	
            	try {
    				Thread.sleep(3000);
    			} catch (Exception e) {
    				// TODO: handle exception
    			}
                bos.write("123".getBytes());
                bos.flush();
                System.out.println(123);
            	
            	
            }
            
            
        }catch(Exception e){  
            e.printStackTrace();  
        }  
    }  

}  