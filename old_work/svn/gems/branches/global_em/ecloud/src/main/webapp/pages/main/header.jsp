<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ecloud" uri="/WEB-INF/tlds/ecloud.tld"%>
<%@ taglib prefix="security"
	uri="http://www.springframework.org/security/tags"%>
	
<spring:url value="/users/changepassworddialog.ems"
	var="changePasswordDialogUrl" scope="request" />

<spring:url value="/themes/default/css/menu.css" var="menucss" />
<spring:url value="/home.ems" var="homeUrl" />
<link rel="stylesheet" type="text/css" href="${menucss}" />
<c:set var='cloudMode' value="false" scope="session" />
<c:if test="${cloudMode != null && cloudMode != ''}">
	<c:set var='cloudMode' value="${cloudMode}" scope="session" />
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
	function showhelp() {
		if (confirm("Help is at http://www.enlightedinc.biz. Please make sure you have internet connectivity."))
			window.open("http://www.enlightedinc.biz");
	}	
</script>

<script type="text/javascript">
		$(document).ready(function(){
			//highlight last selected menu		
			var TabId=1;
			if ($.cookie('last_menu_cookie') != null ){//if cookie exists--		
				TabId=$.cookie('last_menu_cookie');
			}
			
			highLightTab(TabId);
		});
	</script>

	<table id="tblcompcreated" border="0" cellpadding="0" cellspacing="0"
		class="headertbl">
		<tr>
			<td align="left" width="28%"><spring:url
					value="/themes/default/images/logo.png" var="imglogo" /> <img
				src="${imglogo}" style='padding-top: 4px' /><span id="welcome"
				class="welcm"><spring:message code="header.welcome" />&nbsp;<sec:authentication
						property="principal.username" /></span></td>
			<td>
				<span id="dashboardTitle" class="dashboardTitle"><spring:message code="header.dashboardTitle" />
			</td>
							
			<td align="right" width="44%">
			<table border="0" cellpadding="0" cellspacing="0"
				style="border-collapse: collapse;">
			<tr>
			<td>
				<div class="nav">
				<ul id="cssdropdown">
					<spring:url value="/home.ems" var="customers" />
					<li class="headlink"><a id="customersMenu" href="${customers}" 
						class="link" onclick="highLightTab(1);">
							<spring:url value="/themes/default/images/facilities.png"
								var="imgcustomers" />
							<div class='imgicon'>
								<img src="${imgcustomers}" />
							</div>
							<div class='menutxt' id="customersMenu">
								<spring:message code="menu.customers" />
							</div>
					</a></li>
					
					
					<li class="headlink"><a id="adminMenu" href="#" class="link"> <spring:url
									value="/themes/default/images/admin.png" var="imgadmin" />
								<div class='imgicon'>
									<img src="${imgadmin}" />
								</div>
								<div class='menutxt' id="administrationMenu">
									<spring:message code="menu.administration" />
								</div>
						</a>
						<ul id="subMenu">
						<c:if test="${cloudMode == 'true'}">
							<spring:url value="/health/monitoring.ems" var="monitoring_dashboard" />
							<security:authorize access="hasAnyRole('Admin','SystemAdmin','SupportAdmin','ThirdPartySupportAdmin')">
							<li><a href="${monitoring_dashboard}" id="monitoringDashboardMenu">Monitoring Dashboard</a></li>
							</security:authorize>
						</c:if>
						<spring:url value="/eminstance/listUnregEms.ems" var="unRegEmList" />
						<security:authorize access="hasAnyRole('Admin','SystemAdmin','SupportAdmin')">
						<li><a href="${unRegEmList}" id="unregisterMenu">Unregistred EMs</a></li>
						</security:authorize>

						<c:if test="${cloudMode == 'true'}">
							<spring:url value="/replicaserver/list.ems" var="replicaServerList" />
							<security:authorize access="hasAnyRole('Admin','SystemAdmin')">
							<li><a href="${replicaServerList}" id="replicaServerMenu">Replica Servers</a></li>
							</security:authorize>
						</c:if>
						<spring:url value="/upgrades/list.ems" var="upgradesList" />
						<security:authorize access="hasAnyRole('Admin','SystemAdmin','SupportAdmin')">
						<li><a href="${upgradesList}" id="upgradeMenu">Upgrades Management</a></li>
						</security:authorize>
						<spring:url value="/eminstance/statusList.ems" var="emStatusList" />
						<security:authorize access="hasAnyRole('Admin','SystemAdmin')">
						<li><a href="${emStatusList}" id="emStatusMenu">Task Management</a></li>				
						</security:authorize>
						<spring:url value="/users/list.ems" var="emUsersList" />
						<security:authorize access="hasAnyRole('Admin','SystemAdmin')">
						<li><a href="${emUsersList}" id="emUsersMenu">User Management</a></li>
						</security:authorize>
						<security:authorize access="hasAnyRole('Admin','SystemAdmin','SupportAdmin')">
						<spring:url value="/reports/auditlog.ems" var="auditlog" />
						<li><a href="${auditlog}" id="auditlogMenu"><spring:message code="menu.auditlog" /></a></li>
						</security:authorize>
						<li><a href="#" onclick="changePasswordDialog();" id="changePasswordDialogMenu">Change Password</a></li>
						</ul> 
					</li>
					
					<li class="headlink"><spring:url
							value="/themes/default/images/help.png" var="imghelp" /> 
						<a	id="helpMenu" href="#" class="link" onclick="showhelp();">
								<div class='imgicon'>
									<img src="${imghelp}" />
								</div>
								<div class='menutxt' id="helpMenu">
									<spring:message code="header.help" />
								</div>
						</a></li>
					
						<li class="headlink noright" ><spring:url
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
			<td align="right"><ecloud:showAppVersion />&nbsp;<c:set var="now"
					value="<%=new java.util.Date()%>" /> <spring:message
					code="header.current.server.time" />
				<fmt:formatDate pattern="yyyy-MM-dd HH:mm" value="${now}" /></td>
		</tr>
	</table>
<div id="changePasswordDialog"></div>