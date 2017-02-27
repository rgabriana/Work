<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<spring:url value="/services/org/plugloadProfile" var="changePlugloadProfileEmployeeUrl" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Assign Profile</title>

<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />

<style>
	/*Override JQuery Dialog modal background css */
	div.aggf-message-text {font-weight:bold; float: left; padding-top: 5px;}
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
</style>

<script type="text/javascript">

var PAGE = "${page}";
$(document).ready(function() {
	/* start('1#######END', 1); */
	print_plugload_employee_data();
}); 

var idlist = "";

//XML to send to the server for selected plugloads
var fxXML = "";

//This Json array is sent to the server for updating the db
var jsonParent = [];

//The array which gets updated when user clicks on the combo boxes to update the groupid's
var plugloadToUpdate = new Array();

function print_plugload_employee_data()
{
	
	var jsonTop = [];
	var jsonTopTop = [];
	var jsonObj = [];
	var idarray = new Array();
	fxXML = "";
	fxXML = "<plugloads>";
	for(var i=0; i<SELECTED_PLUGLOADS_TO_UPDATE_PROFILE.length; i++){
		var plugloadJson = SELECTED_PLUGLOADS_TO_UPDATE_PROFILE[i];
		//var postData = getplugloadXML(plugloadJson.id);
		idarray[i] = plugloadJson.id;
		
		fxXML= fxXML+ "<plugload><id>"+plugloadJson.id+"</id></plugload>";
		
		jsonObj.push({id: plugloadJson.id});
		jsonTop.push({plugload:jsonObj});
		}	
	fxXML = fxXML + "</plugloads>";
	idlist = idarray.join(',');
	startPlugloadEmployee('1#######END', 1, "status", "desc",idlist);
}

function startPlugloadEmployee(inputdata, pageNum, orderBy, orderWay,selectedplugloads) {	
	var successdata="";			
			jQuery("#assignPlugloadTable").jqGrid({     
				url: '<spring:url value="/services/org/plugloadProfile/assignlist/${pid}"/>',
				userData: "userdata",				
				mtype: "POST",
				postData: {"selectedplugloads":selectedplugloads},
				datatype: "json",			
				autowidth: true,
				autoencode: true,	
				scrollOffset: 0,
				formatter: {
					 integer: {thousandsSeparator: ",", defaultValue: '0'},
				     number: {decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, defaultValue: '0.00'}
				},
				/* userData : "userdata", */
				 forceFit: true,	 
			  	colNames:["S No.","Plugload Name", "Template Name"  ,"Profile Name"   ],
			   	colModel:[
					{name:'sno', index:'sno', hidden:false, align:"left",sortable:false,width:"10%",search:false},
					{name:'plugloadName', index:'plugloadName', align:"left", sortable:false,width:"30%", search:false},
			   	    {name:'templateName', index:'templateName',align:"left", sortable:false,width:"30%", searchoptions:{sopt:['cn']}},
			   		{name:'profileList',  index:'profileList',align:"left",sortable:false,width:"40%", edittype:"select",formatter:dropdownempplugloadrenderer}
			   	   ],			   
			    jsonReader: { 
			        root:"assignPlugloads", 
			        page:"page", 
			        total:"total", 
			        records:"records",
			        repeatitems:false,			        
			        id : "sno"
			   	},
			    multiselect: false,
	 		   	pager: '#assignplugloadPagingDiv',
			   	page: pageNum,	
				sortorder: orderWay,
			   	sortname: orderBy,
			    hidegrid: false,
			    viewrecords: true,	
				onSortCol: function(index, iCol, sortOrder) {				
					$('#orderWay').attr('value', sortOrder);
			   		$('#orderBy').attr('value', index);
					ModifyEmployeePlugloadGridDefaultStyles();
					forceFitPlugloadTableHeight();
			   	},
			   	onPaging: function (pgButton)
			   	{
			   		ModifyEmployeePlugloadGridDefaultStyles();
			   		forceFitPlugloadTableHeight();
			   	},
				loadComplete: function(data) {
			   		if (data.assignPlugloads != undefined) {
				   		if (data.assignPlugloads.length == undefined) {
				   			// Hack: Currently, JSON serialization via jersey treats single item differently
				   			jQuery("#assignPlugloadTable").jqGrid('addRowData', 0, data.assignPlugloads);
				   		}
				   	}
	 
			   	ModifyEmployeePlugloadGridDefaultStyles();
			   	forceFitPlugloadTableHeight();
			   		
			   	}
			    });
			
	   		jQuery("#assignPlugloadTable").jqGrid('navGrid',"#assignplugloadPagingDiv",
					{edit:false,add:false,del:false}, 
					{}, 
					{}, 
					{}, 
					{closeAfterSearch:true, beforeShowSearch: function(form){
											    form.keydown(function(e) {
											        if (e.which == 13) {
											           // $("#fbox_plugloadTable_search").focus();
											        }
											    });
											}
					}
					);	
	   		
}
function resizeNewPlugloadsGrid(){
	//resize new plugload grid
	var gridContainerEL = $(this).width();	
	jQuery("#assignPlugloadTable").jqGrid("setGridWidth", gridContainerEL - 378);
	
}

function exitPlugloadEmployeeWindowDialog(){
	$("#assignPlugloadProfileToPlugloadsDailog").dialog("close");
}

function forceFitPlugloadTableHeight(){
	
	var jgrid = jQuery("#assignPlugloadTable");
	var containerHeight =$(this).height();
	var otherElementHeight = $("#boxButton").height();
	
	var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
	var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
	var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
	resizeNewPlugloadsGrid();
	jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight -gridBodyHeight- gridHeaderFooterHeight) * .80));
}


function createemployeePlugloadarray(value)
{
	var flag = "false";
	
	for (var i = 0; i < plugloadToUpdate.length; i++) {
		var parts = value.split(":");
		var result = parts[parts.length - 1];
		
		parts = plugloadToUpdate[i].split(":");
		var arrresult = parts[parts.length - 1]; 
		if(result == arrresult)
		{
			flag = "true";
			plugloadToUpdate.splice(i,1);
			break;
		} 
		else
		{
			flag = "false";
		}	   
		if(flag=="true") break;
	}
	plugloadToUpdate.push(value);
	var jsonTopParent = [];	
	var jsonObj = [];
	//clear the json array 
	jsonParent = [];
	for (var i = 0; i < plugloadToUpdate.length; i++) {
		var parts = plugloadToUpdate[i].split(":");
		var result = parts[parts.length - 1];
	        jsonObj.push({id: parts[1], groupId: parts[0]}); 
        
    }
	jsonParent.push({plugload:jsonObj});
	var temp = "<plugloads><plugload><id>28</id></plugload><plugload><id>26</id></plugload></plugloads>";
	jsonTopParent.push({plugloads:jsonParent});

}

function dropdownempplugloadrenderer(cellvalue, options, rowObject){
	var source = "<select id=" + "assign-profile-combo"+ " onchange=createemployeePlugloadarray(this.options[this.selectedIndex].value)>";	
	var len;	
	if(rowObject.profileList.length>=2)
		{		
		len = rowObject.profileList.length;
		for(var i = 0;i < len; ++i)
			{
			var displayName;
			if(rowObject.profileList[i].name==rowObject.templateName)
				{
				displayName = rowObject.profileList[i].name + "_Default";
				}
			else
				{
				displayName = rowObject.profileList[i].name;
				}
			if(rowObject.currentGroupId==rowObject.profileList[i].id)
			source = source + "<option value=" + rowObject.profileList[i].id +":"+rowObject.plugloadId+ " selected='selected'"+  ">" + displayName + "</option>";
			else
			source = source + "<option value=" + rowObject.profileList[i].id +":"+rowObject.plugloadId + ">" + displayName + "</option>";
			}		
		}
	else
		{		
		source = source + "<option value=" + rowObject.profileList.id + ">" + rowObject.profileList.name + "</option>";
		
		}
	source = source +"</select>";	
	return source;
}

//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	//$("#assignPlugloadTable").setGridWidth($(window).width()-20);
}).trigger('resize');

function ModifyEmployeePlugloadGridDefaultStyles() {  
	   $('#' + "assignPlugloadTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "assignPlugloadTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "assignPlugloadTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   //$('#' + "plugloadTable").removeClass("ui-jqgrid-htable");
}

function setEmpPlugloadAssignMessage(msg, color){
	$("#plugload-message-div").css("color", color);
	$("#plugload-message-div").html(msg);
}

function apply_plugload_employee_clicked()
{
	
	$.ajax({
		type: 'POST',
		url: "${changePlugloadProfileEmployeeUrl}/changeassignlist",
		data: JSON.stringify(jsonParent),
		async: false,
		success: function(data){
			fxXML = "";
			plugloadToUpdate = new Array();
			print_plugload_employee_data();
			//alert("Records Updated");	
			setEmpPlugloadAssignMessage(data.msg,"green");
		},
		complete: function(){
		},
		dataType:"json",
		contentType: "application/json; charset=utf-8"
	});
 } 
</script>
</head>
<body id="apf-main-box">

<div id = "boxButton" align="right" >
 <table id="apply-wrapper-table"  width=100% height=100%>	
	<tr>
	<td align="left">
	<div id="plugload-message-div" class="aggf-message-text">&nbsp;</div>
	</td>		
		<td height=auto align="right">		
		<div class="buttons-wrapper">
			<button id="apf-apply-btn" onclick=apply_plugload_employee_clicked()>Apply</button>
			<button id="apf-cancel-btn" onclick=exitPlugloadEmployeeWindowDialog()>Cancel</button> 
			&nbsp;
		</div>			
		</td>
	</tr>
</table> 
</div>
<table id="assignPlugloadTable"></table>
<div id="assignplugloadPagingDiv"></div>
 
</body>
</html>
