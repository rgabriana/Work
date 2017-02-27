<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><spring:message code="title"/></title>


<spring:url value="/themes/default/css/style.css" var="stylecss" />
<link rel="stylesheet" type="text/css" href="${stylecss}" />


<spring:url value="/themes/standard/css/jquery/jquery-ui-1.8.16.custom.css"
	var="jquerycss" />
<link rel="stylesheet" type="text/css" href="${jquerycss}" />

<spring:url value="/scripts/jquery/jquery.1.6.4.min.js" var="jquery"></spring:url>
<script type="text/javascript" src="${jquery}"></script>

<spring:url value="/scripts/jquery/jquery.ui.1.8.16.custom.min.js"
	var="jqueryui"></spring:url>
<script type="text/javascript" src="${jqueryui}"></script>

<spring:url value="/scripts/jquery/jquery.validate.1.9.min.js"
	var="jqueryvalidate"></spring:url>
<script type="text/javascript" src="${jqueryvalidate}"></script>

</head>
<body>
	<c:if test="${mode == 'admin'}">
	<script type="text/javascript">
		$("body").css("margin", "1px 1px");
		$("html").css("margin", "1px 1px");
	</script>
	</c:if>
	<div style="width: 100%;">
		<c:if test="${mode != 'admin'}">
			<div id="header" style="width: 100%; min-width: 800px; margin: 0 auto;">
				<tiles:insertAttribute name="header" ignore="true" />
			</div>
		</c:if>
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