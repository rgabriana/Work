<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/replicaserver/duplicate/add/" var="checkDuplicateAddReplicaServer" scope="request" />
<spring:url value="/services/org/replicaserver/duplicate/edit/" var="checkDuplicateEditReplicaServer" scope="request" />

<style>
	#create_replicaServer{padding:10px 15px;}
	#create_replicaServer table{width:100%;}
	#create_replicaServer td{padding-bottom:3px;}
	#create_replicaServer td.fieldLabel{width:40%; font-weight:bold;}
	#create_replicaServer td.fieldValue{width:60%;}
	#create_replicaServer .inputField{width:100%; height:20px;}
	#create_replicaServer #saveBtn{padding: 0 10px;}
	#create_replicaServer .M_M{display: none;}
	#create_replicaServer .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}    
</style>

<script type="text/javascript">

$(document).ready(function(){
	
	$("#create_replicaServer").validate({
		rules: {
			name: {
				required: true,
			},
			ip: {
				required: true,
			},
			internalIp: {
				required: true,
			},
			uid: {
				required: true,
			}
		},
		messages: {
			name: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			ip: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			internalIp: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			uid: {
				required: '<spring:message code="error.above.field.required"/>',
			}
		}
	});
	
});
	
	function saveReplicaServer(){
		
		clearReplicaLabelMessages();
		
		var replicaServerForm = $("#create_replicaServer");
		
		if (replicaServerForm.valid()){
			if ( "${mode}" != "Edit"){
				$.ajax({
			 		type: 'GET',
			 		url: "${checkDuplicateAddReplicaServer}"+$("#name").val()+"/"+$("#ip").val()+"/"+$("#internalIp").val()+"/"+$("#uid").val()+"?ts="+new Date().getTime(),
			 		dataType: "text",
			 		success: function(data){
						if(data == "name"){
							$("#replica_name_error_msg").text("Name Already Exists");
							$("#name").addClass("invalidField");
						}else if(data == "ip") {
							$("#replica_ip_error_msg").text("IP Already Exists");
							$("#ip").addClass("invalidField");
						}else if(data == "internalIp") {
							$("#replica_internalIp_error_msg").text("Internal Ip Already Exists");
							$("#internalIp").addClass("invalidField");
						}else if(data == "uid") {
							$("#replica_uid_error_msg").text("UID Already Exists");
							$("#uid").addClass("invalidField");
						}else if(data == "none") {
							$("#create_replicaServer").submit();
						}
						
					},
					error: function(){
						alert("Server Error");
					}
			 	});
				
			}
			else{
				$.ajax({
			 		type: 'GET',
			 		url: "${checkDuplicateEditReplicaServer}"+$("#id").val()+"/"+$("#name").val()+"/"+$("#ip").val()+"/"+$("#internalIp").val()+"/"+$("#uid").val()+"?ts="+new Date().getTime(),
			 		dataType: "text",
			 		success: function(data){
						if(data == "name"){
							$("#replica_name_error_msg").text("Name Already Exists");
							$("#name").addClass("invalidField");
						}else if(data == "ip") {
							$("#replica_ip_error_msg").text("IP Already Exists");
							$("#ip").addClass("invalidField");
						}else if(data == "internalIp") {
							$("#replica_internalIp_error_msg").text("Internal Ip Already Exists");
							$("#internalIp").addClass("invalidField");
						}else if(data == "uid") {
							$("#replica_uid_error_msg").text("UID Already Exists");
							$("#uid").addClass("invalidField");
						}else if(data == "none") {
							$("#create_replicaServer").submit();
						}
						
					},
					error: function(){
						alert("Server Error");
					}
			 	});
			}
		}
		
	}
	
	function closeReplicaServerDialog(){
		$("#replicaServerDetailsDialog").dialog("close");
	}
	
	function clearReplicaLabelMessages() {
		$("#replica_name_error_msg").text("");
		$("#replica_ip_error_msg").text("");
		$("#replica_internalIp_error_msg").text("");
		$("#replica_uid_error_msg").text("");
		$("#name").removeClass("invalidField");
		$("#ip").removeClass("invalidField");
		$("#internalIp").removeClass("invalidField");
		$("#uid").removeClass("invalidField");
	}
	
</script>

<div>
	<spring:url value="/replicaserver/save.ems" var="actionURL" scope="request" />
	<form:form id="create_replicaServer" commandName="replicaServer" method="post" 
		action="${actionURL}">
		<form:hidden path="id" name="id" id="id"/>
		<table>
			<tr>
				<td class="fieldLabel">Name*</td>
				<td class="fieldValue"><form:input class="inputField" id="name" name="name" size="40" path="name" /><span id="replica_name_error_msg" class="error"></span></td>
			</tr>
			<tr>
				<td class="fieldLabel">Ip*</td>
				<td class="fieldValue"><form:input class="inputField" id="ip" name="ip" size="40" path="ip" /><span id="replica_ip_error_msg" class="error"></td>
			</tr>
			<tr>
				<td class="fieldLabel">Internal Ip*</td>
				<td class="fieldValue"><form:input class="inputField" id="internalIp" name="internalIp" size="40" path="internalIp" /><span id="replica_internalIp_error_msg" class="error"></td>
			</tr>
			<tr>
				<td class="fieldLabel">Mac id*</td>
				<td class="fieldValue"><form:input class="inputField" id="macId" name="macId" size="40" path="macId" /><span id="replica_macId_error_msg" class="error"></td>
			</tr>
			<c:if test="${mode == 'Edit'}">
			<tr>
				<td class="fieldLabel">UID*</td>
				<td class="fieldValue"><form:input class="inputField" id="uid" name="uid" size="40" path="uid" readonly="true"/><span id="replica_uid_error_msg" class="error"></td>
			</tr>
			</c:if>
			<tr>
				<td />
				<td><input id="saveBtn" type="button"
					value="<spring:message code="action.save" />" onclick="saveReplicaServer();">&nbsp;
					<input type="button" id="btnClose"
					value="<spring:message code="action.cancel" />" onclick="closeReplicaServerDialog()">	
				</td>
			</tr>
		</table>
	</form:form>
</div>