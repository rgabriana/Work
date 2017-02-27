<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

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
			sppaPrice: {
				required: true,
				number:true,
			},
			taxRate: {
				required: true,
				number:true,
			},
			blockPurchaseEnergy: {
				required: true,
				number:true,
			},
			geoLocation: {
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
			},
			sppaPrice: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			taxRate: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			blockPurchaseEnergy: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			geoLocation: {
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
	
	var selectedOption = $("#taxablecombo").find("option:selected").attr('value');
	enableDisableTaxRate(selectedOption);
	
	var isSppaBillChecked = $('#sppaBillEnabled').attr('checked')?true:false;
	$("#sppaBillEnabled").change(function(){
	    if($(this).is(':checked')){
	    	var msg = $("#taxablecombo").val();
	    	enableDisableTaxRate(msg);
	    	$("#sppaDetails").show();
	    }else{
	    	enableDisableTaxRate(false);
	    	$("#sppaDetails").hide();
	    }
	});
	
	if(isSppaBillChecked && isChecked){
		$("#sppaDetails").show();
	}else{
		$("#sppaDetails").hide();
	}
	
	$("#taxablecombo").change(function(){
		var selectedOption = $(this).find("option:selected").attr('value');
		enableDisableTaxRate(selectedOption);
	});
	
});
	
	function enableDisableTaxRate(flag)
	{
		if(flag=='yes')
		{
			$("#taxRate").prop('disabled', false);
			$('#taxRate').removeAttr("readonly");
		}else
		{
			$("#taxRate").prop('disabled', true);
			$('#taxRate').attr('readonly', 'readonly'); 
			$("#taxRate").val("");
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
			
			<c:if test="${mode == 'edit'}">
			<tr>
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
                    <form:options items="${replicaServerCollection}" itemValue="id" itemLabel="ip"/>
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
						<td class="fieldLabel">SPPA Price*</td>
						<td class="fieldValue"><form:input class="inputField" id="sppaPrice"
		 							name="sppaPrice" size="40" path="sppaPrice"/></td>
					</tr>
						
					<tr>
						<td class="fieldLabel">Taxable</td>
						<td class="fieldValue">
							  <form:select path="taxable" id="taxablecombo">
                    			<form:option value="yes" label="Yes"></form:option>
                    			<form:option value="no" label="No"></form:option>
                			  </form:select>
		                 </td>
		            </tr>
			        
			        <tr>
						<td class="fieldLabel">Tax Rate*</td>
						<td class="fieldValue"><form:input class="inputField" id="taxRate"
		 							name="taxRate" size="40" path="taxRate"/></td>
					</tr>         
		           
		           <tr>
						<td class="fieldLabel">Block Purchase Energy (kWh)*</td>
						<td class="fieldValue"><form:input class="inputField" id="blockPurchaseEnergy"
		 							name="blockPurchaseEnergy" size="40" path="blockPurchaseEnergy"/></td>
					</tr>
					
					 <tr>
						<td class="fieldLabel">Geo Loc Code*</td>
						<td class="fieldValue"><form:input class="inputField" id="geoLocation"
		 							name="geoLocation" size="40" path="geoLocation"/></td>
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



