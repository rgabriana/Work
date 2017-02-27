<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<style>
	#create_gem{padding:10px 15px;}
	#create_gem table{width:100%;}
	#create_gem td{padding-bottom:3px;}
	#create_gem td.fieldLabel{width:40%; font-weight:bold;}
	#create_gem td.fieldValue{width:60%;}
	#create_gem .inputField{width:100%; height:100%;}
	#create_gem #saveUserBtn{padding: 0 10px;}
	#create_gem .M_M{display: none;}
	#create_gem .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}
    
</style>

<script type="text/javascript">
	//Add gem form validation 
	var requirederr = '<spring:message code="error.above.field.required"/>';
	var createGemValidatorObj = {
			rules: {
				name: "required",
				port: {
				      required: true,
				      digits: true
				    },
				gemsIpAddress: {
					required: true,
					validip: ""
				}

			},
			messages: {
				name: { required: requirederr },
				port: { required: requirederr },
				gemsIpAddress: {
					required: requirederr,
					validip: '<spring:message code="error.invalid.ip"/>'
				}
			}
		};
	
	jQuery.validator.addMethod("validip", function(value, element) {
		var ipPattern = /^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/;
		var ipArray = value.match(ipPattern);
		if (value == "0.0.0.0") {
			return false;
		}
		else if (value == "255.255.255.255") {
			return false;
		}
		if (ipArray == null) {
			return false;
		}
		else {
			for (i = 1; i < 5; i++) {					
				if (ipArray[i] > 255) {
					return false;
				}
			}
		}
		return true;
		});
	
	
	$("#create_gem").validate(createGemValidatorObj);
	
</script>

<div>
	<spring:url value="/gems/save.ems" var="actionURL" scope="request" />
	<form:form id="create_gem" commandName="gems" method="post"	action="${actionURL}">
		<form:hidden path="id" />
		<input type="hidden" id="gemId" name="gemId" value="${gemId}" />
		<table>
			<tr>
				<td class="fieldLabel"><spring:message code="gems.name" />*</td>
				<td class="fieldValue"><form:input class="inputField" id="name" name="name" size="40" path="name" />
				<span id="domainMsg" class="error"></span></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="gems.gemsIpAddress" /><span class="M_M">*</span></td>
				<td class="fieldValue"><form:input class="inputField"  id="gemsIpAddress" path="gemsIpAddress"
					name="ip" size="40" /></td>
			</tr>
			<tr>
				<td class="fieldLabel"><spring:message code="gems.port" /><span class="M_M">*</span></td>
				<td class="fieldValue"><form:input class="inputField" id="port" name="port" size="40" path="port" /></td>
			</tr>
			<tr>
				<td />
				<td><input id="saveGemBtn" type="submit"
					value="<spring:message code="action.save" />">&nbsp;
					<input type="button" id="btnClose"
					value="<spring:message code="action.cancel" />" onclick="closeDialog()">	
				</td>
			</tr>
		</table>
	</form:form>
</div>