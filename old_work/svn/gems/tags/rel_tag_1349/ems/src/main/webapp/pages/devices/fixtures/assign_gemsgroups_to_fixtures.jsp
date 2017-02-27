<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<spring:url value="/services/org/gemsgroups/list" var="loadGemsGroupsUrl" scope="request" />
<spring:url value="/services/org/gemsgroups/op/creategroup" var="saveGemsGroupUrl" scope="request" />
<spring:url value="/services/org/gemsgroupfixture/op/applygroup/" var="applyGroupToFixtureUrl" scope="request" />
<spring:url value="/services/org/gemsgroups/loadbyname/" var="loadGroupByNameUrl" scope="request" />
<spring:url value="/services/org/gemsgroupfixture/list/" var="loadFixtureByGroupUrl" scope="request" />
<spring:url value="/services/org/gemsgroupfixture/op/managegroup/" var="removeFixtureFromGroupUrl" scope="request" />
<spring:url value="/services/org/gemsgroupfixture/op/resetallgroups" var="resetGroupsForSelectedFixture" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Assign Users</title>

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

<style>
	#aggf-new-fixture-grid-container tr.jqgroup {background-color: #F1F1F1 !important;}
	#aggf-new-fixture-grid-container {border-bottom: 1px solid #AAAAAA;}
	#aggf-assigned-fixture-grid-container {border-bottom: 1px solid #AAAAAA;}
	#aggf-top-panel-container {border-bottom-style:solid; border-bottom-width:1px; border-bottom-color: #555555;}
	#aggf-new-fixture-grid-container th {text-align: left !important; padding-left: 5px !important;}
	#aggf-assigned-fixture-grid-container th {text-align: left !important; padding-left: 5px !important;}
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
$("#aggf-group-type").empty();
// ID of the group will be later on made dynamic
$("#aggf-group-type").append(new Option("${grouptypes}", "2"));

$("#aggf-group-combo").empty();
<c:forEach items="${groups}" var="group">
	$("#aggf-group-combo").append(new Option("${group.groupName}", "${group.id}"));
</c:forEach>

$("#aggf-group-combo-create").empty();
<c:forEach items="${groups}" var="group">
	$("#aggf-group-combo-create").append(new Option("${group.groupName}", "${group.id}"));
</c:forEach>

//Dialog tab setup
$("#aggf-tabs-body").tabs({
	selected: 0, 
	show: function(event, ui) {
		resizeNewFixturesGrid();
		resizeAssignFixturesGrid();
	}
});

$(document).ready(function() {
	createNewFixturesGrid();
	createAssignedFixturesGrid();
	
	loadAssignedFixtures();
});

function createNewFixturesGrid(){
	jQuery("#aggf-new-fixture-table").jqGrid({
		datatype: "local",
		autowidth: true,
		scrollOffset: 0,
		forceFit: true,
	   	colNames:['id', 'Selected Fixtures'],
	   	colModel:[
  	   		{name:'id', index:'id', hidden: true},
	   		{name:'name', index:'name', sortable:false}
	   	],
	    viewrecords: true,
	    multiselect: true
	});
	
	 $.each(SELECTED_FIXTURES_TO_ASSIGN_GROUPS, function(){
		jQuery("#aggf-new-fixture-table").jqGrid('addRowData', this.id, this);
		jQuery("#aggf-new-fixture-table").jqGrid('setSelection', this.id);
	 });
}

function createAssignedFixturesGrid(){
	jQuery("#aggf-assigned-fixture-table").jqGrid({
		datatype: "local",
		autowidth: true,
		scrollOffset: 0,
		forceFit: true,
	   	colNames:['id', 'Fixtures'],
	   	colModel:[
  	   		{name:'id', index:'id', hidden: true},
	   		{name:'name', index:'name', sortable:false}
	   	],
	    viewrecords: true,
	    multiselect: true
	});
}

function loadAssignedFixtures(){
	setManageGemsGroupMessage("", "black");
	
	//Remove all fixtures
	$("#aggf-assigned-fixture-table").jqGrid("resetSelection");
	$("#aggf-assigned-fixture-table").jqGrid("clearGridData");
	
	//load selected fixtures from floor plan
	$.each(SELECTED_FIXTURES_TO_ASSIGN_GROUPS, function(){
		jQuery("#aggf-assigned-fixture-table").jqGrid('addRowData', ('af-'+this.id), this);
		jQuery("#aggf-assigned-fixture-table").jqGrid('setSelection', ('af-'+this.id));
	});
	 
	var groupId = $("#aggf-group-combo").val();
	if(groupId==null || groupId==""){
		return false;
	}
	//load assigned fixtures
	$.ajax({
 		url: "${loadFixtureByGroupUrl}"+groupId+"?ts="+new Date().getTime(),
 		success: function(data){
			if(data != null){
				var gemsGroupFixtureList;
				var responseData = data.gemsGroupFixture;
				if(responseData != undefined){
					if(responseData.length == undefined){ //FIXME : JSON response issue : For only 1 record it returns Object instead of an Array
						var dataWrapper = {};
						dataWrapper.gemsGroupFixture = [];
						dataWrapper.gemsGroupFixture.push(responseData);
						gemsGroupFixtureList = dataWrapper;
					} else if(responseData.length > 0){
						gemsGroupFixtureList = data;
					}
					
					var ASSIGNED_FIXTURES_LIST = [];
					$.each(gemsGroupFixtureList.gemsGroupFixture, function(){
						var gridRow = jQuery("#aggf-assigned-fixture-table").jqGrid('getRowData', ('af-'+this.fixture.id));
						//Don't add if fixture is selected from floor plan
						if(this.fixture.id != gridRow.id){
							jQuery("#aggf-assigned-fixture-table").jqGrid('addRowData', ('af-'+this.fixture.id), {id:this.fixture.id, name:this.fixture.name});
							jQuery("#aggf-assigned-fixture-table").jqGrid('setSelection', ('af-'+this.fixture.id));
						}
					});
				}
			}
		},
		error: function(){
			setAssignGemsGroupMessage("Failed to load assigned fixtures by group.", "red");
		},
 		dataType:"json",
 		contentType: "application/json; charset=utf-8",
 	});
}

function loadGroupFixtures(){
	setManageGemsGroupMessage("", "black");
	
	//Remove all fixtures
	$("#aggf-assigned-fixture-table").jqGrid("resetSelection");
	$("#aggf-assigned-fixture-table").jqGrid("clearGridData");
	
	var groupId = $("#aggf-group-combo").val();
	if(groupId==null || groupId==""){
		return false;
	}
	//load assigned fixtures
	$.ajax({
 		url: "${loadFixtureByGroupUrl}"+groupId+"?ts="+new Date().getTime(),
 		success: function(data){
			if(data != null){
				var gemsGroupFixtureList;
				var responseData = data.gemsGroupFixture;
				if(responseData != undefined){
					if(responseData.length == undefined){ //FIXME : JSON response issue : For only 1 record it returns Object instead of an Array
						var dataWrapper = {};
						dataWrapper.gemsGroupFixture = [];
						dataWrapper.gemsGroupFixture.push(responseData);
						gemsGroupFixtureList = dataWrapper;
					} else if(responseData.length > 0){
						gemsGroupFixtureList = data;
					}
					
					var ASSIGNED_FIXTURES_LIST = [];
					$.each(gemsGroupFixtureList.gemsGroupFixture, function(){
						var gridRow = jQuery("#aggf-assigned-fixture-table").jqGrid('getRowData', ('af-'+this.fixture.id));
						jQuery("#aggf-assigned-fixture-table").jqGrid('addRowData', ('af-'+this.fixture.id), {id:this.fixture.id, name:this.fixture.name});
						jQuery("#aggf-assigned-fixture-table").jqGrid('setSelection', ('af-'+this.fixture.id));
					});
				}
			}
		},
		error: function(){
			setAssignGemsGroupMessage("Failed to load assigned fixtures by group.", "red");
		},
 		dataType:"json",
 		contentType: "application/json; charset=utf-8",
 	});
}
function loadCreateTab() {
	setManageGemsGroupMessage("", "black");
}

function loadGroups() {
	setManageGemsGroupMessage("", "black");
	$("#aggf-group-combo").empty();
	$.ajax({
 		url: "${loadGemsGroupsUrl}?ts="+new Date().getTime(),
 		success: function(data){
			if(data != null){
				var gemsGroupList = [];
				var responseData = data.gemsGroup;
				if(responseData != undefined){
					if(responseData.length == undefined){ //FIXME : JSON response issue : For only 1 record it returns Object instead of an Array
						$("#aggf-group-combo").append(new Option(data.gemsGroup.name, data.gemsGroup.id));
					} else if(responseData.length > 0){
						gemsGroupList = data;
						$.each(gemsGroupList.gemsGroup, function() {
							$("#aggf-group-combo").append(new Option(this.name, this.id));
						});
					}
				}
				loadGroupFixtures();
			}
		},
		error: function(){
			setAssignGemsGroupMessage("Failed to load group list.", "red");
		},
 		dataType:"json",
 		contentType: "application/json; charset=utf-8",
 	});

}

function manageGemsGroup(){
	setManageGemsGroupMessage("", "black");
	
	//add new fixtures to group
	var selectedFixturesRows = jQuery("#aggf-assigned-fixture-table").getGridParam('selarrrow');

	var addFixtures = [];
	for(var i=0; i<selectedFixturesRows.length; i++){
		var gridRow = jQuery("#aggf-assigned-fixture-table").jqGrid('getRowData', selectedFixturesRows[i]);
		addFixtures.push(gridRow.id);
	}
	if(addFixtures.length > 0){
		removeFixturesFromGroup(addFixtures);
	}
}

function resetGemsGroupsFromFixture(){
	setManageGemsGroupMessage("", "black");
	
	var selectedFixturesRows = jQuery("#aggf-assigned-fixture-table").getGridParam('selarrrow');
	var addFixtures = [];
	for(var i=0; i<selectedFixturesRows.length; i++){
		var gridRow = jQuery("#aggf-assigned-fixture-table").jqGrid('getRowData', selectedFixturesRows[i]);
		addFixtures.push(gridRow.id);
	}
	if(addFixtures.length > 0){
		resetAllGroupFromSelectedFixtures(addFixtures);
	}
}


function assignNewFixturesToGroup(addFixturesArr){
	setManageGemsGroupMessage("Processing...", "black");
	$("#aggf-manage-btn").attr("disabled", true);
	$("#aggf-reset-btn").attr("disabled", true);
	
	var dataXML = "";
	for(var i=0; i<addFixturesArr.length; i++){
		dataXML += "<fixture><id>"+ addFixturesArr[i] +"</id></fixture>";
	}
	dataXML = "<fixtures>"+dataXML+"</fixtures>";
	
 	$.ajax({
 		type: 'POST',
 		url: "${applyGroupToFixtureUrl}"+ $("#aggf-group-combo").val() +"?ts="+new Date().getTime(),
 		data: dataXML,
 		success: function(data){
			if(data != null){
				var xml=data.getElementsByTagName("response");
				for (var j=0; j<xml.length; j++) {
					setManageGemsGroupMessage("Assignment successful.", "green");
					$("#aggf-manage-btn").removeAttr("disabled");
					$("#aggf-reset-btn").removeAttr("disabled");
				}
			}
		},
		error: function(){
			setManageGemsGroupMessage("Failed to assign fixtures.", "red");
		},
 		dataType:"xml",
 		contentType: "application/xml; charset=utf-8",
 	});
}

function removeFixturesFromGroup(removeFixturesArr){
	setManageGemsGroupMessage("Processing...", "black");
	$("#aggf-manage-btn").attr("disabled", true);
	$("#aggf-reset-btn").attr("disabled", true);

	var dataXML = "";
	for(var i=0; i<removeFixturesArr.length; i++){
		dataXML += "<fixture><id>"+ removeFixturesArr[i] +"</id></fixture>";
	}
	dataXML = "<fixtures>"+dataXML+"</fixtures>";
	
 	$.ajax({
 		type: 'POST',
 		url: "${removeFixtureFromGroupUrl}"+ $("#aggf-group-combo").val() +"?ts="+new Date().getTime(),
 		data: dataXML,
 		success: function(data){
			if(data != null){
				var xml=data.getElementsByTagName("response");
				for (var j=0; j<xml.length; j++) {
					setManageGemsGroupMessage("Assignment successful.", "green");
					$("#aggf-manage-btn").removeAttr("disabled");
					$("#aggf-reset-btn").removeAttr("disabled");
				}
			}
			loadGroupFixtures();
		},
		error: function(){
			setManageGemsGroupMessage("Failed to remove fixtures.", "red");
		},
 		dataType:"xml",
 		contentType: "application/xml; charset=utf-8",
 	});
}

function resetAllGroupFromSelectedFixtures(removeFixturesArr){
	setManageGemsGroupMessage("Processing...", "black");
	$("#aggf-manage-btn").attr("disabled", true);
	$("#aggf-reset-btn").attr("disabled", true);
	
	var dataXML = "";
	for(var i=0; i<removeFixturesArr.length; i++){
		dataXML += "<fixture><id>"+ removeFixturesArr[i] +"</id></fixture>";
	}
	dataXML = "<fixtures>"+dataXML+"</fixtures>";
	
 	$.ajax({
 		type: 'POST',
 		url: "${resetGroupsForSelectedFixture}" + "?ts="+new Date().getTime(),
 		data: dataXML,
 		success: function(data){
			if(data != null){
				var xml=data.getElementsByTagName("response");
				for (var j=0; j<xml.length; j++) {
					setManageGemsGroupMessage("Reset Group successful.", "green");
					$("#aggf-manage-btn").removeAttr("disabled");
					$("#aggf-reset-btn").removeAttr("disabled");
				}
			}
			loadGroupFixtures();
		},
		error: function(){
			setManageGemsGroupMessage("Failed to reset Group.", "red");
		},
 		dataType:"xml",
 		contentType: "application/xml; charset=utf-8",
 	});
}


function saveGemsGroup(){
	setAssignGemsGroupMessage("", "black");
	
	if($("#aggf-group-name").val().trim()==""){
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
 		url: "${loadGroupByNameUrl}"+$("#aggf-group-name").val().trim()+"?ts="+new Date().getTime(),
 		success: function(data){
			if(data == null){
				createNewGemsGroup();
			} else {
				setAssignGemsGroupMessage("A group with this name already exists.", "red");
				return false;
			}
		},
		error: function(){
			setAssignGemsGroupMessage("Failed to load group.", "red");
		},
 		dataType:"json",
 		contentType: "application/json; charset=utf-8",
 	});
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
		setAssignGemsGroupMessage("Please select a fixture", "red");
		return false;
	}
	
	setAssignGemsGroupMessage("Processing...", "black");
	applyGroupToFixtures(groupId);
}

function createNewGemsGroup(){
	$("#aggf-save-btn").attr("disabled", true);
	setAssignGemsGroupMessage("Processing...", "black");
	
	var dataXML = "<gemsGroup>"+
						"<id></id>"+
						"<name>"+ $("#aggf-group-name").val().trim() +"</name>"+
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
					applyGroupToFixtures(gemsGroupId);
				}
			}
		},
		error: function(){
			setAssignGemsGroupMessage("Failed to create group.", "red");
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
			if(data != null){
				var xml=data.getElementsByTagName("response");
				for (var j=0; j<xml.length; j++) {
					setAssignGemsGroupMessage("Assignment successful.", "green");
					$("#aggf-save-btn").removeAttr("disabled");
				}
			}
		},
		error: function(){
			setAssignGemsGroupMessage("Failed to assign fixtures.", "red");
		},
 		dataType:"xml",
 		contentType: "application/xml; charset=utf-8",
 	});
}

function resizeNewFixturesGrid(){
	//resize new fixture grid
	var gridContainerEL = document.getElementById("aggf-new-fixture-grid-container");
	forceFitJQgridHeight(jQuery("#aggf-new-fixture-table"), gridContainerEL.offsetHeight);
	jQuery("#aggf-new-fixture-table").setGridWidth(gridContainerEL.offsetWidth);
}

function resizeAssignFixturesGrid(){
	//resize assigned fixture grid
	var gridContainerEL = document.getElementById("aggf-assigned-fixture-grid-container");
	forceFitJQgridHeight(jQuery("#aggf-assigned-fixture-table"), gridContainerEL.offsetHeight);
	jQuery("#aggf-assigned-fixture-table").setGridWidth(gridContainerEL.offsetWidth);
}

function forceFitJQgridHeight(jgrid, containerHeight){
	var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
	var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
	var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
	jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - gridHeaderFooterHeight) * .99)); 
}

function setAssignGemsGroupMessage(msg, color){
	$("#aggf-message-div").css("color", color);
	$("#aggf-message-div").html(msg);
}

function setManageGemsGroupMessage(msg, color){
	$("#aggf-manage-message-div").css("color", color);
	$("#aggf-manage-message-div").html(msg);
}
</script>
</head>
<body id="aggf-main-box">
<div id="aggf-tabs-body">
	<ul>
		<li><a href="#tab-create-group" onclick="loadCreateTab();">Create / Join Group</a></li>
		<li><a href="#tab-manage-group" onclick="loadGroups();">Manage Groups</a></li>
	</ul>
	
	<div id="tab-create-group">
		<table width=100% height=100% style="padding:0 10px;">	
			<tr height=24px>
				<td valign="top" height=24px>
					<div id="aggf-message-div" class="aggf-message-text"></div>
				</td>
			</tr>
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
			<tr height=30px>
				<td colspan="2">
					<div class="field-label">Select Group Type:</div> 
					<div class="field-input"><select id="aggf-group-type"></select></div>
				</td>
			</tr>
			<tr height=220px>
				<td id="aggf-new-fixture-grid-container" colspan="2" valign="top">
					<table id="aggf-new-fixture-table"></table>
				</td>
			</tr>
			<tr>
				<td valign="top">
					<button id="aggf-save-btn" onclick="saveGemsGroup();">Create & Join Group</button>
				</td>
				<td valign="top">
					<button id="aggf-save-btn" onclick="joinGemsGroup();">Join Existing Group</button>
				</td>
			</tr>
		</table>
	</div>
	
	<div id="tab-manage-group">
		<table width=100% height=100% style="padding:0 10px;">	
			<tr height=24px>
				<td valign="top" height=24px>
					<div id="aggf-manage-message-div"  class="aggf-message-text"></div>
				</td>
			</tr>
			<tr height=30px>
				<td>
					<div class="field-label">Select Group:</div> 
					<div class="field-input"><select id="aggf-group-combo" onchange="javascript: loadGroupFixtures();"></select></div>
				</td>
			</tr>
			<tr height=250px>
				<td id="aggf-assigned-fixture-grid-container" colspan="2" valign="top">
					<table id="aggf-assigned-fixture-table"></table>
				</td>
			</tr>
			<tr>
				<td valign="top">
					<button id="aggf-manage-btn" onclick="manageGemsGroup();">Leave Group</button>
				</td>
				<td valign="top">
					<button id="aggf-reset-btn" onclick="resetGemsGroupsFromFixture();">Reset Groups</button>
				</td>
			</tr>
		</table>
	</div>
</div>
	
</body>
</html>