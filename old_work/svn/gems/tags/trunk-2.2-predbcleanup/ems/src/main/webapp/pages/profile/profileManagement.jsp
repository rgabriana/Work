<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<spring:url	value="/services/org/profile/delete" var="deleteprofilesUrl" scope="request" />


<div id="profileMgmtDialog"></div>

<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}	
	#profileMgmtMessage {float: left; font-weight: bold; padding: 5px 0 0 5px;}
</style>

<div id="profile-list-topPanel" style="background:#fff">
		<div id="profile-dialog-form" style="padding-top: 10px;">
			<span style="font-weight: bold;">Profile Management</span>
			<div style="height: 10px;"/></div>
			<input type="button" id="addProfileBtn" onClick="javascript: parent.parent.showProfileDetailsForm(defaultProfileId,'new','false',templateId)" value="<spring:message code='profile.label.addprofileBtn'/>" style="float: left; margin-right:5px" />
			<button id="deleteFixtureButton" onclick="javascript: beforeDeleteProfile();">Delete</button>
			<div id="profileMgmtMessage">
				${message}
			</div>
		</div> 
		<br style="clear:both;"/>
</div>

<script type="text/javascript">
var PAGE = "${page}";
var templateId = "${templateId}";

var profileGrid = jQuery("#profileMgmtTable");
var defaultProfileId = 1;
var deleteProfileData = [];

	$(document).ready(function() {
		
		//hide server message after 10 secs
		var hide_message_timer = setTimeout("clearProfileMgmtMessage()", 10000);
		
		jQuery("#profileMgmtTable").jqGrid({
			datatype: "local",
			autowidth: true,
			scrollOffset: 18,
			forceFit: true,
			hoverrows: false,
			colNames:["id","profileId", "Name","Fixture associated", "Action","defaultProfile"],
		   	colModel:[
				{name:'id', index:'id', hidden: true},
				{name:'profileId', index:'profileId', hidden: true},
		   		{name:'name', index:'name', sorttype:"string", width:"40%"},
		   		{name:'fixtureCount', index:'fixtureCount', align:'center', sorttype:"string", width:"15%"},
		   		{name:'action', index:'action', align:"right", sortable:false, width:"15%"},
		   		{name:'defaultProfile', index:'defaultProfile', hidden: true}
		   	],
		   	cmTemplate: { title: false },
 		    multiselect: true,
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
		
		<c:forEach items="${profileList}" var="profile">
			var localData = new Object;
			localData.id = "${profile.id}";
			localData.profileId = "${profile.id}";
			localData.fixtureCount = "${profile.fixtureCount}";
			localData.defaultProfile = "${profile.defaultProfile}";
			if("${profile.defaultProfile}"=='true')
			{
				defaultProfileId = localData.id;
				localData.name = "${profile.name}_Default";
			}
			else
			{
				localData.name = "${profile.name}";
			}
			localData.action = "";
			if("${profile.defaultProfile}"=='true')
				localData.action += "<button onclick=\"parent.parent.showProfileDetailsForm(${profile.id},'edit','true',templateId)\">"+ 
														"<spring:message code='profile.label.viewprofileBtn' />"+
													"</button>";
			else
				localData.action += "<button onclick=\"parent.parent.showProfileDetailsForm(${profile.id},'edit','false',templateId)\">"+ 
				"<spring:message code='profile.label.editBtn' />"+
				"</button>";
												
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
	
	function beforeDeleteProfile(){
		var selIds = jQuery("#profileMgmtTable").getGridParam('selarrrow');
		var profileNum = selIds.length;
		if(profileNum == 0 ){
			alert("Please select a profile to delete");
			return false;
		}
		getSelectedProfilesToDelete();
		
	}
	function getSelectedProfilesToDelete(){
		var selIds =  jQuery("#profileMgmtTable").getGridParam('selarrrow');
		var fixNum = selIds.length;
		var deleteStatus = false;
		
		for(var i=0; i<fixNum; i++){
			var profileRow = jQuery("#profileMgmtTable").jqGrid('getRowData', selIds[i]);
			if(profileRow.defaultProfile=='true')
			{
				alert("Default Profile cannot be deleted. Please unselect it before deleting.");
				deleteStatus =true;
				break;
			}else if(profileRow.fixtureCount>0)
			{
				 alert("Profile has fixture associated with it. It cannot be deleted.");
				 deleteStatus =true;
				 break;
			}
			
			 var profileJson = {};
			 profileJson.id = profileRow.profileId;
			 deleteProfileData.push(profileJson);	
		}
		if(!deleteStatus)
		deleteProfile();
	}
	
	function getProfileXMLData()
	{
		var xmldata ="";
		xmldata = "<groups>";
		for(var i=0; i<deleteProfileData.length; i++){
			xmldata += "<group>"+
			"<id>"+deleteProfileData[i].id+"</id>"+
			"</group>";
		}
		xmldata += "</groups>";
		return xmldata;
	}
	
	function deleteProfile(){
		var selIds = jQuery("#profileMgmtTable").getGridParam('selarrrow');
		var xmlData= getProfileXMLData();
		var profileNum = selIds.length;
		var proceed = confirm("Are you sure you want to delete "+profileNum+ " selected profiles");
		if(proceed){
			$.ajax({
				type: 'POST',
				url: "${deleteprofilesUrl}"+"?ts="+new Date().getTime(),
				data: xmlData,
				success: function(data){
					deleteProfileData=[];
					reloadProfileListFrame();
				},
				dataType:"xml",
				contentType: "application/xml; charset=utf-8"
			});
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

<table id="profileMgmtTable"></table>
<div id="addprofileMgmtDialog" style="overflow: hidden"></div>

<div id="profileMgmtPagingDiv"></div>