package arthur.douban.dataUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

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
    	Topic t = new Topic("123", "测试2", "123", 123321, 123333, "测试法阿斯蒂芬");
    	updateEntity(t);
	}
    
    public static  <T> void  insertEntity(T obj){
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
			
			String sql = "select * from "+tableName+" where  "+fieldName+" = '"+id+"'";
			ResultSet re = state.executeQuery(sql);   // 是不是存在
			if(re.next()){
				System.out.println("to update");
				updateEntity(obj);
				return;
			}
			String insertSql1 = "  insert into "+tableName+" (";
			String insertSql2 = ") values (";
			Field[] declaredFields = clazz.getDeclaredFields();
			for(int i = 0 ; i< declaredFields.length ; i++){
				Field one = declaredFields[i];
				arthur.douban.dataUtils.Field annotation = one.getAnnotation(arthur.douban.dataUtils.Field.class);
		    	String tableFieldName =null;
		    	String classFieldName =one.getName();
		    	String name = one.getName();
		    	name = name.substring(0,1).toUpperCase()+name.substring(1);
		    	Method method2 = clazz.getMethod("get"+name);
		    	Object value = method2.invoke(obj);
		    	if(annotation !=null){
		    		tableFieldName = annotation.fiedlName();
		    	}else{
		    		tableFieldName = classFieldName;
		    	}
		    	String typeName = one.getType().getSimpleName();
		    	insertSql1 = insertSql1+tableFieldName+",";
		    	switch (typeName) {
					case "String":
						String valueStr =  value.toString();
						if(valueStr.indexOf("'")!=-1){
							valueStr = valueStr.replaceAll("'", "‘"); 
						}
						insertSql2 = insertSql2+" '"+valueStr+"',";
						break; 
					case "long":
						insertSql2 = insertSql2+""+value+",";
						break;
					default:
						throw new RuntimeException("未能识别的数据类型");
				}
			}
			insertSql1 = insertSql1.substring(0, insertSql1.length()-1);
			insertSql2 = insertSql2.substring(0, insertSql2.length()-1);
			String insertSql =  insertSql1 + insertSql2 +")";
			int executeUpdate = state.executeUpdate(insertSql);
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
			ResultSet re = state.executeQuery(sql);
			Field[] fields = itemClazz.getDeclaredFields();
			while(re.next()){
				t = itemClazz.newInstance();
				for(int i = 0 ; i< fields.length ; i++){
					Field one = fields[i];
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
    /**
     * 获得一次查询，一个field的值
     * @param f
     * @param re
     * @return
     * @throws SQLException
     */
    private static <A> Object oneFiledValueGet(Field f , ResultSet re) throws SQLException{
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
			default:
				throw new RuntimeException("未能识别的数据类型");
		}
    }
    public static  <T> void  updateEntity(T obj){
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
			String updateSql = "  update "+tableName+" set  ";
			
			Field[] declaredFields = clazz.getDeclaredFields();
			for(int i = 0 ; i< declaredFields.length ; i++){
				Field one = declaredFields[i];
				arthur.douban.dataUtils.Field annotation = one.getAnnotation(arthur.douban.dataUtils.Field.class);
		    	String tableFieldName =null;
		    	String classFieldName =one.getName();
		    	String name = one.getName();
		    	name = name.substring(0,1).toUpperCase()+name.substring(1);
		    	Method method2 = clazz.getMethod("get"+name);
		    	
		    	Object value = method2.invoke(obj);
		    	if(value == null || "".equals(value))continue;
		    	if(annotation !=null){
		    		tableFieldName = annotation.fiedlName();
		    	}else{
		    		tableFieldName = classFieldName;
		    	}
		    	String typeName = one.getType().getSimpleName();
		    	
		    	switch (typeName) {
					case "String":
						updateSql = updateSql +tableFieldName+"='"+value+"',";
						break; 
					case "long":
						updateSql = updateSql +tableFieldName+"="+value+" ,";
						break;
					default:
						throw new RuntimeException("未能识别的数据类型");
				}
			}
			updateSql  = updateSql.substring(0,updateSql.length()-1);
			updateSql = updateSql+" where "+fieldName +" = '"+id+"'";
//			System.out.println(updateSql);
			int executeUpdate = state.executeUpdate(updateSql);
			if(executeUpdate !=1){
				log.error("update fail");
			}
		} catch (Exception e) {
			log.error("",e);
		}finally{
			try {if(state!=null){state.close();}} catch (Exception e2) {}
			try {if(conn!=null){conn.close();}} catch (Exception e2) {}
		}
    }
}