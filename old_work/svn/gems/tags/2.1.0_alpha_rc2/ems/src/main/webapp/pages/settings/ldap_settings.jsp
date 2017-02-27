<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<div class="outermostdiv">
	<div class="outerContainer">
		<span id="userlist_header_text"><spring:message	code="ldap.settings" /></span>
		<div class="i1"></div>
	</div>
	
	<div class="upperdiv" style="height: 185px;margin:10px">
		<spring:url value="/settings/ldap/save.ems" var="actionURL" scope="request" />
		<form:form id="ldap-settings" method="post" action="${actionURL}" commandName="ldapSettings">
			<table cellspacing="10px">
				<tr>
					<td class="formPrompt"><spring:message code="auth.type" />:</td>
					<td class="formValue"><form:select path="authenticationType">
							<form:option value="DATABASE" />
							<form:option value="LDAP" />
						</form:select></td>
				</tr>
				<tr>
					<td class="formPrompt"><spring:message code="ldap.auth.url" />:</td>
					<td class="formValue"><form:input path="url" class="biginput"/></td>
				</tr>
				<tr>
					<td class="formPrompt"><spring:message code="ldap.auth.type" />:</td>
					<td class="formValue"><form:select
							path="ldapAuthenticationType">
							<form:option value="none" />
							<form:option value="simple" />
							<form:option value="DIGEST-MD5" />
						</form:select></td>
				</tr>
				<tr>
					<td class="formPrompt"><spring:message
							code="ldap.security.principal" />:</td>
					<td class="formValue"><form:input path="securityPrincipal" class="biginput"/></td>
				</tr>
				<tr>
					<td class="formPrompt"><spring:message
							code="ldap.allow.nonems.users" />:</td>
					<td class="formValue"><form:checkbox path="allowNonVFMUsers" /></td>
				</tr>
				<tr>
					<td class="formPrompt"><span></span></td>
					<td class="formValue"><input id="saveUserBtn" type="submit"
						value="<spring:message code="action.save" />"></td>
				</tr>
			</table>
		</form:form>
	</div>
	
	<div class="general">
		<br/>
		<b>Instructions to Fill the Data:</b>		
		<ul>
			<li><b>Authentication Type:</b> It can be either of DATABASE or
				LDAP. However the "admin"" user is always authenticated against
				database.</li>
			<li><b>Url:</b> Please check with your LDAP/IT administrator for
				the url to connect to LDAP. Usually it's in the form of
				ldap(s)://hostname:port</li>
			<li><b>Authentication Type:</b> Please check with your LDAP/IT
				administrator for the authentication type used to encrypt the
				password.</li>
			<li><b>Security Principal:</b> This determines the search path of the
				user. An example is "uid={0},ou=users,o=system". {0} acts as a place
				holder and will be replaced with the user name used for
				authentication.</li>
			<li><b>Allow Non VEM users:</b> Setting this to true will allow
				all the LDAP users to connect to VEM system with Employee role. For
				other roles and for assignment of switches, the user need to be added
				to VEM system.</li>
		</ul>
	</div>
</div>