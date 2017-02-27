<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<html>
<head>

<spring:url value="/services/org/public/nonsecure/forgotpassword/generate/supportkey" var="supportKeyUrl" scope="request" />
<spring:url value="/services/org/public/nonsecure/download" var="downloadToken" scope="request" />
<spring:url value="/services/org/public/nonsecure/upload" var="uploadPassword" scope="request" />
<spring:url	value="/j_spring_security_logout"	var="loginPage" />
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


function loginFormSubmit(){
	
	$('#errorMsg').html('');
	var username = $('#userNameTextBox').val();
	if (username == null || username == "") {
		$('#error').html('<spring:message code="error.user.required"/>');
		return false;
	}
	
	 var formData = new FormData($("#forgotPasswordFormId")[0]);
	 //formData.append('file',$("#file"));
	 //formData.append('email',$('#userNameTextBox').val());
     $.ajax({
         url: '${uploadPassword}',
         type: 'POST',
         data:  formData,
         async: false,
         cache: false,
         processData: false,
         contentType: false,
         success: function(data, textStatus, jqXHR)
         {
        	 	$("#passwordTextBox").val(data);
        		$("#loginForm").submit();
         },
         error: function (jqXHR, textStatus, errorThrown)
         {
         	//alert(jqXHR.responseText+"::textStatus::"+JSON.stringify(textStatus)+"::errorThrown::"+JSON.stringify(errorThrown));
         	$("#jUserName").val('');
         	 $("#passwordTextBox").val('');
         	$("#error").text(jqXHR.responseText);
         	$(".errorMsg").css("height","25px");
         	$(".forgotPasswordForm").height($(".forgotPasswordForm").height()+$(".errorMsg").height());
         }
     });
     
   
}
function validateFormAndResetPassword(){
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
	$("#jUserName").val($("#userNameTextBox").val());
	//clear the cookie, which holds last menu selected.
	$.cookie('last_menu_cookie', null, {path: '/'});
	return true;
}

function validateFormAndDownloadFile(){
	$('#errorMsg').html('');
	var username = $('#userNameTextBox').val();
	if (username == null || username == "") {
		$('#error').html('<spring:message code="error.user.required"/>');
		return false;
	}
	
	
    $.ajax({
        url: "${downloadToken}/"+username,
        type: 'GET',
        async: false,
        cache: false,
        contentType: null,
        success: function(data, textStatus, jqXHR)
        {
        	$("#error").text('');
        	document.location = "${downloadToken}/"+username;
        },
        error: function (jqXHR, textStatus, errorThrown)
        {
        	//alert(jqXHR.responseText+"::textStatus::"+JSON.stringify(textStatus)+"::errorThrown::"+JSON.stringify(errorThrown));
        	ajaxErrorHandler(jqXHR, textStatus, errorThrown);
        }
    });
	//document.location = "${downloadToken}/"+username;
	return true;
}
function ajaxErrorHandler(jqXHR, textStatus, errorThrown){
	$("#jUserName").val('');
	$('#userNameTextBox').attr("disabled", false);
	$("#resetloginDiv").hide();
	$("#error").text(jqXHR.responseText);
	$(".errorMsg").css("height","15px");
	$(".forgotPasswordForm").height($(".forgotPasswordForm").height());

}
	function validateForm(){
		$('#errorMsg').html('');
		var username = $('#userNameTextBox').val();
		if (username == null || username == "") {
			$('#error').html('<spring:message code="error.user.required"/>');
			return false;
		}

		//clear the cookie, which holds last menu selected.
		$.cookie('last_menu_cookie', null, {path: '/'});
        var formData = $("#forgotPasswordFormId").serialize();
	        $.ajax({
	            url: '${supportKeyUrl}',
	            type: 'POST',
	            data: formData,
	            async: false,
	            cache: false,
	            contentType: "application/json; charset=utf-8",
	            success: function(data, textStatus, jqXHR)
	            {
	                //data - response from server
	                $("#resetloginDiv").show();
	                $('#userNameTextBox').attr("disabled", true); 
	                $("#supportKey").val(data);
	                $("#loginButton").hide();
	        		$("#supportKeyId").show(); 
	        		$("#supportKeyMsg").html('<spring:message code="forgot.message" />');
	        		//$(".login").height($(".login").height()+$("#resetloginDiv").height());
	        		var temp = $('#resetloginDiv .loginPrompt').height() * 2;
					var temp1= $('#resetloginDiv .submitGenSuppKey').height() * 2;
	        		$(".forgotPasswordForm").height($("#supportKeyMsg").height()+$(".forgotPasswordForm").height()+$("#supportKey").height()+temp+temp1 );
	        		//$(".login").height($(".login").height()+temp+temp1);
	        		$("#jUserName").val($("#userNameTextBox").val());
	        		
	        		$("#error").text('');
	            },
	            error: function (jqXHR, textStatus, errorThrown)
	            {
	            	//alert(jqXHR.responseText+"::textStatus::"+JSON.stringify(textStatus)+"::errorThrown::"+JSON.stringify(errorThrown));
	            	ajaxErrorHandler(jqXHR, textStatus, errorThrown);
	            }
	        });
	        
		
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
	$(document).ready(function() {
		$("#resetloginDiv").hide();
		$("#passwordTextBox").val('');
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
			$('#commMsg').html('<spring:message code="success.password.reset"/>');
		</c:if>

			$(".forgotPasswordForm").height(350);
			$("#supportKey").val('');
			//$("#loginButton").show();
			$("#supportKeyId").hide();
			$("#supportKeyMsg").html('<spring:message code="forgot.message" />');
			$("#supportKeyMsgToken").html('<spring:message code="forgot.message.generate.token" />');
			$("#resetloginDiv").hide();
			$('#userNameTextBox').attr("disabled", false);
    		$(".forgotPasswordForm").height(350);
		<c:if test="${param.resetFail == 'true'}">
			$(".errorMsg").css("height","25px");
			$('#error').html('<spring:message code="error.password.reset.forgot"/>');
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
			<img id="enlogo" src="themes/default/images/enlighted-logo.png"
				alt="" />
		</div>
		<div class="errorMsg" id='flash_detection' align="center"></div>
		<div class="commMsg" id="commMsg"></div>
		<div id='ver' align="center">
			<b><ems:showAppVersion /></b>
		</div>
		<div class="forgotPasswordForm">
			<div class="loginHeader">
				<span><spring:message code="forgot.header.label" /></span>
			</div>
			<div class="errorMsg" id="error"></div>
			<form method="post" name="forgotPasswordForm" id="forgotPasswordFormId"
				action="" target="_top" enctype="multipart/form-data">
				
				<div id="supportKeyMsgToken">
				</div>
							
				<div>
					<span class="loginPrompt"><spring:message
							code="login.user.id.label" /></span> <input type="text"
						id="userNameTextBox" class="forgotloginField" name="email"></input>
				</div>
					
				<div class="generatesuppkey">
						<div id="supportKeyId">
							<span class="loginPrompt "><spring:message
								code="forgot.support" /></span> <textarea readonly 
								id="supportKey" class="supportKeyTextArea" name="supportkey"></textarea>
						</div>
						<div id="loginButton">
							<input class="submitGenSuppKey" id="loginButton1" type="button" onclick="return validateFormAndDownloadFile();"
								value='<spring:message code="forgot.submit"/>' />
							
							<div id="supportKeyMsg">
							</div>
							<div>
								<span class="loginPrompt"><spring:message
										code="forgot.file.label" /></span>  <input class="forgotfileField" id="file" type="file" name="file" />
							</div>
							
							<input class="submitGenSuppKey" id="loginButton1" type="button" onclick="return loginFormSubmit();;"
								value='<spring:message code="forgot.import"/>' />
							
							<div class="forgotTologinPage">
			  							<a href="${loginPage}"> Go to Login Page</a>
							</div>
						</div>
						
						
			  	</div>
			 </form>
			 
				<div id="resetloginDiv">
					
					<spring:url value="/j_spring_security_check?forgotpasssecurity=true" var="login_url" />
						<form method="post" id="loginForm" name="loginForm"
										onsubmit="return validateFormAndResetPassword();" action="${login_url}" target="_top">
							<input type="hidden"
								id="jUserName" class="loginField" name="j_username"></input>
						<span class="loginPrompt"><spring:message
								code="login.password.label" /></span> <input type="password"
							id="passwordTextBox" class="forgotloginField" name="j_password"></input>
						<div>
							<input class="submitGenSuppKey" id="loginButton" type="submit" 
								value='<spring:message code="forgot.submit.reset"/>' />
								<div class="forgotTologinPage">
			  							<a href="${loginPage}"> Go to Login Page</a>
								</div>
						</div>
					</form>
				</div> 
				
			  	
			
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
