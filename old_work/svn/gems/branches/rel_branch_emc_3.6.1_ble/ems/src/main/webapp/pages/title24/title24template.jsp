<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><spring:message code="title"/></title>

<spring:url value="/scripts/jquery/jquery-1.11.0.min.js" var="jquery1110"></spring:url><script type="text/javascript" src="${jquery1110}"></script>
<spring:url value="/scripts/jquery/jqgrid/5.0.2/js/i18n/grid.locale-en.js" var="jqGridLocale502"></spring:url><script type="text/javascript" src="${jqGridLocale502}"></script>
<spring:url value="/scripts/jquery/jqgrid/5.0.2/js/jquery.jqGrid.min.js" var="jqGrid502"></spring:url><script type="text/javascript" src="${jqGrid502}"></script>
<spring:url value="/scripts/jquery/jqgrid/5.0.2/css/ui.jqgrid.css" var="jqgridUICss502" /><link rel="stylesheet" type="text/css" href="${jqgridUICss502}" />
<spring:url value="/scripts/jquery/jqgrid/5.0.2/themes/jqModal.css" var="jqgridjqModal502" /><link rel="stylesheet" type="text/css" href="${jqgridjqModal502}" />

<spring:url value="/scripts/enlighted/title24DR.js" var="title24drjs"></spring:url><script type="text/javascript" src="${title24drjs}"></script>
<spring:url value="/scripts/enlighted/title24Autodaylight.js" var="title24autodaylightjs"></spring:url><script type="text/javascript" src="${title24autodaylightjs}"></script>
<spring:url value="/scripts/enlighted/title24.js" var="title24js"></spring:url><script type="text/javascript" src="${title24js}"></script> 
<spring:url value="/scripts/jquery/jquery-ui.min.css" var="jqueryuimin" /><link rel="stylesheet" type="text/css" href="${jqueryuimin}" />
<spring:url value="/scripts/jquery/jquery-ui.min.js" var="jqueryuijs"></spring:url><script type="text/javascript" src="${jqueryuijs}"></script>
<spring:url value="/themes/title24/style.css" var="title24stylecss" /><link rel="stylesheet" type="text/css" href="${title24stylecss}" /> 
<spring:url value="/themes/title24/title24.css" var="title24css" /><link rel="stylesheet" type="text/css" href="${title24css}" />

 
 
<!-- 
<spring:url value="/themes/default/css/style.css" var="stylecss" />
<link rel="stylesheet" type="text/css" href="${stylecss}" />

<spring:url value="/themes/standard/css/jquery/jquery-ui-1.8.16.custom.css"	var="jquerycss" />
<link rel="stylesheet" type="text/css" href="${jquerycss}" />

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

 -->
 <spring:url value="/scripts/jquery/jquery.validate.1.9.min.js" var="jqueryvalidate"></spring:url>
<script type="text/javascript" src="${jqueryvalidate}"></script>
 
<spring:url value="/scripts/jquery/jquery-migrate-1.2.1.js"	var="jquerymigrate"></spring:url>
<script type="text/javascript" src="${jquerymigrate}"></script>

  
<spring:url value="/scripts/jquery/jquery.layout-latest-1.4.0.js"	var="jquerylayout"></spring:url>
<script type="text/javascript" src="${jquerylayout}"></script>

<script type="text/javascript">
	var myLayout;

	pageLayout_settings = {
		name: "outerLayout",
		paneClass: "pane", 	
		applyDefaultStyles : true,
		initClosed : false,
		north__initClosed : false,
		north__spacing_open: 0,
		south__spacing_open: 0,
		south__size : 50
	};
	
	layoutSettings_Inner = {
		applyDefaultStyles: true
	};
	$(document).ready(function() {
		myLayout = $('body').layout(pageLayout_settings);
		
		$('#header').mouseover(function() {
        	myLayout.allowOverflow('north');  
        	myLayout.allowOverflow('floorplan');
        });
        
        
        $('#header').mouseout(function() {
        	myLayout.resetOverflow(this);  
        });
		
	});
</script>

</head>
<body>
	<div id="header" class="ui-layout-north"
		style="width: 100%; margin: 0 auto;">
		<tiles:insertAttribute name="header" ignore="true" />
	</div>
	<div id="center" class="ui-layout-center" style="width: 100%; margin: 0 auto;">
		<tiles:insertAttribute name="body" ignore="true" />
	</div>
	<div id="footer" class="ui-layout-south" style="width: 100%; margin: 0 auto;">
		<tiles:insertAttribute name="footer" ignore="true" />
	</div>
</body>
</html>