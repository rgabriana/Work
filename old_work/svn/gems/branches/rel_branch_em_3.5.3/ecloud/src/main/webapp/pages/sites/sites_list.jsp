<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<spring:url value="/services/org/site/v1/getsitelistbycustomerid/" var="getSiteListByCustomerId" scope="request" />
<spring:url value="/sites/viewsitedetails.ems" var="getSiteDetailUrl" scope="request" />
<spring:url value="/sites/create.ems?customerId=${customerId}" var="createSiteUrl" scope="request" />
<spring:url value="/sites/edit.ems" var="editSiteUrl" scope="request" />
<spring:url value="/services/org/site/v1/checkassociatedsite/" var="checkAssociatedSiteUrl" scope="request" />
<spring:url value='/services/org/site/v1/delete/' var='deletSiteUrl' />
<spring:url value="/sites/viewallanomaliesdetails.ems" var="getSiteAnomaliesDetailUrl" scope="request" />
<spring:url value="/scripts/jquery/jquery.datetime.picker.0.9.9.js" var="jquerydatetimepicker"></spring:url>
<script type="text/javascript" src="${jquerydatetimepicker}"></script>
<style>
#divform{width:100%;}
#form1{float:left;}
#btn1{float:left;}
#btn2{float:left;}
</style>

<script type="text/javascript">
$(document).ready(function() {
    clearLabelMessage();
    $('#searchSiteString').val("");
    $("#searchSiteColumn").val($("#searchSiteColumn option:first").val());
    startSite('1####END', 1, "name", "asc");
	$("#siteTable").setGridWidth($(window).width() - 25);
	
	 $('#addSiteButton').click(function() {
	    	clearLabelMessage();
	        $("#siteFormDialog").load("${createSiteUrl}"+"&ts="+new Date().getTime()).dialog({
	            title : "New Site",
	            width :  Math.floor($('body').width() * .35),
	            minHeight : 250,
	            modal : true
	        });
	        return false;
	    });
	 
	 
	 $('#showAllAnomalies').click(function()
	 {
		 clearLabelMessage();
		 $('#customerId').val(${customerId});
		 $('#viewAllSiteAnamoliesDetailForm').submit();
	 });
});

function ModifySiteGridDefaultStyles() {  
	   $('#' + "siteTable" + ' tr').removeClass("ui-widget-content");
	   $('#' + "siteTable" + ' tr:nth-child(even)').addClass("evenTableRow");
	   $('#' + "siteTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	   $("#siteTable").setGridWidth($(window).width()-25);	
}

function displayLabelMessage(Message, Color) {
		$("#message").html(Message);
		$("#message").css("color", Color);
}

function clearLabelMessage(Message, Color) {
		displayLabelMessage("", "black");
}


//function for pagination
function startSite(inputdata, pageNum, orderBy, orderWay) {
		jQuery("#siteTable").jqGrid({
			url: "${getSiteListByCustomerId}"+"${customerId}"+"?ts="+new Date().getTime(),
			mtype: "POST",
			postData: {"userData": inputdata},
			datatype: "json",
			autoencode: true,
			hoverrows: false,
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			colNames: ["id", "Name", "Geo Location", "Region","SPPA Price","Block Purchase Energy","PO Number","Square Foot","Release date","Estimated Burn Hours","Details","Action"],
		       colModel: [
		       { name:'id', index:'id', hidden: true},
		       { name: 'name', index: 'name',sortable:true,sorttype:'string',width:'10%',searchoptions:{sopt:['cn']}},
		       { name: "geoLocation", index: 'geoLocation', sortable:true,width:'6%'},
		       { name: "region", index: 'region', sortable:true,sorttype:'string',width:'6%'},
		       { name: "sppaPrice", index: 'sppaPrice', sortable:true,sorttype:'string',width:'4%'},
		       { name: "blockPurchaseEnergy", index: 'blockPurchaseEnergy', sortable:true,width:'10%'},
		       { name: "poNumber", index: 'poNumber',sortable:true,sorttype:'string',width:'6%'},
		       { name: "squareFoot", index: 'squareFoot',sortable:false,width:'6%'},
		       { name: 'billStartDate', index: 'billStartDate', width: '10%', align: 'center', sorttype: 'string', formatter:sitelistcustomdateformatter},
		       { name: "estimatedBurnHours", index: 'estimatedBurnHours',sortable:false,width:'9%'},
		       { name: "details", index: 'details',sortable:false,width:'5%', align: "right",formatter: viewSiteDetailsFormatter},
		       { name: "action", index: 'action',sortable:false,width:'8%', align: "right",formatter: viewSiteActionFormatter}],
		   	jsonReader: { 
				root:"sites", 
		        page:"page", 
		        total:"total", 
		        records:"records", 
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    pager: '#sitePagingDiv',
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
			//footerrow: true,
		   	loadComplete: function(data) {
		   		if (data != null){
		   			if (data.sites != undefined) {
				   		if (data.sites.length == undefined) {
				   			jQuery("#siteTable").jqGrid('addRowData', 0, data.sites);
				   		}
				   	}
		   		}
		   		ModifySiteGridDefaultStyles();
		   	}
		});
		jQuery("#siteTable").jqGrid('navGrid',"#sitePagingDiv",{edit:false,add:false,del:false,search:false});
		forceFitSiteTableWidth();
	}
	//function for pagination

	function forceFitSiteTableWidth(){
		var jgrid = jQuery("#siteTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#sitePagingDiv").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 10);
		
		$("#siteTable").setGridWidth($(window).width() - 25);
	}
	function sitelistcustomdateformatter(cellvalue, options, rowObject)
	{
		  var date = new Date(cellvalue);
		  options = $.extend({}, $.jgrid.formatter.date, options);
          return $.fmatter.util.DateFormat("", date, 'Y-m-d H:i:s', options);
	}
	function viewSiteDetailsFormatter(cellvalue, options, rowObject) {
		var rowId = rowObject.id;
		return "<div id=\"form1\"><button onclick=\"onSiteDetails("+rowId+");\">Details</button>&nbsp;</div>";
	}
	function viewSiteActionFormatter(cellvalue, options, rowObject) {
		var rowId = rowObject.id;
		return "<security:authorize access="hasAnyRole('Admin','SystemAdmin','SupportAdmin','SPPA')"><div id=\"form1\"><button onclick=\"onSiteEdit("+rowId+");\">Edit</button>&nbsp;<button onclick=\"onBeforeSiteDelete("+rowId+");\">Delete</button></div></security:authorize>";
	}
	function onSiteEdit(rowId){
		$("#siteFormDialog").load("${editSiteUrl}?siteId="+rowId+"&ts="+new Date().getTime()).dialog({
	        title : "Edit Site",
	        width :  Math.floor($('body').width() * .40),
	        minHeight : 250,
	        modal : true
	    });
	}
	function onBeforeSiteDelete(rowId)
	{
		var proceed = confirm("Are you sure you want to delete selected site?");
		if(proceed){
			$.ajax({
				type: 'POST',
				url: "${checkAssociatedSiteUrl}"+rowId+"?ts="+new Date().getTime(),
				success: function(data){
					if(data.status==0)
					{
						deleteSite(rowId);
					}else
					{
						alert("Site cannot be deleted. Energy Managers are associated with this Site");
					}
				},
				dataType:"json",
				contentType: "application/json; charset=utf-8"
			});
		}
	}
	function onSiteDetails(rowId)
	{
		$('#siteId').val(rowId);
		$('#viewSiteDetailForm').submit();
	}
	
//this is to resize the jqGrid on resize of browser window.
$(window).bind('resize', function() {
	$("#siteTable").setGridWidth($(window).width()-25);
}).trigger('resize');

function deleteSite(rowId)
{
	$.ajax({
		type: 'POST',
		url: "${deletSiteUrl}"+rowId+"?ts="+new Date().getTime(),
		success: function(data){
			resetSiteList();
		},
		dataType:"json",
		contentType: "application/json; charset=utf-8"
	});
}
function searchSiteist(){
	var userdata = "1" + "#" + $("#searchSiteColumn").val() + "#" +encodeURIComponent($.trim($('#searchSiteString').val())) + "#" + "true" + "#" + "END";
	$("#siteTable").jqGrid("GridUnload");
	startSite(userdata, 1, "name", "desc");
}

function resetSiteList(){
	$("#siteTable").jqGrid("GridUnload");
	$('#searchSiteString').val("");
	$("#searchSiteColumn").val($("#searchColumn option:first").val());
	startSite("1####END", 1, "name", "desc");
}

</script>
 

<form id='viewSiteDetailForm' action="${getSiteDetailUrl}" method='POST'>
<input id="siteId" name="siteId" type="hidden"/>
</form>


<form id='viewAllSiteAnamoliesDetailForm' action="${getSiteAnomaliesDetailUrl}" method='POST'>
<input id="customerId" name="customerId" type="hidden"/>
</form>
<div id="siteFormDialog"></div>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="outerDiv" style="padding: 5px 5px 5px 5px">
			<div>
					<security:authorize access="hasAnyRole('Admin','SystemAdmin','SupportAdmin','SPPA')"><div style="font-weight: bolder; "><spring:message code="sites.customer"/> ${customerName}</div></security:authorize>
					<security:authorize access="hasAnyRole('Admin','SystemAdmin','SupportAdmin','SPPA')"><div style="padding: 10px 0px 0px 0px;font-weight: bolder; "><button id="addSiteButton">Add</button>&nbsp;<button id="showAllAnomalies">Anomalies</button></security:authorize>
					<security:authorize access="hasAnyRole('ThirdPartySupportAdmin')"><div style="padding: 10px 0px 10px 0px;font-weight: bolder; "><spring:message code="sites.customer"/> ${customerName}&nbsp;<button id="showAllAnomalies">Anomalies</button></security:authorize>
					<div style="float:right;padding: 0px 0px 0px 10px">
						<button id="resetSiteButton" onclick="resetSiteList()">Reset</button>
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px">
						<button id="searchSiteButton" onclick="searchSiteist()">Search</button>
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px">
						<input type="text" name="searchSiteString" id="searchSiteString">
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px">
						<select id="searchSiteColumn">
						  <option value="name">Name</option>
						  <option value="geoLocation">Geo Location</option>
						  <option value="region">Region</option>
						  <option value="poNumber">PO Number</option>
						</select>
					</div>
					<div style="float:right;padding: 0px 0px 0px 10px;">
						<label>Search by</label>
					</div>
				</div>	
			</div>	
    </div>
	<div style="padding: 0px 5px;">
		<table id="siteTable"></table>
		<div id="sitePagingDiv"></div>
	</div>
 </div>