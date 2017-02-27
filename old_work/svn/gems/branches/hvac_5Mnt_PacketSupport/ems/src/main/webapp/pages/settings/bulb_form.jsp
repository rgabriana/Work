<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<spring:url value="/services/org/bulbservice/add" var="addBulbUrl" scope="request" />
<spring:url value="/services/org/bulbservice/edit" var="editBulbUrl" scope="request" />
<spring:url value="/services/org/bulbservice/details" var="checkForDuplicateBulbUrl" scope="request" />
<spring:url value="/services/org/bulbservice/list" var="loadAllBulbs" scope="request" />
<style>

#addBulbTable table {
	border: thin dotted #7e7e7e;
	padding: 10px;
}

#addBulbTable th {
	text-align: right;
	vertical-align: top;
	padding-right: 10px;
}

#addBulbTable td {
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

var BULB_ID = "${bulb.id}";
var bulbXML = "";
var bulbFormErrorFlag = false;

$(document).ready(function() {
	clearBulbLabelMessage() ;	
	$("#bulbname").val($.trim("${bulb.bulbName}"));
	$("#bulbmanufacturer").val($.trim("${bulb.manufacturer}"));
	$("#bulbtype").val($.trim("${bulb.type}"));
	$("#bulbinitiallumens").val($.trim("${bulb.initialLumens}"));
	$("#bulbdesignlumens").val($.trim("${bulb.designLumens}"));
	$("#bulbenergy").val($.trim("${bulb.energy}"));
	$("#bulblifeinsstart").val($.trim("${bulb.lifeInsStart}"));
	$("#bulblifeprogstart").val($.trim("${bulb.lifeProgStart}"));
	$("#bulbdiameter").val($.trim("${bulb.diameter}"));
	$("#bulblength").val($.trim("${bulb.length}"));
	$("#bulbcri").val($.trim("${bulb.cri}"));
	$("#bulbcolortemp").val($.trim("${bulb.colorTemp}"));
	
});

function displayBulbLabelMessage(Message, Color) {		
		if("${from}" == 'fixtureclass')
		{
			$("#error").html(Message);
			$("#error").css("color", Color);
			bulbFormErrorFlag = true;
		}
		else
		{
			$("#bulbformerror").html(Message);
			$("#bulbformerror").css("color", Color);
			bulbFormErrorFlag = false;
		}
}
function clearBulbLabelMessage() {
	if(bulbFormErrorFlag==true)
	{
		 $("#error").html("");
		 $("#error").css("color", COLOR_DEFAULT);
		 bulbFormErrorFlag= false;
	}
	else
	{
	 $("#bulbformerror").html("");
	 $("#bulbformerror").css("color", COLOR_DEFAULT);
	}
}

function IsBulbNumericValue(sText)
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

function IsBulbDoubleValue(sText)
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

function buildXML()
{	
	if ( $('#bulbname').val().trim() == ''){
		displayBulbLabelMessage("Bulb Name Field should not be empty", COLOR_FAILURE);
		return false;
	}
	if ( $('#bulbmanufacturer').val().trim() == ''){
		displayBulbLabelMessage("Manufacturer Field should not be empty", COLOR_FAILURE);
		return false;
	}
	if ( $('#bulbtype').val().trim() == ''){l
		displayBulbLabelMessage("Type Field should not be empty", COLOR_FAILURE);
		return false;
	}
	
	if ( !IsBulbNumericValue($('#bulbinitiallumens').val().trim())){
		displayBulbLabelMessage("Bulb Intial Lumens should be  a Number", COLOR_FAILURE);
		return false;
	}
	if ( !IsBulbNumericValue($('#bulbdesignlumens').val().trim())){
		displayBulbLabelMessage("Bulb Design Lumens should be Number", COLOR_FAILURE);
		return false;
	}
	if ( !IsBulbNumericValue($('#bulbenergy').val().trim())){
		displayBulbLabelMessage("Energy Name Field should be a Number", COLOR_FAILURE);
		return false;
	}
	if ( !IsBulbNumericValue($('#bulblifeinsstart').val().trim())){
		displayBulbLabelMessage("Life Ins Start Field should be a Number", COLOR_FAILURE);
		return false;
	}
	if ( !IsBulbNumericValue($('#bulblifeprogstart').val().trim())){
		displayBulbLabelMessage("Lifeprogstart Field should be a Number", COLOR_FAILURE);
		return false;
	}
	if ( !IsBulbNumericValue($('#bulbdiameter').val().trim())){
		displayBulbLabelMessage("Bulb Diameter Field should be a Number", COLOR_FAILURE);
		return false;
	}
	if ( !IsBulbDoubleValue($('#bulblength').val().trim())){
		displayBulbLabelMessage("Bulb Length Field should be a Number", COLOR_FAILURE);
		return false;
	}
	if ( !IsBulbNumericValue($('#bulbcri').val().trim())){
		displayBulbLabelMessage("Cri Field should be a Number", COLOR_FAILURE);
		return false;
	}
	if ( !IsBulbNumericValue($('#bulbcolortemp').val().trim())){
		displayBulbLabelMessage("Color temperature Field should be a Number", COLOR_FAILURE);
		return false;
	} 
	if(!IsBulbNumericValue($('#bulbinitiallumens').val().trim())){
		displayBulbLabelMessage("Lamp Initial Lumens should be a Number", COLOR_FAILURE);
		return false;
	}
	
	var bulbEnergyVar,bulbDiameterVar,bulbCriVar,bulbColorTempVar;
	if($('#bulbenergy').val().trim()=='')
	{
		bulbEnergyVar = null;
	}
	else
	{
		bulbEnergyVar = $('#bulbenergy').val().trim();
	}
	if($('#bulbdiameter').val().trim()=='')
	{
		bulbDiameterVar = null;
	}
	else
	{
		bulbDiameterVar = $('#bulbdiameter').val().trim();
	}
	if($('#bulbcri').val().trim()=='')
	{
		bulbCriVar = null;
	}
	else
	{
		bulbCriVar = $('#bulbcri').val().trim();
	}
	if($('#bulbcolortemp').val().trim()=='')
	{
		bulbColorTempVar = null;
	}
	else
	{
		bulbColorTempVar = $('#bulbcolortemp').val().trim();
	}
	
if("${mode}" == 'edit'){
		bulbXML	=   "<bulb>"+
		"<id>"+"${bulb.id}"+"</id>"+
		"<name>"+$('#bulbname').val().trim()+"</name>"+
		"<manufacturer>"+$('#bulbmanufacturer').val().trim()+"</manufacturer>"+
		"<type>"+$('#bulbtype').val().trim()+"</type>"+
		"<initiallumens>"+$('#bulbinitiallumens').val().trim()+"</initiallumens>"+
		"<designlumens>"+$('#bulbdesignlumens').val().trim()+"</designlumens>"+
		"<energy>"+bulbEnergyVar+"</energy>"+
		"<lifeinsstart>"+$('#bulblifeinsstart').val().trim()+"</lifeinsstart>"+
		"<lifeprogstart>"+$('#bulblifeprogstart').val().trim()+"</lifeprogstart>"+
		"<diameter>"+bulbDiameterVar+"</diameter>"+
		"<length>"+$('#bulblength').val().trim()+"</length>"+
		"<cri>"+bulbCriVar+"</cri>"+
		"<colortemp>"+bulbColorTempVar+"</colortemp>"+
		"</bulb>";		
		}
else
		{		
		bulbXML	=   "<bulb>"+		
		"<name>"+$('#bulbname').val().trim()+"</name>"+
		"<manufacturer>"+$('#bulbmanufacturer').val().trim()+"</manufacturer>"+
		"<type>"+$('#bulbtype').val().trim()+"</type>"+
		"<initiallumens>"+$('#bulbinitiallumens').val().trim()+"</initiallumens>"+
		"<designlumens>"+$('#bulbdesignlumens').val().trim()+"</designlumens>"+
		"<energy>"+bulbEnergyVar+"</energy>"+			
		"<lifeinsstart>"+$('#bulblifeinsstart').val().trim()+"</lifeinsstart>"+
		"<lifeprogstart>"+$('#bulblifeprogstart').val().trim()+"</lifeprogstart>"+
		"<diameter>"+bulbDiameterVar+"</diameter>"+
		"<length>"+$('#bulblength').val().trim()+"</length>"+
		"<cri>"+bulbCriVar+"</cri>"+
		"<colortemp>"+bulbColorTempVar+"</colortemp>"+
		"</bulb>";
		}

		if("${mode}" == 'edit'){ 
			editBulb();
		}else{					
			addBulb(); 
		}	
		/*$.ajax({
		type: 'POST',
		url: "${checkForDuplicateBulbUrl}"+"?ts="+new Date().getTime(),
		data: bulbXML,
		success: function(data){	
			if(data == null){	
					
			}else {
				if(BULB_ID != data.id){
					displayBulbLabelMessage("Bulb with the name already exists", COLOR_FAILURE);
				}else{
					editBulb();
				}			
			}	
		},
		dataType:"json",
		contentType: "application/xml; charset=utf-8"
	});*/
	
		
}

function editBulb(){	
	$.ajax({
			data: bulbXML,
			type: "POST",
			url: "${editBulbUrl}"+"?ts="+new Date().getTime(),
			success: function(data){
				closeBulbDialog('editmode');				
			},
			error: function(){
				displayBulbLabelMessage("Error.Bulb is not Saved", COLOR_FAILURE);
			},
			dataType:"json",
			contentType : "application/xml; charset=utf-8"
		});
}

function closeBulbDialog(bulbCloseMode)
{
	if("${from}" == 'fixtureclass')
	{
		  $('#bulbFormShow').slideUp('slow', function() {
		    // Animation complete.
		  });
		  clearBulbLabelMessage();
		  //var id = null;
		  //showAddedBulb(id);
		  if(bulbCloseMode=='cancelmode')
		  {
			  fillBulbCombo();
		  }  
		  $("#fixtureClassBulbs").removeAttr("disabled");
	}
	else
	{
	var ifr = parent.document.getElementById('bulbFrame');
	window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src + "?ts="+new Date().getTime();
	}
}

function showAddedBulb(bulbid)
{
	$.ajax({
		  type: 'GET',
		  url: "${loadAllBulbs}"+"?ts="+new Date().getTime(),
		  data: "",
		  success: function(data){
		   if(data!=null){
		    var bulbData = data.bulb;
		    if(bulbData != undefined){
		     $("#fixtureClassBulbs").empty();		     
		     if(bulbData.length == undefined){
		      $('#fixtureClassBulbs').append($('<option></option>').val(bulbData.id).html(bulbData.name));
		      if(bulbid != null) $("#fixtureClassBulbs").val(bulbid);
		     } else if(bulbData.length > 0){
		      $.each(bulbData, function(i, bulb) {
		       $('#fixtureClassBulbs').append($('<option></option>').val(bulb.id).html(bulb.name));
		       if(bulbid != null) $("#fixtureClassBulbs").val(bulbid);
		      });
		     }
		    }
		   }
		  },
		  dataType:"json",
		  contentType: "application/json; charset=utf-8"
		 });
}

function addBulb(){
	$.ajax({
			data: bulbXML,
			type: "POST",
			url: "${addBulbUrl}"+"?ts="+new Date().getTime(),
			success: function(data){
				if("${from}" == 'fixtureclass')
				{						
					showAddedBulb(data.msg);
				}
				closeBulbDialog('addmode');
			},
			error: function(){				
				displayBulbLabelMessage("Error.Bulb is not added", COLOR_FAILURE);
			},
			dataType:"json",
			contentType : "application/xml; charset=utf-8"
		});
	
}

</script>

<div style="clear: both;"><span id="bulbformerror"></span></div>
<div style="margin:10px 0px 0px 20px;">

<table id="addBulbTable">
	<tr>
		<th >Bulb : </th>
		<td ><input id="bulbname" name="bulbname" />
		</td>
	</tr>
	<tr>
		<th >Manufacturer :</th>
		<td ><input id="bulbmanufacturer" name="bulbmanufacturer"/>
		</td>
	</tr>
	<tr>
		<th >Type :</th>
		<td ><input id="bulbtype" name="bulbtype"/>
		</td>
	</tr>
	<tr>
		<th>Initial Lumens :</th>
		<td ><input id="bulbinitiallumens" name="bulbinitiallumens"/>
		</td>
	</tr>
	<tr>
		<th >Design Lumens :</th>
		<td ><input id="bulbdesignlumens" name="bulbdesignlumens"/>
		</td>
	</tr>
	
	<tr>
		<th >Energy : </th>
		<td ><input id="bulbenergy" name="bulbenergy" />
		</td>
	</tr>
	
	<tr>
		<th >Life Ins Start : </th>
		<td ><input id="bulblifeinsstart" name="bulblifeinsstart"/>
		</td>
	</tr>
	<tr>
		<th >Life Prog Start :</th>
		<td ><input id="bulblifeprogstart" name="bulblifeprogstart" />
		</td>
	</tr>
	<tr>
		<th>Diameter : </th>
		<td ><input id="bulbdiameter" name="bulbdiameter"/>
		</td>
	</tr>
	<tr>
		<th>Length : </th>
		<td ><input id="bulblength" name="bulblength"/>
		</td>
	</tr>
	<tr>
		<th>Cri : </th>
		<td ><input id="bulbcri" name="bulbcri"/>
		</td>
	</tr>	
	<tr>
		<th>Color Temperature : </th>
		<td ><input id="bulbcolortemp" name="bulbcolortemp"/>
		</td>
	</tr>
	<tr>
		<th><span></span></th>
		<td>
			<c:if test="${mode == 'add'}">
			<button type="button" onclick="buildXML();">
				Add
			</button>&nbsp;
			</c:if>
			<c:if test="${mode == 'edit'}">
			<button type="button" onclick="buildXML();">
				Save
			</button>&nbsp;
			</c:if>
			<input type="button" id="btnClose"
				value="<spring:message code="action.cancel" />" onclick="closeBulbDialog('cancelmode')">
		</td>
	</tr>
</table>