<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<spring:url value="/settings/ldap/server_setting.ems" var="loadServerSetting" />
<spring:url value="/settings/ldap/login_setting.ems" var="loadLoginSetting" />
<spring:url value="/settings/ldap/advanced_setting.ems"	var="loadServerList" />

<script type="text/javascript">
	$(document).ready(function() {		
		$("#login").hide();		
		$("#server").hide();
		
		//create tabs
		$("#innercenter").tabs({
			cache : true
		});
		loadlS();
		$(".ui-layout-center").css("overflow","hidden");
	});
</script>

<script type="text/javascript">
	function loadServerSettings() {		
		loadSS();
	}

	var loadSS = function() {
		$("#login").hide();		
		$("#server").show();
		var ifr;
		ifr = document.getElementById("serverSettingFrame");
		ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = "${loadServerSetting}";
	}
	
	function loadlS() {
		$("#login").show();		
		$("#server").hide();

		var ifr;
		ifr = document.getElementById("loginSettingFrame");
		ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = "${loadLoginSetting}";
	}
	
	function loadLoginSettings() {
		loadlS();
	}
</script>

<div id="innercenter" class="outermostdiv outerContainer" style="height: 100%;">
	<ul>
		<li><a id="ls" href="#login" onclick="loadLoginSettings();"><span>Login	Settings</span></a></li>
		<li><a id="ss" href="#server" onclick="loadServerSettings();"><span>Server Settings</span></a></li>
	</ul>
	
	<div style="height:100%;padding: 0px;">
		<div id="login"  style="height: 100%;">
			<iframe frameborder="0" id="loginSettingFrame" style="width: 100%; height: 100%;"></iframe>
		</div>
		<div id="server" style="height: 100%;">
			<iframe frameborder="0" id="serverSettingFrame"	style="width: 100%; height: 100%;"></iframe>
		</div>		
	</div>
</div>