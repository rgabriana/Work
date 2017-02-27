<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<spring:url value="/modules/PlotChartModule.swf" var="plotchartmodule"></spring:url>
<spring:url value="/modules/EnergySummaryModule.swf" var="energysummarymodule"></spring:url>
<spring:url value="/admin/organization/setting.ems" var="loadSetting"/>
<spring:url value="/events/list.ems" var="loadEvents"/>
<spring:url value="/admin/organization/installdevices.ems" var="installDevices"/>
<spring:url value="/profile/setting.ems" var="loadProfile"/>
<spring:url value="/profile/profilesfixturessettings.ems" var="loadProfilesFixturesSettings"/>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<script type="text/javascript">
	$(document).ready(function(){
		nodeclick();
		loadEnergyConsumption();
		$("#maindiv").css("height", $(window).height() - 140);
	});
	
	function nodeclick() {
		$('#facilityTreeViewDiv').treenodeclick(function(){	
			loadEnergyConsumption();
		});
	}
	
	function loadEnergyConsumption(){
		removeWheelEvent();
		loadEC();
	}

	function removeWheelEvent() {
		if(window.addEventListener) {
	        var eventType = ($.browser.mozilla) ? "DOMMouseScroll" : "mousewheel";            
	        window.removeEventListener(eventType, handleWheel, false);
	    }
	}
	
	var loadEC = function() {
		try{
			getFloorPlanObj("energysummary").updateEnergyConsumption(treenodetype, treenodeid, "day");	
		}
		catch (ex){
			flash_ec(treenodetype, treenodeid);
		}
	}

	function handleWheel(event) {
		var app = document.getElementById("YOUR_APPLICATION");
	    var edelta = ($.browser.mozilla) ? -event.detail : event.wheelDelta/40;                                   
	    var o = {x: event.screenX, y: event.screenY, 
	        delta: edelta,
	        ctrlKey: event.ctrlKey, altKey: event.altKey, 
	        shiftKey: event.shiftKey}
		if (getFloorPlanObj("floorplan") != null)
	    	getFloorPlanObj("floorplan").handleWheel(o);
	}
		
	//**** Keep functions global or refresh tree functionality might break. *********//
	var getFloorPlanObj = function(objectName) {			
		if ($.browser.mozilla) {
			return document[objectName] 
		}
		return document.getElementById(objectName);
	}
	
	var flash_ec = function(nodetype, nodeid) {
		
		var buildNumber = "";
		
		var versionString = "<ems:showAppVersion />";
		
		var indexNumber = versionString.lastIndexOf('.', (versionString.length)-1);
		
		if(indexNumber != -1 ){
			buildNumber = versionString.slice(indexNumber+1);
		}else{
			buildNumber = Math.floor(Math.random()*10000001);// For Development Version
		}
		
		var energysummarymoduleString = "${energysummarymodule}"+"?buildNumber="+buildNumber;
		
		var EC_data = "";
		if ($.browser.msie) {
			EC_data = "<object id='energysummary' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='http://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='100%'>";
			EC_data +=  "<param name='src' value='"+energysummarymoduleString+"'/>";
			EC_data +=  "<param name='padding' value='0px'/>";
			EC_data +=  "<param name='wmode' value='opaque'/>";
			EC_data +=  "<param name='flashvars' value='orgType=" + nodetype + "&orgId=" + nodeid +  "&contextRoot=" + '${contextPath}' +"&mode=REPORT&modeid=OUTAGE'/>";
			EC_data +=  "<embed id='energysummary' name='energysummary' src='"+energysummarymoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
			EC_data +=  " height='100%'";
			EC_data +=  " width='100%'";
			EC_data +=  " padding='0px'";
			EC_data +=  " wmode='opaque'";
			EC_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid +  "&contextRoot=" + '${contextPath}' + "&mode=REPORT&modeid=OUTAGE" + "'/>";
			EC_data +=  "</object>";
		} else {
			EC_data = "<embed id='energysummary' name='energysummary' src='"+energysummarymoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
			EC_data +=  " height='100%'";
			EC_data +=  " width='100%'";
			EC_data +=  " wmode='opaque'";
			EC_data +=  " padding='0px'";
			EC_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid +  "&contextRoot=" + '${contextPath}' + "&mode=REPORT&modeid=OUTAGE" + "'/>";
		}
		
		var tabEC =document.getElementById("tab_ec");
		tabEC.innerHTML = EC_data; 

		// quick fix for the duplicate flash object
		$('div.alt').remove(); 
	}	
</script>

<div class="outermostdiv" style="margin-left:0px;">
	<div class="outerContainer">
		<span><spring:message code="menu.usage" /></span>
	</div>
	<div id="maindiv" style="padding:5px;">
		<div id="tab_ec" class="pnl_rht"></div>
	</div>		
</div>
 