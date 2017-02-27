<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<spring:url value="/users/create.ems" var="createUserUrl" scope="request" />
<spring:url value="/users/edit.ems" var="editUserUrl" scope="request" />
<spring:url value="/users/assigncustomers.ems" var="assignCustomersUrl" scope="request" />

<spring:url value="/services/org/users/delete/" var="deleteuserUrl" scope="request" />

<div id="userDetailsDialog"></div>
<div id="editUserDetailsDialog"></div>
<div id="assignCustomersDialog"></div>
<button id="newUserButton">New User</button>
<div id="userDiv">
					<table id="usersListGrid"></table>
					<div id="pagingUsers"></div>					
</div>
		
<script type="text/javascript">

$(document).ready(function() {		
		start('1#######END', 1, "status", "desc");		
	});
	
	$('#newUserButton').click(function() {
            	//clearLabelMessage();
                $("#userDetailsDialog").load("${createUserUrl}").dialog({
                    title : "New User",
                    width :  Math.floor($('body').width() * .35),
                    minHeight : 300,
                    modal : true
                });
                return false;
            });
            
	function ModifyGridDefaultStyles() {  
		   $('#' + "usersListGrid" + ' tr').removeClass("ui-widget-content");
		   $('#' + "usersListGrid" + ' tr:nth-child(even)').addClass("evenTableRow");
		   $('#' + "usersListGrid" + ' tr:nth-child(odd)').addClass("oddTableRow");
	}
	
	function forceFitGridTableHeight(){
		var jgrid = jQuery("#usersListGrid");
		var containerHeight = $(this).height();		
		
		var gridHeight = jgrid.parents("div.ui-jqgrid:first").height();
		var gridBodyHeight = jgrid.parents("div.ui-jqgrid-bdiv:first").height();
		var gridHeaderFooterHeight = gridHeight - gridBodyHeight;		
		jgrid.jqGrid("setGridHeight", Math.floor((containerHeight - gridHeaderFooterHeight - 120) * .99));		 
	}
	
	function start(inputdata, pageNum, orderBy, orderWay) {
		jQuery("#usersListGrid").jqGrid({
			url: '<spring:url value="/services/org/users/list/users"/>',
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
		   	colNames:["id", "Email(Username)","First Name", "Last Name", "Role", "Status","Action"],
		   	colModel:[
				{name:'id', index:'id', hidden:true},
				{name:'email', index:'email', align:"center", sortable:true, width:"8%", search:false},
				{name:'firstname', index:'firstname', align:"center", sortable:true, width:"8%", search:false},
				{name:'lastname', index:'lastname', align:"center", sortable:true, width:"8%", search:false},
				{name:'roletype', index:'role', align:"center", sortable:true, width:"8%", search:false},
				{name:'status', index:'status', align:"center", sortable:true, width:"5%", search:false},
				{name:'id', index:'id', align:"center", sortable:true, width:"8%", search:false,formatter: actionButtonRenderer}					   		
		   	],
		   	jsonReader: { 
		        root:"userlist", 
		        page:"page", 
		        total:"total", 
		        records:"records",
		        repeatitems:false,
		        id : "id"
		   	},
		   	cmTemplate: { title: false },
		    multiselect: false, 		   	
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
		   		if (data.userlist != undefined) {
			   		if (data.userlist.length == undefined) {
			   			// Hack: Currently, JSON serialization via jersey treats single item differently			   			
			   			jQuery("#usersListGrid").jqGrid('addRowData', 0, data.userlist);
			   		}
			   	}		   		
		   		ModifyGridDefaultStyles();
				forceFitGridTableHeight();		   		
		   	}
		});
	}
	
function showCustomersForm(userId)
{
$("#assignCustomersDialog").load("${assignCustomersUrl}?userId="+userId+ "&ts="+ new Date().getTime()).dialog({
                    title : "Assign Customers to User",
                    width :  Math.floor($('body').width() * .35),
                    minHeight : 300,
                    modal : true
                });
}

function showDeleteForm(userId)
{	
	var con=confirm("Are you sure you want to delete this user ?");
	if (con==true)
  	{  	
  	$.ajax({
			type: 'POST',
			url: "${deleteuserUrl}"+userId+"?ts="+new Date().getTime(),
			data: "",
			success: function(data){
				location.reload();
			},			
			contentType: "application/json; charset=utf-8"
		});  	
  	}	
}

function editCustomersForm(userId)
{
   $("#editUserDetailsDialog").load("${editUserUrl}?userId="+userId+ "&ts="+ new Date().getTime()).dialog({
                    title : "Edit User",
                    width :  Math.floor($('body').width() * .35),
                    minHeight : 300,
                    modal : true
                });
}
	
function actionButtonRenderer(cellvalue, options, rowObject){
		var source = "";		
		source = "<button onclick=\"editCustomersForm(" + rowObject.id + ");\">Edit</button>&nbsp;<button onclick=\"showCustomersForm(" + rowObject.id + ");\">Assign Customers</button>&nbsp;<button onclick=\"showDeleteForm(" + rowObject.id + ");\">Delete</button>&nbsp;";
		return source;
	}

</script>

