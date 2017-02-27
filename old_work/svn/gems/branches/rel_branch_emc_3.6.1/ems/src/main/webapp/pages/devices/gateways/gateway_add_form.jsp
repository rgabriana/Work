<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<spring:url value="/services/org/gateway/add/macaddr/" var="addGatewayUrl" scope="request" />
<spring:url value="/services/org/gateway/sendgatewayinfo/ipaddr/" var="sendGatewayInfoUrl" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
	
	.ui-dialog-title{font-size: 1.4em; font-weight: bolder;}
	
	div.message-text {font-weight:bold; float: left; padding-top: 5px;}
	div.field-label{float:left; width:39%; font-weight:bold;}
	div.field-input{float:left; width:60%;}
</style>

<script type="text/javascript">
	$(document).ready(function() {
		setMessage("", "red");
		
		$('#fd-ok-btn').click(function(){addGateway();});
		$('#fd-close-btn').click(function(){closeDialog();});
	});

	function validateIP(value) {
		var ipPattern = /^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/;
		var hostNamePattern = /^([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]{0,61}[a-zA-Z0-9])(\.([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]{0,61}[a-zA-Z0-9]))*$/;
		
		var ipArray = value.match(ipPattern);
		if (value == "0.0.0.0") {
			return false;
		}
		else if (value == "255.255.255.255") {
			return false;
		}
		if (ipArray == null) {
			var hostName = value.match(hostNamePattern);
			
			if(hostName == null)
				return false;
			else
				return true;
		}
		else {
			for (var i = 1; i < 5; i++) {					
				if (ipArray[i] > 255) {
					return false;
				}
			}
		}
		return true;
	}

	function validateMAC(value) {
		var macPattern = /^(\w{2})\:(\w{2})\:(\w{2})\:(\w{2})\:(\w{2})\:(\w{2})$/;
		var macArray = value.match(macPattern);
		if (value == "0:0:0:0:0:0") {
			return false;
		}
		
		if(macArray == null) {
			return false;
		}

		return true;
	}

	function addGateway() {
		setMessage("Adding Gateway...", "black");
		
		var macaddr = $("#macAddress").val();
		var ipaddr = "";
		if("${emcMode}" ==  "true"){
			ipaddr = $("#macAddress").val();
		}else{
			ipaddr = $('#ipAddress').val();
			if(validateIP(ipaddr) != true)
			{
				setMessage("Please enter a valid IP address or host name", "red");
				$('#ipAddress').val("");
				return false;
			}
		}
		
		if(validateMAC(macaddr) != true)
		{
			setMessage("Please enter a valid MAC address", "red");
			$('#macAddress').val("");
			return false;
		}

		// Make an AJAX call to the service
		$.ajax({
			async: false,
			data: "",
			type: "POST",
			url: "${addGatewayUrl}"+macaddr+"/ipaddr/"+ipaddr+"/floorid/"+"${floorId}",
			success: function(data){
				if(data.status == "0") {
					setMessage("Successfully added the gateway", "green");
					$('#macAddress').val("");
					if("${emcMode}" !=  "true"){
						$('#ipAddress').val("");
					}
					
					$.ajax({
						async: true,
						data: "",
						type: "POST",
						url: "${sendGatewayInfoUrl}"+ipaddr	});
					}
				else if(data.status == "2") {
					setMessage("Gateway with the same MAC address already exists", "red");
					$('#macAddress').val("");
				}
				else if(data.status == "3") {
					if("${emcMode}" !=  "true"){
						setMessage("Gateway with the same IP address already exists", "red");
						$('#ipAddress').val("");
					}
				}
				else {
					setMessage("Failed to add the gateway", "red");
					$('#macAddress').val("");
					if("${emcMode}" !=  "true"){
						$('#ipAddress').val("");
					}
				}
			},
			error: function(){
				setMessage("Failure to add the gateway", "red");
			},
			dataType:"json"
		});
		
	}	
	
	function closeDialog() {
		$("#addGatewayDialog").dialog("close");
	}
	
	function setMessage(msg, color){
		$("#message-div").css("color", color);
		$("#message-div").html(msg);
	}
</script>
</head>

<body>
		
<div class="topmostContainer">
<div class="outermostdiv">

	<div class="innerdiv">
		<form id="gateway-details" onsubmit="javascript: return false;" action="">
			<table width=100% height=100% style="padding:0 10px;">	
				<tr height=24px>
					<td colspan="2" valign="middle" height=24px>
						<div id="message-div" class="aggf-message-text"></div>
					</td>
				</tr>
				<tr height=30px>
					<td colspan="2">
						<div class="field-label">MAC address:</div> 
						<div class="field-input"><input id="macAddress" type="text"/></div>
					</td>
				</tr>
				<c:if test="${emcMode != 'true'}">
				<tr height=30px>
					<td colspan="2">
						<div class="field-label">IP address/Host name:</div> 
						<div class="field-input"><input id="ipAddress" type="text"/></div>
					</td>
				</tr>
				</c:if>
				<tr height=10px>
				</tr>
				<tr>
					<td valign="top" align="center">
						<button id="fd-ok-btn" type="submit">Add</button>
					</td>
					<td valign="top" align="left">
						<button id="fd-close-btn">Close</button>
					</td>
				</tr>
			</table>
		</form>
	</div>
</div>
</div>

</body>
</html>