<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/events/filter.ems" var="eventsFilterUrl" scope="request" />
<spring:url value="/scripts/jquery/jquery.datetime.picker.0.9.9.js" var="jquerydatetimepicker"></spring:url>
<script type="text/javascript" src="${jquerydatetimepicker}"></script>
<spring:url value="/scripts/jquery/jquery.dualListBox-1.3.min.js" var="jqueryduallistbox"></spring:url>
<script type="text/javascript" src="${jqueryduallistbox}"></script>

<spring:url value="/scripts/jquery/jquery.alerts.js"
	var="jqueryalerts"></spring:url>
<script type="text/javascript" src="${jqueryalerts}"></script>

<spring:url value="/themes/standard/css/jquery/jquery.alerts.css"
	var="jqueryalertscss" />
<link rel="stylesheet" type="text/css" href="${jqueryalertscss}" />

<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; }
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
</style>

<script type="text/javascript">

		var url="";
		var selectedEvents = [];
		var postData;
		var toggle=true;
		$(document).ready(function() {
			url='<spring:url value="/services/events/EmEventList"/>';
			start('1#####Critical,Major##END', 1, "eventTime", "desc");
			loadEventFilterAccordian();
		});
		
		
		function start(inputdata, pageNum, orderBy, orderWay) {
			jQuery("#eventsTable").jqGrid({
				url: url,
				userData: "userdata",
				mtype: "POST",
				postData: {"userData": inputdata},
				datatype: "json",
				autoencode: true,
				autowidth: true,
				hoverrows: false,
				scrollOffset: 0,
				rowNum:100,
				forceFit: true,
			   	colNames:["Time","Event Type", "Device", "Severity", "Desciption"],
			   	colModel:[
			   		{name:'eventTime',index:'eventTime', sorttype:"string", width:"20%"},
			   		{name:'eventType',index:'eventType', sorttype:"string", width:"15%"},
			   		{name:'name', index:'name',  sorttype:"string", width:"15%"},
			   		{name:'severity',index:'severity', sorttype:"string", width:"10%"},
			   		{name:'description',index:'description', sorttype:"string", width:"40%"}
			   	],
			   	pager: '#eventsDiv',
			   	page: pageNum,
			   	sortorder: orderWay,
			   	sortname: orderBy,
			    hidegrid: false,
			    viewrecords: true,
			   	loadui: "block",
			   	toolbar: [true,"top"],
			   	onSortCol: function(index, iCol, sortOrder) {
			   		$('#orderWay').attr('value', sortOrder);
			   		$('#orderBy').attr('value', index);
			   	},
			
			   	loadComplete: function() {
			   		selectedEvents = [];
			   		var query;
			   		jQuery("#eventsTable").jqGrid('setGridParam',{postData: jQuery("#eventsTable").jqGrid('getGridParam', 'userData')});
			   		$.each(jQuery("#eventsTable").jqGrid('getGridParam', 'postData'), function(key,val)  {
						if(key == 'userData') {
							query = encodeURIComponent(val);
							postData = val;
							var splitData = val.split("#");
							if (splitData.length > 0) {
								var resolved = splitData[0];
								if(toggle == false) {
									$("#includeResolved").attr('checked', 'checked');
								}
								else {
									$("#includeResolved").attr('checked', false);
								}
							}
						}
					});
					ModifyGridDefaultStyles();
					$('#exportInput').attr('value', query);
					
			   	}
			});
			
			jQuery("#eventsTable").jqGrid('navGrid',"#eventsDiv",{edit:false,add:false,del:false,search:false});

		}
	
		function loadEventFilterAccordian(){
			$("#t_eventsTable").css("padding", "3px 0px 10px 7px");
			$("#t_eventsTable").css("width", $('#t_eventsTable').width() - 10);
			//$("#t_eventsTable").append("<label>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</label>");
			//$("#t_eventsTable").append("<input type='checkbox' id='includeResolved' onclick='showOrHideEMEvent(this.checked)'/>");
			$("#t_eventsTable").append("<label>&nbsp;EM Events</label>");
			//$("#t_eventsTable").append("<label style='color: #000000;'><spring:message code='eventsAndFault.showUEMEvent'/></label>");
			//$("#t_eventsTable").append("<label>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</label>");
			//$("#t_eventsTable").append("<label style='color: #000000;'><spring:message code='eventsAndFault.export'/></label>");
			//$("#t_eventsTable").append("<label>&nbsp;</label>");
			//$("#t_eventsTable").append("<label style='color: #0000FF; solid #0000FF; cursor:pointer;' onclick='exportCSV()'>CSV</label>");
			$("#t_eventsTable").append("<form id='exportForm' action=<spring:url value='/services/events/list/getexportdata'/> method='POST'> <input type='hidden' id='exportInput' name='exportInput'></input>" + 
					"<input type='hidden' id='orderWay' name='orderWay'></input>" +
					"<input type='hidden' id='orderBy' name='orderBy'></input>" + "</form>");
			$("#t_eventsTable").after('<div style="padding: 0px 0px 3px 0px" id="filteraccordion"><h2><a href="#"><spring:message code="eventsAndFault.filter"/></a></h2><div id="filterContent"></div></div>');
			
			forceFitEventTableWidth();
			
			
			$("#filteraccordion").accordion({
				autoHeight: false,
				collapsible: true,
				active: false,
				change: function(event, ui) {
							forceFitEventTableWidth();						
						}
				});
					
			$("#filterContent").load("${eventsFilterUrl}"+"?ts="+new Date().getTime());
			
		}
		
		function forceFitEventTableWidth(){
			var jgrid = jQuery("#eventsTable");
			var containerHeight = $("body").height();
			
			var gridTotalHeight = jgrid.parents("div.ui-jqgrid:first").height();
			var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
			var gridHeaderFooterHeight = gridTotalHeight - gridBodyHeight;
			
			//t_eventsTable padding + other paddings and margins.
			var pads = 17;
			jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - gridHeaderFooterHeight  - pads) * .99)); 
		}
		
		$(function() {
			$(window).resize(function() {
				forceFitEventTableWidth();
			});
		});
		
		function showOrHideEMEvent(selected) {
			var splitData = postData.split("#");
			if (splitData.length > 0) {
				if(selected) {
					toggle=false;
					url='<spring:url value="/services/events/list/getdata"/>';
					postData = "1" + "#" + splitData[1] + "#" + splitData[2] + "#" + splitData[3] + "#"
					+ splitData[4] + "#" + splitData[5] + "#" + splitData[6] + "#" + splitData[7];
									}
				else {
					toggle=true;
					url='<spring:url value="/services/events/EmEventList"/>';
					postData = "-1" + "#" + splitData[1] + "#" + splitData[2] + "#" + splitData[3] + "#"
					+ splitData[4] + "#" + "Critical,Major" + "#" + splitData[6] + "#" + splitData[7];

				}
			}
			var pageNo = jQuery("#eventsTable").jqGrid('getGridParam', 'page');
			var sname = jQuery("#eventsTable").jqGrid('getGridParam', 'sortname');
			var sorder = jQuery("#eventsTable").jqGrid('getGridParam', 'sortorder');
			
			//$("#eventsTable").jqGrid("GridUnload");
			$("#eventsTable").jqGrid("clearGridData");
			jQuery("#eventsTable").jqGrid('setGridParam',{url:url,postData: {"userData": postData},orderBy:sname,orderWay:sorder,page:pageNo}).trigger("reloadGrid");
			//start(postData, 1, sname, sorder);
		}
		function exportCSV() {
			$('#exportForm').submit();
		}
		//this is to resize the jqGrid on resize of browser window.
		$(window).bind('resize', function() {
			$("#eventsTable").setGridWidth($(window).width()-20);
		}).trigger('resize');
		
		
		function ModifyGridDefaultStyles() {  
			   $('#' + "eventsTable" + ' tr').removeClass("ui-widget-content");
			   $('#' + "eventsTable" + ' tr:nth-child(even)').addClass("evenTableRow");
			   $('#' + "eventsTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
		}
		
</script>

<table id="eventsTable"></table>
<div id="eventsDiv"></div>