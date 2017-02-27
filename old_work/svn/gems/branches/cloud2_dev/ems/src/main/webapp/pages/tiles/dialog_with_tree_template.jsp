<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><spring:message code="title" /></title>
</head>
<body>
	<script type="text/javascript">
		$("body").css("margin", "1px 1px");
		$("html").css("height", "99%");
	</script>
	<div id="header" class="ui-layout-north"
		style="width: 100%; margin: 0 auto;">
		<tiles:insertAttribute name="header" ignore="true" />
	</div>
	<!-- <div id="center" class="ui-layout-center" style="width: 100%; margin: 0 auto;">
		<tiles:insertAttribute name="body" ignore="true" />
	</div>-->
	
	<div id="left" class="ui-layout-west"
		style="width: 30%; height: 100%; margin: 0 auto;">
		<tiles:insertAttribute name="left" ignore="true" />
	</div>
	<div id="right" class="ui-layout-center">
		<tiles:insertAttribute name="body" ignore="true" />
	</div>
</body>
</html>