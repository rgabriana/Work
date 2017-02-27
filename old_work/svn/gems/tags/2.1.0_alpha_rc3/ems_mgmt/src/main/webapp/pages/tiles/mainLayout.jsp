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

<spring:url value="/scripts/jquery/jquery.alerts.js"
	var="jqueryalerts"></spring:url>
<script type="text/javascript" src="${jqueryalerts}"></script>

<spring:url value="/themes/standard/css/jquery/jquery.alerts.css"
	var="jqueryalertscss" />
<link rel="stylesheet" type="text/css" href="${jqueryalertscss}" />

</head>
<body>
	<div style="width: 100%;">
		<div id="header" style="width: 100%; min-width: 800px; margin: 0 auto;">
			<tiles:insertAttribute name="header" ignore="true" />
		</div>
		<div id="body" style="width: 100%; margin: 5px auto;">
			<tiles:insertAttribute name="body" ignore="true" />
		</div>
		<div style="width: 100%; margin: 0 auto;">
			<tiles:insertAttribute name="footer" ignore="true" />
		</div>
	</div>
</body>
</html>