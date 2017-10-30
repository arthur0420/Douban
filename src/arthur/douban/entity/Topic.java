package arthur.douban.entity;



import java.io.Serializable;

import arthur.douban.dataUtils.Entity;
import arthur.douban.dataUtils.Field;

/**
 * @author ouyangyasi
 *
 */
@Entity(tableName="topic")
public class Topic implements Serializable{
	/**
	 * 
	 */
	@Field(ignore=true)
	private static final long serialVersionUID = 1L;
	
	@Field(fiedlName="topic_id")
	String id;
	String title ;
	String author_id;
	long last_reply_time;
	long publish_time;
	String content;
	long flush_time;
	String group_name;
	int flush_reply_num;
	int last_reply_num;
	public static void main(String[] args) {
		String author_id2 = new Topic().getAuthor_id();
		System.out.println(author_id2);
	}
	public Topic(String id, String title, String author_id,
			long last_reply_time, long publish_time, String content,
			long flush_time, String group_name, int flush_reply_num,
			int last_reply_num) {
		this.id = id;
		this.title = title;
		this.author_id = author_id;
		this.last_reply_time = last_reply_time;
		this.publish_time = publish_time;
		this.content = content;
		this.flush_time = flush_time;
		this.group_name = group_name;
		this.flush_reply_num = flush_reply_num;
		this.last_reply_num = last_reply_num;
	}
	
	public Topic(){
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor_id() {
		return author_id;
	}

	public void setAuthor_id(String author_id) {
		this.author_id = author_id;
	}

	public long getLast_reply_time() {
		return last_reply_time;
	}

	public void setLast_reply_time(long last_reply_time) {
		this.last_reply_time = last_reply_time;
	}

	public long getPublish_time() {
		return publish_time;
	}

	public void setPublish_time(long publish_time) {
		this.publish_time = publish_time;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getFlush_time() {
		return flush_time;
	}

	public void setFlush_time(long flush_time) {
		this.flush_time = flush_time;
	}

	public String getGroup_name() {
		return group_name;
	}

	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}

	public int getFlush_reply_num() {
		return flush_reply_num;
	}

	public void setFlush_reply_num(int flush_reply_num) {
		this.flush_reply_num = flush_reply_num;
	}

	public int getLast_reply_num() {
		return last_reply_num;
	}

	public void setLast_reply_num(int last_reply_num) {
		this.last_reply_num = last_reply_num;
	}

	@Override
	public String toString() {
		return "Topic [id=" + id + ", title=" + title + ", author_id="
				+ author_id + ", last_reply_time=" + last_reply_time
				+ ", publish_time=" + publish_time + ", content=" + content
				+ ", flush_time=" + flush_time + ", group_name=" + group_name
				+ ", flush_reply_num=" + flush_reply_num + ", last_reply_num="
				+ last_reply_num + "]";
	}
	
	
}
