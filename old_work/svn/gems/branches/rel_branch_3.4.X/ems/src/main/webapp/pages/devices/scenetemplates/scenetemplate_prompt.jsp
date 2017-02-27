<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<spring:url value="/services/org/scenelightlevels/add" var="saveLightLevelURl" scope="request" />
<spring:url value="/services/org/scenetemplate/list/" var="checkDuplicateSceneTemplateUrl" scope="request" />
<spring:url value="/services/org/scenelightlevels/list/" var="getSceneLightLevelBySceneTemplateIdURL" scope="request" />
<spring:url value="/services/org/scenetemplate/edit/" var="editsceneTemplateName" scope="request" />

<spring:url	value="/services/org/scenelightlevels/delete/" var="deleteSceneLightlevelUrl" scope="request" />
	
<style>
  #scene-level-table.ui-widget-content {border: 1px solid #888888 !important;}
  
  .disableAddScenehButton
	{
		background: #CCCCCC !important;
		border-color :#CCCCCC !important;
		color:#FFFFFF; font-size:13px; clear: both; float: left; margin: -3px; width:120px;border:0px;font-weight:bold;
	}
	.enableAddSceneButton
	{
		color:#FFFFFF;background-color:#5a5a5a; font-size:13px; clear: both; float: left; margin: -3px; width:120px;border:0px;font-weight:bold;
	}
	
</style>	

<html>
<head>
<script type="text/javascript">
	var COLOR_FAILURE = "red";
	var COLOR_SUCCESS = "green";
	var COLOR_DEFAULT = "black";
	
	var sceneTemplateId=0;
	

	$(document).ready(function() {
		sceneTemplateId ="${scenetemplateId}";
		var modeType="${modeType}";
		var scenetemplatename= "${scenetemplatename}";
		if(modeType=='edit')
		{
			$('#sceneTemplateName').val(scenetemplatename);
			$('#addSceneLevelBtn').removeClass("disableAddScenehButton");	
			$('#addSceneLevelBtn').addClass("enableAddSceneButton");	
		}else
		{
			$('#addSceneLevelBtn').addClass("disableAddScenehButton");	
		}
		createSceneLevelGrid(sceneTemplateId);		
		
		$('#addSceneLevelBtn').click(function(){startValidation();});
		
	});
	
	function createSceneLevelGrid(tsceneTemplateID){		
		jQuery("#scene-level-table").jqGrid({
			url: "${getSceneLightLevelBySceneTemplateIdURL}"+tsceneTemplateID+"?ts="+new Date().getTime(),
			datatype: "json",
			mtype: "GET",
			autowidth: false,	
			width:420,
			height:225,
			scrollOffset: 0,
			hoverrows: false,
			forceFit: true,
		   	colNames:['id', 'Scene Name', 'Light Level', 'Order', 'Action'],
		   	colModel:[   
                {name:'id', index:'id', hidden:true},
	  	   		{name:'name', index:'name', width:"30%"},
		   		{name:'lightlevel', index:'lightlevel', sortable:false,width:"30%"},
		   		{name:'sceneOrder', index:'sceneOrder', sortable:false, width:"10%"},
		   		{name:'Action', index:'Action', sortable:false,width:"30%",formatter: editSceneTemplateFormatter}
		   	],
		    jsonReader: { 
		           root:"scenelightleveltemplates", 
		           repeatitems:false,
		           id : "id"
		       },
		   	cmTemplate: {align: 'center', editable: true},		   	
		    viewrecords: true,	
		    gridComplete: function(){
		    	ModifyGridDefaultStyles();
		    },
		    sortname: 'SceneName',
		    sortorder: "desc",
		   	loadComplete: function(data) {
		    	   if (data != null){
		    		   if (data.scenelightleveltemplates != undefined) {
					   		if (data.scenelightleveltemplates.length == undefined) {
					   			// Hack: Currently, JSON serialization via jersey treats single item differently
					   			jQuery("#scene-level-table").jqGrid('addRowData', 0, data.scenelightleveltemplates);
					   		}
		    		   }
		    	   }else
	    		   {
		    		   $('#scene-level-table').jqGrid('clearGridData');
	    		   }
		    	   
		    	   ModifyGridDefaultStyles();
		   }
		});
	}
		
	function ModifyGridDefaultStyles() { 
		$('#' + "scene-level-table" + ' tr').removeClass("ui-widget-content");
		$('#' + "scene-level-table" + ' tr:nth-child(even)').addClass("evenTableRow");
		$('#' + "scene-level-table" + ' tr:nth-child(odd)').addClass("oddTableRow");
	}
	function editSceneTemplateFormatter(cellvalue, options, rowObject){
		var source = "";
		var mode='edit';
		source = "<button onclick=\"javascript: parent.parent.parent.showSceneLightLevelPrompt(" + rowObject.id + ",'" + mode + "');\">Edit</button> <button onclick=\"javascript:onDeleteSceneLightLevel(" + rowObject.id + ");\">Delete</button>";
		return source;
	}	
	function startValidation() {
		
		clearSceneTemplatePromptMessage();
		
		var gridRowCount = jQuery("#scene-level-table").jqGrid('getGridParam', 'records');
		if(gridRowCount>=6){
			//$("#addSceneLevelBtn").attr("disabled", true);
			displaySceneTemplatePromptMessage("Maximum 6 scenes can be created per scene template", COLOR_FAILURE);
			return;
		}

		if(sceneTemplateId<0)
		{
			displaySceneTemplatePromptMessage("Please create scene template first then add scenes", COLOR_FAILURE);
			return;
		}else
		{
			parent.parent.parent.showSceneLightLevelPrompt(sceneTemplateId,'new');
		}		
	}
	function onDeleteSceneLightLevel(rowId){
		if(confirm("Are you sure you want to delete the scene?") == true)
		{
			$.ajax({
		 		type: 'POST',
		 		url: "${deleteSceneLightlevelUrl}"+rowId+"?ts="+new Date().getTime(),
		 		dataType : "json",
		 		success: function(data){
				if(data.status == 0) {
					reloadSceneLightLevelGrid(sceneTemplateId);
				}
				},
				error: function(){
					alert("Error occured. Scene could not be deleted");
				},
		 		contentType: "application/xml; charset=utf-8"
			});
		}
	}
	function cancelValidation() {
		exitWindow();
	}

	function exitWindow() {
		$('#sceneTemplatePromptDialog').dialog('close');
	}
	
	function beforeSaveSceneTemplates()
	{
		var sceneTemplateName = $("#sceneTemplateName").val();
		
		if(sceneTemplateName == null || sceneTemplateName == ""){
			displaySceneTemplatePromptMessage("Please enter scene template Name", COLOR_FAILURE);
			return;
		}		
	    var regExpStr = /^[a-z0-9\_\s]+$/i;
	    if(regExpStr.test(sceneTemplateName) == false) {
	    	displaySceneTemplatePromptMessage("Scene template name must contain only letters, numbers, or underscore", COLOR_FAILURE);	
	    	return;   	
	    } 
		if(sceneTemplateId<0)
		{
			checkDuplicatesceneTemplateName(sceneTemplateName);
		}else
		{
			saveSceneTemplates();
		}
	}
	
	function saveSceneTemplates()
	{
		clearSceneTemplatePromptMessage();
		var sceneName = $('#sceneTemplateName').val();
		$.ajax({
			type : 'POST',			
			url : "${editsceneTemplateName}"+ sceneTemplateId +"/" + sceneName + "?ts="+ new Date().getTime(),
			data : "",
			success : function(data) {
				sceneTemplateId = data.id;
				displaySceneTemplatePromptMessage("Scene Template name saved successfully", COLOR_SUCCESS);
				$('#addSceneLevelBtn').removeClass("disableAddScenehButton");	
				$('#addSceneLevelBtn').addClass("enableAddSceneButton");	
			},
			dataType : "json",
			contentType : "application/json; charset=utf-8"
		});
	}
	function displaySceneTemplatePromptMessage(Message, Color) {
		$("#scenetemplate-message-div").html(Message);
		$("#scenetemplate-message-div").css("color", Color);
	}
	
	function clearSceneTemplatePromptMessage() {
		displaySceneTemplatePromptMessage("", COLOR_DEFAULT);
	}

	function checkDuplicatesceneTemplateName(sceneTemplateName) 
	{
			$.ajax({
				type : 'GET',			
				url : "${checkDuplicateSceneTemplateUrl}" + encodeURIComponent(sceneTemplateName) + "?ts="+ new Date().getTime(),
				data : "",
				success : function(data) {
					if (data == null) {
						flag = false;
						displaySceneTemplatePromptMessage("Scene Template with this name already exists.", COLOR_FAILURE);					
					} else {
						$.each(data, function(index) {
							sceneTemplateId = data[index].id;
							saveSceneTemplates();
						});					
					}
				},
				dataType : "json",
				contentType : "application/json; charset=utf-8"
			});
	}
	
	function reloadSceneLightLevelGrid(tsceneTemplateID){
		var newURL = "${getSceneLightLevelBySceneTemplateIdURL}"+tsceneTemplateID+"?ts="+new Date().getTime();
		$("#scene-level-table").jqGrid().setGridParam({sortname: 'SceneName', sortorder:'desc',url:newURL}).trigger("reloadGrid");
	}
</script>
</head>
<body>
	<div id="scenetemplate-message-div" style="padding-top: 5px; padding-left: 7px;"></div>
	<table id="SceneTemplatePromptTable" style="margin: 5px; width: 95%;">
		<tr>
			<th style="text-align: left;width: 40%">Scene Template Name: </th>
			<td style="width: 40%" ><input type="text"
				id="sceneTemplateName">
				</td>
				<td>
				<div align="center" style="padding-top: 5px;padding-left: 5px;">
				<button id="save-btn" onclick="beforeSaveSceneTemplates()">Save</button>
				</div>
				</td>
		</tr>
		<tr></tr>
		<tr>
			<td style="padding-top: 8px; padding-left: 2px;"><button type="button" class="disableAddScenehButton"
					id="addSceneLevelBtn" >Add Scene</button></td>
		</tr>
		<tr valign="top">
			<td style="padding-top: 5px; padding-left: 2px;">
				<table id="scene-level-table"></table>
			</td>
		</tr>
	</table>
	<div align="center" style="padding-top: 5px;">
		<button id="cancel-btn" onclick="cancelValidation();">Close</button>
	</div>
</body>
</html>

