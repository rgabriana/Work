<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:url value="/reports/generate.ems" var="customerInvoiceReport" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script type="text/javascript">

var custId ;

$(document).ready(function() {
	$( "#start_time" ).datetimepicker({
		timeFormat: 'hh:mm:ss',
		dateFormat: 'mm/dd/yy',
		showTimepicker:false,
		currentText:'Today',
	    onClose: function(dateText, inst) {
	    },
	    onSelect: function (selectedDateTime){
	    },
	});
	
	$( "#end_time" ).datetimepicker({
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
	var start_time = $("#start_time").val().substring(0,10).replace(/[/]/g,"-");
	var end_time = $("#end_time").val().substring(0,10).replace(/[/]/g,"-");

	$('#customerId').val(custId);
	$('#startDate').val(start_time);
	$('#endDate').val(end_time);
	$('#birtInvoiceForm').submit();
	$("#invoiceBirtPromptDialog").dialog("close");
}

</script>

</head>
<body>

<div class="innerContainer">
	<div class="formContainer">
		<div class="field">
			<div class="formPrompt"><span>Start Date:</span></div>
			<div class="formValue"><input id="start_time" name="start_time" type="text" /></div>
		</div>
		<div class="field">
			<div class="formPrompt"><span>End Date:</span></div>
			<div class="formValue"><input id="end_time" name="end_time" type="text" /></div>
		</div>
		<div class="field">
			<div class="formPrompt"><span>*Both dates are inclusive.</span></div>
			<div class="formValue">
				<button style="display: inline"  id="submitReportTime" onclick="showReport()">Show Report</button>
				<span style="display: inline; font-size: 1.2em;" id="statmsg"></span>
			</div>
		</div>
		
	</div>
</div>

<form id="birtInvoiceForm" action="${customerInvoiceReport}" target="_blank" METHOD="POST">
	<input id="customerId" name="customerId" type="hidden"/>
	<input id="startDate" name="startDate" type="hidden"/>
	<input id="endDate" name="endDate" type="hidden"/>
</form>

</body>
</html>
