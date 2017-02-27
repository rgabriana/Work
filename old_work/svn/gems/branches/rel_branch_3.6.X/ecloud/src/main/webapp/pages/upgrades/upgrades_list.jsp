<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/eminstance/listbycutomerid/" var="getEmInstanceList" scope="request" />
<spring:url value="/services/org/eminstance/delete/" var="deleteEmInstance" scope="request" />
<spring:url value="/services/org/upgradesmanagement/downloaddebianfile" var="getDebianUrl" scope="request" />

<spring:url value="/services/org/upgradesmanagement/listUpgrades/" var="getUpgradesList" scope="request" />

<spring:url value="/services/org/upgradesmanagement/loadUpgradeFilesList/" var="getUpgradeFilesList" scope="request" />

<style>

</style>
<script type="text/javascript">


$().ready(function() {
		
	startUpgradeFileListTable(1, "desc");
	$("#UpgradesTable").setGridWidth($(window).width() - 25);
	
});

function startUpgradeFileListTable(pageNum, orderWay)
{
	 // Set up the jquery grid
		jQuery("#UpgradesTable").jqGrid({
	       // Ajax related configurations
	       url: "${getUpgradeFilesList}"+"?ts="+new Date().getTime(),
	       datatype: "json",
	       mtype: "POST",
	       autoencode: true,
		   hoverrows: false,
		   autowidth: true,
		   scrollOffset: 0,
		   forceFit: true,
	       colNames: ["id", "Type", "Name", "Action"],
	       colModel: [
		   { name:'id', index:'id', hidden: true},
	       { name: "type", index: 'type', sorttype:'string',width:'8%' },
	       { name: "name", index: 'name', sortable:false,width:'12%' },
	       { name: "location", index: 'location',sortable:false,width:'15%',formatter: downloadButtonRenderer}],
	      
	       jsonReader: { 
	           root:"upgradefiles", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
	       },
	       	cmTemplate: { title: false },
		    pager: '#upgradeFilesPagingDiv',
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
	    		   if (data.upgradefiles != undefined) {
				   		if (data.upgradefiles.length == undefined) {
				   			// Hack: Currently, JSON serialization via jersey treats single item differently
				   			jQuery("#UpgradesTable").jqGrid('addRowData', 0, data.upgradefiles);
				   		}
	    		   }
	    	   }
	    	   
	    	   ModifyGridDefaultStyles();
	    	}
	   });
	jQuery("#UpgradesTable").jqGrid('navGrid',"#upgradeFilesPagingDiv",
										{edit:false,add:false,del:false,search:false}, 
										{}, 
										{}, 
										{}, 
										{},
										{});
	forceFitUnRegEmTableWidth();

}

function downloadButtonRenderer(cellvalue, options, rowObject){
		var source = "";	
		source = "<button onclick=\"javascript: download('" + rowObject.id + "');\">Download</button>";				
		return source;
	}
 

function ModifyGridDefaultStyles() {  
	   $('#' + "UpgradesTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "UpgradesTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "UpgradesTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#UpgradesTable").setGridWidth($(window).width()-25);
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
	var gridFooterHeight = $("#fixtureClassPagingDiv").height();
	var gridDivUpperHeight = $("#divupperupgrade").height();
	jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight- gridDivUpperHeight - 30);
	
	$("#UpgradesTable").setGridWidth($(window).width() - 25);
}

function upload()
{
	if($('#fileId').val() != ""){
		$("#errorLabel").text("");
		$("#upload").submit();
		$('#uploadsubmit').attr("disabled", true);
	}else{
		$("#errorLabel").text("Please select a file");
		$("#errorLabel").css("color", "red");
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
<div id="overridecssstyle" style="overflow:hidden;">
<div id="divupperupgrade">
<form:form id="upload" modelAttribute="upgradeFile" action="${uploadFileURL}" method="post" enctype="multipart/form-data">
				
						<form:input path="fileData" type="file" id="fileId"/>				
				
				
						<input id="uploadsubmit" type="button" onclick="upload();" value="Upload Now" />
						<label id="errorLabel"></label>
				
</form:form>
<form id="downloadForms" action="${getDebianUrl}" METHOD="POST">
		<input id="aid" name="aid" type="hidden"/>		
</form>
</div>
<div style="width: 100%;height: 100%;background: #fff; padding: 0px 5px 0px 0px;">
	<div id="outerDiv">
		<div style="padding: 5px 0px 5px 5px;font-weight: bolder; ">Available Upgrades</div>
	</div>
	<div style="overflow: auto">
		<table id="UpgradesTable"></table>
		<div id="upgradeFilesPagingDiv"></div>
	</div>
</div>
</div>