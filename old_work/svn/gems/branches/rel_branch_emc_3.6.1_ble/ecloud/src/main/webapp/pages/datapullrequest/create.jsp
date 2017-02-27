<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<style>
	#requestForm{padding:10px 15px;}
	#requestForm table{width:100%;}
	#requestForm td{padding-bottom:3px;}
	#requestForm td.fieldLabel{width:30%; font-weight:bold;}
	#requestForm td.fieldValue{width:60%;}
	#requestForm .inputField{width:100%; height:20px;}
	#requestForm #createBtn{padding: 0 10px;}
	#requestForm .M_M{display: none;}
	#requestForm .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}  
	label.error {color:red;}  
</style>

<script type="text/javascript">
		
	$(document).ready(function(){
		
		$.validator.addMethod('dateRangeValidity',
			    function (value) { 
					var d2 = new Date(value);
					var d1 = new Date($("#fromDate").val());
					return (d2 > d1);
			    }, ' To Date should be greater than From Date.');
		
		$.validator.addMethod('dateValidity',
			    function (value) { 
					var d = new Date(value);
					return !isNaN(d.valueOf());
			    }, ' Please enter valid date.');
		
		$("#requestForm").validate({
			rules: {
				emId: {
					required: true,
				},
				tableName: {
					required: true,
				},	
				fromDate: {
					required: true,
					dateValidity: true
				},
				toDate: {
					required: true,
					dateValidity: true,
					dateRangeValidity: true
				}
			},
			messages: {
				
			}
		});
		
		$( "#toDate" ).datetimepicker({
			format:'d.m.Y H:i',
			inline:true,
		    onClose: function(dateText, inst) {
		    },
		    onSelect: function (selectedDateTime){
		    }
		});
		
		$( "#fromDate" ).datetimepicker({
			format:'d.m.Y H:i',
			inline:true,
		    onClose: function(dateText, inst) {
		    },
		    onSelect: function (selectedDateTime){
		    }
		});
		
	});
	
	function create() {
		$("#requestForm").submit();
	}
	
	
	
	
</script>


<div>
	<spring:url value="/datapullrequest/save.ems?customerId=${customerId}" var="submit" scope="request"/>
	<form:form id="requestForm" commandName="request" method="post" 
		action="${submit}">
		<form:hidden path="id" />
		<table>
			<tr>
				<td class="fieldLabel">Energy Manager*</td>
				<td class="fieldValue">
				 <form:select path="emId" id="emId">
                    <form:options items="${emList}" itemValue="emInstanceId" itemLabel="emInstanceName"  />
                 </form:select></td>
			</tr>
			<tr>
				<td class="fieldLabel">Table*</td>
				<td class="fieldValue">
				 <form:select path="tableName" id="tableName">
                    <form:options items="${tables}"  />
                 </form:select></td>
			</tr>
			<tr>
				<td class="fieldLabel">From Date*</td>
				<td class="fieldValue"><form:input  id="fromDate" name="fromDate"  path="fromDate"  type="text" /></td>
			</tr>
			<tr>
				<td class="fieldLabel">To Date*</td>
				<td class="fieldValue"><form:input  id="toDate" name="toDate"  path="toDate" type="text" /></td>
			</tr>
			<tr>
				<td class="fieldLabel"></td>
				<td class="fieldValue"><input class="createAction" type="button" id="createBtn" value="Create" onclick="create();"></input></td>
			</tr>
		</table>
	</form:form>
</div>



