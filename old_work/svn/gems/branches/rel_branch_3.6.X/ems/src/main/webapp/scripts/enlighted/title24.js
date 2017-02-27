
//DEPENDENT GRID OBJBECT
//var noofdefaultcolumns = 3;
//var gridTimSwitchControl;
//var colnames = ['Step Detail', 'Step No', 'Test Name'];
//var colmodels = [{
//    name: 'name',
//    classes: "stepdetailcol",
//    index: 'name',
//    editable: true,
//    width: 350
//}, {
//    name: 'test',
//    index: 'test',
//    editable: true,
//    hidden: false,
//    width: 50
//}, {
//    name: 'step',
//    index: 'step',
//    editable: true,
//    hidden: false,
//    width: 50
//}];
//var spaceNo = 0;
//var spaceRowMap = new Object();
//var gridspaceDetail;
//var spacerowmap0 = new Object();
var dependentGridsDetails = [
		{
			name : "#timeSwitchControlTable",
			grdObj : null,
			parentGrid : null,
			noofdefaultcolumns : 4,
			colnames : ['', '', 'Step No', 'Test Name'],
			colmodels : [{
						    name: 'itemno',
						    classes: "stepdetailcol",
						    index: 'itemno',
						    editable: false,
						    width: 50
						},{
						    name: 'name',
						    classes: "stepdetailcol",
						    index: 'name',
						    editable: false,
						    sortable: false,
						    width: 350
						}, {
						    name: 'test',
						    index: 'test',
						    editable: true,
						    hidden: false,
						    width: 50
						}, {
						    name: 'step',
						    index: 'step',
						    editable: true,
						    hidden: false,
						    width: 50
						}],
			spaceRowMap : new Object(),
			spaceNo : 0,
			data: 			
					[{
				        "name": "All lights can be turned on and off by their respective area control switch",
				        "test": "1. Automatic Time Switch Controls",
				        "step": "Step 1: Simulate occupied condition",
				        "itemno" : "a"
				    }, {
				        "name": "Verify the switch only operates lighting in the ceiling&dash;height partitioned area in which the switch is located",
				        "test": "1. Automatic Time Switch Controls",
				        "step": "Step 1: Simulate occupied condition",
				        "itemno" : "b"
				    }, {
				        "name": "All lighting, including emergency and egress lighting, turns off. Exempt lighting may remain on per Section 130.1(c)1 and 130.1(a)1.",
				        "test": "1. Automatic Time Switch Controls",
				        "step": "Step 2: Simulate unoccupied condition",
				        "itemno" : "a"
				    },
		
				    {
				        "name": "Manual override switch allows only the lights in the selected ceiling height partitioned space where the override switch is located and remain on no longer than 2 hours (unless serving public areas and override switch is captive key type).",
				        "test": "1. Automatic Time Switch Controls",
				        "step": "Step 2: Simulate unoccupied condition",
				        "itemno" : "b"
				    }, {
				        "name": "System returned to initial operating conditions",
				        "test": "1. Automatic Time Switch Controls",
				        "step": "Step 3: System returned to initial operating conditions",
				        "itemno" : "a"
				    }, {
				        "name": "Lights controlled by occupancy sensors turn off within a maximum of 30 minutes from start of an unoccupied condition per Standard Section 110.9(b)",
				        "test": "2. Occupancy Sensors",
				        "step": "Step 1: Simulate an unoccupied condition",
				        "itemno" : "a"
				    }, {
				        "name": "The occupant sensor does not trigger a false on from movement in an area adjacent to the controlled space or from HVAC operation",
				        "test": "2. Occupancy Sensors",
				        "step": "Step 1: Simulate an unoccupied condition",
				        "itemno" : "b"
				    }, {
				        "name": "Status indicator or annunciator operates correctly",
				        "test": "2. Occupancy Sensors",
				        "step": "Step 2: Simulate an occupied condition",
				        "itemno" : "a"
				    }, {
				        "name": "Lights controlled by occupancy sensors turn on immediately upon an occupied condition OR sensor indicates space is occupied and lights may be turned on manually",
				        "test": "2. Occupancy Sensors",
				        "step": "Step 2: Simulate an occupied condition",
				        "itemno" : "b"
				    }, {
				        "name": "System returned to initial operating conditions",
				        "test": "2. Occupancy Sensors",
				        "step": "Step 3: System returned to initial operating conditions",
				        "itemno" : "a"
				    },
				    {
				        "name": "Lights go to partial off state within a maximum of 30 minutes from start of an unoccupied condition per Standard Section 110.9(a)",
				        "test": "3. Partial Off Occupancy Sensor",
				        "step": "Step 1: Simulate an unoccupied condition",
				        "itemno" : "a"
				    },
				    {
				        "name": "The occupant sensor does not trigger a false \"on\" from movement in an area " +
				        		"adjacent to the controlled space or from HVAC operation. For library book stacks or " +
				        		"warehouse aisle, activity beyond the stack or aisle shall not activate the lighting in " +
				        		" the aisle or stack.",
				        "test": "3. Partial Off Occupancy Sensor",
				        "step": "Step 1: Simulate an unoccupied condition",
				        "itemno" : "b"
				    },
				    {
				        "name": "<p>In the partial off state, lighting shall consume no more than 50% of installed lighting<br />\
power, or:<br />\
&bull; No more than 60% of installed lighting power for metal halide or high\
pressure sodium lighting in warehouses.<br />\
&bull; No more than 60% of installed lighting power for corridors and stairwells in\
which the installed lighting power is 80 percent or less of the value allowed\
under the Area Category Method.<br />\
Light level may be used as a proxy for lighting power when measurements are taken</p>",
				        "test": "3. Partial Off Occupancy Sensor",
				        "step": "Step 1: Simulate an unoccupied condition",
				        "itemno" : "c"
				    },
				    {
				        "name": "The occupant sensing controls shall turn lights fully ON in each separately controlled " +
				        		"areas, Immediately upon an occupied condition",
				        "test": "3. Partial Off Occupancy Sensor",
				        "step": "Step 2: Simulate an occupied condition",
				        "itemno" : "a"
				    },
				    {
				        "name": "Immediately upon an occupied condition, the first stage activates between 30 to 70% of the lighting automatically.",
				        "test": "4. Partial On Occupancy Sensors",
				        "step": "Step 1. Simulate an occupied condition. Verify partial on operation.",
				        "itemno" : "a"
				    },
				    {
				        "name": "After the first stage occurs, manual switches allow an occupant to activate the" +
				        		" alternate set of lights, activate 100% of the lighting power, and manually deactivate	all of the lights.",
				        "test": "4. Partial On Occupancy Sensors",
				        "step": "Step 1. Simulate an occupied condition. Verify partial on operation.",
				        "itemno" : "b"
				    },
				    {
				        "name": "Both stages (automatic on and manual on) lights turn off within a maximum of 30 minutes from start of an unoccupied condition per Standard Section 110.9(a)",
				        "test": "4. Partial On Occupancy Sensors",
				        "step": "Step 2. Simulate an unoccupied",
				        "itemno" : "a"
				    },
				    {
				        "name": "The occupant sensor does not trigger a false \"on\" from movement in an area adjacent to the controlled space or from HVAC operation",
				        "test": "4. Partial On Occupancy Sensors",
				        "step": "Step 2. Simulate an unoccupied",
				        "itemno" : "b"
				    },
				    {
				        "name": "Area served by controlled lighting (square feet)",
				        "test": "5. Additional test for Occupancy Sensors Serving Small Zones in Office Spaces Larger than 250 Square Feet, to Qualify for a Power Adjustment Factor (PAF)",
				        "step": "Step 1. Verify area served and compare actual PAF with claimed PAF. Refer to Functional Test II.",
				        "itemno" : "a"
				    },
				    {
				        "name": "Enter PAF corresponding to controlled area from line (a) above (<125sf for PAF=0.4," +
				        		"126&dash;250sf for PAF=0.3, 251&dash;500sf for PAF=0.2).",
				        "test": "5. Additional test for Occupancy Sensors Serving Small Zones in Office Spaces Larger" +
				        		" than 250 Square Feet, to Qualify for a Power Adjustment Factor (PAF)",
				        "step": "Step 1. Verify area served and compare actual PAF with claimed PAF. Refer to Functional Test II.",
				        "itemno" : "b"
				    },
				    {
				        "name": "Enter PAF claimed for occupant sensor control in this space from the Certificate of Compliance",
				        "test": "5. Additional test for Occupancy Sensors Serving Small Zones in Office Spaces Larger than 250 Square Feet, to Qualify for a Power Adjustment Factor (PAF)",
				        "step": "Step 1. Verify area served and compare actual PAF with claimed PAF. Refer to Functional Test II.",
				        "itemno" : "c"
				    },
				    {
				        "name": "The PAF corresponding to the controlled area (line b), is less than or equal to the PAF claimed in the compliance documentation (line c)",
				        "test": "5. Additional test for Occupancy Sensors Serving Small Zones in Office Spaces Larger than 250 Square Feet, to Qualify for a Power Adjustment Factor (PAF)",
				        "step": "Step 1. Verify area served and compare actual PAF with claimed PAF. Refer to Functional Test II.",
				        "itemno" : "d"
				    },
				    {
				        "name": "Sensors shall not trigger in response to movement in adjacent walkways or workspaces.",
				        "test": "5. Additional test for Occupancy Sensors Serving Small Zones in Office Spaces Larger than 250 Square Feet, to Qualify for a Power Adjustment Factor (PAF)",
				        "step": "Step 1. Verify area served and compare actual PAF with claimed PAF. Refer to Functional Test II.",
				        "itemno" : "e"
				    },
				    {
				        "name": "All steps are conducted in Functional Test 2 \"Occupancy Sensor (On Off Control)\" and all answers are Yes (Y)",
				        "test": "5. Additional test for Occupancy Sensors Serving Small Zones in Office Spaces Larger than 250 Square Feet, to Qualify for a Power Adjustment Factor (PAF)",
				        "step": "Step 1. Verify area served and compare actual PAF with claimed PAF. Refer to Functional Test II.",
				        "itemno" : "f"
				    }
				]
		},
		{
			name : "#testResultTable",
			grdObj : null,
			parentGrid : null,
			noofdefaultcolumns : 1,
			colnames : ['Test Detail'],
			colmodels : [{
						    name: 'name',
						    classes: "stepdetailcol",
						    index: 'name',
						    editable: false,
						    sortable:false,
						    width: 400
						}],
			spaceRowMap : new Object(),
			spaceNo : 0,
			data: 			
					[{
				        "name": "<p><strong>I Automatic Time Switch Controls </strong>(all answers must be Y).</p>" 
				    },
				    {
				        "name": "<p><strong>II Occupancy Sensor (On Off Control)</strong> (all answers must be Y).</p>" 
				    },
				    {
				        "name": "<p><strong>III Partial Off Occupancy Sensor</strong> (all answers must be Y). For warehouses, " +
				        		"library book stacks, corridors, stairwells in nonresidential buildings must also be " +
				        		"accompanied by passing Test I or Test II.</p>" 
				    },
				    {
				        "name": "<p><strong>IV Partial On Occupant Sensor for PAF </strong>(all answers must be Y).</p>" 
				    },
				    {
				        "name": "<p><strong>V Occupant Sensor serving small zones for PAF </strong>(all answers must be Y). Also must pass Test II</p>" 
				    }
				    
				]
		
		}
];


/**
 * Array that has to be configured for jqgrid in Functional Tests table that will have rows with differet data type apart from select box
 */
var gridTimSwitchControlTypeMap = new Object();
gridTimSwitchControlTypeMap[21] = $.parseJSON('{"type":"text"}');
gridTimSwitchControlTypeMap[19] = $.parseJSON('{"type":"text"}');
gridTimSwitchControlTypeMap[20] = $.parseJSON('{"type":"text"}');

function saveRow(grid, idToSave) {
    var selectedRowId = grid.jqGrid('getGridParam', 'selrow');
    if (idToSave != null && idToSave != 'undefined') {
        selectedRowId = idToSave;
    }
    if (selectedRowId == null || selectedRowId == 'undefined') {
        alert('Please select the row to save');
        return false;
    }

    grid.jqGrid('saveRow', selectedRowId, false, 'clientArray');
    if (idToSave != null && idToSave != 'undefined') {
        $("#spaceDetailTableSave").attr("disabled", false);
    } else {
        $("#spaceDetailTableSave").attr("disabled", true);
    }
    $("#spaceDetailTableEdit").attr("disabled", false);
    $("#spaceDetailTableAdd").attr("disabled", false);
    return true;
}

function dependentGridsAfterParentRowDelete(spaceNo){
	for (var x =0 ; x < dependentGridsDetails.length; x++){
		var gridArrObj = dependentGridsDetails[x];
		var record = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	    var rowData = gridArrObj.parentGrid.jqGrid('getRowData');
	    for (var i = 0; i < record; i++) {
	        var spaceN = rowData[i].no;
	        gridArrObj.spaceRowMap[spaceN] = i + gridArrObj.noofdefaultcolumns;
	    }
	    var selectedRowId = gridArrObj.spaceRowMap[spaceNo];
	    for (var i = gridArrObj.colnames.length - 1; i >= 0; i--) {
	        if (i == selectedRowId) {
	        	gridArrObj.colnames.splice(i, 1);
	        	gridArrObj.colmodels.splice(i, 1);
	        }
	    }
	    console.log("For Grid "+ x +" Colnames are "+ JSON.stringify(gridArrObj.colnames));
	    saveAllRowsInGrid(gridArrObj.grdObj);
	    gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	}
}
function delRowSpaceDetail(parentTableRowId, spaceNo) {
	 dependentGridsDetails[0].parentGrid.jqGrid('delRowData', parentTableRowId);
	    dependentGridsDetails[0].parentGrid.trigger('reloadGrid');
    dependentGridsAfterParentRowDelete(spaceNo);
    dependentGridsRedraw();
    $("#spaceDetailTableAdd").attr("disabled", false);
    $("#spaceDetailTableSave").attr("disabled", true);
}

function dependentGridsInitiate(){
	for (var i = 0 ; i < dependentGridsDetails.length; i++){
		var gridArrObj =dependentGridsDetails[i]; 
		gridArrObj.grdObj = $(gridArrObj.name);
	}
}

function dependentGridsSetParentGrid(grid){
	for (var i = 0 ; i < dependentGridsDetails.length; i++){
		var gridArrObj =dependentGridsDetails[i]; 
		gridArrObj.parentGrid = grid;
	}
}
function renderSpaceDetailGrid() {
    var lastsel2;
    $("#del_spaceDetailTable").click(function() {
        alert('Deleting');
    });
    dependentGridsInitiate();
    var grid = $("#spaceDetailTable"),
    mydata = $.parseJSON($("#lightcontrolaccepatancespacedetaildata").val());
    dependentGridsSetParentGrid(grid);
    grid.jqGrid({
        datatype: "local",
        data: mydata,
        colNames: ['Id', 'Space No', 'Space Name', 'Space Type', 'Untested Areas', 'Actions'],
        colModel: [{
                name: 'id',
                index: 'id',
                key: true,
                hidden: true,
                width: 70,
                sorttype: "int"
            }, {
                name: 'no',
                index: 'invdate',
                width: 90,
                sorttype: "int"
            }, {
                name: 'name',
                index: 'name',
                width: 100,
                editable: true,
                width: '200'
            }, {
                name: 'type',
                index: 'amount',
                width: 80,
                align: "right",
                sorttype: "text",
                editable: true,
                width: '200'
            }, {
                name: 'untested',
                index: 'tax',
                width: 80,
                align: "right",
                sorttype: "text",
                editable: true,
                width: '300'
            }, {
                name: 'actions',
                index: 'tax',
                width: 80,
                align: "left",
                editable: false,
                width: '50',
                formatter: function(cellvalue, options, rowObject) {
                    return "<input type='button' value='Delete' onclick='delRowSpaceDetail(" + options.rowId + ", " + rowObject.no + ")'\>";
                }
            }


        ],
        onSelectRow: function(id) {
            if (id && id != lastsel2) {
                lastsel2 = id;
            }
            saveRow(grid, lastsel2);
            grid.jqGrid('editRow', id, true);
        },
        cellattr: function(rowId, tv, rawObject, cm, rdata) {
            return 'style="white-space: normal!important"';
        },
        search: true,
        pager: null,
        jsonReader: {
            cell: ""
        },
        rowNum: 100000,
        editurl: 'clientArray',
        rowList: [100000],
        sortname: 'id',
        sortorder: 'asc',
        viewrecords: true,
        height: "100%",
        caption: '<p><span style="font-size:16px"><strong>B. Functional Testing of Lighting Controls</strong></span><br />\
        	<strong><span style="font-size:12px">Representative Spaces Selected</span></strong><br /><br />\
        	<span style="font-size:10px">For every space in the building, conduct functional tests I through V below if applicable. If there are several geometrically similar&nbsp;spaces that use the same lighting controls, test only one space and list in the cells below which &ldquo;untested spaces&rdquo; are&nbsp;represented by that tested space.<br />\
        	EXCEPTION: For buildings with up to seven (7) occupancy sensors, all occupancy sensors shall be tested. (NA7.6.2.3)</span></p>'
    });
//    grid.jqGrid('navGrid', '#spaceDetailDiv', {
//        add: false,
//        edit: false,
//        del: false,
//        search: false,
//        refresh: false
//    }, {}, {}, {}, {
//        multipleSearch: true,
//        multipleGroup: true,
//        showQuery: true
//    });

    dependentGridsPopulateFromDB();
    dependentGridsRedraw();
    
    $("#spaceDetailTableEdit").click(function() {
        var selectedRowId = grid.jqGrid('getGridParam', 'selrow');

        if (selectedRowId == null || selectedRowId == 'undefined') {
            alert('Please select the row to edit');
            return true;
        }
        grid.jqGrid('editRow', selectedRowId);
        this.disabled = 'true';
        jQuery("#spaceDetailTableSave").attr("disabled", false);
        $("#spaceDetailTableAdd").attr("disabled", true);
    });


    function checksave(result) {
        if (result.responseText == "") {
            alert("Update is missing!");
            return false;
        }
        return true;
    }
    $("#spaceDetailTableSave").click(function() {
    	//$("#spaceDetailTableAdd").attr("disabled", false);
    	return saveRow(grid);
    });

    $("#spaceDetailTableAdd").click(function() {
        var record = grid.jqGrid('getGridParam', 'records');
        var newrowid = record + 1;
        dependentGridsSpaceRowAfterParentRowAdded(newrowid);
        var addNewparameters = {
            rowID: dependentGridsDetails[0].spaceNo,
            initdata: {
                id: dependentGridsDetails[0].spaceNo,
                no: dependentGridsDetails[0].spaceNo,
                name: '',
                type: '',
                untested: 'N.A.'
            },
            // position :"afterSelected",
            position: "last",
            useDefValues: false,
            useFormatter: false,
            addRowParams: {
                extraparam: {}
            }
        }
        grid.jqGrid('addRow', addNewparameters);
        $("#spaceDetailTableSave").attr("disabled", false);
        $("#spaceDetailTableEdit").attr("disabled", true);
        $("#spaceDetailTableAdd").attr("disabled", true);
        dependentGridsAfterParentRowAdded();
        dependentGridsRedraw();
        
    });
    //renderSensorControlGrid();
    renderAutoDLControlSystem(null);
    
}
function dependentGridsSpaceRowAfterParentRowAdded(newrowid){
	for (var i = 0 ; i < dependentGridsDetails.length; i++){
		var gridArrObj =dependentGridsDetails[i]; 
		gridArrObj.spaceNo = gridArrObj.spaceNo + 1;
		gridArrObj.spaceRowMap[gridArrObj.spaceNo] = newrowid + (gridArrObj.noofdefaultcolumns - 1);
	}
}

function dependentGridsAfterParentRowAdded(){
	/**
	 * STARTED : DEPENDENT GRID 1
	 */
	var gridArrObj = dependentGridsDetails[0];
	var newrowid = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	gridArrObj.colnames.push( gridArrObj.spaceNo);
	gridArrObj.colmodels.push({
        name: 'space' + newrowid,
        index: 'name' + newrowid,
        width: 50,
        sortable: false,
        edittype: 'select',
        editable: true,
        editoptions: {
            value: ":;N:N;Y:Y"
            //,defaultValue: "N"
        }
    });
	saveAllRowsInGrid(gridArrObj.grdObj);
    gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
    /**
	 * ENDED : DEPENDENT GRID 1
	 */
    gridArrObj = dependentGridsDetails[1];
	var newrowid = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	gridArrObj.colnames.push("Pass /<br />Fail");
	gridArrObj.colmodels.push({
        name: 'space' + newrowid,
        index: 'name' + newrowid,
        width: 50,
        edittype: 'select',
        editable: true,
        sortable: false,
        editoptions: {
            value: "PASS:PASS;FAIL:FAIL"
            //,defaultValue: "N"
        }
    });
    saveAllRowsInGrid(gridArrObj.grdObj);
    gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
    /**
	 * ENDED : DEPENDENT GRID 2
	 */
}

function dependentGridsPopulateFromDB(){
	//If page is rendered from db data the need to update the spaceRowMap
	/**
	 * START:::Started for Table Functional Tests here
	 */
	var gridArrObj = dependentGridsDetails[0];
    var recordPop = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
    var rowDataPop = gridArrObj.parentGrid.jqGrid('getRowData');
    for (var i = 0; i < recordPop; i++) {
    	var spaceN = rowDataPop[i].no;
    	if (gridArrObj.spaceNo < spaceN){
    		gridArrObj.spaceNo = parseInt(spaceN);
    	}
    	gridArrObj.spaceRowMap[spaceN] = i + gridArrObj.noofdefaultcolumns;
    	gridArrObj.colnames.push('Space ' + spaceN);
    	gridArrObj.colmodels.push({
            name: 'space' + (i+1),
            index: 'name' + (i+1),
            width: 50,
            edittype: 'select',
            editable: true,
            sortable: false,
            editoptions: {
                value: ":;N:N;Y:Y"
            }
        });
    }
    recordPop = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
    rowDataPop = gridArrObj.grdObj.jqGrid('getRowData');
    var tempdata1 = $("#lightcontrolaccepatancefunctionaltestdata").val();
    if(!tempdata1.isCustomEmpty()){
    	var d1=$.parseJSON(tempdata1);
    	if (Object.keys(d1).length > 0){
    		gridArrObj.data = d1;
    	}
    }
    /**
	 * END:::Endeded for Table Functional Tests here
	 */
    
    /**
	 * START:::Started for Table Testing Results here
	 */
	gridArrObj = dependentGridsDetails[1];
    recordPop = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
    rowDataPop = gridArrObj.parentGrid.jqGrid('getRowData');
    for (var i = 0; i < recordPop; i++) {
    	var spaceN = rowDataPop[i].no;
    	if (gridArrObj.spaceNo < spaceN){
    		gridArrObj.spaceNo = parseInt(spaceN);
    	}
    	gridArrObj.spaceRowMap[spaceN] = i + gridArrObj.noofdefaultcolumns;
    	gridArrObj.colnames.push('Space ' + spaceN);
    	gridArrObj.colmodels.push({
            name: 'space' + (i+1),
            index: 'name' + (i+1),
            width: 50,
            edittype: 'select',
            sortable: false,
            editable: true,
            editoptions: {
                value: ":;N:N;Y:Y"
            }
        });
    }
    recordPop = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
    rowDataPop = gridArrObj.grdObj.jqGrid('getRowData');
    tempdata1 = $("#lightcontrolaccepatanceresultdata").val();
    if(!tempdata1.isCustomEmpty()){
    	var d1=$.parseJSON(tempdata1);
    	if (Object.keys(d1).length > 0){
    		gridArrObj.data = d1;
    	}
    }
    /**
	 * END:::Endeded for Table Testing Results here
	 */
}

function saveRowAutoTimeSwitchControl(idToSave) {
    var selectedRowId = dependentGridsDetails[0].grdObj.jqGrid('getGridParam', 'selrow');
    if (idToSave != null && idToSave != 'undefined') {
        selectedRowId = idToSave;
    }
    if (!isRowEdited(dependentGridsDetails[0].grdObj, selectedRowId)) {
        console.log('Row ' + selectedRowId + ' not edited');
        return;
    }
    var rowData = dependentGridsDetails[0].grdObj.getRowData(selectedRowId);

    dependentGridsDetails[0].data = dependentGridsDetails[0].grdObj.jqGrid('getRowData');
    rowData = dependentGridsDetails[0].data[selectedRowId];

    console.log('saving ' + selectedRowId + ' and rowdata ' + JSON.stringify(rowData));
    if (selectedRowId == null || selectedRowId == 'undefined') {
        alert('Please select the row to save');
        return false;
    }
    dependentGridsDetails[0].grdObj.jqGrid('saveRow', selectedRowId, false, 'clientArray');

    dependentGridsDetails[0].data = dependentGridsDetails[0].grdObj.jqGrid('getRowData');

    console.log("AFTER SAVE DATA: " + JSON.stringify(dependentGridsDetails[0].data[selectedRowId]));

    reloadTestResultData();
    return true;
}


function dependentGridsRedraw() {
	drawFunctionalTestGrid();
	drawTestResultGrid();
}

function reloadTestResultData(){
	var gridArrObj = dependentGridsDetails[1]; 
	//Update the pass/fail results in the grids data now dynamically
    //Iterate on SubGrid 1 (viz Functional Test ) and then check whether all answers are Y.
	var parentGridRecords = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	var subgrid1 = dependentGridsDetails[0];
	var subgridgroupsdata = $(dependentGridsDetails[0].name).jqGrid("getGridParam", "groupingView").groups;
	var maingroupinfo = (subgridgroupsdata).filter(function (e){
        return e.idx == 0;
    });
	//Filter the subgroid groups data and find out row numbers 
	console.log("Test Result data is "+ JSON.stringify(gridArrObj.data));
	var rowN = -1;
	for (var i = 0; i < parentGridRecords; i++){
		var subgridrownum = 0;
		var lastrow = 0;
		for (var j = 0; j < maingroupinfo.length; j++ ){  
			var startRow = 0;//maingroupinfo[j].startRow; //Above group should pass to make pass the lower group.//Remove lastrow as well if dont not want
			lastrow = lastrow + maingroupinfo[j].cnt;
			for (var cnt =0; cnt < lastrow; cnt++){
    			var colname = 'space'+ (i+1);
    			rowN = (startRow  +  cnt);
    			var val = subgrid1.data[startRow  +  cnt][colname];
    			console.log("subgrid1.data["+(rowN)+"]["+colname+"]"+'Value is :'+ val+":"+rowN);
    			if ( rowN > 17 && rowN < 21){
    				gridArrObj.data[j][colname] = 'PASS';
    			}else{
	    			if (val != undefined && !val.isCustomEmpty() && val == 'Y'){
	    				gridArrObj.data[j][colname] = 'PASS';
	    			}else{
	    				gridArrObj.data[j][colname] = 'FAIL';
	    				break;
	    			}
    			}
			}
		}
	}
	console.log("Test Result data after updates is "+ JSON.stringify(gridArrObj.data));
	gridArrObj.grdObj.trigger("reloadGrid");

}
function drawTestResultGrid() {
	var gridArrObj = dependentGridsDetails[1]; 
	var grid = gridArrObj.grdObj;
	var gridname = gridArrObj.name;
    var record = grid.jqGrid('getGridParam', 'records');
    if (record != null && record != 'undefined' && record > 0) {
        $.jgrid.gridUnload(gridname);
        grid = $(gridname);
        gridArrObj.grdObj = grid;
        reloadTestResultData();
    }
    var lastsel2;
    grid.jqGrid({
        datatype: "local",
        data: gridArrObj.data,
        colNames: gridArrObj.colnames,
        colModel: gridArrObj.colmodels,
        editurl: 'clientArray',
        loadonce: false,
        cellattr: function(rowId, tv, rawObject, cm, rdata) {
            return 'style="white-space: normal!important"';
        },
        onSelectRow: function(id) {
            if (id && id != lastsel2) {
                lastsel2 = id;
            }
            //saveAllRowsInGrid(grid);
            //grid.jqGrid('editRow', id, true);
        },
        gridComplete: function() {

        },
        search: true,
        jsonReader: {
            cell: ""
        },
        sortname: 'id',
//        grouping: true,
//        groupingView: {
//            groupField: ['test', 'step'],
//            groupColumnShow: [false, false]
//        },
        sortorder: 'asc',
        viewrecords: true,
        height: "100%",
        caption: '<p><span style="font-size:16px"><strong>C.&nbsp;Testing Results</strong></span><br /> &nbsp;</p'
    });
}


function drawFunctionalTestGrid() {
	var gridArrObj = dependentGridsDetails[0];
	var parentRecords = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	
    var record = dependentGridsDetails[0].grdObj.jqGrid('getGridParam', 'records');
    var grid = dependentGridsDetails[0].grdObj;
    if (record != null && record != 'undefined' && record > 0) {
        $.jgrid.gridUnload(dependentGridsDetails[0].name);
        grid = $(dependentGridsDetails[0].name);
        dependentGridsDetails[0].grdObj = grid;
    }
    var lastsel2;
    grid.jqGrid({
        datatype: "local",
        data: dependentGridsDetails[0].data,
        colNames: dependentGridsDetails[0].colnames,
        colModel: dependentGridsDetails[0].colmodels,
        editurl: 'clientArray',
        cellattr: function(rowId, tv, rawObject, cm, rdata) {
            return 'style="white-space: normal!important"';
        },
        onSelectRow: function(id) {
            if (id && id != lastsel2) {

                if (lastsel2 != null && lastsel2 != 'undefined') {
                    var rowDataSave = grid.getRowData(lastsel2);
                    dependentGridsDetails[0].data = grid.jqGrid('getRowData');
                    rowDataSave = dependentGridsDetails[0].data[lastsel2];
                    console.log('Last SelId ' + lastsel2 + ' Current SelId ' + id + ' Saving Row ' + JSON.stringify(rowDataSave));
                    saveRowAutoTimeSwitchControl(lastsel2);
                    grid.jqGrid('restoreRow', lastsel2);
                }
                var defaulttype = true;
                var ref = (gridTimSwitchControlTypeMap[id]);
                if (ref != null && ref != 'undefined') {
                    if (ref.type == 'text') {
                        defaulttype = false;
                    }
                }
                var colModels = grid.jqGrid("getGridParam", "colModel");
                for (var i = (dependentGridsDetails[0].noofdefaultcolumns); i < colModels.length; i++) {
                    var cm = colModels[i];
                    if (!defaulttype) {
                        cm.edittype = 'text';
                        cm.editoptions = null;

                    } else {
                        cm.edittype = 'select';
                        cm.editoptions = {
                            value: ":;N:N;Y:Y"
                            //,defaultValue: "N"
                        };
                    }
                }
                grid.jqGrid('editRow', id, true);
                lastsel2 = id;
            }
        },
        loadComplete: function() {
            //Add css to groups
            var i, group, cssClass, headerIdPrefix = this.id + "ghead_",
                groups = $(this).jqGrid("getGridParam", "groupingView").groups,
                l = groups.length;
            for (i = 0; i < l; i++) {
                group = groups[i];
                switch (group.dataIndex) {
                    case "test":
                        cssClass = "maingroup";
                        break;
                    case "step":
                        cssClass = "subgroup";
                        break;
                    case "test3":
                        cssClass = "class3";
                        break;
                    default:
                        cssClass = "";
                        break;
                }
                if (cssClass != "") {
                    $("#" + headerIdPrefix + group.idx + "_" + i).addClass(cssClass);
                }
            }

        },
        search: true,
        jsonReader: {
            cell: ""
        },
        sortable: false,
        sortname: 'id',
        grouping: true,
        groupingView: {
            groupField: ['test', 'step'],
            groupColumnShow: [false, false]
        },
        sortorder: 'asc',
        viewrecords: true,
        height: "100%",
        rowNum: 50000,
        caption: "Functional Tests"
    });
    
    gridArrObj.grdObj.jqGrid('setGroupHeaders', {
  	  useColSpanStyle: true,   
  	  groupHeaders:[{startColumnName: 'itemno', numberOfColumns: 2, titleText: '<p style="text-align: center;"><strong>Functional Tests</strong></p>\
  		<p style="text-align: center;">Confirm compliance (Y/N) for all control system types (I&dash;V) present in each space:</p>'}]
  	});
    
    gridArrObj.grdObj.jqGrid('setGroupHeaders', {
  	  useColSpanStyle: true,   
  	  groupHeaders:[{startColumnName: 'space'+1, numberOfColumns: parentRecords, titleText: 'Tested Space Number'}]
  	});
}

/***
	Network SEttings grid

**/
var interfacegrid;
var interfaceDetailGrid;
var profilesGrid;
$(document).ready(function() { 

	registerClickEvents();
	
    //renderInterfaceTable();
    //renderInterfaceDetail();
    //renderProfilesTable();
});


function genericSaveTitle24UI(){
	 $("#lightcontrolaccepatancesubmitflag").val('true');
     var grd = jQuery("#spaceDetailTable");
     saveAllRowsInGrid(grd);
     var grdjsonstr = JSON.stringify(grd.jqGrid('getRowData'));
     $("#lightcontrolaccepatancespacedetaildata").val(grdjsonstr);
     
     dependentGridsSaveAfterSubmit();
     var grd = jQuery(getDRParentGrid()[0].name);
     saveAllRowsInGrid(grd);
     var grdjsonstr = JSON.stringify(grd.jqGrid('getRowData'));
     $("#drspacegriddata").val(grdjsonstr);
     dependentDRGridsSaveAfterSubmit();
     
     var grd = jQuery(autoDayLightControlSystemGrid[0].name);
     saveAllRowsInGrid(grd);
     var grdjsonstr = JSON.stringify(grd.jqGrid('getRowData'));
     console.log("Value JSON AutoDL::"+grdjsonstr);
     $("#autodlcontrolsystem").val(grdjsonstr);
     dependentAutoDLGridsSaveAfterSubmit();
     
     $("#systestForm").submit();
}

function registerClickEvents(){
	$("#accordianLightControlSubmit").click(function() {
		$("#formtype").val("accordianLightControlSubmit");
		genericSaveTitle24UI();
    });
	
	$("#accordianDRControlSubmit").click(function() {
		$("#formtype").val("accordianDRControlSubmit");
		genericSaveTitle24UI();
    });
	$("#accordianOLCControlSubmit").click(function() {
		$("#formtype").val("accordianOLCControlSubmit");
		genericSaveTitle24UI();
    });
	$("#accordianAutoDayLightControlSubmit").click(function() {
		$("#formtype").val("accordianAutoDayLightControlSubmit");
		genericSaveTitle24UI();
    });
}

function registerClickEvents_BKP(){
	$("#accordianLightControlSubmit").click(function() {
        $("#lightcontrolaccepatancesubmitflag").val('true');
        var grd = jQuery("#spaceDetailTable");
        saveAllRowsInGrid(grd);
        var grdjsonstr = JSON.stringify(grd.jqGrid('getRowData'));
        $("#lightcontrolaccepatancespacedetaildata").val(grdjsonstr);
        
        dependentGridsSaveAfterSubmit();
        
        $("#systestForm").submit();
    });
	
	$("#accordianDRControlSubmit").click(function() {
        //$("#lightcontrolaccepatancesubmitflag").val('true');
        var grd = jQuery(getDRParentGrid()[0].name);
        saveAllRowsInGrid(grd);
        var grdjsonstr = JSON.stringify(grd.jqGrid('getRowData'));
        $("#drspacegriddata").val(grdjsonstr);
        dependentDRGridsSaveAfterSubmit();
        
        $("#systestForm").submit();
    });
	$("#accordianOLCControlSubmit").click(function() {
        $("#systestForm").submit();
    });
	$("#accordianAutoDayLightControlSubmit").click(function() {
        //$("#lightcontrolaccepatancesubmitflag").val('true');
        var grd = jQuery(autoDayLightControlSystemGrid.name);
        saveAllRowsInGrid(grd);
        var grdjsonstr = JSON.stringify(grd.jqGrid('getRowData'));
        $("#autodlcontrolsystem").val(grdjsonstr);
        dependentAutoDLGridsSaveAfterSubmit();
        
        $("#systestForm").submit();
    });
}
function dependentGridsSaveAfterSubmit(){
   var gridArrObj = dependentGridsDetails[0];
   saveAllRowsInGrid(gridArrObj.grdObj);
   grdjsonstr = JSON.stringify(gridArrObj.grdObj.jqGrid('getRowData'));
   console.log("functional test data:"+grdjsonstr);
   $("#lightcontrolaccepatancefunctionaltestdata").val(grdjsonstr);
   gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
   
   //IF the upper grid is edited during submit then the lower test result grid needs to be redraw the latest value.
   reloadTestResultData();
   gridArrObj = dependentGridsDetails[1];
   gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
   $("#lightcontrolaccepatanceresultdata").val(JSON.stringify(gridArrObj.data ));
}


function renderProfilesTable() {

    profilesGrid = $("#profiles");


    var colnameprofiles = [ 'Profile Name', '','Created From', 'Morning', 'Day', 'Evening', 'Night',
        'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'
    ];
    var colmodelsprofiles = [

        
        {
            name: 'name',
            index: 'name',
            editable: true,
            width: 155
        }, 
        {
            name: 'blank1',
            index: 'blank1',
            editable: true,
            align:'right',
            hidden:true,
            width: 155
        },
        {
            name: 'parent',
            index: 'parent',
            editable: true,
            width: 100
        }, {
            name: 'mor',
            index: 'mor',
            editable: true,
            width: 55
        }, {
            name: 'day',
            index: 'day',
            editable: true,
            width: 55
        }, {
            name: 'eve',
            index: 'eve',
            editable: true,
            width: 55
        }, {
            name: 'ngt',
            index: 'ngt',
            editable: true,
            width: 55
        }, {
            name: 'mon',
            index: 'mon',
            editable: true,
            width: 30
        }, {
            name: 'tue',
            index: 'tue',
            editable: true,
            width: 30
        }, {
            name: 'wed',
            index: 'wed',
            editable: true,
            width: 33
        }, {
            name: 'thu',
            index: 'thu',
            editable: true,
            width: 30
        }, {
            name: 'fri',
            index: 'fri',
            editable: true,
            width: 30
        }, {
            name: 'sat',
            index: 'sat',
            editable: true,
            width: 30
        }, {
            name: 'sun',
            index: 'sun',
            editable: true,
            width: 30
        }

    ];

    var dataprofiles = [
        //{"name":"","parent":"","mor":"","day":"","eve":"","ngt":"","mon":"","tue":"","wed":"","thu":"","fri":"","sat":"","sun":""}
        {
            "name": "Breakroom_Default",
            "blank1":"Click here details&nbsp;&nbsp;&nbsp;",
            "parent": "Default",
            "mor": "6:00 AM",
            "day": "9:00 AM",
            "eve": "6:00 PM",
            "ngt": "9:00 PM",
            "mon": "Yes",
            "tue": "Yes",
            "wed": "Yes",
            "thu": "Yes",
            "fri": "Yes",
            "sat": "No",
            "sun": "No",
            "dropPercent" : "", "risePercent": "","intensityNormTime": "",
            	"dimBackoffTime": "", "minLevelBeforeOff": "", "toOffLinger": "",
      		   	"initialOnLevel": "", "initialOnTime": "", "isHighBay": "",
            	"motionThresholdGain": "", "daylightHarvestingInputCheckbox": "", "daylightProfileBelowMinCheckBox": "",
            	"daylightForceProfileMinValueCheckBox": "","holidayLevel": "",
            	"drReactivity": "", "drLowLevel": "", "drModerateLevel": "", "drHighLevel": "", "drSpecialLevel": "",
            	"darkLux": "", "neighborLux": "", "envelopeOnLevel": ""
        }
    ];
    
     var mySubgrids= {
        Breakroom_Default: [
            // data for subgrid for the id=Breakroom_Default
            {period: "Weekday Settings", name:"Morning", minlightlevel: "0", maxlightlevel: "75"},
            {period: "Weekday Settings", name:"Day", minlightlevel: "0", maxlightlevel: "75"},
            {period: "Weekday Settings", name:"Evening", minlightlevel: "0", maxlightlevel: "75"},
            {period: "Weekday Settings", name:"Night", minlightlevel: "0", maxlightlevel: "75"},
            
            {period: "Weekend Settings", name:"Morning", minlightlevel: "0", maxlightlevel: "75"},
            {period: "Weekend Settings", name:"Day", minlightlevel: "0", maxlightlevel: "75"},
            {period: "Weekend Settings", name:"Evening", minlightlevel: "0", maxlightlevel: "75"},
            {period: "Weekend Settings", name:"Night", minlightlevel: "0", maxlightlevel: "75"}

        ]
    };
    var lastsel2;
    profilesGrid.jqGrid({
        datatype: "local",
        data: dataprofiles,
        colNames: colnameprofiles,
        colModel: colmodelsprofiles,
        editurl: 'clientArray',
        cellattr: function(rowId, tv, rawObject, cm, rdata) {
            return 'style="white-space: normal!important"';
        },
        onSelectRow: function(id) {
			$(this).jqGrid("toggleSubGridRow", id);
        },
        loadComplete: function() {

        },
        search: false,
        jsonReader: {
            cell: ""
        },
        sortname: 'id',
        pager: '#profilesPager',
        grouping: false,
        groupingView: {
            groupField: ['name'],
            groupColumnShow: [false]
        },
        sortorder: 'asc',
        viewrecords: true,
        height: "100%",
        width: "100%",
        //shrinkToFit : false,
        //autowidth : true,
        caption: "Profile Details",
        shrinkToFit: false,
        subGrid: true,
        //subGridOptions: { openicon: "ui-helper-hidden" },
        ondblClickRow: function (rowid) {
		    $(this).jqGrid("toggleSubGridRow", rowid);
		},
    	subGridRowExpanded: function (subgridDivId, rowId) {
	        var subgridTableId = subgridDivId + "_t";
	        $("#" + subgridDivId).html("<table id='" + subgridTableId + "'></table>");
			
			//$("#" + subgridDivId).html($("#" + subgridDivId).html() + "<br/> Advanced <br/>");
			var cr = dataprofiles[rowId -1];
	        var crow = mySubgrids[cr.name];// dataprofiles[rowId -1];
	        $("#" + subgridTableId).jqGrid({
	            datatype: 'local',
	            data:  crow ,//mySubgrids[dataprofiles[rowId -1].name],
	            colNames: ['Period', 'Name', 'Min light level when on (0-100)(%)', 'Max light level when on (0-100)(%)',
	            			'Ramp-up time (0-10)(sec)','Active motion window (1-200)(min)','Motion sensitivity (0-10)',
	            			'Ambient Sensitivity (0-10)',],
	            colModel: [
	                { name: 'period', width: 80 },
	                { name: 'name', width: 80 },
	                { name: 'minlightlevel', width: 80 },
	                { name: 'maxlightlevel', width: 80 },
	                { name: 'rampup', width: 80 },
	                { name: 'activemotionwindow', width: 80 },
	                { name: 'motionsensitivity', width: 80 },
	                { name: 'ambientsensivity', width: 80 },
	            ],
	            grouping: true,
		        groupingView: {
		            groupField: ['period'],
		            groupColumnShow: [false]
		        }
		        //,caption: dataprofiles[rowId -1].name+" Details"
	
	        });
	        
//	        $("#" + subgridDivId).html();
			
			var htmlToAppend = "<br/> "+" <div id='general'> General Behaviour" + "</div>";
			htmlToAppend += "<div id='generaldetail' >";
			htmlToAppend += "Adjust fixture light levels when ambient light levels drop by  "+cr.dropPercent+" (%) or rise by  "+cr.risePercent+"(%).";
			htmlToAppend += "</div>";
			
			htmlToAppend += "<div id='generaldetail' >";
			htmlToAppend += "Only change fixture light level after change in ambient light level is stable for "+cr.intensityNormTime+"  seconds.";
			htmlToAppend += "</div>";

			htmlToAppend += "<div id='generaldetail' >";
			htmlToAppend += "Change fixture light level due to change in ambient light level no more than once every "+cr.dimBackoffTime+" minutes.";
			htmlToAppend += "</div>";
			
			htmlToAppend += "<div id='generaldetail' >";
			htmlToAppend += "Dim and linger to "+cr.minLevelBeforeOff+" (%) for "+cr.toOffLinger+" seconds before turning off fixture light.";
			htmlToAppend += "</div>";

			htmlToAppend += "<div id='generaldetail' >";
			htmlToAppend += "Fixture initially turns on at "+cr.initialOnLevel+" (%) for "+cr.initialOnTime+" seconds.";
			htmlToAppend += "</div>";

			htmlToAppend += "<div id='generaldetail' >";
			htmlToAppend += "Highbay (0 = disabled, 1 = enabled) "+cr.isHighBay;
			htmlToAppend += "</div>";

			htmlToAppend += "<div id='generaldetail' >";
			htmlToAppend += "Motion threshold (1 = Minimum threshold, 10 = Maximum threshold) "+cr.motionThresholdGain;
			htmlToAppend += "</div>";
			
			htmlToAppend += "<div id='generaldetail' >";
			htmlToAppend += "Enable Daylight Harvesting in Override Mode <input id='daylightHarvestingInputCheckbox' type='checkbox' disabled='' readonly='readonly' "+cr.daylightHarvestingInputCheckbox+" >";
			htmlToAppend += "</div>";

			htmlToAppend += "<div id='generaldetail' >";
			htmlToAppend += "When min light level is greater than zero, allow daylight harvesting to turn off light <input id=\"daylightProfileBelowMinCheckBox\" "+cr.daylightProfileBelowMinCheckBox+" type=\"checkbox\" disabled=\"\" readonly=\"readonly\" value=\"0\">";
			htmlToAppend += "</div>";

			htmlToAppend += "<div id='generaldetail' >";
			htmlToAppend += "When min light level is greater than zero, initialize light to min level at 1) start of period when there is no motion, or 2) after daylight harvesting is no longer applicable";
			htmlToAppend += "<input id=\"daylightForceProfileMinValueCheckBox\" type=\"checkbox\" disabled=\"\" readonly=\"readonly\" "+cr.daylightForceProfileMinValueCheckBox+">";
			htmlToAppend += "</div>";

			htmlToAppend += "<div id='generaldetail' >";
			htmlToAppend += "Holiday Override Profile "+cr.holidayLevel;
			htmlToAppend += "</div>";

		
			htmlToAppend += " <div id='drbhaviour'> Demand Response Behavior Version 2.2.X and Below" + "</div>";
			htmlToAppend += " <div id='drbhaviour2.2.X'> Version 2.2.X and Below" + "</div>";
			htmlToAppend += "<div id='drbhaviourdetail2.2.X' >";
			htmlToAppend += "Response level to a DR event "+cr.drReactivity+" (Hint: 0 is no response and 10 is most aggressive response.)";
			htmlToAppend += "</div>";

			htmlToAppend += "<br/> "+" <div id='drbhaviour2.3'> Version 2.3 and Above" + "</div>";
			htmlToAppend += "<div id='drbhaviourdetail2.3' >  DR Level	:	Selected Override Profile </div>";
			htmlToAppend += "<div id='drbhaviourdetail2.3' >  Low		:	"+cr.drLowLevel+" </div>";
			htmlToAppend += "<div id='drbhaviourdetail2.3' >  Moderate	:	"+cr.drModerateLevel+"</div>";
			htmlToAppend += "<div id='drbhaviourdetail2.3' >  High	  	:	"+cr.drHighLevel+"</div>";
			htmlToAppend += "<div id='drbhaviourdetail2.3' >  Special	:	"+cr.drSpecialLevel+"</div>";


			htmlToAppend += " <div id='envbhaviour'> Envelope Behavior" + "</div>";
			htmlToAppend += "<div id='envbhaviourdetail' >";
			htmlToAppend += "Enable when ambient light is less than "+cr.darkLux+" (lux) and the neighbor affects ambient light level by "+cr.neighborLux+" (lux).";
			htmlToAppend += "</div>";

			htmlToAppend += "<div id='envbhaviourdetail' >";
			htmlToAppend += "Envelope on level "+cr.envelopeOnLevel+" (%).";
			htmlToAppend += "</div>";

			$("#" + subgridDivId).append( htmlToAppend );
	        
	        
		}

    });
    
    profilesGrid.jqGrid("hideCol", "subgrid");
    // profilesGrid.jqGrid('navGrid','#profilesPager',{add:false,edit:false,del:false,search:false,refresh:false},
    //                       {},{},{},{multipleSearch:true, multipleGroup:true, showQuery: true});

    //profilesGrid.jqGrid('setGridWidth',
    //				Math.round($(window).width() * 0.85, true));

}

function renderInterfaceDetail() {
    interfaceDetailGrid = $("#interfaceDetail");

    var colnamesinterfacedetail = ['', 'Property', 'Value', 'Interface Type'];
    var colmodelsinterfacedetail = [{
        name: 'srno',
        index: 'srno',
        editable: false,
        width: 25
    }, {
        name: 'label',
        index: 'label',
        editable: false,
        width: 325
    }, {
        name: 'value',
        index: 'value',
        editable: false,
        width: 250
    }, {
        name: 'grname',
        index: 'grname',
        editable: false,
        width: 250
    }];

    var datainterfacesdetail = [{
            "label": "Enable Port",
            "value": "Yes",
            "grname": "eth0"
        }, {
            "label": "Mac Address",
            "value": "6c:62:6d:66:24:a3",
            "grname": "eth0"
        }, {
            "label": "Connection Status",
            "value": "Cable is connected",
            "grname": "eth0"
        }, {
            "label": "Configure IPV4",
            "value": "Static",
            "grname": "eth0"
        }, {
            "label": "IPV4 Address",
            "value": "192.168.4.200",
            "grname": "eth0"
        }, {
            "label": "Subnet Mask",
            "value": "255.255.255.0",
            "grname": "eth0"
        }, {
            "label": "Default Gateway",
            "value": "192.168.4.1",
            "grname": "eth0"
        }, {
            "label": "DNS Server",
            "value": "",
            "grname": "eth0"
        }, {
            "label": "Search Domains",
            "value": "",
            "grname": "eth0"
        },

        {
            "label": "Enable Port",
            "value": "Yes",
            "grname": "eth1"
        }, {
            "label": "Mac Address",
            "value": "6c:62:6d:66:24:a4",
            "grname": "eth1"
        }, {
            "label": "DHCP Server",
            "value": "Yes",
            "grname": "eth1"
        }, {
            "label": "Connection Status",
            "value": "Cable is connected",
            "grname": "eth1"
        }, {
            "label": "Configure IPV4",
            "value": "Static",
            "grname": "eth1"
        }, {
            "label": "IPV4 Address",
            "value": "169.254.0.1",
            "grname": "eth1"
        }, {
            "label": "Subnet Mask",
            "value": "255.255.0.0",
            "grname": "eth1"
        }, {
            "label": "Default Gateway",
            "value": "",
            "grname": "eth1"
        }, {
            "label": "DNS Server",
            "value": "",
            "grname": "eth1"
        }, {
            "label": "Search Domains",
            "value": "",
            "grname": "eth1"
        },


    ];
    var lastsel2;
    interfaceDetailGrid.jqGrid({
        datatype: "local",
        data: datainterfacesdetail,
        colNames: colnamesinterfacedetail,
        colModel: colmodelsinterfacedetail,
        editurl: 'clientArray',
        cellattr: function(rowId, tv, rawObject, cm, rdata) {
            return 'style="white-space: normal!important"';
        },
        onSelectRow: function(id) {

        },
        loadComplete: function() {

        },
        search: false,
        jsonReader: {
            cell: ""
        },
        sortname: 'id',
        grouping: true,
        groupingView: {
            groupField: ['grname'],
            groupColumnShow: [false]
        },
        sortorder: 'asc',
        viewrecords: true,
        height: "100%",
        caption: "Interface Details"
    });


}

function renderInterfaceTable() {

    interfacegrid = $("#interface");

    var colnamesinterface = ['Interface Type', 'Interface Name'];
    var colmodelsinterface = [{
        name: 'type',
        index: 'type',
        editable: false,
        width: 350
    }, {
        name: 'name',
        index: 'name',
        editable: false,
        width: 250
    }];

    var datainterfaces = [{
        "type": "Corporate Network",
        "name": "eth0"
    }, {
        "type": "Building Network",
        "name": "eth1"
    }, {
        "type": "Bacnet Network",
        "name": "eth1"
    }];
    var lastsel2;
    interfacegrid.jqGrid({
        datatype: "local",
        data: datainterfaces,
        colNames: colnamesinterface,
        colModel: colmodelsinterface,
        editurl: 'clientArray',
        cellattr: function(rowId, tv, rawObject, cm, rdata) {
            return 'style="white-space: normal!important"';
        },
        onSelectRow: function(id) {

        },
        loadComplete: function() {

        },
        search: false,
        jsonReader: {
            cell: ""
        },
        sortname: 'id',
        //grouping:true,
        //groupingView : {
        //	groupField : ['test','step'],
        //	groupColumnShow : [false,false]
        //},
        sortorder: 'asc',
        viewrecords: true,
        height: "100%",
        caption: "Interfaces"
    });

}


