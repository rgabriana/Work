<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<style>
	#create_emInstance{padding:10px 15px;}
	#create_emInstance table{width:100%;}
	#create_emInstance td{padding-bottom:3px;}
	#create_emInstance td.fieldLabel{width:40%; font-weight:bold;}
	#create_emInstance td.fieldValue{width:60%;}
	#create_emInstance .inputField{width:100%; height:20px;}
	#create_emInstance #saveBtn{padding: 0 10px;}
	#create_emInstance .M_M{display: none;}
	#create_emInstance .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}    
</style>

<script type="text/javascript">

$(document).ready(function(){
	
	$("#create_emInstance").validate({
		rules: {
			name: {
				required: true,
			},
			version: {
				required: true,
			},	
			macId: {
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
			}
		}
	});
});
	
	function saveEmInstance(){
			$("#create_emInstance").submit();
	}
	
	function closeDialog(){
		$("#emInstanceDetailsDialog").dialog("close");
	}
	
</script>

<div>
	<spring:url value="/eminstance/save.ems" var="actionURL" scope="request" />
	<form:form id="create_emInstance" commandName="emInstance" method="post" 
		action="${actionURL}">
		<form:hidden path="id" />
		<input type="hidden" id="customerId" name="customerId" value="${customerId}" />
		<table>
			<tr>
				<td class="fieldLabel">Name*</td>
				<td class="fieldValue"><form:input class="inputField" id="name" name="name" size="40" path="name" />
			</tr>
			<tr>
				<td class="fieldLabel">Version*</td>
				<td class="fieldValue"><form:input class="inputField" id="version"
					name="version" size="40" path="version"/></td>
			</tr>
			<tr>
				<td class="fieldLabel">Mac Id*</td>
				<td class="fieldValue"><form:input class="inputField" id="macId"
					name="macId" size="40" path="macId"/></td>
			</tr>
			
			<tr>
				<td />
				<td><input id="saveBtn" type="button"
					value="<spring:message code="action.save" />" onclick="saveEmInstance();">&nbsp;
					<input type="button" id="btnClose"
					value="<spring:message code="action.cancel" />" onclick="closeDialog()">	
				</td>
			</tr>
		</table>
	</form:form>
</div>



