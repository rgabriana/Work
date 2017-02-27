<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<style>
	.innerContainer{
		/* padding: 10px 20px; */
	}
	fieldset{padding: 10px;}
    legend{font-weight: bold; margin-left: 10px; padding: 0 2px;}
    
 	//.button{padding: 0 10px;}
    //div.fieldWrapper{clear:both; height:24px; width:40%; margin-bottom:10px;}
 	//div.fieldLabel{float:left; height: 100%; width:30%; font-weight:bold;}
 	//div.fieldValue{float:left; height: 100%; width:65%;}
 	//.input{width:200px; height:95%;}
 	.messageDiv{display:inline; font-weight:bold; padding-left:10px;}
 	//div.spacing-div{height:5px;}
 	//.img-upg-progressbar{height:1em !important; border: 1px solid #DDDDDD !important; border-radius: 4px 4px 4px 4px !important;}
 	//.img-upg-progressbar .ui-progressbar-value{ background-image: url(../themes/default/images/pbar-ani.gif) !important; border-radius: 4px 0 0 4px !important;} 	
 	//div.property-container{width:70%;}
 	//div.property-container div.property-wrapper{width:25%; float:left;}
 	//div.property-container div.property-wrapper .input{width:95%; height:24px;}
 	//div.property-container div.property-wrapper label{font-weight: bold;} 	
 	//div.imageupg-tab-container {border:1px solid #ccc;}
 	//div.tbldiv {margin:10px; padding-right:17px;}
 	//div.image-upgrade-wrapper {background:#fff;}
 	//div fieldset{padding:20px 10px;}
 	.enablebuttonbutton
	{
		padding:3px 5px 5px 5px;
		height:28px; color:#fff; background:url(../images/blue1px.png);
		border:1px solid #3399cc;
	}

	.disablebutton
	{
		padding:3px 5px 5px 5px;
		height:28px;
		color:#fff; 
		background:none;
		border:1px solid #3399cc;
	}
	
	html, body{margin:3px 3px 0px 3px !important; padding:0px !important; background: #ffffff; overflow:hidden !important;}	

	</style>

<spring:url value="/services/org/imageupgrade/jobstatus/" var="getImageUpgradeJobStatus" scope="request" />
<spring:url value="/imageupgrade/startimageupgrade.ems" var="startImageUpgradeUrl" scope="request" />
<spring:url value="/scripts/jquery/jquery.blockUI.2.39.js" var="blockUI"></spring:url>
<script type="text/javascript" src="${blockUI}"></script>

<script type="text/javascript"> 
	//Constants
	COLOR_FAILURE = "red";
	COLOR_SUCCESS = "green";
	COLOR_DEFAULT = "black";
	COLOR_BLACK = "black";
	
$(document).ready(function(){
	//show server message
	displayImageUpgradeMessage("${message}", COLOR_SUCCESS);
	
	//Get Image upgrade Job current running status
	getImageUpgradeJobStatus();
	
	//Global variables
	UPLOAD_FX_IMG_COMBO = $("#fximageId");
	UPLOAD_GW_IMG_COMBO = $("#gwImageId");
	UPLOAD_WDS_IMG_COMBO = $("#wdsimageId");
	loadFixtureImageUploadCombo();
	loadGatewayImageUploadCombo();
	loadWdsImageUploadCombo();
	
});


//Load Fixture Images drop down
function loadFixtureImageUploadCombo(){
	UPLOAD_FX_IMG_COMBO = $("#fximageId");
	
	UPLOAD_FX_IMG_COMBO.empty();
	UPLOAD_FX_IMG_COMBO.append($('<option></option>').val("-1").html("Select a fixture image"));
	<c:forEach items="${fixtureUpgradeimages}" var="fixtureImage">
		UPLOAD_FX_IMG_COMBO.append($('<option></option>').val("${fixtureImage}").html("${fixtureImage}"));
	</c:forEach>
}

//Load Gateway Images drop down
function loadGatewayImageUploadCombo(){
	UPLOAD_GW_IMG_COMBO = $("#gwImageId");
	
	UPLOAD_GW_IMG_COMBO.empty();
	UPLOAD_GW_IMG_COMBO.append($('<option></option>').val("-1").html("Select a gateway image"));
	<c:forEach items="${gatewayUpgradeimages}" var="gatewayImage">
		UPLOAD_GW_IMG_COMBO.append($('<option></option>').val("${gatewayImage}").html("${gatewayImage}"));
	</c:forEach>
}

//Load Wds Images drop down
function loadWdsImageUploadCombo(){
	UPLOAD_WDS_IMG_COMBO = $("#wdsimageId");
	
	UPLOAD_WDS_IMG_COMBO.empty();
	UPLOAD_WDS_IMG_COMBO.append($('<option></option>').val("-1").html("Select a EWS image"));
	<c:forEach items="${wdsUpgradeimages}" var="wdsImage">
		UPLOAD_WDS_IMG_COMBO.append($('<option></option>').val("${wdsImage}").html("${wdsImage}"));
	</c:forEach>
}



function displayImageUpgradeMessage(Message, Color) {
	$("#image_upload_message").html(Message);
	$("#image_upload_message").css("color", Color);
}
function clearImageUpgradeMessage(Message, Color) {
	displaySwitchLabelMessage("", COLOR_DEFAULT);
}

function displayFixtureUpgradeMessage(Message, Color) {
	$("#image_upgrade_message").html(Message);
	$("#image_upgrade_message").css("color", Color);
}
function displayWdsUpgradeMessage(Message, Color) {
	$("#image_upgrade_message").html(Message);
	$("#image_upgrade_message").css("color", Color);
}

function clearFixtureUpgradeMessage(Message, Color) {
	displayFixtureUpgradeMessage("", COLOR_DEFAULT);
}

function displayGatewayUpgradeMessage(Message, Color) {
	$("#image_upgrade_message").html(Message);
	$("#image_upgrade_message").css("color", Color);
}
function clearGatewayUpgradeMessage(Message, Color) {
	displayGatewayUpgradeMessage("", COLOR_DEFAULT);
}
function clearWdsUpgradeMessage(Message, Color) {
	displayWdsUpgradeMessage("", COLOR_DEFAULT);
}

</script>


<script type="text/javascript">
function validateImageUpgradeForm() {
	// save user preference for fixture grid
	//saveGridParameters(FIXTURE_GRID);
	//alert("abc");
	var imageName = $('#imgName').val().toLowerCase();
	if(imageName.indexOf("\\") > -1)
	{
		var imagename_array = imageName.split("\\");
		imageName = imagename_array[imagename_array.length - 1];
	}
	var imageNameArray = imageName.split(".");
	var fileName = imageNameArray[0];
	var fileExtension = imageNameArray[imageNameArray.length - 1];
	var fileNameArray = fileName.split("_");
	var version = fileNameArray[0];
	
	$("#fileName").val(imageName);
	if(imageName == ""){
		displayImageUpgradeMessage("<spring:message code='imageUpgrade.message.validation.emptyFileUpload'/>", COLOR_FAILURE);
		return false;
	}
	if((fileExtension != "bin") && (fileExtension != "tar")){
		alert('enLighted application image with only \'.bin\' or \'.tar\' extension is allowed.\nPlease check the selected filename. \n\nFile name should be of one of the following format: \n[version]_su_app.bin \n[version]_gw_app.bin \n[version]_su_firm.bin \n[version]_gw_firm.bin \n[version]_su_pyc.bin \n[version]_su.bin \n[version]_gw.tar  \n[version]_sw.tar');
		return false;
	}
	// 2.0 pattern
	if( (fileName.match(/su$/g) == null) && (fileName.match(/gw$/g) == null) && (fileName.match(/cu$/g) == null) && (fileName.match(/sw$/g) == null) )  {
		// 1.0 pattern
		if ((fileName.indexOf("su_firm") < 0) 
				&& (fileName.indexOf("su_app") < 0) 
				&& (fileName.indexOf("gw_app") < 0) 
				&& (fileName.indexOf("gw_firm") < 0) 
				&& (fileName.indexOf("su_pyc") < 0))
		{
			alert('enLighted application image filename should contain \'su.bin\' or \'gw.tar\' or \'cu.bin\' or \'sw.bin\'  or \'su_firm\' or \'su_app\' or \'gw_app\' or \'gw_firm\' or \'su_pyc\'.\nPlease check the selected filename. \n\nFile name should be of one of the following format: \n[version]_su_app.bin \n[version]_gw_app.bin \n[version]_su_firm.bin \n[version]_gw_firm.bin \n[version]_su_pyc.bin');
			return false;
		}
	}
	if(!IsNumeric(version)){
		alert('enLighted application image filename should contain version number at the start of the filename. \n\nFile name should be of one of the following format: \n[version]_su_app.bin \n[version]_gw_app.bin \n[version]_su_firm.bin \n[version]_gw_firm.bin \n[version]_su_pyc.bin');
		return false;
	}
	displayImageUpgradeMessage("<spring:message code='imageUpgrade.message.uploadFileWaiting'/>", COLOR_BLACK);
	
}

function IsNumeric(sText)
{
	var ValidChars = "0123456789";
	var IsNumber=true;
	var Char;
	for (i = 0; i < sText.length && IsNumber == true; i++){ 
		Char = sText.charAt(i); 
		if (ValidChars.indexOf(Char) == -1){
			IsNumber = false;
		}
	}
	return IsNumber;
}


	function getImageUpgradeJobStatus()
	{
		$.ajax({
			type: 'GET',
			url: "${getImageUpgradeJobStatus}?ts="+new Date().getTime(),
			data: "",
			success: function(data){
					if(data!=null){
						if(data.msg="Image Running" && data.status==0)
						{
						 	alert("Image Running");
							$("#imgUpgradeSubmitBtn").attr("disabled","disabled");
						 	$("#imgUpgradeSubmitBtn").addClass("disablebutton");
						 	
						}else
						{
							$('#imgUpgradeSubmitBtn').removeAttr("disabled");
						 	$("#imgUpgradeSubmitBtn").removeClass("disablebutton");
						}
					}
				},
				error: function (xhr, st, err) {
		            //If Session Timeout then navigate to login page
		            if (xhr.status === 401) {
		  	    	  window.location="${logouturl}"+"?ts="+new Date().getTime();
		  	   		 }
		        },
			dataType:"json",
			contentType: "application/json; charset=utf-8",
		});
	}
	
	function clearAllImageUpgradeStatusMsg()
	{
		clearFixtureUpgradeMessage();
		clearGatewayUpgradeMessage();
		clearWdsUpgradeMessage();
	}
	
</script>
	<div id="imageUploadDiv" style="background-color:#FFFFFF !important;">
	<div style="height:5px;"></div>
	<span style="font-weight:bold;"><spring:message code="menu.imageupgrade"/></span>
	<div style="height:20px;"></div>
	<fieldset>
			<legend><spring:message code="imageUpgrade.label.uploadImage"/></legend>
			<div class="messageDiv" id="image_upload_message"></div>
			<table>
			<tr><td>
			
			<form action="saveNewImages.ems" id="firmwareupgrade-register" name="firmwareUpgrade" method="post" enctype="multipart/form-data" onsubmit="javascript: return validateImageUpgradeForm();">
				<input type="file" name="upload" id="imgName" />
				<input type="hidden" name="fileName" id="fileName" />
				<input type="submit" name="submit" class="button" value="<spring:message code='imageUpgrade.label.upload'/>" />
				
			</form></td><td>	
			<select id="gwImageId"></select> 
			<select id="fximageId"></select>
			<select id="wdsimageId"></select>
			<button id='imgUpgradeSubmitBtn' onclick='javascript: parent.validateUpgradeForm(3,$("#gwImageId").val(),$("#fximageId").val(),$("#wdsimageId").val());'>
				<spring:message code='imageUpgrade.label.startupgrade' />
			</button>
			</td></tr>
			</table>
			<div class="messageDiv" id="image_upgrade_message"></div>
	</fieldset>
	<div style="height:20px;"></div>
	</div>