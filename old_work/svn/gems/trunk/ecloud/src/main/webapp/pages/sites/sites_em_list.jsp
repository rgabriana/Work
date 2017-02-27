<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<spring:url value="/services/org/site/v1/loademinstbysiteid/" var="getEmInstbySiteIdUrl" scope="request" />
<spring:url value="/sites/assignemtositemapping.ems?siteId=${siteId}" var="assignEmToSiteMappingUrl" scope="request" />
<spring:url value="/services/org/site/v1/unassignemtosite/" var="unassignemtositeUrl" scope="request" />
<style>
#divform{width:100%;}
#form1{float:left;}
#btn1{float:left;}
#btn2{float:left;}
</style>

<script type="text/javascript">
var admin , systemadmin , supportadmin;
$(document).ready(function() {

    clearLabelMessage();

	startSitesEM('1####END', 1, "name", "desc");
	$("#sitesEmTable").setGridWidth($(window).width() - 25);


    $('#assignEnergyManagerButton').click(function() {
    	clearLabelMessage();
        $("#assignEMToSiteDialog").load("${assignEmToSiteMappingUrl}"+"&ts="+new Date().getTime()).dialog({
            title : "Assign Energy Managers to Site",
            minWidth :  700,
            minHeight : 0,
            modal : true,
            position: {my: "top", at: "top-20%", of: "#sitesEmTable"},
            resizable:false
        });
        return false;
    });

    $('div#assignEMToSiteDialog').bind('dialogclose', function(event) {
    	resetSiteEMList();
    });
});

function ModifySitesEmGridDefaultStyles() {
	   $('#' + "sitesEmTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "sitesEmTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "sitesEmTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#sitesEmTable").setGridWidth($(window).width()-25);
}

function displayLabelMessage(Message, Color) {
		$("#message").html(Message);
		$("#message").css("color", Color);
}

function clearLabelMessage(Message, Color) {
		displayLabelMessage("", "black");
}
function resetSiteEMList(){
	$("#sitesEmTable").jqGrid("GridUnload");
	startSitesEM("1####END", 1, "name", "desc");
}
//function for pagination
function startSitesEM(inputdata, pageNum, orderBy, orderWay) {
		jQuery("#sitesEmTable").jqGrid({
			url: "${getEmInstbySiteIdUrl}"+"${siteId}"+"?ts="+new Date().getTime(),
			mtype: "POST",
			postData: {"userData": inputdata},
			datatype: "json",
			autoencode: true,
			hoverrows: false,
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			formatter: {
				 integer: {thousandsSeparator: ",", defaultValue: '0'},
			     number: {decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, defaultValue: '0.00'}
			},
			colNames: ["id", "Name", "Version", "Mac Id","EM Address","Last connectivity","Call Home Health","Sync Connectivity","SSH Port","Action"],
		       colModel: [
		       { name:'id', index:'id', hidden: true},
		       { name: 'name', index: 'name',sortable:true,sorttype:'string',width:'10%'},
		       { name: "version", index: 'version', sortable:true,width:'6%'},
		       { name: "macId", index: 'macId', sortable:true,sorttype:'string',width:'8%'},
		       { name: "address", index: 'address', sortable:true,width:'8%'},
		       { name: "utcLastConnectivityAt", index: 'utcLastConnectivityAt',sortable:true,sorttype:'string',width:'10%'},
		       { name: "healthOfEmInstance", index: 'healthOfEmInstance',sortable:false,width:'10%'},
		       { name: "syncConnectivity", index: 'syncConnectivity',sortable:false,width:'10%'},
// 		       { name: "sppaPrice", index: 'sppaPrice',sortable:true,width:'6%'},
		       { name: "sshTunnelPort", index: 'sshTunnelPort',sortable:false,width:'6%'},
		       { name: "action", index: 'action',sortable:false,width:'8%', align: "right",formatter: viewSiteemlistActionFormatter}],

		   	jsonReader: {
				root:"emInsts",
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		   	page: pageNum,
		   	sortorder: orderWay,
		   	sortname: orderBy,
		   	hidegrid: false,
		    viewrecords: true,
		   	loadui: "block",
		   	toolbar: [false,"top"],
		   	onSortCol: function(index, iCol, sortOrder) {
		   		$('#orderWay').attr('value', sortOrder);
		   		$('#orderBy').attr('value', index);
		   	},
		   	loadComplete: function(data) {
		   		if (data != null){
		   			if (data.emInsts != undefined) {
				   		if (data.emInsts.length == undefined) {
				   			jQuery("#sitesEmTable").jqGrid('addRowData', 0, data.emInsts);
				   		}
				   	}
		   		}
		   		ModifySitesEmGridDefaultStyles();

		   	}

		});

		jQuery("#sitesEmTable").jqGrid('navGrid',"#sitesEmPagingDiv",
										{edit:false,add:false,del:false,search:false},
										{},
										{},
										{},
										{},
										{});

		forceFitSitesEmTableWidth();
	}
//function for pagination

	function forceFitSitesEmTableWidth(){
		var jgrid = jQuery("#sitesEmTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#sitesEmPagingDiv").height();

		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 75);

		$("#sitesEmTable").setGridWidth($(window).width() - 25);
	}

//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	$("#sitesEmTable").setGridWidth($(window).width()-25);
}).trigger('resize');

function onUnAssignEmFromSite(rowId)
{
	var proceed = confirm("Are you sure you want to unassign energy manager from this site?");
	if(proceed){
	var emXML= "<emInstances>";
	emXML+="<emInstance><id>"+rowId+"</id></emInstance>";
	emXML+="</emInstances>";
	$.ajax({
		type: 'POST',
		url: "${unassignemtositeUrl}"+"${siteId}",
		data: emXML,
		success: function(data){
			if(data != null){
				var xml=data.getElementsByTagName("response");
				for (var j=0; j<xml.length; j++) {
					var status = xml[j].getElementsByTagName("status")[0].childNodes[0].nodeValue;
					if(status>0){// success:
						resetSiteEMList();
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
}
function viewSiteemlistActionFormatter(cellvalue, options, rowObject) {
	var rowId = rowObject.id;
	return "<security:authorize access="hasAnyRole('Admin','SystemAdmin','SupportAdmin','SPPA')"><div id=\"form1\"><button onclick=\"onUnAssignEmFromSite("+rowId+");\">Unassign EM</button></div></security:authorize>";
}

</script>

<div id="assignEMToSiteDialog" style="overflow: hidden;"></div>

<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="outerDiv" style="padding: 5px 5px 5px 5px">
			<div>
			<div style="font-weight: bolder; "><spring:message code="emInstance.sites"/> ${siteName}</div>
				<div style="padding: 10px 0px 0px 0px"><security:authorize access="hasAnyRole('Admin','SystemAdmin','SupportAdmin','SPPA')"><button id="assignEnergyManagerButton">Assign Energy Managers</button></security:authorize>
				</div>
			</div>
			<div style="min-height:10px"></div>

    </div>
	<div style="padding: 0px 5px;">
		<table id="sitesEmTable"></table>
		<div id="sitesEmPagingDiv"></div>
	</div>
 </div>