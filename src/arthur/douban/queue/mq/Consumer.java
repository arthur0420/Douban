package arthur.douban.queue.mq;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;

public class Consumer {
	private static Logger log = Logger.getLogger(Consumer.class);
	public static void init()  {
		
	}
	public static void end(){
		
	}
	public static void getMessage(String topic ) throws Exception{
		
	}
	public static void main(String[] args) throws Exception {
		
        Socket client = new Socket("127.0.0.1", 20006);  
        client.setSoTimeout(10000); // 读取超时  
        BufferedOutputStream bos = new BufferedOutputStream(client.getOutputStream());
        BufferedInputStream bis = new BufferedInputStream(client.getInputStream());  
        boolean flag = true;  
        try{ 
        /*	byte[] buff = new byte[1024];
        	int read = bis.read(buff);
        	System.out.println(new String(buff));*/
            bos.write(("中").getBytes());
            bos.flush();
        }catch(SocketTimeoutException e){  
            System.out.println("Time out, No response");  
        }  
        if(client != null){  
            //如果构造函数建立起了连接，则关闭套接字，如果没有建立起连接，自然不用关闭  
            client.close(); //只关闭socket，其关联的输入输出流也会被关闭  
        }
	}
}
