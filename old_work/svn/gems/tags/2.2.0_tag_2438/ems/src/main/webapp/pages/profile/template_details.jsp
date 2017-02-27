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
</style>

<script type="text/javascript">
var requirederr = '<spring:message code="error.above.field.required"/>';
$("#create_template").validate({
	rules: {
		name: "required",
		validDomain: "required",
		
	},
	messages: {
		name: requirederr,
		validDomain: requirederr,
	}
});


function saveTemplate(){
	$("#create_template").attr("action",'<spring:url value="/profileTemplateManagement/save.ems" />');
	$("#create_template").submit();
	
};

function closeTemplate(){
	$("#templateMgmtDialog").dialog('close');
	
}

</script>
<div>
			
	<form:form id="create_template" commandName="profileTemplate" method="post">
        <form:hidden path="id"/>
		<table>
			<tr>
				<td class="fieldLabel"><spring:message code="profiletemplate.name" />*</td>
				<td class="fieldValue"><form:input class="inputField" id="name" name="name" path="name" /></td>
			</tr>
			<tr>
			<td style="height: 5px;"> </td>
			</tr>
			<tr>
				<!-- <td /> -->
				<td colspan="2" align="center">
					<input type="button" id="saveTemplateBtn"
					value="<spring:message code="action.save" />" onclick="saveTemplate()">
					
					<input type="button" id="closeTemplateBtn"
					value="<spring:message code="action.cancel" />" onclick="closeTemplate()">
				</td>
			</tr>
		</table>
	</form:form>
</div>


