package com.ems.ws;


import java.io.IOException;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class JacksonObjectMapper {

	protected static final Logger logger = Logger
			.getLogger(JacksonObjectMapper.class);
   public static void main(String[] args) throws IOException {
   }

   public static Object getObjFromJson(final String json, final Class objcls){
	   try {
		ObjectMapper mapper = new ObjectMapper();
		   return mapper.readValue(json, objcls);
	   } catch (Exception e) {
			logger.error("****FAILED*****",e);
		}
		   return null;
   }
   public static String getJsonFromObj(final Object obj){
	   try {
		StringWriter stringEmp = new StringWriter();
		   ObjectMapper mapper = new ObjectMapper();
		   mapper.writeValue(stringEmp, obj);
		   return stringEmp.toString();
	} catch (Exception e) {
		logger.error("****FAILED*****",e);
	}
	   return null;
   }

}
