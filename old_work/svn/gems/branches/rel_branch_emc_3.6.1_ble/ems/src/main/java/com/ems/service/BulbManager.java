package com.ems.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.dao.BulbDao;
import com.ems.model.Bulb;
import com.ems.model.BulbList;
import com.ems.model.EventsAndFault;
import com.ems.utils.CommonUtils;
import com.ems.ws.util.Response;


@Service("bulbManager")
@Transactional(propagation = Propagation.REQUIRED)
public class BulbManager {

	@Resource
	BulbDao bulbDao;
	
	@Resource
	private EventsAndFaultManager eventManager;

	private static final Logger m_Logger = Logger.getLogger("WSLogger");
	
	@Resource(name = "systemConfigurationManager")
    private SystemConfigurationManager systemConfigurationManager;
	
	@Autowired
	private MessageSource messageSource;
	
	public BulbList loadBulbList(String orderway, int offset, int limit) {
		return bulbDao.loadBulbList(orderway, offset, limit);
	}
	
	public Response uploadBulbList(List<Bulb> bulbList) {
		Response resp = new Response();
		Integer status = 200;
		try
		{
			for(Bulb bulb : bulbList)
			{
				Map<String,Object> nameVal = new HashMap<String,Object>();
				nameVal.put("bulbname", bulb.getBulbName());
				nameVal.put("bulbtype", bulb.getType());	
				nameVal.put("bulbmanufacturer", bulb.getManufacturer());
				resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameVal);
				if(resp!= null && resp.getStatus()!=200){
					m_Logger.error("Validation error"+resp.getMsg());
					return resp;
				}
				Bulb bulbExisting = getBulbByName(bulb.getBulbName());
				
				if(bulbExisting != null)
				{
					m_Logger.error("Bulb upload failed: Bulb with the same name already exists: " + bulb.getBulbName());
					eventManager.addEvent("Bulb upload failed: Bulb with the same name already exists: " + bulb.getBulbName(), EventsAndFault.FIXTURE_CONFIGURATION_UPLOAD, EventsAndFault.MAJOR_SEV_STR);
					status = 300;
					continue;
				}

				addBulb(bulb);
			}
			resp.setStatus(status);		//Successful uploading of bulbs from EmConfig, keep this statement at last 
		}
		catch(Exception e)
		{
			e.printStackTrace();
			resp.setStatus(500);
		}
		return resp;
    }
	
	public Bulb addBulb(Bulb bulb) {
		return (Bulb)bulbDao.addBulb(bulb);		
    }
	
	public Bulb mergeBulb(Bulb bulb) {
		return (Bulb)bulbDao.mergeBulb(bulb);		
    }
	
	public Bulb getBulbById(Long id)
	{
		return bulbDao.getBulbById(id);
	}

	public void editBulb(Bulb bulb) {
		// TODO Auto-generated method stub
		bulbDao.editBulb(bulb);
	}

	public void deleteBulbById(Long id) {
		// TODO Auto-generated method stub
		bulbDao.deleteBulbById(id);		
	}

	public Bulb getBulbByName(String bulbName) {
		// TODO Auto-generated method stub
		return bulbDao.getBulbByName(bulbName);
	}
	
	public List<Bulb> getAllBulbs() {
		return bulbDao.getAllBulbs();
	}
	public List<Object[]> getBulbsCountByBulbName()
    {
       return  bulbDao.getBulbsCountByBulbName();
    }
}
