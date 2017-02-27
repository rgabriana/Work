<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<style type="text/css">
	html, body{margin:3px 3px 0px 3px !important; padding:0px !important; background: #ffffff;}	
</style>

<div id="fixtureDialog"></div>
<spring:url value="/themes/default/images/connected.png" var="fixtureConnected"/>
<script type="text/javascript">
	$(document).ready(function() {

		jQuery("#fixtureTable").jqGrid({
			datatype: "local",
			autowidth: true,
			forceFit: true,
			scrollOffset: 0,
			hoverrows: false,
		   	colNames:["Status", "MAC Address", "Sensor Name", "Version", "Gateway", "Location"],
		   	colModel:[
				{name:'status', index:'status', align:"center", sortable:false, width:"8%"},
		   		{name:'macAddress', index:'macAddress', sorttype:"string", width:"11%"},
		   		{name:'fixtureName', index:'fixtureName', sorttype:"string", width:"11%"},
		   		{name:'version', index:'version', sorttype:"string", width:"8%"},
		   		{name:'gatewayName', index:'gatewayName', sorttype:"string", width:"8%"},
		   		{name:'location', index:'location', sorttype:"string", width:"14%"},
		   	],
// 		   	rowNum:30,
// 		   	rowList:[10,20,30],
// 		   	pager: '#fixturePagingDiv',
		   	sortname: 'fixtureName',
		    viewrecords: true,
		    sortorder: "desc",
		    gridComplete: function(){
		    	ModifyGridDefaultStyles();
		    }
		});
		
		forceFitFixtureTableHeight();
			
		var mydata =  [];
		
		<c:forEach items="${fixtures}" var="fixture">
			var localData = new Object;
			localData.status = " <img src=\"${fixtureConnected}\" alt=\"Connected\" />";
			localData.macAddress = "${fixture.macAddress}";
			localData.fixtureName = "${fixture.fixtureName}";
			localData.version = "${fixture.version}";
			localData.gatewayName = "${fixture.gateway.gatewayName}";
			localData.location = "${fixture.location}";
			localData.action = "";
			mydata.push(localData);
		</c:forEach>
		
		for(var i=0; i<=mydata.length; i++)
		{
			jQuery("#fixtureTable").jqGrid('addRowData', i+1, mydata[i]);
		}

		jQuery("#fixtureTable").jqGrid('navGrid',"#fixturePagingDiv",{edit:false,add:false,del:false});
				
	});
	
	function forceFitFixtureTableHeight(){
		var jgrid = jQuery("#fixtureTable");
		var containerHeight = $(this).height();
		var otherElementHeight = $("#fixture-list-topPanel").height();
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .97)); 
	}
	
	function ModifyGridDefaultStyles() {  
		   $('#' + "fixtureTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "fixtureTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "fixtureTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	}
</script>

<table id="fixtureTable"></table>
<div id="fixturePagingDiv"></div>