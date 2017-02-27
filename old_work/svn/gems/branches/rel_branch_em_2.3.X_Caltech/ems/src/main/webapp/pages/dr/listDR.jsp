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
							<!--<th  align="left"><spring:message code="dr.label.target.reduction"/></th> -->
							 <th style="display: none;" align="left"><spring:message code="dr.label.enabled"/></th>
							<th  align="left"><spring:message code="dr.label.duration"/></th>
							<th  align='left'><spring:message code="dr.label.run"/></th>
							
						</tr>
					</thead>
					<c:forEach items="${drlist}" var="dr">
						
						<tr id="${dr.id}row" class="editableRow">
							<td>
							<!-- 
								<select id="${dr.id}level">
									<option id="${dr.id}Low" value="Low">Low</option>
									<option id="${dr.id}Moderate" value="Moderate">Moderate</option>
									<option id="${dr.id}High" value="High">High</option>
									<option id="${dr.id}Special" value="Special">Special</option>
 									<option id="${dr.id}High" value="High">High</option>
 									<option id="${dr.id}Critical" value="Critical">Critical</option>
								</select>
							-->
							<label style="font-weight: bold;" id="${dr.id}level" size="10" value="${dr.priceLevel}" type="text">${dr.priceLevel}</label>
							</td>
							<td>
								<form id="${dr.id}form1" onsubmit="return false;">
									<div class="innerContainerInputFieldValue">
										<input id="${dr.id}price" class="validate[required,custom[number],min[0]] text-input" size="10" value="${dr.pricing}" type="text"/>
									</div>
								</form>
							</td>
							<!-- 
							<td>
								<form id="${dr.id}form2" onsubmit="return false;">
									<div class="innerContainerInputFieldValue">
										<input id="${dr.id}reduction" class="validate[required,custom[integer],min[0],max[100]] text-input" size="3" value="${dr.targetReduction}" type="text"/>
									</div>
								</form>
							</td>
							 -->
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
									   $("#" + "${dr.id}" + "reduction").attr("disabled", "disabled");
									   $("#" + "${dr.id}" + "enabled").attr("disabled", "disabled");
									   $("#" + "${dr.id}" + "duration").attr("disabled", "disabled");
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
		<div id="dr2">
					<table id="drTableCanCom"></table>
					<div id="drPagingDivCanCom"></div>
		</div>
	</div>
</div>
</div>
</div>
<script type="text/javascript">

function clicked(cb)
{
	if(cb.checked==true)
	{	
	document.getElementById("dr1").style.display="none";
	document.getElementById("dr2").style.display="";		
	}
	else
	{	
	document.getElementById("dr2").style.display="none";	
	document.getElementById("dr1").style.display="";	
	}
}

$(document).ready(function() {		
		start('1#######END', 1, "status", "desc");
		fillCanCom('1#######END',1,"status","desc");
		document.getElementById("dr2").style.display="none";
	});
	function fillCanCom(inputdata, pageNum, orderBy, orderWay) {
		jQuery("#drTableCanCom").jqGrid({
			url: '<spring:url value="/services/org/dr/list/alternate/cancom"/>',
			userData: "userdata",			
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
				{name:'starttime', index:'starttime', align:"center", sortable:true, width:"8%", search:false,formatter: datetimeRenderer},
				{name:'priority', index:'priority', align:"center", sortable:true, width:"8%", search:false,formatter: zeropRenderer},
				{name:'dridentifier', index:'dridentifier', align:"center", sortable:true, width:"8%", search:false},
				{name:'drstatus', index:'drstatus', align:"center", sortable:true, width:"8%", search:false},
				{name:'jitter', index:'jitter', align:"center", sortable:true, width:"8%", search:false, formatter: convertToSeconds},
				{name:'optin', index:'optin', align:"center", sortable:true, width:"8%", search:false,formatter: optinButtonRendererCanCom}	   		
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
 		   	pager: '#drPagingDivCanCom',
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
		   		if (data.drtarget != undefined) {
			   		if (data.drtarget.length == undefined) {
			   			// Hack: Currently, JSON serialization via jersey treats single item differently			   			
			   			jQuery("#drTableCanCom").jqGrid('addRowData', 0, data.drtarget);
			   		}
			   	}		   		
		   		ModifyGridDefaultStylesCanCom();
				forceFitdrTableHeightCanCom();		   		
		   	}
		});
	}
	
	jQuery("#drTableCanCom").jqGrid('navGrid',"#drPagingDivCanCom",{edit:false,add:false,del:false});
		
	function start(inputdata, pageNum, orderBy, orderWay) {
		jQuery("#drTable").jqGrid({
			url: '<spring:url value="/services/org/dr/list/alternate/filter"/>',
			userData: "userdata",			
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
		   	loadui: "block",
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
	
function ModifyGridDefaultStylesCanCom() {  
		   $('#' + "drTableCanCom" + ' tr').removeClass("ui-widget-content");
		   $('#' + "drTableCanCom" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "drTableCanCom" + ' tr:nth-child(odd)').addClass("oddTableRow");		   
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
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .29)); 
	}

function forceFitdrTableHeightCanCom(){
		var jgrid = jQuery("#drTableCanCom");
		var containerHeight = $(this).height();		
		var weekdayTableHeight = $("#weekdayTable").height();
		var innderDivHeight = $("#innerdiv").height();		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		var otherElementHeight = innderDivHeight - weekdayTableHeight;  
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .22)); 
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
		var source = "";
		var mDate = new Date(rowObject.starttime);		
		var dd = mDate.getDate(); 
		var mm = mDate.getMonth()+1; 
		var yyyy = mDate.getFullYear();
		var hh = mDate.getHours();
		var mins = mDate.getMinutes();
		var ss = mDate.getSeconds();
		
		if(dd<10)
		{
		dd='0'+dd;
		} 
		if(mm<10)
		{
		mm='0'+mm;
		} 
						
		var completeDate = yyyy+'/'+mm+'/'+dd+' '+hh+':'+mins+':'+ss;	
		
		source = "<label>" + completeDate + "</label>";
		
		return source;
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
				source = "<label><spring:message code='dr.label.optedout' /></label>";
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
				   //'","targetReduction":"' + $("#" + id +"reduction").val() + 
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
							   $("#" + id + "reduction").attr("disabled", "disabled");
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
	$("#drmessage").html("");
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
			   //'","targetReduction":"' + $("#" + id +"reduction").val() + 
			   '","enabled":"' + $("#" + id +"enabled").val() + 
			   '"}',
			   dataType: "json",
			   success: function(msg){
				   if(msg.status == "1") {
					   if(msg.msg == "S") {
						   resetCancelBtnLable();
						   $("#drmessage").css("color", "green");
						   $("#drmessage").html("Demand Response is instantiated successfully.");
						   $("#" + id  + "Yes").attr("selected", "selected");
						   $("#" + id + "init").attr("value", '<spring:message code="action.cancel"/>');
						   $("#" + id + "init").attr("onclick", "cancelDR(this);");
						   $("#" + id + "update").attr("disabled", "disabled");
						   $("#" + id + "level").attr("disabled", "disabled");
						   $("#" + id + "price").attr("disabled", "disabled");
						   $("#" + id + "reduction").attr("disabled", "disabled");
						   $("#" + id + "enabled").attr("disabled", "disabled");
						   $("#" + id + "duration").attr("disabled", "disabled");
						   //Once DR Started, explictly fire Sticker Component
						   getScheduleEventList();
					   }
					   else if(msg.msg == "E"){
						   $("#drmessage").css("color", "red");
						   $("#drmessage").html("Another demand response is already under execution, cannot instantiate a new one!");
					   }
					   else {
						   $("#drmessage").css("color", "red");
						   $("#drmessage").html("Some unexpected error while instantiating the Demand Response.");
					   }
					   
				   }   
			   },
			   error: function() {
				   $("#drmessage").css("color", "red");
				   $("#drmessage").html("Some unexpected error while instantiating the Demand Response.");
			   }		   
		});
	}

}

function cancelDR(obj) {
	$("#drmessage").html("");
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
					   $("#drmessage").css("color", "green");
					   $("#drmessage").html("Demand Response is cancelled successfully.");
					   $("#" + id  + "No").attr("selected", "selected");
					   $("#" + id + "init").attr("value", '<spring:message code="dr.action.initiate"/>');
					   $("#" + id + "init").attr("onclick", "initDR(this);");
					   $("#" + id + "update").removeAttr("disabled");
					   $("#" + id + "level").removeAttr("disabled");
					   $("#" + id + "price").removeAttr("disabled");
					   $("#" + id + "reduction").removeAttr("disabled");
					   $("#" + id + "enabled").removeAttr("disabled");
					   $("#" + id + "duration").removeAttr("disabled");
					   //Once DR Cancalled Event Explictly call removeStickerMenu
					   removeStickerMenu();
				   }else if(msg.msg == "E")
					{
					   //DR Already finshed/cancelled
					  $("#drmessage").css("color", "red");
					  $("#drmessage").html("Demand Response already finished/cancelled.");
					  $("#" + id + "init").attr("value", '<spring:message code="dr.action.initiate"/>');
					 
					}
				   else {
					   $("#drmessage").css("color", "red");
					   $("#drmessage").html("Some unexpected error while canceling the Demand Response.");
				   }
				   
			   }   
		   },
		   error: function() {
			   $("#drmessage").css("color", "red");
			   $("#drmessage").html("Some unexpected error while canceling the Demand Response.");
		   }		   
	});
}
function resetCancelBtnLable()
{
	<c:forEach items="${drlist}" var="dr">
	var id = "${dr.id}";
	 $("#" + id + "init").attr("value", '<spring:message code="dr.action.initiate"/>');
 </c:forEach>
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