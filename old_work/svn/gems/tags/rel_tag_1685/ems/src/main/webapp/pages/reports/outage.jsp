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

<script type="text/javascript">
	$(document).ready(function(){
		nodeclick();
		showflash();
		$("#maindiv").css("height", $(window).height() - 140);
	});
	
	function nodeclick() {
		$('#facilityTreeViewDiv').treenodeclick(function(){	
			showflash();
		});
	}
	
	//common function to show floor plan for selected node
	var showflash=function(){
		//variable coming from LHS tree
		removeWheelEvent();
		loadFloorPlan();	
	}
	
	function removeWheelEvent() {
		if(window.addEventListener) {
	        var eventType = ($.browser.mozilla) ? "DOMMouseScroll" : "mousewheel";            
	        window.removeEventListener(eventType, handleWheel, false);
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
	
	function loadFloorPlan(){
		loadFP();
	}

	var loadFP = function() {
		try{
			if(window.addEventListener) {
	            var eventType = ($.browser.mozilla) ? "DOMMouseScroll" : "mousewheel";            
	            window.addEventListener(eventType, handleWheel, false);
	            getFloorPlanObj("floorplan").onmousemove=null; // Handling poor mouse wheel behavior in Internet Explorer.
	        }
			getFloorPlanObj("floorplan").changeLevel(treenodetype, treenodeid, 'REPORT','OUTAGE');
		}
		catch (ex){
			flash_fp(treenodetype, treenodeid);
		}
	}
	
	//**** Keep functions global or refresh tree functionality might break. *********//
	var getFloorPlanObj = function(objectName) {			
		if ($.browser.mozilla) {
			return document[objectName] 
		}
		return document.getElementById(objectName);
	}
	
	var flash_fp = function(nodetype, nodeid) {		
		var FP_data = "";
		
		var buildNumber = "";
		
		var versionString = "<ems:showAppVersion />";
		
		var indexNumber = versionString.lastIndexOf('.', (versionString.length)-1);
		
		if(indexNumber != -1 ){
			buildNumber = versionString.slice(indexNumber+1);
		}else{
			buildNumber = Math.floor(Math.random()*10000001);// For Development Version
		}
		
		var plotchartmoduleString = "${plotchartmodule}"+"?buildNumber="+buildNumber;
		
		if ($.browser.msie) {
			FP_data = "<object id='floorplan' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='http://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='100%'>";
			FP_data +=  "<param name='src' value='"+plotchartmoduleString+"'/>";
			FP_data +=  "<param name='padding' value='0px'/>";
			FP_data +=  "<param name='wmode' value='opaque'/>";
			FP_data +=  "<param name='allowFullScreen' value='true'/>";
			FP_data +=  "<param name='flashVars' value='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=REPORT&modeid=OUTAGE'/>";
			FP_data +=  "<embed id='floorplan' name='floorplan' src='"+plotchartmoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " padding='0px'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=REPORT&modeid=OUTAGE'/>";
			FP_data +=  "</object>";
		} else {
			FP_data = "<embed id='floorplan' name='floorplan' src='"+plotchartmoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " padding='0px'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=REPORT&modeid=OUTAGE'/>";
		}
		
		var tabFP =document.getElementById("tab_fp");
		tabFP.innerHTML = FP_data; 
		// quick fix for the duplicate flash object
		$('div.alt').remove(); 
	}
</script>

<div class="outermostdiv" style="margin-left:0px;">
	<div class="outerContainer">
		<span><spring:message code="menu.outage" /></span>
	</div>
	<div id="maindiv" style="padding:5px;">
		<div id="tab_fp" class="pnl_rht"></div>
	</div>		
</div>
 