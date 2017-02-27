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
    var gemURL;
    
    var getNodeDetails=function(name){
		var arr=$(name.split('_'));				
		return arr;				
	}

    <%
    boolean viewTreeOnly = false;
    if (request.getParameter("viewTreeOnly")!=null && request.getParameter("viewTreeOnly").equals("true")) {
    	viewTreeOnly = true;
    }  
    %>

    $(document).ready(function() {    	
    	$.jstree._themes = "${jstreethemefolder}";
    	

		//Bind click event to call a function later for facility tree
  	    $.fn.treenodeclick = function(param) {  
  	    	$("#dashboardTreeViewDiv a").live("click", function() {
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

        loadTree();
    });
    
    //fuction to detect which accordion tab was clicked
    function SetAccTabSelected(tb){
    	accTabSelected=tb;
    }
    
	function removeclick() {
		$('#dashboardTreeViewDiv').removenodeclick();
		$('#dashboardTreeViewDiv').unbind('select_node.jstree');
	}
    
    function loadTree() {
    	
        var initialSelectedNode = $.cookie('jstree_select');
        if(initialSelectedNode){
            initialSelectedNode = initialSelectedNode.replace("#","");       
        }else {
            initialSelectedNode = "company_1"; 
            $.cookie('jstree_select', '#' + initialSelectedNode,  { path: '/' });
        }  
        
        var nodeDetails = getNodeDetails(initialSelectedNode);
        treenodetype = nodeDetails[0];
        treenodeid = nodeDetails[1];

        $("#dashboardTreeViewDiv").jstree({
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
                         select_node : function (e) { 
                             this.toggle_node(e); 
                         }
                     }
                 }
             },
             plugins : [ "themes", "html_data", "ui", "cookies" ,"checkbox", "types"]
        });
        
	    $("#dashboardTreeViewDiv").bind("select_node.jstree", function (event, data) {
		    var selectedNode = data.rslt.obj.attr("id");
		    var nodeDetails = getNodeDetails(selectedNode);
		    treenodetype = nodeDetails[0];
		    treenodeid = nodeDetails[1];
		    
	    	// Create URL for Auto Authentication of GEMS
	    	var apiKey = data.rslt.obj.attr("apiKey");
	    	var ipAddress =data.rslt.obj.attr("ipAddress");
	    	var port = data.rslt.obj.attr("port");
	    	gemURL = "https://"+ ipAddress + ":" + port +"/ems/ems_dashboard?api_key="+apiKey;

	    });
	    
	    
	    $("#dashboardTreeViewDiv").delegate("a","dblclick", function(e,data) {
	    	window.open(gemURL);
        });
	    
		
        <c:if test="${jsTreeOptions.checkBoxes != 'true'}">
        $("#dashboardTreeViewDiv").bind("loaded.jstree", function (event, data) {
            $(this).find('li[rel!=file]').find('.jstree-checkbox:first').hide();
          });
        </c:if>
        
        //expand the tab
    	var treeOnly = <%=viewTreeOnly%>;  
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
	   
</script>

<div id="accordionfacility" class="left-menu">
	<h2 id="h2facility" onclick="SetAccTabSelected('fcog');"><a href="#"><spring:message code="menu.facilities" /></a></h2>
	<div class="accinner">
	<!-- Tree view starts here -->		
		<div id="dashboardTreeViewDiv" class="treeviewbg">
			<ul>
				<c:forEach items='${dashboardTreeHierarchy.treeNodeList}' var='node1'>
					<li rel="company" id='<c:out value="${node1.nodeType.lowerCaseName}"/>_${node1.nodeId}' apiKey='<c:out value="${node1.apiKey}"/>'><a href='#'>${node1.name}</a>
						<ul>
							<c:forEach items='${node1.treeNodeList}' var='node2'>
								<li rel="campus" id='<c:out value="${node2.nodeType.lowerCaseName}"/>_${node2.nodeId}'
								apiKey='<c:out value="${node2.apiKey}"/>'
								ipAddress='<c:out value="${node2.ipAddress}"/>'
								port='<c:out value="${node2.port}"/>'><a href='#'>${node2.name}</a>
								</li>
							</c:forEach>
						</ul></li>
				</c:forEach>
			</ul>
		</div>
	</div>
</div>	
