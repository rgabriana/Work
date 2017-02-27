<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<spring:url value="/services/org/eminstance/toggle/em/tunnel/flag/" var="toggleTunnelSwitch" scope="request" />
<spring:url value="/services/org/eminstance/get/em/tunnel/flag/value/" var="getTunnelSwitch" scope="request" />
<spring:url value="/services/org/eminstance/get/em/browsable/link/" var="getBrowsableLink" scope="request" />
<spring:url value="/services/org/eminstance/reset/browsabilityflag/" var="resetBrowsabilityFlag" scope="request" />
<c:set var="emInstanceId" value="${emInstanceDetailsView.id}" />
<c:set var="openTunnelToCloud" value="${emInstanceDetailsView.openTunnelToCloud}" />

<script>
var stopMsg = "Revoke Access" ;
var startMsg = "Allow Access" ;
//var InProgressMsg = "Earlier task in Progress" ;
var InProgressMsg = "Establishing Access   " ;
var cancelAccess ="Cancel";
var successMsg = "Energy Manager access process has started. Process will need 5 mins to complete. You will need to refresh the page after 5 min. you will find browse Link activated"
var errorMsg = "Energy Manager access process has failed due to internal server error. Contact Adminstrator" ;
var endingMsg = "Energy Manager access process will end in next 5 mins."; 
var supportedVersion = '${systemConfiguration.value}' ;
var versionErrorMesage = "Only version of EM above "+supportedVersion+" is supported for browsing" ;
var notSupported = "Energy Manager access not Supported!!" ;
var refreshBrowser = "Wait Till the task get completed. You may need to refresh Browser after 5 mins are over..... "
var browsablelink = "" ;
$(document).ready(function() {
	$('#resetPreviousTaskButton').attr("disabled", false);
	$('#tunnelMsg').text("");
	$('#tunnelerrorMsg').text("");
	$('#inProgressMsg').text("");
	// depending on opentunnelto cloud flag change the button text on loading the page
	if(${emInstanceDetailsView.openTunnelToCloud}&&${emInstanceDetailsView.browseEnabledFromCloud})
	{
		$('#tunnelbuttonMessage').text(stopMsg);
		
	}
	else if(!${emInstanceDetailsView.openTunnelToCloud}&&!${emInstanceDetailsView.browseEnabledFromCloud})
	{
		$('#tunnelbuttonMessage').text( startMsg);	
	}else
	{
		$('#inProgressMsg').text( InProgressMsg);
		$('#tunnelbuttonMessage').text(cancelAccess);
	}
	getbrowsableLink();
	var running_version = parseVersionString('${emInstanceDetailsView.version}');
	var latest_version = parseVersionString(supportedVersion);
	// check for version. if below show not supported if above then show button and links enabled.
	if (running_version.major < latest_version.major) {
		$('#browseLink').text(versionErrorMesage);
		$('#emTunnelButton').attr("disabled", true);
		$('#tunnelbuttonMessage').text(notSupported);
		
		
	} else if (running_version.minor < latest_version.minor|| running_version.patch < latest_version.patch ||  running_version.remain < latest_version.remain ) {
		$('#browseLink').text(versionErrorMesage);
		$('#emTunnelButton').attr("disabled", true);
		$('#tunnelbuttonMessage').text(notSupported);
	} else {
		$('#emTunnelButton').attr("disabled", false);
		// if brwose is enabled and open tunnell to cloud flag is enabled then show the browse link.
		if(${emInstanceDetailsView.browseEnabledFromCloud}&&${emInstanceDetailsView.openTunnelToCloud})
		{	
				$('#browseLink').html(browsablelink);
		}
		else
		{
			$('#browseLink').text("");
		}
	}
	

	
$('#emTunnelButton').click(function() {
	
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
 		url: "${getTunnelSwitch}"+${emInstanceDetailsView.id}+"?ts="+new Date().getTime(),
 		dataType: "text",
 		success: function(data){
	 			$.ajax({
	 		 		type: 'GET',
	 		 		async:false,
	 		 		url: "${toggleTunnelSwitch}"+${emInstanceDetailsView.id}+"/"+data+"?ts="+new Date().getTime(),
	 		 		dataType: "text",
	 		 		success: function(data){
	 		 			if(data==="true")
	 		 			{
	 		 				$('#inProgressMsg').text( InProgressMsg);
	 		 				$('#tunnelbuttonMessage').text(cancelAccess);
	 		 		 		$('#tunnelMsg').text(successMsg);
	 		 		 		flag= true;
	 		 			}else if(data==="false")
	 		 			{
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
	 		 				$('#inProgressMsg').text( InProgressMsg);
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
	 		url: "${getBrowsableLink}"+${emInstanceDetailsView.id}+"?ts="+new Date().getTime(),
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
 		url: "${resetBrowsabilityFlag}"+${emInstanceDetailsView.id}+"?ts="+new Date().getTime(),
 		dataType: "text",
 		success: function(data){
 			if(data==="true")
	 		{
 				$('#tunnelbuttonMessage').text(startMsg);
	 		}
 		}
 	});
}
function parseVersionString (str) {
    if (typeof(str) != 'string') { return false; }
    var x = str.split('.');
    var maj = parseInt(x[0]) || 0;
    var min = parseInt(x[1]) || 0;
    var pat = parseInt(x[2]) || 0;
    var rem = parseInt(x[3]) || 0;
    return {
        major: maj,
        minor: min,
        patch: pat,
        remain: rem
    }
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
				<div class="formValue">${emInstanceDetailsView.name}</div>
			</div>
			<security:authorize access="hasAnyRole('Admin','SystemAdmin','SupportAdmin','SPPA')">
			<div class="field">
				<div class="formPrompt"><span>Start Browsing Process:</span></div>
				<span id="inProgressMsg" class="success"></span><button id="emTunnelButton"> <span id="tunnelbuttonMessage"></span></button><br></br> <span id="tunnelMsg" class="success"></span> <span id="tunnelerrorMsg" class="error"></span>
			</div>
			</security:authorize>
			<div class="field">
				<div class="formPrompt"><span>Browse Link:</span></div>
				<div class="formValue"><span id="browseLink" class="success"></span></div>
			</div>
			<div class="field">
				
			</div>
	    </div>