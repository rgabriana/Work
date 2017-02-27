<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ecloud" uri="/WEB-INF/tlds/ecloud.tld"%>
<%@ taglib prefix="security"
	uri="http://www.springframework.org/security/tags"%>
<spring:url value="/facilities/home.ems" var="customerFacilitiesUrl" />	
<spring:url value="/themes/default/css/menu.css" var="menucss" />
<link rel="stylesheet" type="text/css" href="${menucss}" />
<style type="text/css">
html,body {
	margin: 0px !important;
}
</style>

<script type="text/javascript"> 
$.ajaxSetup({
   cache: true
 });
</script>
<spring:url	value="/j_spring_security_logout" var="logout_url" />
<script type="text/javascript">
</script>

	<script type="text/javascript">
		$(document).ready(function(){
		});
		
		function callLogOut()
		{
			window.location = "${logout_url}"
		}
		function returnToFacility(){
			var customerId= "${customerId}";
			$('#reportheaderCustomerId').val(customerId);
			$('#reportheaderForm').submit();	
		}
	</script>

	<table id="tblcompcreated" border="0" cellpadding="0" cellspacing="0"
		class="headertbl">
		<tr>
			<td align="left"><spring:url
					value="/themes/default/images/enlighted-logo.png" var="imglogo"  /> <img
				src="${imglogo}" style='padding-top: 4px;width: 25%;height: 25%;padding-left: 10px;' /></td>
			<td width="22%">
			</td>
			<td align="right"><input type="button" value="Back to facility"  class="reportbtn" onclick="returnToFacility()"/>&nbsp;&nbsp;<input type="button" value="Logout" class="reportbtn" onclick="callLogOut()"/></td>
		</tr>
		
					
								
	</table>
<form id="reportheaderForm" action="${customerFacilitiesUrl}" METHOD="POST">
		<input id="reportheaderCustomerId" name="customerId" type="hidden"/>
	</form>
