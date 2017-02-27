<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<spring:url value="/modules/PlotChartModule.swf" var="placedfixtureplotchartmodule"></spring:url>
<spring:url value="/services/org/fixture/getcommissionstatus" var="getCommissionStatusUrl" scope="request" />
<spring:url value="/services/org/fixture/getplacedfixturecommissionstatus" var="getPlacedFixtureCommissionStatusUrl" scope="request" />
<spring:url value="/services/org/fixture/validateplacedfixture" var="validatePlacedFixtureUrl" scope="request" />
<spring:url value="/services/org/fixture/exitplacedfixturecommission/" var="exitPlacedCommissionUrl" scope="request" />
<spring:url value="/services/org/fixture/op/hopper/" var="enabledisableHopperUrl" scope="request" />
<spring:url value="/services/org/fixture/op/hopper/" var="enabledisablePlacedFixtureHopperUrl" scope="request" />
<spring:url value="/services/org/fixture/getfixturehopperstatus" var="getFixtureHopperStatusUrl" scope="request" />
<spring:url value="/services/org/fixture/commissionplacedfixtures/" var="startGatewayToDefaultModeUrl" scope="request" />



<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
	
	.ui-dialog-title{font-size: 1.4em; font-weight: bolder;}
	.ui-jqgrid tr.ui-row-ltr td {border-right-width: 0;}
	.ui-jqgrid tr.jqgrow td {border-bottom: 1px dotted #CCCCCC;}
	
	#placed-fxcd-main-box tr.ui-state-highlight{background-color: #3399FF !important; color: white !important;}
</style>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Commission Placed Fixtures</title>

<style>
	#placed-fxcd-main-box {width:100%; height:100%;}

	#placed-fxcd-main-box th{text-align:left; border-right:0 none;}
	#placed-fxcd-main-box th span.ui-jqgrid-resize{display:none !important;}
	.label{font-weight: bold; font-size: 0.9em; color: #555555;}
	.highlightGray{background-color: #EEEEEE;}
 	#placed-fxcd-mesaage, #placed-fxcd-locationText, #placed-fxcd-gateway-name{color: #000000; /*padding-left:10px;*/}

	#placed-fxcd-main-box fieldset{border: none;}
	#placed-fxcd-main-box fieldset.form-column-left{float:left;width: 60%;}
	#placed-fxcd-main-box fieldset.form-column-right{float:left;width: 49%;}
	#placed-fxcd-main-box .fieldWrapper{padding-bottom:2px;}
	#placed-fxcd-main-box .fieldPadding{height:4px;}
	#placed-fxcd-main-box .fieldlabel{float:left; height:20px; width: 15%; font-weight: bold;}
	#placed-fxcd-main-box .fieldButton{float:left; height:20px;width: 15%;padding-left: 10px}
	#placed-fxcd-main-box .fieldInput{float:left; height:20px; width: 40%;}
	#placed-fxcd-main-box .text {height:100%; width:100%;}
	#placed-fxcd-main-box .readOnly {border:0px none;}
	
	.placed-fxcd-row-icon{float: left; height: 16px; margin-left: 5px; width: 16px;}
	
	
	#placed-fxcd-Table td {
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

var placed_fixture_commission_retry_counter = 0;
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
	
	SELECTED_COMMISSION_FIXTURES_TO_HOPPER = [];
	
	LOADING_IMAGE_STRING = "<img alt='loading' src='../themes/default/images/ajax-loader_small.gif'>";
		
	getPlacedCommissioningStatus_BeforeStart();
	
	//Init flash object
	var load_placed_fixture_flash_floor = function(nodetype, nodeid) {
		
		
		var FP_data = "";
		
		var buildNumber = "";
		
		var versionString = "<ems:showAppVersion />";
		
		var indexNumber = versionString.lastIndexOf('.', (versionString.length)-1);
		
		if(indexNumber != -1 ){
			buildNumber = versionString.slice(indexNumber+1);
		}else{
			buildNumber = Math.floor(Math.random()*10000001);// For Development Version
		}
		
		var placedfixtureplotchartmodule_url = "${placedfixtureplotchartmodule}"+"?buildNumber="+buildNumber;
		
		
		if ($.browser.msie) {
			FP_data = "<object id='c_placed_fx_floorplan' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='100%'>";
			FP_data +=  "<param name='src' value='" + placedfixtureplotchartmodule_url + "'/>";
			FP_data +=  "<param name='padding' value='0px'/>";
			FP_data +=  "<param name='wmode' value='opaque'/>";
			FP_data +=  "<param name='allowFullScreen' value='true'/>";
			FP_data +=  "<param name='flashVars' value='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION&modeid=PLACED_FIXTURE_COMMISSION'/>";
			FP_data +=  "<embed id='c_placed_fx_floorplan' name='c_placed_fx_floorplan' src='" + placedfixtureplotchartmodule_url + "' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " padding='0px'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION&modeid=PLACED_FIXTURE_COMMISSION'/>";
			FP_data +=  "</object>";
		} else {
			FP_data = "<embed id='c_placed_fx_floorplan' name='c_placed_fx_floorplan' src='" + placedfixtureplotchartmodule_url + "' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " padding='0px'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION&modeid=PLACED_FIXTURE_COMMISSION'/>";
		}
		
		var tabFP =document.getElementById("placed-fxcd-floorplan-flash");
		tabFP.innerHTML = FP_data; 

		// quick fix for the duplicate flash object
		$('div.alt').remove();
	}
	load_placed_fixture_flash_floor('floor', '${floorId}');
	
	$('#placed-fxcd-done-btn').click(function(){donePlacedFxcdButtonHandler();});
	
	
	//Mark un-editable field as readonly
	$('input.readOnly').attr('readonly', 'readonly');
	$('input.readOnly').focus(function() {
		 $(this).blur();
	});
}); //End : Document Ready


function getPlacedCommissioningStatus_BeforeStart(){
	
	placed_fixture_commission_retry_counter = 0;
	
	
	if (SELECTED_COMMISSION_FIXTURES_TO_HOPPER.length > 0 ){
		displayPlacedFxCommissionMessage("Bringing gateway to default mode. Please wait..."+LOADING_IMAGE_STRING, COLOR_SUCCESS);
	}else{
		displayPlacedFxCommissionMessage("Starting commissioning process. Please wait..."+LOADING_IMAGE_STRING, COLOR_SUCCESS);
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
		clearPlacedFxCommissionMessage();
		//TODO this.Timer_UserInactivity
		
		if (SELECTED_COMMISSION_FIXTURES_TO_HOPPER.length > 0 ){
			ShowAllFixtureToHopperFinalMessage();
		}
		
	} else if(bStatus == COMM_ERROR_GW_CH_CHANGE_DEF) {
		alert("Error in commissioning. Not able to move the gateway to default wireless parameters during commissioning. Please try again.");
		exitPlacedFxcdWindow();
	} else {
		GetPlacedCommissioningStatus_Error();
	}
}

function GetPlacedCommissioningStatus_Error(){
	if(placed_fixture_commission_retry_counter < 30) {
		var placed_commission_retry_timer = setTimeout("getPlacedCommissioningStatus_BeforeStart()", 2000);
		placed_fixture_commission_retry_counter++;
	} else {
		alert("Error in starting commissioning. Please try again");
		exitPlacedFxcdWindow();
	}
}


function disableEnterKey(evt)
{
	 var keyCode = evt ? (evt.which ? evt.which : evt.keyCode) : event.keyCode;
     if (keyCode == 13) {
          return false;
     }
}

var SELECTED_PLACED_FIXTURES_TO_COMMISSION = [];

var PLACED_FIXTURE_RETRY_COUNTER_ARRAY = {};

var TOTAL_PLACED_FIXTURE_NUMBER = 0;

var currentPlacedFixtureNumber = 0;

var SELECTED_PLACED_FIXTURES_TO_HOPPER = [];

var SELECTED_COMMISSION_FIXTURES_TO_HOPPER = [];

var COMMISSIONED_FIXTURE_HOPPER_RETRY_COUNTER_ARRAY = {};

var COMMISSION_FIXTURE_HOPPER_SUCCESS_NUMBER = 0;
var COMMISSION_FIXTURE_HOPPER_FAILURE_NUMBER = 0;
var currentCommissionedFixtureToHopperNumber = 0;
var TOTAL_COMMISSIONED_FIXTURE_HOPPER_NUMBER = 0;

var COMMISSION_FIXTURE_HOPPER_SUCCESS_ARRAY = new Array();

var PLACED_FIXTURE_HOPPER_RETRY_COUNTER_ARRAY = {};

var PLACED_FIXTURE_HOPPER_SUCCESS_NUMBER = 0;
var PLACED_FIXTURE_HOPPER_FAILURE_NUMBER = 0;
var currentPlacedFixtureToHopperNumber = 0;
var TOTAL_PLACED_FIXTURE_HOPPER_NUMBER = 0;

var PLACED_FIXTURE_HOPPER_SUCCESS_ARRAY = new Array();

var COMMISSION_FIXTURE_SUCCESS_NUMBER = 0;

var COMMISSION_FIXTURE_FAILURE_NUMBER = 0;

var SELECTED_PLACED_FIXTURES_WHICHARE_HOPPERS = new Array();

function commissionPlacedFixtureDevice(selPlacedFixtures){
	
	SELECTED_PLACED_FIXTURES_TO_COMMISSION = [];
	
	SELECTED_PLACED_FIXTURES_WHICHARE_HOPPERS = new Array();
		
	if(selPlacedFixtures != ""){
		SELECTED_PLACED_FIXTURES_TO_COMMISSION = eval("("+selPlacedFixtures+")");
	}
	
		
	TOTAL_PLACED_FIXTURE_NUMBER = SELECTED_PLACED_FIXTURES_TO_COMMISSION.length;
	
	for(var j=0; j<SELECTED_PLACED_FIXTURES_TO_COMMISSION.length; j++){
		var selectedPlacedFixtureId = SELECTED_PLACED_FIXTURES_TO_COMMISSION[j].id;
		PLACED_FIXTURE_RETRY_COUNTER_ARRAY[selectedPlacedFixtureId] = 0;
		
		if(SELECTED_PLACED_FIXTURES_TO_COMMISSION[j].isHopper == 1){
			SELECTED_PLACED_FIXTURES_WHICHARE_HOPPERS.push(selectedPlacedFixtureId);
		}
	}
		
	displayPlacedFxCommissionMessage("Fixture(s) commissioning in process..."+LOADING_IMAGE_STRING, COLOR_SUCCESS);
	
	currentPlacedFixtureNumber = 0;
	
	COMMISSION_FIXTURE_SUCCESS_NUMBER = 0;
	
	COMMISSION_FIXTURE_FAILURE_NUMBER = 0;
	
	validatePlacedFixtures();
}


function validatePlacedFixtures(){
	
	var postPlacedFixtureData = "<fixtures>"+"<fixture><id>"+SELECTED_PLACED_FIXTURES_TO_COMMISSION[currentPlacedFixtureNumber].id+"</id></fixture>"+"</fixtures>";
	
	var urlData = "/gatewayId/"+GATEWAY_ID;
	
	$.ajax({
		url: "${validatePlacedFixtureUrl}"+urlData+"?ts="+new Date().getTime(),
		dataType:"json",
		type : "POST",
		data : postPlacedFixtureData,
		contentType: "application/xml; charset=utf-8",
		success: function(data){
			getPlacedFixtureCommissionStatus(SELECTED_PLACED_FIXTURES_TO_COMMISSION[currentPlacedFixtureNumber].id);
			if( currentPlacedFixtureNumber < SELECTED_PLACED_FIXTURES_TO_COMMISSION.length -1 ){
				currentPlacedFixtureNumber++;
				validatePlacedFixtures();
			}
		},
		error: function(){
			validatePlacedFixtures();
		}
	});
}


function getPlacedFixtureCommissionStatus(placedCommsionedFixtureId){
	$.ajax({
		url: "${getPlacedFixtureCommissionStatusUrl}"+"/fixtureId/"+placedCommsionedFixtureId+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			getPlacedFixturesCommissionStatus_Success(data.msg,data.status);
		},
		error: function(){
			getPlacedFixturesCommissionStatus_Success("");
		}
	});
	
}

function getPlacedFixturesCommissionStatus_Success(message,placedCommsionedFixtureId){
	if(message.toUpperCase() == COMMISSIONED) {
		
		COMMISSION_FIXTURE_SUCCESS_NUMBER++;
		ShowPlacedFixtureCommissioningSuccessful(placedCommsionedFixtureId);
		ShowPlacedFixtureCommissioningStatusMessage();
		try {
			
			if(SELECTED_PLACED_FIXTURES_WHICHARE_HOPPERS.indexOf(placedCommsionedFixtureId) != -1 ){
				isHopper = true;
				getPlacedCommissionPlanObj("c_placed_fx_floorplan").replacePlacedFixtureWithCommissionedFixture('floor', '${floorId}', placedCommsionedFixtureId,isHopper);
			}else{
				isHopper = false;
				getPlacedCommissionPlanObj("c_placed_fx_floorplan").replacePlacedFixtureWithCommissionedFixture('floor', '${floorId}', placedCommsionedFixtureId,isHopper);
			}
			
		} catch(e){
		
		}
		
	} else {
				
		if(PLACED_FIXTURE_RETRY_COUNTER_ARRAY[placedCommsionedFixtureId] < 10){
			setTimeout("getPlacedFixtureCommissionStatus("+placedCommsionedFixtureId+")", 5000);
			PLACED_FIXTURE_RETRY_COUNTER_ARRAY[placedCommsionedFixtureId] = PLACED_FIXTURE_RETRY_COUNTER_ARRAY[placedCommsionedFixtureId] + 1;
		} else {
			ShowPlacedFixtureCommissioningFailed(placedCommsionedFixtureId);
			COMMISSION_FIXTURE_FAILURE_NUMBER++;
			
		}
		
	}
	showPlacedFixtureCommissionFinalMessage();
}

function ShowPlacedFixtureCommissioningSuccessful(placedCommsionedFixtureId) {
	PLACED_FIXTURE_RETRY_COUNTER_ARRAY[placedCommsionedFixtureId] = 0;
}

function ShowPlacedFixtureCommissioningStatusMessage() {
	
	var remainingPlacedFixture = TOTAL_PLACED_FIXTURE_NUMBER - COMMISSION_FIXTURE_SUCCESS_NUMBER;
	
	displayPlacedFxCommissionMessage("Please wait...no of fixture(s) sucessfully commissioned:"+COMMISSION_FIXTURE_SUCCESS_NUMBER+" ,  no of fixtures remaining:"+remainingPlacedFixture+" "+LOADING_IMAGE_STRING, COLOR_SUCCESS);
		
}

function ShowPlacedFixtureCommissioningFailed(placedCommsionedFixtureId) {
		PLACED_FIXTURE_RETRY_COUNTER_ARRAY[placedCommsionedFixtureId] = 0;
}

function showPlacedFixtureCommissionFinalMessage(){
	var currentFixtureCompleted = COMMISSION_FIXTURE_SUCCESS_NUMBER + COMMISSION_FIXTURE_FAILURE_NUMBER;
	
	if(COMMISSION_FIXTURE_FAILURE_NUMBER > 0 && currentFixtureCompleted == TOTAL_PLACED_FIXTURE_NUMBER){
		displayPlacedFxCommissionMessage("No of fixture(s) sucessfully commissioned: "+COMMISSION_FIXTURE_SUCCESS_NUMBER+" ,  No of fixture(s) failed to Commission: "+COMMISSION_FIXTURE_FAILURE_NUMBER, COLOR_SUCCESS);
		getPlacedCommissionPlanObj("c_placed_fx_floorplan").refreshFixtureData();
	}
	
	if(COMMISSION_FIXTURE_FAILURE_NUMBER == 0 && currentFixtureCompleted == TOTAL_PLACED_FIXTURE_NUMBER){
		displayPlacedFxCommissionMessage("All the fixture(s) are sucessfully commissioned. Total no of fixtures commissioned: "+COMMISSION_FIXTURE_SUCCESS_NUMBER, COLOR_SUCCESS);
		getPlacedCommissionPlanObj("c_placed_fx_floorplan").refreshFixtureData();
	}
}

function enableDisableHopper(selPlacedFixHopper,selCommissionedFixHopper){
	
	SELECTED_PLACED_FIXTURES_TO_HOPPER = [];

	SELECTED_COMMISSION_FIXTURES_TO_HOPPER = [];
	
	if(selPlacedFixHopper != ""){
		SELECTED_PLACED_FIXTURES_TO_HOPPER = eval("("+selPlacedFixHopper+")");
	}
	
	if(selCommissionedFixHopper != ""){
		SELECTED_COMMISSION_FIXTURES_TO_HOPPER = eval("("+selCommissionedFixHopper+")");
	}
	
		
	for(var k=0; k<SELECTED_PLACED_FIXTURES_TO_HOPPER.length; k++){
		var placedFixtureHopperJson = SELECTED_PLACED_FIXTURES_TO_HOPPER[k];
		PLACED_FIXTURE_HOPPER_RETRY_COUNTER_ARRAY[placedFixtureHopperJson.id] = 0;
	}
	
		
	for(var l=0; l<SELECTED_COMMISSION_FIXTURES_TO_HOPPER.length; l++){
		var commissionedFixtureHopperJson = SELECTED_COMMISSION_FIXTURES_TO_HOPPER[l];
		COMMISSIONED_FIXTURE_HOPPER_RETRY_COUNTER_ARRAY[commissionedFixtureHopperJson.id] = 0;
	}
	
	COMMISSION_FIXTURE_HOPPER_SUCCESS_ARRAY = new Array();
	
	PLACED_FIXTURE_HOPPER_SUCCESS_ARRAY = new Array();
	
	initenabledisablePlacedFixtureHopper();
	
	initenabledisableCommissionedFixtureHopper();
	
		
	if ( SELECTED_PLACED_FIXTURES_TO_HOPPER.length > 0){
		startenabledisablePlacedFixtureHopper();
	}else if (SELECTED_COMMISSION_FIXTURES_TO_HOPPER.length > 0){
		initGatewayToCommissionModeProcess();
	}
}

function initenabledisablePlacedFixtureHopper(){
	
	PLACED_FIXTURE_HOPPER_SUCCESS_NUMBER = 0;
	PLACED_FIXTURE_HOPPER_FAILURE_NUMBER = 0;
	currentPlacedFixtureToHopperNumber = 0;
	TOTAL_PLACED_FIXTURE_HOPPER_NUMBER = SELECTED_PLACED_FIXTURES_TO_HOPPER.length;
	
}

function startenabledisablePlacedFixtureHopper(){
	
	displayPlacedFxCommissionMessage("Please wait..Hoppers are getting enabled"+LOADING_IMAGE_STRING, COLOR_SUCCESS);
	
	var postPlacedFixtureHopperData = "<fixtures>"+"<fixture><id>"+SELECTED_PLACED_FIXTURES_TO_HOPPER[currentPlacedFixtureToHopperNumber].id+"</id></fixture>"+"</fixtures>";
	
	var isEnable = true;
	
	$.ajax({
			url: "${enabledisablePlacedFixtureHopperUrl}"+isEnable+"/gw/"+GATEWAY_ID+"?ts="+new Date().getTime(),
			dataType:"json",
			contentType: "application/xml; charset=utf-8",
			type : "POST",
			data : postPlacedFixtureHopperData,
			success: function(data){
				getPlacedFixtureHopperStatus(SELECTED_PLACED_FIXTURES_TO_HOPPER[currentPlacedFixtureToHopperNumber].id);
				if( currentPlacedFixtureToHopperNumber < SELECTED_PLACED_FIXTURES_TO_HOPPER.length -1 ){
					currentPlacedFixtureToHopperNumber++;
					startenabledisablePlacedFixtureHopper();
				}
			},
			error: function() {
				startenabledisablePlacedFixtureHopper();
			}
		});
}

function getPlacedFixtureHopperStatus(fixtureId){
	
	$.ajax({
		url: "${getFixtureHopperStatusUrl}"+"/fixtureId/"+fixtureId+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			getPlacedFixtureHopperStatus_Success(data.msg,data.status);
		},
		error: function(){
			getPlacedFixtureHopperStatus_Success("","");
		}
	});
	
}

function getPlacedFixtureHopperStatus_Success(message,fixtureId){
	
	if(message == "1") {
		
		PLACED_FIXTURE_HOPPER_SUCCESS_NUMBER++;
		PLACED_FIXTURE_HOPPER_SUCCESS_ARRAY.push(fixtureId);
		ShowPlacedFixtureToHopperSuccessful(fixtureId);
		ShowPlacedFixtureToHopperStatusMessage();
		getPlacedCommissionPlanObj("c_placed_fx_floorplan").replacePlacedFixtureWithHopper('floor', '${floorId}', fixtureId);
		
	} else {
				
		if(PLACED_FIXTURE_HOPPER_RETRY_COUNTER_ARRAY[fixtureId] < 10){
			setTimeout("getPlacedFixtureHopperStatus("+fixtureId+")", 5000);
			PLACED_FIXTURE_HOPPER_RETRY_COUNTER_ARRAY[fixtureId] = PLACED_FIXTURE_HOPPER_RETRY_COUNTER_ARRAY[fixtureId] + 1;
		}else {
			ShowPlacedFixtureToHopperFailed(fixtureId);
			PLACED_FIXTURE_HOPPER_FAILURE_NUMBER++;
		}
		
	}
	
	ShowPlacedFixtureToHopperFinalMessage();

}

function ShowPlacedFixtureToHopperSuccessful(fixtureId) {
	PLACED_FIXTURE_HOPPER_RETRY_COUNTER_ARRAY[fixtureId] = 0;
}

function ShowPlacedFixtureToHopperStatusMessage() {
	
	var remainingPlacedFixtureToHopper = TOTAL_PLACED_FIXTURE_HOPPER_NUMBER - PLACED_FIXTURE_HOPPER_SUCCESS_NUMBER;
	
	displayPlacedFxCommissionMessage("Please wait...No of placed fixtures to hoppers success:"+PLACED_FIXTURE_HOPPER_SUCCESS_NUMBER+" ,  No of placed fixtures to hoppers remaining:"+remainingPlacedFixtureToHopper+" "+LOADING_IMAGE_STRING, COLOR_SUCCESS);
		
}

function ShowPlacedFixtureToHopperFailed(fixtureId) {
	PLACED_FIXTURE_HOPPER_RETRY_COUNTER_ARRAY[fixtureId] = 0;
}

function ShowPlacedFixtureToHopperFinalMessage(){
	
	var currentPlacedFixtureToHopperCompleted = PLACED_FIXTURE_HOPPER_SUCCESS_NUMBER + PLACED_FIXTURE_HOPPER_FAILURE_NUMBER;
	
	if(PLACED_FIXTURE_HOPPER_FAILURE_NUMBER > 0 && currentPlacedFixtureToHopperCompleted == TOTAL_PLACED_FIXTURE_HOPPER_NUMBER){
		displayPlacedFxCommissionMessage("No of placed fixture(s) sucessfully made as hoppers: "+PLACED_FIXTURE_HOPPER_SUCCESS_NUMBER+" ,  No of placed fixture(s) failed to be made as hoppers: "+PLACED_FIXTURE_HOPPER_FAILURE_NUMBER, COLOR_SUCCESS);
		if (SELECTED_COMMISSION_FIXTURES_TO_HOPPER.length > 0){
			initGatewayToCommissionModeProcess();
		}
	}

	if(PLACED_FIXTURE_HOPPER_FAILURE_NUMBER == 0 && currentPlacedFixtureToHopperCompleted == TOTAL_PLACED_FIXTURE_HOPPER_NUMBER){
		displayPlacedFxCommissionMessage("All the placed fixture(s) are sucessfully made as hoppers. Total no of placed fixtures made as hoppers: "+PLACED_FIXTURE_HOPPER_SUCCESS_NUMBER, COLOR_SUCCESS);
		if (SELECTED_COMMISSION_FIXTURES_TO_HOPPER.length > 0){
			initGatewayToCommissionModeProcess();
		}
	}
}

function initenabledisableCommissionedFixtureHopper(){
	currentCommissionedFixtureToHopperNumber = 0;
	COMMISSION_FIXTURE_HOPPER_SUCCESS_NUMBER = 0;
	COMMISSION_FIXTURE_HOPPER_FAILURE_NUMBER = 0;
	TOTAL_COMMISSIONED_FIXTURE_HOPPER_NUMBER = SELECTED_COMMISSION_FIXTURES_TO_HOPPER.length;
}

var get_gateway_to_commission_retry_counter = 0;

function initGatewayToCommissionModeProcess(){
	get_gateway_to_commission_retry_counter = 0;
	var urlOption = "gateway/"+ GATEWAY_ID;
	displayPlacedFxCommissionMessage("Bringing gateway to commissioning Mode. Please wait..."+LOADING_IMAGE_STRING, COLOR_SUCCESS);
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
			//exitPlacedFxcdWindow();
		}
	} else {
		//exitPlacedFxcdWindow();
		displayPlacedFxCommissionMessage("Gateway in commissioning mode. Please wait..Hoppers are getting enabled"+LOADING_IMAGE_STRING, COLOR_SUCCESS);
		enabledisableCommissionedFixtureHopper();
	}
}

function getGatewayToCommissionMode_Check(){
	displayPlacedFxCommissionMessage(get_gateway_to_commission_retry_counter  + ": getting gateway to commissioning mode process. Please wait..."+LOADING_IMAGE_STRING, COLOR_SUCCESS);
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

function enabledisableCommissionedFixtureHopper(){
			
	var postCommissionedFixtureHopperData = "<fixtures>"+"<fixture><id>"+SELECTED_COMMISSION_FIXTURES_TO_HOPPER[currentCommissionedFixtureToHopperNumber].id+"</id></fixture>"+"</fixtures>";
	
	var isEnable = true;
	
	$.ajax({
			url: "${enabledisableHopperUrl}"+isEnable+"?ts="+new Date().getTime(),
			dataType:"json",
			contentType: "application/xml; charset=utf-8",
			type : "POST",
			data : postCommissionedFixtureHopperData,
			success: function(data){
				getCommissionedFixtureHopperStatus(SELECTED_COMMISSION_FIXTURES_TO_HOPPER[currentCommissionedFixtureToHopperNumber].id);
				if( currentCommissionedFixtureToHopperNumber < SELECTED_COMMISSION_FIXTURES_TO_HOPPER.length -1 ){
					currentCommissionedFixtureToHopperNumber++;
					enabledisableCommissionedFixtureHopper();
				}
			},
			error: function() {
				enabledisableCommissionedFixtureHopper();
			}
		});
}

function getCommissionedFixtureHopperStatus(fixtureId){
	$.ajax({
		url: "${getFixtureHopperStatusUrl}"+"/fixtureId/"+fixtureId+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			getCommissionedFixtureHopperStatus_Success(data.msg,data.status);
		},
		error: function(){
			getCommissionedFixtureHopperStatus_Success("","");
		}
	});
	
}

function getCommissionedFixtureHopperStatus_Success(message,fixtureId){
	
	if(message == "1") {
		
		COMMISSION_FIXTURE_HOPPER_SUCCESS_NUMBER++;
		COMMISSION_FIXTURE_HOPPER_SUCCESS_ARRAY.push(fixtureId);
		ShowCommissionedFixtureToHopperSuccessful(fixtureId);
		ShowCommissionedFixtureToHopperStatusMessage();
		getPlacedCommissionPlanObj("c_placed_fx_floorplan").replaceCommissionedFixtureWithHopper('floor', '${floorId}', fixtureId);
		
	} else {
				
		if(COMMISSIONED_FIXTURE_HOPPER_RETRY_COUNTER_ARRAY[fixtureId] < 10){
			setTimeout("getCommissionedFixtureHopperStatus("+fixtureId+")", 5000);
			COMMISSIONED_FIXTURE_HOPPER_RETRY_COUNTER_ARRAY[fixtureId] = COMMISSIONED_FIXTURE_HOPPER_RETRY_COUNTER_ARRAY[fixtureId] + 1;
		}else {
			ShowCommissionedFixtureToHopperFailed(fixtureId);
			COMMISSION_FIXTURE_HOPPER_FAILURE_NUMBER++;
		}
		
	}
	ShowCommissionedFixtureToHopperFinalMessage();
	
}

function ShowCommissionedFixtureToHopperSuccessful(fixtureId) {
	COMMISSIONED_FIXTURE_HOPPER_RETRY_COUNTER_ARRAY[fixtureId] = 0;
}

function ShowCommissionedFixtureToHopperStatusMessage() {
	
	var remainingCommissionedFixtureToHopper = TOTAL_COMMISSIONED_FIXTURE_HOPPER_NUMBER - COMMISSION_FIXTURE_HOPPER_SUCCESS_NUMBER;
	
	displayPlacedFxCommissionMessage("Please wait...No of commissioned fixtures to hoppers success:"+COMMISSION_FIXTURE_HOPPER_SUCCESS_NUMBER+" ,  No of commissioned fixtures to hoppers remaining:"+remainingCommissionedFixtureToHopper+" "+LOADING_IMAGE_STRING, COLOR_SUCCESS);
		
}

function ShowCommissionedFixtureToHopperFailed(fixtureId) {
	COMMISSIONED_FIXTURE_HOPPER_RETRY_COUNTER_ARRAY[fixtureId] = 0;
}

function ShowCommissionedFixtureToHopperFinalMessage(){
	
	var currentCommissionedFixtureToHopperCompleted = COMMISSION_FIXTURE_HOPPER_SUCCESS_NUMBER + COMMISSION_FIXTURE_HOPPER_FAILURE_NUMBER;
	
	if(COMMISSION_FIXTURE_HOPPER_FAILURE_NUMBER > 0 && currentCommissionedFixtureToHopperCompleted == TOTAL_COMMISSIONED_FIXTURE_HOPPER_NUMBER){
		displayPlacedFxCommissionMessage("No of commissioned fixture(s) sucessfully made as hoppers: "+COMMISSION_FIXTURE_HOPPER_SUCCESS_NUMBER+" ,  No of commissioned fixture(s) failed to be made as hoppers: "+COMMISSION_FIXTURE_HOPPER_FAILURE_NUMBER, COLOR_SUCCESS);
		startGateWaytoDefaultMode();
	}

	if(COMMISSION_FIXTURE_HOPPER_FAILURE_NUMBER == 0 && currentCommissionedFixtureToHopperCompleted == TOTAL_COMMISSIONED_FIXTURE_HOPPER_NUMBER){
		displayPlacedFxCommissionMessage("All the commissioned fixture(s) are sucessfully made as hoppers. Total no of fixture(s) which are made as hoppers: "+COMMISSION_FIXTURE_HOPPER_SUCCESS_NUMBER, COLOR_SUCCESS);
		startGateWaytoDefaultMode();
	}
}

function ShowAllFixtureToHopperFinalMessage(){
	
	var currentCommissionedFixtureToHopperCompleted = COMMISSION_FIXTURE_HOPPER_SUCCESS_NUMBER + COMMISSION_FIXTURE_HOPPER_FAILURE_NUMBER;
	
	var currentPlacedFixtureToHopperCompleted = PLACED_FIXTURE_HOPPER_SUCCESS_NUMBER + PLACED_FIXTURE_HOPPER_FAILURE_NUMBER;
	
	if (SELECTED_COMMISSION_FIXTURES_TO_HOPPER.length > 0 && SELECTED_PLACED_FIXTURES_TO_HOPPER.length == 0){
		
		if(COMMISSION_FIXTURE_HOPPER_FAILURE_NUMBER > 0 && currentCommissionedFixtureToHopperCompleted == TOTAL_COMMISSIONED_FIXTURE_HOPPER_NUMBER){
			displayPlacedFxCommissionMessage("No of commissioned fixture(s) sucessfully made as hoppers: "+COMMISSION_FIXTURE_HOPPER_SUCCESS_NUMBER+" ,  No of commissioned fixture(s) failed to be made as hoppers: "+COMMISSION_FIXTURE_HOPPER_FAILURE_NUMBER, COLOR_SUCCESS);
		}

		if(COMMISSION_FIXTURE_HOPPER_FAILURE_NUMBER == 0 && currentCommissionedFixtureToHopperCompleted == TOTAL_COMMISSIONED_FIXTURE_HOPPER_NUMBER){
			displayPlacedFxCommissionMessage("All the commissioned fixture(s) are sucessfully made as Hoppers. Total no of fixture(s) made as hoppers: "+COMMISSION_FIXTURE_HOPPER_SUCCESS_NUMBER, COLOR_SUCCESS);
		}
	}
	
	if (SELECTED_COMMISSION_FIXTURES_TO_HOPPER.length > 0 && SELECTED_PLACED_FIXTURES_TO_HOPPER.length > 0){
		
		if(currentPlacedFixtureToHopperCompleted == TOTAL_PLACED_FIXTURE_HOPPER_NUMBER && currentCommissionedFixtureToHopperCompleted == TOTAL_COMMISSIONED_FIXTURE_HOPPER_NUMBER){
			displayPlacedFxCommissionMessage("No of placed fixture(s) sucessfully made as hoppers: "+PLACED_FIXTURE_HOPPER_SUCCESS_NUMBER+" ,  No of placed fixture(s) failed to made as hoppers: "+PLACED_FIXTURE_HOPPER_FAILURE_NUMBER+ " , No of commissioned fixture(s) sucessfully made as hoppers: "+COMMISSION_FIXTURE_HOPPER_SUCCESS_NUMBER+" ,  No of commissioned fixture(s) failed to be made as hoppers: "+COMMISSION_FIXTURE_HOPPER_FAILURE_NUMBER, COLOR_SUCCESS);
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


function donePlacedFxcdButtonHandler(){
	var isExit = confirm("Are you sure you wish to exit?");
	if(isExit) {
		exit_placed_commission_retry_counter = 0;
		exitPlacedFixtureCommissionWindow();
	}
}

function exitPlacedFixtureCommissionWindow(){
	placed_fixture_commission_retry_counter = 4;
	initExitPlacedFixtureCommissionProcess();
}

function initExitPlacedFixtureCommissionProcess(){
	var urlOption = "gateway/"+ GATEWAY_ID;
	displayPlacedFxCommissionMessage("Exiting Commissioning process. Please wait..."+LOADING_IMAGE_STRING, COLOR_SUCCESS);
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
			exitPlacedFxcdWindow();
		}
	} else {
		exitPlacedFxcdWindow();
	}
}

function getPlacedCommissioningStatus_AfterExit(){
	displayPlacedFxCommissionMessage(exit_placed_commission_retry_counter  + ": Exiting commissioning process. Please wait..."+LOADING_IMAGE_STRING, COLOR_SUCCESS);
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

function exitPlacedFxcdWindow(){
	reloadFixturesListIFrame();
  	$("#placedFixtureCommissioningDialog").dialog("close");
}

function resizePlacedFixtureCommissionDialog(){
	//resize flash object
	var flashEl = document.getElementById("placed-fxcd-flash-container");
	$("#placed-fxcd-floorplan-flash").css("height", flashEl.offsetHeight - 2);
	$("#placed-fxcd-floorplan-flash").css("width", flashEl.offsetWidth - 1);
	
}

function displayPlacedFxCommissionMessage(Message, Color) {
	$("#placed-fxcd-mesaage").html(Message);
	$("#placed-fxcd-mesaage").css("color", Color);
}
function clearPlacedFxCommissionMessage() {
	displayPlacedFxCommissionMessage("", COLOR_DEFAULT);
}

function reloadFixturesListIFrame(){
	var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("fixturesFrame");
	ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src;
}

</script>

<body> 
 
<table id="placed-fxcd-main-box" style="width:100%; height:100%;">
	<tr height=0> <td width="100%"></td> <td width="0%"></td> </tr> 
	<tr height="30px">
		<td colspan=2 style="border-bottom: 1px solid #DCDCDC;"> 
		<table style="width:100%;" cellspacing=0 >
		 		<tr>
			 		<td style="width:20%; text-align:left;"><span id="placed-fxcd-gateway-name" class="label">Gateway: <c:out value="${gateway.gatewayName}"/></span></td>
			 		<td style="width:73%; text-align:center;"><span id="placed-fxcd-mesaage" class="label"></span></td>
			 		<td style="width:8%; text-align:right;"><button id="placed-fxcd-done-btn" style="float:right">Done</button></td>
		 		</tr>
		  </table>
		  </td>
	 </tr>
	 <tr height="auto">
	 		<td style="border:1px solid black; height:100%;">
	 		<table cellspacing=0 style="width:100%; height:100%;">
				<tr style="height:30px;">
				 	<td  class="highlightGray" style="border:0px none;">
				 		<div style = "float:left; padding:7px 0 0 2px;" class="label" id="placed-fxcd-locationText">Location: <c:out value="${gateway.location}"/></div>
				  	</td>
				</tr>
				<tr style="height:auto;">
				 	<td id="placed-fxcd-flash-container" style="vertical-align: top; border:1px solid black;">
				 		<div id="placed-fxcd-floorplan-flash" style="height:100%; width:100%;"> </div>
				 	</td>
				</tr>
			</table>
			</td>
			<td></td>
	 </tr>
 </table>

</body>

</html>