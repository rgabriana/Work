<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/locatordevice/add/name/" var="addLocatorDeviceUrl" scope="request" />
<spring:url value="/services/org/locatordevice/update/name/" var="updateLocatorDeviceUrl" scope="request" />
<spring:url value="/services/org/locatordevice/details/" var="checkDuplicateLocatorDeviceUrl" scope="request" />


<style>

#ldTable table {
	border: thin dotted #7e7e7e;
	padding: 10px;
}

#ldTable th {
	text-align: right;
	vertical-align: top;
	padding-right: 10px;
}

#ldTable td {
	vertical-align: top;
	padding-top: 2px;
}

#center {
  height : 90% !important;
}
		
</style>


<div style="margin:10px 0px 0px 20px;">
<div id="locatorDevice-message-div"></div>
<form:form id="create_locator_device" commandName="locatordevice" method="post" 
	action=""  onsubmit="return false;">
	<form:hidden id="id" name="id" path="id"/>
	<form:hidden id="floorId" name="floorId" path="floorId"/>
	<form:hidden id="xaxis" name="xaxis" path="xaxis"/>
	<form:hidden id="yaxis" name="yaxis" path="yaxis"/>
	<table id="ldTable">
		
		<tr>
			<td class="fieldLabel">Name:</td>
			<td class="fieldValue"><form:input class="inputField" id="name" name="name"
					path="name" size="27"/></td>
		</tr>
		<c:if test="${action == 'create'}">
		<tr>
			<td class="fieldLabel">Type:</td>
			<td class="fieldValue">
				<form:select class="inputField" path="locatorDeviceType" id="locatorDeviceType" name="locatorDeviceType" >
					<form:options items="${locatorDeviceTypeList}" itemValue="name"
						itemLabel="name" />
				</form:select>
			</td>
		</tr>		
		</c:if>
		
		<tr>
			<td />
			<td><input id="saveLocatorDeviceBtn" type="button"
				value="<spring:message code="action.save" />" >&nbsp;
				<input type="button" id="btnClose"
				value="<spring:message code="action.cancel" />" >	
			</td>
		</tr>
	</table>
</form:form>
</div>

<script type="text/javascript">

var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";

var LOCATOR_DEVICE_XAXIS = "${xaxis}";
var LOCATOR_DEVICE_YAXIS = "${yaxis}";
var LOCATOR_DEVICE_PAGE = "${page}";

var ACTION = "${action}"

//Global variable for accessing current locator device id
var LOCATOR_DEVICE_ID = "${locatordevice.id}";

$(document).ready(function() {
	//add click handler
	$('#saveLocatorDeviceBtn').click(function(){startValidation();});
	$('#btnClose').click(function(){cancelValidation();});
	
});

function startValidation(){
	
	clearLocatorDeviceLabelInitialMessage();
	
	var locatorDeviceName = $("#name").val();
	
	if(locatorDeviceName == null || locatorDeviceName == ""){
		displayLocatorDeviceLabelMessage("Please enter a Device Name", COLOR_FAILURE);
		return;
	}
	
	checkForDuplicateLocatorDeviceName();
	
}

function addLocatorDevice(){
	
	var locatorDeviceName = encodeURIComponent($('#name').val());
	$.ajax({
		data: "",
		type: "POST",
		url: "${addLocatorDeviceUrl}"+locatorDeviceName+"/locatorDeviceType/"+$('#locatorDeviceType').val().replace(/ /g,"_")+"/floorId/"+$('#floorId').val()+"/xaxis/"+Math.ceil(LOCATOR_DEVICE_XAXIS)+"/yaxis/"+Math.ceil(LOCATOR_DEVICE_YAXIS),
		success: function(data){
			
			if(LOCATOR_DEVICE_PAGE == "FLOORPLAN"){
				parent.parent.getFloorPlanObj("floorplan").plotChartRefresh();
			}else if(LOCATOR_DEVICE_PAGE == "COMMISSION"){
				parent.getCommissionPlanObj("c_fx_floorplan").addLocatorDevice();
			}else{ // from devices page
				if(parent.parent.getFloorPlanObj("floorplan") != undefined){
					parent.parent.getFloorPlanObj("floorplan").plotChartRefresh();
				}
			}
			exitLocatorDeviceWindow();
		},
		error: function(){
			displayLocatorDeviceLabelMessage("Error.Device is not created", COLOR_FAILURE);
		},
		dataType:"json"
	});
}


function updateLocatorDevice(){
	$.ajax({
		data: "",
		type: "POST",
		url: "${updateLocatorDeviceUrl}"+$('#name').val()+"/id/"+$('#id').val(),
		success: function(data){
			if(LOCATOR_DEVICE_PAGE == "FLOORPLAN"){
				parent.parent.getFloorPlanObj("floorplan").plotChartRefresh();
			}else if(LOCATOR_DEVICE_PAGE == "COMMISSION"){
				parent.getCommissionPlanObj("c_fx_floorplan").updateLocatorDevice();
			}else{ // from devices page
				if(parent.parent.getFloorPlanObj("floorplan") != undefined){
					parent.parent.getFloorPlanObj("floorplan").plotChartRefresh();
				}
			}
			exitLocatorDeviceWindow();
		},
		error: function(){
			displayLocatorDeviceLabelMessage("Error.Device is not updated", COLOR_FAILURE);
		},
		dataType:"json"
	});
}

function checkForDuplicateLocatorDeviceName(){
	$.ajax({
		type: 'POST',
		url: "${checkDuplicateLocatorDeviceUrl}"+$('#floorId').val()+"/"+$('#name').val()+"?ts="+new Date().getTime(),
		data: "",
		success: function(data){
			if(data == null){
				if(ACTION == 'edit'){
					updateLocatorDevice();
				}else{
					addLocatorDevice();
				}
					
			}else{
				if(LOCATOR_DEVICE_ID != data.id){
					displayLocatorDeviceLabelMessage("Device with the name already exists", COLOR_FAILURE);
				}else{
					updateLocatorDevice();
				}
			}
		},
		dataType:"json",
		contentType: "application/json; charset=utf-8"
	});
	
}


function cancelValidation(){
	exitLocatorDeviceWindow();
}

function exitLocatorDeviceWindow(){
	$('#locatorDeviceFormDialog').dialog('close');
}

function displayLocatorDeviceLabelMessage(Message, Color) {
	$("#locatorDevice-message-div").html(Message);
	$("#locatorDevice-message-div").css("color", Color);
}

function clearLocatorDeviceLabelInitialMessage() {
	displayLocatorDeviceLabelMessage("", COLOR_DEFAULT);
}


</script>