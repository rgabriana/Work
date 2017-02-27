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
var OriprofileName="";
	$(document).ready(function() {		
		manageType = "${management}";
		OriprofileName = "${plugloadgroup.name}";
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
			$('#edit-basic input').removeAttr("readonly");
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
					$('#edit-basic input').attr('readonly', 'readonly'); 
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
			$('#edit-basic input').attr('readonly', 'readonly');
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
		
		$("#plugloadprofileaccordion").accordion({
			autoHeight: false,
			collapsible: true,
			active: false,
			change: function(event, ui) {
						//resetAdvanced();
			}
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
				$("#edit-basic").serialize(),
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
<spring:url value="/plugloadProfile/updatePlugloadProfileData.ems" var="updateBasicConfig"/>
<spring:url value="/plugloadProfile/createNewPlugloadProfileData.ems" var="createNewProfile"/>
<spring:url value="/plugloadProfile/realTimePlugloadProfilePush.ems" var="pushCurrentPlugloadProfile"/>


<form:form commandName="profilehandler" id="edit-basic" method="post" action="${updateBasicConfig}" onsubmit="return false;">
<div class="infoDialog" id="helpDialog"></div>

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
								 	
									 	<input id="savebtn" type="button" class="updatebtn" onclick="submitHeader();"
													value="Save" />
									 </c:if>		
									 
									 <c:if test="${management=='edit'}">
									 	 <c:if test="${plugloadgroup.defaultProfile=='false'}">
										 	<div style="padding-bottom: 10px">						
												<input id="updatePlugloadProfilebtn" type="button" class="updatebtn" onclick="submitHeader();"
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
							<th  align="left" >Active motion window</th>
							<th  align="left">Manual Override Time</th>
							<th  align="center" colspan="4">Mode</th>
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
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.morningProfile.id}" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="morningProfile.activeMotion"/></div></td>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.morningProfile.id}" class='validate[required,custom[integer],min[0],max[1000],funcCall[validateLevel]] text-input' path="morningProfile.manualOverrideTime"/></div></td>
							
							<td><form:radiobutton path="morningProfile.mode" value="0" label="On"/></td>
							<td><form:radiobutton path="morningProfile.mode" value="1"  label="Off"/></td>
							<td><form:radiobutton path="morningProfile.mode"  value="2" label="Occupancy" style='vertical-align: middle; margin: 0px;'/></td>
							<td><form:radiobutton path="morningProfile.mode"  value="3" label="Vacancy" style='vertical-align: middle; margin: 0px;'/></td>
								
						

							</tr>
						<tr>
							<td class="day cleartd" ><spring:message code="profile.day"/></td>
							<form:hidden path="dayProfile.id"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.dayProfile.id}" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="dayProfile.activeMotion"/></div></td>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.dayProfile.id}" class='validate[required,custom[integer],min[0],max[1000],funcCall[validateLevel]] text-input' path="dayProfile.manualOverrideTime"/></div></td>
							
							<td><form:radiobutton path="dayProfile.mode" label="On" value="0"/></td>
							<td><form:radiobutton path="dayProfile.mode" label="Off"  value="1"/></td>
							<td><form:radiobutton path="dayProfile.mode"  value="2" label="Occupancy" style='vertical-align: middle; margin: 0px;'/></td>
							<td><form:radiobutton path="dayProfile.mode"  value="3" label="Vacancy" style='vertical-align: middle; margin: 0px;'/></td>
								
						
							
							</tr>
						<tr>
							<td class="evening cleartd" ><spring:message code="profile.evening"/></td>
							<form:hidden path="eveningProfile.id"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.eveningProfile.id}" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="eveningProfile.activeMotion"/></div></td>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.eveningProfile.id}" class='validate[required,custom[integer],min[0],max[1000],funcCall[validateLevel]] text-input' path="eveningProfile.manualOverrideTime"/></div></td>
							
							<td><form:radiobutton path="eveningProfile.mode" label="On" value="0"/></td>
							<td><form:radiobutton path="eveningProfile.mode" label="Off" value="1"/></td>
							<td><form:radiobutton path="eveningProfile.mode"  value="2" label="Occupancy" style='vertical-align: middle; margin: 0px;'/></td>
							<td><form:radiobutton path="eveningProfile.mode"  value="3" label="Vacancy" style='vertical-align: middle; margin: 0px;'/></td>
								
						
							
							</tr>
						<tr>
							<td class="night cleartd"><spring:message code="profile.night"/></td>
							<form:hidden path="nightProfile.id"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="1" id="${profilehandler.nightProfile.id}" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="nightProfile.activeMotion"/></div></td>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.nightProfile.id}" class='validate[required,custom[integer],min[0],max[1000],funcCall[validateLevel]] text-input' path="nightProfile.manualOverrideTime"/></div></td>
							
							<td><form:radiobutton path="nightProfile.mode" label="On" value="0"/></td>
							<td><form:radiobutton path="nightProfile.mode" label="Off" value="1"/></td>
							<td><form:radiobutton path="nightProfile.mode"  value="2" label="Occupancy" style='vertical-align: middle; margin: 0px;'/></td>
							<td><form:radiobutton path="nightProfile.mode"  value="3" label="Vacancy" style='vertical-align: middle; margin: 0px;'/></td>
							
							</tr>			
							
								
						<tr>
							<td colspan="7" class="profheader">
								<spring:message code="profile.weekend.settings"/>
							</td>
						</tr>				
						<tr>
							<td class="morning cleartd"><spring:message code="profile.morning"/></td>
							<form:hidden path="morningProfileWeekEnd.id"/>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.morningProfileWeekEnd.id}" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="morningProfileWeekEnd.activeMotion"/></div></td>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.morningProfileWeekEnd.id}" class='validate[required,custom[integer],min[0],max[1000],funcCall[validateLevel]] text-input' path="morningProfileWeekEnd.manualOverrideTime"/></div></td>
							
							<td><form:radiobutton path="morningProfileWeekEnd.mode" value="0" label="On"/></td>
							<td><form:radiobutton path="morningProfileWeekEnd.mode" value="1" label="Off"/></td>
							<td><form:radiobutton path="morningProfileWeekEnd.mode"  value="2" label="Occupancy" style='vertical-align: middle; margin: 0px;'/></td>
							<td><form:radiobutton path="morningProfileWeekEnd.mode"  value="3" label="Vacancy" style='vertical-align: middle; margin: 0px;'/></td>
								
						

							</tr>
						<tr>
							<td class="day cleartd" ><spring:message code="profile.day"/></td>
							<form:hidden path="dayProfileWeekEnd.id"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.dayProfileWeekEnd.id}" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="dayProfileWeekEnd.activeMotion"/></div></td>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.dayProfileWeekEnd.id}" class='validate[required,custom[integer],min[0],max[1000],funcCall[validateLevel]] text-input' path="dayProfileWeekEnd.manualOverrideTime"/></div></td>
														
							<td><form:radiobutton path="dayProfileWeekEnd.mode" label="On" value="0"/></td>
							<td><form:radiobutton path="dayProfileWeekEnd.mode" label="Off" value="1"/></td>
							<td><form:radiobutton path="dayProfileWeekEnd.mode"  value="2" label="Occupancy" style='vertical-align: middle; margin: 0px;'/></td>
							<td><form:radiobutton path="dayProfileWeekEnd.mode"  value="3" label="Vacancy" style='vertical-align: middle; margin: 0px;'/></td>
								
						
							
							</tr>
						<tr>
							<td class="evening cleartd" ><spring:message code="profile.evening"/></td>
							<form:hidden path="eveningProfileWeekEnd.id"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.eveningProfileWeekEnd.id}" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="eveningProfileWeekEnd.activeMotion"/></div></td>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.eveningProfileWeekEnd.id}" class='validate[required,custom[integer],min[0],max[1000],funcCall[validateLevel]] text-input' path="eveningProfileWeekEnd.manualOverrideTime"/></div></td>
														
							<td><form:radiobutton path="eveningProfileWeekEnd.mode" label="On" value="0"/></td>
							<td><form:radiobutton path="eveningProfileWeekEnd.mode" label="Off" value="1"/></td>
							<td><form:radiobutton path="eveningProfileWeekEnd.mode"  value="2" label="Occupancy" style='vertical-align: middle; margin: 0px;'/></td>
							<td><form:radiobutton path="eveningProfileWeekEnd.mode"  value="3" label="Vacancy" style='vertical-align: middle; margin: 0px;'/></td>
								
						
							
							</tr>
						<tr>
							<td class="night cleartd"><spring:message code="profile.night"/></td>
							<form:hidden path="nightProfileWeekEnd.id"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="1" id="${profilehandler.nightProfileWeekEnd.id}" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="nightProfileWeekEnd.activeMotion"/></div></td>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.nightProfileWeekEnd.id}" class='validate[required,custom[integer],min[0],max[1000],funcCall[validateLevel]] text-input' path="nightProfileWeekEnd.manualOverrideTime"/></div></td>
														
							<td><form:radiobutton path="nightProfileWeekEnd.mode" label="On" value="0"/></td>
							<td><form:radiobutton path="nightProfileWeekEnd.mode" label="Off" value="1"/></td>
							<td><form:radiobutton path="nightProfileWeekEnd.mode"  value="2" label="Occupancy" style='vertical-align: middle; margin: 0px;'/></td>
							<td><form:radiobutton path="nightProfileWeekEnd.mode"  value="3" label="Vacancy" style='vertical-align: middle; margin: 0px;'/></td>
							
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
							<th  align="left" >Active motion window</th>
							<th  align="left" >Manual Override Time</th>
							<th  align="center" colspan="4">Mode</th>
							</tr>
					</thead>
					<tbody>
						<tr>
							<td colspan="7" class="profheader">
								<spring:message code="profile.override.settings"/>
							</td>
						</tr>					
						<tr>
							<td class="morning cleartd"><spring:message code="profile.overide1"/></td>
							<form:hidden path="morningProfileHoliday.id"/>
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.morningProfileHoliday.id}" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="morningProfileHoliday.activeMotion"/></div></td>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.morningProfileHoliday.id}" class='validate[required,custom[integer],min[0],max[1000],funcCall[validateLevel]] text-input' path="morningProfileHoliday.manualOverrideTime"/></div></td>
														
							<td><form:radiobutton path="morningProfileHoliday.mode" value="0" label="On"/></td>
							<td><form:radiobutton path="morningProfileHoliday.mode" value="1" label="Off" /></td>
							<td><form:radiobutton path="morningProfileHoliday.mode"  value="2" label="Occupancy" style='vertical-align: middle; margin: 0px;'/></td>
							<td><form:radiobutton path="morningProfileHoliday.mode"  value="3" label="Vacancy" style='vertical-align: middle; margin: 0px;'/></td>
								
						

							</tr>
						<tr>
							<td class="day cleartd" ><spring:message code="profile.overide2"/></td>
							<form:hidden path="dayProfileHoliday.id"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.dayProfileHoliday.id}" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="dayProfileHoliday.activeMotion"/></div></td>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.dayProfileHoliday.id}" class='validate[required,custom[integer],min[0],max[1000],funcCall[validateLevel]] text-input' path="dayProfileHoliday.manualOverrideTime"/></div></td>
														
							<td><form:radiobutton path="dayProfileHoliday.mode" label="On" value="0"/></td>
							<td><form:radiobutton path="dayProfileHoliday.mode" label="Off" value="1"/></td>
							<td><form:radiobutton path="dayProfileHoliday.mode"  value="2" label="Occupancy" style='vertical-align: middle; margin: 0px;'/></td>
							<td><form:radiobutton path="dayProfileHoliday.mode"  value="3" label="Vacancy" style='vertical-align: middle; margin: 0px;'/></td>
								
						
							
							</tr>
						<tr>
							<td class="evening cleartd" ><spring:message code="profile.overide3"/></td>
							<form:hidden path="eveningProfileHoliday.id"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.eveningProfileHoliday.id}" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="eveningProfileHoliday.activeMotion"/></div></td>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.eveningProfileHoliday.id}" class='validate[required,custom[integer],min[0],max[1000],funcCall[validateLevel]] text-input' path="eveningProfileHoliday.manualOverrideTime"/></div></td>
														
							<td><form:radiobutton path="eveningProfileHoliday.mode" label="On" value="0"/></td>
							<td><form:radiobutton path="eveningProfileHoliday.mode" label="Off" value="1"/></td>
							<td><form:radiobutton path="eveningProfileHoliday.mode"  value="2" label="Occupancy" style='vertical-align: middle; margin: 0px;'/></td>
							<td><form:radiobutton path="eveningProfileHoliday.mode"  value="3" label="Vacancy" style='vertical-align: middle; margin: 0px;'/></td>
								
						
							
							</tr>
						<tr>
							<td class="night cleartd"><spring:message code="profile.overide4"/></td>
							<form:hidden path="nightProfileHoliday.id"/>
							
							<td><div class="settingInlineFieldValue"><form:input size="1" id="${profilehandler.nightProfileHoliday.id}" class='validate[required,custom[integer],min[0],max[100],funcCall[validateLevel]] text-input' path="nightProfileHoliday.activeMotion"/></div></td>
							
							<td><div class="settingInlineFieldValue"><form:input size="3" id="${profilehandler.nightProfileHoliday.id}" class='validate[required,custom[integer],min[0],max[1000],funcCall[validateLevel]] text-input' path="nightProfileHoliday.manualOverrideTime"/></div></td>
							
							
							<td><form:radiobutton path="nightProfileHoliday.mode" label="On" value="0"/></td>
							<td><form:radiobutton path="nightProfileHoliday.mode" label="Off" value="1"/></td>
							<td><form:radiobutton path="nightProfileHoliday.mode"  value="2" label="Occupancy" style='vertical-align: middle; margin: 0px;'/></td>
							<td><form:radiobutton path="nightProfileHoliday.mode"  value="3" label="Vacancy" style='vertical-align: middle; margin: 0px;'/></td>
							
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
						<div class="settingAccordionFormDiv">							
							<fieldset>
								<legend><span class="settingAccordionFormDivHeader">General</span></legend>								
								<div class="settingAccordionFormField" >
									<span>
									 Device initially turns on at </span>
									<div class="settingAccordionFormInputField"> <form:input  class='validate[required,custom[integer],min[5],max[25]] text-input'  path="initialOnLevel" size="3"/></div>
									<span>% for</span>
									<div class="settingAccordionFormInputField"><form:input  class='validate[required,custom[integer],min[5],max[25]] text-input'   path="initialOnTime" size="3"/></div>
									<span>seconds</span>
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
									<span>Number of missed heartbeats before going to safety mode</span>
									<div class="settingAccordionFormInputField"><form:input   class='validate[required,custom[integer],min[0],max[100]] text-input' path="noOfMissedHeartbeats" size="3"/></div>									
								</div>
								<div class="settingAccordionFormField" >
									<span>State when the communication with SUs in the group is not possible</span>
									<div class="settingAccordionFormInputField">
									<form:radiobutton path="safetyMode" label="On" value="1"/>
									<form:radiobutton path="safetyMode" label="Off" value="0"/>
									</div>
								<div class="settingAccordionFormField" >
									<span>Heartbeat interval to SUs</span>
									<div class="settingAccordionFormInputField"><form:input   class='validate[required,custom[integer],min[0],max[100]] text-input' path="heartbeatInterval" size="3"/></div>									
								</div>	
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


function submitHeader() {
	var url;		
	
	if(manageType == 'new'){
		url = "${createNewProfile}";	
	}else{
		url = "${updateBasicConfig}";					
	}
	
	
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
				if(response.success == 1){ //Success
					if(manageType == 'new')
					{
						OriprofileName = $("#profilename").val();
					}else
					{
						
						OriprofileName = $("#profilename").val();
					}
					
					$('#id').val(response.groupId);
	                // $('#typeid').val(response.groupId);
					//alert(response.message);
					displayPlugloadprofileBasicMessage(response.message, "green");
				} else { 
			    	submit =false;
				}
				$.ptTimeSelect.closeCntr();
	});
}
	
</script>