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
<spring:url value="/plugloadProfile/realTimePlugloadProfilePush.ems" var="pushCurrentPlugloadProfile"/>

<style type="text/css">


	html, body{margin:3px 3px 0px 3px !important; padding:0px !important; background: #ffffff;}	
	.entable tr input[type="text"] { border: none !important;}
	
	.settingProfileNameFieldLabel {padding-left: 10px; display:inline;padding-right: 10px;}
	.settingProfileNameFieldLabel span {font-weight: bold}
	.settingProfileNameFieldValue {position : relative ; display: inline;}
	.settingDivOuterForTopBox { clear: both; padding-bottom: 5px; }
	.settingProfileNameSelectValue {position : relative ; display: inline;width: 100%;clear: both;}
	
	.disablePlugloadProfilePushButton
	{
		background: #CCCCCC !important;
		border-color :#CCCCCC !important;
		color:#FFFFFF; font-size:13px; clear: both; float: left; margin: -3px; width:150px;height:35px;border:0px;font-weight:bold;
	}
	.enablePlugloadProfilePushButton
	{
		color:#FFFFFF;background-color:#5a5a5a; font-size:13px; clear: both; float: left; margin: -3px; width:150px;height:35px;border:0px;font-weight:bold;
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
var OriplugloadprofileName="";
	$(document).ready(function() {		
		manageType = "${management}";
		OriplugloadprofileName = "${plugloadgroup.name}";
		if(manageType=='new')
		{
			var oldProfileName = "${oldProfileName}";
			var profileName = "${plugloadgroup.name}";
			if(oldProfileName!="")
			{
				$("#profilename").val(oldProfileName);
			}else
			{
				$("#profilename").val("");
			}
			$('#edit-plugload-profile-basic input').removeAttr("readonly");
			var deactivator = function(event){ event.preventDefault(); }; 
			$(':checkbox').unbind('click',deactivator); 
		}
		else if(manageType=='edit')
		{
			 $("#derivedProfile").attr("disabled","disabled");
			 var profileName = "${plugloadgroup.name}";
			if("${plugloadgroup.defaultProfile}"=='true')
			{
				if(profileName!="Default"){
					$("#pfprofilename1").text(profileName+"_Default");
					$('#edit-plugload-profile-basic input').attr('readonly', 'readonly'); 
					var deactivator = function(event){ event.preventDefault(); }; 
					$(':checkbox').click(deactivator); 
				}
			}else{
				$("#profileEditname").val(profileName);
			}
		}else if(manageType=='readonly')
		{
			if("${plugloadgroup.defaultProfile}"=='true')			
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
			$('#edit-plugload-profile-basic input').attr('readonly', 'readonly');
			$('#edit-plugload-profile-basic select').attr('disabled', true);
			var deactivator = function(event){ event.preventDefault(); }; 
			$(':checkbox').click(deactivator); 
			var pushPendingFlag = "${pushPendingStatus}";
			$('#plugloadprofilepushbtn').attr("disabled",true);
			if(pushPendingFlag=='true')
			{
				$('#plugloadprofilepushbtn').removeAttr("disabled");
				$('#plugloadprofilepushbtn').removeClass("disablePlugloadProfilePushButton");	
				$('#plugloadprofilepushbtn').addClass("enablePlugloadProfilePushButton");	
			}
		}
		
		$("#edit-plugload-profile-basic").validationEngine('attach' , {

			isOverflown: true,

			overflownDIV: "#settingDivTop"

		});
		
		var dialogOptions = {
			title : "<spring:message code='plugloadprofile.help.title'/>",
			modal : true,
			height : 500,
			width : 700,
			draggable : true,
			close : function(ev, ui){
				$(this).dialog("destroy");
			}
		};
		
		var dialogAdvancedOptions = {
				title : "<spring:message code='plugloadprofile.advancedhelp.title'/>",
				modal : true,
				height : 350,
				width : 700,
				draggable : true,
				close : function(ev, ui){
					$(this).dialog("destroy");
				}
			};
		
		$("#plugloadprofileaccordion").accordion({
			autoHeight: false,
			collapsible: true,
			active: false,
			change: function(event, ui) {
						//resetAdvanced();
			}
		 });
		$('.help').click(function() {
			$("#plhelpDialog").html('<p>This screen shows the schedule-based configuration of a plugload. A plugload\'s profile can be configured for four periods per day for each type of day, which are weekdays and weekends. The start time for each period must be specified. A period begins at the specified start time and ends at the beginning of the start time of the next period, with "Day" following "Morning", "Evening" following "Day", "Night" following "Evening", and "Morning" following "Night". The "Weekdays" must be selected. Any day not selected is a weekend day.</p>' + 
			'<br /><p>The columns in the table are:</p><br />' + 
			'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Period -- the period of the day that the settings apply. Typically, morning starts at 6am; followed by day, which starts at 9am; followed by evening, which starts at 6pm; followed by night, which starts at 9pm; and ends at the next morning.</p><br />' +
			'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Active motion window (1-200)(min) -- The length of time to continue supplying power to the controlled outlet even after no additional motion has been detected. During periods when it is expected that the controlled outlet should be powered, such as during normal organization operation hours, the value should be a longer length of time, such as 15 minutes or more. During other times, a short length, such as 3 minutes, should be used.</p><br />' +
			'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Controlled Outlet -- There are four modes of operation for the controlled outlet. The first two are applicable for timer based controls such as fans or furniture mounted task lighting and the other two are applicable for occupancy based settings.</p>' +
			
			'<p class="dialogsmallpad">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1.&nbsp;Always on  -- The controlled outlet is powered during the period</p>' +
			'<p class="dialogsmallpad">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;2.&nbsp;Always off  -- The controlled outlet not powered during the period</p>' +
			'<p class="dialogsmallpad">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;3.&nbsp;On when occupied  -- The controlled outlet is powered when there is occupancy</p>' +
			'<p class="dialogsmallpad">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;4.&nbsp;On when vacant  -- The controlled outlet is powered, when there is no occupancy (vacancy)</p><br />' +
						
			'<p>Override Profiles<br/>You can set up to eight Override instances. You can customize the following variables during each instance:</p>' +
			'<p class="dialogsmallpad">1.&nbsp;Active motion window : 1-200 minutes</p>' +
			'<p class="dialogsmallpad">2.&nbsp;Controlled Outlet : Always on, Always off, On when occupied, On when vacant</p><br />'
			);
			
			$("#plhelpDialog").dialog(dialogOptions);
			return false;
		});
		
		$('.helpAdvanced').click(function() {
			$("#plhelpDialog").html('<p>This screen allows the following advanced level configuration attributes to be changed:</p> <br />' +
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Holiday override profile -- You assign either no override, or the override profile to be used during a holiday override event.<br /><br />' +
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Demand Response Level (Low-High)  -- You assign either no override, or the override profile to be used for each DR level (which are Low, Moderate, High, and Special).<br /><br />' +
					'<p class="dialogbigpad">&bull;&nbsp;&nbsp;&nbsp;Safety Settings -- A plugload generally operates as a part of a motion group. When it is not a part of a motion group, or when the plugload does not receive communication from the other members of the motion group, it operates in safety mode. The behavior of the plugload when operating in safety mode can be configured.<br />'+
					'<p class="dialogbigpad">The attributes that can be set include:</p>' +
					'<p class="dialogsmallpad">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&bull;&nbsp;&nbsp;&nbsp;Number of attempts (1-10) - The maximum number of attempts to use in obtaining occupancy status from motion group members before transitioning to safety mode</p>' +
					'<p class="dialogsmallpad">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&bull;&nbsp;&nbsp;&nbsp;The time interval (1 - 600) (in seconds) to wait for a response from motion group members before initiating another attempt</p>' +
					'<p class="dialogsmallpad">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&bull;&nbsp;&nbsp;&nbsp;Whether the controlled outlet will be On or Off during safety mode</p>'
			);
			$("#plhelpDialog").dialog(dialogAdvancedOptions);
			return false;
		});
	
	});
	
	
	function changePlugloadProfile()
	{
		var plugloadProfileName = $("#profilename").val();
		var plugloadGroupId = $("#derivedProfile").val();
		var plugloadTemplateId ='${templateId}';
		reload_plugload_profile_dialog(plugloadGroupId,plugloadTemplateId,plugloadProfileName);
	}
	
	function pushPlugloadProfileToPlugload()
	{
		$.post(
				"${pushCurrentPlugloadProfile}",
				$("#edit-plugload-profile-basic").serialize(),
				function(data){
					var response = eval("("+data+")");
					if(response.success==1){ //Success
						$('#typeid').val(response.typeid);
						displayPlugloadprofileBasicMessage(response.message, "green");
					} else { //Failure
						displayPlugloadprofileBasicMessage(response.message, "red");
					}
				}
			);
	}
		
</script>
<spring:url value="/plugloadProfile/updatePlugloadProfileData.ems" var="updatePlugloadProfileBasicConfig"/>
<spring:url value="/plugloadProfile/createNewPlugloadProfileData.ems" var="createNewPlugloadProfile"/>

<form:form commandName="profilehandler" id="edit-plugload-profile-basic" method="post" action="${updatePlugloadProfileBasicConfig}" onsubmit="return false;">
<div class="infoDialog" id="plhelpDialog"></div>

<div class="settingDivTop">

<div id="basicConfigurationTop1" style="border:1px;border-style:solid; vertical-align:middle; border-color:#F2F2F2;padding-left: 10px;padding-right: 10px;padding-top: 5px;padding-bottom: 10px;">
	<div  class="settingDivOuterForTopBox">
		<div class="upperdiv">
			
					<input type="hidden" name="templateId" value="${templateId}"/>
						<div class="settingProfileNameFieldLabel">
									<span>Plugload Profile Name: </span>
									<div class="settingProfileNameFieldValue">
										<c:if test="${management=='new'}">
											<input id="profilename"  name="profilename"  class='validate[required] text-input' size="20"/>
										</c:if>
										<c:if test="${management=='edit'}">
											<c:choose>
												<c:when test="${plugloadgroup.defaultProfile=='true'}">
													<label id="pfprofilename1"></label>
												</c:when>
												<c:otherwise>
													<input id="profileEditname"  name="profilename"  class='validate[required] text-input' size="20"/>
												</c:otherwise>
											</c:choose>
										</c:if>
										<c:if test="${management=='readonly'}">
											<c:choose>
												<c:when test="${plugloadgroup.defaultProfile=='true' && plugloadgroup.profileNo!=0}">
													<!-- <label id="pfprofilename2"><c:out value="${plugloadgroup.name}_Default"/></label> -->
													<label id="pfprofilename2"><c:out value="${plugloadgroup.name}"/></label>
												</c:when>
												<c:otherwise>
													<label id="pfprofilename3"><c:out value="${plugloadgroup.name}"/></label>
												</c:otherwise>
											</c:choose>
										</c:if>
									</div>
					</div>
					<div class="settingProfileNameFieldLabel">
							<span>Created From: </span>
								<c:if test="${management=='new'}">
									<select id="derivedProfile" name="derivedProfile" onchange="javascript: changePlugloadProfile()">
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
											 <!-- <label id="derivedFrom"><c:out value="${groups.name}_Default"/></label> -->
											 <label id="derivedFrom"><c:out value="${groups.name}"/></label>
										</c:when>
										<c:otherwise>
											 <label id="derivedFrom"><c:out value="${groups.name}"/></label>
										</c:otherwise>
									</c:choose>
								</c:if>
								<div id="basic_plugload_profile_message" style="font-weight: bold; padding: 0px 5px; display: inline;"></div>
									<div style="float: right;padding-right: 20px;">
									
									 <div style="padding-bottom: 10px">			
								     
								     <c:if test="${management=='new'}">
								 	
									 	<input id="newbtn" type="button" class="updatebtn" onclick="submitPlugloadProfileHeader();"
													value="Save" />
									 </c:if>		
									 
									 <c:if test="${management=='edit'}">
									 	 <c:if test="${plugloadgroup.defaultProfile=='false'}">
										 	<div style="padding-bottom: 10px">						
												<input id="updatePlugloadProfilebtn" type="button" class="updatebtn" onclick="submitPlugloadProfileHeader();"
													value="<spring:message code='action.update'/>" />
											</div>
										 </c:if>
									 </c:if>
									 
									 <c:if test="${management=='readonly' && type=='plugload' && plugloadgroup.defaultProfile!='true'}">
									 <input type="button" id="plugloadprofilepushbtn" onclick="pushPlugloadProfileToPlugload();" class="disablePlugloadProfilePushButton"
											value="Push Plugload Profile"/>
									 </c:if>
									
									</div>
								 
							</div>
					</div>
		</div>	
	</div>
</div>
	<div id="basicConfigurationTop2" style="border:1px;border-style:solid; border-color:#F2F2F2;padding-left: 10px;padding-right: 10px;padding-top: 10px;">
	<div class="settingDivOuter">
		<spring:url value="/themes/default/images/helpquestion.png" var="imgfacilities" />
		<div style="float:right; cursor:pointer"><img class="help" src="${imgfacilities}" alt="Help" title="Help"/></div>
	</div>
	
	<div class="settingDivOuter">
			<input type="hidden" id="weekdays" name="weekdays"/>
			<input type="hidden" name="type" value="${type}"/>
			<input type="hidden" id="typeid" name="typeid" value="${typeid}"/>
			<input type="hidden" id="id" name="id" value="${typeid}"/>
			<form:hidden path="plugloadProfileConfiguration.id"/>
			<div class="upperdiv">
				<div class="settingInlineFieldsError">
					<span id="invalidTimeRange"></span>
				</div>
				
				<div class="settingInlineFieldDivTop">
				<div class="settingInlineFieldLabel morning"><span><spring:message code="profile.morning"/></span>:</div>
				<c:if test="${management=='new' || management=='edit'}">
					<div class="settingInlineFieldValue"><form:input class="validate[required,funcCall[checkFormat]] text-input inputbox" path="plugloadProfileConfiguration.morningTime" id="morningTime" size="10" /></div>
				</c:if>
				<c:if test="${management=='readonly'}">
					<div class="settingInlineFieldValue"><label id="morningTime"><c:out value="${profilehandler.plugloadProfileConfiguration.morningTime}"/></label></div>
				</c:if>
				
				</div>				
				<div class="settingInlineFieldDivTop">
					<div class="settingInlineFieldLabel day"><span><spring:message code="profile.day"/></span>:</div>
					<c:if test="${management=='new' || management=='edit'}">
						<div class="settingInlineFieldValue"><form:input class="validate[required,funcCall[checkFormat]] text-input inputbox" path="plugloadProfileConfiguration.dayTime" id="dayTime" size="10" /></div>
					</c:if>
					<c:if test="${management=='readonly'}">
						<div class="settingInlineFieldValue"><label id="dayTime"><c:out value="${profilehandler.plugloadProfileConfiguration.dayTime}"/></label></div>
					</c:if>
					
				</div>
				<div class="settingInlineFieldDivTop">
					<div class="settingInlineFieldLabel evening"><span><spring:message code="profile.evening"/></span>:</div>
					<c:if test="${management=='new' || management=='edit'}">
						<div class="settingInlineFieldValue"><form:input class="validate[required,funcCall[checkFormat]] text-input inputbox" path="plugloadProfileConfiguration.eveningTime" id="eveningTime" size="10" /></div>
					</c:if>
					<c:if test="${management=='readonly'}">
						<div class="settingInlineFieldValue"><label id="eveningTime"><c:out value="${profilehandler.plugloadProfileConfiguration.eveningTime}"/></label></div>
					</c:if>
					
				</div>
				<div class="settingInlineFieldDivTop">
					<div class="settingInlineFieldLabel night"><span><spring:message code="profile.night"/></span>:</div>
					<c:if test="${management=='new' || management=='edit'}">
						<div class="settingInlineFieldValue"><form:input class="validate[required,funcCall[checkFormat]] text-input inputbox" path="plugloadProfileConfiguration.nightTime" id="nightTime" size="10" /></div>
					</c:if>
					<c:if test="${management=='readonly'}">
						<div class="settingInlineFieldValue"><label id="nightTime"><c:out value="${profilehandler.plugloadProfileConfiguration.nightTime}"/></label></div>
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
						<c:forEach items="${profilehandler.plugloadProfileConfiguration.weekDays}" var="day">
							<td class="proftbltdlbl"><c:out value="${day.day}"/></td>
							<td class="proftbltd"><input id="${day.day}" type="checkbox" value="weekday" 
							 <c:if test="${day.type == 'weekday'}"> <c:out value='checked=checked' /> </c:if> /></td>						
						</c:forEach>	
					</tr>
				</table>
			</div>
			
			<div class="settingDivInner">				
				<table class="entable rhtinput" style="width: 100%; height: 100%;" border="0">
					<thead>
						<tr>
							<th  align="left"><spring:message code="profile.settings.period"/></th>
							<th  align="center" >Active motion window</th>
							<th  align="center">Controlled Outlet</th>
							</tr>
					</thead>
					<tbody>
						<tr>
							<td colspan="3" class="profheader">
								<spring:message code="profile.weekday.settings"/>
							</td>
						</tr>					
						<tr>
							<td class="morning cleartd"><spring:message code="profile.morning"/></td>
							<form:hidden path="morningProfile.id"/>
							<form:hidden path="morningProfile.manualOverrideTime"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.morningProfile.id}" class='validate[required,custom[integer],min[1],max[200],funcCall[validateLevel]] text-input' path="morningProfile.activeMotion"/></div></td>
							
							<td align="right" style="padding-right:10px">
								<form:select class="inputField" path="morningProfile.mode">
									<form:option value="0" label="Always on" />
									<form:option value="2" label="Always off" />
									<form:option value="1" label="On when occupied" />
									<form:option value="3" label="On when vacant" />
								</form:select>
							</td>
							
							</tr>
						<tr>
							<td class="day cleartd" ><spring:message code="profile.day"/></td>
							<form:hidden path="dayProfile.id"/>
							<form:hidden path="dayProfile.manualOverrideTime"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.dayProfile.id}" class='validate[required,custom[integer],min[1],max[200],funcCall[validateLevel]] text-input' path="dayProfile.activeMotion"/></div></td>
							
							<td align="right" style="padding-right:10px">
								<form:select class="inputField" path="dayProfile.mode">
									<form:option value="0" label="Always on" />
									<form:option value="2" label="Always off" />
									<form:option value="1" label="On when occupied" />
									<form:option value="3" label="On when vacant" />
								</form:select>
							</td>
							
							</tr>
						<tr>
							<td class="evening cleartd" ><spring:message code="profile.evening"/></td>
							<form:hidden path="eveningProfile.id"/>
							<form:hidden path="eveningProfile.manualOverrideTime"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.eveningProfile.id}" class='validate[required,custom[integer],min[1],max[200],funcCall[validateLevel]] text-input' path="eveningProfile.activeMotion"/></div></td>
							
							<td align="right" style="padding-right:10px">
								<form:select class="inputField" path="eveningProfile.mode">
									<form:option value="0" label="Always on" />
									<form:option value="2" label="Always off" />
									<form:option value="1" label="On when occupied" />
									<form:option value="3" label="On when vacant" />
								</form:select>
							</td>
							
							</tr>
						<tr>
							<td class="night cleartd"><spring:message code="profile.night"/></td>
							<form:hidden path="nightProfile.id"/>
							<form:hidden path="nightProfile.manualOverrideTime"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="1" id="${profilehandler.nightProfile.id}" class='validate[required,custom[integer],min[1],max[200],funcCall[validateLevel]] text-input' path="nightProfile.activeMotion"/></div></td>
							
							<td align="right" style="padding-right:10px">
								<form:select class="inputField" path="nightProfile.mode">
									<form:option value="0" label="Always on" />
									<form:option value="2" label="Always off" />
									<form:option value="1" label="On when occupied" />
									<form:option value="3" label="On when vacant" />
								</form:select>
							</td>
							
							</tr>			
							
								
						<tr>
							<td colspan="3" class="profheader">
								<spring:message code="profile.weekend.settings"/>
							</td>
						</tr>				
						<tr>
							<td class="morning cleartd"><spring:message code="profile.morning"/></td>
							<form:hidden path="morningProfileWeekEnd.id"/>
							<form:hidden path="morningProfileWeekEnd.manualOverrideTime"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.morningProfileWeekEnd.id}" class='validate[required,custom[integer],min[1],max[200],funcCall[validateLevel]] text-input' path="morningProfileWeekEnd.activeMotion"/></div></td>
							
							<td align="right" style="padding-right:10px">
								<form:select class="inputField" path="morningProfileWeekEnd.mode">
									<form:option value="0" label="Always on" />
									<form:option value="2" label="Always off" />
									<form:option value="1" label="On when occupied" />
									<form:option value="3" label="On when vacant" />
								</form:select>
							</td>
							
							</tr>
						<tr>
							<td class="day cleartd" ><spring:message code="profile.day"/></td>
							<form:hidden path="dayProfileWeekEnd.id"/>
							<form:hidden path="dayProfileWeekEnd.manualOverrideTime"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.dayProfileWeekEnd.id}" class='validate[required,custom[integer],min[1],max[200],funcCall[validateLevel]] text-input' path="dayProfileWeekEnd.activeMotion"/></div></td>
							
							<td align="right" style="padding-right:10px">
								<form:select class="inputField" path="dayProfileWeekEnd.mode">
									<form:option value="0" label="Always on" />
									<form:option value="2" label="Always off" />
									<form:option value="1" label="On when occupied" />
									<form:option value="3" label="On when vacant" />
								</form:select>
							</td>
							
							</tr>
						<tr>
							<td class="evening cleartd" ><spring:message code="profile.evening"/></td>
							<form:hidden path="eveningProfileWeekEnd.id"/>
							<form:hidden path="eveningProfileWeekEnd.manualOverrideTime"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.eveningProfileWeekEnd.id}" class='validate[required,custom[integer],min[1],max[200],funcCall[validateLevel]] text-input' path="eveningProfileWeekEnd.activeMotion"/></div></td>
							
							<td align="right" style="padding-right:10px">
								<form:select class="inputField" path="eveningProfileWeekEnd.mode">
									<form:option value="0" label="Always on" />
									<form:option value="2" label="Always off" />
									<form:option value="1" label="On when occupied" />
									<form:option value="3" label="On when vacant" />
								</form:select>
							</td>
							
							</tr>
						<tr>
							<td class="night cleartd"><spring:message code="profile.night"/></td>
							<form:hidden path="nightProfileWeekEnd.id"/>
							<form:hidden path="nightProfileWeekEnd.manualOverrideTime"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="1" id="${profilehandler.nightProfileWeekEnd.id}" class='validate[required,custom[integer],min[1],max[200],funcCall[validateLevel]] text-input' path="nightProfileWeekEnd.activeMotion"/></div></td>
							
							<td align="right" style="padding-right:10px">
								<form:select class="inputField" path="nightProfileWeekEnd.mode">
									<form:option value="0" label="Always on" />
									<form:option value="2" label="Always off" />
									<form:option value="1" label="On when occupied" />
									<form:option value="3" label="On when vacant" />
								</form:select>
							</td>
							
							</tr>			
						
					</tbody>
				</table>
				
				
			</div>
			
	</div>
	</div>
	<div id="basicConfigurationTop3" style="border:1px;border-style:solid; border-color:#F2F2F2;padding-left: 10px;padding-right: 10px;padding-top: 10px;">
	<div class="settingDivOuter">
			<div class="settingDivInner">	
			<table class="entable rhtinput" style="width: 100%; height: 100%;" border="0">
					<thead>
						<tr>
							<th  align="left"><spring:message code="profile.settings.period"/></th>
							<th  align="center">Active motion window</th>
							<th  align="center">Controlled Outlet</th>
							</tr>
					</thead>
					<tbody>
						<tr>
							<td colspan="3" class="profheader">
								<spring:message code="profile.override.settings"/>
							</td>
						</tr>					
						<tr>
							<td class="morning cleartd"><spring:message code="profile.overide1"/></td>
							<form:hidden path="morningProfileHoliday.id"/>
							<form:hidden path="morningProfileHoliday.manualOverrideTime"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.morningProfileHoliday.id}" class='validate[required,custom[integer],min[1],max[200],funcCall[validateLevel]] text-input' path="morningProfileHoliday.activeMotion"/></div></td>
							
							<td align="right" style="padding-right:10px">
								<form:select class="inputField" path="morningProfileHoliday.mode">
									<form:option value="0" label="Always on" />
									<form:option value="2" label="Always off" />
									<form:option value="1" label="On when occupied" />
									<form:option value="3" label="On when vacant" />
								</form:select>
							</td>
							
							</tr>
						<tr>
							<td class="day cleartd" ><spring:message code="profile.overide2"/></td>
							<form:hidden path="dayProfileHoliday.id"/>
							<form:hidden path="dayProfileHoliday.manualOverrideTime"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.dayProfileHoliday.id}" class='validate[required,custom[integer],min[1],max[200],funcCall[validateLevel]] text-input' path="dayProfileHoliday.activeMotion"/></div></td>
							
							<td align="right" style="padding-right:10px">
								<form:select class="inputField" path="dayProfileHoliday.mode">
									<form:option value="0" label="Always on" />
									<form:option value="2" label="Always off" />
									<form:option value="1" label="On when occupied" />
									<form:option value="3" label="On when vacant" />
								</form:select>
							</td>
							
							</tr>
						<tr>
							<td class="evening cleartd" ><spring:message code="profile.overide3"/></td>
							<form:hidden path="eveningProfileHoliday.id"/>
							<form:hidden path="eveningProfileHoliday.manualOverrideTime"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.eveningProfileHoliday.id}" class='validate[required,custom[integer],min[1],max[200],funcCall[validateLevel]] text-input' path="eveningProfileHoliday.activeMotion"/></div></td>
							
							<td align="right" style="padding-right:10px">
								<form:select class="inputField" path="eveningProfileHoliday.mode">
									<form:option value="0" label="Always on" />
									<form:option value="2" label="Always off" />
									<form:option value="1" label="On when occupied" />
									<form:option value="3" label="On when vacant" />
								</form:select>
							</td>
							
							</tr>
						<tr>
							<td class="night cleartd"><spring:message code="profile.overide4"/></td>
							<form:hidden path="nightProfileHoliday.id"/>
							<form:hidden path="nightProfileHoliday.manualOverrideTime"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="1" id="${profilehandler.nightProfileHoliday.id}" class='validate[required,custom[integer],min[1],max[200],funcCall[validateLevel]] text-input' path="nightProfileHoliday.activeMotion"/></div></td>
							
							<td align="right" style="padding-right:10px">
								<form:select class="inputField" path="nightProfileHoliday.mode">
									<form:option value="0" label="Always on" />
									<form:option value="2" label="Always off" />
									<form:option value="1" label="On when occupied" />
									<form:option value="3" label="On when vacant" />
								</form:select>
							</td>
							
						</tr>
						
						<tr>
							<td colspan="3" class="profheader cleartd">
							</td>
						</tr>
						
						<tr>
							<td class="morning cleartd"><spring:message code="profile.overide5"/></td>
							<form:hidden path="override5.id"/>
							<form:hidden path="override5.manualOverrideTime"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.override5.id}" class='validate[required,custom[integer],min[1],max[200],funcCall[validateLevel]] text-input' path="override5.activeMotion"/></div></td>
							
							<td align="right" style="padding-right:10px">
								<form:select class="inputField" path="override5.mode">
									<form:option value="0" label="Always on" />
									<form:option value="2" label="Always off" />
									<form:option value="1" label="On when occupied" />
									<form:option value="3" label="On when vacant" />
								</form:select>
							</td>
						
						</tr>
						<tr>
							<td class="day cleartd" ><spring:message code="profile.overide6"/></td>
							<form:hidden path="override6.id"/>
							<form:hidden path="override6.manualOverrideTime"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.override6.id}" class='validate[required,custom[integer],min[1],max[200],funcCall[validateLevel]] text-input' path="override6.activeMotion"/></div></td>
							
							<td align="right" style="padding-right:10px">
								<form:select class="inputField" path="override6.mode">
									<form:option value="0" label="Always on" />
									<form:option value="2" label="Always off" />
									<form:option value="1" label="On when occupied" />
									<form:option value="3" label="On when vacant" />
								</form:select>
							</td>
						
						</tr>
						<tr>
							<td class="evening cleartd" ><spring:message code="profile.overide7"/></td>
							<form:hidden path="override7.id"/>
							<form:hidden path="override7.manualOverrideTime"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.override7.id}" class='validate[required,custom[integer],min[1],max[200],funcCall[validateLevel]] text-input' path="override7.activeMotion"/></div></td>
							
							<td align="right" style="padding-right:10px">
								<form:select class="inputField" path="override7.mode">
									<form:option value="0" label="Always on" />
									<form:option value="2" label="Always off" />
									<form:option value="1" label="On when occupied" />
									<form:option value="3" label="On when vacant" />
								</form:select>
							</td>
							
							</tr>
						<tr>
							<td class="night cleartd"><spring:message code="profile.overide8"/></td>
							<form:hidden path="override8.id"/>
							<form:hidden path="override8.manualOverrideTime"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="1" id="${profilehandler.override8.id}" class='validate[required,custom[integer],min[1],max[200],funcCall[validateLevel]] text-input' path="override8.activeMotion"/></div></td>
							
							<td align="right" style="padding-right:10px">
								<form:select class="inputField" path="override8.mode">
									<form:option value="0" label="Always on" />
									<form:option value="2" label="Always off" />
									<form:option value="1" label="On when occupied" />
									<form:option value="3" label="On when vacant" />
								</form:select>
							</td>
							
						</tr>
								
						
					</tbody>
				</table>
			</div>
			</div>
			</div>
	
	<script type="text/javascript">
	$(document).ready(function(){
		$("#plugloadprofileaccordion").accordion({
			autoHeight: false,
			collapsible: true,
			active: false,
			change: function(event, ui) {
						//resetAdvanced();
					}
			});
	});
	</script>
	
	<div id="plugloadprofileaccordion" class="fitwholepage">
		<h2><a href="#"><spring:message code="profile.advanced"/></a></h2>
		<div>
			<div class="settingDivOuter">
				<div style="padding: 10px 10px 0px 10px;">
					
						<div id="advanced_message" style="font-weight: bold; padding: 0px 5px; display: inline;"></div>
						<div style="float: right;">
							<spring:url value="/themes/default/images/helpquestion.png" var="imgfacilities" />
							<div style="float:right; cursor:pointer"><img class="helpAdvanced" src="${imgfacilities}" alt="Help" title="Help"/></div>
						</div>
						<form:hidden id="profileid" path="id"/>
						<form:hidden path="initialOnLevel"/>
						<form:hidden path="initialOnTime"/>
						<div class="settingAccordionFormDiv">							
							<fieldset>
								<legend><span class="settingAccordionFormDivHeader">General</span></legend>								
								<div class="settingAccordionFormField">
									<span title="Override Plugload profile during holidays"><spring:message code='plugloadprofile.advanced.general.8.1'/></span>
									<div class="settingAccordionFormInputField">
										<form:select path="holidayLevel" id="holiday_combo">
													<%
														int plhc = 0;
													%>
													<c:forEach items="${override2list}" var="element">
														<form:option value="<%=plhc%>" label='${element}' />
														<%
															if (plhc == 0) {
																plhc = 5;
															}
															else {
																plhc++;		
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

														<form:option value="<%=i%>" label='${element}'/>
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
								<legend><span class="settingAccordionFormDivHeader">Safety Settings</span></legend>
								<div class="settingAccordionFormField" >
									<span>Enter Safety Operation after</span>
									<div class="settingAccordionFormInputField"><form:input   class='validate[required,custom[integer],min[1],max[10],funcCall[validateLevel]] text-input' path="noOfMissedHeartbeats" size="3"/></div>									
									<span>unsuccessful attempts at obtaining occupancy status from motion group members,</span>
								</div>
								<div class="settingAccordionFormField" >
									<span>waiting up to</span>
									<div class="settingAccordionFormInputField"><form:input   class='validate[required,custom[integer],min[1],max[600],funcCall[validateLevel]] text-input' path="heartbeatInterval" size="3"/></div>									
									<span>seconds in each attempt for a response.</span>
								</div>
								<div class="settingAccordionFormField" >
									<span>In Safety Operation, set the controlled outlet</span>
									<div class="settingAccordionFormInputField">
									<form:radiobutton path="safetyMode" label="On" value="0"/>
									<form:radiobutton path="safetyMode" label="Off" value="1"/>
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
	
function displayPlugloadprofileBasicMessage(Message, Color) {
	
	$("#basic_plugload_profile_message").html(Message);
	$("#basic_plugload_profile_message").css("color", Color);
}


function submitPlugloadProfileHeader() {

	displayPlugloadprofileBasicMessage("", "black");
	
	var isValid = $("#edit-plugload-profile-basic").validationEngine('validate');
	var manage = manageType;
	
	//Check for valid plugload profile name - special characters not allowed. Only Letter, digit, space and underscore allowed
	var isvalidPlugloadProfileName = validatePlugloadProfileName();
	if(isvalidPlugloadProfileName == false)
	{
		return;
	}
	
	
	if(submit==false && manageType=='new')
	{
		$("#newbtn").attr("disabled", true);
		//Check for Duplicate Plugload Profile Name in NEW plugload profile Scenario
    	var isPlugloadProfileDuplicate = checkForDuplicatePlugloadProfile(); 
    	if(isPlugloadProfileDuplicate)
		{
    		$('#newbtn').removeAttr("disabled");
    		$("#plugloadProfileFormDialog").scrollTop(0);
			return;
		}
    	//Once save is done, chnage the save button to update button for further updates
    	//$("#newbtn").attr('value','Update');
    	manage = "new";
    	submit =true;
	}else
		{
		//Check for Duplicate excluding current plugload profile name and checking for other plugload profile name
		var newName="";
		
		if(manageType=='new')
		{
			newName = $("#profilename").val();
		}else
		{
			newName = $("#profileEditname").val();
		}
		//console.log(newName + " --- " + OriplugloadprofileName);
		var isPlugloadProfileDuplicate=false; 
		if(newName!=OriplugloadprofileName)
		{
			isPlugloadProfileDuplicate = checkForDuplicatePlugloadProfile();
		}
		if(isPlugloadProfileDuplicate)
		{
			$("#plugloadProfileFormDialog").scrollTop(0);
			return;
		}
		manage = "edit";
		}
    
    //Form is valid and ready to update
	if(isValid==true)
	{
		var url;
		if(manage=='edit')
		{
			url = "${updatePlugloadProfileBasicConfig}";
		}
		else
		{
			//$("#newbtn").attr('disabled',true);
			url = "${createNewPlugloadProfile}";
		}
		displayPlugloadprofileBasicMessage("", "green");
		$("#invalidTimeRange").hide();
		
		//var isAdvanceValid = $("#edit-advanced").validationEngine('validate');
		var isAdvanceValid = true;
		if(isValid && isAdvanceValid) {
			var result = validatePlugloadProfileTime();
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
						$("#edit-plugload-profile-basic").serialize(),
						function(data){
							var response = eval("("+data+")");
							if(response.success == 1){ //Success
								if(manageType == 'new')
								{
									OriplugloadprofileName = $("#profilename").val();
									$('#newbtn').removeAttr("disabled");
									$("#newbtn").attr('value','Update');
								}else
								{
									
									OriplugloadprofileName = $("#profileEditname").val();
								}
								
								$('#id').val(response.groupId);
				                $('#typeid').val(response.groupId);
								//alert(response.message);
								displayPlugloadprofileBasicMessage(response.message, "green");
								if($.cookie('em_plugload_profile_nodetype_selected') == "plugloadgroup"){
									parent.parent.refreshPlugloadProfileTree();
								}
							} else { // Failure
								displayPlugloadprofileBasicMessage(response.message, "red");
								if(manageType=='new'){
									$('#newbtn').removeAttr("disabled");
									$("#newbtn").attr('value','New');
								}
								submit =false;
							}
							$.ptTimeSelect.closeCntr();
				});
				
			}
		}
	}
	
	
	$.ptTimeSelect.closeCntr();
	//$('#profilename').focus();
	$("#plugloadProfileFormDialog").scrollTop(0);
}

function checkForDuplicatePlugloadProfile()
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
		url: '<spring:url value="/services/org/plugloadProfile/duplicatecheck/"/>'+ chkProfileName,
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
		
		displayPlugloadprofileBasicMessage('<spring:message code="error.duplicate.plugloadProfile"/>', "red");
		$("#profilename").addClass("invalidField");
		return true;
	}	
	else {	
		displayPlugloadprofileBasicMessage("", "black");
		$("#profilename").removeClass("invalidField");
		return false;
	}
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

function validatePlugloadProfileName()
{
	var isvalidPlugloadProfileName = true;
	var name ="";
	if(manageType=='new')
	{
		name = $("#profilename").val();
	}else
	{
		name = $("#profileEditname").val();
	}
	
	var invalidFormatStr = 'Plugload Profile name must contain only letters, numbers, or underscore';
    var regExpStr = /^[a-z0-9\_\s]+$/i;
    if(regExpStr.test(name) == false) {
    	isvalidPlugloadProfileName= false;
    }
	if(isvalidPlugloadProfileName==false){
		displayPlugloadprofileBasicMessage(invalidFormatStr, "red");
		$("#profilename").addClass("invalidField");
	}	
	else {	
		displayPlugloadprofileBasicMessage("", "black");
		$("#profilename").removeClass("invalidField");
	}
	return isvalidPlugloadProfileName;
}

function validatePlugloadProfileTime() {
	
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