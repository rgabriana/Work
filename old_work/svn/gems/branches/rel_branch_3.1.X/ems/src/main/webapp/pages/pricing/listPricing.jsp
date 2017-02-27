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

<script type="text/javascript">
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
function ptTimeSelectClosed(i) {
	$(i).closest('form').validationEngine('validateField', i);
}

function addNewRow(tableId) {
	clearLabelMessage();
	$('.newAction').attr('disabled', 'disabled');
	var daytype = null;
	if(tableId == 'weekdayTable') { 
		daytype = "weekday";
	} else {
		daytype = "weekend";
	}
	$('#' + tableId + ' tr:last').after('\
	<tr class="editableRow" id="newrow"> \
			<td> <form id="newform1" onsubmit="return false;" >\
				<input type="hidden" id="newtype" value="' + daytype + '"/>\
				<span style="padding-right: 10px;"><spring:message code="pricing.from"/></span> \
				<input type="text" size="8" class="validate[required,funcCall[checkFormat]] text-input" id="newfromTime"/>   \
				<span style="padding: 0px 10px;"><spring:message code="pricing.to"/></span> \
				<input type="text" size="8" class="validate[required,funcCall[checkFormat]] text-input" id="newtoTime"/> \
			</form><span id="newerror" class="error"></span></td>	\
			<td> \
				<select id="newlevel"> \
					<option id="newopt1" value="Off Peak">Off Peak</option> \
					<option id="newopt2" value="Peak">Peak</option> \
					<option id="newopt3" value="Partial Peak">Partial Peak</option> \
				</select> \
			</td> \
			<td><form id="newform2" onsubmit="return false;" > <input id="newprice" class="validate[required,funcCall[checkPricing]] text-input" size="10" type="text"/> </form> </td> \
			<td align="right" style="padding-right:5px"> \
				<input type="button" id="newadd" onclick="submitNewRow();" value=<spring:message code="action.add"/> /> \
				<input type="button" id="newcancel" onclick="cancelNewRow();" value=<spring:message code="action.cancel"/> /> \
			</td> \
	</tr>');
	$("#newform1").validationEngine();
	$("#newform2").validationEngine();
	$('#newfromTime').ptTimeSelect({onClose: ptTimeSelectClosed, onFocusDisplay: false, popupImage: '<img src="${timePicker}" class="timePickerImageStyle" />' });
	$('#newtoTime').ptTimeSelect({onClose: ptTimeSelectClosed, onFocusDisplay: false, popupImage: '<img src="${timePicker}" class="timePickerImageStyle" />' });
	
	displayLabelMessage('<spring:message code="pricing.row.add.success"/>', COLOR_SUCCESS);
}

function cancelNewRow() {
	clearLabelMessage();
	$("#newform1").validationEngine('hide');
	$("#newform2").validationEngine('hide');
	$('#newrow').remove();
	$('.newAction').removeAttr('disabled');
	displayLabelMessage('<spring:message code="pricing.row.remove.success"/>', COLOR_SUCCESS);
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
	var id = $(obj).attr('id').split('u')[0];
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
					   displayLabelMessage('<spring:message code="pricing.change.success"/>', COLOR_SUCCESS);
				   }
			   }			   
		});	
	}

}

function deleteRow(obj) {
	clearLabelMessage();
	if(confirm('Are you sure you want to delete this pricing row ?')){
		var id = $(obj).attr('id').split('d')[0];
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
					   displayLabelMessage('<spring:message code="pricing.delete.success"/>', COLOR_SUCCESS);
				   }
			   }			   
		});
	}
}

function submitNewRow() {
	clearLabelMessage();
	var valid1 = $("#newform1").validationEngine('validate');
	var valid2 = $("#newform2").validationEngine('validate');
	if(valid1 && valid2) {
		$("#newerror").html('');
		var interval = $("#newfromTime").val() + " - " + $("#newtoTime").val();
		$.ajax({
			   type: "POST",
			   url: '<spring:url value="/services/org/pricing/add"/>',
			   contentType: "application/json",
			   data: '{"priceLevel":"' + $("#newlevel").val()  + '","interval":"' + interval + '","price":"' + $("#newprice").val() + '","dayType":"' + $("#newtype").val() + '"}',
			   dataType: "json",
			   success: function(msg){
				   if(msg.status == "1") {
					   if(msg.msg == "overlap") {
						   $("#newerror").html('<spring:message code="error.valid.interval.overlaps"/>');
					   }
					   else if(msg.msg == "noInterval") {
						   $("#newerror").html('<spring:message code="error.valid.interval.invalid"/>');
					   }
					   else {
						   $("#newerror").html('<spring:message code="pricing.invalid.input"/>');
					   }
				   }
				   else {
					   	var newid = msg.msg;
						$('#newform1').attr('id', newid + 'form1');
						$('#newform2').attr('id', newid + 'form2');
						$("#" + newid +"form1").validationEngine();
						$("#" + newid +"form2").validationEngine();
						$("#newrow").attr('id', newid + "row");
						$('#newtype').attr('id', newid + 'type');
						$("#newfromTime").attr('id', newid + "fromTime");
						$("#newtoTime").attr('id', newid + "toTime");
						$("#newerror").attr('id', newid + "error");
						$("#newlevel").attr('id', newid + "level");
						$("#newopt1").attr('id', newid + "opt1");
						$("#newopt2").attr('id', newid + "opt2");
						$("#newopt3").attr('id', newid + "opt3");
						$("#newprice").attr('id', newid + "price");
						$("#newadd").attr('onclick', "updateRow(this);");
						$("#newadd").attr('value', '<spring:message code="action.update"/>' );
						$("#newadd").attr('id', newid + "update");
						$("#newcancel").attr('onclick', "deleteRow(this);");
						$("#newcancel").attr('value', '<spring:message code="action.delete"/>' );
						$("#newcancel").attr('id', newid + "delete");
						$('.newAction').removeAttr('disabled');
						
						displayLabelMessage('<spring:message code="pricing.row.save.success"/>', COLOR_SUCCESS);
				   }
			   }			   
		});	

	}

}

function displayLabelMessage(Message, Color) {
		$("#pricing_message").html(Message);
		$("#pricing_message").css("color", Color);
	}
function clearLabelMessage(Message, Color) {
	displayLabelMessage("", COLOR_DEFAULT);
}
</script>
<div class="topmostContainer">
	<div id="pricingTableDiv" class="outermostdiv">
		<div class="outerContainer">
			<span>Pricing</span>
			<div id="pricingPanel">
  				<div id="pricingDiv"></div>
			</div>
		</div>
		<fieldset style="margin: 15px">
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

		<fieldset style="margin: 15px">
			<legend>
				<spring:message code="pricing.header.weekend" /> 
			</legend>
			<input
				id="newWeekend" type="button"
				onclick="addNewRow('weekendTable');"
				value='<spring:message code="pricing.action.new"/>' />
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

		</fieldset>
		<!-- <div class="emptyContainer" style="width: 100%; height: 160px"></div> -->
	</div>
</div>
<script type="text/javascript">
	$(function() {
		$(window).resize(function() {
			var setSize = $(window).height();
			setSize = setSize - 100;
			$(".topmostContainer").css("height", setSize);
		});
	});
	$(".topmostContainer").css("overflow", "auto");
	$(".topmostContainer").css("height", $(window).height() - 100);	
	var pricingType = "${pricingType}";
	if(pricingType == "1")
	{
	document.getElementById("pricingDiv").innerHTML="<b><u>Flat Pricing is selected , to enable editing of the Price details goto Organization Setup then select Time of Use (TOU) Pricing</u><b>"; 
	$('#pricingTableDiv').children().attr('disabled','disabled');	
	}
</script>