package com.communicator.uem;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class JsonUtil<T> {
	
	static Logger logger = Logger.getLogger(JsonUtil.class.getName());
	
	public static String getUemResponseJSONString(Object object) {
		
		String output = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			((UemResponse)object).listToMap();
			output = mapper.writeValueAsString(object);
		} catch (JsonGenerationException e) {
			logger.error(e.getMessage(),e);
		} catch (JsonMappingException e) {
			logger.error(e.getMessage(),e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
	    return output;
	}
	
	public T getUemResponseObject(String jsonString, Class<T> clazz) {
		T object = null;
		if(jsonString != null && !"".equals(jsonString)) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				object = mapper.readValue(jsonString, clazz);
				((UemResponse)object).mapToList();
			} catch (JsonParseException e) {
				logger.error(e.getMessage(),e);
			} catch (JsonMappingException e) {
				logger.error(e.getMessage(),e);
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
			}
		}
		return object;
	}
	
	public static String getJSONString(Object object) {
		
		String output = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			output = mapper.writeValueAsString(object);
		} catch (JsonGenerationException e) {
			logger.error(e.getMessage(),e);
		} catch (JsonMappingException e) {
			logger.error(e.getMessage(),e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
	    return output;
	}
	
	public T getObject(String jsonString, Class<T> clazz) {
		T object = null;
		if(jsonString != null && !"".equals(jsonString)) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				object = mapper.readValue(jsonString, clazz);
			} catch (JsonParseException e) {
				logger.error(e.getMessage(),e);
			} catch (JsonMappingException e) {
				logger.error(e.getMessage(),e);
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
			}
		}
		return object;
	}
	
	public T getObject(String jsonString, TypeReference<T> typeReference) {
		T object = null;
		if(jsonString != null && !"".equals(jsonString)) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				object = mapper.readValue(jsonString, typeReference);
			} catch (JsonParseException e) {
				logger.error(e.getMessage(),e);
			} catch (JsonMappingException e) {
				logger.error(e.getMessage(),e);
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
			}
		}
		return object;
	}

}
