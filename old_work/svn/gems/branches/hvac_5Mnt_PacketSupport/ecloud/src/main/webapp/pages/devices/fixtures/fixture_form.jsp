<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ecloud" uri="/WEB-INF/tlds/ecloud.tld"%>

<spring:url value="/devices/fixtures/updateFixture.ems"
	var="updateFixtureUrl" scope="request" />
<spring:url value="/services/org/fixture/op/dim/abs/"
	var="dimfixtureUrl" scope="request" />
<spring:url value="/services/org/fixture/op/mode/" var="autofixtureUrl"
	scope="request" />
<spring:url value="/services/org/fixture/switchrunningimage"
	var="switchImagefixtureUrl" scope="request" />
<spring:url value="/services/org/fixture/checkswitchrunningimagestatus"
	var="checkswitchrunningimagestatusUrl" scope="request" />
<spring:url value="/services/org/profile" var="changeFixtureProfileUrl" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Fixture Details</title>

<!-- Util Function required for Fixture Form -->
<script type="text/javascript">
//Constants 
var COMMUNICATION_TYPE_ONE = "Wireless";
var COMMUNICATION_TYPE_TWO = "Powerline";
var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
var restoreStatusAttempt = 0;
var orgSliderValue = 0;

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

function reloadFixturesFrame(){
	var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("fixturesFrame");
	ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src;
}



</script>

<!-- JS function required for Fixture Form -->
<script type="text/javascript">
// 	$("#updateButton").button();
	var BALLASTS_DATA = {}; //JSON Object: key=ballat_id, value=ballast_object_json
	var FX_ORIGINAL_PROFILE_BEFORE_UPDATE = "${originalProfileFrom}";
	var FX_CURRENT_PROFILE_BEFORE_UPDATE = "${currentProfile}";
	var PROFILE_DATA = {}; //JSON Object: key=profile_name, value=profile_id
	var currentGroupListIndex = 0;
	var groupNameList = [];
	
	var FIXTURE_CLASS_DETAILS_DATA = {};
	
	var FX_ORIGINAL_BALLAST_BEFORE_UPDATE = "${fixture.ballast.id}";
	var FX_ORIGINAL_BALLAST_LABEL_BEFORE_UPDATE = "${fixture.ballast.ballastName}";
	var FX_ORIGINAL_BULB_BEFORE_UPDATE = "${fixture.bulb.id}";
	var FX_ORIGINAL_BULB_LABEL_BEFORE_UPDATE = "${fixture.bulb.bulbName}";
	var FX_ORIGINAL_VOLTAGE_BEFORE_UPDATE = "${fixture.voltage}";
	var FX_ORIGINAL_NO_OF_BALLASTS_BEFORE_UPDATE = "${fixture.noOfFixtures}";
	var FX_ORIGINAL_NO_OF_BULBS_BEFORE_UPDATE = "${fixture.noOfBulbs}";
	
	loadFormValues();
	loadAllProfiles();
	
	$('#enableHopper').click(function() {
		$('#isHopper').val(this.checked?"1":"0");
	});
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
	function loadFormValues(){
		//Fetch Model Attributes in JS Variables which requires manipulation of values
		var temperature = '<c:out value="${fixture.avgTemperature}"/>';
		var macAddress = '<c:out value="${fixture.macAddress}"/>';
		var dimmerControl = '<c:out value="${fixture.dimmerControl}"/>';
		var isHopper = '<c:out value="${fixture.isHopper}"/>';
		var commType = '<c:out value="${fixture.commType}"/>';

		$("#enableHopper").attr('checked', isHopper==1);
		$("#temperature").html(getTemperatureString(temperature));
		$("#macAddress").html(getMACAddress(macAddress));
		$("#Currentstate").html((dimmerControl<=0?"OFF":"ON") + " ("+dimmerControl+"%)");
		
		switch(1*commType){
			case 1:
				$("#communicationType").html(COMMUNICATION_TYPE_ONE);
				$("#macAddressWrapper").css("display", "block");
				$("#ipAddressWrapper").css("display", "none");
			  	break;
			case 2:
				$("#communicationType").html(COMMUNICATION_TYPE_TWO);
				$("#macAddressWrapper").css("display", "none");
				$("#ipAddressWrapper").css("display", "block");
			  	break;
			default:
				$("#communicationType").html("");
		}		
		
		//Create Slider
		var defaultSliderValue = dimmerControl;
		$( "#slider_fixture" ).slider({
			value: defaultSliderValue,
			min: 0,
			max: 100,
			step: 1,
			change: function( event, ui ) {
				updateSliderValueSpan(ui.value);
				//setDimmerState(ui.value);
			}
		});
		
		$( "#slider_fixture" ).bind( "slidestop", function(event, ui) {
			updateSliderValueSpan(ui.value);
			setDimmerState(ui.value);
		});

		$( "#slider_fixture" ).bind( "slidestart", function(event, ui) {
			orgSliderValue = ui.value;
		});

		updateSliderValueSpan(defaultSliderValue);
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
	
	function setSliderValue(value){
		$("#slider_fixture").slider("value", value);
		setDimmerState(value) ;
	}
	
	function updateSliderValueSpan(value){
		$("#slider_value").html(value+"%");
		$("#slider_value").css("left", (value*3 - (value<10?15:20)) + "px");
	}

	function resetSliderValue(){
		$("#slider_fixture").slider("value", orgSliderValue);
		updateSliderValueSpan(orgSliderValue) ;
	}
	
	function setDimmerState(value){
		var lightControlMinutes = $("#lightControlMinutes").val();
		
		if(lightControlMinutes == "") {
			displayLabelMessage('<spring:message code="fixtureForm.message.validation.time_blank"/>', COLOR_FAILURE);
			resetSliderValue();
		}else if(!(/^([0-9]+)$/).test(lightControlMinutes)){
			displayLabelMessage("Manual Override Time should be a Number", COLOR_FAILURE);
			resetSliderValue();
			return;
		}else if(lightControlMinutes > 99999){
			displayLabelMessage("Manual Override Time should be less than or equal to 99999", COLOR_FAILURE);
			resetSliderValue();
			return;
		}
		else if(1*lightControlMinutes == 0) {
			displayLabelMessage('<spring:message code="fixtureForm.message.validation.time_zero"/>', COLOR_FAILURE);
			resetSliderValue();
		}else {
			clearLabelMessage();
			setCurrentState(value);

			$.ajax({
				type: 'POST',
				url: "${dimfixtureUrl}"+value+"/"+(1*lightControlMinutes)+"/"+"${pid}?ts="+ new Date().getTime(),
				data: "<fixtures><fixture><id>${fixture.id}</id></fixture></fixtures>",
				success: function(data){
	 					},
				dataType:"xml",
				contentType: "application/xml; charset=utf-8",
			});
			
		}
	}
	
	function displayLabelMessage(Message, Color) {
		$("#fixture_message").html(Message);
		$("#fixture_message").css("color", Color);
	}
	
	function displayRestoreLabelMessage(Message, Color) {
		$("#restore_message").html(Message);
		$("#restore_message").css("color", Color);
	}
	
	
	function clearLabelMessage(Message, Color) {
		displayLabelMessage("", COLOR_DEFAULT);
	}
	
	function setCurrentState(dimmerControlValue) {
		$("#Currentstate").html((dimmerControlValue<=0?"OFF":"ON") + " ("+dimmerControlValue+"%)");
	}
	
	function switchFixtureImage() {
		displayRestoreLabelMessage(
				'<spring:message code="fixtureForm.message.restore.waiting"/>',
				COLOR_DEFAULT);
		$.ajax({
			type : 'POST',
			url : "${switchImagefixtureUrl}",
			data : "<fixture><id>${fixture.id}</id><name>${fixture.fixtureName}</name></fixture>",
			success : function(data) {
				checkFixtureImageRestoreSuccess();
			},
			dataType : "json",
			contentType : "application/xml; charset=utf-8",
		});
	}

	function checkFixtureImageRestoreSuccess() {
		$.ajax({
			type : 'POST',
			url : "${checkswitchrunningimagestatusUrl}",
			data : "<fixture><id>${fixture.id}</id></fixture>",
			success : function(data) {
				var version = $("#currApp").val();
				if (version != data.msg) {
					displayRestoreLabelMessage("Success", COLOR_SUCCESS);
					$("#currApp").val(data.msg);
					var alterversion = $("#firmVersion").text();
					var runningversion = $("#appVersion").text();
					$("#firmVersion").text(runningversion);
					$("#appVersion").text(alterversion);
				} else {
					if(restoreStatusAttempt > 60){
					displayRestoreLabelMessage(
							"Failed to get reboot status from Fixture. Please check connectivity to Fixture.",
							COLOR_FAILURE);
					restoreStatusAttempt = 0;
					}else{
						restoreStatusAttempt++;
						setTimeout(checkFixtureImageRestoreSuccess, 2000);
					}
				}
			},
			dataType : "json",
			contentType : "application/xml; charset=utf-8",
		});
	}
	
	var SELECTED_FIXTURES_TO_UPDATE_PROFILE = 1;
	var UPDATE_PROFILE_COUNTER = 0;
	
	
	function saveFixture(){
		
		$("#updateButton").attr("disabled", true);
		
		displayLabelMessage('<spring:message code="fixtureForm.message.waiting"/>', COLOR_DEFAULT);
		
		var postData = getFixtureXML();
		$.ajax({
			type: 'POST',
			url: "${changeFixtureProfileUrl}/bulkassign/"+$("#currentProfile").val()+"/"+"${pid}",
			data: postData,
			async: false,
			success: function(data){
				if(data!=null){ 
					var xml=data.getElementsByTagName("response");
					for (var j=0; j<xml.length; j++) {
						var status = xml[j].getElementsByTagName("status")[0].childNodes[0].nodeValue;
						UPDATE_PROFILE_COUNTER = status;
					}
				}
			},
			complete: function(){
				if(UPDATE_PROFILE_COUNTER >= SELECTED_FIXTURES_TO_UPDATE_PROFILE){
					displayLabelMessage("Fixture Updated Successfully.", COLOR_SUCCESS);
					$("#originalProfileFromText").html(CURRENT_PROFILE_BEFORE_UPDATE);
					CURRENT_PROFILE_BEFORE_UPDATE = $("#currentProfile option:selected").text();
					$('#updateButton').removeAttr("disabled");
				} else {
					displayLabelMessage("Fixture Update Failed.", COLOR_FAILURE);
					$('#updateButton').removeAttr("disabled");
				}
			},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8"
		});
	}
	
	function getFixtureXML(){
		var xmlStr="<fixtures><fixture><id>"+$("#id").val()+"</id></fixture></fixtures>";
		return xmlStr;
	}
	
	function setDimmerOnAuto(){
		$.ajax({
			type: 'POST',
			url: "${autofixtureUrl}AUTO/${pid}?ts="+ new Date().getTime(),
			data: "<fixtures><fixture><id>${fixture.id}</id></fixture></fixtures>",
			success: function(data){
 				},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8",
		});
		
	}
	
	function loadAllProfiles(){
		PROFILE_DATA = {};
		<c:forEach items="${groups}" var="group">
			PROFILE_DATA["${group.name}"] = "${group.id}";
		</c:forEach>
		
		if(FX_CURRENT_PROFILE_BEFORE_UPDATE == "Custom"){
			//alert("if Custom ");
			var modifiedCurrentProfile = document.getElementById("currentProfile");
			var groupId = '<c:out value="${fixture.groupId}"/>';
			modifiedCurrentProfile.add(new Option("Custom",groupId), null);
						
			for (var i = 0; i < modifiedCurrentProfile.options.length; i++) {
			    if (modifiedCurrentProfile.options[i].text == "Custom") {
			    	modifiedCurrentProfile.selectedIndex = i;
			    	//alert("i "+i);
			        break;
			    }
			}
			
		}
	}
	
	
	function getAllFixtureClasses(){
		FIXTURE_CLASS_DETAILS_DATA = {};
		<c:forEach items="${fixtureclasses}" var="fixtureclass">
			var fixtureClassObj = {};
			fixtureClassObj.id = "${fixtureclass.id}";
			fixtureClassObj.name = "${fixtureclass.name}";
			fixtureClassObj.noOfBallasts = "${fixtureclass.noOfBallasts}";
			fixtureClassObj.voltage = "${fixtureclass.voltage}";
			fixtureClassObj.ballastId = "${fixtureclass.ballast.id}";
			fixtureClassObj.ballastName = "${fixtureclass.ballast.ballastName}";
			fixtureClassObj.lampNum = "${fixtureclass.ballast.lampNum}";
			fixtureClassObj.bulbId = "${fixtureclass.bulb.id}";
			fixtureClassObj.bulbName = "${fixtureclass.bulb.bulbName}";
			FIXTURE_CLASS_DETAILS_DATA["${fixtureclass.id}"] = fixtureClassObj;
			
			if("${fixtureclass.id}" == "${fixture.fixtureClassId}"){
				$("#fixtureclassLabel").text("${fixtureclass.name}");
			}
		</c:forEach>
	}

	function setFixtureDetailsFields(){
		if ($("#fixtureClassCombo option:selected").text() != "Select Fixture Type"){
			var fixtureClassId = $("#fixtureClassCombo").val();
			var ballastId = FIXTURE_CLASS_DETAILS_DATA[fixtureClassId].ballastId;
			var ballastName = FIXTURE_CLASS_DETAILS_DATA[fixtureClassId].ballastName;
			var bulbId = FIXTURE_CLASS_DETAILS_DATA[fixtureClassId].bulbId;
			var bulbName = FIXTURE_CLASS_DETAILS_DATA[fixtureClassId].bulbName;
			var noOfBallasts = FIXTURE_CLASS_DETAILS_DATA[fixtureClassId].noOfBallasts;
			var voltage = FIXTURE_CLASS_DETAILS_DATA[fixtureClassId].voltage;
			//var noOfBulbs = BALLASTS_DATA[ballastId].lampnum;
			var noOfBulbs = FIXTURE_CLASS_DETAILS_DATA[fixtureClassId].lampNum;
			$("#ballastType").val(ballastId);
			$("#ballastTypeLabel").text(ballastName);
			$("#lampType").val(bulbId);
			$("#lampTypeLabel").text(bulbName);
			$("#Voltage").val(voltage);
			$("#VoltageLabel").text(voltage);
			$("#noOfFixtures").val(noOfBallasts);
			$("#noOfFixturesLabel").text(noOfBallasts);
			$("#noOfBulbs").val(noOfBulbs);
			$("#noOfBulbsLabel").text(noOfBulbs);
		}else{
			
			$("#ballastType").val(FX_ORIGINAL_BALLAST_BEFORE_UPDATE);
			$("#ballastTypeLabel").text(FX_ORIGINAL_BALLAST_LABEL_BEFORE_UPDATE);
			$("#lampType").val(FX_ORIGINAL_BULB_BEFORE_UPDATE);
			$("#lampTypeLabel").text(FX_ORIGINAL_BULB_LABEL_BEFORE_UPDATE);
			$("#Voltage").val(FX_ORIGINAL_VOLTAGE_BEFORE_UPDATE);
			$("#VoltageLabel").text(FX_ORIGINAL_VOLTAGE_BEFORE_UPDATE);
			$("#noOfFixtures").val(FX_ORIGINAL_NO_OF_BALLASTS_BEFORE_UPDATE);
			$("#noOfFixturesLabel").text(FX_ORIGINAL_NO_OF_BALLASTS_BEFORE_UPDATE);
			$("#noOfBulbs").val(FX_ORIGINAL_NO_OF_BULBS_BEFORE_UPDATE);
			$("#noOfBulbsLabel").text(FX_ORIGINAL_NO_OF_BULBS_BEFORE_UPDATE);
				
		}
		
	}
	
	var CURRENT_PROFILE_BEFORE_UPDATE = "";
	
	$(document).ready(function() {
		if("${fixture.id}" == ""){
			displayLabelMessage("Failed to get Fixture Details from EM Instance.Please close this dialog box and try again.", COLOR_FAILURE);
			$("#updateButton").attr("disabled", true);
		}else{
			getAllFixtureClasses();	
			$('#enableHopper').attr("disabled", true);
			CURRENT_PROFILE_BEFORE_UPDATE = $("#currentProfile option:selected").text();
		}
	});

	function confirmRestore()
	{
		if(confirm("Are you sure you want to boot to alternate version?"))
			switchFixtureImage();
	}
	
	function getSelectedProfile(id)
	{
		var profile=null;
		for (var key in PROFILE_DATA)
		{
		   if (PROFILE_DATA.hasOwnProperty(key))
		   {
			   var groupId = PROFILE_DATA[key];
			   if(groupId ==id)
			   {
				   profile = key;
				    break;
			   }
		   }
		}
		if(profile==null && FX_CURRENT_PROFILE_BEFORE_UPDATE == "Custom")
		{
			profile = "Custom";
		}
		return profile;
	}
</script>

<style>
#fixture-dialog-form {
	font-size: 12px;
	padding: 10px;
}

#fixture-dialog-form input.text {
	width: 96%;
}

#fixture-dialog-form hr {
	margin-top: 5px;
	margin-bottom: 5px;
}

#fixture-dialog-form fieldset {
	float: left;
	padding: 0;
	border: 0;
}

#fixture-dialog-form fieldset.column33 {
	width: 33%;
	background: none;
}

#fixture-dialog-form fieldset.column50 {
	width: 50%;
}

#fixture-dialog-form fieldset.column66 {
	width: 66%;
}

#fixture-dialog-form div.fieldWrapper {
	clear: both;
	padding: 5px;
	height: 15px;
}

#fixture-dialog-form div.fieldLabel {
	float: left;
	width: 160px; /*width:40%;*/
	font-weight: bold;
}

#fixture-dialog-form div.fieldValue {
	float: left;
	width: 49%;
}

#fixture-dialog-form fieldset.longlabel div.fieldLabel {
	width: 50%;
}
/*  	#fixture-dialog-form fieldset.longlabel div.fieldValue{float:left; width:49%;} */
#fixture-dialog-form div.fieldValue select {
	width: 95%;
	height: 100%;
}

#fixture-dialog-form div.fieldValue input {
	width: 95%;
	height: 100%;
}

#fixture-dialog-form div.sliderWrapper {
	margin-top: 10px;
}

#fixture-dialog-form div.sliderWrapper div {
	display: inline;
}

#fixture-dialog-form div.sliderDiv div {
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
#fixture-dialog-form .ui-widget-content {
	border: 1px solid #888888 !important;
}
</style>

</head>
<body>
	<div id="fixture-dialog-form">
		<spring:url value="/fixture/updateFixture.ems" var="actionURL"
			scope="request" />
		<div style="padding-bottom: 5px;">
			<button id="updateButton" onclick="javascript:return saveFixture();"
				style="float: left;">
				<spring:message code="fixtureForm.label.applyBtn" />
			</button>
			&nbsp;
			<div id="fixture_message"
				style="font-size: 14px; font-weight: bold; padding: 5px 0 0 10px; float: left;"></div>
			<br style="clear: both" />
		</div>
		<form:form id="fixture-form" commandName="fixture" method="post"
			action="${actionURL}" onsubmit="return false;">
			<form:hidden id="id" name="id" path="id" />
			<form:hidden id="isHopper" name="isHopper" path="isHopper" />
			<form:hidden id="currApp" name="currApp" path="currApp" />
			<form:hidden id="ballastType" name="ballastType" path="ballast.id" />
			<form:hidden id="lampType" name="lampType" path="bulb.id" />
			<form:hidden id="Voltage" name="voltage" path="voltage" />
			<form:hidden id="noOfFixtures" name="noOfFixtures" path="noOfFixtures" />
			<form:hidden id="noOfBulbs" name="noOfBulbs" path="noOfBulbs" />
			<div class="upperdiv divtop">
				<!-- <fieldset class="column50"> -->
				<div class="fieldWrapper">
					<div class="fieldLabel">
						<spring:message code="fixtureForm.label.FixtureName" />
						:
					</div>
					<div class="fieldValue">
					 <!-- <form:input class="text" id="fixtureName" name="fixtureName"
							path="fixtureName" /> -->
						<div class="fieldValue">${fixture.name}</div>
					</div>
				</div>
				<div class="fieldWrapper">
					<div class="fieldLabel">
						<spring:message code="fixtureForm.label.Location" />
						:
					</div>
					<div class="fieldValue">${fixture.location}</div>
				</div>
				<!-- </fieldset> -->
				<!-- <br style="clear:both"/> -->
				<!-- <fieldset class="column33"> -->
				<div class="fieldWrapper">
					<div class="fieldLabel">
						<spring:message code="fixtureForm.label.ID" />
						:
					</div>
					<div class="fieldValue">${fixture.id}</div>
				</div>
				<!-- </fieldset> -->
				<!-- <fieldset class="column33"> -->
				<div class="fieldWrapper">
					<div class="fieldLabel">
						<spring:message code="fixtureForm.label.Floorplanposition" />
						:
					</div>
					<div class="fieldValue">${fixture.xaxis},&nbsp;${fixture.yaxis}</div>
				</div>
				<!-- </fieldset> -->
			</div>
			<br style="clear: both" />
			<div class="upperdiv divmiddle">
				<fieldset class="column33">
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.Currentstate" />
							:
						</div>
						<div class="fieldValue" id="Currentstate">${fixture.dimmerControl}</div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.Temperature" />
							:
						</div>
						<div class="fieldValue" id="temperature"></div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.CommunicationType" />
							:
						</div>
						<div class="fieldValue" id="communicationType"></div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.LastCommunications" />
							:
						</div>
						<div class="fieldValue">
							<ecloud:breakDateDiffInString
								dateValue="${fixture.lastConnectivityAt}"
								datePattern="EEE MMM d HH:mm:ss zzz yyyy" />
						</div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							Fixture Type
						</div>
						<div class="fieldValue">
						<!-- <form:select id="fixtureClassCombo" name="fixtureClassId" path="fixtureClassId"
								class="text" onchange="javascript: setFixtureDetailsFields();">
								<option selected="selected">Select Fixture Type</option>
								<form:options items="${fixtureclasses}" itemValue="id"
									itemLabel="name" />
							</form:select> -->
							<label id="fixtureclassLabel"></label>
						</div>
					</div>
					<div class="fieldWrapper" style="border-top: thin solid #AAAAAA;border-left: thin solid #AAAAAA;border-top-left-radius:10px">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.Ballasttype" />
							:
						</div>
						<div class="fieldValue">
							<label id="ballastTypeLabel">${fixture.ballast.ballastName}</label>
						</div>
					</div>
					<div class="fieldWrapper" style="border-bottom: thin solid #AAAAAA;border-left: thin solid #AAAAAA;border-bottom-left-radius:10px">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.LampType" />
							:
						</div>
						<div class="fieldValue">
							<label id="lampTypeLabel">${fixture.bulb.bulbName}</label>
						</div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.Originalprofilefrom" />
							:
						</div>
						<div class="fieldValue" id="originalProfileFromText">${originalProfileFrom}</div>
					</div>
					<div class="fieldWrapper" style="display: none;">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.Originalprofilefrom" />
							:
						</div>
						<div class="fieldValue">
							<form:input class="text" id="originalProfileFrom"
								name="originalProfileFrom" path="originalProfileFrom" />
						</div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.CharacterizationStatus" />
							:
						</div>
						<div class="fieldValue">
							<label id="characterizationStatus">${characterizationStatus}</label>
						</div>
					</div>
				</fieldset>
				<fieldset class="column33 longlabel">
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.Currentpowerusage" />
							:
						</div>
						<div class="fieldValue">${fixture.wattage}&nbsp;watts</div>
					</div>
					<!-- 			<div class="fieldWrapper"> -->
					<%-- 				<div class="fieldLabel"><spring:message code="fixtureForm.label.Ambientlightlevel"/>:</div> --%>
					<%-- 				<div class="fieldValue">${fixture.lightLevel}&nbsp;foot-candles</div> --%>
					<!-- 			</div> -->
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.Lastoccupancyseen" />
							:
						</div>
						<div class="fieldValue">
							<ecloud:breakSecondsInString seconds="${fixture.lastOccupancySeen}" />
						</div>
					</div>
					<div class="fieldWrapper" id="macAddressWrapper">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.MACaddress" />
							:
						</div>
						<div class="fieldValue" id="macAddress"></div>
					</div>
					<div class="fieldWrapper" id="ipAddressWrapper">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.ipAddress" />
							:
						</div>
						<div class="fieldValue" id="ipAddress">${fixture.ipAddress}</div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.Mode" />
							:
						</div>
						<div class="fieldValue">${fixture.currentState}</div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">&nbsp;&nbsp;</div>
						<div class="fieldValue">&nbsp;&nbsp;</div>
					</div>
					<div class="fieldWrapper" style="border-top: thin solid #AAAAAA;">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.NoofBallast" />
							:
						</div>
						<div class="fieldValue">
							<label id="noOfFixturesLabel">${fixture.noOfFixtures}</label>
						</div>
					</div>
					<div class="fieldWrapper" style="border-bottom: thin solid #AAAAAA;">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.Lampperballast" />
							:
						</div>
						<div class="fieldValue">
							<label id="noOfBulbsLabel">${fixture.noOfBulbs}</label>
						</div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.Currentprofile" />
							:
						</div>
						<div class="fieldValue">
							<form:select id="currentProfile" name="currentProfile"
								path="groupId" class="text">
								<form:options items="${groups}" itemValue="id"
									itemLabel="name" />
							</form:select>
						</div>
					</div>
					
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.FixtureStatus" />
							:
						</div>
						<div class="fieldValue">
							<label id="fixtureStatus">${fixtureStatus}</label>
						</div>
					</div>
					
<!-- 					<div class="fieldWrapper" style="display: none;"> -->
<!-- 						<div class="fieldLabel">Group id:</div> -->
<!-- 						<div class="fieldValue"> -->
<%-- 							<form:input class="text" id="groupId" name="groupId" --%>
<%-- 								path="groupId" /> --%>
<!-- 						</div> -->
<!-- 					</div> -->
				</fieldset>
				<fieldset class="column33">
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.Applicationversion" />
							:
						</div>
						<div id="appVersion" name="appVersion" class="fieldValue">${fixture.version}</div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.Firmwareversion" />
								:
						</div>
						<div id="firmVersion" name="firmVersion" class="fieldValue">${fixture.firmwareVersion}</div>
					</div>
					<div class="fieldWrapper">
					<div class="fieldLabel">
						<spring:message code="fixtureForm.label.Bootloaderversion" />
							:
					</div>
					<div class="fieldValue">${fixture.bootLoaderVersion}</div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.Cuversion" />
							:
						</div>
						<div class="fieldValue">${fixture.cuVersion}</div>
					</div>
					<div class="fieldWrapper">
						<div class="fieldLabel">&nbsp;&nbsp;</div>
						<div class="fieldValue">&nbsp;&nbsp;</div>
					</div>
					<div class="fieldWrapper" style="border-top:thin solid #AAAAAA;border-right: thin solid #AAAAAA;border-top-right-radius:10px">
						<div class="fieldLabel">
							<spring:message code="fixtureForm.label.Voltage" />
							:
						</div>
						<div class="fieldValue">
							<label id="VoltageLabel">${fixture.voltage}</label>
						</div>
					</div>
					<div class="fieldWrapper" style="border-bottom: thin solid #AAAAAA;border-right: thin solid #AAAAAA;border-bottom-right-radius:10px">
						<div class="fieldLabel">&nbsp;&nbsp;</div>
						<div class="fieldValue">&nbsp;&nbsp;</div>
					</div>
					<div class="fieldWrapper">
						<!-- 			<div class="fieldLabel"></div> -->
						<div style="padding-top: 5px;">
							<input type="checkbox" name="enableHopper" id="enableHopper"
								style="width: 12px;" /> <label for="enableHopper"
								style="font-weight: bold;"><spring:message
									code="fixtureForm.label.EnableHopper" /></label>
						</div>
					</div>
				</fieldset>
				<div class="fieldWrapper">
					<div class="fieldLabel">
						<spring:message code="fixtureForm.label.Groups" />
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
			</div>
			<br style="clear: both" />
			<div class="upperdiv" style="padding-left: 5px;">
				<table cellspacing="0" cellpadding="0">
					<tr>
						<td width=33%>
							<table cellspacing="0" cellpadding="0">
								<tr>
									<td><div class="fieldLabel">
											<spring:message code="fixtureForm.label.Dimming" />
											:&nbsp;
										</div></td>
									<td>
										<button onclick="javascript:setDimmerOnAuto(); return false;">
											<spring:message code="fixtureForm.label.Auto" />
										</button>
									</td>
									<td>&nbsp; <span><spring:message
												code="fixtureForm.label.manualfor" />:</span>&nbsp; <input
										id="lightControlMinutes" type="text" style="width: 30px;"
										value="60"></input>&nbsp; <span><spring:message
												code="fixtureForm.label.min" /></span>
									</td>
								</tr>
							</table>
						</td>
						<td width=33%>
							<table cellspacing="0" cellpadding="0">
								<tr>
									<td>
										<button onclick="javascript:setSliderValue(0); return false;">
											<spring:message code="fixtureForm.label.Off" />
										</button>
									</td>
									<td style="width: 300px; padding: 2px 25px;">
										<div id="slider_marker">
											&nbsp; <span style="float: left; color: #AAA;">0%</span> <span
												style="float: left; color: #AAA; margin-left: 43%;">50%</span>
											<span style="float: right; color: #AAA;">100%</span>
										</div>
										<div id="slider_fixture"></div>
										<div id="slider_info" style="padding-top: 7px;">
											<span id="slider_value"
												style="position: relative; background-color: #DDDDDD; border: thin solid #AAAAAA; padding: 0 2px;">0%</span>
										</div>
									</td>
									<td>
										<button
											onclick="javascript:setSliderValue(100); return false;">
											<spring:message code="fixtureForm.label.Fullon" />
										</button>
									</td>
								</tr>
							</table>
						</td>
						<td width=33%></td>
					</tr>
				</table>
			</div>
			<br style="clear: both" />
			<c:if test="${fn:startsWith(fixture.version, '1') == false}">
				<div class="upperdiv" style="padding-left: 5px;">
					<table cellpadding="0" cellspacing="0">
						<tr>
							<td>
								<button id="restoreButton"
									onclick="javascript:return confirmRestore();">
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
			</c:if>

		</form:form>
	</div>
</body>
</html>
