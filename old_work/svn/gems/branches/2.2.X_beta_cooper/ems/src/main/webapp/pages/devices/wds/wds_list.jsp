<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/services/org/switch/startcommission/" var="startCommissionUrl" scope="request" />
<spring:url value="/services/org/wds/deleteWds/" var="deleteWdsUrl" scope="request" />
			
<div id="wdsDialog"></div>

<div id="wds-list-topPanel" style="background: #fff">
	<c:if test="${page == 'floor' or page == 'area'}">
		<button id="newWds" onclick="javascript: parent.parent.showWdsCommissioningIdentifyWindow();">Add</button>
		<!--<button id="discoverWdsButton" onclick="javascript: parent.parent.showWDSDiscoveryWindow();" style="float: right;"><spring:message code='wdsForm.label.discoverBtn'/></button>-->
	</c:if>
	<br style="clear:both;"/>
	<div style="height:10px;"></div>
</div>

<script type="text/javascript">
var PAGE = "${page}";
var MAX_ROW_NUM = 99999;

var DISC_STATUS_SUCCESS = 1; 				//All the nodes are discovered
var DISC_STATUS_STARTED = 2; 				//Discovery started		
var DISC_STATUS_INPROGRESS = 3; 			//Discovery is in progress		
var DISC_ERROR_INPROGRESS = 4; 				//Discovery is already in progress
var DISC_ERROR_GW_CH_CHANG_DEF = 5; 		//Not able to move the Gateway to default wireless parameters during discovery		
var DISC_ERROR_TIMED_OUT = 6; 				//Not able to find all the nodes within 3 minute timeout.			
var DISC_ERROR_GW_CH_CHANGE_CUSTOM = 7; 	//Not able to move the Gateway to custom wireless parameters after discovery		
var COMM_STATUS_SUCCESS = 8; 				//Commissioning is successful		
var COMM_STATUS_STARTED = 9; 				//Commissioning started		
var COMM_STATUS_INPROGRESS = 10; 			//Commissioning is in progress		
var COMM_STATUS_FAIL = 11; 					//Commissioning failed		
var COMM_ERROR_INPROGRESS = 12; 			//Commissioning is already in progress		
var COMM_ERROR_GW_CH_CHANGE_DEF = 13; 		//Not able to move the Gateway to default wireless parameters during commissioning.			
var COMM_ERROR_GW_CH_CHANGE_CUSTOM = 14; 	//Not able to move the Gateway to custom wireless parameters during commissioning.
var COMM_ERROR_INACTIVE_TIMED_OUT = 15; 	//Commissioning Timed out due to inactivity
var COMM_ERROR_INACTIVE_TIMED_OUT_GW_CH_CHANGE_CUSTOM = 16;

	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#wdsTable").setGridWidth($(window).width()-20);
	}).trigger('resize');

	function startCommission(selectedGateway, switchId, switchType) {
		var floorId = 1; //treenodeid; //selected tree node id (floor id)
		
		var urlOption = "floor/"+floorId+"/gateway/"+selectedGateway;
		
		$.ajax({
			url: "${startCommissionUrl}"+urlOption+"?ts="+new Date().getTime(),
			dataType:"json",
			contentType: "application/json; charset=utf-8",
			success: function(data){
				var status = (1 * data.status);
				if(status == COMM_STATUS_STARTED) {
					parent.parent.showWdsCommissioningForm(false, switchId, selectedGateway,switchType);
				} else if(status == DISC_ERROR_INPROGRESS) {
					alert("Discovery is already in progress. Please try later.");
				} else if(status == COMM_ERROR_INPROGRESS) {
					alert("Commissioning is already in progress. Please try later.");
				}
				//exitWindow();
			}
		});
	}

	$(document).ready(function() {
	WDS_GRID = $("#wdsTable");
		WDS_GRID.jqGrid({
			datatype: "local",
			autowidth: true,
			scrollOffset: 0,
			hoverrows: false,
			forceFit: true,
		   	colNames:['Name', 'Switch','Gateway','Version','Upgrade Status', 'Action'],
		   	colModel:[
		   		{name:'name', index:'name', sorttype:"string", width:"20%"},
		   		{name:'switchName', index:'switchName', sorttype:"string", width:"20%"},
		   		{name:'gatewayName', index:'gatewayName', sortable:true, width:"15%"},
		   		{name:'version', index:'version', sortable:true, width:"15%" },
		   		{name:'upgradeStatus', index:'upgradeStatus', sortable:true, width:"15%"},
		   		{name:'action', index:'action', align:"right", sortable:false, width:"15%"},
		   	],
		   	cmTemplate: { title: false },
 		   	rowNum:MAX_ROW_NUM,
		   	sortname: 'name',
		    viewrecords: true,
		    sortorder: "desc",
		    loadComplete: function() {
		    	 ModifyGridDefaultStyles();
		    }
		});

		forceFitSwitchTableHeight();
		
		var mydata =  [];
		
		<c:forEach items="${wdss}" var="wds">
			var localData = new Object;
			localData.name = '<c:out value="${wds.name}" escapeXml="true" />';
			localData.id =  "${wds.id}";
			localData.floor = "${wds.floorId}";
			localData.area = "${wds.areaId}";
			localData.version ="${wds.version}";
			localData.gatewayName = "${wds.gatewayName}";
			localData.upgradeStatus = "${wds.upgradeStatus}";
			localData.switchName = "${wds.wdsSwitch.name}";
			
			localData.action = "";			
			localData.action += "&nbsp;<button onclick=\"javascript: parent.parent.showWdsEdit(${wds.id}, '${wds.name}');\">View</button>";
			localData.action += "&nbsp;<button onclick=\"javascript: deleteWds(${wds.id}, '${wds.name}');\">Delete</button>";
								
			mydata.push(localData);
		</c:forEach>
		
		if(mydata)
		{
			for(var i=0;i<mydata.length;i++)
			{
				WDS_GRID.jqGrid('addRowData',mydata[i].id,mydata[i]);
			}
		}

		jQuery("#wdsTable").jqGrid('navGrid',"#wdsPagingDiv",{edit:false,add:false,del:false});
		
		saveGridParameters(WDS_GRID);
		fillingGridWithUserSelection(WDS_GRID);
	});
	
	$(window).unload(function(){
		saveGridParameters(WDS_GRID);
		
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
		//Resetting the Wds grid according to user selections
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
		var jgrid = jQuery("#wdsTable");
		var containerHeight = $(this).height();
		var otherElementHeight = $("#switch-list-topPanel").height();
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .99)); 
	}
	
	function ModifyGridDefaultStyles() {  
		   $('#' + "wdsTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "wdsTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "wdsTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	}
	
	function deleteWds(wdsId) {
		$.ajax({
			type: 'POST',
			url: "${deleteWdsUrl}"+wdsId+"?ts="+new Date().getTime(),
			data: "",
			success: function(data) {
				if(data.status == 1) {
					alert("<spring:message code='wdslisting.association.warn'/>");
				} else {
					reloadWdsFrame();
				}
			},
			dataType:"json",
			contentType: "application/json; charset=utf-8"
		});
	}
	
	function reloadWdsFrame(){
		var ifr = parent.document.getElementById('wdsFrame');
		window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = ifr.src;
	}
	
</script>
<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}
</style>
<table id="wdsTable"></table>
<div id="wdsPagingDiv"></div>