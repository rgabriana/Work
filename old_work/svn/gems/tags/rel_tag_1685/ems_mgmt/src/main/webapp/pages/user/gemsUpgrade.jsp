<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/scripts/jquery/jquery.blockUI.2.39.js" var="jquery_blockUI"></spring:url>
<script type="text/javascript" src="${jquery_blockUI}"></script>

<spring:url value="/scripts/jquery/jquery.tablesorter.min.js"
	var="jquery_tablesorter"></spring:url>
<script type="text/javascript" src="${jquery_tablesorter}"></script>

<script type="text/javascript">	
	$().ready(function() {		
		$("#upload").validate({
			rules: {
				fileData: {
					required: true,
					accept: "deb"
				}
			},
			messages: {
				fileData: {
					required: '<spring:message code="error.above.field.required"/>',
					accept: "<spring:message code='error.deb.extension.required'/>"
				}
			}			
		});
		
		$(".outermostdiv").css("minHeight", $(window).height() - 85);		
	});

	function upload() {		
		var isValid = $('#upload').valid();		
		if(isValid) {	                
	        $('#uploadsubmit').attr('disabled', 'disabled');
			$("#uploadProgressDiv").show();
			var loadingImageString="<img alt='loading' src='themes/default/images/ajax-loader_small.gif'>";
	        $("#uploadProgressIndicator").html("<span>" + '<spring:message code="restore.upload.wait"/>' +loadingImageString + "</span><span id='uploadSizeIndicator'></span>");
	        var uploadPollServer = setInterval( function() {
			 	$.ajax({
					 type: "POST",
					 cache: false,
					 dataType: "html",
					 url: '<spring:url value="/services/org/upload/size"/>',
					 beforeSend: function() {
					 },
					 success: function(msg){
					 	if(msg != "-1") {
					 		$("#uploadSizeIndicator").text(msg + " KB of data is uploaded.");
					 	}
					 } 
					 });
					
					 }, 5000);
 	       	$("#upload").submit();
		}
	}	
</script>

<spring:url value="uploadImageFile.emsmgmt" var="uploadFileURL" scope="request"/>
<div class="pgset">
<div class="outermostdiv">
	<div class="outerContainer">
		<span id="userlist_header_text"><spring:message	code="upgrade.title" /></span>
		<div class="i1"></div>
		<div style="width:100%;">
		<fieldset style="padding: 10px;">
			<legend style="font-weight: bold"><spring:message code="upgrade.upload.legend"/></legend>
			
			<form:form id="upload" modelAttribute="upgradeFile" action="${uploadFileURL}" method="post" enctype="multipart/form-data">
				<div class="field">
					<div class="formPrompt"><span><spring:message code="upgrade.label.image.upload"/></span></div>
					<div class="formValue">
						<form:input path="fileData" type="file"/>
					</div>
				</div>
				<div id="uploadProgressDiv" class="field">
					<div class="formPrompt"><span></span></div>
					<div class="formValue" id="uploadProgressIndicator"></div>
				</div>
				<script type="text/javascript">
					var status = <%=request.getParameter("uploadStatus")%>;
					var filename = <%=request.getParameter("filename")%>;
					if(status == null) {
						$("#uploadProgressDiv").hide();
					}
					else {
						if(status == "F") {
							$("#uploadProgressIndicator").html("<span style='color:red'>"+ '<spring:message code="error.restore.upload.internal"/>' +"</span>");
						}
						else if(status == "S" && filename != null) {
							$("#uploadProgressIndicator").html("<span style='color:green'>File "+ filename +" was uploaded successfully.</span>");
						}
					}
				</script>
				<div class="field">
					<div class="formPrompt"><span></span></div>
					<div class="formValue">
						<input class="navigation" id="uploadsubmit" type="button" onclick="upload();" value=<spring:message code='action.upload'/> />
					</div>
				</div>
			</form:form>
		</fieldset>
		</div>
		<c:if test="${!empty fileList}">
			
			<script type="text/javascript">
				
				function deleteUpgradeFile(elementObj) {
			        var id = $(elementObj).attr('id').split('T')[0];
			        var filename = $("#" + id + "Tname").html();
			        
			       	jConfirm('Are you sure you want to delete '+filename+"?",'<spring:message code="deletion.confirmation.title"/>',function(result){
			             if(result) {
		                		$("input.action").attr('disabled', "disabled");
		                		$("#listProgressDiv").show();
		                		$("#listProgressIndicator").css('color', "#333333");
		                		var loadingImageString="<img alt='loading' src='themes/default/images/ajax-loader_small.gif'>";
		                        $("#listProgressIndicator").html('<spring:message code="delete.image.wait"/>'+loadingImageString);
		            			$.ajax({
		         				   type: "POST",
		         				   cache: false,
		         				   dataType: "html",
		         				   url: '<spring:url value="/services/org/upgrade/delete/"/>'+ encodeURIComponent(filename),
		         				   async: true,
		         				   beforeSend: function(){
		         				   },
		         				   success: function(data){
		         						var errorMsg = null;
		         						if(data != "S") {
		         							if((data == "F")) {
		         								errorMsg = '<spring:message code="error.backup.delete"/>';
		         							} 
		         							else if(data == "I") {
		         								errorMsg = '<spring:message code="error.backup.delete"/>';
		         							}
		         							$("#listProgressIndicator").css('color', "red");
		         							$("#listProgressIndicator").html(errorMsg);
		         							return false;
		         						}
		         						else {
		         							$("#" + id + "Trow").remove();
			         				    	$("#listProgressIndicator").css('color', "green");
			         					    $("#listProgressIndicator").html("File "+ filename +" was deleted successfully.");
		         						}
		         					},
		         				    error: function() {
		         				    	$("#listProgressIndicator").css('color', "red");
		         					    $("#listProgressIndicator").html('<spring:message code="error.connection.server"/>');
		         				    },
		         				    complete: function() {
		         				    	$("input.action").removeAttr('disabled');
		         				    }
		         			});	
			              }
			              else {
			            	  return false;
			              }
			        });
				}
				
				
				function upgrade(elementObj) {
					var id = $(elementObj).attr('id').split('T')[0];
					var filename = $("#" + id + "Tname").html();

					jConfirm(
							'This will change your existing EMS application. Are you sure you want to upgrade to '
									+ filename + "?",
							'<spring:message code="deletion.confirmation.title"/>',
							function(result) {
								if (result) {
									upgradePollServer = null;
									restartTomcat = "F";
									$.when(upgradesubmit(filename),
											pollUpgrade());
								}

							});
				}

				var upgradePollServer = null;
				var restartTomcat = "F";
				
				function upgradesubmit(filename) {
					$("#listProgressDiv").show();
					$("#listProgressIndicator").css('color', "#333333");
					var loadingImageString = "<img alt='loading' src='themes/default/images/ajax-loader_small.gif'>";
					$("#listProgressIndicator").html(
							'Please wait while ems is upgraded to ' + filename + ' ...' + loadingImageString);

							$.ajax({
								type : "POST",
								cache : false,
								dataType : "html",
								url : '<spring:url value="/services/org/upgrade/"/>'
										+ encodeURIComponent(filename),
								async : true,
								beforeSend : function() {

									$.blockUI({
										message : 'Check the progress here.',
										css : {
											border : 'none',
											padding : '15px',
											backgroundColor : '#000000',
											'-webkit-border-radius' : '10px',
											'-moz-border-radius' : '10px',
											opacity : .5,
											color : '#FFFFFF',
											top : '20px'
										}
									});

									$('.blockMsg')
											.html(
													'<div style="float: left;"><span style="font-weight: bold;">Progress</span></div>'
															+ '<div style="clear: both;">'
															+ '<ul>'
															+ '<li><span style="color: blue; font-weight: bold;" id="step1">Shutdown EMS application if it is up</span></li>'
															+ '<li><span style="color: red; font-weight: bold;" id="step2">Upgrade EMS</span></li>'
															+ '<li><span style="color: red; font-weight: bold;" id="step3">Start ems application</span></li>'
															+ '</ul>'
															+ '</div> <br />'
															+ '<div style="padding: 5px; max-height: 200px; overflow: auto; clear: both; text-align:left;" id="blockProgress">'
															+ '</div>'
															+ '<br/> <div id="closeprogress" style="font-weight: bold;"></div>');

								},
								success : function(data) {

									if ((data == "ALREADY_RUNNING")) {
										clearInterval(upgradePollServer);
										$.unblockUI();
										$("#listProgressIndicator").css(
												'color', "red");
										$("#listProgressIndicator")
												.html(
														'<spring:message code="error.process.already.running"/>');
									} else if (data == "FAILURE") {
										clearInterval(upgradePollServer);
										$.unblockUI();
										$("#listProgressIndicator").css(
												'color', "red");
										$("#listProgressIndicator")
												.html(
														'<spring:message code="error.upgrade.internal"/>');
									}
								},
								error : function() {
									if(restartTomcat != "T") {
										clearInterval(upgradePollServer);
										$.unblockUI();
										$("#listProgressIndicator").css('color',
												"red");
										$("#listProgressIndicator")
												.html(
														'<spring:message code="error.connection.server"/>');
									}
								},
								complete : function() {
								}
							});
				}

				function pollUpgrade() {
					
					$("<span id='showlogs'></span>").appendTo("#blockProgress");
					$("<br/>").appendTo("#blockProgress");
					var step_no = 1;
					var count = 1;
					var errorCount = 1;
					upgradePollServer = setInterval(
							function() {
										
										$.ajax({
											type : "POST",
											cache : false,
											dataType : "html",
											url : '<spring:url value="/services/org/upgrade/logs"/>',
											beforeSend : function() {
											},
											success : function(msg) {
												errorCount = 1;
												var parts = msg
														.split('EMS_UPGRADE_STARTED');
												$('#showlogs').html(parts[0]);
												if (parts[1] != null
														&& parts[1] != "") {
													if(restartTomcat == "F" && parts[1].search("TomcatRestart") != -1) {
														restartTomcat = "T";
													}
													if(restartTomcat == "T" && parts[1].search("TomcatRestartSuccess") != -1) {
														restartTomcat = "D";
													}
													while (parts[1]
															.search("step"
																	+ step_no) >= 0) {
														$('#step' + step_no)
																.css('color',
																		'green');
														step_no = step_no + 1;
														$('#step' + step_no)
																.css('color',
																		'blue');
													}
													if (parts[1]
															.search("ERROR:") >= 0) {
														var errorMsg = parts[1]
																.split('ERROR:');
														$('#showlogs')
																.html(
																		parts[0]
																				+ "<br /> ERROR: "
																				+ errorMsg[1]);
														$('#closeprogress')
																.html(
																		'<span>Click <a onclick="$.unblockUI();">here</a> to close this window.</span>');
														clearInterval(upgradePollServer);
														$("div").css('cursor',
																'default');
														$('#step' + step_no)
																.css('color',
																		'red');
														$(
																"#listProgressIndicator")
																.css('color',
																		"red");
														$(
																"#listProgressIndicator")
																.html(
																		'<spring:message code="error.upgrade.internal"/>');
													}
													if (step_no == 4) {
														if(parts[1].search("TomcatRestartFailed") != -1) {
															$('#step3').css('color',
																	'red');
														}
														$('#closeprogress')
																.html(
																		'<span>Click <a onclick="$.unblockUI();">here</a> to close this window.</span>');
														clearInterval(upgradePollServer);
														$("div").css('cursor',
																'default');
														$(
																"#listProgressIndicator")
																.css('color',
																		"green");
														$(
																"#listProgressIndicator")
																.html(
																		'<spring:message code="upgrade.successful"/>');
													}
												}
											},
						                    complete: function(transport){
						                    	if(restartTomcat == "T") {
						                    		count++;
						                    	}
						                    },
						                    error: function() {
						                    	errorCount++;
						                    }
										});
										
										if((restartTomcat == "T" && count > 120) || errorCount > 100) {
											$('#closeprogress')
											.html('<span>Click <a onclick="$.unblockUI();">here</a> to close this window.</span>');
											clearInterval(upgradePollServer);
											$("div").css('cursor', 'default');
											$("#listProgressIndicator").css('color',"red");
											$("#listProgressIndicator").html('<spring:message code="error.no.server.access"/>');
											$("<span> WARNING: " + '<spring:message code="error.no.server.access"/>' + "</span>").appendTo("#blockProgress");	
										}
							}, 5000);
				}
				

			</script>

		<div style="margin-top:20px;">
			<fieldset style="padding: 10px;">
				<legend style="font-weight: bold"><spring:message code="upgrade.list.legend"/></legend>
				<div>				
	 				<div id="listProgressDiv" style="padding-bottom: 5px; text-align: center;">
	 					<span style="font-weight: normal; font-size: 1em; " id="listProgressIndicator"></span>
	 				</div>
	 				<script type="text/javascript">
	 					$("#listProgressDiv").hide();
	 				</script>
	 				
					<div id="tableContainer">
						<table id="imagelisttable" class="tablesorter entable" width="100%">
						<thead>
							<tr>
								<th><spring:message code="upgrade.label.list.creation.time"/></th>
								<th><spring:message code="restore.label.list.file"/></th>
								<th><spring:message code="restore.label.list.file.size"/></th>
								<th><spring:message code="upgrade.label.list.version"/></th>
								<th>Action</th>
							</tr>
						</thead>
						<c:set var="count" value="${-1}" scope="request"/>
						<tbody>
							<c:forEach items="${fileList}" var="image">
								<c:set var="count" value="${count+1}" scope="request"/>
								<tr id=<c:out value="${count}"/>Trow >
									<td id=<c:out value="${count}"/>Tdate ><c:out value="${image.creationDate}"/></td>
									<td id=<c:out value="${count}"/>Tname ><c:out value="${image.upgradeFileName}"/></td>
									<td class="alignright" id=<c:out value="${count}"/>Tsize ><c:out value="${image.upgradeFileSize}"/></td>
									<td class="alignright" id=<c:out value="${count}"/>Tver ><c:out value="${image.version}"/></td>
									<td class="alignright">
										<input class="action" id=<c:out value="${count}"/>Trestore type="button" onclick="upgrade(this);" value="<spring:message code='action.upgrade'/>" />
										<input class="action" id=<c:out value="${count}"/>Tdelete  type="button" onclick="deleteUpgradeFile(this);" value="<spring:message code='action.delete'/>" />
									</td>
								</tr>
							</c:forEach>
						</tbody>
						</table>
					</div>
				</div>
			</fieldset>
			</div>
			<script type="text/javascript">
			
			$("#imagelisttable").tablesorter({
							sortList: [[0,1]],
							headers: { 0: {sorter:'text'}, 1: {sorter: 'text'}, 2: {sorter: 'numeric'}, 3: {sorter: 'numeric'}, 4: {sorter: false} }
				});
			</script>
		</c:if>
		</div>
	</div>
</div>