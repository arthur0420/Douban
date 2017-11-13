package arthur.mq.message;

public class MessageWrapper {
	private long messageId; // 队列中心提供。
	private String topic ;
	private String tag; 
	private String key;
	private byte[] data;
	private long startTime = System.currentTimeMillis();   //创建对象时设置。
	
	public MessageWrapper(String topic, String tag, String key, byte[] data) {
		super();
		this.topic = topic;
		this.tag = tag;
		this.key = key;
		this.data = data;
	}
	public MessageWrapper() {
	}
	public long getStartTime(){
		return startTime;
	}
	public long getMessageId() {
		return messageId;
	}
	public void setMessageId(long messageId) {
		this.messageId = messageId;
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
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
}
