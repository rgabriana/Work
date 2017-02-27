<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Plugload Details</title>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
	
	.plugloaddetailstabsStyle
	{
		height: 90%;
	}
</style>

<spring:url value="/devices/plugloads/plugload_form.ems" var="plugloadFormUrl" scope="request" />
<spring:url value="/plugloadProfile/plugloadsetting.ems" var="plugloadProfileUrl" scope="request" />
<spring:url value="/modules/EnergySummaryModule.swf" var="fixtureEnergySummaryUrl" scope="request" />

<script type="text/javascript">
IS_LOADED_OVERVIEW = false;
IS_LOADED_PROFILE = false;
IS_LOADED_ENERGY = false;


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

plugloadId = "${plugloadId}";

var flash_plugload_ec = function(nodetype, nodeid) {
	
	var buildNumber = "";
	
	var versionString = "<ems:showAppVersion />";
	
	var indexNumber = versionString.lastIndexOf('.', (versionString.length)-1);
	
	if(indexNumber != -1 ){
		buildNumber = versionString.slice(indexNumber+1);
	}else{
		buildNumber = Math.floor(Math.random()*10000001);// For Development Version
	}
	
	var fixtureEnergySummaryUrlString = "${fixtureEnergySummaryUrl}"+"?buildNumber="+buildNumber;
	
	var EC_data = "";
	if ($.browser.msie) {
		EC_data = "<object id='plugload-energy-summary' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='100%'>";
		EC_data +=  "<param name='src' value='"+fixtureEnergySummaryUrlString+"'/>";
		EC_data +=  "<param name='padding' value='0px'/>";
		EC_data +=  "<param name='wmode' value='opaque'/>";
		EC_data +=  "<param name='flashvars' value='orgType=fixture" + "&orgId=" + plugloadId + "&contextRoot=" + '${contextPath}'+ "'/>";
		EC_data +=  "<embed id='plugload-energy-summary' name='plugload-energy-summary' src='"+fixtureEnergySummaryUrlString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
		EC_data +=  " height='100%'";
		EC_data +=  " width='100%'";
		EC_data +=  " padding='0px'";
		EC_data +=  " wmode='opaque'";
		EC_data +=  " flashvars='orgType=fixture" + "&orgId=" + plugloadId + "&contextRoot=" + '${contextPath}'+ "'/>";
		EC_data +=  "</object>";
	} else {
		EC_data = "<embed id='plugload-energy-summary' name='plugload-energy-summary' src='"+fixtureEnergySummaryUrlString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
		EC_data +=  " height='100%'";
		EC_data +=  " width='100%'";
		EC_data +=  " wmode='opaque'";
		EC_data +=  " padding='0px'";
		EC_data +=  " flashvars='orgType=fixture" + "&orgId=" + plugloadId + "&contextRoot=" + '${contextPath}'+ "'/>";
	}
	
	var tabEC =document.getElementById("tab-pg-energy");
	tabEC.innerHTML = EC_data; 
}

var loadPlugloadEC = function() {
	try{
		getFloorPlanObj("plugload-energy-summary").updateEnergyConsumption(treenodetype, treenodeid, "day");	
	}
	catch (ex){
		flash_plugload_ec(treenodetype, treenodeid);
	}
}


$("#plugload-details-tabs").tabs({
		selected: 0,
		create: function(event, ui) {
			//load overview tab content
			$("#tab-overview").load("${plugloadFormUrl}?plugloadId="+plugloadId+ "&ts="+ new Date().getTime());
			IS_LOADED_OVERVIEW = true;
		},
		select: function(event, ui) {
// 			alert(ui.index);
			switch(ui.index) {
				case 0://Overview tab selected
// 					  if(!IS_LOADED_OVERVIEW){
						  	$("#tab-overview").load("${plugloadFormUrl}?plugloadId="+plugloadId+ "&ts="+ new Date().getTime());
							IS_LOADED_OVERVIEW = true;
// 					  }
					  break;
				 case 1://Profile tab selected
// 					  if(!IS_LOADED_PROFILE){
							
						  	$("#tab-pg-profile").load("${plugloadProfileUrl}?plugloadId="+plugloadId+ "&ts="+ new Date().getTime());
						  	IS_LOADED_PROFILE = true;
// 					  }
					  break;
					  	case 2://Energy Summary tab selected
					 // if(!IS_LOADED_ENERGY){
						  	loadPlugloadEC();
						  	IS_LOADED_ENERGY = true;
					  //}
					  break;
				default:
			}
		}
	});

</script>
</head>
<body>

<div id="plugload-details-tabs" class="plugloaddetailstabsStyle" >
	<ul>
		<li><a href="#tab-overview">Overview</a></li>
		<li><a href="#tab-pg-profile">Plugload Profile</a></li>
		<li><a href="#tab-pg-energy">Energy Summary</a></li>
	</ul>
	
	<div id="tab-overview">&nbsp;&nbsp;<spring:message code='action.loading'/></div>
	
	<div id="tab-pg-profile">&nbsp;&nbsp;<spring:message code='action.loading'/></div>
	
	<div id="tab-pg-energy" class="tab-content">&nbsp;&nbsp;<spring:message code='action.loading'/></div>
</div>

</body>
</html>