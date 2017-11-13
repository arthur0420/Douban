package arthur.mq.server;

import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import arthur.config.Config;

public class ServerHold extends Thread{
	private static Logger log = Logger.getLogger(ServerHold.class);
	private static boolean flag = true;
	private static ServerSocket server = null;
	public static void init() throws Exception {
		int port = Integer.parseInt(Config.getConfig("port")) ;
		server = new ServerSocket(port);
		new ServerHold().start();
	}
	@Override
	public void run() {
		try {
	        Socket client = null;  
	        while(flag){
	            client = server.accept();  
	            System.out.println("get an client");  
	            ServerThread thread = new ServerThread(client);
	            CheckAliveTask.addServerThread(thread);
	            thread.start();
	        }
	        server.close();  
		} catch (Exception e) {
			log.error("socketserver error ",e);
		}
	}
	public static void end(){
		flag= false;
	}
	public static void main(String[] args) throws Exception {
		init();
	}
}
