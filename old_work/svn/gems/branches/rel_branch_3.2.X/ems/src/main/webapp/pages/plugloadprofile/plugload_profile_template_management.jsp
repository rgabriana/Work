<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>


<spring:url value="/plugloadProfileTemplateManagement/list.ems" var="plugloadTemplateSettingUrl" />

<spring:url value="/plugloadProfile/filterPlugloadProfile.ems" var="filterPlugloadProfileUrl" />

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
	$("#plugloadtemplateSetting").click();
	$(".ui-layout-center").css("overflow","hidden");
	
	//Chrome issue ENL 2667 - Fix 
	var evt = document.createEvent('UIEvents');
    evt.initUIEvent('resize', true, false,window,0);
    window.dispatchEvent(evt);
    //Chrome issue ENL 2667
});

function loadPlugloadTemplateSetting()
{

	tabselected = 'plugloadtemplateSetting';
	$("#tab_plugloadtemplateSetting").show();		
	$("#tab_filterPlugloadProfile").hide();
    var ifr;
    ifr = document.getElementById("plugloadtemplateSettingFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
   	ifr.src="${plugloadTemplateSettingUrl}?ts="+new Date().getTime();
    return false;
}

function loadFilterPlugloadProfile()
{
	tabselected = 'filterPlugloadProfile';
	$("#tab_plugloadtemplateSetting").hide();		
	$("#tab_filterPlugloadProfile").show();
    var ifr;
    ifr = document.getElementById("filterPlugloadProfileFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
   	ifr.src="${filterPlugloadProfileUrl}";    
    return false;
}

</script>

<div id="innercenter" class="ui-layout-center outermostdiv outerContainer" >
	<ul>		
				 
        <security:authorize access="hasAnyRole('Admin','FacilitiesAdmin','TenantAdmin')">
				<li id="litempsetting"  style="display:none"><a id="plugloadtemplateSetting" href="#tab_plugloadtemplateSetting" onclick="loadPlugloadTemplateSetting();"><span>Plugload Template Settings</span></a></li>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin','TenantAdmin')">
			<li id="lifiltprofile" style="display:none"><a id="filterPlugloadProfile" href="#tab_filterPlugloadProfile" onclick="loadFilterPlugloadProfile();"><span>Plugload Profile Filter Settings</span></a></li>
		</security:authorize>
		
	</ul>
	<div class="ui-layout-content ui-widget-content ui-corner-bottom" style="border-left: 0; border-top: 0; padding: 0px; width: 100%; height: 100%;">
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin','TenantAdmin')">
			<div id="tab_plugloadtemplateSetting" class="pnl_rht"><iframe frameborder="0" id="plugloadtemplateSettingFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin','TenantAdmin')">
			<div id="tab_filterPlugloadProfile" class="pnl_rht"><iframe frameborder="0" id="filterPlugloadProfileFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>
		
	</div>		
</div>