<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<style type="text/css">
	html, body{margin:3px 3px 0px 3px !important; padding:0px !important; background: #ffffff;}	
</style>

<spring:url value="/themes/default/images/connected.png" var="plugloadConnected"/>
<script type="text/javascript">
	$(document).ready(function() {

		jQuery("#plugloadTable").jqGrid({
			datatype: "local",
			autowidth: true,
			forceFit: true,
			scrollOffset: 0,
			hoverrows: false,
		   	colNames:["id","Status", "MAC Address", "Plugload Name", "Version", "Gateway", "Location"],
		   	colModel:[
				{name:'id', index:'id', hidden:true},
				{name:'status', index:'status', align:"center", sortable:true, width:"8%"},
		   		{name:'macAddress', index:'macAddress', sortable:true, width:"11%", formatter: snapAddressrenderer},
		   		{name:'name', index:'name', sorttype:"string", width:"11%"},
		   		{name:'version', index:'version', sorttype:"string", width:"8%"},
		   		{name:'gatewayName', index:'gatewayName', sorttype:"string", width:"8%"},
		   		{name:'location', index:'location', sorttype:"string", width:"14%"},
		   	],
		   	cmTemplate: { title: false },
		    multiselect: false,
 		   	pager: '#plugloadPagingDiv',
 		 	page: 1,
		   	sortname: 'name',
		    viewrecords: true,
		    hidegrid: false,
			sortorder: 'asc',
		    gridComplete: function(){
		    	ModifyGridDefaultStyles();
		    }
		});
		
		forceFitPlugloadDeviceTableHeight();
			
		var mydata =  [];
		
		<c:forEach items="${plugloads}" var="plugload">
			var localData = new Object;
			localData.status = " <img src=\"${plugloadConnected}\" alt=\"Connected\" />";
			localData.macAddress = "${plugload.macAddress}";
			localData.name = "${plugload.name}";
			localData.version = "${plugload.version}";
			localData.gatewayName = "${plugload.gateway.gatewayName}";
			localData.location = "${plugload.location}";
			localData.action = "";
			mydata.push(localData);
		</c:forEach>
		
		for(var i=0; i<=mydata.length; i++)
		{
			jQuery("#plugloadTable").jqGrid('addRowData', i+1, mydata[i]);
		}

		jQuery("#plugloadTable").jqGrid('navGrid',"#plugloadPagingDiv",{edit:false,add:false,del:false});
		
		$("#plugloadTable").jqGrid().setGridParam({sortname: 'name', sortorder:'asc'}).trigger("reloadGrid");
				
	});
	
	function forceFitPlugloadDeviceTableHeight(){
		var jgrid = jQuery("#plugloadTable");
		var containerHeight = $(this).height();
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - gridHeaderFooterHeight) * .97)); 
	}
	
	function ModifyGridDefaultStyles() {  
		   $('#' + "plugloadTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "plugloadTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "plugloadTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
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
</script>

<table id="plugloadTable"></table>
<div id="plugloadPagingDiv"></div>