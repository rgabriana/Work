<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/sppa/generateLastMonthCustomerBillList/"
	var="getCustomerGmbList" scope="request" />

<style>
<!--
 .gmbtable {
	background: #fff;
	color: #003300;
	width: 100%;
	font-family: Tahoma;
	font-size: 12px;
	padding: 5px 10px 3px 10px;
	float: left
   }
-->
</style>

<script type="text/javascript">
	$(document).ready(
			function() {

				$("#customerGmbReportTable").jqGrid(
						{
							// Ajax related configurations
							url : "${getCustomerGmbList}" + "${custId}/${startDate}/${endDate}"
									+ "?ts=" + new Date().getTime(),
							datatype : "json",
							mtype : "GET",
							autoencode : true,
							hoverrows : false,
							autowidth : true,
							scrollOffset : 0,
							forceFit : true,
							formatter : {
							     integer : {thousandsSeparator: " ", defaultValue: '0'},
							     number : {decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 0, defaultValue: '0'}
							},
							colNames : [ "id", "<b>Name</b>",
									"<b>Guidline Usage(kWh)</b>", "<b>Actual Usage(kWh)</b>",
									"<b>SPPA Rate</b>", "<b>SPPA Payment Due</b>",
									"<b>Savings(%)</b>" ],
							colModel : [ {
								name : 'id',
								index : 'id',
								hidden : true
							}, {
								name : "emInstance.name",
								index : 'emInstance.name',
								sortable : false,
								width : '10%',
								align : "left"
							}, {
								name : "baselineEnergy",
								index : 'baselineEnergy',
								sortable : false,
								width : '10%',
								align : "right",
								formatter:viewBaselineEnergyFormatter
							}, {
								name : "consumedEnergy",
								index : 'consumedEnergy',
								sortable : false,
								width : '20%',
								align : "right",
								formatter:viewConsumedEnergyFormatter
							}, {
								name : "emInstance.sppaPrice",
								index : 'emInstance.sppaPrice',
								sortable : false,
								width : '10%',
								align : "right",
								formatter:'currency',
								formatoptions:{decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, prefix: "$ "} 
							}, {
								name : "sppaCost",
								index : 'sppaCost',
								sortable : false,
								width : '10%',
								align : "right",
								formatter:'currency',
								formatoptions:{decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, prefix: "$ "} 
							}, {
								name : "savings",
								index : 'savings',
								sortable : false,
								width : '20%',
								align : "center",
								formatter : viewSavingsFormatter
						
							} ],

							jsonReader : {
								root : "sppaBill",
								repeatitems : false,
								id : "id"
							},
							rownumbers : true,
							shrinkToFit : true,
							hidegrid : false,
							height : 250,
							rowNum : 100,
							viewrecords : true,					
							loadComplete : function(data) {
								if (data != null) {
									if (data.sppaBill != undefined) {
										if (data.sppaBill.length == undefined) {
											// Hack: Currently, JSON serialization via jersey treats single item differently
											jQuery("#customerGmbReportTable")
													.jqGrid('addRowData', 0,
															data.sppaBill);
										}
									}
								}
								ModifyGridDefaultStyles();
								//forceFitcustomerGmbReportTableWidth();
							}
						});

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
					var jgrid = jQuery("#customerGmbReportTable");
					var containerHeight = $("body").height();
					var headerHeight = $("#header").height();
					var footerHeight = $("#footer").height();
					var outerDivHeight = $("#outerDiv").height();
					var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();

					jgrid.jqGrid("setGridHeight", containerHeight
							- headerHeight - footerHeight - outerDivHeight
							- gridHeaderHeight - 10);

					$("#customerGmbReportTable").setGridWidth(
							$(window).width() - 25);
				}

				function viewSavingsFormatter(cellvalue, options, rowObject) {
					var baselineEnergy = rowObject.baselineEnergy;
					var consumedEnergy = rowObject.consumedEnergy;
					var savings = (baselineEnergy - consumedEnergy) * 100/ baselineEnergy;
					return savings.toFixed(0);
				}
				
				function viewBaselineEnergyFormatter(cellvalue, options, rowObject) {
					var baselineEnergy = rowObject.baselineEnergy;
					var baselineEnergyKwh = baselineEnergy/ 1000;
					return numberWithCommas(baselineEnergyKwh.toFixed(2));
				}
				
				function viewConsumedEnergyFormatter(cellvalue, options, rowObject) {
					var consumedEnergy = rowObject.consumedEnergy;
					var consumedEnergyKwh = consumedEnergy / 1000;
					return numberWithCommas(consumedEnergyKwh.toFixed(2));
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
			    <td align="right">Report Period : <fmt:formatDate type="date" value="${customerBill.billStartDate}" /> to <fmt:formatDate type="date" value="${customerBill.billEndDate}" /></br>
			        Report Date : <fmt:formatDate type="date" value="${now}" />
			    </td>
				
			</tr>
		</table>
	</div>	
	<div id="outerDiv">
		<div
			style="font-weight: bold; font-size: 2.5em; font-style: normal; text-align: center; background: #fff; width: 100%;">Invoice</div>
	</div>
	<div
		style="background: #fff; color: #003300; font-family: Tahoma; font-size: 12px; padding: 5px 10px 3px 10px;">
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
					     <c:when test="${customerBill.baselineEnergy != '0'}">
				         	<fmt:formatNumber  value="${(customerBill.sppaCost * 1000)/(customerBill.baselineEnergy - customerBill.consumedEnergy)}" type="currency"/>
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
					     <c:when test="${customerBill.consumedEnergy != '0'}">
					    <fmt:formatNumber type="currency" value="${(customerBill.baseCost - customerBill.savedCost) * 1000 /(customerBill.consumedEnergy)}" />
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
					<td align=""><fmt:formatNumber type="currency" value="${customerBill.baseCost}"/></td>
				</tr>
				<tr>
					<td style="font-weight: bold;">Actual Usage:</td>
					<td><fmt:formatNumber  maxFractionDigits="2" type="number" value="${customerBill.consumedEnergy/1000}"/></td>
					<td><fmt:formatNumber type="currency" value="${customerBill.baseCost - customerBill.savedCost}"/></td>
				</tr>
				<tr>
					<td style="font-weight: bold;">Savings:</td>
					<td>
					<c:choose>
					     <c:when test="${customerBill.baselineEnergy != '0'}">
					<fmt:formatNumber type="percent" value="${(customerBill.baselineEnergy - customerBill.consumedEnergy)/(customerBill.baselineEnergy)}"/>
					</c:when>
					    <c:otherwise>
					         0
					    </c:otherwise>
					</c:choose>
					</td>
					<td><fmt:formatNumber type="currency" value="${customerBill.savedCost}"/></td>
				</tr>
				<tr style="color: #003300;font-weight:bold;">
					<td style="border-top: 1px solid black;">Saved by AT&T</td>
					<td style="border-top: 1px solid black;"></td>
					<td style="border-top: 1px solid black;"><fmt:formatNumber type="currency" value="${customerBill.savedCost - customerBill.sppaCost}"/></td>
				</tr>
				<tr>
					<td style="border-top: 1px solid black;">SPPA payable to
						Enlighted:</td>
					<td style="border-top: 1px solid black;"></td>
					<td style="border-top: 1px solid black;"><fmt:formatNumber type="currency" value="${customerBill.sppaCost}"/></td>
				</tr>
			</table>

		</div>
		&nbsp;&nbsp
		<div>

			<table id="tblinvoice2" border="0" cellpadding="0" cellspacing="0"
				class="gmbtable" style="font-size: 14px;">
				<tr>
					<td>Previous Amount Due:</td>
					<td>$ 0</td>
					<td></td>
				</tr>
				<tr>
					<td>Received:</td>
					<td>$ 0</td>
					<td></td>
				</tr>
				<tr>
					<td>Current Charges:</td>
					<td>$ 0</td>
					<td></td>
				</tr>
				<tr>
					<td style="font-weight: bold;">Total Amount Due:</td>
					<td><fmt:formatNumber type="currency" value="${customerBill.sppaCost}"/></td>
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