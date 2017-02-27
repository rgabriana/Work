<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<spring:url value="/scripts/jquery/jquery.ptTimeSelect.js" var="ptTimeSelect"></spring:url>
<script type="text/javascript" src="${ptTimeSelect}"></script>
<spring:url value="/themes/standard/css/jquery/jquery.ptTimeSelect.css" var="ptTimeSelectCss"></spring:url>
<link rel="stylesheet" type="text/css" href="${ptTimeSelectCss}" />

<style type="text/css">
	input[type="text"]{width:300px; height:22px;}
	textarea{width:300px; height:75px;}
</style>

<script type="text/javascript">

var COLOR_SUCCESS = "green";
var COLOR_FAILURE = "red";
var COLOR_DEFAULT = "black";

//prevent is used to prevent the customized click action at form.
var prevent = false;
var initialTime;
var dhcpEnable;
$().ready(function() {
	
	dhcpEnable = '${dhcpEnable}';
	if(dhcpEnable=="")
		dhcpEnable= "true";
	if(dhcpEnable == "true")
	{
		var radio = document.getElementById("dhcpEnable");
		if(radio)
		radio.checked = "checked";
	}
	else
	{
		var radio = document.getElementById("dhcpDisable");
		if(radio)
		radio.checked = "checked";
	}
    
	var displayStatus = "${dhcpSettingStatus}";  
    if(displayStatus.length != 0)
    {
		$("#accordion").accordion({
			autoHeight: false,
			active: 1
		});

        if(displayStatus=="success")
    		displayLabelMessage("DHCP setting changed successfully.", COLOR_SUCCESS);
        else if(displayStatus=="failure")
    		displayLabelMessage("DHCP setting change failed.", COLOR_FAILURE);
	}
    else
    {
    	clearLabelMessage();
		$("#accordion").accordion({
			autoHeight: false,
			active: 3
		});
	}
	
	initialTime = $("#gemstime").val().toUpperCase();
	
	
	jQuery.validator.addMethod("checkFormat", function(value, element) {
			value = $.trim(value);
			if(value != null && value != "") {
		        var timeReg = /^\d{1,2}:\d\d (AM|PM)$/i;
		        if(timeReg.test(value) == false) {
		            return false;
		        }
		        var timearr = value.split(" ");
		        var timearr1 = timearr[0].split(":");
				if(timearr1[0] < 0 || timearr1[0] >  12){
					return false;
				}
				if(timearr1[1] < 0 || timearr1[1] >  59){
					return false;
				}
			}
			return true;
		}, '<spring:message code="error.invalid.time"/>');
	
	
	var requirederr = '<spring:message code="error.above.field.required"/>';
	$("#company-register").validate({
		rules: {
			name: "required",
			address: "required",
			email: {
				required: true,
				email: true
			},
			contact: {
				required: true
			},
			gemstime: {
				checkFormat: ""
			},
			newPassword: {
				required: true,
				minlength: 5
			},
			confirmPassword: {
				required: true,
				equalTo: "#newPassword"
			},
			price: {
				required: true,
				min: 0,
				digits: true
				
			}
		},
		messages: {
			name: requirederr,
			address: requirederr,
			email: {
				required: requirederr,
				email: '<spring:message code="error.valid.email.required"/>'
			},
			contact: {
				required: requirederr
			},
			newPassword: {
				required: requirederr,
				minlength: '<spring:message code="error.password.length"/>'
			},
			confirmPassword: {
				required: requirederr,
				equalTo: '<spring:message code="error.passwords.not.match"/>'
			},
			price: {
				required: requirederr,
				min: '<spring:message code="error.price.number.required"/>',
				digits: '<spring:message code="error.price.number.required"/>'
			}
		}
	});

    var catcher = function() {
      var changed = false;
      $('form').each(function() {
        if ($(this).data('initialForm') != $(this).serialize()) {
          changed = true;
          $(this).addClass('changed');
        } else {
          $(this).removeClass('changed');
        }
      });
      if (changed) {
        return 'There are unsaved changes.';
      }
    };

      $('form').each(function() {
        $(this).data('initialForm', $(this).serialize());
      }).submit(function(e) {
    	  prevent = false;
      var formEl = this;
      var changed = false;
      $('form').each(function() {
        if (this != formEl && $(this).data('initialForm') != $(this).serialize()) {
          changed = true;
          $(this).addClass('changed');
        } else {
          $(this).removeClass('changed');
        }
      });
      if (changed && !confirm('Another form on this page has been changed. Are you sure you want to continue with this submission?')) {
    	 prevent = true;
        e.preventDefault();
      } else {
        $(window).unbind('beforeunload', catcher);
      }
      });
      $(window).bind('beforeunload', catcher);



});

function timeChanged() {
	var newTime = $("#gemstime").val().toUpperCase();
	if (newTime != null && newTime != "") {
		if(newTime != initialTime) {
			$("#changeTime").val('true');
		}
		else {
			$("#changeTime").val('false');
		}
	}
	else {
		$("#changeTime").val('false');
	}
}

function toggleValidDomain(obj){
	if($(obj).val() == 'true'){
		$('#validDomain').removeAttr('disabled');
	}else{
		$('#validDomain').attr('disabled', 'disabled');
	}
}

function copyEmail(obj){
	if($('#notificationEmail').val() == ''){
		$('#notificationEmail').html($(obj).val());
	}
}

function displayLabelMessage(Message, Color) {
		$("#user_message").html(Message);
		$("#user_message").css("color", Color);
	
	}
	function clearLabelMessage(Message, Color) {
		displayLabelMessage("", COLOR_DEFAULT);
	}
</script>

<script>
$('#firstStep').show();
$('#secondStep').hide();
$('#thirdStep').hide();
$('#fourthStep').hide();
$('#fifthStep').hide();
</script>


<div class="topLevelContainer plaindiv">
	<c:if test="${mode == 'admin'}">
	<div id="accordion" class="fitwholepage" >
		<spring:url value="/scripts/jquery/jquery.blockUI.2.39.js" var="jquery_blockUI"></spring:url>
		<script type="text/javascript" src="${jquery_blockUI}"></script>
	
		<script type="text/javascript"> 	
			$().ready(function(){

				jQuery.validator.addMethod("validip", function(value, element) {
					var ipPattern = /^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/;
					var ipArray = value.match(ipPattern);
					if (value == "0.0.0.0") {
						return false;
					}
					else if (value == "255.255.255.255") {
						return false;
					}
					if (ipArray == null) {
						return false;
					}
					else {
						for (i = 1; i < 5; i++) {					
							if (ipArray[i] > 255) {
								return false;
							}
						}
					}
					return true;
					});
				
				$("#network-details").validate({
					rules: {
						ip: {
							required: true,
							validip: ""
						},
						mask: {
							required: true,
							validip: ""
						},
						gateway: {
							required: true,
							validip: ""
						}
					},
					messages: {
						ip: {
							required: '<spring:message code="error.above.field.required"/>',
							validip: '<spring:message code="error.invalid.ip"/>'
						},
						mask: {
							required: '<spring:message code="error.above.field.required"/>',
							validip: '<spring:message code="error.invalid.mask"/>'
						},
						gateway: {
							required: '<spring:message code="error.above.field.required"/>',
							validip: '<spring:message code="error.invalid.gateway"/>'
						}
					}
				});
				
				$("#network-details").submit(function() {
					
					var serverError = false;
					var pollServer = null;
					var ip = null;
					function formsubmit() {
		                $.ajax({
		                    type: "POST",
		                    cache: false,
		                    dataType:"html",
		                    url: '<spring:url value="/services/system/updatenetworkdetails"/>',
		                    data: $('#network-details').serialize(),
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
		                    	if(msg == "F") {
			                    	serverError = true;
			                    	if(serverError) {
			                    		clearInterval(pollServer);
			                    		$('.blockMsg').html('<span>Internal error while submitting your request. Click <a onclick="$.unblockUI();">here</a> to continue.</span></span>');
			                    		$("div").css('cursor', 'default');
			                    	}
			                    }
		                    },
		                    complete: function(){
		                    }
						});
					}
					
					
					function poll() {
						if(document.getElementById("typeStatic").checked) {
							ip = $('#ip').val();
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
				                    cache: false,
				                    dataType: "html",
				                    crossDomain: true,
				                    url: location.protocol + '//' + ip + '/ems',
				                    beforeSend: function(){
				                    },
				                    success: function(msg){
										clearInterval(pollServer);
				                    	$('.blockMsg').html('<a href="' + location.protocol + '//' + ip  + '/ems' + '" id="login" >Click here to re-login to the application.</a>');
				                    	$(window).unbind('beforeunload');
				                    	$("div").css('cursor', 'default');
				                    },
				                    complete: function(transport){
				                    	count++;
				                    },
				                    error: function() {
				                    }
								});
								 
								if(count > 4) {
									clearInterval(pollServer);
									$('.blockMsg').html('<span>Your request is submitted successfully. If input is not valid, you might not be able to re-login.  In that case you should contact admin. <a href="' + location.protocol + '//' + ip  + '/ems' + '" id="login" >Click here to re-login to the application.</a></span>');
									$(window).unbind('beforeunload');
									$("div").css('cursor', 'default');
								 }
									
							}, 10000);
						}
						else if(document.getElementById("typeDynamic").checked){
							setTimeout(function() {
								if(!serverError) {
									$('.blockMsg').html('<span>Your request is submitted successfully. Please contact admin for new login address.</span>');
									$(window).unbind('beforeunload');
									$("div").css('cursor', 'default');
								}
							}, 30000);
		
						} 
		
					}
					
					if(prevent) {
						prevent = false;
					}
					else {
						var isValid = $('#network-details').valid();
						if(isValid) {
							$.when(formsubmit(),poll());
						}
					}
		
					return false;
				});
				
				$("#dhcp-submit").click(function() {
					
					clearLabelMessage();
					
					var radioDhcpEnable = document.getElementById("dhcpEnable");
					var radioDhcpDisable = document.getElementById("dhcpDisable");

					var gatewaysPresent = '${gatewaysPresent}';
					if(gatewaysPresent=="")
					{
						gatewaysPresent = "false";
					}
					
					if(radioDhcpEnable.checked == true)
					{
						if(dhcpEnable == "true")
						{
							displayLabelMessage("DHCP server is already on.", COLOR_DEFAULT);
							return false;
						}
						else if(gatewaysPresent == "true")
						{
							alert("Please delete the added gateways before proceeding.");
							radioDhcpEnable.checked = false;
							radioDhcpDisable.checked = true;
							return false;
						}
					}
					else
					{
						if(dhcpEnable == "false")
						{
							displayLabelMessage("DHCP server is already off.", COLOR_DEFAULT);
							return false;
						}
						else if(gatewaysPresent == "true")
						{
							var ret = confirm("There are gateways already discovered/commissioned in the network. Proceed anyway?");
							
							if (ret == false)
							{
								radioDhcpEnable.checked = true;
								radioDhcpDisable.checked = false;
								return false;
							}
						}
					}
					
					displayLabelMessage("Processing...", COLOR_DEFAULT);
					$("#dhcp-details").submit();
				});
			});
			
			function disableForm() {
				$(".accordion-outerContainer").find('label.error').remove();
				$(".accordion-outerContainer").find('input.error').removeClass('error');
				$('#ip').attr('disabled', 'disabled');
				$('#mask').attr('disabled', 'disabled');
				$('#gateway').attr('disabled', 'disabled');
			}
			
			function enableForm() {
				$('#ip').removeAttr('disabled');
				$('#mask').removeAttr('disabled');
				$('#gateway').removeAttr('disabled');
				
			
			}

	 		
		</script>
	
		<h2><a  href="#"><spring:message code="dhcpSetup.heading.name"/></a></h2>
		<div class="accordion-outerContainer">
			<div class="i1"></div>
			<div class="innerContainer">
				<div class="formContainer">
				<div id="user_message" style="font-size: 12px; font-weight: bold;" ></div>
				<spring:url value="/admin/dhcp_setup/save.ems"
					var="dhcpSetting"  />
					<form id="dhcp-details" action="${dhcpSetting}">
						<div class="field">
							<div class="formPrompt"><span><spring:message code="system.dhcp.label"/></span></div>
							<div class="formValue">
								<input style="display: inline;" type="radio" id="dhcpEnable" name="dhcp" value="true"/>
								<span style="display: inline;"><spring:message code="system.dhcp.enable"/></span> &nbsp;&nbsp;
								<input style="display: inline;" type="radio" id="dhcpDisable" name="dhcp" value="false"/>
								<span style="display: inline;" ><spring:message code="system.dhcp.disable"/></span> &nbsp;&nbsp;&nbsp;&nbsp;
							</div>
						</div>
						<div class="field">
							<div class="formPrompt"><span></span></div>
							<div class="formValue"><input class="saveAction" id="dhcp-submit" type="button" value="<spring:message code='action.save'/>"></input></div>
						</div>
					</form>
				</div>
			</div>
		</div>		
		<h2><a  href="#"><spring:message code="network.header"/></a></h2>
		<div class="accordion-outerContainer">
			<div class="i1"></div>
			<div class="innerContainer">
				<div class="formContainer">
					<form id="network-details" method="post">
						<div class="field">
							<div class="formPrompt"><span><spring:message code="network.label.building.address"/></span></div>
							<div class="formValue"><span>${system[1]}</span></div>
						</div>
						<div class="field">
							<div class="formPrompt"><span><spring:message code="network.label.intranet.address"/></span></div>
							<div class="formValue"><span>${system[0]}</span></div>
						</div>
						<div class="field">
							<div class="formPrompt"><span><spring:message code="network.label.type"/></span></div>
							<div class="formValue">
								<input style="display: inline;" type="radio" id="typeStatic" onclick="enableForm();" checked="checked" name="type" value="static"/>
								<span style="display: inline;"><spring:message code="network.type.static"/></span> &nbsp;&nbsp;
								<input style="display: inline;" type="radio" id="typeDynamic" onclick="disableForm();" name="type" value="dynamic"/>
								<span style="display: inline;" ><spring:message code="network.type.dynamic"/></span>
							</div>
						</div>
						<div class="field">
							<div class="formPrompt"><span><spring:message code="network.label.internet.ip.address"/></span></div>
							<div class="formValue"><input size="20" maxlength="30" type="text" name="ip" id="ip" /></div>
						</div>
						<div class="field">
							<div class="formPrompt"><span><spring:message code="network.label.subnet.mask"/></span></div>
							<div class="formValue"><input size="20" maxlength="30" type="text" name="mask" id="mask" /></div>
						</div>
						<div class="field">
							<div class="formPrompt"><span><spring:message code="network.label.gateway"/></span></div>
							<div class="formValue"><input size="20" maxlength="30" type="text" name="gateway" id="gateway" /></div>
						</div>
						<div class="field">
							<div class="formPrompt"><span></span></div>
							<div class="formValue"><input class="saveAction" type="submit" id="networkSubmit" value="<spring:message code='action.submit'/>"></input></div>
						</div>
					</form>
				</div>
			</div>
		</div>
		<h2><a  href="#"><spring:message code="companySetup.heading.name"/></a></h2>
		</c:if>
		
		<div class="outerContainer plaindiv">
			<c:if test="${mode != 'admin'}"><span><spring:message code="companySetup.heading.name"/></span></c:if>
			<div class="i1"></div>
			<div>
				<div class="innerContainer">
					<spring:url value="/admin/organization/companyUpdate.ems" var="adminURL" scope="request"/>
					<spring:url value="createCompany.ems" var="actionURL" scope="request"/>
					<form:form id="company-register" commandName="company" method="post" action="${actionURL}">
						<input type="hidden" name="changeTime" id="changeTime" value="false"/>
						<div class="field">
							<div class="formPrompt"><span><spring:message code="companySetup.label.name"/></span></div>
							<div class="formValue"><form:input id="name" name="name"  path="name"/></div>
						</div>
						<div class="field">
							<div class="formPrompt"><span><spring:message code="companySetup.label.address"/></span></div>
							<div class="formValue"><form:textarea id="address" name="address"  path="address"></form:textarea></div>
						</div>
						<div class="field">
							<div class="formPrompt"><span><spring:message code="companySetup.label.email"/></span></div>
							<div class="formValue"><form:input onblur="copyEmail(this)" path="email" id="email" name="email"  maxLength="80"></form:input></div>
						</div>
						<div class="field">
							<div class="formPrompt"><span><spring:message code="companySetup.label.phone"/></span></div>
							<div class="formValue"><form:input id="contact" path="contact" name="contact" ></form:input></div>
						</div>
						<c:if test="${mode != 'admin'}">
							<div class="field">
								<div class="formPrompt"><span><spring:message code="companySetup.label.newPassword"/></span></div>
								<div class="formValue"><input type="password" id="newPassword" name="newPassword" ></input></div>
							</div>
							<div class="field">
								<div class="formPrompt"><span><spring:message code="companySetup.label.confirmPassword"/></span></div>
								<div class="formValue"><input id="confirmPassword" type="password" name="confirmPassword" ></input></div>
							</div>
						</c:if>
						<div class="field">
							<div class="formPrompt"><span><spring:message code="companySetup.label.timezone"/></span></div>
							<div class="formValue"><form:select id="timeZone" path="timeZone" name="timeZone" style="width: 200px;">
									<form:options items="${timezone}" itemValue="name" itemLabel="name"></form:options>
									</form:select>
							</div>
						</div>
						<div class="field">
							<div class="formPrompt"><span><spring:message code="companySetup.label.gemstime"/></span></div>
							<c:set var="now" value="<%=new java.util.Date()%>"/>
							<div class="formValue"><input id="gemstime" name="gemstime" onchange="timeChanged()" value='<fmt:formatDate pattern="hh:mm a" value="${now}" />' /></div>
						</div>
						<script type="text/javascript">
							$("#gemstime").ptTimeSelect({onClose: function(i) { $(i).closest('form').valid(); timeChanged();}});
						</script>
						<div class="field">
							<div class="formPrompt"><span><spring:message code="companySetup.label.requestAccess"/></span></div>
							<div class="formValue"><form:select name="selfLogin" path="selfLogin" onchange="toggleValidDomain(this);" id="selfLogin" style="width: 60px;">
									<form:option value="true"><spring:message code="lov.yes"/></form:option>
									<form:option value="false"><spring:message code="lov.no"/></form:option>
									</form:select>
							</div>
						</div>
						<div class="field">
							<div class="formPrompt"><span><spring:message code="companySetup.label.validCompanyDomains"/></span></div>
							<div class="formValue">
								<form:textarea id="validDomain" path="validDomain" name="validDomain" ></form:textarea>
								<span><spring:message code="companySetup.label.egDomains"/></span>
							</div>
						</div>
						<div class="field">
							<div class="formPrompt"><span><spring:message code="companySetup.label.notificationEmails"/></span></div>
							<div class="formValue">
								<form:textarea id="notificationEmail" path="notificationEmail" name="notificationEmail" ></form:textarea>
								<span><spring:message code="companySetup.label.egNotificationEmails"/></span>
							</div>
						</div>
						<div class="field">
							<div class="formPrompt"><span><spring:message code="companySetup.label.notificationSeverity"/></span></div>
							<div class="formValue"><form:select id="severity" path="severityLevel" name="severity" style="width: 200px;">
									<form:option value="Critical and Warning"><spring:message code="lov.critical.and.warning"/></form:option>
									<form:option value="Critical only"><spring:message code="lov.critical.only"/></form:option>
									<form:option value="None"><spring:message code="lov.none"/></form:option>
									</form:select>
							</div>
						</div>
						<div class="field">
							<div class="formPrompt"><span><spring:message code="companySetup.label.electricityRate"/></span></div>
							<div class="formValue">
								<div id="elecprefix" style="float: right; position: relative; right:125px"><span><spring:message code="companySetup.text.cents.kwh"/></span></div>
								<form:input path="price" id="price" name="price"  maxLength="10" style="width:50px"></form:input>
								<div id="elechint" style="clear: both;"><span><spring:message code="companySetup.label.egElectricityRate"/></span></div>
								<div style="clear: both;"><span><spring:message code="companySetup.label.pathToAdminPricing"/></span></div>
							</div>
						</div>
						<div class="field">
							<div class="formPrompt"><span></span></div>
							<div class="formValue"><input class="saveAction" id="submit" type="submit" value="<spring:message code='label.next'/>"></input></div>
						</div>
						
						<div style="height:50px"><!-- dont delete this div --></div>
						
						<script type="text/javascript">
							/* $('#elecprefix').css('right', $('#elechint').width() - $('#price').width() - $('#elecprefix').width() - 15); */
							if($('#price').val() != null && $('#price').val() != "") {
								var elecrate = parseFloat($('#price').val());
								$('#price').val(Math.round(elecrate * 100));
							}
						</script>
					</form:form>
				</div>
			</div>
		</div>
		
	<c:if test="${mode == 'admin'}">
	</div>
	</c:if>
</div>

<script type="text/javascript">
	if($('#selfLogin').val() == 'true'){
		$('#validDomain').removeAttr('disabled');
	}else{
		$('#validDomain').attr('disabled', 'disabled');
	}
	
	<c:if test="${mode == 'admin'}">
		$(function() {
			$(window).resize(function() {
				var setSize = $(window).height();
				setSize = setSize - 100;
				$(".topLevelContainer").css("height", setSize);
			});
		});
		$(".topLevelContainer").css("overflow", "auto");	
		$(".topLevelContainer").css("height", $(window).height() - 87);
		$("#company-register").attr("action", '${adminURL}');
		$('#submit').attr("value", "<spring:message code='action.save'/>");
	</c:if>

</script>
