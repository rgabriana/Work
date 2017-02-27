<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/ballastservice/loadBallastList" var="getBallastList" scope="request" />
<spring:url value="/services/org/ballastservice/deleteballast/" var="deleteBallast" scope="request" />

<spring:url value="/settings/addballast.ems" var="newBallastUrl" scope="request" />
<spring:url value="/settings/editballast.ems" var="editBallastUrl" scope="request" />

<spring:url value="/settings/importvoltpowercurve.ems" var="importVoltPowerCurveUrl" scope="request" />
<spring:url value="/settings/showpowermaps.ems" var="ballastpowermapUrl" scope="request" />

<style>


</style>

<script type="text/javascript">
var url="";
var ballasttogglemode = false;         //Determines the mode

$(document).ready(function() {
	ballasttogglemode = false;    
    url =  '<spring:url value="/services/org/ballastservice/loadBallastList"/>';
    
    start(1, "desc",url);
	$("#ballastTable").setGridWidth($(window).width() - 25);
	
    $('#newBallastButton').click(function() {
    	$("#newBallastDialog").load("${newBallastUrl}"+"?ts="+new Date().getTime()).dialog({
            title : "New Ballast",
            width :  Math.floor($('body').width() * .30),
            minHeight : 250,
            modal : true
        });
        return false;
    });
});




function onEdit(rowId){
	$("#newBallastDialog").load("${editBallastUrl}?ballastId="+rowId+"&ts="+new Date().getTime()).dialog({
        title : "Edit Ballast",
        width :  Math.floor($('body').width() * .40),
        minHeight : 250,
        modal : true
    });
}



function onDelete(rowId)
{
	if(confirm("Are you sure you want to delete the Ballast?") == true)
	{
		$.ajax({
	 		type: 'POST',
	 		url: "${deleteBallast}"+rowId+"?ts="+new Date().getTime(),
	 		dataType : "json",
	 		success: function(data){
			if(data.status == 3) {
				alert("Default ballast cannot be deleted.");
			}
			if(data.status == 1) {
				alert("Ballast cannot be deleted as it is associated with a fixture.");
			}
			if(data.status == 2) {
				alert("Ballast cannot be deleted as it is associated with a Fixture Class.");
			}
			else{
				reloadBallastListFrame();
			}
			
			},
			error: function(){
				alert("Error occured.Balast failed to delete");
			},
	 		contentType: "application/xml; charset=utf-8"
		});
	}
}

function onImport(rowId)
{
	$("#uploadVoltPowerCurveDialog").load("${importVoltPowerCurveUrl}?ballastId="+rowId+"&ts="+new Date().getTime()).dialog({
        title : "Upload Ballast Volt Power Curve",
        width :  450,
        minHeight : 160,
        modal : true
    });
}

function onViewClick(rowId)
{
	$("#ballastpowermapDialog").load("${ballastpowermapUrl}?ballastId="+rowId+"&ts="+new Date().getTime()).dialog({
        title : "Power Usage",
        width :  450,
        minHeight : 360,
        modal : true,
        close: function(event, ui) {
        	reloadBallastListFrame();
		}
    });
}

function reloadBallastListFrame(){
	var ifr = parent.document.getElementById('ballastFrame');
	window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src + "?ts="+new Date().getTime();
}

function ModifyBallastGridDefaultStyles() {  
	   $('#' + "ballastTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "ballastTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "ballastTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#ballastTable").setGridWidth($(window).width()-25);	
}


//function for pagination
function start(pageNum, orderWay,url) {
		jQuery("#ballastTable").jqGrid({
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
			colNames: ["Id", "Display Label","Lamp Type","No Of Lamps","Ballast Factor","Lamp Wattage","Ballast Manufacturer","Baseline Load","Ballast Name","Action","Volt Power Map","Input Voltage"],
		       colModel: [
		       { name:'id', index:'id', sorttype:'string',width:'3%', sortable:true,searchoptions:{sopt:['eq']}},		       
		       { name: 'displayLabel',index:'displayLabel', align:"center", sorttype:'string', width:"25%", sortable:true,searchoptions:{sopt:['cn']}},
		       { name: "bulbType", index: 'bulbType',search:false, sortable:false,width:'6%' },
		       { name: "noOfBulbs", index: 'noOfBulbs',search:false, sortable:false,width:'6%' },
		       { name: "ballastFactor", index: 'ballastFactor',search:false,sortable:false,width:'6%'},
		       { name: "bulbWattage", index: 'bulbWattage',search:false,sortable:false,width:'6%'},
		       { name: "ballastManufacturer", index: 'ballastManufacturer',sortable:false,width:'8%',searchoptions:{sopt:['cn']}},
		       { name: 'baselineLoad',index:'baselineLoad', align:"center",search:false, sortable:false, width:"6%"},
		       { name: 'name', index: 'name',sorttype:'string',width:'15%',searchoptions:{sopt:['cn']}},
		       { name: "action", index: 'action',sortable:false,width:'12%', align: "left",formatter: viewActionFormatter,search:false},
		       { name: "importExport", index: 'importExport',sortable:false,width:'12%', align: "left",formatter: viewImportExportFormatter,search:false},
		       {name:'inputvolt', index:'inputvolt', hidden:true,searchoptions: {searchhidden: true,sopt:['eq']}}],
		       
		   	jsonReader: { 
				root:"ballasts", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    pager: '#ballastPagingDiv',
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
		   		var csvballastflag="${successflag}";
		   		var invalidfileentries = "${invalidfileentries}";
		   		var entryLabel = "entry";
		   		if(csvballastflag == 'false')
		   		{	
		   			displayBallastListLabelMessage("Failed to import CSV file.","red");		   			
		   		}
		   		else if(csvballastflag == 'true')
		   		{
		   			if(invalidfileentries > 0)
		   			{
		   				if(invalidfileentries > 1)
			   			{
			   				entryLabel = "entries";
			   			}		   				
		   			displayBallastListLabelMessage("CSV file imported successfully ,"+invalidfileentries+ " invalid entries found in file.","green");
		   			}
		   			else
		   			{
		   			displayBallastListLabelMessage("CSV file imported successfully.","green");
		   			}
		   		}
		   		if (data != null){
		   			if (data.ballasts != undefined) {
				   		if (data.ballasts.length == undefined) {
				   			jQuery("#ballastTable").jqGrid('addRowData', 0, data.ballasts);
				   		}
				   	}
		   		}
		   		
		   		ModifyBallastGridDefaultStyles();
		   		
		   	}

		});
		
		jQuery("#ballastTable").jqGrid('navGrid',"#ballastPagingDiv",
										{edit:false,add:false,del:false}, 
										{}, 
										{}, 
										{}, 
										{},
										{});
		
		forceFitBallastTableWidth();
	}	

	function viewActionFormatter(cellvalue, options, rowObject) {
		var rowId = rowObject.id;
		return "<button onclick=\"javascript: onEdit('"+rowId+"');\">Edit</button>&nbsp<button onclick=\"javascript: onDelete('"+rowId+"');\">Delete</button>";
	}
	function viewImportExportFormatter(cellvalue, options, rowObject){
		var rowId = rowObject.id;
		var source = "";
		if (rowObject.isPowerMap == 'true') {
			source = "<button onclick=\"javascript: onImport('"+rowId+"');\">Import</button>&nbsp;&nbsp;<button onclick=\"javascript: onViewClick('"+rowId+"');\">View</button>";
		} else {
			source = "<button onclick=\"javascript: onImport('"+rowId+"');\">Import</button>";
		}
		return source;
	}
	function forceFitBallastTableWidth(){
		var jgrid = jQuery("#ballastTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#ballastDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#ballastPagingDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 30);
		
		$("#ballastTable").setGridWidth($(window).width() - 25);
	}
	
	function displayBallastListLabelMessage(Message, Color) {
		$("#voltpowermapmessage").html(Message);
		$("#voltpowermapmessage").css("color", Color);
	}

	function clearBallastListLabelMessage(Message, Color) {
		displayBallastListLabelMessage("", "black");
	}
	
	function applyBallastToggleCall()
	{
		if(ballasttogglemode==false)
		{
			ballasttogglemode=true;
			
			var postdata = $("#ballastTable").jqGrid('getGridParam','postData');
			postdata._search = false;
			postdata.searchField = "";
			postdata.searchOper = "";
			postdata.searchString = "";
			
			url =  '<spring:url value="/services/org/ballastservice/loadBallastListByUsage"/>';
					
			jQuery("#ballastTable").jqGrid('setGridParam',{url:url,page:1}).trigger("reloadGrid");
			
			return;
		}
		if(ballasttogglemode==true)
		{
			var postdata = $("#ballastTable").jqGrid('getGridParam','postData');
			postdata._search = false;
			postdata.searchField = "";
			postdata.searchOper = "";
			postdata.searchString = "";
			
			url =  '<spring:url value="/services/org/ballastservice/loadBallastList"/>';
		    jQuery("#ballastTable").jqGrid('setGridParam',{url:url,page:1}).trigger("reloadGrid");
		    
			ballasttogglemode=false;
			
			return;
		}
	}

//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	$("#ballastTable").setGridWidth($(window).width()-25);
}).trigger('resize');

</script>

<div id="newBallastDialog"></div>
<div id="uploadVoltPowerCurveDialog"></div>
<div id="ballastpowermapDialog"></div>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="ballastDiv" style="padding: 5px 5px 5px 5px">
			<button id="newBallastButton">Add</button><div style="display: inline;padding-left:100px;"><input type="checkbox" name="mcheckedballast" onchange="applyBallastToggleCall()">&nbsp;<b>Show Used Ballasts</b></input></div>
			<div style="display: inline;padding-left:300px;"><span id="voltpowermapmessage" style="font-weight:bold"></span></div>			
			<div style="min-height:5px"></div>
    </div>
	<div style="padding: 0px 5px;">
		<table id="ballastTable"></table>
		<div id="ballastPagingDiv"></div>
	</div>
 </div>