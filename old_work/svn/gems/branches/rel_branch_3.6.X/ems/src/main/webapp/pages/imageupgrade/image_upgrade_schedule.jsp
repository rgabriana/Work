<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:url value="/modules/PlotChartModule.swf" var="plotchartmodule"></spring:url>

<spring:url value="/scripts/jquery/jquery.datetime.picker.0.9.9.js" var="jquerydatetimepicker"></spring:url>
<script type="text/javascript" src="${jquerydatetimepicker}"></script>

<spring:url value="/services/org/imageupgrade/modifyFirmwareUpgradeSchedule/" var="modifyFirmwareUpgradeScheduleUrl" scope="request" />

<spring:url value="/services/org/imageupgrade/scheduleFirmwareUpgradeJob/" var="scheduleFirmwareUpgradeJobUrl" scope="request" />

<style>

.disableAddButton{
	cursor: not-allowed;
    border:1px solid #a8c1d5; -webkit-border-radius: 3px; -moz-border-radius: 3px;border-radius: 3px;font-size:12px;font-family:arial, helvetica, sans-serif;text-decoration:none; display:inline-block;text-shadow: -1px -1px 0 rgba(0,0,0,0.3);font-weight:normal; color: #FFFFFF;
	background-color: #CEDCE7; background-image: -webkit-gradient(linear, left top, left bottom, from(#CEDCE7), to(#596a72));
	background-image: -webkit-linear-gradient(top, #CEDCE7, #596a72);
	background-image: -moz-linear-gradient(top, #CEDCE7, #596a72);
	background-image: -ms-linear-gradient(top, #CEDCE7, #596a72);
	background-image: -o-linear-gradient(top, #CEDCE7, #596a72);
	background-image: linear-gradient(to bottom, #CEDCE7, #596a72);filter:progid:DXImageTransform.Microsoft.gradient(GradientType=0,startColorstr=#CEDCE7, endColorstr=#596a72);
}

</style>


<script type="text/javascript">

	
	var scheduleImageUpgradeDeviceType = "SCHEDULE_IMAGE_UPGRADE_GATEWAY";
	
	$(document).ready(function() {
		
		showImageUpgradeScheduleflash();
		$('#facilityTreeViewDiv').treenodeclick(function(){
			showImageUpgradeScheduleflash();
		});
		
		loadImageDeviceTypes();
		
		$('#deviceTypeSelectId').change(function() {
			
			var deviceType = $(this).val();
			
			if(deviceType == "Gateway"){
				scheduleImageUpgradeDeviceType = "SCHEDULE_IMAGE_UPGRADE_GATEWAY";
			}else if(deviceType == "Fixture"){
				scheduleImageUpgradeDeviceType = "SCHEDULE_IMAGE_UPGRADE_FIXTURE";
			}else{ //for deviceType == "WDS"
				scheduleImageUpgradeDeviceType = "SCHEDULE_IMAGE_UPGRADE_ERC";
			}
			
			getFloorPlanObj("schedule_image_upgrade_floorplan").reloadScheduleImageUpgradeReportTable(scheduleImageUpgradeDeviceType);
			
			$('#deviceModelSelectId').empty();
			
			<c:forEach items="${firmwareUpgradeSchedules}" var="firmwareUpgradeSchedule">
				if("${firmwareUpgradeSchedule.deviceType}" == deviceType){
					
					var exists = false;
					$('#deviceModelSelectId option').each(function(){
					    if (this.value == "${firmwareUpgradeSchedule.modelNo}") {
					        exists = true;
					        return false;
					    }
					});
					
					if(!exists){
						$('#deviceModelSelectId').append($('<option></option>').val("${firmwareUpgradeSchedule.modelNo}").html("${firmwareUpgradeSchedule.modelNo}"));
					}
				}
			</c:forEach>
			
			$('#deviceImageSelectId').empty();
			var deviceModelId = $('#deviceModelSelectId').val();
			<c:forEach items="${firmwareUpgradeSchedules}" var="firmwareUpgradeSchedule">
				if("${firmwareUpgradeSchedule.modelNo}" == deviceModelId){
					$('#deviceImageSelectId').append($('<option></option>').val("${firmwareUpgradeSchedule.id}").html("${firmwareUpgradeSchedule.fileName}"));
				}
			</c:forEach> 
			
			if(deviceType == "Fixture"){
				
				$("#imageUpgradeScheduleFilesDiv").css("display", "block");
				
				$("#addImageUpgradeScheduleFile").css("display", "block");
				
			}else{ // for gateway and ERC
				
				$("#imageUpgradeScheduleFilesDiv").css("display", "none");
				
				$("#addImageUpgradeScheduleFile").css("display", "none");
				
				$('#imageUpgradeScheduleFilesTable').jqGrid('clearGridData');
				
				mydata =  [];
			}
			
		});
		
		$('#deviceModelSelectId').change(function() {
			$('#deviceImageSelectId').empty();
			var deviceModelId = $(this).val();
			<c:forEach items="${firmwareUpgradeSchedules}" var="firmwareUpgradeSchedule">
				if("${firmwareUpgradeSchedule.modelNo}" == deviceModelId){
					$('#deviceImageSelectId').append($('<option></option>').val("${firmwareUpgradeSchedule.id}").html("${firmwareUpgradeSchedule.fileName}"));
				}
			</c:forEach> 
		});
		
		$("#startDatePicker").datetimepicker({
			ampm: false,
			timeFormat: 'hh:mm:ss',
			dateFormat: 'yy:mm:dd',
			showSecond: true,
			minDate: 0,
		    onClose: function(dateText, inst) {
		       
		    },
		    onSelect: function (selectedDateTime){
		        
		    }
		});
		
		$('input[name="startSchedule"]:radio' ).change(function(){
			if($(this).val() == "Now"){
				$("#startDatePicker").val("");
				$("#startDatePicker").prop('disabled', true);
				
				$("#iusDiv1").css('display', 'block');
				$("#iusDiv2").css('display', 'none');
				
				loadImageDeviceTypes();
				
				$("#imageUpgradeScheduleFilesDiv").css("display", "none");
				
				$("#iusDiv4").css("display", "none");
				
				$("#iusDiv5").css("display", "none");
				
				$("#addImageUpgradeScheduleFile").css("display", "none");
				
				$('#imageUpgradeScheduleFilesTable').jqGrid('clearGridData');
				
				mydata =  [];
				
				$("#activeImageUpgradeScheduleFilesDiv").css("display", "none");
				
				$("#retriesId").val("");
				$("#retryIntervalId").val("");
				$("#jobName").val("");
				
				scheduleImageUpgradeDeviceType = "SCHEDULE_IMAGE_UPGRADE_GATEWAY";
				
				getFloorPlanObj("schedule_image_upgrade_floorplan").reloadScheduleImageUpgradeReportTable(scheduleImageUpgradeDeviceType);
				
			}
			else{
				
				scheduleImageUpgradeDeviceType = "SCHEDULE_IMAGE_UPGRADE_FIXTURE";
				
				$("#startDatePicker").prop('disabled', false);
				
				$("#iusDiv1").css('display', 'none');
				$("#iusDiv2").css('display', 'block');
				
				$("#imageUpgradeScheduleFilesDiv").css("display", "none");
				
				$("#iusDiv4").css("display", "block");
				
				$("#iusDiv5").css("display", "block");
				
				$("#activeImageUpgradeScheduleFilesDiv").css("display", "block");
				
				$('#imageUpgradeScheduleFilesTable').jqGrid('clearGridData');
				
				mydata =  [];
				
				if("${isFixtureUpgradeActive}" == "true"){
					var startDateTime = "${startDatePicker}";
					$('#startDatePicker').val(startDateTime.substring(0, 19).replace(/-/g,":"));
					$("#retriesId").val("${retries}");
					$("#retryIntervalId").val("${retryInterval}");
					$("#jobName").val("${jobName}");
					if("${runtoComplete}" == "true"){
						$('#runtoComplete').attr('checked', true);
						$("#duration").val("");
						$("#duration").prop('disabled', true);
					}else{
						$('#runtoComplete').attr('checked', false);
						$("#duration").prop('disabled', false);
						$("#duration").val("${duration}");
					}
					if("${onreboot}" == "true"){
						$('#onreboot').attr('checked', true);
					}else{
						$('#onreboot').attr('checked', false);
					}
					if("${deviceSelection}" == "All"){
						$("input[name=deviceSelection][value='All']").prop('checked', true);
					}else if("${deviceSelection}" == "OnlySelected"){
						$("input[name=deviceSelection][value='OnlySelected']").prop('checked', true);
					}else if("${deviceSelection}" == "ExceptSelected"){
						$("input[name=deviceSelection][value='ExceptSelected']").prop('checked', true);
					}else{
						$("input[name=deviceSelection][value='OnlySelected']").prop('checked', true);
					}
					
					if("${deviceSelection}" != "All"){
						getFixtureListOfActiveFirmwareUpgradeSchedule();
						getFloorPlanObj("schedule_image_upgrade_floorplan").loadFixturesInReportTableByFixtureIds(fixtureArrayString);
					}else{
						getFloorPlanObj("schedule_image_upgrade_floorplan").reloadScheduleImageUpgradeReportTable(scheduleImageUpgradeDeviceType);
					}
				}
				else{
					$("#retriesId").val("");
					$("#retryIntervalId").val("");
					$("#jobName").val("");
					$('#runtoComplete').attr('checked', false);
					$("#duration").prop('disabled', false);
					$("#duration").val("");
					$('#onreboot').attr('checked', false);
					$("input[name=deviceSelection][value='OnlySelected']").prop('checked', true);
					getFloorPlanObj("schedule_image_upgrade_floorplan").reloadScheduleImageUpgradeReportTable(scheduleImageUpgradeDeviceType);
				}
			}
		});
		
		
		
		$('input[name="deviceSelection"]:radio' ).change(function(){
			if($(this).val() == "All"){
				getFloorPlanObj("schedule_image_upgrade_floorplan").reloadScheduleImageUpgradeReportTable(scheduleImageUpgradeDeviceType);
				//$("#image_Upgrade_Schedule_Fp_Div").css("display", "none");
			}else{
				//$("#image_Upgrade_Schedule_Fp_Div").css("display", "block");
			}
		});	
		
		$('#runtoComplete').change(function() {
			   if($(this).is(":checked")) {
					$("#duration").val("");
					$("#duration").prop('disabled', true);
			   }else{
					$("#duration").prop('disabled', false);
			   }
			   
		});
		
		forceFitImageUpgradeFloorPlanDiv();
		
		initImageUpgradeScheduleFilesTable();
		
		initActiveFixtureImageUpgradeScheduleFilesTable();
		
		addActiveFixtureImageUpgradeScheduleFiles();
			
	});
	
	function forceFitImageUpgradeFloorPlanDiv(){
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var upperDivHeight = $("#image_Upgrade_Schedule_Div1").height();
		var lowerDivHeight = $("#image_Upgrade_Schedule_Div2").height();
		$('#image_Upgrade_Schedule_Fp_Div').height(containerHeight - headerHeight - footerHeight - upperDivHeight - lowerDivHeight - 30);
	}
	
	function loadImageDeviceTypes(){
		
		$('#deviceTypeSelectId').empty();
		
		$('#deviceTypeSelectId').append($('<option></option>').val("Gateway").html("Gateway"));
		$('#deviceTypeSelectId').append($('<option></option>').val("Fixture").html("Fixture"));
		$('#deviceTypeSelectId').append($('<option></option>').val("WDS").html("ERC"));
		
		$('#deviceModelSelectId').empty();
		var deviceType = $('#deviceTypeSelectId').val();
		<c:forEach items="${firmwareUpgradeSchedules}" var="firmwareUpgradeSchedule">
			if("${firmwareUpgradeSchedule.deviceType}" == deviceType){
				
				var exists = false;
				$('#deviceModelSelectId option').each(function(){
				    if (this.value == "${firmwareUpgradeSchedule.modelNo}") {
				        exists = true;
				        return false;
				    }
				});
				
				if(!exists){
					$('#deviceModelSelectId').append($('<option></option>').val("${firmwareUpgradeSchedule.modelNo}").html("${firmwareUpgradeSchedule.modelNo}"));
				}
			}
		</c:forEach>
		
		$('#deviceImageSelectId').empty();
		var deviceModelId = $('#deviceModelSelectId').val();
		<c:forEach items="${firmwareUpgradeSchedules}" var="firmwareUpgradeSchedule">
			if("${firmwareUpgradeSchedule.modelNo}" == deviceModelId){
				$('#deviceImageSelectId').append($('<option></option>').val("${firmwareUpgradeSchedule.id}").html("${firmwareUpgradeSchedule.fileName}"));
			}
		</c:forEach> 
		
	}
		
	//common function to show floor plan for selected node
	var showImageUpgradeScheduleflash=function(){
		//variable coming from LHS tree
		removeImageUpgradeScheduleWheelEvent();
		loadImageUpgradeScheduleFloorPlan();	
	}
	
	function removeImageUpgradeScheduleWheelEvent() {
		if(window.addEventListener) {
	        var eventType = ($.browser.mozilla) ? "DOMMouseScroll" : "mousewheel";            
	        window.removeEventListener(eventType, handleWheel, false);
	    }
	}

	function handleWheel(event) {
		var app = document.getElementById("YOUR_APPLICATION");
	    var edelta = ($.browser.mozilla) ? -event.detail : event.wheelDelta/40;                                   
	    var o = {x: event.screenX, y: event.screenY, 
	        delta: edelta,
	        ctrlKey: event.ctrlKey, altKey: event.altKey, 
	        shiftKey: event.shiftKey}
		if (getFloorPlanObj("schedule_image_upgrade_floorplan") != null)
	    	getFloorPlanObj("schedule_image_upgrade_floorplan").handleWheel(o);
	}
	
	function loadImageUpgradeScheduleFloorPlan(){
		loadImageUpgradeScheduleFP();
	}
	var loadImageUpgradeScheduleFP = function() {
		try{
			if(window.addEventListener) {
	            var eventType = ($.browser.mozilla) ? "DOMMouseScroll" : "mousewheel";            
	            window.addEventListener(eventType, handleWheel, false);
	            getFloorPlanObj("schedule_image_upgrade_floorplan").onmousemove=null; // Handling poor mouse wheel behavior in Internet Explorer.
	        }
	        //alert(treenodetype + " " + treenodeid);
			getFloorPlanObj("schedule_image_upgrade_floorplan").changeLevel(treenodetype, treenodeid, 'SCHEDULE_IMAGE_UPGRADE','');
		}
		catch (ex){
			flash_fp(treenodetype, treenodeid);
		}
	}
	
	//**** Keep functions global or refresh tree functionality might break. *********//
	var getFloorPlanObj = function(objectName) {			
		if ($.browser.mozilla) {
			return document[objectName] 
		}
		return document.getElementById(objectName);
	}
	
	var flash_fp = function(nodetype, nodeid) {		
		var FP_data = "";
		
		var buildNumber = "";
		
		var versionString = "<ems:showAppVersion />";
		
		var indexNumber = versionString.lastIndexOf('.', (versionString.length)-1);
		
		if(indexNumber != -1 ){
			buildNumber = versionString.slice(indexNumber+1);
		}else{
			buildNumber = Math.floor(Math.random()*10000001);// For Development Version
		}
		
		var plotchartmoduleString = "${plotchartmodule}"+"?buildNumber="+buildNumber;
		
		if ($.browser.msie) {
			FP_data = "<object id='schedule_image_upgrade_floorplan' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='100%'>";
			FP_data +=  "<param name='src' value='"+plotchartmoduleString+"'/>";
			FP_data +=  "<param name='padding' value='0px'/>";
			FP_data +=  "<param name='wmode' value='opaque'/>";
			FP_data +=  "<param name='allowFullScreen' value='true'/>";
			FP_data +=  "<param name='flashVars' value='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=SCHEDULE_IMAGE_UPGRADE&modeid='/>";
			FP_data +=  "<embed id='schedule_image_upgrade_floorplan' name='schedule_image_upgrade_floorplan' src='"+plotchartmoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " padding='0px'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=SCHEDULE_IMAGE_UPGRADE&modeid='/>";
			FP_data +=  "</object>";
		} else {
			FP_data = "<embed id='schedule_image_upgrade_floorplan' name='schedule_image_upgrade_floorplan' src='"+plotchartmoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " padding='0px'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&mode=SCHEDULE_IMAGE_UPGRADE&modeid='/>";
		}
		
		var tabFP =document.getElementById("tab_image_fp");
		tabFP.innerHTML = FP_data; 
		// quick fix for the duplicate flash object
		$('div.alt').remove(); 
	}
	
	function validateImageUpgradeScheduleForm() {
		
		getFloorPlanObj("schedule_image_upgrade_floorplan").getSelectedDevices();
	}
	
	
	function setSelectedScheduleImageUpgradeDevices(selDevices, schedule_image_upgrade_devicetype) {
	 	// Devices
		selDeviceRows = [];
		
		var dataStr = "";
		if (selDevices != null && selDevices.length > 0) {
			selDeviceRows = selDevices;
			for (var count = 0; count < selDeviceRows.length; count++) {
				var device = selDeviceRows[count];
				dataStr += "{id: " + device.id + ", name: " + device.name + ", version: " + device.version + "},";
			}
		}
		
		var deviceType;
		
		if(schedule_image_upgrade_devicetype == "SCHEDULE_IMAGE_UPGRADE_GATEWAY"){
			deviceType = "Gateway";
		}else if(schedule_image_upgrade_devicetype == "SCHEDULE_IMAGE_UPGRADE_FIXTURE"){
			deviceType = "Fixture";
		}else{ //for schedule_image_upgrade_devicetype == "SCHEDULE_IMAGE_UPGRADE_ERC"
			deviceType = "WDS";
		}
		
		
		var deviceIds = [];
		
		var deviceNum = selDeviceRows.length;
		
		for(var i = 0 ; i < deviceNum; i++){
			var device = selDeviceRows[i]; 
			deviceIds.push(device.id);
		}
				
		var regExpStr = /^[0-9]+$/i;
	    
		if(regExpStr.test($("#retriesId").val()) == false) {
	    	alert("Please enter a number for No.Of Retries Field");
	    	return false;
	    }
	    
	    if(regExpStr.test($("#retryIntervalId").val()) == false) {
	    	alert("Please enter a number for Retry Interval Field");
	    	return false;
	    }
	    
	    var duration = 0;
	    
	    var runtoComplete = false;
	    
	    var reboot = false;
	    
	    var runDaily = false;
	    
	    var retries = $("#retriesId").val();
	    
	    var retryInterval = $("#retryIntervalId").val();
	    
	    var startTime = "";
		
	    var UrlParameters = "";
	    
	    var selDeviceMapString = "";
	    
	    var selDeviceIds = "";
	    
		if($("input[name='startSchedule']:checked").val() == "Now"){
	    	
			if(mydata == "" && deviceType == "Fixture"){
				alert("Please add one or more Fixture image file(s)");
				return false;
			}
			
			if(deviceIds.length == 0) {
				if(deviceType == "Gateway"){
					alert("<spring:message code='imageUpgrade.message.validation.emptyGateways'/>");
				}else if(deviceType == "Fixture"){
					alert("<spring:message code='imageUpgrade.message.validation.emptyFixtures'/>");
				}else{ // for deviceType = "WDS";
					alert("<spring:message code='imageUpgrade.message.validation.emptyWds'/>");
				}
				return false;
			}
			
			selDeviceMapString = "";
			
			if(deviceType == "Fixture")
			{
				selDeviceMapString = "{ 'seldevicemapkeyvalues' : [" ;
				
				for(var i=0;i<mydata.length;i++){
					selDeviceIds = "";
					for(var j=0;j<selDeviceRows.length;j++){
						if(mydata[i].modelName == selDeviceRows[j].modelNo){
							if(selDeviceIds != ""){
								selDeviceIds = selDeviceIds + ",";
							}
							selDeviceIds = selDeviceIds + selDeviceRows[j].id;
						}
					}
					selDeviceMapString = selDeviceMapString + "{"+ "'modelNo':'" + mydata[i].modelName + "'," + "'deviceIds':'" + selDeviceIds + "'}";
					if(i != mydata.length -1){
						selDeviceMapString = selDeviceMapString + ",";
		    		}
				}
				selDeviceMapString = selDeviceMapString + "]}";
				
			}
			else{
				selDeviceMapString = "{ 'seldevicemapkeyvalues' : [" ;
				selDeviceIds = "";
				for(var j=0;j<selDeviceRows.length;j++){
					if($("#deviceModelSelectId option:selected").text() == selDeviceRows[j].modelNo){
						if(selDeviceIds != ""){
							selDeviceIds = selDeviceIds + ",";
						}
						selDeviceIds = selDeviceIds + selDeviceRows[j].id;
					}
				}
				selDeviceMapString = selDeviceMapString + "{"+ "'modelNo':'" + $("#deviceModelSelectId option:selected").text() + "'," + "'deviceIds':'" + selDeviceIds + "'}";
				selDeviceMapString = selDeviceMapString + "]}";
			}
			
			startTime = "Now";
			
			runtoComplete = false;
			
			runDaily = false;
			
			reboot = false;
			
			duration = 0;
			
			var modelImageMapString = "";
		    
		    if(deviceType == "Fixture")
			{
		    	modelImageMapString = "{ 'modelimagemapkeyvalues' : [" ;
		    	for(var i=0;i<mydata.length;i++)
				{
		    		modelImageMapString = modelImageMapString + "{"+ "'modelname':'" + mydata[i].modelName + "'," + "'imagefile':'" + mydata[i].imageFile + "'}";
		    		if(i != mydata.length -1){
		    			modelImageMapString = modelImageMapString + ",";
		    		}
		    	}
		    	modelImageMapString = modelImageMapString + "]}";
			}else{
				modelImageMapString = "{ 'modelimagemapkeyvalues' : [" + "{"+ "'modelname':'" + $("#deviceModelSelectId option:selected").text() + "'," + "'imagefile':'" + $("#deviceImageSelectId  option:selected").text() + "'}" + "]}";
			}
	    	
		    UrlParameters = startTime + "/" + runtoComplete + "/" + duration + "/" + runDaily + "/" + reboot + "/" + retries + "/" + retryInterval + "/"+ deviceType + "?";
		    
		    UrlParameters = UrlParameters + "includeList="+selDeviceMapString + "&";
		    
		    if($("#jobName").val() != ""){
		    	UrlParameters = UrlParameters + "jobName=" + $("#jobName").val()+"&";
		    }
		    
		    UrlParameters = UrlParameters + "ts="+new Date().getTime();
			
			var confirmMsg = "<spring:message code='imageUpgrade.message.confirmImageUpgrade'/>";
			var result = confirm(confirmMsg);
			if (result == true) {
				//AJAX call to upgrade image
				$.ajax({
					type: 'POST',
					url: "${modifyFirmwareUpgradeScheduleUrl}"+UrlParameters,
					data: modelImageMapString,
					contentType: 'application/json',
					success: function(data){
						if(data!=null){	
							if(data.status == -1) {
								alert("Cannot proceed with image upgrade");
								reloadImagescheduleFrame();
							}else {
								alert("Image upgrade started");
								reloadImagescheduleFrame();
								//getFloorPlanObj("schedule_image_upgrade_floorplan").startImageUpgradeRefresh();
							}
						}
					}
				});
			}
			return result;
	    }
		else{
			
			if(activeFixtureImagedata == ""){
				alert("Please make one or more Fixture image file(s) as Active in the Upload Image Page");
				return false;
			}
			
			if($("#startDatePicker").val() == ""){
		    	alert("Please select a date in Start From Field");
			    return false;
		    }
			
			if( !$("#runtoComplete").is(":checked") && regExpStr.test($("#duration").val()) == false ) {
		    	alert("Please enter a number for Duration Field");
		    	return false;
		    }
		    
		    if( !$("#runtoComplete").is(":checked") && $("#duration").val() == 0 ){
	    		alert("Please enter a number greater than zero for Duration Field");
	    		return false;
	    	}
		    
		    if(deviceIds.length == 0 && $("input[name='deviceSelection']:checked").val() != "All") {
				if(deviceType == "Gateway"){
					alert("<spring:message code='imageUpgrade.message.validation.emptyGateways'/>");
				}else if(deviceType == "Fixture"){
					alert("<spring:message code='imageUpgrade.message.validation.emptyFixtures'/>");
				}else{ // for deviceType = "WDS";
					alert("<spring:message code='imageUpgrade.message.validation.emptyWds'/>");
				}
				//alert("No Devices selected");
				return false;
			}
		    
		    selDeviceMapString = "";
		    
		    if(activeFixtureImagedata)
			{
				selDeviceMapString = "{ 'seldevicemapkeyvalues' : [" ;
				var selDeviceIds ;
				for(var i=0;i<activeFixtureImagedata.length;i++){
					selDeviceIds = "";
					for(var j=0;j<selDeviceRows.length;j++){
						if(activeFixtureImagedata[i].modelName == selDeviceRows[j].modelNo){
							if(selDeviceIds != ""){
								selDeviceIds = selDeviceIds + ",";
							}
							selDeviceIds = selDeviceIds + selDeviceRows[j].id;
						}
					}
					selDeviceMapString = selDeviceMapString + "{"+ "'modelNo':'" + activeFixtureImagedata[i].modelName + "'," + "'deviceIds':'" + selDeviceIds + "'}";
					if(i != activeFixtureImagedata.length -1){
						selDeviceMapString = selDeviceMapString + ",";
		    		}
				}
				selDeviceMapString = selDeviceMapString + "]}";
			}
		    
		    startTime = encodeURIComponent($("#startDatePicker").val());
			
			if(activeFixtureImagedata)
			{
		    	modelImageMapString = "{ 'modelimagemapkeyvalues' : [" ;
		    	for(var i=0;i<activeFixtureImagedata.length;i++)
				{
		    		modelImageMapString = modelImageMapString + "{"+ "'modelname':'" + activeFixtureImagedata[i].modelName + "'," + "'imagefile':'" + activeFixtureImagedata[i].imageFile + "'}";
		    		if(i != activeFixtureImagedata.length -1){
		    			modelImageMapString = modelImageMapString + ",";
		    		}
		    	}
		    	modelImageMapString = modelImageMapString + "]}";
			}
			
			if(!$("#runtoComplete").is(':checked')){
		    	duration = $("#duration").val();
		    }else{
		    	duration = 0;
		    }
		    
		    if($("#runtoComplete").is(':checked')){
		    	runtoComplete = true;
		    }else{
		    	runtoComplete = false;
		    }
		    
		    if($("#onreboot").is(':checked')){
		    	reboot = true;
		    }else{
		    	reboot = false;
		    }
		    
		    runDaily = true;
		    
		    UrlParameters = startTime + "/" + runtoComplete + "/" + duration + "/" + runDaily + "/" + reboot + "/" + retries + "/" + retryInterval + "/"+ deviceType + "?";
		    
		    if($("input[name='deviceSelection']:checked").val() == "All"){
		    	UrlParameters = UrlParameters ;
		    }else if ($("input[name='deviceSelection']:checked").val() == "OnlySelected"){
		    	UrlParameters = UrlParameters + "includeList="+selDeviceMapString + "&";
		    }else if ($("input[name='deviceSelection']:checked").val() == "ExceptSelected"){
		    	UrlParameters = UrlParameters + "excludeList="+selDeviceMapString + "&";
		    }
		    
		    if($("#jobName").val() != ""){
		    	UrlParameters = UrlParameters + "jobName=" + $("#jobName").val()+"&";
		    }
		    
		    UrlParameters = UrlParameters + "ts="+new Date().getTime();
			
			var confirmMsg = "<spring:message code='imageUpgrade.message.confirmImageUpgrade'/>";
			var result = confirm(confirmMsg);
			if (result == true) {
				//AJAX call to upgrade image
				$.ajax({
					type: 'POST',
					url: "${modifyFirmwareUpgradeScheduleUrl}"+UrlParameters,
					data: modelImageMapString,
					contentType: 'application/json',
					success: function(data){
						if(data!=null){	
							if(data.status == -1) {
								alert("Cannot proceed with image upgrade");
								reloadImagescheduleFrame();
							}else {
								alert("Schedule image upgrade success");
								//getFloorPlanObj("schedule_image_upgrade_floorplan").startImageUpgradeRefresh();
								reloadImagescheduleFrame();
							}
						}
					}
				});
			}
			return result;
	    }
	}
	
	function resetImageUpgradeScheduleForm() {
		$("input[name=startSchedule][value='Now']").prop('checked', true);
		$("#startDatePicker").val("");
		$("#startDatePicker").prop('disabled', true);
		
		$("#iusDiv1").css('display', 'block');
		$("#iusDiv2").css('display', 'none');
		
		loadImageDeviceTypes();
		
		$("#imageUpgradeScheduleFilesDiv").css("display", "none");
		
		$("#iusDiv4").css("display", "none");
		
		$("#iusDiv5").css("display", "none");
		
		$("#addImageUpgradeScheduleFile").css("display", "none");
		
		$('#imageUpgradeScheduleFilesTable').jqGrid('clearGridData');
		
		mydata =  [];
		
		$("#activeImageUpgradeScheduleFilesDiv").css("display", "none");
		
		$("#retriesId").val("");
		$("#retryIntervalId").val("");
		$("#jobName").val("");
		
		scheduleImageUpgradeDeviceType = "SCHEDULE_IMAGE_UPGRADE_GATEWAY";
		
		getFloorPlanObj("schedule_image_upgrade_floorplan").reloadScheduleImageUpgradeReportTable(scheduleImageUpgradeDeviceType);
		
	}
	
	function initImageUpgradeScheduleFilesTable(){
		
		jQuery("#imageUpgradeScheduleFilesTable").jqGrid({
			datatype: "local",
			autowidth: true,
			scrollOffset: 0,
			hoverrows: false,
			forceFit: true,
		   	colNames:["Id",'Device Type', 'Model','Image','Action'],
		   	colModel:[
   	          	{ name:'id', index:'id', hidden: true},
		   		{name:'deviceType', index:'deviceType', sortable:false, width:"30%"},
		   		{name:'modelName', index:'modelName',sortable:false,width:"30%"},
		   		{name:'imageFile', index:'imageFile',sortable:false,width:"20%"},
		   		{name:'action', index:'action', align:"right", sortable:false, width:"20%"}
		   	],
		   	viewrecords: true,
		    sortorder: 'asc',
		    loadComplete: function() {
		    }    
		});
		
		$("#imageUpgradeScheduleFilesTable").setGridHeight(100);
		$("#imageUpgradeScheduleFilesTable").setGridWidth($("#image_Upgrade_Schedule_Div1").width() - 25);
		
	}
	
	var mydata =  [];
	
	var rowNumber = -1;
	
	function addFixtureImageUpgradeScheduleFiles(){
		
		if(mydata)
		{
			for(var i=0;i<mydata.length;i++)
			{
				if(mydata[i].modelName == $( "#deviceModelSelectId option:selected" ).text() ) {
					alert("Model already added.Please delete it from below table to add again with different Image");
					return false;
				}
			}
		}
		
		rowNumber = rowNumber + 1 ;
		var localData = new Object;
		localData.id =  rowNumber;
		localData.deviceType = $("#deviceTypeSelectId option:selected" ).text();
		localData.modelName =  $( "#deviceModelSelectId option:selected" ).text();
		localData.imageFile = $("#deviceImageSelectId  option:selected").text();
		localData.action = "";
		localData.action += "&nbsp;<button onclick=\'javascript: deleteImageUpgradeScheduleFiles("+localData.id+");\'>Delete</button>";
		
		mydata.push(localData);
		
		jQuery("#imageUpgradeScheduleFilesTable").addRowData(localData.id, localData);
		
	}
	
	function initActiveFixtureImageUpgradeScheduleFilesTable(){
		
		jQuery("#activeImageUpgradeScheduleFilesTable").jqGrid({
			datatype: "local",
			autowidth: true,
			scrollOffset: 0,
			hoverrows: false,
			forceFit: true,
		   	colNames:["Id",'Device Type', 'Model','Active Images'],
		   	colModel:[
   	          	{ name:'id', index:'id', hidden: true},
		   		{name:'deviceType', index:'deviceType', sortable:false, width:"30%"},
		   		{name:'modelName', index:'modelName',sortable:false,width:"30%"},
		   		{name:'imageFile', index:'imageFile',sortable:false,width:"20%"}
		   	],
		   	viewrecords: true,
		    sortorder: 'asc',
		    loadComplete: function() {
		    }    
		});
		
		$("#activeImageUpgradeScheduleFilesTable").setGridHeight(100);
		$("#activeImageUpgradeScheduleFilesTable").setGridWidth($("#image_Upgrade_Schedule_Div1").width() - 25);
		
	}
	
	var activeFixtureImagedata =  [];
	
	function addActiveFixtureImageUpgradeScheduleFiles(){
		
		<c:forEach items="${firmwareUpgradeSchedules}" var="firmwareUpgradeSchedule">
			if("${firmwareUpgradeSchedule.active}" == "true" && "${firmwareUpgradeSchedule.deviceType}" == "Fixture"){
				
				localData = new Object;
				localData.id =  "${firmwareUpgradeSchedule.id}";;
				localData.deviceType = "${firmwareUpgradeSchedule.deviceType}";
				localData.modelName =  "${firmwareUpgradeSchedule.modelNo}";
				localData.imageFile = "${firmwareUpgradeSchedule.fileName}";
				
				activeFixtureImagedata.push(localData);
				
				jQuery("#activeImageUpgradeScheduleFilesTable").addRowData(localData.id, localData);
			}
		</c:forEach>
		
	}
	
	function deleteImageUpgradeScheduleFiles(rowid){
		jQuery("#imageUpgradeScheduleFilesTable").delRowData(rowid);
		
		$.each(mydata, function(j){
		    if(mydata[j].id === rowid) {
		    	mydata.splice(j,1);
		        return false;
		    }
		});
	}
	
	var fixtureArrayString;
	
	function getFixtureListOfActiveFirmwareUpgradeSchedule(){
		 
		 fixtureArrayString = "";
		
		 if("${deviceSelection}" == "OnlySelected"){
			 
			<c:forEach items="${firmwareUpgradeSchedules}" var="firmwareUpgradeSchedule">
			if("${firmwareUpgradeSchedule.active}" == "true" && "${firmwareUpgradeSchedule.deviceType}" == "Fixture"){
				if("${firmwareUpgradeSchedule.includeList}" != "0"){
					if(fixtureArrayString != ""){
						fixtureArrayString =  fixtureArrayString + "," + "${firmwareUpgradeSchedule.includeList}";
					}else{
						fixtureArrayString =  "${firmwareUpgradeSchedule.includeList}";
					}
				}
			}
			</c:forEach>
			 
		 }
		 else if ("${deviceSelection}" == "ExceptSelected"){
			 
			<c:forEach items="${firmwareUpgradeSchedules}" var="firmwareUpgradeSchedule">
			if("${firmwareUpgradeSchedule.active}" == "true" && "${firmwareUpgradeSchedule.deviceType}" == "Fixture"){
				if("${firmwareUpgradeSchedule.excludeList}" != "0"){
					if(fixtureArrayString != ""){
						fixtureArrayString =  fixtureArrayString + "," + "${firmwareUpgradeSchedule.excludeList}";
					}else{
						fixtureArrayString =  "${firmwareUpgradeSchedule.excludeList}";
					}
				}
			}
			</c:forEach>
		}
		 
	}
	
	function reloadImagescheduleFrame(){
		var ifr = parent.document.getElementById('imagescheduleFrame');
		window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = ifr.src + new Date().getTime();
	}
	
</script>
<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}
</style>

<div style="width: 100%;height:100%">
	<div style="width: 100%;" id="image_Upgrade_Schedule_Div1">
		<div style="font-weight:bold;">
			<table cellspacing="10">
				<tr>
					<td>Firmware Upgrade</td>
					<td><input type="radio" name="startSchedule" value="Now" checked="checked"></input>Now</td>
					<td><input type="radio" name="startSchedule"  value="Start" ></input>Schedule</td>
					<td><input id="startDatePicker" name="startDatePicker" type="text" disabled="true"/></td>
				</tr>
			</table>
		</div>
		<div style="height:5px;"></div>
		<div id="iusDiv1" style="display:block">
		<table cellspacing="10">
			<tr>
				<td>Device Type :</td>
				<td><select id="deviceTypeSelectId" style="width: 150px;"></select></td>
				<td>Model :</td>
				<td><select id="deviceModelSelectId" style="width: 150px;"></select></td>
				<td>Image :</td>
				<td><select id="deviceImageSelectId" style="width: 250px;"></select></td>
				<td><input type="button" id="addImageUpgradeScheduleFile" value="Add" onclick='addFixtureImageUpgradeScheduleFiles();' style="display:none"></td>
			</tr>
		</table>
		</div>
		<div id="iusDiv2" style="display:none;font-weight:bold;">
			<table cellspacing="10">
				<tr>
					<td>Device Type : Fixture</td>
				</tr>
			</table>
		</div>
		<div id="imageUpgradeScheduleFilesDiv" style="display:none">
			<table id="imageUpgradeScheduleFilesTable"></table>
		</div>
		<div id="activeImageUpgradeScheduleFilesDiv" style="display:none">
			<table id="activeImageUpgradeScheduleFilesTable"></table>
		</div>
		<div id="iusDiv3" style="display:block">
		<table cellspacing="10" id="iusTable1"/>
			<tr>
				<td>Job Name:</td>
				<td><input id="jobName" type="text" style="width: 150px;"/></td>
				<td>No.of Retries :</td>
				<td><input type="text" name="retries" id="retriesId" style="width: 150px;"></input></td>
				<td>Retry Interval(sec) :</td>
				<td><input type="text" name="retryInterval"  id="retryIntervalId" style="width: 150px;"></input></td>
			</tr>
		</table>
		</div>
		<div id="iusDiv4" style="display:none;">
		<table cellspacing="10">
			<tr>
				<td><input type="checkbox" id="runtoComplete" name="runtoComplete" value="runtoComplete">Run to Completion</td>
				<td>Duration(Minutes):<input id="duration" type="text"/></td>
				<td><input type="checkbox" name="onreboot" id="onreboot" value="onreboot">On Reboot</td>
			</tr>
		</table>
		</div>
		<div id="iusDiv5" style="display:none;">
		<table cellspacing="5" style="border: 1px solid black;">
			<tr>
				<td><input type="radio" name="deviceSelection" value="All"></input>All Devices</td>
				<td><input type="radio" name="deviceSelection"  value="OnlySelected" checked="checked"></input>Inlcude Only Selected</td>
				<td><input type="radio" name="deviceSelection"  value="ExceptSelected">Exclude Only Selected</input></td>
			</tr>
		</table>
		</div>
	</div>
	<div style="width: 100%;" id="image_Upgrade_Schedule_Fp_Div"  style="display:block;">
		<div id="tab_image_fp" class="pnl_rht"></div>
	</div>
	<div style="width: 100%;" id="image_Upgrade_Schedule_Div2">
		<div>
			<table cellspacing="10">
				<tr>
					<td><input type="button" id="imageUpgradeScheduleSubmit" value="Submit" onclick='validateImageUpgradeScheduleForm();'></td>
					<td><input type="button" id="imageUpgradeScheduleReset" value="Reset" onclick='resetImageUpgradeScheduleForm();'></td>
				</tr>
			</table>
		</div>
	</div>
	<div style="height:5px;"></div>
</div>