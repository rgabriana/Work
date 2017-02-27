<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<script type="text/javascript">
	$(document).ready(function(){
		$("#maindiv").html("${auditlog}");
	});
</script>

<div class="outermostdiv" style="margin-left:0px;">
	<div class="outerContainer">
		<span><spring:message code="menu.auditlog" /></span>
	</div>
	<div id="maindiv" style="padding:5px;">
		
	</div>		
</div>
 