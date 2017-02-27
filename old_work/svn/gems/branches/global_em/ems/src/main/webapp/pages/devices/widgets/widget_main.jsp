<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/modules/PlotChartModule.swf" var="plotchartmodule"></spring:url>
<spring:url value="/services/org/switch/updateSwitchFixtures/" var="updateSwitchToFixtureUrl" scope="request" />
<style>
	.innerContainer{
		/* padding: 10px 20px; */
	}
	fieldset{padding: 10px;}
    legend{font-weight: bold; margin-left: 10px; padding: 0 2px;}
    
 	.button{padding: 0 10px;}
    div.fieldWrapper{clear:both; height:24px; width:40%; margin-bottom:10px;}
 	div.fieldLabel{float:left; height: 100%; width:30%; font-weight:bold;}
 	div.fieldValue{float:left; height: 100%; width:65%;}
 	.input{width:200px; height:95%;}
 	.messageDiv{display:inline; font-weight:bold; padding-left:10px;}
 	div.spacing-div{height:5px;}
 	.img-upg-progressbar{height:1em !important; border: 1px solid #DDDDDD !important; border-radius: 4px 4px 4px 4px !important;}
 	.img-upg-progressbar .ui-progressbar-value{ background-image: url(../themes/default/images/pbar-ani.gif) !important; border-radius: 4px 0 0 4px !important;} 	
 	div.property-container{width:70%;}
 	div.property-container div.property-wrapper{width:25%; float:left;}
 	div.property-container div.property-wrapper .input{width:95%; height:24px;}
 	div.property-container div.property-wrapper label{font-weight: bold;} 	
 	div.imageupg-tab-container {border:1px solid #ccc;}
 	div.tbldiv {margin:10px; padding-right:17px;}
 	div.image-upgrade-wrapper {background:#fff;}
 	div fieldset{padding:20px 10px;}
</style>

<script type="text/javascript"> 
	function removeWidgetWheelEvent() {
		if(window.addEventListener) {
	        var eventType = ($.browser.mozilla) ? "DOMMouseScroll" : "mousewheel";            
	        window.removeEventListener(eventType, handleWidgetWheel, false);
	    }
	}
	
	//common function to show floor plan for selected node
	var showflashWidget=function(){
		//variable coming from LHS tree
		removeWidgetWheelEvent();
		loadWidgetFP();	
	}

	function handleWidgetWheel(event) {
		var app = document.getElementById("YOUR_APPLICATION");
	    var edelta = ($.browser.mozilla) ? -event.detail : event.wheelDelta/40;                                   
	    var o = {x: event.screenX, y: event.screenY, 
	        delta: edelta,
	        ctrlKey: event.ctrlKey, altKey: event.altKey, 
	        shiftKey: event.shiftKey}
		if (getWidgetFloorPlanObj("widget_floorplan") != null)
	    	getWidgetFloorPlanObj("widget_floorplan").handleWheel(o);
	}
	
	//**** Keep functions global or refresh tree functionality might break. *********//
	var getWidgetWidgetFloorPlanObj = function(objectName) {			
		if ($.browser.mozilla) {
			return document[objectName] 
		}
		return document.getElementById(objectName);
	}
	
	var widget_fp = function(nodetype, nodeid) {		
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
			FP_data = "<object id='widget_floorplan' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='https://fpdownload.macromedia.com/get/flashplayer/current/swflash.cab' width='100%' height='100%'>";
			FP_data +=  "<param name='src' value='"+plotchartmoduleString+"'/>";
			FP_data +=  "<param name='padding' value='0px'/>";
			FP_data +=  "<param name='wmode' value='opaque'/>";
			FP_data +=  "<param name='allowFullScreen' value='true'/>";
			FP_data +=  "<param name='flashVars' value='orgType=" + nodetype + "&orgId=" + nodeid + "&switchId=" + "${switchId}" + "&fixtureVersion=" + "${fixtureVersion}" + "&mode=SWITCH_MODE&modeid='/>";
			FP_data +=  "<embed id='widget_floorplan' name='widget_floorplan' src='"+plotchartmoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " padding='0px'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&switchId=" + "${switchId}" + "&fixtureVersion=" + "${fixtureVersion}" + "&mode=SWITCH_MODE&modeid='/>";
			FP_data +=  "</object>";
		} else {
			FP_data = "<embed id='widget_floorplan' name='widget_floorplan' src='"+plotchartmoduleString+"' pluginspage='http://www.adobe.com/go/getflashplayer'";
			FP_data +=  " height='100%'";
			FP_data +=  " width='100%'";
			FP_data +=  " wmode='opaque'";
			FP_data +=  " padding='0px'";
			FP_data +=  " allowFullScreen='true'";
			FP_data +=  " flashvars='orgType=" + nodetype + "&orgId=" + nodeid + "&switchId=" + "${switchId}" + "&fixtureVersion=" + "${fixtureVersion}" + "&mode=SWITCH_MODE&modeid='/>";
		}
		
		var tabFP =document.getElementById("tab_widget_fp");
		tabFP.innerHTML = FP_data; 
		// quick fix for the duplicate flash object
		$('div.alt').remove(); 
	}
	
	var loadWidgetFP = function() {
		try{
			if(window.addEventListener) {
	            var eventType = ($.browser.mozilla) ? "DOMMouseScroll" : "mousewheel";            
	            window.addEventListener(eventType, handleWidgetWheel, false);
	            getWidgetFloorPlanObj("widget_floorplan").onmousemove=null; // Handling poor mouse wheel behavior in Internet Explorer.
	        }
			getWidgetFloorPlanObj("widget_floorplan").changeLevelForWidget(treenodetype, treenodeid, 'GRP_WIDGET_MODE', '', 0);
		}
		catch (ex){
			widget_fp(treenodetype, treenodeid);
		}
	}
	
	$(document).ready(function() {
		if(SELECTED_FIXTURES_TO_ASSIGN_SWITCH_GROUPS != undefined && SELECTED_FIXTURES_TO_ASSIGN_SWITCH_GROUPS.length > 0) {
			var dataXML = "";
			// Make service call to update the group with fixtures
			$.each(SELECTED_FIXTURES_TO_ASSIGN_SWITCH_GROUPS, function(){
				dataXML += "<fixture><id>"+ this.id +"</id></fixture>";
			 });
			dataXML = "<fixtures>"+dataXML+"</fixtures>";
		 	$.ajax({
		 		type: 'POST',
		 		url: "${updateSwitchToFixtureUrl}"+${switchId}+"?ts="+new Date().getTime(),
		 		data: dataXML,
		 		async: false,
		 		success: function(data){
				},
				error: function(){
				},
		 		dataType:"html",
		 		contentType: "application/xml; charset=utf-8",
		 	});
		}
		showflashWidget();
		$('#facilityDlgTreeViewDiv').treenodeclick(function(){
			showflashWidget();
		});
	});
	
</script>
<div class="outermostdiv">
<div id="tab_widget_fp" class="pnl_rht"></div>
</div>