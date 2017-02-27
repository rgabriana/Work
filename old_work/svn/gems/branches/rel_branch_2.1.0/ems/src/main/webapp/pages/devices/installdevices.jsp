<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<spring:url value="/devices/gateways/manage.ems" var="gatewayList" />
<spring:url value="/devices/fixtures/manage.ems" var="fixtureList" />
<spring:url value="/devices/switches/manage.ems" var="switchList" />
<spring:url value="/devices/groups/manage.ems" var="groupList" />

<script type="text/javascript">
	$(document).ready(function() {
		//var innerLayout;
		//innerLayout = $('div.pane-center').layout( layoutSettings_Inner );
		var innertabselected = $.cookie('innertabselected');
		//create tabs
		$("#innerdevicecenter").tabs();
		
		if(innertabselected == 'fixtures') { $("#fixtures,#innerdevicecenter").click(); }
		else if(innertabselected == 'switches') { $("#switches,#innerdevicecenter").click(); }
		else if(innertabselected == 'groups') { $("#groups,#innerdevicecenter").click(); }
		else manageGateways();		
	});	
</script>

<script type="text/javascript">
	
	function manageGateways() {
		$.cookie('innertabselected', 'gateways',  {path: '/' });
		var ifr;
		ifr = document.getElementById("gatewaysFrame");
		ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = "${gatewayList}";
		
		return false;
	}
	function manageFixtures() {
		$.cookie('innertabselected', 'fixtures',  {path: '/' });
		var ifr;
		ifr = document.getElementById("fixturesFrame");
		ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = "${fixtureList}";
		
		return false;
	}
	function manageSwitches() {
		$.cookie('innertabselected', 'switches',  {path: '/' });
		var ifr;
		ifr = document.getElementById("switchesFrame");
		ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = "${switchList}";
		return false;
	}
	
	function manageGroups() {
		$.cookie('innertabselected', 'groups',  {path: '/' });
		var ifr;
		ifr = document.getElementById("groupsFrame");
		ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = "${groupList}";
		return false;
	}
</script>

<style type="text/css">
	html, body{margin:3px 3px 0px 3px !important; padding:0px !important; background: #ffffff; overflow:hidden !important;}	
</style>

<div id="innerdevicecenter" class="ui-layout-center pnl_rht_inner">
	<ul
		style="-moz-border-radius-bottomleft: 0; -moz-border-radius-bottomright: 0;">
		<li><a id="gateways" href="#tab_gateways"
			onclick="manageGateways();"><span>Gateways</span> </a></li>
		<li><a id="fixtures" href="#tab_fixtures"
			onclick="manageFixtures();"><span>Fixtures</span> </a></li>
		<li><a id="switches" href="#tab_switches"
			onclick="manageSwitches();"><span>Switches</span> </a>
		</li>
		<li><a id="groups" href="#tab_groups"
			onclick="manageGroups();"><span>Groups</span> </a>
		</li>
	</ul>
	<div class="ui-layout-content ui-widget-content ui-corner-bottom"
		style="border-left: 0; border-top: 0; padding: 0px;">
		<div id="tab_gateways" class="pnl_rht">
			<iframe frameborder="0" id="gatewaysFrame"
				style="width: 100%; height: 100%;"></iframe>
				<script type="text/javascript">		
				$(function() {
					$(window).resize(function() {
						$("#gatewaysFrame").css("height", $(window).height() - 40);
					});
				});
				$("#gatewaysFrame").css("height", $(window).height() - 40);
				</script>
		</div>
		<div id="tab_fixtures" class="pnl_rht"
			style="width: 100%; height: 100%;">
			<iframe frameborder="0" id="fixturesFrame"
				style="width: 100%; height: 100%;"></iframe>
				<script type="text/javascript">		
				$(function() {
					$(window).resize(function() {
						$("#fixturesFrame").css("height", $(window).height() - 40);
					});
				});
				$("#fixturesFrame").css("height", $(window).height() - 40);
				</script>
		</div>
		<div id="tab_switches" class="pnl_rht">
			<iframe frameborder="0" id="switchesFrame"
				style="width: 100%; height: 100%;"></iframe>
				<script type="text/javascript">		
				$(function() {
					$(window).resize(function() {
						$("#switchesFrame").css("height", $(window).height() - 40);
					});
				});
				$("#switchesFrame").css("height", $(window).height() - 40);
				</script>
		</div>
		<div id="tab_groups" class="pnl_rht">
			<iframe frameborder="0" id="groupsFrame"
				style="width: 100%; height: 100%;"></iframe>
				<script type="text/javascript">		
				$(function() {
					$(window).resize(function() {
						$("#groupsFrame").css("height", $(window).height() - 40);
					});
				});
				$("#groupsFrame").css("height", $(window).height() - 40);
				</script>
		</div>
	</div>

</div>

