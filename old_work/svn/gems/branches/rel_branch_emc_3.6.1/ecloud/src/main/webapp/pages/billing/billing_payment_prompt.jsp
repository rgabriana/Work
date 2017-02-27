<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<spring:url value="/bill/updateBillPayment.ems" var="updateBillPaymentURL" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<style type="text/css">

#formContainer .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}    
</style>
<script type="text/javascript">

var custId;
var isFormValid = true;

$(document).ready(function() {
	custId = "${customerId}";
});

function updateCustomerBillPayment() {
	
	$("#validationMsg").text("");
	var paymentAmount = $("#billPaymentAmt").val();
	$('#bppfCustomerId').val(custId);
	$('#bppfPaymentAmt').val(paymentAmount);
	if(paymentAmount!=null && paymentAmount>0)
	{
		isFormValid = true;
	}else
	{
		isFormValid = false;
	}
	if(isFormValid)
	{
		$('#billPaymentPromtForm').submit();
		$("#BillPaymentDialog").dialog("close");
	}else
	{
		$("#validationMsg").text('Please enter valid payment amount');
		return false;
	}
}

</script>

</head>
<body>

<div class="innerContainer">
	<div class="formContainer">
		<div class="field">
			<span id="validationMsg" class="error"></span>
			<div class="formPrompt"><span>Payment Date:</span></div>
			<div class="formValue"><fmt:formatDate type="date" value="${paymentDate}" /></div>
		</div>
		<div class="field">
			<div class="formPrompt"><span>Payment Amount:</span></div>
			<div class="formValue"><input id="billPaymentAmt" name="billPaymentAmt" type="text" /></div>
		</div>
		<div class="field">
			<div class="formValue" style="">
				<div class="formPrompt"><span></span></div>
				<button style="display: inline;"  id="billPayBtn" onclick="updateCustomerBillPayment()">Submit</button>
			</div>
		</div>
	</div>
</div>

<form id="billPaymentPromtForm" action="${updateBillPaymentURL}" METHOD="POST">
	<input id="bppfCustomerId" name="bppfCustomerId" type="hidden"/>
	<input id="bppfPaymentAmt" name="bppfPaymentAmt" type="hidden"/>
</form>

</body>
</html>
