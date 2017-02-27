<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/services/org/firmwareupgradeschedule/loadFirmwareUpgradeScheduleList" var="loadFirmwareUpgradeScheduleList" scope="request" />
<spring:url value="/services/org/imageupgrade/deleteFirmwareImage/" var="deleteFirmwareUpgradeScheduleUrl" scope="request" />
<spring:url value="/services/org/imageupgrade/deactivateFirmwareImage/" var="deactivateFirmwareUpgradeScheduleUrl" scope="request" />
<spring:url value="/services/org/imageupgrade/activateFirmwareImage/" var="activateFirmwareUpgradeScheduleUrl" scope="request" />
<spring:url value="/services/org/imageupgrade/checkDuplicateActiveFirmwareScheduleByModelNo/" var="checkDuplicateActiveFirmwareScheduleByModelNoUrl" scope="request" />

<script type="text/javascript">
	var PAGE = "${page}";
	var MAX_ROW_NUM = 99999;

	//Constants
	COLOR_FAILURE = "red";
	COLOR_SUCCESS = "green";
	COLOR_DEFAULT = "black";
	COLOR_BLACK = "black";
	var IMAGE_FILESIZE_LIMIT = "";

	$(document).ready(function() {
		
		var uploadStatus = "${uploadStatus}";
		
		IMAGE_FILESIZE_LIMIT = "${ImageFileSizeLimit}";
			
		if(uploadStatus != null && uploadStatus == "true"){
			displayDeviceImageUploadMessage("${message}", COLOR_SUCCESS);
		}
		
		if(uploadStatus != null && uploadStatus == "false"){
			displayDeviceImageUploadMessage("${message}", COLOR_FAILURE);
		}
		
		startFirmwareUpgradeScheduleTable(1,"addedTime", "desc");
		$("#firmwareUpgradeScheduleTable").setGridWidth($(window).width() - 25);
	});
		
	//function for pagination
	function startFirmwareUpgradeScheduleTable(pageNum, orderBy, orderWay) {
			jQuery("#firmwareUpgradeScheduleTable").jqGrid({
				url: "${loadFirmwareUpgradeScheduleList}?ts="+new Date().getTime(),
				mtype: "POST",
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
				colNames: ["id", "Image Name","Device Type","Model No","Version","Active","Description","Added Time","Action"],
			       colModel: [
			       { name:'id', index:'id', hidden: true},
			       { name: 'fileName', index: 'fileName',sorttype:'string',width:'15%' },
			       { name: 'deviceType', index: 'deviceType',sorttype:'string',width:'15%' },
			       { name: 'modelNo', index: 'modelNo',sorttype:'string',width:'15%' },
			       { name: 'version', index: 'modelNo',sorttype:'string',width:'15%' },
			       { name: 'active', index: 'active',sorttype:'string',width:'15%' },
			       { name: 'description', index: 'description',sorttype:'string',width:'15%' },
			       { name: "addedTime", index: 'addedTime', sorttype:'string',width:'15%' },
			       { name: "action", index: 'action',sortable:false,width:'12%', align: "left",formatter: viewFirmwareUpgradeScheduleActionFormatter}],
			       
			   	jsonReader: { 
					root:"firmwareUpgradeSchedules", 
			        page:"page", 
			        total:"total", 
			        records:"records", 
			        repeatitems:false,
			        id : "id"
			   	},
			   	pager: '#firmwareUpgradeSchedulePagingDiv',
			   	page: pageNum,
			   	sortorder: orderWay,
			   	sortname: orderBy,
			    hidegrid: false,
			    viewrecords: true,
			   	loadui: "block",
			   	toolbar: [false,"top"],
			   	onSortCol: function(index, iCol, sortOrder) {
			   	},
			   	loadComplete: function(data) {
			   		if (data != null){
			   			if (data.firmwareUpgradeSchedules != undefined) {
					   		if (data.firmwareUpgradeSchedules.length == undefined) {
					   			jQuery("#firmwareUpgradeScheduleTable").jqGrid('addRowData', 0, data.firmwareUpgradeSchedules);
					   		}
					   	}
			   		}
			   		
			   		ModifyFirmwareUpgradeScheduleGridDefaultStyles();
			   		
			   	}
	
			});
			
			jQuery("#firmwareUpgradeScheduleTable").jqGrid('navGrid',"#firmwareUpgradeSchedulePagingDiv",
											{edit:false,add:false,del:false,search:false}, 
											{}, 
											{}, 
											{}, 
											{},
											{});
			
			forceFitFirmwareUpgradeScheduleTableWidth();
		}
	
	function viewFirmwareUpgradeScheduleActionFormatter(cellvalue, options, rowObject) {
		var firmwareUpgradeScheduleId = rowObject.id;
		var firmwareUpgradeScheduleFileName = rowObject.fileName;
		var firmwareUpgradeScheduleModelNo = rowObject.modelNo;
		var firmwareUpgradeScheduleActionFormatter = "";
		firmwareUpgradeScheduleActionFormatter += "<button onclick=\"javascript: onDeleteFirmwareUpgradeSchedule('"+firmwareUpgradeScheduleId+"','"+firmwareUpgradeScheduleFileName+"');\">Delete</button>";
		if(rowObject.deviceType == "Fixture"){
			if(rowObject.active == "true"){
				firmwareUpgradeScheduleActionFormatter += "&nbsp;<button onclick=\"javascript: onActivateDeactivateFirmwareUpgradeSchedule('"+firmwareUpgradeScheduleId+"','"+firmwareUpgradeScheduleFileName+"','"+firmwareUpgradeScheduleModelNo+"','false');\">Deactivate</button>";
			}else{
				firmwareUpgradeScheduleActionFormatter += "&nbsp;<button onclick=\"javascript: onActivateDeactivateFirmwareUpgradeSchedule('"+firmwareUpgradeScheduleId+"','"+firmwareUpgradeScheduleFileName+"','"+firmwareUpgradeScheduleModelNo+"','true');\">Activate</button>";
			}
		}
		return firmwareUpgradeScheduleActionFormatter;
	}
	
	function onDeleteFirmwareUpgradeSchedule(firmwareUpgradeScheduleId,firmwareUpgradeScheduleFileName){
		var proceed = confirm("Are you sure you want to delete the Image: "+firmwareUpgradeScheduleFileName+"?");
		if(proceed==true) {
			$.ajax({
				type: "POST",
				url: "${deleteFirmwareUpgradeScheduleUrl}"+firmwareUpgradeScheduleId+"?ts="+new Date().getTime(),
				dataType : "json",
				contentType : "application/xml; charset=utf-8",
				success: function(data){
					if(data.status == 1) {
						reloadFirmwareUpgradeScheduleTable();
					}
				}
			});
	 	}
	}
	
	function onActivateDeactivateFirmwareUpgradeSchedule(firmwareUpgradeScheduleId,firmwareUpgradeScheduleFileName,firmwareUpgradeScheduleModelNo,activate){
		var proceed;
		if (activate == "true"){
			proceed = confirm("Are you sure you want to activate the Image: "+firmwareUpgradeScheduleFileName+"?");
		}else{
			proceed = confirm("Are you sure you want to deactivate the Image: "+firmwareUpgradeScheduleFileName+"?");
		}
		
		if(proceed==true) {
			if(activate == "false"){
				$.ajax({
					type: "POST",
					url: "${deactivateFirmwareUpgradeScheduleUrl}"+firmwareUpgradeScheduleFileName+"/Fixture/"+firmwareUpgradeScheduleModelNo+"?ts="+new Date().getTime(),
					dataType : "json",
					contentType : "application/xml; charset=utf-8",
					success: function(data){
						if(data.status == 0) {
							reloadFirmwareUpgradeScheduleTable();
						}
					}
				});
			}
			else{
				
				$.ajax({
					type: "POST",
					url: "${checkDuplicateActiveFirmwareScheduleByModelNoUrl}"+"Fixture/"+firmwareUpgradeScheduleModelNo+"?ts="+new Date().getTime(),
					dataType : "json",
					contentType : "application/xml; charset=utf-8",
					success: function(data){
						if(data.status == 0) {
							$.ajax({
								type: "POST",
								url: "${activateFirmwareUpgradeScheduleUrl}"+firmwareUpgradeScheduleFileName+"/Fixture/"+firmwareUpgradeScheduleModelNo+"?ts="+new Date().getTime(),
								dataType : "json",
								contentType : "application/xml; charset=utf-8",
								success: function(data){
									if(data.status == 0) {
										reloadFirmwareUpgradeScheduleTable();
									}
								}
							});
						}else{
							alert("An image with the same model No is already active.Please deactivate it before activating this image.");
						}
					}
				});
			}
		}
	}
	
	function reloadFirmwareUpgradeScheduleTable(){
		$('#firmwareUpgradeScheduleTable').trigger( 'reloadGrid' );
	}
	
	function forceFitFirmwareUpgradeScheduleTableWidth(){
		var jgrid = jQuery("#firmwareUpgradeScheduleTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#firmwareUpgradeScheduleDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#firmwareUpgradeSchedulePagingDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 30);
		
		$("#firmwareUpgradeScheduleTable").setGridWidth($(window).width() - 25);
	}
	
	function ModifyFirmwareUpgradeScheduleGridDefaultStyles() {  
		   $('#' + "firmwareUpgradeScheduleTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "firmwareUpgradeScheduleTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "firmwareUpgradeScheduleTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
		   $("#firmwareUpgradeScheduleTable").setGridWidth($(window).width()-25);	
	}

	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#firmwareUpgradeScheduleTable").setGridWidth($(window).width()-20);
	}).trigger('resize');
	
	function checkDeviceImagesize(obj) { 
		
		displayDeviceImageUploadMessage("", COLOR_DEFAULT);
		
		if(obj.files[0].size > IMAGE_FILESIZE_LIMIT) {
			$('#imgName').val('');
			var ImageFileSizeLimitInMB = Math.round(IMAGE_FILESIZE_LIMIT / (1024*1024) );
			displayDeviceImageUploadMessage("Upload file size should be less than "+ImageFileSizeLimitInMB+" MB.", COLOR_FAILURE);
		}
    }
	
	function displayDeviceImageUploadMessage(Message, Color) {
		$("#device_image_upload_message").html(Message);
		$("#device_image_upload_message").css("color", Color);
	}
	
	function clearDeviceImageUploadMessage(Message, Color) {
		displayDeviceImageUploadMessage("", COLOR_DEFAULT);
	}
	
	
	function validateDeviceImageUploadForm() {
		var imageName = $('#imgName').val().toLowerCase();
		if(imageName.indexOf("\\") > -1)
		{
			var imagename_array = imageName.split("\\");
			imageName = imagename_array[imagename_array.length - 1];
		}
		var imageNameArray = imageName.split(".");
		var fileName = imageNameArray[0];
		var fileExtension = imageNameArray[imageNameArray.length - 1];
		var fileNameArray = fileName.split("_");
		var version = fileNameArray[0];
		
		$("#fileName").val(imageName);
		if(imageName == ""){
			displayDeviceImageUploadMessage("<spring:message code='imageUpgrade.message.validation.emptyFileUpload'/>", COLOR_FAILURE);
			return false;
		}
		
		if(fileExtension != "tar"){
			alert('enLighted application image with only \'.tar\' extension is allowed.\nPlease check the selected filename.');
			return false;
		}
		
		displayDeviceImageUploadMessage("<spring:message code='imageUpgrade.message.uploadFileWaiting'/>", COLOR_BLACK);
		
	}
	
	function IsNumeric(sText)
	{
		var ValidChars = "0123456789";
		var IsNumber=true;
		var Char;
		for (i = 0; i < sText.length && IsNumber == true; i++){ 
			Char = sText.charAt(i); 
			if (ValidChars.indexOf(Char) == -1){
				IsNumber = false;
			}
		}
		return IsNumber;
	}
	
	
</script>
<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}
</style>



<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="firmwareUpgradeScheduleDiv" style="padding: 5px 5px 5px 5px">
		<div style="font-weight:bold;">Upload Image Package File</div>
		<div style="min-height:5px"></div>
		<form action="saveNewDeviceImage.ems" id="firmwareupgrade-register" name="firmwareUpgrade" method="post" enctype="multipart/form-data" onsubmit="javascript: return validateDeviceImageUploadForm();">
			<input type="file" name="upload" id="imgName" onChange="checkDeviceImagesize(this);" />
			<input type="hidden" name="fileName" id="fileName" />
			<input type="submit" name="submit" class="button" value="<spring:message code='imageUpgrade.label.upload'/>" />
		</form>	
		<div style="min-height:5px"></div>
		<div class="messageDiv" id="device_image_upload_message"></div>
    </div>
	<div style="padding: 0px 5px;">
		<table id="firmwareUpgradeScheduleTable"></table>
		<div id="firmwareUpgradeSchedulePagingDiv"></div>
	</div>
 </div>