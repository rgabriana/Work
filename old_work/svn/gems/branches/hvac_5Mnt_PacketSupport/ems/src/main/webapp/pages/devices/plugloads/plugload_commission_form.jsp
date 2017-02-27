<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<spring:url value="/modules/PlotChartModule.swf" var="plotchartmodule"></spring:url>
<spring:url value="/services/org/plugload/getcommissionstatus" var="getCommissionStatusUrl" scope="request" />
<spring:url value="/services/org/plugload/list/" var="getAllPlugloadBySecondaryGatewayUrl" scope="request" />
<spring:url value="/services/org/plugload/details/" var="getPlugloadDetailsUrl" scope="request" />
<spring:url value="/services/org/plugload/updateduringcommission/" var="getPlugloadUpdateUrl" scope="request" />
<spring:url value="/services/org/plugload/startplugloadcommissioning" var="startPlugloadUpdateUrl" scope="request" />
<spring:url value="/services/org/plugload/commissionplugload" var="commissionedPlugload" scope="request" />

<spring:url value="/services/org/plugload/changeprofile/" var="changePlugloadProfileUrl" scope="request" />
<spring:url value="/services/org/plugload/exitcommission/" var="exitCommissionUrl" scope="request" />
<spring:url value="/services/org/plugload/rma/" var="rmaUrl" scope="request" />

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
	
	.ui-dialog-title{font-size: 1.4em; font-weight: bolder;}
	.ui-jqgrid tr.ui-row-ltr td {border-right-width: 0;}
	.ui-jqgrid tr.jqgrow td {border-bottom: 1px dotted #CCCCCC;}
	
	#plcd-main-box tr.ui-state-highlight{background-color: #3399FF !important; color: white !important;}	
</style>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Commission and Place Plugload</title>

<style>
	#plcd-main-box {width:100%; height:100%;}

	#plcd-main-box th{text-align:left; border-right:0 none;}
	#plcd-main-box th span.ui-jqgrid-resize{display:none !important;}
	.label{font-weight: bold; font-size: 0.9em; color: #555555;}
	.highlightGray{background-color: #EEEEEE;}
 	#plcd-mesaage, #locationText, #plcd-gateway-name{color: #000000; /*padding-left:10px;*/}

	form.plcd-form-body {font-size: 0.9em; padding: 2px 4px 0 4px;}
	#plcd-main-box fieldset{border: none;}
	#plcd-main-box fieldset.form-column-left{float:left; width: 50%;}
	#plcd-main-box fieldset.form-column-right{float:left; width: 50%;}
	#plcd-main-box .fieldWrapper{padding-bottom:2px;}
	#plcd-main-box .fieldPadding{height:45px;}
	#plcd-main-box .fieldlabel{float:left; height:20px; width: 25%; font-weight: bold;}
	#plcd-main-box .fieldInput{float:left; height:20px; width: 60%;}
	#plcd-main-box .fieldInputCombo{float:left; height:20px; width: 60.5%;}
	#plcd-main-box .text {height:100%; width:100%;}
	#plcd-main-box .readOnly {border:0px none;}
	#plcd-main-box .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}
	.plcd-row-icon{float: left; height: 16px; margin-left: 5px; width: 16px;}
	
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
var PLUGLOAD_ID = "${plugloadId}";
var IS_BULK_COMMISSION = "${isBulkCommission}" == "true";

var CURRENT_PL_OBJECT = {};
var PROFILE_DATA = {}; //JSON Object: key=profile_name, value=profile_id
var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";

var DISC_STATUS_SUCCESS = 1; 				//All the nodes are discovered
var DISC_STATUS_STARTED = 2; 				//Discovery started		
var DISC_STATUS_INPROGRESS = 3; 			//Discovery is in progress		
var DISC_ERROR_INPROGRESS = 4; 				//Discovery is already in progress
var DISC_ERROR_GW_CH_CHANG_DEF = 5; 		//Not able to move the Gateway to default wireless parameters during discovery		
var DISC_ERROR_TIMED_OUT = 6; 				//Not able to find all the nodes within 3 minute timeout.			
var COMM_STATUS_INPROGRESS = 10; 			//Commissioning is in progress	
var COMM_ERROR_GW_CH_CHANGE_DEF = 13; 		//Not able to move the Gateway to default wireless parameters during commissioning.		
var VALIDATED = "VALIDATED";
var PLACED = "PLACED";		
var COMMISSIONED = "COMMISSIONED";
var UNCOMMISSIONED = "UNCOMMISSIONED";
var DISCOVERED = "DISCOVERED";

var PROFILE_PLUGLOAD_DEFAULT = "Default";
var IS_PROFILE_PLUGLOAD_DEFAULT_PRESENT = false;
var PL_DEFAULT_VAL_PROFILE = "";

var COMMISSION_STATUS_UNKNOWN = 0;
var COMMISSION_STATUS_COMMUNICATION = 2;
var COMMISSION_STATUS_MOTION = 4;
var COMMISSION_STATUS_DIMMING = 8;
var COMMISSION_STATUS_WIRELESS = 16;

var commission_retry_counter = 0;
var commission_placement_retry_counter = 0;
var plugload_data_uncommissioned = [];
var exit_commission_retry_counter = 0;

//Poll Plugload list (timer)
var poll_plugload_timer;
var poll_plugload_timer_is_on=0;

var bRMAInProgress = false;

function getCommissionPlanObj(objectName) {			
	if ($.browser.mozilla) {
		return document[objectName] 
	}
	return document.getElementById(objectName);
}	

function timedPollPlugload() {
	loadAllPlugloadBySecondaryGatewayId();
	poll_plugload_timer = setTimeout("timedPollPlugload()", 2000);
}

function startPollPlugloadTimer(){
	if (!poll_plugload_timer_is_on){
		poll_plugload_timer_is_on=1;
		timedPollPlugload();
	}
}

function stopPollPlugloadTimer(){
	clearTimeout(poll_plugload_timer);
	poll_plugload_timer_is_on=0;
}

function enableDiscoveryButton(isEnable){
	if(isEnable == true){
		$('#plugloadcd-discovery-btn').removeAttr("disabled");
		$('#plugloadcd-discovery-btn').removeClass("disableButton");	
		$('#plugloadcd-discovery-btn').addClass("enableButton");	
	    $('#plugloadcd-discovery-btn').css('background-color', 'red');
	}
	else
	{
		// Disable the "Start discovery" button
		$('#plugloadcd-discovery-btn').attr("disabled", true);
		$('#plugloadcd-discovery-btn').removeClass("enableButton");	
		$('#plugloadcd-discovery-btn').addClass("disableButton");	
	    $('#plugloadcd-discovery-btn').css('background-color', 'grey');
	}
}

$(document).ready(function() {
	enableDisableUIControls(false);
	
	getCommissioningStatus_BeforeStart();
	getAllPlugloadProfiles();
	//startPollPlugloadTimer(); 	//UNCOMMENT ME (for testing)
	
	//Init other Plugload list grid
	var othr_fx_cnt = 0;
	jQuery("#discplugload-plugload-list-grid").jqGrid({
		datatype: "local",
		autowidth: true,
		forceFit: true,
		scrollOffset: 0,
	   	colNames:["id", "Discovered (<span id='othr-plugload-cnt'>"+othr_fx_cnt+"</span> Plugload)", "",""],
	   	colModel:[
  			{name:'id', index:'id', hidden:true},
			{name:'name', index:'name', width:"70%", sortable:false},
			{name:'action', index:'action', width:"30%", sortable:false,  formatter: otherPlIconRenderer},
			{name:'switchtype', index:'switchtype', hidden:true}
	   	],
	   	multiselect: false,
	   	onSelectRow: function(rowid, status){
	   		//alert("rowid " + rowid);
	   		selectPlugload(rowid, 2);
      	}
	});
	//Set grid's height to fit into its container
	var otherGridEL = document.getElementById("other-plugload-list-container");
	forceFitJQgridHeight(jQuery("#discplugload-plugload-list-grid"), otherGridEL.offsetHeight);
	
	//Init TabPanel
	$("#pl-form-basic-tab").css("display", "block");
	$("#pl-form-details-tab").css("display", "block");
	$("#plugload-form-tabs").tabs({selected: 0});
	
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
			FP_data = "<object id='c_plugload_floorplan' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='100%'>";
			FP_data +=  "<param name='src' value='" + plotchartmodule_url + "'/>";
			FP_data +=  "<param name='padding' value='0px'/>";
			FP_data +=  "<param name='wmode' value='opaque'/>";
			FP_data +=  "<param name='allowFullScreen' value='true'/>";
			FP_data +=  "<param name='flashVars' value='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION&modeid=PLUGLOAD_COMMISSION&enablePlugloadFeature=${enablePlugloadProfileFeature}'/>";
			FP_data +=  "<embed id='c_plugload_floorplan' name='c_plugload_floorplan' src='" + plotchartmodule_url + "' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " padding='0px'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION&modeid=PLUGLOAD_COMMISSION&enablePlugloadFeature=${enablePlugloadProfileFeature}'/>";
			FP_data +=  "</object>";
		} else {
			FP_data = "<embed id='c_plugload_floorplan' name='c_plugload_floorplan' src='" + plotchartmodule_url + "' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " padding='0px'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION&modeid=PLUGLOAD_COMMISSION&enablePlugloadFeature=${enablePlugloadProfileFeature}'/>";
		}
		
		var tabFP =document.getElementById("plcd-floorplan-flash");
		tabFP.innerHTML = FP_data; 

		// quick fix for the duplicate flash object
		$('div.alt').remove();
	}
	load_flash_floor('floor', '${floorId}');
	
	//add on click handler
	$('#plcd-update-btn').click(function(){updatePlugload();});
	$('#plcd-undo-btn').click(function(){undoButtonHandler();});
	$('#plcd-commission-btn').click(function(){commissionDevice();});
	$('#plcd-done-btn').click(function(){doneButtonHandler();});
	
	
	//Mark un-editable field as readonly
	$('input.readOnly').attr('readonly', 'readonly');
	$('input.readOnly').focus(function() {
		 $(this).blur();
	});
}); //End : Document Ready

function getAllPlugloadProfiles(){
	PROFILE_DATA = {};
	<c:forEach items="${groups}" var="group">
		PROFILE_DATA["${group.name}"] = "${group.id}";
	</c:forEach>
}

function getCommissioningStatus_BeforeStart(){
	displayPlCommissionMessage("Starting commissioning process. Please wait...", COLOR_SUCCESS);
	
	$.ajax({
		url: "${getCommissionStatusUrl}"+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			getCommissioningStatus_Success(1 * data.status);
		}
	});
}


function otherPlIconRenderer(cellvalue, options, rowObject){
	var cellData = "";
	if(rowObject.state == DISCOVERED) {
		cellData += "<div class='plcd-row-icon' style=\"background-image: url('/ems/themes/default/images/C.png')\"></div>";
	} else {
		cellData += "<div class='plcd-row-icon' style=\"background-image: url('/ems/themes/default/images/Cg.png')\"></div>";
	}
	return cellData;
}

function getCommissioningStatus_Success(bStatus){
	
	if(bStatus == COMM_STATUS_INPROGRESS) {
		clearPlCommissionMessage();
		startPollPlugloadTimer();
		//TODO this.Timer_UserInactivity
		enableDiscoveryButton(true);
	} else if(bStatus == COMM_ERROR_GW_CH_CHANGE_DEF) {
		alert("Error in commissioning. Not able to move the Gateway to default wireless parameters during commissioning. Please try again.");
		exitPlCWindow();
	} else {
		GetCommissioningStatus_Error();
	}
}

function GetCommissioningStatus_Error(){
	if(commission_retry_counter < 30) {
		var commission_retry_timer = setTimeout("getCommissioningStatus_BeforeStart()", 2000);
		commission_retry_counter++;
	} else {
		alert("Error in starting Commissioning. Please try again");
		exitPlCWindow();
	}
}

function enableDisableUIControls(isEnable){
	if(isEnable){
		//enable form elements
		$("#gatewayChannel").removeAttr("disabled");
		$("#plugloadname").removeAttr("disabled");
		
		//enable buttons
		$('#plcd-update-btn').removeAttr("disabled");
		$('#plcd-update-btn').removeClass("disableButton");	
		$('#plcd-update-btn').addClass("enableButton");	
		
		$('#plcd-undo-btn').removeAttr("disabled");
		$('#plcd-undo-btn').removeClass("disableButton");	
		$('#plcd-undo-btn').addClass("enableButton");
		
		$('#plcd-commission-btn').removeAttr("disabled");
		$('#plcd-commission-btn').removeClass("disableButton");	
		$('#plcd-commission-btn').addClass("enableButton");
	} else {
		//disable form elements
		
		$("#gatewayChannel").attr("disabled", true);
		$("#plugloadname").attr("disabled", true);

		//disable buttons
	      
		$('#plcd-update-btn').attr("disabled", true);
		$('#plcd-update-btn').removeClass("enableButton");	
		$('#plcd-update-btn').addClass("disableButton");	
		
		$('#plcd-undo-btn').attr("disabled", true);
		$('#plcd-undo-btn').removeClass("enableButton");	
		$('#plcd-undo-btn').addClass("disableButton");	
		
		$('#plcd-commission-btn').attr("disabled", true);
		$('#plcd-commission-btn').removeClass("enableButton");	
		$('#plcd-commission-btn').addClass("disableButton");	
	}	
}



function loadAllPlugloadBySecondaryGatewayId(){
	var params = "secondarygateway/"+GATEWAY_ID+"/";
	$.ajax({
		url: "${getAllPlugloadBySecondaryGatewayUrl}"+params+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			if(data!=null){
				plData = data.plugload;
				if(plData != undefined){
					if(plData.length == undefined){ //FIXME : JSON response issue : For only 1 record it returns Object instead of an Array
						var dataWrapper = {};
						dataWrapper.plugload = [];
						dataWrapper.plugload.push(plData);
						loadAllPlugloadBySecondaryGatewayId_Success(dataWrapper);
					} else if(plData.length > 0){
						loadAllPlugloadBySecondaryGatewayId_Success(data);
					}
				}
			}
		}
	});
}

function loadAllPlugloadBySecondaryGatewayId_Success(data){
	plugload_data_uncommissioned = [];

	var uncommissionJGrid = jQuery("#discplugload-plugload-list-grid");

	uncommissionJGrid.jqGrid("clearGridData");
	
	var allPlugload= data.plugload;
	
	$.each(allPlugload, function(i, plugload) {
		if(plugload.state != COMMISSIONED){
			if(IS_BULK_COMMISSION || (!IS_BULK_COMMISSION && PLUGLOAD_ID == plugload.id)){
			var markSelected = false;
			if(CURRENT_PL_OBJECT.id != undefined && CURRENT_PL_OBJECT.id == plugload.id){
				 markSelected = true;				 
			}			
			clearPlCommissionMessage();	
			plugload_data_uncommissioned.push(plugload);
			uncommissionJGrid.jqGrid('addRowData', plugload_data_uncommissioned.length+1 , plugload);
			if(markSelected){				
			uncommissionJGrid.jqGrid('setSelection', plugload_data_uncommissioned.length+1, false);			
			}
		  }
		}
	});
	
	updatePlugloadCount();
}

function updatePlugloadCount(){
	$("#othr-plugload-cnt").html(plugload_data_uncommissioned.length);
}
var currentPlugload = {};
function selectPlugload(rowid, gridIndex) {
	clearPlCommissionMessage();
	
	var uncommissionJGrid = jQuery("#discplugload-plugload-list-grid");
	 if(gridIndex == 2) { // Other Grid
		if(rowid > 0){
			currentPlugload = uncommissionJGrid.jqGrid('getRowData',rowid); 
		} else {
			displayPlCommissionMessage("Select a Plugload from Available Plugload list", COLOR_FAILURE);
			return;
		}
	}
	 loadPlugloadDetails(currentPlugload.id);
}

function loadPlugloadDetails(plugloadId){
	$.ajax({
		url: "${getPlugloadDetailsUrl}"+plugloadId+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			enableDisableUIControls(true);
			CURRENT_PL_OBJECT = data; //save into glabal variable
			setPlugloadBasicInfo(CURRENT_PL_OBJECT);
		}
	});
}

function setPlugloadBasicInfo(plugload){
	$("#plugloadId").val(plugload.id);
	$("#plugloadname").val(plugload.name);
	
	
	if(PL_DEFAULT_VAL_PROFILE == ""){
		PL_DEFAULT_VAL_PROFILE = PROFILE_PLUGLOAD_DEFAULT;
	}
	
	<c:forEach items="${groups}" var="group">
		<c:if test="${group.name == 'Default'}">
		IS_PROFILE_PLUGLOAD_DEFAULT_PRESENT = true;
		</c:if>
	</c:forEach>
	if(!IS_PROFILE_PLUGLOAD_DEFAULT_PRESENT){
		PL_DEFAULT_VAL_PROFILE = $("#currentprofile").val();
	}
	
	if(PL_DEFAULT_VAL_PROFILE != ""){
		$("#currentprofile").val(PL_DEFAULT_VAL_PROFILE);
	} else {
		$("#currentprofile").val(plugload.currentProfile); //Profile
	}
	
}


function validatePlugloadForm(){	
	//Validate the Plugload Name
	if(!validatePlugloadName())
		{
		return false;		
		}	
	if(CURRENT_PL_OBJECT.id == undefined) {
		displayPlCommissionMessage("Select a Plugload from the list", COLOR_FAILURE);
		return false;
	}
	var plugloadname = $("#plugloadname").val();
	plugloadname = $.trim(plugloadname);
	if(plugloadname == ""){
		displayPlCommissionMessage("Enter Plugload name", COLOR_FAILURE);
		return false;
	} else {
		clearPlCommissionMessage();		
		clearPlugloadMessage();
		//TODO duplicate name check
		// 	return false;
	}

	return true;
}

function getPlugloadXML(){
	//Update Global variable
	CURRENT_PL_OBJECT.name = $("#plugloadname").val();
	CURRENT_PL_OBJECT.originalprofilefrom = CURRENT_PL_OBJECT.currentProfile;
	CURRENT_PL_OBJECT.currentprofile = $("#currentprofile").val();
	var xmldata = "<plugload>"+
						"<id>"+CURRENT_PL_OBJECT.id+"</id>"+
						"<name>"+CURRENT_PL_OBJECT.name+"</name>"+
						"<currentProfile>"+CURRENT_PL_OBJECT.currentProfile+"</currentProfile>"+
						"<floorid>"+CURRENT_PL_OBJECT.floorId+"</floorid>"+
						"<xaxis>"+CURRENT_PL_OBJECT.xaxis+"</xaxis>"+
						"<yaxis>"+CURRENT_PL_OBJECT.yaxis+"</yaxis>"+
						"<state>"+CURRENT_PL_OBJECT.state+"</state>"+
						"<gatewayid>"+GATEWAY_ID+"</gatewayid>"+
					"</plugload>";
	PL_DEFAULT_VAL_PROFILE = CURRENT_PL_OBJECT.currentProfile;
	return xmldata;	 
}

function updatePlugload(){
	
	if(!validatePlugloadForm()){
		return false;
	}
	var xmlData = getPlugloadXML();
	
	displayPlCommissionMessage("Updating Plugload...", COLOR_SUCCESS);
	$.ajax({
		url: "${getPlugloadUpdateUrl}"+"?ts="+new Date().getTime(),
		type: "POST",
		data: xmlData, 
		dataType:"xml",
		contentType: "application/xml; charset=utf-8",
		success: function(data){
			displayPlCommissionMessage("Plugload updated successfully", COLOR_SUCCESS);
			updatePlugloadProfile(false);
		}
	});
}

function updatePlugloadProfile(bCommission) {
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
			groupId = PROFILE_DATA[CURRENT_PL_OBJECT.currentprofile];
		}
	}
	
	var urlOption = "plugloadId/"+ CURRENT_PL_OBJECT.id 
					+ "/groupId/"+ groupId
					+ "/currentProfile/"+ CURRENT_PL_OBJECT.currentprofile
					+ "/originalProfile/"+ CURRENT_PL_OBJECT.originalprofilefrom ;
	$.ajax({
		url: "${changePlugloadProfileUrl}"+urlOption+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			if (bCommission) {
				displayPlCommissionMessage("Commissioning Plugload, please wait...", COLOR_SUCCESS);
				startCommissioning();
			}
		}
	});	
}

function commissionDevice(){
	if(!validatePlugloadForm()){
		resetDevicePositionOnFloorplan();
		return false;
	}
	enableDisableUIControls(false);
	enableDiscoveryButton(false);
	
	var xmlData = getPlugloadXML();
	
	displayPlCommissionMessage("Updating Plugload...", COLOR_SUCCESS);
	$.ajax({
		url: "${getPlugloadUpdateUrl}",
		type: "POST",
		data: xmlData, 
		dataType:"xml",
		contentType: "application/xml; charset=utf-8",
		success: function(data){
			updatePlugloadProfile(true);
			displayPlCommissionMessage("Plugload commissioning in process...", COLOR_SUCCESS);
		},
		error: function() {
			bRMAInProgress = false;
			resetDevicePositionOnFloorplan();
		}
	});
}

function startCommissioning() {
	//var xmlData = getPlugloadXML();
	var urlData = "";
	//if(IS_BULK_COMMISSION){
		urlData = "/floor/"+ '${floorId}' + "/gateway/"+GATEWAY_ID+"/type/"+COMMISSION_TYPE;
	//}
	displayPlCommissionMessage("Plugload commissioning in process...", COLOR_SUCCESS);
	$.ajax({
		url: "${startPlugloadUpdateUrl}"+urlData+"?ts="+new Date().getTime(),
		type: "GET",
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			startPlugloadCommission();
		},
		error: function() {
			bRMAInProgress = false;
			resetDevicePositionOnFloorplan();
		}
	});
}
function startPlugloadCommission(){
	$.ajax({
		url: "${commissionedPlugload}"+"/plugloadId/"+CURRENT_PL_OBJECT.id+"/gateway/"+GATEWAY_ID+"?ts="+new Date().getTime(),
		type: "GET",
		dataType:"",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			getCommissionPlacementStatus();
		},
		error: function(){
			resetDevicePositionOnFloorplan();
		}
	});
	
	displayPlCommissionMessage(commission_placement_retry_counter+": Plugload commissioning in process...", COLOR_SUCCESS);
}

function getCommissionPlacementStatus(){
	$.ajax({
		url: "${getCommissionStatusUrl}"+"/plugloadId/"+CURRENT_PL_OBJECT.id+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			getCommissionPlacementStatus_Success(data.msg);
		},
		error: function(){
			getCommissionPlacementStatus_Success("");
		}
	});
	
	displayPlCommissionMessage(commission_placement_retry_counter+": Plugload commissioning in process...", COLOR_SUCCESS);
}

function getCommissionPlacementStatus_Success(status){
	if(status.toUpperCase() == COMMISSIONED) {
		ShowPlugloadCommissioningSuccessful();		
		try {
			displayPlCommissionMessage("Plugload commissioned successfully.", COLOR_SUCCESS);
			getCommissionPlanObj("c_plugload_floorplan").addPlugload('floor', '${floorId}', $("#plugloadId").val(), $("#plugloadname").val(), "");
		} catch(e){
			resetDevicePositionOnFloorplan();
			displayPlCommissionMessage("Plugload commissioned successfully, but failed to do placement on floor plan.", COLOR_SUCCESS);
		}
		// reset current Plugload id
		CURRENT_PL_OBJECT = {};
		bRMAInProgress = false;
	} else {
		if(commission_placement_retry_counter < 10){
			var commission_placement_retry_timer = setTimeout("getCommissionPlacementStatus()", 2000);
			commission_placement_retry_counter++;
		} else {
			ShowPlugloadCommissioningFailed();
		}
	}
}

function ShowPlugloadCommissioningSuccessful() {
	commission_placement_retry_counter = 0;
	displayPlCommissionMessage("Plugload commissioned successfully.", COLOR_SUCCESS);
	enableDiscoveryButton(true);
	reloadPlugloadListIFrame();
}

function resetDevicePositionOnFloorplan() {
	try {
		getCommissionPlanObj("c_plugload_floorplan").resetDevicePosition();
	} catch(e){
		// Protect for javascript + flex communication failure
	}
}

function ShowPlugloadCommissioningFailed() {
//TODO 	this.resetDevicePosition();
	commission_placement_retry_counter = 0;
	displayPlCommissionMessage(" Plugload Commissioned Failed", COLOR_FAILURE);
	resetDevicePositionOnFloorplan();
	enableDisableUIControls(true);
	enableDiscoveryButton(true);
	bRMAInProgress = false;
	$("#plugloadname").val("");
	$("#plugloadId").val("");
	alert("Could not communicate with Plugload using the provided Plugload ID. \n\nTry the following: \n- Verify that the Plugload name is entered \n- Check that the Plugload has power \n- Check that the EM server is connected to the 'building lighting network"); //TODO : message
}

function undoButtonHandler(){
	setPlugloadBasicInfo(CURRENT_PL_OBJECT);
}

function doneButtonHandler(){
	var isExit = confirm("Are you sure you wish to exit?");
	if(isExit) {
		exit_commission_retry_counter = 0;
		exitPlugloadCommissionWindow();
	}
}

function initExitCommissionProcess(){
	var urlOption = "gateway/"+ GATEWAY_ID;
	displayPlCommissionMessage("Exiting commissioning process. Please wait...", COLOR_SUCCESS);
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
			exitPlCWindow();
		}
	} else {
		exitPlCWindow();
	}
}

function getCommissioningStatus_AfterExit(){
	displayPlCommissionMessage(exit_commission_retry_counter  + ": Exiting commissioning process. Please wait...", COLOR_SUCCESS);
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


function exitPlugloadCommissionWindow(){
	commission_retry_counter = 4;
	stopPollPlugloadTimer();
	// This will initiate finish commissioning
	initExitCommissionProcess();
}

function exitPlCWindow(){
  	$("#plugloadCommissioningDialog").dialog("close");
}

function resizePlugloadCommissionDialog(){
	//resize flash object
	var flashEl = document.getElementById("plcd-flash-container");
	$("#plcd-floorplan-flash").css("height", flashEl.offsetHeight - 2);
	$("#plcd-floorplan-flash").css("width", flashEl.offsetWidth - 1);
	
	//resize other Plugload grid
	var otherGridEL = document.getElementById("other-plugload-list-container");
	forceFitJQgridHeight(jQuery("#discplugload-plugload-list-grid"), otherGridEL.offsetHeight);
	jQuery("#discplugload-plugload-list-grid").setGridWidth(otherGridEL.offsetWidth);
}

function forceFitJQgridHeight(jgrid, containerHeight){
	var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
	var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
	var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
// 	alert(containerHeight + ">>" + Math.floor((containerHeight - gridHeaderFooterHeight) * .99));
	jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - gridHeaderFooterHeight) * .99)); 
}

function displayPlCommissionMessage(Message, Color) {
	$("#plcd-mesaage").html(Message);
	$("#plcd-mesaage").css("color", Color);
}
function clearPlCommissionMessage() {
	displayPlCommissionMessage("", COLOR_DEFAULT);
}

function reloadPlugloadListIFrame(){
	var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("plugloadsFrame");
	ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src;
}

function validatePlugloadName()
{
	var chktemplatename = $("#plugloadname").val();
	var returnresult = false;	
	if(chktemplatename=="" || chktemplatename==" ")
	{		
		 clearPlugloadMessage();
		 $("#errorMsg").text("Above field is required.");
		 $("#plugloadname").addClass("invalidField");
		return false;
	}	
	
	var invalidFormatStr = 'Plugload name must contain only letters, numbers, or underscore';
    var regExpStr = /^[a-z0-9\_\s]+$/i;
    if(regExpStr.test(chktemplatename) == false) {
    	$("#errorMsg").text(invalidFormatStr);
		$("#plugloadname").addClass("invalidField");
    	return false;
    }
    return true;
}

function clearPlugloadMessage()
{		 
		 $("#errorMsg").text("");
		 $("#plugloadname").removeClass("invalidField");
}

</script>

<body> 
 <table id="plcd-main-box" style="width:100%; height:100%;">
	 <tr height=0> <td width="20%"></td> <td width="80%"></td> </tr>
	 <tr height="30px">
		 <td colspan=2 style="border-bottom: 1px solid #DCDCDC;">
		 	<table style="width:100%;" cellspacing=0 >
		 		<tr>
			 		<td style="width:20%; text-align:left;"><span id="plcd-gateway-name" class="label">Gateway: <c:out value="${gateway.gatewayName}"/></span></td>
			 		<td style="width:53%; text-align:center;"><span id="plcd-mesaage" class="label"></span></td>
			 		<td style="width:8%; text-align:right;"><button id="plcd-done-btn" style="float:right">Done</button></td>
		 		</tr>
		 	</table>
		 </td>
	 </tr>
	 <tr height="18px">
		 <td class="highlightGray"><div class="label">Available Plugload</div></td>
		 <td class="highlightGray"><div class="label">Plugload Configuration</div></td>		 			 
	 </tr>
	 <tr height="auto">
		 <td style="border:1px solid black; height:100%;">
		  	<table cellspacing=0 style="width:100%; height:100%;">
		  		
				<tr style="height:100%;">
				 	<td id="other-plugload-list-container" style="vertical-align: top; border-top:1px solid black;">
						<table id="discplugload-plugload-list-grid"></table>
					</td>
				</tr>
			</table>
		 </td>
		 <td style="height:100%;">
		 	<table cellspacing=0 style="width:100%; height:100%;">		 	
				<tr style="height:136px;">
				 	<td style="vertical-align: top; border:1px solid black;">
						<div id="plugload-form-tabs">
							<ul>
								<li><a href="#pl-form-basic-tab">Basic</a></li>
								<li><a href="#pl-form-details-tab" style="visibility:hidden;">Details</a></li>
							</ul>
							<div id="pl-form-basic-tab" class="tab-container" style="display:none;">
								<form id="pl-basic-form" class="plcd-form-body">
									<input type="hidden" id="plugloadId"/>
									
									<fieldset class="form-column-left">
										<div class="fieldWrapper">
											<div class="fieldlabel"><label for="plugloadname">Name:</label></div>
											<div class="fieldInput"><input class="text readonly" id="plugloadname" name="plugloadname" size="40" maxLength="30"/></div>
											<br style="clear:both;"/>
											<br/>
											<span id="errorMsg" class="error"></span>
										</div>										
										<div class="fieldPadding"></div>
										
									</fieldset>
									<fieldset style="height:100% !important;">
										<div class="fieldWrapper">
											<div class="fieldlabel"><label for="currentprofile">Profile:</label></div>
											<div class="fieldInputCombo1">
												<select class="text" id="currentprofile" name="currentprofile">
													<c:forEach items="${groups}" var="group">
														<option value="${group.name}">${group.name}</option>
													</c:forEach>
												</select>
											</div>
											<br style="clear:both;"/>
										</div>
										<div style="height:30px;"></div>
										
									</fieldset>
									<br style="clear:both;"/>
								</form>
							</div>

						</div>
				 	</td>
				</tr>
				<tr style="height:30px;">
				 	<td  class="highlightGray" style="border:2px none;">
				 		<div style = "float:left; padding:7px 0 0 2px;" class="label" id="locationText">Location: <c:out value="${gateway.location}"/></div>
				 		<span  style="float:right;">
					 		<button id="plcd-update-btn">Update</button>
					 		<button id="plcd-undo-btn" style="display: none">Undo</button>
					 		<button id="plcd-commission-btn">Commission and place</button>
				 		</span>
				 	</td>
				</tr>
				<tr style="height:auto;">
				 	<td id="plcd-flash-container" style="vertical-align: top; border:1px solid black;">
				 		<div id="plcd-floorplan-flash" style="height:100%; width:100%;"> </div>
				 	</td>
				</tr>
			</table>
		 </td>
	 </tr>
 </table>
</body>
</html>