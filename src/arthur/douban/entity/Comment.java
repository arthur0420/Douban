package arthur.douban.entity;



import arthur.douban.dataUtils.Entity;
import arthur.douban.dataUtils.Field;

/**
 * @author ouyangyasi
 *
 */
@Entity(tableName="comment")
public class Comment {
	String id;
	String topic_id ;
	String author;
	String content;
	long time;
	String group_name;
	
	public Comment(){
		
	}
	public Comment(String id, String topic_id, String author, String content,
			long time, String group_name) {
		super();
		this.id = id;
		this.topic_id = topic_id;
		this.author = author;
		this.content = content;
		this.time = time;
		this.group_name = group_name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTopic_id() {
		return topic_id;
	}
	public void setTopic_id(String topic_id) {
		this.topic_id = topic_id;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public String getGroup_name() {
		return group_name;
	}
	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}
	
}
