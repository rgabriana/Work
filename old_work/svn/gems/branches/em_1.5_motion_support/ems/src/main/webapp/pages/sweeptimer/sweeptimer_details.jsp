<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/scripts/jquery/jquery.validationEngine.js"
	var="jquery_validationEngine"></spring:url>
<script type="text/javascript" src="${jquery_validationEngine}"></script>
<spring:url value="/scripts/jquery/jquery.validationEngine-en.js"
	var="jquery_validationEngine_en"></spring:url>
<script type="text/javascript" src="${jquery_validationEngine_en}"></script>

<style>
	#sweeptimer_details .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}    
</style>

<script type="text/javascript">
	var oldSweepTimerName='';
	var COLOR_SUCCESS = "green";
	var COLOR_DEFAULT = "black";
	var COLOR_FAULT = "red";

	$(document).ready(function(){
			oldSweepTimerName = $("#name").val();		
	});
	
	function getSweepTimerName(){
		var chkSweepTimerName = $("#name").val();
		var returnresult = false;
		$.ajax({
			type: "GET",
			cache: false,
			url: '<spring:url value="/services/org/sweeptimer/list/"/>'+ chkSweepTimerName,
			dataType: "text",
			async: false,
			success: function(msg) {
				var count = (msg).indexOf(chkSweepTimerName);
				if(oldSweepTimerName != chkSweepTimerName && count > 0) {
					returnresult = false;
				}
				else {
					returnresult = true;
				}
			},
			error: function (jqXHR, textStatus, errorThrown){
				returnresult = false;
			}
		});
		return returnresult;
	}
	
	var isTimingValid = true;
	function validateTime(inputField) { 
	
		if(inputField.value!="")
		{
		  var isValid = /^([0-1]?[0-9]|2[0-4]):([0-5][0-9])(:[0-5][0-9])?$/.test(inputField.value); 
		  
	        if (isValid) { 
	            inputField.style.borderColor = '#757575'; 
	            $("#domainMsg").text('');
	        } else { 
	        	$("#domainMsg").text('Please enter the time in HH:mm format');
	        	//inputField.value="";
	            inputField.style.borderColor = '#ff0000';
	        } 
	        isTimingValid = isValid;
	        return isValid; 
		
		}else
		{
			$("#domainMsg").text("");
			inputField.style.borderColor = '#757575'; 
		}
		return true;
    } 
	
	var isOverrideTimeValid = true;
	
	function validateOverideTime(inputField)
	{
		// Overide Timer Validation
		var overrideTimer = inputField;
		var overrideTimerValue = overrideTimer.value;
		var pattern ="^(0|[1-9][0-9]*)$";
		var regObj=new RegExp(pattern);
		var numberNotValid = false;
		
		if(isNaN(overrideTimerValue))
		{
			numberNotValid = true;
		}else if(!regObj.test(overrideTimerValue))
		{
			numberNotValid = true;
		}else
		{
			// If number is Valid, then check for min : 0 , Max : 120
			if((overrideTimerValue<0) || (overrideTimerValue > 120))
			{
				numberNotValid = true;
			}
		}
		if(numberNotValid)
		{
			$("#domainMsg").text('Please enter a whole number between 0 and 120');
			overrideTimer.style.borderColor = '#ff0000'; 
			overrideTimer.focus();
		}
		else
		{
			overrideTimer.style.borderColor = '#757575'; 
            $("#domainMsg").text('');
		}
		isOverrideTimeValid = !numberNotValid;
		return isOverrideTimeValid;
	}
	
	function validateAndSaveSweepTimer(){
		if(isTimingValid)
		{
			var sweepTimerName = $("#name").val();
			$("#domainMsg").text("");
			if(sweepTimerName == ""){
				$("#name").addClass("invalidField");
				$("#domainMsg").text('Please fill in this field');
				return false;
			}
			
			var result = getSweepTimerName();
			if(!result){
				$("#domainMsg").text('<spring:message code="error.duplicate.sweeptimername"/>');
				$("#name").addClass("invalidField");
				return false;
			}
			else {	
				$("#domainMsg").text("");
				$("#name").removeClass("invalidField");
			}
			/*
			if(!isTimingValid)
			{
				$("#domainMsg").text('Please enter the time in HH:mm format');
				return false;
			}
			*/
			
			if(!validateTimerIntervals())
			{
				return false;
			}
			
			if(!isOverrideTimeValid)
			{
				$("#domainMsg").text('Please enter a whole number between 0 and 120');
				return false;
			}
			return true;
		}
		
		return false;
	}
	
	function resetError(inputField)
	{
		$("#domainMsg").text("");
		$("#name").removeClass("invalidField");
	}
	function validateTimerIntervals()
	{
		var isValidTime = true;
		for (var count = 0; count < 7; count++) {
			var startTime1 = document.getElementById(count+"_startTime1");
			var endTime1 = document.getElementById(count+"_endTime1");
			var startTime2 = document.getElementById(count+"_startTime2");
			var endTime2 = document.getElementById(count+"_endTime2");
			var startTime3 = document.getElementById(count+"_startTime3");
			var endTime3 = document.getElementById(count+"_endTime3");
			
			var startTime1Val =startTime1.value;
			var endTime1Val = endTime1.value;
			var startTime2Val = startTime2.value;
			var endTime2Val = endTime2.value;
			var startTime3Val = startTime3.value;
			var endTime3Val = endTime3.value;
			
			// 4. both field should be present or empty for each day - VALIDATION CASE 4
			if(startTime1Val==="" && endTime1Val.length>0){
				isValidTime = false;
				$("#domainMsg").text('Start time 1 field is empty');
				startTime1.style.borderColor = '#ff0000'; 
				startTime1.focus();
				break;
			}
			if(endTime1Val==="" && startTime1Val.length>0){
				isValidTime = false;
				$("#domainMsg").text('End time 1 field is empty');
				endTime1.style.borderColor = '#ff0000';
				endTime1.focus();
				break;
			}
			if(startTime2Val==="" && endTime2Val.length>0){
				isValidTime = false;
				$("#domainMsg").text('Start time 2 field is empty');
				startTime2.style.borderColor = '#ff0000';
				startTime2.focus();
				break;
			}
			if(endTime2Val==="" && startTime2Val.length>0){
				isValidTime = false;
				$("#domainMsg").text('End time 2 field is empty');
				endTime2.style.borderColor = '#ff0000'; 
				endTime2.focus();
				break;
			}
			
			if(startTime3Val==="" && endTime3Val.length>0){
				isValidTime = false;
				$("#domainMsg").text('Start time 3 field is empty');
				startTime3.style.borderColor = '#ff0000';
				startTime3.focus();
				break;
			}
			if(endTime3Val==="" && startTime3Val.length>0){
				isValidTime = false;
				$("#domainMsg").text('End time 3 field is empty');
				endTime3.style.borderColor = '#ff0000'; 
				endTime3.focus();
				break;
			}
			
			// 1. if endtime 1 < start time 1 -> error - VALIDATION CASE 1
			
			//  1 if endTime greater than startTime,
			// -1 if endTime less than startTime
			//  0 if endTime equals to startTime

			if(dateTimeCompare(endTime1Val,startTime1Val)==-1)
			{
				isValidTime = false;
				$("#domainMsg").text('End time can not be less than start time');
				endTime1.style.borderColor = '#ff0000';
				endTime1.focus();
				break;
			}
			if(isEmpty(endTime1Val,startTime1Val)!=0 && dateTimeCompare(endTime1Val,startTime1Val)==0)
			{
				isValidTime = false;
				$("#domainMsg").text('End time can not be same as start time');
				endTime1.style.borderColor = '#ff0000';
				endTime1.focus();
				break;
			}
			
			//alert(dateTimeCompare(endTime2Val,startTime2Val));
			// 2.  if endtime 2 < start time 2 -> error - VALIDATION CASE 2
			if(dateTimeCompare(endTime2Val,startTime2Val)==-1)
			{
				isValidTime = false;
				$("#domainMsg").text('End time 2 can not be less than start time 2');
				endTime2.style.borderColor = '#ff0000'; 
				endTime2.focus();
				break;
			}
		
			if(isEmpty(endTime2Val,startTime2Val)!=0 && dateTimeCompare(endTime2Val,startTime2Val)==0)
			{
				isValidTime = false;
				$("#domainMsg").text('End time 2 can not be same as start time 1');
				endTime2.style.borderColor = '#ff0000'; 
				endTime2.focus();
				break;
			}
			
			// 3.  if endtime 3 < start time 3 -> error  VALIDATION CASE 3
			if(dateTimeCompare(endTime3Val , startTime3Val)==-1)
			{
				isValidTime = false;
				$("#domainMsg").text('End time 3 can not be less than start time 3');
				endTime3.style.borderColor = '#ff0000'; 
				endTime3.focus();
				break;
			}
			if(isEmpty(endTime3Val,startTime3Val)!=0 && dateTimeCompare(endTime3Val , startTime3Val)==0)
			{
				isValidTime = false;
				$("#domainMsg").text('End time 3 can not be same as start time 3');
				endTime3.style.borderColor = '#ff0000'; 
				endTime3.focus();
				break;
			}
			
			// 3. start time 2 and end time 2 should not in between start time 1 and end time 1
			/*
			if(startTime2Val <= startTime1Val && startTime2Val <= endTime1Val)
			{
				isValidTime = false;
				$("#domainMsg").text('Start time 2 can not be in between start time 1 and end time 1');
				startTime2.style.borderColor = '#ff0000'; 
				break;
			}
			
			if(endTime2Val <= endTime1Val && endTime2Val <= startTime1Val)
			{
				isValidTime = false;
				$("#domainMsg").text('End time 2 can not be in between start time 1 and end time 1');
				startTime2.style.borderColor = '#ff0000'; 
				break;
			}	
			*/
		}
		return isValidTime;
	}
	
	function dateTimeCompare(time1,time2) { 
		  var t1 = new Date(); 
		  var time1Parts = time1.split(":"); 
		  t1.setHours(time1Parts[0],time1Parts[1],0,0); 
		  var t2 = new Date(); 
		  var time2Parts = time2.split(":"); 
		  t2.setHours(time2Parts[0],time2Parts[1],0,0); 
		  // returns 1 if greater, -1 if less and 0 if the same 
		  if (t1.getTime()>t2.getTime()) return 1; 
		  if (t1.getTime()<t2.getTime()) return -1; 
		  return 0; 
	} 
	
	function isEmpty(time1,time2)
	{
		if(time1.length>0)
			return 1;
		else return 0;
	}
	function saveSweepTimer(){
		
		if(validateAndSaveSweepTimer() == true){
			var sweepTimerObj = "<sweepTimer>";
			sweepTimerObj += "	<id>" + $("#sweepid").val() + "</id>";
			sweepTimerObj += "	<listOfSweepTimerDetails>";
			for (var count = 0; count < 7; count++) {
				sweepTimerObj += "	<sweepTimerDetails>";
				sweepTimerObj += "	<id>" + $("#"+count+"_id").val() + "</id>";
				sweepTimerObj += "	<day>" + $("#"+count+"_day").val() + "</day>";
				sweepTimerObj += "	<shortOrder>"+count+"</shortOrder>";
				sweepTimerObj += "	<overrideTimer>" + $("#"+count+"_overrideTimer").val() + "</overrideTimer>";
				sweepTimerObj += "	<startTime1>" + $("#"+count+"_startTime1").val() + "</startTime1>";
				sweepTimerObj += "	<endTime1>" + $("#"+count+"_endTime1").val() + "</endTime1>";
				sweepTimerObj += "	<startTime2>" + $("#"+count+"_startTime2").val() + "</startTime2>";
				sweepTimerObj += "	<endTime2>" + $("#"+count+"_endTime2").val() + "</endTime2>";
				sweepTimerObj += "	<startTime3>" + $("#"+count+"_startTime3").val() + "</startTime3>";
				sweepTimerObj += "	<endTime3>" + $("#"+count+"_endTime3").val() + "</endTime3>";
				sweepTimerObj += "	</sweepTimerDetails>";
			}
			sweepTimerObj += "	</listOfSweepTimerDetails>";
			sweepTimerObj += "	<name>" + $("#name").val() + "</name>";
			sweepTimerObj += "	</sweepTimer>";
			
			$.ajax({
				   type: "POST",
				   url: '<spring:url value="/services/org/sweeptimer/save"/>',
				   contentType: "application/xml",
				   data: sweepTimerObj,
				   dataType: "json",
				   success: function(msg){
					   if(msg.status == "0") {
							//alert('Sweep Timer saved successfully');
					   } else {
							//alert('Error saving Sweep Timer details!');
					   }  
					   closeDialog();
				   }
				 
			});	
		}
	}
</script>
<div style="padding-top: 10px;padding-left: 5px;padding-right: 5px;padding-bottom: 5px;">
	<spring:url value="/sweeptimer/save.ems" var="actionURL" scope="request" />
	<form:form id="sweeptimer_details" commandName="sweepTimer" method="post"  modelAttribute="sweepTimer"
		action="${actionURL}">
		<form:hidden id="sweepid" path="id" />
		<label style="padding-top: 5;padding-left: 5">Name: </label><input  id="name" name="name" required="required" value="${sweepTimer.name}"  
		style="padding-top: 5;padding-left: 5" onfocus="resetError(this);" />
		<div style="height: 15px;"><span id="domainMsg" style="color: #FF0000"></span></div>
		<table class="entable" style="width: 100%;table-layout: fixed;">
			<thead>
				<tr>
					<th align="center" style="width: 0px;display:none;" ></th>
					<th align="center"><spring:message code="sweeptimers.day" /></th>
					<th align="center"><spring:message code="sweeptimers.starttime1" /></th>
					<th align="center"><spring:message code="sweeptimers.endtime1" /></th>
					<th align="center"><spring:message code="sweeptimers.startime2" /></th>
					<th align="center"><spring:message code="sweeptimers.endtime2" /></th>
					<th align="center"><spring:message code="sweeptimers.startime3" /></th>
					<th align="center"><spring:message code="sweeptimers.endtime3" /></th>
					<th align="center"><spring:message code="sweeptimers.override" /></th>
					
				</tr>
			</thead>
			<c:forEach items="${sweepTimer.sweepTimerDetails}" var="sweepTimerDetail" varStatus="idx">
				<tr id="${idx.index}_row" class="editableRow">
				<td style="width:0px;display:none;">
					<input id="${idx.index}_id" value="${sweepTimerDetail.id}" type="hidden" style="overflow: hidden;width: 0px;visibility: hidden;"/>
				</td>
				<td align="center">
					<div>
						<input readonly="readonly" style="border-style: none;" type="text" size="8" id="${idx.index}_day"  value="${sweepTimerDetail.day}" name="${sweepTimerDetail.day}" />
					</div>
				</td>
				<td align="center">
					<div class="innerContainerInputFieldValue">
							<input  type="text" size="8" id="${idx.index}_startTime1" value="${sweepTimerDetail.startTime1}" 
							name="${sweepTimerDetail.startTime1}"  onchange="validateTime(this);" style="text-align: right;"/>
					</div>
				</td>
				<td align="center">
					<div class="innerContainerInputFieldValue">
							<input  type="text" size="8" style="text-align: right;" id="${idx.index}_endTime1"  value="${sweepTimerDetail.endTime1}"  
							name="${sweepTimerDetail.endTime1}"    onchange="validateTime(this);" style="text-align: right;" />
					</div>
					
				</td>
				<td align="center">
					<div class="innerContainerInputFieldValue">
							<input type="text" size="8" id="${idx.index}_startTime2"  value="${sweepTimerDetail.startTime2}" 
							name="${sweepTimerDetail.startTime2}"  onchange="validateTime(this);"  style="text-align: right;" />
					</div>
				</td>
				<td align="center">
					<div class="innerContainerInputFieldValue">
							<input type="text" size="8" id="${idx.index}_endTime2" value="${sweepTimerDetail.endTime2}" 
							name="${sweepTimerDetail.endTime2}"   onchange="validateTime(this);"  style="text-align: right;" />
					</div>
				</td>
				<td align="center">
					<div class="innerContainerInputFieldValue">
							<input type="text" size="8" id="${idx.index}_startTime3"  value="${sweepTimerDetail.startTime3}" 
							name="${sweepTimerDetail.startTime3}"  onchange="validateTime(this);"  style="text-align: right;" />
					</div>
				</td>
				<td align="center">
					<div class="innerContainerInputFieldValue">
							<input type="text" size="8" id="${idx.index}_endTime3" value="${sweepTimerDetail.endTime3}" 
							name="${sweepTimerDetail.endTime3}"   onchange="validateTime(this);"  style="text-align: right;" />
					</div>
				</td>
				<td align="center">
					<div class="innerContainerInputFieldValue">
							<input  type="text" size="8" id="${idx.index}_overrideTimer"   value="${sweepTimerDetail.overrideTimer}" 
							name="${sweepTimerDetail.overrideTimer}" style="text-align: right;" onchange="validateOverideTime(this);"/>
					</div>
				</td>
				</tr>
			</c:forEach>
		</table>
		<div style="height: 5px;"></div>
		<input id="saveSweepTimerBtn" type="button"	value="<spring:message code="action.save" />" onclick="saveSweepTimer();">
	</form:form>
</div>