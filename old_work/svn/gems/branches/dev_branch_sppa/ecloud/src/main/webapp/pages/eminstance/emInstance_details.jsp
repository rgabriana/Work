<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<script type="text/javascript">
	
	function saveEmInstance(){
			$("#create_emInstance").submit();
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
				<td class="fieldLabel">Name</td>
				<td class="fieldValue"><form:input class="inputField" id="name" name="name" size="40" path="name" />
			</tr>
			<tr>
				<td class="fieldLabel">Version</span></td>
				<td class="fieldValue"><input class="inputField" id="version"
					name="version" size="40" /></td>
			</tr>
			<tr>
				<td class="fieldLabel">Mac Id</span></td>
				<td class="fieldValue"><input class="inputField" id="macId"
					name="macId" size="40" /></td>
			</tr>
			<tr>
				<td class="fieldLabel">Time Zone</td>
				<td class="fieldValue"><form:input class="inputField" id="timeZone" name="timeZone" size="40"
						path="timeZone" /></td>
			</tr>
			<tr>
				<td class="fieldLabel">Database Name</td>
				<td class="fieldValue"><form:input class="inputField" id="databaseName" name="databaseName" size="40"
						path="databaseName" /></td>
			</tr>
			<tr>
				<td />
				<td><input id="saveUserBtn" type="button"
					value="<spring:message code="action.save" />" onclick="saveEmInstance();">&nbsp;
					<input type="button" id="btnClose"
					value="<spring:message code="action.cancel" />" onclick="closeDialog()">	
				</td>
			</tr>
		</table>
	</form:form>
</div>



