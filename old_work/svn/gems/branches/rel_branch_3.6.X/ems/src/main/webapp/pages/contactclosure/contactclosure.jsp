<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<spring:url value="/services/org/contactclosure/saveContactClosure" var="saveContactClosureUrl" scope="request" />
<spring:url value="/scripts/jquery/jquery.multiselect.js" var="jquerymultiselect"></spring:url>
<script type="text/javascript" src="${jquerymultiselect}"></script>

<spring:url value="/themes/standard/css/jquery/jquery.multiselect.css" var="multiselectCss"></spring:url>
<link rel="stylesheet" type="text/css" href="${multiselectCss}" />

<spring:url value="/scripts/jquery/jquery.multipleselectbox.js" var="jquerymultipleselectbox"></spring:url>
<script type="text/javascript" src="${jquerymultipleselectbox}"></script>

<spring:url value="/themes/standard/css/jquery/multipleselectbox.css" var="multipleselectbox"></spring:url>
<link rel="stylesheet" type="text/css" href="${multipleselectbox}" />

		
<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
	
	.ui-dialog-title{font-size: 1.4em; font-weight: bolder;}
	
	table#contact_closure_list td{overflow: hidden !important;}
		
	.entable td{
		padding-left:5px;
		/* border:1px #252525 solid; */
		border: 1px solid #E5E5E5 !important;
		height:32px;
		text-overflow:ellipsis;
	}
	.outermostdiv{height:98%;}
	
	.MultipleSelectBox{
		width: 250px;
	}
			
</style>

<script type="text/javascript">
	var COLOR_SUCCESS = "green";
	var COLOR_DEFAULT = "black";
	var COLOR_ERROR = "red";
	
	var bHideArray = [false,false,false,false,false,false,false,false];
	
	var sceneSelectedArray = [0,0,0,0,0,0,0,0];
	
	
    $(document).ready(function() {
    		
    	    $("#macAddress").text("${macAddress}");
 		    $("#ipAddress").text("${ipAddress}");
 		    
    		<c:forEach items='${contactClosureList}' var='contactClosureControl' varStatus="loopCounter">
        		$('#'+'<c:out value="contactClosureAction${loopCounter.count}"/>').val('${contactClosureControl.action}');
        	</c:forEach>
        	
        	
        	<c:forEach items='${contactClosureList}' var='contactClosureControl' varStatus="loopCounter">
    			
	        	if ($('#'+'<c:out value="contactClosureAction${loopCounter.count}"/>').val() == 1) {
	        		$('#'+'<c:out value="contactClosureDuration${loopCounter.count}"/>').val('${contactClosureControl.duration}');
	        		$('#'+'<c:out value="contactClosurePercentage${loopCounter.count}"/>').val('${contactClosureControl.percentage}');
	        		$('#'+'<c:out value="contactClosureTdSubAction${loopCounter.count}"/>').html("NA");
				}else if ($('#'+'<c:out value="contactClosureAction${loopCounter.count}"/>').val() == 4 ){
					var s1 = $('<select id=\'<c:out value="contactClosureSubAction${loopCounter.count}"/>\' name=\'<c:out value="contactClosureSubAction${loopCounter.count}"/>\' multiple=\'multiple\' />');
					$('#'+'<c:out value="contactClosureTdSubAction${loopCounter.count}"/>').html("");
					$('#'+'<c:out value="contactClosureTdSubAction${loopCounter.count}"/>').append(s1);
					<c:forEach items="${switchList}" var="switch">
			    	 	$('#'+'<c:out value="contactClosureSubAction${loopCounter.count}"/>').append($('<option></option>').val("${switch.id}").html("${switch.name}"));
			    	</c:forEach>
			    	
			    	$('#'+'<c:out value="contactClosureSubAction${loopCounter.count}"/>').multiselect();
			    	
			    	<c:if test="${contactClosureControl.subAction != ''}">
		    	 		$('#'+'<c:out value="contactClosureSubAction${loopCounter.count}"/>').val("${contactClosureControl.subAction}".split(","));
		    	 	</c:if>
		    	 	
		    	 	$('#'+'<c:out value="contactClosureSubAction${loopCounter.count}"/>').multiselect("refresh");
							    	 	
	    	 		$('#'+'<c:out value="contactClosureDuration${loopCounter.count}"/>').val("");
					$('#'+'<c:out value="contactClosureDuration${loopCounter.count}"/>').prop("disabled", true);
					
					$('#'+'<c:out value="contactClosurePercentage${loopCounter.count}"/>').val("");
					$('#'+'<c:out value="contactClosurePercentage${loopCounter.count}"/>').prop("disabled", true);
		    	 			    	 	
		    	}else if ($('#'+'<c:out value="contactClosureAction${loopCounter.count}"/>').val() == 2 ){
		    		var s2String = "";
	 		    	s2String = '<input type=\'text\' id=\'<c:out value="controlDropDown${loopCounter.count}"/>\'  value=\'Select switches and scenes\' size=\'40\' readonly /><br />';
	 		    	s2String = s2String + '<ul id=\'<c:out value="MultipleSelectBox_DropDown${loopCounter.count}"/>\'  style=\'position: absolute; background-color: white; display: none;\'>';
	 		    	
	 		    	<c:forEach items='${switchScenesList}' var='switchScenes'>
	 		    		s2String = s2String + '<li class=\'optgroup\'><input type=\'checkbox\' id=\'<c:out value="switchObj_${switchScenes.id}_${loopCounter.count}"/>\' ';
	 		    			s2String = s2String + 'name=\'<c:out value="switchObj_${switchScenes.id}_${loopCounter.count}"/>\' value=\'<c:out value="${switchScenes.id}"/>\' ';
	 		    					s2String = s2String + 'onclick=\'<c:out value="onSwitchSel(this,\'swgrp_${switchScenes.id}_${loopCounter.count}\')"/>\' ><span><c:out value="${switchScenes.name}"/></span></li>';
	 		    					
						<c:forEach items='${switchScenes.sceneList}' var='scene'>
							s2String = s2String + '<li class=\'optgroupitem\'><input type=\'radio\' name=\'<c:out value="swgrp_${switchScenes.id}_${loopCounter.count}"/>\' ';
								s2String = s2String + 'value=\'<c:out value="${scene.switchId}_${scene.id}"/>\' disabled ><span><c:out value="${scene.name}"/></span></li>' ;
						</c:forEach>
	 		    					
	 		    	</c:forEach>
	 		    	s2String = s2String + '</ul>';
	 		    	var s2 = $(s2String);
	 		    	$('#'+'<c:out value="contactClosureTdSubAction${loopCounter.count}"/>').html("");
					$('#'+'<c:out value="contactClosureTdSubAction${loopCounter.count}"/>').append(s2);
					
					
					$('#'+'<c:out value="MultipleSelectBox_DropDown${loopCounter.count}"/>').multipleSelectBox();
					
					$('#'+'<c:out value="controlDropDown${loopCounter.count}"/>').click(function(e) {
						e.stopPropagation();
						var arrayIndex = '<c:out value="${loopCounter.count}"/>' - 1;
						if (bHideArray[arrayIndex]) {
							bHideArray[arrayIndex] = false;
							$('#'+'<c:out value="MultipleSelectBox_DropDown${loopCounter.count}"/>').slideUp("slow");
						} else {
							bHideArray[arrayIndex] = true;
							$('#'+'<c:out value="MultipleSelectBox_DropDown${loopCounter.count}"/>').slideDown("slow");
						}
					});
					
					//<c:forEach items='${switchScenesList}' var='switchScenes'>
					//	var switchObjId = '<c:out value="switchObj_${switchScenes.id}_${loopCounter.count}"/>';
					//	var swgrpId = '<c:out value="swgrp_${switchScenes.id}_${loopCounter.count}"/>';
					//	onSwitchSel($("input[name='"+switchObjId+"']"), swgrpId);
					//</c:forEach>
					
					
					if("${contactClosureControl.subAction}" != "" && "${contactClosureControl.subAction}".indexOf("_") >= 0 ){
						var subActionArray = "${contactClosureControl.subAction}".split(",");
						var inputIndex = '<c:out value="${loopCounter.count}"/>';
						sceneSelectedArray[inputIndex - 1] = 0;
			   			for(i = 0; i < subActionArray.length; i++){
				   			var switchObjId = "switchObj_" + subActionArray[i].split("_")[0] + "" + "_" + inputIndex;
				   			var swgGrpName = "swgrp_" + subActionArray[i].split("_")[0] + "" + "_" + inputIndex;
				   			$('#'+switchObjId).prop('checked', true);
				   			sceneSelectedArray[inputIndex - 1] = sceneSelectedArray[inputIndex - 1] + 1;
							$("#controlDropDown"+inputIndex).val(sceneSelectedArray[inputIndex - 1] + " selected");
				   			$("input[name='" + swgGrpName + "']").removeAttr('disabled');
				   			$("input[name='"+ swgGrpName +"'][value='" + subActionArray[i] + "']").prop('checked', true);
			 		 	}
			   		}
					
					$('#'+'<c:out value="contactClosureDuration${loopCounter.count}"/>').val("");
					$('#'+'<c:out value="contactClosureDuration${loopCounter.count}"/>').prop("disabled", true);
					
					$('#'+'<c:out value="contactClosurePercentage${loopCounter.count}"/>').val("");
					$('#'+'<c:out value="contactClosurePercentage${loopCounter.count}"/>').prop("disabled", true);
					
		    	}
				else{
		    		$('#'+'<c:out value="contactClosureDuration${loopCounter.count}"/>').val("");
					$('#'+'<c:out value="contactClosureDuration${loopCounter.count}"/>').prop("disabled", true);
					
					$('#'+'<c:out value="contactClosurePercentage${loopCounter.count}"/>').val("");
					$('#'+'<c:out value="contactClosurePercentage${loopCounter.count}"/>').prop("disabled", true);
					
					$('#'+'<c:out value="contactClosureTdSubAction${loopCounter.count}"/>').html("NA");
		    	}
        	
    		</c:forEach>
        	
    	
		<c:forEach items='${contactClosureList}' var='contactClosureControl' varStatus="loopCounter">
			$('#'+'<c:out value="contactClosureAction${loopCounter.count}"/>').change(function () {
	        	if ($('#'+'<c:out value="contactClosureAction${loopCounter.count}"/>').val() == 1) {
	        		$('#'+'<c:out value="contactClosureDuration${loopCounter.count}"/>').prop("disabled", false);
	        		$('#'+'<c:out value="contactClosurePercentage${loopCounter.count}"/>').prop("disabled", false);
	        		$('#'+'<c:out value="contactClosureTdSubAction${loopCounter.count}"/>').html("NA");
				}else if ($('#'+'<c:out value="contactClosureAction${loopCounter.count}"/>').val() == 4 ){
					var s1 = $('<select id=\'<c:out value="contactClosureSubAction${loopCounter.count}"/>\' name=\'<c:out value="contactClosureSubAction${loopCounter.count}"/>\' multiple=\'multiple\' />');
					$('#'+'<c:out value="contactClosureTdSubAction${loopCounter.count}"/>').html("");
					$('#'+'<c:out value="contactClosureTdSubAction${loopCounter.count}"/>').append(s1);
					<c:forEach items="${switchList}" var="switch">
			    	 	$('#'+'<c:out value="contactClosureSubAction${loopCounter.count}"/>').append($('<option></option>').val("${switch.id}").html("${switch.name}"));
			    	</c:forEach>
			    	
			    	$('#'+'<c:out value="contactClosureSubAction${loopCounter.count}"/>').multiselect();
			    	
			    	<c:if test="${contactClosureControl.subAction != ''}">
		    	 		$('#'+'<c:out value="contactClosureSubAction${loopCounter.count}"/>').val("${contactClosureControl.subAction}".split(","));
		    	 	</c:if>
		    	 	
		    	 	$('#'+'<c:out value="contactClosureSubAction${loopCounter.count}"/>').multiselect("refresh");
					
		    	 	$('#'+'<c:out value="contactClosureDuration${loopCounter.count}"/>').val("");
	    	 		$('#'+'<c:out value="contactClosureDuration${loopCounter.count}"/>').prop("disabled", true);
	    	 		
	    	 		$('#'+'<c:out value="contactClosurePercentage${loopCounter.count}"/>').val("");
	    	 		$('#'+'<c:out value="contactClosurePercentage${loopCounter.count}"/>').prop("disabled", true);
		    	 	
		    	}else if ($('#'+'<c:out value="contactClosureAction${loopCounter.count}"/>').val() == 2 ){
		    		
		    		var s2String = "";
	 		    	s2String = '<input type=\'text\' id=\'<c:out value="controlDropDown${loopCounter.count}"/>\'  value=\'Select switches and scenes\' size=\'40\' readonly /><br />';
	 		    	s2String = s2String + '<ul id=\'<c:out value="MultipleSelectBox_DropDown${loopCounter.count}"/>\' style=\'position: absolute; background-color: white; display: none;\'>';
	 		    	
	 		    	<c:forEach items='${switchScenesList}' var='switchScenes'>
	 		    		s2String = s2String + '<li class=\'optgroup\'><input type=\'checkbox\' id=\'<c:out value="switchObj_${switchScenes.id}_${loopCounter.count}"/>\' ';
	 		    			s2String = s2String + 'name=\'<c:out value="switchObj_${switchScenes.id}_${loopCounter.count}"/>\' value=\'<c:out value="${switchScenes.id}"/>\' ';
	 		    					s2String = s2String + 'onclick=\'<c:out value="onSwitchSel(this,\'swgrp_${switchScenes.id}_${loopCounter.count}\')"/>\' ><span><c:out value="${switchScenes.name}"/></span></li>';
	 		    					
						<c:forEach items='${switchScenes.sceneList}' var='scene'>
							s2String = s2String + '<li class=\'optgroupitem\'><input type=\'radio\' name=\'<c:out value="swgrp_${switchScenes.id}_${loopCounter.count}"/>\' ';
								s2String = s2String + 'value=\'<c:out value="${scene.switchId}_${scene.id}"/>\' disabled ><span><c:out value="${scene.name}"/></span></li>' ;
						</c:forEach>
	 		    					
	 		    	</c:forEach>
	 		    	s2String = s2String + '</ul>';
	 		    	var s2 = $(s2String);
	 		    	$('#'+'<c:out value="contactClosureTdSubAction${loopCounter.count}"/>').html("");
					$('#'+'<c:out value="contactClosureTdSubAction${loopCounter.count}"/>').append(s2);
					
					
					$('#'+'<c:out value="MultipleSelectBox_DropDown${loopCounter.count}"/>').multipleSelectBox();
					
					$('#'+'<c:out value="controlDropDown${loopCounter.count}"/>').click(function(e) {
						e.stopPropagation();
						var arrayIndex = '<c:out value="${loopCounter.count}"/>' - 1;
						if (bHideArray[arrayIndex]) {
							bHideArray[arrayIndex] = false;
							$('#'+'<c:out value="MultipleSelectBox_DropDown${loopCounter.count}"/>').slideUp("slow");
						} else {
							bHideArray[arrayIndex] = true;
							$('#'+'<c:out value="MultipleSelectBox_DropDown${loopCounter.count}"/>').slideDown("slow");
						}
					});
					
					//<c:forEach items='${switchScenesList}' var='switchScenes'>
					//	var switchObjId = '<c:out value="switchObj_${switchScenes.id}_${loopCounter.count}"/>';
					//	var swgrpId = '<c:out value="swgrp_${switchScenes.id}_${loopCounter.count}"/>';
					//	onSwitchSel($("input[name='"+switchObjId+"']"), swgrpId);
					//</c:forEach>
					
					
					if("${contactClosureControl.subAction}" != "" && "${contactClosureControl.subAction}".indexOf("_") >= 0 ){
						var subActionArray = "${contactClosureControl.subAction}".split(",");
						var inputIndex = '<c:out value="${loopCounter.count}"/>';
						sceneSelectedArray[inputIndex - 1] = 0;
			   			for(i = 0; i < subActionArray.length; i++){
				   			var switchObjId = "switchObj_" + subActionArray[i].split("_")[0] + "" + "_" + inputIndex;
				   			var swgGrpName = "swgrp_" + subActionArray[i].split("_")[0] + "" + "_" + inputIndex;
				   			$('#'+switchObjId).prop('checked', true);
				   			sceneSelectedArray[inputIndex - 1] = sceneSelectedArray[inputIndex - 1] + 1;
							$("#controlDropDown"+inputIndex).val(sceneSelectedArray[inputIndex - 1] + " selected");
				   			$("input[name='" + swgGrpName + "']").removeAttr('disabled');
				   			$("input[name='"+ swgGrpName +"'][value='" + subActionArray[i] + "']").prop('checked', true);
			 		 	}
			   		}
					
					$('#'+'<c:out value="contactClosureDuration${loopCounter.count}"/>').val("");
					$('#'+'<c:out value="contactClosureDuration${loopCounter.count}"/>').prop("disabled", true);
					
					$('#'+'<c:out value="contactClosurePercentage${loopCounter.count}"/>').val("");
					$('#'+'<c:out value="contactClosurePercentage${loopCounter.count}"/>').prop("disabled", true);
		    		
		    	}
				else{
		    		$('#'+'<c:out value="contactClosureDuration${loopCounter.count}"/>').val("");
					$('#'+'<c:out value="contactClosureDuration${loopCounter.count}"/>').prop("disabled", true);
					
					$('#'+'<c:out value="contactClosurePercentage${loopCounter.count}"/>').val("");
					$('#'+'<c:out value="contactClosurePercentage${loopCounter.count}"/>').prop("disabled", true);
					
					$('#'+'<c:out value="contactClosureTdSubAction${loopCounter.count}"/>').html("NA");
		    	}
			});  	
		</c:forEach>
    			
    	$(window).resize(); //To refresh/recalculate height and width of all regions
    	console.log('dummy');


    });
      
	
	function onSwitchSel(switchObj, sceneObj) {
		var inputIndex = sceneObj.split("_")[2];
		console.log("inputIndex:"+inputIndex);
		if (switchObj.checked) {
			$("input[name='" + sceneObj + "']").removeAttr('disabled');
			$("input:radio[name='" + sceneObj + "'][disabled=false]:first").attr('checked', true);
			
			sceneSelectedArray[inputIndex - 1] = sceneSelectedArray[inputIndex - 1] + 1;
			$("#controlDropDown"+inputIndex).val(sceneSelectedArray[inputIndex - 1] + " selected");
			console.log("selected sceneSelectedArray[inputIndex - 1] : "+sceneSelectedArray[inputIndex - 1]);
		}else {
			$("input[name='" + sceneObj + "']").prop('checked', false);
			$("input[name='" + sceneObj + "']").attr('disabled', 'disabled');
			
			sceneSelectedArray[inputIndex -1] = sceneSelectedArray[inputIndex -1] - 1;
			if(sceneSelectedArray[inputIndex -1] != 0){
				$("#controlDropDown"+inputIndex).val(sceneSelectedArray[inputIndex -1] + " selected");
			}else{
				$("#controlDropDown"+inputIndex).val("Select switches and scenes");
			}
			
			
			console.log("unselected sceneSelectedArray[inputIndex - 1] : "+sceneSelectedArray[inputIndex - 1]);
		}
	}
    
    function saveContactClosureSettings(){
		
    	clearCcEditLabelMessage();
		    	
    	var name1 = "201";
		var action1 = "0";
		var duration1 = "0";
		var subAction1 = "";
		var percentage1 = "0";
		
		var name2 = "202";
		var action2 = "0";
		var duration2 = "0";
		var subAction2 = "";
		var percentage2 = "0";
		
		var name3 = "203";
		var action3 = "0";
		var duration3 = "0";
		var subAction3 = "";
		var percentage3 = "0";
		
		var name4 = "204";
		var action4 = "0";
		var duration4 = "0";
		var subAction4 = "";
		var percentage4 = "0";
		
		var macAddress = $("#macAddress").text();
		
		var ipAddress = $("#ipAddress").text();
		
		var productId = "${productId}";
		
		var hwType = "${hwType}";
		
		var fwVersion = "${fwVersion}";
		
		var inputContactClosureVo = "";
		
		
		
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
					displayCcEditLabelMessage('Please enter positive integer value for duration in first row',COLOR_ERROR);
					return false;
				}
			}else{
				displayCcEditLabelMessage('please enter a value for duration in first row',COLOR_ERROR);
				return false;
			}
			
			percentage1 = $('#contactClosurePercentage1').val();
			if(percentage1 !=""){
				if(!isPositiveInteger(percentage1) || percentage1 == "0" || percentage1 > 100){
					displayCcEditLabelMessage('Please enter positive integer value less than 100 for percentage in first row',COLOR_ERROR);
					return false;
				}
			}else{
				displayCcEditLabelMessage('please enter a value for percentage1 in first row',COLOR_ERROR);
				return false;
			}
		}else if(action1 == "2"){
			
			subAction1 = "";
	    	
	    	<c:forEach items='${switchScenesList}' var='switchScenes'>
				var swgGrpName = '<c:out value="swgrp_${switchScenes.id}_1"/>';
				
				if($("input[name='" + swgGrpName + "']:checked").val() != undefined ){
					if(subAction1 == ""){
						subAction1 = $("input[name='" + swgGrpName + "']:checked").val();
					}else{
						subAction1 = subAction1 + "," + $("input[name='" + swgGrpName + "']:checked").val();
					}
				}
				
	    	</c:forEach>
	    	
	    	if(subAction1 == ""){
				displayCcEditLabelMessage('please select atleast one scene in first row',COLOR_ERROR);
				return false;
			}
			
		}else if(action1 == "4"){
			if($("#contactClosureSubAction1").val() == null){
				displayCcEditLabelMessage('please select a value for Switch Group in first row',COLOR_ERROR);
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
					displayCcEditLabelMessage('Please enter positive integer value for duration in second row',COLOR_ERROR);
					return false;
				}
			}else{
				displayCcEditLabelMessage('please enter a value for duration in second row',COLOR_ERROR);
				return false;
			}
			
			percentage2 = $('#contactClosurePercentage2').val();
			if(percentage2 !=""){
				if(!isPositiveInteger(percentage2) || percentage2 == "0" || percentage2 > 100){
					displayCcEditLabelMessage('Please enter positive integer value less than 100 for percentage in second row',COLOR_ERROR);
					return false;
				}
			}else{
				displayCcEditLabelMessage('please enter a value for percentage in second row',COLOR_ERROR);
				return false;
			}
			
		}else if(action2 == "2"){
			subAction2 = "";
	    	
	    	<c:forEach items='${switchScenesList}' var='switchScenes'>
				var swgGrpName = '<c:out value="swgrp_${switchScenes.id}_2"/>';
				
				if($("input[name='" + swgGrpName + "']:checked").val() != undefined ){
					if(subAction2 == ""){
						subAction2 = $("input[name='" + swgGrpName + "']:checked").val();
					}else{
						subAction2 = subAction2 + "," + $("input[name='" + swgGrpName + "']:checked").val();
					}
				}
				
	    	</c:forEach>
	    	
	    	if(subAction2 == ""){
				displayCcEditLabelMessage('please select atleast one scene in second row',COLOR_ERROR);
				return false;
			}
			
		}else if(action2 == "4"){
			if($("#contactClosureSubAction2").val() == null){
				displayCcEditLabelMessage('please select a value for Switch Group in second row',COLOR_ERROR);
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
					displayCcEditLabelMessage('Please enter positive integer value for duration in third row',COLOR_ERROR);
					return false;
				}
			}else{
				displayCcEditLabelMessage('please enter a value for duration in third row',COLOR_ERROR);
				return false;
			}
			
			percentage3 = $('#contactClosurePercentage3').val();
			if(percentage3 !=""){
				if(!isPositiveInteger(percentage3) || percentage3 == "0" || percentage3 > 100){
					displayCcEditLabelMessage('Please enter positive integer value less than 100 for percentage in third row',COLOR_ERROR);
					return false;
				}
			}else{
				displayCcEditLabelMessage('please enter a value for percentage in third row',COLOR_ERROR);
				return false;
			}
			
		}else if(action3 == "2"){
			
			subAction3 = "";
	    	
	    	<c:forEach items='${switchScenesList}' var='switchScenes'>
				var swgGrpName = '<c:out value="swgrp_${switchScenes.id}_3"/>';
				
				if($("input[name='" + swgGrpName + "']:checked").val() != undefined ){
					if(subAction3 == ""){
						subAction3 = $("input[name='" + swgGrpName + "']:checked").val();
					}else{
						subAction3 = subAction3 + "," + $("input[name='" + swgGrpName + "']:checked").val();
					}
				}
				
	    	</c:forEach>
	    	
	    	if(subAction3 == ""){
				displayCcEditLabelMessage('please select atleast one scene in third row',COLOR_ERROR);
				return false;
			}
			
		}else if(action3 == "4"){
			if($("#contactClosureSubAction3").val() == null){
				displayCcEditLabelMessage('please select a value for Switch Group in third row',COLOR_ERROR);
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
					displayCcEditLabelMessage('Please enter positive integer value for duration in fourth row',COLOR_ERROR);
					return false;
				}
			}else{
				displayCcEditLabelMessage('please enter a value for duration in fourth row',COLOR_ERROR);
				return false;
			}
			
			percentage4 = $('#contactClosurePercentage4').val();
			if(percentage4 !=""){
				if(!isPositiveInteger(percentage4) || percentage4 == "0" || percentage4 > 100){
					displayCcEditLabelMessage('Please enter positive integer value less than 100 for percentage in fourth row',COLOR_ERROR);
					return false;
				}
			}else{
				displayCcEditLabelMessage('please enter a value for percentage in fourth row',COLOR_ERROR);
				return false;
			}
			
		}else if(action4 == "2"){
			
			subAction4 = "";
	    	
	    	<c:forEach items='${switchScenesList}' var='switchScenes'>
				var swgGrpName = '<c:out value="swgrp_${switchScenes.id}_4"/>';
				
				if($("input[name='" + swgGrpName + "']:checked").val() != undefined ){
					if(subAction4 == ""){
						subAction4 = $("input[name='" + swgGrpName + "']:checked").val();
					}else{
						subAction4 = subAction4 + "," + $("input[name='" + swgGrpName + "']:checked").val();
					}
				}
				
	    	</c:forEach>
	    	
	    	if(subAction4 == ""){
				displayCcEditLabelMessage('please select atleast one scene in fourth row',COLOR_ERROR);
				return false;
			}
			
		}else if(action4 == "4"){
			if($("#contactClosureSubAction4").val() == null){
				displayCcEditLabelMessage('please select a value for Switch Group in fourth row',COLOR_ERROR);
				return false;
			}
		}
		
		inputContactClosureVo = '{"contactClosureVo":[{"macAddress":"' +macAddress+ '","ipAddress":"' +ipAddress+ '","productId":"' +productId+ '","hwType":"' 
															+hwType+ '","fwVersion":"' +fwVersion+ '","contactClosureControlsList":[{"name":"' 
															+ name1 + '","action":"' + action1 + '","subAction":"' + subAction1 + '","duration":"' + duration1  +'","percentage":"' + percentage1  +'"},{"name":"' 
															+ name2 + '","action":"' + action2 + '","subAction":"' + subAction2 + '","duration":"' + duration2  +'","percentage":"' + percentage2  +'"},{"name":"' 
															+ name3 + '","action":"' + action3 + '","subAction":"' + subAction3 + '","duration":"' + duration3  +'","percentage":"' + percentage3  +'"},{"name":"' 
															+ name4 + '","action":"' + action4 + '","subAction":"' + subAction4 + '","duration":"' + duration4  +'","percentage":"' + percentage4  +'"}]}]}';
		
		<c:if test="${contactClosureListSize == '8'}">
		
		var name5 = "205";
		var action5 = "0";
		var duration5 = "0";
		var subAction5 = "";
		var percentage5 = "0";
		
		var name6 = "206";
		var action6 = "0";
		var duration6 = "0";
		var subAction6 = "";
		var percentage6 = "0";
		
		var name7 = "207";
		var action7 = "0";
		var duration7 = "0";
		var subAction7 = "";
		var percentage7 = "0";
		
		var name8 = "208";
		var action8 = "0";
		var duration8 = "0";
		var subAction8 = "";
		var percentage8 = "0";
		
		action5 = $('#contactClosureAction5').val();
		
		if($("#contactClosureSubAction5").val() == null){
			subAction5 = "";
		}else{
			subAction5 = $("#contactClosureSubAction5").val();
		}
		
		if(action5 == "1"){
			duration5 = $('#contactClosureDuration5').val();
			if(duration5 !=""){
				if(!isPositiveInteger(duration5) || duration5 == "0"){
					displayCcEditLabelMessage('Please enter positive integer value for duration in fifth row',COLOR_ERROR);
					return false;
				}
			}else{
				displayCcEditLabelMessage('please enter a value for duration in fifth row',COLOR_ERROR);
				return false;
			}
			
			percentage5 = $('#contactClosurePercentage5').val();
			if(percentage5 !=""){
				if(!isPositiveInteger(percentage5) || percentage5 == "0" || percentage5 > 100){
					displayCcEditLabelMessage('Please enter positive integer value less than 100 for percentage in fifth row',COLOR_ERROR);
					return false;
				}
			}else{
				displayCcEditLabelMessage('please enter a value for percentage in fifth row',COLOR_ERROR);
				return false;
			}
			
		}else if(action5 == "2"){
			
			subAction5 = "";
	    	
	    	<c:forEach items='${switchScenesList}' var='switchScenes'>
				var swgGrpName = '<c:out value="swgrp_${switchScenes.id}_5"/>';
				
				if($("input[name='" + swgGrpName + "']:checked").val() != undefined ){
					if(subAction5 == ""){
						subAction5 = $("input[name='" + swgGrpName + "']:checked").val();
					}else{
						subAction5 = subAction5 + "," + $("input[name='" + swgGrpName + "']:checked").val();
					}
				}
				
	    	</c:forEach>
	    	
	    	if(subAction5 == ""){
				displayCcEditLabelMessage('please select atleast one scene in fifth row',COLOR_ERROR);
				return false;
			}
			
		}else if(action5 == "4"){
			if($("#contactClosureSubAction5").val() == null){
				displayCcEditLabelMessage('please select a value for Switch Group in fifth row',COLOR_ERROR);
				return false;
			}
		}
		
		action6 = $('#contactClosureAction6').val();
		
		if($("#contactClosureSubAction6").val() == null){
			subAction6 = "";
		}else{
			subAction6 = $("#contactClosureSubAction6").val();
		}
		
		if(action6 == "1"){
			duration6 = $('#contactClosureDuration6').val();
			if(duration6 !=""){
				if(!isPositiveInteger(duration6) || duration6 == "0"){
					displayCcEditLabelMessage('Please enter positive integer value for duration in sixth row',COLOR_ERROR);
					return false;
				}
			}else{
				displayCcEditLabelMessage('please enter a value for duration in sixth row',COLOR_ERROR);
				return false;
			}
			
			percentage6 = $('#contactClosurePercentage6').val();
			if(percentage6 !=""){
				if(!isPositiveInteger(percentage6) || percentage6 == "0" || percentage6 > 100){
					displayCcEditLabelMessage('Please enter positive integer value less than 100 for percentage in sixth row',COLOR_ERROR);
					return false;
				}
			}else{
				displayCcEditLabelMessage('please enter a value for percentage in sixth row',COLOR_ERROR);
				return false;
			}
			
		}else if(action6 == "2"){
			
			
			subAction6 = "";
	    	
	    	<c:forEach items='${switchScenesList}' var='switchScenes'>
				var swgGrpName = '<c:out value="swgrp_${switchScenes.id}_6"/>';
				
				if($("input[name='" + swgGrpName + "']:checked").val() != undefined ){
					if(subAction6 == ""){
						subAction6 = $("input[name='" + swgGrpName + "']:checked").val();
					}else{
						subAction6 = subAction6 + "," + $("input[name='" + swgGrpName + "']:checked").val();
					}
				}
				
	    	</c:forEach>
	    	
	    	if(subAction6 == ""){
				displayCcEditLabelMessage('please select atleast one scene in sixth row',COLOR_ERROR);
				return false;
			}
			
		}else if(action6 == "4"){
			if($("#contactClosureSubAction6").val() == null){
				displayCcEditLabelMessage('please select a value for Switch Group in sixth row',COLOR_ERROR);
				return false;
			}
		}
		
		action7 = $('#contactClosureAction7').val();
		
		if($("#contactClosureSubAction7").val() == null){
			subAction7 = "";
		}else{
			subAction7 = $("#contactClosureSubAction7").val();
		}
		
		if(action7 == "1"){
			duration7 = $('#contactClosureDuration7').val();
			if(duration7 !=""){
				if(!isPositiveInteger(duration7) || duration7 == "0"){
					displayCcEditLabelMessage('Please enter positive integer value for duration in seventh row',COLOR_ERROR);
					return false;
				}
			}else{
				displayCcEditLabelMessage('please enter a value for duration in seventh row',COLOR_ERROR);
				return false;
			}
			
			percentage7 = $('#contactClosurePercentage7').val();
			if(percentage7 !=""){
				if(!isPositiveInteger(percentage7) || percentage7 == "0" || percentage7 > 100){
					displayCcEditLabelMessage('Please enter positive integer value less than 100 for percentage in seventh row',COLOR_ERROR);
					return false;
				}
			}else{
				displayCcEditLabelMessage('please enter a value for percentage in seventh row',COLOR_ERROR);
				return false;
			}
			
		}else if(action7 == "2"){
						
			subAction7 = "";
	    	
	    	<c:forEach items='${switchScenesList}' var='switchScenes'>
				var swgGrpName = '<c:out value="swgrp_${switchScenes.id}_7"/>';
				
				if($("input[name='" + swgGrpName + "']:checked").val() != undefined ){
					if(subAction7 == ""){
						subAction7 = $("input[name='" + swgGrpName + "']:checked").val();
					}else{
						subAction7 = subAction7 + "," + $("input[name='" + swgGrpName + "']:checked").val();
					}
				}
				
	    	</c:forEach>
	    	
	    	if(subAction7 == ""){
				displayCcEditLabelMessage('please select atleast one scene in seventh row',COLOR_ERROR);
				return false;
			}
			
		}else if(action7 == "4"){
			if($("#contactClosureSubAction7").val() == null){
				displayCcEditLabelMessage('please select a value for Switch Group in seventh row',COLOR_ERROR);
				return false;
			}
		}
		
		action8 = $('#contactClosureAction8').val();
		
		if($("#contactClosureSubAction8").val() == null){
			subAction8 = "";
		}else{
			subAction8 = $("#contactClosureSubAction8").val();
		}
		
		if(action8 == "1"){
			duration8 = $('#contactClosureDuration8').val();
			if(duration8 !=""){
				if(!isPositiveInteger(duration8) || duration8 == "0"){
					displayCcEditLabelMessage('Please enter positive integer value for duration in eigth row',COLOR_ERROR);
					return false;
				}
			}else{
				displayCcEditLabelMessage('please enter a value for duration in eigth row',COLOR_ERROR);
				return false;
			}
			
			percentage8 = $('#contactClosurePercentage8').val();
			if(percentage8 !=""){
				if(!isPositiveInteger(percentage8) || percentage8 == "0" || percentage8 > 100){
					displayCcEditLabelMessage('Please enter positive integer value less than 100 for percentage in eigth row',COLOR_ERROR);
					return false;
				}
			}else{
				displayCcEditLabelMessage('please enter a value for percentage in eigth row',COLOR_ERROR);
				return false;
			}
			
		}else if(action8 == "2"){
			
			subAction8 = "";
	    	
	    	<c:forEach items='${switchScenesList}' var='switchScenes'>
				var swgGrpName = '<c:out value="swgrp_${switchScenes.id}_8"/>';
				
				if($("input[name='" + swgGrpName + "']:checked").val() != undefined ){
					if(subAction8 == ""){
						subAction8 = $("input[name='" + swgGrpName + "']:checked").val();
					}else{
						subAction8 = subAction8 + "," + $("input[name='" + swgGrpName + "']:checked").val();
					}
				}
				
	    	</c:forEach>
	    	
	    	if(subAction8 == ""){
				displayCcEditLabelMessage('please select atleast one scene in eigth row',COLOR_ERROR);
				return false;
			}
			
		}else if(action8 == "4"){
			if($("#contactClosureSubAction8").val() == null){
				displayCcEditLabelMessage('please select a value for Switch Group in eigth row',COLOR_ERROR);
				return false;
			}
		}
		
		inputContactClosureVo = '{"contactClosureVo":[{"macAddress":"' +macAddress+ '","ipAddress":"' +ipAddress+ '","productId":"' +productId+ '","hwType":"' 
														+hwType+ '","fwVersion":"' +fwVersion+ '","contactClosureControlsList":[{"name":"' 
														+ name1 + '","action":"' + action1 + '","subAction":"' + subAction1 + '","duration":"' + duration1  +'","percentage":"' + percentage1  +'"},{"name":"' 
														+ name2 + '","action":"' + action2 + '","subAction":"' + subAction2 + '","duration":"' + duration2  +'","percentage":"' + percentage2  +'"},{"name":"' 
														+ name3 + '","action":"' + action3 + '","subAction":"' + subAction3 + '","duration":"' + duration3  +'","percentage":"' + percentage3  +'"},{"name":"'
														+ name4 + '","action":"' + action4 + '","subAction":"' + subAction4 + '","duration":"' + duration4  +'","percentage":"' + percentage4  +'"},{"name":"'
														+ name5 + '","action":"' + action5 + '","subAction":"' + subAction5 + '","duration":"' + duration5  +'","percentage":"' + percentage5  +'"},{"name":"'
														+ name6 + '","action":"' + action6 + '","subAction":"' + subAction6 + '","duration":"' + duration6  +'","percentage":"' + percentage6  +'"},{"name":"'
														+ name7 + '","action":"' + action7 + '","subAction":"' + subAction7 + '","duration":"' + duration7  +'","percentage":"' + percentage7  +'"},{"name":"'
														+ name8 + '","action":"' + action8 + '","subAction":"' + subAction8 + '","duration":"' + duration8  +'","percentage":"' + percentage8  +'"}]}]}';
	
		</c:if>
		
		$.ajax({
	 		type: 'POST',
	 		url: "${saveContactClosureUrl}?ts="+new Date().getTime(),
	 		contentType: "application/json",
	 		//data: '{"contactClosureVo":[{"macAddress":"' +macAddress+ '","ipAddress":"' +ipAddress+ '","productId":"' +productId+ '","hwType":"' +hwType+ '","fwVersion":"' +fwVersion+ '","contactClosureControlsList":[{"name":"' + name1 + '","action":"' + action1 + '","subAction":"' + subAction1 + '","duration":"' + duration1  +'"},{"name":"' + name2 + '","action":"' + action2 + '","subAction":"' + subAction2 + '","duration":"' + duration2  +'"},{"name":"' + name3 + '","action":"' + action3 + '","subAction":"' + subAction3 + '","duration":"' + duration3  +'"},{"name":"' + name4 + '","action":"' + action4 + '","subAction":"' + subAction4 + '","duration":"' + duration4  +'"}]}]}', 
	 		data: inputContactClosureVo,
	 		dataType: "json",
	 		success: function(data){
				displayCcEditLabelMessage('Contact Closure Settings successfully saved.',COLOR_SUCCESS);
			},
			error: function(){
				displayCcEditLabelMessage('Error.',COLOR_ERROR);
			}
	 	});
	}
    
    function isPositiveInteger(n) {
        return 0 === n % (!isNaN(parseFloat(n)) && 0 <= ~~n);
    }
    
    function displayCcEditLabelMessage(Message, Color) {
		$("#contact_closure_message_id").html(Message);
		$("#contact_closure_message_id").css("color", Color);
	}
    function clearCcEditLabelMessage(Message, Color) {
		displayCcEditLabelMessage("", COLOR_DEFAULT);
	}

</script>

<div class="outermostdiv">


<div class="innerdiv"">

<div style="height:5px;"></div>
	
<div id="contact_closure_message_id" style="font-size: 14px; font-weight: bold;padding: 5px 0 0 5px;" ></div>

<div style="height:5px;"></div>

	<div id="contactClouseDetailsId1" style="font-size: 14px; font-weight: bold;padding: 5px 0 0 5px;" >
	MAC Address : <span id="macAddress"></span> ,  IP Address : <span id="ipAddress"></span>
	</div>

	<table id="contact_closure_list" class="entable" style="width: 100%">
		<thead>
			<tr>
				<th align="center" style="width: 10%" >Contact Closure</th>
				<th align="center" style="width: 40%" >Action</th>
				<th align="center" style="width: 30%" >Switch Group</th>
				<th align="center" style="width: 10%" >Percentage( 1-100 )</th>
				<th align="center" style="width: 10%" >Duration( In Minutes )</th>
			</tr>
		</thead>
		
		<c:forEach var="inputNumber" begin="1" end="${contactClosureListSize}">
			<tr>
			<td ><b>Input <c:out value="${inputNumber}"/></b></td>	
			<td ><select id='<c:out value="contactClosureAction${inputNumber}"/>'>
					<option value="0">Do Nothing</option>
					<option value="1">Set all fixtures in Energy Manager to %</option>
					<option value="2">Set all fixtures in Switch Group to Scene</option>
					<option value="3">Set all fixtures in Energy Manager to Auto mode</option>
					<option value="4">Set all fixtures in Switch Group to Auto mode</option>
				 </select>
			</td>
			<td id='<c:out value="contactClosureTdSubAction${inputNumber}"/>'>
			</td>
			<td ><input id='<c:out value="contactClosurePercentage${inputNumber}"/>' size="10" type="text" /></td>
			<td ><input id='<c:out value="contactClosureDuration${inputNumber}"/>' size="10" type="text" /></td>
			</tr>
		</c:forEach>
		
	</table>
	<div style="height:5px;"></div>
	<button style="padding-left:5px;" id="saveContactClosure"  onclick="saveContactClosureSettings();">Save</button>
</div>


</div>
