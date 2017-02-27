<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<style>
	#activate_appInstance{padding:10px 15px;}
	#activate_appInstance table{width:100%;}
	#activate_appInstance td{padding-bottom:3px;}
	#activate_appInstance td.fieldLabel{width:40%; font-weight:bold;}
	#activate_appInstance td.fieldValue{width:60%;}
	#activate_appInstance .inputField{width:100%; height:20px;}
	#activate_appInstance #saveBtn{padding: 0 10px;}
	#activate_appInstance .M_M{display: none;}
	#activate_appInstance .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}    
</style>

<script type="text/javascript">

$(document).ready(function(){
		
	$("#activate_appInstance").validate({
		rules: {
			name: {
				required: true,
			},
			version: {
				required: true,
			},	
			macId: {
				required: true,
			},
			appCommissionedDate:
			{
				required: true,
			}
		},
		messages: {
			name: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			macId: {
				required: '<spring:message code="error.above.field.required"/>',
			}
		}
	});	
	
	$( "#appCommissionedDate" ).datetimepicker({
		dateFormat: 'mm/dd/yy',
		showTimepicker:false,
		currentText:'Today',
		//maxDate: 0,
	    onClose: function(dateText, inst) {
	    },
	    onSelect: function (selectedDateTime){
	    }
	});
});

	<c:if test="${mode == 'true'}">
	$("#customerCombo").empty();
	<c:forEach items="${customerList}" var="customer">
		$('#customerCombo').append($('<option></option>').val("${customer.id}").html("${customer.name}"));
	</c:forEach>
	</c:if>
	function activateAppInstance(){
		   var mode = "${mode}";
		   var selectedCustomerId =1;
		   if(mode=='true')
		   {
			   selectedCustomerId = $("#customerCombo").val();
		   }
		   $("#customerId").val(selectedCustomerId);
		   $("#activate_appInstance").submit();
	}
	
	function closeDialog(){
		$("#appActivateDialog").dialog("close");
	}
	
</script>

<div>
	<spring:url value="/appinstance/activateApp.ems" var="actionURL" scope="request" />
	<form:form id="activate_appInstance" commandName="appInstance" method="post" 
		action="${actionURL}">
		<form:hidden path="id" />
		<input type="hidden" id="customerId" name="customerId" />
		<table>
			<tr>
				<td class="fieldLabel">Name*</td>
				<td class="fieldValue"><form:input class="inputField" id="name" name="name" size="40" path="name" />
			</tr>
			<tr>
				<td class="fieldLabel">Mac Id*</td>
				<td class="fieldValue"><form:input class="inputField" id="macId"
					name="macId" size="40" path="macId" readonly="true"/></td>
			</tr>
			<tr>
				<td class="fieldLabel">App Commissioned Date*</td>
				
				<fmt:formatDate value="${appInstance.appCommissionedDate}"  
                type="date" 
                pattern="MM/dd/yyyy"
                var="theFormattedDate" />
				<td class="fieldValue"><form:input class="inputField" id="appCommissionedDate" name="appCommissionedDate"  path="appCommissionedDate" value="${theFormattedDate}" type="text" size="40"/></td>
			</tr>
						
			<c:if test="${mode == 'true'}">
			<tr>
				<td class="fieldLabel">Select Customer*</td>
				<td class="fieldValue"><select id="customerCombo"></select></td>
			</tr>
			</c:if>
			
			<tr>
				<td />
				<td><input id="saveBtn" type="button"
					value="Activate" onclick="activateAppInstance();">&nbsp;
					<input type="button" id="btnClose"
					value="<spring:message code="action.cancel" />" onclick="closeDialog()">	
				</td>
			</tr>
		</table>
	</form:form>
</div>