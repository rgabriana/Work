<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<spring:url value="/scripts/jquery/jquery.ptTimeSelect.js" var="ptTimeSelect"></spring:url>
<script type="text/javascript" src="${ptTimeSelect}"></script>
<spring:url value="/themes/standard/css/jquery/jquery.ptTimeSelect.css" var="ptTimeSelectCss"></spring:url>
<link rel="stylesheet" type="text/css" href="${ptTimeSelectCss}" />
<spring:url value="/scripts/jquery/jquery.dualListBox-1.3.min.js" var="jqueryduallistbox"></spring:url>
<script type="text/javascript" src="${jqueryduallistbox}"></script>
<spring:url value="/themes/default/images/time_picker.jpeg" var="timePicker" scope="request"/>

<spring:url value="/services/org/emailnotification/saveEmailNotificationScheduler" var="saveEmailNotificationSchedulerUrl" scope="request" />

<style type="text/css">

html {height:100% !important;}

fieldset {
    border:1px solid #999;
    border-radius:8px;
	padding: 5px 5px 5px 5px;
}

.rectbltdlbl{color:black !important;}

</style>

<script type="text/javascript">

	var COLOR_SUCCESS = "green";
	var COLOR_DEFAULT = "black";
	var COLOR_ERROR = "red";
			
	$(document).ready(function() {
		
		clearEmailNotificationConfigMessage();
		
		if("${emailNotificationSchedulerEnable}" == "true"){
			$('#emailId').prop("disabled", false);
			$('#timeId').prop("disabled", false);
			$('#recurrenceScheduler1').prop("disabled", false);
			$('#recurrenceScheduler2').prop("disabled", false);
			$('#recurrenceScheduler3').prop("disabled", false);
			$('#recurrenceScheduler4').prop("disabled", false);
			$('#recurrenceScheduler5').prop("disabled", false);
			$('#recurrenceScheduler6').prop("disabled", false);
			$('#recurrenceScheduler7').prop("disabled", false);
			
			$("#enableOneHourNotificationId").prop("disabled", false);
			
			$('#Critical').prop('disabled', false);
			$('#Major').prop('disabled', false);
			$('#Minor').prop('disabled', false);
			$('#Warning').prop('disabled', false);
			$('#Info').prop('disabled', false);
			
			$("#box1View").prop('disabled', false);
			$("#box2View").prop('disabled', false);
			
			$("#to2").prop('disabled', false);
			$("#allTo2").prop('disabled', false);
			$("#allTo1").prop('disabled', false);
			$("#to1").prop('disabled', false);
			
			$('#enableSchedulerId').prop('checked', true);
			$('#emailId').val("${emailNotificationSchedulerEmail}");
			$('#timeId').val("${emailNotificationReportTime}");
			
			if("${enableOneHourNotification}" == "true"){
				$('#enableOneHourNotificationId').prop('checked', true);
			}else{
				$('#enableOneHourNotificationId').prop('checked', false);
			}
			
			if ("${emailNotificationRecurrence}".indexOf("MON") >= 0){
				$('#recurrenceScheduler1').prop('checked', true);
			}else{
				$('#recurrenceScheduler1').prop('checked', false);
			}
			
			if ("${emailNotificationRecurrence}".indexOf("TUE") >= 0){
				$('#recurrenceScheduler2').prop('checked', true);
			}else{
				$('#recurrenceScheduler2').prop('checked', false);
			}
			
			if ("${emailNotificationRecurrence}".indexOf("WED") >= 0){
				$('#recurrenceScheduler3').prop('checked', true);
			}else{
				$('#recurrenceScheduler3').prop('checked', false);
			}
			
			if ("${emailNotificationRecurrence}".indexOf("THU") >= 0){
				$('#recurrenceScheduler4').prop('checked', true);
			}else{
				$('#recurrenceScheduler4').prop('checked', false);
			}
			
			if ("${emailNotificationRecurrence}".indexOf("FRI") >= 0){
				$('#recurrenceScheduler5').prop('checked', true);
			}else{
				$('#recurrenceScheduler5').prop('checked', false);
			}
			
			if ("${emailNotificationRecurrence}".indexOf("SAT") >= 0){
				$('#recurrenceScheduler6').prop('checked', true);
			}else{
				$('#recurrenceScheduler6').prop('checked', false);
			}
			
			if ("${emailNotificationRecurrence}".indexOf("SUN") >= 0){
				$('#recurrenceScheduler7').prop('checked', true);
			}else{
				$('#recurrenceScheduler7').prop('checked', false);
			}
			
			if ("${emailNotificationSeverityList}".indexOf("Critical") >= 0){
				$('#Critical').prop('checked', true);
			}else{
				$('#Critical').prop('checked', false);
			}
			
			if ("${emailNotificationSeverityList}".indexOf("Major") >= 0){
				$('#Major').prop('checked', true);
			}else{
				$('#Major').prop('checked', false);
			}
			
			if ("${emailNotificationSeverityList}".indexOf("Minor") >= 0){
				$('#Minor').prop('checked', true);
			}else{
				$('#Minor').prop('checked', false);
			}
			
			if ("${emailNotificationSeverityList}".indexOf("Warning") >= 0){
				$('#Warning').prop('checked', true);
			}else{
				$('#Warning').prop('checked', false);
			}
			
			if ("${emailNotificationSeverityList}".indexOf("Info") >= 0){
				$('#Info').prop('checked', true);
			}else{
				$('#Info').prop('checked', false);
			}
			
			if("${emailNotificationEventTypeList}" != ""){
				var emailNotificationEventTypeList = "${emailNotificationEventTypeList}".split(','); 
				
				$.each(emailNotificationEventTypeList , function(i, val) { 
					$("#box1View option[value='"+emailNotificationEventTypeList[i]+"']").remove();
					$("#box2View").append("<option value='"+emailNotificationEventTypeList[i]+"'>"+emailNotificationEventTypeList[i]+"</option>");
				});
			}
			
			
			
		}else{
			$("#emailId").prop("disabled", true);
			$("#timeId").prop("disabled", true);
			$("#recurrenceScheduler1").prop("disabled", true);
			$("#recurrenceScheduler2").prop("disabled", true);
			$("#recurrenceScheduler3").prop("disabled", true);
			$("#recurrenceScheduler4").prop("disabled", true);
			$("#recurrenceScheduler5").prop("disabled", true);
			$("#recurrenceScheduler6").prop("disabled", true);
			$("#recurrenceScheduler7").prop("disabled", true);
			
			$("#enableOneHourNotificationId").prop("disabled", true);
			
			$('#Critical').prop('disabled', true);
			$('#Major').prop('disabled', true);
			$('#Minor').prop('disabled', true);
			$('#Warning').prop('disabled', true);
			$('#Info').prop('disabled', true);
			
			$("#box1View").prop('disabled', true);
			$("#box2View").prop('disabled', true);
			
			$("#to2").prop('disabled', true);
			$("#allTo2").prop('disabled', true);
			$("#allTo1").prop('disabled', true);
			$("#to1").prop('disabled', true);
			
			$('#enableSchedulerId').prop('checked', false);
			$('#emailId').val("");
			$('#timeId').val("");
			$('#recurrenceScheduler1').prop('checked', false);
			$('#recurrenceScheduler2').prop('checked', false);
			$('#recurrenceScheduler3').prop('checked', false);
			$('#recurrenceScheduler4').prop('checked', false);
			$('#recurrenceScheduler5').prop('checked', false);
			$('#recurrenceScheduler6').prop('checked', false);
			$('#recurrenceScheduler7').prop('checked', false);
			
			$('#enableOneHourNotificationId').prop('checked', false);
		}
		
		$('#enableSchedulerId').change(function () {
			if ($(this).attr("checked")) {
				$('#emailId').prop("disabled", false);
				$('#timeId').prop("disabled", false);
				$('#recurrenceScheduler1').prop("disabled", false);
				$('#recurrenceScheduler2').prop("disabled", false);
				$('#recurrenceScheduler3').prop("disabled", false);
				$('#recurrenceScheduler4').prop("disabled", false);
				$('#recurrenceScheduler5').prop("disabled", false);
				$('#recurrenceScheduler6').prop("disabled", false);
				$('#recurrenceScheduler7').prop("disabled", false);
				
				$("#enableOneHourNotificationId").prop("disabled", false);
				
				$('#Critical').prop('disabled', false);
				$('#Major').prop('disabled', false);
				$('#Minor').prop('disabled', false);
				$('#Warning').prop('disabled', false);
				$('#Info').prop('disabled', false);
				
				$("#box1View").prop('disabled', false);
				$("#box2View").prop('disabled', false);
				
				$("#to2").prop('disabled', false);
				$("#allTo2").prop('disabled', false);
				$("#allTo1").prop('disabled', false);
				$("#to1").prop('disabled', false);
				
		    }else{
		    	$("#emailId").prop("disabled", true);
				$("#timeId").prop("disabled", true);
				$("#recurrenceScheduler1").prop("disabled", true);
				$("#recurrenceScheduler2").prop("disabled", true);
				$("#recurrenceScheduler3").prop("disabled", true);
				$("#recurrenceScheduler4").prop("disabled", true);
				$("#recurrenceScheduler5").prop("disabled", true);
				$("#recurrenceScheduler6").prop("disabled", true);
				$("#recurrenceScheduler7").prop("disabled", true);
				
				$("#enableOneHourNotificationId").prop("disabled", true);
				
				$('#Critical').prop('disabled', true);
				$('#Major').prop('disabled', true);
				$('#Minor').prop('disabled', true);
				$('#Warning').prop('disabled', true);
				$('#Info').prop('disabled', true);
				
				$("#box1View").prop('disabled', true);
				$("#box2View").prop('disabled', true);
				
				$("#to2").prop('disabled', true);
				$("#allTo2").prop('disabled', true);
				$("#allTo1").prop('disabled', true);
				$("#to1").prop('disabled', true);
				
				$('#emailId').val("");
				$('#timeId').val("");
				$('#recurrenceScheduler1').prop('checked', false);
				$('#recurrenceScheduler2').prop('checked', false);
				$('#recurrenceScheduler3').prop('checked', false);
				$('#recurrenceScheduler4').prop('checked', false);
				$('#recurrenceScheduler5').prop('checked', false);
				$('#recurrenceScheduler6').prop('checked', false);
				$('#recurrenceScheduler7').prop('checked', false);
				
				$('#enableOneHourNotificationId').prop('checked', false);
				
				$('#Critical').prop('checked', false);
				$('#Major').prop('checked', false);
				$('#Minor').prop('checked', false);
				$('#Warning').prop('checked', false);
				$('#Info').prop('checked', false);
				
				$('#box2View').empty();
				$('#box1View').empty();
				
				<c:forEach var="event" items="${events}">
					$("#box1View").append("<option value='${event.type}'>${event.type}</option>");
				</c:forEach>
		    }
		});
		
		$("#timeId").ptTimeSelect({zIndex: 10000,onFocusDisplay: false, popupImage: '<img src="${timePicker}" class="timePickerImageStyle" />' });
		
		jQuery.configureBoxes({useFilters: false, selectOnSubmit: false});
		
	});
	
	function IsEmail(email) {
		  
		var emails = email.split(/[,]+/);
		var regex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
        valid = true;
        for (var i in emails) {
            value = $.trim(emails[i]);
            valid = valid && regex.test(value);
        }
        return valid;
	}
	
	function validateTime(inputField) { 
		//var isValid = /^([0-1]?[0-9]|2[0-4]):([0-5][0-9])(:[0-5][0-9])?$/.test(inputField); 
		var isValid = /^([0-1]?\d):([0-5]\d)\s(?:AM|PM)$/.test(inputField);
		//var isValid = /^([0-9]|1[0-2]):([0-5]\d)\s?(AM|PM)?$/.test(inputField);
		return isValid;
	}
	
	function saveEmailNotificationScheduler(){
		
		clearEmailNotificationConfigMessage();
		
		var enableSchedulerString = "false";
		
		var emailString = "";
		
		var timeString = "";
		
		var severityList = "";
		
		var eventTypeList = "";
		
		var recurrenceSchedulerString = "";
		
		var enableOneHourNotificationString = "false";
		
		if($('#enableSchedulerId').is(":checked")){
			
			enableSchedulerString = "true";
			
			if($('#emailId').val() == ""){
				//alert("Email field cannot be empty");
				dispalyEmailNotificationConfigMessage('Email field cannot be empty.',COLOR_ERROR);
				return false;
			}
			
			if(!IsEmail($('#emailId').val())){
				//alert("Please enter a valid email address");
				dispalyEmailNotificationConfigMessage('Please enter valid comma seperated email addresses.',COLOR_ERROR);
				return false;
			}
			
			$("#box2View").children('option').attr('selected', 'selected');
			
			var first = true;
			
			<c:forEach var="severity" items="${severities}">
				if($("#"+ "${severity}").attr("checked") == "checked") {
					if(!first) {
						severityList = severityList + ",";
					}
					else {
						first = false;
					}
					severityList = severityList + "${severity}";
				}
			</c:forEach>
			
			eventTypeList = ($('#box2View').val() == null  ? "" : $('#box2View').val());
			
			if($('#timeId').val() == ""){
				if($('#enableOneHourNotificationId').is(":checked")){
					if(($('#recurrenceScheduler1').is(":checked") ||  $('#recurrenceScheduler2').is(":checked") || $('#recurrenceScheduler3').is(":checked") || 
							$('#recurrenceScheduler4').is(":checked") || $('#recurrenceScheduler5').is(":checked") || $('#recurrenceScheduler6').is(":checked") || 
							$('#recurrenceScheduler7').is(":checked") || eventTypeList != "" || severityList != "")){
						dispalyEmailNotificationConfigMessage('Time field cannot be empty.',COLOR_ERROR);
						return false;
					}
				}else{
					dispalyEmailNotificationConfigMessage('Time field cannot be empty.',COLOR_ERROR);
					return false;
				}
			}
			
			if($('#timeId').val() != ""){
				if(!validateTime($('#timeId').val())){
					dispalyEmailNotificationConfigMessage('Please enter the time in HH:MM AM/PM format.',COLOR_ERROR);
					return false;
				}
			}
			
			if(!($('#recurrenceScheduler1').is(":checked") ||  $('#recurrenceScheduler2').is(":checked") || $('#recurrenceScheduler3').is(":checked") || 
					$('#recurrenceScheduler4').is(":checked") || $('#recurrenceScheduler5').is(":checked") || $('#recurrenceScheduler6').is(":checked") || $('#recurrenceScheduler7').is(":checked"))){
				if($('#enableOneHourNotificationId').is(":checked")){
					if($('#timeId').val() != "" || eventTypeList != "" || severityList != ""){
						dispalyEmailNotificationConfigMessage('Please select atleast one recurrence checkbox.',COLOR_ERROR);
						return false;
					}
				}else{
					dispalyEmailNotificationConfigMessage('Please select atleast one recurrence checkbox.',COLOR_ERROR);
					return false;
				}
			}
			
			if($('#timeId').val() != ""){
				if(!validateTime($('#timeId').val())){
					dispalyEmailNotificationConfigMessage('Please enter the time in HH:MM AM/PM format.',COLOR_ERROR);
					return false;
				}
			}
			
			
			if($('#recurrenceScheduler1').is(":checked")){
				recurrenceSchedulerString = "MON";
			}
			
			if($('#recurrenceScheduler2').is(":checked")){
				if(recurrenceSchedulerString == ""){
					recurrenceSchedulerString = "TUE"
				}else{
					recurrenceSchedulerString = recurrenceSchedulerString + ",TUE"
				}
			}
			
			if($('#recurrenceScheduler3').is(":checked")){
				if(recurrenceSchedulerString == ""){
					recurrenceSchedulerString = "WED"
				}else{
					recurrenceSchedulerString = recurrenceSchedulerString + ",WED"
				}
			}
			
			if($('#recurrenceScheduler4').is(":checked")){
				if(recurrenceSchedulerString == ""){
					recurrenceSchedulerString = "THU"
				}else{
					recurrenceSchedulerString = recurrenceSchedulerString + ",THU"
				}
			}
			
			if($('#recurrenceScheduler5').is(":checked")){
				if(recurrenceSchedulerString == ""){
					recurrenceSchedulerString = "FRI"
				}else{
					recurrenceSchedulerString = recurrenceSchedulerString + ",FRI"
				}
			}
			
			if($('#recurrenceScheduler6').is(":checked")){
				if(recurrenceSchedulerString == ""){
					recurrenceSchedulerString = "SAT"
				}else{
					recurrenceSchedulerString = recurrenceSchedulerString + ",SAT"
				}
			}
			
			if($('#recurrenceScheduler7').is(":checked")){
				if(recurrenceSchedulerString == ""){
					recurrenceSchedulerString = "SUN"
				}else{
					recurrenceSchedulerString = recurrenceSchedulerString + ",SUN"
				}
			}
			
			if($('#enableOneHourNotificationId').is(":checked")){
				enableOneHourNotificationString = "true";
			}else{
				enableOneHourNotificationString = "false";
			}
			
			emailString = $('#emailId').val();
			
			timeString = $('#timeId').val();
			
			
		}
		
		$.ajax({
	 		type: 'POST',
	 		url: "${saveEmailNotificationSchedulerUrl}?ts="+new Date().getTime(),
	 		contentType: "application/json",
	 		data: '{"enabled":"' + enableSchedulerString + '","eventTypeList":"' + eventTypeList + '","severityList":"' + severityList + '","emailList":"' + emailString  +'","time":"' + timeString + '","weeklyRecurrence":"' + recurrenceSchedulerString +'","enableOneHourNotification":"'+enableOneHourNotificationString+'"}', 
	 		dataType: "json",
	 		success: function(data){
				//alert("Email Notification Scheduler Options Successfully saved");
				dispalyEmailNotificationConfigMessage('Email Notification Scheduler Options successfully saved.',COLOR_SUCCESS);
			},
			error: function(){
				//alert("Error");
				dispalyEmailNotificationConfigMessage('Error.',COLOR_ERROR);
			}
	 	});
	}
	
	function dispalyEmailNotificationConfigMessage(Message, Color) {
		$("#emailnotificationconfigerror").html(Message);
		$("#emailnotificationconfigerror").css("color", Color);
	}

	function clearEmailNotificationConfigMessage() {
		dispalyEmailNotificationConfigMessage("", COLOR_DEFAULT);
	}
	
</script>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="outerDiv" style="padding: 5px 5px 5px 5px;">
		<div><label style="padding: 5px 5px 5px 5px;font-weight:bold">Email Notifications</label></div>
		<div style="height:5px"></div>
		<fieldset>
		<legend><span>Scheduler</span></legend>
		<div>
			<table style="width: 100%;">
				<tr>
					<td align="left" style="padding: 5px 5px 5px 5px;font-weight:bold"><input type="checkbox" name="enableScheduler" id="enableSchedulerId">  Enable</td>
				</tr>
				<tr>
				<table style="padding:15px 0px 15px 0px">
						<tr>
							<td width="200" colspan="1" style="align: center;padding:0px 5px 0px 5px">
								<label style="color: black;font-weight:bold"><spring:message code='eventsAndFault.selectEventType'/> <br /></label>
								<select id="box1View" multiple="multiple" style="height: 200px; width: 200px;">
								<c:forEach var="event" items="${events}">
									<option value="${event.type}" >${event.type}</option>
								</c:forEach>
								</select>
							</td>
							<td width="175">
								<button id="to2" type="button">&nbsp;>&nbsp;</button>
			                    <button id="allTo2" type="button">&nbsp;>>&nbsp;</button>
			                    <button id="allTo1" type="button">&nbsp;<<&nbsp;</button>
			                    <button id="to1" type="button">&nbsp;<&nbsp;</button>
							</td>
							<td width="200" colspan="1">
								<label style="color: black;font-weight:bold"><spring:message code='eventsAndFault.selectedEventTypes'/> <br /></label>
								<select id="box2View" name="box2View" multiple="multiple" style="height: 200px; width: 200px;">
								</select>
							</td>
						</tr>
				</table>
				</tr>
				<tr>
				<td align="left" style="padding: 15px 5px 5px 5px;">
				<span style="font-weight:bold;padding:0px 15px 0px 5px"><spring:message code='eventsAndFault.importance'/></span>
				
					<c:forEach var="severity" items="${severities}">
						<input id="${severity}" value="${severity}" type=checkbox ><label style="color: black">${severity} &nbsp;&nbsp;&nbsp;</label></input>
					</c:forEach>
				</td>
				</tr>
				<tr>
					<table style="padding:15px 0px 15px 0px">
					<tr>
					<td align="left" style="padding: 5px 0px 5px 0px;font-weight:bold">Email: <input type="text" name="email" id="emailId" size="50">
									   Time: <input type="text" name="time" id="timeId" size="8">
					</td>
					</tr>
					<tr>
					<td align="left">
					(one or more email addresses, separated by commas.)
					</td>
					</tr>
					</table>
				 </tr>
				 <tr>
					 <table border="0" cellpadding="0" cellspacing="0" style="padding-top:5px;padding-bottom:10px">
						<tr>
							<td style="padding:0px 15px 0px 5px;font-weight:bold">Recurrence:</td>
							<td class="rectbltdlbl">Monday</td>
							<td class="proftbltd"><input type="checkbox" id="recurrenceScheduler1"></td>
							<td class="rectbltdlbl">Tuesday</td>
							<td class="proftbltd"><input type="checkbox" id="recurrenceScheduler2"></td>
							<td class="rectbltdlbl">Wednesday</td>
							<td class="proftbltd"><input type="checkbox" id="recurrenceScheduler3"></td>
							<td class="rectbltdlbl">Thursday</td>
							<td class="proftbltd"><input type="checkbox" id="recurrenceScheduler4"></td>
							<td class="rectbltdlbl">Friday</td>
							<td class="proftbltd"><input type="checkbox" id="recurrenceScheduler5"></td>
							<td class="rectbltdlbl">Saturday</td>
							<td class="proftbltd"><input type="checkbox" id="recurrenceScheduler6"></td>
							<td class="rectbltdlbl">Sunday</td>
							<td class="proftbltd"><input type="checkbox" id="recurrenceScheduler7"></td>
						</tr>
					 </table>
				 </tr>
				 <tr>
				 	<table border="0" cellpadding="0" cellspacing="0" style="padding-bottom:5px">
				 	<tr>
				 		<td align="left" style="padding: 5px 5px 5px 5px;font-weight:bold"><input type="checkbox" name="enableOneHourNotification" id="enableOneHourNotificationId">  Enable 1 hour notification for all critical and major alerts</td>
			 		</tr>
				 	</table>
				 </tr>
				 <tr>
				 	<table border="0" cellpadding="0" cellspacing="0" style="padding-bottom:5px">
				 	<tr>
					 	<td align="left" style="padding: 5px 5px 5px 5px;"><button id="emailNotificationSchedulerBtn" onclick="saveEmailNotificationScheduler();">Save</button></td>
						<td>
							<span id="emailnotificationconfigerror" class="error" style="font-weight: bold;padding-left:20px"></span>
						</td>
					</tr>
				 	</table>
				 </tr>
			</table>
		</div>
		</fieldset>
		<div style="height:5px"></div>
	</div>
	
 </div>