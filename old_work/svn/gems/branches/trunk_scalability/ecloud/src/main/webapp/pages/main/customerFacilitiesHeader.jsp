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
<link rel="stylesheet" type="text/css" href="${menucss}" />

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
			<td align="left"><spring:url
					value="/themes/default/images/logo.png" var="imglogo" /> <img
				src="${imglogo}" style='padding-top: 4px' /><span id="welcome"
				class="welcm"><spring:message code="header.welcome" />&nbsp;<sec:authentication
						property="principal.username" /></span></td>
			<td>
				<span id="dashboardTitle" class="dashboardTitle"><spring:message code="header.dashboardTitle" />
			</td>
							
			<td align="right">
				<div class="nav">
					<ul id="cssdropdown">
						<spring:url value="/home.ems" var="customers" />
						<li class="headlink" style="width: 105px"><a id="customersMenu"
							href="${customers}" class="link" onclick="highLightTab(1);">
								<spring:url value="/themes/default/images/facilities.png"
									var="imgcustomers" />
								<div class='imgicon'>
 									<img src="${imgcustomers}" />
 								</div>
 								<div class='menutxt'>
 									<spring:message code="menu.customers" />
 								</div>
 						</a></li>
						
						
						<li class="headlink" style="width: 140px"><a id="2" href="#" class="link"> <spring:url
										value="/themes/default/images/admin.png" var="imgadmin" />
									<div class='imgicon'>
										<img src="${imgadmin}" />
									</div>
									<div class='menutxt'>
										<spring:message code="menu.administration" />
									</div>
							</a>
							<ul id="subMenu">
							<security:authorize access="hasAnyRole('Admin','SystemAdmin','SupportAdmin','SPPA')">
							<spring:url value="/eminstance/listCustomerEms.ems?customerId=${customerId}" var="customerEmList" />
							<li><a href="${customerEmList}" id="facilityMappingMenu">Facility Mapping</a></li>
							</security:authorize>
							</ul> 
						</li>
						
						
						<li class="headlink" style="width: 65px"><spring:url
								value="/themes/default/images/help.png" var="imghelp" /> 
							<a	id="3" href="#" class="link" onclick="showhelp();">
									<div class='imgicon'>
										<img src="${imghelp}" />
									</div>
									<div class='menutxt'>
										<spring:message code="header.help" />
									</div>
							</a>
						</li>
						
						<li class="headlink noright" style="width: 80px"><spring:url
								value="/j_spring_security_logout" var="logout_url" /> <spring:url
								value="/themes/default/images/logout.png" var="imglogout" /> 
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