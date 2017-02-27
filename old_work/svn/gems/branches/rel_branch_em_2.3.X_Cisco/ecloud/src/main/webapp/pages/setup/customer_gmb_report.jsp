<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/sppa/generateLastMonthCustomerBillList/" var="getCustomerGmbList" scope="request" />
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
<script type="text/javascript">

$().ready(function() {
	 // Set up the jquery grid
   
    $("#customerGmbReportTable").jqGrid({
        // Ajax related configurations
        url: "${getCustomerGmbList}"+"${custId}"+"?ts="+new Date().getTime(),
        datatype: "json",
        mtype: "GET",
        autoencode: true,
        hoverrows: false,
        autowidth: true,
        scrollOffset: 0,
        forceFit: true,
        colNames: ["id","Site", "Name", "Guidline Usage(kWh)", "Actual Usage(kWh)", "SPPA Rate","SPPA Payment Due","Savings( % Saved )"],
        colModel: [
		{ name:'id', index:'id', hidden: true},
		{ name:'emInstance.id', index:'emInstance.id',sortable:false, width:'10%',align: "left"},
        { name: "emInstance.name", index: 'emInstance.name',sortable:false, width:'10%',align: "left" },
        { name: "baselineEnergy", index: 'baselineEnergy', sortable:false, width:'10%',align: "left" },
        { name: "consumedEnergy", index: 'consumedEnergy', sortable:false, width:'20%',align: "left" },
        { name: "sppaCost", index: 'sppaCost',sortable:false,width:'10%', align: "left"},
        { name: "savedCost", index: 'savedCost',sortable:false,width:'10%', align: "left"},
        { name: "savings", index: 'savings',sortable:false,width:'20%', align: "center",formatter: viewSavingsFormatter}],
       
        jsonReader: { 
            root:"sppaBill", 
            repeatitems:false,
            id : "id"
        },
        rownumbers :true,
		shrinkToFit: true,
		hidegrid: false,
        height: 250,
        rowNum :100,
        viewrecords: true,
        loadComplete: function(data) {
        	if (data != null){
        	if (data.sppaBill != undefined) {
		   		if (data.sppaBill.length == undefined) {
		   			// Hack: Currently, JSON serialization via jersey treats single item differently
		   			jQuery("#customerGmbReportTable").jqGrid('addRowData', 0, data.sppaBill);
		   		}
		   	}
        	}
    		ModifyGridDefaultStyles();
    		forceFitcustomerGmbReportTableWidth();
	   	}
    })
    
    $(function() {
		$(window).resize(function() {
			var setSize = $(window).height();
			setSize = setSize - 100;
			$(".topmostContainer").css("height", setSize);
			forceFitcustomerGmbReportTableWidth();
		});
	});
	$(".topmostContainer").css("overflow", "auto");
	$(".topmostContainer").css("height", $(window).height() - 100);
	
	function ModifyGridDefaultStyles() {  
		   $('#' + "customerGmbReportTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "customerGmbReportTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "customerGmbReportTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
		   $("#customerGmbReportTable").setGridWidth($(window).width()-80);	
	}
	
	function forceFitcustomerGmbReportTableWidth(){
		var jgrid = jQuery("#customerGmbReportTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight -10);
		
		$("#customerGmbReportTable").setGridWidth($(window).width() - 25);
	}
	
	function viewSavingsFormatter(cellvalue, options, rowObject) {
		var baselineEnergy = rowObject.baselineEnergy;
		var consumedEnergy = rowObject.consumedEnergy;
		var savings = (baselineEnergy - consumedEnergy) * 100 / baselineEnergy
		return savings;
	}
	
});

</script>


<div style="width: 100%;">
<div id="outerDiv">
		<div style="font-weight:bold;font-size:2.5em;font-style:normal;text-align: center;background: #fff">Sites Report</div>
</div>
<div style="overflow:auto">
	<table id="customerGmbReportTable"></table>
</div>
</div>
