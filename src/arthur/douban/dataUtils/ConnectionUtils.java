package arthur.douban.dataUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.fluent.Content;
import org.apache.log4j.Logger;

import arthur.douban.entity.Comment;
import arthur.douban.entity.Group;
import arthur.douban.entity.Topic;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class ConnectionUtils {
	static Logger log = Logger.getLogger(ConnectionUtils.class);
	private static Connection conn;
    private static ComboPooledDataSource ds = new ComboPooledDataSource();
    public static Connection getConnection() {
        try {
            conn = ds.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
    public static void main(String[] args) throws SQLException {
    	Connection connection = getConnection();
    	Statement s = connection.createStatement();
    	ResultSet r = s.executeQuery("select * from topic a where a.flush_reply_num > a.last_reply_num");
    	List l  =new ArrayList();
    	while(r.next()){
    		String string = r.getString(1);
    		l.add(string);
    	}
    	s.close();
    	
    	PreparedStatement p = connection.prepareStatement("delete from comment  where topic_id =?");
    	for (int i = 0; i < l.size(); i++) {
			String t = (String)l.get(i);
			p.setString(1, t);
			p.addBatch();
		}
    	int[] executeBatch = p.executeBatch();
    	for (int i = 0; i < executeBatch.length; i++) {
			System.out.println(executeBatch[i]);
		}
	}

    public static  <T> void  insertEntity(T obj){
    	Connection conn = null;
		Statement state = null;
		PreparedStatement ps = null;
		Class clazz = obj.getClass();
		try {
			 
			conn = ConnectionUtils.getConnection();
			state = conn.createStatement();
			
			Entity annotationEntity = (Entity)clazz.getAnnotation(Entity.class);
			String tableName = annotationEntity.tableName();
			Field field = clazz.getDeclaredField("id");
			arthur.douban.dataUtils.Field anno= field.getAnnotation(arthur.douban.dataUtils.Field.class);
			String fieldName = "id";
			if(anno !=null){
				fieldName =  anno.fiedlName();
			}
			String id = "";
			Method method = clazz.getMethod("getId");
			id = (String)method.invoke(obj);
			if(id != null  && !id.equals("")){
				String sql = "select * from "+tableName+" where  "+fieldName+" = '"+id+"'";
				ResultSet re = state.executeQuery(sql);   // 是不是存在
				if(re.next()){
					updateEntity(obj);
					return;
				}
			}
			
			String insertSql1 = "  insert into "+tableName+" (";
			String insertSql2 = ") values (";
			Field[] declaredFields = clazz.getDeclaredFields();
			
			ArrayList<String> typeNameList = new ArrayList<String>();
			ArrayList<Object> valueList = new ArrayList<Object>();
			for(int i = 0 ; i< declaredFields.length ; i++){  //组织语句
				Field one = declaredFields[i];
				arthur.douban.dataUtils.Field annotation = one.getAnnotation(arthur.douban.dataUtils.Field.class);
				if(annotation!=null){
					boolean ignore = annotation.ignore();
					if(ignore)continue;
				}
				
				String name = one.getName();
		    	name = name.substring(0,1).toUpperCase()+name.substring(1);
		    	Method method2 = clazz.getMethod("get"+name);
		    	Object value = method2.invoke(obj);
		    	if(value == null)continue;
		    	if(value instanceof Long && (Long)value == 0)continue;
		    	if(value instanceof Integer && (Integer)value == 0)continue;
		    	valueList.add(value);
				String typeName = one.getType().getSimpleName();
				typeNameList.add(typeName);
		    	String tableFieldName =null;
		    	String classFieldName =one.getName();
		    	if(annotation !=null){
		    		tableFieldName = annotation.fiedlName();
		    	}else{
		    		tableFieldName = classFieldName;
		    	}
		    	insertSql1 = insertSql1+tableFieldName+",";
		    	insertSql2 = insertSql2+" ?,";
			}
			insertSql1 = insertSql1.substring(0, insertSql1.length()-1);
			insertSql2 = insertSql2.substring(0, insertSql2.length()-1);
			String insertSql =  insertSql1 + insertSql2 +")";
			log.info("excute sql :"+insertSql);
			ps = conn.prepareStatement(insertSql);
			for(int j = 0 ; j< valueList.size(); j++){
		    	Object value = valueList.get(j);
				String typeName = typeNameList.get(j);
				switch (typeName) {
					case "String":
						String valueStr =  value.toString();
						ps.setString(j+1, valueStr);
						break; 
					case "long":
						ps.setLong(j+1, (Long)value);
						break;
					case "int":
						ps.setInt(j+1, (int)value);
						break;
					default:
						throw new RuntimeException("未能识别的数据类型");
				}
			}
			int executeUpdate = ps.executeUpdate();
			if(executeUpdate !=1){
				log.error("insert fail");
			}
		} catch (Exception e) {
			log.error("",e);
		}finally{
			try {if(state!=null){state.close();}} catch (Exception e2) {}
			try {if(conn!=null){conn.close();}} catch (Exception e2) {}
		}
    }
    public static  <A> int  batchInsert(List<A> list){
    	Connection conn = null;
    	int  executedNum = 0;
		try {
			conn = ConnectionUtils.getConnection();
			executedNum = batchInsert(list, conn);
		} catch (Exception e) {
			log.error("",e);
		}finally{
			try {if(conn!=null){conn.close();}} catch (Exception e2) {}
		}
		return executedNum;
    }
    public static <A> int batchInsert(List<A> list,Connection conn) throws Exception{
    	PreparedStatement ps = null;
		Object obj = list.get(0);
		Class clazz = obj.getClass();
		int  execute= 0;
		try {
			Entity annotationEntity = (Entity)clazz.getAnnotation(Entity.class);
			String tableName = annotationEntity.tableName();
			Field field = clazz.getDeclaredField("id");
			arthur.douban.dataUtils.Field anno= field.getAnnotation(arthur.douban.dataUtils.Field.class);
			String fieldName = "id";
			if(anno !=null){
				fieldName =  anno.fiedlName();
			}
			String id = "";
			Method method = clazz.getMethod("getId");
			id = (String)method.invoke(obj);
			
			String insertSql1 = "  insert into "+tableName+" (";
			String insertSql2 = ") values (";
			Field[] declaredFields = clazz.getDeclaredFields();
			
			ArrayList<Method> methodList = new ArrayList<Method>();
			ArrayList<String> typeNameList = new ArrayList<String>();
			for(int i = 0 ; i< declaredFields.length ; i++){  //组织语句
				Field one = declaredFields[i];
				arthur.douban.dataUtils.Field annotation = one.getAnnotation(arthur.douban.dataUtils.Field.class);
				if(annotation!=null){
					boolean ignore = annotation.ignore();
					if(ignore)continue;
				}
				
				String name = one.getName();
		    	name = name.substring(0,1).toUpperCase()+name.substring(1);
		    	Method method2 = clazz.getMethod("get"+name);
				String typeName = one.getType().getSimpleName();
				methodList.add(method2);
				typeNameList.add(typeName);
				
		    	String tableFieldName =null;
		    	String classFieldName =one.getName();
		    	if(annotation !=null){
		    		tableFieldName = annotation.fiedlName();
		    	}else{
		    		tableFieldName = classFieldName;
		    	}
		    	insertSql1 = insertSql1+tableFieldName+",";
		    	insertSql2 = insertSql2+" ?,";
			}
			insertSql1 = insertSql1.substring(0, insertSql1.length()-1);
			insertSql2 = insertSql2.substring(0, insertSql2.length()-1);
			String insertSql =  insertSql1 + insertSql2 +")";
			log.info("excute sql :"+insertSql);
			ps = conn.prepareStatement(insertSql);
			for(int i =0 ; i <list.size(); i++){  //遍历数据。
				A a = list.get(i);
				for(int j = 0 ; j< methodList.size(); j++){
			    	Method method2 = methodList.get(j);
			    	Object value = method2.invoke(a);
					String typeName = typeNameList.get(j);
					switch (typeName) {
						case "String":
							String valueStr =  value.toString();
							ps.setString(j+1, valueStr);
							break; 
						case "long":
							ps.setLong(j+1, (Long)value);
							break;
						case "int":
							ps.setInt(j+1, (int)value);
							break;
						default:
							throw new RuntimeException("未能识别的数据类型");
					}
				}
				ps.addBatch();
			}
			
			int[] batch= ps.executeBatch();
			
			for(int i : batch){
				if(i == Statement.SUCCESS_NO_INFO){
					execute++;
				}
				if(i>=0){
					execute+=i;
				}
			}
			log.info("executeBatch  , listSize:"+list.size() +", success:"+ execute);
		} catch (Exception e) {
			log.error("",e);
			throw e;
		}finally{
			try {if(ps!=null){ps.close();}} catch (Exception e2) {}
		}
		return execute;
    }
    /**
     * 泛型确定要返回的entity类型，  注解标志entity的表名和字段别名
     * 反射执行。
     * @param id
     * @param itemClazz
     * @return
     */
    public static <A>  A  getEntity(String id ,Class<A> itemClazz){
		Connection conn = null;
		Statement state = null;
		A t = null;
		try {
			conn = ConnectionUtils.getConnection();
			state = conn.createStatement();
			
			
			Entity annotationEntity = (Entity)itemClazz.getAnnotation(Entity.class);
			String tableName = annotationEntity.tableName();
			Field field = itemClazz.getDeclaredField("id");
			arthur.douban.dataUtils.Field annotation = field.getAnnotation(arthur.douban.dataUtils.Field.class);
			String fiedlName = annotation.fiedlName();
			
			
			String sql = "select * from "+tableName+" where  "+fiedlName+" = '"+id+"'";
			
			ResultSet re = state.executeQuery(sql);
			
			if(re.next()){
				t = itemClazz.newInstance();
				Field[] fields = itemClazz.getDeclaredFields();
				for(int i = 0 ; i< fields.length ; i++){
					Field one = fields[i];
					arthur.douban.dataUtils.Field annF = one.getAnnotation(arthur.douban.dataUtils.Field.class);
					if(annF!=null){
						boolean ignore = annF.ignore();
						if(ignore)continue;
					}
					Object value = oneFiledValueGet(one, re);
					String name = one.getName();
					name = name.substring(0,1).toUpperCase()+name.substring(1);
					Method method = itemClazz.getMethod("set"+name,one.getType());
					method.invoke(t, value);
				}
			}
		} catch (Exception e) {
			log.error("",e);
		}finally{
			try {if(state!=null){state.close();}} catch (Exception e2) {}
			try {if(conn!=null){conn.close();}} catch (Exception e2) {}
		}
		return t;
	}
    public static <A>  List<A>  getEntities(Class<A> itemClazz){
		Connection conn = null;
		Statement state = null;
		A t = null;
		ArrayList<A> resultList = new ArrayList<A>();
		try {
			conn = ConnectionUtils.getConnection();
			state = conn.createStatement();
			Entity annotationEntity = (Entity)itemClazz.getAnnotation(Entity.class);
			String tableName = annotationEntity.tableName();
			String sql = "select * from "+tableName+"";
			log.info("excute sql :"+sql);
			ResultSet re = state.executeQuery(sql);
			Field[] fields = itemClazz.getDeclaredFields();
			while(re.next()){
				t = itemClazz.newInstance();
				for(int i = 0 ; i< fields.length ; i++){
					Field one = fields[i];
					arthur.douban.dataUtils.Field annotation = one.getAnnotation(arthur.douban.dataUtils.Field.class);
					if(annotation!=null){
						boolean ignore = annotation.ignore();
						if(ignore)continue;
					}
					Object value = oneFiledValueGet(one, re);
					String name = one.getName();
					name = name.substring(0,1).toUpperCase()+name.substring(1);
					Method method = itemClazz.getMethod("set"+name,one.getType());
					method.invoke(t, value);
				}
				resultList.add(t);
			}
		} catch (Exception e) {
			log.error("",e);
		}finally{
			try {if(state!=null){state.close();}} catch (Exception e2) {}
			try {if(conn!=null){conn.close();}} catch (Exception e2) {}
		}
		return resultList;
	}
	 public static <A>  List<A>  getEntitiesCondition(Class<A> itemClazz,String where ,String order){
		Connection conn = null;
		Statement state = null;
		List<A> list = new ArrayList<A>();
		try {
			conn = ConnectionUtils.getConnection();
			state = conn.createStatement();
			
			
			Entity annotationEntity = (Entity)itemClazz.getAnnotation(Entity.class);
			String tableName = annotationEntity.tableName();
			Field field = itemClazz.getDeclaredField("id");
			arthur.douban.dataUtils.Field annotation = field.getAnnotation(arthur.douban.dataUtils.Field.class);
			String fiedlName = annotation.fiedlName();
			
			
			String sql = "select * from "+tableName+" where  "+where +"  order by  "+ order;
			log.info(" queryList "+sql);
			ResultSet re = state.executeQuery(sql);
			
			while(re.next()){
				A t = itemClazz.newInstance();
				Field[] fields = itemClazz.getDeclaredFields();
				for(int i = 0 ; i< fields.length ; i++){
					Field one = fields[i];
					arthur.douban.dataUtils.Field annotation1 = one.getAnnotation(arthur.douban.dataUtils.Field.class);
					if(annotation1!=null){
						boolean ignore = annotation1.ignore();
						if(ignore)continue;
					}
					Object value = oneFiledValueGet(one, re);
					String name = one.getName();
					name = name.substring(0,1).toUpperCase()+name.substring(1);
					Method method = itemClazz.getMethod("set"+name,one.getType());
					method.invoke(t, value);
				}
				list.add(t);
			}
		} catch (Exception e) {
			log.error("",e);
		}finally{
			try {if(state!=null){state.close();}} catch (Exception e2) {}
			try {if(conn!=null){conn.close();}} catch (Exception e2) {}
		}
		return list;
	}
    /**
     * 获得一次查询，一个field的值
     * @param f
     * @param re
     * @return
     * @throws SQLException
     */
    private static  Object oneFiledValueGet(Field f , ResultSet re) throws SQLException{
    	arthur.douban.dataUtils.Field annotation = f.getAnnotation(arthur.douban.dataUtils.Field.class);
    	String tableFieldName =null;
    	String classFieldName =f.getName();
    	if(annotation !=null){
    		tableFieldName = annotation.fiedlName();
    	}else{
    		tableFieldName = classFieldName;
    	}
    	String typeName = f.getType().getSimpleName();
    	switch (typeName) {
			case "String":
				String string = re.getString(tableFieldName);
				return string;
			case "long":
				long l = re.getLong(tableFieldName);
				return l;
			case "int":
				int i = re.getInt(tableFieldName);
				return i;
			default:
				throw new RuntimeException("未能识别的数据类型");
		}
    }
    
    public static  <T> void  updateEntity(T obj){
    	Connection conn = null;
		try {
			conn = ConnectionUtils.getConnection();
			updateEntity(obj, conn);
		} catch (Exception e) {
			log.error("",e);
		}finally{
			try {if(conn!=null){conn.close();}} catch (Exception e2) {}
		}
    }
    //不关闭 链接的处理。
    public static  <T> void  updateEntity(T obj,Connection conn){
		PreparedStatement ps = null;
		Class clazz = obj.getClass();
		try {
			Entity annotationEntity = (Entity)clazz.getAnnotation(Entity.class);
			String tableName = annotationEntity.tableName();
			Field field = clazz.getDeclaredField("id");
			arthur.douban.dataUtils.Field anno= field.getAnnotation(arthur.douban.dataUtils.Field.class);
			String fieldName = "id";
			if(anno !=null){
				fieldName =  anno.fiedlName();
			}
			String id = "";
			Method method = clazz.getMethod("getId");
			id = (String)method.invoke(obj);
			if(id== null || id.equals("")){
				log.error("error id 为空");
				return;
			}
			String updateSql = "  update "+tableName+"  set ";
			
			Field[] declaredFields = clazz.getDeclaredFields();
			
			List<Object> vl = new ArrayList<Object>();
			List<String> tl = new ArrayList<>();
			for(int i = 0 ; i< declaredFields.length ; i++){
				Field one = declaredFields[i];
				arthur.douban.dataUtils.Field annotation = one.getAnnotation(arthur.douban.dataUtils.Field.class);
				if(annotation!=null){
					boolean ignore = annotation.ignore();
					if(ignore)continue;
				}
		    	String tableFieldName =null;
		    	String classFieldName =one.getName();
		    	String name = one.getName();
		    	if(name.equals("id"))continue;
		    	name = name.substring(0,1).toUpperCase()+name.substring(1);
		    	Method method2 = clazz.getMethod("get"+name);
		    	Object value = method2.invoke(obj);
		    	if(annotation !=null){
		    		tableFieldName = annotation.fiedlName();
		    	}else{
		    		tableFieldName = classFieldName;
		    	}
		    	String typeName = one.getType().getSimpleName();
		    	if(value == null)continue;
		    	if(value instanceof Long && (Long)value == 0)continue;
		    	if(value instanceof Integer && (Integer)value == 0)continue;
		    	updateSql = updateSql +tableFieldName+"=? ,";
		    	vl.add(value);
		    	tl.add(typeName);
			}
			updateSql  = updateSql.substring(0,updateSql.length()-1);
			updateSql = updateSql+" where  "+fieldName +" = ?";
			ps = conn.prepareStatement(updateSql);
			
			for(int i = 0 ; i<tl.size(); i++){
				Object value = vl.get(i);
				String typeName = tl.get(i);
				switch (typeName) {
					case "String":
						ps.setString(i+1, value.toString());
						break;
					case "long":
						ps.setLong(i+1, (Long)value);
						break;
					case "int":
						ps.setInt(i+1, (int)value);
						break;
					default:
						throw new RuntimeException("未能识别的数据类型");
		    	}
			}
			ps.setString(tl.size()+1, id);
			log.info("excute sql :"+updateSql);
			int executeUpdate = ps.executeUpdate();
			if(executeUpdate !=1){
				log.error("update fail");
			}
		} catch (Exception e) {
			log.error("",e);
		}finally{
			try {if(ps!=null){ps.close();}} catch (Exception e2) {}
		}
    }
    public static void updateFlushTopic(String topicId, long flush_time,int commentSum,Connection conn) throws Exception{
    	Statement state =null; 
    	try {
    		state = conn.createStatement();
    		String sql = "update topic  set  flush_time="+flush_time+",flush_reply_num=flush_reply_num+"+commentSum+"    where topic_id = "+topicId;
    		log.info("update  sql:"+sql);
    		int executeUpdate = state.executeUpdate(sql);
    		log.info("update  sql:"+sql+",result:"+executeUpdate);
    		if(executeUpdate!=1){
    			log.error("update sql fail ,sql :"+sql);
    		}
		} catch (Exception e) {
			log.equals(e);
			throw e;
		}finally{
			if(state!=null)state.close();
		}
    }
	public static void updateFlushTopic(String topicId,int commentSum,Connection conn) throws Exception{
		Statement state =null; 
    	try {
    		state = conn.createStatement();
    		String sql = "update topic  set  flush_reply_num=flush_reply_num+"+commentSum+"    where topic_id = "+topicId;
    		log.info("update  sql:"+sql);
    		int executeUpdate = state.executeUpdate(sql);
    		log.info("update  sql:"+sql+",result:"+executeUpdate);
    		if(executeUpdate!=1){
    			log.error("update sql fail ,sql :"+sql);
    		}
		} catch (Exception e) {
			log.equals(e);
			throw e;
		}finally{
			if(state!=null)state.close();
		}
	}
	
    public static  <T> void  deleteEntity(T obj){
    	Connection conn = null;
		Statement state = null;
		Class clazz = obj.getClass();
		try {
			conn = ConnectionUtils.getConnection();
			state = conn.createStatement();
			
			Entity annotationEntity = (Entity)clazz.getAnnotation(Entity.class);
			String tableName = annotationEntity.tableName();
			Field field = clazz.getDeclaredField("id");
			arthur.douban.dataUtils.Field anno= field.getAnnotation(arthur.douban.dataUtils.Field.class);
			String fieldName = "id";
			if(anno !=null){
				fieldName =  anno.fiedlName();
			}
			String id = "";
			Method method = clazz.getMethod("getId");
			id = (String)method.invoke(obj);
			if(id== null || id.equals("")){
				log.error("error id 为空");
				return;
			}
			String updateSql = "  delete from "+tableName+"   ";
			updateSql  = updateSql.substring(0,updateSql.length()-1);
			updateSql = updateSql+" where "+fieldName +" = '"+id+"'";
			log.info("excute sql :"+updateSql);
			int executeUpdate = state.executeUpdate(updateSql);
			if(executeUpdate !=1){
				log.error("delete fail");
			}
		} catch (Exception e) {
			log.error("",e);
		}finally{
			try {if(state!=null){state.close();}} catch (Exception e2) {}
			try {if(conn!=null){conn.close();}} catch (Exception e2) {}
		}
    }
    
}