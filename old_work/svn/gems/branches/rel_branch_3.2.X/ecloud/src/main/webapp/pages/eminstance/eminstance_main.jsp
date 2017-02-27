<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<spring:url value="/eminstance/listDetails.ems" var="details" />

<spring:url value="/eminstance/loadEmTaskListByEmInstanceId.ems" var="loadEmTaskListByEmInstanceId" />

<spring:url value="/eminstance/loadEmEvents.ems" var="emEvents" />

<spring:url value="/eminstance/loademstats.ems" var="stats" />

<spring:url value="/emstate/loademstate.ems" var="state" />

<spring:url value="/eminstance/loadEmBrowseSetting.ems" var="loadEmBrowseSetting" />

<style type="text/css">
	.no-close .ui-dialog-titlebar-close {display: none }
</style>

<script type="text/javascript">

$(document).ready(function() {
	
	//create tabs
	$("#innercenter").tabs({
		cache: true
	});
	var browseEnable = "${showBrowse}" ;
	$("#lidetails").show();
	$('#lischeduler').show();
	$('#lievents').show();
	$('#listats').show();
	$('#listate').show();
	$('#listbrowse').hide();
	if(browseEnable=="true"){
		$('#listbrowse').show();
	}else{
		$('#listbrowse').hide();
	}
	$("#details").click();
	
	$(".ui-layout-center").css("overflow","hidden");
	
});

function loadDetails() {
	$("#tab_details").show();		
	$("#tab_scheduler").hide();
	$("#tab_events").hide();
	$("#tab_stats").hide();
	$("#tab_state").hide();
	$("#tab_browse").hide();
    var ifr;
    ifr = document.getElementById("detailsFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
   	ifr.src="${details}?emInstanceId=${emInstanceId}&ts="+new Date().getTime();
    return false;
}

function loadScheduler() {
	$("#tab_details").hide();		
	$("#tab_scheduler").show();
	$("#tab_events").hide();
	$("#tab_stats").hide();
	$("#tab_state").hide();
	$("#tab_browse").hide();
    var ifr;
    ifr = document.getElementById("schedulerFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src = "${loadEmTaskListByEmInstanceId}?emInstanceId=${emInstanceId}&ts="+new Date().getTime();
    return false;
}

function loadEvents() {
	$("#tab_details").hide();		
	$("#tab_scheduler").hide();
	$("#tab_events").show();
	$("#tab_stats").hide();
	$("#tab_state").hide();
	$("#tab_browse").hide();
    var ifr;
    ifr = document.getElementById("eventsFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src = "${emEvents}?emInstanceId=${emInstanceId}&ts="+new Date().getTime();
    return false;
}

function loadStats() {
	$("#tab_details").hide();		
	$("#tab_scheduler").hide();
	$("#tab_events").hide();
	$("#tab_stats").show();
	$("#tab_state").hide();
	$("#tab_browse").hide();
    var ifr;
    ifr = document.getElementById("statsFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src = "${stats}?emInstanceId=${emInstanceId}&ts="+new Date().getTime();
    return false;
}

function loadState() {
	$("#tab_details").hide();		
	$("#tab_scheduler").hide();
	$("#tab_events").hide();
	$("#tab_stats").hide();
	$("#tab_state").show();
	$("#tab_browse").hide();
    var ifr;
    ifr = document.getElementById("stateFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src = "${state}?emInstanceId=${emInstanceId}&ts="+new Date().getTime();
    return false;
}
function loadBrowse() {
	$("#tab_details").hide();		
	$("#tab_scheduler").hide();
	$("#tab_events").hide();
	$("#tab_stats").hide();
	$("#tab_state").hide();
	$("#tab_browse").show();
    var ifr;
    ifr = document.getElementById("stateBrowse");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src = "${loadEmBrowseSetting}?emInstanceId=${emInstanceId}&ts="+new Date().getTime();
    return false;
}


</script>

<div id="innercenter" class="ui-layout-center outermostdiv outerContainer" >
	<div id="divform" style="float: right;padding: 5px 5px 5px 5px;"><div id="form1"><form action="list.ems" method=POST name="form1" ><input type="hidden" name="customerId" value ="${customerId}" ></input><button onclick="document.form1.submit();">Em Instance List</button></form></div></div><br>
	&nbsp;&nbsp;
	<div style="min-height:5px"></div>
	<div>
	<ul>		
				 
        <li id="lidetails"  style="display:none"><a id="details" href="#tab_details" onclick="loadDetails();"><span>Details</span></a></li>
		<li id="lischeduler" style="display:none"><a id="scheduler" href="#tab_scheduler" onclick="loadScheduler();"><span>Scheduler</span></a></li>
		<li id="lievents" style="display:none"><a id="events" href="#tab_events" onclick="loadEvents();"><span>Events</span></a></li>
		<li id="listats" style="display:none"><a id="stats" href="#tab_stats" onclick="loadStats();"><span>Health Stats</span></a></li>
		<li id="listate" style="display:none"><a id="state" href="#tab_state" onclick="loadState();"><span>State</span></a></li>
		<li id="listbrowse" style="display:none"><a id="state" href="#tab_browse" onclick="loadBrowse();"><span>Browse Energy Manager</span></a></li>
		
	</ul>
	</div>
	<div class="ui-layout-content ui-widget-content ui-corner-bottom" style="border-left: 0; border-top: 0; padding: 0px; width: 100%; height: 100%;">
		
		<div id="tab_details" class="pnl_rht"><iframe frameborder="0" id="detailsFrame" style="width: 100%; height: 100%;"></iframe></div>
		<div id="tab_scheduler" class="pnl_rht"><iframe frameborder="0" id="schedulerFrame" style="width: 100%; height: 100%;"></iframe></div>
		<div id="tab_events" class="pnl_rht"><iframe frameborder="0" id="eventsFrame" style="width: 100%; height: 100%;"></iframe></div>
		<div id="tab_stats" class="pnl_rht"><iframe frameborder="0" id="statsFrame" style="width: 100%; height: 100%;"></iframe></div>
		<div id="tab_state" class="pnl_rht"><iframe frameborder="0" id="stateFrame" style="width: 100%; height: 100%;"></iframe></div>
		<div id="tab_browse" class="pnl_rht"><iframe frameborder="0" id="stateBrowse" style="width: 100%; height: 100%;"></iframe></div>
	</div>		
</div>