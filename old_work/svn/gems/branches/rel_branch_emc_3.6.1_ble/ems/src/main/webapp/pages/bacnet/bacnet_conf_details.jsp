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
$('#saveconfirm').text("");
//$('#error').text("");
$(document).ready(function() {
jQuery("#bacnetConfTable").jqGrid({
	datatype: "local",
	autowidth: true,
	scrollOffset: 0,
	hoverrows: false,
	forceFit: true,
	colNames:['Id', 'Name', 'Value'],
   	colModel:[   
        {name:'id', index:'id', width:"20%", hidden:true},
	   	{name:'name', index:'name',sorttype:"string", width:"30%",editable: true},
   		{name:'value', index:'value', sortable:false, width:"30%",editable: true}
   	],
   	pager: '#pBacnetConfDiv',
    editurl: 'clientArray',
   	cmTemplate: { title: false },
	rowNum:MAX_ROW_NUM,
   	sortname: 'name',
    viewrecords: true,
    sortorder: 'asc',
    loadComplete: function() {
    	 ModifyGridDefaultStyles();
    },
    onSelectRow: function(id) {
        if (id && id != lastsel) {
    		if (lastsel != 0){
    			if(saveRow(lastsel)){
    				lastsel = id;
    			}else{
    				return;
    			}
    		}else {
    			lastsel = id;
    		}
        } else {
        	if (!saveRow(id)){
        		return ;
        	}
        }
        var rowData = jQuery('#bacnetConfTable').jqGrid('getRowData', id);
        var notAllowedNames = ["DBFile","DeviceNameFormatStringArea","DeviceNameFormatStringEM","DeviceNameFormatStringSwitchGroup","EnergyManagerInstance","EnergyManagerName","GemsIpAddress","LogLevel","ObjectNameFormatStringArea","ObjectNameFormatStringEM","ObjectNameFormatStringFixture","ObjectNameFormatStringPlugload","ObjectNameFormatStringSwitchGroup","ObjectsFile","RestApiKey","RestApiSecret","SwitchGroupBaseInstance","UpdateAreaTimeout","UpdateConfigTimeout","UpdateOccupancyTimeout","Interface","ObjectNameFormatStringAreaFixture","EnableSwitchgroups","ObjectNameFormatStringSwitchSceneDimLevel","ObjectNameFormatStringEMFixture","ObjectNameFormatStringAreaPlugload","ObjectNameFormatStringSwitchScenePlugLevel","InteractiveLogLevel","VendorId","DetailedMode","fixtureOccupancySensor","MaxAPDU","APDUTimeout"];
        var isEditable = true;
        for (var i =0; i < notAllowedNames.length; i++){
        	var nan = notAllowedNames[i];
        	if (nan == rowData.name){
        		isEditable = false;
        		break;
        	}
        }
        if (isEditable){
        	jQuery("#bacnetConfTable").jqGrid('editRow', id	, true);
        }else{
        	alert('This value not allowed to be edited');
        }
    },
});

forceFitBacnetConfTableHeight();

isBacnetEnabled = "${isBacnetEnabled}";

if(isBacnetEnabled == 'true'){
	document.getElementById("enableBacnet").checked = true;
	isEnableBacnetChecked = "true";
} else if(isBacnetEnabled == 'false') {
	document.getElementById("enableBacnet").checked = false;
	isEnableBacnetChecked = "false";
}

isBacnetNetworkConfigured = "${isBacnetNetworkConfigured}";

if(isBacnetNetworkConfigured == "false"){
	$('#errorMessageId').text("Bacnet Network is not configured. Please configure Bacnet Network in Network Settings page.");
	$('#enableBacnet').prop("disabled", true);
	$('#bacnetConfTableGridSave').prop("disabled", true);
}else{
	$('#errorMessageId').text("");
}

var mydata =  [];

<c:forEach items="${bacnetConfigurationDeatils}" var="bacnetConf">
	
	var localData = new Object;
	localData.id =  "${bacnetConf.id}";
	localData.name = '<c:out value="${bacnetConf.name}" escapeXml="true" />';			
	localData.value =  '<c:out value="${bacnetConf.value}" escapeXml="true" />';
						
	mydata.push(localData);
	maxId = Math.max(maxId, localData.id);
</c:forEach>

if(mydata)
{
	for(var i=0;i<mydata.length;i++)
	{
		jQuery("#bacnetConfTable").jqGrid('addRowData',mydata[i].id,mydata[i]);
	}
}

jQuery("#bacnetConfTable").jqGrid('navGrid',"#pBacnetConfDiv",{edit:false,add:false,del:false});

$("#bacnetConfTable").jqGrid().setGridParam({sortname: 'name', sortorder:'asc'}).trigger("reloadGrid");
	
});

function saveRow(idToSave) {
	//$('#error').text("");
	$('#saveconfirm').text("");
	var selectedRowId = jQuery("#bacnetConfTable").jqGrid('getGridParam', 'selrow');
    //console.log("selectedRowId" + selectedRowId + " idToSave " + idToSave + " ls" + lastsel);
    if (idToSave != null && idToSave != 'undefined') {
        selectedRowId = idToSave;
    } else {
    	selectedRowId = lastsel;
    }
    if (selectedRowId == null || selectedRowId == 'undefined') {
        alert('Please select the row to save');
        return false;
    }

    jQuery("#bacnetConfTable").jqGrid('saveRow', selectedRowId, false, 'clientArray');
    
    if (lastsel != 0){
		var rowData = jQuery('#bacnetConfTable').jqGrid('getRowData', lastsel);
    	if (rowData.name == undefined || rowData.name == null || rowData.name == '' || rowData.value == undefined || rowData.value == null || rowData.value == ''){
    		alert('Please enter Name/Value. Name/Value can not be empty');
    		jQuery("#bacnetConfTable").jqGrid('editRow', lastsel, true);
    		return false;
    	} else if (rowData.name == 'null' || rowData.value == 'null'){
    		alert('Name/Value can not be null');
    		jQuery("#bacnetConfTable").jqGrid('editRow', lastsel, true);
    		return false;
    	}
	}
    
    $("#bacnetConfTableGridAdd").attr("disabled", false);
    return true;
}

function addNewRow() {
	//$('#error').text("");
	$('#saveconfirm').text("");
	isAddedNew = true;
		var record = jQuery("#bacnetConfTable").jqGrid('getGridParam', 'records');
		
		var addNewparameters = {
			rowID : maxId + 1,
			initdata : {
				id :'',
				name : '',
				value:''
			},
			position : "last",
			useDefValues : false,
			useFormatter : false,
			addRowParams : {
				extraparam : {}
			}
		};
		maxId = maxId+1;
		var selectedRow = $("#bacnetConfTable").jqGrid('getGridParam', 'selrow');
		//console.log("add new row" + selectedRow);
		jQuery("#bacnetConfTable").jqGrid('addRow', addNewparameters);
		$("#bacnetConfTableGridAdd").attr("disabled", true);
		//scrollToRow('#bacnetConfTable', record);
}
function isRowEdited(element, id) {
    var edited = "0";
    var ind = element.getInd(id, true);
    if (ind != false) {
        edited = $(ind).attr("editable");
    }
    if (edited == "1") {
        // row is being edited
        return true;
    } else {
        // row is not being edited
        return false;
    }
}
function saveAllRowsInGrid(grd){
	var record = grd.jqGrid('getGridParam', 'records');
    for (var i = 1; i <= record; i++) {
    	if(isRowEdited(grd, i)){
    		if (!saveRow(i)){
    			return false;
    		}
    	}
    }
    return true;
    //var rowData = grd.jqGrid('getRowData');
    //console.log(JSON.stringify(rowData));
}

function saveBACnetConfiguration(){
	//$('#error').text("");
	$('#saveconfirm').text("");
	beforeSavingBacnetConfig();
	
	var selectedRowId = jQuery("#bacnetConfTable").jqGrid('getGridParam', 'selrow');
	if(!saveRow(selectedRowId)){
		return false;
	}
	if (!saveAllRowsInGrid(jQuery("#bacnetConfTable"))){
		return false;	
	}
	//console.log("SBACnet selectedRowId" + selectedRowId);
	var gridData = jQuery("#bacnetConfTable").getRowData();
	var postData = JSON.stringify(gridData);
	postData = '{ "baCnetConfiguration": ' + postData + '}';
	
	$.ajax({
		type : "POST",
		url : "${saveAllBacnetConfigDeatils}"+"/"+isEnableBacnetChecked,
		data : postData,
		dataType : "json",
		contentType : "application/json; charset=utf-8",
		success : function(response, textStatus, xhr) {
			console.log("success");
			$("#saveconfirm").html('<spring:message code="bacnet.save.confirmation"/>');
			$("#bacnetConfTableGridAdd").attr("disabled", false);
			var ifr = document.getElementById("bconfFrame");
			if( ifr != null || ifr != undefined ){
				ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
				ifr.src = ifr.src + new Date().getTime();
				ifr.src = "${bacnetConf}";
			}	
			$("#bacnetConfTable").jqGrid().setGridParam({sortname: 'name', sortorder:'asc'}).trigger("reloadGrid");
			return true;
		},
		error : function(xhr, textStatus, errorThrown) {
			$("#error").html('<spring:message code="bacnet.save.confirmation"/>');
			return false;
		}
	});
}

function ModifyGridDefaultStyles() { 
	$('#' + "bacnetConfTable" + ' tr').removeClass("ui-widget-content");
	$('#' + "bacnetConfTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	$('#' + "bacnetConfTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
}

function beforeSavingBacnetConfig(){
	if ($('#enableBacnet').is(':checked')) {
		isEnableBacnetChecked = "true";
		if(isBacnetEnabled == "true"){
			if(confirm('Changing any parameter will restart BACnet services. Do you really want to proceed?')){
				return true;
			}else{
				return false;
			}
		}else{
			return true;
		}
	}else{
		isEnableBacnetChecked = "false";
		if(confirm('Enable BACnet parameter is unchecked - This will stop BACnet services. Do you really want to proceed?')){
			return true;
		}else{
			return false;
		}
	}
	return false;
}

function forceFitBacnetConfTableHeight(){
	var jgrid = jQuery("#bacnetConfTable");
	var containerHeight = $(this).height();
	var otherElementHeight = $("#others-list-topPanel").height();
	
	var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
	var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
	var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
	
	jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .80)); 
}

//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	$("#bacnetConfTable").setGridWidth($(window).width()-20);
}).trigger('resize');
</script>

<div class="outerContainer" style="height: 100%;" >
	<span id="errorMessageId" style="color:red"></span>
	<span id="error" style="color:red"></span>
	<span id="saveconfirm" style="color:green"></span>
	<!-- <div class="i1"></div> -->
	
	<div id="bacnetConfTableButtonDiv">
		<table style="width: 100%;">
			<tr>
				<td style="width:10%;"><span><spring:message code="bacnet.label.enable"/></span><span><input type="checkbox" id="enableBacnet" name="enableBacnet" /></span></td>
				<td style="width:6%;"><input id="bacnetConfTableGridAdd" class="saveAction" type="button" value="Add Row" onclick="addNewRow()" disabled="disabled" style="display: none;" ></td>
				<td style="width:7%;"><input id="bacnetConfTableGridSaveRow" class="saveAction" type="button" value="Save Row" onclick="saveRow()" disabled="disabled" style="display: none;"></td>
				<td style="width:70%;"><span></span></td>
				<td style="width:5%;"><input id="bacnetConfTableGridSave" class="saveAction" type="submit" value="Submit" onclick="saveBACnetConfiguration()"></td>
			</tr>
		</table>
		<%-- <div style="float: left; padding-left: 5px;"><input id="bacnetConfTableGridAdd" class="saveAction" type="button" value="Add Row" onclick="addNewRow()"></div>
		<div style="float: left; padding-left: 5px;"><input id="bacnetConfTableGridSaveRow" class="saveAction" type="button" value="Save Row" onclick="saveRow()"></div>
		<div style="float: left; padding: 5px;"><span><spring:message code="bacnet.label.enable"/></span><span><input type="checkbox" id="enableBacnet" name="enableBacnet" /></span></div>
		<div style="float: left; padding-left: 900px;">
		<input id="bacnetConfTableGridSave" class="saveAction" type="submit" value="Submit" onclick="saveBACnetConfiguration()"></div> --%>
	</div>
	<br/>
	
	<!-- <div class="i1"></div> -->
	<%-- <span ><spring:message code="bacnet.header"/></span> --%>
	
	<!-- <div class="i1"></div> -->
	<!-- <div class="innerContainer">
		<div class="formContainer"> -->
			<!-- <div style="clear: both"><span id="error" class="load-save-errors" style="color:green!important"></span></div>
			<div style="clear: both"><span id="saveconfirm" class="save_confirmation" style="color:green!important" ></span></div> -->
			
			<table id="bacnetConfTable" class="entable" style="width: 100%; height: 100%;">
			<div id="pBacnetConfDiv"></div>
			</table>
		<!-- </div>
	</div> -->
</div>

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