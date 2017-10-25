package arthur.douban.queue.mq;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;

import com.mysql.fabric.xmlrpc.base.Array;

import arthur.douban.entity.Group;
import arthur.douban.entity.Topic;
import arthur.douban.event.GroupEvent;
import arthur.douban.event.TopicEvent;

public class Producer {
	private static Logger log = Logger.getLogger(Producer.class);
	private static DefaultMQProducer producer ;
	public static void init() throws MQClientException{
		producer = new DefaultMQProducer("doubanProducer");
		producer.setNamesrvAddr("176.122.148.51:9876");
		producer.setInstanceName("Producer");
		producer.start();
	}
	public static void end(){
		producer.shutdown();
	}
	public static void sendMessage(String topic , String tag , byte[] messge) throws MQClientException, RemotingException, MQBrokerException, InterruptedException{
		 Message msg = new Message(topic, tag ,
                 messge);
         SendResult sendResult = producer.send(msg);
         log.info(sendResult);
	}
	public static void sendMessage(String topic , String tag , byte[][] messages) throws MQClientException, RemotingException, MQBrokerException, InterruptedException{
		List<Message> list = new ArrayList<Message>();
		for(int i = 0 ; i< messages.length ;i++){
			byte[] one = messages[i];
			Message message = new Message(topic, tag, one);
			list.add(message);
		}
        SendResult sendResult = producer.send(list);
        log.info(sendResult);
	}
	public static void sendMessage(List<Message> list) throws MQClientException, RemotingException, MQBrokerException, InterruptedException{
        SendResult sendResult = producer.send(list);
        log.info(sendResult);
	}
	public static void main(String[] args) throws Exception {
		init();
		List<Message> listG = new ArrayList<Message>();
		List<Message> listT = new ArrayList<Message>();
		for(int i = 0 ; i< 20; i++){
			if(i%2 == 0){
				GroupEvent groupEvent = new GroupEvent(0, new Group("1321", "1",	 "www.baidu.com"+i, 0), 0);
				byte[] byteArray = DataFormat.getByteArray(groupEvent);
				sendMessage("douban", "group", byteArray);
			}else{
				Topic topic = new Topic();
				topic.setTitle("title"+i);
				TopicEvent te = new TopicEvent(topic);
				byte[] byteArray = DataFormat.getByteArray(te);
				sendMessage("douban", "topic", byteArray);
			}
		}
//		sendMessage(listG);
//		sendMessage(listT);
		end();
	}
}
