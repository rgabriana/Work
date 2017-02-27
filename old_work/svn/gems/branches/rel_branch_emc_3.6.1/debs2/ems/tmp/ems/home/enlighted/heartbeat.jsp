<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<%
response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader("Expires", -1); //prevents caching at the proxy server
%>
</head>
<body>
<div id="systat"><span id="maintenance">N</span></div>
<div id="drstat"><span id="starttime"></span></div>
<div id="drstat"><span id="level"></span></div>
<div id="drstat"><span id="duration">-1</span></div>
<div id="drstat"><span id="drtype"></span></div>
<div id="drstat"><span id="currenttime"><%=new java.util.Date().getTime()%></span></div>
<div id="drstat"><span id="drstatuschangedtime">-1</span></div>
</body>
</html>
