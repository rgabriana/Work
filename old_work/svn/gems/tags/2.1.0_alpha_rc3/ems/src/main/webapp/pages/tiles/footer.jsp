<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<script type="text/javascript">
	$(document).ready(function() {
		//define configurations for dialog
		var dialogOptions = {
			title : "Enlighted Energy Manager",
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
		
		$('#userNameTextBox').focus();
	});
</script>

<div id="notesDialog">
	<table border="0" cellpadding="0" cellspacing="5">
		<tr>
			<td><b>Corporate Headquarters:</b></td>
		</tr>
		<tr>
			<td>930 Benecia Avenue<br />
				Sunnyvale, CA 94085<br /> Phone: 650.964.1094<br /> Fax:
				650.964.1094<br /> Email: info@enlightedinc.com
			</td>
		</tr>
		<tr>
			<td style="padding-top:5px;"><b>For Sales:</b></td>
		</tr>
		<tr>
			<td>Email: sales@enlightedinc.com<br /> Phone:650.964.1155</td>
		</tr>
		<tr>
			<td style="padding-top:5px;"><b>For Customer Service:</b></td>
		</tr>
		<tr>
			<td>Email: support@enlightedinc.com<br />
				Phone: 650.964.1155 or 866-377-4111
			</td>
		</tr>		
	</table>
</div>

<spring:url value="/doc/EULA.pdf" var="eula" />
<div style="float:left;width:100%">
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
</div>