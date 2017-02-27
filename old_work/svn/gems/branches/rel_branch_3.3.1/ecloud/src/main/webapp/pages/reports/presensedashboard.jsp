<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="uem" uri="/WEB-INF/tlds/ecloud.tld"%>

<spring:url value="/reports/occupancyreport.ems" var="loadOccReport"/>
<spring:url value="/reports/occupancymaps.ems" var="loadOccMap"/>
<spring:url value="/reports/occupancyreportnonfloorlevel.ems" var="loadOccReportNonFloorLevel"/>
<spring:url value="/services/org/facility/nodepath/" var="breadscrumUrl"/>
<script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAxxy66yub_hN7FFOKpN0U_niaGbA004C8"></script>

<c:set var="contextPath" value="${pageContext.request.contextPath}"/>

<style type="text/css">
	.no-close .ui-dialog-titlebar-close {display: none }
	#occContentDiv{
		overflow-y:hidden !important;
		background-color: white!important;
	}
	#bldg-info strong{
		font-weight:bold;
	}
	#bldg-info strong:AFTER {
		content: " ";
	}
	.active { color:red!important;text-decoration: underline!important; }
}
</style>

<script type="text/javascript">

var tabselected;
var path;
var map,mapData;
	$(document).ready(function() {
		$.cookie('uem_facilities_tab_selected', 'maps',  {path: '/' });
		var innerLayout;
		innerLayout = $('div.pane-center').layout( layoutSettings_Inner );
		//create tabs
		$("#innercenterreport").tabs({
			cache: true
		});
		tabselected = 'maps';
		showSettings();
		nodeclick();
		updateBreadscumLabel();
		
	});

function nodeclick() {
	$('#facilityTreeViewDiv').treenodeclick(function(){
		showSettings();	
		updateBreadscumLabel();
		if(map)
		{
			selectLocationOnMap();
		}
		
	});
}

var preSelection;
function gotoFacility(level,id)
{
	$(preSelection).removeClass('active');
	$("#floor"+id).addClass('active');
	preSelection = ("#floor"+id);
	
	var treeElement = $("#facilityTreeViewDiv").find("#" + treenodetype+"_"+treenodeid);
	if(treeElement){
		$("#facilityTreeViewDiv").jstree("deselect_node", treeElement);
	}
	treenodetype =level;//'building';
	treenodeid = id;//marker.buildingId;
	$.cookie('uem_facilities_tab_selected', 'charts',  {path: '/' });
	tabselected = 'charts';
	showSettings();
	updateBreadscumLabel();
	treeElement = $("#facilityTreeViewDiv").find("#" + treenodetype+"_"+treenodeid);
	if(treeElement){
		$("#facilityTreeViewDiv").jstree("select_node", treeElement);
	}
}
var orgMarkerBounds;
function drawMap() 
{
//   var mapData = [{id:1,name:'Pride Kumar',locX:18.539072,locY:73.829922,bldgLevel:5,consPercent:20},{id:2,name:'Building1',locX:18.470842,locY:73.821056,bldgLevel:3,consPercent:78}
//    ,{id:3,name:'Building2',locX:18.529932,locY:73.829479,bldgLevel:1,consPercent:50},{id:4,name:'Building3',locX:19.083873,locY:72.8659263,bldgLevel:2,consPercent:90}];
   var myLatlng = new google.maps.LatLng(0.0000, 0.0000);
   var mapOptions = {zoom: 1,center: myLatlng};
   map = new google.maps.Map(document.getElementById("map_div"),mapOptions);
   var markers = [];
   orgMarkerBounds = new google.maps.LatLngBounds();
   $.ajax({
		type: "GET",
		cache: false,
		url: '<spring:url value="/services/occupancyreportservice/loadBuildingOccupancyData/"/>'+${customerId},
		dataType: "json",
		success: function(data,textStatus,jqXHR) {
			mapData = data;
			$.each(mapData,function(index){
			  myLatlng = new google.maps.LatLng(this.locX, this.locY);
			  var x = 32;var y = 37;			 
			  // Sachin: TO REMOVE THIS HARDCODED RANGE VALUES
			  var color = "green";
			  if(this.occupPercent>=70){
				  color  = "red";
			  }else if(this.occupPercent>=30){
				  color  = "orange";  
			  }
			  var image = {url: '../themes/default/images/office-'+color+'.png',scaledSize: new google.maps.Size(x, y),optimized:true};
			  var infoContent = "<div id='bldg-info' style='width:200;height:100%' ><strong>Building:</strong> "+this.name+"</br><strong>No. of Sensors:</strong>"+this.avgNoOfSensors+"</br><strong>Avg. YTD Occupancy:</strong>"+this.occupPercent+"%</br>";
				var floorCont = "";
				if(this.childLevels)
				{
					floorCont = "<strong>Floors:</strong></br>";
					$.each(this.childLevels,function(index){
						var lid = "gotoFacility(\"floor\","+this.levelId+")";
						var fdet = this.levelName + " ("+this.occupPercent+"%)";
						floorCont = floorCont + "<a id='floor"+this.levelId+"' style='color:#009bc0!important;text-decoration:none;font-weight:normal;' class='link'  href='javascript:"+lid+";'>"+fdet+"</a></br>";	
					});
				}
				infoContent = infoContent + floorCont + "</div>";
			var tooltip = this.name+"|"+"YTD-"+this.occupPercent+"%";		
			  var marker = new google.maps.Marker({position: myLatlng,map: map,title:tooltip,icon: image});
			  marker.buildingId = this.buildingId;
			  orgMarkerBounds.extend(myLatlng);
			  
			  google.maps.event.addListener(marker, 'dblclick', function() {
				  gotoFacility("building",marker.buildingId);
			  });
			  
			  var op = 'Not Available',nos="Not Available";
			  if(this.occupPercent)
					op = this.occupPercent+"%"
				if(this.avgNoOfSensors)
					nos = this.avgNoOfSensors; 
			
			  var infowindow = new google.maps.InfoWindow({
			      content: infoContent,
			      position:myLatlng
			  });
			  google.maps.event.addListener(marker, 'click', function() {
				    infowindow.open(map,marker);
				});
		   });
		 map.fitBounds(orgMarkerBounds);
		},
		error: function(jqXHR,textStatus,errorThrown) {
			
		}
	}); 
}
function selectLocationOnMap()
{ 
	if(treenodetype=='campus'||treenodetype=='building'||treenodetype=='floor') {
		var markerBounds = new google.maps.LatLngBounds();
	    for(var i=0;i<mapData.length;i++)
		{
	    	if(treenodetype=='campus'&&mapData[i].campusId==treenodeid){
	    		markerBounds.extend(new google.maps.LatLng(mapData[i].locX, mapData[i].locY));
	        }else if(treenodetype=='building'&&mapData[i].buildingId==treenodeid){
	        	 markerBounds.extend(new google.maps.LatLng(mapData[i].locX, mapData[i].locY));
	        	 break;
	        }else if(treenodetype=='floor'&&mapData[i].childLevels){
	     		for(var j=0;j<mapData[i].childLevels.length;j++){
	     			if(mapData[i].childLevels[j].levelId==treenodeid){
	     				markerBounds.extend(new google.maps.LatLng(mapData[i].locX, mapData[i].locY));
	   	        	 break;
	     			}
	     		}
	        }
		}
	    map.fitBounds(markerBounds);		
	}else if(treenodetype=='organization'&&orgMarkerBounds){
		map.fitBounds(orgMarkerBounds);
	}
		
    
}

//common function to show floor plan for selected node
var showSettings =function(){	
	tabselected = $.cookie('uem_facilities_tab_selected');
	var accordianSelect = $.cookie('uem_accordian_select');
	if(accordianSelect==null)
	{
		$.cookie('uem_accordian_select', 'facility',  {path: '/' });
	}
	if (tabselected == 'charts') {
		$('#occchart').click();
	}else if(tabselected == 'maps'){
		$("#occmaps").click();		
	}
}

function showNodeSpecificTabs()
{
	tabselected = $.cookie('uem_profile_tab_selected');
	//the 'accTabSelected' variable is global and defined in facility_tree.jsp

	$("#occmaps").show();
	$('#occchart').show();
	if (tabselected == 'maps') {
		$("#occmaps").click();		
	}
	else if (tabselected == 'charts') {
		$("#occchart").click();
	}
}

//fuction to show allowed tabs as per accordion tab selected
function setAllowedTab() {
	$('#accordionfacility h2').accordiontabclick(function(){
			$.cookie('uem_accordian_select', 'facility',  {path: '/' });
 			showSettings();
	});
}

function loadPreSenseCharts(){
	var ifr;
    ifr = document.getElementById("chartFrame");
    ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
    
	if (treenodetype == 'floor') {
	    ifr.src="${loadOccReport}?customerId="+${customerId}+"&ts="+new Date().getTime();		
	}
	else {
	    ifr.src="${loadOccReportNonFloorLevel}?customerId="+${customerId}+"&ts="+new Date().getTime();
	}
	tabselected = 'charts';
	$.cookie('uem_facilities_tab_selected', 'charts',  {path: '/' });
    return false;
}

function loadPreSenseMaps(){
	tabselected = 'maps';
	$.cookie('uem_facilities_tab_selected', 'maps',  {path: '/' });
	if(!map){
		drawMap();
	}
	
	//ifr = document.getElementById("mapsFrame");
	//if($("#mapsFrame").attr('map-loaded')=='false')
	//{
	//	ifr.contentWindow.document.body.innerHTML = "&nbsp;<spring:message code='action.loading'/>";
	//	ifr.src="${loadOccMap}?customerId="+${customerId}+"&ts="+new Date().getTime();
	//	$("#mapsFrame").attr('map-loaded','true');
	//}	
	return false;
}

function updateBreadscumLabel()
{
	var url = "${breadscrumUrl}" +treenodeid +"?ts="+new Date().getTime();
		$.ajax({
		        type: "GET",
		        cache: false,
		        async: false,
		        url: url,
		        dataType: "text",
		        success: function(msg) {
		        	path = msg;
		        }
		});
}
//END PROFILE
</script>

<div id="innercenterreport" class="ui-layout-center">
	<ul>		
        <li><a id="occmaps" href="#tab_maps" onclick="loadPreSenseMaps();"><span>Map</span></a></li>
        <li><a id="occchart" href="#tab_charts" onclick="loadPreSenseCharts();"><span>Charts</span></a></li> 		
	</ul>
	
	<div class="ui-layout-content ui-widget-content ui-corner-bottom" id="occContentDiv" style="border-left: 0; border-top: 0; padding: 0px;">
		<div id="tab_maps" class="pnl_rht">
			<div style="padding-top: 2px;font-weight: bold;"><span id="breadscrumHeader"></span></div>
    		<div id="map_div" style="width: 100%; height: 100%"></div>
		</div>
		<div id="tab_charts" class="pnl_rht"><iframe frameborder="0" id="chartFrame" style="width: 100%; height: 100%;"></iframe></div>
	</div>	
</div>
