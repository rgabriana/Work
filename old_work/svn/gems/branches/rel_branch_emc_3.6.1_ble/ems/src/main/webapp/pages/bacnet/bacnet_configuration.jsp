<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<%-- <spring:url value="/bacnet/config.ems" var="serverConfiguration" /> --%>
<spring:url value="/bacnet/allConfDetails.ems" var="bacnetConf" />
<spring:url value="/bacnet/pointConfiguration.ems" var="pointConfiguration" />
<spring:url value="/bacnet/reportConfiguration.ems" var="reportConfiguration"></spring:url>

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
    
	//$('#libsc').show();
	$('#libconf').show();
	$("#lipoints").show();
	$("#lireports").show();
	
	if($.browser.msie && parseInt($.browser.version) == 8){
		
		$(window).bind('resize', function() {
		}).trigger('resize');
	
	}
	else{
		//Chrome issue  - Fix 
		var evt = document.createEvent('UIEvents');
	    evt.initUIEvent('resize', true, false,window,0);
	    window.dispatchEvent(evt);
	    //Chrome issue ENL 2667
	}
	
	$("#bconf").click();
	
	$(".ui-layout-center").css("overflow","hidden");
	
});

/* function loadServerConfig() {
	tabselected = 'bsc';
	$("#tab_bsc").show();
	$("#tab_bconf").hide();
	//$("#tab_points").hide();
	//$("#tab_reports").hide();
	
    var ifr;
    ifr = document.getElementById("bscFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
   	ifr.src="${serverConfiguration}";
    return false;
} */

function loadBacnetConfig() {
	tabselected = 'bconf';
	
	$("#tab_bconf").show();
	//$("#tab_bsc").hide();
	$("#tab_points").hide();
	$("#tab_reports").hide();
	
    var ifr;
    ifr = document.getElementById("bconfFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
   	ifr.src="${bacnetConf}";
    return false;
}

function loadPointsConfig() {
	tabselected = 'points';
	
	$("#tab_points").show();
	$("#tab_bsc").hide();
	$("#tab_bconf").hide();
	$("#tab_reports").hide();
	
    var ifr;
    ifr = document.getElementById("pointsFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src = "${pointConfiguration}";
    return false;
}

function loadReportsConfig() {
	tabselected = 'reports';
	
	$("#tab_reports").show();
	$("#tab_points").hide();
	$("#tab_bsc").hide();
	$("#tab_bconf").hide();
	
    var ifr;
    ifr = document.getElementById("reportsFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    //ifr.contentWindow.document.body.innerHTML = "&nbsp; To be Implement As Follows";
    ifr.src = "${reportConfiguration}";
    return false;
}

</script>

<div id="innercenter" class="ui-layout-center outermostdiv outerContainer" >
	<ul>		 
        <%-- <security:authorize access="hasAnyRole('Admin','FacilitiesAdmin')">
				<li id="libsc"  style="display:none"><a id="bsc" href="#tab_bsc" onclick="loadServerConfig();"><span>BACnet Server Configuration</span></a></li>
		</security:authorize> --%>
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin')">
				<li id="libconf"  style="display:none"><a id="bconf" href="#tab_bconf" onclick="loadBacnetConfig();"><span>BACnet Configuration Details</span></a></li>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin')">
			<li id="lipoints" style="display:none;"><a id="points" href="#tab_points" onclick="loadPointsConfig();"><span>BACnet Point Configuration</span></a></li>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin')">
			<li id="lireports" style="display:none;"><a id="reports" href="tab_reports" onclick="loadReportsConfig();"><span>BACnet Configuration Report</span></a></li>
		</security:authorize>
	</ul>
	<div class="ui-layout-content ui-widget-content ui-corner-bottom" style="border-left: 0; border-top: 0; padding: 0px; width: 100%; height: 99%;">
		
		<%-- <security:authorize access="hasAnyRole('Admin','FacilitiesAdmin')">
			<div id="tab_bsc" class="pnl_rht"><iframe frameborder="0" id="bscFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize> --%>
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin')">
			<div id="tab_bconf" class="pnl_rht" style="display:none;"><iframe frameborder="0" id="bconfFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin')">
			<div id="tab_points" class="pnl_rht"><iframe frameborder="0" id="pointsFrame" style="width: 100%; height: 100%; "></iframe></div>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin')">
			<div id="tab_reports" class="pnl_rht"><iframe frameborder="0" id="reportsFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>
		
	</div>		
</div>