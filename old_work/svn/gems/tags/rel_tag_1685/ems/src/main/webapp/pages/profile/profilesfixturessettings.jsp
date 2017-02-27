<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<style type="text/css">
	html, body{margin:3px 3px 0px 3px !important; padding:0px !important; background: #ffffff;}	
</style>

<div id="fixtureDialog"></div>
<spring:url value="/themes/default/images/connected.png" var="fixtureConnected"/>
<script type="text/javascript">
	$(document).ready(function() {

		jQuery("#fixtureTable").jqGrid({
			datatype: "local",
			autowidth: true,
			forceFit: true,
			scrollOffset: 0,
		   	colNames:["Status", "MAC Address", "Sensor Name", "Current Image", "Version", "Gateway", "Current Profile", "Upgrade Status", "Action"],
		   	colModel:[
				{name:'status', index:'status', align:"center", sortable:false, width:"8%"},
		   		{name:'macAddress', index:'macAddress', sortable:false, width:"11%"},
		   		{name:'fixtureName', index:'fixtureName', sorttype:"string", width:"11%"},
		   		{name:'currApp', index:'currApp', sortable:false, width:"11%"},
		   		{name:'version', index:'version', sortable:false, width:"8%"},
		   		{name:'gatewayName', index:'gatewayName', sortable:false, width:"11%"},
		   		{name:'currentProfile', index:'currentProfile', sortable:false, width:"11%"},
		   		{name:'upgradeStatus', index:'upgradeStatus', sortable:false, width:"11%"},
		   		{name:'action', index:'action', align:"right", sortable:false, width:"18%"}
		   	],
// 		   	rowNum:30,
// 		   	rowList:[10,20,30],
// 		   	pager: '#fixturePagingDiv',
		   	sortname: 'fixtureName',
		    viewrecords: true,
		    sortorder: "desc"
		});
		
		forceFitFixtureTableHeight();
			
		var mydata =  [];
		
		<c:forEach items="${fixtures}" var="fixture">
			var localData = new Object;
			localData.status = " <img src=\"${fixtureConnected}\" alt=\"Connected\" />";
			localData.macAddress = "${fixture.macAddress}";
			localData.fixtureName = "${fixture.fixtureName}";
			localData.currApp = "${fixture.currApp}";
			localData.version = "${fixture.version}";
			localData.gatewayName = "${fixture.gateway.gatewayName}";
			localData.currentProfile = "${fixture.currentProfile}";
			localData.upgradeStatus = "${fixture.upgradeStatus}";
			localData.action = "";
			<c:choose>
				<c:when test="${fixture.state == 'COMMISSIONED'}">
					localData.action += "<button onclick=\"javascript: parent.parent.showFixtureForm(${fixture.id});\"><spring:message code='fixtureForm.label.editBtn' /></button>";
				</c:when>
				<c:otherwise>
					localData.action += "<button onclick=\"parent.parent.showFixtureCommissioningIdentifyWindow(false, ${fixture.gateway.id}, ${fixture.id})\"> <spring:message code='gatewayForm.label.commissionBtn' /> </button>";
				</c:otherwise>
			</c:choose>
			localData.action += "&nbsp;<button>Delete</button>";
								
			mydata.push(localData);
		</c:forEach>
		
		for(var i=0; i<=mydata.length; i++)
		{
			jQuery("#fixtureTable").jqGrid('addRowData', i+1, mydata[i]);
		}

		jQuery("#fixtureTable").jqGrid('navGrid',"#fixturePagingDiv",{edit:false,add:false,del:false});
				
	});
	
	function forceFitFixtureTableHeight(){
		var jgrid = jQuery("#fixtureTable");
		var containerHeight = $(this).height();
		var otherElementHeight = $("#fixture-list-topPanel").height();
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .99)); 
	}
</script>

<table id="fixtureTable"></table>
<div id="fixturePagingDiv"></div>