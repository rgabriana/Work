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
<spring:url value="/profile/list.ems" var="loadProfileManagement"/>
<spring:url value="/profile/profilesfixturessettings.ems" var="loadProfilesFixturesSettings"/>
<spring:url value="/plugloadProfile/plugloadprofilesdevicessettings.ems" var="plugloadTemplateDevicesUrl"/>
<spring:url value="/plugloadProfile/list.ems" var="plugloadTemplateSettingsUrl"/>
<spring:url value="/plugloadProfile/plugloadprofilesetting.ems" var="plugloadProfileSettingsUrl"/>
<spring:url value="/plugloadProfile/addEditPlugloadProfileSetting.ems" var="plugloadProfileAddEditUrl" scope="request" />
<spring:url value="/events/view.ems" var="eventsViewUrl" scope="request" />
<spring:url value="/profile/importprofiledialog.ems" var="uploadProfileDialogURL" scope="request" />
<spring:url value="/plugloadProfile/importplugloadprofiledialog.ems" var="uploadPlugloadProfileDialogURL" scope="request" />
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<style type="text/css">
	.no-close .ui-dialog-titlebar-close {display: none }
	#ambient-form .fieldWrapper{clear: both;height: 15px;padding: 10px;}
	.fieldWrapper .fieldLabel{float: left;font-weight: bold;width: 30%;}
	.fieldWrapper .fieldValue{float: left;width: 50%;}
</style>

<script type="text/javascript">
   	var dialogLayout;
    $(document).ready(function() {
    
		var innerLayout;
		innerLayout = $('div.pane-center').layout( layoutSettings_Inner );
		
		//create tabs
		$("#innercenter").tabs({
			cache: true
		});
		
		//create plugload profile template tabs
		$("#plugloadTemplateMainDiv").tabs({
			cache: true
		});
		
		var accordianSelect = $.cookie('accordian_select');
		
		if(accordianSelect=="plugloadprofile"){
			$("#innercenter").css("display", "none");
			$("#plugloadTemplateMainDiv").css("display", "block");
		}
		
		<c:if test="${enableProfileFeature == 'true'}">
		profilenodeclick();
		</c:if>
		
		<c:if test="${enablePlugloadProfileFeature == 'true'}">
		plugloadprofilenodeclick();
		</c:if>
		
		nodeclick();
		showflash();
		setAllowedTab();
		
		
		if(accordianSelect=="facility")
		{
			//$("#accordionfacility").accordion("activate", 2);
			$("#accordionfacility").accordion({active:"h2:last"})
			$("#lips").hide(); //hide profile tab for first time.
			$("#litmplt").hide(); 
		}
		
		var resizeTimer;
		$(window).resize(function() {
		    clearTimeout(resizeTimer);
		    resizeTimer = setTimeout(resizeEventFired, 100);
		});
		
		$("div.ui-layout-resizer").bind('mouseleave', function () {
		    document.body.style.cursor = "auto";
		});
		$("div.ui-layout-resizer").bind('mouseenter', function () {
		    document.body.style.cursor = "auto";
		});
		
		$(document).bind("refreshENLEvent", function() {
			if (tabselected == "install") {
				loadDeviceInstall();
			}
			if (tabselected == "template") {
				loadProfileTemplates();
			}
			else {
			var fpObj = getFloorPlanObj("floorplan"); 
			if (fpObj != null) {
				fpObj.plotChartRefresh();
			}
			}
		});
		
	});
</script>

<script type="text/javascript">
var tabselected;

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
		return document[objectName]; 
	}
	return document.getElementById(objectName);
};


function resizeEventFired() {
	try {
		getFloorPlanObj("floorplan").resizeEventFired();
	} catch(e) {
	}
};
function showNodeSpecificTabs()
{
	tabselected = $.cookie('em_profile_tab_selected');
	//the 'accTabSelected' variable is global and defined in facility_tree.jsp
			if(profilenodetype=='template')
			{
				//$('#templateName').text(profilenodename + ' Template');
				$("#litmplt").show(); 	
				$("#lifp").hide();
		 		$("#liin").hide();
	 			$("#lips").hide(); 	
	 			$("#install").hide();
	 			$("#events_and_faults").hide();
	 			$('#lief').hide();
	 			$('#ec').hide();
	 			$("#settingstab").show();
	 			$("#settingstab").html("Devices");
	 			$("#tmplt").click();
			}else
			{
				//$('#profileName').text(profilenodename + ' Profile');
				$("#litmplt").hide();
				$("#lifp").hide();
		 		$("#liin").hide();
	 			$("#lips").show(); 	
	 			$("#install").hide();
	 			$("#events_and_faults").show();
	 			$('#lief').show();
	 			$("#settingstab").show();
	 			$("#settingstab").html("Devices");
	 			$('#ec').show();
	 			if(tabselected == 'profile')
				{
					$("#ps").click();	
				}
	 			if (tabselected == 'energy consumption') {
					$("#ec").click();		
				}
				else if (tabselected == 'settings') {
					$("#os").click();
				}
				else if (tabselected == 'events') {
					
					$("#ef").click();		
				}
			}
}
//fuction to show allowed tabs as per accordion tab selected
function setAllowedTab() {
	$('#accordionfacility h2').accordiontabclick(function(){
		if(accTabSelected=='pf'){
			$.cookie('accordian_select', 'profile',  {path: '/' });
			showNodeSpecificTabs();
		}else if(accTabSelected=='plpf'){
			$.cookie('accordian_select', 'plugloadprofile',  {path: '/' });
		}
		else{
			$.cookie('accordian_select', 'facility',  {path: '/' });
			$('#ec').show();
 			$("#ec").click();
			$("#litmplt").hide();
			$("#liin").show();
 			$("#lips").hide();
 			$("#install").show();
 			$('#lief').show();
 			$("#events_and_faults").show();
 			$("#settingstab").html("Settings");
 			showflash();
		}	
	});
}

var flash_ec = function(nodetype, nodeid) {
	/*
	$('#tab_ec').flash(
			{
			  id: 'energysummary', 
			  src: '${energysummarymodule}',
			  width: '100%',
			  height: '100%', 
			  padding: '0px',
			  wmode: 'opaque',
			  flashvars: { orgType: nodetype, orgId: nodeid }  
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
	
	var energysummarymoduleString = "${energysummarymodule}"+"?buildNumber="+buildNumber;
		
	var EC_data = "";
	if ($.browser.msie) {
		EC_data = "<object id='energysummary' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='100%'>";
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
	
	if(nodetype == "plugloadgroup"){
		var tabEC =document.getElementById("tab_ec_plugload");
		tabEC.innerHTML = EC_data; 
	}else{
		var tabEC =document.getElementById("tab_ec");
		tabEC.innerHTML = EC_data; 
	}
	

	// quick fix for the duplicate flash object
	$('div.alt').remove(); 
}

var flash_fp = function(nodetype, nodeid) {		
	/*
	$('#tab_fp').flash(
			{
			  id: 'floorplan', 
			  src: '${plotchartmodule}',
			  width: '100%',
			  height: '100%', 
			  padding: '0px',
			  wmode: 'opaque',
			  allowFullScreen:'true',
			  flashvars: { orgType: nodetype, orgId: nodeid, mode: 'FLOORPLAN' }  
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
	
	var plotchartmoduleString = "${plotchartmodule}"+"?buildNumber="+buildNumber;
	
	var FP_data = "";
	if ($.browser.msie) {
		FP_data = "<object id='floorplan' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='100%'>";
		FP_data +=  "<param name='src' value='"+plotchartmoduleString+"'/>";
		FP_data +=  "<param name='padding' value='0px'/>";
		FP_data +=  "<param name='wmode' value='opaque'/>";
		FP_data +=  "<param name='allowFullScreen' value='true'/>";
		FP_data +=  "<param name='flashVars' value='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=FLOORPLAN&enableMotionBits=${enableMotionBits}&role=${role}&enablePlugloadFeature=${enablePlugloadProfileFeature}'/>";
		FP_data +=  "<embed id='floorplan' name='floorplan' src='"+plotchartmoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
		FP_data +=  " height='100%'";
		FP_data +=  " width='100%'";
		FP_data +=  " padding='0px'";
		FP_data +=  " wmode='opaque'";
		FP_data +=  " allowFullScreen='true'";
		FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=FLOORPLAN&enableMotionBits=${enableMotionBits}&role=${role}&enablePlugloadFeature=${enablePlugloadProfileFeature}'/>";
		FP_data +=  "</object>";
	} else {
		FP_data = "<embed id='floorplan' name='floorplan' src='"+plotchartmoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
		FP_data +=  " height='100%'";
		FP_data +=  " width='100%'";
		FP_data +=  " wmode='opaque'";
		FP_data +=  " padding='0px'";
		FP_data +=  " allowFullScreen='true'";
		FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=FLOORPLAN&enableMotionBits=${enableMotionBits}&role=${role}&enablePlugloadFeature=${enablePlugloadProfileFeature}'/>";
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


var loadPlugloadEC = function() {
	
	try{
		getFloorPlanObj("energysummary").updateEnergyConsumption(plugloadProfilenodetype, plugloadProfilenodeid, "day");	
	}
	catch (ex){
		flash_ec(plugloadProfilenodetype, plugloadProfilenodeid);
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
	//Special case for auditor were we want main page to land on energy consumption
	var roleType = '${role}';
	var role =  "auditor" ;
	if(roleType == role)
	{
		tabselected = 'energy consumption' ;
		$.cookie('em_facilities_tab_selected', 'energy consumption',  {path: '/' });
	}
	tabselected = $.cookie('em_facilities_tab_selected');
	var accordianSelect = $.cookie('accordian_select');
	if(accordianSelect==null)
	{
		$.cookie('accordian_select', 'facility',  {path: '/' });
		//$("#accordionfacility").accordion("activate", 2);
		$("#accordionfacility").accordion({active:"h2:last"})
		$("#lips").hide();
		$("#litmplt").hide(); 
	}
	if(accordianSelect=='profile')
	{
		accTabSelected='pf';
		$("#accordionfacility").accordion("activate", 0);
		showNodeSpecificTabs();
		return;
	}
	if (tabselected == 'install') {
		if (treenodetype == 'floor' || treenodetype == 'area')
			$("#lifp").css('display','block');		
		else
			$("#lifp").css('display','none');		

		$('#in').click();
	}
	else if (tabselected == 'settings') {
		if (treenodetype == 'floor' || treenodetype == 'area')
			$("#lifp").css('display','block');
		else
			$("#lifp").css('display','none');

		$('#os').click();
	}
	else if (tabselected == 'energy consumption') {
		if (treenodetype == 'floor' || treenodetype == 'area')
			$("#lifp").css('display','block');
		else
			$("#lifp").css('display','none');

		$("#ec").click();		
	}
	else if (tabselected == 'events') {
		if (treenodetype == 'floor' || treenodetype == 'area')
			$("#lifp").css('display','block');
		else
			$("#lifp").css('display','none');

		$("#ef").click();		
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
        shiftKey: event.shiftKey}
	if (getFloorPlanObj("floorplan") != null)
    	getFloorPlanObj("floorplan").handleWheel(o);
}

function nodeclick() {
	$('#facilityTreeViewDiv').treenodeclick(function(){						
		showflash();					
	});
}

//call click event for left side profile-tree view's selected node.
function profilenodeclick() {
	tabselected = $.cookie('em_profile_tab_selected');
	$('#profileTreeViewDiv').profiletreenodeclick(function(){
		if(profilenodetype=='template')
		{
			$.cookie('em_profile_tab_selected', 'template',  {path: '/' });
			//$('#templateName').text(profilenodename + ' Template');
			showNodeSpecificTabs();
		}else if(profilenodetype=='group')
		{
			if(tabselected=='template')
			{
				tabselected='profile';
				$.cookie('em_profile_tab_selected', 'profile',  {path: '/' });
			}
			showNodeSpecificTabs();
		}
	});
}

var plugloadprofilenodetypeselected;

var plugloadtemplatetabselected;

//call click event for left side plugload-profile-tree view's selected node.
function plugloadprofilenodeclick() {
	plugloadprofilenodetypeselected = $.cookie('em_plugload_profile_nodetype_selected');
	plugloadtemplatetabselected = $.cookie('em_plugloadtemplate_tab_selected');
	
	
	$('#plugloadProfileTreeViewDiv').plugloadprofiletreenodeclick(function(){
		loadPlugloadProfileTabs();
	});
}

function loadPlugloadProfileTabs(){
	if(plugloadProfilenodetype=='plugloadtemplate')
	{
		$("#liecplugload").hide();
		$("#liefplugload").hide();
		$.cookie('em_plugload_profile_nodetype_selected', 'plugloadtemplate',  {path: '/' });
		plugloadprofilenodetypeselected = "plugloadtemplate";
		if(plugloadtemplatetabselected == "plugloadtemplatedevices"){
			$("#plugloadtemplatedevices").click();
		}else if(plugloadtemplatetabselected == "plugloadtemplatesettings"){
			$("#plugloadtemplateps").click();
		}else{
			$("#plugloadtemplatedevices").click();
		}
	}else if(plugloadProfilenodetype=='plugloadgroup')
	{
		$("#liecplugload").show();
		$("#liefplugload").show();
		$.cookie('em_plugload_profile_nodetype_selected', 'plugloadgroup',  {path: '/' });
		plugloadprofilenodetypeselected = "plugloadgroup";
		if(plugloadtemplatetabselected == "plugloadtemplatedevices"){
			$("#plugloadtemplatedevices").click();
		}else if(plugloadtemplatetabselected == "plugloadtemplatesettings"){
			$("#plugloadtemplateps").click();
		}else if(plugloadtemplatetabselected == "plugloadenergyconsumption"){
			$("#ec_plugload").click();
		}else if(plugloadtemplatetabselected == "plugloadevents"){
			$("#efplugload").click();
		}else{
			$("#plugloadtemplatedevices").click();
		}
	}
	
}

function loadSettings() {
	removeWheelEvent();
	tabselected = 'settings';
	$.cookie('em_facilities_tab_selected', 'settings',  {path: '/' });
    var ifr;
    ifr = document.getElementById("settingFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    
    //Open different fixtures list for profiles and facilities
    if(accTabSelected=='pf'){
    	ifr.src="${loadProfilesFixturesSettings}?ts="+new Date().getTime();
    }
    else{
    	ifr.src="${loadSetting}?ts="+new Date().getTime();
    }
    
    return false;
}


function loadEvents() {
	removeWheelEvent();
	tabselected = 'events';
	$.cookie('em_facilities_tab_selected', 'events',  {path: '/' });
    var ifr;
    ifr = document.getElementById("eventsAndFaultsFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
   	ifr.src="${loadEvents}?ts="+new Date().getTime();
    return false;
}

function loadPlugloadEvents() {
	removeWheelEvent();
	plugloadtemplatetabselected = 'plugloadevents';
	$.cookie('em_plugloadtemplate_tab_selected', 'plugloadevents',  {path: '/' });
    var ifr;
    ifr = document.getElementById("eventsAndFaultsPlugloadFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
   	ifr.src="${loadEvents}?ts="+new Date().getTime();
    return false;
}

function loadDeviceInstall() {
	removeWheelEvent();
	tabselected = 'install';
	$.cookie('em_facilities_tab_selected', 'install',  {path: '/' });
    var ifr;
    ifr = document.getElementById("installFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src="${installDevices}?ts="+new Date().getTime();
    return false;
}

function loadPlugloadTemplateDevices() {
	removeWheelEvent();
	plugloadtemplatetabselected = 'plugloadtemplatedevices';
	$.cookie('em_plugloadtemplate_tab_selected', 'plugloadtemplatedevices',  {path: '/' });
    var ifr;
    ifr = document.getElementById("plugloadtemplatedevicesFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src="${plugloadTemplateDevicesUrl}?ts="+new Date().getTime();
    forcefitPlugloadProfileTabs();
    return false;
}

function forcefitPlugloadProfileTabs(){
	var $header = $('#header');
    var $footer = $('#footer');
    var plugloadTemplateSubDivHeight = Math.floor(($(this).height() - $header.height() - $footer.height()) * 0.97);
    $('#plugloadTemplateSubDiv').height(plugloadTemplateSubDivHeight);
}

function loadPlugloadTemplateSettings() {
	removeWheelEvent();
	plugloadtemplatetabselected = 'plugloadtemplatesettings';
	$.cookie('em_plugloadtemplate_tab_selected', 'plugloadtemplatesettings',  {path: '/' });
	
	plugloadprofilenodetypeselected = $.cookie('em_plugload_profile_nodetype_selected');   //$.cookie('em_plugload_profile_nodetype_selected', 'plugloadgroup',  {path: '/' });
	
	if(plugloadprofilenodetypeselected == "plugloadtemplate"){
		var ifr;
	    ifr = document.getElementById("plugloadtemplateSettingsFrame");
	    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	    ifr.src="${plugloadTemplateSettingsUrl}?plugloadTemplateId="+plugloadProfilenodeid+"&ts="+new Date().getTime();
	    forcefitPlugloadProfileTabs();
	    return false;
	}else if (plugloadprofilenodetypeselected == "plugloadgroup"){
		var ifr;
	    ifr = document.getElementById("plugloadtemplateSettingsFrame");
	    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	    ifr.src="${plugloadProfileSettingsUrl}?ts="+new Date().getTime();
	    forcefitPlugloadProfileTabs();
	    return false;
	}
    
}

function loadProfile() {
	removeWheelEvent();
	tabselected = 'profile';
    var ifr;
    $.cookie('em_profile_tab_selected', 'profile',  {path: '/' });
    ifr = document.getElementById("profileFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src="${loadProfile}?ts="+new Date().getTime();
    return false;
}
function loadProfileTemplates()
{
	//$('#templateName').text(profilenodename + ' Template');
	removeWheelEvent();
	tabselected = 'template';
    var ifr;
    $.cookie('em_profile_tab_selected', 'template',  {path: '/' });
    ifr = document.getElementById("templateFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src="${loadProfileManagement}?templateId="+profilenodeid+"&ts="+new Date().getTime();
    return false;
}

function loadEnergyConsumption(){
	removeWheelEvent();
	tabselected = 'energy consumption';
	$.cookie('em_facilities_tab_selected', 'energy consumption',  {path: '/' });
	loadEC();
}

function loadPlugloadEnergyConsumption(){
	removeWheelEvent();
	plugloadtemplatetabselected = 'plugloadenergyconsumption';
	$.cookie('em_plugloadtemplate_tab_selected', 'plugloadenergyconsumption',  {path: '/' });
	loadPlugloadEC();
}

function loadFloorPlan(){
	tabselected = 'floor plan';
	$.cookie('em_facilities_tab_selected', 'floor plan',  {path: '/' });
	loadFP();
}

function removeWheelEvent() {
	if(window.addEventListener) {
        var eventType = ($.browser.mozilla) ? "DOMMouseScroll" : "mousewheel";            
        window.removeEventListener(eventType, handleWheel, false);
    }
}

function triggerRefreshEvent(msg) {
	SELECTED_FIXTURES_TO_ASSIGN_SWITCH_GROUPS = [];
	SELECTED_PLUGLOAD_TO_ASSIGN_SWITCH_GROUPS = [];
	SWITCH_GROUP_VERSION =  "";
	SELECTED_FIXTURES_TO_RESET_GROUPS = [];
	SELECTED_FIXTURES = [];
	SELECTED_SWITCHES = [];
	SELECTED_FIXTURES_TO_ASSIGN_MOTION_GROUPS = [];
	SELECTED_PLUGLOADS_TO_ASSIGN_MOTION_GROUPS = [];
	MOTION_GROUP_VERSION =  "";
	
	//console.log("trigger: " + msg);
	$(document).trigger({
		type: "refreshENLEvent",
		message: msg,
		time: new Date()
	});
}
</script>
<spring:url value="/services/org/gateway/reboot/" var="rebootGatewayUrl" scope="request" />
<spring:url value="/devices/gateways/gateway_form.ems" var="updatwGatewayUrl" scope="request" />
<spring:url value="/devices/gateways/updateGateway.ems" var="gatewaySubmitURL" scope="request"/>
<spring:url value="/devices/gateways/gateway_commission_form.ems" var="commissionGatewayUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_form.ems" var="fixtureFormUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_details.ems" var="fixtureDetailsUrl" scope="request" />
<spring:url value="/devices/plugloads/plugload_details.ems" var="plugloadDetailsUrl" scope="request" />
<spring:url value="/devices/switches/switch_create.ems" var="switchCreateUrl" scope="request" />
<spring:url value="/devices/switches/switch_edit.ems" var="switchEditUrl" scope="request" />
<spring:url value="/motionbits/create.ems" var="mbScheduleCreateUrl" scope="request" />
<spring:url value="/motionbits/edit.ems" var="mbScheduleEditUrl" scope="request" />
<spring:url value="/devices/switches/create_switch_from_flex.ems" var="createSwitchFromFlexUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_commission_form.ems" var="commissionFixtureUrl" scope="request" />
<spring:url value="/devices/fixtures/placed_fixture_commission_form.ems" var="placedCommissionFixtureUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_commission_start_identify.ems" var="startCommissionFixtureUrl" scope="request" />
<spring:url value="/devices/fixtures/placed_fixture_commission_start_identify.ems" var="startCommissionPlacedFixtureUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_discovery_form.ems" var="discoverFixtureUrl" scope="request" />
<spring:url value="/devices/plugloads/plugload_discovery_form.ems" var="discoverPlugloadeUrl" scope="request" />
<spring:url value="/devices/plugloads/plugload_commission_start_identify.ems" var="startCommissionPlugloadUrl" scope="request" />
<spring:url value="/devices/plugloads/plugload_commission_form.ems" var="commissionPlugloadUrl" scope="request" />
<spring:url value="/devices/wds/wds_commission_start_identify.ems" var="startCommissionWdsUrl" scope="request" />
<spring:url value="/devices/wds/wds_commission_form.ems" var="commissionWdsUrl" scope="request" />
<spring:url value="/devices/wds/wds_discovery_form.ems" var="discoverWdsUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_delete_dialog.ems" var="deleteFixtureDialogUrl" scope="request" />
<spring:url value="/devices/plugloads/plugload_delete_dialog.ems" var="deletePlugloadDialogUrl" scope="request" />
<spring:url value="/users/assignuserstoswitches.ems" var="assignUserToSwitchesDailogUrl" scope="request" />
<spring:url value="/devices/fixtures/assignprofiletofixtures.ems" var="assignProfileToFixturesDailogUrl" scope="request" />
<spring:url value="/devices/plugloads/assignplugloadprofiletoplugloads.ems" var="assignPlugloadProfileToPlugloadsDailogUrl" scope="request" />
<spring:url value="/devices/fixtures/assignareatofixtures.ems" var="assignAreaToFixturesDailogUrl" scope="request" />
<spring:url value="/devices/fixtures/assignfixturetypetofixtures.ems" var="assignFixtureTypeToFixturesDailogUrl" scope="request" />
<spring:url value="/devices/switches/assignareatoswitches.ems" var="assignAreaToSwitchesDailogUrl" scope="request" />
<spring:url value="/devices/wds/assignareatodevices.ems" var="assignAreaToDevicesDailogUrl" scope="request" />
<spring:url value="/devices/switches/settings/dialog.ems" var="switchSettingsDialogUrl" scope="request" />
<spring:url value="/devices/groups/groups_create.ems" var="assignGroupsToFixturesDailogUrl" scope="request" />
<spring:url value="/devices/groups/groups_edit.ems" var="editGroupDailogUrl" scope="request" />
<spring:url value="/devices/groups/fixture_groups_reset.ems" var="resetFixtureGemsGroupsDialogUrl" scope="request" />
<spring:url value="/devices/widget/switch/show.ems" var="switchWidgetDlgUrl" scope="request" />
<spring:url value="/devices/widget/switch/prompt.ems" var="switchPromptUrl" scope="request" />
<spring:url value="/devices/widget/scenetemplate/prompt.ems" var="scenetemplatePromptUrl" scope="request" />
<spring:url value="/devices/widget/scenelightlevel/prompt.ems" var="sceneLightLevelPromptUrl" scope="request" />
<spring:url value="/devices/widget/group/prompt.ems" var="groupPromptUrl" scope="request" />
<spring:url value="/devices/widget/group/show.ems" var="groupWidgetDlgUrl" scope="request" />
<spring:url value="/devices/widget/create/switch.ems" var="createSwitchUrl" scope="request" />
<spring:url value="/devices/widget/create/group.ems" var="createGroupUrl" scope="request" />
<spring:url value="/devices/wds/wdsedit.ems" var="wdsEditDialogUrl" scope="request" />
<spring:url value="/devices/locatordevices/locatorDevice_create.ems" var="createLocatorDeviceUrl" scope="request" />
<spring:url value="/devices/locatordevices/locatorDevice_edit.ems" var="editLocatorDeviceUrl" scope="request" />
<spring:url value="/devices/widget/all/show.ems" var="wigetconfigureUrl" scope="request" />

<spring:url value="/profileTemplateManagement/profile_template_form.ems" var="updateTemplateFormUrl" scope="request" />
<spring:url value="/profile/addeditsetting.ems" var="ProfileAddEditUrl" scope="request" />
<spring:url value="/devices/fixtures/assignprofiletofixturesemployeerole.ems" var="assignProfileToFixturesEmployeeDialogUrl" scope="request" />
<spring:url value="/devices/plugloads/assignplugloadprofiletoplugloadsemployeerole.ems" var="assignPlugloadProfileToPlugloadsEmployeeDialogUrl" scope="request" />
<spring:url value="/profile/fixturesetting.ems" var="fixtureProfileUrl" scope="request" />
<spring:url value="/plugloadProfile/plugloadsetting.ems" var="plugloadProfileUrl" scope="request" />
<spring:url value="/profile/importprofilesetting.ems" var="importProfileSettingUrl" scope="request" />
<spring:url value="/plugloadProfile/importplugloadprofilesetting.ems" var="importPlugloadProfileSettingUrl" scope="request" />
<spring:url value="/devices/fixtures/fixturePowerMapForm.ems" var="fixturePowerMapForm" scope="request" />
<spring:url value="/services/org/wds/startcommission/" var="startCommissionWDSUrlV2" scope="request" />
<spring:url value="/services/org/fixture/op/setAmbientThreshold" var="setAmbientThresholdUrl" scope="request" />
<script type="text/javascript">

function showGateWayForm(gatewayId) {
	var windowHeight = window.screen.availHeight;
	var windowWidth = window.screen.availWidth;
	
	$("#gatewayFormDialog").load("${updatwGatewayUrl}?gatewayId="+gatewayId+"&ts="+new Date().getTime(), function() {
		  $("#gatewayFormDialog").dialog({
				modal:true,
				title: '<spring:message code="gatewayForm.heading.name"/>',
// 				height: windowHeight-300, //default auto
				width: "75%",
				buttons: {
					"Reboot" : function() {						  
						 $.ajax({
							type: 'POST',
							url: "${rebootGatewayUrl}"+gatewayId,
							success: function(data){									
							},
							dataType:"xml",
							contentType: "application/xml; charset=utf-8"
						});						
					},
					"Update": function() {
							if($("#gw_info_ipAddress").val() == ""){
								$("#gateway_message").css("color", "red");
								$("#gateway_message").html('Ip Address feild cannot be empty');
							}else{
								$("#gateway_message").css("color", "black");
								$("#gateway_message").html('<spring:message code="gatewayForm.label.waitingMessage"/>');
							
								$.post(
									"${gatewaySubmitURL}",
									$("#gateway-form").serialize(),
									function(data){
										var response = eval("("+data+")");
										$("#gateway_message").css("display", "block");
										$("#gateway_message").html(response.message);
										if(response.success==1){ //Success
											$("#gateway_message").css("color", "green");
											reloadGatewayFrame();
										} else { //Failure
											$("#gateway_message").css("color", "red");
										}
									}
								);
	            			return false;
							}
					},
					Cancel: function() {
						$("#gatewayFormDialog").dialog("close");
						
					}
				},
				close: function(event, ui) {
					try {
						getFloorPlanObj("floorplan").plotChartRefresh();
						
						
					} catch(e) {
					}
					$("#gatewayFormDialog").html("");
				}
			});
		});
	return false;
}

function reloadGatewayFrame(){
	var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("gatewaysFrame");
	ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src;
}

function reloadLocatorDeviceFrame(){
	var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("othersFrame");
	ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src;
}

function reloadSceneTemplateFrame(){
	var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("scenetemplatesFrame");
	ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src;
}

// Show Gateway Commission Dialog
function showGatewayCommissioningForm(gatewayId) {
	
	
	$("#gatewayCommissioningDialog").load("${commissionGatewayUrl}?gatewayId="+gatewayId+"&ts="+new Date().getTime(), function() {
		  $("#gatewayCommissioningDialog").dialog({
				modal:true,
				title: '<span id="gw-comm-dialog-title-id"><spring:message code="gatewayCommissionForm.heading.name"/></span>',
				width : Math.floor($('body').width() * .98),
				height : Math.floor($('body').height() * .98),
				closeOnEscape: false,
				open: function(event, ui) {
					$(this).parent().children().children('.ui-dialog-titlebar-close').hide();
				},
				close: function(event, ui) {
					try {
						getFloorPlanObj("floorplan").plotChartRefresh();
						
					} catch(e) {
					}
				}
			});
		});
	return false;
}

var SWITCH_ID = "";
function showWdsCommissioningIdentifyWindow(switchId){
	var options = "?ts="+new Date().getTime();
	
	SWITCH_ID = switchId;
	
	$("#wdsCommissioningStartIdentifyDialog").load("${startCommissionWdsUrl}"+options, function() {
		  $("#wdsCommissioningStartIdentifyDialog").dialog({
				title: "Start Identify-Config-Commission-Place",
				width : 400,
				height : 200,
				closeOnEscape: false,
				modal: true
			});
		});
	return false;
}

// Beign Wds Commissioning directly
function startCommissionWdsV2(selectedGateway){
	var DISC_STATUS_SUCCESS = 1; 				//All the nodes are discovered
	var DISC_STATUS_STARTED = 2; 				//Discovery started		
	var DISC_STATUS_INPROGRESS = 3; 			//Discovery is in progress		
	var DISC_ERROR_INPROGRESS = 4; 				//Discovery is already in progress
	var DISC_ERROR_GW_CH_CHANG_DEF = 5; 		//Not able to move the Gateway to default wireless parameters during discovery		
	var DISC_ERROR_TIMED_OUT = 6; 				//Not able to find all the nodes within 3 minute timeout.			
	var DISC_ERROR_GW_CH_CHANGE_CUSTOM = 7; 	//Not able to move the Gateway to custom wireless parameters after discovery		
	var COMM_STATUS_SUCCESS = 8; 				//Commissioning is successful		
	var COMM_STATUS_STARTED = 9; 				//Commissioning started		
	var COMM_STATUS_INPROGRESS = 10; 			//Commissioning is in progress		
	var COMM_STATUS_FAIL = 11; 					//Commissioning failed		
	var COMM_ERROR_INPROGRESS = 12; 			//Commissioning is already in progress		
	var COMM_ERROR_GW_CH_CHANGE_DEF = 13; 		//Not able to move the Gateway to default wireless parameters during commissioning.			
	var COMM_ERROR_GW_CH_CHANGE_CUSTOM = 14; 	//Not able to move the Gateway to custom wireless parameters during commissioning.
	var COMM_ERROR_INACTIVE_TIMED_OUT = 15; 	//Commissioning Timed out due to inactivity
	var COMM_ERROR_INACTIVE_TIMED_OUT_GW_CH_CHANGE_CUSTOM = 16;

	var selectType = 0;
	var floorId = treenodeid; //selected tree node id (floor id)
	var urlOption = "";
	urlOption = "floor/"+floorId+"/gateway/"+selectedGateway;
	$.ajax({
		url: "${startCommissionWDSUrlV2}"+urlOption+"?ts="+new Date().getTime(),
		dataType:"json",
		contentType: "application/json; charset=utf-8",
		success: function(data){
			var status = (1 * data.status);
			if(status == COMM_STATUS_STARTED) {
				showWdsCommissioningForm(selectedGateway);
			} else if(status == DISC_ERROR_INPROGRESS) {
				alert("Discovery is already in progress. Please try later.");
			} else if(status == COMM_ERROR_INPROGRESS) {
				alert("Commissioning is already in progress. Please try later.");
			}
		}
	});
	
}

//Show WDS Commission Dialog
function showWdsCommissioningForm(gatewayId) {
	
	var data = "gatewayId="+gatewayId+"&ts="+new Date().getTime();
	
	$("#wdsCommissioningDialog").load("${commissionWdsUrl}", data, function() {
		  $("#wdsCommissioningDialog").dialog({
				modal: true,
				title: "Commission and Place ERC",
				width : Math.floor($('body').width() * .98),
				height : Math.floor($('body').height() * .96),
				closeOnEscape: false,
				open: function(event, ui){
					$(this).parent().children().children('.ui-dialog-titlebar-close').hide();
					resizeWdsCommissionDialog();
				},
				resizeStop: function(event, ui) {
					resizeWdsCommissionDialog();
				},
				close: function(event, ui) {
					try {
						/*if(SWITCH_ID == undefined || SWITCH_ID == "")
							location.reload();
						*/
						getFloorPlanObj("widget_floorplan").plotChartRefresh();
						SWITCH_ID = "";
					} catch(e) {
					}
				}
			});
		});
	
	return false;
}

function showWDSDiscoveryWindow(){	
	$("#wdsDiscoveryDialog").load("${discoverWdsUrl}"+"?ts="+new Date().getTime(), function() {
		  $("#wdsDiscoveryDialog").dialog({
				title: "Discover ERC",
				width : 480,
				minHeight : 200,
				closeOnEscape: false,
				modal: true,
				dialogClass: 'no-close'
			});
		});
	return false;
}

function showFixtureCommissioningForm(isBulkCommission, fixtureId, gatewayId, type){
	var data = "isBulkCommission="+isBulkCommission+"&fixtureId="+fixtureId+"&gatewayId="+gatewayId+"&type="+type+"&ts="+new Date().getTime();
	
	
	$("#fixtureCommissioningDialog").load("${commissionFixtureUrl}", data, function() {
		  $("#fixtureCommissioningDialog").dialog({
				modal: true,
				title: "Commission and Place Fixtures",
				width : Math.floor($('body').width() * .98),
				height : Math.floor($('body').height() * .96),
				closeOnEscape: false,
				open: function(event, ui){
					$(this).parent().children().children('.ui-dialog-titlebar-close').hide();
					resizeFixtureCommissionDialog();
				},
				resizeStop: function(event, ui) {
					resizeFixtureCommissionDialog();
				},
				close: function(event, ui) {
					try {
						getFloorPlanObj("floorplan").plotChartRefresh();
						
					} catch(e) {
					}
					$("#fixtureCommissioningDialog").html("");
				}
			});
		});
	return false;
}

function showPlacedFixtureCommissioningForm(gatewayId){
	var data = "gatewayId="+gatewayId+"&ts="+new Date().getTime();
	
	
	$("#placedFixtureCommissioningDialog").load("${placedCommissionFixtureUrl}", data, function() {
		  $("#placedFixtureCommissioningDialog").dialog({
				modal: true,
				title: "Commission Placed Fixtures",
				width : Math.floor($('body').width() * .98),
				height : Math.floor($('body').height() * .96),
				closeOnEscape: false,
				open: function(event, ui){
					$(this).parent().children().children('.ui-dialog-titlebar-close').hide();
					resizePlacedFixtureCommissionDialog();
				},
				resizeStop: function(event, ui) {
					resizePlacedFixtureCommissionDialog();
				},
				close: function(event, ui) {
					try {
						getFloorPlanObj("floorplan").plotChartRefresh();
						
					} catch(e) {
					}
					$("#placedFixtureCommissioningDialog").html("");
				}
			});
		});
	return false;
}

function showFixtureCommissioningIdentifyWindow(isBulkCommission, gatewayId, fixtureId){
	var options = "?isBulkCommission="+isBulkCommission+"&gatewayId="+gatewayId+"&fixtureId="+fixtureId+"&ts="+new Date().getTime();
	
	$("#fixtureCommissioningStartIdentifyDialog").load("${startCommissionFixtureUrl}"+options, function() {
		  $("#fixtureCommissioningStartIdentifyDialog").dialog({
				title: "Start Identify-Config-Commission-Place",
				width : 480,
				minHeight : 280,
				closeOnEscape: false,
				modal: true
			});
		});
	return false;
}

function showFixtureDiscoveryWindow(){	
	$("#fixtureDiscoveryDialog").load("${discoverFixtureUrl}"+"?ts="+new Date().getTime(), function() {
		  $("#fixtureDiscoveryDialog").dialog({
				title: "Discover Sensors",
				width : 480,
				minHeight : 200,
				closeOnEscape: false,
				modal: true,
				dialogClass: 'no-close'
			});
		});
	return false;
}

function showCommissionPlacedSensorsWindow(){
	
	$("#placedFixtureCommissioningStartIdentifyDialog").load("${startCommissionPlacedFixtureUrl}", function() {
		  $("#placedFixtureCommissioningStartIdentifyDialog").dialog({
				title: "Start Identify-Config-Commission-Placed-Fixtures",
				width : 400,
				height : 180,
				closeOnEscape: false,
				modal: true
			});
		});
	return false;
	
}

function showPlugloadDiscoveryWindow(){	
	$("#plugloadDiscoveryDialog").load("${discoverPlugloadeUrl}"+"?ts="+new Date().getTime(), function() {
		  $("#plugloadDiscoveryDialog").dialog({
				title: "Discover Plugloads",
				width : 480,
				minHeight : 200,
				closeOnEscape: false,
				modal: true,
				dialogClass: 'no-close'
			});
		});
	return false;
}
function showPlugloadCommissioningIdentifyWindow(isBulkCommission, gatewayId, plugloadId){
	var options = "?isBulkCommission="+isBulkCommission+"&gatewayId="+gatewayId+"&plugloadId="+plugloadId+"&ts="+new Date().getTime();
	
	$("#plugloadCommissioningStartIdentifyDialog").load("${startCommissionPlugloadUrl}"+options, function() {
		  $("#plugloadCommissioningStartIdentifyDialog").dialog({
				title: "Start Commission Place",
				width : 480,
				minHeight : 280,
				closeOnEscape: false,
				modal: true
			});
		});
	return false;
}
function showPlugloadCommissioningForm(isBulkCommission, plugloadId, gatewayId, type){
	var data = "isBulkCommission="+isBulkCommission+"&plugloadId="+plugloadId+"&gatewayId="+gatewayId+"&type="+type+"&ts="+new Date().getTime();
	
	
	$("#plugloadCommissioningDialog").load("${commissionPlugloadUrl}", data, function() {
		  $("#plugloadCommissioningDialog").dialog({
				modal: true,
				title: "Commission and Place Plugload",
				width : Math.floor($('body').width() * .98),
				height : Math.floor($('body').height() * .96),
				closeOnEscape: false,
				open: function(event, ui){
					$(this).parent().children().children('.ui-dialog-titlebar-close').hide();
					resizePlugloadCommissionDialog();
				},
				resizeStop: function(event, ui) {
					resizePlugloadCommissionDialog();
				},
				close: function(event, ui) {
					try {
						getFloorPlanObj("floorplan").plotChartRefresh();
						
					} catch(e) {
					}
					$("#plugloadCommissioningDialog").html("");
				}
			});
		});
	return false;
}
function showWdsEdit(wdsname,model,fromFloorPlan)
{

	var width;
	var height;
	var pageUrl;

	pageUrl = "${wdsEditDialogUrl}";
	width= Math.floor($('body').width() * .45);
	height=310;
	
	
	$("#wdsEditDialog").load(pageUrl+"?wdsId="+wdsname+"&ts="+new Date().getTime(), function() {
		$("#wdsEditDialog").dialog({
			modal:true,
			title: 'ERC Edit',
			width :width,
			height : height,// Math.floor($('body').height() * .30),
			close: function(event, ui) {
				if(fromFloorPlan != undefined && fromFloorPlan == true) {
					getFloorPlanObj("widget_floorplan").plotChartRefresh();
				} else {
					//location.reload();
					triggerRefreshEvent("showWidgetDialog");
				}
			}
		});
	});
	
}

function showFixtureForm(fixtureId) {	
		
	$("#fixtureFormDialog").load("${fixtureDetailsUrl}?fixtureId="+fixtureId+ "&ts="+ new Date().getTime(), function() {
		$("#fixtureFormDialog").dialog({
			modal:true,
			title: '<spring:message code="fixtureForm.heading.name"/>',
			width:  Math.floor($('body').width() * .98),
			height: Math.floor($('body').height() * .94),
			close: function(event, ui) {
				$("#fixtureFormDialog").html("");
				$("#fixtureFormDialog").dialog("destroy");
				try{
					if( document.getElementById("installFrame") != null || document.getElementById("installFrame") != undefined ){
						var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("fixturesFrame");
						if( ifr != null || ifr != undefined ){
							ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
							ifr.src = ifr.src + new Date().getTime();
						}
					}
				}catch(e) {
				}
				//triggerRefreshEvent("showFixtureForm");
			}
		});
	});
	return false;
}

function showPlugloadForm(plugloadId) {	
	
	$("#plugloadFormDialog").load("${plugloadDetailsUrl}?plugloadId="+plugloadId+ "&ts="+ new Date().getTime(), function() {
		$("#plugloadFormDialog").dialog({
			modal:true,
			title: '<spring:message code="plugloadForm.heading.name"/>',
			width:  Math.floor($('body').width() * .98),
			height: Math.floor($('body').height() * .94),
			close: function(event, ui) {
				$("#plugloadFormDialog").html("");
				$("#plugloadFormDialog").dialog("destroy");
				try{
					if( document.getElementById("installFrame") != null || document.getElementById("installFrame") != undefined ){
						var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("plugloadsFrame");
						if( ifr != null || ifr != undefined ){
							ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
							ifr.src = ifr.src + new Date().getTime();
						}
					}
				}catch(e) {
				}
				//triggerRefreshEvent("showFixtureForm");
			}
		});
	});
	return false;
}

var SELECTED_FIXTURES = [];
function showSwitchForm(switchId, selFixtures, xaxis, yaxis) {
	var url = "";
	if(switchId != undefined && switchId != ""){
		url = "${switchEditUrl}?switchId="+switchId+"&ts="+new Date().getTime();
	} else {
		//when come from switches -> devices -> create switch
		if(xaxis == undefined || xaxis == "" || yaxis == undefined || yaxis == ""){
			xaxis="0";
			yaxis="0";
		}
		url = "${switchCreateUrl}"+"?ts="+new Date().getTime()+"&xaxis="+xaxis+"&yaxis="+yaxis;		
	}
	
	if(selFixtures != undefined){
		SELECTED_FIXTURES = eval("("+selFixtures+")");	
	}
	
	$("#switchFormDialog").load(url, function() {
		
		$("#switchFormDialog").dialog({
			modal:true,
			title: '<spring:message code="switchForm.heading.name"/>',
			width:  760, //Math.floor($('body').width() * .60)
			minHeight: 450,
			resizable: false,
			close: function(event, ui) {
				if(SELECTED_FIXTURES != undefined && SELECTED_FIXTURES.length > 0) {
					SELECTED_FIXTURES = []; //Reset global variable 'SELECTED_SWITCHES'
					try{
						getFloorPlanObj("floorplan").plotChartRefresh();
					} catch(e){
					}
				}
			}
		});
	});
	return false;
}

var SELECTED_FIXTURES = [];
function showMotionBitsForm(selFixtures) {
	var url = "${mbScheduleCreateUrl}"+"?ts="+new Date().getTime();		
	var title = "Create Motion Bits Configuration";
	
	if(selFixtures != undefined){
		SELECTED_FIXTURES = eval("("+selFixtures+")");	
	}

	$("#mbScheduleFormDialog").load(url, function() {
		
		$("#mbScheduleFormDialog").dialog({
			modal:true,
			title: title,
			width:  550,
			height: 500,
			resizable: false,
			close: function(event, ui) {
				if(SELECTED_FIXTURES != undefined && SELECTED_FIXTURES.length > 0) {
					SELECTED_FIXTURES = [];
					try{
						getFloorPlanObj("floorplan").plotChartRefresh();
					} catch(e){
					}
				}
			}
		});
	});
	return false;
}


function createSwitchFromFlex() {
	
	$("#switchFormDialog").load("${createSwitchFromFlexUrl}"+"?ts="+new Date().getTime(), function() {
		$("#switchFormDialog").dialog({
			modal:true,
			title: '<spring:message code="switchForm.heading.name"/>',
			width:  760, 
			minHeight: 450,
			resizable: false,
			close: function(event, ui) {
				getFloorPlanObj("floorplan").plotChartRefresh();
			}
		});
	});
	return false;
}

function showDeleteFixtureDialog(){
	$("#deleteFixtureDailog").load("${deleteFixtureDialogUrl}"+"?ts="+new Date().getTime(), function() {
		$("#deleteFixtureDailog").dialog({
			modal:true,
			title: 'Fixture Deletion',
			width:  460, 
			minHeight: 350,
			closeOnEscape: false,
			beforeClose: function(event, ui) {
				//Reload fixture list
				var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("fixturesFrame");
				ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
				ifr.src = ifr.src + new Date().getTime();
			},
			close: function(event, ui) {
				try {
					getFloorPlanObj("floorplan").plotChartRefresh();
				} catch(e) {
				}
			}
		});
	});
}

function showDeletePlugloadDialog(){
	$("#deletePlugloadDialog").load("${deletePlugloadDialogUrl}"+"?ts="+new Date().getTime(), function() {
		$("#deletePlugloadDialog").dialog({
			modal:true,
			title: 'Plugload Deletion',
			width:  460, 
			minHeight: 350,
			closeOnEscape: false,
			beforeClose: function(event, ui) {
				//Reload fixture list
				var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("plugloadsFrame");
				ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
				ifr.src = ifr.src + new Date().getTime();
			},
			close: function(event, ui) {
				try {
					getFloorPlanObj("floorplan").plotChartRefresh();
				} catch(e) {
				}
			}
		});
	});
}

var SELECTED_SWITCHES = [];
function assignUserToSwitches(selSwitches){
	SELECTED_SWITCHES = eval("("+selSwitches+")");
	
	$("#assignUserToSwitchesDailog").load("${assignUserToSwitchesDailogUrl}"+"?switchId=" + SELECTED_SWITCHES[0].id + "&ts="+new Date().getTime(), function() {
		$("#assignUserToSwitchesDailog").dialog({
			modal:true,
			title: 'Assign users to switches',
			width : Math.floor($('body').width() * .60),
			height : Math.floor($('body').height() * .70),
			resizeStop: function(event, ui) {				
				resizeAssignUserToSwitchesDialog();
			},
			close: function(event, ui) {
				SELECTED_SWITCHES = []; //Reset global variable 'SELECTED_SWITCHES'
				getFloorPlanObj("floorplan").plotChartRefresh();
			}
		});
	});
}

var SELECTED_FIXTURES_TO_UPDATE_PROFILE = [];
function assignProfileToFixtures(selFixtures,role){
	if(selFixtures != undefined) {
		SELECTED_FIXTURES_TO_UPDATE_PROFILE = eval("("+selFixtures+")");	
	}
	var width;
	var height;
	var pageUrl;
	if(role=="employee")
	{
		pageUrl = "${assignProfileToFixturesEmployeeDialogUrl}";
		width= Math.floor($('body').width() * .74);
		height= Math.floor($('body').height() * .72);
	}else
	{
		pageUrl = "${assignProfileToFixturesDailogUrl}";
		width= Math.floor($('body').width() * .45);
		height=310;
	}
	
	$("#assignProfileToFixturesDailog").load(pageUrl+"?ts="+new Date().getTime(), function() {
		$("#assignProfileToFixturesDailog").dialog({
			modal:true,
			title: 'Assign profile to fixtures',
			width :width,
			height : height,// Math.floor($('body').height() * .30),
			close: function(event, ui) {
				SELECTED_FIXTURES_TO_UPDATE_PROFILE = []; //Reset global variable 'SELECTED_FIXTURES_TO_UPDATE_PROFILE'
				getFloorPlanObj("floorplan").plotChartRefresh();
			}
		});
	});
}

var SELECTED_PLUGLOADS_TO_UPDATE_PROFILE = [];
function assignProfileToPlugload(selPlugloads,role){
	if(selPlugloads != undefined) {
		SELECTED_PLUGLOADS_TO_UPDATE_PROFILE = eval("("+selPlugloads+")");	
	}
	var width;
	var height;
	var pageUrl;
	//if(role=="employee")
	//{
		//pageUrl = "${assignPlugloadProfileToPlugloadsEmployeeDialogUrl}";
		//width= Math.floor($('body').width() * .74);
		//height= Math.floor($('body').height() * .72);
	//}else
	//{
		pageUrl = "${assignPlugloadProfileToPlugloadsDailogUrl}";
		width= Math.floor($('body').width() * .45);
		height=310;
	//}
	
	$("#assignPlugloadProfileToPlugloadsDailog").load(pageUrl+"?ts="+new Date().getTime(), function() {
		$("#assignPlugloadProfileToPlugloadsDailog").dialog({
			modal:true,
			title: 'Assign plugload profile to Plugloads',
			width :width,
			height : height,// Math.floor($('body').height() * .30),
			close: function(event, ui) {
				SELECTED_PLUGLOADS_TO_UPDATE_PROFILE = []; //Reset global variable 'SELECTED_PLUGLOADS_TO_UPDATE_PROFILE'
				getFloorPlanObj("floorplan").plotChartRefresh();
			}
		});
	});
}

var SELECTED_FIXTURES_TO_ASSIGN_AREA = [];
function assignAreaToFixtures(selFixtures){
	if(selFixtures != undefined) {
		SELECTED_FIXTURES_TO_ASSIGN_AREA = eval("("+selFixtures+")");	
	}
	
	$("#assignAreaToFixturesDailog").load("${assignAreaToFixturesDailogUrl}"+"?ts="+new Date().getTime(), function() {
		$("#assignAreaToFixturesDailog").dialog({
			modal:true,
			title: 'Assign area to fixtures',
			width : Math.floor($('body').width() * .40),
			height : 130,// Math.floor($('body').height() * .30),
			close: function(event, ui) {
				SELECTED_FIXTURES_TO_ASSIGN_AREA = []; //Reset global variable 'SELECTED_FIXTURES_TO_ASSIGN_AREA'
				getFloorPlanObj("floorplan").plotChartRefresh();
			}
		});
	});
}

var SELECTED_FIXTURES_TO_FIXTURE_TYPE = [];
var SELECTED_LOCATOR_DEVICES_TO_FIXTURE_TYPE = [];
function assignFixtureTypeToFixtures(selFixtures,selLocatorDevices){
	if(selFixtures != undefined && selFixtures != "") {
		SELECTED_FIXTURES_TO_FIXTURE_TYPE = eval("("+selFixtures+")");	
	}
	
	if(selLocatorDevices != undefined && selLocatorDevices != "") {
		SELECTED_LOCATOR_DEVICES_TO_FIXTURE_TYPE = eval("("+selLocatorDevices+")");	
	}
	
	$("#assignFixtureTypeToFixturesDailog").load("${assignFixtureTypeToFixturesDailogUrl}"+"?ts="+new Date().getTime(), function() {
		$("#assignFixtureTypeToFixturesDailog").dialog({
			modal:true,
			title: 'Assign fixture type to fixtures',
			width : Math.floor($('body').width() * .40),
			height : 130,// Math.floor($('body').height() * .30),
			close: function(event, ui) {
				SELECTED_FIXTURES_TO_FIXTURE_TYPE = []; //Reset global variable 'SELECTED_FIXTURES_TO_FIXTURE_TYPE'
				SELECTED_LOCATOR_DEVICES_TO_FIXTURE_TYPE = []; //Reset global variable 'SELECTED_LOCATOR_DEVICES_TO_FIXTURE_TYPE'
				getFloorPlanObj("floorplan").plotChartRefresh();
			}
		});
	});
}

var SELECTED_SWITCHES_TO_ASSIGN_AREA = [];
function assignAreaToSwitches(selSwitches){
	if(selSwitches != undefined) {
		SELECTED_SWITCHES_TO_ASSIGN_AREA = eval("("+selSwitches+")");	
	}
	
	$("#assignAreaToSwitchesDailog").load("${assignAreaToSwitchesDailogUrl}"+"?ts="+new Date().getTime(), function() {
		$("#assignAreaToSwitchesDailog").dialog({
			modal:true,
			title: 'Assign area to switches',
			width : Math.floor($('body').width() * .40),
			height : 130,// Math.floor($('body').height() * .30),
			close: function(event, ui) {
				SELECTED_SWITCHES_TO_ASSIGN_AREA = []; //Reset global variable 'SELECTED_FIXTURES_TO_ASSIGN_AREA'
				getFloorPlanObj("floorplan").plotChartRefresh();
			}
		});
	});
}

var SELECTED_WDS_TO_ASSIGN_AREA = [];

function assignAreaToSelectedDevices(selSwitches,selFixtures,selWds){
	SELECTED_SWITCHES_TO_ASSIGN_AREA = [];
	SELECTED_FIXTURES_TO_ASSIGN_AREA = [];
	SELECTED_WDS_TO_ASSIGN_AREA = [];
	if(selSwitches != undefined && selSwitches != "") {
		SELECTED_SWITCHES_TO_ASSIGN_AREA = eval("("+selSwitches+")");
	}
	
	if(selFixtures != undefined && selFixtures != "") {
		SELECTED_FIXTURES_TO_ASSIGN_AREA = eval("("+selFixtures+")");	
	}
	
	if(selWds != undefined && selWds != "") {
		SELECTED_WDS_TO_ASSIGN_AREA = eval("("+selWds+")");	
	}
	
	
	$("#assignAreaToDevicesDailog").load("${assignAreaToDevicesDailogUrl}"+"?ts="+new Date().getTime(), function() {
			$("#assignAreaToDevicesDailog").dialog({
				modal:true,
				title: 'Assign Area to Selected Devices',
				width : Math.floor($('body').width() * .40),
				height : 130,// Math.floor($('body').height() * .30),
				close: function(event, ui) {
					SELECTED_SWITCHES_TO_ASSIGN_AREA = []; //Reset global variable 'SELECTED_FIXTURES_TO_ASSIGN_AREA'
					SELECTED_FIXTURES_TO_ASSIGN_AREA = [];
					SELECTED_WDS_TO_ASSIGN_AREA = [];
					getFloorPlanObj("floorplan").plotChartRefresh();
				}
			});
	});
	
}

function openSwitchSettingsDialog(switchId){
	$("#switchSettingsDialog").load("${switchSettingsDialogUrl}?switchId="+switchId+"&ts="+new Date().getTime(), function() {
		$("#switchSettingsDialog").dialog({
			modal:true,
			title: 'Switch Settings',
			minWidth : 350,
			minHeight : 350,
			close: function(event, ui) {
				getFloorPlanObj("floorplan").plotChartRefresh();
			}
		});
	});
}


var SELECTED_FIXTURES_TO_ASSIGN_GROUPS = [];
var selectedGroupId = undefined;
var selectedGroupName = undefined;
function assignGemsGroupToFixtures(selFixtures, mode, groupId, groupName){
	
	selectedGroupId = groupId;
	selectedGroupName = groupName;
	var title = 'Create/Join Group';
	var url = '${assignGroupsToFixturesDailogUrl}';
	if( mode == 'edit') {
		url = '${editGroupDailogUrl}';
		title = 'Edit Group';
	}
	if(selFixtures != undefined) {
		SELECTED_FIXTURES_TO_ASSIGN_GROUPS = eval("("+selFixtures+")");	
	}
		
	$("#assignGroupsToFixturesDailog").load(url+"?ts="+new Date().getTime(), function() {
		$("#assignGroupsToFixturesDailog").dialog({
			modal:true,
			title: title,
			width : 370, //Math.floor($('body').width() * .30),
			minHeight : 410, //Math.floor($('body').height() * .60),
			close: function(event, ui) {
				if(SELECTED_FIXTURES_TO_ASSIGN_GROUPS != undefined && SELECTED_FIXTURES_TO_ASSIGN_GROUPS.length > 0) {
					SELECTED_FIXTURES_TO_ASSIGN_GROUPS = []; //Reset global variable 'SELECTED_FIXTURES_TO_ASSIGN_GROUPS'
					getFloorPlanObj("floorplan").plotChartRefresh();
				}
				selectedGroupId = undefined;
				selectedGroupName = undefined;
			}
		});
	});

}


var SELECTED_FIXTURES_TO_RESET_GROUPS = [];
function resetFixtureGemsGroups(selFixtures){

	var title = 'Reset Fixture Groups';
	var url = '${resetFixtureGemsGroupsDialogUrl}';
	if(selFixtures != undefined) {
		SELECTED_FIXTURES_TO_RESET_GROUPS = eval("("+selFixtures+")");	
	}	
	$("#resetFixtureGemsGroupsDialog").load(url+"?ts="+new Date().getTime(), function() {
		$("#resetFixtureGemsGroupsDialog").dialog({
			modal:true,
			title: title,
			width : 370, //Math.floor($('body').width() * .30),
			minHeight : 75, //Math.floor($('body').height() * .60),
			close: function(event, ui) {
				if(SELECTED_FIXTURES_TO_RESET_GROUPS != undefined && SELECTED_FIXTURES_TO_RESET_GROUPS.length > 0) {
					SELECTED_FIXTURES_TO_RESET_GROUPS = []; //Reset global variable 'SELECTED_FIXTURES_TO_ASSIGN_GROUPS'
					getFloorPlanObj("floorplan").plotChartRefresh();
				}
			}
		});
	});
}

//This function will be called from EventAndFault_List page on click of View Button
function showEventFaultViewWindow(id){	
	
	$("#viewDialog").load("${eventsViewUrl}",{"id": id}, function() {
		$("#viewDialog").dialog({
			datatype: "local",
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			modal:true,
			title: 'Event Details',
			width: 600,
			height: 480,
			close: function(event, ui) {
				document.getElementById("eventsAndFaultsFrame").contentWindow.loadGridOnViewWindowClose();
			}
		});
	}); 
}

function showSceneTemplatePrompt(scenetemplateId){	
	var url ="";
	var header = "";
	url = "${scenetemplatePromptUrl}?scenetemplateId="+scenetemplateId+"&ts="+new Date().getTime();
	if(scenetemplateId==-1)
	{
		header = 'Create Scene Template';
	}else
	{
		header = 'Edit Scene Template';
	}
	
	$("#sceneTemplatePromptDialog").load(url, function() {
		$("#sceneTemplatePromptDialog").dialog({
			autowidth: false,
			scrollOffset: 0,
			forceFit: true,
			modal:true,
			title: header,
			width:450,
			height:450,
			close: function(event, ui) {
				reloadSceneTemplateFrame();
			}
		});
	}); 
}

function showSceneLightLevelPrompt(sceneTemplateID,mode){
	var header = "";
	if(mode=='new')
	{
		header = 'Add Scene';
	}else
	{
		header = 'Edit Scene';
	}
	$("#sceneLightLevelPromptDialog").load("${sceneLightLevelPromptUrl}?sceneTemplateID="+sceneTemplateID+"&mode="+mode, function() {
		$("#sceneLightLevelPromptDialog").dialog({
			autowidth: false,
			scrollOffset: 0,
			forceFit: true,
			modal:true,
			title: header,
			width:300,
			height:180,
			close: function(event, ui) {				
			}
		});
	}); 
}
var SELECTED_PLUGLOAD_TO_ASSIGN_SWITCH_GROUPS =[];
var SELECTED_FIXTURES_TO_ASSIGN_SWITCH_GROUPS = [];
var SWITCH_GROUP_VERSION =  "";

function showSwitchPrompt(selFixtures, swVersion, bulkConfigure,selplugloads){	
	SELECTED_FIXTURES_TO_ASSIGN_SWITCH_GROUPS = eval("("+selFixtures+")");
	SELECTED_PLUGLOAD_TO_ASSIGN_SWITCH_GROUPS = eval("("+selplugloads+")");
	console.log("SELECTED_PLUGLOAD_TO_ASSIGN_SWITCH_GROUPS " + SELECTED_PLUGLOAD_TO_ASSIGN_SWITCH_GROUPS);
	SWITCH_GROUP_VERSION = swVersion;
	if (!bulkConfigure) {
		bulkConfigure = false;
	}
	
	$("#switchPromptDialog").load("${switchPromptUrl}?bc="+bulkConfigure, function() {
		$("#switchPromptDialog").dialog({
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			modal:true,
			title: 'Create Switch',
			width: 350,
			height: 200,
			close: function(event, ui) {
				
			}
		});
	}); 
}

var switchFormData = "";
<spring:url value="/services/org/switch/updateGroupSyncFlag/" var="updateGroupSyncFlag" scope="request" />
<spring:url value="/devices/widget/switch/editAndApply.ems" var="applySwitchChangesURL" scope="request" />
function showWidgetDialog(switchId,switchName,fixtureVersion) {
	dialogLayout_settings = {
			zIndex:				0		// HANDLE BUG IN CHROME - required if using 'modal' (background mask)
		,	resizeWithWindow:	false	// resizes with the dialog, not the window
		,	spacing_open:		6
		,	spacing_closed:		6
		,	north__size:		'0%' 
		,	north__minSize:		0 
		,	west__size:			'30%' 
		,	west__minSize:		100 
		,	west__maxSize:		300 
		,	south__size:		'auto' 
		,	south__closable:	false 
		,	south__resizable:	false 
		,	south__slidable:	false 
	};
	
	var dialogLayout;
	if(switchId == null || switchId == ""){
		//var switchName = prompt("Enter switch name:", "");
		
		if(switchName != null && switchName != "") {

			// First call a service to create a virtual switch; get the id back and then pass it on to the widget dialog
			$("#widgetDialog").load("${createSwitchUrl}?switchName="+encodeURIComponent(switchName)+"&fixtureVersion="+fixtureVersion, function() {
				$("#widgetDialog").dialog({
					modal:true,
					title: 'Edit Switch Group',
					width:  Math.floor($('body').width() * .98),
					height: Math.floor($('body').height() * .98),
					open: function() {
						if (!dialogLayout) {
							dialogLayout = $("#widgetDialog").layout( dialogLayout_settings );
						}
					},	
					resize:	function() {
						if (dialogLayout) dialogLayout.resizeAll(); 
					},
					close: function(event, ui) {
						if (dialogLayout) {
							dialogLayout.destroy();
						}
						$("#widgetDialog").html("");
						$("#widgetDialog").dialog("destroy");
						//location.reload();
						triggerRefreshEvent("showWidgetDialog");
					}
				});
			});
			return false;
		}
	}
	else {
		switchFormData = "";
		$("#widgetDialog").load("${switchWidgetDlgUrl}?switchId="+ switchId+"&ts="+new Date().getTime(), function() {
			$("#widgetDialog").dialog({
				modal:true,
				title: 'Edit Switch Group',
				width:  Math.floor($('body').width() * .98),
				height: Math.floor($('body').height() * .98),
				open: function() {
					if (!dialogLayout) {
						dialogLayout = $("#widgetDialog").layout( dialogLayout_settings );
					}
				},	
				resize:	function() {
					if (dialogLayout) dialogLayout.resizeAll(); 
				},
				beforeClose: function(event, ui) {
					switchFormData = $("#editSwitchForm").serialize();
				},
				close: function(event, ui) {
					if (dialogLayout) {
						dialogLayout.destroy();
					}
					$("#widgetDialog").html("");
					$("#widgetDialog").dialog("destroy");
					//location.reload();
					triggerRefreshEvent("showWidgetDialog");
					$.ajax({
						url: "${updateGroupSyncFlag}"+switchId+"/"+false+"?ts="+new Date().getTime(),
						dataType:"json",
						contentType: "application/json; charset=utf-8",
						success: function(data){
						}
					});
					
					$.ajax({
		    			type: "POST",
		    			url: "${applySwitchChangesURL}?switchId="+switchId,
		    			datatype: "html",
		    			success: function(msg) {
		    			}
		    		});	
				}
			});
		});
		return false;
	}
	
}

// Show Bulk configure
function showBulkConfigureWidgetDialog(switchId,switchName,fixtureVersion) {
	dialogLayout_settings = {
			zIndex:				0		// HANDLE BUG IN CHROME - required if using 'modal' (background mask)
		,	resizeWithWindow:	false	// resizes with the dialog, not the window
		,	spacing_open:		6
		,	spacing_closed:		6
		,	north__size:		'17%' 
		,	north__minSize:		110 
		,	west__size:			'30%' 
		,	west__minSize:		100 
		,	west__maxSize:		300 
		,	south__size:		'auto' 
		,	south__closable:	false 
		,	south__resizable:	false 
		,	south__slidable:	false 
	};

	var dialogLayout;
	if(switchId == null || switchId == ""){
		//var switchName = prompt("Enter switch name:", "");
		if(switchName != null && switchName != "") {
			// First call a service to create a virtual switch; get the id back and then pass it on to the widget dialog
			var bulkcommission = "false";
			if (fixtureVersion == "2.x") {
				bulkcommission = "true";
			}
			$("#widgetDialog").load("${createSwitchUrl}?switchName="+encodeURIComponent(switchName)+"&fixtureVersion="+fixtureVersion+"&bc="+bulkcommission, function() {
				$("#widgetDialog").dialog({
					modal:true,
					title: 'Edit',
					width:  Math.floor($('body').width() * .98),
					height: Math.floor($('body').height() * .98),
					open: function() {
						if (!dialogLayout) {
							dialogLayout = $("#widgetDialog").layout( dialogLayout_settings );
						}
					},	
					resize:	function() {
						if (dialogLayout) dialogLayout.resizeAll(); 
					},
					close: function(event, ui) {
						if (dialogLayout) {
							dialogLayout.destroy();
						}
						$("#widgetDialog").html("");
						$("#widgetDialog").dialog("destroy");
						//location.reload();
						triggerRefreshEvent("showBulkConfigureWidgetDialog");
					}
				});
			});
		}else {
			// First call a service to create a virtual switch; get the id back and then pass it on to the widget dialog
			$("#widgetDialog").load("${wigetconfigureUrl}?switchId="+ switchId+"&ts="+new Date().getTime(), function() {
				$("#widgetDialog").dialog({
					modal:true,
					title: 'Edit',
					width:  Math.floor($('body').width() * .98),
					height: Math.floor($('body').height() * .98),
					open: function() {
						if (!dialogLayout) {
							dialogLayout = $("#widgetDialog").layout( dialogLayout_settings );
						}
					},	
					resize:	function() {
						if (dialogLayout) dialogLayout.resizeAll(); 
					},
					close: function(event, ui) {
						if (dialogLayout) {
							dialogLayout.destroy();
						}
						$("#widgetDialog").html("");
						$("#widgetDialog").dialog("destroy");
						//location.reload();
						triggerRefreshEvent("showBulkConfigureWidgetDialog");
					}
				});
			});
		}
	}
	return false;
}


var SELECTED_FIXTURES_TO_ASSIGN_MOTION_GROUPS = [];
var SELECTED_PLUGLOADS_TO_ASSIGN_MOTION_GROUPS=[];
var MOTION_GROUP_VERSION =  "";

function showGroupPrompt(selFixtures, grVersion,selplugloads){	

	SELECTED_FIXTURES_TO_ASSIGN_MOTION_GROUPS = eval("("+selFixtures+")");
	SELECTED_PLUGLOADS_TO_ASSIGN_MOTION_GROUPS = eval("("+selplugloads+")");
	MOTION_GROUP_VERSION = grVersion;

	$("#groupPromptDialog").load("${groupPromptUrl}", function() {
		$("#groupPromptDialog").dialog({
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			modal:true,
			title: 'Create Group',
			width: 350,
			height: 200,
			close: function(event, ui) {
			}
		});
	}); 
}

var groupFormData = "";
<spring:url value="/services/org/motiongroup/updateGroupSyncFlag/" var="updateMotionGroupSyncFlag" scope="request" />
<spring:url value="/devices/widget/group/editAndApply.ems" var="applyMotionGroupChangesURL" scope="request" />
function showGroupWidgetDialog(groupId,groupName,fixtureVersion) {
	dialogLayout_settings = {
			zIndex:				0		// HANDLE BUG IN CHROME - required if using 'modal' (background mask)
		,	resizeWithWindow:	false	// resizes with the dialog, not the window
		,	spacing_open:		6
		,	spacing_closed:		6
		,	north__size:		'0%' 
		,	north__minSize:		0 
		,	west__size:			'30%' 
		,	west__minSize:		100 
		,	west__maxSize:		300 
		,	south__size:		'auto' 
		,	south__closable:	false 
		,	south__resizable:	false 
		,	south__slidable:	false 
	};
	
	var dialogLayout;
	if(groupId == null || groupId == ""){
		//var groupName = prompt("Enter Group name:", "");
		
		if(groupName != null && groupName != "") {

			// First call a service to create a virtual switch; get the id back and then pass it on to the widget dialog
			$("#groupWidgetDialog").load("${createGroupUrl}?groupName="+encodeURIComponent(groupName)+"&fixtureVersion="+fixtureVersion, function() {
				$("#groupWidgetDialog").dialog({
					modal:true,
					title: 'Edit Motion Group',
					width:  Math.floor($('body').width() * .98),
					height: Math.floor($('body').height() * .98),
					open: function() {
						if (!dialogLayout) {
							dialogLayout = $("#groupWidgetDialog").layout( dialogLayout_settings );
						}
					},	
					resize:	function() {
						if (dialogLayout) dialogLayout.resizeAll(); 
					},
					close: function(event, ui) {
						if (dialogLayout) {
							dialogLayout.destroy();
						}
						$("#groupWidgetDialog").html("");
						$("#groupWidgetDialog").dialog("destroy");
						//location.reload();
						triggerRefreshEvent("showGroupWidgetDialog");
					}
				});
			});
			return false;
		}
	}
	else {
		groupFormData="";
		$("#groupWidgetDialog").load("${groupWidgetDlgUrl}?groupId="+ groupId, function() {
			$("#groupWidgetDialog").dialog({
				modal:true,
				title: 'Edit Motion Group',
				width:  Math.floor($('body').width() * .98),
				height: Math.floor($('body').height() * .98),
				open: function() {
					if (!dialogLayout) {
						dialogLayout = $("#groupWidgetDialog").layout( dialogLayout_settings );
					}
				},	
				resize:	function() {
					if (dialogLayout) dialogLayout.resizeAll(); 
				},
				beforeClose: function(event, ui) {
					groupFormData = $("#editGroupForm").serialize();
	    		},
				close: function(event, ui) {
					if (dialogLayout) {
						dialogLayout.destroy();
					}
					//location.reload();
					$("#groupWidgetDialog").html("");
					$("#groupWidgetDialog").dialog("destroy");
					triggerRefreshEvent("showGroupWidgetDialog");
					$.ajax({
						url: "${updateMotionGroupSyncFlag}"+groupId+"/"+false+"?ts="+new Date().getTime(),
						dataType:"json",
						contentType: "application/json; charset=utf-8",
						success: function(data){
						}
					});
					
					$.ajax({
		    			type: "POST",
		    			url: "${applyMotionGroupChangesURL}?groupId="+groupId,
		    			datatype: "html",
		    			success: function(msg) {
		    			}
		    		});	
				}
			});
		});
		return false;
	}
	
}

function showLocatorDeviceForm(locatorDeviceId, xaxis, yaxis,page) {
	var url = "";
	if(locatorDeviceId != undefined && locatorDeviceId != ""){
		url = "${editLocatorDeviceUrl}?locatorDeviceId="+locatorDeviceId+"&page="+page+"&ts="+new Date().getTime();
		title = "Edit";
	} else {
		//when come from devices -> --> locator devices --> create
		if(xaxis == undefined || xaxis == "" || yaxis == undefined || yaxis == ""){
			xaxis="10";
			yaxis="10";
		}
		
		url = "${createLocatorDeviceUrl}?xaxis="+xaxis+"&yaxis="+yaxis+"&page="+page+"&ts="+new Date().getTime();
		title = "Add";
	}
	
	
	$("#locatorDeviceFormDialog").load(url, function() {
		
		$("#locatorDeviceFormDialog").dialog({
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			modal:true,
			title: title,
			width: 350,
			height: 200,
			close: function(event, ui) {
				//getFloorPlanObj("floorplan").plotChartRefresh();
				reloadLocatorDeviceFrame();
			}
		});
	});
	return false;
}

// This function will close EventAndFaultView popupwindow
function closeDialog(){
	$("#viewDialog").dialog('close');        	
}
function showProfileTemplateForm(templateId) {
	
	$("#profileTemplateFormDialog").load("${updateTemplateFormUrl}?templateId="+templateId+"?ts="+new Date().getTime(), function() {
		$("#profileTemplateFormDialog").dialog({
			modal:true,
			title: '<spring:message code="profiletemplateForm.heading.name"/>',
			width : Math.floor($('body').width() * .98),
			height : Math.floor($('body').height() * .98),
			closeOnEscape: false,
			resizable: false,
			close: function(event, ui) {
			}
		});
	});
	return false;
}

function showProfileDetailsForm(groupId,type,defaultProfile,templateId) {	
	var heading = "";
	var url="";
	if(type=="new" && defaultProfile=='false')
	{
		heading = '<spring:message code="profileForm.heading.newProfile"/>';
		url = "${ProfileAddEditUrl}";
		if(oldProfileName=="")
		{
			url +="?groupId="+groupId+ "&templateId="+templateId+"&type="+ type + "&ts="+ new Date().getTime();
		}else
		{
			url +="?groupId="+groupId+ "&templateId="+templateId+"&type="+ type + "&oldProfileName="+ oldProfileName + "&ts="+ new Date().getTime();
		}
	}
	else if(type=="edit" && defaultProfile=='false')
	{
		heading = '<spring:message code="profileForm.heading.editProfile"/>';
		url = "${ProfileAddEditUrl}";
		url +="?groupId="+groupId+ "&templateId="+templateId+"&type="+ type + "&ts="+ new Date().getTime();
	}else
	{
		heading = '<spring:message code="profileForm.heading.name"/>';
		url = "${fixtureProfileUrl}";
		url +="?fixtureId="+1+"&groupId="+groupId+ "&ts="+ new Date().getTime();
		
	}
	$("#profileFormDialog").load(url, function() {
		$("#profileFormDialog").dialog({
			modal:true,
			title: heading,	
			width:  Math.floor($('body').width() * .98),
			height: Math.floor($('body').height() * .94),
			closeOnEscape: false,
			resizable: false,
			close: function(event, ui) {
				refreshProfileTree();
				$("#profileFormDialog").html("");
				$("#profileFormDialog").dialog("destroy");
				triggerRefreshEvent("showProfileDetailsForm");
			}
		});
	});
	oldProfileName ="";
	return false;
}

var oldPlugloadProfileName="";

function showPlugloadProfileDetailsForm(plugloadGroupId,type,defaultProfile,plugloadTemplateId) {	
	var heading = "";
	var url="";
	if(type=="new" && defaultProfile=='false')
	{
		heading = '<spring:message code="profileForm.heading.newProfile"/>';
		url = "${plugloadProfileAddEditUrl}";
		if(oldPlugloadProfileName=="")
		{
			url +="?groupId="+plugloadGroupId+ "&templateId="+plugloadTemplateId+"&type="+ type + "&ts="+ new Date().getTime();
		}else
		{
			url +="?groupId="+plugloadGroupId+ "&templateId="+plugloadTemplateId+"&type="+ type + "&oldProfileName="+ oldPlugloadProfileName + "&ts="+ new Date().getTime();
		}
	}
	else if(type=="edit" && defaultProfile=='false')
	{
		heading = '<spring:message code="profileForm.heading.editProfile"/>';
		url = "${plugloadProfileAddEditUrl}";
		url +="?groupId="+plugloadGroupId+ "&templateId="+plugloadTemplateId+"&type="+ type + "&ts="+ new Date().getTime();
	}else
	{
		heading = '<spring:message code="plugloadProfileForm.heading.name"/>';
		url = "${plugloadProfileUrl}";
		url +="?plugloadId="+1+"&groupId="+plugloadGroupId+ "&ts="+ new Date().getTime();
		
	}

	$("#plugloadProfileFormDialog").load(url, function() {
		$("#plugloadProfileFormDialog").dialog({
			modal:true,
			title: heading,	
			width:  Math.floor($('body').width() * .98),
			height: Math.floor($('body').height() * .94),
			closeOnEscape: false,
			resizable: false,
			close: function(event, ui) {
				//refreshPlugloadProfileTree();
				//$("#plugloadProfileFormDialog").html("");
				//$("#plugloadProfileFormDialog").dialog("destroy");
				//triggerRefreshEvent("showPlugloadProfileDetailsForm");
				location.reload();
			}
		});
	});
	oldPlugloadProfileName ="";
	return false;
}

function reload_plugload_profile_dialog(plugloadGroupId,plugloadTemplateId,plugloadProfileName) 
{ 
	oldPlugloadProfileName = plugloadProfileName;
	$('#plugloadProfileFormDialog').dialog('destroy');
	showPlugloadProfileDetailsForm(plugloadGroupId,'new','false',plugloadTemplateId);
}

function refreshProfileTree() {
		$.ajax({
		        type: "GET",
		        cache: false,
		        async: false,
		        url: '<spring:url value="/facilities/profiletree.ems"/>',
		        dataType: "html",
		        success: function(msg) {
		                removeclick();
		                $('#profileTreeViewDiv', window.parent.document).html($("#profileTreeViewDiv", $(msg)).html());
		                loadProfileTree();
		                profilenodeclick();
		        }
		});
}

function refreshPlugloadProfileTree() {
	$.ajax({
	        type: "GET",
	        cache: false,
	        async: false,
	        url: '<spring:url value="/facilities/plugloadprofiletree.ems"/>',
	        dataType: "html",
	        success: function(msg) {
					//removePlugloadProfileclick();
					removeclick();
	                $('#plugloadProfileTreeViewDiv', window.parent.document).html($("#plugloadProfileTreeViewDiv", $(msg)).html());
	                loadPlugLoadProfileTree();
	                plugloadprofilenodeclick();
	        }
	});
}

var oldProfileName="";
function reload_dialog(groupId,templateId,profileName) 
{ 
	oldProfileName = profileName;
	$('#profileFormDialog').dialog('destroy'); 
    showProfileDetailsForm(groupId,'new','false',templateId);
} 
function showImportProfileForm() {	
	heading = '<spring:message code="profileForm.heading.newProfile"/>';
	url = "${importProfileSettingUrl}";
	url +="?groupId="+-1+"&templateId="+profilenodeid+"&ts="+ new Date().getTime();
	$("#profileFormDialog").load(url, function() {
		$("#profileFormDialog").dialog({
			modal:true,
			title: heading,	
			width:  Math.floor($('body').width() * .98),
			height: Math.floor($('body').height() * .94),
			closeOnEscape: false,
			resizable: false,
			close: function(event, ui) {
				refreshProfileTree();
				$("#profileFormDialog").html("");
				$("#profileFormDialog").dialog("destroy");
				triggerRefreshEvent("showProfileDetailsForm");
			}
		});
	});
	return false;
}

function showImportPlugloadProfileForm() {	
	heading = '<spring:message code="plugloadprofileForm.heading.newPlugloadProfile"/>';
	url = "${importPlugloadProfileSettingUrl}";
	url +="?groupId="+-1+"&templateId="+plugloadProfilenodeid+"&ts="+ new Date().getTime();
	$("#plugloadProfileFormDialog").load(url, function() {
		$("#plugloadProfileFormDialog").dialog({
			modal:true,
			title: heading,	
			width:  Math.floor($('body').width() * .98),
			height: Math.floor($('body').height() * .94),
			closeOnEscape: false,
			resizable: false,
			close: function(event, ui) {
				//refreshProfileTree();
				$("#plugloadProfileFormDialog").html("");
				$("#plugloadProfileFormDialog").dialog("destroy");
				//triggerRefreshEvent("showPlugloadProfileDetailsForm");
				location.reload();
			}
		});
	});
	return false;
}

function showFixtureCurveForm(selFixtures)
{
	if(selFixtures != undefined){
		SELECTED_FIXTURES = eval("("+selFixtures+")");	
	
		var fixtureId=0;
		var cnt = 0;
		if(SELECTED_FIXTURES != undefined && SELECTED_FIXTURES.length > 0) {
			
			$.each(SELECTED_FIXTURES, function(i, fixtureJson) {
				if(cnt==0)
				{
					fixtureId = fixtureJson.id;
				}
				cnt++;
			});
		}
	}
	var options = "?fixtureId="+fixtureId+"&fixtCnt="+ cnt +"&ts="+new Date().getTime();

	$("#fixturePowermapFormDialog").load("${fixturePowerMapForm}"+options).dialog({
        title : "Power Usage",
        width :  450,
        minHeight : 360,
        modal : true,
        close: function(event, ui) {
        	SELECTED_FIXTURES = [];
        	getFloorPlanObj("floorplan").plotChartRefresh();
		}
    });
}
function onImportProfileHandler()
{
	$("#uploadProfileDialog").load("${uploadProfileDialogURL}?&ts="+new Date().getTime()).dialog({
        title : "Import profile",
        width :  450,
        minHeight : 130,
        modal : true
    });
}

function onImportPlugloadProfileHandler()
{
	$("#uploadPlugloadProfileDialog").load("${uploadPlugloadProfileDialogURL}?&ts="+new Date().getTime()).dialog({
        title : "Import plugload profile",
        width :  450,
        minHeight : 130,
        modal : true
    });
}

</script>

<div id="innercenter" class="ui-layout-center">
	<ul>		
		<li><a id="ec" href="#tab_ec" onclick="loadEnergyConsumption();"><span>Energy Consumption</span></a></li>
				 
        <security:authorize access="hasAnyRole('Admin','Employee','TenantAdmin','FacilitiesAdmin')">
				<li id="lifp"><a id="fp" href="#tab_fp" onclick="loadFloorPlan();"><span>Floor Plan</span></a></li>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin')">
			<li id="liin"><a id="in" href="#install" onclick="loadDeviceInstall();"><span>Devices</span></a></li>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin','TenantAdmin')">
			<li><a id="os" href="#settings" onclick="loadSettings();"><span id="settingstab">Settings</span></a></li>
			<li id="lips" ><a id="ps" href="#profile" onclick="loadProfile();"><span id="profileName">Settings</span></a></li>
			<li id="litmplt" ><a id="tmplt" href="#template" onclick="loadProfileTemplates();"><span id="templateName">Settings</span></a></li>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin', 'TenantAdmin')">
			<li id="lief" ><a id="ef" href="#events_and_faults" onclick="loadEvents();"><span id="eventName">Events</span></a></li>
		</security:authorize>
	</ul>
	<div class="ui-layout-content ui-widget-content ui-corner-bottom" style="border-left: 0; border-top: 0; padding: 0px;">
		<div id="tab_ec" class="pnl_rht flasharea"></div>
		
		<security:authorize access="hasAnyRole('Admin','Employee','TenantAdmin','FacilitiesAdmin')">
			<div id="tab_fp" class="pnl_rht"></div>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin')">
			<div id="install" class="pnl_rht"><iframe frameborder="0" id="installFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>

		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin','TenantAdmin')">
			<div id="settings" class="pnl_rht"><iframe frameborder="0" id="settingFrame" style="width: 100%; height: 100%;"></iframe></div>
			<div id="profile" class="pnl_rht"><iframe frameborder="0" id="profileFrame" style="width: 100%; height: 100%;"></iframe></div>
			<div id="template" class="pnl_rht"><iframe frameborder="0" id="templateFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin', 'TenantAdmin')">
			<div id="events_and_faults" class="pnl_rht"><iframe frameborder="0" id="eventsAndFaultsFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>
	</div>		
</div>

<div id="plugloadTemplateMainDiv" class="ui-layout-center" style="display:none">
	<ul>
	
		<li id="liecplugload"><a id="ec_plugload" href="#tab_ec_plugload" onclick="loadPlugloadEnergyConsumption();"><span>Energy Consumption</span></a></li>
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin')">
			<li id="liplugloadtemplatedevices"><a id="plugloadtemplatedevices" href="#plugloadtemplatedevicesdiv" onclick="loadPlugloadTemplateDevices();"><span>Devices</span></a></li>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin','TenantAdmin')">
			<li id="liplugloadtemplateps" ><a id="plugloadtemplateps" href="#plugloadtemplateprofilesettings" onclick="loadPlugloadTemplateSettings();"><span>Settings</span></a></li>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin', 'TenantAdmin')">
			<li id="liefplugload" ><a id="efplugload" href="#events_and_faults_plugload" onclick="loadPlugloadEvents();"><span>Events</span></a></li>
		</security:authorize>
		
	</ul>
	<div class="ui-layout-content ui-widget-content ui-corner-bottom" id="plugloadTemplateSubDiv" style="border-left: 0; border-top: 0; padding: 0px;">
	
		<div id="tab_ec_plugload" class="pnl_rht flasharea"></div>
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin')">
			<div id="plugloadtemplatedevicesdiv" class="pnl_rht"><iframe frameborder="0" id="plugloadtemplatedevicesFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>
	
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin','TenantAdmin')">
			<div id="plugloadtemplateprofilesettings" class="pnl_rht"><iframe frameborder="0" id="plugloadtemplateSettingsFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin', 'TenantAdmin')">
			<div id="events_and_faults_plugload" class="pnl_rht"><iframe frameborder="0" id="eventsAndFaultsPlugloadFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>
		
	</div>	
</div>

<div id="switchFormDialog"></div>
<div id="mbScheduleFormDialog"></div>
<div id="fixturePowermapFormDialog"></div>
<div id="fixtureFormDialog"></div>
<div id="plugloadFormDialog"></div>
<div id="gatewayFormDialog"></div>
<div id="gatewayCommissioningDialog"></div>
<div id="fixtureCommissioningDialog"></div>
<div id="placedFixtureCommissioningDialog"></div>
<div id="wdsCommissioningDialog"></div>
<div id="wdsCommissioningStartIdentifyDialog"></div>
<div id="fixtureCommissioningStartIdentifyDialog"></div>
<div id="placedFixtureCommissioningStartIdentifyDialog"></div>
<div id="fixtureDiscoveryDialog"></div>
<div id="plugloadDiscoveryDialog"></div>
<div id="plugloadCommissioningStartIdentifyDialog"></div>
<div id="plugloadCommissioningDialog"></div>
<div id="wdsDiscoveryDialog"></div>
<div id="deleteFixtureDailog"></div>
<div id="deletePlugloadDialog"></div>
<div id="assignUserToSwitchesDailog"></div>
<div id="assignProfileToFixturesDailog"></div>
<div id="assignPlugloadProfileToPlugloadsDailog"></div>
<div id="assignAreaToFixturesDailog"></div>
<div id="assignFixtureTypeToFixturesDailog"></div>
<div id="assignAreaToSwitchesDailog"></div>
<div id="assignAreaToDevicesDailog"></div>
<div id="switchSettingsDialog"></div>
<div id="assignGroupsToFixturesDailog"></div>
<div id="resetFixtureGemsGroupsDialog"></div>
<div id="viewDialog"></div>
<div id="profileTemplateFormDialog"></div>
<div id="profileFormDialog"></div>
<div id="plugloadProfileFormDialog"></div>
<div id="widgetDialog"></div>
<div id="groupWidgetDialog"></div>
<div id="wdsEditDialog"></div>
<div id="switchPromptDialog"></div>
<div id="sceneTemplatePromptDialog"></div>
<div id="sceneLightLevelPromptDialog"></div>
<div id="groupPromptDialog"></div>
<div id="locatorDeviceFormDialog"></div>
<div id="uploadProfileDialog"></div>
<div id="uploadPlugloadProfileDialog"></div>
<div id="ambientThreshloldDialog" title="Set Sensor(s) Ambient Threshold Value">
	<form id="ambient-form" onsubmit="return false;" style="padding:0.5em;">
		<p>You are about to set the daylight harvesting target light level on the selected sensor(s).
		Set the value(s) manually or select sensor's current ambient value.</p>
		<br style="clear: both">
		<div class="upperdiv">
			<div class="fieldWrapper">
				<div class="fieldLabel">Selected Sensors:</div>
				<div class="fieldValue" id="selecetedSensors"></div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel">Select Action:</div>
				<div class="fieldValue">
					<input type="radio" name="sensoract" id="act_manual" checked="checked">Manual
					<input type="radio" name="sensoract" id="act_auto" style="margin-left: 1em;">Auto</div>
			</div>
			<div class="fieldWrapper" id="thresholdTxt">
				<div class="fieldLabel">Threshold Value:</div>
				<div class="fieldValue"><input type="text" name="thresholdVal" id="thresholdVal" class="text"></div>
			</div>
		</div>
		<br style="clear: both">
		<div style="text-align:center;">
			<button id="submitButton">Submit</button>
			<button id="cancelButton">Cancel</button>
		</div>	
	</form>	
</div>
<script>
var selectedFixtureIds;
function showAmbientThresholdForm(selFixtures,selFixtureNames) {
	if(selFixtures){
		selectedFixtureIds = selFixtures;
		$("#selecetedSensors").empty();
		$("#selecetedSensors").append(selFixtureNames);
		
		$("#act_manual").attr("checked",true);
		$("#thresholdTxt").show();
		$("#thresholdVal").val("");
		
		$("#ambientThreshloldDialog").dialog({
			modal:true,
			width:  500,
			height: 300,
		});
	}
	return false;
}
$("input[name='sensoract']").change(function() {
	var selAct = $("input[name='sensoract']:checked").attr("id");
	if(selAct=="act_auto"){
		$("#thresholdVal").val("");
		$("#thresholdTxt").hide();
	}else{
		$("#thresholdTxt").show();
	}	
});
$("#submitButton").click(function(){
	if(!selectedFixtureIds){
		return;
	}
	var val = parseInt($("#thresholdVal").val());
	var isAuto = false;
	var selAct = $("input[name='sensoract']:checked").attr("id");
	if(selAct=="act_auto"){
		isAuto = true;
		val=0;
	}
	if(!isAuto&&((isNaN(val)||val<0))){		
		alert("Please enter valid numeric value.");
		return;
	}	
	
	$.ajax({
		url:"${setAmbientThresholdUrl}/"+isAuto+"/"+val,
		contentType: "application/xml; charset=utf-8",
		type:"POST",
		dataType:"json",
		data:selectedFixtureIds,		
		success:function(response,status,jqhr){
			alert("The request for setting Daylight Target Value is submitted.");
			$("#ambientThreshloldDialog").dialog("close");
		},
		error:function(data){
			alert("Error: "+data);
		}
	});	
});
$("#cancelButton").click(function(){
	$("#ambientThreshloldDialog").dialog("close");
});
</script>
