<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<spring:url value="/services/org/ballastservice/add" var="addBallastUrl" scope="request" />
<spring:url value="/services/org/ballastservice/edit" var="editBallastUrl" scope="request" />
<spring:url value="/services/org/ballastservice/details" var="checkForDuplicateBallastUrl" scope="request" />
<spring:url value="/services/org/ballastservice/list" var="loadAllBallasts" scope="request" />
<spring:url value="/services/org/ballastservice/getfixtureCount/" var="checkForBallastFixtureCountUrl" scope="request" />
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
var ballastFormErrorFlag = false;

//Global variable for accessing current ballast id
var BALLAST_ID = "${ballast.id}";

$(document).ready(function() {
	clearBallastLabelMessage();
	
	//Load no of bulbs
	<c:forEach items="${bulbcount}" var="buCount">
		$('#ballastLampNum').append($('<option></option>').val("${buCount.bulbCount}").html("${buCount.bulbCount}"));
	</c:forEach>
	
	$("#itemNum").val($.trim("${ballast.itemNum}"));
	$("#ballastName").val($.trim("${ballast.ballastName}"));
	$("#ballastInputVoltage").val($.trim("${ballast.inputVoltage}"));
	$("#ballastLampType").val($.trim("${ballast.lampType}"));
	$("#ballastLampNum").val($.trim("${ballast.lampNum}"));
	$("#ballastFactor").val($.trim("${ballast.ballastFactor}"));
	$("#ballastManufacturer").val($.trim("${ballast.ballastManufacturer}"));
	$("#ballastWattage").val($.trim("${ballast.wattage}"));
	$("#ballastBaselineLoad").val($.trim("${ballast.baselineLoad}"));
	$("#ballastDisplayLabel").val($.trim("${ballast.displayLabel}"));
	
});


function displayBallastLabelMessage(Message, Color) {	
	if("${from}" == 'fixtureclass')
	{
		$("#error").html(Message);
		$("#error").css("color", Color);
		ballastFormErrorFlag = true;
	}
	else
	{
		$("#ballastformerror").html(Message);
		$("#ballastformerror").css("color", Color);
		ballastFormErrorFlag = false;
	}
}
function clearBallastLabelMessage() {
	if(ballastFormErrorFlag==true)
	{
		 $("#error").html("");
		 $("#error").css("color", COLOR_DEFAULT);
		 ballastFormErrorFlag= false;
	}
	else
	{
	 $("#ballastformerror").html("");
	 $("#ballastformerror").css("color", COLOR_DEFAULT);
	 ballastFormErrorFlag = false;
	}
}
function showAddedBallast(ballastid)
{
	$.ajax({
		  type: 'GET',
		  url: "${loadAllBallasts}"+"?ts="+new Date().getTime(),
		  data: "",
		  success: function(data){
		   if(data!=null){
		    var ballastData = data.ballast;
		    if(ballastData != undefined){
		     $("#fixtureClassBallasts").empty();		     
		     if(ballastData.length == undefined){
		      $('#fixtureClassBallasts').append($('<option></option>').val(ballastData.id).html(ballastData.displayLabel));
		      if(ballastid != null) $("#fixtureClassBallasts").val(ballastid);
		     } else if(ballastData.length > 0){
		      $.each(ballastData, function(i, ballast) {
		       $('#fixtureClassBallasts').append($('<option></option>').val(ballast.id).html(ballast.displayLabel));
		       if(ballastid != null) $("#fixtureClassBallasts").val(ballastid);
		      });
		     }
		    }
		   }
		  },
		  dataType:"json",
		  contentType: "application/json; charset=utf-8"
		 });
	}

function closeBallastDialog(ballastCloseMode){
	if("${from}" == 'fixtureclass')
	{
		  $('#ballastFormShow').slideUp('slow', function() {
		    // Animation complete.
		  });
		  clearBallastLabelMessage();		  
		  if(ballastCloseMode=='cancelmode')
		  {
			  fillBallastCombo();
		  }  
		
		  $("#fixtureClassBallasts").removeAttr("disabled");
	}
	else
	{
	refreshBallastListFrame();
	$("#newBallastDialog").dialog("close");
	}
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
	clearBallastLabelMessage();	
	if ( $.trim($("#ballastName").val()) == ''){
		displayBallastLabelMessage("Ballast Name Field should not be empty", COLOR_FAILURE);
		return false;
	}
	if ( $.trim($('#ballastInputVoltage').val()) == ''){
		displayBallastLabelMessage("Input Voltage Field should not be empty", COLOR_FAILURE);
		return false;
	}
	if ( $.trim($('#ballastLampType').val()) == ''){
		displayBallastLabelMessage("Lamp Type Field should not be empty", COLOR_FAILURE);
		return false;
	}
	if ( $.trim($('#ballastLampNum').val()) == ''){
		displayBallastLabelMessage("Lamp Number Field should not be empty", COLOR_FAILURE);
		return false;
	}else{
		if(!IsNumericValue($.trim($('#ballastLampNum').val()))){
			displayBallastLabelMessage("Lamp Number Field should be a Number", COLOR_FAILURE);
			return false;
		}
	}
	if ( $.trim($('#ballastFactor').val()) == ''){
		displayBallastLabelMessage("Ballast Factor Field should not be empty", COLOR_FAILURE);
		return false;
	}else{
		if(!IsDoubleValue($.trim($('#ballastFactor').val()))){
			displayBallastLabelMessage("Ballast Factor should be a Number", COLOR_FAILURE);
			return false;
		}
	}
	
	if ( $.trim($('#ballastWattage').val()) == ''){
		displayBallastLabelMessage("Wattage Field should not be empty", COLOR_FAILURE);
		return false;
	}else{
		if(!IsNumericValue($.trim($('#ballastWattage').val()))){
			displayBallastLabelMessage("Wattage Field should be a Number", COLOR_FAILURE);
			return false;
		}
	}
	if ( $.trim($('#ballastManufacturer').val()) == ''){
		displayBallastLabelMessage("Ballast Manufacturer Field should not be empty", COLOR_FAILURE);
		return false;
	}
	if ( $.trim($('#ballastBaselineLoad').val()) != ''){
		if(!IsDoubleValue($.trim($('#ballastBaselineLoad').val()))){
			displayBallastLabelMessage("Baseline Load should be a Number", COLOR_FAILURE);
			return false;
		}
		baselineLoadValue = $.trim($('#ballastBaselineLoad').val());
	}else{
		baselineLoadValue = "";
	}
	
	if ( $.trim($('#itemNum').val()) != ''){
		if(!IsNumericValue($.trim($('#itemNum').val()))){
			displayBallastLabelMessage("Item Number should be a Number", COLOR_FAILURE);
			return false;
		}
		itemNumValue = $.trim($('#itemNum').val());
	}else{
		itemNumValue = "";
	}
	
	if($.trim($('#ballastDisplayLabel').val()) != ''){
		displayLabelValue = $.trim($('#ballastDisplayLabel').val());
	}else{
		displayLabelValue = "";
	}
	
	if("${mode}" == 'edit'){
		checkForBallastFixtureCount();
	}else{
		checkForDuplicateBallast();
	}
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

function checkForBallastFixtureCount(){
	
	$.ajax({
		type: 'POST',
		url: "${checkForBallastFixtureCountUrl}"+"${ballast.id}"+"?ts="+new Date().getTime(),
		data: "",
		success: function(data){
			if(data.status == 0){
				checkForDuplicateBallast();					
			}else{
				var proceed = confirm("One or more Fixtures are associated with this Ballast.Do you want to continue?");
				if(proceed == true){
					checkForDuplicateBallast();	
				}
			}
		},
		error: function(){
			displayLabelMessage("Error Occured.", COLOR_FAILURE);
		},
		dataType:"json",
		contentType: "application/json; charset=utf-8"
	});
}

function checkForDuplicateBallast(){
	
	if("${mode}" == 'edit'){
		
		ballastXML	=   "<ballast>"+
		"<id>"+"${ballast.id}"+"</id>"+
		"<itemNum>"+itemNumValue+"</itemNum>"+
		"<name>"+$.trim($('#ballastName').val())+"</name>"+
		"<inputVoltage>"+$.trim($('#ballastInputVoltage').val())+"</inputVoltage>"+
		"<bulbType>"+$.trim($('#ballastLampType').val())+"</bulbType>"+
		"<noOfBulbs>"+$.trim($('#ballastLampNum').val())+"</noOfBulbs>"+
		"<ballastFactor>"+$.trim($('#ballastFactor').val())+"</ballastFactor>"+
		"<voltpowermapid>"+$.trim("${ballast.voltPowerMapId}")+"</voltpowermapid>"+
		"<bulbWattage>"+$.trim($('#ballastWattage').val())+"</bulbWattage>"+
		"<ballastManufacturer>"+$.trim($('#ballastManufacturer').val())+"</ballastManufacturer>"+
		"<displayLabel>"+displayLabelValue+"</displayLabel>"+
		"<baselineLoad>"+baselineLoadValue+"</baselineLoad>"+
		"<isDefault>"+$.trim("${ballast.isDefault}")+"</isDefault>"+
		"</ballast>";
		
	}else{
		
		ballastXML =    "<ballast>"+
		"<itemNum>"+itemNumValue+"</itemNum>"+
		"<name>"+$.trim($('#ballastName').val())+"</name>"+
		"<inputVoltage>"+$.trim($('#ballastInputVoltage').val())+"</inputVoltage>"+
		"<bulbType>"+$.trim($('#ballastLampType').val())+"</bulbType>"+
		"<noOfBulbs>"+$.trim($('#ballastLampNum').val())+"</noOfBulbs>"+
		"<ballastFactor>"+$.trim($('#ballastFactor').val())+"</ballastFactor>"+
		"<bulbWattage>"+$.trim($('#ballastWattage').val())+"</bulbWattage>"+
		"<ballastManufacturer>"+$.trim($('#ballastManufacturer').val())+"</ballastManufacturer>"+
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
					displayBallastLabelMessage("Ballast with the display name already exists", COLOR_FAILURE);
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
				if("${from}" == 'fixtureclass')
				{						
					  showAddedBallast(data.msg);					  
				}				
				closeBallastDialog('addmode');
			},
			error: function(){
				displayBallastLabelMessage("Error.Ballast is not added", COLOR_FAILURE);
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
				closeBallastDialog('editmode');
			},
			error: function(){
				displayBallastLabelMessage("Error.Ballast is not Saved", COLOR_FAILURE);
			},
			dataType:"json",
			contentType : "application/xml; charset=utf-8"
		});
	
}

</script>
<div style="clear: both;"><span id="ballastformerror"></span></div>
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
		<td ><input id="ballastInputVoltage" name="ballastInputVoltage"/>
		</td>
	</tr>
	<tr>
		<th >Lamp Type:</th>
		<td ><input id="ballastLampType" name="ballastLampType"/>
		</td>
	</tr>
	<tr>
		<th >No Of Lamps:</th>
		<td ><select id="ballastLampNum" name="ballastLampNum"></select>
		</td>
	</tr>
	
	<tr>
		<th >Ballast Factor:</th>
		<td ><input id="ballastFactor" name="ballastFactor" />
		</td>
	</tr>
	
	<tr>
		<th >Wattage:</th>
		<td ><input id="ballastWattage" name="ballastWattage"/>
		</td>
	</tr>
	<tr>
		<th >Ballast Manufacturer:</th>
		<td ><input id="ballastManufacturer" name="ballastManufacturer" />
		</td>
	</tr>
	<tr>
		<th>Baseline Load:</th>
		<td ><input id="ballastBaselineLoad" name="ballastBaselineLoad"/>
		</td>
	</tr>
	<tr>
		<th>Display Label:</th>
		<td ><input id="ballastDisplayLabel" name="ballastDisplayLabel"/>
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
				value="<spring:message code="action.cancel" />" onclick="closeBallastDialog('cancelmode')">
		</td>
	</tr>
</table>
</div>