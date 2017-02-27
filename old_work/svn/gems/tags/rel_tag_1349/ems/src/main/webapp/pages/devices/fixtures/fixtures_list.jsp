<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<div id="fixtureDialog"></div>

<div id="fixture-list-topPanel" style="background:#fff;">
	<c:if test="${page == 'floor'}">
		<button id="discoverFixtureButton" onclick="javascript: parent.parent.showFixtureDiscoveryWindow();">Discover</button>
		<button id="bulkFixtureCommissionButton" onclick="javascript: parent.parent.showFixtureCommissioningIdentifyWindow(true, 0, 0);">Bulk Commission</button>
	</c:if>
	<button id="deleteFixtureButton" onclick="javascript: beforeDeleteMultiFixtures();">Delete</button>
	<div style="height:10px;"></div>
</div>

<spring:url value="/themes/default/images/connected.png" var="fixtureConnected"/>
<spring:url value="/themes/default/images/noconnectivity1.png" var="fixtureNoConnectivity1"/>
<spring:url value="/themes/default/images/noconnectivity2.png" var="fixtureNoConnectivity2"/>
<spring:url value="/themes/default/images/noconnectivity3.png" var="fixtureNoConnectivity3"/>

<script type="text/javascript">
var PAGE = "${page}";
	$(document).ready(function() {
			start('1#######END', 1, "", "desc");
	});

	function start(inputdata, pageNum, orderBy, orderWay) {
		jQuery("#fixtureTable").jqGrid({
			url: '<spring:url value="/services/org/fixture/list/alternate/filter/${page}/${pid}"/>',
			userData: "userdata",
			mtype: "POST",
			postData: {"userData": inputdata},
			datatype: "json",
			autoencode: true,
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			formatter: {
				 integer: {thousandsSeparator: ",", defaultValue: '0'},
			     number: {decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, defaultValue: '0.00'}
			},
		   	colNames:["id", "Status", "MAC Address", "Sensor Name", "Image", "Version", "Gateway", "Current Profile", "Upgrade Status", "Action"],
		   	colModel:[
				{name:'id', index:'id', hidden:true},
				{name:'status', index:'status', align:"center", sortable:true, width:"5%", search:false, formatter: statusImageRenderer},
		   		{name:'snapaddress', index:'snapaddress', sortable:true, width:"12%", searchoptions:{sopt:['cn']},formatter: snapAddressrenderer},
		   		{name:'name', index:'name', sortable:true, width:"20%", searchoptions:{sopt:['cn']},formatter: fixturenamerenderer},
		   		{name:'currapp', index:'currapp', sortable:true, width:"6%", search:false, formatter: currAppRenderer},
		   		{name:'version', index:'version', sortable:true, width:"8%", searchoptions:{sopt:['cn']}},
		   		{name:'gateway', index:'gateway', sortable:true, width:"10%", searchoptions:{sopt:['cn']}, formatter: gatewayNameRenderer},
		   		{name:'currentprofile', index:'currentprofile', sortable:true, width:"12%", searchoptions:{sopt:['cn']}},
		   		{name:'upgradestatus', index:'upgradestatus', sortable:true, width:"13%", searchoptions:{sopt:['cn']}},
		   		{name:'state', index:'state', align:"right", hidden:(PAGE!="floor"), sortable:true, width:"14%", searchoptions:{sopt:['cn']}, formatter: actionImageRenderer}
		   	],
		   	jsonReader: { 
		        root:"fixture", 
		        page:"page", 
		        total:"total", 
		        records:"records",
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    multiselect: true,
 		   	pager: '#fixturePagingDiv',
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
		   		if (data.fixture != undefined) {
			   		if (data.fixture.length == undefined) {
			   			// Hack: Currently, JSON serialization via jersey treats single item differently
			   			
			   			jQuery("#fixtureTable").jqGrid('addRowData', 0, data.fixture);
			   		}
			   	}
		   	}

		});
		
		jQuery("#fixtureTable").jqGrid('navGrid',"#fixturePagingDiv",{edit:false,add:false,del:false});
		forceFitFixtureTableHeight();
		/*	
		var mydata =  [];
		var lastConnectivityAt;
		var serverCurrentTime = "${currenttime}";
		
		<c:forEach items="${fixtures}" var="fixture">
			var localData = new Object;
// 			localData.status = " <img src=\"${fixtureConnected}\" alt=\"Connected\" />";
			lastConnectivityAt = "${fixture.lastConnectivityAt}";
			localData.status =  statusImageRenderer(serverCurrentTime, lastConnectivityAt);
			
			localData.id = "${fixture.id}";
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
					<c:if test="${page == 'floor'}">
						localData.action += "<button onclick=\"parent.parent.showFixtureCommissioningIdentifyWindow(false, ${fixture.gateway.id}, ${fixture.id})\"> <spring:message code='gatewayForm.label.commissionBtn' /> </button>";
					</c:if>
				</c:otherwise>
			</c:choose>
								
			mydata.push(localData);
		</c:forEach>

		for(var i=0; i<=mydata.length; i++)
		{
			jQuery("#fixtureTable").jqGrid('addRowData', i+1, mydata[i]);
		}
		*/
	}
	
	function statusImageRenderer(cellvalue, options, rowObject){
		var serverCurrentTime = "${currenttime}";
		var connectivityDifference = (serverCurrentTime - parseLastConnectivityDate(rowObject.lastconnectivityat));
		
		var connectivityLevel3 = 1000 * 60 * 60 * 24 * 7;
        var connectivityLevel2 = 1000 * 60 * 60 * 24;
        var connectivityLevel1 = 1000 * 60 * 10;

        var source = "";
        if(connectivityDifference > connectivityLevel3) {
			source = "${fixtureNoConnectivity3}";
        } else if(connectivityDifference > connectivityLevel2) {
			source = "${fixtureNoConnectivity2}";
        } else if(connectivityDifference > connectivityLevel1) {
			source = "${fixtureNoConnectivity1}";
        } else {
			source = "${fixtureConnected}";
        }
        
		return "<img src=\""+source+"\"/>";
	}
	
	function parseLastConnectivityDate(dateStr){
		var year = 0;
		var month = 0;
		var day = 0;
		var hour = 0;
		var min = 0;
		var sec = 0;
		var milli = 0;
		
		var tkns = dateStr.split(" ");
		var datePart = tkns[0]; //Date part
		var dateTkn = datePart.split("-"); // year - month - day
		if(dateTkn.length >= 3){
			year = dateTkn[0];
			month = dateTkn[1];
			day = dateTkn[2];
		}
		
		if(tkns.length > 1){ //Time part
			var timePart = tkns[1];
			var timePartTkns = timePart.split(".");
			var minPart = timePartTkns[0];
			var minTkn = minPart.split(":"); //hours : minutes : sec
			if(minTkn.length >= 3){
				hour = minTkn[0];
				min = minTkn[1];
				sec = minTkn[2];
			}
			
			if(timePartTkns.length > 1){ //Milli seconds
				milli = timePartTkns[1];
			}
		}
		//alert(dateStr + ">"+ year + "," +  month + "," + date + "," + hour + "," + min + "," + sec + "," + milli);
		return new Date(year,  month, day, hour, min, sec, milli).getTime();
	}
	
	function actionImageRenderer(cellvalue, options, rowObject){
		var source = "";
		if (rowObject.state == 'COMMISSIONED') {
				source = "<button onclick=\"javascript: parent.parent.showFixtureForm(" + rowObject.id + ");\"><spring:message code='fixtureForm.label.editBtn' /></button>";
		} else {
			<c:if test="${page == 'floor'}">
				source = "<button onclick=\"parent.parent.showFixtureCommissioningIdentifyWindow(false, " + rowObject.gateway.id + "," +  rowObject.id + ")\"> <spring:message code='gatewayForm.label.commissionBtn' /> </button>";
			</c:if>
		}
		return source;
	}

	function gatewayNameRenderer(cellvalue, options, rowObject){
		// This is used, so that it can render the single records as well. otherwise jsonmap=gateway.name can be 
		// used
		return rowObject.gateway.name;
	}
	
	function currAppRenderer(cellvalue, options, rowObject) {
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
function fixturenamerenderer(cellvalue, options, rowObject)
{
	if(rowObject.ishopper==1)
		return rowObject.name + " (Hopper) " ;
		else
			return rowObject.name ;
}
	function forceFitFixtureTableHeight(){
		var jgrid = jQuery("#fixtureTable");
		var containerHeight = $(this).height();
		var otherElementHeight = $("#fixture-list-topPanel").height();
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .99)); 
	}

	function beforeDeleteMultiFixtures(){
		var selIds = jQuery("#fixtureTable").getGridParam('selarrrow');
		var fixNum = selIds.length;
		if(fixNum == 0 ){
			alert("Please select a fixture to delete");
			return false;
		}
		
		deleteFixtures();
	}
	
	function deleteFixtures(){
		var proceed = confirm("Are you sure you want to delete selected fixtures");
		if(proceed){
			parent.parent.showDeleteFixtureDialog();
		}
	}
	
	function snapAddressrenderer(cellvalue, options, rowObject)
	{
		var strMacAddress = "";
        if(cellvalue.indexOf(":") <= 0)
        {
            while(cellvalue.length > 0)
            {
                if(cellvalue.length >= 3)
                {
                    strMacAddress += cellvalue.substr(0,2) + ":";
                    cellvalue = cellvalue.substr(2);
                }
                else
                {
                    strMacAddress += cellvalue.substr(0,2);
                    cellvalue = cellvalue.substr(2);
                }
            }
        }
        else
        {
        	var macSplitArray;
        	macSplitArray = cellvalue.split(':');
        	for (var i = 0; i < macSplitArray.length; i++) {
        	    if(macSplitArray[i].length == 1){
        	    	macSplitArray[i] = "0"+macSplitArray[i];
        	    }
        	    
        	    if(i == 0){
        			strMacAddress = macSplitArray[i];
        		}else{
        			strMacAddress = strMacAddress + ":" + macSplitArray[i];
        		}
        	    
        	}
        }
       return strMacAddress;
	}
	
	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#fixtureTable").setGridWidth($(window).width()-20);
	}).trigger('resize');	
	
</script>
<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}
</style>
<table id="fixtureTable"></table>
<div id="fixturePagingDiv"></div>