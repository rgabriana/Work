<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/eminstance/billinglistbycutomerid/" var="getEmInstanceBillList" scope="request" />
<spring:url value="/bill/reGenerateCustomerBill.ems" var="reGenerateCustomerBill" scope="request" />
<spring:url value="/bill/customerBilling.ems" var="newBillPromptDialogUrl" />
<spring:url value="/bill/generateCustomerReport.ems" var="customerReportUrl" />
<spring:url value="/pdfbills/downloadCustomerBill.ems" var="downloadCustomerBillUrl" />
<spring:url value="/services/sppa/getCurrentBillRunningStatus" var="checkBillingProcessRunningStatusUrl" scope="request" />
<spring:url value="/sites/billanomalies.ems" var="billAnomaliesDetailsURL" />
<style>

.aParent div {
  float: left;
}
#billform1{float:center;}

.jqgrid-rownum
{
    background: none !important;
    border: none !important;
    color: #000000 !important;
    font-weight: bold;
}
</style>
<spring:url value="/scripts/jquery/jquery.datetime.picker.0.9.9.js" var="jquerydatetimepicker"></spring:url>
<script type="text/javascript" src="${jquerydatetimepicker}"></script>
<script type="text/javascript">
var lastBillDate;
var totalBillCount=0;
$(document).ready(function() {
	
	var billstartdate = $.cookie('billstartdate');
	var billenddate = $.cookie('billenddate');
	if(billstartdate!=null)
	{
		$.cookie('billstartdate',"",  {path: '/' });
	}
	if(billenddate!=null)
	{
		$.cookie('billenddate', "",  {path: '/' });
	}
	
    clearLabelMessage();
	start(1, "desc");
	$("#emInstanceBillingTable").setGridWidth($(window).width() - 25);
    $('#generateNewBillButton').click(function() {
    	
    	clearLabelMessage();
    	var customerId = "${customerId}";
    	
    	$.ajax({
    		type: "GET",
    		contentType: "application/json; charset=utf-8",
    		url: '<spring:url value="/services/org/site/v1/checksiteemlisttbycustomerid/"/>'+customerId,
    		dataType: "json",
    		success: function(data) {
    			if(data.status == 0) {
    				alert("Bill cannot be generated as this customer doesn't have any associated sites or the sites doesn't have any assigned Energy Managers.");
    			}else{
    				if(lastBillDate!=null)
    		   		{
    		    		var billdateStr = getCustomDateFormatString(lastBillDate);
    		   		}
    		    	//console.log(totalBillCount);
    		    	
    		    	$("#emInstanceBillDialog").load("${newBillPromptDialogUrl}?customerId="+customerId+"&lastBillDate="+billdateStr + "&totalBillCount="+totalBillCount+"&ts="+new Date().getTime(), function() {
    		  		  $("#emInstanceBillDialog").dialog({
    		  				modal:true,
    		  				title: 'Billing',
    		  				width : 500,
    		  				height : 200,
    		  				closeOnEscape: false,
    		  				open: function(event, ui) {
    		  				},
    		  				
    		  			});
    		  		});
    		        return false;
    			}
    		}
    	});
    		
    	
    });
});

function reloadBillingTable()
{
		$("#emInstanceBillingTable").trigger("reloadGrid");
}
function ModifyGridDefaultStyles() {  
	   $('#' + "emInstanceBillingTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "emInstanceBillingTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "emInstanceBillingTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#emInstanceBillingTable").setGridWidth($(window).width()-25);	
}

function displayLabelMessage(Message, Color) {
		$("#billlist_message").html(Message);
		$("#billlist_message").css("color", Color);
}

function clearLabelMessage(Message, Color) {
		displayLabelMessage("", "black");
}

function downloadBill(rowId,startDate,endDate)
{
	var customerId = "${customerId}";
	$("#dcbfCustomerId").val(customerId);
	$("#dcbfCustomerBillId").val(rowId);
	var startDateStr = getCustomDateFormatString(startDate);
	var endDateStr = getCustomDateFormatString(endDate);
	$('#dcbfstartDateId').val(startDateStr);
	$('#dcbfendDateId').val(endDateStr);
	$('#downloadCustomerBillForm').submit();
}
// SHOW BILL ANOMALY REPORT PAGE
function showBillAnomaliesDetail(rowId,startDate,endDate)
{
	//Reusing existing Customer Report form and changing action URL for Bill Anomaly Report Page
	var customerId = "${customerId}";
	$("#crfCustomerId").val(customerId);
	$("#crfCustomerBillId").val(rowId);
	var startDateStr = getCustomDateFormatString(startDate);
	var endDateStr = getCustomDateFormatString(endDate);
	
	$('#crfstartDateId').val(startDateStr);
	$('#crfendDateId').val(endDateStr);
	$("#customerReportForm").attr("action", "${billAnomaliesDetailsURL}");
	$("#customerReportForm").attr("target", "_parent");
	$('#customerReportForm').submit();
	//alert(top.location);

}
function showReport(rowId,startDate,endDate)
{
	var customerId = "${customerId}";
	$("#crfCustomerId").val(customerId);
	
	$("#crfCustomerBillId").val(rowId);
	
	var startDateStr = getCustomDateFormatString(startDate);
	var endDateStr = getCustomDateFormatString(endDate);
	
	$('#crfstartDateId').val(startDateStr);
	$('#crfendDateId').val(endDateStr);
	
	//console.log(startDate + " == " + endDate);
	$('#customerReportForm').submit();
}

function getCustomDateFormat(bDate)
{
	var billdate = new Date(bDate);
	var dd = billdate.getDate(); 
	var mm = billdate.getMonth()+1; 
	var yyyy = billdate.getFullYear(); 
	if(dd<10)
	{
		dd='0'+dd;
	} 
	if(mm<10)
	{
		mm='0'+mm;
	} 
	var billdateStr = mm+'/'+dd+'/'+yyyy; 

	return billdateStr;
}


function getCustomDateFormatString(bDate)
{
	var dd = bDate.substring(8,10);
	var mm = bDate.substring(5,7);
	var yyyy = bDate.substring(0,4);
	var billdateStr = mm+'/'+dd+'/'+yyyy; 
	return billdateStr;
}

//function for pagination
function start(pageNum, orderWay) {
		jQuery("#emInstanceBillingTable").jqGrid({
			url: "${getEmInstanceBillList}"+"${customerId}"+"?ts="+new Date().getTime(),
			mtype: "POST",
			datatype: "json",
			autoencode: true,
			hoverrows: false,
			autowidth: true,
			rownumbers: true,
			scrollOffset: 0,
			forceFit: true,
			formatter: {
				 integer: {thousandsSeparator: ",", defaultValue: '0'},
			     number: {decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, defaultValue: '0.00'}
			},
			colNames: ["id", "Start Date", "End Date","Bill Generation Date","Action"],
		       colModel: [
		       { name:'id', index:'id', hidden: true},
		       { name: 'billStartDate', index: 'billStartDate',sorttype:'string', formatter:billinglistcustomdateformatter, align: "center", width:'10%' },
		       { name: "billEndDate", index: 'billEndDate', width:'10%',formatter:billinglistcustomdateformatter,align: "center" },
		       { name: "billCreationTime", index: 'billCreationTime', width:'10%',formatter:billinglistcustomdateformatter,align: "center" },
		       { name: "action", index: 'action',sortable:false,width:'15%', align: "center",formatter: viewActionFormatter}],
		       
		   	jsonReader: { 
				root:"customerSppaBill", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    pager: '#emInstanceBillPagingDiv',
		   	page: pageNum,
		   	sortorder: orderWay,
		   	sortname: "billCreationTime",
		    hidegrid: false,
		    viewrecords: true,
		   	loadui: "block",
		   	toolbar: [false,"top"],
		   	onSortCol: function(index, iCol, sortOrder) {
		   	},
		   	loadComplete: function(data) {
		   		if (data != null){
		   			if(data.customerSppaBill.length>1)
		   			{
		   				totalBillCount = data.customerSppaBill.length;
		   				lastBillDate = data.customerSppaBill[0].billEndDate;
		   			}
		   			//FIXME : JSON response issue : For only 1 record it returns Object instead of an Array
		   			if (data.customerSppaBill != undefined) {
				   		if (data.customerSppaBill.length == undefined) {
				   			jQuery("#emInstanceBillingTable").jqGrid('addRowData', 0, data.customerSppaBill);
				   			lastBillDate = data.customerSppaBill.billEndDate;
				   			totalBillCount = 1;
				   		}
				   	}
		   		}
		   		//alert(lastBillDate);
		   		ModifyGridDefaultStyles();
		   		
		   	}

		});
		
		jQuery("#emInstanceBillingTable").jqGrid('navGrid',"#emInstancePagingDiv",
										{edit:false,add:false,del:false,search:false}, 
										{}, 
										{}, 
										{}, 
										{},
										{});
		
		forceFitEmInstanceTableWidth();
	}
//function for pagination

	function billinglistcustomdateformatter(cellvalue, options, rowObject)
	{
		  var date = new Date(cellvalue);
		  options = $.extend({}, $.jgrid.formatter.date, options);
          return $.fmatter.util.DateFormat("", date, 'n/j/Y', options);
	}
	
	function forceFitEmInstanceTableWidth(){
		var a = parent.document.body.clientHeight;  		
		var jgrid = jQuery("#emInstanceBillingTable");
		var containerHeight =a;
		var otherElementHeight = $("#outerDiv").height();		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();		
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();		
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;		
		var mHeight = containerHeight - otherElementHeight - gridHeaderFooterHeight;		
		jgrid.jqGrid("setGridHeight", Math.floor((mHeight) * .77)); 
		$("#emInstanceBillingTable").setGridWidth($(window).width() - 25);
	}

	function viewActionFormatter(cellvalue, options, rowObject) {
		var rowId = rowObject.id;
		var startDate = rowObject.billStartDate;
		var endDate = rowObject.billEndDate;
		// EDAC-412 : Reverting chnages to original one : As Sreedhar has fixed the real issue in backend code.
		return "<div id=\"billdivform\" class=\"aParent\"><div id=\"generatebtn1\"><button onclick=\"javascript: regenerateBill('"+rowId+"');\">Regenerate</button>&nbsp;</div><div id=\"generatebtn2\"><button onclick=\"javascript: showReport('"+rowId+"','"+startDate+"','"+endDate+"');\">View</button>&nbsp;</div><div id=\"generatebtn3\"><button onclick=\"javascript: downloadBill('"+rowId+"','"+startDate+"','"+endDate+"');\">Download</button>&nbsp;</div><div id=\"generatebtn4\"><button onclick=\"javascript: showBillAnomaliesDetail('"+rowId+"','"+startDate+"','"+endDate+"');\">Bill Anomaly Report</button>&nbsp;</div></div>";
	}
	function submitERegenerateBillForm(rowId)
	 {
	 	var customerId = "${customerId}";
	 	$("#rgbCustomerId").val(customerId);
	 	$("#rgbCustomerBillId").val(rowId);
	 	$('#reGenerateBillForm').submit();
	 }
	function regenerateBill(rowId) {
		$.ajax({
				type : 'GET',
				url : "${checkBillingProcessRunningStatusUrl}?ts="+new Date().getTime(),
				success : function(data) {
					var status = data.status;
					//Ajax call to check whether any bill process is still under progress. If yes restrict the user to generate the further bill till previous bill process is completed.
					if(status=="0")
					{
						submitERegenerateBillForm(rowId);
					}else
					{
						displayLabelMessage('Bill is already being created. Please retry after some time','red');
						setTimeout("clearLabelMessage()", 10000);
					}
				},
				dataType : "json",
			});
	}
		
//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	$("#emInstanceBillingTable").setGridWidth($(window).width()-25);
}).trigger('resize');

</script>
<div id="emInstanceBillDialog"></div>
 
<form id="reGenerateBillForm" action="${reGenerateCustomerBill}" target="_blank" METHOD="POST">
 	<input id="rgbCustomerId" name="rgbCustomerId" type="hidden"/>
	<input id="rgbCustomerBillId" name="rgbCustomerBillId" type="hidden"/>
</form>
<form id="customerReportForm" action="${customerReportUrl}" target="_blank" METHOD="POST">
	<input id="crfCustomerId" name="crfCustomerId" type="hidden"/>
	<input id="crfCustomerBillId" name="crfCustomerBillId" type="hidden"/>
	<input id="crfstartDateId" name="crfstartDateId" type="hidden"/>
	<input id="crfendDateId" name="crfendDateId" type="hidden"/>
</form>

<form id="downloadCustomerBillForm" action="${downloadCustomerBillUrl}" target="_blank" METHOD="POST">
	<input id="dcbfCustomerId" name="dcbfCustomerId" type="hidden"/>
	<input id="dcbfCustomerBillId" name="dcbfCustomerBillId" type="hidden"/>
	<input id="dcbfstartDateId" name="dcbfstartDateId" type="hidden"/>
	<input id="dcbfendDateId" name="dcbfendDateId" type="hidden"/>
</form>


<div style="width: 100%;">
	<div id="outerDiv">
		<div style="font-weight: bolder;padding-top: 5px;padding-bottom: 5px;"><spring:message code="eminstance.bill.header"/>${customerName}
		</div>
		<table>
		<tr>
	    <td><button id="generateNewBillButton">Generate Bill</button>	</td>
		<td><div id="billlist_message" style="font-size: 14px; font-weight: bold;padding-left: 10px;" ></div></td>
		</tr>
		</table>	
		<div style="min-height:5px"></div>
	</div>
	<div style="overflow:auto">
		<table id="emInstanceBillingTable"></table>
		<div id="emInstanceBillPagingDiv"></div>
	</div>
 </div>
