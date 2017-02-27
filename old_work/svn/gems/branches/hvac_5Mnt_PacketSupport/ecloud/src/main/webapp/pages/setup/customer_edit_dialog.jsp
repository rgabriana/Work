<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/customer/list/" var="checkForDuplicateCustomerNameUrl" scope="request" />

<style>
	#edit_customer{padding:10px 15px;}
	#edit_customer table{width:100%;}
	#edit_customer td{padding-bottom:3px;}
	#edit_customer td.fieldLabel{width:40%; font-weight:bold;}
	#edit_customer td.fieldValue{width:60%;}
	#edit_customer .inputField{width:100%; height:20px;}
	#edit_customer #saveUserBtn{padding: 0 10px;}
	#edit_customer .M_M{display: none;}
	#edit_customer .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}    
</style>

<script type="text/javascript">
	
	$(document).ready(function(){
		
		$.validator.addMethod('positiveNumber',
			    function (value) { 
					return Number(value) >= 0;;
			    }, 'Please enter a positive Number.');
		
		$("#edit_customer").validate({
			rules: {
				name: {
					required: true,
				},
				address: {
					required: true,
				},	
				email: {
					required: true,
					email: true,
				},	
				contact: {
					required: true,
				}
			},
			messages: {
				name: {
					required: '<spring:message code="error.above.field.required"/>',
				},
				address: {
					required: '<spring:message code="error.above.field.required"/>',
				},
				email: {
					required: '<spring:message code="error.above.field.required"/>',
				},
				contact: {
					required: '<spring:message code="error.above.field.required"/>',
				}
			}
		});
		
		//add click handler
		$('#saveCustomerBtn').click(function(){checkForDuplicateCustomerName();});
		
	});
	
//Global variable for accessing current customer id
var CUSTOMER_ID = "${customer.id}";

function checkForDuplicateCustomerName(){
	
	$.ajax({
		type: 'GET',
		url: "${checkForDuplicateCustomerNameUrl}"+$('#name').val().trim()+"?ts="+new Date().getTime(),
		data: "",
		success: function(data){
			if(data == null){
				$('#edit_customer').submit();
			}else{
				if(CUSTOMER_ID != data.id){
					alert("Customer with the same name already exists");
				}else{
					$('#edit_customer').submit();
				}
			}
		},
		dataType:"json",
		contentType: "application/json; charset=utf-8"
	});	
}
	
</script>


<div>
	<spring:url value="/saveCustomer.ems" var="submit" scope="request"/>
	<form:form id="edit_customer" commandName="customer" method="post" 
		action="${submit}">
		<form:hidden path="id" />
		<table>
			<tr>
				<td class="fieldLabel"><spring:message code="customer.name" />*</td>
				<td class="fieldValue"><form:input class="inputField" id="name" name="name" size="40" path="name" /></span></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="customer.address" />*</td>
				<td class="fieldValue"><form:input class="inputField" id="address" name="address" size="40" path="address" /></span></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="customer.email" />*</td>
				<td class="fieldValue"><form:input class="inputField" id="email" name="email" size="40" path="email" /></span></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="customer.contact" />*</td>
				<td class="fieldValue"><form:input class="inputField" id="contact" name="contact" size="40" path="contact" /></span></td>
			</tr>
			<tr>
				<td class="fieldLabel"></td>
				<td class="fieldValue"><input class="saveAction" type="button" id="saveCustomerBtn" value="<spring:message code='action.save'/>"></input></span></td>
			</tr>
		</table>
	</form:form>
</div>



