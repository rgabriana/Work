<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<spring:url value="/facilities/organization/setting.ems" var="loadSetting"/>


<style type="text/css">
	.no-close .ui-dialog-titlebar-close {display: none }
</style>

<script type="text/javascript">
   	
	$(document).ready(function() {
    
		var innerLayout;
		innerLayout = $('div.pane-center').layout( layoutSettings_Inner );
		
		//create tabs
		$("#innercenter").tabs({
			cache: true
		});
		tabselected = 'settings';
		showSettings();
		nodeclick();
				
	});


var tabselected;

function nodeclick() {
	$('#facilityTreeViewDiv').treenodeclick(function(){						
		showSettings();					
	});
}

var showSettings=function(){	
		
	if (tabselected == 'settings') {
		$('#os').click();
	}
	
}

function loadSettings() {
	tempnodetype = treenodetype;
	tempnodeid = treenodeid;
	tabselected = 'settings';
	var ifr;
    ifr = document.getElementById("settingFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    ifr.src="${loadSetting}?facilityId="+tempnodeid+"&ts="+new Date().getTime();
    return false;
}

</script>

<div id="innercenter" class="ui-layout-center">
	<ul>		
		<li><a id="os" href="#settings" onclick="loadSettings();"><span id="settingstab">Settings</span></a></li>
	</ul>
	
	<div class="ui-layout-content ui-widget-content ui-corner-bottom" style="border-left: 0; border-top: 0; padding: 0px;">
		<div id="settings" class="pnl_rht"><iframe frameborder="0" id="settingFrame" style="width: 100%; height: 100%;"></iframe></div>
	</div>		
</div>
