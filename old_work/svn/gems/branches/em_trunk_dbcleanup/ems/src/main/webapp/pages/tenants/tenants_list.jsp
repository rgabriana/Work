<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}	
	.ui-dialog-title{font-size: 1.4em; font-weight: bolder;}	
	table#all_tenants_list td{overflow: hidden !important;}
</style>

<div class="outermostdiv">
	<spring:url value="/tenants/create.ems" var="createTenantUrl" scope="request" />		
	<spring:url value="/facilities/tenant_tree_assignment.ems" var="facilitiesAssignment" scope="request"/>
	<script type="text/javascript">
	var COLOR_SUCCESS = "green";
	var COLOR_DEFAULT = "black";
	
        $(document).ready(function() {   
        	
            $('#newTenantButton').click(function() {
            	clearLabelMessage();
            	$("#tenantDetailDialog").load("${createTenantUrl}"+"?ts="+new Date().getTime(), function() {
           		    $("#tenantDetailDialog").dialog({
           			  title : "New Tenant",
                         width :  Math.floor($('body').width() * .30),
                         minHeight : 240,
                         modal : true
           			});
	            });
            	return false;
            });
            
            $('#assignFacilityButton').click(function() {
            	window.location='${facilitiesAssignment}'+"?ts="+new Date().getTime();
            });

            
            $(window).load(function() {
            	window.editTenantsDetails = function(url) {
            		clearLabelMessage();
            		$("#tenantDetailDialog").load(url+"?ts="+new Date().getTime(), function() {
	            		  $("#tenantDetailDialog").dialog({
	            			  title : "Edit Tenant",
	                          width :  Math.floor($('body').width() * .30),
	                          minHeight : 240,
	                          modal : true
	            			});
	            });
            	return false;
              }	
            });
            
            $(window).load(function() {
                window.loadTenantUsers = function(tenantId) {
                    $("#tenantId").val(tenantId);
                    $('#userListForm').submit();
                    return false;
                }
            });
            
            $(window).load(function() {
                window.editTenantFacilities = function(url) {
                    $("#assignFacilityDialog").load(url).dialog({
                        title : "Assign Facility",
                        modal : true
                    });
                    return false;
                }
            });
            
            var displayStatus= "${facilityAssignmentStatus}";  
            if(displayStatus=="success")
            displayLabelMessage('<spring:message code="user.facilityAssignment.success"/>', COLOR_SUCCESS);
          
            var status= '<%=request.getParameter("status")%>'; 
        	if(status == "new") {
        		displayLabelMessage('<spring:message code="tenant.add.success"/>', COLOR_SUCCESS);
        	} 
        	if(status == "edit") {
        		displayLabelMessage('<spring:message code="tenant.edit.success"/>', COLOR_SUCCESS);
        	}
        });
   	 function displayLabelMessage(Message, Color) {
	  		$("#user_message").html(Message);
	  		$("#user_message").css("color", Color);
		}
		function clearLabelMessage(Message, Color) {
			displayLabelMessage("", COLOR_DEFAULT);
		}
		
	</script>

	<div id="tenantDetailDialog"></div>	
	<div id="assignFacilityDialog"></div>

<div class="outerContainer">
	<span>Tenants</span>
	<div class="i1"></div>
</div>
<div class="innerdiv">
<button id="newTenantButton">Create Tenant</button>
<button id="assignFacilityButton" style="float: right;">Assign Facility</button>
<div id="user_message" style="font-size: 14px; font-weight: bold;padding: 5px 0 0 5px;" ></div>
<div style="height:5px;"></div>

	<spring:url value="/tenants" var="editTenantUrl" scope="request" />
	<table id="all_tenants_list" class="entable" style="width: 100%">
		<thead>
			<tr>
				<th align="left"><spring:message code="tenant.name" /></th>
				<th align="left"><spring:message code="tenant.validDomain" /></th>
				<th align="left"><spring:message code="tenant.email" /></th>
				<th align="left"><spring:message code="tenant.phoneNo" /></th>
				<th align="left"><spring:message code="tenant.address" /></th>
				<th align="left"><spring:message code="tenant.status" /></th>
				<th align="center">Manage Tenant Users</th>
			</tr>
		</thead>
		<c:forEach items="${tenantsList}" var="tenant">
			<tr>
				<%-- <td><a
					onclick="javascript:editTenantsDetails('${editTenantUrl}/${tenant.id}/edit.ems'); return false;" href="#">${tenant.name}</a></td> --%>
				<td>${tenant.name}</td>	
				<td>${tenant.validDomain}</td>
				<td>${tenant.email}</td>
				<td>${tenant.phoneNo}</td>
				<td>${tenant.address}</td>
				<td>${tenant.status.name}</td>
				<spring:url value="/users" var="usersList" scope="request"/>
				<spring:url value="/themes/default/images/assign_facility.png" var="assignFacilityImg" />
				<spring:url value="/themes/default/images/manage_users.png" var="manageUsersImg" />
				<td align="right" style="padding-right:5px;">
					<div>
						    <button onclick="editTenantsDetails('${editTenantUrl}/${tenant.id}/edit.ems');">Edit</button>
						    <c:if test="${tenant.status.name == 'ACTIVE'}">
								<button id='tenantuser${tenant.id}' onclick='loadTenantUsers("${tenant.id}");'>Manage Users</button>
							</c:if>
					</div>
				</td>
			</tr>
		</c:forEach>
	</table>

	<spring:url value="/users/list.ems" var="usersList" />
	<form id="userListForm" action="${usersList}" method="post">
		<input type="hidden" id="tenantId" name="tenantId" />
	</form>
</div>
</div>
