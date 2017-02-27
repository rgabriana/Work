<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<spring:url value="/modules/PlotChartModule.swf" var="plotchartmodule"></spring:url>
<spring:url value="/services/org/fixture/getcommissionstatus" var="getCommissionStatusUrl" scope="request" />
<spring:url value="/services/org/fixture/list/" var="getAllFixturesBySecondaryGatewayUrl" scope="request" />
<spring:url value="/services/org/fixture/details/" var="getFixtureDetailsUrl" scope="request" />
<spring:url value="/services/org/fixture/updateduringcommission" var="getFixtureUpdateUrl" scope="request" />
<spring:url value="/services/org/fixture/validatefixture" var="validateFixtureUrl" scope="request" />
<spring:url value="/services/org/fixture/changeprofile/" var="changeFixtureProfileUrl" scope="request" />
<spring:url value="/services/org/fixture/exitcommission/" var="exitCommissionUrl" scope="request" />

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
<title>Commission and Place Fixtures</title>

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
	#fxcd-main-box fieldset.form-column-right{float:left; width: 49%;}
	#fxcd-main-box .fieldWrapper{padding-bottom:2px;}
	#fxcd-main-box .fieldPadding{height:4px;}
	#fxcd-main-box .fieldlabel{float:left; height:20px; width: 25%; font-weight: bold;}
	#fxcd-main-box .fieldInput{float:left; height:20px; width: 60%;}
	#fxcd-main-box .fieldInputCombo{float:left; height:23px; width: 60.5%;}
	#fxcd-main-box .text {height:100%; width:100%;}
	#fxcd-main-box .readOnly {border:0px none;}
	
	.fxcd-row-icon{float: left; height: 16px; margin-left: 5px; width: 16px;}
</style>
</head>


<script type="text/javascript">
var COMMISSION_TYPE = "${type}";
var GATEWAY_ID  = "${gateway.id}";
var FIXTURE_ID = "${fixtureId}";
var IS_BULK_COMMISSION = "${isBulkCommission}" == "true";

var CURRENT_FIXTURE_OBJECT = {};
var ALL_FIXTURES_DATA = [];
var BALLASTS_DATA = {}; //JSON Object: key=ballat_id, value=ballast_object_json
var PROFILE_DATA = {}; //JSON Object: key=profile_name, value=profile_id

var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";

var COMM_STATUS_INPROGRESS = 10; 			//Commissioning is in progress	
var COMM_ERROR_GW_CH_CHANGE_DEF = 13; 		//Not able to move the Gateway to default wireless parameters during commissioning.		
var VALIDATED = "VALIDATED";
var PLACED = "PLACED";		
var COMMISSIONED = "COMMISSIONED";
var UNCOMMISSIONE = "UNCOMMISSIONED";

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
var fixture_data_uncommissioned = [];
var fixture_data_strobed = [];
var exit_commission_retry_counter = 0;

//Poll Fixtures list (timer)
var poll_fixtures_timer;
var poll_fixtures_timer_is_on=0;

function getCommissionPlanObj(objectName) {			
	if ($.browser.mozilla) {
		return document[objectName] 
	}
	return document.getElementById(objectName);
}	

function timedPollFixtures() {
	loadAllFixtureBySecondaryGatewayId();
	poll_fixtures_timer = setTimeout("timedPollFixtures()", 2000);
}

function startPollFixturesTimer(){
	if (!poll_fixtures_timer_is_on){
		poll_fixtures_timer_is_on=1;
		timedPollFixtures();
	}
}

function stopPollFixturesTimer(){
	clearTimeout(poll_fixtures_timer);
	poll_fixtures_timer_is_on=0;
}

$(document).ready(function() {
	enableDisableUIControls(false);
	getCommissioningStatus_BeforeStart();

	getAllBallasts();
	getAllProfiles();
	
//	startPollFixturesTimer(); //UNCOMMENT ME (for testing)
	
	//Init strobed fixture list grid
	var sb_fx_cnt = 0;
	jQuery("#strobed-fx-list-grid").jqGrid({
		datatype: "local",
		autowidth: true,
		forceFit: true,
		scrollOffset: 0,
	   	colNames:["id", "Strobed (<span id='sb-fx-cnt'>"+sb_fx_cnt+"</span> fixtures)", ""],
	   	colModel:[
			{name:'id', index:'id', hidden:true},
  			{name:'name', index:'name', width:"70%", sortable:false},
			{name:'action', index:'action', width:"30%", sortable:false, formatter: strobedFxIconRenderer}
	   	],
	   	multiselect: false,
	   	onSelectRow: function(rowid, status){
	   		selectFixture(rowid, 1);
      	}
	});
	//Set grid's height to fit into its container
	var strobedGridEL = document.getElementById("strobed-fx-list-container");
	forceFitJQgridHeight(jQuery("#strobed-fx-list-grid"), strobedGridEL.offsetHeight);
	
	//Init other fixture list grid
	var othr_fx_cnt = 0;
	jQuery("#other-fx-list-grid").jqGrid({
		datatype: "local",
		autowidth: true,
		forceFit: true,
		scrollOffset: 0,
	   	colNames:["id", "Others (<span id='othr-fx-cnt'>"+othr_fx_cnt+"</span> fixtures)", ""],
	   	colModel:[
  			{name:'id', index:'id', hidden:true},
			{name:'name', index:'name', width:"70%", sortable:false},
			{name:'action', index:'action', width:"30%", sortable:false,  formatter: otherFxIconRenderer}
	   	],
	   	multiselect: false,
	   	onSelectRow: function(rowid, status){
	   		selectFixture(rowid, 2);
      	}
	});
	//Set grid's height to fit into its container
	var otherGridEL = document.getElementById("other-fx-list-container");
	forceFitJQgridHeight(jQuery("#other-fx-list-grid"), otherGridEL.offsetHeight);
	
	//Init TabPanel
	$("#fx-form-basic-tab").css("display", "block");
	$("#fx-form-details-tab").css("display", "block");
	$("#fixture-form-tabs").tabs({selected: 0});
	
	//Init flash object
	var load_flash_floor = function(nodetype, nodeid) {
		/*
		$('#fxcd-floorplan-flash').flash({
			id : 'c_fx_floorplan',
			src : '${plotchartmodule}',
			width : '100%',
			height : '100%',
			padding : '0px',
			wmode : 'opaque',
			flashvars : {
				orgType : nodetype,
				orgId : nodeid,
				mode: 'COMMISSION'
			}
		});
		*/
		var FP_data = "";
		//var plotchartmodule_url = "${plotchartmodule}?ts=" + new Date().getTime();
		
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
			FP_data = "<object id='c_fx_floorplan' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='http://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='100%'>";
			FP_data +=  "<param name='src' value='" + plotchartmodule_url + "'/>";
			FP_data +=  "<param name='padding' value='0px'/>";
			FP_data +=  "<param name='wmode' value='opaque'/>";
			FP_data +=  "<param name='allowFullScreen' value='true'/>";
			FP_data +=  "<param name='flashVars' value='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION'/>";
			FP_data +=  "<embed id='c_fx_floorplan' name='c_fx_floorplan' src='" + plotchartmodule_url + "' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " padding='0px'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION'/>";
			FP_data +=  "</object>";
		} else {
			FP_data = "<embed id='c_fx_floorplan' name='c_fx_floorplan' src='" + plotchartmodule_url + "' pluginspage='http://www.adobe.com/go/getflashplayer'";
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
	$('#fxcd-update-btn').click(function(){updateFixture();});
	$('#fxcd-undo-btn').click(function(){undoButtonHandler();});
	$('#fxcd-commission-btn').click(function(){commissionDevice();});
	$('#fxcd-done-btn').click(function(){doneButtonHandler();});
	
	
	//Mark un-editable field as readonly
	$('input.readOnly').attr('readonly', 'readonly');
	$('input.readOnly').focus(function() {
		 $(this).blur();
	});
}); //End : Document Ready

function strobedFxIconRenderer(cellvalue, options, rowObject){
	var cellData = "";
	if((rowObject.commissionstatus & COMMISSION_STATUS_MOTION) == COMMISSION_STATUS_MOTION) {
		if(COMMISSION_TYPE == 4) {
			if((rowObject.commissionstatus & COMMISSION_STATUS_DIMMING) == COMMISSION_STATUS_DIMMING) {
				cellData += "<div class='fxcd-row-icon' style=\"background-image: url('/ems/themes/default/images/D.png')\"></div>";
			} else {
				cellData += "<div class='fxcd-row-icon' style=\"background-image: url('/ems/themes/default/images/Dg.png')\"></div>";
			}
		}
	}
	return cellData;
}

function otherFxIconRenderer(cellvalue, options, rowObject){
	var cellData = "";
	if(rowObject.commissionstatus == COMMISSION_STATUS_UNKNOWN) {
		cellData += "<div class='fxcd-row-icon' style=\"background-image: url('/ems/themes/default/images/Cg.png')\"></div>";
		if(COMMISSION_TYPE == 4) {
			cellData += "<div class='fxcd-row-icon' style=\"background-image: url('/ems/themes/default/images/Dg.png')\"></div>";
		}
	} else {
		if((rowObject.commissionstatus & COMMISSION_STATUS_COMMUNICATION) == COMMISSION_STATUS_COMMUNICATION) {
			cellData += "<div class='fxcd-row-icon' style=\"background-image: url('/ems/themes/default/images/C.png')\"></div>";
			if(COMMISSION_TYPE == 4) {
				if((rowObject.commissionstatus & COMMISSION_STATUS_DIMMING) == COMMISSION_STATUS_DIMMING) {
					cellData += "<div class='fxcd-row-icon' style=\"background-image: url('/ems/themes/default/images/D.png')\"></div>";
				} else {
					cellData += "<div class='fxcd-row-icon' style=\"background-image: url('/ems/themes/default/images/Dg.png')\"></div>";
				}
			}
		} else { // Just in case if we missing the communication ACK
			if(COMMISSION_TYPE == 4) {
				if((rowObject.commissionstatus & COMMISSION_STATUS_DIMMING) == COMMISSION_STATUS_DIMMING) {
					cellData += "<div class='fxcd-row-icon' style=\"background-image: url('/ems/themes/default/images/C.png')\"></div>";
					cellData += "<div class='fxcd-row-icon' style=\"background-image: url('/ems/themes/default/images/D.png')\"></div>";
				} else {
					cellData += "<div class='fxcd-row-icon' style=\"background-image: url('/ems/themes/default/images/Cg.png')\"></div>";
					cellData += "<div class='fxcd-row-icon' style=\"background-image: url('/ems/themes/default/images/Dg.png')\"></div>";
				}
			} else {
				cellData += "<div class='fxcd-row-icon' style=\"background-image: url('/ems/themes/default/images/Cg.png')\"></div>";
			}
		}
	}
	return cellData;
}

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
		startPollFixturesTimer();
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

function enableDisableUIControls(isEnable){
	if(isEnable){
		//enable form elements
		$("#fixturename").removeAttr("disabled");
		$("#nooffixtures").removeAttr("disabled");
		$("#currentprofile").removeAttr("disabled");
		$("#voltage").removeAttr("disabled");
		$("#ballasttype").removeAttr("disabled");
		$("#lamptype").removeAttr("disabled");
		$("#description").removeAttr("disabled");
		$("#notes").removeAttr("disabled");
		
		//enable buttons
		$('#fxcd-update-btn').removeAttr("disabled");
		$('#fxcd-undo-btn').removeAttr("disabled");
		$('#fxcd-commission-btn').removeAttr("disabled");
	} else {
		//disable form elements
		$("#fixturename").attr("disabled", true);
		$("#nooffixtures").attr("disabled", true);
		$("#currentprofile").attr("disabled", true);
		$("#voltage").attr("disabled", true);
		$("#ballasttype").attr("disabled", true);
		$("#lamptype").attr("disabled", true);
		$("#description").attr("disabled", true);
		$("#notes").attr("disabled", true);

		//disable buttons
		$('#fxcd-update-btn').attr("disabled", true);
		$('#fxcd-undo-btn').attr("disabled", true);
		$('#fxcd-commission-btn').attr("disabled", true);
	}	
	
}

function loadAllFixtureBySecondaryGatewayId(){
	var params = "secondarygateway/"+GATEWAY_ID+"/";
	$.ajax({
		url: "${getAllFixturesBySecondaryGatewayUrl}"+params+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			if(data!=null){
				fixturesData = data.fixture;
				if(fixturesData != undefined){
					if(fixturesData.length == undefined){ //FIXME : JSON response issue : For only 1 record it returns Object instead of an Array
						var dataWrapper = {};
						dataWrapper.fixture = [];
						dataWrapper.fixture.push(fixturesData);
						loadAllFixtureBySecondaryGatewayId_Success(dataWrapper);
					} else if(fixturesData.length > 0){
						loadAllFixtureBySecondaryGatewayId_Success(data);
					}
				}
			}
		}
	});
}

function loadAllFixtureBySecondaryGatewayId_Success(data){
	fixture_data_uncommissioned = [];
	fixture_data_strobed = [];

	var strobedJGrid = jQuery("#strobed-fx-list-grid");
	var uncommissionJGrid = jQuery("#other-fx-list-grid");

	uncommissionJGrid.jqGrid("clearGridData");
	strobedJGrid.jqGrid("clearGridData");
	
	var allFixtures = data.fixture;
	$.each(allFixtures, function(i, fixture) {
		if(fixture.state != COMMISSIONED){
			if(IS_BULK_COMMISSION || (!IS_BULK_COMMISSION && FIXTURE_ID == fixture.id)){
				var markSelected = false;
				if(CURRENT_FIXTURE_OBJECT.id != undefined && CURRENT_FIXTURE_OBJECT.id == fixture.id){
					 markSelected = true;
				}
				
				if((fixture.commissionstatus & COMMISSION_STATUS_MOTION) == COMMISSION_STATUS_MOTION) {
					fixture_data_strobed.push(fixture);
					strobedJGrid.jqGrid('addRowData', fixture_data_strobed.length + 1, fixture);
					if(markSelected){
						strobedJGrid.jqGrid('setSelection', fixture_data_strobed.length + 1, false);
					}
				} else {
					fixture_data_uncommissioned.push(fixture);
					uncommissionJGrid.jqGrid('addRowData', fixture_data_uncommissioned.length + 1, fixture);
					if(markSelected){
						uncommissionJGrid.jqGrid('setSelection', fixture_data_uncommissioned.length + 1, false);
					}
				}
			}
		}
	});
	
	
//load 1st strobed fixture details in fixture form
// 	if(fixture_data_strobed.length > 0){
// 		var currentFixture = jQuery("#strobed-fx-list-grid").jqGrid('getRowData', 1); 
// 		if(currentFixture.id != $("#fixtureId").val()){
// 			selectFixture(1, 1);
// 		}
// 	}
	
	updateFixtureCount();
}

function updateFixtureCount(){
	$("#sb-fx-cnt").html(fixture_data_strobed.length);
	$("#othr-fx-cnt").html(fixture_data_uncommissioned.length);
}

function selectFixture(rowid, gridIndex) {
	clearFxCommissionMessage();
	var currentFixture = {};
	
	var strobedJGrid = jQuery("#strobed-fx-list-grid");
	var uncommissionJGrid = jQuery("#other-fx-list-grid");
	
	if(gridIndex == 1){ // Strobed Grid
		if(rowid > 0){
			currentFixture = strobedJGrid.jqGrid('getRowData', rowid); 
			uncommissionJGrid.jqGrid("resetSelection");
		} else {
			displayFxCommissionMessage("Select a fixture from Strobed fixure list", COLOR_FAILURE);
			return;
		}
	} else {
		if(rowid > 0){
			currentFixture = uncommissionJGrid.jqGrid('getRowData',rowid); 
			strobedJGrid.jqGrid("resetSelection");
		} else {
			displayFxCommissionMessage("Select a fixture from Others fixure list", COLOR_FAILURE);
			return;
		}
	}	

	loadFixtureDetails(currentFixture.id);
}

function loadFixtureDetails(fixtureId){
	$.ajax({
		url: "${getFixtureDetailsUrl}"+fixtureId+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			enableDisableUIControls(true);
			CURRENT_FIXTURE_OBJECT = data; //save into glabal variable
			setfixtureBasicInfo(CURRENT_FIXTURE_OBJECT);
			setfixtureDetailInfo(CURRENT_FIXTURE_OBJECT);
		}
	});
}

function setfixtureBasicInfo(fixture){
	$("#fixtureId").val(fixture.id);
	$("#fixturename").val(fixture.name);
	$("#nooffixtures").val(fixture.nooffixtures); //No. of ballast

	if(FX_DEFAULT_VAL_PROFILE == ""){
		FX_DEFAULT_VAL_PROFILE = PROFILE_OPEN_OFFICE;
	}
	
	if(FX_DEFAULT_VAL_PROFILE != ""){
		$("#currentprofile").val(FX_DEFAULT_VAL_PROFILE);
	} else {
		$("#currentprofile").val(fixture.currentprofile); //Profile
	}
	
	if(FX_DEFAULT_VAL_VOLTAGE != ""){
		$("#voltage").val(FX_DEFAULT_VAL_VOLTAGE);
	}else{
		$("#voltage").val(fixture.voltage);	
	}
	
	if(FX_DEFAULT_VAL_BALLAST != ""){
		$("#ballasttype").val(FX_DEFAULT_VAL_BALLAST);
		setNoOfLamps();
	}else{
		if(fixture.ballast != null){
			$("#ballasttype").val(fixture.ballast.id);
			setNoOfLamps();
		}	
	}
	
	if(FX_DEFAULT_VAL_LAMP != ""){
		$("#lamptype").val(FX_DEFAULT_VAL_LAMP);
	}else{
		if(fixture.bulb != null){
			$("#lamptype").val(fixture.bulb.id);
		}	
	}	
	
}

function setfixtureDetailInfo(fixture){
	$("#description").val(fixture.description);
	$("#notes").val(fixture.notes);
	$("#ballastLastServiceDate").val((fixture.ballastlastservicedate != null) ? fixture.ballastlastservicedate : "");
}

function setNoOfLamps(){
	var ballastId = $("#ballasttype").val();
	$("#noofbulbs").val(BALLASTS_DATA[ballastId].lampnum); //No. of lamps
}

function validateFixtureForm(){
	if(CURRENT_FIXTURE_OBJECT.id == undefined) {
		displayFxCommissionMessage("Select a fixture from the list", COLOR_FAILURE);
		return false;
	}
	
	var FixtureName = $("#fixturename").val();
	FixtureName = $.trim(FixtureName);
	if(FixtureName == ""){
		displayFxCommissionMessage("Enter fixture name", COLOR_FAILURE);
		return false;
	} else {
		//TODO duplicate name check
// 		displayFxCommissionMessage("This fixture name is already in use. Please use any other name", COLOR_FAILURE);
// 		return false;
	}

	return true;
}

function getFixtureXML(){
	//Update Global variable
	CURRENT_FIXTURE_OBJECT.name = $("#fixturename").val();
	CURRENT_FIXTURE_OBJECT.noofbulbs = $("#noofbulbs").val();
	CURRENT_FIXTURE_OBJECT.nooffixtures = $("#nooffixtures").val();
	CURRENT_FIXTURE_OBJECT.originalprofilefrom = CURRENT_FIXTURE_OBJECT.currentprofile;
	CURRENT_FIXTURE_OBJECT.currentprofile = $("#currentprofile").val();
	CURRENT_FIXTURE_OBJECT.voltage = $("#voltage").val();
	CURRENT_FIXTURE_OBJECT.ballast.id = $("#ballasttype").val();
	CURRENT_FIXTURE_OBJECT.bulb.id = $("#lamptype").val();
	CURRENT_FIXTURE_OBJECT.description = $("#description").val();
	CURRENT_FIXTURE_OBJECT.notes = $("#notes").val();
	CURRENT_FIXTURE_OBJECT.ballastlastservicedate = $("#ballastLastServiceDate").val();

	var xmldata = "<fixture>"+
						"<id>"+CURRENT_FIXTURE_OBJECT.id+"</id>"+
						"<noofbulbs>"+CURRENT_FIXTURE_OBJECT.noofbulbs+"</noofbulbs>"+
						"<currentprofile>"+CURRENT_FIXTURE_OBJECT.currentprofile+"</currentprofile>"+
						"<name>"+CURRENT_FIXTURE_OBJECT.name+"</name>"+
						"<description>"+CURRENT_FIXTURE_OBJECT.description+"</description>"+
						"<notes>"+CURRENT_FIXTURE_OBJECT.notes+"</notes>"+
						"<ballast>"+
							"<id>"+CURRENT_FIXTURE_OBJECT.ballast.id+"</id>"+
							"<name></name>"+
							"<lampnum></lampnum>"+
						"</ballast>"+
						"<bulb>"+
							"<id>"+CURRENT_FIXTURE_OBJECT.bulb.id+"</id>"+
							"<name></name>"+
						"</bulb>"+
						"<nooffixtures>"+CURRENT_FIXTURE_OBJECT.nooffixtures+"</nooffixtures>"+
						"<voltage>"+CURRENT_FIXTURE_OBJECT.voltage+"</voltage>"+
					"</fixture>";
					
	FX_DEFAULT_VAL_PROFILE = CURRENT_FIXTURE_OBJECT.currentprofile;
	FX_DEFAULT_VAL_BALLAST = CURRENT_FIXTURE_OBJECT.ballast.id;
	FX_DEFAULT_VAL_LAMP = CURRENT_FIXTURE_OBJECT.bulb.id;
	FX_DEFAULT_VAL_VOLTAGE = CURRENT_FIXTURE_OBJECT.voltage;
	
	return xmldata;	
}

function updateFixture(){
	if(!validateFixtureForm()){
		return false;
	}
	var xmlData = getFixtureXML();
	
	displayFxCommissionMessage("Updating Fixture...", COLOR_SUCCESS);
	$.ajax({
		url: "${getFixtureUpdateUrl}"+"?ts="+new Date().getTime(),
		type: "POST",
		data: xmlData, 
		dataType:"xml",
		contentType: "application/xml; charset=utf-8",
		success: function(data){
			displayFxCommissionMessage("Fixture updated successfully", COLOR_SUCCESS);
			updateFixtureProfile(false);
		}
	});
}

function updateFixtureProfile(bCommission) {
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
			groupId = PROFILE_DATA[CURRENT_FIXTURE_OBJECT.currentprofile];
		}
	}
	
	var urlOption = "fixtureId/"+ CURRENT_FIXTURE_OBJECT.id 
					+ "/groupId/"+ groupId
					+ "/currentProfile/"+ CURRENT_FIXTURE_OBJECT.currentprofile
					+ "/originalProfile/"+ CURRENT_FIXTURE_OBJECT.originalprofilefrom ;
	$.ajax({
		url: "${changeFixtureProfileUrl}"+urlOption+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			if (bCommission) {
				displayFxCommissionMessage("Commissioning Fixture, please wait...", COLOR_SUCCESS);
				validateFixture();
			}
		}
	});	
}

function commissionDevice(){
	if(!validateFixtureForm()){
		resetDevicePositionOnFloorplan();
		return false;
	}
	enableDisableUIControls(false);
	
	var xmlData = getFixtureXML();
	
	displayFxCommissionMessage("Updating Fixture...", COLOR_SUCCESS);
	$.ajax({
		url: "${getFixtureUpdateUrl}",
		type: "POST",
		data: xmlData, 
		dataType:"xml",
		contentType: "application/xml; charset=utf-8",
		success: function(data){
			displayFxCommissionMessage("Fixture updated successfully", COLOR_SUCCESS);
			updateFixtureProfile(true);
		},
		error: function() {
			resetDevicePositionOnFloorplan();
		}
	});	
}

function validateFixture(){
	var urlData = "";
	if(IS_BULK_COMMISSION){
		urlData = "/gatewayId/"+GATEWAY_ID;
	}
	urlData += "/fixtureId/"+CURRENT_FIXTURE_OBJECT.id ;
	
	$.ajax({
		url: "${validateFixtureUrl}"+urlData+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			getCommissionPlacementStatus();
		}
	});
	
	displayFxCommissionMessage("Fixture commissioning in process...", COLOR_SUCCESS);
}

function getCommissionPlacementStatus(){
	$.ajax({
		url: "${getCommissionStatusUrl}"+"/fixtureId/"+CURRENT_FIXTURE_OBJECT.id+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			getCommissionPlacementStatus_Success(data.msg);
		},
		error: function(){
			getCommissionPlacementStatus_Success("");
		}
	});
	
	displayFxCommissionMessage(commission_placement_retry_counter+": Fixture commissioning in process...", COLOR_SUCCESS);
}

function getCommissionPlacementStatus_Success(status){
	if(status.toUpperCase() == COMMISSIONED) {
		ShowFixtureCommissioningSuccessful();
		try {
			getCommissionPlanObj("c_fx_floorplan").addFixture('floor', '${floorId}', $("#fixtureId").val());
		} catch(e){
			resetDevicePositionOnFloorplan();
			displayFxCommissionMessage("Fixture commissioned successfully, but failed to do placement on floor plan.", COLOR_SUCCESS);
		}
		// reset current fixture id
		CURRENT_FIXTURE_OBJECT = {};
	} else {
		if(commission_placement_retry_counter < 10){
			var commission_placement_retry_timer = setTimeout("getCommissionPlacementStatus()", 2000);
			commission_placement_retry_counter++;
		} else {
			ShowFixtureCommissioningFailed();
		}
	}
}

function ShowFixtureCommissioningSuccessful() {
	commission_placement_retry_counter = 0;
	displayFxCommissionMessage("Fixture commissioned successfully.", COLOR_SUCCESS);
	reloadFixtureListIFrame();
}

function resetDevicePositionOnFloorplan() {
	try {
		getCommissionPlanObj("c_fx_floorplan").resetDevicePosition();
	} catch(e){
		// Protect for javascript + flex communication failure
	}
}

function ShowFixtureCommissioningFailed() {
//TODO 	this.fixtureChart.resetDevicePosition();
	commission_placement_retry_counter = 0;
	displayFxCommissionMessage("Fixture Commissioned Failed", COLOR_FAILURE);
	resetDevicePositionOnFloorplan();
	enableDisableUIControls(true);
	alert("Could not communicate with fixture using the provided fixture ID. \n\nTry the following: \n- Verify that the fixture name is entered \n- Check that the fixture has power \n- Check that the GEM server is connected to the 'building lighting network"); //TODO : message
}

function getAllBallasts(){
	BALLASTS_DATA = {};
	<c:forEach items="${ballasts}" var="ballast">
		var ballastObj = {};
		ballastObj.id = "${ballast.id}";
		ballastObj.lampnum = "${ballast.lampNum}";
		ballastObj.name = "${ballast.ballastName}";
		BALLASTS_DATA["${ballast.id}"] = ballastObj;
	</c:forEach>
}

function getAllProfiles(){
	PROFILE_DATA = {};
	<c:forEach items="${groups}" var="group">
		PROFILE_DATA["${group.name}"] = "${group.id}";
	</c:forEach>
}

function undoButtonHandler(){
	setfixtureBasicInfo(CURRENT_FIXTURE_OBJECT);
	setfixtureDetailInfo(CURRENT_FIXTURE_OBJECT);
}

function doneButtonHandler(){
	var isExit = confirm("Are you sure you wish to exit?");
	if(isExit) {
		exit_commission_retry_counter = 0;
		exitFixtureCommissionWindow();
	}
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


function exitFixtureCommissionWindow(){
	commission_retry_counter = 4;
	stopPollFixturesTimer();
	// This will initiate finish commissioning
	initExitCommissionProcess();
}

function exitWindow(){
  	$("#fixtureCommissioningDialog").dialog("close");
}

function resizeFixtureCommissionDialog(){
	//resize flash object
	var flashEl = document.getElementById("fxcd-flash-container");
	$("#fxcd-floorplan-flash").css("height", flashEl.offsetHeight - 2);
	$("#fxcd-floorplan-flash").css("width", flashEl.offsetWidth - 1);
	
	//resize strobed fixture grid
	var strobedGridEL = document.getElementById("strobed-fx-list-container");
	forceFitJQgridHeight(jQuery("#strobed-fx-list-grid"), strobedGridEL.offsetHeight);
	jQuery("#strobed-fx-list-grid").setGridWidth(strobedGridEL.offsetWidth);
	
	//resize other fixture grid
	var otherGridEL = document.getElementById("other-fx-list-container");
	forceFitJQgridHeight(jQuery("#other-fx-list-grid"), otherGridEL.offsetHeight);
	jQuery("#other-fx-list-grid").setGridWidth(otherGridEL.offsetWidth);
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

function reloadFixtureListIFrame(){
	var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("fixturesFrame");
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
			 		<td style="width:73%; text-align:center;"><span id="fxcd-mesaage" class="label"></span></td>
			 		<td style="width:8%; text-align:right;"><button id="fxcd-done-btn" style="float:right">Done</button></td>
		 		</tr>
		 	</table>
		 </td>
	 </tr>
	 <tr height="18px">
		 <td class="highlightGray"><div class="label">Available Fixtures</div></td>
		 <td class="highlightGray"><div class="label">Fixture Configuration</div></td>
	 </tr>
	 <tr height="auto">
		 <td style="border:1px solid black; height:100%;">
		  	<table cellspacing=0 style="width:100%; height:100%;">
				<tr style="height:50%;">
				 	<td id="strobed-fx-list-container" style="vertical-align: top;">
						<table id="strobed-fx-list-grid"></table>
					</td>
				</tr>
				<tr style="height:50%;">
				 	<td id="other-fx-list-container" style="vertical-align: top; border-top:1px solid black;">
						<table id="other-fx-list-grid"></table>
					</td>
				</tr>
			</table>
		 </td>
		 <td style="height:100%;">
		 	<table cellspacing=0 style="width:100%; height:100%;">
				<tr style="height:136px;">
				 	<td style="vertical-align: top; border:1px solid black;">
						<div id="fixture-form-tabs">
							<ul>
								<li><a href="#fx-form-basic-tab">Basic</a></li>
								<li><a href="#fx-form-details-tab" style="visibility:hidden;">Details</a></li>
							</ul>
							<div id="fx-form-basic-tab" class="tab-container" style="display:none;">
								<form id="fx-basic-form" class="fxcd-form-body">
									<input type="hidden" id="fixtureId"/>
									
									<fieldset class="form-column-left">
										<div class="fieldWrapper">
											<div class="fieldlabel"><label for="fixturename">Name:</label></div>
											<div class="fieldInput"><input class="text readonly" id="fixturename" name="fixturename" size="40" /></div>
											<br style="clear:both;"/>
										</div>
										<div class="fieldPadding"></div>
										<div class="fieldWrapper">
											<div class="fieldlabel"><label for="ballasttype">Ballast Type:</label></div>
											<div class="fieldInputCombo">
												<select class="text" id="ballasttype" name="ballasttype" onchange="javascript: setNoOfLamps();">
													<c:forEach items="${ballasts}" var="ballast">
														<option value="${ballast.id}">${ballast.ballastName}</option>
													</c:forEach>
												</select>
											</div>
											<br style="clear:both;"/>
										</div>
										
										<div class="fieldWrapper">
											<div class="fieldlabel"><label for="lamptype">Lamp Type:</label></div>
											<div class="fieldInputCombo">
												<select class="text" id="lamptype" name="lamptype">
													<c:forEach items="${lamps}" var="lamp">
														<option value="${lamp.id}">${lamp.bulbName}</option>
													</c:forEach>
												</select>
											</div>
											<br style="clear:both;"/>
										</div>
										
										<div class="fieldWrapper">
											<div class="fieldlabel"><label for="voltage">Voltage(Vac):</label></div>
											<div class="fieldInputCombo">
												<select class="text" id="voltage" name="voltage">
													<option value="120">120</option>
													<option value="277">277</option>
												</select>
											</div>
											<br style="clear:both;"/>
										</div>
									</fieldset>
								
								
									<fieldset class="form-column-rigth">
										<div class="fieldWrapper">
											<div class="fieldlabel"><label for="currentprofile">Profile:</label></div>
											<div class="fieldInputCombo">
												<select class="text" id="currentprofile" name="currentprofile">
													<c:forEach items="${groups}" var="group">
														<option value="${group.name}">${group.name}</option>
													</c:forEach>
												</select>
											</div>
											<br style="clear:both;"/>
										</div>
										
										<div class="fieldWrapper">
											<div class="fieldlabel"><label for="nooffixtures">No. of Ballasts:</label></div>
											<div class="fieldInputCombo">
												<select class="text" id="nooffixtures" name="nooffixtures">
													<option value="1">1</option>
													<option value="2">2</option>
													<option value="3">3</option>
													<option value="4">4</option>
												</select>
											</div>
											<br style="clear:both;"/>
										</div>
										
										<div class="fieldWrapper">
											<div class="fieldlabel"><label for="noofbulbs">No. of Lamps:</label></div>
											<div class="fieldInputCombo">
												<select class="text" id="noofbulbs" name="noofbulbs" disabled="true">
													<option value="1">1</option>
													<option value="2">2</option>
													<option value="3">3</option>
													<option value="4">4</option>
												</select>
											</div>
											<br style="clear:both;"/>
										</div>
									</fieldset>
									<br style="clear:both;"/>
								</form>
							</div>

							<div id="fx-form-details-tab" class="tab-container" style="display:none;">
								<form id="fx-details-form" class="fxcd-form-body">
									<fieldset class="form-column-left">
										<div class="fieldWrapper">
											<div class="fieldlabel"><label for="description">Description:</label></div>
											<div class="fieldInput"><input class="text readonly" id="description" name="description" size="40" /></div>
											<br style="clear:both;"/>
										</div>
										
										<div class="fieldWrapper">
											<div class="fieldlabel"><label for="notes">Notes:</label></div>
											<div class="fieldInput"><input class="text" id="notes" name="notes" /></div>
											<br style="clear:both;"/>
										</div>
										
										<div class="fieldWrapper">
											<div class="fieldlabel"><label for="ballastLastServiceDate">Ballast Created on:</label></div>
											<div class="fieldInput"><input class="text readOnly" id="ballastLastServiceDate" name="ballastLastServiceDate" /></div>
											<br style="clear:both;"/>
										</div>
									</fieldset>
									<br style="clear:both;"/>
								</form>
							</div>
						</div>
				 	</td>
				</tr>
				<tr style="height:30px;">
				 	<td  class="highlightGray" style="border:0px none;">
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