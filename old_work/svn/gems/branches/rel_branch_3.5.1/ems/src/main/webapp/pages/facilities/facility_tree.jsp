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
    <c:if test="${enableProfileFeature == 'true'}">
    var profilenodename;
    var profilenodetype;
    var profilenodeid;
    </c:if>
    <c:if test="${enablePlugloadProfileFeature == 'true'}">
    var plugloadProfilenodename;
    var plugloadProfilenodetype;
    var plugloadProfilenodeid;
    </c:if>
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
  		
  		<c:if test="${enableProfileFeature == 'true'}">
		//Bind click event to call a function later for profile tree
		$.fn.profiletreenodeclick = function(param) {
			$("#profileTreeViewDiv a").live("click", function() {					
				if ($.isFunction(param)) param();					
			});
		};
		</c:if>
		
		<c:if test="${enablePlugloadProfileFeature == 'true'}">
		$.fn.plugloadprofiletreenodeclick = function(param) {
			$("#plugloadProfileTreeViewDiv a").live("click", function() {					
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
		<c:if test="${enableProfileFeature == 'true'}">
		loadProfileTree();
		</c:if>
		<c:if test="${enablePlugloadProfileFeature == 'true'}">
		loadPlugLoadProfileTree();
		</c:if>
        loadTree();
    });
    
    //fuction to detect which accordion tab was clicked
    function SetAccTabSelected(tb){
    	accTabSelected=tb;
    	if(accTabSelected == "plpf"){
    		$("#innercenter").css("display", "none");
    		$("#plugloadTemplateMainDiv").css("display", "block");
    		loadPlugloadProfileTabs();
    	}else{
    		$("#innercenter").css("display", "block");
    		$("#plugloadTemplateMainDiv").css("display", "none");
    	}
    }
    
	function removeclick() {
		$('#facilityTreeViewDiv').removenodeclick();
		$('#facilityTreeViewDiv').unbind('select_node.jstree');
	}
    
    function loadTree() {
    	
        var initialSelectedNode = $.cookie('em_facilites_jstree_select',{ path: '/' });
        
        if(initialSelectedNode){
            initialSelectedNode = initialSelectedNode.replace("#","");
            var treeElement = $("#facilityTreeViewDiv").find("#" + initialSelectedNode);
            if(treeElement == undefined || treeElement.attr("id") == undefined) {
            	initialSelectedNode = '${facilityTreeHierarchy.treeNodeList[0].nodeType.lowerCaseName}' + "_" + '${facilityTreeHierarchy.treeNodeList[0].nodeId}';
            	$.cookie('em_facilites_jstree_select', '#' + initialSelectedNode,  { path: '/' });
            }
        }else {
            initialSelectedNode = '${facilityTreeHierarchy.treeNodeList[0].nodeType.lowerCaseName}' + "_" + '${facilityTreeHierarchy.treeNodeList[0].nodeId}'; 
            $.cookie('em_facilites_jstree_select', '#' + initialSelectedNode,  { path: '/' });
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
            	 save_selected : "em_facilites_jstree_select",
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
    	
    	if("${enablePlugloadProfileFeature}" == 'true' && $.cookie('accordian_select') == "facility"){
    		$("#accordionfacility").accordion({    		
            		active: treeOnly?0:2,
            		autoHeight:false
           	});
    	}else{
    		$("#accordionfacility").accordion({    		
        		active: treeOnly?0:1,
        		autoHeight:false
        	});
    	}
    	
    }
    
  	//Added by Nitin to generate profile tree
	
	function loadProfileTree() {	
	    var initialSelectedNode = $.cookie('jstree_profile_select');
	    if(initialSelectedNode){
	        initialSelectedNode = initialSelectedNode.replace("#","");       
	    }else {
	    	initialSelectedNode = '${profileTreeHierarchy.treeNodeList[0].nodeType.lowerCaseName}' + "_" + '${profileTreeHierarchy.treeNodeList[0].nodeId}'; 
	        $.cookie('jstree_profile_select', "#" + initialSelectedNode,  { path: '/' });
	       
	    }  
	    
	     var treeElement = $("#profileTreeViewDiv").find("#" + initialSelectedNode);
	     profilenodename= treeElement.find('a:eq(0)').text();
	    if(profilenodename==undefined)
    	{
	       initialSelectedNode = '${profileTreeHierarchy.treeNodeList[0].nodeType.lowerCaseName}' + "_" + '${profileTreeHierarchy.treeNodeList[0].nodeId}'; 
	       $.cookie('jstree_profile_select', "#" + initialSelectedNode,  { path: '/' });
    	   profilenodename = '${profileTreeHierarchy.treeNodeList[0].name}';
    	}
	 	  
	    var nodeDetails = getNodeDetails(initialSelectedNode);
	    profilenodetype = nodeDetails[0];
	    profilenodeid = nodeDetails[1];		          
	    	           
	    $("#profileTreeViewDiv").jstree({
	         core : { 
	        	 animation : 0
	         },
	         themes : {
	             theme : "default"
	         },
	         cookies : {
	        	 save_selected : "jstree_profile_select",
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
		    profilenodename= data.rslt.obj.find('a:eq(0)').text(); //get the text of selected node to put on RHS profile tab.
		    $.cookie('jstree_profile_select', '#' + selectedNode,  { path: '/' });
		    
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
	
	function loadPlugLoadProfileTree() {
		
		var initialSelectedNode = $.cookie('jstree_plugload_profile_select');
	    
		if(initialSelectedNode){
	        initialSelectedNode = initialSelectedNode.replace("#","");       
	    }else {
	    	initialSelectedNode = '${plugloadProfileTreeHierarchy.treeNodeList[0].nodeType.lowerCaseName}' + "_" + '${plugloadProfileTreeHierarchy.treeNodeList[0].nodeId}'; 
	        $.cookie('jstree_plugload_profile_select', "#" + initialSelectedNode,  { path: '/' });
	       
	    }  
	    
	    var treeElement = $("#plugloadProfileTreeViewDiv").find("#" + initialSelectedNode);
	    plugloadProfilenodename= treeElement.find('a:eq(0)').text();
	    
	    if(plugloadProfilenodename==undefined)
    	{
	       initialSelectedNode = '${plugloadProfileTreeHierarchy.treeNodeList[0].nodeType.lowerCaseName}' + "_" + '${plugloadProfileTreeHierarchy.treeNodeList[0].nodeId}'; 
	       $.cookie('jstree_plugload_profile_select', "#" + initialSelectedNode,  { path: '/' });
	       plugloadProfilenodename = '${plugloadProfileTreeHierarchy.treeNodeList[0].name}';
    	}
	 	  
	    var nodeDetails = getNodeDetails(initialSelectedNode);
	    plugloadProfilenodetype = nodeDetails[0];
	    plugloadProfilenodeid = nodeDetails[1];
	    
	    $("#plugloadProfileTreeViewDiv").jstree({
	         core : { 
	        	 animation : 0
	         },
	         themes : {
	             theme : "default"
	         },
	         cookies : {
	        	 save_selected : "jstree_plugload_profile_select",
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
                     'plugloadtemplate' : {
                          icon : {
                       	 
                          }
                      },
                     'plugloadgroup' : {
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
	    
	    $("#plugloadProfileTreeViewDiv").bind("select_node.jstree", function (event, data) {
		    
	    	var selectedNode = data.rslt.obj.attr("id");
		    var nodeDetails = getNodeDetails(selectedNode);
		    plugloadProfilenodetype = nodeDetails[0];
		    plugloadProfilenodeid = nodeDetails[1];
		    plugloadProfilenodename= data.rslt.obj.find('a:eq(0)').text(); //get the text of selected node to put on RHS plugload profile tab.
		    $.cookie('jstree_plugload_profile_select', '#' + selectedNode,  { path: '/' });
		    
	    });
		
	    
	    $("#plugloadProfileTreeViewDiv").delegate("a","dblclick", function(e) {
        	$("#plugloadProfileTreeViewDiv").jstree("toggle_node", this);
        });
	    
	    <c:if test="${jsTreeOptions.checkBoxes != 'true'}">
	      $("#plugloadProfileTreeViewDiv").bind("loaded.jstree", function (event, data) {
	        $(this).find('li[rel!=file]').find('.jstree-checkbox:first').hide();
	      });
	    </c:if>
	    
	}
	
</script>

<div id="accordionfacility" class="left-menu">
	<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin','TenantAdmin')">
	<c:if test="${enableProfileFeature == 'true'}">
		<c:if test="${!viewTreeOnly}">
		  	<h2 id="h2profile" onclick="SetAccTabSelected('pf');"><a href="#" id="profileTemplateMenu">Profile Templates</a></h2>
			<div class="accinner">		
				<div id="profileTreeViewDiv" class="treeviewbg" oncontextmenu="return false;">
					<ul>
						<c:forEach items='${profileTreeHierarchy.treeNodeList}' var='template'>
							<li rel='${template.nodeType.lowerCaseName}' id='<c:out value="${template.nodeType.lowerCaseName}" escapeXml="true"/>_${template.nodeId}'><a href='#'>${template.name}</a>
								<ul>
									<c:forEach items='${template.treeNodeList}' var='profile'>
										<li rel='${profile.nodeType.lowerCaseName}' id='<c:out value="${profile.nodeType.lowerCaseName}"/>_${profile.nodeId}'><a href='#'><c:out value="${profile.name}" escapeXml="true"/></a>
										</li>
									</c:forEach>
								</ul>
							</li>
						</c:forEach>
					</ul>
				</div>
			</div>
		</c:if>
		</c:if>
	</security:authorize>
	<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin','TenantAdmin')">
	<c:if test="${enablePlugloadProfileFeature == 'true'}">
		<c:if test="${!viewTreeOnly}">
		  	<h2 id="h2plugloadprofile" onclick="SetAccTabSelected('plpf');"><a href="#" id="plugloadProfileTemplateMenu">Plugload Profile Templates</a></h2>
			<div class="accinner">		
				<div id="plugloadProfileTreeViewDiv" class="treeviewbg" oncontextmenu="return false;">
					<ul>
						<c:forEach items='${plugloadProfileTreeHierarchy.treeNodeList}' var='plugloadTemplate'>
							<li rel='${plugloadTemplate.nodeType.lowerCaseName}' id='<c:out value="${plugloadTemplate.nodeType.lowerCaseName}" escapeXml="true"/>_${plugloadTemplate.nodeId}'><a href='#'>${plugloadTemplate.name}</a>
								<ul>
									<c:forEach items='${plugloadTemplate.treeNodeList}' var='plugloadProfile'>
										<li rel='${plugloadProfile.nodeType.lowerCaseName}' id='<c:out value="${plugloadProfile.nodeType.lowerCaseName}"/>_${plugloadProfile.nodeId}'><a href='#'><c:out value="${plugloadProfile.name}" escapeXml="true"/></a>
										</li>
									</c:forEach>
								</ul>
							</li>
						</c:forEach>
					</ul>
				</div>
			</div>
		</c:if>
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
