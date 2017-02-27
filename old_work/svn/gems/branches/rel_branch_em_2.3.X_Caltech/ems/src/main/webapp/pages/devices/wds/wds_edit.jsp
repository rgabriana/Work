<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>ERC Edit</title>

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />


<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
	
	.ui-dialog-title{font-size: 1.4em; font-weight: bolder;}
</style>

<style>
#wds-edit-wrapper .fieldWrapper{padding-bottom:4px;width:100%;}
#wds-edit-wrapper .fieldlabel{float:left; height:22px; width: 14%; font-weight: bold;}
#wds-edit-wrapper .fieldInput{float:left; height:22px; width: 50%;}

</style>


<script type="text/javascript">
var wdsid=${wds.id};
var UPDATE_PROFILE_COUNTER = 0;
$(document).ready(function() {
	
});

function closeTemplate(){
	$("#wdsEditDialog").dialog('close');
	
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

function closeTemplate(){
	$("#wdsEditDialog").dialog('close');
	
}

function clearMessageTemplate()
{		 
		 $("#wdserrorMsg").text("");
		 $("#wdsname").removeClass("invalidField");
}

function saveTemplate()
{	
	var id = $.trim(wdsid);
	var newname = $.trim($('#wdsname').val());
	$.ajax({
		type: "POST",
		cache: false,
		url: '<spring:url value="/services/org/wds/save/"/>'+ id + '/' + newname,
		dataType: "text",
		async: false,
		success: function(msg) {		
			closeTemplate();
		},
		error: function (jqXHR, textStatus, errorThrown){			
			returnresult = false;
		}
	});	
}

function validateEWSTemplate()
{
	var chktemplatename = $("#wdsname").val();
	var returnresult = false;	
	if(chktemplatename=="" || chktemplatename==" ")
	{		
		 clearMessageTemplate();
		 $("#wdserrorMsg").text("Above field is required.");
		 $("#wdsname").addClass("invalidField");
		return false;
	}	
	
	var invalidFormatStr = 'ERC name must contain only letters, numbers, or underscore';
    var regExpStr = /^[a-z0-9\_\s]+$/i;
    if(regExpStr.test(chktemplatename) == false) {
    	$("#wdserrorMsg").text(invalidFormatStr);
		$("#wdsname").addClass("invalidField");
    	return false;
    }
    
	$.ajax({
		type: "GET",
		cache: false,
		url: '<spring:url value="/services/org/wds/duplicatecheck/"/>'+ chktemplatename,
		dataType: "text",
		async: false,
		success: function(msg) {			
			var count = (msg).indexOf(chktemplatename);					
			if(count > 0) {
				returnresult = false;
			}
			else {
				returnresult = true;
			}
		},
		error: function (jqXHR, textStatus, errorThrown){			
			returnresult = false;
		}
	});	
	if(!returnresult){
		clearMessageTemplate();
		$("#wdserrorMsg").text('<spring:message code="error.duplicate.ews"/>');
		$("#wdsname").addClass("invalidField");
		return false;
	}	
	else {	
		clearMessageTemplate();
		saveTemplate();
	}
	
}

</script>
</head>
<body id="wdsedit-main-box" >

<div id = "wdseditboxButton" align="left" >

<div id = "wds-edit-wrapper" style="height:100%; width:95%;padding-left: 10px;">
<div style="height:3px;"></div>

<div class="fieldWrapper" >
			<div class="fieldlabel" >Name : </div>
			<div class="fieldInput"><input class="text" id="wdsname" name="wdsname" value="${wds.name}"/></div>			
			<br style="clear:both;"/>
			<span id="wdserrorMsg" class="error"></span>
</div>

<div class="fieldWrapper">
			<div class="fieldlabel" >Model : </div>
			<div class="fieldlabel"><c:out value="${wdsmodel}"></c:out></div>			
			<br style="clear:both;"/>
</div>
</div>

</div>

<div id ="saveews" align="left" style="padding-left: 20px;padding-bottom: 10px;">
<input type="button" id="saveTemplateBtn"
					value="<spring:message code="action.save" />" onclick="validateEWSTemplate()">			
</div>

<div style = "padding-left: 10px;">
<table id="wdseditTable" class="entable" style="width: 95%; height: 100%;">
<thead>
						<tr class="editableRow">
							<th width="20%" align="left">ERC</th>
							<th  align="left">Manipulation</th>
							<th  align="left">Action</th>
							<th  align="left">Manipulation</th>
							<th  align="left">Action</th>							
							</tr>
						</thead>
					
						<tr class="editableRow">
							<td  align="left"><button id="wdsediton-apply-btn"  style="width: 50px;background:#A9A9A9;">On</button></th>
							<td  align="left">----Tap----></th>
							<td  align="left">Select Scene All On</th>
							<td  align="left">Press & Hdd -></th>
							<td  align="left">brighten</th>							
						</tr>
						
						<tr class="editableRow">
							<td  align="left"><button id="wdseditoff-apply-btn" style="width: 50px;background:#A9A9A9;">Off</button></th>
							<td  align="left">----Tap----></th>
							<td  align="left">Select Scene All Off</th>
							<td  align="left">Press & Hdd -></th>
							<td  align="left">dim</th>							
						</tr>
						
						<tr class="editableRow">
							<td  align="left"><button id="wdseditscene-apply-btn" style="width: 50px;background:#A9A9A9;">Scene</button></th>
							<td  align="left">----Tap----></th>
							<td  align="left">Switch to next Scene</th>
							<td  align="left"></th>
							<td  align="left"></th>							
						</tr>
						
						<tr class="editableRow">
							<td  align="left"><button id="wdseditauto-apply-btn" style="width: 50px;background:#A9A9A9;">Auto</button></th>
							<td  align="left">----Tap----></th>
							<td  align="left">goto Auto Mode</th>
							<td  align="left"></th>
							<td  align="left"></th>							
						</tr>
</table>
</div>

</body>
</html>