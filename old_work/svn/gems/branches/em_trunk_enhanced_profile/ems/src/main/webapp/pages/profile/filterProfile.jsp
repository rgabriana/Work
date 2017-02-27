<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fun"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url	value="/profiles/filterProfile.ems" var="filterProfileurl" scope="request" />
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
var PAGE = "${page}";
INITIAL_PROFILE_FILTER_DATA = {}; //Global variable to store tenants assignments before modifing

$(document).ready(
	function() {
		//Save initial assignments
		INITIAL_PROFILE_FILTER_DATA = getFormValuesJSON("templateProfileAssignmentForm");

		$('#applyFilter').bind('click', function() {
			var modified_ids = getModifiedTemplateProfile();			
			$('#selectedTemplateProfiles').val(modified_ids);
			$('#filterProfiletreeSubmitForm').submit();						
		});

		//Init Tree
		$("#templateProfileTreeViewDiv").bind("loaded.jstree",function(event, data) {
					$("#templateProfileTreeViewDiv").jstree("open_all");
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
					plugins : [ "themes", "html_data",
							"checkbox", "types"]
				//"cookies" ,
				});
	});
	
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
	var newAssignments = $("#templateProfileAssignmentForm").serialize();
	var formValues = newAssignments.split("&");
	for (var j = 0; j < formValues.length; j++) {
		var cmbValues = formValues[j].split("=");
		var templateId = cmbValues[0];
		var profileId = cmbValues[1];
		//Check if it is modified
		if (INITIAL_PROFILE_FILTER_DATA[templateId] != profileId) {
			modified_ids.push(templateId + "_" + profileId +"_t");
		}
	}
	//var modified_ids = []; 
	$("#templateProfileTreeViewDiv").find(".jstree-undetermined").each(function(i,element){      
		var nodeName= $(element).attr("id") +"_t";
		modified_ids.push(nodeName);
	});

	$("#templateProfileTreeViewDiv").jstree("get_checked", null, true).each(
			function() {
				var nodeName= this.id +"_t";
				modified_ids.push(nodeName);
			});

	$("#templateProfileTreeViewDiv").jstree("get_unchecked", null, true).each(
			function() {
				var nodeName= this.id +"_f";
				modified_ids.push(nodeName);
			});
	
	return modified_ids;
}

</script>
<div class="outermostdiv">
<div class="outerContainer">
<div id="profile-list-topPanel" style="background:#fff">
		<div id="filterProfile-dialog-form">
			<button id="applyFilter" style="float: left; margin-top: 10px;"">Apply Filter</button>
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
							class="selected"><a href='#' disabled="true">${template.name}</a>
								<ul>
									<c:forEach items='${template.treeNodeList}' var='profile'>
										<c:choose>
											<c:when test="${profile.name == 'Default'}">
													<li rel="${profile.nodeType.lowerCaseName}"  id='<c:out value="${profile.nodeType.lowerCaseName}"/>_${profile.nodeId}'
													<c:if test="${profile.isSelected == true}">class='jstree-checked'</c:if>
													><a href='#' disabled="true"><c:out value="${profile.name}" escapeXml="true"/></a>
													</li>	
											</c:when>
											<c:when test="${template.name == profile.name}">
												<li rel="${profile.nodeType.lowerCaseName}" id='<c:out value="${profile.nodeType.lowerCaseName}"/>_${profile.nodeId}'
												<c:if test="${profile.isSelected == true}">class='jstree-checked'</c:if>><a href='#' disabled="true"><c:out value="${profile.name}_Default" escapeXml="true"/></a>
												</li>
											</c:when>
											<c:otherwise>
												<li rel="${profile.nodeType.lowerCaseName}" id='<c:out value="${profile.nodeType.lowerCaseName}"/>_${profile.nodeId}'
												<c:if test="${profile.isSelected == true}">class='jstree-checked'</c:if>><a href='#' disabled="true"><c:out value="${profile.name}" escapeXml="true"/></a>
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