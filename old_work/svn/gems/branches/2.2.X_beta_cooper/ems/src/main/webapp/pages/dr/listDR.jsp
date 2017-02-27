<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/scripts/jquery/jquery.validationEngine.js" var="jquery_validationEngine"></spring:url>
<script type="text/javascript" src="${jquery_validationEngine}"></script>
<spring:url value="/scripts/jquery/jquery.validationEngine-en.js" var="jquery_validationEngine_en"></spring:url>
<script type="text/javascript" src="${jquery_validationEngine_en}"></script>


<div class="topmostContainer">
<div class="outermostdiv">
<div class="outerContainer">
	<span ><spring:message code="dr.header"/></span>
	<div style="padding: 0px 0px 0px 100px; display: inline;"><span id="drmessage"></span></div>
	<div class="i1"></div>
	<div class="innerdiv">
		<table id="weekdayTable" class="entable" style="width: 100%; height: 100%;" >
					<thead>
						<tr class="editableRow">
							<th width="20%" align="left"><spring:message code="dr.label.price.level"/></th>
							<th  align="left"><spring:message code="dr.label.pricing"/></th>
							<th  align="left"><spring:message code="dr.label.target.reduction"/></th>
							<th  align="left"><spring:message code="dr.label.enabled"/></th>
							<th  align="left"><spring:message code="dr.label.duration"/></th>
							<th  align='right' style='padding-right:5px' ><spring:message code="dr.label.run"/></th>
							<th  align='right' style='padding-right:5px' ><spring:message code="action.update"/></th>
						</tr>
					</thead>
					<c:forEach items="${drlist}" var="dr">
						
						<tr id="${dr.id}row" class="editableRow">
							<td>
								<select id="${dr.id}level">
									<option id="${dr.id}Off Peak" value="Off Peak">Off Peak</option>
									<option id="${dr.id}Normal" value="Normal">Normal</option>
									<option id="${dr.id}Peak" value="Peak">Peak</option>
									<option id="${dr.id}Moderate" value="Moderate">Moderate</option>
									<option id="${dr.id}High" value="High">High</option>
									<option id="${dr.id}Critical" value="Critical">Critical</option>
								</select>
							</td>
							<td>
								<form id="${dr.id}form1" onsubmit="return false;">
									<div class="innerContainerInputFieldValue">
										<input id="${dr.id}price" class="validate[required,custom[number],min[0]] text-input" size="10" value="${dr.pricing}" type="text"/>
									</div>
								</form>
							</td>
							<td>
								<form id="${dr.id}form2" onsubmit="return false;">
									<div class="innerContainerInputFieldValue">
										<input id="${dr.id}reduction" class="validate[required,custom[integer],min[0],max[100]] text-input" size="3" value="${dr.targetReduction}" type="text"/>
									</div>
								</form>
							</td>
							<td>
								<select id="${dr.id}enabled">
									<option id="${dr.id}Yes" value="Yes">Yes</option>
									<option id="${dr.id}No" value="No">No</option>
								</select>
							</td>
							<td>
								<form id="${dr.id}form3" onsubmit="return false;">
									<div class="innerContainerInputFieldValue">
										<input id="${dr.id}duration" class="validate[required,custom[integer],min[1]] text-input" size="10" value="${dr.duration}" type="text"/>
									</div>
								</form>
							</td>
							<td align='right' style='padding-right:5px'>
								<input type="button" id="${dr.id}init" onclick="initDR(this);" value=<spring:message code="dr.action.initiate"/> />
							</td>
							<td align='right' style='padding-right:5px'>
								<input type="button" id="${dr.id}update" onclick="updateDR(this);" value=<spring:message code="action.update"/> />
							</td>
						</tr>
						<script type="text/javascript">
								
								$("#"+"${dr.id}"+"${dr.priceLevel}").attr("selected", "selected");
								$("#"+"${dr.id}"+"${dr.enabled}").attr("selected", "selected");
								if("${dr.enabled}" == "Yes") {
									   $("#" + "${dr.id}" + "init").attr("value", '<spring:message code="action.cancel"/>');
									   $("#" + "${dr.id}" + "init").attr("onclick", "cancelDR(this);");
									   $("#" + "${dr.id}" + "update").attr("disabled", "disabled");
									   $("#" + "${dr.id}" + "level").attr("disabled", "disabled");
									   $("#" + "${dr.id}" + "price").attr("disabled", "disabled");
									   $("#" + "${dr.id}" + "reduction").attr("disabled", "disabled");
									   $("#" + "${dr.id}" + "enabled").attr("disabled", "disabled");
									   $("#" + "${dr.id}" + "duration").attr("disabled", "disabled");
								}
								$("#"+"${dr.id}" + "form1").validationEngine('attach' , {
									isOverflown: true,
									overflownDIV: ".topmostContainer"
								});
								
								$("#"+"${dr.id}" + "form2").validationEngine('attach' , {
									isOverflown: true,
									overflownDIV: ".topmostContainer"
								});
								$("#"+"${dr.id}" + "form3").validationEngine('attach' , {
									isOverflown: true,
									overflownDIV: ".topmostContainer"

								});
						</script>

														
				
					</c:forEach>
					
		</table>
	</div>
</div>
</div>
</div>
<script type="text/javascript">

function updateDR(obj) {
	$("#drmessage").html("");
	var id = $(obj).attr('id').split('u')[0];
	if($("#"+id + "form1").validationEngine('validate') && $("#"+id+"form2").validationEngine('validate') && $("#"+id+"form3").validationEngine('validate')) {
 		$.ajax({
			   type: "POST",
			   url: '<spring:url value="/services/org/dr/update"/>',
			   contentType: "application/json",
			   data: '{"id":"' + id + '","priceLevel":"' + $("#"+id+"level").val()  + 
				   '","pricing":"' + $("#" + id +"price").val() + 
				   '","duration":"' + $("#" + id + "duration").val() +
				   '","targetReduction":"' + $("#" + id +"reduction").val() + 
				   '","enabled":"' + $("#" + id +"enabled").val() + 
				   '"}',
			   dataType: "json",
			   success: function(msg){
				   if(msg.status == "1") {
					   if($("#" + id +"enabled").val() == "Yes") {
						   if(msg.msg == "S") {
							   $("#drmessage").css("color", "green");
							   $("#drmessage").html("Demand Response is updated and instantiated successfully.");
							   $("#" + id + "init").attr("value", '<spring:message code="action.cancel"/>');
							   $("#" + id + "init").attr("onclick", "cancelDR(this);");
							   $("#" + id + "update").attr("disabled", "disabled");
							   $("#" + id + "level").attr("disabled", "disabled");
							   $("#" + id + "price").attr("disabled", "disabled");
							   $("#" + id + "reduction").attr("disabled", "disabled");
							   $("#" + id + "enabled").attr("disabled", "disabled");
							   $("#" + id + "duration").attr("disabled", "disabled");
						   }
						   else if(msg.msg == "R") {
							   $("#drmessage").css("color", "red");
							   $("#drmessage").html("Some unexpected error while updating the Demand Response. Please refresh the page and try again.");
						   }
						   else if(msg.msg == "E"){
							   $("#drmessage").css("color", "red");
							   $("#drmessage").html("Demand Response is updated successfully but cannot be instantiated since another Demand Response is already under execution, cannot instantiate a new one!");
							   $("#" + id  + "No").attr("selected", "selected");
						   }
						   else {
							   $("#drmessage").css("color", "red");
							   $("#drmessage").html("Demand Response is updated successfully but cannot be instantiated due to some unexpected error.");
							   $("#" + id  + "No").attr("selected", "selected");
						   }
					   }
					   else if(msg.msg == "S") {
						   $("#drmessage").css("color", "green");
						   $("#drmessage").html("Demand Response is updated successfully.");
					   }

				   }   
			   },
			   error: function() {
				   $("#drmessage").css("color", "red");
				   $("#drmessage").html("Some unexpected error while updating the Demand Response.");
			   }
		});	
	}

}

function initDR(obj) {
	$("#drmessage").html("");
	var id = $(obj).attr('id').split('i')[0];
	if($("#"+id + "form1").validationEngine('validate') && $("#"+id+"form2").validationEngine('validate') && $("#"+id+"form3").validationEngine('validate')) {
 		$.ajax({
			   type: "POST",
			   url: '<spring:url value="/services/org/dr/initiate"/>',
			   contentType: "application/json",
			   data: '{"id":"' + id + '","priceLevel":"' + $("#"+id+"level").val()  + 
			   '","pricing":"' + $("#" + id +"price").val() + 
			   '","duration":"' + $("#" + id + "duration").val() +
			   '","targetReduction":"' + $("#" + id +"reduction").val() + 
			   '","enabled":"' + $("#" + id +"enabled").val() + 
			   '"}',
			   dataType: "json",
			   success: function(msg){
				   if(msg.status == "1") {
					   if(msg.msg == "S") {
						   $("#drmessage").css("color", "green");
						   $("#drmessage").html("Demand Response is instantiated succuessfully.");
						   $("#" + id  + "Yes").attr("selected", "selected");
						   $("#" + id + "init").attr("value", '<spring:message code="action.cancel"/>');
						   $("#" + id + "init").attr("onclick", "cancelDR(this);");
						   $("#" + id + "update").attr("disabled", "disabled");
						   $("#" + id + "level").attr("disabled", "disabled");
						   $("#" + id + "price").attr("disabled", "disabled");
						   $("#" + id + "reduction").attr("disabled", "disabled");
						   $("#" + id + "enabled").attr("disabled", "disabled");
						   $("#" + id + "duration").attr("disabled", "disabled");
					   }
					   else if(msg.msg == "E"){
						   $("#drmessage").css("color", "red");
						   $("#drmessage").html("Another demand response is already under execution, cannot instantiate a new one!");
					   }
					   else {
						   $("#drmessage").css("color", "red");
						   $("#drmessage").html("Some unexpected error while instantiating the Demand Response.");
					   }
					   
				   }   
			   },
			   error: function() {
				   $("#drmessage").css("color", "red");
				   $("#drmessage").html("Some unexpected error while instantiating the Demand Response.");
			   }		   
		});
	}

}

function cancelDR(obj) {
	$("#drmessage").html("");
	var id = $(obj).attr('id').split('i')[0];
 	$.ajax({
		   type: "POST",
		   url: '<spring:url value="/services/org/dr/cancel/"/>',
		   dataType: "json",
		   success: function(msg){
			   if(msg.status == "1") {
				   if(msg.msg == "S") {
					   $("#drmessage").css("color", "green");
					   $("#drmessage").html("Demand Response is canceled succuessfully.");
					   $("#" + id  + "No").attr("selected", "selected");
					   $("#" + id + "init").attr("value", '<spring:message code="dr.action.initiate"/>');
					   $("#" + id + "init").attr("onclick", "initDR(this);");
					   $("#" + id + "update").removeAttr("disabled");
					   $("#" + id + "level").removeAttr("disabled");
					   $("#" + id + "price").removeAttr("disabled");
					   $("#" + id + "reduction").removeAttr("disabled");
					   $("#" + id + "enabled").removeAttr("disabled");
					   $("#" + id + "duration").removeAttr("disabled");
				   }
				   else {
					   $("#drmessage").css("color", "red");
					   $("#drmessage").html("Some unexpected error while canceling the Demand Response.");
				   }
				   
			   }   
		   },
		   error: function() {
			   $("#drmessage").css("color", "red");
			   $("#drmessage").html("Some unexpected error while canceling the Demand Response.");
		   }		   
	});
}

$(function() {
	$(window).resize(function() {
		var setSize = $(window).height();
		setSize = setSize - 118;
		$(".topmostContainer").css("height", setSize);
	});
});
$(".topmostContainer").css("overflow", "auto");
$(".topmostContainer").css("height", $(window).height() - 118);
</script>