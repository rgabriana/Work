<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<style type="text/css">
	.message
	{
		clean:both;
		margin-top:5px;
		color:green;font-weight:bold;
	}
</style>

<script type="text/javascript">

function registerNTPClicked(item,actValIfFalse,actValIfTrue,referenceVal,item1){
	setGenericCheckBoxValue(item,actValIfFalse,actValIfTrue,referenceVal);
	$(item).change(
			function(event){
				changeGenericCheckBoxVal(event.target,actValIfFalse,actValIfTrue);
			}
	);
}
function changeGenericCheckBoxVal(target, actValIfFalse,actValIfTrue){
	if($(target).is(":checked")) {
		$(target).val(actValIfTrue);
	}else{
		$(target).val(actValIfFalse);
	}
}
function setGenericCheckBoxValue(item,actValIfFalse,actValIfTrue,referenceVal){
	var actVal = $(item).val();
	if(actVal != undefined && referenceVal != undefined && actVal == referenceVal){
		$(item).prop('checked', false);
		$(item).val(actValIfFalse);
	}else{
		$(item).prop('checked', true);
		$(item).val(actValIfTrue);
	}
}
function setGenericTextValueDependOther(item,actValIfFalse,actValIfTrue,referenceVal,item1){
	var actVal = $(item1).val();
	if(actVal != undefined && referenceVal != undefined && actVal == referenceVal){
		$(item).val(actValIfFalse);
	}else{
		$(item).val(actValIfTrue);
	}
}
$(document).ready(function() {
	
	jQuery.validator.addMethod("positiveIntegerOnly", function(value, element) {
	    return value != undefined && value != null && value.trim() != '' && /^([0-9]+)?$/i.test(value);
	}, "Integer only please");
	
	var flagAuth = "${emailModel.flagAuth}";
	var flagTls = "${emailModel.flagTls}";
	if(flagAuth == undefined || flagAuth == null || flagAuth.trim() == '' || flagAuth.trim() == 'false'){
		$("#flagAuth1").val('false');
	}else{
		$("#flagAuth1").val('true');
	}
	if(flagTls == undefined || flagTls == null || flagTls.trim() == '' || flagTls.trim() == 'false'){
		$("#flagTls1").val('false');
	}else{
		$("#flagTls1").val('true');
	}
	registerNTPClicked("#flagAuth1",'false','true','false');
	registerNTPClicked("#flagTls1",'false','true','false');
			$("#validationForm")
			.validate(
					{
						rules : {
							host : {
								required : true,
							},
							port : {
								required : true,positiveIntegerOnly : true,
							},
							user : {
								required : true,
							},
							pass : {
								required : true,
							},
							protocol : {
								required : true,
							},
						},
						messages : {
							host : {
								required : "Please enter Smtp host",
							},
							port : {
								required : "Please enter Smtp port",positiveIntegerOnly : "Port should be of type integer",
							},
							user : {
								required : "Please enter Smtp username credential",
							},
							pass : {
								required : "Please enter Smtp password for the user above",
							},
							protocol : {
								required : "Please enter Protocol to use e.g. smtp",
							},
						}
					});
			
			<c:if test="${not empty RESULT}">
				$("#result").html("${RESULT}") ;
			</c:if>

});
function validateUpload()
{	 	
	//setGenericTextValue("#flagAuth",'false','true','false');
	//setGenericTextValue("#flagTls",'false','true','false');
	
	if ($("#validationForm").valid()) {
		setGenericTextValueDependOther("#flagAuth",'false','true','false',"#flagAuth1");
		setGenericTextValueDependOther("#flagTls",'false','true','false',"#flagTls1");
		$("#validationForm").submit();
		return true;
	} else {
		//$('.error').focus();
		$("#result").html("") ;
	}
	return false;
}

</script>
<div class="outermostdiv">
	
	<div class="outerContainer">
      <span>Email Configuration Settings</span>
      <div class="i1"></div>
   </div>
	<div class="upperdiv"
		style="height: 275px; margin: 10px; padding: 10px;">
	<div id="msesage" class="message">
		<label id="result"></label>
	</div>
	    <spring:url  value="/settings/saveemailconfig.ems" var="saveMail" />
		<form:form id="validationForm" action="${saveMail}" method="post" commandName="emailModel">
					<form:input id="emailId" type="hidden" name="id" path="id" />
					<div class="field">
						<div class="formPrompt">
							<span><spring:message code="email.host.label" /></span>
						</div>
						<div class="formValue">
							<form:input id="host" name="host" path="host" />
						</div>
					</div>
					<div class="field">
						<div class="formPrompt">
							<span><spring:message code="email.port.label" /></span>
						</div>
						<div class="formValue">
							<form:input id="port" name="port" path="port" />
						</div>
					</div>
					<div class="field">
						<div class="formPrompt">
							<span><spring:message code="email.user.label" /></span>
						</div>
						<div class="formValue">
							<form:input id="user" name="user" path="user" />
						</div>
					</div>
					<div class="field">
						<div class="formPrompt">
							<span><spring:message code="email.pass.label" /></span>
						</div>
						<div class="formValue">
							<form:input id="pass" name="pass" path="pass" type="password" />
						</div>
					</div>
					<div class="field">
						<div class="formPrompt">
							<span><spring:message code="email.protocol.label" /></span>
						</div>
						<div class="formValue">
							<form:input id="protocol" name="protocol" path="protocol" />
						</div>
					</div>
					<div class="field">
						<div class="formPrompt">
							<span><spring:message code="email.flagAuth.label" /></span>
						</div>
						<div class="formValue">
							<input type="checkbox" id="flagAuth1" name="flagAuth1" />
							<form:input type="hidden" id="flagAuth" name="flagAuth" path="flagAuth" />
						</div>
					</div>
					<div class="field">
						<div class="formPrompt">
							<span><spring:message code="email.flagTls.label" /></span>
						</div>
						<div class="formValue">
							<input type="checkbox" id="flagTls1" name="flagTls1" />
							<form:input type="hidden" id="flagTls" name="flagTls" path="flagTls" />
						</div>
					</div>
					<div class="field">
						<div class="formPrompt">
							<span></span>
						</div>
						<div class="formValue">
							<input id="saveUserBtnValid" style="clear:both;margin-top:30px!important;" type="button" onclick="validateUpload();" value="Save">
						</div>
					</div>
						
		</form:form>
	</div>
</div>