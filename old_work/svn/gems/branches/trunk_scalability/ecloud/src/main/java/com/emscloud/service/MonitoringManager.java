package com.emscloud.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.template.CloudConnectionTemplate;
import com.communication.utils.CloudHttpResponse;
import com.emscloud.constant.ReplicaServerWebserviceUrl;
import com.emscloud.dao.EmHealthMonitorDao;
import com.emscloud.dao.EmInstanceDao;
import com.emscloud.model.EmHealthMonitor;
import com.emscloud.model.EmInstance;
import com.emscloud.util.CommonUtils;
import com.emscloud.util.Wrapper;
import com.emscloud.vo.FixtureHealthDataVO;
import com.emscloud.vo.GatewayHealthDataVO;


@Service("monitoringManager")
@Transactional(propagation = Propagation.REQUIRED)
public class MonitoringManager {
	
	@Resource
	EmInstanceDao emInstanceDao;
	
	@Resource
	EmHealthMonitorDao emHealthMonitorDao;
	
	@Resource
	EmInstanceManager		emInstanceManager;
	
	@Resource
	CloudConnectionTemplate cloudConnectionTemplate ;
	
	public void updateHealthMonitor(String mac, int totalGW, int uoGW, int cGW, int totalSensors, int uoSensors, int criticalSensors){
		
		EmInstance emInstance = emInstanceDao.getEmInstanceForMac(mac);		
		EmHealthMonitor healthMonitor = new EmHealthMonitor();
		healthMonitor.setCaptureAt(new Date());
		healthMonitor.setEmInstance(emInstance);
		healthMonitor.setGatewaysTotal(totalGW);
		healthMonitor.setGatewaysUnderObservation(uoGW);
		healthMonitor.setGatewaysCritical(cGW);
		healthMonitor.setSensorsTotal(totalSensors);
		healthMonitor.setSensorsUnderObservation(uoSensors);
		healthMonitor.setSensorsCritical(criticalSensors);					
		emHealthMonitorDao.saveObject(healthMonitor);
	}
	
	public  List<GatewayHealthDataVO> getGatewayListForHealthData(String emInstanceId)
	{
		List<GatewayHealthDataVO> gatewayHealthDataVO = null; 
		try{
		EmInstance em = emInstanceManager.loadEmInstanceById(Long.parseLong(emInstanceId));
		String emMac = em.getMacId() ;
		CloudHttpResponse result = cloudConnectionTemplate.executeGet(ReplicaServerWebserviceUrl.GET_GATEWAY_HEALTH_DATA+emMac, em.getReplicaServer().getInternalIp() );
		String response = result.getResponse() ;
		JAXBContext jaxbContext = JAXBContext.newInstance(Wrapper.class ,GatewayHealthDataVO.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		 gatewayHealthDataVO =  CommonUtils.unmarshal(jaxbUnmarshaller, GatewayHealthDataVO.class, response);
		return gatewayHealthDataVO;
		}catch(Exception ex)
		{
			ex.printStackTrace();
			return gatewayHealthDataVO ;
		}
    	
	}

	public List<FixtureHealthDataVO> getFixtureListForHealthData(
			String emInstanceId) {
		List<FixtureHealthDataVO> fixtureHealthDataVO = null; 
		try{
		EmInstance em = emInstanceManager.loadEmInstanceById(Long.parseLong(emInstanceId));
		String emMac = em.getMacId() ;
		CloudHttpResponse result = cloudConnectionTemplate.executeGet(ReplicaServerWebserviceUrl.GET_FIXTURE_HEALTH_DATA+emMac, em.getReplicaServer().getInternalIp() );
		String response = result.getResponse() ;
		System.out.println(response);
		JAXBContext jaxbContext = JAXBContext.newInstance(Wrapper.class ,FixtureHealthDataVO.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		fixtureHealthDataVO =  CommonUtils.unmarshal(jaxbUnmarshaller, FixtureHealthDataVO.class, response);
		return fixtureHealthDataVO;
		}catch(Exception ex)
		{
			ex.printStackTrace();
			return fixtureHealthDataVO ;
		}
	}

}
