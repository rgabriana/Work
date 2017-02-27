<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c'%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<style type="text/css">
	html, body{margin:5px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}
</style>

<div style="background:#fff">
	<c:if test="${mode == 'admin'}">
		<%@ include file="changeOrg.jsp" %>
	</c:if>
	
	<c:if test="${mode == 'admin'}">
		<c:if test="${page == 'campus'}">
			<div style="height:5px;"></div>
			<%@ include file="upload_emconfig_file.jsp" %>
		</c:if>
	</c:if>
	
	<c:if test="${page != 'just_area'}">
		<div id="outerContainer">		
			<div style="padding-top:15px"><strong><span><spring:message code="genericSetup.heading.name"/></span></strong></div>
			<div class="i1"></div>
			<c:if test="${page == 'campus'}">
				<%@ include file="createCampus.jsp" %>
			</c:if>
			<c:if test="${page == 'building'}">
				<%@ include file="createBuilding.jsp" %>
			</c:if>
			<c:if test="${page == 'floor'}">			
				<%@ include file="createFloor.jsp" %>			
			</c:if>
			<c:if test="${page == 'area'}">
				<%@ include file="createArea.jsp" %>
			</c:if>
		</div>
	</c:if>
</div>