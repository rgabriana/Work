<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<spring:url value="/scripts/jquery/jquery.validationEngine.js" var="jquery_validationEngine"></spring:url>
<script type="text/javascript" src="${jquery_validationEngine}"></script>
<spring:url value="/scripts/jquery/jquery.validationEngine-en.js" var="jquery_validationEngine_en"></spring:url>
<script type="text/javascript" src="${jquery_validationEngine_en}"></script>
<spring:url value="/services/org/dr/updateflag/id/" var="updateFlags" scope="request" />

<spring:url value="/services/org/dr/start/" var="startDRUrl" scope="request" />
<fmt:setLocale value="en_US"/>
<div class="topmostContainer">
<div class="outermostdiv">
<div class="outerContainer">
	<span ><spring:message code="dr.header"/></span>
	<div style="padding: 0px 0px 0px 100px; display: inline;"><span id="drmessage"></span></div>
	<div class="i1"></div>
	<div class="innerdiv">
		<table id="weekdayTable" class="entable" style="width: 100%; height: 100%;" >
					<thead>
						<tr class="editableRow">
							<th width="20%" align="left"><spring:message code="dr.label.price.level"/></th>
							<th  align="left"><spring:message code="dr.label.pricing"/></th>
							<th style="display: none;" align="left"><spring:message code="dr.label.enabled"/></th>
							<th  align="left"><spring:message code="dr.label.duration"/></th>
							<th  align='left'><spring:message code="dr.label.run"/></th>
							
						</tr>
					</thead>
					<c:forEach items="${drlist}" var="dr">
						
						<tr id="${dr.id}row" class="editableRow">
							<td>
							
							<label style="font-weight: bold;" id="${dr.id}level" size="10" value="${dr.priceLevel}" type="text">${dr.priceLevel}</label>
							</td>
							<td>
								<form id="${dr.id}form1" onsubmit="return false;">
									<div class="innerContainerInputFieldValue">
										<input id="${dr.id}price" class="validate[required,custom[number],min[0]] text-input" size="10" value="${dr.pricing}" type="text"/>
									</div>
								</form>
							</td>
							
							<td  style="display: none;">
								<select id="${dr.id}enabled">
									<option id="${dr.id}Yes" value="Yes">Yes</option>
									<option id="${dr.id}No" value="No">No</option>
								</select>
							</td>
							<td>
								<form id="${dr.id}form3" onsubmit="return false;">
									<div class="innerContainerInputFieldValue">
										<input id="${dr.id}duration" class="validate[required,custom[integer],min[1]] text-input" size="10" 
											value=<fmt:parseNumber integerOnly="true" type="number" value="${dr.duration/60}"/> type="text"/>
									</div>
								</form>
							</td>
							<td>
								<input type="button" id="${dr.id}init" onclick="initDR(this);" value=<spring:message code="dr.action.initiate"/> />
								<input type="button" id="${dr.id}update" onclick="updateDR(this);" value=<spring:message code="action.update"/> />
							</td>							
						</tr>
						<script type="text/javascript">
								
								$("#"+"${dr.id}"+"${dr.priceLevel}").attr("selected", "selected");
								$("#"+"${dr.id}"+"${dr.enabled}").attr("selected", "selected");
								if("${dr.enabled}" == "Yes") {
									   $("#" + "${dr.id}" + "init").attr("value", '<spring:message code="action.cancel"/>');
									   $("#" + "${dr.id}" + "init").attr("onclick", "cancelDR(this);");
									   $("#" + "${dr.id}" + "update").attr("disabled", "disabled");
									   $("#" + "${dr.id}" + "level").attr("disabled", "disabled");
									   $("#" + "${dr.id}" + "price").attr("disabled", "disabled");
									   $("#" + "${dr.id}" + "enabled").attr("disabled", "disabled");
									   $("#" + "${dr.id}" + "duration").attr("disabled", "disabled");
								}else
								{
									  $("#" + "${dr.id}" + "init").attr("value", '<spring:message code="dr.action.initiate"/>');
									  $("#" + "${dr.id}" + "init").attr("onclick", "initDR(this);");
									  $("#" + "${dr.id}" + "init").removeAttr("disabled");
									  $("#" + "${dr.id}" + "level").removeAttr("disabled");
									  $("#" + "${dr.id}" + "price").removeAttr("disabled");
									  $("#" + "${dr.id}" + "enabled").removeAttr("disabled");
									  $("#" + "${dr.id}" + "duration").removeAttr("disabled");
									  $("#" + "${dr.id}" + "update").removeAttr("disabled");
								}
								$("#"+"${dr.id}" + "form1").validationEngine('attach' , {
									isOverflown: true,
									overflownDIV: ".topmostContainer"
								});
								
								$("#"+"${dr.id}" + "form2").validationEngine('attach' , {
									isOverflown: true,
									overflownDIV: ".topmostContainer"
								});
								$("#"+"${dr.id}" + "form3").validationEngine('attach' , {
									isOverflown: true,
									overflownDIV: ".topmostContainer"

								});
						</script>				
					</c:forEach>
		</table><br/><hr/><hr/><br/>
		<div id ="cbdiv" style="float: right; margin-right: 12px;"><input type="checkbox" name="drtabletype" value="mvalue" onclick='clicked(this);' /><b>Show Only Cancelled/Completed</b><br></div>
		<span><spring:message code="dr.openadr.header"/></span>
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

$(document).ready(function() {
		url = '<spring:url value="/services/org/dr/list/alternate/filter"/>';
		start(1, "status", "desc",url);
	});
	
function start(pageNum, orderBy, orderWay,url) {
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

function updateDR(obj) {
	$("#drmessage").html("");
	var id = $(obj).attr('id').split('u')[0];
	//&& $("#"+id+"form2").validationEngine('validate')
	if($("#"+id + "form1").validationEngine('validate')  && $("#"+id+"form3").validationEngine('validate')) {
 		$.ajax({
			   type: "POST",
			   url: '<spring:url value="/services/org/dr/update"/>',
			   contentType: "application/json",
			   data: '{"id":"' + id + '","priceLevel":"' + $("#"+id+"level").text()  + 
				   '","pricing":"' + $("#" + id +"price").val() + 
				   '","duration":"' + $("#" + id + "duration").val() +
				   '","enabled":"' + $("#" + id +"enabled").val() + 
				   '"}',
			   dataType: "json",
			   success: function(msg){
				   if(msg.status == "1") {
					   if($("#" + id +"enabled").val() == "Yes") {
						   if(msg.msg == "S") {
							   $("#drmessage").css("color", "green");
							   $("#drmessage").html("Demand Response is updated and instantiated successfully.");
							   $("#" + id + "init").attr("value", '<spring:message code="action.cancel"/>');
							   $("#" + id + "init").attr("onclick", "cancelDR(this);");
							   $("#" + id + "update").attr("disabled", "disabled");
							   $("#" + id + "level").attr("disabled", "disabled");
							   $("#" + id + "price").attr("disabled", "disabled");
							   $("#" + id + "enabled").attr("disabled", "disabled");
							   $("#" + id + "duration").attr("disabled", "disabled");
						   }
						   else if(msg.msg == "R") {
							   $("#drmessage").css("color", "red");
							   $("#drmessage").html("Some unexpected error while updating the Demand Response. Please refresh the page and try again.");
						   }
						   else if(msg.msg == "E"){
							   $("#drmessage").css("color", "red");
							   $("#drmessage").html("Demand Response is updated successfully but cannot be instantiated since another Demand Response is already under execution, cannot instantiate a new one!");
							   $("#" + id  + "No").attr("selected", "selected");
						   }
						   else {
							   $("#drmessage").css("color", "red");
							   $("#drmessage").html("Demand Response is updated successfully but cannot be instantiated due to some unexpected error.");
							   $("#" + id  + "No").attr("selected", "selected");
						   }
					   }
					   else if(msg.msg == "S") {
						   $("#drmessage").css("color", "green");
						   $("#drmessage").html("Demand Response is updated successfully.");
					   }

				   }   
			   },
			   error: function() {
				   $("#drmessage").css("color", "red");
				   $("#drmessage").html("Some unexpected error while updating the Demand Response.");
			   }
		});	
	}

}

function initDR(obj) {
	toggleInitiateButton(true);
	 $("#drmessage").css("color", "green");
	$("#drmessage").html("Starting Demand Response...");
	var id = $(obj).attr('id').split('i')[0];
	var urlOptions = "level/"+$("#"+id+"level").text()+"/duration/"+$("#" + id + "duration").val()+"/type/MANUAL/starttime/0/drindentifier/-1/status/Active/";
	if($("#"+id + "form1").validationEngine('validate') &&  $("#"+id+"form3").validationEngine('validate')) {
 		$.ajax({
			   type: "POST",
			   url: "${startDRUrl}"+urlOptions+"?ts="+new Date().getTime(),
			   contentType: "application/json",
			   data: '{"id":"' + id + '","priceLevel":"' + $("#"+id+"level").text()  + 
			   '","pricing":"' + $("#" + id +"price").val() + 
			   '","duration":"' + $("#" + id + "duration").val() +
			   '","enabled":"' + $("#" + id +"enabled").val() + 
			   '"}',
			   dataType: "json",
			   success: function(msg){
				   if(msg.status == "1") {
					   if(msg.msg == "S") {
						   //$("#drmessage").css("color", "green");
						   $("#" + id  + "Yes").attr("selected", "selected");
						   $("#" + id + "init").attr("value", '<spring:message code="action.cancel"/>');
						   $("#" + id + "init").attr("onclick", "cancelDR(this);");
						   $("#" + id + "update").attr("disabled", "disabled");
						   $("#" + id + "level").attr("disabled", "disabled");
						   $("#" + id + "price").attr("disabled", "disabled");
						   $("#" + id + "enabled").attr("disabled", "disabled");
						   $("#" + id + "duration").attr("disabled", "disabled");
						   //Once DR Started, explictly fire Sticker Component
						   //checkNetworkConnectivity();
						   alert("Demand Response is instantiated successfully.");
						   window.location.reload();
					   }
					   else if(msg.msg == "E"){
						  // $("#drmessage").css("color", "red");
						 alert("Another demand response is already under execution, cannot instantiate a new one!");
						 window.location.reload();
					   }
					   else {
						   $("#drmessage").css("color", "red");
						   $("#drmessage").html("Some unexpected error while instantiating the Demand Response.");
					   }
					   toggleInitiateButton(false);
				   }   
			   },
			   error: function() {
				   toggleInitiateButton(false);
				   $("#drmessage").css("color", "red");
				   $("#drmessage").html("Some unexpected error while instantiating the Demand Response.");
			   }		   
		});
	}

}

function cancelDR(obj) {
	toggleInitiateButton(true);
	$("#drmessage").css("color", "green");
	$("#drmessage").html("Cancelling Demand Response...");
	
	var id = $(obj).attr('id').split('i')[0];
 	$.ajax({
		   type: "POST",
		   url: '<spring:url value="/services/org/dr/cancel/"/>',
		   contentType: "application/json",
		   data: '{"id":"' + id + '"}',
		   dataType: "json",
		   success: function(msg){
			   if(msg.status == "1") {
				   if(msg.msg == "S") {
					   //$("#drmessage").css("color", "green");
					   $("#" + id  + "No").attr("selected", "selected");
					   $("#" + id + "init").attr("value", '<spring:message code="dr.action.initiate"/>');
					   $("#" + id + "init").attr("onclick", "initDR(this);");
					   $("#" + id + "update").removeAttr("disabled");
					   $("#" + id + "level").removeAttr("disabled");
					   $("#" + id + "price").removeAttr("disabled");
					   $("#" + id + "enabled").removeAttr("disabled");
					   $("#" + id + "duration").removeAttr("disabled");
					   //Once DR Cancalled Event Explictly call removeStickerMenu
					   //removeStickerMenu();
					   alert("Demand Response is cancelled successfully.");
					   window.location.reload();
				   }else if(msg.msg == "E")
					{
					   //DR Already finshed/cancelled
					  //$("#drmessage").css("color", "red");
					  $("#" + id + "init").attr("value", '<spring:message code="dr.action.initiate"/>');
					  $("#" + id + "init").attr("onclick", "initDR(this);");
    				  alert("Demand Response already finished/cancelled.");
					  window.location.reload();
					 
					}
				   else {
					   $("#drmessage").css("color", "red");
					   $("#drmessage").html("Some unexpected error while canceling the Demand Response.");
				   }
				   toggleInitiateButton(false);
			   }   
		   },
		   error: function() {
			   toggleInitiateButton(false);
			   $("#drmessage").css("color", "red");
			   $("#drmessage").html("Some unexpected error while canceling the Demand Response.");
		   }		   
	});
}

function toggleInitiateButton(flag)
{
	if(flag)
	{
		<c:forEach items="${drlist}" var="dr">
			var id = "${dr.id}";
			$("#" + id + "init").attr("disabled", "disabled");
		 </c:forEach>
	}else
	{
		<c:forEach items="${drlist}" var="dr">
			var id = "${dr.id}";
			$("#" + id + "init").removeAttr("disabled");
		</c:forEach>
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