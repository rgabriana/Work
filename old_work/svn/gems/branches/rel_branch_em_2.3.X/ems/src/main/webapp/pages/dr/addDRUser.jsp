<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<div class="outerContainer">
	<span ><spring:message code="dr.user.registration"/></span>
	<div class="i1"></div>
	<div class="innerContainer">
		<div class="formContainer">
			<div style="clear: both"><span id="error" class="load-save-errors"></span></div>
			<div style="clear: both"><span id="confirm" class="save_confirmation"></span></div>
			<spring:url value="/dr/registerUserWithoutCertificate.ems" var="submit1" scope="request"/>
			<spring:url value="/dr/registerUserWithCertificate.ems" var="submit2" scope="request"/>
			<form:form id="drUserRegistration" commandName="druser" method="post" enctype="multipart/form-data">
				<form:input type="hidden" name="keystoreFileName" id="keystoreFileName" path="keystoreFileName"/>
				<form:input type="hidden" name="keystorePassword" id="keystorePassword" path="keystorePassword"/>
				<form:input type="hidden" name="truststoreFileName" id="truststoreFileName" path="truststoreFileName"/>
				<form:input type="hidden" name="truststorePassword" id="truststorePassword" path="truststorePassword"/>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.server"/></span></div>
					<div class="formValue"><form:input id="server" name="server" path="server"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.version"/></span></div>
					<div class="formValue">
						<form:select id="version" path="version" maxlength="400">
							<form:options items="${versionList}" />
						</form:select></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.user"/></span></div>
					<div class="formValue"><form:input id="name" path="name" maxlength="40"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.password"/></span></div>
					<div class="formValue"><input type="password" id="password" name="password" maxlength="40" path="password"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.confirm.password"/></span></div>
					<div class="formValue"><input id="confirmPassword" type="password" name="confirmPassword"  maxlength="40" /></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.polling.frequency"/></span></div>
					<div class="formValue"><form:input id="timeInterval" path="timeInterval" maxlength="40"/></div>
				</div>	
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.venId"/></span></div>
					<div class="formValue"><form:input id="venId" path="venId" maxlength="40"/></div>
				</div>	
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.market.context.1"/></span></div>
					<div class="formValue"><form:input id="marketcontext1" path="marketcontext1" maxlength="400"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.market.context.2"/></span></div>
					<div class="formValue"><form:input id="marketcontext2" path="marketcontext2" maxlength="400"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.market.context.3"/></span></div>
					<div class="formValue"><form:input id="marketcontext3" path="marketcontext3" maxlength="400"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.vtn.id.1"/></span></div>
					<div class="formValue"><form:input id="vtnId1" path="vtnId1" maxlength="400"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.vtn.id.2"/></span></div>
					<div class="formValue"><form:input id="vtnId2" path="vtnId2" maxlength="400"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.vtn.id.3"/></span></div>
					<div class="formValue"><form:input id="vtnId3" path="vtnId3" maxlength="400"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.keystore.certificate"/></span></div>
					<div class="formValue"><input id="keystoreCertificate" type="file" name="keystoreCertificate"/>
						   <c:if test="${druserKeystoreAvailable}"> 
						   Keystore File Name Previously Uploaded:"${druserKeystoreFileName}"</c:if>
					       </div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.keystore.password"/></span></div>
					<div class="formValue"><input type="password" id="keystoreCertificatePass" name="keystoreCertificatePass" maxlength="40" /></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.truststore.certificate"/></span></div>
					<div class="formValue"><input id="truststoreCertificate" type="file" name="truststoreCertificate"/>
					<c:if test="${druserTruststoreAvailable}"> 
					   Truststore File Name Previously Uploaded:"${druserTruststoreFileName}"</c:if>
					</div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.truststore.password"/></span></div>
					<div class="formValue"><input type="password" id="truststoreCertificatePass" name="truststoreCertificatePass" maxlength="40" /></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.prefix"/></span></div>
					<div class="formValue"><form:input id="prefix" path="prefix" maxlength="400"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="dr.servicepath"/></span></div>
					<div class="formValue"><form:input id="servicepath" path="servicepath" maxlength="400"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span></span></div>
					<div class="formValue"><input class="saveAction" id="saveAdr" type="button" value="<spring:message code='action.submit'/>" onclick="saveAdrConfig();"></input></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span></span></div>
					<div class="formValue"><input id="cleandr" type="button" value="Clean ADR Events" onclick="cleanAdrEvents();"></input></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span></span></div>
					<div class="formValue">Clicking the above button will remove all the ADR events data from database.</input></div>
					
				</div>
			</form:form>
		</div>
	</div>
</div>

<script type="text/javascript">

var drPollTimeInterval;

$().ready(function() {
	
	var requirederr = '<spring:message code="error.above.field.required"/>';
	$("#drUserRegistration").validate({
		rules: {
			server: "required",
			timeInterval: {
				required: true,
				number: true
			}
		},
		messages: {
			server: requirederr,
			timeInterval: {
				required: requirederr,
				number: 'Please Enter a Number'
			}
		}
	});
	
	var status = '<%=request.getParameter("status")%>';
	if(status == 'S') {
		$("#confirm").html('<spring:message code="dr.user.save.confirmation"/>');
	} 
	if(status == 'E') {
		$("#error").html('<spring:message code="dr.user.save.failure"/>');
	}
	
	drPollTimeInterval = "${drPollTimeInterval}";
	
	$("#version").change(function(){
	    if($("#version").val() == '2.0'){
	    	$("#venId").prop('disabled', false);
	    	$("#marketcontext1").prop('disabled', false);
	    	$("#marketcontext2").prop('disabled', false);
	    	$("#marketcontext3").prop('disabled', false);
	    	$("#vtnId1").prop('disabled', false);
	    	$("#vtnId2").prop('disabled', false);
	    	$("#vtnId3").prop('disabled', false);
	    	$("#keystoreCertificate").prop('disabled', false);
	    	$("#keystoreCertificatePass").prop('disabled', false);
	    	$("#truststoreCertificate").prop('disabled', false);
	    	$("#truststoreCertificatePass").prop('disabled', false);
	    	$("#prefix").prop('disabled', false);
	    	$("#servicepath").prop('disabled', false);
	    }else{
	    	$("#venId").prop('disabled', true);
	    	$("#marketcontext1").prop('disabled', true);
	    	$("#marketcontext2").prop('disabled', true);
	    	$("#marketcontext3").prop('disabled', true);
	    	$("#vtnId1").prop('disabled', true);
	    	$("#vtnId2").prop('disabled', true);
	    	$("#vtnId3").prop('disabled', true);
	    	$("#keystoreCertificate").prop('disabled', true);
	    	$("#keystoreCertificatePass").prop('disabled', true);
	    	$("#truststoreCertificate").prop('disabled', true);
	    	$("#truststoreCertificatePass").prop('disabled', true);
	    	$("#prefix").prop('disabled', true);
	    	$("#servicepath").prop('disabled', true);
	    }
	});
	
	var versionNumber = $("#version").val();
	
	if(versionNumber == '2.0'){
		$("#venId").prop('disabled', false);
		$("#marketcontext1").prop('disabled', false);
    	$("#marketcontext2").prop('disabled', false);
    	$("#marketcontext3").prop('disabled', false);
    	$("#vtnId1").prop('disabled', false);
    	$("#vtnId2").prop('disabled', false);
    	$("#vtnId3").prop('disabled', false);
    	$("#keystoreCertificate").prop('disabled', false);
    	$("#keystoreCertificatePass").prop('disabled', false);
    	$("#truststoreCertificate").prop('disabled', false);
    	$("#truststoreCertificatePass").prop('disabled', false);
    	$("#prefix").prop('disabled', false);
    	$("#servicepath").prop('disabled', false);
	}else{
		$("#venId").prop('disabled', true);
		$("#marketcontext1").prop('disabled', true);
    	$("#marketcontext2").prop('disabled', true);
    	$("#marketcontext3").prop('disabled', true);
    	$("#vtnId1").prop('disabled', true);
    	$("#vtnId2").prop('disabled', true);
    	$("#vtnId3").prop('disabled', true);
    	$("#keystoreCertificate").prop('disabled', true);
    	$("#keystoreCertificatePass").prop('disabled', true);
    	$("#truststoreCertificate").prop('disabled', true);
    	$("#truststoreCertificatePass").prop('disabled', true);
    	$("#prefix").prop('disabled', true);
    	$("#servicepath").prop('disabled', true);
	}
	
	$(window).resize(function() {
		var setSize = $(window).height();
		setSize = setSize - 118;
		$(".outerContainer").css("height", setSize);
	});
	
	$(".outerContainer").css("overflow", "auto");
	$(".outerContainer").css("height", $(window).height() - 118);
	
});


function saveAdrConfig(){
	if(validateSubmitForm() == true){
		$("#drUserRegistration").submit();
	}
}

function cleanAdrEvents(){
	var proceed = confirm("Are you sure you want to clean all the ADR Events from DB?");
	if( proceed == true){
		$("#cleandr").prop('disabled', true);
		$.ajax({
			   type: "GET",
			   url: '<spring:url value="/services/org/dr/cleandrevents/"/>',
			   contentType: "application/json",
			   dataType: "json",
			   success: function(data){
					if(data.status == "1") {
						alert("ADR Events Cleaned from DB");  
					}else{
						alert("Error occured while cleaning ADR Events from DB");
					}
					$("#cleandr").prop('disabled', false);
					
			   },
			   error: function() {
				   alert("Error occured while cleaning ADR Events from DB");
				   $("#cleandr").prop('disabled', false);
			   }		   
		});
	}
	
}

function validateSubmitForm() {
	$("#confirm").html("");
	$("#error").html("");
	
	var keystoreName = $('#keystoreCertificate').val().toLowerCase();
	if(keystoreName.indexOf("\\") > -1)
	{
		var keystoreName_array = keystoreName.split("\\");
		keystoreName = keystoreName_array[keystoreName_array.length - 1];
	}
	var keystoreNameArray = keystoreName.split(".");
	var keystorefileName = keystoreNameArray[0];
	var keystorefileExtension = keystoreNameArray[keystoreNameArray.length - 1];
	
	if(keystoreName != ""){
		$("#keystoreFileName").val(keystoreName);
		if($('#keystoreCertificatePass').val() == ""){
			$("#error").html("KeyStore Certificate Password is mandatory if KeyStore Certificate is selected");
			return false;
		}
	}else{
		if($('#keystoreCertificatePass').val() != ""){
			$("#error").html("Please select a KeyStore Certificate");
			return false;
		}
	}
	
	var keystoreCertificatePassValue = $('#keystoreCertificatePass').val();
	
	if(keystoreCertificatePassValue != ""){
		$("#keystorePassword").val(keystoreCertificatePassValue);
	}
	
	var truststoreName = $('#truststoreCertificate').val().toLowerCase();
	if(truststoreName.indexOf("\\") > -1)
	{
		var truststoreName_array = truststoreName.split("\\");
		truststoreName = truststoreName_array[truststoreName_array.length - 1];
	}
	var truststoreNameArray = truststoreName.split(".");
	var truststorefileName = truststoreNameArray[0];
	var truststorefileExtension = truststoreNameArray[truststoreNameArray.length - 1];
	
	if(truststoreName != ""){
		$("#truststoreFileName").val(truststoreName);
		if($('#truststoreCertificatePass').val() == ""){
			$("#error").html("TrustStore Certificate Password is mandatory if TrustStore Certificate is selected");
			return false;
		}
	}else{
		if($('#truststoreCertificatePass').val() != ""){
			$("#error").html("Please select a TrustStore Certificate ");
			return false;
		}
	}
	
	var truststoreCertificatePassValue = $('#truststoreCertificatePass').val();
	
	if(truststoreCertificatePassValue != ""){
		$("#truststorePassword").val(truststoreCertificatePassValue);
	}
	
	
	if($("#version").val() == '1.0'){
		
		if($("#password").val() == '' || $("#name").val() == ''){
			$("#error").html("Username and Password is mandatory if version is 1.0");
			return false;
		}
		
		$("#keystoreFileName").val("");
		$("#keystorePassword").val("");
		$("#truststoreFileName").val("");
		$("#truststorePassword").val("");
		$('#drUserRegistration').attr('action','${submit1}');
			
	}
	else{
		
		if($("#name").val() != '' && $("#password").val() == ''){
			$("#error").html("Please enter password");
			return false;
		}
		if($("#password").val() != '' && $("#name").val() == ''){
			$("#error").html("Please enter username");
			return false;
		}
		
		$('#drUserRegistration').attr('action', '${submit2}'); 
	}
	
	if($("#password").val() != $("#confirmPassword").val()){
		$("#error").html("Password and Conform Password values should be same");
		return false;
	}
	
	if(parseInt($("#timeInterval").val()) < parseInt(drPollTimeInterval)){
		$("#error").html("Polling time interval cannot be less than "+drPollTimeInterval+" seconds");
		return false;
	}
	
	return true;
}
	
</script>