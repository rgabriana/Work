<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/hvac/op/status/device" var="hvacDeviceStatus" scope="request" />
<spring:url value="/services/org/hvac/op/device" var="hvacUrl" scope="request" />

<style>
#ambienttemperature
{
	border-style: none;
	padding-left: 5px;
}
#temperature
{
	border-style: none;
	font-size: 20px;
	font-weight: bolder;
	padding-left: 2px;
}

.ui-jqgrid tr.jqgrow td {
	border-bottom: 1px dotted #CCCCCC;
}

.enableButton{
	padding:0px 5px;
 	height:28px; color:#fff; 
 	background:url(../themes/default/images/blue1px.png) !important;
	border:1px solid #3399cc !important;
}

.disableButton
{
	padding:0px 5px;
	height:28px; color:#fff; 
    background: #CECECE !important;
	border:1px solid #CECECE !important;
	
}


</style>
<script type="text/javascript">
var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";

var ambientTemperature;
var temperatureVal;
var previousStatus;
var hvacId;

var hvacDeviceName;

var isOnInProcess = false;
var isOffInProcess = false;

var timerInt;
// 1000 - Read temperature
// 1001 - Read ambient temperature
// 2000 - SET temperature when clicked on +/-
</script>

<script type="text/javascript">
		
	function setHvacDeviceMessage(msg, color){
		$("#hvacdetails-message-div").css("color", color);
		$("#hvacdetails-message-div").html(msg);
	}
	function clearHvacDeviceMessage(Message, Color) {
		setHvacDeviceMessage("", COLOR_DEFAULT);
  	}
	
	function getModbusRunningStatus()
	{
		setHvacDeviceMessage("Loading modbus status...", COLOR_SUCCESS);
		$.ajax({
			url: "${hvacUrl}"+ "/"+ hvacId + "/function/1003/-1",
			type: "POST",
			dataType:"json",
			contentType: "application/json; charset=utf-8",
			success: function(data){
				loadTemperature();
				getFunctionStatus(hvacId,1003,-1);
			},
			error: function() {
				setHvacDeviceMessage("Error in retrieving modbus", COLOR_FAILURE);
			}
		});
	}
	function loadTemperature()
	{
		setHvacDeviceMessage("Loading Temperature...", COLOR_SUCCESS);
		$.ajax({
			url: "${hvacUrl}"+ "/"+ hvacId + "/function/1000/-1",
			type: "POST",
			dataType:"json",
			contentType: "application/json; charset=utf-8",
			success: function(data){
				if(timerInt!=null)
				{
					clearInterval(timerInt); 
				}
				getFunctionStatus(hvacId,1000,-1);
				loadAmbientTemperature();
			},
			error: function() {
				setHvacDeviceMessage("Error in Loading Temperature", COLOR_FAILURE);
			}
		});
	}
	function loadAmbientTemperature()
	{
		setHvacDeviceMessage("Loading Ambient Temperature...", COLOR_SUCCESS);
		$.ajax({
			url: "${hvacUrl}"+ "/"+ hvacId + "/function/1001/-1?ts="+new Date().getTime(),
			type: "POST",
			dataType:"json",
			contentType: "application/json; charset=utf-8",
			success: function(data){
				getFunctionStatus(hvacId,1001,-1);
			},
			error: function() {
				setHvacDeviceMessage("Error in Loading Ambient Temperature", COLOR_FAILURE);
			}
		});
	}
	function onHvac()
	{
		if(isOnInProcess==false)
		{
			isOnInProcess = true;
			setHvacDeviceMessage("Processing...", COLOR_DEFAULT);
			
			$.ajax({
				url: "${hvacUrl}" + "/" +hvacId+"/function/2002/9?ts="+new Date().getTime(),
				type: "POST",
				dataType:"json", 
				contentType: "application/json; charset=utf-8",
				success: function(data){
					getFunctionStatus(hvacId,2002,9);
					setHvacDeviceMessage("Device ON successfully", COLOR_SUCCESS);
				},
				error: function() {
					setHvacDeviceMessage("Error in Device ON", COLOR_FAILURE);
				},
				complete: function() {
					isOnInProcess=false;
				}
			});
		}
	}

	function offHvac()
	{
		if(isOffInProcess==false)
		{
			isOffInProcess = true;
			setHvacDeviceMessage("Processing...", COLOR_DEFAULT);
			
			$.ajax({
				url: "${hvacUrl}"+ "/" + hvacId+"/function/2002/1?ts="+new Date().getTime(),
				type: "POST",
				dataType:"json", 
				contentType: "application/json; charset=utf-8",
				success: function(data){
					getFunctionStatus(hvacId,2002,1);
					setHvacDeviceMessage("Device OFF successfully", COLOR_SUCCESS);
				},
				error: function() {
					setHvacDeviceMessage("Error in Device OFF", COLOR_SUCCESS);
				},
				complete: function() {
					isOffInProcess=false;
				}
			});
		}
		
	}

	function incrementTemperature()
	{
		temperatureVal =0;
		setHvacDeviceMessage("", COLOR_DEFAULT);
		$("#incrementButton").attr("disabled", true);
		
		temperatureVal = $("#temperature").val();
		if(temperatureVal!='NA')
		{
			if(temperatureVal>=16 && temperatureVal<=30)
			temperatureVal++;
		
			$.ajax({
				url: "${hvacUrl}"+ "/"+ hvacId + "/function/2000/"+temperatureVal+"?ts="+new Date().getTime(),
				type: "POST",
				dataType:"json", 
				contentType: "application/json; charset=utf-8",
				success: function(data){
					timerInt = setInterval( function() {
						loadTemperature();
						//getFunctionStatus(hvacId,1000,-1); // TODO: Need to revisit
						setHvacDeviceMessage("Loading status...", COLOR_SUCCESS);
					 }, 3000);
				},
				error: function() {
					setHvacDeviceMessage("Error in setting new temperature", COLOR_FAILURE);
				}
			});
		}
		
	}

	function decrementTemperature()
	{
		temperatureVal =0;
		setHvacDeviceMessage("", COLOR_DEFAULT);
		$("#decrementButton").attr("disabled", true);
		
		temperatureVal = $("#temperature").val();
		
		if(temperatureVal!='NA')
		{
			if(temperatureVal>=16 && temperatureVal<=30)
			temperatureVal--;
			
			$.ajax({
				url: "${hvacUrl}"+ "/"+ hvacId + "/function/2000/"+temperatureVal+"?ts="+new Date().getTime(),
				type: "POST",
				dataType:"json", 
				contentType: "application/json; charset=utf-8",
				success: function(data){
				timerInt = setInterval( function() {
					loadTemperature();
					//getFunctionStatus(hvacId, 1000,-1); // TODO: Need to revisit
					setHvacDeviceMessage("Loading status", COLOR_SUCCESS);
				 }, 3000);
				},
				error: function() {
					setHvacDeviceMessage("Error in setting new temperature", COLOR_FAILURE);
				}
			});
		}
		
	}

	function refreshHvacDetails()
	{
		setHvacDeviceMessage("Refreshing...", COLOR_SUCCESS);
		getModbusRunningStatus();
	}

	function getFunctionStatus(deviceId, functionId, args)
	{
		setHvacDeviceMessage("Loading Temperature...", COLOR_SUCCESS);
		$.ajax({
			url: "${hvacDeviceStatus}"+ "/" +deviceId+"/function/"+functionId+"/args/"+args+"?ts="+new Date().getTime(),
			type: "GET",
			dataType:"json", 
			contentType: "application/json; charset=utf-8",
			success: function(data){
				
				if(data.status>=0)
				{
					//SET RESPONSE HERE
					if(functionId==1000)
					{
						$("#temperature").val(data.result);
						temperatureVal = $("#temperature").val();
						setHvacDeviceMessage("Temperature updated", COLOR_SUCCESS);
					}
					else if(functionId==1001)
					{
						$("#ambienttemperature").val(data.result);
						setHvacDeviceMessage("Ambient Temperature updated", COLOR_SUCCESS);
					}else if(functionId==1003) //Update the button status
					{
						if(data.result>=9)
						{
							// ON CONDITION
							toggleON(false);
						}else
						{
							// OFF CONDITION
							toggleON(true);
						}
					}else if(functionId==2002 && data.args == 9) //ON
					{
						loadTemperature();
						toggleON(false);
					}
					else if(functionId==2002 && data.args == 1) // OFF
					{
						setHvacDeviceMessage("Device turned off", COLOR_FAILURE);
						$("#temperature").val('NA');
						$("#ambienttemperature").val('NA');
						toggleON(true);
					}
				}else
				{
					setHvacDeviceMessage(data.message, COLOR_FAILURE);
					if(functionId==1000)
					{
						$("#temperature").val('NA');
					}
					else if(functionId==1001)
					{
						$("#ambienttemperature").val('NA');
					}
				}
				
			},
			complete: function() {
			},
			error: function() {
				setHvacDeviceMessage("Error in updating.", COLOR_FAILURE);
			}
		});
	}
	
	function toggleON(flag)
	{
		if(flag)
		{
			//ENABLE ON
			$("#onHvacBtn").removeClass("disableButton");  
			$("#onHvacBtn").removeAttr("disabled");
			$("#onHvacBtn").addClass("enableButton");   
			
			//DISABLE OFF
			$("#offHvacBtn").removeClass("enableButton"); 
			$("#offHvacBtn").attr("disabled", true);
			$("#offHvacBtn").addClass("disableButton"); 
		}else
		{	
			//Disable ON
			$("#onHvacBtn").removeClass("enableButton"); 
			$("#onHvacBtn").attr("disabled", true);
			$("#onHvacBtn").addClass("disableButton"); 
			
			//ENABLE OFF
			$("#offHvacBtn").removeClass("disableButton"); 
			$("#offHvacBtn").removeAttr("disabled");
			$("#offHvacBtn").addClass("enableButton"); 
		}
	}
	$(document).ready(function(){
		hvacId =  "${hvacId}";
		$("#offHvacBtn").addClass("disableButton"); 
		$("#offHvacBtn").attr("disabled", true);
		$("#onHvacBtn").addClass("disableButton"); 
		$("#onHvacBtn").attr("disabled", true);
		setHvacDeviceMessage("Processing, please wait...", COLOR_SUCCESS);
		getModbusRunningStatus();
	});
	
</script>
<spring:url value="/themes/default/images/refresh.png" var="refreshIcon"/>
<spring:url value="/themes/default/images/plus.png" var="plus"/>
<spring:url value="/themes/default/images/minus.png" var="minus"/>
<div style="padding-left: 5px;padding-top: 5px;">
		<table id="hvac_details_table">
			<tr>
				<td colspan="3">
					<div id="hvacdetails-message-div">&nbsp;</div>
				</td>
				<td style="text-align: left;">
					<img id="refresh" style="width: 20px;height: 20px;"  src="${refreshIcon}" onclick="refreshHvacDetails();" />
				</td>
			</tr>
			<!-- 
			<tr>
				<td>Ambient Temperature:  </td>
				<td><input type="text"  id="ambienttemperature" readonly="readonly" name="lastName" size="5" /></td>
				<td />
				<td />
			</tr>
			 -->
			<tr style="height:5px;"></tr>
			<tr >
				<td>Temperature (&deg;C) :</td>
				<td><input type="text"   id="temperature" name="contact" size="2" readonly="readonly"/></td>
				<td style="text-align: right;">
					<img id="incrementButton" style="width: 20px;height: 20px;"  src="${minus}" onclick="decrementTemperature();" />
					<img id="decrementButton" style="width: 20px;height: 20px;"  src="${plus}" onclick="incrementTemperature();" />
				</td>
				<td />
			</tr>
			<tr style="height:5px;"></tr>
			<tr>
				<td />
				<td colspan="1">
				<div align="right">
				<input id="onHvacBtn" type="button" 
					value="ON" onclick="onHvac();">
					</div>
					
				</td>
				<td colspan="1">
				<div align="left">
				<input type="button" id="offHvacBtn"
					value="OFF" onclick="offHvac()">
					</div>
				</td>
				<td/>
			</tr>
		</table>
</div>