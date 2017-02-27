<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<spring:url value="/sweeptimer/delete.ems" var="actionURL" scope="request" />
<spring:url value="/sweeptimer/create.ems" var="createSweepTimerUrl" scope="request" />
<spring:url value="/sweeptimer/edit.ems" var="editSweepTimerUrl" scope="request" />
<spring:url value="/sweeptimer" var="assignSweepTimerUrl" scope="request"/>
<spring:url value="/sweeptimer/list.ems" var="sweeptimerList" />

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

      $(document).ready(function() {
          //define configurations for dialog
          var userDialogOptions = {
              title : "New Sweeptimer",
              modal : true,
              autoOpen : false,
              height : 300,
              width : 500,
              resizeable : false
          };

          $('#newSweepTimerButton').click(function() {
          	clearLabelMessage();
              $("#sweepTimerDetailsDialog").load("${createSweepTimerUrl}"+"?ts="+new Date().getTime()).dialog({
                  title : "New Sweep Timer",
                  width :  Math.floor($('body').width() * .50),
                  minHeight : 300,
                  modal : true,
                  resizable: false
              });
              return false;
          });
        clearLabelMessage();
		var deleteStatus = getParameterByName("deleteStatus");
		if(deleteStatus)
		{
			if(deleteStatus==1)
			{
				displayLabelMessage("Sweep Timer is associated with facility, Please unassign it first.",COLOR_FAULT);
			}else if(deleteStatus==0)
			{
				displayLabelMessage("Sweep Timer deleted successfully.",COLOR_SUCCESS);
			}
		
		}
		
		var assignStatus = getParameterByName("assignStatus");
		if(assignStatus)
		{
			if(assignStatus==1)
			{
				displayLabelMessage("Sweep Timer is associated with facility, Please unassign it first.",COLOR_FAULT);
			}else if(assignStatus==0)
			{
				displayLabelMessage("Sweep Timer has been assign successfully.",COLOR_SUCCESS);
			}
		
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

     
     var editSweepTimer = function(id){
   		clearLabelMessage();
       	$("#sweepTimerDetailsDialog").load('${editSweepTimerUrl}'+"?sweepTimerId="+id +"&ts="+new Date().getTime()).dialog({
               title : "Edit Sweep Timer",
               width :  Math.floor($('body').width() * .50),
               minHeight : 300,
               modal : true,
               resizable: false
           });
           return false;
       };
  	function closeDialog(){
      	$("#sweepTimerDetailsDialog").dialog('close'); 
      	window.top.location = "${sweeptimerList}";      	
      }

  	function beforeDeleteSweepTimer(){
  		clearLabelMessage();
  		if(confirm('Are you sure you want to delete this sweep timer?')){
  			return true;
  		}
  		return false;
  	}
  	
  	var assignSweepTimer = function(url){
  		clearLabelMessage();
      	$("#assignSweepTimerDialog").load(url+"?ts="+new Date().getTime()).dialog({
              title : "Assign Sweep Timer to Facility",
              width :  Math.floor($('body').width() * .38),
              height : 500,
              modal : true
          });
          return false;
      };
      
      function closeAssignSweepTimerDialog(){
      	$("#assignSweepTimerDialog").dialog('close');        	
      }
      
      function displayLabelMessage(Message, Color) {
  		$("#sweepTimer_message").html(Message);
  		$("#sweepTimer_message").css("color", Color);
  	}
      function clearLabelMessage(Message, Color) {
  		displayLabelMessage("", COLOR_DEFAULT);
  	}
  	
  	$(".outermostdiv").css("overflow", "auto");	
	$(".outermostdiv").css("height", $(window).height());
  </script>

	<div id="sweepTimerDetailsDialog"></div>
	<div id="assignSweepTimerDialog"></div>
	
<!-- 	<b>User Settings</b> <br /> -->
<div class="topmostContainer">
	<div class="outermostdiv">
		<div class="outerContainer">
			<span id="sweeptimerlist_header_text"><spring:message code="sweeptimers.management" /></span>
			<div class="i1"></div>
		</div>
		<div class="innerdiv">
		
		<div style="float:left;padding-right:10px;padding-bottom: 10px;">	
			<button id="newSweepTimerButton">Create</button>
			<button id="btnAssignSweepTimer" onclick="return assignSweepTimer('${assignSweepTimerUrl}/assignSweepTimerToFacility.ems');">Assign Sweep Timer</button>
		</div>
			
		<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin')">
			<button id="gotoTenantPage" style="display:none;">Back To Tenants</button>
		</security:authorize>
			
		<div id="sweepTimer_message" style="font-size: 14px; font-weight: bold;padding: 5px 0 0 5px;" ></div>
		
		
		<div style="height:5px;"></div>
			<table id="all_sweeptimers_list" class="entable" style="width: 100%">
				<thead>
					<tr>
						<th align="center" width="40px"><spring:message code="sweeptimers.sno" /></th>
						<th align="center"><spring:message code="sweeptimers.name" /></th>
						<th align="center">Action</th>
					</tr>
				</thead>
		
				<c:forEach items="${sweepTimerList}" var="sweepTimer" varStatus="status">
					<tr>
						<td align="center" width="40px">${status.count}</td>
						<td align="center">${sweepTimer.name}</td>
						<td align="center" style="padding-right:3px;">
							<div align="center" style="padding-right:10px;">
								<form id="sweeptimer-delete" method="post" onsubmit="return beforeDeleteSweepTimer();" action="${actionURL}">
									<button id="btnEditSweepTimer" onclick="return editSweepTimer('${sweepTimer.id}');">Edit</button>
									<input type="hidden" id="sweepTimerId" name="sweepTimerId" value="${sweepTimer.id}"/>
									<input type="submit" value="Delete"/>
								</form>
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
