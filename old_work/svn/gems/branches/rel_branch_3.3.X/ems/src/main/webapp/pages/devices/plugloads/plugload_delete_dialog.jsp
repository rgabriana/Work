<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %> 
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<spring:url value="/services/org/plugload/decommission/" var="decommissionPlugloadUrl" scope="request" />
<spring:url value="/services/org/plugload/forcedelete/" var="forcedeletePlugloadUrl" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Assign Users</title>

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

<style>
	 td.fdd-button-row{background-color: #EEEEEE;}
</style>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
</style>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Plugload Deletion</title>

<script type="text/javascript">
var plugloadFrame = document.getElementById("installFrame").contentWindow.document.getElementById("plugloadsFrame").contentWindow.document;
var plugloadGrid = plugloadFrame.getElementById("plugloadTable");

var deleteplugloadData = [];
var forcefullyDeleteplugloadData = [];
var DELETE_COUNT = 0;
var FORCE_DELETED_COUNT = 0;
var isRetry = false;

$(document).ready(function() {
	createDeletedplugloadGrid();
	getSelectedplugloadsToDelete();
	disableButtons(true);
	deleteplugloadsOneByOne();
});

function getSelectedplugloadsToDelete(){
	var selIds = $(plugloadGrid).getGridParam('selarrrow');
	var fixNum = selIds.length;
	for(var i=0; i<fixNum; i++){
		var plugloadRow = $(plugloadGrid).jqGrid('getRowData', selIds[i]);
		
		var fxJson = {};
		fxJson.id = plugloadRow.id;
		fxJson.plugloadName = plugloadRow.plugloadName;
		fxJson.deletestatus = "Waiting...";
		
		deleteplugloadData.push(fxJson);	
		
		// in case of retry attempt, update existing cell status
		if(isRetry == false)
			jQuery("#plugload-deleting-table").jqGrid('addRowData', fxJson.id, fxJson);
		else
			jQuery("#plugload-deleting-table").jqGrid('setCell', fxJson.id, "deletestatus", fxJson.deletestatus);
	}
}

function createDeletedplugloadGrid(){	
	jQuery("#plugload-deleting-table").jqGrid({
		datatype: "local",
		autowidth: true,
		scrollOffset: 0,
		hoverrows: false,
		forceFit: true,
		height: "200px",
	   	colNames:['id', 'Plugload Name', 'Delete Status'],
	   	colModel:[
  	   		{name:'id', index:'id', hidden: true},
	   		{name:'plugloadName', index:'plugloadName', sortable:false, width:"218px"},
	   		{name:'deletestatus', index:'deletestatus', sortable:false, width:"218px"}
	   	],
	    viewrecords: true,
	    gridComplete: function(){
	    	ModifyGridDefaultStyles();
	    }
	});
	
}

function ModifyGridDefaultStyles() {  
	   $('#' + "plugload-deleting-table" + ' tr').removeClass("ui-widget-content");
	   $('#' + "plugload-deleting-table" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "plugload-deleting-table" + ' tr:nth-child(odd)').addClass("oddTableRow");
}

function deleteplugloadsOneByOne(){	
	if(deleteplugloadData.length == 0){
		var fixNum = jQuery('#plugload-deleting-table').jqGrid('getGridParam', 'records');
		if(fixNum > 0){			
			setDeleteplugloadMessage("Some Plugload(s) were not reachable. Click on Retry if they become reachable. Click on Force Delete to forcefully delete them from Energy Manager.");
		} else {
			setDeleteplugloadMessage("All Plugload(s) are deleted successfully.");
		}
		disableButtons(false);
		return false;
	}
		
	var plugload = deleteplugloadData.shift();
	jQuery("#plugload-deleting-table").jqGrid('setCell', plugload.id, "deletestatus", "Processing...");
	var fxXML= "<plugloads><plugload><id>"+plugload.id+"</id></plugload></plugloads>";
		
	$.ajax({
		type: 'POST',
		url: "${decommissionPlugloadUrl}?v="+DELETE_COUNT,
		data: fxXML,
		success: function(data){
			if(data != null){				
				var xml=data.getElementsByTagName("response");
				for (var j=0; j<xml.length; j++) {
					var status = xml[j].getElementsByTagName("status")[0].childNodes[0].nodeValue;
					var currplugloadId = xml[j].getElementsByTagName("msg")[0].childNodes[0].nodeValue;
					
					if(status==1){ // success: remove from list
						jQuery("#plugload-deleting-table").jqGrid('delRowData', currplugloadId);
					} else { // failed: plugload not reachable
						jQuery("#plugload-deleting-table").jqGrid('setCell', currplugloadId, "deletestatus", "Plugload not reachable");
					}
				}
			}
		},
		complete: function(){
			DELETE_COUNT++;
			deleteplugloadsOneByOne();
		},
		dataType:"xml",
		contentType: "application/xml; charset=utf-8",
	});
}

function forcefullyDeleteplugloadsOneByOne(){
	if(forcefullyDeleteplugloadData == 0){
		var fixNum = jQuery('#plugload-deleting-table').jqGrid('getGridParam', 'records');
		if(fixNum == 0){			
			setDeleteplugloadMessage("All plugload(s) are deleted successfully.");
		}
		disableButtons(false);
		return false;
	}
	
	var plugload = forcefullyDeleteplugloadData.shift();
	jQuery("#plugload-deleting-table").jqGrid('setCell', plugload.id, "deletestatus", "Processing...");
	var plXML= "<plugloads><plugload><id>"+plugload.id+"</id></plugload></plugloads>";

	$.ajax({
		type: 'POST',
		url: "${forcedeletePlugloadUrl}?v="+FORCE_DELETED_COUNT,
		data: plXML,
		success: function(data){			
			if(data != null){				
				var xml=data.getElementsByTagName("response");
				for (var j=0; j<xml.length; j++) {
					var status = xml[j].getElementsByTagName("status")[0].childNodes[0].nodeValue;
					var currplugloadId = xml[j].getElementsByTagName("msg")[0].childNodes[0].nodeValue;
					
					if(status==1){// success: remove from list
						jQuery("#plugload-deleting-table").jqGrid('delRowData', currplugloadId);
					}
				}
			}
		},
		complete: function(){
			FORCE_DELETED_COUNT++;
			forcefullyDeleteplugloadsOneByOne();
		},
		dataType:"xml",
		contentType: "application/xml; charset=utf-8",
	});
}

function disableButtons(isDisable){
	if(isDisable){
		$("#fdd-close-btn").attr("disabled", true);
		$("#fdd-retry-btn").attr("disabled", true);
		$("#fdd-retry-btn").hide();
		$("#fdd-forcedelete-btn").attr("disabled", true);
		$("#fdd-forcedelete-btn").hide();
	}else{
		$("#fdd-close-btn").removeAttr("disabled");
		var fixNum = jQuery('#plugload-deleting-table').jqGrid('getGridParam', 'records');
		if(fixNum > 0) {
			$("#fdd-retry-btn").show();
			$("#fdd-forcedelete-btn").show();
		}
		$("#fdd-retry-btn").removeAttr("disabled");
		$("#fdd-forcedelete-btn").removeAttr("disabled");
	}
}

function deleteCloseHandler(){
	    
	$("#deletePlugloadDialog").dialog("close");
}

function deleteRetryHandler(){	
	setDeleteplugloadMessage("");	
	isRetry = true;
	getSelectedplugloadsToDelete();
	disableButtons(true);
	deleteplugloadsOneByOne();
}

function deleteForcefullyHandler(){
    var bProceed = false;
	$('<div></div>').appendTo('body')
	  .html('<div><h3>Do you want to continue deleting unreachable plugload(s)?<h3></div>')
	  .dialog({
	      modal: true, title: 'Confirm', zIndex: 10000, autoOpen: true,
	      width: 'auto', resizable: false,
	      buttons: {
	          Yes: function () {
	              bProceed = true;
	              $(this).dialog("close");
					disableButtons(true);
					
					var undeletedplugloads = jQuery('#plugload-deleting-table').jqGrid('getRowData');
					for(var i=0; i<undeletedplugloads.length; i++){
						forcefullyDeleteplugloadData.push(undeletedplugloads[i]);
						jQuery("#plugload-deleting-table").jqGrid('setCell', undeletedplugloads[i].id, "deletestatus", "Waiting...");
					}					
				forcefullyDeleteplugloadsOneByOne();					
	          },
	          No: function () {
	              $(this).dialog("close");
	          }
	      },
	      close: function (event, ui) {
	          $(this).remove();
	      }
	});
}

function setDeleteplugloadMessage(message){
	$("#delete-plugload-message").html(message);
}
</script>
</head>
<body id="fdd-main-body">
<table style="margin:5px;width:450px;height:300px;">
	<tr>
		<td>
			<div id="delete-plugload-message"></div>
		</td>
	</tr>
	<tr>
		<td valign="top">
			<table id="plugload-deleting-table" style="width:450px;height:100%;"></table>
		</td>
	</tr>
	<tr>
		<td align="center" class="fdd-button-row">
			<button id="fdd-close-btn" onclick="javascript: deleteCloseHandler();">Close</button>
			<button id="fdd-retry-btn" onclick="javascript: deleteRetryHandler();">Retry</button>
			<button id="fdd-forcedelete-btn" onclick="javascript: deleteForcefullyHandler();">Force Delete</button>
		</td>
	</tr>
</table>

</body>
</html>