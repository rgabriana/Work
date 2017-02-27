<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<spring:url value="/services/org/plugload/startplugloadcommissioning/" var="startCommissionPlugloadUrl" scope="request" />

<style>

	#pc-identify-wrapper{background-color: #EEEEEE;}	
	#pc-identify-wrapper .text{height:100%; width:100%;}	
	#pc-identify-wrapper .fcsi-north{height:105px; width:100%; text-align: center;}
	#pc-identify-wrapper .fcsi-body{background-color: #EEEEEE;}
	#details-message-plugload{height:100%; width:99%; resize:none;}
		
	#plugload-message-div{font-weight:bold; padding: 5px 10px 0 10px;}
	
	#plugloadCommisionTable table {
		border: thin dotted #7e7e7e;
		padding: 10px;
		
	}
	
	#plugloadCommisionTable th {
		text-align: right;
		vertical-align: middle;
		padding-right: 10px;
	}
	
	#plugloadCommisionTable td {
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
//Options from Model
IS_BULK_COMMISSION = "${isBulkCommission}"=="true";
PLUGLOAD_ID = "${plugloadId}";
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
	$('#pl-ok-btn').click(function(){startValidation();});
	$('#pl-cancel-btn').click(function(){cancelValidation();});
	
	//Fill gateway combo
	setGatewayData();
	
	$('#details-message-plugload').attr('readonly', 'readonly');
	$('#details-message-plugload').focus(function(){$(this).blur();});
	
});

function setGatewayData(){
	var gw_cnt = 0;
	$("#gatewayCombo").empty();
	<c:forEach items="${gateways}" var="gateway">
		$('#gatewayCombo').append($('<option></option>').val("${gateway.id}").html("${gateway.gatewayName}"));
		gw_cnt++;
	</c:forEach>

	if(gw_cnt==0){
		displayPlgInitialMessage("There is no commissioned Gateway in this floor. Plugload cannot be commissioned without a commissioned Gateway.", COLOR_FAILURE);
		$('#pl-ok-btn').attr("disabled", true);
	}
}

function startValidation(){
	var selectType = 0;
	var selectedGateway = null;
	clearFxInitialMessage();
	
	selectedGateway = $("#gatewayCombo").val();;
	if(selectedGateway == null || selectedGateway == ""){
		displayPlgInitialMessage("Please select a Gateway from the Gateway list", COLOR_FAILURE);
		return;
	}
	startCommissionPlugload(selectType, selectedGateway);
}

function startCommissionPlugload(selectType, selectedGateway){
	var floorId = treenodeid; //selected tree node id (floor id)
	var urlOption = "";
	if(IS_BULK_COMMISSION){
		urlOption = "floor/"+floorId+"/gateway/"+selectedGateway+"/type/"+selectType;
	} else {
		urlOption = "plugloadId/"+PLUGLOAD_ID+"/type/"+selectType;
	}
	
	$.ajax({
		url: "${startCommissionPlugloadUrl}"+urlOption+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			var status = (1 * data.status);
			if(status == COMM_STATUS_STARTED) {
				showPlugloadCommissioningForm(IS_BULK_COMMISSION, PLUGLOAD_ID, selectedGateway, selectType);
			} else if(status == DISC_ERROR_INPROGRESS) {
				alert("Discovery is already in progress. Please try later.");
			} else if(status == COMM_ERROR_INPROGRESS) {
				alert("Commissioning is already in progress. Please try later.");
			}
			exitPCSIWindow();
		}
	});
	
}

function cancelValidation(){
	var selectType = 0;
	var selectedGateway = null;
	exitPCSIWindow();
}

function exitPCSIWindow(){
	$("#plugloadCommissioningStartIdentifyDialog").dialog("close");
}

function displayPlgInitialMessage(Message, Color) {
	$("#plugload-message-div").html(Message);
	$("#plugload-message-div").css("color", Color);
}
function clearFxInitialMessage() {
	displayPlgInitialMessage("", COLOR_DEFAULT);
}

</script>
</head>
<body>
<div id="pc-identify-wrapper" style="height:100%; width:100%;">
	<div class="fcsi-north">
		<textarea id="details-message-plugload">Click on the 'Commission and Place' button to commission the plugload and drop it on the floor plan. Finally, drag the plugload to place it at the proper location on the floor plan.</textarea>
	</div>
<div class="fcsi-body">
	<table id="plugloadCommisionTable" style="width: 100%">
		<tr <c:if test="${isBulkCommission == false}"> style="visibility:hidden;"</c:if>>
			<th>Select a Gateway:</th>
			<td><select class="text" id="gatewayCombo" name="gatewayId" style="width: 200px;"> </select><br/>
			<br style="clear:both;"/></td>
		</tr>
		
		<tr>
			<th></th>
			<td style="float:right;"><input id="pl-ok-btn" type="button"
				value="OK">&nbsp;
				<input type="button" id="pl-cancel-btn"
				value="Cancel">	
			</td>
		</tr>
	</table>
	<div id="plugload-message-div"></div>
</div>
</div>
</body>
</html>