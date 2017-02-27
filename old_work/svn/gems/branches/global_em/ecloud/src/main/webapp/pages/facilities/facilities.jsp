<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="uem" uri="/WEB-INF/tlds/ecloud.tld"%>
<spring:url value="/facilities/organization/setting.ems" var="loadSetting"/>
<spring:url value="/modules/FloorplanModule.swf" var="floorplanModule"></spring:url>
<spring:url value="/modules/ECModule.swf" var="energysummarymodule"></spring:url>
<spring:url value="/profile/setting.ems" var="loadProfile"/>
<spring:url value="/profile/list.ems" var="loadProfileManagement"/>
<spring:url value="/profile/profilesfixturessettings.ems" var="loadProfilesFixturesSettings"/>
<spring:url value="/profile/addeditsetting.ems" var="ProfileAddEditUrl" scope="request" />
<spring:url value="/profile/fixturesetting.ems" var="fixtureProfileUrl" scope="request" />
<spring:url value="/devices/fixtures/assignprofiletofixtures.ems" var="assignProfileToFixturesDailogUrl" scope="request" />
<spring:url value="/devices/gateways/gateway_form.ems" var="updateGatewayUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_details.ems" var="fixtureDetailsUrl" scope="request" />
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<style type="text/css">
	.no-close .ui-dialog-titlebar-close {display: none }
</style>

<script type="text/javascript">
   	
	$(document).ready(function() {
    
		var innerLayout;
		innerLayout = $('div.pane-center').layout( layoutSettings_Inner );
		
		//create tabs
		$("#innercenter").tabs({
			cache: true
		});
		tabselected = 'settings';
		<c:if test="${enableSensorProfile == 'true'}">
		profilenodeclick();
		</c:if>
		showSettings();
		nodeclick();
		<c:if test="${enableSensorProfile == 'true'}">
		setAllowedTab();
		</c:if>
		var accordianSelect = $.cookie('uem_accordian_select');
		<c:if test="${enableSensorProfile == 'true'}">
		if(accordianSelect=="facility")
		{
			$("#lips").hide(); //hide profile tab for first time.
			$("#litmplt").hide(); 
		}
		</c:if>
		
		var resizeTimer;
		$(window).resize(function() {
		    clearTimeout(resizeTimer);
		    resizeTimer = setTimeout(resizeEventFired, 100);
		});
		
	});


var tabselected;

function resizeEventFired() {
	try {
		getFloorPlanObj("floorplan").resizeEventFired();
	} catch(e) {
	}
};

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
function nodeclick() {
	$('#facilityTreeViewDiv').treenodeclick(function(){						
		showSettings();					
	});
}
//**** Keep functions global or refresh tree functionality might break. *********//
var getFloorPlanObj = function(objectName) {			
	if ($.browser.mozilla) {
		return document[objectName]; 
	}
	return document.getElementById(objectName);
};

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
var showSettings =function(){	
	removeWheelEvent();
	tabselected = $.cookie('uem_facilities_tab_selected');
	var accordianSelect = $.cookie('uem_accordian_select');
	if(accordianSelect==null)
	{
		$.cookie('uem_accordian_select', 'facility',  {path: '/' });
	}
	
	<c:if test="${enableSensorProfile == 'true'}">
	if(accordianSelect=='profile')
	{
		accTabSelected='pf';
		$("#accordionfacility").accordion("activate", 0);
		showNodeSpecificTabs();
		return;
	}
	</c:if>
	
	$('#os').show();

	
	if (tabselected == 'install') {
		if (treenodetype == 'floor')
			$("#lifp").css('display','block');
		else
			$("#lifp").css('display','none');

		$('#in').click();
	}
	else if (tabselected == 'settings') {
		if (treenodetype == 'floor')
			$("#lifp").css('display','block');
		else
			$("#lifp").css('display','none');

		$('#os').click();
	}
	else if (tabselected == 'energy consumption') {
		if (treenodetype == 'floor')
			$("#lifp").css('display','block');
		else
			$("#lifp").css('display','none');

		$("#ec").click();		
	}
	else{
		if (treenodetype == 'floor') {
			$("#lifp").css('display','block');
			$("#fp").click(); //to show floor plan tab as selected							
		}
		else {
			$("#lifp").css('display','none');
			$("#ec").click();								
		}
	}
}
var flash_ec = function(nodetype, nodeid) {
	
	var buildNumber = "";
	
	var versionString = "<uem:showAppVersion />";
	
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
	
	var tabEC =document.getElementById("tab_ec");
	tabEC.innerHTML = EC_data; 

	// quick fix for the duplicate flash object
	$('div.alt').remove(); 
}

var loadEC = function() {
	var tempnodetype;
	var tempnodid;

	tempnodetype = treenodetype;
	tempnodid = treenodeid;
	try{
		getFloorPlanObj("energysummary").updateEnergyConsumption(tempnodetype, tempnodid, "day");	
	}
	catch (ex){
		flash_ec(tempnodetype, tempnodid);
	}
}
var flash_fp = function(nodetype, nodeid) {		
	
	var buildNumber = "";
	
	var versionString = "<uem:showAppVersion />";
	
	var indexNumber = versionString.lastIndexOf('.', (versionString.length)-1);
	
	if(indexNumber != -1 ){
		buildNumber = versionString.slice(indexNumber+1);
	}else{
		buildNumber = Math.floor(Math.random()*10000001);// For Development Version
	}
	
	var plotchartmoduleString = "${floorplanModule}"+"?buildNumber="+buildNumber;
	
	var FP_data = "";
	if ($.browser.msie) {
		FP_data = "<object id='floorplan' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='100%'>";
		FP_data +=  "<param name='src' value='"+plotchartmoduleString+"'/>";
		FP_data +=  "<param name='padding' value='0px'/>";
		FP_data +=  "<param name='wmode' value='opaque'/>";
		FP_data +=  "<param name='allowFullScreen' value='true'/>";
		FP_data +=  "<param name='flashVars' value='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=FLOORPLAN'/>";
		FP_data +=  "<embed id='floorplan' name='floorplan' src='"+plotchartmoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
		FP_data +=  " height='100%'";
		FP_data +=  " width='100%'";
		FP_data +=  " padding='0px'";
		FP_data +=  " wmode='opaque'";
		FP_data +=  " allowFullScreen='true'";
		FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=FLOORPLAN'/>";
		FP_data +=  "</object>";
	} else {
		FP_data = "<embed id='floorplan' name='floorplan' src='"+plotchartmoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
		FP_data +=  " height='100%'";
		FP_data +=  " width='100%'";
		FP_data +=  " wmode='opaque'";
		FP_data +=  " padding='0px'";
		FP_data +=  " allowFullScreen='true'";
		FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=FLOORPLAN'/>";
	}
	
	var tabFP =document.getElementById("tab_fp");
	tabFP.innerHTML = FP_data; 
	// quick fix for the duplicate flash object
	$('div.alt').remove(); 
}

//call click event for left side profile-tree view's selected node.
function profilenodeclick() {
	tabselected = $.cookie('uem_profile_tab_selected');
	$('#profileTreeViewDiv').profiletreenodeclick(function(){
		if(profilenodetype=='profilegroup')
		{
			if(tabselected=='template')
			{
				tabselected='profile';
				$.cookie('uem_profile_tab_selected', 'profile',  {path: '/' });
			}
			showNodeSpecificTabs();
		}else
		{
			$.cookie('uem_profile_tab_selected', 'template',  {path: '/' });
			showNodeSpecificTabs();
		}
	});
}

function showNodeSpecificTabs()
{
	tabselected = $.cookie('uem_profile_tab_selected');
	//the 'accTabSelected' variable is global and defined in facility_tree.jsp
			if(profilenodetype=='profiletemplate' || profilenodetype=='campus' || profilenodetype=='building' || profilenodetype=='globaldefault')
			{
				$("#litmplt").show(); 	
				$("#lifp").hide();
				$("#liin").hide();
		 		$("#lips").hide(); 
		 		$("#install").hide();
	 			$('#ec').hide();
// 	 			$("#settingstab").show();
// 	 			$("#settingstab").html("Devices");
	 			$("#tmplt").click();
	 			$('#os').hide();
			}else
			{
				$("#litmplt").hide();
				$("#lifp").hide();
				$("#liin").hide();
		 		$("#lips").show(); 
		 		$("#install").hide();
// 	 			$("#settingstab").show();
// 	 			$("#settingstab").html("Devices");
	 			$('#ec').hide();
	 			$('#os').hide();
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
			}
}

//fuction to show allowed tabs as per accordion tab selected
function setAllowedTab() {
	$('#accordionfacility h2').accordiontabclick(function(){
		if(accTabSelected=='pf'){
			$.cookie('uem_accordian_select', 'profile',  {path: '/' });
			showNodeSpecificTabs();
		}else{
			$.cookie('uem_accordian_select', 'facility',  {path: '/' });
			$('#ec').show();
 			$("#ec").click();
			$("#litmplt").hide();
			$("#liin").show();
			$("#lips").hide();
			$("#install").show();
 			$("#settingstab").html("Settings");
 			showSettings();
		}	
	});
}


function loadSettings() {
	removeWheelEvent();
	tempnodetype = treenodetype;
	tempnodeid = treenodeid;
	tabselected = 'settings';
	$.cookie('uem_facilities_tab_selected', 'settings',  {path: '/' });
	var ifr;
    ifr = document.getElementById("settingFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
     //Open different fixtures list for profiles and facilities
    if(accTabSelected=='pf'){
    	ifr.src="${loadProfilesFixturesSettings}?ts="+new Date().getTime();
    }
    else{
    	ifr.src="${loadSetting}?facilityId="+tempnodeid+"&ts="+new Date().getTime();
    }
    return false;
}

function loadDeviceInstall() {
	removeWheelEvent();
	tabselected = 'install';
	$.cookie('uem_facilities_tab_selected', 'install',  {path: '/' });
    var ifr;
    //ifr = document.getElementById("installFrame");
    //ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    //ifr.src="${installDevices}?ts="+new Date().getTime();
    return false;
}

function loadProfile() {
	removeWheelEvent();
	tabselected = 'profile';
    var ifr;
    $.cookie('uem_profile_tab_selected', 'profile',  {path: '/' });
    ifr = document.getElementById("profileFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src="${loadProfile}?ts="+new Date().getTime();
    return false;
}

function loadProfileTemplates()
{
	removeWheelEvent();
	tabselected = 'template';
    var ifr;
    $.cookie('uem_profile_tab_selected', 'template',  {path: '/' });
    ifr = document.getElementById("templateFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src="${loadProfileManagement}?templateId="+profilenodeid+"&parentNodeId="+parentnodeid+"&ts="+new Date().getTime();
    return false;
}

function loadEnergyConsumption(){
	removeWheelEvent();
	tabselected = 'energy consumption';
	$.cookie('uem_facilities_tab_selected', 'energy consumption',  {path: '/' });
	loadEC();
}

function loadFloorPlan(){
	tabselected = 'floor plan';
	$.cookie('uem_facilities_tab_selected', 'floor plan',  {path: '/' });
	loadFP();
}
function removeWheelEvent() {
	if(window.addEventListener) {
        var eventType = ($.browser.mozilla) ? "DOMMouseScroll" : "mousewheel";            
        window.removeEventListener(eventType, handleWheel, false);
    }
}

function handleWheel(event) {
	//var app = document.getElementById("YOUR_APPLICATION");
    var edelta = ($.browser.mozilla) ? -event.detail : event.wheelDelta/40;                                   
    var o = {x: event.screenX, y: event.screenY, 
        delta: edelta,
        ctrlKey: event.ctrlKey, altKey: event.altKey, 
        shiftKey: event.shiftKey}
	if (getFloorPlanObj("floorplan") != null)
    	getFloorPlanObj("floorplan").handleWheel(o);
}
//PROFILE START

function showProfileDetailsForm(groupId,type,defaultProfile,templateId) {	
	var heading = "";
	var url="";
    var customerId = "${customerId}";
	if(type=="new" && defaultProfile=='false')
	{
		heading = '<spring:message code="profileForm.heading.newProfile"/>';
		url = "${ProfileAddEditUrl}";
		if(oldProfileName=="")
		{
			url +="?groupId="+groupId+ "&templateId="+templateId+"&type="+ type + "&customerId="+customerId+"&ts="+ new Date().getTime();
		}else
		{
			url +="?groupId="+groupId+ "&templateId="+templateId+"&type="+ type + "&customerId="+customerId+"&oldProfileName="+ oldProfileName + "&ts="+ new Date().getTime();
		}
	}
	else if(type=="edit" && defaultProfile=='false')
	{
		heading = '<spring:message code="profileForm.heading.editProfile"/>';
		url = "${ProfileAddEditUrl}";
		url +="?groupId="+groupId+ "&templateId="+templateId+"&type="+ type + "&customerId="+customerId+"&ts="+ new Date().getTime();
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
				//loadProfileTemplates();
				location.reload();
			}
		});
	});
	oldProfileName ="";
	return false;
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
var oldProfileName="";
function reload_dialog(groupId,templateId,profileName) 
{ 
	oldProfileName = profileName;
	$('#profileFormDialog').dialog('destroy'); 
    showProfileDetailsForm(groupId,'new','false',templateId);
} 

//END PROFILE

function showGateWayForm(gatewayId,pid) {
	var windowHeight = window.screen.availHeight;
	var windowWidth = window.screen.availWidth;
	
	$("#gatewayFormDialog").load("${updateGatewayUrl}?gatewayId="+gatewayId+"&pid="+pid+"&ts="+new Date().getTime(), function() {
		  $("#gatewayFormDialog").dialog({
				modal:true,
				title: '<spring:message code="gatewayForm.heading.name"/>',
// 				height: windowHeight-300, //default auto
				width: "75%",
				buttons: {
					
					Cancel: function() {
						$("#gatewayFormDialog").dialog("close");
						
					}
				},
				close: function(event, ui) {
					try {
						getFloorPlanObj("floorplan").plotChartRefresh();
						reloadGatewayFrame();
						
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

function showFixtureForm(fixtureId,pid) {	
	$("#fixtureFormDialog").load("${fixtureDetailsUrl}?fixtureId="+fixtureId+"&pid="+pid+"&ts="+ new Date().getTime(), function() {
		$("#fixtureFormDialog").dialog({
			modal:true,
			title: '<spring:message code="fixtureForm.heading.name"/>',
			width:  Math.floor($('body').width() * .98),
			height: Math.floor($('body').height() * .94),
			close: function(event, ui) {
				try {
					getFloorPlanObj("floorplan").plotChartRefresh();
					// added to refresh the fixture list grid on fixture list page when edit action happens. 
					var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("fixturesFrame");
					ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
					ifr.src = ifr.src;
				} catch(e) {
				}
				//location.reload();
				$("#fixtureFormDialog").html("");
			}
		});
	});
	return false;
}

var SELECTED_FIXTURES_TO_UPDATE_PROFILE = [];
function assignProfileToFixtures(selFixtures,facilityId){
	if(selFixtures != undefined) {
		SELECTED_FIXTURES_TO_UPDATE_PROFILE = eval("("+selFixtures+")");	
	}
	var width;
	var height;
	var pageUrl;
	pageUrl = "${assignProfileToFixturesDailogUrl}";
	width= Math.floor($('body').width() * .45);
	height=310;
	
	$("#assignProfileToFixturesDailog").load(pageUrl+"?facilityId="+facilityId+"&ts="+new Date().getTime(), function() {
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
//END PROFILE
</script>

<div id="innercenter" class="ui-layout-center">
	<ul>		
		<li><a id="ec" href="#tab_ec" onclick="loadEnergyConsumption();"><span>Energy Consumption</span></a></li>
				 
        <li id="lifp"><a id="fp" href="#tab_fp" onclick="loadFloorPlan();"><span>Floor Plan</span></a></li>
        
        <li id="liin"><a id="in" href="#install" onclick="loadDeviceInstall();"><span>Devices</span></a></li>
		<li><a id="os" href="#settings" onclick="loadSettings();"><span id="settingstab">Settings</span></a></li>
		<c:if test="${enableSensorProfile == 'true'}">
		<li id="lips" ><a id="ps" href="#profile" onclick="loadProfile();"><span id="profileName">Settings</span></a></li>
		<li id="litmplt" ><a id="tmplt" href="#template" onclick="loadProfileTemplates();"><span id="templateName">Settings</span></a></li>
		</c:if>		
	</ul>
	
	<div class="ui-layout-content ui-widget-content ui-corner-bottom" style="border-left: 0; border-top: 0; padding: 0px;">
		<div id="tab_ec" class="pnl_rht flasharea"></div>
		
		<div id="tab_fp" class="pnl_rht"></div>
		
		<div id="install" class="pnl_rht"><iframe frameborder="0" id="installFrame" style="width: 100%; height: 100%;"></iframe></div>
		<div id="settings" class="pnl_rht"><iframe frameborder="0" id="settingFrame" style="width: 100%; height: 100%;"></iframe></div>
		<c:if test="${enableSensorProfile == 'true'}">
		<div id="profile" class="pnl_rht"><iframe frameborder="0" id="profileFrame" style="width: 100%; height: 100%;"></iframe></div>
		<div id="template" class="pnl_rht"><iframe frameborder="0" id="templateFrame" style="width: 100%; height: 100%;"></iframe></div>
		</c:if>
		
	</div>		
</div>
<div id="profileFormDialog"></div>
<div id="fixtureFormDialog"></div>
<div id="gatewayFormDialog"></div>
<div id="assignProfileToFixturesDailog"></div>