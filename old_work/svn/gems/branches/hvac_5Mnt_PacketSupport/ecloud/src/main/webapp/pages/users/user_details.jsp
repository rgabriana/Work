<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<style>
	#create_user{padding:10px 15px;}
	#create_user table{width:100%;}
	#create_user td{padding-bottom:3px;}
	#create_user td.fieldLabel{width:40%; font-weight:bold;}
	#create_user td.fieldValue{width:60%;}
	#create_user .inputField{width:100%; height:20px;}
	#create_user #saveUserBtn{padding: 0 10px;}
	#create_user .M_M{display: none;}
	#create_user .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}    
</style>

<script type="text/javascript">
	var oldusername='';
	$(document).ready(function(){
		oldusername = $("#email").val();		
	});
	
	jQuery.validator.addMethod("noSpace", function(value, element) { 
		  return value.indexOf(' ') < 0 ; 
		}, "Space character is not allowed");
	
</script>

<script type="text/javascript">
	//Add user form validation 
	var requirederr = '<spring:message code="error.above.field.required"/>';
	
	var requiredUserNameErr = '<spring:message code="error.username.required"/>';
	
	function getUserName(){
		var chkusername = $("#email").val();
		var returnresult = false;
		
		$.ajax({
			type: "GET",
			cache: false,
			url: '<spring:url value="/services/org/users/list/"/>'+ chkusername,
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
			email: { required: requiredUserNameErr, email: '<spring:message code="error.valid.email.required"/>' },
			confirmPassword: { equalTo: '<spring:message code="error.passwords.not.match"/>' }
		}
		
	};
	
	//Make password and confirm password fields mandatory while creating new User  
	var userID = "${user.id}";
	if(userID == ""){ //create new user
		createUserValidatorObj.rules.password={required: true , noSpace: true}
		createUserValidatorObj.rules.confirmPassword={required: true, equalTo: "#password" , noSpace: true }
		createUserValidatorObj.messages.password={required: requirederr}
		createUserValidatorObj.messages.confirmPassword={required: requirederr, equalTo: '<spring:message code="error.passwords.not.match"/>'}
		$("#create_user .M_M").css("display", "inline"); //Show M_M: Mandatory mark
	}
	
	$("#create_user").validate(createUserValidatorObj);
	
	function clearMessage(){
		 
		 $("#domainMsg").text("");
		 $("#email").removeClass("invalidField");
		
	 }
	
	function validateAndSaveUser(){		
		var username = $("#email").val();
		
		$("#domainMsg").text("");
		
		if(username == ""){
			$("#email").addClass("invalidField");
			return false;
		}
		
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
		return true;
	}
	
	function saveUser(){
		if(validateAndSaveUser() == true){
			$("#create_user").submit();
		}
	}
	
	function closeUserDetailsDialog(){
		$('#userDetailsDialog').dialog("close");
	}
	
</script>

<div>
<spring:url value="/users/save.ems" var="actionURL" scope="request" />
	<form:form id="create_user" commandName="user" method="post" 
		action="${actionURL}">
		<form:hidden path="id" />	
		<table>
			<tr>
				<td class="fieldLabel">Email</td>
				<td class="fieldValue"><form:input class="inputField" id="email" name="email" size="40" path="email"/><span id="domainMsg" class="error"></span></td>
			</tr>
			<tr>
				<td class="fieldLabel">Password*</span></td>
				<td class="fieldValue"><input class="inputField" type="password" id="password"
					name="password" size="40" /></td>
			</tr>
			<tr>
				<td class="fieldLabel">Confirm Password*</span></td>
				<td class="fieldValue"><input class="inputField" type="password" id="confirmPassword"
					name="confirmPassword" size="40" /></td>
			</tr>
			<tr>
				<td class="fieldLabel">First Name</td>
				<td class="fieldValue"><form:input class="inputField" id="firstName" name="firstName" size="40"
						path="firstName" /></td>
			</tr>
			<tr>
				<td class="fieldLabel">LastName</td>
				<td class="fieldValue"><form:input class="inputField" id="lastName" name="lastName" size="40"
						path="lastName" /></td>
			</tr>			
			<tr>
				<td class="fieldLabel">Roles</td>
				<td class="fieldValue"><form:select class="inputField" path="roleType">
						<form:options items="${roles}" itemValue="name" itemLabel="name" />
					</form:select></td>
			</tr>
			<tr>
				<td class="fieldLabel">User Status</td>
				<td class="fieldValue"><form:select class="inputField" id="status" path="status">
						<form:options items="${statusList}" itemLabel="label" />
					</form:select></td>
			</tr>
			<tr>
				<td />
				<td><input id="saveUserBtn" type="button"
					value="<spring:message code="action.save" />" onclick="saveUser();">&nbsp;
					<input type="button" id="btnClose"
					value="<spring:message code="action.cancel" />" onclick="closeUserDetailsDialog();">	
				</td>
			</tr>
		</table>
	</form:form>
</div>