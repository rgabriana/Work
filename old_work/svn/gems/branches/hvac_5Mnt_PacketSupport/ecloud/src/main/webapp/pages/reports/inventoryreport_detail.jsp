<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Inventory Report</title>

<style>

.fieldHeader{text-align: center;background-color: #7C7C7C;color: #FFFFFF} 	

#main-container {
	padding-left: 20px;
}

</style>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
</style>

<script type="text/javascript">

$(document).ready(function() {
});

function exportDetailInventoryReport() {
	$('#siteId').val("${site.id}");
	$('#exportDetailIRForm').submit();
}

</script>
</head>
<body id="ireport-main-box" >

<div class="topmostContainer">
	<div class="outermostdiv">
		<div class="outerContainer">
			<h3><span id="inventoryreport_header_text"><spring:message code="menu.inventoryreport" /></span></h3>
			<div class="i1"></div>
		</div>
		
		<div class="innerdiv">
		
			<div style="float:right;padding-bottom: 10px;padding-right: 10px;">	
				<button id="exportInventoryReportBtn" onclick="exportDetailInventoryReport ();">Export CSV</button>
			</div>
			<div id="divform" style="float: right;padding-bottom: 10px;padding-right: 10px;"><div id="form1"><form action="inventoryList.ems" method=POST name="form1" ><input type="hidden" name="customerId" value ="${customerId}" ></input><button onclick="document.form1.submit();">Site Inventory Report List</button></form></div></div><br>
					<div class="settingAccordionFormDiv">
							<fieldset  style="width:97.5%">
								<legend><span class="settingAccordionFormDivHeader">${site.name}</span></legend>
										
					<table class="entable" style="width:100%">	
					<th class="fieldHeader">
					<b>Sensors</b>
					</th>
					<th class="fieldHeader" >
					<b>Count (${totalCommissionedSensorsCount})</b>
					</th>
					<c:forEach items='${totalCommissionedSensors}' var='fixture'>	
					<tr>
						
					<td style="width:80%">
						${fixture.key}				
					</td>	
					<td style="width:20%;left-margin:3px;" align="center">
						<div id='${fixture.key}'>${fixture.value}</div>
					</td>
					  
					</tr>			
					</c:forEach>
					</table>	
					
					<br>	
					<br>
					
					<table class="entable" style="width:100%">	
					<th class="fieldHeader">
					<b>ERC</b>
					</th>
					<th class="fieldHeader" >
					<b>Count (${totalCommissionedErcCount})</b>
					</th>
 					<c:forEach items='${totalCommissionedErc}' var='ercObj'>
					<tr>
						
					<td style="width:80%">
						ERC (Version - ${ercObj[0]})			
					</td>	
					<td style="width:20%;left-margin:3px;" align="center">
						<div id='${ercObj[0]}'>${ercObj[1]}</div>
					</td>
					  
					</tr>
						
 					</c:forEach>
					</table>
					
					<br>	
					<br>
					
					<table class="entable" style="width:100%">	
					<th class="fieldHeader">
					<b>Other Devices</b>
					</th>
					<th class="fieldHeader" >
					<b>Count (${totalOtherDeviceCount})</b>
					</th>
 					<c:forEach items='${totalOtherDevices}' var='otherDevice'>
					<tr>
						
					<td style="width:80%">
						${otherDevice.key}				
					</td>	
					<td style="width:20%;left-margin:3px;" align="center">
						<div id='${otherDevice.key}'>${otherDevice.value}</div>
					</td>
					  
					</tr>
						
 					</c:forEach>
					</table>
					
					
					<br>	
					<br>
					
					<table class="entable" style="width:100%">	
					<th class="fieldHeader">
					<b>CU</b>
					</th>
					<th class="fieldHeader" >
					<b>Count (${totalCommissionedCuCount})</b>
					</th>
 					<c:forEach items='${totalCommissionedCus}' var='cuObj'>
					<tr>
						
					<td style="width:80%">
						CU v.${cuObj[0]}			
					</td>	
					<td style="width:20%;left-margin:3px;" align="center">
						<div id='${cuObj[0]}'>${cuObj[1]}</div>
					</td>
					  
					</tr>
						
 					</c:forEach>
					</table>
					
					<br>	
					<br>
					
					<table class="entable" style="width:100%">	
					<th class="fieldHeader">
					<b>Ballast/Drivers</b>
					</th>
					<th class="fieldHeader">
					<b>Manufacturer</b>
					</th>
					<th class="fieldHeader">
					<b>Baseline Load</b>
					</th>
					<th class="fieldHeader">
					<b>Count of Ballasts (${totalBallastsCount})</b>
					</th>
					<th class="fieldHeader">
					<b>Count of Fixture (${totalBallastAssociatedCount})</b>
					</th>
					<c:forEach items='${totalBallastAssociated}' var='ballast'>	
					<tr>
						
					<td style="width:80%">
						${ballast[0]}				
					</td>	
					
					<td style="width:80%">
						${ballast[1]}				
					</td>
					<td style="width:80%">
						${ballast[4]}				
					</td>
					<td style="width:20%;left-margin:3px;" align="center">
						<div id='${ballast[0]}'>${ballast[2]}</div>
					</td>
					<td style="width:20%;left-margin:3px;" align="center">
						<div id='${ballast[0]}'>${ballast[3]}</div>
					</td>
					  
					</tr>			
					</c:forEach>
					</table>
					
					<br>	
					<br>
					
					<table class="entable" style="width:100%">	
					<th class="fieldHeader">
					<b>Lamps By Type</b>
					</th>
					<th class="fieldHeader">
					<b>Manufacturer</b>
					</th>
					<th class="fieldHeader">
					<b>Count of Lamps(${totalBulbsCount})</b>
					</th>
					<th class="fieldHeader">
					<b>Count of Fixture(${totalLampsAssociatedCount})</b>
					</th>
					<c:forEach items='${totalBulbsAssociated}' var='lamps'>	
					<tr>
						
					<td style="width:80%">
						${lamps[0]}				
					</td>	
					<td style="width:80%">
						${lamps[1]}				
					</td>	
					<td style="width:20%;left-margin:3px;" align="center">
						<div id='${lamps[0]}'>${lamps[2]}</div>
					</td>
					<td style="width:20%;left-margin:3px;" align="center">
						<div id='${lamps[0]}'>${lamps[3]}</div>
					</td>  
					</tr>			
					</c:forEach>
					</table>
					
					<br>	
					<br>
					
					<table class="entable" style="width:100%">	
					<th class="fieldHeader">
					<b>Fixture Type Name</b>
					</th>
					<th class="fieldHeader">
					<b>Ballast Display Label</b>
					</th>
					<th class="fieldHeader">
					<b>Count of Fixture(${totalFxTypeAssociatedCount})</b>
					</th>

    				<c:forEach items='${totalFxTypeAssociated}' var='fxType'>
					<tr>
					<td style="width:80%">
						${fxType[2]}							
					</td>	
					<td style="width:80%">
						${fxType[3]}			
					</td>	
					<td style="width:20%;left-margin:3px;" align="center">
						<div id='${fxType[2]}'>${fxType[0]}</div>
					</td>
					</tr>	
 					</c:forEach>
					</table>
					
			</fieldset>							
			</div>
		</div>
	</div>
</div>
<form id='exportDetailIRForm' action=<spring:url value='/services/inventoryreport/exportdetailreport'/> method='POST'>
<input id="siteId" name="siteId" type="hidden"/>
</form>
</body>
</html>
<script type="text/javascript">
	$(function() {
		$(window).resize(function() {
			var setSize = $(window).height();
			setSize = setSize - 150;
			$(".innerdiv").css("height", setSize);
		});
	});
	$(".innerdiv").css("overflow", "auto");
	$(".innerdiv").css("height", $(window).height() - 150);
</script>