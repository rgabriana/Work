<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<style>
</style>
<style>
.fieldLabel {
	padding-top: 8px;
	padding-bottom: 8px;
}

.inputField {
	height: 20px;
	vertical-align: middle;
}
</style>

<script type="text/javascript">
var firstTime = true;
	$(document).ready(function(){
		
		$("#level").val([]); 
		$("#customer").val([]); 

	  	var status= '<%=request.getParameter("status")%>';
						if (status == "false") {
							$("#error").html("Key generation faliled.");
							$("#error").css("color", "red");
						} else if (status == "true") {
							$("#confirm").html("Key Generated successfully");
							$("#confirm").css("color", "green");
						}

						$("#edit_customer")
								.validate(
										{
											rules : {
												macId : {
													required : true,
												},
												level : {
													required : true,
												},
												startDate : {
													required : true,
													date : true,
													checkFormat: ""
													
												},
												endDate : {
													required : true,
													date : true,
												}
											},
											messages : {
												macId : {
													required : '<spring:message code="error.above.field.required"/>',
												},
												level : {
													required : '<spring:message code="error.above.field.required"/>',
												},
												startDate : {
													required : '<spring:message code="error.above.field.required"/>',
													date:'<spring:message code="error.invalid.date"/>',
												},
												endDate : {
													required : '<spring:message code="error.above.field.required"/>',
													date:'<spring:message code="error.invalid.date"/>',
												}
											},
											errorElement : "div",
											wrapper : "div", // a wrapper around the error message 
											errorPlacement : function(error,
													element) {
												offset = element.offset();
												error.insertBefore(element)
												error.addClass('message'); // add a class to the wrapper 
												error.css('display', 'block');
											}
										});

						$(".maindiv").css("height", $(window).height() - 190);
						$(".maindiv").css("overflow", "auto");
						
						$("#startDate").val("mm/dd/yyyy");
						$("#endDate").val("mm/dd/yyyy");
						
						jQuery.validator.addMethod("checkFormat", function(value, element) {
							
							var start = $("#startDate").val();
							 var end = $("#endDate").val();
							 var nowDate=new Date();
							 var stDate = new Date(start);
							 var enDate = new Date(end);
							 var compDate = enDate - stDate;
							 if(compDate < 0)
							 return false;
							 else
							 return true;
						}, '<spring:message code="error.range.date"/>');
						
						 $(function(){ 
					          $("#startDate").datepicker(); 
					          $("#endDate").datepicker(); 
					      }); 
					});
	
		function clearForm()
		{
			if(firstTime)
			{
				$("#startDate").val("");
				$("#endDate").val("");
			}
			firstTime = false;
		}
		
		function clearError()
		{
			$("#error").html("");
		}
	
</script>

<div class="outermostdiv">
	<div class=outerContainer>
		<span>Generate Key</span>
		<div class="i1"></div>
		<div class="upperdiv">
			<div class="formContainer">
				<div style="clear: both">
					<span id="error" class="load-save-errors"></span>
				</div>
				<div style="clear: both">
					<span id="confirm" class="save_confirmation"></span>
				</div>
				<spring:url value="/saveKey.ems" var="submitUrl" scope="request" />
				<form:form id="edit_customer" commandName="licenseDetails" 
					method="post" action="${submitUrl}" >
					<form:hidden path="id" />
					<table id="generateKeyTable"
						style="border: 1px; border-color: red; padding-top: 5px;">
						<tr>
							<td class="fieldLabel"><spring:message
									code="licenseDetails.customer" />*</td>
							<td><form:select class="required" id="customer" name="customer"
									path="customerId">
									<c:forEach items="${customers}" var="customers">
										<option value="${customers.id}">${customers.name}</option>
									</c:forEach>
								</form:select></td>
							<td />
						</tr>
						<tr>
							<td class="fieldLabel"><spring:message
									code="licenseDetails.macId" />*</td>
							<td class="fieldValue"><form:input class="inputField"
									id="macId" name="macId" size="40" path="macId" /></td>
							<td />
						</tr>

						<tr>
							<td class="fieldLabel"><spring:message
									code="licenseDetails.level" />*</td>
							<td class="fieldValue">
								<form:select class="required"  id="level" name="level" path="level">
												<option value="Platinum">Platinum</option>
												<option value="Gold">Gold</option>
												<option value="Silver">Silver</option>
								</form:select>
							</td>
							<td/>
						</tr>

						<tr>
							<td class="fieldLabel"><spring:message
									code="licenseDetails.startDate" />*</td>
							<td class="fieldValue">
									<input type="text" id="startDate"  name="startDate"  onclick="clearForm();">
									</td>
							<td class="fieldLabel"></td>
						</tr>

						<tr>
							<td class="fieldLabel"><spring:message
									code="licenseDetails.endDate" />*</td>
									<td class="fieldValue">
							<input type="text" id="endDate"  name="endDate" onclick="clearForm();"></td>
							<td class="fieldLabel"></td>
						</tr>

						<tr>
							<td><div class="field">
									<div class="formPrompt">
										<span></span>
									</div>
									<div class="formValue">
										<input class="saveAction" id="submit" type="submit" value="<spring:message code='action.submit'/>"></input>
									</div>
								</div></td>
							<td />
							<td />
						</tr>
					</table>
				</form:form>
			</div>
		</div>
	</div>
</div>


