<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<spring:url value="/settings/cleanUp.ems" var="cleanUp" />

<spring:url value="/settings/groups.ems" var="groups" />

<spring:url value="/devices/groups/obsoleteManage.ems" var="obsoleteGroupList" />

<style type="text/css">
	.no-close .ui-dialog-titlebar-close {display: none }
</style>

<script type="text/javascript">

var tabselected;

$(document).ready(function() {
	
	//create tabs
	$("#innercenter").tabs({
		cache: true
	});
	
	$("#ligrp").show();
	$('#licup').show();
	$("#cup").click();
	
	$(".ui-layout-center").css("overflow","hidden");
	
});

function loadCleanUp() {
	tabselected = 'cup';
	$("#tab_cup").show();		
	$("#tab_group").hide();
    var ifr;
    ifr = document.getElementById("cupFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
   	ifr.src="${cleanUp}";
    return false;
}

function loadGroups() {
	tabselected = 'grp';
	$("#tab_group").show();		
	$("#tab_cup").hide();
    var ifr;
    ifr = document.getElementById("groupFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src = "${obsoleteGroupList}";
    return false;
}


</script>

<div id="innercenter" class="ui-layout-center outermostdiv outerContainer" >
	<ul>		
				 
        <security:authorize access="hasAnyRole('Admin','FacilitiesAdmin')">
				<li id="licup"  style="display:none"><a id="cup" href="#tab_cup" onclick="loadCleanUp();"><span>Settings</span></a></li>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin')">
			<li id="ligrp" style="display:none"><a id="grp" href="#tab_group" onclick="loadGroups();"><span>Groups</span></a></li>
		</security:authorize>
		
		
	</ul>
	<div class="ui-layout-content ui-widget-content ui-corner-bottom" style="border-left: 0; border-top: 0; padding: 0px; width: 100%; height: 100%;">
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin')">
			<div id="tab_cup" class="pnl_rht"><iframe frameborder="0" id="cupFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin')">
			<div id="tab_group" class="pnl_rht"><iframe frameborder="0" id="groupFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>

		
	</div>		
</div>