<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<c:set var="emInstanceId" value="${emInstanceDetailsView.id}" />
<c:set var="pauseSyncStatus" value="${emInstanceDetailsView.pauseSyncStatus}" />

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
				<div class="formValue">${emInstanceDetailsView.name}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Version:</span></div>
				<div class="formValue">${emInstanceDetailsView.version}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Mac Id:</span></div>
				<div class="formValue">${emInstanceDetailsView.macId}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Contact Name:</span></div>
				<div class="formValue">${emInstanceDetailsView.contactName}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Contact Email:</span></div>
				<div class="formValue">${emInstanceDetailsView.contactEmail}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>EM Address:</span></div>
				<div class="formValue">${emInstanceDetailsView.address}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Contact PhoneNo:</span></div>
				<div class="formValue">${emInstanceDetailsView.contactPhone}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Last Connectivity (UTC):</span></div>
				<div class="formValue">${emInstanceDetailsView.utcLastConnectivityAt}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Health of Em Instance:</span></div>
				<div class="formValue">${emInstanceDetailsView.healthOfEmInstance}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Replica Server:</span></div>
				<div class="formValue">${emInstanceDetailsView.replicaServer.name}</div>
			</div>			
			<div class="field">
				<div class="formPrompt"><span>Database Name:</span></div>
				<div class="formValue">${emInstanceDetailsView.databaseName}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>SSH Port:</span></div>
				<div class="formValue">${emInstanceDetailsView.sshTunnelPort}</div>
			</div>	
			<div class="field">
				<div class="formPrompt"><span>Certificate Start Date:</span></div>
				<div class="formValue">${emInstanceDetailsView.certStartDate}</div>
			</div>	
			<div class="field">
				<div class="formPrompt"><span>Certificate End Date:</span></div>
				<div class="formValue">${emInstanceDetailsView.certEndDate}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Sync Paused:</span></div>
				<div class="formValue"><c:out value="${pauseSyncStatus ?  'Yes': 'No'}" /></div>
			</div>
										
	    </div>