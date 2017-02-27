<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fun"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url	value="/plugloadProfile/plugload_template_profile_visibility.ems" var="plugloadGroupsVisibilityUrl" scope="request" />

<spring:url value="/scripts/jquery/jquery.cookie.20110708.js"
	var="jquery_cookie"></spring:url>
	
<script type="text/javascript" src="${jquery_cookie}"></script>

<spring:url value="/scripts/jquery/jstree/jquery.jstree.js"
	var="jquery_jstree"></spring:url>
<script type="text/javascript" src="${jquery_jstree}"></script>

<spring:url value="/scripts/jquery/jstree/themes/"
	var="jstreethemefolder"></spring:url>
	
<style>
	
</style>
<script type="text/javascript">

INITIAL_PLUGLOAD_PROFILE_FILTER_DATA = {}; //Global variable to store tenants assignments before modifing
var minOnePlugloadProfileSelected=false;

//Lists for toggle mode
var divinvisibleplugloadgroup = [];   // Contains the list of div id's of groups which will be invisible in toggle mode.
var divinvisibleplugloadtemplate = [];  // Contains the list of div id's of templates which will be invisible in toggle mode.
var togglmodevisibleall = [];   // Contains the list of div id's of groups/templates which will be visible in toggle mode. Usage : selectAllPlugload/clearAllPlugload
var plugload_associated_profile={};
var togglemode = false;         //Determines the mode

//List for show all mode
var allnodes = [];

$(document).ready(
	function() {
		
		//Make toggle mode off on page load.
		togglemode = false;
		
		<c:forEach items="${plugloadProfileTreeHierarchy.treeNodeList}" var="template">
		var makeTemplateInvisible = false;	
		var localTemplateData = new Object;
		localTemplateData.templateName = "plugloadtemplate_"+"${template.nodeId}";
		//console.log("localTemplateData.templateName:"+localTemplateData.templateName);
		localTemplateData.count="${template.count}";
		allnodes.push(localTemplateData.templateName);  // Push in all nodes by default
		
		<c:forEach items="${template.treeNodeList}" var="profile">
		var localProfileData = new Object;
		localProfileData.profileName = "plugloadgroup_"+"${profile.nodeId}";
		localProfileData.count = "${profile.count}";
		//console.log("localProfileData.profileName:"+localProfileData.profileName);
		//console.log("count:"+localProfileData.count);
		allnodes.push(localProfileData.profileName); // Push in all nodes by default		
		if(localProfileData.count > 0)
			{
			// Insert to make groups invisible
			divinvisibleplugloadgroup.push(localProfileData.profileName);		
			}
		if(localProfileData.count==0)
			{
			makeTemplateInvisible = true;
			togglmodevisibleall.push(localProfileData.profileName);
			}	
		</c:forEach>
		if(makeTemplateInvisible == false && localTemplateData.count != 0)
			{
			divinvisibleplugloadtemplate.push(localTemplateData.templateName);
			}	
		if(localTemplateData.count==0)
			{
			togglmodevisibleall.push(localTemplateData.templateName);
			}
		</c:forEach>	

		//Save initial assignments
		INITIAL_PLUGLOAD_PROFILE_FILTER_DATA = getPlugloadFormValuesJSON("PlugloadTemplateProfileAssignmentForm");
		plugload_associated_profile="${plugloadAssociatedProfile}";
		$('#applyPlugloadTemplateFilter').bind('click', function() {
			if(togglemode==false){
			var modified_ids = getModifiedPlugloadTemplateProfile();
			if(minOnePlugloadProfileSelected)
			{
				$('#selectedPlugloadTemplateProfiles').val(modified_ids);
				$('#filterPlugloadProfiletreeSubmitForm').submit();				
			}
			}
			else
				{
				alert("Click On Show All and Try Again!");
				}
		});

		//Init Tree
		$("#plugloadTemplateProfileTreeViewDiv").bind("loaded.jstree",function(event, data) {
			$("#plugloadTemplateProfileTreeViewDiv").jstree("open_all");
			if(plugload_associated_profile!=null && plugload_associated_profile.length>0)
				handlePlugloadAssociation();
		});

		$.jstree._themes = "${jstreethemefolder}";
		$("#plugloadTemplateProfileTreeViewDiv").jstree(
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
						real_checkboxes_names :function (n) { return [("check_" + (n[0].id || Math.ceil(Math.random() * 10000))), 1]; }
					},
					types: {
		                 types : {
		                     'template' : {
		                         icon : {
		                        	 image : '../themes/default/images/floor.png'
		                         }
		                     },
		                     'group' : {
		                         icon : {
		                        	 image : '../themes/default/images/area.png'
		                         }
		                     },
		                     'default' : {
		                         icon : {
		                             image : '../themes/default/images/company.png'
		                         },
		                         valid_children : 'default'
		                     }
		                 }
		             },
					plugins : [ "themes", "html_data", "ui",
							"checkbox", "types"]
				//"cookies" ,
				});
	});
	
function loadPlugloadProfileFilter()
{	
	window.location.reload();
}

function getPlugloadFormValuesJSON(formId) {
	var serializeStr = $("#" + formId).serialize();
	var formJSON = {};
	var formValues = serializeStr.split("&");
	for ( var j = 0; j < formValues.length; j++) {
		var field = formValues[j].split("=");
		formJSON[field[0]] = field[1];
	}
	return formJSON;
}

function resetFacilityAssignmentTree() {
	for ( var p in INITIAL_PLUGLOAD_PROFILE_FILTER_DATA) {
		if (INITIAL_PLUGLOAD_PROFILE_FILTER_DATA.hasOwnProperty(p)) {
			$('select[name="' + p + '"]').val(INITIAL_PLUGLOAD_PROFILE_FILTER_DATA[p]);
		}
	}
	return false;
}
function getModifiedPlugloadTemplateProfile() {
	var modified_ids = [];
	/*var newAssignments = $("#PlugloadTemplateProfileAssignmentForm").serialize();
	var formValues = newAssignments.split("&");
	for (var j = 0; j < formValues.length; j++) {
		var cmbValues = formValues[j].split("=");
		var templateId = cmbValues[0];
		var profileId = cmbValues[1];
		//Check if it is modified
		if (INITIAL_PLUGLOAD_PROFILE_FILTER_DATA[templateId] != profileId) {
			modified_ids.push(templateId + "_" + profileId +"_t");
		}
	}*/
	//var modified_ids = []; 
	$("#plugloadTemplateProfileTreeViewDiv").find(".jstree-undetermined").each(function(i,element){      
		var nodeName= $(element).attr("id") +"_t";
		modified_ids.push(nodeName);
	});

	$("#plugloadTemplateProfileTreeViewDiv").jstree("get_checked", null, true).each(
			function() {
				minOnePlugloadProfileSelected=true;
				var nodeName= this.id +"_t";
				modified_ids.push(nodeName);
			});

	$("#plugloadTemplateProfileTreeViewDiv").jstree("get_unchecked", null, true).each(
			function() {
				var nodeName= this.id +"_f";
				modified_ids.push(nodeName);
			});
	if(minOnePlugloadProfileSelected)
	{
		return modified_ids;
	}
	else
	{
		alert("Please select at least one Plugload profile");
		return;
	}
	
}

function handlePlugloadAssociation()
{
	var profileStr="";
	for ( var j = 0; j < plugload_associated_profile.length; j++) {
		profileStr += plugload_associated_profile[j];
	}
	profileStr+=" Plugload Profiles can not deselected as plugload are associated with them";
	alert(profileStr);
}

function selectAllPlugload()
{	if(togglemode==true)
	{	
	 for ( var j = 0; j < togglmodevisibleall.length; j++) {
		var field = togglmodevisibleall[j];		
		var element = "#" + field;
		$("#plugloadTemplateProfileTreeViewDiv").jstree('check_node',element);
	}  
	}else {
	 for ( var j = 0; j < allnodes.length; j++) {
		var field = allnodes[j];		
		var element = "#" + field;
		$("#plugloadTemplateProfileTreeViewDiv").jstree('check_node',element);
	} 
	}
}

function clearAllPlugload()
{
	if(togglemode==true)
		{
		 for ( var j = 0; j < togglmodevisibleall.length; j++) {
			var field = togglmodevisibleall[j];		
			var element = "#" + field;
			$("#plugloadTemplateProfileTreeViewDiv").jstree('uncheck_node',element);
		} 		 
		}else {
	 for ( var j = 0; j < allnodes.length; j++) {
		var field = allnodes[j];		
		var element = "#" + field;
		$("#plugloadTemplateProfileTreeViewDiv").jstree('uncheck_node',element);
	} 
		}
}
function applyPlugloadTemplateToggleCall(){	
	// divinvisibleplugloadgroup - for group , divinvisibleplugloadtemplate - for template
	if(togglemode==false) {
	for ( var j = 0; j < divinvisibleplugloadgroup.length; j++) {
		var field = divinvisibleplugloadgroup[j];
		document.getElementById(field).style.display="none";
	}
	
	for ( var j = 0; j < divinvisibleplugloadtemplate.length; j++) {
		var field = divinvisibleplugloadtemplate[j];
		document.getElementById(field).style.display="none";
	}
	togglemode = true;	
	$("#applyToggle").attr('value','Show All');
	//alert("Toggle mode is ON");
	}else{
		//No togglemode
		for ( var j = 0; j < divinvisibleplugloadgroup.length; j++) {
				var field = divinvisibleplugloadgroup[j];
				document.getElementById(field).style.display="";
			}
			
			for ( var j = 0; j < divinvisibleplugloadtemplate.length; j++) {
				var field = divinvisibleplugloadtemplate[j];
				document.getElementById(field).style.display="";
			}		
		togglemode = false;
		//alert("Toggle mode is OFF");
		$("#applyToggle").attr('value','Show Unused');
	}	
 } 

</script>
<div class="outermostdiv">
<div class="outerContainer">
<div id="profile-list-topPanel" style="background:#fff">
		<div id="filterProfile-dialog-form">						
			
			<!-- <button id="applyToggle" style="float: right; margin-top: 10px;" onclick="applyPlugloadTemplateToggleCall(this);">Show Unused</button> -->			
			<input onclick="applyPlugloadTemplateToggleCall()" type="button" value="Show Unused" id="applyToggle" style="float: left; margin-top: 10px;margin-left: 10px;width:110px;"></input>
			<button id="selectAllPlugloadId" style="float: left; margin-top: 10px;margin-left: 10px;" onclick="selectAllPlugload()">Select All Shown</button>
			
			<button id="clearAllPlugloadId" style="float: left; margin-top: 10px;margin-left: 10px;" onclick="clearAllPlugload()">Deselect All Shown</button>
			
			<button id="revertAllId" style="float: right; margin-top: 10px;margin-right: 10px;" onclick="loadPlugloadProfileFilter()">Revert Changes</button>
			
			<button id="applyPlugloadTemplateFilter" style="float: right; margin-top: 10px;margin-right: 12px;">Apply Filter</button>			
			
			<div id="filterPlugloadProfileMessage">
				${message}
			</div>
		</div> 
		<br style="clear:both;"/>
		<div style="height:10px;"></div>
</div>

	<form id="PlugloadTemplateProfileAssignmentForm" action="${plugloadGroupsVisibilityUrl}" METHOD="POST">
	
				<div id="plugloadTemplateProfileTreeViewDiv" class = "treeviewbg" style="padding-bottom: 20px;">
					<ul>
						<c:forEach items='${plugloadProfileTreeHierarchy.treeNodeList}' var='template'>
							<li id='<c:out value="${template.nodeType.lowerCaseName}"  escapeXml="true" />_${template.nodeId}' 
							<c:if test="${fun:length(template.treeNodeList)==0 && (template.isSelected==true)}">class='jstree-checked'</c:if>
							class="selected"><a href='#' disabled="true" id='link_<c:out value="${template.nodeType.lowerCaseName}"/>_${template.nodeId}'>${template.name}(${template.count})</a>
								<ul>
									<c:forEach items='${template.treeNodeList}' var='profile'>
										<c:choose>
											<c:when test="${profile.name == 'Default'}">
													<li rel="${profile.nodeType.lowerCaseName}"  id='<c:out value="${profile.nodeType.lowerCaseName}"/>_${profile.nodeId}'
													<c:if test="${profile.isSelected == true}">class='jstree-checked'</c:if>
													><a href='#' disabled="true" id='link_<c:out value="${profile.nodeType.lowerCaseName}"/>_${profile.nodeId}'><c:out value="${profile.name}" escapeXml="true"/><c:out value="${profile.count}" escapeXml="true"/></a>
													</li>	
											</c:when>
											<c:otherwise>
												<li rel="${profile.nodeType.lowerCaseName}" id='<c:out value="${profile.nodeType.lowerCaseName}"/>_${profile.nodeId}'
												<c:if test="${profile.isSelected == true}">class='jstree-checked'</c:if>><a href='#' disabled="true" id='link_<c:out value="${profile.nodeType.lowerCaseName}"/>_${profile.nodeId}'><c:out value="${profile.name}" escapeXml="true"/><c:out value="(${profile.count})" escapeXml="true"/></a>
												</li>
											</c:otherwise>
										</c:choose>
									</c:forEach>
								</ul>
							</li>
						</c:forEach>
					</ul>
				</div>
	</form>
	<form id="filterPlugloadProfiletreeSubmitForm" action="${plugloadGroupsVisibilityUrl}" METHOD="POST">
		<input id="selectedPlugloadTemplateProfiles" name="selectedPlugloadTemplateProfiles" type="hidden"/>
	</form>
</div>
</div>