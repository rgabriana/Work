<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security"
	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<spring:url value="/org/networksettings/saveNetworkSettings"
	var="saveNetworkSettingsURL" />
<spring:url value="/org/networksettings/apply/settings"
	var="applyNetworkSettingsURL" />

<spring:url value="/themes/default/images/floorplan64/needSyncFlagGreen.png" var="networkConnected"/>
<spring:url value="/themes/default/images/floorplan64/needSyncFlagRed.png" var="networkNotConnected"/>

<style type="text/css">
.no-close .ui-dialog-titlebar-close {
	display: none
}
</style>
<spring:url value="/scripts/jquery/jquery.blockUI.2.39.js" var="jquery_blockUI"></spring:url>
	<script type="text/javascript" src="${jquery_blockUI}"></script>
	
<script type="text/javascript">

var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
var applyMessage = "Applying network settings.... Please refresh the page after 2-3 minutes.";
var disableGatewayMessage = "Disabling this port will disable Gateway Network and DHCP Server.";
var unmapGatewayMessage = "Mapping Gateway network to NA will disable Gateway Network and DHCP server.";
var pollCalled = false;
var oldSelectedIndex,oldSelectedCorporateIndex;
var subnetErrorMessage="",dgErrorMessage="";

	$(document)
			.ready(
					function() {		
						oldSelectedCorporateIndex = $("#ncI option:selected").text();  
						oldSelectedIndex =$("#nbI option:selected").text();     
						//create tabs
						$("#innernetworkclasscenter").tabs({
							cache : true
						});
						enableDisableNetworkingTabs();
						$("#innernetworkclasscenter").click();
						$(".ui-layout-center").css("overflow", "hidden");
						var buildingEth = "${nimBuilding.networkSettings.name}";
						var corporateEth = "${nimCorporate.networkSettings.name}";						
						enableDisableCorporateEth(oldSelectedCorporateIndex);
						disableBuildingEth(oldSelectedIndex);						
												
					});

	function disableTabs(tabList) {
		$("#innernetworkclasscenter").tabs({
			disabled : tabList
		});
	}
	
	function disableBuildingEth(i){
		var nCI = $("#ncI option:selected").text();
		var nbI = $("#nbI option:selected").text();
		var nbacI = $("#nbacI option:selected").text();
		
		var isDHCPEnabled = "${isDHCPEnabled}";
		
		var checked = (isDHCPEnabled == 'true');		
		$("#dhcpserver" + i).prop('checked', checked);
		var isChecked = ($('#dhcpserver'+i).is(":checked"));
		if(isChecked){
			$("#configureIPV4" + i).prop('disabled', true);
			$("#subnet_mask" + i).prop('disabled', true);
			$("#default_gateway" + i).prop('disabled', true);
			$("#ipaddress" + i).prop('disabled', true);
			$("#dns" + i).prop('disabled', true);
			$("#search_domain_fields" + i).prop('disabled', true);	
		}
		if($("#configureIPV4"+i).val() == 'DHCP'){
			$("#subnet_mask" + i).prop('disabled', true);
			$("#default_gateway" + i).prop('disabled', true);
			$("#ipaddress" + i).prop('disabled', true);
			$("#dns" + i).prop('disabled', true);
			$("#search_domain_fields" + i).prop('disabled', true);
		}
		if(nbI == 'NA') {
			if(nbacI != 'NA') {
				$("#dhcpservertr"+nbacI).hide();
			}
			$("#dhcpservertr"+nCI).hide();			
		}else if(nbI != nbacI){
			$("#dhcpservertr"+nbacI).hide();
		}
		else{
			$("#dhcpservertr"+i).show();
		}
		if(nbacI != 'NA') {
			if($("#configureIPV4"+nbacI).val() == 'DHCP'){
				$("#subnet_mask" + nbacI).prop('disabled', true);
				$("#default_gateway" + nbacI).prop('disabled', true);
				$("#ipaddress" + nbacI).prop('disabled', true);
				$("#dns" + nbacI).prop('disabled', true);
				$("#search_domain_fields" + nbacI).prop('disabled', true);
			}			
		}
	}
	
	function enableDisableCorporateEth(i){
	
		$("#enablePort" + i).prop('disabled', true);
		$("#enablePort" + i).prop('checked', true);
		$("#dhcpserver" + i).prop('checked', false);		
		if($("#configureIPV4"+i).val() == 'DHCP'){
			$("#subnet_mask" + i).prop('disabled', true);
			$("#default_gateway" + i).prop('disabled', true);
			$("#ipaddress" + i).prop('disabled', true);
			$("#dns" + i).prop('disabled', true);
			$("#search_domain_fields" + i).prop('disabled', true);
		}
		$("#dhcpservertr"+i).hide();
	}
	
	function enableDisableDHCPServer(i){
		var gatewaysPresent = '${gatewaysPresent}';
		
		if(i == $("#nbI option:selected").text()){
			var isChecked = ($('#dhcpserver'+i).is(":checked"));
			if(isChecked){
				$("#"+"ipaddress"+i).val("169.254.0.1");
				$("#"+"subnet_mask"+i).val("255.255.0.0");
				$("#"+"default_gateway"+i).val("");			
				$("#"+"configureIPV4"+i).val("Static");
			}else{
				if(gatewaysPresent == 'true'){
					$('<div></div>').appendTo('body')
					  .html('There are gateways already discovered/commissioned in the network. Are you sure you want to proceed?')				  
					  .dialog({
					      modal: true, title: 'Do you want to disable DHCP server?', zIndex: 10000, autoOpen: true,
					      width : 500,
						  height : 150, resizable: false,
					      buttons: {
					          Yes: function () {
					        	  isChecked = false;
					              $(this).dialog("close");
					          },
					          No: function () {
					        	  isChecked = true;		
									$("#dhcpserver" + i).prop('checked', true);
									$("#configureIPV4" + i).prop('disabled', isChecked);
									$("#subnet_mask" + i).prop('disabled', isChecked);
									$("#default_gateway" + i).prop('disabled', isChecked);
									$("#ipaddress" + i).prop('disabled', isChecked);
									$("#dns" + i).prop('disabled', isChecked);
									$("#search_domain_fields" + i).prop('disabled', isChecked);	
					              $(this).dialog("close");
					          }
					      },
					      close: function (event, ui) {
					          $(this).remove();
					      }
					});				
					
					}else{
							isChecked = false;
						} 
			}
			
			$("#configureIPV4" + i).prop('disabled', isChecked);
			$("#subnet_mask" + i).prop('disabled', isChecked);
			$("#default_gateway" + i).prop('disabled', isChecked);
			$("#ipaddress" + i).prop('disabled', isChecked);
			$("#dns" + i).prop('disabled', isChecked);
			$("#search_domain_fields" + i).prop('disabled', isChecked);			
		}		
	}
	
	function enableDisablePort(i){
		var nbIText = $("#nbI option:selected").text();
		var isPortChecked = ($('#enablePort'+i).is(":checked"));
		
		if(i == nbIText){
			if(!isPortChecked){
				$("#disablePortText"+i).html(disableGatewayMessage);
				$("#disablePortText"+i).css("color", COLOR_FAILURE);
				$("#disablePortText"+i).show();
				$("#dhcpserver" + i).prop('disabled', true);
				$("#dhcpserver" + i).prop('checked', false);
				$("#configureIPV4" + i).prop('disabled', true);
				$("#subnet_mask" + i).prop('disabled', true);
				$("#default_gateway" + i).prop('disabled', true);
				$("#ipaddress" + i).prop('disabled', true);
				$("#dns" + i).prop('disabled', true);
				$("#search_domain_fields" + i).prop('disabled', true);
			}else{
				$("#disablePortText"+i).hide();
				$("#dhcpserver" + i).prop('disabled', false);
				$("#dhcpserver" + i).prop('checked', true);
				$("#"+"ipaddress"+i).val("169.254.0.1");
				$("#"+"subnet_mask"+i).val("255.255.0.0");
				$("#"+"default_gateway"+i).val("");			
				$("#"+"configureIPV4"+i).val("Static");
				$("#configureIPV4" + i).prop('disabled', true);
				$("#subnet_mask" + i).prop('disabled', true);
				$("#default_gateway" + i).prop('disabled', true);
				$("#ipaddress" + i).prop('disabled', true);
				$("#dns" + i).prop('disabled', true);
				$("#search_domain_fields" + i).prop('disabled', true);	
			}
		}else{
			 $("#dhcpserver" + i).prop('disabled', true);
			 $("#dhcpserver" + i).prop('checked', false);	
		}		
			
	}
	
	function getVal(s, i) {
		if (s == 'DHCP') {
			$("#subnet_mask" + i).prop('disabled', true);
			$("#default_gateway" + i).prop('disabled', true);
			$("#ipaddress" + i).prop('disabled', true);
			$("#dns" + i).prop('disabled', true);
			$("#search_domain_fields" + i).prop('disabled', true);
		} else {
			$("#subnet_mask" + i).prop('disabled', false);
			$("#default_gateway" + i).prop('disabled', false);
			$("#ipaddress" + i).prop('disabled', false);
			$("#dns" + i).prop('disabled', false);
			$("#search_domain_fields" + i).prop('disabled', false);
		}
	}

	var tabselected;
	var isFormValid = true;

		
	function getNimXML(){
		var nimXML = '<networkInterfaceMappings><networkInterfaceMapping><id>'+${nimBuilding.id}+'</id><networkSettingsId>'+$('#nbI').val()+'</networkSettingsId><networkTypeId>'+1+'</networkTypeId></networkInterfaceMapping><networkInterfaceMapping><id>'+${nimCorporate.id }+'</id><networkSettingsId>'+$('#ncI').val()+'</networkSettingsId><networkTypeId>'+2+'</networkTypeId></networkInterfaceMapping>';
		var nimBacnet = '<networkInterfaceMapping><id>'+${nimBacnet.id}+'</id><networkSettingsId>'+$('#nbacI').val()+'</networkSettingsId><networkTypeId>'+3+'</networkTypeId></networkInterfaceMapping>';
		var nimXMLEnd = '</networkInterfaceMappings>';
		
		if("${isBacnetEnabled}" ==  "true"){
			
			nimXML = nimXML+nimBacnet+nimXMLEnd;
		}else{
			
			nimXML = nimXML+nimBacnet+nimXMLEnd;
		}		
		return nimXML;
	}
	
	function getNSXML(){
		var nsXML = '<interfacess>';
		var configureIPV4Value ="";			
		var nCIText = $("#ncI option:selected").text();
		var nbIText = $("#nbI option:selected").text();
		var nbacIText = $("#nbacI option:selected").text();		
		var includedArray=[];		
		includedArray[0] = nCIText;
		if(nbIText != 'NA')includedArray[1] = nbIText;
		if(nbacIText != 'NA') includedArray[2] = nbacIText;		
		for(i in includedArray){
			var x = includedArray[i];
			configureIPV4Value =$("#configureIPV4"+x).val();			
			nsXML = nsXML + '<interfaces><name>'+x+'</name><enablePort>'+$('#enablePort'+x).is(":checked")+'</enablePort><is_dhcp_server>'+$('#dhcpserver'+x).is(":checked")+'</is_dhcp_server><default_gateway>'+$("#default_gateway"+x).val()+'</default_gateway><dns>'+$("#dns"+x).val()+'</dns><search_domain_fields>'+$("#search_domain_fields"+x).val()+'</search_domain_fields><ipaddress>'+$('#ipaddress'+x).val()+'</ipaddress><subnet_mask>'+$('#subnet_mask'+x).val()+'</subnet_mask><macaddress>'+$("#macaddress"+x).val()+'</macaddress>'+'<configureIPV4>'+configureIPV4Value+'</configureIPV4></interfaces>';
		}
		nsXML = nsXML + '</interfacess>';
		return nsXML;
	}

	

	function validateForm(){
		var nCIText = $("#ncI option:selected").text();
		var nbIText = $("#nbI option:selected").text();
		var nbacIText = $("#nbacI option:selected").text();
		
		var includedArray=[];
		
		includedArray[0] = nCIText;
		if(nbIText != 'NA') includedArray[1] = nbIText;
		if(nbacIText != 'NA') includedArray[2] = nbacIText;
		var formStatus = true;
		
		for(i in includedArray){
		
			var ip = $("#ipaddress"+includedArray[i]).val();
			var mask = $("#subnet_mask"+includedArray[i]).val();
			var dg = $("#default_gateway"+includedArray[i]).val();
			var isChecked = ($('#enablePort'+includedArray[i]).is(":checked"));
			if(!formStatus){						
			}else{
				if(isChecked && $("#configureIPV4"+includedArray[i]).val() != 'DHCP'){
					formStatus = validateFormIP(ip,mask,dg,includedArray[i]);					
				}				 
			}
		}
		
		return formStatus;	
	}
	
	function validateSubnetAndGateway(nCI,nBI,nbacI){
		var subnetAndGatewayValidationStatus = true;
		var isSubnetUnique=true, isGatewayUnique=true;
		subnetErrorMessage="",dgErrorMessage="";
		var nCIpaddress = $("#ipaddress"+nCI).val();
		var nBIpaddress = $("#ipaddress"+nBI).val();		
		var nbacIpaddress = $("#ipaddress"+nbacI).val();		
		var nCISubnetVal = $("#subnet_mask"+nCI).val();
		var nBISubnetVal = $("#subnet_mask"+nBI).val();
		var nbacISubnetVal = $("#subnet_mask"+nbacI).val();
		
		var isChecked = ($('#dhcpserver'+nBI).is(":checked"));
		
		var nCIGatewayVal = $("#default_gateway"+nCI).val();
		var nBIGatewayVal = $("#default_gateway"+nBI).val();
		var nbacIGatewayVal = $("#default_gateway"+nbacI).val();
		
		var nCIIPV4Val = $("#"+"configureIPV4"+nCI).val();
		var nBIIPV4Val = $("#"+"configureIPV4"+nBI).val();
		var nbacIIPV4Val = $("#"+"configureIPV4"+nbacI).val();
		
		subnetErrorMessage = "Subnet masks values";
		dgErrorMessage ="Default Gateway values";		
		clearConfigMessage();
		
		if(nBI!= 'NA' && !isChecked && nCIIPV4Val!='DHCP' && nBIIPV4Val!='DHCP'){
			isSubnetUnique = isSameSubNetwork(nCIpaddress,nBIpaddress,nCISubnetVal,nBISubnetVal);
			if(!isSubnetUnique) return false;
		}
		
		if(nbacI !='NA' && nbacI != nCI){	
			if(nCIIPV4Val!='DHCP' && nbacIIPV4Val!='DHCP'){
				isSubnetUnique = isSameSubNetwork(nCIpaddress,nbacIpaddress,nCISubnetVal,nbacISubnetVal);
				if(!isSubnetUnique) return false;				
			}
			
		}
		
		if(nBI!= 'NA' && nbacI !='NA' && nbacI != nBI){			
			if(!isChecked  && nBIIPV4Val!='DHCP' && nbacIIPV4Val!='DHCP'){	
				isSubnetUnique = isSameSubNetwork(nBIpaddress,nbacIpaddress,nBISubnetVal,nbacISubnetVal);
				if(!isSubnetUnique) return false;
			}
		}
		
		
		if(nCIGatewayVal == nBIGatewayVal && !isChecked &&  nCIIPV4Val!='DHCP' && nBIIPV4Val!='DHCP'){	
			isGatewayUnique = false;	
		
		}
		if(nbacI !='NA' && nbacI != nCI ){			
			if(nbacIGatewayVal == nCIGatewayVal && nCIIPV4Val!='DHCP' && nbacIIPV4Val!='DHCP'){
				isGatewayUnique = false;
			}
		}
		if(nbacI !='NA' && nbacI != nBI){			
			if(nbacIGatewayVal == nBIGatewayVal && !isChecked && nBIIPV4Val!='DHCP' && nbacIIPV4Val!='DHCP'){	
				isGatewayUnique = false;
			}
		}
		
		subnetAndGatewayValidationStatus = isGatewayUnique && isSubnetUnique;		
		return subnetAndGatewayValidationStatus;
			
		}
	
	  function isSameSubNetwork(ip1,ip2,mask1,mask2){		 
		  	var isSameSubNetworkFlagValid=true;
		  	var ipPattern = /^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/;		
			
			var ipArray1 = ip1.match(ipPattern);
			var maskArray1 = mask1.match(ipPattern);
			var ipArray2 = ip2.match(ipPattern);
			var maskArray2 = mask2.match(ipPattern);
			
	 
	   	if(ipArray1 != null && ipArray2 != null){
	   	   	if(mask1 == mask2){
		   		var nOctA1=ipArray1[1] & maskArray1[1];
			   	var nOctA2=ipArray1[2] & maskArray1[2];
			   	var nOctA3=ipArray1[3] & maskArray1[3];
			   	var nOctA4=ipArray1[4] & maskArray1[4];
			   	var nOctB1=ipArray2[1] & maskArray2[1];
			   	var nOctB2=ipArray2[2] & maskArray2[2];
			   	var nOctB3=ipArray2[3] & maskArray2[3];
			   	var nOctB4=ipArray2[4] & maskArray2[4]
		   		
		   		if ((nOctA1==nOctB1) && (nOctA2==nOctB2) && (nOctA3==nOctB3) && (nOctA4==nOctB4)){
		   			isSameSubNetworkFlagValid = false;		   			
				}else{
					isSameSubNetworkFlagValid = true;					
				}	   		
		   	}else{		   		
		   		if(ipArray1[1] == ipArray2[1]){
					if(ipArray1[2] == ipArray2[2]){
						if(ipArray1[3] == ipArray2[3]){
							isSameSubNetworkFlagValid = false;						
						}
		   	}		
		   	return isSameSubNetworkFlagValid;
		    }
		   	}
	   	}
		return isSameSubNetworkFlagValid;
	
	    }	
	
	
	function validateFormIP(ip,mask,dg,interfaceName){		
		var nbI = $("#nbI option:selected").text();
		var formStatus=true;
		var ipPattern = /^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/;		
		var ipArray = ip.match(ipPattern);
		var maskArray = mask.match(ipPattern);
		var dgArray = dg.match(ipPattern);
		if (ip == "0.0.0.0" ) {
			$("#ipaddress"+interfaceName).css("border","1px solid red");	
			formStatus=false;
		} else{
			$("#ipaddress"+interfaceName).css("border","1px solid gray");
		}
		if(mask == "0.0.0.0"){
			$("#subnet_mask"+interfaceName).css("border","1px solid red");	
			formStatus=false;
		}else{
			$("#subnet_mask"+interfaceName).css("border","1px solid gray");	
		}
		if(dg == "0.0.0.0"){
			$("#default_gateway"+interfaceName).css("border","1px solid red");	
			formStatus=false;
		}else{
			$("#default_gateway"+interfaceName).css("border","1px solid gray");	
		}
		if (ip == "255.255.255.255" || mask == "255.255.255.255" || dg == "255.255.255.255") {
			formStatus=false;			
		}
		if (ipArray == null ) {
			$("#ipaddress"+interfaceName).css("border","1px solid red");
			formStatus=false;
		}else{
			$("#ipaddress"+interfaceName).css("border","1px solid gray");
		}
		if(maskArray == null){
			$("#subnet_mask"+interfaceName).css("border","1px solid red");
			formStatus=false;
		}else{
			$("#subnet_mask"+interfaceName).css("border","1px solid gray");
		}
		if(dgArray == null){
			var isChecked = ($('#dhcpserver'+interfaceName).is(":checked"));			
			if(nbI == interfaceName && isChecked){
				$("#default_gateway"+interfaceName).css("border","1px solid gray");
				formStatus = true;
			}else{
				$("#default_gateway"+interfaceName).css("border","1px solid red");
				formStatus=false;				
			}			
		}else{
			$("#default_gateway"+interfaceName).css("border","1px solid gray");
		}
		
			for (i = 1; i < 5; i++) {					
				if ((ipArray != null && ipArray[i] > 255) ||(maskArray != null && maskArray[i] > 255) || (dgArray != null && dgArray[i] > 255)) {
					return false;
				}
			}
		
		return formStatus;
		}
	
	function updateRow() {		
		var nCIText = $("#ncI option:selected").text();
		var nbIText = $("#nbI option:selected").text();
		var nbacIText = $("#nbacI option:selected").text();		
		var validateFormStatus = validateForm();
		if(!isFormValid || !validateFormStatus){	
		displayConfigMessage("Please check the settings", COLOR_FAILURE);			
			return;
		}else{
			var subnetAndGatewayStatus = validateSubnetAndGateway(nCIText,nbIText,nbacIText);
			if(!subnetAndGatewayStatus){
				displayConfigMessage("All interfaces should belong to different networks.", COLOR_FAILURE);	
				return;
			}else{
				clearConfigMessage();
				formsubmit();				
			}
						
		}
		return false;
	}
	
	function formsubmit(){
		pollCalled = false;
	//	console.log('called form submit'+location.host);
	//console.log("Calling formsubmit");
		var nCI = $("#ncI option:selected").text();
		var serverError = false;		
		var nimXML = getNimXML();
		var nsXML = getNSXML();
		var ndStart = '<networkDetails>';
		var ndEnd = '</networkDetails>';
		var ndXML = ndStart + nimXML + nsXML + ndEnd;
		
							$.ajax({
					            type: "POST",					            
					            contentType : "application/xml",
					            url: '<spring:url value="/services/org/networksettings/apply/settings"/>',
					            data : ndXML,
					            beforeSend: function(){
					            	$.blockUI({ 
					            		message: '<spring:message code="network.sumbit.wait"/>',
					            		css: { 
					                        border: 'none', 
					                        padding: '15px', 
					                        backgroundColor: '#000000', 
					                        '-webkit-border-radius': '10px', 
					                        '-moz-border-radius': '10px', 
					                        opacity: .5, 
					                        color: '#FFFFFF' 
					                    }
					            	});
					            	
					            },
					            success: function(msg){
					            	//console.log("success called");	
					            	if(!pollCalled) poll();
					            },
					            error: function() {
			                    	poll();
			                    },
					            complete: function(){
					            //	console.log("settings applied in complete");
					            	if(!pollCalled) poll();
					            }
							});
		
	}		
		function poll() {
			pollCalled = true;
			var nCI = $("#ncI option:selected").text();
			var pollServer = null;
			var ip = null;
			
								ip = $("#"+"ipaddress"+nCI).val();
								var port = "";

								if(location.host.indexOf(":") >= 0) {
									port = location.host.split(':');
									port = port[1];
								}
								var count = 0;
								if(port != "") {
									ip = ip + ":" + port;
								}
								pollServer = setInterval( function() {
									 $.ajax({
					                    type: "GET",					                    
					                    dataType: "html",
					                    crossDomain: true,
					                    url: '<spring:url value="/services/org/networksettings/applySettingsStatus"/>' ,
					                    beforeSend: function(){
					                    },
					                    success: function(msg){
					                    	if(msg == 'SUCCESS'){
					                    		//console.log("connected to em"+msg);
												clearInterval(pollServer);
												if(location.host == 'localhost'){
													ip = 'localhost';
												}
						                    	$('.blockMsg').html('<a href="' + location.protocol + '//' + ip  + '/ems' + '" id="login" >Network Settings applied. Please re-login to the application.</a>');
						                    	$(window).unbind('beforeunload');
						                    	$("div").css('cursor', 'default');
					                    	}else if(msg == 'FAILURE'){
					                    		//console.log("connected to em"+msg);
												clearInterval(pollServer);
												if(location.host == 'localhost'){
													ip = 'localhost';
												}
												$('.blockMsg').html('<a href="' + location.protocol + '//' + ip  + '/ems' + '" id="login" >Some error occured. Please contact Admin</a>');
						                    	$(window).unbind('beforeunload');
						                    	$("div").css('cursor', 'default');
					                    	}
					                    	
					                    },
					                    complete: function(transport){					                    	
					                    	count++;
					                    },
					                    error: function() {
					                    	//console.log("failed to connect to em");
					                    }
									});
									 
									 if(count > 5) {
										clearInterval(pollServer);
										$('.blockMsg').html('<span><a href="' + location.protocol + '//' + ip  + '/ems' + '" id="login" >Network Settings applied.</a></span>');
										$(window).unbind('beforeunload');
										$("div").css('cursor', 'default');
									 } 
										
								}, 10000);							
							 
						}
		 


	function displayConfigMessage(Message, Color) {
		$("#networkconfigerror").html(Message);
		$("#networkconfigerror").css("color", Color);
	}
	function clearConfigMessage() {
		var nbI = $("#nbI option:selected").text();
		if(nbI != 'NA'){
			$("#disableGatewayText").html("");
			$("#disableGatewayText").css("color", COLOR_DEFAULT);	
		}
		
		 $("#networkconfigerror").html("");
		 $("#networkconfigerror").css("color", COLOR_DEFAULT);
	}
	function validateMapping(networkType)
	{
		
		var nCI = $("#ncI option:selected").text();
		var nbI = $("#nbI option:selected").text();
		var nbacI = $("#nbacI option:selected").text();
		
		isFormValid = true;
		clearConfigMessage();
		
		if(nCI == nbI){
			displayConfigMessage("Corporate and Gateway network can not be on the same interface.", COLOR_FAILURE);
			isFormValid = false;	
			return;
		}
		
		if(nCI != 'NA'){
			$("#disablePortText"+nCI).hide();
			//Check if is configured earlier by user. Kepp the same settings if configured
			var confIPV_Corp ="${nimCorporate.networkSettings.configureIPV4}";			
			if(confIPV_Corp != undefined && confIPV_Corp != null && confIPV_Corp == 'Static'){
				$("#"+"configureIPV4"+nCI).val("Static");
				$("#"+"configureIPV4"+nCI).prop('disabled', false);
				$("#"+"ipaddress"+nCI).val('${nimCorporate.networkSettings.ipaddress}');
				$("#"+"ipaddress"+nCI).prop('disabled', false);
				$("#"+"subnet_mask"+nCI).prop('disabled', false);
				$("#"+"subnet_mask"+nCI).val('${nimCorporate.networkSettings.subnet_mask}');
				$("#"+"default_gateway"+nCI).prop('disabled', false);
				$("#"+"default_gateway"+nCI).val('${nimCorporate.networkSettings.default_gateway}');
				$("#"+"dns"+nCI).prop('disabled', false);
				$("#"+"search_domain_fields"+nCI).prop('disabled', false);
				$("#dhcpserver" + nCI).prop('checked', false);
				$("#dhcpservertr"+nCI).hide();
				$("#enablePort" + nCI).prop('disabled', true);
				$("#enablePort" + nCI).prop('checked', true);
			}else{
				$("#"+"configureIPV4"+nCI).val(confIPV_Corp);
				$("#"+"configureIPV4"+nCI).prop('disabled', false);
				$("#dhcpserver" + nCI).prop('checked', false);
				$("#dhcpservertr"+nCI).hide();
				$("#enablePort" + nCI).prop('disabled', true);
				$("#enablePort" + nCI).prop('checked', true);
				getVal($("#"+"configureIPV4"+nCI).val(), nCI);
			}
		}		
		if(nbI != 'NA'){			
			var isDHCPEnabled = "${isDHCPEnabled}";
			var checked = (isDHCPEnabled == 'true');
			var bldIP = '${nimBuilding.networkSettings.ipaddress}';
			var bldSN = '${nimBuilding.networkSettings.subnet_mask}';
			var bldDG = '${nimBuilding.networkSettings.default_gateway}'
			if(bldIP !=''){
				$("#"+"ipaddress"+nbI).val('${nimBuilding.networkSettings.ipaddress}');
			}else{
				$("#"+"ipaddress"+nbI).val('169.254.0.1');
			}
			if(bldSN != ''){
				$("#"+"subnet_mask"+nbI).val('${nimBuilding.networkSettings.subnet_mask}');
			}else{
				$("#"+"subnet_mask"+nbI).val('255.255.255.0');
			}
			if(bldDG != ''){
				$("#"+"default_gateway"+nbI).val('${nimBuilding.networkSettings.default_gateway}');
			}else{
				$("#"+"default_gateway"+nbI).val('');
			}
			
			
			
			//$("#"+"ipaddress"+nbI).val("169.254.0.1");
			//$("#"+"subnet_mask"+nbI).val("255.255.0.0");
			//$("#"+"default_gateway"+nbI).val("");			
			$("#"+"configureIPV4"+nbI).val("Static");
			$("#dhcpserver" + nbI).prop('checked', checked);
			$("#"+"configureIPV4"+nbI).prop('disabled', $("#dhcpserver"+nbI).is(":checked"));
			$("#"+"ipaddress"+nbI).prop('disabled',  $("#dhcpserver"+nbI).is(":checked"));
			$("#"+"subnet_mask"+nbI).prop('disabled',  $("#dhcpserver"+nbI).is(":checked"));
			$("#"+"default_gateway"+nbI).prop('disabled',  $("#dhcpserver"+nbI).is(":checked"));
			$("#"+"dns"+nbI).prop('disabled',  $("#dhcpserver"+nbI).is(":checked"));
			$("#"+"search_domain_fields"+nbI).prop('disabled',  $("#dhcpserver"+nbI).is(":checked"));
			$("#dhcpservertr"+nbI).show();
			$("#enablePort" + nbI).prop('disabled', false);
			$("#enablePort" + nbI).prop('checked', true);
			$("#disablePortText"+nbI).hide();
			$("#dhcpserver" + nbI).prop('disabled', false);
			$("#disableGatewayText").hide();
			
		}else{			
			$("#disableGatewayText").html(unmapGatewayMessage);
			$("#disableGatewayText").css("color", COLOR_FAILURE);			
			$("#disableGatewayText").show();
		}
		
		if((nbacI != 'NA' && nbI!=nbacI) && (nbacI != 'NA' && nCI!=nbacI)){
			$("#disablePortText"+nbacI).hide();
			$("#"+"configureIPV4"+nbacI).val("Static");
			$("#"+"configureIPV4"+nbacI).prop('disabled', false);
			$("#"+"ipaddress"+nbacI).prop('disabled', false);
			$("#"+"subnet_mask"+nbacI).prop('disabled', false);
			$("#"+"default_gateway"+nbacI).prop('disabled', false);
			$("#"+"dns"+nbacI).prop('disabled', false);
			$("#"+"search_domain_fields"+nbacI).prop('disabled', false);
			$("#dhcpserver" + nbacI).prop('checked', false);
			$("#dhcpservertr"+nbacI).hide();
			$("#enablePort" + nbacI).prop('disabled', false);
			$("#enablePort" + nbacI).prop('checked', true);
		}		
		
		
		
		enableDisableNetworkingTabs();
		selectActiveTab();		
	}
	
	var mappedInterface = [];
	var includedArray=[];
	var excludedArray = [];
	function enableDisableNetworkingTabs()
	{	
		var nCI = $("#ncI option:selected").text();
		var nbI = $("#nbI option:selected").text();
		var nbacI = $("#nbacI option:selected").text();
		mappedInterface = [];
		<c:forEach items="${ethList1}" varStatus="i" var="mapObj">
		var mappedObj = "${mapObj}";
		var mappedObjArr = mappedObj.split("=");
		includedArray=[];
		mappedInterface["${i.index}"] = mappedObjArr[1];
		</c:forEach>
		for(var iObj in mappedInterface)
		{
			var item = mappedInterface[iObj];
			
			if(nCI==item)
			{
				includedArray.push(iObj);				
				
			}
			if(nbI==item)
			{
				includedArray.push(iObj);				
				
			}
			if(nbacI==item)
			{
				includedArray.push(iObj);				
				
			}
		}
		excludedArray = [];
		for(var selObj in mappedInterface)
		{
			var isfound = false;
			for(var iObj in includedArray)
			{
				var xyz = includedArray[iObj];
				if(xyz == selObj)
				{
					isfound = true;
					break;
				}
			}
			if( !isfound ){
				excludedArray.push(parseInt(selObj));
			}
		}
		
		
		selectActiveTab();
	}
	
	function selectActiveTab()
	{
		var nCIVal = $("#ncI option:selected").text();
		
		for(var iObj in mappedInterface)
		{
			var item = mappedInterface[iObj];
		
			if(nCIVal==item)
			{
				//console.log("Tab Selected " + iObj);
				$('#innernetworkclasscenter').tabs('select', iObj);
				$("#innernetworkclasscenter").click();
				break;
			}
		}
		disableTabs(excludedArray);
	
	}
	
	
</script>

<div id="innercenter" class="outermostdiv outerContainer" style="height: 100%;">
		<form:form id="network-settings"  onsubmit="return false;"
		 commandName="networkInterfaceMapping">
		<div>
		<table cellspacing="5px">
						<tr>
							<td class="formValue"><input id="btn_saveConfiguration" onclick="updateRow()" 
								type="button" value="Save and Apply Configuration">
								</td>
							<td>
								<div>
									<span style="padding-right: 20px"></span>
								</div>
							</td>							
						<td>
							<div style="width: 100%">
								<span id="networkconfigerror" class="error"
									style="font-weight: bold; padding-left: 20px"></span>
							</div>
						</td>
						</tr>						
		</table>
		</div>
		<table cellspacing="5px">

			<tr>
				<td>
					
					</td>
			</tr>
			<tr>
				<form:form id="corporate" commandName="nimCorporate">
					<td class="formPrompt">Corporate Network</td>
					<td><form:hidden path="id" /> <form:select id="ncI" onchange="validateMapping('corporate')"
							path="mappedNetworkInterface" name="mappedNetworkInterface">
							<form:options items="${ethList1}" />
						</form:select></td>		
								
				</form:form>
			</tr>
			<tr >
				<form:form id="building" commandName="nimBuilding" >
					<td class="formPrompt">Gateway Network</td>
					<td><form:hidden path="id" /> <form:select id="nbI"   onchange="validateMapping('building')"
							path="mappedNetworkInterface" name="mappedNetworkInterface">
							<form:option value="0" label="NA" />
							<form:options items="${ethList1}" />
						</form:select>  <label id="disableGatewayText"></label></td>							
				</form:form>
			</tr>
			<tr style="visibility: ${isBacnetEnabled == 'true' ? 'visible' : 'hidden'}">
				<form:form id="bacnet" commandName="nimBacnet">
					<td class="formPrompt">Bacnet Network </td>
					<td><form:hidden path="id" /> <form:select id="nbacI" onchange="validateMapping('bacnet')"
							path="mappedNetworkInterface" name="mappedNetworkInterface">
							<form:option value="0" label="NA" />
							<form:options items="${ethList1}" />
						</form:select>
					</td>
					
				</form:form>
			</tr>			
		</table>
	</form:form>
	
	<div id="innernetworkclasscenter"
		class="ui-layout-center pnl_rht_inner">

			<ul
				style="-moz-border-radius-bottomleft: 0; -moz-border-radius-bottomright: 0;">
				<c:forEach items="${networkSettingsList}" var="networkSettings"
					varStatus="i">
					<li><a id="eth${i.index}" href="#tab${i.index}"
						tabindex="${i.index}"><span><c:out
									value="${networkSettings.name}" /></span> </a></li>
				</c:forEach>
			</ul>

		<div class="ui-layout-content ui-widget-content ui-corner-bottom"
			style="border-left: 0; border-top: 0; padding: 0px;">
			<spring:url value="/settings/saveNetworkSettings.ems" var="actionURL"
				scope="request" />
				
			<c:forEach items="${networkSettingsList}" var="networkSettingsVar"
				varStatus="i">
				<div id="tab${i.index}" class="pnl_rht">
					<c:set var="currentNetworkType"
							value="${mappedValues[networkSettingsVar.name]}"></c:set>
					<form:form id="network-settings${networkSettingsVar.name}" method="post" onsubmit="return false;"
						commandName="networksettings${i.index}">
					
						<table cellspacing="10px">
							<form:hidden path="id" id="networkSettingsId${networkSettingsVar.name}"/>
							<form:hidden path="macaddress" />
							<form:hidden path="name" />
							<%-- <tr>
								<td class="formPrompt">Current configuration: <c:out
										value="${currentNetworkType}" /></td>
							</tr> --%>
							<tr>
								<td class="formValue"><form:checkbox path="enablePort" onchange="enableDisablePort('${networkSettingsVar.name}');"
										id="enablePort${networkSettingsVar.name}" /></td>
								<td  class="formPrompt"><label id="enablePortText${networkSettingsVar.name}">Enable Port</label></td>	
								<td  class="formPrompt"><label id="disablePortText${networkSettingsVar.name}"></label></td>								
							</tr>
							<tr>
								<td class="formPrompt">Mac Address -</td>
								<td class="formValue"><form:input path="macaddress"
										id="macaddress${networkSettingsVar.name}" disabled="true" /></td>
							</tr>
							<tr>
								<td class="formPrompt">Connection Status : </td>
								<td class="formValue"><c:if
										test="${networkSettingsVar.connected_status  == 'up'}">
										<img src="${networkConnected}" height=16 width=16/> <label style="color: green">Cable is connected</label>
									</c:if> <c:if test="${networkSettingsVar.connected_status  == 'down' || networkSettingsVar.connected_status =='' || networkSettingsVar.connected_status == null }">
										<img src="${networkNotConnected}" height=16 width=16/> <label style="color: red">Cable is disconnected</label>
									</c:if></td>
							</tr>
							<tr id="dhcpservertr${networkSettingsVar.name}">
							<td  class="formPrompt" ><label >DHCP Server</label></td>
								<td class="formValue"><input type="checkbox"  onchange="enableDisableDHCPServer('${networkSettingsVar.name}');"
										id="dhcpserver${networkSettingsVar.name}"  /></td>															
							</tr>
							<tr id="ntColumn1">
								<td class="formPrompt">Configure IPV4:</td>
								<td class="formValue"><form:select path="configureIPV4"
										id="configureIPV4${networkSettingsVar.name}"
										onchange="getVal(value,'${networkSettingsVar.name}');">
										<form:option value="Static" />
										<form:option value="DHCP" />
									</form:select></td>

							</tr>
							<tr >
								<td class="formPrompt">IPV4 Address:</td>
								<td class="formValue"><form:input id="ipaddress${networkSettingsVar.name}"
										path="ipaddress" /></td>
							</tr>							
							<tr >
								<td class="formPrompt">Subnet Mask:</td>
								<td class="formValue"><form:input	id="subnet_mask${networkSettingsVar.name}"										
										path="subnet_mask" class="biginput" /></td>
							</tr>

							<tr >
								<td class="formPrompt">Default Gateway:</td>
								<td class="formValue"><form:input
										id="default_gateway${networkSettingsVar.name}"
										
										path="default_gateway" class="biginput" /></td>
							</tr>
							</div>

							<tr>
								<td class="formPrompt">DNS Server</td>
								<td class="formValue"><input id="dns${networkSettingsVar.name}" class="biginput" /></td>
							</tr>
							<tr>
								<td class="formPrompt">Search Domains</td>
								<td class="formValue"><input id="search_domain_fields${networkSettingsVar.name}"
									path="search_domain_fields" class="biginput" /></td>
							</tr>
						</table>
					</form:form>
				</div>
			</c:forEach>
		</div>
	</div>	

 
</div>