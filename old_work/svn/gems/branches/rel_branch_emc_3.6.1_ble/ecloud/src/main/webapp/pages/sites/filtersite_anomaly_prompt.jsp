<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:url value="/sites/filtersiteanomalybydate.ems" var="filterSiteAnomalyByDate" />
<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<style type="text/css">

#formContainer .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}    
</style>
<script type="text/javascript">

var id;
var modeType="";
var isFormValid = true;
var currentServerDateStr;
$(document).ready(function() {
	currentServerDateStr = "${currentServerDateStr}";
	$( "#filtersiteAnomalystarttime" ).datetimepicker({
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
	$('#filtersiteAnomalystarttime').datepicker( 'option', 'maxDate', currentServerDateStr );
	
	$( "#filtersiteAnomalyendtime" ).datetimepicker({
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
	
	$('#filtersiteAnomalyendtime').datepicker( 'option', 'maxDate', currentServerDateStr );
	
	modeType = "${modeType}"
	if(modeType=="customer")
	{
		id = "${customerId}";
	}else if(modeType=="site")
	{
		id = "${siteId}";
	}else if(modeType=="analysis")
	{
		id = "${siteId}";
	}
	else if(modeType=="bill")
	{
		id = "${customerId}";
	}
	
	function restrictDates(type, date) {
		isFormValid = true;
		var startDate =	$("#filterAnomalystarttime").datepicker("getDate") ;
		var endDate = 	$("#filtersiteAnomalyendtime").datepicker("getDate");
		if(endDate < startDate)
		{
			isFormValid = false;
		}
	}
});

function validateSiteAnomalyByDateForm()
{
	var startDate =	$("#filterAnomalystarttime").datepicker("getDate") ;
	var endDate = 	$("#filtersiteAnomalyendtime").datepicker("getDate");
	if(endDate == null || startDate == null ){
		isFormValid = false;
	}
	if(isFormValid==true)
	{
		filterSiteAnomalyByDate();
	}else
	{
		$("#domainMsg").text('Please select the date');
		$("#filtersiteAnomalyendtime").addClass("invalidField");
		return false;
	}
}
function filterSiteAnomalyByDate() {
	
	$("#domainMsg").text("");
	$("#filtersiteAnomalyendtime").removeClass("invalidField");
	var starttime = $("#filtersiteAnomalystarttime").val();
	var endtime = $("#filtersiteAnomalyendtime").val();
	$('#fsaId').val(id);
	$('#fsastartDateId').val(starttime);
	$('#fsaendDateId').val(endtime);
	$('#fsamode').val(modeType);
	
	if(isFormValid)
	{
		$('#filterSiteAnomalyByDateForm').submit();
		$("#siteAnomaliesFormDialog").dialog("close");
	}else
	{
		$("#domainMsg").text('End date should not prior to start date');
		$("#filtersiteAnomalyendtime").addClass("invalidField");
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
			<div class="formValue"><input id="filtersiteAnomalystarttime" name="filtersiteAnomalystarttime" type="text" /></div>
		</div>
		<div class="field">
			<div class="formPrompt"><span>End Date:</span></div>
			<div class="formValue"><input id="filtersiteAnomalyendtime" name="filtersiteAnomalyendtime" type="text" readonly="readonly"  /></div>
		</div>
		<div class="field">
			<div class="formPrompt"><span>*Both dates are inclusive.</span></div>
			<div class="formValue">
				<button style="display: inline"  id="filterSiteAnomalyByDateButton" onclick="validateSiteAnomalyByDateForm()">Apply</button>
				<span style="display: inline; font-size: 1.2em;" id="statmsg"></span>
			</div>
		</div>
	</div>
</div>

<form id="filterSiteAnomalyByDateForm" action="${filterSiteAnomalyByDate}" METHOD="POST">
	<input id="fsaId" name="fsaId" type="hidden"/>
	<input id="fsastartDateId" name="fsastartDateId" type="hidden"/>
	<input id="fsaendDateId" name="fsaendDateId" type="hidden"/>
	<input id="fsamode" name="fsamode" type="hidden"/>
</form>

</body>
</html>
