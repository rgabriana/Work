<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/customer/listAllCustomers" var="getCustomerList" scope="request" />
<spring:url value="/services/org/customer/save" var="saveCustomer" scope="request" />
<spring:url value="/editCustomer.ems" var="customerEditDialogUrl" scope="request" />
<spring:url value="/addCustomer.ems" var="customerAddDialogUrl" scope="request" />
<spring:url value="/eminstance/list.ems" var="emInstancesListUrl" />
<spring:url value="gmbCustomerInvoicePrompt.ems" var="invoicePromptDialogUrl" />
<spring:url value="/bill/emBillingMain.ems" var="emBillingMainUrl" />


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

function openEmInstanceList(rowId){
	$('#emInstanceCustomerId').val(rowId);
	$('#emInstanceListSubmitForm').submit();	
}

function openEMInstanceBillingList(rowId)
{
	$('#emInstanceCustomerId').val(rowId);
	$("#emInstanceListSubmitForm").attr("action", "${emBillingMainUrl}");
	$("#emInstanceListSubmitForm").submit();
}
function viewActionFormatter(cellvalue, options, rowObject) {
	var rowId = rowObject.id;
	return "<button onclick=\"javascript:openEmInstanceList('"+rowId+"');\">Energy Managers</button>&nbsp;<button onclick=\"javascript: openPopup('"+rowId+"','edit');\">Edit</button>&nbsp;"
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

function viewGmbReportFormatter(cellvalue, options, rowObject) {
	 var rowId = rowObject.id;
	 return "<button onclick=\"javascript:openGmbInvoicePrompt('"+rowId+"');\">Report</button>";
}

function viewBillingFormatter(cellvalue, options, rowObject)
{
	var rowId = rowObject.id;
	return "<button onclick=\"javascript:openEMInstanceBillingList('"+rowId+"');\">Billing</button>";
}
$(document).ready(function() {
    
	startCustomerList(1, "desc");
	$("#customerTable").setGridWidth($(window).width() - 25);
	
	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#customerTable").setGridWidth($(window).width()-25);
	}).trigger('resize');

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
		colNames: ["id", "Name", "Address", "Email", "Contact","SPPA Price","Report","Action","Billing"],
	       colModel: [
	        { name:'id', index:'id', hidden: true},
	        { name: "name", index: 'name',sortable:true, width:'10%',align: "left" },
	        { name: "address", index: 'address', sortable:false, width:'20%',align: "left" },
	        { name: "email", index: 'email', sortable:false, width:'20%',align: "left" },
	        { name: "contact", index: 'contact',sortable:false,width:'10%', align: "left"},
	        { name: "sppaPrice", index: 'sppaPrice',sortable:false,width:'10%', align: "left"},
	        { name: "gmbReport", index: 'gmbReport',sortable:false,width:'10%', align: "center",formatter: viewGmbReportFormatter},
	        { name: "action", index: 'action',sortable:false,width:'20%', align: "left",formatter: viewActionFormatter},
	        { name: "billing", index: 'action',sortable:false,width:'10%', align: "center",formatter: viewBillingFormatter}],
	       
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

function ModifyCustomersTableDefaultStyles() {  
	   $('#' + "customerTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "customerTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "customerTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#customerTable").setGridWidth($(window).width()-25);
}

</script>

<div id="customerDialog"></div>
<div id="invoicePromptDialog"></div>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="outerDiv" style="padding: 5px 5px 5px 5px">
			<div style="font-weight: bolder; "><spring:message code="customer.heading"/></div>
			<button id="newCustomerButton" onclick="openPopup('','add');">Add</button><br>	
			<div style="min-height:5px"></div>
    </div>
	<div style="padding: 0px 5px;">
		<table id="customerTable"></table>
		<div id="customerTablePagingDiv"></div>
	</div>
	<form id="emInstanceListSubmitForm" action="${emInstancesListUrl}" METHOD="POST">
		<input id="emInstanceCustomerId" name="customerId" type="hidden"/>
	</form>
 </div>
