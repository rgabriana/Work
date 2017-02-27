<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<spring:url value="/services/org/wds/startcommission/" var="startCommissionWDSUrl" scope="request" />

<style>

	#fc-identify-wrapper{background-color: #EEEEEE;}	
	#fc-identify-wrapper .text{height:100%; width:100%;}	
	#fc-identify-wrapper .fcsi-north{height:105px; width:100%; text-align: center;}
	#fc-identify-wrapper .fcsi-body{background-color: #EEEEEE;}
	#details-message-wds{height:100%; width:99%; resize:none;}
		
	#wds-message-div{font-weight:bold; padding: 5px 10px 0 10px;}
	
	#wdsCommisionTable table {
		border: thin dotted #7e7e7e;
		padding: 10px;
		
	}
	
	#wdsCommisionTable th {
		text-align: right;
		vertical-align: middle;
		padding-right: 10px;
	}
	
	#wdsCommisionTable td {
		vertical-align: top;
		padding-top: 2px;
	}
	
	
	#center {
	  height : 90% !important;
	}
	
	
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
	$('#wds-ok-btn').click(function(){startValidation();});
	$('#wds-cancel-btn').click(function(){cancelValidation();});
	
	//Fill gateway combo
	setGatewayData();
	
	$('#details-message-wds').attr('readonly', 'readonly');
	$('#details-message-wds').focus(function(){$(this).blur();});
	
});

function setGatewayData(){
	var gw_cnt = 0;
	$("#gatewayCombo").empty();
	<c:forEach items="${gateways}" var="gateway">
		$('#gatewayCombo').append($('<option></option>').val("${gateway.id}").html("${gateway.gatewayName}"));
		gw_cnt++;
	</c:forEach>

	if(gw_cnt==0){
		displayFxInitialMessage("There is no commissioned Gateway in this floor. ERC cannot be commissioned without a commissioned Gateway.", COLOR_FAILURE);
		$('#wds-ok-btn').attr("disabled", true);
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
	$("#wds-message-div").html(Message);
	$("#wds-message-div").css("color", Color);
}
function clearFxInitialMessage() {
	displayFxInitialMessage("", COLOR_DEFAULT);
}

</script>
</head>
<body>
<div id="fc-identify-wrapper" style="height:100%; width:100%;">
	<div class="fcsi-north">
		<textarea id="details-message-wds">Simultaneously, press and hold down the second and forth buttons until the green LED blinks. When done correctly, the LEDs will continue to slowly blink until 5 minutes has elapsed, the ERC has been commissioned, or another button is pressed.</textarea>
	</div>
<div class="fcsi-body">
	<table id="wdsCommisionTable">
		<tr>
			<th>Select a Gateway:</th>
			<td><select class="text" id="gatewayCombo" name="gatewayId" style="width: 200px;"> </select></td>
		</tr>
		
		<tr>
			<th></th>
			<td><input id="wds-ok-btn" type="button"
				value="OK">&nbsp;
				<input type="button" id="wds-cancel-btn"
				value="Cancel">	
			</td>
		</tr>
	</table>
	<div id="wds-message-div"></div>
</div>
</div>
</body>
</html>