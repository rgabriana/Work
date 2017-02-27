<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<spring:url value="/modules/PlotChartModule.swf" var="plotchartmodule"></spring:url>
<spring:url value="/services/org/wds/getcommissionstatus" var="getCommissionStatusUrl" scope="request" />
<spring:url value="/services/org/wds/list/discovered/" var="getAllWdsBySecondaryGatewayUrl" scope="request" />
<spring:url value="/services/org/wds/details/" var="getWdsDetailsUrl" scope="request" />
<spring:url value="/services/org/wds/updateduringcommission" var="getWdsUpdateUrl" scope="request" />
<spring:url value="/services/org/wds/startcommission" var="startWdsUpdateUrl" scope="request" />
<spring:url value="/services/org/wds/validatewds" var="validateWdsUrl" scope="request" />
<spring:url value="/services/org/wds/changeprofile/" var="changeWdsProfileUrl" scope="request" />
<spring:url value="/services/org/wds/exitcommission/" var="exitCommissionUrl" scope="request" />
<spring:url value="/services/org/wds/rma/" var="rmaUrl" scope="request" />
<spring:url value="/services/org/wds/startWdsnetworkdiscovery/" var="startNetworkDiscoveryUrl" scope="request" />



<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
	
	.ui-dialog-title{font-size: 1.4em; font-weight: bolder;}
	.ui-jqgrid tr.ui-row-ltr td {border-right-width: 0;}
	.ui-jqgrid tr.jqgrow td {border-bottom: 1px dotted #CCCCCC;}
	
	#fxcd-main-box tr.ui-state-highlight{background-color: #3399FF !important; color: white !important;}
</style>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Commission and Place EWS</title>

<style>
	#fxcd-main-box {width:100%; height:100%;}

	#fxcd-main-box th{text-align:left; border-right:0 none;}
	#fxcd-main-box th span.ui-jqgrid-resize{display:none !important;}
	.label{font-weight: bold; font-size: 0.9em; color: #555555;}
	.highlightGray{background-color: #EEEEEE;}
 	#fxcd-mesaage, #locationText, #fxcd-gateway-name{color: #000000; /*padding-left:10px;*/}

	form.fxcd-form-body {font-size: 0.9em; padding: 2px 4px 0 4px;}
	#fxcd-main-box fieldset{border: none;}
	#fxcd-main-box fieldset.form-column-left{float:left; width: 50%;}
	#fxcd-main-box fieldset.form-column-right{float:left; width: 50%;}
	#fxcd-main-box .fieldWrapper{padding-bottom:2px;}
	#fxcd-main-box .fieldPadding{height:45px;}
	#fxcd-main-box .fieldlabel{float:left; height:20px; width: 25%; font-weight: bold;}
	#fxcd-main-box .fieldInput{float:left; height:20px; width: 60%;}
	#fxcd-main-box .fieldInputCombo{float:left; height:20px; width: 60.5%;}
	#fxcd-main-box .text {height:100%; width:100%;}
	#fxcd-main-box .readOnly {border:0px none;}
	
	.fxcd-row-icon{float: left; height: 16px; margin-left: 5px; width: 16px;}
	
	.disableButton
	{
		background: #CCCCCC !important;
		border-color :#CCCCCC !important;
		color:#FFFFFF; font-size:13px; clear: both;border:0px;font-weight:bold;
	}
	.enableButton
	{
		color:#FFFFFF;background-color:#5a5a5a; font-size:13px; clear: both;border:0px;font-weight:bold;
	}
</style>
</head>


<script type="text/javascript">
var COMMISSION_TYPE = "${type}";
var GATEWAY_ID  = "${gateway.id}";
var WDS_ID = "${wdsId}";
var IS_BULK_COMMISSION = "${isBulkCommission}" == "false";

var CURRENT_WDS_OBJECT = {};

var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";

var COMM_STATUS_INPROGRESS = 10; 			//Commissioning is in progress	
var COMM_ERROR_GW_CH_CHANGE_DEF = 13; 		//Not able to move the Gateway to default wireless parameters during commissioning.		
var VALIDATED = "VALIDATED";
var PLACED = "PLACED";		
var COMMISSIONED = "COMMISSIONED";
var UNCOMMISSIONED = "UNCOMMISSIONED";
var DISCOVERED = "DISCOVERED";

var PROFILE_OPEN_OFFICE = "Open Office";
var FX_DEFAULT_VAL_PROFILE = "";
var FX_DEFAULT_VAL_BALLAST = "";
var FX_DEFAULT_VAL_LAMP = "";
var FX_DEFAULT_VAL_VOLTAGE = "";

var COMMISSION_STATUS_UNKNOWN = 0;
var COMMISSION_STATUS_COMMUNICATION = 2;
var COMMISSION_STATUS_MOTION = 4;
var COMMISSION_STATUS_DIMMING = 8;
var COMMISSION_STATUS_WIRELESS = 16;

var commission_retry_counter = 0;
var commission_placement_retry_counter = 0;
var wds_data_uncommissioned = [];
var exit_commission_retry_counter = 0;

//Poll Wds list (timer)
var poll_wds_timer;
var poll_wds_timer_is_on=0;

var bRMAInProgress = false;

function getCommissionPlanObj(objectName) {			
	if ($.browser.mozilla) {
		return document[objectName] 
	}
	return document.getElementById(objectName);
}	

function timedPollWds() {
	loadAllWdsBySecondaryGatewayId();
	poll_wds_timer = setTimeout("timedPollWds()", 2000);
}

function startPollWdsTimer(){
	if (!poll_wds_timer_is_on){
		poll_wds_timer_is_on=1;
		timedPollWds();
	}
}

function stopPollWdsTimer(){
	clearTimeout(poll_wds_timer);
	poll_wds_timer_is_on=0;
}

$(document).ready(function() {
	enableDisableUIControls(false);
	getCommissioningStatus_BeforeStart();
	
	//startPollWdsTimer(); 	//UNCOMMENT ME (for testing)
	
	//Init other WDS list grid
	var othr_fx_cnt = 0;
	jQuery("#discwds-wds-list-grid").jqGrid({
		datatype: "local",
		autowidth: true,
		forceFit: true,
		scrollOffset: 0,
	   	colNames:["id", "Others (<span id='othr-wds-cnt'>"+othr_fx_cnt+"</span> EWS)", "",""],
	   	colModel:[
  			{name:'id', index:'id', hidden:true},
			{name:'name', index:'name', width:"70%", sortable:false},
			{name:'action', index:'action', width:"30%", sortable:false,  formatter: otherFxIconRenderer},
			{name:'switchtype', index:'switchtype', hidden:true}
	   	],
	   	multiselect: false,
	   	onSelectRow: function(rowid, status){
	   		//alert("rowid " + rowid);
	   		selectWds(rowid, 2);
      	}
	});
	//Set grid's height to fit into its container
	var otherGridEL = document.getElementById("other-wds-list-container");
	forceFitJQgridHeight(jQuery("#discwds-wds-list-grid"), otherGridEL.offsetHeight);
	
	//Init TabPanel
	$("#fx-form-basic-tab").css("display", "block");
	$("#fx-form-details-tab").css("display", "block");
	$("#wds-form-tabs").tabs({selected: 0});
	
	//Init flash object
	var load_flash_floor = function(nodetype, nodeid) {
		
		var FP_data = "";
		
		var buildNumber = "";
		
		var versionString = "<ems:showAppVersion />";
		
		var indexNumber = versionString.lastIndexOf('.', (versionString.length)-1);
		
		if(indexNumber != -1 ){
			buildNumber = versionString.slice(indexNumber+1);
		}else{
			buildNumber = Math.floor(Math.random()*10000001);// For Development Version
		}
		
		var plotchartmodule_url = "${plotchartmodule}"+"?buildNumber="+buildNumber;
		
		
		if ($.browser.msie) {
			FP_data = "<object id='c_wds_floorplan' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='100%'>";
			FP_data +=  "<param name='src' value='" + plotchartmodule_url + "'/>";
			FP_data +=  "<param name='padding' value='0px'/>";
			FP_data +=  "<param name='wmode' value='opaque'/>";
			FP_data +=  "<param name='allowFullScreen' value='true'/>";
			FP_data +=  "<param name='flashVars' value='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION'/>";
			FP_data +=  "<embed id='c_wds_floorplan' name='c_wds_floorplan' src='" + plotchartmodule_url + "' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " padding='0px'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION'/>";
			FP_data +=  "</object>";
		} else {
			FP_data = "<embed id='c_wds_floorplan' name='c_wds_floorplan' src='" + plotchartmodule_url + "' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " padding='0px'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION'/>";
		}
		
		var tabFP =document.getElementById("fxcd-floorplan-flash");
		tabFP.innerHTML = FP_data; 

		// quick fix for the duplicate flash object
		$('div.alt').remove();
	}
	load_flash_floor('floor', '${floorId}');
	
	//add on click handler
	$('#fxcd-update-btn').click(function(){updateWDS();});
	$('#fxcd-undo-btn').click(function(){undoButtonHandler();});
	$('#fxcd-commission-btn').click(function(){commissionDevice();});
	$('#fxcd-done-btn').click(function(){doneButtonHandler();});
	$('#wdscd-discovery-btn').click(function(){discoveryButtonHandler();});
	
	
	//Mark un-editable field as readonly
	$('input.readOnly').attr('readonly', 'readonly');
	$('input.readOnly').focus(function() {
		 $(this).blur();
	});
}); //End : Document Ready

function getCommissioningStatus_BeforeStart(){
	displayFxCommissionMessage("Starting commissioning process. Please wait...", COLOR_SUCCESS);
	
	$.ajax({
		url: "${getCommissionStatusUrl}"+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			getCommissioningStatus_Success(1 * data.status);
		}
	});
}

function getCommissioningStatus_Success(bStatus){
	
	if(bStatus == COMM_STATUS_INPROGRESS) {
		clearFxCommissionMessage();
		startPollWdsTimer();
		//TODO this.Timer_UserInactivity
	} else if(bStatus == COMM_ERROR_GW_CH_CHANGE_DEF) {
		alert("Error in commissioning. Not able to move the Gateway to default wireless parameters during commissioning. Please try again.");
		exitWindow();
	} else {
		GetCommissioningStatus_Error();
	}
}

function GetCommissioningStatus_Error(){
	if(commission_retry_counter < 4) {
		var commission_retry_timer = setTimeout("getCommissioningStatus_BeforeStart()", 2000);
		commission_retry_counter++;
	} else {
		alert("Error in starting Commissioning. Please try again");
		exitWindow();
	}
}
function otherFxIconRenderer(cellvalue, options, rowObject){
	var cellData = "";
	if(rowObject.state == DISCOVERED) {
		cellData += "<div class='fxcd-row-icon' style=\"background-image: url('/ems/themes/default/images/C.png')\"></div>";
	} else {
		cellData += "<div class='fxcd-row-icon' style=\"background-image: url('/ems/themes/default/images/Cg.png')\"></div>";
	}
	return cellData;
}

function getCommissioningStatus_Success(bStatus){
	
	if(bStatus == COMM_STATUS_INPROGRESS) {
		clearFxCommissionMessage();
		startPollWdsTimer();
		//TODO this.Timer_UserInactivity
	} else if(bStatus == COMM_ERROR_GW_CH_CHANGE_DEF) {
		alert("Error in commissioning. Not able to move the Gateway to default wireless parameters during commissioning. Please try again.");
		exitWindow();
	} else {
		GetCommissioningStatus_Error();
	}
}

function GetCommissioningStatus_Error(){
	if(commission_retry_counter < 4) {
		var commission_retry_timer = setTimeout("getCommissioningStatus_BeforeStart()", 2000);
		commission_retry_counter++;
	} else {
		alert("Error in starting Commissioning. Please try again");
		//exitWindow();
	}
}

function enableDisableUIControls(isEnable){
	if(isEnable){
		//enable form elements
		$("#gatewayChannel").removeAttr("disabled");
		$("#wdsname").removeAttr("disabled");
		//$("#wdstype").removeAttr("disabled");
		
		//enable buttons
		$('#fxcd-update-btn').removeAttr("disabled");
		$('#fxcd-update-btn').removeClass("disableButton");	
		$('#fxcd-update-btn').addClass("enableButton");	
		
		$('#fxcd-undo-btn').removeAttr("disabled");
		$('#fxcd-undo-btn').removeClass("disableButton");	
		$('#fxcd-undo-btn').addClass("enableButton");
		
		$('#fxcd-commission-btn').removeAttr("disabled");
		$('#fxcd-commission-btn').removeClass("disableButton");	
		$('#fxcd-commission-btn').addClass("enableButton");
	} else {
		//disable form elements
		
		$("#gatewayChannel").attr("disabled", true);
		$("#wdsname").attr("disabled", true);
		$("#wdstype").attr("disabled",true);

		//disable buttons
	      
		$('#fxcd-update-btn').attr("disabled", true);
		$('#fxcd-update-btn').removeClass("enableButton");	
		$('#fxcd-update-btn').addClass("disableButton");	
		
		$('#fxcd-undo-btn').attr("disabled", true);
		$('#fxcd-undo-btn').removeClass("enableButton");	
		$('#fxcd-undo-btn').addClass("disableButton");	
		
		$('#fxcd-commission-btn').attr("disabled", true);
		$('#fxcd-commission-btn').removeClass("enableButton");	
		$('#fxcd-commission-btn').addClass("disableButton");	
	}	
}



function loadAllWdsBySecondaryGatewayId(){
	var params = "secondarygateway/"+GATEWAY_ID;
	$.ajax({
		url: "${getAllWdsBySecondaryGatewayUrl}"+params+"?ts="+new Date().getTime(),
		dataType:"xml",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			if(data!=null){
				switchData= parseXml(data);
				if(switchData != undefined){
					var wdses =  [];
					if(switchData.length == undefined) { //FIXME : JSON response issue : For only 1 record it returns Object instead of an Array
						wdses.push(switchData);
						loadAllWDSBySecondaryGatewayId_Success(wdses);
					} else if(switchData.length > 0) {
						loadAllWDSBySecondaryGatewayId_Success(switchData);
					} else {
						loadAllWDSBySecondaryGatewayId_Success(wdses);
					}
				}
			}
		}
	});
}
function parseXml(xml) { 
    var wds = []; 
    $(xml).find("wds").each(function() { 
    	wds.push({ 
    		id: $(this).find("id").text(),
    		name: $(this).find("name").text(),
    		floorid: $(this).find("floorid").text(),
    		campusid: $(this).find("campusid").text(),
    		xaxis: $(this).find("xaxis").text(),
    		yaxis: $(this).find("yaxis").text(),
            state: $(this).find("state").text(),
            gatewayid: $(this).find("gatewayid").text(),
        }); 
    }); 
    return wds; 
} 

function loadAllWDSBySecondaryGatewayId_Success(data){
	wds_data_uncommissioned = [];

	var uncommissionJGrid = jQuery("#discwds-wds-list-grid");

	uncommissionJGrid.jqGrid("clearGridData");
	
	var allWds= data;
	
	$.each(allWds, function(i, wds) {
		if(wds.state != COMMISSIONED){
			clearFxCommissionMessage();	
			wds_data_uncommissioned.push(wds);
			uncommissionJGrid.jqGrid('addRowData', wds_data_uncommissioned.length , wds);
			uncommissionJGrid.jqGrid('setSelection', wds_data_uncommissioned.length + 1, false);
		}
	});
	
	updateWdsCount();
}

function updateWdsCount(){
	$("#othr-wds-cnt").html(wds_data_uncommissioned.length);
}
var currentWds = {};
function selectWds(rowid, gridIndex) {
	clearFxCommissionMessage();
	
	var uncommissionJGrid = jQuery("#discwds-wds-list-grid");
	 if(gridIndex == 2) { // Other Grid
		if(rowid > 0){
			currentWds = uncommissionJGrid.jqGrid('getRowData',rowid); 
		} else {
			displayFxCommissionMessage("Select a EWS from Others EWS list", COLOR_FAILURE);
			return;
		}
	}
	 loadWdsDetails(currentWds.id);
}

function loadWdsDetails(wdsId){
	$.ajax({
		url: "${getWdsDetailsUrl}"+wdsId+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			enableDisableUIControls(true);
			CURRENT_WDS_OBJECT = data; //save into glabal variable
			setWdsBasicInfo(CURRENT_WDS_OBJECT);
		}
	});
}

function setWdsBasicInfo(wds){
	$("#wdsId").val(wds.id);
	$("#wdsname").val(wds.name);
}

function setWdsDetailInfo(wds){
	$("#name").val(wds.name);
}

function validateWDSForm(){
	if(CURRENT_WDS_OBJECT.id == undefined) {
		displayFxCommissionMessage("Select a EWS from the list", COLOR_FAILURE);
		return false;
	}
	var wdsname = $("#wdsname").val();
	WDSName = $.trim(wdsname);
	if(WDSName == ""){
		displayFxCommissionMessage("Enter EWS name", COLOR_FAILURE);
		return false;
	} else {
		clearFxCommissionMessage();
		//TODO duplicate name check
		// 	return false;
	}

	return true;
}

function getWDSXML(){
	//Update Global variable
	CURRENT_WDS_OBJECT.name = $("#wdsname").val();
	
	var xmldata = "<wds>"+
						"<id>"+CURRENT_WDS_OBJECT.id+"</id>"+
						"<name>"+CURRENT_WDS_OBJECT.name+"</name>"+
						"<floorid>"+CURRENT_WDS_OBJECT.floorid+"</floorid>"+
						"<xaxis>"+CURRENT_WDS_OBJECT.xaxis+"</xaxis>"+
						"<yaxis>"+CURRENT_WDS_OBJECT.yaxis+"</yaxis>"+
						"<state>"+CURRENT_WDS_OBJECT.state+"</state>"+
						"<gatewayid>"+CURRENT_WDS_OBJECT.gatewayid+"</gatewayid>"+
					"</wds>";
	//FX_DEFAULT_VAL_PROFILE = CURRENT_WDS_OBJECT.currentprofile;
	return xmldata;	
}

function updateWDS(){
	
	if(!validateWDSForm()){
		return false;
	}
	var xmlData = getWDSXML();
	
	displayFxCommissionMessage("Updating EWS...", COLOR_SUCCESS);
	$.ajax({
		url: "${getWdsUpdateUrl}"+"?ts="+new Date().getTime(),
		type: "POST",
		data: xmlData, 
		dataType:"xml",
		contentType: "application/xml; charset=utf-8",
		success: function(data){
			displayFxCommissionMessage("EWS updated successfully", COLOR_SUCCESS);
			//updateWdsProfile(false);
		}
	});
}

function updateWdsProfile(bCommission) {
	var currentprofile = $("#currentprofile").val();
	if(currentprofile == ""){
		resetDevicePositionOnFloorplan();
		return;		
	}
	
	var groupId = 0;
	if (currentprofile != "Custom") {
		if (currentprofile == "Global") {
			groupId = 0;
		} else {
			groupId = PROFILE_DATA[CURRENT_WDS_OBJECT.currentprofile];
		}
	}
	
	var urlOption = "wdsId/"+ CURRENT_WDS_OBJECT.id 
					+ "/groupId/"+ groupId
					+ "/currentProfile/"+ CURRENT_WDS_OBJECT.currentprofile
					+ "/originalProfile/"+ CURRENT_WDS_OBJECT.originalprofilefrom ;
	$.ajax({
		url: "${changeWdsProfileUrl}"+urlOption+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			if (bCommission) {
				displayFxCommissionMessage("Commissioning EWS, please wait...", COLOR_SUCCESS);
				validateWds();
			}
		}
	});	
}

function commissionDevice(){
	if(!validateWDSForm()){
		resetDevicePositionOnFloorplan();
		return false;
	}
	enableDisableUIControls(false);
	
	var xmlData = getWDSXML();
	
	displayFxCommissionMessage("Updating EWS...", COLOR_SUCCESS);
	$.ajax({
		url: "${getWdsUpdateUrl}",
		type: "POST",
		data: xmlData, 
		dataType:"xml",
		contentType: "application/xml; charset=utf-8",
		success: function(data){
			startCommissioning();
			displayFxCommissionMessage("EWS commissioning in process...", COLOR_SUCCESS);
		},
		error: function() {
			bRMAInProgress = false;
			resetDevicePositionOnFloorplan();
		}
	});
}

function startCommissioning() {
	var xmlData = getWDSXML();
	
	displayFxCommissionMessage("EWS commissioning in process...", COLOR_SUCCESS);
	$.ajax({
		url: "${startWdsUpdateUrl}",
		type: "POST",
		data: xmlData, 
		dataType:"xml",
		contentType: "application/xml; charset=utf-8",
		success: function(data){
			getCommissionPlacementStatus();
		},
		error: function() {
			bRMAInProgress = false;
			resetDevicePositionOnFloorplan();
		}
	});
}

function validateWds(){
	var urlData = "";
	if(IS_BULK_COMMISSION){
		urlData = "/gatewayId/"+GATEWAY_ID;
	}
	urlData += "/wdsId/"+CURRENT_WDS_OBJECT.id ;
	
	$.ajax({
		url: "${validateWdsUrl}"+urlData+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			getCommissionPlacementStatus();
		}
	});
	
	displayFxCommissionMessage("EWS commissioning in process...", COLOR_SUCCESS);
}

function getCommissionPlacementStatus(){
	$.ajax({
		url: "${getCommissionStatusUrl}"+"/wds/"+CURRENT_WDS_OBJECT.id+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			getCommissionPlacementStatus_Success(data.msg);
		},
		error: function(){
			getCommissionPlacementStatus_Success("");
		}
	});
	
	displayFxCommissionMessage(commission_placement_retry_counter+": EWS commissioning in process...", COLOR_SUCCESS);
}

function getCommissionPlacementStatus_Success(status){
	if(status.toUpperCase() == COMMISSIONED) {
		ShowWdsCommissioningSuccessful();
		try {
			displayFxCommissionMessage("EWS commissioned successfully.", COLOR_SUCCESS);
			if(SWITCH_ID != undefined && SWITCH_ID != "")
				getCommissionPlanObj("c_wds_floorplan").addWds('floor', '${floorId}', $("#wdsId").val(), SWITCH_ID, GATEWAY_ID);
			else
				getCommissionPlanObj("c_wds_floorplan").addWds('floor', '${floorId}', $("#wdsId").val(), "", "");
		} catch(e){
			resetDevicePositionOnFloorplan();
			displayFxCommissionMessage("EWS commissioned successfully, but failed to do placement on floor plan.", COLOR_SUCCESS);
		}
		// reset current WDS id
		CURRENT_WDS_OBJECT = {};
		bRMAInProgress = false;
	} else {
		if(commission_placement_retry_counter < 10){
			var commission_placement_retry_timer = setTimeout("getCommissionPlacementStatus()", 2000);
			commission_placement_retry_counter++;
		} else {
			ShowWdsCommissioningFailed();
		}
	}
}

function ShowWdsCommissioningSuccessful() {
	commission_placement_retry_counter = 0;
	displayFxCommissionMessage("EWS commissioned successfully.", COLOR_SUCCESS);
	reloadWdsListIFrame();
}

function resetDevicePositionOnFloorplan() {
	try {
		getCommissionPlanObj("c_wds_floorplan").resetDevicePosition();
	} catch(e){
		// Protect for javascript + flex communication failure
	}
}

function ShowWdsCommissioningFailed() {
//TODO 	this.resetDevicePosition();
	commission_placement_retry_counter = 0;
	displayFxCommissionMessage(" EWS Commissioned Failed", COLOR_FAILURE);
	resetDevicePositionOnFloorplan();
	enableDisableUIControls(true);
	bRMAInProgress = false;
	alert("Could not communicate with EWS using the provided EWS ID. \n\nTry the following: \n- Verify that the EWS name is entered \n- Check that the EWS has power \n- Check that the EM server is connected to the 'building lighting network"); //TODO : message
}

function undoButtonHandler(){
	setWdsBasicInfo(CURRENT_WDS_OBJECT);
}

function doneButtonHandler(){
	var isExit = confirm("Are you sure you wish to exit?");
	if(isExit) {
		exit_commission_retry_counter = 0;
		exitWdsCommissionWindow();
	}
}

function discoveryButtonHandler() {
	var floorId = treenodeid; //selected tree node id (floor id)

	$.ajax({
		url: "${startNetworkDiscoveryUrl}floor/"+floorId+"/gateway/"+GATEWAY_ID+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			if (data.status == 0) {
				displayFxCommissionMessage("Discovery initiated, please wait...");
			}else if (data.status == 1) {
				displayFxCommissionMessage("Discovery in progress...");
			}else {
				displayFxCommissionMessage("Please restart EWS commissioning.");
			}
		}
	});
}

function initExitCommissionProcess(){
	var urlOption = "gateway/"+ GATEWAY_ID;
	displayFxCommissionMessage("Exiting commissioning process. Please wait...", COLOR_SUCCESS);
	$.ajax({
		url: "${exitCommissionUrl}"+urlOption+"?ts="+new Date().getTime(),
		type: "GET",
		contentType: "application/json; charset=utf-8",
		success: function(data) {
			getCommissioningStatusAfterExit_Success(COMM_STATUS_INPROGRESS);
		}
	});	
}

function getCommissioningStatusAfterExit_Success(bStatus) {
	if(bStatus == COMM_STATUS_INPROGRESS) {
		if(exit_commission_retry_counter < 4) {
			var exit_commission_retry_timer = setTimeout("getCommissioningStatus_AfterExit()", 1000);
			exit_commission_retry_counter++;
		} else {
			alert("Exit commissioning timeout...");
			exitWindow();
		}
	} else {
		exitWindow();
	}
}

function getCommissioningStatus_AfterExit(){
	displayFxCommissionMessage(exit_commission_retry_counter  + ": Exiting commissioning process. Please wait...", COLOR_SUCCESS);
	$.ajax({
		url: "${getCommissionStatusUrl}"+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			getCommissioningStatusAfterExit_Success(1 * data.status);
		},
		error: function() {
			getCommissioningStatusAfterExit_Success(COMM_ERROR_GW_CH_CHANGE_DEF);
		}
	});
}


function exitWdsCommissionWindow(){
	commission_retry_counter = 4;
	stopPollWdsTimer();
	// This will initiate finish commissioning
	initExitCommissionProcess();
}

function exitWindow(){
  	$("#wdsCommissioningDialog").dialog("close");
}

function resizeWdsCommissionDialog(){
	//resize flash object
	var flashEl = document.getElementById("fxcd-flash-container");
	$("#fxcd-floorplan-flash").css("height", flashEl.offsetHeight - 2);
	$("#fxcd-floorplan-flash").css("width", flashEl.offsetWidth - 1);
	
	//resize other WDS grid
	var otherGridEL = document.getElementById("other-wds-list-container");
	forceFitJQgridHeight(jQuery("#discwds-wds-list-grid"), otherGridEL.offsetHeight);
	jQuery("#discwds-wds-list-grid").setGridWidth(otherGridEL.offsetWidth);
}

function forceFitJQgridHeight(jgrid, containerHeight){
	var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
	var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
	var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
// 	alert(containerHeight + ">>" + Math.floor((containerHeight - gridHeaderFooterHeight) * .99));
	jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - gridHeaderFooterHeight) * .99)); 
}

function displayFxCommissionMessage(Message, Color) {
	$("#fxcd-mesaage").html(Message);
	$("#fxcd-mesaage").css("color", Color);
}
function clearFxCommissionMessage() {
	displayFxCommissionMessage("", COLOR_DEFAULT);
}

function reloadWdsListIFrame(){
	var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("switchesFrame");
	ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src;
}
</script>

<body> 
 <table id="fxcd-main-box" style="width:100%; height:100%;">
	 <tr height=0> <td width="20%"></td> <td width="80%"></td> </tr>
	 <tr height="30px">
		 <td colspan=2 style="border-bottom: 1px solid #DCDCDC;">
		 	<table style="width:100%;" cellspacing=0 >
		 		<tr>
			 		<td style="width:20%; text-align:left;"><span id="fxcd-gateway-name" class="label">Gateway: <c:out value="${gateway.gatewayName}"/></span></td>
			 		<td style="width:20%; text-align:left;"><span id="wdscd-gateway-discovey" class="label"><button id="wdscd-discovery-btn" style="float:left">Start Discovery</button></span></td>
			 		<td style="width:53%; text-align:center;"><span id="fxcd-mesaage" class="label"></span></td>
			 		<td style="width:8%; text-align:right;"><button id="fxcd-done-btn" style="float:right">Done</button></td>
		 		</tr>
		 	</table>
		 </td>
	 </tr>
	 <tr height="18px">
		 <td class="highlightGray"><div class="label">Available EWS</div></td>
		 <td class="highlightGray"><div class="label">EWS Configuration</div></td>		 			 
	 </tr>
	 <tr height="auto">
		 <td style="border:1px solid black; height:100%;">
		  	<table cellspacing=0 style="width:100%; height:100%;">
		  		
				<tr style="height:100%;">
				 	<td id="other-wds-list-container" style="vertical-align: top; border-top:1px solid black;">
						<table id="discwds-wds-list-grid"></table>
					</td>
				</tr>
			</table>
		 </td>
		 <td style="height:100%;">
		 	<table cellspacing=0 style="width:100%; height:100%;">		 	
				<tr style="height:136px;">
				 	<td style="vertical-align: top; border:1px solid black;">
						<div id="wds-form-tabs">
							<ul>
								<li><a href="#fx-form-basic-tab">Basic</a></li>
								<li><a href="#fx-form-details-tab" style="visibility:hidden;">Details</a></li>
							</ul>
							<div id="fx-form-basic-tab" class="tab-container" style="display:none;">
								<form id="fx-basic-form" class="fxcd-form-body">
									<input type="hidden" id="wdsId"/>
									
									<fieldset class="form-column-left">
										<div class="fieldWrapper">
											<div class="fieldlabel"><label for="wdsname">Name:</label></div>
											<div class="fieldInput"><input class="text readonly" id="wdsname" name="wdsname" size="40" /></div>
											<br style="clear:both;"/>
										</div>										
										<div class="fieldPadding"></div>
										<!-- 
										<div class="fieldWrapper">
											<div class="fieldlabel"><label for="wdstype">Switch Type:</label></div>
											<div class="fieldInput"> <input class="text readonly" id="wdstype" name="wdstype" value="${currentWds.switchType}" size="40" /></div>
											<br style="clear:both;"/>
										</div>
										 -->
									</fieldset>
									<fieldset class="form-column-rigth">
									<!-- 
										<div class="fieldWrapper">
											<div class="fieldlabel"><label for="gatewayChannel">Gateway Channel:</label></div>
											<div class="fieldInput"> <input class="text readonly" id="gatewayChannel" name="gatewayChannel" value="${gateway.channel}" size="40" /></div>
											<br style="clear:both;"/>
										</div>
										-->
										<!-- 
										<div class="fieldWrapper">
											<div class="fieldlabel"><label for="wdstype">EWS Type:</label></div>
											<div class="fieldInput"> <input class="text readonly" id="wdstype" name="wdstype" value="Switch Group" size="40" /></div>
											<br style="clear:both;"/>
										</div>
										 -->
										<div class="fieldPadding"></div>
									</fieldset>
								</form>
							</div>

						</div>
				 	</td>
				</tr>
				<tr style="height:30px;">
				 	<td  class="highlightGray" style="border:2px none;">
				 		<div style = "float:left; padding:7px 0 0 2px;" class="label" id="locationText">Location: <c:out value="${gateway.location}"/></div>
				 		<span  style="float:right;">
					 		<button id="fxcd-update-btn">Update</button>
					 		<button id="fxcd-undo-btn" style="display: none">Undo</button>
					 		<button id="fxcd-commission-btn">Commission and place</button>
				 		</span>
				 	</td>
				</tr>
				<tr style="height:auto;">
				 	<td id="fxcd-flash-container" style="vertical-align: top; border:1px solid black;">
				 		<div id="fxcd-floorplan-flash" style="height:100%; width:100%;"> </div>
				 	</td>
				</tr>
			</table>
		 </td>
	 </tr>
 </table>
</body>
</html>