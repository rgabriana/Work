<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<spring:url value="/services/inventoryreport/loadsitelistbycustomerid/" var="getSiteListByCustomerId" scope="request" />
<spring:url value="/reports/inventoryDetail.ems" var="getDetailInventoryReportUrl" scope="request" />
<style>
#divform{width:100%;}
#form1{float:left;}
#btn1{float:left;}
#btn2{float:left;}
</style>

<script type="text/javascript">
$(document).ready(function() {
    clearLabelMessage();
    $('#searchInventoryReportString').val("");
    $("#searchInventoryReportColumn").val($("#searchInventoryReportColumn option:first").val());
	start('1####END', 1, "name", "asc");
	$("#inventorySummaryTable").setGridWidth($(window).width() - 25);
});

function ModifyIRGridDefaultStyles() {  
	   $('#' + "inventorySummaryTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "inventorySummaryTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "inventorySummaryTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#inventorySummaryTable").setGridWidth($(window).width()-25);	
}

function displayLabelMessage(Message, Color) {
		$("#message").html(Message);
		$("#message").css("color", Color);
}

function clearLabelMessage(Message, Color) {
		displayLabelMessage("", "black");
}

//function for pagination
function start(inputdata, pageNum, orderBy, orderWay) {
		jQuery("#inventorySummaryTable").jqGrid({
			url: "${getSiteListByCustomerId}"+"${customerId}"+"?ts="+new Date().getTime(),
			mtype: "POST",
			postData: {"userData": inputdata},
			datatype: "json",
			autoencode: true,
			hoverrows: false,
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			colNames: ["id", "Name", "Geo Location","Fixture Count", "Sensor Count","Gateway Count","Ballast Count","Lamps Count","Action"],
		       colModel: [
		       { name:'id', index:'id', hidden: true},
		       { name: 'name', index: 'name',sortable:true,sorttype:'string',width:'10%'},
		       { name: "geoLocation", index: 'geoLocation',sortable:true,sorttype:'string',width:'10%'},
		       { name: "fixtureCount", index: 'fixtureCount', sortable:false,width:'6%',search:false},
		       { name: "sensorCount", index: 'sensorCount', sortable:false,sorttype:'string',width:'8%',search:false,},
		       { name: "gatewayCount", index: 'gatewayCount', sortable:false,width:'8%',search:false},
		       { name: "ballastCount", index: 'ballastCount',sortable:false,sorttype:'string',width:'10%',search:false,},
		       { name: "lampsCount", index: 'lampsCount',sortable:false,width:'10%',search:false,},
		       { name: "action", index: 'action',sortable:false,width:'10%', align: "right",formatter: viewDetailsFormatter,search:false,}
		       ],
		   	jsonReader: { 
				root:"siteReport", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    pager: '#inventorySummaryPagingDiv',
		   	page: pageNum,
		   	sortorder: orderWay,
		   	sortname: orderBy,
		   	hidegrid: false,
		    viewrecords: true,
		   	loadui: "block",
		   	toolbar: [false,"top"],
		   	onSortCol: function(index, iCol, sortOrder) {
		   		$('#orderWay').attr('value', sortOrder);
		   		$('#orderBy').attr('value', index);
		   	},
			//footerrow: true,
		   	loadComplete: function(data) {
		   		if (data != null){
		   			if (data.siteReport != undefined) {
				   		if (data.siteReport.length == undefined) {
				   			jQuery("#inventorySummaryTable").jqGrid('addRowData', 0, data.siteReport);
				   		}
				   	}
		   		}
		   		ModifyIRGridDefaultStyles();
// 		   		var $self = $(this),
//              fxsum = $self.jqGrid("getCol", "fixtureCount", false, "sum");
// 		   		sensum = $self.jqGrid("getCol", "sensorCount", false, "sum");
// 		   		gwsum = $self.jqGrid("getCol", "gatewayCount", false, "sum");
// 		   		ballastsum = $self.jqGrid("getCol", "ballastCount", false, "sum");
// 		   		bulbssum = $self.jqGrid("getCol", "lampsCount", false, "sum");
//             	$self.jqGrid("footerData", "set", {name: "Total:", fixtureCount: fxsum,sensorCount:sensum,gatewayCount:gwsum,ballastCount:ballastsum,lampsCount:bulbssum});
		   	}
		   

		});
		jQuery("#inventorySummaryTable").jqGrid('navGrid',"#inventorySummaryPagingDiv",{edit:false,add:false,del:false,search:false});
		forceFitIRTableWidth();
	}
//function for pagination

	function forceFitIRTableWidth(){
		var jgrid = jQuery("#inventorySummaryTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#inventorySummaryPagingDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 10);
		
		$("#inventorySummaryTable").setGridWidth($(window).width() - 25);
	}
	
	function viewDetailsFormatter(cellvalue, options, rowObject) {
		var rowId = rowObject.id;
		return "<div id=\"form1\"><button onclick=\"onDetailInventoryReport("+rowId+");\">Details</button>&nbsp;</div>";
	}
	function onDetailInventoryReport(siteId)
	{
		$('#siteId').val(siteId);
		$("#viewDetailInventoryReport").attr("action", "${getDetailInventoryReportUrl}");
		$('#viewDetailInventoryReport').submit();
	}
	
//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	$("#inventorySummaryTable").setGridWidth($(window).width()-25);
}).trigger('resize');

function exportHighLevelSiteReport()
{
	$('#emInstanceCustomerId').val("${customerId}");
	$('#exportIRForm').submit();
}


function back(){
	window.location = "/ecloud/home.ems";
}

function searchInventoryReportList(){
	
	var userdata = "1" + "#" + $("#searchInventoryReportColumn").val() + "#" +encodeURIComponent($.trim($('#searchInventoryReportString').val())) + "#" + "true" + "#" + "END";
	$("#inventorySummaryTable").jqGrid("GridUnload");
	start(userdata, 1, "name", "desc");
}

function resetInventoryReportList(){
	
	$("#inventorySummaryTable").jqGrid("GridUnload");
	$('#searchInventoryReportString').val("");
	$("#searchInventoryReportColumn").val($("#searchInventoryReportColumn option:first").val());
	var userdata = "1####END";
	start(userdata, 1, "name", "desc");
}

</script>
<spring:url value='/services/inventoryreport/exportreport' var='getExportAggregatedDataUrl' /> 
<form id='exportIRForm' action="${getExportAggregatedDataUrl}" method='POST'>
<input id="emInstanceCustomerId" name="customerId" type="hidden"/>
</form>

<form id='viewDetailInventoryReport' action="${getExportAggregatedDataUrl}" method='POST'>
<input id="siteId" name="siteId" type="hidden"/>
</form>

<div id="emScheduleUpgradeDialog"></div>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="outerDiv" style="padding: 5px 5px 5px 5px">
			<div>
			<div style="float:right;padding: 0px 0px 0px 10px">
	 			<button id="back" onclick="back()">Back</button>
		 	</div>
		 	<div style="float:right;padding: 0px 0px 0px 10px">
		 		<button id="resetInventoryReportButton" onclick="resetInventoryReportList()">Reset</button>
			</div>
			<div style="float:right;padding: 0px 0px 0px 10px">
				<button id="searchInventoryReportButton" onclick="searchInventoryReportList()">Search</button>
			</div>
			<div style="float:right;padding: 0px 0px 0px 10px">
				<input type="text" name="searchInventoryReportString" id="searchInventoryReportString"/>
			</div>
			<div style="float:right;padding: 0px 0px 0px 10px">
				<select id="searchInventoryReportColumn">
				  <option value="name">Name</option>
				  <option value="geoLocation">Geo Location</option>
				</select>
			</div>
			<div style="float:right;padding: 0px 0px 0px 10px;font-weight: bolder; ">
				<label>Search by</label>
			</div>
			<div><button id="invSummaryExportButton" onclick="exportHighLevelSiteReport();">Export</button></div>
			</div>
			
    </div>
	<div style="padding: 0px 5px;">
		<table id="inventorySummaryTable"></table>
		<div id="inventorySummaryPagingDiv"></div>
	</div>
 </div>