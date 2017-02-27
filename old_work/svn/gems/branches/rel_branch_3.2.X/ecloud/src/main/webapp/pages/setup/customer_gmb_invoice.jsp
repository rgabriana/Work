<%@page import="java.util.Date"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/sppa/generateLastMonthCustomerBillList/"
	var="getCustomerGmbList" scope="request" />

<style>

 .gmbtable {
	background: #fff;
	color: #003300;
	width: 100%;
	font-family: Tahoma;
	font-size: 12px;
	padding: 5px 10px 3px 10px;
	float: left
   }
  .jqgrid-rownum
{
    background: none !important;
    border: none !important;
    color: #000000 !important;
    font-weight: bold;
}
.ui-jqgrid .ui-jqgrid-htable th div {
    height:auto !important;
    overflow:hidden;
    padding-right:4px;
    padding-top:2px;
    position:relative;
    vertical-align:text-top;
    white-space:normal !important;
}

#center 
{
    height : 95% !important;
}

</style>

<script type="text/javascript">

var mode = "${mode}";

	$(document).ready(
			function() {

				$("#customerGmbReportTable").jqGrid(
						{
							datatype : "local",
							autoencode : true,
							hoverrows : false,
							autowidth : true,
							scrollOffset : 0,
							forceFit : true,
							formatter : {
							     integer : {thousandsSeparator: " ", defaultValue: '0'},
							     number : {decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 0, defaultValue: '0'}
							},
							colNames : [ "id","Geo Loc Code", "<b>Name</b>",
									"<b>Guideline Usage</br>(kWh)</b>", "<b>Actual Usage(kWh)</b>",
									"<b>Savings (kWh)</b>",
									"<b>SPPA Rate</b>","<b>SPPA Cost</b>","<b>Tax</b>", "<b>SPPA Payment</br> Due</b>",
									"<b>Savings(%)</b>","<b>Block Purchase </br>Remaining (kWh)</b>","<b>Block Purchase </br>Term Remaining</b>"],
							colModel : [ {
								name : 'id',
								index : 'id',
								hidden : true
							},{
								name : "geoLocation",
								index : 'geoLocation',
								sortable : false,
								width : '10%',
								align : "left"
							},{
								name : "name",
								index : 'name',
								sortable : false,
								width : '10%',
								align : "left"
							}, {
								name : "baselineEnergy",
								index : 'baselineEnergy',
								sortable : false,
								width : '12%',
								align : "right",
								formatter:viewBaselineEnergyFormatter
							}, {
								name : "consumedEnergy",
								index : 'consumedEnergy',
								sortable : false,
								width : '18%',
								align : "right",
								formatter:viewConsumedEnergyFormatter
							},{
								name : "energySaved",
								index : 'energySaved',
								sortable : false,
								width : '12%',
								align : "right",
								formatter:viewSavedEnergyFormatter
							},{
								name : "sppaPrice",
								index : 'sppaPrice',
								sortable : false,
								width : '10%',
								align : "right",
								formatter:'currency',
								formatoptions:{decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 4, prefix: "$ "} 
							},{
								name : "sppaCost",
								index : 'sppaCost',
								sortable : false,
								width : '10%',
								align : "right",
								formatter:'currency',
								formatoptions:{decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, prefix: "$ "} 
							},{
								name : "tax",
								index : 'tax',
								sortable : false,
								width : '8%',
								align : "right",
								formatter:'currency',
								formatoptions:{decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, prefix: "$ "} 
							},{
								name : "sppaPayableDue",
								index : 'sppaPayableDue',
								sortable : false,
								width : '12%',
								align : "right",
								formatter:'currency',
								formatoptions:{decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, prefix: "$ "} 
							},{
								name : "savings",
								index : 'savings',
								sortable : false,
								width : '10%',
								align : "center",
								formatter : viewSavingsFormatter
						
							},{
								name : "blockEnergyRemaining",
								index : 'blockEnergyRemaining',
								sortable : false,
								width : '15%',
								align : "right",
								hidden:(mode!="bill"),
								formatter:viewBlockEnergyRemainingFormatter
							},{
								name : "blockTermRemaining",
								index : 'blockTermRemaining',
								sortable : false,
								width : '18%',
								align : "right",
								hidden:(mode!="bill"),
								formatter:viewBlockTermRemainingFormatter
							}],
							shrinkToFit : true,
							hidegrid : false,
							height : "100%",
							rowNum : 100,
							viewrecords : true,					
							loadComplete : function(data) {
								ModifyGridDefaultStyles();
							}
						});

				var mydata =  [];
				
				<c:forEach items="${sppaBill}" var="sppabill">
					var localData = new Object;
					localData.id =  "${sppabill.id}";
					localData.geoLocation =  "${sppabill.geoLocation}";
					localData.name =  "${sppabill.name}";
					localData.baselineEnergy =  "${sppabill.baselineEnergy}";
					localData.consumedEnergy =  "${sppabill.consumedEnergy}";
					localData.energySaved =  "${sppabill.energySaved}";
					localData.sppaPrice =  "${sppabill.sppaPrice}";
					localData.sppaCost =  "${sppabill.sppaCost}";
					localData.tax =  "${sppabill.tax}";
					localData.sppaPayableDue =  "${sppabill.sppaPayableDue}";
					localData.blockEnergyRemaining =  "${sppabill.blockEnergyRemaining}";
					localData.blockTermRemaining =  "${sppabill.blockTermRemaining}";
					mydata.push(localData);
				</c:forEach>
				
				if(mydata)
				{
					for(var i=0;i<mydata.length;i++)
					{
						jQuery("#customerGmbReportTable").jqGrid('addRowData',mydata[i].id,mydata[i]);
					}
				}
				function ModifyGridDefaultStyles() {
					$('#' + "customerGmbReportTable" + ' tr').removeClass(
							"ui-widget-content");
					$('#' + "customerGmbReportTable" + ' tr:nth-child(even)')
							.addClass("evenTableRow");
					$('#' + "customerGmbReportTable" + ' tr:nth-child(odd)')
							.addClass("oddTableRow");
					$("#customerGmbReportTable").setGridWidth(
							$(window).width() - 80);
				}

				function forceFitcustomerGmbReportTableWidth() {
					
					$("#customerGmbReportTable").setGridWidth(
							$(window).width() - 25);
				}

				
				function viewSavingsFormatter(cellvalue, options, rowObject) {
					var baselineEnergy = rowObject.baselineEnergy;
					var consumedEnergy = rowObject.consumedEnergy;
					var savings = (baselineEnergy - consumedEnergy) * 100/ baselineEnergy;
					
					if(!isNaN(savings))
						return savings.toFixed(0);
					else
						return 0;
					
				}
				
				function viewBaselineEnergyFormatter(cellvalue, options, rowObject) {
					var baselineEnergy = rowObject.baselineEnergy;
					var baselineEnergyKwh = baselineEnergy/ 1000;
					return numberWithCommas(baselineEnergyKwh.toFixed(2));
				}

				function viewSavedEnergyFormatter(cellvalue, options, rowObject) {
					var savedEnergy = rowObject.energySaved;
					var savedEnergyKwh = savedEnergy/ 1000;
					return numberWithCommas(savedEnergyKwh.toFixed(2));
				}
								
				function viewConsumedEnergyFormatter(cellvalue, options, rowObject) {
					var consumedEnergy = rowObject.consumedEnergy;
					var consumedEnergyKwh = consumedEnergy / 1000;
					return numberWithCommas(consumedEnergyKwh.toFixed(2));
				}
				
				function viewBlockEnergyRemainingFormatter(cellvalue, options, rowObject) {
					var blockEnergyRemaining = rowObject.blockEnergyRemaining;
					var blockEnergyRemainingKwh = blockEnergyRemaining / 1;
					if(!isNaN(blockEnergyRemainingKwh))
					return numberWithCommas(blockEnergyRemainingKwh.toFixed(2));
					else
					return 0;
				}
				function viewBlockTermRemainingFormatter(cellvalue, options, rowObject)
				{
					var blockTermRemaining = rowObject.blockTermRemaining;
					if(isNaN(blockTermRemaining))
					{
						blockTermRemaining = 0;
					}
					
					blockTermRemaining = blockTermRemaining/365;
					var suffix =" year";
					if(blockTermRemaining>1)
					{
						suffix+="s";
					}
					return blockTermRemaining.toFixed(2) + suffix;
				}
				
				function numberWithCommas(x) {
				    var parts = x.toString().split(".");
				    parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ",");
				    return parts.join(".");
				}
				

			})
</script>

<fmt:setLocale value="en_US"/>


<div style="width: 100%; height: 100%; background: #fff; padding: 0px 0px">
	<div style="background: #fff; color: #003300; font-family: Tahoma; font-size: 12px; padding: 5px 10px 3px 10px;">
		<jsp:useBean id="now" class="java.util.Date" scope="request" />
		<table id="tblcompcreated" border="0" cellpadding="0" cellspacing="0" style="width: 100%;">
			<tr>
				<td align="left"><spring:url
						value="/themes/default/images/att.jpg" var="imglogo" /> <img
					src="${imglogo}" style='padding-top: 4px;width:66px;height:99px;' /></td>
			    <td align="right"><spring:url
						value="/themes/default/images/enlighted-logo.png" var="enlightedLogo" /> <img
					src="${enlightedLogo}" style='padding-top: 4px;width:143px;height:40px;' /></td>
				
			</tr>
			<tr>
			<td></td>
			 <td align="right">Report Period : <fmt:formatDate type="date" value="${customerBill.billStartDate}" /> to <fmt:formatDate type="date" value="${customerBill.billEndDate}" /></br>
			        Report Date : <fmt:formatDate type="date" value="${now}" />
			    </td>
			</tr>
		</table>
	</div>	
	<div id="outerDiv">
		<div
			style="font-weight: bold; font-size: 2.5em; font-style: normal; text-align: center; background: #fff; width: 100%;">Invoice</div>
			 <spring:url value="/themes/default/images/ReportBG.png" var="bg" />
			<div style="background: #fff";><img src="${bg}" style="width: 100%"/></div>
	</div>
	<div
		style="background: #fff; color: #003300; font-family: Tahoma; font-size: 12px; padding: 5px 10px 3px 10px; margin: 0 auto; margin-top: -100px;">
		<div style="padding: 5px 10px 3px 10px;">
			AT&T Energy Department</br> 208 S.Akard St.</br> Dallas,TX 75202
		</div>
		&nbsp; &nbsp;

		<div align="left" style="color: #003300;padding: 5px 10px 3px 10px;">
			Billing No of Days:${customerBill.noOfDays}</br> Number of
			Sites:${noOfEmInstances}
		</div>

		<div align="right">
			<table id="tblinvoice3" border="0" cellpadding="0" cellspacing="0"
				class="invoice3">

				<tr>
					<td align="left" style="color: #f00; font-size: 16px;">Cost per kWh</td>
					<td></td>
				</tr>
				<tr>
					<td align="left">Average sPPA:</td>					
					<td align="right">
					 <c:choose>
					     <c:when test="${customerBill.baselineEnergy > '0'}">
				         	<fmt:formatNumber maxFractionDigits="4" currencySymbol="$"  value="${(customerBill.sppaCost * 1000)/(customerBill.baselineEnergy - customerBill.consumedEnergy)}" type="currency"/>
					    </c:when>
					    <c:otherwise>
					          0
					    </c:otherwise>
					</c:choose>
					</td>
				</tr>
				<tr>
					<td align="left">Average Utility:</td>
					<td align="right">
					<c:choose>
					     <c:when test="${customerBill.consumedEnergy > '0'}">
					    <fmt:formatNumber maxFractionDigits="4" type="currency" currencySymbol="$" value="${(customerBill.baseCost - customerBill.savedCost) * 1000 /(customerBill.consumedEnergy)}" />
					</c:when>
					    <c:otherwise>
					          0
					    </c:otherwise>
					</c:choose>
					</td>
				</tr>

			</table>
		</div>
		&nbsp;&nbsp;
		<div>

			<table id="tblinvoice1" border="0" cellpadding="0" cellspacing="0"
				class="gmbtable" style="font-size: 14px;padding:10px 10px 10px 10px;">
				<tr>
					<td></td>
					<td style="font-weight: bold;">kWh</td>
					<td></td>
				</tr>
				<tr>
					<td style="font-weight: bold;">Guideline Usage:</td>
					<td><fmt:formatNumber  maxFractionDigits="2" type="number" value="${customerBill.baselineEnergy/1000}"/></td>
					<td align=""><fmt:formatNumber type="currency" currencySymbol="$" value="${customerBill.baseCost}"/></td>
				</tr>
				<tr>
					<td style="font-weight: bold;">Actual Usage:</td>
					<td><fmt:formatNumber  maxFractionDigits="2" type="number" value="${customerBill.consumedEnergy/1000}"/></td>
					<td><fmt:formatNumber type="currency" currencySymbol="$"  value="${customerBill.baseCost - customerBill.savedCost}"/></td>
				</tr>
				<tr>
					<td style="font-weight: bold;">Savings:</td>
					<td>
					<c:choose>
					     <c:when test="${customerBill.baselineEnergy > '0'}">
					<fmt:formatNumber type="percent" value="${(customerBill.baselineEnergy - customerBill.consumedEnergy)/(customerBill.baselineEnergy)}"/>
					</c:when>
					    <c:otherwise>
					         0
					    </c:otherwise>
					</c:choose>
					</td>
					<td><fmt:formatNumber type="currency"  currencySymbol="$" value="${customerBill.savedCost}"/></td>
				</tr>
				<tr style="color: #003300;font-weight:bold;">
					<td style="border-top: 1px solid black;">Saved by AT&T</td>
					<td style="border-top: 1px solid black;"></td>
					<td style="border-top: 1px solid black;"><fmt:formatNumber type="currency" currencySymbol="$" value="${customerBill.savedCost - customerBill.sppaCost - customerBill.tax}"/></td>
				</tr>
				<tr>
					<td style="border-top: 1px solid black;">SPPA payable</td>
					<td style="border-top: 1px solid black;"></td>
					<td style="border-top: 1px solid black;"><fmt:formatNumber type="currency" currencySymbol="$" value="${customerBill.sppaCost}"/></td>
				</tr>
				<tr>
					<td>Tax</td>
					<td></td>
					<td><fmt:formatNumber type="currency" currencySymbol="$" value="${customerBill.tax}"/></td>
				</tr>
				<tr>
					<td style="border-top: 1px solid black;">SPPA payable to Enlighted</td>
					<td style="border-top: 1px solid black;"></td>
					<td style="border-top: 1px solid black;"><fmt:formatNumber type="currency" currencySymbol="$" value="${customerBill.sppaCost + customerBill.tax}"/></td>
				</tr>
			</table>

		</div>
		&nbsp;&nbsp
		<div>

			<table id="tblinvoice2" border="0" cellpadding="0" cellspacing="0"
				class="gmbtable" style="font-size: 14px;">
				<tr>
					<td>Previous Amount Due:</td>
					<td><fmt:formatNumber type="currency" currencySymbol="$" value="${customerBill.prevAmtDue}"/></td>
					<td></td>
				</tr>
				<tr>
					<td>Received:</td>
					<td><fmt:formatNumber type="currency" currencySymbol="$" value="${customerBill.paymentReceived}"/></td>
					<td></td>
				</tr>
				<tr>
					<td>Current Charges:</td>
					<td><fmt:formatNumber type="currency" currencySymbol="$" value="${customerBill.currentCharges}"/></td>
					<td></td>
				</tr>
				<tr>
					<td style="font-weight: bold;">Total Amount Due:</td>
					<td><fmt:formatNumber type="currency" currencySymbol="$" value="${customerBill.totalAmtDue}"/></td>
					<td></td>
				</tr>
				<tr>
					<td style="font-weight: bold;">Bill Due date:</td>
					<td align="left"><fmt:formatDate type="date" value="${billDueDate}" /></td>
					<td></td>
				</tr>
			</table>

		</div>
		&nbsp;&nbsp;
		<div class="i1"></div>
		<div id="outerDiv">
			<div style="font-weight: bold; font-size: 2.5em; font-style: normal; text-align: center; background: #fff">Sites
				Report
			</div>
		</div>
	
		<div style="overflow: auto;padding: 5px 10px 3px 10px;">
			<table id="customerGmbReportTable"></table>
		</div>
		
	</div>

</div>