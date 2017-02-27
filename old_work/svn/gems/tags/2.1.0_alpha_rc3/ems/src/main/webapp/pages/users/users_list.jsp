<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}
	
	.ui-dialog-title{font-size: 1.4em; font-weight: bolder;}
	
	table#all_users_list td{overflow: hidden !important;}
</style>
<div class="outermostdiv">

	<spring:url value="/users/create.ems?tenantId=${tenantId}" var="createUserUrl"
		scope="request" />
			
	<script type="text/javascript">
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
            	 $('#gotoTenantPage').click(function() {
                 	window.location="${tenantPageUrl}"+"?ts="+new Date().getTime();
                 });
            	 $("#userlist_header_text").text("User settings for tenant \"${tenant.name}\"");
            }
        });
        
    	function closeDialog(){
        	$("#userDetailsDialog").dialog('close');        	
        }

    </script>

	<div id="userDetailsDialog"></div>

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

<div style="height:5px;"></div>

	<spring:url value="/users" var="editUserUrl" scope="request" />
	<table id="all_users_list" class="entable" style="width: 100%">
		<thead>
			<tr>
				<th align="left"><spring:message code="users.email" /></th>
				<th align="left"><spring:message code="users.firstName" /></th>
				<th align="left"><spring:message code="users.lastName" /></th>
				<th align="left"><spring:message code="users.contact" /></th>
				<th align="left"><spring:message code="users.role" /></th>
				<th align="left"><spring:message code="users.status" /></th>
<!-- 				<th align="left"></th> -->
			</tr>
		</thead>

		<c:forEach items="${usersList}" var="user">
			<tr>
				<td><a onclick="editUsersDetails('${editUserUrl}/${user.id}/edit.ems'); return false;"
					href="#">${user.email}</a></td>
				<td>${user.firstName}</td>
				<td>${user.lastName}</td>
				<td>${user.contact}</td>
				<td>${user.role.roleType}</td>
				<td>${user.status.name}</td>
<!-- 				<td></td> -->
			</tr>
		</c:forEach>
	</table>
</div>
</div>