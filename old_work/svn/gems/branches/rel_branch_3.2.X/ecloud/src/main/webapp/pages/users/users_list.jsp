<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>

<spring:url value="/users/create.ems" var="createUserUrl" scope="request" />
<spring:url value="/users/edit.ems" var="editUserUrl" scope="request" />
<spring:url value="/users/assigncustomers.ems" var="assignCustomersUrl" scope="request" />

<spring:url value="/services/org/users/delete/" var="deleteuserUrl" scope="request" />

<div id="userDetailsDialog"></div>
<div id="assignCustomersDialog"></div>

<div style="width: 100%; height: 100%; background: #fff; padding: 0px 5px 0px 0px" id="userDiv">
	<div id="outerDiv" style="padding: 5px 5px 5px 5px">
			<button id="newUserButton">New User</button>
			<div style="min-height:5px"></div>
	</div>
	<div style="padding: 0px 5px;">
		<table id="usersListGrid"></table>
		<div id="pagingUsers"></div>
	</div>
</div>
		
<script type="text/javascript">

$(document).ready(function() {		
		start(1, "email", "desc");		
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
		   $("#usersListGrid").setGridWidth($(window).width()-25);
	}
	
	function forceFitGridTableHeight(){
		var jgrid = jQuery("#usersListGrid");
		var containerHeight = $("body").height();
		var headerHeight = $("#header").height();
		var footerHeight = $("#footer").height();
		var outerDivHeight = $("#outerDiv").height();
		var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
		var gridFooterHeight = $("#pagingUsers").height();
		
		jgrid.jqGrid("setGridHeight", containerHeight - headerHeight - footerHeight - outerDivHeight - gridHeaderHeight - gridFooterHeight - 20);
		
		$("#usersListGrid").setGridWidth($(window).width() - 25);
	}
	
	function start(pageNum, orderBy, orderWay) {
		jQuery("#usersListGrid").jqGrid({
			url: '<spring:url value="/services/org/users/list/users"/>',
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
				{name:'email', index:'email', align:"center", sortable:true, sorttype:'string',width:"8%", search:false},
				{name:'firstname', index:'firstname', align:"center", sortable:true, sorttype:'string',width:"8%", search:false},
				{name:'lastname', index:'lastname', align:"center", sortable:true, sorttype:'string',width:"8%", search:false},
				{name:'roletype', index:'roletype', align:"center", sortable:true, sorttype:'string',width:"8%", search:false},
				{name:'status', index:'status', align:"center", sortable:true, sorttype:'string',width:"5%", search:false},
				{name:'action', index:'action', align:"center", sortable:false, width:"8%", search:false,formatter: actionButtonRenderer}					   		
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
		    pager: '#pagingUsers',
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
		   		if (data != null) {
			   		if (data.userlist != undefined) {
			   			if (data.userlist.length == undefined) {		   			
			   				jQuery("#usersListGrid").jqGrid('addRowData', 0, data.userlist);
			   			}
			   		}
			   	}		   		
		   		ModifyGridDefaultStyles();
			}
		});
		
		
		jQuery("#usersListGrid").jqGrid('navGrid',"#pagingUsers",
				{edit:false,add:false,del:false,search:false}, 
				{}, 
				{}, 
				{}, 
				{},
				{});

		forceFitGridTableHeight();
		
	}
	
function showCustomersForm(userId)
{
	$("#assignCustomersDialog").load("${assignCustomersUrl}?userId="+userId+ "&ts="+ new Date().getTime()).dialog({
        title : "Assign Customers to User",
        width :  Math.floor($('body').width() * .35),
        position : ['top',50],
        //minHeight : 300,
        overflow: scroll,
        resizable: false,
        create: function() {
            $(this).css("maxHeight", Math.floor($('body').width() * .35)); 
        },
        modal : true,
        close: function() {
			$("#assignCustomersDialog").html("");
        }
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
   $("#userDetailsDialog").load("${editUserUrl}?userId="+userId+ "&ts="+ new Date().getTime()).dialog({
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

