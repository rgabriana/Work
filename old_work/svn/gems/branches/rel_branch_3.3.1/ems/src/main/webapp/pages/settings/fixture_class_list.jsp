<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/fixtureclassservice/loadFixtureClassList" var="getFixtureClassList" scope="request" />

<spring:url value="/settings/addfixtureclass.ems" var="addFixtureClassUrl" scope="request" />

<spring:url value="/settings/editfixtureclass.ems" var="editFixtureClassUrl" scope="request" />

<spring:url value="/services/org/fixtureclassservice/deleteFixtureClass/" var="deleteFixtureClassUrl" scope="request" />


<style>


</style>

<script type="text/javascript">

$(document).ready(function() {
	
	var page = "FIXTURE_CLASS_LIST";
	    
	startFixtureClassTable(1, "desc");
	$("#fixtureClassTable").setGridWidth($(window).width() - 25);
	
    $('#newFixtureClassButton').click(function() {
    	$("#newFixtureClassDialog").load("${addFixtureClassUrl}"+"?page="+page+"&ts="+new Date().getTime()).dialog({
            title : "Add Fixture Type",
            width :  Math.floor($('body').width() * .50),
            minHeight : 300,
            position: ['center',30],
            modal : true,
            close: function(event, ui) {
				$("#newFixtureClassDialog").html("");
			}           
        });
        return false;
    });
});

function onEditFixtureClass(rowId){
	
	var page = "FIXTURE_CLASS_LIST";
	
	$("#newFixtureClassDialog").load("${editFixtureClassUrl}?fixtureClassId="+rowId+"&page="+page+"&ts="+new Date().getTime()).dialog({
        title : "Edit Fixture Type",
        width :  Math.floor($('body').width() * .40),
        minHeight : 250,
        modal : true,
        close: function(event, ui) {
			$("#newFixtureClassDialog").html("");
		}
    });
}

function onDeleteFixtureClass(rowId)
{
	if(confirm("Are you sure you want to delete the Fixture Type?") == true)
	{
		$.ajax({
	 		type: 'POST',
	 		url: "${deleteFixtureClassUrl}"+rowId+"?ts="+new Date().getTime(),
	 		dataType : "json",
	 		success: function(data){
				if(data.status == 1) {
					alert("Cannot delete this Fixture Type.Please disassociate the fixtures associated with this Fixture Type");
				}else{
					reloadFixtureClassListFrame();
				}
			},
			error: function(){
				alert("Error occured.Fixture Type failed to delete");
			},
	 		contentType: "application/xml; charset=utf-8"
		});
	}
}

function reloadFixtureClassListFrame(){
	var ifr = parent.document.getElementById('fixtureclassFrame');
	window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src + "?ts="+new Date().getTime();
}

function ModifyFixtureClassGridDefaultStyles() {  
	   $('#' + "fixtureClassTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "fixtureClassTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "fixtureClassTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#fixtureClassTable").setGridWidth($(window).width()-25);	
}


//function for pagination
function startFixtureClassTable(pageNum, orderWay) {
		jQuery("#fixtureClassTable").jqGrid({
			url: "${getFixtureClassList}?ts="+new Date().getTime(),
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
			colNames: ["id", "Fixture Type Name","No of Ballasts","Voltage","Ballast","Bulb","Action"],
		       colModel: [
		       { name:'id', index:'id', hidden: true},
		       { name: 'name', index: 'name',sorttype:'string',width:'15%' },
		       { name: "noOfBallasts", index: 'noOfBallasts', sortable:false,width:'15%' },
		       { name: "voltage", index: 'voltage', sortable:false,width:'15%' },
		       { name: "ballast", index: 'ballast', sortable:false,width:'15%',formatter: ballastNameRenderer},
		       { name: "bulb", index: 'bulb', sortable:false,width:'15%',formatter: bulbNameRenderer},
		       { name: "action", index: 'action',sortable:false,width:'12%', align: "left",formatter: viewActionFormatter}],
		       
		   	jsonReader: { 
				root:"fixtureclasses", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    pager: '#fixtureClassPagingDiv',
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
		   			if (data.fixtureclasses != undefined) {
				   		if (data.fixtureclasses.length == undefined) {
				   			jQuery("#fixtureClassTable").jqGrid('addRowData', 0, data.fixtureclasses);
				   		}
				   	}
		   		}
		   		
		   		ModifyFixtureClassGridDefaultStyles();
		   		
		   	}

		});
		
		jQuery("#fixtureClassTable").jqGrid('navGrid',"#fixtureClassPagingDiv",
										{edit:false,add:false,del:false,search:false}, 
										{}, 
										{}, 
										{}, 
										{},
										{});
		
		forceFitFixtureClassTableWidth();
	}

	function viewActionFormatter(cellvalue, options, rowObject) {
		var rowId = rowObject.id;
		return "<button onclick=\"javascript: onEditFixtureClass('"+rowId+"');\">Edit</button>&nbsp<button onclick=\"javascript: onDeleteFixtureClass('"+rowId+"');\">Delete</button>";
	}
	
	function ballastNameRenderer(cellvalue, options, rowObject){
		return rowObject.ballast.name;
	}
	
	function bulbNameRenderer(cellvalue, options, rowObject){
		return rowObject.bulb.name;
	}
	
	function forceFitFixtureClassTableWidth(){
		var jgrid = jQuery("#fixtureClassTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#fixtureClassDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#fixtureClassPagingDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 30);
		
		$("#fixtureClassTable").setGridWidth($(window).width() - 25);
	}
	
	
//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	$("#fixtureClassTable").setGridWidth($(window).width()-25);
}).trigger('resize');

</script>

<div id="newFixtureClassDialog"></div>

<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="fixtureClassDiv" style="padding: 5px 5px 5px 5px">
			<button id="newFixtureClassButton">Add</button><br>	
			<div style="min-height:5px"></div>
    </div>
	<div style="padding: 0px 5px;">
		<table id="fixtureClassTable"></table>
		<div id="fixtureClassPagingDiv"></div>
	</div>
 </div>