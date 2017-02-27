<%@ taglib prefix="sec"	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems_mgmt.tld"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>


<style type="text/css">
html,body {
	margin: 0px !important;
}
</style>

<script type="text/javascript"> 
	$.ajaxSetup({
	   cache: true
	 });
	
	$(document).ready(function(){
		var mainpage_url = location.protocol + '//' + location.host + '/ems/home.ems';
		$("#mainpage").attr("href", mainpage_url);			
	});
	
</script>

<table id="tblcompcreated" border="0" cellpadding="0" cellspacing="0"
	class="headertbl" >
	<tr>
		<td align="left"><spring:url
				value="/themes/default/images/logo.png" var="imglogo" /> <img
			src="${imglogo}" style='padding-top: 4px' /> 
			<%-- <span id="welcome"	class="welcm"><spring:message code="header.welcome" />&nbsp;
			<sec:authentication property="principal.username" /> </span>--%>
		</td>
		<td align="right">
			<%-- <a id="mainpage" href="#"><spring:message code="header.back.link"/></a> --%>
			<table border="0" cellpadding="0" cellspacing="0" style="border-collapse: collapse;">
				<tr>
					<td>
						<div class="nav">
							<ul id="cssdropdown">
								<li class="headlink noright" >									 
									<spring:url	value="/themes/default/images/back.png" var="imgBack" /> 
									<a id="mainpage" href="#" class="link">
										<div class='imgicon'>
											<img src="${imgBack}" />
										</div>
										<div class='menutxt'>
											<spring:message code="header.back.link"/>
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
		<td></td>
		<td align="right"><ems:showAppVersion />&nbsp;<c:set var="now"
				value="<%=new java.util.Date()%>" /> <spring:message
				code="header.current.server.time" />
			<fmt:formatDate pattern="yyyy-MM-dd HH:mm" value="${now}" /></td>
	</tr>
</table>

