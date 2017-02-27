<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/bacnetconfig/save" var="saveAllBacnetConfigDeatils" scope="request"/>
<spring:url value="/bacnet/allConfDetails.ems" var="bacnetConf" />

<script type="text/javascript">
var maxId = 0;
var isBacnetEnabled = "";

var isBacnetNetworkConfigured = "";

var isEnableBacnetChecked = "";

var MAX_ROW_NUM = 99999;
var lastsel = 0;
var url = "";
$('#saveconfirm').text("");
//$('#error').text("");
//var data = '[{"id":"1","point_name":"Energy Consumption - Lighting","point_instance_id":"1","device_name":"EM","device_id":"3001"},{"id":"2","point_name":"Energy Consumption - Plugload","point_instance_id":"2","device_name":"EM","device_id":"3001"},{"id":"3","point_name":"Emergency","point_instance_id":"3","device_name":"EM","device_id":"3001"},{"id":"4","point_name":"Demand Response Level","point_instance_id":"4","device_name":"EM","device_id":"3001"},{"id":"5","point_name":"Energy Consumption - Lighting","point_instance_id":"1","device_name":"Area1","device_id":"11000"},{"id":"6","point_name":"Energy Consumption - Plugload","point_instance_id":"2","device_name":"Area1","device_id":"11000"},{"id":"7","point_name":"Emergency","point_instance_id":"1","device_name":"Area1","device_id":"11000"}]';
$(document).ready(function() {
	url =  '<spring:url value="/services/bacnetconfig/loadBacnetReportConfigurationList"/>';
	start(1, "desc",url);
	


forceFitBacnetConfTableHeight();

isBacnetEnabled = "${isBacnetEnabled}";

isBacnetNetworkConfigured = "${isBacnetNetworkConfigured}";

if(isBacnetNetworkConfigured == "false"){
	$('#errorMessageId').text("Bacnet Network is not configured. Please configure Bacnet Network in Network Settings page.");
	$('#enableBacnet').prop("disabled", true);
	$('#bacnetReportConfGridGridSave').prop("disabled", true);
}else{
	$('#errorMessageId').text("");
}

//var mydata =  [];

//<c:forEach items="${reportConfigurationList}" var="reportConf">

// var localData = new Object;
// localData.id =  "${reportConf.id}";
// localData.deviceid = '<c:out value="${reportConf.deviceid}" escapeXml="true" />';			
// localData.objecttype =  '<c:out value="${reportConf.objecttype}" escapeXml="true" />';
//localData.objectinstance = '<c:out value="${reportConf.objectinstance}" escapeXml="true" />';			
//localData.objectname =  '<c:out value="${reportConf.objectname}" escapeXml="true" />';

//mydata.push(localData);
//</c:forEach>

/* var json = $.parseJSON(data);
$(json).each(function(i, val) {
	var localData = new Object;
  $.each(val, function(k, v) {
	  
	  if(k=="id"){
		  localData.id = v;
	  }
	  if(k=="point_name"){
		  localData.point_name = v;
	  }
	  if(k=="point_instance_id"){
		  localData.point_instance_id = v;
	  }
	  if(k=="device_name"){
		  localData.device_name = v;
	  }
	  if(k=="device_id"){
		  localData.device_id = v;
	  }
  });
  mydata.push(localData);
}); */

/* if(mydata)
{
	for(var i=0;i<mydata.length;i++)
	{
		jQuery("#bacnetReportConfGrid").jqGrid('addRowData',mydata[i].id,mydata[i]);
	}
}
 */
jQuery("#bacnetReportConfGrid").jqGrid('navGrid',"#pBacnetConfDiv",{edit:false,add:false,del:false});

$("#bacnetReportConfGrid").jqGrid().setGridParam({sortname: 'name', sortorder:'asc'}).trigger("reloadGrid");
	
});

//function for pagination
function start(pageNum, orderWay,url) {
	jQuery("#bacnetReportConfGrid").jqGrid({
		url: url + "?ts="+new Date().getTime(),			
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
		colNames:['Id','Device Id' ,'Object Type', 'Object Instance', 'Object Name'],
	   	colModel:[   
	        {name:'id', index:'id', width:"10%", hidden:true},
		   	{name:'deviceid', index:'deviceid',sorttype:"int", width:"15%",editable: false},
	   		{name:'objecttype', index:'objecttype', sorttype:"string", width:"15%",editable: false},
	   		{name:'objectinstance', index:'objectinstance', sorttype:"int", width:"15%",editable: false},
	   		{name:'objectname', index:'objectname', sorttype:"string", width:"75%",editable: false}
	   	],
	   	jsonReader: { 
	        root:"bacnetReportConfiguration",
	        page:"page", 
	        total:"total", 
	        records:"records",
	        repeatitems:false,
	        id : "id"
	    },
	   	pager: '#pBacnetConfDiv',
	   	page: pageNum,
	   	sortorder: orderWay,
	   	sortname: "deviceid",
	    hidegrid: false,
	    viewrecords: true,
	   	loadui: "block",
	   	toolbar: [false,"top"],
	   	onSortCol: function(index, iCol, sortOrder) {
	   	},
	    loadComplete: function(data) {
	    	 ModifyGridDefaultStyles();
	    },
	});	
}

function ModifyGridDefaultStyles() { 
	$('#' + "bacnetReportConfGrid" + ' tr').removeClass("ui-widget-content");
	$('#' + "bacnetReportConfGrid" + ' tr:nth-child(even)').addClass("evenTableRow");
	$('#' + "bacnetReportConfGrid" + ' tr:nth-child(odd)').addClass("oddTableRow");
}



function forceFitBacnetConfTableHeight(){
	var jgrid = jQuery("#bacnetReportConfGrid");
	var containerHeight = $(this).height();
	var otherElementHeight = $("#others-list-topPanel").height();
	
	var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
	var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
	var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
	
	jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .80)); 
}

//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	$("#bacnetReportConfGrid").setGridWidth($(window).width()-20);
}).trigger('resize');

function exportBacnetReport() {
	$('#exportBRForm').submit();
}
</script>

<div class="outerContainer" style="height: 100%;" >
	<span id="errorMessageId" style="color:red"></span>
	<span id="error" style="color:red"></span>
	<span id="saveconfirm" style="color:green"></span>
	
	<div id="bacnetReportConfGridButtonDiv">
		<table style="width: 100%;">
			<tr>
				<td style="width:5%;"><input id="bacnetReportExportCSV" class="saveAction" type="button" value="Download Excel" onclick="exportBacnetReport()" ></td>
			</tr>
		</table>
	</div>
	<br/>
	
	<table id="bacnetReportConfGrid" class="entable" style="width: 100%; height: 100%;">
	<div id="pBacnetConfDiv"></div>
	</table>
</div>
<form id='exportBRForm' action=<spring:url value='/services/bacnetconfig/exportbacnetreportcsv'/> method='POST'></form>
<script type="text/javascript">
var error = '<%=request.getParameter("error")%>';
var saveconfirm = '<%=request.getParameter("saveconfirm")%>';
if(error == 'save_error') {
	$("#error").html('<spring:message code="error.bacnet.save"/>');
} else {
	error = '${error}';
}
if(error == 'load_error') {
	$("#error").html('<spring:message code="error.bacnet.load"/>');
}
if(saveconfirm == 'save_success') {
	$("#saveconfirm").html('<spring:message code="bacnet.save.confirmation"/>');
}
$(function() {
	$(window).resize(function() {
		var setSize = $(window).height();
		setSize = setSize - 50;
		$(".outerContainer").css("height", setSize);
	});
});
//$(".outerContainer").css("overflow-x", "auto");
$(".outerContainer").css("height", $(window).height() - 50);
</script>
