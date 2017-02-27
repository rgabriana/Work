<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>


<spring:url value="/services/org/wds/getdiscoverystatus" var="getDiscoveryStatusUrl" scope="request" />
<spring:url value="/services/org/gateway/du/updatenoofwds/" var="updateNoOfWdsUrl" scope="request" />
<spring:url value="/services/org/wds/startWdsnetworkdiscovery/" var="startNetworkDiscoveryUrl" scope="request" />
<spring:url value="/services/org/wds/getwdscountbygateway/" var="getCountByGatewayUrl" scope="request" />
<spring:url value="/services/org/wds/cancelwdsnetworkdiscovery" var="cancelNetworkDiscoveryUrl" scope="request" />

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
	#fx-discovery-wrapper #wds-discovery-form{border-bottom: 1px solid #DDDDDD; padding: 10px 20px 5px;}
	#fx-discovery-wrapper .fieldWrapper{padding-bottom:4px;}
	#fx-discovery-wrapper .fieldlabel{float:left; height:22px; width: 49%; font-weight: bold;}
	#fx-discovery-wrapper .fieldInput{float:left; height:22px; width: 50%;}
	#fx-discovery-wrapper .fieldInputCombo{float:left; height:24px; width: 50%;}
	#fx-discovery-wrapper .text{height:100%; width:100%;}
	#fx-discovery-wrapper .readOnly {border:0px none;}
	#fx-discovery-wrapper .buttons-wrapper{ text-align: right;}
	
	#fx-discovery-wrapper #fd-message-div{font-weight:bold; padding: 5px 10px 0 10px;}
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
wdsCountMap = {}; //Map having gatewayid as key and no. of wds as value

var discovery_timer;
var discovery_timer_is_on=0;
var iDiscoveryInProgress = 0;

function timedDiscovery() {
	getUpdatedDiscoveryStatus();
	discovery_timer = setTimeout("timedDiscovery()", 3000);
}

function startDiscoveryTimer(){
	if (!discovery_timer_is_on){
		discovery_timer_is_on=1;
		timedDiscovery();
	}
}

function stopDiscoveryTimer(){
	clearTimeout(discovery_timer);
	discovery_timer_is_on=0;
}

$(document).ready(function() {
	//Store Model data in javascript object
	<c:forEach var="entry" items="${sensorsCountMap}" varStatus="status">
		sensorsCountMap["${entry.key}"] = "${entry.value}";
    </c:forEach>
	
	<c:forEach var="fxentry" items="${wdsCountMap}" varStatus="status">
		wdsCountMap["${fxentry.key}"] = "${fxentry.value}";
    </c:forEach>
    
	//add click handler
	$('#fd-start-btn').click(function(){StartBtnHandler();});
	$('#fd-cancel-btn').click(function(){cancelDiscovery();});
	$('#fd-ok-btn').click(function(){completeDiscovery();});
	
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
	$("#fdGatewayCombo").empty(); //$("#fdGatewayCombo").append(new Option("", ""));
	<c:forEach items="${gateways}" var="gateway">
		$('#fdGatewayCombo').append($('<option></option>').val("${gateway.id}").html("${gateway.gatewayName}"));
		gw_cnt++;
	</c:forEach>

	onGatewaySelect();
	
	if(gw_cnt==0){
		displayFxDiscoveryMessage("There is no commissioned Gateway in this floor. WDS cannot be discovered without a commissioned Gateway.", COLOR_FAILURE);
		$('#fd-start-btn').attr("disabled", true);
	}
}

function onGatewaySelect(){ 
	var gatewayId = $("#fdGatewayCombo").val();
	$("#noOfInstalled").val(sensorsCountMap[gatewayId]);
	$("#noOfDiscovered").val(wdsCountMap[gatewayId]);
}

function StartBtnHandler(){
	clearFxDiscoveryMessage();
	var noOfInstalled = $("#noOfInstalled").val();
	var noOfDiscovered = 1 * $("#noOfDiscovered").val();
	
	if(noOfInstalled == ""){
		displayFxDiscoveryMessage("No. of installed WDS should not be left blank", COLOR_FAILURE);
		return;
	}
	
	var rege = /^([0-9]+)$/;
	var isValidNumber = rege.test(noOfInstalled);
	
	if(isValidNumber == false){
		displayFxDiscoveryMessage("No. of installed WDS should be a Number", COLOR_FAILURE);
		return;
	}
	
	if(1*noOfInstalled == 0){
		displayFxDiscoveryMessage("No. of installed sensors should not be 0", COLOR_FAILURE);
		return;
	}
	
	if(1*noOfInstalled <= 1*noOfDiscovered){
		displayFxDiscoveryMessage("No. of installed WDS should not be less than or equal to no of discovered WDS", COLOR_FAILURE);
		return;
	}
	
	if(noOfInstalled > 99999){
		displayFxDiscoveryMessage("No. of installed WDS should be less than or equal to 99999", COLOR_FAILURE);
		return;		
	}
	
	getDiscoveryStatus();
	
	//disable combo,buttons
	$('#fd-start-btn').attr("disabled", true);
	$('#fdGatewayCombo').attr("disabled", true);
	$('#noOfInstalled').attr("disabled", true);
	
}

function getDiscoveryStatus(){
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
		displayFxDiscoveryMessage("Discovery is already in progress. Please try later.", COLOR_FAILURE);
		iDiscoveryInProgress = 1;
	} else {
		updateGatewayNoOfWds();
	}
}

function updateGatewayNoOfWds(){
	var gatewayId = $("#fdGatewayCombo").val();
	var noOfWds = $("#noOfInstalled").val();
	sensorsCountMap[gatewayId] = noOfWds;
	
	$.ajax({
		url: "${updateNoOfWdsUrl}gateway/"+gatewayId+"/noofwds/"+noOfWds+"?ts="+new Date().getTime(),
		success: function(data){
			startNetworkDiscovery();
		}
	});
}

function startNetworkDiscovery(){
	var gatewayId = $("#fdGatewayCombo").val();
	var floorId = treenodeid; //selected tree node id (floor id)

	$.ajax({
		url: "${startNetworkDiscoveryUrl}floor/"+floorId+"/gateway/"+gatewayId+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			startNetworkDiscovery_success(1 * data.status);
		}
	});
}

function startNetworkDiscovery_success(Status){
	if(Status == DISC_STATUS_STARTED) {				
		startDiscoveryTimer();
		displayFxDiscoveryMessage("Discovering WDS...", COLOR_SUCCESS);
	} else if(Status == COMM_ERROR_INPROGRESS) {
		displayFxDiscoveryMessage("Commissioning is already in progress. Please try later.", COLOR_FAILURE);
	} else if(Status == DISC_STATUS_INPROGRESS) {
		displayFxDiscoveryMessage("Discovery is already in progress. Please try later.", COLOR_FAILURE);
	} else if(Status == DISC_ERROR_INPROGRESS) {
		displayFxDiscoveryMessage("Discovery is already in progress. Please try later.", COLOR_FAILURE);
	}
}

function getUpdatedDiscoveryStatus(){
	loadAllWds(); // Ensuring that the discoverd WDS count is fetched first before calling Discovery status
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
					displayFxDiscoveryMessage("All WDS are discovered.", COLOR_SUCCESS);
					showCompleteDiscovery();
					break;
					
				case DISC_ERROR_GW_CH_CHANG_DEF:
					displayFxDiscoveryMessage("Unable to communicate with gateway, please check gateway connectivity.", COLOR_FAILURE);
					showCompleteDiscovery();
					break;
					
				case DISC_ERROR_TIMED_OUT:
					displayFxDiscoveryMessage("Discovery timeout.", COLOR_FAILURE);
					showCompleteDiscovery();
					break;
					
				case DISC_ERROR_GW_CH_CHANGE_CUSTOM:
					displayFxDiscoveryMessage("Not able to move the Gateway to custom wireless parameters after discovery.", COLOR_FAILURE);
					showCompleteDiscovery();
					break;
			}			
		}
	});
}

function loadAllWds(){
	var gatewayId = $("#fdGatewayCombo").val();
	
	$.ajax({
		url: "${getCountByGatewayUrl}"+gatewayId+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			var resNoOfWds = (1 * data.status);
			$("#noOfDiscovered").val(resNoOfWds);
			wdsCountMap[gatewayId] = resNoOfWds;
			
			var noOfInstalled = 1 * $("#noOfInstalled").val();

			if(resNoOfWds >= noOfInstalled) {
				displayFxDiscoveryMessage("All WDS are discovered.", COLOR_SUCCESS);
				showCompleteDiscovery();
			}
		}
	});
}

function showCompleteDiscovery(){
	stopDiscoveryTimer();

	$('#fd-ok-btn').css("visibility", "visible");
	$('#fd-start-btn').css("display", "none");
	$('#fd-cancel-btn').css("display", "none");
}

function completeDiscovery(){
	reloadSwichtFrame();
	exitWindow();
}

function exitWindow(){
	stopDiscoveryTimer();
	$("#wdsDiscoveryDialog").dialog("close");
}

function cancelDiscovery(){
	if (iDiscoveryInProgress == 0) {
		var choice = confirm("Are you sure you wish to cancel WDS discovery?");
		if(choice == true){
			stopDiscoveryTimer();
			reloadSwichtFrame();
			$.ajax({
				url: "${cancelNetworkDiscoveryUrl}"+"?ts="+new Date().getTime(),
				success: function(data){
					getDiscoveryStatus_BeforeCancel();
				}
			});
		}
	} else {
		exitWindow();
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
				displayFxDiscoveryMessage("Finishing Discovery. Please wait...", COLOR_SUCCESS);
				var cancel_timer = setTimeout("getDiscoveryStatus_BeforeCancel()", 1000);
			} else {
				exitWindow();
			}
		}
	});
}

function displayFxDiscoveryMessage(Message, Color) {
	$("#fd-message-div").html(Message);
	$("#fd-message-div").css("color", Color);
}
function clearFxDiscoveryMessage() {
	displayFxDiscoveryMessage("", COLOR_DEFAULT);
}

function reloadSwichtFrame(){
	var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("switchesFrame");
	ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src;
}
</script>

</head>
<body>
<div id="fx-discovery-wrapper" style="height:100%; width:100%;">
		<form id="wds-discovery-form">
			<div class="fieldWrapper">
				<div class="fieldlabel"><label for="gatewayId">Gateway:</label></div>
				<div class="fieldInputCombo">
					<select class="text" id="fdGatewayCombo" name="gatewayId"  onchange="javascript: onGatewaySelect();"> </select>
				</div>
				<br style="clear:both;"/>
			</div>
			
			<div class="fieldWrapper">
				<div class="fieldlabel">Number of installed WDS:</div>
				<div class="fieldInput"><input class="text" id="noOfInstalled" name="noOfInstalled"/></div>
				<br style="clear:both;"/>
			</div>
			<div style="height:3px;"></div>
			<div class="fieldWrapper">
				<div class="fieldlabel">Number of discovered WDS:</div>
				<div class="fieldInput"><input class="text readOnly" id="noOfDiscovered" name="noOfDiscovered"/></div>
				<br style="clear:both;"/>
			</div>
		</form>
		
		<div id="fd-message-div"></div>
		
		<div class="buttons-wrapper">
			<button id="fd-ok-btn" style="visibility: hidden;">&nbsp;OK&nbsp;</button>
			<button id="fd-start-btn">Start</button>
			<button id="fd-cancel-btn">Cancel</button> 
			&nbsp;
		</div>
</div>
</body>
</html>