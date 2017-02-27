<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<spring:url value="/services/systemconfig/updatettlvalue" var="updatettlvalueUrl" scope="request" />
<script type="text/javascript">
var ttlval;
var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";

$(document).ready(function() {

var rs2val = "${ttlMap.rs2}";
var su2val = "${ttlMap.su2}";
$("#ttlrs2").val(rs2val);
$("#ttlsu2").val(su2val);
clearLabelMessage();
});

function validateSystemConfigForm(){
	clearLabelMessage();	
	
	//validation start
	var rs2val = $('#ttlrs2').val();	
	//test ttlval is empty
	if(rs2val == "")
	{
		displayLabelMessage("Enter a value between 1 and 15", COLOR_FAILURE);
		return;
	}	
	//test is numeric	
	if(isNaN(rs2val))
	{
		displayLabelMessage("TTL value for RS2 sensors should be an integer between 1 to 15", COLOR_FAILURE);
		return;
	}	
	else
	{	
		if(Number(rs2val) < 1 || Number(rs2val) > 15)
		{
		displayLabelMessage("TTL value for RS2 sensors should be greater than or equal to 1 and less than or equal to 15", COLOR_FAILURE);
		return;
		}			
		var array = rs2val.toString().split(".");
		//test decimal digits					
		if(rs2val.indexOf(".") != -1)
		if(array[1].length > 0)
		{
			//more than one decimal digit is not allowed
			displayLabelMessage("The value for RS2 sensors should be an integer between 1 and 15", COLOR_FAILURE);
			return;		
		}   	
		
	}
	
	var su2val = $('#ttlsu2').val();	
	//test ttlval is empty
	if(su2val == "")
	{
		displayLabelMessage("Enter a value between 1 and 3", COLOR_FAILURE);
		return;
	}	
	//test is numeric	
	if(isNaN(su2val))
	{
		displayLabelMessage("TTL value for Non RS2 sensors should be an integer between 1 to 3", COLOR_FAILURE);
		return;
	}	
	else
	{	
		if(Number(su2val) < 1 || Number(su2val) > 3)
		{
		displayLabelMessage("TTL value for Non RS2 sensor should be greater than or equal to 1 and less than or equal to 3", COLOR_FAILURE);
		return;
		}			
		var array = su2val.toString().split(".");
		//test decimal digits					
		if(su2val.indexOf(".") != -1)
		if(array[1].length > 0)
		{
			//more than one decimal digit is not allowed
			displayLabelMessage("The value for Non RS2 sensors should be an integer between 1 and 3", COLOR_FAILURE);
			return;		
		}   	
		
	}
	
	updatettlval()
}

function clearLabelMessage() {
	 $("#ttlerror").html("");
	 $("#ttlerror").css("color", COLOR_DEFAULT);
}

function displayLabelMessage(Message, Color) {
		$("#ttlerror").html(Message);
		$("#ttlerror").css("color", Color);
}
function reset() {
	$("#ttlrs2").val(15);
	$("#ttlsu2").val(3);

}

function updatettlval(){	
	var dataXML = '<ttlconfigurations>'
	    +'<ttlconfiguration><modelNo>rs2</modelNo><hopCount>'+$("#ttlrs2").val()+'</hopCount></ttlconfiguration>'
		+'<ttlconfiguration><modelNo>su2</modelNo><hopCount>'+$("#ttlsu2").val()+'</hopCount></ttlconfiguration>'
		+'</ttlconfigurations>';
$.ajax({
		type: 'POST',
		url: "${updatettlvalueUrl}",		
		contentType: "application/xml",
		data : dataXML,
		success: function(data){
			if(data == null){
				displayLabelMessage("TTL value not modified", COLOR_FAILURE);
			}else{
				displayLabelMessage("TTL value saved", COLOR_SUCCESS);
			}
		}		
		
	});
}

</script>

<div style="clear: both;"><span id="ttlerror"></span></div>
<div id="ttlForm" style="margin-left:50px;margin-top:20px; overflow: auto;">

			<div class="fieldWrapper" >
			<div class="fieldlabel" style="display:inline-block;"><b>TTL Value for RS2:</b></div>		
			<div class="fieldValue" style="display:inline-block;padding-left: 10px;"><input id="ttlrs2" /></div>
			</div>			
			<br>
			<div class="fieldWrapper" >
			<div class="fieldlabel" style="display:inline-block;"><b>TTL Value for Non RS2:</b></div>		
			<div class="fieldValue" style="display:inline-block;padding-left: 10px;"><input id="ttlsu2"/></div>
			<br style="clear:both;"/><br style="clear:both;"/>
			<button type="button" onclick="validateSystemConfigForm();">Save</button>&nbsp;&nbsp;
			<button type="button" onclick="reset();">Reset to Default</button>&nbsp;
			<br style="clear:both;"/><br style="clear:both;"/>
			</div>

</div>
