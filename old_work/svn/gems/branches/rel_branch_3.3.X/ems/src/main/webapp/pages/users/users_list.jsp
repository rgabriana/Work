<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<spring:url value="/users/delete.ems" var="actionURL" scope="request" />
<spring:url value="/users/create.ems?tenantId=${tenantId}" var="createUserUrl" scope="request" />

<spring:url value="/facilities" var="assignFacilityUserUrl" scope="request"/>
<spring:url value="/users" var="unlockUserUrl" scope="request"/>
		
<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
	
	.ui-dialog-title{font-size: 1.4em; font-weight: bolder;}
	
	table#all_users_list td{overflow: hidden !important;}
	
	table#all_users_list {table-layout:auto !important;}
			
</style>

<div class="outermostdiv">
	<script type="text/javascript">
		var COLOR_SUCCESS = "green";
		var COLOR_DEFAULT = "black";
		
        $(document).ready(function() {
            //define configurations for dialog
            var userDialogOptions = {
                title : "New User",
                modal : true,
                autoOpen : false,
                height : 300,
                width : 500,
                draggable : true
            }

            $('#newUserButton').click(function() {
            	clearLabelMessage();
                $("#userDetailsDialog").load("${createUserUrl}"+"&ts="+new Date().getTime()).dialog({
                    title : "New User",
                    width :  Math.floor($('body').width() * .35),
                    minHeight : 300,
                    modal : true
                });
                return false;
            });
            $(window).load(function() {
                window.editUsersDetails = function(url) {
                	clearLabelMessage();
                    $("#userDetailsDialog").load(url+"?ts="+new Date().getTime()).dialog({
                        title : "Edit User",
                        width :  Math.floor($('body').width() * .35),
                        minHeight : 300,
                        modal : true
                    });
                    return false;
                }
            });
            
            var tenantID="${tenantId}";
            if(!(tenantID == "0" || tenantID == "")){
            	<spring:url value="../tenants/list.ems" var="tenantPageUrl" scope="request" />
            	 $("#gotoTenantPage").css("display","inline");
            	 $("#gotoTenantPage").css("float","right");
            	 $('#gotoTenantPage').click(function() {
                 	window.location="${tenantPageUrl}"+"?ts="+new Date().getTime();
                 });
            	 $("#userlist_header_text").text("User settings for tenant \"${tenant.name}\"");
            }
            
            var displayStatus= "${facilityAssignmentStatus}";  
            if(displayStatus=="success")
            displayLabelMessage('<spring:message code="user.facilityAssignment.success"/>', COLOR_SUCCESS);
            
            var status= '<%=request.getParameter("status")%>'; 
        	if(status == "new") {
        		displayLabelMessage('<spring:message code="user.add.success"/>', COLOR_SUCCESS);
        		if("${newUser.role.roleType}" != "Admin"){
        			assignFacility('${assignFacilityUserUrl}/${newUser.id}/assignFacilityUser.ems','${newUser.email}');
        		}
        	} 
        	if(status == "edit") {
        		displayLabelMessage('<spring:message code="user.edit.success"/>', COLOR_SUCCESS);
        	}
        	if(status == "delete") {
        		displayLabelMessage('<spring:message code="user.delete.success"/>', COLOR_SUCCESS);
        	}
        	if(status == "unlocked") {
        		displayLabelMessage('<spring:message code="user.unlock.success"/>', COLOR_SUCCESS);
        	}
        });
        
    	function closeDialog(){
        	$("#userDetailsDialog").dialog('close');        	
        }

    	function closeUnlockUserDialog(){
        	$("#unlockUserDialog").dialog('close');        	
        }
    	
    	function beforeDeleteUser(){
    		clearLabelMessage();
    		if(confirm('Are you sure you want to delete this user?')){
    			return true;
    		}
    		return false;
    	}
    	
    	var assignFacility = function(url,userEmail){
    		clearLabelMessage();
        	$("#assignFacilityDialog").load(url+"?ts="+new Date().getTime()).dialog({
                title : "Assign Facility to User "+userEmail,
                width :  Math.floor($('body').width() * .35),
                height : 500,
                modal : true
            });
            return false;
        };
        
        var unlockUser = function(url,userEmail){
    		clearLabelMessage();
        	$("#unlockUserDialog").load(url+"?ts="+new Date().getTime()).dialog({
                title : "Password Unlock for User "+userEmail,
                width :  Math.floor($('body').width() * .35),
                height : 200,
                modal : true
            });
            return false;
        };
        
        function closeAssignFacilityDialog(){
        	$("#assignFacilityDialog").dialog('close');        	
        }
        function displayLabelMessage(Message, Color) {
    		$("#user_message").html(Message);
    		$("#user_message").css("color", Color);
    	}
        function clearLabelMessage(Message, Color) {
    		displayLabelMessage("", COLOR_DEFAULT);
    	}
    	
    	$(".outermostdiv").css("overflow", "auto");	
		$(".outermostdiv").css("height", $(window).height());
    </script>

	<div id="userDetailsDialog"></div>
	<div id="assignFacilityDialog"></div>
	<div id="unlockUserDialog"></div>
	
<!-- 	<b>User Settings</b> <br /> -->
<div class="outerContainer">
	<span id="userlist_header_text"><spring:message code="users.usermanagement" /></span>
	<div class="i1"></div>
</div>
<div class="innerdiv">

<button id="newUserButton">New User</button>
	
<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin')">
	<button id="gotoTenantPage" style="display:none;">Back To Tenants</button>
</security:authorize>
	
<div id="user_message" style="font-size: 14px; font-weight: bold;padding: 5px 0 0 5px;" ></div>


<div style="height:5px;"></div>

	<spring:url value="/users" var="editUserUrl" scope="request" />
	
	<table id="all_users_list" class="entable" style="width: 100%">
		<thead>
			<tr>
				<th align="center"><spring:message code="users.email" /></th>
				<th align="center"><spring:message code="users.firstName" /></th>
				<th align="center"><spring:message code="users.lastName" /></th>
				<th align="center"><spring:message code="users.contact" /></th>
				<th align="center"><spring:message code="users.role" /></th>
				<th align="center"><spring:message code="users.status" /></th>
				<th align="center">Action</th>
			</tr>
		</thead>

		<c:forEach items="${usersList}" var="user">
			<tr>
				<%-- <td><a onclick="editUsersDetails('${editUserUrl}/${user.id}/edit.ems'); return false;"
					href="#">${user.email}</a></td> --%>
				<td>${user.email}</td>	
				<td>${user.firstName}</td>
				<td>${user.lastName}</td>
				<td>${user.contact}</td>
				<td>${user.role.roleType}</td>
				<td>${user.status.name}</td>
				<td align="right" style="padding-right:5px;width:25%;">					
					<div>
						<form id="user-details" method="post" onsubmit="return beforeDeleteUser();" action="${actionURL}">
							<input type="hidden" id="userId" name="userId" value="${user.id}"/>
							<input type="hidden" id="tenantId" name="tenantId" value="${tenantId}"/>
							<security:authorize access="hasAnyRole('Admin','FacilitiesAdmin')">
								<c:choose>
									<c:when test="${user.userLocked}">
										<input type="button" id="${user.email}_assignUnlockUserBtn" onclick="return unlockUser('${unlockUserUrl}/${user.id}/unlockUser.ems','${user.email}');" value="Unlock"/>
									</c:when>
									<c:otherwise>
										<%-- <input type="button" id="${user.email}_assignUnlockUserBtn" onclick="return unlockUser('${unlockUserUrl}/${user.id}/unlockUser.ems','${user.email}');" value="Reset Password"/> --%>
									</c:otherwise>
								</c:choose>
								<%-- <input type="button" id="${user.email}_assignUnlockUserBtn" onclick="return unlockUser('${unlockUserUrl}/${user.id}/unlockUser.ems','${user.email}');" value="Reset Password"/> --%>
							</security:authorize>
							
							<input type="button" id="${user.email}_editUserBtn" name="editTenantId" value="Edit" onclick="editUsersDetails('${editUserUrl}/${user.id}/edit.ems');"/>
							<c:if test="${user.role.roleType != 'Admin'}">
								<input type="button" id="${user.email}_assignFacilityBtn" onclick="return assignFacility('${assignFacilityUserUrl}/${user.id}/assignFacilityUser.ems','${user.email}');" value="Assign Facility"/>
							</c:if>
							<input type="submit" id="${user.email}_userDeleteBtn" value="Delete"/>
						</form>
					</div>
					<%-- <div style="padding-right:5px;"><button id="btnAssignFacility" onclick="return assignFacility('${assignFacilityUserUrl}/${user.id}/assignFacilityUser.ems');">Assign Facility</button></div> --%>
					
				</td>				
			</tr>
		</c:forEach>
	</table>
</div>
</div>