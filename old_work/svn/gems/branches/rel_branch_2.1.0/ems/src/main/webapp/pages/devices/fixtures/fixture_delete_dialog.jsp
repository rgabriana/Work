<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<spring:url value="/services/org/fixture/decommission/" var="decommissionFixtureUrl" scope="request" />
<spring:url value="/services/org/fixture/decommissionwithoutack/" var="decommissionWoAckFixtureUrl" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Assign Users</title>

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

<style>
	 td.fdd-button-row{background-color: #EEEEEE;}
</style>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
</style>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Fixture Deletion</title>

<script type="text/javascript">
var fixtureFrame = document.getElementById("installFrame").contentWindow.document.getElementById("fixturesFrame").contentWindow.document;
var fixGrid = fixtureFrame.getElementById("fixtureTable");

var deleteFixtureData = [];
var forcefullyDeleteFixtureData = [];
var DELETE_COUNT = 0;
var FORCE_DELETED_COUNT = 0;

$(document).ready(function() {
	createDeletedFixtureGrid();
	getSelectedFixturesToDelete();
	disableButtons(true);
	deleteFixturesOneByOne();
});

function getSelectedFixturesToDelete(){
	var selIds = $(fixGrid).getGridParam('selarrrow');
	var fixNum = selIds.length;
	for(var i=0; i<fixNum; i++){
		var fixtureRow = $(fixGrid).jqGrid('getRowData', selIds[i]);
		
		var fxJson = {};
		fxJson.id = fixtureRow.id;
		//fxJson.name = fixtureRow.fixtureName; //depericated with pagination support
		fxJson.name = fixtureRow.name;
		fxJson.deletestatus = "Waiting...";
		
		deleteFixtureData.push(fxJson);	
		jQuery("#fixture-deleting-table").jqGrid('addRowData', fxJson.id, fxJson);
	}
}

function createDeletedFixtureGrid(){
	jQuery("#fixture-deleting-table").jqGrid({
		datatype: "local",
		autowidth: true,
		scrollOffset: 0,
		forceFit: true,
		height: "200px",
	   	colNames:['id', 'Fixture Name', 'Delete Status'],
	   	colModel:[
  	   		{name:'id', index:'id', hidden: true},
	   		{name:'name', index:'name', sortable:false, width:"218px"},
	   		{name:'deletestatus', index:'deletestatus', sortable:false, width:"218px"}
	   	],
	    viewrecords: true
	});
	
}

function deleteFixturesOneByOne(){
	if(deleteFixtureData.length == 0){
		var fixNum = jQuery('#fixture-deleting-table').jqGrid('getGridParam', 'records');
		if(fixNum > 0){
			setDeleteFixtureMessage("Some fixture(s) were not reachable. Click on RETRY if you want to forcefully delete these fixture(s).");
		} else {
			setDeleteFixtureMessage("All fixture(s) are deleted successfully.");
		}
		disableButtons(false);
		return false;
	}
		
	var fixture = deleteFixtureData.shift();
	jQuery("#fixture-deleting-table").jqGrid('setCell', fixture.id, "deletestatus", "Processing...");
	var fxXML= "<fixtures><fixture><id>"+fixture.id+"</id></fixture></fixtures>";
		
	$.ajax({
		type: 'POST',
		url: "${decommissionFixtureUrl}?v="+DELETE_COUNT,
		data: fxXML,
		success: function(data){
			if(data != null){
				var xml=data.getElementsByTagName("response");
				for (var j=0; j<xml.length; j++) {
					var status = xml[j].getElementsByTagName("status")[0].childNodes[0].nodeValue;
					var currFixtureId = xml[j].getElementsByTagName("msg")[0].childNodes[0].nodeValue;
					
					if(status==1){ // success: remove from list
						jQuery("#fixture-deleting-table").jqGrid('delRowData', currFixtureId);
					} else { // failed: fixture not reachable
						jQuery("#fixture-deleting-table").jqGrid('setCell', currFixtureId, "deletestatus", "Fixture not reachable");
					}
				}
			}
		},
		complete: function(){
			DELETE_COUNT++;
			deleteFixturesOneByOne();
		},
		dataType:"xml",
		contentType: "application/xml; charset=utf-8",
	});
}

function forcefullyDeleteFixturesOneByOne(){
	if(forcefullyDeleteFixtureData == 0){
		var fixNum = jQuery('#fixture-deleting-table').jqGrid('getGridParam', 'records');
		if(fixNum > 0){
			setDeleteFixtureMessage("Some fixture(s) were not reachable. Click on RETRY if you want to forcefully delete these fixture(s).");
		} else {
			setDeleteFixtureMessage("All fixture(s) are deleted successfully.");
		}
		disableButtons(false);
		return false;
	}
	
	var fixture = forcefullyDeleteFixtureData.shift();
	jQuery("#fixture-deleting-table").jqGrid('setCell', fixture.id, "deletestatus", "Processing...");
	var fxXML= "<fixtures><fixture><id>"+fixture.id+"</id></fixture></fixtures>";
	
	$.ajax({
		type: 'POST',
		url: "${decommissionWoAckFixtureUrl}?v="+FORCE_DELETED_COUNT,
		data: fxXML,
		success: function(data){
			if(data != null){
				var xml=data.getElementsByTagName("response");
				for (var j=0; j<xml.length; j++) {
					var status = xml[j].getElementsByTagName("status")[0].childNodes[0].nodeValue;
					var currFixtureId = xml[j].getElementsByTagName("msg")[0].childNodes[0].nodeValue;
					
					if(status==1){// success: remove from list
						jQuery("#fixture-deleting-table").jqGrid('delRowData', currFixtureId);
					}
				}
			}
		},
		complete: function(){
			FORCE_DELETED_COUNT++;
			forcefullyDeleteFixturesOneByOne();
		},
		dataType:"xml",
		contentType: "application/xml; charset=utf-8",
	});
}

function disableButtons(isDisable){
	if(isDisable){
		$("#fdd-done-btn").attr("disabled", true);
		$("#fdd-retry-btn").attr("disabled", true);
	}else{
		$("#fdd-done-btn").removeAttr("disabled");
		$("#fdd-retry-btn").removeAttr("disabled");
	}
}

function deleteDoneHandler(){
	$("#deleteFixtureDailog").dialog("close");
}

function deleteRetryHandler(){
	disableButtons(true);
	
	var undeletedFixtures = jQuery('#fixture-deleting-table').jqGrid('getRowData');
	for(var i=0; i<undeletedFixtures.length; i++){
		forcefullyDeleteFixtureData.push(undeletedFixtures[i]);
		jQuery("#fixture-deleting-table").jqGrid('setCell', undeletedFixtures[i].id, "deletestatus", "Waiting...");
	}
	
	forcefullyDeleteFixturesOneByOne();
}

function setDeleteFixtureMessage(message){
	$("#delete-fixture-message").html(message);
}
</script>
</head>
<body id="fdd-main-body">
<table style="margin:5px;width:450px;height:300px;">
	<tr>
		<td>
			<div id="delete-fixture-message"></div>
		</td>
	</tr>
	<tr>
		<td valign="top">
			<table id="fixture-deleting-table" style="width:450px;height:100%;"></table>
		</td>
	</tr>
	<tr>
		<td align="center" class="fdd-button-row">
			<button id="fdd-done-btn" onclick="javascript: deleteDoneHandler();">Done</button>
			<button id="fdd-retry-btn" onclick="javascript: deleteRetryHandler();">Retry</button>
		</td>
	</tr>
</table>

</body>
</html>