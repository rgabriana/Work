<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/services/org/imageupgradejob/loadImageUpgradeJobList" var="loadImageUpgradeJobList" scope="request" />

<spring:url value="/services/org/imageupgrade/cancelFirmwareUpgrade/" var="cancelFirmwareUpgradeUrl" scope="request" />

<style>
a, a:link, a:active, a:visited {
    color: #000000 !important;
    cursor: pointer;
}
a:hover {
    color: #000000 !important;
}
</style>

<script type="text/javascript">
	
	$(document).ready(function() {
		startImageUpgradeJobTable('1####END', 1, "scheduledTime","desc");
		$("#imageUpgradeJobTable").setGridWidth($(window).width() - 25);
	});
		
	//function for pagination
	function startImageUpgradeJobTable(inputdata, pageNum, orderBy, orderWay) {
			jQuery("#imageUpgradeJobTable").jqGrid({
				url: "${loadImageUpgradeJobList}?ts="+new Date().getTime(),
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
				colNames: ["id", "Job Name","Device Type","Image Name","status","noOfRetries","scheduledTime", "Start Time", "End Time", "Action"],
			       colModel: [
			       { name:'id', index:'id', hidden: true},
			       { name: 'jobName', index: 'jobName',sorttype:'string',width:'15%',formatter: imageUpgradeJobNameFormatter},
			       { name: 'deviceType', index: 'deviceType',sorttype:'string',width:'15%' },
			       { name: 'imageName', index: 'imageName',sorttype:'string',width:'15%' },
			       { name: 'status', index: 'status',sorttype:'string',width:'15%' },
			       { name: 'noOfRetries', index: 'noOfRetries',sorttype:'string',width:'15%' },
			       { name: "scheduledTime", index: 'scheduledTime', sorttype:'string',width:'15%' },
			       { name: "startTime", index: 'startTime', sorttype:'string',width:'15%' },
			       { name: "endTime", index: 'endTime', sorttype:'string',width:'15%' },
			       { name: "action", index: 'action',sortable:false,width:'12%', align: "left",formatter: viewImageUpgradeJobActionFormatter}],
			       
			   	jsonReader: { 
					root:"imageUpgradeJobs", 
			        page:"page", 
			        total:"total", 
			        records:"records", 
			        repeatitems:false,
			        id : "id"
			   	},
			    pager: '#imageUpgradeJobPagingDiv',
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
			   			if (data.imageUpgradeJobs != undefined) {
					   		if (data.imageUpgradeJobs.length == undefined) {
					   			jQuery("#imageUpgradeJobTable").jqGrid('addRowData', 0, data.imageUpgradeJobs);
					   		}
					   	}
			   		}
			   		
			   		ModifyImageUpgradeJobGridDefaultStyles();
			   		
			   	}
	
			});
			
			jQuery("#imageUpgradeJobTable").jqGrid('navGrid',"#imageUpgradeJobPagingDiv",
											{edit:false,add:false,del:false,search:false}, 
											{}, 
											{}, 
											{}, 
											{},
											{});
			
			forceFitImageUpgradeJobTableWidth();
		}
	
	function imageUpgradeJobNameFormatter(cellvalue, options, rowObject)
	{
		var imageUpgradeJobId = rowObject.id;
		var imageUpgradeJobName = rowObject.jobName;
		return "<a href='#' onclick='showImageUpgradeDeviceStatusGridByJobName(\""+imageUpgradeJobName+"\");' id='imageUpgradeJobNameFormatterId' style='text-decoration:underline'>"+imageUpgradeJobName+"</a>";
	}
	
	function showImageUpgradeDeviceStatusGridByJobName(imageUpgradeJobName){
		parent.jobName = imageUpgradeJobName;
		parent.document.getElementById('imagestatus').click();
	}
	
	function viewImageUpgradeJobActionFormatter(cellvalue, options, rowObject) {
		var imageUpgradeJobId = rowObject.id;
		var imageUpgradeJobName = rowObject.jobName;
		var imageUpgradeStatus = rowObject.status;
		if(imageUpgradeStatus == "Scheduled" || imageUpgradeStatus == "In Progress"){
			return "<button onclick=\"javascript: onCancelImageUpgradeJob('"+imageUpgradeJobId+"','"+imageUpgradeJobName+"');\">Cancel</button>";
		}else{
			return "";
		}
	}
	
	function onCancelImageUpgradeJob(imageUpgradeJobId,imageUpgradeJobName){
		var proceed = confirm("Are you sure you want to cancel the Job: "+imageUpgradeJobName+"?");
		if(proceed==true) {
			$.ajax({
				type: "POST",
				url: "${cancelFirmwareUpgradeUrl}"+imageUpgradeJobId+"?ts="+new Date().getTime(),
				dataType : "json",
				contentType : "application/xml; charset=utf-8",
				success: function(data){
					if(data.status == 1) {
						alert("The Job is cancelled");
						reloadImageUpgradeJobTable();
					}
				}
			});
	 	}
	}
	
	function reloadImageUpgradeJobTable(){
		$('#imageUpgradeJobTable').trigger( 'reloadGrid' );
	}
	
	function forceFitImageUpgradeJobTableWidth(){
		var jgrid = jQuery("#imageUpgradeJobTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var imageUpgradeJobDivHeight = $("#imageUpgradeJobDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#imageUpgradeJobPagingDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - imageUpgradeJobDivHeight - gridHeaderHeight - gridFooterHeight - 30);
		
		$("#imageUpgradeJobTable").setGridWidth($(window).width() - 25);
	}
	
	function ModifyImageUpgradeJobGridDefaultStyles() {  
		   $('#' + "imageUpgradeJobTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "imageUpgradeJobTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "imageUpgradeJobTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
		   $("#imageUpgradeJobTable").setGridWidth($(window).width()-25);	
	}

	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#imageUpgradeJobTable").setGridWidth($(window).width()-20);
	}).trigger('resize');
	
	function searchImageUpgradeJobList(){
		
		var userdata = "1" + "#" + $("#searchImageUpgradeJobColumn").val() + "#" +encodeURIComponent($.trim($('#searchImageUpgradeJobString').val())) + "#" + "true" + "#" + "END";
		$("#imageUpgradeJobTable").jqGrid("GridUnload");
		startImageUpgradeJobTable(userdata, 1, "scheduledTime", "desc");
	}

	function resetImageUpgradeJobList(){
		
		$("#imageUpgradeJobTable").jqGrid("GridUnload");
		$('#searchImageUpgradeJobString').val("");
		$("#searchImageUpgradeJobColumn").val($("#searchImageUpgradeJobColumn option:first").val());
		startImageUpgradeJobTable("1####END", 1, "scheduledTime", "desc");
	}
	
	
</script>
<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}
</style>



<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="imageUpgradeJobDiv" style="padding: 0px 5px;">
		<table cellspacing="10" style="width:100%">
				<tr>
					<td style="font-weight:bold;" align="left">Image Upgrade Job Table</td>
					<td align="right">
						<table>
							<tr>
								<td style="font-weight:bold;">Search by <select id="searchImageUpgradeJobColumn">
								  <option value="jobName">Job Name</option>
								  <option value="deviceType">Device Type</option>
								  <option value="imageName">Image Name</option>
								  <option value="status">Status</option>
								  </select>
								</td>
								<td><input type="text" name="searchImageUpgradeJobString" id="searchImageUpgradeJobString"/></td>
								<td><input type="button" id="searchImageUpgradeJobButton" onclick="searchImageUpgradeJobList()" value="Search" /></td>
								<td><input type="button" id="resetImageUpgradeJobButton" onclick="resetImageUpgradeJobList()" value="Reset"/></td>
							<tr>
						</table>
					</td>
				</tr>
		</table>
		<div style="min-height:5px"></div>
	</div>
	<div style="padding: 0px 5px;">
		<table id="imageUpgradeJobTable"></table>
		<div id="imageUpgradeJobPagingDiv"></div>
	</div>
 </div>