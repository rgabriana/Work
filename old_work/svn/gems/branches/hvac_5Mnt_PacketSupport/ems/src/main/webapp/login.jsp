<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<html>
<head>

<spring:url value="/themes/default/images/enlighted-logo.png" var="logoUrl"/>
<spring:url value="/themes/default/css/style.css" var="stylecss" />
<link rel="stylesheet" type="text/css" href="${stylecss}" />

<spring:url
	value="/themes/standard/css/jquery/jquery-ui-1.8.16.custom.css"
	var="jquerycss" />
<link rel="stylesheet" type="text/css" href="${jquerycss}" />

<spring:url value="/scripts/jquery/jquery.1.6.4.min.js" var="jquery"></spring:url>
<script type="text/javascript" src="${jquery}"></script>
<spring:url value="/scripts/jquery/jquery.cookie.20110708.js"
	var="jquery_cookie"></spring:url>
<script type="text/javascript" src="${jquery_cookie}"></script>

<spring:url value="/scripts/jquery/jquery.ui.1.8.16.custom.min.js"
	var="jqueryui"></spring:url>
<script type="text/javascript" src="${jqueryui}"></script>

<spring:url value="/scripts/standard/flash_detect_min.js"
	var="flash_detect_url"></spring:url>
<script type="text/javascript" src="${flash_detect_url}"></script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title><spring:message code="title" /></title>

<script type="text/javascript">	
	function validateForm(){
		$('#errorMsg').html('');
		var username = $('#userNameTextBox').val();
		if (username == null || username == "") {
			$('#error').html('<spring:message code="error.user.required"/>');
			return false;
		}

		var password = $('#passwordTextBox').val();
		if (password == null || password == "") {
			$('#error').html('<spring:message code="error.password.required"/>');
			return false;
		}

		//clear the cookie, which holds last menu selected.
		$.cookie('last_menu_cookie', null, {path: '/'});
		return true;
	}

	$().ready(function() {
		$(function() {
			$(window).resize(function() {
				var setSize = $(window).height();
				setSize = setSize - 30;
				$(".login").css("height", setSize);
			});
		});
		$(".login").css("height", $(window).height() - 30);
	});
</script>

<script type="text/javascript">

function delayResponseClientSide(){
	$("#notesDialog").hide();
	var time = '${defaultDelayTimeInMillis}';
	if(time == null || time == undefined || time == ''){
		time = 1;
	}
	$('#error').hide();
	var start_time = new Date();
	while(true){
		var now = new Date();
		if((now - start_time) > time){
			break;
		};
	}
	$('#error').show();
}
	$(document).ready(function() {
		//delayResponseClientSide();
		//define configurations for dialog
		var dialogOptions = {
			title : "Enlighted Energy Manager",
			modal : true,
			autoOpen : false,
			height : 300,
			width : 500,
			draggable : true
		}

		$("#notesDialog").dialog(dialogOptions);

		$('#contactUs').click(function() {
			$("#notesDialog").dialog('open');
			return false;
		});
		
		$('#userNameTextBox').focus();
		
		<c:if test="${param.loginAttempts == 'true'}">
			$(".errorMsg").css("height","25px");
		</c:if>
		<c:if test="${param.resetSuccess == 'true'}">
			$(".commMsg").css("height","25px");
		</c:if>
		<c:if test="${param.resetFail == 'true'}">
			$(".commMsg").css("height","25px");
		</c:if>
	});
	
</script>
</head>
<body>
	<div id="notesDialog">
		<table border="0" cellpadding="0" cellspacing="5">
			<tr>
				<td><b>Corporate Headquarters:</b></td>
			</tr>
			<tr>
				<td>930 Benecia Avenue<br />
					Sunnyvale, CA 94085<br /> Phone: 650.964.1094<br /> Fax:
					650.964.1094<br /> Email: info@enlightedinc.com
				</td>
			</tr>
			<tr>
				<td style="padding-top:5px;"><b>For Sales:</b></td>
			</tr>
			<tr>
				<td>Email: sales@enlightedinc.com<br /> Phone:650.964.1155</td>
			</tr>
			<tr>
				<td style="padding-top:5px;"><b>For Customer Service:</b></td>
			</tr>
			<tr>
				<td>Email: support@enlightedinc.com<br />
					Phone: 650.964.1155 or 866-377-4111
				</td>
			</tr>		
		</table>
	</div>
	<div class="login">
		<div align="left">
			<img id="enlogo" src="${logoUrl}" alt="" />  
		</div>
		<div class="errorMsg" id='flash_detection' align="center"></div>
		<script type="text/javascript">
			if (!FlashDetect.installed) {
				$('#flash_detection').html(
						'Flash is required to work with this application. Please <a href="http://www.adobe.com/products/flashplayer.html" target="blank">Install</a>');
			} else {
				if (!FlashDetect.versionAtLeast(10,3,183)) {
					$('#flash_detection').html(
						'Flash player of version 10.3.183 or higher is required to work with the application. Please <a href="http://www.adobe.com/products/flashplayer.html" target="blank">Update</a>');
				}
			}
		</script>

		<div id='ver' align="center">
			<b><ems:showAppVersion /></b>
		</div>
		<div class="loginForm">
			<div class="loginHeader">
				<span><spring:message code="login.header.label" /></span>
			</div>
			<div class="errorMsg" id="error"></div>
			<div class="commMsg" id="commMsg"></div>
			<c:if test="${param.error == 'true'}">
				<script>$('#error').html('<spring:message code="error.login.incorrect"/>');</script>
			</c:if>
			<c:if test="${param.sessionExpire == 'true'}">
				<script>$('#error').html('<spring:message code="error.session.expired"/>');</script>
			</c:if>
			<c:if test="${param.inactive == 'true'}">
				<script>$('#error').html('<spring:message code="error.login.inactive"/>');</script>
			</c:if>
			<c:if test="${param.facilityNotAssigned == 'true'}">
				<script>$('#error').html('<spring:message code="error.login.facilityNotAssigned"/>');</script>
			</c:if>
			<c:if test="${param.certificateNotFound == 'true'}">
				<script>$('#error').html('<spring:message code="error.login.certificationNotFound"/>');</script>
			</c:if>
			<c:if test="${param.UnkownSecurityException == 'true'}">
				<script>$('#error').html('<spring:message code="error.login.UnkownSecurityException"/>');</script>
			</c:if>
			<c:if test="${param.loginAttempts == 'true'}">
				<script>$('#error').html('<spring:message code="error.login.LoginAttemptsException"/>');</script>
			</c:if>
			<c:if test="${param.resetSuccess == 'true'}">
				<script>$('#commMsg').html('<spring:message code="success.password.reset"/>');</script>
			</c:if>
			<c:if test="${param.resetFail == 'true'}">
				<script>$('#error').html('<spring:message code="error.password.reset"/>');</script>
			</c:if>
			<c:if test="${not empty loginerror}">
				<script>$('#error').html('${loginerror}');</script>
			</c:if>
			<spring:url value="/j_spring_security_check" var="login_url" />
			<form method="post" name="loginForm"
				onsubmit="return validateForm();" action="${login_url}" target="_top">
				
				<div>
					<span class="loginPrompt"><spring:message
							code="login.user.id.label" /></span> <input type="text"
						id="userNameTextBox" class="loginField" name="j_username"></input>
				</div>
				<div>
					<span class="loginPrompt dvpwd"><spring:message
							code="login.password.label" /></span> <input type="password"
						id="passwordTextBox" class="loginField" name="j_password"></input>
						
				</div>
				
				<div class="loginsignup">
				
					<input class="submitAction" id="loginButton" type="submit"
						value='<spring:message code="login.submit"/>' />
						
				</div>
				
				<div class="forgetpassowrdlink">
				<spring:url
						value="/forgotPassword.jsp"
						var="toForgotPassword" />
					<a id="forgotpassword" href="${toForgotPassword}" ><span><spring:message	code="login.forgotpassword.label"/></span></a>
				</div>
				
				<!-- <div class="loginsignup"><input class="submitAction" type="submit" value='Signup'/></div> -->
			</form>
			<script type="text/javascript">
		</script>
		</div>
	</div>
	<div class="footer footerlogin">
		<div style="float: left" class="footerinner">
			<spring:message code="footer.copyright.text" />
		</div>
		<div style="float: right" class="footerinner">
			<a id="contactUs" href=""><spring:message
					code="footer.contact.us.label" /></a>
		</div>
	</div>
</body>
</html>
