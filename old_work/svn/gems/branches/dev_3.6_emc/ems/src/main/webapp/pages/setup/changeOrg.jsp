<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>

<spring:url value="/scripts/jquery/jquery.cookie.20110708.js" var="jquery_cookie"></spring:url>
<script type="text/javascript" src="${jquery_cookie}"></script>

<script type="text/javascript"> 
	function resetOrg() {
		$('#orgname').val('${org.name}');
		$('#orgname').removeClass('error');
		$('#planMap').removeClass('error');
		$('#editOrg').find(".error").html('');
		$('#sizeerror').html('');
		$('#planMap').val('');
		$("#uploadconfirm").html('');
		$("#uploadconfirm").hide();
	}
	
	function conformFloorPlanUpgradeForm(){
	 	
		var ifArea = "${page == 'area'}";
		
		if (ifArea == "true"){
			if (  $("#planMap").val() == '' ){
	 			return true;
	 		}else{
	 			var proceed = confirm("Uploading image of different pixel dimensions will have the following problems.\n - A smaller size image will lead to Fixtures going out of bound of floor plan and will not be visible.\n - A bigger size image will lead to Fixtures changing position on floor plan.\nAre you sure you want to upload a new floor plan image ?");
	 			if(proceed == true){
	 				return true;
		 		}else{
		 			$('#planMap').val('');
		 			return false;
		 		}
	 		}
		}else{
			return true;
		}
		
 	}
	
	
</script>

<div id="editOrg" style="padding:10px 0px 0px 0px; height:100% !important;background:#fff">
	 <div class="upperdiv" style="height:100% !important; min-height:90px">
		<div style="padding:10px;">
			<form:form commandName="org" id="edit-org-name"  method="post" enctype="multipart/form-data" onsubmit="javascript: return conformFloorPlanUpgradeForm();">
				<form:hidden id="orgid" path="id"/>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="settings.organization"/></span></div>
					<div style="float:left"><form:input path="name" id="orgname" size="40" /> <c:if test="${page == 'just_area'}"><c:if test="${zoneSensorLicenseEnabled == 'true'}"><form:checkbox id="zoneSensorEnable" name="zoneSensorEnable" path="zoneSensorEnable" />  Enable as Zone Sensor </c:if></c:if></div>
				</div>
				
				<script type="text/javascript">
				 	$(document).ready(function() {
						var refreshTree = <%=request.getParameter("refreshTree")%>;
						if(refreshTree != null && refreshTree) {
							$.ajax({
								type: "GET",
								cache: false,
								async: false,
								url: '<spring:url value="/facilities/tree.ems"/>',
								dataType: "html",
								success: function(msg) {
									parent.removeclick();
									$('#facilityTreeViewDiv', window.parent.document).html($("#facilityTreeViewDiv", $(msg)).html());
									parent.loadTree();
									parent.nodeclick();
								}
							});
						}
					});
				 	
				</script>
				
				<c:if test="${page == 'area'}"> 
					<div class="field">
						<div class="formPrompt"><span><spring:message code="settings.file.id"/></span></div>
						<div style="float:left"><c:out value="${org.id}"></c:out> <br/></div>
					</div>
					<div class="field">
						<div class="formPrompt"><span><spring:message code="settings.file.site.id"/></span></div>
						<div style="float:left"><form:input path="siteId" id="siteId" size="10" maxLength="18"/> <br/></div>
					</div>
					<div class="field">
						<div class="formPrompt"><span><spring:message code="settings.file.name"/></span></div>
						<a style="float:left" target="_blank" href=<spring:url value='/services/org/floor/${org.id}'/>><c:out value="${org.floorPlanUrl}"></c:out> <br/></a>
					</div>
					<div class="field">
						<div class="formPrompt"><span><spring:message code="settings.file.size"/></span></div>
						<div style="float:left"><c:out value="${fp_size} bytes"></c:out> <br/></div>
					</div> 
					<div class="field">
						<div class="formPrompt"><span><spring:message code="settings.file.dimensions"/></span></div>
						<div style="float:left"><c:out value="${fp_width} x ${fp_height}"></c:out> <br/></div>
					</div> 
					<div class="field">
						<div class="formPrompt"><span><spring:message code="settings.file.uploadedOn"/></span></div>
						<div style="float:left">
							<c:choose>
							    <c:when test="${empty org.uploadedOn}">
							        <c:out value="-"></c:out>
							    </c:when>
							    <c:otherwise>
							    	<fmt:formatDate value="${org.uploadedOn}" pattern="yyyy-MM-dd hh:mm:ss"></fmt:formatDate>
							    </c:otherwise>
							</c:choose>
						<br/></div>
					</div> 
					<div class="field">
						<div class="formPrompt"><span><spring:message code="floorSetup.label.upload.plan" /> (< <c:out value="${floorplan_imagesize_limit}"></c:out> MB):</span></div>
						<div class="formValue">
							<form:input id="planMap" onChange="checksize(this);" name="planMap" path="planMap.fileData" type="file" accept="gif,png,jpeg,jpg"/>
							<span class="error" id="sizeerror"></span>
							<span class="confirm" id="uploadconfirm"></span>
						</div>
					</div>
				</c:if>
				<div class="field">
					<div class="formPrompt"><span></span></div>
					<div>
						<input class="navigation" id="orgsubmit" type="submit" value="<spring:message code='action.save'/>"></input>
						<input class="navigation" id="orgreset" type="button" onclick='resetOrg();' value="<spring:message code='action.reset'/>"></input>
					</div>
				</div>
			</form:form>
		</div>
	 </div>	
	</div>
	<c:if test="${page == 'campus'}">
		<script type="text/javascript">
			$().ready(function() {
				$.validator.addMethod("regx", function(value, element, regexpr) {          
				    return regexpr.test(value);
				}, "Please enter a valid Name. Allowed Special Characters are @ # - _ : . ,");
				
				$.validator.addMethod("orgnameregx", function(value, element, regexpr) {          
				    return regexpr.test(value);
				}, "Please enter a valid Name. Allowed Special Characters are @ # - _ : . ,");
				
				$("#edit-org-name").validate({
					
					rules: {
						name: {
							required: true,
							regx: /^[^|\\!?^<>~$%&*\/()\+=[\]{}`\'\";]+$/,
							duplicate: "",
							maxlength: 128
						},
						orgname: {
							required: true,
							orgnameregx: /^[^|\\!?^<>~$%&*\/()\+=[\]{}`\'\";]+$/,
							duplicate: "",
							maxlength: 128
						}
					},
					messages: {
						name: {
							required: '<spring:message code="error.above.field.required"/>',
							maxlength: '<spring:message code="error.invalid.name.maxlength"/>'
						},
						orgname: {
							required: '<spring:message code="error.above.field.required"/>',
							maxlength: '<spring:message code="error.invalid.name.maxlength"/>'
						}
					}
				});
			});
			$('#edit-org-name').attr('action', '<spring:url value="/admin/organization/editCompanyName.ems"/>');
		</script>
	</c:if>
	<c:if test="${page == 'building'}">
		<script type="text/javascript">
			var editCampus = '${org.name}';
			$().ready(function() {
				
				$.validator.addMethod("regx", function(value, element, regexpr) {          
				    return regexpr.test(value);
				}, "Please enter a valid Name. Allowed Special Characters are @ # - _ : . ,");
				
				jQuery.validator.addMethod("duplicateOrg", function(value, element) {
					value = $.trim(value);
					<c:forEach items="${campuses}" var="campus">
						if(value == "${campus.name}" && "${campus.name}" != editCampus) {
							return false;
						}
					</c:forEach>
					return true;
					}, '<spring:message code="error.duplicate.campus"/>');
				
				$("#edit-org-name").validate({
					rules: {
						name: {
							required: true,
							regx: /^[^|\\!?^<>~$%&*\/()\+=[\]{}`\'\";]+$/,
							duplicateOrg: "",
							maxlength: 128
						}
						
					},
					messages: {
						name: {
							required: '<spring:message code="error.above.field.required"/>',
							maxlength: '<spring:message code="error.invalid.name.maxlength"/>'
						}
					}
				});
	
	
			});
			$('#edit-org-name').attr('action', '<spring:url value="/admin/organization/editCampusName.ems"/>');
		</script>
	</c:if>
	<c:if test="${page == 'floor'}">
		<script type="text/javascript">
			var editBuilding = '${org.name}';
			$().ready(function() {
				
				$.validator.addMethod("regx", function(value, element, regexpr) {          
				    return regexpr.test(value);
				}, "Please enter a valid Name. Allowed Special Characters are @ # - _ : . ,");
				
				jQuery.validator.addMethod("duplicateOrg", function(value, element) {
					value = $.trim(value);
					<c:forEach items="${buildings}" var="building">
						if(value == "${building.name}" && "${building.name}" != editBuilding) {
							return false;
						}
					</c:forEach>
					return true;
					}, '<spring:message code="error.duplicate.building"/>');
				
				$("#edit-org-name").validate({
					rules: {
						name: {
							required: true,
							regx: /^[^|\\!?^<>~$%&*\/()\+=[\]{}`\'\";]+$/,
							duplicateOrg: "",
							maxlength: 128
						}		
					},
					messages: {
						name: {
							required: '<spring:message code="error.above.field.required"/>',
							maxlength: '<spring:message code="error.invalid.name.maxlength"/>'
						}
					}
				});
	
	
			});
			$('#edit-org-name').attr('action', '<spring:url value="/admin/organization/editBuildingName.ems"/>');
		</script>
	</c:if>
	<c:if test="${page == 'area'}">
		<script type="text/javascript">
			var editFloor = '${org.name}';
			var uploadFloorConfirmation = <%=request.getParameter("upload")%>;
			if(uploadFloorConfirmation != null && uploadFloorConfirmation) {
				$("#uploadconfirm").show();
				$("#uploadconfirm").html("<spring:message code='settings.floor.upload.confirmation'/>");
				parent.parent.getFloorPlanObj("floorplan").floorPlanRefresh();
			}
			else {
				$("#uploadconfirm").hide();
			}
			if(uploadFloorConfirmation != null && !uploadFloorConfirmation){
				$('#sizeerror').show();
				$('#sizeerror').html('<spring:message code="error.upload.floor.plan.size"/> <c:out value="${floorplan_imagesize_limit}"></c:out> MB');
			}
			
			$().ready(function() {
				
				$.validator.addMethod("regx", function(value, element, regexpr) {          
				    return regexpr.test(value);
				}, "Please enter a valid Name. Allowed Special Characters are @ # - _ : . ,");
				
				jQuery.validator.addMethod("duplicateOrg", function(value, element) {
					value = $.trim(value);
					<c:forEach items="${floors}" var="floor">
						if(value == "${floor.name}" && "${floor.name}" != editFloor) {
							return false;
						}
					</c:forEach>
					return true;
					}, '<spring:message code="error.duplicate.floor"/>');
				
				jQuery.validator.addMethod("positiveIntegerOnly", function(value, element) {
				    return value != undefined && value != null && value.trim() != '' && /^([0-9]+)?$/i.test(value) && value > 0;
				}, "Please enter positive integer only");
				
				$("#edit-org-name").validate({
					rules: {
						name: {
							required: true,
							regx: /^[^|\\!?^<>~$%&*\/()\+=[\]{}`\'\";]+$/,
							duplicateOrg: "",
							maxlength: 128
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
						siteId : {
							required: '<spring:message code="error.above.field.required"/>',
							positiveIntegerOnly : "Please enter positive integer only"
						}
					}
				});
	
	
			});
			$('#edit-org-name').attr('action', '<spring:url value="/admin/organization/editFloorName.ems"/>');
			
			
			function checksize(obj) { 
				$("#uploadconfirm").html('');
				$("#uploadconfirm").hide();
				$('#sizeerror').hide();
				
				var floorplan_imagesize_limit_kb = "${floorplan_imagesize_limit}" * 1024 * 1024;
				
				if(obj.files[0].size > floorplan_imagesize_limit_kb) {
					$('#planMap').val('');
					$('#sizeerror').show();
					$('#sizeerror').html('<spring:message code="error.upload.floor.plan.size"/> <c:out value="${floorplan_imagesize_limit}"></c:out> MB' );
				}
		    }
		</script>
		
	</c:if>
	
	<c:if test="${page == 'just_area'}">
		<script type="text/javascript">
			var editArea = '${org.name}';
			$().ready(function() {
				
				$.validator.addMethod("regx", function(value, element, regexpr) {          
				    return regexpr.test(value);
				}, "Please enter a valid Name. Allowed Special Characters are @ # - _ : . ,");
				
				jQuery.validator.addMethod("duplicateOrg", function(value, element) {
					value = $.trim(value);
					<c:forEach items="${areas}" var="area">
						if(value == "${area.name}" && "${area.name}" != editArea) {
							return false;
						}
					</c:forEach>
					return true;
					}, '<spring:message code="error.duplicate.area"/>');
				
				$("#edit-org-name").validate({
					rules: {
						name: {
							required: true,
							regx: /^[^|\\!?^<>~$%&*\/()\+=[\]{}`\'\";]+$/,
							duplicateOrg: "",
							maxlength: 128
						}		
					},
					messages: {
						name: {
							required: '<spring:message code="error.above.field.required"/>',
							maxlength: '<spring:message code="error.invalid.name.maxlength"/>'
						}
					}
				});
	
	
			});
			$('#edit-org-name').attr('action', '<spring:url value="/admin/organization/editAreaName.ems"/>');
		</script>
	</c:if>
	