<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<spring:url value="/services/org/plugloadProfile" var="changePlugloadProfileUrl" scope="request" />

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

var UPDATE_PLUGLOAD_PROFILE_COUNTER = 0;
$(document).ready(function() {
	
});

var previousId="##";

function assignPlugloadProfileToPlugload(selectedGroupId,selectedGroupName,id){
		$("#apf-apply-plugload-profile-button").attr('disabled', 'disabled');
		id = encodeURIComponent(id);
		if(!("#"+id==previousId)) {
		selectedGroupId = encodeURIComponent(selectedGroupId);
		selectedGroupName = encodeURIComponent(selectedGroupName);
		
		setPlugloadProfileMessage("Applying,Please wait...", "green",id);
		UPDATE_PLUGLOAD_PROFILE_COUNTER = 0;
		var postData = getPlugloadXML();
		$.ajax({
			type: 'POST',
			url: "${changePlugloadProfileUrl}/bulkassign/"+selectedGroupName+"/"+selectedGroupId,
			data: postData,
			async: false,
			success: function(data){
				if(data!=null){ 
					var xml=data.getElementsByTagName("response");
					for (var j=0; j<xml.length; j++) {
						var status = xml[j].getElementsByTagName("status")[0].childNodes[0].nodeValue;
						UPDATE_PLUGLOAD_PROFILE_COUNTER = status;
					}
				}
			},
			complete: function(){
				//UPDATE_PLUGLOAD_PROFILE_COUNTER++;
				if(UPDATE_PLUGLOAD_PROFILE_COUNTER >= SELECTED_PLUGLOADS_TO_UPDATE_PROFILE.length){
					setPlugloadProfileMessageForSuccess("Success ("+UPDATE_PLUGLOAD_PROFILE_COUNTER+"/"+SELECTED_PLUGLOADS_TO_UPDATE_PROFILE.length+")", "green",id);
				} else {
					setPlugloadProfileMessage("Failed ("+UPDATE_PLUGLOAD_PROFILE_COUNTER+"/"+SELECTED_PLUGLOADS_TO_UPDATE_PROFILE.length+")", "Red",id);
				}
			},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8"
		});
	}
	$("#apf-apply-plugload-profile-button").removeAttr('disabled'); 
}

function getPlugloadXML(){
	var xmlStr="<plugloads>";
	for(var i=0; i<SELECTED_PLUGLOADS_TO_UPDATE_PROFILE.length; i++)
	{
		xmlStr+="<plugload><id>"+ SELECTED_PLUGLOADS_TO_UPDATE_PROFILE[i].id +"</id></plugload>";
	}
	xmlStr+="</plugloads>";
	return xmlStr;
}

function cancelProfileToPlugload(){
	$("#assignPlugloadProfileToPlugloadsDailog").dialog("close");
}

function setPlugloadProfileMessage(msg, color,id){
	var name = "#" + id;
	//alert(name);
	$(name).css("color", color);
	$(name).html(msg);	
}  

function setPlugloadProfileMessageForSuccess(msg, color,id){
	var name = "#" + id;	
	$(name).css("color", color);
	$(name).html(msg);
	if(previousId != "##")
		{
		//clear the previous text message
		$(previousId).html("");		
		}
	previousId = name;
}

</script>
</head>
<body id="apf-main-box" >
<div id="main-container">	
	<div id="spaceTaker">&nbsp;</div>
	<c:forEach items='${plugloadProfileHierarchy.treeNodeList}' var='template'>	
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
	${profile.name}				
	</td>	
	<td style="width:40%;left-margin:3px;" align="center">
		<div id='${profile.nodeId}'>&nbsp;</div>
	</td>
	
	<td height=auto width = auto align="left" style="width:10%">
		
			<button id="apf-apply-plugload-profile-button" onclick="assignPlugloadProfileToPlugload('${profile.nodeId}','${profile.name}','${profile.nodeId}')">Apply</button>			
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