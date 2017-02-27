<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<spring:url value="/devices/plugloads/updatePlugload.ems"
	var="updatePlugloadUrl" scope="request" />


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

$(document).ready(function() {
	
	loadFormValues();
	
	loadAllPlugloadProfiles();
	
	PLUGLOAD_ORIGINAL_PROFILE_BEFORE_UPDATE = "${originalPlugloadProfileFrom}";
	
	PLUGLOAD_CURRENT_PROFILE_BEFORE_UPDATE = "${currentPlugloadProfile}";
	
	$('#enableHopper').click(function() {
		$('#isHopper').val(this.checked?"1":"0");
	});
});

function loadFormValues(){
	clearPlugloadLabelMessage();
	var isHopper = '<c:out value="${plugload.isHopper}"/>';
	$("#enableHopper").attr('checked', isHopper==1);
}

function loadAllPlugloadProfiles(){
	PLUGLOAD_PROFILE_DATA = {};
	<c:forEach items="${groups}" var="group">
	PLUGLOAD_PROFILE_DATA["${group.name}"] = "${group.id}";
	</c:forEach>
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

function clearPlugloadLabelMessage(Message, Color) {
	displayPlugloadLabelMessage("", COLOR_DEFAULT);
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
		<form:hidden id="currentProfile" name="currentProfile" path="currentProfile" />
			<div class="upperdiv">
				<!-- <fieldset class="column50"> -->
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
						<spring:message code="fixtureForm.label.Location" />
						:
					</div>
					<div class="fieldValue">${plugload.location}</div>
				</div>
				<!-- </fieldset> -->
				<!-- <br style="clear:both"/> -->
				<!-- <fieldset class="column33"> -->
				<div class="fieldWrapper">
					<div class="fieldLabel">
						<spring:message code="fixtureForm.label.ID" />
						:
					</div>
					<div class="fieldValue">${plugload.id}</div>
				</div>
				<div class="fieldWrapper">
					<div class="fieldLabel">
						Floor Plan Position :
					</div>
					<div class="fieldValue">${plugload.xaxis},&nbsp;${plugload.yaxis}</div>
				</div>
			
			</div>		
			<br style="clear: both" />
			<div class="upperdiv">
			<fieldset class="column33">
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.Currentstate" />
							:
						</div>
						<div class="fieldValue" id="Currentstate">${plugload.state}</div>
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
					
				</fieldset>
				<fieldset class="column33">
					<div class="fieldWrapper">
						<div class="fieldLabel">
							Current Power Usage :
						</div>
						<div class="fieldValue" id="currentPowerUsage">${plugload.managedLoad + plugload.unmanagedLoad}&nbsp;watts</div>
					</div>
<!-- 					<div class="fieldWrapper"> -->
<!-- 						<div class="fieldLabel"> -->
<!-- 							Communication Type : -->
<!-- 						</div> -->
<!-- 						<div class="fieldValue" id="communicationType"></div> -->
<!-- 					</div> -->
					<div class="fieldWrapper">
						<div class="fieldLabel">
							Bootloader Version :
						</div>
						<div class="fieldValue" id="bootloaderVersion">${plugload.bootLoaderVersion}</div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							Previous Profile:
						</div>
						<div class="fieldValue">
							<form:input class="text" id="originalProfileFrom" name="originalProfileFrom"
								path="originalProfileFrom" readonly="true"/>
						</div>
					</div>
					
					
				</fieldset>
					<fieldset class="column33">
					<div class="fieldWrapper">
						<div class="fieldLabel">
							Current Firmware Version :
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
					<div class="fieldLabel">
							Current Profile:
						</div>
						<div class="fieldValue">
							<form:select id="groupId" name="groupId"
								path="groupId" class="text">
								<form:options items="${groups}" itemValue="id"
									itemLabel="name" />
							</form:select>
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
					<br style="clear: both" />
			</div>	
			<br style="clear: both" />
			<div class="upperdiv" style="padding-left: 5px;">
				<table cellspacing="0" cellpadding="50">
					<tr>
						<td width=33%>
							<table cellspacing="5" cellpadding="50">
								<tr>
									<td><div class="fieldLabel">
											Set Mode :&nbsp;
										</div></td>
									<td>
										<form:radiobutton path="currentState" label="Auto" value="Auto"/>						
										<form:radiobutton path="currentState" label="On" value="On"/>
										<form:radiobutton path="currentState" label="Off" value="Off"/>
									</td>
									
								</tr>
							</table>
						</td>
					</tr>
				</table>
				<br style="clear: both" />
				<table cellpadding="0" cellspacing="0">
						<tr>
							<td>
								<button id="restoreButton"
								>
									<spring:message code="fixtureForm.label.restoreBtn" />
								</button> Restore will boot the fixture with alternate version. &nbsp;
							</td>
							<td>
								<div id="restore_message"
									style="font-size: 14px; font-weight: bold; padding: 5px 0 0 10px;"></div>
							</td>
						</tr>
					</table>					
			</div>
		</form:form>
	</div>
</body>
</html>
