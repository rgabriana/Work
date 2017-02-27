<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<spring:url value="/scripts/jquery/jquery.cookie.20110708.js"
	var="jquery_cookie"></spring:url>
<script type="text/javascript" src="${jquery_cookie}"></script>

<spring:url value="/scripts/jquery/jstree/jquery.jstree.js"
	var="jquery_jstree"></spring:url>
<script type="text/javascript" src="${jquery_jstree}"></script>

<spring:url value="/scripts/jquery/jstree/themes/"
	var="jstreethemefolder"></spring:url>


<script type="text/javascript">
	INITIAL_DR_FACILiTY_VALUES = {}; //Global variable to store tenants assignments before modifing

	$(document)
			.ready(
					function() {
						//Save initial assignments
						INITIAL_DR_FACILiTY_VALUES = getFormValuesJSON("drfacilityAssignmentForm");

						$('#facilitysubmitButton').bind('click', function() {
							var modified_ids = getModifiedDRFacilities();
							//alert(modified_ids);
							$('#selectedFacilities').val(modified_ids);
							$('#treeSubmitForm').submit();						
						});

						//Init Tree
						$("#drfacilityTreeViewDiv").bind(
								"loaded.jstree",
								function(event, data) {
									$("#drfacilityTreeViewDiv").jstree(
											"open_all");
								});

						$.jstree._themes = "${jstreethemefolder}";
						$("#drfacilityTreeViewDiv").jstree(
								{
									core : {
										animation : 0
									},
									themes : {
										theme : "default"
									},
									checkbox :{
										//override_ui : true,
										real_checkboxes : true,
										real_checkboxes_names :function (n) { return [("check_" + (n[0].id || Math.ceil(Math.random() * 10000))), 1]; }
									},
									types: {
						                 types : {
						                     'company' : {
						                         icon : {
						                        	 image : '../themes/default/images/company.png'
						                         }
						                     },
						                     'campus' : {
						                         icon : {
						                        	 image : '../themes/default/images/campus.png'
						                         }
						                     },
						                     'building' : {
						                         icon : {
						                        	 image : '../themes/default/images/building.png'
						                         }
						                     },
						                     'floor' : {
						                         icon : {
						                        	 image : '../themes/default/images/floor.png'
						                         }
						                     },
						                     'default' : {
						                         icon : {
						                             image : '../themes/default/images/area.png'
						                         },
						                         valid_children : 'default'
						                     }
						                 }
						             },
									plugins : [ "themes", "html_data", "ui",
											"checkbox", "types" ]
								//"cookies" ,
								});
						
					});


	

	function getModifiedDRFacilities() {
		var modified_ids = [];
		$("#drfacilityTreeViewDiv").jstree("get_checked", null, true).each(
				function() {
					modified_ids.push(this.id);
				});

		return modified_ids;
	}

	function getFormValuesJSON(formId) {
		var serializeStr = $("#" + formId).serialize();
		var formJSON = {};
		var formValues = serializeStr.split("&");
		for ( var j = 0; j < formValues.length; j++) {
			var field = formValues[j].split("=");
			formJSON[field[0]] = field[1];
		}
		return formJSON;
	}

	function resetFacilityAssignmentTree() {
		for ( var p in INITIAL_DR_FACILiTY_VALUES) {
			if (INITIAL_DR_FACILiTY_VALUES.hasOwnProperty(p)) {
				$('select[name="' + p + '"]').val(INITIAL_DR_FACILiTY_VALUES[p]);
			}
		}
		return false;
	}
</script>
<div class="outermostdiv">
	<div class="outerContainer">
		<span>Assign facility</span>
		<div class="i1"></div>
	</div>

	<div class="innerdiv">
		<button id="facilitysubmitButton">Save</button>
		<button id="btnClose" onclick="closeAssignFacilityDRDialog()">
			<spring:message code="action.cancel" />
		</button>

		<div style="height: 5px;">&nbsp</div>

		<spring:url value="/dr/save_dr_facilities.ems"
			var="saveDRFacilityAssignment" scope="request" />
		<form id="drfacilityAssignmentForm"
			action="${saveDRFacilityAssignment}" METHOD="POST">
			
			<div id="drfacilityTreeViewDiv">
				<ul>
					<c:forEach items='${facilityTreeHierarchy.treeNodeList}'
						var='node1'>
						<li rel="${node1.nodeType.lowerCaseName}" <c:forEach items="${overrideSchedulesFacility}"
								var="overrideSchFac">
								<c:if
									test="${overrideSchFac.facilityId == node1.nodeId and overrideSchFac.facilityType.lowerCaseName == node1.nodeType.lowerCaseName}">class='jstree-checked'</c:if>
							</c:forEach> 
							id='<c:out value="${node1.nodeType.lowerCaseName}"/>_${node1.nodeId}' class="selected"><a
							href='#' disabled="true" id='link_<c:out value="${node1.nodeType.lowerCaseName}"/>_${node1.nodeId}'><c:out value="${node1.name}" escapeXml="true"/></a> 
							<ul>
								<c:forEach items='${node1.treeNodeList}' var='node2'>
									<li rel="${node2.nodeType.lowerCaseName}" <c:forEach items="${overrideSchedulesFacility}"
											var="overrideSchFac">
											<c:if
												test="${overrideSchFac.facilityId == node2.nodeId and overrideSchFac.facilityType.lowerCaseName == node2.nodeType.lowerCaseName}">class='jstree-checked'</c:if>
										</c:forEach> 
										id='<c:out value="${node2.nodeType.lowerCaseName}"/>_${node2.nodeId}'><a
										href='#' disabled="true" id='link_<c:out value="${node2.nodeType.lowerCaseName}"/>_${node2.nodeId}'><c:out value="${node2.name}" escapeXml="true"/></a>
										<ul>
											<c:forEach items='${node2.treeNodeList}' var='node3'>
												<li rel="${node3.nodeType.lowerCaseName}" <c:forEach
														items="${overrideSchedulesFacility}" var="overrideSchFac">
														<c:if
															test="${overrideSchFac.facilityId == node3.nodeId and overrideSchFac.facilityType.lowerCaseName == node3.nodeType.lowerCaseName}">class='jstree-checked'</c:if>
													</c:forEach>
													id='<c:out value="${node3.nodeType.lowerCaseName}"/>_${node3.nodeId}'><a
													href="#" disabled="true" id='link_<c:out value="${node3.nodeType.lowerCaseName}"/>_${node3.nodeId}'><c:out value="${node3.name}" escapeXml="true"/></a> 
													<ul>
														<c:forEach items='${node3.treeNodeList}' var='node4'>
															<li rel="${node4.nodeType.lowerCaseName}" <c:forEach
																	items="${overrideSchedulesFacility}" var="overrideSchFac">
																	<c:if
																		test="${overrideSchFac.facilityId == node4.nodeId and overrideSchFac.facilityType.lowerCaseName == node4.nodeType.lowerCaseName}">class='jstree-checked'</c:if>
																</c:forEach> 
																id='<c:out value="${node4.nodeType.lowerCaseName}"/>_${node4.nodeId}'><a
																href="#" disabled="true" id='link_<c:out value="${node4.nodeType.lowerCaseName}"/>_${node4.nodeId}'><c:out value="${node4.name}" escapeXml="true"/></a> 
																<ul>
																	<c:forEach items='${node4.treeNodeList}' var='node5'>
																		<li rel="${node5.nodeType.lowerCaseName}" <c:forEach
																				items="${overrideSchedulesFacility}" var="overrideSchFac">
																				<c:if
																					test="${overrideSchFac.facilityId == node5.nodeId and overrideSchFac.facilityType.lowerCaseName == node5.nodeType.lowerCaseName}">class='jstree-checked'</c:if>
																			</c:forEach> 
																			id='<c:out value="${node5.nodeType.lowerCaseName}"/>_${node5.nodeId}'><a
																			href="#" disabled="true" id='link_<c:out value="${node5.nodeType.lowerCaseName}"/>_${node5.nodeId}'><c:out value="${node5.name}" escapeXml="true"/></a></li>
																	</c:forEach>
																</ul></li>
														</c:forEach>
													</ul></li>
											</c:forEach>
										</ul></li>
								</c:forEach>
							</ul></li>
					</c:forEach>
				</ul>
</div>
</form>
	<form id="treeSubmitForm" action="${saveDRFacilityAssignment}" METHOD="POST">
		<input id="selectedFacilities" name="selectedFacilities" type="hidden"/>
		<input id="scheduleId" name="scheduleId" type="hidden" value="${scheduleId}"/>
	</form>
	
	</div>
</div>

