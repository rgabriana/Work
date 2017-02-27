<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<spring:url value="/services/events/update" var="updateEvent"
	scope="request" />
<c:set var="eventId" value="${eventsandfaultView.id}" />
<div>
	<table border="0" cellpadding="0" cellspacing="5">
		<tr>
			<td class="formPrompt"><spring:message code='eventsAndFault.time' />:</td>
			<td class="formValue"><label><fmt:formatDate
						pattern="yyyy-MM-dd hh:mm:ss a "
						value="${eventsandfaultView.eventTime}" /></label></td>
		</tr>
		<tr>
			<td class="formPrompt"><spring:message code='eventsAndFault.location' />:</td>
			<td class="formValue"><label> <c:if
						test="${ not empty eventsandfaultView.fixture}">
							${eventsandfaultView.fixture.location}
						</c:if> <c:if test="${ not empty eventsandfaultView.gateway}">
							${eventsandfaultView.gateway.location}
						</c:if>
			</label></td>
		</tr>
		<tr>
			<td class="formPrompt"><spring:message code='eventsAndFault.severity' />:</td>
			<td class="formValue"><label>${eventsandfaultView.severity}</label></td>
		</tr>
		<tr>
			<td class="formPrompt"><spring:message code='eventsAndFault.eventType' />:</td>
			<td class="formValue"><label>${eventsandfaultView.eventType}</label></td>
		</tr>
		<tr>
			<td class="formPrompt"><spring:message code='eventsAndFault.description' />:</td>
			<td class="formValue"><label>${eventsandfaultView.description}</label></td>
		</tr>
		<tr>
			<td class="formPrompt"><spring:message code='eventsAndFault.fixture' />:</td>
			<td class="formValue"><label> <c:if
						test="${ not empty eventsandfaultView.fixture}">
						${eventsandfaultView.fixture.fixtureName}
					</c:if>
			</label></td>
		</tr>
		<tr>
			<td class="formPrompt"><spring:message code='eventsAndFault.gateway' />:</td>
			<td class="formValue"><label>
					<c:if
						test="${ not empty eventsandfaultView.gateway}">
						${eventsandfaultView.gateway.gatewayName}
					</c:if>
					<c:if
						test="${ not empty eventsandfaultView.fixture}">
						${eventsandfaultView.fixture.gateway.gatewayName}
					</c:if>
			</label></td>
		</tr>
		<tr>
			<td class="formPrompt"><spring:message code='eventsAndFault.comments' />:</td>
			<td class="formValue"><textarea readonly="readonly" rows="4" cols="50">${eventsandfaultView.resolutionComments}</textarea></td>
		</tr>
		<tr>
			<td class="formPrompt"><spring:message code='eventsAndFault.resolvedBy' />:</td>
			<td class="formValue"><label>${eventsandfaultView.resolvedBy.email}</label></td>
		</tr>
		<tr>
			<td class="formPrompt"><spring:message code='eventsAndFault.resolvedOn' />:</td>
			<td class="formValue"><label><fmt:formatDate
						pattern="yyyy-MM-dd hh:mm:ss a "
						value="${eventsandfaultView.resolvedOn}" /></label></td>
		</tr>
		<tr>
			<td class="formPrompt"><spring:message code='eventsAndFault.resolved' />:</td>
			<td class="formValue"><input type="checkbox" id="resolve" /></td>
		</tr>
		<tr>
			<td class="formPrompt"><spring:message code='eventsAndFault.addComment' />:</td>
			<td class="formValue"><textarea id="newComment" rows="4" cols="50"></textarea></td>
		</tr>
		<tr>
			<td class="formPrompt"><span></span></td>
			<td class="formValue">
				<button type="button" onclick="updateEvent();">
					<spring:message code='action.submit' />
				</button>&nbsp;
				<input type="button" id="btnClose"
					value="<spring:message code="action.cancel" />" onclick="closeDialog()">
			</td>
		</tr>
	</table>
</div>

<script type="text/javascript">

<c:if test="${eventsandfaultView.active == false}">
	$("#resolve").attr("checked", "checked");
	$("#resolve").attr("disabled", "disabled");
</c:if>

function updateEvent() {
	if($("#newComment").val() == "") {
		$("#newComment").val(" ");
	}
	 $.ajax({
		type: "POST",
		url: "${updateEvent}" + "/" + '${eventId}'  + "/" + encodeURIComponent($("#newComment").val()) + "/" + $("#resolve").is(":checked"),
		dataType: "html",
		success: function(msg) {
			dialogResult = msg;
			$("#viewDialog").dialog("close");
		},
		error: function() {
			dialogResult = "E";
			$("#viewDialog").dialog("close");
		}
	});
}
</script>