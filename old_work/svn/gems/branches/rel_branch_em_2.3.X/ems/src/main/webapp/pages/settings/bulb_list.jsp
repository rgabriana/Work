<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/bulbservice/loadBulbList" var="getBulbList" scope="request" />
<spring:url value="/services/org/bulbservice/deletebulb/" var="deleteBulb" scope="request" />

<spring:url value="/settings/addbulb.ems" var="newBulbUrl" scope="request" />
<spring:url value="/settings/editbulb.ems" var="editBulbUrl" scope="request" />
<style>


</style>
<script type="text/javascript">

function reloadBulbListFrame(){ 
	var ifr = parent.document.getElementById('bulbFrame');
	window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	ifr.src = ifr.src + "?ts="+new Date().getTime();
	}

//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	$("#bulbTable").setGridWidth($(window).width()-25);
}).trigger('resize');

$(document).ready(function() {
    
    startBulbs(1, "desc");
	$("#bulbTable").setGridWidth($(window).width() - 25);
	
	$('#newBulbButton').click(function() {
    	$("#newBulbDialog").load("${newBulbUrl}"+"?ts="+new Date().getTime()).dialog({
            title : "New Bulb",
            width :  Math.floor($('body').width() * .30),
            minHeight : 280,            
            modal : true
        });
        return false;
    });
	
    
});

function startBulbs(pageNum, orderWay) {
		jQuery("#bulbTable").jqGrid({
			url: "${getBulbList}?ts="+new Date().getTime(),
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
			colNames: ["id", "Bulb","Manufacturer","Type","Initial Lumens","Design Lumens","Energy","Life Ins Start","Life Prog Start","Diameter","Length","Cri","Color Temperature","Action"],
		       colModel: [
		       { name:'id', index:'id', hidden: true},
		       { name: 'name', index: 'name',sorttype:'string',width:'15%' },
		       { name: "manufacturer", index: 'manufacturer', sortable:false,width:'6%' },
		       { name: "type", index: 'type', sortable:false,width:'6%' },
		       { name: "initiallumens", index: 'initiallumens',sortable:false,width:'6%'},
		       { name: "designlumens", index: 'designlumens',sortable:false,width:'6%'},
		       { name: "energy", index: 'energy',sortable:false,width:'5%'},
		       { name: 'lifeinsstart',index:'lifeinsstart', align:"center", sortable:false, width:"6%"},
		       { name: 'lifeprogstart',index:'lifeprogstart', align:"center", sortable:false, width:"6%"},
		       { name: 'diameter',index:'diameter', align:"center", sortable:false, width:"6%"},
		       { name: 'length',index:'length', align:"center", sortable:false, width:"6%"},
		       { name: 'cri',index:'cri', align:"center", sortable:false, width:"3%"},
		       { name: 'colortemp',index:'colortemp', align:"center", sortable:false, width:"6%"},
		       { name: "action", index: 'action',sortable:false,width:'9%', align: "left",formatter: viewActionFormatter}],
		       
		   	jsonReader: { 
				root:"bulbs", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    pager: '#bulbPagingDiv',
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
		   			if (data.bulbs != undefined) {
				   		if (data.bulbs.length == undefined) {
				   			jQuery("#bulbTable").jqGrid('addRowData', 0, data.bulbs);
				   		}
				   	}
		   		}
		   		
		   		ModifyBulbGridDefaultStyles();
		   		
		   	}

		});
		
		jQuery("#bulbTable").jqGrid('navGrid',"#bulbPagingDiv",
										{edit:false,add:false,del:false,search:false}, 
										{}, 
										{}, 
										{}, 
										{},
										{});
		
		forceFitBulbTableWidth();
	}
	
	function viewActionFormatter(cellvalue, options, rowObject) {
		var rowId = rowObject.id;
		return "<button onclick=\"javascript: onEditBulb('"+rowId+"');\">Edit</button>&nbsp<button onclick=\"javascript: onDeleteBulb('"+rowId+"');\">Delete</button>";
	}	
	
	function onDeleteBulb(rowId)
{
	if(confirm("Are you sure you want to delete the Bulb?") == true)
	{
		$.ajax({
	 		type: 'POST',
	 		url: "${deleteBulb}"+rowId+"?ts="+new Date().getTime(),
	 		dataType : "json",
	 		success: function(data){
			if(data.status == 1) {
				alert("Bulb cannot be deleted as it is associated with a fixture.");
			}
			if(data.status == 2) {
				alert("Bulb cannot be deleted as it is associated with a Fixture Class.");
			}
			else{
				reloadBulbListFrame();
			}
			
			},
			error: function(){
				alert("Error occured.Bulb failed to delete");
			},
	 		contentType: "application/xml; charset=utf-8"
		});
	}
}	
	
	function onEditBulb(rowId){
	$("#newBulbDialog").load("${editBulbUrl}?bulbId="+rowId+"&ts="+new Date().getTime()).dialog({
        title : "Edit Bulb",
        width :  Math.floor($('body').width() * .40),
        minHeight : 250,
        modal : true
    });
	}
	
	function ModifyBulbGridDefaultStyles() {  
	   $('#' + "bulbTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "bulbTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "bulbTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#bulbTable").setGridWidth($(window).width()-25);	
	}
	
	function forceFitBulbTableWidth(){
		var jgrid = jQuery("#bulbTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#bulbDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#bulbPagingDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 30);
		
		$("#bulbTable").setGridWidth($(window).width() - 25);
	}


</script>

<div id="newBulbDialog"></div>

<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="bulbDiv" style="padding: 5px 5px 5px 5px">
			<button id="newBulbButton">Add</button><br>	
			<div style="min-height:5px"></div>
    </div>
	<div style="padding: 0px 5px;">
		<table id="bulbTable"></table>
		<div id="bulbPagingDiv"></div>
	</div>
</div>