/**
 * 
 */
package com.ems.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.annotations.Synchronize;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.SystemConfigurationDao;
import com.ems.model.SystemConfiguration;
import com.ems.model.Title24;
import com.ems.model.User;
import com.ems.types.AuthenticationType;
import com.ems.util.Constants;

/**
 * @author yogesh
 * 
 */
@Service("systemConfigurationManager")
@Transactional(propagation = Propagation.REQUIRED)
public class SystemConfigurationManager<T> {

	private static Logger syslog = Logger.getLogger("SysLog");
    @Resource
    private SystemConfigurationDao systemConfigurationDao;

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.SystemConfigurationManager#delete(java.lang.Long)
     */

    public void delete(Long id) {
        systemConfigurationDao.removeObject(SystemConfiguration.class, id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.SystemConfigurationManager#loadAllConfig()
     */

    public List<SystemConfiguration> loadAllConfig() {
        return systemConfigurationDao.loadAllConfig();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.SystemConfigurationManager#loadAllConfigMap()
     */

    public HashMap<String, String> loadAllConfigMap() {
        return systemConfigurationDao.loadAllConfigMap();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ems.service.SystemConfigurationManager#loadConfigById(java.lang.Long)
     */

    public SystemConfiguration loadConfigById(Long id) {
        return systemConfigurationDao.loadConfigById(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.SystemConfigurationManager#save(com.ems.service. SystemConfigurationManager)
     */

    public SystemConfiguration save(SystemConfiguration systemConfiguration) {
        return (SystemConfiguration) systemConfigurationDao.saveObject(systemConfiguration);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.SystemConfigurationManager#update(com.ems.service. SystemConfigurationManager)
     */

    public SystemConfiguration update(SystemConfiguration systemConfiguration) {
        return (SystemConfiguration) systemConfigurationDao.saveObject(systemConfiguration);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.ems.service.SystemConfigurationManager#loadConfigByName(com.ems. service. SystemConfigurationManager)
     */

    public SystemConfiguration loadConfigByName(String name) {
        return systemConfigurationDao.loadConfigByName(name);
    }
    /**
     * Loads json object from sys config key. If key is not there then default object will be returned
     * 
     * @param name
     * @param type
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public T loadJSONObjectFromSysConfigByName(String name, Class<T> type) throws JsonParseException, JsonMappingException, IOException, InstantiationException, IllegalAccessException {
    	final SystemConfiguration dbjsonSC = loadConfigByName(name);
		String dbjson = dbjsonSC == null ?null : dbjsonSC.getValue();
		T obj = null; 
		final ObjectMapper mapper = new ObjectMapper();
		if (!StringUtils.isEmpty(dbjson)){
			obj = mapper.readValue(dbjson, type);
		}else{
			obj = type.newInstance();
		}
		return obj;
    }
    
    public void updateSysConfigJSON(final String name, final T obj) throws JsonGenerationException, JsonMappingException, IOException{
    	final ObjectMapper mapper = new ObjectMapper();
    	SystemConfiguration sc = loadConfigByName(name);
    	final String jsonStr = mapper.writeValueAsString(obj);
    	//syslog.debug("json String for object "+ obj.getClass().getName()+" to be updated::"+ jsonStr );
    	sc.setValue(jsonStr);
		update(sc);
    }
    
    public boolean isExternalUser(User user){
	 	SystemConfiguration authTypeConfig = this
				.loadConfigByName("auth.auth_type");
	 	AuthenticationType authenticationType = AuthenticationType.DATABASE;
		if (authTypeConfig != null) {
			authenticationType = AuthenticationType.valueOf(authTypeConfig
					.getValue());
			
		}
		if (user != null && ("admin".equals(user.getEmail()) )){
			return false;
		}
		return authenticationType != AuthenticationType.DATABASE;
		
    }
    
    public void saveInitialTTlConfiguration(SystemConfiguration ttlConfig){
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    		Map<String,Integer> map = new HashMap<String,Integer>();
    		map.put("rs2", 15);
    		map.put("su2", 3);
			String ttlConfigurationString = mapper.writeValueAsString(map);
			ttlConfig.setValue(ttlConfigurationString);
			save(ttlConfig);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
