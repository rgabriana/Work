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
<spring:url value="/services/org/fixture/rma/" var="rmaUrl" scope="request" />
<spring:url value="/services/org/fixture/op/identify/fixture/" var="identifyFixtureUrl" scope="request" />
<spring:url value="/services/org/fixture/op/unstrobe/gateway/" var="unstrobeFixtureUrl" scope="request" />
<spring:url value="/services/org/fixtureclassservice/loadAllFixtureClasses" var="loadAllFixtureClassesUrl" scope="request" />

<spring:url value="/settings/addfixtureclass.ems" var="addFixtureClassUrl" scope="request" />
<spring:url value="/settings/editfixtureclass.ems" var="editFixtureClassUrl" scope="request" />

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
	#fxcd-main-box fieldset.form-column-left{float:left;width: 60%;}
	#fxcd-main-box fieldset.form-column-right{float:left;width: 49%;}
	#fxcd-main-box .fieldWrapper{padding-bottom:2px;}
	#fxcd-main-box .fieldPadding{height:4px;}
	#fxcd-main-box .fieldlabel{float:left; height:20px; width: 15%; font-weight: bold;}
	#fxcd-main-box .fieldButton{float:left; height:20px;width: 15%;padding-left: 10px}
	#fxcd-main-box .fieldInput{float:left; height:20px; width: 40%;}
	#fxcd-main-box .fieldInputCombo{float:left; height:23px; width: 40.5%;}
	#fxcd-main-box .fieldInputCombo1{float:left; height:23px; width: 60.5%;}
	#fxcd-main-box .text {height:100%; width:100%;}
	#fxcd-main-box .readOnly {border:0px none;}
	
	.fxcd-row-icon{float: left; height: 16px; margin-left: 5px; width: 16px;}
	
	
	#fxcd-Table td {
		vertical-align: top;
		padding-top: 2px;
	}
	
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
var FIXTURE_CLASS_DATA = {};

var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";

var COMM_STATUS_INPROGRESS = 10; 			//Commissioning is in progress	
var COMM_ERROR_GW_CH_CHANGE_DEF = 13; 		//Not able to move the Gateway to default wireless parameters during commissioning.		
var VALIDATED = "VALIDATED";
var PLACED = "PLACED";		
var COMMISSIONED = "COMMISSIONED";
var UNCOMMISSIONE = "UNCOMMISSIONED";

var PROFILE_OPEN_OFFICE = "Open Office_Default";
var IS_PROFILE_OPEN_OFFICE_PRESENT = false;
var FX_DEFAULT_VAL_PROFILE = "";
var FX_DEFAULT_VAL_BALLAST = "";
var FX_DEFAULT_VAL_LAMP = "";
var FX_DEFAULT_VAL_VOLTAGE = "";
var FX_DEFAULT_VAL_NO_OF_BALLASTS = "";
var FX_DEFAULT_VAL_NO_OF_LAMPS = "";
var FX_DEFAULT_VAL_FIXTURE_CLASS = "";
var FX_DEFAULT_VAL_BALLAST_NAME = "";
var FX_DEFAULT_VAL_LAMP_NAME = "";

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
var scrollPosition = 0

var bRMAInProgress = false;
var fixtureGridType=0;
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
	localStorage.setItem("GridParam",null);
	document.onkeypress = disableEnterKey;
	enableDisableUIControls(false);
	getCommissioningStatus_BeforeStart();
	
	// getAllBallasts();
	getAllProfiles();
	getAllFixtureClass();
	
//	startPollFixturesTimer(); //UNCOMMENT ME (for testing)
	
	//Init strobed fixture list grid
	var sb_fx_cnt = 0;
	jQuery("#strobed-fx-list-grid").jqGrid({
		datatype: "local",
		autowidth: true,
		forceFit: true,
		multiselect : true,
		scrollOffset: 0,
	   	colNames:["id", "Strobed (<span id='sb-fx-cnt'>"+sb_fx_cnt+"</span> fixtures)", ""],
	   	colModel:[
			{name:'id', index:'id', hidden:true},
  			{name:'name', index:'name', width:"70%", sortable:false},
			{name:'action', index:'action', width:"30%", sortable:false, formatter: strobedFxIconRenderer}
	   	],
	   	onSelectRow: function(rowid, status){
	   		selectFixture(rowid, 1);
      	}
	});
	//Set grid's height to fit into its container
	var strobedGridEL = document.getElementById("strobed-fx-list-container");
	forceFitJQgridHeight(jQuery("#strobed-fx-list-grid"), strobedGridEL.offsetHeight);
	jQuery("#strobed-fx-list-grid").jqGrid('hideCol', 'cb');
	//Init other fixture list grid
	var othr_fx_cnt = 0;
	jQuery("#other-fx-list-grid").jqGrid({
		datatype: "local",
		autowidth: true,
		toolbar:[true,'top'],
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
	
	$('#t_other-fx-list-grid')
    .append("<div style='padding-right:10px'><input type='button' value='Identify' style='float:right;' onclick=\"identifyFixture();\"/></div>");
	$('#t_other-fx-list-grid').css("height", 28); 
	
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
			FP_data = "<object id='c_fx_floorplan' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='100%'>";
			FP_data +=  "<param name='src' value='" + plotchartmodule_url + "'/>";
			FP_data +=  "<param name='padding' value='0px'/>";
			FP_data +=  "<param name='wmode' value='opaque'/>";
			FP_data +=  "<param name='allowFullScreen' value='true'/>";
			FP_data +=  "<param name='flashVars' value='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION&modeid=FIXTURE_COMMISSION'/>";
			FP_data +=  "<embed id='c_fx_floorplan' name='c_fx_floorplan' src='" + plotchartmodule_url + "' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " padding='0px'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION&modeid=FIXTURE_COMMISSION'/>";
			FP_data +=  "</object>";
		} else {
			FP_data = "<embed id='c_fx_floorplan' name='c_fx_floorplan' src='" + plotchartmodule_url + "' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " padding='0px'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION&modeid=FIXTURE_COMMISSION'/>";
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
	
	$('#addFxClass').click(function(){addFixtureClassHandler();});
	$('#editFxClass').click(function(){editFixtureClassHandler();});
	
	//Mark un-editable field as readonly
	$('input.readOnly').attr('readonly', 'readonly');
	$('input.readOnly').focus(function() {
		 $(this).blur();
	});
}); //End : Document Ready

function addFixtureClassHandler(){
	displayFxCommissionMessage("", COLOR_DEFAULT);
	var page = "COMMISSION";
	$("#newFixtureClassDialog").load("${addFixtureClassUrl}"+"?page="+page+"&ts="+new Date().getTime()).dialog({
	        title : "Add Fixture Type",
	        width :  Math.floor($('body').width() * .50),
	        minHeight : 300,
            modal : true,
	        close: function(event, ui) {
				$("#newFixtureClassDialog").html("");
			}
	 });
}

function editFixtureClassHandler(){
	displayFxCommissionMessage("", COLOR_DEFAULT);
	var page = "COMMISSION";
	if ($("#fixtureClass option:selected").text() == "Select Fixture Type"){
		displayFxCommissionMessage("Select a Fixture Type from the list to Edit", COLOR_FAILURE);
	}
	else{
		$("#newFixtureClassDialog").load("${editFixtureClassUrl}?fixtureClassId="+$("#fixtureClass").val()+"&page="+page+"&ts="+new Date().getTime()).dialog({
        title : "Edit Fixture Type",
        width :  Math.floor($('body').width() * .40),
        minHeight : 250,
        modal : true,
        close: function(event, ui) {
			$("#newFixtureClassDialog").html("");
		}
    	});
	}
}

function refreshFixtureClassCombo(addedFixtureId){
	$.ajax({
		type: 'GET',
		url: "${loadAllFixtureClassesUrl}"+"?ts="+new Date().getTime(),
		success: function(data){
			if(data!=null){
				fixtureClassData = data.fixtureClass;
				if(fixtureClassData != undefined){
					$("#fixtureClass").empty();
					FIXTURE_CLASS_DATA = {};
					$('#fixtureClass').append($('<option></option>').val("Select Fixture Type").html("Select Fixture Type"));
					if(fixtureClassData.length == undefined){
						$('#fixtureClass').append($('<option></option>').val(fixtureClassData.id).html(fixtureClassData.name));
						$('#fixtureClass').val(addedFixtureId);
						var fixtureClassObj = {};
						fixtureClassObj.id = fixtureClassData.id;
						fixtureClassObj.name = fixtureClassData.name;
						fixtureClassObj.noOfBallasts = fixtureClassData.noOfBallasts;
						fixtureClassObj.voltage = fixtureClassData.voltage;
						fixtureClassObj.ballastId = fixtureClassData.ballast.id;
						fixtureClassObj.bulbId = fixtureClassData.bulb.id;
						fixtureClassObj.ballastName = fixtureClassData.ballast.name;
						fixtureClassObj.bulbName = fixtureClassData.bulb.name;
						fixtureClassObj.lampNum = fixtureClassData.ballast.noOfBulbs;
						FIXTURE_CLASS_DATA[fixtureClassData.id] = fixtureClassObj;
						setFixtureClassFields();
						
					} else if(fixtureClassData.length > 0){
						$.each(fixtureClassData, function(i, fixtureclass) {
							$('#fixtureClass').append($('<option></option>').val(fixtureclass.id).html(fixtureclass.name));
							$('#fixtureClass').val(addedFixtureId);
							var fixtureClassObj = {};
							fixtureClassObj.id = fixtureclass.id;
							fixtureClassObj.name = fixtureclass.name;
							fixtureClassObj.noOfBallasts = fixtureclass.noOfBallasts;
							fixtureClassObj.voltage = fixtureclass.voltage;
							fixtureClassObj.ballastId = fixtureclass.ballast.id;
							fixtureClassObj.bulbId = fixtureclass.bulb.id;
							fixtureClassObj.ballastName = fixtureclass.ballast.name;
							fixtureClassObj.bulbName = fixtureclass.bulb.name;
							fixtureClassObj.lampNum = fixtureclass.ballast.noOfBulbs;
							FIXTURE_CLASS_DATA[fixtureclass.id] = fixtureClassObj;
							setFixtureClassFields();
							
						});
					}
				}
			}
		},
		dataType:"json",
		contentType: "application/json; charset=utf-8"
	});
}


function saveFxGridParameters(grid) {       
    var gridInfo = new Object();
    gridInfo.selarrrow = grid.jqGrid('getGridParam', 'selarrrow');
    localStorage.setItem("GridParam",JSON.stringify(gridInfo));
}
function fillingFxGridWithUserSelection(gridName)
{
	//Resetting the Fixture grid according to user selections
	 var gridParams = localStorage.getItem("GridParam");
	    if(gridParams !=null && gridParams!="")
		    { 
	    	 	var grid = gridName;  
	    		jQuery(grid).jqGrid("resetSelection");
		        var gridInfo = $.parseJSON(gridParams); 
		        if(gridInfo!=null && gridInfo.selarrrow!=null)
		        {
			     	var selFixtureRows = gridInfo.selarrrow;
		 	        if(selFixtureRows != null && selFixtureRows != undefined){
				        for(var i=0;i<selFixtureRows.length;i++){
				        		jQuery(grid).jqGrid('setSelection', selFixtureRows[i],false);
					   	}
		        	}
		        }
		    } 
}
function checkGridSelectionEnable()
{
	var ids = jQuery("#strobed-fx-list-grid").jqGrid('getGridParam', 'selarrrow');
	if(ids!=null && (ids.length>1 || ids.length==0))
	{
		// Reset Current Fixture object if Multiple selection or none of the fixture selected
		CURRENT_FIXTURE_OBJECT={};
		return true;
	}else
	{
		return false;
	}
}
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
	if(commission_retry_counter < 30) {
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
		$("#currentprofile").removeAttr("disabled");
		$("#fixtureClass").removeAttr("disabled");
		$("#description").removeAttr("disabled");
		$("#notes").removeAttr("disabled");
		
		//enable buttons
		$('#addFxClass').removeAttr("disabled");
		$('#editFxClass').removeAttr("disabled");
		$('#fxcd-update-btn').removeAttr("disabled");
		$('#fxcd-undo-btn').removeAttr("disabled");
		$('#fxcd-commission-btn').removeAttr("disabled");
	} else {
		//disable form elements
		$("#fixturename").attr("disabled", true);
		$("#currentprofile").attr("disabled", true);
		$("#fixtureClass").attr("disabled", true);
		$("#description").attr("disabled", true);
		$("#notes").attr("disabled", true);

		//disable buttons
		$('#addFxClass').attr("disabled", true);
		$('#editFxClass').attr("disabled", true);
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
	
	var strobedJGrid = jQuery("#strobed-fx-list-grid");
	var uncommissionJGrid = jQuery("#other-fx-list-grid");
	
	fixture_data_uncommissioned = [];
	fixture_data_strobed = [];

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

	// set user preferences of grid and then perform load. If this is firstime, function will handle that.
	// When grid data is loaded completely, reload the grid with user selection Options like User selected row.
	fillingFxGridWithUserSelection(strobedJGrid) ;
	
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
	saveFxGridParameters(strobedJGrid);
	var ids = jQuery("#strobed-fx-list-grid").jqGrid('getGridParam', 'selarrrow');
	if(gridIndex == 1){ // Strobed Grid
		if(rowid > 0){
			currentFixture = strobedJGrid.jqGrid('getRowData', rowid); 
			//In Multi selection list, if user deselect fixture one by one and deduced to single Fixture in the selection list, wrong Fixture was getting selected
			// In above case, loading Fixture detail of the remanining single Fixture
			if(ids.length==1)
			{
				currentFixture = strobedJGrid.jqGrid('getRowData', ids); 
			}
			uncommissionJGrid.jqGrid("resetSelection");
		} else {
			displayFxCommissionMessage("Select a fixture from Strobed fixure list", COLOR_FAILURE);
			return;
		}
	} else {
		if(rowid > 0){
			currentFixture = uncommissionJGrid.jqGrid('getRowData',rowid); 
			// Once Fixture is moved to Uncommissioned Grid, reset the highlight of the Fixture present in the Strobbed Grid
			localStorage.setItem("GridParam",null);
			strobedJGrid.jqGrid("resetSelection");
		} else {
			displayFxCommissionMessage("Select a fixture from Others fixure list", COLOR_FAILURE);
			return;
		}
	}	
	fixtureGridType = gridIndex;
	// If Multiselction property is enable, then disable the Fixture commissioning features
	var isEnable = checkGridSelectionEnable();
	if(gridIndex==1 && isEnable)
	{
		currentFixture={};
		$("#fixturename").val("");
		enableDisableUIControls(false);
	}else
	{
		loadFixtureDetails(currentFixture.id);
	}
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

function disableEnterKey(evt)
{
	 var keyCode = evt ? (evt.which ? evt.which : evt.keyCode) : event.keyCode;
     if (keyCode == 13) {
          return false;
     }
}

function setfixtureBasicInfo(fixture){
	$("#fixtureId").val(fixture.id);
	$("#fixturename").val(fixture.name);
	//$("#nooffixtures").val(fixture.nooffixtures); //No. of ballast

	if(FX_DEFAULT_VAL_PROFILE == ""){
		FX_DEFAULT_VAL_PROFILE = PROFILE_OPEN_OFFICE;
	}
	
	<c:forEach items="${groups}" var="group">
		<c:if test="${group.name == 'Open Office_Default'}">
			IS_PROFILE_OPEN_OFFICE_PRESENT = true;
		</c:if>
	</c:forEach>
	
	if(!IS_PROFILE_OPEN_OFFICE_PRESENT){
		FX_DEFAULT_VAL_PROFILE = $("#currentprofile").val();
	}
	
	if(FX_DEFAULT_VAL_PROFILE != ""){
		$("#currentprofile").val(FX_DEFAULT_VAL_PROFILE);
	} else {
		$("#currentprofile").val(fixture.currentprofile); //Profile
	}
	
	if(FX_DEFAULT_VAL_NO_OF_BALLASTS != ""){
		$("#nooffixtures").text(FX_DEFAULT_VAL_NO_OF_BALLASTS);
	} else {
		$("#nooffixtures").text(fixture.nooffixtures); //No. of ballast
	}
	
	if(FX_DEFAULT_VAL_NO_OF_LAMPS != ""){
		$("#noofbulbs").text(FX_DEFAULT_VAL_NO_OF_LAMPS);
	} else {
		$("#noofbulbs").text(fixture.noofbulbs); //No. of bulbs
	}
	
	if(FX_DEFAULT_VAL_VOLTAGE != ""){
		$("#voltage").text(FX_DEFAULT_VAL_VOLTAGE);
	}else{
		$("#voltage").text(fixture.voltage);	
	}
	
	if(FX_DEFAULT_VAL_BALLAST_NAME != ""){
		$("#ballasttype").text(FX_DEFAULT_VAL_BALLAST_NAME);
	}else{
		if(fixture.ballast != null){
			$("#ballasttype").text(fixture.ballast.name);
		}	
	}
		
	if(FX_DEFAULT_VAL_LAMP_NAME != ""){
		$("#lamptype").text(FX_DEFAULT_VAL_LAMP_NAME);
	}else{
		if(fixture.bulb != null){
			$("#lamptype").text(fixture.bulb.name);
		}	
	}	
	
	
	if(FX_DEFAULT_VAL_FIXTURE_CLASS != ""){
		$("#fixtureClass").val(FX_DEFAULT_VAL_FIXTURE_CLASS);
	}else{
		if(fixture.fixtureClassId == undefined ){
			$("#fixtureClass").prop('selectedIndex', 0);
		}else{
			$("#fixtureClass").val(fixture.fixtureClassId);
		}
			
	}
}

function setfixtureDetailInfo(fixture){
	$("#description").val(fixture.description);
	$("#notes").val(fixture.notes);
	$("#ballastLastServiceDate").val((fixture.ballastlastservicedate != null) ? fixture.ballastlastservicedate : "");
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
	
	if ($("#fixtureClass option:selected").text() == "Select Fixture Type"){
		displayFxCommissionMessage("Select a Fixture Type from the list", COLOR_FAILURE);
		return false;
	}

	//Storbed Grid - If Multiple Selection is Enable, commissioning cannot not be done.
	if(fixtureGridType==1)
	{
		var isEnable = checkGridSelectionEnable();
		if(isEnable)
		{
			displayFxCommissionMessage("Select a single fixture from the list", COLOR_FAILURE);
			return false;
		}
	}
	return true;
}

function getFixtureXML(){
	//Update Global variable
	CURRENT_FIXTURE_OBJECT.name = $("#fixturename").val();
	CURRENT_FIXTURE_OBJECT.noofbulbs = $("#noofbulbs").text();
	CURRENT_FIXTURE_OBJECT.nooffixtures = $("#nooffixtures").text();;
	CURRENT_FIXTURE_OBJECT.originalprofilefrom = CURRENT_FIXTURE_OBJECT.currentprofile;
	CURRENT_FIXTURE_OBJECT.currentprofile = $("#currentprofile").val();
	CURRENT_FIXTURE_OBJECT.voltage = $("#voltage").text();
	//CURRENT_FIXTURE_OBJECT.ballast.id = $("#ballasttype").val();
	CURRENT_FIXTURE_OBJECT.ballast.id = FIXTURE_CLASS_DATA[$("#fixtureClass").val()].ballastId;
	CURRENT_FIXTURE_OBJECT.ballastName = FIXTURE_CLASS_DATA[$("#fixtureClass").val()].ballastName;
	//CURRENT_FIXTURE_OBJECT.bulb.id = $("#lamptype").val();
	CURRENT_FIXTURE_OBJECT.bulb.id = FIXTURE_CLASS_DATA[$("#fixtureClass").val()].bulbId;
	CURRENT_FIXTURE_OBJECT.bulbName = FIXTURE_CLASS_DATA[$("#fixtureClass").val()].bulbName;
	CURRENT_FIXTURE_OBJECT.description = $("#description").val();
	CURRENT_FIXTURE_OBJECT.notes = $("#notes").val();
	CURRENT_FIXTURE_OBJECT.ballastlastservicedate = $("#ballastLastServiceDate").val();
	
	CURRENT_FIXTURE_OBJECT.fixtureClassId = $("#fixtureClass").val();

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
						"<fixtureClassId>"+CURRENT_FIXTURE_OBJECT.fixtureClassId+"</fixtureClassId>"+
					"</fixture>";
					
	FX_DEFAULT_VAL_PROFILE = CURRENT_FIXTURE_OBJECT.currentprofile;
	FX_DEFAULT_VAL_BALLAST = CURRENT_FIXTURE_OBJECT.ballast.id;
	FX_DEFAULT_VAL_LAMP = CURRENT_FIXTURE_OBJECT.bulb.id;
	FX_DEFAULT_VAL_VOLTAGE = CURRENT_FIXTURE_OBJECT.voltage;
	FX_DEFAULT_VAL_NO_OF_BALLASTS = CURRENT_FIXTURE_OBJECT.nooffixtures;
	FX_DEFAULT_VAL_NO_OF_LAMPS = CURRENT_FIXTURE_OBJECT.noofbulbs;
	FX_DEFAULT_VAL_FIXTURE_CLASS = CURRENT_FIXTURE_OBJECT.fixtureClassId;
	FX_DEFAULT_VAL_BALLAST_NAME = CURRENT_FIXTURE_OBJECT.ballastName;
	FX_DEFAULT_VAL_LAMP_NAME = CURRENT_FIXTURE_OBJECT.bulbName;
	
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

function rmaDevice(fixtureId, fixtureName, fixtureVersion){
	if(!validateFixtureForm()){
		return false;
	}

	var ver1 = fixtureVersion.charAt(0);
	var ver2 = CURRENT_FIXTURE_OBJECT.version.charAt(0);
	
	if(ver1 != ver2)
	{
		alert("RMA does not work for fixtures with different version");
		return false;
	}

	enableDisableUIControls(false);

	var bConfirm = confirm("Selected fixture : " + fixtureName + "  Replace with : " + $("#fixturename").val()
			+ "\n				Do you want to continue?");
	
	if(bConfirm) {
		displayFxCommissionMessage("Performing Fixture RMA...", COLOR_SUCCESS);
		$.ajax({
			url: "${rmaUrl}" + fixtureId + "/" + CURRENT_FIXTURE_OBJECT.id,
			type: "POST",
			async: false,
			dataType:"xml",
			contentType: "application/xml; charset=utf-8",
			success: function(data){
				displayFxCommissionMessage("RMA successful", COLOR_SUCCESS);
				// In case of RMA the old fixture id is retained so assign it accordingly
				CURRENT_FIXTURE_OBJECT.id = fixtureId;
				
				// Now commision the device on Success
				bRMAInProgress = true;
				validateFixture();
			},
			error: function() {
			}
		});
	}
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
			bRMAInProgress = false;
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
			getCommissionPlanObj("c_fx_floorplan").addFixture('floor', '${floorId}', $("#fixtureId").val(), bRMAInProgress);
		} catch(e){
			resetDevicePositionOnFloorplan();
			displayFxCommissionMessage("Fixture commissioned successfully, but failed to do placement on floor plan.", COLOR_SUCCESS);
		}
		// reset current fixture id
		CURRENT_FIXTURE_OBJECT = {};
		bRMAInProgress = false;
		//Once Fixture Successfully commissioned and placed on floorplan, remove the selection in both the grid
		resetSelection();
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
	bRMAInProgress = false;
	alert("Could not communicate with fixture using the provided fixture ID. \n\nTry the following: \n- Verify that the fixture name is entered \n- Check that the fixture has power \n- Check that the EM server is connected to the 'building lighting network"); //TODO : message
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

function getAllFixtureClass(){
	FIXTURE_CLASS_DATA = {};
	<c:forEach items="${fixtureclasses}" var="fixtureclass">
		var fixtureClassObj = {};
		fixtureClassObj.id = "${fixtureclass.id}";
		fixtureClassObj.name = "${fixtureclass.name}";
		fixtureClassObj.noOfBallasts = "${fixtureclass.noOfBallasts}";
		fixtureClassObj.voltage = "${fixtureclass.voltage}";
		fixtureClassObj.ballastId = "${fixtureclass.ballast.id}";
		fixtureClassObj.ballastName = "${fixtureclass.ballast.ballastName}";
		fixtureClassObj.lampNum = "${fixtureclass.ballast.lampNum}";
		fixtureClassObj.bulbId = "${fixtureclass.bulb.id}";
		fixtureClassObj.bulbName = "${fixtureclass.bulb.bulbName}";
		FIXTURE_CLASS_DATA["${fixtureclass.id}"] = fixtureClassObj;
	</c:forEach>
}

function setFixtureClassFields(){
	if ($("#fixtureClass option:selected").text() != "Select Fixture Type"){
		var fixtureClassId = $("#fixtureClass").val();
		var ballastId = FIXTURE_CLASS_DATA[fixtureClassId].ballastId;
		var ballastName = FIXTURE_CLASS_DATA[fixtureClassId].ballastName;
		var bulbId = FIXTURE_CLASS_DATA[fixtureClassId].bulbId;
		var bulbName = FIXTURE_CLASS_DATA[fixtureClassId].bulbName;
		var noOfBallasts = FIXTURE_CLASS_DATA[fixtureClassId].noOfBallasts;
		var voltage = FIXTURE_CLASS_DATA[fixtureClassId].voltage;
		//var noOfBulbs = BALLASTS_DATA[ballastId].lampnum;
		var noOfBulbs = FIXTURE_CLASS_DATA[fixtureClassId].lampNum;
		$("#ballasttype").text(ballastName);
		$("#lamptype").text(bulbName);
		$("#voltage").text(voltage);
		$("#nooffixtures").text(noOfBallasts);
		$("#noofbulbs").text(noOfBulbs);
	}else{
		setfixtureBasicInfo(CURRENT_FIXTURE_OBJECT);
	}
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
//Press the "Identify" button which will allow the Fixture to go to Dimming Cycle i.e. OFF -> ON -> Validation
function identifyFixture()
{
	clearFxCommissionMessage();
	var currentFixture = {};
	var uncommissionJGrid = jQuery("#other-fx-list-grid");
	var id = jQuery("#other-fx-list-grid").jqGrid('getGridParam', 'selrow');
	if(id!=null && id.length>0)
	{
		currentFixture = uncommissionJGrid.jqGrid('getRowData', id);
		$.ajax({
			url: "${identifyFixtureUrl}"+ currentFixture.id +"/?ts="+new Date().getTime(),
			type: "POST",
			dataType:"xml",
			success: function(data){
				//Fixture Validation SUCCESS
			}
		});
	}else
	{
		displayFxCommissionMessage("Please select an SU from the Others list before clicking on the Identify button", COLOR_FAILURE);
		return false;
	}
}

function unstrobeFixture()
{
	clearFxCommissionMessage();
	var ids = jQuery("#strobed-fx-list-grid").jqGrid('getGridParam', 'selarrrow');
	var currentFixture = {};
	var strobedJGrid = jQuery("#strobed-fx-list-grid");
	if (ids!=null && ids.length>0) {
		var xmlData="<fixtures>";
        for (var i=0; i<ids.length; i++) {
       		currentFixture = strobedJGrid.jqGrid('getRowData', ids[i]);
          	xmlData+="<fixture><id>"+currentFixture.id+"</id></fixture>";
        }
        xmlData+="</fixtures>";
    	$.ajax({
    		url: "${unstrobeFixtureUrl}"+GATEWAY_ID+"/?ts="+new Date().getTime(),
    		type: "POST",
    		data: xmlData, 
    		dataType:"xml",
    		contentType: "application/xml; charset=utf-8",
    		success: function(data){
    			//Fixture(s) unstrobbed successfuly
    			resetSelection();
    		}
    	});
	}else
	{
		displayFxCommissionMessage("Please select an SU in the Strobed list before clicking on the Unstrobe button", COLOR_FAILURE);
		return false;
	}
}
function resetSelection()
{
	var strobedJGrid = jQuery("#strobed-fx-list-grid");
	var uncommissionJGrid = jQuery("#other-fx-list-grid");
	strobedJGrid.jqGrid("resetSelection");
	uncommissionJGrid.jqGrid("resetSelection");
	localStorage.setItem("GridParam",null);
	fixtureGridType=0;
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
		 <td class="highlightGray"><div style="display: inline" class="label">Available Fixtures</div>
		 <div style="display: inline;padding-right:10px;float: right;"><button id=unstrobeBtn onclick="unstrobeFixture()">Unstrobe</button></div></td>
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
									<div style="background : none repeat scroll 0 0 #F2F2F2 !important;">
									<input type="hidden" id="fixtureId"/>
									<fieldset class="form-column-left">
										<div class="fieldWrapper">
											<div class="fieldlabel"><label for="fixturename">Name:</label></div>
											<div class="fieldInput"><input class="text readonly" id="fixturename" name="fixturename" size="40" /></div>
											<br style="clear:both;"/>
										</div>
										
										<div class="fieldPadding"></div>
										
										<div class="fieldWrapper">
											<div class="fieldlabel"><label for="fixtureClass">Fixture Type:</label></div>
											<div class="fieldInputCombo">
												<select class="text" id="fixtureClass" name="fixtureClass" onchange="javascript: setFixtureClassFields();">
													<option selected="selected">Select Fixture Type</option>
													<c:forEach items="${fixtureclasses}" var="fixtureclass">
														<option value="${fixtureclass.id}">${fixtureclass.name}</option>
													</c:forEach>
												</select>
											</div>
											<div class="fieldButton">
												<button id="addFxClass" style="text-align:center;">Add</button>
												<button id="editFxClass" style="text-align:center;">Edit</button>
											</div>
											<br style="clear:both;"/>
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
										<div style="height:31px;"></div>
										
									</fieldset>
									<br style="clear:both;"/>
									</div>
																		
									<div style="padding: 2px 4px 2px 13px;background : none repeat scroll 0 0 #F2F2F2 !important;border-top: 1px solid">
										<table id="fxcd-Table">
											<tr>
												<td style="font-weight:bold;">Ballast Type:</td>
												<td style="width: 200px">
												<label id="ballasttype" name="ballasttype"/>	
												</td>
												<td style="font-weight:bold;">Lamp Type:</td>
												<td style="width: 200px">
												<label id="lamptype" name="lamptype"/>	
												</td>
											</tr>
											<tr>
												<td style="font-weight:bold;">No. of Ballasts:</td>
												<td style="width: 200px">
												<label id="nooffixtures" name="nooffixtures"/>	
												</td>
												<td style="font-weight:bold;">No. of Lamps:</td>
												<td style="width: 200px">
												<label id="noofbulbs" name="noofbulbs"/>	
												</td>
												<td style="font-weight:bold;">Voltage(Vac):</td>
												<td style="width: 200px">
												<label id="voltage" name="voltage"/>	
												</td>
											</tr>
										</table>
									</div>
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
 <div id="newFixtureClassDialog"></div> 
</body>
</html>