<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<spring:url	value="/services/org/profile/delete" var="deleteprofilesUrl" scope="request" />
<spring:url	value="/services/org/profile/exportprofile" var="exportprofilesUrl" scope="request" />
<spring:url	value="/services/org/profile/exportallprofile" var="exportallprofilesUrl" scope="request" />

<div id="profileMgmtDialog"></div>

<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}	
	#profileMgmtMessage {float: left; font-weight: bold; padding: 5px 0 0 5px;}
</style>

<div id="profile-list-topPanel" style="background:#fff;">
		<div id="profile-dialog-form" style="padding-top: 10px;">
			<span style="font-weight: bold;">Profile Management</span>
			<div style="height: 10px;"/></div>
			<input type="button" id="addProfileBtn" onClick="javascript: parent.parent.showProfileDetailsForm(-1,'new','false',templateId)" value="<spring:message code='profile.label.addprofileBtn'/>" style="float: left; margin-right:5px" />
			 <input type="button" id="importprofilebtn" onclick="javascript: parent.parent.onImportProfileHandler()"	value="Import profile"/>
			<input type="button" id="exportAllProfileBtn" onClick="exportAllProfiles(templateId)" value="<spring:message code='profile.label.exportallprofile'/>" style="float: left; margin-right:5px" />
			<br style="clear:both;"/>
			<div id="profileMgmtMessage">
				${message}
			</div>
		</div> 
		<br style="clear:both;"/>
</div>

<script type="text/javascript">
var PAGE = "${page}";
var templateId = "${templateId}";
var defaultProfileId = -1;
var deleteProfileData = [];

	$(document).ready(function() {
		
		//hide server message after 10 secs
		var hide_message_timer = setTimeout("clearProfileMgmtMessage()", 10000);
		
		jQuery("#profileMgmtTable").jqGrid({
			datatype: "local",
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			hoverrows: false,
			colNames:["id","profileId", "Name","Fixture associated", "Action","defaultProfile"],
		   	colModel:[
				{name:'id', index:'id', hidden: true},
				{name:'profileId', index:'profileId', hidden: true},
		   		{name:'name', index:'name', sorttype:"string", width:"40%",searchoptions:{sopt:['cn']}},
		   		{name:'fixtureCount', index:'fixtureCount', align:'center', sorttype:"string", width:"15%",search:false, },
		   		{name:'action', index:'action', align:"left", sortable:false, width:"15%",search:false, },
		   		{name:'defaultProfile', index:'defaultProfile', hidden: true,}
		   	],
		   	cmTemplate: { title: false },
 		    multiselect: false,
 		   	pager: '#profileMgmtPagingDiv',
		   	page: 1,
		   	sortname: 'name',
		    viewrecords: true,
		    hidegrid: false,
		    sortorder: "asc",
		    loadComplete: function() {
		    	 ModifyGridDefaultStyles();
		    	 deleteProfileData = [];
		    }
		});
		
		
		forceFitProfileTableHeight();
		
		var mydata =  [];
		var count=0;
		<c:forEach items="${profileList}" var="profile">
			var localData = new Object;
			localData.id = "${profile.id}";
			localData.profileId = "${profile.id}";
			localData.fixtureCount = "${profile.fixtureCount}";
			localData.defaultProfile = "${profile.defaultProfile}";
			localData.name = "${profile.name}";
			localData.action = "";
			
			if(localData.defaultProfile=="true")
			{
				defaultProfileId = localData.id;
				localData.action += "<button onclick=\"parent.parent.showProfileDetailsForm(${profile.id},'edit','true',templateId)\">"+ 
														"<spring:message code='profile.label.viewprofileBtn' />"+
													"</button>&nbsp;<button onclick=\"exportProfile(${profile.id})\">"+ 
														"<spring:message code='profile.label.exportprofile' />"+
													"</button>";
			}
			else
			{
				localData.action += "<button onclick=\"parent.parent.showProfileDetailsForm(${profile.id},'edit','false',templateId)\">"+ 
				"<spring:message code='profile.label.editBtn' />"+
				"</button>";
				
				localData.action += "&nbsp;<button onclick=\"beforeDeleteProfile(${profile.id},${profile.fixtureCount},${profile.defaultProfile})\">"+ 
				"<spring:message code='profile.label.deleteBtn' />"+
				"</button>";
				
				localData.action += "&nbsp;<button onclick=\"exportProfile(${profile.id})\">"+ 
				"<spring:message code='profile.label.exportprofile' />"+
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
			jQuery("#profileMgmtTable").jqGrid('addRowData', i+1, mydata[i]);
		}

		jQuery("#profileMgmtTable").jqGrid('navGrid',"#profileMgmtPagingDiv",{edit:false,add:false,del:false});

		$("#profileMgmtTable").jqGrid().setGridParam({sortname: 'name', sortorder:'asc'}).trigger("reloadGrid");
		
	});
	
	function forceFitProfileTableHeight(){
		var jgrid = jQuery("#profileMgmtTable");
		var containerHeight = $(this).height();
		var otherElementHeight = $("#profile-list-topPanel").height();
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .99)); 
	}

	function clearProfileMgmtMessage(){
		$("#profileMgmtMessage").html("");
	}
	
	function beforeDeleteProfile(profileId,fixtureCount,isDefaultProfile){
		var deleteStatus = false;
		if(isDefaultProfile=='true')
		{
			alert("Default Profile cannot be deleted.");
			deleteStatus =true;
		}else if(fixtureCount>0)
		{
			 alert("Profile has fixture(s) associated with it. It cannot be deleted.");
			 deleteStatus =true;
		}
		 var profileJson = {};
		 profileJson.id = profileId;
		 deleteProfileData.push(profileJson);	
		
		if(!deleteStatus)
		{
			deleteProfile(profileId);
		}
		
	}
	
	function getProfileXMLData(profileId)
	{
		var xmldata ="";
		xmldata = "<groups>";
			xmldata += "<group>"+
			"<id>"+profileId+"</id>"+
			"</group>";
		xmldata += "</groups>";
		return xmldata;
	}
	
	function deleteProfile(profileId){
		var xmlData= getProfileXMLData(profileId);
		var proceed = confirm("Are you sure you want to delete selected profile?");
		if(proceed){
			$.ajax({
				type: 'POST',
				url: "${deleteprofilesUrl}"+"?ts="+new Date().getTime(),
				data: xmlData,
				success: function(data){
					deleteProfileData=[];
					reloadProfileListFrame();
					parent.parent.refreshProfileTree();
				},
				dataType:"xml",
				contentType: "application/xml; charset=utf-8"
			});
		}
	}
	
	function exportProfile(profileId)
	{
		$("#exportProfileForm").attr("action", "${exportprofilesUrl}/"+profileId+"?ts="+new Date().getTime());
		$('#exportProfileForm').submit();
	}
	function exportAllProfiles(templateId)
	{
		var totalrecords = jQuery("#profileMgmtTable").jqGrid('getGridParam', 'records');
		if(totalrecords>0)
		{
			$("#exportProfileForm").attr("action", "${exportallprofilesUrl}/"+templateId+"?ts="+new Date().getTime());
			$('#exportProfileForm').submit();
		}else
		{
			alert("Atlease one profile should be present to export");
		}
	}
	function reloadProfileListFrame(){
		var ifr = parent.document.getElementById('templateFrame');
		window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = ifr.src;
	}

  	function ModifyGridDefaultStyles() {  
  		   $('#' + "profileMgmtTable" + ' tr').removeClass("ui-widget-content");
  		   $('#' + "profileMgmtTable" + ' tr:nth-child(even)').addClass("evenTableRow");
  		   $('#' + "profileMgmtTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
  	}  
     
	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#profileMgmtTable").setGridWidth($(window).width()-20);
	}).trigger('resize');	
</script>


<form id='exportProfileForm' action="${exportprofilesUrl}" method='GET' target="_blank">
</form>
<table id="profileMgmtTable"></table>
<div id="addprofileMgmtDialog" style="overflow: hidden"></div>

<div id="profileMgmtPagingDiv"></div>