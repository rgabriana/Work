<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<spring:url value="/services/systemconfig/updatefloorplansize" var="updateFloorPlanSizeUrl" scope="request" />
<script type="text/javascript">
var imageSize;
var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";

$(document).ready(function() {
imageSize = "${floorplansize}";
$("#floorplanimagetext").val(imageSize);
clearLabelMessage();
});

function validateSystemConfigForm(){
	clearLabelMessage();	
	
	//validation start
	var imageSize = $('#floorplanimagetext').val();	
	//test imageSize is empty
	if(imageSize == "")
	{
		displayLabelMessage("Floor plan image size empty", COLOR_FAILURE);
		return;
	}	
	//test is numeric	
	if(isNaN(imageSize))
	{
		displayLabelMessage("Floor plan image size should be numeric", COLOR_FAILURE);
		return;
	}	
	else
	{	
		if(Number(imageSize) < 1)
		{
		displayLabelMessage("Floor plan image size should be greater than or equal to 1", COLOR_FAILURE);
		return;
		}			
		var array = imageSize.toString().split(".");
		//test decimal digits					
		if(imageSize.indexOf(".") != -1)
		if(array[1].length > 1)
		{
			//more than one decimal digit is not allowed
			displayLabelMessage("More than one decimal digit is not allowed.", COLOR_FAILURE);
			return;		
		}   	
		
	}			
	//validation end
	updateFloorPlanImageSize()
}

function clearLabelMessage() {
	 $("#floorplanerror").html("");
	 $("#floorplanerror").css("color", COLOR_DEFAULT);
}

function displayLabelMessage(Message, Color) {
		$("#floorplanerror").html(Message);
		$("#floorplanerror").css("color", Color);
}

function updateFloorPlanImageSize(){
$.ajax({
		type: 'POST',
		url: "${updateFloorPlanSizeUrl}"+"/"+$.trim($('#floorplanimagetext').val())+"?ts="+new Date().getTime(),
		success: function(data){
			if(data == null){
				displayLabelMessage("Floor plan image size not modified", COLOR_FAILURE);
			}else{
				displayLabelMessage("Floor plan image size modified", COLOR_SUCCESS);
			}
		},
		dataType:"json",
		contentType: "application/xml; charset=utf-8"
	});
}

</script>

<div style="clear: both;"><span id="floorplanerror"></span></div>
<div id="mFloorForm" style="margin-left:50px;margin-top:20px; overflow: auto;">

			<div class="fieldWrapper" >
			<div class="fieldlabel" style="display:inline-block;"><b>Image size (MB):</b></div>		
			<div class="fieldValue" style="display:inline-block;padding-left: 10px;"><input id="floorplanimagetext" name="floorplanimagetext"/></div>
			<button type="button" onclick="validateSystemConfigForm();">
				Save
			</button>&nbsp;			
			<br style="clear:both;"/>
			</div>

</div>
