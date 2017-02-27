<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/scripts/jquery/jquery.ptTimeSelect.js" var="ptTimeSelect"></spring:url>
<script type="text/javascript" src="${ptTimeSelect}"></script>
<spring:url value="/themes/standard/css/jquery/jquery.ptTimeSelect.css" var="ptTimeSelectCss"></spring:url>
<link rel="stylesheet" type="text/css" href="${ptTimeSelectCss}" />
<spring:url value="/scripts/jquery/jquery.validationEngine.js" var="jquery_validationEngine"></spring:url>
<script type="text/javascript" src="${jquery_validationEngine}"></script>
<spring:url value="/scripts/jquery/jquery.validationEngine-en.js" var="jquery_validationEngine_en"></spring:url>
<script type="text/javascript" src="${jquery_validationEngine_en}"></script>
<spring:url value="/themes/default/images/time_picker.jpeg" var="timePicker" scope="request"/>

<style type="text/css">
	html, body{margin:3px 3px 0px 3px !important; padding:0px !important; background: #ffffff;}	
	.entable tr input[type="text"] { border: none !important;}
	
	.settingProfileNameFieldLabel {padding-left: 10px; display:inline;padding-right: 10px;}
	.settingProfileNameFieldLabel span {font-weight: bold}
	.settingProfileNameFieldValue {position : relative ; display: inline;}
	.settingDivOuterForTopBox { clear: both; padding-bottom: 5px; }
	.settingProfileNameSelectValue {position : relative ; display: inline;width: 100%;clear: both;}
	
	.disablePushButton
	{
		background: #CCCCCC !important;
		border-color :#CCCCCC !important;
		color:#FFFFFF; font-size:13px; clear: both; float: left; margin: -3px; width:85px;height:35px;border:0px;font-weight:bold;
	}
	.enablePushButton
	{
		color:#FFFFFF;background-color:#5a5a5a; font-size:13px; clear: both; float: left; margin: -3px; width:85px;height:35px;border:0px;font-weight:bold;
	}
</style>

<!--TODO Move profile name change logic to profile tree on click event & get the profile value from tree itself-->
<script type="text/javascript">
	var weekdays = new Array();
	//var selected_node = ($.cookie('jstree_select')).replace("#","");
	//$('#profileName', window.parent.document).html(selected_node);
</script>

<script type="text/javascript">
var manageType;
var submit=false;
var OriprofileName="";
	$(document).ready(function() {		
		manageType = "${management}";
		OriprofileName = "${group.name}";
		$('#daylightHarvestingInputCheckbox').prop('disabled', false);
		$('#daylightProfileBelowMinCheckBox').prop('disabled', false);
		$('#daylightForceProfileMinValueCheckBox').prop('disabled', false);
		if(manageType=='new')
		{
			var oldProfileName = "${oldProfileName}";
			var profileName = "${group.name}";
			if(oldProfileName!="")
			{
				$("#profilename").val(oldProfileName);
			}else
			{
				$("#profilename").val("");
			}
			$('#edit-basic input').removeAttr("readonly");
			var deactivator = function(event){ event.preventDefault(); }; 
			$(':checkbox').unbind('click',deactivator); 
		}
		else if(manageType=='edit')
		{
			 $("#derivedProfile").attr("disabled","disabled");
			 var profileName = "${group.name}";
			if("${group.defaultProfile}"=='true')
			{
				if(profileName!="Default"){
					$("#pfprofilename1").text(profileName+"_Default");
					$('#edit-basic input').attr('readonly', 'readonly'); 
					var deactivator = function(event){ event.preventDefault(); }; 
					$(':checkbox').click(deactivator); 
				}
			}else{
				$("#profileEditname").val(profileName);
			}
		}else if(manageType=='readonly')
		{
			$('#daylightHarvestingInputCheckbox').prop('disabled', true);
			$('#daylightProfileBelowMinCheckBox').prop('disabled', true);
			$('#daylightForceProfileMinValueCheckBox').prop('disabled', true);
			if("${group.defaultProfile}"=='true')			
			{			
			$("#drlow_combo").attr('disabled',true);
			$("#drmoderate_combo").attr('disabled',true);
			$("#drhigh_combo").attr('disabled',true);
			$("#drspecial_combo").attr('disabled',true);
			$("#holiday_combo").attr('disabled',true);
			}
		else
			{
			$("#drlow_combo").attr('disabled',false);
			$("#drmoderate_combo").attr('disabled',false);
			$("#drhigh_combo").attr('disabled',false);
			$("#drspecial_combo").attr('disabled',false);
			$("#holiday_combo").attr('disabled',false);
			}
			$('#edit-basic input').attr('readonly', 'readonly');
			var deactivator = function(event){ event.preventDefault(); }; 
			$(':checkbox').click(deactivator); 
			var pushPendingFlag = "${pushPendingStatus}";
			$('#pushbtn').attr("disabled",true);
			if(pushPendingFlag=='true')
			{
				$('#pushbtn').removeAttr("disabled");
				$('#pushbtn').removeClass("disablePushButton");	
				$('#pushbtn').addClass("enablePushButton");	
			}
		}
		$("#edit-basic").validationEngine('attach' , {

			isOverflown: true,

			overflownDIV: "#settingDivTop"

		});
		
		var dialogOptions = {
			title : "<spring:message code='profile.help.title'/>",
			modal : true,
			height : 500,
			width : 700,
			draggable : true,
			close : function(ev, ui){
				$(this).dialog("destroy");
			}
		};
		
		var dialogAdvancedOptions = {
				title : "<spring:message code='profile.advancedhelp.title'/>",
				modal : true,
				height : 500,
				width : 700,
				draggable : true,
				close : function(ev, ui){
					$(this).dialog("destroy");
				}
			};
		
		$('.help').click(function() {
			$("#helpDialog").html('<p>This screen shows the schedule-based configuration of a fixture. A fixture\'s behavior is configured for 4 periods per day for each type of day, which are weekdays and weekends. The start time for each period must be specified. A period begins at the specified start time and ends at the beginning of the start time of the next period, with "Day" following "Morning", "Evening" following "Day", "Night" following "Evening", and "Morning" following "Night". The "Weekdays" must be selected. Any day not selected is a weekend day.</p>' + 
			'<br /><p>The columns in the table are:</p><br />' + 
			'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Period -- the period of the day that the settings apply. Typically, morning starts at 6am; followed by day, which starts at 9am; followed by evening, which starts at 6pm; followed by night, which starts at 9pm; and which ends at the next morning.</p><br />' +
			'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Min light level when on (0-100)(%) -- the minimum level between 0 and 100 percent to set the light. Setting the value to 0 allows a light to be turned completely off when no occupant is present or there is an abundance of ambient light. Typically this should either be zero for a private office, or a value around 25 for an open office.</p>	<br />' +
			'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Max light level when on (0-100)(%) -- the maximum level between 0 and 100 percent to set the light. This is the value to be reduced when there is too much illumination by the light. Typically the value would be 80, which would be reduced to no more than 25 or 30.</p>	<br />' + 
			'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Ramp-up time (0-10)(sec) -- the length of time to change a light that is off to its computed on value. For example, when the value is 10, a light that is off will be brought up to the computed light level over 10 seconds when it is turned on.</p><br />' +
			'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Active motion window (1-200)(min) -- the length of time to hold a light on after no additional motion has been detected. During periods when it is expected that the lights should be on, such as during the day on week days, the value should be a longer length of time, such as 15 minutes. Other times, a short length, such as 3 minutes, should be used. When the occupants under the fixture are very still, the value may be increased to a larger value, such as 30 minutes.</p><br />' +
			'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Motion sensitivity (0-10) -- the sensitivity of continuous motion to recognize that occupancy has occurred. Setting the value to 0 will cause the fixture to behavior as if an occupant is always present, such as an area that needs to be continuously lit at night. The default value of 1 is for a fast reaction. A higher value might be used for fixtures in open offices to ignore passersby in corridors.</p>	<br />' +
			'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Ambient sensitivity (0-10) -- determines if the light will be dimmed due to ambient light. The value 0 means that the light will not be dimmed. The value 10 means that the light is most sensitive to ambient light.</p>	<br />' +	
			'<p>There are two major factors in determining the values for the profiles. In areas where there are people present, then the values should be chosen to minimize light level changes. However, to save the maximum energy, the values should be chosen to minimize the length of time a light is on and the intensity of the light.</p>'
			+ '<br /><p>For open floor areas with cubicles during the times when people are expected to be present, the suggested values are:</p>' +
			'<p class="dialogsmallpad">1.&nbsp;Min light level -- set to 30, or an appropriate value greater then 0</p>' +
			'<p class="dialogsmallpad">2.&nbsp;Max light level -- set initially to 100, but reduce when an occupant of the area requests a lower light level or the area is too bright</p>' +
			'<p class="dialogsmallpad">3.&nbsp;Ramp-up time -- set to 4, to slowly turn on lights</p>' +
			'<p class="dialogsmallpad">4.&nbsp;Active motion window -- set to a relatively long period, such as 15 minutes</p>' +
			'<p class="dialogsmallpad">5.&nbsp;Motion sensitivity -- set to a fast reactive value (such as 1) for lights in rooms, such as offices and conference rooms, and to a slower reactive value (such as 4) for lights that might pick up transient motion, which is not occupancy</p>' +
			'<p class="dialogsmallpad">6.&nbsp;Ambient sensitivity -- set to a medium value such as 5</p><br />' +
			'<p>For times when people are not expected to be present, the suggested values are:</p>' +
			'<p class="dialogsmallpad">1.&nbsp;Min light level -- set to 0 to allow a light to be turned off</p>' +
			'<p class="dialogsmallpad">2.&nbsp;Max light level -- set initially to 60, but adjust if needed</p>' +
			'<p class="dialogsmallpad">3.&nbsp;Ramp-up time -- set to 0, but increase if other lights would be on</p>' +
			'<p class="dialogsmallpad">4.&nbsp;Active motion window -- set to a relatively short period, such as 3 minutes</p>' +
			'<p class="dialogsmallpad">5.&nbsp;Motion sensitivity -- set to the same or slightly lower than used for when people are present</p>' +
			'<p class="dialogsmallpad">6.&nbsp;Ambient sensitivity -- set to a higher value, such as 7</p><br />' +
			'<p>Override Profiles<br/>You can set up to eight Override instances. You can customize the following variables during each instance:</p>' +
			'<p class="dialogsmallpad">1.&nbsp;Minimum Light Level when On: 0-100%</p>' +
			'<p class="dialogsmallpad">2.&nbsp;Maximum Light Level when On: 0-100%</p>' +
			'<p class="dialogsmallpad">3.&nbsp;Ramp-up Time: 0-10 Seconds</p>' +
			'<p class="dialogsmallpad">4.&nbsp;Active Motion Window: 1-200 Minutes</p>' +
			'<p class="dialogsmallpad">5.&nbsp;Motion Sensitivity: 0-10 Level</p>' +
			'<p class="dialogsmallpad">6.&nbsp;Ambient Sensitivity: 0-10 Level</p>'  			
			);
			
			$("#helpDialog").dialog(dialogOptions);
			return false;
		});
		
		$('.helpAdvanced').click(function() {
			$("#helpDialog").html('<p>This screen allows the following low level configuration attributes to be changed:</p> <br />' +
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Drop (5 - 25) (%) -- the percentage that the ambient light level must drop before fixture light level adjustment.</p><br />' +
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Rise (5 - 25) (%) -- the percentage that the ambient light level must rise before fixture light level adjustment.</p><br />' +
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Stable ambient (1 - 200) (sec) -- time in seconds to evaluate changes in ambient light before changing the light level. A typical value is 10. Larger values slow down the reaction to changes in ambient light.</p><br />' +
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Change period time (1 - 200) (min) -- length of time in minutes that must pass before the fixture light level can be changed due to a change in the ambient light level to avoid rapid cycling in fixture light level due to passing clouds, which irritates occupants. A typical value is 10 minutes.</p><br />' +
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Linger light level before off  (0 - 25) (%) -- the light level for the fixture before turning off. When turning a light off due to lack of occupancy, the light fixture will first be set at this level for the length of time specified by field "Linger time before off" and then turned off.</p><br />' +
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Linger time before off (0 - 600) (sec) -- the time in seconds to hold on the fixture before turning off. This field is used with field "Linger light level before off" to specify the behavior before turning off a light due to lack of occupancy.</p><br />' +
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Initial on level (0 - 100)(%) -- The minimum light level when a luminaire is turned on. The value is a property of the combination of the ballast and bulbs of a luminaire. Fluorescent luminaires typically need to turn on with the light level set to at least 50%.</p><br />' +
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Initial on time (1 - 14400)(sec)  -- The time that the light level must be held at or above the initial on level when a luminaire is turned on.</p><br />' +
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Highbay -- Provides additional interpretation to the motion detection (0 = disabled, 1 = enabled) due to height of the sensor, such as above 25 feet.</p><br />' +
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Motion threshold (0-255) -- the threshold between 1 (minimal) and 255 (maximal) for detecting motion. When set to zero, the default value is used. The recommended values are between 1 and 10. This value should be increased if there are false triggers of motion, such as heat from a printer located beneath the fixture, or air from a vent.</p><br />' +
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Holiday override profile -- You can assign override profile on a holiday. </p><br />' +
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;DR response level (0 - 10) -- the sensitivity between 0 (none) and 10 (most) for responding to a DR event. To disable response to a DR event, the value must be set to zero. To most aggressively respond to a DR event (that is to maximally reduce energy consumption), the value must be set to 10.</p><br />' +
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Demand Response Level (Low-High) -- You can assign overrides (up to four) to increasing levels of Demand Response (DR). The following DR levels are available: </p>' +
					'<p class="dialogsmallpad">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&bull;Low</p>' +
					'<p class="dialogsmallpad">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&bull;Moderate</p>' +
					'<p class="dialogsmallpad">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&bull;High</p>' +
					'<p class="dialogsmallpad">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&bull;Special</p><br />' +					
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Enable level (0 -100) (lux) -- the ambient light level in lux below which to turn on envelope lighting. For example, when the ambient light is below this level, the motion sensitivity attribute (set on the Schedule tab) behaves as if the value was set to 1. It is also used in determining whether corridor and envelop lighting behavior should be applied. A typical value is 30.</p><br />' +
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Neighbor level (10 - 250) (lux) -- used to affect corridor and envelop lighting behavior. A typical value is 40. A lower value is conservative, causing neighbors to come on aggressively. A higher value is energy efficient, but may stop the envelope lighting behavior.</p><br />' +
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Envelope on level (0 - 100) (%) -- the light level which to turn on a light during envelope lighting. For corridors, the value should be 100 to light up the entire corridor, or 50 for "just ahead lighting".</p><br />' +
					'<p>The values on this page allow tweaking the behavior for the fixture, and should only be changed with assistance from an Enlighted support engineer.</p><br />'
			);
			$("#helpDialog").dialog(dialogAdvancedOptions);
			return false;
		});
	});
	function changeProfile()
	{
		var profileName = $("#profilename").val();
		var groupId = $("#derivedProfile").val();
		var templateId ='${templateId}';
		reload_dialog(groupId,templateId,profileName);
	}	
</script>
<spring:url value="/profile/updateProfileData.ems" var="updateBasicConfig"/>
<spring:url value="/profile/createNewProfileData.ems" var="createNewProfile"/>
<spring:url value="/profile/realTimeProfilePush.ems" var="pushCurrentProfile"/>

<form:form commandName="profilehandler" id="edit-basic" method="post" action="${updateBasicConfig}" onsubmit="return false;">
<div class="infoDialog" id="helpDialog"></div>

<div class="settingDivTop">

<div id="basicConfigurationTop1" style="border:1px;border-style:solid; vertical-align:middle; border-color:#F2F2F2;padding-left: 10px;padding-right: 10px;padding-top: 5px;padding-bottom: 10px;">
	<div  class="settingDivOuterForTopBox">
		<div class="upperdiv">
			
			<div id="profile_rename_message" style="font-weight: bold; padding: 0px 5px; display: inline;"></div>
						<input type="hidden" id=isdefaultProfile name="isdefaultProfile" value="${group.defaultProfile}"/>
						<input type="hidden" name="templateId" value="${templateId}"/>
						<div class="settingProfileNameFieldLabel">
									<span>Profile Name: </span>
									<div class="settingProfileNameFieldValue">
										<c:if test="${management=='new'}">
											<input id="profilename"  name="profilename"  class='validate[required] text-input' size="20"/>
										</c:if>
										<c:if test="${management=='edit'}">
											<c:choose>
												<c:when test="${group.defaultProfile=='true'}">
													<label id="pfprofilename1"></label>
												</c:when>
												<c:otherwise>
													<input id="profileEditname"  name="profilename"  class='validate[required] text-input' size="20"/>
												</c:otherwise>
											</c:choose>
										</c:if>
										<c:if test="${management=='readonly'}">
											<c:choose>
												<c:when test="${group.defaultProfile=='true' && group.profileNo!=0}">
													<label id="pfprofilename2"><c:out value="${group.name}_Default"/></label>
												</c:when>
												<c:otherwise>
													<label id="pfprofilename3"><c:out value="${group.name}"/></label>
												</c:otherwise>
											</c:choose>
										</c:if>
									</div>
					</div>
					<div class="settingProfileNameFieldLabel">
							<span>Created From: </span>
								<c:if test="${management=='new'}">
									<select class="text" id=derivedProfile name="derivedProfile" onchange="javascript: changeProfile()">
										<c:forEach items="${groups}" var="selgroup">
											<c:choose>
												<c:when test="${typeid==selgroup.id}">
													<option  selected="selected" value="${selgroup.id}">${selgroup.name}</option>
												</c:when>
												<c:otherwise>
													<option  value="${selgroup.id}">${selgroup.name}</option>
												</c:otherwise>
											</c:choose>
										</c:forEach>
									</select>
								</c:if>
								
								<c:if test="${management=='edit' || management=='readonly'}">
									<c:choose>
										<c:when test="${groups.id==1}">
											 <label id="derivedFrom"><c:out value="Default"/></label>
										</c:when>
										<c:when test="${groups.defaultProfile=='true'}">
											 <label id="derivedFrom"><c:out value="${groups.name}_Default"/></label>
										</c:when>
										<c:otherwise>
											 <label id="derivedFrom"><c:out value="${groups.name}"/></label>
										</c:otherwise>
									</c:choose>
								</c:if>
													
							
							<div style="float: right;padding-right: 20px;">
							
								 <c:if test="${management=='new'}">
									 <input type="button" id="newbtn" onclick="submitHeader();"
											value="Save"/>
								 </c:if>
								 <c:if test="${management=='edit'}">
								  <c:if test="${group.defaultProfile=='false'}">
										 <div style="padding-bottom: 10px">						
												<input id="updatebtn" type="button" class="updatebtn" onclick="submitHeader();"
													value="<spring:message code='action.update'/>" />
										</div>
								 </c:if>
								</c:if>
								<c:if test="${management=='readonly' && type=='fixture' && group.defaultProfile!='true'}">
									 <input type="button" id="pushbtn" onclick="pushProfileToSU();" class="disablePushButton"
											value="Push Profile"/>
								 </c:if>
								
							</div>
					</div>
		</div>	
	</div>
</div>
	<div id="basicConfigurationTop2" style="border:1px;border-style:solid; border-color:#F2F2F2;padding-left: 10px;padding-right: 10px;padding-top: 10px;">
	<div class="settingDivOuter">
		<div id="basic_message" style="font-weight: bold; padding: 0px 5px; display: inline;"></div>
		<spring:url value="/themes/default/images/helpquestion.png" var="imgfacilities" />
		<div style="float:right; cursor:pointer"><img class="help" src="${imgfacilities}" alt="Help" title="Help"/></div>
	</div>
	
	<div class="settingDivOuter">
			<input type="hidden" id="weekdays" name="weekdays"/>
			<input type="hidden" name="type" value="${type}"/>
			<input type="hidden" id="typeid" name="typeid" value="${typeid}"/>
			<input type="hidden" id="id" name="id" value="${typeid}"/>
			<form:hidden path="profileConfiguration.id"/>
			<div class="upperdiv">
				<div class="settingInlineFieldsError">
					<span id="invalidTimeRange"></span>
				</div>
				<div class="settingInlineFieldDivTop">
					<div class="settingInlineFieldLabel morning"><span><spring:message code="profile.morning"/></span>:</div>
					<c:if test="${management=='new' || management=='edit'}">
						<div class="settingInlineFieldValue"><form:input class="validate[required,funcCall[checkFormat]] text-input inputbox" path="profileConfiguration.morningTime" id="morningTime" size="10" /></div>
					</c:if>
					<c:if test="${management=='readonly'}">
						<div class="settingInlineFieldValue"><label id="morningTime"><c:out value="${profilehandler.profileConfiguration.morningTime}"/></label></div>
					</c:if>
					
				</div>				
				<div class="settingInlineFieldDivTop">
					<div class="settingInlineFieldLabel day"><span><spring:message code="profile.day"/></span>:</div>
					<c:if test="${management=='new' || management=='edit'}">
						<div class="settingInlineFieldValue"><form:input class="validate[required,funcCall[checkFormat]] text-input inputbox" path="profileConfiguration.dayTime" id="dayTime" size="10" /></div>
					</c:if>
					<c:if test="${management=='readonly'}">
						<div class="settingInlineFieldValue"><label id="dayTime"><c:out value="${profilehandler.profileConfiguration.dayTime}"/></label></div>
					</c:if>
					
				</div>
				<div class="settingInlineFieldDivTop">
					<div class="settingInlineFieldLabel evening"><span><spring:message code="profile.evening"/></span>:</div>
					<c:if test="${management=='new' || management=='edit'}">
						<div class="settingInlineFieldValue"><form:input class="validate[required,funcCall[checkFormat]] text-input inputbox" path="profileConfiguration.eveningTime" id="eveningTime" size="10" /></div>
					</c:if>
					<c:if test="${management=='readonly'}">
						<div class="settingInlineFieldValue"><label id="eveningTime"><c:out value="${profilehandler.profileConfiguration.eveningTime}"/></label></div>
					</c:if>
					
				</div>
				<div class="settingInlineFieldDivTop">
					<div class="settingInlineFieldLabel night"><span><spring:message code="profile.night"/></span>:</div>
					<c:if test="${management=='new' || management=='edit'}">
						<div class="settingInlineFieldValue"><form:input class="validate[required,funcCall[checkFormat]] text-input inputbox" path="profileConfiguration.nightTime" id="nightTime" size="10" /></div>
					</c:if>
					<c:if test="${management=='readonly'}">
						<div class="settingInlineFieldValue"><label id="nightTime"><c:out value="${profilehandler.profileConfiguration.nightTime}"/></label></div>
					</c:if>
				</div>
				
				<script type="text/javascript"> 
					$("#invalidTimeRange").html('<spring:message code="profile.error.invalid.time.range"/>');
					$("#invalidTimeRange").hide();
					
					function ptTimeSelectClosed(i) {
						$(i).closest('form').validationEngine('validateField', i);
					}
					
					$("#morningTime").ptTimeSelect({zIndex: 10000, onClose: ptTimeSelectClosed, onFocusDisplay: false, popupImage: '<img src="${timePicker}" class="timePickerImageStyle" />' });
					$("#dayTime").ptTimeSelect({zIndex: 10000, onClose: ptTimeSelectClosed, onFocusDisplay: false, popupImage: '<img src="${timePicker}" class="timePickerImageStyle" /> '});
					$("#eveningTime").ptTimeSelect({zIndex: 10000, onClose: ptTimeSelectClosed, onFocusDisplay: false, popupImage: '<img src="${timePicker}"class="timePickerImageStyle" />'});
					$("#nightTime").ptTimeSelect({zIndex: 10000, onClose: 	ptTimeSelectClosed, onFocusDisplay: false, popupImage: '<img src="${timePicker}" class="timePickerImageStyle" />'});
				</script>
				
				<table border="0" cellpadding="0" cellspacing="0" style="padding-top:5px">
					<tr>
						<td class="profheader" style="padding:0px 30px 0px 20px"><spring:message code="profile.weekdays"/></td>
						<c:forEach items="${profilehandler.profileConfiguration.weekDays}" var="day">
							<td class="proftbltdlbl"><c:out value="${day.day}"/></td>
							<td class="proftbltd"><input id="${day.day}" type="checkbox" value="weekday" 
							 <c:if test="${day.type == 'weekday'}"> <c:out value='checked=checked' /> </c:if> /></td>						
						</c:forEach>	
					</tr>
				</table>
			</div>
			
			<div class="settingDivInner">				
				<table class="entable rhtinput" style="width: 100%; height: 100%;">
					<thead>
						<tr>
							<th  align="left"><spring:message code="profile.settings.period"/></th>
							<th  align="left"><spring:message code="profile.settings.min.level"/></th>
							<th  align="left"><spring:message code="profile.settings.max.level"/></th>
							<th  align="left"><spring:message code="profile.settings.rampup"/></th>
							<th  align="left"><spring:message code="profile.settings.active.motion.window"/></th>
							<th  align="left"><spring:message code="profile.settings.motion.sensitivity"/></th>
							<th  align="left"><spring:message code="profile.settings.ambient.sensitivity"/></th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td colspan="7" class="profheader">
								<spring:message code="profile.weekday.settings"/>
							</td>
						</tr>					
						<tr>
							<td class="morning cleartd"><spring:message code="profile.morning"/></td>
							<form:hidden path="morningProfile.id"/>
							<form:hidden path="morningProfile.manualOverrideDuration"/>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.morningProfile.id}min" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="morningProfile.minLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.morningProfile.id}max" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="morningProfile.onLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input' path="morningProfile.rampUpTime"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[1],max[200]] text-input' path="morningProfile.motionDetectDuration"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input' path="morningProfile.motionSensitivity"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input' path="morningProfile.ambientSensitivity"/></div></td>
						</tr>
						<tr>
							<td class="day cleartd"><spring:message code="profile.day"/></td>
							<form:hidden path="dayProfile.id"/>
							<form:hidden path="dayProfile.manualOverrideDuration"/>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.dayProfile.id}min" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="dayProfile.minLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.dayProfile.id}max" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="dayProfile.onLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="dayProfile.rampUpTime"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[1],max[200]] text-input'  path="dayProfile.motionDetectDuration"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="dayProfile.motionSensitivity"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="dayProfile.ambientSensitivity"/></div></td>
						</tr>
						<tr>
							<td class="evening cleartd"><spring:message code="profile.evening"/></td>
							<form:hidden path="eveningProfile.id"/>
							<form:hidden path="eveningProfile.manualOverrideDuration"/>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.eveningProfile.id}min" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="eveningProfile.minLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.eveningProfile.id}max" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="eveningProfile.onLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="eveningProfile.rampUpTime"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[1],max[200]] text-input'  path="eveningProfile.motionDetectDuration"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="eveningProfile.motionSensitivity"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="eveningProfile.ambientSensitivity"/></div></td>
						</tr>
						<tr>
							<td class="night cleartd"><spring:message code="profile.night"/></td>
							<form:hidden path="nightProfile.id"/>
							<form:hidden path="nightProfile.manualOverrideDuration"/>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.nightProfile.id}min" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="nightProfile.minLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.nightProfile.id}max" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="nightProfile.onLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="nightProfile.rampUpTime"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[1],max[200]] text-input'  path="nightProfile.motionDetectDuration"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="nightProfile.motionSensitivity"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="nightProfile.ambientSensitivity"/></div></td>
						</tr>					
	 					<tr>
							<td colspan="7" class="profheader cleartd">
								<spring:message code="profile.weekend.settings"/>
							</td>
						</tr>
						<tr>
							<td class="morning cleartd"><spring:message code="profile.morning"/></td>
							<form:hidden path="morningProfileWeekEnd.id"/>
							<form:hidden path="morningProfileWeekEnd.manualOverrideDuration"/>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.morningProfileWeekEnd.id}min" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="morningProfileWeekEnd.minLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.morningProfileWeekEnd.id}max" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="morningProfileWeekEnd.onLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="morningProfileWeekEnd.rampUpTime"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[1],max[200]] text-input'  path="morningProfileWeekEnd.motionDetectDuration"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="morningProfileWeekEnd.motionSensitivity"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="morningProfileWeekEnd.ambientSensitivity"/></div></td>
						</tr>
						<tr>
							<td class="day cleartd"><spring:message code="profile.day"/></td>
							<form:hidden path="dayProfileWeekEnd.id"/>
							<form:hidden path="dayProfileWeekEnd.manualOverrideDuration"/>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.dayProfileWeekEnd.id}min" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="dayProfileWeekEnd.minLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.dayProfileWeekEnd.id}max" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="dayProfileWeekEnd.onLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="dayProfileWeekEnd.rampUpTime"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[1],max[200]] text-input'  path="dayProfileWeekEnd.motionDetectDuration"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="dayProfileWeekEnd.motionSensitivity"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="dayProfileWeekEnd.ambientSensitivity"/></div></td>
						</tr>
						<tr>
							<td class="evening cleartd"><spring:message code="profile.evening"/></td>
							<form:hidden path="eveningProfileWeekEnd.id"/>
							<form:hidden path="eveningProfileWeekEnd.manualOverrideDuration"/>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.eveningProfileWeekEnd.id}min" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="eveningProfileWeekEnd.minLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.eveningProfileWeekEnd.id}max" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="eveningProfileWeekEnd.onLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="eveningProfileWeekEnd.rampUpTime"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[1],max[200]] text-input'  path="eveningProfileWeekEnd.motionDetectDuration"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="eveningProfileWeekEnd.motionSensitivity"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="eveningProfileWeekEnd.ambientSensitivity"/></div></td>
						</tr>
						<tr>
							<td class="night cleartd"><spring:message code="profile.night"/></td>
							<form:hidden path="nightProfileWeekEnd.id"/>
							<form:hidden path="nightProfileWeekEnd.manualOverrideDuration"/>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.nightProfileWeekEnd.id}min" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="nightProfileWeekEnd.minLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.nightProfileWeekEnd.id}max" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="nightProfileWeekEnd.onLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="nightProfileWeekEnd.rampUpTime"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[1],max[200]] text-input'  path="nightProfileWeekEnd.motionDetectDuration"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="nightProfileWeekEnd.motionSensitivity"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input' path="nightProfileWeekEnd.ambientSensitivity"/></div></td>
						</tr>
					</tbody>
				</table>
			</div>
	</div>
	</div>
	
	<div id="basicConfigurationTop3" style="border:1px;border-style:solid; border-color:#F2F2F2;padding-left: 10px;padding-right: 10px;padding-top: 10px;">
	<div class="settingDivOuter">
			<div class="settingDivInner">				
				<table class="entable rhtinput" style="width: 100%; height: 100%;">
					<thead>
						<tr>
							<th  align="left"><spring:message code="profile.settings.overrideprofiles"/></th>
							<th  align="left"><spring:message code="profile.settings.min.level"/></th>
							<th  align="left"><spring:message code="profile.settings.max.level"/></th>
							<th  align="left"><spring:message code="profile.settings.rampup"/></th>
							<th  align="left"><spring:message code="profile.settings.active.motion.window"/></th>
							<th  align="left"><spring:message code="profile.settings.motion.sensitivity"/></th>
							<th  align="left"><spring:message code="profile.settings.ambient.sensitivity"/></th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td class="morning cleartd"><spring:message code="profile.overide1"/></td>
							<form:hidden path="morningProfileHoliday.id"/>
							<form:hidden path="morningProfileHoliday.manualOverrideDuration"/>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.morningProfileHoliday.id}min" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="morningProfileHoliday.minLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.morningProfileHoliday.id}max" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="morningProfileHoliday.onLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input' path="morningProfileHoliday.rampUpTime"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[1],max[200]] text-input' path="morningProfileHoliday.motionDetectDuration"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input' path="morningProfileHoliday.motionSensitivity"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input' path="morningProfileHoliday.ambientSensitivity"/></div></td>
						</tr>
						<tr>
							<td class="day cleartd"><spring:message code="profile.overide2"/></td>
							<form:hidden path="dayProfileHoliday.id"/>
							<form:hidden path="dayProfileHoliday.manualOverrideDuration"/>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.dayProfileHoliday.id}min" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="dayProfileHoliday.minLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.dayProfileHoliday.id}max" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="dayProfileHoliday.onLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="dayProfileHoliday.rampUpTime"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[1],max[200]] text-input'  path="dayProfileHoliday.motionDetectDuration"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="dayProfileHoliday.motionSensitivity"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="dayProfileHoliday.ambientSensitivity"/></div></td>
						</tr>
						<tr>
							<td class="evening cleartd"><spring:message code="profile.overide3"/></td>
							<form:hidden path="eveningProfileHoliday.id"/>
							<form:hidden path="eveningProfileHoliday.manualOverrideDuration"/>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.eveningProfileHoliday.id}min" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="eveningProfileHoliday.minLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.eveningProfileHoliday.id}max" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="eveningProfileHoliday.onLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="eveningProfileHoliday.rampUpTime"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[1],max[200]] text-input'  path="eveningProfileHoliday.motionDetectDuration"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="eveningProfileHoliday.motionSensitivity"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="eveningProfileHoliday.ambientSensitivity"/></div></td>
						</tr>
						<tr>
							<td class="night cleartd"><spring:message code="profile.overide4"/></td>
							<form:hidden path="nightProfileHoliday.id"/>
							<form:hidden path="nightProfileHoliday.manualOverrideDuration"/>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.nightProfileHoliday.id}min" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="nightProfileHoliday.minLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.nightProfileHoliday.id}max" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="nightProfileHoliday.onLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="nightProfileHoliday.rampUpTime"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[1],max[200]] text-input'  path="nightProfileHoliday.motionDetectDuration"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="nightProfileHoliday.motionSensitivity"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="nightProfileHoliday.ambientSensitivity"/></div></td>
						</tr>					
	 					<tr>
							<td colspan="7" class="profheader cleartd">
							</td>
						</tr>
						<tr>
							<td class="morning cleartd"><spring:message code="profile.overide5"/></td>
							<form:hidden path="override5.id"/>
							<form:hidden path="override5.manualOverrideDuration"/>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.override5.id}min" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="override5.minLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.override5.id}max" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="override5.onLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="override5.rampUpTime"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[1],max[200]] text-input'  path="override5.motionDetectDuration"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="override5.motionSensitivity"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="override5.ambientSensitivity"/></div></td>
						</tr>
						<tr>
							<td class="day cleartd"><spring:message code="profile.overide6"/></td>
							<form:hidden path="override6.id"/>
							<form:hidden path="override6.manualOverrideDuration"/>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.override6.id}min" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="override6.minLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.override6.id}max" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="override6.onLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="override6.rampUpTime"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[1],max[200]] text-input'  path="override6.motionDetectDuration"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="override6.motionSensitivity"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="override6.ambientSensitivity"/></div></td>
						</tr>
						<tr>
							<td class="evening cleartd"><spring:message code="profile.overide7"/></td>
							<form:hidden path="override7.id"/>
							<form:hidden path="override7.manualOverrideDuration"/>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.override7.id}min" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="override7.minLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.override7.id}max" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="override7.onLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="override7.rampUpTime"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[1],max[200]] text-input'  path="override7.motionDetectDuration"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="override7.motionSensitivity"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="override7.ambientSensitivity"/></div></td>
						</tr>
						<tr>
							<td class="night cleartd"><spring:message code="profile.overide8"/></td>
							<form:hidden path="override8.id"/>
							<form:hidden path="override8.manualOverrideDuration"/>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.override8.id}min" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="override8.minLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.override8.id}max" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="override8.onLevel"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="override8.rampUpTime"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[1],max[200]] text-input'  path="override8.motionDetectDuration"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input'  path="override8.motionSensitivity"/></div></td>
							<td><div class="settingInlineFieldValue"><form:input size="3" class='validate[required,custom[integer],min[0],max[10]] text-input' path="override8.ambientSensitivity"/></div></td>
						</tr>
					</tbody>
				</table>
			</div>
	</div>
	</div>
	
	
	
	<script type="text/javascript">
	
	function daylightHarvestingOpenDialog() {
		
		if($("#daylightHarvestingInputCheckbox").is(":checked")) {
			$("#daylightHarvestingInput").val(0);
		}else{
			$("#daylightHarvestingInput").val(1);
		}
		var daylightHarvestingVal = $("#daylightHarvestingInput").val();
		if(daylightHarvestingVal == '1'){
	    	$("#daylightHarvestingDialog").html("<p style=\"padding:10px;\">Daylight Harvesting in Override Mode will be <strong><span style=\"color:#FF0000\">OFF</span></strong>.<br/><br/> Please confirm by clicking &#39;Yes&#39;&nbsp;if you do not want to do the Daylight Harvesting in Override Mode</p>");
		}else{
			$("#daylightHarvestingDialog").html("<p style=\"padding:10px;\">You have selected the Daylight Harvesting in Override Mode to be <strong><span style=\"color:#00FF00\">ON</span></strong>.<br/><br/> Please confirm by clicking &#39;Yes&#39;&nbsp;</p>");
		}
	    // Define the Dialog and its properties.
	    $("#daylightHarvestingDialog").dialog({
	        resizable: false,
	        modal: true,
	        title: "Confirm",
	        height: 200,
	        width: 450,
	        buttons: {
	            "Yes": function () {
	                $(this).dialog('close');
	                prevdaylightHarvestingVal = daylightHarvestingVal;
	                callbackdaylightHarvesting(true,daylightHarvestingVal);
	            },
	                "No": function () {
	                $(this).dialog('close');
	                callbackdaylightHarvesting(false,prevdaylightHarvestingVal);
	            }
	        }
	    });
	}
	var prevdaylightHarvestingVal = 0;
	function daylightHarvestingOpenDialogPrevVal() {
		prevdaylightHarvestingVal = $("#daylightHarvestingInput").val();
	}


	function callbackdaylightHarvesting(flag, value) {
	    if (flag) {
	    	setdaylightHarvestingCheckedVal(value);
	    } else{
	    	setdaylightHarvestingCheckedVal(prevdaylightHarvestingVal);
	    }
	}
	
	function setdaylightHarvestingCheckedVal(val){
		if(val == 0){
			$('#daylightHarvestingInputCheckbox').prop('checked', true);
		}else{
			$('#daylightHarvestingInputCheckbox').prop('checked', false);
		}
		$('#daylightHarvestingInput').val(val);
	}

	function setGenericCheckBoxValue(item,val){
		if(val == 0){
			$(item).prop('checked', false);
		}else{
			$(item).prop('checked', true);
		}
		$(item).val(val);
	}
	function changeGenericCheckBoxVal(target, valueField){
		//alert(this.value);
		if($(target).is(":checked")) {
			$(valueField).val(1);
		}else{
			$(valueField).val(0);
		}
	}

	$(document).ready(function(){
		//$('#daylightHarvestingInput').click(daylightHarvestingOpenDialogPrevVal);
		$('#daylightHarvestingInputCheckbox').change(daylightHarvestingOpenDialog);
		daylightHarvestingOpenDialogPrevVal();
		setdaylightHarvestingCheckedVal(prevdaylightHarvestingVal);
		
		$("#daylightProfileBelowMinCheckBox").change(function(event){changeGenericCheckBoxVal(event.target,"#daylightProfileBelowMin");});
		setGenericCheckBoxValue("#daylightProfileBelowMinCheckBox",$("#daylightProfileBelowMin").val());
		$("#daylightForceProfileMinValueCheckBox").change(function(event){changeGenericCheckBoxVal(event.target,"#daylightForceProfileMinValue");});
		setGenericCheckBoxValue("#daylightForceProfileMinValueCheckBox",$("#daylightForceProfileMinValue").val());
		
		$("#profileaccordion").accordion({
			autoHeight: false,
			collapsible: true,
			active: false,
			change: function(event, ui) {
						//resetAdvanced();
					}
			});
	});
	</script>
	
	<div id="profileaccordion" class="fitwholepage">
		<h2><a href="#"><spring:message code="profile.advanced"/></a></h2>
		<div>
			<div class="settingDivOuter">
				<div style="padding: 10px 10px 0px 10px;">
					
						<div id="advanced_message" style="font-weight: bold; padding: 0px 5px; display: inline;"></div>
						<div style="float: right;">
							<spring:url value="/themes/default/images/helpquestion.png" var="imgfacilities" />
							<div style="float:right; cursor:pointer"><img class="helpAdvanced" src="${imgfacilities}" alt="Help" title="Help"/></div>
						</div>
<%-- 						<form:hidden id="profileid" path="id"/> --%>
						<div class="settingAccordionFormDiv">							
							<fieldset>
								<legend><span class="settingAccordionFormDivHeader"><spring:message code='profile.advanced.behavior.general'/></span></legend>								
								<div class="settingAccordionFormField">
									<span><spring:message code='profile.advanced.general.1.1'/></span>
									<div class="settingAccordionFormInputField"> <form:input  class='validate[required,custom[integer],min[5],max[25]] text-input'  path="dropPercent" size="3"/></div>
									<span><spring:message code='profile.advanced.general.1.2'/></span>
									<div class="settingAccordionFormInputField"><form:input  class='validate[required,custom[integer],min[5],max[25]] text-input'   path="risePercent" size="3"/></div>
									<span><spring:message code='profile.advanced.general.1.3'/></span>
								</div>
								<div class="settingAccordionFormField">
									<span><spring:message code='profile.advanced.general.2.1'/></span>
									<div class="settingAccordionFormInputField"><form:input   class='validate[required,custom[integer],min[1],max[200]] text-input'   path="intensityNormTime" size="3"/></div>
									<span><spring:message code='profile.advanced.general.2.2'/></span>
								</div>
								<div class="settingAccordionFormField">
									<span><spring:message code='profile.advanced.general.3.1'/></span>
									<div class="settingAccordionFormInputField"><form:input  class='validate[required,custom[integer],min[1],max[200]] text-input'  path="dimBackoffTime" size="3"/></div>
									<span><spring:message code='profile.advanced.general.3.2'/></span>
								</div>
								<div class="settingAccordionFormField">
									<span><spring:message code='profile.advanced.general.4.1'/></span>
									<div class="settingAccordionFormInputField"><form:input   class='validate[required,custom[integer],min[0],max[60]] text-input' path="minLevelBeforeOff" size="3"/></div>
									<span><spring:message code='profile.advanced.general.4.2'/></span>
									<div class="settingAccordionFormInputField"><form:input   class='validate[required,custom[integer],min[0],max[9999]] text-input' path="toOffLinger" size="3"/></div>
									<span><spring:message code='profile.advanced.general.4.3'/></span>
								</div>
								<div class="settingAccordionFormField">
									<span><spring:message code='profile.advanced.general.5.1'/></span>
									<div class="settingAccordionFormInputField"><form:input   class='validate[required,custom[integer],min[1],max[100]] text-input' path="initialOnLevel" size="3"/></div>
									<span><spring:message code='profile.advanced.general.5.2'/></span>
									<div class="settingAccordionFormInputField"><form:input  class='validate[required,custom[integer],min[1],max[14400]] text-input'  path="initialOnTime" size="3"/></div>
									<span><spring:message code='profile.advanced.general.5.3'/></span>
								</div>
								<div class="settingAccordionFormField">
									<span><spring:message code='profile.advanced.general.6.1'/></span>
									<div class="settingAccordionFormInputField"><form:input   class='validate[required,custom[integer],min[0],max[1]] text-input' path="isHighBay" size="3"/></div>
								</div>
								<div class="settingAccordionFormField">
									<span><spring:message code='profile.advanced.general.6.2'/></span>
									<div class="settingAccordionFormInputField"><form:input   class='validate[required,custom[integer]] text-input' path="motionThresholdGain" size="3"/></div>
								</div>
								<div class="settingAccordionFormField">
									<span title="Level 0 mean Daylight harvesting is always on and Level 1 means Daylight harvesting is off"><spring:message code='profile.advanced.general.7.1'/></span>
									<div class="settingAccordionFormInputField"><input  id="daylightHarvestingInputCheckbox" type="checkbox" /></div>
									<div class="settingAccordionFormInputField"><form:input  id="daylightHarvestingInput" type="hidden" path="daylightHarvesting" /></div>
									<div id="daylightHarvestingDialog"></div>
								
								</div>
								<div class="settingAccordionFormField">
									<span title="Level 1 mean Allow Daylight Harvesting to turn off light below min value "><spring:message code='profile.advanced.general.7.2'/></span>
									<div class="settingAccordionFormInputField"><input  id="daylightProfileBelowMinCheckBox" type="checkbox" /></div>
									<div class="settingAccordionFormInputField"><form:input  id="daylightProfileBelowMin" type="hidden" path="daylightProfileBelowMin" /></div>
								</div>
								<div class="settingAccordionFormField">
									<span title="Level 1 mean Force profile min value "><spring:message code='profile.advanced.general.7.3'/></span>
									<div class="settingAccordionFormInputField"><input  id="daylightForceProfileMinValueCheckBox" type="checkbox" /></div>
									<div class="settingAccordionFormInputField"><form:input  id="daylightForceProfileMinValue" type="hidden" path="daylightForceProfileMinValue" /></div>
								</div>
								<div class="settingAccordionFormField">
									<span title="Override profile during holidays"><spring:message code='profile.advanced.general.8.1'/></span>
									<div class="settingAccordionFormInputField">
										<form:select path="holidayLevel" id="holiday_combo">
													<%
														int i = 0;
													%>
													<c:forEach items="${override2list}" var="element">
														<form:option value="<%=i%>" label='${element}' />
														<%
															if (i == 0) {
																i = 5;
															}
															else {
																i++;		
															}
														%>
													</c:forEach>
										</form:select>
									</div>
								</div>
							</fieldset>
						</div>
						
						<div class="settingAccordionFormDiv">
							<fieldset>
								<legend><span class="settingAccordionFormDivHeader"><spring:message code='profile.advanced.behavior.dr'/></span></legend>
								<legend><span class="settingAccordionFormDivHeader"><spring:message code='profile.advanced.behavior.dr.sub'/></span></legend>								
								<div class="settingAccordionFormField">									
									<span><spring:message code='profile.advanced.dr.1'/></span>
									<div class="settingAccordionFormInputField"><form:input   class='validate[required,custom[integer],min[0],max[10]] text-input'  path="drReactivity" size="2"/></div>
									<span><spring:message code='profile.advanced.dr.2'/></span>
								</div>									
								
								<legend><span class="settingAccordionFormDivHeader"><spring:message code='profile.advanced.behavior.adr'/></span></legend>								
								<div class="settingAccordionFormField">
								<table id="tableadr" class="entable" style="width: 100%; height: 100%;">
								<thead>
								<tr class="editableRow">
								<th  align="left">DR Level</th>
								<th  align="left">Select Override Profile</th>
								</tr>
								</thead>
								<tr class="editableRow">								
									<td><span><spring:message code='profile.advanced.behavior.adr.low'/></span></td>
																
											<td>											
											<form:select path="drLowLevel" id="drlow_combo">											
													<%
														int i = 0;
													%>
													<c:forEach items="${overridelist}" var="element">

														<form:option value="<%=i%>" label='${element}' />
														<%
															i++;
														%>
													</c:forEach>
													</form:select></td>
										</tr>				
								<tr class="editableRow">	
									<td><span><spring:message code='profile.advanced.behavior.adr.moderate'/></span></td>
									<td><form:select path="drModerateLevel" id="drmoderate_combo">
													<%
														int i = 0;
													%>
													<c:forEach items="${overridelist}" var="element">

														<form:option value="<%=i%>" label='${element}'/>
														<%
															i++;
														%>
													</c:forEach>
													</form:select>
  									</td>
									</tr>
								<tr class="editableRow">
									<td><span><spring:message code='profile.advanced.behavior.adr.high'/></span></td>
									<td><form:select path="drHighLevel" id="drhigh_combo">
													<%
														int i = 0;
													%>
													<c:forEach items="${overridelist}" var="element">

														<form:option value="<%=i%>" label='${element}' />
														<%
															i++;
														%>
													</c:forEach>
													</form:select>
  									</td>
									</tr>
								<tr class="editableRow">
									<td><span><spring:message code='profile.advanced.behavior.adr.special'/></span></td>
									<td><form:select path="drSpecialLevel" id="drspecial_combo">
													<%
														int i = 0;
													%>
													<c:forEach items="${overridelist}" var="element">

														<form:option value="<%=i%>" label='${element}' />
														<%
															i++;
														%>
													</c:forEach>
													</form:select>
  									</td>
  									</tr>									
								</table>
								</div>
							</fieldset>							
						</div>				
						
						<div class="settingAccordionFormDiv">
							<fieldset>
								<legend><span class="settingAccordionFormDivHeader"><spring:message code='profile.advanced.behavior.envelope'/></span></legend>
								<div class="settingAccordionFormField">
									<span><spring:message code='profile.advanced.envelope.1.1'/></span>
									<div class="settingAccordionFormInputField"><form:input   class='validate[required,custom[integer],min[0],max[100]] text-input' path="darkLux" size="3"/></div>
									<span><spring:message code='profile.advanced.envelope.1.2'/></span>
									<div class="settingAccordionFormInputField"><form:input  class='validate[required,custom[integer],min[10],max[250]] text-input'  path="neighborLux" size="3"/></div>
									<span><spring:message code='profile.advanced.envelope.1.3'/></span>
								</div>
								<div class="settingAccordionFormField">
									<span><spring:message code='profile.advanced.envelope.2.1'/></span>
									<div class="settingAccordionFormInputField"><form:input  class='validate[required,custom[integer],min[0],max[100]] text-input'  path="envelopeOnLevel" size="2"/></div>
									<span><spring:message code='profile.advanced.envelope.2.2'/></span>
								</div>
							</fieldset>
						</div>						
				</div>
			</div>
		</div>
	</div>
</div>
</form:form>

<script type="text/javascript">
	
	function pushProfileToSU()
	{
		$.post(
				"${pushCurrentProfile}",
				$("#edit-basic").serialize(),
				function(data){
					var response = eval("("+data+")");
					if(response.success==1){ //Success
						$('#typeid').val(response.typeid);
						displayBasicMessage(response.message, "green");
					} else { //Failure
						displayBasicMessage(response.message, "red");
					}
				}
			);
	}
	function submitHeader() {
		
		var isValid = $("#edit-basic").validationEngine('validate');
		var manage = manageType;
		
		//Check for valid profile name - special characters not allowed. Only Letter, digit, space and underscore allowed
		var isValidProfileName = validateProfileName();
    	if(isValidProfileName==false)
		{
			return;
		}
    		    	
	    if(submit==false && manageType=='new')
    	{
	    	//Check for Duplicate Profile Name in NEW profile Scenario
	    	var isProfileDuplicate = checkForDuplicateProfile(); 
	    	if(isProfileDuplicate)
			{
				return;
			}
	    	//Once save is done, chnage the save button to update button for further updates
	    	$("#newbtn").attr('value','Update');
	    	manage = "new";
	    	submit =true;
    	}else
   		{
    		//Check for Duplicate excluding current profile name and checking for other profile name
    		var newName="";
    		
    		if(manageType=='new')
			{
    			newName = $("#profilename").val();
			}else
			{
				newName = $("#profileEditname").val();
			}
    		//console.log(newName + " --- " + OriprofileName);
    		var isProfileDuplicate=false; 
    		if(newName!=OriprofileName)
    		{
    			isProfileDuplicate = checkForDuplicateProfile();
    		}
    		if(isProfileDuplicate)
			{
				return;
			}
    		manage = "edit";
   		}
	    var customerId = "${customerId}";
	    //Form is valid and ready to update
		if(isValid==true)
		{
			var url;
			if(manage=='edit')
			{
				url = "${updateBasicConfig}";
			}
			else
			{
				url = "${createNewProfile}?customerId="+customerId;
			}
			displayBasicMessage("", "green");
			$("#invalidTimeRange").hide();
			
			var isAdvanceValid = $("#edit-advanced").validationEngine('validate');
			var isAdvanceValid = true;
			if(isValid && isAdvanceValid) {
				var result = validateTime();
				if (result == "E") {
					$("#invalidTimeRange").show();
				}
				else {
					var object = new Object();
					object.day="monday";
					object.value="true";
					weekdays[0] = $("#Monday").is(':checked');
					weekdays[1] = $("#Tuesday").is(':checked');
					weekdays[2] = $("#Wednesday").is(':checked');
					weekdays[3] = $("#Thursday").is(':checked');
					weekdays[4] = $("#Friday").is(':checked');
					weekdays[5] = $("#Saturday").is(':checked');
					weekdays[6] = $("#Sunday").is(':checked');
					$('#weekdays').val(weekdays);
					$.post(
							url,
							$("#edit-basic").serialize(),
							function(data){
								var response = eval("("+data+")");
								if(response.success==1){ //Success
									if(manageType=='new')
									{
										OriprofileName = $("#profilename").val();
									}else
									{
										OriprofileName = $("#profileEditname").val();
									}
									//console.log("OriprofileName " + OriprofileName);
									$('#id').val(response.groupId);
									$('#typeid').val(response.groupId);
									displayBasicMessage(response.message, "green");
									parent.parent.refreshProfileTree();
								} else { //Failure
									displayBasicMessage(response.message, "red");
									$("#newbtn").attr('value','New');
							    	submit =false;
								}
								$.ptTimeSelect.closeCntr();
							}
						);
				}
			}
		}
		
		$.ptTimeSelect.closeCntr();
		$('#profilename').focus();
	}
	
	function checkForDuplicateProfile()
	{
		var chkProfileName="";
		if(manageType=='new')
		{
			chkProfileName = $("#profilename").val();
		}else
		{
			chkProfileName = $("#profileEditname").val();
		}
		var index = chkProfileName.search(/Default/i);
		if(index>0)
		{
			var arr = chkProfileName.split("_", 1);
			chkProfileName = arr[0];
		}
		var returnresult = true;	
		$.ajax({
			type: "GET",
			cache: false,
			url: '<spring:url value="/services/org/profile/duplicatecheck/"/>'+ chkProfileName,
			dataType: "text",
			async: false,
			success: function(msg) {			
				var count = (msg).indexOf(chkProfileName);	
				if(count > 0) {
					returnresult = true;
				}
				else {
					returnresult = false;
				}
			},
			error: function (jqXHR, textStatus, errorThrown){			
				returnresult = false;
			}
		});
		if(returnresult){
			
			displayBasicMessage('<spring:message code="error.duplicate.profile"/>', "red");
			$("#profilename").addClass("invalidField");
			return true;
		}	
		else {	
			displayBasicMessage("", "black");
			$("#profilename").removeClass("invalidField");
			return false;
		}
	}
	
	function displayProfileRenameMessage(Message, Color) {
		$("#profile_rename_message").html(Message);
		$("#profile_rename_message").css("color", Color);
	}
	
	function displayBasicMessage(Message, Color) {
		$("#basic_message").html(Message);
		$("#basic_message").css("color", Color);
	}
	
	function displayAdvancedMessage(Message, Color) {
		$("#advanced_message").html(Message);
		$("#advanced_message").css("color", Color);
	}
	
	function checkFormat(field) {
		var time = field.val();
		var invalidFormat = '<spring:message code="error.valid.time.format"/>';
		
        var timeReg = /^\d{1,2}:\d\d (AM|PM)$/i;
        if(timeReg.test(time) == false) {
            return invalidFormat;
        }
		var timearr = time.split(" ");
		var timearr1 = timearr[0].split(":");
		if((timearr1[0] < 0) || (timearr1[0] >  12)){
			return invalidFormat;
		}
		if((timearr1[1] < 0)|| (timearr1[1] >  59)){
			return invalidFormat;
		}
	}
	
	function validateLevel(field) {
		var fieldarr = $(field).attr('id').split('m');
		if(fieldarr[1] == "in") {
			var min = parseInt(field.val());
			var max = parseInt($('#'+fieldarr[0] + "max").val());
			if(min > max) {
				return '<spring:message code="error.valid.min.greater.than.max"/>';
			}
		}
		else {
			var max = parseInt(field.val());
			var min = parseInt($('#'+fieldarr[0] + "min").val());
			if(max < min) {
				return '<spring:message code="error.valid.max.less.than.min"/>';
			}
		}
		
		
	}
	
	function validateDRLevel(field) {
		var fieldarr = $(field).attr('id').split('m');
		var minDR = "${minDR}";
		var maxDR = "${maxDR}";		
		if(fieldarr[1] == "in") {
			var min = parseInt(field.val());
			var max = parseInt($('#'+fieldarr[0] + "max").val());
			if(min > minDR)
				{
				return '<spring:message code="error.valid.mindr"/>';
				}
			if(min > max) {
				return '<spring:message code="error.valid.min.greater.than.max"/>';
			}
		}
		else {
			var max = parseInt(field.val());
			var min = parseInt($('#'+fieldarr[0] + "min").val());
			if(max > maxDR)
				{
				return '<spring:message code="error.valid.maxdr"/>';
				}
			if(max < min) {
				return '<spring:message code="error.valid.max.less.than.min"/>';
			}
		}
		
		
	}
	function validateProfileName()
	{
		var isValidProfileName = true;
		var name ="";
		if(manageType=='new')
		{
			name = $("#profilename").val();
		}else
		{
			name = $("#profileEditname").val();
		}
		
		var invalidFormatStr = 'Profile name must contain only letters, numbers, or underscore';
        var regExpStr = /^[a-z0-9\_\s]+$/i;
        if(regExpStr.test(name) == false) {
        	isValidProfileName= false;
        }
		if(isValidProfileName==false){
			displayBasicMessage(invalidFormatStr, "red");
			$("#profilename").addClass("invalidField");
		}	
		else {	
			displayBasicMessage("", "black");
			$("#profilename").removeClass("invalidField");
		}
		return isValidProfileName;
	}
	function validateTime() {
		
		var m = $("#morningTime").val();
		var d = $("#dayTime").val();
		var e = $("#eveningTime").val();
		var n = $("#nightTime").val();
		m = m.split(/ /);
		d = d.split(/ /);
		e = e.split(/ /);
		n = n.split(/ /);
		mt = m[0].split(/:/);
		dt = d[0].split(/:/);
		et = e[0].split(/:/);
		nt = n[0].split(/:/);
		if(m[1] == 'PM' && mt[0] != '12') {
			mt[0] = parseInt(mt[0]) + 12;
		}
		m = parseInt(mt[0] +  mt[1]);
		
		if(e[1] == 'PM' && et[0] != '12') {
			et[0] = parseInt(et[0]) + 12;
		}
		e = parseInt(et[0] +  et[1]);
		
		if(n[1] == 'PM' && nt[0] != '12') {
			nt[0] = parseInt(nt[0]) + 12;
		}
		n = parseInt(nt[0] +  nt[1]);
		
		if(d[1] == 'PM' && dt[0] != '12') {
			dt[0] = parseInt(dt[0]) + 12;
		}
		d = parseInt(dt[0] +  dt[1]);
		
		if((m < d && d < e && e < n) || (d < e && e < n && n < m) || (e < n && n < m && m < d) || (n < m && m < d && d < e)) {
			return "S";
		}
		else {
			return "E";
		}
	}
</script>