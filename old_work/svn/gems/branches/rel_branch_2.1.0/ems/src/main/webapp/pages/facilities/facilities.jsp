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
<spring:url value="/events/view.ems" var="eventsViewUrl" scope="request" />
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
		
		profilenodeclick();
		nodeclick();
		showflash();
		setAllowedTab();
		$("#lips").hide(); //hide profile tab for first time.
		
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
		return document[objectName] 
	}
	return document.getElementById(objectName);
}	

//fuction to show allowed tabs as per accordion tab selected
function setAllowedTab() {
	$('#accordionfacility h2').accordiontabclick(function(){				
		//the 'accTabSelected' variable is global and defined in facility_tree.jsp
		if(accTabSelected=='pf'){
	 		$("#lifp").hide();
	 		$("#liin").hide();
 			$("#lips").show(); 	
 			$("#install").hide();
 			$("#events_and_faults").show();
 			$('#lief').show();
 			$("#settingstab").html("Devices");
 			$("#ec").click();
 		}
 		else{
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
		EC_data = "<embed id='energysummary' name='energysummary' src='"+energysummarymoduleString+"' pluginspage='https://www.adobe.com/go/getflashplayer'";
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
		FP_data +=  "<param name='flashVars' value='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=FLOORPLAN&role=${role}'/>";
		FP_data +=  "<embed id='floorplan' name='floorplan' src='"+plotchartmoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
		FP_data +=  " height='100%'";
		FP_data +=  " width='100%'";
		FP_data +=  " padding='0px'";
		FP_data +=  " wmode='opaque'";
		FP_data +=  " allowFullScreen='true'";
		FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=FLOORPLAN&role=${role}'/>";
		FP_data +=  "</object>";
	} else {
		FP_data = "<embed id='floorplan' name='floorplan' src='"+plotchartmoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
		FP_data +=  " height='100%'";
		FP_data +=  " width='100%'";
		FP_data +=  " wmode='opaque'";
		FP_data +=  " padding='0px'";
		FP_data +=  " allowFullScreen='true'";
		FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=FLOORPLAN&role=${role}'/>";
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
	$('#profileTreeViewDiv').profiletreenodeclick(function(){
		$('#profileName').text(profilenodename + ' Profile');

		if (tabselected == 'energy consumption') {
			$("#ec").click();		
		}
		else if (tabselected == 'settings') {
			$("#os").click();
		}
		else //if (tabselected == 'profile') 
		{
			$('#ps').click();
		}
	});
}

function loadSettings() {
	removeWheelEvent();
	tabselected = 'settings';
    var ifr;
    ifr = document.getElementById("settingFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    
    //Open different fixtures list for profiles and facilities
    if(accTabSelected=='pf'){
    	ifr.src="${loadProfilesFixturesSettings}";
    }
    else{
    	ifr.src="${loadSetting}";
    }
    
    return false;
}


function loadEvents() {
	removeWheelEvent();
	tabselected = 'events';
    var ifr;
    ifr = document.getElementById("eventsAndFaultsFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
   	ifr.src="${loadEvents}?ts="+new Date().getTime();
    return false;
}

function loadDeviceInstall() {
	removeWheelEvent();
	tabselected = 'install';
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
    ifr = document.getElementById("profileFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src="${loadProfile}?ts="+new Date().getTime();
    return false;
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

<spring:url value="/devices/gateways/gateway_form.ems" var="updatwGatewayUrl" scope="request" />
<spring:url value="/devices/gateways/updateGateway.ems" var="gatewaySubmitURL" scope="request"/>
<spring:url value="/devices/gateways/gateway_commission_form.ems" var="commissionGatewayUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_form.ems" var="fixtureFormUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_details.ems" var="fixtureDetailsUrl" scope="request" />
<spring:url value="/devices/switches/switch_create.ems" var="switchCreateUrl" scope="request" />
<spring:url value="/devices/switches/switch_edit.ems" var="switchEditUrl" scope="request" />
<spring:url value="/devices/switches/create_switch_from_flex.ems" var="createSwitchFromFlexUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_commission_form.ems" var="commissionFixtureUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_commission_start_identify.ems" var="startCommissionFixtureUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_discovery_form.ems" var="discoverFixtureUrl" scope="request" />
<spring:url value="/devices/fixtures/fixture_delete_dialog.ems" var="deleteFixtureDialogUrl" scope="request" />
<spring:url value="/users/assignuserstoswitches.ems" var="assignUserToSwitchesDailogUrl" scope="request" />
<spring:url value="/devices/fixtures/assignprofiletofixtures.ems" var="assignProfileToFixturesDailogUrl" scope="request" />
<spring:url value="/devices/fixtures/assignareatofixtures.ems" var="assignAreaToFixturesDailogUrl" scope="request" />
<spring:url value="/devices/switches/settings/dialog.ems" var="switchSettingsDialogUrl" scope="request" />
<spring:url value="/devices/groups/groups_create.ems" var="assignGroupsToFixturesDailogUrl" scope="request" />
<spring:url value="/devices/groups/groups_edit.ems" var="editGroupDailogUrl" scope="request" />
<spring:url value="/devices/groups/fixture_groups_reset.ems" var="resetFixtureGemsGroupsDialogUrl" scope="request" />


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

// Show Commission Dialog
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
				} catch(e) {
				}
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
				ifr.src = ifr.src;
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
function assignProfileToFixtures(selFixtures){
	if(selFixtures != undefined) {
		SELECTED_FIXTURES_TO_UPDATE_PROFILE = eval("("+selFixtures+")");	
	}
	
	$("#assignProfileToFixturesDailog").load("${assignProfileToFixturesDailogUrl}"+"?ts="+new Date().getTime(), function() {
		$("#assignProfileToFixturesDailog").dialog({
			modal:true,
			title: 'Assign profile to fixtures',
			width : Math.floor($('body').width() * .40),
			height : 130,// Math.floor($('body').height() * .30),
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
// This function will close EventAndFaultView popupwindow
function closeDialog(){
	$("#viewDialog").dialog('close');        	
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
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin')">
			<li><a id="os" href="#settings" onclick="loadSettings();"><span id="settingstab">Settings</span></a></li>
			<li id="lips" ><a id="ps" href="#profile" onclick="loadProfile();"><span id="profileName">Profile</span></a></li>
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

		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin')">
			<div id="settings" class="pnl_rht"><iframe frameborder="0" id="settingFrame" style="width: 100%; height: 100%;"></iframe></div>
			<div id="profile" class="pnl_rht"><iframe frameborder="0" id="profileFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin', 'TenantAdmin')">
			<div id="events_and_faults" class="pnl_rht"><iframe frameborder="0" id="eventsAndFaultsFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>
	</div>		
</div>
<div id="switchFormDialog"></div>
<div id="fixtureFormDialog"></div>
<div id="gatewayFormDialog"></div>
<div id="gatewayCommissioningDialog"></div>
<div id="fixtureCommissioningDialog"></div>
<div id="fixtureCommissioningStartIdentifyDialog"></div>
<div id="fixtureDiscoveryDialog"></div>
<div id="deleteFixtureDailog"></div>
<div id="assignUserToSwitchesDailog"></div>
<div id="assignProfileToFixturesDailog"></div>
<div id="assignAreaToFixturesDailog"></div>
<div id="switchSettingsDialog"></div>
<div id="assignGroupsToFixturesDailog"></div>
<div id="resetFixtureGemsGroupsDialog"></div>
<div id="viewDialog"></div>