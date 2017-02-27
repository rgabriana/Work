package com.ems.ws;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.ems.model.SystemConfiguration;
import com.ems.security.exception.EmsValidationException;
import com.ems.server.device.DeviceServiceImpl;
import com.ems.service.FixtureManager;
import com.ems.service.SystemConfigurationManager;
import com.ems.types.TemperatureType;
import com.ems.utils.CommonUtils;
import com.ems.vo.TTLConfiguration;
import com.ems.ws.util.Response;

@Controller
@Path("/systemconfig")
public class SystemConfigurationService {
	
	@Resource(name = "systemConfigurationManager")
    private SystemConfigurationManager systemConfigurationManager;
	
	@Resource(name = "fixtureManager")
    private FixtureManager fixtureManager;
	
	@Autowired
	private MessageSource messageSource;
	
	private static final Logger m_Logger = Logger.getLogger("WSLogger");
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	
	@Path("edit")
    @POST
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response editSystemConfigValue(SystemConfiguration systemConfiguration) throws EmsValidationException {
		Response resp = new Response();
		if(systemConfiguration.getName()!=null && systemConfiguration.getValue()!=null){
			Map<String,Object> nameValMap = new HashMap<String,Object>();
			nameValMap.put("systemConfigName", systemConfiguration.getName());
			nameValMap.put("systemConfigValue", systemConfiguration.getValue());
			resp = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, nameValMap);
			if(resp!= null && resp.getStatus()!=200){
				m_Logger.error("Validation error "+resp.getMsg());
				return resp;
			}
			if("auth.auth_type".equals(systemConfiguration.getName())){
				if(!systemConfiguration.getValue().equals("DATABASE") && !systemConfiguration.getValue().equals("LDAP")){
					m_Logger.error("Invalid value for "+ systemConfiguration.getName() + ", Allowed values are DATABASE or LDAP");
					throw new IllegalArgumentException("Invalid value for " + systemConfiguration.getName());
				}
			} else if("temperature_unit".equals(systemConfiguration.getName())){
				if(!systemConfiguration.getValue().equals("F") && !systemConfiguration.getValue().equals("C")){
					m_Logger.error("Invalid value for "+ systemConfiguration.getName() + ", Allowed values are F or C");
					throw new IllegalArgumentException("Invalid value for " + systemConfiguration.getName());
				}
			}
			String[] sysConfRestArray = {"upgrade.su_app_pattern","upgrade.su_firm_pattern","upgrade.gw_app_pattern","upgrade.gw_firm_pattern","upgrade.su_20_pattern","upgrade.cu_20_pattern","upgrade.sw_20_pattern","upgrade.su_ble_pattern","upgrade.su_pyc_pattern","upgrade.gw_pyc_pattern","imageUpgrade.test_file","rest.api.key","upgrade.gw_20_pattern","gems.version.build","ec.scaling.for.277v","ec.adj.for.277v","ec.adj.for.240v","breakroom_normal","closed corridor_normal","closed corridor_alwayson","open office_normal","open office_alwayson","open office_dim","private office_normal","highbay_normal","highbay_alwayson","conference room_normal","uem.enable","uem.ip","uem.apikey","uem.secretkey","iptables.rules.static.ports","em.UUID","emLicenseKeyValue","ec.scaling.for.110v","upgrade.plugload_pattern","apply.network.status","ec.adj.for.110v","ec.scaling.for.240v"};
			boolean retRestVal = Arrays.asList(sysConfRestArray).contains(systemConfiguration.getName());
			String[] sysConfigNamesArray = {"ec.apply.scaling.factor","dr.service_enabled","dhcp.enable","em.forcepasswordexpiry","flag.ems.apply.validation","menu.bacnet.show","menu.openADR.show","sweeptimer.enable","motionbits.enable","dashboard_sso","networksettings.isSetupReady","enable.softmetering","enable.emergencyfx.calc","show.cu.failure.in.outage.report","erc.batteryreportscheduler.enable","bacnet_config_enable","ssl.enabled","bulbconfiguration.enable","profileupgrade.enable","profileoverride.init.enable","enable.profilefeature","add.more.defaultprofile","enable.plugloadprofilefeature"};
			boolean retval = Arrays.asList(sysConfigNamesArray).contains(systemConfiguration.getName());
			String[] sysConfNumbersArray = {"default_utc_time_cmd_frequency","default_utc_time_cmd_offset","imageUpgrade.interBucketDelay","imageUpgrade.plcPacketSize","imageUpgrade.zigbeePacketSize","commandRetryDelay","commandNoOfRetries","perf.pmStatsMode","event.outageVolts","event.outageAmbLight","cmd.no_multicast_targets","discovery.retry_interval","discovery.max_no_install_sensors","discovery.max_time","cmd.multicast_inter_pkt_delay","cmd.pmstats_processing_threads","discovery.validationTargetAmbLight","db_pruning.5min_table","db_pruning.hourly_table","db_pruning.daily_table","fixture.default_voltage","plugload.default_voltage","db_pruning.events_and_fault_table","bacnet.vendor_id","bacnet.server_port","bacnet.network_id","bacnet.max_APDU_length","bacnet.APDU_timeout","bacnet.device_base_instance","fixture.sorting.path","db_pruning.emsaudit_table","commissioning.inactivity_timeout","perf.base_power_correction_percentage","event.outage_detect_percentage","event.fixture_outage_detect_watts","discovery.validationTargetRelAmbLight","discovery.validationMaxEnergyPercentReading","imageUpgrade.no_multicast_retransmits","dr.repeat_interval","imageUpgrade.default_fail_retries","imageUpgrade.no_test_runs","cmd.multicast_inter_pkt_delay_2","stats.temp_offset_1","default_su_hop_count","discovery.validationTargetRelAmbLight_2","pmstats_process_batch_time","db_pruning.em_stats_table","db_pruning.events_and_fault_history_table","db_pruning.events_and_fault_table_records","cmd.pmstats_queue_threshold","stats.temp_offset_2","db_pruning.ems_user_audit_table","db_pruning.ems_user_audit_history_table","default_hopper_tx_power","cloud.communicate.type","discovery.hopper_channel_change_no_of_retries","discovery.gw_wait_time_for_hoppers","imageUpgrade.interPacketDelay","imageUpgrade.no_multicast_targets","cmd.unicast_inter_pkt_delay","cmd.ack_dbupdate_threads","cmd.response_listener_threads","imageUpgrade.interPacketDelay_2","dr.minimum.polltimeinterval","cmd.pmstats_process_batch_size","enable.connexusfeature","floorplan.imagesize.limit","switch.initial_scene_active_time","switch.extend_scene_active_time","uem.pkt.forwarding.enable","enable.pricing","wds.normal.level.min","wds.low.level.min","enable.cloud.communication"};
			boolean allowNumericalVal = Arrays.asList(sysConfNumbersArray).contains(systemConfiguration.getName());
			if (retRestVal == true) {
				m_Logger.error("Not Permitted to change the value for "+ systemConfiguration.getName());
				throw new IllegalArgumentException("Not Permitted to change the value for " + systemConfiguration.getName());
			} else if (retval == true) {
				if(!systemConfiguration.getValue().equalsIgnoreCase("true") && !systemConfiguration.getValue().equalsIgnoreCase("false")){
					m_Logger.error("Invalid value for "+ systemConfiguration.getName() + ", Allowed values are true or false");
					throw new IllegalArgumentException("Invalid value for " + systemConfiguration.getName());
				}
			} else if (allowNumericalVal == true) {
				if(!StringUtils.isEmpty(systemConfiguration.getValue())){
					final Pattern digitPattern = Pattern.compile("\\d+");
					final Matcher matcher = digitPattern.matcher(String.valueOf(systemConfiguration.getValue()).trim());
					if (!matcher.matches()) {
						m_Logger.error("Only Numerical values allowed for "+ systemConfiguration.getName());
						throw new IllegalArgumentException("Only Numerical values allowed for " + systemConfiguration.getName());
					}
				}
			}
			
			SystemConfiguration sc = systemConfigurationManager.loadConfigByName(systemConfiguration.getName());
			if(sc != null && sc.getId() != null) {
				sc.setValue(systemConfiguration.getValue());
				systemConfigurationManager.save(sc);
			}
			else {
				systemConfigurationManager.save(systemConfiguration);
			}
			if("perf.pmStatsMode".equals(systemConfiguration.getName())){
				SystemConfiguration sc1 = systemConfigurationManager.loadConfigByName("enable.softmetering");
				if(systemConfiguration.getValue().equalsIgnoreCase("2"))
				{
					sc1.setValue("true");
				}else
				{
					sc1.setValue("false");
				}
				systemConfigurationManager.save(sc1);
			}
		} else {
			m_Logger.error("Name/Value is null");
			throw new IllegalArgumentException("Name/Value is null");
		}
		return resp;
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("details/{name}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public SystemConfiguration loadConfigByName(@PathParam("name") String name) {
		
		return systemConfigurationManager.loadConfigByName(name);
		
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin','Auditor','Employee')")
	@Path("gettemperaturetype")
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Response getFixtureCount() {
		Response oResponse = new Response();
		SystemConfiguration temperatureConfig = systemConfigurationManager.loadConfigByName("temperature_unit");
		String dbTemp;
		if(temperatureConfig!=null)
		{
		dbTemp = temperatureConfig.getValue();
		if(dbTemp.equalsIgnoreCase(TemperatureType.F.getName()))
		{
			oResponse.setMsg(TemperatureType.F.getName());
		}
		else if(dbTemp.equalsIgnoreCase(TemperatureType.C.getName()))
		{
			oResponse.setMsg(TemperatureType.C.getName());
		}
		}
		return oResponse;		
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("updatefloorplansize/{imagesize}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateFloorPlanImageSize(@PathParam("imagesize") String imagesize) {
		Response response = new Response();
		response = CommonUtils.isParamValueAllowed(messageSource, systemConfigurationManager, "systemConfigImageSizeValue",imagesize);
		if(response!= null && response.getStatus()!=200){		
			m_Logger.error("Validation error "+response.getMsg());
			return response;
		}
		
		SystemConfiguration sysConfig  = systemConfigurationManager.loadConfigByName("floorplan.imagesize.limit");
		sysConfig.setValue(imagesize);
		systemConfigurationManager.update(sysConfig);		
		return response;
	}
	
	@PreAuthorize("hasAnyRole('Admin','FacilitiesAdmin')")
	@Path("updatettlvalue")
    @POST
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public Response updateTTLValue(List<TTLConfiguration> ttl) {
		Response response = new Response();
		SystemConfiguration ttlConfiguration = systemConfigurationManager
				.loadConfigByName("ttl_configuration_map");		
		Map<String,Integer> map = new HashMap<String,Integer>();
		for(TTLConfiguration ttlConfig : ttl){
			map.put(ttlConfig.getModelNo(), ttlConfig.getHopCount());
		}		
		ObjectMapper mapper = new ObjectMapper();
		try {
			String s = mapper.writeValueAsString(map);
			//System.out.println(s);
			if(ttlConfiguration != null){				
				ttlConfiguration.setValue(s);
				systemConfigurationManager.save(ttlConfiguration);
			}else{
				ttlConfiguration = new SystemConfiguration();
				ttlConfiguration.setName("ttl_configuration_map");
				ttlConfiguration.setValue(s);
				systemConfigurationManager.save(ttlConfiguration);				
			}
			DeviceServiceImpl.getInstance().setTTLValues(s);
			fixtureManager.sendTTLToHoppers();
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
		return response;
	}
		
}
	
