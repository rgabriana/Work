<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:url value="gmbCustomerInvoice.ems" var="gmbCustomerInvoiceUrl" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script type="text/javascript">

var custId ;

$(document).ready(function() {
	$( "#starttime" ).datetimepicker({
		timeFormat: 'hh:mm:ss',
		dateFormat: 'mm/dd/yy',
		showTimepicker:false,
		currentText:'Today',
	    onClose: function(dateText, inst) {
	    },
	    onSelect: function (selectedDateTime){
	    },
	});
	
	$( "#endtime" ).datetimepicker({
		timeFormat: 'hh:mm:ss',
		dateFormat: 'mm/dd/yy',
		showTimepicker:false,
		currentText:'Today',
	    onClose: function(dateText, inst) {
	    },
	    onSelect: function (selectedDateTime){
	    },
	});
	
	custId = "${custId}";
	
});

function showReport() {
	var starttime = $("#starttime").val();
	var endtime = $("#endtime").val();
	
	$('#gmbCustomerId').val(custId);
	$('#startDateId').val(starttime);
	$('#endDateId').val(endtime);
	$('#gmbInvoiceForm').submit();
	$("#invoicePromptDialog").dialog("close");
}

</script>

</head>
<body>

<div class="innerContainer">
	<div class="formContainer">
		<div class="field">
			<div class="formPrompt"><span>Start Date:</span></div>
			<div class="formValue"><input id="starttime" name="starttime" type="text" /></div>
		</div>
		<div class="field">
			<div class="formPrompt"><span>End Date:</span></div>
			<div class="formValue"><input id="endtime" name="endtime" type="text" /></div>
		</div>
		<div class="field">
			<div class="formPrompt"><span>*Both dates are inclusive.</span></div>
			<div class="formValue">
				<button style="display: inline"  id="submitReportTime" onclick="showReport()">Submit</button>
				<span style="display: inline; font-size: 1.2em;" id="statmsg"></span>
			</div>
		</div>
		
	</div>
</div>

<form id="gmbInvoiceForm" action="${gmbCustomerInvoiceUrl}" target="_blank" METHOD="POST">
	<input id="gmbCustomerId" name="customerId" type="hidden"/>
	<input id="startDateId" name="startDate" type="hidden"/>
	<input id="endDateId" name="endDate" type="hidden"/>
</form>

</body>
</html>
