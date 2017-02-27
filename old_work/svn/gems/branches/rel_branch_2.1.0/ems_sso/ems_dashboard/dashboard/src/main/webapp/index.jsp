<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head>
        <title>Enlighted Inc</title>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
        <script type="text/javascript" src="scripts/swfobject.js"></script>
        <script type="text/javascript">
            swfobject.embedSWF('modules/EnergySummaryDashBoard.swf', 'dashboard', '100%', '100%', '9.0.45', 'swfobject/expressinstall.swf', {}, {}, {});
        </script>
        <link type="text/css" rel="stylesheet" href="themes/default/css/enlighted.css" />
        <style type="text/css">
            
            html, body, #dashboard {
                height: 100%;
                overflow: hidden;
            }
        </style>  
    </head>
    
    <body><div>
	<table cellpadding="0" cellspacing="0" width=100% style="font-size: 1.3em;">
		<tr>
			<td align="left" width=45%>
				<span id="elfgem" style="valign:middle; font-weight: bold; font-size: 20px; color: green"><spring:message code="header.title"/></span>
				<span style="valign:middle; font-weight: bold; font-size: 14px;"><ems:showAppVersion/></span>
				<span id="divCurrentTime" style="valign:middle; font-weight: bold; font-size: 12px;">
				<c:set var="now" value="<%=new java.util.Date()%>"/>
				(<spring:message code="header.current.server.time"/>&nbsp;<fmt:formatDate type="both" pattern="yyyy-MM-dd HH:mm a" dateStyle="short"
				 timeStyle="short" value="${now}"/></span>)
			</td>
			<td align="center" style="font-weight: bold; font-size: 14px; width: 10%;">
				<span id="welcome"><spring:message code="header.welcome"/>&nbsp;<sec:authentication property="principal.username" /></span>
			</td>
			
			<td align="right" width=45%>
				<div id="headerActionContainer">
					<ul id="headerActions"  style="margin: 5px;">
						
						<li>
						<spring:url value="/j_spring_security_logout" var="logout_url" />
						<a href="${logout_url}"><spring:message code="header.logout"/></a></li>
					</ul>
				</div>
			</td>
		</tr>
	</table>
</div>
    
        <div id="dashboard">
            
        </div>
    </body>
    </html>