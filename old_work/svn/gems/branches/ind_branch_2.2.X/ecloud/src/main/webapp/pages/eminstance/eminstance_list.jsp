<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/eminstance/listbycutomerid/" var="getEmInstanceList" scope="request" />
<spring:url value="/services/org/eminstance/delete/" var="deleteEmInstance" scope="request" />

<spring:url value="/eminstance/create.ems?customerId=${customerId}" var="createEmInstanceUrl" scope="request" />
<spring:url value="/eminstance/edit.ems" var="editmInstanceUrl" scope="request" />

<style>
#divform{width:100%;}
#form1{float:left;}
#btn1{float:left;}
#btn2{float:left;}
 

</style>

<script type="text/javascript">

$(document).ready(function() {
    //define configurations for dialog
    var emInstanceDialogOptions = {
        title : "New EM Instance",
        modal : true,
        autoOpen : false,
        height : 300,
        width : 500,
        draggable : true
    }

    $('#newEmInstanceButton').click(function() {
    	clearLabelMessage();
        $("#emInstanceDetailsDialog").load("${createEmInstanceUrl}"+"&ts="+new Date().getTime()).dialog({
            title : "New EM Instance",
            width :  Math.floor($('body').width() * .30),
            minHeight : 250,
            modal : true
        });
        return false;
    });
});

function viewActionFormatter(cellvalue, options, rowObject) {
	var rowId = rowObject.id;
	return "<div id=\"divForm\"><div id=\"form1\"><form action=\"viewemstats.ems\" method=POST name=\"form1\"><input type=\"hidden\" name=\"emInstanceId\" value ='"+rowId+"' ></input><button onclick=\"document.form1.submit();\">Health Stats</button>&nbsp;</form></div><div id=\"btn1\"><button onclick=\"javascript: onEdit('"+rowId+"');\">Edit</button>&nbsp;</div><div id=\"btn2\"><button onclick=\"javascript: onDelete('"+rowId+"');\">Delete</button></div></div>";
}

function onActivate(rowId)
{
	window.location = "/ecloud/eminstance/map.ems?id="+rowId;
}

function onViewStats(rowId)
{
	window.location = "viewemstats.ems?emInstanceId="+rowId;
}

function onEdit(rowId){
	$("#emInstanceDetailsDialog").load("${editmInstanceUrl}?emInstanceId="+rowId+"&ts="+new Date().getTime()).dialog({
        title : "Edit EM Instance",
        width :  Math.floor($('body').width() * .30),
        minHeight : 250,
        modal : true
    });
}
function onDelete(rowId)
{
	if(confirm("Are you sure you want to delete the EM server instance?") == true)
	{
		$.ajax({
	 		type: 'POST',
	 		url: "${deleteEmInstance}"+rowId+"?ts="+new Date().getTime(),
	 		success: function(data){
				//window.location = "/ecloud/eminstance/list.ems?deleteStatus=0";
				location.reload();
			},
			error: function(){
				//window.location = "/ecloud/eminstance/list.ems?deleteStatus=1";
				alert("EM server instance failed to delete");
			},
	 		contentType: "application/xml; charset=utf-8"
	 	});
	}
}

function getParameterByName(name) 
{ 
  name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]"); 
  var regexS = "[\\?&]" + name + "=([^&#]*)"; 
  var regex = new RegExp(regexS); 
  var results = regex.exec(window.location.search); 
  if(results == null) 
    return ""; 
  else 
    return decodeURIComponent(results[1].replace(/\+/g, " ")); 
} 

$().ready(function() {
		clearLabelMessage();
	
	 // Set up the jquery grid
		jQuery("#emInstanceTable").jqGrid({
	       // Ajax related configurations
	       url: "${getEmInstanceList}"+"${customerId}"+"?ts="+new Date().getTime(),
	       datatype: "json",
	       mtype: "GET",
	       colNames: ["id", "Name", "Version", "Mac Id", "Last connectivity","Health","Action"],
	       colModel: [
	       { name:'id', index:'id', hidden: true},
	       { name: 'name', index: 'name',sorttype:'string',width:'20%' },
	       { name: "version", index: 'version', sortable:false,width:'8%' },
	       { name: "macId", index: 'mac_id', sortable:false,width:'12%' },
	       { name: "lastConnectivityAt", index: 'last_connectivity_at',sortable:false,width:'15%'},
	       { name: "healthOfEmInstance", index: 'healthOfEmInstance',sortable:false,width:'15%'},
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
	       sortname: 'name',
	       sortorder: 'asc',
		   loadComplete: function(data) {
	    	   if (data != null){
	    		   if (data.emInstance != undefined) {
				   		if (data.emInstance.length == undefined) {
				   			// Hack: Currently, JSON serialization via jersey treats single item differently
				   			jQuery("#emInstanceTable").jqGrid('addRowData', 0, data.emInstance);
				   		}
	    		   }
	    	   }
	    	   
	    	   ModifyGridDefaultStyles();
	    	   }
	   });

	var status = getParameterByName("deleteStatus");
	if(status)
	{
		if(status == 0)
			displayLabelMessage("EM server instance deleted successfully", "green");
		else
			displayLabelMessage("EM server instance failed to delete", "red");
	}
	
	$(".topmostContainer").css("overflow", "auto");
	$(".topmostContainer").css("height", $(window).height() - 100);
	
});

function ModifyGridDefaultStyles() {  
	   $('#' + "emInstanceTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "emInstanceTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "emInstanceTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#emInstanceTable").setGridWidth($(window).width()-80);	
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
	$("#emInstanceTable").setGridWidth($(window).width()-80);
}).trigger('resize');

</script>

<div id="emInstanceDetailsDialog"></div>

<div class="topmostContainer">
<div class="outermostdiv">
<div class="outerContainer">
	
		<div class="innerContainer" >
			<div style="font-weight: bolder; "><spring:message code="eminstance.customer"/> ${customerName}</div>
			<button id="newEmInstanceButton">Add</button>	
			<div class="i1"></div>
			<div id="message" style="font-size: 14px; font-weight: bold;padding: 5px 0 0 5px;" ></div>
			<table id="emInstanceTable"></table>
		</div>
	</div>
</div>
</div>
