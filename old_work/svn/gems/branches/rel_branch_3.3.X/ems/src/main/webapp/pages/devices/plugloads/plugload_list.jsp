<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<spring:url value="/services/org/plugload/" var="getPlacedPlugloadsByFloorIdUrl" scope="request" />
<div id="plugloadDialog"></div>
<spring:url value="/themes/default/images/floorplan64/pl_connected_on.png" var="plugloadConnected"/>
<spring:url value="/themes/default/images/floorplan64/pl_connected_off.png" var="plugloadNotConnected"/>
<spring:url value="/themes/default/images/floorplan64/pl_disconnected.png" var="plugloadDisConnected"/>
<spring:url value="/themes/default/images/floorplan64/pl_connected_on_notcomm.png" var="plugloadConnectedButNotCommissioned"/>
<spring:url value="/themes/default/images/floorplan64/PlugloadDescribedLocatedIdentifiedHopper.png" var="plugloadConnectedButNotCommissioneHopperd"/>

<div id="plugload-list-topPanel" style="background:#fff;">

	<div id="fixture-list-topLeftPanel" style="float:right;width: 410px;text-align: right;">
		<c:if test="${page == 'floor'}">
			<button id="commissionPlacedSensors" onclick="getPlacedPlugloadsByFloorId();">Commission Placed Plugloads</button>
			<button id="discoverFixtureButton" onclick="javascript: parent.parent.showPlugloadDiscoveryWindow();">Discover</button>
			<button id="bulkFixtureCommissionButton" onclick="javascript: parent.parent.showPlugloadCommissioningIdentifyWindow(true, 0, 0);">Bulk Commission</button>
		</c:if>
	</div>
	
	<div id="fixture-list-topRightPanel" style="text-align: left;">
		
		<button id="deletePlugloadButton" onclick="javascript: beforeDeleteMultiplugloads();">Delete</button>
				
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
		   	colNames:["id", "Status","Name","Image","Version", "Gateway","Current Profile", "Upgrade Status","Action"],
		   	colModel:[
				{name:'id', index:'id', hidden:true},		
				{name:'status', index:'status', align:"center", sortable:true, width:"8%", search:false, formatter: plStatusImageRenderer},
				{name:'plugloadName', index:'plugloadName', sortable:true, width:"10%", formatter: plugloadNameRenderer, searchoptions:{sopt:['cn']}},
				{name:'currapp', index:'currapp', sortable:true, width:"8%", search:false, formatter: plCurrAppRenderer},
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
	 	else if (rowObject.state == 'PLACED') {
			<c:if test="${page == 'floor'}">
				source = "";
			</c:if>
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
	
	function getPlacedPlugloadsByFloorId(){
		var floorId = "${pid}";
		$.ajax({
			url: "${getPlacedPlugloadsByFloorIdUrl}"+"placed/list/floor/"+floorId+"?ts="+new Date().getTime(),
			dataType:"json",
			type : "GET",
			contentType: "application/json; charset=utf-8",
			success: function(data){
				if(data!=null){
					parent.parent.showCommissionPlacedPlugloadWindow();
				}
				else{
					alert("There are no Placed Plugloads on this floor to Commission");
				}
			}
		});
	}
	function plStatusImageRenderer(cellvalue, options, rowObject){

		if (rowObject.state == 'COMMISSIONED') {

			var serverCurrentTime =  "${currenttime}";
			var connectivityDifference = ((serverCurrentTime - parsePlConnectivityDate(rowObject.lastconnectivityat))/1000)/60;
			
	        var source = "";
	
			if(connectivityDifference <= 15) // less than 15 mins
			{
				if(rowObject.isHopper == 1){
					source = "${plugloadConnected}";
				}else{
					source = "${plugloadConnected}";
				}
			}
			else if(connectivityDifference > 15 && connectivityDifference <= 10080 ) // between 15 minutes and 7 days
			{
				if(rowObject.isHopper == 1){
					source = "${plugloadNotConnected}";
				}else{
					source = "${plugloadNotConnected}";
				}
			}
			else if(connectivityDifference > 10080) // greater than 7 days
			{
				if(rowObject.isHopper == 1){
					source = "${plugloadDisConnected}";
				}else{
					source = "${plugloadDisConnected}";
				}
			}
			return "<img src=\""+source+"\" height=16 width=16/>";
		}
		else if (rowObject.state == 'PLACED'){
			var sourcePlaced = "";
			if(rowObject.isHopper == 1){
				sourcePlaced = "${plugloadConnectedButNotCommissioneHopperd}";
			}else{
				sourcePlaced = "${plugloadConnectedButNotCommissioned}";
			}
						
			return "<img src=\""+sourcePlaced+"\" height=16 width=16/>";
		}
		else
			return "";
	}
	function parsePlConnectivityDate(str)
	{
		
		var date = 0;
		// code to change the date format as Date.parse(str) function is failing in IE8 for the date format returned from server.
		//for example changing str from '2012-06-28T16:00:15.142+05:30' to '2012/06/28 16:00:15' and adding the millisec(142) after the parse for IE8
		if($.browser.msie && (parseInt($.browser.version) == 8)){
			var strArray = str.split(".");
			str = strArray[0].replace(/[-]/g, "/");
			str = str.replace(/[T]/g, " ");
			tempArray = strArray[1].split("+");
			date = Date.parse(str) + Number(tempArray[0]);
			return date;
			
		}else{
			date = Date.parse(str);
			return date;
		}
		
	}
	function plCurrAppRenderer(cellvalue, options, rowObject) {
		if (rowObject.version.indexOf("2") == 0) {
			return '<spring:message code="fixture.current.app.2" />';
		} else {
			if(cellvalue == '1') {
				return '<spring:message code="fixture.current.app.1" />';
			}
			else if(cellvalue == '2') {
				return '<spring:message code="fixture.current.app.2" />';
			}
			else {
				//TODO: return display text value instead of code.
				return cellvalue;	
			}
		}
		
	}
</script>
<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}
</style>

<table id="plugloadTable"></table>
<div id="plugloadPagingDiv"></div>