<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<spring:url value="/services/org/bulbservice/add" var="addBulbUrl" scope="request" />
<spring:url value="/services/org/bulbservice/edit" var="editBulbUrl" scope="request" />
<spring:url value="/services/org/bulbservice/details" var="checkForDuplicateBulbUrl" scope="request" />
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

$(document).ready(function() {
	clearLabelMessage() ;	
	$("#name").val($.trim("${bulb.bulbName}"));
	$("#manufacturer").val($.trim("${bulb.manufacturer}"));
	$("#type").val($.trim("${bulb.type}"));
	$("#initiallumens").val($.trim("${bulb.initialLumens}"));
	$("#designlumens").val($.trim("${bulb.designLumens}"));
	$("#energy").val($.trim("${bulb.energy}"));
	$("#lifeinsstart").val($.trim("${bulb.lifeInsStart}"));
	$("#lifeprogstart").val($.trim("${bulb.lifeProgStart}"));
	$("#diameter").val($.trim("${bulb.diameter}"));
	$("#length").val($.trim("${bulb.length}"));
	$("#cri").val($.trim("${bulb.cri}"));
	$("#colortemp").val($.trim("${bulb.colorTemp}"));
	
});

function displayLabelMessage(Message, Color) {
		$("#error").html(Message);
		$("#error").css("color", Color);
}
function clearLabelMessage() {
	 $("#error").html("");
	 $("#error").css("color", COLOR_DEFAULT);
}

function buildXML()
{
if ( $('#name').val().trim() == ''){
		displayLabelMessage("Bulb Name Field should not be empty", COLOR_FAILURE);
		return false;
	}
	
if("${mode}" == 'edit'){
		bulbXML	=   "<bulb>"+
		"<id>"+"${bulb.id}"+"</id>"+
		"<name>"+$('#name').val().trim()+"</name>"+
		"<manufacturer>"+$('#manufacturer').val().trim()+"</manufacturer>"+
		"<type>"+$('#type').val().trim()+"</type>"+
		"<initiallumens>"+$('#initiallumens').val().trim()+"</initiallumens>"+
		"<designlumens>"+$('#designlumens').val().trim()+"</designlumens>"+
		"<energy>"+$('#energy').val().trim()+"</energy>"+
		"<lifeinsstart>"+$('#lifeinsstart').val().trim()+"</lifeinsstart>"+
		"<lifeprogstart>"+$('#lifeprogstart').val().trim()+"</lifeprogstart>"+
		"<diameter>"+$('#diameter').val().trim()+"</diameter>"+
		"<length>"+$('#length').val().trim()+"</length>"+
		"<cri>"+$('#cri').val().trim()+"</cri>"+
		"<colortemp>"+$('#colortemp').val().trim()+"</colortemp>"+
		"</bulb>";
		
		}
else
		{		
		bulbXML	=   "<bulb>"+		
		"<name>"+$('#name').val().trim()+"</name>"+
		"<manufacturer>"+$('#manufacturer').val().trim()+"</manufacturer>"+
		"<type>"+$('#type').val().trim()+"</type>"+
		"<initiallumens>"+$('#initiallumens').val().trim()+"</initiallumens>"+
		"<designlumens>"+$('#designlumens').val().trim()+"</designlumens>"+
		"<energy>"+$('#energy').val().trim()+"</energy>"+
		"<lifeinsstart>"+$('#lifeinsstart').val().trim()+"</lifeinsstart>"+
		"<lifeprogstart>"+$('#lifeprogstart').val().trim()+"</lifeprogstart>"+
		"<diameter>"+$('#diameter').val().trim()+"</diameter>"+
		"<length>"+$('#length').val().trim()+"</length>"+
		"<cri>"+$('#cri').val().trim()+"</cri>"+
		"<colortemp>"+$('#colortemp').val().trim()+"</colortemp>"+
		"</bulb>";
		}
		$.ajax({
		type: 'POST',
		url: "${checkForDuplicateBulbUrl}"+"?ts="+new Date().getTime(),
		data: bulbXML,
		success: function(data){	
			if(data == null){	
				if("${mode}" == 'edit'){ 
					editBulb();
				}else{					
					addBulb(); 
				}		
			}else {
				if(BULB_ID != data.id){
					displayLabelMessage("Bulb with the name already exists", COLOR_FAILURE);
				}else{
					editBulb();
				}			
			}	
		},
		dataType:"json",
		contentType: "application/xml; charset=utf-8"
	});
		
}

function editBulb(){	
	$.ajax({
			data: bulbXML,
			type: "POST",
			url: "${editBulbUrl}"+"?ts="+new Date().getTime(),
			success: function(data){
				closeBulbDialog();				
			},
			error: function(){
				displayLabelMessage("Error.Bulb is not Saved", COLOR_FAILURE);
			},
			dataType:"json",
			contentType : "application/xml; charset=utf-8"
		});
}

function closeBulbDialog()
{
	var ifr = parent.document.getElementById('bulbFrame');
	window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src + "?ts="+new Date().getTime();
}

function addBulb(){
	$.ajax({
			data: bulbXML,
			type: "POST",
			url: "${addBulbUrl}"+"?ts="+new Date().getTime(),
			success: function(data){
				closeBulbDialog();
			},
			error: function(){				
				displayLabelMessage("Error.Bulb is not added", COLOR_FAILURE);
			},
			dataType:"json",
			contentType : "application/xml; charset=utf-8"
		});
	
}

</script>

<div style="clear: both;"><span id="error"></span></div>
<div style="margin:10px 0px 0px 20px;">

<table id="addBulbTable">
	<tr>
		<th >Bulb : </th>
		<td ><input id="name" name="name" />
		</td>
	</tr>
	<tr>
		<th >Manufacturer :</th>
		<td ><input id="manufacturer" name="manufacturer"/>
		</td>
	</tr>
	<tr>
		<th >Type :</th>
		<td ><input id="type" name="type"/>
		</td>
	</tr>
	<tr>
		<th>Initial Lumens :</th>
		<td ><input id="initiallumens" name="initiallumens"/>
		</td>
	</tr>
	<tr>
		<th >Design Lumens :</th>
		<td ><input id="designlumens" name="designlumens"/>
		</td>
	</tr>
	
	<tr>
		<th >Energy : </th>
		<td ><input id="energy" name="energy" />
		</td>
	</tr>
	
	<tr>
		<th >Life Ins Start : </th>
		<td ><input id="lifeinsstart" name="lifeinsstart"/>
		</td>
	</tr>
	<tr>
		<th >Life Prog Start :</th>
		<td ><input id="lifeprogstart" name="lifeprogstart" />
		</td>
	</tr>
	<tr>
		<th>Diameter : </th>
		<td ><input id="diameter" name="diameter"/>
		</td>
	</tr>
	<tr>
		<th>Length : </th>
		<td ><input id="length" name="length"/>
		</td>
	</tr>
	<tr>
		<th>Cri : </th>
		<td ><input id="cri" name="cri"/>
		</td>
	</tr>	
	<tr>
		<th>Color Temperature : </th>
		<td ><input id="colortemp" name="colortemp"/>
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
				value="<spring:message code="action.cancel" />" onclick="closeBallastDialog()">
		</td>
	</tr>
</table>