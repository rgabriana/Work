<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fun"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url	value="/profile/template_profile_visibility.ems" var="groupsVisibilityUrl" scope="request" />

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

INITIAL_PROFILE_FILTER_DATA = {}; //Global variable to store tenants assignments before modifing
var minOneProfileSelected=false;

//Lists for toggle mode
var divinvisible = [];   // Contains the list of div id's of groups which will be invisible in toggle mode.
var divinvisibletemplate = [];  // Contains the list of div id's of templates which will be invisible in toggle mode.
var togglmodevisibleall = [];   // Contains the list of div id's of groups/templates which will be visible in toggle mode. Usage : SelectAll/ClearAll
var fixture_associated_profile={};
var togglemode = false;         //Determines the mode

//List for show all mode
var allnodes = [];

$(document).ready(
	function() {
		
		//Make toggle mode off on page load.
		togglemode = false;
		
		<c:forEach items="${profileTreeHierarchy.treeNodeList}" var="template">
			var makeTemplateInvisible = false;	
			var localTemplateData = new Object;
			localTemplateData.templateName = "profiletemplate_"+"${template.nodeId}";
			localTemplateData.count="${template.count}";
			allnodes.push(localTemplateData.templateName);  // Push in all nodes by default
			
			<c:forEach items="${template.treeNodeList}" var="profile">
				var localProfileData = new Object;
				localProfileData.profileName = "profilegroup_"+"${profile.nodeId}";
				localProfileData.count = "${profile.count}";
				//alert(localProfileData.profileName);
				allnodes.push(localProfileData.profileName); // Push in all nodes by default		
				if(localProfileData.count > 0)
				{
					// Insert to make groups invisible
					divinvisible.push(localProfileData.profileName);		
				}
				if(localProfileData.count==0)
				{
					makeTemplateInvisible = true;
					togglmodevisibleall.push(localProfileData.profileName);
				}	
			</c:forEach>
			if(makeTemplateInvisible == false && localTemplateData.count != 0)
			{
				divinvisibletemplate.push(localTemplateData.templateName);
			}	
			if(localTemplateData.count==0)
			{
				togglmodevisibleall.push(localTemplateData.templateName);
			}
		</c:forEach>	

		//Save initial assignments
		INITIAL_PROFILE_FILTER_DATA = getFormValuesJSON("templateProfileAssignmentForm");
		fixture_associated_profile="${fixtureAssociatedProfile}";
		$('#applyFilter').bind('click', function() {
			if(togglemode==false){
			var modified_ids = getModifiedTemplateProfile();
			if(minOneProfileSelected)
			{
				$('#selectedTemplateProfiles').val(modified_ids);
				$('#filterProfiletreeSubmitForm').submit();				
			}
			}
			else
				{
				alert("Click On Show All and Try Again!");
				}
		});

		//Init Tree
		$("#templateProfileTreeViewDiv").bind("loaded.jstree",function(event, data) {
			$("#templateProfileTreeViewDiv").jstree("open_all");
			if(fixture_associated_profile!=null && fixture_associated_profile.length>0)
				handleFixtureAssociation();
		});

		$.jstree._themes = "${jstreethemefolder}";
		$("#templateProfileTreeViewDiv").jstree(
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
		                     'profiletemplate' : {
		                         icon : {
		                        	 image : '../themes/default/images/floor.png'
		                         }
		                     },
		                     'profilegroup' : {
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
	
function loadFilterProfileFilter()
{	
	window.location.reload();
}

function getFormValuesJSON(formId) {
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
	for ( var p in INITIAL_PROFILE_FILTER_DATA) {
		if (INITIAL_PROFILE_FILTER_DATA.hasOwnProperty(p)) {
			$('select[name="' + p + '"]').val(INITIAL_PROFILE_FILTER_DATA[p]);
		}
	}
	return false;
}
function getModifiedTemplateProfile() {
	var modified_ids = [];
	/*var newAssignments = $("#templateProfileAssignmentForm").serialize();
	var formValues = newAssignments.split("&");
	for (var j = 0; j < formValues.length; j++) {
		var cmbValues = formValues[j].split("=");
		var templateId = cmbValues[0];
		var profileId = cmbValues[1];
		//Check if it is modified
		if (INITIAL_PROFILE_FILTER_DATA[templateId] != profileId) {
			modified_ids.push(templateId + "_" + profileId +"_t");
		}
	}*/
	//var modified_ids = []; 
	$("#templateProfileTreeViewDiv").find(".jstree-undetermined").each(function(i,element){      
		var nodeName= $(element).attr("id") +"_t";
		modified_ids.push(nodeName);
	});

	$("#templateProfileTreeViewDiv").jstree("get_checked", null, true).each(
			function() {
				minOneProfileSelected=true;
				var nodeName= this.id +"_t";
				modified_ids.push(nodeName);
			});

	$("#templateProfileTreeViewDiv").jstree("get_unchecked", null, true).each(
			function() {
				var nodeName= this.id +"_f";
				modified_ids.push(nodeName);
			});
	if(minOneProfileSelected)
	{
		return modified_ids;
	}
	else
	{
		alert("Please select at least one profile");
		return;
	}
	
}

function handleFixtureAssociation()
{
	var profileStr="";
	for ( var j = 0; j < fixture_associated_profile.length; j++) {
		profileStr += fixture_associated_profile[j];
	}
	profileStr+=" Profiles can not deselected as fixture are associated with them";
	alert(profileStr);
}

function selectAll()
{	if(togglemode==true)
	{	
	 for ( var j = 0; j < togglmodevisibleall.length; j++) {
		var field = togglmodevisibleall[j];		
		var element = "#" + field;
		$("#templateProfileTreeViewDiv").jstree('check_node',element);
	}  
	}else {
	 for ( var j = 0; j < allnodes.length; j++) {
		var field = allnodes[j];		
		var element = "#" + field;
		$("#templateProfileTreeViewDiv").jstree('check_node',element);
	} 
	}
}

function clearAll()
{
	if(togglemode==true)
		{
		 for ( var j = 0; j < togglmodevisibleall.length; j++) {
			var field = togglmodevisibleall[j];		
			var element = "#" + field;
			$("#templateProfileTreeViewDiv").jstree('uncheck_node',element);
		} 		 
		}else {
	 for ( var j = 0; j < allnodes.length; j++) {
		var field = allnodes[j];		
		var element = "#" + field;
		$("#templateProfileTreeViewDiv").jstree('uncheck_node',element);
	} 
		}
}
function applyToggleCall(){	
	// divinvisible - for group , divinvisibletemplate - for template
	if(togglemode==false) {
	for ( var j = 0; j < divinvisible.length; j++) {
		var field = divinvisible[j];
		document.getElementById(field).style.display="none";
	}
	
	for ( var j = 0; j < divinvisibletemplate.length; j++) {
		var field = divinvisibletemplate[j];
		document.getElementById(field).style.display="none";
	}
	togglemode = true;	
	$("#applyToggle").attr('value','Show All');
	//alert("Toggle mode is ON");
	}else{
		//No togglemode
		for ( var j = 0; j < divinvisible.length; j++) {
				var field = divinvisible[j];
				document.getElementById(field).style.display="";
			}
			
			for ( var j = 0; j < divinvisibletemplate.length; j++) {
				var field = divinvisibletemplate[j];
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
			
			<!-- <button id="applyToggle" style="float: right; margin-top: 10px;" onclick="applyToggleCall(this);">Show Unused</button> -->			
			<input onclick="applyToggleCall()" type="button" value="Show Unused" id="applyToggle" style="float: left; margin-top: 10px;margin-left: 10px;width:110px;"></input>
			<button id="selectAll" style="float: left; margin-top: 10px;margin-left: 10px;" onclick="selectAll()">Select All Shown</button>
			
			<button id="clearAll" style="float: left; margin-top: 10px;margin-left: 10px;" onclick="clearAll()">Deselect All Shown</button>
			
			<button id="revertAll" style="float: right; margin-top: 10px;margin-right: 10px;" onclick="loadFilterProfileFilter()">Revert Changes</button>
			
			<button id="applyFilter" style="float: right; margin-top: 10px;margin-right: 12px;">Apply Filter</button>			
			
			<div id="filterProfileMessage">
				${message}
			</div>
		</div> 
		<br style="clear:both;"/>
		<div style="height:10px;"></div>
</div>

	<form id="templateProfileAssignmentForm" action="${groupsVisibilityUrl}" METHOD="POST">
	
	
				<div id="templateProfileTreeViewDiv" class = "treeviewbg" style="padding-bottom: 20px;">
				
					<ul>
						<c:forEach items='${profileTreeHierarchy.treeNodeList}' var='template'>
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
	<form id="filterProfiletreeSubmitForm" action="${groupsVisibilityUrl}" METHOD="POST">
		<input id="selectedTemplateProfiles" name="selectedTemplateProfiles" type="hidden"/>
	</form>
</div>
</div>