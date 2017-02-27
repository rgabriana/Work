<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<spring:url value="/scripts/jquery/jquery.cookie.20110708.js" var="jquery_cookie"></spring:url>
<script type="text/javascript" src="${jquery_cookie}"></script>

<spring:url value="/scripts/jquery/jquery.alerts.js" var="jqueryalerts"></spring:url>
<script type="text/javascript" src="${jqueryalerts}"></script>

<spring:url value="/themes/standard/css/jquery/jquery.alerts.css"
	var="jqueryalertscss" />
<link rel="stylesheet" type="text/css" href="${jqueryalertscss}" />

<spring:url value="/scripts/jquery/jquery.jBreadCrumb.1.1.js"
	var="jquerybreadcrumb"></spring:url>
<script type="text/javascript" src="${jquerybreadcrumb}"></script>

<spring:url value="/scripts/jquery/jquery.validate.1.9.min.js" var="jqueryvalidate"></spring:url>
<script type="text/javascript" src="${jqueryvalidate}"></script>

<spring:url value="/facilities/organization/addChildFacility.ems" var="createChildFacility"
	scope="request" />

<spring:url value="/facilities/tree.ems" var="refreshTreeUrl"
	scope="request" />

<c:if test="${facilityType != null && facilityType != ''}">
<c:set var='facilityType' value="${facilityType}" scope="session" />
</c:if>

<style type="text/css">
	/* html, body {margin:0px !important; background:#fff;} */
	html,body {	margin: 0px !important;}
	input[type="text"]{width:300px; height:22px;}
</style>

<script type="text/javascript">
var editChildFacility = null;

var editFacility = null;

$().ready(function() {
	
	$('#facilityName').val('${facilityName}');
	
	editFacility = '${facilityName}';
	
	$(".maindiv").css("height", $(window).height() - 155);
	$(".maindiv").css("overflow", "auto");
	
	$("#company").text("${companyName}");
	
	$('#parentFacilityId').val("${facilityId}");
	$('#parentFacilityType').val("${facilityType}");
	
	<c:if test="${!empty childFacilities}">
	$("#childfacilitysubmit").val("<spring:message code='action.create.another'/>");
	</c:if>
	$('#cancel').hide();
	
	<c:choose>
	    <c:when test="${facilityType == 'organization'}">
	    	$('#childFacilityType').val("campus");
	    </c:when>
	    <c:when test="${facilityType == 'campus'}">
	    	$("#campus").text("${campusName}");
	    	$('#childFacilityType').val("building");
		</c:when>
		<c:when test="${facilityType == 'building'}">
			$("#campus").text("${campusName}");
			$("#building").text("${buildingName}");
			$('#childFacilityType').val("floor");
		</c:when>
		<c:when test="${facilityType == 'floor'}">
			$("#campus").text("${campusName}");
			$("#building").text("${buildingName}");
			$("#floor").text("${floorName}");
			$('#childFacilityType').val("");
		</c:when>
	    <c:otherwise>
	        
	    </c:otherwise>
	</c:choose>
	
	
	jQuery.validator.addMethod("duplicateChildFacility", function(value, element) {
		value = $.trim(value);
		<c:forEach items="${childFacilities}" var="facility">
			if(value.toLowerCase() == "${facility.name}".toLowerCase() && "${facility.name}" != editChildFacility) {
				return false;
			}
		</c:forEach>
		return true;
		});
	
	$("#create-child-facility").validate({
		rules: {
			facilityName: {
				required: true,
				duplicateChildFacility: true,
				maxlength: 64
			}
			
		},
		messages: {
			facilityName: {
				required: '<spring:message code="error.above.field.required"/>',
				duplicateChildFacility:'<spring:message code="error.duplicate.childFacility"/>',
				maxlength: '<spring:message code="error.invalid.name.maxlength64"/>'
			}
		}
	});
	
	
	$('#create-child-facility').attr('action', '${createChildFacility}'+"?ts="+new Date().getTime());
	
	jQuery.validator.addMethod("duplicateFacility", function(value, element) {
		value = $.trim(value);
		<c:forEach items="${siblingFacilities}" var="facility">
			if(value.toLowerCase() == "${facility.name}".toLowerCase() && "${facility.name}" != editFacility) {
				return false;
			}
		</c:forEach>
		return true;
		});
	
	$("#edit-facility-name").validate({
		rules: {
			facilityName: {
				required: true,
				duplicateFacility: true,
				maxlength: 64
			}
			
		},
		messages: {
			facilityName: {
				required: '<spring:message code="error.above.field.required"/>',
				duplicateFacility:'<spring:message code="error.duplicate.SiblingFacility"/>',
				maxlength: '<spring:message code="error.invalid.name.maxlength64"/>'
			}
		}
	});
	
	$('#edit-facility-name').attr('action', '<spring:url value="/facilities/organization/editFacilityName.ems"/>'+"?ts="+new Date().getTime());
	
	$("#breadCrumb").jBreadCrumb({minimumCompressionElements: 6});
	
	var refreshTree = <%=request.getParameter("refreshTree")%>;
	if(refreshTree != null && refreshTree) {
		$.ajax({
			type: "GET",
			cache: false,
			async: false,
			url: "${refreshTreeUrl}?customerId=${customerId}&ts="+new Date().getTime(),
			dataType: "html",
			success: function(msg) {
				parent.removeclick();
				$('#facilityTreeViewDiv', window.parent.document).html($("#facilityTreeViewDiv", $(msg)).html());
				parent.loadTree("${facilityType}"+"_"+"${facilityId}");
				parent.nodeclick();
				parent.showSettings();
			}
		});
	}
	
	
});

function editChildFacilityName(obj) {
	$("#deleteStatus").html("");
	$('#create-child-facility').attr('action', '<spring:url value="/facilities/organization/editChildFacilityName.ems"/>'+"?ts="+new Date().getTime());
	facilityid = $(obj).attr('id');
	$('#childFacilityId').val(facilityid);
	<c:forEach items="${childFacilities}" var="facility">
		if(facilityid == "${facility.nodeId}") {
			editChildFacility = "${facility.name}";
			$('#childFacilityName').val("${facility.name}");
			$('#childFacilityType').val("${facility.nodeType}");
		}
	</c:forEach>
	$('#outerContainer').find(".error").html('');
	$("#name").removeClass('error');
	$("#childfacilitysubmit").val("<spring:message code='action.save'/>");
	$('#cancel').show();
}

function back() {
	$("label.error").hide();
	$(".error").removeClass("error");
	$('#id').val('');
	$('#create-child-facility').attr('action', '${createChildFacility}'+"?ts="+new Date().getTime());
	editChildFacility = null;
	$('#childFacilityName').val("");
	$("#childfacilitysubmit").val("<spring:message code='action.create.another'/>");
	$('#cancel').hide();
	
}

function resetFacility(){
	$("label.error").hide();
	$(".error").removeClass("error");
	editFacility = '${facilityName}';
	$('#facilityName').val('${facilityName}');
}

function saveFacility(){
	$("label.error").hide();
	$(".error").removeClass("error");
}

function saveChildFacility() {
	$("#deleteStatus").html("");
}

function deleteFacility(obj) {
	$("#deleteStatus").html("");
	var name;
	var facilityId = $(obj).attr('id');
	<c:forEach items="${childFacilities}" var="facility">
	if(facilityId == "${facility.nodeId}") {
		name = "${facility.name}";
	}
	</c:forEach>
	jConfirm('Are you sure you want to delete '+ name+"?",'<spring:message code="deletion.confirmation.title"/>',function(result){
        if(result) {
        	$.ajax({
        		type: "POST",
        		cache: false,
        		url: '<spring:url value="/services/org/facility/v1/delete/"/>'+facilityId,
        		dataType: "html",
        		success: function(msg) {
        			if(msg == "F") {
        				$("#deleteStatus").html("Facility '" + name  + "' cannot be deleted as there is at least one child Facility attached to it." );
        			}
        			else if(msg == "FEMMAP"){
        				$("#deleteStatus").html("Facility '" + name  + "' cannot be deleted as it is Mapped to an EM Instance floor.Please UNMAP it before deleting this floor." );
        			}
        			else if(msg == "S") {
        				$('#deleteRefresh').attr('action', '<spring:url value="/facilities/delete/refresh.ems"/>');
        				$('#deletedParentFacilityId').val("${facilityId}");
        				$('#deleteRefresh').submit();
        			}
        		}
        	});
        }
	});
}


</script>

<div id="editFacility" style="padding:10px 0px 0px 0px; height:100% !important;background:#fff">
<div class="upperdiv" style="height:100% !important; min-height:90px">
	<div style="padding:10px;">
		<form id="edit-facility-name" method="post" >
			
			<input id="facilityId" name="facilityId" type="hidden" value="${facilityId}"/>
			<input id="facilityType" name="facilityType" type="hidden" value="${facilityType}"/>
			
			<div class="field">
				<div class="formPrompt"><span><spring:message code="settings.organization"/></span></div>
				<div style="float:left"><input name='facilityName' id="facilityName" size="40" /></div>
			</div>
			<div class="field">
				<div class="formPrompt"><span></span></div>
				<div>
					<input class="navigation" id="facilitysubmit" type="submit" value="<spring:message code='action.save'/>" onclick="saveFacility();"></input>
					<input class="navigation" id="facilityreset" type="button" onclick='resetFacility();' value="<spring:message code='action.reset'/>"></input>
				</div>
			</div>
		</form>
	</div>
</div>	
</div>

<c:if test="${facilityType != 'floor'}">
<div style="padding-top:15px"><strong><span><spring:message code="genericSetup.heading.name"/></span></strong></div>
<div class="i1"></div>

<div class="maindiv">
	<div>
	<div class="upperdiv" style="padding: 0px 10px 10px 10px;">
		<form id="deleteRefresh" METHOD="POST">
			<input id="deletedParentFacilityId" name="deletedParentFacilityId" type="hidden"/>
		</form>
		<div>
			<div class="breadCrumbTop module">
					<div id="breadCrumb" class="breadCrumb module">
						<c:choose>
						    <c:when test="${facilityType == 'organization'}">
							    <ul>
									<li><span id="company"></span></li>
									<li><span id="campus"><spring:message
												code="setup.campus.header" /></span></li>
								</ul>
						    </c:when>
						    <c:when test="${facilityType == 'campus'}">
							    <ul>
									<li><span id="company"></span></li>
									<li><span id="campus"></span></li>
									<li><span id="building"><spring:message
												code="setup.building.header" /></span></li>
								</ul>
							</c:when>
							<c:when test="${facilityType == 'building'}">
								<ul>
									<li><span id="company"></span></li>
									<li><span id="campus"></span></li>
									<li><span id="building"></span></li>
									<li><span id="floor"><spring:message
												code="setup.floor.header" /></span></li>
								</ul>
							</c:when>
							<c:when test="${facilityType == 'floor'}">
								<ul>
									<li><span id="company"></span></li>
									<li><span id="campus"></span></li>
									<li><span id="building"></span></li>
									<li><span id="floor"></span></li>
								</ul>
							</c:when>
						    <c:otherwise>
						        
						    </c:otherwise>
					    </c:choose>

					</div>
				</div>
			
			<form id="create-child-facility" method="post">
				<input id="childFacilityId" name="facilityId" type="hidden"/>
				<input id="childFacilityType" name="facilityType" type="hidden" />
				<input id="parentFacilityId" name="parentFacilityId" type="hidden"/>
				<input id="parentFacilityType" name="parentFacilityType" type="hidden" />
				<div class="field">
					<div class="formPrompt">
						<c:choose>
						    <c:when test="${facilityType == 'organization'}">
						    	<span><spring:message code="campusSetup.label.name" /></span>
						    </c:when>
						    <c:when test="${facilityType == 'campus'}">
						    	<span><spring:message code="buildingSetup.label.building.name" /></span>
							</c:when>
							<c:when test="${facilityType == 'building'}">
								<span><spring:message code="floorSetup.label.name" /></span>
							</c:when>
							<c:otherwise>
						        
						    </c:otherwise>
					    </c:choose>
					</div>
					<div class="formValue">
						<input id="childFacilityName" name="facilityName" size="24" />
					</div>
				</div>
				<div class="field">
					<div class="formPrompt">
						<span></span>
					</div>
					<div>
						<input class="navigation" id="childfacilitysubmit" onclick="saveChildFacility();"
							type="submit" value="<spring:message code='action.create'/>"></input>
						<input class="navigation" id="cancel" onclick="back();"
							type="button" value="<spring:message code='action.cancel'/>"></input>
					</div>
				</div>
			</form>
		</div>
		
		<c:if test="${!empty childFacilities}">
			<div style="text-align: center; padding-top: 20px;">
					<span style="color: red;" id="deleteStatus"></span>
			</div>
			<div id="tableContainer" class="tableSettings" style="max-height: 175px; margin-top: 30px;">
				<table cellpadding="0" cellspacing="0" class="entable"
					style="width: 100%">
					<thead>
						<tr>
							<c:choose>
							    <c:when test="${facilityType == 'organization'}">
							    	<th><spring:message code="campusSetup.header.name" /></th>
							    </c:when>
							    <c:when test="${facilityType == 'campus'}">
							    	<th><spring:message code="buildingSetup.header.building.name" /></th>
								</c:when>
								<c:when test="${facilityType == 'building'}">
									<th><spring:message code="floorSetup.header.floor.name" /></th>
								</c:when>
								<c:otherwise>
							        
							    </c:otherwise>
						    </c:choose>
									<th class="alignright">Action</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach items="${childFacilities}" var="facility">
							<tr>
								<td id=<c:out value="${facility.nodeId}"/> ><c:out value="${facility.name}" /></td>
								<td class='btnsettings'><input
									id='<c:out value="${facility.nodeId}"/>' type="button"
									onclick="editChildFacilityName(this);"
									value="<spring:message code='action.edit'/>" />
									&nbsp; 
									<input id='<c:out value="${facility.nodeId}"/>' type="button"
									onclick='deleteFacility(this);'
									value="<spring:message code='action.delete'/>" />
								</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
			</c:if>
		</div>
	</div>
</div>
</c:if>