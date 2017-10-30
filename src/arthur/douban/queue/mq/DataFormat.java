package arthur.douban.queue.mq;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import arthur.douban.entity.Group;
import arthur.douban.event.GroupEvent;

public class DataFormat {
	public static   byte[] getByteArray(Object t)throws Exception{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(t);
        out.close();
        byte[] byteArray = baos.toByteArray();
        return byteArray;
	}
	public static Object getObjectByByteArray(byte[] m) throws Exception{
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(m));
		Object readObject = ois.readObject();
		return readObject;
	}
	
	public static void main(String[] args) throws Exception {
		GroupEvent groupEvent = new GroupEvent(0, new Group("1", "shenzhen", "https://baidu.com", 0l), 0);
		byte[] byteArray =getByteArray(groupEvent);
		System.out.println(byteArray.length);
		
		Object objectByByteArray = getObjectByByteArray(byteArray);
		System.out.println(objectByByteArray);
		
	}
}
