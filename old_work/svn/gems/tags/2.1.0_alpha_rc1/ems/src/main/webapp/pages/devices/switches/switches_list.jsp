<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<div id="SwitchesDialog"></div>

<div id="switch-list-topPanel" style="background: #fff">
	<c:if test="${page == 'floor'}">
		<button id="newSwitch" onclick="javascript:parent.parent.showSwitchForm();">Create Switch</button>
		<div style="height:10px;"></div>
	</c:if>
</div>

<script type="text/javascript">
var PAGE = "${page}";

<spring:url value="/services/org/switch/delete/" var="deleteSwitchUrl" scope="request" />

	$(document).ready(function() {
		
		jQuery("#switchTable").jqGrid({
			datatype: "local",
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
		   	colNames:['Name', 'Action'],
		   	colModel:[
		   		{name:'name', index:'name', sorttype:"string", width:"34%"},
		   		{name:'action', index:'action', align:"right", sortable:false, hidden:(PAGE=="area"), width:"33%"}
		   	],
		   	cmTemplate: { title: false },
// 		   	rowNum:30,
// 		   	rowList:[10,20,30],
// 		   	pager: '#switchPagingDiv',
		   	sortname: 'name',
		    viewrecords: true,
		    sortorder: "desc"    
		});

		forceFitSwitchTableHeight();
		
		var mydata =  [];
		
		<c:forEach items="${switches}" var="switch">
			var localData = new Object;
			localData.name = "${switch.name}";
			localData.floor = "${switch.floorId}";
			localData.action = "";
			<c:if test="${page == 'floor'}">
				localData.action += "<button onclick=\"javascript: parent.parent.showSwitchForm(${switch.id});\">Edit</button>";
			</c:if>
			localData.action += "&nbsp;<button onclick=\"javascript: deleteSwitch(${switch.id}, '${switch.name}');\">Delete</button>";
								
			mydata.push(localData);
		</c:forEach>
		
		for(var i=0;i<=mydata.length;i++)
		{
			jQuery("#switchTable").jqGrid('addRowData',i+1,mydata[i]);
		}

		jQuery("#switchTable").jqGrid('navGrid',"#switchPagingDiv",{edit:false,add:false,del:false});
			
	});
	
	function forceFitSwitchTableHeight(){
		var jgrid = jQuery("#switchTable");
		var containerHeight = $(this).height();
		var otherElementHeight = $("#switch-list-topPanel").height();
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .99)); 
	}
	
	function deleteSwitch(switchID, switchName){
		var proceed = confirm("<spring:message code='switchForm.message.validation.deleteConfirmation'/>: "+switchName+"?");
		if(proceed==true) {
			$.ajax({
				url: "${deleteSwitchUrl}"+switchID+"?ts="+new Date().getTime(),
				success: function(data){
// 					alert("Switch deleted successfully.");
					reloadSwitchesListFrame();
				}
			});
	 	}
	}
	
	function reloadSwitchesListFrame(){
		var ifr = parent.document.getElementById('switchesFrame');
		window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = ifr.src;
	}
</script>
<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}
</style>
<table id="switchTable"></table>
<div id="switchPagingDiv"></div>