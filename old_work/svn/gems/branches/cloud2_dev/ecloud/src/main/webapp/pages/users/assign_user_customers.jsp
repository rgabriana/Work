<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/users/assigncustomers" var="assignCustomersToUsersUrl" scope="request" />

<script type="text/javascript">
var USER_ID;
$(document).ready(function() {
	USER_ID	= "${userid}";		
	});

function applyUserToCustomers()
{
var mVar = "";
var cb = "";
<c:forEach items="${customerlist}" var="customer">   
		cb = document.getElementById(${customer.customerId});   
		if(cb.checked==true && mVar=="")
		{  
		mVar = "${customer.customerId}";
		}
		else if(cb.checked==true)
		{
		mVar = mVar + "," + "${customer.customerId}";
		}
</c:forEach>

if(mVar=="")
{
mVar = "-1";
}
$.ajax({
		type: 'POST',
		url: "${assignCustomersToUsersUrl}/"+USER_ID+"/"+mVar,		
		success: function(data){
				alert("Assignment of customers done successfully");
		},		
		contentType: "application/xml; charset=utf-8"
	});	
}

</script>
<div id="customerlistDiv">
<button id="assignButton" onclick="javascript: applyUserToCustomers();">Assign Customers</button>
<div style="height: 300px; overflow: auto"> 
<table id="all_customers_list" class="entable" style="width: 100%">
<thead>
			<tr>
				<th align="center">Select</th>
				<th align="center">Customer Name</th>			
			</tr>
</thead>

<c:forEach items="${customerlist}" var="customer">
				<tr>
				<input type="hidden" name="tenantId" value="${customer.customerId}"/>
				<c:if test="${customer.selected == 'true'}">
				<td><input type="checkbox" id="${customer.customerId}" name="" value="${customer.customerId}" checked="true"></td>
				</c:if>
				<c:if test="${customer.selected == 'false'}">
				<td><input type="checkbox" id="${customer.customerId}" name="${customer.customerId}" value="${customer.selected}"></td>
				</c:if>
				<td><label for="${customer.customerName}">${customer.customerName}</label></td>			
			</tr>
</c:forEach>

</table>
</div>