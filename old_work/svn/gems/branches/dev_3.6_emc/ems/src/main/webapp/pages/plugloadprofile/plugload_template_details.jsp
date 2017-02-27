<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<style>
	#create_plugload_template{padding:10px 15px;}
	#create_plugload_template table{width:100%;}
	#create_plugload_template td{ padding-bottom:3px;}
	#create_plugload_template td.fieldLabel{width:35%; font-weight:bold;}
	#create_plugload_template td.fieldValue{width:65%;}
	#create_plugload_template .inputField{width:100%; height:20px;}
	#create_plugload_template #savePlugloadTemplateBtn{padding: 0 10px;}
	#create_plugload_template #closePlugloadTemplateBtn{padding: 0 10px;}
	#create_plugload_template .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}
</style>

<script type="text/javascript">
var requirederr = '<spring:message code="error.above.field.required"/>';

var createPlugloadTemplateValidatorObj = {
		rules: {
			name: { required: true},			
		},
		messages: {
			name: { required: requirederr }			
		}		
	};
$("#create_plugload_template").validate(createPlugloadTemplateValidatorObj);

function validatePlugloadTemplate()
{
	var chktemplatename = $("#name").val();
	var returnresult = false;	
	if(chktemplatename=="" || chktemplatename==" ")
	{		
		 clearMessagePlugloadTemplate();
		 $("#plugloadTemplateerrorMsg").text("Above field is required.");
		 $("#name").addClass("invalidField");
		return false;
	}	
	
	var invalidFormatStr = 'Plugload Template name must contain only letters, numbers, or underscore';
    var regExpStr = /^[a-z0-9\_\s]+$/i;
    if(regExpStr.test(chktemplatename) == false) {
    	$("#plugloadTemplateerrorMsg").text(invalidFormatStr);
		$("#name").addClass("invalidField");
    	return false;
    }
    
	$.ajax({
		type: "GET",
		cache: false,
		url: '<spring:url value="/services/org/plugloadProfileTemplate/duplicatecheck/"/>'+ chktemplatename,
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
		clearMessagePlugloadTemplate();
		$("#plugloadTemplateerrorMsg").text('<spring:message code="error.duplicate.plugloadProfiletemplate"/>');
		$("#name").addClass("invalidField");
		return false;
	}	
	else {	
		clearMessagePlugloadTemplate();
		savePlugloadTemplate();
	}
}

function savePlugloadTemplate(){
	//$("#create_plugload_template").attr("action",'<spring:url value="/profileTemplateManagement/save.ems" />');
	$("#create_plugload_template").submit();
	
};

function closePlugloadTemplate(){
	$("#plugloadTemplateMgmtDialog").dialog('close');
	
}

function clearMessagePlugloadTemplate()
{		 
		 $("#plugloadTemplateerrorMsg").text("");
		 $("#name").removeClass("invalidField");
}
function disableEnterKey(evt)
{
	 var keyCode = evt ? (evt.which ? evt.which : evt.keyCode) : event.keyCode;
     if (keyCode == 13) {
          return false;
     }
}


</script>
<div>
	<spring:url value="/plugloadProfileTemplateManagement/save.ems" var="plugloadProfileTemplateManagementSaveURL" scope="request" />			
	<form:form id="create_plugload_template" commandName="plugloadProfileTemplate" method="post" action="${plugloadProfileTemplateManagementSaveURL}" onKeyPress="return disableEnterKey(event)">
        <form:hidden path="id"/>
		<table>
			<tr>
				<td class="fieldLabel"><spring:message code="plugloadProfileTemplate.name" />*</td>
				<td class="fieldValue"><form:input class="inputField" id="name" name="name" path="name" onkeypress="clearMessagePlugloadTemplate();" onmousedown="clearMessagePlugloadTemplate();"/><span id="plugloadTemplateerrorMsg" class="error"></span></td>
			</tr>
			<tr>
			<td style="height: 5px;"> </td>
			</tr>
			<tr>
				<!-- <td /> -->
				<td colspan="2" align="center">
					<input type="button" id="savePlugloadTemplateBtn"
					value="<spring:message code="action.save" />" onclick="validatePlugloadTemplate()">
					
					<input type="button" id="closePlugloadTemplateBtn"
					value="<spring:message code="action.cancel" />" onclick="closePlugloadTemplate()">
				</td>
			</tr>
		</table>
	</form:form>
</div>


