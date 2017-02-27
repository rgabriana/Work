<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>

<c:set var="siteId" value="${siteDetailsView.id}" />

<script>
</script>
<style>
html,body {
    background-color: #FFFFFF !important;
}
</style>
		<div style="width: 100%; height: 100%; background: #fff; padding: 10px 10px 20px 10px" id="detailsDiv">
			<div class="field">
				<div class="formPrompt"><span>Site Id:</span></div>
				<div class="formValue">${siteDetailsView.id}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Geo Location:</span></div>
				<div class="formValue">${siteDetailsView.geoLocation}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Name:</span></div>
				<div class="formValue">${siteDetailsView.name}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>SPPA Price:</span></div>
				<div class="formValue">${siteDetailsView.sppaPrice}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Block Purchase Energy:</span></div>
				<div class="formValue">${siteDetailsView.blockPurchaseEnergy}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Block Energy Consumed:</span></div>
				<div class="formValue">${siteDetailsView.blockEnergyConsumed}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Total Billed no. of Days:</span></div>
				<div class="formValue">${siteDetailsView.totalBilledNoOfDays}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>PO Number:</span></div>
				<div class="formValue">${siteDetailsView.poNumber}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Square Foot:</span></div>
				<div class="formValue">${siteDetailsView.squareFoot}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Region:</span></div>
				<div class="formValue">${siteDetailsView.region}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Release Date:</span></div>
				<div class="formValue">${siteDetailsView.billStartDate}</div>
			</div>
			<div class="field">
				<div class="formPrompt"><span>Estimated Burn Hours:</span></div>
				<div class="formValue">${siteDetailsView.estimatedBurnHours}</div>
			</div>
	    </div>