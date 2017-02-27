<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<spring:url value="/sites/details.ems" var="details" />
<spring:url value="/sites/loadsitesemlist.ems" var="loadsitesemlistUrl" />
<spring:url value="/sites/viewsiteanoaliesdetails.ems" var="loadsitesAnomalylistUrl" />
<style type="text/css">
	.no-close .ui-dialog-titlebar-close {display: none }
</style>
<script type="text/javascript">
$(document).ready(function() {
	//create tabs
	$("#innercenter").tabs({
		cache: true
	});
	$("#lisitedetails").show();
	$('#lisiteemdetails').show();
	$('#lisiteanomalydetails').show();
	$("#sitedetails").click();
	$(".ui-layout-center").css("overflow","hidden");
	//Chrome issue : Page gets scrambled when dialog is opened mostly occures when used in Tab structure layout- Fixed 
	var evt = document.createEvent('UIEvents');
	evt.initUIEvent('resize', true, false,window,0);
	window.dispatchEvent(evt);
});
function loadSiteDetails() {
	$("#tab_sitedetails").show();		
	$("#tab_siteemdetails").hide();
	$("#tab_siteanomalydetails").hide();
    var ifr;
    ifr = document.getElementById("siteDetailsFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
   	ifr.src="${details}?siteId=${siteId}&ts="+new Date().getTime();
    return false;
}
function loadSiteEmDetails() {
	$("#tab_sitedetails").hide();
	$("#tab_siteanomalydetails").hide();
	$("#tab_siteemdetails").show();
    var ifr;
    ifr = document.getElementById("siteEmDetailFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src = "${loadsitesemlistUrl}?siteId=${siteId}&ts="+new Date().getTime();
    return false;
}
function loadSiteAmonalyDetails() {
	$("#tab_sitedetails").hide();		
	$("#tab_siteemdetails").hide();
	$("#tab_siteanomalydetails").show();		
    var ifr;
    ifr = document.getElementById("siteAnomalyDetailFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src = "${loadsitesAnomalylistUrl}?siteId=${siteId}&ts="+new Date().getTime();
    return false;
}
</script>
<div id="innercenter" class="ui-layout-center outermostdiv outerContainer" >
	<div id="divform" style="float: right;padding: 5px 5px 5px 5px;"><div id="form1"><form action="list.ems" method=POST name="form1" ><input type="hidden" name="customerId" value ="${customerId}" ></input><button onclick="document.form1.submit();">Site List</button></form></div></div><br>
	&nbsp;&nbsp;
	<div style="min-height:5px"></div>
	<div>
	<ul>		
        <li id="lisitedetails"  style="display:none"><a id="sitedetails" href="#tab_sitedetails" onclick="loadSiteDetails();"><span>Details</span></a></li>
		<li id="lisiteemdetails" style="display:none"><a id="siteemdetails" href="#tab_siteemdetails" onclick="loadSiteEmDetails();"><span>Energy Managers</span></a></li>
		<li id="lisiteanomalydetails" style="display:none"><a id="siteanomalydetails" href="#tab_siteanomalydetails" onclick="loadSiteAmonalyDetails();"><span>Site Anomalies</span></a></li>
	</ul>
	</div>
	<div class="ui-layout-content ui-widget-content ui-corner-bottom" style="border-left: 0; border-top: 0; padding: 0px; width: 100%; height: 100%;">
		<div id="tab_sitedetails" class="pnl_rht"><iframe frameborder="0" id="siteDetailsFrame" style="width: 100%; height: 100%;"></iframe></div>
		<div id="tab_siteemdetails" class="pnl_rht"><iframe frameborder="0" id="siteEmDetailFrame" style="width: 100%; height: 100%;"></iframe></div>
		<div id="tab_siteanomalydetails" class="pnl_rht"><iframe frameborder="0" id="siteAnomalyDetailFrame" style="width: 100%; height: 100%;"></iframe></div>
	</div>		
</div>