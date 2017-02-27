<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>

<spring:url value="/services/org/fixture/" var="getPlacedFixturesByFloorIdUrl" scope="request" />

<div id="fixtureDialog"></div>

<spring:url value="/themes/default/images/connectivityHealthy.png" var="fixtureConnected"/>
<spring:url value="/themes/default/images/connectivityPowerOff.png" var="fixtureNoConnectivity1"/>
<spring:url value="/themes/default/images/connectivityProblem.png" var="fixtureNoConnectivity2"/>
<spring:url value="/themes/default/images/noconnectivity3.png" var="fixtureNoConnectivity3"/>
<spring:url value="/services/org/getServerTimeOffsetFromGMT" var="getServerTimeOffsetFromGMTUrl" scope="request" />

<spring:url value="/themes/default/images/ConnectivityHealthyHopper.png" var="fixtureConnectivityHealthyHopper"/>
<spring:url value="/themes/default/images/ConnectivityPowerOffHopper.png" var="fixtureConnectivityPowerOffHopper"/>
<spring:url value="/themes/default/images/ConnectivityProblemHopper.png" var="fixtureConnectivityProblemHopper"/>

<script type="text/javascript">
var PROFILE_LIST = {};
var EM_PROFILE_MAPPING={};
var PAGE = "${page}";
	$(document).ready(function() {
		getAllProfiles();
		start('1#######END', 1, "id", "desc");
	});
	function getAllProfiles(){
		PROFILE_LIST = {};
		<c:forEach items="${groups}" var="group">
		PROFILE_LIST["${group.id}"] = "${group.name}";
		</c:forEach>
		
		EM_PROFILE_MAPPING={};
		<c:forEach items="${emProfileMappings}" var="emProfileMapping">
		EM_PROFILE_MAPPING["${emProfileMapping.emGroupId}"] = "${emProfileMapping.uemProfileId}";
		</c:forEach>
		
	}
	function start(inputdata, pageNum, orderBy, orderWay) {
		jQuery("#fixtureTable").jqGrid({
			url: '<spring:url value="/services/org/fixture/list/alternate/filter/${page}/${pid}"/>',
			userData: "userdata",
			mtype: "POST",
			postData: {"userData": inputdata},
			datatype: "json",
			autoencode: true,
			hoverrows: false,
			autowidth: true,
			scrollOffset: 0,
			forceFit: true,
			formatter: {
				 integer: {thousandsSeparator: ",", defaultValue: '0'},
			     number: {decimalSeparator:".", thousandsSeparator: ",", decimalPlaces: 2, defaultValue: '0.00'}
			},
		   	colNames:["id", "groupid","Status", "MAC Address", "Sensor Name", "Image", "Version", "Gateway", "Current Profile", "Upgrade Status", "Action"],
		   	colModel:[
				{name:'id', index:'id', hidden:true},
				{name:'groupid', index:'groupid', hidden:true},
				{name:'status', index:'status', align:"center", sortable:true, width:"8%", search:false, formatter: statusImageRenderer},
		   		{name:'snapaddress', index:'snapaddress', sortable:true, width:"11%", searchoptions:{sopt:['cn']},formatter: snapAddressrenderer},
		   		{name:'name', index:'name', sortable:true, width:"13%", searchoptions:{sopt:['cn']},formatter: fixturenamerenderer},
		   		{name:'currapp', index:'currapp', sortable:true, width:"8%", search:false, formatter: currAppRenderer},
		   		{name:'version', index:'version', sortable:true, width:"10%", formatter: currVersionRenderer, searchoptions:{sopt:['cn']}},
		   		{name:'gateway', index:'gateway', sortable:true, width:"10%", searchoptions:{sopt:['cn']}, formatter: gatewayNameRenderer},
		   		{name:'currentprofile', index:'currentprofile', sortable:true, width:"12%", searchoptions:{sopt:['cn']},formatter: profileNameRenderer},
		   		{name:'upgradestatus', index:'upgradestatus', sortable:true, width:"14%", searchoptions:{sopt:['cn']}},
		   		{name:'state', index:'state', align:"right",hidden:(PAGE!="floor"), sortable:true , width:"12%", searchoptions:{sopt:['cn']}, formatter: actionImageRenderer}
		   	],
		   	jsonReader: { 
		        root:"fixture", 
		        page:"page", 
		        total:"total", 
		        records:"records",
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    multiselect: false,
 		   	pager: '#fixturePagingDiv',
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
		   		if (data!=null && data.fixture != undefined) {
			   		if (data.fixture.length == undefined) {
			   			// Hack: Currently, JSON serialization via jersey treats single item differently
			   			jQuery("#fixtureTable").jqGrid('addRowData', 0, data.fixture);
			   		}
			   	}
		   		ModifyGridDefaultStyles();
		   	}

		});
		
		jQuery("#fixtureTable").jqGrid('navGrid',"#fixturePagingDiv",{edit:false,add:false,del:false});
		
		forceFitFixtureTableHeight();
	}
	
	function statusImageRenderer(cellvalue, options, rowObject){

		if (rowObject.state == 'COMMISSIONED') {

			var serverCurrentTime =  "${currenttime}";
			var connectivityDifference = ((serverCurrentTime - rowObject.lastconnectivityat)/1000)/60;
			
	        var source = "";
	
			if(connectivityDifference <= 15) // less than 15 mins
			{
				if(rowObject.ishopper == 1){
					source = "${fixtureConnectivityHealthyHopper}";
				}else{
					source = "${fixtureConnected}";
				}
			}
			else if(connectivityDifference > 15 && connectivityDifference <= 10080 ) // between 15 minutes and 7 days
			{
				if(rowObject.ishopper == 1){
					source = "${fixtureConnectivityPowerOffHopper}";
				}else{
					source = "${fixtureNoConnectivity1}";
				}
			}
			else if(connectivityDifference > 10080) // greater than 7 days
			{
				if(rowObject.ishopper == 1){
					source = "${fixtureConnectivityProblemHopper}";
				}else{
					source = "${fixtureNoConnectivity2}";
				}
			}
			return "<img src=\""+source+"\" height=16 width=16/>";
		}
		else
			return "";
	}
	
	function parseConnectivityDate(str)
	{
		
		var date = 0;
		// code to change the date format as Date.parse(str) function is failing in IE8 for the date format returned from server.
		//for example changing str from '2012-06-28T16:00:15.142+05:30' to '2012/06/28 16:00:15' and adding the millisec(142) after the parse for IE8
		if($.browser.msie && (parseInt($.browser.version) == 8)){
			var strArray = str.split(".");
			str = strArray[0].replace(/[-]/g, "/");
			str = str.replace(/[T]/g, " ");
			tempArray = strArray[1].split("+");
			date = Date.parse(str) + Number(tempArray[0]);
			return date;
			
		}else{
			date = Date.parse(str);
			return date;
		}
		
	}
	
	function actionImageRenderer(cellvalue, options, rowObject){
		var source = "";
		
		if (rowObject.state == 'COMMISSIONED') {
				source = "<button onclick=\"javascript: parent.parent.showFixtureForm(" + rowObject.id +","+ "${pid}"  + ");\"><spring:message code='fixtureForm.label.detailsBtn' /></button>";
		}
		return source;
	}

	function gatewayNameRenderer(cellvalue, options, rowObject){
		// This is used, so that it can render the single records as well. otherwise jsonmap=gateway.name can be 
		// used
		if(rowObject.gateway.name == undefined){
			return "";
		}else{
			return rowObject.gateway.name;
		}
		return "";
	}
	
	function profileNameRenderer(cellvalue, options, rowObject)
	{
		var groupName="";
		if(rowObject.groupid<=16)
		{
			groupName = rowObject.currentprofile;
		}else
		{
			var uemGroupId = EM_PROFILE_MAPPING[rowObject.groupid];
			groupName = PROFILE_LIST[uemGroupId];
		}
		return groupName;
	}
	function currAppRenderer(cellvalue, options, rowObject) {
		if (rowObject.version.indexOf("2") == 0) {
			return '<spring:message code="fixture.current.app.2" />';
		} else {
			if(cellvalue == '1') {
				return '<spring:message code="fixture.current.app.1" />';
			}
			else if(cellvalue == '2') {
				return '<spring:message code="fixture.current.app.2" />';
			}
			else {
				//TODO: return display text value instead of code.
				return cellvalue;	
			}
		}
		
	}
	
	function currVersionRenderer(cellvalue, options, rowObject) {
		if (rowObject.version.indexOf("2") == 0) { 
			return rowObject.version;
		} else {
			if(rowObject.currapp == "1") {
				return rowObject.firmwareversion;
			}
			else if(rowObject.currapp == "2") {
				return rowObject.version;
			}
			else {
				//TODO: return display text value instead of code.
				return cellvalue;	
			}
		}
		
	}
	
	function fixturenamerenderer(cellvalue, options, rowObject)
	{
		if(rowObject.ishopper==1)
			return rowObject.name + " (Hopper) " ;
			else
				return rowObject.name ;
	}
	function forceFitFixtureTableHeight(){
		var jgrid = jQuery("#fixtureTable");
		var containerHeight = $(this).height();
		var otherElementHeight = $("#fixture-list-topPanel").height();
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;
		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - otherElementHeight - gridHeaderFooterHeight) * .99)); 
	}

	function snapAddressrenderer(cellvalue, options, rowObject)
	{
		var strMacAddress = "";
        if(cellvalue.indexOf(":") <= 0)
        {
            while(cellvalue.length > 0)
            {
                if(cellvalue.length >= 3)
                {
                    strMacAddress += cellvalue.substr(0,2) + ":";
                    cellvalue = cellvalue.substr(2);
                }
                else
                {
                    strMacAddress += cellvalue.substr(0,2);
                    cellvalue = cellvalue.substr(2);
                }
            }
        }
        else
        {
        	var macSplitArray;
        	macSplitArray = cellvalue.split(':');
        	for (var i = 0; i < macSplitArray.length; i++) {
        	    if(macSplitArray[i].length == 1){
        	    	macSplitArray[i] = "0"+macSplitArray[i];
        	    }
        	    
        	    if(i == 0){
        			strMacAddress = macSplitArray[i];
        		}else{
        			strMacAddress = strMacAddress + ":" + macSplitArray[i];
        		}
        	    
        	}
        }
       return strMacAddress;
	}
	
	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		$("#fixtureTable").setGridWidth($(window).width()-20);
	}).trigger('resize');	
	
	
	function ModifyGridDefaultStyles() {  
		   $('#' + "fixtureTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "fixtureTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "fixtureTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
		   //$('#' + "fixtureTable").removeClass("ui-jqgrid-htable");
	}
	
	function getProfileObjById(profileId)
	{
		if(PROFILE_LIST!=null && PROFILE_LIST.length>0)
		{
			var profile = new Object();
			for (var i=0;i<PROFILE_LIST.length;i++)
			{
				var profileObj = PROFILE_LIST[i];
				
				if(profileObj.id == profileId)
				{
					profile.id = profileObj.id;
					profile.name = profileObj.name;
					profile.defaultProfile = profileObj.defaultProfile;
					profile.profileNo = profileObj.profileNo;
					break;
				}
			}
			return profile;
		}
		return null;
	}
		
</script>
<style type="text/css">
	html, body{margin:3px 0px 0px 0px !important; background: #ffffff; overflow: hidden !important;}
</style>
<table id="fixtureTable"></table>
<div id="fixturePagingDiv"></div>