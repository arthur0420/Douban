package arthur.xiaomi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class StandardInputOutput {  
    public static void main(String args[]) {  
         try {
             System.out.println("please Input:");
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
             String line = null;
             while (!"exit".equals(line = br.readLine())) {
                 System.out.print(line);
                 if(line == null)
                	 Thread.sleep(1000);
             }
         } catch (Exception e) {
             System.out.println(e.toString());
         }
    }  
}
