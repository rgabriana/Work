<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/area" var="assignAreaToSwitchesUrl" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Assign Area</title>

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

<style>
	table#aas-wrapper-table td {padding: 0 20px}
	td#aas-form-container div.fieldLabel{float:left; width:35%; font-weight:bold;}
 	td#aas-form-container div.fieldValue{float:left; width:65%;}
	#aas-message-div {font-weight:bold; float: left;}
</style>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
</style>

<script type="text/javascript">


var UPDATE_AREA_COUNTER = 0;
$(document).ready(function() {
	
	//load Area combo
	$("#aas-area-combo").empty();
	<c:forEach items="${areas}" var="area">
		$('#aas-area-combo').append($('<option></option>').val("${area.id}").html("${area.name}"));
	</c:forEach>
	
});

function assignAreaToSwitch(){
	if(SELECTED_SWITCHES_TO_ASSIGN_AREA.length == 0){
		setSwitchAreaMessage("Please select a switch.", "red");
		return false;
	}
	
	var selectedAreaId = $("#aas-area-combo").val();
	if(selectedAreaId == null || selectedAreaId == ""){
		setSwitchAreaMessage("Please select an area.", "red");
		return false;
	}

	$("#aas-apply-btn").attr("disabled", true);
	
	var switchXML = "";
	for(var i=0; i<SELECTED_SWITCHES_TO_ASSIGN_AREA.length; i++){
		var switchJson = SELECTED_SWITCHES_TO_ASSIGN_AREA[i];
		switchXML += getSwitchXML(switchJson.id);
	}
	var postData = getSwitchXML_LIST(switchXML);
	
	setSwitchAreaMessage("Processing...", "black");
	// url : /services/org/area/{aid}/assignfixtures
	$.ajax({
		type: 'POST',
		url: "${assignAreaToSwitchesUrl}/"+selectedAreaId+"/assignswitches",
		data: postData,
		success: function(data){
			setSwitchAreaMessage("Assignment Successful.", "green");
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

function cancelAreaToSwitch(){
	$("#assignAreaToSwitchesDailog").dialog("close");
}

function setSwitchAreaMessage(msg, color){
	$("#aas-message-div").css("color", color);
	$("#aas-message-div").html(msg);
}
</script>
</head>
<body id="aas-main-box">
<table id="aas-wrapper-table" width=100% height=100%>
	<tr>
		<td>
			<div id="aas-message-div">&nbsp;</div>
		</td>
	</tr>
	<tr>
		<td id="aas-form-container" valign="top">
			<div class="fieldLabel">Select Area:</div>
			<div class="fieldValue">
				<select id="aas-area-combo" style="width:100%; height:100%;"> </select>
			</div>
		</td>
	</tr>
	<tr>
		<td height=auto align="right">
			<button id="aas-apply-btn" onclick="assignAreaToSwitch();">Apply</button>
			<button id="aas-cancel-btn" onclick="cancelAreaToSwitch();">Cancel</button>
		</td>
	</tr>
</table>
	
</body>
</html>