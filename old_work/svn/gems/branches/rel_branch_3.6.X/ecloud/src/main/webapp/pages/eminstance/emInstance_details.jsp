<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<style>
	#create_emInstance{padding:10px 15px;}
	#create_emInstance table{width:100%;}
	#create_emInstance td{padding-bottom:3px;}
	#create_emInstance td.fieldLabel{width:40%; font-weight:bold;}
	#create_emInstance td.fieldValue{width:60%;}
	#create_emInstance .inputField{width:100%; height:20px;}
	#create_emInstance #saveBtn{padding: 0 10px;}
	#create_emInstance .M_M{display: none;}
	#create_emInstance .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}  
	.sppaDetails{
	width:100%;
	padding:2px;
	border: 1px;
	border-style: groove;}
	.sppaDetails td{padding-bottom:3px;}
	 .sppaDetails td.fieldLabel{width:40%; font-weight:bold;}
	 .sppaDetails td.fieldValue{width:60%;}
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
	
	$("#create_emInstance").validate({
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
			contactName: {
				required: true,
			},
			contactEmail: {
				required: true,email:true,
			},
			contactPhone: {
				required: true,
			},
			address: {
				required: true,
			},
			emCommissionedDate:
			{
				required: true,
			},
			noOfEmergencyFixtures:
			{
				positiveInteger:true,
			},
			emergencyFixturesGuidelineLoad:{
				required: true,
				positiveNumber:true,
			},
			emergencyFixturesLoad:{
				required: true,
				positiveNumber:true,
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
			contactName: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			contactEmail: {
				required: '<spring:message code="error.valid.email.required"/>',
			},
			contactPhone: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			address: {
				required: '<spring:message code="error.above.field.required"/>',
			}
		}
	});
	
	$("#sppaEnabled").change(function(){
	    if($(this).is(':checked')){
	    	$("#replicaServerCombo").prop('disabled', false);
	    	$('#sppaBillEnabled').prop('disabled', false);
	    }else{
	    	alert("If you toggle off the Synch enabled, the Synch will stop from local server to Master. If the synch enabled is toggle back again, then the migration needs to be done again.");
	    	$("#replicaServerCombo").prop('disabled', true);
	    	$('#sppaBillEnabled').prop('disabled', true);
	    	$('#sppaBillEnabled').prop('checked', false);
	    	$("#sppaDetails").hide();
	    }
	});
	
	var isChecked = $('#sppaEnabled').attr('checked')?true:false;
	
	if(isChecked){
		$("#replicaServerCombo").prop('disabled', false);
	}else{
		$("#replicaServerCombo").prop('disabled', true);
		$('#sppaBillEnabled').prop('checked', false);
		$('#sppaBillEnabled').prop('disabled', true);
	}
	
	if($('#replicaServerCombo option').length == 0){
		$("#sppaEnabled").prop('disabled', true);
	}else{
		$("#sppaEnabled").prop('disabled', false);
	}
	
	var isSppaBillChecked = $('#sppaBillEnabled').attr('checked')?true:false;
	$("#sppaBillEnabled").change(function(){
	    if($(this).is(':checked')){
	    	$("#sppaDetails").show();
	    }else{
	    	$("#sppaDetails").hide();
	    }
	});
	
	if(isSppaBillChecked && isChecked){
		$("#sppaDetails").show();
	}else{
		$("#sppaDetails").hide();
	}
	
	var emergencyFixtureCount = $("#noOfEmergencyFixtures").val();
	enableDisableEmergencyOptions(emergencyFixtureCount);
	$("#noOfEmergencyFixtures").change(function(){
		var selectedOption = $("#noOfEmergencyFixtures").val();
		enableDisableEmergencyOptions(selectedOption);
	});
	
	currentServerDateStr = "${currentServerDateStr}";
	$( "#emCommissionedDate" ).datetimepicker({
		dateFormat: 'mm/dd/yy',
		showTimepicker:false,
		currentText:'Today',
		//maxDate: 0,
	    onClose: function(dateText, inst) {
	    },
	    onSelect: function (selectedDateTime){
	    }
	});
	
	var supportedVersionString = "${supportedVersionString}";
	
	var running_version = parseEmInstanceVersionString('${emInstance.version}');
	
	var latest_version = parseEmInstanceVersionString(supportedVersionString);
	
	if (running_version.major < latest_version.major) {
		$("#sshTunnelEnabledRow").hide();
		
	}else if(running_version.major == latest_version.major){
		
		if (running_version.minor < latest_version.minor) {
			$("#sshTunnelEnabledRow").hide();
			
		}else if(running_version.minor == latest_version.minor){
			
			if (running_version.patch < latest_version.patch) {
				$("#sshTunnelEnabledRow").hide();
				
			}else if(running_version.patch == latest_version.patch){
				
				if (running_version.remain < latest_version.remain) {
					$("#sshTunnelEnabledRow").hide();
					
				}
			}
		}
		
	}
	
});
	
	function enableDisableEmergencyOptions(value)
	{
		if(value <= 0)
		{
			$("#emergencyFixturesGuidelineLoad").prop('disabled', true);
			$('#emergencyFixturesGuidelineLoad').attr('readonly', 'readonly'); 
			$("#emergencyFixturesGuidelineLoad").val("");
			$("#emergencyFixturesLoad").prop('disabled', true);
			$('#emergencyFixturesLoad').attr('readonly', 'readonly'); 
			$("#emergencyFixturesLoad").val("");
			$('#emerFixGuidLoadLabel').text("Emergency Fixtures Guideline Load (kW)");
			$('#emerFixLoadLabel').text("Emergency Fixtures Load (kW)");
		}else
		{
			$("#emergencyFixturesGuidelineLoad").prop('disabled', false);
			$('#emergencyFixturesGuidelineLoad').removeAttr("readonly");
			$("#emergencyFixturesLoad").prop('disabled', false);
			$('#emergencyFixturesLoad').removeAttr("readonly");
			$('#emerFixGuidLoadLabel').text("Emergency Fixtures Guideline Load (kW)*");
			$('#emerFixLoadLabel').text("Emergency Fixtures Load (kW)*");
		}
	}
	
	function saveEmInstance(){
			$("#create_emInstance").submit();
	}
	
	function closeDialog(){
		$("#emInstanceDetailsDialog").dialog("close");
	}
	
	function onDownload(){
		window.location = "downloadcert.ems?emInstanceId="+$("#id").val();
	}
	
	function parseEmInstanceVersionString (str) {
	    if (typeof(str) != 'string') { return false; }
	    var x = str.split('.');
	    var maj = parseInt(x[0]) || 0;
	    var min = parseInt(x[1]) || 0;
	    var pat = parseInt(x[2]) || 0;
	    var rem = parseInt(x[3]) || 0;
	    return {
	        major: maj,
	        minor: min,
	        patch: pat,
	        remain: rem
	    }
	}
	
</script>

<div>
	<spring:url value="/eminstance/save.ems" var="actionURL" scope="request" />
	<form:form id="create_emInstance" commandName="emInstance" method="post" 
		action="${actionURL}">
		<form:hidden path="id" id="id" name="id" />
		<input type="hidden" id="customerId" name="customerId" value="${customerId}" />
		<table>
			<tr>
				<td class="fieldLabel">Name*</td>
				<td class="fieldValue"><form:input class="inputField" id="name" name="name" size="40" path="name" />
			</tr>
			<tr>
				<td class="fieldLabel">Version*</td>
				<td class="fieldValue"><form:input class="inputField" id="version"
					name="version" size="40" path="version"/></td>
			</tr>
			<tr>
				<td class="fieldLabel">Mac Id*</td>
				<td class="fieldValue"><form:input class="inputField" id="macId"
					name="macId" size="40" path="macId"/></td>
			</tr>
			<tr>
				<td class="fieldLabel">Contact Name*</td>
				<td class="fieldValue"><form:input class="inputField" id="contactName" 
						name="contactName" size="40" path="contactName" />
			</tr>
			<tr>
				<td class="fieldLabel">Contact Email*</td>
				<td class="fieldValue"><form:input class="inputField" id="contactEmail"
					name="contactEmail" size="40" path="contactEmail"/></td>
			</tr>
			<tr>
				<td class="fieldLabel">EM Address*</td>
				<td class="fieldValue"><form:input class="inputField" id="address"
					name="address" size="40" path="address"/></td>
			</tr>
			<tr>
				<td class="fieldLabel">Contact PhoneNo*</td>
				<td class="fieldValue"><form:input class="inputField" id="contactPhone"
					name="contactPhone" size="40" path="contactPhone"/></td>
			</tr>
			 <tr>
				<td class="fieldLabel">EM Commissioned Date*</td>
				
				<fmt:formatDate value="${emInstance.emCommissionedDate}"  
                type="date" 
                pattern="MM/dd/yyyy"
                var="theFormattedDate" />
				<td class="fieldValue"><form:input  id="emCommissionedDate" name="emCommissionedDate"  path="emCommissionedDate" value="${theFormattedDate}" type="text" /></td>
			</tr>
			<c:if test="${mode == 'edit'}">
			<tr id="sshTunnelEnabledRow">
				<td class="fieldLabel">Enable SSH Tunnel</td>
				<td class="fieldValue">
				 <form:checkbox path="openSshTunnelToCloud" id="sshTunnelEnabled" />
	            </td>
            </tr>
			<tr>
				<td class="fieldLabel">Cloud Sync Enable</td>
				<td class="fieldValue">
				 <form:checkbox path="sppaEnabled" id="sppaEnabled" />
	            </td>
            </tr>
         	<tr>
				<td class="fieldLabel">Replica Server*</td>
				<td class="fieldValue">
				 <form:select path="replicaServer.id" id="replicaServerCombo">
                    <form:options items="${replicaServerCollection}" itemValue="id" itemLabel="displayLabel"  />
                 </form:select></td>
			</tr>
			<tr>
				<td class="fieldLabel">SPPA Enable</td>
				<td class="fieldValue">
				 <form:checkbox path="sppaBillEnabled" id="sppaBillEnabled" />
	            </td>
            </tr>
            
            <tr>
            <td colspan="2" width="100%">
           		 <table id="sppaDetails" class="sppaDetails">
           		
					<tr>
						<td class="fieldLabel">No. of Emergency Fixtures*</td>
						<td class="fieldValue"><form:input class="inputField" id="noOfEmergencyFixtures"
		 							name="noOfEmergencyFixtures" size="40" path="noOfEmergencyFixtures"/></td>
					</tr>
					<tr>
						<td class="fieldLabel" id="emerFixGuidLoadLabel">Emergency Fixtures Guideline Load (kW)</td>
						<td class="fieldValue"><form:input class="inputField" id="emergencyFixturesGuidelineLoad"
		 							name="emergencyFixturesGuidelineLoad" size="40" path="emergencyFixturesGuidelineLoad"/></td>
					</tr>
					<tr>
						<td class="fieldLabel" id="emerFixLoadLabel">Emergency Fixtures Load (kW)</td>
						<td class="fieldValue"><form:input class="inputField" id="emergencyFixturesLoad"
		 							name="emergencyFixturesLoad" size="40" path="emergencyFixturesLoad"/></td>
					</tr>
					
					</table> 
			</td>
            </tr> 
			</c:if>
			<tr>
				<td />
				<td><input id="saveBtn" type="button"
					value="<spring:message code="action.save" />" onclick="saveEmInstance();">&nbsp;
					<input type="button" id="btnClose"
					value="<spring:message code="action.cancel" />" onclick="closeDialog()">	
				</td>
			</tr>
			<c:if test="${mode == 'edit' && downloadCertEnable == 'true'}">
			<tr>
			<td />
			<td>
				<input type="button" id="btnDownload"
					value="Download Cert" onclick="onDownload();"> 
			</td>
			</tr>
			</c:if>
		</table>
	</form:form>
</div>



