<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/area" var="assignAreaToFixturesUrl" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Assign Area</title>

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

<style>
	table#aaf-wrapper-table td {padding: 0 20px}
	td#aaf-form-container div.fieldLabel{float:left; width:35%; font-weight:bold;}
 	td#aaf-form-container div.fieldValue{float:left; width:65%;}
	#aaf-message-div {font-weight:bold; float: left;}
</style>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
</style>

<script type="text/javascript">
//load profile combo
$("#aaf-area-combo").empty();
<c:forEach items="${areas}" var="area">
	$('#aaf-area-combo').append($('<option></option>').val("${area.id}").html("${area.name}"));
</c:forEach>

var UPDATE_AREA_COUNTER = 0;
$(document).ready(function() {
	
});

function assignAreaToFixture(){
	if(SELECTED_FIXTURES_TO_ASSIGN_AREA.length == 0){
		setFixtureAreaMessage("Please select a fixture.", "red");
		return false;
	}
	
	var selectedAreaId = $("#aaf-area-combo").val();
	if(selectedAreaId == null || selectedAreaId == ""){
		setFixtureAreaMessage("Please select an area.", "red");
		return false;
	}

	$("#aaf-apply-btn").attr("disabled", true);
	
	var fixtureXML = "";
	for(var i=0; i<SELECTED_FIXTURES_TO_ASSIGN_AREA.length; i++){
		var fixtureJson = SELECTED_FIXTURES_TO_ASSIGN_AREA[i];
		fixtureXML += getFixtureXML(fixtureJson.id);
	}
	var postData = getFixtureXML_LIST(fixtureXML);
	
	setFixtureAreaMessage("Processing...", "black");
	// url : /services/org/area/{aid}/assignfixtures
	$.ajax({
		type: 'POST',
		url: "${assignAreaToFixturesUrl}/"+selectedAreaId+"/assignfixtures",
		data: postData,
		success: function(data){
			setFixtureAreaMessage("Assignment Successful.", "green");
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

function cancelAreaToFixture(){
	$("#assignAreaToFixturesDailog").dialog("close");
}

function setFixtureAreaMessage(msg, color){
	$("#aaf-message-div").css("color", color);
	$("#aaf-message-div").html(msg);
}
</script>
</head>
<body id="aaf-main-box">
<table id="aaf-wrapper-table" width=100% height=100%>
	<tr>
		<td>
			<div id="aaf-message-div">&nbsp;</div>
		</td>
	</tr>
	<tr>
		<td id="aaf-form-container" valign="top">
			<div class="fieldLabel">Select Area:</div>
			<div class="fieldValue">
				<select id="aaf-area-combo" style="width:100%; height:100%;"> </select>
			</div>
		</td>
	</tr>
	<tr>
		<td height=auto align="right">
			<button id="aaf-apply-btn" onclick="assignAreaToFixture();">Apply</button>
			<button id="aaf-cancel-btn" onclick="cancelAreaToFixture();">Cancel</button>
		</td>
	</tr>
</table>
	
</body>
</html>