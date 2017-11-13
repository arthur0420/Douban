package arthur.xiaomi;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileSystemClassLoader extends ClassLoader { 
	 
	   private String rootDir; 
	 
	   public FileSystemClassLoader(String rootDir) { 
	       this.rootDir = rootDir; 
	   } 
	   
	   protected Class<?> findClass(String name) throws ClassNotFoundException { 
		   	byte[] byteArray = new byte[1024];
		   	int read  = 0;
	        try {
	        	FileInputStream fos = new FileInputStream("d://filter");
	        	read = fos.read(byteArray);
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        Class<?> defineClass = defineClass(name, byteArray, 0,read);
	        
	        return defineClass; 
	       
	   } 
	}