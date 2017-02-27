<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
	

<div class="innerContainer">
	<div class="formContainer">
		<form id="filterform" onsubmit="submitfilter(); return false;">
			<div class="field">
				<div class="formPrompt"><span><spring:message code='eventsAndFault.startDate'/></span></div>
				<div class="formValue"><input id="startDatePicker" name="startDatePicker" type="text" /></div>
			</div>
			<div class="field">
				<div class="formPrompt"><span><spring:message code='eventsAndFault.endDate'/></span></div>
				<div class="formValue"><input id="endDatePicker" name="endDatePicker" type="text" /></div>
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
});

function submitfilter() {
	
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
			var sevirity = "${severity}";
			var emSevirity;
			
			if ($('#includeResolved').is(":checked"))
			{
				url='<spring:url value="/services/events/list/getdata"/>';
				sev = sev + "${severity}";
			}else
			{
				url='<spring:url value="/services/events/EmEventList"/>'; 
				console.log("sevirity " + sevirity);
				if(sevirity=="SEVERE")
				{
					sev = sev + "Critical,Major";
				}else if(sevirity=="INFO")
				{
					sev = sev + "Info";
				}else if(sevirity=="WARNING")
				{
					sev = sev + "Warning";
				}
			}
		}
	</c:forEach>
	var groupId= $("#group").val() ;
	if(groupId=null)
	{
		groupId="";
	}
	var userdata = "1" + "#" +
					encodeURIComponent($.trim($('#searchText').val())) + "#" +
					"" + "#" +
					$('#startDatePicker').val() + "#" + 
					$('#endDatePicker').val() + "#" +
					sev + "#" +
					"" + "#" + "END";
	$("#eventsTable").jqGrid("clearGridData");
	jQuery("#eventsTable").jqGrid('setGridParam',{url:url,postData: {"userData": userdata},page:1}).trigger("reloadGrid");
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
