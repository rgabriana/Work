<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<spring:url value="/services/org/emtaskmanagement/loadEmTasksByEmInstanceId/" var="getEmTaskListByEmInstanceId" scope="request" />

<spring:url value="/eminstance/scheduleEmUpgrade.ems" var="createEmTaskUrl" />
<spring:url value="/eminstance/scheduleLogTask.ems" var="createEmLogTaskUrl" />

<style>
#divform{width:100%;}
#form1{float:left;}
#btn1{float:left;}
#btn2{float:left;}
 

</style>

<script type="text/javascript">

$(document).ready(function() {
    //clearLabelMessage();
	start(1, "desc");
	$("#emTasksTable").setGridWidth($(window).width() - 25);
	
	$('#newEmTaskButton').click(function() {
		//clearLabelMessage();
	    $("#newEmTaskDialog").load("${createEmTaskUrl}?emInstanceId="+"${emInstanceId}"+"&ts="+new Date().getTime()).dialog({
	        title : "New EM Upgrade Task",
	        width :  Math.floor($('body').width() * .30),
	        minHeight : 250,
	        position : ['top',50],
	        modal : true
	    });
	    return false;
	});
	$('#newEmLogTaskButton').click(function() {
		//clearLabelMessage();
	    $("#newEmLogTaskDialog").load("${createEmLogTaskUrl}?emInstanceId="+"${emInstanceId}"+"&ts="+new Date().getTime()).dialog({
	        title : "New EM Log Upload Task",
	        width :  Math.floor($('body').width() * .30),
	        minHeight : 250,
	        position : ['top',50],
	        modal : true
	    });
	    return false;
	});
});



function getParameterByName(name) 
{ 
  name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]"); 
  var regexS = "[\\?&]" + name + "=([^&#]*)"; 
  var regex = new RegExp(regexS); 
  var results = regex.exec(window.location.search); 
  if(results == null) 
    return ""; 
  else 
    return decodeURIComponent(results[1].replace(/\+/g, " ")); 
} 



function ModifyGridDefaultStyles() {  
	   $('#' + "emTasksTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "emTasksTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "emTasksTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#emTasksTable").setGridWidth($(window).width()-25);	
}

function displayLabelMessage(Message, Color) {
		$("#message").html(Message);
		$("#message").css("color", Color);
}

function clearLabelMessage(Message, Color) {
		displayLabelMessage("", "black");
}

//function for pagination
function start(pageNum, orderWay) {
		jQuery("#emTasksTable").jqGrid({
			url: "${getEmTaskListByEmInstanceId}"+"${emInstanceId}"+"?ts="+new Date().getTime(),
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
			colNames: ["id", "EM Id", "Task Code", "Task Status","Progress Status","priority","Start Time","Offset Time","No.of attempts"],
		       colModel: [
		       { name:'id', index:'id', hidden: true},
		       { name: "emInstanceId", index: 'emInstanceId',hidden: true },
		       { name: "taskCode", index: 'taskCode', sortable:false,width:'12%' },
		       { name: 'taskStatus', index: 'taskStatus',sorttype:'string',width:'10%' },
		       { name: "progressStatus", index: 'progressStatus', sortable:false,width:'12%' },
		       { name: "priority", index: 'priority', sortable:false,width:'12%' },
		       { name: "utcStartTime", index: 'utcStartTime', sortable:false,width:'8%' },
		       { name: "offsetTime", index: 'offsetTime',sortable:false,width:'15%'},
		       { name: "numberOfAttempts", index: 'numberOfAttempts',sortable:false,width:'10%'}],
		       
		   	jsonReader: { 
				root:"emTasks", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    pager: '#emTasksPagingDiv',
		   	page: pageNum,
		   	sortorder: orderWay,
		   	sortname: "startTime",
		    hidegrid: false,
		    viewrecords: true,
		   	loadui: "block",
		   	toolbar: [false,"top"],
		   	onSortCol: function(index, iCol, sortOrder) {
		   	},
		   	loadComplete: function(data) {
		   		if (data != null){
		   			if (data.emTasks!= undefined) {
				   		if (data.emTasks.length == undefined) {
				   			jQuery("#emTasksTable").jqGrid('addRowData', 0, data.emTasks);
				   		}
				   	}
		   		}
		   		
		   		ModifyGridDefaultStyles();
		   		
		   	}

		});
		
		jQuery("#emTasksTable").jqGrid('navGrid',"#emTasksPagingDiv",
										{edit:false,add:false,del:false,search:false}, 
										{}, 
										{}, 
										{}, 
										{},
										{});
		
		//forceFitEmTasksTableWidth();
		forceFitTasksTableHeight();
	}

	function forceFitEmTasksTableWidth(){
		var jgrid = jQuery("#emTasksTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#emTasksPagingDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 10);
		
		$("#emTasksTable").setGridWidth($(window).width() - 25);
	}
	
	function forceFitTasksTableHeight(){		
		var a = parent.document.body.clientHeight;  			
		var jgrid = jQuery("#emTasksTable");
		var containerHeight =a;//$(window).height();		
		var otherElementHeight = $("#outerDiv").height();		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();		
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();		
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;		
		var mHeight = containerHeight - otherElementHeight - gridHeaderFooterHeight;		
		jgrid.jqGrid("setGridHeight", Math.floor((mHeight) * .71)); 
	}


//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	$("#emTasksTable").setGridWidth($(window).width()-25);
}).trigger('resize');


</script>

<div id="newEmTaskDialog"></div>
<div id="newEmLogTaskDialog"></div>

<div style="width: 100%;">
	<div id="outerDiv">
		<div style="font-weight: bolder; ">Tasks For EM Instance: ${emInstanceName}</div>
		<security:authorize access="hasAnyRole('Admin','SystemAdmin','SupportAdmin')">
		<button id="newEmTaskButton">Add Upgrade Task</button>	
		</security:authorize>
		<security:authorize access="hasAnyRole('Admin','SystemAdmin','SupportAdmin','ThirdPartySupportAdmin')">
		<button id="newEmLogTaskButton">Add Log Upload Task </button>
		</security:authorize>
		<br>
    </div>
  	<div style="overflow:auto">
<table id="emTasksTable"></table>
		<div id="emTasksPagingDiv"></div>
	</div>
 </div>