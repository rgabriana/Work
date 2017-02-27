<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>
<html >

<head>
<meta content="text/html; charset=utf-8" http-equiv="Content-Type" />
<spring:url value="/scripts/jquery/jquery-1.11.0.min.js" var="jquery1110"></spring:url><script type="text/javascript" src="${jquery1110}"></script>

<!-- 
<spring:url value="/themes/default/css/style.css" var="stylecss" />
<link rel="stylesheet" type="text/css" href="${stylecss}" />

<spring:url value="/themes/standard/css/jquery/jquery-ui-1.8.16.custom.css"	var="jquerycss" />
<link rel="stylesheet" type="text/css" href="${jquerycss}" />

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

 -->
 <spring:url value="/scripts/jquery/jquery.validate.1.9.min.js" var="jqueryvalidate"></spring:url>
<script type="text/javascript" src="${jqueryvalidate}"></script>
 
<spring:url value="/scripts/jquery/jquery-migrate-1.2.1.js"	var="jquerymigrate"></spring:url>
<script type="text/javascript" src="${jquerymigrate}"></script>

  
<spring:url value="/scripts/jquery/jquery.layout-latest-1.4.0.js"	var="jquerylayout"></spring:url>
<script type="text/javascript" src="${jquerylayout}"></script>
<script>
$( document ).ready(function() {
	renderSpaceDetailGrid();
});

</script>
<style>

.pagebreakbefore{
	page-break-before: always;
}
.pagebreakafter{
	page-break-after: always;
}
.pagebreakavoid {
  page-break-inside: avoid;
}


</style>

</head>

<body>
<div style="font: 14px 'Calibri';">
<ems:pagenumresetstartcount startPageNumValue="1"/>
 <DIV class="pagebreakbefore "></DIV> 
<form:form id="systestForm" method="POST" commandName="title24" action="">

<div style="display:none;">
<span>
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
		</span>
</div>

<div  >
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
	<tbody >
		<tr>
			<td colspan="2"><span style="font-size:14px"><strong>Automatic Shut&#45;off Controls: Automatic Time Switch Control and Occupant Sensor</strong></span></td>
		</tr>
		<tr>
			<td><strong>Intent:</strong></td>
			<td>Lights are turned off or set to a lower level when not needed per Section 110.9(a) &amp; 130.1(c).</td>
		</tr>
		<tr>
			<td colspan="2">
			<span style="font-size:14px"><strong>Guidance</strong> <br/></span>

			This acceptance test form must be filled out for all newly&mdash;installed lighting control systems of the following types:<br/>

			I. Automatic Time Switch Controls<br />
			II. Occupancy Sensors<br />
			III. Partial&mdash;OFF occupancy sensors<br />
			IV. Partial&mdash;ON occupancy sensors (only if used to claim a Power Adjustment Factor)<br />
			V. Occupancy Sensors serving small zones in large open plan offices (only if used to claim a Power Adjustment Factor)<br />

			For automatic daylighting controls use acceptance test form NRCA&mdash;LTI&mdash;03&mdash;A; for demand responsive lighting controls, use<br />
			acceptance test form NRCA&mdash;LTI&mdash;04&mdash;A.<br />
			The tests on this certificate are required by Section 140.6(a)2 and 130.4(a) of the Building Energy Efficiency Standards 2013.<br />
			The tests themselves are described in Sections 140.6(a)2 and in Reference Appendix NA7.6.
			</td>
		</tr>
	</tbody>
</table>
</div>
<p>&nbsp;</p>

<br/>
<div  >
<table align="left" border="1" cellpadding="0" cellspacing="0" style="width:100%"  >
	<tbody>
		<tr>
			<td colspan="3"><span style="font-size:14px"><strong>A. Construction Inspection</strong></span><br />
			Fill out Section A to cover spaces 1 through 3 that are functionally tested under Section B. Make as many copies of pages 2&mdash;5 as<br />
			are required to test all spaces in the building, and attach to page 1.<br />
			Instruments needed to perform tests include, but are not limited to: hand-held amperage meter, power meter, or light meter</td>
		</tr>
		<tr>
			<td style="width:2%;">&nbsp; 1 &nbsp;&nbsp;</td>
			<td colspan="2" rowspan="1"><strong>Automatic Time Switch Controls Construction Inspection&mdash;confirm for all listed in Section B</strong></td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td style="width:2%;">&nbsp; a. &#09;</td>
			<td>All automatic time switch controls are programmed for (check all):</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="lightcontrolaccepatance.autotimeswitchcontrolprogrammed" value="1"/></td>
			<td>Weekdays</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="lightcontrolaccepatance.autotimeswitchcontrolprogrammed" value="2"/></td>
			<td>Weekend</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="lightcontrolaccepatance.autotimeswitchcontrolprogrammed" value="3"/></td>
			<td>Holidays</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td>&nbsp; b.</td>
			<td>Document for the owner automatic time switch programming (check all):</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="lightcontrolaccepatance.ownerautoswitchprogramming" value="1"/></td>
			<td>Weekdays settings</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="lightcontrolaccepatance.ownerautoswitchprogramming" value="2"/></td>
			<td>Weekend settings</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="lightcontrolaccepatance.ownerautoswitchprogramming" value="3"/></td>
			<td>Holidays settings</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="lightcontrolaccepatance.ownerautoswitchprogramming" value="4"/></td>
			<td>Set&mdash;up settings</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="lightcontrolaccepatance.ownerautoswitchprogramming" value="5"/></td>
			<td>Preference program setting</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="lightcontrolaccepatance.ownerautoswitchprogramming" value="6"/></td>
			<td>Verify the correct time and date is properly set in the time switch</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="lightcontrolaccepatance.ownerautoswitchprogramming" value="7"/></td>
			<td>Verify the battery is installed and energized</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="lightcontrolaccepatance.ownerautoswitchprogramming" value="8"/></td>
			<td>Override time limit is no more than 2 hours</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="lightcontrolaccepatance.ownerautoswitchprogramming" value="9"/></td>
			<td>Occupant Sensors and Automatic Time Switch Controls have been certified to the Energy Commission in accordance with the applicable provision in Section 110.9 of the Standards, and model numbers for all such controls are listed on the Commission database as Certified Appliance and Control Devices&nbsp;</td>
		</tr>
		<tr>
			<td><strong>&nbsp; 2.</strong></td>
			<td colspan="2" rowspan="1"><strong>Occupancy Sensor Construction Inspection&mdash;confirm for all listed in Section B</strong></td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="lightcontrolaccepatance.occsensorconstructioninspection" value="1"/></td>
			<td>Occupancy sensors are not located within four feet of any HVAC diffuser</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="lightcontrolaccepatance.occsensorconstructioninspection" value="2"/></td>
			<td>Ultrasonic occupancy sensors do not emit audible sound 5 feet from source</td>
		</tr>
		
	</tbody>
</table>
</div>

<c:set var="lightspacedatalength" scope="request" value="${title24.lightcontrolaccepatance.spacedetaildatajsonarray.length()}"/>
<c:set var="lightfunctionaltestdatalength" scope="request" value="${title24.lightcontrolaccepatance.functionaltestdatajsonarray.length()}"/>


<div style="clear:both;"  class="pagebreakbefore">
<table align="center" border="1" cellpadding="0" cellspacing="0" style="width:100%">
	<tbody >
		<tr>
			<td colspan="4"><span ><strong>B. Functional Testing of Lighting Controls</strong></span><br />
			<strong style="font-size:14px">Representative Spaces Selected</strong><br />
			For every space in the building, conduct functional tests I through V below if applicable. If there are several geometrically similar<br />
			spaces that use the same lighting controls, test only one space and list in the cells below which &ldquo;untested spaces&rdquo; are<br />
			represented by that tested space.<br />
			EXCEPTION: For buildings with up to seven (7) occupancy sensors, all occupancy sensors shall be tested. (NA7.6.2.3)</td>
		</tr>
		
		<tr style="font-weight:bold;">
			<td>Space No</td>
			<td>Space Name</td>
			<td>Space Type</td>
			<td>Untested Areas</td>
		</tr>
		
		<c:if test="${lightspacedatalength > 0}">
			<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
			     
			     <tr>
					<td>${title24.lightcontrolaccepatance.spacedetaildatajsonarray.getJSONObject(index).getString("no")}</td>
					<td>${title24.lightcontrolaccepatance.spacedetaildatajsonarray.getJSONObject(index).getString("name")}</td>
					<td>${title24.lightcontrolaccepatance.spacedetaildatajsonarray.getJSONObject(index).getString("type")}</td>
					<td>${title24.lightcontrolaccepatance.spacedetaildatajsonarray.getJSONObject(index).getString("untested")}</td>
				</tr>
			</c:forEach>
		</c:if>
		
	</tbody>
</table> 

</div>


<div style="clear:both;">

	<c:set var="rowno" scope="request" value="-1"/>
	<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
	<tbody>
		<tr>
			<td colspan="2" rowspan="1">
			<p style="text-align: center;"><strong>Functional Tests</strong></p>

			<p>Confirm compliance (Y/N) for all control system types (I&mdash;V) present in each</p>
			</td>
			<td colspan="${lightspacedatalength}" rowspan="1" style="text-align: center;">
			<p><strong>Tested Space Number</strong></p>

			<p>&nbsp;</p>
			</td>
		</tr>
		<tr>
			<td colspan="2" rowspan="1"><strong>1. Automatic Time Switch Controls</strong></td>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<td>${title24.lightcontrolaccepatance.spacedetaildatajsonarray.getJSONObject(index).getString("no")}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${lightspacedatalength + 2}">Step 1: Simulate occupied condition</td>
		</tr>
		<tr>
			<td>&nbsp;a.&nbsp;</td>
			<td>All lights can be turned on and off by their respective area control switch</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;b.</td>
			<td>Verify the switch only operates lighting in the ceiling&mdash;height partitioned area in<br />
			which the switch is located</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${lightspacedatalength + 2}">Step 2: Simulate unoccupied condition</td>
		</tr>
		<tr>
			<td>&nbsp;a.</td>
			<td>All lighting, including emergency and egress lighting, turns off. Exempt lighting may<br />
			remain on per Section 130.1(c)1 and 130.1(a)1.</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;b.</td>
			<td>Manual override switch allows only the lights in the selected ceiling height<br />
			partitioned space where the override switch is located and remain on no longer<br />
			than 2 hours (unless serving public areas and override switch is captive key type).</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="2">Step 3: System returned to initial operating conditions</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		
		
		
		<!-- Second sub table -->
		
		<tr >
			<td colspan="2" rowspan="1"><strong>2. Occupancy Sensors</strong></td>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<td>${title24.lightcontrolaccepatance.spacedetaildatajsonarray.getJSONObject(index).getString("no")}</td>
				</c:forEach>
			</c:if>
		</tr>
		
		<tr>
			<td colspan="${lightspacedatalength + 2}">Step 1: Simulate an unoccupied condition</td>
		</tr>
		<tr>
			<td>&nbsp;a.&nbsp;</td>
			<td>Lights controlled by occupancy sensors turn off within a maximum of 30 minutes from start of an unoccupied condition per Standard Section 110.9(b)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;b.</td>
			<td>The occupant sensor does not trigger a false "on" from movement in an area
adjacent to the controlled space or from HVAC operation</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${lightspacedatalength + 2}">Step 2: Simulate an occupied condition</td>
		</tr>
		<tr>
			<td>&nbsp;a.</td>
			<td>Status indicator or annunciator operates correctly</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;b.</td>
			<td>Lights controlled by occupancy sensors turn on immediately upon an occupied
condition OR sensor indicates space is "occupied" and lights may be turned on
manually</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="2">Step 3: System returned to initial operating conditions</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		
		
<!-- Third sub table -->
</tbody>
</table>
<div class="pagebreakbefore"> 
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
<tbody>


<tr >
			<td colspan="2" rowspan="1"><strong>3. Partial Off Occupancy Sensor</strong></td>
		</tr>
		<tr>
			<td colspan="${lightspacedatalength + 2}">Step 1: Simulate an unoccupied condition</td>
		</tr>
		<tr>
			<td>&nbsp;a.&nbsp;</td>
			<td>Lights go to partial off state within a maximum of 30 minutes from start of an<br />
			unoccupied condition per Standard Section 110.9(a)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
			
		</tr>
		<tr>
			<td>&nbsp;b.</td>
			<td>The occupant sensor does not trigger a false &quot;on&quot; from movement in an area<br />
			adjacent to the controlled space or from HVAC operation. For library book stacks or<br />
			warehouse aisle, activity beyond the stack or aisle shall not activate the lighting in<br />
			the aisle or stack.</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;c.</td>
			<td>In the partial off state, lighting shall consume no more than 50% of installed lighting<br />
			power, or:<br />
			&bull; No more than 60% of installed lighting power for metal halide or high<br />
			pressure sodium lighting in warehouses.<br />
			&bull; No more than 60% of installed lighting power for corridors and stairwells in<br />
			which the installed lighting power is 80 percent or less of the value allowed<br />
			under the Area Category Method.<br />
			Light level may be used as a proxy for lighting power when measurements are taken</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${lightspacedatalength + 2}">Step 2: Simulate an occupied condition</td>
		</tr>
		<tr>
			<td>&nbsp;a.</td>
			<td>The occupant sensing controls shall turn lights fully ON in each separately controlled<br />
			areas, Immediately upon an occupied condition</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		
		
		
		
<!-- Forth sub table -->		
		<tr>
			<td colspan="2" rowspan="1"><strong>4. Partial On Occupancy Sensors</strong></td>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<td>${title24.lightcontrolaccepatance.spacedetaildatajsonarray.getJSONObject(index).getString("no")}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${lightspacedatalength + 2}">Step 1. Simulate an occupied condition. Verify partial on operation.</td>
		</tr>
		<tr>
			<td>&nbsp;a.&nbsp;</td>
			<td>Immediately upon an occupied condition, the first stage activates between 30 to 70%<br />
			of the lighting automatically.</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;b.</td>
			<td>After the first stage occurs, manual switches allow an occupant to activate the <br />
			alternate set of lights, activate 100% of the lighting power, and manually deactivate <br />
			all of the lights.</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${lightspacedatalength + 2}">Step 2: Simulate an occupied condition</td>
		</tr>
		<tr>
			<td>&nbsp;a.</td>
			<td>Both stages (automatic on and manual on) lights turn off within a maximum of 30<br />
			minutes from start of an unoccupied condition per Standard Section 110.9(a)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;b.</td>
			<td>The occupant sensor does not trigger a false &quot;on&quot; from movement in an area adjacent<br />
			to the controlled space or from HVAC operation</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>

</tbody>
</table>
</div>

<div class="pagebreakbefore"> 
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
<tbody>		
		<!-- fifth sub table -->
		<tr>
			<td colspan="2" rowspan="1"><strong>5. Additional test for Occupancy Sensors Serving Small Zones in Office Spaces Larger<br />
			than 250 Square Feet, to Qualify for a Power Adjustment Factor (PAF)</strong></td>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<td>${title24.lightcontrolaccepatance.spacedetaildatajsonarray.getJSONObject(index).getString("no")}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${lightspacedatalength + 2}">First, complete Functional Test 2 (above ) for each controlled zone</td>
		</tr>
		<tr>
			<td colspan="${lightspacedatalength + 2}">Step 1. Verify area served and compare actual PAF with claimed PAF. Refer to Functional Test II.</td>
		</tr>
		<tr>
			<td>&nbsp;a.&nbsp;</td>
			<td>Area served by controlled lighting (square feet).</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;b.</td>
			<td>Enter PAF corresponding to controlled area from line (a) above (<125sf for PAF=0.4,126-250sf for PAF=0.3, 251-500sf for PAF=0.2).</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;c.</td>
			<td>Enter PAF claimed for occupant sensor control in this space from the Certificate of&nbsp;Compliance</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;d.</td>
			<td>The PAF corresponding to the controlled area (line b), is less than or equal to the PAF&nbsp;claimed in the compliance documentation (line c)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;e.</td>
			<td>Sensors shall not trigger in response to movement in adjacent walkways or&nbsp;workspaces.</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;f.</td>
			<td>All steps are conducted in Functional Test 2 &ldquo;Occupancy Sensor (On Off Control)&rdquo; and&nbsp;all answers are Yes (Y)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.functionaltestdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>


<!--  Result sub table -->
<!-- Only for new table altogether-->
<c:set var="rowno" scope="request" value="-1"/>		
		<tr>
			<td>&nbsp;<strong>C</strong></td>
			<td><strong>Testing Results</strong></td>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<td>PASS/FAIL</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="2"><strong>I Automatic Time Switch Controls</strong> (all answers must be Y).</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.resultdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
			
		</tr>
		<tr>
			<td colspan="2"><strong>II Occupancy Sensor (On Off Control) </strong>(all answers must be Y).</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.resultdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="2"><strong>III Partial Off Occupancy Sensor </strong>(all answers must be Y). For warehouses, library book<br />
			stacks, corridors, stairwells in nonresidential buildings must also be accompanied by<br />
			passing Test I or Test II.</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.resultdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="2"><strong>IV Partial On Occupant Sensor for PAF</strong> (all answers must be Y).</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.resultdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="2"><strong>V Occupant Sensor serving small zones for PAF</strong> (all answers must be Y). Also must pass<br />
			Test II</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${lightspacedatalength > 0}">
				<c:forEach begin="0" end="${lightspacedatalength -1}" var="index">
					<c:set var="temp" scope="request" value="space${index+1}"/>
					<td>${title24.lightcontrolaccepatance.resultdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<!-- Result Sub table ends -->


		
<!-- Main table ends here -->
</tbody>
</table>
</div> 
<!-- Main table ends here -->

<p>&nbsp;</p>	

<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
	<tbody><!--  sub table -->
		<tr>
			<td>D.</td>
			<td colspan="${lightspacedatalength + 1}">Evaluation :</td>
		</tr>
		<tr>
			<!--  <td><input type="checkbox" name="lightfinalresult"/></td> -->
			<td><form:checkbox cssClass="" path="lightcontrolaccepatance.evaluation" value="1"/></td>
			<td colspan="${lightspacedatalength + 1}">PASS: All applicable Construction Inspection responses are complete and all applicable Equipment Testing Requirements
responses are positive (Y &mdash; yes)</td>
		</tr>
		<!-- Sub table ends -->
	</tbody>
</table>

</div>




<div class="pagebreakbefore">
	 <div class="pagebreakbefore"><jsp:include page="/pages/title24/title24LightingDeclaration.jsp"></jsp:include></div>  
</div>

















<div  class="pagebreakbefore">
<h2 >AUTOMATIC DAYLIGHTING CONTROL ACCEPTANCE DOCUMENT</h2>

<div>
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
	<tbody><!--  sub table -->
		<tr>
			<td colspan="3"><strong>Check boxes for all pages of this NRCA&mdash;LTI&mdash;03&mdash;A completed and included in this submittal</strong></td>
		</tr>
		<tr>
			<td><form:checkbox cssClass="" path="autodlcontrolaccepatance.nrcalti03a" value="1"/></td>
			<td colspan="1" rowspan="1"><span >NRCA-LTI-03-A Page 1 & 2</span></td>
			<td colspan="1" rowspan="1">Construction Inspection. This page required for all submittals.</td>
		</tr>
		<tr>
			<td><form:checkbox cssClass="" path="autodlcontrolaccepatance.nrcalti03a" value="2"/></td>
			<td colspan="1" rowspan="1"><span >NRCA-LTI-03-A Page 3 & 4</span></td>
			<td colspan="1" rowspan="1">Continuous dimming control functional performance test &ndash; watt&mdash;meter or amp&mdash;meter measurement</td>
		</tr>
		<tr>
			<td><form:checkbox cssClass="" path="autodlcontrolaccepatance.nrcalti03a" value="3"/></td>
			<td colspan="1" rowspan="1"><span >NRCA-LTI-03-A Page 5 & 6</span></td>
			<td colspan="1" rowspan="1">Stepped Switching/ Stepped Dimming functional performance test &ndash; watt&mdash;meter or amp&mdash;meter measurement</td>
		</tr>
		<tr>
			<td><form:checkbox cssClass="" path="autodlcontrolaccepatance.nrcalti03a" value="4"/></td>
			<td colspan="1" rowspan="1"><span >NRCA-LTI-03-A Page 7 & 8</span></td>
			<td colspan="1" rowspan="1">Continuous dimming control functional performance test &ndash; light meter power measurement, and default look&mdash;up table of fraction of rated power versus fraction of rated light output.</td>
		</tr>
		<tr>
			<td><form:checkbox cssClass="" path="autodlcontrolaccepatance.nrcalti03a" value="5"/></td>
			<td colspan="1" rowspan="1"><span >NRCA-LTI-03-A Page 10 & 11</span></td>
			<td colspan="1" rowspan="1">Stepped Switching/ Stepped Dimming functional performance test &ndash; based on light output</td>
		</tr>
		<!-- Sub table ends -->
	</tbody>
</table>

<p>&nbsp;</p>

<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
	<tbody>
		<tr>
			<td colspan="2"><span style="font-size:16px"><strong>I. Construction Inspection NA&mdash;7.6.1.1</strong></span></td>
		</tr>
		<tr>
			<td colspan="2"><strong>1 Drawing of Daylit Zone(s) must be shown on plans or attached to this form. Select one or both of the following:</strong></td>
		</tr>
		<tr>
			<td><form:checkbox cssClass="" path="autodlcontrolaccepatance.drawdlzone" value="1"/></td>
			<td>Shown on plans page #&rsquo;s&nbsp;<form:input path="autodlcontrolaccepatance.drawdlzoneshownpages" cssClass="underline" value=""/></td>
		</tr>
		<tr>
			<td><form:checkbox cssClass="" path="autodlcontrolaccepatance.drawdlzone" value="2"/></td>
			<td>Daylit zones(s) drawn in on as&mdash;built plans (attached) page #&rsquo;s<form:input path="autodlcontrolaccepatance.drawdlzonebuiltplanpages" cssClass="underline" value=""/></td>
		</tr>
	</tbody>
</table>

<p>&nbsp;</p>

<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
	<tbody>
		<tr>
			<td>&nbsp;</td>
			<td><em>Check box below if sampling method is used in accordance with NA7.6.1.2. If checked, attach a page with names of other controls in sample (only for buildings with &gt; 5 daylight control systems, sample group glazing same orientation)</em></td>
		</tr>
	</tbody>
</table>

<p>&nbsp;</p>

<c:set var="autodlcontrolsystemdatalength" scope="request" value="${title24.autodlcontrolaccepatance.autodlcontrolsystemgriddatajsonarray.length()}"/>

<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
	<tbody>
		<tr>
			<td style="text-align: left;"><strong>Control System</strong></td>
			<td style="text-align: left;"><strong>System Name</strong></td>
			<td style="text-align: left;"><strong>Check if Tested Control is Representative of Sample</strong></td>
		</tr>
			
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
				     
				     <tr>
						<td>${title24.autodlcontrolaccepatance.autodlcontrolsystemgriddatajsonarray.getJSONObject(index).getString("sysNo")}</td>
						<td>${title24.autodlcontrolaccepatance.autodlcontrolsystemgriddatajsonarray.getJSONObject(index).getString("name")}</td>
						<td>${title24.autodlcontrolaccepatance.autodlcontrolsystemgriddatajsonarray.getJSONObject(index).getString("chkfield")}</td>
					</tr>
				</c:forEach>
			</c:if>
	</tbody>
</table>
<p>&nbsp;</p>

<c:set var="rowno" scope="request" value="-1"/>	
<c:set var="autodlsensordatalength" scope="request" value="${title24.autodlcontrolaccepatance.sensorcontroldatajsonarray.length()}"/>
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
	<tbody>
		<tr>
			<td colspan="2" rowspan="1"><strong>2&nbsp;System Information</strong></td>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<td>${title24.autodlcontrolaccepatance.autodlcontrolsystemgriddatajsonarray.getJSONObject(index).getString("sysNo")}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="2">&nbsp;&nbsp;<strong>Zone Type: </strong>Skylit (Sky), Primary Sidelit (PS), or Secondary Sidelit (SS)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.sensorcontroldatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="2">&nbsp;<strong>&nbsp;Control Type:</strong> Continuous Dimming with more than 10 light levels (C), Stepped Dimming (SD), Switching (SW)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.sensorcontroldatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="2">&nbsp;&nbsp;<strong>Design Footcandles:</strong> (enter number or &ldquo;Unknown&rdquo;)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.sensorcontroldatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>

</tbody>
</table>
</div>
<div class="pagebreakbefore"> 
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
<tbody>

		
		<tr>
			<td colspan="${autodlcontrolsystemdatalength+ 2}" rowspan="1"><strong>3&nbsp;Sensor and Controls</strong></td>
		</tr>
		<tr>
			<td colspan="2"><strong>&nbsp; &nbsp;Control Loop Type: </strong>Open Loop (OL), Closed Loop (CL)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.sensorcontroldatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="2"><strong>&nbsp; &nbsp;Sensor Location: </strong>Outside (O), Inside Skylight (IS), Near Windows facing out (NW), In Controlled Zone (CZ)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.sensorcontroldatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="2"><strong>&nbsp; &nbsp;Sensor Location is Appropriate to Control Loop Type: (Y/N)</strong><br />
			If control loop type is Open Loop (OL): Enter yes (Y) if location = Outside (O), Inside Skylight (IS), or Near Windows facing out (NW); otherwise, enter no (N).<br />
			If Control loop type is Closed Loop (CL): Enter yes (Y) if location = In Controlled Zone (CZ); otherwise, enter no (N).</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.sensorcontroldatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="2"><strong>Control Adjustments are in Appropriate Location (Y/N): </strong>Yes, If Readily Accessible or<br />
			Yes if in Ceiling &le; 11 ft , No for all other .</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.sensorcontroldatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength+ 2}"><strong>4 Has documentation been provided by the installer:</strong></td>
		</tr>
		<tr>
			<td colspan="2">&nbsp; &nbsp;Installation Manuals and Calibration Instructions Provided to Building Owner:(Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.sensorcontroldatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="2">&nbsp; &nbsp;Location of Light Sensor on Plans: (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.sensorcontroldatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="2">&nbsp; &nbsp;Location of Light Sensor on Plans: (Page Number)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.sensorcontroldatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength+ 2}"><strong>5 Separate Controls of Luminaires in Daylit Zones:</strong></td>
		</tr>
		<tr>
			<td colspan="2">Are luminaires controlled by automatic daylighting controls only in daylit zones: (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.sensorcontroldatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="2">Separately circuited for daylit zones by windows and daylit zones under skylights: (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.sensorcontroldatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength+ 2}"><strong>6 Daylighting control device certification</strong></td>
		</tr>
		<tr>
			<td colspan="2">&nbsp; &nbsp;Daylighting control has been certified in accordance with &sect;110.9: (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.sensorcontroldatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="2"><strong>Construction Inspection PASS/FAIL</strong>. If all responses on <strong>Construction Inspectionpages</strong> 1 &amp; 2are complete and all Yes/No questions have a Yes (Y) response, the tests PASS; If any responses on this page are incomplete OR there are any no (N) responses, the tests <strong>FAIL</strong></td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.sensorcontroldatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
	</tbody>
</table>

<p>&nbsp;</p>


<c:set var="rowno" scope="request" value="-1"/>		

</div>
<div class="pagebreakbefore"> 

<!-- START SAMPLE TABLE for cds data -->
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
	<tbody>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">
			<p><span style="font-size:16px"><strong>II. Functional Performance Testing &ndash; Continuous Dimming Systems NA-7.6.1.2.1 </strong></span></p>

			<p><strong>Power estimation using amp-meter measurement, or alternate option &ndash; watt-meter measurement</strong></p>
			</td>
		</tr>
		<tr>
			<td colspan="2" rowspan="2">Complete all tests on page 3 &amp; 4 (No Daylight Test, Full Daylight Test, and Partial Daylight Test) and fill out Pass/Fail section on Page</td>
			<td colspan="${autodlcontrolsystemdatalength + 0}" rowspan="1">Applicable Control System</td>
		</tr>
		 <tr>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<td>${title24.autodlcontrolaccepatance.autodlcontrolsystemgriddatajsonarray.getJSONObject(index).getString("sysNo")}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}" rowspan="1"><strong>System Information</strong></td>
		</tr>
		<tr>
			<td>&nbsp;a.&nbsp;</td>
			<td>Control Loop Type: Open Loop or Closed Loop? (O or C)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<!--  ENDS SAMPLE -->
		
		
		
		
		<tr>
			<td>&nbsp;b.&nbsp;</td>
			<td>Indicate if Mandatory control &mdash; M (required for skylit zone or primary sidelit zone with installed general lighting power &gt; 120 W);<br />
			or Voluntary &mdash;V (M, V)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;c.</td>
			<td>If automatic daylighting controls are mandatory, are all general lighting luminaires in daylit zones controlled by automatic daylight controls? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;d.</td>
			<td>Documented general lighting design footcandles. (Enter footcandle value or &ldquo;Unknown&rdquo; (U))</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;e.</td>
			<td>Power estimation method. Measured Amps Multiplied by Volts, Volt&mdash;Amps (VA), alternate option is Measured Watts (W)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}"><strong>Step 1: Identify Reference Location </strong>(location where minimum daylight illuminance is measured in zone served by the controlled lighting.)</td>
		</tr>
		<tr>
			<td>&nbsp;f.</td>
			<td>Method Used: Illuminance or Distance? (I or D)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Override daylight control system and drive electric lights to highest light level for the following:</td>
		</tr>
		<tr>
			<td>&nbsp;g.</td>
			<td>Highest light level fc &ndash; enter measured footcandles (fc) from controlled electric lighting (does not include daylight illuminance)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;h.</td>
			<td>Full load Highest light level power. Enter measured Amps times Volts, Volt&mdash;Amps (VA) or measured Watts(W).</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;i.</td>
			<td>Indicate whether this is Full Output (FO), or Task Tuned (Lumen Maintenance) (TT)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Step 2: No Daylight Test controls enabled &amp; daylight less than 1 fc at reference location</td>
		</tr>
		<tr>
			<td>&nbsp;j.</td>
			<td>Method Used: Night time manual measurement (Night), Night Time Illuminance Logging (Log), Cover Fenestration (CF), Cover Open Loop Photosensor (COLP)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;k.</td>
			<td>Reference Illuminance (footcandles) as measured at Reference Location (see Step 1). Enter footcandles</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;l.</td>
			<td>Enter Y if either of the following statements are true:<br />
			[Reference Illuminance (line j)] /[Highest light level fc (line g)] &gt; 70% when line I = FO?90%? or<br />
			[Reference Illuminance (line j)] / [design footcandles (line d)] &gt; 80%? (Y/ N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Step 3: Full Daylight Test conducted when daylight greater than reference illuminance (line j)</td>
		</tr>
		<tr>
			<td>&nbsp;m.</td>
			<td>Enter measured Amps Multiplied by Volts, Volt&mdash;Amps (VA) or measured Watts (W).</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;n.</td>
			<td>System power reduction enter [1 &ndash; (line m)/(line h)] enter as percent.</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;o.</td>
			<td>Is System Power Reduction (line m) &gt; 65% when line i = FO, or &gt; 56% when line i = TT (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;p.</td>
			<td>With uncontrolled lights also on, no lamps are dimmed outside of daylit zone by same control mechanism or formula (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;q.</td>
			<td>Dimmed lamps have stable output (no perceptible visual flicker) (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
</tbody>
</table>
</div>
<div class="pagebreakbefore"> 
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
<tbody>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Step 4: Partial Daylight Test conducted when daylight between 60% and 95% of (line k)</td>
		</tr>
		<tr>
			<td>&nbsp;r.</td>
			<td>Daylight illuminance (light level without electric light) measured at Reference Location (fc)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;s.</td>
			<td>Daylight illuminance divided by the Reference Illuminance = (line r )/ (line k). Enter %.</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;t.</td>
			<td>Is Ratio of Daylight illuminance to Ref. illuminance (line s) between 60% and 95%? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;u.</td>
			<td>Total (daylight + electric light) illuminance measured at the Reference Location (fc)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;v.</td>
			<td>Total illuminance divided by the Reference Illuminance = (line u )/ (line k), Enter %</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;w.</td>
			<td>Is Total illuminance divided by the Reference illuminance (line u) between 100% and 150%? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}"><strong>III. Evaluation :</strong></td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td>PASS: All applicable Construction Inspection responses on pages 1 &amp; 2 are complete and all applicable Functional Performance Testing Requirements responses are positive (Y &mdash; yes) (applicable questions on pages 3 &amp; 4 = c, l, o, p, q, t, w)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		
		
	</tbody>
</table>

<p>&nbsp;</p>


<c:set var="rowno" scope="request" value="-1"/>		
</div>
<div class="pagebreakbefore"> 
<!-- START SAMPLE TABLE for sds data -->
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
	<tbody>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">
			<p><span style="font-size:16px"><strong>II. NA7.6.1.2.2 Functional Performance Testing &ndash; Stepped Switching/ Stepped Dimming Systems</strong></span></p>

			<p><strong>Power estimation using watt&mdash;meter or amp&mdash;meter measurement</strong></p>
			</td>
		</tr>
		<tr>
			<td colspan="2" rowspan="2">Complete all tests on pages 5 &amp; 6 (No Daylight Test, Full Daylight Test, and Partial Daylight Test) and fill out Pass/Fail section on Page 6.</td>
			<td colspan="${autodlcontrolsystemdatalength + 0}" rowspan="1">Applicable Control System</td>
		</tr>
		 <tr>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<td>${title24.autodlcontrolaccepatance.autodlcontrolsystemgriddatajsonarray.getJSONObject(index).getString("sysNo")}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}" rowspan="1"><strong>System Information</strong></td>
		</tr>
		<tr>
			<td>&nbsp;a.&nbsp;</td>
			<td>Control Loop Type: Open Loop or Closed Loop? (O or C)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<!--  ENDS SAMPLE -->
		
		<tr>
			<td>&nbsp;b.&nbsp;</td>
			<td>Indicate if Mandatory control &mdash; M (required for skylit zone or primary sidelit zone with installed general lighting power &gt; 120 W);<br />
			or Voluntary &mdash;V (M, V)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;c.</td>
			<td>If automatic daylighting controls are mandatory, are all general lighting luminaires in daylight zones controlled by automatic daylight controls? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;d.</td>
			<td>Power estimation method. Measured Watts (W), Measured Amps Multiplied by Volts, Volt&mdash;Amps (VA),</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Step 1: Identify Reference Location (location where minimum daylight illuminance is measured in zone served by the controlled lighting.)</td>
		</tr>
		<tr>
			<td>&nbsp;e.</td>
			<td>Method Used: Illuminance or Distance? (I or D)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Step 2: No Daylight Test (daylight less than 1 fc at reference location)</td>
		</tr>
		<tr>
			<td>&nbsp;f.</td>
			<td>Method Used: Night time manual measurement (Night), Night Time Illuminance Logging (Log) attach plot of fc or power, Cover Fenestration (CF), Cover Photosensor (CP)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;g.</td>
			<td>Reference Illuminance (foot&mdash;candles) measured at Reference Location</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;h.</td>
			<td>Enter measured Watts (W), or Amps Multiplied by Volts, Volt&mdash;Amps (VA)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;i.&nbsp;</td>
			<td>Indicate whether this is Full Output (FO), or Task Tuned (Lumen Maintenance) (TT)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Step 3: Full Daylight Test conducted when daylight &gt; 150% of reference illuminance (line g)</td>
		</tr>
		<tr>
			<td>&nbsp;j.</td>
			<td>Measured Watts of Volt&mdash;Amps &mdash; record system power</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;k.</td>
			<td>System fraction of power reduction = [1&mdash;(line j) / (line h)],</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;l.</td>
			<td>Is System Power Reduction (k) &gt; 65% when line i = FO or &gt;56% when line i = TT (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Step 4: Partial Daylight Test</td>
			
		</tr>
		<tr>
			<td>&nbsp;m.</td>
			<td>Method Used: Light Logging (Log), Partially Cover Fenestration (PCF), Open Loop Setpoint Adjustment (OLSA)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;n.&nbsp;</td>
			<td>If the control has three steps of control or less, all steps of control are tested. If the control has more than three steps, testing three steps of control is sufficient for showing compliance.Tests have been conducted at various daylight levels that correspond to steps of electric lighting control. (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>

</tbody>
</table>
</div>
<div class="pagebreakbefore"> 
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
<tbody>

		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">First Stage of Control</td>
		</tr>
		<tr>
			<td>&nbsp;F1&nbsp;</td>
			<td>Total (daylight + electric light) illuminance measured at the Reference Location (foot&mdash;candles) when stage turns off or dims</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;F2</td>
			<td>Is the measured total illuminance between 100% and 150% of the Reference Illuminance (line g)? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;F3</td>
			<td>With time delay disabled, control stage does not cycle (i.e. deadband is sufficient)? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Second Stage of Control</td>
		</tr>
		<tr>
			<td>&nbsp;F4&nbsp;</td>
			<td>Total (daylight + electric light) illuminance measured at the Reference Location (foot&mdash;candles) when stage turns off or dims</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;F5</td>
			<td>Is the measured total illuminance between 100% and 150% of the Reference Illuminance (line g)? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;F6</td>
			<td>With time delay disabled, control stage does not cycle (i.e. deadband is sufficient)? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Third Stage of Control</td>
		</tr>
		<tr>
			<td>&nbsp;F7&nbsp;</td>
			<td>Total (daylight + electric light) illuminance measured at the Reference Location (foot&mdash;candles) when stage turns off or dims</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;F8</td>
			<td>Is the measured total illuminance between 100% and 150% of the Reference Illuminance (line g)? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;F9</td>
			<td>With time delay disabled, control stage does not cycle (i.e. deadband is sufficient)? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Step 5: Time Delay Test (conduct at least 60 minutes after overriding time delay)</td>
		</tr>
		<tr>
			<td>&nbsp;r.</td>
			<td>After change of state from little daylight to full daylight, time in minutes before light output is reduced</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;s.</td>
			<td>Is the measured time delay (line r) greater than or equal to 3 minutes? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}"><strong>III. PASS/FAIL Evaluation (check one):</strong></td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td>
			<p>PASS: All applicable Construction Inspectionresponseson pages 1 &amp; 2are complete and all applicable FunctionalPerformance Testing Requirements responses are positive (Y &mdash; yes) (applicable questions on pages 5 &amp; 6 are on lines c, l, n, F2, F3, F5, F6, F8, F9, s)</p>

			<p>FAIL: Any applicable Construction Inspectionresponses on pages 1 &amp; 2are incomplete OR there is one or more negative (N &mdash; no) responses in any applicable Functional Performance Testing Requirements section (applicable questions on pages 5 &amp; 6 are on lines c, l, n, F2, F3, F5, F6, F8, F9, s). System does not pass and is NOT eligible for Certificate of Occupancy according to Section 10&mdash;103(a)3B. Fix problem(s) and retest until the system(s) passes all portions of this test before retesting and resubmitting NRCA&mdash;LTI&mdash;03&mdash;A with PASSED test to the enforcement agency. Describe below the failure mode and corrective action needed.</p>
			</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdsdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		
</tbody>
</table>

<p>&nbsp;</p>

<c:set var="rowno" scope="request" value="-1"/>		

</div>
<div class="pagebreakbefore"> 
<!-- START SAMPLE TABLE for cds lmm data -->
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
	<tbody>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">
			<p><span style="font-size:16px"><strong>II. Functional Performance Testing &ndash; Continuous Dimming Systems NA-7.6.1.2.1</strong></span></p>

			<p><strong>Power estimation using light meter measurement</strong></p>
			</td>
		</tr>
		<tr>
			<td colspan="2" rowspan="2">Complete all tests on page 7 &amp; 8 (No Daylight Test, Full Daylight Test, and Partial Daylight Test) and fill out Pass/Fail section on Page 8.</td>
			<td colspan="${autodlcontrolsystemdatalength + 0}" rowspan="1">Applicable Control System</td>
		</tr>
		<tr>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<td>${title24.autodlcontrolaccepatance.autodlcontrolsystemgriddatajsonarray.getJSONObject(index).getString("sysNo")}</td>
				</c:forEach>
			</c:if>
		</tr>

<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}" rowspan="1"><strong>System Information</strong></td>
			
		</tr>
		
		
		<tr>
			<td>&nbsp;a.&nbsp;</td>
			<td>Control Loop Type: Open Loop or Closed Loop? (O or C)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		
		
		<tr>
			<td>&nbsp;b.</td>
			<td>Indicate if Mandatory control - M (required for skylit zone or primary sidelit zone with installed general lighting power &gt; 120 W);<br />
			for Control Credit &ndash; CC; or Voluntary not for credit -V (M, CC, V)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;c.</td>
			<td>If automatic daylighting controls are mandatory, are all general lighting luminaires in daylight zones controlled by automatic daylight controls? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;d.</td>
			<td>Documented general lighting design footcandles. If design footcandles not documented leave blank (enter fc)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;e.</td>
			<td>Power estimation method. (see line r) Default ratio of power to light (Dfc), cut-sheet ratio of power to light (CSfc) If CSFc &ndash; attach cut-sheet. Enter Dfc or CSfc,</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Step 1: Identify Reference Location (location where minimum daylight illuminance is measured in zone served by the controlled lighting.).</td>
		</tr>
		<tr>
			<td>&nbsp;f.</td>
			<td>Method Used: Illuminance or Distance? (I or D)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Override daylight control system and drive electric lights to full light output for highest light level fc.:</td>
		</tr>
		<tr>
			<td>&nbsp;g.</td>
			<td>Highest light level fc &ndash; enter measured controlled electric lighting footcandles (fc)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;h.</td>
			<td>Indicate whether this is Full Output (FO), or Task Tuned (Lumen Maintenance) (TT)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Step 2: No Daylight Test</td>
		</tr>
		<tr>
			<td>&nbsp;i.</td>
			<td>Method Used: Night time manual measurement (Night), Night Time Illuminance Logging (Log), Cover Fenestration (CF), Cover Open Loop Photosensor (COLP)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;j.</td>
			<td>Reference Illuminance (footcandles) measured at Reference Location (Illuminance of general lighting at the reference location)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;k.</td>
			<td>Enter Y if either of the following statements are true:<br />
			If line h = FO; [Reference Illuminance (line i)] / [Full Output fc (line g)] &gt; 70%? or<br />
			[Reference Illuminance (line i)] / [design footcandles (line d)] &gt; 80%? (Y/ N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Step 3: Full Daylight Test conducted when daylight &gt; reference illuminance (line i)</td>
		</tr>
		<tr>
			<td>&nbsp;l.</td>
			<td>Daylight illuminance (light level with electric lighting turned off) measured at Reference Location (fc)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;m.</td>
			<td>Daylight illuminance (line l) greater than Reference Illuminance (line j) ? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;n.</td>
			<td>Fraction controlled wattage turned off. Enter %.</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;o.</td>
			<td>Fraction of controlled wattage dimmed [1 &ndash; ( line n)] Enter %.</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
	</tbody>
</table>
</div>
<div class="pagebreakbefore"> 
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
<tbody>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Fill out lines p through s only if fraction of controlled wattage turned off (line n) &lt; 100%.</td>
		</tr>
		<tr>
			<td>&nbsp;p.</td>
			<td>Total (daylight + electric light) illuminance measured at the Reference Location (fc)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;q.</td>
			<td>Electric lighting illuminance at the Reference Location (fc) [(line p) &ndash; (line l)]</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;r.</td>
			<td>Electric lighting illuminance (line q) divided by Highest Light Level fc (line g). Enter %</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;s.</td>
			<td>Dimmed luminaire fraction of rated power. Attach manufacturer&rsquo;s cut-sheet or use default graph of rated power to light output on page 9. Label applicable control system (column A, B or C) on cut-sheet or graph. Enter fraction of rated power in %.</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;t.</td>
			<td>System Power Reduction = [1 &ndash; (line o) * (line s)]</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;u.</td>
			<td>Is System Power Reduction (line t) &gt; 65% when line h = FO, or &gt; 56% when line h = TT (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;v.</td>
			<td>With uncontrolled lights also on, no lamps dimmed outside of daylit zone by control (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;w.</td>
			<td>Dimmed lamps have stable output, no perceptible flicker (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Step 4: Partial Daylight Test conducted when daylight between 60% and 95% of (line i)</td>
		</tr>
		<tr>
			<td>&nbsp;x.</td>
			<td>Daylight illuminance (light level without electric light) measured at Reference Location (fc)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;y.</td>
			<td>Daylight illuminance divided by the Reference Illuminance = (line x)/ (line j). Enter %</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;z.</td>
			<td>Is Ratio of Daylight illuminance to Ref illuminance (line y) between 60% and 95%? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;aa.&nbsp;</td>
			<td>Total (daylight + electric light) illuminance measured at the Reference Location (fc)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;bb.</td>
			<td>Total illuminance divided by the Reference Illuminance = (line aa )/ (line j). Enter %</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;cc.&nbsp;</td>
			<td>Is Ratio of Total illum. to Reference illum. (line bb) between 100% and 150%? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}"><strong>III.&nbsp;PASS/FAIL Evaluation (check one):</strong></td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td>
			<p>PASS: All applicable Construction Inspection responses on pages 1 &amp; 2 are complete and all applicable FunctionalPerformance Testing Requirements responses are positive (Y - yes) (applicable questions on pages 7 &amp; 8 = c, k, m, u, v, w, z, cc)</p>

			<p>FAIL: Any applicable Construction Inspection responses on pages 1 &amp; 2 are incomplete OR there is one or more negative (N - no) responses in any applicable Functional Performance Testing Requirements section (applicable questions on pages 7 &amp; 8= c, k, m, u, v, w, z, cc). System does not pass and is NOT eligible for Certificate of Occupancy according to Section 10-103(a)3B. Fix problem(s) and retest until the system(s) passes all portions of this test before retesting and resubmitting NRCA-LTI-03-A with PASSED test to the enforcement agency. Describe below the failure mode and corrective action needed.</p>
			</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingcdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
</tbody>
</table>
		

<p>&nbsp;</p>		
		
<c:set var="rowno" scope="request" value="-1"/>
</div>
<div class="pagebreakbefore"> 

<!-- START SAMPLE TABLE for sds lmm data -->
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
	<tbody>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">
			<p><span style="font-size:16px"><strong>II. NA7.6.1.2.2 Functional Performance Testing &ndash; Stepped Switching/ Stepped Dimming Systems</strong></span></p>
			<p><strong>Power estimation based on light output</strong></p>
			</td>
		</tr>
		<tr>
			<td colspan="2" rowspan="2">Complete all tests on page 10 &amp; 11 (No Daylight Test, Full Daylight Test, and Partial Daylight Test) and fill out Pass/Fail section on Page 11.</td>
			<td colspan="${autodlcontrolsystemdatalength + 0}" rowspan="1">Applicable Control System</td>
		</tr>
		<tr>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<td>${title24.autodlcontrolaccepatance.autodlcontrolsystemgriddatajsonarray.getJSONObject(index).getString("sysNo")}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}" rowspan="1"><strong>System Information</strong></td>
		</tr>
		<tr>
			<td>&nbsp;a.&nbsp;</td>
			<td>Open Loop or Closed Loop? (O or C)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;b.</td>
			<td>Indicate if Mandatory control &mdash; M (skylit zone or primary sidelit zone with installed general lighting power &gt; 120 W);<br />
			for Control Credit &ndash; CC; or Voluntary not for credit &mdash;V (M, CC, V)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;c.</td>
			<td>If automatic daylighting controls are mandatory, are all general lighting luminaires in daylight zones controlled by automatic daylight controls? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;d.</td>
			<td>Power estimation method. Counting (C) &ndash; not allowed for step dimming, Counting plus Cut Sheet (C+CS) attach ballast cut sheet with steps of power and light.</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Step 1: Identify Reference Location (location where minimum daylight illuminance is measured in zone served by the controlled lighting.)</td>
		</tr>
		<tr>
			<td>&nbsp;e.</td>
			<td>Method Used: Illuminance or Distance? (I or D)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Step 2: No Daylight Test</td>
		</tr>
		<tr>
			<td>&nbsp;f.</td>
			<td>Method Used: Night time manual measurement (Night), Night Time Illuminance Logging (Log) attach plot of fc or power, Cover Fenestration (CF), Cover Photosensor (CP)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;g.</td>
			<td>Reference Illuminance (foot&mdash;candles) measured at Reference Location</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;h.</td>
			<td>Indicate whether this is Full Output (FO), or Task Tuned (Lumen Maintenance) (TT)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Step 3: Full Daylight Test conducted when daylight &gt; 150 percent of reference illuminance (line g)</td>
		</tr>
		<tr>
			<td>&nbsp;i.</td>
			<td>Fraction system wattage turned off</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;j.</td>
			<td>Fraction of system wattage dimmed</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;k.</td>
			<td>Step dimming level as a fraction of rated light output if applicable</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;l.</td>
			<td>Dimmed ballast fraction of rated power from cut&mdash;sheet</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;m.</td>
			<td>System Power Reduction = [1 &ndash; (line j)*(line l)]</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;n.</td>
			<td>Is System Power Reduction (line m) &gt; 65% when line i = FO or &gt;56% when line i = TT (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;o.</td>
			<td>With uncontrolled lights also on, no lamps controlled outside of daylit zone (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;p.</td>
			<td>Dimmed lamps have stable output, no perceptible visual flicker (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Step 4: Partial Daylight Test</td>
		</tr>
		<tr>
			<td>&nbsp;q.</td>
			<td>Method Used: Light Logging (Log), Partially Cover Fenestration (PCF), Open Loop Setpoint Adjustment (OLSA)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;r.</td>
			<td>If the control has three steps of control or less, all steps of control are tested. If the control has more than three steps, testing three steps of control is sufficient for showing compliance. Tests have been conducted at various daylight levels that correspond to steps of electric lighting control. (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
	</tbody>
</table>
</div>
<div class="pagebreakbefore"> 
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
<tbody>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">&nbsp;First Stage of Control</td>
		</tr>
		<tr>
			<td>&nbsp;F1.</td>
			<td>Total (daylight + electric light) illuminance measured at the Reference Location (foot&mdash;candles) when stage turns off or dims</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;F2.</td>
			<td>Is the measured total illuminance between 100% and 150% of the Reference Illuminance (line g)? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;F3.</td>
			<td>With time delay disabled, control stage does not cycle (i.e. deadband is sufficient)? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">&nbsp;Second Stage of Control</td>
		</tr>
		<tr>
			<td>&nbsp;F4.</td>
			<td>Total (daylight + electric light) illuminance measured at the Reference Location (foot&mdash;candles) when stage turns off or dims</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;F5.</td>
			<td>Is the measured total illuminance between 100% and 150% of the Reference Illuminance (line g)? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;F6.</td>
			<td>With time delay disabled, control stage does not cycle (i.e. deadband is sufficient)? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Third Stage of Control</td>
		</tr>
		<tr>
			<td>&nbsp;F7.&nbsp;</td>
			<td>Total (daylight + electric light) illuminance measured at the Reference Location (foot&mdash;candles) when stage turns off or dims</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;F8.</td>
			<td>Is the measured total illuminance between 100% and 150% of the Reference Illuminance (line g)? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;F9.&nbsp;</td>
			<td>With time delay disabled, control stage does not cycle (i.e. deadband is sufficient)? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}">Step 5: Time Delay Test (conduct at least 60 minutes after overriding time delay)</td>
		</tr>
		<tr>
			<td>&nbsp;s.</td>
			<td>After change of state from little daylight to full daylight, time in minutes before light output is reduced</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td>&nbsp;t.</td>
			<td>Is the measured time delay (line s) greater than or equal to 3 minutes? (Y/N)</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
		<tr>
			<td colspan="${autodlcontrolsystemdatalength + 2}"><strong>III.&nbsp;PASS/FAIL Evaluation (check one):</strong></td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td>
			<p>PASS: All applicable Construction Inspectionresponseson pages1 &amp; 2are complete and all applicable FunctionalPerformance Testing Requirements responses are positive (Y &mdash; yes) (applicable questions on pages 10 &amp; 11 are on lines c, n, o, p, r, F2, F3, F5, F6, F8, F9, t)</p>

			<p>FAIL: Any applicable Construction Inspectionresponses on pages1 &amp; 2are incomplete OR there is one or more negative (N &mdash; no) responses in any applicable Functional Performance Testing Requirements section (applicable questions on pages 10 &amp; 11 are on lines c, n, o, p, r, F2, F3, F5, F6, F8, F9, t). System does not pass and is NOT eligible for Certificate of Occupancy according to Section 10&mdash;103(a)3B. Fix problem(s) and retest until the system(s) passes all portions of this test before retesting and resubmitting NRCA&mdash;LTI&mdash;03&mdash;A with PASSED test to the enforcement agency. Describe below the failure mode and corrective action needed.</p>
			</td>
			<c:set var="rowno" scope="request" value="${rowno + 1}"/>
			<c:if test="${autodlcontrolsystemdatalength > 0}">
				<c:forEach begin="0" end="${autodlcontrolsystemdatalength -1}" var="index">
					<c:set var="temp" scope="request" value="${index+1}"/>
					<td>${title24.autodlcontrolaccepatance.autodlfunctionaltestingsdslmmdatajsonarray.getJSONObject(rowno).getString(temp)}</td>
				</c:forEach>
			</c:if>
		</tr>
</tbody>
</table>		
		
</div>

</div>

<div class="pagebreakbefore">
	 <div class="pagebreakbefore"><jsp:include page="/pages/title24/title24AutoDLDeclaration.jsp"></jsp:include></div>  
</div>


<!-- END OF AUTODL ACCEPTANCE DOCUMENT -->




<!-- START OF DR CONTROL ACCEPTANCE DOCUMENT -->


<div  class="pagebreakbefore">
<h2 >DEMAND RESPONSIVE LIGHTING CONTROL ACCEPTANCE DOCUMENT</h2>
<div>
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
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
			<td colspan="2" rowspan="1">Hand&mdash;held amperage and voltage meter</td>
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
			<td colspan="2" rowspan="1">Test building&mdash;wide reduction in lighting power to at least 15% below the maximum total lighting power, as calculated on an area&mdash;weighted basis (measured in illuminance or power). However, any single space must not reduce the combined illuminance from daylight and electric light to less than 50% of the design illuminance.</td>
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

<p>&nbsp;</p>

<c:set var="rowno" scope="request" value="-1"/>		
<c:set var="drspacedatalength" scope="request" value="${title24.dracceptance.drspacedatajsonarray.length()}"/>
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
	<tbody>
		<tr>
			<td style="text-align: left;"><strong>Space/Circuit No</strong></td>
			<td style="text-align: left;"><strong>Space/Circuit Name</strong></td>
		</tr>
			<c:if test="${drspacedatalength > 0}">
				<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
				     <tr>
						<td>${title24.dracceptance.drspacedatajsonarray.getJSONObject(index).getString("no")}</td>
						<td>${title24.dracceptance.drspacedatajsonarray.getJSONObject(index).getString("name")}</td>
					</tr>
				</c:forEach>
			</c:if>
	</tbody>
</table>
<p>&nbsp;</p>

<c:choose>
    <c:when test="${title24.dracceptance.methodofmeasurement == 'Method1'}">
<div class="pagebreakbefore"> 
	    <table border="1" cellpadding="0" cellspacing="0" style="width:100%">
		<tbody>
			<tr>
				<td colspan="${drspacedatalength + 2}">
				<p><span style="font-size:16px"><strong>Method 1: Illuminance Measurement.</strong></span>
				</p>
				<p><br />
				In each space, select one location for illuminance measurement. The chosen location must not be in a primary or secondary skylit or sidelit area, and when placed at the location, the illuminance meter must not have a direct view of a window or skylight. If this is not possible, perform the test at a time and location at which daylight illuminance provides less than half of the design illuminance. Mark each location to ensure that the illuminance meter can be accurately located.
				</p>
				</td>
			</tr>
			<tr>
				<td colspan="2" rowspan="2"></td>
				<td colspan="${drspacedatalength + 0}" rowspan="1">Space Number</td>
			</tr>
			<tr>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<td>${title24.dracceptance.drspacedatajsonarray.getJSONObject(index).getString("no")}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td colspan="${drspacedatalength + 2}" rowspan="1"><strong>Step 1: Full output test</strong></td>
			</tr>
			<tr>
				<td>&nbsp;a.&nbsp;</td>
				<td>Using the manual switches/dimmers in each space, set the lighting system to design full output. Note that the lighting in areas with photocontrols or occupancy/vacancy sensors may be at less than full output, or may be off.</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method1datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;b.</td>
				<td>Take one illuminance measurement at a representative location in each space, using an illuminance meter.</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method1datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;c.</td>
				<td>Simulate a demand response condition using the demand responsive control.</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method1datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;d.</td>
				<td>Take one illuminance measurement at the same locations as above, with the electric lighting system in the demand response condition.</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method1datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;e.</td>
				<td>Turn off the electric lighting and measure the daylighting at the same location (if present)</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method1datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;f.</td>
				<td>Calculate the reduction in illuminance in the demand response condition, compared with the design full output condition. [((line b &mdash; line e)&mdash; (line d &ndash; line e)) /(line b &mdash; line e)]</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method1datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;g.</td>
				<td>Note the area of each controlled space</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method1datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;h.</td>
				<td>The area&mdash;weighted reduction must be at least 0.15 (15%) but must not reduce the combined illuminance from electric light and daylight to less than 50% of the design illuminance in any individual space.</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method1datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;i.</td>
				<td>The demand response signal must not reduce the power input of any individual circuit by more than 50%.</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method1datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td colspan="${drspacedatalength + 2}">Step 2: Minimum output test</td>
			</tr>
			<tr>
				<td>&nbsp;a.</td>
				<td>Using the manual switches/dimmers in each space, set the lighting system to minimum output (but not off). Note that the lighting in areas with photocontrols or occupancy/vacancy sensors may be at more than minimum output, or may be off.</td>
				
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method1datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;b.</td>
				<td>Take one illuminance measurement at each location, using an illuminance meter.</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method1datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;c.</td>
				<td>Simulate a demand response condition using the demand responsive control.</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method1datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;d.</td>
				<td>Take one illuminance measurement at each location with the electric lighting system in the demand response condition.</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method1datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			
			</tbody>
</table>
</div>
<div class="pagebreakbefore"> 
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
<tbody>
			<tr>
				<td>&nbsp;e.</td>
				<td>In each space, the illuminance in the demand response condition must not be less than the<br />
				illuminance in the minimum output condition or 50% of the design illuminance, whichever is less.</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method1datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td colspan="${drspacedatalength + 2}">EXCEPTION: In daylit spaces, the illuminance in the demand response condition maybe below the minimum output setting, but in the demand response condition the combined illuminance from daylight and electric light must be at least 50% of the design illuminance.</td>
			</tr>
			<tr>
				<td colspan="${drspacedatalength + 2}"><strong>Evaluation:</strong></td>
			</tr>
			<tr>
				<td colspan="2">PASS: All applicable Construction Inspection responses are complete and all applicable Equipment Testing Requirementsresponses are positive (Y &mdash; yes)</td>
				<c:set var="rowno" scope="request" value="${rowno + 2}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method1datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
		</tbody>
		</table>
		</div>
    </c:when>
    <c:otherwise>
<div class="pagebreakbefore"> 
	    <table border="1" cellpadding="0" cellspacing="0" style="width:100%">
		<tbody>
			<tr>
				<td colspan="${drspacedatalength + 2}">
				<p><span style="font-size:16px"><strong>Method 2: Power Input Measurement.</strong></span>
				</p>
				<p><br />
				At the lighting circuit panel, select at least one lighting circuit that serves spaces required to meet Section 130.1(b) to measure the reduction in electrical current. Alternatively, employ the power monitoring capabilities of the DR controls system to monitor the circuits in the tests below. The testing process is constant with either approach.
				</p>
				</td>
			</tr>
			<tr>
				<td colspan="2" rowspan="2"></td>
				<td colspan="${drspacedatalength + 0}" rowspan="1">Circuit Number</td>
			</tr>
			<tr>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<td>${title24.dracceptance.drspacedatajsonarray.getJSONObject(index).getString("no")}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td colspan="${drspacedatalength + 2}" rowspan="1"><strong>Step 1: Full output test</strong></td>
			</tr>
			<tr>
				<td>&nbsp;a.&nbsp;</td>
				<td>Using the manual switches/dimmers in each space, set the lighting system to full output. Note that the lighting in areas with photocontrols or occupancy/vacancy sensors may be at less than full output, or may be off.</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method2datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;b.</td>
				<td>Take one electric power measurement for each selected circuit.</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method2datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;c.</td>
				<td>Take one electric power measurement at each circuit location with the electric lighting system in the demand response condition.</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method2datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;d.</td>
				<td>Calculate the reduction in lighting power in the demand response condition, compared with the full output condition [(b&mdash;d)/b]</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method2datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;e.</td>
				<td>Note the area of each controlled space</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method2datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;f.</td>
				<td>Calculate the area&mdash;weighted average reduction in electric power in the demand response condition, compared with the full output condition. The area&mdash;weighted reduction must be at least 15%</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method2datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;g.</td>
				<td>Note the area of each controlled space</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method2datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;h.</td>
				<td>The demand response signal must not reduce the power input of any individual circuit by more than 50%.</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method2datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td colspan="${drspacedatalength + 2}">Step 2: Minimum output test</td>
			</tr>
			<tr>
				<td>&nbsp;a.</td>
				<td>Using the manual switches/dimmers in each space, set the lighting system to minimum output (but not off). Note that the lighting in areas with photocontrols or occupancy/vacancy sensors may be at more than minimum output, or may be off.</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method2datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;b.</td>
				<td>Take one electric power measurement for each selected circuit location.</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method2datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;c.</td>
				<td>Simulate a demand response condition using the demand responsive control.</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method2datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td>&nbsp;d.</td>
				<td>Take one electric power measurement at each circuit with the electric lighting system in the demand response condition.</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method2datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
		</tbody>
</table>
</div>
<div class="pagebreakbefore"> 
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
<tbody>
			<tr>
				<td>&nbsp;e.</td>
				<td>In each space, the electric power input in the demand response condition must not be less than the power input in the minimum light output condition or 50% of the design illuminance power input condition, whichever is less..</td>
				<c:set var="rowno" scope="request" value="${rowno + 1}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method2datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
			<tr>
				<td colspan="${drspacedatalength + 2}">EXCEPTION: Circuits that supply power to the daylit portion of enclosed spaces as long as lighting in non&mdash;daylit portions of the space are not reduced below the lesser of 50% power input leve or the minimum light output condition.</td>
			</tr>
			<tr>
				<td colspan="${drspacedatalength + 2}"><strong>Evaluation:</strong></td>
			</tr>
			<tr>
				<td colspan="2">PASS: All applicable Construction Inspection responses are complete and all applicable Equipment Testing Requirementsresponses are positive (Y &mdash; yes)</td>
				<c:set var="rowno" scope="request" value="${rowno + 2}"/>
				<c:if test="${drspacedatalength > 0}">
					<c:forEach begin="0" end="${drspacedatalength -1}" var="index">
						<c:set var="temp" scope="request" value="${index+1}"/>
						<td>${title24.dracceptance.method2datajsonarray.getJSONObject(rowno).getString(temp)}</td>
					</c:forEach>
				</c:if>
			</tr>
		</tbody>
		</table>
		</div>
    </c:otherwise>
</c:choose>

</div>

</div>

<div class="pagebreakbefore">
	 <div class="pagebreakbefore"><jsp:include page="/pages/title24/title24DRDeclaration.jsp"></jsp:include></div>  
</div>






<!-- END OF DR ACCEPTANCE DOCUMENT -->
<!-- START OF OUTDOOR ACCEPTANCE DOCUMENT -->

<div  class="pagebreakbefore">
<h2 >OUTDOOR LIGHTING ACCEPTANCE DOCUMENT</h2>
<div>
	<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
	<tbody>
		<tr>
			<td colspan="3"><strong>NA7.8.1.2 Outdoor Motion Sensor Acceptance</strong></td>
		</tr>
		<tr>
			<td><strong>Intent:</strong></td>
			<td colspan="2" rowspan="1">Luminaires that can accept an incandescent lamp (for instance, screw&mdash;base fixtures) rated over100W are controlled with a motion sensor per Section 130.2(a).<br />
			Luminaires mounted 24 feet or below are controlled with a motion sensor per Section 130.2(c)3A</td>
		</tr>
		<tr>
			<td colspan="3"><strong>A. Construction Inspection</strong></td>
		</tr>
		<tr>
			<td>&nbsp;1.</td>
			<td colspan="2" rowspan="1">Motion Sensor Construction Inspection</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.constructioninspection" value="1"/></td>
			<td>Motion sensor has been located to minimize false signals</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.constructioninspection" value="2"/></td>
			<td>Sensor is not triggered by motion outside of controlled area</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.constructioninspection" value="3"/></td>
			<td>Desired motion sensor coverage is not blocked by obstruction that could adversely affect performance</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.constructioninspection" value="4"/></td>
			<td>The lighting power of each luminaire is set to reduce by atleast 40 percent but no more than 80 percent, in the unoccupied condition</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.constructioninspection" value="5"/></td>
			<td>No more than 1,500 watts of lighting power is controlled together, by the same sensor or group of sensors</td>
		</tr>
		
<tr>
	<td colspan="2" rowspan="1">&nbsp;Test Result</td>
	<td>
		<c:out value="${title24.olc.ciatest}"/>
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
			<td><form:checkbox cssClass="" path="olc.ftmotion" value="1"/></td>
			<td>Status indicator operates correctly.</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.ftmotion" value="2"/></td>
			<td>Lights controlled by motion sensors turn on immediately upon entry into the area lit by the controlled lights near the motion sensor</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.ftmotion" value="3"/></td>
			<td>Signal sensitivity is adequate to achieve desired control</td>
		</tr>
		
<tr>
	<td colspan="2" rowspan="1">&nbsp;Test Result</td>
	<td>
		<c:out value="${title24.olc.fttest1}"/>
	</td>
</tr>		
		<tr>
			<td>&nbsp;2.</td>
			<td colspan="2" rowspan="1">Simulate no motion in area with lighting controlled by the sensor but with pedestrian motion adjacent to this area. Verify and document the following:</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.ftnomotion" value="1"/></td>
			<td>The occupant sensor does not trigger a false &ldquo;on&rdquo; from movement outside of the controlled area</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.ftnomotion" value="2"/></td>
			<td>Signal sensitivity is adequate to achieve desired control.</td>
		</tr>

<tr>
	<td colspan="2" rowspan="1">&nbsp;Test Result</td>
	<td>
		<c:out value="${title24.olc.fttest2}"/>
	</td>
</tr>
		<tr>
			<td colspan="3"><strong>NA7.8.2 Outdoor Lighting Automatic Shut&mdash;off Controls Acceptance</strong></td>
		</tr>
		<tr>
			<td colspan="3"><strong>Intent:</strong> All installed outdoor lighting shall be controlled by a photocontrol or outdoor astronomical time&mdash;switch control that automatically turns OFF the outdoor lighting when daylight is available, per Section 130.2(c)1. All outdoor lighting shall also be controlled by an automatic scheduling control that automatically turns OFF the lighting outside of business hours or occupied times. Certain types of outdoor lighting shall also be controlled by motion sensor controls. Outdoor lighting shall be circuited separately from other electrical loads.</td>
		</tr>
</tbody>
</table>
</div>
<div class="pagebreakbefore"> 
<table border="1" cellpadding="0" cellspacing="0" style="width:100%">
<tbody>		
		<tr>
			<td colspan="3"><strong>C. Construction Inspection</strong></td>
		</tr>
		<tr>
			<td>&nbsp;1.</td>
			<td colspan="2" rowspan="1">Outdoor Lighting Daytime Shut&mdash;off Controls</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.cidayoff" value="1"/></td>
			<td>All outdoor lighting is controlled either by a photocontrol or outdoor astronomical time&mdash;switch control that automatically turns OFF the outdoor lighting when daylight is available</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.cidayoff" value="2"/></td>
			<td>Astronomical time switch controls and photocontrols have been certified to the Energy Commission in accordance with the applicable provision in Standards Section 110.9. Verify that model numbers of all such controls are listed on the Energy Commission database as &ldquo;Certified Appliances &amp; Control Devices.&rdquo;</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.cidayoff" value="3"/></td>
			<td>If an astronomical time switch is installed, the ON and OFF times should be within 99 minutes of sunrise and sunset. Verify that the controller is programmed with the location of the site, local date and time. Disconnect controller from power source, reconnect, and verify that all programmed settings are retained.</td>
		</tr>
		<tr>
			<td>&nbsp;2.</td>
			<td colspan="2" rowspan="1">Outdoor Lighting Scheduling (Night&mdash;Time Shut Off) Controls</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.cinightoff" value="1"/></td>
			<td>All outdoor lighting is controlled by a scheduling control, which is either a time clock or astronomical time clock.</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.cinightoff" value="2"/></td>
			<td>Controls are programmed with acceptable weekday, weekend, and holiday (if applicable) schedules</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.cinightoff" value="3"/></td>
			<td>Controls have been certified to the Energy Commission in accordance with the applicable provision in Standards Section 110.9. Verify that model numbers of all such controls are listed on the Energy Commission database as &ldquo;Certified Appliances &amp; Control Devices.&rdquo;</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.cinightoff" value="4"/></td>
			<td>Demonstrate and document for the owner time switch programming including weekday, weekend, holiday schedules as well as all set&mdash;up and preference program settings</td>
		</tr>

<tr>
	<td colspan="2" rowspan="1">&nbsp;Test Result</td>
	<td>
		<c:out value="${title24.olc.citest1}"/>
	</td>
</tr>
		<tr>
			<td>&nbsp;3.</td>
			<td colspan="2" rowspan="1">Lighting systems that meet the criteria of Section 130.2(c)4 and 5 of the Standards shall have at least one of the following:</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.lscriteriameet" value="1"/></td>
			<td>A part&mdash;night outdoor lighting control as defined in Section 100.1, which meets the functional requirements of NA7.7.1</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.lscriteriameet" value="2"/></td>
			<td>Motion sensors capable of automatically reducing lighting power by at least 40 percent but not exceeding 80 percent, which have auto&mdash;ON functionality, and which meets the requirements of NA7.7.1</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.lscriteriameet" value="3"/></td>
			<td>A centralized time&mdash;based zone lighting control capable ofautomatically reducing lighting power by at least<br />
			50 percent. This control shall be certified to the Commission in accordance with the applicable provision in Standards section 110.9. Verify that model numbers of all such controls are listed on the Energy Commission database as &ldquo;Certified Appliances &amp; Control Devices.&rdquo;</td>
		</tr>
		
<tr>
	<td colspan="2" rowspan="1">&nbsp;Test Result</td>
	<td>
		<c:out value="${title24.olc.citest2}"/>
	</td>
</tr>
		<tr>
			<td colspan="3"><strong>D. Functional Testing</strong></td>
		</tr>
		<tr>
			<td>&nbsp;1.</td>
			<td colspan="2" rowspan="1">Outdoor Lighting Daytime Shut&mdash;off Controls</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox cssClass="" path="olc.ftdayoff" value="1"/></td>
			<td>Controlled lights are off during daylight hours.</td>
		</tr>
<tr>
	<td colspan="2" rowspan="1">&nbsp;Test Result</td>
	<td>
		<c:out value="${title24.olc.citest3}"/>
	</td>
</tr>

<tr>
	<td colspan="2" rowspan="1"><strong>Evaluation:</strong></td>
	<td>
		<c:out value="${title24.olc.evaluation}"/>
	</td>
</tr>
	</tbody>
</table>

<p>&nbsp;</p>
</div>	
<div class="pagebreakbefore">
	 <div class="pagebreakbefore"><jsp:include page="/pages/title24/title24OutdoorDeclaration.jsp"></jsp:include></div>  
</div>


	

</div>


</form:form>

</div>
</body>

</html>
