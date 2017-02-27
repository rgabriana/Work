package com.ems.cws.services;

import static org.junit.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ems.cws.SensorService;
import com.ems.vo.model.Sensor;


@PrepareForTest
@RunWith(PowerMockRunner.class)
public class SensorServiceTest {
	
	@Test
	public void testPathUrlatClass() throws ClassNotFoundException{		
		Class clazz = SensorService.class;	
		Annotation annotation = clazz.getAnnotation(javax.ws.rs.Path.class);		
		assertEquals("MisMatch in URLS. ", "@javax.ws.rs.Path(value=/org/sensor)", annotation.toString());
	}
	
	@Test
	public void testPathUrlatgetSensorStats() throws ClassNotFoundException, SecurityException, NoSuchMethodException{		
		Method method = SensorService.class.getMethod("getSensorList", String.class, Long.class, String.class);		
		Annotation annotation = method.getAnnotation(javax.ws.rs.Path.class);		
		assertEquals("MisMatch in URLS. ", "@javax.ws.rs.Path(value=list/{property}/{pid}/{limit:.*})", annotation.toString());
	}
	@Test
	public void testPathUrlatgetSensorDetails() throws ClassNotFoundException, SecurityException, NoSuchMethodException{		
		Method method = SensorService.class.getMethod("getSensorDetails", Long.class);		
		Annotation annotation = method.getAnnotation(javax.ws.rs.Path.class);		
		assertEquals("MisMatch in URLS. ", "@javax.ws.rs.Path(value=details/{fid})", annotation.toString());
	}
	@Test
	public void testPathUrlatdimFixture() throws ClassNotFoundException, SecurityException, NoSuchMethodException{		
		Method method = SensorService.class.getMethod("dimFixture", String.class, String.class , String.class,List.class);		
		Annotation annotation = method.getAnnotation(javax.ws.rs.Path.class);		
		assertEquals("MisMatch in URLS. ", "@javax.ws.rs.Path(value=op/dim/{mode}/{percentage}/{time})", annotation.toString());
	}

}
