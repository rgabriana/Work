<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>


<spring:url	value="/services/org/plugloadTemplateManagement/delete" var="deletePlugloadTemplateUrl" scope="request" />
<spring:url	value="/plugloadProfileTemplateManagement/" var="editPlugloadTemplateUrl" scope="request" />
<style>
	/*Override JQuery Dialog modal background css */
	.ui-widget-overlay {background: none repeat scroll 50% 50% #000000 !important; opacity: 0.9 !important;}	
	.ui-dialog-title{font-size: 1.4em; font-weight: bolder;}	
	table#plugloadTemplateMgmtTable td{overflow: hidden !important;}
	#plugloadTemplateMgmtMessage {float: left; font-weight: bold; padding: 5px 0 0 5px;}
</style>
<script type="text/javascript">
var PAGE = "${page}";
var deletePlugloadTemplateData = [];
	$(document).ready(function() {		
		
		//hide server message after 10 secs
		var hide_plugload_template_message_timer = setTimeout("clearPlugloadTemplateMgmtMessage()", 10000);
		
		jQuery("#plugloadTemplateMgmtTable").jqGrid({
			datatype: "local",
			autowidth: true,
			scrollOffset: 18,
			forceFit: true,
			hoverrows: false,
			colNames:["id", "templateId","Name", "Plugload Profile associated","Plugload associated", "Action"],
		   	colModel:[
				{name:'id', index:'id', hidden: true},
				{name:'templateId', index:'templateId', hidden: true},
		   		{name:'name', index:'name', sorttype:"string", width:"40%"},
		   		{name:'plugloadProfileCount', index:'plugloadProfileCount',align:'center', sorttype:"string", width:"15%"},
		   		{name:'plugloadCount', index:'plugloadCount', align:'center', sorttype:"string", width:"15%"},
		   		{name:'action', index:'action', align:"right", sortable:false, width:"15%"}
		   	],
		   	cmTemplate: { title: false },
 		    multiselect: true,
 		   	pager: '#plugloadTemplateMgmtPagingDiv',
		   	page: 1,
		   	sortname: 'name',
		    viewrecords: true,
		    hidegrid: false,
		    sortorder: "asc",
		    loadComplete: function() {
		    	 ModifyPlugloadTemplateGridDefaultStyles();
		    }
		});
		
		forceFitPlugloadTemplateTableHeight();
		var mydata =  [];
		
		<c:forEach items="${plugloadProfileTemplateList}" var="template">
			var localData = new Object;
			localData.templateId = "${template.id}";
			localData.id = "${template.id}";
			localData.name = "${template.name}";
			localData.plugloadProfileCount = "${template.plugloadProfileCount}";
			localData.plugloadCount = "${template.plugloadCount}";
			localData.action = "";
			localData.action += "<button onclick=\"editPlugloadTemplateDetails(${template.id},'${template.name}')\">"+ 
													"<spring:message code='profiletemplate.label.editBtn' />"+
												"</button>";											
												
			mydata.push(localData);
		</c:forEach>				
		for(var i=0; i<=mydata.length; i++)
		{
			jQuery("#plugloadTemplateMgmtTable").jqGrid('addRowData', i+1, mydata[i]);
		}
		jQuery("#plugloadTemplateMgmtTable").jqGrid('navGrid',"#plugloadTemplateMgmtPagingDiv",{edit:false,add:false,del:false});
		$("#plugloadTemplateMgmtTable").jqGrid().setGridParam({sortname: 'name', sortorder:'asc'}).trigger("reloadGrid");
		
		 $(window).load(function() {
         	window.editPlugloadTemplateDetails = function(templateId,templateName) {
	         		if(templateName!="Default")
	         		{
	         			
		         		clearPlugloadTemplateMgmtMessage();
		         		$("#plugloadTemplateMgmtDialog").load('${editPlugloadTemplateUrl}'+templateId+"/edit.ems"+"?ts="+new Date().getTime(), function() {
			            		  $("#plugloadTemplateMgmtDialog").dialog({
			            			  title : "Edit Plugload Template",
			                          width :  425,
			                          minHeight : 135,
			                          modal : true
			            			});
			            });
		         		return false;
	         		}else
         			{
	         			 alert("Default Plugload Template cannot be edited.");
         			}
           }	
         });
		
	});
	
	function forceFitPlugloadTemplateTableHeight(){
		var jgrid = jQuery("#plugloadTemplateMgmtTable");
		var containerHeight = $(this).height();
		var otherElementHeight = $("#plugload-template-list-topPanel").height();
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .82)); 
	}

	function clearPlugloadTemplateMgmtMessage(){
		$("#plugloadTemplateMgmtMessage").html("");
	}
	
	function beforeDeleteTemplate(){
		var selIds = jQuery("#plugloadTemplateMgmtTable").getGridParam('selarrrow');
		var templateNum = selIds.length;
		if(templateNum == 0 ){
			alert("Please select a template to delete");
			return false;
		}
		getSelectedPlugloadTemplateToDelete();
		
	}
	function getSelectedPlugloadTemplateToDelete(){
		var selIds =  jQuery("#plugloadTemplateMgmtTable").getGridParam('selarrrow');
		var fixNum = selIds.length;
		var deleteStatus = false;
		for(var i=0; i<fixNum; i++){
			var templateRow = jQuery("#plugloadTemplateMgmtTable").jqGrid('getRowData', selIds[i]);
			
			if(templateRow.plugloadProfileCount>0)
			{
				 alert("Plugload Template has plugload profiles associated with it. It cannot be deleted.");
				 deleteStatus =true;
				 break;
			}
			if(templateRow.plugloadCount>0)
			{
				 alert("Plugload Profiles in Plugload templates has one or more plugloads associated with it. It cannot be deleted.");
				 deleteStatus =true;
				 break;
			}
			
			 var templateJson = {};
			 templateJson.id = templateRow.templateId;
			 templateJson.name = templateRow.name;
			 deletePlugloadTemplateData.push(templateJson);	
		}
		if(!deleteStatus)
			deletePlugloadTemplate();
	}
	
	function getPlugloadTemplateXMLData()
	{
		var xmldata ="";
		xmldata = "<plugloadProfileTemplates>";
		for(var i=0; i<deletePlugloadTemplateData.length; i++){
			xmldata += "<plugloadProfileTemplate>"+
			"<id>"+deletePlugloadTemplateData[i].id+"</id>"+
			"<name>"+deletePlugloadTemplateData[i].name+"</name>"+
			"</plugloadProfileTemplate>";
		}
		xmldata += "</plugloadProfileTemplates>";
		return xmldata;
	}
	
	function deletePlugloadTemplate(){
		var selIds = jQuery("#plugloadTemplateMgmtTable").getGridParam('selarrrow');
		var xmlData= getPlugloadTemplateXMLData();
		var templateNum = selIds.length;
		var proceed = confirm("Are you sure you want to delete "+templateNum+ " selected plugload templates");
		if(proceed){
		$.ajax({
			type: 'POST',
			url: "${deletePlugloadTemplateUrl}"+"?ts="+new Date().getTime(),
			data: xmlData,
			success: function(data){
				deletePlugloadTemplateData=[];
				reloadPlugloadTemplateListFrame();
			},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8"
		});
		}
	}
	function reloadPlugloadTemplateListFrame(){
		var ifr = parent.document.getElementById('plugloadtemplateSettingFrame');
		window.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
		ifr.src = ifr.src + new Date().getTime();
	}
  	var addPlugloadTemplateDialog = function(url){
      	$("#plugloadTemplateMgmtDialog").load(url).dialog({
              title : "New Plugload Template",
              width :  425,
              height : 135,
              modal : true,
              close: function(event, ui) { location.href = "/ems/plugloadProfileTemplateManagement/list.ems"; }
          });
          return false;
      };
      
  	function ModifyPlugloadTemplateGridDefaultStyles() {  
  		   $('#' + "plugloadTemplateMgmtTable" + ' tr').removeClass("ui-widget-content");
  		   $('#' + "plugloadTemplateMgmtTable" + ' tr:nth-child(even)').addClass("evenTableRow");
  		   $('#' + "plugloadTemplateMgmtTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
  	}  
      
	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#plugloadTemplateMgmtTable").setGridWidth($(window).width()-20);
	}).trigger('resize');	
</script>
<div class="outermostdiv">
<div class="outerContainer">
<!-- 	<span>Plugload Template Management</span> -->
	<div class="i1"></div>
</div>
<div class="innerdiv">
<div id="errorMessage">
<div id="plugload-template-list-topPanel" style="background:#fff">
		<div id="template-dialog-form">
			<spring:url	value="/plugloadProfileTemplateManagement/create.ems?"	var="actionAddPlugloadTemplateURL" scope="request" />
			
			<button id="deleteFixtureButton" onclick="javascript: beforeDeleteTemplate();">Delete</button>
			
			<form id="addTemplateButton" name="addtemplate" method="post" action="${actionAddPlugloadTemplateURL}" style="float: right;">
				<input type="button" id="addTemplateBtn" onClick="return addPlugloadTemplateDialog('${actionAddPlugloadTemplateURL}')" value="<spring:message code='profiletemplate.label.addtemplateBtn'/>" style="float: left; margin-right:5px" />
			</form>
			
			<div id="plugloadTemplateMgmtMessage">
				${message}
			</div>
		</div> 
		<br style="clear:both;"/>
		<div style="height:10px;"></div>
</div>
<table id="plugloadTemplateMgmtTable"></table>
<div id="plugloadTemplateMgmtDialog" style="overflow: hidden"></div>

<div id="plugloadTemplateMgmtPagingDiv"></div>
</div>
</div>