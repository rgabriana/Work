<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<spring:url value="/bill/billingListDetails.ems" var="billingList" />

<spring:url value="/bill/billingPaymentDetails.ems" var="billPaymentList" />


<style type="text/css">
	.no-close .ui-dialog-titlebar-close {display: none }
</style>

<script type="text/javascript">

$(document).ready(function() {
	
	//create tabs
	$("#innercenter").tabs({
		cache: true
	});
	
	$("#libillinglist").show();
	$('#libillpayments').show();
	$("#billingList").click();
	
	$(".ui-layout-center").css("overflow","hidden");
	$(window).resize(); //To refresh/recalculate height and width of all regions
	
});

function loadBilingList() {
	$("#tab_billinglist").show();		
	$("#tab_billpayment").hide();
	
    var ifr;
    ifr = document.getElementById("billingListFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
   	ifr.src="${billingList}?customerId=${customerId}&ts="+new Date().getTime();
    return false;
}

function loadBillPaymentList() {
	$("#tab_billinglist").hide();		
	$("#tab_billpayment").show();

    var ifr;
    ifr = document.getElementById("billPaymentFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src = "${billPaymentList}?customerId=${customerId}&ts="+new Date().getTime();
    return false;
}


</script>

<div id="innercenter" class="ui-layout-center outermostdiv outerContainer" >
	<div>
	<ul>		
        <li id="libillinglist"  style="display:none"><a id="billingList" href="#tab_billinglist" onclick="loadBilingList();"><span>Billing Details</span></a></li>
		<li id="libillpayments" style="display:none"><a id="billPayment" href="#tab_billpayment" onclick="loadBillPaymentList();"><span>Bill Payments</span></a></li>
	</ul>
	</div>
	<div class="ui-layout-content ui-widget-content ui-corner-bottom" style="border-left: 0; border-top: 0; padding: 0px; width: 100%; height: 100%;">
		<div id="tab_billinglist" class="pnl_rht"><iframe frameborder="0" id="billingListFrame" style="width: 100%; height: 100%;"></iframe></div>
		<div id="tab_billpayment" class="pnl_rht"><iframe frameborder="0" id="billPaymentFrame" style="width: 100%; height: 100%;"></iframe></div>
	</div>		
</div>
