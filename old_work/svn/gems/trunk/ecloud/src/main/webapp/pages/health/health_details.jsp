<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<spring:url value="/health/monitoring.ems" var="monitoring_dashboard" />
<spring:url value="/health/devices/details.ems" var="healthDeviceDetails" />
<style type="text/css">
	.no-close .ui-dialog-titlebar-close {display: none }
</style>

<script type="text/javascript">

$(document).ready(function() {
	
	//create tabs
	$("#innercenter").tabs({
		cache: true
	});
	
	$("#liGateways").show();
	$('#liFixtures').show();
	$("#tab_Gateways").show();
	$("#tab_Fixtures").hide();
	loadGateways();
	$(".ui-layout-center").css("overflow","hidden");
	
});

function loadGateways() {
	$("#tab_Gateways").show();		
	$("#tab_Fixtures").hide();	
    var ifr;
    ifr = document.getElementById("gatewaysHealthFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
   	ifr.src="${healthDeviceDetails}?emInstanceId=${emInstanceId}&deviceType=gateways&ts="+new Date().getTime();
    return false;
}

function loadFixtures() {
	$("#tab_Gateways").hide();		
	$("#tab_Fixtures").show();
	var ifr;
    ifr = document.getElementById("fixturesHealthFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src = "${healthDeviceDetails}?emInstanceId=${emInstanceId}&deviceType=fixtures&ts="+new Date().getTime();
    return false;
}

</script>


<div id="innercenter" class="ui-layout-center outermostdiv outerContainer" >
	<div id="divform" style="float: right;padding: 5px 5px 5px 5px;">
	<div id="form1"><form action="${monitoring_dashboard}" method="GET" name="form1" ><button onclick="document.form1.submit();">Monitoring Dashboard</button></form></div></div><br>
	&nbsp;&nbsp;
	<div style="min-height:5px"></div>
	<div>
	<ul>		
				 
        <li id="liGateways"  style="display:none"><a id="details" href="#tab_Gateways" onclick="loadGateways();"><span>Gateways</span></a></li>
		<li id="liFixtures" style="display:none"><a id="scheduler" href="#tab_Fixtures" onclick="loadFixtures();"><span>Fixtures</span></a></li>
	</ul>
	</div>
	<div class="ui-layout-content ui-widget-content ui-corner-bottom" style="border-left: 0; border-top: 0; padding: 0px; width: 100%; height: 100%;">
		
		<div id="tab_Gateways" class="pnl_rht"><iframe frameborder="0" id="gatewaysHealthFrame" style="width: 100%; height: 100%;"></iframe></div>
		<div id="tab_Fixtures" class="pnl_rht"><iframe frameborder="0" id="fixturesHealthFrame" style="width: 100%; height: 100%;"></iframe></div>		
	</div>		
</div>