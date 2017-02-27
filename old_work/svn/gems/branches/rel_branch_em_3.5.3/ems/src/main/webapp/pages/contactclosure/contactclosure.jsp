<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<spring:url value="/services/org/contactclosure/saveContactClosure" var="saveContactClosureUrl" scope="request" />
<spring:url value="/scripts/jquery/jquery.multiselect.js" var="jquerymultiselect"></spring:url>
<script type="text/javascript" src="${jquerymultiselect}"></script>

<spring:url value="/themes/standard/css/jquery/jquery.multiselect.css" var="multiselectCss"></spring:url>
<link rel="stylesheet" type="text/css" href="${multiselectCss}" />

<spring:url value="/services/org/contactclosure/discover" var="discoverContactClosureUrl" scope="request" />

<spring:url value="/services/org/contactclosure/getcontactclosure" var="getContactClosureUrl" scope="request" />
		
<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
	
	.ui-dialog-title{font-size: 1.4em; font-weight: bolder;}
	
	table#contact_closure_list td{overflow: hidden !important;}
	
	/* table#all_users_list {table-layout:auto !important;}*/

	.entable td{
		padding-left:5px;
		/* border:1px #252525 solid; */
		border: 1px solid #E5E5E5 !important;
		height:32px;
		text-overflow:ellipsis;
	}
	.outermostdiv{height:98%;}
			
</style>

<script type="text/javascript">
	var COLOR_SUCCESS = "green";
	var COLOR_DEFAULT = "black";
	var COLOR_ERROR = "red";
	
    $(document).ready(function() {
    	if("${contactClosureEnable}" == "true"){
    		
    		$('#discoverButtonId').css("display", "block");
    		
    		$('#contactClosureAction1').prop("disabled", false);
    		$('#contactClosureAction2').prop("disabled", false);
    		$('#contactClosureAction3').prop("disabled", false);
    		$('#contactClosureAction4').prop("disabled", false);
    		
    		$('#contactClosureDuration1').prop("disabled", false);
    		$('#contactClosureDuration2').prop("disabled", false);
    		$('#contactClosureDuration3').prop("disabled", false);
    		$('#contactClosureDuration4').prop("disabled", false);
    		    		
    		$('#enableContactClosureId').prop('checked', true);
    		
    		$("#contactClosureAction1").val(${contactClosureList[0].action});
        	$("#contactClosureAction2").val(${contactClosureList[1].action});
        	$("#contactClosureAction3").val(${contactClosureList[2].action});
        	$("#contactClosureAction4").val(${contactClosureList[3].action});
        	
        	if ($("#contactClosureAction1").val() == 1) {
        		$("#contactClosureDuration1").val(${contactClosureList[0].duration});
        		$('#contactClosureTdSubAction1').html("NA");
			}else if ($("#contactClosureAction1").val() == 2){
				var s1 = $("<select id=\"contactClosureSubAction1\" name=\"contactClosureSubAction1\" multiple=\"multiple\" />");
				$('#contactClosureTdSubAction1').html("");
				$('#contactClosureTdSubAction1').append(s1);
				<c:forEach items="${switchList}" var="switch">
		    	 	$('#contactClosureSubAction1').append($('<option></option>').val("${switch.id}").html("${switch.name}"));
		    	</c:forEach>
		    	
		    	$("#contactClosureSubAction1").multiselect();
		    	
		    	<c:if test="${contactClosureList[0].subAction != ''}">
	    	 		$("#contactClosureSubAction1").val("${contactClosureList[0].subAction}".split(","));
	    	 	</c:if>
	    	 	
	    	 	$("#contactClosureSubAction1").multiselect("refresh");
				
	    	 	$("#contactClosureDuration1").val(${contactClosureList[0].duration});
	    	}else{
	    		$("#contactClosureDuration1").val("");
				$('#contactClosureDuration1').prop("disabled", true);
				
				$('#contactClosureTdSubAction1').html("NA");
	    	}
        	
        	if ($("#contactClosureAction2").val() == 1) {
        		$("#contactClosureDuration2").val(${contactClosureList[1].duration});
        		$('#contactClosureTdSubAction2').html("NA");
			}else if ($("#contactClosureAction2").val() == 2){
				var s2 = $("<select id=\"contactClosureSubAction2\" name=\"contactClosureSubAction2\" multiple=\"multiple\" />");
				$('#contactClosureTdSubAction2').html("");
				$('#contactClosureTdSubAction2').append(s2);
				<c:forEach items="${switchList}" var="switch">
		    	 	$('#contactClosureSubAction2').append($('<option></option>').val("${switch.id}").html("${switch.name}"));
		    	</c:forEach>
		    	
		    	$("#contactClosureSubAction2").multiselect();
		    	
		    	<c:if test="${contactClosureList[1].subAction != ''}">
	    	 		$("#contactClosureSubAction2").val("${contactClosureList[1].subAction}".split(","));
	    	 	</c:if>
	    	 	
	    	 	$("#contactClosureSubAction2").multiselect("refresh");
				
	    	 	$("#contactClosureDuration2").val(${contactClosureList[1].duration});
	    	}else{
				$("#contactClosureDuration2").val("");
				$('#contactClosureDuration2').prop("disabled", true);
				
				$('#contactClosureTdSubAction2').html("NA");
	    	}
        	
        	if ($("#contactClosureAction3").val() == 1) {
        		$("#contactClosureDuration3").val(${contactClosureList[2].duration});
        		$('#contactClosureTdSubAction3').html("NA");
			}else if ($("#contactClosureAction3").val() == 2){
				var s3 = $("<select id=\"contactClosureSubAction3\" name=\"contactClosureSubAction3\" multiple=\"multiple\" />");
				$('#contactClosureTdSubAction3').html("");
				$('#contactClosureTdSubAction3').append(s3);
				<c:forEach items="${switchList}" var="switch">
		    	 	$('#contactClosureSubAction3').append($('<option></option>').val("${switch.id}").html("${switch.name}"));
		    	</c:forEach>
		    	
		    	$("#contactClosureSubAction3").multiselect();
		    	
		    	<c:if test="${contactClosureList[2].subAction != ''}">
	    	 		$("#contactClosureSubAction3").val("${contactClosureList[2].subAction}".split(","));
	    	 	</c:if>
	    	 	
	    	 	$("#contactClosureSubAction3").multiselect("refresh");
				
	    	 	$("#contactClosureDuration3").val(${contactClosureList[2].duration});
	    	}else{
				$("#contactClosureDuration3").val("");
				$('#contactClosureDuration3').prop("disabled", true);
				
				$('#contactClosureTdSubAction3').html("NA");
	    	}
        	
        	if ($("#contactClosureAction4").val() == 1) {
        		$("#contactClosureDuration4").val(${contactClosureList[3].duration});
        		$('#contactClosureTdSubAction4').html("NA");
			}else if ($("#contactClosureAction4").val() == 2){
				var s4 = $("<select id=\"contactClosureSubAction4\" name=\"contactClosureSubAction4\" multiple=\"multiple\" />");
				$('#contactClosureTdSubAction4').html("");
				$('#contactClosureTdSubAction4').append(s4);
				<c:forEach items="${switchList}" var="switch">
		    	 	$('#contactClosureSubAction4').append($('<option></option>').val("${switch.id}").html("${switch.name}"));
		    	</c:forEach>
		    	
		    	$("#contactClosureSubAction4").multiselect();
		    	
		    	<c:if test="${contactClosureList[3].subAction != ''}">
	    	 		$("#contactClosureSubAction4").val("${contactClosureList[3].subAction}".split(","));
	    	 	</c:if>
	    	 	
	    	 	$("#contactClosureSubAction4").multiselect("refresh");
				
	    	 	$("#contactClosureDuration4").val(${contactClosureList[3].duration});
	    	}else{
				$("#contactClosureDuration4").val("");
				$('#contactClosureDuration4').prop("disabled", true);
				
				$('#contactClosureTdSubAction4').html("NA");
	    	}
        	
        	
    	}else{
    		
    		$('#discoverButtonId').css("display", "none");
    		
    		$("#contactClosureAction1").val("0");
        	$("#contactClosureAction2").val("0");
        	$("#contactClosureAction3").val("0");
        	$("#contactClosureAction4").val("0");
    		
    		$("#contactClosureDuration1").val("");
        	$("#contactClosureDuration2").val("");
        	$("#contactClosureDuration3").val("");
        	$("#contactClosureDuration4").val("");
    		
    		$('#contactClosureAction1').prop("disabled", true);
    		$('#contactClosureAction2').prop("disabled", true);
    		$('#contactClosureAction3').prop("disabled", true);
    		$('#contactClosureAction4').prop("disabled", true);
    		
    		$('#contactClosureDuration1').prop("disabled", true);
    		$('#contactClosureDuration2').prop("disabled", true);
    		$('#contactClosureDuration3').prop("disabled", true);
    		$('#contactClosureDuration4').prop("disabled", true);
    		
    		$('#enableContactClosureId').prop('checked', false);
    		
    	}
    	
    	$("#macAddress1").text("${macAddress1}");
		$("#ipAddress1").text("${ipAddress1}");
    	
    	$('#enableContactClosureId').change(function () {
			if ($(this).attr("checked")) {
				$('#contactClosureAction1').prop("disabled", false);
	    		$('#contactClosureAction2').prop("disabled", false);
	    		$('#contactClosureAction3').prop("disabled", false);
	    		$('#contactClosureAction4').prop("disabled", false);
	    		
	    		$('#contactClosureDuration1').prop("disabled", false);
	    		$('#contactClosureDuration2').prop("disabled", false);
	    		$('#contactClosureDuration3').prop("disabled", false);
	    		$('#contactClosureDuration4').prop("disabled", false);
	    		
	    		$("#contactClosureAction1").val("0");
	        	$("#contactClosureAction2").val("0");
	        	$("#contactClosureAction3").val("0");
	        	$("#contactClosureAction4").val("0");
	        	
	        	$("#contactClosureDuration1").val("");
	        	$("#contactClosureDuration2").val("");
	        	$("#contactClosureDuration3").val("");
	        	$("#contactClosureDuration4").val("");
	        	
	        	$('#contactClosureDuration1').prop("disabled", true);
	    		$('#contactClosureDuration2').prop("disabled", true);
	    		$('#contactClosureDuration3').prop("disabled", true);
	    		$('#contactClosureDuration4').prop("disabled", true);
	    		
	        	$('#contactClosureTdSubAction1').html("NA");
	        	$('#contactClosureTdSubAction2').html("NA");
	        	$('#contactClosureTdSubAction3').html("NA");
	        	$('#contactClosureTdSubAction4').html("NA");
	    		
		    }else{
		    	
		    	$("#contactClosureAction1").val("0");
	        	$("#contactClosureAction2").val("0");
	        	$("#contactClosureAction3").val("0");
	        	$("#contactClosureAction4").val("0");
	        	
	        	$("#contactClosureDuration1").val("");
	        	$("#contactClosureDuration2").val("");
	        	$("#contactClosureDuration3").val("");
	        	$("#contactClosureDuration4").val("");
		    	
		    	$('#contactClosureAction1').prop("disabled", true);
	    		$('#contactClosureAction2').prop("disabled", true);
	    		$('#contactClosureAction3').prop("disabled", true);
	    		$('#contactClosureAction4').prop("disabled", true);
	    		
	    		$('#contactClosureDuration1').prop("disabled", true);
	    		$('#contactClosureDuration2').prop("disabled", true);
	    		$('#contactClosureDuration3').prop("disabled", true);
	    		$('#contactClosureDuration4').prop("disabled", true);
	    		
	    		$('#contactClosureTdSubAction1').html("");
	    		$('#contactClosureTdSubAction2').html("");
	    		$('#contactClosureTdSubAction3').html("");
	    		$('#contactClosureTdSubAction4').html("");
	    			    		
	    	}
		});
    	
    	$('#contactClosureAction1').change(function () {
    		if ($("#contactClosureAction1").val() == 1) {
				$('#contactClosureDuration1').prop("disabled", false);
				$('#contactClosureTdSubAction1').html("NA");
			}else if ($("#contactClosureAction1").val() == 2){
				var s1 = $("<select id=\"contactClosureSubAction1\" name=\"contactClosureSubAction1\" multiple=\"multiple\" />");
				$('#contactClosureTdSubAction1').html("");
				$('#contactClosureTdSubAction1').append(s1);
				<c:forEach items="${switchList}" var="switch">
		    	 	$('#contactClosureSubAction1').append($('<option></option>').val("${switch.id}").html("${switch.name}"));
		    	</c:forEach>
		    	
		    	$("#contactClosureSubAction1").multiselect();
		    	
		    	<c:if test="${contactClosureList[0].subAction != ''}">
	    	 		$("#contactClosureSubAction1").val("${contactClosureList[0].subAction}".split(","));
	    	 	</c:if>
	    	 	
	    	 	$("#contactClosureSubAction1").multiselect("refresh");
				
	    	 	$('#contactClosureDuration1').prop("disabled", false);
	    	}else{
				$("#contactClosureDuration1").val("");
				$('#contactClosureDuration1').prop("disabled", true);
				
				$('#contactClosureTdSubAction1').html("NA");
	    	}
		});
    	
    	$('#contactClosureAction2').change(function () {
    		if ($("#contactClosureAction2").val() == 1) {
				$('#contactClosureDuration2').prop("disabled", false);
				$('#contactClosureTdSubAction2').html("NA");
			}else if ($("#contactClosureAction2").val() == 2){
				var s2 = $("<select id=\"contactClosureSubAction2\" name=\"contactClosureSubAction2\" multiple=\"multiple\" />");
				$('#contactClosureTdSubAction2').html("");
				$('#contactClosureTdSubAction2').append(s2);
				<c:forEach items="${switchList}" var="switch">
		    	 	$('#contactClosureSubAction2').append($('<option></option>').val("${switch.id}").html("${switch.name}"));
		    	</c:forEach>
		    	
		    	$("#contactClosureSubAction2").multiselect();
		    	
		    	<c:if test="${contactClosureList[1].subAction != ''}">
	    	 		$("#contactClosureSubAction2").val("${contactClosureList[1].subAction}".split(","));
	    	 	</c:if>
	    	 	
	    	 	$("#contactClosureSubAction2").multiselect("refresh");
				
	    	 	$('#contactClosureDuration2').prop("disabled", false);
	    	}else{
				$("#contactClosureDuration2").val("");
				$('#contactClosureDuration2').prop("disabled", true);
				
				$('#contactClosureTdSubAction2').html("NA");
	    	}
		});
    	
    	$('#contactClosureAction3').change(function () {
    		if ($("#contactClosureAction3").val() == 1) {
				$('#contactClosureDuration3').prop("disabled", false);
				$('#contactClosureTdSubAction3').html("NA");
			}else if ($("#contactClosureAction3").val() == 2){
				var s3 = $("<select id=\"contactClosureSubAction3\" name=\"contactClosureSubAction3\" multiple=\"multiple\" />");
				$('#contactClosureTdSubAction3').html("");
				$('#contactClosureTdSubAction3').append(s3);
				<c:forEach items="${switchList}" var="switch">
		    	 	$('#contactClosureSubAction3').append($('<option></option>').val("${switch.id}").html("${switch.name}"));
		    	</c:forEach>
		    	
		    	$("#contactClosureSubAction3").multiselect();
		    	
		    	<c:if test="${contactClosureList[2].subAction != ''}">
	    	 		$("#contactClosureSubAction3").val("${contactClosureList[2].subAction}".split(","));
	    	 	</c:if>
	    	 	
	    	 	$("#contactClosureSubAction3").multiselect("refresh");
				
	    	 	$('#contactClosureDuration3').prop("disabled", false);
	    	}else{
				$("#contactClosureDuration3").val("");
				$('#contactClosureDuration3').prop("disabled", true);
				
				$('#contactClosureTdSubAction3').html("NA");
	    	}
		});
    	
    	$('#contactClosureAction4').change(function () {
			if ($("#contactClosureAction4").val() == 1) {
				$('#contactClosureDuration4').prop("disabled", false);
				$('#contactClosureTdSubAction4').html("NA");
			}else if ($("#contactClosureAction4").val() == 2){
				var s4 = $("<select id=\"contactClosureSubAction4\" name=\"contactClosureSubAction4\" multiple=\"multiple\" />");
				$('#contactClosureTdSubAction4').html("");
				$('#contactClosureTdSubAction4').append(s4);
				<c:forEach items="${switchList}" var="switch">
		    	 	$('#contactClosureSubAction4').append($('<option></option>').val("${switch.id}").html("${switch.name}"));
		    	</c:forEach>
		    	
		    	$("#contactClosureSubAction4").multiselect();
		    	
		    	<c:if test="${contactClosureList[3].subAction != ''}">
	    	 		$("#contactClosureSubAction4").val("${contactClosureList[3].subAction}".split(","));
	    	 	</c:if>
	    	 	
	    	 	$("#contactClosureSubAction4").multiselect("refresh");
				
	    	 	$('#contactClosureDuration4').prop("disabled", false);
	    	}else{
				$("#contactClosureDuration4").val("");
				$('#contactClosureDuration4').prop("disabled", true);
				
				$('#contactClosureTdSubAction4').html("NA");
	    	}
		});
    			
    	$(window).resize(); //To refresh/recalculate height and width of all regions
    });
    
    
    function saveContactClosureSettings(){
		
    	clearLabelMessage();
		
		var enableContactClosureString = "false";
		
		var name1 = "201";
		var action1 = "0";
		var duration1 = "0";
		
		var name2 = "202";
		var action2 = "0";
		var duration2 = "0";
		
		var name3 = "203";
		var action3 = "0";
		var duration3 = "0";
		
		var name4 = "204";
		var action4 = "0";
		var duration4 = "0";
		
		var macAddress1 = $("#macAddress1").text();
		
		var ipAddress1 = $("#ipAddress1").text();
		
		var productId1 = "${productId1}";
		
		var hwType1 = "${hwType1}";
		
		var fwVersion1 = "${fwVersion1}";
		
		var subAction1 = "";
		
		var subAction2 = "";
		
		var subAction3 = "";
		
		var subAction4 = "";
				
		if($('#enableContactClosureId').is(":checked")){
			enableContactClosureString = "true";
			
			action1 = $('#contactClosureAction1').val();
			
			if($("#contactClosureSubAction1").val() == null){
				subAction1 = "";
			}else{
				subAction1 = $("#contactClosureSubAction1").val();
			}
			
			
			if(action1 == "1"){
				duration1 = $('#contactClosureDuration1').val();
				if(duration1 !=""){
					if(!isPositiveInteger(duration1) || duration1 == "0"){
						displayLabelMessage('Please enter positive integer value for duration in first row',COLOR_ERROR);
						return false;
					}
				}else{
					displayLabelMessage('please enter a value for duration in first row',COLOR_ERROR);
					return false;
				}
			}else if(action1 == "2"){
				duration1 = $('#contactClosureDuration1').val();
				if(duration1 !=""){
					if(!isPositiveInteger(duration1) || duration1 == "0"){
						displayLabelMessage('Please enter positive integer value for duration in first row',COLOR_ERROR);
						return false;
					}
				}else{
					displayLabelMessage('please enter a value for duration in first row',COLOR_ERROR);
					return false;
				}
				if($("#contactClosureSubAction1").val() == null){
					displayLabelMessage('please select a value for Sub Action in first row',COLOR_ERROR);
					return false;
				}
			}
			
			action2 = $('#contactClosureAction2').val();
			
			if($("#contactClosureSubAction2").val() == null){
				subAction2 = "";
			}else{
				subAction2 = $("#contactClosureSubAction2").val();
			}
			
			if(action2 == "1"){
				duration2 = $('#contactClosureDuration2').val();
				if(duration2 !=""){
					if(!isPositiveInteger(duration2) || duration2 == "0"){
						displayLabelMessage('Please enter positive integer value for duration in second row',COLOR_ERROR);
						return false;
					}
				}else{
					displayLabelMessage('please enter a value for duration in second row',COLOR_ERROR);
					return false;
				}
			}else if(action2 == "2"){
				duration2 = $('#contactClosureDuration2').val();
				if(duration2 !=""){
					if(!isPositiveInteger(duration2) || duration2 == "0"){
						displayLabelMessage('Please enter positive integer value for duration in second row',COLOR_ERROR);
						return false;
					}
				}else{
					displayLabelMessage('please enter a value for duration in second row',COLOR_ERROR);
					return false;
				}
				if($("#contactClosureSubAction2").val() == null){
					displayLabelMessage('please select a value for Sub Action in second row',COLOR_ERROR);
					return false;
				}
			}
			
			action3 = $('#contactClosureAction3').val();
			
			if($("#contactClosureSubAction3").val() == null){
				subAction3 = "";
			}else{
				subAction3 = $("#contactClosureSubAction3").val();
			}
			
			if(action3 == "1"){
				duration3 = $('#contactClosureDuration3').val();
				if(duration3 !=""){
					if(!isPositiveInteger(duration3) || duration3 == "0"){
						displayLabelMessage('Please enter positive integer value for duration in third row',COLOR_ERROR);
						return false;
					}
				}else{
					displayLabelMessage('please enter a value for duration in third row',COLOR_ERROR);
					return false;
				}
			}else if(action3 == "2"){
				duration3 = $('#contactClosureDuration3').val();
				if(duration3 !=""){
					if(!isPositiveInteger(duration3) || duration3 == "0"){
						displayLabelMessage('Please enter positive integer value for duration in third row',COLOR_ERROR);
						return false;
					}
				}else{
					displayLabelMessage('please enter a value for duration in third row',COLOR_ERROR);
					return false;
				}
				if($("#contactClosureSubAction3").val() == null){
					displayLabelMessage('please select a value for Sub Action in third row',COLOR_ERROR);
					return false;
				}
			}
			
			action4 = $('#contactClosureAction4').val();
			
			if($("#contactClosureSubAction4").val() == null){
				subAction4 = "";
			}else{
				subAction4 = $("#contactClosureSubAction4").val();
			}
			
			if(action4 == "1"){
				duration4 = $('#contactClosureDuration4').val();
				if(duration4 !=""){
					if(!isPositiveInteger(duration4) || duration4 == "0"){
						displayLabelMessage('Please enter positive integer value for duration in fourth row',COLOR_ERROR);
						return false;
					}
				}else{
					displayLabelMessage('please enter a value for duration in fourth row',COLOR_ERROR);
					return false;
				}
			}else if(action4 == "2"){
				duration4 = $('#contactClosureDuration4').val();
				if(duration4 !=""){
					if(!isPositiveInteger(duration4) || duration4 == "0"){
						displayLabelMessage('Please enter positive integer value for duration in fourth row',COLOR_ERROR);
						return false;
					}
				}else{
					displayLabelMessage('please enter a value for duration in fourth row',COLOR_ERROR);
					return false;
				}
				if($("#contactClosureSubAction4").val() == null){
					displayLabelMessage('please select a value for Sub Action in fourth row',COLOR_ERROR);
					return false;
				}
			}
			
		}
		
		$.ajax({
	 		type: 'POST',
	 		url: "${saveContactClosureUrl}?ts="+new Date().getTime(),
	 		contentType: "application/json",
	 		data: '{"enabled":"' + enableContactClosureString + '","contactClosureVo":[{"macAddress":"' +macAddress1+ '","ipAddress":"' +ipAddress1+ '","productId":"' +productId1+ '","hwType":"' +hwType1+ '","fwVersion":"' +fwVersion1+ '","contactClosureControlsList":[{"name":"' + name1 + '","action":"' + action1 + '","subAction":"' + subAction1 + '","duration":"' + duration1  +'"},{"name":"' + name2 + '","action":"' + action2 + '","subAction":"' + subAction2 + '","duration":"' + duration2  +'"},{"name":"' + name3 + '","action":"' + action3 + '","subAction":"' + subAction3 + '","duration":"' + duration3  +'"},{"name":"' + name4 + '","action":"' + action4 + '","subAction":"' + subAction4 + '","duration":"' + duration4  +'"}]}]}', 
	 		dataType: "json",
	 		success: function(data){
				displayLabelMessage('Contact Closure Settings successfully saved.',COLOR_SUCCESS);
				if(enableContactClosureString == "true"){
					$('#discoverButtonId').css("display", "block");
				}else{
					$('#discoverButtonId').css("display", "none");
				}
			},
			error: function(){
				displayLabelMessage('Error.',COLOR_ERROR);
			}
	 	});
	}
    
    function startDiscovery(){
    	$.ajax({
    		type: 'POST',
    		url: "${discoverContactClosureUrl}?ts="+new Date().getTime(),
    		dataType:"json",
    		success: function(data){
    			displayLabelMessage("Dicovery initiated...", "blue");
    			getContactClosure();
    		}
    	});
    }
    
    function getContactClosure(){
    	$.ajax({
    		type: 'POST',
    		url: "${getContactClosureUrl}?ts="+new Date().getTime(),
    		dataType:"json",
    		success: function(data){
    			$("#macAddress1").text(data.contactClosureVo.macAddress);
    			$("#ipAddress1").text(data.contactClosureVo.ipAddress);
    			displayLabelMessage("", "black");
    		}
    	});
    }
    
    function isPositiveInteger(n) {
        return 0 === n % (!isNaN(parseFloat(n)) && 0 <= ~~n);
    }
    
    function displayLabelMessage(Message, Color) {
		$("#contact_closure_message").html(Message);
		$("#contact_closure_message").css("color", Color);
	}
    function clearLabelMessage(Message, Color) {
		displayLabelMessage("", COLOR_DEFAULT);
	}
</script>

<div class="outermostdiv">

<div class="outerContainer">
	<span>Contact Closure Configuration</span>
	<div class="i1"></div>
</div>

<div class="innerdiv">

<input type="checkbox" name="enableContactClosure" id="enableContactClosureId"/>  Enable

<input type="button" name="discoverButton" id="discoverButtonId" value="Discover" style="float:right" onclick="startDiscovery();"/>

<div style="height:5px;"></div>
	
<div id="contact_closure_message" style="font-size: 14px; font-weight: bold;padding: 5px 0 0 5px;" ></div>

<div style="height:5px;"></div>

	<div id="contactClouseDetailsId1" style="font-size: 14px; font-weight: bold;padding: 5px 0 0 5px;" >
	Device ID : 1 , MAC Address : <span id="macAddress1"></span> ,  IP Address : <span id="ipAddress1"></span>
	</div>

	<table id="contact_closure_list" class="entable" style="width: 100%">
		<thead>
			<tr>
				<th align="center" style="width: 15%" >Contact Closure</th>
				<th align="center" style="width: 40%" >Action</th>
				<th align="center" style="width: 30%" >Switch Group</th>
				<th align="center" style="width: 15%" >Duration( In Minutes )</th>
			</tr>
		</thead>
		<tr>
			<td ><b>Input 1</b></td>	
			<td ><select id="contactClosureAction1">
					<option value="0">Do Nothing</option>
					<option value="1">Set all fixtures in Energy Manager to 100%</option>
					<option value="2">Set all fixtures in Switch Group to 100%</option>
					<option value="3">Set all fixtures to Auto mode</option>
				 </select>
			</td>
			<td id="contactClosureTdSubAction1">
			</td>
			<td ><input id="contactClosureDuration1" size="10" type="text" /></td>
		</tr>
		<tr>
			<td ><b>Input 2</b></td>	
			<td ><select id="contactClosureAction2">
					<option value="0">Do Nothing</option>
					<option value="1">Set all fixtures in Energy Manager to 100%</option>
					<option value="2">Set all fixtures in Switch Group to 100%</option>
					<option value="3">Set all fixtures to Auto mode</option>
				 </select>
			</td>
			<td id="contactClosureTdSubAction2">
			</td>
			<td ><input id="contactClosureDuration2" size="10" type="text" /></td>
		</tr>
		
		<tr>
			<td ><b>Input 3</b></td>	
			<td ><select id="contactClosureAction3">
					<option value="0">Do Nothing</option>
					<option value="1">Set all fixtures in Energy Manager to 100%</option>
					<option value="2">Set all fixtures in Switch Group to 100%</option>
					<option value="3">Set all fixtures to Auto mode</option>
				 </select>
			</td>
			<td id="contactClosureTdSubAction3">
			</td>
			<td ><input id="contactClosureDuration3" size="10" type="text" /></td>
		</tr>
		
		<tr>
			<td ><b>Input 4</b></td>	
			<td ><select id="contactClosureAction4">
					<option value="0">Do Nothing</option>
					<option value="1">Set all fixtures in Energy Manager to 100%</option>
					<option value="2">Set all fixtures in Switch Group to 100%</option>
					<option value="3">Set all fixtures to Auto mode</option>
				 </select>
			</td>
			<td id="contactClosureTdSubAction4">
			</td>
			<td ><input id="contactClosureDuration4" size="10" type="text" /></td>
		</tr>
			
	</table>
	<div style="height:5px;"></div>
	<button style="padding-left:5px;" id="saveContactClosure"  onclick="saveContactClosureSettings();">Save</button>
</div>


</div>