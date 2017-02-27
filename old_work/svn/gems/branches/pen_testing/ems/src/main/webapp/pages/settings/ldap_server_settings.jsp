<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<style type="text/css">
	html body{margin:3px 0px 0px 0px !important; background: #ffffff !important; overflow: hidden !important;}
</style>
<script type="text/javascript"> 
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
$().ready(function(){
	$.validator.addMethod("ldapnameregx", function(value, element, regexpr) {          
		if (value != "") {
			return regexpr.test(value);
		}
		return true;
	}, "Please enter a valid Name.");
	
	$.validator.addMethod("baseDnsregx", function(value, element, regexpr) {          
		if (value != "") {
			return regexpr.test(value);
		}
		return true;
	}, "Please enter a valid Base DNS.");
	$.validator.addMethod("userAttregx", function(value, element, regexpr) {          
		if (value != "") {
			return regexpr.test(value);
		}
		return true;
	}, "Please enter a valid User Attribute.");
	$.validator.addMethod("nonAnoDnregx", function(value, element, regexpr) {          
		if (value != "") {
			return regexpr.test(value);
		}
		return true;
	}, "Please enter a valid DN.");
	$.validator.addMethod("nonAnoPsregx", function(value, element, regexpr) {          
		if (value != "") {
			return regexpr.test(value);
		}
		return true;
	}, "Please enter a valid Password.");
	
			$("#ldap-settings").validate({
				rules: {
					name:
					{
						required: true,
						ldapnameregx: /^[A-Za-z0-9\s\_\-\.\,\:\@\#\$\&\*]+$/,
						maxlength: 40
					},
					server:
					{
						required: true,
						validip: ""
					},
					port:
					{
						required: true,
						number: true
					},
					baseDns: {
						required: true,
						baseDnsregx: /^[A-Za-z0-9\s\_\-\.\,\:\@\#\=]+$/,
						maxlength:250
					},
					userAttribute:
					{
						required: true,
						userAttregx: /^[A-Za-z0-9\s\_\-\.\,\:\@\!\*]+$/
					},
					nonAnonymousDn:
					{
						required : {
							depends : function(element) {
								var cbox = $('#allowAnonymous');
								if (cbox.is(':checked')) {
									return false;
								} else {
									return true;
								}
							}
						},
						maxlength: 250,
						nonAnoDnregx: /^[A-Za-z0-9\s\_\-\.\,\:\@\#\=]+$/
					},
					nonAnonymousPassword:
					{
						required : {
							depends : function(element) {
								var cbox = $('#allowAnonymous');
								if (cbox.is(':checked')) {
									return false;
								} else {
									return true;
								}
							}
						},
						maxlength: 20,
						nonAnoPsregx: /^[A-Za-z0-9\_\-\.\,\:\@\#\!\~\$\%\^\&\*\(\)\?]+$/
					}
				},
				messages: {
					name: {
						required: '<spring:message code="error.above.field.required"/>',
						maxlength: '<spring:message code="error.invalid.ladap.name.maxlength"/>'
					},
					server: {
						required: '<spring:message code="error.above.field.required"/>',
						validip: '<spring:message code="error.invalid.ip"/>'
					},
					port: {
						required: '<spring:message code="error.above.field.required"/>',
					},
					baseDns: {
						required: '<spring:message code="error.above.field.required"/>',
						maxlength: '<spring:message code="error.invalid.ladap.dns.maxlength"/>'
					},
					userAttribute: {
						required: '<spring:message code="error.above.field.required"/>'
					},
					nonAnonymousDn:
					{
						maxlength: '<spring:message code="error.invalid.ladap.dns.maxlength"/>'
					},
					nonAnonymousPassword:
					{
						maxlength: '<spring:message code="error.invalid.ladap.pwd.maxlength"/>'
					}
				}
			});
			
			allowAnonymousCheckBoxSelect();
			var displayStatus= "${status}";  
	        if(displayStatus=="success")
	          displayLabelMessage('<spring:message code="ldap.message.save.success"/>', COLOR_SUCCESS);
	        

		jQuery.validator.addMethod("validip", function(value, element) {
			var ipPattern = /^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/;
			var ipArray = value.match(ipPattern);
			if (value == "0.0.0.0") {
				return false;
			}
			else if (value == "255.255.255.255") {
				return false;
			}
			if (ipArray == null) {
				return false;
			}
			else {
				for (i = 1; i < 5; i++) {					
					if (ipArray[i] > 255) {
						return false;
					}
				}
			}
			return true;
			});
});

function allowAnonymousCheckBoxSelect()
{
	var cb = $('#allowAnonymous');
	if (cb.is(':checked'))
	{
		$("#nonAnonymousDn").removeClass("error"); 
		$("#nonAnonymousPassword").removeClass("error");
		$("#nonAnonymousDn").attr("disabled", "disabled"); 
		$("#nonAnonymousPassword").attr("disabled", "disabled");
	}else
	{
		$("#nonAnonymousDn").removeAttr("disabled"); 
		$("#nonAnonymousPassword").removeAttr("disabled"); 
	}
} 
function displayLabelMessage(Message, Color)
{
	$("#ldap_message").html(Message);
	$("#ldap_message").css("color", Color);

}
function clearLabelMessage(Message, Color) {
	displayLabelMessage("", COLOR_DEFAULT);
}
</script>
<div class="outermostdiv">
	<div class="outerContainer">
		<span id="userlist_header_text"><spring:message
				code="ldap.server.setting" /></span>
		<div class="i1"></div>
		<div id="ldap_message" style="font-size: 14px; font-weight: bold;padding: 5px 0 0 0px;" ></div>
	</div>

	<div class="upperdiv">
		<spring:url value="/settings/ldap/server/save.ems" var="actionURL"	scope="request" />
		<form:form id="ldap-settings" method="post" action="${actionURL}"
			commandName="ldapSettings">
			<table cellspacing="10px">
				<form:hidden path="id" />
				<tr>
					<td class="formPrompt"><spring:message
							code="ldap.auth.configuration.name" />:</td>
					<td class="formValue"><form:input id="name"  path="name" class="biginput" /></td>
				</tr>
				<tr>
					<td class="formPrompt"><spring:message
							code="ldap.auth.server.url" />:</td>
					<td class="formValue"><form:input id="server" path="server"
							class="biginput" /></td>
				</tr>
				<tr>
					<td class="formPrompt"><spring:message
							code="ldap.auth.server.port" />:</td>
					<td class="formValue"><form:input id="port" path="port" class="biginput" /></td>
				</tr>
				<tr>
					<td class="formPrompt"><spring:message
							code="ldap.auth.base.dns" />:</td>
					<td class="formValue"><form:textarea id="baseDns" path="baseDns"
							class="biginput" /></td>
				</tr>
				<tr>
					<td class="formPrompt"><spring:message
							code="ldap.auth.user.attribute" />:</td>
					<td class="formValue"><form:input id="userAttribute" path="userAttribute"
							class="biginput" /></td>
				</tr>
				<tr>
					<td class="formPrompt"><spring:message code="ldap.allow.tls" />:</td>
					<td class="formValue"><form:checkbox path="tls" /></td>
				</tr>
				<tr>
					<td class="formPrompt"><spring:message
							code="ldap.allow.en.password.storage" />:</td>
					<td class="formValue"><form:select path="passwordEncrypType">
							<form:option value="none" />
							<form:option value="simple" />
							<form:option value="DIGEST-MD5" />
						</form:select></td>
				</tr>
				<tr>
					<td class="formPrompt"><spring:message
							code="ldap.auth.allow.anonymous.user" />:</td>
					<td class="formValue"><form:checkbox id="allowAnonymous" path="allowAnonymous" onclick="allowAnonymousCheckBoxSelect()"/></td>
				</tr>
				<tr>
					<td class="formPrompt"><spring:message
							code="ldap.auth.anonymous.dn" />:</td>
					<td class="formValue"><form:input id="nonAnonymousDn" path="nonAnonymousDn"
							class="biginput" /></td>
				</tr>
				<tr>
					<td class="formPrompt"><spring:message
							code="ldap.auth.anonymous.password" />:</td>
					<td class="formValue"><form:password id="nonAnonymousPassword" path="nonAnonymousPassword"
							class="biginput" />
					</td>
				</tr>
				<tr>
					<td class="formPrompt"><span></span></td>
					<td class="formValue"><input id="saveUserBtn" type="submit"
						value="<spring:message code="action.save" />"></td>
				</tr>
			</table>
		</form:form>
	</div>
	
	<!-- <div class="general">
		<br /> <b>Instructions to Fill the Data:</b>
		<ul>
			<li><b>Name:</b> Name of the LDAP Configuration. It must be
				unique.</li>
			<li><b>Server:</b> Please check with your LDAP/IT administrator
				for the Hostname to connect to LDAP. Hostname of the LDAP Server.
				For e.g. ldap.example.com .</li>
			<li><b>Port:</b> Standard LDAP ports are 389 and 636. 389 is the
				standard non-secure port where communications occur in cleartext
				(analogous to HTTP Port 80). 636 is the standard encrypted LDAP port
				(analogous to HTTP Port 443).</li>
			<li><b>Base DNS:</b>enter the base dn to search against when
				authenticating LDAP users. You can enter multiple DNs, one per line.
				So if all of your users are organized under several sub-containers
				under say for e.g., cn=Users,dc=example,dc=org, then you only need
				to enter 1 base dn, cn=Users, dc=example, dc=com.</li>
			<li><b>User Attribute:</b> The attribute in the user's object
				representing the username. For Active Directory, it is
				sAMAccountName and for most Unix LDAP environments, it isuid.</li>
			<li><b>Use TLS Encryption:</b> For encrypted communications,
				select this option.</li>
			<li><b>Store Password In Encrypted Form:</b> perform MD5
				encryption of the passwords before they are sent to LDAP.</li>
			<li><b>Allow Anonymous Use:</b> some LDAP configurations
				(especially common in Active Directory setups) restrict anonymous
				searches. If your LDAP setup does not allow anonymous searches, or
				these are restricted in such a way that login names for users cannot
				be retrieved as a result of them, then you have to specify here a
				DN//password pair that will be used for these searches..</li>

		</ul>
	</div> -->
</div>