<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<spring:url value="/scripts/jquery/jquery.ptTimeSelect.js" var="ptTimeSelect"></spring:url>
<script type="text/javascript" src="${ptTimeSelect}"></script>
<spring:url value="/themes/standard/css/jquery/jquery.ptTimeSelect.css" var="ptTimeSelectCss"></spring:url>
<link rel="stylesheet" type="text/css" href="${ptTimeSelectCss}" />
<spring:url value="/themes/default/images/time_picker.jpeg" var="timePicker" scope="request"/>

<spring:url value="/services/org/wds/getWdsReportData" var="getWdsReportDataUrl" scope="request" />
<spring:url value="/services/org/wds/exportwdspdfdata" var="exportWdsPdfReportUrl" />
<spring:url value="/services/org/wds/exportwdsdata" var="exportWdsCsvReportUrl" />
<spring:url value="/services/org/wds/saveErcBatteryReportScheduler" var="saveErcbatterySchedulerUrl" scope="request" />

<style type="text/css">

html {height:100% !important;}

fieldset {
    border:1px solid #999;
    border-radius:8px;
	padding: 5px 5px 5px 5px;
}

</style>

<script type="text/javascript">
	
	var postData;
			
	$(document).ready(function() {
		start('1####END', 1, "batteryLevel", "desc");
		$("#wdsReportsTable").setGridWidth($(window).width() - 25);
		
		if("${ercBatteryReportSchedulerEnable}" == "true"){
			$('#emailId').prop("disabled", false);
			$('#timeId').prop("disabled", false);
			$('#recurrenceScheduler1').prop("disabled", false);
			$('#recurrenceScheduler2').prop("disabled", false);
			$('#recurrenceScheduler3').prop("disabled", false);
			$('#recurrenceScheduler4').prop("disabled", false);
			$('#recurrenceScheduler5').prop("disabled", false);
			$('#recurrenceScheduler6').prop("disabled", false);
			$('#recurrenceScheduler7').prop("disabled", false);
			
			$('#enableSchedulerId').prop('checked', true);
			$('#emailId').val("${ercBatteryReportSchedulerEmail}");
			$('#timeId').val("${ercBatteryReportTime}");
			
			if ("${ercBatteryReportRecurrence}".indexOf("MON") >= 0){
				$('#recurrenceScheduler1').prop('checked', true);
			}else{
				$('#recurrenceScheduler1').prop('checked', false);
			}
			
			if ("${ercBatteryReportRecurrence}".indexOf("TUE") >= 0){
				$('#recurrenceScheduler2').prop('checked', true);
			}else{
				$('#recurrenceScheduler2').prop('checked', false);
			}
			
			if ("${ercBatteryReportRecurrence}".indexOf("WED") >= 0){
				$('#recurrenceScheduler3').prop('checked', true);
			}else{
				$('#recurrenceScheduler3').prop('checked', false);
			}
			
			if ("${ercBatteryReportRecurrence}".indexOf("THU") >= 0){
				$('#recurrenceScheduler4').prop('checked', true);
			}else{
				$('#recurrenceScheduler4').prop('checked', false);
			}
			
			if ("${ercBatteryReportRecurrence}".indexOf("FRI") >= 0){
				$('#recurrenceScheduler5').prop('checked', true);
			}else{
				$('#recurrenceScheduler5').prop('checked', false);
			}
			
			if ("${ercBatteryReportRecurrence}".indexOf("SAT") >= 0){
				$('#recurrenceScheduler6').prop('checked', true);
			}else{
				$('#recurrenceScheduler6').prop('checked', false);
			}
			
			if ("${ercBatteryReportRecurrence}".indexOf("SUN") >= 0){
				$('#recurrenceScheduler7').prop('checked', true);
			}else{
				$('#recurrenceScheduler7').prop('checked', false);
			}
			
		}else{
			$("#emailId").prop("disabled", true);
			$("#timeId").prop("disabled", true);
			$("#recurrenceScheduler1").prop("disabled", true);
			$("#recurrenceScheduler2").prop("disabled", true);
			$("#recurrenceScheduler3").prop("disabled", true);
			$("#recurrenceScheduler4").prop("disabled", true);
			$("#recurrenceScheduler5").prop("disabled", true);
			$("#recurrenceScheduler6").prop("disabled", true);
			$("#recurrenceScheduler7").prop("disabled", true);
			
			$('#enableSchedulerId').prop('checked', false);
			$('#emailId').val("");
			$('#timeId').val("");
			$('#recurrenceScheduler1').prop('checked', false);
			$('#recurrenceScheduler2').prop('checked', false);
			$('#recurrenceScheduler3').prop('checked', false);
			$('#recurrenceScheduler4').prop('checked', false);
			$('#recurrenceScheduler5').prop('checked', false);
			$('#recurrenceScheduler6').prop('checked', false);
			$('#recurrenceScheduler7').prop('checked', false);
		}
		
		$('#enableSchedulerId').change(function () {
			if ($(this).attr("checked")) {
				$('#emailId').prop("disabled", false);
				$('#timeId').prop("disabled", false);
				$('#recurrenceScheduler1').prop("disabled", false);
				$('#recurrenceScheduler2').prop("disabled", false);
				$('#recurrenceScheduler3').prop("disabled", false);
				$('#recurrenceScheduler4').prop("disabled", false);
				$('#recurrenceScheduler5').prop("disabled", false);
				$('#recurrenceScheduler6').prop("disabled", false);
				$('#recurrenceScheduler7').prop("disabled", false);
				
		    }else{
		    	$("#emailId").prop("disabled", true);
				$("#timeId").prop("disabled", true);
				$("#recurrenceScheduler1").prop("disabled", true);
				$("#recurrenceScheduler2").prop("disabled", true);
				$("#recurrenceScheduler3").prop("disabled", true);
				$("#recurrenceScheduler4").prop("disabled", true);
				$("#recurrenceScheduler5").prop("disabled", true);
				$("#recurrenceScheduler6").prop("disabled", true);
				$("#recurrenceScheduler7").prop("disabled", true);
				
				$('#emailId').val("");
				$('#timeId').val("");
				$('#recurrenceScheduler1').prop('checked', false);
				$('#recurrenceScheduler2').prop('checked', false);
				$('#recurrenceScheduler3').prop('checked', false);
				$('#recurrenceScheduler4').prop('checked', false);
				$('#recurrenceScheduler5').prop('checked', false);
				$('#recurrenceScheduler6').prop('checked', false);
				$('#recurrenceScheduler7').prop('checked', false);
		    }
		});
		
		$("#timeId").ptTimeSelect({zIndex: 10000,onFocusDisplay: false, popupImage: '<img src="${timePicker}" class="timePickerImageStyle" />' });
		
	});
	
	function start(inputdata, pageNum, orderBy, orderWay) {
		jQuery("#wdsReportsTable").jqGrid({
			url: "${getWdsReportDataUrl}"+"?ts="+new Date().getTime(),
			mtype: "POST",
			postData: {"userData": inputdata},
			datatype: "json",
			autoencode: true,
			hoverrows: false,
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			formatter: {
				 integer: {thousandsSeparator: ",", defaultValue: '0'},
			     number: {decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, defaultValue: '0.00'}
			},
			colNames: ["id", "ERC Name", "Location", "Battery Level","Last Reported Time"],
		       colModel: [
		       { name:'id', index:'id', hidden: true},
		       { name: 'name', index: 'name',sortable:true,sorttype:'string',width:'10%'},
		       { name: "location", index: 'location', sortable:true,width:'6%'},
		       { name: "batteryLevel", index: 'batteryLevel', sortable:true,sorttype:'string',width:'8%'},
		       { name: "captureAtStr", index: 'captureAtStr', sortable:true,sorttype:'string',width:'8%'}],
		       
		   	jsonReader: { 
				root:"wdses", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    pager: '#wdsReportsPagingDiv',
		   	page: pageNum,
		   	sortorder: orderWay,
		   	sortname: orderBy,
		   	hidegrid: false,
		    viewrecords: true,
		   	loadui: "block",
		   	toolbar: [false,"top"],
		   	onSortCol: function(index, iCol, sortOrder) {
		   		$('#orderWay').attr('value', sortOrder);
		   		$('#orderBy').attr('value', index);
		   	},
		   	loadComplete: function(data) {
		   		if (data != null){
		   			if (data.wdses != undefined) {
				   		if (data.wdses.length == undefined) {
				   			jQuery("#wdsReportsTable").jqGrid('addRowData', 0, data.wdses);
				   		}
				   	}
		   		}
		   		ModifyWdsReportsTableGridDefaultStyles();
		   		
		   	}

		});
		
		jQuery("#wdsReportsTable").jqGrid('navGrid',"#wdsReportsPagingDiv",
										{edit:false,add:false,del:false,search:false}, 
										{}, 
										{}, 
										{}, 
										{},
										{});
		
		forceFitReportsTableWidth();
	}
	
	function forceFitReportsTableWidth(){
		var jgrid = jQuery("#wdsReportsTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#wdsReportsPagingDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 10);
		
		$("#wdsReportsTable").setGridWidth($(window).width() - 25);
	}
	
	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		forceFitReportsTableWidth();
	}).trigger('resize');
	
	function ModifyWdsReportsTableGridDefaultStyles() {  
		   $('#' + "wdsReportsTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "wdsReportsTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "wdsReportsTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	}
	
	function exportWdsCsvReport(){
		$('#exportWdsCsvReportForm').submit();
	}
	
	function exportWdsPdfReport(){
		$('#exportWdsPdfReportForm').submit();
	}
	
	function IsEmail(email) {
		  var regex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
		  return regex.test(email);
	}
	
	function validateTime(inputField) { 
		//var isValid = /^([0-1]?[0-9]|2[0-4]):([0-5][0-9])(:[0-5][0-9])?$/.test(inputField); 
		var isValid = /^([0-1]?\d):([0-5]\d)\s(?:AM|PM)$/.test(inputField);
		//var isValid = /^([0-9]|1[0-2]):([0-5]\d)\s?(AM|PM)?$/.test(inputField);
		return isValid;
	}
	
	function saveErcbatteryScheduler(){
		
		var enableSchedulerString = "false";
		
		var emailString = "";
		
		var timeString = "";
		
		var recurrenceSchedulerString = "";
		
		if($('#enableSchedulerId').is(":checked")){
			
			enableSchedulerString = "true";
			
			if($('#emailId').val() == ""){
				alert("Email field cannot be empty");
				return false;
			}
			
			if(!IsEmail($('#emailId').val())){
				alert("Please enter a valid email address");
				return false;
			}
			
			if($('#timeId').val() == ""){
				alert("Time field cannot be empty");
				return false;
			}
			
			if(!validateTime($('#timeId').val())){
				alert("Please enter the time in HH:MM AM/PM format");
				return false;
			}
			
			if(!($('#recurrenceScheduler1').is(":checked") ||  $('#recurrenceScheduler2').is(":checked") || $('#recurrenceScheduler3').is(":checked") || 
					$('#recurrenceScheduler4').is(":checked") || $('#recurrenceScheduler5').is(":checked") || $('#recurrenceScheduler6').is(":checked") || $('#recurrenceScheduler7').is(":checked"))){
				alert("Please select atleast one recurrence checkbox");
				return false;
			}
			
			
			if($('#recurrenceScheduler1').is(":checked")){
				recurrenceSchedulerString = "MON";
			}
			
			if($('#recurrenceScheduler2').is(":checked")){
				if(recurrenceSchedulerString == ""){
					recurrenceSchedulerString = "TUE"
				}else{
					recurrenceSchedulerString = recurrenceSchedulerString + ",TUE"
				}
			}
			
			if($('#recurrenceScheduler3').is(":checked")){
				if(recurrenceSchedulerString == ""){
					recurrenceSchedulerString = "WED"
				}else{
					recurrenceSchedulerString = recurrenceSchedulerString + ",WED"
				}
			}
			
			if($('#recurrenceScheduler4').is(":checked")){
				if(recurrenceSchedulerString == ""){
					recurrenceSchedulerString = "THU"
				}else{
					recurrenceSchedulerString = recurrenceSchedulerString + ",THU"
				}
			}
			
			if($('#recurrenceScheduler5').is(":checked")){
				if(recurrenceSchedulerString == ""){
					recurrenceSchedulerString = "FRI"
				}else{
					recurrenceSchedulerString = recurrenceSchedulerString + ",FRI"
				}
			}
			
			if($('#recurrenceScheduler6').is(":checked")){
				if(recurrenceSchedulerString == ""){
					recurrenceSchedulerString = "SAT"
				}else{
					recurrenceSchedulerString = recurrenceSchedulerString + ",SAT"
				}
			}
			
			if($('#recurrenceScheduler7').is(":checked")){
				if(recurrenceSchedulerString == ""){
					recurrenceSchedulerString = "SUN"
				}else{
					recurrenceSchedulerString = recurrenceSchedulerString + ",SUN"
				}
			}
			
			emailString = $('#emailId').val();
			
			timeString = $('#timeId').val();
			
		}else{
			emailString = "default";
			
			timeString = "default";
			
			recurrenceSchedulerString = "default";
		}
		
		
		
		$.ajax({
	 		type: 'POST',
	 		url: "${saveErcbatterySchedulerUrl}/"+enableSchedulerString+"/"+emailString+"/"+timeString+"/"+recurrenceSchedulerString+"?ts="+new Date().getTime(),
	 		success: function(data){
				alert("Scheduler Options Successfully saved");
			},
			error: function(){
				alert("Error");
			},
	 		contentType: "application/xml; charset=utf-8"
	 	});
	}
	
</script>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="outerDiv" style="padding: 5px 5px 5px 5px;">
		<div><label style="padding: 5px 5px 5px 5px;font-weight:bold">ERC Battery Report</label></div>
		<div style="height:5px"></div>
		<fieldset>
		<legend><span>Scheduler</span></legend>
		<div>
			<table style="width: 100%;">
				<tr>
					<td align="left" style="padding: 5px 5px 5px 5px;font-weight:bold"><input type="checkbox" name="enableScheduler" id="enableSchedulerId">  Enable</td>
				</tr>
				<tr>
					<td align="left" style="padding: 5px 5px 5px 5px;font-weight:bold">Email: <input type="text" name="email" id="emailId" size="30">
									   Time: <input type="text" name="time" id="timeId" size="8">
					</td>
				 </tr>
				 <tr>
					 <table border="0" cellpadding="0" cellspacing="0" style="padding-top:5px;padding-bottom:15px">
						<tr>
							<td style="padding:0px 15px 0px 5px;font-weight:bold">Recurrence:</td>
							<td class="proftbltdlbl">Monday</td>
							<td class="proftbltd"><input type="checkbox" id="recurrenceScheduler1"></td>
							<td class="proftbltdlbl">Tuesday</td>
							<td class="proftbltd"><input type="checkbox" id="recurrenceScheduler2"></td>
							<td class="proftbltdlbl">Wednesday</td>
							<td class="proftbltd"><input type="checkbox" id="recurrenceScheduler3"></td>
							<td class="proftbltdlbl">Thursday</td>
							<td class="proftbltd"><input type="checkbox" id="recurrenceScheduler4"></td>
							<td class="proftbltdlbl">Friday</td>
							<td class="proftbltd"><input type="checkbox" id="recurrenceScheduler5"></td>
							<td class="proftbltdlbl">Saturday</td>
							<td class="proftbltd"><input type="checkbox" id="recurrenceScheduler6"></td>
							<td class="proftbltdlbl">Sunday</td>
							<td class="proftbltd"><input type="checkbox" id="recurrenceScheduler7"></td>
						</tr>
					 </table>
				 </tr>
				 <tr>
					<td align="left" style="padding: 5px 5px 5px 5px;"><button id="saveErcbatterySchedulerBtn" onclick="saveErcbatteryScheduler();">Save</button></td>
				 </tr>
			</table>
		</div>
		</fieldset>
		<div style="height:5px"></div>
		<table style="width: 100%;">
			<tr>
				<td align="right"><button id="exportWdsCsvReportBtn" onclick="exportWdsCsvReport();">Export Csv</button>
								  <button id="exportWdsPdfReportBtn" onclick="exportWdsPdfReport();">Export Pdf</button>
				</td>
			</tr>
		</table>
		<div style="height:5px"></div>
	</div>
	<div style="padding: 0px 5px;">
		<table id="wdsReportsTable"></table>
		<div id="wdsReportsPagingDiv"></div>
	</div>
 </div>
 <form id='exportWdsCsvReportForm' action="${exportWdsCsvReportUrl}" method='POST'></form>
 <form id="exportWdsPdfReportForm" action="${exportWdsPdfReportUrl}" METHOD="POST"></form>