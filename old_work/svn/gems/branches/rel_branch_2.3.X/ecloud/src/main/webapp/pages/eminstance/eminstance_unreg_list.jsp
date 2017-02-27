<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/eminstance/listUnRegEmInstance/" var="getUnRegEmInstanceList" scope="request" />

<spring:url value="/eminstance/activate.ems" var="activatEmInstanceUrl" scope="request" />

<style>

</style>

<script type="text/javascript">

	$(document).ready(function() {
	    
		startUnRegEmInstance(1, "desc");
		$("#UnRegEmInstanceTable").setGridWidth($(window).width() - 25);
	
	});
	
	function viewUnRegEmInstanceFormatter(cellvalue, options, rowObject) {
		var rowId = rowObject.id;
		return "<button onclick=\"javascript: activateEM('"+rowId+"');\">Activate</button> ";
	}

	function activateEM(rowId){
		$("#emActivateDialog").load("${activatEmInstanceUrl}?emInstanceId="+rowId+"&ts="+new Date().getTime()).dialog({
	        title : "Activate EM Instance",
	        width :  Math.floor($('body').width() * .30),
	        minHeight : 250,
	        modal : true
	    });
	}
	
	function ModifyUnRegEmInstanceDefaultStyles() {  
		   $('#' + "UnRegEmInstanceTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "UnRegEmInstanceTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "UnRegEmInstanceTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
		   $("#UnRegEmInstanceTable").setGridWidth($(window).width()-25);
	}
	
	
	//function for pagination
	function startUnRegEmInstance(pageNum, orderWay) {
		jQuery("#UnRegEmInstanceTable").jqGrid({
			url: "${getUnRegEmInstanceList}"+"?ts="+new Date().getTime(),
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
			colNames: ["id","Version", "Mac Id","Last connectivity","Action"],
		       colModel: [
		       { name:'id', index:'id', hidden: true},
		       { name: "version", index: 'version', sortable:true,width:'6%' },
		       { name: "macId", index: 'mac_id', sortable:true,width:'6%' },
		       { name: "utcLastConnectivityAt", index: 'utcLastConnectivityAt',sortable:true,width:'10%'},
		       { name: "action", index: 'action',sortable:false,width:'10%', align: "right",formatter: viewUnRegEmInstanceFormatter}],
		       
		   	jsonReader: { 
				root:"emInsts", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    pager: '#UnRegEmInstancePagingDiv',
		   	page: pageNum,
		   	sortorder: orderWay,
		   	sortname: "version",
		    hidegrid: false,
		    viewrecords: true,
		   	loadui: "block",
		   	toolbar: [false,"top"],
		   	onSortCol: function(index, iCol, sortOrder) {
		   	},
		   	loadComplete: function(data) {
		   		if (data != null){
		   			if (data.emInsts != undefined) {
				   		if (data.emInsts.length == undefined) {
				   			jQuery("#UnRegEmInstanceTable").jqGrid('addRowData', 0, data.emInsts);
				   		}
				   	}
		   		}
		   		
		   		ModifyUnRegEmInstanceDefaultStyles();
		   		
		   	}

		});
		
		jQuery("#UnRegEmInstanceTable").jqGrid('navGrid',"#UnRegEmInstancePagingDiv",
										{edit:false,add:false,del:false,search:false}, 
										{}, 
										{}, 
										{}, 
										{},
										{});
		
		forceFitUnRegEmTableWidth();
	}
	//function for pagination

	
	function forceFitUnRegEmTableWidth(){
		var jgrid = jQuery("#UnRegEmInstanceTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#UnRegEmInstancePagingDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 10);
		
		$("#UnRegEmInstanceTable").setGridWidth($(window).width() - 25);
	}
	

</script>

<div id="emActivateDialog"></div>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
 	<div id="outerDiv" style="padding: 5px 5px 5px 5px">
 			<div style="font-weight: bolder; ">Unregistered EM Instances</div>
 			<div style="min-height:5px"></div>
     </div>
 	<div style="padding: 0px 5px;">
 		<table id="UnRegEmInstanceTable"></table>
 		<div id="UnRegEmInstancePagingDiv"></div>
 	</div>
</div>