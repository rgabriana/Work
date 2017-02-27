<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/settings/updateLampConfiguration.ems" var="updateLampConfigurationURL" scope="request" />

<style>
	html body{margin:0px 0px 0px 0px !important; background: #ffffff !important; overflow: hidden !important;}

#bcd-main-box {width:100%; height:100%;  margin: 0px; !important}

	#bcd-main-box th{text-align:left; border-right:0 none;}
	#bcd-main-box th span.ui-jqgrid-resize{display:none !important;}
	.label{font-weight: bold; font-size: 0.9em; color: #555555;}
	.highlightGray{background-color: #EEEEEE;}

	form.bcd-main-box {font-size: 0.9em; padding: 2px 4px 0 4px;}
	
	form.bulb-conf-form {font-size: 0.9em; padding: 2px 4px 0 4px;}
	form.bulb-conf-form {font-size: 0.9em; padding: 2px 4px 0 4px;}
	#lamp-conf-form fieldset{border: medium; !important;}
	#bcd-main-box fieldset.form-column-left{float:left; width: 90%;}
	#bcd-main-box fieldset.form-column-right{float:left; width: 35%;height: 40%}
	#bcd-main-box fieldset.form-column-left1{float:left; width: 90%;}
	#bcd-main-box fieldset.form-column-right1{float:left; width: 46%;}
	
	#bcd-main-box .fieldWrapper{padding-bottom:2px;padding-top: 5px;}
	#bcd-main-box .fieldPadding{height:15px;}
	#bcd-main-box .fieldlabel{float:left; height:20px; width: 45%; font-weight: bold;}
	#bcd-main-box .fieldlabel2{float:left; height:20px; width: 45%; font-weight: bold;}
	#bcd-main-box .fieldlabel1{float:left;padding-left:5px; height:20px; width: 10%; font-weight: bold;}
	#bcd-main-box .fieldInput{float:left; height:20px; width:30%;}
	#bcd-main-box .fieldInput1{float:left; height:20px; width:20%;}
	#bcd-main-box .fieldInputCombo{float:left; height:23px; width: 30%;}
	#bcd-main-box .fieldInputCombo1{float:left; height:23px; width: 15%;}
	#bcd-main-box .text {height:100%; width:100%;}
	#bcd-main-box .readOnly {border:0px none;}
</style>

<script type="text/javascript">

var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
	$(document).ready(function(){
	
		$("#lamp-conf-form").validate({
			rules: {
				warmupTime: {
					digits: true,
					required: true,
					range: [1, 300]
				},
				stabilizationTime: {
					required: true,
					digits: true,
					range: [1, 20]
				},
				potentialDegradeThreshold: {
					required: true,
					range:[1,100],
					digits: true
				}
			},
			messages: {
				warmupTime: {
					required: '<spring:message code="error.above.field.required"/>',
					digits: 'Please enter a value in range of 1 to 300',
				},
				stabilizationTime: {
					required: '<spring:message code="error.above.field.required"/>',
					digits: 'Please enter a value in range of 1 to 20',
				},
				potentialDegradeThreshold: {
					required: '<spring:message code="error.above.field.required"/>',
					digits: 'Please enter a value in range of 1 to 100',
				}
			}
		});
		
	});
	
	function updateLampConfiguration(){
		var isValid = true;
		//ValidateForm()
		var form = $( "#lamp-conf-form" );
		form.validate();
		isValid = form.valid();
		if(isValid)
		{
			$.post(
				"${updateLampConfigurationURL}"+"?ts="+new Date().getTime(),
				$("#lamp-conf-form").serialize(),
				function(data){
					var response = eval("("+data+")");
					if(response.success==1){ //Success
						displayLabelMessage(response.message, COLOR_SUCCESS);
					} else { //Failure
						displayLabelMessage(response.message, COLOR_FAILURE);
					}
				}
			);
		}
	}
	function displayLabelMessage(Message, Color) {
		$("#lamp_conf_message").html(Message);
		$("#lamp_conf_message").css("color", Color);
	}
	
	function clearLabelMessage(Message, Color) {
		displayLabelMessage("", COLOR_DEFAULT);
	}
</script>
<div class="outermostdiv" style="margin-left:0px;">
	 <table id="bcd-main-box" style="width:100%; height:100%;">
	 <tr>
		 <td style="border-bottom: 1px">
		 	<div id="bulb-conf-form-div" class="tab-container">
		 	<div id="lamp_conf_message" style="font-size: 14px; font-weight: bold; padding: 5px 0 0 10px; float: left;"></div>
		 
		 	<form:form id="lamp-conf-form" method="post" commandName="lampCalibrationConf" >
		 		<form:hidden id="id" name="id" path="id" />
									<fieldset class="form-column-left">
									<legend>Power Usage Characterization:</legend>
										<div class="fieldWrapper">
											<label style="font-weight: bold; padding-right: 41px;" for="fixturename"  >Warmup Time (Sec):</label>
											<form:input id="warmupTime" name="warmupTime" path="warmupTime" size="5"   />
											<br style="clear:both;"/>
										</div>
										<div class="fieldPadding"></div>
										<div class="fieldWrapper">
											<label style="font-weight: bold; padding-right: 15px;" for="fixturename">Stabilization Time (Sec):</label>
											<form:input id="stabilizationTime" name="stabilizationTime" path="stabilizationTime" size="5" />
											<br style="clear:both;"/>
										</div>
									</fieldset>
									<br style="clear:both;"/>
										<br style="clear:both;"/>
									<fieldset class="form-column-left">
									<legend>Threshold:</legend>
									<div class="fieldWrapper">
											<label style="font-weight: bold;" for="ballasttype">Drop in Power Usage (%):</label>
											<form:input id="potentialDegradeThreshold" name="potentialDegradeThreshold" path="potentialDegradeThreshold" size="5" />
											<br style="clear:both;"/>
									</div>
									</fieldset>
									
							</form:form>
			</div>
		 </td>
	 </tr>
	 <tr style="height:20px;">
	 	<td  style="border:0px none;">
	 		<span  style="float:right; padding-top: 5px;padding-right: 10px;padding-bottom: 2px;">
		 		<button id="fxcd-update-btn" onclick="updateLampConfiguration();">Update</button>
<!-- 		 		<button id="fxcd-undo-btn">Schedule</button> -->
	 		</span>
	 	</td>
	</tr>
	</table>
</div> 
