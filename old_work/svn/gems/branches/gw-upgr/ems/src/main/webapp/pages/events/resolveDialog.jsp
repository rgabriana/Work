<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>


<spring:url value="/services/events/resolve" var="resolveEvents" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<style>
	table#apf-wrapper-table td {padding: 0 20px}
	td#apf-form-container div.fieldLabel{float:left; width:35%; font-weight:bold;}
 	td#apf-form-container div.fieldValue{float:left; width:65%;}
	#apf-message-div {font-weight:bold; float: left;}
</style>

<script type="text/javascript">

function submitForm(){
	if($("#comment").val() == "") {
		$("#comment").val(" ");
	}
	$.ajax({
		type: "POST",
		url: "${resolveEvents}" + "/" + encodeURIComponent('<%=request.getParameter("ids")%>')  + "/" + encodeURIComponent($("#comment").val()),
		dataType: "html",
		success: function(msg) {
			dialogResult = msg;
			$("#commentDialog").dialog("close");
		},
		error: function() {
			dialogResult = "E";
			$("#commentDialog").dialog("close");
		}
	});	
}


function cancelDialog(){
	$("#commentDialog").dialog("close");
}

</script>
</head>
<body id="apf-main-box">

<table id="apf-wrapper-table" width=100% height=100%>
	<tr>
		<td id="apf-form-container" valign="top">
			<div class="fieldLabel">Comment :</div>
			<div class="fieldValue">
				<textarea id="comment" name="comment" cols="37" rows="4"></textarea>
			</div>
		</td>
	</tr>
	<tr>
		<td height=auto align="right">
			<button id="apf-apply-btn" onclick="submitForm();">Submit</button>
			<button id="apf-cancel-btn" onclick="cancelDialog();">Cancel</button>
		</td>
	</tr>
</table>

</body>
</html>