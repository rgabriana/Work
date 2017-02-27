<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<script type="text/javascript" src="/ems/scripts/enlighted/em.js"></script>
<spring:url value="/themes/default/css/style.css" var="stylecss" />
<link rel="stylesheet" type="text/css" href="${stylecss}" />
<spring:url value="/themes/default/images" var="imageDir" />
<spring:url	value="/j_spring_security_logout"	var="loginPage" />
<spring:url value="/users/resetUserPassword.ems" var="actionURL" scope="request" />
<spring:url value="/scripts/jquery/jquery.1.6.4.min.js" var="jquery"></spring:url>
<script type="text/javascript" src="${jquery}"></script>
<spring:url value="/scripts/jquery/jquery.validate.1.9.min.js" var="jqueryvalidate"></spring:url>
<script type="text/javascript" src="${jqueryvalidate}"></script>
<spring:url value="/scripts/jquery/jquery.layout-latest.js"	var="jquerylayout"></spring:url>
<script type="text/javascript" src="${jquerylayout}"></script>

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
	#resetPassword{
		text-align:center;margin-top:20px; margin-left: auto;
									    margin-right: auto;
									    width: 50%;
		margin: 0;
		z-index: 1;
	    position: absolute;
	    top: 50%;
	    left: 50%;
	    margin-right: -50%;
	    transform: translate(-50%, -50%) 
	}   
	#resetMain{
			background: url(${imageDir}/background-big.jpg) #f4f4f4 fixed no-repeat center top; 
			z-index: -1;
			width:100%;
			height:100%;
	}
	#saveUserBtn{
		margin-top:20px;
	}
	.tologinPage{
		clear: both; float: right;
		text-align:right; margin-left: auto;
									    margin-right: 0px;
									    width: 50%;
	}
	.ui-layout-center,.ui-layout-south{
		background:white!important;
	}
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
		<c:choose>
			<c:when test="${resultSuccess == 'true'}">
				$("#tableDiv").hide();
				$('#commMsg').html('<spring:message code="success.password.reset"/>');
			</c:when>
			<c:when test="${resultSuccess == 'false'}">
				$("#tableDiv").hide();
				$('#error').html('<spring:message code="error.password.reset"/>');
			</c:when>
			<c:otherwise>
			</c:otherwise>
		</c:choose>
		
		$( "#password" ).keydown(function() {
			validateInput();
		});
		$( "#confirmPassword" ).keydown(function() {
			validateInput();
		});
	});
</script>

<script type="text/javascript">

function validateInput(){
	$("#update_user").validate(changePasswordValidatorObj);
}
function saveUser(){
	$("#update_user").validate(changePasswordValidatorObj);
	//if(validateAndSaveUser() == true){
		if($("#update_user").valid()){
			$("#update_user").submit();
		}
	//}
}

</script>
<div id="resetMain">
		<div align="left">
			<img id="enlogo" src="${imageDir}/enlighted-logo.png"
				alt="" />
		</div>
<div id="resetPassword">
<div class="errorMsg" id="error"></div>
<div class="commMsg" id="commMsg"></div>

<c:choose>
	<c:when test="${resultSuccess == 'true'}">
		<% request.getSession().invalidate(); %>
		<div class="tologinPage">
			<a href="${loginPage}"> Go to Login Page</a>
		</div>
	</c:when>
	<c:when test="${resultSuccess == 'false'}">
		<% request.getSession().invalidate(); %>
		<div class="tologinPage">
			<a href="${loginPage}"> Go to Login Page</a>
		</div>
	</c:when>
	<c:otherwise>
		<form:form id="update_user" commandName="user" method="post" 
			action="${actionURL}">
			<form:hidden path="id" />
			<div id="tableDiv">
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
						value="<spring:message code="action.reset" />" onclick="saveUser();">&nbsp;
						<%-- <input type="button" id="btnClose"
						value="<spring:message code="action.cancel" />" onclick="closeUnlockUserDialog()"> --%>	
					</td>
				</tr>
			</table>
			</div>
			<div class="tologinPage">
					<a href="${loginPage}"> Go to Login Page</a>
			</div>
		</form:form>
	</c:otherwise>
</c:choose>	

		
	

</div>


</div>


