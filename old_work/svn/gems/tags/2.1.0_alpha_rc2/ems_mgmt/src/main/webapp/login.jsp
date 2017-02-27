<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page session="false" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="ems" uri="/WEB-INF/tlds/ems_mgmt.tld"%>

<html>
<head>

<spring:url value="/themes/default/css/style.css" var="stylecss" />
<link rel="stylesheet" type="text/css" href="${stylecss}" />
<spring:url value="/scripts/jquery/jquery.1.6.4.min.js" var="jquery"></spring:url>
<script type="text/javascript" src="${jquery}"></script>
<spring:url value="/scripts/jquery/jquery.cookie.20110708.js" var="jquery_cookie"></spring:url>
<script type="text/javascript" src="${jquery_cookie}"></script>

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title><spring:message code="title"/></title>

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
	
</script>

</head>
<body>
	<div class="login">
		<div align="left">
			<img id="enlogo" src="themes/default/images/enlighted-logo.png" alt=""/>								
		</div>
		<div id='ver' align="center">
			<span id="elfgem"><spring:message code="header.title"/></span>
			<b><ems:showAppVersion/></b>
		</div>
		<div class="loginForm">
			<div class="loginHeader">
				<span><spring:message code="login.header.label"/></span>
			</div>
			<div class="errorMsg" id="error"></div>
			<c:if test="${param.error == 'true'}">
				<script>$('#error').html('<spring:message code="error.login.incorrect"/>');</script>
			</c:if>
			<c:if test="${param.sessionExpire == 'true'}">
				<script>$('#error').html('<spring:message code="error.session.expired"/>');</script>
			</c:if>
			<spring:url value="/j_spring_security_check" var="login_url" />
			<form method="post" name="loginForm" onsubmit="return validateForm();" action="${login_url}">
				<div>
					<span class="loginPrompt"><spring:message code="login.user.id.label"/></span>
					<input type="text" id="userNameTextBox" class="loginField" name="j_username"></input>
				</div>
				<div>
					<span class="loginPrompt dvpwd"><spring:message code="login.password.label"/></span>
					<input type="password" id="passwordTextBox" class="loginField" name="j_password"></input>
				</div>
				<div><input class="submitAction" type="submit" value='<spring:message code="login.submit"/>'/></div>
				<!--<div class="loginsignup"><input class="submitAction" type="submit" value='Signup'/></div>-->
			</form>
		</div>
	</div>
	<div class="footer footerlogin">
		<div class="footerinner"><spring:message code="footer.copyright.text"/></div>
	</div>
</body>
</html>
