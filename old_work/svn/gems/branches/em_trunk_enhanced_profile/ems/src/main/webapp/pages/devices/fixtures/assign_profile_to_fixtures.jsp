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
/* table#apf-profile-table tr {background-color: #E8E8E8 !important;} */
/* div.fieldLabel{float:left; width:100%;}
	div.fieldHeader{float:left; width:100%; height:100%;font-weight:bold; background-color: #7C7C7C } */
#apf-profile-div {
	font-weight: bold;
	float: left;
	padding-left: 20px
}

div.fieldHeader{text-align: center;padding-top: 9px;padding-left: 20px;float:left; width:94%; height:25px;font-weight:bold; background-color: #7C7C7C;} 	

#main-container {
	padding-left: 20px;
}
/* #apf-profile-table{width:100%;} */
div.evenrow {
	background: #DDDDDD;
	color: #545454;
	height: 32px;
	font-weight: 500 !important;
}

div.oddrow {
	background: #FFFFFF;
	color: #545454;
	height: 32px;	
	font-weight: 500 !important;
}
</style>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
</style>

<script type="text/javascript">

var UPDATE_PROFILE_COUNTER = 0;
$(document).ready(function() {
	
});


function assignProfileToFixture(selectedGroupId,selectedGroupName){
	$("#apf-apply-profile-button").attr('disabled', 'disabled');
		
	selectedGroupId = encodeURIComponent(selectedGroupId);
	selectedGroupName = encodeURIComponent(selectedGroupName);
	
	setFixtureProfileMessage("Processing...", "green");
	UPDATE_PROFILE_COUNTER = 0;
		
	for(var i=0; i<SELECTED_FIXTURES_TO_UPDATE_PROFILE.length; i++){
		var fixtureJson = SELECTED_FIXTURES_TO_UPDATE_PROFILE[i];
		var postData = getFixtureXML(fixtureJson.id);
		var originalprofile = fixtureJson.currentprofile;
		
		// url : /services/org/profile/assign/to/{currentprofile}/from/{originalprofile}/gid/{groupid}
		$.ajax({
			type: 'POST',
			url: "${changeFixtureProfileUrl}/assign/to/"+selectedGroupName+"/from/"+originalprofile+"/gid/"+selectedGroupId,
			data: postData,
			async: false,
			success: function(data){
				fixtureJson.currentprofile = selectedGroupName;
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
	resizeWidthTable();
	$("#apf-apply-profile-button").removeAttr('disabled'); 
	
}

function getFixtureXML(fixtureId){
	return "<fixture><id>"+fixtureId+"</id></fixture>";
}

function cancelProfileToFixture(){
	$("#assignProfileToFixturesDailog").dialog("close");
}

function setFixtureProfileMessage(msg, color){
	$("#apf-profile-div").css("color", color);
	$("#apf-profile-div").html(msg);
}  

</script>
</head>
<body id="apf-main-box" >
<div id="main-container">	
	<div id="apf-profile-div">&nbsp;</div>
	<c:forEach items='${profileHierarchy.treeNodeList}' var='template'>	
	<div class="fieldHeader"><b>${template.name}</b></div>		
	<%
	int i = 1;	
	%>
	<c:forEach items='${template.treeNodeList}' var='profile'>	
	<table class="entable" >	
	<%
	if(i%2!=0)
	{				
	%>	
	<tr>	
	<%
	}
	%>	
	<%
	if(i%2==0)
	{	
	%>	
	<tr>	
	<%
	}
	%>
		
	<td>
	<c:if test="${template.name == profile.name}">		
	${profile.name}_Default	
	</c:if>
	<c:if test="${template.name != profile.name}">	
	${profile.name}				
	</c:if>	
	</td>	
	<td height=auto width = auto align="left" style="width:10%">
			<button id="apf-apply-profile-button" onclick="assignProfileToFixture('${profile.nodeId}','${profile.name}')">Apply</button>			
		</td>	 
	</tr>			
	</table>
	<% i++; %>
	</c:forEach>	
	<br>	
	</c:forEach>					
	</div>
</body>
</html>