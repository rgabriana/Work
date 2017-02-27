<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/services/org/imageupgradedevicestatus/loadImageUpgradeDeviceStatusList" var="loadImageUpgradeDeviceStatusList" scope="request" />

<script type="text/javascript">
	
	$(document).ready(function() {
		if(parent.jobName == ""){
			startImageUpgradeDeviceStatusTable('1####END', 1, "startTime", "desc");
		}else{
			$('#searchImageUpgradeDeviceStatusString').val(parent.jobName);
			$("#searchImageUpgradeDeviceStatusButton").click();
		}
		
		parent.jobName = "";
		
		$("#imageUpgradeDeviceStatusTable").setGridWidth($(window).width() - 25);
	});
		
	//function for pagination
	function startImageUpgradeDeviceStatusTable(inputdata, pageNum, orderBy, orderWay) {
			jQuery("#imageUpgradeDeviceStatusTable").jqGrid({
				url: "${loadImageUpgradeDeviceStatusList}?ts="+new Date().getTime(),
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
				colNames: ["id","Job Name","Device Name","Status","New Version","No Of Attempts","Start Time", "End Time","Description"],
			       colModel: [
			       { name:'id', index:'id', hidden: true},
			       { name: 'jobName', index: 'jobName',sorttype:'string',width:'15%' },
			       { name: 'deviceName', index: 'deviceName',sorttype:'string',width:'15%' },
			       { name: 'status', index: 'status',sorttype:'string',width:'10%' },
			       { name: 'new_version', index: 'new_version',sorttype:'string',width:'10%' },
			       { name: 'noOfAttempts', index: 'noOfAttempts',sorttype:'string',width:'10%' },
			       { name: "startTime", index: 'startTime', sorttype:'string',width:'20%' },
			       { name: "endTime", index: 'endTime', sorttype:'string',width:'20%' },
			       { name: "description", index: 'description', sorttype:'string',width:'20%' }],
			       
			   	jsonReader: { 
					root:"imageUpgradeDeviceStatuses", 
			        page:"page", 
			        total:"total", 
			        records:"records", 
			        repeatitems:false,
			        id : "id"
			   	},
			   	pager: '#imageUpgradeDeviceStatusPagingDiv',
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
			   			if (data.imageUpgradeDeviceStatuses != undefined) {
					   		if (data.imageUpgradeDeviceStatuses.length == undefined) {
					   			jQuery("#imageUpgradeDeviceStatusTable").jqGrid('addRowData', 0, data.imageUpgradeDeviceStatuses);
					   		}
					   	}
			   		}
			   		
			   		ModifyImageUpgradeDeviceStatusGridDefaultStyles();
			   		
			   	}
	
			});
			
			jQuery("#imageUpgradeDeviceStatusTable").jqGrid('navGrid',"#imageUpgradeDeviceStatusPagingDiv",
											{edit:false,add:false,del:false,search:false}, 
											{}, 
											{}, 
											{}, 
											{},
											{});
			
			forceFitImageUpgradeDeviceStatusTableWidth();
		}
	
	
	function forceFitImageUpgradeDeviceStatusTableWidth(){
		var jgrid = jQuery("#imageUpgradeDeviceStatusTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var imageUpgradeDeviceStatusDivHeight = $("#imageUpgradeDeviceStatusDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#imageUpgradeDeviceStatusPagingDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - imageUpgradeDeviceStatusDivHeight - gridHeaderHeight - gridFooterHeight - 30);
		
		$("#imageUpgradeDeviceStatusTable").setGridWidth($(window).width() - 25);
	}
	
	function ModifyImageUpgradeDeviceStatusGridDefaultStyles() {  
		   $('#' + "imageUpgradeDeviceStatusTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "imageUpgradeDeviceStatusTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "imageUpgradeDeviceStatusTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
		   $("#imageUpgradeDeviceStatusTable").setGridWidth($(window).width()-25);	
	}

	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#imageUpgradeDeviceStatusTable").setGridWidth($(window).width()-20);
	}).trigger('resize');
	
	
	function searchImageUpgradeDeviceStatusList(){
		
		var userdata = "1" + "#" + $("#searchImageUpgradeDeviceStatusColumn").val() + "#" +encodeURIComponent($.trim($('#searchImageUpgradeDeviceStatusString').val())) + "#" + "true" + "#" + "END";
		$("#imageUpgradeDeviceStatusTable").jqGrid("GridUnload");
		startImageUpgradeDeviceStatusTable(userdata, 1, "startTime", "desc");
	}

	function resetImageUpgradeDeviceStatusList(){
		
		$("#imageUpgradeDeviceStatusTable").jqGrid("GridUnload");
		$('#searchImageUpgradeDeviceStatusString').val("");
		$("#searchImageUpgradeDeviceStatusColumn").val($("#searchImageUpgradeDeviceStatusColumn option:first").val());
		startImageUpgradeDeviceStatusTable("1####END", 1, "startTime", "desc");
	}
	
	
</script>
<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}
</style>



<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="imageUpgradeDeviceStatusDiv" style="padding: 0px 5px;">
		<table cellspacing="10" style="width:100%">
				<tr>
					<td style="font-weight:bold;" align="left">Image Upgrade Device Status Table</td>
					<td align="right">
						<table>
							<tr>
								<td style="font-weight:bold;">Search by <select id="searchImageUpgradeDeviceStatusColumn">
								  <option value="jobName">Job Name</option>
								  <option value="deviceName">Device Name</option>
								  <option value="status">Status</option>
								  <option value="new_version">New version</option>
								  </select>
								</td>
								<td><input type="text" name="searchImageUpgradeDeviceStatusString" id="searchImageUpgradeDeviceStatusString"/></td>
								<td><input type="button" id="searchImageUpgradeDeviceStatusButton" onclick="searchImageUpgradeDeviceStatusList()" value="Search" /></td>
								<td><input type="button" id="resetImageUpgradeDeviceStatusButton" onclick="resetImageUpgradeDeviceStatusList()" value="Reset" /></td>
							<tr>
						</table>
					</td>
				</tr>
		</table>
	<div style="min-height:5px"></div>
	</div>
	<div style="padding: 0px 5px;">
		<table id="imageUpgradeDeviceStatusTable"></table>
		<div id="imageUpgradeDeviceStatusPagingDiv"></div>
	</div>
 </div>