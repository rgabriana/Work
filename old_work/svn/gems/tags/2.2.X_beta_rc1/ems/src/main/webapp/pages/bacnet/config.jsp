<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<script type="text/javascript">

$().ready(function() {
	
	var requirederr = '<spring:message code="error.above.field.required"/>';
	var num_0_to_4194303 = '<spring:message code="error.numeric.0.to.4194303"/>';
	var num_1_to_120 = '<spring:message code="error.numeric.1.to.120"/>';
	var num_1_to_65535 = '<spring:message code="error.numeric.1.to.65535"/>';
	
	$("#bacnetConfig").validate({
		rules: {
			vendorId: {
				required: true,
				digits: true,
				min: 1,
				max: 65535
			},
			serverPort: {
				required: true,
				digits: true,
				min: 1,
				max: 65535
			},
			networkId: {
				required: true,
				digits: true,
				min: 1,
				max: 65535
			},
			apduLength: {
				required: true,
				digits: true,
				min: 1,
				max: 65535
			},
			apduTimeout: {
				required: true,
				digits: true,
				min: 1,
				max: 120
			},
			deviceBaseInstance: {
				required: true,
				digits: true,
				min: 0,
				max: 4194303
			}
		},
		messages: {
			vendorId: {
				required: requirederr,
				digits: num_1_to_65535,
				min: num_1_to_65535,
				max: num_1_to_65535
			},
			serverPort: {
				required: requirederr,
				digits: num_1_to_65535,
				min: num_1_to_65535,
				max: num_1_to_65535
			},
			networkId: {
				required: requirederr,
				digits: num_1_to_65535,
				min: num_1_to_65535,
				max: num_1_to_65535
			},
			apduLength: {
				required: requirederr,
				digits: num_1_to_65535,
				min: num_1_to_65535,
				max: num_1_to_65535
			},
			apduTimeout: {
				required: requirederr,
				digits: num_1_to_120,
				min: num_1_to_120,
				max: num_1_to_120
			},
			deviceBaseInstance: {
				required: requirederr,
				digits: num_0_to_4194303,
				min: num_0_to_4194303,
				max: num_0_to_4194303
			}
		}
	});


});

</script>


<div class="outerContainer">
	<span ><spring:message code="bacnet.header"/></span>
	<div class="i1"></div>
	<div class="innerContainer">
		<div class="formContainer">
			<div style="clear: both"><span id="error" class="load-save-errors"></span></div>
			<div style="clear: both"><span id="confirm" class="save_confirmation"></span></div>
			<spring:url value="/bacnet/submit.ems" var="submit" scope="request"/>
			<form:form id="bacnetConfig" commandName="bacnet" method="post" action="${submit}">
				<div class="field">
					<div class="formPrompt"><span><spring:message code="bacnet.label.vendor.id"/></span></div>
					<div class="formValue"><form:input readonly='true' id="vendorId" path="vendorId"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="bacnet.label.server.port"/></span></div>
					<div class="formValue"><form:input id="serverPort" path="serverPort"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="bacnet.label.network.id"/></span></div>
					<div class="formValue"><form:input id="networkId" path="networkId"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="bacnet.label.max.apdu.length"/></span></div>
					<div class="formValue"><form:input id="apduLength" path="apduLength"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="bacnet.label.apdu.timeout"/></span></div>
					<div class="formValue"><form:input id="apduTimeout" path="apduTimeout"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="bacnet.label.device.base.instance"/></span></div>
					<div class="formValue"><form:input id="deviceBaseInstance" path="deviceBaseInstance"/></div>
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
var error = '<%=request.getParameter("error")%>';
var confirm = '<%=request.getParameter("confirm")%>';
if(error == 'save_error') {
	$("#error").html('<spring:message code="error.bacnet.save"/>');
} else {
	error = '${error}';
}
if(error == 'load_error') {
	$("#error").html('<spring:message code="error.bacnet.load"/>');
}
if(confirm == 'save_success') {
	$("#confirm").html('<spring:message code="bacnet.save.confirmation"/>');
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
</script>