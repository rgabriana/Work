<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>

<spring:url value="/scripts/jquery/jquery.blockUI.2.39.js" var="jquery_blockUI"></spring:url>
<script type="text/javascript" src="${jquery_blockUI}"></script>


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
	});

	function upload() {
		
		var isValid = $('#upload').valid();
		
		if(isValid) {
	                
	        $('#uploadsubmit').attr('disabled', 'disabled');
			$("#uploadProgressDiv").show();
			var loadingImageString="<img alt='loading' src='themes/default/images/ajax-loader_small.gif'>";
	        $("#uploadProgressIndicator").html("<span>" + '<spring:message code="restore.upload.wait"/>' +loadingImageString + "</span>");
 	       	$("#upload").submit();
		}
	}
	
	
</script>
<spring:url value="uploadImageFile.emsmgmt" var="uploadFileURL" scope="request"/>
<div class="outerContainer">
	<div class="i1"></div>
	<div class="innerContainer" style="padding-bottom: 10px">
		<fieldset style="padding: 10px;">
			<legend style="font-weight: bold"><spring:message code="upgrade.upload.legend"/></legend>
			<div class="formContainer">
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
			</div>
		</fieldset>
		<%-- <c:if test="${!empty fileList}"> --%>
			
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
			        
			       	jConfirm('This will clean any data in your existing database. Are you sure you want to restore '+filename+"?",'<spring:message code="deletion.confirmation.title"/>',function(result){
			             if(result) {
			            	 $.when(upgradesubmit(filename),pollUpgrade());
			             }
		                		
			        });
				}
				
				var restorePollServer = null;
				function upgradesubmit(filename) {
					
				}
				
				function pollUpgrade() {
					
				}

			
			</script>
		
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
						<table class="entable" width="100%">
						<thead>
							<tr>
								<th><spring:message code="upgrade.label.list.creation.time"/></th>
								<th><spring:message code="restore.label.list.file"/></th>
								<th><spring:message code="restore.label.list.file.size"/></th>
								<th><spring:message code="upgrade.label.list.version"/></th>
								<th></th>
							</tr>
						</thead>
						<c:set var="count" value="${-1}" scope="request"/>
						<tbody>
							<c:forEach items="${fileList}" var="image">
								<c:set var="count" value="${count+1}" scope="request"/>
								<tr id=<c:out value="${count}"/>Trow>
									<td id=<c:out value="${count}"/>Tdate ><c:out value="${image.creationDate}"/></td>
									<td id=<c:out value="${count}"/>Tname ><c:out value="${image.upgradeFileName}"/></td>
									<td id=<c:out value="${count}"/>Tsize ><c:out value="${image.upgradeFileSize}"/></td>
									<td id=<c:out value="${count}"/>Tver ><c:out value="${image.version}"/></td>
									<td>
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
		<%-- </c:if> --%>
	</div>
</div>