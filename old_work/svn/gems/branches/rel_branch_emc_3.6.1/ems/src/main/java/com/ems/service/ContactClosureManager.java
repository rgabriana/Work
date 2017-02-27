package com.ems.service;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ems.model.NetworkInterfaceMapping;
import com.ems.model.SystemConfiguration;
import com.ems.server.SchedulerManager;
import com.ems.server.device.barionet.BarionetImpl;
import com.ems.server.device.barionet.BarionetListener;
import com.ems.types.NetworkType;
import com.ems.utils.AdminUtil;
import com.ems.utils.ArgumentUtils;
import com.ems.vo.ContactClosure;
import com.ems.vo.ContactClosureControls;
import com.ems.vo.ContactClosureVo;

@Service("contactClosureManager")
@Transactional(propagation = Propagation.REQUIRED)
public class ContactClosureManager {
	private static final Logger m_Logger = Logger.getLogger("SysLog");

	@Resource(name = "systemConfigurationManager")
	SystemConfigurationManager systemConfigurationManager;

	JobDetail contactClosureSchedulerJob;

	@Resource
	NetworkSettingsManager networkSettingsManager;

	@Resource(name = "emManager")
	EMManager emManager;
	@Resource(name = "switchManager")
	private SwitchManager switchManager;

	private Map<String, BarionetListener> ccList = Collections.synchronizedMap(new HashMap<String, BarionetListener>());
	private List<String> ccArrayList = Collections.synchronizedList(new ArrayList<String>());

	private boolean isRunning = false;
	private BarionetImpl oBarionetDevice = null;

	private static final int STATE_ZERO_TO_ONE = 1;
	private static final int DO_NOTHING = 0;
	private static final int ALL_ON = 1;
	private static final int SELECTED_SWITCH_ALL_ON = 2;
	private static final int ALL_AUTO = 3;
	private static final int SELECTED_SWITCH_AUTO = 4;
	private static final int BARIONET_TELNET_PORT = 12302;

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public String getDefaultContactClosure() {
		ContactClosure contactClosure = new ContactClosure();
		contactClosure.setEnabled(false);

		List<ContactClosureVo> contactClosureVoList = new ArrayList<ContactClosureVo>();

		List<ContactClosureControls> contactClosureControlsList = new ArrayList<ContactClosureControls>();

		ContactClosureControls contactClosureControls1 = new ContactClosureControls();

		contactClosureControls1.setName("201");
		contactClosureControls1.setAction(0);
		contactClosureControls1.setDuration(0);
		contactClosureControls1.setSubAction("");
		contactClosureControls1.setPercentage(100);

		contactClosureControlsList.add(contactClosureControls1);

		ContactClosureControls contactClosureControls2 = new ContactClosureControls();

		contactClosureControls2.setName("202");
		contactClosureControls2.setAction(0);
		contactClosureControls2.setDuration(0);
		contactClosureControls2.setSubAction("");
		contactClosureControls2.setPercentage(100);

		contactClosureControlsList.add(contactClosureControls2);

		ContactClosureControls contactClosureControls3 = new ContactClosureControls();

		contactClosureControls3.setName("203");
		contactClosureControls3.setAction(0);
		contactClosureControls3.setDuration(0);
		contactClosureControls3.setSubAction("");
		contactClosureControls3.setPercentage(100);

		contactClosureControlsList.add(contactClosureControls3);

		ContactClosureControls contactClosureControls4 = new ContactClosureControls();

		contactClosureControls4.setName("204");
		contactClosureControls4.setAction(0);
		contactClosureControls4.setDuration(0);
		contactClosureControls4.setSubAction("");
		contactClosureControls4.setPercentage(100);

		contactClosureControlsList.add(contactClosureControls4);

		ContactClosureVo contactClosureVo1 = new ContactClosureVo();

		contactClosureVo1
				.setContactClosureControlsList(contactClosureControlsList);

		contactClosureVo1.setIpAddress("");

		contactClosureVo1.setMacAddress("");

		contactClosureVo1.setProductId("");

		contactClosureVo1.setHwType("");

		contactClosureVo1.setFwVersion("");

		contactClosureVoList.add(contactClosureVo1);

		contactClosure.setContactClosureVo(contactClosureVoList);

		String contactClosureJsonString = "";
		try {
			ObjectMapper mapper = new ObjectMapper();
			contactClosureJsonString = mapper
					.writeValueAsString(contactClosure);
		} catch (JsonGenerationException e) {
			m_Logger.error("Error: ", e);
		} catch (JsonMappingException e) {
			m_Logger.error("Error: ", e);
		} catch (IOException e) {
			m_Logger.error("Error: ", e);
		}
		return contactClosureJsonString;
	}
	
	private List<ContactClosureControls> getDefaultCCList() {
		List<ContactClosureControls> contactClosureControlsList = new ArrayList<ContactClosureControls>();

		ContactClosureControls contactClosureControls1 = new ContactClosureControls();

		contactClosureControls1.setName("201");
		contactClosureControls1.setAction(0);
		contactClosureControls1.setDuration(0);
		contactClosureControls1.setSubAction("");
		contactClosureControls1.setPercentage(100);

		contactClosureControlsList.add(contactClosureControls1);

		ContactClosureControls contactClosureControls2 = new ContactClosureControls();

		contactClosureControls2.setName("202");
		contactClosureControls2.setAction(0);
		contactClosureControls2.setDuration(0);
		contactClosureControls2.setSubAction("");
		contactClosureControls2.setPercentage(100);

		contactClosureControlsList.add(contactClosureControls2);

		ContactClosureControls contactClosureControls3 = new ContactClosureControls();

		contactClosureControls3.setName("203");
		contactClosureControls3.setAction(0);
		contactClosureControls3.setDuration(0);
		contactClosureControls3.setSubAction("");
		contactClosureControls3.setPercentage(100);

		contactClosureControlsList.add(contactClosureControls3);

		ContactClosureControls contactClosureControls4 = new ContactClosureControls();

		contactClosureControls4.setName("204");
		contactClosureControls4.setAction(0);
		contactClosureControls4.setDuration(0);
		contactClosureControls4.setSubAction("");
		contactClosureControls4.setPercentage(100);

		contactClosureControlsList.add(contactClosureControls4);
		return contactClosureControlsList;
	}
	
	private List<ContactClosureControls> getDefaultBarionet100CCList() {
		List<ContactClosureControls> contactClosureControlsList = new ArrayList<ContactClosureControls>();

		ContactClosureControls contactClosureControls1 = new ContactClosureControls();

		contactClosureControls1.setName("201");
		contactClosureControls1.setAction(0);
		contactClosureControls1.setDuration(0);
		contactClosureControls1.setSubAction("");
		contactClosureControls1.setPercentage(100);

		contactClosureControlsList.add(contactClosureControls1);

		ContactClosureControls contactClosureControls2 = new ContactClosureControls();

		contactClosureControls2.setName("202");
		contactClosureControls2.setAction(0);
		contactClosureControls2.setDuration(0);
		contactClosureControls2.setSubAction("");
		contactClosureControls2.setPercentage(100);

		contactClosureControlsList.add(contactClosureControls2);

		ContactClosureControls contactClosureControls3 = new ContactClosureControls();

		contactClosureControls3.setName("203");
		contactClosureControls3.setAction(0);
		contactClosureControls3.setDuration(0);
		contactClosureControls3.setSubAction("");
		contactClosureControls3.setPercentage(100);

		contactClosureControlsList.add(contactClosureControls3);

		ContactClosureControls contactClosureControls4 = new ContactClosureControls();

		contactClosureControls4.setName("204");
		contactClosureControls4.setAction(0);
		contactClosureControls4.setDuration(0);
		contactClosureControls4.setSubAction("");
		contactClosureControls4.setPercentage(100);

		contactClosureControlsList.add(contactClosureControls4);
		
		ContactClosureControls contactClosureControls5 = new ContactClosureControls();

		contactClosureControls5.setName("205");
		contactClosureControls5.setAction(0);
		contactClosureControls5.setDuration(0);
		contactClosureControls5.setSubAction("");
		contactClosureControls5.setPercentage(100);

		contactClosureControlsList.add(contactClosureControls5);
		
		ContactClosureControls contactClosureControls6 = new ContactClosureControls();

		contactClosureControls6.setName("206");
		contactClosureControls6.setAction(0);
		contactClosureControls6.setDuration(0);
		contactClosureControls6.setSubAction("");
		contactClosureControls6.setPercentage(100);

		contactClosureControlsList.add(contactClosureControls6);
		
		ContactClosureControls contactClosureControls7 = new ContactClosureControls();

		contactClosureControls7.setName("207");
		contactClosureControls7.setAction(0);
		contactClosureControls7.setDuration(0);
		contactClosureControls7.setSubAction("");
		contactClosureControls7.setPercentage(100);

		contactClosureControlsList.add(contactClosureControls7);
		
		ContactClosureControls contactClosureControls8 = new ContactClosureControls();

		contactClosureControls8.setName("208");
		contactClosureControls8.setAction(0);
		contactClosureControls8.setDuration(0);
		contactClosureControls8.setSubAction("");
		contactClosureControls8.setPercentage(100);

		contactClosureControlsList.add(contactClosureControls8);
				
		return contactClosureControlsList;
	}
	
	public synchronized ContactClosure getCCDataFromDB() {
		ContactClosure contactClosureDB = null;
		SystemConfiguration contactClosureConfiguraiton = systemConfigurationManager
				.loadConfigByName("contact_closure_configuration");
		if (contactClosureConfiguraiton != null) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				contactClosureDB = mapper.readValue(contactClosureConfiguraiton.getValue(),ContactClosure.class);
			} catch (JsonGenerationException e) {
				m_Logger.error("Error: ", e);
			} catch (JsonMappingException e) {
				m_Logger.error("Error: ", e);
			} catch (IOException e) {
				m_Logger.error("Error: ", e);
			}
		}
		return contactClosureDB;
	}

	public synchronized void saveCCDataToDB(ContactClosure contactClosureDB) {
		SystemConfiguration contactClosureConfiguraiton = systemConfigurationManager
				.loadConfigByName("contact_closure_configuration");
		if (contactClosureConfiguraiton != null) {
			try {
				ObjectMapper mapper = new ObjectMapper();
				String contactClosureJsonString = mapper
						.writeValueAsString(contactClosureDB);

				contactClosureConfiguraiton.setValue(contactClosureJsonString);
				systemConfigurationManager.save(contactClosureConfiguraiton);
			} catch (JsonGenerationException e) {
				m_Logger.error("Error: ", e);
			} catch (JsonMappingException e) {
				m_Logger.error("Error: ", e);
			} catch (IOException e) {
				m_Logger.error("Error: ", e);
			}
		}
	}
	// Do we need this function?
	public void saveContactClosure(ContactClosure contactClosure) {
		ContactClosure contactClosureDB = getCCDataFromDB();
		if (contactClosureDB != null) {
			for (ContactClosureVo contactClosureVo1 : contactClosureDB
					.getContactClosureVo()) {
				if (contactClosure.getContactClosureVo().get(0).getMacAddress()
						.equalsIgnoreCase(contactClosureVo1.getMacAddress())) {
					contactClosureVo1
							.setContactClosureControlsList(contactClosure
									.getContactClosureVo().get(0)
									.getContactClosureControlsList());
					break;
				}
			}
			saveCCDataToDB(contactClosureDB);
		}
	}
	
	public void enableDisableBarionetPort(boolean isContactClosureEnabled){
		String nwinterface;
		
		final NetworkInterfaceMapping netbuild = networkSettingsManager.loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Building.getName());
		if (netbuild != null && netbuild.getNetworkSettings() != null ){
			nwinterface = "";
			nwinterface = netbuild.getNetworkSettings().getName();
			if(!StringUtils.isEmpty(nwinterface)){
				if (isContactClosureEnabled) {
					AdminUtil.enableDisablePort(BARIONET_TELNET_PORT, 0, nwinterface, null);
				}else{
					AdminUtil.enableDisablePort(0, BARIONET_TELNET_PORT, null, nwinterface);
				}
			}
		}
		
		final NetworkInterfaceMapping netcorp = networkSettingsManager.loadCurrentNetworkInterfaceMappingByNetworkType(NetworkType.Corporate.getName());
		if (netcorp != null && netcorp.getNetworkSettings() != null ){
			nwinterface = "";
			nwinterface = netcorp.getNetworkSettings().getName();
			if(!StringUtils.isEmpty(nwinterface)){
				if (isContactClosureEnabled) {
				AdminUtil.enableDisablePort(BARIONET_TELNET_PORT, 0, nwinterface, null);
			}else{
				AdminUtil.enableDisablePort(0, BARIONET_TELNET_PORT, null, nwinterface);
			}	
			}
		}
		
	}

	public void monitorBarionetDeviceIfEnabled(ContactClosure contactClosure) {
		if (contactClosure.getEnabled()) {
			if (oBarionetDevice == null) {
				oBarionetDevice = new BarionetImpl();
				oBarionetDevice.start();
			} else {
				if (!oBarionetDevice.isRunning()) {
					oBarionetDevice = null;
					oBarionetDevice = new BarionetImpl();
					oBarionetDevice.start();
				}
			}
			startBarionetMonitor();
			createNewContactClosureSchedulerJob(true);
		}
	}
	
	public void enableDisableContactClosure(Boolean isEnabled) {
		ContactClosure contactClosure = null;
		try {
			contactClosure = getCCDataFromDB();
			if (contactClosure != null) {
				contactClosure.setEnabled(isEnabled);
				saveCCDataToDB(contactClosure);
			}
		} catch (Exception e) {
			m_Logger.error("Error: ", e);
		} finally {
			if (contactClosure.getEnabled()) {
				enableDisableBarionetPort(true);
				monitorBarionetDeviceIfEnabled(contactClosure);
			} else {
				enableDisableBarionetPort(false);
				createNewContactClosureSchedulerJob(false);
				if (oBarionetDevice != null) {
					oBarionetDevice.stopRunning();
				}
				stopBarionetMonitor();
			}
		}
	}
	
	public synchronized void updateDefaultContactClosure(ContactClosureVo oCC) {
		try {
			boolean bUpdate = false;
			ContactClosure contactClosure = getCCDataFromDB();
			if (contactClosure != null) {
				Iterator<ContactClosureVo> oCCItr = contactClosure
						.getContactClosureVo().iterator();
				while (oCCItr.hasNext()) {
					ContactClosureVo ccObj = oCCItr.next();
					if (ccObj.getMacAddress().equalsIgnoreCase(
							"")) {
						m_Logger.info("Updating default: "
								+ oCC.getMacAddress());
						ccObj.setMacAddress(oCC.getMacAddress());
						ccObj.setIpAddress(oCC.getIpAddress());
						bUpdate = true;
						break;
					}
				}
				if (bUpdate) {
					saveCCDataToDB(contactClosure);
				}
			}
		} catch (Exception e) {
			m_Logger.error("Error: ", e);
		} finally {
			startBarionMonitor(oCC);
		}
	}

	public synchronized ContactClosureVo getContactClosureVoByMacAddress(
			String macAddress) {
		ContactClosureVo contactClosureVo = null;
		try {
			ContactClosure contactClosure = getCCDataFromDB();
			if (contactClosure != null) {
				for (ContactClosureVo contactClosureVo1 : contactClosure
						.getContactClosureVo()) {
					if (macAddress.equalsIgnoreCase(contactClosureVo1
							.getMacAddress())) {
						contactClosureVo = contactClosureVo1;
						break;
					}
				}
			}
		} catch (NullPointerException e) {
			m_Logger.error("Error: ", e);
		} catch (Exception e) {
			m_Logger.error("Error: ", e);
		}

		return contactClosureVo;
	}
	
	public synchronized void updateContactClosure(ContactClosureVo oCC) {
		try {
			boolean bUpdate = false;
			ContactClosure contactClosure = getCCDataFromDB();
			if (contactClosure != null) {
				Iterator<ContactClosureVo> oCCItr = contactClosure
						.getContactClosureVo().iterator();
				while (oCCItr.hasNext()) {
					ContactClosureVo ccObj = oCCItr.next();
					if (ccObj.getMacAddress().equalsIgnoreCase(
							oCC.getMacAddress())) {
						m_Logger.info("Updating: " + oCC.getMacAddress());
						ccObj.setMacAddress(oCC.getMacAddress());
						ccObj.setIpAddress(oCC.getIpAddress());
						bUpdate = true;
						break;
					}
				}
				if (bUpdate) {
					saveCCDataToDB(contactClosure);
				}
			}
		} catch (Exception e) {
			m_Logger.error("Error: ", e);
		} finally {
			startBarionMonitor(oCC);
		}
	}
	
	public synchronized void updateContactClosureControlList(
			ContactClosureVo oCC) {
		boolean bUpdate = false;
		try {
			ContactClosure contactClosure = getCCDataFromDB();
			if (contactClosure != null) {
				Iterator<ContactClosureVo> oCCItr = contactClosure
						.getContactClosureVo().iterator();
				while (oCCItr.hasNext()) {
					ContactClosureVo ccObj = oCCItr.next();
					if (ccObj.getMacAddress().equalsIgnoreCase(
							oCC.getMacAddress())) {
						m_Logger.info("Updating CCList: " + oCC.getMacAddress());
						ccObj.setContactClosureControlsList(oCC
								.getContactClosureControlsList());
						bUpdate = true;
						break;
					}
				}
				if (bUpdate) {
					saveCCDataToDB(contactClosure);
				}
			}
		} catch (Exception e) {
			m_Logger.error("Error: ", e);
		}
	}

	public synchronized void addContactClosure(ContactClosureVo ccvo) {
		try {
			ContactClosure contactClosure = getCCDataFromDB();
			if (contactClosure != null) {
				if("0.0.0.0".equals(ccvo.getIpAddress())) {
						m_Logger.error("Error: Contact closure with IP Address 0.0.0.0 found, ignoring it");
				}
				else {
					ccvo.setContactClosureControlsList(getDefaultCCList());
					ccvo.setFwVersion("");
					ccvo.setHwType("");
					ccvo.setProductId("");
					contactClosure.getContactClosureVo().add(ccvo);
					saveCCDataToDB(contactClosure);
				}
			}
		} catch (Exception e) {
			m_Logger.error("Error: ", e);
		} finally {
			startBarionMonitor(ccvo);
		}

	}

	public synchronized boolean removeContactClosure(String macAddress) {
		Boolean retVal = false;
		try {
			ContactClosure contactClosure = getCCDataFromDB();
			
			if (contactClosure != null) {
				Iterator<ContactClosureVo> oCCItr = contactClosure
						.getContactClosureVo().iterator();
				while (oCCItr.hasNext()) {
					ContactClosureVo ccObj = oCCItr.next();
					if (macAddress != null  && macAddress.equalsIgnoreCase(ccObj.getMacAddress())) {
						stopBarionetMonitor(ccObj);
						oCCItr.remove();
						saveCCDataToDB(contactClosure);
						retVal = true;
						break;
					}
				}
			}
		} catch (Exception e) {
			m_Logger.error("Error: ", e);
		}		
		return retVal;
	}
	
	public synchronized void updateContactClosureControlListForBarionet100(
			String macAddress) {
		boolean bUpdate = false;
		try {
			ContactClosure contactClosure = getCCDataFromDB();
			if (contactClosure != null) {
				Iterator<ContactClosureVo> oCCItr = contactClosure
						.getContactClosureVo().iterator();
				while (oCCItr.hasNext()) {
					ContactClosureVo ccObj = oCCItr.next();
					if (macAddress.equalsIgnoreCase(ccObj.getMacAddress())) {
						if (!ArgumentUtils.isNullOrEmpty(ccObj
								.getContactClosureControlsList())) {
							if (ccObj.getContactClosureControlsList().size() != 8) {
								ccObj.setContactClosureControlsList(getDefaultBarionet100CCList());
								bUpdate = true;
							}
						}
						break;
					}
				}
				if (bUpdate) {
					saveCCDataToDB(contactClosure);
				}
			}
		} catch (Exception e) {
			m_Logger.error("Error: ", e);
		}

	}
	
	public void healthCheckBarionetMonitor() {
		if (!isRunning()) {
			setRunning(true);
			startBarionetMonitor();
			setRunning(false);
		}
	}
	
	public void stopBarionetMonitor() {
		synchronized (ccList) {
			Iterator<BarionetListener> ccValues = ccList.values().iterator();
			while (ccValues.hasNext()) {
				BarionetListener bl = ccValues.next();
				if (bl != null) {
					bl.cleanUp();
					ccValues.remove();
				}
			}
		}
	}

	public void startBarionetMonitor() {
		ContactClosure ccObj = getCCDataFromDB();
		if (ccObj.getEnabled()) {
			List<ContactClosureVo> ccvoList = ccObj.getContactClosureVo();
			if (ccvoList != null) {
				Iterator<ContactClosureVo> ccvoItr = ccvoList.iterator();
				while (ccvoItr.hasNext()) {
					ContactClosureVo ccvo = ccvoItr.next();
					startBarionMonitor(ccvo);
				}
			}
		}
	}
	
	public void startBarionMonitor(ContactClosureVo ccvo) {
		if (!ccvo.getMacAddress().equals("")) {
			BarionetListener barionetObj = ccList.get(ccvo.getMacAddress());
			if (barionetObj == null) {
				barionetObj = new BarionetListener(ccvo.getMacAddress(), ccvo.getIpAddress());
				ccList.put(ccvo.getMacAddress(), barionetObj);
				ccArrayList.add(ccvo.getMacAddress());
				barionetObj.start();
			}else {
				barionetObj.ping();
			}
		}
	}

	private void stopBarionetMonitor(ContactClosureVo ccvo) {
		if (!ccvo.getMacAddress().equals("")) {
			BarionetListener barionetObj = ccList.get(ccvo.getMacAddress());
			if (barionetObj != null) {
				if(barionetObj.isRunning()) {
					barionetObj.cleanUp();
					barionetObj.interrupt();
				}
				ccList.remove(ccvo.getMacAddress());
				ccArrayList.remove(ccvo.getMacAddress());
			}
		}
		
	}
	
	public void removeCCEntry(String sMacaddress) {
		ccList.remove(sMacaddress);
	}
	
	public void removeCCArrayListEntry(String sMacaddress) {
		ccArrayList.remove(sMacaddress);
	}
	
	public boolean isCCArrayListContainsMacAddress(String sMacaddress){
		return ccArrayList.contains(sMacaddress);
	}

	public void createNewContactClosureSchedulerJob(
			Boolean isContactClosureEnabled) {

		String ContactClosureSchedulerJobName = "contactClosureSchedulerJob";
		String ContactClosureSchedulerTriggerName = "ContactClosureSchedulerJobTrigger";
		String contactClosureCron15Minstatement = "0 0/1 * * * ?";

		try {
			// check if job exist, if not create.
			// Delete the older Quartz job and create a new one
			if (SchedulerManager
					.getInstance()
					.getScheduler()
					.checkExists(
							new JobKey(ContactClosureSchedulerJobName,
									SchedulerManager.getInstance()
											.getScheduler().getSchedulerName()))) {
				if (SchedulerManager
						.getInstance()
						.getScheduler()
						.deleteJob(
								new JobKey(ContactClosureSchedulerJobName,
										SchedulerManager.getInstance()
												.getScheduler()
												.getSchedulerName())) == false)
					m_Logger.debug("Failed to delete Quartz job"
							+ ContactClosureSchedulerJobName);
			}
		} catch (Exception e) {
			m_Logger.error(e.getMessage(), e);
		}

		if (isContactClosureEnabled) {
			try {

				// create job
				contactClosureSchedulerJob = newJob(
						ContactClosureSchedulerJob.class).withIdentity(
						ContactClosureSchedulerJobName,
						SchedulerManager.getInstance().getScheduler()
								.getSchedulerName()).build();
				// create trigger
				CronTrigger contactClosureSchedulerJobTrigger = (CronTrigger) newTrigger()
						.withIdentity(
								ContactClosureSchedulerTriggerName,
								SchedulerManager.getInstance().getScheduler()
										.getSchedulerName())
						.withSchedule(
								CronScheduleBuilder
										.cronSchedule(contactClosureCron15Minstatement))
						.startNow().build();

				// schedule job
				SchedulerManager
						.getInstance()
						.getScheduler()
						.scheduleJob(contactClosureSchedulerJob,
								contactClosureSchedulerJobTrigger);

			} catch (Exception e) {
				m_Logger.error(e.getMessage(), e);
			}
		}
	}

	public void sendDiscoveryCmd() {
		if (oBarionetDevice != null && oBarionetDevice.isRunning()) {
			String corporateMapping="eth0";
			String nimCorporate = networkSettingsManager.loadCurrentMappingByNetworkType(NetworkType.Corporate.getName());
			if (nimCorporate == null) {
				nimCorporate = corporateMapping;
			}
			String nimBuilding = networkSettingsManager.loadCurrentMappingByNetworkType(NetworkType.Building.getName());
			if(nimBuilding == null){
				nimBuilding = nimCorporate;
			}
			
			NetworkInterface ni;
			try {
				ni = NetworkInterface.getByName(nimBuilding);
				oBarionetDevice.sendBroadcastReq(ni);
			} catch (SocketException e) {
				m_Logger.error("Error sending discovery cmd: " + e.getMessage());
			}
		}
	}
	
	/**
	 * statechange,1,0\nstatechange,2,0\nstatechange,3,0\nstatechange,4,0\n
	 * statechange
	 * ,201,0\nstatechange,202,0\nstatechange,203,0\nstatechange,204,0
	 * 
	 * @param contactClosureVo
	 */
	public void checkInputTriggerStatus(String macAddress,
			Map<String, Integer> oInputTriggerMap) {
		ContactClosureVo contactClosureVo = getContactClosureVoByMacAddress(macAddress);
		m_Logger.debug("Processing state change event: "
				+ contactClosureVo.getMacAddress());
		boolean bUpdate = false;
		List<ContactClosureControls> controlslist = contactClosureVo
				.getContactClosureControlsList();
		if (controlslist != null) {
			Iterator<ContactClosureControls> controlsItr = controlslist
					.iterator();
			while (controlsItr.hasNext()) {
				ContactClosureControls cccObj = controlsItr.next();
				if (cccObj != null) {
					Integer value = -1;
					int action = cccObj.getAction();
					if ((value = oInputTriggerMap.get(cccObj.getName())) != null) {
						if (cccObj.getLastStatus() != value.intValue()) {
							bUpdate = true;
							cccObj.setLastStatus(value.intValue());
							m_Logger.debug(contactClosureVo.getMacAddress()
									+ ", " + contactClosureVo.getIpAddress()
									+ ", Trigger " + " => " + cccObj.getName()
									+ ", State Changed to : "
									+ value.intValue() + ", Perform Action: "
									+ action);
							if (value.intValue() == STATE_ZERO_TO_ONE) {
								switch (action) {
								case ALL_ON:
									emManager.setEmergencyOnByPercentage(cccObj
											.getDuration(),cccObj
											.getPercentage());
									break;
								case SELECTED_SWITCH_ALL_ON:
									if (!"".equals(cccObj.getSubAction())) {
										String[] switchSceneIdArray = cccObj
												.getSubAction().split(",");
										for (int i = 0; i < switchSceneIdArray.length; i++) {
											long switchId = Long.parseLong(switchSceneIdArray[i].split("_")[0]);
											long sceneId = Long.parseLong(switchSceneIdArray[i].split("_")[1]);
											int sceneOrder = switchManager.getSceneOrderForGivenSwitchAndScene(switchId,sceneId);
											switchManager.sendSwitchGroupMsgToFixture(switchId,"scene", sceneOrder);
										}
									}

									break;
								case ALL_AUTO:
									emManager.setAllAuto();
									break;
								case SELECTED_SWITCH_AUTO:
									if (!"".equals(cccObj.getSubAction())) {
										String[] switchIdArray = cccObj
												.getSubAction().split(",");
										for (int i = 0; i < switchIdArray.length; i++) {
											emManager.setAutoBySwitchId(Long.parseLong(switchIdArray[i]));
										}
									}
									break;
								default:
									// Do nothing is selected
									break;
								}
							} else {
								// Go to All Auto
								//emManager.setAllAuto();
							}
						}

					}
				}
			}
		}
		if (bUpdate) {
			updateContactClosureControlList(contactClosureVo);
		}
	}
	
	public void addPercentageContactClosureControl(){
		ContactClosure contactClosure = getCCDataFromDB();
		for(ContactClosureControls contactClosureControls : contactClosure.getContactClosureVo().get(0).getContactClosureControlsList()){
			if((contactClosureControls.getAction() == 1 && contactClosureControls.getPercentage() == 0)|| (contactClosureControls.getAction() == 2 && contactClosureControls.getPercentage() == 0)){
				contactClosureControls.setPercentage(100);
			}//else{
				//contactClosureControls.setPercentage(0);
			//}
		}
		saveCCDataToDB(contactClosure);
	}
}
