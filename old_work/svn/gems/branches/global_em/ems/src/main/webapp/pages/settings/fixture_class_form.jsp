<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<spring:url value="/services/org/fixtureclassservice/add" var="addFixtureClassUrl" scope="request" />
<spring:url value="/services/org/fixtureclassservice/edit" var="editFixtureClassUrl" scope="request" />
<spring:url value="/services/org/fixtureclassservice/details" var="checkForDuplicateFixtureClassUrl" scope="request" />
<spring:url value="/services/org/fixtureclassservice/getfixtureCount/" var="checkForFixtureClassFixtureCountUrl" scope="request" />
<spring:url value="/settings/addballast.ems" var="newBallastUrl" scope="request" />
<spring:url value="/settings/addbulb.ems" var="newBulbUrl" scope="request" />

<style>

#center {
  height : 95% !important;
}

.invidiv { display: none;border:2px solid;width:90%;}

#mForm .fieldWrapper{padding-bottom:4px;width:100%;}
#mForm .fieldlabel{float:left; height:22px; width: 14%;}
#mForm .fieldInput{float:left; height:22px; width: 50%;}

</style>


<script type="text/javascript">

var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";

//Global variable for accessing current fixture class id
var FIXTURE_CLASS_ID = "${fixtureClass.id}";

//Global variable for accessing current page
var FIXTURE_CLASS_PAGE = "${page}";

$(document).ready(function() {
	clearLabelMessage();
	$('#ballastFormShow').load("${newBallastUrl}"+"?ts="+new Date().getTime()+"&from=fixtureclass");
	$('#bulbFormShow').load("${newBulbUrl}"+"?ts="+new Date().getTime()+"&from=fixtureclass");
		
	//load Ballasts combo
	$("#fixtureClassBallasts").empty();
	<c:forEach items="${ballasts}" var="ballast">
		$('#fixtureClassBallasts').append($('<option></option>').val("${ballast.id}").html("${ballast.displayLabel}"));
	</c:forEach>
	
	//load Bulbs combo
	$("#fixtureClassBulbs").empty();
	<c:forEach items="${bulbs}" var="bulb">
		$('#fixtureClassBulbs').append($('<option></option>').val("${bulb.id}").html("${bulb.bulbName}"));
	</c:forEach>	
	
	//Load voltage levels
	<c:forEach items="${voltagelevels}" var="voltagelevel">
		$('#fixtureClassVoltage').append($('<option></option>').val("${voltagelevel.voltage}").html("${voltagelevel.voltage}"));
	</c:forEach>
	
	//Load noOfBallasts
	<c:forEach items="${ballastcount}" var="bCount">
		$('#fixtureClassNoOfBallasts').append($('<option></option>').val("${bCount.ballastCount}").html("${bCount.ballastCount}"));
	</c:forEach>
	
	if("${mode}" == 'edit'){
		$('#fixtureClassName').val("${fixtureClass.name}");
		$('#fixtureClassNoOfBallasts').val("${fixtureClass.noOfBallasts}");
		$('#fixtureClassVoltage').val("${fixtureClass.voltage}");
		$('#fixtureClassBallasts').val("${ballastId}");
		$('#fixtureClassBulbs').val("${bulbId}");
	}	
});

function fillBallastCombo()
{
	showAddedBallast(null);	
}

function fillBulbCombo()
{	
	showAddedBulb(null);	
}

function clearLabelMessage() {
	 $("#error").html("");
	 $("#error").css("color", COLOR_DEFAULT);
}

function closeFixtureClassDialog(){
	if ( FIXTURE_CLASS_PAGE != "COMMISSION"){
		refreshFixtureClassListFrame();
	}
	$("#newFixtureClassDialog").dialog("close");
}

function refreshFixtureClassListFrame(){
	var ifr = parent.document.getElementById('fixtureclassFrame');
	window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src + "?ts="+new Date().getTime();
}

function validateFixtureClassForm(){	
	clearLabelMessage();
	
	if ( $.trim($('#fixtureClassName').val()) == ''){
		displayLabelMessage("Fixture Type Name Field should not be empty", COLOR_FAILURE);		
		return false;
	}
	if ( $.trim($('#fixtureClassNoOfBallasts').val()) == ''){
		displayLabelMessage("No of ballasts Field should not be empty", COLOR_FAILURE);
		return false;
	}else{
		if(!IsNumericValue($.trim($('#fixtureClassNoOfBallasts').val()))){
			displayLabelMessage("No of ballasts Field should be a Number", COLOR_FAILURE);
			return false;
		}
	}
	if ( $.trim($('#fixtureClassVoltage').val()) == ''){
		displayLabelMessage("Voltage Field should not be empty", COLOR_FAILURE);
		return false;
	}else{
		if(!IsNumericValue($.trim($('#fixtureClassVoltage').val()))){
			displayLabelMessage("Voltage should be a Number", COLOR_FAILURE);
			return false;
		}
	}
	
	if("${mode}" == 'edit'){
		checkForFixtureClassFixtureCount();
	}else{
		checkForDuplicateFixtureClass();
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

function checkForFixtureClassFixtureCount(){
	
	$.ajax({
		type: 'POST',
		url: "${checkForFixtureClassFixtureCountUrl}"+"${fixtureClass.id}"+"?ts="+new Date().getTime(),
		data: "",
		success: function(data){
			if(data.status == 0){
				checkForDuplicateFixtureClass();					
			}else{
				var proceed = confirm("One or more Fixtures are associated with this Fixture Type.Do you want to continue?");
				if(proceed == true){
					checkForDuplicateFixtureClass();	
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

function checkForDuplicateFixtureClass(){
	var fixtureClassName = $.trim($('#fixtureClassName').val());
	
	$.ajax({
		type: 'POST',
		url: "${checkForDuplicateFixtureClassUrl}"+"/name/"+$.trim($('#fixtureClassName').val())+"/noOfBallasts/"+$.trim($('#fixtureClassNoOfBallasts').val())+
		"/voltage/"+$.trim($('#fixtureClassVoltage').val())+"/ballastId/"+$('#fixtureClassBallasts').val()+"/bulbId/"+$('#fixtureClassBulbs').val()+"?ts="+new Date().getTime(),
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
					if($.trim($('#fixtureClassName').val()) == data.name){
						displayLabelMessage("A Fixture Type with the same name already exists.", COLOR_FAILURE);
					}else{
						var proceed = confirm("A Fixture Type with the same combination (Ballast,Bulb,No of Ballasts,Voltage) already exists.Do you want to continue?");
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
			url: "${addFixtureClassUrl}"+"/name/"+$.trim($('#fixtureClassName').val())+"/noOfBallasts/"+$.trim($('#fixtureClassNoOfBallasts').val())+
					"/voltage/"+$.trim($('#fixtureClassVoltage').val())+"/ballastId/"+$('#fixtureClassBallasts').val()+"/bulbId/"+$('#fixtureClassBulbs').val()+"?ts="+new Date().getTime(),
			success: function(data){
					if ( FIXTURE_CLASS_PAGE == "COMMISSION"){
						parent.refreshFixtureClassCombo(data.msg);
					}
					closeFixtureClassDialog();
			},
			error: function(){
				displayLabelMessage("Error.Fixture Type is not added", COLOR_FAILURE);
			},
			dataType:"json",
			contentType : "application/xml; charset=utf-8"
		});
	
}

function editFixtureClass(){
	
	$.ajax({
			data: "",
			type: "POST",
			url: "${editFixtureClassUrl}"+"/id/"+"${fixtureClass.id}"+"/name/"+$.trim($('#fixtureClassName').val())+"/noOfBallasts/"+$.trim($('#fixtureClassNoOfBallasts').val())+
					"/voltage/"+$.trim($('#fixtureClassVoltage').val())+"/ballastId/"+$('#fixtureClassBallasts').val()+"/bulbId/"+$('#fixtureClassBulbs').val()+"?ts="+new Date().getTime(),
			success: function(data){
					if ( FIXTURE_CLASS_PAGE == "COMMISSION"){
						parent.refreshFixtureClassCombo("${fixtureClass.id}");
					}
					closeFixtureClassDialog();
			},
			error: function(){
				displayLabelMessage("Error.Fixture Type is not saved", COLOR_FAILURE);
			},
			dataType:"json",
			contentType : "application/xml; charset=utf-8"
		});	
}


function displayLabelMessage(Message, Color) {
	$("#error").html(Message);
	$("#error").css("color", Color);
}

function addBallastFormShow()
{	  
	  $('#ballastFormShow').load("${newBallastUrl}"+"?ts="+new Date().getTime()+"&from=fixtureclass");
	  //Clear list and disable it
	  $("#fixtureClassBallasts").empty();
	  $("#fixtureClassBallasts").attr("disabled", true);
	  
	  if($('#fixtureClassBulbs').is(':disabled'))
	  {			  
		  showAddedBulb(null);
		  $("#fixtureClassBulbs").removeAttr("disabled");
	  }
	  
	  $('#ballastFormShow').show("slow"); 
	  $('#bulbFormShow').slideUp('slow', function() {
		    // Animation complete.
		  });
	  clearBulbLabelMessage();
}

function addBulbFormShow()
{
      $('#bulbFormShow').load("${newBulbUrl}"+"?ts="+new Date().getTime()+"&from=fixtureclass");
	  $("#fixtureClassBulbs").empty();
	  $("#fixtureClassBulbs").attr("disabled", true);
	  
	  if($('#fixtureClassBallasts').is(':disabled'))
	  {			  
		  showAddedBallast(null);
		  $("#fixtureClassBallasts").removeAttr("disabled");
	  }
	  
	  $('#bulbFormShow').show("slow"); 
	  $('#ballastFormShow').slideUp('slow', function() {
		    // Animation complete.
		  });
	  clearBallastLabelMessage();
}

function closeBDialog()
{
$('#ballastFormShow').slideUp('slow', function() {
    // Animation complete.
  });
}
</script>
					
<div style="clear: both;"><span id="error"></span></div>
<div id="mForm" style="margin-left:50px;margin-top:20px;height:300px; overflow: auto;">		
			
			<div class="fieldWrapper" >	
			<div class="fieldlabel" style="display:inline-block;"><b>Name : </b></div>		
			<div class="fieldInput" style="display:inline-block;padding-left: 10px;"><input id="fixtureClassName" name="fixtureClassName" style="min-width:210px;"/></div>
			<br style="clear:both;"/>
			</div>
						
			<div class="fieldWrapper" >
			<div class="fieldlabel" style="display:inline-block;"><b>Ballast:</b></div>		
			<div class="fieldValue" style="display:inline-block;padding-left: 10px;"><select id="fixtureClassBallasts" style="width:100%; height:100%;min-width:210px;"></select></div>
			<div id="ballastBtnDiv" class="fieldValue" style="display:inline-block;"><c:if test="${mode == 'add'}"><button type="button" id="btnShowBallastForm" onclick="addBallastFormShow();" style="min-width:75px;">Add Ballast</button></c:if></div>
			<br style="clear:both;"/>
			</div>
			
			<div id="ballastFormShow" class="invidiv">					

			</div>
			
			<div class="fieldWrapper" >
			<div class="fieldlabel" style="display:inline-block;"><b>No. Of Ballast:</b></div>		
			<div class="fieldValue" style="display:inline-block;padding-left: 10px;"><select id="fixtureClassNoOfBallasts" name="fixtureClassNoOfBallasts" style="min-width:215px;"></select></div>
			<br style="clear:both;"/>			
			</div>	
			
			<br style="clear:both;"/>				
			<div class="fieldWrapper" >	
			<div class="fieldlabel" style="display:inline-block;"><b>Bulb:</b></div>		
			<div class="fieldValue" style="display:inline-block;padding-left: 10px;"><select id="fixtureClassBulbs" style="width:100%; height:100%;min-width:215px;"> </select></div>
			<div id="bulbBtnDiv" class="fieldValue" style="display:inline-block;"><c:if test="${mode == 'add'}"><button type="button" id="btnShowBulbForm" onclick="addBulbFormShow();" style="min-width:75px;">Add Bulb</button></c:if></div>
			<br style="clear:both;"/>
			</div>
			<div id="bulbFormShow" class="invidiv">	
			</div>
			<div class="fieldWrapper" >
			<div class="fieldlabel" style="display:inline-block;"><b>Voltage:</b></div>					
			<div class="fieldValue" style="display:inline-block;padding-left: 10px;"><select id="fixtureClassVoltage" name="fixtureClassVoltage" style="min-width:215px;"></div>			
			<br style="clear:both;"/>					
			</div>			
			
			<div class="fieldWrapper" >
			<div class="fieldlabel" style="display:inline-block;"><input type="button" id="btn" style="display: none;"></input></div>
			<br style="clear:both;"/><br>			
			</div>

			<div class="fieldWrapper" >
			<div class="fieldlabel" style="display:inline-block;"></div>
			<div class="fieldValue" style="display:inline-block;padding-left: 10px;">
			<c:if test="${mode == 'add'}">
			<input type="button" onclick="validateFixtureClassForm();" style="min-width:75px;" value="Add"></input>
			</c:if>
			<c:if test="${mode == 'edit'}">
			<input type="button" onclick="validateFixtureClassForm();" style="min-width:75px;" value="Save"></input>
			</c:if>
			</div>			
			<div class="fieldValue" style="display:inline-block;"><input type="button" style="min-width:75px;" id="btnClose" value="<spring:message code="action.cancel" />" onclick="closeFixtureClassDialog()"></div>
			<br style="clear:both;"/><br>					
			</div>		
</div>



