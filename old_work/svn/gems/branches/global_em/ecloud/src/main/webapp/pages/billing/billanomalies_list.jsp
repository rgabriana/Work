<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<spring:url value="/sites/filtersiteanomalyprompt.ems" var="filterAnomalyPromptDialogUrl" />
<spring:url value="/services/org/site/v1/getbillanamolieslistbycustomerId/" var="getBillAnomaliesListByCustomerId" scope="request" />
<spring:url value="/scripts/jquery/jquery.datetime.picker.0.9.9.js" var="jquerydatetimepicker"></spring:url>
<spring:url value="/sites/billanomalies.ems" var="billAnomaliesDetailsURL" />
<script type="text/javascript" src="${jquerydatetimepicker}"></script>
<spring:url value="/sites/analysesiteanomaliesbygeoloc.ems" var="getBillAnomaliesListByGeoLocURL" scope="request" />
<style>
#divform{width:100%;}
#form1{float:left;}
#btn1{float:left;}
#btn2{float:left;}
</style>

<script type="text/javascript">
$(document).ready(function() {
    $('#searchBillAnomaliesString').val("");
    $("#searchBillAnomaliesColumn").val($("#searchBillAnomaliesColumn option:first").val());
    var startDate = "${startDate}";
    var endDate =  "${endDate}";
    var userdata = "1" + "#issue#BlockTermRemaining#true#" + startDate + "# " + endDate  +  "# " + "END";
    startBillSiteAnomolies(userdata, 1, "geoLocation", "desc");
	$("#billanamoliesTable").setGridWidth($(window).width() - 25);
});

function ModifyBillSiteGridDefaultStyles() {  
	   $('#' + "billanamoliesTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "billanamoliesTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "billanamoliesTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#billanamoliesTable").setGridWidth($(window).width()-25);	
}

//function for pagination
function startBillSiteAnomolies(inputdata, pageNum, orderBy, orderWay) {
		jQuery("#billanamoliesTable").jqGrid({
			url: "${getBillAnomaliesListByCustomerId}"+"${customerId}"+"?ts="+new Date().getTime(),
			mtype: "POST",
			postData: {"userData": inputdata},
			datatype: "json",
			autoencode: true,
			hoverrows: false,
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			colNames: ["id", "Geo Location", "Report Date","Start Date", "End Date","Issue","Details","Action"],
		       colModel: [
		       { name:'id', index:'id', hidden: true},
		       { name: "geoLocation", index: 'geoLocation', sortable:true,width:'6%'},
		       { name: "reportDate", index: 'reportDate',formatter: 'date', formatoptions: {srcformat:'Y-m-d H:i:s',newformat:'Y-m-d H:i:s'}, sortable:true,width:'6%'},
		       { name: "startDate", index: 'startDate',formatter: 'date', formatoptions: {srcformat:'Y-m-d H:i:s',newformat:'Y-m-d H:i:s'}, sortable:true,width:'6%'},
		       { name: "endDate", index: 'endDate',formatter: 'date', formatoptions: {srcformat:'Y-m-d H:i:s',newformat:'Y-m-d H:i:s'}, sortable:true,width:'6%'},
		       { name: "issue", index: 'issue',sortable:true,width:'6%'},
		       { name: "details", index: 'details',sortable:true,width:'6%'},
		       { name: "action", index: 'action',sortable:false,width:'5%', align: "left",formatter: viewBillAomalyActionFormatter}],
		   	jsonReader: { 
				root:"siteAnomaly", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    pager: '#billAnomoliesPagingDiv',
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
		   			if (data.siteAnomaly != undefined) {
				   		if (data.siteAnomaly.length == undefined) {
				   			jQuery("#billanamoliesTable").jqGrid('addRowData', 0, data.siteAnomaly);
				   		}
				   	}
		   		}
		   		ModifyBillSiteGridDefaultStyles();
		   	}
		});
		jQuery("#billanamoliesTable").jqGrid('navGrid',"#billAnomoliesPagingDiv",{edit:false,add:false,del:false,search:false});
		forceFitBillSiteAnomaliesTableWidth();
	}
	//function for pagination

	function forceFitBillSiteAnomaliesTableWidth(){
		var jgrid = jQuery("#billanamoliesTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#billAnomoliesPagingDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 10);
		
		$("#billanamoliesTable").setGridWidth($(window).width() - 25);
	}
	
	
//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	$("#billanamoliesTable").setGridWidth($(window).width()-25);
}).trigger('resize');

function viewBillAomalyActionFormatter(cellvalue, options, rowObject) {
	var geoLocation = rowObject.geoLocation;
	var startDate = rowObject.billStartDate;
	var endDate = rowObject.billEndDate;
	return "<div id=\"billAnomalydivform\" class=\"aParent\"><div id=\"billAnalysisBtn\"><button onclick=\"javascript: analyseBillAnomaly('"+geoLocation+"','"+startDate+"','"+endDate+"');\">Analysis</button>&nbsp;</div>";
}
// This will display all the above anomalies of that site during that month period.
function analyseBillAnomaly(geoLocation,startDate,endDate)
{
	$("#bageoLocation").val(geoLocation);
		
	var billstartdate = $.cookie('billstartdate');
	var billenddate = $.cookie('billenddate');
 	$("#billanamoliesTable").jqGrid("GridUnload");

 	billstartdate = decodeURIComponent($.trim(billstartdate));
 	billstartdate = billstartdate.replace(/\+/g," ");
 	
 	billenddate = decodeURIComponent($.trim(billenddate));
 	billenddate = billenddate.replace(/\+/g," ");
	
	var startDateStr = getCustomDateFormatString(billstartdate);
	var endDateStr = getCustomDateFormatString(billenddate);
	$('#bastartDateId').val(startDateStr);
	$('#baendDateId').val(endDateStr);
	$("#analyseBillAnomalyForm").attr("action", "${getBillAnomaliesListByGeoLocURL}");
	$('#analyseBillAnomalyForm').submit();
}
function searchBillAnomaliesList(){
	var userdata = "1" + "#" + $("#searchBillAnomaliesColumn").val() + "#" +encodeURIComponent($.trim($('#searchBillAnomaliesString').val())) + "#" + "true" + "#" + "${startDate}" + "# " + "${endDate}"  + "# "+"END";
	$("#billanamoliesTable").jqGrid("GridUnload");
	startBillSiteAnomolies(userdata, 1, "geoLocation", "desc");
}
function getCustomDateFormatString(bDate)
{
	var dd = bDate.substring(8,10);
	var mm = bDate.substring(5,7);
	var yyyy = bDate.substring(0,4);
	var billdateStr = mm+'/'+dd+'/'+yyyy; 
	return billdateStr;
}
function resetBillAnomaliesList(){
		var billstartdate = $.cookie('billstartdate');
		var billenddate = $.cookie('billenddate');
	 	$("#billanamoliesTable").jqGrid("GridUnload");

	 	billstartdate = decodeURIComponent($.trim(billstartdate));
	 	billstartdate = billstartdate.replace(/\+/g," ");
	 	
	 	billenddate = decodeURIComponent($.trim(billenddate));
	 	billenddate = billenddate.replace(/\+/g," ");

	 	$('#searchBillAnomaliesString').val("");
	 	$("#searchBillAnomaliesColumn").val($("#searchBillAnomaliesColumn option:first").val());
	 	var userdata = "1" + "#issue#BlockTermRemaining#true#" + billstartdate + "# " + billenddate  +  "# " + "END";
	 	//console.log(billstartdate + " " + billenddate);
	 	startBillSiteAnomolies(userdata, 1, "geoLocation", "desc");
}

function applyBillAnomalyFilterBtn()
{
	$("#billAnomaliesFormDialog").load("${filterAnomalyPromptDialogUrl}?id="+"${customerId}"+"&mode=bill&ts="+new Date().getTime(), function() {
		  $("#billAnomaliesFormDialog").dialog({
				modal:true,
				title: 'Filter Anomaly',
				width : 500,
				height : 200,
				closeOnEscape: false,
				open: function(event, ui) {
				},
			});
		});
}
</script>

<form id="analyseBillAnomalyForm" target="_self" METHOD="POST">
	<input id="bageoLocation" name="bageoLocation" type="hidden"/>
	<input id="bastartDateId" name="bastartDateId" type="hidden"/>
	<input id="baendDateId" name="baendDateId" type="hidden"/>
</form>

<div id="billAnomaliesFormDialog"></div>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="outerDiv" style="padding: 5px 5px 5px 5px">
			<div>
			<div style="font-weight: bolder;padding-bottom: 10px; "><spring:message code="allsites.anomalies"/> ${customerName}</div>
				
					<div style="float:right;padding: 0px 0px 0px 10px">
						<button id="resetBillAnomaliesButton" onclick="resetBillAnomaliesList()">Reset</button>
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px">
						<button id="searchBillAnomaliesButton" onclick="searchBillAnomaliesList()">Search</button>
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px">
						<input type="text" name="searchBillAnomaliesString" id="searchBillAnomaliesString">
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px">
						<select id="searchBillAnomaliesColumn">
						  <option value="geoLocation">Geo Location</option>
						  <option value="issue">Issue</option>
						  <option value="details">Details</option>
						</select>
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px;font-weight: bolder; ">
						<label>Search by</label>
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px">
						<button id="applyBillAnomalyFilterBtn" onclick="applyBillAnomalyFilterBtn()">Filter Anomaly</button>
					</div>
					<spring:url value="/bill/emBillingMain.ems" var="emBillingMainUrl" />
					<div id="divAnamaliesform" style="padding: 0px 10px 0px 0px"><div id="anomaliesform1"><form action="${emBillingMainUrl}" method=POST name="anomaliesform1" ><input type="hidden" name="customerId" value ="${customerId}" ></input><button onclick="document.anomaliesform1.submit();">Return Billing Details</button></form></div></div>
			</div>	
    </div>
	<div style="padding: 0px 5px;">
		<table id="billanamoliesTable"></table>
		<div id="billAnomoliesPagingDiv"></div>
	</div>
 </div>