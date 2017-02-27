<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<spring:url value="/services/org/gemsgroups/loadbyname/" var="loadGroupByNameUrl" scope="request" />

<style>
	#GroupPromptTable table {
		border: thin dotted #7e7e7e;
		padding: 20px;
	}
	
	#GroupPromptTable th {
		text-align: right;
		vertical-align: top;
		padding-right: 10px;
	}
	
	#GroupPromptTable td {
		vertical-align: top;
		padding-top: 2px;
	}
	
	
	
</style>


<div style="margin:10px 0px 0px 20px;">
	
	<div id="group-message-div"></div>
	
	<table id="GroupPromptTable">
		
		<tr>
			<th >Enter Group Name:</th>
			<td >
				<input type="text" id="groupName">
			</td>
		</tr>
		<tr>
			<th><span></span></th>
			<td>
				<button type="button" id="groupBtnOk" >
					<spring:message code="action.ok" />
				</button>&nbsp;&nbsp;
				<button type="button" id="groupBtnClose">
				<spring:message code="action.cancel" />
				</button>
			</td>
		</tr>
	</table>
	
</div>

<script type="text/javascript">

var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";

$(document).ready(function() {
	//add click handler
	$('#groupBtnOk').click(function(){startValidation();});
	$('#groupBtnClose').click(function(){cancelValidation();});
	
});

function startValidation(){
	
	clearFxInitialMessage();
	
	var groupName = $("#groupName").val();
	
	if(groupName == null || groupName == ""){
		displayFxInitialMessage("Please enter a Group Name", COLOR_FAILURE);
		return;
	}
	
	checkDuplicateGroupName(groupName);
}


function saveGroup(groupName) {
	parent.parent.showGroupWidgetDialog('',groupName);
	exitWindow();
}

function cancelValidation(){
	exitWindow();
}

function exitWindow(){
	$('#groupPromptDialog').dialog('close');
}

function displayFxInitialMessage(Message, Color) {
	$("#group-message-div").html(Message);
	$("#group-message-div").css("color", Color);
}
function clearFxInitialMessage() {
	displayFxInitialMessage("", COLOR_DEFAULT);
}

function checkDuplicateGroupName(groupName) {
	$.ajax({
 		url: "${loadGroupByNameUrl}"+groupName+"?ts="+new Date().getTime(),
 		success: function(data){
			if(data == null){
				saveGroup(groupName);
			} else {
				displayFxInitialMessage("The Group with the name already exists.", COLOR_FAILURE);
				
			}
		},
		error: function(){
			displayFxInitialMessage("Failed to load Group", COLOR_FAILURE);
		},
 		dataType:"json",
 		contentType: "application/json; charset=utf-8",
 	});
	
}


</script>

