<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<spring:url value="/scripts/jquery/jquery.cookie.20110708.js" var="jquery_cookie"></spring:url>
<script type="text/javascript" src="${jquery_cookie}"></script>

<spring:url value="/scripts/jquery/jstree/jquery.jstree.js"	var="jquery_jstree"></spring:url>
<script type="text/javascript" src="${jquery_jstree}"></script>

<spring:url value="/scripts/jquery/jstree/themes/" var="jstreethemefolder"></spring:url>


<style>
	#tenantfacilityTreeViewDiv select{height:22px; margin-left:5px; width: 120px;}
	#tenantfacilityTreeViewDiv li { min-height:24px; line-height:24px; }
	#tenantfacilityTreeViewDiv a { line-height:20px; height:20px; color: black !important; font-weight: bold !important;}
	#tenantfacilityTreeViewDiv a ins { height:20px; width:20px; }
	#tenantfacilityTreeViewDiv .jstree-clicked { background-color: #FFFFFF !important; border: 1px solid #FFFFFF !important; }  
	#tenantfacilityTreeViewDiv .jstree-hovered { background-color: #FFFFFF !important; border: 1px solid #FFFFFF !important; }  
	button {padding: 0 5px;}
</style>

<script type="text/javascript">
INITIAL_TENANTS = {}; //Global variable to store tenants assignments before modifing

    $(document).ready(function() {
    	
    	$(".outermostdiv").css("overflow", "auto");
    	$(".outermostdiv").css("height", $(window).height() - 118);
        $(".outermostdiv").css("border-top" , "5px solid #CCCCCC");
    	
    	//Save initial assignments
    	INITIAL_TENANTS = getFormValuesJSON("tenantfacilityAssignmentForm"); 
    	
    	$('#facilitysubmitButton').bind('click', function() {
	        var modified_ids = getModifiedTenants();
//    			alert(modified_ids);
			if(modified_ids.length > 0){
		         $('#selectedFacilities').val(modified_ids);
		         $('#treeSubmitForm').submit();
			}
    	});


    	//Init Tree
        $("#tenantfacilityTreeViewDiv").bind("loaded.jstree", function (event, data) {
        	$("#tenantfacilityTreeViewDiv").jstree("open_all");
        });
    	
    	$.jstree._themes = "${jstreethemefolder}";
        $("#tenantfacilityTreeViewDiv").jstree({
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
                open_node : function () {event.stopImmediatePropagation(); return false;},
                close_node : function () {event.stopImmediatePropagation(); return false;}
            }, */
            types: {
                types : {
                    company : {
                        icon : {
                       	 image : '../themes/default/images/company.png'
                        }
                    },
                    campus : {
                        icon : {
                       	 image : '../themes/default/images/campus.png'
                        }
                    },
                    building : {
                        icon : {
                       	 image : '../themes/default/images/building.png'
                        }
                    },
                    floor : {
                        icon : {
                       	 image : '../themes/default/images/floor.png'
                        }
                    },
                    default : {
                        icon : {
                            image : '../themes/default/images/area.png'
                        },
                        valid_children : 'default',
                        /* select_node : function (e) { 
                            this.toggle_node(e); 
                        } */
                    }
                }
            },
            plugins : [ "themes", "html_data",   "ui", "types"] //"cookies" ,
       });
        
        $('#gotoTenantPage').click(function() {
        	<spring:url value="../tenants/list.ems" var="tenantPageUrl" scope="request" />
         	window.location="${tenantPageUrl}";
        });         
    });
    
    
    function comboChange(cmb){
    	//Update tree downward
    	$(cmb).siblings("ul").find("select").each(function(){
    		$(this).val(cmb.value);
    	});
    	
    	//Update tree upward
    	updateTreeUpward(cmb);
    }
    
    
    function updateTreeUpward(cmb){
    	$(cmb).parents("ul:first").siblings("select").each(function(){
    		// Update upward combo values
    		// Set to new Tenant if all other siblings combo have same Tenant
    		// else Reset to blank
        	var upCmbArr = $(this).siblings("ul").children("li");
			var isAllSet = true;
			upCmbArr.each(function(){
				if(cmb.value != $(this).children("select").val()){
					isAllSet = false;
				}
			});
			$(this).val(isAllSet ? cmb.value : 0);
			
			//Go upto root
    		updateTreeUpward(this);
    	});
    }

	function getModifiedTenants() {
		var modified_ids = [];
		var newAssignments = $("#tenantfacilityAssignmentForm").serialize();
		var formValues = newAssignments.split("&");
		for (var j = 0; j < formValues.length; j++) {
			var cmbValues = formValues[j].split("=");
			var facilityId = cmbValues[0];
			var tenantId = cmbValues[1];
			//Check if it is modified
			if (INITIAL_TENANTS[facilityId] != tenantId) {
				modified_ids.push(facilityId + "_" + tenantId);
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
	
	function resetFacilityAssignmentTree(){
		for (var p in INITIAL_TENANTS) {
		    if (INITIAL_TENANTS.hasOwnProperty(p)) {
		        $('select[name="'+p+'"]').val(INITIAL_TENANTS[p]);
		    }
		}
		return false;
	}
</script>
<div class="outermostdiv">
	<div class="outerContainer">
		<span>Assign facility to Tenants</span>
		<div class="i1"></div>
	</div>
	
	<div class="innerdiv">
	<button id="facilitysubmitButton">Save</button>
	<button onclick="javascript:resetFacilityAssignmentTree();">Undo</button>
	<button id="gotoTenantPage">Back To Tenants</button>
	<div style="height:5px;">&nbsp</div>
	
	<spring:url value="/facilities/save_tenant_locations.ems" var="saveFacilityAssignment" scope="request" />
	<form id="tenantfacilityAssignmentForm" action="${saveFacilityAssignment}" METHOD="POST">
	
	<c:set var='tenantOptions' value="${showBacnet}"/>
	
	
		<div id="tenantfacilityTreeViewDiv">
				<ul>
					<c:forEach items='${facilityTreeHierarchy.treeNodeList}' var='node1'>
						<li rel="${node1.nodeType.lowerCaseName}" id='<c:out value="${node1.nodeType.lowerCaseName}"/>_${node1.nodeId}'><a href='#'><c:out value="${node1.name}" escapeXml="true"/></a>
						<select name="${node1.nodeType.lowerCaseName}_${node1.nodeId}" onchange="javascript: comboChange(this);">
							<option value='0' <c:if test="${0 == node1.tenantid}">selected='selected'</c:if>>--</option>
							<c:forEach items="${tenantsList}" var="tenant">
								<option value='${tenant.id}' <c:if test="${tenant.id == node1.tenantid}">selected='selected'</c:if>>${tenant.name}</option>
							</c:forEach>
						</select>
							<ul>
								<c:forEach items='${node1.treeNodeList}' var='node2'>
									<li rel="${node2.nodeType.lowerCaseName}" id='<c:out value="${node2.nodeType.lowerCaseName}"/>_${node2.nodeId}'><a href='#'><c:out value="${node2.name}" escapeXml="true"/></a>
									<select name="${node2.nodeType.lowerCaseName}_${node2.nodeId}" onchange="javascript: comboChange(this);">
										<option value='0' <c:if test="${0 == node2.tenantid}">selected='selected'</c:if>>--</option>
										<c:forEach items="${tenantsList}" var="tenant">
											<option value='${tenant.id}' <c:if test="${tenant.id == node2.tenantid}">selected='selected'</c:if>>${tenant.name}</option>
										</c:forEach>
									</select>
										<ul>
											<c:forEach items='${node2.treeNodeList}' var='node3'>
												<li rel="${node3.nodeType.lowerCaseName}" id='<c:out value="${node3.nodeType.lowerCaseName}"/>_${node3.nodeId}'><a href=""><c:out value="${node3.name}" escapeXml="true"/></a>
												<select name="${node3.nodeType.lowerCaseName}_${node3.nodeId}" onchange="javascript: comboChange(this);">
													<option value='0' <c:if test="${0 == node3.tenantid}">selected='selected'</c:if>>--</option>
													<c:forEach items="${tenantsList}" var="tenant">
														<option value='${tenant.id}' <c:if test="${tenant.id == node3.tenantid}">selected='selected'</c:if>>${tenant.name}</option>
													</c:forEach>
												</select>
													<ul>
														<c:forEach items='${node3.treeNodeList}' var='node4'>
															<li rel="${node4.nodeType.lowerCaseName}" id='<c:out value="${node4.nodeType.lowerCaseName}"/>_${node4.nodeId}'><a href=""><c:out value="${node4.name}" escapeXml="true"/></a>
															<select name="${node4.nodeType.lowerCaseName}_${node4.nodeId}" onchange="javascript: comboChange(this);">
																<option value='0' <c:if test="${0 == node4.tenantid}">selected='selected'</c:if>>--</option>
																<c:forEach items="${tenantsList}" var="tenant">
																	<option value='${tenant.id}' <c:if test="${tenant.id == node4.tenantid}">selected='selected'</c:if>>${tenant.name}</option>
																</c:forEach>
															</select>
																<ul>
																	<c:forEach items='${node4.treeNodeList}' var='node5'>
																		<li rel="${node5.nodeType.lowerCaseName}" id='<c:out value="${node5.nodeType.lowerCaseName}"/>_${node5.nodeId}'><a href=""><c:out value="${node5.name}" escapeXml="true"/></a>
																		<select name="${node5.nodeType.lowerCaseName}_${node5.nodeId}" onchange="javascript: comboChange(this);">
																			<option value='0' <c:if test="${0 == node5.tenantid}">selected='selected'</c:if>>--</option>
																			<c:forEach items="${tenantsList}" var="tenant">
																				<option value='${tenant.id}' <c:if test="${tenant.id == node5.tenantid}">selected='selected'</c:if>>${tenant.name}</option>
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
	



	<form id="treeSubmitForm" action="${saveFacilityAssignment}" METHOD="POST">
		<input id="selectedFacilities" name="selectedFacilities" type="hidden"/>
		<input id="userId" name="userId" type="hidden" value="${userId}"/>
	</form>

</div>
</div>

