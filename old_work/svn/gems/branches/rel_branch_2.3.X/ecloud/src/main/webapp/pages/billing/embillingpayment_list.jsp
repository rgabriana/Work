<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/bills/paymentlistbycutomerid/" var="getBillPaymentListURL" scope="request" />
<spring:url value="/bill/billingPaymentPromt.ems" var="billPaymentPromtURL" />

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
    clearLabelMessage();
	start(1, "desc");
	$("#emInstanceBillingTable").setGridWidth($(window).width() - 25);
    $('#paymentBtn').click(function() {
    	    	
    	clearLabelMessage();
    	var titleStr = 'Bill Payment for '+"${customerName}";
    	var customerId = "${customerId}";
    	$("#BillPaymentDialog").load("${billPaymentPromtURL}?customerId="+customerId+"&ts="+new Date().getTime(), function() {
  		  $("#BillPaymentDialog").dialog({
  				modal:true,
  				title: titleStr,
  				width : 500,
  				height : 150,
  				closeOnEscape: false,
  				open: function(event, ui) {
  				},
  			});
  		});
        return false;
    });
});


function ModifyGridDefaultStyles() {  
	   $('#' + "emInstanceBillingTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "emInstanceBillingTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "emInstanceBillingTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#emInstanceBillingTable").setGridWidth($(window).width()-25);	
}

function displayLabelMessage(Message, Color) {
		$("#message").html(Message);
		$("#message").css("color", Color);
}

function clearLabelMessage(Message, Color) {
		displayLabelMessage("", "black");
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
		jQuery("#billingPaymentTable").jqGrid({
			url: "${getBillPaymentListURL}"+"${customerId}"+"?ts="+new Date().getTime(),
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
			colNames: ["id", "Customer ID", "Payment Date","Bill Paid Amount ($)"],
		       colModel: [
		       { name:'id', index:'id', hidden: true},
		       { name: 'customer.id', index: 'customer.id',sorttype:'string', align: "center", width:'10%',hidden: true },
		       { name: "paymentDate", index: 'paymentDate', width:'10%',formatter:'date',align: "center" },
		       { name: "paymentAmount", index: 'paymentAmount', width:'10%',formatter:'currency',align: "center" }],
		       
		   	jsonReader: { 
				root:"billPayments", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    pager: '#billingPaymentPagingDiv',
		   	page: pageNum,
		   	sortorder: orderWay,
		   	sortname: "paymentDate",
		    hidegrid: false,
		    viewrecords: true,
		   	loadui: "block",
		   	toolbar: [false,"top"],
		   	onSortCol: function(index, iCol, sortOrder) {
		   	},
		   	loadComplete: function(data) {
		   		if (data != null){
		   			//FIXME : JSON response issue : For only 1 record it returns Object instead of an Array
		   			if (data.billPayments != undefined) {
				   		if (data.billPayments.length == undefined) {
				   			jQuery("#billingPaymentTable").jqGrid('addRowData', 0, data.billPayments);
				   		}
				   	}
		   		}
		   		ModifyGridDefaultStyles();
		   		
		   	}

		});
		
		jQuery("#billingPaymentTable").jqGrid('navGrid',"#billingPaymentPagingDiv",
										{edit:false,add:false,del:false,search:false}, 
										{}, 
										{}, 
										{}, 
										{},
										{});
		
		forceFitEmInstanceTableWidth();
	}
//function for pagination

	function forceFitEmInstanceTableWidth(){
		var a = parent.document.body.clientHeight;  		
		var jgrid = jQuery("#billingPaymentTable");
		var containerHeight =a;
		var otherElementHeight = $("#outerDiv").height();		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();		
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();		
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;		
		var mHeight = containerHeight - otherElementHeight - gridHeaderFooterHeight;		
		jgrid.jqGrid("setGridHeight", Math.floor((mHeight) * .77)); 
		$("#billingPaymentTable").setGridWidth($(window).width() - 25);
	}
	
//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	$("#billingPaymentTable").setGridWidth($(window).width()-25);
}).trigger('resize');

</script>
<div id="BillPaymentDialog"></div>
 
<form id="billPaymentForm" action="${billPaymentURl}"  METHOD="POST">
 	<input id="bpCustomerId" name="bpCustomerId" type="hidden"/>
</form>

<div style="width: 100%;">
	<div id="outerDiv">
		<div style="font-weight: bolder;padding-top: 5px;padding-bottom: 5px;"><spring:message code="billPayment.header"/> ${customerName}
		</div>
		<button id="paymentBtn">Pay Bill</button><br>	
		<div style="min-height:5px"></div>
	</div>
	<div style="overflow:auto">
		<table id="billingPaymentTable"></table>
		<div id="billingPaymentPagingDiv"></div>
	</div>
 </div>
