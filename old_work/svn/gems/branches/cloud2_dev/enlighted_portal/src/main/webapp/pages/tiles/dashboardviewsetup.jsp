<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><spring:message code="title" /></title>

<spring:url value="/themes/default/css/style.css" var="stylecss" />
<link rel="stylesheet" type="text/css" href="${stylecss}" />


<spring:url
	value="/themes/standard/css/jquery/jquery-ui-1.8.16.custom.css"
	var="jquerycss" />
<link rel="stylesheet" type="text/css" href="${jquerycss}" />

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

<spring:url value="/scripts/jquery/jquery.1.6.4.min.js" var="jquery"></spring:url>
<script type="text/javascript" src="${jquery}"></script>

<spring:url value="/scripts/jquery/jquery.ui.1.8.16.custom.min.js"
	var="jqueryui"></spring:url>
<script type="text/javascript" src="${jqueryui}"></script>

<spring:url value="/scripts/jquery/jquery.validate.1.9.min.js"
	var="jqueryvalidate"></spring:url>
<script type="text/javascript" src="${jqueryvalidate}"></script>

<spring:url value="/scripts/jquery/jquery.layout-latest.js"
	var="jquerylayout"></spring:url>
<script type="text/javascript" src="${jquerylayout}"></script>

<!--<spring:url value="/scripts/jquery/jquery.flash.js" var="jquery_flash"></spring:url>
<script type="text/javascript" src="${jquery_flash}"></script>-->

<spring:url value="/scripts/jquery/jquery.cookie.20110708.js" var="jquery_cookie"></spring:url>
<script type="text/javascript" src="${jquery_cookie}"></script>

<spring:url value="/scripts/jquery/jqgrid/grid.locale-en.js" var="jqueryLocaleEn"></spring:url>
<script type="text/javascript" src="${jqueryLocaleEn}"></script>

<spring:url value="/scripts/jquery/jqgrid/jquery.jqgrid.4.2.0.min.js" var="jqueryJqGrid"></spring:url>
<script type="text/javascript" src="${jqueryJqGrid}"></script>

<spring:url value="/scripts/enlighted/facilities.js" var="facilities"></spring:url>
<script type="text/javascript" src="${facilities}"></script>

<script type="text/javascript">
    var myLayout;

    pageLayout_settings = {
        name : "outerLayout",
        paneClass : "pane",
        applyDefaultStyles : true,
        initClosed : false,
        north__initClosed : false,
        north__spacing_open : 0,
        south__spacing_open : 0,
        south__size : 50
    };

    layoutSettings_Inner = {
        applyDefaultStyles : true
    };

    $(document).ready(function() {
        myLayout = $('body').layout(pageLayout_settings);

		$(window).resize(); //To refresh/recalculate height and width of all regions
    });
</script>

</head>
<body>
		<div id="header" class="ui-layout-north"
			onmouseover="myLayout.allowOverflow('north')"
			onmouseover="myLayout.allowOverflow('floorplan')"
			onmouseout="myLayout.resetOverflow(this)"
			style="width: 100%; margin: 0 auto;">
			<tiles:insertAttribute name="header" ignore="true" />
		</div>
		<div id="left" class="ui-layout-west"
			style="width: 100%; margin: 0 auto;">
			<tiles:insertAttribute name="left" ignore="true" />
		</div>
		
		<div id="right" class="ui-layout-center">		
			<tiles:insertAttribute name="body" ignore="true" />		
		</div>
		<div id="footer" class="ui-layout-south"
			style="width: 100%; margin: 0 auto;">
			<tiles:insertAttribute name="footer" ignore="true" />
		</div>
	
</body>
</html>