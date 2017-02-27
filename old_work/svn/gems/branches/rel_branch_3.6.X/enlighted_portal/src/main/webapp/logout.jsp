<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>

<spring:url value="/login.jsp" var="loginUrl"></spring:url>
<script type="text/javascript">
<!--
window.parent.location = "${loginUrl}"
//-->
</script>