package com.communicator.json;

import java.io.File;
import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;



@Service("jsonMapper")
public class JsonMapper {
	
	public void mapObjectsToJson(File  file, Object dataObj){
		
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			mapper.writeValue(file, dataObj);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
