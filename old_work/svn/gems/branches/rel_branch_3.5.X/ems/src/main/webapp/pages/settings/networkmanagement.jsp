<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<spring:url value="/org/networksettings/saveNetworkSettings"
	var="saveNetworkSettingsURL" />
<spring:url value="/settings/load_interfaces.ems" var="loadInterfaces" />
<html>
<head>
<script type="text/javascript">


</script>
</head>
<script type="text/javascript">
	$(document).ready(function() {
		$("#network-settings0").validate({
			rules: {
				ipaddress: {
					required: true
				},
				subnet_mask: {
					required: true,					
				},
				default_gateway: {
					required: false
				}				
			},
			messages: {
				ipaddress: {
					required: '<spring:message code="error.above.field.required"/>',
				},
				subnet_mask: {
					required: '<spring:message code="error.above.field.required"/>',
				},
				default_gateway: {
					required: '<spring:message code="error.above.field.required"/>',
				}
			}
		});
	
		$("#innernetworkclasscenter").tabs();		       
	
		$("#innernetworkclasscenter").click();

	});
	
	function disableTabs(tabList){
		$( "#tabs" ).tabs({			 
			  disabled: [0,1]
			});
	}

	
	function refreshIframe(){		
		var ifr;
		ifr = parent.document.getElementById("masterbuildingFrame");		
		ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = "${loadInterfaces}";
	}
	
	function getDHCPVal(s,i){		
		$("#enablePort"+i).attr('checked', s.checked);
		$("#enablePort"+i).attr('disabled', !s.checked);
	}
	
	function getVal(s,i){		
		 if(s == 'DHCP'){
			 $("#subnet_mask"+i).prop('disabled', true);
			 $("#default_gateway"+i).prop('disabled', true);
			 $("#ipaddress"+i).prop('disabled', true);
		}else{
			$("#subnet_mask"+i).prop('disabled', false);
			 $("#default_gateway"+i).prop('disabled', false);
			 $("#ipaddress"+i).prop('disabled', false);
		} 
	}
</script>

<style type="text/css">
html, body {
	margin: 3px 3px 0px 3px !important;
	padding: 0px !important;
	background: #ffffff;
	overflow: hidden !important;
}
</style>
<body>
<div id="innernetworkclasscenter" class="ui-layout-center pnl_rht_inner">
<div id="tabs" class="plugloaddetailstabsStyle">
	<ul 
		style="-moz-border-radius-bottomleft: 0; -moz-border-radius-bottomright: 0;">
		<c:forEach items="${networkSettingsList}"  var="networkSettings" varStatus="i">
			<li><a id="eth${i.index}" href="#tab${i.index}" tabindex="${i.index}"><span><c:out
							value="${networkSettings.name}"  /></span> </a></li>
		</c:forEach>
	</ul>
	</div>
	<div class="ui-layout-content ui-widget-content ui-corner-bottom"
		style="border-left: 0; border-top: 0; padding: 0px;">
		<spring:url value="/settings/saveNetworkSettings.ems" var="actionURL"
			scope="request" />		
		<c:forEach items="${networkSettingsList}"  var="networkSettingsVar" varStatus="i">
			<div id="tab${i.index}" class="pnl_rht">
				<form:form id="network-settings${i.index}" method="post" action="${actionURL}"
					commandName="networksettings${i.index}">
					<c:set var="currentNetworkType" value="${mappedValues[networkSettingsVar.name]}"></c:set>
					<table cellspacing="10px">
						<form:hidden path="id" />						
						<form:hidden path="macaddress" />
						<form:hidden path="name" />
						<tr>
							<td class="formPrompt">Current configuration:   <c:out value="${currentNetworkType}" /></td>
						</tr>
						<tr>
							<td class="formValue"><form:checkbox path="enablePort" id="enablePort${i.index}"/></td>
							<td class="formPrompt">Enable Port</td>
						</tr>
						<tr>
							<td class="formPrompt">Mac Address -</td>
							<td class="formValue">
								<form:input path="macaddress"  id="macaddress${i.index}" disabled="true"/>
							</td>
						</tr>
						<tr>
							<td class="formPrompt">Connection Status :</td>
							<td class="formValue">
							<c:if test="${networkSettingsVar.connected_status  == 'up'}"> <label style="color:green">Cable is connected</label></c:if>
							<c:if test="${networkSettingsVar.connected_status  == 'down'}"> <label style="color:red">Cable is disconnected</label></c:if>
							</td>
						</tr>
						<tr id="ntColumn" style="${currentNetworkType eq 'Building' ? '' : 'visibility:hidden;' }">
							<td class="formPrompt">DHCP Server:</td>
							<td class="formValue"><form:checkbox path="is_dhcp_server"  onchange="getDHCPVal(this,'${i.index}');" id="isDHCPServer${i.index}" /></td>
						</tr>
						
						<tr id="ntColumn1" >
							<td class="formPrompt">Configure IPV4:</td>
							<td class="formValue">
							<form:select path="configureIPV4" id="configureIPV4${i.index}" onchange="getVal(value,'${i.index}');" disabled="${currentNetworkType eq 'Building'}">
									<form:option value="Static" />
									<form:option value="DHCP" />
								</form:select></td>
								
						</tr>				
						<tr id="ipAddress"  >
							<td class="formPrompt">IPV4 Address:</td>
							<td class="formValue"><form:input id="ipaddress${i.index}" disabled="${currentNetworkType eq 'Building' || networkSettingsVar.configureIPV4 == 'DHCP'}"
									path="ipaddress" class="biginput" /></td>
						</tr>
						<tr id="sMask">
							<td class="formPrompt">Subnet Mask:</td>
							<td class="formValue"><form:input id="subnet_mask${i.index}" disabled="${currentNetworkType eq 'Building' || networkSettingsVar.configureIPV4 == 'DHCP'}"
									path="subnet_mask" class="biginput" /></td>
						</tr>

						<tr id="gatewy">
							<td class="formPrompt">Default Gateway:</td>
							<td class="formValue"><form:input id="default_gateway${i.index}" disabled="${currentNetworkType eq 'Building' || networkSettingsVar.configureIPV4 == 'DHCP'}"
									path="default_gateway" class="biginput" /></td>
						</tr>
						</div>
						
						<tr>
							<td class="formPrompt">DNS</td>
							<td class="formValue"><input id="dns"
									 class="biginput" /></td>
						</tr>
						<tr>
							<td class="formPrompt">Search Domain Fields</td>
							<td class="formValue"><input id="sdf"
									path="search_domain_fields" class="biginput" /></td>
						</tr>

						<tr>
							<td class="formPrompt"><span></span></td>
							<td class="formValue"><input id="saveUserBtn" type="submit" onsubmit="refreshIframe();"
								value="<spring:message code="action.save" />"></td>
						</tr>
					</table>
				</form:form>
			</div>
		</c:forEach>
	</div>
</div>
</body>
</html>