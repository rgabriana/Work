<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="ecloud" uri="/WEB-INF/tlds/ecloud.tld"%>
<%@ taglib prefix="security"
	uri="http://www.springframework.org/security/tags"%>

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

<script type="text/javascript">
	function highLightTab(tabId){
		$.cookie('last_menu_cookie', tabId, {path: '/'});
		
		$('a.link').removeClass('highLight');
		$('#'+tabId).addClass('highLight');	
	}
	
</script>

<c:if test="${companySetup != 'true'}">
	<!-- Following table to be shown when company details are available -->
	<script type="text/javascript">
		$(document).ready(function(){
			//highlight last selected menu		
			var TabId=1;
			if ($.cookie('last_menu_cookie') != null ){//if cookie exists--		
				TabId=$.cookie('last_menu_cookie');
			}
			
			highLightTab(TabId);
			
		});
	</script>

   <jsp:useBean id="now" class="java.util.Date" scope="request" />
	<table id="tblcompcreated" border="0" cellpadding="0" cellspacing="0"
		class="headertbl">
		<tr>
			<td align="left"><spring:url
					value="/themes/default/images/att.jpg" var="imglogo" /> <img
				src="${imglogo}" style='padding-top: 4px;width:66px;height:99px;' /></td>
		    <td align="right">Report Period:(From Date) to (To Date)</br>
		        Report Date:<fmt:formatDate type="date" value="${now}" />
		    </td>
			
		</tr>
	</table>
</c:if>