<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/switch/op/dim/switch/" var="dimSwitchUrl" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Assign Area</title>

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

<style>
/* 	table#sws-wrapper-table td {padding: 0 20px} */
	td#sws-form-container div.fieldLabel{float:left; width:35%; font-weight:bold;}
 	td#sws-form-container div.fieldValue{float:left; width:65%;}
	#sws-message-div {font-weight:bold; float: left;}
	 	
 	#sws-wrapper-table div.sliderWrapper{margin-top: 10px;}
 	#sws-wrapper-table div.sliderWrapper div{display:inline;}
 	#sws-wrapper-table div.sliderDiv div{border: thin solid #AAAAAA;padding: 8px;}
 	
 	#sws-wrapper-table td.left-padding, #sws-wrapper-table td.right-padding{width:20px;}
 	#sws-wrapper-table div.sws-btn-wrapper {height:100%; border: 1px solid #CCCCCC; border-radius: 5px 5px 5px 5px; background-image: url("/ems/themes/default/images/gray_gradient_bg.png")}
 	#sws-wrapper-table div.sws-status-icon {border-radius: 8px; height:16px; width:16px; box-shadow:1px 1px 1px;}
 	#sws-wrapper-table div.icon-list {float:left;}
 	#sws-wrapper-table div.icon-btn {display: block; margin: 0 auto;}
 	#sws-wrapper-table div.sws-btn-text {font-weight: bold; text-align: center; text-shadow: 0 1px 1px #F6F6F6;}
 	#sws-wrapper-table div.sws-button {float: left; height: 100%; width: 33%;}
 	#sws-wrapper-table div.sws-btn-icon-wrapper {padding: 4px 0;}
 	#sws-wrapper-table div.sws-status-on {background-color: #00CC00;}
 	#sws-wrapper-table div.sws-status-off {background-color: #CCCCCC;}
 	#sws-wrapper-table div.sws-list-wrapper {border:1px solid #BBBBBB; border-top:none; padding: 8px; font-weight: bold;}
</style>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}	
	/*Fix for missing border of JQuery Slider panel */
 	#sws-wrapper-table .ui-widget-content {border: 1px solid #888888 !important;}
</style>

<script type="text/javascript">
var SWITCH_ID = "${switch.id}";

$(document).ready(function() {
	//Create Slider
	var defaultSliderValue = 50;
	$( "#sws_slider_fixture" ).slider({
		value: defaultSliderValue,
		min: 0,
		max: 100,
		step: 1,
		change: function( event, ui ) {
			updateSliderValueSpan(ui.value);
			setDimmerState(ui.value);
		}
	});
	updateSliderValueSpan(defaultSliderValue);
});

function setSliderValue(value){
	$("#sws_slider_fixture").slider("value", value);
}

function updateSliderValueSpan(value){
	$("#sws_slider_value").html(value+"%");
	$("#sws_slider_value").css("left", (value*3 - (value<10?20:(value==100?30:25))) + "px");
}

function setDimmerState(dimmerValue){
	$.ajax({
		type: "POST",
		url: "${dimSwitchUrl}"+ SWITCH_ID +"/"+ dimmerValue +"/60/"+"?ts="+new Date().getTime(),
		success: function(data){
			if(data != null){
				
			}
		},
		dataType:"json",
		contentType: "application/json; charset=utf-8",
	});
}

function setSceneToSwitch(sceneId){
	$.ajax({
		type: "POST",
		url: "${dimSwitchUrl}"+ SWITCH_ID +"/scene/"+ sceneId +"/102/60/"+"?ts="+new Date().getTime(),
		success: function(data){
			if(data != null){
				
			}
		},
		dataType:"json",
		contentType: "application/json; charset=utf-8",
	});
}

function applySceneToSwitch(sceneId){
	resetStatusIcon();
	setStatusIconAsOn(sceneId);
	
	if(sceneId=="auto"){
		setDimmerState(101);
	} else if(sceneId=="fullon"){
		setSliderValue(100);
	} else if(sceneId=="fulloff"){
		setSliderValue(0);
	} else{
		setSceneToSwitch(sceneId);
	}
}

function resetStatusIcon(){
	$("div.sws-status-icon").each(function(){
		$(this).removeClass("sws-status-on");
		$(this).addClass("sws-status-off");
	});
}

function setStatusIconAsOn(sceneId){
	$("div#"+sceneId + " div.sws-status-icon").addClass("sws-status-on");
	$("div#"+sceneId + " div.sws-status-icon").removeClass("sws-status-off");
}
</script>
</head>
<body id="sws-main-box">
<table id="sws-wrapper-table" height=100%>
	<tr>
		<td class="left-padding"></td>
		<td style="height: 80px; width:300px;">
			<div id="slider_marker">&nbsp;
				<span style="float:left;color:#AAA;">0%</span>
				<span style="float:left;color:#AAA;margin-left:40%;">50%</span>
				<span style="float:right;color:#AAA;">100%</span>
			</div>
			<div id="sws_slider_fixture"></div>
			<div id="sws_slider_info" style="padding-top: 7px;">&nbsp;
				<span id="sws_slider_value" style="position:relative; background-color: #DDDDDD; border: thin solid #AAAAAA; padding: 0 2px;">0%</span>
			</div>
		</td>
		<td class="right-padding"></td>
	</tr>
	
	<tr>
		<td class="left-padding"></td>
		<td style="height: 45px;">
			<div class="sws-btn-wrapper">
				<div id="auto" class="sws-button" style="border-right: 1px solid #CCCCCC;" onclick="applySceneToSwitch('auto');">
					<div class="sws-btn-icon-wrapper">
						<div class="sws-status-icon icon-btn sws-status-off"></div>
					</div>
					<div class="sws-btn-text">Auto</div>
				</div>
				
				<div id="fullon" class="sws-button" onclick="applySceneToSwitch('fullon');">
					<div class="sws-btn-icon-wrapper">
						<div class="sws-status-icon icon-btn sws-status-off"></div>
					</div>
					<div class="sws-btn-text">All On</div>
				</div>
				
				<div id="fulloff" class="sws-button" style="border-left: 1px solid #CCCCCC;" onclick="applySceneToSwitch('fulloff');">
					<div class="sws-btn-icon-wrapper">
						<div class="sws-status-icon icon-btn sws-status-off"></div>
					</div>
					<div class="sws-btn-text">All Off</div>
				</div>
				<br style="clear:both"/>
			</div>
		</td>
		<td class="right-padding"></td>
	</tr>
	
	<tr>
		<td class="left-padding"></td>
		<td height=auto valign="top">
			<div style="height: 5px;"></div>
			<div style="background-color: #999; border-radius: 6px 6px 0 0; color: white; font-weight: bold; padding: 8px;">Scenes</div>
				<c:forEach items='${scenes}' var='scene'>
					<c:if test="${scene.name != 'All On'}">
						<c:if test="${scene.name != 'All Off'}">
							<div id="${scene.id}" class="sws-list-wrapper" onclick="applySceneToSwitch('${scene.id}')">
								<div class="sws-status-icon icon-list sws-status-off"></div>
								&nbsp; ${scene.name}
							</div>
						</c:if>
					</c:if>
				</c:forEach>
		</td>
		<td class="right-padding"></td>
	</tr>
</table>

</body>
</html>