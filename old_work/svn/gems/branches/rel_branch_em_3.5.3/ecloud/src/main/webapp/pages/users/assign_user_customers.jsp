<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/users/assigncustomers" var="assignCustomersToUsersUrl" scope="request" />

<script type="text/javascript">
var USER_ID;
$(document).ready(function() {
	USER_ID	= "${userid}";
	var dailogTitle = "Assign Customers to User " + "${userName}";
	$("#assignCustomersDialog").dialog('option', 'title', dailogTitle);
	
});

function applyUserToCustomers()
{
	var mVar = "";
		
	$("input[type='checkbox']:checked").each( 
		    function() { 
		       if(mVar == ""){
		    		mVar = $(this).val();
		    	}else{
		    		mVar = mVar + "," + $(this).val();
		    	}
		    } 
	);
	
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
<div style="height: 10px"/> 
<button id="assignButton" onclick="javascript: applyUserToCustomers();" style="padding-left:5px;">Assign Customers</button>
<div style="height: 10px"/> 
<table id="all_customers_list" class="entable" style="width: 100%;padding-left:5px;">
<thead>
			<tr>
				<th align="center">Select</th>
				<th align="center">Customer Name</th>			
			</tr>
</thead>

<c:forEach items="${customerlist}" var="customer">
			<tr>
				<c:if test="${customer.selected == 'true'}">
				<td><input type="checkbox" id="${customer.customerId}" value="${customer.customerId}" checked></td>
				</c:if>
				<c:if test="${customer.selected == 'false'}">
				<td><input type="checkbox" id="${customer.customerId}" value="${customer.customerId}"></td>
				</c:if>
				<td><label>${customer.customerName}</label></td>			
			</tr>
</c:forEach>
</table>
</div>