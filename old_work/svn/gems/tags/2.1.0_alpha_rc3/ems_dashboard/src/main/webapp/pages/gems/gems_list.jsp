<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
	
	.ui-dialog-title{font-size: 1.4em; font-weight: bolder;}
	
	table#all_gems_list td{overflow: hidden; !important;}
	
</style>
<div class="outermostdiv">
<spring:url value="/gems/" var="deleteGemsUrl" scope="request" />
<spring:url value="/gems/" var="activateGEM" scope="request" />
<spring:url value="/gems/" var="deactivateGEM" scope="request" />

<spring:url value="/gems/create.ems?" var="createGEMSUrl"	scope="request" />
<spring:url value="/gems/list.ems" var="gemLst" />	
		

<%   
   String sAction = request.getParameter("error");   
   if ( sAction != null) { %>   
   <script> alert("Error occured while saving VEM, Please enter unique IP/Name");</script>   
<% } %> 


	<script type="text/javascript">
		
        $(document).ready(function() {
            $('#addGEMButton').click(function() {
                $("#gemsDetailsDialog").load("${createGEMSUrl}"+"&ts="+new Date().getTime()).dialog({
                    title : "New VEM",
                    width :  Math.floor($('body').width() * .35),
                    minHeight : 150,
                    modal : true
                });
                return false;
            });
           
            $(window).load(function() {
                window.editGEMSDetails = function(url) {
                    $("#gemsDetailsDialog").load(url+"?ts="+new Date().getTime()).dialog({
                        title : "Edit VEM",
                        width :  Math.floor($('body').width() * .35),
                        minHeight : 150,
                        modal : true
                    });
                    return false;
                }
            });
           
        });
        function closeDialog(){
        	$("#gemsDetailsDialog").dialog('close');        	
        }
        
        $(window).load(function() {
            window.deleteGEMSDetails = function(gemID, gemName) {
            	var deleteUrl = '${deleteGemsUrl}' + gemID +"/deleteGems.ems"+"?ts="+new Date().getTime();
               	var proceed = confirm("<spring:message code='gemsForm.message.validation.deleteConfirmation'/>: "+gemName+"?");
        		if(proceed==true) {
        			$.ajax({
        				url: deleteUrl,
        				success: function(data){
        					window.location.href= "${gemLst}";
        				}
        			});
        	 	}
                return false;
            }
        });
       
        $(window).load(function() {
            window.activateGEMS = function(gemID, gemName) {
            	var activateURL = '${activateGEM}' + gemID +"/activate.ems"+"?ts="+new Date().getTime();
        			$.ajax({
        				url: activateURL,
        				success: function(data){
        					window.location.href= "${gemLst}";
        				}
        			});
                return false;
            }
        });
        
        $(window).load(function() {
            window.deActivateGEMS = function(gemID, gemName) {
            	var deActivateURL = '${deactivateGEM}' + gemID +"/deactivate.ems"+"?ts="+new Date().getTime();
        			$.ajax({
        				url: deActivateURL,
        				success: function(data){
        					window.location.href= "${gemLst}";
        				}
        			});
                return false;
            }
        });
        
    </script>
    
	<div id="gemsDetailsDialog"></div>

<div class="outerContainer">
 	<span id="gemsslist_header_text"><spring:message code="gems.summary" /></span>
	<div class="i1"></div>
</div>
<div class="innerdiv">
<button id="addGEMButton"><spring:message code="gems.newVEM" /></button>
<div style="height:5px;"></div>
	<spring:url value="/gems" var="editGemsUrl" scope="request" />
	<table id="all_gems_list" class="entable" style="width: 100%">
		<thead>
			<tr>
				<th align="center"><spring:message code="gems.name" /></th>
				<th align="center"><spring:message code="gems.gemsUniqueAddress" /></th>
				<th align="center"><spring:message code="gems.gemsIpAddress" /></th>
				<th align="center"><spring:message code="gems.port" /></th>
				<th align="center"><spring:message code="gemsForm.action" /></th>
				<th align="center"><spring:message code="gemsForm.Activate" /></th>
			</tr>
		</thead>
		<c:forEach items="${gemsList}" var="gems">
			<tr>
				<td><a onclick="editGEMSDetails('${editGemsUrl}/${gems.id}/edit.ems'); return false;"
					href="#">${gems.name}</a></td>
				<td>${gems.gemsUniqueAddress}</td>
				<td>${gems.gemsIpAddress}</td>
				<td>${gems.port}</td>
				<td align="center" ><input type="button" id="removeGEMButton" value="<spring:message code="action.delete" />" onclick="deleteGEMSDetails(${gems.id},'${gems.name}')"></td>
				<c:if test="${pageContext.request.method=='GET'}">
				<c:choose>
				<c:when test="${gems.status=='A'}">
					<td align="center" ><input type="button" id="deActivateButton" value="<spring:message code="action.deactivate" />" onclick="deActivateGEMS(${gems.id},'${gems.name}')"></td>
				</c:when>
				<c:otherwise>
					<td align="center" ><input type="button" id="activateButton" value="<spring:message code="action.activate" />" onclick="activateGEMS(${gems.id},'${gems.name}')"></td>
				</c:otherwise>
				</c:choose>
				</c:if>
			</tr>
		</c:forEach>
	</table>
</div>
</div>