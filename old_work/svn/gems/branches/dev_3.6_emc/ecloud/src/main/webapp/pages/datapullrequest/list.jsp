<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<style>
#divform{width:100%;}
#form1{float:left; }
#btn1{float:left;}
#btn2{float:left;}
#btn3{float:left;} 
#btn4{float:left;}

</style>

<spring:url value="/datapullrequest/create.ems" var="dataPullRequestCreateDialogUrl" scope="request" />
<spring:url value="/services/org/datapullrequest/loaddata/" var="loadDataUrl" scope="request" />
<spring:url value="/services/org/datapullrequest/getdata/" var="getDataUrl" scope="request" />

<spring:url value="/services/org/datapullrequest/cancel/requestId/" var="cancelUrl" scope="request" />
<spring:url value="/services/org/datapullrequest/delete/requestId/" var="deleteUrl" scope="request" />

<spring:url value="/scripts/jquery/jquery.datetime.picker.0.9.9.js" var="jquerydatetimepicker"></spring:url>
<script type="text/javascript" src="${jquerydatetimepicker}"></script>


<div id="requestDialog"></div>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	
	<div id="outerDiv" style="padding: 10px 5px 5px 5px;">
			<div><span style="font-weight: bolder;">Energy Data Requests for ${customerName}</span></div>
			<div style="padding: 10px 0px 0px 0px;">
				<security:authorize access="hasAnyRole('Admin')">
				<button id="newButton" onclick="newPopup();">Create Energy Data Request</button> 
				<span id="cnfMsg" style="margin: 0px 20px;"></span><br>
				 </security:authorize>
			</div>	
			<div style="min-height:5px"></div>
    </div>
   
   <div style="padding: 0px 5px;">
		<table id="dataTable"></table>
		<div id="dataDiv"></div>
	</div>
 </div>
 
<script type="text/javascript">

	$(document).ready(function() {
		start(1, "requestDate", "desc");
		if("${create}" == "true") {
			cnfMsg('green', 'Energy Data Pull Request successfully created with task id = ' + "${id}");	
		}
		else if ("${create}" == "false") {
			cnfMsg('red', "Failed to create Energy Data Pull Request.");
		}
		
	});
	
	function cnfMsg(color, message) {
		console.log('testing');
		$("#cnfMsg").css('color', color);
		$("#cnfMsg").text(message);
	}

	function start(pageNum, orderBy, orderWay) {
		jQuery("#dataTable").jqGrid({
			url: "${loadDataUrl}"+"?ts="+new Date().getTime(),
			mtype: "POST",
			postData: {"customerId": ${custId}},
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
			colNames: ["Task Id", "EM", "Requested At", "Last Updated At", "Requested By","Replica Server","Table","From Date","To Date","State","Action"],
		       colModel: [
		       { name:'id', index:'id', sortable:true,sorttype:'string',width:'4%'},
		       { name: 'em', index: 'em',sortable:true,sorttype:'string',width:'10%'},
		       { name: "requestDate", index: 'requestDate', sortable:true,width:'8%'},
		       { name: "lastUpdateDate", index: 'lastUpdateDate', sortable:true,width:'8%'},
		       { name: "requestedBy", index: 'requestedBy', sortable:true, sorttype:'string',width:'8%'},
		       { name: "replicaServer", index: 'replicaServer',sortable:true,width:'10%'},
		       { name: "tableName", index: 'tableName', sortable:true,width:'12%'},
		       { name: "from", index: 'from',sortable:true,sorttype:'string',width:'8%'},
		       { name: "to", index: 'to',sortable:true,width:'8%'},
		       { name: "state", index: 'state',sortable:true,width:'6%'},
		       { name: "action", index: 'action',sortable:false,width:'18%',formatter: viewActionFormatter}],
		       
		   	jsonReader: { 
				root:"list", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    pager: '#dataDiv',
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
		   		if (data != null){
		   			if (data.emInsts != undefined) {
				   		if (data.emInsts.length == undefined) {
				   			jQuery("#dataTable").jqGrid('addRowData', 0, data.list);
				   		}
				   	}
		   		}
		   		ModifyGridDefaultStyles();
		   		
		   	}

		});
		
		jQuery("#dataTable").jqGrid('navGrid',"#dataDiv",
										{edit:false,add:false,del:false,search:false}, 
										{}, 
										{}, 
										{}, 
										{},
										{});
		
		forceFitDataTableWidth();
	}
	
	function ModifyGridDefaultStyles() {  
	   $('#' + "dataTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "dataTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "dataTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	}

	function forceFitDataTableWidth(){
		var jgrid = jQuery("#dataTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#dataDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 20);
		
		$("#dataTable").setGridWidth($(window).width() - 25);
	}
	

 
	function newPopup() {
 	
 		$("#requestDialog").load("${dataPullRequestCreateDialogUrl}?customerId="+ ${custId} + "&ts="+new Date().getTime(), function() {
 	  		  $("#requestDialog").dialog({
 	  				modal:true,
 	  				title: 'Create Energy Data Request',
 	  				width : 550,
 	  				height : 250,
 	  				closeOnEscape: false,
 	  				open: function(event, ui) {
 	  				},
 	  			});
 	  	});
 	}


	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#dataTable").setGridWidth($(window).width()-25);
	}).trigger('resize');
	
	
	function viewActionFormatter(cellvalue, options, rowObject) {
		var state = rowObject.state;
		var rowId = rowObject.id;
		var output = "<security:authorize access="hasAnyRole('Admin')"><div id=\"divForm\">";
		if(state == 'Successful'){
			output = output + "<div id=\"btn1\"><button onclick=\"window.location = \'${getDataUrl}" + rowId + "\'\">Download</button>&nbsp;</div>"; 	
		}
		if(state == 'Queued' || state == 'Processing') {
			output = output + "<div id=\"btn2\"><button onclick=\"javascript: onAction('"+rowId+"', 'cancel');\">Cancel</button>&nbsp;</div>";
		}
		if(state != 'Deleted') {
			output = output + "<div id=\"btn3\"><button onclick=\"javascript: onAction('"+rowId+"', 'delete');\">Delete</button></div>";
		}
		output = output + "</div></security:authorize>";
		return output;
	}
	
	function onAction(rowId, action){	
		if(confirm("Are you sure you want to "+ action +" an energy data request with id = "  + rowId + "?") == true) {
			updateState(rowId, action);
		}
	}

	function updateState(rowId, action){
		var url = "${cancelUrl}";
		var successMsg = "cancelled";
		if(action == 'delete') {
			url = "${deleteUrl}";
			successMsg = "deleted";
		}
		$.ajax({
			url : url + rowId + "?ts="+new Date().getTime(),
			async : false,
			success : function(data) {	
				if(data == "S") {
					cnfMsg('green', "Task with id " + rowId + " " + successMsg + " successfully." );
					$("#dataTable").trigger('reloadGrid');
				}
				else {
					cnfMsg('red', "Task with id " + rowId + " could not be " + successMsg + " successfully." );
				}
			},
		});	
	}
 	

 </script>