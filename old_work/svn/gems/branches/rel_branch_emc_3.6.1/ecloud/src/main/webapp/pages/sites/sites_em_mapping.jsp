<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<spring:url value="/themes/standard/css/jquery/jqgrid/ui.jqgrid.css" var="gridcss" />
<spring:url value="/services/org/eminstance/loademinstbycustomerid/" var="getEmInstbyCustomerId" scope="request" />
<spring:url value="/services/org/site/v1/loademinstbysiteid/" var="getEmInstbySiteIdUrl" scope="request" />
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
    $('#searchString').val("");
    $("#searchColumn").val($("#searchColumn option:first").val());
    startEMSiteMapping('1####END', 1, "name", "desc");
    $('#assignEmToSiteButton').click(function() {
    	assignEMToSite();
    	refreshEmInstanceList();
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

function startEMSiteMapping(inputdata, pageNum, orderBy, orderWay) {
		jQuery("#sitesEmMappingTable").jqGrid({
			url: "${getEmInstbyCustomerId}"+"${customerId}"+"?ts="+new Date().getTime(),
			mtype: "POST",
			postData: {"userData": inputdata},
			datatype: "json",
			autoencode: true,
			hoverrows: false,
			autowidth: true,
			scrollOffset: 0,
			height: "100%",
			forceFit: true,
			colNames: ["id", "Name","Mac Id", "Version", "Address"],
		       colModel: [
			       { name:'id', index:'id', hidden: true},
			       { name: 'name', index: 'name', sortable:true, sorttype: 'string', width:'50px'},
			       { name: 'macId', index: 'macId', sortable:true, sorttype: 'string', width:'50px'},
			       { name: 'version', index: 'version', sortable:true, sorttype: 'string', width:'50px'},
			       { name: 'address', index: 'address', sortable:true, sorttype: 'string', width:'50px'}
		       ],

		   	jsonReader: {
				root:"emInsts",
		        page:"page",
		        total:"total",
		        records:"records",
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		   	pager: '#sitesEmMappingPagingDiv',
		    multiselect: true,
		    multiboxonly: true,
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
				   			jQuery("#sitesEmMappingTable").jqGrid('addRowData', 0, data.emInsts);
				   		}
				   	}
		   		}
		   		// Remove EMs that are already assigned to this site
		   		$.ajax({
					type: "POST",
			 		async:false,
					url: "${getEmInstbySiteIdUrl}"+"${siteId}"+"?ts="+new Date().getTime(),
					datatype: "json",
					success: function (data) {
						var emIds = $(data).find("id");
						var len = +$(emIds).length;
						for (var i=0; i<len; i++) {
							var rowId = $(emIds[i]).text();
							if (rowId) {
								jQuery("#sitesEmMappingTable").jqGrid('delRowData', rowId);
							}
 						}
					}
		   		})
		   		ModifySitesEmMappingGridDefaultStyles();
		   	},
		   	onSelectRow: function() {
			    clearLabelMessage();
		   	},
		   	onSelectAll: function() {
			    clearLabelMessage();
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
							displayLabelMessage("Energy manager(s) successfully assigned to site: "+ "${siteName}","green");
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

	// Called after new assignment
	function refreshEmInstanceList() {
		var column = $("#searchColumn").val();
		var search = encodeURIComponent($.trim($('#searchString').val()))
		if (column == "name" && !search) {
			resetEmInstanceList();	// No search parameters. Just display everything
		} else {
			searchEmInstanceList();	// Refresh using the current search criteria
		}
	}
	function onSearch() {
	    clearLabelMessage();
	    searchEmInstanceList();
	}
	function searchEmInstanceList(){
		var userdata = "1" + "#" + $("#searchColumn").val() + "#" +encodeURIComponent($.trim($('#searchString').val())) + "#" + "true" + "#" + "END";
		$("#sitesEmMappingTable").jqGrid("GridUnload");
		startEMSiteMapping(userdata, 1, "name", "desc");
	}

	function resetEmInstanceList(){
	    clearLabelMessage();
		$("#sitesEmMappingTable").jqGrid("GridUnload");
		$('#searchString').val("");
		$("#searchColumn").val($("#searchColumn option:first").val());
		startEMSiteMapping("1####END", 1, "name", "desc");
	}

</script>
<div id="assignEMToSiteDialog"></div>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="outerDiv" style="padding: 5px 5px 5px 5px">
			<div>
				<div style="float:right;padding: 0px 0px 0px 10px">
					<button id="resetEmInstButton" onclick="resetEmInstanceList()">Reset</button>
				</div>
				<div style="float:right;padding: 0px 0px 0px 10px">
					<button id="searchEmInstButton" onclick="onSearch()">Search</button>
				</div>
				<div style="float:right;padding: 0px 0px 0px 10px">
					<input type="text" name="searchString" id="searchString">
				</div>
				<div style="float:right;padding: 0px 0px 0px 10px">
					<select id="searchColumn">
					  <option value="name">Name</option>
					  <option value="version">Version</option>
					  <option value="macId">Mac Id</option>
					  <option value="address">Em Address</option>
					</select>
				</div>
				<div style="float:right;padding: 0px 0px 0px 10px;font-weight: bolder; ">
					<label>Search by</label>
				</div>
				<div style="padding: 10px 0px 0px 0px">
				     <button id="assignEmToSiteButton">Assign</button>&nbsp;
				     <button id="cancelEmToSiteButton" onclick="closeEmSiteMappingDialog()">Close</button>
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