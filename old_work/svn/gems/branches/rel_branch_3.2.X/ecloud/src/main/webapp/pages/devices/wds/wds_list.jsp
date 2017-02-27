<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<script type="text/javascript">
var PAGE = "${page}";
var MAX_ROW_NUM = 99999;

	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#wdsTable").setGridWidth($(window).width()-20);
	}).trigger('resize');

	$(document).ready(function() {
		start("name", "desc");
	});
	
	function start(orderBy, orderWay) {			
		jQuery("#wdsTable").jqGrid({
			url: '<spring:url value="/services/org/wds/list/${page}/${pid}"/>',			
			mtype: "POST",			
			datatype: "json",
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			hoverrows: false,			
			colNames:['Name','MAC Address','Switch','Gateway','Version','Battery Level','Capture At','Upgrade Status','Action'],
		   	colModel:[
				{name:'name', index:'name', sorttype:"string", sortable:true, width:"20%"},
				{name:'macaddress', index:'macaddress', sorttype:"string", sortable:true, width:"15%", formatter: macAddressRenderer},
				{name:'switchName', index:'switchName', sorttype:"string", sortable:true, width:"20%"},
				{name:'gatewayName', index:'gatewayName', sorttype:"string", sortable:true, width:"15%"},
		   		{name:'version', index:'version', sorttype:"string", sortable:true, width:"15%" },
			   	{name:'batteryLevel', index:'batteryLevel', sorttype:"string", sortable:true, width:"15%", formatter: batteryLevelRenderer },
			   	{name:'captureAtStr', index:'captureAtStr', sorttype:"string", sortable:true, width:"15%" },
			   	{name:'upgradestatus', index:'upgradestatus', sorttype:"string", sortable:true, width:"15%"},
			   	{name:'action', index:'action', align:"right", hidden:(PAGE!="floor"), sortable:false, width:"15%", formatter: actionWdsRenderer}			   	
		   	],		   	
		   	jsonReader: {repeatitems: false, root: function (obj) {return obj;}},
		   	cmTemplate: { title: false },		    
 		   	rowNum:MAX_ROW_NUM,
		   	viewrecords: true,
		    sortorder: orderWay,
		   	sortname: orderBy,
		   	loadui: "block",
		   	toolbar: [false,"top"],
		   	onSortCol: function(index, iCol, sortOrder) {
		   		$('#orderWay').attr('value', sortOrder);
		   		$('#orderBy').attr('value', index);
		   	},
		    loadComplete: function(data) {
		    	ModifyGridDefaultStyles();
		    }
		});
		
		forceFitWdsTableHeight();
		var captureAtString = "Capture At (" + "${emtimezone}" + ")";
		jQuery("#wdsTable").jqGrid('setLabel', "captureAtStr", captureAtString);
	}
	
	
	function batteryLevelRenderer(cellvalue, options, rowObject){
		var bcolor = "#CCCCCC";
		var imageHolder='';
		var source = '';	
		
		imageHolder ='<div align="left" style="float:left;"><img id="erc_off" src="../../themes/default/images/erc_off.png"	 height="16" width="16" alt="" /></div>&nbsp;';
		if(rowObject.batteryLevel == 'Normal'){
		imageHolder ='<div align="left" style="float:left;"><img id="erc_healthy" src="../../themes/default/images/erc_healthy.png"	 height="16" width="16" alt="" /></div>&nbsp;';
		}
		if(rowObject.batteryLevel == 'Low'){
		imageHolder ='<div align="left" style="float:left;"><img id="erc_low" src="../../themes/default/images/erc_low.png"	 height="16" width="16" alt="" /></div>&nbsp;';
		}
		if(rowObject.batteryLevel == 'Critical'){
		imageHolder ='<div align="left" style="float:left;"><img id="erc_trouble" src="../../themes/default/images/erc_trouble.png"	 height="16" width="16" alt="" /></div>&nbsp;';
		}
		source=imageHolder+ rowObject.batteryLevel;
		return source;
	}	
	
	function actionWdsRenderer(cellvalue, options, rowObject){
		var source = "";
		
		if (rowObject.state == 'COMMISSIONED') {
				source = "<button onclick=\"javascript: parent.parent.showWdsDetailsForm(" + rowObject.id +","+ ${pid} + ");\"><spring:message code='wdsForm.label.detailsBtn' /></button>";
		}
		return source;
	}
	
	function macAddressRenderer(cellvalue, options, rowObject)
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
	
	function forceFitWdsTableHeight(){
		var jgrid = jQuery("#wdsTable");
		var containerHeight = $(this).height();
		var otherElementHeight = $("#switch-list-topPanel").height();
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .99)); 
	}
	
	function ModifyGridDefaultStyles() {  
		   $('#' + "wdsTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "wdsTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "wdsTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	}			

</script>

<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}
</style>
<table id="wdsTable"></table>
<div id="wdsPagingDiv"></div>
