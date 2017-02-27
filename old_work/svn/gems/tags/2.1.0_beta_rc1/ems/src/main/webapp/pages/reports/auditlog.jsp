<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<spring:url value="/reports/auditfilter.ems" var="auditsFilterUrl" scope="request" />

<spring:url value="/scripts/jquery/jquery.datetime.picker.0.9.9.js" var="jquerydatetimepicker"></spring:url>
<script type="text/javascript" src="${jquerydatetimepicker}"></script>
<spring:url value="/scripts/jquery/jquery.dualListBox-1.3.min.js" var="jqueryduallistbox"></spring:url>
<script type="text/javascript" src="${jqueryduallistbox}"></script>

<style type="text/css">html {height:100% !important;}</style>

<script type="text/javascript">
	
	var postData;
			
	$(document).ready(function() {
		start('1#####END', 1, "logTime", "desc");
		var jgrid = jQuery("#auditsTable");
		jgrid.jqGrid("setGridWidth", $('#filteraccordion').width() );
	});
	
	function start(inputdata, pageNum, orderBy, orderWay) {
		jQuery("#auditsTable").jqGrid({
			url: '<spring:url value="/services/audits/getdata"/>',
			userData: "userdata",
			mtype: "POST",
			postData: {"userData": inputdata},
			datatype: "json",
			autoencode: true,
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
		   	colNames:["<spring:message code='audit.time'/>","<spring:message code='audit.username'/>", "<spring:message code='audit.action'/>", "<spring:message code='audit.description'/>"],
		   	colModel:[
		   		{name:'logTime',index:'logTime', sorttype:"string", width:"13%"},
		   		{name:'username',index:'username', sorttype:"string", width:"25%"},
		   		{name:'actionType',index:'actionType', sorttype:"string", width:"15%"},
		   		{name:'description',index:'description', sortable:false, width:"47%"}
		   	],
		   	pager: '#auditsDiv',
		   	page: pageNum,
		   	sortorder: orderWay,
		   	sortname: orderBy,
		    hidegrid: false,
		    viewrecords: true,
		   	loadui: "block",
		   	loadComplete: function() {
		   		jQuery("#auditsTable").jqGrid('setGridParam',{postData: jQuery("#auditsTable").jqGrid('getGridParam', 'userData')});
				$.each(jQuery("#auditsTable").jqGrid('getGridParam', 'postData'), function(key,val)  {
					if(key == 'userData') {
						postData = val;
					}
				});
		   	}
		});
		
		jQuery("#auditsTable").jqGrid('navGrid',"#auditDiv",{edit:false,add:false,del:false,search:false});

		forceFitTableWidth();

		$("#filteraccordion").accordion({
			autoHeight: false,
			collapsible: true,
			active: false,
			change: function(event, ui) {
						forceFitTableWidth();					
					}
		}); 
				
		$("#filterContent").load("${auditsFilterUrl}"+"?ts="+new Date().getTime()); 
		
		$("div.ui-jqgrid-sortable").css("border-left", "1px solid white");
	}
	
	function forceFitTableWidth(){
		var jgrid = jQuery("#auditsTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#auditsDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 10);
		
		$("#auditsTable").setGridWidth($(window).width() - 25);
	}
	
	/*$(function() {
		$(window).resize(function() {
			forceFitTableWidth();
		});
	}); */
	
	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		forceFitTableWidth();
	}).trigger('resize');
	
</script>
<div style="width: 100%;">
	<div id="outerDiv">
		<div style="padding: 5px 0px 5px 5px;"><span style="font-weight: bolder; "><spring:message code="audit.header" /></span></div>
		<div style="padding: 0px 0px 3px 0px" id="filteraccordion">
			<h2><a href="#"><spring:message code="audit.filter"/></a></h2>
			<div id="filterContent"></div>
		</div>
	</div>
	<div style="overflow: auto">
		<table id="auditsTable"></table>
		<div id="auditsDiv"></div>
	</div>
 </div>