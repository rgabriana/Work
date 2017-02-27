<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<div id="SwitchesDialog"></div>

<div id="switch-list-topPanel" style="background: #fff">
	<c:if test="${page == 'floor'}">
		<button id="newSwitchflow" onclick="$('#switch_message').text(''); javascript:parent.parent.showSwitchPrompt();">Create</button>
		<label id="switch_message" style="font-size: 14px; font-weight: bold;padding-left: 30px;"></label>		
	</c:if>
	<br style="clear:both;"/>
	<div style="height:10px;"></div>
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
			hoverrows: false,
			forceFit: true,
		   	colNames:['Name', 'Action'],
		   	colModel:[
		   		{name:'name', index:'name', sorttype:"string", width:"34%"},
		   		{name:'action', index:'action', align:"right", sortable:false, width:"33%"},
		   	],
		   	cmTemplate: { title: false },
 		   	rowNum:MAX_ROW_NUM,
// 		   	rowList:[10,20,30],
// 		   	pager: '#switchPagingDiv',
		   	sortname: 'name',
		    viewrecords: true,
		    sortorder: 'asc',
		    loadComplete: function() {
		    	 ModifyGridDefaultStyles();
		    }
		});

		forceFitSwitchTableHeight();
		
		<c:if test="${page == 'floor' or page == 'area'}">
			
			$('#switch_message').text(''); 
			
		</c:if>
		
		var mydata =  [];
		
		<c:forEach items="${switches}" var="switch">
			var localData = new Object;
			localData.name = '<c:out value="${switch.name}" escapeXml="true" />';
			localData.id =  "${switch.id}";
			localData.floor = "${switch.floorId}";
			localData.area = "${switch.areaId}";
			
			localData.action = "";
			<c:if test="${page == 'floor' and switch.areaId == undefined }">
				localData.action += "<button onclick=\"$('#switch_message').text('');javascript: parent.parent.showWidgetDialog(${switch.id});\">Edit</button>";
			</c:if>
			<c:if test="${page == 'area'}">
				localData.action += "<button onclick=\"$('#switch_message').text('');javascript: parent.parent.showWidgetDialog(${switch.id});\">Edit</button>";
			</c:if>
			
			localData.action += "&nbsp;<button onclick=\"$('#switch_message').text('');javascript: deleteSwitch(${switch.id});\">Delete</button>";
								
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
	
	function deleteSwitch(switchID){
		var switchName = "";
		<c:forEach items="${switches}" var="sw">
			if("${sw.id}" == switchID)
				switchName = "${sw.name}";
		</c:forEach>
		var proceed = confirm("<spring:message code='switchForm.message.validation.deleteConfirmation'/>: "+switchName+"?");
		if(proceed==true) {
			$.ajax({
				url: "${deleteSwitchUrl}"+switchID+"?ts="+new Date().getTime(),
				dataType : "json",
				contentType : "application/xml; charset=utf-8",
				success: function(data){
// 					alert("Switch deleted successfully.");
					if(data.status == 1) {
						$("#switch_message").text("Cannot delete the switch ("+switchName+").Please disassociate the fixtures and EWS associated with this switch");
						$("#switch_message").css("color", 'red');
					}else{
						reloadSwitchesListFrame();
						parent.parent.getFloorPlanObj("floorplan").plotChartRefresh();
					}
					
				}
			});
	 	}
	}
	
	function reloadSwitchesListFrame(){
		var ifr = parent.document.getElementById('switchesFrame');
		window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = ifr.src + new Date().getTime();
	}	
	
	function ModifyGridDefaultStyles() {  
		   $('#' + "switchTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "switchTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "switchTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
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