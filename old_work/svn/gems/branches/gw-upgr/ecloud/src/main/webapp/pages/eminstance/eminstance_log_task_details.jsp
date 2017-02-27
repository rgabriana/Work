<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<style>
	#scheduleLogTask_emInstance {padding:10px 15px;}
	#scheduleLogTask_emInstance table{width:100%;}
	#scheduleLogTask_emInstance td{padding-bottom:3px;}
	#scheduleLogTask_emInstance td.fieldLabel{width:40%; font-weight:bold;}
	#scheduleLogTask_emInstance td.fieldValue{width:60%;}
	#scheduleLogTask_emInstance .inputField{width:100%; height:20px;}
	#scheduleLogTask_emInstance #saveBtn{padding: 0 10px;}
	#scheduleLogTask_emInstance .M_M{display: none;}
	#scheduleLogTask_emInstance .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}    
</style>

<script type="text/javascript">
	
	
</script>

<div>
	<spring:url value="/emtasks/saveEmLogTask.ems" var="actionURL" scope="request" />
	<form:form id="scheduleLogTask_emInstance" commandName="emtask" method="post" action="${actionURL}">
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
			<%-- <tr>
				<td class="fieldLabel">Start Time</td>
				<td class="fieldValue"><form:input id="contact" path="startTime" name="startTime" ></form:input></td>
			</tr> --%>
			<tr>
				<td class="fieldLabel">Priority*</td>
				<td class="fieldValue">
					<form:select name="priority" path="priority" id="priority">
						<form:options items="${priorityTypeList}"/>
					</form:select>
				</td>
			</tr>
			<tr>
				 <td class="fieldLabel">Log Name*</td>
				<td class="fieldValue">
					<form:select name="lagNameSelect" path="logNameParameters" id="logNameParameters">
						<form:options items="${logEnumsList}"/>
					</form:select>
				</td> 
			</tr> 
			<tr>
				<td class="fieldLabel">Log Type*</td>
				<td class="fieldValue">
					<form:select name="logTypeSelect" path="logTypeParameters" id="logTypeParameters">
						<form:options items="${typeOfUploadList}"/>
					</form:select>
				</td>
			</tr>
			<tr>
				<td />
				<td>
					<input id="saveBtn" type="submit" value="Schedule Log Task">
				</td>
			</tr>
		</table>
	</form:form>
</div>