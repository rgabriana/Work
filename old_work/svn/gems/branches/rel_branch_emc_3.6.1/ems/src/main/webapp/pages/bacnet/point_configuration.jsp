<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/bacnetconfig/saveBacnetObjectCfgs" var="submitPointConfiguration" scope="request"/>


<style>
.outerPointContainer {
    margin: 5px 0 5px 5px;
}

.opg{
	padding: 10px 1px 10px 10px;
}
</style>
<script type="text/javascript">

var isBacnetEnabled = "";

var isBacnetNetworkConfigured = "";

$().ready(function() {
	
	isBacnetEnabled = "${isBacnetEnabled}";
	
	isBacnetNetworkConfigured = "${isBacnetNetworkConfigured}";
	
	if(isBacnetNetworkConfigured == "false"){
		$('#errorMessageId').text("Bacnet Network is not configured. Please configure Bacnet Network in Network Settings page.");
		$('#submit').prop("disabled", true);
	}else{
		$('#errorMessageId').text("");
	}
	
});



function savePointConfiguration(){
	$('#saveconfirm').text("");
	var bocfgXML = '<bacnetObjectsCfgs>';
	<c:forEach items="${bacnetObjectsCfgList}" var="bacnetObjConf">
		var isValidObject = 'n';
		var t = $("#isvalidobject_${bacnetObjConf.id}");
		if(t.is(':checked')){
			isValidObject = 'y';
		}
	    bocfgXML = bocfgXML+'<bacnetObjectsCfg><id>${bacnetObjConf.id}</id><bacnetobjecttype>${bacnetObjConf.bacnetobjecttype}</bacnetobjecttype><bacnetobjectinstance>${bacnetObjConf.bacnetobjectinstance}</bacnetobjectinstance><bacnetobjectdescription>${bacnetObjConf.bacnetobjectdescription}</bacnetobjectdescription><isvalidobject>'+isValidObject+'</isvalidobject><pointkeyword>${bacnetObjConf.pointkeyword}</pointkeyword><bacnetpointtype>${bacnetObjConf.bacnetpointtype}</bacnetpointtype></bacnetObjectsCfg>';
	</c:forEach>
	bocfgXML = bocfgXML +'</bacnetObjectsCfgs>';
	$.ajax({
		type : "POST",
		url : "${submitPointConfiguration}",
		data : bocfgXML,
		dataType : "xml",
		contentType : "application/xml; charset=utf-8",
		success : function(response, textStatus, xhr) {
			console.log("success");
			$("#saveconfirm").html('<spring:message code="bacnet.save.confirmation"/>');
			var ifr = document.getElementById("bconfFrame");
			if( ifr != null || ifr != undefined ){
				ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
				ifr.src = ifr.src + new Date().getTime();
				ifr.src = "${bacnetConf}";
			}
			return true;
		},
		error : function(xhr, textStatus, errorThrown) {
			console.log("error");
			$("#error").html('<spring:message code="bacnet.save.confirmation"/>');
			return false;
		}
	});
}


</script>

<div class="outerContainer">
	<span id="errorMessageId" style="color:red"></span>
	<span id="error" style="color:red"></span>
	<span id="saveconfirm" style="color:green"></span>
	<div class="i1"></div>
	<div class="formContainer">
		<%-- <form:form id="bacnetObjectCfg" onsubmit="savePointConfiguration();" > --%>
		<div class="outerPointContainer">
			<span><spring:message code="bacnet.label.groupheader" /></span>
			<div class="i1"></div>
		</div>
		<div class="upperdiv" style="margin: 5px; padding: 5px;  ">
			<div class="outerPointContainer">
				<span><spring:message code="bacnet.label.emsubheader" /></span>
				<div class="i1"></div>
			</div>
			<table id="all_bacnet_objects_cfg_listEm" style="width: 70%; ">
			<thead>
				<tr>
					<th align="center" style="width: 5%" hidden="true" ></th>
					<th align="center" style="width: 9%" ></th>
					<th align="center" style="width: 2%" ></th>
					<th align="center" style="width: 25%" ></th>
				</tr>
			</thead>
			
			<c:forEach items="${bocEmList}" var="bocEm">
				<tr>
					<td hidden="true" >${bocEm.id}</td>	
					<td style="text-align: right">
						<c:if test="${bocEm.isvalidobject == 'y'}">
							<input type="checkbox" checked="checked" id="isvalidobject_${bocEm.id}" <c:if test="${totalNoOfEmBaseLicenses <= 0}"> disabled </c:if> name="isvalidobject_${bocEm.id}" >
						</c:if>
						<c:if test="${bocEm.isvalidobject == 'n'}">
							<input type="checkbox" id="isvalidobject_${bocEm.id}" <c:if test="${totalNoOfEmBaseLicenses <= 0}"> disabled </c:if> name="isvalidobject_${bocEm.id}" >
						</c:if>
					 </td>
					 <td></td>
					 <td style="text-align: left"><b>${bocEm.bacnetobjectdescription}</b></td>
				</tr>
			</c:forEach>
			</table>
			
			<div class="outerPointContainer">
				<span><spring:message code="bacnet.label.areasubheader" /></span>
				<div class="i1"></div>
			</div>
			
			<table id="all_bacnet_objects_cfg_listArea"  style="width: 70%; ">
			<thead>
				<tr>
					<th align="center" style="width: 5%" hidden="true" ></th>
					<th align="center" style="width: 9%" ></th>
					<th align="center" style="width: 2%" ></th>
					<th align="center" style="width: 25%" ></th>
				</tr>
			</thead>
			<c:forEach items="${bocAreaList}" var="bocArea">
				<tr>
					<td hidden="true" >${bocArea.id}</td>	
					<td style="text-align: right">
						<c:if test="${bocArea.isvalidobject == 'y'}">
							<input type="checkbox" checked="checked" id="isvalidobject_${bocArea.id}" <c:if test="${totalNoOfEmGroupPointBaseLicenses <= 0}"> disabled </c:if> name="isvalidobject_${bocArea.id}" >
						</c:if>
						<c:if test="${bocArea.isvalidobject == 'n'}">
							<input type="checkbox" id="isvalidobject_${bocArea.id}" <c:if test="${totalNoOfEmGroupPointBaseLicenses <= 0}"> disabled </c:if> name="isvalidobject_${bocArea.id}" >
						</c:if>
					 </td>
					 <td></td>
					 <td style="text-align: left"><b>${bocArea.bacnetobjectdescription}</b></td>
				</tr>
			</c:forEach>
			</table>
			</div>
			
			<div class="i1"></div>
			<div class="outerPointContainer">
				<span><spring:message code="bacnet.label.individualheader" /></span>
				<div class="i1"></div>
			</div>
			<div class="upperdiv" style="margin: 5px; padding: 5px;  ">
			<div class="outerPointContainer">
				<span><spring:message code="bacnet.label.fixsubheader" /></span>
				<div class="i1"></div>
			</div>
			
			<table id="all_bacnet_objects_cfg_listFixture"  style="width: 70%; ">
			<thead>
				<tr>
					<th align="center" style="width: 5%" hidden="true" ></th>
					<th align="center" style="width: 9%" ></th>
					<th align="center" style="width: 2%" ></th>
					<th align="center" style="width: 25%" ></th>
				</tr>
			</thead>
			<c:forEach items="${bocFixtureList}" var="bocFixture">
				<tr>
					<td hidden="true" >${bocFixture.id}</td>	
					<td style="text-align: right">
						<c:if test="${bocFixture.isvalidobject == 'y'}">
							<input type="checkbox" checked="checked" id="isvalidobject_${bocFixture.id}" <c:if test="${totalNoOfEmSensorPointBaseLicenses <= 0}"> disabled </c:if> name="isvalidobject_${bocFixture.id}" >
						</c:if>
						<c:if test="${bocFixture.isvalidobject == 'n'}">
							<input type="checkbox" id="isvalidobject_${bocFixture.id}" <c:if test="${totalNoOfEmSensorPointBaseLicenses <= 0}"> disabled </c:if> name="isvalidobject_${bocFixture.id}" >
						</c:if>
					 </td>
					 <td></td>
					 <td style="text-align: left"><b>${bocFixture.bacnetobjectdescription}</b></td>
				</tr>
			</c:forEach>
			</table>
			<div class="outerPointContainer">
				<span><spring:message code="bacnet.label.plsubheader" /></span>
				<div class="i1"></div>
			</div>
			<table id="all_bacnet_objects_cfg_listPlugload"  style="width: 70%; ">
			<thead>
				<tr>
					<th align="center" style="width: 5%" hidden="true" ></th>
					<th align="center" style="width: 9%" ></th>
					<th align="center" style="width: 2%" ></th>
					<th align="center" style="width: 25%" ></th>
				</tr>
			</thead>
			<c:forEach items="${bocPlugloadList}" var="bocPlugload">
				<tr>
					<td hidden="true" >${bocPlugload.id}</td>	
					<td style="text-align: right">
						<c:if test="${bocPlugload.isvalidobject == 'y'}">
							<input type="checkbox" checked="checked" id="isvalidobject_${bocPlugload.id}" <c:if test="${totalNoOfEmSensorPointBaseLicenses <= 0}"> disabled </c:if> name="isvalidobject_${bocPlugload.id}" >
						</c:if>
						<c:if test="${bocPlugload.isvalidobject == 'n'}">
							<input type="checkbox" id="isvalidobject_${bocPlugload.id}" <c:if test="${totalNoOfEmSensorPointBaseLicenses <= 0}"> disabled </c:if> name="isvalidobject_${bocPlugload.id}" >
						</c:if>
					 </td>
					 <td></td>
					 <td style="text-align: left"><b>${bocPlugload.bacnetobjectdescription}</b></td>
				</tr>
			</c:forEach>
			</table>
		</div>
		<div class="field">
			<div class="formValue"><input class="saveAction" <c:if test="${totalNoOfEmBaseLicenses <= 0 && totalNoOfEmGroupPointBaseLicenses <= 0 && totalNoOfEmSensorPointBaseLicenses <= 0}"> disabled </c:if> id="submit" type="submit" onclick="savePointConfiguration();" value="<spring:message code='action.submit'/>"></input></div>
		</div>
		<%-- </form:form> --%>
	</div>
</div>

<script type="text/javascript">
var error = '<%=request.getParameter("error")%>';
var saveconfirm = '<%=request.getParameter("saveconfirm")%>';
if(error == 'save_error') {
	$("#error").html('<spring:message code="error.bacnet.save"/>');
} else {
	error = '${error}';
}
if(error == 'load_error') {
	$("#error").html('<spring:message code="error.bacnet.load"/>');
}
if(saveconfirm == 'save_success') {
	$("#saveconfirm").html('<spring:message code="bacnet.save.confirmation"/>');
}

$(function() {
	$(window).resize(function() {
		var setSize = $(window).height();
		setSize = setSize - 50;
		$(".outerContainer").css("height", setSize);
	});
});
$(".outerContainer").css("overflow", "auto");
$(".outerContainer").css("height", $(window).height() - 50);
</script>