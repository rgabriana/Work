<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<spring:url value="/scripts/jquery/jquery.cookie.20110708.js" var="jquery_cookie"></spring:url>
<script type="text/javascript" src="${jquery_cookie}"></script>

<spring:url value="/scripts/jquery/jstree/jquery.jstree.js"	var="jquery_jstree"></spring:url>
<script type="text/javascript" src="${jquery_jstree}"></script>

<spring:url value="/scripts/jquery/jstree/themes/" var="jstreethemefolder"></spring:url>

<script type="text/javascript">
    var treenodetype;
    var treenodeid;    
    var validclick=false;
    var accTabSelected='fcop';
    var profilenodename;
    var profilenodetype;
    var profilenodeid;
    
    var getNodeDetails=function(name){
		var arr=$(name.split('_'));				
		return arr;				
	}

  

    $(document).ready(function() {    	
    	$.jstree._themes = "${jstreethemefolder}";
    	
    	
// 	    $.fn.treenodeclick = function(param) {    		
// 			return this.each(function(){
// 				$(this).click(function(){					
//					if ($.isFunction(param)) param();
//				}
// 			});
// 		};

		//Bind click event to call a function later for facility tree
  	    $.fn.treenodeclick = function(param) {  
  	    	$("#facilityTreeViewDiv a").live("click", function() {
  	    		if ($.isFunction(param)) param();
  			});
  		};

		//Bind click event to call a function later for profile tree
		$.fn.profiletreenodeclick = function(param) {
			$("#profileTreeViewDiv a").live("click", function() {					
				if ($.isFunction(param)) param();					
			});
		};
		
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
		loadProfileTree();
        loadTree();
    });
    
    //fuction to detect which accordion tab was clicked
    function SetAccTabSelected(tb){
    	accTabSelected=tb;
    }
    
	function removeclick() {
		$('#facilityTreeViewDiv').removenodeclick();
		$('#facilityTreeViewDiv').unbind('select_node.jstree');
	}
    
    function loadTree() {
    	
        var initialSelectedNode = $.cookie('jstree_select');
        
        if(initialSelectedNode){
            initialSelectedNode = initialSelectedNode.replace("#","");
            var treeElement = $("#facilityTreeViewDiv").find("#" + initialSelectedNode);
            if(treeElement == undefined || treeElement.attr("id") == undefined) {
            	initialSelectedNode = '${facilityTreeHierarchy.treeNodeList[0].nodeType.lowerCaseName}' + "_" + '${facilityTreeHierarchy.treeNodeList[0].nodeId}';
            	$.cookie('jstree_select', '#' + initialSelectedNode,  { path: '/' });
            }
        }else {
            initialSelectedNode = '${facilityTreeHierarchy.treeNodeList[0].nodeType.lowerCaseName}' + "_" + '${facilityTreeHierarchy.treeNodeList[0].nodeId}'; 
            $.cookie('jstree_select', '#' + initialSelectedNode,  { path: '/' });
        }  
        
        var nodeDetails = getNodeDetails(initialSelectedNode);
        treenodetype = nodeDetails[0];
        treenodeid = nodeDetails[1];

        $("#facilityTreeViewDiv").jstree({
             core : { 
                 animation : 0
             },
             themes : {
                 theme : "default"
             },       
             cookies : {
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
    
  	//Added by Nitin to generate profile tree
	
	function loadProfileTree() {	
		
	    var initialSelectedNode = $.cookie('jstree_profile_select');
	    if(initialSelectedNode){
	        initialSelectedNode = initialSelectedNode.replace("#","");       
	    }else {
	        initialSelectedNode = "group_1"; 
	        $.cookie('jstree_profile_select', "#" + initialSelectedNode,  { path: '/' });
	    }  
	    
	    var nodeDetails = getNodeDetails(initialSelectedNode);
	    profilenodetype = nodeDetails[0];
	    profilenodeid = nodeDetails[1];		          
	    	           
	    $("#profileTreeViewDiv").jstree({
	         core : { 
	         },
	         themes : {
	             theme : "default"
	         },
	         cookies : {
	             cookie_options : { 
	                 path : "/"
	             },
	             auto_save:false
	         },
	         ui : {
	                 select_limit : 1,
	                 selected_parent_close : "false"
	         },
	         types : {
	             open_node : function () {event.stopImmediatePropagation(); return false;},
	             close_node : function () {event.stopImmediatePropagation(); return false;}
	         },
	         plugins : [ "themes", "html_data", "ui", "cookies" ,"checkbox", "types"]
	    });
	    
	    $("#profileTreeViewDiv").bind("select_node.jstree", function (event, data) {
		    var selectedNode = data.rslt.obj.attr("id");
		    var nodeDetails = getNodeDetails(selectedNode);
		    
		    profilenodename = data.rslt.obj.find('a').text(); //get the text of selected node to put on RHS profile tab.
		    profilenodetype = nodeDetails[0];
		    profilenodeid = nodeDetails[1];
	        
		    $.cookie('jstree_profile_select', '#' + selectedNode,  { path: '/' });
		    
	    });
		
	    <c:if test="${jsTreeOptions.checkBoxes != 'true'}">
	    $("#profileTreeViewDiv").bind("loaded.jstree", function (event, data) {
	        $(this).find('li[rel!=file]').find('.jstree-checkbox:first').hide();
	      });
	    </c:if>
	  }
</script>

<div id="accordionfacility" class="left-menu">
	<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin')">
		<c:if test="${!viewTreeOnly}">
		  	<h2 id="h2profile" onclick="SetAccTabSelected('pf');"><a href="#">Profiles</a></h2>
			<div class="accinner">		
				<div id="profileTreeViewDiv" class="treeviewbg" oncontextmenu="return false;">
					<ul>
						<c:forEach items='${profileTreeHierarchy.treeNodeList}' var='group'>
							<li id='<c:out value="${group.nodeType.lowerCaseName}"/>_${group.nodeId}'><a href='#'>${group.name}</a>
							</li>
						</c:forEach>
					</ul>
				</div>
			</div>
		</c:if>
	</security:authorize>	
	<h2 id="h2facility" onclick="SetAccTabSelected('fcog');"><a href="#">Facilities</a></h2>
	<div class="accinner">
	<!-- Tree view starts here -->		
		<div id="facilityTreeViewDiv" class="treeviewbg" oncontextmenu="return false;">
			<ul>
				<c:forEach items='${facilityTreeHierarchy.treeNodeList}' var='node1'>
					<li rel="${node1.nodeType.lowerCaseName}" id='<c:out value="${node1.nodeType.lowerCaseName}"/>_${node1.nodeId}'><a href='#'><c:out value="${node1.name}" escapeXml="true"/></a>
						<ul>
							<c:forEach items='${node1.treeNodeList}' var='node2'>
								<li rel="${node2.nodeType.lowerCaseName}" id='<c:out value="${node2.nodeType.lowerCaseName}"/>_${node2.nodeId}'><a href='#'><c:out value="${node2.name}" escapeXml="true"/></a>
									<ul>
										<c:forEach items='${node2.treeNodeList}' var='node3'>
											<li rel="${node3.nodeType.lowerCaseName}" id='<c:out value="${node3.nodeType.lowerCaseName}"/>_${node3.nodeId}'><a href=""><c:out value="${node3.name}" escapeXml="true"/></a>
												<ul>
													<c:forEach items='${node3.treeNodeList}' var='node4'>
														<li rel="${node4.nodeType.lowerCaseName}" id='<c:out value="${node4.nodeType.lowerCaseName}"/>_${node4.nodeId}'><a href=""><c:out value="${node4.name}" escapeXml="true"/></a>
															<ul>
																<c:forEach items='${node4.treeNodeList}' var='node5'>
																	<li rel="${node5.nodeType.lowerCaseName}" id='<c:out value="${node5.nodeType.lowerCaseName}"/>_${node5.nodeId}'><a href=""><c:out value="${node5.name}" escapeXml="true"/></a></li>
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
	</div>
</div>	