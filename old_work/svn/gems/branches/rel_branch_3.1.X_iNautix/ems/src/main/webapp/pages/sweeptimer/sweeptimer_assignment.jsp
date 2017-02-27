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

<style>
	#facilityTreeViewDiv select{height:22px; margin-left:5px; width: 10em;}
	#facilityTreeViewDiv li { min-height:24px; line-height:24px; }
	#facilityTreeViewDiv a { line-height:20px; height:20px; color: black !important; font-weight: bold !important;}
	#facilityTreeViewDiv a ins { height:20px; width:20px; }
	#facilityTreeViewDiv .jstree-clicked { background-color: #FFFFFF !important; border: 1px solid #FFFFFF !important; }  
	#facilityTreeViewDiv .jstree-hovered { background-color: #FFFFFF !important; border: 1px solid #FFFFFF !important; }  
</style>
<script type="text/javascript">
INITIAL_SWEEPTIMERS = {}; //Global variable to store tenants assignments before modifing
INITIAL_SWEEPTIMERS_STR = ""; //Global variable to store tenants assignments before modifing
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
var COLOR_FAULT = "red";
	$(document)
			.ready(
					function() {

				    	//Save initial assignments
				    	INITIAL_SWEEPTIMERS = getFormValuesJSON("sweepTimerAssignmentForm"); 
				    	INITIAL_SWEEPTIMERS_STR = $("#sweepTimerAssignmentForm").serialize();
				    	
				    	$('#sweeptimersubmitButton').bind('click', function() {
					        var modified_ids = getModifiedSweepTimers();
					        if(modified_ids!="")
				        	{
					        	$('#selectedSweepTimers').val(modified_ids);
								$('#sweepTimerTreeSubmitForm').submit();
				        	}else
			        		{
				        		displayLabelMessage("Please make a selection before saving.",COLOR_FAULT);
			        		}
						});

						$('#sweeptimerresetButton').bind('click', function() {
							var formValues = INITIAL_SWEEPTIMERS_STR.split("&");
							for (var j = 0; j < formValues.length; j++) {
					 			var cmbValues = formValues[j].split("=");
								var facilityId = cmbValues[0];
								var sweepTimerId = cmbValues[1];
								var selId = facilityId + "_sel";
								var selectObj = document.getElementById(selId);
								var i;
								for (i = 0; i < selectObj.length; i++)
								{
									if(selectObj.options[i].value == sweepTimerId)
									{
										selectObj.selectedIndex = i;
										break;
									}
								}
							}
						});

						$('#sweeptimerButton').bind('click', function() {
							window.location = "/ems/sweeptimer/list.ems";
						});

						//Init Tree
						$("#facilityTreeViewDiv").bind(
								"loaded.jstree",
								function(event, data) {
									$("#facilityTreeViewDiv").jstree(
											"open_all");
								});

						$.jstree._themes = "${jstreethemefolder}";
						$("#facilityTreeViewDiv").jstree(
								{
									core : {
										animation : 0
									},
									themes : {
										theme : "default"
									},
									  ui : {
									         select_limit : 1,
									         selected_parent_close : "false"
									 },
									/* types : {
										open_node : function() {
											event.stopImmediatePropagation();
											return false;
										},
										close_node : function() {
											event.stopImmediatePropagation();
											return false;
										}
									}, */
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
									plugins : [ "themes", "html_data", "ui","types", "xml_data" ]
								});

						$('#gotoTenantPage').click(function() {
							<spring:url value="../tenants/list.ems" var="tenantPageUrl" scope="request" />
							window.location = "${tenantPageUrl}";
						});
					});


    function comboChange(cmb){
    	clearLabelMessage();
    	// BUG ID : 1965 : child floor/area nodes should not get automatic associations : Hence commenting out default automcatic associtation
    	//Update tree downward
		//   $(cmb).siblings("ul").find("select").each(function(){
		//   	$(this).val(cmb.value);
		//   	});
    }
    
	function getModifiedSweepTimers() {
		var modified_ids = [];
		var newAssignments = $("#sweepTimerAssignmentForm").serialize();
		var formValues = newAssignments.split("&");
		for (var j = 0; j < formValues.length; j++) {
 			var cmbValues = formValues[j].split("=");
			var facilityId = cmbValues[0];
			var sweepTimerId = cmbValues[1];
			//Check if it is modified
			if (INITIAL_SWEEPTIMERS[facilityId] != sweepTimerId) {
				modified_ids.push(facilityId + "_" + sweepTimerId);
			}
		}
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
	function displayLabelMessage(Message, Color) {
	  		$("#sweep_message").html(Message);
	  		$("#sweep_message").css("color", Color);
	  	}
    function clearLabelMessage(Message, Color) {
  		displayLabelMessage("", COLOR_DEFAULT);
  	}
</script>

<div class="topmostContainer">
<div class="outermostdiv">
	<div class="outerContainer">
		<span>Assign Sweep Timer to Facility</span>
		<div class="i1"></div>
	</div>

	<div class="innerdiv">
		<button id="sweeptimersubmitButton" type="button">Save</button>
		<button id="sweeptimerresetButton" type="reset">Reset</button>
		<!--<button id="sweeptimerButton" type="button">Sweep Timers</button>-->

		<div id="sweep_message" style="font-size: 14px; font-weight: bold;padding: 5px 0 0 5px;" ></div>
	
		<div style="height: 5px;">&nbsp</div>

		<spring:url value="/sweeptimer/save_sweep_timer_assignment.ems"
			var="saveSweepTimerAssignment" scope="request" />
 		<form id="sweepTimerAssignmentForm"
			action="${saveFacilityAssignment}" METHOD="POST">

		<c:set var='tenantOptions' value="${showBacnet}" />


			<div id="facilityTreeViewDiv">
				<ul>
					<c:forEach items='${facilityTreeHierarchy.treeNodeList}' var='node1'>
						<li rel="${node1.nodeType.lowerCaseName}" id='<c:out value="${node1.nodeType.lowerCaseName}"/>_${node1.nodeId}'><a href='#'><c:out value="${node1.name}" escapeXml="true"/></a>
							<span style="padding: 0px 10px;"></span>
							<select id='${node1.nodeType.lowerCaseName}_${node1.nodeId}_sel' name="${node1.nodeType.lowerCaseName}_${node1.nodeId}" onchange="javascript: comboChange(this);">
							<option value='0' <c:if test="${0 == node1.sweepTimerId}">selected='selected'</c:if>>--</option>
							<c:forEach items="${sweepTimerList}" var="sweeptimer">
								<option value='${sweeptimer.id}' <c:if test="${sweeptimer.id == node1.sweepTimerId}">selected='selected'</c:if>>${sweeptimer.name}</option>
							</c:forEach>
						</select>
							<ul>
								<c:forEach items='${node1.treeNodeList}' var='node2'>
									<li rel="${node2.nodeType.lowerCaseName}" id='<c:out value="${node2.nodeType.lowerCaseName}"/>_${node2.nodeId}'><a href='#'><c:out value="${node2.name}" escapeXml="true"/></a>
									<span style="padding: 0px 10px;"></span>
									<select id="${node2.nodeType.lowerCaseName}_${node2.nodeId}_sel" name="${node2.nodeType.lowerCaseName}_${node2.nodeId}" onchange="javascript: comboChange(this);">
										<option value='0' <c:if test="${0 == node2.sweepTimerId}">selected='selected'</c:if>>--</option>
										<c:forEach items="${sweepTimerList}" var="sweeptimer">
											<option value='${sweeptimer.id}' <c:if test="${sweeptimer.id == node2.sweepTimerId}">selected='selected'</c:if>>${sweeptimer.name}</option>
										</c:forEach>
									</select>
										<ul>
											<c:forEach items='${node2.treeNodeList}' var='node3'>
												<li rel="${node3.nodeType.lowerCaseName}" id='<c:out value="${node3.nodeType.lowerCaseName}"/>_${node3.nodeId}'><a href=""><c:out value="${node3.name}" escapeXml="true"/></a>
												<span style="padding: 0px 10px;"></span>
												<select id="${node3.nodeType.lowerCaseName}_${node3.nodeId}_sel" name="${node3.nodeType.lowerCaseName}_${node3.nodeId}" onchange="javascript: comboChange(this);">
													<option value='0' <c:if test="${0 == node3.sweepTimerId}">selected='selected'</c:if>>--</option>
													<c:forEach items="${sweepTimerList}" var="sweeptimer">
														<option value='${sweeptimer.id}' <c:if test="${sweeptimer.id == node3.sweepTimerId}">selected='selected'</c:if>>${sweeptimer.name}</option>
													</c:forEach>
												</select>
													<ul>
														<c:forEach items='${node3.treeNodeList}' var='node4'>
															<li rel="${node4.nodeType.lowerCaseName}" id='<c:out value="${node4.nodeType.lowerCaseName}"/>_${node4.nodeId}'><a href=""><c:out value="${node4.name}" escapeXml="true"/></a>
															<span style="padding: 0px 10px;"></span>
															<select id="${node4.nodeType.lowerCaseName}_${node4.nodeId}_sel" name="${node4.nodeType.lowerCaseName}_${node4.nodeId}" onchange="javascript: comboChange(this);">
																<option value='0' <c:if test="${0 == node4.sweepTimerId}">selected='selected'</c:if>>--</option>
																<c:forEach items="${sweepTimerList}" var="sweeptimer">
																	<option value='${sweeptimer.id}' <c:if test="${sweeptimer.id == node4.sweepTimerId}">selected='selected'</c:if>>${sweeptimer.name}</option>
																</c:forEach>
															</select>
																<ul>
																	<c:forEach items='${node4.treeNodeList}' var='node5'>
																		<li rel="${node5.nodeType.lowerCaseName}" id='<c:out value="${node5.nodeType.lowerCaseName}"/>_${node5.nodeId}'><a href=""><c:out value="${node5.name}" escapeXml="true"/></a>
																		<span style="padding: 0px 10px;"></span>
																		<select id="${node5.nodeType.lowerCaseName}_${node5.nodeId}_sel" name="${node5.nodeType.lowerCaseName}_${node5.nodeId}" onchange="javascript: comboChange(this);">
																			<option value='0' <c:if test="${0 == node5.sweepTimerId}">selected='selected'</c:if>>--</option>
																			<c:forEach items="${sweepTimerList}" var="sweeptimer">
																				<option value='${sweeptimer.id}' <c:if test="${sweeptimer.id == node5.sweepTimerId}">selected='selected'</c:if>>${sweeptimer.name}</option>
																			</c:forEach>
																		</select>
																		</li>
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
	<form id="sweepTimerTreeSubmitForm" action="${saveSweepTimerAssignment}" METHOD="POST">
		<input id="selectedSweepTimers" name="selectedSweepTimers" type="hidden"/>
	</form>
	
	</div>
</div>
</div>

