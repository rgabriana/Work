<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<spring:url	value="/services/org/profiletemplate/delete" var="deleteTemplateUrl" scope="request" />
<spring:url	value="/profileTemplateManagement/" var="editTemplateUrl" scope="request" />
<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}	
	.ui-dialog-title{font-size: 1.4em; font-weight: bolder;}	
	table#templateMgmtTable td{overflow: hidden !important;}
	#templateMgmtMessage {float: left; font-weight: bold; padding: 5px 0 0 5px;}
</style>
<script type="text/javascript">
var PAGE = "${page}";
var deleteTemplateData = [];
	$(document).ready(function() {		
		
		//hide server message after 10 secs
		var hide_message_timer = setTimeout("clearTemplateMgmtMessage()", 10000);
		
		jQuery("#templateMgmtTable").jqGrid({
			datatype: "local",
			autowidth: true,
			scrollOffset: 18,
			forceFit: true,
			hoverrows: false,
			colNames:["id", "templateId","Name", "Profile associated","Action"],
		   	colModel:[
				{name:'id', index:'id', hidden: true},
				{name:'templateId', index:'templateId', hidden: true},
		   		{name:'name', index:'name', sorttype:"string", width:"40%"},
		   		{name:'profileCount', index:'profileCount',align:'center', sorttype:"string", width:"15%"},
		   		//{name:'fixtureCount', index:'fixtureCount', align:'center', sorttype:"string", width:"15%"},
		   		{name:'action', index:'action', align:"right", sortable:false, width:"15%"}
		   	],
		   	cmTemplate: { title: false },
 		    multiselect: true,
 		   	pager: '#templateMgmtPagingDiv',
		   	page: 1,
		   	sortname: 'name',
		    viewrecords: true,
		    hidegrid: false,
		    sortorder: "asc",
		    loadComplete: function() {
		    	 ModifyGridDefaultStyles();
		    }
		});
		
		forceFitTemplateTableHeight();
		var mydata =  [];
		
		<c:forEach items="${templateList}" var="template">
			var localData = new Object;
			localData.templateId = "${template.id}";
			localData.id = "${template.id}";
			localData.name = "${template.name}";
			localData.profileCount = "${template.profileCount}";
			localData.fixtureCount = "${template.fixtureCount}";
			localData.action = "";
			localData.action += "<button onclick=\"editTemplateDetails(${template.id})\">"+ 
													"<spring:message code='profiletemplate.label.editBtn' />"+
												"</button>";											
												
			mydata.push(localData);
		</c:forEach>				
		for(var i=0; i<=mydata.length; i++)
		{
			jQuery("#templateMgmtTable").jqGrid('addRowData', i+1, mydata[i]);
		}
		jQuery("#templateMgmtTable").jqGrid('navGrid',"#templateMgmtPagingDiv",{edit:false,add:false,del:false});
		$("#templateMgmtTable").jqGrid().setGridParam({sortname: 'name', sortorder:'asc'}).trigger("reloadGrid");
		
		 $(window).load(function() {
         	window.editTemplateDetails = function(templateId) {
         		clearTemplateMgmtMessage();
         		$("#templateMgmtDialog").load('${editTemplateUrl}'+templateId+"/edit.ems"+"?ts="+new Date().getTime(), function() {
	            		  $("#templateMgmtDialog").dialog({
	            			  title : "Edit Template",
	                          width :  425,
	                          minHeight : 135,
	                          modal : true
	            			});
	            });
         	return false;
           }	
         });
		
	});
	
	function forceFitTemplateTableHeight(){
		var jgrid = jQuery("#templateMgmtTable");
		var containerHeight = $(this).height();
		var otherElementHeight = $("#template-list-topPanel").height();
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .82)); 
	}

	function clearTemplateMgmtMessage(){
		$("#templateMgmtMessage").html("");
	}
	
	function beforeDeleteTemplate(){
		var selIds = jQuery("#templateMgmtTable").getGridParam('selarrrow');
		var templateNum = selIds.length;
		if(templateNum == 0 ){
			alert("Please select a template to delete");
			return false;
		}
		getSelectedTemplateToDelete();
		
	}
	function getSelectedTemplateToDelete(){
		var selIds =  jQuery("#templateMgmtTable").getGridParam('selarrrow');
		var fixNum = selIds.length;
		var deleteStatus = false;
		for(var i=0; i<fixNum; i++){
			var templateRow = jQuery("#templateMgmtTable").jqGrid('getRowData', selIds[i]);
			
			if(templateRow.profileCount>0)
			{
				 alert("Template has profiles associated with it. It cannot be deleted.");
				 deleteStatus =true;
				 break;
			}
			if(templateRow.fixtureCount>0)
			{
				 alert("Profiles in templates has fixture associated with it. It cannot be deleted.");
				 deleteStatus =true;
				 break;
			}
			
			 var templateJson = {};
			 templateJson.id = templateRow.templateId;
			 templateJson.name = templateRow.name;
			 deleteTemplateData.push(templateJson);	
		}
		if(!deleteStatus)
			deleteTemplate();
	}
	
	function getTemplateXMLData()
	{
		var xmldata ="";
		xmldata = "<profiletemplates>";
		for(var i=0; i<deleteTemplateData.length; i++){
			xmldata += "<profiletemplate>"+
			"<id>"+deleteTemplateData[i].id+"</id>"+
			"<name>"+deleteTemplateData[i].name+"</name>"+
			"</profiletemplate>";
		}
		xmldata += "</profiletemplates>";
		return xmldata;
	}
	
	function deleteTemplate(){
		var selIds = jQuery("#templateMgmtTable").getGridParam('selarrrow');
		var xmlData= getTemplateXMLData();
		var templateNum = selIds.length;
		var proceed = confirm("Are you sure you want to delete "+templateNum+ " selected templates");
		if(proceed){
		$.ajax({
			type: 'POST',
			url: "${deleteTemplateUrl}"+"?ts="+new Date().getTime(),
			data: xmlData,
			success: function(data){
				deleteTemplateData=[];
				reloadTemplateListFrame();
			},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8"
		});
		}
	}
	function reloadTemplateListFrame(){
		var ifr = parent.document.getElementById('templateSettingFrame');
		window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = ifr.src + new Date().getTime();
	}
  	var addTemplateDialog = function(url){
      	$("#templateMgmtDialog").load(url).dialog({
              title : "New Template",
              width :  425,
              height : 135,
              modal : true,
              close: function(event, ui) { location.href = "/ems/profileTemplateManagement/list.ems"; }
          });
          return false;
      };
      
  	function ModifyGridDefaultStyles() {  
  		   $('#' + "templateMgmtTable" + ' tr').removeClass("ui-widget-content");
  		   $('#' + "templateMgmtTable" + ' tr:nth-child(even)').addClass("evenTableRow");
  		   $('#' + "templateMgmtTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
  	}  
      
	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#templateMgmtTable").setGridWidth($(window).width()-20);
	}).trigger('resize');	
</script>
<div class="outermostdiv">
<div class="outerContainer">
<!-- 	<span>Template Management</span> -->
	<div class="i1"></div>
</div>
<div class="innerdiv">
<div id="errorMessage">
<div id="template-list-topPanel" style="background:#fff">
		<div id="template-dialog-form">
			<spring:url	value="/profileTemplateManagement/create.ems?"	var="actionAddTemplateURL" scope="request" />
			
			<button id="deleteFixtureButton" onclick="javascript: beforeDeleteTemplate();">Delete</button>
			
			<form id="addTemplateButton" name="addtemplate" method="post" action="${actionAddTemplateURL}" style="float: right;">
				<input type="button" id="addTemplateBtn" onClick="return addTemplateDialog('${actionAddTemplateURL}')" value="<spring:message code='profiletemplate.label.addtemplateBtn'/>" style="float: left; margin-right:5px" />
			</form>
			
			<div id="templateMgmtMessage">
				${message}
			</div>
		</div> 
		<br style="clear:both;"/>
		<div style="height:10px;"></div>
</div>
<table id="templateMgmtTable"></table>
<div id="templateMgmtDialog" style="overflow: hidden"></div>

<div id="templateMgmtPagingDiv"></div>
</div>
</div>