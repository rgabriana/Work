<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:url value="/services/org/eminstance/gatewayListForHealthData" var="gatewayListForHealthData" scope="request" />

<script type="text/javascript">

$(document).ready(function() {	
	
	function renderEmHealthStatusList(pageNum,orderWay){
	jQuery("#gatewayList").jqGrid({
		url: "${gatewayListForHealthData}"+"/${emInstanceId}"+"?ts="+new Date().getTime(),
		mtype: "POST",
		datatype: "json",
		autoencode: true,
		height: "100%",
		hoverrows: false,
		autowidth: true,
		scrollOffset: 0,
		forceFit: true,
		colNames: ["Id","Name","Mac Address","Version","No. of Sensors","Last Connectivity"],
	       colModel: [
	       { name:'gatewayId', index:'gatewayId', hidden: true},
	       { name: 'gatewayName', index: 'gatewayName',sorttype:'string',width:'10%' },
	       { name: "gatewayMac", index: 'gatewayMac', sortable:'string',width:'6%' },
	       { name: "gatewayVersion", index: 'gatewayVersion',sortable:'string',width:'6%'},
	       { name: "noOfSensor", index: 'noOfSensor',sortable:'string',width:'6%' },
	       { name: "lastGatewayConnectivity", index: 'lastGatewayConnectivity', sortable:'string',width:'10%'} 
	       ],
	   	cmTemplate: { title: false },
	    pager: '#gatewayListPager',
	   	page: pageNum,
	   	sortorder: orderWay,
	   	sortname: "name",
	    hidegrid: false,
	    viewrecords: true,
	   	loadui: "block",
	   	toolbar: [false,"top"],
	   	onSortCol: function(index, iCol, sortOrder) {
	   	},
	    loadComplete: function(data) {
	   		if (data != null){
	   			if (data.gatewayHealthDataVO != undefined) {
			   		if (data.gatewayHealthDataVO.length == undefined) {
			   			jQuery("#gatewayList").jqGrid('addRowData', 0, data.gatewayHealthDataVO);
			   		}
			   	}
	   		}
	   		ModifyGridDefaultStyles();
	   	},
	   	jsonReader: { 
			root:"gatewayHealthDataVO", 
	        page:"page", 
	        total:"total", 
	        records:"records", 
	        repeatitems:false,
	        id : "id"
	   	}
	});
	
	  $("#gatewayList").jqGrid('navGrid',"#gatewayListPager",
				{edit:false,add:false,del:false,search:false}, 
				{}, 
				{}, 
				{}, 
				{},
				{});

		   forceFitGwHealthTableWidth();
	}
	
	function forceFitGwHealthTableWidth(){
		var jgrid = jQuery("#gatewayList");
		var containerHeight = $("body").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#gatewayListPager").height();		
		jgrid.jqGrid("setGridHeight",  containerHeight -outerDivHeight - gridHeaderHeight - gridFooterHeight - 10);		
		$("#gatewayList").setGridWidth($(window).width() - 25);
	}

	function ModifyGridDefaultStyles() {  
		   $('#' + "gatewayList" + ' tr').removeClass("ui-widget-content");
		   $('#' + "gatewayList" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "gatewayList" + ' tr:nth-child(odd)').addClass("oddTableRow");
		   $("#gatewayList").setGridWidth($(window).width()-25);

   }
	renderEmHealthStatusList(1, "desc");
	$("#gatewayList").setGridWidth($(window).width() - 25);
	
	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#gatewayList").setGridWidth($(window).width()-25);
	}).trigger('resize');


});
	
</script>
<html>
<body>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
<div id="outerDiv" style="padding: 5px 5px 5px 5px">
			<div style="font-weight: bolder; ">Gateway Dashboard</div>
<div style="min-height:5px"></div>
    </div>

<div style="padding: 0px 5px;">
		<table id="gatewayList"></table>
		<div id="gatewayListPager"></div>
	</div>
</div>
</body>
</html>

