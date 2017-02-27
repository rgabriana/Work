<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/eminstance/listbycutomerid/" var="getEmInstanceList" scope="request" />
<spring:url value="/services/org/eminstance/delete/" var="deleteEmInstance" scope="request" />
<spring:url value="/services/org/upgrades/downloaddebianfile" var="getDebianUrl" scope="request" />

<spring:url value="/services/org/upgrades/listUpgrades/" var="getUpgradesList" scope="request" />

<style>

</style>
<script type="text/javascript">


$().ready(function() {
		
	 // Set up the jquery grid
		jQuery("#UpgradesTable").jqGrid({
	       // Ajax related configurations
	       url: "${getUpgradesList}"+"?ts="+new Date().getTime(),
	       datatype: "json",
	       mtype: "GET",
	       colNames: ["id", "Type", "Name", "Action"],
	       colModel: [
		   { name:'id', index:'id', hidden: true},
	       { name: "type", index: 'type', sorttype:'string',width:'8%' },
	       { name: "name", index: 'name', sortable:false,width:'12%' },
	       { name: "location", index: 'location',sortable:false,width:'15%',formatter: downloadButtonRenderer}],
	      
	       jsonReader: { 
	           root:"upgrades",	           
	           repeatitems:false,
	           id : "id"
	       },
	       rownumbers :true,
	       autoWidth:true,
	       scrollOffset: 0,
		   shrinkToFit: true,
	       viewrecords: true,
	       loadonce: true,
           sortable:true,
	       sortname: 'name',
	       sortorder: 'asc',
		   loadComplete: function(data) {
			   if (data != null){
	    		   if (data.upgrades != undefined) {
				   		if (data.upgrades.length == undefined) {
				   			// Hack: Currently, JSON serialization via jersey treats single item differently
				   			jQuery("#UpgradesTable").jqGrid('addRowData', 0, data.upgrades);
				   		}
	    		   }
	    	   }
	    	   
	    	   ModifyGridDefaultStyles();
	    	}
	   });
	
	forceFitUnRegEmTableWidth();
	
});

function downloadButtonRenderer(cellvalue, options, rowObject){
		var source = "";	
		source = "<button onclick=\"javascript: download('" + rowObject.id + "');\">Download</button>";				
		return source;
	}
 

function ModifyGridDefaultStyles() {  
	   $('#' + "UpgradesTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "UpgradesTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "UpgradesTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
}


//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	//$("#UnRegEmInstanceTable").setGridWidth($(window).width()-25);
	forceFitUnRegEmTableWidth();
}).trigger('resize');

function forceFitUnRegEmTableWidth(){
	var jgrid = jQuery("#UpgradesTable");
	var containerHeight = $("body").height();
	var headerHeight = $("#header").height();
	var footerHeight = $("#footer").height();
	var outerDivHeight = $("#outerDiv").height();
	var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
	
	jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - 10);
	
	$("#UpgradesTable").setGridWidth($(window).width() - 25);
}

function upload()
{
	var isValid = $('#upload').valid();	
	$("#upload").submit();
	if(isValid) {   
	       	
	}	
}

function download(id)
{
var debId = id;
$('#aid').attr("value",debId);
$('#downloadForms').submit();
//$.ajax({
//		url: "${getDebianUrl}"+debId+"?ts="+new Date().getTime(),
//		type: "GET",s
//		contentType: "application/deb; charset=utf-8",
//		success: function(data) {
//			alert("Done"+data);
//		}
//	});	
}
</script>
<spring:url value="/upgrades/uploadImageFile.ems" var="uploadFileURL" scope="request"/>
<form:form id="upload" modelAttribute="upgradeFile" action="${uploadFileURL}" method="post" enctype="multipart/form-data">
				
						<form:input path="fileData" type="file"/>				
				
				
						<input id="uploadsubmit" type="button" onclick="upload();" value="Upload Now" />					
				
</form:form>
<form id="downloadForms" action="${getDebianUrl}" METHOD="POST">
		<input id="aid" name="aid" type="hidden"/>		
</form>
<div style="width: 100%;">
	<div id="outerDiv">
		<div style="padding: 5px 0px 5px 5px;font-weight: bolder; ">Available Upgrades</div>
	</div>
	<div style="overflow: auto">
		<table id="UpgradesTable"></table>
		
	</div>
</div>