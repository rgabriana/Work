<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>


<div id="locatorDevices-list-topPanel" style="background: #fff">
	<c:if test="${page == 'floor'}">
		<div style="float: left;"><button id="createLocatorDevice" onclick="javascript:parent.parent.showLocatorDeviceForm();">Add</button></div>	
	</c:if>
	
	<br style="clear:both;"/>
	<div style="height:5px;"></div>

</div>

<script type="text/javascript">
var PAGE = "${page}";
var MAX_ROW_NUM = 99999;
<spring:url value="/services/org/locatordevice/delete/" var="deleteLocatorDeviceUrl" scope="request" />
	$(document).ready(function() {
		
		jQuery("#locatorDevicesTable").jqGrid({
			datatype: "local",
			autowidth: true,
			scrollOffset: 0,
			hoverrows: false,
			forceFit: true,
		   	colNames:['Name', 'Type','Fixture Type','Action'],
		   	colModel:[
		   		{name:'name', index:'name', sorttype:"string", width:"30%"},
		   		{name:'locatorDeviceType', index:'locatorDeviceType',width:"30%"},
		   		{name:'fixtureClassName', index:'fixtureClassName',width:"20%"},
		   		{name:'action', index:'action', align:"right", sortable:false, hidden:(PAGE=="area"), width:"20%"}
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

		forceFitLocatorDevicesTableHeight();
		
		var mydata =  [];
		
		<c:forEach items="${locatordevices}" var="locatordevice">
			var localData = new Object;
			localData.name = '<c:out value="${locatordevice.name}" escapeXml="true" />';			
			localData.id =  "${locatordevice.id}";
			localData.locatorDeviceType =  "${locatordevice.locatorDeviceType.name}";
			localData.fixtureClassName = '<c:out value="${locatordevice.fixtureClassName}" escapeXml="true" />';
			localData.action = "";
			<c:if test="${page == 'floor'}">
			localData.action += "<button onclick=\"javascript: parent.parent.showLocatorDeviceForm(${locatordevice.id});\">Edit</button>";
			</c:if>
			localData.action += "&nbsp;<button onclick=\"javascript: deleteLocatordevices(${locatordevice.id}, '${locatordevice.name}');\">Delete</button>";
								
			mydata.push(localData);
		</c:forEach>
		
		if(mydata)
		{
			for(var i=0;i<mydata.length;i++)
			{
				jQuery("#locatorDevicesTable").jqGrid('addRowData',mydata[i].id,mydata[i]);
			}
		}

		jQuery("#locatorDevicesTable").jqGrid('navGrid',"#locatorDevicesPagingDiv",{edit:false,add:false,del:false});
		
		$("#locatorDevicesTable").jqGrid().setGridParam({sortname: 'name', sortorder:'asc'}).trigger("reloadGrid");
			
	});
	
	function deleteLocatordevices(locatorDeviceID, locatorDeviceName){
		var proceed = confirm("Are you sure you want to delete the Locater Device: "+locatorDeviceName+"?");
		if(proceed==true) {
			$.ajax({
				url: "${deleteLocatorDeviceUrl}"+locatorDeviceID+"?ts="+new Date().getTime(),
				dataType : "json",
				contentType : "application/xml; charset=utf-8",
				success: function(data){
					if(data.status == 1) {
						//parent.parent.getFloorPlanObj("floorplan").updateLocatorDevice();
						reloadLocatorDeviceListFrame();
						try {
							if(parent.parent.getFloorPlanObj("floorplan") != null && parent.parent.getFloorPlanObj("floorplan") != undefined){
								parent.parent.getFloorPlanObj("floorplan").plotChartRefresh();
							}
						} catch(e) {
							
						}
					}
				}
			});
	 	}
	}
	
	function forceFitLocatorDevicesTableHeight(){
		var jgrid = jQuery("#locatorDevicesTable");
		var containerHeight = $(this).height();
		var otherElementHeight = $("#others-list-topPanel").height();
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .99)); 
	}
	
	function reloadLocatorDeviceListFrame(){
		var ifr = parent.document.getElementById('othersFrame');
		window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = ifr.src + new Date().getTime();
	}
	
	function ModifyGridDefaultStyles() {  
		   $('#' + "locatorDevicesTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "locatorDevicesTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "locatorDevicesTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	}

	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#locatorDevicesTable").setGridWidth($(window).width()-20);
	}).trigger('resize');
	
</script>
<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}
</style>
<table id="locatorDevicesTable"></table>
<div id="locatorDevicesPagingDiv"></div>