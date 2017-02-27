<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<div id="plugloadDialog"></div>

<div id="plugload-list-topPanel" style="background:#fff;">
	<div id="fixture-list-topLeftPanel" style="float:right;width: 400px;text-align: right;">
		<c:if test="${page == 'floor'}">
			<button id="discoverFixtureButton" onclick="javascript: parent.parent.showPlugloadDiscoveryWindow();">Discover</button>
			<button id="bulkFixtureCommissionButton" onclick="javascript: parent.parent.showPlugloadCommissioningIdentifyWindow(true, 0, 0);">Bulk Commission</button>
		</c:if>
	</div>
	
	<div id="fixture-list-topRightPanel" style="text-align: left;">
		
		<button id="deleteFixtureButton" onclick="javascript: beforeDeleteMultiplugloads();">Delete</button>
				
	</div>
	
	<div style="height:10px;"></div>
</div>


<script type="text/javascript">

var PAGE = "${page}";
	$(document).ready(function() {
		
		start('1#######END', 1, "plugloadName", "desc");
	});
	
	function start(inputdata, pageNum, orderBy, orderWay) {
		jQuery("#plugloadTable").jqGrid({
			url: '<spring:url value="/services/org/plugload/list/alternate/filter/${page}/${pid}"/>',
			userData: "userdata",
			mtype: "POST",
			postData: {"userData": inputdata},
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
		   	colNames:["id", "Name","Version", "Gateway","Current Profile", "Upgrade Status","Action"],
		   	colModel:[
				{name:'id', index:'id', hidden:true},		   			   		
				{name:'plugloadName', index:'plugloadName', sortable:true, width:"10%", formatter: plugloadNameRenderer, searchoptions:{sopt:['cn']}},
				{name:'version', index:'version', sortable:true, width:"10%", formatter: currVersionRenderer, searchoptions:{sopt:['cn']}},		   		
				{name:'gateway', index:'gateway', sortable:true, width:"10%", searchoptions:{sopt:['cn']}, formatter: gatewayNameRenderer},
				{name:'currentProfile', index:'currentProfile', sortable:true, width:"12%", searchoptions:{sopt:['cn']},formatter: profileNameRenderer},
		   		{name:'upgradeStatus', index:'upgradeStatus', sortable:true, width:"14%", searchoptions:{sopt:['cn']}}		   	,
		   		{name:'state', index:'state', align:"right", sortable:true , width:"12%", searchoptions:{sopt:['cn']}, formatter: actionImageRenderer}
		   	],
		   	jsonReader: { 
		        root:"plugload", 
		        page:"page", 
		        total:"total", 
		        records:"records",
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    multiselect: true,
		    multiboxonly: true,			
			pager: '#plugloadPagingDiv',
		   	page: pageNum,
			sortorder: orderWay,
		   	sortname: orderBy,
		    hidegrid: false,
		    viewrecords: true,
		   	loadui: "block",
		   	toolbar: [false,"top"],		   	
			onSortCol: function(index, iCol, sortOrder) {
		   		$('#orderWay').attr('value', sortOrder);
		   		$('#orderBy').attr('value', index);
		   	},
		   	loadComplete: function(data) {
		   		if (data.plugload != undefined) {
			   		if (data.plugload.length == undefined) {
			   			// Hack: Currently, JSON serialization via jersey treats single item differently
			   			jQuery("#plugloadTable").jqGrid('addRowData', 0, data.plugload);
			   		}
			   	}
		   		
		   		ModifyGridDefaultStyles();
		   		
		   	}

		});
		
		jQuery("#plugloadTable").jqGrid('navGrid',"#plugloadPagingDiv",{edit:false,add:false,del:false});
		forceFitPlugloadTableHeight();
		
	}
	
	function actionImageRenderer(cellvalue, options, rowObject){
	 	var source = "";
	 	if (rowObject.state == 'COMMISSIONED') {
	 	source = "<button onclick=\"javascript: parent.parent.showPlugloadForm(" + rowObject.id + ");\"><spring:message code='fixtureForm.label.editBtn' /></button>";
	 	}
	 	else {
			<c:if test="${page == 'floor'}">
				source = "<button onclick=\"parent.parent.showPlugloadCommissioningIdentifyWindow(false, " + rowObject.gateway.id + "," +  rowObject.id + ")\"> <spring:message code='gatewayForm.label.commissionBtn' /> </button>";
			</c:if>
		}
		return source; 
	}
	function gatewayNameRenderer(cellvalue, options, rowObject){
		 // This is used, so that it can render the single records as well. otherwise jsonmap=gateway.name can be 
		// used
		if(rowObject.gateway.name == undefined){
			return "";
		}else{
			return rowObject.gateway.name;
		} 
		
	}
	

	function plugloadNameRenderer(cellvalue, options, rowObject)
	{
		
			return rowObject.name;
		
	}
	
	
	function profileNameRenderer(cellvalue, options, rowObject)
	{
		
			return rowObject.currentProfile;
		
	}

	
	function currVersionRenderer(cellvalue, options, rowObject) {
		return rowObject.version;
		
	}
	
function plugloadnamerenderer(cellvalue, options, rowObject)
{
/* 	if(rowObject.ishopper==1)
		return rowObject.name + " (Hopper) " ;
		else */
			return "Test1" ;
}
	function forceFitPlugloadTableHeight(){
		var jgrid = jQuery("#plugloadTable");
		var containerHeight = $(this).height();
		var otherElementHeight = $("#plugload-list-topPanel").height();
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .99)); 
		
	}

	function beforeDeleteMultiplugloads(){
		var selIds = jQuery("#plugloadTable").getGridParam('selarrrow');
		var fixNum = selIds.length;
		if(fixNum == 0 ){
			alert("Please select a plugload to delete");
			return false;
		}
		deletePlugload();
	}
	
	function deletePlugload(){
		var selIds = jQuery("#plugloadTable").getGridParam('selarrrow');
		var fixNum = selIds.length;
		var proceed = confirm("Are you sure you want to delete "+fixNum+ " selected plugloads");
		if(proceed){
			parent.parent.showDeletePlugloadDialog();
		}
	}
	
	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#plugloadTable").setGridWidth($(window).width()-20);
	}).trigger('resize');	
	
	
	function ModifyGridDefaultStyles() {  
		   $('#' + "plugloadTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "plugloadTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "plugloadTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
		   //$('#' + "plugloadTable").removeClass("ui-jqgrid-htable");
	}
	
	
	
</script>
<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}
</style>

<table id="plugloadTable"></table>
<div id="plugloadPagingDiv"></div>