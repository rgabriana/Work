<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Fixture Details</title>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
</style>

<spring:url value="/devices/fixtures/fixture_form.ems" var="fixtureFormUrl" scope="request" />
<spring:url value="/profile/fixturesetting.ems" var="fixtureProfileUrl" scope="request" />
<spring:url value="/modules/EnergySummaryModule.swf" var="fixtureEnergySummaryUrl" scope="request" />

<script type="text/javascript">
IS_LOADED_OVERVIEW = false;
IS_LOADED_PROFILE = false;
IS_LOADED_ENERGY = false;

fixtureId = "${fixtureId}";

var flash_fx_ec = function(nodetype, nodeid) {
	/*
	$('#tab-fx-energy').flash({
		id: 'fx-energy-summary', 
		src: '${fixtureEnergySummaryUrl}',
		width: '100%',
		height: "700px",
		padding: '0px',
		wmode: 'opaque',
		flashvars: { orgType: "fixture", orgId: fixtureId }
	});
	*/
	
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
		EC_data = "<object id='fx-energy-summary' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='http://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='700px'>";
		EC_data +=  "<param name='src' value='"+fixtureEnergySummaryUrlString+"'/>";
		EC_data +=  "<param name='padding' value='0px'/>";
		EC_data +=  "<param name='wmode' value='opaque'/>";
		EC_data +=  "<param name='flashvars' value='orgType=fixture" + "&orgId=" + fixtureId + "&contextRoot=" + '${contextPath}'+ "'/>";
		EC_data +=  "<embed id='fx-energy-summary' name='fx-energy-summary' src='"+fixtureEnergySummaryUrlString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
		EC_data +=  " height='700px'";
		EC_data +=  " width='100%'";
		EC_data +=  " padding='0px'";
		EC_data +=  " wmode='opaque'";
		EC_data +=  " flashvars='orgType=fixture" + "&orgId=" + fixtureId + "&contextRoot=" + '${contextPath}'+ "'/>";
		EC_data +=  "</object>";
	} else {
		EC_data = "<embed id='fx-energy-summary' name='fx-energy-summary' src='"+fixtureEnergySummaryUrlString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
		EC_data +=  " height='700px'";
		EC_data +=  " width='100%'";
		EC_data +=  " wmode='opaque'";
		EC_data +=  " padding='0px'";
		EC_data +=  " flashvars='orgType=fixture" + "&orgId=" + fixtureId + "&contextRoot=" + '${contextPath}'+ "'/>";
	}
	
	var tabEC =document.getElementById("tab-fx-energy");
	tabEC.innerHTML = EC_data; 
}

var loadFxEC = function() {
	try{
		getFloorPlanObj("fx-energy-summary").updateEnergyConsumption(treenodetype, treenodeid, "day");	
	}
	catch (ex){
		flash_fx_ec(treenodetype, treenodeid);
	}
}

$("#fixture-details-tabs").tabs({
		selected: 0,
		create: function(event, ui) {
			//load overview tab content
			$("#tab-overview").load("${fixtureFormUrl}?fixtureId="+fixtureId+ "&ts="+ new Date().getTime());
			IS_LOADED_OVERVIEW = true;
		},
		select: function(event, ui) {
// 			alert(ui.index);
			switch(ui.index) {
				case 0://Overview tab selected
// 					  if(!IS_LOADED_OVERVIEW){
						  	$("#tab-overview").load("${fixtureFormUrl}?fixtureId="+fixtureId+ "&ts="+ new Date().getTime());
							IS_LOADED_OVERVIEW = true;
// 					  }
					  break;
				case 1://Profile tab selected
// 					  if(!IS_LOADED_PROFILE){
						  	$("#tab-fx-profile").load("${fixtureProfileUrl}?fixtureId="+fixtureId+ "&ts="+ new Date().getTime());
						  	IS_LOADED_PROFILE = true;
// 					  }
					  break;
				case 2://Energy Summary tab selected
					  if(!IS_LOADED_ENERGY){
						  	loadFxEC();
						  	IS_LOADED_ENERGY = true;
					  }
					  break;
				default:
			}
		}
	});

</script>
</head>
<body>

<div id="fixture-details-tabs">
	<ul>
		<li><a href="#tab-overview">Overview</a></li>
		<li><a href="#tab-fx-profile">Profile</a></li>
		<li><a href="#tab-fx-energy">Energy Summary</a></li>
	</ul>
	
	<div id="tab-overview">&nbsp;&nbsp;<spring:message code='action.loading'/></div>
	
	<div id="tab-fx-profile">&nbsp;&nbsp;<spring:message code='action.loading'/></div>
	
	<div id="tab-fx-energy" class="tab-content"><%--&nbsp;&nbsp;<spring:message code='action.loading'/>--%></div>
</div>

</body>
</html>