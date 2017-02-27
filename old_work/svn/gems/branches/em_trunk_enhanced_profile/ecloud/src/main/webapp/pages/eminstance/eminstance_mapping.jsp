<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<spring:url value="/scripts/jquery/jstree/jquery.jstree.js"	var="jquery_jstree"></spring:url>
<script type="text/javascript" src="${jquery_jstree}"></script>

<spring:url value="/scripts/jquery/jstree/themes/" var="jstreethemefolder"></spring:url>

<style>
	#cloudfacilityTreeViewDiv select{height:22px; margin-left:5px; width: 120px;}
	#cloudfacilityTreeViewDiv li { min-height:24px; line-height:24px; }
	#cloudfacilityTreeViewDiv a { line-height:20px; height:20px; color: black !important; font-weight: bold !important;}
	#cloudfacilityTreeViewDiv a ins { height:20px; width:20px; }
	#cloudfacilityTreeViewDiv .jstree-clicked { background-color: #FFFFFF !important; border: 1px solid #FFFFFF !important; }  
	#cloudfacilityTreeViewDiv .jstree-hovered { background-color: #FFFFFF !important; border: 1px solid #FFFFFF !important; }  
	button {padding: 0 5px;}
</style>

<script>
$().ready(function() {
	//Init Tree
    $("#cloudfacilityTreeViewDiv").bind("loaded.jstree", function (event, data) {
    	$("#cloudfacilityTreeViewDiv").jstree("open_all");
    });
	
	$.jstree._themes = "${jstreethemefolder}";
    $("#cloudfacilityTreeViewDiv").jstree({
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
                    valid_children : 'default',
                    /* select_node : function (e) { 
                        this.toggle_node(e); 
                    } */
                }
            }
        },
        plugins : [ "themes", "html_data",   "ui", "types"] //"cookies" ,
   });

    $('#gotoEmInstancePage').click(function() {
    	window.location="list.ems?customerId="+'${eminstance.customer.id}';	
    });
    
	$('#mappingsubmitButton').bind('click', function() {
        var modified_ids = getMapping();
//			alert(modified_ids);
		if(modified_ids.length > 0){
	         $('#selectedFacilities').val(modified_ids);
	         $('#treeSubmitForm').submit();
		}
	});
});

function campusComboChange(item)
{
	var strId = item.id;
	var cldcampusid = strId.split("_");
	
	<c:forEach items='${emfacilitytree.treeNodeList}' var='emcampus'>
		if(cldcampusid[1] == "${emcampus.nodeId}")
		{
			<c:forEach items='${emcampus.treeNodeList}' var='embuilding'>
				// Get the id of the select item for the em building
				var embldgid = "building_" + "${embuilding.nodeId}" + "_sel";
				var bldgList = document.getElementById(embldgid);
				while(bldgList.options.length)
				{
					bldgList.remove(0);
				}
				<c:forEach items="${cloudbuildinglist}" var="cloudbldg" varStatus="i">
					var i = 0;
					if (item.value == "${cloudbldg.cloudCampus.id}") {
					    var bldg=new Option("${cloudbldg.name}",i);
					    bldg.value = "${cloudbldg.id}";
					    bldgList.options.add(bldg);
					    i++;
					}
				</c:forEach>
				bldgComboChange(bldgList);
			</c:forEach>			
		}
	</c:forEach>
}

function bldgComboChange(item)
{
	alert("bldgComboChange");
	var strId = item.id;
	var cldbldgid = strId.split("_");
	
	<c:forEach items='${emfacilitytree.treeNodeList}' var='emcampus'>
		<c:forEach items='${emcampus.treeNodeList}' var='embuilding'>
			if(cldbldgid[1] == "${embuilding.nodeId}")
			{
				<c:forEach items='${embuilding.treeNodeList}' var='emfloor'>
					// Get the id of the select item for the em floor
					var emfloorid = "floor_" + "${emfloor.nodeId}" + "_sel";
					var floorList = document.getElementById(emfloorid);
					while(floorList.options.length)
					{
						floorList.remove(0);
					}
					<c:forEach items="${cloudfloorlist}" var="cloudfloor" varStatus="i">
						var i = 0;
						if (item.value == "${cloudfloor.cloudBuilding.id}") {
						    var floor=new Option("${cloudfloor.name}",i);
						    floor.value = "${cloudfloor.id}";
						    floorList.options.add(floor);
						    i++;
						}
					</c:forEach>
				</c:forEach>			
			}
		</c:forEach>			
	</c:forEach>
}

function getMapping() {
	var modified_ids = [];
	var newAssignments = $("#cloudfacilityAssignmentForm").serialize();
	alert(newAssignments);
/*	var formValues = newAssignments.split("&");
	for (var j = 0; j < formValues.length; j++) {
		var cmbValues = formValues[j].split("=");
		var facilityId = cmbValues[0];
		var tenantId = cmbValues[1];
		//Check if it is modified
		if (INITIAL_TENANTS[facilityId] != tenantId) {
			modified_ids.push(facilityId + "_" + tenantId);
		}
	}*/
	return modified_ids;
}

</script>
<div class="outermostdiv">
	<div class="outerContainer">
		<span>Facility mapping</span>
		<div class="i1"></div>
	</div>
	
	<div class="innerdiv">
	<button id="mappingsubmitButton">Save</button>
	<button onclick="javascript:resetFacilityAssignmentTree();">Undo</button>
	<button id="gotoEmInstancePage">Back To EmInstance</button>
	<div style="height:5px;">&nbsp</div>
	
	<spring:url value="/facilities/save_tenant_locations.ems" var="saveFacilityAssignment" scope="request" />
	<form id="cloudfacilityAssignmentForm" action="${saveFacilityAssignment}" METHOD="POST">
	
		<div id="cloudfacilityTreeViewDiv">
				<ul>
					<c:forEach items='${emfacilitytree.treeNodeList}' var='node1'>
						<li rel="${node1.nodeType.lowerCaseName}" id='<c:out value="${node1.nodeType.lowerCaseName}"/>_${node1.nodeId}'><a href='#'><c:out value="${node1.name}" escapeXml="true"/></a>
						<select name="${node1.nodeType.lowerCaseName}_${node1.nodeId}" id="${node1.nodeType.lowerCaseName}_${node1.nodeId}_sel"  onchange="javascript: campusComboChange(this);">
							<c:forEach items="${cloudcampuslist}" var="campus">
								<option value='${campus.id}' <c:if test="${campus.name == node1.name}">selected='selected'</c:if>>${campus.name}</option>
							</c:forEach>
						</select>
							<ul>
								<c:forEach items='${node1.treeNodeList}' var='node2'>
									<li rel="${node2.nodeType.lowerCaseName}" id='<c:out value="${node2.nodeType.lowerCaseName}"/>_${node2.nodeId}'><a href='#'><c:out value="${node2.name}" escapeXml="true"/></a>
									<select name="${node2.nodeType.lowerCaseName}_${node2.nodeId}" id="${node2.nodeType.lowerCaseName}_${node2.nodeId}_sel" onchange="javascript: bldgComboChange(this);">
										<c:forEach items="${cloudbuildinglist}" var="bldg">
											<option value='${bldg.id}' <c:if test="${bldg.name == node1.name}">selected='selected'</c:if>>${bldg.name}</option>
										</c:forEach>
									</select>
										<ul>
											<c:forEach items='${node2.treeNodeList}' var='node3'>
												<li rel="${node3.nodeType.lowerCaseName}" id='<c:out value="${node3.nodeType.lowerCaseName}"/>_${node3.nodeId}'><a href=""><c:out value="${node3.name}" escapeXml="true"/></a>
												<select name="${node3.nodeType.lowerCaseName}_${node3.nodeId}" id="${node3.nodeType.lowerCaseName}_${node3.nodeId}_sel" onchange="javascript: floorComboChange(this);">
													<c:forEach items="${cloudfloorlist}" var="floor">
														<option value='${floor.id}' <c:if test="${floor.name == node3.name}">selected='selected'</c:if>>${floor.name}</option>
													</c:forEach>
												</select>
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
