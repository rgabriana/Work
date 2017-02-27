package com.ems.util;

import java.io.File;
import java.io.StringReader;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;

public class ImportExportFileUtil {

	final static private Logger log = Logger.getLogger(ImportExportFileUtil.class);
	public static boolean validateXMLSchema(String xsdPath, String xmlString){
        
        try {
            SchemaFactory factory = 
                    SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            Schema schema = factory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlString)));
        } catch (Exception e) {
        	log.error("Error Occured:",e);
            return false;
        }
        return true;
    }
	
}
