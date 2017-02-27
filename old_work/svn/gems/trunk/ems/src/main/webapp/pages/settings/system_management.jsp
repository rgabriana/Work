<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<spring:url value="/services/org/fixture/delete/all/discovered" var="deletealldicoveredfixture" scope="request" />
<spring:url value="/services/org/fixture/delete/all/placed" var="deleteallplacedfixture" scope="request" />
<spring:url value="/services/systemconfig/details" var="getExistingTemperatureUnitUrl" scope="request" />
<spring:url value="/services/systemconfig/edit" var="editTemperatureUnitUrl" scope="request" />

<spring:url value="/services/systemconfig/details" var="getExistingPasswordExpiryFlagUrl" scope="request" />
<spring:url value="/services/systemconfig/edit" var="editPasswordExpiryFlagUrl" scope="request" />

<spring:url value="/settings/editsystemconfig.ems" var="editSystemConfigDialogUrl" scope="request" />
<spring:url value="/settings/editfloorplanconfig.ems" var="editFloorPlanImageSizeDialogUrl" scope="request" />
<spring:url value="/settings/editttlconfig.ems" var="editTTLValueDialogUrl" scope="request" />

<spring:url value="/services/systemconfig/details" var="getExistingSoftMeteringUrl" scope="request" />
<spring:url value="/services/systemconfig/edit" var="editSoftMeteringFlagUrl" scope="request" />

<script type="text/javascript">

var existingTempUnit;
var id;
var existingPasswordExpFlag;
var passExpId;
var softMeteringId=-1;
var existingSoftMetering;
$(document).ready(function() {
	$("#temperatureUnitStatus").html("") ;
	$.ajax({
		type:'POST',
		data: "",
		url: "${getExistingTemperatureUnitUrl}"+"/"+"temperature_unit"+"?ts="+new Date().getTime(),
		success: function(data){
			if(data == null){
				$("#temperatureUnitStatus").html("Unable to find temperature unit");
			}else{
				id = data.id;
				existingTempUnit = data.value;
				switch(data.value){
				case "F":
				case "f":
					var radioFahrenheit = document.getElementById("fahrenheit");
					radioFahrenheit.checked = true;
					break;
				case "C":
				case "c":
					var radioCelcius = document.getElementById("celcius");
					radioCelcius.checked = true;
					break;					
				}				
			}
		},
		dataType:"json",
		contentType: "application/xml; charset=utf-8"
	});	
	
	// Get SoftMetering Flag Details.
	//perf.pmStatsMode = 1 = PM_STATS_FIRMWARE_MODE = SoftMetering Disabled
	//perf.pmStatsMode = 2 = PM_STATS_GEMS_MODE = SoftMetering Enabled
	$("#softMeteringStatus").html("") ;
	$.ajax({
		type:'POST',
		data: "",
		url: "${getExistingSoftMeteringUrl}"+"/"+"enable.softmetering"+"?ts="+new Date().getTime(),
		success: function(data){
			if(data == null){
				$("#softMeteringStatus").html("Unable to find softmetering flag status");
			}else{
				softMeteringId = data.id;
				existingSoftMetering = data.value;
				
				if(data.value == "true"){
					document.getElementById("softMeteringFlag").checked = true;
				}else{
					document.getElementById("softMeteringFlag").checked = false;
				}
			}
		},
		dataType:"json",
		contentType: "application/xml; charset=utf-8"
	});	
});

$(document).ready(function() {
	$("#passwordExpiryStatus").html("") ;
	$.ajax({
		type:'POST',
		data: "",
		url: "${getExistingPasswordExpiryFlagUrl}"+"/"+"em.forcepasswordexpiry"+"?ts="+new Date().getTime(),
		success: function(data){
			if(data == null){
				$("#passwordExpiryStatus").html("Unable to find password expiry status");
			}else{
				passExpId = data.id;
				existingPasswordExpFlag = data.value;
				switch(data.value){
				case "False":
				case "false":
					document.getElementById("passwordExp").checked = false;
					break;
				case "True":
				case "true":
					document.getElementById("passwordExp").checked = true;
					break;					
				}				
			}
		},
		dataType:"json",
		contentType: "application/xml; charset=utf-8"
	});	
});

function deleteAllDiscoveredFixture()
{
	if(confirm('Are you sure you want to delete all discovered but not commissioned devices?')){
	var fixtureDeletedCount ;
	$.ajax({
        type: 'POST',
        url: "${deletealldicoveredfixture}",
        async:false,
        success: function(data){
        	
            if(data != null){
                var xml=data.getElementsByTagName("response");
                for (var j=0; j<xml.length; j++) {
                    var status = xml[j].getElementsByTagName("status")[0].childNodes[0].nodeValue;
                   fixtureDeletedCount= xml[j].getElementsByTagName("msg")[0].childNodes[0].nodeValue;
                 
                    }
                $("#deletefixturecount").html("Total Deleted Devices " + fixtureDeletedCount) ;
                }
            else
            	{
            	fixtureDeletedCount=0 ;
            	$("#deletefixturecount").html("Total Deleted Devices " + fixtureDeletedCount) ;
            	}
            }
        });	
	}
	
	if(fixtureDeletedCount==undefined)
	{
	fixtureDeletedCount=0 ;
	$("#deletefixturecount").html("Total Deleted Devices " + fixtureDeletedCount) ;
	}
}

function deleteAllPlacedFixture()
{
	if(confirm('Are you sure you want to delete all placed but not commissioned devices?')){
	var placedFixtureDeletedCount ;
	$.ajax({
        type: 'POST',
        url: "${deleteallplacedfixture}",
        async:false,
        success: function(data){
        	
            if(data != null){
                var xml=data.getElementsByTagName("response");
                for (var j=0; j<xml.length; j++) {
                    var status = xml[j].getElementsByTagName("status")[0].childNodes[0].nodeValue;
                    placedFixtureDeletedCount= xml[j].getElementsByTagName("msg")[0].childNodes[0].nodeValue;
                 
                    }
                $("#deleteplacedfixturecount").html("Total Deleted Placed Devices " + placedFixtureDeletedCount) ;
                }
            else
            	{
            	placedFixtureDeletedCount=0 ;
            	$("#deleteplacedfixturecount").html("Total Deleted Placed Devices " + placedFixtureDeletedCount) ;
            	}
            }
        });	
	}
	
	if(placedFixtureDeletedCount==undefined)
	{
		placedFixtureDeletedCount=0 ;
		$("#deleteplacedfixturecount").html("Total Deleted Placed Devices " + placedFixtureDeletedCount) ;
	}
}


function cleanCache()
{
	if(confirm('Are you sure you want to Clean System Cache ?')){
		return true;
	}
	return false;
}

function editSystemConfigurationValue()
{
	$("#editSystemConfigDialog").load("${editSystemConfigDialogUrl}"+"?ts="+new Date().getTime()).dialog({
        title : "Edit SystemConfig Value",
        width :  Math.floor($('body').width() * .30),
        minHeight : 250,
        modal : true
    });
}

function onRadioSelection(){
	$("#temperatureUnitStatus").html("");
}

function chkTemperatureUnitSelection()
{
	var radioFahrenheit = document.getElementById("fahrenheit");
	var radioCelcius = document.getElementById("celcius");
	var updatedTempUnit = "";
	if(radioFahrenheit.checked == true)
	{
		updatedTempUnit = "F";		
	}
	else
	{ 
		if(radioCelcius.checked == true)
		{
			updatedTempUnit = "C";			
		}
	}	
				
	if(existingTempUnit.toUpperCase() != updatedTempUnit.toUpperCase()){		
		updateTemperatureUnitValue(updatedTempUnit);
	}		
}

var systemConfigXML = "";

function updateTemperatureUnitValue(updatedTempUnit){	
		
	systemConfigXML = "<systemConfiguration>"+
	"<id>"+id+"</id>"+
	"<name>"+"temperature_unit"+"</name>"+
	"<value>"+updatedTempUnit+"</value>"+
	"</systemConfiguration>";
	
	$.ajax({
			data: systemConfigXML,
			type: "POST",
			url: "${editTemperatureUnitUrl}"+"?ts="+new Date().getTime(),
			success: function(data){
				existingTempUnit = updatedTempUnit;
				$("#temperatureUnitStatus").html("Temperature unit updated successfully");
			},
			error: function(){
				$("#temperatureUnitStatus").html("Failed to update temperature unit");
			},
			dataType:"json",
			contentType : "application/xml; charset=utf-8"
		});
	
}

function editFloorPlanImageValue()
{
	$("#editFloorPlanImageSizeDialog").load("${editFloorPlanImageSizeDialogUrl}"+"?ts="+new Date().getTime()).dialog({
        title : "Edit Floor Plan Image Size",
        width :  Math.floor($('body').width() * .30),
        minHeight : 150,
        modal : true
    });
}

function editTTLValue()
{
	$("#editTTLValueDialog").load("${editTTLValueDialogUrl}"+"?ts="+new Date().getTime()).dialog({
        title : "Edit TTL Value",
        width :  Math.floor($('body').width() * .30),
        minHeight : 150,
        modal : true
    });
}

function chkPasswordExpiryFlag()
{
	var passwordExpiryCheckbox = document.getElementById("passwordExp");
	var updatedPasswordExpFlag = "";
	if(passwordExpiryCheckbox.checked == true)
	{
		updatedPasswordExpFlag = "true";		
	}
	else
	{ 
		if(passwordExpiryCheckbox.checked == false)
		{
			updatedPasswordExpFlag = "false";			
		}
	}	
				
	if(existingPasswordExpFlag.toUpperCase() != updatedPasswordExpFlag.toUpperCase()){		
		updatePasswordExpiryFlag(updatedPasswordExpFlag);
	} else {
		var elemError = document.getElementById("passwordExpiryStatus");
		elemError.style.color = "Green";
		if(updatedPasswordExpFlag == "false"){
			elemError.innerHTML = "Passsword Expiry Flag is already disabled";
		} else if(updatedPasswordExpFlag == "true"){
			elemError.innerHTML = "Passsword Expiry Flag is already enabled";
		}
	}
}

function onCheckboxSelection(){
	$("#passwordExpiryStatus").html("");
}

var sysConfigXML = "";

function updatePasswordExpiryFlag(updatedPasswordExpFlag){	
		
	sysConfigXML = "<systemConfiguration>"+
	"<id>"+passExpId+"</id>"+
	"<name>"+"em.forcepasswordexpiry"+"</name>"+
	"<value>"+updatedPasswordExpFlag+"</value>"+
	"</systemConfiguration>";
	
	$.ajax({
			data: sysConfigXML,
			type: "POST",
			url: "${editPasswordExpiryFlagUrl}"+"?ts="+new Date().getTime(),
			success: function(data){
				existingPasswordExpFlag = updatedPasswordExpFlag;
				var elem = document.getElementById("passwordExpiryStatus");
				elem.innerHTML = "Password Expiry Flag updated successfully";
				elem.style.color = "Green";
			},
			error: function(){
				var elemError = document.getElementById("passwordExpiryStatus");
				elemError.innerHTML = "Failed to update Password Expiry Flag";
				elemError.style.color = "Red";
			},
			dataType:"json",
			contentType : "application/xml; charset=utf-8"
		});
	
}
function onSoftMeteringCheckboxSelection()
{
	$("#softMeteringStatus").html("");
}
function setSoftMeteringFlag()
{
	var softMeteringCheckbox = document.getElementById("softMeteringFlag");
	var updatedSoftMetering = "";
	if(softMeteringCheckbox.checked == true)
	{
		updatedSoftMetering = "true";		
	}
	else
	{ 
		if(softMeteringCheckbox.checked == false)
		{
			updatedSoftMetering = "false";			
		}
	}	
	if(existingSoftMetering != updatedSoftMetering){		
		updateSoftMeteringFlag(updatedSoftMetering);
	} else {
		var elemError = document.getElementById("softMeteringStatus");
		elemError.style.color = "Green";
		if(updatedSoftMetering == "false"){
			elemError.innerHTML = "Soft Metering Flag is already disabled";
		} else if(updatedSoftMetering == "true"){
			elemError.innerHTML = "Soft Metering Flag is already enabled";
		}
	}
}
var sysConfigSoftMeteringXML = "";
function updateSoftMeteringFlag(updatedSoftMetering){	
	sysConfigSoftMeteringXML = "<systemConfiguration>"+
	"<id>"+softMeteringId+"</id>"+
	"<name>"+"enable.softmetering"+"</name>"+
	"<value>"+updatedSoftMetering+"</value>"+
	"</systemConfiguration>";
	
	$.ajax({
			data: sysConfigSoftMeteringXML,
			type: "POST",
			url: "${editSoftMeteringFlagUrl}"+"?ts="+new Date().getTime(),
			success: function(data){
				existingSoftMetering = updatedSoftMetering;
				var elem = document.getElementById("softMeteringStatus");
				elem.innerHTML = "Soft Metering Flag updated successfully";
				elem.style.color = "Green";
			},
			error: function(){
				var elemError = document.getElementById("softMeteringStatus");
				elemError.innerHTML = "Failed to update Soft Metering Flag";
				elemError.style.color = "Red";
			},
			dataType:"json",
			contentType : "application/xml; charset=utf-8"
		});
	
}
</script>
<div class="outermostdiv">
	
	<div id="editSystemConfigDialog"></div>
	
	<div id="editFloorPlanImageSizeDialog"></div>
	<div id="editTTLValueDialog"></div>
	
	<div class="outerContainer">
		<span><spring:message code="header.system.cleanup" /></span>
		<div class="i1"></div>
	</div>

	<div class="upperdiv"
		style="height: 35px; margin: 10px; padding: 10px;">
		<spring:url value="/settings/cleancache.ems" var="cleancache"
			scope="request" />
		<form action="${cleancache}" onsubmit="javascript:return cleanCache()">
			<input type="submit"
				value='<spring:message	code="system.cleanup.label.cleancache" />' />
		</form>
	</div>
	<div class="outerContainer">
		<span><spring:message code="header.system.masterGemsSetting" /></span>
		<div class="i1"></div>
	</div>

	<div class="upperdiv"
		style="height: 35px; margin: 10px; padding: 10px;">
		<spring:url value="/settings/master_gems_setting.ems"
			var="masterGemsSetting"  />
		<form action="${masterGemsSetting}">
			<input type="submit" value='<spring:message	code="system.cleanup.label.masterGemsSetting" />' />
		</form>
	</div>
	
	<div class="outerContainer">
	  <span>Discovered Devices Cleanup</span>
	  <div class="i1"></div>
	</div>

	<div class="upperdiv"
	  style="height: 35px; margin: 10px; padding: 10px;">
	 
	   <input type="submit" id="deleteAllBtn" value='<spring:message  code="system.label.delete.all" />'  onclick="deleteAllDiscoveredFixture()"/> 
	   <label id="deletefixturecount" style="padding-left: 10px;" ></label>
	
	</div>
	
	<div class="outerContainer">
	  <span>Placed Devices Cleanup</span>
	  <div class="i1"></div>
	</div>

	<div class="upperdiv"
	  style="height: 35px; margin: 10px; padding: 10px;">
	 
	   <input type="submit" id="deleteAllPlacedBtn" value='<spring:message  code="system.label.delete.placed.all" />'  onclick="deleteAllPlacedFixture()"/> 
	   <label id="deleteplacedfixturecount" style="padding-left: 10px;" ></label>
	
	</div>
	
	<security:authorize access="hasAnyRole('Admin')">
	<div class="outerContainer">
	  <span>System Configuration Values</span>
	  <div class="i1"></div>
	</div>

	<div class="upperdiv"
	  style="height: 35px; margin: 10px; padding: 10px;">
	 
	   <input type="submit" id="editSystemConfigurationBtn" value='Edit System Configuration Value'  onclick="editSystemConfigurationValue()"/> 
	   
	</div>
	</security:authorize>
	
	<div class="outerContainer">
	  <span>Temperature Unit</span>
	  <div class="i1"></div>
	</div>

	<div class="upperdiv"
	  style="height: 35px; margin: 10px; padding: 10px;">
	  
	   <input type="radio" name="temperature" id="fahrenheit" value="F" style="margin-left: 10px;" onclick="onRadioSelection()"/><span>Fahrenheit (F)</span>
	   <input type="radio" name="temperature" id="celcius" value="�C" style="margin-left: 10px;" onclick="onRadioSelection()"/><span>Celsius (�C)</span>	 
	   <input type="submit" id="updateTemperatureUnitBtn" value='Apply' style="margin-left: 20px;" onclick="chkTemperatureUnitSelection()"/>
	   <label id="temperatureUnitStatus" style="padding-left: 10px;"></label> 
	   
	</div>
	
	<div class="outerContainer">
	  <span>Floor Plan Maximum Allowed Image Size</span>
	  <div class="i1"></div>
	</div>

	<div class="upperdiv"
	  style="height: 35px; margin: 10px; padding: 10px;">
	 
	   <input type="submit" id="editFloorPlanImageSizeBtn" value='Edit Floor Plan Maximum Allowed Image Size'  onclick="editFloorPlanImageValue()"/> 
	   
	</div>
	
	<div class="outerContainer">
	  <span>Configure TTL Value</span>
	  <div class="i1"></div>
	</div>
	
	<div class="upperdiv"
	  style="height: 35px; margin: 10px; padding: 10px;">
	 
	   <input type="submit" id="editTTLBtn" value='Edit TTL'  onclick="editTTLValue()"/> 
	   
	</div>
	
	<div class="outerContainer">
	  <span>Password Expiry Flag</span>
	  <div class="i1"></div>
	</div>

	<div class="upperdiv"
	  style="height: 35px; margin: 10px; padding: 10px;">
	   <label id="enableDisableLabel" style="padding-left: 10px;">Enable/Disable</label>
	   <input id="passwordExp" type="checkbox" name="passwordExp" style="margin-left: 10px;" onclick="onCheckboxSelection()" value="false">
	   <input type="submit" id="setPasswordExpFlagBtn" value='Apply' style="margin-left: 20px;" onclick="chkPasswordExpiryFlag()"/>
	   <label id="passwordExpiryStatus" style="padding-left: 10px;"></label>
	</div>
	
	<div class="outerContainer">
	  <span>Soft Metering Flag</span>
	  <div class="i1"></div>
	</div>

	<div class="upperdiv"
	  style="height: 35px; margin: 10px; padding: 10px;">
	   <label id="enableDisableSoftMeteringLabel" style="padding-left: 10px;">Enable/Disable</label>
	   <input id="softMeteringFlag" type="checkbox" name="softMeteringFlag" style="margin-left: 10px;" onclick="onSoftMeteringCheckboxSelection()" value="false">
	   <input type="submit" id="setSoftMeteringFlagBtn" value='Apply' style="margin-left: 20px;" onclick="setSoftMeteringFlag()"/>
	   <label id="softMeteringStatus" style="padding-left: 10px;"></label>
	</div>
	
</div>