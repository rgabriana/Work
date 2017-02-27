<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:url value="/sites/filterallanomalybydate.ems" var="filterAnomalyByDate" />
<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<style type="text/css">

#formContainer .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}    
</style>
<script type="text/javascript">

var custId;
var isFormValid = true;
var currentServerDateStr;
$(document).ready(function() {
	currentServerDateStr = "${currentServerDateStr}";
	$( "#filterAnomalystarttime" ).datetimepicker({
		timeFormat: 'hh:mm:ss',
		dateFormat: 'mm/dd/yy',
		showTimepicker:false,
		currentText:'Today',
	    onClose: function(dateText, inst) {
	    },
	    onSelect: function (selectedDateTime){
	    	restrictDates("start",selectedDateTime);
	    }
	});
	$('#filterAnomalystarttime').datepicker( 'option', 'maxDate', currentServerDateStr );
	//$('#billstarttime').datetimepicker('setDate', newDate);
	//$('#filterAnomalystarttime').val(nextBillingDateStr);
	//console.log("IN DIALOG "+ totalBillCount);
	//$("#filterAnomalystarttime").datetimepicker('disable');
	
	$( "#filterAnomalyendtime" ).datetimepicker({
		timeFormat: 'hh:mm:ss',
		dateFormat: 'mm/dd/yy',
		showTimepicker:false,
		currentText:'Today',
	    onClose: function(dateText, inst) {
	    },
	    onSelect: function (selectedDateTime){
	    	restrictDates("end",selectedDateTime);
	    }
	});
	
	$('#filterAnomalyendtime').datepicker( 'option', 'maxDate', currentServerDateStr );
	custId = "${customerId}";
	
	function restrictDates(type, date) {
		isFormValid = true;
		var startDate =	$("#filterAnomalystarttime").datepicker("getDate") ;
		var endDate = 	$("#filterAnomalyendtime").datepicker("getDate");
		//console.log("End Date dateText " + endDate);
		//console.log("Start Date dateText " + startDate);
		if(endDate < startDate)
		{
			//console.log("NOT VALID");
			isFormValid = false;
			//$("#email").addClass("invalidField");
		}
	}

	
});

function filterAnomalyByDate() {
	$("#domainMsg").text("");
	$("#filterAnomalyendtime").removeClass("invalidField");
	var starttime = $("#filterAnomalystarttime").val();
	var endtime = $("#filterAnomalyendtime").val();
	$('#faCustomerId').val(custId);
	$('#fastartDateId').val(starttime);
	$('#faendDateId').val(endtime);
	if(isFormValid)
	{
		$('#filterAnomalyByDateForm').submit();
		$("#AllsiteAnomaliesFormDialog").dialog("close");
	}else
	{
		$("#domainMsg").text('End date should not prior to start date');
		$("#filterAnomalyendtime").addClass("invalidField");
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
			<div class="formValue"><input id="filterAnomalystarttime" name="filterAnomalystarttime" type="text" /></div>
		</div>
		<div class="field">
			<div class="formPrompt"><span>End Date:</span></div>
			<div class="formValue"><input id="filterAnomalyendtime" name="filterAnomalyendtime" type="text" readonly="readonly"  /></div>
		</div>
		<div class="field">
			<div class="formPrompt"><span>*Both dates are inclusive.</span></div>
			<div class="formValue">
				<button style="display: inline"  id="filterAnomalyByDateButton" onclick="filterAnomalyByDate()">Apply</button>
				<span style="display: inline; font-size: 1.2em;" id="statmsg"></span>
			</div>
		</div>
	</div>
</div>

<form id="filterAnomalyByDateForm" action="${filterAnomalyByDate}" METHOD="POST">
	<input id="faCustomerId" name="faCustomerId" type="hidden"/>
	<input id="fastartDateId" name="fastartDateId" type="hidden"/>
	<input id="faendDateId" name="faendDateId" type="hidden"/>
</form>

</body>
</html>
