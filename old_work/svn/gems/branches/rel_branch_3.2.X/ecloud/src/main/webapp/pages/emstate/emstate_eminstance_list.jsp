<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<spring:url value="/services/org/emstate/getEmStateListByEmInstanceId/" var="getEmStateListByEmInstanceId" scope="request" />

<style>
#divform{width:100%;}
#form1{float:left;}
#btn1{float:left;}
#btn2{float:left;}
 

</style>

<script type="text/javascript">

var hideLogColumn = false;

$(document).ready(function() {
    //clearLabelMessage();
	
	<security:authorize access="hasAnyRole('ThirdPartySupportAdmin','SPPA')">
		hideLogColumn = true;
	</security:authorize>
	
	start(1, "desc");
	$("#emStateTable").setGridWidth($(window).width() - 25);
	
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
	   $('#' + "emStateTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "emStateTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "emStateTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#emStateTable").setGridWidth($(window).width()-25);	
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
		jQuery("#emStateTable").jqGrid({
			url: "${getEmStateListByEmInstanceId}"+"${emInstanceId}"+"?ts="+new Date().getTime(),
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
			colNames: ["id", "EM Id", "Set Time (UTC)","EM Status","Data Base Status", "Failed Attempts","Log"],
		       colModel: [
		       { name:'id', index:'id', hidden: true},
		       { name: "emInstanceId", index: 'emInstanceId',hidden: true },
		       { name: "utcSetTime", index: 'utcSetTime', sortable:true,width:'20%' },
		       { name: "emStatus", index: 'emStatus', sortable:false,width:'20%' },
		       { name: "databaseState", index: 'databaseState', sortable:false,width:'20%' },
		       { name: "failedattempts", index: 'failedattempts', sortable:false,width:'20%' },
		       { name:'log',index:'log', align:"center", hidden:(hideLogColumn == true) , sortable:false, width:"20%"}],
		       
		   	jsonReader: { 
				root:"emState", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    pager: '#emStatePagingDiv',
		   	page: pageNum,
		   	sortorder: orderWay,
		   	sortname: "setTime",
		    hidegrid: false,
		    viewrecords: true,
		   	loadui: "block",
		   	toolbar: [false,"top"],
		   	onSortCol: function(index, iCol, sortOrder) {
		   	},
		   	loadComplete: function(data) {
		   		if (data != null){
		   			if (data.emState!= undefined) {
				   		if (data.emState.length == undefined) {
				   			jQuery("#emStateTable").jqGrid('addRowData', 0, data.emState);
				   		}
				   	}
		   		}
		   		
		   		ModifyGridDefaultStyles();
		   		
		   	}

		});
		
		jQuery("#emStateTable").jqGrid('navGrid',"#emStatePagingDiv",
										{edit:false,add:false,del:false,search:false}, 
										{}, 
										{}, 
										{}, 
										{},
										{});
		
		//forceFitEmStateTableWidth();
		forceFitStateTableHeight();
	}

	function viewLogFormatter(cellvalue, options, rowObject) {
		var rowId = rowObject.id;
		var logMessage = rowObject.log;
		return '<a onclick="alert('+logMessage+');" style="color:003300;">' + "viewlog" + '</>';
	}
	
	function showLogView(rowId,logMessage) {
		alert(logMessage);
	}

	function forceFitEmStateTableWidth(){
		var jgrid = jQuery("#emStateTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#emStatePagingDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 10);
		
		$("#emStateTable").setGridWidth($(window).width() - 25);
	}
	
	function forceFitStateTableHeight(){		
		var a = parent.document.body.clientHeight;  			
		var jgrid = jQuery("#emStateTable");
		var containerHeight =a;//$(window).height();		
		var otherElementHeight = $("#outerDiv").height();		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();		
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();		
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;		
		var mHeight = containerHeight - otherElementHeight - gridHeaderFooterHeight;		
		jgrid.jqGrid("setGridHeight", Math.floor((mHeight) * .73)); 
	}


//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	$("#emStateTable").setGridWidth($(window).width()-25);
}).trigger('resize');


</script>

<div style="width: 100%;">
	<div id="outerDiv">
		<div style="font-weight: bolder; ">State For EM Instance: ${emInstanceName}</div>
	</div>
	<div style="overflow:auto">
		<table id="emStateTable"></table>
		<div id="emStatePagingDiv"></div>
	</div>
 </div>