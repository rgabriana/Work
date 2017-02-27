<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/gemsgroups/op/creategroup" var="saveGemsGroupUrl" scope="request" />
<spring:url value="/services/org/gemsgroups/op/editgroup" var="editGemsGroupUrl" scope="request" />
<spring:url value="/services/org/gemsgroupfixture/op/applygroup/" var="applyGroupToFixtureUrl" scope="request" />
<spring:url value="/services/org/gemsgroupfixture/op/asssignFixturesToGroup/" var="assignFixturesToGroupURL" scope="request" />
<spring:url value="/services/org/gemsgroups/loadbyname/" var="loadGroupByNameUrl" scope="request" />
<spring:url value="/services/org/fixture/list/" var="getAvailableFixtureUrl" scope="request" />
<spring:url value="/services/org/gemsgroupfixture/list/" var="getGroupFixtureListURL" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

<style>
	#aggf-new-fixture-grid-container tr.jqgroup {background-color: #F1F1F1 !important;}
	#aggf-new-fixture-grid-container {border-bottom: 1px solid #AAAAAA;}
	#aggf-new-fixture-grid-container th {text-align: left !important; padding-left: 5px !important;}
	div.aggf-message-text {font-weight:bold; float: left; padding-top: 5px;}
	
	#aggf-group-name {height: 24px; width:99.5%;}
	#aggf-group-type {height: 26px; width:100%;}
	#aggf-group-combo {height: 26px; width:100%;}
	div.field-label{float:left; width:39%; font-weight:bold;}
	div.field-input{float:left; width:60%;}
</style>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
</style>


<script type="text/javascript">

var basePage = ""; 

$("#aggf-group-type").empty();
$('#aggf-group-type').append($('<option></option>').val("2").html("${grouptypes}"));


$("#aggf-group-combo-create").empty();
<c:forEach items="${groups2}" var="group">
	$('#aggf-group-combo-create').append($('<option></option>').val("${group.id}").html("${group.groupName}"));
</c:forEach>

<c:if test="${empty groups2}">
	$("#aggf-join-btn").hide();
</c:if>

var GROUP_GRID = $("#aggf-new-fixture-table");

var MAX_ROW_NUM = 99999;

var group_selIds=null;

var fixturesData="";

$(document).ready(function() {
	
	group_selIds=null;
	
	createNewFixturesGrid();
});


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

var groupAssignedFixtureList = undefined;

function createNewFixturesGrid(){
	jQuery("#aggf-new-fixture-table").jqGrid({
		datatype: "local",
		autowidth: true,
		scrollOffset: 0,
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
	    }
	});
	
	if(SELECTED_FIXTURES_TO_ASSIGN_GROUPS != undefined && SELECTED_FIXTURES_TO_ASSIGN_GROUPS.length > 0) {
		basePage = "floorplan";
		var noOfSensorsVersion2 = 0;
		$.each(SELECTED_FIXTURES_TO_ASSIGN_GROUPS, function(){
			 if(parseFloat(this.version.substring(0,3)) >= 2.0) {
				 jQuery("#aggf-new-fixture-table").jqGrid('addRowData', this.id, this);
				 jQuery("#aggf-new-fixture-table").jqGrid('setSelection', this.id);
				 noOfSensorsVersion2++;
			 }
		 });
		
		if(noOfSensorsVersion2 == 0){
			$('input:radio[name=sensorVersion]')[1].click();
		}
		
	}
	else {
		basePage = "devices";
		$.ajax({
			type: 'GET',
			url: "${getAvailableFixtureUrl}"+treenodetype+"/"+treenodeid+"/1000"+"?ts="+new Date().getTime(),
			data: "",
			beforeSend: function() {
			     setAssignGemsGroupMessage("Loading,Please Wait...", "green");
			  },
			  complete: function(){
			     setAssignGemsGroupMessage("", "black");
			  },
			success: function(data){
					if('${mode}' == "edit"){
						$('#groupId').val(selectedGroupId);
						$('#groupName').val(selectedGroupName);
						getAssignedGroupFixtures();
					}
					if(data != null) {
						fixturesData = data.fixture;
						if('${mode}' == "edit"){
							
						var editFixturesVersion = "";
						
						if(groupAssignedFixtureList != null && groupAssignedFixtureList.length != undefined) {
														
							if(parseFloat(groupAssignedFixtureList[0].fixture.version.substring(0,3)) >= 2.0 ){
								$('#groupSensorType').text("2.0+");
								editFixturesVersion = "2.0+";
							}
							if(parseFloat(groupAssignedFixtureList[0].fixture.version.substring(0,3)) >= 1.5 && parseFloat(groupAssignedFixtureList[0].fixture.version.substring(0,3)) < 2.0){
								$('#groupSensorType').text("1.5+");
								editFixturesVersion = "1.5+";
							}
							
						}
						else if(groupAssignedFixtureList != null && groupAssignedFixtureList != undefined && groupAssignedFixtureList.fixture != undefined) {
							
							
							if(parseFloat(groupAssignedFixtureList.fixture.version.substring(0,3)) >= 2.0){
								$('#groupSensorType').text("2.0+");
								editFixturesVersion = "2.0+";
							}
							if(parseFloat(groupAssignedFixtureList.fixture.version.substring(0,3)) >= 1.5 && parseFloat(groupAssignedFixtureList.fixture.version.substring(0,3)) < 2.0){
								$('#groupSensorType').text("1.5+");
								editFixturesVersion = "1.5+";
							}
							
						}
							
						loadFixtureGrid(editFixturesVersion);	
							
						}
						else{
							loadFixtureGrid("2.0+");
						}
					}
				},
			dataType:"json",
			contentType: "application/json; charset=utf-8",
		});
	}
	 resizeNewFixturesGrid();
}


function loadFixtureGrid(fixtureVersion) {
	
	$('#aggf-new-fixture-table').jqGrid('clearGridData');
	
	
	if (fixtureVersion == "1.5+"){
		if (basePage == "devices"){
			
			var selectedFixtures = undefined;
			if(fixturesData.length != undefined && fixturesData.length > 0){
				
				for(var i=0; i<fixturesData.length ; i++){
					if(fixturesData[i].state == "COMMISSIONED" && parseFloat(fixturesData[i].version.substring(0,3)) >= 1.5 && parseFloat(fixturesData[i].version.substring(0,3)) < 2.0){
						if('${mode}' == "edit"){
							if(groupAssignedFixtureList != null && groupAssignedFixtureList.length != undefined) {
								for(var j=0; j<groupAssignedFixtureList.length ; j++){
									if(groupAssignedFixtureList[j].fixture.id == fixturesData[i].id) {
										 jQuery("#aggf-new-fixture-table").jqGrid('addRowData', fixturesData[i].id , fixturesData[i]);
										 jQuery("#aggf-new-fixture-table").jqGrid('setSelection', fixturesData[i].id);
										 fixturesData[i].version = '0';
									}
								}
							}
							else if(groupAssignedFixtureList != null && groupAssignedFixtureList != undefined && groupAssignedFixtureList.fixture != undefined) {
								if(groupAssignedFixtureList.fixture.id == fixturesData[i].id) {
									 
									 jQuery("#aggf-new-fixture-table").jqGrid('addRowData', fixturesData[i].id , fixturesData[i]);
									 jQuery("#aggf-new-fixture-table").jqGrid('setSelection', fixturesData[i].id);
									 fixturesData[i].version = '0';
								}
							}
						}
					}							
				}
				
				for(var i=0; i<fixturesData.length ; i++){
					if(fixturesData[i].state == "COMMISSIONED" && parseFloat(fixturesData[i].version.substring(0,3)) >= 1.5 && parseFloat(fixturesData[i].version.substring(0,3)) < 2.0){
						jQuery("#aggf-new-fixture-table").jqGrid('addRowData', fixturesData[i].id , fixturesData[i]);
					}							
				}
	
			}
			else {
				if(fixturesData.state == "COMMISSIONED" && parseFloat(fixturesData.version.substring(0,3)) >= 1.5 && parseFloat(fixturesData.version.substring(0,3)) < 2.0){
					jQuery("#aggf-new-fixture-table").jqGrid('addRowData', fixturesData.id , fixturesData);
					if(groupAssignedFixtureList != null) {
						if(groupAssignedFixtureList.length != undefined) {
							for(var j=0; j<groupAssignedFixtureList.length ; j++){
								if(groupAssignedFixtureList[j].fixture.id == fixturesData.id) {
									 jQuery("#aggf-new-fixture-table").jqGrid('setSelection', fixturesData.id);
								}
							}
						}
						else if(groupAssignedFixtureList != undefined && groupAssignedFixtureList.fixture != undefined) {
							if(groupAssignedFixtureList.fixture.id == fixturesData.id) {
								 jQuery("#aggf-new-fixture-table").jqGrid('setSelection', fixturesData.id);
							}
						}
				}
			}
		}
	}
	else{
		
		if(SELECTED_FIXTURES_TO_ASSIGN_GROUPS != undefined && SELECTED_FIXTURES_TO_ASSIGN_GROUPS.length > 0) {
			//basePage = "floorplan";
			$.each(SELECTED_FIXTURES_TO_ASSIGN_GROUPS, function(){
				 if(parseFloat(this.version.substring(0,3)) >= 1.5 && parseFloat(this.version.substring(0,3)) < 2.0) {
					 jQuery("#aggf-new-fixture-table").jqGrid('addRowData', this.id, this);
					 jQuery("#aggf-new-fixture-table").jqGrid('setSelection', this.id);
				 }
			 });
		}
		
	}
		
		$("#aggf-group-combo-create").empty();
		<c:forEach items="${groups1}" var="group">
			$('#aggf-group-combo-create').append($('<option></option>').val("${group.id}").html("${group.groupName}"));
		</c:forEach>
		
		<c:if test="${empty groups1}">
			$("#aggf-join-btn").hide();
		</c:if>
		
		<c:if test="${not empty groups1}">
		$("#aggf-join-btn").show();
		</c:if>
		
  }
	

 if(fixtureVersion == "2.0+"){
		
		if (basePage == "devices"){
			
			var selectedFixtures = undefined;
			if(fixturesData.length != undefined && fixturesData.length > 0){
				
				for(var i=0; i<fixturesData.length ; i++){
					if(fixturesData[i].state == "COMMISSIONED" && parseFloat(fixturesData[i].version.substring(0,3)) >= 2.0 ){
						if('${mode}' == "edit"){
							if(groupAssignedFixtureList != null && groupAssignedFixtureList.length != undefined) {
								for(var j=0; j<groupAssignedFixtureList.length ; j++){
									if(groupAssignedFixtureList[j].fixture.id == fixturesData[i].id) {
										 jQuery("#aggf-new-fixture-table").jqGrid('addRowData', fixturesData[i].id , fixturesData[i]);
										 jQuery("#aggf-new-fixture-table").jqGrid('setSelection', fixturesData[i].id);
										 fixturesData[i].version = '0';
									}
								}
							}
							else if(groupAssignedFixtureList != null && groupAssignedFixtureList != undefined && groupAssignedFixtureList.fixture != undefined) {
								if(groupAssignedFixtureList.fixture.id == fixturesData[i].id) {
									 
									 jQuery("#aggf-new-fixture-table").jqGrid('addRowData', fixturesData[i].id , fixturesData[i]);
									 jQuery("#aggf-new-fixture-table").jqGrid('setSelection', fixturesData[i].id);
									 fixturesData[i].version = '0';
								}
							}
						}
					}							
				}
				
				for(var i=0; i<fixturesData.length ; i++){
					if(fixturesData[i].state == "COMMISSIONED" && parseFloat(fixturesData[i].version.substring(0,3)) >= 2.0 ){
						jQuery("#aggf-new-fixture-table").jqGrid('addRowData', fixturesData[i].id , fixturesData[i]);
					}							
				}
	
			}
			else {
				if(fixturesData.state == "COMMISSIONED" && parseFloat(fixturesData.version.substring(0,3)) >= 2.0 ){
					jQuery("#aggf-new-fixture-table").jqGrid('addRowData', fixturesData.id , fixturesData);
					if(groupAssignedFixtureList != null) {
						if(groupAssignedFixtureList.length != undefined) {
							for(var j=0; j<groupAssignedFixtureList.length ; j++){
								if(groupAssignedFixtureList[j].fixture.id == fixturesData.id) {
									 jQuery("#aggf-new-fixture-table").jqGrid('setSelection', fixturesData.id);
								}
							}
						}
						else if(groupAssignedFixtureList != undefined && groupAssignedFixtureList.fixture != undefined) {
							if(groupAssignedFixtureList.fixture.id == fixturesData.id) {
								 jQuery("#aggf-new-fixture-table").jqGrid('setSelection', fixturesData.id);
							}
						}
				}
			}
		}
	}
	else{
		
		if(SELECTED_FIXTURES_TO_ASSIGN_GROUPS != undefined && SELECTED_FIXTURES_TO_ASSIGN_GROUPS.length > 0) {
			//basePage = "floorplan";
			$.each(SELECTED_FIXTURES_TO_ASSIGN_GROUPS, function(){
				 if(parseFloat(this.version.substring(0,3)) >= 2.0) {
					 jQuery("#aggf-new-fixture-table").jqGrid('addRowData', this.id, this);
					 jQuery("#aggf-new-fixture-table").jqGrid('setSelection', this.id);
				 }
			 });
		}
		
	}
		
		$("#aggf-group-combo-create").empty();
		<c:forEach items="${groups2}" var="group">
			$('#aggf-group-combo-create').append($('<option></option>').val("${group.id}").html("${group.groupName}"));
		</c:forEach>
		
		
		<c:if test="${empty groups2}">
			$("#aggf-join-btn").hide();
		</c:if>
		
		<c:if test="${not empty groups2}">
			$("#aggf-join-btn").show();
		</c:if>
		
  }
			
}

function getAssignedGroupFixtures() {
	
	$.ajax({
		type: 'GET',
		url: "${getGroupFixtureListURL}"+ selectedGroupId +"?ts="+new Date().getTime(),
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

function resizeNewFixturesGrid(){
	//resize new fixture grid
	var gridContainerEL = document.getElementById("aggf-new-fixture-grid-container");
	forceFitJQgridHeight(jQuery("#aggf-new-fixture-table"), gridContainerEL.offsetHeight);
	jQuery("#aggf-new-fixture-table").jqGrid("setGridWidth", 345 );
}


function forceFitJQgridHeight(jgrid, containerHeight){
	var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
	var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
	var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
	jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - gridHeaderFooterHeight) * .99)); 
}

function saveGemsGroup(){
	setAssignGemsGroupMessage("", "black");
	
	if($.trim($("#aggf-group-name").val())==""){
		setAssignGemsGroupMessage("Please enter group name", "red");
		return false;
	}
	
	var selIds = jQuery("#aggf-new-fixture-table").getGridParam('selarrrow');
	if(selIds.length == 0){
		setAssignGemsGroupMessage("Please select a fixture", "red");
		return false;
	}
	
//Check for duplicate name
	setAssignGemsGroupMessage("Processing...", "black");
	$.ajax({
 		url: "${loadGroupByNameUrl}"+$.trim($("#aggf-group-name").val())+"?ts="+new Date().getTime(),
 		success: function(data){
			if(data == null){
				createNewGemsGroup();
			} else {
				setAssignGemsGroupMessage("A group with this name already exists", "red");
				return false;
			}
		},
		error: function(){
			setAssignGemsGroupMessage("Failed to load group", "red");
		},
 		dataType:"json",
 		contentType: "application/json; charset=utf-8",
 	});
}

function editGroup() {
	setAssignGemsGroupMessage("", "black");
	
	if($.trim($("#groupName").val())==""){
		setAssignGemsGroupMessage("Please enter group name", "red");
		return false;
	}
	
	
	//Check for duplicate name
	setAssignGemsGroupMessage("Processing...", "black");
	$.ajax({
 		url: "${loadGroupByNameUrl}"+$.trim($("#groupName").val())+"?ts="+new Date().getTime(),
 		success: function(data){
			if(data == null){
				editGemsGroup();
				$("#aggf-assign-btn").removeAttr("disabled");
				$("#editGroupBtn").removeAttr("disabled");
			} else {
				setAssignGemsGroupMessage("A group with this name already exists", "red");
				return false;
			}
		},
		error: function(){
			setAssignGemsGroupMessage("Failed to load group", "red");
		},
 		dataType:"json",
 		contentType: "application/json; charset=utf-8",
 	});
}

function editGemsGroup(){
	$("#aggf-assign-btn").attr("disabled", true);
	$("#editGroupBtn").attr("disabled", true);
	setAssignGemsGroupMessage("Processing...", "black");
	
	var dataXML = "<gemsGroup>"+
						"<id>" + selectedGroupId + "</id>"+
						"<name>"+ $.trim($("#groupName").val()) +"</name>"+
						"<description></description>"+
						"<type>"+
							"<id>"+ $("#aggf-group-type").val() +"</id>"+
						"</type>"+
					"</gemsGroup>";
					
 	$.ajax({
 		type: 'POST',
 		url: "${editGemsGroupUrl}"+"?ts="+new Date().getTime(),
 		data: dataXML,
 		success: function(data){
 			var xml=data.getElementsByTagName("response");
			if(xml[0].getElementsByTagName("msg")[0].childNodes[0].nodeValue == "S"){
				setAssignGemsGroupMessage("Saved Group Successfully", "green");
				if(basePage == "devices") {
					reloadGroupsFrame();
				}
			}
			else {
				setAssignGemsGroupMessage("Failed to edit group", "red");	
			}
		},
		error: function(){
			setAssignGemsGroupMessage("Failed to edit group", "red");
		},
 		dataType:"xml",
 		contentType: "application/xml; charset=utf-8",
 	});
}

function reloadGroupsFrame(){
	var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("groupsFrame");
	ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src;
}

function joinGemsGroup(){
	setAssignGemsGroupMessage("", "black");
	
	var groupId = $("#aggf-group-combo-create").val();
	if(groupId==null || groupId==""){
		setAssignGemsGroupMessage("Please select group name", "red");
		return false;
	}
	
	var selIds = jQuery("#aggf-new-fixture-table").getGridParam('selarrrow');
	if(selIds.length == 0){
		setAssignGemsGroupMessage("Please select atleast one fixture", "red");
		return false;
	}
	
	setAssignGemsGroupMessage("Processing...", "black");
	$.when(pollStatus(groupId), applyGroupToFixtures(groupId));
}

function assignToGroup(){
	setAssignGemsGroupMessage("", "black");
	
	var selRemoveIds = jQuery("#aggf-new-fixture-table").getGridParam('selarrrow');
	if(selRemoveIds.length == 0){
		setAssignGemsGroupMessage("Please select atleast one fixture", "red");
		return false;
	}
	
	var proceed = confirm("This will remove any unselected fixture from the Group and assign only current selected fixtures. Do you wish to continue?");
	if(proceed==true) {
		$("#aggf-assign-btn").attr("disabled", true);
		$("#editGroupBtn").attr("disabled", true);
		
		setAssignGemsGroupMessage("Processing...", "black");
		$.when(pollStatus(selectedGroupId), proceedWithAssignment());
	}
}

function proceedWithAssignment() {
	var selIds = jQuery("#aggf-new-fixture-table").getGridParam('selarrrow');
	var dataXML = "";
	for(var i=0; i<selIds.length; i++){
		var fixtureJson = jQuery("#aggf-new-fixture-table").jqGrid('getRowData', selIds[i]);
		dataXML += "<fixture><id>"+ fixtureJson.id +"</id></fixture>";
	}
	dataXML = "<fixtures>"+dataXML+"</fixtures>";
	
 	$.ajax({
 		type: 'POST',
 		url: "${assignFixturesToGroupURL}"+selectedGroupId+"?ts="+new Date().getTime(),
 		data: dataXML,
 		success: function(data){
 			clearInterval(pollServer);
 			var parts = data.split(',');
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
					out = out + "Assignment done.";	
				}
				else {
					failed = Aproc - Asuccess;
					out = out + "Assignment done." + " (" + failed + " of " + Atotal + " requests" + " failed)";
				}
					
			}
			setAssignGemsGroupMessage(out, "green");
		},
		error: function(){
			clearInterval(pollServer);
			setAssignGemsGroupMessage("Failed to change assignment of fixtures", "red");
		},
		complete: function() {
			$("#aggf-assign-btn").removeAttr("disabled");
			$("#editGroupBtn").removeAttr("disabled");
		},
 		dataType:"html",
 		contentType: "application/xml; charset=utf-8",
 	});
}

function createNewGemsGroup(){
	$("#aggf-save-btn").attr("disabled", true);
	$("#aggf-join-btn").attr("disabled", true);
	setAssignGemsGroupMessage("Processing...", "black");
	
	var dataXML = "<gemsGroup>"+
						"<id></id>"+
						"<name>"+ $.trim($("#aggf-group-name").val()) +"</name>"+
						"<description></description>"+
						"<type>"+
							"<id>"+ $("#aggf-group-type").val() +"</id>"+
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
					$.when(pollStatus(gemsGroupId), applyGroupToFixtures(gemsGroupId));
				}
			}
		},
		error: function(){
			setAssignGemsGroupMessage("Failed to create group", "red");
		},
		complete: function() {
			$("#aggf-save-btn").removeAttr("disabled");
			$("#aggf-join-btn").removeAttr("disabled");
		},
 		dataType:"xml",
 		contentType: "application/xml; charset=utf-8",
 	});
}

function applyGroupToFixtures(gemsGroupId){
	var selIds = jQuery("#aggf-new-fixture-table").getGridParam('selarrrow');
	var dataXML = "";
	if(selIds.length > 0){
		for(var i=0; i<selIds.length; i++){
			var fixtureJson = jQuery("#aggf-new-fixture-table").jqGrid('getRowData', selIds[i]);
			dataXML += "<fixture><id>"+ fixtureJson.id +"</id></fixture>";
		}
		dataXML = "<fixtures>"+dataXML+"</fixtures>";
	}
	
 	$.ajax({
 		type: 'POST',
 		url: "${applyGroupToFixtureUrl}"+gemsGroupId+"?ts="+new Date().getTime(),
 		data: dataXML,
 		success: function(data){
	 			clearInterval(pollServer);
				var parts = data.split(',');
				total = Number(parts[0]) + Number(parts[3]);
				proc = Number(parts[1]) + Number(parts[4]);
				success = Number(parts[2]) + Number(parts[5]);
				if(total > 0) {
					if(proc == success) {
						setAssignGemsGroupMessage("Assignment successful", "green");
					}
					else {
						failed = proc - success;
						setAssignGemsGroupMessage("Assignment done" + " (" + failed + " of " + total + " requests" + " failed)", "green");
					}
						
				}
		},
		error: function(){
			clearInterval(pollServer);
			setAssignGemsGroupMessage("Group created successfully but failed to assign fixtures", "red");
		},
 		dataType:"html",
 		contentType: "application/xml; charset=utf-8",
 	});
}

function setAssignGemsGroupMessage(msg, color){
	$("#aggf-message-div").css("color", color);
	$("#aggf-message-div").html(msg);
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
									setAssignGemsGroupMessage(out, "black");
								}
							}
						}
					});
			}, 5000);
}

</script>
</head>
<body id="aggf-main-box">
<div id="aggf-tabs-body">
	
	<div id="tab-create-group">
		<table width=100% height=100% style="padding:0 10px;">	
			<tr height=24px>
				<td colspan="2" valign="top" height=24px>
					<div id="aggf-message-div" class="aggf-message-text"></div>
				</td>
			</tr>
			<c:if test="${mode != 'edit'}">
			<tr height=30px>
				<td colspan="2">
					<div class="field-label">New Group Name:</div> 
					<div class="field-input"><input id="aggf-group-name" type="text"/></div>
				</td>
			</tr>
			<tr height=30px>
				<td colspan="2">
					<div class="field-label">Select Existing Group:</div> 
					<div class="field-input"><select id="aggf-group-combo-create"></select></div>
				</td>
			</tr>
			</c:if>
			<c:if test="${mode == 'edit'}">
			<tr height=30px>
				<td colspan="2">
						<div class="field-label">Edit Group:</div> 
						<div class="field-input">
							<input name="groupName" id="groupName" type="text"/>
							<button id="editGroupBtn" style="height: 24px" onclick="editGroup();">Save</button>
						</div>
				</td>
			</tr>
			</c:if>
			<tr height=30px>
				<td colspan="2">
					<div class="field-label">Select Group Type:</div> 
					<div class="field-input"><select id="aggf-group-type"></select></div>
				</td>
			</tr>
			
			<tr height=30px>
				<td colspan="2">
					<c:if test="${mode != 'edit'}">
						<div class="field-label">Sensor's Type</div> 
						<div class="field-input" id="sensorVersionCheckGroup">
							<input type="radio" name="sensorVersion"  value="2.0+" checked="checked" onclick="loadFixtureGrid('2.0+')">2.0+
							&nbsp
							<input type="radio" name="sensorVersion"  value="1.5" onclick="loadFixtureGrid('1.5+')">1.5+
						</div>
						
					</c:if>
					<c:if test="${mode == 'edit'}">
						<div class="field-label">Sensors Type</div> 
						<div class="field-input">
							<label id="groupSensorType"></label>
						</div>
					</c:if>
				</td>
			</tr>
			<tr height=220px>
				<td id="aggf-new-fixture-grid-container" colspan="2" valign="top">
					<!-- <span style="font-weight: bold; font-size: 0.85em">*Only 2.0+ sensors can participate in groups.</span> -->
					<table id="aggf-new-fixture-table"></table>
				</td>
			</tr>
			<c:if test="${mode != 'edit'}">
			<tr>
				<td valign="top">
					<button id="aggf-save-btn" onclick="saveGemsGroup();">Create & Join Group</button>
				</td>
				<td valign="top">
					<button id="aggf-join-btn" onclick="joinGemsGroup();">Join Existing Group</button>
				</td>
			</tr>
			</c:if>
			<c:if test="${mode == 'edit'}">
			<tr>
				<td valign="top" colspan="2" >
					<button id="aggf-assign-btn" onclick="assignToGroup();">Assign selected fixtures to group</button>
				</td>
			</tr>
			</c:if>
		</table>
	</div>
	
</div>
	
</body>
</html>