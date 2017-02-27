<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<style type="text/css">
html, body{margin:3px 0px 0px 0px !important; background: #ffffff !important; overflow: hidden !important;}
</style>

<spring:url value="/floor/saveFloorUploadFile.ems"
	var="saveFloorUploadFile" scope="request" />

<script type="text/javascript">

	//Constants
	COLOR_FAILURE = "red";
	COLOR_SUCCESS = "green";
	COLOR_DEFAULT = "black";
	COLOR_BLACK = "black";

	$(document).ready(function() {
		
		clearEmConfigFileUploadMessage();
		
		var uploadStatus = "${uploadStatus}";
		
		var message = "${message}";
		
		//console.log("uploadStatus:"+uploadStatus);
		
		//console.log("message:"+message);
			
		if(uploadStatus != null && uploadStatus == "true"){
			displayEmConfigFileUploadMessage("File uploaded successfully", COLOR_SUCCESS);
		}
		
		if(uploadStatus != null && uploadStatus == "false" && message != null && message != ""){
			displayEmConfigFileUploadMessage(message, COLOR_FAILURE);
		}else if (uploadStatus != null && uploadStatus == "false"){
			displayEmConfigFileUploadMessage("Cannot upload the zip file as devices.xml file is not found.", COLOR_FAILURE);
		}
			
	});
	
	function validateUploadZipFile() {
		clearEmConfigFileUploadMessage();
		var imageName = $('#zipFileName').val().toLowerCase();
		if(imageName.indexOf("\\") > -1)
		{
			var imagename_array = imageName.split("\\");
			imageName = imagename_array[imagename_array.length - 1];
		}
		var imageNameArray = imageName.split(".");
		var fileName = imageNameArray[0];
		var fileExtension = imageNameArray[imageNameArray.length - 1];
		
		$("#fileName").val(imageName);
		if(imageName == ""){
			displayEmConfigFileUploadMessage("Please select a file to upload.", COLOR_FAILURE);
			return false;
		}
		if(fileExtension != "zip"){
			displayEmConfigFileUploadMessage("Only files with zip extension is allowed.Please check the selected filename.", COLOR_FAILURE);
			return false;
		}
		
		displayEmConfigFileUploadMessage("File is getting uploaded.Please wait.", COLOR_SUCCESS);
	}
	
	function displayEmConfigFileUploadMessage(Message, Color) {
		$("#emconfig_upload_message").html(Message);
		$("#emconfig_upload_message").css("color", Color);
	}
	
	function clearEmConfigFileUploadMessage() {
		displayEmConfigFileUploadMessage("", COLOR_DEFAULT);
	}
	
	
</script>

<div id="imageUploadDiv" style="background-color:#FFFFFF !important;">
<fieldset>
		<legend>Upload new EMConfig file</legend>			
		<table>
			<tr>
				<td>
				
					<form action="${saveFloorUploadFile}" id="saveFloorUploadFileId" name="saveFloorUploadFile" method="post" enctype="multipart/form-data" onsubmit="javascript: return validateUploadZipFile();">
						<input type="file" name="upload" id="zipFileName" />
						<input type="hidden" name="fileName" id="fileName" />
						<input type="submit" name="submit" class="button" value="<spring:message code='imageUpgrade.label.upload'/>" />
						
					</form>
				</td>
			</tr>
		</table>
		<div class="messageDiv" id="emconfig_upload_message"></div>
		
</fieldset>
</div>

