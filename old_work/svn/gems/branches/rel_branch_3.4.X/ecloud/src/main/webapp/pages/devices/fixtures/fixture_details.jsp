<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="uem" uri="/WEB-INF/tlds/ecloud.tld"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Fixture Details</title>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
	
	.fixturedetailstabsStyle
	{
		height: 90%;
	}
</style>

<spring:url value="/devices/fixtures/fixture_form.ems" var="fixtureFormUrl" scope="request" />

<script type="text/javascript">
IS_LOADED_OVERVIEW = false;

fixtureId = "${fixtureId}";
pid = "${pid}";

$("#fixture-details-tabs").tabs({
		selected: 0,
		create: function(event, ui) {
			//load overview tab content
			$("#tab-overview").load("${fixtureFormUrl}?fixtureId="+fixtureId+"&pid="+pid+"&ts="+ new Date().getTime());
			IS_LOADED_OVERVIEW = true;
		},
		select: function(event, ui) {
			switch(ui.index) {
				case 0://Overview tab selected
// 					  if(!IS_LOADED_OVERVIEW){
						  	$("#tab-overview").load("${fixtureFormUrl}?fixtureId="+fixtureId+"&pid="+pid+ "&ts="+ new Date().getTime());
							IS_LOADED_OVERVIEW = true;
// 					  }
					  break;
				default:
			}
		}
	});

</script>
</head>
<body>

<div id="fixture-details-tabs" class="fixturedetailstabsStyle" >
	<ul>
		<li><a href="#tab-overview">Overview</a></li>
	</ul>
	
	<div id="tab-overview">&nbsp;&nbsp;<spring:message code='action.loading'/></div>
	
</div>

</body>
</html>