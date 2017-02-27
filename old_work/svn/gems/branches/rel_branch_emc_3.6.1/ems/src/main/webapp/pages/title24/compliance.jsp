<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<spring:url value="/services/org/utility/generatetitle24report"	var="reportURL"></spring:url> 

<div id="outerTitle24Container">

<!-- 
<spring:url value="/scripts/jquery/jquery-1.11.0.min.js" var="jquery1110"></spring:url><script type="text/javascript" src="${jquery1110}"></script>
<spring:url value="/scripts/jquery/jqgrid/5.0.2/js/i18n/grid.locale-en.js" var="jqGridLocale502"></spring:url><script type="text/javascript" src="${jqGridLocale502}"></script>
<spring:url value="/scripts/jquery/jqgrid/5.0.2/js/jquery.jqGrid.min.js" var="jqGrid502"></spring:url><script type="text/javascript" src="${jqGrid502}"></script>
<spring:url value="/scripts/jquery/jqgrid/5.0.2/css/ui.jqgrid.css" var="jqgridUICss502" /><link rel="stylesheet" type="text/css" href="${jqgridUICss502}" />
<spring:url value="/scripts/jquery/jqgrid/5.0.2/themes/jqModal.css" var="jqgridjqModal502" /><link rel="stylesheet" type="text/css" href="${jqgridjqModal502}" />
<spring:url value="/scripts/enlighted/title24.js" var="title24js"></spring:url><script type="text/javascript" src="${title24js}"></script>
<spring:url value="/scripts/jquery/jquery-ui.min.css" var="jqueryuimin" /><link rel="stylesheet" type="text/css" href="${jqueryuimin}" />
<spring:url value="/scripts/jquery/jquery-ui.min.js" var="jqueryuijs"></spring:url><script type="text/javascript" src="${jqueryuijs}"></script>
<spring:url value="/themes/title24/style.css" var="title24stylecss" /><link rel="stylesheet" type="text/css" href="${title24stylecss}" />
<spring:url value="/themes/title24/title24.css" var="title24css" /><link rel="stylesheet" type="text/css" href="${title24css}" />
 -->

<style type="text/css">
   
    body {
    background-color: #eef;
    }
    #tabs {
    width: 95%;
    margin-left: auto;
    margin-right: auto;
    margin-top: 10px;
    }
    /* Note that jQuery UI CSS is loaded from Google's CDN in the "Add Resources" pane to the left.  Normally this would be done in your
    <head>
        */
    
</style>


<script type='text/javascript'>//<![CDATA[
            $(window).load(function(){
            $("#tabs").tabs({
                activate: function (event, ui) {
                    var active = $('#tabs').tabs('option', 'active');
                    //$("#tabid").html('the tab id is ' + $("#tabs ul>li a").eq(active).attr("href"));

                }
            }

            );
            });//]]>

        </script>
        <script>
            function ischecked(ele) {
            	if ( $(ele).is(":checked") ){
            		return true;
            	}else{
            		return false;
            	}
            }
              $(function() {
                //$( "#accordionsystest" ).accordion();

                $( "#accordionsystest" ).accordion({
                  collapsible: true
                });
     //    var obj = $("a:visited");
   //      alert(obj);
 //obj.parent(".tabvisited").addClass('tabvisitedActive');


                  //$("#lightingControlDoc").load("LightingControlDocument.html");

                  renderSpaceDetailGrid();


            	var orgAccepted=$('input[name=acceptOrgSetting]:checked').val();

            	changeColorTab('input[name=acceptOrgSetting]:checked', "#orgLi a");
            	changeColorTab('input[name=acceptNetworkSetting]:checked', "#netLi a");
            	changeColorTab('input[name=acceptProfileSetting]:checked', "#profLi a");
            	changeColorTab('input[name=acceptSystemTest]:checked', "#systestLi a");

            	$("#systestLi .tab").addClass("tabvisitedActive");
            	$(".tab").click(function () {
    $(".tab").removeClass("tabvisitedActive");
    // $(".tab").addClass("active"); // instead of this do the below
    $(this).addClass("tabvisitedActive");
});

            	
            	 var w = $(window).width();
            	 var h = $(window).height();
            	 $('#tabs').css('width', 0.9 * w);
            	 //$('.accordianContentCustom').css('height', 0.4 * h);
            	 //$('div[id^=ui-id-]').css('height', 0.4 * h);

              });

              $(function() {
            	  $('#orgAcceptForm #submit').click(function(){
            	     $("#netLi a").click();
            	  });

            	  $('#netSettingsForm #prev').click(function(){
            	     $("#orgLi a").click();
            	  });

            	  $('#netSettingsForm #next').click(function(){
            	     $("#profLi a").click();
            	  });

                  $('#profileSettingsForm #prev').click(function(){
            	     $("#netLi a").click();
            	  });

            	  $('#profileSettingsForm  #next').click(function(){
            	     $("#systestLi a").click();
            	  });


            	  $('#systestForm #prev').click(function(){
            	     $("#profLi a").click();
            	  });

            	  $('#systestForm #next').click(function(){
            	     alert('Form getting submitted to backend ...');
            	  });

            });
              
              function rendertitle24PDF(){
            	  window.open("${reportURL}");
              }
            function changeColorTab(elementinput, elementtochange){
              	var val = $(elementinput).val();
              	$(elementtochange).removeClass('tabcolordefault');
            	$(elementtochange).removeClass('tabcolorgreen');
            	$(elementtochange).removeClass('tabcolorred');
              	if (val == null || val == 'undefined'){
              	   	$(elementtochange).addClass('tabcolordefault');
              	}else if( val == 'Yes'){
              		$(elementtochange).addClass('tabcolorgreen');
              	}else{
              		$(elementtochange).addClass('tabcolorred');
              	}

              }
            
            function setTestResult(ele,testid,nooftests){
            	var name = ele.name;
            	var testname = testid;
            	var val = [];
            	$('input[name="'+name+'"]:checked').each(function(i){
                   val[i] = $(this).val();
                });
            	if(val!=null && val.length!="undefined" && nooftests!=null && nooftests!="undefined" ){
            		if(val.length == nooftests){
                		document.getElementById(testname).value = "Pass";
                		doEvaluation();
                	} else {
                		document.getElementById(testname).value = "Fail";
                		doEvaluation();
                	}
            	}
            }
            
            function doEvaluation(){
            	var t1 = $("#aconstinsp").val();
            	$("#aconstinspBean").val(t1);
            	var t2 = $("#olcftmotion").val();
            	$("#olcftmotionBean").val(t2);
            	var t3 = $("#olcftnomotion").val();
            	$("#olcftnomotionBean").val(t3);
            	var t4 = $("#olccidayoff").val();
            	$("#olccidayoffBean").val(t4);
            	var t5 = $("#olccinightoff").val();
            	$("#olccinightoffBean").val(t5);
            	var t6 = $("#olclscriteriameet").val();
            	$("#olclscriteriameetBean").val(t6);
            	var t7 = $("#olcftdayoff").val();
            	$("#olcftdayoffBean").val(t7);
            	if(
            			(t1 != undefined && t1 != null && t1 == 'Pass') &&
            			(t2 != undefined && t2 != null && t2 == 'Pass') &&
            			(t3 != undefined && t3 != null && t3 == 'Pass') &&
            			(t4 != undefined && t4 != null && t4 == 'Pass') &&
            			(t5 != undefined && t5 != null && t5 == 'Pass') &&
            			(t6 != undefined && t6 != null && t6 == 'Pass') &&
            			(t7 != undefined && t7 != null && t7 == 'Pass') 
            			){
            		//Set value to pass
            		document.getElementById('complienceTestEvaluation').value = "Pass";
            		$("#complienceTestEvaluationBean").val('Pass');
            	}else{
            		//set value to Fail
            		document.getElementById('complienceTestEvaluation').value = "Fail";
            		$("#complienceTestEvaluationBean").val('Fail');
            	}
            }

        </script>




<spring:url value="/title24/complianceUpdate.ems" var="actionURL" scope="request"/>
<form:form id="systestForm" method="POST" commandName="title24" action="${actionURL}">


<div id="tabs">
            <ul class="title24tabul">
                <li id="systestLi" ><a class="tab" href="#tabs-4">Compliance Tests</a></li>
                <li id="compRepLi" ><a class="tab"  href="#tabs-5" >Compliance Reports</a></li>
            </ul>
            
            <div id="tabs-4">
                
                    <div id="accordionsystest">
                        <h3 class="accordianHeaders">Lighting Control Acceptance</h3>
                        
                        <div>
                            <div id="controlLightingDiv" class="accordianContentCustom">
                                <div style="position:relative;left:50.40px;" ><span style="font-size:16px" ><strong>A.  Construction Inspection</strong></span></div>
                                <div style="position:relative;left:50.40px; padding-top:12px;" ><span >Fill out Section A to cover spaces 1 through 3 that are functionally tested under Section B.  Make as many copies of pages 2-5 as</span></div>
                                <div style="position:relative;left:50.46px;padding-top:12px;" ><span >are required to test all spaces in the building, and attach to page 1.</span></div>
                                <div style="position:relative;left:50.49px;padding-top:12px;" ><span >Instruments needed to perform tests include, but are not limited to: hand-held amperage meter, power meter, or light meter</span></div>
                                <div style="position:relative;left:55.56px;padding-top:12px; float:left" ><span >1</span></div>
                                <div style="position:relative;left:76.62px;padding-top:12px;"  ><span >Automatic Time Switch Controls Construction Inspection&mdash;confirm for all listed in Section B</span></div>
                                <div style="position:relative;left:85.81px;padding-top:12px; float:left" ><span >a.</span></div>
                                <div style="position:relative;left:113.17px;padding-top:12px;" ><span >All automatic time switch controls are programmed for (check all):</span></div>
                                
								
								<div class="springmulticheckboxdiv">
	                                <div  class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="lightcontrolaccepatance.autotimeswitchcontrolprogrammed" value="1"/></span></div>
									<div class="springcheckboxlabeldiv"><span >Weekdays</span></div>
									<div  class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="lightcontrolaccepatance.autotimeswitchcontrolprogrammed" value="2"/></span></div>
									<div  class="springcheckboxlabeldiv"><span >Weekend</span></div>
									<div  class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="lightcontrolaccepatance.autotimeswitchcontrolprogrammed" value="3"/></span></div>
									<div  class="springcheckboxlabeldiv"><span >Holidays</span></div>
  								</div>				
				
								<div class="clearboth">
	                                <div style="position:relative;left:85.81px;padding-top:12px;float:left;" class="bpointstyle" ><span >b.</span></div>
	                                <div style="position:relative;left:113.17px;padding-top:12px;" ><span >Document for the owner automatic time switch programming (check all):</span></div>
	                                <div class="springmulticheckboxdiv">
		                                <div class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="lightcontrolaccepatance.ownerautoswitchprogramming" value="1"/></span></div>
		                                <div class="springcheckboxlabeldiv" ><span >Weekdays settings</span></div>
		                                <div class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="lightcontrolaccepatance.ownerautoswitchprogramming" value="2"/></span></div>
		                                <div class="springcheckboxlabeldiv" ><span >Weekend settings</span></div>
		                                <div class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="lightcontrolaccepatance.ownerautoswitchprogramming" value="3"/></span></div>
		                                <div class="springcheckboxlabeldiv" ><span >Holidays settings</span></div>
		                                <div class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="lightcontrolaccepatance.ownerautoswitchprogramming" value="4"/></span></div>
		                                <div class="springcheckboxlabeldiv" ><span >Set-up settings</span></div>
		                                <div class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="lightcontrolaccepatance.ownerautoswitchprogramming" value="5"/></span></div>
		                                <div class="springcheckboxlabeldiv" ><span >Preference program setting</span></div>
		                                <div class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="lightcontrolaccepatance.ownerautoswitchprogramming" value="6"/></span></div>
		                                <div class="springcheckboxlabeldiv" ><span >Verify the correct time and date is properly set in the time switch</span></div>
		                                <div class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="lightcontrolaccepatance.ownerautoswitchprogramming" value="7"/></span></div>
		                                <div class="springcheckboxlabeldiv" ><span >Verify the battery is installed and energized</span></div>
		                                <div class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="lightcontrolaccepatance.ownerautoswitchprogramming" value="8"/></span></div>
		                                <div class="springcheckboxlabeldiv" ><span >Override time limit is no more than 2 hours</span></div>
		                                <div class="springcheckboxlabeldiv" ><span >Occupant Sensors and Automatic Time Switch Controls have been certified to the Energy Commission in</span></div>
		                                <div class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="lightcontrolaccepatance.ownerautoswitchprogramming" value="9"/></span></div>
		                                <div class="springcheckboxlabeldiv" ><span >accordance with the applicable provision in Section 110.9 of the Standards, and model numbers for all such</span></div>
		                                <div class="springcheckboxlabeldiv" ><span >controls are listed on the Commission database as Certified Appliance and Control Devices</span></div>
	                                </div>
                                </div>
                                
                                <div style="position:relative;left:55.56px;padding-top:12px;" class=" floatLeft"><span >2</span></div>
                                <div style="position:relative;left:76.62px;padding-top:12px;" ><span >Occupancy Sensor Construction Inspection&dash;confirm  for all listed in Section B</span></div>
                                
                                <div class="springmulticheckboxdiv">
	                                <div class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="lightcontrolaccepatance.occsensorconstructioninspection" value="1"/></span></div>
	                                <div class="springcheckboxlabeldiv"  ><span >Occupancy sensors are not located within four feet of any HVAC diffuser</span></div>
	                                <div class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="lightcontrolaccepatance.occsensorconstructioninspection" value="2"/></span></div>
	                                <div class="springcheckboxlabeldiv"  ><span >Ultrasonic occupancy sensors do not emit audible sound 5 feet from source</span></div>
                                </div>
                                
                                <div style="position:relative;left:50.40px;padding-top:12px;" ><span ></span></div>
                                <br/>
                                <div style="position:relative;left:50.40px;" id="controlLightingDivjqgrid1">
                                    <table id="spaceDetailTable"></table>
                                    <div id="spaceDetailDiv"></div>
                                    <br />
                                    <div id="controlLightingDivjqgrid1buttons">
                                        <input type="hidden" id="spaceDetailTableEdit"  value="Edit row" />
                                        <input type="button" id="spaceDetailTableSave" disabled="true" value="Save Space" />
                                        <input type="button" id="spaceDetailTableAdd" value="Add Space" />
                                    </div>
                                    <br />
                                    <table id="timeSwitchControlTable"></table>
                                    <table id="timeSwitchControlTableDiv"></table>
                                    <br/>
                                    <table id="testResultTable"></table>
                                </div>
                                <div style="position:relative;left:50.40px;" class="top12">
                                
                                <table border="0" cellpadding="0" cellspacing="0" style="width:100%">
									<tbody>
										<tr >
											<td colspan="1" style="width:3%;padding-bottom:15px"><span style="font-size:16px" ><strong>D.<br/></strong></span></td>
											<td colspan="1" style="width:97%;padding-bottom:15px"><span style="font-size:16px" ><strong>Evaluation</strong></span></td>
										</tr>
										<tr >
											<td><span ><form:checkbox cssClass="" path="lightcontrolaccepatance.evaluation" value="1"/></td>
											<td><span ><p>PASS: All applicable <strong>Construction Inspection</strong> responses are complete and all applicable <strong>Equipment Testing Requirements</strong>&nbsp;responses are positive (Y - yes)</p></span></td>
										</tr>
									</tbody>
								</table>
                                
                                </div>
                                 <div id="accordianSubmitDiv" style="padding-top:20px;">
		                            <div class="field">
		                                <div class="formPrompt"><span></span></div>
		                                <div class="formValue"><input class="saveAction" id="accordianLightControlSubmit" type="button" value="Submit"/></div>
		                            </div>
		                        </div>
                            </div>
                        </div>
                        <h3 class="accordianHeaders">Automatic Daylight Control Acceptance</h3>
                        <div class="">
                            <div id="autoDayLightControl"  class="accordianContentCustom">
                            
                            <div style="position:relative;left:50.40px;" ><span >Check boxes for all pages of this NRCA-LTI-03-A completed and included in this submittal</span></div>
                                <div class="springmulticheckboxdivADC">
	                                <div  class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="autodlcontrolaccepatance.nrcalti03a" value="1"/></span></div>
									<div class="springcheckboxlabeldiv floatLeft" style="padding-right:20px;"><span >NRCA-LTI-03-A Page 1 & 2</span></div>
									<div class="springcheckboxlabeldiv"><span >Construction Inspection. This page required for all submittals.</span></div>
									<div  class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="autodlcontrolaccepatance.nrcalti03a" value="2"/></span></div>
									<div class="springcheckboxlabeldiv floatLeft" style="padding-right:20px;"><span >NRCA-LTI-03-A Page 3 & 4</span></div>
									<div class="springcheckboxlabeldiv"><span >Continuous dimming control functional performance test &dash; watt-meter or amp-meter measurement</span></div>
									<div  class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="autodlcontrolaccepatance.nrcalti03a" value="3"/></span></div>
									<div class="springcheckboxlabeldiv floatLeft" style="padding-right:20px;"><span >NRCA-LTI-03-A Page 5 & 6</span></div>
									<div class="springcheckboxlabeldiv"><span >Stepped Switching/ Stepped Dimming functional performance test &dash; watt-meter or amp-meter measurement</span></div>
									<div  class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="autodlcontrolaccepatance.nrcalti03a" value="4"/></span></div>
									<div class="springcheckboxlabeldiv floatLeft" style="padding-right:20px;"><span >NRCA-LTI-03-A Page 7 & 8</span></div>
									<div class="springcheckboxlabeldiv"><span >Continuous dimming control functional performance test &dash; light meter power measurement, and default look-up table of fraction of rated power versus fraction of rated light output.</span></div>
									<div  class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="autodlcontrolaccepatance.nrcalti03a" value="5"/></span></div>
									<div class="springcheckboxlabeldiv floatLeft" style="padding-right:20px;"><span >NRCA-LTI-03-A Page 10 & 11</span></div>
									<div class="springcheckboxlabeldiv"><span >Stepped Switching/ Stepped Dimming functional performance test &dash; based on light output</span></div>
  								</div>
  								<div style="position:relative;left:51.30px;" class="top12 mainHeadPoint"><span class="cls_015">I. Construction Inspection NA-7.6.1.1</span></div>
								<div style="position:relative;left:51.30px;" class="top12 subHeadPoint"><span class="cls_016">1   Drawing of Daylit Zone(s) must be shown on plans or attached to this form. Select one or both of the following:</span></div>
								<div class="springmulticheckboxdivADC">
		                                <div  class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="autodlcontrolaccepatance.drawdlzone" value="1"/></span></div>
										<div class="springcheckboxlabeldiv floatLeft" style="padding-right:20px;"><span >Shown on plans page #'s</span><form:input path="autodlcontrolaccepatance.drawdlzoneshownpages" cssClass="underline" value=""/></div>
										<div  class="springcheckboxdiv floatLeft"><span ><form:checkbox cssClass="" path="autodlcontrolaccepatance.drawdlzone" value="2"/></span></div>
										<div class="springcheckboxlabeldiv floatLeft" style="padding-right:20px;"><span >Daylit zones(s) drawn in on as-built plans (attached) page #'s</span><form:input path="autodlcontrolaccepatance.drawdlzonebuiltplanpages" cssClass="underline" value=""/></div>
	  							</div>			
                           
	                           	<div style="position:relative;left:51.30px;" class="clearboth top12"><span class="cls_005">Check box below if sampling method is used in accordance with NA7.6.1.2. If checked, attach a page with names of other controls</span></div>
								<div style="position:relative;left:51.41px;" class="top12"><span class="cls_005">in sample (only for buildings with > 5 daylight control systems, sample group glazing same orientation)</span></div>
                           		
	                           	<div style="position:relative;left:50.40px;" class="top12" id="autoDLControlSystemGridMain">
	                                    <table id="autoDLControlSystemGrid"></table>
	                                    <div id="autoDLControlSystemGridDiv"></div>
	                                    <br />
	                                    <div id="autoDLControlSystemDependents">
	                                        <input type="button" id="autoDLControlSystemGridSave" disabled="true" value="Save row" />
	                                        <input type="button" id="autoDLControlSystemGridAdd" value="Add row" />
	                                    </div>
	                                    <br />
	                                    <table id="autoDLSensroControlGrid"></table>
	                                  	<br />
	                                  	<table id="functionalTestingCDSGrid"></table>
	                                  	<br />
	                                  	<table id="functionalTestingSDSGrid"></table>
	                                  	<br />
	                                  	<table id="functionalTestingCDSLMMGrid"></table>
	                                  	<br />
	                                  	<table id="functionalTestingSDSLMMGrid"></table>
	                                  	<br />
	                             </div>
	                             
	                             <div id="accordianSubmitDiv" style="padding-top:20px;">
		                            <div class="field">
		                                <div class="formPrompt"><span></span></div>
		                                <div class="formValue"><input class="saveAction" id="accordianAutoDayLightControlSubmit" type="button" value="Submit"/></div>
		                            </div>
		                        </div>
                           </div>
                            
                            
                        </div>
                        <h3  class="accordianHeaders">Demand Responsive Lighting Control Acceptance</h3>
                        <div class="">
                            <div id="drlightControl"  class="accordianContentCustom">
                            		

<table border="0" cellpadding="0" cellspacing="10" style="padding-left:10px;" >
	<tbody>
		<tr>
			<td colspan="4"><strong>Demand Responsive Lighting Control</strong></td>
		</tr>
		<tr>
			<td colspan="2" rowspan="1"><strong>Intent:</strong></td>
			<td colspan="2" rowspan="1">Test the reduction in lighting power due to the demand responsive lighting control as per Sections110.9(a), 130.1(e) and 130.5(e).</td>
		</tr>
		<tr>
			<td colspan="4"><strong>NA7.6.3 Acceptance tests for Demand Responsive Lighting Controls in accordance with Section 130.1(e)</strong></td>
		</tr>
		<tr>
			<td>&nbsp;1</td>
			<td colspan="3" rowspan="1">Instrumentation to perform test includes, but not limited to:</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td>&nbsp;a.&nbsp;</td>
			<td colspan="2" rowspan="1">Hand&dash;held amperage and voltage meter</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td>&nbsp;b.</td>
			<td colspan="2" rowspan="1">Power meter</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td>&nbsp;c.</td>
			<td colspan="2" rowspan="1">Light meter</td>
		</tr>
		<tr>
			<td>&nbsp;2&nbsp;</td>
			<td colspan="3" rowspan="1">Construction Inspection</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="dracceptance.constructioninspection" value="1"/></td>
			<td colspan="2" rowspan="1">Verify the demand responsive control is capable of receiving a demand response signal directly or indirectly&nbsp;through another device and that it complies with the requirements in Section 130.5(e).</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>130.5(e)</td>
			<td>Demand responsive controls and equipment shall be capable of receiving and automatically responding to at least one standards based messaging protocol which enables demand response after receiving a demand response signal.</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>Definition</td>
			<td>DEMAND RESPONSE SIGNAL is a signal sent by the local utility, Independent System Operator (ISO), or designated curtailment service provider or aggregator, to a customer, indicating a price or a request to modify electricity consumption, for a limited time period.</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="dracceptance.constructioninspection" value="2"/></td>
			<td colspan="2" rowspan="1">If the demand response signal is received from another device (such as an EMCS), that system must itself becapable of receiving a demand response signal from a utility meter or other external source.</td>
		</tr>
		<tr>
			<td colspan="4">&nbsp;</td>
		</tr>
		<tr>
			<td colspan="4">NA7.6.3.2 Functional Test</td>
		</tr>
		<tr>
			<td><form:checkbox cssClass="" path="dracceptance.functionaltestcheckbox" value="1"/></td>
			<td>&nbsp;1</td>
			<td colspan="2" rowspan="1">Use either Method 1 (illuminance measurement) or Method 2 (power input measurement) to perform the functional test.</td>
		</tr>
		<tr>
			<td><form:checkbox cssClass="" path="dracceptance.functionaltestcheckbox" value="2"/></td>
			<td>&nbsp;2</td>
			<td colspan="2" rowspan="1">Test building&dash;wide reduction in lighting power to at least 15% below the maximum total lighting power, as calculated on an area&dash;weighted basis (measured in illuminance or power). However, any single space must not reduce the combined illuminance from daylight and electric light to less than 50% of the design illuminance.</td>
		</tr>
		<tr>
			<td><form:checkbox cssClass="" path="dracceptance.functionaltestcheckbox" value="3"/></td>
			<td>&nbsp;3</td>
			<td colspan="2" rowspan="1">For buildings with up to seven (7) enclosed spaces requiring demand responsive lighting controls, all spaces shall be tested.</td>
		</tr>
		<tr>
			<td><form:checkbox cssClass="" path="dracceptance.functionaltestcheckbox" value="4"/></td>
			<td>&nbsp;4</td>
			<td colspan="2" rowspan="1">For buildings with more than seven (7) enclosed spaces requiring demand responsive lighting controls, sampling may be done on additional spaces with similar lighting systems. If the first enclosed space with a demand responsive lighting control in the sample group passes the acceptance test, the remaining building spaces in the sample group also pass. If the first enclosed space with a demand responsive lighting control in the sample group fails the acceptance test the rest of the enclosed spaces in that group must be tested.</td>
		</tr>
		<tr>
			<td><form:checkbox cssClass="" path="dracceptance.functionaltestcheckbox" value="5"/></td>
			<td>&nbsp;5</td>
			<td colspan="2" rowspan="1">If any tested demand responsive lighting control system fails it shall be repaired, replaced or adjusted until it passes the test.</td>
		</tr>
	</tbody>
</table>

<table border="0" cellpadding="0" cellspacing="10" style="padding-left:10px;"  class="top12" >
	<tbody>
		<tr>
			<td><form:checkbox cssClass="" path="dracceptance.drtest" value="1"/>
			</td>
			<td colspan="1" rowspan="1">Trigger the DR test</td>
		</tr>
	</tbody>
</table>									
									
									<div style="position:relative;left:50.40px;" class="top12" id="autoDLControlSystemGridMain">
										<br />
	                                    <table id="drSpaceGrid"></table>
	                                    <br />
	                                    <div id="drSpaceButtonDiv">
	                                        <input type="button" id="drSpaceGridSave" disabled="true" value="Save Space/Circuit" />
	                                        <input type="button" id="drSpaceGridAdd" value="Add Space/Circuit" />
	                                    </div>
	                                    
	                                    <div id="drSpaceGridDiv"></div>
	                                    <br />
	                                    
	                                    <div class="springmulticheckboxdivADC" >
	                                    <div  class="springcheckboxdiv floatLeft"><span >Method of Measurement</span></div>
											<div class="springcheckboxlabeldiv" style="padding-left:50.40px;">
														<span style="padding-left:50.40px;">
															<form:select id="dracceptancemethodofmeasurement" path="dracceptance.methodofmeasurement">
															    <form:option value="Method1">Method 1</form:option>
															    <form:option value="Method2">Method 2</form:option>
														    </form:select>
													    </span>
										    </div>
  										</div>
	                                    
	                                    
	                                    <br/>
	                                    <div id="drMethod1Div">
		                                    <table id="drMethod1Grid"></table>
		                                  	<br />
	                                  	</div>
	                                  	<div id="drMethod2Div">
		                                  	<table id="drMethod2Grid"></table>
		                                  	<br />
	                                  	</div>
									</div>
									
									<div class="field">
			                                <div class="formPrompt"><span></span></div>
			                                <div class="formValue"><input class="saveAction" id="accordianDRControlSubmit" type="button" value="Submit"/></div>
			                         </div>
			                            
                            </div>
                        </div>
                        <h3  class="accordianHeaders">Outdoor Lighting Control Acceptance</h3>
                        <div class="">
                        	<div id="outdoorLightControl"  class="accordianContentCustom">
                        		<div  >

	<table border="0" cellpadding="0" cellspacing="10px" style="padding-left:20px;padding-top:0px;"  >
	<tbody>
		<tr>
			<td width="100%" colspan="3"><strong>NA7.8.1.2 Outdoor Motion Sensor Acceptance</strong></td>
		</tr>
		
		<tr>
			<td width="4%"><strong>Intent:</strong></td>
			<td width="96%" colspan="2" rowspan="1">Luminaires that can accept an incandescent lamp (for instance, screw&dash;base fixtures) rated over 100W are controlled with a motion sensor per Section 130.2(a).<br />
			Luminaires mounted 24 feet or below are controlled with a motion sensor per Section 130.2(c)3A </td>
		</tr>
		<tr>
			<td colspan="3"><strong>A. Construction Inspection</strong></td>
		</tr>
		<tr>
			<td >&nbsp;1.</td>
			<td colspan="2" rowspan="1">Motion Sensor Construction Inspection</td>
		</tr>
		<tr>
			<td width="3%">&nbsp;</td>
			<td width="3%"><form:checkbox cssClass="" path="olc.constructioninspection" value="1" onchange="setTestResult(this,'aconstinsp',5);" /></td>
			<td width="94%">Motion sensor has been located to minimize false signals</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.constructioninspection" value="2" onchange="setTestResult(this,'aconstinsp',5);" /></td>
			<td>Sensor is not triggered by motion outside of controlled area</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.constructioninspection" value="3" onchange="setTestResult(this,'aconstinsp',5);" /></td>
			<td>Desired motion sensor coverage is not blocked by obstruction that could adversely affect performance</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.constructioninspection" value="4" onchange="setTestResult(this,'aconstinsp',5);" /></td>
			<td>The lighting power of each luminaire is set to reduce by atleast 40 percent but no more than 80 percent, in the unoccupied condition</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.constructioninspection" value="5" onchange="setTestResult(this,'aconstinsp',5);"/></td>
			<td>No more than 1,500 watts of lighting power is controlled together, by the same sensor or group of sensors</td>
		</tr>
		<tr>
			<td colspan="2" rowspan="1">&nbsp;Test Result</td>
			<td>
				<select id="aconstinsp" name="aconstinsp" disabled="disabled" >
  					<option value="Pass" <c:if test="${title24.olc.ciatest == 'Pass'}"> selected="selected" </c:if> >Pass</option>
  					<option value="Fail" <c:if test="${title24.olc.ciatest == 'Fail'}"> selected="selected" </c:if> >Fail</option>
  				</select>
  				<form:input id="aconstinspBean" type="hidden" path="olc.ciatest"/>
  			</td>
		</tr>
		<tr>
			<td colspan="3"><strong>B. Functional testing</strong></td>
		</tr>
		<tr>
			<td>&nbsp;1.</td>
			<td colspan="2" rowspan="1">Simulate motion of a pedestrian in area under lights controlled by the motion sensor. Verify and document the following:</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.ftmotion" value="1" onchange="setTestResult(this,'olcftmotion',3);" /></td>
			<td>Status indicator operates correctly.</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.ftmotion" value="2" onchange="setTestResult(this,'olcftmotion',3);" /></td>
			<td>Lights controlled by motion sensors turn on immediately upon entry into the area lit by the controlled lights near the motion sensor</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.ftmotion" value="3" onchange="setTestResult(this,'olcftmotion',3);" /></td>
			<td>Signal sensitivity is adequate to achieve desired control</td>
		</tr>
		<tr>
			<td colspan="2" rowspan="1">&nbsp;Test Result</td>
			<td>
				<select id="olcftmotion" name="olcftmotion" disabled="disabled" >
  					<option value="Pass" <c:if test="${title24.olc.fttest1 == 'Pass'}"> selected="selected" </c:if> >Pass</option>
  					<option value="Fail" <c:if test="${title24.olc.fttest1 == 'Fail'}"> selected="selected" </c:if> >Fail</option>
  				</select>
  				<form:input id="olcftmotionBean" type="hidden" path="olc.fttest1"/>
  			</td>
		</tr>
		<tr>
			<td>&nbsp;2.</td>
			<td colspan="2" rowspan="1">Simulate no motion in area with lighting controlled by the sensor but with pedestrian motion adjacent to this area. Verify and document the following:</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.ftnomotion" value="1" onchange="setTestResult(this,'olcftnomotion',2);" /></td>
			<td>The occupant sensor does not trigger a false &ldquo;on&rdquo; from movement outside of the controlled area</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.ftnomotion" value="2" onchange="setTestResult(this,'olcftnomotion',2);" /></td>
			<td>Signal sensitivity is adequate to achieve desired control.</td>
		</tr>
		<tr>
			<td colspan="2" rowspan="1">&nbsp;Test Result</td>
			<td>
				<select id="olcftnomotion" name="olcftnomotion" disabled="disabled" >
  					<option value="Pass" <c:if test="${title24.olc.fttest2 == 'Pass'}"> selected="selected" </c:if> >Pass</option>
  					<option value="Fail" <c:if test="${title24.olc.fttest2 == 'Fail'}"> selected="selected" </c:if> >Fail</option>
  				</select>
  				<form:input id="olcftnomotionBean" type="hidden" path="olc.fttest2"/>
  			</td>
		</tr>
		<tr>
			<td colspan="3"><strong>NA7.8.2 Outdoor Lighting Automatic Shut&dash;off Controls Acceptance</strong></td>
		</tr>
		<tr>
			<td colspan="3"><strong>Intent:</strong> All installed outdoor lighting shall be controlled by a photocontrol or outdoor astronomical time&dash;switch control that automatically turns OFF the outdoor lighting when daylight is available, per Section 130.2(c)1. All outdoor lighting shall also be controlled by an automatic scheduling control that automatically turns OFF the lighting outside of business hours or occupied times. Certain types of outdoor lighting shall also be controlled by motion sensor controls. Outdoor lighting shall be circuited separately from other electrical loads.</td>
		</tr>
		<tr>
			<td colspan="3"><strong>C. Construction Inspection</strong></td>
		</tr>
		<tr>
			<td>&nbsp;1.</td>
			<td colspan="2" rowspan="1">Outdoor Lighting Daytime Shut&dash;off Controls</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.cidayoff" value="1" onchange="setTestResult(this,'olccidayoff',3);" /></td>
			<td>All outdoor lighting is controlled either by a photocontrol or outdoor astronomical time&dash;switch control that automatically turns OFF the outdoor lighting when daylight is available</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.cidayoff" value="2" onchange="setTestResult(this,'olccidayoff',3);" /></td>
			<td>Astronomical time switch controls and photocontrols have been certified to the Energy Commission in accordance with the applicable provision in Standards Section 110.9. Verify that model numbers of all such controls are listed on the Energy Commission database as &ldquo;Certified Appliances &amp; Control Devices.&rdquo;</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.cidayoff" value="3" onchange="setTestResult(this,'olccidayoff',3);" /></td>
			<td>If an astronomical time switch is installed, the ON and OFF times should be within 99 minutes of sunrise and sunset. Verify that the controller is programmed with the location of the site, local date and time. Disconnect controller from power source, reconnect, and verify that all programmed settings are retained.</td>
		</tr>
		<tr>
			<td colspan="2" rowspan="1">&nbsp;Test Result</td>
			<td>
				<select id="olccidayoff" name="olccidayoff" disabled="disabled" >
  					<option value="Pass" <c:if test="${title24.olc.citest1 == 'Pass'}"> selected="selected" </c:if> >Pass</option>
  					<option value="Fail" <c:if test="${title24.olc.citest1 == 'Fail'}"> selected="selected" </c:if> >Fail</option>
  				</select>
  				<form:input id="olccidayoffBean" type="hidden" path="olc.citest1"/>
  			</td>
		</tr>
		<tr>
			<td>&nbsp;2.</td>
			<td colspan="2" rowspan="1">Outdoor Lighting Scheduling (Night&dash;Time Shut Off) Controls</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.cinightoff" value="1" onchange="setTestResult(this,'olccinightoff',4);" /></td>
			<td>All outdoor lighting is controlled by a scheduling control, which is either a time clock or astronomical time clock.</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.cinightoff" value="2" onchange="setTestResult(this,'olccinightoff',4);" /></td>
			<td>Controls are programmed with acceptable weekday, weekend, and holiday (if applicable) schedules</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.cinightoff" value="3" onchange="setTestResult(this,'olccinightoff',4);" /></td>
			<td>Controls have been certified to the Energy Commission in accordance with the applicable provision in Standards Section 110.9. Verify that model numbers of all such controls are listed on the Energy Commission database as &ldquo;Certified Appliances &amp; Control Devices.&rdquo;</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.cinightoff" value="4" onchange="setTestResult(this,'olccinightoff',4);" /></td>
			<td>Demonstrate and document for the owner time switch programming including weekday, weekend, holiday schedules as well as all set&dash;up and preference program settings</td>
		</tr>
		<tr>
			<td colspan="2" rowspan="1">&nbsp;Test Result</td>
			<td>
				<select id="olccinightoff" name="olccinightoff" disabled="disabled" >
  					<option value="Pass" <c:if test="${title24.olc.citest2 == 'Pass'}"> selected="selected" </c:if> >Pass</option>
  					<option value="Fail" <c:if test="${title24.olc.citest2 == 'Fail'}"> selected="selected" </c:if> >Fail</option>
  				</select>
  				<form:input id="olccinightoffBean" type="hidden" path="olc.citest2"/>
  			</td>
		</tr>
		<tr>
			<td>&nbsp;3.</td>
			<td colspan="2" rowspan="1">Lighting systems that meet the criteria of Section 130.2(c)4 and 5 of the Standards shall have at least one of the following:</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.lscriteriameet" value="1" onchange="setTestResult(this,'olclscriteriameet',3);" /></td>
			<td>A part&dash;night outdoor lighting control as defined in Section 100.1, which meets the functional requirements of NA7.7.1</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.lscriteriameet" value="2" onchange="setTestResult(this,'olclscriteriameet',3);" /></td>
			<td>Motion sensors capable of automatically reducing lighting power by at least 40 percent but not exceeding 80 percent, which have auto&dash;ON functionality, and which meets the requirements of NA7.7.1</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.lscriteriameet" value="3" onchange="setTestResult(this,'olclscriteriameet',3);" /></td>
			<td>A centralized time&dash;based zone lighting control capable of automatically reducing lighting power by at least<br />
			50 percent. This control shall be certified to the Commission in accordance with the applicable provision in Standards section 110.9. Verify that model numbers of all such controls are listed on the Energy Commission database as &ldquo;Certified Appliances &amp; Control Devices.&rdquo;</td>
		</tr>
		<tr>
			<td colspan="2" rowspan="1">&nbsp;Test Result</td>
			<td>
				<select id="olclscriteriameet" name="olclscriteriameet" disabled="disabled" >
  					<option value="Pass" <c:if test="${title24.olc.citest3 == 'Pass'}"> selected="selected" </c:if> >Pass</option>
  					<option value="Fail" <c:if test="${title24.olc.citest3 == 'Fail'}"> selected="selected" </c:if> >Fail</option>
  					<form:input id="olclscriteriameetBean" type="hidden" path="olc.citest3"/>
  				</select>
  			</td>
		</tr>
		<tr>
			<td colspan="3"><strong>D. Functional Testing</strong></td>
		</tr>
		<tr>
			<td>&nbsp;1.</td>
			<td colspan="2" rowspan="1">Outdoor Lighting Daytime Shut&dash;off Controls</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.ftdayoff" value="1" onchange="setTestResult(this,'olcftdayoff',1);" /></td>
			<td>Controlled lights are off during daylight hours.</td>
		</tr>
		<tr>
			<td colspan="2" rowspan="1">&nbsp;Test Result</td>
			<td>
				<select id="olcftdayoff" name="olcftdayoff" disabled="disabled" >
  					<option value="Pass" <c:if test="${title24.olc.fttestd == 'Pass'}"> selected="selected" </c:if> >Pass</option>
  					<option value="Fail" <c:if test="${title24.olc.fttestd == 'Fail'}"> selected="selected" </c:if> >Fail</option>
  				</select>
  				<form:input id="olcftdayoffBean" type="hidden" path="olc.fttestd"/>
  			</td>
		</tr>
		<tr>
			<td colspan="2" rowspan="1"><strong>Evaluation:</strong></td>
			<td>
				<select id="complienceTestEvaluation" name="complienceTestEvaluation" disabled="disabled" >
  					<option value="Pass" <c:if test="${title24.olc.evaluation == 'Pass'}"> selected="selected" </c:if> >Pass</option>
  					<option value="Fail" <c:if test="${title24.olc.evaluation == 'Fail'}"> selected="selected" </c:if> >Fail</option>
  				</select>
  				<form:input id="complienceTestEvaluationBean" type="hidden" path="olc.evaluation"/>
  			</td>
		</tr>
	</tbody>
</table>
<div id="accordianSubmitDiv" style="padding-top:20px;">
    <div class="field">
        <div class="formPrompt"><span></span></div>
        <div class="formValue"><input class="saveAction" id="accordianOLCControlSubmit" type="button" value="Submit"/></div>
    </div>
</div>
<p>&nbsp;</p>
</div>
                        		
                            </div>
                        </div>
                    </div>
		                   <!--  <div class="field">
		                        <div class="formPrompt"><span>Accept the System Tests :</span></div>
		                        <div class="formValue">
		                            <input name="acceptSystemTest"  type="radio" value="Yes" onclick="changeColorTab(this, '#systestLi a');" />
		                            <div class="notesmedium"><span>Yes</span></div>
		                        </div>
		                        <div class="formValue" style="padding-left:35px;">
		                            <input name="acceptSystemTest" type="radio" value="No" onclick="changeColorTab(this, '#systestLi a');" />
		                            <div class="notesmedium"><span>No</span></div>
		                        </div>
		                    </div>
		                    <br/><br/><br/>
		                    <div class="field">
		                        <div class="field">
		                            <div class="field">
		                                <div class="formPrompt"><span></span></div>
		                                <div class="formValue"><input class="saveAction" id="prev" type="button" value="Previous"/></div>
		                            </div>
		                        </div>
		                        <div id="prevdivform2" style="padding-top:20px;">
		                            <div class="field">
		                                <div class="formPrompt"><span></span></div>
		                                <div class="formValue"><input class="saveAction" id="next" type="button" value="Submit"/></div>
		                            </div>
		                        </div>
		                    </div> -->
                    <br/><br/><br/><br/><br/>
               
            </div>
			
			<div id="tabs-5">
					Download the report pdf by clicking button.   <input type="button" value="Generate PDF" onclick="rendertitle24PDF();"/>
			</div>        
</div>
        <div><span>
        	<form:input id="lightcontrolaccepatancesubmitflag" type="hidden" path="lightcontrolaccepatance.submitflag"/>
		    <form:input type="hidden" path="compliance.flag"/>
		    <form:input id="lightcontrolaccepatancespacedetaildata" type="hidden" path="lightcontrolaccepatance.spacedetaildata"/>
		    <form:input id="lightcontrolaccepatancefunctionaltestdata" type="hidden" path="lightcontrolaccepatance.functionaltestdata"/>
		    <form:input id="lightcontrolaccepatanceresultdata" type="hidden" path="lightcontrolaccepatance.resultdata"/>
		    
		    <form:input id="autodlcontrolsystem" type="hidden" path="autodlcontrolaccepatance.autodlcontrolsystemgriddata"/>
		    <form:input id="autodldependentspacecontrol" type="hidden" path="autodlcontrolaccepatance.sensorcontroldata"/>
		    <form:input id="autodlfunctionaltestingcds" type="hidden" path="autodlcontrolaccepatance.autodlfunctionaltestingcdsdata"/>
		    <form:input id="autodlfunctionaltestingsds" type="hidden" path="autodlcontrolaccepatance.autodlfunctionaltestingsdsdata"/>
		    <form:input id="autodlfunctionaltestingcdslmm" type="hidden" path="autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdata"/>
		    <form:input id="autodlfunctionaltestingsdslmm" type="hidden" path="autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdata"/>
		    <form:input id="drmethod1data" type="hidden" path="dracceptance.method1data"/>
		    <form:input id="drspacegriddata" type="hidden" path="dracceptance.drspacedata"/>
		    <form:input id="drmethod2data" type="hidden" path="dracceptance.method2data"/>
		    <form:input id="formtype" type="hidden" path="formtype"/>
		</span></div>
         </form:form>
        <div id="tabid"></div>
        
</div>