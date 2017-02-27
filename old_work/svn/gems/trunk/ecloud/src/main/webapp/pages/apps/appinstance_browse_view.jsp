<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<spring:url value="/services/org/appinstance/toggle/app/tunnel/flag/" var="toggleTunnelSwitch" scope="request" />
<spring:url value="/services/org/appinstance/get/app/tunnel/flag/value/" var="getTunnelSwitch" scope="request" />
<spring:url value="/services/org/appinstance/get/app/browsable/link/" var="getBrowsableLink" scope="request" />
<spring:url value="/services/org/appinstance/reset/browsabilityflag/" var="resetBrowsabilityFlag" scope="request" />
<c:set var="appInstanceId" value="${appInstanceDetailsView.id}" />
<c:set var="openTunnelToCloud" value="${appInstanceDetailsView.openTunnelToCloud}" />

<script>
var stopMsg = "Revoke Access" ;
var startMsg = "Allow Access" ;

var EstablishingMsg = "Establishing Access   " ;
var RevokingMsg = "Revoking Access   " ;
var cancelAccess ="Cancel";
var successMsg = "App access process has started. Process will need 5 mins to complete. You will need to refresh the page after 5 min. you will find browse Link activated."
var errorMsg = "App access process has failed due to internal server error. Contact Adminstrator." ;
var endingMsg = "App access process will end in next 5 mins."; 
var refreshBrowser = "Wait Till the task gets completed. You may need to refresh Browser after 5 mins are over..... "
var browsablelink = "" ;
$(document).ready(function() {
	$('#resetPreviousTaskButton').attr("disabled", false);
	$('#tunnelMsg').text("");
	$('#tunnelerrorMsg').text("");
	$('#inProgressMsg').text("");
	// depending on opentunnelto cloud flag change the button text on loading the page
	if(${appInstanceDetailsView.openTunnelToCloud})
	{
		$('#tunnelbuttonMessage').text(stopMsg);
		
	}
	else if(!${appInstanceDetailsView.openTunnelToCloud})
	{
		$('#tunnelbuttonMessage').text( startMsg);	
	}else
	{
		if(!${appInstanceDetailsView.openTunnelToCloud}) {
			$('#inProgressMsg').text( RevokingMsg);
		}
		else {
			$('#inProgressMsg').text( EstablishingMsg);
		}
			
		
		$('#tunnelbuttonMessage').text(cancelAccess);
	}
	getbrowsableLink();
	$('#appTunnelButton').attr("disabled", false);
	// if brwose is enabled and open tunnell to cloud flag is enabled then show the browse link.
	if(${appInstanceDetailsView.openTunnelToCloud})
	{	
			$('#browseLink').html(browsablelink);
	}
	else
	{
		$('#browseLink').text("");
	}
	

	
$('#appTunnelButton').click(function() {
	
	$('#tunnelMsg').text("");
	$('#tunnelerrorMsg').text("");
	$('#inProgressMsg').text("");
	var currentButtonSelected = $('#tunnelbuttonMessage').text();
	if(currentButtonSelected==cancelAccess)
	{
		cancelBrowsabilityTask();
		return;
	}
	var flag =false ;
	$.ajax({
 		type: 'GET',
 		url: "${getTunnelSwitch}"+${appInstanceDetailsView.id}+"?ts="+new Date().getTime(),
 		dataType: "text",
 		success: function(data){
	 			$.ajax({
	 		 		type: 'GET',
	 		 		async:false,
	 		 		url: "${toggleTunnelSwitch}"+${appInstanceDetailsView.id}+"/"+data+"?ts="+new Date().getTime(),
	 		 		dataType: "text",
	 		 		success: function(data){
	 		 			if(data==="true")
	 		 			{
	 		 				$('#inProgressMsg').text( EstablishingMsg);
	 		 				$('#tunnelbuttonMessage').text(cancelAccess);
	 		 		 		$('#tunnelMsg').text(successMsg);
	 		 		 		flag= true;
	 		 			}else if(data==="false")
	 		 			{
	 		 				$('#inProgressMsg').text( RevokingMsg);
	 		 				$('#tunnelbuttonMessage').text( cancelAccess);
	 		 		 		$('#tunnelMsg').text(endingMsg);
	 		 		 		$('#browseLink').text("");
	 		 		 		flag= true;
	 		 			} else if(data==="error")
	 		 			{
	 		 				$('#tunnelbuttonMessage').text( startMsg);
	 		 				$('#tunnelerrorMsg').text(errorMsg);
	 		 				$('#browseLink').text("");
	 		 				flag= false;
	 		 			}
	 		 			else if(data==="In Progress")
	 		 			{
	 		 				$('#tunnelbuttonMessage').text( cancelAccess);
	 		 				$('#inProgressMsg').text( EstablishingMsg);
	 		 				$('#tunnelerrorMsg').text(refreshBrowser);
	 		 				$('#browseLink').text("");
	 		 				flag= false;
	 		 			}
	 		 		 	
	 				},
	 				error: function(){
	 					$('#tunnelbuttonMessage').text( startMsg);
	 					$('#tunnelerrorMsg').text(errorMsg);
	 					$('#browseLink').text("");
	 					flag= false;
	 				}
	 		 	});
 
		},
		error: function(){
			alert(errorMsg);
			flag=false ;
		}
 	});/* end of toggleTunnelSwitch */
 	
 	return flag ;

}); /* end of getTunnelSwitch */


}); /* end of document ready  */

function getbrowsableLink()
{
	$.ajax({
	 		type: 'GET',
	 		url: "${getBrowsableLink}"+${appInstanceDetailsView.id}+"?ts="+new Date().getTime(),
	 		async:false,
	 		dataType: "text",
	 		success: function(data){
	 			browsablelink="<a href=" +data+" target=\"_blank\">Link</a>"; 
	 		 	
			},
	 	});
}

function cancelBrowsabilityTask()
{
	$.ajax({
 		type: 'GET',
 		url: "${resetBrowsabilityFlag}"+${appInstanceDetailsView.id}+"?ts="+new Date().getTime(),
 		dataType: "text",
 		success: function(data){
 			if(data==="true")
	 		{
 				$('#tunnelbuttonMessage').text(startMsg);
	 		}
 		}
 	});
}


</script>

<style>
html,body {
    background-color: #FFFFFF !important;
    
}
</style>

		<div style="width: 100%; height: 100%; background: #fff; padding: 10px 10px 20px 10px" id="detailsDiv">
			<div class="field">
				<div class="formPrompt"><span>Name:</span></div>
				<div class="formValue">${appInstanceDetailsView.name}</div>
			</div>
			<security:authorize access="hasAnyRole('Admin','SystemAdmin','SupportAdmin','SPPA')">
			<div class="field">
				<div class="formPrompt"><span>Start Browsing Process:</span></div>
				<span id="inProgressMsg" class="success"></span><button id="appTunnelButton"> <span id="tunnelbuttonMessage"></span></button><br></br> <span id="tunnelMsg" class="success"></span> <span id="tunnelerrorMsg" class="error"></span>
			</div>
			</security:authorize>
			<div class="field">
				<div class="formPrompt"><span>Browse Link:</span></div>
				<div class="formValue"><span id="browseLink" class="success"></span></div>
			</div>
			<div class="field">
				
			</div>
	    </div>