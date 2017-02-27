<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Switch Details</title>

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

<spring:url value="/services/org/fixture/list/" var="getAvailableFixtureUrl" scope="request" />
<spring:url value="/services/org/fixture/op/dim/abs/" var="setfixturedimmerUrl" scope="request" />
<spring:url value="/services/org/switchfixtures/list/sid/" var="switchfixturelistUrl" scope="request" />
<spring:url value="/services/org/switchfixtures/reassignswitchfixture" var="reassignswitchfixturesUrl" scope="request" />
<spring:url value="/services/org/switchfixtures/delete/" var="deleteswitchfixturesUrl" scope="request" />
<spring:url value="/services/org/scene/list/sid/" var="sceneslistbyswitchidUrl" scope="request" />
<spring:url value="/services/org/scene/list/scenelevel/sid/" var="sceneslevelbysceneidUrl" scope="request" />
<spring:url value="/services/org/scene/savescene" var="savesceneUrl" scope="request" />
<spring:url value="/services/org/scene/savescenelist" var="saveSceneListUrl" scope="request" />
<spring:url value="/services/org/scene/delete/" var="deletesceneUrl" scope="request" />
<spring:url value="/services/org/scene/savescenelevel" var="saveSceneLevelUrl" scope="request" />
<spring:url value="/services/org/scene/updatescenelevel" var="updateSceneLevelUrl" scope="request" />
<spring:url value="/services/org/scene/details/sid/" var="checkDuplicateSceneUrl" scope="request" />
<spring:url value="/devices/switches/saveSwitch.ems" var="saveSwitchUrl" scope="request"/>
<spring:url value="/services/org/switch/details/floor/" var="checkDuplicateSwitchUrl" scope="request"/>
<spring:url value="/services/org/switch/details/area/" var="checkDuplicateAreaSwitchUrl" scope="request"/>
<spring:url value="/services/org/switch/du/updateposition/" var="updateSwitchPositionUrl" scope="request"/>
<spring:url value="/services/org/facilities/" var="getFloorIdFromAreaIDUrl" scope="request"/>


<!-- Utility Functions  -->
<script type="text/javascript">

var basepage = "devices";
$.fn.serializeObject = function()
{
    var o = {};
    var a = this.serializeArray();
    $.each(a, function() {
        if (o[this.name] !== undefined) {
            if (!o[this.name].push) {
                o[this.name] = [o[this.name]];
            }
            o[this.name].push(this.value || '');
        } else {
            o[this.name] = this.value || '';
        }
    });
    return o;
};

function objToString (obj) {
	var str = '';
	for (var p in obj) {
	    if (obj.hasOwnProperty(p)) {
	        str += p + '::' + obj[p] + '\n';
	    }
	}
	return str;
}

function reloadSwitchesFrame(){
	var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("switchesFrame");
	ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src;
}
</script>

<script type="text/javascript">
	var COLOR_FAILURE = "red";
	var COLOR_SUCCESS = "green";
	var COLOR_DEFAULT = "black";
	var MAX_ROW_NUM = 99999;
	//Global variable for accessing current switch id
	var SWITCH_ID = "${switch.id}";
	
	var SWITCH_XAXIS = "${xaxis}";
	var SWITCH_YAXIS = "${yaxis}";
	
	var SWITCH_GRID = $("#switchfixtureTable");
	var SCENE_GRID = $("#scenefixtureTable");
	
	var switch_selIds=null;
	var scene_selIds=null;
	
	//Constants for form validation
	var MAX_CUSTOM_SCENES = 4;
	var ALL_ON_NAME = "All On";
	var ALL_OFF_NAME = "All Off";
	
	//Grid,Combo data variables
	oldSwitchFixtures = [];
	fixturesData = [];
	scenefixturesData = [];
	sceneData = [];
	floorIDforAreaLevelSwitch = null ;
	
	//Form action variables (create|edit)
	switchFormAction = "create";
	sceneFormAction = "create";
	<c:if test="${action == 'edit'}">
		switchFormAction = "edit";
	</c:if>

	$(document).ready(function() {
		switch_selIds=null;
		scene_selIds=null;
		
		//Dialog tab setup
		$("#switch-form-tabs").tabs({
			selected: 0 , 
			create: function(event, ui) {
				//Show tabs after rendering the tab panel
				$("#tabs-scenes").css("visibility", "visible");
				$("#tabs-switch").css("visibility", "visible");
			}
		});
		
		//Switch form setup
		createSwitchPageFixtureGrid();
		loadFloorFixtures();

		//Scene form setup
		if(switchFormAction == "create"){
			$("#save-fixtures-button").attr("disabled", true);
		} else if(switchFormAction == "edit"){
			loadScenesList();
			$("#switch-name-text").html("Edit Switch:");
		}
		
		createScenePageFixtureGrid();
		showSceneForm(false);
		showSceneFixtureTable(false);
		showDimmerSlider(false);
		
		//Create Dimmer Slider
		var defaultSliderValue = 0;//dimmerControl;
		$( "#slider_scene_dimmer" ).slider({
			value: defaultSliderValue,
			min: 0,
			max: 100,
			step: 1,
			change: function( event, ui ) {
				updateSliderValueSpan(ui.value);
			}
		});
		
		$( "#slider_scene_dimmer" ).bind( "slidestop", function(event, ui) {
			updateSliderValueSpan(ui.value);
			setDimmerState(ui.value);
		});
		
		updateSliderValueSpan(defaultSliderValue);
	});
	
	$(window).unload(function(){
		saveGridParameters(SWITCH_GRID);
		saveGridParameters_scene(SCENE_GRID);
	});
	
//All Java Script functions 
	function createSwitchPageFixtureGrid(){
		//Fixture table 
		jQuery("#switchfixtureTable").jqGrid({
			//url : "${getAvailableFixtureUrl}"+treenodetype+"/"+treenodeid+"/1000",
			datatype: "clientSide",
			width : 740, // Math.floor($('body').width() * .58) , 
			height : 250, //Math.floor($(this).height() * .73),
			scrollOffset: 0,
		   	colNames:['id', 'xaxis', 'yaxis', 'Name', 'Dimmer Level'],
		   	colModel:[
				{name:'id', index:'id', hidden:true},
				{name:'xaxis', index:'xaxis', hidden:true},
				{name:'yaxis', index:'yaxis', hidden:true},
		   		{name:'name', index:'name', sorttype:"string", width:"50%"},
		   		{name:'lightlevel', index:'lightlevel', sortable:false, width:"50%"}
		   	],
		   	sortname: 'name',
		   	rowNum: MAX_ROW_NUM,
		    viewrecords: true,
		    sortorder: "desc",		    
		   	multiselect: true,
			toolbar: [true,"bottom"],
		   	caption: "Current Fixtures",
		   	onSortCol: function (index, columnIndex, sortOrder) {
		   		saveGridParameters(SWITCH_GRID);
		    },
		    gridComplete: function(){
		    	fillingGridWithUserSelection(SWITCH_GRID);
		    }
		});
	
	//Add a button in footer panel 
		$("#t_switchfixtureTable").append("<button id='save-fixtures-button' onclick='javascript: setFixtureToSwitch();'><spring:message code='switchForm.button.label.saveFixtures'/></button>");
		$("#t_switchfixtureTable").css("height", "24px");
		$("#t_switchfixtureTable").addClass("ui-widget-header");
	}
	
	//FOR SWITCH
	function fillingGridWithUserSelection(gridName) {
        if(switch_selIds != null && switch_selIds != undefined){
	        for(var i=0;i<switch_selIds.length;i++){
	        	jQuery(gridName).jqGrid('setSelection', switch_selIds[i]);
		   	}
        }
	}
	
	function saveGridParameters(grid) {
	    switch_selIds = jQuery("#switchfixtureTable").getGridParam('selarrrow');
	}
	//--------------------------------------------------------------------
	
	//FOR SCENE
	function fillingGridWithUserSelection_scene(gridName) {
        if(scene_selIds != null && scene_selIds != undefined){
	        for(var i=0;i<scene_selIds.length;i++){
	        	jQuery(gridName).jqGrid('setSelection', scene_selIds[i]);
		   	}
        }
	}
	
	function saveGridParameters_scene(grid) {
	    scene_selIds = jQuery("#scenefixtureTable").getGridParam('selarrrow');
	}
	//--------------------------------------------------------------------
	
	
	function createScenePageFixtureGrid(){
		//Assigned Fixture table in scenes tab
		jQuery("#scenefixtureTable").jqGrid({
			datatype: "local",
			width : 740, //Math.floor($('body').width() * .58) , 
			height : 180, //Math.floor($(this).height() * .73),
			scrollOffset: 0,
		   	colNames:['id', 'xaxis', 'yaxis', 'Name', 'Dimmer Level'],
		   	colModel:[
				{name:'id', index:'id', hidden:true},
				{name:'xaxis', index:'xaxis', hidden:true},
				{name:'yaxis', index:'yaxis', hidden:true},
		   		{name:'name', index:'name', sorttype:"string", width:"50%"},
		   		{name:'lightlevel', index:'lightlevel', sortable:false, width:"50%"}		
		   	],
		   	sortname: 'name',
		    viewrecords: true,
		    sortorder: "desc",		    
		   	multiselect: true,
		   	onSelectRow: function(rowid, status){
				updateDimmerOnFixtureSelection(rowid, status);
	      	},
	      	onSelectAll: function(rowid, status){
				updateDimmerOnFixtureSelection(rowid, false);
	      	},
		   	caption: "Current Fixtures",
		   	onSortCol: function (index, columnIndex, sortOrder) {
		   		saveGridParameters_scene(SCENE_GRID);
		    },
		    gridComplete: function(){
		    	fillingGridWithUserSelection_scene(SCENE_GRID);
		    }
		});
	}	
	
	//load grid with all available fixtures for selected floor
	function loadFloorFixtures(){
		
		$.ajax({
			type: 'GET',
			url: "${getAvailableFixtureUrl}"+treenodetype+"/"+treenodeid+"/1000"+"?ts="+new Date().getTime(),
			data: "",
			success: function(data){
			    if(data != null){
				    fixturesData = data.fixture;
				    var firstFixture;
					if(fixturesData.length != undefined && fixturesData.length > 0){
							
						for(var i=0; i<fixturesData.length ; i++){
							var fixtureJson = fixturesData[i];
							if(fixturesData[i].state == "COMMISSIONED"){
								jQuery("#switchfixtureTable").jqGrid('addRowData', fixtureJson.id , fixtureJson);
							}							
						}
						
						firstFixture = fixturesData[0];
	
					} else {
						
						if(data.fixture.state == "COMMISSIONED"){
							jQuery("#switchfixtureTable").jqGrid('addRowData', data.fixture.id , data.fixture);
						}
						
						firstFixture = data.fixture;
						
					}
					
					if(switchFormAction=="create"){
						$("#buildingId").val(firstFixture.buildingid);
						$("#campusId").val(firstFixture.campusid);
						if(treenodetype=='floor')
						{
							
							$("#floorId").val(treenodeid);
						}
						else 
						{	
							getFloorIdFromAreaID(treenodeid);
							$("#floorId").val(floorIDforAreaLevelSwitch);
							$("#areaId").val(treenodeid);
						}
					}
					
					//Load assigned fixture of selected switch in case of edit
					if(switchFormAction=="edit"){
						loadCurrentFixtures();
					}
					
					markSelectedFixtureOnFloorPlan();
			  	}	
			},
			dataType:"json",
			contentType: "application/json; charset=utf-8",
		});
	}
	
	function getFloorIdFromAreaID(areaID)
	{
		
		$.ajax({
			type: 'GET',
			url: "${getFloorIdFromAreaIDUrl}"+"Area"+"/"+areaID+"?ts="+new Date().getTime(),
			async:false,
			data: "",
			success: function(data){
				floorIDforAreaLevelSwitch = data ;
				
					}
	});
		
	}

	function markSelectedFixtureOnFloorPlan(){
		if(SELECTED_FIXTURES.length > 0){
			basepage = "floorplan";
			$("#save-fixtures-button").attr("disabled", true);
			
			//Mark as selected
			var fixNum = jQuery('#switchfixtureTable').jqGrid('getGridParam', 'records');
			var rowIds = jQuery('#switchfixtureTable').jqGrid('getDataIDs');
		  	for(var row=0; row<SELECTED_FIXTURES.length ; row++){
				var fixtureId = SELECTED_FIXTURES[row].id;
				for(var i=0; i<rowIds.length; i++){
					var fixtureJson = jQuery("#switchfixtureTable").jqGrid('getRowData', rowIds[i]);
					if(fixtureId == fixtureJson.id){
						jQuery("#switchfixtureTable").jqGrid('setSelection', rowIds[i]);

						//Add fixture details in scene's page fixture table
						scenefixturesData.push(fixtureJson);
						jQuery("#scenefixtureTable").jqGrid('addRowData', fixtureId, fixtureJson);
						
						break;
					}
				}
			}
		}
	}
	
//Get current assigned fixtures
	function loadCurrentFixtures() {
			$.ajax({
				type: 'GET',
				url: "${switchfixturelistUrl}"+SWITCH_ID+"?ts="+new Date().getTime(),
				data: "",
				success: function(data){
						if(data != null){
							oldSwitchFixtures = [];
							scenefixturesData = [];
							$("#scenefixtureTable").jqGrid("clearGridData");
							$("#switchfixtureTable").jqGrid("resetSelection");
							
							var xml=data.getElementsByTagName("switchFixtures");
							for (var i=0; i<xml.length; i++) {
								var id = xml[i].getElementsByTagName("id")[0].childNodes[0].nodeValue;
								var fixtureId = xml[i].getElementsByTagName("fixtureid")[0].childNodes[0].nodeValue;
							  	oldSwitchFixtures.push({"id": id, "fixtureid": fixtureId});

							  	if(fixturesData != null){
							  		
							  	//Mark current fixture as Selected in Grid
								  	if(fixturesData.length != undefined && fixturesData.length > 0){
								  		
								  		for(var row=0; row<fixturesData.length ; row++){
											var fixtureJson = fixturesData[row];
											if(fixtureId == fixtureJson.id){
												jQuery("#switchfixtureTable").jqGrid('setSelection', fixtureJson.id);

												//Add fixtue details in scene's page fixture table
												scenefixturesData.push(fixtureJson);
												jQuery("#scenefixtureTable").jqGrid('addRowData', fixtureId, fixtureJson);

												break;
											}
										}
								  		
								  	}else{
								  		
								  		if(fixtureId == fixturesData.id){
								  			
								  			jQuery("#switchfixtureTable").jqGrid('setSelection', fixturesData.id);
									  		
									  		//Add fixtue details in scene's page fixture table
									  		scenefixturesData.push(fixturesData);
									  		jQuery("#scenefixtureTable").jqGrid('addRowData', fixturesData.id, fixturesData);
								  		}
								  		
								  	}
							  		
							  	}
							}
						}
					},
				dataType:"xml",
				contentType: "application/xml; charset=utf-8",
			});
	}
	
//Load scenes combo box
	function loadScenesList(){
		$.ajax({
			type: 'GET',
			url: "${sceneslistbyswitchidUrl}"+SWITCH_ID+"?ts="+new Date().getTime(),
			data: "",
			success: function(data){
					if(data != null){
						sceneData = data.scene;
						if(sceneData.length > 0) {
							$("#scene-combo").empty();
							$.each(sceneData, function(i, sceneJson) {
								$('#scene-combo').append($('<option></option>').val(sceneJson.id).html(sceneJson.name));
							});
						}
					}
				},
			dataType:"json",
			contentType: "application/json; charset=utf-8",
		});
	}
	
	function saveSwitchBtnHandler(){
		clearSwitchLabelMessage();
		var switchName = $.trim($("#switchName").val());
		if (switchName == "") {
			displaySwitchLabelMessage("<spring:message code='switchForm.message.validation.name'/>", COLOR_FAILURE);
		}else if (jQuery("#switchfixtureTable").getGridParam("records") == 0) {
			displaySwitchLabelMessage("<spring:message code='switchForm.message.validation.fixtureCount'/>", COLOR_FAILURE);
		} else {
			checkForDuplicateSwitchName(switchName, treenodetype , treenodeid);
		} 
		return false;
	}
	
	function checkForDuplicateSwitchName(switchName, treenodetype , treenodeid){
		if(treenodetype=='floor')
			{
		$.ajax({
			type: 'POST',
			url: "${checkDuplicateSwitchUrl}"+treenodeid+"/"+switchName+"?ts="+new Date().getTime(),
			data: "",
			success: function(data){
				if(data == null){
					saveSwitch();
				}else{
					if(SWITCH_ID != data.id){
						displaySwitchLabelMessage("<spring:message code='switchForm.message.validation.duplicateName'/>", COLOR_FAILURE);
					}
				}
			},
			dataType:"json",
			contentType: "application/json; charset=utf-8"
		});
			}
		else
			{
			$.ajax({
				type: 'POST',
				url: "${checkDuplicateAreaSwitchUrl}"+treenodeid+"/"+switchName+"?ts="+new Date().getTime(),
				data: "",
				success: function(data){
					if(data == null){
						saveSwitch();
					}else{
						if(SWITCH_ID != data.id){
							displaySwitchLabelMessage("<spring:message code='switchForm.message.validation.duplicateName.area'/>", COLOR_FAILURE);
						}
					}
				},
				dataType:"json",
				contentType: "application/json; charset=utf-8"
			});
			}
	}

	function saveSwitch(){
 		$.post(
			"${saveSwitchUrl}"+"?ts="+new Date().getTime(),
			$("#switch-form").serialize(),
			function(data){
				if(data != null){
					displaySwitchLabelMessage("<spring:message code='switchForm.message.success'/>", COLOR_SUCCESS);
					$("#save-fixtures-button").removeAttr("disabled");
					
					var xml=data.getElementsByTagName("switch");
					for (var i=0; i<xml.length; i++) {
						var switchid = xml[i].getElementsByTagName("id")[0].childNodes[0].nodeValue;
						SWITCH_ID = switchid;//Update global variable
					}

					if(switchFormAction=="create"){
						//Create Default Scenes
						saveDefaultScene();
						$("#id").val(SWITCH_ID);
						switchFormAction="edit";
					}
					
					if(basepage == "devices") {
						reloadSwitchesFrame();
					}
				} else { //Failure
					displaySwitchLabelMessage("<spring:message code='switchForm.message.failed'/>", COLOR_FAILURE);
				}
			}
		);
	}
	
	function setFixtureToSwitch(){	
		$("#save-fixtures-button").attr("disabled", true);	
		var selIds = jQuery("#switchfixtureTable").getGridParam('selarrrow');
		var fixNum = selIds.length;
		if(fixNum == 0) {
			$("#save-fixtures-button").removeAttr("disabled");
			displaySwitchLabelMessage("Please select atleast one fixture.", COLOR_FAILURE);
			return;
		}
		displaySwitchLabelMessage("Processing...", COLOR_DEFAULT);
		var switchFixtureXML = "";
		var sceneLevelXML = "";
		for(var i = 0 ; i < fixNum; i++){
			var fixture = jQuery("#switchfixtureTable").jqGrid('getRowData', selIds[i]); 
			switchFixtureXML += getSwitchfixturesMappingXML(fixture.id);
			//collect scene level data for new fixture
			sceneLevelXML += getSceneLevelXMLforNewFixture(fixture);
		}

		if(switchFixtureXML.length > 0){
			saveSwitchfixturesMapping("<switchFixturess>"+switchFixtureXML+"</switchFixturess>");	
		}
		
		//Save scenelevel for new fixtures
		if(sceneLevelXML.length > 0){
			saveSceneLevel(getSceneLevelXML_List(sceneLevelXML));
		}
			
		if(oldSwitchFixtures.length == 0) {
			updateSwitchPosition(SWITCH_XAXIS, SWITCH_YAXIS);
		}
		
		for (var i = 0 ; i < fixNum; i++) {
			var fixture = jQuery("#switchfixtureTable").jqGrid('getRowData', selIds[i]); 
			for (var j=0; j < oldSwitchFixtures.length; j++) {
				if (oldSwitchFixtures[j].fixtureid == fixture.id) {
					oldSwitchFixtures.splice(j, 1);
				}
			}
		}
		
		//Delete old switchFixture mapping from scenes table
		if(oldSwitchFixtures.length > 0){
			for (var j=0; j < oldSwitchFixtures.length; j++) {
				var fixtureID = oldSwitchFixtures[j].fixtureid;
				jQuery("#scenefixtureTable").jqGrid('delRowData', fixtureID);
			}
		}
		
		showSceneForm(false);
		showSceneFixtureTable(false);
		showDimmerSlider(false);
		
		$("#save-fixtures-button").removeAttr("disabled");
		return false;
	}
	
	function getSwitchfixturesMappingXML(fixtureId){
		return "<switchFixtures><id></id><fixtureid>"+fixtureId+"</fixtureid><switchid>"+SWITCH_ID+"</switchid></switchFixtures>";
	}
	function saveSwitchfixturesMapping(sfixtureXML){
		var post_data = sfixtureXML;
		$.ajax({
			type: 'POST',
			url: "${reassignswitchfixturesUrl}"+"?ts="+new Date().getTime(),
			data: post_data,
			success: function(data){
				if(data!=null){
					loadCurrentFixtures();
					displaySwitchLabelMessage("<spring:message code='switchForm.message.switchfixture.success'/>", COLOR_SUCCESS);
				}
			},
			error: function() {
				displaySwitchLabelMessage("Internal Error", COLOR_FAILURE);
				$("#save-fixtures-button").removeAttr("disabled");
			},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8"
		});
	}
	
	function updateSwitchPosition(xaxis, yaxis){
		var post_data = "<switches><switch><id>"+SWITCH_ID+"</id><xaxis>"+Math.ceil(xaxis)+"</xaxis><yaxis>"+Math.ceil(yaxis)+"</yaxis></switch></switches>";
		$.ajax({
			type: 'POST',
			url: "${updateSwitchPositionUrl}"+"?ts="+new Date().getTime(),
			data: post_data,
			success: function(data){
				if(data!=null){
					
				}
			},
			error: function() {
				displaySwitchLabelMessage("Internal Error", COLOR_FAILURE);
				$("#save-fixtures-button").removeAttr("disabled");
			},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8"
		});
	}
	
	function displaySwitchLabelMessage(Message, Color) {
		$("#switch_form_message").html(Message);
		$("#switch_form_message").css("color", Color);

	}
	function clearSwitchLabelMessage(Message, Color) {
		displaySwitchLabelMessage("", COLOR_DEFAULT);
	}
	
	function setSliderValue(value){
		$("#slider_scene_dimmer").slider("value", value);
	}
	
	function updateSliderValueSpan(value){
		$("#slider_value").html(value+"%");
		$("#slider_value").css("left", (value*7 - (value<10?15:20)) + "px");
	}
	
	function enableSlider(isEnable){
		if(isEnable){
			$('#slider_scene_dimmer').slider('enable');
		} else {
			$('#slider_scene_dimmer').slider('disable');
		}
	}
	
	function enableDisableDimmerSlider(){
		var selIds = jQuery("#scenefixtureTable").getGridParam('selarrrow');
		if(selIds.length==0){
			$('#slider_scene_dimmer').slider('disable');
		} else {
			$('#slider_scene_dimmer').slider('enable');

			var currSceneName = $("#scene-combo").find('option:selected').text(); 
			if((sceneFormAction=="edit") && (currSceneName == ALL_ON_NAME || currSceneName == ALL_OFF_NAME)){
				$('#slider_scene_dimmer').slider('disable');
			}
		}
	}
	
	function setDimmerState(value){
		var sceneId = $("#scene-id").val();
		var selIds = jQuery("#scenefixtureTable").getGridParam('selarrrow');
		if(selIds.length>0) {
			var fixtureIdsXML = "";
			var sceneLevelXML = "";
			for( var i=0; i<selIds.length; i++){
				var fixture = jQuery("#scenefixtureTable").jqGrid('getRowData', selIds[i]); 
				jQuery("#scenefixtureTable").jqGrid('setCell', selIds[i], 'lightlevel', value);
				fixtureIdsXML += "<fixture><id>"+fixture.id+"</id></fixture>";
				
				sceneLevelXML += getSceneLevelXML("", SWITCH_ID, sceneId, fixture.id, value);
			}
			updateSceneLevel(getSceneLevelXML_List(sceneLevelXML));
			
			$.ajax({
				type: 'POST',
				url: "${setfixturedimmerUrl}"+value+"/"+60+"?ts="+new Date().getTime(),
				data: "<fixtures>"+fixtureIdsXML+"</fixtures>",
				success: function(data){
							//alert("done");
	 					},
				dataType:"xml",
				contentType: "application/xml; charset=utf-8"
			});
		}
		saveSceneHandler();
	}
	
	function updateDimmerOnFixtureSelection(rowid, status) {
		if(status){ //Select
			var fixture = jQuery("#scenefixtureTable").jqGrid('getRowData', rowid); 
			setSliderValue(fixture.lightlevel);
		} else { //Deselect
			var selIds = jQuery("#scenefixtureTable").getGridParam('selarrrow');
			if(selIds.length > 0){
				var fixture = jQuery("#scenefixtureTable").jqGrid('getRowData', selIds[0]); 
				setSliderValue(fixture.lightlevel);
			} else {
				setSliderValue(0);
			}
		}
		enableDisableDimmerSlider();
	}
	
//Scene Configuration
	function saveSceneHandler(){
		clearAllLabelMessage();
		
		var sceneName = $.trim($("#scene-name").val());
		if (sceneName == "") {
			displaySceneLabelMessage2("<spring:message code='sceneForm.message.validation.name'/>", COLOR_FAILURE);
		} else {
			checkForDuplicateSceneName(sceneName);
		}
		return false;
	}
	

	function checkForDuplicateSceneName(sceneName){
		$.ajax({
			type: 'POST',
			url: "${checkDuplicateSceneUrl}"+SWITCH_ID+"/"+sceneName+"?ts="+new Date().getTime(),
			data: "",
			success: function(data){
				if(data == null){
					var sceneName = $.trim($("#scene-name").val());
					var sceneId= $("#scene-id").val();
					saveScene(sceneId, sceneName);
				}else{
					var currSceneId= $("#scene-id").val();
					if(currSceneId != data.id){
						displaySceneLabelMessage2("<spring:message code='sceneForm.message.validation.duplicateName'/>", COLOR_FAILURE);
					}
				}
			},
			dataType:"json",
			contentType: "application/json; charset=utf-8"
		});
	}
	
	function saveScene(sceneId, sceneName){		
		var post_data = "<scene><id>"+sceneId+"</id><name>"+sceneName+"</name><switchid>"+SWITCH_ID+"</switchid></scene>";
		$.ajax({
			type: 'POST',
			url: "${savesceneUrl}"+"?ts="+new Date().getTime(),
			data: post_data,
			success: function(data){
				if(data!=null){
					loadScenesList();//Reload scenes combo
					
					$("#scene-save-button").hide();
					showSceneForm(true);
					showSceneFixtureTable(true);
					showDimmerSlider(true);
					enableDisableDimmerSlider();
					
					if(sceneFormAction=="create"){
						var xml=data.getElementsByTagName("scene");
						for (var i=0; i<xml.length; i++) {
							var sceneId = xml[i].getElementsByTagName("id")[0].childNodes[0].nodeValue;
							getSceneLevel(sceneId);//Reload fixtures dimmer level based on scene
							
							$("#scene-id").val(sceneId);
							
							//save sceneLevels for all fixtures
							var fixNum = jQuery('#scenefixtureTable').jqGrid('getGridParam', 'records');
							var rowIds = jQuery('#scenefixtureTable').jqGrid('getDataIDs');
							if(fixNum > 0){
								var sceneLevelXML = "";
								for(var i=0; i<rowIds.length; i++){
									var fixture = jQuery("#switchfixtureTable").jqGrid('getRowData', rowIds[i]);
									sceneLevelXML += getSceneLevelXML("", SWITCH_ID, sceneId, fixture.id, fixture.lightlevel);
								}
								updateSceneLevel(getSceneLevelXML_List(sceneLevelXML));
							}
						}
					}
				}
			},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8"
		});
	}
	
	function saveDefaultScene(){
		var post_data = "<scenes>"+
							"<scene><id></id><name>"+ALL_ON_NAME+"</name><switchid>"+SWITCH_ID+"</switchid></scene>"+
							"<scene><id></id><name>"+ALL_OFF_NAME+"</name><switchid>"+SWITCH_ID+"</switchid></scene>"+
						"</scenes>";
						
		$.ajax({
			type: 'POST',
			url: "${saveSceneListUrl}"+"?ts="+new Date().getTime(),
			data: post_data,
			success: function(data){
				loadScenesList();
				if(data != null){
					var fixNum = jQuery('#scenefixtureTable').jqGrid('getGridParam', 'records');
					var rowIds = jQuery('#scenefixtureTable').jqGrid('getDataIDs');
					
					//save sceneLevels for default scene and all attached fixtures
					if(fixNum > 0){
						var sceneLevelXML = "";
						var xml = data.getElementsByTagName("scene");
						
						for (var i=0; i<xml.length; i++) {
							var sceneId = xml[i].getElementsByTagName("id")[0].childNodes[0].nodeValue;
							var sceneName = xml[i].getElementsByTagName("name")[0].childNodes[0].nodeValue;
							
							var dimmerLevel = 0;
							if(sceneName == ALL_ON_NAME){
								dimmerLevel = 100;
							}
							
							for(var i=0; i<rowIds.length; i++){
								var fixture = jQuery("#switchfixtureTable").jqGrid('getRowData', rowIds[i]);
								sceneLevelXML += getSceneLevelXML("", SWITCH_ID, sceneId, fixture.id, dimmerLevel);
							}
						}
						
						saveSceneLevel(getSceneLevelXML_List(sceneLevelXML));
					}
				}
			},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8"
		});
	}
	
	function getSceneLevelXMLforNewFixture(fixture){
		//collect scene level data for new fixture
		var sLevelXML = "";
		if(sceneData.length > 0) {
			$.each(sceneData, function(i, scene) {
				var dimmerLevel = 0;
				if(scene.name == ALL_ON_NAME){
					dimmerLevel = 100;
				} else if(scene.name == ALL_OFF_NAME){
					dimmerLevel = 0;
				} else {
					dimmerLevel = fixture.lightlevel;
				}
				sLevelXML += getSceneLevelXML("", SWITCH_ID, scene.id, fixture.id, dimmerLevel);
			});
		}
		return sLevelXML;
	}
	
	function getSceneLevelXML(id, switchId, sceneId, fixtureId, ligthLevel){
		var levelXML = "<sceneLevel>"+
				"<id>"+ id +"</id>"+
				"<switchid>"+ switchId +"</switchid>"+
				"<sceneid>"+ sceneId +"</sceneid>"+
				"<fixtureid>"+ fixtureId +"</fixtureid>"+
				"<lightlevel>"+ ligthLevel +"</lightlevel>"+
			"</sceneLevel>";
		return levelXML;
	}
	
	function getSceneLevelXML_List(sceneLevelXML){
		return "<sceneLevels>" + sceneLevelXML + "</sceneLevels>";
	}
	
	function saveSceneLevel(sceneLevelXML){
		$.ajax({
			type: 'POST',
			url: "${saveSceneLevelUrl}"+ "/" + SWITCH_ID + "?ts="+new Date().getTime(),
			data: sceneLevelXML,
			success: function(data){
				//
			},
			error: function() {
				displaySwitchLabelMessage("Internal Error", COLOR_FAILURE);
				$("#save-fixtures-button").removeAttr("disabled");
			},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8"
		});				
	}
	
	function updateSceneLevel(sceneLevelXML){
		$.ajax({
			type: 'POST',
			url: "${updateSceneLevelUrl}"+ "?ts="+new Date().getTime(),
			data: sceneLevelXML,
			success: function(data){
				//
			},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8"
		});				
	}
	
	function displaySceneLabelMessage1(Message, Color) {
		$("#scene_form_message1").html(Message);
		$("#scene_form_message1").css("color", Color);

	}
	function displaySceneLabelMessage2(Message, Color) {
		$("#scene_form_message2").html(Message);
		$("#scene_form_message2").css("color", Color);

	}
	function clearSceneLabelMessage() {
		displaySceneLabelMessage2("", COLOR_DEFAULT);
	}
	
	function clearAllLabelMessage() {
		displaySwitchLabelMessage("", COLOR_DEFAULT);
		displaySceneLabelMessage1("", COLOR_DEFAULT);
		displaySceneLabelMessage2("", COLOR_DEFAULT);
	}
	
	function showSceneForm(isVisible){
		if(isVisible){
			$("#scene-form-wrapper").css("visibility", "visible");
		} else {
			$("#scene-form-wrapper").css("visibility", "hidden");
		}
	}
	
	function showDimmerSlider(isVisible){
		if(isVisible){
			$("#dimmer-slider-wrapper").css("visibility", "visible");
		} else {
			$("#dimmer-slider-wrapper").css("visibility", "hidden");
		}
	}
	
	function showSceneFixtureTable(isVisible){
		if(isVisible){
			$("#scene-fixture-table-wrapper").css("visibility", "visible");
		} else {
			$("#scene-fixture-table-wrapper").css("visibility", "hidden");
		}
	}
	
	function createScene(){
		clearAllLabelMessage();
		if(sceneData.length >= (MAX_CUSTOM_SCENES+2)){
			displaySceneLabelMessage1("<spring:message code='sceneForm.message.validation.maxCustomScenes_1'/> "+MAX_CUSTOM_SCENES+" <spring:message code='sceneForm.message.validation.maxCustomScenes_2'/>", COLOR_FAILURE);
			return false;
		}
		
		$("#scene-save-button").show();
		sceneFormAction = "create";
		$("#scene-name").val("");
		$("#scene-id").val("");
		
		showSceneForm(true);
		showSceneFixtureTable(false);
		showDimmerSlider(false);
		enableSlider(false);
		
		return false;
	}
	
	function editScene(){
		clearAllLabelMessage();
		if ($("#scene-combo").val() == null) {
			displaySceneLabelMessage1("<spring:message code='sceneForm.message.validation.selectToEdit'/>", COLOR_FAILURE);
			return false;
		}
		
		$("#scene-save-button").hide();
		sceneFormAction = "edit";
		$("#scene-name").val($("#scene-combo").find('option:selected').text());
		$("#scene-id").val($("#scene-combo").val());
		
		showSceneForm(true);
		showSceneFixtureTable(true);
		showDimmerSlider(true);
		enableDisableDimmerSlider();

		getSceneLevel($("#scene-combo").val());
		return false;
	}
	
	function getSceneLevel(sceneId){
		$.ajax({
			type: 'GET',
			url: "${sceneslevelbysceneidUrl}"+sceneId+"?ts="+new Date().getTime(),
			data: "",
			success: function(data){
					if(data != null){
						var xml=data.getElementsByTagName("sceneLevel");
						for (var i=0; i<xml.length; i++) {
							var fixtureId = xml[i].getElementsByTagName("fixtureid")[0].childNodes[0].nodeValue;
							var lightLevel = xml[i].getElementsByTagName("lightlevel")[0].childNodes[0].nodeValue;
							
							//Update dimmer level in fixture Grid
						  	for(var row=0; row<scenefixturesData.length ; row++){
								var fixtureJson = scenefixturesData[row];
								if(fixtureId == fixtureJson.id){
									jQuery("#scenefixtureTable").jqGrid('setCell',fixtureId, "lightlevel", lightLevel);
									break;
								}
							}
							
						  	var selIds = jQuery("#scenefixtureTable").getGridParam('selarrrow');
							if(selIds.length > 0){
								var fixture = jQuery("#scenefixtureTable").jqGrid('getRowData', selIds[0]); 
								setSliderValue(fixture.lightlevel);
							}
						}
					}
				},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8",
		});
	}
	
	function removeScene(){
		clearAllLabelMessage();
		var currSceneName = $("#scene-combo").find('option:selected').text(); 

		if (currSceneName == "" || currSceneName == null) {
			displaySceneLabelMessage1("<spring:message code='sceneForm.message.validation.selectToRemove'/>", COLOR_FAILURE);
			return false;
		}
		
		if (currSceneName == ALL_ON_NAME || currSceneName == ALL_OFF_NAME){
			displaySceneLabelMessage1("<spring:message code='sceneForm.message.validation.deleteDefaultScene'/>", COLOR_FAILURE);
		} else {
			var proceed = confirm("<spring:message code='sceneForm.message.validation.deleteConfirmation'/>: "+currSceneName+"?");
			if(proceed==true) {
				$.ajax({
					url: "${deletesceneUrl}"+$("#scene-combo").val()+"?ts="+new Date().getTime(),
					success: function(data){
						loadScenesList(); //Reload scenes combo
					}
				});
		 	}
		}
		
		showSceneForm(false);
		showSceneFixtureTable(false);
		showDimmerSlider(false);
		
		return false;
	}
	
	function sceneSelectionChange(){
		clearAllLabelMessage();
		showSceneForm(false);
		showSceneFixtureTable(false);
		showDimmerSlider(false);
		
		enableDisableDimmerSlider();
		
		// 		alert($("#scene-combo").find('option:selected').text());
	}
	
</script>

<style>
	#switch-form-tabs .tab-container{padding:10px !important;}
	
 	#switch-form-tabs div.fieldWrapper{clear:both; height:24px;}
 	#switch-form-tabs div.fieldLabel{float:left; height: 100%; width:15%; font-weight:bold;}
 	#switch-form-tabs div.fieldValue{float:left; height: 100%; width:20%;}
 	#switch-form-tabs div.fieldButton{float:left; height: 100%; width:60%;  padding-left:10px;}
 	#switch-form-tabs button{height: 24px; padding: 0 10px;}
 	#switch-form-tabs button span{padding: 0;}
</style>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
	
	/*Fix for missing border of JQuery Slider panel */
 	div#dimmer-slider-wrapper .ui-widget-content {border: 1px solid #888888 !important;}
 	
 	#switch-form-tabs tr.ui-state-highlight{background-color: white !important; color: black !important;}
 	
 	#switch-form-tabs a.ui-jqgrid-titlebar-close{display: none;}
 	#switch-form-tabs span.ui-jqgrid-title{font-weight:bold; font-size:1.1em;}
</style>

</head>
<body>
<div id="switch-form-tabs">
	<ul>
		<li><a href="#tabs-switch">1. Switch Configuration</a></li>
		<li><a href="#tabs-scenes">2. Scenes Configuration</a></li>
	</ul>
	
<!-- Switch Page -->
	<div id="tabs-switch" class="tab-container" style="visibility: hidden;"> <!-- Keep it hidden as default and Show it after rendering the tab panel -->
		<spring:url value="/switches/saveSwitch.ems" var="switchActionURL" scope="request"/>
		<form:form id="switch-form" commandName="switch" method="post" action="${switchActionURL}" onsubmit="return false;">  
			<form:hidden id="id" name="id" path="id"/>
			<form:hidden id="floorId" name="floorId" path="floorId"/>
			<form:hidden id="areaId" name="areaId" path="areaId"/>
			<form:hidden id="buildingId" name="buildingId" path="buildingId"/>
			<form:hidden id="campusId" name="campusId" path="campusId"/>
			<form:hidden id="xaxis" name="xaxis" path="xaxis"/>
			<form:hidden id="yaxis" name="yaxis" path="yaxis"/>
			
			<div class="fieldWrapper">
				<div  id="switch-name-text" class="fieldLabel"><spring:message code="switchForm.label.name"/>:</div>
				<div class="fieldValue"><form:input id="switchName" name="name" path="name" style="width:100%; height:100%;"/></div>
				<div class="fieldButton">&nbsp;&nbsp;
					<button id="switchSaveBtn" onclick="javascript: return saveSwitchBtnHandler();" style="float:left;"><spring:message code="switchForm.button.label.save"/></button>
					<div id="switch_form_message" style="font-size: 14px; font-weight: bold; padding: 5px 0 0 10px; float:left;"></div>
				</div>
			</div>
		</form:form><br/>
		
		<div id="switch-fixture-table-wrapper">
			<table id="switchfixtureTable"></table>
		</div>
	</div>
	
	
<!-- Scene Page	 -->
	<div id="tabs-scenes" class="tab-container" style="visibility: hidden;"> <!-- Keep it hidden as default and Show it after rendering the tab panel -->
		<div id="scene-list-wrapper" class="fieldWrapper">
			<div class="fieldLabel"><spring:message code="sceneForm.label.comboname"/>:</div>
			<div class="fieldValue">
				<select id="scene-combo" name="scene" style="width:100%; height:100%;" onchange="javascript: sceneSelectionChange();">
				</select>
			</div>
			
			<div class="fieldButton">
				<button onclick="javascript: editScene();" ><spring:message code="sceneForm.button.label.edit"/></button>&nbsp;
				<button onclick="javascript: createScene();" ><spring:message code="sceneForm.button.label.add"/></button>&nbsp;
				<button onclick="javascript: removeScene();" ><spring:message code="sceneForm.button.label.remove"/></button>
				<div id="scene_form_message1" style="font-size: 14px; font-weight: bold; padding: 5px 0 0 10px; float:left;"></div>
			</div>
		</div>
		
		<div style="height:10px"></div>
		
		<div id="scene-form-wrapper" class="fieldWrapper">
		<spring:url value="/scenes/saveScene.ems" var="sceneActionURL" scope="request"/>
			<form id="scene-form" style="height: 100%;" method="post" action="${sceneActionURL}" onsubmit="return false;">
				<input type="hidden" id="scene-id" name="id" ></input>
				<div class="fieldLabel"><spring:message code="sceneForm.label.scenename"/>:</div>
				<div class="fieldValue"><input type="text" id="scene-name" name="name" style="width:100%; height:100%;"></input></div>
				<div class="fieldButton">
					<button id="scene-save-button" onclick="javascript: saveSceneHandler();" style="float:left;"><spring:message code='switchForm.button.label.save'/></button>
					<div id="scene_form_message2" style="font-size: 14px; font-weight: bold; padding: 5px 0 0 10px; float:left;"></div>
				</div>
			</form>
		</div>
		
		<div style="height:10px"></div>
		
		<div id="dimmer-slider-wrapper" style="width:700px; padding: 2px 20px;">
			<div id="slider_marker">&nbsp;
				<span style="float:left;color:#AAA;">0%</span>
				<span style="float:left;color:#AAA;margin-left:46.5%;">50%</span>
				<span style="float:right;color:#AAA;">100%</span>
			</div>
			<div id="slider_scene_dimmer"></div>
			<div id="slider_info" style="padding-top: 7px;">&nbsp;
				<span id="slider_value" style="position:relative; background-color: #DDDDDD; border: thin solid #AAAAAA; padding: 0 2px;">0%</span>
			</div>
		</div>
		
		<div style="height:10px"></div>
		
		<div id="scene-fixture-table-wrapper">
			<table id="scenefixtureTable"></table>
		</div>
	</div>
</div>
</body>
</html>