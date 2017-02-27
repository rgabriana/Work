<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<spring:url value="/appinstance/listDetails.ems" var="details" />

<spring:url value="/appinstance/loadAppBrowseSetting.ems" var="loadAppBrowseSetting" />

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
	$("#tab_browse").hide();
    var ifr;
    ifr = document.getElementById("detailsFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
   	ifr.src="${details}?appInstanceId=${appInstanceId}&ts="+new Date().getTime();
    return false;
}

function loadBrowse() {
	$("#tab_details").hide();	
	$("#tab_browse").show();
    var ifr;
    ifr = document.getElementById("stateBrowse");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src = "${loadAppBrowseSetting}?appInstanceId=${appInstanceId}&ts="+new Date().getTime();
    return false;
}


</script>

<div id="innercenter" class="ui-layout-center outermostdiv outerContainer" >
	<div id="divform" style="float: right;padding: 5px 5px 5px 5px;"><div id="form1"><form action="list.ems" method=POST name="form1" ><input type="hidden" name="customerId" value ="${customerId}" ></input><button onclick="document.form1.submit();">App Instance List</button></form></div></div><br>
	&nbsp;&nbsp;
	<div style="min-height:5px"></div>
	<div>
	<ul>		
				 
        <li id="lidetails"  style="display:none"><a id="details" href="#tab_details" onclick="loadDetails();"><span>Details</span></a></li>		
		<li id="listbrowse" style="display:none"><a id="state" href="#tab_browse" onclick="loadBrowse();"><span>Browse App Instance</span></a></li>
		
	</ul>
	</div>
	<div class="ui-layout-content ui-widget-content ui-corner-bottom" style="border-left: 0; border-top: 0; padding: 0px; width: 100%; height: 100%;">
		
		<div id="tab_details" class="pnl_rht"><iframe frameborder="0" id="detailsFrame" style="width: 100%; height: 100%;"></iframe></div>		
		<div id="tab_browse" class="pnl_rht"><iframe frameborder="0" id="stateBrowse" style="width: 100%; height: 100%;"></iframe></div>
	</div>		
</div>