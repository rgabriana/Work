<spring:url value="addFloor.ems" var="createURL" scope="request" />
<spring:url value="updateFloor.ems" var="updateURL" scope="request" />
<spring:url value="/admin/organization/addFloor.ems" var="createFloor"
	scope="request" />
<spring:url value="/admin/organization/updateFloor.ems"
	var="updateFloor" scope="request" />

<spring:url value="/scripts/jquery/jquery.alerts.js" var="jqueryalerts"></spring:url>
<script type="text/javascript" src="${jqueryalerts}"></script>

<spring:url value="/themes/standard/css/jquery/jquery.alerts.css"
	var="jqueryalertscss" />
<link rel="stylesheet" type="text/css" href="${jqueryalertscss}" />

<spring:url value="/scripts/jquery/jquery.jBreadCrumb.1.1.js"
	var="jquerybreadcrumb"></spring:url>
<script type="text/javascript" src="${jquerybreadcrumb}"></script>

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
	 $('#fourthStep').show();
	 $('#fifthStep').hide();
	 $('#sixthStep').hide();
	 var floors = null;
	 var loadBuilding = null;
</script>

<script type="text/javascript">

var editFloor = null;
var floorid = null;
var isSetup = "Y";
var isSave = false;

<c:if test="${mode == 'admin'}">
	isSetup = "N";
</c:if>

$().ready(function() {
	
	$.validator.addMethod("floorregx", function(value, element, regexpr) {          
	    return regexpr.test(value);
	}, "Please enter a valid Floor Name. Allowed Special Characters are @ # - _ : . ,");
	
	$.validator.addMethod("floordescregx", function(value, element, regexpr) {          
		if (value != "") {
			return regexpr.test(value);
		}
		return true;
	}, "Please enter a valid Floor Description. Allowed Special Characters are @ # - _ \\ / & * : . ,");
	
	jQuery.validator.addMethod("duplicate", function(value, element) {
		value = $.trim(value);
		for (var key in floors) {
			if(value.toLowerCase() == floors[key][0].toLowerCase() && floors[key][0] != editFloor) {
				return false;
			}
		}
		return true;
		}, '<spring:message code="error.duplicate.floor"/>');
	
	jQuery.validator.addMethod("positiveIntegerOnly", function(value, element) {
	    return value != undefined && value != null && value.trim() != '' && /^([0-9]+)?$/i.test(value) && value > 0;
	}, "Please enter positive integer only");
	
	$("#create-floor").validate({
		rules: {
			name: {
				required: true,
				floorregx: /^[^|\\!?^<>~$%&*\/()\+=[\]{}`\'\";]+$/,
				duplicate: "",
				maxlength: 128
			},
			description:
			{
				floordescregx: /^[^|!?^<>~$%\()\+=[\]{}`\'\";]+$/,
				maxlength: 511
			},
			siteId : {
				required: true,
				positiveIntegerOnly : true
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
			},
			siteId : {
				required: '<spring:message code="error.above.field.required"/>',
				positiveIntegerOnly : "Please enter positive integer only"
			}
		}
	});
	
	<c:if test="${mode != 'admin'}">
		$(".maindiv").css("height", $(window).height() - 205);
		$(".maindiv").css("overflow", "auto");
	</c:if>	
	
	<c:if test="${mode == 'admin'}">
		$(".maindiv").css("height", $(window).height() - 160);
		$(".maindiv").css("overflow", "auto");
	</c:if>
	
	var uploadFloorPlanConfirmation = <%=request.getParameter("upload")%>;
	
	if(uploadFloorPlanConfirmation != null && !uploadFloorPlanConfirmation){
		$('#planMap').val('');
		$('#sizeerror').show();
		$('#sizeerror').html('<spring:message code="error.upload.floor.plan.size"/> <c:out value="${floorplan_imagesize_limit}"></c:out> MB');
	}else{
		$('#sizeerror').hide();
	}
	
});

function listFloor(building) {
	$("#deleteStatus").html("");
	$('#floorList').html('');
	floors = {};
	$('#name').val('');
	$('#description').val('');
	$('#size').val('');
	$('#outerContainer').find(".error").html('');
	$("#name").removeClass('error');
	if(loadBuilding == null || loadBuilding == '') {
		loadBuilding = $(building).val();
	}
	
	$.ajax({
		type: "GET",
		cache: false,
		url: '<spring:url value="/services/org/floor/list/"/>'+loadBuilding,
		dataType: "xml",
		success: function(msg) {
			
			var count = $(msg).find('name').length;
			if(count > 0) {
				$("#submit").val("<spring:message code='action.create.another'/>");
				isSave = false;
				$("#tableContainer").show();
			}
			else {
				$("#submit").val("<spring:message code='action.create'/>");
				isSave = false;
				$("#tableContainer").hide();
			};
			$(msg).find('floor').each(function() {
				floors[$(this).find('id').text()] = new Array($(this).find('name').text(), 
						$(this).find('description').text(),$(this).find('siteId').text());
				$("<tr></tr>").html( 
						'<td id=' +
						$(this).find('id').text()  + "name >" + $(this).find('name').text() + '</td> <td>' + $(this).find('description').text() + "</td> <td class='btnsettings'> <input id=" +
						$(this).find('id').text() + " type='button' onclick='edit(this);' value=<spring:message code='action.edit'/> />&nbsp;<input id=" 
						+ $(this).find('id').text() +" type='button' onclick='deleteOrg(this);' value=<spring:message code='action.delete'/> /></td>"
				).appendTo("#floorList");

			});
		},
		error: function (msg){
			$("#tableContainer").hide();
		}
	});
	loadBuilding = null;
}

function listBuilding(camp) {
	$('#building').html('');
	$('#ifBuilding').show();
	$('.floorError').hide();
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
		error: function (){
			$('#ifBuilding').hide();
			$('.floorError').show();
			$("#tableContainer").hide();
		}
	});
	if(loadBuilding != null) {
		listFloor($('#building'));
	}
	
}

function checksize(obj) { 
	$('#sizeerror').hide();
	
	var floorplan_imagesize_limit_kb = "${floorplan_imagesize_limit}" * 1024 * 1024;
	
	if(obj.files[0].size > floorplan_imagesize_limit_kb) {
		$('#planMap').val('');
		$('#sizeerror').show();
		$('#sizeerror').html('<spring:message code="error.upload.floor.plan.size"/> <c:out value="${floorplan_imagesize_limit}"></c:out> MB');
	}
}

function edit(obj) {
	
	$("#deleteStatus").html("");
	<c:if test="${mode == 'admin'}">
		$('#create-floor').attr('action', '${updateFloor}'+"?ts="+new Date().getTime());
	</c:if>
	<c:if test="${mode != 'admin'}">
		$('#create-floor').attr('action', '${updateURL}'+"?ts="+new Date().getTime());
	</c:if>
	floorid = $(obj).attr('id');
	$('#id').val(floorid);
	for (var key in floors) {
		if(floorid == key) {
			editFloor = floors[key][0];
			$('#name').val(floors[key][0]);
			$('#description').val(floors[key][1]);
			$('#siteId').val(floors[key][2]);
			$('#planMap').val('');
		}
	}
	$('#outerContainer').find(".error").html('');
	$("#name").removeClass('error');
	$('#planMap').removeClass('error');
	$("#submit").val("<spring:message code='action.save'/>");
	isSave = true;
	$('#cancel').show();
	<c:if test="${mode != 'admin'}">
	$('#campus').attr('disabled', 'disabled');
	$('#building').attr('disabled', 'disabled');
	</c:if>
}

function back() {
	$('#outerContainer').find(".error").html('');
	$("#name").removeClass('error');
	$('#id').val('');
	<c:if test="${mode == 'admin'}">
		$('#create-floor').attr('action', '${createFloor}'+"?ts="+new Date().getTime());
	</c:if>
	<c:if test="${mode != 'admin'}">
		$('#create-floor').attr('action', '${createURL}'+"?ts="+new Date().getTime());
	</c:if>
	editFloor = null;
	$('#planMap').val('');
	$('#planMap').removeClass('error');
	$('#name').val("");
	$('#description').val("");
	$('#siteId').val("");
	$('#size').val("");
	$("#submit").val("<spring:message code='action.create.another'/>");
	isSave = false;
	$('#cancel').hide();
	<c:if test="${mode != 'admin'}">
		$('#campus').removeAttr('disabled');
		$('#building').removeAttr('disabled');
	</c:if>
}



function saveFloor(){
	
	$("#deleteStatus").html("");
	<c:if test="${mode != 'admin'}">
	$('#campusId').val($('#campus').val());
	$('#buildingId').val($('#building').val());
	</c:if>
	
}


function save() {
	
	if (isSetup == "N"){
		
		if(isSave == true){
		
			if ( $("#planMap").val() == ''){
				saveFloor();
			}else{
				
				var proceed = confirm("Uploading image of different pixel dimensions will have the following problems.\n - A smaller size image will lead to Fixtures going out of bound of floor plan and will not be visible.\n - A bigger size image will lead to Fixtures changing position on floor plan.\nAre you sure you want to upload a new floor plan image ?");
	 			if(proceed == true){
		 			saveFloor();
		 		}else{
		 			$('#planMap').val('');
		 			
		 		}
				
			}
			
		}else{
			
			saveFloor();
		}
	}else{
		saveFloor();
	}
	
}

function deleteOrg(obj) {
	var name;
	var id = $(obj).attr('id');
	for (var key in floors) {
		if(id == key) {
			name = floors[key][0];
		}
	}
	jConfirm('Are you sure you want to delete '+ name+"?",'<spring:message code="deletion.confirmation.title"/>',function(result){
        if(result) {
        	$.ajax({
        		type: "POST",
        		cache: false,
        		url: '<spring:url value="/services/org/floor/delete/"/>'+id,
        		dataType: "html",
        		success: function(msg) {
        			if(msg == "F") {
        				$("#deleteStatus").html("Floor '" + name  + "' has associated data. It cannot be deleted." );
        			}
        			else if(msg == "S") {
        				<c:if test="${mode == 'admin'}">
        					$('#deleteRefresh').attr('action', '<spring:url value="/admin/delete/refresh.ems"/>');
        					
        				</c:if>
        				<c:if test="${mode != 'admin'}">
        						$('#cid').val($('#campus').val());
								$('#bid').val($('#building').val());
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
		<form id="deleteRefresh" action=<spring:url value="deleteFloor.ems"/>>
			<input type="hidden" id="cid" name="cid" /> <input type="hidden"
				id="bid" name="bid" />
		</form>
		<c:if test="${mode != 'admin'}">
			<div class="titletxt">
				<spring:message code="setup.floor.header" />
			</div>
		</c:if>
		<c:if test="${mode == 'admin'}">
			<div class="breadCrumbTop module">
				<div id="breadCrumb" class="breadCrumb module">
					<ul>
						<li><span id="company"></span></li>
						<li><span id="campus"></span></li>
						<li><span id="building"></span></li>
						<li><span id="floor"><spring:message
									code="setup.floor.header" /></span></li>
					</ul>
				</div>
			</div>
		</c:if>
		<form:form id="create-floor" commandName="floor" method="post"
			action="${createURL}" enctype="multipart/form-data">
			<form:hidden id="id" name="id" path="id" />
			<form:hidden id="campusId" name="id" path="building.campus.id" />
			<!--This is required because html does not submit disabled select tag values. This is as per http://www.w3.org/TR/html4/interact/forms.html  -->
			<form:hidden id="buildingId" name="id" path="building.id" />
			<!--This is required because html does not submit disabled select tag values. This is as per http://www.w3.org/TR/html4/interact/forms.html  -->
			<c:if test="${mode != 'admin'}">
				<div class="field">
					<div class="formPrompt">
						<span><spring:message code="floorSetup.label.select.campus" /></span>
					</div>
					<div class="formValue">
						<form:select id="campus" onChange="listBuilding(this);"
							path="building.campus.id" name="campus">
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
									code="floorSetup.label.select.building" /></span>
						</div>
						<div class="formValue">
							<form:select id="building" onChange="listFloor(this);"
								path="building.id" name="building">
							</form:select>
						</div>
					</div>
				</c:if>
				<div class="field">
					<div class="formPrompt">
						<span><spring:message code="floorSetup.label.name" /></span>
					</div>
					<div class="formValue">
						<form:input id="name" name="name" 
							path="name" />
					</div>
				</div>
				<div class="field">
					<div class="formPrompt">
						<span><spring:message code="floorSetup.label.description" /></span>
					</div>
					<div class="formValue">
						<form:textarea id="description" name="description" path="description"></form:textarea>
					</div>
				</div>
				<div class="field">
					<div class="formPrompt">
						<span><spring:message code="settings.file.site.id" /></span>
					</div>
					<div class="formValue">
						<form:input id="siteId" name="siteId" path="siteId" maxLength="18"></form:input>
					</div>
				</div>
				<div class="field">
					<div class="formPrompt">
						<span><spring:message
								code="floorSetup.label.select.floor.size" /></span>
					</div>
					<div class="formValue">
						<select id="size" name="size">
							<option value="less">
								<spring:message code="floorSetup.size.option.less" />
							</option>
							<option value="greater">
								<spring:message code="floorSetup.size.option.greater" />
							</option>
						</select>
					</div>
				</div>
				<div class="field">
					<div class="formPrompt">
						<span><spring:message code="floorSetup.label.upload.plan" /> (< ${floorplan_imagesize_limit} MB):</span>
					</div>
					<div class="formValue">
						<form:input id="planMap" onChange="checksize(this);"
							name="planMap" path="planMap.fileData" type="file"
							accept="gif,png,jpeg,jpg" />
						<span class="error" id="sizeerror"></span>
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
		</form:form>

		<span class="floorError"><spring:message
				code="error.no.building" /></span>


		<script type="text/javascript">
		$('#sizeerror').hide();
		var selected_campus = <%=request.getParameter("default_selected_campus")%>;
		loadBuilding = <%=request.getParameter("default_selected_building")%>;
		if(selected_campus == null) {
			selected_campus = "${default_selected_campus}";
		}
		if(loadBuilding == null) {
			loadBuilding =  "${default_selected_building}";
		}
		<c:if test="${mode != 'admin'}">
		if(selected_campus != null || selected_campus != '') {
			$('#campus').val(selected_campus);			
		}
		</c:if>
		<c:if test="${mode == 'admin'}">
		$("#company").text("${companyName}");
		$('#create-floor').attr('action', '${createFloor}');
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
		<div style="text-align: center;padding-top: 15px;" >
				<span style="color: red;" id="deleteStatus"></span>
		</div>
		<div id="tableContainer" class="tableSettings" style="max-height: 175px; margin-top: 20px;">
			<table cellpadding="0" cellspacing="0" class="entable"
				style="width: 100%">
				<thead>
					<tr>
						<th><spring:message code="floorSetup.header.floor.name" /></th>
						<th><spring:message
								code="floorSetup.header.floor.description" /></th>
						<th class="alignright">Action</th>
					</tr>
				</thead>
				<tbody id="floorList">

				</tbody>
			</table>
		</div>
	</div>
	</div>
	<div class="navdiv" align="center">
		<c:if test="${mode != 'admin'}">
			<spring:url value="/createBuilding.ems" var="prevURL" scope="request" />
			<spring:url value="/createArea.ems" var="nextURL" scope="request" />

			<table border="0" cellpadding="0" cellspacing="0">
				<tr>
					<td align="right">
						<form method="post" action="${prevURL}">
							<input class="navigation" type="submit"
								value="<spring:message code='label.prev'/>" />
						</form>
					</td>
					<td style="width: 30px"></td>
					<td align="left"><c:if test="${floorcount != '0'}">
							<form method="post" action="${nextURL}">
								<input id="floorNext" class="navigation" type="submit"
									value="<spring:message code='label.next'/>" />
							</form>
						</c:if></td>
				</tr>
			</table>
		</c:if>
	</div>
</div>
<!-- <div style="height: 50px;"> -->
	<!-- dont delete this div -->
<!-- </div> -->