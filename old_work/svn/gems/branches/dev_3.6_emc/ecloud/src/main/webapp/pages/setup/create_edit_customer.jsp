<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<spring:url value="/services/org/customer/listAllCustomers" var="getCustomerList" scope="request" />
<spring:url value="/services/org/customer/save" var="saveCustomer" scope="request" />
<spring:url value="/editCustomer.ems" var="customerEditDialogUrl" scope="request" />
<spring:url value="/addCustomer.ems" var="customerAddDialogUrl" scope="request" />
<spring:url value="/appinstance/list.ems" var="appInstancesListUrl" />
<spring:url value="/eminstance/list.ems" var="emInstancesListUrl" />
<spring:url value="gmbCustomerInvoicePrompt.ems" var="invoicePromptDialogUrl" />
<spring:url value="reportCustomerInvoicePrompt.ems" var="invoiceBirtPromptDialogUrl" />
<spring:url value="/bill/emBillingMain.ems" var="emBillingMainUrl" />
<spring:url value="/reports/inventoryList.ems" var="customerInventoryList" />
<spring:url value="/sites/list.ems" var="sitelistsUrl" />
<spring:url value="/facilities/home.ems" var="customerFacilitiesUrl" />
<spring:url value="/datapullrequest/getlist.ems" var="customerDataPullRequestListUrl" />
<style>
	#formContainer{padding:10px 15px;}
	#formContainer table{width:100%;}
	#formContainer td{padding-bottom:3px;}
	#formContainer td.fieldLabel{width:40%; font-weight:bold;}
	#formContainer td.fieldValue{width:60%;}
	#formContainer .inputField{width:100%; height:20px;}
	#formContainer #saveUserBtn{padding: 0 10px;}
	#formContainer .M_M{display: none;}
	#formContainer .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}
	#divform{width:100%;}
	#form1 {float:left;}
	#btn{float:left;}

	.jqgrow .jqgrid-rownum
	{
		color:#000 !important;
		background-color: transparent !important;
		background-image: none !important;
	}


</style>

<spring:url value="/scripts/jquery/jquery.datetime.picker.0.9.9.js" var="jquerydatetimepicker"></spring:url>
<script type="text/javascript" src="${jquerydatetimepicker}"></script>

<script type="text/javascript">

var thirdpartysupportadmin = false;

function openPopup(rowid,mode)
{

	if(mode=="add"){
		$("#customerDialog").load("${customerAddDialogUrl}?ts="+new Date().getTime(), function() {
	  		  $("#customerDialog").dialog({
	  				modal:true,
	  				title: 'Add Customer',
	  				width : 550,
	  				height : 250,
	  				closeOnEscape: false,
	  				open: function(event, ui) {

	  				},

	  			});
	  	});
	}else{
		$("#customerDialog").load("${customerEditDialogUrl}?customerId="+rowid+"&ts="+new Date().getTime(), function() {
	  		  $("#customerDialog").dialog({
	  				modal:true,
	  				title: 'Edit Customer',
	  				width : 550,
	  				height : 250,
	  				closeOnEscape: false,
	  				open: function(event, ui) {

	  				},

	  			});
	  	});
	}
	//alert("rowObject " + rowObject);
}

function openAppInstanceList(rowId){
	$('#appInstanceCustomerId').val(rowId);
	$('#appInstanceListSubmitForm').submit();
}

function openEmInstanceList(rowId){
	$('#emInstanceCustomerId').val(rowId);
	$('#emInstanceListSubmitForm').submit();
}

function openDataPullRequestList(rowId){
	$('#emInstanceCustomerId').val(rowId);
	$("#emInstanceListSubmitForm").attr("action", "${customerDataPullRequestListUrl}");
	$('#emInstanceListSubmitForm').submit();
}

function openFacilities(rowId){
	$('#facilitiesCustomerId').val(rowId);
	$('#customerFacilitiesForm').submit();
}

function openEMInstanceBillingList(rowId)
{
	$('#emInstanceCustomerId').val(rowId);
	$("#emInstanceListSubmitForm").attr("action", "${emBillingMainUrl}");
	$("#emInstanceListSubmitForm").submit();
}
function viewActionFormatter(cellvalue, options, rowObject) {
	var rowId = rowObject.id;
	return "<button onclick=\"javascript:openAppInstanceList('"+rowId+"');\">Apps</button>&nbsp;" +
				"<security:authorize access="hasAnyRole('Admin','SystemAdmin','SupportAdmin','SPPA')"><button onclick=\"javascript:openEmInstanceList('"+rowId+"');\">Energy Managers</button>&nbsp;</security:authorize>&nbsp;"+
				"<security:authorize access="hasAnyRole('Admin','SystemAdmin','SupportAdmin','SPPA')"><button onclick=\"javascript: openFacilities('"+rowId+"');\">Facilities</button>&nbsp;</security:authorize>&nbsp;" +
				"<security:authorize access="hasAnyRole('Admin','SystemAdmin','SPPA')"><button onclick=\"javascript: openPopup('"+rowId+"','edit');\">Edit</button>&nbsp;</security:authorize>&nbsp;" +
				"<button onclick=\"javascript:openSitesList('"+rowId+"');\">Sites</button>&nbsp;" +
				"<security:authorize access="hasAnyRole('Admin')"><button onclick=\"javascript:openDataPullRequestList('"+rowId+"');\">Energy Data Request</button>&nbsp;</security:authorize>"

}

function checkforSite(rowId){
	$.ajax({
		type: "POST",
		contentType: "application/json; charset=utf-8",
		url: '<spring:url value="/services/org/site/v1/getsitelistbycustomerid/"/>'+rowId,
		dataType: "json",
		success: function(data) {
			if(data.total == 0) {
				alert("Report cannot be generated as this customer doesn't have any associated sites.")
			}
			else{
				openGmbInvoicePrompt(rowId);
			}
		}
	});
}

function openGmbInvoicePrompt(rowId){

	$("#invoicePromptDialog").load("${invoicePromptDialogUrl}?customerId="+rowId+"&ts="+new Date().getTime(), function() {
		  $("#invoicePromptDialog").dialog({
				modal:true,
				title: 'Enter Time Period of Report',
				width : 500,
				height : 200,
				closeOnEscape: false,
				open: function(event, ui) {

				},

			});
	});
}
function openInventoryReportList(rowId)
{
	$('#emInstanceCustomerId').val(rowId);
	$("#emInstanceListSubmitForm").attr("action", "${customerInventoryList}");
	$("#emInstanceListSubmitForm").submit();
}
function openSitesList(rowId)
{
	$('#emInstanceCustomerId').val(rowId);
	$("#emInstanceListSubmitForm").attr("action", "${sitelistsUrl}");
	$("#emInstanceListSubmitForm").submit();
}
function viewGmbReportFormatter(cellvalue, options, rowObject) {
	 var rowId = rowObject.id;
	 return "<button onclick=\"javascript:checkforSite('"+rowId+"');\">Report</button>";
}

function viewBillingFormatter(cellvalue, options, rowObject)
{
	var rowId = rowObject.id;
	return "<security:authorize access="hasAnyRole('Admin','SystemAdmin','SupportAdmin','SPPA')"><button onclick=\"javascript:openEMInstanceBillingList('"+rowId+"');\">Billing</button></security:authorize>";
}

function viewInventoryReportFormatter(cellvalue, options, rowObject)
{
	var rowId = rowObject.id;
	return "<button onclick=\"javascript:openInventoryReportList('"+rowId+"');\">Inventory Report</button>";
}
$(document).ready(function() {

	startCustomerList(1, "desc");
	$("#customerTable").setGridWidth($(window).width() - 25);

	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#customerTable").setGridWidth($(window).width()-25);
	}).trigger('resize');

	<security:authorize access="hasAnyRole('ThirdPartySupportAdmin')">
		thirdpartysupportadmin = true;
	</security:authorize>

});

//function for pagination
function startCustomerList(pageNum, orderWay) {
	jQuery("#customerTable").jqGrid({
		url: "${getCustomerList}"+"?ts="+new Date().getTime(),
		mtype: "POST",
		datatype: "json",
		autoencode: true,
		hoverrows: false,
		autowidth: true,
		scrollOffset: 0,
		forceFit: true,
		formatter: {
			 integer: {thousandsSeparator: ",", defaultValue: '0'},
		     number: {decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, defaultValue: '0.00'}
		},
		colNames: ["id", "Name", "Address", "Email", "Contact","Report","Action","Billing","Inventory Report"],
	       colModel: [
	        { name:'id', index:'id', hidden: true},
	        { name: "name", index: 'name',sortable:true, width:'10%',align: "left" },
	        { name: "address", index: 'address', sortable:false, width:'10%',align: "left" },
	        { name: "email", index: 'email', sortable:false, width:'10%',align: "left" },
	        { name: "contact", index: 'contact',sortable:false,width:'10%', align: "left"},
	        { name: "gmbReport", index: 'gmbReport',sortable:false,width:'10%', align: "center",formatter: viewGmbReportFormatter},
	        { name: "action", index: 'action',sortable:false,width:'45%', align: "left",formatter: viewActionFormatter},
	        { name: "billing", index: 'action',sortable:false,width:'10%', align: "center",formatter: viewBillingFormatter},
	        { name: "inventoryReport", index: 'action',sortable:false,width:'12%', align: "center",formatter: viewInventoryReportFormatter}],

	   	jsonReader: {
			root:"customers",
	        page:"page",
	        total:"total",
	        records:"records",
	        repeatitems:false,
	        id : "id"
	   	},
	   	cmTemplate: { title: false },
	    pager: '#customerTablePagingDiv',
	   	page: pageNum,
	   	sortorder: orderWay,
	   	sortname: "name",
	    hidegrid: false,
	    viewrecords: true,
	   	loadui: "block",
	   	toolbar: [false,"top"],
	   	onSortCol: function(index, iCol, sortOrder) {
	   	},
	   	loadComplete: function(data) {
	   		if (data != null){
	   			if (data.customers != undefined) {
			   		if (data.customers.length == undefined) {
			   			jQuery("#customerTable").jqGrid('addRowData', 0, data.customers);
			   		}
			   	}
	   		}
	   		authenticateRows();
	   		ModifyCustomersTableDefaultStyles();

	   	}

	});

	jQuery("#customerTable").jqGrid('navGrid',"#customerTablePagingDiv",
									{edit:false,add:false,del:false,search:false},
									{},
									{},
									{},
									{},
									{});

	forceFitCustomersTableHeight();
}
//function for pagination

function forceFitCustomersTableHeight(){
	var jgrid = jQuery("#customerTable");
	var containerHeight = $("body").height();
	var headerHeight = $("#header").height();
	var footerHeight = $("#footer").height();
	var outerDivHeight = $("#outerDiv").height();
	var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
	var gridFooterHeight = $("#customerTablePagingDiv").height();

	jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 10);
	$("#customerTable").setGridWidth($(window).width() - 25);
}

function authenticateRows()
{
	if(thirdpartysupportadmin == true)
	{
		jQuery("#customerTable").jqGrid('hideCol', 'billing');
		//EC-25 : For user 'Third Party Support Admin' Report tab should be displayed.
		//jQuery("#customerTable").jqGrid('hideCol', 'gmbReport');
	}
}

function ModifyCustomersTableDefaultStyles() {
	   $('#' + "customerTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "customerTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "customerTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#customerTable").setGridWidth($(window).width()-25);
}

</script>

<div id="customerDialog"></div>
<div id="invoicePromptDialog"></div>
<div id="invoiceBirtPromptDialog"></div>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >

	<div id="outerDiv" style="padding: 5px 5px 5px 5px">
			<div style="font-weight: bolder; "><spring:message code="customer.heading"/></div>
			<security:authorize access="hasAnyRole('Admin','SystemAdmin','SPPA')">
			<button id="newCustomerButton" onclick="openPopup('','add');">Add</button><br>
			 </security:authorize>
			<div style="min-height:5px"></div>
    </div>

	<div style="padding: 0px 5px;">
		<table id="customerTable"></table>
		<div id="customerTablePagingDiv"></div>
	</div>
	<form id="appInstanceListSubmitForm" action="${appInstancesListUrl}" METHOD="POST">
		<input id="appInstanceCustomerId" name="customerId" type="hidden" />
	</form>
	<form id="emInstanceListSubmitForm" action="${emInstancesListUrl}" METHOD="POST">
		<input id="emInstanceCustomerId" name="customerId" type="hidden"/>
	</form>
	<form id="customerFacilitiesForm" action="${customerFacilitiesUrl}" METHOD="POST">
		<input id="facilitiesCustomerId" name="customerId" type="hidden"/>
	</form>
 </div>
