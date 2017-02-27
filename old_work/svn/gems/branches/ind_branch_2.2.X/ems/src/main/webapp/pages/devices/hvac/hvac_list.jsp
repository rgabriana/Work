<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/hvac/decommission" var="deleteHvacUrl" scope="request" />
<spring:url value="/devices/hvac/load.ems" var="loadHVACDevice" scope="request" />


<div id="HVACDialog"></div>

<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}	
</style>


<script type="text/javascript">
var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
	$(document).ready(function() {
		
		jQuery("#hvacTable").jqGrid({
			datatype: "local",
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			colNames:["id", "Name", "Controller Id","Device Id", "Type", "Action"],
		   	colModel:[
				{name:'id', index:'id', hidden: true},
		   		{name:'name', index:'name', sorttype:"string", width:"25%"},
		   		{name:'controllerId', index:'controllerId', sortable:false, width:"11%"},
		   		{name:'deviceId', index:'deviceId', sortable:false, width:"11%"},
		   		{name:'type', index:'type', sortable:false, width:"25%"},
		   		{name:'action', index:'action', align:"center", sortable:false,  width:"15%"}
		   	],
		   	cmTemplate: { title: false },
// 		    multiselect: true,
// 		   	rowNum:30,
// 		   	rowList:[10,20,30],
// 		   	pager: '#gatewayPagingDiv',
		   	sortname: 'name',
		    viewrecords: true,
		    sortorder: "desc"
		});
		
		forceFitHVACTableHeight();
		
		
		var mydata =  [];
		
		<c:forEach items="${hvacDevices}" var="hvac">
			var localData = new Object;
			localData.id = "${hvac.id}";
			localData.name = "${hvac.name}";
			localData.controllerId = "${hvac.controllerId}";
			localData.deviceId = "${hvac.deviceId}";
			<c:if test="${hvac.deviceType == '1'}">
				localData.type = "Cassette AC";
			</c:if>
			<c:if test="${hvac.deviceType == '2'}">
				localData.type = "Split AC";
			</c:if>
			<c:if test="${hvac.deviceType == '3'}">
				localData.type = "Duct AC";
			</c:if>
			localData.action = "";
			<c:if test="${page == 'floor'}">
			localData.action += "<button onclick=\"showHVACDeviceForm(${hvac.id})\">"+ 
													"<spring:message code='gatewayForm.label.editBtn' />"+
												"</button>";
			</c:if>
			localData.action += "&nbsp;<button onclick=\"javascript: deleteHVAC(${hvac.id},'${hvac.name}');\">Delete</button>";
			mydata.push(localData);
		</c:forEach>

		

		for(var i=0; i<=mydata.length; i++)
		{
			jQuery("#hvacTable").jqGrid('addRowData', i+1, mydata[i]);
		}

		jQuery("#hvacTable").jqGrid('navGrid',"#hvacPagingDiv",{edit:false,add:false,del:false});
		
		
		 var displayStatus= "${updateStatus}";  
         if(displayStatus=="success")
         displayLabelMessage('Hvac Device updated successfully', COLOR_SUCCESS);
		
	});
	
	function showHVACDeviceForm(hvacId) {
		displayLabelMessage("", COLOR_DEFAULT);
		var windowHeight = window.screen.availHeight;
		var windowWidth = window.screen.availWidth;
		
		$("#HVACDialog").load("${loadHVACDevice}?hvacId="+hvacId+"&ts="+new Date().getTime(), function() {
			  $("#HVACDialog").dialog({
					modal:true,
					title: 'HVAC Device',
//	 				height: windowHeight-300, //default auto
					width: "30%"
				});
			});
		return false;
	}
	function forceFitHVACTableHeight(){
		var jgrid = jQuery("#hvacTable");
		var containerHeight = $(this).height();
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - gridHeaderFooterHeight) * .99)); 
	}

	
	function deleteHVAC(hvacId,name){
		displayLabelMessage("", COLOR_DEFAULT);
		var proceed = confirm("Are you sure you want to delete device "+name+ "?");
		if(proceed){
			$.ajax({
				type: 'POST',
				url: "${deleteHvacUrl}"+"?ts="+new Date().getTime(),
				data: "<hvacdevice><id>"+hvacId+"</id><name>"+name + "</name></hvacdevice>",
				success: function(data){
					reloadHVACListFrame();
				},
				dataType:"xml",
				contentType: "application/xml; charset=utf-8",
			});
		}
	}
	
	
	
	function reloadHVACListFrame(){
		var ifr = parent.document.getElementById('hvacFrame');
		window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = ifr.src;
	}

      
	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#hvacTable").setGridWidth($(window).width()-20);
	}).trigger('resize');	
	
	 function displayLabelMessage(Message, Color) {
		$("#display_message").html(Message);
		$("#display_message").css("color", Color);
 	
	}
	function clearLabelMessage(Message, Color) {
		displayLabelMessage("", COLOR_DEFAULT);
	}
</script>
<div id="display_message" style="font-size: 14px; font-weight: bold;padding: 5px 0 0 5px;" ></div>
<div style="padding-top: 0px;">
<table id="hvacTable" width="100%"></table>
</div>

<div id="hvacPagingDiv"></div>
