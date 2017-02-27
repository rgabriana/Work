<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<spring:url value="/services/systemconfig/edit" var="editSystemConfigUrl" scope="request" />
<spring:url value="/services/systemconfig/details" var="checkForSystemConfigNameUrl" scope="request" />
<style>

#systemConfigTable table {
	border: thin dotted #7e7e7e;
	padding: 10px;
}

#systemConfigTable th {
	text-align: right;
	vertical-align: top;
	padding-right: 10px;
}

#systemConfigTable td {
	vertical-align: top;
	padding-top: 2px;
}

#center {
  height : 95% !important;
}
#systemConfigTable .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}
</style>


<script type="text/javascript">

var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";

$(document).ready(function() {
	clearLabelMessage() ;
	
	
});


function displayLabelMessage(Message, Color) {
		$("#error").html(Message);
		$("#error").css("color", Color);
}
function clearLabelMessage() {
	 $("#error").html("");
	 $("#error").css("color", COLOR_DEFAULT);
}

function closeSystemConfigDialog(){
	$("#editSystemConfigDialog").dialog("close");
}

function validateSystemConfigForm(){
	clearLabelMessage();
	var isValid = true;
	var configName = $("#name").val();
	var regExpStr = /^[A-Za-z0-9\s\-\_\.]+$/i;
	if ( configName.trim() == ''){
		displayLabelMessage("Name Field should not be empty", COLOR_FAILURE);
		return false;
	} else {
		if(regExpStr.test(configName) == false) {
			displayLabelMessage("Name is invalid", COLOR_FAILURE);
		   	$("#name").addClass("invalidField");
		   	return false;   	
		}
	}
	var configValue = $("#value").val();
	var regExpStrConfig = /^[A-Za-z0-9\s\-\_\.\,\:\{\}\"\@]+$/i;
	if (configValue.trim() == ''){
		displayLabelMessage("Value Field should not be empty", COLOR_FAILURE);
		return false;
	} else {
		if(configValue.trim() == 'null'){
			displayLabelMessage("Value Field should not be null", COLOR_FAILURE);
			return false;
		}
		if(regExpStrConfig.test(configValue) == false) {
			displayLabelMessage("Invalid Value", COLOR_FAILURE);
		   	$("#value").addClass("invalidField");
		   	return false;   	
		}
	}
	isValid = isConfigValueAllowed(configName, configValue);
	if(isValid){
		checkForSystemConfigName();
	}
}

function isConfigValueAllowed(configName, configValue){
	var allowed = true;
	if(configName == "auth.auth_type"){
		if(configValue!="DATABASE" && configValue!="LDAP"){
			allowed =  false;
			displayLabelMessage("Value is invalid", COLOR_FAILURE);
		   	$("#value").addClass("invalidField");
		   	return false;
		}
	} else if(configName == "temperature_unit"){
		if(configValue!="F" && configValue!="C"){
			allowed =  false;
			displayLabelMessage("Value is invalid", COLOR_FAILURE);
		   	$("#value").addClass("invalidField");
		   	return false;
		}
	}
	var sysConfRestArray = ["upgrade.su_app_pattern","upgrade.su_firm_pattern","upgrade.gw_app_pattern","upgrade.gw_firm_pattern","upgrade.su_20_pattern","upgrade.cu_20_pattern","upgrade.sw_20_pattern","upgrade.su_ble_pattern","upgrade.su_pyc_pattern","upgrade.gw_pyc_pattern","imageUpgrade.test_file","rest.api.key","upgrade.gw_20_pattern","gems.version.build","ec.scaling.for.277v","ec.adj.for.277v","ec.adj.for.240v","breakroom_normal","closed corridor_normal","closed corridor_alwayson","open office_normal","open office_alwayson","open office_dim","private office_normal","highbay_normal","highbay_alwayson","conference room_normal","uem.enable","uem.ip","uem.apikey","uem.secretkey","iptables.rules.static.ports","em.UUID","emLicenseKeyValue","ec.scaling.for.110v","upgrade.plugload_pattern","apply.network.status","ec.adj.for.110v","ec.scaling.for.240v"];
	var sysConfigNamesArray = ["ec.apply.scaling.factor","dr.service_enabled","dhcp.enable","em.forcepasswordexpiry","flag.ems.apply.validation","menu.bacnet.show","menu.openADR.show","sweeptimer.enable","motionbits.enable","dashboard_sso","networksettings.isSetupReady","enable.softmetering","enable.emergencyfx.calc","show.cu.failure.in.outage.report","erc.batteryreportscheduler.enable","bacnet_config_enable","ssl.enabled","bulbconfiguration.enable","profileupgrade.enable","profileoverride.init.enable","enable.profilefeature","add.more.defaultprofile","enable.plugloadprofilefeature"];
	var sysConfNumbersArray = ["default_utc_time_cmd_frequency","default_utc_time_cmd_offset","imageUpgrade.interBucketDelay","imageUpgrade.plcPacketSize","imageUpgrade.zigbeePacketSize","commandRetryDelay","commandNoOfRetries","perf.pmStatsMode","event.outageVolts","event.outageAmbLight","cmd.no_multicast_targets","discovery.retry_interval","discovery.max_no_install_sensors","discovery.max_time","cmd.multicast_inter_pkt_delay","cmd.pmstats_processing_threads","discovery.validationTargetAmbLight","db_pruning.5min_table","db_pruning.hourly_table","db_pruning.daily_table","fixture.default_voltage","plugload.default_voltage","db_pruning.events_and_fault_table","bacnet.vendor_id","bacnet.server_port","bacnet.network_id","bacnet.max_APDU_length","bacnet.APDU_timeout","bacnet.device_base_instance","fixture.sorting.path","db_pruning.emsaudit_table","commissioning.inactivity_timeout","perf.base_power_correction_percentage","event.outage_detect_percentage","event.fixture_outage_detect_watts","discovery.validationTargetRelAmbLight","discovery.validationMaxEnergyPercentReading","imageUpgrade.no_multicast_retransmits","dr.repeat_interval","imageUpgrade.default_fail_retries","imageUpgrade.no_test_runs","cmd.multicast_inter_pkt_delay_2","stats.temp_offset_1","default_su_hop_count","discovery.validationTargetRelAmbLight_2","pmstats_process_batch_time","db_pruning.em_stats_table","db_pruning.events_and_fault_history_table","db_pruning.events_and_fault_table_records","cmd.pmstats_queue_threshold","stats.temp_offset_2","db_pruning.ems_user_audit_table","db_pruning.ems_user_audit_history_table","default_hopper_tx_power","cloud.communicate.type","discovery.hopper_channel_change_no_of_retries","discovery.gw_wait_time_for_hoppers","imageUpgrade.interPacketDelay","imageUpgrade.no_multicast_targets","cmd.unicast_inter_pkt_delay","cmd.ack_dbupdate_threads","cmd.response_listener_threads","imageUpgrade.interPacketDelay_2","dr.minimum.polltimeinterval","cmd.pmstats_process_batch_size","enable.connexusfeature","floorplan.imagesize.limit","switch.initial_scene_active_time","switch.extend_scene_active_time","uem.pkt.forwarding.enable","enable.pricing","wds.normal.level.min","wds.low.level.min","enable.cloud.communication"];
	
	var isRestNamePresentInArray = sysConfRestArray.indexOf(configName);
	var isNamePresentInArray = sysConfigNamesArray.indexOf(configName);
	var onlyDigitNameInArray = sysConfNumbersArray.indexOf(configName);
	
    if(isRestNamePresentInArray > -1){
    	allowed =  false;
		displayLabelMessage("Invalid Value", COLOR_FAILURE);
	   	$("#name").addClass("invalidField");
	   	return false;
    } else if(isNamePresentInArray > -1){
    	var areEqual = configValue.toUpperCase() === "true".toUpperCase();
    	var areEqual1 = configValue.toUpperCase() === "false".toUpperCase();
    	if(!areEqual && !areEqual1){
			allowed =  false;
			displayLabelMessage("Invalid Value", COLOR_FAILURE);
		   	$("#value").addClass("invalidField");
		   	return false;
		}
    } else if(onlyDigitNameInArray > -1){
    	var numericalRegExpStr = /^[0-9]+$/i;
    	if(numericalRegExpStr.test(configValue) == false) {
			displayLabelMessage("Invalid Value", COLOR_FAILURE);
		   	$("#value").addClass("invalidField");
		   	return false;   	
		}
    }
    return allowed;
}

var systemConfigXML = "";

function checkForSystemConfigName(){
	
	$.ajax({
		type: 'POST',
		url: "${checkForSystemConfigNameUrl}"+"/"+$('#name').val().trim()+"?ts="+new Date().getTime(),
		success: function(data){
			if(data == null){
				displayLabelMessage("System Config with the below name doesn't exists", COLOR_FAILURE);
			}else{
				editSystemConfig(data.id);
			}
		},
		dataType:"json",
		contentType: "application/xml; charset=utf-8"
	});
	
}

function editSystemConfig(id){
	
	displayLabelMessage("",COLOR_DEFAULT),
	
	systemConfigXML = "<systemConfiguration>"+
	"<id>"+id+"</id>"+
	"<name>"+$('#name').val().trim()+"</name>"+
	"<value>"+$('#value').val().trim()+"</value>"+
	"</systemConfiguration>";
	
	$.ajax({
			data: systemConfigXML,
			type: "POST",
			url: "${editSystemConfigUrl}"+"?ts="+new Date().getTime(),
			success: function(data){
				displayLabelMessage("System Configuration value is successfully saved", COLOR_SUCCESS);
			},
			error: function(){
				displayLabelMessage("Error.System Configuration value is not saved", COLOR_FAILURE);
			},
			dataType:"json",
			contentType : "application/xml; charset=utf-8"
		});
	
}

function clearMessage(id){
	 $("#"+id).removeClass("invalidField");
}

</script>
<div style="clear: both;"><span id="error"></span></div>
<div style="margin:10px 0px 0px 20px;">
<div style="clear: both;"><span id="message">*In the Name Filed,Please enter name of the System Configuration value you want to edit </span></div>
<table id="systemConfigTable">
	<tr>
		<th >Name:</th>
		<td ><input id="name" name="name" onkeypress="clearMessage(this.id);"/>
		</td>
	</tr>
	
	<tr>
		<th>Value:</th>
		<td ><input id="value" name="value" onkeypress="clearMessage(this.id);"/>
		</td>
	</tr>
	<tr>
		<th><span></span></th>
		<td>
			<button type="button" onclick="validateSystemConfigForm();">
				Save
			</button>&nbsp;
			<input type="button" id="btnClose"
				value="<spring:message code="action.cancel" />" onclick="closeSystemConfigDialog()">
		</td>
	</tr>
</table>
</div>