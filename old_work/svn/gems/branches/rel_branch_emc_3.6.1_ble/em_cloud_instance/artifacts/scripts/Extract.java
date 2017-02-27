import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;


public class Extract {

    //1. java Extract.java to compile
    //2. javac Extract filepath
    //3. data extracted is dumped at data_extracted.dump
    public static void main (String[] args) {
        System.out.println("File path = " + args[0] );
        ZipInputStream zipOtherIn = null;
		ObjectInputStream objectIn = null;		
		InputStream is = null;
		Map<String, Object> map = null;
		try {
			is = new FileInputStream(args[0]);
			zipOtherIn = new ZipInputStream(is);
			zipOtherIn.getNextEntry();
			objectIn = new ObjectInputStream(zipOtherIn);
			map = (HashMap<String, Object>) objectIn.readObject();
			System.out.println("Update recieved from a local EM server with [macId,Version,Max WAL Id, DbName}: ["
					+ map.get("macId")
					+ ","
					+ map.get("version")
					+ ","
					+ map.get("maxWalLogDataId")
					+ "]");
			
			if(map != null && map.get("data") != null) {
				String fn = "data_extracted.dump";
				PrintWriter out = null;
				out = new PrintWriter(fn);
				for(String s: (List<String>) map.get("data")) {
					out.println(s);
				}
				out.close();
            }
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(is != null){
				try{
					is.close();
				}catch (IOException e) {					
					e.printStackTrace();
				}
			}
			if(zipOtherIn != null){
				try {
					zipOtherIn.close();
				} catch (IOException e) {					
					e.printStackTrace();
				}
			}
			if(objectIn != null){
				try{
					objectIn.close();
				}catch (IOException e) {					
					e.printStackTrace();
				}
			}
		}
    }

}
