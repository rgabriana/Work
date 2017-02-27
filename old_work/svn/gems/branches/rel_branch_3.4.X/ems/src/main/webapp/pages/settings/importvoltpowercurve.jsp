<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/ballastservice/op/importvoltpowermap/" var="uploadVoltPowerMap" scope="request" />
<spring:url value="/settings/uploadBallastCSVFile.ems" var="uploadBallastCSV" scope="request" />
<style>

#center {
  height : 95% !important;
}
	  th{text-align:left; border-right:0 none;}
	  th span.ui-jqgrid-resize{display:none !important;}
	 .label{font-weight: bold; font-size: 0.9em; color: #555555;}
	 .highlightGray{background-color: #EEEEEE;}
	 .fieldWrapper{padding-bottom:2px;padding-top: 5px;}
	 .fieldPadding{height:10px;}
	 .fieldlabel{float:left; height:20px; width: 60%; font-weight: bold;}
	 .fieldlabel2{float:left; height:20px; width: 20%; font-weight: bold;}
	 .fieldlabel1{float:left;padding-left:5px; height:20px; width: 10%; font-weight: bold;}
	 .fieldInput{float:left; height:20px; width:20%;}
	 .fieldInput1{float:left; height:20px; width:20%;}
	 .fieldInputCombo{float:left; height:23px; width: 30%;}
	 .fieldInputCombo1{float:left; height:23px; width: 15%;}
	 .fieldInputFile{float:left; height:23px; width:90%;}
	 .text {height:100%; width:100%;}
	 .readOnly {border:0px none;}
</style>

<script type="text/javascript">

var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
var ballastId;
$(document).ready(function(){
	clearLabelMessage();
	ballastId = "${ballastId}";
	$("#ballastid").val(ballastId);
});

function validateUpload()
{
	clearLabelMessage();
	var form = $('#upload-form');
	var file = $('input[type="file"]', form).val();
	var isValid = true;
	if ( file ) {
		var get_ext = file.split('.');
		get_ext = get_ext.reverse();
		var extension = get_ext[0];
		var arr = ["csv"];
	    var indexVal = $.inArray(extension, arr);
		if (indexVal!=-1){
		} else {
			isValid = false;
			displayLabelMessage("Invalid file type, Please select valid csv file.", COLOR_FAILURE);
		}
	}else{
		isValid = false;
		displayLabelMessage("Please select the csv file.", COLOR_FAILURE);
	}
	return isValid;
}
function displayLabelMessage(Message, Color) {
	$("#lamp_conf_message").html(Message);
	$("#lamp_conf_message").css("color", Color);
}

function clearLabelMessage(Message, Color) {
	displayLabelMessage("", COLOR_DEFAULT);
}
</script>
<form action="${uploadBallastCSV}" id="upload-form" method="post" enctype="multipart/form-data" onsubmit="return validateUpload()" >
<div class="outermostdiv" style="margin-left:0px;margin-top: 10px;">
	 <table id="bcd-main-box" style="width:95%; height:100%; border: 1px; padding-left: 5px;" >
	 <tr>
	 <td colspan="2">
	 <div id="lamp_conf_message" style="font-size: 14px; font-weight: bold; padding: 5px 0 0 0px; float: left;width: 100%"></div>
	 </td>
	 </tr>
	 <tr>
		  <td width="30%">
			<div class="fieldlabel">Input Voltage:</div>
		</td>
		<td width="50%">
			<div class="fieldInputCombo">
				<select id="voltage" name="voltage" class="text" >
					<option value="120">120</option>
					<option value="277">277</option>
				</select>
			</div>
		</td>
		<input type="hidden" name="ballastid" id="ballastid" />
	</tr>
	<tr>
		 <td width="30%">
				<div class="fieldWrapper">
					<div class="fieldlabel"><label for="fixturename">CSV file path:</label></div>
					<div class="fieldInput"></div>
					<br style="clear:both;"/>
				</div>
		 </td>
		 <td width="50%">	<div class="fieldWrapper">					 
						<input type="file" id="fileSelectObj" class="fieldInputFile"  name="upload"/>					
				</div>	
		 </td>
	 </tr>
	 <tr>
	 	<td width="30%"> <div class="fieldPadding"></div></td>
	 	<td></td>
	 </tr>
	 <tr>
		 <td width="30%">
		 </td>
		   <td width="50%">	
		  <input id="saveUserBtnValid" type="submit" value="Upload">
		 </td>
	 </tr>
	</table>
</div> 
</form>