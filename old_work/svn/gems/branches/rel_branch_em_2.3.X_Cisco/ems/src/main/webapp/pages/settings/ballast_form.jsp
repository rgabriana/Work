<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<spring:url value="/services/org/ballastservice/add" var="addBallastUrl" scope="request" />
<spring:url value="/services/org/ballastservice/edit" var="editBallastUrl" scope="request" />
<spring:url value="/services/org/ballastservice/details" var="checkForDuplicateBallastUrl" scope="request" />
<style>

#addBallastTable table {
	border: thin dotted #7e7e7e;
	padding: 10px;
}

#addBallastTable th {
	text-align: right;
	vertical-align: top;
	padding-right: 10px;
}

#addBallastTable td {
	vertical-align: top;
	padding-top: 2px;
}

#center {
  height : 95% !important;
}

</style>


<script type="text/javascript">

var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";

//Global variable for accessing current ballast id
var BALLAST_ID = "${ballast.id}";

$(document).ready(function() {
	clearLabelMessage() ;
	
	//Load no of bulbs
	<c:forEach items="${bulbcount}" var="buCount">
		$('#lampNum').append($('<option></option>').val("${buCount.bulbCount}").html("${buCount.bulbCount}"));
	</c:forEach>
	
	$("#itemNum").val($.trim("${ballast.itemNum}"));
	$("#ballastName").val($.trim("${ballast.ballastName}"));
	$("#inputVoltage").val($.trim("${ballast.inputVoltage}"));
	$("#lampType").val($.trim("${ballast.lampType}"));
	$("#lampNum").val($.trim("${ballast.lampNum}"));
	$("#ballastFactor").val($.trim("${ballast.ballastFactor}"));
	$("#ballastManufacturer").val($.trim("${ballast.ballastManufacturer}"));
	$("#wattage").val($.trim("${ballast.wattage}"));
	$("#baselineLoad").val($.trim("${ballast.baselineLoad}"));
	$("#displayLabel").val($.trim("${ballast.displayLabel}"));
	
});


function displayLabelMessage(Message, Color) {
		$("#error").html(Message);
		$("#error").css("color", Color);
}
function clearLabelMessage() {
	 $("#error").html("");
	 $("#error").css("color", COLOR_DEFAULT);
}

function closeBallastDialog(){
	refreshBallastListFrame();
	$("#newBallastDialog").dialog("close");
}

function refreshBallastListFrame(){
	var ifr = parent.document.getElementById('ballastFrame');
	window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src + "?ts="+new Date().getTime();
}

var baselineLoadValue = "";

var itemNumValue = "";

var displayLabelValue = "";

var ballastXML = "";

function validateBallastForm(){
	clearLabelMessage();
	if ( $('#ballastName').val().trim() == ''){
		displayLabelMessage("Ballast Name Field should not be empty", COLOR_FAILURE);
		return false;
	}
	if ( $('#inputVoltage').val().trim() == ''){
		displayLabelMessage("Input Voltage Field should not be empty", COLOR_FAILURE);
		return false;
	}
	if ( $('#lampType').val().trim() == ''){
		displayLabelMessage("Lamp Type Field should not be empty", COLOR_FAILURE);
		return false;
	}
	if ( $('#lampNum').val().trim() == ''){
		displayLabelMessage("Lamp Number Field should not be empty", COLOR_FAILURE);
		return false;
	}else{
		if(!IsNumericValue($('#lampNum').val().trim())){
			displayLabelMessage("Lamp Number Field should be a Number", COLOR_FAILURE);
			return false;
		}
	}
	if ( $('#ballastFactor').val().trim() == ''){
		displayLabelMessage("Ballast Factor Field should not be empty", COLOR_FAILURE);
		return false;
	}else{
		if(!IsDoubleValue($('#ballastFactor').val().trim())){
			displayLabelMessage("Ballast Factor should be a Number", COLOR_FAILURE);
			return false;
		}
	}
	
	if ( $('#wattage').val().trim() == ''){
		displayLabelMessage("Wattage Field should not be empty", COLOR_FAILURE);
		return false;
	}else{
		if(!IsNumericValue($('#wattage').val().trim())){
			displayLabelMessage("Wattage Field should be a Number", COLOR_FAILURE);
			return false;
		}
	}
	if ( $('#ballastManufacturer').val().trim() == ''){
		displayLabelMessage("Ballast Manufacturer Field should not be empty", COLOR_FAILURE);
		return false;
	}
	if ( $('#baselineLoad').val().trim() != ''){
		if(!IsDoubleValue($('#baselineLoad').val().trim())){
			displayLabelMessage("Baseline Load should be a Number", COLOR_FAILURE);
			return false;
		}
		baselineLoadValue = $('#baselineLoad').val().trim();
	}else{
		baselineLoadValue = "";
	}
	
	if ( $('#itemNum').val().trim() != ''){
		if(!IsNumericValue($('#itemNum').val().trim())){
			displayLabelMessage("Item Number should be a Number", COLOR_FAILURE);
			return false;
		}
		itemNumValue = $('#itemNum').val().trim();
	}else{
		itemNumValue = "";
	}
	
	if($('#displayLabel').val().trim() != ''){
		displayLabelValue = $('#displayLabel').val().trim();
	}else{
		displayLabelValue = "";
	}
	
	checkForDuplicateBallast();
}

function IsNumericValue(sText)
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

function IsDoubleValue(sText)
{
	var ValidChars = "0123456789.";
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

var ballastXML = "";

function checkForDuplicateBallast(){
	
	if("${mode}" == 'edit'){
		
		ballastXML	=   "<ballast>"+
		"<id>"+"${ballast.id}"+"</id>"+
		"<itemNum>"+itemNumValue+"</itemNum>"+
		"<name>"+$('#ballastName').val().trim()+"</name>"+
		"<inputVoltage>"+$('#inputVoltage').val().trim()+"</inputVoltage>"+
		"<bulbType>"+$('#lampType').val().trim()+"</bulbType>"+
		"<noOfBulbs>"+$('#lampNum').val().trim()+"</noOfBulbs>"+
		"<ballastFactor>"+$('#ballastFactor').val().trim()+"</ballastFactor>"+
		"<voltpowermapid>"+$.trim("${ballast.voltPowerMapId}")+"</voltpowermapid>"+
		"<bulbWattage>"+$('#wattage').val().trim()+"</bulbWattage>"+
		"<ballastManufacturer>"+$('#ballastManufacturer').val().trim()+"</ballastManufacturer>"+
		"<displayLabel>"+displayLabelValue+"</displayLabel>"+
		"<baselineLoad>"+baselineLoadValue+"</baselineLoad>"+
		"<isDefault>"+$.trim("${ballast.isDefault}")+"</isDefault>"+
		"</ballast>";
		
	}else{
		
		ballastXML =    "<ballast>"+
		"<itemNum>"+itemNumValue+"</itemNum>"+
		"<name>"+$('#ballastName').val().trim()+"</name>"+
		"<inputVoltage>"+$('#inputVoltage').val().trim()+"</inputVoltage>"+
		"<bulbType>"+$('#lampType').val().trim()+"</bulbType>"+
		"<noOfBulbs>"+$('#lampNum').val().trim()+"</noOfBulbs>"+
		"<ballastFactor>"+$('#ballastFactor').val().trim()+"</ballastFactor>"+
		"<bulbWattage>"+$('#wattage').val().trim()+"</bulbWattage>"+
		"<ballastManufacturer>"+$('#ballastManufacturer').val().trim()+"</ballastManufacturer>"+
		"<displayLabel>"+displayLabelValue+"</displayLabel>"+
		"<baselineLoad>"+baselineLoadValue+"</baselineLoad>"+
		"</ballast>";
		
	}
	
	$.ajax({
		type: 'POST',
		url: "${checkForDuplicateBallastUrl}"+"?ts="+new Date().getTime(),
		data: ballastXML,
		success: function(data){
			if(data == null){
				if("${mode}" == 'edit'){
					editBallast();
				}else{
					addBallast();
				}
					
			}else{
				if(BALLAST_ID != data.id){
					displayLabelMessage("Ballast with the display name already exists", COLOR_FAILURE);
				}else{
					editBallast();
				}
			}
		},
		dataType:"json",
		contentType: "application/xml; charset=utf-8"
	});
	
}

function addBallast(){

	$.ajax({
			data: ballastXML,
			type: "POST",
			url: "${addBallastUrl}"+"?ts="+new Date().getTime(),
			success: function(data){
				closeBallastDialog();
			},
			error: function(){
				displayLabelMessage("Error.Ballast is not added", COLOR_FAILURE);
			},
			dataType:"json",
			contentType : "application/xml; charset=utf-8"
		});
	
}

function editBallast(){
	
	$.ajax({
			data: ballastXML,
			type: "POST",
			url: "${editBallastUrl}"+"?ts="+new Date().getTime(),
			success: function(data){
				closeBallastDialog();
			},
			error: function(){
				displayLabelMessage("Error.Ballast is not Saved", COLOR_FAILURE);
			},
			dataType:"json",
			contentType : "application/xml; charset=utf-8"
		});
	
}

</script>
<div style="clear: both;"><span id="error"></span></div>
<div style="margin:10px 0px 0px 20px;">

<table id="addBallastTable">
	<tr>
		<th >Item Number:</th>
		<td ><input id="itemNum" name="itemNum" />
		</td>
	</tr>
	<tr>
		<th >Ballast Name:</th>
		<td ><input id="ballastName" name="ballastName"/>
		</td>
	</tr>
	<tr>
		<th >Input Voltage:</th>
		<td ><input id="inputVoltage" name="inputVoltage"/>
		</td>
	</tr>
	<tr>
		<th >Lamp Type:</th>
		<td ><input id="lampType" name="lampType"/>
		</td>
	</tr>
	<tr>
		<th >Lamp Number:</th>
		<td ><select id="lampNum" name="lampNum"></select>
		</td>
	</tr>
	
	<tr>
		<th >Ballast Factor:</th>
		<td ><input id="ballastFactor" name="ballastFactor" />
		</td>
	</tr>
	
	<tr>
		<th >Wattage:</th>
		<td ><input id="wattage" name="wattage"/>
		</td>
	</tr>
	<tr>
		<th >Ballast Manufacturer:</th>
		<td ><input id="ballastManufacturer" name="ballastManufacturer" />
		</td>
	</tr>
	<tr>
		<th>Baseline Load:</th>
		<td ><input id="baselineLoad" name="baselineLoad"/>
		</td>
	</tr>
	<tr>
		<th>Display Label:</th>
		<td ><input id="displayLabel" name="displayLabel"/>
		</td>
	</tr>
	<tr>
		<th><span></span></th>
		<td>
			<c:if test="${mode == 'add'}">
			<button type="button" onclick="validateBallastForm();">
				Add
			</button>&nbsp;
			</c:if>
			<c:if test="${mode == 'edit'}">
			<button type="button" onclick="validateBallastForm();">
				Save
			</button>&nbsp;
			</c:if>
			<input type="button" id="btnClose"
				value="<spring:message code="action.cancel" />" onclick="closeBallastDialog()">
		</td>
	</tr>
</table>
</div>