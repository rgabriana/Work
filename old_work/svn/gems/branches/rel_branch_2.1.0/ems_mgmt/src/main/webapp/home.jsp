<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><spring:message code="title"/></title>

<spring:url	value="/themes/standard/css/jquery/jquery-ui-1.8.16.custom.css"	var="jquerycss" />
<link rel="stylesheet" type="text/css" href="${jquerycss}" />

<spring:url value="/scripts/jquery/jquery.1.6.4.min.js" var="jquery"></spring:url>
<script type="text/javascript" src="${jquery}"></script>

<spring:url value="/scripts/jquery/jquery.ui.1.8.16.custom.min.js" var="jqueryui"></spring:url>
<script type="text/javascript" src="${jqueryui}"></script>

<script type="text/javascript">

$().ready(
	function() {
		var page = '<%=request.getParameter("page")%>' ;
		var code = '<%=request.getParameter("code")%>' ;
		$.ajax({
			type : "POST",
			cache : false,
			dataType : "html",
			url : location.protocol + '//' + location.host + '/ems/services/system/emsmgmt/validate/' + code,
			async : false,
			beforeSend : function() {
			},
			success : function(data) {
				if(data != "F") {
					$("#page").val(page);
					$("#code1").val(code);
					$("#code2").val(data);
					$("#navigatePage").submit();
				}
				else {
					$("#navigateError").submit();
				}
			},
			error : function() {
				$("#navigateError").submit();
			},
			complete : function() {		
			}
		});
		
		
	});
	

</script>

</head>
<body>

<div>

	<form id="navigatePage" method="post" action=<spring:url value="/navigatePage.emsmgmt"/>>
		<input type="hidden" id="page" name="page" /> 
		<input type="hidden" id="code1" name="code1" />
		<input type="hidden" id="code2" name="code2" />
	</form>
	
	<form id="navigateError" action=<spring:url value="/error.emsmgmt"/>>
	</form>
	
</div>


</body>
</html>