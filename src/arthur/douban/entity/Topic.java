package arthur.douban.entity;


import org.apache.log4j.Logger;

import arthur.douban.dataUtils.Entity;
import arthur.douban.dataUtils.Field;

@Entity(tableName="topic")
public class Topic {
	
	@Field(fiedlName="topic_id")
	String id;
	
	String title ;
	String author_id;
	long last_reply_time;
	long publish_time;
	String content;
	
	public void setLast_reply_time(long last_reply_time) {
		this.last_reply_time = last_reply_time;
	}
	public void setPublish_time(long publish_time) {
		this.publish_time = publish_time;
	}
	public Topic(String id, String title,
			String author_id, long last_reply_time, long publish_time,
			String content) {
		this.id = id;
		this.title = title;
		this.author_id = author_id;
		this.last_reply_time = last_reply_time;
		this.publish_time = publish_time;
		this.content = content;
	}
	public Topic(){
	}
	public String getTitle() {
		return title;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getLast_reply_time() {
		return last_reply_time;
	}
	public long getPublish_time() {
		return publish_time;
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
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	/*public static void insert(Topic topic){
		Connection conn = null;
		Statement state = null;
		try {
			conn = ConnectionUtils.getConnection();
			state = conn.createStatement();
			String existSql = "select * from topic where topic_id =  '"+topic.getTopic_id()+"'";
			ResultSet r1 = state.executeQuery(existSql);
			if(r1.next()){
				
			}else{
				StringBuilder insertSql = new StringBuilder();
				
			}
		} catch (SQLException e) {
			log.error("",e);
		}finally{
			try {if(state!=null){state.close();}} catch (Exception e2) {}
			try {if(conn!=null){conn.close();}} catch (Exception e2) {}
		}
	}
	public static Topic get(String id){
		Connection conn = null;
		Statement state = null;
		Topic t = null;
		try {
			conn = ConnectionUtils.getConnection();
			state = conn.createStatement();
			String sql = "select * from topic where topic_id =  '"+id+"'";
			ResultSet re = state.executeQuery(sql);
			if(re.next()){
				String topic_id = re.getString("topic_id");
				String title = re.getString("title");
				String author_id= re.getString("author_id");
				long last_reply_time = re.getLong("topic_id");
				long publish_time= re.getLong("topic_id");
				String content= re.getString("content");
				t = new Topic(topic_id, title, author_id, last_reply_time, publish_time, content);
			}
		} catch (SQLException e) {
			log.error("",e);
		}finally{
			try {if(state!=null){state.close();}} catch (Exception e2) {}
			try {if(conn!=null){conn.close();}} catch (Exception e2) {}
		}
		return t;
	}*/
}
