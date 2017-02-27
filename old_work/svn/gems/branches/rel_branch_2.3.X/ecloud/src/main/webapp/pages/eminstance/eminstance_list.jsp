<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/eminstance/listbycutomerid/" var="getEmInstanceList" scope="request" />
<spring:url value="/services/org/eminstance/loademinstbycustomerid/" var="getEmInstbyCustomerId" scope="request" />
<spring:url value="/services/org/eminstance/delete/" var="deleteEmInstance" scope="request" />

<spring:url value="/eminstance/create.ems?customerId=${customerId}" var="createEmInstanceUrl" scope="request" />
<spring:url value="/eminstance/edit.ems" var="editmInstanceUrl" scope="request" />

<spring:url value="/eminstance/scheduleUpgrade.ems" var="scheduleUpgradeEmInstanceUrl" scope="request" />

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

    clearLabelMessage();
	start(1, "desc");
	$("#emInstanceTable").setGridWidth($(window).width() - 25);

	var status = getParameterByName("deleteStatus");
	if(status)
	{
		if(status == 0)
			displayLabelMessage("EM server instance deleted successfully", "green");
		else
			displayLabelMessage("EM server instance failed to delete", "red");
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
	return "<div id=\"divForm\"><div id=\"btn1\"><button onclick=\"javascript: onEdit('"+rowId+"');\">Edit</button>&nbsp;</div><div id=\"btn2\"><button onclick=\"javascript:alert('Delete feature coming shortly');\">Delete</button></div></div>";
}

function viewScheduleFormatter(cellvalue, options, rowObject) {
	var rowId = rowObject.id;
	return "<button onclick=\"javascript: scheduleEM('"+rowId+"');\">Sched</button> ";
}

function viewDownloadFormatter(cellvalue, options, rowObject) {
	var rowId = rowObject.id;
	return "<button onclick=\"javascript: onDownload('"+rowId+"');\">Down</button> ";
}

function scheduleEM(rowId){
	$("#emScheduleUpgradeDialog").load("${scheduleUpgradeEmInstanceUrl}?emInstanceId="+rowId+"&ts="+new Date().getTime()).dialog({
        title : "Schedule Upgrade of EM Instance",
        width :  Math.floor($('body').width() * .30),
        minHeight : 250,
        modal : true
    });
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
        width :  Math.floor($('body').width() * .40),
        minHeight : 250,
        modal : true
    });
}

function onDownload(rowId){
	window.location = "downloadcert.ems?emInstanceId="+rowId;
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

function ModifyGridDefaultStyles() {  
	   $('#' + "emInstanceTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "emInstanceTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "emInstanceTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#emInstanceTable").setGridWidth($(window).width()-25);	
}

function displayLabelMessage(Message, Color) {
		$("#message").html(Message);
		$("#message").css("color", Color);
}

function clearLabelMessage(Message, Color) {
		displayLabelMessage("", "black");
}

//function for pagination
function start(pageNum, orderWay) {
		jQuery("#emInstanceTable").jqGrid({
			url: "${getEmInstbyCustomerId}"+"${customerId}"+"?ts="+new Date().getTime(),
			mtype: "POST",
			datatype: "json",
			autoencode: true,
			hoverrows: false,
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			formatter: {
				 integer: {thousandsSeparator: ",", defaultValue: '0'},
			     number: {decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, defaultValue: '0.00'}
			},
			colNames: ["id", "Name", "Version", "Mac Id","Contact Name","Contact Email","EM Address","Contact Phone","Last connectivity","Health","SPPA Price","Details","Action"],
		       colModel: [
		       { name:'id', index:'id', hidden: true},
		       { name: 'name', index: 'name',sorttype:'string',width:'10%' },
		       { name: "version", index: 'version', sortable:false,width:'6%' },
		       { name: "macId", index: 'mac_id', sortable:false,width:'8%' },
		       { name: 'contactName', index: 'contactName',sorttype:'string',width:'8%' },
		       { name: "contactEmail", index: 'contactEmail', sortable:false,width:'8%' },
		       { name: "address", index: 'address', sortable:false,width:'8%' },
		       { name: "contactPhone", index: 'contactPhone', sortable:false,width:'8%' },
		       { name: "utcLastConnectivityAt", index: 'utcLastConnectivityAt',sortable:false,width:'10%'},
		       { name: "healthOfEmInstance", index: 'healthOfEmInstance',sortable:false,width:'10%'},
		       { name: "sppaPrice", index: 'sppaPrice',sortable:false,width:'6%'},
		       { name: 'details',index:'details', align:"center", sortable:false, width:"6%", formatter: viewDetailsFormatter},
		       { name: "action", index: 'action',sortable:false,width:'10%', align: "right",formatter: viewActionFormatter}],
		       
		   	jsonReader: { 
				root:"emInsts", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    pager: '#emInstancePagingDiv',
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
		   			if (data.emInsts != undefined) {
				   		if (data.emInsts.length == undefined) {
				   			jQuery("#emInstanceTable").jqGrid('addRowData', 0, data.emInsts);
				   		}
				   	}
		   		}
		   		
		   		ModifyGridDefaultStyles();
		   		
		   	}

		});
		
		jQuery("#emInstanceTable").jqGrid('navGrid',"#emInstancePagingDiv",
										{edit:false,add:false,del:false,search:false}, 
										{}, 
										{}, 
										{}, 
										{},
										{});
		
		forceFitEmInstanceTableWidth();
	}
//function for pagination

	function forceFitEmInstanceTableWidth(){
		var jgrid = jQuery("#emInstanceTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#emInstancePagingDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 10);
		
		$("#emInstanceTable").setGridWidth($(window).width() - 25);
	}
	
	function viewDetailsFormatter(cellvalue, options, rowObject) {
		var rowId = rowObject.id;
		return "<div id=\"form1\"><form action=\"vieweminstancedetails.ems\"  method=POST name=\"form1\"><input type=\"hidden\" name=\"emInstanceId\" value ='"+rowId+"' ></input><button onclick=\"document.form1.submit();\">Details</button>&nbsp;</form></div>";
	}
	
//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	$("#emInstanceTable").setGridWidth($(window).width()-25);
}).trigger('resize');

</script>

<div id="emInstanceDetailsDialog"></div>

<div id="emScheduleUpgradeDialog"></div>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="outerDiv" style="padding: 5px 5px 5px 5px">
			<div style="font-weight: bolder; "><spring:message code="eminstance.customer"/> ${customerName}</div>
			<button id="newEmInstanceButton">Add</button><br>	
			<div style="min-height:5px"></div>
    </div>
	<div style="padding: 0px 5px;">
		<table id="emInstanceTable"></table>
		<div id="emInstancePagingDiv"></div>
	</div>
 </div>