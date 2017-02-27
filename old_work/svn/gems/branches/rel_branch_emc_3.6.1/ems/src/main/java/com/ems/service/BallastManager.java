package com.ems.service;

import java.math.BigInteger;
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

import com.ems.dao.BallastDao;
import com.ems.model.Ballast;
import com.ems.model.BallastList;
import com.ems.model.EventsAndFault;
import com.ems.utils.CommonUtils;
import com.ems.ws.util.Response;

@Service("ballastManager")
@Transactional(propagation = Propagation.REQUIRED)
public class BallastManager {

	@Resource
	BallastDao ballastDao;

	@Resource
	private EventsAndFaultManager eventManager;

	@Resource(name = "systemConfigurationManager")
    private SystemConfigurationManager systemConfigurationManager;

	@Autowired
	private MessageSource messageSource;

	private static final Logger m_Logger = Logger.getLogger("WSLogger");

	public Response uploadBallastList(List<Ballast> ballastList){
		Response resp = new Response();
		Integer status = 200;
		try
		{
			for(Ballast ballast : ballastList)
			{
				Map<String,Object> nameVal = new HashMap<String,Object>();
				nameVal.put("ballastName", ballast.getBallastName());
				nameVal.put("ballastInputVoltage", ballast.getInputVoltage());
				nameVal.put("ballastBulbType", ballast.getLampType());
				nameVal.put("ballastManufacturer", ballast.getBallastManufacturer());
				nameVal.put("displayLabel", ballast.getDisplayLabel());
				resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameVal);
				if(resp!= null && resp.getStatus()!=200){
					m_Logger.error("Validation error"+resp.getMsg());
					return resp;
				}

				Ballast ballastExisting = getBallastByDisplayLabel(ballast.getDisplayLabel());

				if(ballastExisting != null)
				{
					m_Logger.error("Ballast upload failed: Ballast with the same display lable already exists: " + ballast.getDisplayLabel());
					eventManager.addEvent("Ballast upload failed: Ballast with the same display lable already exists: " + ballast.getDisplayLabel(), EventsAndFault.FIXTURE_CONFIGURATION_UPLOAD);
					status = 300;
					continue;
				}

				addBallast(ballast);
			}
			resp.setStatus(status);		//Successful uploading of ballasts from EmConfig ,  keep this statement at last
		}
		catch(Exception e)
		{
			e.printStackTrace();
			resp.setStatus(500);
		}
		return resp;
	}

	public Ballast addBallast(Ballast ballast) {

		return ballastDao.addBallast(ballast);
    }
	public Ballast mergeBallast(Ballast ballast,List<BigInteger> fixturesIdList) {

		return ballastDao.mergeBallast(ballast,fixturesIdList);
    }

	public void editBallast(Ballast ballast,List<BigInteger> fixturesIdList) {

		ballastDao.editBallast(ballast,fixturesIdList);
	}

	public BallastList loadBallastList(String order,String orderway,Boolean bSearch, String searchField, String searchString, String searchOper, int offset, int limit) {
		return ballastDao.loadBallastList(order,orderway,bSearch,searchField,searchString,searchOper, offset, limit);
	}

	public void deleteBallastById (Long id) {
		ballastDao.deleteBallastById(id);

	}

	public Ballast getBallastById(Long id) {
		return ballastDao.getBallastById(id);
	}

	public Ballast getBallastByDisplayLabel(String displayLabel) {
		return ballastDao.getBallastByDisplayLabel(displayLabel);
	}

	public Ballast getBallastByName(String name) {
		return ballastDao.getBallastByName(name);
	}

	public List<Ballast> getAllBallasts() {
		return ballastDao.getAllBallasts();
	}

	public BallastList loadBallastListByUsage(String order,String orderway,Boolean bSearch, String searchField, String searchString, String searchOper, int offset,
			int limit) {
		// TODO Auto-generated method stub
		return ballastDao.loadBallastListByUsage(order,orderway,bSearch,searchField,searchString,searchOper, offset, limit);
	}
	public List<Object[]> getBallastCountByBallastName()
	{
       return  ballastDao.getBallastCountByBallastName();
	}
}
