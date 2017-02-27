<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>

<spring:url value="/imageupgrademanage/imageFileUpload.ems" var="imageUploadUrl" />

<spring:url value="/imageupgrademanage/imageSchedule.ems" var="imageScheduleUrl" />

<spring:url value="/imageupgrademanage/imageJob.ems" var="imageJobUrl" />

<spring:url value="/imageupgrademanage/imageStatus.ems" var="imageStatusUrl" />

<style type="text/css">
	.no-close .ui-dialog-titlebar-close {display: none }
</style>

<script type="text/javascript">

var tabselected;

var jobName = "";

$(document).ready(function() {
	
	//create tabs
	$("#innercenter").tabs({
		cache: true
	});	
    
	$("#liimageupload").show();
	$('#liimageschedule').show();
	$('#liimagejob').show();
	$('#liimagestatus').show();
	
	if($.browser.msie && parseInt($.browser.version) == 8){
		
		$(window).bind('resize', function() {
		}).trigger('resize');
	
	}
	else{
		//Chrome issue  - Fix 
		var evt = document.createEvent('UIEvents');
	    evt.initUIEvent('resize', true, false,window,0);
	    window.dispatchEvent(evt);
	    //Chrome issue ENL 2667
	}
	
	$("#imageupload").click();
	
	$(".ui-layout-center").css("overflow","hidden");
	
});

function loadImageUpload() {
	tabselected = 'imageupload';
	$("#tab_imageupload").show();		
	$("#tab_imageschedule").hide();
	$("#tab_imagejob").hide();
	$("#tab_imagestatus").hide();
	
    var ifr;
    ifr = document.getElementById("imageuploadFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
   	ifr.src="${imageUploadUrl}?ts="+new Date().getTime();
    return false;
}

function loadImageSchedule() {
	tabselected = 'imageschedule';
	$("#tab_imageschedule").show();
	$("#tab_imagejob").hide();
	$("#tab_imageupload").hide();
	$("#tab_imagestatus").hide();
	
    var ifr;
    ifr = document.getElementById("imagescheduleFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src = "${imageScheduleUrl}?ts="+new Date().getTime();
    return false;
}
function loadImageJob() {
	tabselected = 'imagejob';
	$("#tab_imagejob").show();		
	$("#tab_imageupload").hide();
	$("#tab_imageschedule").hide();
	$("#tab_imagestatus").hide();
	
    var ifr;
    ifr = document.getElementById("imagejobFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src = "${imageJobUrl}?ts="+new Date().getTime();
    return false;
}

function loadImageStatus() {
	tabselected = 'imagestatus';
	$("#tab_imagestatus").show();		
	$("#tab_imageupload").hide();
	$("#tab_imageschedule").hide();
	$("#tab_imagejob").hide();
	
    var ifr;
    ifr = document.getElementById("imagestatusFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src = "${imageStatusUrl}?ts="+new Date().getTime();
    return false;
}
</script>

<div id="innercenter" class="ui-layout-center outermostdiv outerContainer" >
	<ul>		
				 
        <security:authorize access="hasAnyRole('Admin')">
				<li id="liimageupload"  style="display:none"><a id="imageupload" href="#tab_imageupload" onclick="loadImageUpload();"><span>Upload Image</span></a></li>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin')">
			<li id="liimageschedule" style="display:none"><a id="imageschedule" href="#tab_imageschedule" onclick="loadImageSchedule();"><span>Schedule Image</span></a></li>
		</security:authorize>
		<security:authorize access="hasAnyRole('Admin')">
			<li id="liimagejob" style="display:none"><a id="imagejob" href="#tab_imagejob" onclick="loadImageJob();"><span>Image Job History</span></a></li>
		</security:authorize>
		<security:authorize access="hasAnyRole('Admin')">
			<li id="liimagestatus" style="display:none"><a id="imagestatus" href="#tab_imagestatus" onclick="loadImageStatus();"><span>Device Status</span></a></li>
		</security:authorize>				
	</ul>
	<div class="ui-layout-content ui-widget-content ui-corner-bottom" style="border-left: 0; border-top: 0; padding: 0px; width: 100%; height: 100%;">
		
		<security:authorize access="hasAnyRole('Admin')">
			<div id="tab_imageupload" class="pnl_rht"><iframe frameborder="0" id="imageuploadFrame" style="width: 100%; height: 95%;"></iframe></div>
		</security:authorize>
		
		<security:authorize access="hasAnyRole('Admin')">
			<div id="tab_imageschedule" class="pnl_rht"><iframe frameborder="0" id="imagescheduleFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>
		<security:authorize access="hasAnyRole('Admin')">
			<div id="tab_imagejob" class="pnl_rht"><iframe frameborder="0" id="imagejobFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>
		<security:authorize access="hasAnyRole('Admin')">
			<div id="tab_imagestatus" class="pnl_rht"><iframe frameborder="0" id="imagestatusFrame" style="width: 100%; height: 100%;"></iframe></div>
		</security:authorize>				
		
	</div>		
</div>