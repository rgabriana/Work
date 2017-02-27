<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/switch/details/floor/" var="checkDuplicateFloorSwitchUrl" scope="request"/>
<spring:url value="/services/org/switch/details/area/" var="checkDuplicateAreaSwitchUrl" scope="request"/>

<spring:url value="/devices/widget/switch/editAndApply.ems" var="applySwitchChangesURL" scope="request" />

<spring:url value="/scripts/jquery/jstree/themes/" var="jstreethemefolder"></spring:url>

<script type="text/javascript">
    var treenodetype;
    var treenodeid;    
    var validclick=false;
    var accTabSelected='fcop';
    var profilenodename;
    var profilenodetype;
    var profilenodeid;
    var oldSwitchName;
    var newSwitchName;
    
    var getNodeDetails=function(name){
		var arr=$(name.split('_'));				
		return arr;				
	}
    var title24flag= false;
    $(document).ready(function() {
    	
    	title24flag = $("#title24flag").val();
    	oldSwitchName = $("#name").val();
    	
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
    	
    	function startSwitchValidation(){
    		
    		$("#switchUpdateProgressDiv").html("");
			$("#switchUpdateProgressDiv").css("color", "black");
    		
    		if($("#name").val() == null || $("#name").val() == "" ){
    			$("#switchUpdateProgressDiv").html("Switch name cannot be empty");
    			$("#switchUpdateProgressDiv").css("color", "red");
    			return;
    		}
    		
    		newSwitchName = $("#name").val();
    		
    		var newSwitchNameRegExp = /^[A-Za-z0-9\s\-\_\.\@\#]+$/i;
    		if(newSwitchName!=""){
    			if(newSwitchNameRegExp.test(newSwitchName) == false) {
    				$("#switchUpdateProgressDiv").html("Switch name is invalid");
        			$("#switchUpdateProgressDiv").css("color", "red");
    			   	return false;   	
    			}
    		}
    		if(title24flag=="true"){
    			var initialSceneActiveTimeVal = $("#initialSceneActiveTime").val();
    			if(initialSceneActiveTimeVal > 120){
    				$("#switchUpdateProgressDiv").html("Intial time can not be greater than 120 in case EM is Title24 compliant");
        			$("#switchUpdateProgressDiv").css("color", "red");
    			   	return false;   
    			}
    		}
    		var yesorno = confirm("Are you sure you want to apply the changes to the fixtures and/or plugloads and/or ERC?");
    		 if(yesorno == false)
    			 return;
    		 
    		 if ( oldSwitchName == newSwitchName ){
    			 applySwitchChanges();
    		 }
    		 else{
    			 checkDuplicateSwitchName(newSwitchName, treenodetype , treenodeid);
    		 }
    		  
    	}
    
    	function applySwitchChanges() {
    		 
    		$.ajax({
    			type: "POST",
    			url: "${applySwitchChangesURL}?switchId="+${switchId},
    			data: $("#editSwitchForm").serialize(),
    			datatype: "html",
    			beforeSend: function() {
    				$("#switchUpdateProgressDiv").html("Processing, please wait...");
    				$("#switchUpdateProgressDiv").css("color", "green");
    			},
    			success: function(msg) {
    				$("#switchUpdateProgressDiv").html("Success");
    				$("#switchUpdateProgressDiv").css("color", "green");
    				
    				oldSwitchName = newSwitchName;
    				
    				try {
						getFloorPlanObj("widget_floorplan").plotChartRefresh();
					} catch(e) {
					}
    			},
    			error: function() {
    				$("#switchUpdateProgressDiv").html("Failed");
    				$("#switchUpdateProgressDiv").css("color", "red");
    			}
    		});	
    	}
    	
    	function checkDuplicateSwitchName(switchName, treenodetype , treenodeid) {
    		if(treenodetype=='floor')
    		{
    			$.ajax({
    				type: 'POST',
    				async: true,
    				url: "${checkDuplicateFloorSwitchUrl}"+treenodeid+"/"+encodeURIComponent(switchName)+"?ts="+new Date().getTime(),
    				data: "",
    				success: function(data){
    					if(data == null)
    					{
    						applySwitchChanges();
    					}
    					else
    					{
    						$("#switchUpdateProgressDiv").html("The Switch with the name already exists.");
    		    			$("#switchUpdateProgressDiv").css("color", "red");
    					}
    				},
    				dataType:"json",
    				contentType: "application/json; charset=utf-8"
    			});
    		}
    		else
    		{
    			$.ajax({
    				type: 'POST',
    				url: "${checkDuplicateAreaSwitchUrl}"+treenodeid+"/"+encodeURIComponent(switchName)+"?ts="+new Date().getTime(),
    				data: "",
    				success: function(data){
    					if(data == null){
    						applySwitchChanges()
    					}
    					else{
    						$("#switchUpdateProgressDiv").html("The Switch with the name already exists.");
    		    			$("#switchUpdateProgressDiv").css("color", "red");
    					}
    				},
    				dataType:"json",
    				contentType: "application/json; charset=utf-8"
    			});
    		}
    		
    	}
    	
		function validateInitialTime(evt) {
			var theEvent = evt || window.event;
			var key = theEvent.keyCode || theEvent.which;
			key = String.fromCharCode( key );
			var regex = /[0-9]|\./;
			if( !regex.test(key) ) {
				theEvent.returnValue = false;
				if ( evt.keyCode == 46 || evt.keyCode == 8 ) {
					// if key pressed is backspace or delete, then let it happen...
				}else				{
					if(theEvent.preventDefault) theEvent.preventDefault();
				}
			}
		}

</script>

<div style="padding-top: 5px;padding-left: 5px;border-style:solid;border-width:2px;" class="appbg" style="height:20%">
<input type=button onclick="startSwitchValidation();" value="Apply"/>
<div id="switchUpdateProgressDiv" style="font-weight: bold; padding: 0px 5px; display: inline;"></div>
	<form:form id="editSwitchForm" commandName="switch" method="post" onsubmit="return false;">
	<input type="hidden" id="title24flag" value="${title24flag}"/>
		<form:hidden path="id" id="id" />
			<table class="appbg">
				<tr>
					<td><label style="font-weight: bold">Switch name:</label></td>
					<td><form:input path="name" id="name" /></td>
				</tr>
				<tr>
					<td><label style="font-weight: bold">Switch type:</label></td>
					<td><form:radiobutton path="modeType" id="autoonautooff"
							value="0" /><span>Auto On/Auto Off</span></td>
				</tr>
				<tr>
					<td />
					<td><form:radiobutton path="modeType" id="manualonautooff"
							value="1" /><span>Manual On/Auto Off</span></td>
				</tr>
				<tr>
					<td><label style="font-weight: bold">Initial time:</label></td>
					<td><form:input path="initialSceneActiveTime" id="initialSceneActiveTime" maxlength="5" onkeypress='validateInitialTime(event)'/></td>
				</tr>
			</table>
		</form:form>
	<label style="font-weight: bold">Fixture version:</label>
	<label>${switchGroup.fixtureVersion}+</label>
</div>

<div id="accordionfacilitydlg" class="left-menu" style="height:77%">
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
