<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<spring:url value="/services/org/scenelightlevels/add" var="saveLightLevelURl" scope="request" />
<spring:url value="/services/org/scenelightlevels/list/" var="getSceneLightLevelBySceneTemplateIdAndSceneOrderURL" scope="request" />

<style>
	#create_scenelightleveldialog{padding:10px 15px;}
	#create_scenelightleveldialog table{width:100%;}
	#create_scenelightleveldialog td{ padding-bottom:3px;}
	#create_scenelightleveldialog td.fieldLabel{width:35%; font-weight:bold;}
	#create_scenelightleveldialog td.fieldValue{width:65%;}
	#create_scenelightleveldialog .inputField{width:100%; height:20px;}
	#create_scenelightleveldialog #saveSceneLightLevelBtn{padding: 0 10px;}
	#create_scenelightleveldialog #closeSceneLightLevelBtn{padding: 0 10px;}
	#create_scenelightleveldialog .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}
</style>

<script type="text/javascript">
var requirederr = '<spring:message code="error.above.field.required"/>';
var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";

$(document).ready(function() {
});

function validateSceneLightLevelName()
{	var isValid = true;
	clearMessage();	
	var chkName = $("#name").val();	
	if(chkName=="" || chkName==" ")
	{		
		displayLightLevelPromtMessage("Name field is required", COLOR_FAILURE);		
		isValid = false;	
		return;
	}	
	var invalidFormatStr = 'Scene name must contain only letters, numbers, or underscore';
    var regExpStr = /^[a-z0-9\_\s]+$/i;
    if(regExpStr.test(chkName) == false) {
    	displayLightLevelPromtMessage(invalidFormatStr, COLOR_FAILURE);	
		isValid = false;    	
    } 
    var invalidFormatStr = 'Please enter a value in the range of 0 to 100';
    var lightLevel = $("#lightlevel").val();	
    var regex = /^[0-9\b]+$/;
    if(regex.test(lightLevel) == false) {
    	displayLightLevelPromtMessage(invalidFormatStr, COLOR_FAILURE);	
		isValid = false;    	
    } 
    if(lightLevel<0 || lightLevel >100)
   	{
    	displayLightLevelPromtMessage(invalidFormatStr, COLOR_FAILURE);	
		isValid = false; 
   	}
	var gridRowCount = jQuery("#scene-level-table").jqGrid('getGridParam', 'records');
	if(gridRowCount>0){		
		var data = jQuery("#scene-level-table").jqGrid('getRowData');
		for(var i=0;i<data.length;i++){
			if(lightLevel == data[i].lightlevel)
			{
				isValid = false; 
				displayLightLevelPromtMessage("Specified light level already exists", COLOR_FAILURE);
			}
		}
	}
    return isValid;
}

function postValidate(){
	
	if(validateSceneLightLevelName() == true){
		saveTemplate();
	}	
}

function saveTemplate(){	
	var sceneTemplateID = "${sceneTemplateID}";
	var xmlData= getSceneLightLevel(sceneTemplateID);
	$.ajax({
		type: 'POST',
		url: "${saveLightLevelURl}"+"?ts="+new Date().getTime(),
		data: xmlData,
		success: function(data){
			$('#sceneLightLevelPromptDialog').dialog('close');
			reloadSceneLightLevelGrid(sceneTemplateID);
		},
		dataType:"xml",
		contentType: "application/xml; charset=utf-8"
	});
}

function getSceneLightLevel(scenetemplateid)
{
	var xmldata ="";
	var name = $("#name").val();
	var lightlevel =  $("#lightlevel").val();
	var sceneOrder = $("#sceneOrder").val();
	var id = $("#id").val();
	xmldata = "<scenelightleveltemplates>"+
		"<id>"+id+"</id>"+
		"<scenetemplateid>"+scenetemplateid+"</scenetemplateid>"+
		"<name>"+ name +"</name>"+
		"<lightlevel>"+ lightlevel +"</lightlevel>"+
		"<sceneOrder>"+ sceneOrder +"</sceneOrder>";
	    xmldata += "</scenelightleveltemplates>";
	return xmldata;
}

function closeSceneLightLevelDialog(){
	$('#sceneLightLevelPromptDialog').dialog('close');
}

function clearMessage()
{
	displayLightLevelPromtMessage("", COLOR_DEFAULT);
}

function displayLightLevelPromtMessage(Message, Color) {
	$("#errorMsg").html(Message);
	$("#errorMsg").css("color", Color);
}

function disableEnterKey(evt)
{
	 var keyCode = evt ? (evt.which ? evt.which : evt.keyCode) : event.keyCode;
     if (keyCode == 13) {
          return false;
     }
}

$("#create_scenelightleveldialog").submit(function(e){
    return false;
});
</script>
<div>
	<form:form id="create_scenelightleveldialog" commandName="sceneLightLevelTemplate" method="post" action="" onKeyPress="return disableEnterKey(event)">
        <form:hidden id="id"  path="id"/>
        <form:hidden id="sceneOrder" name="sceneOrder" path="sceneOrder" />
        <div id="errorMsg"></div>
		<table>
			<tr>
				<td class="fieldLabel">Scene Name</td>
				<td class="fieldValue"><form:input class="inputField" id="name" name="name" path="name" onkeypress="clearMessage();" onmousedown="clearMessage();"/></td>
			</tr>
			<tr>
				<td class="fieldLabel">Light Level</td>
				<td class="fieldValue"><form:input class="inputField"  id="lightlevel" name="lightlevel" path="lightlevel" onkeypress="clearMessage();" onmousedown="clearMessage();" /></td>
			</tr>			
			<tr>
			<td style="height: 5px;"> </td>
			</tr>
			<tr>
				<td colspan="2" align="center">
					<input type="button" id="saveSceneLightLevelBtn"
					value="<spring:message code="action.save" />" onclick="postValidate()">
					
					<input type="button" id="closeSceneLightLevelBtn"
					value="<spring:message code="action.cancel" />" onclick="closeSceneLightLevelDialog()">
				</td>
			</tr>
		</table>
	</form:form>
</div>