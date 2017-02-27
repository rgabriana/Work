<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/area" var="assignSwitchesUrl" scope="request" />

<spring:url value="/services/org/area" var="assignFixturesUrl" scope="request" />

<spring:url value="/services/org/area" var="assignWdsUrl" scope="request" />

<spring:url value="/services/org/area" var="assignPlugloadUrl" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Assign Area to Selected Devices</title>

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

<style>
	table#aasd-wrapper-table td {padding: 0 20px}
	td#aasd-form-container div.fieldLabel{float:left; width:35%; font-weight:bold;}
 	td#aasd-form-container div.fieldValue{float:left; width:65%;}
	#aasd-message-div {font-weight:bold; float: left;}
</style>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
</style>

<script type="text/javascript">


$(document).ready(function() {
	$("#aasd-area-combo").empty();
	<c:forEach items="${areas}" var="area">
		$('#aasd-area-combo').append($('<option></option>').val("${area.id}").html("${area.name}"));
	</c:forEach>

});

var selectedDeviceAreaId = "";

function assignSelectedAreaToDevices(){
	if(SELECTED_SWITCHES_TO_ASSIGN_AREA.length == 0 && SELECTED_FIXTURES_TO_ASSIGN_AREA.length == 0 && SELECTED_WDS_TO_ASSIGN_AREA == 0 && SELECTED_PLUGLOAD_TO_ASSIGN_AREA == 0){
		setSelectedDeviceMessage("Please select a switch,fixture,plugload or wds", "red");
		return false;
	}
	
	selectedDeviceAreaId = $("#aasd-area-combo").val();
	if(selectedDeviceAreaId == null || selectedDeviceAreaId == ""){
		setSelectedDeviceMessage("Please select an area.", "red");
		return false;
	}

	$("#aasd-apply-btn").attr("disabled", "disabled");
	
	setSelectedDeviceMessage("Processing...", "black");
	
	if(SELECTED_SWITCHES_TO_ASSIGN_AREA != ""){
		assignSelectedAreaToSwitches();
	}else if(SELECTED_FIXTURES_TO_ASSIGN_AREA != ""){
		assignSelectedAreaToFixtures();
	}else if(SELECTED_WDS_TO_ASSIGN_AREA != ""){
		assignSelectedAreaToWds();
	}else if(SELECTED_PLUGLOAD_TO_ASSIGN_AREA!=""){
		assignSelectedAreaToPlugload();
	}
}

function assignSelectedAreaToSwitches(){
	var switchXML = "";
	for(var i=0; i<SELECTED_SWITCHES_TO_ASSIGN_AREA.length; i++){
		var switchJson = SELECTED_SWITCHES_TO_ASSIGN_AREA[i];
		switchXML += getSwitchXML(switchJson.id);
	}
	
	var postSwitchData = getSwitchXML_LIST(switchXML);
	
	$.ajax({
		type: 'POST',
		url: "${assignSwitchesUrl}/"+selectedDeviceAreaId+"/assignswitches",
		data: postSwitchData,
		success: function(data){
			if(SELECTED_FIXTURES_TO_ASSIGN_AREA != ""){
				assignSelectedAreaToFixtures();
			}else if(SELECTED_WDS_TO_ASSIGN_AREA != ""){
				assignSelectedAreaToWds();
			}else if(SELECTED_PLUGLOAD_TO_ASSIGN_AREA != ""){
				assignSelectedAreaToPlugload();
			}	
			else{
				setSelectedDeviceMessage("Assignment Successful", "green");
			}
		},
		dataType:"xml",
		contentType: "application/xml; charset=utf-8"
	});
}

function getSwitchXML(switchId){
	return "<switch><id>"+switchId+"</id></switch>";
}

function getSwitchXML_LIST(switchXML){
	return "<switches>"+switchXML+"</switches>";
}

function assignSelectedAreaToFixtures(){
	var fixtureXML = "";
	for(var i=0; i<SELECTED_FIXTURES_TO_ASSIGN_AREA.length; i++){
		var fixtureJson = SELECTED_FIXTURES_TO_ASSIGN_AREA[i];
		fixtureXML += getFixtureXML(fixtureJson.id);
	}
	var postFixtureData = getFixtureXML_LIST(fixtureXML);
	
	$.ajax({
		type: 'POST',
		url: "${assignFixturesUrl}/"+selectedDeviceAreaId+"/assignfixtures",
		data: postFixtureData,
		success: function(data){
			if(SELECTED_WDS_TO_ASSIGN_AREA != ""){
				assignSelectedAreaToWds();
			}else if(SELECTED_PLUGLOAD_TO_ASSIGN_AREA != ""){
				assignSelectedAreaToPlugload();
			}else{
				setSelectedDeviceMessage("Assignment Successful", "green");
			}
		},
		dataType:"xml",
		contentType: "application/xml; charset=utf-8"
	});
}

function getFixtureXML(fixtureId){
	return "<fixture><id>"+fixtureId+"</id></fixture>";
}

function getFixtureXML_LIST(fixtureXML){
	return "<fixtures>"+fixtureXML+"</fixtures>";
}

function assignSelectedAreaToWds(){
	var wdsXML = "";
	for(var i=0; i<SELECTED_WDS_TO_ASSIGN_AREA.length; i++){
		var wdsJson = SELECTED_WDS_TO_ASSIGN_AREA[i];
		wdsXML += getWdsXML(wdsJson.id);
	}
	var postWdsData = getWdsXML_LIST(wdsXML);
	
	$.ajax({
		type: 'POST',
		url: "${assignWdsUrl}/"+selectedDeviceAreaId+"/assignwds",
		data: postWdsData,
		success: function(data){
			if(SELECTED_PLUGLOAD_TO_ASSIGN_AREA != ""){
				assignSelectedAreaToPlugload();
			}else{
				setSelectedDeviceMessage("Assignment Successful", "green");
			}
		},
		dataType:"xml",
		contentType: "application/xml; charset=utf-8"
	});
}

function getWdsXML(wdsId){
	return "<wds><id>"+wdsId+"</id></wds>";
}

function getWdsXML_LIST(wdsXML){
	return "<wdsList>"+wdsXML+"</wdsList>";
}


function assignSelectedAreaToPlugload(){
	var plugloadXML = "";
	for(var i=0; i<SELECTED_PLUGLOAD_TO_ASSIGN_AREA.length; i++){
		var plugloadJson = SELECTED_PLUGLOAD_TO_ASSIGN_AREA[i];
		plugloadXML += getPlugloadXML(plugloadJson.id);
	}
	var postPlugloadData = getPlugloadXML_LIST(plugloadXML);
	
	$.ajax({
		type: 'POST',
		url: "${assignPlugloadUrl}/"+selectedDeviceAreaId+"/assignplugloads",
		data: postPlugloadData,
		success: function(data){
			setSelectedDeviceMessage("Assignment Successful", "green");
		},
		dataType:"xml",
		contentType: "application/xml; charset=utf-8"
	});
}

function getPlugloadXML(plugloadId){
	return "<plugload><id>"+plugloadId+"</id></plugload>";
}

function getPlugloadXML_LIST(plugloadXML){
	return "<plugloads>"+plugloadXML+"</plugloads>";
}

function cancelSelectedDevices(){
	$("#assignAreaToDevicesDailog").dialog("close");
}

function setSelectedDeviceMessage(msg, color){
	$("#aasd-message-div").css("color", color);
	$("#aasd-message-div").html(msg);
}
</script>
</head>
<body id="aasd-main-box">
<table id="aasd-wrapper-table" width=100% height=100%>
	<tr>
		<td>
			<div id="aasd-message-div">&nbsp;</div>
		</td>
	</tr>
	<tr>
		<td id="aasd-form-container" valign="top">
			<div class="fieldLabel">Select Area:</div>
			<div class="fieldValue">
				<select id="aasd-area-combo" style="width:100%; height:100%;"> </select>
			</div>
		</td>
	</tr>
	<tr>
		<td height=auto align="right">
			<button id="aasd-apply-btn" onclick="assignSelectedAreaToDevices();">Apply</button>
			<button id="aasd-cancel-btn" onclick="cancelSelectedDevices();">Cancel</button>
		</td>
	</tr>
</table>
	
</body>
</html>