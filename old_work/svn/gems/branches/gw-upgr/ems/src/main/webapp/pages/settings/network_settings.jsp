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
<script type="text/javascript">

var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
var applyMessage = "Applying network settings.... Please refresh the page after 2-3 minutes.";
var disableGatewayMessage = "Disabling this port will disable Gateway Network and DHCP Server.";
var unmapGatewayMessage = "Mapping Gateway network to NA will disable Gateway Network and DHCP server.";

var oldSelectedIndex,oldSelectedCorporateIndex;

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
						enableDisableCorporateEth(corporateEth);
						disableBuildingEth(buildingEth);						
												
					});

	function disableTabs(tabList) {
		$("#innernetworkclasscenter").tabs({
			disabled : tabList
		});
	}
	
	function disableBuildingEth(i){
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
		$("#dhcpservertr"+i).show();
		
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
	
		if(i == $("#nbI option:selected").text()){
			var isChecked = ($('#dhcpserver'+i).is(":checked"));
			if(isChecked){
				$("#"+"ipaddress"+i).val("169.254.0.1");
				$("#"+"subnet_mask"+i).val("255.255.0.0");
				$("#"+"default_gateway"+i).val("");			
				$("#"+"configureIPV4"+i).val("Static");
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
		
			if(!formStatus){						
			}else{				
				
				if($("#configureIPV4"+includedArray[i]).val() != 'DHCP'){
					formStatus = validateFormIP(ip,mask,dg,includedArray[i]);					
				}				 
			}
		}		
		return formStatus;	
	}
	
	function validateFormIP(ip,mask,dg,interfaceName){		
		var formStatus=true;
		var ipPattern = /^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/;		
		var ipArray = ip.match(ipPattern);
		var maskArray = mask.match(ipPattern);
		var dgArray = dg.match(ipPattern);
		if (ip == "0.0.0.0" ) {
			$("#ipaddress"+interfaceName).css("border","1px solid red");	
			formStatus=false;
		}else if(mask == "0.0.0.0"){
			$("#subnet_mask"+interfaceName).css("border","1px solid red");	
			formStatus=false;
		}else if(dg == "0.0.0.0"){
			$("#default_gateway"+interfaceName).css("border","1px solid red");	
			formStatus=false;
		}else if (ip == "255.255.255.255" || mask == "255.255.255.255" || dg == "255.255.255.255") {
			formStatus=false;			
		}
		if (ipArray == null ) {
			$("#ipaddress"+interfaceName).css("border","1px solid red");
			formStatus=false;
		}else if(maskArray == null){
			$("#subnet_mask"+interfaceName).css("border","1px solid red");
			formStatus=false;
		}else if(dg.length >0 && dgArray == null){
			$("#default_gateway"+interfaceName).css("border","1px solid red");
			formStatus=false;
		}
		else {
			for (i = 1; i < 5; i++) {					
				if (ipArray[i] > 255 || maskArray[i] > 255 || (dgArray != null && dgArray[i] > 255)) {
					return false;
				}
			}
		}
		return formStatus;
		}
	
	function updateRow() {
		var validateFormStatus = validateForm();
		if(!isFormValid || !validateFormStatus)		{	
			displayConfigMessage("Form is invalid. Please check the settings", COLOR_FAILURE);			
			return;
		} 
		 
			 var nimXML = getNimXML();
				var nsXML = getNSXML();
		
				 $.ajax({
							type : "POST",
							url : '<spring:url value="/services/org/networksettings/saveNetworkInterfaceMappings"/>',
							contentType : "application/xml",
							data : nimXML,
							success : function(msg) {
								
								$.ajax({
									type : "POST",
									url : '<spring:url value="/services/org/networksettings/saveNetworkSettings"/>',
									contentType : "application/xml",
									data : nsXML,
									success : function(msg) {
										displayConfigMessage("Network Settings saved", COLOR_SUCCESS);
										window.location.reload();
									}
								}); 
								
							}
						});
				}			 
		 

	function applyConfiguration(){
		var ret = confirm("Are you sure you want to proceed? Please take a note of the IP addresses you have configured.");
		if (ret == false)
		{	
			return false;
		}
		$("#btn_applyConfiguration").attr('disabled',true);
		displayConfigMessage(applyMessage, COLOR_SUCCESS);	
		$.ajax({
			type : "GET",
			url : '<spring:url value="/services/org/networksettings/apply/settings"/>',
			contentType : "application/xml",			
			success : function(msg) {
				$("#btn_applyConfiguration").attr('disabled',false);
				displayConfigMessage("Network Settings applied", COLOR_SUCCESS);				
					}
				}); 
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
			displayConfigMessage("Building and Corporate interface can not be same", COLOR_FAILURE);
			isFormValid = false;	
			return;
		}		
		
		
		if(nCI != 'NA'){
			$("#disablePortText"+nCI).hide();
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
		}		
		if(nbI != 'NA'){			
			var isDHCPEnabled = "${isDHCPEnabled}";
			var checked = (isDHCPEnabled == 'true');
			$("#"+"ipaddress"+nbI).val("169.254.0.1");
			$("#"+"subnet_mask"+nbI).val("255.255.0.0");
			$("#"+"default_gateway"+nbI).val("");			
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
								type="button" value="Save Configuration">
								</td>
							<td>
								<div>
									<span style="padding-right: 20px"></span>
								</div>
							</td>
							<td class="formValue"><input id="btn_applyConfiguration" 
								type="submit" value="Apply Configuration"  onclick="applyConfiguration();" />
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
								<td class="formPrompt">DNS</td>
								<td class="formValue"><input id="dns${networkSettingsVar.name}" class="biginput" /></td>
							</tr>
							<tr>
								<td class="formPrompt">Search Domain Fields</td>
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