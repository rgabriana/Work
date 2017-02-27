<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" /> 
</head>
<body>
	<script type="text/javascript">
		$("body").css("margin", "1px 1px");
		$("html").css("height", "99%");
	</script>
	<div id="center" style="width: 100%; height: 100%; padding: 0px;">
		<tiles:insertAttribute name="body" ignore="true" />
	</div>
</body>
</html>