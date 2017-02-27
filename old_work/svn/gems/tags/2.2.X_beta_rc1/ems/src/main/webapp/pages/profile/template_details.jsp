<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<style>
	#create_template{padding:10px 15px;}
	#create_template table{width:100%;}
	#create_template td{ padding-bottom:3px;}
	#create_template td.fieldLabel{width:35%; font-weight:bold;}
	#create_template td.fieldValue{width:65%;}
	#create_template .inputField{width:100%; height:20px;}
	#create_template #saveTemplateBtn{padding: 0 10px;}
	#create_template #closeTemplateBtn{padding: 0 10px;}
	#create_template .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}
</style>

<script type="text/javascript">
var requirederr = '<spring:message code="error.above.field.required"/>';



var createUserValidatorObj = {
		rules: {
			name: { required: true},			
		},
		messages: {
			name: { required: requirederr }			
		}		
	};
$("#create_template").validate(createUserValidatorObj);

function validateTemplate()
{
	var chktemplatename = $("#name").val();
	var returnresult = false;	
	if(chktemplatename=="" || chktemplatename==" ")
	{		
		 clearMessageTemplate();
		 $("#errorMsg").text("Above field is required.");
		 $("#name").addClass("invalidField");
		return false;
	}	
	
	var invalidFormatStr = 'Template name must contain only letters, numbers, or underscore';
    var regExpStr = /^[a-z0-9\_\s]+$/i;
    if(regExpStr.test(chktemplatename) == false) {
    	$("#errorMsg").text(invalidFormatStr);
		$("#name").addClass("invalidField");
    	return false;
    }
    
	$.ajax({
		type: "GET",
		cache: false,
		url: '<spring:url value="/services/org/profiletemplate/duplicatecheck/"/>'+ chktemplatename,
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
		$("#errorMsg").text('<spring:message code="error.duplicate.profiletemplate"/>');
		$("#name").addClass("invalidField");
		return false;
	}	
	else {	
		clearMessageTemplate();
		saveTemplate();
	}
}

function saveTemplate(){
	//$("#create_template").attr("action",'<spring:url value="/profileTemplateManagement/save.ems" />');
	$("#create_template").submit();
	
};

function closeTemplate(){
	$("#templateMgmtDialog").dialog('close');
	
}

function clearMessageTemplate()
{		 
		 $("#errorMsg").text("");
		 $("#name").removeClass("invalidField");
}

</script>
<div>
	<spring:url value="/profileTemplateManagement/save.ems" var="actionURL" scope="request" />			
	<form:form id="create_template" commandName="profileTemplate" method="post" action="${actionURL}">
        <form:hidden path="id"/>
		<table>
			<tr>
				<td class="fieldLabel"><spring:message code="profiletemplate.name" />*</td>
				<td class="fieldValue"><form:input class="inputField" id="name" name="name" path="name" onkeypress="clearMessageTemplate();" onmousedown="clearMessageTemplate();"/><span id="errorMsg" class="error"></span></td>
			</tr>
			<tr>
			<td style="height: 5px;"> </td>
			</tr>
			<tr>
				<!-- <td /> -->
				<td colspan="2" align="center">
					<input type="button" id="saveTemplateBtn"
					value="<spring:message code="action.save" />" onclick="validateTemplate()">
					
					<input type="button" id="closeTemplateBtn"
					value="<spring:message code="action.cancel" />" onclick="closeTemplate()">
				</td>
			</tr>
		</table>
	</form:form>
</div>


