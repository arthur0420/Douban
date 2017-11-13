package arthur.xiaomi;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;


public class ServerC {
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		Class c =  Filter.class;
		InputStream resourceAsStream = c.getClassLoader().getResourceAsStream("arthur/xiaomi/Filter.class");
		System.out.println(resourceAsStream);
		
		byte[] a = new byte[1024];
        int read = resourceAsStream.read(a);
        System.out.println(read);
        FileOutputStream fos = new FileOutputStream("d://filter");
        fos.write(a, 0, read);;
        fos.close();
	}
}
