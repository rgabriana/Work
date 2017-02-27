<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<spring:url value="/services/org/fixture/fixtureCalibrationMap/updateEnabledFlag" var="updateEnabledFlagUrl" scope="request" />
<spring:url value="/services/org/fixture/forgetAllFixtureCurve" var="forgetFixtureCurveUrl" scope="request" />
<script type="text/javascript">
var fixtureId;
var urlForFixtureCurve;
var COLOR_FAILURE = "red";
var COLOR_SUCCESS = "green";
var COLOR_DEFAULT = "black";
var mode = "${type}";
clearLabelMessage("");
var fixtureLampCalibrationId =0;
$(document).ready(function() {	
	
	fixtureId = "${fixtureId}";
	fixtureLampCalibrationId = "${fixtureLampCalibrationId}";
	start(1, "status", "desc");

	var grid = jQuery("#fixturepowermapTable");
	$("#updateFxCurve").click(function() {
	  var r=confirm("This will modify the existing power usage characterization data. Are you sure you want to proceed?");
	  if(r==true)
	  {
		   var data = grid.getRowData();
		   var fxXML = "<fixtureVoltPowerList>";
		   
		   if(SELECTED_FIXTURES != undefined && SELECTED_FIXTURES.length > 0) {
				$.each(SELECTED_FIXTURES, function(i, fixtureJson) {
					 fxXML+="<fixtures><id>"+fixtureJson.id+"</id></fixtures>";
				});
			}
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
	 	       fxXML+="<fixtureCalibrationMaps><id>"+data[i].id+"</id><volt>"+ volt + "</volt><enabled>"+ enable +"</enabled></fixtureCalibrationMaps>";
	 	   }
		   fxXML +="</fixtureVoltPowerList>";
			$.ajax({
				type: 'POST',
				url: "${updateEnabledFlagUrl}?ts="+new Date().getTime(),
				data: fxXML,
				success: function(data){
					if(data != null){
						var xml=data.getElementsByTagName("response");
						for (var j=0; j<xml.length; j++) {
							var status = xml[j].getElementsByTagName("status")[0].childNodes[0].nodeValue;
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
	
	
	$("#forgetFixtureCurve").click(function() {
		if(confirm("Are you sure you want to delete the Fixture's power usage characterization data?") == true)
		{
		 var fxXML = "<fixtures>";
		   
	     if(SELECTED_FIXTURES != undefined && SELECTED_FIXTURES.length > 0) {
			$.each(SELECTED_FIXTURES, function(i, fixtureJson) {
				 fxXML+="<fixture><id>"+fixtureJson.id+"</id></fixture>";
			});
		 }
	     fxXML += "</fixtures>";
		  $.ajax({
				type: 'POST',
				url: "${forgetFixtureCurveUrl}",
				data: fxXML,
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
	urlForFixtureCurve = '<spring:url value="/services/org/fixture/list/voltpowermap/"/>'+ fixtureId+"/"+mode;	
	jQuery("#fixturepowermapTable").jqGrid({
		url: urlForFixtureCurve,			
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
		colNames: ["Id","Light Level","Power","Enable"],
	       colModel: [
	       { name:'id', index:'id',hidden:true},
	       { name: 'volt',index:'volt', align:"center", sortable:false,formatter: lightLevelRenderer },
	       { name: "power", index: 'power', hidden:(mode!="single"), search:false, sortable:false, formatter:'number' },
	       {name: 'enabled', index: 'enabled', align: 'center', formatter: 'checkbox',formatoptions:{disabled :false},editable: true,sortable:false, edittype: 'checkbox'}],
       jsonReader: { 
			root:"fixtureCalibrationMap", 
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
	   			if (data.fixtureCalibrationMap != undefined) {
			   		if (data.fixtureCalibrationMap.length == undefined) {
		   				jQuery("#fixturepowermapTable").jqGrid('addRowData', 0, data.fixtureCalibrationMap);
			   		}
			   	}
	   		}	   		
	   	ModifyBallastVoltPowerMapGridDefaultStyles();
	   	}
	});
	
	}
function lightLevelRenderer(cellvalue, options, rowObject)
{
	var lightLevel = rowObject.volt;
	return (lightLevel * 10);
}
function ModifyBallastVoltPowerMapGridDefaultStyles()
{
	var jgrid = jQuery("#fixturepowermapTable");	
	jgrid.jqGrid("setGridHeight", 250);	
}
function generateFixtureCurveCSV()
{
	$("#fixtureidfixturecsv").val(fixtureId);
	$('#exportFixtureCurveForm').submit();
}
function displayLabelMessage(Message, Color) {
	$("#fvpMessage").html(Message);
	$("#fvpMessage").css("color", Color);
}

function clearLabelMessage(Message, Color) {
	displayLabelMessage("", COLOR_DEFAULT);
}
</script>
<div style="padding-left:10px;padding-right:10px;">
<table style="border: 1px;width: 100%">
<tr>
 <td colspan="4">
	 <div id="fvpMessage" style="font-size: 14px; font-weight: bold; padding: 5px 0 0 0px; float: right;width: 100%;"></div>
</td>
</tr>

<tr>
<td colspan="3">
<table style="border: 1px; border-style:solid;padding: 2px;">
<tr><td>Source : <span id="fvpFixtureName">${fixtureSourceName}</span></td></tr>
<tr><td>Ballast :<span id="fvpballastType">${ballastType}</span></td></tr>
<tr><td>Lamps : <span id="fvplamps">${lamps}</span></td></tr>
<tr><td>Lamp Manufacturer : <span id="bvplamps">${lampManufacturer}</span></td></tr>
<tr><td>Line Voltage : <span id="fvplinevoltage">${lineVoltage}</span>vac</td></tr>
</table>
</td>
<td valign="bottom" align="right">
<input type="button" id="forgetFixtureCurve" value="Forget"/>
</td>
</tr>

<tr>
<td colspan="4" align="right">
	<c:if test="${type == 'single'}">
	<input type="button" id="fixtureCurveCsvBtn" onclick="generateFixtureCurveCSV();" value="Export CSV"/>
	</c:if>
	<input type="button" id="updateFxCurve" value="Update"/>
	
</td>
</tr>
</table>
</div>
<div style="padding-left:10px;padding-right:10px;">
<table id="fixturepowermapTable"></table>
</div>
<form id='exportFixtureCurveForm' action=<spring:url value='/services/org/fixture/list/getexportdata'/> method='POST'>
<input type='hidden' id='fixtureidfixturecsv' name='fixtureidfixturecsv'></input>
</form>