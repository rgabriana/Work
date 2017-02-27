<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/eminstance/listemstats/" var="getEmStatsList" scope="request" />
<spring:url value="/services/org/eminstance/loademstats/" var="loadEmStatsList" scope="request" />

<style>
span.cellWithoutBackground
{
    display:block;
    background-image:none;
    margin-right:-2px;
    margin-left:-2px;
    height:14px;
    padding:4px;
}

</style>

<script type="text/javascript">
	var CPU_THRESHOLD_VALUE = "${cpuThresholdValue}";
	
	$(document).ready(function() {
		start(1, "desc");
		$("#emStatsTable").setGridWidth($(window).width() - 25);
	});

	function start(pageNum, orderWay) {
		jQuery("#emStatsTable").jqGrid({
			url: "${loadEmStatsList}"+"${emInstanceId}"+"?ts="+new Date().getTime(),
			mtype: "POST",
			datatype: "json",
			autoencode: true,
			hoverrows: false,
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			formatter: {
				 integer: {thousandsSeparator: ",", defaultValue: '0'},
			     number: {decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, defaultValue: '0.00'}
			},
			colNames: ["id", "Captured At", "Active thread Count", "GC Count", "GC Time", "Heap Used", "Non Heap Used", "Sys Load", "CPU %"],
		       colModel: [
			   { name:'id', index:'id', sortable:false, hidden: true},
		       { name: "UtcCaptureAt", index: 'UtcCaptureAt',sorttype:"string", width:'175',align: "left" },
		       { name: "activeThreadCount", index: 'activeThreadCount', sortable:false, width:'175',align: "left" },
		       { name: "gcCount", index: 'gcCount', sortable:false, width:'100',align: "left" },
		       { name: "gcTime", index: 'gcTime', sortable:false, width:'100',align: "left" },
		       { name: "heapUsed", index: 'heapUsed',sortable:false,width:'100', align: "left"},
		       { name: "nonHeapUsed", index: 'nonHeapUsed',sortable:false,width:'150', align: "left"},
		       { name: "sysLoad", index: 'sysLoad',sortable:false,width:'100', align: "left"},
		       { name: "cpuPercentage", index: 'cpuPercentage',sortable:false,width:'100', align: "center",formatter: CPUFormatter}],
		       
		   	jsonReader: { 
				root:"emStats", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    pager: '#emStatsPagingDiv',
		   	page: pageNum,
		   	sortorder: orderWay,
		   	sortname: "captureAt",
		    hidegrid: false,
		    viewrecords: true,
		   	loadui: "block",
		   	toolbar: [false,"top"],
		   	onSortCol: function(index, iCol, sortOrder) {
		   	},
		   	loadComplete: function(data) {
		   		if (data != null){
		   			if (data.emStats!= undefined) {
				   		if (data.emStats.length == undefined) {
				   			jQuery("#emStatsTable").jqGrid('addRowData', 0, data.emStats);
				   		}
				   	}
		   		}
		   		
		   		ModifyGridDefaultStyles();
		   		
		   	}

		});
		
		jQuery("#emStatsTable").jqGrid('navGrid',"#emStatsPagingDiv",
										{edit:false,add:false,del:false,search:false}, 
										{}, 
										{}, 
										{}, 
										{},
										{});
		
		forceFitEmStatsTableWidth();
	}
	
	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		forceFitEmStatsTableWidth();
	}).trigger('resize');
	
	function ModifyGridDefaultStyles() {  
		   $('#' + "emStatsTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "emStatsTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "emStatsTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
    }
	
	function CPUFormatter(cellvalue, options, rowObject)
	{
	    var color = (cellvalue > CPU_THRESHOLD_VALUE ) ? "red" : "green";
	    
	    return '<span class="cellWithoutBackground" style="background-color:' +
	    color + ';">' + cellvalue + '</span>';
	}
	
	function forceFitEmStatsTableWidth(){
		var jgrid = jQuery("#emStatsTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#emStatsPagingDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 10);
		
		$("#emStatsTable").setGridWidth($(window).width() - 25);
	}
	
</script>

<div id="divform" style="float: right;"><div id="form1"><form action="list.ems" method=POST name="form1" ><input type="hidden" name="customerId" value ="${customerId}" ></input><button onclick="document.form1.submit();">Back</button></form></div></div><br>
<div style="width: 100%;">
	<div id="outerDiv">
		<div style="padding: 5px 0px 5px 5px;font-weight: bolder; "><spring:message code="emstats.eminstance"/> ${emInstanceName}</div>
    </div>
	<div style="overflow:auto">
		<table id="emStatsTable"></table>
		<div id="emStatsPagingDiv"></div>
	</div>
 </div>


