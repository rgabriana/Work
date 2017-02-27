<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<link rel="stylesheet" type="text/css" href="${gridcss}" />
<spring:url value="/services/org/site/v1/assignemtosite/" var="assignemtositeUrl" scope="request" />
<style>
#divform{width:100%;}
#form1{float:left;}
#btn1{float:left;}
#btn2{float:left;}
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay { background: none repeat scroll 50% 50% #000000; opacity: 0.9;overflow: auto;}
</style>

<script type="text/javascript">
var selectedEmData = [];
$(document).ready(function() {
    clearLabelMessage();
    startEMSiteMapping();
    $('#assignEmToSiteButton').click(function() {
    	assignEMToSite();
    	$("#assignEmToSiteButton").attr('disabled', 'disabled');
    });
});

function ModifySitesEmMappingGridDefaultStyles() {  
	   $('#' + "sitesEmMappingTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "sitesEmMappingTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "sitesEmMappingTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
}

function displayLabelMessage(Message, Color) {
		$("#message").html(Message);
		$("#message").css("color", Color);
}

function clearLabelMessage(Message, Color) {
		displayLabelMessage("", "black");
}

function startEMSiteMapping() {
		jQuery("#sitesEmMappingTable").jqGrid({
			datatype: "local",
			autoencode: true,
			hoverrows: false,
			autowidth: true,
			scrollOffset: 0,
			height: "200px",
			forceFit: true,
			colNames: ["id", "Name","Mac Id"],
		       colModel: [
		       { name:'id', index:'id', hidden: true},
		       { name: 'name', index: 'name',width:'50px'},
		       { name:'macId', index:'macId',width:'50px'}
		       ],
		   	cmTemplate: { title: false },
		    multiselect: true,
		    multiboxonly: true,
		   	sortorder: "desc",
		   	sortname: 'name',
		   	hidegrid: false,
		    viewrecords: true,
		   	loadui: "block",
		   	toolbar: [false,"top"],
		   	loadComplete: function(data) {
		   		ModifySitesEmMappingGridDefaultStyles();
		   	 loadGridData();
		   	}

		});
		jQuery("#sitesEmMappingTable").jqGrid('navGrid',"#sitesEmMappingPagingDiv",
										{edit:false,add:false,del:false,search:false}, 
										{}, 
										{}, 
										{}, 
										{},
										{});
	}
	function loadGridData()
	{
		var mydata =  [];
		<c:forEach items="${emList}" var="emObj">
			var localData = new Object();
			localData.id = "${emObj[0]}";
			localData.name = '<c:out value="${emObj[1]}" escapeXml="true" />';
			localData.macId = "${emObj[2]}";
			mydata.push(localData);
		</c:forEach>
		for(var i=0; i<=mydata.length; i++)
		{
			jQuery("#sitesEmMappingTable").jqGrid('addRowData', i+1, mydata[i]);
		}
		jQuery("#sitesEmMappingTable").jqGrid('navGrid',"#sitesEmMappingPagingDiv",{edit:false,add:false,del:false});
		$("#sitesEmMappingTable").jqGrid().setGridParam({sortname: 'name', sortorder:'desc'}).trigger("reloadGrid");
	}
	function closeEmSiteMappingDialog(){
		$("#assignEMToSiteDialog").dialog("close");
	}
	function assignEMToSite(){
		var selIds = jQuery("#sitesEmMappingTable").jqGrid().getGridParam('selarrrow');
		var emNum = selIds.length;
		var emXML= "<emInstances>";
		for(var i=0; i<emNum; i++){
			var em = jQuery("#sitesEmMappingTable").jqGrid('getRowData', selIds[i]);
			emXML+="<emInstance><id>"+em.id+"</id></emInstance>";
		}
		emXML+="</emInstances>";
		$.ajax({
			type: 'POST',
			url: "${assignemtositeUrl}"+"${siteId}",
			data: emXML,
			success: function(data){
				if(data != null){
					var xml=data.getElementsByTagName("response");
					for (var j=0; j<xml.length; j++) {
						var status = xml[j].getElementsByTagName("status")[0].childNodes[0].nodeValue;
						if(status>0){// success:
							displayLabelMessage("Energy manager successfully assigned to site"+ "${siteName}","green");
						}
					}
				}
			},
			complete: function(){
			},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8",
		});
	}
	
</script>
<div id="assignEMToSiteDialog"></div>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="outerDiv" style="padding: 5px 5px 5px 5px">
			<div>
				<div style="padding: 10px 0px 0px 0px"><button id="assignEmToSiteButton">Assign</button>&nbsp;<button id="cancelEmToSiteButton" onclick="closeEmSiteMappingDialog()">Cancel</button>
				</div>
					<div id="message" style="padding-top: 5px;"></div>	
			</div>	
			<div style="min-height:10px"></div>
    </div>
	<div style="padding: 0px 5px;">
		<table id="sitesEmMappingTable"></table>
		<div id="sitesEmMappingPagingDiv"></div>
	</div>
 </div>