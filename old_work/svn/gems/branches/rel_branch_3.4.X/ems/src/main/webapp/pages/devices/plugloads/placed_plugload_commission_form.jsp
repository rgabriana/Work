<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<spring:url value="/modules/PlotChartModule.swf" var="placedplugloadplotchartmodule"></spring:url>
<spring:url value="/services/org/plugload/getcommissionstatus" var="getCommissionStatusUrl" scope="request" />
<spring:url value="/services/org/plugload/getplacedplugloadcommissionstatus" var="getPlacedPlugloadCommissionStatusUrl" scope="request" />
<spring:url value="/services/org/plugload/validateplacedplugload" var="validatePlacedPlugloadUrl" scope="request" />
<spring:url value="/services/org/plugload/exitplacedplugloadcommission/" var="exitPlacedCommissionUrl" scope="request" />
<spring:url value="/services/org/plugload/op/hopper/" var="enabledisableHopperUrl" scope="request" />
<spring:url value="/services/org/plugload/op/hopper/" var="enabledisablePlacedPlugloadHopperUrl" scope="request" />
<spring:url value="/services/org/plugload/getplugloadhopperstatus" var="getPlugloadHopperStatusUrl" scope="request" />
<spring:url value="/services/org/plugload/commissionplacedplugloads/" var="startGatewayToDefaultModeUrl" scope="request" />



<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
	
	.ui-dialog-title{font-size: 1.4em; font-weight: bolder;}
	.ui-jqgrid tr.ui-row-ltr td {border-right-width: 0;}
	.ui-jqgrid tr.jqgrow td {border-bottom: 1px dotted #CCCCCC;}
	
	#placed-pplcd-main-box tr.ui-state-highlight{background-color: #3399FF !important; color: white !important;}
</style>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Commission Placed Plugloads</title>

<style>
	#placed-pplcd-main-box {width:100%; height:100%;}

	#placed-pplcd-main-box th{text-align:left; border-right:0 none;}
	#placed-pplcd-main-box th span.ui-jqgrid-resize{display:none !important;}
	.label{font-weight: bold; font-size: 0.9em; color: #555555;}
	.highlightGray{background-color: #EEEEEE;}
 	#placed-plcd-mesaage, #placed-plcd-locationText, #placed-plcd-gateway-name{color: #000000; /*padding-left:10px;*/}

	#placed-pplcd-main-box fieldset{border: none;}
	#placed-pplcd-main-box fieldset.form-column-left{float:left;width: 60%;}
	#placed-pplcd-main-box fieldset.form-column-right{float:left;width: 49%;}
	#placed-pplcd-main-box .fieldWrapper{padding-bottom:2px;}
	#placed-pplcd-main-box .fieldPadding{height:4px;}
	#placed-pplcd-main-box .fieldlabel{float:left; height:20px; width: 15%; font-weight: bold;}
	#placed-pplcd-main-box .fieldButton{float:left; height:20px;width: 15%;padding-left: 10px}
	#placed-pplcd-main-box .fieldInput{float:left; height:20px; width: 40%;}
	#placed-pplcd-main-box .text {height:100%; width:100%;}
	#placed-pplcd-main-box .readOnly {border:0px none;}
	
	.placed-plcd-row-icon{float: left; height: 16px; margin-left: 5px; width: 16px;}
	
	
	#placed-plcd-Table td {
		vertical-align: top;
		padding-top: 2px;
	}
	
</style>
</head>


<script type="text/javascript">
var COMMISSION_TYPE = "${type}";
var GATEWAY_ID  = "${gateway.id}";


var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";

var COMM_STATUS_INPROGRESS = 10; 			//Commissioning is in progress	
var COMM_ERROR_GW_CH_CHANGE_DEF = 13; 		//Not able to move the Gateway to default wireless parameters during commissioning.		
var VALIDATED = "VALIDATED";
var PLACED = "PLACED";		
var COMMISSIONED = "COMMISSIONED";
var UNCOMMISSIONE = "UNCOMMISSIONED";


var COMMISSION_STATUS_UNKNOWN = 0;
var COMMISSION_STATUS_COMMUNICATION = 2;
var COMMISSION_STATUS_MOTION = 4;
var COMMISSION_STATUS_DIMMING = 8;
var COMMISSION_STATUS_WIRELESS = 16;

var placed_plugload_commission_retry_counter = 0;
var exit_placed_commission_retry_counter = 0;

var isHopper = true;

var LOADING_IMAGE_STRING = "";

function getPlacedCommissionPlanObj(objectName) {			
	if ($.browser.mozilla) {
		return document[objectName] 
	}
	return document.getElementById(objectName);
}	


$(document).ready(function() {
	document.onkeypress = disableEnterKey;
	
	SELECTED_COMMISSION_PLUGLOADS_TO_HOPPER = [];
	
	LOADING_IMAGE_STRING = "<img alt='loading' src='../themes/default/images/ajax-loader_small.gif'>";
		
	getPlacedCommissioningStatus_BeforeStart();
	
	//Init flash object
	var load_placed_plugload_flash_floor = function(nodetype, nodeid) {
		
		
		var FP_data = "";
		
		var buildNumber = "";
		
		var versionString = "<ems:showAppVersion />";
		
		var indexNumber = versionString.lastIndexOf('.', (versionString.length)-1);
		
		if(indexNumber != -1 ){
			buildNumber = versionString.slice(indexNumber+1);
		}else{
			buildNumber = Math.floor(Math.random()*10000001);// For Development Version
		}
		
		var placedplugloadplotchartmodule_url = "${placedplugloadplotchartmodule}"+"?buildNumber="+buildNumber;
		
		
		if ($.browser.msie) {
			FP_data = "<object id='c_placed_Pl_floorplan' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='100%'>";
			FP_data +=  "<param name='src' value='" + placedplugloadplotchartmodule_url + "'/>";
			FP_data +=  "<param name='padding' value='0px'/>";
			FP_data +=  "<param name='wmode' value='opaque'/>";
			FP_data +=  "<param name='allowFullScreen' value='true'/>";
			FP_data +=  "<param name='flashVars' value='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION&modeid=PLACED_PLUGLOAD_COMMISSION'/>";
			FP_data +=  "<embed id='c_placed_Pl_floorplan' name='c_placed_pl_floorplan' src='" + placedplugloadplotchartmodule_url + "' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " padding='0px'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION&modeid=PLACED_PLUGLOAD_COMMISSION'/>";
			FP_data +=  "</object>";
		} else {
			FP_data = "<embed id='c_placed_pl_floorplan' name='c_placed_pl_floorplan' src='" + placedplugloadplotchartmodule_url + "' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " padding='0px'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION&modeid=PLACED_PLUGLOAD_COMMISSION'/>";
		}
		
		var tabFP =document.getElementById("placed-plcd-floorplan-flash");
		tabFP.innerHTML = FP_data; 

		// quick fix for the duplicate flash object
		$('div.alt').remove();
	}
	load_placed_plugload_flash_floor('floor', '${floorId}');
	
	$('#placed-plcd-done-btn').click(function(){donePlacedPlcdButtonHandler();});
	
	
	//Mark un-editable field as readonly
	$('input.readOnly').attr('readonly', 'readonly');
	$('input.readOnly').focus(function() {
		 $(this).blur();
	});
}); //End : Document Ready


function getPlacedCommissioningStatus_BeforeStart(){
	
	//placed_plugload_commission_retry_counter = 0;
	
	
	if (SELECTED_COMMISSION_PLUGLOADS_TO_HOPPER.length > 0 ){
		displayPlacedPlCommissionMessage("Bringing gateway to default mode. Please wait..."+LOADING_IMAGE_STRING, COLOR_SUCCESS);
	}else{
		displayPlacedPlCommissionMessage("Starting commissioning process. Please wait..."+LOADING_IMAGE_STRING, COLOR_SUCCESS);
	}
		
	$.ajax({
		url: "${getCommissionStatusUrl}"+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			getPlacedCommissioningStatus_Success(1 * data.status);
		}
	});
}

function getPlacedCommissioningStatus_Success(bStatus){
	if(bStatus == COMM_STATUS_INPROGRESS) {
		clearPlacedPlCommissionMessage();
		//TODO this.Timer_UserInactivity
		
		if (SELECTED_COMMISSION_PLUGLOADS_TO_HOPPER.length > 0 ){
			ShowAllPlugloadToHopperFinalMessage();
		}
		
	} else if(bStatus == COMM_ERROR_GW_CH_CHANGE_DEF) {
		alert("Error in commissioning. Not able to move the gateway to default wireless parameters during commissioning. Please try again.");
		exitPlacedPlcdWindow();
	} else {
		GetPlacedCommissioningStatus_Error();
	}
}

function GetPlacedCommissioningStatus_Error(){
	if(placed_plugload_commission_retry_counter < 30) {
		var placed_commission_retry_timer = setTimeout("getPlacedCommissioningStatus_BeforeStart()", 2000);
		placed_plugload_commission_retry_counter++;
	} else {
		alert("Error in starting commissioning. Please try again");
		exitPlacedPlcdWindow();
	}
}


function disableEnterKey(evt)
{
	 var keyCode = evt ? (evt.which ? evt.which : evt.keyCode) : event.keyCode;
     if (keyCode == 13) {
          return false;
     }
}

var SELECTED_PLACED_PLUGLOADS_TO_COMMISSION = [];

var PLACED_PLUGLOAD_RETRY_COUNTER_ARRAY = {};

var TOTAL_PLACED_PLUGLOAD_NUMBER = 0;

var currentPlacedPlugloadNumber = 0;

var SELECTED_PLACED_PLUGLOADS_TO_HOPPER = [];

var SELECTED_COMMISSION_PLUGLOADS_TO_HOPPER = [];

var COMMISSIONED_PLUGLOAD_HOPPER_RETRY_COUNTER_ARRAY = {};

var COMMISSION_PLUGLOAD_HOPPER_SUCCESS_NUMBER = 0;
var COMMISSION_PLUGLOAD_HOPPER_FAILURE_NUMBER = 0;
var currentCommissionedPlugloadToHopperNumber = 0;
var TOTAL_COMMISSIONED_PLUGLOAD_HOPPER_NUMBER = 0;

var COMMISSION_PLUGLOAD_HOPPER_SUCCESS_ARRAY = new Array();

var PLACED_PLUGLOAD_HOPPER_RETRY_COUNTER_ARRAY = {};

var PLACED_PLUGLOAD_HOPPER_SUCCESS_NUMBER = 0;
var PLACED_PLUGLOAD_HOPPER_FAILURE_NUMBER = 0;
var currentPlacedPlugloadToHopperNumber = 0;
var TOTAL_PLACED_PLUGLOAD_HOPPER_NUMBER = 0;

var PLACED_PLUGLOAD_HOPPER_SUCCESS_ARRAY = new Array();

var COMMISSION_PLUGLOAD_SUCCESS_NUMBER = 0;

var COMMISSION_PLUGLOAD_FAILURE_NUMBER = 0;

var SELECTED_PLACED_PLUGLOADS_WHICHARE_HOPPERS = new Array();

function commissionPlacedPlugloadDevice(selPlacedPlugloads){
	
	SELECTED_PLACED_PLUGLOADS_TO_COMMISSION = [];
	
	SELECTED_PLACED_PLUGLOADS_WHICHARE_HOPPERS = new Array();
		
	if(selPlacedPlugloads != ""){
		SELECTED_PLACED_PLUGLOADS_TO_COMMISSION = eval("("+selPlacedPlugloads+")");
	}
	
		
	TOTAL_PLACED_PLUGLOAD_NUMBER = SELECTED_PLACED_PLUGLOADS_TO_COMMISSION.length;
	
	for(var j=0; j<SELECTED_PLACED_PLUGLOADS_TO_COMMISSION.length; j++){
		var selectedPlacedPlugloadId = SELECTED_PLACED_PLUGLOADS_TO_COMMISSION[j].id;
		PLACED_PLUGLOAD_RETRY_COUNTER_ARRAY[selectedPlacedPlugloadId] = 0;
		
		if(SELECTED_PLACED_PLUGLOADS_TO_COMMISSION[j].isHopper == 1){
			SELECTED_PLACED_PLUGLOADS_WHICHARE_HOPPERS.push(selectedPlacedPlugloadId);
		}
	}
		
	displayPlacedPlCommissionMessage("Plugload(s) commissioning in process..."+LOADING_IMAGE_STRING, COLOR_SUCCESS);
	
	currentPlacedPlugloadNumber = 0;
	
	COMMISSION_PLUGLOAD_SUCCESS_NUMBER = 0;
	
	COMMISSION_PLUGLOAD_FAILURE_NUMBER = 0;
	
	validatePlacedPlugloads();
	
}


function validatePlacedPlugloads(){
	
	var postPlacedPlugloadData = "<plugloads>"+"<plugload><id>"+SELECTED_PLACED_PLUGLOADS_TO_COMMISSION[currentPlacedPlugloadNumber].id+"</id></plugload>"+"</plugloads>";
	
	var urlData = "/gatewayId/"+GATEWAY_ID;
	
	$.ajax({
		url: "${validatePlacedPlugloadUrl}"+urlData+"?ts="+new Date().getTime(),
		dataType:"json",
		type : "POST",
		data : postPlacedPlugloadData,
		contentType: "application/xml; charset=utf-8",
		success: function(data){
			getPlacedPlugloadCommissionStatus(SELECTED_PLACED_PLUGLOADS_TO_COMMISSION[currentPlacedPlugloadNumber].id);
			if( currentPlacedPlugloadNumber < SELECTED_PLACED_PLUGLOADS_TO_COMMISSION.length -1 ){
				currentPlacedPlugloadNumber++;
				validatePlacedPlugloads();
			}
		},
		error: function(){
			validatePlacedPlugloads();
		}
	});
}


function getPlacedPlugloadCommissionStatus(placedCommsionedPlugloadId){
	$.ajax({
		url: "${getPlacedPlugloadCommissionStatusUrl}"+"/plugloadId/"+placedCommsionedPlugloadId+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			getPlacedPlugloadsCommissionStatus_Success(data.msg,data.status);
		},
		error: function(){
			getPlacedPlugloadsCommissionStatus_Success("");
		}
	});
	
}

function getPlacedPlugloadsCommissionStatus_Success(message,placedCommsionedPlugloadId){
	if(message.toUpperCase() == COMMISSIONED) {
		
		COMMISSION_PLUGLOAD_SUCCESS_NUMBER++;
		ShowPlacedPlugloadCommissioningSuccessful(placedCommsionedPlugloadId);
		ShowPlacedPlugloadCommissioningStatusMessage();
		try {
			
			if(SELECTED_PLACED_PLUGLOADS_WHICHARE_HOPPERS.indexOf(placedCommsionedPlugloadId) != -1 ){
				isHopper = true;
				getPlacedCommissionPlanObj("c_placed_pl_floorplan").replacePlacedPlugloadWithCommissionedPlugload('floor', '${floorId}', placedCommsionedPlugloadId,isHopper);
			}else{
				isHopper = false;
				getPlacedCommissionPlanObj("c_placed_pl_floorplan").replacePlacedPlugloadWithCommissionedPlugload('floor', '${floorId}', placedCommsionedPlugloadId,isHopper);
			}
			
		} catch(e){
		
		}
		
	} else {
				
		if(PLACED_PLUGLOAD_RETRY_COUNTER_ARRAY[placedCommsionedPlugloadId] < 10){
			setTimeout("getPlacedPlugloadCommissionStatus("+placedCommsionedPlugloadId+")", 5000);
			PLACED_PLUGLOAD_RETRY_COUNTER_ARRAY[placedCommsionedPlugloadId] = PLACED_PLUGLOAD_RETRY_COUNTER_ARRAY[placedCommsionedPlugloadId] + 1;
		} else {
			ShowPlacedPlugloadCommissioningFailed(placedCommsionedPlugloadId);
			COMMISSION_PLUGLOAD_FAILURE_NUMBER++;
			
		}
		
	}
	showPlacedPlugloadCommissionFinalMessage();
}

function ShowPlacedPlugloadCommissioningSuccessful(placedCommsionedPlugloadId) {
	PLACED_PLUGLOAD_RETRY_COUNTER_ARRAY[placedCommsionedPlugloadId] = 0;
}

function ShowPlacedPlugloadCommissioningStatusMessage() {
	
	var remainingPlacedPlugload = TOTAL_PLACED_PLUGLOAD_NUMBER - COMMISSION_PLUGLOAD_SUCCESS_NUMBER;
	
	displayPlacedPlCommissionMessage("Please wait...no of plugload(s) sucessfully commissioned:"+COMMISSION_PLUGLOAD_SUCCESS_NUMBER+" ,  no of plugloads remaining:"+remainingPlacedPlugload+" "+LOADING_IMAGE_STRING, COLOR_SUCCESS);
		
}

function ShowPlacedPlugloadCommissioningFailed(placedCommsionedPlugloadId) {
		PLACED_PLUGLOAD_RETRY_COUNTER_ARRAY[placedCommsionedPlugloadId] = 0;
}

function showPlacedPlugloadCommissionFinalMessage(){
	var currentPlugloadCompleted = COMMISSION_PLUGLOAD_SUCCESS_NUMBER + COMMISSION_PLUGLOAD_FAILURE_NUMBER;
	
	if(COMMISSION_PLUGLOAD_FAILURE_NUMBER > 0 && currentPlugloadCompleted == TOTAL_PLACED_PLUGLOAD_NUMBER){
		displayPlacedPlCommissionMessage("No of plugload(s) sucessfully commissioned: "+COMMISSION_PLUGLOAD_SUCCESS_NUMBER+" ,  No of plugload(s) failed to Commission: "+COMMISSION_PLUGLOAD_FAILURE_NUMBER, COLOR_SUCCESS);
		getPlacedCommissionPlanObj("c_placed_pl_floorplan").refreshPlugloadData();
	}
	
	if(COMMISSION_PLUGLOAD_FAILURE_NUMBER == 0 && currentPlugloadCompleted == TOTAL_PLACED_PLUGLOAD_NUMBER){
		displayPlacedPlCommissionMessage("All the plugload(s) are sucessfully commissioned. Total no of plugloads commissioned: "+COMMISSION_PLUGLOAD_SUCCESS_NUMBER, COLOR_SUCCESS);
		getPlacedCommissionPlanObj("c_placed_pl_floorplan").refreshPlugloadData();
	}
}

function enableDisablePlugloadHopper(selPlacedFixHopper,selCommissionedFixHopper){
	
	SELECTED_PLACED_PLUGLOADS_TO_HOPPER = [];

	SELECTED_COMMISSION_PLUGLOADS_TO_HOPPER = [];
	
	if(selPlacedFixHopper != ""){
		SELECTED_PLACED_PLUGLOADS_TO_HOPPER = eval("("+selPlacedFixHopper+")");
	}
	
	if(selCommissionedFixHopper != ""){
		SELECTED_COMMISSION_PLUGLOADS_TO_HOPPER = eval("("+selCommissionedFixHopper+")");
	}
	
		
	for(var k=0; k<SELECTED_PLACED_PLUGLOADS_TO_HOPPER.length; k++){
		var placedPlugloadHopperJson = SELECTED_PLACED_PLUGLOADS_TO_HOPPER[k];
		PLACED_PLUGLOAD_HOPPER_RETRY_COUNTER_ARRAY[placedPlugloadHopperJson.id] = 0;
	}
	
		
	for(var l=0; l<SELECTED_COMMISSION_PLUGLOADS_TO_HOPPER.length; l++){
		var commissionedPlugloadHopperJson = SELECTED_COMMISSION_PLUGLOADS_TO_HOPPER[l];
		COMMISSIONED_PLUGLOAD_HOPPER_RETRY_COUNTER_ARRAY[commissionedPlugloadHopperJson.id] = 0;
	}
	
	COMMISSION_PLUGLOAD_HOPPER_SUCCESS_ARRAY = new Array();
	
	PLACED_PLUGLOAD_HOPPER_SUCCESS_ARRAY = new Array();
	
	initenabledisablePlacedPlugloadHopper();
	
	initenabledisableCommissionedPlugloadHopper();
	
		
	if ( SELECTED_PLACED_PLUGLOADS_TO_HOPPER.length > 0){
		startenabledisablePlacedPlugloadHopper();
	}else if (SELECTED_COMMISSION_PLUGLOADS_TO_HOPPER.length > 0){
		initGatewayToCommissionModeProcess();
	}
}

function initenabledisablePlacedPlugloadHopper(){
	
	PLACED_PLUGLOAD_HOPPER_SUCCESS_NUMBER = 0;
	PLACED_PLUGLOAD_HOPPER_FAILURE_NUMBER = 0;
	currentPlacedPlugloadToHopperNumber = 0;
	TOTAL_PLACED_PLUGLOAD_HOPPER_NUMBER = SELECTED_PLACED_PLUGLOADS_TO_HOPPER.length;
	
}

function startenabledisablePlacedPlugloadHopper(){
	
	displayPlacedPlCommissionMessage("Please wait..Hoppers are getting enabled"+LOADING_IMAGE_STRING, COLOR_SUCCESS);
	
	var postPlacedPlugloadHopperData = "<plugloads>"+"<plugload><id>"+SELECTED_PLACED_PLUGLOADS_TO_HOPPER[currentPlacedPlugloadToHopperNumber].id+"</id></plugload>"+"</plugloads>";
	
	var isEnable = true;
	
	$.ajax({
			url: "${enabledisablePlacedPlugloadHopperUrl}"+isEnable+"/gw/"+GATEWAY_ID+"?ts="+new Date().getTime(),
			dataType:"json",
			contentType: "application/xml; charset=utf-8",
			type : "POST",
			data : postPlacedPlugloadHopperData,
			success: function(data){
				getPlacedPlugloadHopperStatus(SELECTED_PLACED_PLUGLOADS_TO_HOPPER[currentPlacedPlugloadToHopperNumber].id);
				if( currentPlacedPlugloadToHopperNumber < SELECTED_PLACED_PLUGLOADS_TO_HOPPER.length -1 ){
					currentPlacedPlugloadToHopperNumber++;
					startenabledisablePlacedPlugloadHopper();
				}
			},
			error: function() {
				startenabledisablePlacedPlugloadHopper();
			}
		});
}

function getPlacedPlugloadHopperStatus(plugloadId){
	
	$.ajax({
		url: "${getPlugloadHopperStatusUrl}"+"/plugloadId/"+plugloadId+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			getPlacedPlugloadHopperStatus_Success(data.msg,data.status);
		},
		error: function(){
			getPlacedPlugloadHopperStatus_Success("","");
		}
	});
	
}

function getPlacedPlugloadHopperStatus_Success(message,plugloadId){
	
	if(message == "1") {
		
		PLACED_PLUGLOAD_HOPPER_SUCCESS_NUMBER++;
		PLACED_PLUGLOAD_HOPPER_SUCCESS_ARRAY.push(plugloadId);
		ShowPlacedPlugloadToHopperSuccessful(plugloadId);
		ShowPlacedPlugloadToHopperStatusMessage();
		getPlacedCommissionPlanObj("c_placed_pl_floorplan").replacePlacedPlugloadWithHopper('floor', '${floorId}', plugloadId);
		
	} else {
				
		if(PLACED_PLUGLOAD_HOPPER_RETRY_COUNTER_ARRAY[plugloadId] < 10){
			setTimeout("getPlacedPlugloadHopperStatus("+plugloadId+")", 5000);
			PLACED_PLUGLOAD_HOPPER_RETRY_COUNTER_ARRAY[plugloadId] = PLACED_PLUGLOAD_HOPPER_RETRY_COUNTER_ARRAY[plugloadId] + 1;
		}else {
			ShowPlacedPlugloadToHopperFailed(plugloadId);
			PLACED_PLUGLOAD_HOPPER_FAILURE_NUMBER++;
		}
		
	}
	
	ShowPlacedPlugloadToHopperFinalMessage();

}

function ShowPlacedPlugloadToHopperSuccessful(plugloadId) {
	PLACED_PLUGLOAD_HOPPER_RETRY_COUNTER_ARRAY[plugloadId] = 0;
}

function ShowPlacedPlugloadToHopperStatusMessage() {
	
	var remainingPlacedPlugloadToHopper = TOTAL_PLACED_PLUGLOAD_HOPPER_NUMBER - PLACED_PLUGLOAD_HOPPER_SUCCESS_NUMBER;
	
	displayPlacedPlCommissionMessage("Please wait...No of placed plugloads to hoppers success:"+PLACED_PLUGLOAD_HOPPER_SUCCESS_NUMBER+" ,  No of placed plugloads to hoppers remaining:"+remainingPlacedPlugloadToHopper+" "+LOADING_IMAGE_STRING, COLOR_SUCCESS);
		
}

function ShowPlacedPlugloadToHopperFailed(plugloadId) {
	PLACED_PLUGLOAD_HOPPER_RETRY_COUNTER_ARRAY[plugloadId] = 0;
}

function ShowPlacedPlugloadToHopperFinalMessage(){
	
	var currentPlacedPlugloadToHopperCompleted = PLACED_PLUGLOAD_HOPPER_SUCCESS_NUMBER + PLACED_PLUGLOAD_HOPPER_FAILURE_NUMBER;
	
	if(PLACED_PLUGLOAD_HOPPER_FAILURE_NUMBER > 0 && currentPlacedPlugloadToHopperCompleted == TOTAL_PLACED_PLUGLOAD_HOPPER_NUMBER){
		displayPlacedPlCommissionMessage("No of placed plugload(s) sucessfully made as hoppers: "+PLACED_PLUGLOAD_HOPPER_SUCCESS_NUMBER+" ,  No of placed plugload(s) failed to be made as hoppers: "+PLACED_PLUGLOAD_HOPPER_FAILURE_NUMBER, COLOR_SUCCESS);
		if (SELECTED_COMMISSION_PLUGLOADS_TO_HOPPER.length > 0){
			initGatewayToCommissionModeProcess();
		}
	}

	if(PLACED_PLUGLOAD_HOPPER_FAILURE_NUMBER == 0 && currentPlacedPlugloadToHopperCompleted == TOTAL_PLACED_PLUGLOAD_HOPPER_NUMBER){
		displayPlacedPlCommissionMessage("All the placed plugload(s) are sucessfully made as hoppers. Total no of placed plugloads made as hoppers: "+PLACED_PLUGLOAD_HOPPER_SUCCESS_NUMBER, COLOR_SUCCESS);
		if (SELECTED_COMMISSION_PLUGLOADS_TO_HOPPER.length > 0){
			initGatewayToCommissionModeProcess();
		}
	}
}

function initenabledisableCommissionedPlugloadHopper(){
	currentCommissionedPlugloadToHopperNumber = 0;
	COMMISSION_PLUGLOAD_HOPPER_SUCCESS_NUMBER = 0;
	COMMISSION_PLUGLOAD_HOPPER_FAILURE_NUMBER = 0;
	TOTAL_COMMISSIONED_PLUGLOAD_HOPPER_NUMBER = SELECTED_COMMISSION_PLUGLOADS_TO_HOPPER.length;
}

var get_gateway_to_commission_retry_counter = 0;

function initGatewayToCommissionModeProcess(){
	get_gateway_to_commission_retry_counter = 0;
	var urlOption = "gateway/"+ GATEWAY_ID;
	displayPlacedPlCommissionMessage("Bringing gateway to commissioning Mode. Please wait..."+LOADING_IMAGE_STRING, COLOR_SUCCESS);
	$.ajax({
		url: "${exitPlacedCommissionUrl}"+urlOption+"?ts="+new Date().getTime(),
		type: "GET",
		contentType: "application/json; charset=utf-8",
		success: function(data) {
			getGatewayToCommissionMode_Success(COMM_STATUS_INPROGRESS);
		}
	});	
}

function getGatewayToCommissionMode_Success(bStatus) {
	if(bStatus == COMM_STATUS_INPROGRESS) {
		if(get_gateway_to_commission_retry_counter < 4) {
			var gateway_to_commission_retry_timer = setTimeout("getGatewayToCommissionMode_Check()", 1000);
			get_gateway_to_commission_retry_counter++;
		} else {
			alert("Exit commissioning timeout...");
			//exitPlacedPlcdWindow();
		}
	} else {
		//exitPlacedPlcdWindow();
		displayPlacedPlCommissionMessage("Gateway in commissioning mode. Please wait..Hoppers are getting enabled"+LOADING_IMAGE_STRING, COLOR_SUCCESS);
		enabledisableCommissionedPlugloadHopper();
	}
}

function getGatewayToCommissionMode_Check(){
	displayPlacedPlCommissionMessage(get_gateway_to_commission_retry_counter  + ": getting gateway to commissioning mode process. Please wait..."+LOADING_IMAGE_STRING, COLOR_SUCCESS);
	$.ajax({
		url: "${getCommissionStatusUrl}"+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			getGatewayToCommissionMode_Success(1 * data.status);
		},
		error: function() {
			getGatewayToCommissionMode_Success(COMM_ERROR_GW_CH_CHANGE_DEF);
		}
	});
}

function enabledisableCommissionedPlugloadHopper(){
			
	var postCommissionedPlugloadHopperData = "<plugloads>"+"<plugload><id>"+SELECTED_COMMISSION_PLUGLOADS_TO_HOPPER[currentCommissionedPlugloadToHopperNumber].id+"</id></plugload>"+"</plugloads>";
	
	var isEnable = true;
	
	$.ajax({
			url: "${enabledisableHopperUrl}"+isEnable+"?ts="+new Date().getTime(),
			dataType:"json",
			contentType: "application/xml; charset=utf-8",
			type : "POST",
			data : postCommissionedPlugloadHopperData,
			success: function(data){
				getCommissionedPlugloadHopperStatus(SELECTED_COMMISSION_PLUGLOADS_TO_HOPPER[currentCommissionedPlugloadToHopperNumber].id);
				if( currentCommissionedPlugloadToHopperNumber < SELECTED_COMMISSION_PLUGLOADS_TO_HOPPER.length -1 ){
					currentCommissionedPlugloadToHopperNumber++;
					enabledisableCommissionedPlugloadHopper();
				}
			},
			error: function() {
				enabledisableCommissionedPlugloadHopper();
			}
		});
}

function getCommissionedPlugloadHopperStatus(plugloadId){
	$.ajax({
		url: "${getPlugloadHopperStatusUrl}"+"/plugloadId/"+plugloadId+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			getCommissionedPlugloadHopperStatus_Success(data.msg,data.status);
		},
		error: function(){
			getCommissionedPlugloadHopperStatus_Success("","");
		}
	});
	
}

function getCommissionedPlugloadHopperStatus_Success(message,plugloadId){
	
	if(message == "1") {
		
		COMMISSION_PLUGLOAD_HOPPER_SUCCESS_NUMBER++;
		COMMISSION_PLUGLOAD_HOPPER_SUCCESS_ARRAY.push(plugloadId);
		ShowCommissionedPlugloadToHopperSuccessful(plugloadId);
		ShowCommissionedPlugloadToHopperStatusMessage();
		getPlacedCommissionPlanObj("c_placed_pl_floorplan").replaceCommissionedPlugloadWithHopper('floor', '${floorId}', plugloadId);
		
	} else {
				
		if(COMMISSIONED_PLUGLOAD_HOPPER_RETRY_COUNTER_ARRAY[plugloadId] < 10){
			setTimeout("getCommissionedPlugloadHopperStatus("+plugloadId+")", 5000);
			COMMISSIONED_PLUGLOAD_HOPPER_RETRY_COUNTER_ARRAY[plugloadId] = COMMISSIONED_PLUGLOAD_HOPPER_RETRY_COUNTER_ARRAY[plugloadId] + 1;
		}else {
			ShowCommissionedPlugloadToHopperFailed(plugloadId);
			COMMISSION_PLUGLOAD_HOPPER_FAILURE_NUMBER++;
		}
		
	}
	ShowCommissionedPlugloadToHopperFinalMessage();
	
}

function ShowCommissionedPlugloadToHopperSuccessful(plugloadId) {
	COMMISSIONED_PLUGLOAD_HOPPER_RETRY_COUNTER_ARRAY[plugloadId] = 0;
}

function ShowCommissionedPlugloadToHopperStatusMessage() {
	
	var remainingCommissionedPlugloadToHopper = TOTAL_COMMISSIONED_PLUGLOAD_HOPPER_NUMBER - COMMISSION_PLUGLOAD_HOPPER_SUCCESS_NUMBER;
	
	displayPlacedPlCommissionMessage("Please wait...No of commissioned plugloads to hoppers success:"+COMMISSION_PLUGLOAD_HOPPER_SUCCESS_NUMBER+" ,  No of commissioned plugloads to hoppers remaining:"+remainingCommissionedPlugloadToHopper+" "+LOADING_IMAGE_STRING, COLOR_SUCCESS);
		
}

function ShowCommissionedPlugloadToHopperFailed(plugloadId) {
	COMMISSIONED_PLUGLOAD_HOPPER_RETRY_COUNTER_ARRAY[plugloadId] = 0;
}

function ShowCommissionedPlugloadToHopperFinalMessage(){
	
	var currentCommissionedPlugloadToHopperCompleted = COMMISSION_PLUGLOAD_HOPPER_SUCCESS_NUMBER + COMMISSION_PLUGLOAD_HOPPER_FAILURE_NUMBER;
	
	if(COMMISSION_PLUGLOAD_HOPPER_FAILURE_NUMBER > 0 && currentCommissionedPlugloadToHopperCompleted == TOTAL_COMMISSIONED_PLUGLOAD_HOPPER_NUMBER){
		displayPlacedPlCommissionMessage("No of commissioned plugload(s) sucessfully made as hoppers: "+COMMISSION_PLUGLOAD_HOPPER_SUCCESS_NUMBER+" ,  No of commissioned plugload(s) failed to be made as hoppers: "+COMMISSION_PLUGLOAD_HOPPER_FAILURE_NUMBER, COLOR_SUCCESS);
		startGateWaytoDefaultMode();
	}

	if(COMMISSION_PLUGLOAD_HOPPER_FAILURE_NUMBER == 0 && currentCommissionedPlugloadToHopperCompleted == TOTAL_COMMISSIONED_PLUGLOAD_HOPPER_NUMBER){
		displayPlacedPlCommissionMessage("All the commissioned plugload(s) are sucessfully made as hoppers. Total no of plugload(s) which are made as hoppers: "+COMMISSION_PLUGLOAD_HOPPER_SUCCESS_NUMBER, COLOR_SUCCESS);
		startGateWaytoDefaultMode();
	}
}

function ShowAllPlugloadToHopperFinalMessage(){
	
	var currentCommissionedPlugloadToHopperCompleted = COMMISSION_PLUGLOAD_HOPPER_SUCCESS_NUMBER + COMMISSION_PLUGLOAD_HOPPER_FAILURE_NUMBER;
	
	var currentPlacedPlugloadToHopperCompleted = PLACED_PLUGLOAD_HOPPER_SUCCESS_NUMBER + PLACED_PLUGLOAD_HOPPER_FAILURE_NUMBER;
	
	if (SELECTED_COMMISSION_PLUGLOADS_TO_HOPPER.length > 0 && SELECTED_PLACED_PLUGLOADS_TO_HOPPER.length == 0){
		
		if(COMMISSION_PLUGLOAD_HOPPER_FAILURE_NUMBER > 0 && currentCommissionedPlugloadToHopperCompleted == TOTAL_COMMISSIONED_PLUGLOAD_HOPPER_NUMBER){
			displayPlacedPlCommissionMessage("No of commissioned plugload(s) sucessfully made as hoppers: "+COMMISSION_PLUGLOAD_HOPPER_SUCCESS_NUMBER+" ,  No of commissioned plugload(s) failed to be made as hoppers: "+COMMISSION_PLUGLOAD_HOPPER_FAILURE_NUMBER, COLOR_SUCCESS);
		}

		if(COMMISSION_PLUGLOAD_HOPPER_FAILURE_NUMBER == 0 && currentCommissionedPlugloadToHopperCompleted == TOTAL_COMMISSIONED_PLUGLOAD_HOPPER_NUMBER){
			displayPlacedPlCommissionMessage("All the commissioned plugload(s) are sucessfully made as Hoppers. Total no of plugload(s) made as hoppers: "+COMMISSION_PLUGLOAD_HOPPER_SUCCESS_NUMBER, COLOR_SUCCESS);
		}
	}
	
	if (SELECTED_COMMISSION_PLUGLOADS_TO_HOPPER.length > 0 && SELECTED_PLACED_PLUGLOADS_TO_HOPPER.length > 0){
		
		if(currentPlacedPlugloadToHopperCompleted == TOTAL_PLACED_PLUGLOAD_HOPPER_NUMBER && currentCommissionedPlugloadToHopperCompleted == TOTAL_COMMISSIONED_PLUGLOAD_HOPPER_NUMBER){
			displayPlacedPlCommissionMessage("No of placed plugload(s) sucessfully made as hoppers: "+PLACED_PLUGLOAD_HOPPER_SUCCESS_NUMBER+" ,  No of placed plugload(s) failed to made as hoppers: "+PLACED_PLUGLOAD_HOPPER_FAILURE_NUMBER+ " , No of commissioned plugload(s) sucessfully made as hoppers: "+COMMISSION_PLUGLOAD_HOPPER_SUCCESS_NUMBER+" ,  No of commissioned plugloads(s) failed to be made as hoppers: "+COMMISSION_PLUGLOAD_HOPPER_FAILURE_NUMBER, COLOR_SUCCESS);
		}

	}
	
}

function startGateWaytoDefaultMode(){
	
	$.ajax({
		url: "${startGatewayToDefaultModeUrl}"+"gateway/"+GATEWAY_ID+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			var status = (1 * data.status);
			if(status == COMM_STATUS_STARTED) {
				getPlacedCommissioningStatus_BeforeStart();
			} else if(status == DISC_ERROR_INPROGRESS) {
				alert("Discovery is already in progress. Please try later.");
			} else if(status == COMM_ERROR_INPROGRESS) {
				alert("Commissioning is already in progress. Please try later.");
			}
			
		}
	});
	
}


function donePlacedPlcdButtonHandler(){
	var isExit = confirm("Are you sure you wish to exit?");
	if(isExit) {
		exit_placed_commission_retry_counter = 0;
		exitPlacedPlugloadCommissionWindow();
	}
}

function exitPlacedPlugloadCommissionWindow(){
	placed_plugload_commission_retry_counter = 4;
	initExitPlacedPlugloadCommissionProcess();
}

function initExitPlacedPlugloadCommissionProcess(){
	var urlOption = "gateway/"+ GATEWAY_ID;
	displayPlacedPlCommissionMessage("Exiting Commissioning process. Please wait..."+LOADING_IMAGE_STRING, COLOR_SUCCESS);
	$.ajax({
		url: "${exitPlacedCommissionUrl}"+urlOption+"?ts="+new Date().getTime(),
		type: "GET",
		contentType: "application/json; charset=utf-8",
		success: function(data) {
			getPlacedCommissioningStatusAfterExit_Success(COMM_STATUS_INPROGRESS);
		}
	});	
}

function getPlacedCommissioningStatusAfterExit_Success(bStatus) {
	if(bStatus == COMM_STATUS_INPROGRESS) {
		if(exit_placed_commission_retry_counter < 4) {
			var exit_placed_commission_retry_timer = setTimeout("getPlacedCommissioningStatus_AfterExit()", 1000);
			exit_placed_commission_retry_counter++;
		} else {
			alert("Exit commissioning timeout...");
			exitPlacedPlcdWindow();
		}
	} else {
		exitPlacedPlcdWindow();
	}
}

function getPlacedCommissioningStatus_AfterExit(){
	displayPlacedPlCommissionMessage(exit_placed_commission_retry_counter  + ": Exiting commissioning process. Please wait..."+LOADING_IMAGE_STRING, COLOR_SUCCESS);
	$.ajax({
		url: "${getCommissionStatusUrl}"+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			getPlacedCommissioningStatusAfterExit_Success(1 * data.status);
		},
		error: function() {
			getPlacedCommissioningStatusAfterExit_Success(COMM_ERROR_GW_CH_CHANGE_DEF);
		}
	});
}

function exitPlacedPlcdWindow(){
	reloadPlugloadsListIFrame();
  	$("#placedPlugloadCommissioningDialog").dialog("close");
}

function resizePlacedPlugloadCommissionDialog(){
	//resize flash object
	var flashEl = document.getElementById("placed-plcd-flash-container");
	$("#placed-plcd-floorplan-flash").css("height", flashEl.offsetHeight - 2);
	$("#placed-plcd-floorplan-flash").css("width", flashEl.offsetWidth - 1);
	
}

function displayPlacedPlCommissionMessage(Message, Color) {
	$("#placed-plcd-mesaage").html(Message);
	$("#placed-plcd-mesaage").css("color", Color);
}
function clearPlacedPlCommissionMessage() {
	displayPlacedPlCommissionMessage("", COLOR_DEFAULT);
}

function reloadPlugloadsListIFrame(){
	var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("plugloadsFrame");
	ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src;
}

</script>

<body> 
 
<table id="placed-pplcd-main-box" style="width:100%; height:100%;">
	<tr height=0> <td width="100%"></td> <td width="0%"></td> </tr> 
	<tr height="30px">
		<td colspan=2 style="border-bottom: 1px solid #DCDCDC;"> 
		<table style="width:100%;" cellspacing=0 >
		 		<tr>
			 		<td style="width:20%; text-align:left;"><span id="placed-plcd-gateway-name" class="label">Gateway: <c:out value="${gateway.gatewayName}"/></span></td>
			 		<td style="width:73%; text-align:center;"><span id="placed-plcd-mesaage" class="label"></span></td>
			 		<td style="width:8%; text-align:right;"><button id="placed-plcd-done-btn" style="float:right">Done</button></td>
		 		</tr>
		  </table>
		  </td>
	 </tr>
	 <tr height="auto">
	 		<td style="border:1px solid black; height:100%;">
	 		<table cellspacing=0 style="width:100%; height:100%;">
				<tr style="height:30px;">
				 	<td  class="highlightGray" style="border:0px none;">
				 		<div style = "float:left; padding:7px 0 0 2px;" class="label" id="placed-plcd-locationText">Location: <c:out value="${gateway.location}"/></div>
				  	</td>
				</tr>
				<tr style="height:auto;">
				 	<td id="placed-plcd-flash-container" style="vertical-align: top; border:1px solid black;">
				 		<div id="placed-plcd-floorplan-flash" style="height:100%; width:100%;"> </div>
				 	</td>
				</tr>
			</table>
			</td>
			<td></td>
	 </tr>
 </table>

</body>

</html>