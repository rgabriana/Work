<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<spring:url value="/services/org/fixture/op/setAmbientThreshold" var="setAmbientThresholdUrl" scope="request" />

<style>
.upperdivDialog{
	background: repeat-x #d9dad9;
	border-top-left-radius: 15px;
	border-top-right-radius: 15px;
	border-bottom-left-radius: 15px;
	border-bottom-right-radius: 15px;
	height:100%;
	border:1px solid #cccccc;
	padding-top:5px;
	padding-bottom:10px;
	border-bottom:2px solid #cccccc;
	overflow:auto;
}
</style>

<form id="ambient-form" onsubmit="return false;" style="padding:0.5em;">
		<p>You are about to set the daylight harvesting target light level on the selected sensor(s).
		Set the value(s) manually or select sensor's current ambient value.</p>
		<br style="clear: both">
		<div class="upperdivDialog">
			<div class="fieldWrapper">
				<div class="fieldLabel">Selected Sensors:</div>
				<div class="fieldValue" id="selecetedSensors"></div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel">Select Action:</div>
				<div class="fieldValue">
					<input type="radio" name="sensoract" id="act_manual" checked="checked">Manual
					<input type="radio" name="sensoract" id="act_auto" style="margin-left: 1em;">Auto</div>
			</div>
			<div class="fieldWrapper" id="thresholdTxt">
				<div class="fieldLabel">Threshold Value:</div>
				<div class="fieldValue"><input type="text" name="thresholdVal" id="thresholdVal" class="text"></div>
			</div>
		</div>
		<br style="clear: both">
		<div style="text-align:center;">
			<button id="submitButton">Submit</button>
			<button id="cancelButton">Cancel</button>
		</div>	
	</form>

<script type="text/javascript">

var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";

var selFixtureNames;
$(document).ready(function() {
	selFixtureNames = "${selFixtureNames}";
	
	if(selectedFixtureIds){
		$("#selecetedSensors").empty();
		$("#selecetedSensors").append(selFixtureNames);
		
		$("#act_manual").attr("checked",true);
		$("#thresholdTxt").show();
		$("#thresholdVal").val("");
	}
});

$("input[name='sensoract']").change(function() {
	var selAct = $("input[name='sensoract']:checked").attr("id");
	if(selAct=="act_auto"){
		$("#thresholdVal").val("");
		$("#thresholdTxt").hide();
	}else{
		$("#thresholdTxt").show();
	}	
});
$("#submitButton").click(function(){
	if(!selectedFixtureIds){
		return;
	}
	var val = parseInt($("#thresholdVal").val());
	var isAuto = false;
	var selAct = $("input[name='sensoract']:checked").attr("id");
	if(selAct=="act_auto"){
		isAuto = true;
		val=0;
	}
	if(!isAuto&&((isNaN(val)||val<0))){		
		alert("Please enter valid numeric value.");
		return;
	}	
	if(val>32000){
		alert("Day light harvesting target value should not more than 32000");
		return;
	}	
	$.ajax({
		url:"${setAmbientThresholdUrl}/"+isAuto+"/"+val,
		contentType: "application/xml; charset=utf-8",
		type:"POST",
		dataType:"json",
		data:selectedFixtureIds,		
		success:function(response,status,jqhr){
			alert("The request for setting Daylight Target Value is submitted.");
			$("#ambientThreshloldDialog").dialog("close");
		},
		error:function(data){
			alert("Error: "+data);
		}
	});	
});
$("#cancelButton").click(function(){
	$("#ambientThreshloldDialog").dialog("close");
});

</script>

