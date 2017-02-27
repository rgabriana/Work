<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<div id="GroupsDialog"></div>

<div id="group-list-topPanel" style="background: #fff">
	<c:if test="${page == 'floor'}">
		<div style="display: inline;"><button id="newGroup" onclick="javascript:parent.parent.assignGemsGroupToFixtures();">Create/Join Group</button></div>
	</c:if>
	<div style="display: inline;"><span style="font-weight:bold;" id="deleteScheduledMsg"></span></div>
	<div style="height:5px;"></div>

</div>

<script type="text/javascript">
var PAGE = "${page}";
var MAX_ROW_NUM = 99999;
<spring:url value="/services/org/gemsgroups/op/deletegroup/" var="deleteGroupUrl" scope="request" />
	$(document).ready(function() {
		
		jQuery("#groupTable").jqGrid({
			datatype: "local",
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
		   	colNames:['Name', 'Action'],
		   	colModel:[
		   		{name:'groupName', index:'groupName', sorttype:"string", width:"34%"},
		   		{name:'action', index:'action', align:"right", sortable:false, hidden:(PAGE=="area"), width:"33%"}
		   	],
		   	cmTemplate: { title: false },
 		   	rowNum:MAX_ROW_NUM,
// 		   	rowList:[10,20,30],
// 		   	pager: '#groupPagingDiv',
		   	sortname: 'name',
		    viewrecords: true,
		    sortorder: "desc"    
		});

		forceFitGroupTableHeight();
		
		var mydata =  [];
		
		<c:forEach items="${groups}" var="group">
			var localData = new Object;
			localData.groupName = "${group.groupName}";
			localData.id =  "${group.id}";
			localData.action = "";
			<c:if test="${page == 'floor'}">
				localData.action += "<button onclick='javascript:parent.parent.assignGemsGroupToFixtures(null, \"" + "edit" + "\", " + ${group.id} + ", \"" + "${group.groupName}" + "\");'>Edit</button>";
			</c:if>
			localData.action += "&nbsp;<button onclick=\"javascript: deleteGroup(${group.id}, '${group.groupName}');\">Delete</button>";
								
			mydata.push(localData);
		</c:forEach>
		
		if(mydata)
		{
			//alert("mydata.length : "+mydata.length);
			for(var i=0;i<mydata.length;i++)
			{
				jQuery("#groupTable").jqGrid('addRowData',mydata[i].id,mydata[i]);
			}
		}

		jQuery("#groupTable").jqGrid('navGrid',"#groupPagingDiv",{edit:false,add:false,del:false});
			
	});
	
	function deleteGroup(groupID, groupName){
		var proceed = confirm("Are you sure you want to delete the group: "+groupName+"?");
		if(proceed==true) {
					
			$.ajax({
				type: 'POST',
				url: "${deleteGroupUrl}"+groupID+"?ts="+new Date().getTime(),
				beforeSend: function() {
				     $("#deleteScheduledMsg").html("Group deletion activity is scheduled. This page will be automatically refreshed. Please wait..");
				 },
				success: function(data){
 					//alert("Group deleted successfully.");
					reloadGroupsListFrame();
					//parent.parent.getFloorPlanObj("floorplan").plotChartRefresh();
				},
				dataType : "json",
				contentType : "application/xml; charset=utf-8"
			});
	 	}
	}
	
	function forceFitGroupTableHeight(){
		var jgrid = jQuery("#groupTable");
		var containerHeight = $(this).height();
		var otherElementHeight = $("#group-list-topPanel").height();
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .99)); 
	}
	
	function reloadGroupsListFrame(){
		var ifr = parent.document.getElementById('groupsFrame');
		window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = ifr.src;
	}	

	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#groupTable").setGridWidth($(window).width()-20);
	}).trigger('resize');
	
</script>
<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}
</style>
<table id="groupTable"></table>
<div id="groupPagingDiv"></div>