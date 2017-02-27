<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<div class="outermostdiv">
	
	<div class="outerContainer">
		<span><spring:message code="header.system.cleanup" /></span>
		<div class="i1"></div>
	</div>

	<div class="upperdiv"
		style="height: 35px; margin: 10px; padding: 10px;">
		<spring:url value="/settings/cleancache.ems" var="cleancache"
			scope="request" />
		<form action="${cleancache}">
			<input type="submit"
				value='<spring:message	code="system.cleanup.label.cleancache" />' />
		</form>
	</div>
	<div class="outerContainer">
		<span><spring:message code="header.system.masterGemsSetting" /></span>
		<div class="i1"></div>
	</div>

	<div class="upperdiv"
		style="height: 35px; margin: 10px; padding: 10px;">
		<spring:url value="/settings/master_gems_setting.ems"
			var="masterGemsSetting"  />
		<form action="${masterGemsSetting}">
			<input type="submit" value='<spring:message	code="system.cleanup.label.masterGemsSetting" />' />
		</form>
	</div>

</div>