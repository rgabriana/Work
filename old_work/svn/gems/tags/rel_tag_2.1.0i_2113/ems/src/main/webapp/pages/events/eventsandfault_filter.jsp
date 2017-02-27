<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
	

<div class="innerContainer">
	<div class="formContainer">
		<form id="filterform" onsubmit="submitfilter(); return false;">
			<div class="field">
				<div class="formPrompt"><span><spring:message code='eventsAndFault.profile'/></span></div>
				<div class="formValue">
					<select id="group" name="group" style="width: 200px;">
						<option selected="selected" value=""></option>
						<c:forEach var="group" items="${groups}">
							<option value="${group.id}" >${group.name}</option>
						</c:forEach>
					</select>
				</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span><spring:message code='eventsAndFault.startDate'/></span></div>
				<div class="formValue"><input id="startDatePicker" name="startDatePicker" type="text" /></div>
			</div>
			<div class="field">
				<div class="formPrompt"><span><spring:message code='eventsAndFault.endDate'/></span></div>
				<div class="formValue"><input id="endDatePicker" name="endDatePicker" type="text" /></div>
			</div>
			<div class="field">
				<div class="formPrompt"><span><spring:message code='eventsAndFault.label.eventType'/></span></div>
				<div class="formValue">
					<table>
						<tr>
							<td width="200" colspan="1" style="align: center;">
								<label style="color: black"><spring:message code='eventsAndFault.selectEventType'/> <br /></label>
								<select id="box1View" multiple="multiple" style="height: 200px; width: 200px;">
								<c:forEach var="event" items="${events}">
									<option value="${event.type}" >${event.type}</option>
								</c:forEach>
								</select>
							</td>
							<td width="150">
								<button id="to2" type="button">&nbsp;>&nbsp;</button>
			                    <button id="allTo2" type="button">&nbsp;>>&nbsp;</button>
			                    <button id="allTo1" type="button">&nbsp;<<&nbsp;</button>
			                    <button id="to1" type="button">&nbsp;<&nbsp;</button>
							</td>
							<td width="200" colspan="1">
								<label style="color: black"><spring:message code='eventsAndFault.selectedEventTypes'/> <br /></label>
								<select id="box2View" name="box2View" multiple="multiple" style="height: 200px; width: 200px;">
								</select>
							</td>
						</tr>
					</table>
				</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span><spring:message code='eventsAndFault.importance'/></span></div>
				<div class="formValue">
					<c:forEach var="severity" items="${severities}">
						<input id="${severity}" value="${severity}" type=checkbox ><label style="color: black">${severity} &nbsp;&nbsp;&nbsp;</label></input>
					</c:forEach>
				</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span><spring:message code='eventsAndFault.label.resolved'/></span></div>
				<div class="formValue">
					<select id="resolved" name="resolved">
					  <option value="-1"><spring:message code='lov.ignore'/></option>
					  <option value="0"><spring:message code='lov.yes'/></option>
					  <option selected="selected" value="1"><spring:message code='lov.no'/></option>
					</select>
				</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span><spring:message code='eventsAndFault.search'/></span></div>
				<div class="formValue"><input id="searchText" name="searchText" type="text"></input></div>
			</div>
			<div class="field">
				<div class="formPrompt"><span></span></div>
				<div class="formValue">
					<input class="navigation" type="submit" id="filterSubmit" value="<spring:message code='action.submit'/>" />
					<input class="navigation" type="button" id="filterreset" onclick="reset();$('#allTo1').click();" value="<spring:message code='action.reset'/>" />
				</div>
			</div>
		</form>
	</div>
</div>

<script type="text/javascript" >

$(document).ready(function() {
	if(parent.accTabSelected == 'pf')
	{
		$("#group").val(profilenodeid); 
		$("#group").attr("disabled", true);
	}
});

function submitfilter() {
	
	$("#box2View").children('option').attr('selected', 'selected');
	
	var first = true;
	var sev = "";
	
	<c:forEach var="severity" items="${severities}">
		if($("#"+ "${severity}").attr("checked") == "checked") {
			if(!first) {
				sev = sev + ",";
			}
			else {
				first = false;
			}
			sev = sev + "${severity}";
		}
	</c:forEach>
	
	var userdata = $('#resolved').val() + "#" +
					encodeURIComponent($.trim($('#searchText').val())) + "#" +
					$("#group").val() + "#" +
					$('#startDatePicker').val() + "#" + 
					$('#endDatePicker').val() + "#" +
					sev + "#" +
					($('#box2View').val() == null  ? "" : $('#box2View').val()) + "#" + "END";
	//alert(userdata);
	$("#eventsTable").jqGrid("GridUnload");
	start(userdata, 1, "", "asc");			
	
}

$( "#startDatePicker" ).datetimepicker({
	ampm: true,
	timeFormat: 'hh:mm:ss TT',
	dateFormat: 'yy:mm:dd',
	showSecond: true,
    onClose: function(dateText, inst) {
        var endDateTextBox = $('#endDatePicker');
        if (endDateTextBox.val() != '') {
            var testStartDate = $(this).datetimepicker('getDate');
            var testEndDate =  endDateTextBox.datetimepicker('getDate');
            if (testStartDate > testEndDate  && $(this).val() != '')
                endDateTextBox.val(dateText);
        }
    },
    onSelect: function (selectedDateTime){
        var start = $(this).datetimepicker('getDate');
        $('#endDatePicker').datetimepicker('option', 'minDate', new Date(start.getTime()));
    },
});
$( "#endDatePicker" ).datetimepicker({
	ampm: true,
	timeFormat: 'hh:mm:ss TT',
	dateFormat: 'yy:mm:dd',
	showSecond: true,
    onClose: function(dateText, inst) {
        var startDateTextBox = $('#startDatePicker');
        if (startDateTextBox.val() != '') {
            var testStartDate = startDateTextBox.datetimepicker('getDate');
            var testEndDate =$(this).datetimepicker('getDate');
            if (testStartDate > testEndDate && $(this).val() != '')
                startDateTextBox.val(dateText);
        }
    },
    onSelect: function (selectedDateTime){
        var end = $(this).datetimepicker('getDate');
        $('#startDatePicker').datetimepicker('option', 'maxDate', new Date(end.getTime()) );
    }
});

$.configureBoxes({useFilters: false, selectOnSubmit: false});

$('#startDatePicker').datetimepicker('option', 'maxDate', '' );
$('#startDatePicker').datetimepicker('option', 'minDate', '' );
$('#endDatePicker').datetimepicker('option', 'minDate', '');
$('#endDatePicker').datetimepicker('option', 'maxDate', '');

</script>
