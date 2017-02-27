<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<spring:url value="/services/systemconfig/edit" var="editSystemConfigUrl" scope="request" />
<spring:url value="/services/systemconfig/details" var="checkForSystemConfigNameUrl" scope="request" />
<style>

#systemConfigTable table {
	border: thin dotted #7e7e7e;
	padding: 10px;
}

#systemConfigTable th {
	text-align: right;
	vertical-align: top;
	padding-right: 10px;
}

#systemConfigTable td {
	vertical-align: top;
	padding-top: 2px;
}

#center {
  height : 95% !important;
}

</style>


<script type="text/javascript">

var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";

$(document).ready(function() {
	clearLabelMessage() ;
	
	
});


function displayLabelMessage(Message, Color) {
		$("#error").html(Message);
		$("#error").css("color", Color);
}
function clearLabelMessage() {
	 $("#error").html("");
	 $("#error").css("color", COLOR_DEFAULT);
}

function closeSystemConfigDialog(){
	$("#editSystemConfigDialog").dialog("close");
}

function validateSystemConfigForm(){
	clearLabelMessage();
	if ( $('#name').val().trim() == ''){
		displayLabelMessage("Name Field should not be empty", COLOR_FAILURE);
		return false;
	}
	if ( $('#value').val().trim() == ''){
		displayLabelMessage("Value Field should not be empty", COLOR_FAILURE);
		return false;
	}
	
	checkForSystemConfigName()
	
}

var systemConfigXML = "";

function checkForSystemConfigName(){
	
	$.ajax({
		type: 'POST',
		url: "${checkForSystemConfigNameUrl}"+"/"+$('#name').val().trim()+"?ts="+new Date().getTime(),
		success: function(data){
			if(data == null){
				displayLabelMessage("System Config with the below name doesn't exists", COLOR_FAILURE);
			}else{
				editSystemConfig(data.id);
			}
		},
		dataType:"json",
		contentType: "application/xml; charset=utf-8"
	});
	
}

function editSystemConfig(id){
	
	displayLabelMessage("",COLOR_DEFAULT),
	
	systemConfigXML = "<systemConfiguration>"+
	"<id>"+id+"</id>"+
	"<name>"+$('#name').val().trim()+"</name>"+
	"<value>"+$('#value').val().trim()+"</value>"+
	"</systemConfiguration>";
	
	$.ajax({
			data: systemConfigXML,
			type: "POST",
			url: "${editSystemConfigUrl}"+"?ts="+new Date().getTime(),
			success: function(data){
				displayLabelMessage("System Configuration value is successfully saved", COLOR_SUCCESS);
			},
			error: function(){
				displayLabelMessage("Error.System Configuration value is not saved", COLOR_FAILURE);
			},
			dataType:"json",
			contentType : "application/xml; charset=utf-8"
		});
	
}

</script>
<div style="clear: both;"><span id="error"></span></div>
<div style="margin:10px 0px 0px 20px;">
<div style="clear: both;"><span id="message">*In the Name Filed,Please enter name of the System Configuration value you want to edit </span></div>
<table id="systemConfigTable">
	<tr>
		<th >Name:</th>
		<td ><input id="name" name="name"/>
		</td>
	</tr>
	
	<tr>
		<th>Value:</th>
		<td ><input id="value" name="value"/>
		</td>
	</tr>
	<tr>
		<th><span></span></th>
		<td>
			<button type="button" onclick="validateSystemConfigForm();">
				Save
			</button>&nbsp;
			<input type="button" id="btnClose"
				value="<spring:message code="action.cancel" />" onclick="closeSystemConfigDialog()">
		</td>
	</tr>
</table>
</div>