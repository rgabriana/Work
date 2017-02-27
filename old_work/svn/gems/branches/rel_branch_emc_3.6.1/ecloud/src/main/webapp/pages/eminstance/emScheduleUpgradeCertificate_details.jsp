<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<style>
	#scheduleUpgrade_emInstance {padding:10px 15px;}
	#scheduleUpgrade_emInstance table{width:100%;}
	#scheduleUpgrade_emInstance td{padding-bottom:3px;}
	#scheduleUpgrade_emInstance td.fieldLabel{width:40%; font-weight:bold;}
	#scheduleUpgrade_emInstance td.fieldValue{width:60%;}
	#scheduleUpgrade_emInstance .inputField{width:100%; height:20px;}
	#scheduleUpgrade_emInstance #saveBtn{padding: 0 10px;}
	#scheduleUpgrade_emInstance .M_M{display: none;}
	#scheduleUpgrade_emInstance .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}    
</style>

<script type="text/javascript">
	
	function closeScheduleUpgradeDialog(){
		$("#newEmTaskDialog").dialog("close");
	}
	
</script>

<div>
	<spring:url value="/emtasks/saveScheduleCertificateUpgrade.ems" var="actionURL" scope="request" />
	<form:form id="scheduleUpgrade_emInstance" commandName="emtask" method="post" action="${actionURL}">
		<form:hidden id="emInstanceId" path="emInstanceId"/>
		<table>
			<tr>
				<td class="fieldLabel">Name*</td>
				<td class="fieldValue">
					<span class="formValueSpan" id="emInstanceName">${emInstanceName}</span>
				</td>
			</tr>
			<tr>
				<td class="fieldLabel">Version*</td>
				<td class="fieldValue">
					<span class="formValueSpan" id="emInstanceVersion">${emInstanceVersion}</span>
				</td>
			</tr>			
			<tr>
				<td class="fieldLabel">Priority*</td>
				<td class="fieldValue">
					<form:select name="priority" path="priority" id="priority">
						<form:options items="${priorityTypeList}"/>
					</form:select>
				</td>
			</tr>	
			<tr>
				<td />
				<td>
					<input id="saveBtn" type="submit" value="Schedule Certificate Sync">
					&nbsp;
					<input type="button" id="btnClose" value="<spring:message code="action.cancel"/>" onclick="closeScheduleUpgradeDialog()">	
				</td>
			</tr>
		</table>
	</form:form>
</div>