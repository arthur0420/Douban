package arthur.mq.client;

import arthur.mq.message.MessageWrapper;

public abstract class MessageExecuter implements Runnable {
	private  MessageWrapper message ;
	
	public void setMessage(MessageWrapper message){
		this.message = message;
	}
	public MessageWrapper getMessage(){
		return message;
	}
}
