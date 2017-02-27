<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/fixture/op" var="assignBleModeToFixturesUrl" scope="request" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Assign BLE Mode</title>

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

<style>
	table#abmf-wrapper-table td {padding: 0 20px}
	td#abmf-form-container div.fieldLabel{float:left; width:35%; font-weight:bold;}
 	td#abmf-form-container div.fieldValue{float:left; width:65%;}
	#abmf-message-div {font-weight:bold; float: left;}
</style>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
</style>

<script type="text/javascript">


var UPDATE_AREA_COUNTER = 0;
$(document).ready(function() {
	
	//load Area combo
	$("#abmf-combo").empty();
	<c:forEach items="${blemodes}" var="bleModes">
		$('#abmf-combo').append($('<option></option>').val("${bleModes.name}").html("${bleModes.name}"));
	</c:forEach>
	
});

function assignBleModeToFixture(){
	if(SELECTED_FIXTURES_TO_ASSIGN_BLEMODE.length == 0){
		setFixtureBlemodeMessage("Please select a fixture.", "red");
		return false;
	}
	
	var selectedMode = $("#abmf-combo").val();
	//var scanDuration = $("#abmf-duration").val();
	if(selectedMode == null || selectedMode == ""){
		setFixtureBlemodeMessage("Please select BLE Mode.", "red");
		return false;
	}
	/*
	if(scanDuration == null || scanDuration == "" || isNaN(scanDuration)){
		setFixtureBlemodeMessage("Please enter valid Scan Duration.", "red");
		return false;
	}
	*/

	//$("#abmf-apply-btn").attr("disabled", true);
	
	var fixtureXML = "";
	for(var i=0; i<SELECTED_FIXTURES_TO_ASSIGN_BLEMODE.length; i++){
		var fixtureJson = SELECTED_FIXTURES_TO_ASSIGN_BLEMODE[i];
		fixtureXML += getBleFixtureXML(fixtureJson.id);
	}
	var postData = getBleFixtureXML_LIST(fixtureXML);
	
	setFixtureBlemodeMessage("Processing...", "black");
	// url : /services/org/area/{aid}/assignfixtures
	$.ajax({
		type: 'POST',
		url: "${assignBleModeToFixturesUrl}/assignblemode/"+selectedMode,
		data: postData,
		success: function(data){
			setFixtureBlemodeMessage("Assignment Successful.", "green");
		},
		error: function(){
			console.log("Error saving Ble mode to fixtures");
		},
		dataType:"xml",
		contentType: "application/xml; charset=utf-8"
	});
}

function getBleFixtureXML(fixtureId){
	return "<fixture><id>"+fixtureId+"</id></fixture>";
}

function getBleFixtureXML_LIST(fixtureXML){
	return "<fixtures>"+fixtureXML+"</fixtures>";
}

function cancelBleModeToFixture(){
	$("#assignBleModeToFixturesDailog").dialog("close");
}

function setFixtureBlemodeMessage(msg, color){
	$("#abmf-message-div").css("color", color);
	$("#abmf-message-div").html(msg);
}
</script>
</head>
<body id="abmf-main-box">
<table id="abmf-wrapper-table" width=100% height=100%>
	<tr>
		<td>
			<div id="abmf-message-div">&nbsp;</div>
		</td>
	</tr>
	<tr>
		<td id="abmf-form-container" valign="top">
			<div class="fieldLabel">Select BLE Modes:</div>
			<div class="fieldValue">
				<select id="abmf-combo" style="width:100%; height:100%;"> </select>
			</div>
		</td>
	</tr>
	<!-- 
	<tr>
		<td id="abmf-form-container" valign="top">
			<div class="fieldLabel">Scan Duration(Seconds):</div>
			<div class="fieldValue">
				<input type="text" id="abmf-duration" style="width:25%; height:100%;" value=60 /> 
			</div>
		</td>
	</tr>
	 -->
	<tr>
		<td height=auto align="right">
			<button id="abmf-apply-btn" onclick="assignBleModeToFixture();">Apply</button>
			<button id="abmf-cancel-btn" onclick="cancelBleModeToFixture();">Cancel</button>
		</td>
	</tr>
</table>
	
</body>
</html>