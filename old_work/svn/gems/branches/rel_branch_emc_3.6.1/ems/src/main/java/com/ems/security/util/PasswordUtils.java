package com.ems.security.util;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import com.ems.server.ServerMain;
import com.ems.util.Constants;



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
		proc = rt.exec(new String[]{"authadmin.sh", "change", "admin", oldEncodedPassword, newEncodedPassword, newsalt});
		BufferedReader outputStream = new BufferedReader(
				new InputStreamReader(proc.getInputStream()));
		String output = outputStream.readLine();
						
		
	}
	
	public  static String[] extractAdminTypeUserPassword(String userName) throws FileNotFoundException,IOException{
		
		File adminUserCredentialsFile = new File(ServerMain.getInstance().getAdminUserCredentials());
		if(!adminUserCredentialsFile.exists()) {
			adminUserCredentialsFile.createNewFile();
		}
		
		FileInputStream fInStream = new FileInputStream(ServerMain.getInstance().getAdminUserCredentials());
		String saltedhash = null;
		String salt = null;
		String output = null;
		Scanner scanner = new Scanner(fInStream);
		while (scanner.hasNextLine()) {
			output = scanner.nextLine();
			int indexOfEqual = 0;
			int indexOfColon = 0;
			String adminUserName;
			if(output.trim().indexOf("=")!=-1){
				indexOfEqual = output.trim().indexOf("=");
				adminUserName = output.trim().substring(0, indexOfEqual);
				if(userName.equalsIgnoreCase(adminUserName)){
					if(output.trim().indexOf(";")!=-1){
						indexOfColon = output.trim().indexOf(";");
						saltedhash = output.trim().substring(indexOfEqual+1, indexOfColon);
						salt = output.trim().substring(indexOfColon+1);
					}
					else{
						saltedhash = output.trim();
					}
					break;
				}
			}
		}
		String[] hashAndSalt = {saltedhash,salt};
		scanner.close();
		return hashAndSalt;
	}
	
	public static String generateAdminTypeUserDigest(String password,String userName) throws FileNotFoundException, IOException{
		
		String salt = extractAdminTypeUserPassword(userName)[1];
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
	
	public static void updateAdminTypeUserPassword(String userName,String newPassword) throws FileNotFoundException, IOException{
		Runtime rt = Runtime.getRuntime();
		Process proc = null;
		String oldEncodedPassword = extractAdminTypeUserPassword(userName)[0];
		String newsalt = generateSalt();
		PasswordEncoder shaPasswordEncoder = new ShaPasswordEncoder();
		String newEncodedPassword = shaPasswordEncoder.encodePassword(newPassword, newsalt);
		if(oldEncodedPassword != null){
			proc = rt.exec(new String[]{"authadmin.sh", "change", userName,oldEncodedPassword, newEncodedPassword, newsalt});
		}else{ // if the entry is not found in the adminusercredentials file,the user will be added to it.
			proc = rt.exec(new String[]{"authadmin.sh", "new", userName,newEncodedPassword, newEncodedPassword,newsalt});
		}
		BufferedReader outputStream = new BufferedReader(
				new InputStreamReader(proc.getInputStream()));
		String output = outputStream.readLine();
	}
	
	public static void addAdminTypeUser(String userName,String newPassword) throws FileNotFoundException, IOException{
		Runtime rt = Runtime.getRuntime();
		Process proc;
		String newsalt = generateSalt();
		PasswordEncoder shaPasswordEncoder = new ShaPasswordEncoder();
		String newEncodedPassword = shaPasswordEncoder.encodePassword(newPassword, newsalt);
		proc = rt.exec(new String[]{"authadmin.sh", "new", userName,newEncodedPassword, newEncodedPassword,newsalt});
		BufferedReader outputStream = new BufferedReader(
				new InputStreamReader(proc.getInputStream()));
		String output = outputStream.readLine();
	}
	
	public static void deleteAdminTypeUser(String userName) throws FileNotFoundException, IOException{
		Runtime rt = Runtime.getRuntime();
		Process proc;
		proc = rt.exec(new String[]{"authadmin.sh", "delete", userName});
		BufferedReader outputStream = new BufferedReader(
				new InputStreamReader(proc.getInputStream()));
		String output = outputStream.readLine();
	}
	
	public static boolean isPasswordUsedBefore(final String newPass, final String[] oldPassStrArray){
		boolean flag = false;
		for (final String oldPass : oldPassStrArray){
			if(newPass.equals(oldPass)){
				return true;
			}
		}
		return flag;
	} 
}
