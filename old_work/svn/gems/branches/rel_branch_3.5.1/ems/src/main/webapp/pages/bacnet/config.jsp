<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<script type="text/javascript">

var detailedModeString = "";

var isBacnetEnabled = "";

var isBacnetNetworkConfigured = "";

$().ready(function() {
	
	var requirederr = '<spring:message code="error.above.field.required"/>';
	var num_0_to_4194303 = '<spring:message code="error.numeric.0.to.4194303"/>';
	var num_1_to_120 = '<spring:message code="error.numeric.1.to.120"/>';
	var num_1_to_65535 = '<spring:message code="error.numeric.1.to.65535"/>';
	
	$("#bacnetConfig").validate({
		rules: {
			vendorId: {
				required: true,
				digits: true,
				min: 1,
				max: 65535
			},
			serverPort: {
				required: true,
				digits: true,
				min: 1,
				max: 65535
			},
			networkId: {
				required: true,
				digits: true,
				min: 1,
				max: 65535
			},
			apduLength: {
				required: true,
				digits: true,
				min: 1,
				max: 65535
			},
			apduTimeout: {
				required: true,
				digits: true,
				min: 1,
				max: 65535
			},
			energymanagerBaseInstance: {
				required: true,
				digits: true,
				min: 0,
				max: 4194303
			},
			switchgroupBaseInstance: {
				required: true,
				digits: true,
				min: 0,
				max: 4194303
			},
			areaBaseInstance: {
				required: true,
				digits: true,
				min: 0,
				max: 4194303
			}
		},
		messages: {
			vendorId: {
				required: requirederr,
				digits: num_1_to_65535,
				min: num_1_to_65535,
				max: num_1_to_65535
			},
			serverPort: {
				required: requirederr,
				digits: num_1_to_65535,
				min: num_1_to_65535,
				max: num_1_to_65535
			},
			networkId: {
				required: requirederr,
				digits: num_1_to_65535,
				min: num_1_to_65535,
				max: num_1_to_65535
			},
			apduLength: {
				required: requirederr,
				digits: num_1_to_65535,
				min: num_1_to_65535,
				max: num_1_to_65535
			},
			apduTimeout: {
				required: requirederr,
				digits: num_1_to_65535,
				min: num_1_to_65535,
				max: num_1_to_65535
			},
			energymanagerBaseInstance: {
				required: requirederr,
				digits: num_0_to_4194303,
				min: num_0_to_4194303,
				max: num_0_to_4194303
			},
			switchgroupBaseInstance: {
				required: requirederr,
				digits: num_0_to_4194303,
				min: num_0_to_4194303,
				max: num_0_to_4194303
			},
			areaBaseInstance: {
				required: requirederr,
				digits: num_0_to_4194303,
				min: num_0_to_4194303,
				max: num_0_to_4194303
			}
		}
	});
	
	isBacnetEnabled = "${bacnet.enableBacnet}";
	
	detailedModeString = "${bacnet.detailedMode}";
	
	isBacnetNetworkConfigured = "${isBacnetNetworkConfigured}";
	
	
	if(isBacnetNetworkConfigured == "false"){
		$('#errorMessageId').text("Bacnet Network is not configured. Please configure Bacnet Network in Network Settings page.");
		$('#enableBacnet').prop("disabled", true);
		$('#serverPort').prop("disabled", true);
		$('#networkId').prop("disabled", true);
		$('#apduLength').prop("disabled", true);
		$('#apduTimeout').prop("disabled", true);
		$('#energymanagerBaseInstance').prop("disabled", true);
		$('#switchgroupBaseInstance').prop("disabled", true);
		$('#areaBaseInstance').prop("disabled", true);
		$('#detailedMode').prop("disabled", true);
		$('#submit').prop("disabled", true);
	}else{
		$('#errorMessageId').text("");
		if(isBacnetEnabled == "false"){
			//$('#vendorId').prop("disabled", true);
			$('#serverPort').prop("disabled", true);
			$('#networkId').prop("disabled", true);
			$('#apduLength').prop("disabled", true);
			$('#apduTimeout').prop("disabled", true);
			$('#energymanagerBaseInstance').prop("disabled", true);
			$('#switchgroupBaseInstance').prop("disabled", true);
			$('#areaBaseInstance').prop("disabled", true);
			$('#detailedMode').prop("disabled", true);
		}
	}

});
	
function enableDisableBacnet(){
	if ($('#enableBacnet').is(':checked')) {
		//$('#vendorId').prop("disabled", false);
		$('#serverPort').prop("disabled", false);
		$('#networkId').prop("disabled", false);
		$('#apduLength').prop("disabled", false);
		$('#apduTimeout').prop("disabled", false);
		$('#energymanagerBaseInstance').prop("disabled", false);
		$('#switchgroupBaseInstance').prop("disabled", false);
		$('#areaBaseInstance').prop("disabled", false);
		$('#detailedMode').prop("disabled", false);
	}else{
		//$('#vendorId').prop("disabled", true);
		$('#serverPort').prop("disabled", true);
		$('#networkId').prop("disabled", true);
		$('#apduLength').prop("disabled", true);
		$('#apduTimeout').prop("disabled", true);
		$('#energymanagerBaseInstance').prop("disabled", true);
		$('#switchgroupBaseInstance').prop("disabled", true);
		$('#areaBaseInstance').prop("disabled", true);
		$('#detailedMode').prop("disabled", true);
	}
}

function beforeSavingBacnetConfig(){
	if ($('#enableBacnet').is(':checked')) {
		
		if(detailedModeString == "false"){
			if($('#detailedMode').is(':checked')){
				if(confirm('Changing Detailed Mode parameter will restart BACnet services and introduce sensor level BACnet points. Do you really want to proceed?')){
					return true;
				}else{
					return false;
				}
			}
		}
		if(isBacnetEnabled == "true"){
			if(confirm('Changing any parameter will restart BACnet services. Do you really want to proceed?')){
				return true;
			}else{
				return false;
			}
		}else{
			return true;
		}
	}else{
		if(confirm('Enable BACnet parameter is unchecked - This will stop BACnet services. Do you really want to proceed?')){
			return true;
		}else{
			return false;
		}
	}
	return false;
}

</script>


<div class="outerContainer">
	<span id="errorMessageId" style="color:red"></span>
	<div class="i1"></div>
	<span ><spring:message code="bacnet.header"/></span>
	<div class="i1"></div>
	<div class="innerContainer">
		<div class="formContainer">
			<div style="clear: both"><span id="error" class="load-save-errors"></span></div>
			<div style="clear: both"><span id="saveconfirm" class="save_confirmation"></span></div>
			<spring:url value="/bacnet/submit.ems" var="submit" scope="request"/>
			<form:form id="bacnetConfig" commandName="bacnet" method="post"  onsubmit="return beforeSavingBacnetConfig();" action="${submit}">
				<div class="field">
					<div class="formPrompt"><span><spring:message code="bacnet.label.enable"/></span></div>
					<div class="formValue"><form:checkbox id="enableBacnet" name="enableBacnet" path="enableBacnet" onclick="javascript:enableDisableBacnet();"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="bacnet.label.vendor.id"/></span></div>
					<div class="formValue"><form:input disabled='true' id="vendorId" path="vendorId"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="bacnet.label.server.port"/></span></div>
					<div class="formValue"><form:input id="serverPort" path="serverPort"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="bacnet.label.network.id"/></span></div>
					<div class="formValue"><form:input id="networkId" path="networkId"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="bacnet.label.max.apdu.length"/></span></div>
					<div class="formValue"><form:input id="apduLength" path="apduLength"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="bacnet.label.apdu.timeout"/></span></div>
					<div class="formValue"><form:input id="apduTimeout" path="apduTimeout"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="bacnet.label.energymanager.base.instance"/></span></div>
					<div class="formValue"><form:input id="energymanagerBaseInstance" path="energymanagerBaseInstance"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="bacnet.label.switchgroup.base.instance"/></span></div>
					<div class="formValue"><form:input id="switchgroupBaseInstance" path="switchgroupBaseInstance"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="bacnet.label.area.base.instance"/></span></div>
					<div class="formValue"><form:input id="areaBaseInstance" path="areaBaseInstance"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="bacnet.label.detailedmode"/></span></div>
					<div class="formValue"><form:checkbox id="detailedMode" name="detailedMode" path="detailedMode"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span></span></div>
					<div class="formValue"><input class="saveAction" id="submit" type="submit" value="<spring:message code='action.submit'/>"></input></div>
				</div>
			</form:form>
		</div>
	</div>
</div>

<script type="text/javascript">
var error = '<%=request.getParameter("error")%>';
var saveconfirm = '<%=request.getParameter("saveconfirm")%>';
if(error == 'save_error') {
	$("#error").html('<spring:message code="error.bacnet.save"/>');
} else {
	error = '${error}';
}
if(error == 'load_error') {
	$("#error").html('<spring:message code="error.bacnet.load"/>');
}
if(saveconfirm == 'save_success') {
	$("#saveconfirm").html('<spring:message code="bacnet.save.confirmation"/>');
}
$(function() {
	$(window).resize(function() {
		var setSize = $(window).height();
		setSize = setSize - 118;
		$(".outerContainer").css("height", setSize);
	});
});
$(".outerContainer").css("overflow", "auto");
$(".outerContainer").css("height", $(window).height() - 118);
</script>