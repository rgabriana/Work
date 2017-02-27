<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script type="text/javascript">

$(document).ready(function() {
	$( "#datetime" ).datetimepicker({
		timeFormat: 'hh:mm:ss',
		dateFormat: 'yy:mm:dd',
		showSecond: true,
	    onClose: function(dateText, inst) {
	    },
	    onSelect: function (selectedDateTime){
	    },
	});
});

function submitDateTimeForm() {
	$("#statmsg").css('color', "black");
	$("#statmsg").html('Processing...');
	var timezone = $("select[name='timezone'] option:selected").html().replace(/\//g,"#");
	var datetime = $("#datetime").val();
	if(datetime == null || datetime == '') {
		datetime = "N";
	}
    $.ajax({
		type: "POST",
	    url: "<spring:url value='/services/org/updateSeverDateTimeSettings'/>" + "/" +	
	    					encodeURIComponent(timezone) + "/" + encodeURIComponent(datetime),
	    						
	    dataType: "html",
	    before: function() {
	    	$("#submitDateTime").attr("disabled", "disabled");
	    },
	    success: function(data){
	            if(data == "S"){ //Success
	            	$("#statmsg").css('color', "green");
	            	$("#statmsg").html("<spring:message code='companySetup.datetime.success'/>");
	            } else if(data == "N"){ //Failure
	            	$("#statmsg").css('color', "red");
	            	$("#statmsg").html("<spring:message code='companySetup.datetime.failure'/>");
	            }
	            else { //No changes
	            	$("#statmsg").css('color', "black");
	            	$("#statmsg").html("<spring:message code='companySetup.datetime.nochanges'/>");
	            }
	    },
	    error: function(data) {
		    	$("#statmsg").css('color', "red");
	        	$("#statmsg").html("<spring:message code='companySetup.datetime.failure'/>");
	    },
	    complete: function() {
	    	$("#submitDateTime").removeAttr("disabled");
	    }
	});
}

</script>

</head>
<body>

<div class="innerContainer">
	<div class="formContainer">
		<div class="field">
			<div class="formPrompt"><span><spring:message code='companySetup.label.change.timezone'/></span></div>
			<div class="formValue">
				<select id="timezone" name="timezone" style="width: 200px;">
					<option selected="selected" value="${currenttimezone}">${currenttimezone}</option>
					<c:forEach var="tz" items="${timezone}">
						<option value="${tz.name}" >${tz.name}</option>
					</c:forEach>
				</select>
			</div>
		</div>
		<div class="field">
			<div class="formPrompt"><span><spring:message code='companySetup.label.change.gemsdatetime'/></span></div>
			<div class="formValue"><input id="datetime" name="datetime" type="text" /></div>
		</div>
		<div class="field">
			<div class="formPrompt"><span></span></div>
			<div class="formValue">
				<button style="display: inline"  id="submitDateTime" onclick="submitDateTimeForm();"><spring:message code='fixtureForm.label.applyBtn'/></button>
				<span style="display: inline; font-size: 1.2em;" id="statmsg"></span>
			</div>
		</div>
	</div>
</div>

</body>
</html>
