<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/customer/list/" var="getCustomerList" scope="request" />

<spring:url value="/services/org/customer/license/list/" var="getLicenseList" scope="request" />
<spring:url value="/services/org/" var="getLicense" scope="request" />
<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}	
</style>

<style>
.fieldWrapper{padding-bottom:2px;}
.fieldPadding{height:4px;}
.fieldlabel{float:left; height:20px; width: 10%; font-weight: bold;}
.fieldInputCombo{float:left; height:23px; width: 20%;}

.ui-jqgrid-bdiv
{
 overflow-x : hidden !important;
}
.topmostContainer
{
	overflow-x : hidden !important;
	overflow-y : hidden !important;
}

</style>
<script type="text/javascript">
var MAX_ROW_NUM = 99999;
function viewActionFormatter(cellvalue, options, rowObject) {
	return '<a class="gridLink" href="${getLicense}'+ cellvalue  +'">Download</a>';
}

function statusFormatter(cellvalue, options, rowObject)
{
	 var end = rowObject.childNodes[3].textContent;
	 var nowDate=new Date();
	 var enDate = new Date(end);
	 if(enDate <nowDate)
		 return 'Inactive';
	 else
		 return 'Active';
}
var CUSTOMER='ALL' ;
$().ready(function() {
	loadAllCustomer();
	var URL =  "${getLicenseList}"+CUSTOMER 
	 // Set up the jquery grid
	jQuery("#customerGrid").jqGrid({
		url: URL,
		mtype: "GET",
		datatype: "xml",
		autoWidth:true,
		rownumbers :true,
		scrollOffset: 0,
		rowNum :-1,
		forceFit: true,
		colNames:['Mac', 'Customer Name', 'Start Date', 'End Date', 'Status', 'Download'],
	   	colModel:[
			{name:'macid', index:'macid',  width:'200',sortable:false,sorttype:'string',align: 'left'},
	   		{name:'customer', index:'customer',  width:'200',sortable:false,sorttype:'string', align: 'left'},
	   		{name:'startdate', index:'startdate',   width:'200',sortable:false,sorttype:'string',align: 'left'},
	   		{name:'enddate', index:'enddate',  width:'200',sortable:false,sorttype:'string',align: 'left'},
	   		{name:'status', index:'status',  width:'200',sortable:false,sorttype:'string',align: 'left',formatter: statusFormatter},
	   		{name:'downloadrestpath', index:'downloadrestpath',  sortable:false,width:'200',align: 'center',formatter: viewActionFormatter},
	   	],
	   	xmlReader: { 
	        root:"licensePanels", 
	        row:"licensePanel",
	        repeatitems:false,
	        id : "mac"
	    	},
	   	cmTemplate: { title: false },
	    hidegrid: false,
	    height:'450',
		sortname: 'name',
		sortorder: 'desc' , 
	    viewrecords: true,
	   	loadComplete: function(data) {
	   		if (data.fixture != undefined) {
		   		if (data.fixture.length == undefined) {
		   			// Hack: Currently, JSON serialization via jersey treats single item differently
		   			jQuery("#customerGrid").jqGrid('addRowData', 0, data.fixture);
		   		}
		   	}
	   	}

	});
	
	
});

function changeCustomer()
{
	var currItem = $("#totalCustomer option:selected").text(); 
	CUSTOMER = currItem;
	var newurl = "${getLicenseList}"+CUSTOMER 
	jQuery("#customerGrid").jqGrid('setGridParam',{url:newurl}).trigger("reloadGrid");
}

</script>
<div class="topmostContainer">
<div class="outermostdiv">
<div class="outerContainer">

<div class="fieldWrapper">
		<div class="fieldlabel"><label for="totalCustomer"><spring:message code="License.list"/></label></div>
		<div class="fieldInputCombo">
				<select class="text" id="totalCustomer" onchange="javascript: changeCustomer();">
				</select>
		</div>
		<br style="clear:both;"/>
</div>

	<div class="i1"></div>
	<div>
		<table id="customerGrid"></table>
	</div>
</div>
</div>
</div>
<script type="text/javascript">
	$(function() {
		$(window).resize(function() {
			var setSize = $(window).height();
			setSize = setSize - 100;
			$(".topmostContainer").css("height", setSize);
		});
	});
	$(".topmostContainer").css("overflow", "auto");
	var setSize = $(window).height();
	setSize = setSize - 100;
	$(".topmostContainer").css("height", setSize);
	
	function loadAllCustomer()
	{
		$.ajax({
			url: "${getCustomerList}?ts="+new Date().getTime(),
			dataType:"xml",
			contentType: "application/json; charset=utf-8",
			success: function(data){
				if(data!=null){
					$(data).find('customer').each(function(value){ 
						 var label = $(this).find('name').text();
				          $("#totalCustomer").append("<option  value='"+ value +"'>"+label+"</option>"); 
				     }); 
					$("#totalCustomer").append("<option  value='All'>All</option>");
					$("select#totalCustomer").val('All');  
				}
			}
		});
	}
	
</script>