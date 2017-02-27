<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<style>
	#activate_emInstance{padding:10px 15px;}
	#activate_emInstance table{width:100%;}
	#activate_emInstance td{padding-bottom:3px;}
	#activate_emInstance td.fieldLabel{width:40%; font-weight:bold;}
	#activate_emInstance td.fieldValue{width:60%;}
	#activate_emInstance .inputField{width:100%; height:20px;}
	#activate_emInstance #saveBtn{padding: 0 10px;}
	#activate_emInstance .M_M{display: none;}
	#activate_emInstance .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}    
</style>

<script type="text/javascript">

$(document).ready(function(){
	
	$("#activate_emInstance").validate({
		rules: {
			name: {
				required: true,
			},
			version: {
				required: true,
			},	
			macId: {
				required: true,
			},
			contactName: {
				required: true,
			},
			contactEmail: {
				required: true,email: true,
			},
			contactPhone: {
				required: true,
			},
			address: {
				required: true,
			}
		},
		messages: {
			name: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			version: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			macId: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			contactName: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			contactEmail: {
				required: '<spring:message code="error.valid.email.required"/>',
			},
			contactPhone: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			address: {
				required: '<spring:message code="error.above.field.required"/>',
			}
		}
	});
		
});

	$("#customerCombo").empty();
	<c:forEach items="${customerList}" var="customer">
		$('#customerCombo').append($('<option></option>').val("${customer.id}").html("${customer.name}"));
	</c:forEach>
	
	function activateEmInstance(){
		   var selectedCustomerId = $("#customerCombo").val();
		   $("#customerId").val(selectedCustomerId);
		   $("#activate_emInstance").submit();
	}
	
	function closeDialog(){
		$("#emActivateDialog").dialog("close");
	}
	
</script>

<div>
	<spring:url value="/eminstance/activateEm.ems" var="actionURL" scope="request" />
	<form:form id="activate_emInstance" commandName="emInstance" method="post" 
		action="${actionURL}">
		<form:hidden path="id" />
		<input type="hidden" id="customerId" name="customerId" />
		<table>
			<tr>
				<td class="fieldLabel">Name*</td>
				<td class="fieldValue"><form:input class="inputField" id="name" name="name" size="40" path="name" />
			</tr>
			<tr>
				<td class="fieldLabel">Version*</td>
				<td class="fieldValue"><form:input class="inputField" id="version"
					name="version" size="40" path="version" readonly="true" /></td>
			</tr>
			<tr>
				<td class="fieldLabel">Mac Id*</td>
				<td class="fieldValue"><form:input class="inputField" id="macId"
					name="macId" size="40" path="macId" readonly="true"/></td>
			</tr>
			
			<tr>
			<td class="fieldLabel">Contact Name*</td>
			<td class="fieldValue"><form:input class="inputField" id="contactName" 
					name="contactName" size="40" path="contactName" />
			</tr>
			<tr>
				<td class="fieldLabel">Contact Email*</td>
				<td class="fieldValue"><form:input class="inputField" id="contactEmail"
					name="contactEmail" size="40" path="contactEmail"/></td>
			</tr>
			<tr>
				<td class="fieldLabel">EM Address*</td>
				<td class="fieldValue"><form:input class="inputField" id="address"
					name="address" size="40" path="address"/></td>
			</tr>
			<tr>
				<td class="fieldLabel">Contact PhoneNo*</td>
				<td class="fieldValue"><form:input class="inputField" id="contactPhone"
					name="contactPhone" size="40" path="contactPhone"/></td>
			</tr>
			
			<tr>
				<td class="fieldLabel">Select Customer*</td>
				<td class="fieldValue"><select id="customerCombo"></select></td>
			</tr>
			<!--
			<tr>
				<td class="fieldLabel">Replica Server*</td>
				<td class="fieldValue">
				 <form:select path="replicaServer.id">
                    <form:options items="${replicaServerCollection}" itemValue="id" itemLabel="ip"/>
                 </form:select></td>
			</tr>
			-->
			<tr>
				<td />
				<td><input id="saveBtn" type="button"
					value="Activate" onclick="activateEmInstance();">&nbsp;
					<input type="button" id="btnClose"
					value="<spring:message code="action.cancel" />" onclick="closeDialog()">	
				</td>
			</tr>
		</table>
	</form:form>
</div>