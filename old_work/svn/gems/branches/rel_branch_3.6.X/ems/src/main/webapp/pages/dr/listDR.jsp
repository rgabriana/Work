<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<spring:url value="/scripts/jquery/jquery.datetime.picker.0.9.9.js" var="jquerydatetimepicker"></spring:url>
<script type="text/javascript" src="${jquerydatetimepicker}"></script>
<spring:url value="/scripts/jquery/jquery.validationEngine.js" var="jquery_validationEngine"></spring:url>
<script type="text/javascript" src="${jquery_validationEngine}"></script>
<spring:url value="/scripts/jquery/jquery.validationEngine-en.js" var="jquery_validationEngine_en"></spring:url>
<script type="text/javascript" src="${jquery_validationEngine_en}"></script>
<spring:url value="/services/org/dr/updateflag/id/" var="updateFlags" scope="request" />

<spring:url value="/services/org/dr/start/" var="startDRUrl" scope="request" />
<spring:url value="/services/org/dr/list/manualdr/showAllChecked/" var="listMDR" scope="request"/>
<spring:url value="/services/org/dr/delete/id/" var="deleteDr" scope="request"/>
<spring:url value="/services/org/dr/delete/multiple" var="deleteMultiDr" scope="request"/>
<spring:url value="/services/org/dr/cancel/multiple" var="cancelMultiDr" scope="request"/>
<spring:url value="/dr/prompt.ems?drId=" var="drPrompt" scope="request"/>

<fmt:setLocale value="en_US"/>
<div id="drDetailsDialog"></div>
<div class="topmostContainer">
<div class="outermostdiv">
<div class="outerContainer">
	<span ><spring:message code="manual.override.scheduels.header"/></span>
	<div style="padding: 0px 0px 0px 100px; display: inline;"><span id="drmessage"></span></div>
	<div class="i1"></div>
	<div>		
		<div>
		<input id="addoverride" type="button" value="Add Override"></input>
		<input id="cancelselected" type="button" value="Cancel Selected" onclick="javascript: beforeCancelDeleteMultiDRs(this);"></input>
		<input id="deleteselected" type="button" value="Delete Selected" onclick="javascript: beforeCancelDeleteMultiDRs(this);"></input>
		<input type="checkbox" id="showall" name="showall" value="showallvalue" onclick='showAllClicked(this);'/><b>Show All</b>
		</div>
	</div>
	<div class="i1"></div>
	<div class="innerdiv">
	    <div id ="dr">
	        <table id="weekdayTable" style="width: 100%; height: 100%;" ></table><!-- class="entable"-->			
		    <div id="weekdayPagingDiv"></div>
	    </div>		
		<br/><hr/><hr/><br/>
		<div id ="cbdiv" style="float: right; margin-right: 12px;"><input type="checkbox" name="drtabletype" value="mvalue" onclick='clicked(this);' /><b>Show Only Cancelled/Completed</b><br></div>
		<span><spring:message code="dr.openadr.header"/></span>
		<div class="i1"></div>
		<div id ="dr1">
					<table id="drTable"></table>
					<div id="drPagingDiv"></div>
		</div>
	</div>
</div>
</div>
</div>

<script type="text/javascript">
var url="";
function clicked(cb)
{
	if(cb.checked==true)
	{	
		url = '<spring:url value="/services/org/dr/list/alternate/cancom"/>';
	}
	else
	{	
		url = '<spring:url value="/services/org/dr/list/alternate/filter"/>';
	}
	jQuery("#drTable").jqGrid('setGridParam',{url:url,page:1}).trigger("reloadGrid");
}

function showAllClicked(chkbox){
	$("#weekdayTable").jqGrid().setGridParam({sortname: "starttime", sortorder: "desc", url:"${listMDR}"+chkbox.checked, page:1}).trigger("reloadGrid");	
}

$(document).ready(function() {
	startUpperTable(1, "starttime", "desc");	
	url = '<spring:url value="/services/org/dr/list/alternate/filter"/>';		
	startLowerTable(1, "status", "desc",url);	

    $('#addoverride').click(function() {
        $("#drDetailsDialog").load("${drPrompt}"+"-1").dialog({
            title : "New Override",
            width :  Math.floor($('body').width() * .35),
            minHeight : 300,
            modal : true
        });
        return false;
    });   
});

function closeDialog(){
	$("#drDetailsDialog").dialog('close');        	
}

function startUpperTable(pageNum, orderBy, orderWay) {
	var isChecked = $('#showall').is(":checked");
	jQuery("#weekdayTable").jqGrid({
		url: "${listMDR}"+isChecked,
		mtype: "POST",			
		datatype: "json",
		autoencode: true,
		hoverrows: false,
		autowidth: true,
		scrollOffset: 0,
		forceFit: true,
		formatter: {
			 integer:{thousandsSeparator: ",", defaultValue: '0'},
		     number: {decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, defaultValue: '0.00'}
		},
		colNames:["id", "Event Type", "Description", "Start Time", "Duration(min)", "Pricing(per kWh)", "Override Level", "Status", "Action"],
	   	colModel:[			
			{name:'id', index:'id', hidden:true},			
	   		{name:'drtype', index:'drtype', align:"center", sorttype:"string", width:"15%", searchoptions:{sopt:['cn']}, formatter:eventTypeRenderer},
	   		{name:'description', index:'description', align:'center', sorttype:"string", width:"18%", search:false, },
	   		{name:'starttime', index:'starttime', align:"center", sortable:true, width:"18%", formatter:datetimeRenderer},
	   		{name:'duration', index:'duration', align:"center", sortable:true, sorttype:"number", width:"15%", search:false, formatter:durationRenderer},
	   		{name:'pricing', index:'pricing', align:"center", sortable:true, width:"15%", search:false, formatter:pricingRenderer},
	   		{name:'pricelevel', index:'pricelevel', align:"center", width:"15%", sortable:true, sorttype:"string", search:false, },
	   		{name:'drstatus', index:'drstatus', align:"center", sortable:true, sorttype:"string", width:"18%", search:false, },
	   		{name:'action', index:'action', align:"center", sortable:false, width:"22%", search:false, formatter:actionRenderer},	   		
	   	],
	   	jsonReader: { 
	        root:"drtarget", 
	        page:"page", 
	        total:"total", 
	        records:"records",
	        repeatitems:false,
	        id : "id"
	   	},
	   	cmTemplate: { title: false },
	    multiselect: true,
	    multiboxonly: true,
		pager: '#weekdayPagingDiv',
	   	page: pageNum,
	   	sortorder: orderWay,
	   	sortname: orderBy,
	   	rowNum:5,
	    hidegrid: false,
	    viewrecords: true,
	   	toolbar: [false,"top"],
	   	onSortCol: function(index, iCol, sortOrder) {
	   		$('#orderWay').attr('value', sortOrder);
	   		$('#orderBy').attr('value', index);
	   	},
	   	loadComplete: function(data) {
	   		if (data.drtarget != undefined) {
		   		if (data.drtarget.length == undefined) {
		   			// Hack: Currently, JSON serialization via jersey treats single item differently			   			
		   			jQuery("#weekdayTable").jqGrid('addRowData', 0, data.drtarget);
		   		}
		   	}	   		
	   	}
	});
		
	$("#weekdayTable").jqGrid().setGridParam({sortname: orderBy, sortorder: orderWay}).trigger("reloadGrid");
}
  
    jQuery("#weekdayTable").jqGrid('navGrid',"#weekdayPagingDiv",{edit:false,add:false,del:false});

function startLowerTable(pageNum, orderBy, orderWay,url) {
		jQuery("#drTable").jqGrid({
			url: url,
			mtype: "POST",			
			datatype: "json",
			autoencode: true,
			hoverrows: false,
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			formatter: {
				 integer:{thousandsSeparator: ",", defaultValue: '0'},
			     number: {decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, defaultValue: '0.00'}
			},
		   	colNames:["id", "Price Level","Pricing", "Duration (in seconds)", "Start Time", "Priority", "Dr Identifier", "Status","Start After (in seconds)","Opt out"],
		   	colModel:[
				{name:'id', index:'id', hidden:true},
				{name:'pricelevel', index:'pricelevel', align:"center", sortable:true, width:"8%", search:false},
				{name:'pricing', index:'pricing', align:"center", sortable:true, width:"8%", search:false},
				{name:'duration', index:'duration', align:"center", sortable:true, width:"8%", search:false},
				{name:'starttime', index:'starttime', align:"center", sortable:true, width:"8%", search:false,formatter:datetimeRenderer},
				{name:'priority', index:'priority', align:"center", sortable:true, width:"8%", search:false,formatter: zeropRenderer},
				{name:'dridentifier', index:'dridentifier', align:"center", sortable:true, width:"8%", search:false},
				{name:'drstatus', index:'drstatus', align:"center", sortable:true, width:"8%", search:false},
				{name:'jitter', index:'jitter', align:"center", sortable:true, width:"8%", search:false, formatter: convertToSeconds},
				{name:'optin', index:'optin', align:"center", sortable:true, width:"8%", search:false,formatter: optinButtonRenderer}	   		
		   	],
		   	jsonReader: { 
		        root:"drtarget", 
		        page:"page", 
		        total:"total", 
		        records:"records",
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    multiselect: false,
 		   	pager: '#drPagingDiv',
		   	page: pageNum,
		   	sortorder: orderWay,
		   	sortname: orderBy,
		    hidegrid: false,
		    viewrecords: true,
		   	toolbar: [false,"top"],
		   	onSortCol: function(index, iCol, sortOrder) {
		   		$('#orderWay').attr('value', sortOrder);
		   		$('#orderBy').attr('value', index);
		   	},
		   	loadComplete: function(data) {
		   		if (data.drtarget != undefined) {
			   		if (data.drtarget.length == undefined) {
			   			// Hack: Currently, JSON serialization via jersey treats single item differently			   			
			   			jQuery("#drTable").jqGrid('addRowData', 0, data.drtarget);
			   		}
			   	}		   		
		   		ModifyGridDefaultStyles();
				forceFitdrTableHeight();		   		
		   	}
		});
	}
		jQuery("#drTable").jqGrid('navGrid',"#drPagingDiv",{edit:false,add:false,del:false});		
		
		
function ModifyGridDefaultStyles() {  
		   $('#' + "drTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "drTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "drTable" + ' tr:nth-child(odd)').addClass("oddTableRow");		   
}
	
function forceFitdrTableHeight(){
		var jgrid = jQuery("#drTable");
		var containerHeight = $(this).height();		
		var weekdayTableHeight = $("#weekdayTable").height();
		var innderDivHeight = $("#innerdiv").height();		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		var otherElementHeight = innderDivHeight - weekdayTableHeight;  
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .28)); 
}

function optinRow(id,drId)
{
var verify=confirm("This will opt out all the events associated with Dr identifier : " + drId + ". Do you wish to continue?");
if (verify==true)
  {
	$.ajax({
		type: 'POST',
		url: "${updateFlags}"+id+"/flag/"+"false",		
		success: function(data){
			//reload the grid
			var page = $('#drTable').getGridParam('page');			
			$("#drTable").trigger("reloadGrid", [{current:true}]);			
		},
		dataType:"xml",
		contentType: "application/xml; charset=utf-8"
	});
  }
else
  {
  return;
  }
}

function datetimeRenderer(cellvalue, options, rowObject){
		
		var dateString = "";
		var ds1 = rowObject.starttime;
		var ds2 = ds1.substring(0,19);
		dateString = ds2.replace('T',' ');
		
		return dateString;
	}
	
function zeropRenderer(cellvalue, options, rowObject){
		var source = "";
		if (rowObject.priority == '2147483647') {           
				source = "<label>0</label>";
		} 
		else
		{
				source = "<label>" + rowObject.priority + "</label>";
		}
		return source;
	}
	
function convertToSeconds(cellvalue, options, rowObject) {
	var source = (rowObject.jitter)/1000;
	return source;
}
	
function optinButtonRenderer(cellvalue, options, rowObject){
		var source = "";
		if (rowObject.optin == 'true') {
				source = "<button onclick=\"optinRow(" + rowObject.id + ", '"+ rowObject.dridentifier +"')\"><spring:message code='dr.button.optin' /></button>";
		} 
		else
		{
				source = "<label><spring:message code='dr.label.optedout'/></label>";
		}
		return source;
	}

function optinButtonRendererCanCom(cellvalue, options, rowObject){
		var source = "";
		if (rowObject.optin == 'true') {
				source = "<label><spring:message code='dr.label.notoptedout'/></label>";
		} 
		else
		{
				source = "<label><spring:message code='dr.label.optedout'/></label>";
		}
		return source;
	}

function eventTypeRenderer(cellvalue, options, rowObject){
	var eventType = "";	
	eventType = (rowObject.drtype == 'MANUAL' ? 'DR' : 'Holiday');
	
	return eventType;	
}

function durationRenderer(cellvalue, options, rowObject){
	var duration =  "";	
	duration = Math.round(rowObject.duration/60).toString();
	
	return duration;	
}

function pricingRenderer(cellvalue, options, rowObject){
	var pricing = "";
	pricing = ((rowObject.pricing == "" || rowObject.pricing == null) ? 'NA' : rowObject.pricing);
	
	return pricing;
}

function actionRenderer(cellvalue, options, rowObject){	
	var drId = rowObject.id;	
	
	var eventType = "";	
	eventType = (rowObject.drtype == 'MANUAL' ? 'DR' : 'Holiday');
	
	var action = "";
	
	if (rowObject.drstatus == "Completed" || rowObject.drstatus == "Cancelled") {
		action = "<button disabled class=\"disabled\ onclick=\"\">"
		    + "<spring:message code='action.edit'/>" + "</button>";
		action += "&nbsp;<button disabled class=\"disabled\ onclick=\"\">"
			+ "<spring:message code='action.cancel' />" + "</button>";
	}
	else {
		action = "<button enabled onclick=\"editDR("+drId+")\">"
			+ "<spring:message code='action.edit'/>" + "</button>";
    	action += "&nbsp;<button enabled onclick=\"cancelDR("+drId+",'"+eventType+"')\">"
			+ "<spring:message code='action.cancel' />" + "</button>";
    }
	
	action += "&nbsp;<button onclick=\"deleteDR("+drId+",'"+eventType+"')\">"
	+ "<spring:message code='action.delete' />" + "</button>";	
	
	return action;
}

function editDR(id){
	 $("#drDetailsDialog").load("${drPrompt}"+id).dialog({
         title : "Edit Override",
         width :  Math.floor($('body').width() * .35),
         minHeight : 300,
         modal : true
     });
     return false;
}

function cancelDR(id,drType) {	
	
	var proceed = confirm("Are you sure you want to cancel selected "+drType+" override?");
	if (proceed)
	{
		$("#drmessage").css("color", "green");
		$("#drmessage").html("Cancelling Override Schedule...");
		
	 	$.ajax({
			   type: "POST",
			   url: '<spring:url value="/services/org/dr/cancel/"/>',
			   contentType: "application/json",
			   data: '{"id":"' + id + '"}',
			   dataType: "json",
			   success: function(msg){
				   if(msg.status == "0") {
					   if(msg.msg == "S") {					 
						   alert("Override Schedule is cancelled successfully.");					   
						   window.location.reload();  
					   }else if(msg.msg == "E")
						{
						  //DR Already finshed/cancelled					  
	    				  alert("Override Schedule already finished/cancelled.");
						  window.location.reload();
						 
						}
					   else {
						   $("#drmessage").css("color", "red");
						   $("#drmessage").html("Some unexpected error while canceling the Override Schedule.");
					   }				   
				   }   
			   },
			   error: function() {			   
				   $("#drmessage").css("color", "red");
				   $("#drmessage").html("Some unexpected error while canceling the Override Schedule.");
			   }		   
		});
	}
}

function deleteDR(id, drType){	
	var proceed = confirm("Are you sure you want to delete selected "+drType+" override?");
	if (proceed)
	{
		$("#drmessage").css("color", "green");
		$("#drmessage").html("Deleting Override Schedule...");
		$.ajax({
	 		type: 'POST',
	 		url: "${deleteDr}"+id+"?ts="+new Date().getTime(),
	 		dataType : "json",
	 		success: function(data){	 			
	 			if(data.status == 0) {
	 				$("#drmessage").html("Override Schedule deleted successfully.");
	 				window.location.reload();				
				}
			},
			error: function(){
				$("#drmessage").css("color", "red");
				$("#drmessage").html("Some unexpected error while deleting the Override Schedule.");
			},
	 		contentType: "application/xml; charset=utf-8"
		});			
     }
}

function beforeCancelDeleteMultiDRs(obj){	
	var selIds = jQuery("#weekdayTable").getGridParam('selarrrow');
	var drNum = selIds.length;
	if(drNum == 0 ){
		alert("Please select Override Schedule/s to "+obj.id.split('s')[0]);
		return false;
	}
	
	if(obj.id.indexOf("delete")>=0)
		deleteDRs();
	else if(obj.id.indexOf("cancel")>=0)
		cancelDRs();
	/* else if(obj.id.contains("cancel"))
		cancelDRs(); */	
}

function deleteDRs(){
	var selIds = jQuery("#weekdayTable").getGridParam('selarrrow');
	var drNum = selIds.length;
	var proceed = confirm("Are you sure you want to delete "+drNum+ " selected Override Schedule/s?");
	if(proceed){
		$("#drmessage").css("color", "green");
		$("#drmessage").html("Deleting Override Schedule/s...");
		var drXML= "<dRTargets>";
		//get selected dr id's to delete
		for(var i=0; i<drNum; i++){
			var drRow = $("#weekdayTable").jqGrid('getRowData', selIds[i]);
			drXML += "<drTarget><id>"+drRow.id+"</id></drTarget>";					
		}		
		drXML += "</dRTargets>";
			
		$.ajax({
			type: 'POST',
			url: "${deleteMultiDr}?ts="+new Date().getTime(),
			data: drXML,
			dataType:"xml",
    		contentType: "application/xml; charset=utf-8",
			success: function(data){
				if(data != null){				
					var xml=data.getElementsByTagName("response");					
					for (var j=0; j<xml.length; j++) {
						var status = xml[j].getElementsByTagName("status")[0].childNodes[0].nodeValue;
						var currDrId = xml[j].getElementsByTagName("msg")[0].childNodes[0].nodeValue;
						
						if(status==1){ // success: remove from list
							jQuery("#weekdayTable").jqGrid('delRowData', currDrId);
						} 
					}
					$("#drmessage").html("Override Schedule/s deleted successfully.");					
					$("#weekdayTable").trigger("reloadGrid");
				}
			},			
		});		
	}
}

function cancelDRs(){
	var selIds = jQuery("#weekdayTable").getGridParam('selarrrow');
	var drNum = selIds.length;
	
	var precancelled = 0;
	var successCount = 0;
	var errorCount = 0;
	var unexErrorCount = 0;
	var proceed = true;
	
	for(var i=0; i<drNum; i++){
		var drRow = $("#weekdayTable").jqGrid('getRowData', selIds[i]);
		if(drRow.drstatus=="Completed" || drRow.drstatus=="Cancelled")
			precancelled+=1;							
	}
	if(precancelled>0){
		var verify = confirm("1 or more selected Override Schedule/s have been completed/cancelled before. Do you want to continue cancelling remaining Override Schedule/s?");
		if(!verify){
			proceed = false;
			return;
		}
	}
	
	if(proceed){
		$("#drmessage").css("color", "green");
		$("#drmessage").html("Cancelling Override Schedule/s...");
			
		var drXML= "<dRTargets>";
		//get selected dr id's to delete
		for(var i=0; i<drNum; i++){
			var drRow = $("#weekdayTable").jqGrid('getRowData', selIds[i]);				
			if(drRow.drstatus!="Completed" && drRow.drstatus!="Cancelled")
			{
				drXML += "<drTarget><id>"+drRow.id+"</id></drTarget>";			
			}
		}		
		drXML += "</dRTargets>";
				
		$.ajax({
			type: 'POST',
			url: "${cancelMultiDr}?ts="+new Date().getTime(),
			data: drXML,
			dataType:"xml",
	    	contentType: "application/xml; charset=utf-8",
			success: function(data){
				if(data != null){				
					var xml=data.getElementsByTagName("response");					
					for (var j=0; j<xml.length; j++) {
						var status = xml[j].getElementsByTagName("status")[0].childNodes[0].nodeValue;
						var currDrId = xml[j].getElementsByTagName("msg")[0].childNodes[0].nodeValue;	
						if(status == "1") {
							if(currDrId == "E") //DR Already finshed/cancelled	
								errorCount+=1;
							else if(currDrId == "U")
								unexErrorCount+=1;
							else
								successCount+=1;
					    }
					}
					
					if(successCount == xml.length)
						$("#drmessage").html("Override Schedule/s cancelled successfully.");
					else if(errorCount>0 && unexErrorCount==0 && successCount==0)
						$("#drmessage").html("Override Schedule/s already finished/cancelled.");
					else if(errorCount==0 && unexErrorCount>0 && successCount==0)
					{
					   $("#drmessage").css("color", "red");
					   $("#drmessage").html("Some unexpected error while canceling the Override Schedule/s.");						
					}
					else
					   	$("#drmessage").html("Few of the selected Override Schedule/s cancelled successfully.");
						
					$("#weekdayTable").trigger("reloadGrid");
				}
			},
			error: function() {			   
				   $("#drmessage").css("color", "red");
				   $("#drmessage").html("Some unexpected error while canceling the Override Schedule/s.");
		    }
		});
	}	
}
$(function() {
	$(window).resize(function() {
		var setSize = $(window).height();
		setSize = setSize - 118;
		$(".topmostContainer").css("height", setSize);
	});
});
$(".topmostContainer").css("overflow", "auto");
$(".topmostContainer").css("height", $(window).height() - 118);
</script>
<style>
.disabled {
		background: #CCCCCC !important;
		border-color :#CCCCCC !important;
		color:#FFFFFF; 
	}
</style>