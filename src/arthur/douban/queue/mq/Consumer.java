package arthur.douban.queue.mq;

import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.rocketmq.client.consumer.DefaultMQPullConsumer;
import org.apache.rocketmq.client.consumer.MessageQueueListener;
import org.apache.rocketmq.client.consumer.PullResult;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;

public class Consumer {
	private static Logger log = Logger.getLogger(Consumer.class);
	static  DefaultMQPullConsumer consumer ; 
	public static void init() throws MQClientException{
		consumer= new DefaultMQPullConsumer("doubanConsumer");
		consumer.setNamesrvAddr("176.122.148.51:9876");
		consumer.setInstanceName("Consumer");
		consumer.registerMessageQueueListener("douban", null);
		consumer.start();
		
	}
	public static void end(){
		consumer.shutdown();
	}
	public static void getMessage(String topic ) throws Exception{
		Set<MessageQueue> mqs = consumer.fetchSubscribeMessageQueues(topic);
//	    log.info("mqs.size() " + mqs.size());
		
	    for (MessageQueue mq : mqs) {
	    	System.out.println("-------");
//	    	log.info("Consume message from queue: " + mq );
	    	
	         int cnter = 0;
	         //每个队列里无限循环，分批拉取未消费的消息，直到拉取不到新消息为止
	         while (cnter++ < 1) {
	             long offset = consumer.fetchConsumeOffset(mq, true);
	             offset = offset < 0 ? 0 : offset;
//	             log.info("消费进度 Offset: " + offset);
	             PullResult result = consumer.pull(mq, null, offset,1);
//	             log.info("接收到的消息集合" + result);
	             
	             switch (result.getPullStatus()) {
		             case FOUND:
		                 if (result.getMsgFoundList() != null) {
		                     int prSize = result.getMsgFoundList().size();
		                     if (prSize != 0) {
		                         for (MessageExt me : result.getMsgFoundList()) {
		                             // 消费每条消息，如果消费失败，比如更新数据库失败，就重新再拉一次消息
		                        	 String messageTopic = me.getTopic();
		                        	 String messageTag = me.getTags();
		                        	 byte[] body = me.getBody();
		                        	 try {
		                        		 Object objectByByteArray = DataFormat.getObjectByByteArray(body);
			                        	 log.info("getObject"+objectByByteArray.getClass()+",topic:"+messageTopic+",tag:"+messageTag+",queueid:"+mq.getQueueId()+",offset:"+offset);
		                        	 } catch (Exception e) {
		                        		 log.info("body"+new String(body,"utf-8")+",topic:"+messageTopic+",tag:"+messageTag+",queueid:"+mq.getQueueId()+",offset:"+offset);
		                        	 }
		                         }
		                     }
		                 }
		                 // 获取下一个下标位置
		                 offset = result.getNextBeginOffset();
		                 // 消费完后，更新消费进度
		                 consumer.updateConsumeOffset(mq, offset);
		                 break;
		             case NO_MATCHED_MSG:
		                 System.out.println("没有匹配的消息");
		                 break;
		             case NO_NEW_MSG:
		                 System.out.println("没有未消费的新消息");
		                 //拉取不到新消息，跳出 SINGLE_MQ 当前队列循环，开始下一队列循环。
		                 break ;
		             case OFFSET_ILLEGAL:
		                 System.out.println("下标错误");
		                 break;
		             default:
		                 break;
	             }
	         }
	     }
	}
	public static void main(String[] args) throws Exception {
		init();
		for (int i = 0; i < 10; i++) {
			Thread.sleep(5000);
			getMessage("douban");
		}
		end();
	}
}
