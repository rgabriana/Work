<spring:url value="addArea.ems" var="createURL" scope="request" />
<spring:url value="updateArea.ems" var="updateURL" scope="request" />
<spring:url value="/admin/organization/addArea.ems" var="createArea"
	scope="request" />
<spring:url value="/admin/organization/updateArea.ems" var="updateArea"
	scope="request" />

<spring:url value="/scripts/jquery/jquery.alerts.js" var="jqueryalerts"></spring:url>
<script type="text/javascript" src="${jqueryalerts}"></script>

<spring:url value="/scripts/jquery/jquery.jBreadCrumb.1.1.js"
	var="jquerybreadcrumb"></spring:url>
<script type="text/javascript" src="${jquerybreadcrumb}"></script>

<spring:url value="/themes/standard/css/jquery/jquery.alerts.css"
	var="jqueryalertscss" />
<link rel="stylesheet" type="text/css" href="${jqueryalertscss}" />

<style type="text/css">
	html,body {	margin: 0px !important;}
	input[type="text"]{width:300px; height:22px;}
	input[type="file"]{width:305px; height:22px;}
	select{width:305px;}
	textarea{width:300px; height:50px;}
</style>

<script type="text/javascript">
	 $('#firstStep').hide();
	 $('#secondStep').hide();
	 $('#thirdStep').hide();
	 $('#fourthStep').hide();
	 $('#fifthStep').show();
	 var areas = null;
	 var loadBuilding = null;
	 var loadFloor = null;
</script>

<script type="text/javascript">

var editArea = null;
var areaid = null;
var isSetup = "Y";

<c:if test="${mode == 'admin'}">
	isSetup = "N";
</c:if>

$().ready(function() {
	
	jQuery.validator.addMethod("duplicate", function(value, element) {
		value = $.trim(value);
		for (var key in areas) {
			if(value.toLowerCase() == areas[key][0].toLowerCase() && areas[key][0] != editArea) {
				return false;
			}
		}
		return true;
		}, '<spring:message code="error.duplicate.area"/>');
	
	$("#create-area").validate({
		rules: {
			name: {
				required: true,
				duplicate: "",
				maxlength: 128
			},
			description:
			{
				maxlength: 511
			}
		},
		messages: {
			name: {
				required: '<spring:message code="error.above.field.required"/>',
				maxlength: '<spring:message code="error.invalid.name.maxlength"/>'
			},
			description:
			{
				maxlength: '<spring:message code="error.invalid.maxlength"/>'
			}
		}
	});

	<c:if test="${mode != 'admin'}">
		$(".maindiv").css("height", $(window).height() - 205);
		$(".maindiv").css("overflow", "auto");
	</c:if>	
	
	<c:if test="${mode == 'admin'}">
		$(".maindiv").css("height", $(window).height() - 290);
		$(".maindiv").css("overflow", "auto");
	</c:if>
});

function listBuilding(camp) {
	$('#building').html('');
	$('.floorError').hide();
	$('.areaError').hide();
	$('#ifBuilding').show();
	<c:if test="${mode != 'admin'}">
	var campId = $(camp).val();
	</c:if>
	<c:if test="${mode == 'admin'}">
	var campId = $('#campusId').val();
	</c:if>
	$.ajax({
		type: "GET",
		cache: false,
		async: false,
		url: '<spring:url value="/services/org/building/list/"/>'+ campId,
		dataType: "xml",
		success: function(msg) {
			
			var count = $(msg).find('name').length;
			if(count > 0) {
				$(msg).find('building').each(function() {
					<c:if test="${mode != 'admin'}">
					if(loadBuilding != null) {
						if(loadBuilding == $(this).find('id').text()) {
							$("<option selected='selected' value='" + $(this).find('id').text() + "'></option>").html($(this).find('name').text()).appendTo("#building");
						}
						else {
							$("<option value='" + $(this).find('id').text() + "'></option>").html($(this).find('name').text()).appendTo("#building");
						}
					}
					else {
						loadBuilding = $(this).find('id').text();
						$("<option selected='selected' value='" + $(this).find('id').text() + "'></option>").html($(this).find('name').text()).appendTo("#building");
					}
					</c:if>
					<c:if test="${mode == 'admin'}">
					if(loadBuilding != null) {
						if(loadBuilding == $(this).find('id').text()) {
							$("#building").text($(this).find('name').text());
							$('#buildingId').val($(this).find('id').text());
						}
					}
					</c:if>
						
				});
			}
			else {
				$('#ifBuilding').hide();
				$('.floorError').show();
				$("#tableContainer").hide();
			}
		},
		error: function(){
			$('#ifBuilding').hide();
			$('.floorError').show();
			$("#tableContainer").hide();
		}
	});
	if(loadBuilding != null) {
		listFloor($('#building'));
	}
	
}

function listFloor(building) {
	$('#floor').html('');
	$('.areaError').hide();
	$('#ifFloor').show();
	<c:if test="${mode != 'admin'}">
	var buildingId = $(building).val();
	</c:if>
	<c:if test="${mode == 'admin'}">
	var buildingId = $('#buildingId').val();
	</c:if>
	$.ajax({
		type: "GET",
		cache: false,
		async: false,
		url: '<spring:url value="/services/org/floor/list/"/>'+ buildingId,
		dataType: "xml",
		success: function(msg) {
			
			var count = $(msg).find('name').length;
			if(count > 0) {
				$(msg).find('floor').each(function() {
					<c:if test="${mode != 'admin'}">
					if(loadFloor != null) {
						if(loadFloor == $(this).find('id').text()) {
							$("<option selected='selected' value='" + $(this).find('id').text() + "'></option>").html($(this).find('name').text()).appendTo("#floor");
						}
						else {
							$("<option value='" + $(this).find('id').text() + "'></option>").html($(this).find('name').text()).appendTo("#floor");
						}
					}
					else {
						loadFloor = $(this).find('id').text();
						$("<option selected='selected' value='" + $(this).find('id').text() + "'></option>").html($(this).find('name').text()).appendTo("#floor");
					}
					</c:if>
					<c:if test="${mode == 'admin'}">
					if(loadFloor != null) {
						if(loadFloor == $(this).find('id').text()) {
							$("#floor").text($(this).find('name').text());
							$('#floorId').val($(this).find('id').text());
						}
					}
					</c:if>
				});
			}
			else {
				$('#ifFloor').hide();
				$('.areaError').show();
				$("#tableContainer").hide();
			}
		},
		error: function() {			
			$("#tableContainer").hide();
		}
	});
	if(loadFloor != null) {
		listArea($('#floor'));
	}
	
}

function listArea(floor) {
	$("#deleteStatus").html("");
	areas = {};
	$('#areaList').html('');
	$('#name').val('');
	$('#description').val('');
	$('#outerContainer').find(".error").html('');
	$("#name").removeClass('error');
	if(loadFloor == null || loadFloor == '') {
		loadFloor = $(floor).val();
	}
	
	$.ajax({
		type: "GET",
		cache: false,
		url: '<spring:url value="/services/org/area/list/"/>'+loadFloor,
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
			$(msg).find('area').each(function() {
				areas[$(this).find('id').text()] = new Array($(this).find('name').text(), 
						$(this).find('description').text());
				$("<tr></tr>").html( 
						'<td id=' +
						$(this).find('id').text()  + "name >" + $(this).find('name').text() + '</td> <td>' + $(this).find('description').text() + "</td> <td class='btnsettings'> <input id=" +
						$(this).find('id').text() + " type='button' onclick='edit(this);' value=<spring:message code='action.edit'/> />&nbsp;<input id=" 
						+ $(this).find('id').text() +" type='button' onclick='deleteOrg(this);' value=<spring:message code='action.delete'/> /></td>"
				).appendTo("#areaList");

			});
		},
		error: function() {
			$("#submit").val("<spring:message code='action.create'/>");
			$("#tableContainer").hide();
		}
	});
	loadBuilding = null;
	loadFloor = null;
}

function edit(obj) {
	$("#deleteStatus").html("");
	<c:if test="${mode == 'admin'}">
		$('#create-area').attr('action', '${updateArea}'+"?ts="+new Date().getTime());
	</c:if>
	<c:if test="${mode != 'admin'}">
		$('#create-area').attr('action', '${updateURL}'+"?ts="+new Date().getTime());
	</c:if>
	areaid = $(obj).attr('id');
	$('#id').val(areaid);
	for (var key in areas) {
		if(areaid == key) {
			editArea = areas[key][0];
			$('#name').val(areas[key][0]);
			$('#description').val(areas[key][1]);
		}
	}
	$('#outerContainer').find(".error").html('');
	$("#name").removeClass('error');
	$("#submit").val("<spring:message code='action.save'/>");
	$('#cancel').show();
	<c:if test="${mode != 'admin'}">
	$('#campus').attr('disabled', 'disabled');
	$('#building').attr('disabled', 'disabled');
	$('#floor').attr('disabled', 'disabled');
	</c:if>
}

function back() {
	$('#outerContainer').find(".error").html('');
	$("#name").removeClass('error');
	$('#id').val('');
	<c:if test="${mode == 'admin'}">
		$('#create-area').attr('action', '${createArea}'+"?ts="+new Date().getTime());
	</c:if>
	<c:if test="${mode != 'admin'}">
		$('#create-area').attr('action', '${createURL}'+"?ts="+new Date().getTime());
	</c:if>
	editArea = null;
	$('#name').val("");
	$('#description').val("");
	$('#size').val("");
	$("#submit").val("<spring:message code='action.create.another'/>");
	$('#cancel').hide();
	<c:if test="${mode != 'admin'}">
		$('#campus').removeAttr('disabled');
		$('#building').removeAttr('disabled');
		$('#floor').removeAttr('disabled');
	</c:if>
}

function save() {
	$("#deleteStatus").html("");
	<c:if test="${mode != 'admin'}">
	$('#campusId').val($('#campus').val());
	$('#buildingId').val($('#building').val());
	$('#floorId').val($('#floor').val());
	</c:if>
}


function deleteOrg(obj) {
	var name;
	var id = $(obj).attr('id');
	for (var key in areas) {
		if(id == key) {
			name = areas[key][0];
		}
	}
	jConfirm('Are you sure you want to delete '+ name+"?",'<spring:message code="deletion.confirmation.title"/>',function(result){
        if(result) {
        	$.ajax({
        		type: "POST",
        		cache: false,
        		url: '<spring:url value="/services/org/area/delete/"/>'+id,
        		dataType: "html",
        		success: function(msg) {
        			if(msg == "F") {
        				$("#deleteStatus").html("Area '" + name + "' has associated data. It cannot be deleted." );
        			}
        			else if(msg == "S") {
        				<c:if test="${mode == 'admin'}">
        					$('#deleteRefresh').attr('action', '<spring:url value="/admin/delete/refresh.ems"/>');
        					
        				</c:if>
        				<c:if test="${mode != 'admin'}">
        						$('#cid').val($('#campus').val());
								$('#bid').val($('#building').val());
								$('#fid').val($('#floor').val());
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
		<form id="deleteRefresh" action=<spring:url value="deleteArea.ems"/>>
			<input type="hidden" id="cid" name="cid" /> <input type="hidden"
				id="bid" name="bid" /> <input type="hidden" id="fid" name="fid" />
		</form>

		<c:if test="${mode != 'admin'}">
			<div class="titletxt">
				<spring:message code="setup.area.header" />
			</div>
		</c:if>

		<c:if test="${mode == 'admin'}">
			<div class="breadCrumbTop module">
				<div id="breadCrumb" class="breadCrumb module">
					<ul>
						<li><span id="company"></span></li>
						<li><span id="campus"></span></li>
						<li><span id="building"></span></li>
						<li><span id="floor"></span></li>
						<li><span id="area"><spring:message
									code="setup.area.header" /></span></li>
					</ul>
				</div>
			</div>
		</c:if>
		<form:form id="create-area" commandName="area" method="post"
			action="${createURL}" enctype="multipart/form-data">
			<form:hidden id="id" name="id" path="id" />
			<form:hidden id="campusId" name="id" path="floor.building.campus.id" />
			<!--This is required because html does not submit disabled select tag values. This is as per http://www.w3.org/TR/html4/interact/forms.html  -->
			<form:hidden id="buildingId" name="id" path="floor.building.id" />
			<!--This is required because html does not submit disabled select tag values. This is as per http://www.w3.org/TR/html4/interact/forms.html  -->
			<form:hidden id="floorId" name="id" path="floor.id" />
			<!--This is required because html does not submit disabled select tag values. This is as per http://www.w3.org/TR/html4/interact/forms.html  -->
			<c:if test="${mode != 'admin'}">
				<div class="field">
					<div class="formPrompt">
						<span><spring:message code="areaSetup.label.select.campus" /></span>
					</div>
					<div class="formValue">
						<form:select id="campus" onChange="listBuilding(this);"
							path="floor.building.campus.id" name="campus">
							<form:options items="${campuses}" itemValue="id" itemLabel="name"></form:options>
						</form:select>
					</div>
				</div>
			</c:if>
			<div id="ifBuilding">
				<c:if test="${mode != 'admin'}">
					<div class="field">
						<div class="formPrompt">
							<span><spring:message
									code="areaSetup.label.select.building" /></span>
						</div>
						<div class="formValue">
							<form:select id="building" onChange="listFloor(this);"
								path="floor.building.id" name="building">
							</form:select>
						</div>
					</div>
				</c:if>
				<div id="ifFloor">
					<c:if test="${mode != 'admin'}">
						<div class="field">
							<div class="formPrompt">
								<span><spring:message code="areaSetup.label.select.floor" /></span>
							</div>
							<div class="formValue">
								<form:select id="floor" onChange="listArea(this);"
									path="floor.id" name="floor">
								</form:select>
							</div>
						</div>
					</c:if>
					<div class="field">
						<div class="formPrompt">
							<span><spring:message code="areaSetup.label.name" /></span>
						</div>
						<div class="formValue">
							<form:input id="name" name="name"
								path="name" />
						</div>
					</div>
					<div class="field">
						<div class="formPrompt">
							<span><spring:message code="areaSetup.label.description" /></span>
						</div>
						<div class="formValue">
							<form:textarea id="description" name="description" path="description"></form:textarea>
						</div>
					</div>
					<div class="field">
						<div class="formPrompt">
							<span></span>
						</div>
						<div>
							<input class="navigation" id="submit" type="submit"
								onclick="save();" value="<spring:message code='action.create'/>"></input>
							<input class="navigation" id="cancel" onclick="back();"
								type="button" value="<spring:message code='action.cancel'/>"></input>
						</div>
					</div>
				</div>
			</div>
		</form:form>

		<span class="floorError"><spring:message
				code="error.no.building" /></span> <span class="areaError"><spring:message
				code="error.no.floor" /></span>


		<script type="text/javascript">
		var selected_campus = <%=request.getParameter("default_selected_campus")%>;
		loadBuilding = <%=request.getParameter("default_selected_building")%>;
		loadFloor = <%=request.getParameter("default_selected_floor")%>;
		if(selected_campus == null) {
			selected_campus = "${default_selected_campus}";
		}
		if(loadBuilding == null) {
			loadBuilding =  "${default_selected_building}";
		}
		if(loadFloor == null) {
			loadFloor =  "${default_selected_floor}";
		}
		<c:if test="${mode != 'admin'}">
		if(selected_campus != null || selected_campus != '') {
			$('#campus').val(selected_campus);			
		}
		</c:if>
		<c:if test="${mode == 'admin'}">
		$("#company").text("${companyName}");
		$('#create-area').attr('action', '${createArea}');
		<c:forEach var="campus" items="${campuses}">
		if("${campus.id}" == selected_campus) {
			$("#campus").text("${campus.name}");
			$('#campusId').val("${campus.id}");
		}
		</c:forEach>
		</c:if>
		
		<c:if test="${mode != 'admin'}">
			if($('#campus').val() == null || $('#campus').val() == ''){
				$("#campus").prop('selectedIndex', 0);
			}
		</c:if>
		
		listBuilding($('#campus'));
		$('#cancel').hide();
		
		<c:if test="${mode == 'admin'}">
		$("#breadCrumb").jBreadCrumb({minimumCompressionElements: 6});
		</c:if>
	</script>
		<div style="text-align: center;">
				<span style="color: red;" id="deleteStatus"></span>
		</div>
		<div id="tableContainer" class="tableSettings" style="max-height: 175px; margin-top: 20px;">
			<table cellpadding="0" cellspacing="0" class="entable"
				style="width: 100%">
				<thead>
					<tr>
						<th><spring:message code="areaSetup.header.floor.name" /></th>
						<th><spring:message code="areaSetup.header.floor.description" /></th>
						<th class="alignright">Action</th>
					</tr>
				</thead>
				<tbody id="areaList">

				</tbody>
			</table>

		</div>
	</div>
	</div>
	<div class="navdiv" align="center">
		<c:if test="${mode != 'admin'}">
			<spring:url value="/createFloor.ems" var="prevURL" scope="request" />
			<spring:url value="/finishSetup.ems" var="finishURL" scope="request" />

			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td align="right">
						<form method="post" action="${prevURL}">
							<input class="navigation" type="submit"
								value="<spring:message code='label.prev'/>" />
						</form>
					</td>
					<td style="width: 30px"></td>
					<td align="left">
						<form method="post" action="${finishURL}">
							<input id="finishAllSetup" class="navigation" type="submit"
								value="<spring:message code='label.finish.setup'/>" />
						</form>
					</td>
				</tr>
			</table>
		</c:if>
	</div>

</div>
<!-- <div style="height: 50px;">
	dont delete this div
</div> -->