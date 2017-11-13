package arthur.xiaomi;

import java.io.IOException;


public class Server {
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		FileSystemClassLoader f = new FileSystemClassLoader("");
		Class<?> findClass = f.findClass("arthur.xiaomi.Filter");
		System.out.println(findClass.getSimpleName());
	}
}
