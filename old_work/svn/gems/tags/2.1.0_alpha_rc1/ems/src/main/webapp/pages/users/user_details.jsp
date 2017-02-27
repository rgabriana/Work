<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<style>
	#create_user{padding:10px 15px;}
	#create_user table{width:100%;}
	#create_user td{padding-bottom:3px;}
	#create_user td.fieldLabel{width:40%; font-weight:bold;}
	#create_user td.fieldValue{width:60%;}
	#create_user .inputField{width:100%; height:100%;}
	#create_user #saveUserBtn{padding: 0 10px;}
	#create_user .M_M{display: none;}
	#create_user .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}    
</style>

<script type="text/javascript">
	var oldusername='';
	$(document).ready(function(){
		oldusername = $("#email").val();		
	});
</script>

<script type="text/javascript">
	//Add user form validation 
	var requirederr = '<spring:message code="error.above.field.required"/>';
	
	function getUserName(){
		var chkusername = $("#email").val();
		var returnresult = false;
		
		$.ajax({
			type: "GET",
			cache: false,
			url: '<spring:url value="/services/org/user/list/"/>'+ chkusername,
			dataType: "text",
			async: false,
			success: function(msg) {
				var count = (msg).indexOf(chkusername);
				if(oldusername != chkusername && count > 0) {
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
		return returnresult;
	}

	var createUserValidatorObj = {
		rules: {
			email: { required: true, email: true},
			confirmPassword: { equalTo: "#password" }
		},
		messages: {
			email: { required: requirederr, email: '<spring:message code="error.valid.email.required"/>' },
			confirmPassword: { equalTo: '<spring:message code="error.passwords.not.match"/>' }
		}
	};
	
	//Make password and confirm password fields mandatory while creating new User  
	var userID = "${user.id}";
	if(userID == ""){ //create new user
		createUserValidatorObj.rules.password={required: true}
		createUserValidatorObj.rules.confirmPassword={required: true, equalTo: "#password"}
		createUserValidatorObj.messages.password={required: requirederr}
		createUserValidatorObj.messages.confirmPassword={required: requirederr, equalTo: '<spring:message code="error.passwords.not.match"/>'}
		$("#create_user .M_M").css("display", "inline"); //Show M_M: Mandatory mark
	}
	
	$("#create_user").validate(createUserValidatorObj);
	
	function validateAndSaveUser(){
		// Validate Domain Name
		var validDomain = "${validDomain}";
		var username = $("#email").val();
		var result = getUserName();
		
		if(!result){
			$("#domainMsg").text('<spring:message code="error.duplicate.username"/>');
			$("#email").addClass("invalidField");
			return false;
		}
		else {	
			$("#domainMsg").text("");
			$("#email").removeClass("invalidField");
		}
		
		if(validDomain!="" && username!=""){
			if(username.indexOf(validDomain) == -1){ //Mark username invalid
				$("#domainMsg").text("Contains invalid domain name, it should be \""+validDomain+"\".");
				$("#email").addClass("invalidField");
				return false;
			} else {
				
				$("#domainMsg").text("");
				$("#email").removeClass("invalidField");
			}
		}
		return true;
	}
</script>

<div>
	<spring:url value="/users/save.ems" var="actionURL" scope="request" />
	<form:form id="create_user" commandName="user" method="post" onsubmit="javascript: return validateAndSaveUser();"
		action="${actionURL}">
		<form:hidden path="id" />
		<input type="hidden" id="tenantId" name="tenantId" value="${tenantId}" />
		<table>
			<tr>
				<td class="fieldLabel"><spring:message code="users.email" />*</td>
				<td class="fieldValue"><form:input class="inputField" id="email" name="email" size="40" path="email" /><span id="domainMsg" class="error"></span></td>
			</tr>
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
				<td class="fieldLabel"><spring:message code="users.firstName" /></td>
				<td class="fieldValue"><form:input class="inputField" id="firstName" name="firstName" size="40"
						path="firstName" /></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="users.lastName" /></td>
				<td class="fieldValue"><form:input class="inputField" id="lastName" name="lastName" size="40"
						path="lastName" /></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="users.contact" /></td>
				<td class="fieldValue"><form:input class="inputField" id="contact" name="contact" size="40"
						path="contact" /></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="users.role" /></td>
				<td class="fieldValue"><form:select class="inputField" path="role.id">
						<form:options items="${roles}" itemValue="id" itemLabel="roleType" />
					</form:select></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="users.status" /></td>
				<td class="fieldValue"><form:select class="inputField" path="status">
						<form:options items="${statusList}" itemValue="name"
							itemLabel="name" />
					</form:select></td>
			</tr>
			<tr>
				<td />
				<td><input id="saveUserBtn" type="submit"
					value="<spring:message code="action.save" />">&nbsp;
					<input type="button" id="btnClose"
					value="<spring:message code="action.cancel" />" onclick="closeDialog()">	
				</td>
			</tr>
		</table>
	</form:form>
</div>


