package arthur.xiaomi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Filter {
	public static void run(int i){
		System.out.println(i*2);
	}
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		List<Future> a = new ArrayList<Future>();
		ExecutorService es = Executors.newFixedThreadPool(5);
		for (int i = 0; i < 10; i++) {
			final String name2 = i+"thread"; 
			Future submit = es.submit(new Runnable() {
				@Override
				public void run() {
					System.out.println("start threadName"+name2);
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
					}
					System.out.println("end threadName"+name2);
				}
			});
			a.add(submit);
			System.out.println("index "+i);
		}
		Thread.sleep(10000);
		for(int i = 0 ; i<a.size() ;i++){
			Future future = a.get(i);
			boolean done = future.isDone();
			System.out.println(i+"threadName"+",done:"+done);
			if(done)System.out.println(future.get());
		}
		while(true){
			es.shutdown();
			boolean terminated = es.isTerminated();
			if(terminated){
				break;
			}else{
				Thread.sleep(1000);
				System.out.println("sleep");
			}
		}
		System.out.println("over");
	}
}
