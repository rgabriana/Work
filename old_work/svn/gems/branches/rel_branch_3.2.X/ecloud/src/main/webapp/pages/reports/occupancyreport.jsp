<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<spring:url value="/services/occupancyreportservice/occupancychart/" var="loadFloorOccReport"/>
<html>
    <head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        
<spring:url value="/themes/default/css/enlighted-d3.css" var="enlightedd3css"></spring:url>
<link rel="stylesheet" type="text/css" href="${enlightedd3css}" />

<spring:url value="/scripts/jquery/enlighted-d3.js" var="enlightedd3js"></spring:url>
<script type="text/javascript" src="${enlightedd3js}"/></script>

<spring:url value="/scripts/jquery/jqgrid/grid.locale-en.js" var="gridlocaleenjs"></spring:url>
<script type="text/javascript" src="${gridlocaleenjs}"></script>

<spring:url value="/scripts/jquery/jqgrid/jquery.jqGrid.min.js" var="jqueryjqGridminjs"></spring:url>
<script type="text/javascript" src="${jqueryjqGridminjs}"></script>

<spring:url value="/scripts/jquery/d3/d3.min.js" var="d3js"></spring:url>
<script type="text/javascript" src="${d3js}" charset="utf-8"> </script>

<spring:url value="/scripts/jquery/nvd3/nv.d3.min.js" var="nvd3js"></spring:url>   
<script src="${nvd3js}"></script>

<spring:url value="/scripts/jquery/nvd3/nv.d3.css" var="nvd3css"></spring:url>
<link href="${nvd3css}" rel="stylesheet" type="text/css" />

<spring:url value="/scripts/c3/c3.min.js" var="c3js"></spring:url>   
<script src="${c3js}"></script>

<spring:url value="/scripts/c3/c3.css" var="c3css"></spring:url>
<link href="${c3css}" rel="stylesheet" type="text/css" />

<style type="text/css">

.ui-jqgrid .ui-jqgrid-bdiv {max-height:100px;}


</style>
<script type="text/javascript">
var lpath;
var otreenodetype;
var otreenodeid;
var getFacilityNodeDetails=function(name){
	var arr=$(name.split('_'));				
	return arr;				
}
	$(document).ready(function() {
		
		lpath=parent.path;
		$("#breadscrumHeader").text(lpath);
		
		var initialSelectedNode = $.cookie('uem_facilites_jstree_select',{ path: '/' });
		initialSelectedNode = initialSelectedNode.replace("#","");
		var nodeDetails = getFacilityNodeDetails(initialSelectedNode);
		otreenodetype = nodeDetails[0];
		otreenodeid = nodeDetails[1];
		$("#otreenodetype").val(otreenodetype);
		$("#otreenodeid").val(otreenodeid);
		console.log("facilityType:otreenodeid" + otreenodetype+":"+otreenodeid);
		drawUtilizationGrapth(800, 525);
		
		$(window).resize(function() {
			$("#utilizationGraphTable").setGridWidth($(window).width() - 25);
		});
		
	});
</script>	


</head>

<body>
	<input type="hidden" id="otreenodetype" name="otreenodetype" value="${otreenodetype}" />
	<input type="hidden" id="otreenodeid" name="otreenodeid" value="${otreenodeid}" />
	<input type="hidden" id="flooroccData" name="flooroccData" value="${loadFloorOccReport}${customerId}" />
    <div style="padding-top: 2px;font-weight: bold;"><span id="breadscrumHeader"></span></div>
    <div id="wrapBarChart">
        <div id="barChart">
            <svg>
                    
            </svg>
        </div>
     </div>
     
    <div id="wrapDonutChart">
        <div id="sensorDonutChart" > <svg ></svg>
        </div>
        <div id="totalsqftDonutChart"><svg></svg>
        </div>
    </div> 
     
<!--     <div id="allOccTypes"> -->
<!--     </div> -->
    
    <div id="allStatTypes">
    </div>
    
    <div id="utilizationGraphTableDiv">
            <table id="utilizationGraphTable" style="height: 100%">
            </table>
    </div>
    </body>
</html>