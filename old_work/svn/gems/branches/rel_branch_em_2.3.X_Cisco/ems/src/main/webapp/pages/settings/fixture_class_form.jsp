<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<spring:url value="/services/org/fixtureclassservice/add" var="addFixtureClassUrl" scope="request" />
<spring:url value="/services/org/fixtureclassservice/edit" var="editFixtureClassUrl" scope="request" />
<spring:url value="/services/org/fixtureclassservice/details" var="checkForDuplicateFixtureClassUrl" scope="request" />

<style>

#addFixtureClassTable table {
	border: thin dotted #7e7e7e;
	padding: 10px;
}

#addFixtureClassTable th {
	text-align: right;
	vertical-align: top;
	padding-right: 10px;
}

#addFixtureClassTable td {
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
var FIXTURE_CLASS_ID = "${fixtureClass.id}";

$(document).ready(function() {
	clearLabelMessage() ;
	
	//load Ballasts combo
	$("#ballasts").empty();
	<c:forEach items="${ballasts}" var="ballast">
		$('#ballasts').append($('<option></option>').val("${ballast.id}").html("${ballast.ballastName}"));
	</c:forEach>
	
	//load Bulbs combo
	$("#bulbs").empty();
	<c:forEach items="${bulbs}" var="bulb">
		$('#bulbs').append($('<option></option>').val("${bulb.id}").html("${bulb.bulbName}"));
	</c:forEach>	
	
	//Load voltage levels
	<c:forEach items="${voltagelevels}" var="voltagelevel">
		$('#voltage').append($('<option></option>').val("${voltagelevel.voltage}").html("${voltagelevel.voltage}"));
	</c:forEach>
	
	//Load noOfBallasts
	<c:forEach items="${ballastcount}" var="bCount">
		$('#noOfBallasts').append($('<option></option>').val("${bCount.ballastCount}").html("${bCount.ballastCount}"));
	</c:forEach>
	
	if("${mode}" == 'edit'){
		$('#name').val("${fixtureClass.name}");
		$('#noOfBallasts').val("${fixtureClass.noOfBallasts}");
		$('#voltage').val("${fixtureClass.voltage}");
		$('#ballasts').val("${ballastId}");
		$('#bulbs').val("${bulbId}");
	}
		
});


function displayLabelMessage(Message, Color) {
		$("#error").html(Message);
		$("#error").css("color", Color);
}
function clearLabelMessage() {
	 $("#error").html("");
	 $("#error").css("color", COLOR_DEFAULT);
}

function closeFixtureClassDialog(){
	refreshFixtureClassListFrame();
	$("#newFixtureClassDialog").dialog("close");
}

function refreshFixtureClassListFrame(){
	var ifr = parent.document.getElementById('fixtureclassFrame');
	window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src + "?ts="+new Date().getTime();
}


function validateFixtureClassForm(){
	
	clearLabelMessage();
	
	if ( $('#name').val().trim() == ''){
		displayLabelMessage("Fixture Class Name Field should not be empty", COLOR_FAILURE);
		return false;
	}
	if ( $('#noOfBallasts').val().trim() == ''){
		displayLabelMessage("No of ballasts Field should not be empty", COLOR_FAILURE);
		return false;
	}else{
		if(!IsNumericValue($('#noOfBallasts').val().trim())){
			displayLabelMessage("No of ballasts Field should be a Number", COLOR_FAILURE);
			return false;
		}
	}
	if ( $('#voltage').val().trim() == ''){
		displayLabelMessage("Volatge Field should not be empty", COLOR_FAILURE);
		return false;
	}else{
		if(!IsNumericValue($('#voltage').val().trim())){
			displayLabelMessage("Voltage should be a Number", COLOR_FAILURE);
			return false;
		}
	}
	
	checkForDuplicateFixtureClass();
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

function checkForDuplicateFixtureClass(){
	var fixtureClassName = $('#name').val().trim();
	
	$.ajax({
		type: 'POST',
		url: "${checkForDuplicateFixtureClassUrl}"+"/name/"+$('#name').val().trim()+"/noOfBallasts/"+$('#noOfBallasts').val().trim()+
		"/voltage/"+$('#voltage').val().trim()+"/ballastId/"+$('#ballasts').val()+"/bulbId/"+$('#bulbs').val()+"?ts="+new Date().getTime(),
		data: "",
		success: function(data){
			if(data == null){
				if("${mode}" == 'edit'){
					editFixtureClass();
				}else{
					addFixtureClass();
				}
					
			}else{
				if(FIXTURE_CLASS_ID != data.id){
					if($('#name').val().trim() == data.name){
						displayLabelMessage("A Fixture Class with the same name already exists.", COLOR_FAILURE);
					}else{
						var proceed = confirm("A Fixture Class with the same combination (Ballast,Bulb,No of Ballasts,Voltage) already exists.Do you want to continue?");
						if(proceed == true){
							if("${mode}" == 'edit'){
								editFixtureClass();
							}else{
								addFixtureClass();
							}
						}
					}
					
				}else{
					editFixtureClass();
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

function addFixtureClass(){
	
	$.ajax({
			data: "",
			type: "POST",
			url: "${addFixtureClassUrl}"+"/name/"+$('#name').val().trim()+"/noOfBallasts/"+$('#noOfBallasts').val().trim()+
					"/voltage/"+$('#voltage').val().trim()+"/ballastId/"+$('#ballasts').val()+"/bulbId/"+$('#bulbs').val()+"?ts="+new Date().getTime(),
			success: function(data){
					closeFixtureClassDialog();
			},
			error: function(){
				displayLabelMessage("Error.Fixture Class is not added", COLOR_FAILURE);
			},
			dataType:"json",
			contentType : "application/xml; charset=utf-8"
		});
	
}

function editFixtureClass(){
	
	$.ajax({
			data: "",
			type: "POST",
			url: "${editFixtureClassUrl}"+"/id/"+"${fixtureClass.id}"+"/name/"+$('#name').val().trim()+"/noOfBallasts/"+$('#noOfBallasts').val().trim()+
					"/voltage/"+$('#voltage').val().trim()+"/ballastId/"+$('#ballasts').val()+"/bulbId/"+$('#bulbs').val()+"?ts="+new Date().getTime(),
			success: function(data){
					closeFixtureClassDialog();
			},
			error: function(){
				displayLabelMessage("Error.Fixture Class is not saved", COLOR_FAILURE);
			},
			dataType:"json",
			contentType : "application/xml; charset=utf-8"
		});
	
}

</script>
<div style="clear: both;"><span id="error"></span></div>
<div style="margin:10px 0px 0px 20px;">

	<table id="addFixtureClassTable">
		<tr>
			<td class="fieldLabel">Name:</td>
			<td class="fieldValue"><input id="name" name="name" /></td>
		</tr>
		<tr>
			<td class="fieldLabel">Ballast:</th>
			<td class="fieldValue"><select id="ballasts" style="width:100%; height:100%;"> </select></td>
		</tr>
		<tr>
			<td class="fieldLabel">Bulb:</th>
			<td class="fieldValue"><select id="bulbs" style="width:100%; height:100%;"> </select></td>
		</tr>
		<tr>
			<td class="fieldLabel">No of Ballasts:</th>
			<td class="fieldValue"><select id="noOfBallasts" name="noOfBallasts"></select></td>
		</tr>
		<tr>
			<td class="fieldLabel">Voltage:</th>
			<td class="fieldValue"><select id="voltage" name="voltage"></select></td>
		</tr>
		<tr>
			<th><span></span></th>
			<td class="fieldValue">
				<c:if test="${mode == 'add'}">
				<button type="button" onclick="validateFixtureClassForm();">
					Add
				</button>&nbsp;
				</c:if>
				<c:if test="${mode == 'edit'}">
				<button type="button" onclick="validateFixtureClassForm();">
					Save
				</button>&nbsp;
				</c:if>
				<input type="button" id="btnClose"
					value="<spring:message code="action.cancel" />" onclick="closeFixtureClassDialog()">
			</td>
		</tr>
	</table>
</div>