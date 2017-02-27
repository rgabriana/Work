<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<style type="text/css">
	html body{margin:3px 0px 0px 0px !important; background: #ffffff !important; overflow: hidden !important;}
</style>
<div class="outermostdiv">
	<div class="outerContainer">
		<span id="userlist_header_text"><spring:message	code="ldap.login.setting" /></span>
		<div class="i1"></div>
		<div id="ldap_message" style="font-size: 14px; font-weight: bold; padding: 5px 0px 0px; color: green;">
			<c:choose>
						<c:when test="${not empty SAVE_RESULT}">
							LDAP Login Settings saved successfully.
						</c:when>
						<c:otherwise>
							
						</c:otherwise>
			</c:choose>
		</div>
	</div>
	
			
	
	
	<div class="upperdiv">
		<spring:url value="/settings/ldap/save.ems" var="actionURL" scope="request" />
		<form:form id="ldap-settings" method="post" action="${actionURL}" commandName="authenticationType" autocomplete="off" >
			<table cellspacing="10px">
				<tr>
					<td class="formPrompt"><spring:message code="auth.type" />:</td>
					<td class="formValue"><form:select path="authenticationType">
							<form:option value="DATABASE" />
							<form:option value="LDAP" />
						</form:select></td>
				</tr>
				<tr>
					<td class="formPrompt"><span></span></td>
					<td class="formValue"><input id="saveUserBtn" type="submit"
						value="<spring:message code="action.save" />"></td>
				</tr>
			</table>
		</form:form>
	</div>
</div>