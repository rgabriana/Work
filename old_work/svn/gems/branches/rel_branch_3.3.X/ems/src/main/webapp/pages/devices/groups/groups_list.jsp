<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<div id="GroupsDialog"></div>

<div id="group-list-topPanel" style="background: #fff">
	<c:if test="${page == 'floor'}">
		<div style="display: inline;"><button id="newGroupflow" onclick="javascript:parent.parent.showGroupPrompt();">Create</button></div>	
	</c:if>
	<div style="display: inline;"><span style="font-weight:bold;" id="deleteScheduledMsg"></span></div>
	<div style="height:5px;"></div>

</div>

<script type="text/javascript">
var PAGE = "${page}";
var MAX_ROW_NUM = 99999;
<spring:url value="/services/org/gemsgroups/op/deletegroup/gtype/2/groupid/" var="deleteGroupUrl" scope="request" />
	$(document).ready(function() {
		
		jQuery("#groupTable").jqGrid({
			datatype: "local",
			autowidth: true,
			scrollOffset: 0,
			hoverrows: false,
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
		    sortorder: 'asc',
		    loadComplete: function() {
		    	 ModifyGridDefaultStyles();
		    }    
		});

		forceFitGroupTableHeight();
		
		var mydata =  [];
		
		<c:forEach items="${groups}" var="group">
			var localData = new Object;
			localData.groupName = '<c:out value="${group.groupName}" escapeXml="true" />';
			localData.id =  "${group.id}";
			localData.action = "";
			<c:if test="${page == 'floor'}">
			localData.action += "<button onclick=\"javascript: parent.parent.showGroupWidgetDialog(${group.id});\">Edit</button>";
			</c:if>
			localData.action += "&nbsp;<button onclick=\"javascript: deleteGroup(${group.id});\">Delete</button>";
								
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
		
		$("#groupTable").jqGrid().setGridParam({sortname: 'groupName', sortorder:'asc'}).trigger("reloadGrid");
			
	});
	
	function forceDeleteGroup(groupID)
	{
		$.ajax({
			type: 'POST',
			url: "${deleteGroupUrl}"+groupID+"/1?ts="+new Date().getTime(),
			beforeSend: function() {
			     $("#deleteScheduledMsg").html("Group deletion activity is scheduled. This page will be automatically refreshed. Please wait..");
			 },
			success: function(data){
					reloadGroupsListFrame();
			},
			dataType : "json",
			contentType : "application/xml; charset=utf-8"
		});						
	}
	
	function deleteGroup(groupID){
		var grpName = "";
		<c:forEach items="${groups}" var="grp">
			if("${grp.id}" == groupID)
				grpName = "${grp.groupName}";
		</c:forEach>
		var proceed = confirm("Are you sure you want to delete the group: "+grpName+"?");
		if(proceed==true) {
					
			$.ajax({
				type: 'POST',
				url: "${deleteGroupUrl}"+groupID+"/0?ts="+new Date().getTime(),
				beforeSend: function() {
				     $("#deleteScheduledMsg").html("Group deletion activity is scheduled. This page will be automatically refreshed. Please wait..");
				 },
				success: function(data){
 					if(data.status == 1)
 					{
						var forceDelete = confirm("Group cannot be deleted as some of the fixtures are not reachable and cannot be removed from the group. Do you still want to go ahead and delete the group?");
						
						if(forceDelete == true)	{
							forceDeleteGroup(groupID);
						}
						else
						{
						     $("#deleteScheduledMsg").html("");
						}
 					}
 					else
 					{
 						reloadGroupsListFrame();
 					}
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
		ifr.src = ifr.src + new Date().getTime();
	}
	
	function ModifyGridDefaultStyles() {  
		   $('#' + "groupTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "groupTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "groupTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
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