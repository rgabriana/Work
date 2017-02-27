<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/customer/list" var="getCustomerList" scope="request" />
<spring:url value="/services/org/customer/save" var="saveCustomer" scope="request" />
<spring:url value="/editCustomer.ems" var="customerEditDialogUrl" scope="request" />
<spring:url value="/addCustomer.ems" var="customerAddDialogUrl" scope="request" />


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



function openPopup(rowid,mode)
{
	
	if(mode=="add"){
		$("#customerDialog").load("${customerAddDialogUrl}?ts="+new Date().getTime(), function() {
	  		  $("#customerDialog").dialog({
	  				modal:true,
	  				title: 'Add Customer',
	  				width : 550,
	  				height : 220,
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
	  				height : 220,
	  				closeOnEscape: false,
	  				open: function(event, ui) {
	  					
	  				},
	  				
	  			});
	  	});
	}
	//alert("rowObject " + rowObject);
}

function onInstance(rowid)
{
	window.location="eminstance/list.ems?customerId="+rowid;	
}

function onEMEnter(rowid)
{
	window.location="facilities/home.ems?customerId="+rowid;
}
function viewActionFormatter(cellvalue, options, rowObject) {
	 var rowId = rowObject.childNodes[3].textContent;
	return "<div id=\"divform\" ><div id=\"form1\"><form action=\"eminstance/list.ems\" method=POST name=\"form1\" ><input type=\"hidden\" name=\"customerId\" value ='"+rowId+"' ></input><button onclick=\"document.form1.submit();\">Energy Managers</button></form></div><div id=\"btn\"> <button onclick=\"javascript: openPopup('"+rowId+"','edit');\">Edit</button></div></div>";
}
$().ready(function() {
	 // Set up the jquery grid
   
	
	
	
    $("#jqTable").jqGrid({
        // Ajax related configurations
        url: "${getCustomerList}",
        datatype: "xml",
        mtype: "GET",
        autoencode: true,
        hoverrows: false,
        autowidth: true,
	scrollOffset: 0,
	forceFit: true,
        colNames: ["id", "Name", "Address", "Email", "Contact","Action"],
        colModel: [
		{ name:'id', index:'id', sortable:false, width:'5%', hidden: true},
        { name: "name", index: 'name',sortable:false, width:'20%',align: "left" },
        { name: "address", index: 'address', sortable:false, width:'20%',align: "left" },
        { name: "email", index: 'email', sortable:false, width:'20%',align: "left" },
        { name: "contact", index: 'contact',sortable:false,width:'20%', align: "left"},
        { name: "action", index: 'action',sortable:false,width:'15%', align: "left",formatter: viewActionFormatter}],
       
        xmlReader: { 
            root:"customers", 
            row:"customer",
            repeatitems:false,
            id : "id"
        },
        rownumbers :true,
		shrinkToFit: true,
		hidegrid: false,
        height: 250,
        rowNum :-1,
        viewrecords: true,
        sortname: "Name",
        sortorder: "asc",
    	loadComplete: function(data) {
    		if (data.customer != undefined) {
		   		if (data.customer.length == undefined) {
		   			// Hack: Currently, JSON serialization via jersey treats single item differently
		   			jQuery("#jqTable").jqGrid('addRowData', 0, data.customer);
		   		}
		   	}
    		
    		ModifyGridDefaultStyles();
	   	}
    })
    
    $(function() {
		$(window).resize(function() {
			var setSize = $(window).height();
			setSize = setSize - 100;
			$(".topmostContainer").css("height", setSize);
		});
	});
	$(".topmostContainer").css("overflow", "auto");
	$(".topmostContainer").css("height", $(window).height() - 100);
	
	function ModifyGridDefaultStyles() {  
		   $('#' + "jqTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "jqTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "jqTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
		   $("#jqTable").setGridWidth($(window).width()-80);	
	}
	
});

</script>
<div class="topmostContainer">
<div class="outermostdiv">
<div class="outerContainer">
	<span ><spring:message code="customer.heading"/></span><br/>
	<button id="newCustomerButton" onclick="openPopup('','add');">Add</button>
	<div class="i1"></div>
	<table id="jqTable"></table>
</div>
<div id="customerDialog"></div>
</div>
</div>