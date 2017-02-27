<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!-- THIS PAGE NOT REQUIRED AS CODE MOVED TO presensedashboard.jsp -->
<html>
    <head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	 <spring:url value="/themes/default/css/enlighted-d3.css" var="enlightedd3css"></spring:url>
	 <link rel="stylesheet" type="text/css" href="${enlightedd3css}" />
	 <script type="text/javascript" src="https://www.google.com/jsapi"></script>   
	 <style type="text/css">
 	</style>       
<script type="text/javascript">
	var lpath;
	$(document).ready(function() {
		lpath=parent.path;
		$("#breadscrumHeader").text(lpath);
	});
</script>	

<script type="text/javascript">
	google.load("visualization", "1", {packages:["map"]});
    google.setOnLoadCallback(drawChart);
    
</script>

</head>

<body>
    <div style="padding-top: 2px;font-weight: bold;"><span id="breadscrumHeader"></span></div>
    <div id="map_div" style="width: 100%; height: 100%"></div>
</body>
</html>