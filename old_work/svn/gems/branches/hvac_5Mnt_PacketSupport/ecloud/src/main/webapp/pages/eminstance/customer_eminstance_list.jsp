<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/eminstance/loademinstbycustomerid/" var="getCustomerEmInstanceList" scope="request" />
<spring:url value="/services/org/facilityemmap/listCustomerCloudEmInstanceMapping/" var="getCustomerCloudEmInstanceList" scope="request" />
<spring:url value="/services/org/facilityemmap/delete/" var="deleteFacilityEmMappingUrl" scope="request" />

<spring:url value="/eminstance/mapeminstance.ems" var="mapEmInstanceUrl" scope="request" />
<spring:url value="/facilities/home.ems" var="customerFacilitiesUrl" />


<script type="text/javascript">
var mode='';
$(document).ready(function() {
	    mode = "${mode}" ;
		$('#searchCustomerEmInstString').val("");
		$("#searchCustomerEmInstColumn").val($("#searchCustomerEmInstColumn option:first").val());
		startCustomerEmInstance('1####END', 1, "name", "desc");
		$("#CustomerEmInstanceTable").setGridWidth($(window).width() - 40);
		
		$('#searchCustomerCloudString').val("");
		$("#searchCustomerCloudColumn").val($("#searchCustomerCloudColumn option:first").val());
		startCustomerEmInstanceMapping('1####END', 1, "emId", "desc");
		$("#CustomerCloudEmInstanceTable").setGridWidth($(window).width() - 40);
	
	});
	
	function reloadRegEmInstanceTable(){
		$('#CustomerEmInstanceTable').trigger( 'reloadGrid' );
	}
	
	function viewCustomerEmInstanceFormatter(cellvalue, options, rowObject) {
		var rowId = rowObject.id;
		var activeButtonHtml = "";
		var ipAddress = rowObject.ipAddress;
		activeButtonHtml = "<button onclick=\"javascript: mapEmInstance('"+rowId+"');\">Map</button>&nbsp;"
		// Show Link button only in case of Orchestrator mode as for Hosted Mode we have browsability feature to navigate to EM
		if(mode=='false')
		{
			activeButtonHtml+="<button onclick=\"javascript: navigateToEM('"+ipAddress+"');\">Link</button> ";
		}
		return activeButtonHtml;
	}

	function mapEmInstance(rowId){
		$("#mapEmInstanceDialog").load("${mapEmInstanceUrl}?emInstanceId="+rowId+"&customerId="+"${customer.id}"+"&ts="+new Date().getTime()).dialog({
	        title : "Map",
	        width :  Math.floor($('body').width() * .65),
	        //minHeight: 0,
	        height :  Math.floor($('body').height() * .45),
	        position : ['top',50],
	        overflow: scroll,
	        resizable: false,
	        create: function() {
	            //$(this).css("maxHeight", Math.floor($('body').width() * .35)); 
	        },
	        modal : true,
	        close: function() {
				$("#mapEmInstanceDialog").html("");
	        }
	    });
	}
	
	function searchCustomerEmInstanceList(){
		
		if($.trim($('#searchCustomerEmInstString').val()) == ''){
			alert("The search text box field is empty.To Search,Please enter some value.");
		}else{
			var userdata = "1" + "#" + $("#searchCustomerEmInstColumn").val() + "#" +encodeURIComponent($.trim($('#searchCustomerEmInstString').val())) + "#" + "true" + "#" + "END";
			$("#CustomerEmInstanceTable").jqGrid("GridUnload");
			startCustomerEmInstance(userdata, 1, "name", "desc");
		}
	}

	function resetCustomerEmInstanceList(){
		
		$("#CustomerEmInstanceTable").jqGrid("GridUnload");
		$('#searchCustomerEmInstString').val("");
		$("#searchCustomerEmInstColumn").val($("#searchCustomerEmInstColumn option:first").val());
		startCustomerEmInstance("1####END", 1, "name", "desc");
	}
		
		//function for pagination
	function startCustomerEmInstance(inputdata, pageNum, orderBy, orderWay) {
		jQuery("#CustomerEmInstanceTable").jqGrid({
			url: "${getCustomerEmInstanceList}"+"${customer.id}"+"?ts="+new Date().getTime(),
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
			colNames: ["EM Instance Id", "EM Name", "MAC Address", "IP Address","Version","Last Connectivity (UTC)","Time Zone","Action"],
		       colModel: [		       
		       { name: "id", index: 'id',sortable:true,width:'10%' },
		       { name: "name", index: 'name',sortable:true,sorttype:'string',width:'14%' },
		       { name: "macId", index: 'macId',sortable:true,sorttype:'string',width:'14%' },
		       { name: "ipAddress", index: 'ipAddress',sortable:true,sorttype:'string',width:'14%' },
		       { name: "version", index: 'version', sortable:true,sorttype:'string',width:'10%' },
		       { name: "utcLastConnectivityAt", index: 'utcLastConnectivityAt',sortable:true,sorttype:'string',width:'15%'},
		       { name: "timeZone", index: 'timeZone', sortable:true,sorttype:'string',width:'10%' },
		       { name: "action", index: 'action',sortable:false,width:'18%', align: "left",formatter: viewCustomerEmInstanceFormatter}],
		       
		   	jsonReader: { 
				root:"emInsts", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    pager: '#CustomerEmInstancePagingDiv',
		   	page: pageNum,
		   	sortorder: orderWay,
		   	sortname: orderBy,
		    hidegrid: false,
		    viewrecords: true,
		   	//loadui: "block",
		   	toolbar: [false,"top"],
		   	onSortCol: function(index, iCol, sortOrder) {
		   	},
		   	loadComplete: function(data) {
		   		if (data != null){
		   			if (data.emInsts != undefined) {
				   		if (data.emInsts.length == undefined) {
				   			jQuery("#CustomerEmInstanceTable").jqGrid('addRowData', 0, data.emInsts);
				   		}
				   	}
		   		}		   		
		   		ModifyRegEmInstanceDefaultStyles();
		   		
		   	}

		});
		
		jQuery("#CustomerEmInstanceTable").jqGrid('navGrid',"#CustomerEmInstancePagingDiv",
										{edit:false,add:false,del:false,search:false}, 
										{}, 
										{}, 
										{}, 
										{},
										{});
		
		forceFitRegEmTableWidth();
	}
		
	function navigateToEM(ip) {
	    var url = "https://"+ip+"/ems/";
	    window.open(url);
	}
	
	function ModifyRegEmInstanceDefaultStyles() {  
		   $('#' + "CustomerEmInstanceTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "CustomerEmInstanceTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "CustomerEmInstanceTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
		   $("#CustomerEmInstanceTable").setGridWidth($(window).width()-40);
	}
		
	function forceFitRegEmTableWidth(){
		var CustomerEmInstanceTableTableJgrid = jQuery("#CustomerEmInstanceTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#CustomerEmInstancePagingDiv").height();
		
		//CustomerEmInstanceTableTableJgrid.jqGrid("setGridHeight", (containerHeight - headerHeight -footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight)/2 - 10);
		var firstdivHeight = $("#firstdiv").height();
		CustomerEmInstanceTableTableJgrid.jqGrid("setGridHeight",  firstdivHeight - 100);
		$("#CustomerEmInstanceTable").setGridWidth($(window).width() - 40);
	}
	
	function startCustomerEmInstanceMapping(inputdata, pageNum, orderBy, orderWay) {
		jQuery("#CustomerCloudEmInstanceTable").jqGrid({
			url: "${getCustomerCloudEmInstanceList}"+"${customer.id}"+"?ts="+new Date().getTime(),
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
			colNames: ["EM Instance Id", "EM Name","Global Facility Path","EM Instance Facility Id","EM Instance Facility Path","Action"],
		       colModel: [		       
		       { name: "emId", index: 'emId', sortable:true,width:'10%' },
		       { name: "emName", index: 'emName',sortable:true,sorttype:'string',width:'14%' },
		       { name: "cloudFacilityNodePath", index: 'cloudFacilityNodePath', sortable:false,width:'23%' },
		       { name: "emFacilityId", index: 'emFacilityId', sortable:true,sorttype:'string',width:'10%' },
		       { name: "emFacilityPath", index: 'emFacilityPath', sortable:true,sorttype:'string',width:'23%' },
		       { name: "action", index: 'action',sortable:false,width:'10%', align: "left",formatter: viewCloudEmInstanceMappingFormatter}],
		       
		   	jsonReader: { 
				root:"facilityEminsts", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    pager: '#CustomerCloudEmInstancePagingDiv',
		   	page: pageNum,
		   	sortorder: orderWay,
		   	sortname: orderBy,
		    hidegrid: false,
		    viewrecords: true,
		   	//loadui: "block",
		   	toolbar: [false,"top"],
		   	onSortCol: function(index, iCol, sortOrder) {
		   	},
		   	loadComplete: function(data) {
		   		if (data != null){
		   			if (data.facilityEminsts != undefined) {
				   		if (data.facilityEminsts.length == undefined) {
				   			jQuery("#CustomerCloudEmInstanceTable").jqGrid('addRowData', 0, data.facilityEminsts);
				   		}
				   	}
		   		}		   		
		   		ModifyCustomerEmInstanceDefaultStyles();
		   		
		   	}

		});
		
		jQuery("#CustomerCloudEmInstanceTable").jqGrid('navGrid',"#CustomerCloudEmInstancePagingDiv",
										{edit:false,add:false,del:false,search:false}, 
										{}, 
										{}, 
										{}, 
										{},
										{});
		
		forceFitCloudEmTableWidth();
	}
	
	function viewCloudEmInstanceMappingFormatter(cellvalue, options, rowObject) {
		var rowId = rowObject.id;
		var deleteFacilityEmMappingHtml = "<button onclick=\"javascript: deleteFacilityEmMapping('"+rowId+"');\">Un-map</button> ";
		return deleteFacilityEmMappingHtml;
	}
	
	function ModifyCustomerEmInstanceDefaultStyles() {  
		   $('#' + "CustomerCloudEmInstanceTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "CustomerCloudEmInstanceTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "CustomerCloudEmInstanceTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
		   $("#CustomerCloudEmInstanceTable").setGridWidth($(window).width()-40);
	}
		
	function forceFitCloudEmTableWidth(){
		var CustomerCloudEmInstanceTableJgrid = jQuery("#CustomerCloudEmInstanceTable");
		var containerHeight2 = $("body").height();
		var headerHeight2 = $("#header").height();
		var footerHeight2 = $("#footer").height();
		var outerDivHeight2 = $("#outerDiv2").height();
		var gridHeaderHeight2 = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight2 = $("#CustomerCloudEmInstancePagingDiv").height();
		
		//CustomerCloudEmInstanceTableJgrid.jqGrid("setGridHeight", (containerHeight2 - headerHeight2 - footerHeight2 - outerDivHeight2 - gridHeaderHeight2 - gridFooterHeight2)/2 - 10);
		var seconddivHeight = $("#seconddiv").height();
		CustomerCloudEmInstanceTableJgrid.jqGrid("setGridHeight",  seconddivHeight - 100);
		$("#CustomerCloudEmInstanceTable").setGridWidth($(window).width() - 40);
	}
	
	function deleteFacilityEmMapping(facilityEmMappingId){
		var proceed = confirm("Are you sure you want to UnMap?");
		if(proceed==true) {
			$.ajax({
				url: "${deleteFacilityEmMappingUrl}"+facilityEmMappingId+"?ts="+new Date().getTime(),
				dataType : "json",
				contentType : "application/xml; charset=utf-8",
				success: function(data){
					reloadFacilityEmMappingTable();
					reloadRegEmInstanceTable();
					alert("Successfully UnMapped");
				}
			});
	 	}
	}
	
	function reloadFacilityEmMappingTable(){
		$('#CustomerCloudEmInstanceTable').trigger( 'reloadGrid' );
	}
	
	function backToFacilities(){
		$('#facilitiesCustomerId').val("${customer.id}");
		$('#customerFacilitiesForm').submit();	
	}
	
	function searchCustomerCloudList(){
		if($.trim($('#searchCustomerCloudString').val()) == ''){
			alert("The search text box field is empty.To Search,Please enter some value.");
		}else{
			var userdata = "1" + "#" + $("#searchCustomerCloudColumn").val() + "#" +encodeURIComponent($.trim($('#searchCustomerCloudString').val())) + "#" + "true" + "#" + "END";
			$("#CustomerCloudEmInstanceTable").jqGrid("GridUnload");
			startCustomerEmInstanceMapping(userdata, 1, "emId", "desc");
		}
	}

	function resetCustomerCloudList(){
		
		$("#CustomerCloudEmInstanceTable").jqGrid("GridUnload");
		$('#searchCustomerCloudString').val("");
		$("#searchCustomerCloudColumn").val($("#searchCustomerCloudColumn option:first").val());
		startCustomerEmInstanceMapping("1####END", 1, "emId", "desc");
	}
	
	
</script>


<div id="mapEmInstanceDialog"></div>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
		<div id="firstdiv" style="width: 100%; height: 46%;">
		<div id="outerDiv" style="padding: 5px 5px 5px 5px">
			 	<div style="float:right;padding: 0px 0px 0px 10px">
			 		<button id="backToFacilities" onclick="backToFacilities()">Back to Facilities</button>
			 	</div>
			 	<div style="float:right;padding: 0px 0px 0px 10px">
			 		<button id="resetCustomerEmInstButton" onclick="resetCustomerEmInstanceList()">Reset</button>
				</div>
				<div style="float:right;padding: 0px 0px 0px 10px">
					<button id="searchCustomerEmInstButton" onclick="searchCustomerEmInstanceList()">Search</button>
				</div>
				<div style="float:right;padding: 0px 0px 0px 10px">
					<input type="text" name="searchCustomerEmInstString" id="searchCustomerEmInstString">
				</div>
				<div style="float:right;padding: 0px 0px 0px 10px">
					<select id="searchCustomerEmInstColumn">
					  <option value="name">EM Name</option>
					  <option value="macId">MAC Address</option>
					  <option value="version">Version</option>
					  <option value="timeZone">Time Zone</option>
					</select>
				</div>
				<div style="float:right;padding: 0px 0px 0px 10px;font-weight: bolder; ">
					<label>Search by</label>
				</div>
	 			<div style="font-weight: bolder; ">Active EM Instances for Organization ${organizationname}</div>
	 			
	 			<div style="min-height:10px"></div>
	 		 	<div style="padding: 0px 5px;">
	 		 		<table id="CustomerEmInstanceTable"></table>
	 		 		<div id="CustomerEmInstancePagingDiv"></div>
	 		 	</div>
	 			
	 	</div>
	 	</div>
	 	<div style="min-height:25px"></div>
	 	<div id="seconddiv" style="width: 100%; height: 46%;">
	 	<div id="outerDiv2" style="padding: 5px 5px 5px 5px;">
			<div style="float:right;padding: 0px 0px 0px 10px">
	 			<button id="resetCustomerCloudButton" onclick="resetCustomerCloudList()">Reset</button>
	 		</div>
			<div style="float:right;padding: 0px 0px 0px 10px">
				<button id="searchCustomerCloudButton" onclick="searchCustomerCloudList()">Search</button>
			</div>
			<div style="float:right;padding: 0px 0px 0px 10px">
				<input type="text" name="searchCustomerCloudString" id="searchCustomerCloudString">
			</div>
			<div style="float:right;padding: 0px 0px 0px 10px">
				<select id="searchCustomerCloudColumn">
				  <option value="emId">EM Instance Id</option>
				  <option value="emFacilityId">EM Instance Facility Id</option>
				  <option value="emFacilityPath">EM Instance Facility Path</option>
				</select>
			</div>
			<div style="float:right;padding: 0px 0px 0px 10px;font-weight: bolder; ">
				<label>Search by</label>
			</div>
				<div style="font-weight: bolder; ">Mappings for Organization ${organizationname}</div>
			</div>
			
			<div style="min-height:10px"></div>
			<div style="padding: 0px 5px;">
				<table id="CustomerCloudEmInstanceTable"></table>
				<div id="CustomerCloudEmInstancePagingDiv"></div>
			</div>
			
		</div>
		</div>
</div>
<form id="customerFacilitiesForm" action="${customerFacilitiesUrl}" METHOD="POST">
	<input id="facilitiesCustomerId" name="customerId" type="hidden"/>
</form>