<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>
<spring:url value="/modules/PlotChartModule.swf" var="plotchartmodule"></spring:url>
<spring:url value="/devices/gateways/manage.ems" var="gatewayListUrl" />
<spring:url value="/services/org/gateway/rma/" var="gatewayrmaUrl" scope="request" />

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
	
	.ui-dialog-title{font-size: 1.4em; font-weight: bolder;}
	
/* 	.ui-dialog-buttonpane {border: 1px solid #DDDDDD !important; margin-top: 0 !important;} */
</style>

<style>
 	#gatewayCommissioningDialog {border-top: 1px solid black !important;} 
	div#commissionDialogBox .floatright{background-color: #EDEDED;}
	div#commissionDialogBox div#gwDetails{background-color:white;}
	div#commissionDialogBox .form-content_wrapper{padding: 0.5em;}
	div#commissionDialogBox fieldset.form-column-left{background-color:white; float:left; width: 30%; border:none;}
	div#commissionDialogBox fieldset.form-column-right{background-color:white; float:left; width: 65%; border: 1px solid black; border-radius:10px; border-color:#AAAAAA; padding: 1px}
	div#commissionDialogBox .form-column-right-wrapper{padding: 5px 0 2px 10px;}
	div#commissionDialogBox .form-column-left-wrapper{padding: 10px 0 0 10px;}
	div#commissionDialogBox .fieldWrapper{padding-bottom:3px;}
	div#commissionDialogBox .fieldPadding{height:3px;}
	div#commissionDialogBox .fieldlabel{float:left; height:20px; width: 30%; font-weight: bold;}
	div#commissionDialogBox .fieldInput{float:left; height:20px; width: 45%;}
	div#commissionDialogBox .fieldInputCombo{float:left; height:24px; width: 45.5%;}
	div#commissionDialogBox fieldset.form-column-left .fieldlabel{width: 30%;}
	div#commissionDialogBox fieldset.form-column-left .fieldInput{width: 60%;}
	div#commissionDialogBox div.form-footer-buttons{background-color: #EDEDED; padding:5px 5px 2px 5px;}
	div#commissionDialogBox .text {height:100%; width:100%;}
	div#commissionDialogBox .button {padding: 0 5px;}
	div#gc_message_Div {float: left; font-weight: bold; padding: 5px 5px 0 5px;}
</style>
<SCRIPT language="JavaScript">
	var isCommissioningInProcess = false;
	var CURRENT_GATEWAY_OBJECT = {};
// 	setGatewayLocation();
	
	$(document).ready(function() {
// 			setGatewayLocation();
			toggleDisabled(true);
			var flash_floor = function(nodetype, nodeid) {
				var FP_data = "";
				//var plotchartmodule_url = "${plotchartmodule}?ts=" + new Date().getTime();
				
				var buildNumber = "";
				
				var versionString = "<ems:showAppVersion />";
				
				var indexNumber = versionString.lastIndexOf('.', (versionString.length)-1);
				
				if(indexNumber != -1 ){
					buildNumber = versionString.slice(indexNumber+1);
				}else{
					buildNumber = Math.floor(Math.random()*10000001);// For Development Version
				}
				
				var plotchartmodule_url = "${plotchartmodule}"+"?buildNumber="+buildNumber;
				
				if ($.browser.msie) {
					FP_data = "<object id='c_floorplan' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='100%'>";
					FP_data +=  "<param name='src' value='" + plotchartmodule_url + "'/>";
					FP_data +=  "<param name='padding' value='0px'/>";
					FP_data +=  "<param name='wmode' value='opaque'/>";
					FP_data +=  "<param name='allowFullScreen' value='true'/>";
					FP_data +=  "<param name='flashVars' value='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION&modeid=GATEWAY_COMMISSION'/>";
					FP_data +=  "<embed id='c_floorplan' name='c_floorplan' src='" + plotchartmodule_url + "' pluginspage='http://www.adobe.com/go/getflashplayer'";
					FP_data +=  " height='100%'";
					FP_data +=  " width='100%'";
					FP_data +=  " padding='0px'";
					FP_data +=  " wmode='opaque'";
					FP_data +=  " allowFullScreen='true'";
					FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION&modeid=GATEWAY_COMMISSION'/>";
					FP_data +=  "</object>";
				} else {
					FP_data = "<embed id='c_floorplan' name='c_floorplan' src='" + plotchartmodule_url + "' pluginspage='http://www.adobe.com/go/getflashplayer'";
					FP_data +=  " height='100%'";
					FP_data +=  " width='100%'";
					FP_data +=  " wmode='opaque'";
					FP_data +=  " padding='0px'";
					FP_data +=  " allowFullScreen='true'";
					FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=COMMISSION&modeid=GATEWAY_COMMISSION'/>";
				}
				
				var tabFP =document.getElementById("div_c_fp");
				tabFP.innerHTML = FP_data; 

				// quick fix for the duplicate flash object
				$('div.alt').remove();
			}
			flash_floor('floor', '${floorId}');

			//Toggle password field 
			$('#showClearText')
					.click(
							function() {
								
										if($.browser.msie && parseInt($.browser.version) == 8){
											if ($('#showClearText').is(':checked')) {
												  var newObject = document.createElement('input');
												  var oldObject = document.getElementById('wirelessEncryptKey');
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
												  var oldObject = document.getElementById('wirelessEncryptKey');
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
											document.getElementById('wirelessEncryptKey').type = this.checked ? "text": "password";
										}
										
										
										
							});
	});
	
	function getCommissionPlanObj(objectName) {			
		if ($.browser.mozilla) {
			return document[objectName] 
		}
		return document.getElementById(objectName);
	}	
	// select gateway
	function selectGateway() {
		
		var count = $("#uncommissionedGateways :selected").length;
		if(count==0)
		{
			return;
		}
		toggleDisabled(false);
		
		if (isCommissioningInProcess)
			return false
		
		var floorId = ${floorId};
		$.ajax({
			type : "GET",
			cache : false,
			url : '<spring:url value="/services/org/gateway/details/"/>'
					+ $("#uncommissionedGateways").val()+"?ts="+new Date().getTime(),
			dataType : "xml",
			success : function(msg) {
				$(msg).find('gateway')
						.each(
								function() {									
									CURRENT_GATEWAY_OBJECT.id = $("#gid").val($(this).find('id').text());
									CURRENT_GATEWAY_OBJECT.name = $("#gatewayName").val($(this).find('name').text());									
									$("#channel").val($(this).find('channel').text());
									var decimalNo = $(this).find('wirelessnetworkid').text();
									$("#wirelessNetworkId").val(convertDecimalToHex(decimalNo));
									//$("#wirelessEncryptType").val($(this).find('wirelessencrypttype').text());
									var wKey = $(this).find('wirelessencryptkey').text();
									if(wKey == "enLightedWorkNow") {
										wKey = "default is in use";
									}
									$("#wirelessEncryptKey").val(wKey);
									$("#macAddress").val($(this).find('macaddress').text());
									$("#ipAddress").val($(this).find('ipaddress').text());
									$("#wirelessRadioRate").val($(this).find('wirelessradiorate').text());
									$version = $(this).find('app1version').text();
									if ($version.match("^1.") != null) { 
										$("#wirelessEncryptType option[value=1]").attr('selected', 'selected');
									} else {
										$("#wirelessEncryptType option[value=2]").attr('selected', 'selected');
									}
								});
			}
		});
		return true;
	}
	
	function resetDevicePositionOnFloorplan() {
		try {
			getCommissionPlanObj("c_floorplan").resetDevicePosition();
		} catch(e){
			// Protect for javascript + flex communication failure
		}
	}
	
	function clearMessageTemplate()
	{		 
			 $("#errorMsgGateway").text("");
			 $("#gatewayName").removeClass("invalidField");
	}
	
	function verifyduplicategateway()
	{
		//Check the duplicate gateway name
		var gatewayName = $("#gatewayName").val();		
		var returnresult = false;	
		if(gatewayName=="" || gatewayName==" ")
		{		
			 clearMessageTemplate();
			 $("#errorMsgGateway").text("Above field is required.");
			 $("#gatewayName").addClass("invalidField");
			return false;
		}	 
		$.ajax({
			type: "GET",
			cache: false,
			url: '<spring:url value="/services/org/gateway/duplicatecheck/"/>'+ gatewayName,
			dataType: "text",
			async: false,
			success: function(msg) {			
				var count = (msg).indexOf(gatewayName);			
				if(count > 0) {
					returnresult = false;
				}
				else {
					returnresult = true;
				}
			},
			error: function (jqXHR, textStatus, errorThrown){			
				returnresult = false;
			}
		});	
		if(!returnresult){
			clearMessageTemplate();
			$("#errorMsgGateway").text('<spring:message code="error.duplicate.gateway"/>');
			$("#gatewayName").addClass("invalidField");
			return false;
		}	
		else {	
			clearMessageTemplate();
			commissionDevice();
		}
		
		
	}
	
	function checkGridSelectionEnable()
	{
		var ids = jQuery("#uncommissionedGateways").jqGrid('getGridParam', 'selarrrow');
		if(ids!=null && (ids.length>1 || ids.length==0))
		{
			// Reset Current gateway object if Multiple selection or none of the gateways are selected
			CURRENT_GATEWAY_OBJECT={};
			return true;
		}else
		{
			return false;
		}
	}
	
	function validateGatewaySelection(){		
		if(CURRENT_GATEWAY_OBJECT.id == undefined) {
			alert("Select a gateway from the list");
			return false;
		}
		
		var isEnable = checkGridSelectionEnable();
		if(isEnable)
		{
			alert("Select a single gateway from the list");
			return false;
		}
		return true;
	}
	
	function rmaGateway(fromGatewayId, fromGatewayName){			
		if(!validateGatewaySelection()){			
			return false;
		}
				
		var bConfirm = confirm("Selected gateway : " + fromGatewayName + "  Replace with : " + $("#gatewayName").val()
				+ "\n				Do you want to continue?");
		
		if(bConfirm){			
			var bSuccess = false;
						
			$("#gc_message_Div").html("Performing Gateway RMA...");
			
			$.ajax({
				url: "${gatewayrmaUrl}" + fromGatewayId + "/" + $("#gid").val(),
				type: "POST",
				async: false,
				dataType:"json",
				contentType: "application/json; charset=utf-8",
				success: function(data){					
					if(data.status == "1"){
						resetDevicePositionOnFloorplan();
						$("#gc_message_Div").html(msg.msg);						
					   }
					else {
						try {
							$("#uncommissionedGateways option:selected").remove();
						   	getCommissionPlanObj("c_floorplan").plotChartRefresh();
						   	bSuccess = true;
						   	//postCommissionDevice();
						}
						catch (e) {
							// TODO: Fixme
						}
						   $("#gc_message_Div").html("Success");						
				    }
				},					
				error: function() {
					resetDevicePositionOnFloorplan();					
				},
				complete: function() {
					//if(bSuccess == false)
						reloadGatewayIFrame();					
				}
			});			
		}		
	}
	
	function commissionDevice() {
		
		var count = $("#uncommissionedGateways :selected").length;
		if(count==0)
		{
			alert("Select a gateway from the list");
			resetDevicePositionOnFloorplan();
			return false;
		}
		if ($("#channel").val() == 4 && $("#wirelessNetworkId").val() == 6854) {
			    resetDevicePositionOnFloorplan();
			   alert("<spring:message code='error.valid.gateway.default.params'/>");
			   return false;
		}
		if (checkNetworkIdFormat($("#wirelessNetworkId").val()) == false) {
			resetDevicePositionOnFloorplan();
			alert("<spring:message code='error.invalid.gateway.networkid'/>")
			return false
		}
		if ($("#wirelessEncryptKey").val() != "default is in use") {
			if ($("#wirelessEncryptKey").val().length != 16) {
				resetDevicePositionOnFloorplan();
				alert("<spring:message code='error.invalid.gateway.securitykey'/>")
				return false;
			}
		}
		if (isCommissioningInProcess) {
			resetDevicePositionOnFloorplan();
			return false;
		}
		isCommissioningInProcess = true;
		var bSuccess = false;
		var floorId = ${floorId};
	   $("#gc_message_Div").html("Please wait...");
		$.ajax({
			   type: "POST",
			   url: '<spring:url value="/services/org/gateway/commission"/>'+"?ts="+new Date().getTime(),
			   contentType: "application/json",
			   data: '{"id":"' + $("#gid").val() + 
				   '", "name":"' + $("#gatewayName").val()  + 
				   '","macaddress":"' + $("#macAddress").val() + 
				   '","ipaddress":"' + $("#ipAddress").val() + 
				   '","wirelessradiorate":"' + $("#wirelessRadioRate").val() + 
				   '","channel":"' + $("#channel").val() + 
				   '","wirelessnetworkid":"' + convertHexToDecimal($("#wirelessNetworkId").val()) + 
				   '","wirelessencrypttype":"' + $("#wirelessEncryptType").val() + 
				   '","wirelessencryptkey":"' + getWirelessEncryptKey($("#wirelessEncryptKey").val()) + 
				   '","floor":' +
				   '{"id":"' + floorId + '"}' +
				   '}',
			   dataType: "json",
			   success: function(msg) {
				   if(msg.status == "1") {
						resetDevicePositionOnFloorplan();
					   $("#gc_message_Div").html(msg.msg);
				   } else {
					   try {
						   $("#uncommissionedGateways option:selected").remove();
					   		getCommissionPlanObj("c_floorplan").addGateway('floor', floorId, $("#gid").val());
					   		bSuccess = true;
					   		postCommissionDevice();
					   }catch (e) {
						   // TODO: Fixme
					   }
					   $("#gc_message_Div").html("Success, please wait...");
				   }
			   },
			   error: function(msg) {
					resetDevicePositionOnFloorplan();
			   },
			   complete: function() {
				   if (bSuccess == false) {
						isCommissioningInProcess = false;
						reloadGatewayIFrame();
				   }
			   }
		});	
		return true;
	}

	
	function postCommissionDevice() {
		var floorId = ${floorId};
		$.ajax({
			   type: "POST",
			   url: '<spring:url value="/services/org/gateway/postcommission"/>'+"?ts="+new Date().getTime(),
			   contentType: "application/json",
			   data: '{"id":"' + $("#gid").val() + 
				   '", "name":"' + $("#gatewayName").val()  + 
				   '","macaddress":"' + $("#macAddress").val() + 
				   '","ipaddress":"' + $("#ipAddress").val() + 
				   '","wirelessradiorate":"' + $("#wirelessRadioRate").val() + 
				   '","channel":"' + $("#channel").val() + 
				   '","wirelessnetworkid":"' + convertHexToDecimal($("#wirelessNetworkId").val()) + 
				   '","wirelessencrypttype":"' + $("#wirelessEncryptType").val() + 
				   '","wirelessencryptkey":"' + $("#wirelessEncryptKey").val() + 
				   '","floor":' +
				   '{"id":"' + floorId + '"}' +
				   '}',
			   dataType: "json",
			   success: function(msg) {
				   toggleDisabled(true);
				   if(msg.status == "1") {
					   $("#gc_message_Div").html(msg.msg);
				   } else {
					   $("#gc_message_Div").html("Success");
				   }
			   },
			   error: function(msg) {
			   },
			   complete: function() {
					isCommissioningInProcess = false;
					reloadGatewayIFrame();
			   }
		});	
		return true;
	}

	function doneBtnHandler(){
		$("#gatewayCommissioningDialog").dialog("close");
		return false;   
	}
	
	function setGatewayLocation(){
		var gwlocation = "";
		<c:forEach items="${gateways}" var="gateway">
			gwlocation = "${gateway.location}";
		</c:forEach>
		var titleEL = $("#gw-comm-dialog-title-id");
		titleEL.html(titleEL.html()+"<span style='font-size:0.8em; padding-left:15px;'>Location: "+gwlocation+"</span>");
// 		alert(titleEL.html());
	}

	function convertDecimalToHex(decimalNo){
		decimalNo = 1*decimalNo;
		var hexNo = decimalNo.toString(16);
		return hexNo.toUpperCase();
	}

	function convertHexToDecimal(HexNo){
		var decimalNo = parseInt(HexNo, 16);
		return decimalNo;
	}
	
	function getWirelessEncryptKey(wKey) {
		if(wKey == "default is in use") {
			return "enLightedWorkNow";
		}
		return wKey;
	}
	
	function reloadGatewayIFrame(){
		var ifr = document.getElementById("installFrame").contentWindow.document.getElementById("gatewaysFrame");
		ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = "${gatewayListUrl}"+"?ts="+new Date().getTime();
	}

	function checkNetworkIdFormat(field) {
		var networkIdstr = field;
		
        var lowerlimit = convertHexToDecimal("2710");
        var upperlimit = convertHexToDecimal("7fff");
        var inetworkId = convertHexToDecimal(networkIdstr);
        if (inetworkId < lowerlimit || inetworkId > upperlimit) {
        	return false;
        }
        return true;
	}
	
	function toggleDisabled(bDisable) {
		if (bDisable) {
			$("#gwc-undo-btn").attr("disabled", true);
			$("#gwc-commission-btn").attr("disabled", true);
			$("#gatewayName").attr("disabled", true);
			$("#channel").attr("disabled", true);
			$("#wirelessNetworkId").attr("disabled", true);
			$("#wirelessEncryptKey").attr("disabled", true);
			$("#wirelessEncryptType").attr("disabled", true);
			$("#showClearText").attr("disabled", true);
		} else {
			$("#gwc-undo-btn").attr("disabled", false);
			$("#gwc-commission-btn").attr("disabled", false);
			$("#gatewayName").attr("disabled", false);
			$("#channel").attr("disabled", false);
			$("#wirelessNetworkId").attr("disabled", false);
			$("#wirelessEncryptKey").attr("disabled", false);
			$("#wirelessEncryptType").attr("disabled", false);
			$("#showClearText").attr("disabled", false);
		}
	}

</SCRIPT>


<style>
div#commissionDialogBox .floatleft {float: left; width: 25%; height: 100%; }
div#commissionDialogBox .floatright {float: right; width: 75%; height: 100%; overflow: auto;}

div#commissionDialogBox #north{border: 0; width: 100%; height: 30.5%; max-height:165px;}
div#commissionDialogBox #div_c_fp{border-width: 1px 0 0 0; border-style:solid; width: 100%; height: 69%;}

#gwDetails .disable { border: none;}
</style>

<div id="commissionDialogBox" style="height: 100%; width: 100%;">
	<div id="north">
		<div id="gwList" class="floatleft">
			<div style="border-right: 1px solid; height: 100%;">
				<form:select id="uncommissionedGateways"
					style="height: 100%; width: 100%; border:0 none;" path="gateways"
					items="${gateways}" itemValue="id" itemLabel="gatewayName"
					multiple="true" onChange="selectGateway();" />
			</div>
		</div>
		<div id="gwDetails" class="floatright">
			<div class="form-content_wrapper">
				<fieldset class="form-column-left">
				<div class="form-column-left-wrapper">
					<div class="fieldWrapper">
						<div class="fieldlabel"><label for="id"><spring:message code="gatewayForm.label.id" />:</label></div>
						<div class="fieldInput"><input class="text readonly" id="gid" name="id" size="40" disabled="true"/></div>
						<br style="clear:both;"/>
					</div>
					<div class="fieldPadding"></div>
					<div class="fieldWrapper">
						<div class="fieldlabel"><label for="gatewayName"><spring:message code="gatewayForm.label.name" />:</label></div>
						<div class="fieldInput"><input class="text" id="gatewayName" name="gatewayName" /></div><span id="errorMsgGateway" class="error"></span>
						<br style="clear:both;"/>
					</div>
					
					<input class="text" type="hidden" id="macAddress" name="macAddress" />
					<input class="text" type="hidden" id="wirelessRadioRate" name="wirelessRadioRate" />
					<input class="text" type="hidden" id="ipAddress" name="ipAddress" />
				</div>
				</fieldset>
				
				<fieldset class="form-column-right">
				<div class="form-column-right-wrapper">
					<div class="fieldWrapper">
						<div class="fieldlabel"><label for="radioCnnlId"><spring:message code="gatewayForm.label.radioChannelID" />:</label></div>
						<div class="fieldInputCombo">
							<select id="channel" name="channel" class="text">
								<option value="0">0</option>
								<option value="1">1</option>
								<option value="2">2</option>
								<option value="3">3</option>
								<option value="4" selected="selected">4</option>
								<option value="5">5</option>
								<option value="6">6</option>
								<option value="7">7</option>
								<option value="8">8</option>
								<option value="9">9</option>
								<option value="10">10</option>
								<option value="11">11</option>
								<option value="12">12</option>
								<option value="13">13</option>
								<option value="14">14</option>
								<option value="15">15</option>
							</select>
						</div>
						<br style="clear:both;"/>
					</div>
					
					<div class="fieldWrapper">
						<div class="fieldlabel"><label for="wirelessNetworkId"><spring:message code="gatewayForm.label.radioNetworkID" />:</label></div>
						<div class="fieldInput"><input class="text" id="wirelessNetworkId" name="wirelessNetworkId" value="6854" size="10" /></div>
						<br style="clear:both;"/>
					</div>
					
					<div class="fieldPadding"></div>
					
					<div class="fieldWrapper">
						<div class="fieldlabel"><label for="wirelessEncryptType"><spring:message code="gatewayForm.label.radioEncryptType" />:</label></div>
						<div class="fieldInputCombo">
							<select name="wirelessEncryptType" id="wirelessEncryptType" class="text">
								<option value="0">none</option>
								<option value="1" selected="selected">aes56</option>
								<option value="2">aes128</option>
							</select>
						</div>
						<br style="clear:both;"/>
					</div>

					<div class="fieldWrapper">
						<div class="fieldlabel"><label for="wirelessEncryptKey"><spring:message code="gatewayForm.label.radioSecurityString" />:</label></div>
						<div class="fieldInput"><input type="password" class="text" id="wirelessEncryptKey" name="wirelessEncryptKey" value="enLightedWorkNow" size="40" class="text"/></div>
						<div style="float:left; margin:5px 0 0 5px; font-weight: bold;"><input type="checkbox" name="showClearText" id="showClearText" style="display: inline;" /> <spring:message code="gatewayForm.label.showClearText" />&nbsp; </div>
						<br style="clear:both;"/>
					</div>
					
				</div>
				</fieldset>
				
				<br style="clear:both;"/>
				</div>

			<div class="form-footer-buttons">
				<table width="100%" cellspacing=0>
					<tr>
						<td width="210px" valign="top">
							<input id="gwc-undo-btn" type="button" class="button" value="<spring:message code='gatewayForm.label.undoBtn'/>" />
							<input id="gwc-commission-btn" type="button" class="button" onclick="verifyduplicategateway();" value="<spring:message code='gatewayForm.label.commissionAndPlaceBtn'/>" /> 
						</td>
						<td width="auto" valign="top">
							<div id="gc_message_Div" style="float: left;"></div>
						</td>
						<td width="55px" valign="top">
							<button id="gwc-done-btn" class="button" onclick="javascript: return doneBtnHandler();">Done</button>
						</td>
					</tr>
				</table>
			</div>
				
		</div>
	</div>
	<div id="div_c_fp"></div>
</div>