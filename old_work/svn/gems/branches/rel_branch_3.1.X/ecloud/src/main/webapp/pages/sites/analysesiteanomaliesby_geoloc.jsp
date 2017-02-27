<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<spring:url value="/sites/filtersiteanomalyprompt.ems" var="filterAnomalyPromptDialogUrl" />
<spring:url value="/services/org/site/v1/getallanamolieslistbysiteid/" var="getallanamolieslistbysiteid" scope="request" />
<spring:url value="/scripts/jquery/jquery.datetime.picker.0.9.9.js" var="jquerydatetimepicker"></spring:url>
<spring:url value="/sites/viewsiteanoaliesdetails.ems" var="loadsitesAnomalylistUrl" />
<script type="text/javascript" src="${jquerydatetimepicker}"></script>
<style>
#divform{width:100%;}
#form1{float:left;}
#btn1{float:left;}
#btn2{float:left;}
</style>

<script type="text/javascript">
var mode = "${analyseAnomalyiew}";
$(document).ready(function() {
    $('#searchAnalyseAnomaliesString').val("");
    $("#searchAnalyseAnomaliesColumn").val($("#searchAnalyseAnomaliesColumn option:first").val());
    var startDate = "${startDate}";
    var endDate = "${endDate}";
    var userdata = "1" + "####" + startDate + "# " + endDate  +  "# " + "END";
    startAnalyseSiteAnomolies(userdata, 1, "geoLocation", "desc");
	$("#analysesiteanamoliesTable").setGridWidth($(window).width() - 25);
});

function ModifyAnalyseSiteGridDefaultStyles() {  
	   $('#' + "analysesiteanamoliesTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "analysesiteanamoliesTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "analysesiteanamoliesTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#analysesiteanamoliesTable").setGridWidth($(window).width()-25);	
}

//function for pagination
function startAnalyseSiteAnomolies(inputdata, pageNum, orderBy, orderWay) {
		jQuery("#analysesiteanamoliesTable").jqGrid({
			url: "${getallanamolieslistbysiteid}"+"${siteId}"+"?ts="+new Date().getTime(),
			mtype: "POST",
			postData: {"userData": inputdata},
			datatype: "json",
			autoencode: true,
			hoverrows: false,
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			colNames: ["id", "Geo Location", "Report Date","Start Date", "End Date","Issue","Details"],
		       colModel: [
		       { name:'id', index:'id', hidden: true},
		       { name: "geoLocation", index: 'geoLocation', sortable:true,width:'6%'},
		       { name: "reportDate", index: 'reportDate',formatter: 'date', formatoptions: {srcformat:'Y-m-d H:i:s',newformat:'Y-m-d H:i:s'}, sortable:true,width:'6%'},
		       { name: "startDate", index: 'startDate',formatter: 'date', formatoptions: {srcformat:'Y-m-d H:i:s',newformat:'Y-m-d H:i:s'}, sortable:true,width:'6%'},
		       { name: "endDate", index: 'endDate',formatter: 'date', formatoptions: {srcformat:'Y-m-d H:i:s',newformat:'Y-m-d H:i:s'}, sortable:true,width:'6%'},
		       { name: "issue", index: 'issue',sortable:true,width:'6%'},
		       { name: "details", index: 'details',sortable:true,width:'20%'},
		       ],
		   	jsonReader: { 
				root:"siteAnomaly", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: true },
		    pager: '#analysesiteAnomoliesPagingDiv',
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
				   			jQuery("#analysesiteanamoliesTable").jqGrid('addRowData', 0, data.siteAnomaly);
				   		}
				   	}
		   		}
		   		ModifyAnalyseSiteGridDefaultStyles();
		   	}
		});
		jQuery("#analysesiteanamoliesTable").jqGrid('navGrid',"#analysesiteAnomoliesPagingDiv",{edit:false,add:false,del:false,search:false});
		forceFitAnalyseSiteAnomaliesTableWidth();
	}
	//function for pagination

	function forceFitAnalyseSiteAnomaliesTableWidth(){
		var jgrid = jQuery("#analysesiteanamoliesTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#analysesiteAnomoliesPagingDiv").height();
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 10	);
		$("#analysesiteanamoliesTable").setGridWidth($(window).width() - 25);
	}
	
	
//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	$("#analysesiteanamoliesTable").setGridWidth($(window).width()-25);
}).trigger('resize');

function searchAnomaliesList(){
var billstartdate = $.cookie('billstartdate');
	var billenddate = $.cookie('billenddate');
	billstartdate = decodeURIComponent($.trim(billstartdate));
 	billstartdate = billstartdate.replace(/\+/g," ");
 	
 	billenddate = decodeURIComponent($.trim(billenddate));
 	billenddate = billenddate.replace(/\+/g," ");
	var userdata = "1" + "#" + $("#searchAnalyseAnomaliesColumn").val() + "#" +encodeURIComponent($.trim($('#searchAnalyseAnomaliesString').val())) + "#" + "true" + "#" + billstartdate + "# " + billenddate  + "# "+"END";
	$("#analysesiteanamoliesTable").jqGrid("GridUnload");
	startAnalyseSiteAnomolies(userdata, 1, "geoLocation", "desc");
}

function resetAnomaliesList(){
		var billstartdate = $.cookie('billstartdate');
		var billenddate = $.cookie('billenddate');
		billstartdate = decodeURIComponent($.trim(billstartdate));
	 	billstartdate = billstartdate.replace(/\+/g," ");
	 	
	 	billenddate = decodeURIComponent($.trim(billenddate));
	 	billenddate = billenddate.replace(/\+/g," ");
	 	
		$("#analysesiteanamoliesTable").jqGrid("GridUnload");
		$('#searchAnalyseAnomaliesString').val("");
		$("#searchAnalyseAnomaliesColumn").val($("#searchAnalyseAnomaliesColumn option:first").val());
		var userdata = "1" + "####" + billstartdate + "# " + billenddate  +  "# " + "END";
		startAnalyseSiteAnomolies(userdata, 1, "geoLocation", "desc");
}
function ApplyAnomalyFilterBtn()
{
	$("#siteAnomaliesFormDialog").load("${filterAnomalyPromptDialogUrl}?id="+"${siteId}"+"&mode=analysis&ts="+new Date().getTime(), function() {
		  $("#siteAnomaliesFormDialog").dialog({
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
 <form id='viewSiteAnamoliesDetailForm1' action="${loadsitesAnomalylistUrl}" method='POST'>
<input id="siteId" name="siteId" type="hidden"/>
</form>

<div id="siteAnomaliesFormDialog"></div>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="outerDiv" style="padding: 5px 5px 5px 5px">
			<div>
			<div style="font-weight: bolder;padding-bottom: 10px;"><spring:message code="sites.anomalies"/> ${siteName}</div>
				<div>
					<div style="float:right;padding: 0px 0px 0px 10px">
						<button id="resetAnomaliesButton" onclick="resetAnomaliesList()">Reset</button>
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px">
						<button id="searchAnomaliesButton" onclick="searchAnomaliesList()">Search</button>
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px">
						<input type="text" name="searchAnalyseAnomaliesString" id="searchAnalyseAnomaliesString">
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px">
						<select id="searchAnalyseAnomaliesColumn">
						  <option value="geoLocation">Geo Location</option>
						  <option value="issue">Issue</option>
						  <option value="details">Details</option>
						</select>
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px;font-weight: bolder; ">
						<label>Search by</label>
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px">
						<button id="applyAnomalyFilterBtn" onclick="ApplyAnomalyFilterBtn()">Filter Anomaly</button>
					</div>
						<spring:url value="/bill/emBillingMain.ems" var="emBillingMainUrl" />
					<div id="divAnamaliesform" style="padding: 0px 10px 0px 0px"><div id="anomaliesform1"><form action="${emBillingMainUrl}" method=POST name="anomaliesform1" ><input type="hidden" name="customerId" value ="${customerId}" ></input><button onclick="document.anomaliesform1.submit();">Return Billing Details</button></form></div></div>
				</div>
			</div>	
    </div>
	<div style="padding: 0px 5px;">
		<table id="analysesiteanamoliesTable"></table>
		<div id="analysesiteAnomoliesPagingDiv"></div>
	</div>
 </div>