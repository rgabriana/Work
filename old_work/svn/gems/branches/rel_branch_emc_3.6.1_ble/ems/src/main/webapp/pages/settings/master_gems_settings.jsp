<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<script type="text/javascript"> 
$().ready(function(){
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
			for (var i = 1; i < 5; i++) {					
				if (ipArray[i] > 255) {
					return false;
				}
			}
		}
		return true;
		});
	
			$("#mastergemssettings").validate({
				rules: {
					masterGemsIp: {
						required: true,
						validip: ""
					},
					masterGemsPort: {
						required: true,
						number: true
					},
					emIp: {
						required: true,
						validip: ""
					},
					emPort:
					{
						required: true,
						number: true
					}
				},
				messages: {
					masterGemsIp: {
						required: '<spring:message code="error.above.field.required"/>',
						validip: '<spring:message code="error.invalid.master.em.ip"/>'
					},
					masterGemsPort: {
						required: '<spring:message code="error.above.field.required"/>',
					},
					emIp: {
						required: '<spring:message code="error.above.field.required"/>',
						validip: '<spring:message code="error.invalid.em.ip"/>'
					},
					emPort: {
						required: '<spring:message code="error.above.field.required"/>'
					}
				}
			});
});
</script>
<div class="outermostdiv">
	<div class="outerContainer">
		<span id="header_text"><spring:message	code="header.system.masterGemsSetting" /></span>
		<div class="i1"></div>
	</div>
	<div class="upperdiv" style="height: 185px;margin:10px">
	<spring:url value="/settings/master_gems_setting/save.ems" var="actionURL" scope="request" />
		<form:form id="mastergemssettings" method="post" action="${actionURL}" commandName="masterGemsSetting">
			<table cellspacing="10px">
				<tr>
					<td class="formPrompt"><spring:message code="system.masterGems.ip" />:</td>
					<td class="formValue"><form:input id="masterIp" path="masterGemsIp" value="" class="biginput" /></td>
				</tr>
				<tr>
					<td class="formPrompt"><spring:message code="system.masterGems.port" />:</td>
					<td class="formValue"><form:input id="masterGemsPort" path="masterGemsPort" value="" class="biginput" /></td>
				</tr>
				<tr>
					<td class="formPrompt"><spring:message code="system.em.ip" />:</td>
					<td class="formValue"><form:input id="emIp" path="emIp" value="" class="biginput" /></td>
				</tr>
				<tr>
					<td class="formPrompt"><spring:message code="system.em.port" />:</td>
					<td class="formValue"><form:input id="emPort" path="emPort" value="" class="biginput" /></td>
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
			<li><b>Master EM Server:</b> Name/IP of the Master EM that this EM communicates to.</li>
			<li><b>Master EM Port:</b>Port number, on which EM will communicate with Master EM.</li>
			<li><b>EM Server:</b> Name/IP for this EM.</li>
			<li><b>EM Port:</b> Port number of this EM.</li>
		</ul>
	</div>
	</div>
