<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<div id="SwitchesDialog"></div>

<div id="switch-list-topPanel" style="background: #fff">
	<c:if test="${page == 'floor' or page == 'area'}">
		<button id="newSwitch" onclick="javascript:parent.parent.showSwitchForm();">Create Switch</button>
		<div style="height:10px;"></div>
	</c:if>
</div>

<script type="text/javascript">
var PAGE = "${page}";
var MAX_ROW_NUM = 99999;
<spring:url value="/services/org/switch/delete/" var="deleteSwitchUrl" scope="request" />

	$(document).ready(function() {
		
		SWITCH_GRID = $("#switchTable");
		SWITCH_GRID.jqGrid({
			datatype: "local",
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
		   	colNames:['Name', 'Action'],
		   	colModel:[
		   		{name:'name', index:'name', sorttype:"string", width:"34%"},
		   		{name:'action', index:'action', align:"right", sortable:false, width:"33%"}
		   	],
		   	cmTemplate: { title: false },
 		   	rowNum:MAX_ROW_NUM,
// 		   	rowList:[10,20,30],
// 		   	pager: '#switchPagingDiv',
		   	sortname: 'name',
		    viewrecords: true,
		    sortorder: "desc"    
		});

		forceFitSwitchTableHeight();
		
		var mydata =  [];
		
		<c:forEach items="${switches}" var="switch">
			var localData = new Object;
			localData.name = '<c:out value="${switch.name}" escapeXml="true" />';
			localData.id =  "${switch.id}";
			localData.floor = "${switch.floorId}";
			localData.area = "${switch.areaId}";
			localData.action = "";
			<c:if test="${page == 'floor' and switch.areaId == undefined }">
				localData.action += "<button onclick=\"javascript: parent.parent.showSwitchForm(${switch.id});\">Edit</button>";
			</c:if>
			<c:if test="${page == 'area'}">
				localData.action += "<button onclick=\"javascript: parent.parent.showSwitchForm(${switch.id});\">Edit</button>";
			</c:if>
			localData.action += "&nbsp;<button onclick=\"javascript: deleteSwitch(${switch.id}, '${switch.name}');\">Delete</button>";
								
			mydata.push(localData);
		</c:forEach>
		
		if(mydata)
		{
			for(var i=0;i<mydata.length;i++)
			{
				SWITCH_GRID.jqGrid('addRowData',mydata[i].id,mydata[i]);
			}
		}

		jQuery("#switchTable").jqGrid('navGrid',"#switchPagingDiv",{edit:false,add:false,del:false});
		
		saveGridParameters(SWITCH_GRID);
		fillingGridWithUserSelection(SWITCH_GRID);
	});
	$(window).unload(function(){
		saveGridParameters(SWITCH_GRID);
		
	});
	function saveGridParameters(grid) {       
	    var gridInfo = new Object();
	    gridInfo.sortname = grid.jqGrid('getGridParam', 'sortname');
	    gridInfo.sortorder = grid.jqGrid('getGridParam', 'sortorder');
	    gridInfo.selrow = grid.jqGrid('getGridParam', 'selrow');
	    gridInfo.page = grid.jqGrid('getGridParam', 'page');
	    gridInfo.rowNum = grid.jqGrid('getGridParam', 'rowNum');
	    localStorage.setItem("GridParam",JSON.stringify(gridInfo));
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
			       setTimeout(function(){
			       jQuery(grid).trigger('reloadGrid'); 
			       jQuery(grid).jqGrid('setSelection',gridInfo.selrow );
			    },100);
		       } 
		    
	}
	function forceFitSwitchTableHeight(){
		var jgrid = jQuery("#switchTable");
		var containerHeight = $(this).height();
		var otherElementHeight = $("#switch-list-topPanel").height();
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .99)); 
	}
	
	function deleteSwitch(switchID, switchName){
		var proceed = confirm("<spring:message code='switchForm.message.validation.deleteConfirmation'/>: "+switchName+"?");
		if(proceed==true) {
			$.ajax({
				url: "${deleteSwitchUrl}"+switchID+"?ts="+new Date().getTime(),
				success: function(data){
// 					alert("Switch deleted successfully.");
					reloadSwitchesListFrame();
					parent.parent.getFloorPlanObj("floorplan").plotChartRefresh();
				}
			});
	 	}
	}
	
	function reloadSwitchesListFrame(){
		var ifr = parent.document.getElementById('switchesFrame');
		window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = ifr.src;
	}	

	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#switchTable").setGridWidth($(window).width()-20);
	}).trigger('resize');
	
</script>
<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}
</style>
<table id="switchTable"></table>
<div id="switchPagingDiv"></div>