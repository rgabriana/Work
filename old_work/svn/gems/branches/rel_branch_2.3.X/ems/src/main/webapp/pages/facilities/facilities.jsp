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
<spring:url value="/events/view.ems" var="eventsViewUrl" scope="request" />
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<style type="text/css">
	.no-close .ui-dialog-titlebar-close {display: none }
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
		
		profilenodeclick();
		nodeclick();
		showflash();
		setAllowedTab();
		
		var accordianSelect = $.cookie('accordian_select');
		if(accordianSelect=="facility")
		{
			$("#lips").hide(); //hide profile tab for first time.
			$("#litmplt").hide(); 
		}
		
		var resizeTimer;
		$(window).resize(function() {
		    clearTimeout(resizeTimer);
		    resizeTimer = setTimeout(resizeEventFired, 100);
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
		}else{
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
	
	var tabEC =document.getElementById("tab_ec");
	tabEC.innerHTML = EC_data; 

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
		FP_data +=  "<param name='flashVars' value='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=FLOORPLAN&enableMotionBits=${enableMotionBits}&role=${role}'/>";
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
</script>

<spring:url value="/devices/gateways/gateway_form.ems" var="updatwGatewayUrl" scope="request" />
<spring:url value="/devices/gateways/updateGateway.ems" var="gatewaySubmitURL" scope="request"/>
<spring:url value="/devices/gateways/gateway_commission_form.ems" var="commissionGatewayUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_form.ems" var="fixtureFormUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_details.ems" var="fixtureDetailsUrl" scope="request" />
<spring:url value="/devices/switches/switch_create.ems" var="switchCreateUrl" scope="request" />
<spring:url value="/devices/switches/switch_edit.ems" var="switchEditUrl" scope="request" />
<spring:url value="/motionbits/create.ems" var="mbScheduleCreateUrl" scope="request" />
<spring:url value="/motionbits/edit.ems" var="mbScheduleEditUrl" scope="request" />
<spring:url value="/devices/switches/create_switch_from_flex.ems" var="createSwitchFromFlexUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_commission_form.ems" var="commissionFixtureUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_commission_start_identify.ems" var="startCommissionFixtureUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_discovery_form.ems" var="discoverFixtureUrl" scope="request" />
<spring:url value="/devices/wds/wds_commission_start_identify.ems" var="startCommissionWdsUrl" scope="request" />
<spring:url value="/devices/wds/wds_commission_form.ems" var="commissionWdsUrl" scope="request" />
<spring:url value="/devices/wds/wds_discovery_form.ems" var="discoverWdsUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_delete_dialog.ems" var="deleteFixtureDialogUrl" scope="request" />
<spring:url value="/users/assignuserstoswitches.ems" var="assignUserToSwitchesDailogUrl" scope="request" />
<spring:url value="/devices/fixtures/assignprofiletofixtures.ems" var="assignProfileToFixturesDailogUrl" scope="request" />
<spring:url value="/devices/fixtures/assignareatofixtures.ems" var="assignAreaToFixturesDailogUrl" scope="request" />
<spring:url value="/devices/switches/assignareatoswitches.ems" var="assignAreaToSwitchesDailogUrl" scope="request" />
<spring:url value="/devices/wds/assignareatodevices.ems" var="assignAreaToDevicesDailogUrl" scope="request" />
<spring:url value="/devices/switches/settings/dialog.ems" var="switchSettingsDialogUrl" scope="request" />
<spring:url value="/devices/groups/groups_create.ems" var="assignGroupsToFixturesDailogUrl" scope="request" />
<spring:url value="/devices/groups/groups_edit.ems" var="editGroupDailogUrl" scope="request" />
<spring:url value="/devices/groups/fixture_groups_reset.ems" var="resetFixtureGemsGroupsDialogUrl" scope="request" />
<spring:url value="/devices/widget/switch/show.ems" var="switchWidgetDlgUrl" scope="request" />
<spring:url value="/devices/widget/switch/prompt.ems" var="switchPromptUrl" scope="request" />
<spring:url value="/devices/widget/group/prompt.ems" var="groupPromptUrl" scope="request" />
<spring:url value="/devices/widget/group/show.ems" var="groupWidgetDlgUrl" scope="request" />
<spring:url value="/devices/widget/create/switch.ems" var="createSwitchUrl" scope="request" />
<spring:url value="/devices/widget/create/group.ems" var="createGroupUrl" scope="request" />
<spring:url value="/devices/wds/wdsedit.ems" var="wdsEditDialogUrl" scope="request" />
<spring:url value="/devices/locatordevices/locatorDevice_create.ems" var="createLocatorDeviceUrl" scope="request" />
<spring:url value="/devices/locatordevices/locatorDevice_edit.ems" var="editLocatorDeviceUrl" scope="request" />

<spring:url value="/profileTemplateManagement/profile_template_form.ems" var="updateTemplateFormUrl" scope="request" />
<spring:url value="/profile/addeditsetting.ems" var="ProfileAddEditUrl" scope="request" />
<spring:url value="/devices/fixtures/assignprofiletofixturesemployeerole.ems" var="assignProfileToFixturesEmployeeDialogUrl" scope="request" />
<spring:url value="/profile/fixturesetting.ems" var="fixtureProfileUrl" scope="request" />
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
					"Update": function() {
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

//Show WDS Commission Dialog
function showWdsCommissioningForm(gatewayId) {
	
	var data = "gatewayId="+gatewayId+"&ts="+new Date().getTime();
	
	$("#wdsCommissioningDialog").load("${commissionWdsUrl}", data, function() {
		  $("#wdsCommissioningDialog").dialog({
				modal: true,
				title: "Commission and Place EWS",
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
						if(SWITCH_ID == undefined || SWITCH_ID == "")
							location.reload();
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
				title: "Discover EWS",
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
			title: 'EWS Edit',
			width :width,
			height : height,// Math.floor($('body').height() * .30),
			close: function(event, ui) {
				if(fromFloorPlan != undefined && fromFloorPlan == true)
					getFloorPlanObj("widget_floorplan").plotChartRefresh();
				else
					location.reload();
			}
		});
	});
	
}

function showFixtureForm(fixtureId) {	
// 	$("#fixtureFormDialog").load("${fixtureFormUrl}?fixtureId="+fixtureId, function() {
	
		
	$("#fixtureFormDialog").load("${fixtureDetailsUrl}?fixtureId="+fixtureId+ "&ts="+ new Date().getTime(), function() {
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
				location.reload();
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

function showSwitchPrompt(){	
	
	$("#switchPromptDialog").load("${switchPromptUrl}", function() {
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
<spring:url value="/services/org/switch/updateGroupSyncFlag/" var="updateGroupSyncFlag" scope="request" />
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
	
	if(switchId == null || switchId == ""){
		//var switchName = prompt("Enter switch name:", "");
		
		if(switchName != null && switchName != "") {

			// First call a service to create a virtual switch; get the id back and then pass it on to the widget dialog
			$("#widgetDialog").load("${createSwitchUrl}?switchName="+encodeURIComponent(switchName)+"&fixtureVersion="+fixtureVersion, function() {
				$("#widgetDialog").dialog({
					modal:true,
					title: 'Switch',
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
					
						location.reload();
					}
				});
			});
			return false;
		}
	}
	else {
		$("#widgetDialog").load("${switchWidgetDlgUrl}?switchId="+ switchId+"&ts="+new Date().getTime(), function() {
			$("#widgetDialog").dialog({
				modal:true,
				title: 'Switch',
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
					location.reload();
					$.ajax({
						url: "${updateGroupSyncFlag}"+switchId+"/"+false+"?ts="+new Date().getTime(),
						dataType:"json",
						contentType: "application/json; charset=utf-8",
						success: function(data){
						}
					});
					
				}
			});
		});
		return false;
	}
	
}

function showGroupPrompt(){	
	
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
	
	if(groupId == null || groupId == ""){
		//var groupName = prompt("Enter Group name:", "");
		
		if(groupName != null && groupName != "") {

			// First call a service to create a virtual switch; get the id back and then pass it on to the widget dialog
			$("#groupWidgetDialog").load("${createGroupUrl}?groupName="+encodeURIComponent(groupName)+"&fixtureVersion="+fixtureVersion, function() {
				$("#groupWidgetDialog").dialog({
					modal:true,
					title: 'Group',
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
						location.reload();
					}
				});
			});
			return false;
		}
	}
	else {
		$("#groupWidgetDialog").load("${groupWidgetDlgUrl}?groupId="+ groupId, function() {
			$("#groupWidgetDialog").dialog({
				modal:true,
				title: 'Group',
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
					location.reload();
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

<div id="switchFormDialog"></div>
<div id="mbScheduleFormDialog"></div>
<div id="fixtureFormDialog"></div>
<div id="gatewayFormDialog"></div>
<div id="gatewayCommissioningDialog"></div>
<div id="fixtureCommissioningDialog"></div>
<div id="wdsCommissioningDialog"></div>
<div id="wdsCommissioningStartIdentifyDialog"></div>
<div id="fixtureCommissioningStartIdentifyDialog"></div>
<div id="fixtureDiscoveryDialog"></div>
<div id="wdsDiscoveryDialog"></div>
<div id="deleteFixtureDailog"></div>
<div id="assignUserToSwitchesDailog"></div>
<div id="assignProfileToFixturesDailog"></div>
<div id="assignAreaToFixturesDailog"></div>
<div id="assignAreaToSwitchesDailog"></div>
<div id="assignAreaToDevicesDailog"></div>
<div id="switchSettingsDialog"></div>
<div id="assignGroupsToFixturesDailog"></div>
<div id="resetFixtureGemsGroupsDialog"></div>
<div id="viewDialog"></div>
<div id="profileTemplateFormDialog"></div>
<div id="profileFormDialog"></div>
<div id="widgetDialog"></div>
<div id="groupWidgetDialog"></div>
<div id="wdsEditDialog"></div>
<div id="switchPromptDialog"></div>
<div id="groupPromptDialog"></div>
<div id="locatorDeviceFormDialog"></div>