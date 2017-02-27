<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/scripts/jquery/jquery.blockUI.2.39.js"
	var="jquery_blockUI"></spring:url>
<script type="text/javascript" src="${jquery_blockUI}"></script>

<spring:url value="/scripts/jquery/jquery.tablesorter.min.js"
	var="jquery_tablesorter"></spring:url>
<script type="text/javascript" src="${jquery_tablesorter}"></script>


<script type="text/javascript">
	var usbIcon = "<img alt='From USB Stick' src='themes/default/images/usb_file.png'>";
	var usbPattern = /\/media\//;

	$()
			.ready(
					function() {

						jQuery.validator
								.addMethod("invalidChar", function(value,
										element) {
									var pattern = /[^0-9a-zA-Z_]/;
									if (pattern.test(value)) {
										return false;
									}
									return true;
								},
										"<spring:message code='error.file.restricted.characters'/>");

						$("#backup")
								.validate(
										{
											rules : {
												backupfilename : {
													required : true,
													invalidChar : ""
												}
											},
											messages : {
												backupfilename : {
													required : '<spring:message code="error.above.field.required"/>'
												}
											}

										});
						$(".outermostdiv").css("minHeight", $(window).height() - 85);
					});

	function startBackUp() {

		var isValid = $('#backup').valid();

		if (isValid) {
			$.when(backupFormsubmit(), pollBackup());
		}
	}

	var backupPollServer = null;
	function backupFormsubmit() {
		$('#backupsubmit').attr('disabled', 'disabled');
		$('#backupfilename').attr('disabled', 'disabled');
		$("#backupProgressDiv").show();
		var loadingImageString = "<img alt='loading' src='themes/default/images/ajax-loader_small.gif'>";
		$("#backupProgressIndicator").html(
				"<span>" + '<spring:message code="backup.wait"/>'
						+ loadingImageString + "</span>");

		$
				.ajax({
					type : "POST",
					cache : false,
					dataType : "html",
					url : '<spring:url value="/services/org/backup/create/"/>'
							+ encodeURIComponent($("#backupfilename").val()),
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
												+ '<li><span style="color: blue; font-weight: bold;" id="step1">Creating database dump.</span></li>'
												+ '<li><span style="color: red; font-weight: bold;" id="step2">Getting database version.</span></li>'
												+ '<li><span style="color: red; font-weight: bold;" id="step3">Compress the data.</span></li>'
												+ '</ul>'
												+ '</div> <br />'
												+ '<div style="padding: 5px; max-height: 200px; overflow: auto; clear: both;text-align:left;" id="blockProgress">'
												+ '</div>'
												+ '<br/> <div id="closeprogress" style="font-weight: bold;"></div>');

					},
					success : function(data) {

						if ((data == "ALREADY_RUNNING")) {
							clearInterval(backupPollServer);
							$.unblockUI();
							$("#backupProgressIndicator")
									.html(
											"<span style='color:red'>"
													+ '<spring:message code="error.process.already.running"/>'
													+ "</span>");
						} else if (data == "FAILURE") {
							clearInterval(backupPollServer);
							$.unblockUI();
							$("#backupProgressIndicator")
									.html(
											"<span style='color:red'>"
													+ '<spring:message code="error.backup.internal"/>'
													+ "</span>");
						}
					},
					error : function() {
						clearInterval(backupPollServer);
						$.unblockUI();
						$("#backupProgressIndicator")
								.html(
										"<span style='color:red'>"
												+ '<spring:message code="error.connection.server"/>'
												+ "</span>");
					},
					complete : function() {
						$('#backupsubmit').removeAttr('disabled');
						$('#backupfilename').removeAttr('disabled');
					}
				});
	}

	function pollBackup() {

		$("<span id='showlogs'></span>").appendTo("#blockProgress");
		$("<br/>").appendTo("#blockProgress");
		var step_no = 1;
		backupPollServer = setInterval(
				function() {
					$
							.ajax({
								type : "POST",
								cache : false,
								dataType : "html",
								url : '<spring:url value="/services/org/backup/logs"/>',
								beforeSend : function() {
								},
								success : function(msg) {
									var parts = msg.split('EMS_BACKUP_STARTED');
									$('#showlogs').html(parts[0]);
									if (parts[1] != null && parts[1] != "") {
										while (parts[1].indexOf("step"
												+ step_no) >= 0) {
											$('#step' + step_no).css('color',
													'green');
											step_no = step_no + 1;
											$('#step' + step_no).css('color',
													'blue');
										}
										if (parts[1].indexOf("ERROR:") >= 0) {
											var errorMsg = parts[1]
													.split('ERROR:');
											$('#showlogs').html(
													parts[0] + "<br /> ERROR: "
															+ errorMsg[1]);
											$('#closeprogress')
													.html(
															'<span>Click <a onclick="$.unblockUI();">here</a> to close this window.</span>');
											clearInterval(restorePollServer);
											$("div").css('cursor', 'default');
											$('#step' + step_no).css('color',
													'red');
											$("#backupProgressIndicator")
													.html(
															"<span style='color:red'>"
																	+ '<spring:message code="error.backup.internal"/>'
																	+ "</span>");
										}
										if (step_no == 4) {
											$('#closeprogress')
													.html(
															'<span>Click <a onclick="location.reload();">here</a> to refresh the page.</span>');
											clearInterval(backupPollServer);
											$("div").css('cursor', 'default');
											$("#backupProgressIndicator")
													.html(
															"<span style='color:green'>"
																	+ '<spring:message code="backup.successful"/>'
																	+ "</span>");
										}
									}
								}
							});

				}, 5000);
	}
</script>
<div class="pgset">
	<div class="outermostdiv">
		<div class="outerContainer">
			<span><spring:message code="backup.header" /></span>
			<div class="i1"></div>
			<div style="width: 100%;">
				<fieldset style="padding: 10px;">
					<legend style="font-weight: bold">
						<spring:message code="backup.legend.title" />
					</legend>
					<span style="font-weight: normal; font-style: italic;">
					<spring:message	code="backup.note"/> </span>

					<form id="backup" method="post"
						onsubmit="startBackUp(); return false;">
						<div class="field">
							<div class="formPrompt">
								<span><spring:message code="backup.label.file.name" /></span>
							</div>
							<div class="formValue">
								<input id="backupfilename" type="text" name="backupfilename"
									size="24" />
							</div>
						</div>

						<div id="backupProgressDiv" class="field">
							<div class="formPrompt">
								<span></span>
							</div>
							<div class="formValue" id="backupProgressIndicator"></div>
						</div>
						<script type="text/javascript">
							$("#backupProgressDiv").hide();
						</script>
						<div class="field">
							<div class="formPrompt">
								<span></span>
							</div>
							<div class="formValue">
								<input class="navigation" id="backupsubmit" type="submit"
									value=<spring:message code='action.start'/>></input>
							</div>
						</div>
					</form>

				</fieldset>
			</div>
		</div>
		<div style="padding: 3px;"></div>

		<script type="text/javascript">
			$()
					.ready(
							function() {

								$("#upload")
										.validate(
												{
													rules : {
														fileData : {
															required : true,
															accept : "tar.gz"
														}
													},
													messages : {
														fileData : {
															required : '<spring:message code="error.above.field.required"/>',
															accept : "<spring:message code='error.tar.gz.extension.required'/>"
														}
													}

												});
							});

			function upload() {

				var isValid = $('#upload').valid();

				if (isValid) {

					$('#uploadsubmit').attr('disabled', 'disabled');
					$("#uploadProgressDiv").show();
					var loadingImageString = "<img alt='loading' src='themes/default/images/ajax-loader_small.gif'>";
					$("#uploadProgressIndicator")
							.html(
									"<span>"
											+ '<spring:message code="restore.upload.wait"/>'
											+ loadingImageString + "</span><span id='uploadSizeIndicator'></span>");

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
		<spring:url value="uploadBackupFile.emsmgmt" var="uploadFileURL"
			scope="request" />

		<div class="outerContainer">
			<span><spring:message code="restore.header" /></span>
			<div class="i1"></div>
			<fieldset style="padding: 10px;">
				<legend style="font-weight: bold">
					<spring:message code="restore.upload.legend" />
				</legend>

				<form:form id="upload" modelAttribute="backUpFile"
					action="${uploadFileURL}" method="post"
					enctype="multipart/form-data">
					<div class="field">
						<div class="formPrompt">
							<span><spring:message code="restore.label.backup.upload" /></span>
						</div>
						<div class="formValue">
							<form:input path="fileData" id="uploadFilePath" type="file" />
						</div>
					</div>
					<div id="uploadProgressDiv" class="field">
						<div class="formPrompt">
							<span></span>
						</div>
						<div class="formValue" id="uploadProgressIndicator"></div>
					</div>
					<script type="text/javascript">
						var status =
					<%=request.getParameter("uploadStatus")%>
						;
						var filename =
					<%=request.getParameter("filename")%>
						;
						if (status == null) {
							$("#uploadProgressDiv").hide();
						} else {
							if (status == "F") {
								$("#uploadProgressIndicator")
										.html(
												"<span style='color:red'>"
														+ '<spring:message code="error.restore.upload.internal"/>'
														+ "</span>");
							} else if (status == "S" && filename != null) {
								$("#uploadProgressIndicator")
										.html(
												"<span style='color:green'>File "
														+ filename
														+ " was uploaded successfully.</span>");
							}
						}
					</script>
					<div class="field">
						<div class="formPrompt">
							<span></span>
						</div>
						<div class="formValue">
							<input class="navigation" id="uploadsubmit" type="button"
								onclick="upload();" value=<spring:message code='action.upload'/>></input>
						</div>
					</div>
				</form:form>

			</fieldset>
			<c:if test="${!empty backups}">

			<script type="text/javascript">
				function deleteBackupFile(elementObj) {
					var id = $(elementObj).attr('id').split('T')[0];
					var filename = $("#" + id + "Tname").html();
					var filepath = $("#" + id + "Tpath").val().replace(/\//g,
							"=");

					jConfirm(
							'Are you sure you want to delete ' + filename + "?",
							'<spring:message code="deletion.confirmation.title"/>',
							function(result) {
								if (result) {
									$("input.action").attr('disabled',
											"disabled");
									$("#listProgressDiv").show();
									$("#listProgressIndicator").css('color',
											"#333333");
									var loadingImageString = "<img alt='loading' src='themes/default/images/ajax-loader_small.gif'>";
									$("#listProgressIndicator").html(
											'<spring:message code="delete.wait"/>'
													+ loadingImageString);
									$
											.ajax({
												type : "POST",
												cache : false,
												dataType : "html",
												url : '<spring:url value="/services/org/backup/delete/"/>'
														+ encodeURIComponent(filename)
														+ "/" + filepath,
												async : true,
												beforeSend : function() {
												},
												success : function(data) {
													var errorMsg = null;
													if (data != "S") {
														if ((data == "F")) {
															errorMsg = '<spring:message code="error.backup.delete"/>';
														} else if (data == "I") {
															errorMsg = '<spring:message code="error.backup.delete"/>';
														}
														$(
																"#listProgressIndicator")
																.css('color',
																		"red");
														$(
																"#listProgressIndicator")
																.html(errorMsg);
														return false;
													} else {
														$("#" + id + "Trow")
																.remove();
														$(
																"#listProgressIndicator")
																.css('color',
																		"green");
														$(
																"#listProgressIndicator")
																.html(
																		"File "
																				+ filename
																				+ " was deleted successfully.");
													}
												},
												error : function() {
													$("#listProgressIndicator")
															.css('color', "red");
													$("#listProgressIndicator")
															.html(
																	'<spring:message code="error.connection.server"/>');
												},
												complete : function() {
													$("input.action")
															.removeAttr(
																	'disabled');
												}
											});
								} else {
									return false;
								}
							});
				}

				function restoreBackupFile(elementObj) {
					var id = $(elementObj).attr('id').split('T')[0];
					var filename = $("#" + id + "Tname").html();
					var filepath = $("#" + id + "Tpath").val().replace(/\//g,
							"=");

					jConfirm(
							'This will clean any data in your existing database. Are you sure you want to restore '
									+ filename + "?",
							'<spring:message code="deletion.confirmation.title"/>',
							function(result) {
								if (result) {
									restorePollServer = null;
									restartTomcat = "F";
									$.when(restoresubmit(filename, filepath),
											pollRestore());
								}

							});
				}

				var restorePollServer = null;
				var restartTomcat = "F";
				function restoresubmit(filename, filepath) {
					$("#listProgressDiv").show();
					$("#listProgressIndicator").css('color', "#333333");
					var loadingImageString = "<img alt='loading' src='themes/default/images/ajax-loader_small.gif'>";
					$("#listProgressIndicator").html(
							'Please wait while backup file ' + filename
									+ ' is restored...' + loadingImageString);

					$
							.ajax({
								type : "POST",
								cache : false,
								dataType : "html",
								url : '<spring:url value="/services/org/backup/restore/"/>'
										+ encodeURIComponent(filename)
										+ '/'
										+ filepath,
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
															+ '<li><span style="color: blue; font-weight: bold;" id="step1">Pre-restore sanity.</span></li>'
															+ '<li><span style="color: red; font-weight: bold;" id="step2">Shutdown ems application if it is up.</span></li>'
															+ '<li><span style="color: red; font-weight: bold;" id="step3">Re-create ems database.</span></li>'
															+ '<li><span style="color: red; font-weight: bold;" id="step4">Restore database.</span></li>'
															+ '<li><span style="color: red; font-weight: bold;" id="step5">Start ems application.</span></li>'
															+ '</ul>'
															+ '</div> <br />'
															+ '<div style="padding: 5px; max-height: 200px; overflow: auto; clear: both; text-align:left;" id="blockProgress">'
															+ '</div>'
															+ '<br/> <div id="closeprogress" style="font-weight: bold;"></div>');

								},
								success : function(data) {

									if ((data == "ALREADY_RUNNING")) {
										clearInterval(restorePollServer);
										$.unblockUI();
										$("#listProgressIndicator").css(
												'color', "red");
										$("#listProgressIndicator")
												.html(
														'<spring:message code="error.process.already.running"/>');
									} else if (data == "FAILURE") {
										clearInterval(restorePollServer);
										$.unblockUI();
										$("#listProgressIndicator").css(
												'color', "red");
										$("#listProgressIndicator")
												.html(
														'<spring:message code="error.restore.internal"/>');
									}
								},
								error : function() {
									if(restartTomcat != "T" && restartTomcat != "D") {
										clearInterval(restorePollServer);
										$.unblockUI();
										$("#listProgressIndicator").css('color',
												"red");
										$("#listProgressIndicator")
												.html(
														'<spring:message code="error.restore.connection.server"/>');
									}
								},
								complete : function() {
								}
							});
				}

				function pollRestore() {
					
					$("<span id='showlogs'></span>").appendTo("#blockProgress");
					$("<br/>").appendTo("#blockProgress");
					var step_no = 1;
					var count = 1;
					var errorCount = 1;
					restorePollServer = setInterval(
							function() {
										
										$.ajax({
											type : "POST",
											cache : false,
											dataType : "html",
											url : '<spring:url value="/services/org/backup/logs"/>',
											beforeSend : function() {
											},
											success : function(msg) {
												errorCount = 1;
												var parts = msg
														.split('EMS_RESTORE_STARTED');
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
														clearInterval(restorePollServer);
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
																		'<spring:message code="error.restore.internal"/>');
													}
													if (step_no == 6) {
														if(parts[1].search("TomcatRestartFailed") != -1) {
															$('#step5').css('color',
																	'red');
														}
														$('#closeprogress')
																.html(
																		'<span>Click <a onclick="$.unblockUI();">here</a> to close this window.</span>');
														clearInterval(restorePollServer);
														$("div").css('cursor',
																'default');
														$(
																"#listProgressIndicator")
																.css('color',
																		"green");
														$(
																"#listProgressIndicator")
																.html(
																		'<spring:message code="restore.successful"/>');
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
											clearInterval(restorePollServer);
											$("div").css('cursor', 'default');
											$("#listProgressIndicator").css('color',"red");
											$("#listProgressIndicator").html('<spring:message code="error.no.server.access"/>');
											$("<span> WARNING: " + '<spring:message code="error.no.server.access"/>' + "</span>").appendTo("#blockProgress");	
										}
							}, 5000);
				}
			</script>

			<fieldset style="padding: 10px; margin-top: 20px;">
				<legend style="font-weight: bold">
					<spring:message code="restore.list.legend" />
				</legend>

				<div id="listProgressDiv"
					style="padding-bottom: 5px; text-align: center;">
					<span style="font-weight: normal; font-size: 1em;"
						id="listProgressIndicator"></span>
				</div>
				<script type="text/javascript">
						$("#listProgressDiv").hide();
					</script>

				<div id="tableContainer" class="tableContainer">
					<table id="backuplisttable" class="tablesorter entable" width="100%">
						<thead>
							<tr>
								<th><spring:message
										code="restore.label.list.last.modified.time" /></th>
								<th><spring:message code="restore.label.list.file" /></th>
								<th><spring:message code="restore.label.list.file.size" /></th>
								<th>Action</th>
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
									<td class="alignright"> 
										<span id=<c:out value="${count}"/>Tusb > </span>
										<input class="action" id=<c:out value="${count}"/>Trestore type="button"
										onclick="restoreBackupFile(this);" value="<spring:message code='action.restore'/>" />
										<input	class="action" id=<c:out value="${count}"/>Tdelete type="button" onclick="deleteBackupFile(this);"
										value="<spring:message code='action.delete'/>" /> 
										<input	id=<c:out value="${count}"/>Tpath type="hidden"
										value="${backup.filepath}" /></td>
								</tr>
								<script type="text/javascript">
									if(usbPattern.test('<c:out value="${backup.filepath}" />')) {
										$('#'+ '<c:out value="${count}"/>' + "Tusb").html(usbIcon);
									}
								</script>
							</c:forEach>
						</tbody>
					</table>
				</div>
			</fieldset>
			<script type="text/javascript">
			
			$("#backuplisttable").tablesorter({
							sortList: [[0,1]],
							headers: { 0: {sorter:'text'}, 1: {sorter: 'text'}, 2: {sorter: 'numeric'}, 3: {sorter: false} }
				});
			</script>
			</c:if>
		</div>
	</div>
</div>
