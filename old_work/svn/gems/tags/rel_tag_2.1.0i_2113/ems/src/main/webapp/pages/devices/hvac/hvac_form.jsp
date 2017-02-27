<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/devices/hvac/update.ems" var="updateHVACDevice" scope="request"/>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Create HVAC</title>

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

<style>
	table#apf-wrapper-table td {padding: 0 20px;}
	td#apf-form-container div.fieldLabel{float:left; width:35%; font-weight:bold;}
 	td#apf-form-container div.fieldValue{float:left; width:65%;}
	#apf-message-div {font-weight:bold; float: left;}
</style>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
</style>

<script type="text/javascript">

var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
var oldhvacname='';
$(document).ready(function(){
	oldhvacname = $("#hvacdevicename").val();
});

function getHvacName(){
	
	var chkhvacname = $("#hvacdevicename").val();
	var returnresult = false;
	
	$.ajax({
		type: "GET",
		cache: false,
		url: '<spring:url value="/services/org/hvac/list/"/>'+ chkhvacname,
		dataType: "text",
		async: false,
		success: function(msg) {
			var count = (msg).indexOf(chkhvacname);
			if(oldhvacname.toLowerCase() != chkhvacname.toLowerCase() && count > 0) {
				returnresult = true;
			}
			else {
				returnresult = false;
			}
		},
		error: function (jqXHR, textStatus, errorThrown){
			returnresult = false;
		}
	});
	return returnresult;
}

function getCommissionPlanObj(objectName) {			
	if ($.browser.mozilla) {
		return document[objectName];
	}
	return document.getElementById(objectName);
}	

function updateHvacDevice(){
	var flag = validateForm();
	if(flag)
	{
// 		$("#apf-apply-btn").attr('disabled', 'disabled');
// 		var xmlData = getHVACXML();
// 		$.ajax({
// 			url: "${updateHVACDevice}",
// 			type: "POST",
// 			data: xmlData, 
// 			dataType:"xml",
// 			contentType: "application/xml; charset=utf-8",
// 			success: function(data){
// 				setHvacDeviceMessage("Device updated successfully", COLOR_SUCCESS);
// 			},
// 			error: function() {
// 				setHvacDeviceMessage("Error in updating device", COLOR_FAILURE);
// 			}
// 		});	
		$('#hvacCreateForm').submit();
		$("#createHvacDeviceDailog").dialog("close");
	}
	
}

function validateForm()
{
	var flag = true;
	var numericExpression = /^[0-9]+$/;
	if( $("#hvacdeviceid").val().match(numericExpression)){
	}else
	{
		setHvacDeviceMessage("Please enter numeric values for Device Id", COLOR_FAILURE);
		return false;
	}
	
	var hvacDeviceName = $("#hvacdevicename").val();
	if(hvacDeviceName==null || hvacDeviceName.length==0)
	{
		setHvacDeviceMessage("Please enter Device Name", COLOR_FAILURE);
		return false;
	}
	
	var result = getHvacName();
	if(result){
		setHvacDeviceMessage("Duplicate Hvac name", COLOR_FAILURE);
		return false;
	}
	else {	
		setHvacDeviceMessage("", COLOR_DEFAULT);
	}
	
	return flag;
}
function getHVACXML(){
	var hvacDeviceName = $("#hvacdevicename").val();
	var hvacid = $("#hvacdeviceid").val();
	var hvacType = $("#hvacdevicetypecombo").val();
	var xaxis = "${hvacDevice.xaxis}";
	var yaxis = "${hvacDevice.xaxis}";
	var floorid = "${floorid}";
	var hvacUpdate = "<hvacdevice><name>"+hvacDeviceName+"</name><deviceid>"+hvacid+"</deviceid><floorid>"+floorid+"</floorid><xaxis>"+xaxis+"</xaxis><yaxis>"+yaxis+"</yaxis><devicetype>"+hvacType+"</devicetype></hvacdevice>"; 
	alert(hvacUpdate);
	return hvacUpdate;
}

function cancelHvacCreate(){
	$("#createHvacDeviceDailog").dialog("close");
}

function setHvacDeviceMessage(msg, color){
	$("#hvac-message-div").css("color", color);
	$("#hvac-message-div").html(msg);
}
function clearHvacDeviceMessage(Message, Color) {
	setHvacDeviceMessage("", COLOR_DEFAULT);
}
</script>
</head>
<body id="apf-main-box">
<form:form id="hvacCreateForm" commandName="hvacDevice" method="post" action="${updateHVACDevice}">
<form:hidden path="id"/>
<table id="apf-wrapper-table" width=100% height=100%>
	<tr>
		<td colspan="2">
			<div id="hvac-message-div">&nbsp;</div>
		</td>
	</tr>
	<tr>
		<td id="apf-form-container" valign="top" colspan="2">
			<div class="fieldLabel">Device Id:</div> 
					<div><form:input id="hvacdeviceid" path="deviceId" type="text" /></div>
			<div style="height: 10px;"></div>
			<div class="fieldLabel">Device Name:</div> 
					<div><form:input id="hvacdevicename" path="name" type="text"  /></div>
					<div style="height: 10px;"></div>
			<div class="fieldLabel">Device Type:</div>
			<div class="fieldValue">
			
			<form:select id="hvacdevicetypecombo" path="deviceType"  >
							<form:option value="1" label="Cassette AC"/>
							<form:option value="2" label="Duct AC"/>
							<form:option value="3" label="Split AC" />
			</form:select> 
			</div>
			<br style="clear:both;"/>
		</td>
	</tr>
	<tr>
		<td height=auto align="right" colspan="2">
			<input id="apf-apply-btn" type="button" value="Update" onclick="updateHvacDevice();"/>
		</td>
	</tr>
</table>
</form:form>
</body>
</html>