<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/eminstance/listUnRegEmInstance/" var="getUnRegEmInstanceList" scope="request" />

<spring:url value="/eminstance/activate.ems" var="activatEmInstanceUrl" scope="request" />

<style>

</style>

<script type="text/javascript">

function viewActionFormatter(cellvalue, options, rowObject) {
	var rowId = rowObject.id;
	return "<button onclick=\"javascript: activateEM('"+rowId+"');\">Activate</button> ";
}

function activateEM(rowId){
	$("#emActivateDialog").load("${activatEmInstanceUrl}?emInstanceId="+rowId+"&ts="+new Date().getTime()).dialog({
        title : "Activate EM Instance",
        width :  Math.floor($('body').width() * .30),
        minHeight : 250,
        modal : true
    });
}

$().ready(function() {
		
	 // Set up the jquery grid
		jQuery("#UnRegEmInstanceTable").jqGrid({
	       // Ajax related configurations
	       url: "${getUnRegEmInstanceList}"+"?ts="+new Date().getTime(),
	       datatype: "json",
	       mtype: "GET",
	       colNames: ["id", "Version", "Mac Id", "Last connectivity","Action"],
	       colModel: [
		   { name:'id', index:'id', hidden: true},
	       { name: "version", index: 'version', sorttype:'string',width:'8%' },
	       { name: "macId", index: 'mac_id', sortable:false,width:'12%' },
	       { name: "lastConnectivityAt", index: 'last_connectivity_at',sortable:false,width:'15%'},
	       { name: "action", index: 'action',sortable:false,width:'25%', align: "right",formatter: viewActionFormatter}],
	      
	       jsonReader: { 
	           root:"emInstance", 
	           repeatitems:false,
	           id : "id"
	       },
	       autoWidth:true,
	       scrollOffset: 0,
		   shrinkToFit: true,
	       viewrecords: true,
	       loadonce: true,
           sortable:true,
	       sortname: 'version',
	       sortorder: 'asc',
		   loadComplete: function(data) {
			   if (data != null){
	    		   if (data.emInstance != undefined) {
				   		if (data.emInstance.length == undefined) {
				   			// Hack: Currently, JSON serialization via jersey treats single item differently
				   			jQuery("#UnRegEmInstanceTable").jqGrid('addRowData', 0, data.emInstance);
				   		}
	    		   }
	    	   }
	    	   
	    	   ModifyGridDefaultStyles();
	    	}
	   });

		
	forceFitUnRegEmTableWidth();
	
});

function ModifyGridDefaultStyles() {  
	   $('#' + "UnRegEmInstanceTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "UnRegEmInstanceTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "UnRegEmInstanceTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
}


//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	//$("#UnRegEmInstanceTable").setGridWidth($(window).width()-25);
	forceFitUnRegEmTableWidth();
}).trigger('resize');

function forceFitUnRegEmTableWidth(){
	var jgrid = jQuery("#UnRegEmInstanceTable");
	var containerHeight = $("body").height();
	var headerHeight = $("#header").height();
	var footerHeight = $("#footer").height();
	var outerDivHeight = $("#outerDiv").height();
	var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
	
	jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - 10);
	
	$("#UnRegEmInstanceTable").setGridWidth($(window).width() - 25);
}

</script>

<div id="emActivateDialog"></div>

<div style="width: 100%;">
	<div id="outerDiv">
		<div style="padding: 5px 0px 5px 5px;font-weight: bolder; ">Unregistered EM Instances</div>
	</div>
	<div style="overflow: auto">
		<table id="UnRegEmInstanceTable"></table>
		
	</div>
 </div>
