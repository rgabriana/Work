<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
	

<div class="innerContainer">
	<div class="formContainer">
		<form id="filterform" onsubmit="submitfilter(); return false;">
			<div class="field">
				<div class="formPrompt"><span><spring:message code='audit.filter.username'/></span></div>
				<div class="formValue"><input id="searchUser" name="searchUser" type="text"></input></div>
			</div>
			<div class="field">
				<div class="formPrompt"><span><spring:message code='audit.filter.startDate'/></span></div>
				<div class="formValue"><input id="startDatePicker" name="startDatePicker" type="text" /></div>
			</div>
			<div class="field">
				<div class="formPrompt"><span><spring:message code='audit.filter.endDate'/></span></div>
				<div class="formValue"><input id="endDatePicker" name="endDatePicker" type="text" /></div>
			</div>
			<div class="field">
				<div class="formPrompt"><span><spring:message code='audit.filter.action'/></span></div>
				<div class="formValue">
					<table>
						<tr>
							<td width="200" colspan="1" style="align: center;">
								<label style="color: black"><spring:message code='audit.filter.selectAction'/> <br /></label>
								<select id="box1View" multiple="multiple" style="height: 200px; width: 200px;">
								<c:forEach var="action" items="${actions}">
									<option value="${action}" >${action}</option>
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
								<label style="color: black"><spring:message code='audit.filter.selectedAction'/> <br /></label>
								<select id="box2View" name="box2View" multiple="multiple" style="height: 200px; width: 200px;">
								</select>
							</td>
						</tr>
					</table>
				</div>
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

function submitfilter() {
	
	$("#box2View").children('option').attr('selected', 'selected');
	
	var userdata = '1' + "#" +
					encodeURIComponent($.trim($('#searchUser').val())) + "#" +
					($('#box2View').val() == null  ? "" : $('#box2View').val()) + "#" +
					$('#startDatePicker').val() + "#" + 
					$('#endDatePicker').val() + "#" + "END";
	//alert(userdata);
	$("#auditsTable").jqGrid("GridUnload");
	start(userdata, 1, "logTime", "desc");			
	
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
$('#endDatePicker').datetimepicker('option', 'minDate', '');
$('#startDatePicker').datetimepicker('option', 'minDate', '');
$('#endDatePicker').datetimepicker('option', 'maxDate', '');

</script>
