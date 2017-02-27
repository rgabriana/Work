<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/fixture/count/gateway/" var="getFixtureCountByGatewayUrl" scope="request" />
<spring:url value="/services/org/wds/list/secondarygateway/" var="getWdsCountByGatewayUrl" scope="request" />
<spring:url value="/services/org/gateway/decommission" var="deleteGatewayUrl" scope="request" />

<div id="GatewayDialog"></div>

<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}	
	#gwMessage {float: left; font-weight: bold; padding: 5px 0 0 5px;}
</style>

<div id="gateway-list-topPanel" style="background:#fff">
	<c:if test="${page == 'floor'}">		
		<div id="gateway-dialog-form">
			<spring:url
				value="/devices/gateways/discoverGateway.ems?floorId=${floorId}"
				var="actionDiscoverURL" scope="request" />
			<spring:url
				value="/devices/gateways/addGateway.ems?floorId=${floorId}"
				var="actionAddGatewayURL" scope="request" />
	
			<c:if test="${dhcpEnable == 'true'}">		
				<form id="discoverGatewayButton" name="discoverGateway" method="post" action="${actionDiscoverURL}">
					<input type="submit" id="discoverGatewayBtn" value="<spring:message code='gatewayForm.label.discoverBtn'/>" style="float: right; margin-left:5px" />
				</form>
			</c:if>
			<c:if test="${dhcpEnable == 'false'}">		
				<form id="addGatewayButton" name="addGateway" method="post" action="${actionAddGatewayURL}">
					<input type="button" id="addGatewayBtn" onClick="return addGatewayDialog('${actionAddGatewayURL}')" value="<spring:message code='gatewayForm.label.addGatewayBtn'/>" style="float: right; margin-left:5px" />
				</form>
			</c:if>
			<form id="bulkGatewayCommissionButton" name="bulkCommissionGateway">
				<input type="button" id="bulkGatewayCommissionBtn" onclick="parent.parent.showGatewayCommissioningForm(0)" value="<spring:message code='gatewayForm.label.bulkCommissionBtn'/>" style="float: right;" />
			</form>
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
			datatype: "local",
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			hoverrows: false,
			colNames:["id", "Name", "Channel", "Network ID", "Version", "Commissioned", "No. of Fixtures", "Upgrade Status", "Action"],
		   	colModel:[
				{name:'id', index:'id', hidden: true},
		   		{name:'gatewayName', index:'gatewayName', sorttype:"string", width:"22%"},
		   		{name:'channel', index:'channel', sorttype:"number", width:"11%"},
		   		{name:'wirelessNetworkId', index:'wirelessNetworkId', sorttype:"number", width:"11%"},
		   		{name:'app2Version', index:'app2Version', sorttype:"string", width:"11%"},
		   		{name:'commissioned', index:'commissioned', sorttype:"string", width:"11%"},
		   		{name:'noOfActiveSensors', index:'noOfActiveSensors', sorttype:"number", width:"11%"},
		   		{name:'upgradeStatus', index:'upgradeStatus', sorttype:"string", width:"11%"},
		   		{name:'action', index:'action', align:"right", sortable:false, hidden:(PAGE=="area"), width:"16%"}
		   	],
		   	cmTemplate: { title: false },
// 		    multiselect: true,
// 		   	rowNum:30,
// 		   	rowList:[10,20,30],
// 		   	pager: '#gatewayPagingDiv',
		   	sortname: 'gatewayName',
		    viewrecords: true,
		    sortorder: "desc",
		    loadComplete: function() {
		    	 ModifyGridDefaultStyles();
		    }
		});
		
		forceFitGatewayTableHeight();
		
		
		var mydata =  [];
		
		<c:forEach items="${gateways}" var="gateway">
			var localData = new Object;
			localData.id = "${gateway.id}";
			localData.gatewayName = '<c:out value="${gateway.gatewayName}" escapeXml="true" />';
			localData.channel = "${gateway.channel}";
			localData.wirelessNetworkId = convertDecimalToHex("${gateway.wirelessNetworkId}");
			localData.app2Version = "${gateway.app2Version}";
			localData.commissioned = "${gateway.commissioned}";
			localData.noOfActiveSensors = "${gateway.noOfActiveSensors}";
			localData.upgradeStatus = "${gateway.upgradeStatus}";
			localData.action = "";
			<c:if test="${page == 'floor'}">
				<c:choose>
					<c:when test="${gateway.commissioned == 'true'}">
							localData.action += "<button onclick=\"parent.parent.showGateWayForm(${gateway.id})\">"+ 
													"<spring:message code='gatewayForm.label.editBtn' />"+
												"</button>";
					</c:when>
					<c:otherwise>
							localData.action += "<button onclick=\"parent.parent.showGatewayCommissioningForm(${gateway.id})\">"+
													"<spring:message code='gatewayForm.label.commissionBtn' />"+
												"</button>";
					</c:otherwise>
				</c:choose>
			</c:if>
			localData.action += "&nbsp;<button onclick=\"beforeDeleteGateway(${gateway.id});\">Delete</button>";
			mydata.push(localData);
		</c:forEach>

		// Fetch Uncommissioned gateways from other floors
		var bFound = false;
		<c:forEach items="${uncommissionedgateways}" var="gateway">
			var gwName = "${gateway.gatewayName}";
			bFound = false;
			for(var i=0; i<=mydata.length; i++)
			{
				if (mydata[i] != null) {
					if (mydata[i].gatewayName == gwName) {
						bFound = true;
						break;
					}
				}
			}
			if (bFound == false) {
				var localData = new Object;
				localData.id = "${gateway.id}";
				localData.gatewayName = "${gateway.gatewayName}";
				localData.channel = "${gateway.channel}";
				localData.wirelessNetworkId = convertDecimalToHex("${gateway.wirelessNetworkId}");
				localData.app2Version = "${gateway.app2Version}";
				localData.commissioned = "${gateway.commissioned}";
				localData.noOActiveSensors = "${gateway.noOfActiveSensors}";
				localData.upgradeStatus = "${gateway.upgradeStatus}";
				localData.action = "";
				<c:if test="${page == 'floor'}">
					<c:choose>
						<c:when test="${gateway.commissioned == 'false'}">
							localData.action += "<button onclick=\"parent.parent.showGatewayCommissioningForm(${gateway.id})\">"+
							"<spring:message code='gatewayForm.label.commissionBtn' />"+
							"</button>";
						</c:when>
					</c:choose>
				</c:if>
				localData.action += "&nbsp;<button onclick=\"beforeDeleteGateway(${gateway.id});\">Delete</button>";
				mydata.push(localData);
			}
		</c:forEach>

		for(var i=0; i<=mydata.length; i++)
		{
			jQuery("#gatewayTable").jqGrid('addRowData', i+1, mydata[i]);
		}

		jQuery("#gatewayTable").jqGrid('navGrid',"#gatewayPagingDiv",{edit:false,add:false,del:false});

		$("#gatewayTable").jqGrid().setGridParam({sortname: 'gatewayName', sortorder:'desc'}).trigger("reloadGrid");
		
	});
	
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
	
	function convertDecimalToHex(decimalNo){
		decimalNo = 1*decimalNo;
		var hexNo = decimalNo.toString(16);
		return hexNo.toUpperCase();
	}
	
	function beforeDeleteGateway(gatewayId){
			// get associated fixtures count
			$.ajax({
				type: 'GET',
				url: "${getFixtureCountByGatewayUrl}"+gatewayId+"?ts="+new Date().getTime(),
				success: function(data){
					if(data != null){
						if((1*data.msg) == 0){
							// get associated fixtures count
							$.ajax({
								type: 'GET',
								url: "${getWdsCountByGatewayUrl}"+gatewayId+"?ts="+new Date().getTime(),
								success: function(data){
									if(data != null){
										var xml=data.getElementsByTagName("wds");
										if(xml != undefined && xml.length != 0)
											alert("This Gateway has EWS(s) associated with it. It cannot be deleted");
										else if(confirm('Are you sure you want to delete this gateway?'))
										{
										  deleteGateway(gatewayId);
										}
									}
								},
								dataType:"xml",
								contentType: "application/json; charset=utf-8"
							});
						} else {
							alert("This Gateway has fixture(s) associated with it. It cannot be deleted");
						}
					}
				},
				dataType:"json",
				contentType: "application/json; charset=utf-8"
			});
	}
	
	function deleteGateway(gatewayId){
		$.ajax({
			type: 'POST',
			url: "${deleteGatewayUrl}"+"?ts="+new Date().getTime(),
			data: "<gateway><id>"+gatewayId+"</id></gateway>",
			success: function(data){
				reloadGatewayListFrame();
			},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8"
		});
	}
	
	function reloadGatewayListFrame(){
		var ifr = parent.document.getElementById('gatewaysFrame');
		window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = ifr.src + new Date().getTime();
	}

  	var addGatewayDialog = function(url){
      	$("#addGatewayDialog").load(url).dialog({
              title : "Add Gateway",
              width :  425,
              height : 200,
              modal : true,
              close: function(event, ui) { location.href = "/ems/devices/gateways/manage.ems"; }
          });
          return false;
      };
      

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