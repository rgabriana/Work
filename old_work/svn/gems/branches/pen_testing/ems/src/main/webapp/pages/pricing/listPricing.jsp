<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/scripts/jquery/jquery.ptTimeSelect.js"
	var="ptTimeSelect"></spring:url>
<script type="text/javascript" src="${ptTimeSelect}"></script>
<spring:url value="/themes/standard/css/jquery/jquery.ptTimeSelect.css"
	var="ptTimeSelectCss"></spring:url>
<link rel="stylesheet" type="text/css" href="${ptTimeSelectCss}" />
<spring:url value="/scripts/jquery/jquery.validationEngine.js"
	var="jquery_validationEngine"></spring:url>
<script type="text/javascript" src="${jquery_validationEngine}"></script>
<spring:url value="/scripts/jquery/jquery.validationEngine-en.js"
	var="jquery_validationEngine_en"></spring:url>
<script type="text/javascript" src="${jquery_validationEngine_en}"></script>

<spring:url value="/pricing/addPricing.ems" var="addPricing"
	scope="request" />
<spring:url value="/pricing/updatePricing.ems" var="updatePricing"
	scope="request" />
<spring:url value="/pricing/removePricing.ems" var="removePricing"
	scope="request" />
<spring:url value="/themes/default/images/time_picker.jpeg"
	var="timePicker" scope="request" />

<spring:url value="/services/org/pricing/updatepricingconfiguration/" var="savePricingConfigUrl" scope="request" />

<script type="text/javascript">

<c:if test="${mode != 'admin'}">
$('#firstStep').hide();
$('#secondStep').hide();
$('#thirdStep').hide();
$('#fourthStep').hide();
$('#fifthStep').hide();
$('#sixthStep').show();
</c:if>

var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
var COLOR_ERROR = "red";

$(document).ready(function() {
	$("#defaultPrice").val("${defaultPrice}");
	
	var pricingType = "${pricingType}";
	$('#pricingType').val(pricingType);
	
	$('#currencyType').val("${currencyType}");
	
	if(pricingType == "2"){
		additionalHeight();
	} else {
		<c:if test="${mode != 'admin'}">
			$(".outermostdiv").css("height", $(window).height() - 30);
		</c:if>
	}
	
	$('#pricingType').change(function () {
		clearLabelMessage();
		if(this.value == 1){
			$('#weekdayFieldSet').css("display","none");
			$("#weekendFieldSet").css("display","none");
			$("#electricityRateTd").show();
			<c:if test="${mode != 'admin'}">
				$(".outermostdiv").css("height", $(window).height() - 30);
			</c:if>
			<c:if test="${mode == 'admin'}">
				$(".outermostdiv").css("height", $(window).height() - 100);
			</c:if>
			dispalyPricingConfigMessage("Please click save configuration button to save default electricity rate",COLOR_ERROR);
		}else{
			$('#weekdayFieldSet').css("display","block");
			$("#weekendFieldSet").css("display","block");
			$("#electricityRateTd").hide();
			dispalyPricingConfigMessage("Please click save configuration button to add new pricing or to update and delete existing pricing",COLOR_ERROR);
			additionalHeight();
			//$('#pricingTableDiv').children().attr('disabled','disabled');
			$('#pricingTableDiv').find('*').attr('disabled','disabled');
		}
	});
	
});

function ptTimeSelectClosed(i) {
	$(i).closest('form').validationEngine('validateField', i);
}

function additionalHeight(){
	var rows = document.getElementById("weekdayTable").getElementsByTagName("tbody")[0].getElementsByTagName("tr").length;
	var rows1 = document.getElementById("weekendTable").getElementsByTagName("tbody")[0].getElementsByTagName("tr").length;
	if(rows == null || rows == undefined){
		rows = 4;
	}
	if(rows1 == null || rows1 == undefined){
		rows1 = 4;
	}
	var additionalHt = (rows - 4) * 32;
	var additionalHt1 = (rows1 - 4) * 32;
	<c:if test="${mode != 'admin'}">
		$(".outermostdiv").css("height", $(window).height() + 280 + additionalHt + additionalHt1);
	</c:if>
	<c:if test="${mode == 'admin'}">
		$(".outermostdiv").css("height", $(window).height() + 130 + additionalHt + additionalHt1);
	</c:if>
}

function addNewRow(tableId) {
	clearLabelMessage();
	clearLabelMessageWeekend();
	var rows = document.getElementById(tableId).getElementsByTagName("tbody")[0].getElementsByTagName("tr").length;
	var r1 = rows+1;
	
	var daytype = null;
	if(tableId == 'weekdayTable') { 
		daytype = "weekday";
		r1 = rows+1+"w";
		var isRowAdded = "update";
		isRowAdded = $("#"+rows+"w_newadd").val();
		if(isRowAdded!= undefined || isRowAdded == "Add"){
			var msg = '<spring:message code="pricing.row.add.error"/>';
			displayLabelMessage(msg + daytype + " table first." , COLOR_ERROR);
			return false;
		}
	} else if(tableId == 'weekendTable') {
		daytype = "weekend";
		r1 = rows+1+"x";
		var isRowAdded = "update";
		isRowAdded = $("#"+rows+"x_newadd").val();
		if(isRowAdded!= undefined || isRowAdded == "Add"){
			var msg = '<spring:message code="pricing.row.add.error"/>';
			displayLabelMessageWeekend(msg + daytype + " table first." , COLOR_ERROR);
			return false;
		}
	}
	
	$('.newAction').attr('disabled', 'disabled');
	
	$('#' + tableId + ' tr:last').after('\
	<tr class="editableRow" id="'+r1+'_newrow"> \
			<td> <form id="'+r1+'_newform1" onsubmit="return false;" >\
				<input type="hidden" id="'+r1+'_newtype" value="' + daytype + '"/>\
				<span style="padding-right: 10px;"><spring:message code="pricing.from"/></span> \
				<input type="text" size="8" class="validate[required,funcCall[checkFormat]] text-input" id="'+r1+'_newfromTime"/>   \
				<span style="padding: 0px 10px;"><spring:message code="pricing.to"/></span> \
				<input type="text" size="8" class="validate[required,funcCall[checkFormat]] text-input" id="'+r1+'_newtoTime"/> \
			</form><span id="'+r1+'_newerror" class="error"></span></td>	\
			<td> \
				<select id="'+r1+'_newlevel"> \
					<option id="'+r1+'_newopt1" value="Off Peak">Off Peak</option> \
					<option id="'+r1+'_newopt2" value="Peak">Peak</option> \
					<option id="'+r1+'_newopt3" value="Partial Peak">Partial Peak</option> \
				</select> \
			</td> \
			<td><form id="'+r1+'_newform2" onsubmit="return false;" > <input id="'+r1+'_newprice" class="validate[required,funcCall[checkPricing]] text-input" size="10" type="text"/> </form> </td> \
			<td align="right" style="padding-right:5px"> \
				<input type="button" id="'+r1+'_newadd" onclick="submitNewRow(\''+r1+'\');" value=<spring:message code="action.add"/> /> \
				<input type="button" id="'+r1+'_newcancel" onclick="cancelNewRow(\''+r1+'\');" value=<spring:message code="action.cancel"/> /> \
			</td> \
	</tr>');
	$("#"+r1+"_newform1").validationEngine();
	$("#"+r1+"_newform2").validationEngine();
	$('#'+r1+'_newfromTime').ptTimeSelect({onClose: ptTimeSelectClosed, onFocusDisplay: false, popupImage: '<img src="${timePicker}" class="timePickerImageStyle" />' });
	$('#'+r1+'_newtoTime').ptTimeSelect({onClose: ptTimeSelectClosed, onFocusDisplay: false, popupImage: '<img src="${timePicker}" class="timePickerImageStyle" />' });
	var size = $(".outermostdiv").height();
	$(".outermostdiv").css("height", size + 32);
	
	if(tableId == 'weekdayTable') {
		displayLabelMessage('<spring:message code="pricing.row.add.success"/>', COLOR_SUCCESS);
	} else if(tableId == 'weekendTable'){
		displayLabelMessageWeekend('<spring:message code="pricing.row.add.success"/>' , COLOR_SUCCESS);
	}
}

function cancelNewRow(rowno) {
	clearLabelMessage();
	clearLabelMessageWeekend();
	
	$("#"+rowno+"_newform1").validationEngine('hide');
	$("#"+rowno+"_newform2").validationEngine('hide');
	$("#"+rowno+"_newrow").remove();
	$('.newAction').removeAttr('disabled');
	var size = $(".outermostdiv").height();
	$(".outermostdiv").css("height", size - 32);
	if(rowno.indexOf("w") > -1){
		displayLabelMessage('<spring:message code="pricing.row.remove.success"/>', COLOR_SUCCESS);
	} else if(rowno.indexOf("x") > -1){
		displayLabelMessageWeekend('<spring:message code="pricing.row.remove.success"/>', COLOR_SUCCESS);
	}
}

function checkFormat(field) {
	var time = field.val();
	var invalidFormat = '<spring:message code="error.valid.time.format"/>';
	if(time.indexOf(" ") == -1) {
		return invalidFormat;
	}
	var timearr = time.split(" ");
	if(timearr[1].toUpperCase() != "AM" && timearr[1].toUpperCase() != "PM") {
		return invalidFormat;
	}
	if(timearr[0].indexOf(":") == -1){
		return invalidFormat;
	}
	var timearr1 = timearr[0].split(":");
	if(timearr1[0] < 0 || timearr1[0] >  12){
		return invalidFormat;
	}
	if(timearr1[1] < 0 || timearr1[0] >  59){
		return invalidFormat;
	}
}
function checkPricing(field)
{
	var price =  field.val();
	var invalidFormat = '<spring:message code="error.price.number.required"/>';
	var RE = /^[\+]?(([0-9]+)([\.,]([0-9]+))?|([\.,]([0-9]+))?)$/;
	if(!RE.test(price)){
	       return invalidFormat;
	   }
}
function updateRow(obj) {
	clearLabelMessage();
	clearLabelMessageWeekend();
	var id = $(obj).attr('id').split('u')[0];
	var daytype = $("#" + id + "type").val();
	$("#" + id + "error").html('');
	if($("#"+id + "form1").validationEngine('validate') && $("#"+id+"form2").validationEngine('validate')) {
		var interval = $("#"+id+"fromTime").val() + " - " + $("#"+id+"toTime").val();
		$.ajax({
			   type: "POST",
			   url: '<spring:url value="/services/org/pricing/update"/>',
			   contentType: "application/json",
			   data: '{"id":"' + id + '","priceLevel":"' + $("#"+id+"level").val()  + '","interval":"' + interval + '","price":"' + $("#" + id +"price").val() + '","dayType":"' + $("#" + id + "type").val() + '"}',
			   dataType: "json",
			   success: function(msg){
				   if(msg.status == "1") {
					   if(msg.msg == "overlap") {
						   $("#" + id + "error").html('<spring:message code="error.valid.interval.overlaps"/>');
					   }
					   else if(msg.msg == "noInterval") {
						   $("#" + id + "error").html('<spring:message code="error.valid.interval.invalid"/>');
					   }
					   else {
						   $("#" + id + "error").html('<spring:message code="pricing.invalid.input"/>');
					   }
					  
				   } 
				   if(msg.status == "0")
				   {
					   if(daytype == "weekday"){
						   displayLabelMessage('<spring:message code="pricing.change.success"/>', COLOR_SUCCESS);
						} else if(daytype == "weekend") {
							displayLabelMessageWeekend('<spring:message code="pricing.change.success"/>', COLOR_SUCCESS);
						}
				   }
			   }			   
		});	
	}

}

function deleteRow(obj) {
	clearLabelMessage();
	clearLabelMessageWeekend();
	if(confirm('Are you sure you want to delete this pricing row ?')){
		var id = $(obj).attr('id').split('d')[0];
		var daytype = $("#" + id + "type").val();
		$("#"+id + "form1").validationEngine('hide');
		$("#"+id+"form2").validationEngine('hide');
		$.ajax({
			   type: "POST",
			   url: '<spring:url value="/services/org/pricing/delete"/>',
			   contentType: "application/json",
			   data: '{"id":"' + id +'"}',
			   dataType: "json",
			   success: function(msg){
				   if(msg.status == "1") {
						  $("#" + id + "error").html('<spring:message code="pricing.invalid.input"/>');	  
				   }
				   else {
					   $("#"+id + "row").remove();
				   }
				   if(msg.status == "0")
				   {
					   var size = $(".outermostdiv").height();
					   $(".outermostdiv").css("height", size - 32);
					   if(daytype == "weekday"){
						   displayLabelMessage('<spring:message code="pricing.delete.success"/>', COLOR_SUCCESS);
						} else if(daytype == "weekend") {
							displayLabelMessageWeekend('<spring:message code="pricing.delete.success"/>', COLOR_SUCCESS);
						}
				   }
			   }			   
		});
	}
}

function submitNewRow(rowno) {
	clearLabelMessage();
	clearLabelMessageWeekend();
	var valid1 = $("#"+rowno+"_newform1").validationEngine('validate');
	var valid2 = $("#"+rowno+"_newform2").validationEngine('validate');
	if(valid1 && valid2) {
		$("#"+rowno+"_newerror").html('');
		var interval = $("#"+rowno+"_newfromTime").val() + " - " + $("#"+rowno+"_newtoTime").val();
		$.ajax({
			   type: "POST",
			   url: '<spring:url value="/services/org/pricing/add"/>',
			   contentType: "application/json",
			   data: '{"priceLevel":"' + $("#"+rowno+"_newlevel").val()  + '","interval":"' + interval + '","price":"' + $("#"+rowno+"_newprice").val() + '","dayType":"' + $("#"+rowno+"_newtype").val() + '"}',
			   dataType: "json",
			   success: function(msg){
				   if(msg.status == "1") {
					   if(msg.msg == "overlap") {
						   $("#"+rowno+"_newerror").html('<spring:message code="error.valid.interval.overlaps"/>');
					   }
					   else if(msg.msg == "noInterval") {
						   $("#"+rowno+"_newerror").html('<spring:message code="error.valid.interval.invalid"/>');
					   }
					   else {
						   $("#"+rowno+"_newerror").html('<spring:message code="pricing.invalid.input"/>');
					   }
				   }
				   else {
					   	var newid = msg.msg;
					   	$('#'+rowno+'_newform1').attr('id', newid + 'form1');
						$('#'+rowno+'_newform2').attr('id', newid + 'form2');
						$("#" + newid +"form1").validationEngine();
						$("#" + newid +"form2").validationEngine();
						$("#"+rowno+"_newrow").attr('id', newid + "row");
						$('#'+rowno+'_newtype').attr('id', newid + 'type');
						$("#"+rowno+"_newfromTime").attr('id', newid + "fromTime");
						$("#"+rowno+"_newtoTime").attr('id', newid + "toTime");
						$("#"+rowno+"_newerror").attr('id', newid + "error");
						$("#"+rowno+"_newlevel").attr('id', newid + "level");
						$("#"+rowno+"_newopt1").attr('id', newid + "opt1");
						$("#"+rowno+"_newopt2").attr('id', newid + "opt2");
						$("#"+rowno+"_newopt3").attr('id', newid + "opt3");
						$("#"+rowno+"_newprice").attr('id', newid + "price");
						$("#"+rowno+"_newadd").attr('onclick', "updateRow(this);");
						$("#"+rowno+"_newadd").attr('value', '<spring:message code="action.update"/>' );
						$("#"+rowno+"_newadd").attr('id', newid + "update");
						$("#"+rowno+"_newcancel").attr('onclick', "deleteRow(this);");
						$("#"+rowno+"_newcancel").attr('value', '<spring:message code="action.delete"/>' );
						$("#"+rowno+"_newcancel").attr('id', newid + "delete");
						$('.newAction').removeAttr('disabled');
						
						if(rowno.indexOf("w") > -1){
							displayLabelMessage('<spring:message code="pricing.row.save.success"/>', COLOR_SUCCESS);
						} else if(rowno.indexOf("x") > -1){
							displayLabelMessageWeekend('<spring:message code="pricing.row.save.success"/>', COLOR_SUCCESS);
						}
				   }
			   }			   
		});	

	}

}

function displayLabelMessage(Message, Color) {
		$("#pricing_message").html(Message);
		$("#pricing_message").css("color", Color);
}

function clearLabelMessage() {
	displayLabelMessage("", COLOR_DEFAULT);
}

function displayLabelMessageWeekend(Message, Color) {
	$("#pricing_message_weekend").html(Message);
	$("#pricing_message_weekend").css("color", Color);
}

function clearLabelMessageWeekend() {
	displayLabelMessageWeekend("", COLOR_DEFAULT);
}

function dispalyPricingConfigMessage(Message, Color) {
	$("#pricingconfigerror").html(Message);
	$("#pricingconfigerror").css("color", Color);
}

function clearPricingConfigMessage() {
	dispalyPricingConfigMessage("", COLOR_DEFAULT);
}


function savePricingConfig(){
	clearPricingConfigMessage();
	clearLabelMessage();
	
	var defaultPriceValue = $('#defaultPrice').val();
	
	if($('#pricingType').val() == "2"){
		defaultPriceValue = 0.15;
	}
	
	if (isNaN(defaultPriceValue)) {
		dispalyPricingConfigMessage('Default Electricity Rate Value must be numeric',COLOR_ERROR);
		return;
	}
	
	if(defaultPriceValue < 0 ){
		dispalyPricingConfigMessage('Default Electricity Rate Value must be postive',COLOR_ERROR);
		return;
	}
	
	$.ajax({
		type: 'POST',
		url: "${savePricingConfigUrl}"+$('#pricingType').val()+"/"+defaultPriceValue+"/"+$('#currencyType').val()+"?ts="+new Date().getTime(),
		data: "",
		success: function(data){
			if(data.status == 1){
				$('#pricingTableDiv').children().removeAttr('disabled');
				dispalyPricingConfigMessage('Pricing Configuration values successfully saved.',COLOR_SUCCESS);
			}
		},
		dataType:"json",
		contentType: "application/json; charset=utf-8"
	});
	
}

</script>
<div class="topmostContainer">
	<div class="outermostdiv">
		<div class="outerContainer">
			<span>Pricing</span>
			<div id="pricingPanel">
  				<div id="pricingDiv"></div>
			</div>
		</div>
		<div class="field">
			<!--
			<div>							
				<span style="font-weight: bold;padding-left:20px"><spring:message code="companySetup.label.enablePricing"/></span>
				<select id="pricingType" style="width: 200px;">
					<option value="1"><spring:message code="companySetup.pricing.fixed"/></option>
					<option value="2"><spring:message code="companySetup.pricing.timeofday"/></option>									
				</select>
				<span style="font-weight: bold;padding-left:20px" id="electricityRateLabel"><spring:message code="companySetup.label.electricityRate"/></span>
				<input id="defaultPrice" maxLength="10" style="width:50px"></input>
				<span style="font-weight: bold;padding-left:20px">Select Currency</span>
				<select id="currencyType" style="width: 200px;padding-right:20px">
					<c:forEach var="currency" items="${currencyArray}">
				    	<option value=<c:out value="${currency}" /> ><c:out value="${currency}" /></option>
				    </c:forEach>
				</select>
				<input style="font-weight: bold;" type="button" id="savePricingConfig" value="Save Pricing Configuration" onclick="savePricingConfig();"></input>
			</div>
			-->
			<div>
			<table>
			<tr>
				<td>
					<span style="font-weight: bold;padding-left:20px"><spring:message code="companySetup.label.enablePricing"/></span>
					<select id="pricingType" style="width: 200px;">
						<option value="1" <c:if test="${pricingType == null || pricingType == '1'}"> selected="selected" </c:if>><spring:message code="companySetup.pricing.fixed"/></option>
						<option value="2" <c:if test="${pricingType == '2'}"> selected="selected" </c:if>><spring:message code="companySetup.pricing.timeofday"/></option>									
					</select>
				</td>
				<td id="electricityRateTd" <c:if test="${pricingType != null && pricingType == '2'}"> style="display: none;" </c:if>>
					<span style="font-weight: bold;padding-left:20px" ><spring:message code="companySetup.label.electricityRate"/></span>
					<input id="defaultPrice" maxLength="10" style="width:50px"></input>
				</td>
				<td>
					<span style="font-weight: bold;padding-left:20px">Select Currency</span>
					<select id="currencyType" style="width: 200px;padding-right:20px">
						<c:forEach var="currency" items="${currencyArray}">
					    	<option value=<c:out value="${currency}" /> <c:if test="${currencyType!=null && currency == currencyType}"> selected="selected" </c:if> ><c:out value="${currency}" /></option>
					    </c:forEach>
				    </select>
			    </td>
			    <td  style="padding-left:20px">
					<input style="font-weight: bold;" type="button" id="savePricingConfig" value="Save Pricing Configuration" onclick="savePricingConfig();"></input>
				</td>
			</tr>
			</table>
			</div>
			
			<div>
				<span id="pricingconfigerror" class="error" style="font-weight: bold;padding-left:20px"></span>
			</div>
		</div>
		<div id="pricingTableDiv">
		<fieldset id="weekdayFieldSet" <c:if test="${pricingType != null && pricingType == '1'}"> style="margin:15px;display: none;" </c:if>
				<c:if test="${pricingType == null || pricingType == '2'}">style="margin:15px;"</c:if>>
			<legend>
				<spring:message code="pricing.header.weekday" />
			</legend>
			<div>
				<div> 
	 				<div style="float: left; padding: 5px 0 10px 0px;">
						<input id="newWeekday" type="button"
							onclick="addNewRow('weekdayTable');" value='<spring:message code="pricing.action.new"/>' />
					 </div>
					<div id="pricing_message" style="font-size: 14px; font-weight: bold;padding: 5px 0 10px 0px; margin-left: 150px;" ></div>
					<div class="i1"></div>
				</div> 
				
				<table id="weekdayTable" class="entable"
					style="width: 100%; height: 100%;">
					<thead>
						<tr class="editableRow">
							<th width="40%" align="left"><spring:message
									code="pricing.label.time.interval" /></th>
							<th align="left"><spring:message
									code="pricing.label.price.level" /></th>
							<th align="left"><spring:message
									code="pricing.label.pricing" /></th>
							<th align="right" style="padding-right: 5px"><spring:message
									code="pricing.label.action" /></th>
						</tr>
					</thead>
					<c:forEach items="${weekdays}" var="day">
						<tr id="${day.id}row" class="editableRow">

							<td><form id="${day.id}form1" onsubmit="return false;">
									<input type="hidden" id="${day.id}type" value="weekday" /> <span
										style="padding-right: 10px;"><spring:message
											code="pricing.from" /></span>
									<div class="innerContainerInputFieldValue">
										<input type="text" size="8"
											class="validate[required,funcCall[checkFormat]] text-input"
											id="${day.id}fromTime" />
									</div>
									<span style="padding: 0px 10px;"><spring:message
											code="pricing.to" /></span>
									<div class="innerContainerInputFieldValue">
										<input type="text" size="8"
											class="validate[required,funcCall[checkFormat]] text-input"
											id="${day.id}toTime" />
									</div>
								</form> <span id="${day.id}error" class="error"></span></td>
							<td><select id="${day.id}level">
									<option id="${day.id}opt1" value="Off Peak">Off Peak</option>
									<option id="${day.id}opt2" value="Peak">Peak</option>
									<option id="${day.id}opt3" value="Partial Peak">Partial
										Peak</option>
							</select></td>
							<td><form id="${day.id}form2" onsubmit="return false;">
									<div class="innerContainerInputFieldValue">
										<input id="${day.id}price"
											class="validate[required,funcCall[checkPricing]] text-input"
											size="10" value="${day.price}" type="text" />
									</div>
								</form></td>
							<td align='right' style='padding-right: 5px'><input
								type="button" id="${day.id}update" onclick="updateRow(this);"
								value=<spring:message code="action.update"/> /> <input
								type="button" id="${day.id}delete" onclick="deleteRow(this);"
								value=<spring:message code="action.delete"/> /></td>
							<script type="text/javascript">
								$("#"+"${day.id}" + "form1").validationEngine();
								$("#"+"${day.id}" + "form2").validationEngine();
								
								$("#"+"${day.id}" + "form1").validationEngine('attach' , {

									isOverflown: true,

									overflownDIV: ".topmostContainer"

								});
								
								$("#"+"${day.id}" + "form2").validationEngine('attach' , {

									isOverflown: true,

									overflownDIV: ".topmostContainer"

								});
								
								var interval = "${day.interval}";
								var subValue = interval.split(' - ');
								var fromTime = subValue[0];
								var toTime = subValue[1];
								$("#"+"${day.id}" + "fromTime").val(fromTime);
								$("#"+"${day.id}" + "toTime").val(toTime);
								$("#"+"${day.id}" + "fromTime").ptTimeSelect({onClose: ptTimeSelectClosed, onFocusDisplay: false, popupImage: '<img src="${timePicker}" class="timePickerImageStyle" />' });
								$("#"+"${day.id}" + "toTime").ptTimeSelect({onClose: ptTimeSelectClosed, onFocusDisplay: false, popupImage: '<img src="${timePicker}" class="timePickerImageStyle" />' });
								var pl = "${day.priceLevel}";
								if(pl == "Off Peak") {
									$("#"+"${day.id}"+"opt1").attr("selected", "selected");
								}
								else if(pl == "Peak"){
									$("#"+"${day.id}"+"opt2").attr("selected", "selected");
								}
								else {
									$("#"+"${day.id}"+"opt3").attr("selected", "selected");
								}
							</script>
						</tr>

					</c:forEach>
				</table>

			</div>
		</fieldset>

		<fieldset id="weekendFieldSet" <c:if test="${pricingType != null && pricingType == '1'}"> style="margin:15px;display: none;" </c:if>
				<c:if test="${pricingType == null || pricingType == '2'}">style="margin:15px;"</c:if>>
			<legend>
				<spring:message code="pricing.header.weekend" /> 
			</legend>
			<div>
			<div style="float: left; padding: 5px 0 10px 0px;">
			<input
				id="newWeekend" type="button"
				onclick="addNewRow('weekendTable');"
				value='<spring:message code="pricing.action.new"/>' />
			</div>
			<div id="pricing_message_weekend" style="font-size: 14px; font-weight: bold;padding: 5px 0 10px 0px; margin-left: 150px;" ></div>
			<div class="i1"></div>
			<table id="weekendTable" class="entable"
				style="width: 100%; height: 100%;">
				<thead>
					<tr class="editableRow">
						<th width="40%" align="left"><spring:message
								code="pricing.label.time.interval" /></th>
						<th align="left"><spring:message
								code="pricing.label.price.level" /></th>
						<th align="left"><spring:message code="pricing.label.pricing" /></th>
						<th align="right" style="padding-right: 5px"><spring:message
								code="pricing.label.action" /></th>
					</tr>
				</thead>
				<c:forEach items="${weekends}" var="day">
					<tr id="${day.id}row" class="editableRow">
						<td><form id="${day.id}form1" onsubmit="return false;">
								<input type="hidden" id="${day.id}type" value="weekend" /> <span
									style="padding-right: 10px;"><spring:message
										code="pricing.from" /></span>
								<div class="innerContainerInputFieldValue">
									<input type="text" size="8"
										class="validate[required,funcCall[checkFormat]] text-input"
										id="${day.id}fromTime" />
								</div>
								<span style="padding: 0px 10px;"><spring:message
										code="pricing.to" /></span>
								<div class="innerContainerInputFieldValue">
									<input type="text" size="8"
										class="validate[required,funcCall[checkFormat]] text-input"
										id="${day.id}toTime" />
								</div>
							</form> <span id="${day.id}error" class="error"></span></td>
						<td><select id="${day.id}level">
								<option id="${day.id}opt1" value="Off Peak">Off Peak</option>
								<option id="${day.id}opt2" value="Peak">Peak</option>
								<option id="${day.id}opt3" value="Partial Peak">Partial
									Peak</option>
						</select></td>
						<td><form id="${day.id}form2" onsubmit="return false;">
								<div class="innerContainerInputFieldValue">
									<input id="${day.id}price"
										class="validate[required,funcCall[checkPricing]] text-input" size="10"
										value="${day.price}" type="text" />
								</div>
							</form></td>
						<td align='right' style='padding-right: 5px'><input
							type="button" id="${day.id}update" onclick="updateRow(this);"
							value=<spring:message code="action.update"/> /> <input
							type="button" id="${day.id}delete" onclick="deleteRow(this);"
							value=<spring:message code="action.delete"/> /></td>
						<script type="text/javascript">
								$("#"+"${day.id}" + "form1").validationEngine();
								$("#"+"${day.id}" + "form2").validationEngine();
								
								$("#"+"${day.id}" + "form1").validationEngine('attach' , {

									isOverflown: true,

									overflownDIV: ".topmostContainer"

								});
								
								$("#"+"${day.id}" + "form2").validationEngine('attach' , {

									isOverflown: true,

									overflownDIV: ".topmostContainer"

								});
								
								var interval = "${day.interval}";
								var subValue = interval.split(' - ');
								var fromTime = subValue[0];
								var toTime = subValue[1];
								$("#"+"${day.id}" + "fromTime").val(fromTime);
								$("#"+"${day.id}" + "toTime").val(toTime);
								$("#"+"${day.id}" + "fromTime").ptTimeSelect({onClose: ptTimeSelectClosed, onFocusDisplay: false, popupImage: '<img src="${timePicker}" class="timePickerImageStyle" />' });
								$("#"+"${day.id}" + "toTime").ptTimeSelect({onClose: ptTimeSelectClosed, onFocusDisplay: false, popupImage: '<img src="${timePicker}" class="timePickerImageStyle" />' });
								var pl = "${day.priceLevel}";
								if(pl == "Off Peak") {
									$("#"+"${day.id}"+"opt1").attr("selected", "selected");
								}
								else if(pl == "Peak"){
									$("#"+"${day.id}"+"opt2").attr("selected", "selected");
								}
								else {
									$("#"+"${day.id}"+"opt3").attr("selected", "selected");
								}
							</script>
					</tr>
				</c:forEach>
			</table>
			</div>
		</fieldset>
		</div>
		<!-- <div class="emptyContainer" style="width: 100%; height: 160px"></div> -->
		
		<div class="navdiv" align="center">
			<c:if test="${mode != 'admin'}">
				<spring:url value="/createArea.ems" var="prevURL" scope="request" />
				<spring:url value="/finishSetup.ems" var="finishURL" scope="request" />
	
				<table border="0" cellpadding="0" cellspacing="0">
					<tr>
						<td align="right">
							<form method="post" action="${prevURL}">
								<input class="navigation" type="submit"
									value="<spring:message code='label.prev'/>" />
							</form>
						</td>
						<td style="width: 30px"></td>
						<td align="left">
							<form method="post" action="${finishURL}">
								<input id="finishAllSetup" class="navigation" type="submit"
									value="<spring:message code='label.finish.setup'/>" />
							</form>
						</td>
					</tr>
				</table>
			</c:if>
		</div>
		
		
	</div>
</div>
<script type="text/javascript">
	
	<c:if test="${mode == 'admin'}">
	
	$(function() {
		$(window).resize(function() {
			var setSize = $(window).height();
			setSize = setSize - 100;
			$(".outermostdiv").css("height", setSize);
		});
	});
	$(".outermostdiv").css("height", $(window).height() - 100);
	$(".outermostdiv").css("overflow", "auto");
	$(".outermostdiv").css("margin-bottom", 5);
	$(".outermostdiv").css("margin-top", 5);
	
	</c:if>

</script>
