<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/events/filter.ems" var="eventsFilterUrl" scope="request" />
<spring:url value="/events/resolve/comment.ems" var="resolveEventsDialog" scope="request" />

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

		var dialogResult;

		var selectedEvents = [];
		var postData;
		var profilenodetype;
	    var profilenodeid;
	    
		$(document).ready(function() {
			if(parent.accTabSelected == 'pf')
			{
				getProfileData();
				var userdata=  "1##"+ profilenodeid +"#####END";
				start(userdata, 1, "", "asc");
			}
			else
			start('1#######END', 1, "eventTime", "desc");
		});
		
		var getProfileData = function() 
		{
			var initialSelectedNode = $.cookie('jstree_profile_select');
		    if(initialSelectedNode){
		        initialSelectedNode = initialSelectedNode.replace("#","");  
		    }
	        else {
		        initialSelectedNode = "group_1"; 
		        $.cookie('jstree_profile_select', "#" + initialSelectedNode,  { path: '/' });
		    }
		    var nodeDetails=$(initialSelectedNode.split('_'));				
		    profilenodetype = nodeDetails[0];
		    profilenodeid = nodeDetails[1];		  
		}
		function start(inputdata, pageNum, orderBy, orderWay) {
			jQuery("#eventsTable").jqGrid({
				url: '<spring:url value="/services/events/list/getdata"/>',
				userData: "userdata",
				mtype: "POST",
				postData: {"userData": inputdata},
				datatype: "json",
				autoencode: true,
				autowidth: true,
				scrollOffset: 0,
				forceFit: true,
			   	colNames:["<spring:message code='eventsAndFault.time'/>","<spring:message code='eventsAndFault.location'/>", "<spring:message code='eventsAndFault.eventType'/>", "<spring:message code='eventsAndFault.severity'/>", "<spring:message code='eventsAndFault.description'/>", "<spring:message code='eventsAndFault.details'/>", "<spring:message code='eventsAndFault.resolve'/>"],
			   	colModel:[
			   		{name:'eventTime',index:'eventTime', sorttype:"string", width:"13%"},
			   		{name:'location',index:'location', sortable:false, width:"28%"},
			   		{name:'eventType',index:'eventType', sorttype:"string", width:"10%"},
			   		{name:'severity',index:'severity', sorttype:"string", width:"6%"},
			   		{name:'description',index:'description', sorttype:"string", width:"31%"},		
			   		{name:'details',index:'details', align:"center", sortable:false, width:"6%", formatter: viewActionFormatter},
			   		{name:'resolved',index:'resolved', align:"center", sortable:false, width:"6%", formatter: cboxFormatter, formatoptions:{disabled:false}}
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
			   		jQuery("#eventsTable").jqGrid('setGridParam',{postData: jQuery("#eventsTable").jqGrid('getGridParam', 'userData')});
					var query;
					$.each(jQuery("#eventsTable").jqGrid('getGridParam', 'postData'), function(key,val)  {
						if(key == 'userData') {
							query = encodeURIComponent(val);
							postData = val;
							var splitData = val.split("#");
							if (splitData.length > 0) {
								var resolved = splitData[0];
								if(resolved == "-1") {
									$("#includeResolved").attr('checked', 'checked');
								}
								else if(resolved == "0") {
									$("#includeResolved").attr('disabled', 'disabled');
								}
							}
						}
					});
					$('#exportInput').attr('value', query);
			   	}
			});
			
			jQuery("#eventsTable").jqGrid('navGrid',"#eventsDiv",{edit:false,add:false,del:false,search:false});

			$("#t_eventsTable").css("padding", "3px 0px 10px 7px");
			$("#t_eventsTable").css("width", $('#t_eventsTable').width() - 10);
			$("#t_eventsTable").append("<input type='button' value=\"<spring:message code='eventsAndFault.markResolved'/>\" onclick='markResolved()'/>");
			$("#t_eventsTable").append("<label>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</label>");
			$("#t_eventsTable").append("<input type='checkbox' id='includeResolved' onchange='showOrHideResolved(this.checked)'/>");
			$("#t_eventsTable").append("<label>&nbsp;</label>");
			$("#t_eventsTable").append("<label style='color: #000000;'><spring:message code='eventsAndFault.includeResolved'/></label>");
			$("#t_eventsTable").append("<label>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</label>");
			$("#t_eventsTable").append("<label style='color: #000000;'><spring:message code='eventsAndFault.export'/></label>");
			$("#t_eventsTable").append("<label>&nbsp;</label>");
			$("#t_eventsTable").append("<label style='color: #0000FF; solid #0000FF; cursor:pointer;' onclick='exportCSV()'>CSV</label>");
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
		
		function showView(id) {
			parent.parent.showEventFaultViewWindow(id);
		}
		
		function cboxFormatter(cellvalue, options, rowObject) {
			if(cellvalue == 'false') {
				return '<label style="color: #0000FF;"><spring:message code="eventsAndFault.resolved"/></label>';
			}
			else {
				return '<input type="checkbox" onchange="onResolvedChange(this.checked, ' + options.rowId + ')"/>';	
			}
		}
		
		function viewActionFormatter(cellvalue, options, rowObject) {
			return '<a class="gridLink" href="#" onclick="showView('+ options.rowId + '); return false;" >' + cellvalue + '<a/>';
		}
		
		function onResolvedChange(selected, id) {
			if(selected)
				selectedEvents.push(id);
			else {
				for(var i=0; i<selectedEvents.length; i++) {
					if(selectedEvents[i] == id)	{
						selectedEvents.splice(i,1); 
					}
				}
			}
		}
		
		function markResolved()	{
			if(selectedEvents.length > 0) {
				dialogResult = "0";
				$("#commentDialog").load("${resolveEventsDialog}"+"?ids="+ encodeURIComponent(selectedEvents), function() {
					$("#commentDialog").dialog({
						modal:true,
						title: 'Resolve Comment',
						width : Math.floor($('body').width() * .40),
						height : 175,
						close: function(event, ui) {
							if(dialogResult == "S") {
								selectedEvents.splice(0,selectedEvents.length);
								var pageNo = jQuery("#eventsTable").jqGrid('getGridParam', 'page');
								var sname = jQuery("#eventsTable").jqGrid('getGridParam', 'sortname');
								var sorder = jQuery("#eventsTable").jqGrid('getGridParam', 'sortorder');
								$("#eventsTable").jqGrid("GridUnload");
								start(postData, pageNo, sname, sorder);
							}
							else if(dialogResult == "E") {
								jAlert('Some unexpected internal error occurred. Please try again.');
							}
						}
					});
				});
			}
			else {
				jAlert('Please check atleast one event.');
			}
		}
		
		function showOrHideResolved(selected) {
			var splitData = postData.split("#");
			if (splitData.length > 0) {
				if(selected) {
					postData = "-1" + "#" + splitData[1] + "#" + splitData[2] + "#" + splitData[3] + "#"
									+ splitData[4] + "#" + splitData[5] + "#" + splitData[6] + "#" + splitData[7];
				}
				else {
					postData = "1" + "#" + splitData[1] + "#" + splitData[2] + "#" + splitData[3] + "#"
					+ splitData[4] + "#" + splitData[5] + "#" + splitData[6] + "#" + splitData[7];
				}
			}
			var pageNo = jQuery("#eventsTable").jqGrid('getGridParam', 'page');
			var sname = jQuery("#eventsTable").jqGrid('getGridParam', 'sortname');
			var sorder = jQuery("#eventsTable").jqGrid('getGridParam', 'sortorder');
			$("#eventsTable").jqGrid("GridUnload");
			start(postData, 1, sname, sorder);

		}
		
		function exportCSV() {
			$('#exportForm').submit();
		}
		
		//this is to resize the jqGrid on resize of browser window.
		$(window).bind('resize', function() {
			$("#eventsTable").setGridWidth($(window).width()-20);
		}).trigger('resize');
		
		//This function will be called on close of the EventAndFaultView Popupwindow.
		function loadGridOnViewWindowClose()
		{
			if(dialogResult == "R") {
				var pageNo = jQuery("#eventsTable").jqGrid('getGridParam', 'page');
				var sname = jQuery("#eventsTable").jqGrid('getGridParam', 'sortname');
				var sorder = jQuery("#eventsTable").jqGrid('getGridParam', 'sortorder');
				$("#eventsTable").jqGrid("GridUnload");
				start(postData, pageNo, sname, sorder);
			}
			else if(dialogResult == "E") {
				jAlert('Some unexpected internal error occurred. Please try again.');
			}
		}
</script>

<table id="eventsTable"></table>
<div id="eventsDiv"></div>
<div id="commentDialog"></div>