<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/services/org/gemsgroupfixture/op/resetallgroups" var="resetFixtureGroupsUrl" scope="request" />
<spring:url value="/services/org/gemsgroups/op/deleteemptygroups" var="deleteEmptyGroupsUrl" scope="request" />

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">

<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;}
</style>


<script type="text/javascript">

var pollResetServer;
$(document).ready(function() {
	var dataXML = "";
	if(SELECTED_FIXTURES_TO_RESET_GROUPS != undefined && SELECTED_FIXTURES_TO_RESET_GROUPS.length > 0) {
		$.each(SELECTED_FIXTURES_TO_RESET_GROUPS, function(){
			dataXML += "<fixture><id>"+ this.id +"</id></fixture>";
		 });
	}
	dataXML = "<fixtures>"+dataXML+"</fixtures>";
	setResetGemsGroupMessage("Processing...", "black");
	$.when(pollResetStatus(0), resetFixtureGroups(dataXML));
});


function resetFixtureGroups(dataXML){
	

	
 	$.ajax({
 		type: 'POST',
 		url: "${resetFixtureGroupsUrl}"+"?ts="+new Date().getTime(),
 		data: dataXML,
 		success: function(data){
	 			clearInterval(pollResetServer);
				if(data != undefined && data != "") {
					var parts = data.split(',');
					Dtotal = Number(parts[3]);
					Dproc = Number(parts[4]);
					Dsuccess = Number(parts[5]);
					out = "";
					if(Dtotal > 0) {
						if(Dproc == Dsuccess) {
							out = "Processed " + Dproc + " of " + Dtotal + " reset requests. ";	
						}
						else {
							failed = Dproc - Dsuccess;
							out = "Processed " + Dproc + " of " + Dtotal + " reset requests (" + failed + " failed). ";
						}
						
					}
					if(Dproc > 0) {
						setResetGemsGroupMessage(out, "green");
					}
					
					//To delete all Empty Groups in the Floor ( Groups which doesn't contain any fixtures)
					$.ajax({
						type: 'POST',
						url: "${deleteEmptyGroupsUrl}?ts="+new Date().getTime(),
						success: function(data){
		 					
						},
						dataType : "json",
						contentType : "application/xml; charset=utf-8"
					});
					
				}
		},
		error: function(){
			clearInterval(pollResetServer);
			setResetGemsGroupMessage("Could not process your request due to some internal error.", "red");
		},
 		dataType:"html",
 		contentType: "application/xml; charset=utf-8",
 	});
}

function setResetGemsGroupMessage(msg, color){
	$("#reset-message-div").css("color", color);
	$("#reset-message-div").html(msg);
}


function pollResetStatus(gid) {

	pollResetServer = setInterval(
			function() {
				$.ajax({
						type : "POST",
						cache : false,
						dataType : "html",
						url : '<spring:url value="/services/org/gemsgroups/status/"/>' + gid,
						success : function(msg) {
							if(msg != undefined && msg != "") {
								var parts = msg.split(',');
								Dtotal = Number(parts[3]);
								Dproc = Number(parts[4]);
								Dsuccess = Number(parts[5]);
								out = "";
								if(Dtotal > 0) {
									if(Dproc == Dsuccess) {
										out = "Processed " + Dproc + " of " + Dtotal + " reset requests. ";	
									}
									else {
										failed = Dproc - Dsuccess;
										out = "Processed " + Dproc + " of " + Dtotal + " reset requests (" + failed + " failed). ";
									}
									
								}
								if(Dproc > 0) {
									setResetGemsGroupMessage(out, "black");
								}
							}
						}
					});
			}, 5000);
}

</script>
</head>
<body>
<div>
	<div id="reset-message-div" style="font-weight:bold; float: left; padding-top: 5px; padding-left: 10px;"></div>
</div>
</body>
</html>