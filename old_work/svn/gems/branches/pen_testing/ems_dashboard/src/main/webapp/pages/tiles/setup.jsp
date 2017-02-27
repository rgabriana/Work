<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />

</head>
<body>

	<div style="width: 100%;">
		<div id="header"
			style="width: 100%; min-width: 800px; margin: 0 auto;">
			<tiles:insertAttribute name="header" ignore="true" />
		</div>
		<div style="width: 100%; margin: 0 auto;">
			<tiles:insertAttribute name="body" ignore="true" />
		</div>
		<c:if test="${mode != 'admin'}">
			<div style="width: 100%; margin: 0 auto;">
				<tiles:insertAttribute name="footer" ignore="true" />
			</div>
		</c:if>
	</div>
</body>
</html>