<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="ems" uri="/WEB-INF/tlds/ems_mgmt.tld"%>

<div>
	<table cellpadding="0" cellspacing="0" width=100%>
		<tr>
			<td align="left">
				<span id="elfgem"><spring:message code="header.title"/></span>
				<b><ems:showAppVersion/></b>	
			</td>
			<td align="center">
				<span id="welcome"><spring:message code="header.welcome"/><%--TODO:Uncomment -> &nbsp;<sec:authentication property="principal.username" /> --%></span>
			</td>
			<td align="right">
				<div id="headerActionContainer">
					<ul id="headerActions">
						<li>
						<spring:url value="/j_spring_security_logout" var="logout_url" />
						<a href="${logout_url}"><spring:message code="header.logout"/></a></li>
					</ul>
				</div>
			</td>
		</tr>
	</table>
</div>
<div class="headerWrapper">
	<a id="mainpage" href="#"><spring:message code="header.back.link"/></a>
</div>

<script>

	var mainpage_url = location.protocol + '//' + location.host + '/ems/home.ems';
	$("#mainpage").attr("href", mainpage_url);

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