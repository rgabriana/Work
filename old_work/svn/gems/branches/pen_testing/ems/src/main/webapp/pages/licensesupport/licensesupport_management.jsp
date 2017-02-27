<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/licenseSupport/uploadNewLicenseFile.ems" var="licenseUploadUrl" />

<style type="text/css">
	.no-close .ui-dialog-titlebar-close {display: none }
	html {height:100% !important;}

	fieldset {
	    border:1px solid #999;
	    border-radius:8px;
		padding: 5px 5px 5px 5px;
	}

</style>

<script type="text/javascript">

var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
var COLOR_ERROR = "red";

$(document).ready(function() {
	var uploadStatus = '<%=request.getParameter("uploadStatus")%>';
	
	if(uploadStatus == "false")
	{
		$("#error").html("<spring:message code='licensefile.fail'/>");
		$("#error").css("color", COLOR_ERROR);
	}
	else if (uploadStatus == "true")
	{
		$("#error").html("<spring:message code='licensefile.success'/>");
		$("#error").css("color", COLOR_SUCCESS);
	}
	else if(uploadStatus == "duplicate"){
		$("#error").html("<spring:message code='licensefile.duplicate'/>");
		$("#error").css("color", COLOR_ERROR);
	}
	else{
		$("#error").html("");
		$("#error").css("color", COLOR_DEFAULT);
	}
	
	$(window).trigger('resize');
});

function checkLicenseFileSize(obj) { 
	$("#error").html("");
	$("#error").css("color", COLOR_DEFAULT);
	
	var license_filesize_limit_kb = 10 * 1024 * 1024;
	
	if(obj.files[0].size > license_filesize_limit_kb) {
		$('#licenseFileId').val('');
		$('#error').html('Licnese file should be less than 10 MB');
		$("#error").css("color", COLOR_ERROR);
	}
}

function validateLicenseUploadForm(){
	var licenseFileName = $('#licenseFileId').val();
	if(licenseFileName == ""){
		$('#error').html('Please select a license file.');
		$("#error").css("color", COLOR_ERROR);
		return false;
	}
}

$(window).resize(function() {
    
});

</script>


<div id="licenseSupportDiv" style="width: 100%;height: 100%;background: #fff; padding: 0px 5px 0px 0px" >
<div id="outerDiv" style="padding: 5px 5px 5px 5px;">
	<div><label style="padding: 5px 5px 5px 5px;font-weight:bold">License Management</label></div>
	<div style="height:5px"></div>
	<fieldset>
	<div>
		<div id="pricingDiv"><label style="padding: 5px 5px 5px 5px;font-weight:bold">UUID : ${UUID}</label></div>
		<div style="height:5px"></div>
		<form action="${licenseUploadUrl}" id="uploadNewLicenseFileId" name="uploadNewLicenseFile" method="post" enctype="multipart/form-data" onsubmit="javascript: return validateLicenseUploadForm();">
			<input type="file" name="upload" id="licenseFileId" onChange="checkLicenseFileSize(this);" accept=".license"/>
			<input type="submit" name="submit" class="button" value="Upload License" />
		</form>
		<div style="height:5px"></div>
		<span id="error"></span>
		<div style="height:5px"></div>
	</div>
	</fieldset>
	<div style="height:5px"></div>
	<c:if test="${totalNoOfEmDevices gt 0}">
	<fieldset>
	<legend><span>Energy Manager</span></legend>
	<div>
		<table style="width: 100%;">
			<tr>
				<td align="left" style="padding: 5px 5px 5px 5px;font-weight:bold">License Details
				</td>
			 </tr>
			 <tr>
				 <table cellpadding="0" cellspacing="10" style="padding-top:5px;padding-bottom:15px;width: 100%;border-collapse: collapse;" >
				 	<tr style="border: 1px solid black;">
					 	<td style="padding:10px 15px 10px 5px;font-weight:bold;border: 1px solid #e5e5e5 !important;">Product Code</td>
						<td style="padding:10px 15px 10px 5px;font-weight:bold;border: 1px solid #e5e5e5 !important;">Number of Sensors</td>
				 	</tr>
				 	<c:forEach items='${licenses.em.emLicenseInstanceList}' var='emLicenseInstance'>
					<tr style="border: 1px solid black;">
						<td style="padding:5px 15px 5px 5px;border: 1px solid #e5e5e5 !important;">${emLicenseInstance.productId}</td>
						<td style="padding:5px 15px 5px 5px;border: 1px solid #e5e5e5 !important;">${emLicenseInstance.noofdevices}</td>
					</tr>
					</c:forEach>
				 </table>
			 </tr>
		</table>
	</div>
	</fieldset>
	</c:if>
	<div style="height:5px"></div>
	<c:if test="${licenses.bacnet.enabled == true}">
	<c:if test="${totalNoOfBacnetDevices gt 0}">
	<fieldset>
	<legend><span>BACnet</span></legend>
	<div>
		<table style="width: 100%;">
			
			<tr>
				<td align="left" style="padding: 5px 5px 5px 5px;font-weight:bold">License Details
				</td>
			 </tr>
			 <tr>
				 <table cellpadding="0" cellspacing="10" style="padding-top:5px;padding-bottom:15px;width: 100%;border-collapse: collapse;">
				 	<tr style="border: 1px solid black;">
					 	<td style="padding:10px 15px 10px 5px;font-weight:bold;border: 1px solid #e5e5e5 !important;">Product Code</td>
						<td style="padding:10px 15px 10px 5px;font-weight:bold;border: 1px solid #e5e5e5 !important;">Number of Sensors</td>
				 	</tr>
				 	<c:forEach items='${licenses.bacnet.bacnetLicenseInstanceList}' var='bacnetLicenseInstance'>
					<tr style="border: 1px solid black;">
						<td style="padding:5px 15px 5px 5px;border: 1px solid #e5e5e5 !important;">${bacnetLicenseInstance.productId}</td>
						<td style="padding:5px 15px 5px 5px;border: 1px solid #e5e5e5 !important;">${bacnetLicenseInstance.noofdevices}</td>
					</tr>
					</c:forEach>
				 </table>
			 </tr>
		</table>
	</div>
	</fieldset>
	</c:if>
	</c:if>
	<div style="height:5px"></div>
	<c:if test="${licenses.zoneSensors.enabled == true}">
	<c:if test="${totalNoOfZoneSensors gt 0}">
	<fieldset>
	<legend><span>Zone Sensors</span></legend>
	<div>
		<table style="width: 100%;">
			
			<tr>
				<td align="left" style="padding: 5px 5px 5px 5px;font-weight:bold">License Details
				</td>
			 </tr>
			 <tr>
				 <table cellpadding="0" cellspacing="10" style="padding-top:5px;padding-bottom:15px;width: 100%;border-collapse: collapse;">
				 	<tr style="border: 1px solid black;">
					 	<td style="padding:10px 15px 10px 5px;font-weight:bold;border: 1px solid #e5e5e5 !important;">Product Code</td>
						<td style="padding:10px 15px 10px 5px;font-weight:bold;border: 1px solid #e5e5e5 !important;">Number of Zone Sensors</td>
				 	</tr>
				 	<c:forEach items='${licenses.zoneSensors.zoneSensorsLicenseInstanceList}' var='zoneSensorsLicenseInstance'>
					<tr style="border: 1px solid black;">
						<td style="padding:5px 15px 5px 5px;border: 1px solid #e5e5e5 !important;">${zoneSensorsLicenseInstance.productId}</td>
						<td style="padding:5px 15px 5px 5px;border: 1px solid #e5e5e5 !important;">${zoneSensorsLicenseInstance.noofdevices}</td>
					</tr>
					</c:forEach>
				 </table>
			 </tr>
		</table>
	</div>
	</fieldset>
	</c:if>
	</c:if>
	<div style="height:5px"></div>
	<c:if test="${licenses.occupancySensor.enabled == true}">
		<fieldset>
			<div style="padding: 5px 5px 5px 5px;font-weight:bold">
				Sensor Occupancy License : Enabled
			</div>
		</fieldset>
		<div style="height:5px"></div>
	</c:if>
</div>
</div>