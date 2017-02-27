<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/area" var="assignSwitchesUrl" scope="request" />

<spring:url value="/services/org/area" var="assignFixturesUrl" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Assign Area to Switches and Fixtures</title>

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

<style>
	table#aasf-wrapper-table td {padding: 0 20px}
	td#aasf-form-container div.fieldLabel{float:left; width:35%; font-weight:bold;}
 	td#aasf-form-container div.fieldValue{float:left; width:65%;}
	#aasf-message-div {font-weight:bold; float: left;}
</style>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
</style>

<script type="text/javascript">


$(document).ready(function() {
	$("#aasf-area-combo").empty();
	<c:forEach items="${areas}" var="area">
		$('#aasf-area-combo').append($('<option></option>').val("${area.id}").html("${area.name}"));
	</c:forEach>

});

var selectedSwitchFixtureAreaId = "";

function assignAreaToSwitchAndFixture(){
	if(SELECTED_SWITCHES_TO_ASSIGN_AREA.length == 0 && SELECTED_FIXTURES_TO_ASSIGN_AREA.length == 0){
		setSwitchFixtureAreaMessage("Please select a switch or fixture", "red");
		return false;
	}
	
	selectedSwitchFixtureAreaId = $("#aasf-area-combo").val();
	if(selectedSwitchFixtureAreaId == null || selectedSwitchFixtureAreaId == ""){
		setSwitchFixtureAreaMessage("Please select an area.", "red");
		return false;
	}

	$("#aasf-apply-btn").attr("disabled", "disabled");
	
	var switchXML = "";
	for(var i=0; i<SELECTED_SWITCHES_TO_ASSIGN_AREA.length; i++){
		var switchJson = SELECTED_SWITCHES_TO_ASSIGN_AREA[i];
		switchXML += getSwitchXML(switchJson.id);
	}
	var postData = getSwitchXML_LIST(switchXML);
	
	setSwitchFixtureAreaMessage("Processing...", "black");
	
	$.ajax({
		type: 'POST',
		url: "${assignSwitchesUrl}/"+selectedSwitchFixtureAreaId+"/assignswitches",
		data: postData,
		success: function(data){
			assignAreaToFixtures();
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

function assignAreaToFixtures(){
	var fixtureXML = "";
	for(var i=0; i<SELECTED_FIXTURES_TO_ASSIGN_AREA.length; i++){
		var fixtureJson = SELECTED_FIXTURES_TO_ASSIGN_AREA[i];
		fixtureXML += getFixtureXML(fixtureJson.id);
	}
	var postFixtureData = getFixtureXML_LIST(fixtureXML);
	
	$.ajax({
		type: 'POST',
		url: "${assignFixturesUrl}/"+selectedSwitchFixtureAreaId+"/assignfixtures",
		data: postFixtureData,
		success: function(data){
			setSwitchFixtureAreaMessage("Assignment Successful.", "green");
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

function cancelAreaToSwitchAndFixture(){
	$("#assignAreaToSwitchesAndFixturesDailog").dialog("close");
}

function setSwitchFixtureAreaMessage(msg, color){
	$("#aasf-message-div").css("color", color);
	$("#aasf-message-div").html(msg);
}
</script>
</head>
<body id="aasf-main-box">
<table id="aasf-wrapper-table" width=100% height=100%>
	<tr>
		<td>
			<div id="aasf-message-div">&nbsp;</div>
		</td>
	</tr>
	<tr>
		<td id="aasf-form-container" valign="top">
			<div class="fieldLabel">Select Area:</div>
			<div class="fieldValue">
				<select id="aasf-area-combo" style="width:100%; height:100%;"> </select>
			</div>
		</td>
	</tr>
	<tr>
		<td height=auto align="right">
			<button id="aasf-apply-btn" onclick="assignAreaToSwitchAndFixture();">Apply</button>
			<button id="aasf-cancel-btn" onclick="cancelAreaToSwitchAndFixture();">Cancel</button>
		</td>
	</tr>
</table>
	
</body>
</html>