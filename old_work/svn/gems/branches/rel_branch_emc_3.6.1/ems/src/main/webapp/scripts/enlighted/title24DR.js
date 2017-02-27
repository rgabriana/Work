/************************************************************************************
 ************************************************************************************
 *****************COMMON JAVSCRIPT CUSTOM FUNCTIONS STARTS****************************
 ************************************************************************************
 ************************************************************************************* 
 */
String.prototype.startsWith = function (str) {
        return !this.indexOf(str);
    }
String.prototype.isCustomEmpty = function() {
	if (this != null && this != undefined && this.trim() && this.length > 0){
    	return false;
    }else{
    	return true;
    }
};
/**
 * Pass ot like [ 'p0', 'p1', 'p2' ]
 * @param vArr
 * @returns {Boolean}
 */
function isCustomExists(vArr) {
	 if (vArr.length <= 0){
		 return false;
	 }
	 for (i=0; i<vArr.length; i++) {
		    var v = vArr[i];
		    if (v == undefined || v == null ){
				return false;
			}
		    try{
			    if (v.isCustomEmpty()){
			    	return false;
			    }
		    }catch(err){
		    	return true;
		    }
	  }
	 
	return true;
}

$(document).ready(function() { 
	//test
	//alert(isCustomExists(['OL','IS']));
});

function roundToTwo(num) {    
    return +(Math.round(num + "e+2")  + "e-2");
}

function toLetters(num) {
    "use strict";
    var mod = num % 26;
    var pow = num / 26 | 0;
    var out = mod ? String.fromCharCode(64 + mod) : (pow--, 'Z');
    return pow ? toLetters(pow) + out : out;
}

function fromLetters(str) {
    "use strict";
    var out = 0,
        len = str.length,
        pos = len;
    while ((pos -= 1) > -1) {
        out += (str.charCodeAt(pos) - 64) * Math.pow(26, len - 1 - pos);
    }
    return out;
}
function isNumber(evt, element) {

    var charCode = (evt.which) ? evt.which : event.keyCode

    		if(charCode == 85){// IF number is Unkown (U) then it is needed to be allowed
    			return true;
    		}
    if (
        (charCode != 45 || $(element).val().indexOf('-') != -1) &&      // “-” CHECK MINUS, AND ONLY ONE.
        (charCode != 46 || $(element).val().indexOf('.') != -1) &&      // “.” CHECK DOT, AND ONLY ONE.
        (charCode < 48 || charCode > 57))
        return false;

    return true;
}   
/************************************************************************************
 ************************************************************************************
 *****************COMMON JAVSCRIPT CUSTOM FUNCTIONS ENDS****************************
 ************************************************************************************
 ************************************************************************************* 
 */
/************************************************************************************
 ************************************************************************************
 *****************COMMON GRID FUNCTIONS STARTS****************************
 ************************************************************************************
 ************************************************************************************* 
 */
function isRowEdited(element, id) {
    var edited = "0";
    var ind = element.getInd(id, true);
    if (ind != false) {
        edited = $(ind).attr("editable");
    }
    if (edited == "1") {
        // row is being edited
        return true;
    } else {
        // row is not being edited
        return false;
    }
}

function saveAllRowsInGrid(grd){
	var record = grd.jqGrid('getGridParam', 'records');
    for (var i = 1; i <= record; i++) {
    	if(isRowEdited(grd, i)){
    		grd.jqGrid('saveRow', i, false, 'clientArray');
    	}
    }
    //console.log(JSON.stringify(rowData));
}
/************************************************************************************
 ************************************************************************************
 *****************COMMON GRID FUNCTIONS ENDS****************************
 ************************************************************************************
 ************************************************************************************* 
 */

/************************************************************************************
 ************************************************************************************
 *****************STRUCTURE OF PARENT GRIDS****************************
 ************************************************************************************
 ************************************************************************************* 
 */
var drspacegrid = [{
										name : "#drSpaceGrid",
										pager : null,
										grdObj : null,
										dataformelement : "#drspacegriddata",
										save : "#drSpaceGridSave",
										add : "#drSpaceGridAdd",
										caption: 'Representative Spaces',
										noofdefaultcolumns : 5,
                             			colnames : ['Id', 'Space/Circuit No', 'Space/Circuit Name', 'Space/Circuit Type', 'Untested Areas', 'Actions'],
                             			groupby : ['test'],
                             			columnshow : [false],
										colmodels : [
													{
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
													    editable: false,
													    hidden: true,
													    width: '200'
													}, {
													    name: 'untested',
													    index: 'tax',
													    width: 80,
													    align: "right",
													    sorttype: "text",
													    editable: false,
													    hidden: true,
													    width: '20'
													}, {
													    name: 'actions',
													    index: 'tax',
													    width: 80,
													    align: "left",
													    editable: false,
													    width: '50',
													    formatter: function(cellvalue, options, rowObject) {
													        return "<input type='button' value='Delete' onclick='delDRParentRow(" + options.rowId + ", " + rowObject.no + ")'\>";
													    }
													}
										             ],
												data : [
												      	{
												      		  "id": "1","itemno":"a.","test" : "Step 1: Full output test",
												      		  "name":"<p>Using the manual switches/dimmers in each space, set the lighting system to design full output. Note that the lighting in areas with photo controls or occupancy/vacancy sensors may be at less than full output, or may be off.</p>"
												      	 },
												        ],
									}];

/************************************************************************************
 ************************************************************************************
 *****************STRUCTURE OF DEPENDENT GRIDS****************************
 ************************************************************************************
 ************************************************************************************* 
 */
var drdependentgrid = [{
										name : "#drMethod1Grid",
										grdObj : null,
										pager : "#drMethod1Div",
										add : "#drMethod1SpaceAdd",
										caption: '<p><strong>Method 1: Illuminance Measurement.</strong><br />\
											In each space, select one location for illuminance measurement. The chosen location must not be in a primary or secondary skylit or sidelit area, and when placed at the location, the illuminance meter must not have a direct view of a window or skylight. If this is not possible, perform the test at a time and location at which daylight illuminance provides less than half of the design illuminance. Mark each location to ensure that the illuminance meter can be accurately located.</p>',
										noofdefaultcolumns : 5,
                             			colnames : ['ID','', '', 'Test', 'Step'],
                             			groupflag : false,
                             			groupby : ['test'],
                             			columnshow : [false],
                             			parentRowDependentMap : new Object(),
                             			currentMaxKeyParentRowDependentMap : 0,
										colmodels : [
										             	{
														    name: 'id',
														    classes: "stepdetailcol",
														    index: 'id',
														    editable: false,
														    hidden: true,
														    sorttype : 'number',
														    width: 50
														},
														{
														    name: 'itemno',
														    classes: "stepdetailcol",
														    index: 'itemno',
														    editable: false,
														    hidden: false,
														    width: 50
														},{
														    name: 'name',
														    classes: "stepdetailcol",
														    index: 'name',
														    editable: false,
														    sortable : false,
														    width: 500
														}, {
														    name: 'test',
														    index: 'test',
														    editable: true,
														    hidden: false,
															 sorttype: function (cellValus, rowData) {
																return 1;
															},
													
														    width: 50
														}, {
														    name: 'step',
														    index: 'step',
														    editable: false,
														    hidden: true,
														    width: 50
														}],
												data : [
												      	{
												      		  "id": "1","itemno":"a.","test" : "Step 1: Full output test",
												      		  "name":"<p>Using the manual switches/dimmers in each space, set the lighting system to design full output. Note that the lighting in areas with photo controls or occupancy/vacancy sensors may be at less than full output, or may be off.</p>"
												      	 },
												      	{
												      		  "id": "2","itemno":"b.","test" : "Step 1: Full output test",
												      		  "name":"Take one illuminance measurement at a representative location in each space, using an illuminance meter. (fc)"
												      	 },
												      	{
												      		  "id": "3","itemno":"c.","test" : "Step 1: Full output test",
												      		  "name":"Simulate a demand response condition using the demand responsive control."
												      	 },
												      	{
												      		  "id": "4","itemno":"d.","test" : "Step 1: Full output test",
												      		  "name":"Take one illuminance measurement at the same locations as above, with the electric lighting system in the demand response condition. (fc)"
												      	 },
												      	{
												      		  "id": "5","itemno":"e.","test" : "Step 1: Full output test",
												      		  "name":"Turn off the electric lighting and measure the daylighting at the same location (if present) (fc)"
												      	 },
												      	{
												      		  "id": "6","itemno":"f.","test" : "Step 1: Full output test",
												      		  "name":"Calculate the reduction in illuminance in the demand response condition, compared with the design full output condition. [((line b - line e)- (line d - line e)) /(line b - line e)] (%)"
												      	 },
												      	{
												      		  "id": "7","itemno":"g.","test" : "Step 1: Full output test",
												      		  "name":"Note the area of each controlled space (sf)"
												      	 },
												      	{
												      		  "id": "8","itemno":"h.","test" : "Step 1: Full output test",
												      		  "name":"The area-weighted reduction must be at least 0.15 (15%) but must not reduce the combined illuminance from electric light and daylight to less than 50% of the design illuminance in any individual space. [  ( (f1 * g1) + (f2 * g2) + .... ) / (  g1+ g2 +.... ) ]"
												      	 },
												      	{
												      		  "id": "9","itemno":"i.","test" : "Step 1: Full output test",
												      		  "name":"The demand response signal must not reduce the power input of any individual circuit by more than 50%. (Y/N)"
												      	 },
												      	{
												      		  "id": "10","itemno":"a.","test" : "Step 2: Minimum output test",
												      		  "name":"Using the manual switches/dimmers in each space, set the lighting system to minimum output (but not off). Note that the lighting in areas with photo controls or occupancy/vacancy sensors may be at more than minimum output, or may be off."
												      	 },
												      	{
												      		  "id": "11","itemno":"b.","test" : "Step 2: Minimum output test",
												      		  "name":"Take one illuminance measurement at each location, using an illuminance meter. (fc)"
												      	 },
												      	{
												      		  "id": "12","itemno":"c.","test" : "Step 2: Minimum output test",
												      		  "name":"Simulate a demand response condition using the demand responsive control."
												      	 },
												      	{
												      		  "id": "13","itemno":"d.","test" : "Step 2: Minimum output test",
												      		  "name":"Take one illuminance measurement at each location with the electric lighting system in the demand response condition. (fc)"
												      	 },
												      	{
												      		  "id": "14","itemno":"e.","test" : "Step 2: Minimum output test",
												      		  "name":"In each space, the illuminance in the demand response condition must not be less than the illuminance in the minimum output condition or 50% of the design illuminance, whichever is less. (Y/N)"
												      	 },
												      	{
												      		  "id": "15","itemno":"","test" : "EXCEPTION: In daylit spaces, the illuminance in the demand response condition maybe below the minimum output setting, but in the demand response condition the combined illuminance from daylight and electric light must be at least 50% of the design illuminance.",
												      		  "name":""
												      	 },
												      	{
												      		  "id": "16","itemno":"","test" : "Evaluation :",
												      		  "name":"PASS: All applicable Construction Inspection responses are complete and all applicable Equipment Testing Requirements responses are positive (Y &dash; yes)"
												      	 },
												        ],
											        gridRowNumberTypeMap : 
			                     						[
				                             				{"edittype":"text"},{"edittype":"text", 
				                             					editoptions: {
				                             						dataInit: function(element) {
					                             	                    $(element).keypress(function(e){
					                             	                    	 return isNumber(e, this);
					                             	                    });
				                             						}
				                             					},
				                             				},
				                             				{"edittype":"text"},{"edittype":"text", 
				                             					editoptions: {
				                             						dataInit: function(element) {
					                             	                    $(element).keypress(function(e){
					                             	                    	 return isNumber(e, this);
					                             	                    });
				                             						}
				                             					},
				                             				
				                             				},
				                             				{"edittype":"text", 
				                             					editoptions: {
				                             						dataInit: function(element) {
					                             	                    $(element).keypress(function(e){
					                             	                    	 return isNumber(e, this);
					                             	                    });
				                             						}
				                             					},
				                             				},// e
				                             				{"editable":false},{"edittype":"text",
				                             					editoptions: {
				                             						dataInit: function(element) {
					                             	                    $(element).keypress(function(e){
					                             	                    	 return isNumber(e, this);
					                             	                    });
				                             						}
				                             					},
				                             				},//g
				                             				{"editable":false,
				                             				},
				                             				{},{"edittype":"text"},{"edittype":"text", 
				                             					editoptions: {
				                             						dataInit: function(element) {
					                             	                    $(element).keypress(function(e){
					                             	                    	 return isNumber(e, this);
					                             	                    });
				                             						}
				                             					},
				                             				},
				                             				{"edittype":"text"},{"edittype":"text", 
				                             					editoptions: {
				                             						dataInit: function(element) {
					                             	                    $(element).keypress(function(e){
					                             	                    	 return isNumber(e, this);
					                             	                    });
				                             						}
				                             					},
				                             				},//d
				                             				{},{"editable":false},{"editable":false}
			                             				]
									},
									{
										name : "#drMethod2Grid",
										grdObj : null,
										pager : "#drMethod2Div",
										add : "#drMethod2SpaceAdd",
										caption: '<p><strong>Method 2: Power Input Measurement.</strong><br />\
											At the lighting circuit panel, select at least one lighting circuit that serves spaces required to meet Section 130.1(b) to measure the reduction in electrical current. Alternatively, employ the power monitoring capabilities of the DR controls system to monitor the circuits in the tests below. The testing process is constant with either approach.</p>',
										noofdefaultcolumns : 5,
                             			colnames : ['ID','', '', 'Test', 'Step'],
                             			groupflag : false,
                             			groupby : ['test'],
                             			columnshow : [false],
                             			parentRowDependentMap : new Object(),
                             			currentMaxKeyParentRowDependentMap : 0,
										colmodels : [
										             	{
														    name: 'id',
														    classes: "stepdetailcol",
														    index: 'id',
														    editable: false,
														    hidden: true,
														    sorttype : 'number',
														    width: 50
														},
														{
														    name: 'itemno',
														    classes: "stepdetailcol",
														    index: 'itemno',
														    editable: false,
														    hidden: false,
														    width: 50
														},{
														    name: 'name',
														    classes: "stepdetailcol",
														    index: 'name',
														    editable: false,
														    sortable : false,
														    width: 500
														}, {
														    name: 'test',
														    index: 'test',
														    editable: true,
														    hidden: false,
															 sorttype: function (cellValus, rowData) {
																return 1;
															},
													
														    width: 50
														}, {
														    name: 'step',
														    index: 'step',
														    editable: false,
														    hidden: true,
														    width: 50
														}],
												data : [
												      	{
												      		  "id": "1","itemno":"a.","test" : "Step 1: Full output test",
												      		  "name":"Using the manual switches/dimmers in each space, set the lighting system to full output. Note that the lighting in areas with photo controls or occupancy/vacancy sensors may be at less than full output, or may be off."
												      	 },
												      	{
												      		  "id": "2","itemno":"b.","test" : "Step 1: Full output test",
												      		  "name":"Take one electric power measurement for each selected circuit. (VA)"
												      	 },
												      	{
												      		  "id": "3","itemno":"c.","test" : "Step 1: Full output test",
												      		  "name":"Simulate a demand response condition using the demand responsive control."
												      	 },
												      	{
												      		  "id": "4","itemno":"d.","test" : "Step 1: Full output test",
												      		  "name":"Take one electric power measurement at each circuit location with the electric lighting system in the demand response condition. (VA)"
												      	 },
												      	{
												      		  "id": "5","itemno":"e.","test" : "Step 1: Full output test",
												      		  "name":"Calculate the reduction in lighting power in the demand response condition, compared with the full output condition [(b&dash;d)/b] (%)"
												      	 },
												      	{
												      		  "id": "6","itemno":"f.","test" : "Step 1: Full output test",
												      		  "name":"Note the area of each controlled space (sf)"
												      	 },
												      	{
												      		  "id": "7","itemno":"g.","test" : "Step 1: Full output test",
												      		  "name":"Calculate the area&dash;weighted average reduction in electric power in the demand response condition, compared with the full output condition. The area&dash;weighted reduction must be at least 15% (1-{[(e1&times;f1)+(e2&times;f2)+(e3&times;f3)&hellip;]/[f1+f2+f3&hellip;]})"
												      	 },
												      	{
												      		  "id": "8","itemno":"h.","test" : "Step 1: Full output test",
												      		  "name":"The demand response signal must not reduce the power input of any individual circuit by more than 50%. (Y/N)"
												      	 },
												      	{
												      		  "id": "9","itemno":"a.","test" : "Step 2: Minimum output test",
												      		  "name":"Using the manual switches/dimmers in each space, set the lighting system to minimum output (but not off). Note that the lighting in areas with photo controls or occupancy/vacancy sensors may be at more than minimum output, or may be off."
												      	 },
												      	{
												      		  "id": "10","itemno":"b.","test" : "Step 2: Minimum output test",
												      		  "name":"Take one electric power measurement for each selected circuit location."
												      	 },
												      	{
												      		  "id": "11","itemno":"c.","test" : "Step 2: Minimum output test",
												      		  "name":"Simulate a demand response condition using the demand responsive control."
												      	 },
												      	{
												      		  "id": "12","itemno":"d.","test" : "Step 2: Minimum output test",
												      		  "name":"Take one electric power measurement at each circuit with the electric lighting system in the demand response condition."
												      	 },
												      	{
												      		  "id": "13","itemno":"e.","test" : "Step 2: Minimum output test",
												      		  "name":"In each space, the electric power input in the demand response condition must not be less than the power input in the minimum light output condition or 50% of the design illuminance power input condition, whichever is less."
												      	 },
												      	{
												      		  "id": "14","itemno":"","test" : "EXCEPTION: Circuits that supply power to the daylit portion of enclosed spaces as long as lighting in non&dash;daylit portions of the space are not reduced below the lesser of 50% power input level or the minimum light output condition.",
												      		  "name":""
												      	 },
												      	{
												      		  "id": "15","itemno":"","test" : "Evaluation :",
												      		  "name":"PASS: All applicable Construction Inspection responses are complete and all applicable Equipment Testing Requirements responses are positive (Y &dash; yes)"
												      	 },
												      ],
										      gridRowNumberTypeMap : 
		                     						[
			                             				{"edittype":"text"},
			                             				{"edittype":"text", 
			                             					editoptions: {
			                             						dataInit: function(element) {
				                             	                    $(element).keypress(function(e){
				                             	                    	 return isNumber(e, this);
				                             	                    });
			                             						}
			                             					},
			                             				},
			                             				{"edittype":"text"},
			                             				{"edittype":"text", 
			                             					editoptions: {
			                             						dataInit: function(element) {
				                             	                    $(element).keypress(function(e){
				                             	                    	 return isNumber(e, this);
				                             	                    });
			                             						}
			                             					},
			                             				},{"editable":false}, //e
			                             				{"edittype":"text", 
			                             					editoptions: {
			                             						dataInit: function(element) {
				                             	                    $(element).keypress(function(e){
				                             	                    	 return isNumber(e, this);
				                             	                    });
			                             						}
			                             					},
			                             				},{"editable":false},//g
			                             				{},//h
			                             				{"edittype":"text"},
			                             				{"edittype":"text", 
			                             					editoptions: {
			                             						dataInit: function(element) {
				                             	                    $(element).keypress(function(e){
				                             	                    	 return isNumber(e, this);
				                             	                    });
			                             						}
			                             					},
			                             				},
			                             				{"edittype":"text"},
			                             				{"edittype":"text", 
			                             					editoptions: {
			                             						dataInit: function(element) {
				                             	                    $(element).keypress(function(e){
				                             	                    	 return isNumber(e, this);
				                             	                    });
			                             						}
			                             					},
			                             				},{},//e
			                             				{"editable":false},{"editable":false},
			                             			],
									}


				];
/************************************************************************************
 ************************************************************************************
 *****************RETRIEVING GRIDS FROM STRUCTURES STARTED****************************
 ************************************************************************************
 ************************************************************************************* 
 */
function getDRParentGrid(){
	return drspacegrid;
}
function getDRDependentGrid(){
	return drdependentgrid;
}
/************************************************************************************
 ************************************************************************************
 *****************RETRIEVING GRIDS FROM STRUCTURES ENDS****************************
 ************************************************************************************
 ************************************************************************************* 
 */
/************************************************************************************
 ************************************************************************************
 *****************EVENTS REGARDING DEPENDENT GRIDS STARTS****************************
 ************************************************************************************
 ************************************************************************************* 
 */
function dependentDRGridsSetParentGrid(grid){
	for (var i = 0 ; i < getDRDependentGrid().length; i++){
		var gridArrObj =getDRDependentGrid()[i]; 
		gridArrObj.parentGrid = grid;
	}
}
function dependentDRGridsInitiate(){
	for (var i = 0 ; i < getDRDependentGrid().length; i++){
		var gridArrObj =getDRDependentGrid()[i]; 
		gridArrObj.grdObj = $(gridArrObj.name);
	}
}
function dependentDRGridsBeforeParentRowAdded(newrowid){
	for (var i = 0 ; i < getDRDependentGrid().length; i++){
		var gridArrObj =getDRDependentGrid()[i]; 
		gridArrObj.currentMaxKeyParentRowDependentMap = gridArrObj.currentMaxKeyParentRowDependentMap + 1;
		gridArrObj.parentRowDependentMap[gridArrObj.currentMaxKeyParentRowDependentMap] = newrowid + (gridArrObj.noofdefaultcolumns - 1);
	}
}
function dependentDRGridsAfterParentRowAdded(){
	/**
	 * STARTED : DEPENDENT GRID 1
	 */
	var gridArrObj = getDRDependentGrid()[0];
	var newrowid = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	gridArrObj.colnames.push((gridArrObj.currentMaxKeyParentRowDependentMap));
	gridArrObj.colmodels.push({
        name: newrowid,
        index: newrowid,
        width: 80,
        edittype: 'select',
        editable: true,
        sortable: false,
        editoptions: {
            value: "N:N;Y:Y",
            defaultValue: "N"
        },
    });
	saveAllRowsInGrid(gridArrObj.grdObj);
    gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
    /**
	 * ENDS : DEPENDENT GRID 1
	 */
    /**
	 * STARTED : DEPENDENT GRID 2
	 */
	var gridArrObj = getDRDependentGrid()[1];
	var newrowid = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	gridArrObj.colnames.push((gridArrObj.currentMaxKeyParentRowDependentMap));
	gridArrObj.colmodels.push({
        name: newrowid,
        index: newrowid,
        width: 80,
        edittype: 'select',
        editable: true,
        sortable: false,
        editoptions: {
            value: "N:N;Y:Y",
            defaultValue: "N"
        },
    });
	saveAllRowsInGrid(gridArrObj.grdObj);
    gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
    /**
	 * ENDS : DEPENDENT GRID 2
	 */
}
function dependentDRGridsRedraw(){
	renderDRMethod1();
	renderDRMethod2();
	
}
function dependentDRGridsAfterParentRowDelete(spaceNo){
	for (var x =0 ; x < getDRDependentGrid().length; x++){
		var gridArrObj = getDRDependentGrid()[x];
		var record = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	    var rowData = gridArrObj.parentGrid.jqGrid('getRowData');
	    for (var i = 0; i < record; i++) {
	        var spaceN = rowData[i].no;
	        gridArrObj.parentRowDependentMap[spaceN] = i + gridArrObj.noofdefaultcolumns;
	    }
	    var selectedRowId = gridArrObj.parentRowDependentMap[spaceNo];
	    for (var i = gridArrObj.colnames.length - 1; i >= 0; i--) {
	        if (i == selectedRowId) {
	        	gridArrObj.colnames.splice(i, 1);
	        	gridArrObj.colmodels.splice(i, 1);
	        }
	    }
	    //console.log("For Grid "+ x +" Colnames are "+ JSON.stringify(gridArrObj.colnames));
	    saveAllRowsInGrid(gridArrObj.grdObj);
	    gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	}
}

function dependentDRGridsPopulateFromDB(){
	/**
	 * START:::Started for Table I here
	 */
	var gridArrObj = getDRDependentGrid()[0];
    var recordPop = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
    var rowDataPop = gridArrObj.parentGrid.jqGrid('getRowData');
    for (var i = 0; i < recordPop; i++) {
    	
    	var spaceN = rowDataPop[i].no;
    	if (gridArrObj.currentMaxKeyParentRowDependentMap < spaceN){
    		gridArrObj.currentMaxKeyParentRowDependentMap = parseInt(spaceN);
    	}
    	gridArrObj.parentRowDependentMap[spaceN] = i + gridArrObj.noofdefaultcolumns;
    	gridArrObj.colnames.push(toLetters(spaceN));
    	gridArrObj.colmodels.push({
            name: (i+1),
            index: (i+1),
            width: 50,
            edittype: 'select',
            editable: true,
            editoptions: {
                value: "N:N;Y:Y",
                defaultValue: "N"
            }
        });
    }
    recordPop = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
    rowDataPop = gridArrObj.grdObj.jqGrid('getRowData');
    var tempdata1 = $("#drmethod1data").val();
    if(!tempdata1.isCustomEmpty()){
    	var d1=$.parseJSON(tempdata1);
    	if (Object.keys(d1).length > 0){
    		gridArrObj.data = d1;
    	}
    }
    /**
	 * END:::Endeded for Table I here
	 */
    
    /**
	 * START:::Started for Table II here
	 */
	gridArrObj = getDRDependentGrid()[1];
    recordPop = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
    rowDataPop = gridArrObj.parentGrid.jqGrid('getRowData');
    for (var i = 0; i < recordPop; i++) {
    	
    	var spaceN = rowDataPop[i].no;
    	if (gridArrObj.currentMaxKeyParentRowDependentMap < spaceN){
    		gridArrObj.currentMaxKeyParentRowDependentMap = parseInt(spaceN);
    	}
    	gridArrObj.parentRowDependentMap[spaceN] = i + gridArrObj.noofdefaultcolumns;
    	gridArrObj.colnames.push(toLetters(spaceN));
    	gridArrObj.colmodels.push({
            name: (i+1),
            index: (i+1),
            width: 50,
            edittype: 'select',
            editable: true,
            editoptions: {
                value: "N:N;Y:Y",
                defaultValue: "N"
            }
        });
    }
    recordPop = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
    rowDataPop = gridArrObj.grdObj.jqGrid('getRowData');
    tempdata1 = $("#drmethod2data").val();
    if(!tempdata1.isCustomEmpty()){
    	var d1=$.parseJSON(tempdata1);
    	if (Object.keys(d1).length > 0){
    		gridArrObj.data = d1;
    	}
    }
    /**
	 * END:::Endeded for Table II here
	 */
}
/************************************************************************************
 ************************************************************************************
 *****************EVENTS REGARDING DEPENDENT GRIDS ENDS****************************
 ************************************************************************************
 ************************************************************************************* 
 */



/************************************************************************************
 ************************************************************************************
 *****************EVENTS REGARDING PARENT ROW STARTS****************************
 ************************************************************************************
 ************************************************************************************* 
 */
function delDRParentRow(parentTableRowId, spaceNo) {
	getDRParentGrid()[0].grdObj.jqGrid('delRowData', parentTableRowId);
	getDRParentGrid()[0].grdObj.trigger('reloadGrid');
    dependentDRGridsAfterParentRowDelete(spaceNo);
    dependentDRGridsRedraw();
}

function saveDRParentRow(grid, idToSave) {
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
        $(getDRParentGrid()[0].save).attr("disabled", false);
    } else {
        $(getDRParentGrid()[0].save).attr("disabled", true);
    }
    $(getDRParentGrid()[0].add).attr("disabled", false);
    return true;
}
/************************************************************************************
 ************************************************************************************
 *****************EVENTS REGARDING PARENT ROW ENDS****************************
 ************************************************************************************
 ************************************************************************************* 
 */

/************************************************************************************
 ************************************************************************************
 *****************RENDERING OF PARENT GRID STARTED************************************
 *************************************************************************************
 ************************************************************************************ 
 */
function renderDRSpaceGrid(){
	var lastsel2;
	var gridArrObj = getDRParentGrid()[0]; 
    dependentDRGridsInitiate();
    var grid = $(gridArrObj.name),
    mydata = $.parseJSON($(gridArrObj.dataformelement).val());
    dependentDRGridsSetParentGrid(grid);
    grid.jqGrid({
        datatype: "local",
        data: mydata,
        colNames: gridArrObj.colnames,
        colModel: gridArrObj.colmodels,
        onSelectRow: function(id) {
            if (id && id != lastsel2) {
                lastsel2 = id;
            }
            saveDRParentRow(grid, lastsel2);
            grid.jqGrid('editRow', id, true);
        },
        cellattr: function(rowId, tv, rawObject, cm, rdata) {
            return 'style="white-space: normal!important"';
        },
        search: true,
        pager: gridArrObj.pager,
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
        caption: gridArrObj.caption
    });
//    grid.jqGrid('navGrid', gridArrObj.pager, {
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

    gridArrObj.grdObj = grid;
    dependentDRGridsPopulateFromDB();
    dependentDRGridsRedraw();
    
    $(gridArrObj.save).click(function() {
    	return saveDRParentRow(grid);
    });

    $(gridArrObj.add).click(function() {
        var record = grid.jqGrid('getGridParam', 'records');
        var newrowid = record + 1;
        dependentDRGridsBeforeParentRowAdded(newrowid);
        var addNewparameters = {
            rowID: getDRDependentGrid()[0].currentMaxKeyParentRowDependentMap,
            initdata: {
                id: getDRDependentGrid()[0].currentMaxKeyParentRowDependentMap,
                no: getDRDependentGrid()[0].currentMaxKeyParentRowDependentMap,
                name: 'Room Location 1',
                type: 'Office',
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
        $(gridArrObj.save).attr("disabled", false);
        $(gridArrObj.add).attr("disabled", true);
        dependentDRGridsAfterParentRowAdded();
        dependentDRGridsRedraw();
        
    });
}
/************************************************************************************
 ************************************************************************************
 *****************RENDERING OF PARENT GRID ENDS************************************
 *************************************************************************************
 ************************************************************************************ 
 */
/************************************************************************************
 ************************************************************************************
 *****************RENDERING OF DEPENDENT GRIDS STARTED************************************
 *************************************************************************************
 ************************************************************************************ 
 */
function dependentDRGridsSaveAfterSubmit(){
	   
	   var gridArrObj = getDRDependentGrid()[0];
	   saveAllRowsInGrid(gridArrObj.grdObj);
	   evaluateDRMethod1Rows();
	   gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	   grdjsonstr = JSON.stringify(gridArrObj.data);
	   $("#drmethod1data").val(grdjsonstr);
	   gridArrObj = getDRDependentGrid()[1];
	   saveAllRowsInGrid(gridArrObj.grdObj);
	   evaluateDRMethod2Rows();
	   gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	   grdjsonstr = JSON.stringify(gridArrObj.data);
	   $("#drmethod2data").val(grdjsonstr);
}

function renderDRMethod2(){
	var gridArrObj = getDRDependentGrid()[1];
	var grid = gridArrObj.grdObj;
	if (grid == undefined || grid == null){
		grid = $(gridArrObj.name);
		gridArrObj.grdObj = grid;
	}
	var record = 0;
	if (grid != undefined && grid != null){
		record = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
		//var parentRecords = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	    if (record != null && record != 'undefined' && record >= 0) {
	        $.jgrid.gridUnload(gridArrObj.name);
	        grid = $(gridArrObj.name);
	        gridArrObj.grdObj = grid;
	    }
	}
	
	var parentRecords = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
    var lastsel2=0;
    
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
            	//alert('ID'+id+'lastsel2'+lastsel2);
                if (lastsel2 != null && lastsel2 != 'undefined') {
                    var rowDataSave = grid.getRowData(lastsel2);
                    gridArrObj.data = grid.jqGrid('getRowData');
                    saveRowDRMethod2(lastsel2);
                    grid.jqGrid('restoreRow', lastsel2);
                    //renderDRMethod1();
                }
                var rowNumberTypeDetails = gridArrObj.gridRowNumberTypeMap[id-1];
                var colModels = grid.jqGrid("getGridParam", "colModel");
                for (var i = (gridArrObj.noofdefaultcolumns); i < colModels.length; i++) {
                    var cm = colModels[i];
                    cm.editable = true;
                    if (rowNumberTypeDetails == 'undefined' || rowNumberTypeDetails == null || rowNumberTypeDetails.edittype == 'undefined' || rowNumberTypeDetails.edittype == null)  {
                        cm.edittype = 'select';
                        cm.editoptions = {
                            value: ":;N:N;Y:Y"
                        };
                    }else{
                    	cm.edittype = rowNumberTypeDetails.edittype;
                        cm.editoptions = rowNumberTypeDetails.editoptions;
                    }
                    if (rowNumberTypeDetails != 'undefined' || rowNumberTypeDetails != null){
                    	if (rowNumberTypeDetails.hasOwnProperty('editable')){
                    		cm.editable = rowNumberTypeDetails.editable;
                    	}
                    	if (rowNumberTypeDetails.hasOwnProperty('sorttype')){
                    		cm.sorttype = rowNumberTypeDetails.sorttype;
                    	}
                    	if (rowNumberTypeDetails.hasOwnProperty('cellattr')){
                    		cm.cellattr = rowNumberTypeDetails.cellattr;
                    	}
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
                        cssClass = "subHeadPoint";
                        break;
                    case "step":
                        cssClass = "subHeadPoint";
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
        gridComplete : function(){

        },
        search: false,
        jsonReader: {
            cell: ""
        },
        sortname : 'id',
        sortorder : 'asc',
        grouping: true,
        groupingView: {
            groupField: gridArrObj.groupby,
            groupColumnShow: gridArrObj.columnshow,
        },
        viewrecords: true,
        height: "100%",
        rowNum: 50000,
        caption: gridArrObj.caption
    });
    gridArrObj.grdObj.jqGrid('setCell', 7, ( gridArrObj.noofdefaultcolumns ), '', {color:'red'},{colSpan: parentRecords});
//    gridArrObj.grdObj.jqGrid('setGroupHeaders', {
//    	  useColSpanStyle: true,   
//    	  groupHeaders:[{startColumnName: 'itemno', numberOfColumns: 2, titleText: 'Complete all tests on page 3 & 4 (No Daylight Test, Full Daylight Test, and Partial Daylight Test) and fill out Pass/Fail section on Page 4.'}]
//    	});
    gridArrObj.grdObj.jqGrid('setGroupHeaders', {
    	  useColSpanStyle: true,   
    	  groupHeaders:[{startColumnName: 1, numberOfColumns: (7), titleText: 'Circuit number'}]
    	});
}

function saveRowDRMethod2(id){
	var gridArrObj = getDRDependentGrid()[1];
	gridArrObj.grdObj.jqGrid('saveRow', id, false, 'clientArray');
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	evaluateDRMethod2Rows();
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
}
function evaluateDRMethod2Rows(){
	var gridArrObj = getDRDependentGrid()[1];
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	var parentGridRecords = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	var currGridRecords = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
	var subgridgroupsdata = $(gridArrObj.name).jqGrid("getGridParam", "groupingView").groups;
	var maingroupinfo = (subgridgroupsdata).filter(function (e){
        return e.idx == 0;
    });
	//Filter the subgroid groups data and find out row numbers 
	//console.log("Test Result data is "+ JSON.stringify(gridArrObj.data));
	var passCheckMap = {'8':'Y', '13':'Y'};
	for (var i = 0; i < parentGridRecords; i++){
		var lastrow = 0;
		for (var j = 0; j < maingroupinfo.length - 1; j++ ){  
			var startRow = maingroupinfo[j].startRow; //Above group should pass to make pass the lower group.//Remove lastrow as well if dont not want
			lastrow = lastrow + maingroupinfo[j].cnt;
			var line_b =0, line_d=0, line_e =0;
			for (var cnt =0; cnt < (lastrow - startRow); cnt++){
				var colname = (i+1);
				var rdata = gridArrObj.data[startRow  +  cnt];
				var tmp = rdata.itemno;
				var val = rdata[colname];
				var valChkRef = passCheckMap[rdata.id];
				if (val != undefined && !val.isCustomEmpty()){
					if (tmp == 'b.'){
						line_b = val;
					}else if (tmp == 'd.'){
						line_d = val;
					}
				}
				if (tmp == 'e.' && rdata.id == '5'){
					var line_e = ( (line_b - line_d) )/ (line_b );
					if (line_e == undefined || line_e == null || isNaN(line_e)){
						line_e = 0;
					}
					line_e = roundToTwo(line_e) * 100;
					gridArrObj.data[startRow  +  cnt][colname] = line_e;
				}
				var totalChecked = $('input[name="dracceptance.constructioninspection"]:checked').length;
				if (totalChecked >= 2){
					if (valChkRef == undefined || valChkRef == null){
						var gColval = gridArrObj.data[6][1] ;
						if (gColval == undefined || gColval == null || isNaN(gColval)){
							gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
						}else{
							if (gColval >= 0.15){
								gridArrObj.data[currGridRecords - 1][colname] = 'PASS';
							}else{
								gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
							}
						}
					}else if (val != undefined && val != valChkRef){
						gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
					}
				}else{
					gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
				}
				//console.log('tmp:'+tmp+':val:'+val+':line_b:'+line_b+':line_d:'+line_d+':line_e:'+line_e+':rdata:'+JSON.stringify(rdata))
			}
			
		}
	}
	//Calculate the summary 
	var data = gridArrObj.data;
	//gridArrObj.noofdefaultcolumns
	var numerator = 0;
	var denominator = 0;
	for (var c=1; c <= parentGridRecords; c++){
		numerator = numerator + ( (data[4][c]) * (data[5][c]) );   
		denominator = denominator + (data[5][c]);
	}
	var line_g = 1 - ( numerator / denominator);
	if (line_g == undefined || line_g == null || isNaN(line_g)){
		line_g = 0;
	}
	line_g = roundToTwo(line_g);
	gridArrObj.data[6][1] = line_g;
	
	gridArrObj.grdObj.jqGrid('setGridParam', {data: gridArrObj.data}).trigger('reloadGrid');
	gridArrObj.grdObj.jqGrid('setCell', 7, ( gridArrObj.noofdefaultcolumns ), '', {'text-align':'center'},{colSpan: parentGridRecords});
	saveAllRowsInGrid(gridArrObj.grdObj);
}
function saveRowDRMethod1(id){
	var gridArrObj = getDRDependentGrid()[0];
	gridArrObj.grdObj.jqGrid('saveRow', id, false, 'clientArray');
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	evaluateDRMethod1Rows();
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
}
function evaluateDRMethod1Rows(){
	var gridArrObj = getDRDependentGrid()[0];
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	var parentGridRecords = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	var currGridRecords = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
	var subgridgroupsdata = $(gridArrObj.name).jqGrid("getGridParam", "groupingView").groups;
	var maingroupinfo = (subgridgroupsdata).filter(function (e){
        return e.idx == 0;
    });
	//Filter the subgroid groups data and find out row numbers 
	//console.log("Test Result data is "+ JSON.stringify(gridArrObj.data));
	var passCheckMap = {'9':'Y', '14':'Y'};
	for (var i = 0; i < parentGridRecords; i++){
		var lastrow = 0;
		for (var j = 0; j < maingroupinfo.length - 1; j++ ){  
			var startRow = maingroupinfo[j].startRow; //Above group should pass to make pass the lower group.//Remove lastrow as well if dont not want
			lastrow = lastrow + maingroupinfo[j].cnt;
			var line_b =0, line_d=0, line_e =0, line_g =0;
			for (var cnt =0; cnt < (lastrow - startRow); cnt++){
				var colname = (i+1);
				var rdata = gridArrObj.data[startRow  +  cnt];
				var tmp = rdata.itemno;
				var val = rdata[colname];
				var valChkRef = passCheckMap[rdata.id];
				if (val != undefined && !val.isCustomEmpty()){
					if (tmp == 'b.'){
						line_b = val;
					}else if (tmp == 'd.'){
						line_d = val;
					}else if (tmp == 'e.'){
						line_e = val;
					}else if (tmp == 'g.'){
						line_g = val;
					}
				}
				if (tmp == 'f.'){
					var line_f = ( (line_b - line_e) - (line_d - line_e) )/ (line_b - line_e);
					if (line_f == undefined || line_f == null || isNaN(line_f)){
						line_f = 0;
					}
					line_f = roundToTwo(line_f) * 100;
					gridArrObj.data[startRow  +  cnt][colname] = line_f;
				}
				var totalChecked = $('input[name="dracceptance.constructioninspection"]:checked').length;
				if (totalChecked >= 2){
					if (valChkRef == undefined || valChkRef == null){
						var hColval = gridArrObj.data[7][1] ;
						if (hColval == undefined || hColval == null || isNaN(hColval)){
							gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
						}else{
							if (hColval >= 0.15){
								gridArrObj.data[currGridRecords - 1][colname] = 'PASS';
							}else{
								gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
							}
						}
					}else if (val != undefined && val != valChkRef){
						gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
					}
				}else{
					gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
				}
				//console.log('tmp:'+tmp+':val:'+val+':line_b:'+line_b+':line_d:'+line_d+':line_e:'+line_e+':rdata:'+JSON.stringify(rdata))
			}
			
		}
	}
	
	
	//Calculate the summary 
	var data = gridArrObj.data;
	//gridArrObj.noofdefaultcolumns
	var numerator = 0;
	var denominator = 0;
	for (var c=1; c <= parentGridRecords; c++){
		numerator = numerator + ( (data[5][c]) * (data[6][c]) );   
		denominator = denominator + (data[6][c]);
	}
	var line_h = numerator / denominator;
	if (line_h == undefined || line_h == null || isNaN(line_h)){
		line_h = 0;
	}
	line_h = roundToTwo(line_h);
	gridArrObj.data[7][1] = line_h;
	
	gridArrObj.grdObj.jqGrid('setGridParam', {data: gridArrObj.data}).trigger('reloadGrid');
	gridArrObj.grdObj.jqGrid('setCell', 8, ( gridArrObj.noofdefaultcolumns ), '', {'text-align':'center'},{colSpan: parentGridRecords});
	saveAllRowsInGrid(gridArrObj.grdObj);
}
function renderDRMethod1(){
	var gridArrObj = getDRDependentGrid()[0];
	var grid = gridArrObj.grdObj;
	if (grid == undefined || grid == null){
		grid = $(gridArrObj.name);
		gridArrObj.grdObj = grid;
	}
	var record = 0;
	if (grid != undefined && grid != null){
		record = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
		//var parentRecords = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	    if (record != null && record != 'undefined' && record >= 0) {
	        $.jgrid.gridUnload(gridArrObj.name);
	        grid = $(gridArrObj.name);
	        gridArrObj.grdObj = grid;
	    }
	}
	
	var parentRecords = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
    var lastsel2=0;
    
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
            	//alert('ID'+id+'lastsel2'+lastsel2);
                if (lastsel2 != null && lastsel2 != 'undefined') {
                    var rowDataSave = grid.getRowData(lastsel2);
                    gridArrObj.data = grid.jqGrid('getRowData');
                    saveRowDRMethod1(lastsel2);
                    grid.jqGrid('restoreRow', lastsel2);
                    //renderDRMethod1();
                }
                var rowNumberTypeDetails = gridArrObj.gridRowNumberTypeMap[id-1];
                var colModels = grid.jqGrid("getGridParam", "colModel");
                for (var i = (gridArrObj.noofdefaultcolumns); i < colModels.length; i++) {
                    var cm = colModels[i];
                    cm.editable = true;
                    if (rowNumberTypeDetails == 'undefined' || rowNumberTypeDetails == null || rowNumberTypeDetails.edittype == 'undefined' || rowNumberTypeDetails.edittype == null)  {
                        cm.edittype = 'select';
                        cm.editoptions = {
                            value: ":;N:N;Y:Y"
                        };
                    }else{
                    	cm.edittype = rowNumberTypeDetails.edittype;
                        cm.editoptions = rowNumberTypeDetails.editoptions;
                    }
                    if (rowNumberTypeDetails != 'undefined' || rowNumberTypeDetails != null){
                    	if (rowNumberTypeDetails.hasOwnProperty('editable')){
                    		cm.editable = rowNumberTypeDetails.editable;
                    	}
                    	if (rowNumberTypeDetails.hasOwnProperty('sorttype')){
                    		cm.sorttype = rowNumberTypeDetails.sorttype;
                    	}
                    	if (rowNumberTypeDetails.hasOwnProperty('cellattr')){
                    		cm.cellattr = rowNumberTypeDetails.cellattr;
                    	}
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
                        cssClass = "subHeadPoint";
                        break;
                    case "step":
                        cssClass = "subHeadPoint";
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
        gridComplete : function(){

        },
        search: false,
        jsonReader: {
            cell: ""
        },
        sortname : 'id',
        sortorder : 'asc',
        grouping: true,
        groupingView: {
            groupField: gridArrObj.groupby,
            groupColumnShow: gridArrObj.columnshow,
        },
        viewrecords: true,
        height: "100%",
        rowNum: 50000,
        caption: gridArrObj.caption
    });
    gridArrObj.grdObj.jqGrid('setCell', 8, ( gridArrObj.noofdefaultcolumns ), '', {color:'red'},{colSpan: parentRecords});
//    gridArrObj.grdObj.jqGrid('setGroupHeaders', {
//    	  useColSpanStyle: true,   
//    	  groupHeaders:[{startColumnName: 'itemno', numberOfColumns: 2, titleText: 'Complete all tests on page 3 & 4 (No Daylight Test, Full Daylight Test, and Partial Daylight Test) and fill out Pass/Fail section on Page 4.'}]
//    	});
    gridArrObj.grdObj.jqGrid('setGroupHeaders', {
    	  useColSpanStyle: true,   
    	  groupHeaders:[{startColumnName: 1, numberOfColumns: (7), titleText: 'Space number'}]
    	});
}

/************************************************************************************
 ************************************************************************************
 *****************RENDERING OF DEPENDENT GRIDS ENDS************************************
 *************************************************************************************
 ************************************************************************************ 
 */
function dracceptancemethodofmeasurementchanged(){
	var value = $('select#dracceptancemethodofmeasurement option:selected').val();
	  if (value == 'Method1'){
		  $("#drMethod1Div").show();
		  $("#drMethod2Div").hide();
	  }else{
		  $("#drMethod1Div").hide();
		  $("#drMethod2Div").show();
	  }
}
function registerDRClickEvents(){
	$("#dracceptancemethodofmeasurement").change(function() {
		dracceptancemethodofmeasurementchanged();
	});
}
$(document).ready(function() { 
	registerDRClickEvents();
	dracceptancemethodofmeasurementchanged();
});

