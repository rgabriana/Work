<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<style type="text/css">
	html, body{margin:3px 3px 0px 3px !important; padding:0px !important; background: #ffffff;}	
</style>

<div id="fixtureDialog"></div>
<spring:url value="/themes/default/images/floorplan64/connectivityHealthy.png" var="fixtureConnected"/>
<spring:url value="/themes/default/images/floorplan64/connectivityPowerOff.png" var="fixtureNoConnectivity1"/>
<spring:url value="/themes/default/images/floorplan64/connectivityProblem.png" var="fixtureNoConnectivity2"/>
<spring:url value="/themes/default/images/noconnectivity3.png" var="fixtureNoConnectivity3"/>

<spring:url value="/themes/default/images/floorplan64/FixtureDescribedLocatedIdentified.png" var="fixturePlaced"/>
<spring:url value="/themes/default/images/floorplan64/FixtureDescribedLocatedIdentifiedHopper.png" var="fixturePlacedHopper"/>

<spring:url value="/themes/default/images/floorplan64/ConnectivityHealthyHopper.png" var="fixtureConnectivityHealthyHopper"/>
<spring:url value="/themes/default/images/floorplan64/ConnectivityPowerOffHopper.png" var="fixtureConnectivityPowerOffHopper"/>
<spring:url value="/themes/default/images/floorplan64/ConnectivityProblemHopper.png" var="fixtureConnectivityProblemHopper"/>

<script type="text/javascript">
	$(document).ready(function() {

		jQuery("#fixtureTable").jqGrid({
			datatype: "local",
			autowidth: true,
			forceFit: true,
			scrollOffset: 0,
			hoverrows: false,
		   	colNames:["id","Status", "MAC Address", "Sensor Name", "Version", "Gateway", "Location"],
		   	colModel:[
				{name:'id', index:'id', hidden:true},
				{name:'status', index:'status', align:"center", sortable:true, width:"8%"},
		   		{name:'macAddress', index:'macAddress', sortable:true, width:"11%"},
		   		{name:'fixtureName', index:'fixtureName', sorttype:"string", width:"11%"},
		   		{name:'version', index:'version', sorttype:"string", width:"8%"},
		   		{name:'gatewayName', index:'gatewayName', sorttype:"string", width:"8%"},
		   		{name:'location', index:'location', sorttype:"string", width:"14%"},
		   	],
		   	cmTemplate: { title: false },
		    multiselect: false,
 		   	pager: '#fixturePagingDiv',
 		 	page: 1,
		   	sortname: 'fixtureName',
		    viewrecords: true,
		    hidegrid: false,
			sortorder: 'asc',
		    gridComplete: function(){
		    	ModifyGridDefaultStyles();
		    }
		});
		
		forceFitFixtureTableHeight();
			
		var mydata =  [];
		
		<c:forEach items="${fixtures}" var="fixture">
			var localData = new Object;
			//localData.status = " <img src=\"${fixtureConnected}\" alt=\"Connected\" />";
			localData.status = profileFixtureStatusImageRenderer("${fixture.isHopper}","${fixture.state}","${fixture.lastConnectivityAt}");
			//localData.macAddress = "${fixture.macAddress}";
			localData.macAddress = snapAddressrenderer("${fixture.macAddress}");
			//localData.fixtureName = "${fixture.fixtureName}";
			localData.fixtureName = fixturenamerenderer("${fixture.isHopper}","${fixture.fixtureName}");
			localData.version = "${fixture.version}";
			localData.gatewayName = "${fixture.gateway.gatewayName}";
			localData.location = "${fixture.location}";
			localData.action = "";
			mydata.push(localData);
		</c:forEach>
		
		for(var i=0; i<=mydata.length; i++)
		{
			jQuery("#fixtureTable").jqGrid('addRowData', i+1, mydata[i]);
		}

		jQuery("#fixtureTable").jqGrid('navGrid',"#fixturePagingDiv",{edit:false,add:false,del:false});
		
		$("#fixtureTable").jqGrid().setGridParam({sortname: 'fixtureName', sortorder:'asc'}).trigger("reloadGrid");
				
	});
	
	function forceFitFixtureTableHeight(){
		var jgrid = jQuery("#fixtureTable");
		var containerHeight = $(this).height();
		var otherElementHeight = $("#fixture-list-topPanel").height();
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .97)); 
	}
	
	function ModifyGridDefaultStyles() {  
		   $('#' + "fixtureTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "fixtureTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "fixtureTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	}
	
	function snapAddressrenderer(cellvalue)
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
	
	function fixturenamerenderer(isHopper,name)
	{
		if(isHopper == 1)
			return name + " (Hopper) " ;
		else
			return name ;
	}
	
	function profileFixtureStatusImageRenderer(isHopper,fixtureState,lastConnectivityAt){

		console.log("lastConnectivityAt"+lastConnectivityAt);
		if (fixtureState == 'COMMISSIONED') {

			var serverCurrentTime =  "${currenttime}";
			var connectivityDifference = ((serverCurrentTime - parseFixtureConnectivityDate(lastConnectivityAt))/1000)/60;
			var source = "";
	
			if(connectivityDifference <= 15) // less than 15 mins
			{
				if(isHopper == 1){
					source = "${fixtureConnectivityHealthyHopper}";
				}else{
					source = "${fixtureConnected}";
				}
			}
			else if(connectivityDifference > 15 && connectivityDifference <= 10080 ) // between 15 minutes and 7 days
			{
				if(isHopper == 1){
					source = "${fixtureConnectivityPowerOffHopper}";
				}else{
					source = "${fixtureNoConnectivity1}";
				}
			}
			else if(connectivityDifference > 10080) // greater than 7 days
			{
				if(isHopper == 1){
					source = "${fixtureConnectivityProblemHopper}";
				}else{
					source = "${fixtureNoConnectivity2}";
				}
			}
				
	
			return "<img src=\""+source+"\" height=16 width=16/>";
		}
		else if (fixtureState == 'PLACED'){
			var sourcePlaced = "";
			if(isHopper == 1){
				sourcePlaced = "${fixturePlacedHopper}";
			}else{
				sourcePlaced = "${fixturePlaced}";
			}
						
			return "<img src=\""+sourcePlaced+"\" height=16 width=16/>";
		}
		else
			return "";
	}
	
	function parseFixtureConnectivityDate(str)
	{
		
		var date = 0;
		date = Date.parse(str);
		date = Date.parse(str.replace(" ", "T"));
		return date;
	}
	
</script>

<table id="fixtureTable"></table>
<div id="fixturePagingDiv"></div>