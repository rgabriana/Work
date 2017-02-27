<spring:url value="addBuilding.ems" var="createURL" scope="request" />
<spring:url value="updateBuilding.ems" var="updateURL" scope="request" />
<spring:url value="/admin/organization/addBuilding.ems"
	var="createBuilding" scope="request" />
<spring:url value="/admin/organization/updateBuilding.ems"
	var="updateBuilding" scope="request" />

<spring:url value="/scripts/jquery/jquery.alerts.js" var="jqueryalerts"></spring:url>
<script type="text/javascript" src="${jqueryalerts}"></script>

<spring:url value="/themes/standard/css/jquery/jquery.alerts.css"
	var="jqueryalertscss" />
<link rel="stylesheet" type="text/css" href="${jqueryalertscss}" />

<spring:url value="/scripts/jquery/jquery.jBreadCrumb.1.1.js"
	var="jquerybreadcrumb"></spring:url>
<script type="text/javascript" src="${jquerybreadcrumb}"></script>

<style type="text/css">
html,body {
	margin: 0px !important;
}

input[type="text"] {
	width: 300px;
	height: 22px;
}

select {
	width: 305px;
}
</style>

<script type="text/javascript">
	 $('#firstStep').hide();
	 $('#secondStep').hide();
	 $('#thirdStep').show();
	 $('#fourthStep').hide();
	 $('#fifthStep').hide();
	 var buildings = null;
</script>

<script type="text/javascript">

var editBuilding = null;
var buildingid = null;
var isSetup = "Y";

<c:if test="${mode == 'admin'}">
	isSetup = "N";
</c:if>

$().ready(function() {
	
	jQuery.validator.addMethod("duplicate", function(value, element) {
		value = $.trim(value);
		for (var key in buildings) {
			if(buildings[key].toLowerCase() == value.toLowerCase() && buildings[key] != editBuilding) {
				return false;
			}
		}
		return true;
		}, '<spring:message code="error.duplicate.building"/>');
	
	$("#create-building").validate({
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
		$(".maindiv").css("height", $(window).height() - 190);
		$(".maindiv").css("overflow", "auto");
	</c:if>	
	
	<c:if test="${mode == 'admin'}">
		$(".maindiv").css("height", $(window).height() - 160);
		$(".maindiv").css("overflow", "auto");
	</c:if>
});

function listBuilding(camp) {
	$("#deleteStatus").html("");
	$('#buildingList').html('');
	buildings = {};
	$('#name').val('');
	$('#outerContainer').find(".error").html('');
	$("#name").removeClass('error');
	<c:if test="${mode != 'admin'}">
	var campId = $(camp).val();
	</c:if>
	<c:if test="${mode == 'admin'}">
	var campId = $('#campusId').val();
	</c:if>
	$.ajax({
		type: "GET",
		cache: false,
		url: '<spring:url value="/services/org/building/list/"/>'+ campId,
		dataType: "xml",
		success: function(msg) {
			
			var count = $(msg).find('name').length;
			
			if(count > 0) {
				$("#submit").val("<spring:message code='action.create.another'/>");
				$("#tableContainer").show();
			}
			else {
				$("#submit").val("<spring:message code='action.create'/>");
				$("#tableContainer").hide();
			};
			$(msg).find('building').each(function() {
				buildings[$(this).find('id').text()] = $(this).find('name').text();
				$("<tr></tr>").html( 
						'<td id=' +
						$(this).find('id').text()  + "name >"+ $(this).find('name').text() + "</td><td class='btnsettings'> <input id=" +
						$(this).find('id').text() + " type='button' onclick='edit(this);' value=<spring:message code='action.edit'/> />&nbsp;<input id=" 
						+ $(this).find('id').text() +" type='button' onclick='deleteOrg(this);'  value=<spring:message code='action.delete'/> /></td>"
				).appendTo("#buildingList");
			});
		},
		error: function (){
			$("#tableContainer").hide();
		}
	});
}

function edit(obj) {
	$("#deleteStatus").html("");
	<c:if test="${mode == 'admin'}">
		$('#create-building').attr('action', '${updateBuilding}'+"?ts="+new Date().getTime());
	</c:if>
	<c:if test="${mode != 'admin'}">
		$('#create-building').attr('action', '${updateURL}'+"?ts="+new Date().getTime());
	</c:if>
	buildingid = $(obj).attr('id');
	$('#id').val(buildingid);
	for (var key in buildings) {
		if(buildingid == key) {
			editBuilding = buildings[key];
			$('#name').val(buildings[key]);
		}
	}
	$('#outerContainer').find(".error").html('');
	$("#name").removeClass('error');
	$("#submit").val("<spring:message code='action.save'/>");
	$('#cancel').show();
	<c:if test="${mode != 'admin'}">
	$('#campus').attr('disabled', 'disabled');
	</c:if>
}

function back() {
	$('#outerContainer').find(".error").html('');
	$("#name").removeClass('error');
	$('#id').val('');
	<c:if test="${mode == 'admin'}">
		$('#create-building').attr('action', '${createBuiliding}'+"?ts="+new Date().getTime());
	</c:if>
	<c:if test="${mode != 'admin'}">
		$('#create-building').attr('action', '${createURL}'+"?ts="+new Date().getTime());
	</c:if>
	editBuilding = null;
	$('#name').val("");
	$("#submit").val("<spring:message code='action.create.another'/>");
	$('#cancel').hide();
	<c:if test="${mode != 'admin'}">
		$('#campus').removeAttr('disabled');
	</c:if>
}

function save() {
	$("#deleteStatus").html("");
	<c:if test="${mode != 'admin'}">
	$('#campusId').val($('#campus').val());
	</c:if>
}

function deleteOrg(obj) {
	var name;
	var id = $(obj).attr('id');
	for (var key in buildings) {
		if(id == key) {
			name = buildings[key];
		}
	}
	jConfirm('Are you sure you want to delete '+ name + "?",'<spring:message code="deletion.confirmation.title"/>',function(result){
        if(result) {
        	$.ajax({
        		type: "POST",
        		cache: false,
        		url: '<spring:url value="/services/org/building/delete/"/>'+id,
        		dataType: "html",
        		success: function(msg) {
        			if(msg == "F") {
        				$("#deleteStatus").html("Building " + name  + " cannot be deleted. There exists atleast one floor under this building." );
        			}
        			else if(msg == "S") {
        				<c:if test="${mode == 'admin'}">
        					$('#deleteRefresh').attr('action', '<spring:url value="/admin/delete/refresh.ems"/>');
        					
        				</c:if>
        				<c:if test="${mode != 'admin'}">
        						$('#cid').val($('#campus').val());
        				</c:if>
        				$('#deleteRefresh').submit();
        			}
        		}
        	});
        }
	});
}

</script>
<div class="maindiv">
	<div>
		<div class="upperdiv" style="padding: 0px 10px 10px 10px;">
			<form id="deleteRefresh"
				action=<spring:url value="deleteBuilding.ems"/>>
				<input type="hidden" id="cid" name="cid" />
			</form>
			<c:if test="${mode != 'admin'}">
				<div class="titletxt">
					<spring:message code="setup.building.header" />
				</div>
			</c:if>

			<c:if test="${mode == 'admin'}">
				<div class="breadCrumbTop module">
					<div id="breadCrumb" class="breadCrumb module">
						<ul>
							<li><span id="company"></span></li>
							<li><span id="campus"></span></li>
							<li><span id="building"><spring:message
										code="setup.building.header" /></span></li>
						</ul>
					</div>
				</div>
			</c:if>
			<form:form id="create-building" commandName="building" method="post"
				action="${createURL}">
				<form:hidden id="id" name="id" path="id" />
				<form:hidden id="campusId" path="campus.id" />
				<!--This is required because html does not submit disabled select tag values. This is as per http://www.w3.org/TR/html4/interact/forms.html  -->
				<c:if test="${mode != 'admin'}">
					<div class="field">
						<div class="formPrompt">
							<span><spring:message
									code="buildingSetup.label.select.campus" /></span>
						</div>
						<div class="formValue">
							<form:select id="campus" onChange="listBuilding(this);"
								path="campus.id" name="campus">
								<form:options items="${campuses}" itemValue="id"
									itemLabel="name"></form:options>
							</form:select>
						</div>
					</div>
				</c:if>
				<div class="field">
					<div class="formPrompt">
						<span><spring:message
								code="buildingSetup.label.building.name" /></span>
					</div>
					<div class="formValue">
						<form:input id="name" name="name" maxlength="20"
							path="name" />
					</div>
				</div>
				<div class="field">
					<div class="formPrompt">
						<span></span>
					</div>
					<div>
						<input class="navigation" id="submit" onclick="save();"
							type="submit" value="<spring:message code='action.create'/>"></input>&nbsp;
						<input class="navigation" id="cancel" onclick="back();"
							type="button" value="<spring:message code='action.cancel'/>"></input>
					</div>
				</div>
			</form:form>

			<script type="text/javascript">
				var selected_campus = <%=request.getParameter("default_selected_campus")%>;
				if(selected_campus == null) {
					selected_campus = "${default_selected_campus}";
				}
				<c:if test="${mode != 'admin'}">
					if(selected_campus != null || selected_campus != '') {
						$('#campus').val(selected_campus);			
					}
				</c:if>
				<c:if test="${mode == 'admin'}">
					$("#company").html("${companyName}");
					$('#create-building').attr('action', '${createBuilding}');
					<c:forEach var="campus" items="${campuses}">
						if("${campus.id}" == selected_campus) {
							$("#campus").html("${campus.name}");
							$('#campusId').val("${campus.id}");
						}
					</c:forEach>
				</c:if>
				
				listBuilding($('#campus'));
				$('#cancel').hide();
				<c:if test="${mode == 'admin'}">
					$("#breadCrumb").jBreadCrumb({minimumCompressionElements: 6});
				</c:if>
			</script>

			<div id="tableContainer" class="tableSettings">
				<div style="text-align: center;">
					<span style="color: red;" id="deleteStatus"></span>
				</div>
				<table cellpadding="0" cellspacing="0" class="entable"
					style="width: 100%">
					<thead>
						<tr>
							<th><spring:message
									code="buildingSetup.header.building.name" /></th>
							<th class="alignright">Action</th>
						</tr>
					</thead>
					<tbody id="buildingList">

					</tbody>
				</table>
			</div>
		</div>
	</div>
	<div class="navdiv" align="center">
		<c:if test="${mode != 'admin'}">
			<spring:url value="createCampus.ems" var="prevURL" scope="request" />
			<spring:url value="createFloor.ems" var="nextURL" scope="request" />

			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td align="right">
						<form method="post" action="${prevURL}">
							<input class="navigation" type="submit"
								value="<spring:message code='label.prev'/>" />
						</form>
					</td>
					<td style="width: 30px"></td>
					<td align="left"><c:if test="${buildingcount != '0'}">
							<form method="post" action="${nextURL}">
								<input class="navigation" type="submit"
									value="<spring:message code='label.next'/>" />
							</form>
						</c:if></td>
				</tr>
			</table>
		</c:if>
	</div>
</div>
<!-- <div style="height: 50px;">
	dont delete this div
</div> -->