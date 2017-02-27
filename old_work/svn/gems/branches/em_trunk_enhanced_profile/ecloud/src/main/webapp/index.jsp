<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/customer/list/" var="getCustomerList" scope="request" />

<spring:url value="/services/org/customer/license/list/" var="getLicenseList" scope="request" />
<spring:url value="/services/org/" var="getLicense" scope="request" />
<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}	
</style>

<style>
.fieldWrapper{padding-bottom:2px;}
.fieldPadding{height:4px;}
.fieldlabel{float:left; height:20px; width: 10%; font-weight: bold;}
.fieldInputCombo{float:left; height:23px; width: 20%;}

.ui-jqgrid-bdiv
{
 overflow-x : hidden !important;
}
.topmostContainer
{
	overflow-x : hidden !important;
	overflow-y : hidden !important;
}

</style>
<script type="text/javascript">
</script>
<div class="topmostContainer">
<div class="outermostdiv">
<div class="outerContainer">


</div>
</div>
</div>
