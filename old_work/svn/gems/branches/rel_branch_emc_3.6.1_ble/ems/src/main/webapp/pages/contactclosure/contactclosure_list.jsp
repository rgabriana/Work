<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<spring:url value="/services/org/contactclosure/enabledisablecontactclosure" var="enableDisableContactClosureUrl" scope="request" />

<spring:url value="/services/org/contactclosure/discover" var="discoverContactClosureUrl" scope="request" />

<spring:url value="/services/org/contactclosure/getcontactclosure" var="getContactClosureListUrl" scope="request" />

<spring:url value="/contactClosure/contactclosure_form.ems" var="contactClosureFormUrl" scope="request" />

<spring:url value="/services/org/contactclosure/removecontactclosure" var="removeContactClosureUrl" scope="request" />

<style type="text/css">

html {height:100% !important;}

</style>

<script type="text/javascript">

	var MAX_ROW_NUM = 99999;
	var COLOR_SUCCESS = "green";
	var COLOR_DEFAULT = "black";
	var COLOR_ERROR = "red";
	var contact_closure_load__retry_counter = 0;
	var LOADING_IMAGE_STRING = "";


	
	
	$(document).ready(function() {
		
		$('#discoverButtonId').css("display", "none");
		$('#enableContactClosureId').prop('checked', false);
		
		LOADING_IMAGE_STRING = "<img alt='loading' src='../themes/default/images/ajax-loader_small.gif'>";
		
		jQuery("#contactClosureTable").jqGrid({
			datatype: "local",
			autowidth: true,
			scrollOffset: 0,
			hoverrows: false,
			forceFit: true,
		   	colNames:['MAC Address', 'IP Address','Action'],
		   	colModel:[
		   		{ name:'macAddress', index: 'macAddress',sortable:true,sorttype:'string',width:'10%'},
		        { name:'ipAddress', index: 'ipAddress', sortable:true,width:'6%'},
		   		{name:'action', index:'action', align:"right", sortable:false,width:"20%"}
		   	],
		   	cmTemplate: { title: false },
 		   	rowNum:MAX_ROW_NUM,
		   	sortname: 'macAddress',
		    viewrecords: true,
		    sortorder: 'asc',
		    loadComplete: function() {
		   		ModifyContactClosureTableGridDefaultStyles();
		    }    
		});

		forceFitContactClosureTableWidth();
		
		//jQuery("#contactClosureTable").jqGrid('navGrid',"#contactClosurePagingDiv",{edit:false,add:false,del:false});
		
		//$("#contactClosureTable").jqGrid().setGridParam({sortname: 'macAddress', sortorder:'asc'}).trigger("reloadGrid");
		loadContactClosureList();
		
		
		$(window).load(function() {
            window.editContactClosure = function(macAddress) {
            	clearCcListLabelMessage();
                $("#contactClosureFormDialog").load("${contactClosureFormUrl}?macAddress="+macAddress+"&ts="+new Date().getTime()).dialog({
                    title : "Edit Contact Closure",
                    width :  Math.floor($('body').width() * .70),
                    minHeight : 400,
                    modal : true
                });
                return false;
            }
        });
		
		
	});

	function removeContactClosure(macAddress) {
		var r = confirm("Are you sure you want to delete contact closure with mac address " + macAddress+" !");
		if( r == true) {
			$.ajax({
		 		type: 'POST',
		 		url: "${removeContactClosureUrl}/"+macAddress,
		 		success: function(data){
		 			if(data == "true") {
			 			var id = jQuery("#contactClosureTable").jqGrid('getGridParam','selrow');
			 			if (id)	{
			 				jQuery("#contactClosureTable").jqGrid('delRowData',id);
			 			}
		 			}
		 		}
			});
			
		}
	}
	
	function loadContactClosureList(){
		$.ajax({
	 		type: 'GET',
	 		url: "${getContactClosureListUrl}"+"?ts="+new Date().getTime(),
	 		dataType: "json",
	 		success: function(data){
				var mydata =  [];
				var isContactClosureEnabled = "false";
				if(data.enabled == "true"){
					isContactClosureEnabled = "true";
				}
				if (data.contactClosureVo != undefined) {
			   		if (data.contactClosureVo.length == undefined) {
			   			// Hack: Currently, JSON serialization via jersey treats single item differently
			   			var localData = new Object;
			   			if(data.contactClosureVo.macAddress != ''){
			   				localData.macAddress = data.contactClosureVo.macAddress;
							localData.ipAddress = data.contactClosureVo.ipAddress;
							localData.action = "";
							if(isContactClosureEnabled == "true"){
								localData.action += "<button onclick=\"javascript: editContactClosure(\'"+data.contactClosureVo.macAddress+"\');\">Edit</button>";
								localData.action += "&nbsp;&nbsp;<button onclick=\"javascript: removeContactClosure(\'"+data.contactClosureVo.macAddress+"\');\">Remove</button>";
							}else{
								localData.action += "<button disabled>Edit</button>";
								localData.action += "&nbsp;&nbsp;<button disabled>Remove</button>";
							}
							mydata.push(localData);
			   			}
						
						if(isContactClosureEnabled == "true"){
							$('#discoverButtonId').css("display", "block");
							$('#enableContactClosureId').prop('checked', true);
						}else{
							$('#discoverButtonId').css("display", "none");
							$('#enableContactClosureId').prop('checked', false);
						}
			   			
			   		}else{
			   			$.each(data.contactClosureVo, function(i, obj) {
							var localData = new Object;
							localData.macAddress = obj.macAddress;
							localData.ipAddress = obj.ipAddress;
							localData.action = "";
							if(isContactClosureEnabled == "true"){
								localData.action += "<button onclick=\"javascript: editContactClosure(\'"+obj.macAddress+"\');\">Edit</button>";
								localData.action += "&nbsp;&nbsp;<button onclick=\"javascript: removeContactClosure(\'"+obj.macAddress+"\');\">Remove</button>";
							}else{
								localData.action += "<button disabled>Edit</button>";
								localData.action += "&nbsp;&nbsp;<button disabled>Remove</button>";
							}
							mydata.push(localData);
						});
			   			
			   			if(data.enabled == "true"){
							$('#discoverButtonId').css("display", "block");
							$('#enableContactClosureId').prop('checked', true);
						}else{
							$('#discoverButtonId').css("display", "none");
							$('#enableContactClosureId').prop('checked', false);
						}
			 			
			   		}
			   	}
				if(mydata)
				{
					for(var i=0;i<mydata.length;i++)
					{
						jQuery("#contactClosureTable").jqGrid('addRowData',mydata[i].id,mydata[i]);
					}
				}
			},
			error: function(){
				console.log("error in getting contact closure list");
			},
	 		contentType: "application/json; charset=utf-8"
	 	});
		
	}
	
	function forceFitContactClosureTableWidth(){
		var jgrid = jQuery("#contactClosureTable");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var outerDivHeight2 = $("#outerDiv2").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		//var gridFooterHeight = $("#contactClosurePagingDiv").height();
		
		//jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - outerDivHeight2 - gridHeaderHeight - gridFooterHeight - 10);
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - outerDivHeight2 - gridHeaderHeight - 40);
		
		$("#contactClosureTable").setGridWidth($(window).width() - 40);
	}
	
	//this is to resize the jqGrid on resize of browser window.
	$(window).bind('resize', function() {
		forceFitContactClosureTableWidth();
	}).trigger('resize');
	
	function ModifyContactClosureTableGridDefaultStyles() {  
		   $('#' + "contactClosureTable" + ' tr').removeClass("ui-widget-content");
		   $('#' + "contactClosureTable" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "contactClosureTable" + ' tr:nth-child(odd)').addClass("oddTableRow");
	}
	
	function startDiscovery(){
		
		displayCclistLabelMessage("Discovery initiated...Please wait.."+LOADING_IMAGE_STRING, COLOR_SUCCESS);
		
		$('#discoverButtonId').prop("disabled", true);
		$('#enableDisableContactClosure').prop("disabled", true);
		contact_closure_load__retry_counter = 0;
		
		$.ajax({
    		type: 'POST',
    		url: "${discoverContactClosureUrl}?ts="+new Date().getTime(),
    		dataType:"json",
    		success: function(data){
    			$('#contactClosureTable').jqGrid('clearGridData');
    			//loadContactClosureList();
    			retryLoadContactClosureList();
    			//clearCcListLabelMessage();
    		},
    		error: function(){
				displayCclistLabelMessage('Error in Discovery.',COLOR_ERROR);
				contact_closure_load__retry_counter = 0;
				$('#discoverButtonId').prop("disabled", false);
				$('#enableDisableContactClosure').prop("disabled", false);
			}
    	});
		
	}
	
	function retryLoadContactClosureList(){
		$('#contactClosureTable').jqGrid('clearGridData');
		loadContactClosureList();
		loopLoadContactClosureList();
	}
	
	function loopLoadContactClosureList(){
		
		if(contact_closure_load__retry_counter < 15) {
			var contact_closure_load_timer = setTimeout("retryLoadContactClosureList()", 2000);
			contact_closure_load__retry_counter++;
		} else {
			clearCcListLabelMessage();
			displayCclistLabelMessage("Discovery completed.", COLOR_SUCCESS);
			$('#discoverButtonId').prop("disabled", false);
			$('#enableDisableContactClosure').prop("disabled", false);
		}
	}
	
	function displayCclistLabelMessage(Message, Color) {
		$("#contact_closure_message").html(Message);
		$("#contact_closure_message").css("color", Color);
	}
    function clearCcListLabelMessage(Message, Color) {
		displayCclistLabelMessage("", COLOR_DEFAULT);
	}
	
    function enableDisableContactClosureSettings(){
    	$('#enableDisableContactClosure').prop("disabled", true);
    	clearCcListLabelMessage();
    	
    	var enableDisableContactClosureString = "false";
    	
    	if($('#enableContactClosureId').is(":checked")){
    		enableDisableContactClosureString = "true";
    	}
    	else{
    		enableDisableContactClosureString = "false";
    	}
			
		$.ajax({
	 		type: 'POST',
	 		url: "${enableDisableContactClosureUrl}?ts="+new Date().getTime(),
	 		contentType: "application/json",
	 		data: '{"enabled":"' + enableDisableContactClosureString +'"}', 
	 		dataType: "json",
	 		success: function(data){
				displayCclistLabelMessage('Contact Closure Settings successfully saved.',COLOR_SUCCESS);
				if(enableDisableContactClosureString == "true"){
					$('#discoverButtonId').css("display", "block");
				}else{
					$('#discoverButtonId').css("display", "none");
				}
				$('#contactClosureTable').jqGrid('clearGridData');
    			loadContactClosureList();
    			$('#enableDisableContactClosure').prop("disabled", false);
			},
			error: function(){
				displayCclistLabelMessage('Error.',COLOR_ERROR);
				$('#enableDisableContactClosure').prop("disabled", false);
			}
	 	});
    	
    }
    
    function closeContactClosureFormDialog(){
    	$("#contactClosureFormDialog").dialog('close');        	
    }
	
</script>
<div id="contactClosureFormDialog"></div>
<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" >
	<div id="outerDiv" style="padding: 5px 5px 5px 5px;">
		<div><label style="padding: 5px 5px 5px 5px;font-weight:bold">Contact Closure Configuration</label></div>
		<div style="height:5px"></div>
		<div style="padding: 5px 5px 5px 5px;">
		<table style="width: 100%;">
			<tr>
				<td align="left"><input type="checkbox" id="enableContactClosureId"/>  Enable
				</td>
				<td align="center">
				<div id="contact_closure_message" style="font-size: 14px; font-weight: bold;" ></div>
				</td>
				<td align="right"><button id="discoverButtonId" onclick="startDiscovery();">Discover</button>
				</td>
			</tr>
		</table>
		</div>
	</div>
	<div style="padding: 0px 5px;">
		<table id="contactClosureTable"></table>
		<div id="outerDiv2" style="padding: 5px 5px 5px 0px;">
		<table style="width: 100%;">
			<tr>
				<td align="left"><button id="enableDisableContactClosure" onclick="enableDisableContactClosureSettings();">Save</button>
				</td>
			</tr>
		</table>
		</div>
	</div>
</div>