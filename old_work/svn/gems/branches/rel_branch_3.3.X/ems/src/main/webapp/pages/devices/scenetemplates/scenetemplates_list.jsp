<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:url	value="/services/org/scenetemplate/delete/" var="deleteSceneTemplateUrl" scope="request" />
<div id="SceneTemplatesDialog"></div>

<div id="scenetemplates-list-topPanel" style="background: #fff">
	<c:if test="${page == 'floor'}">
		<div style="display: inline;"><button id="newscenetemplatesflow" onclick="javascript:parent.parent.showSceneTemplatePrompt(-1);">Create</button></div>	
	</c:if>
	
	<div style="height:5px;"></div>

</div>

<script type="text/javascript">
var PAGE = "${page}";
var MAX_ROW_NUM = 99999;

<spring:url value="/services/org/scene/list/scenelevel/sid/" var="sceneLevelsUrl" scope="request" />
	
	$(document).ready(function() {
		
		SceneTemplate_GRID = $("#scenetemplatesTable");
		SceneTemplate_GRID.jqGrid({
			datatype: "local",
			autowidth: true,
			scrollOffset: 0,
			hoverrows: false,
			forceFit: true,
		   	colNames:['Name', 'Scene Light Levels', 'Action'],
		   	colModel:[
		   		{name:'scenetemplateName', index:'scenetemplateName', sorttype:"string", width:"34%"},
		   		{name:'sceneLevels', index:'sceneLevels', sortable:false, width:"34%"},
		   		{name:'action', index:'action', align:"right", sortable:false, hidden:(PAGE=="area"), width:"33%"}
		   	],
		   	cmTemplate: { title: false },
 		   	rowNum:MAX_ROW_NUM,
		   	sortname: 'name',
		    viewrecords: true,
		    sortorder: 'asc',
		    loadComplete: function() {
		    	 ModifyGridDefaultStyles();
		    }    
		});

		forceFitSceneTemplatesTableHeight();		
		
        var mydata =  [];
		
		<c:forEach items="${scenetemplates}" var="scenetemplate">
		{
			var localData = new Object;
			localData.scenetemplateName = '<c:out value="${scenetemplate.name}" escapeXml="true" />';			
			localData.id =  "${scenetemplate.id}";
			
			localData.sceneLevels = "[";			
			<c:forEach items="${scenelevels}" var="scenelevel">
			<c:if test="${scenelevel.sceneTemplateId == scenetemplate.id}">
			localData.sceneLevels += "&nbsp;${scenelevel.lightlevel}";			
			</c:if>
			</c:forEach>
			localData.sceneLevels +="&nbsp;]";			
						
			localData.action = "";
	
			localData.action += "<button onclick=\"javascript:parent.parent.showSceneTemplatePrompt(${scenetemplate.id})\">Edit</button>";	
					
			localData.action += "&nbsp;<button onclick=\"javascript:onDeleteSceneTemplate(${scenetemplate.id})\">Delete</button>";
								
			mydata.push(localData);
		}
		</c:forEach>	
		
		if(mydata)
		{
			for(var i=0;i<mydata.length;i++)
			{				
				SceneTemplate_GRID.jqGrid('addRowData', mydata[i].id, mydata[i]);
			}
		}

		jQuery("#scenetemplatesTable").jqGrid('navGrid',"#scenetemplatePagingDiv",{edit:false,add:false,del:false});
		
		$("#scenetemplatesTable").jqGrid().setGridParam({sortname: 'scenetemplateName', sortorder:'asc'}).trigger("reloadGrid");
			
	});
	
		
	function forceFitSceneTemplatesTableHeight(){
		var jgrid = jQuery("#scenetemplatesTable");
		var containerHeight = $(this).height();
		var otherElementHeight = $("scenetemplates-list-topPanel").height();
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .92)); 
	}
	
	function reloadSceneTemplatesListFrame(){
		var ifr = parent.document.getElementById('scenetemplatesFrame');
		window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = ifr.src + new Date().getTime();
	}
	function onDeleteSceneTemplate(rowId){
		if(confirm("Are you sure you want to delete the scene template?") == true)
		{
			$.ajax({
		 		type: 'POST',
		 		url: "${deleteSceneTemplateUrl}"+rowId+"?ts="+new Date().getTime(),
		 		dataType : "json",
		 		success: function(data){
				if(data.status == 0) {
					reloadSceneTemplatesListFrame();
					}
				},
				error: function(){
					alert("Error occured. Scene Template could not be deleted");
				},
		 		contentType: "application/xml; charset=utf-8"
			});
		}
	}
	function ModifyGridDefaultStyles() {  
		   $('#' + "scenetemplatesTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "scenetemplatesTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "scenetemplatesTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	}

	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#scenetemplatesTable").setGridWidth($(window).width()-20);
	}).trigger('resize');
	
</script>
<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}
</style>
<table id="scenetemplatesTable" style="width: 100%"></table>
<div id="scenetemplatePagingDiv"></div>