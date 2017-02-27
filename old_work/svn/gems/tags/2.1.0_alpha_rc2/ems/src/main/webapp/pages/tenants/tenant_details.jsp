<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<style>
	#create_tenant{padding:10px 15px;}
	#create_tenant table{width:100%;}
	#create_tenant td{ padding-bottom:3px;}
	#create_tenant td.fieldLabel{width:35%; font-weight:bold;}
	#create_tenant td.fieldValue{width:65%;}
	#create_tenant .inputField{width:100%; height:100%;}
	#create_tenant #saveTenantBtn{padding: 0 10px;}
	#create_tenant #saveTenantAssignFacilityBtn{padding: 0 10px;}
	#create_tenant #closeTenantBtn{padding: 0 10px;}
</style>

<script type="text/javascript">
var requirederr = '<spring:message code="error.above.field.required"/>';
$("#create_tenant").validate({
	rules: {
		name: "required",
		validDomain: "required",
		email: {
			required: true,
			email: true
		},
		phoneNo: "required"
	},
	messages: {
		name: requirederr,
		validDomain: requirederr,
		email: {
			required: requirederr,
			email: '<spring:message code="error.valid.email.required"/>'
		},
		phoneNo: requirederr
	}
});


function saveTenant(){
	$("#create_tenant").attr("action",'<spring:url value="/tenants/save.ems" />');
	$("#create_tenant").submit();
	
};

function saveTenantAssignFacility(){
	
	$("#create_tenant").attr("action",'<spring:url value="/tenants/saveAssignFacility.ems" />');
	$("#create_tenant").submit();
	
};

function closeTenant(){
	$("#tenantDetailDialog").dialog('close');
	
}



</script>

<div>
			
	<form:form id="create_tenant" commandName="tenant" method="post"
		>
        <form:hidden path="id"/>
		<table>
			<tr>
				<td class="fieldLabel"><spring:message code="tenant.name" />*</td>
				<td class="fieldValue"><form:input class="inputField" id="name" name="name" path="name" /></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="tenant.validDomain" />*</td>
				<td class="fieldValue"><form:input class="inputField" id="validDomain" name="validDomain" path="validDomain" /></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="tenant.email" />*</td>
				<td class="fieldValue"><form:input class="inputField" id="email" name="email" path="email" /></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="tenant.phoneNo" />*</td>
				<td class="fieldValue"><form:input class="inputField" id="phoneNo" name="phoneNo" path="phoneNo" /></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="tenant.address" /></td>
				<td class="fieldValue"><form:input class="inputField" id="address" name="address" path="address" /></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="tenant.status" /></td>
				<td class="fieldValue"><form:select class="inputField" path="status">
						<form:options items="${statusList}" itemValue="name"
							itemLabel="name" />
					</form:select></td>
			</tr>
			<tr>
				<!-- <td /> -->
				<td colspan="2" align="center">
					<input type="button" id="saveTenantBtn"
					value="<spring:message code="action.save" />" onclick="saveTenant()">
					<input type="button" id="saveTenantAssignFacilityBtn"
					value="<spring:message code="action.saveAssignFacility" />" onclick="saveTenantAssignFacility()">
					<input type="button" id="closeTenantBtn"
					value="<spring:message code="action.cancel" />" onclick="closeTenant()">
				</td>
			</tr>
		</table>
	</form:form>
</div>


