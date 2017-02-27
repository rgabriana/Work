<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<script type="text/javascript"> 
$().ready(function(){
			$("#cloudsettings").validate({
				rules: {
					serverIp: {
						required: true
					}
				},
				messages: {
					serverIp: {
						required: '<spring:message code="error.above.field.required"/>',
					}
				}
			});
			
			savestatus = "<%=request.getParameter("save")%>";
			if(savestatus == "S") {
				$("#savestatus").css("color", "green");
				$("#savestatus").html("Setup is saved successfully.");
			}
			else if (savestatus == "F") {
				$("#savestatus").css("color", "red");
				$("#savestatus").html("Setup is not saved successfully. Please try again.");
			}
});
</script>
<div class="outermostdiv">
	<div class="outerContainer">
		<span id="header_text"><spring:message	code="header.system.cloudServerSettings" /></span>
		<div class="i1"></div>
	</div>
	<div class="upperdiv" style="height: 165px;margin:10px">
	<spring:url value="/settings/cloudServerSettings/save.ems" var="actionURL" scope="request" />
		<form:form id="cloudsettings" method="post" action="${actionURL}" commandName="cloudServerSetting">
			<table cellspacing="10px">
				<tr>
					<td class="formPrompt"><spring:message code="system.cloudServer.ip" />:</td>
					<td class="formValue"><form:input id="serverIp" path="serverIp" class="biginput" /></td>
				</tr>
				<tr>
					<td class="formPrompt"><spring:message code="system.em.mac" />:</td>
					<td class="formValue"><form:input id="emMac" path="emMac" readonly="true"  class="biginput" /></td>
				</tr>
				<tr>
					<td class="formPrompt"><span></span></td>
					<td class="formValue"><input style="display: inline; margin-right: 10px;" id="saveUserBtn" type="submit"
						value="<spring:message code="action.save" />"><span id="savestatus" style="font-size: 1.1em; display: inline"></span></td>
				</tr>
			</table>
		</form:form>
		</div>
	<div class="general">
		<br/>
		<b>Instructions to Fill the Data:</b>		
		<ul>
			<li><b><spring:message code="system.cloudServer.ip" />: </b>Cloud server domain that this EM communicates to.</li>
			<li><b><spring:message code="system.em.mac" />: </b>Mac Address for this EM.</li>
		</ul>
	</div>
	</div>

