<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<style>
	#dr_prompt{padding:10px 15px;}
	#dr_prompt table{width:100%;}
	#dr_prompt td{padding-bottom:3px;}
	#dr_prompt td.fieldLabel{width:40%; font-weight:bold;}
	#dr_prompt td.fieldValue{width:60%;}
	#dr_prompt .inputField{width:100%; height:20px;}
	#dr_prompt #saveDRTargetBtn{padding: 0 10px;}
	#dr_prompt .M_M{display: none;}
	#dr_prompt .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}
	button.ui-datepicker-current { display: none; }

</style>
<html>
<head>
<script type="text/javascript">	
	$(document).ready(function(){
		<c:set var="now" value="<%=new java.util.Date()%>" />
		var date = "<fmt:formatDate pattern="yyyy-MM-dd HH:mm:ss" value="${now}" />";
		
		$("#startTime").datetimepicker({
			dateFormat: 'yy-mm-dd',
			timeFormat: 'hh:mm:ss',
			showSecond: true,
			minDate:'0',
		    onClose: function(dateText, inst) {
		    },
		    onSelect: function (selectedDateTime){
		    },
		});
		$('#startTime').datetimepicker('setDate', date);
		if("${mode}" == "edit"){
			var status = "${drtarget.drStatus}";		  
			$("#drStatus").val(status);
			if(status == "Active")
				$("#startTime").attr('disabled', true);
			
			$("#drType").attr('disabled', true); 
						
			if($("#drType").val() == "MANUAL")
			{
				$("#priceLevel option[value='HOLIDAY']").prop('disabled', true);
		        $("#priceLevel option[value!='HOLIDAY']").prop('disabled', false);	
		        $("#pricing").attr('disabled', false);
			}
		    else
			{
		    	$("#priceLevel option[value!='HOLIDAY']").prop('disabled', true);
		        $("#priceLevel option[value='HOLIDAY']").prop('disabled', false);
		        $("#pricing").attr('disabled', true);
			}
		}
		
		if("${mode}" == "new"){
		    $("#drStatus").val("Scheduled");		    
		    $("#priceLevel option[value='HOLIDAY']").prop('disabled', true);
		    $("#priceLevel").val("SPECIAL");
		    
		    $("#drType").change(function(){		    	
				if($("#drType").val() == "MANUAL")
				{
					$("#priceLevel option[value='HOLIDAY']").prop('disabled', true);
			        $("#priceLevel option[value!='HOLIDAY']").prop('disabled', false);
			        $("#priceLevel").val("SPECIAL");
			        $("#pricing").attr('disabled', false);
				}
			    else
				{
			    	$("#priceLevel option[value!='HOLIDAY']").prop('disabled', true);
			        $("#priceLevel option[value='HOLIDAY']").prop('disabled', false);
			        $("#priceLevel").val("HOLIDAY");
			        $("#pricing").attr('disabled', true);
				}		
			});
		}
		
		$.validator.addMethod("drdescriptionregx", function(value, element, regexpr) {          
			if (value != "") {
				return regexpr.test(value);
			}
			return true;
		}, "Please enter a valid Description.");
	});	
	
</script>

<script type="text/javascript">
	//Add user form validation 
	var requirederr = '<spring:message code="error.above.field.required"/>';
	
	function dateTimeValidationMethod(value, element){
	    return /^((((19|[2-9]\d)\d{2})[\/\.-](0[13578]|1[02])[\/\.-](0[1-9]|[12]\d|3[01])\s(0[0-9]|1[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]))|(((19|[2-9]\d)\d{2})[\/\.-](0[13456789]|1[012])[\/\.-](0[1-9]|[12]\d|30)\s(0[0-9]|1[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]))|(((19|[2-9]\d)\d{2})[\/\.-](02)[\/\.-](0[1-9]|1\d|2[0-8])\s(0[0-9]|1[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]))|(((1[6-9]|[2-9]\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))[\/\.-](02)[\/\.-](29)\s(0[0-9]|1[0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])))$/g.test(value);
	}
	
	$.validator.addMethod("dateTimeValidation", dateTimeValidationMethod, "Please enter only in correct format.");
	
	var createDrValidatorObj = {
			rules: {
				pricing: { required: true, min: 0 },
				duration: { required: true, min: 1, max:65000, digits: true },
				startTime:{ required: true,dateTimeValidation:true},
				description:{ required: false, maxlength:200,drdescriptionregx:/^[A-Za-z0-9\s\_\-\.\,\:]+$/ }
			},
			messages: {
				pricing: { 
					required: requirederr, 
					min: '<spring:message code="error.price.number.required"/>',
					digits: '<spring:message code="error.price.number.required"/>' 
					},
				duration: { 
					required: requirederr,
					min: '<spring:message code="error.valid.dr.duration.required"/>',
					max: '<spring:message code="error.valid.dr.duration.required"/>',
					digits: '<spring:message code="error.valid.dr.duration.required"/>'
					},
				startTime:{ required: requirederr,dateTimeValidation: '<spring:message code="error.drstarttime.number.required"/>' },
				description: {					
					maxlength: '<spring:message code="error.invalid.dr.description.maxlength"/>'
				}
			}
			
	};
	
	$("#dr_prompt .M_M").css("display", "inline"); //Show Mandatory mark	
	
	function validateAndsaveDRTarget(){		
		var isValid = true;
			
		var description = $("#description").val();	
		if(description=="")
		{		
			$("#description").val(" ");
		}
		
		return isValid;		
	}
	
	function saveDRTarget(){		
		if(validateAndsaveDRTarget() == true){
			$("#dr_prompt").validate(createDrValidatorObj);
			if($("#dr_prompt").valid()){				
				$("#dr_prompt").submit();
			}
		}		
	}
	
</script>
</head>
<body>
<div>
   <spring:url value="/dr/save.ems" var="actionURL" scope="request" />
   <form:form id="dr_prompt" commandName="drtarget" method="post" action="${actionURL}">
        <form:hidden path="id" />
        <form:hidden path="optIn" />			
		<table>
			<tr>
				<td class="fieldLabel"><spring:message code="dr.label.event.type" /></td>
				<td class="fieldValue">
				       <form:select class="inputField" path="drType">				
							<c:forEach var="type" items="${eventType}">
								<c:choose>
									<c:when test="${type.name  == 'MANUAL'}">
										<form:option value="MANUAL" label="DR" />
									</c:when>
									<c:when test="${type.name  == 'OADR'}">
									</c:when>
									<c:otherwise>
										<form:option value="${type.name}" label="${type.name}" />
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</form:select></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="dr.label.description" /></td>
				<td class="fieldValue"><form:input class="inputField" id="description" name="description" size="30" path="description" /></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="dr.label.pricing" /><span class="M_M">*</span></td>
				<td class="fieldValue"><form:input class="inputField" id="pricing" name="pricing" path="pricing" maxLength="10" size="30" /></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="dr.label.duration" /><span class="M_M">*</span></td>
				<td class="fieldValue"><form:input class="inputField" id="duration"	name="duration" path="duration" size="30" /></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="dr.label.start.time" /><span class="M_M">*</span></td>				
				<td class="fieldValue"><form:input class="inputField" id="startTime" name="startTime" path="startTime" type="text" size="30"/></td>
			</tr>			
			<tr>
				<td class="fieldLabel"><spring:message code="dr.label.price.level" /></td>
				<td class="fieldValue"><form:select class="inputField" path="priceLevel"  id="priceLevel">
						<form:options items="${drLevel}" itemValue="name" itemLabel="name" />
					</form:select></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="dr.label.status" /></td>
				<td class="fieldValue"><input class="inputField" id="drStatus" name="drStatus" size="30" readonly/></td>
			</tr>
			<tr>
				<td />
				<td><input id="saveDRTargetBtn" type="button"
					value="<spring:message code="action.save" />" onclick="saveDRTarget();">&nbsp;
					<input type="button" id="btnClose"
					value="<spring:message code="action.cancel" />" onclick="closeDialog()">	
				</td>
			</tr>
		</table>
	</form:form>
</div>
</body>
</html>