package com.enlightedportal.mvc.util;

import java.io.UnsupportedEncodingException;

import com.enlightedinc.keyutil.EnlightedKeyGenerator;
import com.enlightedportal.model.LicenseDetails;

public class ControllerUtils {
	
	public static LicenseDetails generateKey(LicenseDetails li)
	{
		
		String key = li.getMacId()+"|"+li.getLevel()+"|"+li.getStartDate()+"|"+li.getEndDate() ;
		EnlightedKeyGenerator ekg = EnlightedKeyGenerator.getInstance() ;
		ekg.setApiKey(key) ;
		
		try {
			
			byte[] salt =li.getMacId().getBytes("UTF-8") ;
			ekg.setSeceretKey(salt) ;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		byte[] encryptedBytes  = ekg.doEncryption() ;
		li.setApiKey(encryptedBytes) ;
		return li ;
	}

}
