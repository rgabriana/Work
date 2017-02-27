<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<spring:url value="/devices/fixtures/updateFixture.ems" var="updateFixtureUrl" scope="request" />
<spring:url value="/services/org/fixture/op/dim/abs/" var="dimfixtureUrl" scope="request" />
<spring:url value="/services/org/fixture/op/mode/" var="autofixtureUrl" scope="request" />
<spring:url value="/services/org/fixture/switchrunningimage" var="switchImagefixtureUrl" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Fixture Details</title>

<!-- Util Function required for Fixture Form -->
<script type="text/javascript">
//Constants 
var COMMUNICATION_TYPE_ONE = "Wireless";
var COMMUNICATION_TYPE_TWO = "Powerline";
var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";

function getMACAddress(macAddress)
{
    var strMacAddress = "";
    
    if(macAddress.indexOf(":") <= 0)
    {
        while(macAddress.length > 0)
        {
            if(macAddress.length >= 3)
            {
                strMacAddress += macAddress.substr(0,2) + ":";
                macAddress = macAddress.substr(2);
            }
            else
            {
                strMacAddress += macAddress.substr(0,2);
                macAddress = macAddress.substr(2);
            }
        }
    }
    else
    {
        strMacAddress = macAddress;
    }
    return strMacAddress;
}

function getTemperatureString(iTemperature)
{
    if(iTemperature > 82)
        return "Hot (" + iTemperature + "° F)";
    else if(iTemperature > 75)
        return "Warm (" + iTemperature + "° F)";
    else if(iTemperature > 68)
        return "Normal (" + iTemperature + "° F)";
    else if(iTemperature > 65)
        return "Cool (" + iTemperature + "° F)";
    else
        return "Cold (" + iTemperature + "° F)";
}

function reloadFixturesFrame(){
	var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("fixturesFrame");
	ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src;
}
</script>

<!-- JS function required for Fixture Form -->
<script type="text/javascript">
// 	$("#updateButton").button();
	var BALLASTS_DATA = {}; //JSON Object: key=ballat_id, value=ballast_object_json
	var FX_ORIGINAL_PROFILE_BEFORE_UPDATE = "${fixture.originalProfileFrom}";
	var FX_CURRENT_PROFILE_BEFORE_UPDATE = "${fixture.currentProfile}";
	var PROFILE_DATA = {}; //JSON Object: key=profile_name, value=profile_id
	
	loadFormValues();
	loadAllProfiles();
	
	$('#enableHopper').click(function() {
		$('#isHopper').val(this.checked?"1":"0");
	});
	function loadFormValues(){
		//Fetch Model Attributes in JS Variables which requires manipulation of values
		var temperature = '<c:out value="${fixture.avgTemperature}"/>';
		var macAddress = '<c:out value="${fixture.macAddress}"/>';
		var dimmerControl = '<c:out value="${fixture.dimmerControl}"/>';
		var isHopper = '<c:out value="${fixture.isHopper}"/>';
		var commType = '<c:out value="${fixture.commType}"/>';

		$("#enableHopper").attr('checked', isHopper==1);
		$("#temperature").html(getTemperatureString(temperature));
		$("#macAddress").html(getMACAddress(macAddress));
		$("#Currentstate").html((dimmerControl<=0?"OFF":"ON") + " ("+dimmerControl+"%)");
		
		switch(1*commType){
			case 1:
				$("#communicationType").html(COMMUNICATION_TYPE_ONE);
				$("#macAddressWrapper").css("display", "block");
				$("#ipAddressWrapper").css("display", "none");
			  	break;
			case 2:
				$("#communicationType").html(COMMUNICATION_TYPE_TWO);
				$("#macAddressWrapper").css("display", "none");
				$("#ipAddressWrapper").css("display", "block");
			  	break;
			default:
				$("#communicationType").html("");
		}
		
		
		//Create Slider
		var defaultSliderValue = dimmerControl;
		$( "#slider_fixture" ).slider({
			value: defaultSliderValue,
			min: 0,
			max: 100,
			step: 1,
			change: function( event, ui ) {
				updateSliderValueSpan(ui.value);
				setDimmerState(ui.value);
			}
		});
		updateSliderValueSpan(defaultSliderValue);
	}
	
	function setSliderValue(value){
		$("#slider_fixture").slider("value", value);
	}
	
	function updateSliderValueSpan(value){
		$("#slider_value").html(value+"%");
		$("#slider_value").css("left", (value*3 - (value<10?15:20)) + "px");
	}
	
	function setDimmerState(value){
		var lightControlMinutes = $("#lightControlMinutes").val();
		
		if(lightControlMinutes == "") {
			displayLabelMessage('<spring:message code="fixtureForm.message.validation.time_blank"/>', COLOR_FAILURE);
		}else if(1*lightControlMinutes == 0) {
			displayLabelMessage('<spring:message code="fixtureForm.message.validation.time_zero"/>', COLOR_FAILURE);
		}else {
			clearLabelMessage();
			setCurrentState(value);

			$.ajax({
				type: 'POST',
				url: "${dimfixtureUrl}"+value+"/"+(1*lightControlMinutes),
				data: "<fixtures><fixture><id>${fixture.id}</id></fixture></fixtures>",
				success: function(data){
	 					},
				dataType:"xml",
				contentType: "application/xml; charset=utf-8",
			});
			
		}
	}
	
	function displayLabelMessage(Message, Color) {
		$("#fixture_message").html(Message);
		$("#fixture_message").css("color", Color);
	}
	function clearLabelMessage(Message, Color) {
		displayLabelMessage("", COLOR_DEFAULT);
	}
	
	function setCurrentState(dimmerControlValue) {
		$("#Currentstate").html((dimmerControlValue<=0?"OFF":"ON") + " ("+dimmerControlValue+"%)");
	}
	
	function switchFixtureImage(){
		displayLabelMessage('<spring:message code="fixtureForm.message.restore.waiting"/>', COLOR_DEFAULT);
		$.ajax({
			type: 'POST',
			url: "${switchImagefixtureUrl}",
			data: "<fixture><id>${fixture.id}</id></fixture>",
			success: function(data){
				displayLabelMessage(data.msg, COLOR_SUCCESS);
 			},
			dataType:"json",
			contentType: "application/xml; charset=utf-8",
		});
	}
	
	function saveFixture(){
		var FixtureName = $("#fixtureName").val();
		FixtureName = $.trim(FixtureName);
		
		if(FixtureName == ""){
			displayLabelMessage('<spring:message code="fixtureForm.message.validation.name"/>', COLOR_FAILURE);
			return false;
		} else {
			var new_current_profile = $("#currentProfile").val();
			if(FX_CURRENT_PROFILE_BEFORE_UPDATE != new_current_profile){
				//get groupId from profile name
				var groupId = PROFILE_DATA[$("#currentProfile").val()];
				$("#groupId").val(groupId);
				
				//set originalProfile as current profile
				$("#originalProfileFrom").val(FX_CURRENT_PROFILE_BEFORE_UPDATE);
				
				//update JS variable and form text label
				$("#originalProfileFromText").html(FX_CURRENT_PROFILE_BEFORE_UPDATE);
				FX_ORIGINAL_PROFILE_BEFORE_UPDATE = FX_CURRENT_PROFILE_BEFORE_UPDATE;
				FX_CURRENT_PROFILE_BEFORE_UPDATE = new_current_profile;
			}
			
			displayLabelMessage('<spring:message code="fixtureForm.message.waiting"/>', COLOR_DEFAULT);
			$.post(
				"${updateFixtureUrl}"+"?ts="+new Date().getTime(),
				$("#fixture-form").serialize(),
				function(data){
					var response = eval("("+data+")");
					if(response.success==1){ //Success
						displayLabelMessage(response.message, COLOR_SUCCESS);
						reloadFixturesFrame();
					} else { //Failure
						displayLabelMessage(response.message, COLOR_FAILURE);
					}
				}
			);
			return true;
		}
	}
	
	function setDimmerOnAuto(){
		$.ajax({
			type: 'POST',
			url: "${autofixtureUrl}AUTO",
			data: "<fixtures><fixture><id>${fixture.id}</id></fixture></fixtures>",
			success: function(data){
 				},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8",
		});
		
	}
	
	function getAllBallasts(){
		BALLASTS_DATA = {};
		<c:forEach items="${ballasts}" var="ballast">
			var ballastObj = {};
			ballastObj.id = "${ballast.id}";
			ballastObj.lampnum = "${ballast.lampNum}";
			ballastObj.name = "${ballast.ballastName}";
			BALLASTS_DATA["${ballast.id}"] = ballastObj;
		</c:forEach>
	}

	function loadAllProfiles(){
		PROFILE_DATA = {};
		<c:forEach items="${groups}" var="group">
			PROFILE_DATA["${group.name}"] = "${group.id}";
		</c:forEach>
	}
	
	function setNoOfLamps(){
		var ballastId = $("#ballastType").val();
		$("#noOfBulbs").val(BALLASTS_DATA[ballastId].lampnum); //No. of lamps
	}
	
	$(document).ready(function() {
		getAllBallasts();
	});


</script>

<style>
	#fixture-dialog-form { font-size: 12px; padding:10px; }
	#fixture-dialog-form input.text{ width:96%; }
	#fixture-dialog-form hr{ margin-top:5px; margin-bottom:5px;}
	
	#fixture-dialog-form fieldset{float:left; padding:0; border:0;}
	#fixture-dialog-form fieldset.column33{width:33%; background:none;}
	#fixture-dialog-form fieldset.column50{width:50%;}
	#fixture-dialog-form fieldset.column66{width:66%;}
	
 	#fixture-dialog-form div.fieldWrapper{clear:both; padding:5px; height:15px;}
 	#fixture-dialog-form div.fieldLabel{float:left; width:160px; /*width:40%;*/ font-weight:bold;}
 	#fixture-dialog-form div.fieldValue{float:left; width:49%;}
 	#fixture-dialog-form fieldset.longlabel div.fieldLabel{width:50%;}
/*  	#fixture-dialog-form fieldset.longlabel div.fieldValue{float:left; width:49%;} */
 	
 	
 	#fixture-dialog-form div.fieldValue select{width:95%; height: 100%;}
 	#fixture-dialog-form div.fieldValue input{width:95%; height: 100%;}
 	
 	#fixture-dialog-form div.sliderWrapper{margin-top: 10px;}
 	#fixture-dialog-form div.sliderWrapper div{display:inline;}
 	#fixture-dialog-form div.sliderDiv div{border: thin solid #AAAAAA;padding: 8px;}
</style>


<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
	
	/*Fix for missing border of JQuery Slider panel */
 	#fixture-dialog-form .ui-widget-content {border: 1px solid #888888 !important;}
</style>

</head>
<body>
<div id="fixture-dialog-form" >
	<spring:url value="/fixture/updateFixture.ems" var="actionURL" scope="request"/>
	<div style="padding-bottom: 5px;">
		<button id="updateButton" onclick="javascript:return saveFixture();" style="float:left;" ><spring:message code="fixtureForm.label.updateBtn"/></button>&nbsp;
		<c:if test="${fn:startsWith(fixture.version, '1') == false}">
			<button id="restoreButton" onclick="javascript:return switchFixtureImage();" style="float:left;" ><spring:message code="fixtureForm.label.restoreBtn"/></button>
		</c:if>
		<div id="fixture_message" style="font-size: 14px; font-weight: bold; padding: 5px 0 0 10px; float:left;"></div>
		<br style="clear:both"/>
	</div>
	<form:form id="fixture-form" commandName="fixture" method="post" action="${actionURL}" onsubmit="return false;">
	<form:hidden id="id" name="id" path="id"/>
	<form:hidden id="isHopper" name="isHopper" path="isHopper"/>	
	<div class="upperdiv divtop" >
		<!-- <fieldset class="column50"> -->
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.FixtureName"/>:</div>
				<div class="fieldValue"><form:input class="text" id="fixtureName" name="fixtureName" path="fixtureName"/></div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.Location"/>:</div>
				<div class="fieldValue">${fixture.location}</div>
			</div>
		<!-- </fieldset> -->
		<!-- <br style="clear:both"/> -->
		<!-- <fieldset class="column33"> -->
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.ID"/>:</div>
				<div class="fieldValue">${fixture.id}</div>
			</div>
		<!-- </fieldset> -->
		<!-- <fieldset class="column33"> -->
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.Floorplanposition"/>:</div>
				<div class="fieldValue">${fixture.xaxis},&nbsp;${fixture.yaxis}</div>
			</div>
		<!-- </fieldset> -->
		
	</div>
	<br style="clear:both"/>
	<div class="upperdiv divmiddle" >	
		<fieldset class="column33">
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.Currentstate"/>:</div>
				<div class="fieldValue" id="Currentstate">${fixture.dimmerControl}</div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.Temperature"/>:</div>
				<div class="fieldValue" id="temperature"></div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.CommunicationType"/>:</div>
				<div class="fieldValue" id="communicationType"></div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.LastCommunications"/>:</div>
				<div class="fieldValue"><ems:breakDateDiffInString  dateValue="${fixture.lastConnectivityAt}" datePattern="yyyy-MM-dd HH:mm:ss"/></div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.Applicationversion"/>:</div>
				<div class="fieldValue">${fixture.version}</div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.Ballasttype"/>:</div>
				<div class="fieldValue">
					<form:select id="ballastType" name="ballastType" path="ballast.id" class="text" onchange="javascript: setNoOfLamps();">
						<form:options items="${ballasts}" itemValue="id" itemLabel="ballastName" />
				   </form:select>
			   </div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.LampType"/>:</div>
				<div class="fieldValue">
					<form:select id="lampType" name="lampType" path="bulb.id" class="text">
						<form:options items="${lamps}" itemValue="id" itemLabel="bulbName" />
				   </form:select>
			   </div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.Originalprofilefrom"/>:</div>
				<div class="fieldValue" id="originalProfileFromText">${fixture.originalProfileFrom}</div>
			</div>
			<div class="fieldWrapper" style="display:none;">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.Originalprofilefrom"/>:</div>
				<div class="fieldValue"><form:input class="text" id="originalProfileFrom" name="originalProfileFrom" path="originalProfileFrom"/></div>
			</div>
		</fieldset>
		<fieldset class="column33 longlabel">
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.Ambientlightlevel"/>:</div>
				<div class="fieldValue">${fixture.lightLevel}&nbsp;foot-candles</div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.Lastoccupancyseen"/>:</div>
				<div class="fieldValue"><ems:breakSecondsInString seconds="${fixture.lastOccupancySeen}"/></div>
			</div>
			<div class="fieldWrapper" id="macAddressWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.MACaddress"/>:</div>
				<div class="fieldValue" id="macAddress"></div>
			</div>
			<div class="fieldWrapper" id="ipAddressWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.ipAddress"/>:</div>
				<div class="fieldValue" id="ipAddress">${fixture.ipAddress}</div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.Mode"/>:</div>
				<div class="fieldValue">${fixture.currentState}</div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.Firmwareversion"/>:</div>
				<div class="fieldValue">${fixture.firmwareVersion}</div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.NoofBallast"/>:</div>
				<div class="fieldValue">
					<form:select id="noOfFixtures" name="noOfFixtures" path="noOfFixtures" class="text">
						<form:option value="1">1</form:option>
						<form:option value="2">2</form:option>
						<form:option value="3">3</form:option>
						<form:option value="4">4</form:option>
				   </form:select>
				</div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.Lampperballast"/>:</div>
				<div class="fieldValue">
					<form:select id="noOfBulbs" name="noOfBulbs" path="noOfBulbs" class="text">
						<form:option value="1">1</form:option>
						<form:option value="2">2</form:option>
						<form:option value="3">3</form:option>
						<form:option value="4">4</form:option>
				   </form:select>
			   </div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.Currentprofile"/>:</div>
				<div class="fieldValue">
					<form:select id="currentProfile" name="currentProfile" path="currentProfile" class="text">
						<form:options items="${groups}" itemValue="name" itemLabel="name" />
				   </form:select>
			   </div>
			</div>
			<div class="fieldWrapper" style="display:none;">
				<div class="fieldLabel">Group id:</div>
				<div class="fieldValue">
					<form:input class="text" id="groupId" name="groupId" path="groupId"/>
			   </div>
			</div>
		</fieldset>
		<fieldset class="column33">
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.Currentpowerusage"/>:</div>
				<div class="fieldValue">${fixture.wattage}&nbsp;watts</div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel">&nbsp;&nbsp;</div>
				<div class="fieldValue">&nbsp;&nbsp;</div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel">&nbsp;&nbsp;</div>
				<div class="fieldValue">&nbsp;&nbsp;</div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel">&nbsp;&nbsp;</div>
				<div class="fieldValue">&nbsp;&nbsp;</div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.Bootloaderversion"/>:</div>
				<div class="fieldValue">${fixture.bootLoaderVersion}</div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel"><spring:message code="fixtureForm.label.Voltage"/>:</div>
				<div class="fieldValue">
					<form:select id="voltage" name="voltage" path="voltage" class="text">
						<form:option value="120">120</form:option>
						<form:option value="277">277</form:option>
				   </form:select>
			   </div>
			</div>
			<div class="fieldWrapper">
				<div class="fieldLabel">&nbsp;&nbsp;</div>
				<div class="fieldValue">&nbsp;&nbsp;</div>
			</div>
			<div class="fieldWrapper">
	<!-- 			<div class="fieldLabel"></div> -->
				<div style="padding-top:5px;">
					<input type="checkbox" name="enableHopper" id="enableHopper" style="width:12px;"/>
					<label for="enableHopper" style="font-weight:bold;"><spring:message code="fixtureForm.label.EnableHopper"/></label>
				</div>
			</div>
		</fieldset>
	</div>
	<br style="clear:both"/>
	<div class="upperdiv divbottom">
		<table cellspacing="5" cellpadding="0">
			<tr>
				<td><div class="fieldLabel"><spring:message code="fixtureForm.label.Dimming"/>:&nbsp;</div></td>
				<td>
					<button onclick="javascript:setDimmerOnAuto(); return false;"><spring:message code="fixtureForm.label.Auto"/></button>
				</td>
				<td>
					<div>&nbsp;
						<span><spring:message code="fixtureForm.label.manualfor"/>:</span>&nbsp;
						<input id="lightControlMinutes" type="text" style="width:30px;" value="60"></input>&nbsp;
						<span><spring:message code="fixtureForm.label.min"/></span>&nbsp;
						<button onclick="javascript:setSliderValue(0); return false;"><spring:message code="fixtureForm.label.Off"/></button>&nbsp;
					</div>
				</td>
				<td style="width:300px; padding: 2px 20px;">
					<div id="slider_marker">&nbsp;
						<span style="float:left;color:#AAA;">0%</span>
						<span style="float:left;color:#AAA;margin-left:43%;">50%</span>
						<span style="float:right;color:#AAA;">100%</span>
					</div>
					<div id="slider_fixture"></div>
					<div id="slider_info" style="padding-top: 7px;">&nbsp;
						<span id="slider_value" style="position:relative; background-color: #DDDDDD; border: thin solid #AAAAAA; padding: 0 2px;">0%</span>
					</div>
				</td>
				<td>&nbsp;
					<button onclick="javascript:setSliderValue(100); return false;"><spring:message code="fixtureForm.label.Fullon"/></button>
				</td>
			</tr>
		</table>
	</div>	
	</form:form>
</div>
</body>
</html>