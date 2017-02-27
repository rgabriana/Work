<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<spring:url value="/settings/ballastlist.ems" var="ballast" />
<spring:url value="/settings/bulbslist.ems" var="bulb" />
<spring:url value="/settings/fixtureclasslist.ems" var="fixtureClassList" />



<script type="text/javascript">
	$(document).ready(function() {
		//var innerLayout;
		//innerLayout = $('div.pane-center').layout( layoutSettings_Inner );
		//var innertabselected = $.cookie('innertabselected');
		//create tabs
		$("#innerfixtureclasscenter").tabs();
		
		//Click on ballasts tab
		$("#ballasts,#innerfixtureclasscenter").click();		
		
	});	
</script>

<script type="text/javascript">
	
	function manageBallast() {
		//$.cookie('innertabselected', 'gateways',  {path: '/' });
		var ifr;
		ifr = document.getElementById("ballastFrame");
		ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = "${ballast}?ts="+new Date().getTime();
		
		return false;
	}
	function manageBulb() {
		//$.cookie('innertabselected', 'fixtures',  {path: '/' });
		var ifr;
		ifr = document.getElementById("bulbFrame");
		ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = "${bulb}?ts="+new Date().getTime();
		
		return false;
	}
	function manageFixtureClass() {
		//$.cookie('innertabselected', 'switches',  {path: '/' });
		var ifr;
		ifr = document.getElementById("fixtureclassFrame");
		ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = "${fixtureClassList}?ts="+new Date().getTime();
		return false;
	}	
	
	$(window).bind('resize', function() {
		$("#innerfixtureclasscenter").css("height", ($(window).height() - 40));
	}).trigger('resize');
	
</script>

<style type="text/css">
	html, body{margin:3px 3px 0px 3px !important; padding:0px !important; background: #ffffff; overflow:hidden !important;}	
</style>

<div id="innerfixtureclasscenter" class="ui-layout-center pnl_rht_inner">
	<ul
		style="-moz-border-radius-bottomleft: 0; -moz-border-radius-bottomright: 0;">
		<li><a id="ballasts" href="#tab_ballast"
			onclick="manageBallast();"><span>Ballasts</span> </a></li>
		<!--
		<li><a id="bulbs" href="#tab_bulb"
			onclick="manageBulb();"><span>Bulbs</span> </a></li>
		<li><a id="fixtureclasses" href="#tab_fixtureclass"
			onclick="manageFixtureClass();"><span>Fixture Class</span> </a>
		</li>
		-->
	</ul>
	<div class="ui-layout-content ui-widget-content ui-corner-bottom"
		style="border-left: 0; border-top: 0; padding: 0px;">
		<div id="tab_ballast" class="pnl_rht">
			<iframe frameborder="0" id="ballastFrame"
				style="width: 100%; height: 100%;"></iframe>
				<script type="text/javascript">		
				$(function() {
					$(window).resize(function() {
						//$("#ballastFrame").css("height", $(window).height() - 50);
					});
				});
				$("#ballastFrame").css("height", $(window).height() - 40);
				</script>
		</div>
		<!--
		<div id="tab_bulb" class="pnl_rht"
			style="width: 100%; height: 100%;">
			<iframe frameborder="0" id="bulbFrame"
				style="width: 100%; height: 100%;"></iframe>
				<script type="text/javascript">		
				$(function() {
					$(window).resize(function() {
						//$("#bulbFrame").css("height", $(window).height() - 50);
					});
				});
				$("#bulbFrame").css("height", $(window).height() - 40);
				</script>
		</div>
		<div id="tab_fixtureclass" class="pnl_rht">
			<iframe frameborder="0" id="fixtureclassFrame"
				style="width: 100%; height: 100%;"></iframe>
				<script type="text/javascript">		
				$(function() {
					$(window).resize(function() {
						//$("#fixtureclassFrame").css("height", $(window).height() - 40);
					});
				});
				$("#fixtureclassFrame").css("height", $(window).height() - 40);
				</script>
		</div>
		-->
	</div>
</div>

