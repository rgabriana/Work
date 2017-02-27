<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>


<script type="text/javascript">
	$(document).ready(function() {
		//define configurations for dialog
		var dialogOptions = {
			title : "enlighted Inc.",
			modal : true,
			autoOpen : false,
			height : 300,
			width : 500,
			draggable : true
		}

		$("#notesDialog").dialog(dialogOptions);

		$('#contactUs').click(function() {
			$("#notesDialog").dialog('open');
			return false;
		});
	});
</script>

<div id="notesDialog">
	<b>Corporate Headquarters:</b><br/>
	1451 Grant Road, Suite 200<br/>
	Mountain View, CA 94040<br/>
	Phone: 650.964.1094<br/>
	Fax: 650.964.1094<br/>
	Email: info@enlightedinc.com<br/>
	<b>For Sales:</b> Email: sales@enlightedinc.com<br/>
	Phone:650.964.1155 <br />
	<b>For Customer Service:</b><br/>
	Email: support@enlightedinc.com<br/>
	Phone: 650.964.1155 or 866-377-4111
</div>

<spring:url value="/doc/EULA.pdf" var="eula" />
<!--
	<div class="footer">
		<div style="vertical-align: middle; float: left"><spring:message code="footer.copyright.text"/></div>
		<div class="contactus" style="float: right">
			<a href="${eula}" target="_blank"><spring:message code="footer.eula.label"/></a>&nbsp;|&nbsp;<a id="contactUs" href="">
			<spring:message code="footer.contact.us.label"/></a>
		</div>
	</div>
-->
<table border="0" cellspacing="0" class="footer">
	<tr>
		<td align="left">
			<spring:message code="footer.copyright.text"/>
		</td>
		<td align="right">
			<a href="${eula}" target="_blank"><spring:message code="footer.eula.label"/></a>&nbsp;|&nbsp;<a id="contactUs" href="">
		<spring:message code="footer.contact.us.label"/></a>
		</td>
	</tr>
</table>
