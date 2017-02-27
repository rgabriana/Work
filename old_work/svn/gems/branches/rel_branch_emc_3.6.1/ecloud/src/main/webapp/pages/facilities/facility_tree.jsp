<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="ecloud" uri="/WEB-INF/tlds/ecloud.tld"%>

<spring:url value="/scripts/jquery/jquery.cookie.20110708.js" var="jquery_cookie"></spring:url>
<script type="text/javascript" src="${jquery_cookie}"></script>

<spring:url value="/scripts/jquery/jstree/jquery.jstree.js"	var="jquery_jstree"></spring:url>
<script type="text/javascript" src="${jquery_jstree}"></script>

<spring:url value="/scripts/jquery/jstree/themes/" var="jstreethemefolder"></spring:url>

<script type="text/javascript">
    var treenodetype;
    var treenodeid;    
    var validclick=false;
    var accTabSelected='fcog';
    
    var isFloorMapped = false;
    
    <c:if test="${enableSensorProfile == 'true'}">
    var profilenodename;
    var profilenodetype;
    var profilenodeid;
    var parentnodeid;
    </c:if>
    
    var getNodeDetails=function(name){
		var arr=$(name.split('_'));				
		return arr;				
	}

  

    $(document).ready(function() {    	
    	$.jstree._themes = "${jstreethemefolder}";
    	

		//Bind click event to call a function later for facility tree
  	    $.fn.treenodeclick = function(param) {  
  	    	$("#facilityTreeViewDiv a").live("click", function() {
  	    		if ($.isFunction(param)) param();
  			});
  		};
  		
  		<c:if test="${enableSensorProfile == 'true'}">
		//Bind click event to call a function later for profile tree
		$.fn.profiletreenodeclick = function(param) {
			$("#profileTreeViewDiv a").live("click", function() {					
				if ($.isFunction(param)) param();					
			});
		};
		</c:if>
		
		//Bind click event to call a function later for accordion tab
		$.fn.accordiontabclick = function(param) {
			$("#accordionfacility h2").live("click", function() {
				if ($.isFunction(param)) param();
			});
		};
		
	    $.fn.removenodeclick = function() {
			return this.each(function(){
				$(this).unbind('click');
			});
		};

		$(".left-menu").css("overflow", "auto");
		var initialSelectedNode = '${facilityTreeHierarchy.nodeType.lowerCaseName}' + "_" + '${facilityTreeHierarchy.nodeId}';
		loadTree(initialSelectedNode);
		<c:if test="${enableSensorProfile == 'true'}">
			loadProfileTree();
		</c:if>
		
    });
    
    //fuction to detect which accordion tab was clicked
    function SetAccTabSelected(tb){
    	accTabSelected=tb;
    }
    
	function removeclick() {
		$('#facilityTreeViewDiv').removenodeclick();
		$('#facilityTreeViewDiv').unbind('select_node.jstree');
	}
    
    function loadTree(selectedTreeNode) {
    	
        var nodeDetails = getNodeDetails(selectedTreeNode);
		var initialSelectedNode = $.cookie('uem_facilites_jstree_select',{ path: '/' });
        
        if(initialSelectedNode){
            initialSelectedNode = initialSelectedNode.replace("#","");
            var treeElement = $("#facilityTreeViewDiv").find("#" + initialSelectedNode);
            if(treeElement == undefined || treeElement.attr("id") == undefined) {
            	initialSelectedNode = '${facilityTreeHierarchy.treeNodeList[0].nodeType.lowerCaseName}' + "_" + '${facilityTreeHierarchy.treeNodeList[0].nodeId}';
            	$.cookie('uem_facilites_jstree_select', '#' + initialSelectedNode,  { path: '/' });
            }
        }else {
            initialSelectedNode = selectedTreeNode;
            $.cookie('uem_facilites_jstree_select', '#' + initialSelectedNode,  { path: '/' });
        }  
        
        var nodeDetails = getNodeDetails(initialSelectedNode);
        treenodetype = nodeDetails[0];
        treenodeid = nodeDetails[1];
        
        if(treenodetype == 'floor'){
        	<c:forEach items="${facilityEmMappingList}" var="facilityEmMapping">
				var facilityId = "${facilityEmMapping.facilityId}";
				if(facilityId.toString() == treenodeid){
					isFloorMapped = true;
				}
			</c:forEach>
        }
        
        $("#facilityTreeViewDiv").jstree({
             core : { 
                 animation : 0
             },
             themes : {
                 theme : "default"
             },       
             cookies : {
            	 save_selected : "uem_facilites_jstree_select",
                 cookie_options : { 
                     path : "/"
                 }
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
                         /* select_node : function (e) { 
                             this.toggle_node(e); 
                         } */
                     }
                 }
             },
             plugins : [ "themes", "html_data", "ui", "cookies" ,"checkbox", "types"]
        });
        
     	//open/close node on double click
        $("#facilityTreeViewDiv").delegate("a","dblclick", function(e) {
        	$("#facilityTreeViewDiv").jstree("toggle_node", this);
        });
        
      
	    $("#facilityTreeViewDiv").bind("select_node.jstree", function (event, data) {
		    var selectedNode = data.rslt.obj.attr("id");
		    var nodeDetails = getNodeDetails(selectedNode);
		    treenodetype = nodeDetails[0];
		    treenodeid = nodeDetails[1];
		    
		    if (treenodetype == 'floor'){
		    	var selectedNodeRel = data.rslt.obj.attr("rel");
		    	var nodeRelDetails = getNodeDetails(selectedNodeRel);
		    	if(nodeRelDetails[0] == 'floor'){
		    		if(nodeRelDetails[1] == 'mapped'){
		    			isFloorMapped = true;
		    		}else{
		    			isFloorMapped = false;
		    		}
		    	}
		    }
		    
	    });
		
        <c:if test="${jsTreeOptions.checkBoxes != 'true'}">
        $("#facilityTreeViewDiv").bind("loaded.jstree", function (event, data) {
            $(this).find('li[rel!=file]').find('.jstree-checkbox:first').hide();
          });
        </c:if>
        
        //expand the tab
    	<%-- var treeOnly = <%=viewTreeOnly%>; --%>  
    	var treeOnly=false;
    	<c:if test="${viewTreeOnly}">
    		treeOnly=true;
    	</c:if>
    	
    	function countAccordionTabs()
    	{    		
    		if ($('div#accordionfacility > h2').size()==1)
    			treeOnly=true;
    		else
    			treeOnly=false;
    	} 
    	
    	countAccordionTabs();
    	
    	$("#accordionfacility").accordion({    		
    		active: treeOnly?0:1,
    		autoHeight:false
    	});    	
    }
    
    
function loadProfileTree() {	
	    var initialSelectedNode = $.cookie('uem_profiles_jstree_select');
	    if(initialSelectedNode){
	        initialSelectedNode = initialSelectedNode.replace("#","");       
	    }else {
	    	//console.log('${glemProfileTreeHierarchy.treeNodeList[0].nodeType.lowerCaseName}');
	    	initialSelectedNode = '${glemProfileTreeHierarchy.treeNodeList[0].nodeType.lowerCaseName}' + "_" + '${glemProfileTreeHierarchy.treeNodeList[0].nodeId}'+"_" + '${glemProfileTreeHierarchy.treeNodeList[0].parentNodeId}'; 
	        $.cookie('uem_profiles_jstree_select', "#" + initialSelectedNode,  { path: '/' });
	       
	    }  
	    
	     var treeElement = $("#profileTreeViewDiv").find("#" + initialSelectedNode);
	     profilenodename= treeElement.find('a:eq(0)').text();
	    if(profilenodename==undefined)
    	{
	       initialSelectedNode = '${glemProfileTreeHierarchy.treeNodeList[0].nodeType.lowerCaseName}' + "_" + '${glemProfileTreeHierarchy.treeNodeList[0].nodeId}' + '${glemProfileTreeHierarchy.treeNodeList[0].parentNodeId}'; 
	       $.cookie('uem_profiles_jstree_select', "#" + initialSelectedNode,  { path: '/' });
    	   profilenodename = '${glemProfileTreeHierarchy.treeNodeList[0].name}';
    	}
	 	  
	    var nodeDetails = getNodeDetails(initialSelectedNode);
	    profilenodetype = nodeDetails[0];
	    profilenodeid = nodeDetails[1];	
	    parentnodeid = nodeDetails[2];	
	    	           
	    $("#profileTreeViewDiv").jstree({
	         core : { 
	        	 animation : 0
	         },
	         themes : {
	             theme : "default"
	         },
	         cookies : {
	        	 save_selected : "uem_profiles_jstree_select",
	             cookie_options : { 
	                 path : "/"
	             },
	             auto_save:false
	         },
	         ui : {
	                
	        	 select_limit : 1,
	                 selected_parent_close : "false"
	         },
	         types: {
	    	     open_node : function () {event.stopImmediatePropagation(); return false;},
                 close_node : function () {event.stopImmediatePropagation(); return false;},
                 types : {
                     'template' : {
                          icon : {
                       	 
                          }
                      },
                     'group' : {
                          icon : {
                      	      
                          }
                      },
                     'default' : {
                          icon : {
                          
                          },
                       valid_children : 'default'

                      }
                  }
           },
	         plugins : [ "themes", "html_data", "ui", "cookies" ,"checkbox", "types"]
	    });
	    
	    $("#profileTreeViewDiv").bind("select_node.jstree", function (event, data) {
		    
	    	var selectedNode = data.rslt.obj.attr("id");
		    var nodeDetails = getNodeDetails(selectedNode);
		    profilenodetype = nodeDetails[0];
		    profilenodeid = nodeDetails[1];
		    parentnodeid = nodeDetails[2];	
		    profilenodename= data.rslt.obj.find('a:eq(0)').text(); //get the text of selected node to put on RHS profile tab.
		    $.cookie('uem_profiles_jstree_select', '#' + selectedNode,  { path: '/' });
		    
	    });
		
	    
	    $("#profileTreeViewDiv").delegate("a","dblclick", function(e) {
        	$("#profileTreeViewDiv").jstree("toggle_node", this);
        });
	    
	    <c:if test="${jsTreeOptions.checkBoxes != 'true'}">
	    $("#profileTreeViewDiv").bind("loaded.jstree", function (event, data) {
	        $(this).find('li[rel!=file]').find('.jstree-checkbox:first').hide();
	      });
	    </c:if>
	  }
    
  	
</script>

<div id="accordionfacility" class="left-menu">
	<c:if test="${enableSensorProfile == 'true'}">
	<h2 id="h2profile" onclick="SetAccTabSelected('pf');"><a href="#" id="profileTemplateMenu">Profile Templates</a></h2>
	<div class="accinner">		
		<div id="profileTreeViewDiv" class="treeviewbg" oncontextmenu="return false;">
<%-- 		 <ecloud:showProfileTree profileTreeHierarchy="${profileTreeHierarchy}" /> --%>
	 	 <ecloud:showUEMProfileTree uemProfileTreeHierarchy="${glemProfileTreeHierarchy}" />
		</div>
	</div>
	</c:if>
		
	<h2 id="h2facility" onclick="SetAccTabSelected('fcog');"><a href="#">Global Facilities</a></h2>
	<div class="accinner">
	<!-- Tree view starts here -->		
		<div id="facilityTreeViewDiv" class="treeviewbg" oncontextmenu="return false;">
			<ecloud:showFacilityTree facilityTreeHierarchy="${facilityTreeHierarchy}" />
		</div>
	</div>
</div>	
