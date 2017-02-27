<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<spring:url value="/services/org/switch/details/floor/" var="checkDuplicateSwitchUrl" scope="request"/>
<spring:url value="/services/org/switch/details/area/" var="checkDuplicateAreaSwitchUrl" scope="request"/>

<style>
	#SwitchPromptTable table {
		border: thin dotted #7e7e7e;
		padding: 20px;
	}
	
	#SwitchPromptTable th {
		text-align: right;
		vertical-align: top;
		padding-right: 10px;
	}
	
	#SwitchPromptTable td {
		vertical-align: top;
		padding-top: 2px;
	}
	
	#center {
	  height : 90% !important;
	}
	
	
</style>


<div style="margin:10px 0px 0px 20px;">
	
	<div id="switch-message-div"></div>
	
	<table id="SwitchPromptTable">
		
		<tr>
			<th >Enter Switch Name:</th>
			<td >
				<input type="text" id="switchName">
			</td>
		</tr>
		<tr>
			<th id="spfixureHeading">Fixture version:</th>
			<td >
				<input type="radio" name="version" id="version1x" value="1.X">1.X
				<input type="radio" name="version"  id="version2x" value="2.X" >2.X
			</td>
		</tr>
		<tr>
			<th><span></span></th>
			<td>
				<button type="button" id="switchBtnOk" >
					<spring:message code="action.ok" />
				</button>&nbsp;&nbsp;
				<button type="button" id="switchBtnClose">
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
	$('#switchBtnOk').click(function(){startValidation();});
	$('#switchBtnClose').click(function(){cancelValidation();});
	if(SWITCH_GROUP_VERSION != undefined && SWITCH_GROUP_VERSION != "")	{
		if(SWITCH_GROUP_VERSION == "2.x") {
			var radioVersion2x = document.getElementById("version2x");
			radioVersion2x.checked = true;
		}
		else if(SWITCH_GROUP_VERSION == "1.x") {
			var radioVersion1x = document.getElementById("version1x");
			radioVersion1x.checked = true;
		}
		$("#version1x").attr("disabled", true);
		$("#version2x").attr("disabled", true);
		$('#spfixureHeading').css({"color":"grey"});
	}
	else {
		var radioversion = document.getElementById("version2x");
		radioversion.checked = true;
	}
});

function startValidation(){
	
	clearFxInitialMessage();
	
	var switchName = $("#switchName").val();
	
	if(switchName == null || switchName == ""){
		displayFxInitialMessage("Please enter a switch Name", COLOR_FAILURE);
		return;
	}
	
	var radioversion1x = document.getElementById("version1x");
	var fixtureVersion;
	if(radioversion1x.checked == true)
		fixtureVersion = "1.x";
	else
		fixtureVersion = "2.x";
	checkDuplicateSwitchName(switchName, treenodetype, treenodeid, fixtureVersion);
	//createSwitch();
}


function saveSwitch(switchName,fixtureVersion) {
	parent.parent.showWidgetDialog('',switchName,fixtureVersion);
	exitWindow();
}

function cancelValidation(){
	exitWindow();
}

function exitWindow(){
	$('#switchPromptDialog').dialog('close');
}

function displayFxInitialMessage(Message, Color) {
	$("#switch-message-div").html(Message);
	$("#switch-message-div").css("color", Color);
}
function clearFxInitialMessage() {
	displayFxInitialMessage("", COLOR_DEFAULT);
}

function checkDuplicateSwitchName(switchName, treenodetype , treenodeid, fixtureVersion) {
	if(treenodetype=='floor')
	{
		$.ajax({
			type: 'POST',
			async: true,
			url: "${checkDuplicateSwitchUrl}"+treenodeid+"/"+encodeURIComponent(switchName)+"?ts="+new Date().getTime(),
			data: "",
			success: function(data){
				if(data == null)
				{
					saveSwitch(switchName, fixtureVersion);
				}
				else
				{
					displayFxInitialMessage("The Switch with the name already exists.", COLOR_FAILURE);
				}
			},
			dataType:"json",
			contentType: "application/json; charset=utf-8"
		});
	}
	else
	{
		$.ajax({
			type: 'POST',
			url: "${checkDuplicateAreaSwitchUrl}"+treenodeid+"/"+encodeURIComponent(switchName)+"?ts="+new Date().getTime(),
			data: "",
			success: function(data){
				if(data == null){
					saveSwitch(switchName);
				}
				else{
					displayFxInitialMessage("The Switch with the name already exists.", COLOR_FAILURE);
				}
			},
			dataType:"json",
			contentType: "application/json; charset=utf-8"
		});
	}
	
}


</script>

