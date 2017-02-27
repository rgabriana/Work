<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<spring:url value="/services/org/fixture/delete/all/discovered" var="deletealldicoveredfixture" scope="request" />
<spring:url value="/services/org/fixture/delete/all/placed" var="deleteallplacedfixture" scope="request" />
<spring:url value="/services/systemconfig/details" var="getExistingTemperatureUnitUrl" scope="request" />
<spring:url value="/services/systemconfig/edit" var="editTemperatureUnitUrl" scope="request" />

<spring:url value="/settings/editsystemconfig.ems" var="editSystemConfigDialogUrl" scope="request" />
<spring:url value="/settings/editfloorplanconfig.ems" var="editFloorPlanImageSizeDialogUrl" scope="request" />


<script type="text/javascript">

var existingTempUnit;
var id;

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
});

function deleteAllDiscoveredFixture()
{
	if(confirm('Are you sure you want to delete all discovered but not commissioned fixtures?')){
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
                $("#deletefixturecount").html("Total Deleted Fixtures " + fixtureDeletedCount) ;
                }
            else
            	{
            	fixtureDeletedCount=0 ;
            	$("#deletefixturecount").html("Total Deleted Fixtures " + fixtureDeletedCount) ;
            	}
            }
        });	
	}
	
	if(fixtureDeletedCount==undefined)
	{
	fixtureDeletedCount=0 ;
	$("#deletefixturecount").html("Total Deleted Fixtures " + fixtureDeletedCount) ;
	}
}

function deleteAllPlacedFixture()
{
	if(confirm('Are you sure you want to delete all placed but not commissioned fixtures?')){
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
                $("#deleteplacedfixturecount").html("Total Deleted Placed Fixtures " + placedFixtureDeletedCount) ;
                }
            else
            	{
            	placedFixtureDeletedCount=0 ;
            	$("#deleteplacedfixturecount").html("Total Deleted Placed Fixtures " + placedFixtureDeletedCount) ;
            	}
            }
        });	
	}
	
	if(placedFixtureDeletedCount==undefined)
	{
		placedFixtureDeletedCount=0 ;
		$("#deleteplacedfixturecount").html("Total Deleted Placed Fixtures " + placedFixtureDeletedCount) ;
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
</script>
<div class="outermostdiv">
	
	<div id="editSystemConfigDialog"></div>
	
	<div id="editFloorPlanImageSizeDialog"></div>
	
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
	  <span>Discovered Fixtures Cleanup</span>
	  <div class="i1"></div>
	</div>

	<div class="upperdiv"
	  style="height: 35px; margin: 10px; padding: 10px;">
	 
	   <input type="submit" id="deleteAllBtn" value='<spring:message  code="system.label.delete.all" />'  onclick="deleteAllDiscoveredFixture()"/> 
	   <label id="deletefixturecount" style="padding-left: 10px;" ></label>
	
	</div>
	
	<div class="outerContainer">
	  <span>Placed Fixtures Cleanup</span>
	  <div class="i1"></div>
	</div>

	<div class="upperdiv"
	  style="height: 35px; margin: 10px; padding: 10px;">
	 
	   <input type="submit" id="deleteAllPlacedBtn" value='<spring:message  code="system.label.delete.placed.all" />'  onclick="deleteAllPlacedFixture()"/> 
	   <label id="deleteplacedfixturecount" style="padding-left: 10px;" ></label>
	
	</div>
	
	<div class="outerContainer">
	  <span>System Configuration Values</span>
	  <div class="i1"></div>
	</div>

	<div class="upperdiv"
	  style="height: 35px; margin: 10px; padding: 10px;">
	 
	   <input type="submit" id="editSystemConfigurationBtn" value='Edit System Configuration Value'  onclick="editSystemConfigurationValue()"/> 
	   
	</div>
	
	<div class="outerContainer">
	  <span>Temperature Unit</span>
	  <div class="i1"></div>
	</div>

	<div class="upperdiv"
	  style="height: 35px; margin: 10px; padding: 10px;">
	  
	   <input type="radio" name="temperature" id="fahrenheit" value="F" style="margin-left: 10px;" onclick="onRadioSelection()"/><span>Fahrenheit (F)</span>
	   <input type="radio" name="temperature" id="celcius" value="°C" style="margin-left: 10px;" onclick="onRadioSelection()"/><span>Celcius (°C)</span>	 
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
	
	
</div>