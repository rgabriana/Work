<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:url value="/services/org/eminstance/listEMInstanceWithHealthData" var="emInstanaceListForHealthData" scope="request" />
<spring:url value="/health/details.ems" var="emMonitoringDetailsUrl" />

<script type="text/javascript">

function emMonitoringDetails(rowId){
	$('#emInstanceId').val(rowId);
	$('#emInstanceMontioringDetailsForm').submit();	
}

$(document).ready(function() {	
	
	function renderEmHealthStatusList(pageNum, orderBy, orderWay,searchCol,searchStr){
	jQuery("#eminstanceList").jqGrid({
		url: "${emInstanaceListForHealthData}"+"?ts="+new Date().getTime(),
		mtype: "POST",
		datatype: "json",
		postData: {"searchCol": searchCol,"searchStr": searchStr},
		autoencode: true,
		height: "100%",
		hoverrows: false,
		autowidth: true,
		scrollOffset: 0,
		forceFit: true,
		colNames: ["id", "Customer", "EmInstance", "Call Home", "Data Synch","Gateways","Sensors","Action"],
	       colModel: [
	       { name:'emInstanceId', index:'emInstanceId', hidden: true},
	       { name: 'customerName', index: 'customerName',sortable:true,sorttype:'string',width:'10%'},
	       { name: "emInstanceName", index: 'emInstanceName',sortable:true, sorttype:'string',width:'6%' },
	       { name: "lastEmConnectivity", index: 'lastEmConnectivity',sortable:true, sortable:'string',width:'10%' ,formatter: emConnectivityFormatter},
	       { name: "lastDataSynchConnectivity", index: 'lastDataSynchConnectivity', sortable:false,width:'10%', formatter: dataSynchConnectivityFormatter },
	       { name: "gateways", index: 'gateways',sortable:false,width:'6%', formatter: gatewayHealthFormatter },
	       { name: "sensors", index: 'sensors',sortable:false,width:'6%', formatter: sensorHealthFormatter},
	       { name: "action", index: 'action',sortable:false,width:'10%', align: "center",formatter: viewActionFormatter}
	       ],
	   	cmTemplate: { title: false },
	    pager: '#eminstanceListPager',
	   	page: pageNum,
	   	sortorder: orderWay,
		sortname: orderBy,
	    hidegrid: false,
	    viewrecords: true,
	   	loadui: "block",
	   	toolbar: [false,"top"],
	   	onSortCol: function(index, iCol, sortOrder) {
	   	},
	    loadComplete: function(data) {
	   		if (data != null){
	   			if (data.healthDataVOList != undefined && data.healthDataVOList.length == undefined) {
	   				jQuery("#eminstanceList").jqGrid('addRowData', 0, data.healthDataVOList);
			   	}
	   		}
	   		ModifyGridDefaultStyles();
	   	},
	   	jsonReader: { 
			root:"healthDataVOList", 
	        page:"page", 
	        total:"total", 
	        records:"records", 
	        repeatitems:false,
	        id : "id"
	   	}
	});
	
	  $("#eminstanceList").jqGrid('navGrid',"#eminstanceListPager",
				{edit:false,add:false,del:false,search:false}, 
				{}, 
				{}, 
				{}, 
				{},
				{});
		   forceFitEmStatsTableWidth();
	}
	   
	function emConnectivityFormatter(cellvalue, options, rowObject){
		var minuteToConnectivity = rowObject.emConnectivityInMinutes;
		var timestamp = rowObject.lastEmConnectivity;		
		var style = "black";
		var displayText = "";
		

		if(!isNaN(minuteToConnectivity)){
			if(minuteToConnectivity > 10080){
				style= "red";
			}else if(minuteToConnectivity > 15){
				style = "orange";
		    }
			displayText = "<div style='color:" + style +";'>"+ timestamp+ " (" + minuteToConnectivity + " mins)" +"</div>";
	    }else{
		
	       displayText = "<div style='color:" + style +";'>"+ timestamp+ " (" + minuteToConnectivity + ")" +"</div>";
	    }
		return displayText;
		
	}
	
	function dataSynchConnectivityFormatter(cellvalue, options, rowObject){
		var minuteToConnectivity = rowObject.lastDataSynchConnectivityInMinutes;
		var timestamp = rowObject.lastDataSynchConnectivity;		
		var style = "black";
		var displayText = "";
		
		if(!isNaN(minuteToConnectivity)){
			if(minuteToConnectivity > 10080){
				style= "red";
			}else if(minuteToConnectivity > 15){
				style = "orange";
		    }
			displayText = "<div style='color:" + style +";'>"+ timestamp+ " (" + minuteToConnectivity + " mins)" +"</div>";
	    }else{
			
		       displayText = "<div style='color:black;'>"+ "NA" +"</div>";
		    }
		return displayText;
		
	}
	
	function gatewayHealthFormatter(cellvalue, options, rowObject){
		 var gatewayTotal = rowObject.gatewaysTotal;
		 var gatewaysUnderObservation = rowObject.gatewaysUnderObservationNo;
		 var gatewaysCritical = rowObject.gatewaysCriticalNo;
		 var style = "black";
		 
		 if(!isNaN(gatewayTotal) && gatewayTotal > 0 ){
			 var displayText = gatewayTotal;
			 
			 if(gatewaysCritical > 0){
				 style= "red";
			 }else if(gatewaysUnderObservation > 0){
				 style = "orange";
			 }
			 
			 return "<div style='text-align:center;color:" + style +";'>"+ displayText +"</div>";
		 }else{
			 return "";
		 }
	}
	
	function sensorHealthFormatter(cellvalue, options, rowObject){
		 var sensorTotal = rowObject.sensorsTotal;
		 var sensorssUnderObservation = rowObject.sensorsUnderObservationNo;
		 var sensorsCritical = rowObject.sensorsCriticalNo;
		 var style = "black";
		 
		 if(!isNaN(sensorTotal) && sensorTotal > 0 ){
			 var displayText = sensorTotal;
			 
			 if(sensorsCritical > 0){
				 style= "red";
			 }else if(sensorssUnderObservation > 0){
				 style = "orange";
			 }
			 
			 return "<div style='text-align:center;color:" + style +";'>"+ displayText +"</div>";
		 }else{
			 return "";
		 }
	}
	
	function viewActionFormatter(cellvalue, options, rowObject) {
		var rowId = rowObject.emInstanceId;
		return "<button onclick=\"javascript:emMonitoringDetails("+rowId+");\">Details</button>";
		}	
	
	

	function ModifyGridDefaultStyles() {  
		   $('#' + "eminstanceList" + ' tr').removeClass("ui-widget-content");
		   $('#' + "eminstanceList" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "eminstanceList" + ' tr:nth-child(odd)').addClass("oddTableRow");
		   $("#eminstanceList").setGridWidth($(window).width()-25);

   }
	
	function forceFitEmStatsTableWidth(){
		var jgrid = jQuery("#eminstanceList");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#eminstanceListPager").height();		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 10);		
		$("#eminstanceList").setGridWidth($(window).width() - 25);
	}
	

	renderEmHealthStatusList(1, "customerName", "asc","","");
	$("#eminstanceList").setGridWidth($(window).width() - 25);
	
	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#eminstanceList").setGridWidth($(window).width()-25);
	}).trigger('resize');
	
	$("#searchEmInstButton").click(function(){
		$("#eminstanceList").jqGrid("GridUnload");
		renderEmHealthStatusList(1, "customerName", "asc",$("#searchColumn").val(),encodeURIComponent($.trim($('#searchString').val())));
	});
	
	$("#resetEmInstButton").click(function(){
		$("#eminstanceList").jqGrid("GridUnload");
		$('#searchString').val("");
		$("#searchColumn").val($("#searchColumn option:first").val());
		renderEmHealthStatusList(1, "customerName", "asc","","");
	});

});
</script>

<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="outerDiv" style="padding: 5px 5px 5px 5px">
			<div>
				<div style="float:right;padding: 0px 0px 0px 10px">
					<button id="resetEmInstButton">Reset</button>
				</div>
				<div style="float:right;padding: 0px 0px 0px 10px">
					<button id="searchEmInstButton">Search</button>
				</div>
				<div style="float:right;padding: 0px 0px 0px 10px">
					<input type="text" name="searchString" id="searchString">
				</div>
				<div style="float:right;padding: 0px 0px 0px 10px">
					<select id="searchColumn">
					  <option value="customerName">Customer Name</option>
					  <option value="emInstanceName">EmInstance</option>
					  <option value="lastEmConnectivity">Call Home</option>
					</select>
				</div>
				<div style="float:right;padding: 0px 0px 0px 10px;font-weight: bolder; ">
					<label>Search by</label>
				</div>
				
				<div style="font-weight: bolder; ">Health Dashboard</div>
			</div>
		
		<div style="min-height:5px"></div>
    </div>

<div style="padding: 0px 5px;">
		<table id="eminstanceList"></table>
		<div id="eminstanceListPager"></div>
	</div>
</div>


<form id="emInstanceMontioringDetailsForm" action="${emMonitoringDetailsUrl}" METHOD="POST">
		<input id="emInstanceId" name="emInstanceId" type="hidden"/>
</form>