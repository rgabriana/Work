<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>
<html>
<head>
<spring:url value="/themes/default/css/style.css" var="stylecss" />
<link rel="stylesheet" type="text/css" href="${stylecss}" />

<style type="text/css">
.disableProceedButton
{
	background: #CCCCCC !important;
	border-color :#CCCCCC !important;
	color:#FFFFFF; font-size:13px; clear: both; float: left; margin: 20px 10px; width:70px;height:35px;border:0px;font-weight:bold;
}
.enableProceedButton
{
	color:#FFFFFF;background-color:#5a5a5a; font-size:13px; clear: both; float: left; margin: 20px 10px; width:70px;height:35px;border:0px;font-weight:bold;
}
</style>
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
var isTermAndConditionChecked = false;

function termAndConditionsOpend()
{
	isTermAndConditionChecked = true;
	$('input:submit').removeAttr("disabled");
	$('input:submit').removeClass("disableProceedButton");	
	$('input:submit').addClass("enableProceedButton");	
}
function validateForm(){
	$('#errorMsg').html('');
	if (!isTermAndConditionChecked) {
		$('#error').html('<spring:message code="error.termAndCondition.required"/>');
		return false;
	}
	return true;
}
</script>
<script type="text/javascript">
	$().ready(function() {
		$(function() {
			$(window).resize(function() {
				var setSize = $(window).height();
				setSize = setSize - 30;
				$(".login").css("height", setSize);
			});
		});
		$(".login").css("height", $(window).height() - 30);
		
		$('input:submit').attr("disabled", true);
		$('input:submit').addClass("disableProceedButton");	
	});

	$(document).ready(function() {
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
			<img id="enlogo" src="themes/default/images/enlighted-logo.png"	alt="" />
		</div>
		<div class="termAndConditionForm">
			<div class="errorMsg" id="error"></div>
			<spring:url value="/acceptTermsAndConditions.ems" var="acceptTermsURL" />
			<spring:url value="/doc/EULA.pdf" var="eula" />
			<form method="post" name="termAndConditionForm"
				onsubmit="return validateForm();" action="${acceptTermsURL}">
				<div class="termAndConditionHeader">
						<span><spring:message code="termandconditions.header.label" /></span>
				</div>
				<div class="termAndConditionInfo">
					<span>To proceed, Please <a href="${eula}" target="_new" onclick="termAndConditionsOpend()" id="clickHere">click here</a> to read "Terms & Conditions"</span>
				</div>
				<div class="termAndConditionSignUp">
					<input id="acceptTerms" type="submit"
						value='<spring:message code="termandconditions.proceed"/>' />
				</div>
			</form>
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
