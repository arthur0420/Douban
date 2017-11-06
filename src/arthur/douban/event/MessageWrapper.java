package arthur.douban.event;

public class MessageWrapper {
	private long messageId;
	private long startTime = System.currentTimeMillis();
	public long getStartTime(){
		return startTime;
	}
	public long getMessageId() {
		return messageId;
	}
	public void setMessageId(long messageId) {
		this.messageId = messageId;
	}
	
}
