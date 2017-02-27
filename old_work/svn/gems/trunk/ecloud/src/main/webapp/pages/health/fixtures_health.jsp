<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:url value="/services/org/eminstance/v2/fixtureListForHealthData" var="fixtureListForHealthData" scope="request" />

<script type="text/javascript">

$(document).ready(function() {	
	
	function renderEmHealthStatusList(pageNum,orderWay){
	jQuery("#fixtureList").jqGrid({
		url: "${fixtureListForHealthData}"+"/${emInstanceId}"+"?ts="+new Date().getTime(),
		mtype: "POST",
		datatype: "json",
		autoencode: true,
		height: "100%",
		hoverrows: false,
		autowidth: true,
		scrollOffset: 0,
		scroll: 1,
        loadonce: true,
		forceFit: true,
		colNames: ["Id","Name","Mac Address","Version","Last Connectivity","Location"],
	       colModel: [
	       { name:'fixtureId', index:'fixtureId', hidden: true},
	       { name: 'fixtureName', index: 'fixtureName',sorttype:'string',width:'10%' },
	       { name: "fixtureMac", index: 'fixtureMac', sortable:'string',width:'6%' },
	       { name: "fixtureVersion", index: 'fixtureVersion',sortable:'string',width:'6%'},
	       { name: "lastFixtureConnectivity", index: 'lastFixtureConnectivity',sortable:'string',width:'6%' },
	       { name: "location", index: 'location', sortable:'string',width:'10%'} 
	       ],
	   	cmTemplate: { title: false },
	    pager: '#fixtureListPager',
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
			   			jQuery("#fixtureList").jqGrid('addRowData', 0, data.gatewayHealthDataVO);
			   		}
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
	
	  $("#fixtureList").jqGrid('navGrid',"#fixtureListPager",
				{edit:false,add:false,del:false,search:false}, 
				{}, 
				{}, 
				{}, 
				{},
				{});

		   forceFitFixtureHealthTableWidth();
	}
	
	function forceFitFixtureHealthTableWidth(){
		var jgrid = jQuery("#fixtureList");
		var containerHeight = $("body").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#fixtureListPager").height();		
		jgrid.jqGrid("setGridHeight",  containerHeight -outerDivHeight - gridHeaderHeight - gridFooterHeight - 70);		
		$("#fixtureList").setGridWidth($(window).width() - 25);
	}

	function ModifyGridDefaultStyles() {  
		   $('#' + "fixtureList" + ' tr').removeClass("ui-widget-content");
		   $('#' + "fixtureList" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "fixtureList" + ' tr:nth-child(odd)').addClass("oddTableRow");
		   $("#fixtureList").setGridWidth($(window).width()-25);

   }
	renderEmHealthStatusList(1, "desc");
	$("#fixtureList").setGridWidth($(window).width() - 25);
	
	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#fixtureList").setGridWidth($(window).width()-25);
	}).trigger('resize');


});
	
</script>
<html>
<body>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
<div id="outerDiv" style="padding: 5px 5px 5px 5px">
			<div style="font-weight: bolder; ">Fixtures Dashboard</div>
<div style="min-height:5px"></div>
    </div>

<div style="padding: 0px 5px;">
		<table id="fixtureList"></table>
		<div id="fixtureListPager"></div>
	</div>
</div>
</body>
</html>

