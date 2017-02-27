package com.communication.utils;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class JsonUtil<T> {
	
	public static String getCloudResponseJSONString(Object object) {
		
		String output = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			((CloudResponse)object).listToMap();
			output = mapper.writeValueAsString(object);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("getCloudResponseJSONString: " + output);
	    return output;
	}
	
	public T getCloudResponseObject(String jsonString, Class<T> clazz) {
		//System.out.println("getCloudResponseObject: " + jsonString);
		T object = null;
		if(jsonString != null && !"".equals(jsonString)) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				object = mapper.readValue(jsonString, clazz);
				((CloudResponse)object).mapToList();
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return object;
	}
	
	public static String getJSONString(Object object) {
		
		String output = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			output = mapper.writeValueAsString(object);
			//if(object instanceof CloudResponse || object instanceof com.communication.utils.v1.CloudResponse) {
			//	System.out.println("getJSONString: " + output);
			//}
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    return output;
	}
	
	public T getObject(String jsonString, Class<T> clazz) {
		T object = null;
		if(jsonString != null && !"".equals(jsonString)) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				object = mapper.readValue(jsonString, clazz);
				//if(object instanceof CloudResponse || object instanceof com.communication.utils.v1.CloudResponse) {
				//	System.out.println("getObject: " + jsonString);
				//}
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
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
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return object;
	}

}
