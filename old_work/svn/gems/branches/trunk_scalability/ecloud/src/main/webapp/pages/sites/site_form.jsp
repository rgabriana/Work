<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<spring:url value="/services/org/site/v1/checkforduplicatesite/" var="checkForDuplicateSiteNameUrl" scope="request" />
<style>
	#create_site{padding:10px 15px;}
	#create_site table{width:100%;}
	#create_site td{padding-bottom:3px;}
	#create_site td.fieldLabel{width:40%; font-weight:bold;}
	#create_site td.fieldValue{width:60%;}
	#create_site .inputField{width:100%; height:20px;}
	#create_site #saveBtn{padding: 0 10px;}
	#create_site .M_M{display: none;}
	#create_site .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}  
}
</style>

<script type="text/javascript">

$(document).ready(function(){
	
	$.validator.addMethod('positiveInteger',
		    function (n) { 
		        return 0 === n % (!isNaN(parseFloat(n)) && 0 <= ~~n);
		    }, 'Please enter a positive Integer.');
	
	$.validator.addMethod('positiveNumber',
		    function (value) { 
				return Number(value) >= 0;;
		    }, 'Please enter a positive Number.');
	
	$("#create_site").validate({
		rules: {
			name: {
				required: true,
			},
			sppaPrice: {
				required: true,
				positiveNumber:true,
			},
			blockPurchaseEnergy: {
				required: true,
				positiveNumber:true,
			},
			geoLocation: {
				required: true,
			},
			region: {
				required: true,
			},
			poNumber: {
				required: true,
			},
			billStartDate:
			{
				required: true,
			},
			squareFoot: {
				required: true,
				positiveNumber:true,
			},
			estimatedBurnHours:{
				required: true,
				positiveNumber:true,
			}
		},
		messages: {
			name: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			sppaPrice: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			blockPurchaseEnergy: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			geoLocation: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			squareFoot: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			poNumber: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			region: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			billStartDate: {
				required: '<spring:message code="error.above.field.required"/>',
			},
			estimatedBurnHours:{
				required: '<spring:message code="error.above.field.required"/>',
			}
		}
	});
	$( "#billStartDate" ).datetimepicker({
		dateFormat: 'mm/dd/yy',
		showTimepicker:false,
		currentText:'Today',
		//maxDate: 0,
	    onClose: function(dateText, inst) {
	    },
	    onSelect: function (selectedDateTime){
	    }
	});
	
	
});
	
	function saveSite(){
		checkForDuplicateSiteName();
	}
	
	function closeSiteDialog(){
		$("#siteFormDialog").dialog("close");
	}
	
	function parseEmInstanceVersionString (str) {
	    if (typeof(str) != 'string') { return false; }
	    var x = str.split('.');
	    var maj = parseInt(x[0]) || 0;
	    var min = parseInt(x[1]) || 0;
	    var pat = parseInt(x[2]) || 0;
	    var rem = parseInt(x[3]) || 0;
	    return {
	        major: maj,
	        minor: min,
	        patch: pat,
	        remain: rem
	    }
	}
	//Global variable for accessing current customer id
	var SITEID = 	$("#id").val();

	function checkForDuplicateSiteName(){
		var siteName = $('#name').val();
		siteName = $.trim(siteName);
		$.ajax({
			type: 'POST',
			url: "${checkForDuplicateSiteNameUrl}"+siteName+"?ts="+new Date().getTime(),
			success: function(data){
				if(data == null){
					$("#create_site").submit();
				}else{
					
					if(SITEID != data.id){
						alert("Customer with the same name already exists");
					}else{
						$('#create_site').submit();
					}
				}
			},
			dataType:"json",
			contentType: "application/json; charset=utf-8"
		});	
	}
	
</script>

<div>
	<spring:url value="/sites/save.ems" var="actionURL" scope="request" />
	<form:form id="create_site" commandName="site" method="post" action="${actionURL}">
		<form:hidden path="id" id="id" name="id" />
		<input type="hidden" id="customerId" name="customerId" value="${customerId}" />
		<form:hidden path="taxRate" id="taxRate" name="taxRate" />
		
		<table>
			<tr>
				<td class="fieldLabel">Name*</td>
				<td class="fieldValue"><form:input class="inputField" id="name" name="name" size="40" path="name" />
			</tr>
			<tr>
				<td class="fieldLabel">Geo Location*</td>
				<td class="fieldValue"><form:input class="inputField" id="geoLocation"
					name="geoLocation" size="40" path="geoLocation"/></td>
			</tr>
			<tr>
				<td class="fieldLabel">Region*</td>
				<td class="fieldValue">
<%-- 				<form:input class="inputField" id="region"	name="region" size="40" path="region"/> --%>
				<form:select path="region" id="regionCombo">
                    			<form:option value="West" label="West"></form:option>
                    			<form:option value="Central" label="Central"></form:option>
                    			<form:option value="Texas" label="Texas"></form:option>
                    			<form:option value="Midwest" label="Midwest"></form:option>
                    			<form:option value="Northeast" label="Northeast"></form:option>
                    			<form:option value="Southeast" label="Southeast"></form:option>
                </form:select>
				</td>
			</tr>
			<tr>
				<td class="fieldLabel">SPPA Price*</td>
				<td class="fieldValue"><form:input class="inputField" id="sppaPrice" 
						name="sppaPrice" size="40" path="sppaPrice" />
			</tr>
			<tr>
				<td class="fieldLabel">Block Purchase Energy*</td>
				<td class="fieldValue"><form:input class="inputField" id="blockPurchaseEnergy"
					name="blockPurchaseEnergy" size="40" path="blockPurchaseEnergy"/></td>
			</tr>
			<tr>
				<td class="fieldLabel">PO Number*</td>
				<td class="fieldValue"><form:input class="inputField" id="poNumber"
					name="poNumber" size="40" path="poNumber"/></td>
			</tr>
			<tr>
				<td class="fieldLabel">Square Foot*</td>
				<td class="fieldValue"><form:input class="inputField" id="squareFoot"
					name="squareFoot" size="40" path="squareFoot"/></td>
			</tr>
			 <tr>
				<td class="fieldLabel">Release date*</td>
				
				<fmt:formatDate value="${site.billStartDate}"  
                type="date" 
                pattern="MM/dd/yyyy"
                var="theFormattedDate" />
				<td class="fieldValue"><form:input  id="billStartDate" name="billStartDate"  path="billStartDate" value="${theFormattedDate}" type="text" /></td>
			</tr>
			<tr>
				<td class="fieldLabel">Estimated Burn Hours*</td>
				<td class="fieldValue"><form:input class="inputField" id="estimatedBurnHours"
					name="estimatedBurnHours" size="40" path="estimatedBurnHours"/></td>
			</tr>
			<tr>
				<td />
				<td><input id="saveBtn" type="button"
					value="<spring:message code="action.save" />" onclick="saveSite();">&nbsp;
					<input type="button" id="btnClose"
					value="<spring:message code="action.cancel" />" onclick="closeSiteDialog()">	
				</td>
			</tr>
		
		</table>
	</form:form>
</div>



