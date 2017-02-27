<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<spring:url value="/services/org/user/switch/save" var="saveSwitchToUserUrl" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Assign Users</title>

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

<style>
	#asu-user-grid-container tr.jqgroup {background-color: #F1F1F1 !important;}
	/* #asu-top-panel-container {border-bottom-style:solid; border-bottom-width:1px; border-bottom-color: #555555;} */
	#asu-message-div {font-weight:bold; float: left; padding: 5px 0 0 5px;}
	#asu-apply-btn {float: left;}
</style>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
</style>

<script type="text/javascript">
var usersdata =  [];
<c:forEach items="${users}" var="user">
	var userJson = new Object;
	userJson.id = "${user.id}";
	userJson.firstname = "${user.firstName}";
	userJson.lastname = "${user.lastName}";
	userJson.email = "${user.email}";
	userJson.type = "Facility Users";
	userJson.selected = "${user.selected}";
	var tenantid = "${user.tenant.id}";
	if(tenantid != ""){
		userJson.type = "Tenant Users : ${user.tenant.name}";
	}
	usersdata.push(userJson);
</c:forEach>

$(document).ready(function() {
	createUsersGrid();
	//resizeAssignUserToSwitchesDialog();
});

function createUsersGrid(){
	//take column width as 33% of 60% of total width
	var colWidth = Math.floor($('body').width() * .60 * .33) - 20;
	
	jQuery("#asu-user-table").jqGrid({
		datatype: "local",
		data: usersdata,
		scrollOffset: 0,
		autowidth: false,
		forceFit: false,
		height:'auto',
	   	colNames:['id', 'Email', 'First Name', 'Last Name', 'type'],
	   	colModel:[
  	   		{name:'id', index:'id', hidden: true},
	  	   	{name:'email', index:'email', sortable:false, width: colWidth},
	  	   	{name:'firstname', index:'firstname', sortable:false, width: colWidth},
	   		{name:'lastname', index:'lastname', sortable:false, width: colWidth},	   		
	   		{name:'type', index:'type', width:"0%"}
	   	],
	   	sortname: 'type',
	    viewrecords: true,
	    multiselect: true,
	    grouping:true,
		groupingView : { 
			groupField : ['type'], 
			groupColumnShow : [false], 
			groupText : ['<b>{0} ({1})</b>'] 
		},
	    loadComplete: function(data) {
			
			
			<c:forEach items="${users}" var="user">
			
				if("${user.selected}" == "true"){
					jQuery("#asu-user-table").setSelection ("${user.id}", true);
				}
				
			</c:forEach>
			
		}
	});
}

function applyUsersToSwitch(){
	setAssignUserMessage("","black");
	
	//get userids
	var selIds = jQuery("#asu-user-table").getGridParam('selarrrow');
	var userNum = selIds.length;
	
	if(userNum > 0){
		var userSwitchXML = "";
		for(var i=0; i<userNum; i++){
			var userRow = jQuery("#asu-user-table").jqGrid('getRowData', selIds[i]);
			
			//Get selected switches from global variable 'SELECTED_SWITCHES'
			for(var j=0; j<SELECTED_SWITCHES.length; j++){
				userSwitchXML += getUserSwitchXML(userRow.id, SELECTED_SWITCHES[j].id);
			}
		}
		saveUserSwitchMapping(getUserSwitchXML_List(userSwitchXML));
	} else {
		setAssignUserMessage("Please select a user.", "red");
		return false;
	}
}

function saveUserSwitchMapping(userSwitchXMLData){
	$.ajax({
		type: 'POST',
		url: "${saveSwitchToUserUrl}",
		data: userSwitchXMLData,
		success: function(data){
				if(data != null){
					var xml=data.getElementsByTagName("response");
					for (var j=0; j<xml.length; j++) {
// 						var status = xml[j].getElementsByTagName("status")[0].childNodes[0].nodeValue;
						setAssignUserMessage("Assignment successful.", "green");
						//$("#asu-apply-btn").attr("disabled", true);
					}
				}
			},
		dataType:"xml",
		contentType: "application/xml; charset=utf-8",
	});
}

function getUserSwitchXML(userId, SwitchId){
	return "<userSwitches><id></id><userid>"+userId+"</userid><switchid>"+SwitchId+"</switchid></userSwitches>";
}

function getUserSwitchXML_List(userSwitchXML){
	return "<userSwitchess>"+userSwitchXML+"</userSwitchess>";
}

function resizeAssignUserToSwitchesDialog(){
	//resize strobed fixture grid	
	var gridContainerEL = document.getElementById("asu-user-grid-container");
	forceFitJQgridHeight(jQuery("#asu-user-table"), gridContainerEL.offsetHeight);
	jQuery("#asu-user-table").setGridWidth(gridContainerEL.offsetWidth);	
}

function forceFitJQgridHeight(jgrid, containerHeight){
	var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
	var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
	var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
	jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - gridHeaderFooterHeight) * .99));
}

function setAssignUserMessage(msg, color){
	$("#asu-message-div").css("color", color);
	$("#asu-message-div").html(msg);
}
</script>

</head>
<body id="asu-main-box">
<table width=100% height=100% cellspacing="10px">
	<tr>
		<td id="asu-top-panel-container" valign="top" height=45>
			<div style="height:10px;"></div>
			<button id="asu-apply-btn" onclick="applyUsersToSwitch();">Assign Users</button>
			<div id="asu-message-div"></div>
		</td>
	</tr>
	<tr>
		<td id="asu-user-grid-container" valign="top" height=auto>
			<table id="asu-user-table"></table>
		</td>
	</tr>
</table>

</body>
</html>