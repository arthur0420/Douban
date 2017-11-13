package arthur.mq.message;

import java.io.Serializable;

public class Command implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private byte command =0x0;
	private String topic = "";
	private String tag = "";
	private String key = "";
	// 以上三个用于get
	
	private MessageWrapper message =null; // 用于set
	
	public Command() {
	}
	
	public Command(byte command, String topic, String tag, String key,
			MessageWrapper message) {
		super();
		this.command = command;
		this.topic = topic;
		this.tag = tag;
		this.key = key;
		this.message = message;
	}
	public byte getCommand() {
		return command;
	}
	public void setCommand(byte command) {
		this.command = command;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public MessageWrapper getMessage() {
		return message;
	}
	public void setMessage(MessageWrapper message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		return "Command [command=" + command + ", topic=" + topic + ", tag="
				+ tag + ", key=" + key + ", message=" + message + "]";
	}
	
	
}
