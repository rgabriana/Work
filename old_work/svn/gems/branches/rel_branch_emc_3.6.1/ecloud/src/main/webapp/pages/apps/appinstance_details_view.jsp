<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<c:set var="appInstanceId" value="${appInstanceDetailsView.id}" />

<script>
</script>

<style>
html,body {
    background-color: #FFFFFF !important;
    
}
</style>

		<div style="width: 100%; height: 100%; background: #fff; padding: 10px 10px 20px 10px" id="detailsDiv">
			<div class="field">
				<div class="formPrompt"><span>Name:</span></div>
				<div class="formValue">${appInstanceDetailsView.name}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Version:</span></div>
				<div class="formValue">${appInstanceDetailsView.version}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Mac Id:</span></div>
				<div class="formValue">${appInstanceDetailsView.macId}</div>
			</div>			
			<div class="field">
				<div class="formPrompt"><span>EM Address:</span></div>
				<div class="formValue">${appInstanceDetailsView.ipAddress}</div>
			</div>			
			<div class="field">
				<div class="formPrompt"><span>Last Connectivity (UTC):</span></div>
				<div class="formValue">${appInstanceDetailsView.utcLastConnectivityAt}</div>
			</div>			
			<div class="field">
				<div class="formPrompt"><span>SSH Port:</span></div>
				<div class="formValue">${appInstanceDetailsView.sshTunnelPort}</div>
			</div>										
	    </div>