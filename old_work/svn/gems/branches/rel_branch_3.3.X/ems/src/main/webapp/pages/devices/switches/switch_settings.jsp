<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/switch/op/" var="dimSwitchUrl" scope="request" />

<spring:url value="/services/org/plugload/op/" var="plugloadUrl" scope="request" />

<spring:url value="/services/org/switch/op/" var="applySwitchUrl" scope="request" />

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
// Both variables are used in different functions for firing webservice ajax calls. 
var SWITCH_ID = "${switch.id}";
var SCENE_ID ;
var iDimVal = 0;

$(document).ready(function() {		
	
	//Create Slider
	var defaultSliderValue = 50;
	$( "#sws_slider_fixture" ).slider({
		value: defaultSliderValue,
		min: 0,
		max: 100,
		step: 1,
		stop: function( event, ui ) {
		
			updateSliderValueSpan(ui.value);
			setDimmerState(ui.value);
			SCENE_ID=null ;
		},
		 change: function( event, ui ) {						
				updateSliderValueSpan(ui.value);					
			} 
			
		
	});
	
	$( "#sws_slider_fixture" ).bind( "slidestop", function(event, ui) {
		updateSliderValueSpan(ui.value);
		setDimmerState(ui.value);
	});
	
	updateSliderValueSpan(defaultSliderValue);
});

function resetStatusIcon(){
	$("div.sws-status-icon").each(function(){
		$(this).removeClass("sws-status-on");
		$(this).addClass("sws-status-off");
	});
}

function setStatusIconAsOn(obj){
	var selDiv = $(obj).closest('div');		
	var innerDiv = selDiv.find('div.sws-status-icon');
	
	innerDiv.removeClass("sws-status-off");
	innerDiv.addClass("sws-status-on");
}

function setSliderValue(value){	
	
	$("#sws_slider_fixture").slider("value", value);
	setDimmerState(value);
	SCENE_ID=null ;
	
	
}

function updateSliderValueSpan(value){
	$("#sws_slider_value").html(value+"%");
	$("#sws_slider_value").css("left", (value*3 - (value<10?20:(value==100?30:25))) + "px");
}



function setDimmerState(dimmerValue){
	var undef ;
	if(SCENE_ID==='auto'|| typeof SCENE_ID === "undefined"|| SCENE_ID === null)
	{		
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
	else
		{
		
		$.ajax({
			type: "POST",
			url: "${dimSwitchUrl}"+ SWITCH_ID +"/scene/"+ SCENE_ID +"/"+dimmerValue +"/60/"+"?ts="+new Date().getTime(),
			success: function(data){
				if(data != null){
					
				}
			},
			dataType:"json",
			contentType: "application/json; charset=utf-8",
		});
		
		}
		
}

function setSceneToSwitch(sceneId,sceneOrder){	
	//sceneOrder = defaultScenes.indexOf(sceneId);
	$.ajax({
		type: "POST",
		url: "${dimSwitchUrl}"+ SWITCH_ID +"/action/scene/"+ "argument/"+sceneOrder+""+"?ts="+new Date().getTime(),
		success: function(data){
			if(data != null){
				
			}
		},
		dataType:"json",
		contentType: "application/json; charset=utf-8",
	});
	SCENE_ID=null ;
}

function getPlugloadXML(id){
	return "<plugload><id>"+id+"</id></plugload>";
}

function operatePlugLoad(plugURL){
	<c:if test="${not empty plugloads}">
		var plugLoadXML = "<plugloads>"
		<c:forEach items="${plugloads}" var="pl" varStatus="rowCounter">
			plugLoadXML += getPlugloadXML("${pl.id}");
		</c:forEach>
		plugLoadXML += "</plugloads>";
			 $.ajax({
					type: "POST",
					url: plugURL,
					data: plugLoadXML,
					success: function(data){
						if(data != null){
							
						}
					},
					dataType:"xml",
					contentType: "application/xml; charset=utf-8",
			}); 
	</c:if>
}

function applySceneToSwitch(sceneName , sceneId, sceneOrder, obj){
	
	// This is a global variable and used at some places. do not delete.
	SCENE_ID = sceneId
	resetStatusIcon();	
	var n = 60;
	if(sceneName=="auto"){
		setStatusIconAsOn(obj);
		setDimmerState(101);
	} else if(sceneName=="fullon"){
		iDimVal = 100;
		setStatusIconAsOn(obj);
		setSliderValue(100);
		operatePlugLoad("${plugloadUrl}"+"turnOnOff/100/"+n);
	} else if(sceneName=="fulloff"){
		iDimVal = 0;
		setStatusIconAsOn(obj);
		setSliderValue(0);
		operatePlugLoad("${plugloadUrl}"+"turnOnOff/0/"+n);
	} else{
		
		setStatusIconAsOn(obj);
		setSceneToSwitch(SCENE_ID,sceneOrder);
		if(sceneOrder != 0){
			operatePlugLoad("${plugloadUrl}"+"turnOnOff/0/"+n);
		}else{
			operatePlugLoad("${plugloadUrl}"+"turnOnOff/100/"+n);
		}
	}
	
}

/* curl -v -X POST -b cookie.tmp -H"Content-Type: application/xml" -k https://localhost:8443/ems/services/org/switch/op/{switchid}/action/{action}/argument/{argument}
{
	switchid (database ID for the switch, to which the switch group and the fixtures are associated)
	action (auto | scene | dimup | dimdown), default is auto
	argument (auto {0} | scene {0-8} | dimup {10} | dimdown {10})
}
 */
//For 2.2 Sensors only
function applyDimToSwitch(dimVal)
{
	 //Makes the dim	 
	 $.ajax({
			type: "POST",
			url: "${dimSwitchUrl}"+ SWITCH_ID +"/"+ "action/dimdown/argument/" +dimVal +"?ts="+new Date().getTime(),
			success: function(data){
				if(data != null){
					
				}
			},
			dataType:"json",
			contentType: "application/json; charset=utf-8",
		});
	
} 
 
 //For 2.2 Sensors only
 function applyBrightenToSwitch(dimVal)
 {
 	 //Makes the brighten 	 
 	 $.ajax({
 			type: "POST",
 			url: "${dimSwitchUrl}"+ SWITCH_ID +"/"+ "action/dimup/argument/"+dimVal +"?ts="+new Date().getTime(),
 			success: function(data){
 				if(data != null){
 					
 				}
 			},
 			dataType:"json",
 			contentType: "application/json; charset=utf-8",
 		}); 	
 }
 
 //For 2.2 Sensors only
 function applyAuto()
 {	 	 
	 $.ajax({
			type: "POST",
			url: "${dimSwitchUrl}"+ SWITCH_ID +"/"+ "action/auto/argument" +"/101"+"?ts="+new Date().getTime(),
			success: function(data){
				if(data != null){
					
				}
			},
			dataType:"json",
			contentType: "application/json; charset=utf-8",
		}); 
	 
	 operatePlugLoad("${plugloadUrl}"+"mode/AUTO");
 }


	function setDimState() {
		
			//Call web service.
			applyDimToSwitch(iDimVal);
			//setDimmerState(iDimVal);
		
	}

	function setBrightenState() {

	    //Call web service
		applyBrightenToSwitch(iDimVal);
		//setDimmerState(iDimVal);

	}
</script>
</head>
<body id="sws-main-box">
<table>
<tr >
	
	<td style="padding-left:90px;">	
	<button id="autoButton" onclick="applyAuto();">Auto</button>
	</td>
	
	
	<td>
	<button id="dimButton" onclick="setDimState();">Dim</button>
	</td>
	 <td>
	 
	<button id="brightButton" onclick="setBrightenState();">Brighten</button>
	</td>
	
	<td class="right-padding"></td>
	
	</tr>
</table>
<table id="sws-wrapper-table" height=100%>

	<tr>
		<td class="left-padding"></td>
		<td style="height: 8px; width:300px;">			
		</td>
		<td class="right-padding"></td>
	</tr>
	<tr>
		<td class="left-padding"></td>
		<td height=auto valign="top">
			<div style="height: 5px;"></div>
			<div style="background-color: #999; border-radius: 6px 6px 0 0; color: white; font-weight: bold; padding: 8px;">Scenes</div>
				<c:forEach items='${scenes}' var='scene'>
				
							<div id="${scene.id}" class="sws-list-wrapper" onclick="applySceneToSwitch('${scene.name}','${scene.id}','${scene.sceneOrder}',this)">
								<div class="sws-status-icon icon-list sws-status-off"></div>
								&nbsp; ${scene.name}
							</div>
					
				</c:forEach>
		</td>
		<td class="right-padding"></td>
	</tr>
</table>

</body>
</html>