<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib prefix="sec"	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
<spring:url value="/modules/EnergySummaryModule.swf" var="energysummarymodule"></spring:url>

<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<script type="text/javascript">
$(document).ready(function() {
	
	var innerLayout;
	innerLayout = $('div.pane-center').layout( layoutSettings_Inner );
	
	//create tabs
	$("#innercenter").tabs({
		cache: true
	});
	nodeclick();
	loadEC();
	tabselected = 'energy consumption';
	

});
var tabselected;

var getFloorPlanObj = function(objectName) {			
	if ($.browser.mozilla) {
		return document[objectName] 
	}
	return document.getElementById(objectName);
}
function nodeclick() {
	$('#dashboardTreeViewDiv').treenodeclick(function(){	
		showflash();					
	});
}
//common function to show floor plan for selected node
var showflash=function(){	
	if (tabselected == 'energy consumption') {
		$("#ec").click();		
	}
}
var flash_ec = function(nodetype, nodeid) {
	var EC_data = "";
	if ($.browser.msie) {
		EC_data = "<object id='energysummary' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='http://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='100%'>";
		EC_data +=  "<param name='src' value='${energysummarymodule}'/>";
		EC_data +=  "<param name='padding' value='0px'/>";
		EC_data +=  "<param name='wmode' value='opaque'/>";
		EC_data +=  "<param name='flashvars' value='orgType=" + nodetype + "&orgId=" + nodeid + "'/>";
		EC_data +=  "<embed id='energysummary' name='energysummary' src='${energysummarymodule}' pluginspage='http://www.adobe.com/go/getflashplayer'";
		EC_data +=  " height='100%'";
		EC_data +=  " width='100%'";
		EC_data +=  " padding='0px'";
		EC_data +=  " wmode='opaque'";
		EC_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid +  "&contextRoot=" + '${contextPath}' +"'/>";
		EC_data +=  "</object>";
	} else {
		EC_data = "<embed id='energysummary' name='energysummary' src='${energysummarymodule}' pluginspage='http://www.adobe.com/go/getflashplayer'";
		EC_data +=  " height='100%'";
		EC_data +=  " width='100%'";
		EC_data +=  " wmode='opaque'";
		EC_data +=  " padding='0px'";
		EC_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid +  "&contextRoot=" + '${contextPath}' + "'/>";
	}
	
	var tabEC =document.getElementById("tab_ec");
	tabEC.innerHTML = EC_data; 

	// quick fix for the duplicate flash object
	$('div.alt').remove(); 
}


var loadEC = function() {
	var tempnodetype;
	var tempnodid;
	
	//Load energy consumption based on accordion tab selected.
	if(accTabSelected=='pf'){
		tempnodetype = profilenodetype;
		tempnodid = profilenodeid;
	}
	else{
		tempnodetype = treenodetype;
		tempnodid = treenodeid;
	}
		
	try{
		getFloorPlanObj("energysummary").updateEnergyConsumption(tempnodetype, tempnodid, "day");	
	}
	catch (ex){
		flash_ec(tempnodetype, tempnodid);
	}
}

function loadEnergyConsumption(){
	//removeWheelEvent();
	loadEC();
}
/*
 * This Function will set cookies for the Energy Consumption Flash component to store period/Unit selected by user
 */
function setFlashCookies(name, value, days)
{
	var expiresTime;
	var pathStr = '${contextPath}';
	 if(days)
	 {
	 	var date = new Date();
	 	date.setTime(date.getTime()+(days*24*60*60*1000));
	 	expiresTime = date;
	 }else
	 	 expiresTime ="";
	 
	 $.cookie(name, value, { expires: expiresTime, path : pathStr});
	
}
/*
 * This Function will return cookies for the Energy Consumption Flash component
 */
function getFlashCookies(name)
{
	var name = $.cookie(name);
	return name;
}

/*
 * This Function will return cookies for the Energy Consumption Flash component
 */
function deleteFlashCookies(name)
{
	 $.cookie(name,null);
}
</script>
</head>
<body>
<div id="innercenter" class="ui-layout-center">
	<ul>		
		<li><a id="ec" href="#tab_ec" onclick="loadEnergyConsumption();"><span>Energy Consumption</span></a></li>
	</ul>
	<div class="ui-layout-content ui-widget-content ui-corner-bottom" style="border-left: 0; border-top: 0; padding: 0px;">
		<div id="tab_ec" class="pnl_rht flasharea"></div>
	</div>		
</div>
</body>
</html>

