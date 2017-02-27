<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/customer/list" var="getCustomerList" scope="request" />
<spring:url value="/services/org/customer/save" var="saveCustomer" scope="request" />
<spring:url value="/editCustomer.ems" var="customerEditDialogUrl" scope="request" />

<style>
	#customerForm{padding:10px 15px;}
	#customerForm table{width:100%;}
	#customerForm td{padding-bottom:3px;}
	#customerForm td.fieldLabel{width:40%; font-weight:bold;}
	#customerForm td.fieldValue{width:60%;}
	#customerForm .inputField{width:100%; height:20px;}
	#customerForm #saveUserBtn{padding: 0 10px;}
	#customerForm .M_M{display: none;}
	#customerForm .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}    
.customGrid
{
	border: solid 1px #e8eef4 !important; 
    border-collapse: collapse; 
}
.customGrid td 
{ 
    padding: 5px; 
    border: solid 1px #cccccc !important; 
}
.ui-jqgrid-bdiv
{
 overflow-x : hidden !important;
}
</style>
<script type="text/javascript">

function openPopup(rowid)
{
	$("#customerEditDialog").load("${customerEditDialogUrl}?customerId="+rowid+"&ts="+new Date().getTime(), function() {
  		  $("#customerEditDialog").dialog({
  				modal:true,
  				title: 'Edit Customer',
  				width : 550,
  				height : 220,
  				closeOnEscape: false,
  				open: function(event, ui) {
  					
  				},
  				
  			});
  	});
	//alert("rowObject " + rowObject);
}
function viewActionFormatter(cellvalue, options, rowObject) {
	 var rowId = rowObject.childNodes[0].textContent;
	return "<button onclick=\"javascript: openPopup('"+rowId+"');\">Edit</button>";
}
$().ready(function() {
	 // Set up the jquery grid
    $("#jqTable").jqGrid({
        // Ajax related configurations
        url: "${getCustomerList}",
        datatype: "xml",
        mtype: "GET",
        colNames: ["id", "Name", "Address", "Email", "Contact","Action"],
        colModel: [
		{ name:'id', index:'id', sortable:false, hidden: true},
        { name: "name", index: 'name',sortable:false, width:'250',align: "left" },
        { name: "address", index: 'address', sortable:false, width:'350',align: "left" },
        { name: "email", index: 'email', sortable:false, width:'275',align: "left" },
        { name: "contact", index: 'contact',sortable:false,width:'250', align: "left"},
        { name: "action", index: 'action',sortable:false,width:'100', align: "center",formatter: viewActionFormatter}],
       
        xmlReader: { 
            root:"customers", 
            row:"customer",
            repeatitems:false,
            id : "id"
        },
        autoWidth:true,
        rownumbers :true,
		scrollOffset: 0,
		shrinkToFit: true,
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
	   	}
    })
    $("#customerForm").validate({
		rules: {
			name: {
				required: true,
			},
			address: {
				required: true,
			},	
			email: {
				required: true,
				email: true,
			},	
			contact: {
				required: true,
			}
		},
		messages: {
			name: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			address: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			email: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			contact: {
				required: '<spring:message code="error.above.field.required"/>',
			}
		}
	});
    $(function() {
		$(window).resize(function() {
			var setSize = $(window).height();
			setSize = setSize - 100;
			$(".topmostContainer").css("height", setSize);
		});
	});
	$(".topmostContainer").css("overflow", "auto");
	$(".topmostContainer").css("height", $(window).height() - 100);
	
	function editCustomerForm()
	{
		$("#customerEditDialog").load("${customerEditDialogUrl}?customerId="+rowid+"&ts="+new Date().getTime(), function() {
	  		  $("#customerEditDialog").dialog({
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
	
});

</script>
<div class="topmostContainer">
<div class="outermostdiv">
<div class="outerContainer">
	<span ><spring:message code="customer.create"/></span>
	<div class="i1"></div>
	<div class="upperdiv">
		<div class="formContainer">
			<div style="clear: both"><span id="error" class="load-save-errors"></span></div>
			<div style="clear: both"><span id="confirm" class="save_confirmation"></span></div>
			<spring:url value="/saveCustomer.ems" var="submit" scope="request"/>
			<form:form id="customerForm" commandName="customer" method="post" action="${submit}">
				<div class="field">
					<div class="formPrompt"><span><spring:message code="customer.name"/></span></div>
					<div class="formValue"><form:input id="customerName" path="name" size="40"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="customer.address"/></span></div>
					<div class="formValue"><form:input id="customerAddress" path="address" size="40"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="customer.email"/></span></div>
					<div class="formValue"><form:input id="customerEmail" path="email" size="40"/></div>
				</div>
				<div class="field">
					<div class="formPrompt"><span><spring:message code="customer.contact"/></span></div>
					<div class="formValue"><form:input id="customerContact" path="contact" size="40"/></div>
				</div>
				
				
				<div class="field">
					<div class="formPrompt"><span></span></div>
					<div class="formValue"><input class="saveAction" id="submit" type="submit" value="<spring:message code='action.submit'/>"></input></div>
				</div>
				</br>
				</br>
			</form:form>
		</div>
		
	</div>
</div>
<div class="outerContainer">
	<span ><spring:message code="customer.list"/></span>
	<div class="i1"></div>
	<div class="innerContainer">
	<table id="jqTable"></table>
	</div>
</div>
<div id="customerEditDialog"></div>
</div>
</div>