<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/eminstance/listbycutomerid/" var="getEmInstanceList" scope="request" />
<spring:url value="/services/org/eminstance/delete/" var="deleteEmInstance" scope="request" />

<spring:url value="/eminstance/create.ems?customerId=${customerId}" var="createEmInstanceUrl" scope="request" />

<style>

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
            width :  Math.floor($('body').width() * .35),
            minHeight : 300,
            modal : true
        });
        return false;
    });
});

function viewActionFormatter(cellvalue, options, rowObject) {
	var rowId = rowObject.childNodes[0].textContent;
	return "<button onclick=\"javascript: onActivate('"+rowId+"');\">Activate</button>  <button onclick=\"javascript: onDelete('"+rowId+"');\">Delete</button>";
}

function onActivate(rowId)
{
	window.location = "/ecloud/eminstance/map.ems?id="+rowId;
}

function onDelete(rowId)
{
	if(confirm("Are you sure you want to delete the EM server instance?") == true)
	{
		$.ajax({
	 		type: 'POST',
	 		url: "${deleteEmInstance}"+rowId+"?ts="+new Date().getTime(),
	 		success: function(data){
				window.location = "/ecloud/eminstance/list.ems?deleteStatus=0";
			},
			error: function(){
				window.location = "/ecloud/eminstance/list.ems?deleteStatus=1";
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
   $("#emInstanceTable").jqGrid({
       // Ajax related configurations
       url: "${getEmInstanceList}"+"${customerId}"+"?ts="+new Date().getTime(),
       datatype: "xml",
       mtype: "GET",
       colNames: ["id", "Name", "Version", "Timezone", "Last connectivity","Action"],
       colModel: [
		{ name:'id', index:'id', sortable:false, hidden: true},
       { name: "name", index: 'name',sortable:false, width:'250',align: "left" },
       { name: "version", index: 'version', sortable:false, width:'350',align: "left" },
       { name: "timeZone", index: 'time_zone', sortable:false, width:'275',align: "left" },
       { name: "lastConnectivityAt", index: 'last_connectivity_at',sortable:false,width:'200', align: "left"},
       { name: "action", index: 'action',sortable:false,width:'150', align: "center",formatter: viewActionFormatter}],
      
       xmlReader: { 
           root:"emInstances", 
           row:"emInstance",
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
	   		if (data.emInstance != undefined) {
			   		if (data.emInstance.length == undefined) {
			   			// Hack: Currently, JSON serialization via jersey treats single item differently
			   			jQuery("#emInstanceTable").jqGrid('addRowData', 0, data.eminstance);
			   		}
			   	}
		   	}
   })

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

function displayLabelMessage(Message, Color) {
		$("#message").html(Message);
		$("#message").css("color", Color);
	}

function clearLabelMessage(Message, Color) {
		displayLabelMessage("", "black");
	}

</script>

<div id="emInstanceDetailsDialog"></div>

<div class="topmostContainer">
<div class="outermostdiv">
<div class="outerContainer">
	<span ><spring:message code="eminstance.manage"/></span><br/>
	<button id="newEmInstanceButton">Add</button>	
	<div class="i1"></div>
		<div class="innerContainer">
			<div id="message" style="font-size: 14px; font-weight: bold;padding: 5px 0 0 5px;" ></div>
			<table id="emInstanceTable"></table>
		</div>
	</div>
</div>
</div>
