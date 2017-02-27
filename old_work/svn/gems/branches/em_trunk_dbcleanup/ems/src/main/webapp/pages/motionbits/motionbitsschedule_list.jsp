<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<spring:url value="/services/org/motionbits/op/deleteschedule/" var="deleteMBScheduleURL" scope="request" />
<spring:url value="/services/org/motionbits/op/stopschedule/" var="stopMBScheduleURL" scope="request" />
<spring:url value="/motionbits/edit.ems" var="editMBScheduleUrl" scope="request" />
<spring:url value="/motionbits/list.ems" var="mbScheduleList" />
<spring:url value="/services/org/gemsgroups/op/deletegroup/" var="deleteGroupUrl" scope="request" />

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
	
	.ui-dialog-title{font-size: 1.4em; font-weight: bolder;}
	
	table#all_sweeptimers_list td{overflow: hidden !important;}
</style>

<script type="text/javascript">
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
var COLOR_FAULT = "red";
var status="";
      $(document).ready(function() {

    	clearLabelMessage();
		var deleteStatus = getParameterByName("deleteStatus");
		if(deleteStatus)
		{
			if(deleteStatus==1)
			{
				displayLabelMessage('<spring:message code="motionbits.delete.fail"/>',COLOR_FAULT);
			}else if(deleteStatus==0)
			{
				displayLabelMessage('<spring:message code="motionbits.delete.success"/>',COLOR_SUCCESS);
			}
		
		}
		var stopStatus = getParameterByName("stopStatus");
		if(stopStatus)
		{
			if(stopStatus==1)
			{
				displayLabelMessage('<spring:message code="motionbits.stop.fail"/>',COLOR_FAULT);
			}else if(stopStatus==0)
			{
				displayLabelMessage('<spring:message code="motionbits.stop.success"/>',COLOR_SUCCESS);
			}
		
		}
		
		var status = getParameterByName("status");
		if(status=="new")
		{
			displayLabelMessage('<spring:message code="sweeptimers.add.success"/>',COLOR_SUCCESS);
		}
		
		if(status=="edit")
		{
			displayLabelMessage('<spring:message code="sweeptimers.edit.success"/>',COLOR_SUCCESS);
		}
         
      });
      function getParameterByName(name) 
      { 
        name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]"); 
        var regexS = "[\\?&]" + name + "=([^&#]*)"; 
        var regex = new RegExp(regexS); 
        var results = regex.exec(window.location.search); 
        if(results == null) 
          return ""; 
        else 
          return decodeURIComponent(results[1].replace(/\+/g, " ")); 
      } 

     
		function deleteSchedule(id, groupId) {
			if(beforeDeleteMBSchedule(false) == false)
				return;
			
			$.ajax({
		 		type: 'POST',
		 		url: "${deleteMBScheduleURL}"+id+"?ts="+new Date().getTime(),
				beforeSend: function() {
					displayLabelMessage("Motionbits configuration deletion activity is scheduled. This page will be automatically refreshed. Please wait..", COLOR_DEFAULT);
				 },
		 		success: function(data){
					$.ajax({
						type: 'POST',
						url: "${deleteGroupUrl}"+groupId+"?ts="+new Date().getTime(),
						dataType: "json",
						success: function(data){
							if(status)
					 			window.location = "/ems/motionbits/list.ems?deleteStatus=1";
							else
			 					window.location = "/ems/motionbits/list.ems?deleteStatus=0";
						},
						error: function(){
				 			window.location = "/ems/motionbits/list.ems?deleteStatus=1";
						},
						dataType : "json",
						contentType : "application/xml; charset=utf-8"
					});
				},
				error: function(){
		 			window.location = "/ems/motionbits/list.ems?deleteStatus=1";
				},
		 		contentType: "application/xml; charset=utf-8"
		 	});							
			
		}

		function stopSchedule(id) {
			if(beforeDeleteMBSchedule(true) == false)
				return;
			
			$.ajax({
		 		type: 'POST',
		 		url: "${stopMBScheduleURL}"+id+"?ts="+new Date().getTime(),
		 		dataType: "json",
				beforeSend: function() {
					displayLabelMessage("Motionbits configuration stop activity is scheduled. This page will be automatically refreshed. Please wait..", COLOR_DEFAULT);
				 },
		 		success: function(data){
		 			if(status)
			 			window.location = "/ems/motionbits/list.ems?stopStatus=1";
		 			else
		 				window.location = "/ems/motionbits/list.ems?stopStatus=0";
				},
				error: function(){
		 			window.location = "/ems/motionbits/list.ems?stopStatus=1";
				},
		 		contentType: "application/xml; charset=utf-8"
		 	});							
			
		}

		var editSchedule = function(id) {
   		clearLabelMessage();
       	$("#mbScheduleDetailsDialog").load('${editMBScheduleUrl}'+"?mbScheduleId="+id +"&ts="+new Date().getTime()).dialog({
               title : "Edit Motion Bit Configuration",
               width :  550,
               height : 500,
               modal : true,
               resizable: false,
			   close: function(event, ui) {
					window.location = "/ems/motionbits/list.ems";
			   }
           });
           return false;
       };
       
  	function closeDialog(statusType){
      	$("#mbScheduleDetailsDialog").dialog('close'); 
		window.location = "/ems/motionbits/list.ems";
      }

  	function beforeDeleteMBSchedule(bStop){
  		clearLabelMessage();
  		if(bStop == true) {
  	  		if(confirm('Are you sure you want to stop the motion bit activity?')) {
  	  			return true;
  	  		}
  		} else {
	  		if(confirm('Are you sure you want to delete this motion bits configuration?')) {
	  			return true;
	  		}
  		}
  		return false;
  	}
  	
      function displayLabelMessage(Message, Color) {
  		$("#motionbits_message").html(Message);
  		$("#motionbits_message").css("color", Color);
  	}
      function clearLabelMessage(Message, Color) {
  		displayLabelMessage("", COLOR_DEFAULT);
  	}
  	
  	$(".outermostdiv").css("overflow", "auto");	
	$(".outermostdiv").css("height", $(window).height());
  </script>

	<div id="mbScheduleDetailsDialog"></div>
	
<div class="topmostContainer">
	<div class="outermostdiv">
		<div class="outerContainer">
			<span id="mbSchedulelist_header_text"><spring:message code="motionbits.management" /></span>
			<div class="i1"></div>
		</div>
		<div class="innerdiv">
		
		<div id="motionbits_message" style="font-size: 14px; font-weight: bold;padding: 5px 0 0 5px;" ></div>
		
		
		<div style="height:5px;"></div>
			<table id="all_mb_schedule_list" class="entable" style="width: 100%">
				<thead>
					<tr>
						<th align="center" width="40px"><spring:message code="motionbits.sno" /></th>
						<th align="center"><spring:message code="motionbits.name" /></th>
						<th align="center">Action</th>
					</tr>
				</thead>
		
				<c:forEach items="${motionBitsScheduleList}" var="mbSchedule" varStatus="status">
					<tr>
						<td align="center" width="40px">${status.count}</td>
						<td align="center">${mbSchedule.displayName}</td>
						<td align="center" style="padding-right:3px;">
							<div align="right" style="padding-right:10px;">
								<input type="button" id="editButton" onClick="editSchedule('${mbSchedule.id}')" value="Edit">
								<input type="button" id="deleteButton" onClick="deleteSchedule('${mbSchedule.id}','${mbSchedule.motionBitGroup.id}')" value="Delete">
								<input type="button" id="stopNowButton" onClick="stopSchedule('${mbSchedule.id}')" value="Stop Now">
							</div>
						</td>				
					</tr>
				</c:forEach>
			</table>
		</div>
	</div>
</div>
<script type="text/javascript">
	$(function() {
		$(window).resize(function() {
			var setSize = $(window).height();
			setSize = setSize - 100;
			$(".topmostContainer").css("height", setSize);
		});
	});
	$(".topmostContainer").css("overflow", "auto");
	$(".topmostContainer").css("height", $(window).height() - 100);
</script>
