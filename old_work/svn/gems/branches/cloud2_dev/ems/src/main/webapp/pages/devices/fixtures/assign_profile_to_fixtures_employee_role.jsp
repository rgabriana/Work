<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<spring:url value="/services/org/profile" var="changeFixtureProfileUrl" scope="request" />

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
	print_data();
}); 

var idlist = "";

//XML to send to the server for selected fixtures
var fxXML = "";

//This Json array is sent to the server for updating the db
var jsonParent = [];

//The array which gets updated when user clicks on the combo boxes to update the groupid's
var fixtureToUpdate = new Array();

function print_data()
{
	
	var jsonTop = [];
	var jsonTopTop = [];
	var jsonObj = [];
	var idarray = new Array();
	fxXML = "";
	fxXML = "<fixtures>";
	for(var i=0; i<SELECTED_FIXTURES_TO_UPDATE_PROFILE.length; i++){
		var fixtureJson = SELECTED_FIXTURES_TO_UPDATE_PROFILE[i];
		//var postData = getFixtureXML(fixtureJson.id);
		idarray[i] = fixtureJson.id;
		
		fxXML= fxXML+ "<fixture><id>"+fixtureJson.id+"</id></fixture>";
		
		jsonObj.push({id: fixtureJson.id});
		jsonTop.push({fixture:jsonObj});
		}	
	fxXML = fxXML + "</fixtures>";
	idlist = idarray.join(',');
	start('1#######END', 1, "status", "desc",idlist);
}

function start(inputdata, pageNum, orderBy, orderWay,selectedfixtures) {	
	var successdata="";			
			jQuery("#assignfixtureTable").jqGrid({     
				url: '<spring:url value="/services/org/profile/assignlist/${pid}"/>',
				userData: "userdata",				
				mtype: "POST",
				postData: {"selectedfixtures":selectedfixtures},
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
			  	colNames:["S No.","Fixture Name", "Template Name"  ,"Profile Name"   ],
			   	colModel:[
					{name:'sno', index:'sno', hidden:false, align:"left",sortable:false,width:"10%",search:false},
					{name:'fixtureName', index:'fixtureName', align:"left", sortable:false,width:"30%", search:false},
			   	    {name:'templateName', index:'templateName',align:"left", sortable:false,width:"30%", searchoptions:{sopt:['cn']}},
			   		{name:'profileList',  index:'profileList',align:"left",sortable:false,width:"40%", edittype:"select",formatter:dropdownrenderer/* ,editoptions:{value:"ID:CHETAN"} */}
			   	   
			   		],			   
			    jsonReader: { 
			        root:"assignFixtures", 
			        page:"page", 
			        total:"total", 
			        records:"records",
			        repeatitems:false,			        
			        id : "sno"
			   	},
			    multiselect: false,
	 		   	pager: '#assignfixturePagingDiv',
			   	page: pageNum,	
				sortorder: orderWay,
			   	sortname: orderBy,
			    hidegrid: false,
			    viewrecords: true,	
				onSortCol: function(index, iCol, sortOrder) {				
					$('#orderWay').attr('value', sortOrder);
			   		$('#orderBy').attr('value', index);
					ModifyGridDefaultStyles();
					forceFitFixtureTableHeight();
			   	},
			   	onPaging: function (pgButton)
			   	{
			   		ModifyGridDefaultStyles();
			   		forceFitFixtureTableHeight();
			   	},
				loadComplete: function(data) {
			   		if (data.assignFixtures != undefined) {
				   		if (data.assignFixtures.length == undefined) {
				   			// Hack: Currently, JSON serialization via jersey treats single item differently
				   			jQuery("#assignfixtureTable").jqGrid('addRowData', 0, data.assignFixtures);
				   		}
				   	}
	 
			   	ModifyGridDefaultStyles();
			   	forceFitFixtureTableHeight();
			   		
			   	}
			    });
			
	   		jQuery("#assignfixtureTable").jqGrid('navGrid',"#assignfixturePagingDiv",
					{edit:false,add:false,del:false}, 
					{}, 
					{}, 
					{}, 
					{closeAfterSearch:true, beforeShowSearch: function(form){
											    form.keydown(function(e) {
											        if (e.which == 13) {
											           // $("#fbox_fixtureTable_search").focus();
											        }
											    });
											}
					}
					);	
	   		
}
function resizeNewFixturesGrid(){
	//resize new fixture grid
	var gridContainerEL = $(this).width();	
	jQuery("#assignfixtureTable").jqGrid("setGridWidth", gridContainerEL - 378);
	
}

function exitWindowDialog(){
	$("#assignProfileToFixturesDailog").dialog("close");
}

function forceFitFixtureTableHeight(){
	
	var jgrid = jQuery("#assignfixtureTable");
	var containerHeight =$(this).height();
	var otherElementHeight = $("#boxButton").height();
	
	var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
	var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
	var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
	resizeNewFixturesGrid();
	jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight -gridBodyHeight- gridHeaderFooterHeight) * .80));
}


function createarray(value)
{
	var flag = "false";
	
	for (var i = 0; i < fixtureToUpdate.length; i++) {
		var parts = value.split(":");
		var result = parts[parts.length - 1];
		
		parts = fixtureToUpdate[i].split(":");
		var arrresult = parts[parts.length - 1]; 
		if(result == arrresult)
		{
			flag = "true";
			fixtureToUpdate.splice(i,1);
			break;
		} 
		else
		{
			flag = "false";
		}	   
		if(flag=="true") break;
	}
	fixtureToUpdate.push(value);
	var jsonTopParent = [];	
	var jsonObj = [];
	//clear the json array 
	jsonParent = [];
	for (var i = 0; i < fixtureToUpdate.length; i++) {
		var parts = fixtureToUpdate[i].split(":");
		var result = parts[parts.length - 1];
	        jsonObj.push({id: parts[1], groupid: parts[0]}); 
        
    }
	jsonParent.push({fixture:jsonObj});
	var temp = "<fixtures><fixture><id>28</id></fixture><fixture><id>26</id></fixture></fixtures>";
	jsonTopParent.push({fixtures:jsonParent});

}

function dropdownrenderer(cellvalue, options, rowObject){
	var source = "<select id=" + "assign-profile-combo"+ " onchange=createarray(this.options[this.selectedIndex].value)>";	
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
			source = source + "<option value=" + rowObject.profileList[i].id +":"+rowObject.fixtureId+ " selected='selected'"+  ">" + displayName + "</option>";
			else
			source = source + "<option value=" + rowObject.profileList[i].id +":"+rowObject.fixtureId + ">" + displayName + "</option>";
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
	//$("#assignfixtureTable").setGridWidth($(window).width()-20);
}).trigger('resize');

function ModifyGridDefaultStyles() {  
	   $('#' + "assignfixtureTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "assignfixtureTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "assignfixtureTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   //$('#' + "fixtureTable").removeClass("ui-jqgrid-htable");
}

function setAssignMessage(msg, color){
	$("#fixture-message-div").css("color", color);
	$("#fixture-message-div").html(msg);
}

function apply_button_clicked()
{
	
	$.ajax({
		type: 'POST',
		url: "${changeFixtureProfileUrl}/changeassignlist",
		data: JSON.stringify(jsonParent),
		async: false,
		success: function(data){
			fxXML = "";
			fixtureToUpdate = new Array();
			print_data();
			//alert("Records Updated");	
			setAssignMessage(data.msg,"green");
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
	<div id="fixture-message-div" class="aggf-message-text">&nbsp;</div>
	</td>		
		<td height=auto align="right">		
		<div class="buttons-wrapper">
			<button id="apf-apply-btn" onclick=apply_button_clicked()>Apply</button>
			<button id="apf-cancel-btn" onclick=exitWindowDialog()>Cancel</button> 
			&nbsp;
		</div>			
		</td>
	</tr>
</table> 
</div>
<table id="assignfixtureTable"></table>
<div id="assignfixturePagingDiv"></div>
 
</body>
</html>
