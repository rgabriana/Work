<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>
<%@ taglib prefix="security"
	uri="http://www.springframework.org/security/tags"%>
<spring:url value="/services/events/list/scheduledEvent/" var="listScheduleEvents" scope="request" />
<spring:url value="/users/changepassworddialog.ems"
	var="changePasswordDialogUrl" scope="request" />
	<spring:url value="/settings/bulbConfiguration.ems"
	var="bulbConfigurationURL" scope="request" />
<spring:url value="/themes/default/css/menu.css" var="menucss" />
<link rel="stylesheet" type="text/css" href="${menucss}" />

<c:if test="${showBacnet != null && showBacnet != ''}">
	<c:set var='showbacnet' value="${showBacnet}" scope="session" />
</c:if>
<c:if test="${showOpenADR != null && showOpenADR != ''}">
	<c:set var='showopenadr' value="${showOpenADR}" scope="session" />
</c:if>
<c:if test="${enableSweepTimer != null && enableSweepTimer != ''}">
	<c:set var='enableSweepTimer' value="${enableSweepTimer}" scope="session" />
</c:if>
<c:if test="${enableMotionBits != null && enableMotionBits != ''}">
	<c:set var='enableMotionBits' value="${enableMotionBits}" scope="session" />
</c:if>
<c:if test="${enableBulbConfiguration != null && enableBulbConfiguration != ''}">
	<c:set var='enableBulbConfiguration' value="${enableBulbConfiguration}" scope="session" />
</c:if>
<c:if test="${ldapFlag != null && ldapFlag != ''}">
	<c:set var='ldapFlag' value="${ldapFlag}" scope="session" />
</c:if>
<c:if test="${enableConnexusFeature != null && enableConnexusFeature != ''}">
	<c:set var='enableConnexusFeature' value="${enableConnexusFeature}" scope="session" />
</c:if>
<style type="text/css">
html,body {
	margin: 0px !important;
}

.newsticker{
		white-space: nowrap;
		padding: 0;
		color: #ff0000;
		font: bold 11px Verdana;
}
</style>

<script type="text/javascript">
	$.ajaxSetup({
		cache : true
	});
</script>

<script type="text/javascript">
	function highLightTab(tabId) {
		$.cookie('last_menu_cookie', tabId, {
			path : '/'
		});

		$('a.link').removeClass('highLight');
		$('#' + tabId).addClass('highLight');
	}

	function changePasswordDialog() {
		$("#changePasswordDialog").load(
				"${changePasswordDialogUrl}?ts=" + new Date().getTime(),
				function() {
					$("#changePasswordDialog").dialog({
						modal : true,
						title : 'Change Password',
						minWidth : 450,
						minHeight : 200,
						resizable : true
					});
				});
	}
	function openBulbConfigurationDialog() {
		$("#bulbConfigurationDialog").load(
				"${bulbConfigurationURL}?ts=" + new Date().getTime(),
				function() {
					$("#bulbConfigurationDialog").dialog({
						modal : true,
						title : 'Lamp Outage Configuration',
						minWidth : 400,
						minHeight : 260,
						resizable : false
					});
				});
	}
	
	
	function showhelp() {
		if (confirm("Help is at http://www.enlightedinc.biz. Please make sure you have internet connectivity."))
			window.open("http://www.enlightedinc.biz");
	}
	
	function hideMenu()
	{
		
		if($.browser.msie && parseInt($.browser.version) == 8){
			//do nothing for Internet explorer 8
		}
		else{
			$("#subMenu").css('display', 'none');
		}
		
	}
	
	function showMenu()
	{
		
		if($.browser.msie && parseInt($.browser.version) == 8){
			//do nothing for Internet explorer 8
		}
		else{
			$("#subMenu").css('display', 'block');
		}
		
	}
	
</script>

<c:if test="${companySetup != 'true'}">
	<!-- Following table to be shown when company details are available -->
	<script type="text/javascript">

		$(document).ready(function() {
			$("#notificationMessage").css('display', 'block');
			removeStickerMenu();
			//highlight last selected menu		
			var TabId = 1;
			if ($.cookie('last_menu_cookie') != null) {//if cookie exists--		
				TabId = $.cookie('last_menu_cookie');
			}

			highLightTab(TabId);

			$(window).load(function() {
				window.loadTenantUsers = function(tenantId) {
					$("#tenantId").val(tenantId);
					$('#userListForm').submit();
					return false;
				}
			});
			
 			networkCheck = setInterval(function() {
 				checkNetworkConnectivity();
 			},30*1000);
			
			
			if($.browser.msie && parseInt($.browser.version) == 7){
				$("#subMenu").css('position', 'relative');
				$("#reportsSubMenu").css('position', 'relative');
			}
			
			checkNetworkConnectivity();
			
		});
		
		var networkCheck = null;
		function checkNetworkConnectivity()
		{
		    
			var heartbeat_url = location.protocol + '//' + location.host + "/heartbeat.jsp?ts="+new Date().getTime();
			$.ajax(
		    {
		        type: 'GET',
		        cache: false,
		        url: heartbeat_url,
		        success: function(data ,textStatus, xhr)
		        {
		           	 
		    	     if (xhr.status == 200){
		        	   $('#networkMessage').text("");
		        	   if($(data).find("#maintenance").html() == "Y") {
                           $('#networkMessage').text("System is under maintenance. Please come back after some time.");
                          clearInterval(networkCheck);
                       }else{
                    	   updateStickerMenu(data);
                     }
		           }
		        
		        },
		        error: function(XMLHttpRequest, textStatus, errorThrown)
		        {
		            //For Maven Tomcat Run 
		            if(XMLHttpRequest.status == 404){
		            	$('#networkMessage').text("");
		            }
		            else{
		            	$('#networkMessage').text("Failed to connect to EM. Please check your network connection.");
		            }
		        	 
		        }
		    });
		}
		
		/*
		**************************  NEWS STICKER MENU METHODS STARTS  ******************************************************
		*/
		function updateStickerMenu(data)
		{ 
				// DR NOTIFICATION DISPLAY LOGIC
	   			if($(data).find("#duration").html() != "")
	   			{
	   				removeStickerMenu();
	   	        	
	   	        	var starttimeinmillisec = parseInt($(data).find("#starttime").html());
	   	        	var durationinsec = parseInt($(data).find("#duration").html());
	   	        	var durationinmillisec = durationinsec * 1000 ;
	   	        	var durationinmin = durationinsec/60 ;
	   	        	var currenttimeinmillisec = parseInt($(data).find("#currenttime").html());
	   	        	var endtimeinmillisec =  starttimeinmillisec + durationinmillisec ;
	   	        	var timeremaininginmillisec = endtimeinmillisec - currenttimeinmillisec;
	   	        	var timeremaininginsec = timeremaininginmillisec/1000;
	   	        	var timeremaininginmin = timeremaininginsec/60;
	   	        	
	   	        	
	   	        	var level = $(data).find("#level").html();
		        	var type = $(data).find("#drtype").html(); 
	   				
	   				if($(data).find("#duration").html() != "-1" && (durationinsec == 0 || timeremaininginsec > 0))
	   				{
	   		        	var stickerMainDiv = document.getElementById('notificationMessage');
	   					
	   					var spanEle1 = document.createElement('span1');
	   					var spamNodetext1 = document.createTextNode("DR in progress. Initiate type: ");
	   					spanEle1.appendChild(spamNodetext1);
	   					
	   					var spanEle2 = document.createElement('span2');
	   					var spamNodetext2 = document.createTextNode(type);
	   					spanEle2.className="newsticker";
	   					spanEle2.appendChild(spamNodetext2);
	   					
	   					var spanEle3 = document.createElement('span3');
	   					var spamNodetext3 = document.createTextNode(", Level: ");
	   					spanEle3.appendChild(spamNodetext3);
	   					
	   					var spanEle4 = document.createElement('span4');
	   					var spamNodetext4 = document.createTextNode(level);
	   					spanEle4.className="newsticker";
	   					spanEle4.appendChild(spamNodetext4);
	   					
	   					stickerMainDiv.appendChild(spanEle1);
	   					stickerMainDiv.appendChild(spanEle2);
	   					stickerMainDiv.appendChild(spanEle3);
	   					stickerMainDiv.appendChild(spanEle4);

	   					if(durationinsec != 0)
	   					{
	   							var spanEle5 = document.createElement('span5');
	   							var spamNodetext5 = document.createTextNode(", Time Remaining: ");
	   							spanEle5.appendChild(spamNodetext5);
	   							
	   							var spanEle6 = document.createElement('span6');
	   							var spamNodetext6;
	   							if(timeremaininginmin >= 1)
	   							{
	   								spamNodetext6 = document.createTextNode(Math.round(timeremaininginmin) +" min");
	   							}
	   							else 
	   							{
	   								spamNodetext6 = document.createTextNode(Math.round(timeremaininginsec) +" sec");
	   							}
	   							spanEle6.className="newsticker";
	   							spanEle6.appendChild(spamNodetext6);
	   						
	   							stickerMainDiv.appendChild(spanEle5);
	   							stickerMainDiv.appendChild(spanEle6);
	   					}
	   				}
	   			}
		}
		
		
		function removeStickerMenu()
		{
			$("#notificationMessage").empty();
		}
		function getScheduleEventList()
		{
			// Fire Ajax Call to get list of currently active scheduled event in the system.
			// We will refresh the UI everty 30 Sec to check whether new event has been added or any event has been finished.
			// and accordingly UI will get updated
			var timestamp = "?ts="+new Date().getTime();
			$.ajax({
				type: "GET",
				cache: false,
				data: "",
				url: "${listScheduleEvents}"+timestamp,
				contentType: "application/json; charset=utf-8",
				dataType:"json",
				success: function(data) {
					//Currently data returns only DrStatus Object, In Future It may return Image Upgrade Status
		        	updateStickerMenu(data);
				},error:function(xmlReq)
				{
				}
			});
		} 
		function toTitleCase(str) {
		    return str.replace(/\w\S*/g, function (txt) {return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
		}
		/*
		**************************  NEWS STICKER MENU METHODS ENDS  ******************************************************
		*/
	</script>
	
	<div id="networkMessage" style="color:black; background-color:#FFFF00; width:450px; margin:auto; text-align:center;font-weight:bold;" align="center">
								
	</div>
	
	<table id="tblcompcreated" border="0" cellpadding="0" cellspacing="0"
		class="headertbl">
		<tr>
			<c:choose>
			      <c:when test="${enableConnexusFeature == '1'}">
			      <td align="left" width="28%"><spring:url value="/themes/default/images/logo_Connexus.png" var="imglogo" /><img
				src="${imglogo}" style='padding-top: 4px' /> <span id="welcome"
				class="welcmConnexus"><spring:message code="header.welcome" />&nbsp;<sec:authentication
						property="principal.username" /></span></td>
			      </c:when>
			      <c:otherwise>
			      <td align="left" width="28%"> <spring:url	value="/themes/default/images/logo.png" var="imglogo" /><img
				src="${imglogo}" style='padding-top: 4px' /> <span id="welcome"
				class="welcm"><spring:message code="header.welcome" />&nbsp;<sec:authentication
						property="principal.username" /></span></td>
			      </c:otherwise>
			</c:choose>		
						
			<td valign="middle" width="35%">
				<table  border="0" cellpadding"0" cellspacing="0">
					<tr>
						<td align="center">
							<div id="notificationMessage"  align="center" style="display: none;">
				    		</div> 
				    	</td>
					</tr>
				</table>
			</td>
			
			<td align="right">
				<table border="0" cellpadding="0" cellspacing="0"
					style="border-collapse: collapse;">
					<tr>
						<td>
							<div class="nav">
								<ul id="cssdropdown">
									<spring:url value="/facilities/home.ems" var="facilities" />
									<li class="headlink"><a id="facilitiesMenu" href="${facilities}"
										class="link" onclick="highLightTab(1);"> <spring:url
												value="/themes/default/images/facilities.png"
												var="imgfacilities" />
											<div class='imgicon'>
												<img src="${imgfacilities}" />
											</div>
											<div class='menutxt' id="facilitiesMenu">
												<spring:message code="menu.facilities" />
											</div>
									</a></li>
									<security:authorize
										access="hasAnyRole('Admin','FacilitiesAdmin')">
										<li class="headlink" onmouseover="showMenu();" onmouseout="hideMenu();"><a id="adminMenu" href="#" class="link" >
												<spring:url value="/themes/default/images/admin.png"
													var="imgadmin" />
												<div class='imgicon'>
													<img src="${imgadmin}" />
												</div>
												<div class='menutxt' id="administrationMenu">
													<spring:message code="menu.administration" />
												</div>
										</a>
											<ul id="subMenu">
												<spring:url value="/admin/organization/setup.ems"
													var="orgSetup" />
												<li><a href="${orgSetup}" onclick="highLightTab(2);" id="orgSetupMenu"><spring:message
															code="menu.organizationsetup" /></a></li>
												<!-- <spring:url value="/tenants/list.ems" var="tenants" />
												<li><a href="${tenants}" onclick="highLightTab(2);" id="tenantsMenu"><spring:message
															code="menu.tenants" /></a></li>			-->							
												<spring:url value="/users/list.ems" var="usersList" />
												<li><a href="${usersList}" onclick="highLightTab(2);" id="usersListMenu"><spring:message
															code="menu.usersettings" /></a></li>														
												<spring:url value="/pricing/listPricing.ems"
													var="listPricing" />
												<li><a href="${listPricing}" onclick="highLightTab(2);" id="listPricingMenu"><spring:message
															code="menu.pricing" /></a></li>
												<security:authorize access="hasAnyRole('Admin')">
												<c:if test="${enableSweepTimer == 'true'}">
												<spring:url value="/sweeptimer/list.ems" var="sweeptimerList" />
												<li><a href="${sweeptimerList}" onclick="highLightTab(2);" id="sweeptimerListMenu"><spring:message
															code="menu.sweeptimer" /></a></li>
												</c:if>
												</security:authorize>
												<security:authorize access="hasAnyRole('Admin')">
												<c:if test="${enableMotionBits == 'true'}">
												<spring:url value="/motionbits/list.ems" var="motionbitsList" />
												<li><a href="${motionbitsList}" onclick="highLightTab(2);" id="motionbitsListMenu"><spring:message
															code="menu.motionbits" /></a></li>
												</c:if>
												</security:authorize>
												<security:authorize access="hasAnyRole('Admin')">
													<spring:url value="/settings/ldap.ems" var="ldapSettings" />
													<li><a href="${ldapSettings}"
														onclick="highLightTab(2);" id="ldapSettingsMenu"><spring:message
																code="ldap.settings" /></a></li>
												</security:authorize>
												<spring:url value="/dr/listDR.ems" var="drTarget" />
												<li><a href="${drTarget}" onclick="highLightTab(2);" id="drTargetMenu"><spring:message
															code="menu.drtarget" /></a></li>
												<security:authorize access="hasAnyRole('Admin')">			
												<spring:url value="/imageupgrade/get_details.ems"
													var="imageupgrade" />
												<li><a href="${imageupgrade}"
													onclick="highLightTab(2);" id="imageupgradeMenu"><spring:message
															code="menu.imageupgrade" /></a></li>
												</security:authorize>
												<spring:url value="/bacnet/config.ems" var="bacnetConfig" />
												<c:if test="${showbacnet == 'true'}">
													<li><a href="${bacnetConfig}"
														onclick="highLightTab(2);" id="bacnetConfigMenu"><spring:message
																code="menu.bacnetconfiguration" /></a></li>
												</c:if>
												<spring:url value="/dr/addUser.ems" var="addDRUser" />
												<c:if test="${showopenadr == 'true'}">
													<li><a href="${addDRUser}" onclick="highLightTab(2);" id="addDRUserMenu"><spring:message
																code="menu.openadr.configuration" /></a></li>
												</c:if>
												<security:authorize access="hasAnyRole('Admin')">
												<li><a href="#" id="backup" target="_blank" onClick="hideMenu();"><spring:message
															code="menu.backupandrestore"  /></a></li>
												<li><a id="gemsupgrade" target="_blank" onClick="hideMenu();"><spring:message
															code="menu.gemsupgrade" /></a></li>
												</security:authorize>
												
												<li><a href="#" onclick="changePasswordDialog();" id="changePasswordDialogMenu"><spring:message
															code="header.change.password" /></a></li>
												
												<spring:url value="/settings/system_management.ems"
													var="systemManagement" />
												<li><a href="${systemManagement}"
													onclick="highLightTab(2);" id="systemManagementMenu"><spring:message
															code="menu.cleanup" /></a></li>
											
												<spring:url value="/profileTemplateManagement/management.ems" 
												var="templateManagement"/>
												<li><a href="${templateManagement}" id="profileTemplateMainMenu"
													onclick="highLightTab(2);"><spring:message
													code="menu.templateManagement" /></a></li>
												
												<security:authorize access="hasAnyRole('Admin')">
												<c:if test="${enableBulbConfiguration == 'true'}">
												<li><a href="#" onclick="openBulbConfigurationDialog();" id="bulbConfigurationDetailMenu"><spring:message
															code="menu.bulbconfiguration" /></a></li>
												</c:if>
												</security:authorize>
												
											</ul></li>
									</security:authorize>

									<security:authorize
										access="hasAnyRole('Auditor','TenantAdmin','Employee')">
										<li class="headlink"><a id="adminMenu" href="#" class="link">
												<spring:url value="/themes/default/images/admin.png"
													var="imgadmin" />
												<div class='imgicon'>
													<img src="${imgadmin}" />
												</div>
												<div class='menutxt' id="administrationMenu">
													<spring:message code="menu.administration" />
												</div>
										</a>
											<ul>
												<security:authorize access="hasRole('TenantAdmin')">
													<spring:url value="/users/list.ems" var="usersList" />
													<li><a href="#"
														onclick='highLightTab(2); loadTenantUsers("${tenant.id}");' id="tenantsMenu"><spring:message
																code="menu.usersettings" /></a></li>
												</security:authorize>
												 
												 <security:authorize access="hasRole('TenantAdmin')">
												<spring:url value="/profileTemplateManagement/management.ems" var="templateManagement"/>
												<li><a href="${templateManagement}" id="profileTemplateMainMenu"
													onclick="highLightTab(2);"><spring:message
													code="menu.templateManagement" /></a></li>
												</security:authorize>
												
												<c:if test="${ldapFlag != 'false'}">
												<li><a href="#" onclick="changePasswordDialog();" id="changePasswordDialogMenu"><spring:message
															code="header.change.password" /></a></li>
												</c:if>

											</ul></li>
									</security:authorize>

									<security:authorize
										access="hasAnyRole('Admin','Auditor','TenantAdmin','FacilitiesAdmin')">
										<li class="headlink"><spring:url
												value="/themes/default/images/report.png" var="imgreport" />
											<a id="reportMenu" href="#" class="link" >
												<div class='imgicon'>
													<img src="${imgreport}" />
												</div>
												<div class='menutxt' id="reportsMenu">
													<spring:message code="menu.reports" />
												</div>
										</a>
											<ul id="reportsSubMenu">
												<li><spring:url value="/reports/usagebyprofile.ems"
														var="usagebyprofile" /> <a href="${usagebyprofile}"
													onclick="highLightTab(3);" id="usagebyprofileMenu"><spring:message
															code="menu.usage" /></a></li>
												<li><spring:url value="/reports/outage.ems"
														var="outage" /> <a href="${outage}"
													onclick="highLightTab(3);" id="outageMenu"><spring:message
															code="menu.outage" /></a></li>
												<c:if test="${enableBulbConfiguration == 'true'}">
													<li><spring:url value="/reports/bulb.ems" var="bulb" />											 
														<a href="${bulb}" onclick="highLightTab(3);"><spring:message code="menu.bulb" /></a></li>
												</c:if>			
												<security:authorize
													access="hasAnyRole('Admin','Auditor','FacilitiesAdmin', 'TenantAdmin')">
													<li><spring:url value="/reports/auditlog.ems"
															var="auditlog" /> <a href="${auditlog}"
														onclick="highLightTab(3);" id="auditlogMenu"><spring:message
																code="menu.auditlog" /></a></li>
												</security:authorize>
											</ul></li>
									</security:authorize>

									<li class="headlink"><spring:url
											value="/themes/default/images/help.png" var="imghelp" /> <a
										id="helpMenu" href="#" class="link" onclick="showhelp();">
											<div class='imgicon'>
												<img src="${imghelp}" />
											</div>
											<div class='menutxt' id="helpMenu">
												<spring:message code="header.help" />
											</div>
									</a></li>
									<li class="headlink noright"><spring:url
											value="/j_spring_security_logout" var="logout_url" /> <spring:url
											value="/themes/default/images/logout.png" var="imglogout" />
										<a id="logoutMenu" href="${logout_url}" class="link">
											<div class='imgicon'>
												<img src="${imglogout}" />
											</div>
											<div class='menutxt' id="logoutMenu">
												<spring:message code="header.logout" />
											</div>
									</a></li>
								</ul>
							</div>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td></td>
			<td></td>
			<td align="right"><ems:showAppVersion />&nbsp;<c:set var="now"
					value="<%=new java.util.Date()%>" /> <spring:message
					code="header.current.server.time" /> <fmt:formatDate
					pattern="yyyy-MM-dd HH:mm" value="${now}" /></td>
		</tr>
	</table>
	<c:if test="${apacheInstalled == null}">
		<c:if test="${securityKey != null}">
		<script type="text/javascript">
			var backup_url = location.protocol + '//' + location.host
					+ '/ems_mgmt/home.jsp?page=backup&code=' + ${securityKey};
			$("#backup").attr("href", backup_url);
			var gemsupgrade_url = location.protocol + '//' + location.host
					+ '/ems_mgmt/home.jsp?page=gemsupgrade&code=' + ${securityKey};
			$("#gemsupgrade").attr("href", gemsupgrade_url);
		</script>
		</c:if>
	</c:if>
	
	<c:if test="${apacheInstalled != null}">
		<script type="text/javascript">
			var backup_url = location.protocol + '//' + location.host
					+ '/em_mgmt/backuprestore/';
			$("#backup").attr("href", backup_url);
			var gemsupgrade_url = location.protocol + '//' + location.host
					+ '/em_mgmt/upgrade/';
			$("#gemsupgrade").attr("href", gemsupgrade_url);
		</script>
	</c:if>
	<spring:url value="/users/list.ems" var="usersList" />
	<form id="userListForm" action="${usersList}" method="post">
		<input type="hidden" id="tenantId" name="tenantId" />
	</form>

</c:if>

<c:if test="${companySetup == 'true'}">
	<!-- Following table to be shown when company details are NOT available -->

	<table id="tblcompnotcreated" border="0" cellpadding="0"
		cellspacing="0" class="headertbl">
		<tr>
			<td align="left"><spring:url
					value="/themes/default/images/logo.png" var="imglogo" /> <img
				src="${imglogo}" style='padding-top: 4px' /> <span id="welcome"
				class="welcm"><spring:message code="header.welcome" />&nbsp;<sec:authentication
						property="principal.username" /></span></td>
			<td align="right">
				<table border="0" cellpadding="0" cellspacing="0"
					style="border-collapse: collapse;">
					<tr>
						<td>
							<div class="nav">
								<ul id="cssdropdown">
									<li class="headlink"><spring:url
											value="/themes/default/images/admin.png" var="imgadmin" /> <a
										id="adminMenu" href="#" class="link" onclick="changePasswordDialog();">
											<div class='imgicon'>
												<img src="${imgadmin}" />
											</div>
											<div class='menutxt' id="changePasswordMenu">
												<spring:message code="header.change.password" />
											</div>
									</a></li>
									<li class="headlink"><spring:url
											value="/themes/default/images/help.png" var="imghelp" /> <a
										id="helpMenu" href="#" class="link">
											<div class='imgicon'>
												<img src="${imghelp}" />
											</div>
											<div class='menutxt' id="helpMenu">
												<spring:message code="header.help" />
											</div>
									</a></li>
									<li class="headlink noright"><spring:url
											value="/j_spring_security_logout" var="logout_url" /> <spring:url
											value="/themes/default/images/logout.png" var="imglogout" />
										<a id="logoutMenu" href="${logout_url}" class="link">
											<div class='imgicon'>
												<img src="${imglogout}" />
											</div>
											<div class='menutxt' id="logOutMenu">
												<spring:message code="header.logout" />
											</div>
									</a></li>
								</ul>
							</div>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td></td>
			<td align="right"><ems:showAppVersion />&nbsp;<c:set var="now"
					value="<%=new java.util.Date()%>" /> <spring:message
					code="header.current.server.time" /> <fmt:formatDate
					pattern="yyyy-MM-dd HH:mm" value="${now}" /></td>
		</tr>
		<tr>
			<td align="left" colspan='2'>
				<div id="wrapper">
					<div id="firstStep">
						<table cellpadding="0" cellspacing="0" width=100%>
							<tr>
								<td class="currentStep"><em><spring:message
											code="setup.step1.header" /></em><br /> <span><spring:message
											code="setup.step1.description" /></span></td>
								<td class="next"><em><spring:message
											code="setup.step2.header" /></em><br /> <span><spring:message
											code="setup.step2.description" /></span></td>
								<td class="next"><em><spring:message
											code="setup.step3.header" /></em><br /> <span><spring:message
											code="setup.step3.description" /></span></td>
								<td class="next"><em><spring:message
											code="setup.step4.header" /></em><br /> <span><spring:message
											code="setup.step4.description" /></span></td>
								<td class="next"><em><spring:message
											code="setup.step5.header" /></em><br /> <span><spring:message
											code="setup.step5.description" /></span></td>
							</tr>
						</table>
					</div>
					<div id="secondStep">
						<table cellpadding="0" cellspacing="0" width=100%>
							<tr>
								<td class="done"><em><spring:message
											code="setup.step1.header" /></em><br /> <span><spring:message
											code="setup.step1.description" /></span></td>
								<td class="currentStep"><em><spring:message
											code="setup.step2.header" /></em><br /> <span><spring:message
											code="setup.step2.description" /></span></td>
								<td class="next"><em><spring:message
											code="setup.step3.header" /></em><br /> <span><spring:message
											code="setup.step3.description" /></span></td>
								<td class="next"><em><spring:message
											code="setup.step4.header" /></em><br /> <span><spring:message
											code="setup.step4.description" /></span></td>
								<td class="next"><em><spring:message
											code="setup.step5.header" /></em><br /> <span><spring:message
											code="setup.step5.description" /></span></td>
							</tr>
						</table>
					</div>
					<div id="thirdStep">
						<table cellpadding="0" cellspacing="0" width=100%>
							<tr>
								<td class="done"><em><spring:message
											code="setup.step1.header" /></em><br /> <span><spring:message
											code="setup.step1.description" /></span></td>
								<td class="done"><em><spring:message
											code="setup.step2.header" /></em><br /> <span><spring:message
											code="setup.step2.description" /></span></td>
								<td class="currentStep"><em><spring:message
											code="setup.step3.header" /></em><br /> <span><spring:message
											code="setup.step3.description" /></span></td>
								<td class="next"><em><spring:message
											code="setup.step4.header" /></em><br /> <span><spring:message
											code="setup.step4.description" /></span></td>
								<td class="next"><em><spring:message
											code="setup.step5.header" /></em><br /> <span><spring:message
											code="setup.step5.description" /></span></td>
							</tr>
						</table>
					</div>
					<div id="fourthStep">
						<table cellpadding="0" cellspacing="0" width=100%>
							<tr>
								<td class="done"><em><spring:message
											code="setup.step1.header" /></em><br /> <span><spring:message
											code="setup.step1.description" /></span></td>
								<td class="done"><em><spring:message
											code="setup.step2.header" /></em><br /> <span><spring:message
											code="setup.step2.description" /></span></td>
								<td class="done"><em><spring:message
											code="setup.step3.header" /></em><br /> <span><spring:message
											code="setup.step3.description" /></span></td>
								<td class="currentStep"><em><spring:message
											code="setup.step4.header" /></em><br /> <span><spring:message
											code="setup.step4.description" /></span></td>
								<td class="next"><em><spring:message
											code="setup.step5.header" /></em><br /> <span><spring:message
											code="setup.step5.description" /></span></td>
							</tr>
						</table>
					</div>
					<div id="fifthStep">
						<table cellpadding="0" cellspacing="0" width=100%>
							<tr>
								<td class="done"><em><spring:message
											code="setup.step1.header" /></em><br /> <span><spring:message
											code="setup.step1.description" /></span></td>
								<td class="done"><em><spring:message
											code="setup.step2.header" /></em><br /> <span><spring:message
											code="setup.step2.description" /></span></td>
								<td class="done"><em><spring:message
											code="setup.step3.header" /></em><br /> <span><spring:message
											code="setup.step3.description" /></span></td>
								<td class="done"><em><spring:message
											code="setup.step4.header" /></em><br /> <span><spring:message
											code="setup.step4.description" /></span></td>
								<td class="currentStep"><em><spring:message
											code="setup.step5.header" /></em><br /> <span><spring:message
											code="setup.step5.description" /></span></td>
							</tr>
						</table>
					</div>
				</div>
			</td>
		</tr>
	</table>
</c:if>

<div id="changePasswordDialog"></div>
<div id="bulbConfigurationDialog"></div>