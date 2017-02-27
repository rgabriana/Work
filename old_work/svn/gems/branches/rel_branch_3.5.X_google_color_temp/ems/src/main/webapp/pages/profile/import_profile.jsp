<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/profile/uploadprofile.ems" var="uploadProfileUrl" scope="request" />
<style>

#center {
  height : 95% !important;
}
	  th{text-align:left; border-right:0 none;}
	  th span.ui-jqgrid-resize{display:none !important;}
	 .label{font-weight: bold; font-size: 0.9em; color: #555555;}
	 .highlightGray{background-color: #EEEEEE;}
	 .fieldWrapper{padding-bottom:2px;padding-top: 5px;}
	 .fieldPadding{height:10px;}
	 .fieldlabel{float:left; height:20px; width: 60%; font-weight: bold;}
	 .fieldlabel2{float:left; height:20px; width: 20%; font-weight: bold;}
	 .fieldlabel1{float:left;padding-left:5px; height:20px; width: 10%; font-weight: bold;}
	 .fieldInput{float:left; height:20px; width:20%;}
	 .fieldInput1{float:left; height:20px; width:20%;}
	 .fieldInputCombo{float:left; height:23px; width: 30%;}
	 .fieldInputCombo1{float:left; height:23px; width: 15%;}
	 .fieldInputFile{float:left; height:23px; width:90%;}
	 .text {height:100%; width:100%;}
	 .readOnly {border:0px none;}
</style>

<script type="text/javascript">

var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
var ballastId;
$(document).ready(function(){
	clearLabelMessage();
});

function validateProfileUpload()
{
	clearLabelMessage();
	var form = $('#upload-profile-form');
	var file = $('input[type="file"]', form).val();
	var isValid = true;
	if ( file ) {
		var get_ext = file.split('.');
		get_ext = get_ext.reverse();
		
		var extension = get_ext[0];
		var arr = ["xml"];
	    var indexVal = $.inArray(extension, arr);
		if (indexVal!=-1){
		} else {
			isValid = false;
			displayLabelMessage("Invalid file type, Please select valid xml file.", COLOR_FAILURE);
		}
	}else{
		isValid = false;
		displayLabelMessage("Please select the xml file.", COLOR_FAILURE);
	}
	return isValid;
}
function displayLabelMessage(Message, Color) {
	$("#upload_profile_message").html(Message);
	$("#upload_profile_message").css("color", Color);
}

function clearLabelMessage(Message, Color) {
	displayLabelMessage("", COLOR_DEFAULT);
}

function onImportProfileFormSubmit()
{
	if(validateProfileUpload())
	{
		var form = document.getElementById('upload-profile-form');
		var formData = new FormData(form);
		var xhr = new XMLHttpRequest();
		xhr.onreadystatechange = onuploadSucess;
		xhr.open('POST', '${uploadProfileUrl}', true);
		xhr.send(formData); 
		
	}
}

var onuploadSucess = function(event) {
	 if( event.target.readyState == 4 && event.target.status == 200){
		 	var responseText = this.responseText;
		 	var json = $.parseJSON(responseText);
			isError = json.hasOwnProperty('error');
			if(isError == undefined || isError == null || !isError){
				$('#uploadProfileDialog').dialog('destroy');
				showImportProfileForm();
			}else{
				alert(json.message);
			}
	}
}
function cancelUploadProfileDialog()
{
	$('#uploadProfileDialog').dialog('destroy');
}

</script>
<form action="${uploadProfileUrl}" id="upload-profile-form" method="post" enctype="multipart/form-data">
<div class="outermostdiv" style="margin-left:0px;margin-top: 10px;">
	 <table id="ip-main-box" style="width:95%; height:100%; border: 1px; padding-left: 5px;" >
	 <tr>
	 <td colspan="2">
	 <div id="upload_profile_message" style="font-size: 14px; font-weight: bold; padding: 5px 0 0 0px; float: left;width: 100%"></div>
	 </td>
	 </tr>
	<tr>
		 <td width="30%">
				<div class="fieldWrapper">
					<div class="fieldlabel"><label for="fixturename">Import profile path:</label></div>
					<div class="fieldInput"></div>
					<br style="clear:both;"/>
				</div>
		 </td>
		 <td width="50%">	<div class="fieldWrapper">					 
						<input type="file" id="fileSelectObj" class="fieldInputFile"  name="upload"/>
				</div>	
		 </td>
	 </tr>
	 <tr>
	 	<td width="30%"> <div class="fieldPadding"></div></td>
	 	<td></td>
	 </tr>
	 <tr>
		 <td width="30%">
		 </td>
		   <td width="50%">	
		  <input id="uploadProfileBtn" type="button" onclick="onImportProfileFormSubmit()" value="Upload">
		  <input type="button" id="cancelUploadProfileBtn" name="Cancel" value="Cancel" onclick="cancelUploadProfileDialog()"/>	
		 </td>
	 </tr>
	</table>
</div> 
</form>