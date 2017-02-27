package com.ems.security.util;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import com.ems.server.ServerMain;



public final class PasswordUtils {

	public  static String[] extractPassword() throws FileNotFoundException,IOException{
		
		FileInputStream fInStream = new FileInputStream(ServerMain.getInstance().getAdminPassFile());
		DataInputStream in = new DataInputStream(fInStream);				
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String output = br.readLine();
		int indexOfColon = 0;
		String saltedhash = null;
		String salt = null;
		
		if(output.trim().indexOf(";")!=-1){
			indexOfColon = output.trim().indexOf(";");
			saltedhash = output.trim().substring(0, indexOfColon);
			salt = output.trim().substring(indexOfColon+1);
		}
		else{
			saltedhash = output.trim();
			
		}
		

		String[] hashAndSalt = {saltedhash,salt};
		
		in.close();
		
		return hashAndSalt;
			
		
	}
	
	public static String generateSalt() throws IOException{
		
		Runtime rt = Runtime.getRuntime();
		String[] commands = {"openssl","rand","-hex","16"};
		
		Process proc = rt.exec(commands);
		InputStream stdin = proc.getInputStream();
		InputStreamReader isr = new InputStreamReader(stdin);
		BufferedReader br = new BufferedReader(isr);
		String line = br.readLine();
					
		return line;
		
		
		
	}
	
	public static String generateDigest(String password) throws FileNotFoundException, IOException{
		
		String salt = extractPassword()[1];
		String passwordDigest = null;
		
		if(salt!=null){
			PasswordEncoder shaPasswordEncoder = new ShaPasswordEncoder();
			passwordDigest = shaPasswordEncoder.encodePassword(password, salt);
		}
		else{
			PasswordEncoder md5PasswordEncoder = new Md5PasswordEncoder();
			passwordDigest = md5PasswordEncoder.encodePassword(password, salt);
		}
		
		return passwordDigest;
	}
	
	public static void updatePassword(String newPassword) throws FileNotFoundException, IOException{
		Runtime rt = Runtime.getRuntime();
		Process proc;
		String oldEncodedPassword = extractPassword()[0];
		String newsalt = generateSalt();
		PasswordEncoder shaPasswordEncoder = new ShaPasswordEncoder();
		String newEncodedPassword = shaPasswordEncoder.encodePassword(newPassword, newsalt);
		proc = rt.exec(new String[]{"authadmin.sh", "change", oldEncodedPassword, newEncodedPassword, newsalt});
		BufferedReader outputStream = new BufferedReader(
				new InputStreamReader(proc.getInputStream()));
		String output = outputStream.readLine();
						
		
	}
	
	
	
}
