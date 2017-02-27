<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<spring:url value="/sites/filtersiteanomalyprompt.ems" var="filterAnomalyPromptDialogUrl" />
<spring:url value="/services/org/site/v1/getallanamolieslistbycustomerId/" var="getAllAnomaliesListByCustomerId" scope="request" />
<spring:url value="/scripts/jquery/jquery.datetime.picker.0.9.9.js" var="jquerydatetimepicker"></spring:url>
<spring:url value="/sites/viewallanomaliesdetails.ems" var="getSiteAnomaliesDetailUrl" scope="request" />
<script type="text/javascript" src="${jquerydatetimepicker}"></script>
<style>
#divform{width:100%;}
#form1{float:left;}
#btn1{float:left;}
#btn2{float:left;}
</style>

<script type="text/javascript">
$(document).ready(function() {
    $('#searchAllAnomaliesString').val("");
    $("#searchAllAnomaliesColumn").val($("#searchAllAnomaliesColumn option:first").val());
    var startDate = "${startDate}";
    var endDate =  "${endDate}";
    var userdata = "1" + "####" + startDate + "# " + endDate  +  "# " + "END";
    startAllSiteAnomolies(userdata, 1, "geoLocation", "desc");
	$("#allanamoliesTable").setGridWidth($(window).width() - 25);
});

function ModifyAllSiteGridDefaultStyles() {  
	   $('#' + "allanamoliesTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "allanamoliesTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "allanamoliesTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#allanamoliesTable").setGridWidth($(window).width()-25);	
}

//function for pagination
function startAllSiteAnomolies(inputdata, pageNum, orderBy, orderWay) {
		jQuery("#allanamoliesTable").jqGrid({
			url: "${getAllAnomaliesListByCustomerId}"+"${customerId}"+"?ts="+new Date().getTime(),
			mtype: "POST",
			postData: {"userData": inputdata},
			datatype: "json",
			autoencode: true,
			hoverrows: false,
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			colNames: ["id", "Geo Location", "Report Date", "Start Date", "End Date", "Issue","Details"],
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
		    pager: '#allAnomoliesPagingDiv',
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
				   			jQuery("#allanamoliesTable").jqGrid('addRowData', 0, data.siteAnomaly);
				   		}
				   	}
		   		}
		   		ModifyAllSiteGridDefaultStyles();
		   	}
		});
		jQuery("#allanamoliesTable").jqGrid('navGrid',"#allAnomoliesPagingDiv",{edit:false,add:false,del:false,search:false});
		forceFitAllSiteAnomaliesTableWidth();
	}
	//function for pagination

	function forceFitAllSiteAnomaliesTableWidth(){
		var jgrid = jQuery("#allanamoliesTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#allAnomoliesPagingDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 10);
		
		$("#allanamoliesTable").setGridWidth($(window).width() - 25);
	}
	
	
//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	$("#allanamoliesTable").setGridWidth($(window).width()-25);
}).trigger('resize');

function searchAllAnomaliesList(){
	var userdata = "1" + "#" + $("#searchAllAnomaliesColumn").val() + "#" +encodeURIComponent($.trim($('#searchAllAnomaliesString').val())) + "#" + "true" + "### "+"END";
	$("#allanamoliesTable").jqGrid("GridUnload");
	startAllSiteAnomolies(userdata, 1, "geoLocation", "desc");
}

function resetAllAnomaliesList(){
	 $('#customerId').val(${customerId});
	 $('#viewAllSiteAnamoliesDetailForm1').submit();
}

function ApplyAllAnomalyFilterBtn()
{
	$("#AllsiteAnomaliesFormDialog").load("${filterAnomalyPromptDialogUrl}?id="+"${customerId}"+"&mode=customer&ts="+new Date().getTime(), function() {
		  $("#AllsiteAnomaliesFormDialog").dialog({
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
<form id='viewAllSiteAnamoliesDetailForm1' action="${getSiteAnomaliesDetailUrl}" method='POST'>
<input id="customerId" name="customerId" type="hidden"/>
</form>

<div id="AllsiteAnomaliesFormDialog"></div>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="outerDiv" style="padding: 5px 5px 5px 5px">
			<div>
			<div style="font-weight: bolder;padding-bottom: 10px; "><spring:message code="allsites.anomalies"/> ${customerName}</div>
				
					<div style="float:right;padding: 0px 0px 0px 10px">
						<button id="resetAllAnomaliesButton" onclick="resetAllAnomaliesList()">Reset</button>
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px">
						<button id="searchAllAnomaliesButton" onclick="searchAllAnomaliesList()">Search</button>
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px">
						<input type="text" name="searchAllAnomaliesString" id="searchAllAnomaliesString">
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px">
						<select id="searchAllAnomaliesColumn">
						  <option value="geoLocation">Geo Location</option>
						  <option value="issue">Issue</option>
						  <option value="details">Details</option>
						</select>
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px;font-weight: bolder; ">
						<label>Search by</label>
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px">
						<button id="applyAllAnomalyFilterBtn" onclick="ApplyAllAnomalyFilterBtn()">Filter Anomaly</button>
					</div>
					<div id="divAnamaliesform" style="padding: 0px 10px 0px 0px"><div id="anomaliesform1"><form action="list.ems" method=POST name="anomaliesform1" ><input type="hidden" name="customerId" value ="${customerId}" ></input><button onclick="document.form1.submit();">Site List</button></form></div></div>
			</div>	
    </div>
	<div style="padding: 0px 5px;">
		<table id="allanamoliesTable"></table>
		<div id="allAnomoliesPagingDiv"></div>
	</div>
 </div>