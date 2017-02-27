<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>


<spring:url value="/services/org/plugload/getdiscoverystatus" var="getDiscoveryStatusUrl" scope="request" />
<spring:url value="/services/org/gateway/du/updatenoofsensors/" var="updateNoOfSensorsUrl" scope="request" />
<spring:url value="/services/org/plugload/discoverplugloads/" var="startNetworkDiscoveryUrl" scope="request" />
<spring:url value="/services/org/plugload/getcountbygateway/" var="getCountByGatewayUrl" scope="request" />
<spring:url value="/services/org/plugload/cancelnetworkdiscovery" var="cancelNetworkDiscoveryUrl" scope="request" />

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
	
	.ui-dialog-title{font-size: 1.4em; font-weight: bolder;}
</style>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Discover Sensors</title>

<style>
	#pl-discovery-wrapper #plugload-discovery-form{border-bottom: 1px solid #DDDDDD; padding: 10px 20px 5px;}
	#pl-discovery-wrapper .fieldWrapper{padding-bottom:4px;}
	#pl-discovery-wrapper .fieldlabel{float:left; height:22px; width: 49%; font-weight: bold;}
	#pl-discovery-wrapper .fieldInput{float:left; height:22px; width: 50%;}
	#pl-discovery-wrapper .fieldInputCombo{float:left; height:24px; width: 50%;}
	#pl-discovery-wrapper .text{height:100%; width:100%;}
	#pl-discovery-wrapper .readOnly {border:0px none;}
	#pl-discovery-wrapper .buttons-wrapper{ text-align: right;}
	
	#pl-discovery-wrapper #pd-message-div{font-weight:bold; padding: 5px 10px 0 10px;}
</style>

<script type="text/javascript">
var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";

var DISC_STATUS_SUCCESS = 1; 				//All the nodes are discovered
var DISC_STATUS_STARTED = 2; 				//Discovery started		
var DISC_STATUS_INPROGRESS = 3; 			//Discovery is in progress		
var DISC_ERROR_INPROGRESS = 4; 				//Discovery is already in progress
var DISC_ERROR_GW_CH_CHANG_DEF = 5; 		//Not able to move the Gateway to default wireless parameters during discovery		
var DISC_ERROR_TIMED_OUT = 6; 				//Not able to find all the nodes within 3 minute timeout.			
var DISC_ERROR_GW_CH_CHANGE_CUSTOM = 7; 	//Not able to move the Gateway to custom wireless parameters after discovery		
var COMM_STATUS_SUCCESS = 8; 				//Commissioning is successful		
var COMM_STATUS_STARTED = 9; 				//Commissioning started		
var COMM_STATUS_INPROGRESS = 10; 			//Commissioning is in progress		
var COMM_STATUS_FAIL = 11; 					//Commissioning failed		
var COMM_ERROR_INPROGRESS = 12; 			//Commissioning is already in progress		
var COMM_ERROR_GW_CH_CHANGE_DEF = 13; 		//Not able to move the Gateway to default wireless parameters during commissioning.			
var COMM_ERROR_GW_CH_CHANGE_CUSTOM = 14; 	//Not able to move the Gateway to custom wireless parameters during commissioning.
var COMM_ERROR_INACTIVE_TIMED_OUT = 15; 	//Commissioning Timed out due to inactivity
var COMM_ERROR_INACTIVE_TIMED_OUT_GW_CH_CHANGE_CUSTOM = 16;

sensorsCountMap = {}; //Map having gatewayid as key and no. of sensors as value
plugloadsCountMap = {}; //Map having gatewayid as key and no. of plugloads as value

var discovery_timer;
var discovery_timer_is_on=0;
var iDiscoveryInProgress = 0;

function timedPlDiscovery() {
	getUpdatedDiscoveryStatus();
	discovery_timer = setTimeout("timedPlDiscovery()", 3000);
}

function startPlDiscoveryTimer(){
	if (!discovery_timer_is_on){
		discovery_timer_is_on=1;
		timedPlDiscovery();
	}
}

function stopPlDiscoveryTimer(){
	clearTimeout(discovery_timer);
	discovery_timer_is_on=0;
}

$(document).ready(function() {
	//Store Model data in javascript object
	<c:forEach var="entry" items="${sensorsCountMap}" varStatus="status">
		sensorsCountMap["${entry.key}"] = "${entry.value}";
    </c:forEach>
	
	<c:forEach var="fxentry" items="${plugloadsCountMap}" varStatus="status">
		plugloadsCountMap["${fxentry.key}"] = "${fxentry.value}";
    </c:forEach>
    
	//add click handler
	$('#pd-start-btn').click(function(){StartBtnHandler();});
	$('#pd-cancel-btn').click(function(){cancelDiscovery();});
	$('#pd-ok-btn').click(function(){completePlDiscovery();});
	
	//Fill gateway combo
	loadGatewayData();
	
	//Mark un-editable field as readonly
	$('input.readOnly').attr('readonly', 'readonly');
	$('input.readOnly').focus(function() {
		 $(this).blur();
	});
});

function loadGatewayData(){
	var gw_cnt = 0;
	$("#pdGatewayCombo").empty(); //$("#pdGatewayCombo").append(new Option("", ""));
	<c:forEach items="${gateways}" var="gateway">
		$('#pdGatewayCombo').append($('<option></option>').val("${gateway.id}").html("${gateway.gatewayName}"));
		gw_cnt++;
	</c:forEach>

	onGatewaySelect();
	
	if(gw_cnt==0){
		displayPlDiscoveryMessage("There is no commissioned Gateway in this floor. Plugloads cannot be discovered without a commissioned Gateway.", COLOR_FAILURE);
		$('#pd-start-btn').attr("disabled", true);
	}
}

function onGatewaySelect(){ 
	var gatewayId = $("#pdGatewayCombo").val();
	$("#noOfInstalledPl").val(sensorsCountMap[gatewayId]);
	$("#noOfDiscoveredPl").val(plugloadsCountMap[gatewayId]);
}

function StartBtnHandler(){
	clearPlDiscoveryMessage();
	var noOfInstalledPl = $("#noOfInstalledPl").val();
	var noOfDiscoveredPl = 1 * $("#noOfDiscoveredPl").val();
	
	if(noOfInstalledPl == ""){
		displayPlDiscoveryMessage("No. of installed plugloads should not be left blank", COLOR_FAILURE);
		return;
	}
	
	var rege = /^([0-9]+)$/;
	var isValidNumber = rege.test(noOfInstalledPl);
	
	if(isValidNumber == false){
		displayPlDiscoveryMessage("No. of installed plugloads should be a Number", COLOR_FAILURE);
		return;
	}
	
	if(1*noOfInstalledPl == 0){
		displayPlDiscoveryMessage("No. of installed plugloads should not be 0", COLOR_FAILURE);
		return;
	}
	
	if(1*noOfInstalledPl <= 1*noOfDiscoveredPl){
		displayPlDiscoveryMessage("No. of installed sensors should not be less than or equal to no of discovered sensors", COLOR_FAILURE);
		return;
	}
	
	if(noOfInstalledPl > 99999){
		displayPlDiscoveryMessage("No. of installed sensors should be less than or equal to 99999", COLOR_FAILURE);
		return;		
	}
	
	getPlDiscoveryStatus();
	
	//disable combo,buttons
	$('#pd-start-btn').attr("disabled", true);
	$('#pdGatewayCombo').attr("disabled", true);
	$('#noOfInstalledPl').attr("disabled", true);
	
}

function getPlDiscoveryStatus(){
	$.ajax({
		url: "${getDiscoveryStatusUrl}"+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			startDiscovery(1 * data.status);
		}
	});
}

function startDiscovery(bStatus){
	if(bStatus == 2 || bStatus == 3 || bStatus == 4) {
		displayPlDiscoveryMessage("Discovery is already in progress. Please try later.", COLOR_FAILURE);
		iDiscoveryInProgress = 1;
	} else {
		updateGatewayNoOfSensors();
	}
}

function updateGatewayNoOfSensors(){
	var gatewayId = $("#pdGatewayCombo").val();
	var noOfSensors = $("#noOfInstalledPl").val();
	sensorsCountMap[gatewayId] = noOfSensors;
	
	$.ajax({
		url: "${updateNoOfSensorsUrl}gateway/"+gatewayId+"/noofsensors/"+noOfSensors+"?ts="+new Date().getTime(),
		success: function(data){
			startPlNetworkDiscovery();
		}
	});
}

function startPlNetworkDiscovery(){
	var gatewayId = $("#pdGatewayCombo").val();
	var floorId = treenodeid; //selected tree node id (floor id)

	$.ajax({
		url: "${startNetworkDiscoveryUrl}"+floorId+"/"+gatewayId+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			startPlNetworkDiscovery_success(1 * data.status);
		}
	});
}

function startPlNetworkDiscovery_success(Status){
	if(Status == DISC_STATUS_STARTED) {				
		startPlDiscoveryTimer();
		displayPlDiscoveryMessage("Discovering Plugloads...", COLOR_SUCCESS);
	} else if(Status == COMM_ERROR_INPROGRESS) {
		displayPlDiscoveryMessage("Commissioning is already in progress. Please try later.", COLOR_FAILURE);
	} else if(Status == DISC_STATUS_INPROGRESS) {
		displayPlDiscoveryMessage("Discovery is already in progress. Please try later.", COLOR_FAILURE);
	} else if(Status == DISC_ERROR_INPROGRESS) {
		displayPlDiscoveryMessage("Discovery is already in progress. Please try later.", COLOR_FAILURE);
	}
}

function getUpdatedDiscoveryStatus(){
	loadAllplugloads(); // Ensuring that the discoverd plugloads count is fetched first before calling Discovery status
	getDiscoveryStatus_DuringDiscovery();
}

function getDiscoveryStatus_DuringDiscovery(){
	$.ajax({
		url: "${getDiscoveryStatusUrl}"+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			var bStatus = (1 * data.status);
			switch(bStatus) {
				case DISC_STATUS_SUCCESS:
					displayPlDiscoveryMessage("All plugloads are discovered.", COLOR_SUCCESS);
					showcompletePlDiscovery();
					break;
					
				case DISC_ERROR_GW_CH_CHANG_DEF:
					displayPlDiscoveryMessage("Unable to communicate with gateway, please check gateway connectivity.", COLOR_FAILURE);
					showcompletePlDiscovery();
					break;
					
				case DISC_ERROR_TIMED_OUT:
					displayPlDiscoveryMessage("Discovery timeout.", COLOR_FAILURE);
					showcompletePlDiscovery();
					break;
					
				case DISC_ERROR_GW_CH_CHANGE_CUSTOM:
					displayPlDiscoveryMessage("Not able to move the Gateway to custom wireless parameters after discovery.", COLOR_FAILURE);
					showcompletePlDiscovery();
					break;
			}			
		}
	});
}

function loadAllplugloads(){
	var gatewayId = $("#pdGatewayCombo").val();
	
	$.ajax({
		url: "${getCountByGatewayUrl}"+gatewayId+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			var resNoOfplugloads = (1 * data.status);
			$("#noOfDiscoveredPl").val(resNoOfplugloads);
			plugloadsCountMap[gatewayId] = resNoOfplugloads;
			
			var noOfInstalledPl = 1 * $("#noOfInstalledPl").val();

			if(resNoOfplugloads >= noOfInstalledPl) {
				displayPlDiscoveryMessage("All plugloads are discovered.", COLOR_SUCCESS);
				showcompletePlDiscovery();
			}
		}
	});
}

function showcompletePlDiscovery(){
	stopPlDiscoveryTimer();

	$('#pd-ok-btn').css("visibility", "visible");
	$('#pd-start-btn').css("display", "none");
	$('#pd-cancel-btn').css("display", "none");
}

function completePlDiscovery(){
	reloadplugloadsFrame();
	exitPlWindow();
}

function exitPlWindow(){
	stopPlDiscoveryTimer();
	$("#plugloadDiscoveryDialog").dialog("close");
}

function cancelDiscovery(){
	if (iDiscoveryInProgress == 0) {
		var choice = confirm("Are you sure you wish to cancel Sensor discovery?");
		if(choice == true){
			stopPlDiscoveryTimer();
			reloadplugloadsFrame();
			$.ajax({
				url: "${cancelNetworkDiscoveryUrl}"+"?ts="+new Date().getTime(),
				success: function(data){
					getDiscoveryStatus_BeforeCancel();
				}
			});
		}
	} else {
		exitPlWindow();
	}
}

function getDiscoveryStatus_BeforeCancel(){
	$.ajax({
		url: "${getDiscoveryStatusUrl}"+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			var bStatus = (1 * data.status);
			if(bStatus == 2 || bStatus == 3 || bStatus == 4) {
				displayPlDiscoveryMessage("Finishing Discovery. Please wait...", COLOR_SUCCESS);
				var cancel_timer = setTimeout("getDiscoveryStatus_BeforeCancel()", 1000);
			} else {
				exitPlWindow();
			}
		}
	});
}

function displayPlDiscoveryMessage(Message, Color) {
	$("#pd-message-div").html(Message);
	$("#pd-message-div").css("color", Color);
}
function clearPlDiscoveryMessage() {
	displayPlDiscoveryMessage("", COLOR_DEFAULT);
}

function reloadplugloadsFrame(){
	var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("plugloadsFrame");
	ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src;
}
</script>

</head>
<body>
<div id="pl-discovery-wrapper" style="height:100%; width:100%;">
		<form id="plugload-discovery-form">
			<div class="fieldWrapper">
				<div class="fieldlabel"><label for="gatewayId">Gateway:</label></div>
				<div class="fieldInputCombo">
					<select class="text" id="pdGatewayCombo" name="gatewayId"  onchange="javascript: onGatewaySelect();"> </select>
				</div>
				<br style="clear:both;"/>
			</div>
			
			<div class="fieldWrapper">
				<div class="fieldlabel">Number of installed plugloads:</div>
				<div class="fieldInput"><input class="text" id="noOfInstalledPl" name="noOfInstalledPl"/></div>
				<br style="clear:both;"/>
			</div>
			<div style="height:3px;"></div>
			<div class="fieldWrapper">
				<div class="fieldlabel">Number of discovered plugloads:</div>
				<div class="fieldInput"><input class="text readOnly" id="noOfDiscoveredPl" name="noOfDiscoveredPl"/></div>
				<br style="clear:both;"/>
			</div>
		</form>
		
		<div id="pd-message-div"></div>
		
		<div class="buttons-wrapper">
			<button id="pd-ok-btn" style="visibility: hidden;">&nbsp;OK&nbsp;</button>
			<button id="pd-start-btn">Start</button>
			<button id="pd-cancel-btn">Cancel</button> 
			&nbsp;
		</div>
</div>
</body>
</html>