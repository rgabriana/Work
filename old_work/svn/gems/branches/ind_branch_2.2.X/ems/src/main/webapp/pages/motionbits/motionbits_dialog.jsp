<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<spring:url value="/devices/widget/create/motionbitsgroup.ems" var="motionBitCreateUrl" scope="request" />

<style>
	#create_motionbits{padding:10px 15px;}
	#create_motionbits table{width:100%;}
	#create_motionbits td{ padding-bottom:3px;}
	#create_motionbits td.fieldLabel{width:35%; font-weight:bold;}
	#create_motionbits td.fieldValue{width:65%;}
	#create_motionbits .inputField{width:100%; height:20px;}
	#create_motionbits #saveTemplateBtn{padding: 0 10px;}
	#create_motionbits #closeTemplateBtn{padding: 0 10px;}
	#create_motionbits .invalidField{border: 1px solid red; border-radius: 3px 3px 3px 3px; padding: 1.5px;}
</style>

<script type="text/javascript">
var requirederr = '<spring:message code="error.above.field.required"/>';



var createUserValidatorObj = {
		rules: {
			name: { required: true},			
		},
		messages: {
			name: { required: requirederr }			
		}		
	};
$("#create_template").validate(createUserValidatorObj);

function checkEnter(e){
	 e = e || event;
	 return (e.keyCode || event.which || event.charCode || 0) !== 13;
	}


function validateTemplate()
{
	var chktemplatename = $("#name").val();
	var returnresult = false;
	
		
		
	var iChars = "!@#$%^&*()+=-[]\\\';,./{}|\":<>?";
		for ( var i = 0; i < chktemplatename.length; i++) {
			if (iChars.indexOf(chktemplatename.charAt(i)) != -1) {
				clearMessageTemplate();
				$("#errorMsg").text("Above field is invalid.");
				$("#name").addClass("invalidField");
				return false;
			}
		}

		/* if((chktemplatename).indexOf("#")>0 || (chktemplatename).indexOf("%")>0 || (chktemplatename).indexOf("/")>0 || (chktemplatename).indexOf("\\")>0 || (chktemplatename).indexOf("?")>0 )
		{
		clearMessageTemplate();
		 $("#errorMsg").text("Above field is invalid.");
		 $("#name").addClass("invalidField");
		 alert("Doing well");
		return false;
		}  */

		if (chktemplatename == "" || chktemplatename == " ") {
			clearMessageTemplate();
			$("#errorMsg").text("Above field is required.");
			$("#name").addClass("invalidField");
			return false;
		}
		// var count = (msg).indexOf(chktemplatename);

		$
				.ajax({
					type : "GET",
					cache : false,
					url : '<spring:url value="/services/org/motionbits/duplicatecheck/"/>'
							+ chktemplatename,
					dataType : "text",
					async : false,
					success : function(msg) {
						var count = (msg).indexOf(chktemplatename);
						if (count > 0) {
							returnresult = false;
						} else {
							returnresult = true;
						}
					},
					error : function(jqXHR, textStatus, errorThrown) {
						returnresult = false;
					}
				});
		if (!returnresult) {
			clearMessageTemplate();
			$("#errorMsg").text(
					'<spring:message code="motionbits.duplicate"/>');
			$("#name").addClass("invalidField");
			return false;
		} else {
			clearMessageTemplate();
			saveTemplate(chktemplatename);
		}
	}

	function saveTemplate(groupName) {
		createMotionWidgetDialog(groupName);
		exitWindow();
	};

	function exitWindow() {
		$('#motionMgmtDialog').dialog('close');
	}

	function createMotionWidgetDialog(grpName) {
		groupName = encodeURIComponent(grpName);

		dialogLayout_settings = {
			zIndex : 0 // HANDLE BUG IN CHROME - required if using 'modal' (background mask)
			,
			resizeWithWindow : false // resizes with the dialog, not the window
			,
			spacing_open : 6,
			spacing_closed : 6,
			north__size : '0%',
			north__minSize : 0,
			west__size : '30%',
			west__minSize : 100,
			west__maxSize : 300,
			south__size : 'auto',
			south__closable : false,
			south__resizable : false,
			south__slidable : false
		};

		/* var groupName = prompt("Enter Motion Group name:", ""); */

		if (groupName != null && groupName != "") {

			// First call a service to create a virtual switch; get the id back and then pass it on to the widget dialog
			$("#mbScheduleDetailsDialog")
					.load(
							"${motionBitCreateUrl}?groupName="
									+ encodeURIComponent(groupName),
							function() {
								$("#mbScheduleDetailsDialog")
										.dialog(
												{
													modal : true,
													title : 'Motion Group',
													width : Math
															.floor($('body')
																	.width() * .98),
													height : Math
															.floor($('body')
																	.height() * .98),
													open : function() {

														$(
																"#mbScheduleDetailsDialog")
																.layout(
																		dialogLayout_settings);

													},
													resize : function() {
														/* if (dialogLayout) */dialogLayout
																.resizeAll();
													},
													close : function(event, ui) {
														location.reload();
													}
												});
							});
			return false;
		}

	}

	function closeTemplate() {
		$("#motionMgmtDialog").dialog('close');
	}

	function clearMessageTemplate() {
		$("#errorMsg").text("");
		$("#name").removeClass("invalidField");
	}
</script>

<div>
	<spring:url value="/devices/widget/create/motionbitsgroup.ems" var="actionURL" scope="request" />			
	<form:form id="create_motionbits" commandName="mScheduler" method="post" action="${actionURL}" onkeypress="return checkEnter(event);">
        <form:hidden path="id"/>
		<table>
			<tr>
				<td class="fieldLabel"><spring:message code="motionbits.dialog" />*</td>
				<td class="fieldValue"><form:input class="inputField" id="name" name="name" path="name" onkeypress="clearMessageTemplate();" onmousedown="clearMessageTemplate();"/><span id="errorMsg" class="error"></span></td>
			</tr>
			<tr>
			<td style="height: 5px;"> </td>
			</tr>
			<tr>
				<!-- <td /> -->
				<td colspan="2" align="center">
					<input type="button" id="saveTemplateBtn"
					value="<spring:message code="action.save" />" onclick="validateTemplate()">
					
					<input type="button" id="closeTemplateBtn"
					value="<spring:message code="action.cancel" />" onclick="closeTemplate()">
				</td>
			</tr>
		</table>
	</form:form>
</div>


