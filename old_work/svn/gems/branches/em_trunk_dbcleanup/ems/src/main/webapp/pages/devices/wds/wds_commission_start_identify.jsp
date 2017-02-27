<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<spring:url value="/services/org/wds/startcommission/" var="startCommissionWDSUrl" scope="request" />

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
	
	.ui-dialog-title{font-size: 1.4em; font-weight: bolder;}
</style>

<style>
	#fc-identify-wrapper{background-color: #EEEEEE;}
	#fc-identify-wrapper #fixture-identify-form{padding: 10px 40px 0; border-bottom: 1px solid #CCCCCC;}
	#fc-identify-wrapper .fieldWrapper{padding-bottom:4px;}
	#fc-identify-wrapper .fieldlabel{float:left; height:22px; width: 38%; font-weight: bold;}
	#fc-identify-wrapper .fieldInput{float:left; height:22px; width: 60%;}
	#fc-identify-wrapper .text{height:100%; width:100%;}
	#fc-identify-wrapper .buttons-wrapper{ text-align: right;}
	#fc-identify-wrapper .fcsi-north{height:105px; width:100%; text-align: center;}
	#fc-identify-wrapper .fcsi-body{background-color: #EEEEEE;}
	
	#details-message{height:100%; width:99%; resize:none;}
	#fcsi-message-div{font-weight:bold; padding: 5px 10px 0 10px;}
</style>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Start Identify-Config-Commission-Place</title>


<script type="text/javascript">
//Constants
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

$(document).ready(function() {
	//add click handler
	$('#fcsi-ok-btn').click(function(){startValidation();});
	$('#fcsi-cancel-btn').click(function(){cancelValidation();});
	
	//Fill gateway combo
	setGatewayData();
	
	//Mark textarea as readonly
	$('#details-message').attr('readonly', 'readonly');
	$('#details-message').focus(function(){$(this).blur();});
	
});

function setGatewayData(){
	var gw_cnt = 0;
	$("#gatewayCombo").empty();
	<c:forEach items="${gateways}" var="gateway">
		$('#gatewayCombo').append($('<option></option>').val("${gateway.id}").html("${gateway.gatewayName}"));
		gw_cnt++;
	</c:forEach>

	if(gw_cnt==0){
		displayFxInitialMessage("There is no commissioned Gateway in this floor. WDS cannot be commissioned without a commissioned Gateway.", COLOR_FAILURE);
		$('#fcsi-ok-btn').attr("disabled", true);
	}
}

function startValidation(){
	var selectType = 0;
	var selectedGateway = null;
	clearFxInitialMessage();
	
	selectedGateway = $("#gatewayCombo").val();;
	if(selectedGateway == null || selectedGateway == ""){
		displayFxInitialMessage("Please select a Gateway from the Gateway list", COLOR_FAILURE);
		return;
	}
	startCommissionWds(selectType, selectedGateway);
}

function startCommissionWds(selectType, selectedGateway){
	var floorId = treenodeid; //selected tree node id (floor id)
	
	var urlOption = "";
	urlOption = "floor/"+floorId+"/gateway/"+selectedGateway;
	
	$.ajax({
		url: "${startCommissionWDSUrl}"+urlOption+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			var status = (1 * data.status);
			if(status == COMM_STATUS_STARTED) {
				showWdsCommissioningForm(selectedGateway);
			} else if(status == DISC_ERROR_INPROGRESS) {
				alert("Discovery is already in progress. Please try later.");
			} else if(status == COMM_ERROR_INPROGRESS) {
				alert("Commissioning is already in progress. Please try later.");
			}
			exitWindow();
		}
	});
	
}

function cancelValidation(){
	var selectType = 0;
	var selectedGateway = null;
	exitWindow();
}

function exitWindow(){
	$("#wdsCommissioningStartIdentifyDialog").dialog("close");
}

function displayFxInitialMessage(Message, Color) {
	$("#fcsi-message-div").html(Message);
	$("#fcsi-message-div").css("color", Color);
}
function clearFxInitialMessage() {
	displayFxInitialMessage("", COLOR_DEFAULT);
}

</script>
</head>
<body>

<div id="fc-identify-wrapper" style="height:100%; width:100%;">
	<div class="fcsi-body">
		<div style="height:10px;">&nbsp;</div>
		<form id="fixture-identify-form">
			<div class="fieldWrapper">
				<div class="fieldlabel"><label for="gatewayId">Select a Gateway:</label></div>
				<div class="fieldInput">
					<select class="text" id="gatewayCombo" name="gatewayId"> </select>
				</div>
				<br style="clear:both;"/>
			</div>
		</form>
		
		<div id="fcsi-message-div"></div>
		
		<div class="buttons-wrapper">
			<button id="fcsi-ok-btn">&nbsp;OK&nbsp;</button>
			<button id="fcsi-cancel-btn">Cancel</button> 
			&nbsp;
		</div>
	</div>
</div>

</body>
</html>