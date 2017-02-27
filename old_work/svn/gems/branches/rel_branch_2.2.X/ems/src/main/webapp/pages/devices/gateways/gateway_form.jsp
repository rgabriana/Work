<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>


<!--  utils functions -->
<script type="text/javascript">
	function getLastCommunicationsString(strOccValue)
    {
        if(strOccValue <= 0)
            return "0 sec ago";

        var date = new Date(0, 0, 0, 0, 0, strOccValue, 0);
        if(date.getDay() > 0)
        {
            if(date.getHours() > 0)
                return date.getDay().toString() + " days, " + date.getHours().toString() + " hrs ago";
            else
                return date.getDay().toString() + " days ago";
        }
        else if(date.getHours() > 0)
        {
            if(date.getMinutes() > 0)
                return date.getHours().toString() + " hrs, " + date.getMinutes().toString() + " min ago";
            else
                return date.getHours().toString() + " hrs ago";
        }
        else if(date.getMinutes() > 0)
        {
            if(date.getSeconds() > 0)
                return date.getMinutes().toString() + " min, " + date.getSeconds().toString() + " sec ago";
            else
                return date.getMinutes().toString() + " min ago";
        }
        else
            return date.getSeconds().toString() + " sec ago";
    }
	
	function getDateDifference(connectivityDate)
    {
		connectivityDate = new Date(connectivityDate);
        var currentdate = new Date();
        var secondsPerDay = 60 * 60 * 24;
        var diff = Math.ceil((currentdate.getTime() - connectivityDate.getTime())/1000);
        return diff;
    }
	
    function getWirelessEncryptKey(key)
    {
    	if (key == "enLightedWorkNow") {
    		return "default is in use";
    	}
    	return key;
    	
    }
    
    function getMACAddress(macAddress)
	{
        
		var strMacAddress = "";
        
        if(macAddress.indexOf(":") <= 0)
        {
            while(macAddress.length > 0)
            {
                if(macAddress.length >= 3)
                {
                    strMacAddress += macAddress.substr(0,2) + ":";
                    macAddress = macAddress.substr(2);
                }
                else
                {
                    strMacAddress += macAddress.substr(0,2);
                    macAddress = macAddress.substr(2);
                }
            }
        }
        else
        {
        	var macSplitArray;
        	macSplitArray = macAddress.split(':');
        	for (var i = 0; i < macSplitArray.length; i++) {
        	    if(macSplitArray[i].length == 1){
        	    	macSplitArray[i] = "0"+macSplitArray[i];
        	    }
        	    
        	    if(i == 0){
        			strMacAddress = macSplitArray[i];
        		}else{
        			strMacAddress = strMacAddress + ":" + macSplitArray[i];
        		}
        	    
        	}
        	
        }
       return strMacAddress;
    }
    
</script>
	
<script type="text/javascript">
showServerMessage(false);

	//Fetch Model Attributes in JS Variables
	var xaxis = '<c:out value="${gateway.xaxis}"/>';
	var yaxis = '<c:out value="${gateway.yaxis}"/>';
	var commissioned = '<c:out value="${gateway.commissioned}"/>';
	var macAddress = '<c:out value="${gateway.macAddress}"/>';
	var currUptime = '<c:out value="${gateway.currUptime}"/>';
	var message = '<c:out value="${message}"/>';
	var wirelessEncryptKey = '<c:out value="${gateway.wirelessEncryptKey}"/>';

	//load form values
	$("#gw_info_floorPlanPosition").val(xaxis+", "+yaxis);
	$("#gw_info_macAddress").val(getMACAddress(macAddress));
	$("#gw_info_currUptime").val(getLastCommunicationsString(currUptime).replace("ago", ""));
	$("#gw_info_wirelessEncryptKey").val(getWirelessEncryptKey(wirelessEncryptKey));
// 	$("#lastConnectivityAt").val(getLastCommunicationsString(lastCommunicationDiffInMillis));
	
	
	//Disable field based on status of commissioned 
	if(commissioned){
		$("#gw_info_wirelessEncryptType").attr("disabled","disabled");
		$("#gw_info_channel").attr("disabled","disabled");
		$("#gw_info_wirelessEncryptKey").attr("disabled","disabled");
		$("#gw_info_wirelessNetworkId").attr("disabled","disabled");
	}
	
	//Toggle password field 
	$('#gw_info_showClearText').click(function() {
		
		if($.browser.msie && parseInt($.browser.version) == 8){
			if ($('#gw_info_showClearText').is(':checked')) {
				  var newObject = document.createElement('input');
				  var oldObject = document.getElementById('gw_info_wirelessEncryptKey');
				  newObject.type = 'text';
				  if(oldObject.size) newObject.size = oldObject.size;
				  if(oldObject.value) newObject.value = oldObject.value;
				  if(oldObject.name) newObject.name = oldObject.name;
				  if(oldObject.id) newObject.id = oldObject.id;
				  if(oldObject.disabled) newObject.disabled = oldObject.disabled;
				  if(oldObject.readOnly) newObject.readOnly = oldObject.readOnly;
				  if(oldObject.className) newObject.className = oldObject.className;
				  oldObject.parentNode.replaceChild(newObject,oldObject);
			} else {
				  var newObject = document.createElement('input');
				  var oldObject = document.getElementById('gw_info_wirelessEncryptKey');
				  newObject.type = 'password';
				  if(oldObject.size) newObject.size = oldObject.size;
				  if(oldObject.value) newObject.value = oldObject.value;
				  if(oldObject.name) newObject.name = oldObject.name;
				  if(oldObject.id) newObject.id = oldObject.id;
				  if(oldObject.disabled) newObject.disabled = oldObject.disabled;
				  if(oldObject.readOnly) newObject.readOnly = oldObject.readOnly;
				  if(oldObject.className) newObject.className = oldObject.className;
				  oldObject.parentNode.replaceChild(newObject,oldObject);
			}
			
		}
		else{
			document.getElementById('gw_info_wirelessEncryptKey').type=this.checked?"text":"password";
		}
				
	});

	
// 	showServerMessage((message!=""));
	function showServerMessage(show){
		$("#gateway_message").css("display", (show?"block":"none"));
	}
	
	
	//Mark un-editable field as readonly
	$('#gateway-form input.readonly').attr('readonly', 'readonly');
	$('#gateway-form input.readonly').focus(function() {
		 $(this).blur();
	});
	
	
</script>

<style>
		#gateway-dialog-form { font-size: 12px; padding:10px 30px 0; }
		#gateway-dialog-form .disable { border: none; }
		#gateway-dialog-form label, #gateway-dialog-form input, #gateway-dialog-form select { display:block; }
		#gateway-dialog-form select {height: 24px;}
		#gateway-dialog-form select, #gateway-dialog-form input.text { margin-bottom:7px; width:100%; padding: 2px;}
		#gateway-dialog-form fieldset { padding:0px; border:0; margin-top:2px; background:#fff;}
		#gateway-dialog-form fieldset.column { padding:0px 10px 0px 0px; border:0; margin-top:2px; float:left; width:32%; }
		#gateway-dialog-form h1 { font-size: 14px; margin: .6em 0; }
		#gateway-dialog-form .ui-dialog #gateway-dialog-form .ui-state-error { padding: .3em; }
		#gateway-dialog-form .validateTips { border: 1px solid transparent; padding:2px; }
</style>

<!-- Override JQuery Dialog modal background css  -->
<style>
	.ui-widget-overlay {
		background: none repeat scroll 50% 50% #000000;
        opacity: 0.9;
       }
</style>

<div id="gateway-dialog-form" >
	<spring:url value="/devices/gateways/updateGateway.ems" var="actionURL" scope="request"/>
	<form:form id="gateway-form" commandName="gateway" method="post" action="${actionURL}">

<div id="gateway_message" style="font-size: 14px; font-weight: bold; padding-bottom: 5px;"></div>
<!--<div id="gateway_message" class="server_message" style="background-color: #EEEEEE; border: thin solid #AAAAAA; left: 494px; margin: auto; position: absolute; top: 200px; padding:2px;">
		<div class="button"><span style="float:right; cursor: pointer;" onclick="showServerMessage(false);"><b>x&nbsp;</b></span><br style="clear:both;"/></div>
		<div class="text" style="padding: 10px 20px 20px;">${message}</div>
	</div> -->

	<form:hidden id="gw_info_channelhidden" name="channel" path="channel"/>
	<form:hidden id="gw_info_wirelessEncryptTypehidden" name="wirelessEncryptType" path="wirelessEncryptType"/>
	<form:hidden id="gw_info_wirelessNetworkIdhidden" name="wirelessNetworkId" path="wirelessNetworkId"/>
	<form:hidden id="gw_info_wirelessEncryptKeyhidden" name="wirelessEncryptKey" path="wirelessEncryptKey"/>
	<form:hidden name="wirelessRadiorate" path="wirelessRadiorate"/>
	<form:hidden name="ethSecKey" path="ethSecKey"/>
	<form:hidden name="ethSecEncryptType" path="ethSecEncryptType"/>
	
	<fieldset>
		<label for="gw_info_gatewayName"><spring:message code="gatewayForm.label.name"/></label>
		<form:input class="text" id="gw_info_gatewayName" name="gatewayName" path="gatewayName"/>
		
		<label for="gw_info_location"><spring:message code="gatewayForm.label.location"/></label>
		<form:input class="text readonly" id="gw_info_location" name="location" path="location"/>
	</fieldset>
		
	<fieldset class="column">
		<label for="gw_info_id"><spring:message code="gatewayForm.label.id"/></label>
		<form:input class="text readonly" id="gw_info_id" name="id" size="40" path="id"/>
	</fieldset>
	
	<fieldset class="column">
		<label for="gw_info_floorPlanPosition"><spring:message code="gatewayForm.label.floorPlanPosition"/></label>
		<input class="text readonly" id="gw_info_floorPlanPosition" name="floorPlanPosition" size="40"/>
	</fieldset>
	<br clear="both"/><hr style="margin: 10px 0; "/>
	<fieldset class="column">
		<label for="gw_info_lastConnectivityAt"><spring:message code="gatewayForm.label.lastCommunication"/></label>
		<input class="text readonly" id="gw_info_lastConnectivityAt" name="lastConnectivityAt1" size="40" value='<ems:breakDateDiffInString  dateValue="${gateway.lastConnectivityAt}" datePattern="yyyy-MM-dd HH:mm:ss"/>'/>

		<label for="gw_info_app2Version"><spring:message code="gatewayForm.label.applicationVersion"/></label>
		<form:input class="text readonly" id="gw_info_app2Version" name="app2Version" size="40" path="app2Version"/>

		<label for="gw_info_boundFixr"><spring:message code="gatewayForm.label.boundFixtures"/></label>
		<form:input type="text" name="boundFixr" id="gw_info_boundFixr" value="" class="text readonly" path="noOfActiveSensors"/>

		<label for="gw_info_channel"><spring:message code="gatewayForm.label.radioChannelID"/></label>
		<form:select  id="gw_info_channel" name="channel" path="channel" class="text">
			<form:option value="0">0</form:option>
			<form:option value="1">1</form:option>
			<form:option value="2">2</form:option>
			<form:option value="3">3</form:option>
			<form:option value="4">4</form:option>
			<form:option value="5">5</form:option>
			<form:option value="6">6</form:option>
			<form:option value="7">7</form:option>
			<form:option value="8">8</form:option>
			<form:option value="9">9</form:option>
			<form:option value="10">10</form:option>
			<form:option value="11">11</form:option>
			<form:option value="12">12</form:option>
			<form:option value="13">13</form:option>
			<form:option value="14">14</form:option>
			<form:option value="15">15</form:option>
	    </form:select>
	    
		<label for="gw_info_wirelessEncryptType"><spring:message code="gatewayForm.label.radioEncryptType"/></label>
		<form:select name="wirelessEncryptType" path="wirelessEncryptType" id="gw_info_wirelessEncryptType">
			<form:option value="0">none</form:option>
			<form:option value="1">aes56</form:option>
			<form:option value="2">aes128</form:option>
		</form:select>
	</fieldset>
	
	<fieldset class="column">
		<label for="gw_info_macAddress"><spring:message code="gatewayForm.label.MACaddress"/></label>
		<form:input class="text readonly" id="gw_info_macAddress" name="macAddress" size="40" path="macAddress"/>

		<label for="gw_info_firmware"><spring:message code="gatewayForm.label.firmwareVersion"/></label>
		<form:input type="text" name="firmware" id="gw_info_firmware" value="" class="text readonly" path="app1Version"/>

		<label for="gw_info_currUptime"><spring:message code="gatewayForm.label.uptime"/></label>
		<input class="text readonly" id="gw_info_currUptime" name="currUptime1" size="40"/>

		<label for="gw_info_wirelessNetworkId"><spring:message code="gatewayForm.label.radioNetworkID"/></label>
		<form:input class="text" id="gw_info_wirelessNetworkId" name="wirelessNetworkId" size="40" path="hexWirelessNetworkId"/>

		<label for="gw_info_wirelessEncryptKey"><spring:message code="gatewayForm.label.radioSecurityString"/></label>
		<form:input type="password" class="text" id="gw_info_wirelessEncryptKey" name="wirelessEncryptKey" size="40" path="wirelessEncryptKey"/>
	</fieldset>
	
	<fieldset class="column">
		<label for="gw_info_ipAddress"><spring:message code="gatewayForm.label.ipAddress"/></label>
		<form:input class="text readonly" id="gw_info_ipAddress" name="ipAddress" size="40" path="ipAddress"/>
		
		<label for="gw_info_bootLoaderVersion"><spring:message code="gatewayForm.label.bootLoaderVersion"/></label>
		<form:input class="text readonly" id="gw_info_bootLoaderVersion" name="bootLoaderVersion" size="40" path="bootLoaderVersion"/>
		
		<input type="checkbox" name="showClearText" id="gw_info_showClearText" style="margin-top: 107px; display: inline;" />&nbsp;<spring:message code="gatewayForm.label.showClearText"/>
	</fieldset>
<%-- 	<input  id="submit" style="margin:0px; clear:both;" type="submit" value="<spring:message code='gatewayForm.label.updateButton'/>"></input> --%>
	</form:form>
</div>