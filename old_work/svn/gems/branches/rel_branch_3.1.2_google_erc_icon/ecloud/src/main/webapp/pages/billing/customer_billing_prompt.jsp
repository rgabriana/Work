<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:url value="/bill/generateCustomerBill.ems" var="generateCustomerBillUrl" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<style type="text/css">

#formContainer .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}    
</style>
<script type="text/javascript">

var custId;
var nextBillDate;
var isFormValid = true;
var totalBillCount;
var currentServerDateStr;
var nextBillingDateStr;
$(document).ready(function() {

	nextBillDate = "${nextBillingDate}";
	totalBillCount = "${totalBillCount}";
	currentServerDateStr = "${currentServerDateStr}";
	nextBillingDateStr = "${nextBillingDateStr}";
	$( "#billstarttime" ).datetimepicker({
		timeFormat: 'hh:mm:ss',
		dateFormat: 'mm/dd/yy',
		showTimepicker:false,
		currentText:'Today',
	    onClose: function(dateText, inst) {
	    },
	    onSelect: function (selectedDateTime){
	    	restrictDates("start",selectedDateTime);
	    },
	});
	$('#billstarttime').datepicker( 'option', 'maxDate', currentServerDateStr );
	var newDate = new Date();
	newDate.setTime(nextBillDate);
	//$('#billstarttime').datetimepicker('setDate', newDate);
	$('#billstarttime').val(nextBillingDateStr);
	//console.log("IN DIALOG "+ totalBillCount);
	if(totalBillCount>=1)
	{
		$("#billstarttime").datetimepicker('disable');
	}
	
	$( "#billendtime" ).datetimepicker({
		timeFormat: 'hh:mm:ss',
		dateFormat: 'mm/dd/yy',
		showTimepicker:false,
		currentText:'Today',
	    onClose: function(dateText, inst) {
	    },
	    onSelect: function (selectedDateTime){
	    	restrictDates("end",selectedDateTime);
	    },
	});
	
	$('#billendtime').datepicker( 'option', 'maxDate', currentServerDateStr );
	custId = "${customerId}";
	
	function restrictDates(type, date) {
		isFormValid = true;
		var startDate =	$("#billstarttime").datepicker("getDate") ;
		var endDate = 	$("#billendtime").datepicker("getDate");
		//console.log("End Date dateText " + endDate);
		//console.log("Start Date dateText " + startDate);
		if(endDate < startDate)
		{
			//console.log("NOT VALID");
			isFormValid = false;
			$("#email").addClass("invalidField");
		}
	}

	
});

function generateCustomerBill() {
	$("#domainMsg").text("");
	$("#billendtime").removeClass("invalidField");
	var starttime = $("#billstarttime").val();
	var endtime = $("#billendtime").val();
	$('#gbfCustomerId').val(custId);
	$('#gbfbillstartDateId').val(starttime);
	$('#gbfbillendDateId').val(endtime);
	//console.log("isFormValid " + isFormValid);
	if(isFormValid)
	{
		$('#generateBillForm').submit();
		$("#emInstanceBillDialog").dialog("close");
	}else
	{
		$("#domainMsg").text('End date should not prior to start date');
		$("#billendtime").addClass("invalidField");
		return false;
	}
}

</script>

</head>
<body>

<div class="innerContainer">
	<div class="formContainer">
		<div class="field">
			<span id="domainMsg" class="error"></span>
			<div class="formPrompt"><span>Start Date:</span></div>
			<div class="formValue"><input id="billstarttime" name="billstarttime" type="text" /></div>
		</div>
		<div class="field">
			<div class="formPrompt"><span>End Date:</span></div>
			<div class="formValue"><input id="billendtime" name="billendtime" type="text" readonly="readonly"  /></div>
		</div>
		<div class="field">
			<div class="formPrompt"><span>*Both dates are inclusive.</span></div>
			<div class="formValue">
				<button style="display: inline"  id="generatebillbutton" onclick="generateCustomerBill()">Generate</button>
				<span style="display: inline; font-size: 1.2em;" id="statmsg"></span>
			</div>
		</div>
	</div>
</div>

<form id="generateBillForm" action="${generateCustomerBillUrl}" target="_blank" METHOD="POST">
	<input id="gbfCustomerId" name="gbfCustomerId" type="hidden"/>
	<input id="gbfbillstartDateId" name="gbfbillstartDateId" type="hidden"/>
	<input id="gbfbillendDateId" name="gbfbillendDateId" type="hidden"/>
</form>

</body>
</html>
