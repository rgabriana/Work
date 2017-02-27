<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/events/view.ems" var="eventsViewUrl" scope="request" />


<script type="text/javascript">
<!--
    $(document).ready(function() {
            jQuery("#dataList").jqGrid({
            	datatype: "local",
               	colNames:[
               	       <c:forEach items="${metadataSet.columnNames}" var="columnName" varStatus="rowCounter">
                       '${columnName}'
                       <c:if test="${rowCounter.count != metadataSet.columnCount}">,</c:if>
               </c:forEach>
               	],
               	colModel:[
                    <c:forEach items="${metadataSet.columnNames}" var="columnName" varStatus="rowCounter">{name:'${columnName}',index:'${columnName}', width:60, sorttype:"string"}
                      <c:if test="${rowCounter.count != metadataSet.columnCount}">,</c:if>
                    </c:forEach>
               	],
               	caption: "Data"
            });
            
            var mydata = [
               <c:forEach items="${dataSet}" var="dataRow" varStatus="rowCounter">
                 {
                 <c:forEach items="${dataRow}" var="data" varStatus="dataCounter">
                 <c:out value='${metadataSet.columnNames[dataCounter.count-1]}'/>:"${data}"
                       <c:if test="${dataCounter.count != metadataSet.columnCount}">,</c:if>
                 </c:forEach>
                  }
                     <c:if test="${rowCounter.count != noOfRows}">,</c:if>
                </c:forEach>
 
            		];
           for(var i=0;i<=mydata.length;i++){
            	jQuery("#dataList").jqGrid('addRowData',i+1,mydata[i]);
            }

            });
//-->
</script>

<spring:url value="/debug/sqlconsole.ems" var="sqlConsoleUrl" />
<form id="sqlConsoleForm" action="${sqlConsoleUrl}" method="post">
	<input type="text" id="sqlString" name="sqlString" size="150" /> <input
		type="submit" value="Show Data" />
</form>

<table id="dataList"></table>

