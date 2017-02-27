<spring:url value="/addCampus.ems" var="createURL" scope="request" />
<spring:url value="/updateCampus.ems" var="editURL" scope="request" />
<spring:url value="/admin/organization/addCampus.ems" var="createCampus"
	scope="request" />
<spring:url value="/admin/organization/updateCampus.ems"
	var="updateCampus" scope="request" />
<spring:url value="/scripts/jquery/jquery.alerts.js" var="jqueryalerts"></spring:url>
<script type="text/javascript" src="${jqueryalerts}"></script>

<spring:url value="/themes/standard/css/jquery/jquery.alerts.css"
	var="jqueryalertscss" />
<link rel="stylesheet" type="text/css" href="${jqueryalertscss}" />

<spring:url value="/scripts/jquery/jquery.jBreadCrumb.1.1.js"
	var="jquerybreadcrumb"></spring:url>
<script type="text/javascript" src="${jquerybreadcrumb}"></script>

<style type="text/css">
	/* html, body {margin:0px !important; background:#fff;} */
	html,body {	margin: 0px !important;}
	input[type="text"]{width:300px; height:22px;}
</style>

<script type="text/javascript">
var editCampus = null;
var campusid = null;
$().ready(function() {
	
	jQuery.validator.addMethod("duplicate", function(value, element) {
		value = $.trim(value);
		<c:forEach items="${campuses}" var="campus">
			if(value.toLowerCase() == "${campus.name}".toLowerCase() && "${campus.name}" != editCampus) {
				return false;
			}
		</c:forEach>
		return true;
		}, '<spring:message code="error.duplicate.campus"/>');
	
	$("#create-campus").validate({
		rules: {
			name: {
				required: true,
				duplicate: ""
			}
			
		},
		messages: {
			name: {
				required: '<spring:message code="error.above.field.required"/>'
			}
		}
	});
	
	<c:if test="${mode != 'admin'}">
		/* $(function() {
			$(window).resize(function() {
				var setSize = $(window).height();
				setSize = setSize - 240;
				$(".maindiv").css("height", setSize);
			});
		}); */

		$(".maindiv").css("height", $(window).height() - 190);
		$(".maindiv").css("overflow", "auto");
	</c:if>	
	
	<c:if test="${mode == 'admin'}">
		$(".maindiv").css("height", $(window).height() - 160);
		$(".maindiv").css("overflow", "auto");
	</c:if>
});

function edit(obj) {
	$("#deleteStatus").html("");
	<c:if test="${mode == 'admin'}">
		$('#create-campus').attr('action', '${updateCampus}'+"?ts="+new Date().getTime());
	</c:if>
	<c:if test="${mode != 'admin'}">
	$('#create-campus').attr('action', '${editURL}'+"?ts="+new Date().getTime());
	</c:if>
	campusid = $(obj).attr('id');
	$('#id').val(campusid);
	<c:forEach items="${campuses}" var="campus">
		if(campusid == "${campus.id}") {
			editCampus = "${campus.name}";
			$('#name').val("${campus.name}");
			$('#location').val("${campus.location}");
		}
	</c:forEach>
	$('#outerContainer').find(".error").html('');
	$("#name").removeClass('error');
	$("#submit").val("<spring:message code='action.save'/>");
	$('#cancel').show();
}

function back() {
	$('#outerContainer').find(".error").html('');
	$("#name").removeClass('error');
	$('#id').val('');
	<c:if test="${mode == 'admin'}">
		$('#create-campus').attr('action', '${createCampus}'+"?ts="+new Date().getTime());
	</c:if>
	<c:if test="${mode != 'admin'}">
		$('#create-campus').attr('action', '${createURL}'+"?ts="+new Date().getTime());
	</c:if>
	editCampus = null;
	$('#name').val("");
	$('#location').val("");
	$("#submit").val("<spring:message code='action.create.another'/>");
	$('#cancel').hide();
	
}

function save() {
	$("#deleteStatus").html("");
}

function deleteOrg(obj) {
	var name;
	var id = $(obj).attr('id');
	<c:forEach items="${campuses}" var="campus">
	if(id == "${campus.id}") {
		name = "${campus.name}";
	}
	</c:forEach>
	jConfirm('Are you sure you want to delete '+ name+"?",'<spring:message code="deletion.confirmation.title"/>',function(result){
        if(result) {
        	$.ajax({
        		type: "POST",
        		cache: false,
        		url: '<spring:url value="/services/org/campus/delete/"/>'+id,
        		dataType: "html",
        		success: function(msg) {
        			if(msg == "F") {
        				$("#deleteStatus").html("Campus " + name  + " cannot be deleted. There exists atleast one building under this campus." );
        			}
        			else if(msg == "S") {
        				<c:if test="${mode == 'admin'}">
        					$('#deleteRefresh').attr('action', '<spring:url value="/admin/delete/refresh.ems"/>');
        					
        				</c:if>
        				$('#deleteRefresh').submit();
        			}
        		}
        	});
        }
	});
}
</script>

<script type="text/javascript">
	 $('#firstStep').hide();
	 $('#secondStep').show();
	 $('#thirdStep').hide();
	 $('#fourthStep').hide();
	 $('#fifthStep').hide();
</script>
<div class="maindiv">
	<div>
	<div class="upperdiv" style="padding: 0px 10px 10px 10px;">
		<form id="deleteRefresh" action=<spring:url value="deleteCampus.ems"/>>
		</form>
		<c:if test="${mode != 'admin'}">
			<div class="titletxt">
				<spring:message code="setup.campus.header" />
			</div>
		</c:if>
		<div>
			<c:if test="${mode == 'admin'}">
				<div class="breadCrumbTop module">
					<div id="breadCrumb" class="breadCrumb module">
						<ul>
							<li><span id="company"></span></li>
							<li><span id="campus"><spring:message
										code="setup.campus.header" /></span></li>
						</ul>
					</div>
				</div>
			</c:if>
			<form:form id="create-campus" commandName="campus" method="post"
				action="${createURL}">
				<form:hidden id="id" name="id" path="id" />
				<div class="field">
					<div class="formPrompt">
						<span><spring:message code="campusSetup.label.name" /></span>
					</div>
					<div class="formValue">
						<form:input id="name" name="name" size="24" maxlength="20"
							path="name" />
					</div>
				</div>
				<div class="field">
					<div class="formPrompt">
						<span><spring:message code="campusSetup.label.location" /></span>
					</div>
					<div class="formValue">
						<form:input id="location" name="location" size="24" maxlength="20"
							path="location" />
					</div>
				</div>
				<div class="field">
					<div class="formPrompt">
						<span></span>
					</div>
					<div>
						<input class="navigation" id="submit" onclick="save();"
							type="submit" value="<spring:message code='action.create'/>"></input>
						<input class="navigation" id="cancel" onclick="back();"
							type="button" value="<spring:message code='action.cancel'/>"></input>
					</div>
				</div>
			</form:form>
		</div>
		<script type="text/javascript">
			<c:if test="${!empty campuses}">
				$("#submit").val("<spring:message code='action.create.another'/>");
			</c:if>
			$('#cancel').hide();
			<c:if test="${mode == 'admin'}">
				$("#company").text("${companyName}");
				$('#create-campus').attr('action', '${createCampus}');
				$("#breadCrumb").jBreadCrumb({minimumCompressionElements: 6});
			</c:if>
		</script>
		<c:if test="${!empty campuses}">
			<div id="tableContainer" class="tableSettings">
				<div style="text-align: center;">
					<span style="color: red;" id="deleteStatus"></span>
				</div>
				<table cellpadding="0" cellspacing="0" class="entable"
					style="width: 100%">
					<thead>
						<tr>
							<th><spring:message code="campusSetup.header.name" /></th>
							<th><spring:message code="campusSetup.header.location" /></th>
							<th class="alignright">Action</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach items="${campuses}" var="campus">
							<tr>
								<td id=<c:out value="${campus.id}"/> ><c:out value="${campus.name}" /></td>
								<td><c:out value="${campus.location}" /></td>
								<td class='btnsettings'><input
									id='<c:out value="${campus.id}"/>' type="button"
									onclick="edit(this);"
									value="<spring:message code='action.edit'/>" />
									&nbsp; 
									<input id='<c:out value="${campus.id}"/>' type="button"
									onclick='deleteOrg(this);'
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
	<div class="navdiv" align="center">
		<c:if test="${mode != 'admin'}">
			<spring:url value="companySetup.ems" var="prevURL" scope="request" />
			<spring:url value="createBuilding.ems" var="nextURL" scope="request" />
			
			<table border="0" cellpadding="0" cellspacing="0" >
				<tr>
					<td align="right">
						<form method="post" action="${prevURL}">
							<input class="navigation" type="submit"
								value="<spring:message code='label.prev'/>"/>
						</form>
					</td>
					<td style="width:30px"></td>
					<td align="left">
						<c:if test="${!empty campuses}">
							<form method="post" action="${nextURL}">
								<input class="navigation" type="submit"
									value="<spring:message code='label.next'/>"/>
							</form>
						</c:if>
					</td>
				</tr>
			</table>			
		</c:if>
	</div>
</div>