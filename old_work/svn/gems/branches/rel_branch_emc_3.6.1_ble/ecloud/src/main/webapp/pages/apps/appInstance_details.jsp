<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<style>
	#create_appInstance{padding:10px 15px;}
	#create_appInstance table{width:100%;}
	#create_appInstance td{padding-bottom:3px;}
	#create_appInstance td.fieldLabel{width:40%; font-weight:bold;}
	#create_appInstance td.fieldValue{width:60%;}
	#create_appInstance .inputField{width:100%; height:20px;}
	#create_appInstance #saveBtn{padding: 0 10px;}
	#create_appInstance .M_M{display: none;}
	#create_appInstance .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}		
}	
	  
</style>

<script type="text/javascript">

$(document).ready(function(){
	
	$.validator.addMethod('positiveInteger',
		    function (n) { 
		        return 0 === n % (!isNaN(parseFloat(n)) && 0 <= ~~n);
		    }, 'Please enter a positive Integer.');
	
	$.validator.addMethod('positiveNumber',
		    function (value) { 
				return Number(value) >= 0;;
		    }, 'Please enter a positive Number.');
	
	$("#create_appInstance").validate({
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
			ipAddress: {
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
			version: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			macId: {
				required: '<spring:message code="error.above.field.required"/>',
			},			
			ipAddress: {
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
	
	function saveAppInstance(){
			$("#create_appInstance").submit();
	}
	
	function closeDialog(){
		$("#appInstanceDetailsDialog").dialog("close");
	}
	
</script>

<div>
	<spring:url value="/appinstance/save.ems" var="actionURL" scope="request" />
	<form:form id="create_appInstance" commandName="appInstance" method="post" 
		action="${actionURL}">
		<form:hidden path="id" id="id" name="id" />
		<input type="hidden" id="customerId" name="customerId" value="${customerId}" />
		<table>
			<tr>
				<td class="fieldLabel">Name*</td>
				<td class="fieldValue"><form:input class="inputField" id="name" name="name" size="40" path="name" />
			</tr>
			<tr>
				<td class="fieldLabel">Mac Id*</td>
				<td class="fieldValue"><form:input class="inputField" id="macId"
					name="macId" size="40" path="macId"/></td>
			</tr>
			<tr>
				<td class="fieldLabel">App Commissioned Date*</td>
				
				<fmt:formatDate value="${appInstance.appCommissionedDate}"  
                type="date" 
                pattern="MM/dd/yyyy"
                var="theFormattedDate" />
				<td class="fieldValue"><form:input class="inputField" id="appCommissionedDate" name="appCommissionedDate"  path="appCommissionedDate" value="${theFormattedDate}" type="text" size="40" /></td>
			</tr>			
			<tr>
				<td class="fieldLabel">IP Address*</td>
				<td class="fieldValue"><form:input class="inputField" id="ipAddress"
					name="ipAddress" size="40" path="ipAddress"/></td>
			</tr>		
			
			<c:if test="${mode == 'edit'}">
			<tr id="sshTunnelEnabledRow">
				<td class="fieldLabel">Enable SSH Tunnel</td>
				<td class="fieldValue">
				 <form:checkbox path="openSshTunnelToCloud" id="sshTunnelEnabled" />
	            </td>
            </tr>
		
			</c:if>
			<tr>
				<td />
				<td><input id="saveBtn" type="button"
					value="<spring:message code="action.save" />" onclick="saveAppInstance();">&nbsp;
					<input type="button" id="btnClose"
					value="<spring:message code="action.cancel" />" onclick="closeDialog()">	
				</td>
			</tr>
			
		</table>
	</form:form>
</div>