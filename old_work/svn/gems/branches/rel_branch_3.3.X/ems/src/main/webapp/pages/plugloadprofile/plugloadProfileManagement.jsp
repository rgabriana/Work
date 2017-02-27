<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<spring:url	value="/services/org/plugloadProfile/delete" var="deleteplugloadprofilesUrl" scope="request" />
<spring:url	value="/services/org/plugloadProfile/exportplugloadprofile" var="exportplugloadprofilesUrl" scope="request" />
<spring:url	value="/services/org/plugloadProfile/exportallplugloadprofile" var="exportallplugloadprofilesUrl" scope="request" />

<div id="plugloadProfileMgmtDialog"></div>

<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}	
	#profileMgmtMessage {float: left; font-weight: bold; padding: 5px 0 0 5px;}
</style>

<div id="plugload-profile-list-topPanel" style="background:#fff;">
		<div id="profile-dialog-form" style="padding-top: 10px;">
			<span style="font-weight: bold;">Plugload Profile Management</span>
			<div style="height: 10px;"/></div>
			<input type="button" id="addPlugloadProfileBtn" onClick="javascript: parent.parent.showPlugloadProfileDetailsForm(-1,'new','false',plugloadTemplateId)" value="<spring:message code='profile.label.addprofileBtn'/>" style="float: left; margin-right:5px" />
			<input type="button" id="importplugloadprofilebtn" onclick="javascript: parent.parent.onImportPlugloadProfileHandler()"	value="<spring:message code='plugloadprofile.label.importplugloadprofile'/>"/>
			<input type="button" id="exportAllPlugloadProfileBtn" onClick="exportAllPlugloadProfiles(plugloadTemplateId)" value="<spring:message code='plugloadprofile.label.exportallplugloadprofile'/>" style="float: left; margin-right:5px" />
			<br style="clear:both;"/>
			<div id="profileMgmtMessage">
				${message}
			</div>
		</div> 
		<br style="clear:both;"/>
</div>

<script type="text/javascript">
var PAGE = "${page}";
var plugloadTemplateId = "${plugloadTemplateId}";
var defaultProfileId = -1;
var deletePlugloadProfileData = [];

	$(document).ready(function() {
		
		//hide server message after 10 secs
		var hide_message_timer = setTimeout("clearProfileMgmtMessage()", 10000);
		
		jQuery("#plugloadProfileMgmtTable").jqGrid({
			datatype: "local",
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			hoverrows: false,
			colNames:["id","plugloadProfileId", "Name","Plugload associated", "Action","defaultProfile"],
		   	colModel:[
				{name:'id', index:'id', hidden: true},
				{name:'plugloadProfileId', index:'plugloadProfileId', hidden: true},
		   		{name:'name', index:'name', sorttype:"string", width:"40%",searchoptions:{sopt:['cn']}},
		   		{name:'plugloadCount', index:'plugloadCount', align:'center', sorttype:"string", width:"15%",search:false, },
		   		{name:'action', index:'action', align:"left", sortable:false, width:"15%",search:false, },
		   		{name:'defaultProfile', index:'defaultProfile', hidden: true,}
		   	],
		   	cmTemplate: { title: false },
 		    multiselect: false,
 		   	pager: '#plugloadProfileMgmtPagingDiv',
		   	page: 1,
		   	sortname: 'name',
		    viewrecords: true,
		    hidegrid: false,
		    sortorder: "asc",
		    loadComplete: function() {
		    	 ModifyGridDefaultStyles();
		    	 deletePlugloadProfileData = [];
		    }
		});
		
		
		forceFitPlugloadProfileTableHeight();
		
		var mydata =  [];
		var count=0;
		<c:forEach items="${plugloadProfileList}" var="plugloadProfile">
			var localData = new Object;
			localData.id = "${plugloadProfile.id}";
			localData.plugloadProfileId = "${plugloadProfile.id}";
			localData.plugloadCount = "${plugloadProfile.plugloadCount}";
			localData.defaultProfile = "${plugloadProfile.defaultProfile}";
			localData.name = "${plugloadProfile.name}";
			localData.action = "";
			
			if(localData.defaultProfile=="true")
			{
				defaultProfileId = localData.id;
				localData.action += "<button onclick=\"parent.parent.showPlugloadProfileDetailsForm(${plugloadProfile.id},'edit','true',plugloadTemplateId)\">"+ 
															"<spring:message code='profile.label.viewprofileBtn' />"+
															"</button>&nbsp;<button onclick=\"exportPlugloadProfile(${plugloadProfile.id})\">"+ 
															"<spring:message code='plugloadprofile.label.exportplugloadprofile' />"+
														"</button>";
			}
			else
			{
				localData.action += "<button onclick=\"parent.parent.showPlugloadProfileDetailsForm(${plugloadProfile.id},'edit','false',plugloadTemplateId)\">"+ 
				"<spring:message code='profile.label.editBtn' />"+
				"</button>";
				
				localData.action += "&nbsp;<button onclick=\"beforeDeletePlugloadProfile(${plugloadProfile.id},${plugloadProfile.plugloadCount},${plugloadProfile.defaultProfile})\">"+ 
				"<spring:message code='profile.label.deleteBtn' />"+
				"</button>";
				
				localData.action += "&nbsp;<button onclick=\"exportPlugloadProfile(${plugloadProfile.id})\">"+ 
				"<spring:message code='plugloadprofile.label.exportplugloadprofile' />"+
				"</button>";
				//	If Default profile is not present then  store the default ID as first element from the profiles list
				if(count==0)
				{
					defaultProfileId = localData.id;
				}
			}
			
			count++;							
			mydata.push(localData);
		</c:forEach>

		for(var i=0; i<=mydata.length; i++)
		{
			jQuery("#plugloadProfileMgmtTable").jqGrid('addRowData', i+1, mydata[i]);
		}

		jQuery("#plugloadProfileMgmtTable").jqGrid('navGrid',"#plugloadProfileMgmtPagingDiv",{edit:false,add:false,del:false});

		$("#plugloadProfileMgmtTable").jqGrid().setGridParam({sortname: 'name', sortorder:'asc'}).trigger("reloadGrid");
		
	});
	
	function forceFitPlugloadProfileTableHeight(){
		var jgrid = jQuery("#plugloadProfileMgmtTable");
		var containerHeight = $(this).height();
		var otherElementHeight = $("#plugload-profile-list-topPanel").height();
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .97)); 
	}

	function clearProfileMgmtMessage(){
		$("#profileMgmtMessage").html("");
	}
	
	function beforeDeletePlugloadProfile(profileId,plugloadCount,isDefaultProfile){
		var deleteStatus = false;
		if(isDefaultProfile=='true')
		{
			alert("Default Plugload Profile cannot be deleted.");
			deleteStatus =true;
		}else if(plugloadCount>0)
		{
			 alert("Plugload Profile has plugload(s) associated with it. It cannot be deleted.");
			 deleteStatus =true;
		}
		 var profileJson = {};
		 profileJson.id = profileId;
		 deletePlugloadProfileData.push(profileJson);	
		
		if(!deleteStatus)
		{
			deletePlugloadProfile(profileId);
		}
		
	}
	
	function getPlugloadProfileXMLData(profileId)
	{
		var xmldata ="";
		xmldata = "<PlugloadGroupss>";
			xmldata += "<plugloadGroups>"+
			"<id>"+profileId+"</id>"+
			"</plugloadGroups>";
		xmldata += "</PlugloadGroupss>";
		return xmldata;
	}
	
	function deletePlugloadProfile(profileId){
		var xmlData= getPlugloadProfileXMLData(profileId);
		var proceed = confirm("Are you sure you want to delete selected profile?");
		if(proceed){
			$.ajax({
				type: 'POST',
				url: "${deleteplugloadprofilesUrl}"+"?ts="+new Date().getTime(),
				data: xmlData,
				success: function(data){
					deletePlugloadProfileData=[];
					reloadPlugloadProfileListFrame();
					//parent.parent.refreshPlugloadProfileTree();
					location.reload();
				},
				dataType:"xml",
				contentType: "application/xml; charset=utf-8"
			});
		}
	}
	
	function exportPlugloadProfile(plugloadProfileId)
	{
		$("#exportPlugloadProfileForm").attr("action", "${exportplugloadprofilesUrl}/"+plugloadProfileId+"?ts="+new Date().getTime());
		$('#exportPlugloadProfileForm').submit();
	}
	function exportAllPlugloadProfiles(plugloadTemplateId)
	{
		var totalrecords = jQuery("#plugloadProfileMgmtTable").jqGrid('getGridParam', 'records');
		if(totalrecords>0)
		{
			$("#exportPlugloadProfileForm").attr("action", "${exportallplugloadprofilesUrl}/"+plugloadTemplateId+"?ts="+new Date().getTime());
			$('#exportPlugloadProfileForm').submit();
		}else
		{
			alert("Atlease one plugload profile should be present to export");
		}
	}
	function reloadPlugloadProfileListFrame(){
		var ifr = parent.document.getElementById('plugloadtemplateSettingsFrame');
		window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = ifr.src;
	}

  	function ModifyGridDefaultStyles() {  
  		   $('#' + "plugloadProfileMgmtTable" + ' tr').removeClass("ui-widget-content");
  		   $('#' + "plugloadProfileMgmtTable" + ' tr:nth-child(even)').addClass("evenTableRow");
  		   $('#' + "plugloadProfileMgmtTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
  	}  
     
	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#plugloadProfileMgmtTable").setGridWidth($(window).width()-20);
	}).trigger('resize');	
</script>


<form id='exportPlugloadProfileForm' action="${exportplugloadprofilesUrl}" method='GET' target="_blank">
</form>
<table id="plugloadProfileMgmtTable"></table>
<div id="addplugloadProfileMgmtDialog" style="overflow: hidden"></div>

<div id="plugloadProfileMgmtPagingDiv"></div>