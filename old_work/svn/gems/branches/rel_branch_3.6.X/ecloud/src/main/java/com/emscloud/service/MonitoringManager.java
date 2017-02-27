package com.emscloud.service;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.communication.utils.ArgumentUtils;
import com.emscloud.communication.ResponseWrapper;
import com.emscloud.communication.adaptor.CloudAdapter;
import com.emscloud.constant.ReplicaServerWebserviceUrl;
import com.emscloud.dao.EmHealthMonitorDao;
import com.emscloud.dao.EmInstanceDao;
import com.emscloud.model.EmHealthMonitor;
import com.emscloud.model.EmInstance;
import com.emscloud.vo.FixtureHealthDataVO;
import com.emscloud.vo.GatewayHealthDataVO;
import com.sun.jersey.api.client.GenericType;

@Service("monitoringManager")
@Transactional(propagation = Propagation.REQUIRED)
public class MonitoringManager {
	static final Logger logger = Logger.getLogger(MonitoringManager.class
			.getName());
	@Resource
	EmInstanceDao emInstanceDao;

	@Resource
	EmHealthMonitorDao emHealthMonitorDao;

	@Resource
	EmInstanceManager emInstanceManager;

	@Resource
	CloudAdapter cloudAdapter;


	public void updateHealthMonitor(String mac, int totalGW, int uoGW, int cGW,
			int totalSensors, int uoSensors, int criticalSensors) {

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

	public List<GatewayHealthDataVO> getGatewayListForHealthData(
			String emInstanceId) {
		List<GatewayHealthDataVO> gatewayHealthDataVO = null;
		try {
			EmInstance em = emInstanceManager.loadEmInstanceById(Long
					.parseLong(emInstanceId));
			if(em != null){
				String emMac = em.getMacId();
				ResponseWrapper<List<GatewayHealthDataVO>> response = cloudAdapter
						.executeGet(em,
								ReplicaServerWebserviceUrl.GET_GATEWAY_HEALTH_DATA
										+ emMac, MediaType.APPLICATION_XML,
								new GenericType<List<GatewayHealthDataVO>>() {
								});
				if (!ArgumentUtils.isNull(response.getStatus())
						&& response.getStatus() == Response.Status.OK
								.getStatusCode()) {
					gatewayHealthDataVO = response.getItems();
				}
			}
			

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);

		}
		return gatewayHealthDataVO;
	}

	public List<FixtureHealthDataVO> getFixtureListForHealthData(
			String emInstanceId) {
		List<FixtureHealthDataVO> fixtureHealthDataVO = null;
		try {
			EmInstance em = emInstanceManager.loadEmInstanceById(Long
					.parseLong(emInstanceId));
			if(em != null){
				String emMac = em.getMacId();
				ResponseWrapper<List<FixtureHealthDataVO>> response = cloudAdapter
						.executeGet(em,
								ReplicaServerWebserviceUrl.GET_FIXTURE_HEALTH_DATA
										+ emMac, MediaType.APPLICATION_XML,
								new GenericType<List<FixtureHealthDataVO>>() {
								});
				if (!ArgumentUtils.isNull(response.getStatus())
						&& response.getStatus() == Response.Status.OK
								.getStatusCode()) {
					fixtureHealthDataVO = response.getItems();
				}
			}
			

		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return fixtureHealthDataVO;
	}

	public void deleteEmHealthMonitorByEmId(Long id) {
		emHealthMonitorDao.deleteEmHealthMonitorByEmId(id);
	}

}
