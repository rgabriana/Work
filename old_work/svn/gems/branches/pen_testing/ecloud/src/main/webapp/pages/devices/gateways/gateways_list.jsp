<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<div id="GatewayDialog"></div>

<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}	
	#gwMessage {float: left; font-weight: bold; padding: 5px 0 0 5px;}
</style>

<div id="gateway-list-topPanel" style="background:#fff">
	<c:if test="${page == 'floor'}">		
		<div id="gateway-dialog-form">
				<div id="gwMessage">
				${message}
			</div>
		</div> 
		<br style="clear:both;"/>
		<div style="height:10px;"></div>
	</c:if>
</div>

<script type="text/javascript">
var PAGE = "${page}";
	$(document).ready(function() {
		
		//hide server message after 10 secs
		var hide_message_timer = setTimeout("clearGWMessage()", 10000);
		
		jQuery("#gatewayTable").jqGrid({
			url: '<spring:url value="/services/org/gateway/list/${page}/${pid}"/>',
			mtype: "GET",
			datatype: "json",
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			hoverrows: false,
			colNames:["id", "Name", "Channel", "Network ID", "Version", "Commissioned", "No. of Fixtures", "Upgrade Status", "Action"],
		   	colModel:[
				{name:'id', index:'id', hidden: true},
		   		{name:'name', index:'gatewayName', sorttype:"string", width:"22%"},
		   		{name:'channel', index:'channel', sorttype:"number", width:"11%"},
		   		{name:'wirelessnetworkid', index:'wirelessNetworkId', sorttype:"number", width:"11%",formatter:wirelessnetworkIdRenderer},
		   		{name:'app2version', index:'app2version', sorttype:"string", width:"11%"},
		   		{name:'commissioned', index:'commissioned', sorttype:"string", width:"11%"},
		   		{name:'noofactivesensors', index:'noOfActiveSensors', sorttype:"number", width:"11%"},
		   		{name:'upgradeStatus', index:'upgradeStatus', sorttype:"string", width:"11%"},
		   		{name:'action', index:'action', align:"right", sortable:false, hidden:(PAGE!=="floor"), width:"16%",formatter: actionGatewayRenderer}
		   	],
		   
		   	jsonReader: {repeatitems: false, root: function (obj) {return obj;}},
		   	cmTemplate: { title: false },
 		   	rowNum:100,
// 		    multiselect: true,

// 		   	rowList:[10,20,30],
// 		   	pager: '#gatewayPagingDiv',
		   	sortname: 'gatewayName',
		    viewrecords: true,
		    sortorder: "desc",
		    loadui: "block",
		   	toolbar: [false,"top"],
		    loadComplete: function(data) {
		    	 ModifyGridDefaultStyles();
		    }
		});
		
		forceFitGatewayTableHeight();
		
		jQuery("#gatewayTable").jqGrid('navGrid',"#gatewayPagingDiv",{edit:false,add:false,del:false});

		$("#gatewayTable").jqGrid().setGridParam({sortname: 'gatewayName', sortorder:'desc'}).trigger("reloadGrid");
		
	});
	function actionGatewayRenderer(cellvalue, options, rowObject){
		var source = "";
		if (rowObject.commissioned == true ) {
				source = "<button onclick=\"javascript: parent.parent.showGateWayForm(" + rowObject.id +","+ "${pid}"  + ");\"><spring:message code='gatewayForm.label.detailsBtn' /></button>";
		}
		return source;
	}
	function wirelessnetworkIdRenderer(cellvalue, options, rowObject)
	{
		return convertDecimalToHex(rowObject.wirelessnetworkid);
	}
	function convertDecimalToHex(decimalNo){
		decimalNo = 1*decimalNo;
		var hexNo = decimalNo.toString(16);
		return hexNo.toUpperCase();
	}
	function forceFitGatewayTableHeight(){
		var jgrid = jQuery("#gatewayTable");
		var containerHeight = $(this).height();
		var otherElementHeight = $("#gateway-list-topPanel").height();
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .99)); 
	}

	function clearGWMessage(){
		$("#gwMessage").html("");
	}
	
	function reloadGatewayListFrame(){
		var ifr = parent.document.getElementById('gatewaysFrame');
		window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = ifr.src + new Date().getTime();
	}

  	function ModifyGridDefaultStyles() {  
  		   $('#' + "gatewayTable" + ' tr').removeClass("ui-widget-content");
  		   $('#' + "gatewayTable" + ' tr:nth-child(even)').addClass("evenTableRow");
  		   $('#' + "gatewayTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
  	}  
      
	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#gatewayTable").setGridWidth($(window).width()-20);
	}).trigger('resize');	
</script>

<table id="gatewayTable" width="100%"></table>
<div id="addGatewayDialog" style="overflow: hidden"></div>

<div id="gatewayPagingDiv"></div>