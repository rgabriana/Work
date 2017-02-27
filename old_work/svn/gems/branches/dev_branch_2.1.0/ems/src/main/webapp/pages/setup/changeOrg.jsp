<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
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
					<div style="float:left"><form:input path="name" id="orgname" size="40" /></div>
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
						<div class="formPrompt"><span><spring:message code="floorSetup.label.upload.plan" /></span></div>
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
				$("#edit-org-name").validate({
					rules: {
						name: "required"
					},
					messages: {
						name: '<spring:message code="error.above.field.required"/>'
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
							duplicateOrg: ""
						}
						
					},
					messages: {
						name: {
							required: '<spring:message code="error.above.field.required"/>'
						}
					}
				});
	
	
			});
			$('#edit-org-name').attr('action', '<spring:url value="/admin/organization/editCampusName.ems"/>');
			$('#orgname').attr('maxlength', "20");
		</script>
	</c:if>
	<c:if test="${page == 'floor'}">
		<script type="text/javascript">
			var editBuilding = '${org.name}';
			$().ready(function() {
				
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
							duplicateOrg: ""
						}		
					},
					messages: {
						name: {
							required: '<spring:message code="error.above.field.required"/>'
						}
					}
				});
	
	
			});
			$('#edit-org-name').attr('action', '<spring:url value="/admin/organization/editBuildingName.ems"/>');
			$('#orgname').attr('maxlength', "20");
		</script>
	</c:if>
	<c:if test="${page == 'area'}">
		<script type="text/javascript">
			var editFloor = '${org.name}';
			var uploadFloorConfirmation = <%=request.getParameter("upload")%>;
			if(uploadFloorConfirmation != null && uploadFloorConfirmation) {
				$("#uploadconfirm").show();
				$("#uploadconfirm").html("<spring:message code='settings.floor.upload.confirmation'/>");
			}
			else {
				$("#uploadconfirm").hide();
			}
			
			$().ready(function() {
				
				jQuery.validator.addMethod("duplicateOrg", function(value, element) {
					value = $.trim(value);
					<c:forEach items="${floors}" var="floor">
						if(value == "${floor.name}" && "${floor.name}" != editFloor) {
							return false;
						}
					</c:forEach>
					return true;
					}, '<spring:message code="error.duplicate.floor"/>');
				
				$("#edit-org-name").validate({
					rules: {
						name: {
							required: true,
							duplicateOrg: ""
						}		
					},
					messages: {
						name: {
							required: '<spring:message code="error.above.field.required"/>'
						}
					}
				});
	
	
			});
			$('#edit-org-name').attr('action', '<spring:url value="/admin/organization/editFloorName.ems"/>');
			$('#orgname').attr('maxlength', "20");
			
			
			function checksize(obj) { 
				$("#uploadconfirm").html('');
				$("#uploadconfirm").hide();
				$('#sizeerror').hide();
				
				if(obj.files[0].size > 1048576) {
					$('#planMap').val('');
					$('#sizeerror').show();
					$('#sizeerror').html('<spring:message code="error.upload.floor.plan.size"/>');
				}
		    }
		</script>
		
	</c:if>
	
	<c:if test="${page == 'just_area'}">
		<script type="text/javascript">
			var editArea = '${org.name}';
			$().ready(function() {
				
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
							duplicateOrg: ""
						}		
					},
					messages: {
						name: {
							required: '<spring:message code="error.above.field.required"/>'
						}
					}
				});
	
	
			});
			$('#edit-org-name').attr('action', '<spring:url value="/admin/organization/editAreaName.ems"/>');
			$('#orgname').attr('maxlength', "20");
		</script>
	</c:if>
	