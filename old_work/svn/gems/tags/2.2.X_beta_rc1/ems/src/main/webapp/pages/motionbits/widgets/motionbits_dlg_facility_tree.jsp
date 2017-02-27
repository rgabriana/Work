<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/devices/widget/motionbitsgroup/editAndApply.ems" var="applyMotionGroupChangesURL" scope="request" />
<spring:url value="/scripts/jquery/jstree/jquery.jstree.js"	var="jquery_jstree"></spring:url>
<script type="text/javascript" src="${jquery_jstree}"></script>
<spring:url value="/scripts/jquery/jstree/themes/" var="jstreethemefolder"></spring:url>
<spring:url value="/services/org/motionbits/op/validateschedule/" var="validateMotionBitsScheduleUrl" scope="request" />

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
	};

    $(document).ready(function() {  
    	//alert("${motionBitsSchedule.motionBitGroup.id}");
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
	
	var isTimingValid = true;
	
	
	function setMessage(msg, color){
		$("#motionGroupUpdateProgressDiv").css("color", color);
		$("#motionGroupUpdateProgressDiv").html(msg);
	}
	
	function dateTimeCompare(time1,time2) { 
		  var t1 = new Date(); 
		  var time1Parts = time1.split(":"); 
		  t1.setHours(time1Parts[0],time1Parts[1],0,0); 
		  var t2 = new Date(); 
		  var time2Parts = time2.split(":"); 
		  t2.setHours(time2Parts[0],time2Parts[1],0,0); 
		  // returns 1 if greater, -1 if less and 0 if the same 
		  if (t1.getTime()>t2.getTime()) return 1; 
		  if (t1.getTime()<t2.getTime()) return -1; 
		  return 0; 
	} 
	
	function validateSchedule() {
		var schedulename = $.trim($('#name').val());
		
		var iChars = "!@#$%^&*()+=-[]\\\';,./{}|\":<>?";
		for (var i = 0; i < schedulename.length; i++) {
		    if (iChars.indexOf(schedulename.charAt(i)) != -1) {		    	
		    	setMessage("Invalid name for motionbit", "red");		    	
				return false;
		        }
		    }
		
		var url = "${validateMotionBitsScheduleUrl}"+schedulename+"/"+$.trim($("#captureStart").val())+"/"+$.trim($("#captureEnd").val())+"/"+"${motionBitsSchedule.id}"+"?ts="+new Date().getTime();
		
	 	$.ajax({
	 		type: 'POST',
	 		url: url,
	 		success: function(data){
	 			if(data == "") {
	 				// Success
	 				applyGroupChanges();
	 			}
	 			else {
					setMessage(data, "red");
	 			}
			},
			error: function(){
				setMessage("Failed to validate the schedule", "red");
			},
	 		dataType:"html",
	 		contentType: "application/xml; charset=utf-8",
	 	});
	}

	function validate(bStartNow) {

		setMessage("", "black");
		
		if($.trim($('#name').val())==""){
			setMessage("Please enter name", "red");
			return false;
		}
		
		if(isTimingValid == false) {
	    	setMessage("Please enter the time in HH:mm format", "red");
	    	return false;
		}

		/* if(bStartNow == 'true') {
			var date = new Date();
			
			var t = date.getHours() + ":" + date.getMinutes();
			$("#from").val(t);
		} */
		
		var frequency = $("#transmitFreq").val();
		
		var bitLevel = $("#bitLevel").val();
		
		var captureStart = $("#captureStart").val();
		var captureEnd = $("#captureEnd").val();
		
		if(frequency == "") {
			setMessage("Please enter \"from\" frequency", "red");
			return false;
		}
		
		if(bitLevel == "") {
			setMessage("Please enter \"from\" bitLevel", "red");
			return false;
		}
		
		if(captureStart == "") {
			setMessage("Please enter \"from\" time", "red");
			return false;
		}
		
		if(captureStart == "") {
			setMessage("Please enter \"from\" time", "red");
			return false;
		}

		if(captureEnd == "") {
			setMessage("Please enter \"to\" time", "red");
			return false;
		}
		
		if(captureStart == "") {
			setMessage("Please enter \"from\" time", "red");
			return false;
		}
		
		if(dateTimeCompare(captureEnd, captureStart) == -1)
		{
			setMessage("\"To\" time has to be greater than \"from\" time", "red");
			return false;
		}

		if(dateTimeCompare(captureEnd, captureStart) == 0)
		{
			setMessage("\"To\" and \"from\" time have to be different", "red");
			return false;
		}
		setMessage("Processing...", "black");
		
		validateSchedule();
	}
	
	function validateTime(inputField) { 

		if(inputField.value!="")
		{
		  var isValid = /^([0-1]?[0-9]|2[0-4]):([0-5][0-9])(:[0-5][0-9])?$/.test(inputField.value); 
		  
	        if (isValid) { 
	            inputField.style.borderColor = '#757575'; 
	            setMessage("", "black");
	        } else {
	            setMessage("Please enter the time in HH:mm format", "red");
	        	//inputField.value="";
	            inputField.style.borderColor = '#ff0000';
	        } 
	        isTimingValid = isValid;
	        return isValid; 
		
		}
		else
		{
			isTimingValid = true;
	        setMessage("", "black");
			inputField.style.borderColor = '#757575'; 
		}
		return true;
	}
    
    function loadTreeDlg() {
    	
    	//alert("�n loadTreeDlg()");
    	//alert("${motionBitsSchedule.name}"+"");
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
    	
    	countAccordionTabs();
    	
    	$("#accordionfacilitydlg").accordion({    		
    		active: treeOnly?0:1,
    		autoHeight:false
    	});  
    }
    
	    function countAccordionTabs(){    		
			if ($('div#accordionfacilitydlg > h2').size()==1)
				treeOnly=true;
			else
				treeOnly=false;
		}
    
     	function applyGroupChanges() {    		
    	 	$.ajax({
    			type: "POST",
    			url: "${applyMotionGroupChangesURL}?groupId="+"1",
    			data: $("#editGroupFormForMotionGroups").serialize(),
    			datatype: "html"
    			,
    			beforeSend: function() {
    				$("#motionGroupUpdateProgressDiv").html("Processing, please wait...");
    				$("#motionGroupUpdateProgressDiv").css("color", "green");
    			},
    			success: function(msg) {
    				//alert("Success");
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
<input type=button onclick="validate('true');" value="Apply"/>
<div id="motionGroupUpdateProgressDiv" style="font-weight: bold; padding: 0px 5px; display: inline;"></div>
<div style="float:left;text-align: left; width:100%;" class="appbg">
	<form:form id="editGroupFormForMotionGroups" commandName="motionBitsSchedule" method="post" onsubmit="return false;">
		 <form:hidden path="id" id="id" /> 
			<table class="appbg">
			     <tr>
					<td><label style="font-weight: bold">Group name:</label></td>
					<td><form:input path="name" id="name" name="name" /></td>
				 </tr>	
				 
				 <tr>
					<td><label style="font-weight: bold">Frequency:</label></td>
					<%-- <td><form:input path="transmitFreq" id="transmitFreq" name="transmitFreq"/></td> --%>
					<td><form:radiobutton path="transmitFreq" id = "transmitFreq" value="1"/>One</td> 
					<td><form:radiobutton path="transmitFreq" id = "transmitFreq" value="5"/>Five</td>
				 </tr>	
				 
				 
				 <tr>
					<td><label style="font-weight: bold">Bit Level:</label></td>
					<%-- <td><form:input path="bitLevel" id="bitLevel" name="bitLevel"/></td> --%>
					<td><form:radiobutton path="bitLevel" id = "bitLevel" value="1"/>One</td> 
					<td><form:radiobutton path="bitLevel" id = "bitLevel" value="2"/>Two</td>
					
				 </tr>	
				 
				 <tr>
					<td><label style="font-weight: bold">Capture Start:</label></td>
					<td><form:input path="captureStart" id="captureStart" name="captureStart" onchange="validateTime(this);"/></td>
				 </tr>
				 
				  <tr>
					<td><label style="font-weight: bold">Capture End:</label></td>
					<td><form:input path="captureEnd" id="captureEnd" name="captureEnd" onchange="validateTime(this);"/></td>
				 </tr>
				 
			</table>
		</form:form>
</div>
</div>
 

  

<div id="accordionfacilitydlg" class="left-menu">

	<h2 id="h2facilitydlg" onclick="SetDlgAccTabSelected('fcog');"><a href="#">Facilities</a></h2>
<!--	<div class="accinner">
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
 