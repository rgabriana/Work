<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<spring:url value="/modules/PlotChartModule.swf" var="plotchartmodule"></spring:url>
<spring:url value="/modules/EnergySummaryModule.swf" var="energysummarymodule"></spring:url>

<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<style type="text/css">
	.no-close .ui-dialog-titlebar-close {display: none }
</style>

<script type="text/javascript">
var tabselected;
    $(document).ready(function() {
		var innerLayout;
		innerLayout = $('div.pane-center').layout( layoutSettings_Inner );
		//create tabs
		$("#innercenter").tabs({
			cache: true
		});
		nodeclick();
		loadEC();
		showflash();
		setAllowedTab();
	});
</script>

<script type="text/javascript">


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


//**** Keep functions global or refresh tree functionality might break. *********//
var getFloorPlanObj = function(objectName) {			
	if ($.browser.mozilla) {
		return document[objectName] 
	}
	return document.getElementById(objectName);
}	

//fuction to show allowed tabs as per accordion tab selected
function setAllowedTab() {
	$('#accordionfacility h2').accordiontabclick(function(){				
		showflash();
	});
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
		EC_data +=  "<param name='flashvars' value='orgType=" + nodetype + "&orgId=" + nodeid +  "&contextRoot=" + '${contextPath}' +"'/>";
		EC_data +=  "<embed id='energysummary' name='energysummary' src='"+energysummarymoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
		EC_data +=  " height='100%'";
		EC_data +=  " width='100%'";
		EC_data +=  " padding='0px'";
		EC_data +=  " wmode='opaque'";
		EC_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid +  "&contextRoot=" + '${contextPath}'+ "'/>";
		EC_data +=  "</object>";
	} else {
		EC_data = "<embed id='energysummary' name='energysummary' src='"+energysummarymoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
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

var flash_fp = function(nodetype, nodeid) {		
	
	var buildNumber = "";
	
	var versionString = "<ems:showAppVersion />";
	
	var indexNumber = versionString.lastIndexOf('.', (versionString.length)-1);
	
	if(indexNumber != -1 ){
		buildNumber = versionString.slice(indexNumber+1);
	}else{
		buildNumber = Math.floor(Math.random()*10000001);// For Development Version
	}
	
	var plotchartmoduleString = "${plotchartmodule}"+"?buildNumber="+buildNumber;
	
	var FP_data = "";
	if ($.browser.msie) {
		FP_data = "<object id='floorplan' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='http://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='100%'>";
		FP_data +=  "<param name='src' value='"+plotchartmoduleString+"'/>";
		FP_data +=  "<param name='padding' value='0px'/>";
		FP_data +=  "<param name='wmode' value='opaque'/>";
		FP_data +=  "<param name='allowFullScreen' value='true'/>";
		FP_data +=  "<param name='flashVars' value='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=FLOORPLAN&role=${role}'/>";
		FP_data +=  "<embed id='floorplan' name='floorplan' src='"+plotchartmoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
		FP_data +=  " height='100%'";
		FP_data +=  " width='100%'";
		FP_data +=  " padding='0px'";
		FP_data +=  " wmode='opaque'";
		FP_data +=  " allowFullScreen='true'";
		FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=FLOORPLAN&enableMotionBits=${enableMotionBits}&role=${role}'/>";
		FP_data +=  "</object>";
	} else {
		FP_data = "<embed id='floorplan' name='floorplan' src='"+plotchartmoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
		FP_data +=  " height='100%'";
		FP_data +=  " width='100%'";
		FP_data +=  " wmode='opaque'";
		FP_data +=  " padding='0px'";
		FP_data +=  " allowFullScreen='true'";
		FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=FLOORPLAN&enableMotionBits=${enableMotionBits}&role=${role}'/>";
	}
	
	var tabFP =document.getElementById("tab_fp");
	tabFP.innerHTML = FP_data; 
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

var loadFP = function() {
	try{
		if(window.addEventListener) {
            var eventType = ($.browser.mozilla) ? "DOMMouseScroll" : "mousewheel";            
            window.addEventListener(eventType, handleWheel, false);
            getFloorPlanObj("floorplan").onmousemove=null; // Handling poor mouse wheel behavior in Internet Explorer.
        }
		getFloorPlanObj("floorplan").changeFloor(treenodetype, treenodeid, 'FLOORPLAN');	
	}
	catch (ex){
		flash_fp(treenodetype, treenodeid);
	}
}

//common function to show floor plan for selected node
var showflash=function(){	
	removeWheelEvent();
	if (tabselected == 'energy consumption') {
		if (treenodetype == 'floor' || treenodetype == 'area')
			$("#lifp").css('display','block');
		else
			$("#lifp").css('display','none');

		$("#ec").click();		
	}
	else{
		if (treenodetype == 'floor' || treenodetype == 'area') {
			$("#lifp").css('display','block');
			$("#fp").click(); //to show floor plan tab as selected							
		}
		else {
			$("#lifp").css('display','none');
			$("#ec").click();								
		}
	}
}


function handleWheel(event) {
	var app = document.getElementById("YOUR_APPLICATION");
    var edelta = ($.browser.mozilla) ? -event.detail : event.wheelDelta/40;                                   
    var o = {x: event.screenX, y: event.screenY, 
        delta: edelta,
        ctrlKey: event.ctrlKey, altKey: event.altKey, 
        shiftKey: event.shiftKey};
	if (getFloorPlanObj("floorplan") != null)
    	getFloorPlanObj("floorplan").handleWheel(o);
}

function nodeclick() {
 	$('#facilityTreeViewDiv').treenodeclick(function(){						
 		showflash();					
 	});
}

function loadEnergyConsumption(){
	removeWheelEvent();
	tabselected = 'energy consumption';
	loadEC();
}

function loadFloorPlan(){
	tabselected = 'floor plan';
	loadFP();
}

function removeWheelEvent() {
	if(window.addEventListener) {
        var eventType = ($.browser.mozilla) ? "DOMMouseScroll" : "mousewheel";            
        window.removeEventListener(eventType, handleWheel, false);
    }
}
</script>


<script type="text/javascript">


</script>

<div id="innercenter" class="ui-layout-center">
	<ul>		
		<li><a id="ec" href="#tab_ec" onclick="loadEnergyConsumption();"><span>Energy Consumption</span></a></li>
				 
        <security:authorize access="hasAnyRole('Admin','Employee','TenantAdmin','FacilitiesAdmin')">
				<li id="lifp"><a id="fp" href="#tab_fp" onclick="loadFloorPlan();"><span>Floor Plan</span></a></li>
		</security:authorize>
	</ul>
	<div class="ui-layout-content ui-widget-content ui-corner-bottom" style="border-left: 0; border-top: 0; padding: 0px;">
		<div id="tab_ec" class="pnl_rht flasharea"></div>
		
		<security:authorize access="hasAnyRole('Admin','Employee','TenantAdmin','FacilitiesAdmin')">
			<div id="tab_fp" class="pnl_rht"></div>
		</security:authorize>
	</div>		
</div>
