<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/modules/PlotChartModule.swf" var="plotchartmodule"></spring:url>
<spring:url value="/scripts/jquery/jstree/themes/" var="jstreethemefolder"></spring:url>
<style>
	.innerContainer{
		/* padding: 10px 20px; */
	}
	fieldset{padding: 10px;}
    legend{font-weight: bold; margin-left: 10px; padding: 0 2px;}
    
 	.button{padding: 0 10px;}
    div.fieldWrapper{clear:both; height:24px; width:40%; margin-bottom:10px;}
 	div.fieldLabel{float:left; height: 100%; width:30%; font-weight:bold;}
 	div.fieldValue{float:left; height: 100%; width:65%;}
 	.input{width:200px; height:95%;}
 	.messageDiv{display:inline; font-weight:bold; padding-left:10px;}
 	div.spacing-div{height:5px;}
 	.img-upg-progressbar{height:1em !important; border: 1px solid #DDDDDD !important; border-radius: 4px 4px 4px 4px !important;}
 	.img-upg-progressbar .ui-progressbar-value{ background-image: url(../themes/default/images/pbar-ani.gif) !important; border-radius: 4px 0 0 4px !important;} 	
 	div.property-container{width:70%;}
 	div.property-container div.property-wrapper{width:25%; float:left;}
 	div.property-container div.property-wrapper .input{width:95%; height:24px;}
 	div.property-container div.property-wrapper label{font-weight: bold;} 	
 	div.imageupg-tab-container {border:1px solid #ccc;}
 	div.tbldiv {margin:10px; padding-right:17px;}
 	div.image-upgrade-wrapper {background:#fff;}
 	div fieldset{padding:20px 10px;}
</style>

<spring:url value="/services/org/fixture/list/" var="getAvailableFixtureUrl" scope="request" />
<spring:url value="/services/org/gateway/commissioned/list/" var="getAvailableGatewayUrl" scope="request" />
<spring:url value="/imageupgrade/startimageupgrade.ems" var="startImageUpgradeUrl" scope="request" />
<spring:url value="/scripts/jquery/jquery.blockUI.2.39.js" var="blockUI"></spring:url>
<script type="text/javascript" src="${blockUI}"></script>

<script type="text/javascript"> 
	//Constants
	COLOR_FAILURE = "red";
	COLOR_SUCCESS = "green";
	COLOR_DEFAULT = "black";
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
$(document).ready(function(){
	//show server message
	displayImageUpgradeMessage("${message}", COLOR_SUCCESS);
	
	//Global variables
	FX_IMG_COMBO = $("#fximageId");
	GW_IMG_COMBO = $("#gwImageId");
	WDS_IMG_COMBO = $("#wdsimageId");
	loadFixtureImageCombo();
	loadGatewayImageCombo();
	loadWdsImageCombo();
	showflash();
	$('#facilityTreeViewDiv').treenodeclick(function(){
		showflash();
	});
});

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
	WDS_IMG_COMBO.append($('<option></option>').val("-1").html("Select a wds image"));
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

function displayImageUpgradeMessage(Message, Color) {
	$("#image_upload_message").html(Message);
	$("#image_upload_message").css("color", Color);
}
function clearImageUpgradeMessage(Message, Color) {
	displaySwitchLabelMessage("", COLOR_DEFAULT);
}

function displayFixtureUpgradeMessage(Message, Color) {
	$("#image_upgrade_message").html(Message);
	$("#image_upgrade_message").css("color", Color);
}
function displayWdsUpgradeMessage(Message, Color) {
	$("#image_upgrade_message").html(Message);
	$("#image_upgrade_message").css("color", Color);
}

function clearFixtureUpgradeMessage(Message, Color) {
	displayFixtureUpgradeMessage("", COLOR_DEFAULT);
}

function displayGatewayUpgradeMessage(Message, Color) {
	$("#image_upgrade_message").html(Message);
	$("#image_upgrade_message").css("color", Color);
}
function clearGatewayUpgradeMessage(Message, Color) {
	displayGatewayUpgradeMessage("", COLOR_DEFAULT);
}
function clearWdsUpgradeMessage(Message, Color) {
	displayWdsUpgradeMessage("", COLOR_DEFAULT);
}

//this is to resize the fixture-jqGrid and gateway-jqGrid  on resize of browser window.
$(window).bind('resize', function() {	
	$("#fixtureTable").setGridWidth($(window).width()-320);
	$("#gatewayTable").setGridWidth($(window).width()-320);
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
function validateImageUpgradeForm() {
	// save user preference for fixture grid
	//saveGridParameters(FIXTURE_GRID);
	var imageName = $('#imgName').val().toLowerCase();
	if(imageName.indexOf("\\") > -1)
	{
		var imagename_array = imageName.split("\\");
		imageName = imagename_array[imagename_array.length - 1];
	}
	var imageNameArray = imageName.split(".");
	var fileName = imageNameArray[0];
	var fileExtension = imageNameArray[imageNameArray.length - 1];
	var fileNameArray = fileName.split("_");
	var version = fileNameArray[0];
	
	$("#fileName").val(imageName);
	if(imageName == ""){
		displayImageUpgradeMessage("<spring:message code='imageUpgrade.message.validation.emptyFileUpload'/>", COLOR_FAILURE);
		return false;
	}
	if((fileExtension != "bin") && (fileExtension != "tar")){
		alert('enLighted application image with only \'.bin\' or \'.tar\' extension is allowed.\nPlease check the selected filename. \n\nFile name should be of one of the following format: \n[version]_su_app.bin \n[version]_gw_app.bin \n[version]_su_firm.bin \n[version]_gw_firm.bin \n[version]_su_pyc.bin \n[version]_su.bin \n[version]_gw.tar  \n[version]_sw.tar');
		return false;
	}
	// 2.0 pattern
	if( (fileName.match(/su$/g) == null) && (fileName.match(/gw$/g) == null) && (fileName.match(/cu$/g) == null) && (fileName.match(/sw$/g) == null) )  {
		// 1.0 pattern
		if ((fileName.indexOf("su_firm") < 0) 
				&& (fileName.indexOf("su_app") < 0) 
				&& (fileName.indexOf("gw_app") < 0) 
				&& (fileName.indexOf("gw_firm") < 0) 
				&& (fileName.indexOf("su_pyc") < 0))
		{
			alert('enLighted application image filename should contain \'su.bin\' or \'gw.tar\' or \'cu.bin\' or \'sw.bin\'  or \'su_firm\' or \'su_app\' or \'gw_app\' or \'gw_firm\' or \'su_pyc\'.\nPlease check the selected filename. \n\nFile name should be of one of the following format: \n[version]_su_app.bin \n[version]_gw_app.bin \n[version]_su_firm.bin \n[version]_gw_firm.bin \n[version]_su_pyc.bin');
			return false;
		}
	}
	if(!IsNumeric(version)){
		alert('enLighted application image filename should contain version number at the start of the filename. \n\nFile name should be of one of the following format: \n[version]_su_app.bin \n[version]_gw_app.bin \n[version]_su_firm.bin \n[version]_gw_firm.bin \n[version]_su_pyc.bin');
		return false;
	}
	displayImageUpgradeMessage("<spring:message code='imageUpgrade.message.uploadFileWaiting'/>", COLOR_BLACK);
}

function IsNumeric(sText)
{
	var ValidChars = "0123456789";
	var IsNumber=true;
	var Char;
	for (i = 0; i < sText.length && IsNumber == true; i++){ 
		Char = sText.charAt(i); 
		if (ValidChars.indexOf(Char) == -1){
			IsNumber = false;
		}
	}
	return IsNumber;
}

function validateUpgradeForm(device) {
		//if (getFloorPlanObj("image_upgrade_floorplan") != null) {
	    	getFloorPlanObj("image_upgrade_floorplan").getSelectedDevices();
	    //}
}

function setSelectedDevices(selFixtures, selGateways, selWds) {
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
	
	clearFixtureUpgradeMessage();
	clearGatewayUpgradeMessage();
	clearWdsUpgradeMessage();

	var fixtureIds = [];
	var gatewayIds = [];
	var wdsIds = [];
	var fxImageFileName = "";
	var gwImageFileName = "";
	var wdsImageFileName = "";

	if (selGwRows.length == 0 && GW_IMG_COMBO.val() == -1 && selFxRows.length == 0 && FX_IMG_COMBO.val() == -1 && selWdsRows.length == 0 && WDS_IMG_COMBO.val() == -1 ) {
		displayGatewayUpgradeMessage("<spring:message code='imageUpgrade.message.validation.selectImage'/>", COLOR_FAILURE);
		return false;
	}
	//Gateway
	if(selGwRows.length > 0 && GW_IMG_COMBO.val()==-1){
		displayGatewayUpgradeMessage("<spring:message code='imageUpgrade.message.validation.selectGatewayImage'/>", COLOR_FAILURE);
		return false;
	}
	if(selGwRows.length == 0 && GW_IMG_COMBO.val()!=-1) {
		displayFixtureUpgradeMessage("<spring:message code='imageUpgrade.message.validation.emptyGateways'/>", COLOR_FAILURE);
		return false;
	}
	
	// Fixtures
	if(selFxRows.length > 0 && FX_IMG_COMBO.val()==-1){
		displayFixtureUpgradeMessage("<spring:message code='imageUpgrade.message.validation.selectFixtureImage'/>", COLOR_FAILURE);
		return false;
	}
	if(selFxRows.length == 0 && FX_IMG_COMBO.val()!=-1) {
		displayFixtureUpgradeMessage("<spring:message code='imageUpgrade.message.validation.emptyFixtures'/>", COLOR_FAILURE);
		return false;
	}

	//WDS
	if(selWdsRows.length > 0 && WDS_IMG_COMBO.val()==-1){
		displayWdsUpgradeMessage("<spring:message code='imageUpgrade.message.validation.selectWdsImage'/>", COLOR_FAILURE);
		return false;
	}
	if(selWdsRows.length == 0 && WDS_IMG_COMBO.val()!=-1) {
		displayWdsUpgradeMessage("<spring:message code='imageUpgrade.message.validation.emptyWds'/>", COLOR_FAILURE);
		return false;
	}
	
	var isVersionOneImage = true;
	var displayVersionMismatchMsg = false;
	
	gwImageFileName = GW_IMG_COMBO.val();
	
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

	fxImageFileName = FX_IMG_COMBO.val();

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
	wdsImageFileName = WDS_IMG_COMBO.val();
	
	var wdsNum = selWdsRows.length;
	for(var i = 0 ; i < wdsNum; i++){
		var wds = selWdsRows[i]; 
		// Make sure that only devices with the appropriate version get selected
		wdsIds.push(wds.id);
	}
	
	// Now after version check see if there are any devices left to upgrade; if not then display message and exit
	if(gatewayIds.length == 0 && GW_IMG_COMBO.val()!=-1) {
//		alert("<spring:message code='imageUpgrade.message.validation.emptyGateways'/>");
		return false;
	}
	
	if(fixtureIds.length == 0 && FX_IMG_COMBO.val()!=-1) {
//		alert("<spring:message code='imageUpgrade.message.validation.emptyFixtures'/>");
		return false;
	}

	if(wdsIds.length == 0 && WDS_IMG_COMBO.val()!=-1) {
//		alert("<spring:message code='imageUpgrade.message.validation.emptyWds'/>");
		return false;
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
			data: "gwImageName="+gwImageFileName+"&gatewayIds="+gatewayIds+"&fxImageName="+fxImageFileName+"&fixtureIds="+fixtureIds+"&wdsImageName="+wdsImageFileName+"&wdsIds="+wdsIds+"&ts="+new Date().getTime(),
			success: function(data){
				if(data!=null){	
					var jsonData = $.parseJSON(data);
					if(jsonData.success == -1) {
						displayGatewayUpgradeMessage('Cannot proceed with image upgrade. There is some critical <a href="' + location.protocol + '//' + location.host	+ '/em_mgmt/home/"' + '><span style="color: blue; text-decoration: underline;">change/process</span></a> going on the server. Please try after some time.', COLOR_FAILURE);
					}else {
						displayGatewayUpgradeMessage('Imageupgrade scheduled.', COLOR_DEFAULT);
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
			FP_data +=  "<param name='flashVars' value='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=IMAGE_UPGRADE&modeid='/>";
			FP_data +=  "<embed id='image_upgrade_floorplan' name='image_upgrade_floorplan' src='"+plotchartmoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " padding='0px'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=IMAGE_UPGRADE&modeid='/>";
			FP_data +=  "</object>";
		} else {
			FP_data = "<embed id='image_upgrade_floorplan' name='image_upgrade_floorplan' src='"+plotchartmoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " padding='0px'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=IMAGE_UPGRADE&modeid='/>";
		}
		
		var tabFP =document.getElementById("tab_fp");
		tabFP.innerHTML = FP_data; 
		// quick fix for the duplicate flash object
		$('div.alt').remove(); 
	}
</script>

<div class="outermostdiv">
	<div class="outerContainer">
		<span><spring:message code="menu.imageupgrade"/></span>		
	</div>
	
	<div class="outerContainer">
		<fieldset>
			<legend><spring:message code="imageUpgrade.label.uploadImage"/></legend>
			<table>
			<tr><td>
			<form action="saveNewImages.ems" id="firmwareupgrade-register" name="firmwareUpgrade" method="post" enctype="multipart/form-data" onsubmit="javascript: return validateImageUpgradeForm();">
				<input type="file" name="upload" id="imgName" />
				<input type="hidden" name="fileName" id="fileName" />
				<input type="submit" name="submit" class="button" value="<spring:message code='imageUpgrade.label.upload'/>" />
				<div class="messageDiv" id="image_upload_message"></div>
			</form></td><td>	
			<select id="gwImageId"></select> 
			<select id="fximageId"></select>
			<select id="wdsimageId"></select>
			<button id='imgUpgradeSubmitBtn' class="button"	onclick='javascript: validateUpgradeForm(3);'>
				<spring:message code='imageUpgrade.label.startupgrade' />
			</button>
			<div class="messageDiv" id="image_upgrade_message"></div></td></tr>
			</table>
		</fieldset>
	</div>
	<!-- 
	<div class="outerContainer">
		<fieldset>
			<legend><spring:message	code="imageUpgrade.label.selectDevicesImages" /></legend>
			<select id="gwImageId"></select> 
			<select id="fximageId"></select>
			<button id='imgUpgradeSubmitBtn' class="button"	onclick='javascript: validateUpgradeForm(3);'>
				<spring:message code='imageUpgrade.label.startupgrade' />
			</button>
			<div class="messageDiv" id="image_upgrade_message"></div>
		</fieldset>
	</div>
	-->
	<div class="outermostdiv">
		<div id="tab_fp" class="pnl_rht"></div>
			<!-- 
		<fieldset id="fldset">
			<legend><spring:message code="imageUpgrade.label.selectDevices"/></legend>
			<button id='imgUpgradeRefreshBtn' class="button" onclick='javascript: refreshList();'>
				<spring:message code='imageUpgrade.label.refresh' />
			</button>
			<div class="imageupg-tab-container" style="width:100%;margin-top:20px;">
				<div id="imageupg-devices-tabs">
					<ul>
						<li><a href="#tab-fixture"><spring:message code="imageUpgrade.label.fixture"/></a></li>
						<li><a href="#tab-gateway"><spring:message code="imageUpgrade.label.gateway"/></a></li>
					</ul>
					<div id="tab-fixture" class="tbldiv">
						<table id="fixtureTable"></table>
						<div id="fixturePagingDiv"></div>
					</div>					
					<div id="tab-gateway" class="tbldiv">
						<table id="gatewayTable"></table>
						<div id="gatewayPagingDiv"></div>
					</div>
				</div>
			</div>
		</fieldset>
			-->
	</div>
</div>


