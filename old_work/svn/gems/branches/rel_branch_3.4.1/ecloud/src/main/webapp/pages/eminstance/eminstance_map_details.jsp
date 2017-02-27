<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="ecloud" uri="/WEB-INF/tlds/ecloud.tld"%>

<spring:url value="/scripts/jquery/jquery.cookie.20110708.js" var="jquery_cookie"></spring:url>
<script type="text/javascript" src="${jquery_cookie}"></script>

<spring:url value="/scripts/jquery/jstree/jquery.jstree.js" var="jquery_jstree"></spring:url>
<script type="text/javascript" src="${jquery_jstree}"></script>

<spring:url value="/scripts/jquery/jstree/themes/" var="jstreethemefolder"></spring:url>

<spring:url value="/services/org/facilityemmap/emInst/" var="saveEmMappingUrl" scope="request" />

<spring:url value="/services/org/eminstance/emInstfacilityTree/" var="getEmInstfacilityTreeUrl" scope="request" />

<script type="text/javascript">

var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
var COLOR_FAILURE = "red";
var selectedEMFloorPath = "";
	
	$(document)
			.ready(
					function() {
						
						$('#facilitymapsubmitButton').bind('click', function() {
							
							displayLabelMessage('',COLOR_DEFAULT);
							
							var cloud_facility_string = getCloudFacilityId();
							
							var emInst_facility_string = getEmInstFacilityId();
							
							var emInst_facility_id = getIdfromString(emInst_facility_string);
							
							var cloud_facility_id = getIdfromString(cloud_facility_string);
							
							var emInstanceId = "${emInstanceId}";
							
							if(cloud_facility_id == undefined){
								displayLabelMessage('Please select a floor from Customer Cloud Facility Tree on the left.', COLOR_FAILURE);
							}else if (emInst_facility_id == undefined){
								displayLabelMessage('Please select a floor from EM Instance Facility Tree on the right.', COLOR_FAILURE);
							}else{
								var emFacilityPath = selectedEMFloorPath;
								displayLabelMessage('Please wait..Mapping is in progess', COLOR_DEFAULT);
								saveEmMapping(emInst_facility_id,cloud_facility_id,emInstanceId,emFacilityPath);
							}
						});
						
						displayLabelMessage('Please wait as EM Instance Facility Tree is loading...', COLOR_DEFAULT);
						
						
						//Init Customer Cloud Tree
						$("#customerCloudFacilityTreeViewDiv").bind(
								"loaded.jstree",
								function(event, data) {
									$(this).find('li[rel!=floor_unmapped]').find('.jstree-checkbox:first').hide();
									
									<c:forEach items="${facilityEmMappingList}" var="facilityEmMapping">
										var facilityId = "${facilityEmMapping.facilityId}";
										var id = "li[id = floor_"+facilityId.toString()+"]";
										$(this).find(id).find('.jstree-checkbox:first').hide();
									</c:forEach>
									$("#customerCloudFacilityTreeViewDiv").jstree("open_node", $("#organization_"+"${facilityTreeHierarchy.nodeId}"));
								});
						
						$("#customerCloudFacilityTreeViewDiv").bind("change_state.jstree", function(e, data){ 
					        if(data.inst.get_checked().length>1){ 
					                data.inst.uncheck_node(data.rslt[0]); 
					        } 
						}); 


						$.jstree._themes = "${jstreethemefolder}";
						$("#customerCloudFacilityTreeViewDiv").jstree(
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
										two_state : true, // Nessesary to disable default checking childrens
										real_checkboxes_names :function (n) { return [("check_" + (n[0].id || Math.ceil(Math.random() * 10000))), 1]; }
									},
									types: {
						                 types : {
						                     'organization' : {
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
						                     'floor_mapped' : {
						                         icon : {
						                        	 image : '../themes/default/images/floor.png'
						                         }
						                     },
						                     'floor_unmapped' : {
						                         icon : {
						                        	 image : '../themes/default/images/floorUnMapped.png'
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
						
						forceFitFacilityTreeViewDivs();
						
						getEmInstfacilityTree("${emInstanceId}");
						
					});
	
	function initializeEmFacilityTree(){
		//Init Em Instance Tree
		$("#emInstfacilityTreeViewDiv").bind(
				"loaded.jstree",
				function(event, data) {
					$("#emInstfacilityTreeViewDiv").jstree("open_all");
					$(this).find('li[rel!=floor_unmapped]').find('.jstree-checkbox:first').hide();
					
					<c:forEach items="${facilityEmMappingList}" var="facilityEmMapping">
						<c:if test="${facilityEmMapping.emId == emInstanceId}">
							var emFacilityId = "${facilityEmMapping.emFacilityId}";
							var id = "li[id = floor_"+emFacilityId.toString()+"]";
							$(this).find(id).find('.jstree-checkbox:first').hide();
						</c:if>
					</c:forEach>
					
				});
		
		$("#emInstfacilityTreeViewDiv").bind("change_state.jstree", function(e, data){ 
	        if(data.inst.get_checked().length>1){ 
	           data.inst.uncheck_node(data.rslt[0]); 
	        } 
	    });
		
		
		$("#emInstfacilityTreeViewDiv").bind('check_node.jstree', function(e, data) {
				var selectedFloorId = data.rslt.obj.attr("id");
				if(selectedFloorId.toString() == getEmInstFacilityId().toString()){
					var names = data.inst.get_path('#' + data.rslt.obj.attr('id'),false);
			        selectedEMFloorPath = "";
			        jQuery.each(names, function(index, item) {
			        	if(selectedEMFloorPath == ""){
			        		selectedEMFloorPath = item;
			        	}else{
			        		selectedEMFloorPath = selectedEMFloorPath + " --> " + item ;
			        	}
			        });
			    }
		});
		
		

		$.jstree._themes = "${jstreethemefolder}";
		$("#emInstfacilityTreeViewDiv").jstree(
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
						two_state : true, // Nessesary to disable default checking childrens
						real_checkboxes_names :function (n) { return [("check_" + (n[0].id || Math.ceil(Math.random() * 10000))), 1]; }
					},
					types: {
		                 types : {
		                     'organization' : {
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
		                     'floor_mapped' : {
		                         icon : {
		                        	 image : '../themes/default/images/floor.png'
		                         }
		                     },
		                     'floor_unmapped' : {
		                         icon : {
		                        	 image : '../themes/default/images/floorUnMapped.png'
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
		
	}
	
	var emInstfacilityTreeString = "<ul>";
	
	var emInstfacilityTreeData;
	
	function getEmInstfacilityTree(emInstanceId){
		
		$('#facilitymapsubmitButton').attr("disabled", true);
		
		$.ajax({
			url: "${getEmInstfacilityTreeUrl}"+emInstanceId+"?ts="+new Date().getTime(),
			dataType:"json",
			contentType: "application/json; charset=utf-8",
			success: function(data){
				if(data == null){
					$('#facilitymapsubmitButton').removeAttr("disabled");
					alert('Failed to load the EM Instance Facility Tree.Please try again');
					$('#mapEmInstanceDialog').html("");
					$('#mapEmInstanceDialog').dialog('close');
				}
				else if(data.nodeId == undefined){
					$('#facilitymapsubmitButton').removeAttr("disabled");
					alert('EM Activation Process is in Progress.Please try again after few Minutes');
					$('#mapEmInstanceDialog').html("");
					$('#mapEmInstanceDialog').dialog('close');
				}else{
					emInstfacilityTreeString = emInstfacilityTreeString + createChildFacilityTree(data);
					emInstfacilityTreeData = data;
					getCampusData(data.treeNodeList);
					emInstfacilityTreeString = emInstfacilityTreeString + "</li>";
					emInstfacilityTreeString = emInstfacilityTreeString + "</ul>";
					//console.log(emInstfacilityTreeString);
					$("#emInstfacilityTreeViewDiv").append(emInstfacilityTreeString);
					initializeEmFacilityTree();
					displayLabelMessage('',COLOR_DEFAULT);
					$('#facilitymapsubmitButton').removeAttr("disabled");
				}
				
			},
			error: function(){
				$('#facilitymapsubmitButton').removeAttr("disabled");
				alert('Failed to load EM Instance Facility Tree.Please try again');
				$('#mapEmInstanceDialog').html("");
				$('#mapEmInstanceDialog').dialog('close');
			}
		});
	}
	
	function getCampusData(campusData){
		emInstfacilityTreeString = emInstfacilityTreeString + "<ul>";
		var buildingData;
		if (Array.isArray(campusData)){
			for (var i=0; i< campusData.length; i++) {
				buildingData = campusData[i].treeNodeList;
				emInstfacilityTreeString = emInstfacilityTreeString + createChildFacilityTree(campusData[i]);
				//alert(campusData[i].name);
				getBuildingData(buildingData);
			}
		}else{
			buildingData = campusData.treeNodeList;
			//alert(campusData.name);
			emInstfacilityTreeString = emInstfacilityTreeString + createChildFacilityTree(campusData);
			getBuildingData(buildingData);
		}
		emInstfacilityTreeString = emInstfacilityTreeString + "</li>";
		emInstfacilityTreeString = emInstfacilityTreeString + "</ul>";
	}
	
	function getBuildingData(buildingData){
		emInstfacilityTreeString = emInstfacilityTreeString + "<ul>";
		var floorData;
		if (Array.isArray(buildingData)){
			for (var j=0; j< buildingData.length; j++) {
				floorData = buildingData[j].treeNodeList;
				emInstfacilityTreeString = emInstfacilityTreeString + createChildFacilityTree(buildingData[j]);
				//alert(buildingData[j].name);
				getFloorData(floorData);
			}
		}else{
			floorData = buildingData.treeNodeList;
			//alert(buildingData.name);
			emInstfacilityTreeString = emInstfacilityTreeString + createChildFacilityTree(buildingData);
			getFloorData(floorData);
		}
		emInstfacilityTreeString = emInstfacilityTreeString + "</li>";
		emInstfacilityTreeString = emInstfacilityTreeString + "</ul>";
	}
	
	function getFloorData(floorData){
		emInstfacilityTreeString = emInstfacilityTreeString + "<ul>";
		var floorName;
		if (Array.isArray(floorData)){
			for (var k=0; k< floorData.length; k++) {
				floorName = floorData[k].name;
				//alert(floorName);
				emInstfacilityTreeString = emInstfacilityTreeString + createChildFacilityTree(floorData[k]);
			}
		}else{
			floorName = floorData.name;
			//alert(floorName);
			emInstfacilityTreeString = emInstfacilityTreeString + createChildFacilityTree(floorData);
			
		}
		emInstfacilityTreeString = emInstfacilityTreeString + "</li>";
		emInstfacilityTreeString = emInstfacilityTreeString + "</ul>";
	}

	function createChildFacilityTree(childTreeNode){
		var childFacilityTreeString = "";
		var floorString = "FLOOR";
		if (childTreeNode.nodeType != null) {
			if(childTreeNode.nodeType.toLowerCase() == floorString.toLowerCase()){
				if(childTreeNode.isMapped || childTreeNode.isMapped == "true"){
					childFacilityTreeString = "<li rel='"+childTreeNode.nodeType.toLowerCase()+"_mapped' id='"+childTreeNode.nodeType.toLowerCase()+"_"+childTreeNode.nodeId+"'><a href='#'>"+childTreeNode.name+"</a>";
				}else{
					childFacilityTreeString = "<li rel='"+childTreeNode.nodeType.toLowerCase()+"_unmapped' id='"+childTreeNode.nodeType.toLowerCase()+"_"+childTreeNode.nodeId+"'><a href='#'>"+childTreeNode.name+"</a>";
				}
			}else{
				childFacilityTreeString = "<li rel='"+childTreeNode.nodeType.toLowerCase()+"' id='"+childTreeNode.nodeType.toLowerCase()+"_"+childTreeNode.nodeId+"'><a href='#'>"+childTreeNode.name+"</a>";
			}
		}else {
				childFacilityTreeString = "<li rel='organization' id='organization_'"+childTreeNode.nodeId+"'><a href='#'>"+childTreeNode.name+"</a>";
		}
		
		return childFacilityTreeString;
	}
	
	function saveEmMapping(emInst_facility_id,cloud_facility_id,emInstanceId,emFacilityPath){
		
		$('#facilitymapsubmitButton').attr("disabled", true);
		
		$.ajax({
			url: "${saveEmMappingUrl}"+emInstanceId+"/emFacility/"+emInst_facility_id+"/emFacilityPath/"+emFacilityPath+"/cloud/"+cloud_facility_id+"/customerId/"+"${customerId}"+"?ts="+new Date().getTime(),
			dataType:"json",
			contentType: "application/json; charset=utf-8",
			success: function(data){
				parent.reloadRegEmInstanceTable();
				parent.reloadFacilityEmMappingTable();
				displayLabelMessage('', COLOR_DEFAULT);
				$('#facilitymapsubmitButton').removeAttr("disabled");
				alert("Successfully Mapped.");
				$('#mapEmInstanceDialog').html("");
				$('#mapEmInstanceDialog').dialog('close');
			},
			error: function(){
				$('#facilitymapsubmitButton').removeAttr("disabled");
				alert("Mapping Failed.Please try again");
				$('#mapEmInstanceDialog').html("");
				$('#mapEmInstanceDialog').dialog('close');
			}
		});
	}
	
	function getIdfromString(facilityString){
		var str = facilityString;
		var array = str.toString().split('_');
		return array[1];
	}
	
	function getCloudFacilityId() {
		var cloud_facility_ids = [];
		$("#customerCloudFacilityTreeViewDiv").jstree("get_checked", null, true).each(
				function() {
					cloud_facility_ids.push(this.id);
				});

		return cloud_facility_ids;
	}
	
	function getEmInstFacilityId() {
		var emInst_facility_ids = [];
		$("#emInstfacilityTreeViewDiv").jstree("get_checked", null, true).each(
				function() {
					emInst_facility_ids.push(this.id);
				});

		return emInst_facility_ids;
	}
	
	
	function displayLabelMessage(Message, Color) {
		$("#facility_map_message").html(Message);
		$("#facility_map_message").css("color", Color);
	}
	
	function forceFitFacilityTreeViewDivs(){
		var divheight = $('#mapEmInstanceDialog').height() - $('#facilitymapsubmitButton').height() - 100;
		$('#customerCloudFacilityTreeViewDiv').height(divheight);
		$('#emInstfacilityTreeViewDiv').height(divheight);
	}
	
	
</script>
<div class="outermostdiv">
	<div style="font-weight: bolder; ">Select Global Facility on the left and EM Instance Facility on the right</div>
	<div id="facility_map_message" style="font-size: 14px; font-weight: bold;padding: 5px 5px;" ></div>
	
	<div style="padding: 5px 5px;">
		<button id="facilitymapsubmitButton">Save</button>
	</div>
	
	<div class="innerdiv" style="float:left;width:48%;padding: 0px 5px 0px 0px;">
		<div style="height: 5px;">&nbsp</div>
		<div style="font-weight: bolder; ">Global Facility Tree</div>
		<div style="height: 5px;">&nbsp</div>
		<div id="customerCloudFacilityTreeViewDiv" class="treeviewbg" oncontextmenu="return false;" style="overflow: auto">
				<ecloud:showFacilityTree facilityTreeHierarchy="${facilityTreeHierarchy}" />
		</div>
	</div>
	
	<div class="innerdiv" style="float:left;width:48%;padding: 0px 0px 0px 5px;">
		<div style="height: 5px;">&nbsp</div>
		<div style="font-weight: bolder; ">EM Instance Facility Tree</div>
		<div style="height: 5px;">&nbsp</div>
		<div id="emInstfacilityTreeViewDiv" class="treeviewbg" oncontextmenu="return false;"  style="overflow: auto">
		</div>
	</div>
	
</div>

