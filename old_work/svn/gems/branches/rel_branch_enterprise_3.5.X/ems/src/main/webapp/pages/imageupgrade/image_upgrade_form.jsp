<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/modules/PlotChartModule.swf" var="plotchartmodule"></spring:url>
<spring:url value="/scripts/jquery/jstree/themes/" var="jstreethemefolder"></spring:url>
<spring:url value="/imageupgrade/upload.ems" var="imageUpload" />
<style>
	.innerContainer{
		/* padding: 10px 20px; */
	}
	//fieldset{padding: 10px;}
    //legend{font-weight: bold; margin-left: 10px; padding: 0 2px;}
    
 	//.button{padding: 0 10px;}
    //div.fieldWrapper{clear:both; height:24px; width:40%; margin-bottom:10px;}
 	//div.fieldLabel{float:left; height: 100%; width:30%; font-weight:bold;}
 	//div.fieldValue{float:left; height: 100%; width:65%;}
 	//.input{width:200px; height:95%;}
 	//.messageDiv{display:inline; font-weight:bold; padding-left:10px;}
 	//div.spacing-div{height:5px;}
 	//.img-upg-progressbar{height:1em !important; border: 1px solid #DDDDDD !important; border-radius: 4px 4px 4px 4px !important;}
 	//.img-upg-progressbar .ui-progressbar-value{ background-image: url(../themes/default/images/pbar-ani.gif) !important; border-radius: 4px 0 0 4px !important;} 	
 	//div.property-container{width:70%;}
 	//div.property-container div.property-wrapper{width:25%; float:left;}
 	//div.property-container div.property-wrapper .input{width:95%; height:24px;}
 	//div.property-container div.property-wrapper label{font-weight: bold;} 	
 	//div.imageupg-tab-container {border:1px solid #ccc;}
 	//div.tbldiv {margin:10px; padding-right:17px;}
 	//div.image-upgrade-wrapper {background:#fff;}
 	//div fieldset{padding:20px 10px;}
 	.enablebuttonbutton
	{
		padding:3px 5px 5px 5px;
		height:28px; color:#fff; background:url(../images/blue1px.png);
		border:1px solid #3399cc;
	}

	.disablebutton
	{
		padding:3px 5px 5px 5px;
		height:28px;
		color:#fff; 
		background:none;
		border:1px solid #3399cc;
	}
	
	html, body{margin:3px 3px 0px 3px !important; padding:0px !important; background: #ffffff; overflow:hidden !important;}	
	
</style>

<spring:url value="/services/org/fixture/list/" var="getAvailableFixtureUrl" scope="request" />
<spring:url value="/services/org/gateway/commissioned/list/" var="getAvailableGatewayUrl" scope="request" />
<spring:url value="/services/org/imageupgrade/jobstatus/" var="getImageUpgradeJobStatus" scope="request" />
<spring:url value="/imageupgrade/startimageupgrade.ems" var="startImageUpgradeUrl" scope="request" />
<spring:url value="/scripts/jquery/jquery.blockUI.2.39.js" var="blockUI"></spring:url>
<script type="text/javascript" src="${blockUI}"></script>

<script type="text/javascript"> 
	//Constants
	COLOR_FAILURE = "red";
	COLOR_SUCCESS = "green";
	COLOR_DEFAULT = "black";
	COLOR_BLACK = "black";
	DEVICE_FIXTURE = 0;
	DEVICE_GATEWAY = 1;
	IMG_UP_STATUS_SCHEDULED = "Scheduled";
	IMG_UP_STATUS_INPROGRESS = "In Progress";
	IMG_UP_STATUS_SUCCESS = "Success";
	IMG_UP_STATUS_FAIL = "Fail";
	 var MAX_ROW_NUM = 99999;
	 // Gateways;
	var selGwRows = new Array();
	// Fixtures
	var selFxRows = new Array();
	// WDS
	var selWdsRows = new Array();
	 
	  
//     $(window).resize(function(){
// //     	setGridHeight();
// 		setGridWidth();
//     });
var enablePlugloadFeature;
$(document).ready(function(){
	
	showflash();
	$('#facilityTreeViewDiv').treenodeclick(function(){
		showflash();
	});
	
	manageImageUpload();
	enablePlugloadFeature = "${enablePlugloadFeature}";
	var FX_IMG_COMBO_VALUE;
	var GW_IMG_COMBO_VALUE;
	var WDS_IMG_COMBO_VALUE;
	var PLUGLOAD_IMG_COMBO_VALUE;
	
	
});


function manageImageUpload() {
	var ifr;
	ifr = document.getElementById("imageUpgradeFrame");
	ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = "${imageUpload}?ts="+new Date().getTime();
	return false;
}

$(window).unload(function(){
	//saveGridParameters(FIXTURE_GRID);
	
});

function refreshList() {
	saveGridParameters(FIXTURE_GRID);
	
	loadFixturesGrid(treenodetype, treenodeid);
	loadGatewaysGrid(treenodetype, treenodeid);
}
function fillingGridWithUserSelection(gridName)
{
	//Resetting the Fixture grid according to user selections
			 
	 var gridParams = localStorage.getItem("GridParam");
	    if(gridParams !=null && gridParams!="")
		    {                 
		        var gridInfo = $.parseJSON(gridParams);                                    
		        var grid = gridName;                        

		       // grid.jqGrid('setGridParam', { url: gridInfo.url });
		        grid.jqGrid('setGridParam', { sortname: gridInfo.sortname });
		        grid.jqGrid('setGridParam', { sortorder: gridInfo.sortorder });
		       
		        grid.jqGrid('setGridParam', { page: gridInfo.page });
		        grid.jqGrid('setGridParam', { rowNum: gridInfo.rowNum }); 
		       // grid.jqGrid('setGridParam', { postData: gridInfo.postData });
		       //  grid.jqGrid('setGridParam', { search: gridInfo.search });
		     	
		       //Applied this Fix to maintain Sort Order when page gets refreshed.
		     	jQuery(grid).trigger('reloadGrid');

		     	//Applied this Fix to maintain user multiple selection when page gets refreshed.
		     	var selFixtureRows = gridInfo.selarrrow;
	 	        if(selFixtureRows != null && selFixtureRows != undefined){
			        for(var i=0;i<selFixtureRows.length;i++){
			        	jQuery(grid).jqGrid('setSelection', selFixtureRows[i]);
				   	}
	        	}
		    } 
	}
function saveGridParameters(grid) {       
    var gridInfo = new Object();
   // gridInfo.url = grid.jqGrid('getGridParam', 'url');
    gridInfo.sortname = grid.jqGrid('getGridParam', 'sortname');
    gridInfo.sortorder = grid.jqGrid('getGridParam', 'sortorder');
    gridInfo.selarrrow = grid.jqGrid('getGridParam', 'selarrrow');
    gridInfo.page = grid.jqGrid('getGridParam', 'page');
    gridInfo.rowNum = grid.jqGrid('getGridParam', 'rowNum');
   // gridInfo.postData = grid.jqGrid('getGridParam', 'postData');
   // gridInfo.search = grid.jqGrid('getGridParam', 'search');
    localStorage.setItem("GridParam",JSON.stringify(gridInfo));
}
function createFixtureGrid(){
	var gridParams = localStorage.getItem("GridParam");
	var flag =1 ;
	
	FIXTURE_GRID.jqGrid({
		datatype: "clientSide",
 		height: "auto",
		autowidth: true,
		forceFit: true,
		scrollOffset: 18,
		hoverrows: false,
		loadonce: true,
	   	colNames:["id", "Name", "Mode", "Version", "Upgrade Status"],
	   	colModel:[
			{name:'id', index:'id', hidden:true},
	   		{name:'name', index:'name', sorttype:"string", sortable: true, width:"25%"},
	   		{name:'currentstate', index:'currentstate', sorttype:"string", sortable: true, width:"25%"},
	   		{name:'version', index:'version', sorttype:"string", sortable: true, width:"25%"},
	   		{name:'upgradestatus', index:'upgradestatus', sorttype:"string", sortable: true, width:"25%"}
	   		
	   	],
	   sortname: 'name',
	    viewrecords: true,
	    sortorder: "desc",
	   	multiselect: true,
	   	rowNum: MAX_ROW_NUM,
		toolbar: [false,"bottom"],
		loadComplete: function() {
	    	 ModifyFixtureGridDefaultStyles();
	    }
	});
	
	loadFixtureImageCombo();
}

function createGatewayGrid(){
	GATEWAY_GRID.jqGrid({
		datatype: "clientSide",
 		height: "auto",
		autowidth: true,
		forceFit: true,
		scrollOffset: 18,
		hoverrows: false,
		loadonce: true,
		colNames:["id", "Name", "Version", "No. of Fixtures", "Upgrade Status"],
	   	colModel:[
  			{name:'id', index:'id', hidden:true},
	   		{name:'name', index:'name', sorttype:"string", sortable: true, width:"25%"},
	   		{name:'app2version', index:'app2version', sorttype:"string", sortable: true, width:"25%"},
	   		{name:'noofactivesensors', index:'noofactivesensors', sorttype:"int", sortable: true, width:"25%"},
	   		{name:'upgradestatus', index:'upgradestatus', sorttype:"string", sortable: true, width:"25%"}
	   	],
	   	sortname: 'name',
	    viewrecords: true,
	    sortorder: "desc",		    
	   	multiselect: true,
	    rowNum: MAX_ROW_NUM,
		toolbar: [false,"bottom"],

		loadComplete: function() {
	    	 ModifyGatewayGridDefaultStyles();
	    }
		
	});
	
	loadGatewayImageCombo();
}

//Load Fixture Images drop down
function loadFixtureImageCombo(){
	FX_IMG_COMBO = $("#fximageId");
	
	FX_IMG_COMBO.empty();
	FX_IMG_COMBO.append($('<option></option>').val("-1").html("Select a fixture image"));
	<c:forEach items="${fixtureUpgradeimages}" var="fixtureImage">
		FX_IMG_COMBO.append($('<option></option>').val("${fixtureImage}").html("${fixtureImage}"));
	</c:forEach>
}

//Load Gateway Images drop down
function loadGatewayImageCombo(){
	GW_IMG_COMBO = $("#gwImageId");
	
	GW_IMG_COMBO.empty();
	GW_IMG_COMBO.append($('<option></option>').val("-1").html("Select a gateway image"));
	<c:forEach items="${gatewayUpgradeimages}" var="gatewayImage">
		GW_IMG_COMBO.append($('<option></option>').val("${gatewayImage}").html("${gatewayImage}"));
	</c:forEach>
}

//Load Wds Images drop down
function loadWdsImageCombo(){
	WDS_IMG_COMBO = $("#wdsimageId");
	
	WDS_IMG_COMBO.empty();
	WDS_IMG_COMBO.append($('<option></option>').val("-1").html("Select a ERC image"));
	<c:forEach items="${wdsUpgradeimages}" var="wdsImage">
		WDS_IMG_COMBO.append($('<option></option>').val("${wdsImage}").html("${wdsImage}"));
	</c:forEach>
}

//create Devices Tab Panel
function createTabPanel(){
	TAB_PANEL.tabs({
		create: function(event, ui) { 
			TAB_PANEL.tabs("select", ACTIVE_TAB);
			setGridWidth();
		},
		select: function(event, ui) {
			ACTIVE_TAB = ui.index;
		}
	});
}

function setGridWidthOnTabSelect(activeTab){
	/* if(activeTab==0){
		FIXTURE_GRID.jqGrid("setGridWidth", Math.floor(TAB_PANEL.width()*1)-2);
	} else if(activeTab==1){
		GATEWAY_GRID.jqGrid("setGridWidth", Math.floor(TAB_PANEL.width()*1)-2);
	} */
}

//Set grid width to fit in the tab Panel
function setGridWidth(){
	/* FIXTURE_GRID.jqGrid("setGridWidth", Math.floor(TAB_PANEL.width()*1)-2);
	GATEWAY_GRID.jqGrid("setGridWidth", Math.floor(TAB_PANEL.width()*1)-2); */
}

//Set grid height to fit in the main container i.e. right panel
function setGridHeight(){
	var DEFAULT_HEIGHT = 150;
	var gridHeight = DEFAULT_HEIGHT;//default auto height
	var mainCtrHeight =  $("#right")[0].offsetHeight;
	var gridCtrHeight = $("#right").children("div:first")[0].offsetHeight;
	
	var adjustment = mainCtrHeight - gridCtrHeight;
	var newHeight = Math.floor((gridHeight + adjustment)* .99);
	
	if(newHeight > DEFAULT_HEIGHT){//ensure min. as default height
		FIXTURE_GRID.jqGrid("setGridHeight", newHeight);
		GATEWAY_GRID.jqGrid("setGridHeight", newHeight);
	}
}
var WaitMsg = function () {
	jQuery('#tab-fixture').block({ css: { backgroundColor: '#ffffff', color: '#000'}, message: '<h4>Refreshing...</h4>' });
};
var StopWaiting = function () {
    jQuery('#tab-fixture').unblock();
    
    ModifyGatewayGridDefaultStyles();
	ModifyFixtureGridDefaultStyles();
    
};

//load fixtures based on tree selection
function loadFixturesGrid(nodeType, nodeId){
	WaitMsg();
	var COMMISSION_STATE = "COMMISSIONED";
	//FIXTURE_GRID.jqGrid("resetSelection");
	FIXTURE_GRID.jqGrid("clearGridData");

	$.ajax({
		type: 'GET',
		url: "${getAvailableFixtureUrl}"+nodeType+"/"+nodeId+"/",
		data: "",
		asyc:false,
		success: function(data){
				if(data!=null){
					fixturesData = data.fixture;
					if(fixturesData != undefined){
						if(fixturesData.length == undefined){ //FIXME : JSON response issue : For only 1 record it returns Object instead of an Array
							fixturesData.upstatus = progessStatusRenderer(fixturesData.upgradestatus, fixturesData.id, DEVICE_FIXTURE);
							if (fixturesData.state == COMMISSION_STATE) {
								
								FIXTURE_GRID.jqGrid('addRowData', 0, fixturesData);
							}
						} else if(fixturesData.length > 0){
							var cnt = 0;
							$.each(fixturesData, function(i, fixtureJson) {
								if (fixtureJson.state == COMMISSION_STATE) {
									cnt++;
									FIXTURE_GRID.jqGrid('addRowData', fixtureJson.id, fixtureJson);
								}
							});
						}
					}
					//Init all progessbar
					//$(".img-upg-progressbar").progressbar({value: 0});
				}
				 
				// set user preferences of grid and then perform load. If this is firstime, function will handle that.
				// When grid data is loaded completely, reload the grid with user selection Options like  Sort, User selected row.
				fillingGridWithUserSelection(FIXTURE_GRID) ;
				 StopWaiting();

			},
			error: function (xhr, st, err) {
	            // display error information
	            StopWaiting();
	            //If Session Timeout then navigate to login page
	            if (xhr.status === 401) {
	  	    	  window.location="${logouturl}"+"?ts="+new Date().getTime();
	  	   		 }
	        },

		dataType:"json",
		contentType: "application/json; charset=utf-8",
	});
	
}

function progessStatusRenderer(status, deviceId, deviceType){
	var result = status;
	if(status == IMG_UP_STATUS_INPROGRESS){
		var id = "iu-d"+deviceType+"-pbar-"+deviceId;
		result = "<div id='"+id+"' class='img-upg-progressbar'></div>";
	}
	return result;
}

function updateProgessStatus(value, deviceId, deviceType){
	var id = "iu-d"+deviceType+"-pbar-"+deviceId;
	$("#"+id).progressbar("value", value);
}

//load fixtures based on tree selection
function loadGatewaysGrid(nodeType, nodeId){
	GATEWAY_GRID.jqGrid("resetSelection");
	GATEWAY_GRID.jqGrid("clearGridData");
	
	$.ajax({
		type: 'GET',
		url: "${getAvailableGatewayUrl}"+nodeType+"/"+nodeId,
		data: "",
		success: function(data){
				if(data!=null){
					gatewayData = data.gateway;
					if(gatewayData != undefined){
						if(gatewayData.length == undefined){//FIXME : JSON response issue : For only 1 record it returns Object instead of an Array
							gatewayData.upstatus = progessStatusRenderer(gatewayData.upgradestatus, gatewayData.id, DEVICE_GATEWAY);
							GATEWAY_GRID.jqGrid('addRowData', 0, gatewayData);
						} else if(gatewayData.length > 0){
							var cnt = 0;
							$.each(gatewayData, function(i, gatewayJson) {
								cnt++;
								GATEWAY_GRID.jqGrid('addRowData', gatewayJson.id, gatewayJson);
							});
						}
					}

					//Init all progessbar
					//$(".img-upg-progressbar").progressbar({value: 0});
				}
			},
			error: function (xhr, st, err) {
	            //If Session Timeout then navigate to login page
	            if (xhr.status === 401) {
	  	    	  window.location="${logouturl}"+"?ts="+new Date().getTime();
	  	   		 }
	        },
		dataType:"json",
		contentType: "application/json; charset=utf-8",
	});
}

//this is to resize the fixture-jqGrid and gateway-jqGrid  on resize of browser window.
$(window).bind('resize', function() {	
	//$("#fixtureTable").setGridWidth($(window).width()-320);
	//$("#gatewayTable").setGridWidth($(window).width()-320);
}).trigger('resize');

function ModifyGatewayGridDefaultStyles() {  
	   $('#' + "gatewayTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "gatewayTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "gatewayTable" + ' tr:nth-child(odd)').addClass("oddTableRow");	   
}


function ModifyFixtureGridDefaultStyles() {  
	   $('#' + "fixtureTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "fixtureTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "fixtureTable" + ' tr:nth-child(odd)').addClass("oddTableRow");	   
}

</script>


<script type="text/javascript">




function validateUpgradeForm(device,gw,fx,wds,plugload) {
	
		GW_IMG_COMBO_VALUE = gw;
		FX_IMG_COMBO_VALUE = fx;
		WDS_IMG_COMBO_VALUE = wds;
		PLUGLOAD_IMG_COMBO_VALUE = plugload;
		//if (getFloorPlanObj("image_upgrade_floorplan") != null) {
	    	getFloorPlanObj("image_upgrade_floorplan").getSelectedDevices();
	    //}
}

function setSelectedDevices(selFixtures, selGateways, selWds, selPlugload) {
 	// Gateways;
	selGwRows = [];
	// Fixtures
	selFxRows = [];
	// WDS
	selWdsRows = [];
	
	// Plugload
	selplugloadRows = [];
	
	var dataStr = "";
	if (selFixtures != null && selFixtures.length > 0) {
		selFxRows = selFixtures;
		for (var count = 0; count < selFxRows.length; count++) {
			var fixture = selFxRows[count];
			dataStr += "{id: " + fixture.id + ", name: " + fixture.name + ", version: " + fixture.version + "},";
		}
	}
	
	if (selGateways != null && selGateways.length > 0) {
		selGwRows = selGateways;
		for (var count = 0; count < selGwRows.length; count++) {
			var gateway = selGwRows[count];
			dataStr += "{id: " + gateway.id + ", name: " + gateway.name + ", version: " + gateway.app2version + "},";
		}
	}
	
	if (selWds != null && selWds.length > 0) {
		selWdsRows = selWds;
		for (var count = 0; count < selWdsRows.length; count++) {
			var wds = selWdsRows[count];
			dataStr += "{id: " + wds.id + ", name: " + wds.name + ", version: " + wds.version + "},";
		}
	}
	
	if (selPlugload != null && selPlugload.length > 0) {
		selplugloadRows = selPlugload;
		for (var count = 0; count < selplugloadRows.length; count++) {
			var plugload = selplugloadRows[count];
			dataStr += "{id: " + plugload.id + ", name: " + plugload.name + ", version: " + plugload.version + "},";
		}
	}
	
	document.getElementById("imageUpgradeFrame").contentWindow.clearFixtureUpgradeMessage();
	document.getElementById("imageUpgradeFrame").contentWindow.clearGatewayUpgradeMessage();
	document.getElementById("imageUpgradeFrame").contentWindow.clearWdsUpgradeMessage();
	document.getElementById("imageUpgradeFrame").contentWindow.clearPlugloadUpgradeMessage();

	var fixtureIds = [];
	var gatewayIds = [];
	var wdsIds = [];
	var plugloadIds = [];
	var fxImageFileName = "";
	var gwImageFileName = "";
	var wdsImageFileName = "";
	var plugloadImageFileName = "";

	if (selGwRows.length == 0 && GW_IMG_COMBO_VALUE == -1 && selFxRows.length == 0 && FX_IMG_COMBO_VALUE == -1 && selWdsRows.length == 0 && WDS_IMG_COMBO_VALUE == -1  && (enablePlugloadFeature == 'false' || (selplugloadRows.length ==0 && PLUGLOAD_IMG_COMBO_VALUE ==-1)) ) {
		document.getElementById("imageUpgradeFrame").contentWindow.displayGatewayUpgradeMessage("<spring:message code='imageUpgrade.message.validation.selectImage'/>", COLOR_FAILURE);
		return false;
	}
	//Gateway
	if(selGwRows.length > 0 && GW_IMG_COMBO_VALUE ==-1){
		document.getElementById("imageUpgradeFrame").contentWindow.displayGatewayUpgradeMessage("<spring:message code='imageUpgrade.message.validation.selectGatewayImage'/>", COLOR_FAILURE);
		return false;
	}
	if(selGwRows.length == 0 && GW_IMG_COMBO_VALUE !=-1) {
		document.getElementById("imageUpgradeFrame").contentWindow.displayFixtureUpgradeMessage("<spring:message code='imageUpgrade.message.validation.emptyGateways'/>", COLOR_FAILURE);
		return false;
	}
	
	// Fixtures
	if(selFxRows.length > 0 && FX_IMG_COMBO_VALUE ==-1){
		document.getElementById("imageUpgradeFrame").contentWindow.displayFixtureUpgradeMessage("<spring:message code='imageUpgrade.message.validation.selectFixtureImage'/>", COLOR_FAILURE);
		return false;
	}
	if(selFxRows.length == 0 && FX_IMG_COMBO_VALUE !=-1) {
		document.getElementById("imageUpgradeFrame").contentWindow.displayFixtureUpgradeMessage("<spring:message code='imageUpgrade.message.validation.emptyFixtures'/>", COLOR_FAILURE);
		return false;
	}

	//WDS
	if(selWdsRows.length > 0 && WDS_IMG_COMBO_VALUE ==-1){
		document.getElementById("imageUpgradeFrame").contentWindow.displayWdsUpgradeMessage("<spring:message code='imageUpgrade.message.validation.selectWdsImage'/>", COLOR_FAILURE);
		return false;
	}
	if(selWdsRows.length == 0 && WDS_IMG_COMBO_VALUE !=-1) {
		document.getElementById("imageUpgradeFrame").contentWindow.displayWdsUpgradeMessage("<spring:message code='imageUpgrade.message.validation.emptyWds'/>", COLOR_FAILURE);
		return false;
	}
	
	//plugload
	if(enablePlugloadFeature == 'true'){
		if(selplugloadRows.length > 0 && PLUGLOAD_IMG_COMBO_VALUE ==-1){
			document.getElementById("imageUpgradeFrame").contentWindow.displayPlugloadUpgradeMessage("<spring:message code='imageUpgrade.message.validation.selectPlugloadImage'/>", COLOR_FAILURE);
			return false;
		}
		
		if(selplugloadRows.length == 0 && PLUGLOAD_IMG_COMBO_VALUE !=-1 && enablePlugloadFeature == 'true') {
			document.getElementById("imageUpgradeFrame").contentWindow.displayPlugloadUpgradeMessage("<spring:message code='imageUpgrade.message.validation.emptyPlugload'/>", COLOR_FAILURE);
			return false;
		}
	}
	
	var isVersionOneImage = true;
	var displayVersionMismatchMsg = false;
	
	gwImageFileName = GW_IMG_COMBO_VALUE;
	
	// If the image has "enlighted" in the name then it is version 1 image
	if(gwImageFileName.search(/enlighted/i) == -1)
		isVersionOneImage = false;
	
	var gwNum = selGwRows.length;
	for(var i = 0 ; i < gwNum; i++){
		//var gateway = GATEWAY_GRID.jqGrid('getRowData', selGwRows[i]);
		var gateway = selGwRows[i]; 
		
		// Make sure that only devices with the appropriate version get selected
		if((isVersionOneImage && (gateway.app2version.match("^1.") != null)) || (!isVersionOneImage && (gateway.app2version.match("^2.") != null)))
			gatewayIds.push(gateway.id);
		else
			displayVersionMismatchMsg = true;
	}

	fxImageFileName = FX_IMG_COMBO_VALUE;

	isVersionOneImage = true;
	
	// If the image has "enlighted" in the name then it is version 1 image
	if(fxImageFileName.search(/enlighted/i) == -1)
		isVersionOneImage = false;
	
	var fixNum = selFxRows.length;
	for(var i = 0 ; i < fixNum; i++){
		//var fixture = FIXTURE_GRID.jqGrid('getRowData', selFxRows[i]);
		var fixture = selFxRows[i]; 
		
		// Make sure that only devices with the appropriate version get selected
		if((isVersionOneImage && fixture.version.match("^1.") != null) || (!isVersionOneImage && fixture.version.match("^2.") != null))
			fixtureIds.push(fixture.id);
		else
			displayVersionMismatchMsg = true;
	}
	
	if(displayVersionMismatchMsg)
	{
		if(isVersionOneImage)
			alert("<spring:message code='imageUpgrade.message.validation.versionOneDevices'/>");
		else
			alert("<spring:message code='imageUpgrade.message.validation.versionTwoDevices'/>");
	}

	// WDS
	wdsImageFileName = WDS_IMG_COMBO_VALUE;
	
	var wdsNum = selWdsRows.length;
	for(var i = 0 ; i < wdsNum; i++){
		var wds = selWdsRows[i]; 
		// Make sure that only devices with the appropriate version get selected
		wdsIds.push(wds.id);
	}
	
	// Plugload

	if(enablePlugloadFeature == 'true'){
		plugloadImageFileName = PLUGLOAD_IMG_COMBO_VALUE;
		
		var plugloadNum = selplugloadRows.length;
		for(var i = 0 ; i < plugloadNum; i++){
			var plugload = selplugloadRows[i]; 
			// Make sure that only devices with the appropriate version get selected
			plugloadIds.push(plugload.id);
		}
	}
	
	// Now after version check see if there are any devices left to upgrade; if not then display message and exit
	if(gatewayIds.length == 0 && GW_IMG_COMBO_VALUE !=-1) {
//		alert("<spring:message code='imageUpgrade.message.validation.emptyGateways'/>");
		return false;
	}
	
	if(fixtureIds.length == 0 && FX_IMG_COMBO_VALUE !=-1) {
//		alert("<spring:message code='imageUpgrade.message.validation.emptyFixtures'/>");
		return false;
	}

	if(wdsIds.length == 0 && WDS_IMG_COMBO_VALUE !=-1) {
//		alert("<spring:message code='imageUpgrade.message.validation.emptyWds'/>");
		return false;
	}
	if(enablePlugloadFeature == 'true'){
		if(plugloadIds.length == 0 && PLUGLOAD_IMG_COMBO_VALUE !=-1) {
	//		alert("<spring:message code='imageUpgrade.message.validation.emptyWds'/>");
			return false;
		}
	}
	
	// Confirm
	var confirmMsg = "<spring:message code='imageUpgrade.message.confirmImageUpgrade'/>";
	var result = confirm(confirmMsg);
	if (result == true) {
		//$('#imgUpgradeSubmitBtn').attr("disabled", true);
		//AJAX call to upgrade image
		$.ajax({
			type: 'POST',
			url: "${startImageUpgradeUrl}",
			data: "gwImageName="+gwImageFileName+"&gatewayIds="+gatewayIds+"&fxImageName="+fxImageFileName+"&fixtureIds="+fixtureIds+"&wdsImageName="+wdsImageFileName+"&wdsIds="+wdsIds+"&plugloadImageFileName="+plugloadImageFileName+"&plugloadIds="+plugloadIds+"&ts="+new Date().getTime(),
			success: function(data){
				if(data!=null){	
					var jsonData = $.parseJSON(data);
					if(jsonData.success == -1) {
						document.getElementById("imageUpgradeFrame").contentWindow.displayGatewayUpgradeMessage('Cannot proceed with image upgrade. There is some critical <a href="' + location.protocol + '//' + location.host	+ '/em_mgmt/home/"' + '><span style="color: blue; text-decoration: underline;">change/process</span></a> going on the server. Please try after some time.', COLOR_FAILURE);
					}else {
						document.getElementById("imageUpgradeFrame").contentWindow.displayGatewayUpgradeMessage('Image upgrade scheduled.', COLOR_DEFAULT);
						getFloorPlanObj("image_upgrade_floorplan").startImageUpgradeRefresh();
					}
				}
			}
		});
		
		
	}
	return result;
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
		if (getFloorPlanObj("image_upgrade_floorplan") != null)
	    	getFloorPlanObj("image_upgrade_floorplan").handleWheel(o);
	}
	
	function loadFloorPlan(){
		loadFP();
	}
	var loadFP = function() {
		try{
			if(window.addEventListener) {
	            var eventType = ($.browser.mozilla) ? "DOMMouseScroll" : "mousewheel";            
	            window.addEventListener(eventType, handleWheel, false);
	            getFloorPlanObj("image_upgrade_floorplan").onmousemove=null; // Handling poor mouse wheel behavior in Internet Explorer.
	        }
	        //alert(treenodetype + " " + treenodeid);
			getFloorPlanObj("image_upgrade_floorplan").changeLevel(treenodetype, treenodeid, 'IMAGE_UPGRADE','');
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
			FP_data = "<object id='image_upgrade_floorplan' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='100%'>";
			FP_data +=  "<param name='src' value='"+plotchartmoduleString+"'/>";
			FP_data +=  "<param name='padding' value='0px'/>";
			FP_data +=  "<param name='wmode' value='opaque'/>";
			FP_data +=  "<param name='allowFullScreen' value='true'/>";
			FP_data +=  "<param name='flashVars' value='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=IMAGE_UPGRADE&enablePlugloadFeature=${enablePlugloadFeature}&modeid='/>";
			FP_data +=  "<embed id='image_upgrade_floorplan' name='image_upgrade_floorplan' src='"+plotchartmoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " padding='0px'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=IMAGE_UPGRADE&enablePlugloadFeature=${enablePlugloadFeature}&modeid='/>";
			FP_data +=  "</object>";
		} else {
			FP_data = "<embed id='image_upgrade_floorplan' name='image_upgrade_floorplan' src='"+plotchartmoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " padding='0px'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=IMAGE_UPGRADE&enablePlugloadFeature=${enablePlugloadFeature}&modeid='/>";
		}
		
		var tabFP =document.getElementById("tab_fp");
		tabFP.innerHTML = FP_data; 
		// quick fix for the duplicate flash object
		$('div.alt').remove(); 
	}
	
	function clearAllImageUpgradeStatusMsg()
	{
		document.getElementById("imageUpgradeFrame").contentWindow.clearFixtureUpgradeMessage();
		document.getElementById("imageUpgradeFrame").contentWindow.clearGatewayUpgradeMessage();
		document.getElementById("imageUpgradeFrame").contentWindow.clearWdsUpgradeMessage();
	}
	
</script>

<div class="outermostdiv" >
	<div style="background-color:#FFFFFF !important;">
		<iframe frameborder="0" id="imageUpgradeFrame" style="width: 100%; height: 100%;background-color:#FFFFFF !important;">
	</iframe>
	<script type="text/javascript">		
	$(function() {
		$(window).resize(function() {
			$("#imageUpgradeFrame").css("height", "150px");
		});
	});
	$("#imageUpgradeFrame").css("height", "150px");
	</script>
	</div>
	<div style="width: 100%;height:100%">
		<div id="tab_fp" class="pnl_rht"></div>
	</div>
</div>


