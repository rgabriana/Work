<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/ballastvoltpowerservice/updateEnabledFlag" var="updateEnabledFlag" scope="request" />
<spring:url value="/services/org/ballastvoltpowerservice/forgetBallastCurve" var="forgetBallastCurveUrl" scope="request" />
<script type="text/javascript">
var ballastIdForBallastCurve,inputVoltageForBallastCurve,urlForBallastCurve;
var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
clearLabelMessage("");
$(document).ready(function() {	
	ballastIdForBallastCurve = "${ballastId}";
	inputVoltageForBallastCurve = "${firstVoltageLevel}";
	
	$("#bvplinevoltage").text(inputVoltageForBallastCurve + "vac");
	
	start(1, "status", "desc");
	$("#ballastVoltageLevels").empty();
	ballastIdForBallastCurve = "${ballastId}";
	<c:forEach items="${ballastvoltagelevels}" var="level">
		$('#ballastVoltageLevels').append($('<option></option>').val("${level}").html("${level}"));
	</c:forEach>
	
	var grid = jQuery("#powermapTable");
	$("#updateballast").click(function() {
		
	   var r=confirm("This will modify the existing power usage characterization data. Are you sure you want to proceed?");
	   if(r==true)
	   {
	   var data = grid.getRowData();
	   var fxXML= "<ballastVoltPowers>";
	   for(var i=0;i<data.length;i++){
		    var enable = false;
 	        if(data[i].enabled==='Yes')
 	        {
 	        	enable = true;
 	        }else
        	{
 	        	enable = false;
        	}
 	        var volt = data[i].volt/10;
 	       fxXML+="<ballastVoltPower><voltPowerMapId>"+data[i].voltPowerMapId+"</voltPowerMapId><volt>"+ volt + "</volt><enabled>"+ enable +"</enabled></ballastVoltPower>";
 	   }
	   fxXML += "</ballastVoltPowers>";
		$.ajax({
			type: 'POST',
			url: "${updateEnabledFlag}/"+ballastIdForBallastCurve+"?ts="+new Date().getTime(),
			data: fxXML,
			success: function(data){
				if(data != null){
					var xml=data.getElementsByTagName("response");
					for (var j=0; j<xml.length; j++) {
						var status = xml[j].getElementsByTagName("status")[0].childNodes[0].nodeValue;
						var currFixtureId = xml[j].getElementsByTagName("msg")[0].childNodes[0].nodeValue;
						if(status==0){// success
							displayLabelMessage("Power usage characterization data updated.", COLOR_SUCCESS);
						}
					}
				}
			},
			dataType:"xml",
			contentType: "application/xml; charset=utf-8",
		});
	   }
	});
	
	
	$("#forgetBallastCurve").click(function() {
		
		if(confirm("Are you sure you want to delete the Ballast's power usage characterization data?") == true)
		{
		  $.ajax({
				type: 'POST',
				url: "${forgetBallastCurveUrl}/"+ballastIdForBallastCurve+"/inputVoltage/"+inputVoltageForBallastCurve,
				success: function(data){
					if(data != null){
						var xml=data.getElementsByTagName("response");
						for (var j=0; j<xml.length; j++) {
							var status = xml[j].getElementsByTagName("status")[0].childNodes[0].nodeValue;
							var currFixtureId = xml[j].getElementsByTagName("msg")[0].childNodes[0].nodeValue;
							if(status==0){// success
								displayLabelMessage("Power usage characterization data removed successfully.", COLOR_SUCCESS);
							}
						}
					}
				},
				dataType:"xml",
				contentType: "application/xml; charset=utf-8",
			});
		}
		});
});

function start(pageNum, orderBy, orderWay) {	
	urlForBallastCurve = '<spring:url value="/services/org/ballastservice/list/voltpowermap/"/>'+ ballastIdForBallastCurve + '/' + inputVoltageForBallastCurve;	
	jQuery("#powermapTable").jqGrid({
		url: urlForBallastCurve,			
		mtype: "POST",			
		datatype: "json",
		autoencode: true,
		hoverrows: false,
		autowidth: true,
		scrollOffset: 0,
		forceFit: true,
		rowNum: 100,
		formatter: {
			 integer: {thousandsSeparator: ",", defaultValue: '0'},
		     number: {decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, defaultValue: '0.00'}
		},
		colNames: ["Id","voltPowerMapId", "Light level","Power","Include"],
	       colModel: [
	       { name:'id', index:'id',hidden:true},
	       { name:'voltPowerMapId', index:'voltPowerMapId',hidden:true},
	       { name: 'volt',index:'volt', align:"center", sortable:false,formatter: ballastlightLevelRenderer  },
	       { name: "power", index: 'power',search:false, sortable:false,formatter:'number' },
	       {name: 'enabled', index: 'enabled', align: 'center', formatter: 'checkbox',formatoptions:{disabled :false},editable: true,sortable:false, edittype: 'checkbox'}],
	       
	   	jsonReader: { 
			root:"ballastvoltpower", 
	        page:"page", 
	        total:"total", 
	        records:"records", 
	        repeatitems:false,
	        id : "id"	        
	   	},
	   	cmTemplate: { title: false },   	
	   	sortorder: orderWay,
	   	sortname: "name",
	    hidegrid: false,
	    viewrecords: true,	   	
	   	toolbar: [false,"top"],
	   	onSortCol: function(index, iCol, sortOrder) {
	   	},
	   	loadComplete: function(data) {	   		
	   		if (data != null){
	   			if (data.ballastvoltpower != undefined) {
			   		if (data.ballastvoltpower.length == undefined) {
		   			jQuery("#powermapTable").jqGrid('addRowData', 0, data.ballastvoltpower);
			   		}
			   	}
	   		}	   		
	   	ModifyBallastVoltPowerMapGridDefaultStyles();
	   	}
	});
	
	}

function ModifyBallastVoltPowerMapGridDefaultStyles()
{
	var jgrid = jQuery("#powermapTable");	
	jgrid.jqGrid("setGridHeight", 250);	
}

function setVoltageLevelsForPowerMap(sel) {
    var value = sel.options[sel.selectedIndex].value;  
    inputVoltageForBallastCurve = value;
    urlForBallastCurve = '<spring:url value="/services/org/ballastservice/list/voltpowermap/"/>'+ ballastIdForBallastCurve + '/' + inputVoltageForBallastCurve;    
    jQuery("#powermapTable").jqGrid('setGridParam',{url:urlForBallastCurve,page:1}).trigger("reloadGrid");
    $("#bvplinevoltage").text(inputVoltageForBallastCurve + "vac");
}

function generateBallastCSV()
{
	$("#ballastidballastcsv").val(ballastIdForBallastCurve);
	$("#voltagelevelballastcsv").val(inputVoltageForBallastCurve);
	$('#exportBallastForm').submit();
}
function displayLabelMessage(Message, Color) {
	$("#bvpMessage").html(Message);
	$("#bvpMessage").css("color", Color);
}
function ballastlightLevelRenderer(cellvalue, options, rowObject)
{
	var lightLevel = rowObject.volt;
	return (lightLevel * 10);
}
function clearLabelMessage(Message, Color) {
	displayLabelMessage("", COLOR_DEFAULT);
}
</script>
<div style="padding-left:10px;padding-right:10px;">
<table>
<tr>
 <td colspan="4">
	 <div id="bvpMessage" style="font-size: 14px; font-weight: bold; padding: 5px 0 0 0px; float: left;width: 100%"></div>
	 </td>
</tr>
<tr>
<td colspan="3">
<table style="border: 1px; border-style:solid;padding: 2px;">
<tr><td>Source: Ballast</td></tr>
<tr><td>Ballast: <span id="bvpballastType">${ballastType}</span></td></tr>
<tr><td>Lamps: <span id="bvplamps">${lamps}</span></td></tr>
<tr><td>Lamp Manufacturer: <span id="bvplamps">${lampManufacturer}</span></td></tr>
<tr><td>Line Voltage: <span id="bvplinevoltage"></span></td></tr>
</table>
</td>
<td valign="bottom" align="right">
<input type="button" id="forgetBallastCurve" value="Forget"/>
</td>
</tr>

<tr>
<td><b>Select Voltage Level: </b></td>
<td><select id="ballastVoltageLevels" onchange="setVoltageLevelsForPowerMap(this)"></select></td>
<td style="padding-left:90px;">
<input type="button" id="ballastcsvBtn" onclick="generateBallastCSV();"
	value="Export CSV"/>
</td>
<td>
	<input type="button" id="updateballast" value="Update"/>
</td>
</tr>
</table>
</div>
<div style="padding-left:10px;padding-right:10px;">
<table id="powermapTable"></table>
</div>
<form id='exportBallastForm' action=<spring:url value='/services/org/ballastvoltpowerservice/list/getexportdata'/> method='POST'>
<input type='hidden' id='voltagelevelballastcsv' name='voltagelevelballastcsv'></input>
<input type='hidden' id='ballastidballastcsv' name='ballastidballastcsv'></input>
</form>
