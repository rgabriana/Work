<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>
<%@ taglib prefix="security"
	uri="http://www.springframework.org/security/tags"%>

<spring:url value="/users/changepassworddialog.ems"
	var="changePasswordDialogUrl" scope="request" />
<spring:url value="/themes/default/css/menu.css" var="menucss" />
<link rel="stylesheet" type="text/css" href="${menucss}" />

<c:if test="${showBacnet != null && showBacnet != ''}">
	<c:set var='showbacnet' value="${showBacnet}" scope="session" />
</c:if>
<c:if test="${showOpenADR != null && showOpenADR != ''}">
	<c:set var='showopenadr' value="${showOpenADR}" scope="session" />
</c:if>

<style type="text/css">
html,body {
	margin: 0px !important;
}
</style>

<script type="text/javascript"> 
$.ajaxSetup({
   cache: true
 });
</script>

<script type="text/javascript">
	function highLightTab(tabId){
		$.cookie('last_menu_cookie', tabId, {path: '/'});
		
		$('a.link').removeClass('highLight');
		$('#'+tabId).addClass('highLight');	
	}
	
	function changePasswordDialog(){
		$("#changePasswordDialog").load("${changePasswordDialogUrl}?ts="+new Date().getTime(), function() {
			$("#changePasswordDialog").dialog({
				modal:true,
				title: 'Change Password',
				minWidth : 450,
				minHeight : 200,
				resizable: true
			});
		});
	}
</script>

<c:if test="${companySetup != 'true'}">
	<!-- Following table to be shown when company details are available -->
	<script type="text/javascript">
		$(document).ready(function(){
			//highlight last selected menu		
			var TabId=1;
			if ($.cookie('last_menu_cookie') != null ){//if cookie exists--		
				TabId=$.cookie('last_menu_cookie');
			}
			
			highLightTab(TabId);
			
			$(window).load(function() {
		        window.loadTenantUsers = function(tenantId) {        	
		            $("#tenantId").val(tenantId);
		            $('#userListForm').submit();
		            return false;
		        }
		    });
		});
	</script>

	<table id="tblcompcreated" border="0" cellpadding="0" cellspacing="0"
		class="headertbl" >
		<tr>
			<td align="left"><spring:url
					value="/themes/default/images/logo.png" var="imglogo" /> <img
				src="${imglogo}" style='padding-top: 4px' /> <span id="welcome"
				class="welcm"><spring:message code="header.welcome" />&nbsp;<sec:authentication
						property="principal.username" /></span></td>
			<td align="right">
				<table border="0" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
					<tr>
						<td>
							<div class="nav">
								<ul id="cssdropdown">
									<spring:url value="/facilities/home.ems" var="facilities" />
									<li class="headlink" ><a id="1"
										href="${facilities}" class="link" onclick="highLightTab(1);">
											<spring:url value="/themes/default/images/facilities.png"
												var="imgfacilities" />
											<div class='imgicon'>
												<img src="${imgfacilities}" />
											</div>
											<div class='menutxt'>
												<spring:message code="menu.facilities" />
											</div>
									</a></li>
									<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin')">
										<li class="headlink"><a id="2" href="#" class="link"> <spring:url
													value="/themes/default/images/admin.png" var="imgadmin" />
												<div class='imgicon'>
													<img src="${imgadmin}" />
												</div>
												<div class='menutxt'>
													<spring:message code="menu.administration" />
												</div>
										</a>
											<ul>
												<spring:url value="/admin/organization/setup.ems"
													var="orgSetup" />
												<li><a href="${orgSetup}" onclick="highLightTab(2);"><spring:message
															code="menu.organizationsetup" /></a></li>
												<spring:url value="/tenants/list.ems" var="tenants" />
												<li><a href="${tenants}" onclick="highLightTab(2);"><spring:message
															code="menu.tenants" /></a></li>
												<spring:url value="/users/list.ems" var="usersList" />
												<li><a href="${usersList}" onclick="highLightTab(2);"><spring:message
															code="menu.usersettings" /></a></li>
												<spring:url value="/pricing/listPricing.ems" var="listPricing" />
												<li><a href="${listPricing}" onclick="highLightTab(2);"><spring:message
															code="menu.pricing" /></a></li>
												<spring:url value="/settings/ldap.ems" var="ldapSettings" />
												<li><a href="${ldapSettings}" onclick="highLightTab(2);"><spring:message
															code="ldap.settings" /></a></li>
												<spring:url value="/dr/listDR.ems" var="drTarget" />
												<li><a href="${drTarget}" onclick="highLightTab(2);"><spring:message
															code="menu.drtarget" /></a></li>
												<spring:url value="/imageupgrade/get_details.ems"
													var="imageupgrade" />
												<li><a href="${imageupgrade}" onclick="highLightTab(2);"><spring:message
															code="menu.imageupgrade" /></a></li>
												<spring:url value="/bacnet/config.ems" var="bacnetConfig" />
												<c:if test="${showbacnet == 'true'}">
													<li><a href="${bacnetConfig}" onclick="highLightTab(2);"><spring:message
																code="menu.bacnetconfiguration" /></a></li>
												</c:if>
												<spring:url value="/dr/addUser.ems" var="addDRUser" />
												<c:if test="${showopenadr == 'true'}">
													<li><a href="${addDRUser}" onclick="highLightTab(2);"><spring:message
																code="menu.openadr.configuration" /></a></li>
												</c:if>
												<li><a href="#" id="backup" target="_blank"><spring:message
															code="menu.backupandrestore" /></a></li>
												<li><a href="#" id="gemsupgrade" target="_blank"><spring:message
															code="menu.gemsupgrade" /></a></li>
												<li><a href="#" onclick="changePasswordDialog();"><spring:message
															code="header.change.password" /></a></li>
												<spring:url value="/settings/cleanup.ems" var="cleanup" />
												<li><a href="${cleanup}" onclick="highLightTab(2);"><spring:message
															code="menu.cleanup" /></a></li>	
											</ul></li>
									</security:authorize>
			
									<security:authorize access="hasRole('TenantAdmin')">
										<li class="headlink">
											<a id="2" href="#" class="link"> 
												<spring:url value="/themes/default/images/admin.png" var="imgadmin" />
												<div class='imgicon'>
													<img src="${imgadmin}" />
												</div>
												<div class='menutxt'>
													<spring:message code="menu.administration" />
												</div>
											</a>
											<ul>
												<spring:url value="/users/list.ems" var="usersList" />
												<li><a href="#"
													onclick='highLightTab(2); loadTenantUsers("${tenant.id}");'><spring:message
															code="menu.usersettings" /></a></li>
												<li><a href="#"><spring:message
															code="header.change.password" /></a></li>
											</ul></li>
									</security:authorize>
									
									<security:authorize	access="hasAnyRole('Admin','Auditor','TenantAdmin','FacilitiesAdmin')">
										<li class="headlink" >								
											<spring:url	value="/themes/default/images/report.png" var="imgreport" /> 
											<a id="3" href="#">
												<div class='imgicon'>
													<img src="${imgreport}" />
												</div>
												<div class='menutxt'>
													<spring:message code="menu.reports" />
												</div>
											</a>
											<ul>
												<li><spring:url value="/reports/usagebyprofile.ems" var="usagebyprofile" />											
													<a href="${usagebyprofile}" onclick="highLightTab(3);"><spring:message code="menu.usage" /></a></li>
												<li><spring:url value="/reports/outage.ems" var="outage" />											
													<a href="${outage}" onclick="highLightTab(3);"><spring:message code="menu.outage" /></a></li>
												<li><spring:url value="/reports/bulb.ems" var="bulb" />											
													<a href="${bulb}" onclick="highLightTab(3);"><spring:message code="menu.bulb" /></a></li>
											</ul>
										</li>
									</security:authorize>
									
									<li class="headlink" >
										<spring:url	value="/themes/default/images/help.png" var="imghelp" /> 
										<a id="4" href="#" class="link">
											<div class='imgicon'>
												<img src="${imghelp}" />
											</div>
											<div class='menutxt'>
												<spring:message code="header.help" />
											</div>
										</a>
									</li>
									<li class="headlink noright" ><spring:url
											value="/j_spring_security_logout" var="logout_url" /> <spring:url
											value="/themes/default/images/logout.png" var="imglogout" /> <a
										id="4" href="${logout_url}" class="link">
											<div class='imgicon'>
												<img src="${imglogout}" />
											</div>
											<div class='menutxt'>
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
					code="header.current.server.time" />
				<fmt:formatDate pattern="yyyy-MM-dd HH:mm" value="${now}" /></td>
		</tr>
	</table>

	<script type="text/javascript">
	 	var backup_url = location.protocol + '//' + location.host + '/ems_mgmt/backup.emsmgmt';
		$("#backup").attr("href", backup_url);
		var gemsupgrade_url = location.protocol + '//' + location.host + '/ems_mgmt/gemsupgrade.emsmgmt';
		$("#gemsupgrade").attr("href", gemsupgrade_url);
	</script>

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
				<table border="0" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
					<tr>
						<td>
							<div class="nav">
								<ul id="cssdropdown">			
									<li class="headlink">
										<spring:url	value="/themes/default/images/admin.png" var="imgadmin" /> 
										<a id="3" href="#" class="link" onclick="changePasswordDialog();">
											<div class='imgicon'>
												<img src="${imgadmin}" />
											</div>
											<div class='menutxt'>
												<spring:message code="header.change.password" />
											</div>
										</a>
									</li>
									<li class="headlink">
										<spring:url value="/themes/default/images/help.png" var="imghelp" /> 
										<a id="3" href="#" class="link">
											<div class='imgicon'>
												<img src="${imghelp}" />
											</div>
											<div class='menutxt'>
												<spring:message code="header.help" />
											</div>
										</a>
									</li>
									<li class="headlink noright">
										<spring:url value="/j_spring_security_logout" var="logout_url" /> 
										<spring:url	value="/themes/default/images/logout.png" var="imglogout" /> 
										<a id="4" href="${logout_url}" class="link">
											<div class='imgicon'>
												<img src="${imglogout}" />
											</div>
											<div class='menutxt'>
												<spring:message code="header.logout" />
											</div>
										</a>
									</li>
								</ul>
							</div>
						</td>
					</tr>
				</table>
			</td>
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