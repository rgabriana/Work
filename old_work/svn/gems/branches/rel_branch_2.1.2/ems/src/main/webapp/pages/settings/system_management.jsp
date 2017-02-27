<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<spring:url value="/services/org/fixture/delete/all/discovered" var="deletealldicoveredfixture" scope="request" />
<script type="text/javascript">

function deleteAllDiscoveredFixture()
{
	var fixtureDeletedCount ;
	$.ajax({
        type: 'POST',
        url: "${deletealldicoveredfixture}",
        async:false,
        success: function(data){
        	
            if(data != null){
                var xml=data.getElementsByTagName("response");
                for (var j=0; j<xml.length; j++) {
                    var status = xml[j].getElementsByTagName("status")[0].childNodes[0].nodeValue;
                   fixtureDeletedCount= xml[j].getElementsByTagName("msg")[0].childNodes[0].nodeValue;
                 
                    }
                $("#deletefixturecount").html("Total Deleted Fixtures " + fixtureDeletedCount) ;
                }
            else
            	{
            	fixtureDeletedCount=0 ;
            	$("#deletefixturecount").html("Total Deleted Fixtures " + fixtureDeletedCount) ;
            	}
            }
        });	
	
	if(fixtureDeletedCount==undefined)
	{
	fixtureDeletedCount=0 ;
	$("#deletefixturecount").html("Total Deleted Fixtures " + fixtureDeletedCount) ;
	}
}
</script>
<div class="outermostdiv">
	
	<div class="outerContainer">
		<span><spring:message code="header.system.cleanup" /></span>
		<div class="i1"></div>
	</div>

	<div class="upperdiv"
		style="height: 35px; margin: 10px; padding: 10px;">
		<spring:url value="/settings/cleancache.ems" var="cleancache"
			scope="request" />
		<form action="${cleancache}">
			<input type="submit"
				value='<spring:message	code="system.cleanup.label.cleancache" />' />
		</form>
	</div>
	<div class="outerContainer">
		<span><spring:message code="header.system.masterGemsSetting" /></span>
		<div class="i1"></div>
	</div>

	<div class="upperdiv"
		style="height: 35px; margin: 10px; padding: 10px;">
		<spring:url value="/settings/master_gems_setting.ems"
			var="masterGemsSetting"  />
		<form action="${masterGemsSetting}">
			<input type="submit" value='<spring:message	code="system.cleanup.label.masterGemsSetting" />' />
		</form>
	</div>
	
	<div class="outerContainer">
	  <span>Discovered Fixtures Cleanup</span>
	  <div class="i1"></div>
	</div>

	<div class="upperdiv"
	  style="height: 35px; margin: 10px; padding: 10px;">
	 
	   <input type="submit" value='<spring:message  code="system.label.delete.all" />'  onclick="deleteAllDiscoveredFixture()"/> 
	   <label id="deletefixturecount" ></label>
	
	</div>
</div>