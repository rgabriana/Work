<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
		<title>Enlighted Inc</title>
	</head>
	<body>
		<div style="width: 100%;">
			<div id="header" style="width:95%; min-width:800px; margin: 0 auto;">
				<tiles:insertAttribute name="header" ignore="true" />
			</div>
			<div style="width: 95%; margin: 0 auto;">
				<tiles:insertAttribute name="body" ignore="true" />
			</div>
			<div style="width: 95%; margin: 0 auto;">
				<tiles:insertAttribute name="footer" ignore="true" />
			</div>
		</div>
	</body>
</html>