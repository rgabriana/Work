<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/appinstance/listUnRegAppInstance" var="getUnRegAppInstanceList" scope="request" />
<spring:url value="/services/org/appinstance/deleteUnRegAppInstance/" var="deleteUnRegAppInst" scope="request" />

<spring:url value="/appinstance/activate.ems" var="activateAppInstanceUrl" scope="request" />
<spring:url value="/scripts/jquery/jquery.datetime.picker.0.9.9.js" var="jquerydatetimepicker"></spring:url>
<script type="text/javascript" src="${jquerydatetimepicker}"></script>
<style>

</style>

<script type="text/javascript">

	$(document).ready(function() {
	    
		startUnRegAppInstance('1####END', 1, "version", "desc");
		$('#searchUnRegAppString').val("");
		$("#searchUnRegAppColumn").val($("#searchUnRegAppColumn option:first").val());
		$("#UnRegAppInstanceTable").setGridWidth($(window).width() - 25);
	
	});
	
	function viewUnRegAppInstanceFormatter(cellvalue, options, rowObject) {
		var rowId = rowObject.id;
		return "<button onclick=\"javascript: activateApp('"+rowId+"');\">Activate</button>&nbsp;<button onclick=\"javascript: onDeleteUnRegApp('"+rowId+"');\">Delete</button>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
	}

	function activateApp(rowId){
		$("#appActivateDialog").load("${activateAppInstanceUrl}?appInstanceId="+rowId+"&ts="+new Date().getTime()).dialog({
	        title : "Activate App Instance",
	        width :  Math.floor($('body').width() * .30),
	        minHeight : 250,
	        modal : true
	    });
	}
	
	function onDeleteUnRegApp(rowId){
		if(confirm("Are you sure you want to delete the UnRegistered App Instance?") == true)
		{
			$.ajax({
		 		type: 'POST',
		 		url: "${deleteUnRegAppInst}"+rowId+"?ts="+new Date().getTime(),
		 		success: function(data){
					searchUnRegAppInstanceList();
				},
				error: function(){
					alert("UnRegistered App Instance failed to delete");
				},
		 		contentType: "application/xml; charset=utf-8"
		 	});
		}
	}
	
	function ModifyUnRegAppInstanceDefaultStyles() {  
		   $('#' + "UnRegAppInstanceTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "UnRegAppInstanceTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "UnRegAppInstanceTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
		   $("#UnRegAppInstanceTable").setGridWidth($(window).width()-25);
	}
	
	
	//function for pagination
	function startUnRegAppInstance(inputdata, pageNum, orderBy, orderWay) {
		jQuery("#UnRegAppInstanceTable").jqGrid({
			url: "${getUnRegAppInstanceList}"+"?ts="+new Date().getTime(),
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
			colNames: ["id","Version", "MAC Address","Last Connectivity (UTC)","Action"],
		       colModel: [
		       { name:'id', index:'id', hidden: true},
		       { name: "version", index: 'version', sortable:true,width:'6%' },
		       { name: "macId", index: 'mac_id', sortable:true,width:'6%' },
		       { name: "utcLastConnectivityAt", index: 'utcLastConnectivityAt',sortable:true,width:'10%'},
		       { name: "action", index: 'action',sortable:false,width:'10%', align: "right",formatter: viewUnRegAppInstanceFormatter}],
		       
		   	jsonReader: { 
				root:"appInsts", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    pager: '#UnRegAppInstancePagingDiv',
			page: pageNum,
		   	sortorder: orderWay,
		   	sortname: orderBy,
		    hidegrid: false,
		    viewrecords: true,
		   	loadui: "block",
		   	toolbar: [false,"top"],
		   	onSortCol: function(index, iCol, sortOrder) {
		   	},
		   	loadComplete: function(data) {
		   		if (data != null){
		   			if (data.appInsts != undefined) {
				   		if (data.appInsts.length == undefined) {
				   			jQuery("#UnRegAppInstanceTable").jqGrid('addRowData', 0, data.appInsts);
				   		}
				   	}
		   		}
		   		
		   		ModifyUnRegAppInstanceDefaultStyles();
		   		
		   	}

		});
		
		jQuery("#UnRegAppInstanceTable").jqGrid('navGrid',"#UnRegAppInstancePagingDiv",
										{edit:false,add:false,del:false,search:false}, 
										{}, 
										{}, 
										{}, 
										{},
										{});
		
		forceFitUnRegAppTableWidth();
	}
	//function for pagination

	
	function forceFitUnRegAppTableWidth(){
		var jgrid = jQuery("#UnRegAppInstanceTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#UnRegAppInstancePagingDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 10);
		
		$("#UnRegAppInstanceTable").setGridWidth($(window).width() - 25);
	}
	
	function searchUnRegAppInstanceList(){
		
		var userdata = "1" + "#" + $("#searchUnRegAppColumn").val() + "#" +encodeURIComponent($.trim($('#searchUnRegAppString').val())) + "#" + "true" + "#" + "END";
		$("#UnRegAppInstanceTable").jqGrid("GridUnload");
		startUnRegAppInstance(userdata, 1, "version", "desc");
	}

	function resetunRegAppInstanceList(){
		
		$("#UnRegAppInstanceTable").jqGrid("GridUnload");
		$('#searchUnRegAppString').val("");
		$("#searchUnRegAppColumn").val($("#searchUnRegAppColumn option:first").val());
		startUnRegAppInstance("1####END", 1, "version", "desc");
	}
</script>

<div id="appActivateDialog"></div>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
 	<div id="outerDiv" style="padding: 5px 5px 5px 5px">
 	
 			<div>
				<div style="float:right;padding: 0px 0px 0px 10px">
					<button id="resetUnRegAppInstButton" onclick="resetunRegAppInstanceList()">Reset</button>
				</div>
				<div style="float:right;padding: 0px 0px 0px 10px">
					<button id="searchUnRegAppInstButton" onclick="searchUnRegAppInstanceList()">Search</button>
				</div>
				<div style="float:right;padding: 0px 0px 0px 10px">
					<input type="text" name="searchUnRegAppString" id="searchUnRegAppString">
				</div>
				<div style="float:right;padding: 0px 0px 0px 10px">
					<select id="searchUnRegAppColumn">
					  <option value="version">Version</option>
					  <option value="macId">MAC Address</option>
					</select>
				</div>
				<div style="float:right;padding: 0px 0px 0px 10px;font-weight: bolder; ">
					<label>Search by</label>
				</div>
				
				<div style="font-weight: bolder; ">Unregistered App Instances</div>
					
			</div>
			<div style="min-height:10px"></div>
			
     </div>
 	<div style="padding: 0px 5px;">
 		<table id="UnRegAppInstanceTable"></table>
 		<div id="UnRegAppInstancePagingDiv"></div>
 	</div>
</div>
