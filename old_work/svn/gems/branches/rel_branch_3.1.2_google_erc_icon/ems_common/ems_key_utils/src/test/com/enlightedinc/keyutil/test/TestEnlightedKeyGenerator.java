package com.enlightedinc.keyutil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


import com.enlightedinc.keyutil.EnlightedKeyGenerator;

public class TestEnlightedKeyGenerator {
	
	public static void main(String args[])
	{
		String key = "08:00:27:ad:8f:47|Platinum|12/12/2012|12/12/2020" ;
		EnlightedKeyGenerator ekg = EnlightedKeyGenerator.getInstance() ;
		ekg.setApiKey(key) ;
		System.out.println("Before encrpytion Orignal :- " +key);
		try {
				byte[] salt ="08:00:27:ad:8f:47".getBytes("UTF-8") ;
			ekg.setSeceretKey(salt) ;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] encryptedString  = ekg.doEncryption() ;
		writeToFile("restApi.enlighted" , encryptedString) ;
		System.out.println("Encrypted :- " + encryptedString);
		File f = new File("restApi.enlighted") ;
		byte[] filecontent = readFromFile(f.getAbsolutePath()) ;
		ekg.setEncrptedApiKey(filecontent) ;
		String decryptedString = ekg.doDecryption() ;
		System.out.println("After encrpytion Orignal :- " +decryptedString);
		
	}
	public static void writeToFile(String fileName , byte[] data)
	{
		File f = new File(fileName)  ;
		if(!f.exists())
		{
			try {
				f.createNewFile() ;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(data!=null)
		{
		try {
			FileOutputStream fos = new FileOutputStream(f.getAbsolutePath());
			fos.write(data) ;
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
		
	}
	public static byte[] readFromFile(String filePath)
	{
		byte[] fileContent = null ;
		File file = new File(filePath) ;
		try 
		{
			FileInputStream fin = new FileInputStream(file);
			fileContent = new byte[(int)file.length()];
			fin.read(fileContent) ;
			return fileContent ;
			
		}catch(FileNotFoundException e)
	    {
		      System.out.println("File not found" + e);
		    }
		    catch(IOException ioe)
		    {
		      System.out.println("Exception while reading the file " + ioe);
		    }
		return fileContent ;
	}

}
