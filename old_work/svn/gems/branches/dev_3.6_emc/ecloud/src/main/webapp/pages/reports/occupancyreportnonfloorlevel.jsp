<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<spring:url value="/services/occupancyreportservice/loadfacilityoccupancy/" var="loadFacilityOccReport"/>
<spring:url value="/services/occupancyreportservice/loadreportofchild/" var="loadChildLevelOccReport"/>
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

<script type="text/javascript">
$(document).ready(function() {
	generateLeastMostOccupiedTable();
});
</script>	


</head>

<body>
	<input type="hidden" id="baseUrlChildFacilityOccReport" name="baseUrlChildFacilityOccReport" value="${loadChildLevelOccReport}" />
	<input type="hidden" id="baseUrlFacilityOccReport" name="baseUrlFacilityOccReport" value="${loadFacilityOccReport}" />
	<input type="hidden" id="customerId" name="customerId" value="${customerId}" />
	<input type="hidden" id="otreenodetype" name="otreenodetype" value="${otreenodetype}" />
	<input type="hidden" id="otreenodeid" name="otreenodeid" value="${otreenodeid}" />
    <div style="padding-top: 2px;font-weight: bold;"><span id="nonFloorbreadscrumHeader"></span></div>
    
    <div id="nonFloorwrapBarChart">
     <div id="nonFloorbarChart">
       
       </div>
     </div>
     
    <div id="nonFloorwrapDonutChart">
        <div id="nonFloorsensorDonutChart" > <svg ></svg>
        </div>
        <div id="nonFloortotalsqftDonutChart"><svg></svg>
        </div>
    </div> 
     
    <div id="nonFloorallStatTypes">
    </div>
      <div id="nonFloorallOccTypes">
    </div>
    
    <div id="nonFloorutilizationGraphTableDiv">
            <table id="nonFloorutilizationGraphTable">
            </table>
    </div>
    
    <div style="font-weight:bold;float: left;text-align: center;"><span id="tableTitle"></span>
     	<table id="mostOccTable">
        </table>
    </div>
    
    </body>
</html>