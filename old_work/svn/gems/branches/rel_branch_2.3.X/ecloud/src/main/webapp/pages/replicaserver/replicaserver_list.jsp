<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/replicaserver/list/" var="getReplicaServerList" scope="request" />
<spring:url value="/services/org/replicaserver/delete/" var="deleteReplicaServer" scope="request" />
<spring:url value="/services/org/eminstance/listEmInstancesByReplicaServerId/" var="getEmInstanceCountByReplicaServerUrl" scope="request" />

<spring:url value="/replicaserver/create.ems" var="createReplicaServerUrl" scope="request" />
<spring:url value="/replicaserver/edit.ems" var="editReplicaServerUrl" scope="request" />

<style>
#divform{width:100%;}
#form1{float:left;}
#btn1{float:left;}
#btn2{float:left;}
 

</style>

<script type="text/javascript">


function viewActionFormatter(cellvalue, options, rowObject) {
	var rowId = rowObject.id;
	return "<div id=\"divForm\"><div id=\"btn1\"><button onclick=\"javascript: onEdit('"+rowId+"');\">Edit</button>&nbsp;</div><div id=\"btn2\"><button onclick=\"javascript: beforeDeleteReplicaServer('"+rowId+"');\">Delete</button></div></div>";
}

function onEdit(rowId){
	$("#replicaServerDetailsDialog").load("${editReplicaServerUrl}?id="+rowId+"&ts="+new Date().getTime()).dialog({
        title : "Edit Replica Server",
        width :  Math.floor($('body').width() * .30),
        minHeight : 250,
        modal : true
    });
}

function beforeDeleteReplicaServer(rowId){
	// get associated em instances count
	$.ajax({
		type: 'GET',
		url: "${getEmInstanceCountByReplicaServerUrl}"+rowId+"?ts="+new Date().getTime(),
		success: function(data){
			if(data != null){
				var xml=data.getElementsByTagName("emInstance");
				if(xml != undefined && xml.length != 0)
					alert("This Replica Server has EM Instance(s) associated with it. It cannot be deleted");
				else
				{
					onDelete(rowId);
				}
			}
		},
		dataType:"xml",
		contentType: "application/json; charset=utf-8"
	});
}


function onDelete(rowId)
{
	if(confirm("Are you sure you want to delete the Replica Server?") == true)
	{
		$.ajax({
	 		type: 'POST',
	 		url: "${deleteReplicaServer}"+rowId+"?ts="+new Date().getTime(),
	 		success: function(data){
				location.reload();
			},
			error: function(){
				alert("Replica Server failed to delete");
			},
	 		contentType: "application/xml; charset=utf-8"
	 	});
	}
}


$(document).ready(function() {
		clearLabelMessage();
		
		$('#newReplicaServerButton').click(function() {
	    	clearLabelMessage();
	        $("#replicaServerDetailsDialog").load("${createReplicaServerUrl}"+"?ts="+new Date().getTime()).dialog({
	            title : "New Replica Server",
	            width :  Math.floor($('body').width() * .30),
	            minHeight : 250,
	            modal : true
	        });
	        return false;
	    });
	
	 // Set up the jquery grid
		jQuery("#replicaServerTable").jqGrid({
	       // Ajax related configurations
	       url: "${getReplicaServerList}?ts="+new Date().getTime(),
	       datatype: "json",
	       mtype: "GET",
	       colNames: ["id","Name","IP","UID","Internal IP","Mac Id","Action"],
	       colModel: [
	       { name:'id', index:'id', hidden: true},
	       { name: 'name', index: 'name',sorttype:'string',width:'12%' },
	       { name: 'ip', index: 'ip',sorttype:'string',width:'12%' },
	       { name: 'uid', index: 'uid',sorttype:'string',width:'12%' },
	       { name: 'internalIp', index: 'internalIp',sorttype:'string',width:'12%' },
	       { name: 'macId', index: 'macId',sorttype:'string',width:'12%' },
	       { name: "action", index: 'action',sortable:false,width:'25%', align: "right",formatter: viewActionFormatter}],
	      
	       jsonReader: { 
	           root:"replicaServer", 
	           repeatitems:false,
	           id : "id"
	       },
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
	    		   if (data.replicaServer != undefined) {
				   		if (data.replicaServer.length == undefined) {
				   			// Hack: Currently, JSON serialization via jersey treats single item differently
				   			jQuery("#replicaServerTable").jqGrid('addRowData', 0, data.replicaServer);
				   		}
	    		   }
	    	   }
	    	   
	    	   ModifyGridDefaultStyles();
	    	   }
	   });

	
	$(".topmostContainer").css("overflow", "auto");
	$(".topmostContainer").css("height", $(window).height() - 100);
	
});

function ModifyGridDefaultStyles() {  
	   $('#' + "replicaServerTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "replicaServerTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "replicaServerTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#replicaServerTable").setGridWidth($(window).width()-80);	
}

function displayLabelMessage(Message, Color) {
		$("#message").html(Message);
		$("#message").css("color", Color);
}

function clearLabelMessage(Message, Color) {
		displayLabelMessage("", "black");
}

//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	$("#replicaServerTable").setGridWidth($(window).width()-80);
}).trigger('resize');

</script>

<div id="replicaServerDetailsDialog"></div>

<div class="topmostContainer">
<div class="outermostdiv">
<div class="outerContainer">
	
		<div class="innerContainer" >
			<div style="font-weight: bolder; ">Replica Servers</div>
			<button id="newReplicaServerButton">Add</button>	
			<div class="i1"></div>
			<div id="message" style="font-size: 14px; font-weight: bold;padding: 5px 0 0 5px;" ></div>
			<table id="replicaServerTable"></table>
		</div>
	</div>
</div>
</div>
