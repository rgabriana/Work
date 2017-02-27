package com.communication.utils;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

public class Util {

	public static final Logger logger = Logger.getLogger(Util.class.getName());
	
	public static String checkSumApacheCommons(String file){
        String checksum = null;
        try {  
            checksum = DigestUtils.md5Hex(new FileInputStream(file));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return checksum;
    }
	
}
