function updateCheckboxval(elename){
	var ele = $("#"+elename);
	
	if ($(ele).is( ":checked" ) ){
		$(ele).prop('checked', true);
		$(ele).attr("checked", "true");
	}else{
		$(ele).prop('checked', false);
		$(ele).removeAttr("checked");
		
	}
	
}

var autoDayLightControlSystemGrid = [{
										name : "#autoDLControlSystemGrid",
										data : [],
										pager : "#autoDLControlSystemGridDiv",
										save : "#autoDLControlSystemGridSave",
										add : "#autoDLControlSystemGridAdd",
										caption: 'Control Systems',
										colNames : ['Id', 'Space No', 'Constrol System', 'System Name', 'Check if Tested Control is Representative of Sample', 'Actions'],
										colModels : [{
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
										    hidden: true,
										    sorttype: "int"
										}, {
										    name: 'sysNo',
										    index: 'sysNo',
										    width: 100,
										    editable: false,
										    sortable: false,
										    width: '200',
										    formatter: function(cellvalue, options, rowObject) {
										    	
										    	return toLetters(rowObject.no);
										    }
										}, {
										    name: 'name',
										    index: 'name',
										    width: 100,
										    editable: true,
										    sortable: false,
										    width: '200'
										},{
										    name: 'chkfield',
										    index: 'chkfield',
										    align: "left",
										    editable: false,
										    sortable: false,
										    width: '130',
										    formatter: function(cellvalue, options, rowObject) {
										        //return "<input type='button' value='Delete' onclick='delRowAutoDL(" + options.rowId + ", " + rowObject.no + ")'\>";
										        var s = JSON.stringify(cellvalue); 
										        if (cellvalue == undefined || cellvalue == null || cellvalue == ''){
										        	return "<input id='checkfield"+rowObject.no+"' name='checkfield"+rowObject.no+"' type='checkbox' onclick=\"updateCheckboxval('checkfield"+rowObject.no+"')\" />";
										        }
										        return cellvalue;
										    }
										}
										,{
										    name: 'actions',
										    index: 'tax',
										    width: 80,
										    align: "left",
										    editable: false,
										    sortable: false,
										    width: '50',
										    formatter: function(cellvalue, options, rowObject) {
										        return "<input type='button' value='Delete' onclick='delRowAutoDL(" + options.rowId + ", " + rowObject.no + ")'\>";
										    }
										}
										]
									}];

$(document).ready(function() { 
	registerAutoDLClickEvents();
});

function registerAutoDLClickEvents(){
}

var autoDLControlSystemDependents = [
                                     { //DEPENDENT GRID 0
                                     	name : "#autoDLSensroControlGrid",
                             			grdObj : null,
                             			parentGrid : null,
                             			noofdefaultcolumns : 5,
                             			colnames : ['ID','', '', 'Test', 'Step'],
                             			groupby : ['test'],
                             			columnshow : [false],
                             			caption: '',
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
                             						    hidden: true,
                             						    width: 50
                             						},{
                             						    name: 'name',
                             						    classes: "stepdetailcol",
                             						    index: 'name',
                             						    editable: false,
                             						    sortable : false,
                             						    width: 450
                             						}, {
                             						    name: 'test',
                             						    index: 'test',
                             						    editable: true,
                             						    hidden: false,
                             						   sortable: false,
                             						    width: 50
                             						}, {
                             						    name: 'step',
                             						    index: 'step',
                             						    editable: false,
                             						    hidden: true,
                             						    width: 50
                             						}],
                             			parentRowDependentMap : new Object(),
                             			currentMaxKeyParentRowDependentMap : 0,
                             			data: 	[
 												{
 													 "id" : "1",
 													 "name":"Zone Type: Skylit (Sky), Primary Sidelit (PS), or Secondary Sidelit (SS)",
 													 "test":"2 System Information",
 												},
 												{
 													"id" : "2",
 													 "name":"Control Type: Continuous Dimming with more than 10 light levels (C), Stepped Dimming (SD), Switching (SW)",
 													 "test":"2 System Information",
 												},
                             			      	{
                             			      			"id" : "3",

 													 "name":"Design Footcandles: (enter number or Unknown)",
 													 "test":"2 System Information",
 												},
 	                        			      	{
 	                        			      		 "id" : "4",
 														"name":"Control Loop Type: Open Loop (OL), Closed Loop (CL)",
 	                        			      		 "test":"3 Sensor and Controls",
 	                        			      	},
                             			      	{
                             			      		 "id" : "5",
 "name":"<p>Sensor Location: Outside (O), Inside Skylight (IS), Near Windows facing out (NW), In Controlled Zone (CZ)</p>",
                             			      		 "test":"3 Sensor and Controls",
                             			      	 },
                             			      	{
                             			      		 "id" : "6",
 "name":"<p>Sensor Location is Appropriate to Control Loop Type: (Y/N)<br />" +
                             			      		 		"If control loop type is Open Loop (OL): Enter yes (Y) if location = Outside (O), " +
                             			      		 		"Inside Skylight (IS), or Near Windows facing out (NW); otherwise, enter no (N).<br />" +
                             			      		 		"If Control loop type is Closed Loop (CL): Enter yes (Y) if location = In Controlled Zone (CZ); " +
                             			      		 		"otherwise, enter no (N).</p>",
                             			      		 "test":"3 Sensor and Controls",
                             			      	 },
                             			      	 {
                             			      		 "id" : "7",
 "name":"<p>Control Adjustments are in Appropriate Location (Y/N): Yes, If Readily " +
                             			      		 		"Accessible or<br /> Yes if in Ceiling &le; 11 ft , No for all other .</p>",
                             			      		 "test":"3 Sensor and Controls",
                             			      	 },
                             			      	{
                             			      		 "id" : "8",
 "name":"Installation Manuals and Calibration Instructions Provided to Building Owner:(Y/N)",
                             			      		 "test":"4 Has documentation been provided by the installer:",
                             			      	 },
                             			      	{
                             			      		"id" : "9",
  "name":"Location of Light Sensor on Plans: (Y/N)",
                             			      		 "test":"4 Has documentation been provided by the installer:",
                             			      	 },
                             			      	{
                             			      		 "id" : "10",
 "name":"Location of Light Sensor on Plans: (Page Number)",
                             			      		 "test":"4 Has documentation been provided by the installer:",
                             			      	 },
                             			      	{
                             			      		 "id" : "11",
 "name":"Are luminaires controlled by automatic daylighting controls only in daylit zones: (Y/N)",
                             			      		 "test":"5 Separate Controls of Luminaires in Daylit Zones:",
                             			      	 },
                             			      	{
                             			      		 "id" : "12",
 "name":"Separately circuited for daylit zones by windows and daylit zones under skylights: (Y/N)",
                             			      		 "test":"5 Separate Controls of Luminaires in Daylit Zones:",
                             			      	 },
                             			      	{
                             			      		 "id" : "13",
 "name":"Daylighting control has been certified in accordance with &sect;110.9: (Y/N)",
                             			      		 "test":"6 Daylighting control device certification",
                             			      	 },
                             			      	{
                             			      		 "id" : "14",
 "name":"If all responses on Construction Inspection pages 1 & 2are complete and all Yes/No questions have a Yes (Y) response, the tests PASS; If any responses on this page are incomplete OR there are any no (N) responses, the tests FAIL",
                             			      		 "test":"Construction Inspection PASS/FAIL. ",
                             			      	 },
                             			      	],
                     			      	gridRowNumberTypeMap : 
                     			      			[
 												{
 														 "edittype":"select", "editoptions" : { "value" : ":;Sky:Sky;PS:PS;SS:SS"}
 												 },
 												 {
 														 "edittype":"select", "editoptions" : { "value" : ":;C:C;SD:SD;SW:SW"}
 												 },
 												 {
 														 "edittype":"text", editoptions: {
			                             						dataInit: function(element) {
				                             	                    $(element).keypress(function(e){
				                             	                    	 return isNumber(e, this);
				                             	                    });
			                             						}
			                             					},
 												 },
                     			      			 {
                     			      				 "edittype":"select", "editoptions" : { "value" : ":;OL:OL;CL:CL", 
                     			      					}, 
                     			      			 },
                     			      			 {
                     			      				 "edittype":"select", "editoptions" : { "value" : ":;O:O;IS:IS;NW:NW;CZ:CZ",
                     			      					}
                     			      			 },{"editable":false}, //6
                     			      			 {},{},{},
                     			      			 {
                     			      				"edittype":"text",editoptions: {
	                             						dataInit: function(element) {
		                             	                    $(element).keypress(function(e){
		                             	                    	 return isNumber(e, this);
		                             	                    });
	                             						}
	                             					},
                     			      			 },{},{},{},
                     			      			 	{"editable":false}
                     			      			 ],
 									},
 									{//DEPENDENT GRID 1
 	                                     	name : "#functionalTestingCDSGrid",
 	                             			grdObj : null,
 	                             			parentGrid : null,
 	                             			noofdefaultcolumns : 5,
 	                             			colnames : ['ID','', '', 'Test', 'Step'],
 	                             			groupby : ['test'],
 	                             			columnshow : [false],
 	                             			caption: '<h3>II. Functional Performance Testing &ndash; Continuous Dimming Systems NA\-7.6.1.2.1</h3>\
 	                             				<h4>Power estimation using amp&dash;meter measurement, or alternate option &ndash; watt&dash;meter measurement</h4>',
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
 	                             						  sortable: false,
 	                             						    width: 50
 	                             						},{
 	                             						    name: 'name',
 	                             						    classes: "stepdetailcol",
 	                             						    index: 'name',
 	                             						    editable: false,
 	                             						    sortable : false,
 	                             						    width: 400
 	                             						}, {
 	                             						    name: 'test',
 	                             						    index: 'test',
 	                             						    editable: true,
 	                             						    hidden: false,
	 	                             						 sorttype: function (cellValus, rowData) {
	 	                             							 if (rowData.itemno == ''){
	 	                             								 return 'z';
	 	                             							 }
	 	                             						    return rowData.itemno;
	 	                             						},

 	                             						    width: 50
 	                             						}, {
 	                             						    name: 'step',
 	                             						    index: 'step',
 	                             						    editable: false,
 	                             						    hidden: true,
 	                             						    width: 50
 	                             						}],
 	                             			parentRowDependentMap : new Object(),
 	                             			currentMaxKeyParentRowDependentMap : 0,
 	                             			data: 	[
	                             			      	 {
	                             			      		  "id": "1","itemno":"a.","test" : "System Information",
	                             			      		  "name":"Control Loop Type: Open Loop or Closed Loop? (O or C)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "2","itemno":"b.","test" : "System Information",
	                             			      		  "name":"Indicate if Mandatory control &dash; M (required for skylit zone or primary sidelit zone with installed general lighting power > 120 W); \
or Voluntary &dash;V (M, V)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "3","itemno":"c.","test" : "System Information",
	                             			      		  "name":"If automatic daylighting controls are mandatory, are all general lighting luminaires in daylit zones controlled by automatic daylight controls? (Y/N)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "4","itemno":"d.","test" : "System Information",
	                             			      		  "name":"Documented general lighting design footcandles. (Enter footcandle value or \"Unknown\" (U))"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "5","itemno":"e.","test" : "System Information",
	                             			      		  "name":"Power estimation method. Measured Amps Multiplied by Volts, Volt&dash;Amps (VA), alternate option is Measured Watts (W)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "6","itemno":"f.","test" : "Step 1: Identify Reference Location (location where minimum daylight illuminance is measured in zone served by the controlled lighting.)",
	                             			      		  "name":"Method Used: Illuminance or Distance? (I or D)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "7","itemno":"g.","test" : "Override daylight control system and drive electric lights to highest light level for the following:",
	                             			      		  "name":"Highest light level fc &dash; enter measured footcandles (fc) from controlled electric lighting (does not include daylight illuminance)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "8","itemno":"h.","test" : "Override daylight control system and drive electric lights to highest light level for the following:",
	                             			      		  "name":"Full load Highest light level power. Enter measured Amps times Volts, Volt\-Amps (VA) or measured Watts(W)."
	                             			      	 },
	                             			      	{
	                             			      		  "id": "9","itemno":"i.","test" : "Override daylight control system and drive electric lights to highest light level for the following:",
	                             			      		  "name":"Indicate whether this is Full Output (FO), or Task Tuned (Lumen Maintenance) (TT)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "10","itemno":"j.","test" : "Step 2: No Daylight Test controls enabled & daylight less than 1 fc at reference location",
	                             			      		  "name":"Method Used: Night time manual measurement (Night), Night Time Illuminance Logging (Log), Cover Fenestration (CF), Cover Open Loop Photosensor (COLP)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "11","itemno":"k.","test" : "Step 2: No Daylight Test controls enabled & daylight less than 1 fc at reference location",
	                             			      		  "name":"Reference Illuminance (footcandles) as measured at Reference Location (see Step 1). Enter footcandles"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "12","itemno":"l.","test" : "Step 2: No Daylight Test controls enabled & daylight less than 1 fc at reference location",
	                             			      		  "name":"Enter Y if either of the following statements are true:\
[Reference Illuminance (line j)] /[Highest light level fc (line g)] > 70% when line I = FO?90%? or\
[Reference Illuminance (line j)] / [design footcandles (line d)] > 80%? (Y/ N)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "13","itemno":"m.","test" : "Step 3: Full Daylight Test conducted when daylight greater than reference illuminance (line j)",
	                             			      		  "name":"Enter measured Amps Multiplied by Volts, Volt&dash;Amps (VA) or measured Watts (W)."
	                             			      	 },
	                             			      	{
	                             			      		  "id": "14","itemno":"n.","test" : "Step 3: Full Daylight Test conducted when daylight greater than reference illuminance (line j)",
	                             			      		  "name":"System power reduction enter [1 &dash; (line m)/(line h)] enter as percent."
	                             			      	 },
	                             			      	{
	                             			      		  "id": "15","itemno":"o.","test" : "Step 3: Full Daylight Test conducted when daylight greater than reference illuminance (line j)",
	                             			      		  "name":"Is System Power Reduction (line m) > 65% when line i = FO, or > 56% when line i = TT (Y/N)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "16","itemno":"p.","test" : "Step 3: Full Daylight Test conducted when daylight greater than reference illuminance (line j)",
	                             			      		  "name":"With uncontrolled lights also on, no lamps are dimmed outside of daylit zone by same control mechanism or formula (Y/N)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "17","itemno":"q.","test" : "Step 3: Full Daylight Test conducted when daylight greater than reference illuminance (line j)",
	                             			      		  "name":"Dimmed lamps have stable output (no perceptible visual flicker) (Y/N)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "18","itemno":"r.","test" : "Step 4: Partial Daylight Test conducted when daylight between 60% and 95% of (line k)",
	                             			      		  "name":"Daylight illuminance (light level without electric light) measured at Reference Location (fc)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "19","itemno":"s.","test" : "Step 4: Partial Daylight Test conducted when daylight between 60% and 95% of (line k)",
	                             			      		  "name":"Daylight illuminance divided by the Reference Illuminance = (line r )/ (line k). Enter %."
	                             			      	 },
	                             			      	{
	                             			      		  "id": "20","itemno":"t.","test" : "Step 4: Partial Daylight Test conducted when daylight between 60% and 95% of (line k)",
	                             			      		  "name":"Is Ratio of Daylight illuminance to Ref. illuminance (line s) between 60% and 95%? (Y/N)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "21","itemno":"u.","test" : "Step 4: Partial Daylight Test conducted when daylight between 60% and 95% of (line k)",
	                             			      		  "name":"Total (daylight + electric light) illuminance measured at the Reference Location (fc)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "22","itemno":"v.","test" : "Step 4: Partial Daylight Test conducted when daylight between 60% and 95% of (line k)",
	                             			      		  "name":"Total illuminance divided by the Reference Illuminance = (line u )/ (line k), Enter %"
	                             			      	 },
	                             			      	 {
	                             			      		  "id": "23","itemno":"w.","test" : "Step 4: Partial Daylight Test conducted when daylight between 60% and 95% of (line k)",
	                             			      		  "name":"Is Total illuminance divided by the Reference illuminance (line u) between 100% and 150%? (Y/N)"
	                             			      	 },
	                             			      	 {
	                             			      		  "id": "24","itemno":"","test" : "III. Evaluation :",
	                             			      		  "name":"All applicable Construction Inspection responses on pages 1 & 2 are complete and all applicable Functional Performance Testing Requirements responses are positive (Y &dash; yes) (applicable questions on pages 3 & 4 = c, l, o, p, q, t, w)"
	                             			      	 },
 	                             			      	],
 	                             			gridRowNumberTypeMap : 
 	                      			      			[
													{
														 "edittype":"select", "editoptions" : { "value" : ":;O:O;C:C"}
													},
													{
														 "edittype":"select", "editoptions" : { "value" : ":;M:M;V:V"}
													},{},
													{
														 "edittype":"text",editoptions: {
			                             						dataInit: function(element) {
				                             	                    $(element).keypress(function(e){
				                             	                    	 return isNumber(e, this);
				                             	                    });
			                             						}
			                             					},
													},
													{
														 "edittype":"text"
													}, //5
													{
														 "edittype":"select", "editoptions" : { "value" : ":;I:I;D:D"}
													},
													{
														 "edittype":"text",editoptions: {
																dataInit: function(element) {
																	$(element).keypress(function(e){
																	 return isNumber(e, this);
																	});
																}
															},
													},//7
													{
														 "edittype":"text"
													},
													{
														 "edittype":"select", "editoptions" : { "value" : ":;FO:FO;TT:TT"}
													},
													{
														 "edittype":"select", "editoptions" : { "value" : ":;Night:Night;Log:Log;CF:CF;COLP:COLP"}
													},
													{
														 "edittype":"text", editoptions: {
																dataInit: function(element) {
																	$(element).keypress(function(e){
																	 return isNumber(e, this);
																	});
																}
															},
													},//k
													{"editable":false},
													{
														 "edittype":"text",editoptions: {
																dataInit: function(element) {
																	$(element).keypress(function(e){
																	 return isNumber(e, this);
																	});
																}
															},
													},
													{"editable":false},//n
													{"editable":false},//o
													{},{},
													{
														 "edittype":"text",editoptions: {
																dataInit: function(element) {
																	$(element).keypress(function(e){
																	 return isNumber(e, this);
																	});
																}
															},
													},
													{"editable":false},//s
													{"editable":false},//t
													{
														 "edittype":"text",editoptions: {
																dataInit: function(element) {
																	$(element).keypress(function(e){
																	 return isNumber(e, this);
																	});
																}
															},
													},
													{
														"editable":false,"edittype":"text",editoptions: {
																dataInit: function(element) {
																	$(element).keypress(function(e){
																	 return isNumber(e, this);
																	});
																}
															},
													},{"editable":false},//w
													{"editable":false}
 	                      			      			]
 									},
 									{//DEPENDENT GRID 2
	                                     	name : "#functionalTestingSDSGrid",
	                             			grdObj : null,
	                             			parentGrid : null,
	                             			noofdefaultcolumns : 5,
	                             			colnames : ['ID','', '', 'Test', 'Step'],
	                             			groupby : ['test'],
	                             			columnshow : [false],
	                             			caption: '<h3>II. NA7.6.1.2.2 Functional Performance Testing &dash; Stepped Switching/ Stepped Dimming Systems</h3>\
	                             				<h4>Power estimation using watt&dash;meter or amp&dash;meter measurement</h4>',
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
	                             						    width: 400
	                             						}, {
	                             						    name: 'test',
	                             						    index: 'test',
	                             						    editable: true,
	                             						    hidden: false,
 	                             						 sorttype: function (cellValus, rowData) {
 	                             							 if (rowData.itemno == ''){
 	                             								 return 'z';
 	                             							 }
 	                             							if (rowData.itemno.startsWith("F")){
	                             								 return 'n.'+rowData.itemno;
	                             							 }
 	                             						    return rowData.itemno;
 	                             						},

	                             						    width: 50
	                             						}, {
	                             						    name: 'step',
	                             						    index: 'step',
	                             						    editable: false,
	                             						    hidden: true,
	                             						    width: 50
	                             						}],
	                             			parentRowDependentMap : new Object(),
	                             			currentMaxKeyParentRowDependentMap : 0,
	                             			data: 	[
	                             			      	{
	                             			      		  "id": "1","itemno":"a.","test" : "System Information",
	                             			      		  "name":"Control Loop Type. Open Loop or Closed Loop? (O or C)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "2","itemno":"b.","test" : "System Information",
	                             			      		  "name":"Indicate if Mandatory control &dash; M (required for skylit zone or primary sidelit zone with installed general lighting power > 120 W); or Voluntary &dash;V (M, V)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "3","itemno":"c.","test" : "System Information",
	                             			      		  "name":"If automatic daylighting controls are mandatory, are all general lighting luminaires in daylight zones controlled by automatic daylight controls? (Y/N)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "4","itemno":"d.","test" : "System Information",
	                             			      		  "name":"Power estimation method. Measured Watts (W), Measured Amps Multiplied by Volts, Volt&dash;Amps (VA),"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "5","itemno":"e.","test" : "Step 1: Identify Reference Location (location where minimum daylight illuminance is measured in zone served by the controlled lighting.)",
	                             			      		  "name":"Method Used: Illuminance or Distance? (I or D)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "6","itemno":"f.","test" : "Step 2: No Daylight Test (daylight less than 1 fc at reference location)",
	                             			      		  "name":"Method Used: Night time manual measurement (Night), Night Time Illuminance Logging (Log) attach plot of fc or power, Cover Fenestration (CF), Cover Photosensor (CP)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "7","itemno":"g.","test" : "Step 2: No Daylight Test (daylight less than 1 fc at reference location)",
	                             			      		  "name":"Reference Illuminance (foot&dash;candles) measured at Reference Location"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "8","itemno":"h.","test" : "Step 2: No Daylight Test (daylight less than 1 fc at reference location)",
	                             			      		  "name":"Enter measured Watts (W), or Amps Multiplied by Volts, Volt&dash;Amps (VA)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "9","itemno":"i.","test" : "Step 2: No Daylight Test (daylight less than 1 fc at reference location)",
	                             			      		  "name":"Indicate whether this is Full Output (FO), or Task Tuned (Lumen Maintenance) (TT)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "10","itemno":"j.","test" : "Step 3: Full Daylight Test conducted when daylight > 150% of reference illuminance (line g)",
	                             			      		  "name":"Measured Watts of Volt&dash;Amps &dash; record system power"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "11","itemno":"k.","test" : "Step 3: Full Daylight Test conducted when daylight > 150% of reference illuminance (line g)",
	                             			      		  "name":"System fraction of power reduction = [1&dash;(line j) / (line h)],"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "12","itemno":"l.","test" : "Step 3: Full Daylight Test conducted when daylight > 150% of reference illuminance (line g)",
	                             			      		  "name":"Is System Power Reduction (k) > 65% when line i = FO or >56% when line i = TT (Y/N)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "13","itemno":"m.","test" : "Step 4: Partial Daylight Test",
	                             			      		  "name":"Method Used: Light Logging (Log), Partially Cover Fenestration (PCF), Open Loop Setpoint Adjustment (OLSA)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "14","itemno":"n.","test" : "Step 4: Partial Daylight Test",
	                             			      		  "name":"If the control has three steps of control or less, all steps of control are tested. If the control has more than three steps, testing three steps of control is sufficient for showing compliance.Tests have been conducted at various daylight levels that correspond to steps of electric lighting control. (Y/N)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "15","itemno":"F1","test" : "First Stage of Control",
	                             			      		  "name":"Total (daylight + electric light) illuminance measured at the Reference Location (foot&dash;candles) when stage turns off or dims"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "16","itemno":"F2","test" : "First Stage of Control",
	                             			      		  "name":"Is the measured total illuminance between 100% and 150% of the Reference Illuminance (line g)? (Y/N)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "17","itemno":"F3","test" : "First Stage of Control",
	                             			      		  "name":"With time delay disabled, control stage does not cycle (i.e. deadband is sufficient)? (Y/N)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "18","itemno":"F4","test" : "Second Stage of Control",
	                             			      		  "name":"Total (daylight + electric light) illuminance measured at the Reference Location (foot&dash;candles) when stage turns off or dims"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "19","itemno":"F5","test" : "Second Stage of Control",
	                             			      		  "name":"Is the measured total illuminance between 100% and 150% of the Reference Illuminance (line g)? (Y/N)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "20","itemno":"F6","test" : "Second Stage of Control",
	                             			      		  "name":"With time delay disabled, control stage does not cycle (i.e. deadband is sufficient)? (Y/N)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "21","itemno":"F7","test" : "Third Stage of Control",
	                             			      		  "name":"Total (daylight + electric light) illuminance measured at the Reference Location (foot&dash;candles) when stage turns off or dims"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "22","itemno":"F8","test" : "Third Stage of Control",
	                             			      		  "name":"Is the measured total illuminance between 100% and 150% of the Reference Illuminance (line g)? (Y/N)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "23","itemno":"F9","test" : "Third Stage of Control",
	                             			      		  "name":"With time delay disabled, control stage does not cycle (i.e. deadband is sufficient)? (Y/N)"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "24","itemno":"r.","test" : "Step 5: Time Delay Test (conduct at least 60 minutes after overriding time delay)",
	                             			      		  "name":"After change of state from little daylight to full daylight, time in minutes before light output is reduced"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "25","itemno":"s.","test" : "Step 5: Time Delay Test (conduct at least 60 minutes after overriding time delay)",
	                             			      		  "name":"Is the measured time delay (line r) greater than or equal to 3 minutes? (Y/"
	                             			      	 },
	                             			      	{
	                             			      		  "id": "26","itemno":"","test" : "III.	PASS/FAIL Evaluation (check one):",
	                             			      		  "name":"<p>PASS: All applicable Construction Inspection responses on pages 1 &amp; 2 are complete and all applicable Functional Performance Testing Requirements responses are positive (Y &dash; yes) (applicable questions on pages 5 &amp; 6 are on lines c, l, n, F2, F3, F5, F6, F8, F9, s)</p>\
<p>FAIL: Any applicable Construction Inspection responses on pages 1 &amp; 2 are incomplete OR there is one or more negative (N &dash; no) responses in any applicable Functional Performance Testing Requirements section (applicable questions on pages 5 &amp; 6 are on lines c, l, n, F2, F3, F5, F6, F8, F9, s). System does not pass and is NOT eligible for Certificate of Occupancy according to Section 10&dash;103(a)3B. Fix problem(s) and retest until the system(s) passes all portions of this test before retesting and resubmitting NRCA&dash;LTI&dash;03&dash;A with PASSED test to the enforcement agency. Describe below the failure mode and corrective action needed.</p>"
	                             			      	 },
	                             			      	],
             			      	gridRowNumberTypeMap : 
	                            				[
		                             				{
		                             					 "edittype":"select", "editoptions" : { "value" : ":;O:O;C:C"}
		                             				},
		                             				{
		                             					 "edittype":"select", "editoptions" : { "value" : ":;M:M;V:V"}
		                             				},{},
		                             				{
		                             					 "edittype":"text"
		                             				},
		                             				{
		                             					 "edittype":"select", "editoptions" : { "value" : ":;I:I;D:D"}
		                             				},
		                             				{
		                             					 "edittype":"select", "editoptions" : { "value" : ":;Night:Night;Log:Log;CF:CF;COLP:COLP"}
		                             				},
		                             				{
		                             					 "edittype":"text",editoptions: {
																dataInit: function(element) {
																	$(element).keypress(function(e){
																	 return isNumber(e, this);
																	});
																}
															},
		                             				},
		                             				{
		                             					 "edittype":"text",editoptions: {
																dataInit: function(element) {
																	$(element).keypress(function(e){
																	 return isNumber(e, this);
																	});
																}
															},
		                             				},// h
		                             				{
		                             					 "edittype":"select", "editoptions" : { "value" : ":;FO:FO;TT:TT"}
		                             				},
		                             				{
		                             					 "edittype":"text",editoptions: {
																dataInit: function(element) {
																	$(element).keypress(function(e){
																	 return isNumber(e, this);
																	});
																}
															},
		                             				},//j
		                             				{"editable":false},//k
		                             				{"editable":false},//l
		                             				{
		                             					 "edittype":"select", "editoptions" : { "value" : ":;PCF:PCF;OLSA:OLSA"}// m completed
		                             				},{},
		                             				{
		                             					 "edittype":"text"
		                             				},//F1
		                             				{},{},
		                             				{
		                             					 "edittype":"text"
		                             				},{},{},
		                             				{
		                             					 "edittype":"text"
		                             				},
		                             				{},{},// F9
		                             				{
		                             					 "edittype":"text",editoptions: {
																dataInit: function(element) {
																	$(element).keypress(function(e){
																	 return isNumber(e, this);
																	});
																}
															},
		                             				},
		                             				{"editable":false},//s
		                             				{"editable":false}
	                             				],
 									},
 									{ //DEPENDENT GRID 3
                                     	name : "#functionalTestingCDSLMMGrid",
                             			grdObj : null,
                             			parentGrid : null,
                             			noofdefaultcolumns : 5,
                             			colnames : ['ID','', '', 'Test', 'Step'],
                             			groupby : ['test'],
                             			columnshow : [false],
                             			caption: '<h3>II. Functional Performance Testing &dash; Continuous Dimming Systems NA-7.6.1.2.1</h3>\
                             				<h4>Power estimation using light meter measurement</h4>',
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
                             						    width: 400
                             						}, {
                             						    name: 'test',
                             						    index: 'test',
                             						    editable: true,
                             						    hidden: false,
	                             						 sorttype: function (cellValus, rowData) {
	                             							 if (rowData.itemno == ''){
	                             								 return 'zz';
	                             							 }
	                             							if (rowData.itemno.length == 3){
	                             								return 'za'+rowData.itemno;
	                             							}
	                             						    return rowData.itemno;
	                             						},

                             						    width: 50
                             						}, {
                             						    name: 'step',
                             						    index: 'step',
                             						    editable: false,
                             						    hidden: true,
                             						    width: 50
                             						}],
                             			parentRowDependentMap : new Object(),
                             			currentMaxKeyParentRowDependentMap : 0,
                             			data: 	[ // DEPENDENT GRID 3
                             			      	 {
                             			      		  "id": "1","itemno":"a.","test" : "System Information",
                             			      		  "name":"Control Loop Type: Open Loop or Closed Loop? (O or C)"
                             			      	 },
                             			      	{
                             			      		  "id": "2","itemno":"b.","test" : "System Information",
                             			      		  "name":"<p>Indicate if Mandatory control ‐ M (required for skylit zone or primary sidelit zone with installed general lighting power &gt; 120 W);<br /> \
for Control Credit &ndash; CC; or Voluntary not for credit ‐V (M, CC, V)</p>"
                             			      	 },
                             			      	{
                             			      		  "id": "3","itemno":"c.","test" : "System Information",
                             			      		  "name":"If automatic daylighting controls are mandatory, are all general lighting luminaires in daylit zones controlled by automatic daylight controls? (Y/N)"
                             			      	 },
                             			      	{
                             			      		  "id": "4","itemno":"d.","test" : "System Information",
                             			      		  "name":"Documented general lighting design footcandles. (Enter footcandle value or \"Unknown\" (U))"
                             			      	 },
                             			      	{
                             			      		  "id": "5","itemno":"e.","test" : "System Information",
                             			      		  "name":"Power estimation method. (see line r) Default ratio of power to light (Dfc), cut&dash;sheet ratio of power to light (CSfc) If CSFc &dash; attach cut-sheet. Enter Dfc or CSfc,"
                             			      	 },
                             			      	{
                             			      		  "id": "6","itemno":"f.","test" : "Step 1: Identify Reference Location (location where minimum daylight illuminance is measured in zone served by the controlled lighting.)",
                             			      		  "name":"Method Used: Illuminance or Distance? (I or D)"
                             			      	 },
                             			      	{
                             			      		  "id": "7","itemno":"g.","test" : "Override daylight control system and drive electric lights to highest light level for the following:",
                             			      		  "name":"Highest light level fc &dash; enter measured footcandles (fc) from controlled electric lighting (does not include daylight illuminance)"
                             			      	 },
                             			      	{
                             			      		  "id": "8","itemno":"h.","test" : "Override daylight control system and drive electric lights to highest light level for the following:",
                             			      		  "name":"Indicate whether this is Full Output (FO), or Task Tuned (Lumen Maintenance) (TT)"
                             			      	 },
                             			      	{
                             			      		  "id": "9","itemno":"i.","test" : "Step 2: No Daylight Test",
                             			      		  "name":"Method Used: Night time manual measurement (Night), Night Time Illuminance Logging (Log), Cover Fenestration (CF), Cover Open Loop Photosensor (COLP)"
                             			      	 },
                             			      	{
                             			      		  "id": "10","itemno":"j.","test" : "Step 2: No Daylight Test",
                             			      		  "name":"Reference Illuminance (footcandles) measured at Reference Location (Illuminance of general lighting at the reference location)"
                             			      	 },
                             			      	{
                             			      		  "id": "11","itemno":"k.","test" : "Step 2: No Daylight Test",
                             			      		  "name":"Enter Y if either of the following statements are true:<br />\
If line h = FO; [Reference Illuminance (line i)] / [Full Output fc (line g)] &gt; 70%? or [Reference Illuminance (line i)] / [design footcandles (line d)] &gt; 80%? (Y/ N)"
                             			      	 },
                             			      	{
                             			      		  "id": "12","itemno":"l.","test" : "Step 3: Full Daylight Test conducted when daylight > reference illuminance (line i)",
                             			      		  "name":"Daylight illuminance (light level with electric lighting turned off) measured at Reference Location (fc)"
                             			      	 },
                             			      	{
                             			      		  "id": "13","itemno":"m.","test" : "Step 3: Full Daylight Test conducted when daylight > reference illuminance (line i)",
                             			      		  "name":"Daylight illuminance (line l) greater than Reference Illuminance (line j) ? (Y/N)"
                             			      	 },
                             			      	{
                             			      		  "id": "14","itemno":"n.","test" : "Step 3: Full Daylight Test conducted when daylight > reference illuminance (line i)",
                             			      		  "name":"Fraction controlled wattage turned off. Enter %."
                             			      	 },
                             			      	{
                             			      		  "id": "15","itemno":"o.","test" : "Step 3: Full Daylight Test conducted when daylight > reference illuminance (line i)",
                             			      		  "name":"Fraction of controlled wattage dimmed [1 &dash; ( line n)] Enter %."
                             			      	 },
                             			      	{
                             			      		  "id": "16","itemno":"p.","test" : "Fill out lines p through s only if fraction of controlled wattage turned off (line n) < 100%.",
                             			      		  "name":"Total (daylight + electric light) illuminance measured at the Reference Location (fc)"
                             			      	 },
                             			      	{
                             			      		  "id": "17","itemno":"q.","test" : "Fill out lines p through s only if fraction of controlled wattage turned off (line n) < 100%.",
                             			      		  "name":"Electric lighting illuminance at the Reference Location (fc) [(line p) &dash; (line l)]"
                             			      	 },
                             			      	{
                             			      		  "id": "18","itemno":"r.","test" : "Fill out lines p through s only if fraction of controlled wattage turned off (line n) < 100%.",
                             			      		  "name":"Electric lighting illuminance (line q) divided by Highest Light Level fc (line g). Enter %"
                             			      	 },
                             			      	{
                             			      		  "id": "19","itemno":"s.","test" : "Fill out lines p through s only if fraction of controlled wattage turned off (line n) < 100%.",
                             			      		  "name":"Dimmed luminaire fraction of rated power. Attach manufacturer's cut&dash;sheet or use default graph of rated power to light output on page 9. Label applicable control system (column A, B or C) on cut&dash;sheet or graph. Enter fraction of rated power in %."
                             			      	 },
                             			      	{
                             			      		  "id": "20","itemno":"t.","test" : "Fill out lines p through s only if fraction of controlled wattage turned off (line n) < 100%.",
                             			      		  "name":"System Power Reduction = [1 &dash; (line o) * (line s)]"
                             			      	 },
                             			      	{
                             			      		  "id": "21","itemno":"u.","test" : "Fill out lines p through s only if fraction of controlled wattage turned off (line n) < 100%.",
                             			      		  "name":"Is System Power Reduction (line t) > 65% when line h = FO, or > 56% when line h = TT (Y/N)"
                             			      	 },
                             			      	{
                             			      		  "id": "22","itemno":"v.","test" : "Fill out lines p through s only if fraction of controlled wattage turned off (line n) < 100%.",
                             			      		  "name":"With uncontrolled lights also on, no lamps dimmed outside of daylit zone by control (Y/N)"
                             			      	 },{
                             			      		  "id": "23","itemno":"w.","test" : "Fill out lines p through s only if fraction of controlled wattage turned off (line n) < 100%.",
                             			      		  "name":"Dimmed lamps have stable output, no perceptible flicker (Y/N)"
                             			      	 },
                             			      	{
                             			      		  "id": "24","itemno":"x.","test" : "Step 4: Partial Daylight Test conducted when daylight between 60% and 95% of (line i)",
                             			      		  "name":"Daylight illuminance (light level without electric light) measured at Reference Location (fc)"
                             			      	 },
                             			      	{
                            			      		  "id": "25","itemno":"y.","test" : "Step 4: Partial Daylight Test conducted when daylight between 60% and 95% of (line i)",
                            			      		  "name":"Daylight illuminance divided by the Reference Illuminance = (line x)/ (line j). Enter %"
                            			      	 },
                            			      	{
                            			      		  "id": "26","itemno":"z.","test" : "Step 4: Partial Daylight Test conducted when daylight between 60% and 95% of (line i)",
                            			      		  "name":"Is Ratio of Daylight illuminance to Ref illuminance (line y) between 60% and 95%? (Y/N)"
                            			      	 },
                            			      	{
                            			      		  "id": "27","itemno":"aa.","test" : "Step 4: Partial Daylight Test conducted when daylight between 60% and 95% of (line i)",
                            			      		  "name":"Total (daylight + electric light) illuminance measured at the Reference Location (fc)"
                            			      	 },
                            			      	{
                            			      		  "id": "28","itemno":"bb.","test" : "Step 4: Partial Daylight Test conducted when daylight between 60% and 95% of (line i)",
                            			      		  "name":"Total illuminance divided by the Reference Illuminance = (line aa )/ (line j). Enter %"
                            			      	 },
                            			      	{
                            			      		  "id": "29","itemno":"cc.","test" : "Step 4: Partial Daylight Test conducted when daylight between 60% and 95% of (line i)",
                            			      		  "name":"Is Ratio of Total illum. to Reference illum. (line bb) between 100% and 150%? (Y/N)"
                            			      	 },
                            			      	{
                            			      		  "id": "30","itemno":"","test" : "III.	PASS/FAIL Evaluation (check one):",
                            			      		  "name":"<p>All applicable Construction Inspection responses on pages 1 &amp; 2 are complete and all applicable FunctionalPerformance Testing Requirements responses are positive (Y &dash; yes) (applicable questions on pages 7 &amp; 8 = c, k, m, u, v, w, z, cc)</p>\
<p>FAIL: Any applicable Construction Inspection responses on pages 1 &amp; 2 are incomplete OR there is one or more negative (N &dash; no) responses in any applicable Functional Performance Testing Requirements section (applicable questions on pages 7 &amp; 8= c, k, m, u, v, w, z, cc). System does not pass and is NOT eligible for Certificate of Occupancy according to Section 10&dash;103(a)3B. Fix problem(s) and retest until the system(s) passes all portions of this test before retesting and resubmitting NRCA&dash;LTI&dash;03&dash;A with PASSED test to the enforcement agency. Describe below the failure mode and corrective action needed.</p>"
                            			      	 },
                             			      	],
	                             			gridRowNumberTypeMap : 
	                      			      	[ // DEPENDENT GRID 3
												{
													 "edittype":"select", "editoptions" : { "value" : ":;O:O;C:C"}
												},
												{
													 "edittype":"select", "editoptions" : { "value" : ":;M:M;CC:CC;V:V"}
												},{},
												{
													 "edittype":"text",editoptions: {
															dataInit: function(element) {
																$(element).keypress(function(e){
																 return isNumber(e, this);
																});
															}
														},
												},//d
												{
													"edittype":"select", "editoptions" : { "value" : ":;Dfc:Dfc;Csfc:Csfc"}
												},
												{
													 "edittype":"select", "editoptions" : { "value" : ":;I:I;D:D"}
												},
												{
													 "edittype":"text",editoptions: {
															dataInit: function(element) {
																$(element).keypress(function(e){
																 return isNumber(e, this);
																});
															}
														},
												},//g
												{
													 "edittype":"select", "editoptions" : { "value" : ":;FO:FO;TT:TT"} // h completed
												},
												{
													 "edittype":"select", "editoptions" : { "value" : ":;Night:Night;Log:Log;CF:CF;COLP:COLP"}
												},
												{
													 "edittype":"text",editoptions: {
															dataInit: function(element) {
																$(element).keypress(function(e){
																 return isNumber(e, this);
																});
															}
														},
												},{"editable": false},//k completed
												{"edittype":"text",editoptions: {
													dataInit: function(element) {
														$(element).keypress(function(e){
														 return isNumber(e, this);
														});
													}
												},},//l
												{"editable": false},{"edittype":"text",editoptions: {
													dataInit: function(element) {
														$(element).keypress(function(e){
														 return isNumber(e, this);
														});
													}
												},},{"editable": false}//o
												,{"edittype":"text",editoptions: {
													dataInit: function(element) {
														$(element).keypress(function(e){
														 return isNumber(e, this);
														});
													}
												},},//p
												{"editable": false},{"editable": false},// r
												{"edittype":"text",editoptions: {
													dataInit: function(element) {
														$(element).keypress(function(e){
														 return isNumber(e, this);
														});
													}
												},},{"editable": false},//t
												{"editable": false},{},{}, //w
												{"edittype":"text",editoptions: {
													dataInit: function(element) {
														$(element).keypress(function(e){
														 return isNumber(e, this);
														});
													}
												},},//x
												{"editable": false},{"editable": false},//z
												{"edittype":"text",editoptions: {
													dataInit: function(element) {
														$(element).keypress(function(e){
														 return isNumber(e, this);
														});
													}
												},},{"editable": false},{"editable": false},
												{"editable": false},
                      			      		]
 									},
 									{ // DEPENDENT GRID 4
 										name : "#functionalTestingSDSLMMGrid",
                             			grdObj : null,
                             			parentGrid : null,
                             			noofdefaultcolumns : 5,
                             			colnames : ['ID','', '', 'Test', 'Step'],
                             			groupby : ['test'],
                             			columnshow : [false],
                             			caption: '<h3>II. NA7.6.1.2.2 Functional Performance Testing &dash; Stepped Switching/ Stepped Dimming Systems</h3>\
                             				<h4>Power estimation based on light output</h4>',
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
                             						    width: 400
                             						}, {
                             						    name: 'test',
                             						    index: 'test',
                             						    editable: true,
                             						    hidden: false,
	                             						 sorttype: function (cellValus, rowData) {
	                             							 if (rowData.itemno == ''){
	                             								 return 'zz';
	                             							 }
	                             							if (rowData.itemno.startsWith("F")){
	                             								 return 'r.'+rowData.itemno;
	                             							 }
	                             							if (rowData.itemno.length == 3){
	                             								return 'za'+rowData.itemno;
	                             							}
	                             						    return rowData.itemno;
	                             						},

                             						    width: 50
                             						}, {
                             						    name: 'step',
                             						    index: 'step',
                             						    editable: false,
                             						    hidden: true,
                             						    width: 50
                             						}],
                             			parentRowDependentMap : new Object(),
                             			currentMaxKeyParentRowDependentMap : 0,
                             			data: 	[// DEPENDENT GRID 4
                             			      	{
                             			      		  "id": "1","itemno":"a.","test" : "System Information",
                             			      		  "name":"Open Loop or Closed Loop? (O or C)"
                             			      	 },
                             			      	{
                             			      		  "id": "2","itemno":"b.","test" : "System Information",
                             			      		  "name":"Indicate if Mandatory control &dash; M (skylit zone or primary sidelit zone with installed general lighting power > 120 W));\
for Control Credit &dash; CC; or Voluntary not for credit &dash;V (M, CC, V)"
                             			      	 },
                             			      	{
                             			      		  "id": "3","itemno":"c.","test" : "System Information",
                             			      		  "name":"If automatic daylighting controls are mandatory, are all general lighting luminairesin daylight zones controlled by automatic daylight controls? (Y/N)"
                             			      	 },
                             			      	{
                             			      		  "id": "4","itemno":"d.","test" : "System Information",
                             			      		  "name":"Power estimation method. Counting (C) &dash; not allowed for step dimming, Counting plus Cut Sheet (C+CS) attach ballast cut sheet with steps of power and light."
                             			      	 },
                             			      	{
                             			      		  "id": "5","itemno":"e.","test" : "Step 1: Identify Reference Location (location where minimum daylight illuminance is measured in zone served by the controlled lighting.)",
                             			      		  "name":"Method Used: Illuminance or Distance? (I or D)"
                             			      	 },
                             			      	{
                             			      		  "id": "6","itemno":"f.","test" : "Step 2: No Daylight Test",
                             			      		  "name":"Method Used: Night time manual measurement (Night), Night Time Illuminance Logging (Log) attach plot of fc or power, Cover Fenestration (CF), Cover Photosensor (CP)"
                             			      	 },
                             			      	{
                             			      		  "id": "7","itemno":"g.","test" : "Step 2: No Daylight Test",
                             			      		  "name":"Reference Illuminance (foot&dash;candles) measured at Reference Location"
                             			      	 },
                             			      	{
                             			      		  "id": "8","itemno":"h.","test" : "Step 2: No Daylight Test",
                             			      		  "name":"Indicate whether this is Full Output (FO), or Task Tuned (Lumen Maintenance) (TT)"
                             			      	 },
                             			      	{
                             			      		  "id": "9","itemno":"i.","test" : "Step 3: Full Daylight Test conducted when daylight > 150 percent of reference illuminance (line g)",
                             			      		  "name":"Fraction system wattage turned off"
                             			      	 },
                             			      	{
                             			      		  "id": "10","itemno":"j.","test" : "Step 3: Full Daylight Test conducted when daylight > 150 percent of reference illuminance (line g)",
                             			      		  "name":"Fraction of system wattage dimmed"
                             			      	 },
                             			      	{
                             			      		  "id": "11","itemno":"k.","test" : "Step 3: Full Daylight Test conducted when daylight > 150 percent of reference illuminance (line g)",
                             			      		  "name":"Step dimming level as a fraction of rated light output if applicable"
                             			      	 },
                             			      	{
                             			      		  "id": "12","itemno":"l.","test" : "Step 3: Full Daylight Test conducted when daylight > 150 percent of reference illuminance (line g)",
                             			      		  "name":"Dimmed ballast fraction of rated power from cut&dash;sheet"
                             			      	 },
                             			      	{
                             			      		  "id": "13","itemno":"m.","test" : "Step 3: Full Daylight Test conducted when daylight > 150 percent of reference illuminance (line g)",
                             			      		  "name":"System Power Reduction = [1 &dash; (line j)*(line l)]"
                             			      	 },
                             			      	{
                             			      		  "id": "14","itemno":"n.","test" : "Step 3: Full Daylight Test conducted when daylight > 150 percent of reference illuminance (line g)",
                             			      		  "name":"Is System Power Reduction (line m) > 65% when line i = FO or >56% when line i = TT (Y/N)"
                             			      	 },
                             			      	{
                             			      		  "id": "15","itemno":"o.","test" : "Step 3: Full Daylight Test conducted when daylight > 150 percent of reference illuminance (line g)",
                             			      		  "name":"With uncontrolled lights also on, no lamps controlled outside of daylit zone (Y/N)"
                             			      	 },
                             			      	{
                             			      		  "id": "16","itemno":"p.","test" : "Step 3: Full Daylight Test conducted when daylight > 150 percent of reference illuminance (line g)",
                             			      		  "name":"Dimmed lamps have stable output, no perceptible visual flicker (Y/N)"
                             			      	 },
                             			      	{
                             			      		  "id": "17","itemno":"q.","test" : "Step 4: Partial Daylight Test",
                             			      		  "name":"Method Used: Light Logging (Log), Partially Cover Fenestration (PCF), Open Loop Setpoint Adjustment (OLSA)"
                             			      	 },
                             			      	{
                             			      		  "id": "18","itemno":"r.","test" : "Step 4: Partial Daylight Test",
                             			      		  "name":"If the control has three steps of control or less, all steps of control are tested. If the control has more than three steps, testing three steps of control is sufficient for showing compliance. Tests have been conducted at various daylight levels that correspond to steps of electric lighting control. (Y/N)"
                             			      	 },
                             			      	{
                            			      		  "id": "19","itemno":"F1","test" : "First Stage of Control",
                            			      		  "name":"Total (daylight + electric light) illuminance measured at the Reference Location (foot&dash;candles) when stage turns off or dims"
                            			      	 },
                            			      	{
                            			      		  "id": "20","itemno":"F2","test" : "First Stage of Control",
                            			      		  "name":"Is the measured total illuminance between 100% and 150% of the Reference Illuminance (line g)? (Y/N)"
                            			      	 },
                            			      	{
                            			      		  "id": "21","itemno":"F3","test" : "First Stage of Control",
                            			      		  "name":"With time delay disabled, control stage does not cycle (i.e. deadband is sufficient)? (Y/N)"
                            			      	 },
                            			      	{
                            			      		  "id": "22","itemno":"F4","test" : "Second Stage of Control",
                            			      		  "name":"Total (daylight + electric light) illuminance measured at the Reference Location (foot&dash;candles) when stage turns off or dims"
                            			      	 },
                            			      	{
                            			      		  "id": "23","itemno":"F5","test" : "Second Stage of Control",
                            			      		  "name":"Is the measured total illuminance between 100% and 150% of the Reference Illuminance (line g)? (Y/N)"
                            			      	 },
                            			      	{
                            			      		  "id": "24","itemno":"F6","test" : "Second Stage of Control",
                            			      		  "name":"With time delay disabled, control stage does not cycle (i.e. deadband is sufficient)? (Y/N)"
                            			      	 },
                            			      	{
                            			      		  "id": "25","itemno":"F7","test" : "Third Stage of Control",
                            			      		  "name":"Total (daylight + electric light) illuminance measured at the Reference Location (foot&dash;candles) when stage turns off or dims"
                            			      	 },
                            			      	{
                            			      		  "id": "26","itemno":"F8","test" : "Third Stage of Control",
                            			      		  "name":"Is the measured total illuminance between 100% and 150% of the Reference Illuminance (line g)? (Y/N)"
                            			      	 },
                            			      	{
                            			      		  "id": "27","itemno":"F9","test" : "Third Stage of Control",
                            			      		  "name":"With time delay disabled, control stage does not cycle (i.e. deadband is sufficient)? (Y/N)"
                            			      	 },
                             			      	{
                             			      		  "id": "28","itemno":"s.","test" : "Step 5: Time Delay Test (conduct at least 60 minutes after overriding time delay)",
                             			      		  "name":"After change of state from little daylight to full daylight, time in minutes before light output is reduced"
                             			      	 },
                             			      	{
                             			      		  "id": "29","itemno":"t.","test" : "Step 5: Time Delay Test (conduct at least 60 minutes after overriding time delay)",
                             			      		  "name":"Is the measured time delay (line s) greater than or equal to 3 minutes? (Y/N)"
                             			      	 },
                             			      	{
                             			      		  "id": "30","itemno":"","test" : "III.	PASS/FAIL Evaluation (check one):",
                             			      		  "name":"<p>PASS: All applicable Construction Inspection responses on pages1 &amp; 2 are complete and all applicable FunctionalPerformance Testing Requirements responses are positive (Y &dash; yes) (applicable questions on pages 10 &amp; 11 are on lines c, n, o, p, r, F2, F3, F5, F6, F8, F9, t)<br />\
FAIL: Any applicable Construction Inspection responses on pages1 &amp; 2 are incomplete OR there is one or more negative (N &dash; no) responses in any applicable Functional Performance Testing Requirements section (applicable questions on pages 10 &amp; 11 are on lines c, n, o, p, r, F2, F3, F5, F6, F8, F9, t). System does not pass and is NOT eligible for Certificate of Occupancy according to Section 10&dash;103(a)3B. Fix problem(s) and retest until the system(s) passes all portions of this test before retesting and resubmitting NRCA&dash;LTI&dash;03&dash;A with PASSED test to the enforcement agency. Describe below the failure mode and corrective action needed.</p>"
                             			      	 },
                             			      	],
                     			      	gridRowNumberTypeMap : 
                     						[// DEPENDENT GRID 4
	                             				{
	                             					 "edittype":"select", "editoptions" : { "value" : ":;O:O;C:C"}
	                             				},
	                             				{
	                             					 "edittype":"select", "editoptions" : { "value" : ":;M:M;CC:CC;V:V"}
	                             				},{},
	                             				{
	                             					"edittype":"select", "editoptions" : { "value" : ":;C:C;C+CS:C+CS"} //d
	                             				},
	                             				{"edittype":"select", "editoptions" : { "value" : ":;I:I;D:D"}},
	                             				{"edittype":"select", "editoptions" : { "value" : ":;Night:Night;Log:Log;CF:CF;COLP:COLP"}},
	                             				{"edittype":"text"},//g
	                             				{
	                             					 "edittype":"select", "editoptions" : { "value" : ":;FO:FO;TT:TT"}
	                             				},//h
	                             				{"edittype":"text",editoptions: {
													dataInit: function(element) {
														$(element).keypress(function(e){
														 return isNumber(e, this);
														});
													}
												}},{"edittype":"text",editoptions: {
													dataInit: function(element) {
														$(element).keypress(function(e){
														 return isNumber(e, this);
														});
													}
												}},{"edittype":"text",editoptions: {
													dataInit: function(element) {
														$(element).keypress(function(e){
														 return isNumber(e, this);
														});
													}
												}},{"edittype":"text",editoptions: {
													dataInit: function(element) {
														$(element).keypress(function(e){
														 return isNumber(e, this);
														});
													}
												}},
	                             				{"editable":false},//m
	                             				{"editable":false},{},{},//p
	                             				{"edittype":"select", "editoptions" : { "value" : ":;PCF:PCF;OLSA:OLSA"}},{},//r
	                             				{"edittype":"text",editoptions: {
													dataInit: function(element) {
														$(element).keypress(function(e){
														 return isNumber(e, this);
														});
													}
												}},{"editable":false},//t
	                             				{},{"edittype":"text"},{},{},{"edittype":"text"},{},{},//F9
	                             				{"edittype":"text"},{},{"editable":false}
	                             				
                             				]
 									}
 									];


function dependentAutoDLGridsInitiate(){
	for (var i = 0 ; i < autoDLControlSystemDependents.length; i++){
		var gridArrObj =autoDLControlSystemDependents[i]; 
		gridArrObj.grdObj = $(gridArrObj.name);
	}
	//@TODO Add autoDLControlSystemDependents.gridRowNumberTypeMap if they are not by default select type
	// gridRowNumberTypeMap[21] = $.parseJSON('{"edittype":"text"}');
//	autoDLControlSystemDependents[0].gridRowNumberTypeMap[1] = 
//		$.parseJSON('{"edittype":"select", "editoptions" : { "value" : ":;OL:OL;CL:CL"}}');
}
function dependentAutoDLGridsSaveAfterSubmit(){
	   
	   var gridArrObj = autoDLControlSystemDependents[0];
	   saveAllRowsInGrid(gridArrObj.grdObj);
	   evaluateConstructionInspection();
	   gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	   grdjsonstr = JSON.stringify(gridArrObj.data);
	   $("#autodldependentspacecontrol").val(grdjsonstr);
	   //IF the upper grid is edited during submit then the lower test result grid needs to be redraw the latest value.
	   
	   gridArrObj = autoDLControlSystemDependents[1];
	   saveAllRowsInGrid(gridArrObj.grdObj);
	   evaluateFunctionalTestCDS();
	   gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	   grdjsonstr = JSON.stringify(gridArrObj.data);
	   $("#autodlfunctionaltestingcds").val(grdjsonstr);
	   
	   gridArrObj = autoDLControlSystemDependents[2];
	   saveAllRowsInGrid(gridArrObj.grdObj);
	   evaluateFunctionalTestSDS();
	   gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	   grdjsonstr = JSON.stringify(gridArrObj.data);
	   $("#autodlfunctionaltestingsds").val(grdjsonstr);
	   
	   gridArrObj = autoDLControlSystemDependents[3];
	   saveAllRowsInGrid(gridArrObj.grdObj);
	   evaluateFunctionalTestCDSLMM();
	   gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	   grdjsonstr = JSON.stringify(gridArrObj.data);
	   $("#autodlfunctionaltestingcdslmm").val(grdjsonstr);
	   
	   gridArrObj = autoDLControlSystemDependents[4];
	   saveAllRowsInGrid(gridArrObj.grdObj);
	   evaluateFunctionalTestSDSLMM();
	   gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	   grdjsonstr = JSON.stringify(gridArrObj.data);
	   $("#autodlfunctionaltestingsdslmm").val(grdjsonstr);
	   
}
function dependentAutoDLGridsSetParentGrid(grid){
	for (var i = 0 ; i < autoDLControlSystemDependents.length; i++){
		var gridArrObj =autoDLControlSystemDependents[i]; 
		gridArrObj.parentGrid = grid;
	}
}
function renderAutoDLControlSystem(sampledata) {
    var lastsel2;
    dependentAutoDLGridsInitiate();
    var grid = $(autoDayLightControlSystemGrid[0].name);
    if (sampledata != undefined || sampledata != null){
    	autoDayLightControlSystemGrid[0].data = $.parseJSON($("#autoDayLightControlSystemGridData").val());
    }
    dependentAutoDLGridsSetParentGrid(grid);
    
    var d = $.parseJSON($("#autodlcontrolsystem").val());
    console.log("DB DAta::"+JSON.stringify(d));
    grid.jqGrid({
        datatype: "local",
        data: d,
        colNames: autoDayLightControlSystemGrid[0].colNames,
        colModel: autoDayLightControlSystemGrid[0].colModels,
        onSelectRow: function(id) {
            if (id && id != lastsel2) {
                lastsel2 = id;
            }
            saveRowAutoDayLightControlSystemGrid(grid, lastsel2);
            grid.jqGrid('editRow', id, true);
        },
        cellattr: function(rowId, tv, rawObject, cm, rdata) {
            return 'style="white-space: normal!important"';
        },
        search: true,
        //pager: autoDayLightControlSystemGrid[0].pager,
        jsonReader: {
            cell: ""
        },
        gridComplete : function(){
        },
        rowNum: 10000,
        editurl: 'clientArray',
        rowList: [10000],
        sortabel : false,
        sortname: 'id',
        sortorder: 'asc',
        viewrecords: true,
        height: "100%",
        caption: autoDayLightControlSystemGrid[0].caption
    });
//    grid.jqGrid('navGrid', autoDayLightControlSystemGrid[0].pager, {
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

    
    
    $(autoDayLightControlSystemGrid[0].save).click(function() {
    	$(autoDayLightControlSystemGrid[0].add).attr("disabled", false);
        return saveRowAutoDayLightControlSystemGrid(grid);
    });

    $(autoDayLightControlSystemGrid[0].add).click(function() {
        var record = grid.jqGrid('getGridParam', 'records');
        var newrowid = record + 1;
        dependentGridsBeforeAutoDLRowAdded(newrowid);
        var addNewparameters = {
            rowID: autoDLControlSystemDependents[0].currentMaxKeyParentRowDependentMap,
            initdata: {
                id: autoDLControlSystemDependents[0].currentMaxKeyParentRowDependentMap,
                no: autoDLControlSystemDependents[0].currentMaxKeyParentRowDependentMap,
                name: ''
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
        $(autoDayLightControlSystemGrid[0].save).attr("disabled", false);
        $(autoDayLightControlSystemGrid[0].add).attr("disabled", true);
        dependentGridsAfterAutoDLRowAdded();
        dependentGridsAutoDLRedraw();
    });
    dependentGridsAutoDLPopulateFromDB();
    dependentGridsAutoDLRedraw();
    
    //Render the dr table
    renderDRSpaceGrid();
}
function dependentGridsAutoDLRedraw(){
	renderSensorControlGrid();
	renderFunctionTestCDSGrid();
	renderFunctionTestSDSGrid();
	renderFunctionTestCDSLMMGrid();
	renderFunctionTestSDSLMMGrid();
}
function renderFunctionTestSDSLMMGrid(){
	var gridArrObj = autoDLControlSystemDependents[4];
	var record = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
	var parentRecords = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
    var grid = gridArrObj.grdObj;
    if (record != null && record != 'undefined' && record >= 0) {
        $.jgrid.gridUnload(gridArrObj.name);
        grid = $(gridArrObj.name);
        gridArrObj.grdObj = grid;
    }
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
                if (lastsel2 != null && lastsel2 != 'undefined'  && lastsel2 != 0) {
                    var rowDataSave = grid.getRowData(lastsel2);
                    gridArrObj.data = grid.jqGrid('getRowData');
                    saveRowFunctionalTestSDSLMMGrid(lastsel2);
                    grid.jqGrid('restoreRow', lastsel2);
                }
                var rowNumberTypeDetails = gridArrObj.gridRowNumberTypeMap[id-1];
                console.log('For row no:'+id+':rowNumberTypeDetails:'+JSON.stringify(rowNumberTypeDetails));
                console.log('gridArrObj.data['+(id-1)+']:'+ JSON.stringify(gridArrObj.data[(id-1)]) +':RowDataToSave:'+JSON.stringify(grid.getRowData(id)));
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
    gridArrObj.grdObj.jqGrid('setGroupHeaders', {
    	  useColSpanStyle: true,   
    	  groupHeaders:[{startColumnName: 'itemno', numberOfColumns: 2, titleText: 'Complete all tests on page 10 & 11 (No Daylight Test, Full Daylight Test, and Partial Daylight Test) and fill out Pass/Fail section on Page 11.'}]
    	});
    gridArrObj.grdObj.jqGrid('setGroupHeaders', {
    	  useColSpanStyle: true,   
    	  groupHeaders:[{startColumnName: 1, numberOfColumns: parentRecords, titleText: 'Applicable Control Systems'}]
    	});
}


function renderFunctionTestCDSLMMGrid(){
	
	var gridArrObj = autoDLControlSystemDependents[3];
	var record = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
	var parentRecords = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
    var grid = gridArrObj.grdObj;
    
    if (record != null && record != 'undefined' && record >= 0) {
        $.jgrid.gridUnload(gridArrObj.name);
        grid = $(gridArrObj.name);
        gridArrObj.grdObj = grid;
    }
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
                if (lastsel2 != null && lastsel2 != 'undefined'  && lastsel2 != 0) {
                    var rowDataSave = grid.getRowData(lastsel2);
                    gridArrObj.data = grid.jqGrid('getRowData');
                    saveRowFunctionalTestCDSLMMGrid(lastsel2);
                    grid.jqGrid('restoreRow', lastsel2);
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
    gridArrObj.grdObj.jqGrid('setGroupHeaders', {
    	  useColSpanStyle: true,   
    	  groupHeaders:[{startColumnName: 'itemno', numberOfColumns: 2, titleText: 'Complete all tests on page 7 & 8 (No Daylight Test, Full Daylight Test, and Partial Daylight Test) and fill out Pass/Fail section on Page 8.'}]
    	});
    gridArrObj.grdObj.jqGrid('setGroupHeaders', {
    	  useColSpanStyle: true,   
    	  groupHeaders:[{startColumnName: 1, numberOfColumns: parentRecords, titleText: 'Applicable Control Systems'}]
    	});
}
function renderFunctionTestSDSGrid(){
	
	var gridArrObj = autoDLControlSystemDependents[2];
	var record = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
	var parentRecords = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
    var grid = gridArrObj.grdObj;
    
    if (record != null && record != 'undefined' && record >= 0) {
        $.jgrid.gridUnload(gridArrObj.name);
        grid = $(gridArrObj.name);
        gridArrObj.grdObj = grid;
    }
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
                if (lastsel2 != null && lastsel2 != 'undefined'  && lastsel2 != 0) {
                    var rowDataSave = grid.getRowData(lastsel2);
                    gridArrObj.data = grid.jqGrid('getRowData');
                    saveRowFunctionalTestSDSGrid(lastsel2);
                    grid.jqGrid('restoreRow', lastsel2);
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
    gridArrObj.grdObj.jqGrid('setGroupHeaders', {
    	  useColSpanStyle: true,   
    	  groupHeaders:[{startColumnName: 'itemno', numberOfColumns: 2, titleText: 'Complete all tests on pages 5 & 6 (No Daylight Test, Full Daylight Test, and Partial Daylight Test) and fill out Pass/Fail section on Page 6.'}]
    	});
    gridArrObj.grdObj.jqGrid('setGroupHeaders', {
    	  useColSpanStyle: true,   
    	  groupHeaders:[{startColumnName: 1, numberOfColumns: parentRecords, titleText: 'Applicable Control Systems'}]
    	});
}
function renderFunctionTestCDSGrid(){
	
	var gridArrObj = autoDLControlSystemDependents[1];
	var record = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
	var parentRecords = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
    var grid = gridArrObj.grdObj;
    
    if (record != null && record != 'undefined' && record >= 0) {
        $.jgrid.gridUnload(gridArrObj.name);
        grid = $(gridArrObj.name);
        gridArrObj.grdObj = grid;
    }
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
                if (lastsel2 != null && lastsel2 != 'undefined'  && lastsel2 != 0) {
                    var rowDataSave = grid.getRowData(lastsel2);
                    gridArrObj.data = grid.jqGrid('getRowData');
                    saveRowFunctionalTestCDSGrid(lastsel2);
                    grid.jqGrid('restoreRow', lastsel2);
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
    gridArrObj.grdObj.jqGrid('setGroupHeaders', {
    	  useColSpanStyle: true,   
    	  groupHeaders:[{startColumnName: 'itemno', numberOfColumns: 2, titleText: 'Complete all tests on page 3 & 4 (No Daylight Test, Full Daylight Test, and Partial Daylight Test) and fill out Pass/Fail section on Page 4.'}]
    	});
    gridArrObj.grdObj.jqGrid('setGroupHeaders', {
    	  useColSpanStyle: true,   
    	  groupHeaders:[{startColumnName: 1, numberOfColumns: parentRecords, titleText: 'Applicable Control Systems'}]
    	});
}

function saveRowSensorControlGrid(id){
	var gridArrObj = autoDLControlSystemDependents[0];
	gridArrObj.grdObj.jqGrid('saveRow', id, false, 'clientArray');
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	console.log('Data Row saved:'+JSON.stringify(gridArrObj.data[id-1]));
	evaluateConstructionInspection();
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	saveRowFunctionalTestCDSGrid(id);
}

function saveRowFunctionalTestCDSGrid(id){
	var gridArrObj = autoDLControlSystemDependents[1];
	gridArrObj.grdObj.jqGrid('saveRow', id, false, 'clientArray');
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	evaluateFunctionalTestCDS();
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	saveRowFunctionalTestSDSGrid(id);
}
function saveRowFunctionalTestSDSLMMGrid(id){
	var gridArrObj = autoDLControlSystemDependents[4];
	gridArrObj.grdObj.jqGrid('saveRow', id, false, 'clientArray');
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	evaluateFunctionalTestSDSLMM();
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
}
function saveRowFunctionalTestCDSLMMGrid(id){
	var gridArrObj = autoDLControlSystemDependents[3];
	gridArrObj.grdObj.jqGrid('saveRow', id, false, 'clientArray');
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	evaluateFunctionalTestCDSLMM();
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	saveRowFunctionalTestSDSLMMGrid(id);
}
function saveRowFunctionalTestSDSGrid(id){
	var gridArrObj = autoDLControlSystemDependents[2];
	gridArrObj.grdObj.jqGrid('saveRow', id, false, 'clientArray');
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	evaluateFunctionalTestSDS();
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	saveRowFunctionalTestCDSLMMGrid(id);
}
function evaluateFunctionalTestSDSLMM(){
	//PASS of earlier grid is important to evaluate here..
	var gridArrObj0 = autoDLControlSystemDependents[0];
	var currGridRecords0 = gridArrObj0.grdObj.jqGrid('getGridParam', 'records');
	
	var gridArrObj1 = autoDLControlSystemDependents[1];
	var currGridRecords1 = gridArrObj1.grdObj.jqGrid('getGridParam', 'records');
	
	var gridArrObj2 = autoDLControlSystemDependents[2];
	var currGridRecords2 = gridArrObj2.grdObj.jqGrid('getGridParam', 'records');
	
	var gridArrObj3 = autoDLControlSystemDependents[3];
	var currGridRecords3 = gridArrObj3.grdObj.jqGrid('getGridParam', 'records');
	
	//Get the data first 
	var gridArrObj = autoDLControlSystemDependents[4];
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	var parentGridRecords = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	var currGridRecords = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
	var subgridgroupsdata = $(gridArrObj.name).jqGrid("getGridParam", "groupingView").groups;
	var maingroupinfo = (subgridgroupsdata).filter(function (e){
        return e.idx == 0;
    });
	//Filter the subgroid groups data and find out row numbers 
	var passCheckMap = {'c.':'Y', 'n.':'Y', 'o.':'Y', 'p.':'Y', 'r.':'Y', 'F2':'Y', 'F3':'Y', 'F5':'Y', 'F6':'Y', 'F8':'Y', 'F9':'Y', 't.':'Y'};
	for (var i = 0; i < parentGridRecords; i++){
		var lastrow = 0;
		var colname = (i+1);
		evaluateDG4Rows(gridArrObj, colname);
		for (var j = 0; j < maingroupinfo.length - 1; j++ ){  
			var startRow = maingroupinfo[j].startRow; //Above group should pass to make pass the lower group.//Remove lastrow as well if dont not want
			lastrow = lastrow + maingroupinfo[j].cnt;
			for (var cnt =0; cnt < (lastrow - startRow); cnt++){
				var colname = (i+1);
				var rowData = gridArrObj.data[startRow  +  cnt];
				var val = rowData[colname];
				var valChkRef = passCheckMap[rowData.itemno];
				if (gridArrObj0.data[currGridRecords0 - 1][colname] != 'PASS'){
					gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
					break;
				}
				if (gridArrObj1.data[currGridRecords1 - 1][colname] != 'PASS'){
					gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
					break;
				}
				if (gridArrObj2.data[currGridRecords2 - 1][colname] != 'PASS'){
					gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
					break;
				}
				if (gridArrObj3.data[currGridRecords3 - 1][colname] != 'PASS'){
					gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
					break;
				}
				if (valChkRef == undefined || valChkRef == null){
					gridArrObj.data[currGridRecords - 1][colname] = 'PASS';
				}else if (val != undefined && val != valChkRef){
					gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
					break;
				}
			}
		}
	}
	//gridArrObj.grdObj.trigger("reloadGrid");
	gridArrObj.grdObj.jqGrid('setGridParam', {data: gridArrObj.data}).trigger('reloadGrid');
	saveAllRowsInGrid(gridArrObj.grdObj);
}
function evaluateFunctionalTestCDSLMM(){
	//PASS of earlier grid is important to evaluate here..
	var gridArrObj0 = autoDLControlSystemDependents[0];
	var currGridRecords0 = gridArrObj0.grdObj.jqGrid('getGridParam', 'records');
	
	var gridArrObj1 = autoDLControlSystemDependents[1];
	var currGridRecords1 = gridArrObj1.grdObj.jqGrid('getGridParam', 'records');
	
	var gridArrObj2 = autoDLControlSystemDependents[2];
	var currGridRecords2 = gridArrObj2.grdObj.jqGrid('getGridParam', 'records');
	
	//Get the data first 
	var gridArrObj = autoDLControlSystemDependents[3];
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	var parentGridRecords = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	var currGridRecords = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
	var subgridgroupsdata = $(gridArrObj.name).jqGrid("getGridParam", "groupingView").groups;
	var maingroupinfo = (subgridgroupsdata).filter(function (e){
        return e.idx == 0;
    });
	//Filter the subgroid groups data and find out row numbers 
	var passCheckMap = {'c.':'Y', 'k.':'Y', 'm.':'Y', 'u.':'Y', 'v.':'Y', 'w.':'Y', 'z.':'Y', 'cc.':'Y'};
	for (var i = 0; i < parentGridRecords; i++){
		var lastrow = 0;
		var colname = (i+1);
		evaluateDG3Rows(gridArrObj, colname);
		for (var j = 0; j < maingroupinfo.length - 1; j++ ){  
			var startRow = maingroupinfo[j].startRow; //Above group should pass to make pass the lower group.//Remove lastrow as well if dont not want
			lastrow = lastrow + maingroupinfo[j].cnt;
			for (var cnt =0; cnt < (lastrow - startRow); cnt++){
				var rowData = gridArrObj.data[startRow  +  cnt];
				var val = rowData[colname];
				var valChkRef = passCheckMap[rowData.itemno];
				if (gridArrObj0.data[currGridRecords0 - 1][colname] != 'PASS'){
					gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
					break;
				}
				if (gridArrObj1.data[currGridRecords1 - 1][colname] != 'PASS'){
					gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
					break;
				}
				if (gridArrObj2.data[currGridRecords2 - 1][colname] != 'PASS'){
					gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
					break;
				}
				if (valChkRef == undefined || valChkRef == null){
					gridArrObj.data[currGridRecords - 1][colname] = 'PASS';
				}else if (val != undefined && val != valChkRef){
					gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
					break;
				}
			}
		}
	}
	//gridArrObj.grdObj.trigger("reloadGrid");
	gridArrObj.grdObj.jqGrid('setGridParam', {data: gridArrObj.data}).trigger('reloadGrid');
	saveAllRowsInGrid(gridArrObj.grdObj);
}
function evaluateFunctionalTestSDS(){
	//PASS of earlier grid is important to evaluate here..
	var gridArrObj0 = autoDLControlSystemDependents[0];
	var currGridRecords0 = gridArrObj0.grdObj.jqGrid('getGridParam', 'records');
	
	var gridArrObj1 = autoDLControlSystemDependents[1];
	var currGridRecords1 = gridArrObj1.grdObj.jqGrid('getGridParam', 'records');
	
	//Get the data first 
	var gridArrObj = autoDLControlSystemDependents[2];
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	var parentGridRecords = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	var currGridRecords = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
	var subgridgroupsdata = $(gridArrObj.name).jqGrid("getGridParam", "groupingView").groups;
	var maingroupinfo = (subgridgroupsdata).filter(function (e){
        return e.idx == 0;
    });
	//Filter the subgroid groups data and find out row numbers 
	var passCheckMap = {'c.':'Y', 'l.':'Y', 'n.':'Y', 'F2':'Y', 'F3':'Y', 'F5':'Y', 'F6':'Y', 'F8':'Y', 'F9':'Y', 's.':'Y'};
	for (var i = 0; i < parentGridRecords; i++){
		var lastrow = 0;
		var colname = (i+1);
		evaluateDG2Rows(gridArrObj, colname);
		for (var j = 0; j < maingroupinfo.length - 1; j++ ){  
			var startRow = maingroupinfo[j].startRow; //Above group should pass to make pass the lower group.//Remove lastrow as well if dont not want
			lastrow = lastrow + maingroupinfo[j].cnt;
			for (var cnt =0; cnt < (lastrow - startRow); cnt++){
				var rowData = gridArrObj.data[startRow  +  cnt];
				var val = rowData[colname];
				var valChkRef = passCheckMap[rowData.itemno];
				if (gridArrObj0.data[currGridRecords0 - 1][colname] != 'PASS'){
					gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
					break;
				}
				if (gridArrObj1.data[currGridRecords1 - 1][colname] != 'PASS'){
					gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
					break;
				}
				if (valChkRef == undefined || valChkRef == null){
					gridArrObj.data[currGridRecords - 1][colname] = 'PASS';
				}else if (val != undefined && val != valChkRef){
					gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
					break;
				}
			}
		}
	}
	//gridArrObj.grdObj.trigger("reloadGrid");
	gridArrObj.grdObj.jqGrid('setGridParam', {data: gridArrObj.data}).trigger('reloadGrid');
	saveAllRowsInGrid(gridArrObj.grdObj);
}
function evaluateFunctionalTestCDS(){
	//PASS of earlier grid is important to evaluate here..
	var gridArrObj0 = autoDLControlSystemDependents[0];
	var currGridRecords0 = gridArrObj0.grdObj.jqGrid('getGridParam', 'records');
	
	//Get the data first 
	var gridArrObj = autoDLControlSystemDependents[1];
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	var parentGridRecords = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	var currGridRecords = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
	var subgridgroupsdata = $(gridArrObj.name).jqGrid("getGridParam", "groupingView").groups;
	var maingroupinfo = (subgridgroupsdata).filter(function (e){
        return e.idx == 0;
    });
	//Filter the subgroid groups data and find out row numbers 
	var passCheckMap = {'c.':'Y', 'l.':'Y', 'o.':'Y', 'p.':'Y', 'q.':'Y', 't.':'Y', 'w.':'Y'};
	for (var i = 0; i < parentGridRecords; i++){
		var lastrow = 0;
		var colname = (i+1);
		evaluateDG1Row12(gridArrObj, colname);
		evaluateDG1Step3(gridArrObj, colname);
		evaluateDG1Step4(gridArrObj, colname);
		for (var j = 0; j < maingroupinfo.length - 1; j++ ){  
			var startRow = maingroupinfo[j].startRow; //Above group should pass to make pass the lower group.//Remove lastrow as well if dont not want
			lastrow = lastrow + maingroupinfo[j].cnt;
			for (var cnt =0; cnt < (lastrow - startRow); cnt++){
				var rowData = gridArrObj.data[startRow  +  cnt];
				var val = rowData[colname];
				var valChkRef = passCheckMap[rowData.itemno];
				if (gridArrObj0.data[currGridRecords0 - 1][colname] != 'PASS'){
					gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
					break;
				}
				if (valChkRef == undefined || valChkRef == null){
					gridArrObj.data[currGridRecords - 1][colname] = 'PASS';
				}else if (val != undefined && val != valChkRef){
					gridArrObj.data[currGridRecords - 1][colname] = 'FAIL';
					break;
				}
			}
		}
	}
	//gridArrObj.grdObj.trigger("reloadGrid");
	gridArrObj.grdObj.jqGrid('setGridParam', {data: gridArrObj.data}).trigger('reloadGrid');
	saveAllRowsInGrid(gridArrObj.grdObj);
}

function evaluateConstructionInspection(){
	//Get the data first 
	var gridArrObj = autoDLControlSystemDependents[0];
	gridArrObj.data = gridArrObj.grdObj.jqGrid('getRowData');
	var parentGridRecords = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	var currGridRecords = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
	var subgridgroupsdata = $(gridArrObj.name).jqGrid("getGridParam", "groupingView").groups;
	var maingroupinfo = (subgridgroupsdata).filter(function (e){
        return e.idx == 0;
    });
	//Filter the subgroid groups data and find out row numbers 
	//console.log("Test Result data is "+ JSON.stringify(gridArrObj.data));
	var returnVal = "";
	for (var i = 0; i < parentGridRecords; i++){
		var lastrow = 0;
		var colname = (i+1);
		evaluateDG0Row6(gridArrObj, colname);
		for (var j = 0; j < maingroupinfo.length - 1; j++ ){  
			var startRow = maingroupinfo[j].startRow; //Above group should pass to make pass the lower group.//Remove lastrow as well if dont not want
			lastrow = lastrow + maingroupinfo[j].cnt;
			for (var cnt =0; cnt < (lastrow - startRow); cnt++){
				var rowNum = startRow  +  cnt;
				var val = gridArrObj.data[rowNum][colname];
				if (val != undefined && !val.isCustomEmpty() && val != 'N'){
					gridArrObj.data[currGridRecords - 1][colname] = 'PASS';
					returnVal = 'PASS';
				}else{
					gridArrObj.data[currGridRecords - 1 ][colname] = 'FAIL';
					returnVal = 'FAIL';
					break;
				}
			}
		}
	}
	gridArrObj.grdObj.jqGrid('setGridParam', {data: gridArrObj.data}).trigger('reloadGrid');
	saveAllRowsInGrid(gridArrObj.grdObj);
}
function evaluateDG0Row6(gridArrObj, colname) {
	var rowF = 5;
	var row4 = gridArrObj.data[3][colname];
	var row5 = gridArrObj.data[4][colname];
	if (isCustomExists([row4, row5])){
		if (row4 == 'OL'){
			if (row5 == 'CZ'){
				gridArrObj.data[rowF][colname] = 'N';
			}else{
				gridArrObj.data[rowF][colname] = 'Y';
			}
		}else{
			if (row5 == 'CZ'){
				gridArrObj.data[rowF][colname] = 'Y';
			}else{
				gridArrObj.data[rowF][colname] = 'N';
			}
		}
	}else{
		gridArrObj.data[rowF][colname] = 'N';
	}
}
function evaluateDG1Row12(gridArrObj, colname){
	var rowF = 11;
	var rowk = gridArrObj.data[10][colname];
	var rowg = gridArrObj.data[6][colname];
	var rowd = gridArrObj.data[3][colname];
	var rowi = gridArrObj.data[8][colname];
	if (isCustomExists([rowi]) == false){
		rowi = 'NA';
	}
	gridArrObj.data[rowF][colname] = 'N';
	if (isCustomExists([rowk, rowg])){
		var kg = rowk / rowg;
		if (rowi == 'FO'){
			if (kg > 0.7){
				gridArrObj.data[rowF][colname] = 'Y';
			}else{
				gridArrObj.data[rowF][colname] = 'N';
			}			
		}else{
			
			if (kg > 0.9){
				gridArrObj.data[rowF][colname] = 'Y';
			}
		}
		if (isCustomExists([rowk, rowd]) && gridArrObj.data[rowF][colname] != 'Y'){
			var kd = rowk / rowd;
			if (kd > 0.8){
				gridArrObj.data[rowF][colname] = 'Y';
			}else{
				gridArrObj.data[rowF][colname] = 'N';
			}
		}
		
	}else{
		gridArrObj.data[rowF][colname] = 'N';
	}
	
	rowF = 19; //t
}
//SDS Power estimation using light meter measurement -- last table
function evaluateDG4Rows(gridArrObj, colname){
	var rowF=12;//m
	var rowh = gridArrObj.data[7][colname];
	var rowj = gridArrObj.data[9][colname];
	var rowl = gridArrObj.data[11][colname];
	gridArrObj.data[rowF][colname] = 0;
	if (isCustomExists([rowj , rowl])){
		gridArrObj.data[rowF][colname] = roundToTwo( 1- (rowj * rowl)) * 100;
	}
	var rowm = gridArrObj.data[rowF][colname];
	rowF=13;//n
	gridArrObj.data[rowF][colname] = 'N';
	if (isCustomExists(rowh)){
		if (rowh == 'FO'){
			if (rowm > 65){
				gridArrObj.data[rowF][colname] = 'Y';
			}
			
		}else if (rowh == 'TT'){
			if (rowm > 56){
				gridArrObj.data[rowF][colname] = 'Y';
			}
			
		}
	}
	
	//gridArrObj.data[rowF][colname] = 'N';
	
	rowF=19;//t
	gridArrObj.data[rowF][colname] = 'N';
	var rows = gridArrObj.data[18][colname];
	if (isCustomExists(rows)){
		if (rows > 3){
			gridArrObj.data[rowF][colname] = 'Y';
		}
	}
	
	
}
//CDS Power estimation using light meter measurement
function evaluateDG3Rows(gridArrObj, colname){
	var rowh = gridArrObj.data[7][colname];
	var rowi = gridArrObj.data[8][colname];
	var rowj1 = gridArrObj.data[9][colname];
	var rowg = gridArrObj.data[6][colname];
	var rowF=10;//k
	gridArrObj.data[rowF][colname] = 'N';
	if (isCustomExists([rowh])){
		if (rowh == 'FO'){
			if ( (rowj1 / rowg) > 0.70 ){
				gridArrObj.data[rowF][colname] = 'Y';
			}
		}
	}
	var rowk = gridArrObj.data[rowF][colname];
	var rowd = gridArrObj.data[3][colname];
	if (rowk == 'N'){
		if(isCustomExists([rowj1, rowd])){
			if ( (rowj1 / rowd) > 0.80 ){
				gridArrObj.data[rowF][colname] = 'Y';	
			}
		}
	}
	
	
	rowF=12;//m
	var rowl = gridArrObj.data[11][colname];
	var rowj = gridArrObj.data[9][colname];
	gridArrObj.data[rowF][colname] = 'N';
	if (isCustomExists([rowl,rowj])){
		if (rowl > rowj){
			gridArrObj.data[rowF][colname] = 'Y';
		}
	}
	rowF=14;//o
	var rown = gridArrObj.data[13][colname];
	if (isCustomExists([rown])){
		gridArrObj.data[rowF][colname] = roundToTwo((1 - (rown / 100)) * 100);
	}
	var rowp = gridArrObj.data[15][colname];
	rowF=16;//q
	if (isCustomExists([rowp, rowl])){
		gridArrObj.data[rowF][colname] = rowp - rowl;
	}
	var rowq = gridArrObj.data[16][colname];
	rowF=17;//r
	if (isCustomExists([rowq, rowg])){
		gridArrObj.data[rowF][colname] = roundToTwo(rowq  / rowg) * 100;
	}
	rowF=19;//t
	var rowo = gridArrObj.data[14][colname];
	var rows = gridArrObj.data[18][colname];
	if (isCustomExists([rowq, rowg])){
		gridArrObj.data[rowF][colname] = (100 - roundToTwo(rowo * rows ));
	}
	rowF=20;//u
	var rowt = gridArrObj.data[19][colname];
	gridArrObj.data[rowF][colname] = 'N';
	if (isCustomExists([rowt,rowh])){
		if (rowh == 'FO'){
			if (rowt > 65){
				gridArrObj.data[rowF][colname] = 'Y';
			}
		}else if(rowh == 'TT'){
			if (rowt > 56){
				gridArrObj.data[rowF][colname] = 'Y';
			}
		}
	}
	rowF=24;//y
	var rowx = gridArrObj.data[23][colname];
	gridArrObj.data[25][colname] = 'N';
	if (isCustomExists([rowx,rowj])){
		gridArrObj.data[rowF][colname] = roundToTwo(rowx / rowj) * 100;
		var tmp = roundToTwo(rowx / rowj) * 100;
		if(tmp > 60 && tmp < 95){
			gridArrObj.data[25][colname] = 'Y';
		}
	}
	
	rowF=27;//bb
	var rowaa = gridArrObj.data[26][colname];
	gridArrObj.data[28][colname] = 'N';
	if (isCustomExists([rowaa,rowj])){
		gridArrObj.data[rowF][colname] = roundToTwo(rowaa / rowj) * 100;
		var tmp = roundToTwo(rowaa / rowj) * 100;
		if(tmp > 100 && tmp < 150){
			gridArrObj.data[28][colname] = 'Y';
		}
	}
}

//SDS power estimation Power estimation using watt‐meter or amp‐meter measurement
function evaluateDG2Rows(gridArrObj, colname){
	//25
	var rowr = gridArrObj.data[23][colname];
	var rowF=24;//s
	gridArrObj.data[rowF][colname] = 'N';
	if (isCustomExists([rowr])){
		if (rowr >= 3){
			gridArrObj.data[rowF][colname] = 'Y';
		}
	}
	
	var rowk = gridArrObj.data[10][colname];
	rowF=10;//k
	var rowj = gridArrObj.data[9][colname];
	if (isCustomExists([rowj]) == false){
		rowj = 0;
	}
	var rowh = gridArrObj.data[7][colname];
	if (isCustomExists([rowh]) == false){
		rowh = 0;
	}
	if (rowh != 0 ){
		gridArrObj.data[rowF][colname] = roundToTwo(1 - (rowj / rowh)) * 100;
	}else{
		gridArrObj.data[rowF][colname] = 0;
	}
	rowk = gridArrObj.data[10][colname];
	var rowi = gridArrObj.data[8][colname];
	rowF=11;//l
	gridArrObj.data[rowF][colname] = 'N';
	if (isCustomExists(rowi)){
		if (rowi == 'FO' && (rowk > 65)){
			gridArrObj.data[rowF][colname] = 'Y';
		} else if((rowi == 'TT' && (rowk > 56))){
			gridArrObj.data[rowF][colname] = 'Y';
		} else{
			gridArrObj.data[rowF][colname] = 'N';
		}
		
	}
	
}
function evaluateDG1Step3(gridArrObj, colname){
	var rowm = gridArrObj.data[12][colname];
	var rowh = gridArrObj.data[7][colname];
	var rowF=13;
	if (isCustomExists([rowm, rowh])){
		gridArrObj.data[rowF][colname] = roundToTwo((1 - ((rowm  / rowh))) * 100);
	}else{
		gridArrObj.data[rowF][colname] = '0';
	}
	var rown = gridArrObj.data[rowF][colname];
	rowF=14;
	var rowi = gridArrObj.data[8][colname];
	if (isCustomExists([rowi])){
		if (rowi == 'FO'){
			if (rown > 65){
				gridArrObj.data[rowF][colname] = 'Y';
				return;
			}else{
				gridArrObj.data[rowF][colname] = 'N';
			}
		}
		if (rowi == 'TT'){
			if (rown > 56){
				gridArrObj.data[rowF][colname] = 'Y';
				return;
			}else{
				gridArrObj.data[rowF][colname] = 'N';
			}
		}
	}else{
		gridArrObj.data[rowF][colname] = 'N';
	}
}
function evaluateDG1Step4(gridArrObj, colname){
	var rowk = gridArrObj.data[10][colname];
	var rowr = gridArrObj.data[17][colname];
	var rowF = 18;
	gridArrObj.data[rowF][colname] = '0';
	if (isCustomExists([rowk, rowr])){
		gridArrObj.data[rowF][colname] = roundToTwo((rowr / rowk) * 100);
	}
	var rows = gridArrObj.data[rowF][colname];
	rowF=19;
	gridArrObj.data[rowF][colname] = 'N';
	if(rows > 60 && rows < 95){
		gridArrObj.data[rowF][colname] = 'Y';
	}
	var rowu = gridArrObj.data[20][colname];
	rowF=21;//v
	gridArrObj.data[rowF][colname] = '0';
	if (isCustomExists([rowk, rowu])){
		gridArrObj.data[rowF][colname] = roundToTwo( (rowu / rowk) * 100);
	}
	var rowv = gridArrObj.data[rowF][colname];
	rowF=22;//w
	gridArrObj.data[rowF][colname] = 'N';
	if(rowv > 100 && rowv < 150){
		gridArrObj.data[rowF][colname] = 'Y';
	}
}

function renderSensorControlGrid(){
	
	var gridArrObj = autoDLControlSystemDependents[0];
	var record = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
	var parentRecords = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
    var grid = gridArrObj.grdObj;
    
    if (record != null && record != 'undefined' && record >= 0) {
        $.jgrid.gridUnload(gridArrObj.name);
        grid = $(gridArrObj.name);
        gridArrObj.grdObj = grid;
    }
    var lastsel2=0;
    //console.log('Data to reload:'+JSON.stringify(gridArrObj.data));
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
                    saveRowSensorControlGrid(lastsel2);
                    grid.jqGrid('restoreRow', lastsel2);
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
            groupSorted: false,
        },
        viewrecords: true,
        height: "100%",
        rowNum: 50000,
        caption: gridArrObj.caption
    });
    
	  gridArrObj.grdObj.jqGrid('setGroupHeaders', {
	  	  useColSpanStyle: true,   
	  	  groupHeaders:[{startColumnName: 1, numberOfColumns: parentRecords, titleText: 'Applicable Control Systems'}]
	  	});
}
function dependentGridsBeforeAutoDLRowAdded(newrowid){
	for (var i = 0 ; i < autoDLControlSystemDependents.length; i++){
		var gridArrObj =autoDLControlSystemDependents[i]; 
		gridArrObj.currentMaxKeyParentRowDependentMap = gridArrObj.currentMaxKeyParentRowDependentMap + 1;
		gridArrObj.parentRowDependentMap[gridArrObj.currentMaxKeyParentRowDependentMap] = newrowid + (gridArrObj.noofdefaultcolumns - 1);
	}
}
function dependentGridsAfterAutoDLRowAdded(){
	/**
	 * STARTED : DEPENDENT GRID 1
	 */
	var gridArrObj = autoDLControlSystemDependents[0];
	var newrowid = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	gridArrObj.colnames.push(toLetters(gridArrObj.currentMaxKeyParentRowDependentMap));
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
    //console.log(JSON.stringify(gridArrObj.colnames));
    //console.log(JSON.stringify(gridArrObj.colmodels));
    //console.log('DATA:'+ JSON.stringify(gridArrObj.data ))
    /**
	 * ENDS : DEPENDENT GRID 1
	 */
    
    gridArrObj = autoDLControlSystemDependents[1];
	newrowid = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	gridArrObj.colnames.push(toLetters(gridArrObj.currentMaxKeyParentRowDependentMap));
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
    
    gridArrObj = autoDLControlSystemDependents[2];
	newrowid = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	gridArrObj.colnames.push(toLetters(gridArrObj.currentMaxKeyParentRowDependentMap));
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
	 * ENDS : DEPENDENT GRID 3
	 */
    
    gridArrObj = autoDLControlSystemDependents[3];
	newrowid = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	gridArrObj.colnames.push(toLetters(gridArrObj.currentMaxKeyParentRowDependentMap));
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
	 * ENDS : DEPENDENT GRID 4
	 */
    
    gridArrObj = autoDLControlSystemDependents[4];
	newrowid = gridArrObj.parentGrid.jqGrid('getGridParam', 'records');
	gridArrObj.colnames.push(toLetters(gridArrObj.currentMaxKeyParentRowDependentMap));
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
	 * ENDS : DEPENDENT GRID 4
	 */
}
function saveRowAutoDayLightControlSystemGrid(grid, idToSave) {
    var selectedRowId = grid.jqGrid('getGridParam', 'selrow');
    if (idToSave != null && idToSave != 'undefined') {
        selectedRowId = idToSave;
    }
    if (selectedRowId == null || selectedRowId == 'undefined') {
        alert('Please select the row to save');
        return false;
    }

    grid.jqGrid('saveRow', selectedRowId, false, 'clientArray');
    grid.data=grid.jqGrid('getRowData');
    var record = grid.jqGrid('getGridParam', 'records');
    console.log("no of Record:"+record);
    if (idToSave != null && idToSave != 'undefined') {
        $("#autoDLControlSystemGridSave").attr("disabled", false);
    } else {
        $("#autoDLControlSystemGridSave").attr("disabled", true);
    }
    //$("#spaceDetailTableEdit").attr("disabled", false);
    $("#autoDLControlSystemGridAdd").attr("disabled", false);
    return true;
}

function delRowAutoDL(parentTableRowId, spaceNo) {
	autoDLControlSystemDependents[0].parentGrid.jqGrid('delRowData', parentTableRowId);
	autoDLControlSystemDependents[0].parentGrid.trigger('reloadGrid');
	dependentGridsAfterAutoDLParentRowDelete(spaceNo);
   dependentGridsAutoDLRedraw();
   $("#autoDLControlSystemGridAdd").attr("disabled", false);
   $("#autoDLControlSystemGridSave").attr("disabled", true);
}
function dependentGridsAfterAutoDLParentRowDelete(spaceNo){
	for (var x =0 ; x < autoDLControlSystemDependents.length; x++){
		var gridArrObj = autoDLControlSystemDependents[x];
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

function dependentGridsAutoDLPopulateFromDB(){
	//If page is rendered from db data the need to update the spaceRowMap
	/**
	 * START:::Started for Table I here
	 */
	var gridArrObj = autoDLControlSystemDependents[0];
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
            sortable: false,
            editoptions: {
                value: "N:N;Y:Y",
                defaultValue: "N"
            }
        });
    }
    recordPop = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
    rowDataPop = gridArrObj.grdObj.jqGrid('getRowData');
    var tempdata1 = $("#autodldependentspacecontrol").val();
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
	gridArrObj = autoDLControlSystemDependents[1];
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
            sortable: false,
            editoptions: {
                value: "N:N;Y:Y",
                defaultValue: "N"
            }
        });
    }
    recordPop = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
    rowDataPop = gridArrObj.grdObj.jqGrid('getRowData');
    tempdata1 = $("#autodlfunctionaltestingcds").val();
    if(!tempdata1.isCustomEmpty()){
    	var d1=$.parseJSON(tempdata1);
    	if (Object.keys(d1).length > 0){
    		gridArrObj.data = d1;
    	}
    }
    /**
	 * END:::Endeded for Table II here
	 */
    
    
    /**
	 * START:::Started for Table III here
	 */
	gridArrObj = autoDLControlSystemDependents[2];
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
            sortable: false,
            editoptions: {
                value: "N:N;Y:Y",
                defaultValue: "N"
            }
        });
    }
    recordPop = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
    rowDataPop = gridArrObj.grdObj.jqGrid('getRowData');
    tempdata1 = $("#autodlfunctionaltestingsds").val();
    if(!tempdata1.isCustomEmpty()){
    	var d1=$.parseJSON(tempdata1);
    	if (Object.keys(d1).length > 0){
    		gridArrObj.data = d1;
    	}
    }
    /**
	 * END:::Endeded for Table II here
	 */
    
    /**
	 * START:::Started for Table III here
	 */
	gridArrObj = autoDLControlSystemDependents[3];
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
            sortable: false,
            editable: true,
            editoptions: {
                value: "N:N;Y:Y",
                defaultValue: "N"
            }
        });
    }
    recordPop = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
    rowDataPop = gridArrObj.grdObj.jqGrid('getRowData');
    tempdata1 = $("#autodlfunctionaltestingcdslmm").val();
    if(!tempdata1.isCustomEmpty()){
    	var d1=$.parseJSON(tempdata1);
    	if (Object.keys(d1).length > 0){
    		gridArrObj.data = d1;
    	}
    }
    /**
	 * END:::Endeded for Table III here
	 */
    
    /**
	 * START:::Started for Table IV here
	 */
	gridArrObj = autoDLControlSystemDependents[4];
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
            sortable: false,
            editoptions: {
                value: "N:N;Y:Y",
                defaultValue: "N"
            }
        });
    }
    recordPop = gridArrObj.grdObj.jqGrid('getGridParam', 'records');
    rowDataPop = gridArrObj.grdObj.jqGrid('getRowData');
    tempdata1 = $("#autodlfunctionaltestingsdslmm").val();
    if(!tempdata1.isCustomEmpty()){
    	var d1=$.parseJSON(tempdata1);
    	if (Object.keys(d1).length > 0){
    		gridArrObj.data = d1;
    	}
    }
    /**
	 * END:::Endeded for Table IV here
	 */
}




/**
 * Help for data
 */

/*data: 	[
	      	{
	      		  "id": "1","itemno":"a.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "2","itemno":"b.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "3","itemno":"c.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "4","itemno":"d.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "5","itemno":"e.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "6","itemno":"f.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "7","itemno":"g.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "8","itemno":"h.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "9","itemno":"i.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "10","itemno":"j.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "11","itemno":"k.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "12","itemno":"l.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "13","itemno":"m.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "14","itemno":"n.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "15","itemno":"o.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "16","itemno":"p.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "17","itemno":"q.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "18","itemno":"r.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "19","itemno":"s.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "20","itemno":"t.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "21","itemno":"u.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "22","itemno":"v.","test" : "",
	      		  "name":""
	      	 },{
	      		  "id": "23","itemno":"w.","test" : "",
	      		  "name":""
	      	 },
	      	{
	      		  "id": "24","itemno":"x.","test" : "",
	      		  "name":""
	      	 },
	      	],*/

/*gridRowNumberTypeMap : 
			[
	{
		 "edittype":"select", "editoptions" : { "value" : ":;O:O;C:C"}
	},
	{
		 "edittype":"select", "editoptions" : { "value" : ":;M:M;V:V"}
	},{},
	{
		 "edittype":"text"
	},
	{
		 "edittype":"text"
	},
	{
		 "edittype":"select", "editoptions" : { "value" : ":;I:I;D:D"}
	},
	{
		 "edittype":"text"
	},
	{
		 "edittype":"text"
	},
	{
		 "edittype":"select", "editoptions" : { "value" : ":;FO:FO;TT:TT"}
	},
	{
		 "edittype":"select", "editoptions" : { "value" : ":;Night:Night,Log:Log;CF:CF;COLP:COLP"}
	},
	{
		 "edittype":"text"
	},{},
	{
		 "edittype":"text"
	},
	{
		 "edittype":"text"
	},{},{},{},
	{
		 "edittype":"text"
	},
	{
		 "edittype":"text"
	},{},
	{
		 "edittype":"text"
	},
	{
		 "edittype":"text"
	},{}
			]*/