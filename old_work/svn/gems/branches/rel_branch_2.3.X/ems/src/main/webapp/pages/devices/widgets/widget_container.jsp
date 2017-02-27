<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<script type="text/javascript">
 	$(document).ready(function() {
		//create tabs
		$("#widgetcontainer").tabs({
			cache: true
		});
	});
</script>
<div id="widgetcontainer" class="ui-layout-center">
<ul>		
		<li><a id="swlocation" href="#tab_switch_widget"><span>Location</span></a></li>
		<li><a id="fxlist" href="#tab_fx_widget"><span>Fixtures</span></a></li>
		<li><a id="scenelist" href="#tab_scene_widget"><span>Scene</span></a></li>
		<li><a id="wdslist" href="#tab_wds_widget"><span>EWS</span></a></li>
</ul>
<div class="ui-layout-content ui-widget-content ui-corner-bottom" style="border-left: 0; border-top: 0; padding: 0px;">
		<div id="tab_switch_widget" class="pnl_rht flasharea"></div>
		<div id="tab_fx_widget" class="pnl_rht flasharea"></div>
		<div id="tab_scene_widget" class="pnl_rht flasharea"></div>
		<div id="tab_wds_widget" class="pnl_rht flasharea"></div>
</div>
</div>