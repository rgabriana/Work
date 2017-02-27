<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<spring:url value="/services/org/profile" var="changeFixtureProfileUrl" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Assign Profile</title>

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

<style>
	table#apf-wrapper-table td {padding: 0 20px}
	td#apf-form-container div.fieldLabel{float:left; width:35%; font-weight:bold;}
 	td#apf-form-container div.fieldValue{float:left; width:65%;}
	#apf-message-div {font-weight:bold; float: left;}
</style>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
</style>

<script type="text/javascript">
//load profile combo
$("#apf-profile-combo").empty();
<c:forEach items="${profiles}" var="group">
	$("#apf-profile-combo").append(new Option("${group.name}", "${group.id}"));
</c:forEach>

var UPDATE_PROFILE_COUNTER = 0;
$(document).ready(function() {
	
});

function assignProfileToFixture(){
	$("#apf-apply-btn").attr("disabled", true);
	
	var selectedGroupId = $("#apf-profile-combo").val(); 
	var selectedGroupName = $("#apf-profile-combo").find('option:selected').text();

	selectedGroupId = encodeURIComponent(selectedGroupId);
	selectedGroupName = encodeURIComponent(selectedGroupName);
		
	for(var i=0; i<SELECTED_FIXTURES_TO_UPDATE_PROFILE.length; i++){
		var fixtureJson = SELECTED_FIXTURES_TO_UPDATE_PROFILE[i];
		var postData = getFixtureXML(fixtureJson.id);
		var originalprofile = fixtureJson.currentprofile;
		
		// url : /services/org/profile/assign/to/{currentprofile}/from/{originalprofile}/gid/{groupid}
		$.ajax({
			type: 'POST',
			url: "${changeFixtureProfileUrl}/assign/to/"+selectedGroupName+"/from/"+originalprofile+"/gid/"+selectedGroupId,
			data: postData,
			success: function(data){
				//alert("done");
			},
			complete: function(){
				UPDATE_PROFILE_COUNTER++;
				if(UPDATE_PROFILE_COUNTER >= SELECTED_FIXTURES_TO_UPDATE_PROFILE.length){
					setFixtureProfileMessage("Success ("+UPDATE_PROFILE_COUNTER+"/"+SELECTED_FIXTURES_TO_UPDATE_PROFILE.length+")", "green");
				} else {
					setFixtureProfileMessage("Processing ("+UPDATE_PROFILE_COUNTER+"/"+SELECTED_FIXTURES_TO_UPDATE_PROFILE.length+")", "green");
				}
			},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8"
		});
	}
	
}

function getFixtureXML(fixtureId){
	return "<fixture><id>"+fixtureId+"</id></fixture>";
}

function cancelProfileToFixture(){
	$("#assignProfileToFixturesDailog").dialog("close");
}

function setFixtureProfileMessage(msg, color){
	$("#apf-message-div").css("color", color);
	$("#apf-message-div").html(msg);
}
</script>
</head>
<body id="apf-main-box">
<table id="apf-wrapper-table" width=100% height=100%>
	<tr>
		<td>
			<div id="apf-message-div">&nbsp;</div>
		</td>
	</tr>
	<tr>
		<td id="apf-form-container" valign="top">
			<div class="fieldLabel">Select Profile:</div>
			<div class="fieldValue">
				<select id="apf-profile-combo" style="width:100%; height:100%;"> </select>
			</div>
		</td>
	</tr>
	<tr>
		<td height=auto align="right">
			<button id="apf-apply-btn" onclick="assignProfileToFixture();">Apply</button>
			<button id="apf-cancel-btn" onclick="cancelProfileToFixture();">Cancel</button>
		</td>
	</tr>
</table>
	
</body>
</html>