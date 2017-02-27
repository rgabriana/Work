<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<style type="text/css">
	html, body{margin:5px 0px 0px 0px !important; background: #fff !important; overflow: hidden !important;}

	fieldset {
	    border:1px solid #999;
	    border-radius:8px;
		padding: 5px 5px 5px 5px;
	}

	.newsticker{
		white-space: nowrap;
		padding: 0;
		color: #ff0000;
		font: bold 11px Verdana;
	}

</style>

<script type="text/javascript">
		$(document).ready(function() {
			<c:if test="${isDRAssignedAndActiveOnFacility == 'true'}">
			var starttimeinmillisec = parseInt("${starttime}");
        	var durationinsec = parseInt("${duration}");
        	var durationinmillisec = durationinsec * 1000 ;
        	var durationinmin = durationinsec/60 ;
        	var currenttimeinmillisec = parseInt("${currenttime}");
        	var endtimeinmillisec =  starttimeinmillisec + durationinmillisec ;
        	var timeremaininginmillisec = endtimeinmillisec - currenttimeinmillisec;
        	var timeremaininginsec = timeremaininginmillisec/1000;
        	var timeremaininginmin = timeremaininginsec/60;

        	var level = "${level}";
        	var type ="${drtype}";

        	var drProgressStr = "";

        	if( durationinsec != "-1" && (durationinsec == 0 || timeremaininginsec > 0))
			{
	        	var stickerMainDiv = document.getElementById('notificationMessage');

				drProgressStr = "Override in progress.	Initiate type: " + type + ", Level: "+level;

				if(durationinsec != 0)
				{
						drProgressStr = drProgressStr + ", Time Remaining: ";
						if(timeremaininginmin >= 1)
						{
							drProgressStr = drProgressStr + Math.round(timeremaininginmin) +" min";
						}
						else
						{
							drProgressStr = drProgressStr + Math.round(timeremaininginsec) +" sec";
						}
				}
			}

        	$("#drProgressId").html(drProgressStr);

        	$("#drEventType").html("Event Type : "+drEventTypeString("${drTarget.drType}"));

        	$("#drStartTime").html("Start Time : "+drDatetimeString("${drTarget.startTime}"));

        	$("#drDuration").html("Duration(min) : "+drDurationString("${drTarget.duration}"));

        	$("#drPricing").html("Pricing(per kWh) : "+drPricingString("${drTarget.pricing}"));

        	$("#drOverrideLevel").html("Override Level : " + "${drTarget.priceLevel}");

        	$("#drStatus").html("Status : " + "${drTarget.drStatus}");

        	$("#drDescription").html("Description : " + "${drTarget.description}");

        	function drEventTypeString(drtype){
        		var eventType = "";
        		eventType = (drtype == 'MANUAL' ? 'DR' : 'Holiday');
        		return eventType;
        	}

        	function drDatetimeString(startTime){
        		var dateString = "";
        		var ds1 = startTime;
        		var ds2 = ds1.substring(0,19);
        		dateString = ds2.replace('T',' ');
        		return dateString;
        	}

        	function drDurationString(drDuration){
        		var duration =  "";
        		var drDurationSec = parseInt(drDuration);
        		duration = Math.round(drDurationSec/60);
        		return duration.toString();
        	}

        	function drPricingString(drPricing){
        		var pricing = "";
        		pricing = ((drPricing == "" || drPricing == null) ? 'NA' : drPricing);
        		return pricing;
        	}

        	</c:if>

        	<c:if test="${isDRAssignedAndActiveOnFacility == 'false'}">

    		$("#drProgressId").html("There is no active DR in progress for this facility");

        	</c:if>

		});

</script>

<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >

<fieldset>
<legend>Active DR Progress</legend>
<div style="padding:5px;"/>
<div id="drProgressId" align="left" style="padding: 5px 5px 5px 10px;font-weight:bold"></div>
</fieldset>

<c:if test="${isDRAssignedAndActiveOnFacility == 'true'}">
<div style="padding:5px;"/>
<fieldset>
<legend>Active DR Details</legend>
<div style="padding:5px;"/>
	<table >
		<tr><td id="drEventType" align="left" style="padding: 5px 5px 5px 5px;font-weight:bold"></td></tr>
		<tr><td id="drStartTime" align="left" style="padding: 5px 5px 5px 5px;font-weight:bold"></td></tr>
		<tr><td id="drDuration" align="left" style="padding: 5px 5px 5px 5px;font-weight:bold"></td></tr>
		<tr><td id="drPricing" align="left" style="padding: 5px 5px 5px 5px;font-weight:bold"></td></tr>
		<tr><td id="drOverrideLevel" align="left" style="padding: 5px 5px 5px 5px;font-weight:bold"></td></tr>
		<tr><td id="drStatus" align="left" style="padding: 5px 5px 5px 5px;font-weight:bold"></td></tr>
		<tr><td id="drDescription" align="left" style="padding: 5px 5px 5px 5px;font-weight:bold"></td></tr>
	</table>
</fieldset>
</c:if>
</div>

