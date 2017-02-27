<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/motionbits/op/createschedule/" var="createMotionBitsScheduleUrl" scope="request" />
<spring:url value="/services/org/motionbits/op/editschedule" var="editMotionBitsScheduleUrl" scope="request" />
<spring:url value="/services/org/motionbits/op/validateschedule/" var="validateMotionBitsScheduleUrl" scope="request" />
<spring:url value="/services/org/gemsgroups/op/creategroup" var="saveGemsGroupUrl" scope="request" />
<spring:url value="/services/org/gemsgroups/op/editgroup" var="editGemsGroupUrl" scope="request" />
<spring:url value="/services/org/gemsgroupfixture/op/applygroup/" var="applyGroupToFixtureUrl" scope="request" />
<spring:url value="/services/org/gemsgroupfixture/op/asssignFixturesToGroup/" var="assignFixturesToGroupURL" scope="request" />
<spring:url value="/services/org/fixture/list/" var="getAvailableFixtureUrl" scope="request" />
<spring:url value="/services/org/gemsgroupfixture/list/" var="getGroupFixtureListURL" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<style>
	#mbc-new-fixture-grid-container tr.jqgroup {background-color: #F1F1F1 !important;}
	#mbc-new-fixture-grid-container {border-bottom: 1px solid #AAAAAA;}
	#mbc-new-fixture-grid-container th {text-align: left !important; padding-left: 5px !important;}
	div.mbc-message-text {font-weight:bold; float: left; padding-top: 5px;}
	
	div.field-label{float:left; width:28%; font-weight:bold;}
	div.field-input{float:left; width:72%;}
</style>
</head>

<script type="text/javascript">
var MAX_ROW_NUM = 99999;
var group_selIds=null;
var GROUP_GRID = $("#mbc-new-fixture-table");
var SELECTED_FIXTURES_LIST;
var groupAssignedFixtureList = undefined;
var orignalSchedularName = "" ;

$(document).ready(function() {
	
	createNewFixturesGrid();
});

function resizeNewFixturesGrid(){
	//resize new fixture grid
	var gridContainerEL = document.getElementById("mbc-new-fixture-grid-container");
	forceFitJQgridHeight(jQuery("#mbc-new-fixture-table"), gridContainerEL.offsetHeight);
	jQuery("#mbc-new-fixture-table").jqGrid("setGridWidth", 520 );
}

function forceFitJQgridHeight(jgrid, containerHeight){
	var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
	var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
	var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
	jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - gridHeaderFooterHeight) * .99)); 
}

//FOR GROUP
function fillingGridWithUserSelection(gridName) {
    if(group_selIds != null && group_selIds != undefined){
        for(var i=0;i<group_selIds.length;i++){
        	jQuery(gridName).jqGrid('setSelection', group_selIds[i]);
	   	}
    }
}

function saveGridParameters(grid) {
	group_selIds = jQuery(grid).getGridParam('selarrrow');
}

function setMessage(msg, color){
	$("#mbc-message-div").css("color", color);
	$("#mbc-message-div").html(msg);
}

function createNewFixturesGrid(){
	jQuery("#mbc-new-fixture-table").jqGrid({
		datatype: "local",
		autowidth: true,
		scrollOffset: 0,
		hoverrows: false,
		forceFit: true,
		sortname: 'name',
	   	rowNum: MAX_ROW_NUM,
	   	colNames:['id', 'Selected Fixtures'],
	   	colModel:[
  	   		{name:'id', index:'id', hidden: true},
	   		{name:'name', index:'name' , sorttype : 'string'}
	   	],
	    viewrecords: true,
	    multiselect: true,
	    onSortCol: function (index, columnIndex, sortOrder) {
	   		saveGridParameters(GROUP_GRID);
	    },
	    gridComplete: function(){
	    	fillingGridWithUserSelection(GROUP_GRID);
	    	ModifyGridDefaultStyles();
	    }
	});
	
	if('${mode}' != "edit") {
		if(SELECTED_FIXTURES != undefined && SELECTED_FIXTURES.length > 0) {
			basePage = "floorplan";
			$.each(SELECTED_FIXTURES, function(){
				 if(this.version.match("^2.")) {
					 jQuery("#mbc-new-fixture-table").jqGrid('addRowData', this.id, this);
					 jQuery("#mbc-new-fixture-table").jqGrid('setSelection', this.id);
				 }
			 });
		}
	}
	else {
		basePage = "devices";
		$.ajax({
			type: 'GET',
			url: "${getAvailableFixtureUrl}"+"floor"+"/"+"${motionBitsSchedule.motionBitGroup.floor.id}"+"/1000"+"?ts="+new Date().getTime(),
			data: "",
			beforeSend: function() {
			     setAssignGemsGroupMessage("Loading,Please Wait...", "green");
			  },
			  complete: function(){
			     setAssignGemsGroupMessage("", "black");
			  },
			success: function(data){
					if('${mode}' == "edit"){
						$('#name').val("${motionBitsSchedule.displayName}");
						$('#from').val("${motionBitsSchedule.captureStart}");
						$('#to').val("${motionBitsSchedule.captureEnd}");
						orignalSchedularName = "${motionBitsSchedule.name}" ;
						if("${motionBitsSchedule.bitLevel}" == 1)
						{
							var radio = document.getElementById("bitLevelOne");
							radio.checked = "checked";
						}
						else
						{
							var radio = document.getElementById("bitLevelTwo");
							radio.checked = "checked";
						}
						if("${motionBitsSchedule.transmitFreq}" == 1)
						{
							var radio = document.getElementById("freqOne");
							radio.checked = "checked";
						}
						else
						{
							var radio = document.getElementById("freqFive");
							radio.checked = "checked";
						}
						getAssignedGroupFixtures();
					}
					if(data != null) {
						fixturesData = data.fixture;
						var selectedFixtures = undefined;
						if(fixturesData.length != undefined && fixturesData.length > 0){
							
							for(var i=0; i<fixturesData.length ; i++){
								if(fixturesData[i].state == "COMMISSIONED" && fixturesData[i].version.match("^2.")){
									if('${mode}' == "edit"){
										if(groupAssignedFixtureList != null && groupAssignedFixtureList.length != undefined) {
											for(var j=0; j<groupAssignedFixtureList.length ; j++){
												if(groupAssignedFixtureList[j].fixture.id == fixturesData[i].id) {
													 jQuery("#mbc-new-fixture-table").jqGrid('addRowData', fixturesData[i].id , fixturesData[i]);
													 jQuery("#mbc-new-fixture-table").jqGrid('setSelection', fixturesData[i].id);
													 fixturesData[i].version = '0';
												}
											}
										}
										else if(groupAssignedFixtureList != null && groupAssignedFixtureList != undefined && groupAssignedFixtureList.fixture != undefined) {
											if(groupAssignedFixtureList.fixture.id == fixturesData[i].id) {
												 jQuery("#mbc-new-fixture-table").jqGrid('addRowData', fixturesData[i].id , fixturesData[i]);
												 jQuery("#mbc-new-fixture-table").jqGrid('setSelection', fixturesData[i].id);
												 fixturesData[i].version = '0';
											}
										}
									}
								}							
							}
							
							for(var i=0; i<fixturesData.length ; i++){
								if(fixturesData[i].state == "COMMISSIONED" && fixturesData[i].version.match("^2.")){
									jQuery("#mbc-new-fixture-table").jqGrid('addRowData', fixturesData[i].id , fixturesData[i]);
								}							
							}
	
						}
						else {
							if(fixturesData.state == "COMMISSIONED" && fixturesData.version.match("^2.")){
								jQuery("#mbc-new-fixture-table").jqGrid('addRowData', fixturesData.id , fixturesData);
								if(groupAssignedFixtureList != null) {
									if(groupAssignedFixtureList.length != undefined) {
										for(var j=0; j<groupAssignedFixtureList.length ; j++){
											if(groupAssignedFixtureList[j].fixture.id == fixturesData.id) {
												 jQuery("#mbc-new-fixture-table").jqGrid('setSelection', fixturesData.id);
											}
										}
									}
									else if(groupAssignedFixtureList != undefined && groupAssignedFixtureList.fixture != undefined) {
										if(groupAssignedFixtureList.fixture.id == fixturesData.id) {
											 jQuery("#mbc-new-fixture-table").jqGrid('setSelection', fixturesData.id);
										}
									}
								}
							}
						}
					}
				},
			dataType:"json",
			contentType: "application/json; charset=utf-8",
		});
	}
	resizeNewFixturesGrid();
}

function ModifyGridDefaultStyles() {  
	   $('#' + "mbc-new-fixture-table" + ' tr').removeClass("ui-widget-content");
	   $('#' + "mbc-new-fixture-table" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "mbc-new-fixture-table" + ' tr:nth-child(odd)').addClass("oddTableRow");
}

function getAssignedGroupFixtures() {
	
	$.ajax({
		type: 'GET',
		url: "${getGroupFixtureListURL}"+ "${motionBitsSchedule.motionBitGroup.id}" +"?ts="+new Date().getTime(),
		data: "",
		async: false,
		success: function(data){
			if(data != null) {
				groupAssignedFixtureList = data.gemsGroupFixture;
			}
			else {
				groupAssignedFixtureList = null;
			}
			
		},
		dataType:"json",
		contentType: "application/json; charset=utf-8",
	});
}

var isTimingValid = true;
function validateTime(inputField) { 

	if(inputField.value!="")
	{
	  var isValid = /^([0-1]?[0-9]|2[0-4]):([0-5][0-9])(:[0-5][0-9])?$/.test(inputField.value); 
	  
        if (isValid) { 
            inputField.style.borderColor = '#757575'; 
            setMessage("", "black");
        } else {
            setMessage("Please enter the time in HH:mm format", "red");
        	//inputField.value="";
            inputField.style.borderColor = '#ff0000';
        } 
        isTimingValid = isValid;
        return isValid; 
	
	}
	else
	{
		isTimingValid = true;
        setMessage("", "black");
		inputField.style.borderColor = '#757575'; 
	}
	return true;
}

function createNewGemsGroup(){
	$("#mbc-save-btn").attr("disabled", true);
	$("#mbc-start-now-btn").attr("disabled", true);
	setMessage("Processing...", "black");
	
	var dataXML = "<gemsGroup>"+
						"<id></id>"+
						"<name>"+ $("#name").val().trim() +"</name>"+
						"<description></description>"+
						"<type>"+
							"<id>5</id>"+
						"</type>"+
					"</gemsGroup>";
					
 	$.ajax({
 		type: 'POST',
 		url: "${saveGemsGroupUrl}"+"?ts="+new Date().getTime(),
 		data: dataXML,
 		success: function(data){
			if(data != null){
				var xml=data.getElementsByTagName("response");
				for (var j=0; j<xml.length; j++) {
					var status = xml[j].getElementsByTagName("status")[0].childNodes[0].nodeValue;
					var gemsGroupId = xml[j].getElementsByTagName("msg")[0].childNodes[0].nodeValue;
					if(basePage == "devices") {
						reloadGroupsFrame();
					}
					applyGroupToFixtures(gemsGroupId);
				}
			}
		},
		error: function(){
			setMessage("Failed to create group", "red");
		},
		complete: function() {
			$("#mbc-save-btn").removeAttr("disabled");
			$("#aggf-join-btn").removeAttr("disabled");
		},
 		dataType:"xml",
 		contentType: "application/xml; charset=utf-8",
 	});
}

function validateSchedule() {
	var selIds = jQuery("#mbc-new-fixture-table").getGridParam('selarrrow');
	var dataXML = "";
	for(var i=0; i<selIds.length; i++){
		var fixtureJson = jQuery("#mbc-new-fixture-table").jqGrid('getRowData', selIds[i]);
		dataXML += "<fixture><id>"+ fixtureJson.id +"</id><name>" + fixtureJson.name + "</name></fixture>";
	}
	dataXML = "<fixtures>"+dataXML+"</fixtures>";
	var url;
	if('${mode}' != "edit") {
		url = "${validateMotionBitsScheduleUrl}"+$("#name").val().trim()+"/"+$("#from").val().trim()+"/"+$("#to").val().trim()+"/0"+"?ts="+new Date().getTime();
	}
	else {
		url = "${validateMotionBitsScheduleUrl}"+$("#name").val().trim()+"/"+$("#from").val().trim()+"/"+$("#to").val().trim()+"/"+"${motionBitsSchedule.id}"+"?ts="+new Date().getTime();
	}
 	$.ajax({
 		type: 'POST',
 		url: url,
 		data: dataXML,
 		success: function(data){
 			if(data == "") {
 				// Success
 				if('${mode}' == "edit")
 					assignToGroup();
 				else
 					createNewGemsGroup();
 			}
 			else {
				setMessage(data, "red");
 			}
		},
		error: function(){
			setMessage("Failed to validate the schedule", "red");
		},
 		dataType:"html",
 		contentType: "application/xml; charset=utf-8",
 	});
}

function applyGroupToFixtures(gemsGroupId){
	var selIds = jQuery("#mbc-new-fixture-table").getGridParam('selarrrow');
	var dataXML = "";
	if(selIds.length > 0){
		for(var i=0; i<selIds.length; i++){
			var fixtureJson = jQuery("#mbc-new-fixture-table").jqGrid('getRowData', selIds[i]);
			dataXML += "<fixture><id>"+ fixtureJson.id +"</id></fixture>";
		}
		dataXML = "<fixtures>"+dataXML+"</fixtures>";
	}
	
 	$.ajax({
 		type: 'POST',
 		url: "${applyGroupToFixtureUrl}"+gemsGroupId+"?ts="+new Date().getTime(),
 		data: dataXML,
 		success: function(data){
	 			saveMotionBitsSchedule(gemsGroupId, true);
		},
		error: function(){
			clearInterval(pollServer);
			setMessage("Group created successfully but failed to assign fixtures", "red");
		},
 		dataType:"html",
 		contentType: "application/xml; charset=utf-8",
 	});
}

function assignToGroup(){
	setMessage("", "black");
	var proceed = confirm("This will remove any unselected fixture from the configuration and assign only current selected fixtures. Do you wish to continue?");
	if(proceed==true) {
		setMessage("Processing...", "black");
		$.when(proceedWithAssignment());
	}
}

function proceedWithAssignment() {
	var selIds = jQuery("#mbc-new-fixture-table").getGridParam('selarrrow');
	var dataXML = "";
	for(var i=0; i<selIds.length; i++){
		var fixtureJson = jQuery("#mbc-new-fixture-table").jqGrid('getRowData', selIds[i]);
		dataXML += "<fixture><id>"+ fixtureJson.id +"</id></fixture>";
	}
	dataXML = "<fixtures>"+dataXML+"</fixtures>";
	
 	$.ajax({
 		type: 'POST',
 		url: "${assignFixturesToGroupURL}"+"${motionBitsSchedule.motionBitGroup.id}"+"?ts="+new Date().getTime(),
 		data: dataXML,
 		success: function(data){
 			saveMotionBitsSchedule(0, false);
		},
		error: function(){
			clearInterval(pollServer);
			setMessage("Failed to change assignment of fixtures", "red");
		},
 		dataType:"html",
 		contentType: "application/xml; charset=utf-8",
 	});
}

function setAssignGemsGroupMessage(msg, color){
	$("#mbc-message-div").css("color", color);
	$("#mbc-message-div").html(msg);
}


function pollStatus(gid) {

	pollServer = setInterval(
			function() {
				$.ajax({
						type : "POST",
						cache : false,
						dataType : "html",
						url : '<spring:url value="/services/org/gemsgroups/status/"/>' + gid,
						success : function(msg) {
							if(msg != undefined && msg != "") {
								var parts = msg.split(',');
								Dtotal = Number(parts[3]);
								Dproc = Number(parts[4]);
								Dsuccess = Number(parts[5]);
								Atotal = Number(parts[0]);
								Aproc = Number(parts[1]);
								Asuccess = Number(parts[2]);
								out = "";
								if(Dproc > 0) {
									if(Dproc == Dsuccess) {
										out = "Processed " + Dproc + " removal requests. ";	
									}
									else {
										failed = Dproc - Dsuccess;
										out = "Processed " + Dproc + " removal requests (" + failed + " failed). ";
									}
									
								}
								if(Atotal > 0) {
									if(Aproc == Asuccess) {
										out = out + "Processed " + Aproc + " of " + Atotal + " assignment requests";	
									}
									else {
										failed = Aproc - Asuccess;
										out = out + "Processed " + Aproc + " of " + Atotal + " assignment requests" + " (" + failed + " failed)";
									}
										
								}
								if(Dproc > 0 || Atotal > 0) {
									setMessage(out, "black");
								}
							}
						}
					});
			}, 5000);
}

function dateTimeCompare(time1,time2) { 
	  var t1 = new Date(); 
	  var time1Parts = time1.split(":"); 
	  t1.setHours(time1Parts[0],time1Parts[1],0,0); 
	  var t2 = new Date(); 
	  var time2Parts = time2.split(":"); 
	  t2.setHours(time2Parts[0],time2Parts[1],0,0); 
	  // returns 1 if greater, -1 if less and 0 if the same 
	  if (t1.getTime()>t2.getTime()) return 1; 
	  if (t1.getTime()<t2.getTime()) return -1; 
	  return 0; 
} 

function startNow() {
	validateAndSave('true');
}

function validateAndSave(bStartNow) {

	setMessage("", "black");
	
	if($("#name").val().trim()==""){
		setMessage("Please enter name", "red");
		return false;
	}
	
	var selIds = jQuery("#mbc-new-fixture-table").getGridParam('selarrrow');
	if(selIds.length == 0){
		setMessage("Please select a fixture", "red");
		return false;
	}
	
	if(isTimingValid == false) {
    	setMessage("Please enter the time in HH:mm format", "red");
    	return false;
	}

	if(bStartNow == 'true') {
		var date = new Date();
		
		var t = date.getHours() + ":" + date.getMinutes();
		$("#from").val(t);
	}
	
	var from = $("#from").val();
	var to = $("#to").val();
	
	if(from == "") {
		setMessage("Please enter \"from\" time", "red");
		return false;
	}

	if(to == "") {
		setMessage("Please enter \"to\" time", "red");
		return false;
	}
	
	if(dateTimeCompare(to, from) == -1)
	{
		setMessage("\"To\" time has to be greater than \"from\" time", "red");
		return false;
	}

	if(dateTimeCompare(to, from) == 0)
	{
		setMessage("\"To\" and \"from\" time have to be different", "red");
		return false;
	}
	setMessage("Processing...", "black");
	
	validateSchedule();
}
	
function saveMotionBitsSchedule(gemsGroupId, bCreate) {
	var radioFreqOne = document.getElementById("freqOne");
	var freq;
	if(radioFreqOne.checked == true)
		freq = "1";
	else
		freq = "5";

	var radioLevel = document.getElementById("bitLevelOne");
	var level;
	if(radioLevel.checked == true)
		level = "1";
	else
		level = "2";

	var dataXML;
	var url;

	if(bCreate == true) {
		url = "${createMotionBitsScheduleUrl}"+gemsGroupId+"?ts="+new Date().getTime();
		dataXML = "<motionBitsScheduler>"+
						"<id></id>"+
						"<displayName>"+ $("#name").val().trim() +"</displayName>"+
						"<captureStart>"+ $("#from").val().trim() +"</captureStart>"+
						"<captureEnd>"+ $("#to").val().trim() +"</captureEnd>"+
						"<transmitFreq>"+ freq +"</transmitFreq>"+
						"<bitLevel>"+ level +"</bitLevel>"+
						"<daysOfWeek></daysOfWeek>"+
					"</motionBitsScheduler>";
	}
	else 
	{
		url = "${editMotionBitsScheduleUrl}"+"?ts="+new Date().getTime();
		dataXML = "<motionBitsScheduler>"+
						"<id>${motionBitsSchedule.id}</id>"+
						"<displayName>"+ $("#name").val().trim() +"</displayName>"+
						"<name>"+ orignalSchedularName.trim() +"</name>"+
						"<captureStart>"+ $("#from").val().trim() +"</captureStart>"+
						"<captureEnd>"+ $("#to").val().trim() +"</captureEnd>"+
						"<transmitFreq>"+ freq +"</transmitFreq>"+
						"<bitLevel>"+ level +"</bitLevel>"+
						"<motionBitGroup>"+
						"<id>${motionBitsSchedule.motionBitGroup.id}</id>"+
						"</motionBitGroup>"+
						"<daysOfWeek></daysOfWeek>"+
					"</motionBitsScheduler>";
	}
	
	$.ajax({
 		type: 'POST',
 		url: url,
 		data: dataXML,
 		success: function(data){
 			if(bCreate == true) {
				setMessage("Motion bits configuration created/started successfully", "green");
 			}
 			else
 			{
				setMessage("Motion bits configuration saved successfully", "green");
 			}
		},
		error: function(){
			setMessage("Motion bits configuration creation failed", "red");
		},
		complete: function() {
			$("#mbc-save-btn").removeAttr("disabled");
			$("#aggf-join-btn").removeAttr("disabled");
		},
 		dataType:"xml",
 		contentType: "application/xml; charset=utf-8"
 	});
}

</script>

<body id="mbc-main-box">
<div id="mbc-tabs-body">
	
	<div id="tab-create-mb_schedule">
		<table width=100% height=100% style="padding:0 10px;">	
			<tr height=24px>
				<td colspan="2" valign="top" height=24px>
					<div id="mbc-message-div" class="mbc-message-text"></div>
				</td>
			</tr>
			<tr height=30px>
				<td colspan="2">
					<div class="field-label">Name:</div> 
					<div class="field-input"><input id="name" type="text"/></div>
				</td>
			</tr>
			<tr height=30px>
				<td colspan="2">
					<div class="field-label">Frequency:</div>
					<div class="field-input">
						<input style="display: inline;" type="radio" id="freqOne" checked="checked" name="freq" value="1"/>
						<span style="display: inline;">1 minute</span> &nbsp;&nbsp;
						<input style="display: inline;" type="radio" id="freqFive" name="freq" value="5"/>
						<span style="display: inline;">5 minute</span>
					</div>
				</td>
			</tr>
			<tr height=30px>
				<td colspan="2">
					<div class="field-label">Bit level:</div>
					<div class="field-input">
						<input style="display: inline;" type="radio" id="bitLevelOne" checked="checked" name="bitlevel" value="1"/>
						<span style="display: inline;">1 bit</span> &nbsp;&nbsp;
						<input style="display: inline;" type="radio" id="bitLevelTwo" name="bitlevel" value="2"/>
						<span style="display: inline;">2 bit</span>
					</div>
				</td>
			</tr>
			<tr height=30px>
				<td colspan="2">
					<div class="field-label">Duration(HH:mm format):</div>
					<div class="field-input">
						<label>From:</label>&nbsp&nbsp<input id="from" type="text" onchange="validateTime(this);"/>
						<label>To:</label>&nbsp&nbsp<input id="to" type="text" onchange="validateTime(this);"/>
						<br style="clear:both;"/>
					</div>
				</td>
			</tr>
			<tr height=220px>
				<td id="mbc-new-fixture-grid-container" colspan="2" valign="top">
					<span style="font-weight: bold; font-size: 0.85em">*Only 2.0+ sensors can participate in motion bits.</span>
					<table id="mbc-new-fixture-table"></table>
				</td>
			</tr>
			<tr>
				<td valign="top">
					<button id="mbc-save-btn" onclick="validateAndSave('false');">Save</button>
				</td>
			<c:if test="${mode != 'edit'}">
				<td valign="top">
					<button id="mbc-start-now-btn" onclick="startNow();">Start now</button>
				</td>
			</c:if>
			</tr>
		</table>
	</div>
	
</div>
	
</body>
</html>