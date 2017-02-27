<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<spring:url value="/services/events/update" var="updateEvent"
	scope="request" />
<c:set var="eventId" value="${eventsandfaultView.id}" />

<style>
	#eventsAndFaultTable table {
		border: thin dotted #7e7e7e;
		padding: 10px;
	}
	
	#eventsAndFaultTable th {
		text-align: right;
		vertical-align: top;
		padding-right: 10px;
	}
	
	#eventsAndFaultTable td {
		vertical-align: top;
		padding-top: 2px;
	}
	
	#center {
	  height : 95% !important;
	}
	
	
</style>


<div style="margin:10px 0px 0px 20px;">
	
	<table id="eventsAndFaultTable">
		<tr>
			<th><spring:message code='eventsAndFault.time' />:</th>
			<td>
				<label><fmt:formatDate
							pattern="yyyy-MM-dd hh:mm:ss a "
							value="${eventsandfaultView.eventTime}" />
				</label>
			</td>
		</tr>
		<tr>
			<th ><spring:message code='eventsAndFault.location' />:</th>
			<td >
				<label> 
					<c:if test="${ not empty eventsandfaultView.device}">
								${eventsandfaultView.device.location}
					</c:if> 
				</label>
			</td>
		</tr>
		<tr>
			<th><spring:message code='eventsAndFault.severity' />:</th>
			<td><label>${eventsandfaultView.severity}</label></td>
		</tr>
		<tr>
			<th><spring:message code='eventsAndFault.eventType' />:</th>
			<td><label>${eventsandfaultView.eventType}</label></td>
		</tr>
		<tr>
			<th><spring:message code='eventsAndFault.description' />:</th>
			<td><label>${eventsandfaultView.description}</label></td>
		</tr>
		
		<c:if test="${ not empty eventsandfaultView.device}">
				<c:choose>
				  <c:when test="${eventsandfaultView.device.type=='Gateway'}">
				  	<tr>
							<th><spring:message code='eventsAndFault.gateway' />:</th>
							<td>
								<label> 
									 ${eventsandfaultView.device.name}
								</label>
							</td>
					  </tr>
				  </c:when>
				   <c:when test="${eventsandfaultView.device.type=='Fixture'}">
					   <tr>
							<th><spring:message code='eventsAndFault.fixture' />:</th>
							<td>
								<label> 
									 ${eventsandfaultView.device.name}
								</label>
							</td>
					  </tr>
				  </c:when>
				  </c:choose>
		</c:if>
		
		<tr>
			<th><spring:message code='eventsAndFault.comments' />:</th>
			<td><textarea readonly="readonly" rows="4" cols="60">${eventsandfaultView.resolutionComments}</textarea></td>
		</tr>
		<tr>
			<th><spring:message code='eventsAndFault.resolvedBy' />:</th>
			<td><label>${eventsandfaultView.resolvedBy.email}</label></td>
		</tr>
		<tr>
			<th><spring:message code='eventsAndFault.resolvedOn' />:</th>
			<td><label><fmt:formatDate
						pattern="yyyy-MM-dd hh:mm:ss a "
						value="${eventsandfaultView.resolvedOn}" /></label></td>
		</tr>
		<tr>
			<th><spring:message code='eventsAndFault.resolved' />:</th>
			<td><input type="checkbox" id="resolve" /></td>
		</tr>
		<tr>
			<th><spring:message code='eventsAndFault.addComment' />:</th>
			<td><textarea id="newComment" rows="4" cols="60"></textarea></td>
		</tr>
		<tr>
			<th><span></span></th>
			<td>
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