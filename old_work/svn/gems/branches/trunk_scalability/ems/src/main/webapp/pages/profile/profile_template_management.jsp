<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>


<spring:url value="/profileTemplateManagement/list.ems" var="templateSettingUrl" />

<spring:url value="/profile/filterProfile.ems" var="filterProfilUrl" />

<style type="text/css">
	.no-close .ui-dialog-titlebar-close {display: none }
</style>

<script type="text/javascript">

var tabselected;
var templateId = "${id}";
$(document).ready(function() {
	
	//create tabs
	$("#innercenter").tabs({
		cache: true
	});
	
	$("#litempsetting").show();
	$('#lifiltprofile').show();
	$("#templateSetting").click();
	$(".ui-layout-center").css("overflow","hidden");
	
	//Chrome issue ENL 2667 - Fix 
	var evt = document.createEvent('UIEvents');
    evt.initUIEvent('resize', true, false,window,0);
    window.dispatchEvent(evt);
    //Chrome issue ENL 2667
});

function loadTemplateSetting()
{

	tabselected = 'templateSetting';
	$("#tab_templateSetting").show();		
	$("#tab_filterProfile").hide();
    var ifr;
    ifr = document.getElementById("templateSettingFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
   	ifr.src="${templateSettingUrl}?ts="+new Date().getTime();
    return false;
}

function loadFilterProfile()
{
	tabselected = 'filterProfile';
	$("#tab_templateSetting").hide();		
	$("#tab_filterProfile").show();
    var ifr;
    ifr = document.getElementById("filterProfileFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
   	ifr.src="${filterProfilUrl}";    
    return false;
}

</script>

<div id="innercenter" class="ui-layout-center outermostdiv outerContainer" >
	<ul>		
				 
        <security:authorize access="hasAnyRole('Admin','FacilitiesAdmin','TenantAdmin')">
				<li id="litempsetting"  style="display:none"><a id="templateSetting" href="#tab_templateSetting" onclick="loadTemplateSetting();"><span>Template Settings</span></a></li>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin','TenantAdmin')">
			<li id="lifiltprofile" style="display:none"><a id="filterProfile" href="#tab_filterProfile" onclick="loadFilterProfile();"><span>Filter Settings</span></a></li>
		</security:authorize>
		
	</ul>
	<div class="ui-layout-content ui-widget-content ui-corner-bottom" style="border-left: 0; border-top: 0; padding: 0px; width: 100%; height: 100%;">
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin','TenantAdmin')">
			<div id="tab_templateSetting" class="pnl_rht"><iframe frameborder="0" id="templateSettingFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin','TenantAdmin')">
			<div id="tab_filterProfile" class="pnl_rht"><iframe frameborder="0" id="filterProfileFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>
		
	</div>		
</div>