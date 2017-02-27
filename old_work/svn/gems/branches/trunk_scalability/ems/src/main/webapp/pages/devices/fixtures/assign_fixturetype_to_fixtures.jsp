<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/fixtureclassservice" var="assignFixtureTypeToFixturesUrl" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Assign Fixture Type</title>

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

<style>
	table#aft-wrapper-table td {padding: 0 20px}
	td#aft-form-container div.fieldLabel{float:left; width:35%; font-weight:bold;}
 	td#aft-form-container div.fieldValue{float:left; width:65%;}
	#aft-message-div {font-weight:bold; float: left;}
</style>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
</style>

<script type="text/javascript">


var UPDATE_AREA_COUNTER = 0;
$(document).ready(function() {
	
	//load Area combo
	$("#aft-fixturetype-combo").empty();
	<c:forEach items="${fixturetypeArr}" var="fixturetype">
		$('#aft-fixturetype-combo').append($('<option></option>').val("${fixturetype.id}").html("${fixturetype.name}"));
	</c:forEach>
	
});

function assignFixtureTypeToFixture(){
	if(SELECTED_FIXTURES_TO_FIXTURE_TYPE.length == 0){
		setFixtureTypeMessage("Please select a fixture.", "red");
		return false;
	}
	
	var selectedFixtureTypeId = $("#aft-fixturetype-combo").val();
	if(selectedFixtureTypeId == null || selectedFixtureTypeId == ""){
		setFixtureTypeMessage("Please select fixture type.", "red");
		return false;
	}

	$("#aft-apply-btn").attr("disabled", true);
	
	var fixtureXML = "";
	for(var i=0; i<SELECTED_FIXTURES_TO_FIXTURE_TYPE.length; i++){
		var fixtureJson = SELECTED_FIXTURES_TO_FIXTURE_TYPE[i];
		fixtureXML += getFixtureXML(fixtureJson.id);
	}
	var postData = getFixtureXML_LIST(fixtureXML);
	
	setFixtureTypeMessage("Processing...", "black");
	
	$.ajax({
		type: 'POST',
		url: "${assignFixtureTypeToFixturesUrl}/assignfixturetype/"+selectedFixtureTypeId,
		data: postData,
		success: function(data){
			setFixtureTypeMessage("Assignment Successful.", "green");
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

function cancelFixtureTypeToFixture(){
	$("#assignFixtureTypeToFixturesDailog").dialog("close");
}

function setFixtureTypeMessage(msg, color){
	$("#aft-message-div").css("color", color);
	$("#aft-message-div").html(msg);
}
</script>
</head>
<body id="aft-main-box">
<table id="aft-wrapper-table" width=100% height=100%>
	<tr>
		<td>
			<div id="aft-message-div">&nbsp;</div>
		</td>
	</tr>
	<tr>
		<td id="aft-form-container" valign="top">
			<div class="fieldLabel">Select Fixture Type:</div>
			<div class="fieldValue">
				<select id="aft-fixturetype-combo" style="width:100%; height:100%;"> </select>
			</div>
		</td>
	</tr>
	<tr>
		<td height=auto align="right">
			<button id="aft-apply-btn" onclick="assignFixtureTypeToFixture();">Apply</button>
			<button id="aft-cancel-btn" onclick="cancelFixtureTypeToFixture();">Cancel</button>
		</td>
	</tr>
</table>
	
</body>
</html>