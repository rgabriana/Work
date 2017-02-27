<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="security"
	uri="http://www.springframework.org/security/tags"%>
<div>
	<table cellpadding="0" cellspacing="0" width=100%>
		<tr>
			<td align="left" nowrap="nowrap">				
				<b><ems:showAppVersion/>
				<c:set var="now" value="<%=new java.util.Date()%>"/>
				<spring:message code="header.current.server.time"/><fmt:formatDate pattern="yyyy-MM-dd HH:mm Z" value="${now}"/></b>	
			</td>
			<td align="center">
				<span id="welcome"><spring:message code="header.welcome"/>&nbsp;<sec:authentication property="principal.username" /></span>
			</td>
			<td align="right">
				<div id="headerActionContainer">
					<ul id="headerActions">
						<li><a href="#"><spring:message code="header.change.password"/></a></li>
						<li>|</li>
						<li>
						<spring:url value="/j_spring_security_logout" var="logout_url" />
						<a href="${logout_url}"><spring:message code="header.logout"/></a></li>
					</ul>
				</div>
			</td>
		</tr>
	</table>
</div>


<script>
	var element_time = document.getElementById('divCurrentTime');
	if (element_time != null) {
		var LocalDate = new Date();
		var LocalTime = LocalDate.getTime();
		var LocalOffset = LocalDate.getTimezoneOffset() * 60000;
		var ServerTime = parseInt(LocalTime) + parseInt(LocalOffset);
		var ServerDate = new Date(ServerTime);
		var year = ServerDate.getFullYear();
		var month = parseInt(ServerDate.getMonth() + 1);
		var day = ServerDate.getDate();
		var hours = ServerDate.getHours();
		var minutes = ServerDate.getMinutes();
		var seconds = ServerDate.getSeconds();
		var milliseconds = ServerDate.getMilliseconds();

		element_time.innerHTML = "&nbsp;(<spring:message code='header.current.server.time'/> "
				+ year
				+ "-"
				+ getTwoDigits(month)
				+ "-"
				+ getTwoDigits(day)
				+ " " + getTwoDigits(hours) + ":" + getTwoDigits(minutes) + ")";
	}

	function getTwoDigits(value) {
		if (parseInt(value) < 10)
			return "0" + value;
		else
			return value;
	}
</script>