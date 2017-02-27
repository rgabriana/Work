<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>


<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
</style>

<script type="text/javascript">
	$(document).ready(function() {
	//define configurations for dialog
		var dialogOptions = {
			title : "Enlighted Virtuo",
			modal : true,
			height : 300,
			width : 500,
			draggable : true,
			close : function(ev, ui){
				$(this).dialog("destroy");
			}
		}

		$('#contactUs').click(function() {
			$("#notesDialog").html("<b>Corporate Headquarters:</b><br/>"+
					"1451 Grant Road, Suite 200<br/>"+
					"Mountain View, CA 94040<br/>"+
					"Phone: 650.964.1094<br/>"+
					"Fax: 650.964.1094<br/>"+
					"Email: info@enlightedinc.com<br/>"+
					"<b>For Sales:</b> Email: sales@enlightedinc.com<br/>"+
					"Phone:650.964.1155 <br />"+
					"<b>For Customer Service:</b><br/>"+
					"Email: support@enlightedinc.com<br/>"+
					"Phone: 650.964.1155 or 866-377-4111");
			
			$("#notesDialog").dialog(dialogOptions);
			return false;
		});
	});
</script>

<div id="notesDialog"></div>

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