<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<script type="text/javascript">
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
var COLOR_ERROR = "red";
$(document).ready(function() {
			clearLabelMessage() ;
	
		$("#restApiDocDiv").hide();	
		$("#restApiDocDivButton").hide();	
		var modelAttr = <c:out value="${restModel.validated}"/>
		  if(modelAttr===true)
			  { 
			 		 var expired= '<%=request.getParameter("expired")%>'; 
			 		if(expired=="true")
			 			{
			 				$("#restApiDocDiv").hide();	
			 				$("#restApiDocDivButton").hide();
			 				$("#invalid_file_message").html("Api key has been expired. Please renew");
			 				$("#invalid_file_message").css("color", COLOR_ERROR);
			 			}
			 		else
			 			{
			 				 $("#restApiDocDiv").show();	
							 $("#restApiDocDivButton").show();	
			 			}
			  }
		  var status= '<%=request.getParameter("uploadSatus")%>'; 
		  	if(status == "false")
			  {
			  $("#invalid_file_message").html("Api key validation failed. Please make sure you are using Enlighted license Key file or license is not expired.");
				$("#invalid_file_message").css("color", COLOR_ERROR);
			  }
		  	else if (status == "true")
		  		{
		  		    $("#invalid_file_message").html("Api key validation Successfull.");
					$("#invalid_file_message").css("color", COLOR_SUCCESS);
		  		}
			
		  licenseDeleted();
});

function licenseDeleted()
{
	  var status= '<%=request.getParameter("status")%>'; 
  	if(status == "true") {
  		displayLabelMessage('<spring:message code="system.rest.license.invalidation.success"/>', COLOR_SUCCESS);
  	} 
  	if(status == "false") {
  		displayLabelMessage('<spring:message code="error.rest.license.invalidation.failed"/>', COLOR_ERROR);
  	}
	}
function displayLabelMessage(Message, Color) {
		$("#user_message").html(Message);
		$("#user_message").css("color", Color);
}
function clearLabelMessage() {
	displayLabelMessage("", COLOR_DEFAULT);
	 $("#invalid_file_message").html("");
	 $("#invalid_file_message").css("color", COLOR_DEFAULT);
}
function validateUpload()
{
	 
		    if($("#licenseFileid").val() === ''){
		        alert('Please enter enlighted License file');
		    }
		    else
		    	{
		    	$("#validationForm").submit();

		    	}
		

}

</script>
<div class="outermostdiv">
	
   <div id="restApiDocDiv" class="outerContainer">
    <span>Rest Api Docs</span>
      <div class="i1"></div>
   </div>
	<div id="restApiDocDivButton" class="upperdiv" 
		style="height: 35px; margin: 10px; padding: 10px;">
		<spring:url value="/restdocs/apidocs/restapi.html" var="generateWadl" />
		<form action="${generateWadl}" target="_blank"  commandName="restModel">
			<input type="hidden" id="modelAttr" name="modelAttr" value="${restModel}" />
			
			<input id="restApiDoc" type="submit" value='<spring:message	code="system.cleanup.label.generateWadl" />'  />
		</form>
	</div>
	<div class="outerContainer">
    <span>Enlighted Rest Api Key</span>
      <div class="i1"></div>
   </div>
	<div class="upperdiv"
		style="height: 35px; margin: 10px; padding: 10px;">
		<spring:url  value="/settings/validate.ems" var="insertKey" />
		<form:form id="validationForm" action="${insertKey}" method="post" commandName="restModel" enctype="multipart/form-data">
			<form:input id="licenseFileid" type="file" name="file"  path="licenseFile.file" />
			<input id="saveUserBtnValid" type="button" onclick="validateUpload();" value="upload">
						
		</form:form>
		<div id="invalid_file_message" style="font-size: 14px; font-weight: bold;padding: 5px 0 0 5px;" ></div>
	</div>
	<div class="outerContainer">
    <span>Invalidate Enlighted Rest Api Key</span>
      <div class="i1"></div>
   </div>
	<div class="upperdiv"
		style="height: 35px; margin: 10px; padding: 10px;">
		<spring:url value="/settings/invalidate.ems" var="invalidateKey" />
		<form:form action="${invalidateKey}" method="post" >
			<input id="saveUserBtn" type="submit"
						value="Invalidate">
						
		</form:form>
		<div id="user_message" style="font-size: 14px; font-weight: bold;padding: 5px 0 0 5px;" ></div>
	</div>
	
</div>