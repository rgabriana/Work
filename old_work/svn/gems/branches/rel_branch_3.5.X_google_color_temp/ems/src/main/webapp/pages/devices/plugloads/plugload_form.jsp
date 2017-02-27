<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<spring:url value="/devices/plugloads/updatePlugload.ems"
	var="updatePlugloadUrl" scope="request" />
<spring:url	value="/services/org/plugload/reboot/" var="rebootplugloadurl" scope="request" />
<spring:url value="/services/org/plugload/op/mode/" var="autoplugloadUrl"
	scope="request" />
<spring:url value="/services/org/plugload/switchrunningplugloadimage/"
	var="switchImagePlugloadUrl" scope="request" />
<spring:url value="/services/org/plugload/checkswitchrunningplugloadimagestatus/"
	var="checkSwitchRunningPlugloadImageStatusUrl" scope="request" />
<spring:url value="/services/org/plugload/op/turnOnOff/" var="onoffplugloadUrl"
	scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Plugload Details</title>


<script type="text/javascript">
//Constants 
var COMMUNICATION_TYPE_ONE = "Wireless";
var COMMUNICATION_TYPE_TWO = "Powerline";
var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
var restoreStatusAttempt = 0;
var orgSliderValue = 0;

var PLUGLOAD_ORIGINAL_PROFILE_BEFORE_UPDATE = "";
var PLUGLOAD_CURRENT_PROFILE_BEFORE_UPDATE = "";
var PLUGLOAD_PROFILE_DATA = {}; //JSON Object: key=profile_name, value=profile_id

var groupNameList = [];
var currentGroupListIndex = 0;

$(document).ready(function() {
	
	loadFormValues();
	
	loadAllPlugloadProfiles();
	
	PLUGLOAD_ORIGINAL_PROFILE_BEFORE_UPDATE = "${originalPlugloadProfileFrom}";
	
	PLUGLOAD_CURRENT_PROFILE_BEFORE_UPDATE = "${currentPlugloadProfile}";
	
	$('#enableHopper').click(function() {
		$('#isHopper').val(this.checked?"1":"0");
	});
	$("#currentstate").html(getStateString("${plugload.avgVolts}"));
	//var temperature = '<c:out value="${plugload.avgTemperature}"/>';	
	//$("#avgTemperature").html(getTemperatureString(temperature));
	var currUptimePl = '<c:out value="${gateway.currUptime}"/>';

	$("#gw_currUptimePl").text(getLastCommunicationsStringPlugload(currUptimePl).replace("ago", ""));

});

function getTemperatureString(iTemperature)
{	
	var temperatureUnit = "${temperatureunit}";	
	if(!temperatureUnit)
	{
		temperatureUnit = "F";
		
	}
	if(temperatureUnit=="C")
	{
		var Celcius=(iTemperature -32) * 5 / 9;
		Celcius = Celcius.toFixed(1);
		if(iTemperature > 82)
			return "Hot (" + Celcius + "&deg;C)";
		else if(iTemperature > 75)
			return "Warm (" + Celcius + "&deg;C)";
		else if(iTemperature > 68)
			return "Normal (" + Celcius + "&deg;C)";
		else if(iTemperature > 65)
			return "Cool (" + Celcius + "&deg;C)";
		else
			return "Cold (" + Celcius + "&deg;C)";	
	}
	else
	{
		if(iTemperature > 82)
			return "Hot (" + iTemperature + "&deg;F)";
		else if(iTemperature > 75)
			return "Warm (" + iTemperature + "&deg;F)";
		else if(iTemperature > 68)
			return "Normal (" + iTemperature + "&deg;F)";
		else if(iTemperature > 65)
			return "Cool (" + iTemperature + "&deg;F)";
		else
			return "Cold (" + iTemperature + "&deg;F)";		
	}
}


function plugloadReboot()
{	
	var plugloadId = "${plugload.id}";	
	var proceed = confirm("Are you sure you want to reboot the plugload?");
	if(proceed){
		$.ajax({
			type: 'GET',
			url: "${rebootplugloadurl}"+plugloadId+"?ts="+new Date().getTime(),			
			success: function(data){
				alert("Service call success");
			},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8"
		});
	}
}

function getLastCommunicationsStringPlugload(strOccValue) {	
	if (strOccValue <= 0)
		return "0 sec ago";

	var numyears = Math.floor(strOccValue / 31536000);
	var numdays = Math.floor((strOccValue % 31536000) / 86400);
	var numhours = Math.floor(((strOccValue % 31536000) % 86400) / 3600);
	var numminutes = Math.floor((((strOccValue % 31536000) % 86400) % 3600) / 60);
	var numseconds = (((strOccValue % 31536000) % 86400) % 3600) % 60;
	
	if (numdays > 0) {
		if (numhours > 0){
			return numdays + " days, " 	+ numhours + " hrs ago";
		} else {
			return numdays + " days ago";
		}
	} else if (numhours > 0) {
		if (numminutes > 0){
			return numhours + " hrs, " 	+ numminutes + " min ago";
		}
		else{
			return numhours + " hrs ago";
	    }
	} else if (numminutes > 0) {
		if (numseconds > 0)
			return numminutes + " min, " + numseconds + " sec ago";
		else
			return numminutes + " min ago";
	} else {
		return numseconds + " sec ago";
	}

}


function loadFormValues(){
	clearPlugloadLabelMessage();
	var isHopper = '<c:out value="${plugload.isHopper}"/>';
	/*var isState = '<c:out value="${plugload.avgTemperature}"/>';*/
	$("#enableHopper").attr('checked', isHopper==1);
	var cnt = 0;
	<c:forEach items="${groupList}" var="groupName">
		groupNameList[cnt] = "${groupName}";
		cnt++;
	</c:forEach>
	
	if(groupNameList.length >= 1)
		$("#groupName1").val(groupNameList[0]);
	if(groupNameList.length >= 2)
		$("#groupName2").val(groupNameList[1]);
	if(groupNameList.length >= 3)
		$("#groupName3").val(groupNameList[2]);
	if(groupNameList.length >= 4)
		$("#groupName4").val(groupNameList[3]);
	if(groupNameList.length >= 5)
		$("#groupName5").val(groupNameList[4]);
	}
function updateGroupNames(){
	for(var i = currentGroupListIndex, index = 0; ((i < groupNameList.length) || (index < 5)); i++, index++) {
		var idStr = "groupName" + (index + 1);
		$("#"+idStr).val(groupNameList[i]);
	}
}
$('#leftarrowbtn').click(function() {
	if(currentGroupListIndex == 0)
		return;
	
	currentGroupListIndex--;
	updateGroupNames();
	
});
$('#rightarrowbtn').click(function() {
	if(currentGroupListIndex >= (groupNameList.length-5))
		return;
	
	currentGroupListIndex++;
	updateGroupNames();
	
});
function loadAllPlugloadProfiles(){
	PLUGLOAD_PROFILE_DATA = {};
	<c:forEach items="${groups}" var="group">
	PLUGLOAD_PROFILE_DATA["${group.name}"] = "${group.id}";
	</c:forEach>
}

function setCurrentState(onOffStateValue) {
	$("#currentstate").html((onOffStateValue<=0?"OFF":"ON"));
}

function setPlDimmerOnAuto(){
	clearPlugloadLabelMessage();
	$('input[name="currentState"]').prop('checked', false);
	$("#currentstate").html("AUTO");
	$.ajax({
		type: 'POST',
		url: "${autoplugloadUrl}AUTO",
		data: "<plugloads><plugload><id>${plugload.id}</id></plugload></plugloads>",
		success: function(data){
				},
		dataType:"xml",
		contentType: "application/xml; charset=utf-8",
	});
	
}

function setOnOffPlugloads(){
	var lightControlMinutes = $("#lightControlMinutes").val();
	var onoffState = $("input[name=currentState]:checked").val();
	if(onoffState==undefined || onoffState=="undefined"){
		displayPlugloadLabelMessage('<spring:message code="plugloadForm.message.validation.undefined"/>', COLOR_FAILURE);
		return;
	}
	if(lightControlMinutes == "") {
		displayPlugloadLabelMessage('<spring:message code="fixtureForm.message.validation.time_blank"/>', COLOR_FAILURE);
		
		return;
	}else if(!(/^([0-9]+)$/).test(lightControlMinutes)){
		displayPlugloadLabelMessage("Manual Override Time should be a Number", COLOR_FAILURE);
		return;
	}else if(lightControlMinutes > 99999){
		displayPlugloadLabelMessage("Manual Override Time should be less than or equal to 99999", COLOR_FAILURE);
		return;
	}
	else if(1*lightControlMinutes == 0) {
		displayPlugloadLabelMessage('<spring:message code="fixtureForm.message.validation.time_zero"/>', COLOR_FAILURE);
		return;
	}else {
		clearPlugloadLabelMessage();
		setCurrentState(onoffState);
		$.ajax({
			type: 'POST',
			url: "${onoffplugloadUrl}"+onoffState+"/"+(1*lightControlMinutes),
			data: "<plugloads><plugload><id>${plugload.id}</id></plugload></plugloads>",
			success: function(data){
					},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8",
		});
	}
}

function getSelectedPlugloadProfile(id)
{
	var profile=null;
	for (var key in PLUGLOAD_PROFILE_DATA)
	{
	   if (PLUGLOAD_PROFILE_DATA.hasOwnProperty(key))
	   {
		   var groupId = PLUGLOAD_PROFILE_DATA[key];
		   if(groupId ==id)
		   {
			   profile = key;
			    break;
		   }
	   }
	}
	return profile;
}

function getMACAddress(macAddress)
{
    var strMacAddress = "";
    
    if(macAddress.indexOf(":") <= 0)
    {
        while(macAddress.length > 0)
        {
            if(macAddress.length >= 3)
            {
                strMacAddress += macAddress.substr(0,2) + ":";
                macAddress = macAddress.substr(2);
            }
            else
            {
                strMacAddress += macAddress.substr(0,2);
                macAddress = macAddress.substr(2);
            }
        }
    }
    else
    {
    	var macSplitArray;
    	macSplitArray = macAddress.split(':');
    	for (var i = 0; i < macSplitArray.length; i++) {
    	    if(macSplitArray[i].length == 1){
    	    	macSplitArray[i] = "0"+macSplitArray[i];
    	    }
    	    
    	    if(i == 0){
    			strMacAddress = macSplitArray[i];
    		}else{
    			strMacAddress = strMacAddress + ":" + macSplitArray[i];
    		}
    	    
    	}
       // strMacAddress = macAddress;
    }
    return strMacAddress;
}

var plugloadGroupId = "";

function savePlugload(){
	var plugloadName = $("#name").val();
	plugloadName = $.trim(plugloadName);
	if(plugloadName == ""){
		displayPlugloadLabelMessage('<spring:message code="fixtureForm.message.validation.name"/>', COLOR_FAILURE);
		return false;
	} else {
		
		var new_current_plugloadprofile_id = $("#groupId").val();
		var new_current_plugloadprofile = getSelectedPlugloadProfile(new_current_plugloadprofile_id);
		if(plugloadGroupId == ""){
			plugloadGroupId = '<c:out value="${plugload.groupId}"/>';
		}
		//get groupId from profile name
		//$("#groupId").val(new_current_plugloadprofile_id);
		if(plugloadGroupId != new_current_plugloadprofile_id){
			//set originalProfile as current profile
			$("#originalProfileFrom").val(PLUGLOAD_CURRENT_PROFILE_BEFORE_UPDATE);
			
			$("#currentProfile").val(new_current_plugloadprofile);
			PLUGLOAD_ORIGINAL_PROFILE_BEFORE_UPDATE = PLUGLOAD_CURRENT_PROFILE_BEFORE_UPDATE;
			PLUGLOAD_CURRENT_PROFILE_BEFORE_UPDATE = new_current_plugloadprofile;
			plugloadGroupId = new_current_plugloadprofile_id;
		}
		
		displayPlugloadLabelMessage('<spring:message code="plugloadForm.message.waiting"/>', COLOR_DEFAULT);
		
		$.post(
			"${updatePlugloadUrl}"+"?ts="+new Date().getTime(),
			$("#plugload-form").serialize(),
			function(data){
				var response = eval("("+data+")");
				if(response.success==1){ //Success
					displayPlugloadLabelMessage(response.message, COLOR_SUCCESS);
				 	if(parent.parent.getFloorPlanObj("floorplan") != undefined || parent.parent.getFloorPlanObj("floorplan") != null){
						try {
							parent.parent.getFloorPlanObj("floorplan").plotChartRefresh();
						}catch(e) {
						}
					}
				} else { //Failure
					displayPlugloadLabelMessage(response.message, COLOR_FAILURE);
				}
			}
		);
		return true;
	}
}

function displayPlugloadLabelMessage(Message, Color) {
	$("#plugload_message").html(Message);
	$("#plugload_message").css("color", Color);
}

function displayPlugloadRestoreLabelMessage(Message, Color) {
	$("#restore_message").html(Message);
	$("#restore_message").css("color", Color);
}

function clearPlugloadLabelMessage(Message, Color) {
	displayPlugloadLabelMessage("", COLOR_DEFAULT);
}

function getStateString(state)
{
		if(state>0) return "ON";			
		return "OFF";
}

function confirmPlugloadRestore()
{
	if(confirm("Are you sure you want to boot to alternate version?"))
		switchPlugloadImage();
}

function switchPlugloadImage() {
	displayPlugloadRestoreLabelMessage(
			'<spring:message code="plugloadForm.message.restore.waiting"/>',
			COLOR_DEFAULT);
	$.ajax({
		type : 'POST',
		url : "${switchImagePlugloadUrl}",
		data : "<plugload><id>${plugload.id}</id></plugload>",
		success : function(data) {
			checkPlugloadImageRestoreSuccess();
		},
		dataType : "json",
		contentType : "application/xml; charset=utf-8",
	});
}

function checkPlugloadImageRestoreSuccess() {
	$.ajax({
		type : 'POST',
		url : "${checkSwitchRunningPlugloadImageStatusUrl}",
		data : "<plugload><id>${plugload.id}</id></plugload>",
		success : function(data) {
			var version = $("#currApp").val();
			if (version != data.msg) {
				displayPlugloadRestoreLabelMessage("Success", COLOR_SUCCESS);
				$("#currApp").val(data.msg);
				var alterversion = $("#currentFirmwareVersion").text();
				var runningversion = $("#appVersion").text();
				$("#currentFirmwareVersion").text(runningversion);
				$("#appVersion").text(alterversion);
			} else {
				if(restoreStatusAttempt > 60){
					displayPlugloadRestoreLabelMessage(
						"Failed to get reboot status from Plugload. Please check connectivity to Plugload.",
						COLOR_FAILURE);
				restoreStatusAttempt = 0;
				}else{
					restoreStatusAttempt++;
					setTimeout(checkPlugloadImageRestoreSuccess, 2000);
				}
			}
		},
		dataType : "json",
		contentType : "application/xml; charset=utf-8",
	});
}

</script>

<!-- JS function required for plugload Form -->
<script type="text/javascript">


	
	
</script>

<style>
#plugload-dialog-form {
	font-size: 12px;
	padding: 10px;
}

#plugload-dialog-form input.text {
	width: 96%;
}

#plugload-dialog-form hr {
	margin-top: 5px;
	margin-bottom: 5px;
}

#plugload-dialog-form fieldset {
	float: left;
	padding: 0;
	border: 0;
}

#plugload-dialog-form fieldset.column33 {
	width: 33%;
	background: none;
}

#plugload-dialog-form fieldset.column50 {
	width: 50%;
}

#plugload-dialog-form fieldset.column66 {
	width: 66%;
}

#plugload-dialog-form div.fieldWrapper {
	clear: both;
	padding: 5px;
	height: 15px;
}

#plugload-dialog-form div.fieldLabel {
	float: left;
	width: 160px; /*width:40%;*/
	font-weight: bold;
}

#plugload-dialog-form div.fieldValue {
	float: left;
	width: 49%;
}

#plugload-dialog-form fieldset.longlabel div.fieldLabel {
	width: 50%;
}
/*  	#plugload-dialog-form fieldset.longlabel div.fieldValue{float:left; width:49%;} */
#plugload-dialog-form div.fieldValue select {
	width: 95%;
	height: 100%;
}

#plugload-dialog-form div.fieldValue input {
	width: 95%;
	height: 100%;
}

#plugload-dialog-form div.sliderWrapper {
	margin-top: 10px;
}

#plugload-dialog-form div.sliderWrapper div {
	display: inline;
}

#plugload-dialog-form div.sliderDiv div {
	border: thin solid #AAAAAA;
	padding: 8px;
}
</style>


<style>
/*Override JQuery Dialog modal background css */
.ui-widget-overlay {
	background: none repeat scroll 50% 50% #000000;
	opacity: 0.9;
}

/*Fix for missing border of JQuery Slider panel */
#plugload-dialog-form .ui-widget-content {
	border: 1px solid #888888 !important;
}
</style>

</head>
<body>
	<div id="plugload-dialog-form">
		<spring:url value="/plugload/updatePlugload.ems" var="actionURL"
			scope="request" />
		<div style="padding-bottom: 5px;">
			<button id="updateButton"  onclick="javascript:return savePlugload();"
				style="float: left;">
				<spring:message code="fixtureForm.label.applyBtn" />
			</button>
			&nbsp;
			<div id="plugload_message"
				style="font-size: 14px; font-weight: bold; padding: 5px 0 0 10px; float: left;"></div>
			<br style="clear: both" />
		</div>
		<form:form id="plugload-form" commandName="plugload" method="post"
			action="${actionURL}" onsubmit="return false;">
			<form:hidden id="id" name="id" path="id" />
		<form:hidden id="isHopper" name="isHopper" path="isHopper" />
		<form:hidden id="currApp" name="currApp" path="currApp" />
		<form:hidden id="currentProfile" name="currentProfile" path="currentProfile" />
			<div class="upperdiv divtop">
				<fieldset class="column33"> 
				<div class="fieldWrapper">
					<div class="fieldLabel">
						Device Name	:
					</div>
					<div class="fieldValue">
						<form:input class="text" id="name" name="name"
							path="name" />
					</div>
				</div>
				<div class="fieldWrapper">
					<div class="fieldLabel">
						<spring:message code="plugloadForm.label.Location" />
						:
					</div>
					<div class="fieldValue">${plugload.location}</div>
				</div>
				<!-- </fieldset> -->
				<!-- <br style="clear:both"/> -->
				<!-- <fieldset class="column33"> -->
				<div class="fieldWrapper">
					<div class="fieldLabel">
						<spring:message code="plugloadForm.label.ID" />
						:
					</div>
					<div class="fieldValue">${plugload.id}</div>
				</div>
				<div class="fieldWrapper">
					<div class="fieldLabel">
						<spring:message code="plugloadForm.label.Floorplanposition" />
					</div>
					<div class="fieldValue">${plugload.xaxis},&nbsp;${plugload.yaxis}</div>
				</div>
				<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="plugloadForm.label.ModelNo" />
							:
						</div>
						<div class="fieldValue">${plugload.modelNo}</div>
					</div>
			</fieldset>
			<fieldset class="column33">
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="plugloadForm.label.GatewayName" />
							:
						</div>
						<div class="fieldValue">${gateway.name}</div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="plugloadForm.label.lastCommunication" />
							:
						</div>
						<div class="fieldValue">
							<ems:breakDateDiffInString
								dateValue="${plugload.lastConnectivityAt}"
								datePattern="yyyy-MM-dd HH:mm:ss" />
						</div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="plugloadForm.label.uptime" />
							:
						</div>
						<div id="gw_currUptimePl" class="fieldValue"></div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="plugloadForm.label.radioChannelID" />
							:
						</div>
						<div class="fieldValue">${gateway.channel}</div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="plugloadForm.label.radioNetworkID" />
							:
						</div>
						<div class="fieldValue">${hexWirelessNetworkId}</div>
					</div>
				</fieldset>
			</div>		
			<br style="clear: both" />
			<div class="upperdiv">
			<fieldset class="column33">
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="plugloadForm.label.Currentstate" />
							:
						</div>
						<div class="fieldValue" id="currentstate"></div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							Mac Address
							:
						</div>
						<div class="fieldValue" id="snapAddress">${plugload.snapAddress}</div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							Mode		:
						</div>
						<div class="fieldValue" id="mode">${plugload.currentState}</div>
					</div>
<!-- 					<div class="fieldWrapper"> -->
<!-- 						<div class="fieldLabel"> -->
<%-- 							<spring:message code="plugloadForm.label.Temperature" /> --%>
<!-- 							: -->
<!-- 						</div> -->
<!-- 						<div class="fieldValue" id="avgTemperature"></div> -->
<!-- 					</div> -->
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="plugloadForm.label.Cuversion" />
							:
						</div>
						<div class="fieldValue">${plugload.cuVersion}</div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="plugloadForm.label.Originalprofilefrom" />
							:
						</div>
						<div class="fieldValue">
							<form:input class="text" id="originalProfileFrom" name="originalProfileFrom"
								path="originalProfileFrom" readonly="true"/>
						</div>
					</div>
				</fieldset>
				<fieldset class="column33 longlabel">
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="plugloadForm.label.currentPowerM" />
							:
						</div>
						<div class="fieldValue" id="currentPowerUsage">${plugload.managedLoad}&nbsp;watts</div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="plugloadForm.label.currentPowerUM" />
							:
						</div>
						<div class="fieldValue" id="currentPowerUsage">${plugload.unmanagedLoad}&nbsp;watts</div>
					</div>					
<!-- 					<div class="fieldWrapper"> -->
<!-- 						<div class="fieldLabel"> -->
<!-- 							Communication Type : -->
<!-- 						</div> -->
<!-- 						<div class="fieldValue" id="communicationType"></div> -->
<!-- 					</div> -->
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="plugloadForm.label.Bootloaderversion" />
							:
						</div>
						<div class="fieldValue" id="bootloaderVersion">${plugload.bootLoaderVersion}</div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="plugloadForm.label.Voltage" />
							:
						</div>
						<div class="fieldValue">
							<label id="VoltageLabel">${plugload.voltage}</label>
						</div>
					</div>		
					<div class="fieldWrapper">
					<div class="fieldLabel">
							<spring:message code="plugloadForm.label.Currentprofile" />
							:
						</div>
						<div class="fieldValue">
							<form:select id="groupId" name="groupId"
								path="groupId" class="text">
								<form:options items="${groups}" itemValue="id"
									itemLabel="name" />
							</form:select>
						</div>
					</div>
										
				</fieldset>
					<fieldset class="column33">
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="plugloadForm.label.Applicationversion" />
							:
						</div>
						<div id="appVersion" name="appVersion" class="fieldValue">${plugload.version}</div>
					</div>	
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="plugloadForm.label.Firmwareversion" />
							:
						</div>
						<div class="fieldValue" id="currentFirmwareVersion">${plugload.firmwareVersion}</div>
					</div>								
					<div class="fieldWrapper">
						<div class="fieldLabel">
							Latest Communication :
						</div>
						<div class="fieldValue">
							<ems:breakDateDiffInString
								dateValue="${plugload.lastConnectivityAt}"
								datePattern="yyyy-MM-dd HH:mm:ss" />
						</div>
					</div>
								
					<div class="fieldWrapper">
						<div >
							<input type="checkbox" name="enableHopper" id="enableHopper" 
								style="width: 12px;" /> <label for="enableHopper"
								style="font-weight: bold;"><spring:message
									code="fixtureForm.label.EnableHopper" /></label>
						</div>
					</div>
				</fieldset>
				<div class="fieldWrapper">
					<div class="fieldLabel">
						<spring:message code="plugloadForm.label.Groups" />
						:
					</div>
					<div>
						<input type="image" id="leftarrowbtn" align="top" src="../themes/default/images/leftArrowColor.png"/> 
						<input type="text" id="groupName1" name="groupName1" style="text-align:center;" readonly/>
						<input type="text" id="groupName2" name="groupName2" style="text-align:center;" readonly/>
						<input type="text" id="groupName3" name="groupName3" style="text-align:center;" readonly/>
						<input type="text" id="groupName4" name="groupName4" style="text-align:center;" readonly/>
						<input type="text" id="groupName5" name="groupName5" style="text-align:center;" readonly/>
						<input type="image" id="rightarrowbtn" align="top" src="../themes/default/images/rightArrowColor.png"/> 
					</div>
				</div>
					<br style="clear: both" />
			</div>	
			<br style="clear: both" />
			<div class="upperdiv" style="padding-left: 5px;">
				<table cellspacing="0" cellpadding="0" >
					<tr>
						<td width=33%>
							<table cellspacing="0" cellpadding="0">
								<tr>
									<td><div class="fieldLabel">
											<spring:message code="plugloadForm.label.Mode" />
											:&nbsp;
										</div></td>
									<td>&nbsp;
										<button onclick="javascript:setPlDimmerOnAuto(); return false;">
											<spring:message code="plugloadForm.label.Auto" />
										</button>
									</td>
									
									<td>&nbsp; <span><spring:message
												code="plugloadForm.label.or" />:</span>&nbsp;
									</td>
									
									<td>
									<div class="upperdiv" style="padding-left: 5px;padding-right: 10px;">
										<table cellspacing="0" cellpadding="0">
											<tr>
												<td>&nbsp; <span style="font-weight: bold;"><spring:message
												code="plugloadForm.label.manual" />:</span>&nbsp;
									</td>
									
									<td>&nbsp;			
										<span><form:radiobutton path="currentState" label="On" value="100"/></span>
									</td>
									<td>&nbsp;
										<span><form:radiobutton path="currentState" label="Off" value="0"/></span>
									</td>
									
									<td>&nbsp; <span><spring:message
												code="plugloadForm.label.for" />:</span>&nbsp; <input
										id="lightControlMinutes" type="text" style="width: 30px;"
										value="60"></input>&nbsp; <span><spring:message
												code="plugloadForm.label.min" /></span>
									</td>
									<td>&nbsp;
										<button
											onclick="javascript:setOnOffPlugloads(); return false;">
											<spring:message code="plugloadForm.label.Apply" />
										</button>
									</td>
											</tr>
										</table>
									</div>
									</td>
								</tr>
							</table>
						 </td>
						<td width=33%></td>
					</tr>
				</table>
			</div>
			<br style="clear: both" />
			<div class="upperdiv" style="padding-left: 5px;">
				<table cellpadding="0" cellspacing="0">
						<tr>
							<td>
								<button id="restoreButton" onclick="javascript:return confirmPlugloadRestore();" >
									<spring:message code="fixtureForm.label.restoreBtn" />
								</button> Restore will boot the plugload with alternate version. &nbsp;
							</td>
							<td>
								<div id="restore_message"
									style="font-size: 14px; font-weight: bold; padding: 5px 0 0 10px;"></div>
							</td>
						</tr>
					</table>					
			</div>
			<br style="clear: both" />
			<div class="upperdiv" style="padding-left: 5px;">				
				<br style="clear: both" />
				<table cellpadding="0" cellspacing="0">
						<tr>
							<td>
								<button id="rebootButton" onclick="javascript:return plugloadReboot();">
									<spring:message code="plugloadForm.rebootBtn.name" />
								</button> Reboot. &nbsp;
							</td>
							<td>
								<div id="reboot_message"
									style="font-size: 14px; font-weight: bold; padding: 5px 0 0 10px;"></div>
							</td>
						</tr>
					</table>					
			</div>
		</form:form>
	</div>
</body>
</html>
