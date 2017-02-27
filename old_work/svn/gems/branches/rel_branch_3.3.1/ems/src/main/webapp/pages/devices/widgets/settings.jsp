<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security"
	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="ems" uri="/WEB-INF/tlds/ems.tld"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<spring:url value="/devices/widget/all/editAndApply.ems"
	var="applyBulkChangesURL" scope="request" />

<style>
input,textarea,select,button {
	/*	width: 150px;
	margin: 0;
	-webkit-box-sizing: border-box; /* For legacy WebKit based browsers */
	-moz-box-sizing: border-box; /* For all Gecko based browsers */
	box-sizing: border-box;
	*/
}
</style>
<script type="text/javascript">
		$('#facilityDlgTreeViewDiv').treenodeclick(function(){
			console.log("Type: " + treenodetype + ", id: " + treenodeid);
		});
		var isEdit=false;
		var oldMgrpName="";
		var newMgrpName;
		var switchModeType = 0;
		if ("${switch.modeType}" == 0) {
			switchModeType = 0;
		}else {
			switchModeType = 1;
		}
		
		function startBulkValidation() {
			newMgrpName = $("#bgmgname").val();
    		$.ajax({
    			type: "POST",
    			url: "${applyBulkChangesURL}?switchId="+${switchId}+"&isEdit="+isEdit+"&oldMgrpName="+oldMgrpName,
    			data: $("#editSwitchv1Form").serialize(),
    			datatype: "html",
    			beforeSend: function() {
    				$("#messageDiv").html("Processing, please wait...");
    				$("#messageDiv").css("color", "green");
    			},
    			success: function(msg) {
    				$("#messageDiv").html("Success");
    				$("#messageDiv").css("color", "green");
    				
    				oldMgrpName = newMgrpName;
    				
    				try {
						getFloorPlanObj("widget_floorplan").plotChartRefresh();
					} catch(e) {
					}
					isEdit=true;
    			},
    			error: function() {
    				$("#messageDiv").html("Failed");
    				$("#messageDiv").css("color", "red");
    			}
    		});	
    	}
		
		function validateInitialTime(evt) {
			var theEvent = evt || window.event;
			var key = theEvent.keyCode || theEvent.which;
			key = String.fromCharCode( key );
			var regex = /[0-9]|\./;
			if( !regex.test(key) ) {
				theEvent.returnValue = false;
				if ( evt.keyCode == 46 || evt.keyCode == 8 ) {
					// if key pressed is backspace or delete, then let it happen...
				}else{
					if(theEvent.preventDefault) theEvent.preventDefault();
				}
			}
		}
</script>
<form id="editSwitchv1Form" method="post" onsubmit="return false;">
	<table style="width: 100%;">
		<tr>
			<td colspan="2"><input type=button
				onclick="startBulkValidation();" value="Apply" /></td>
			<td colspan="2"><div id="messageDiv"
					style="font-weight: bold; padding: 0px 0px; display: inline;"></div></td>
		</tr>
		<tr>
			<td style="width: 25%;">
				<table>
					<tr>
						<td />
						<td><input type="hidden" name="bgsgid" id="bgsgid"
							value="${switch.id}" /></td>
					</tr>
					<tr>
						<td><label style="font-weight: bold">Switch name:</label></td>
						<td><input type="text" name="bgsgname" id="bgsgname"
							value="${switch.name}" /></td>
					</tr>
					<tr>
						<td><label style="font-weight: bold">Switch type:</label></td>
						<td><input type="radio" name="bgswitchtype"
							id="bgautoonautooff" value="0" checked /><span>Auto
								On/Auto Off</span></td>
					</tr>
					<tr>
						<td />
						<td><input type="radio" name="bgswitchtype"
							id="bgmanualonautooff" value="1" /><span>Manual On/Auto
								Off</span></td>
					</tr>
					<tr>
						<td><label style="font-weight: bold">Initial time:</label></td>
						<td><input type="text" name="bginitialSceneActiveTime"
							id="bginitialSceneActiveTime" maxlength="5"  onkeypress='validateInitialTime(event)'
							value="${switch.initialSceneActiveTime}" /></td>
					</tr>
				</table>
			</td>
			<td style="width: 25%;">
				<table>
					<tr>
						<td><label style="font-weight: bold">Create motion
								group:</label></td>
						<td><input type="hidden" name="bgmgroup" id="bgmgroup"
							value="0" /> <input type="checkbox" name="bgmgroup"
							id="bgmgroup" checked="checked" value="1" /></td>
					</tr>
					<tr>
						<td><label style="font-weight: bold">Motion Group
								name:</label></td>
						<td><input type="text" name="bgmgname" id="bgmgname"
							value="MG_${switch.name}" /></td>
					</tr>
				</table>
			</td>
			<td style="width: 25%;">
				<table>
					<tr>
						<td><label style="font-weight: bold">Apply Scene
								Template:</label></td>
						<td><input type="hidden" name="bgapplyscenetmpl"
							id="bgapplyscenetmpl" value="0" /><input type="checkbox"
							name="bgapplyscenetmpl" id="bgapplyscenetmpl" checked="checked"
							value="1" /></td>
					</tr>
					<tr>
						<td><label style="font-weight: bold">Apply Scene
								Template:</label></td>
						<td><select name="bgscenetemplate" id="bgscenetemplate">
								<c:forEach var="scenetmpl" items="${scenetemplate}">
									<option value="${scenetmpl.id}">${scenetmpl.name}</option>
								</c:forEach>
						</select></td>
					</tr>
				</table>
			</td>
			<td style="width: 25%;">
				<table>
					<tr>
						<td><label style="font-weight: bold">Apply profile:</label></td>
						<td><input type="hidden" name="bgapplyprofile"
							id="bgapplyprofile" value="0" /><input type="checkbox"
							name="bgapplyprofile" id="bgapplyprofile" checked="checked"
							value="1" /></td>
					</tr>
					<tr>
						<td><label style="font-weight: bold">Apply profile:</label></td>
						<td>
							<!-- 
						<select name="bgprofiles" id="bgprofiles">
								<c:forEach var="group" items="${groups}">
									<option value="${group.id}">${group.name}</option>
								</c:forEach>
						</select>
						--> <select name="bgprofiles" id="bgprofiles">
								<c:forEach items="${profileHierarchy.treeNodeList}"
									var="template">
									<optgroup label="${template.name}">
										<c:forEach items="${template.treeNodeList}" var="profile">
											<option value="${profile.nodeId}">${profile.name}</option>
										</c:forEach>
									</optgroup>
								</c:forEach>
						</select>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
</form>
