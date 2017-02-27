<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/ballastservice/loadBallastList" var="getBallastList" scope="request" />
<spring:url value="/services/org/ballastservice/deleteballast/" var="deleteBallast" scope="request" />

<spring:url value="/settings/addballast.ems" var="newBallastUrl" scope="request" />
<spring:url value="/settings/editballast.ems" var="editBallastUrl" scope="request" />


<style>


</style>

<script type="text/javascript">

$(document).ready(function() {
    
    start(1, "desc");
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
			if(data.status == 1) {
				alert("Ballast cannot be deleted as it is associated with a fixture.");
			}else{
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
function start(pageNum, orderWay) {
		jQuery("#ballastTable").jqGrid({
			url: "${getBallastList}?ts="+new Date().getTime(),
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
			colNames: ["id", "Ballast Name","Bulb Type","No Of Bulbs","Ballast Factor","Bulb Wattage","Ballast Manufacturer","Baseline Load","Display Label","Action"],
		       colModel: [
		       { name:'id', index:'id', hidden: true},
		       { name: 'name', index: 'name',sorttype:'string',width:'15%' },
		       { name: "bulbType", index: 'bulbType', sortable:false,width:'6%' },
		       { name: "noOfBulbs", index: 'noOfBulbs', sortable:false,width:'6%' },
		       { name: "ballastFactor", index: 'ballastFactor',sortable:false,width:'6%'},
		       { name: "bulbWattage", index: 'bulbWattage',sortable:false,width:'6%'},
		       { name: "ballastManufacturer", index: 'ballastManufacturer',sortable:false,width:'8%'},
		       { name: 'baselineLoad',index:'baselineLoad', align:"center", sortable:false, width:"6%"},
		       { name: 'displayLabel',index:'displayLabel', align:"center", sortable:false, width:"25%"},
		       { name: "action", index: 'action',sortable:false,width:'12%', align: "left",formatter: viewActionFormatter}],
		       
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
										{edit:false,add:false,del:false,search:false}, 
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
	
	
//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	$("#ballastTable").setGridWidth($(window).width()-25);
}).trigger('resize');

</script>

<div id="newBallastDialog"></div>

<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="ballastDiv" style="padding: 5px 5px 5px 5px">
			<button id="newBallastButton">Add</button><br>	
			<div style="min-height:5px"></div>
    </div>
	<div style="padding: 0px 5px;">
		<table id="ballastTable"></table>
		<div id="ballastPagingDiv"></div>
	</div>
 </div>