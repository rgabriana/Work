<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<style>
	#update_user{padding:10px 15px;}
	#update_user table{width:100%;}
	#update_user td{padding-bottom:3px;}
	#update_user td.fieldLabel{width:40%; font-weight:bold;}
	#update_user td.fieldValue{width:60%;}
	#update_user .inputField{width:100%; height:20px;}
	#update_user #saveUserBtn{padding: 0 10px;}
	#update_user .M_M{display: none;}
	#update_user .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}    
</style>

<script type="text/javascript">
var changePasswordValidatorObj;
	$(document).ready(function(){
		var requiredErr = '<spring:message code="error.above.field.required"/>';
		changePasswordValidatorObj = {
				rules: {
					password: {required: true,minlength : 8, passwordValidate:true},
					confirmPassword: {required: true, equalTo: "#password"}
				},
				messages: {
					password: {required: requiredErr, minlength: '<spring:message code="error.password.length"/>', passwordValidate: '<spring:message code="error.passwords.policy"/>' },
					confirmPassword: {required: requiredErr, equalTo: '<spring:message code="error.passwords.not.match"/>'}
				}
			};
			
		
		
	});
</script>

<script type="text/javascript">


function saveUser(){
	$("#update_user").validate(changePasswordValidatorObj);
	//if(validateAndSaveUser() == true){
		if($("#update_user").valid()){
			$("#update_user").submit();
		}
	//}
}

</script>

<div>
	<spring:url value="/users/unlockUser.ems" var="actionURL" scope="request" />
	<form:form id="update_user" commandName="user" method="post" 
		action="${actionURL}">
		<form:hidden path="id" />
		<table>
			<tr>
				<td class="fieldLabel"><spring:message code="users.password" /><span class="M_M">*</span></td>
				<td class="fieldValue"><input class="inputField" type="password" id="password"
					name="password" size="40" /></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="users.confirmPassword" /><span class="M_M">*</span></td>
				<td class="fieldValue"><input class="inputField" type="password" id="confirmPassword"
					name="confirmPassword" size="40" /></td>
			</tr>
			<tr>
				<td />
				<td><input id="saveUserBtn" type="button"
					value="<spring:message code="action.unlock" />" onclick="saveUser();">&nbsp;
					<input type="button" id="btnClose"
					value="<spring:message code="action.cancel" />" onclick="closeUnlockUserDialog()">	
				</td>
			</tr>
		</table>
	</form:form>
</div>



