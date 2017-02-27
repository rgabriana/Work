<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>
<spring:url value="/companySetup.ems" var="companysetup"/>
<html>
<head>
<spring:url value="/themes/default/css/style.css" var="stylecss" />
<link rel="stylesheet" type="text/css" href="${stylecss}" />
<spring:url
	value="/themes/standard/css/jquery/jquery-ui-1.8.16.custom.css"
	var="jquerycss" />
<link rel="stylesheet" type="text/css" href="${jquerycss}" />

<spring:url value="/scripts/jquery/jquery.1.6.4.min.js" var="jquery"></spring:url>
<script type="text/javascript" src="${jquery}"></script>
<spring:url value="/scripts/jquery/jquery.cookie.20110708.js"
	var="jquery_cookie"></spring:url>
<script type="text/javascript" src="${jquery_cookie}"></script>

<spring:url value="/scripts/jquery/jquery.ui.1.8.16.custom.min.js"
	var="jqueryui"></spring:url>
<script type="text/javascript" src="${jqueryui}"></script>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title><spring:message code="title" /></title>

<style type="text/css">

.promptRestore {
	background: url(../themes/default/images/background-big.jpg) #f4f4f4 fixed no-repeat center top; 
	z-index: -1;
	width:100%;
}
.restorebackupPrompt
{
	width: 100%; 
	height: 530px; 	
	position: relative;
	top: 10px;
	margin-left:auto;
	margin-right:auto;
	border: 1px solid #e0e0e0;
}
.infoMsg {
	font-weight: bold;font-size:12px;
}
.promptPageUrls
{
	background-color:#EBECEC;
	color:#FFFFFF;
	border:1px solid gray; 
	font-family:arial,sans-serif;
	cursor:pointer;
}
.promptPageHeader
{
	margin:0px;color:#000;
	font-size:1.1em;font-weight:bold;padding:20px 10px 6px 10px;margin-bottom:5px;
}
.backuptable {	
	padding-top: 10px;
	clear: both;
	width: 100%;
	overflow:auto;
	overflow-x:hidden;
	max-height: 380px;
}
.buttonBar
{
	padding-top: 10px;
	padding-left: 10px;
}
</style>
<script type="text/javascript">
$().ready(function() {
	$(function() {
		$(window).resize(function() {
			var setSize = $(window).height();
			setSize = setSize - 30;
			$(".promptRestore").css("height", setSize);
		});
	});
	$(".promptRestore").css("height", $(window).height() - 30);
	
});
$(document).ready(function() {
	//define configurations for dialog
	var dialogOptions = {
		title : "Enlighted Energy Manager",
		modal : true,
		autoOpen : false,
		height : 300,
		width : 500,
		draggable : true
	}

	$("#notesDialog").dialog(dialogOptions);

	$('#contactUs').click(function() {
		$("#notesDialog").dialog('open');
		return false;
	});
	
});

$(document).ready(function(){
	var url = '${companysetup}';
	$("#companySetup").attr("href",url);
});


</script>
</head>
<body>

<div id="notesDialog">
		<table border="0" cellpadding="0" cellspacing="5">
			<tr>
				<td><b>Corporate Headquarters:</b></td>
			</tr>
			<tr>
				<td>930 Benecia Avenue<br />
					Sunnyvale, CA 94085<br /> Phone: 650.964.1094<br /> Fax:
					650.964.1094<br /> Email: info@enlightedinc.com
				</td>
			</tr>
			<tr>
				<td style="padding-top:5px;"><b>For Sales:</b></td>
			</tr>
			<tr>
				<td>Email: sales@enlightedinc.com<br /> Phone:650.964.1155</td>
			</tr>
			<tr>
				<td style="padding-top:5px;"><b>For Customer Service:</b></td>
			</tr>
			<tr>
				<td>Email: support@enlightedinc.com<br />
					Phone: 650.964.1155 or 866-377-4111
				</td>
			</tr>		
		</table>
	</div>
	<div class="promptRestore">
		<div align="left">
			<img id="enlogo" src="../themes/default/images/enlighted-logo.png"	alt="" />
		</div>
		<div class="restorebackupPrompt">
			
			<spring:url value="/doc/EULA.pdf" var="eula" />
			
			<div class="promptPageHeader">Backup Restore :</div>

			<div style="padding-top: 10px; padding-left: 10px;">You have the following back up files available in your USB stick which are eligible for restore.</div>
			
			<div style="padding-top: 10px; padding-left: 10px;">Would you like to restore them or proceed with creating a new Organization</div>
			
			<div id="tableContainer" class="backuptable">
					<table id="backuplisttable" class="entable" width="100%">
						<thead>
							<tr>
								<th><spring:message
										code="restore.label.list.last.modified.time" /></th>
								<th><spring:message code="restore.label.list.file" /></th>
								<th><spring:message code="restore.label.list.file.size" /></th>
							</tr>
						</thead>
						<c:set var="count" value="${-1}" scope="request" />
						<tbody>
							<c:forEach items="${backups}" var="backup">
								<c:set var="count" value="${count+1}" scope="request" />
								<tr id=<c:out value="${count}"/>Trow >
									<td id=<c:out value="${count}"/>Tdate ><c:out
											value="${backup.creationDate}" /></td>
									<td id=<c:out value="${count}"/>Tname ><c:out
											value="${backup.backupfileName}" /></td>
									<td id=<c:out value="${count}"/>Tsize ><c:out
											value="${backup.backupfileSize}" /></td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
			</div>
			
			<div class="buttonBar">
				<a href="#" id="backup" class="promptPageUrls" target="_blank">Proceed to backup restore page</a> &nbsp;&nbsp;
				<a href="#" id="companySetup" class="promptPageUrls">Continue setting up a new Organization </a>
			</div>
			
			<c:if test="${apacheInstalled == null}">
				<c:if test="${securityKey != null}">
					
					<script type="text/javascript">
						$(document).ready(function(){
							var backup_url = location.protocol + '//' + location.host
									+ '/ems_mgmt/home.jsp?page=backup&code=' + '${securityKey}';
							$("#backup").attr("href", backup_url);
						});
					</script>
				</c:if>
			</c:if>
			
			<c:if test="${apacheInstalled != null}">
				<script type="text/javascript">
					$(document).ready(function(){
						var backup_url = location.protocol + '//' + location.host
								+ '/em_mgmt/backuprestore/';
						$("#backup").attr("href", backup_url);
					});
				</script>
			</c:if>
			
		</div>
	</div>
	<div class="footer footerlogin">
		<div style="float: left" class="footerinner">
			<spring:message code="footer.copyright.text" />
		</div>
		<div style="float: right" class="footerinner">
			<a id="contactUs" href=""><spring:message
					code="footer.contact.us.label" /></a>
		</div>
	</div>
</body>
</html>