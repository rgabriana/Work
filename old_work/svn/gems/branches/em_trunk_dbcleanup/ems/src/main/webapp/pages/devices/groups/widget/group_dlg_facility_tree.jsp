<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/devices/widget/group/editAndApply.ems" var="applyGroupChangesURL" scope="request" />

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

		//Bind click event to call a function later for facility tree
  	    $.fn.treenodeclick = function(param) {  
  	    	$("#facilityDlgTreeViewDiv a").live("click", function() {
  	    		if ($.isFunction(param)) param();
  			});
  		};
		
		//Bind click event to call a function later for accordion tab
		$.fn.accordiontabclick = function(param) {
			$("#accordionfacilitydlg h2").live("click", function() {
				if ($.isFunction(param)) param();
			});
		};
		
	    $.fn.removenodeclick = function() {
			return this.each(function(){
				$(this).unbind('click');
			});
		};

		$(".left-menu").css("overflow", "auto");
        loadTreeDlg();
    });
    
    //fuction to detect which accordion tab was clicked
    function SetDlgAccTabSelected(tb){
    	accTabSelected=tb;
    }
    
	function removeclick() {
		$('#facilityDlgTreeViewDiv').removenodeclick();
		$('#facilityDlgTreeViewDiv').unbind('select_node.jstree');
	}
    
    function loadTreeDlg() {
    	
        var initialSelectedNode = $.cookie('em_facilites_jstree_select',{ path: '/' });
        
        if(initialSelectedNode){
            initialSelectedNode = initialSelectedNode.replace("#","");
            var treeElement = $("#facilityDlgTreeViewDiv").find("#" + initialSelectedNode);
            if(treeElement == undefined || treeElement.attr("id") == undefined) {
            	initialSelectedNode = '${facilityTreeHierarchy.treeNodeList[0].nodeType.lowerCaseName}' + "_" + '${facilityTreeHierarchy.treeNodeList[0].nodeId}';
            }
        }else {
            initialSelectedNode = '${facilityTreeHierarchy.treeNodeList[0].nodeType.lowerCaseName}' + "_" + '${facilityTreeHierarchy.treeNodeList[0].nodeId}'; 
        }  
        
        var nodeDetails = getNodeDetails(initialSelectedNode);
        treenodetype = nodeDetails[0];
        treenodeid = nodeDetails[1];

        $("#facilityDlgTreeViewDiv").jstree({
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
             cookies : {
            	 save_selected : "em_facilites_jstree_select",
            	 auto_save : false,
                 cookie_options : { 
                     path : "/"
                 }
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
                         /* select_node : function (e) { 
                             this.toggle_node(e); 
                         } */
                     }
                 }
             },
             plugins : [ "themes", "html_data", "ui","cookies", "checkbox", "types"]
        });
        
     	//open/close node on double click
        $("#facilityDlgTreeViewDiv").delegate("a","dblclick", function(e) {
        	$("#facilityDlgTreeViewDiv").jstree("toggle_node", this);
        });
        
        
	    $("#facilityDlgTreeViewDiv").bind("select_node.jstree", function (event, data) {
		    var selectedNode = data.rslt.obj.attr("id");
		    var nodeDetails = getNodeDetails(selectedNode);
		    treenodetype = nodeDetails[0];
		    treenodeid = nodeDetails[1];
	    });
		
	    
        <c:if test="${jsTreeOptions.checkBoxes != 'true'}">
        $("#facilityDlgTreeViewDiv").bind("loaded.jstree", function (event, data) {
            $(this).find('li[rel!=file]').find('.jstree-checkbox:first').hide();
          });
        </c:if>
        
        //expand the tab
    	<%-- var treeOnly = <%=viewTreeOnly%>; --%>  
    	var treeOnly=false;
    	<c:if test="${viewTreeOnly}">
    		treeOnly=true;
    	</c:if>
    	
    	function countAccordionTabs(){    		
    		if ($('div#accordionfacilitydlg > h2').size()==1)
    			treeOnly=true;
    		else
    			treeOnly=false;
    	};
    	
    	countAccordionTabs();
    	
    	$("#accordionfacilitydlg").accordion({    		
    		active: treeOnly?0:1,
    		autoHeight:false
    	});    	
    	}
    	function applyGroupChanges() {
    		$.ajax({
    			type: "POST",
    			url: "${applyGroupChangesURL}?groupId="+${groupId},
    			data: $("#editGroupForm").serialize(),
    			datatype: "html",
    			beforeSend: function() {
    				$("#motionGroupUpdateProgressDiv").html("Processing, please wait...");
    				$("#motionGroupUpdateProgressDiv").css("color", "green");
    			},
    			success: function(msg) {
    				$("#motionGroupUpdateProgressDiv").html("Success");
    				$("#motionGroupUpdateProgressDiv").css("color", "green");
    			},
    			error: function() {
    				$("#motionGroupUpdateProgressDiv").html("Failed");
    				$("#motionGroupUpdateProgressDiv").css("color", "red");
    			}
    		});	
    	}

    
    
</script>

<div style="padding-top: 5px;padding-left: 5px" class="appbg">
<input type=button onclick="applyGroupChanges();" value="Apply"/>
<div id="motionGroupUpdateProgressDiv" style="font-weight: bold; padding: 0px 5px; display: inline;"></div>
<div style="float:left;text-align: left; width:100%;" class="appbg">
	<form:form id="editGroupForm" commandName="group" method="post" onsubmit="return false;">
		<form:hidden path="id" id="id" />
			<table class="appbg">
				<tr>
					<td><label style="font-weight: bold">Group name:</label></td>
					<td><form:input path="gemsGroup.groupName" id="groupName" /></td>
				</tr>
				
			</table>
		</form:form>
</div>
</div>

<div id="accordionfacilitydlg" class="left-menu">
	<h2 id="h2facilitydlg" onclick="SetDlgAccTabSelected('fcog');"><a href="#">Facilities</a></h2>
	<div class="accinner">
	<!-- Tree view starts here -->		
		<div id="facilityDlgTreeViewDiv" class="treeviewbg" oncontextmenu="return false;">
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
