<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<spring:url value="/services/org/user/changepassword" var="changePasswordUrl" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Change Password</title>

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

<style>
	#ucp-message-div {font-weight:bold; float: left; padding: 0 0 5px 0;}
	table#ucp-form-container {padding: 5px 15px; width:100%; }
	#ucp-form{width:100%; height:100%;}
 	table#ucp-form-container td.field-label {font-weight: bold;}
    table#ucp-form-container td.field-input input {height:24px; width: 100%;}
    
	table#ucp-form-container .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}
</style>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
</style>

<script type="text/javascript">

$(document).ready(function() {
	var requiredErr = '<spring:message code="error.above.field.required"/>';
	var changePasswordValidatorObj = {
			rules: {
				oldPassword: {required: true},
				newPassword: {required: true},
				confirmPassword: {required: true, equalTo: "#newPassword1"}
			},
			messages: {
				oldPassword: {required: requiredErr},
				newPassword: {required: requiredErr},
				confirmPassword: {required: requiredErr, equalTo: '<spring:message code="error.passwords.not.match"/>' }
			}
		};
		$("#ucp-form").validate(changePasswordValidatorObj);
		
		$('#ucp-form').submit(function(e){
		    e.preventDefault();
		    changePassword();
		});

});



function cancelChangePassword(){
}

function changePassword(){
	setChangePasswordMessage("", "black");
	
	var newPasswordStr = $("#newPassword1").val();
	if(newPasswordStr.indexOf(' ') > -1){
		$("#np_error_msg").text("Space character is not allowed.");
		$("#newPassword1").addClass("invalidField");
		return false;
	} else {
		$("#np_error_msg").text("");
		$("#newPassword1").removeClass("invalidField");
	}
	
	var confirmPasswordStr = $("#confirmPassword1").val();
	if(confirmPasswordStr.indexOf(' ') > -1){
		$("#cp_error_msg").text("Space character is not allowed.");
		$("#confirmPassword1").addClass("invalidField");
		return false;
	} else {
		$("#cp_error_msg").text("");
		$("#confirmPassword1").removeClass("invalidField");
	}
	
	
	var oldPassword = encodeURIComponent($('#oldPassword1').val());
	var newPassword = encodeURIComponent($('#newPassword1').val());
	
	
	if($("#ucp-form").valid()){
		$.ajax({
			type: 'POST',
			url: "${changePasswordUrl}",
			data: "<userPassword><oldPassword>"+oldPassword+"</oldPassword><newPassword>"+newPassword+"</newPassword></userPassword>",
			success: function(data){
				setChangePasswordMessage(data.msg, (data.status == 1 ? "red" : "green"));
			},
			dataType:"json",
			contentType: "application/xml; charset=utf-8",
		});
	}
}

function setChangePasswordMessage(msg, color){
	$("#ucp-message-div").css("color", color);
	$("#ucp-message-div").html(msg);
}
</script>
</head>
<body >

<form id="ucp-form" method="post" action="" >
<table id="ucp-form-container">
	<tr>
		<td width=35%></td>
		<td width=65%></td>
	</tr>
	<tr>
		<td colspan=2>
			<div id="ucp-message-div"></div>
		</td>
	</tr>
	<tr>
		<td class="field-label">Old Password* :</td>
		<td class="field-input"><input id="oldPassword1" type="password" name="oldPassword" type="text"/></td>
	</tr>
	<tr>
		<td class="field-label">New Password* :</td>
		<td class="field-input"><input id="newPassword1" type="password" name="newPassword" type="text"/><span id="np_error_msg" class="error"></span></td>
	</tr>
	<tr>
		<td class="field-label">Confirm Password* :</td>
		<td class="field-input"><input id="confirmPassword1" type="password" name="confirmPassword" type="text"/><span id="cp_error_msg" class="error"></span></td>
	</tr>
	<tr>
		<td id="ucp-button-container" align="right" colspan=2>
			<input type="submit" id="ucp-change-btn" value="Submit" />
		</td>
	</tr>
</table>
</form>
</body>
</html>