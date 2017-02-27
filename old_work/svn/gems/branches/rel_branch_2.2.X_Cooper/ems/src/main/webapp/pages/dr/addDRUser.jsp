<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<div class="outerContainer">
	<span ><spring:message code="dr.user.registration"/></span>
	<div class="i1"></div>
	<div class="innerContainer">
		<div class="formContainer">
			<div style="clear: both"><span id="error" class="load-save-errors"></span></div>
			<div style="clear: both"><span id="confirm" class="save_confirmation"></span></div>
			<spring:url value="/dr/registerUser.ems" var="submit" scope="request"/>
			<form:form id="drUserRegistration" commandName="druser" method="post" action="${submit}" onsubmit="submitForm();">
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.server"/></span></div>
					<div class="formValue"><form:input id="server" name="server" path="server"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.user"/></span></div>
					<div class="formValue"><form:input id="name" path="name" maxlength="40"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.password"/></span></div>
					<div class="formValue"><input type="password" id="newPassword" name="newPassword" maxlength="40" /></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.confirm.password"/></span></div>
					<div class="formValue"><input id="confirmPassword" type="password" name="confirmPassword"  maxlength="40" /></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span></span></div>
					<div class="formValue"><input class="saveAction" id="submit" type="submit" value="<spring:message code='action.submit'/>"></input></div>
				</div>
			</form:form>
		</div>
	</div>
</div>

<script type="text/javascript">
$().ready(function() {
	
	var requirederr = '<spring:message code="error.above.field.required"/>';
	$("#drUserRegistration").validate({
		rules: {
			server: "required",
			name: "required",
			newPassword: {
				required: true,
				minlength: 5
			},
			confirmPassword: {
				required: true,
				equalTo: "#newPassword"
			}
		},
		messages: {
			server: requirederr,
			name: requirederr,
			newPassword: {
				required: requirederr,
				minlength: '<spring:message code="error.password.length"/>'
			},
			confirmPassword: {
				required: requirederr,
				equalTo: '<spring:message code="error.passwords.not.match"/>'
			}
		}
	});
});

var status = '<%=request.getParameter("status")%>';
if(status == 'S') {
	$("#confirm").html('<spring:message code="dr.user.save.confirmation"/>');
} 
if(status == 'E') {
	$("#error").html('<spring:message code="dr.user.save.failure"/>');
}

$(function() {
	$(window).resize(function() {
		var setSize = $(window).height();
		setSize = setSize - 118;
		$(".outerContainer").css("height", setSize);
	});
});
$(".outerContainer").css("overflow", "auto");
$(".outerContainer").css("height", $(window).height() - 118);

function submitForm() {
	$("#confirm").html("");
	$("#error").html("");
}
	
</script>