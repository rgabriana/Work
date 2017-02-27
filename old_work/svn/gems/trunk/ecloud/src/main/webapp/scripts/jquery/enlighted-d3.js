var barWidth=30;
// Default 28 Colors used in the chart 
var colors = new Array();
colors[0] = "#E48701";
colors[1] = "#A5BC4E";
colors[2] = "#1B95D9";
colors[3] = "#CACA9E";
colors[4] = "#6693B0";
colors[5] = "#F05E27";
colors[6] = "#86D1E4";
colors[7] = "#E4F9A0";
colors[8] = "#FFD512";
colors[9] = "#75B000";
colors[10] = "#0662B0";
colors[11] = "#EDE8C6";
colors[12] = "#CC3300";
colors[13] = "#D1DFE7";
colors[14] = "#52D4CA";
colors[15] = "#C5E05D";
colors[16] = "#E7C174";
colors[17] = "#FFF797";
colors[18] = "#C5F68F";
colors[19] = "#BDF1E6";
colors[20] = "#9E987D";
colors[21] = "#A5BC4E";
colors[22] = "#91C9E5";
colors[23] = "#93DC4A";
colors[24] = "#FFB900";
colors[25] = "#9EBBCD";
colors[26] = "#009797";
colors[27] = "#0DB2C2";

var constants = {
	barxposmargin : 30,
	barHeightScaleFactor : 3,
	barGroupMargin : 20,
	barStrokeWidth : 25,
	barInitXPos : 5,
	barInitYPos : 300,
	barLabelMargin : 20,
	legendYSpacing : 20,
	utilizationGraphWidth : 300,
	utilizationGraphHeight : 325,
	xscale : 1,
	yscale : 1
}
function scaleConstantsFromGrpahWidthHeight(w, h) {
	var wfac = 500;
	var hfac = 325;
	constants.barxposmargin = (w * 30) / wfac;
	constants.barHeightScaleFactor = (h * 3) / hfac;
	constants.barGroupMargin = (w * 20) / wfac;
	constants.barStrokeWidth = (w * 25) / wfac;
	constants.barInitXPos = (w * 5) / wfac;
	constants.barInitYPos = (h * 300) / hfac;
	constants.barLabelMargin = (h * 20) / hfac;
	constants.legendYSpacing = (h * 20) / hfac;
	constants.utilizationGraphWidth = w;
	constants.utilizationGraphHeight = h;
	constants.xscale = w / wfac;
	constants.yscale = h / hfac;
}

var enlGraphInput = {
	svgelement : null,
	id : null,
	x1 : null,
	x2 : null,
	y1 : null,
	y2 : null,
	stroke : null,
	strokewidth : null,
	transform : null,
	fill : null,
	fontSize : null,
	text : null,
	textAnchor : null,
	translatex : null,
	translatey : null,
	x : null,
	y : null,
	r : null,
	width : null,
	height : null,
	innerradius : null,
	outerradius : null,
	startangle : null,
	endangle : null,
	textlength : null,
	textfill : null,
	textstrokewidth : null,
	istextonarc : false,
}

function drawLine(enlGraphInput) {
	enlGraphInput.svgelement.append("line").attr("id", enlGraphInput.id).attr(
			"x1", enlGraphInput.x1).attr("y1", enlGraphInput.y1).style(
			"stroke-width", enlGraphInput.strokewidth).attr("x2",
			enlGraphInput.x2).attr("y2", enlGraphInput.y2).style("stroke",
			enlGraphInput.stroke).text(enlGraphInput.text).attr("transform",
			enlGraphInput.transform).style("font-size",
			enlGraphInput.fontSize + "%").style("fill", enlGraphInput.fill);
}
function drawRectangle(enlGraphInput) {
	enlGraphInput.svgelement.append("rect").attr("id", enlGraphInput.id).attr(
			"x", enlGraphInput.x).attr("y", enlGraphInput.y).attr("width",
			enlGraphInput.width).attr("height", enlGraphInput.height).style(
			"fill", enlGraphInput.fill);
}

function drawCircle(enlGraphInput) {
	enlGraphInput.svgelement.append("circle").attr("id", enlGraphInput.id)
			.attr("cx", enlGraphInput.x).attr("cy", enlGraphInput.y).attr("r",
					enlGraphInput.r).style("fill", enlGraphInput.fill);
}

function writeText(ip) {
	ip.svgelement.append("svg:text").text(ip.text).attr("text-anchor",
			ip.textAnchor).attr("id", ip.id).attr("transform", function() {
		return "translate(" + (ip.translatex) + "," + (ip.translatey) + ")";
	}).style("stroke", ip.stroke).style("stroke-width", ip.strokewidth).style(
			"font-size", ip.fontSize + "%").style("fill", ip.fill);
	;
}
function drawArc(enlGraphInput) {
	var vis = enlGraphInput.svgelement;
	var myScale = d3.scale.linear().domain([ 0, 100 ])
			.range([ 0, 2 * Math.PI ]);
	var sAngleRad = myScale(enlGraphInput.startangle);
	var eAngleRad = myScale(enlGraphInput.endangle);
	var angleTraversedIndDeg = 360 * (enlGraphInput.endangle - enlGraphInput.startangle) / 100;
	var angleTraversed = myScale(angleTraversedIndDeg);
	var actAngleTraversedForCoordInDeg = ((enlGraphInput.startangle * (360 / 100)) + (angleTraversedIndDeg / 2));
	var actAngleTraversedForCoord = myScale(actAngleTraversedForCoordInDeg);
	var txtlen = enlGraphInput.textlength;
	var arc = d3.svg.arc().innerRadius(enlGraphInput.innerradius).outerRadius(
			enlGraphInput.outerradius).startAngle(sAngleRad)
			.endAngle(eAngleRad);

	vis.append("path").attr("d", arc).attr("id", enlGraphInput.id).attr(
			"transform",
			"translate(" + enlGraphInput.translatex + ","
					+ enlGraphInput.translatey + ")").style("fill",
			enlGraphInput.fill);

	if (enlGraphInput.istextonarc) {
		var dy = (enlGraphInput.outerradius - enlGraphInput.innerradius + enlGraphInput.fontSize) / (2);
		var ds = enlGraphInput.innerradius * ((angleTraversed));
		var text = vis.append("text").attr("x", enlGraphInput.innerradius)
				.attr("dy", dy).attr("dx", (ds - (3 * txtlen)) / 3).attr(
						"font-size", enlGraphInput.fontSize).attr("fill",
						enlGraphInput.textfill).attr("stroke-width",
						enlGraphInput.textstrokewidth);

		text.append("textPath").attr("stroke", "red")
				.attr("textLength", txtlen).attr("xlink:href",
						'#' + enlGraphInput.id).text(enlGraphInput.text);
	} else {
		// find co-ords of the text to be inserted on donut arc
		var radofcoord = enlGraphInput.innerradius
				+ ((enlGraphInput.outerradius - enlGraphInput.innerradius) / (2));
		var avgCosineDegRad = (Math.cos(actAngleTraversedForCoordInDeg) + Math
				.cos(actAngleTraversedForCoord)) / 2;
		var avgSineDegRad = (Math.sin(actAngleTraversedForCoordInDeg) + Math
				.sin(actAngleTraversedForCoord)) / 2;
		var txtxpos = enlGraphInput.translatex - (avgCosineDegRad * radofcoord);
		var txtypos = enlGraphInput.translatey - (avgSineDegRad * radofcoord);
		var text = {
			svgelement : vis,
			id : enlGraphInput.id,
			stroke : enlGraphInput.textfill,
			strokewidth : enlGraphInput.textstrokewidth,
			fill : enlGraphInput.textfill,
			fontSize : (enlGraphInput.fontSize),
			text : enlGraphInput.text,
			textAnchor : enlGraphInput.textAnchor,
			translatex : (txtxpos),
			translatey : (txtypos),
		}
		writeText(text);
	}

}
function drawUtilizationGrapth(w, h) {
	scaleConstantsFromGrpahWidthHeight(w, h);

	// These attributes affect all
	// graphical elements in bargroup.
	var occdata = $('input#flooroccData').val() + "/"
			+ $('input#otreenodeid').val();
	$.ajax({
		type : 'GET',
		data : {
			rowData : {}
		},
		dataType : 'json',
		url : occdata,
		async : true,
		beforeSend : function() {
		},
		complete : function() {
		},
		success : function(response, textStatus, xhr) {
			var jsonResponse = xhr.responseText;
			var jsonData = $.parseJSON(jsonResponse);
			isRollOverData = false;
			var datalen = jsonData.data.length;
			renderAllStatisticTypes(jsonData);
			//renderNvd3BarChart(jsonData);
			renderC3BarChart(jsonData);
			renderSensorDonutChart(jsonData);
			renderTotalSqFtDonutChart(jsonData);
			renderUtilizationGraphGrid(jsonData);
		},
		error : function(xhr, textStatus, errorThrown) {
			alert("Error occurred while setting dependet values" + errorThrown
					+ "TextStatus:" + textStatus)
					+ "xhr" + xhr;
		}
	});
}

function renderAllOccTypes(jsonData, clickedElement) {

	// $( "#allStatTypes" ).append( '<input type="radio" name="radio-choice-1"
	// id="radio-choice-1" value="choice-1" >' );

	var spaceTylen = jsonData.allOccTypes.length;
	var statMasterName = jsonData.data[0].occMaster.name;
	$("#allOccTypes").append(
			'<span id="occTypeTitle">Occupancies</span><br/><br/>');
	for ( var cnt = 0; cnt < spaceTylen; cnt++) {
		var statType = jsonData.allOccTypes[cnt].name;
		var checkedStr = "";
		if (statMasterName == statType) {
			checkedStr = 'checked="checked"';
		}
		$("#allOccTypes").append(
				'<input type="radio" name="occTypeId" id="occTypeId" value="'
						+ statType + '"' + checkedStr + ' >' + statType
						+ '</input> <br/>');
	}
}

function renderAllStatisticTypes(jsonData, clickedElement) {

	// $( "#allStatTypes" ).append( '<input type="radio" name="radio-choice-1"
	// id="radio-choice-1" value="choice-1" >' );

	var spaceTylen = jsonData.allStatTypes.length;
	var statMasterName = jsonData.statMaster.name;
	$("#allStatTypes")
			.append(
					'<span id="statisticTypeTitle">Statistics Types</span>&nbsp;&nbsp;:');
	for ( var cnt = 0; cnt < spaceTylen; cnt++) {
		var statType = jsonData.allStatTypes[cnt].name;
		var checkedStr = "";
		if (statMasterName == statType) {
			checkedStr = 'checked="checked"';
		}
		$("#allStatTypes").append(
				'&nbsp;<input type="radio" name="statisticTypeId" id="statisticTypeId" value="'
						+ statType + '"' + checkedStr + ' >' + statType
						+ '</input> &nbsp;&nbsp;');
	}
	$("#allStatTypes").append('</br>');
}

function renderSensorDonutChart(jsonData, clickedElement) {
	var c = generateColorRangeForDonut(jsonData, clickedElement);
	// alert('hi4');
	var w = $("#sensorDonutChart").width();
	var h = $("#sensorDonutChart").height();
	var radW = 170;
	var radH = 170;
	var radWHRatio = radW / radH;
	nv.addGraph(function() {
		var chart = nv.models.pieChart().x(function(d) {
			return d.label
		}).y(function(d) {
			return Number(d.value)
		}).color(c).showLegend(false).showLabels(true) // Display pie labels
		.labelThreshold(0.05) // Configure the minimum slice size for labels
		// to show up
		.labelType("percent") // Configure what type of data to show in the
		// label. Can be "key", "value" or "percent"
		.donut(true) // Turn on Donut mode. Makes pie chart look tasty!
		.donutRatio(0.35) // Configure how big you want the donut hole size to
		// be.
		.width(radW * w / 250).height(radH * h / 175);
		// alert(w+':'+h);
		var radMminWH = Math.min(radW, radH);
		d3.select("#sensorDonutChart svg").attr(
				"viewBox",
				(45 * (radW / 200) * w / 250) + "  "
						+ (49 * (radH / 200) * h / 175) + "  "
						+ (115 * (radW / 200) * w / 250) + " "
						+ (115 * (radH / 200) * h / 175));
		$("#sensorDonutChart svg").text('');
		d3.select("#sensorDonutChart svg").append("text").attr("x", "86%")
				.attr("y", (radH / 2) + 10).attr("text-anchor", "middle")
				.style("font-size", (radMminWH * 30 / 100) + "%").style(
						"font-weight", "bold").text("Sensor Count");

		d3.select("#sensorDonutChart svg").datum(
				generateSensorDonutChartData(jsonData, clickedElement))
				.transition().duration(350).call(chart);

		$(window).resize(function() {
			renderSensorDonutChart(jsonData, clickedElement);
		});

		return chart;
	});
}

function renderTotalSqFtDonutChart(jsonData, clickedElement) {
	var c = generateColorRangeForDonut(jsonData, clickedElement);
	var w = $("#totalsqftDonutChart").width();
	var h = $("#totalsqftDonutChart").height();
	var radW = 170;
	var radH = 170;
	var radWHRatio = radW / radH;
	nv.addGraph(function() {
		var chart = nv.models.pieChart().x(function(d) {
			return d.label
		}).y(function(d) {
			return Number(d.value)
		}).color(c).showLegend(false).showLabels(true) // Display pie labels
		.labelThreshold(.05) // Configure the minimum slice size for labels
		// to show up
		.labelType("percent") // Configure what type of data to show in the
		// label. Can be "key", "value" or "percent"
		.donut(true) // Turn on Donut mode. Makes pie chart look tasty!
		.donutRatio(0.35) // Configure how big you want the donut hole size to
		// be.
		.width(radW * w / 250).height(radH * h / 175);
		var radMminWH = Math.min(radW, radH);
		d3.select("#totalsqftDonutChart svg").attr(
				"viewBox",
				(45 * (radW / 200) * w / 250) + "  "
						+ (49 * (radH / 200) * h / 175) + "  "
						+ (115 * (radW / 200) * w / 250) + " "
						+ (115 * (radH / 200) * h / 175));
		$("#totalsqftDonutChart svg").text('');
		d3.select("#totalsqftDonutChart svg").append("text").attr("x", "86%")
				.attr("y", (radH / 2) + 10).attr("text-anchor", "middle")
				.style("font-size", (radMminWH * 30 / 100) + "%").style(
						"font-weight", "bold").text("Area Sq Ft");

		d3.select("#totalsqftDonutChart svg").datum(
				generateTotalSqFtDonutChartData(jsonData, clickedElement))
				.transition().duration(350).call(chart);

		$(window).resize(function() {
			renderTotalSqFtDonutChart(jsonData, clickedElement);
		});

		return chart;
	});
}
function renderC3BarChart(jsonData) {
	
	var prevdata = generateBarChartData(jsonData);
	var mainJson = getC3FormatData(prevdata, true);
	var chart = c3.generate(mainJson);
	
	chart.resize();	
}
function renderNvd3BarChart(jsonData) {
	nv.addGraph(function() {

		var chart = nv.models.multiBarChart().transitionDuration(1350)
				.reduceXTicks(false) // If
				// 'false',
				// every
				// single
				// x-axis
				// tick
				// label
				// will
				// be
				// rendered.
				.rotateLabels(0) // Angle to rotate x-axis labels.
				.stacked(false).showControls(false) // Allow user to switch
				// between 'Grouped'
				// and
				// 'Stacked' mode.
				.forceY([0,50])
				.groupSpacing(0.1) // Distance between each group of bars.
		;
		chart.yAxis.tickFormat(d3.format(',.0f')).axisLabel('Occupancy %').axisLabelDistance(40);
		var s = d3.select('#barChart svg');
		var data = generateBarChartData(jsonData);

		s.datum(data).call(chart);

		nv.utils.windowResize(chart.update);

		try {
			// add zoom handler
			function addZoom(options) {
				// scaleExtent
				var scaleExtent = 10;

				// parameters
				var yAxis = options.yAxis;
				var xAxis = options.xAxis;
				var xDomain = options.xDomain || xAxis.scale().domain;
				var yDomain = options.yDomain || yAxis.scale().domain;
				var redraw = options.redraw;
				var svg = options.svg;
				var discrete = options.discrete;

				// scales
				var xScale = xAxis.scale();
				var yScale = yAxis.scale();

				// min/max boundaries
				var x_boundary = xScale.domain().slice();
				var y_boundary = yScale.domain().slice();

				// create d3 zoom handler
				var d3zoom = d3.behavior.zoom();

				// ensure nice axis
				xScale.nice();
				yScale.nice();

				// fix domain
				function fixDomain(domain, boundary) {
					if (discrete) {
						domain[0] = parseInt(domain[0]);
						domain[1] = parseInt(domain[1]);
					}
					domain[0] = Math.min(Math.max(domain[0], boundary[0]),
							boundary[1] - boundary[1] / scaleExtent);
					domain[1] = Math.max(boundary[0] + boundary[1]
							/ scaleExtent, Math.min(domain[1], boundary[1]));
					return domain;
				}
				;

				// zoom event handler
				function zoomed() {
					yDomain(fixDomain(yScale.domain(), y_boundary));
					xDomain(fixDomain(xScale.domain(), x_boundary));
					redraw();
				}
				;

				// zoom event handler
				function unzoomed() {
					xDomain(x_boundary);
					yDomain(y_boundary);
					redraw();
					d3zoom.scale(1);
					d3zoom.translate([ 0, 0 ]);
				}
				;

				// initialize wrapper
				d3zoom.x(xScale).y(yScale).scaleExtent([ 1, scaleExtent ]).on(
						'zoom', zoomed);

				// add handler
				svg.call(d3zoom).on('dblclick.zoom', unzoomed);
			}
			;

			// add zoom
			addZoom({
				xAxis : chart.xAxis,
				yAxis : chart.yAxis,
				yDomain : chart.yDomain,
				xDomain : chart.xDomain,
				redraw : function() {
					chart.update()
				},
				svg : s,
			});
			nv.utils.windowResize(chart.update);
		} catch (err) {
			console.log(err);
		}

		return chart;
	}, function() {
		$('.nv-series').click(function() {
			var skipElements = $('#barChart .nv-legend .disabled').text();
			renderSensorDonutChart(jsonData, skipElements);
			renderTotalSqFtDonutChart(jsonData, skipElements);
			renderUtilizationGraphGrid(jsonData, skipElements);
		})

	});
}

function isContainsString(str1, str2) {
	if (str1 != undefined) {
		if (str1.indexOf(str2) > -1) {
			return true;
		}
	}
	return false;
}

function getMaxSpaceDataIndexForData(data){
	var dataIndx = 0;
	var spaceDataLen = 0;
	for(var i =0; i< data.length;i++){
		var spaceDataLenTemp = data[i].spaceData.length;
		if(spaceDataLenTemp >= spaceDataLen){
			spaceDataLen = spaceDataLenTemp;
			dataIndx = i;
		}
	}
	return dataIndx;
}
function generateSensorDonutChartData(jsonData, clickedElement) {
	var data = isRollOverData ? jsonData.rollOverdata : jsonData.data;
	
	//Find dataIndex that has maximum no of spacecnt from spacedata
	var dataIndx = getMaxSpaceDataIndexForData(data);
	var spaceDataLen = data[dataIndx].spaceData.length;
	var jqGridData = [];
	for ( var spacecnt = 0; spacecnt < spaceDataLen; spacecnt++) {

		var rowObj = {};
		var d = data[dataIndx].spaceData[spacecnt];

		if (isContainsString(clickedElement, d.spaceMaster.abbr)) {
			continue;
		}
		rowObj['label'] = d.spaceMaster.abbr;
		rowObj['value'] = d.sensors;
		rowObj['color'] = getStrokeFromSpaceName(d.spaceMaster.groupId);
		rowObj['color_category'] = getStrokeFromSpaceName(d.spaceMaster.groupId);
		jqGridData.push(rowObj);
	}
	return jqGridData;
}

function generateColorRangeForDonut(jsonData, clickedElement) {
	var data = isRollOverData ? jsonData.rollOverdata : jsonData.data;
	var dataIndx = getMaxSpaceDataIndexForData(data);
	var spaceDataLen = data[dataIndx].spaceData.length;
	var jqGridData = [];
	for ( var spacecnt = 0; spacecnt < spaceDataLen; spacecnt++) {
		var rowObj = {};
		var d = data[dataIndx].spaceData[spacecnt];
		if (isContainsString(clickedElement, d.spaceMaster.abbr)) {
			continue;
		}
		// console.log(" d.spaceMaster.profileName " + d.spaceMaster.name +
		// "==>" + d.spaceMaster.groupId );
		jqGridData.push(getStrokeFromSpaceName(d.spaceMaster.groupId));
	}
	// console.log("HERE "+ jqGridData);
	return jqGridData;
}

function generateTotalSqFtDonutChartData(jsonData, clickedElement) {
	var data = isRollOverData ? jsonData.rollOverdata : jsonData.data;
	var dataIndx = getMaxSpaceDataIndexForData(data);
	var spaceDataLen = data[dataIndx].spaceData.length;
	var jqGridData = [];
	for ( var spacecnt = 0; spacecnt < spaceDataLen; spacecnt++) {
		var rowObj = {};
		var d = data[dataIndx].spaceData[spacecnt];
		if (isContainsString(clickedElement, d.spaceMaster.abbr)) {
			continue;
		}
		rowObj['label'] = d.spaceMaster.abbr;
		rowObj['value'] = d.totalSqFt;
		rowObj['color'] = getStrokeFromSpaceName(d.spaceMaster.groupId);
		rowObj['color_category'] = getStrokeFromSpaceName(d.spaceMaster.groupId);
		jqGridData.push(rowObj);
	}
	return jqGridData;
}

function generateBarChartData(jsonData, dataSwapFlag) {
	var jqGridData = [];
	if (dataSwapFlag) {
		generateBarChartDataNonFloor(jsonData, jqGridData);
	} else {
		generateBarChartDataFloor(jsonData, jqGridData);
	}
	return jqGridData;
}

function generateBarChartDataNonFloor(jsonData, jqGridData) {
	var data = jsonData.data;
	var spaceTypes = "";
	var dataLen = data.length;

	for ( var i = 0; i < dataLen; i++) {

		var spaceDataLen = data[i].spaceData.length;
		var distxt = jsonData.statMaster.abbr + " " + data[i].occMaster.abbr;
		if (data[i].occMaster.displayName != undefined) {
			distxt = data[i].occMaster.displayName;
		}
		for ( var spacecnt = 0; spacecnt < spaceDataLen; spacecnt++) {

			var d = data[i].spaceData[spacecnt];
			var ky = d.spaceMaster.name;
			if (!isContainsString(spaceTypes, ky)) {
				var rowObj = {};
				rowObj['key'] = ky;
				jqGridData.push(rowObj);
				var gridLen = jqGridData.length;
				var valObj = [];
				jqGridData[gridLen - 1]['color'] = getStrokeFromSpaceName(d.spaceMaster.groupId);
				jqGridData[gridLen - 1]['values'] = valObj;

				for ( var x = 0; x < dataLen; x++) {
					var distxt1 = jsonData.statMaster.abbr + " "
							+ data[x].occMaster.abbr;
					if (data[x].occMaster.displayName != undefined) {
						distxt1 = data[x].occMaster.displayName;
					}

					var valObjInner = {};
					valObjInner['x'] = distxt1;
					valObjInner['y'] = 0;
					valObjInner['color'] = getStrokeFromSpaceName(d.spaceMaster.groupId);
					valObj.push(valObjInner);
				}
				spaceTypes += ("," + ky);
			}
			var gridLen = jqGridData.length;
			for ( var x = 0; x < gridLen; x++) {
				if (jqGridData[x]['key'] == ky) {
					var valObj = jqGridData[x]['values'];
					for ( var y = 0; y < valObj.length; y++) {
						var valObjInner = valObj[y];
						if (valObjInner['x'] == distxt) {
							valObjInner['y'] = Number(d.value);
							valObjInner['color'] = getStrokeFromSpaceName(d.spaceMaster.groupId);
							// alert(JSON.stringify(valObjInner));
							break;
						}
					}

					break;
				}
			}

		}
	}
	console.log('jqgriddata:  ' + JSON.stringify(jqGridData));
}

function generateBarChartDataFloor(jsonData, jqGridData) {
	var data = jsonData.data;
	//console.log('Input Data Floor:  ' + JSON.stringify(jsonData));
	var spaceTypes = "";
	var dataLen = data.length;

	for ( var i = 0; i < dataLen; i++) {

		var spaceDataLen = data[i].spaceData.length;
		var distxt = jsonData.statMaster.abbr + " " + data[i].occMaster.abbr;
		if (data[i].occMaster.displayName != undefined) {
			distxt = data[i].occMaster.displayName;
		}
		for ( var spacecnt = 0; spacecnt < spaceDataLen; spacecnt++) {

			var d = data[i].spaceData[spacecnt];
			var ky = d.spaceMaster.name;
			if (!isContainsString(spaceTypes, ky)) {
				var rowObj = {};
				rowObj['key'] = ky;
				jqGridData.push(rowObj);
				var gridLen = jqGridData.length;
				var valObj = [];
				jqGridData[gridLen - 1]['color'] = getStrokeFromSpaceName(d.spaceMaster.groupId);
				jqGridData[gridLen - 1]['values'] = valObj;

				for ( var x = 0; x < dataLen; x++) {
					var distxt1 = jsonData.statMaster.abbr + " "
							+ data[x].occMaster.abbr;
					if (data[x].occMaster.displayName != undefined) {
						distxt1 = data[x].occMaster.displayName;
					}

					var valObjInner = {};
					valObjInner['x'] = distxt1;
					valObjInner['y'] = 0;
					valObjInner['color'] = getStrokeFromSpaceName(d.spaceMaster.groupId);
					valObj.push(valObjInner);
				}
				spaceTypes += ("," + ky);
			}
			var gridLen = jqGridData.length;
			for ( var x = 0; x < gridLen; x++) {
				if (jqGridData[x]['key'] == ky) {
					var valObj = jqGridData[x]['values'];
					for ( var y = 0; y < valObj.length; y++) {
						var valObjInner = valObj[y];
						if (valObjInner['x'] == distxt) {
							valObjInner['y'] = Number(d.value);
							valObjInner['color'] = getStrokeFromSpaceName(d.spaceMaster.groupId);
							// alert(JSON.stringify(valObjInner));
							break;
						}
					}

					break;
				}
			}

		}
	}
	//jqGridData = $.parseJSON('[{"key":"Closed Corridor","color":"#F05E27","values":[{"x":"Avg Last 30 days","y":33,"color":"#F05E27"},{"x":"Avg QTD","y":33,"color":"#F05E27"},{"x":"Avg YTD","y":33,"color":"#F05E27"}]},{"key":"Conference Room","color":"#CACA9E","values":[{"x":"Avg Last 30 days","y":50,"color":"#CACA9E"},{"x":"Avg QTD","y":50,"color":"#CACA9E"},{"x":"Avg YTD","y":50,"color":"#CACA9E"}]},{"key":"Open Office","color":"#75B000","values":[{"x":"Avg Last 30 days","y":24,"color":"#75B000"},{"x":"Avg QTD","y":24,"color":"#75B000"},{"x":"Avg YTD","y":24,"color":"#75B000"}]},{"key":"Egress","color":"#86D1E4","values":[{"x":"Avg QTD","y":53,"color":"#86D1E4"},{"x":"Avg YTD","y":53,"color":"#86D1E4"}]},{"key":"Lobby","color":"#E4F9A0","values":[{"x":"Avg QTD","y":46,"color":"#E4F9A0"},{"x":"Avg YTD","y":46,"color":"#E4F9A0"}]},{"key":"Warehouse","color":"#FFD512","values":[{"x":"Avg QTD","y":43,"color":"#FFD512"},{"x":"Avg YTD","y":43,"color":"#FFD512"}]}]');
	//console.log('jqgriddata:  ' + JSON.stringify(jqGridData));
}
function formatToDecimalPlaces(ip, decpl) {

	return parseFloat(Math.round(ip * 100) / 100).toFixed(decpl);
}
function generateStatTypeLegends(jsonData, d3gr, xpos, ypos) {
	var text = {
		svgelement : d3gr,
		id : 'statTitleId',
		stroke : "black",
		strokewidth : (0.1 * constants.xscale) + 'px',
		fill : "black",
		fontSize : (80 * constants.xscale),
		text : 'Statistic',
		textAnchor : 'left',
		translatex : (230 * constants.xscale),
		translatey : (20 * constants.yscale),
	}
	writeText(text);
	var spaceTypesLen = jsonData.allStatTypes.length;
	for ( var cnt = 0; cnt < spaceTypesLen; cnt++) {
		var d = jsonData.allStatTypes[cnt];
		ypos = ypos + constants.legendYSpacing;
		var text = {
			svgelement : d3gr,
			id : remWhiteSpaces("stat-" + d.abbr + "-legend"),
			stroke : 'blue',
			strokewidth : 0,
			fill : getStrokeFromStatName(d.name, jsonData.statMaster.name),
			fontSize : (60 * constants.xscale),
			text : d.name,
			textAnchor : 'left',
			translatex : (xpos),
			translatey : (ypos),
		}
		writeText(text);
		text = {
			svgelement : d3gr,
			id : remWhiteSpaces("stat-" + d.abbr + "-legendSymbol"),
			fill : getStrokeFromStatName(d.name, jsonData.statMaster.name),
			x : (230 * constants.xscale),
			y : (ypos - (8 * constants.yscale)),
			width : (8 * constants.xscale),
			height : (8 * constants.yscale),
		}
		drawRectangle(text);
	}

}

function getStrokeFromStatName(statName, selected, isrect) {
	if (statName == selected) {
		return 'blue';
	}
	if (isrect) {
		return 'white';
	}
	return 'black';
}
function remWhiteSpaces(str) {
	return str.replace(/ /g, "");
}

function generateGrapthTitle(jsonData, d3gr, xpos, ypos) {
	var text = {
		svgelement : d3gr,
		id : 'graphTitleId',
		stroke : '#D8D8D8',
		strokewidth : (0.1 * constants.xscale),
		fill : "#D8D8D8",
		fontSize : (100 * constants.xscale),
		text : 'Utilization',
		textAnchor : 'left',
		translatex : (120 * constants.xscale),
		translatey : (40 * constants.yscale),
	}
	writeText(text);
}

function getStrokeFromSpaceName(id) {

	var color;
	if (id < 29) {
		color = colors[id];
	} else {
		color = "red";
	}
	// if (spaceName == 'Conference Room') {
	// return "blue";
	// } else if (spaceName == 'Open Office') {
	// return "#FE9A2E";
	// } else if (spaceName == 'Private Office') {
	// return "green";
	// }
	// console.log(id + " -- > "+ color );
	return color;
}

function getRowIdFromSpaceType(tableName, clickedElement) {
	var rowId = [];
	// logic to get the exact rowId from spaceType
	var noOfRows = jQuery(tableName).jqGrid('getGridParam', 'records');
	for ( var j = 1; j <= noOfRows; j++) {
		var dataRow = jQuery(tableName).jqGrid('getRowData', j);
		if (isContainsString(clickedElement, dataRow.spaceType)) {
			rowId.push(j);
		}
	}
	return rowId;
}
function contains(a, obj) {
	for ( var i = 0; i < a.length; i++) {
		if (a[i] === obj) {
			return true;
		}
	}
	return false;
}

function generateUtilizationGraphTableGridData(jsonData, jqGridData) {
	var data = jsonData.data;
	var spaceTypes = "";
	var dataLen = data.length;

	for ( var i = 0; i < dataLen; i++) {

		var spaceDataLen = data[i].spaceData.length;
		var distxt = jsonData.statMaster.abbr + " " + data[i].occMaster.abbr;
		if (data[i].occMaster.displayName != undefined) {
			distxt = data[i].occMaster.displayName;
		}
		for ( var spacecnt = 0; spacecnt < spaceDataLen; spacecnt++) {

			var d = data[i].spaceData[spacecnt];
			var ky = d.spaceMaster.name;
			if (!isContainsString(spaceTypes, ky)) {
				var rowObj = {};
				rowObj['key'] = ky;
				jqGridData.push(rowObj);
				var gridLen = jqGridData.length;
				var valObj = [];
				jqGridData[gridLen - 1]['color'] = getStrokeFromSpaceName(d.spaceMaster.groupId);
				jqGridData[gridLen - 1]['values'] = valObj;

				for ( var x = 0; x < dataLen; x++) {
					var distxt1 = jsonData.statMaster.abbr + " "
							+ data[x].occMaster.abbr;
					if (data[x].occMaster.displayName != undefined) {
						distxt1 = data[x].occMaster.displayName;
					}

					var valObjInner = {};
					valObjInner['x'] = distxt1;
					valObjInner['y'] = 0;
					valObjInner['color'] = getStrokeFromSpaceName(d.spaceMaster.groupId);
					valObj.push(valObjInner);
				}
				spaceTypes += ("," + ky);
			}
			var gridLen = jqGridData.length;
			for ( var x = 0; x < gridLen; x++) {
				if (jqGridData[x]['key'] == ky) {
					var valObj = jqGridData[x]['values'];
					for ( var y = 0; y < valObj.length; y++) {
						var valObjInner = valObj[y];
						if (valObjInner['x'] == distxt) {
							valObjInner['y'] = Number(d.value);
							valObjInner['color'] = getStrokeFromSpaceName(d.spaceMaster.groupId);
							// alert(JSON.stringify(valObjInner));
							break;
						}
					}

					break;
				}
			}

		}
	}
	//console.log('jqgriddata:  ' + JSON.stringify(jqGridData));
}

function generateJqGridDataFromIP(jsonData, clickedElement, tableName, cols,
		colMod, spaceTylen, jqGridData) {
	var rowId = getRowIdFromSpaceType(tableName, clickedElement);
	for ( var cnt = 0; cnt < spaceTylen; cnt++) {
		var rowObj = {};
		rowObj['spaceType'] = jsonData.allSpaceTypes[cnt].name;

		var rowIdCnt = cnt + 1;
		if (contains(rowId, rowIdCnt)) {
			$("#" + rowIdCnt).hide();
			continue;
		} else {
			$("#" + rowIdCnt).show();
		}
		jqGridData.push(rowObj);
	}
	var dataLen = jsonData.data.length;
	var spaceDataLen = jsonData.data[0].spaceData.length;
	for ( var spacecnt = 0; spacecnt < spaceDataLen; spacecnt++) {
		for ( var i = 0; i < dataLen; i++) {
			var d = jsonData.data[i].spaceData[spacecnt];
			var distxt = jsonData.statMaster.abbr + " "
					+ jsonData.data[i].occMaster.abbr;
			if (isContainsString(clickedElement, d.spaceMaster.abbr)) {
				continue;
			}
			var statOccCombo = remWhiteSpaces(distxt + 'GridId' + i);
			if (spacecnt == 0) {
				cols.push(distxt);
				colMod.push({
					name : remWhiteSpaces(distxt + 'GridId' + i),
					editable : false,
					width : '10%',
					index : statOccCombo,
				});
			}
			if (i == (dataLen - 1)) {
				jqGridData[spacecnt][statOccCombo] = Number(d.value) + "%";
				jqGridData[spacecnt]['noOfSensors'] = d.sensors;
				jqGridData[spacecnt]['totalSensors'] = d.totalSensors;
				jqGridData[spacecnt]['totalSqFt'] = d.totalSqFt;
			} else {
				jqGridData[spacecnt][statOccCombo] = Number(d.value) + "%";
			}
		}

	}
}

function generateJqGridDataFromIP_ADV(jsonData, clickedElement, tableName, cols,
		colMod, spaceTylen, jqGridData, isNonFloorData) {
	var rowId = getRowIdFromSpaceType(tableName, clickedElement);
	for ( var cnt = 0; cnt < spaceTylen; cnt++) {
		var rowIdCnt = cnt + 1;
		if (contains(rowId, rowIdCnt)) {
			$("#" + rowIdCnt).hide();
			continue;
		} else {
			$("#" + rowIdCnt).show();
		}
	}
	var data = isNonFloorData ?jsonData.rollOverdata:  jsonData.data;
	//alert('JSON Data for Table:'+JSON.stringify(jsonData.rollOverdata));
	var spaceTypes = "";
	var dataLen = data.length;
	
	for ( var i = 0; i < dataLen; i++) {
		var spaceDataLen = data[i].spaceData.length;
		for ( var spacecnt = 0; spacecnt < spaceDataLen; spacecnt++) {
			var d = data[i].spaceData[spacecnt];
			var distxt = jsonData.statMaster.abbr + " "
					+ jsonData.data[i].occMaster.abbr;
			if (isContainsString(clickedElement, d.spaceMaster.abbr)) {
				continue;
			}
			var ky = d.spaceMaster.name;
			if (!isContainsString(spaceTypes, ky)) {
				spaceTypes += ("," + ky);
				var rowObj = {};
				rowObj['spaceType'] = ky;
				jqGridData.push(rowObj);
				
			}
			
			var statOccCombo = remWhiteSpaces(distxt + 'GridId' + i);
			if (spacecnt == 0) {
				cols.push(distxt);
				colMod.push({
					name : remWhiteSpaces(distxt + 'GridId' + i),
					editable : false,
					width : '10%',
					index : statOccCombo,
				});
			}
			var gridLen = jqGridData.length;
			for ( var x = 0; x < gridLen; x++) {
				if (jqGridData[x]['spaceType'] == ky) {
					var valObj = jqGridData[x];
					valObj[statOccCombo] = Number(d.value) + "%";
					valObj['noOfSensors'] = d.sensors;
					valObj['totalSensors'] = d.totalSensors;
					valObj['totalSqFt'] = d.totalSqFt;
					//alert(JSON.stringify(valObj));
					break;
				}
			}
			
		}

	}
}

var jqgridParamClickedElement = '';
var jqgridParamJsonData = '';
var jqgridParamAfterSortingFlag = false;
function renderUtilizationGraphGrid(jsonData, clickedElement, afterSortingFlag) {
	jqgridParamClickedElement = clickedElement;
	jqgridParamJsonData = jsonData;
	if (afterSortingFlag == undefined) {
		jqgridParamAfterSortingFlag = false;
	} else {
		jqgridParamAfterSortingFlag = afterSortingFlag;
	}
	var tableName = '#utilizationGraphTable';
	// $(tableName).empty();
	if (clickedElement != undefined) {
		// $(tableName).jqGrid('delRowData', (cnt+1));
	}
	var cols = [ "Space Type" ];
	var colMod = [ {
		name : 'spaceType',
		width : '7%',
		editable : false,
		index : 'spaceType',
	} ];
	var spaceTylen = jsonData.allSpaceTypes.length;
	var jqGridData = [];

	generateJqGridDataFromIP_ADV(jsonData, clickedElement, tableName, cols, colMod,
			spaceTylen, jqGridData);

	cols.push('Reporting Sensors');
	cols.push('Installed Sensors');
	cols.push('Total Sq Ft');
	colMod.push({
		name : 'noOfSensors',
		editable : false,
		width : '8%',
		index : 'noOfSensors',
	});
	colMod.push({
		name : 'totalSensors',
		editable : false,
		width : '8%',
		index : 'totalSensors',
	});
	colMod.push({
		name : 'totalSqFt',
		editable : false,
		width : '6%',
		index : 'totalSqFt',
	});

	jQuery(tableName)
			.jqGrid(
					{
						data : jqGridData,
						datatype : "local",
						height : 80,
						localReader : {
							autowidth : true,
							scrollOffset : 0,
							repeatitems : true,
							cell : "",
							id : "0",
							root : "rows",
							page : "page",
							total : "total",
							records : "records"
						},
						loadComplete : function(data) {
							forceFitoccChartHealthTableWidth();
						},
						gridComplete : function() {
							if (!jqgridParamAfterSortingFlag) {
								var rowId = getRowIdFromSpaceType(
										'#utilizationGraphTable',
										jqgridParamClickedElement);
								var spaceTylen = jqgridParamJsonData.allSpaceTypes.length;
								for ( var cnt = 0; cnt < spaceTylen; cnt++) {
									var rowIdCnt = cnt + 1;
									if (contains(rowId, rowIdCnt)) {
										$("#" + rowIdCnt).hide();
										continue;
									} else {
										$("#" + rowIdCnt).show();
									}
								}

							}
						},
						colNames : cols,
						colModel : colMod,
						pager : '#p' + "utilizationGraphTable",
						caption : "",
					});
}
function forceFitoccChartHealthTableWidth() {
	var jgrid = jQuery("#utilizationGraphTable");
	var containerHeight = $("body").height();
	var outerDivHeight = 0;// $("#outerDiv").height();
	var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
	var gridFooterHeight = $("#putilizationGraphTable").height();
	// jgrid.jqGrid("setGridHeight", containerHeight -outerDivHeight -
	// gridHeaderHeight - gridFooterHeight - 440);
	// console.log("containerHeight " + containerHeight);
	$("#utilizationGraphTable").setGridWidth($(window).width() - 25);
}

// ////////////////////////////////////////// NON FLOOR LEVEL CHART FUNCTIONS -
// START
// ///////////////////////////////////////////////////////////////////////////////////

var occType = "Last 30 days";
var isRollOverData = false;
function drawNonFloorUtilizationGrapth(w, h) {
	scaleConstantsFromGrpahWidthHeight(w, h);

	// These attributes affect all
	// graphical elements in bargroup.

	var baseUrl = $('input#baseUrlFacilityOccReport').val();
	var customerId = $('input#customerId').val();
	var facilityType = $('input#otreenodetype').val();
	var levelId = $('input#otreenodeid').val();
	console.log("facilityType:levelId" + facilityType + ":" + levelId);
	var path = baseUrl.concat(occType).concat("/").concat(facilityType).concat(
			"/").concat(customerId).concat("/").concat(levelId);

	$.ajax({
		type : 'GET',
		data : {
			rowData : {}
		},
		dataType : 'json',
		url : path,
		async : true,
		beforeSend : function() {
		},
		complete : function() {
		},
		success : function(response, textStatus, xhr) {
			var jsonResponse = xhr.responseText;
			console.log(jsonResponse);
			var jsonData = $.parseJSON(jsonResponse);
			var childData = jsonData["data"];
			var parentData = jsonData["rollOverdata"];
			if (parentData != undefined) {
				isRollOverData = true;
			}

			// alert(JSON.stringify(childData));
			var datalen = jsonData.data.length;
			renderAllNonFloorStatisticTypes(jsonData);
			renderAllNonFloorOccTypes(jsonData);
			//renderNonFloorNvd3BarChart(jsonData);
			renderNonFloorC3BarChart(jsonData);
			//renderNonFloorC3BarChartForDemo(jsonData);
			renderNonFloorSensorDonutChart(jsonData);
			renderNonFloorTotalSqFtDonutChart(jsonData);
			renderNonFloorUtilizationGraphGrid(jsonData);
		},
		error : function(xhr, textStatus, errorThrown) {
			alert("Error occurred while setting dependet values" + errorThrown
					+ "TextStatus:" + textStatus)
					+ "xhr" + xhr;
		}
	});
}

function renderAllNonFloorStatisticTypes(jsonData, clickedElement) {

	var spaceTylen = jsonData.allStatTypes.length;
	var statMasterName = jsonData.statMaster.name;

	var length = $("#nonFloorallStatTypes").length;
	if (length > 0) {
		$("#nonFloorallStatTypes").empty();
	}
	var length = $("#nonFloorallStatTypes").length;
	$("#nonFloorallStatTypes")
			.append(
					'<span id="statisticTypeTitle">Statistics Types</span>&nbsp;&nbsp;:');
	for ( var cnt = 0; cnt < spaceTylen; cnt++) {
		var statType = jsonData.allStatTypes[cnt].name;
		var checkedStr = "";
		if (statMasterName == statType) {
			checkedStr = 'checked="checked"';
		}
		$("#nonFloorallStatTypes").append(
				'&nbsp;<input type="radio" name="statisticTypeId" id="statisticTypeId" value="'
						+ statType + '"' + checkedStr + ' >' + statType
						+ '</input> &nbsp;&nbsp;');
	}
	$("#nonFloorallStatTypes").append('</br>');
}

function renderAllNonFloorOccTypes(jsonData, clickedElement) {
	var spaceTylen = jsonData.allOccTypes.length;
	var statMasterName = jsonData.data[0].occMaster.name;

	var length = $("#nonFloorallOccTypes").length;
	if (length > 0) {
		$("#nonFloorallOccTypes").empty();
	}

	$("#nonFloorallOccTypes").append(
			'<span id="occTypeTitle">Occupancies</span>&nbsp;&nbsp;:');
	for ( var cnt = 0; cnt < spaceTylen; cnt++) {
		var statType = jsonData.allOccTypes[cnt].name;
		var checkedStr = "";
		if (statMasterName == statType) {
			checkedStr = 'checked="checked"';
			occType = statType;
		}
		$("#nonFloorallOccTypes")
				.append(
						'<input type="radio" name="occTypeId" id="occTypeId" onChange="handleOccTypeChange(event)" value="'
								+ statType
								+ '"'
								+ checkedStr
								+ ' >'
								+ statType
								+ '</input> &nbsp;&nbsp;');
	}
}
function handleOccTypeChange(event) {
	occType = event.currentTarget.value;
	drawNonFloorUtilizationGrapth();
	generateLeastMostOccupiedTable();
}
function getC3FormatData(nvd3FormattedData,isFloorLevel){
	var mainJson = {};
	var data = {};
	data.names = {};
	data.type="bar";
	data.groups=[],data.groups[0] = [];
	data.colors = {};
	data.columns=[];
	var categoryMap = {},category=[];
	//alert(prevdata[0].key);
	for(var i=0;i<nvd3FormattedData.length;i++){
		var dataname = "data"+i;
		var  k = nvd3FormattedData[i].key.replace(" ","");
		data.names[dataname] = k;
		data.groups[0].push(dataname);
		data.colors[dataname] = nvd3FormattedData[i].color;
		data.columns[i] = [];
		data.columns[i].push(dataname);
		var values = nvd3FormattedData[i].values;
		for(var j=0;j<values.length;j++){
			data.columns[i].push(nvd3FormattedData[i].values[j].y);
			if(!categoryMap[nvd3FormattedData[i].values[j].x]){
				categoryMap[nvd3FormattedData[i].values[j].x] = nvd3FormattedData[i].values[j].x;
				category.push(nvd3FormattedData[i].values[j].x);
			}
			
		}
	}
	var bindto = "#nonFloorbarChart";
	if(isFloorLevel)
	{
		bindto = "#barChart";
		data.groups=[];
	}
	mainJson.bindto = bindto;
	//mainJson.padding ={top: 0,bottom: -100,left: 100};
	mainJson.data = data;
	mainJson.axis = {x:{type: 'category',categories:category},rotated:true,y:{show:false}};
	if(isFloorLevel)
	{
		mainJson.axis = {x:{type: 'category',categories:category},y:{show:true,min:0,max:91,label: {text: 'Occupancy %',position: 'outer-middle'}}};
	}
	mainJson.tooltip = {grouped: false};
	mainJson.zoom={enabled:true};
	mainJson.legend = {position:'top',inset:{anchor:'top-left'}},
	mainJson.bar = {width:barWidth}
	mainJson.transition = {duration: 1000};
	return mainJson;
	
}
function renderNonFloorC3BarChart(jsonData) {
	var prevdata = generateBarChartData(jsonData, true);
	
	var mainJson = getC3FormatData(prevdata);
	//alert(JSON.stringify(mainJson));
	var chart = c3.generate(mainJson);
	chart.resize();
}
function renderNonFloorC3BarChartForDemo(jsonData) {
	var chart = c3.generate({
		bindto:"#nonFloorbarChart",
		data: {
				columns: [
							['data1', 46,33,Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1)],
							['data2', 0, 33,Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1)],
							['data3', 0, 33,Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1)],
							['data4', 22, 33,Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1),Math.floor((Math.random() * 100) + 1)],
							['data5', 45, 33],
							['data6', 100, 33],
							['data7', 22, 33],
							['data8', 44, 33],
							['data9', 22, 33],
							['data10', 66, 33],
							['data11', 77,33],
							['data12', 98, 33],
							['data13', 12, 33],
							['data14', 2, 33],
							['data15', 29, 33],
							['data16', 29, 33],
							['data17', 0, 33],
							['data18', 29, 33]
				         ],
				names:{data1:'Open Office', data2:'Closed Corridor',data3:'Conference Room',
					data4:'data4',
					data5:'data5',
					data6:'data6',data7:'data7',
					data8:'data8',
					data9:'data9',
					data10:'data10',
					data11:'data11',
					data12:'data12',
					data13:'data13',
					data14:'data14',
					data15:'data15',
					data16:'data16',
					data17:'data17',
					data18:'data18'},         
	            type: 'bar',
	         groups: [['data1', 'data2','data3','data4', 'data5','data6','data7', 'data8','data9','data10','data11', 'data12','data13','data14', 'data15','data16','data17', 'data18']],
	            //groups: [['data1', 'data2'],['data3','data4'],['data5','data6']],
	            colors:{data1:colors[1],data2:colors[2],data3:colors[3],data4:colors[4],data5:colors[5],data6:colors[6],data7:colors[7],data8:colors[8],data9:colors[9],data10:colors[10],
	            	data11:colors[11],data12:colors[12],data13:colors[13],data14:colors[14],data15:colors[15],data16:colors[16],data17:colors[17],data18:colors[18]}
		},
		axis: {
			x: {type: 'category',categories:['Floor 7 North','Floor 7 South'
				,'Floor 8 South'
			,'Floor 9 South'
			,'Floor 17 South'
			,'Floor 27 South'
			,'Floor 37 South'
			,'Floor 47 South'
			,'Floor 57 South'
			,'Floor 67 South'
			,'Floor 77 South'
			,'Floor 87 South'
			,'Floor 97 South'
			,'Floor 107 South']},
			rotated:true
		},
		tooltip:{grouped:false},
		zoom:{enabled:true},	
		legend: {position:'top',inset:{anchor:'top-left',x:0,y:0}},
		bar:{width:barWidth}
	}); 
	chart.legend.hide(['data2','data4','data6','data8']);
	//d3.select(".c3-bars-data2>path").style("fill","rgb(200, 108, 78)");
	//d3.selectAll(".c3-bars-data2>path").style("fill","rgb(200, 108, 78)");

	chart.resize();
	//alert($(".c3-bars-data2 > path").get(0));
	//$(".c3-bars-data2 > path").get(0).attr("fill","rgb(200, 108, 78)");
	//d3.selectAll(".c3-bars-data2 > path").style("fill","rgb(200, 108, 78)");
	//$(".c3-bars-data2 > path").get(0).style.fill="rgb(200, 108, 78)";
	
}
function renderNonFloorNvd3BarChart(jsonData) {
	//alert(JSON.stringify(jsonData.data));
	var dataLen = jsonData.data.length;
//	jsonData.data.push({"spaceData":[{"spaceMaster":{"id":3,"name":"Open Office","abbr":"Open Office","groupId":9},"sensors":4,"totalSqFt":1000,"value":0,"totalSensors":10}],"occMaster":{"id":0,"name":"Last 30 days","abbr":"Last 30 days","displayName":"Floor 8"}});
//	jsonData.data.push({"spaceData":[{"spaceMaster":{"id":3,"name":"Open Office","abbr":"Open Office","groupId":9},"sensors":4,"totalSqFt":1000,"value":0,"totalSensors":10}],"occMaster":{"id":0,"name":"Last 30 days","abbr":"Last 30 days","displayName":"Floor 9"}});
//	jsonData.data.push({"spaceData":[{"spaceMaster":{"id":3,"name":"Open Office","abbr":"Open Office","groupId":9},"sensors":4,"totalSqFt":1000,"value":0,"totalSensors":10}],"occMaster":{"id":0,"name":"Last 30 days","abbr":"Last 30 days","displayName":"Floor 10"}});
	nv.addGraph(function() {

	var chart = nv.models.multiBarHorizontalChart()
		              .transitionDuration(1350)
		              .margin({top: 0, right: 0, bottom: 0, left: 75})
				      .showYAxis(false)
		 		      .stacked(true)
				      .showControls(false)
		;

		chart.yAxis.tickFormat(d3.format(',.0f'));
		var s = d3.select('#nonFloorbarChart svg');
		var data = generateBarChartData(jsonData, true);
		//alert(JSON.stringify(data));
		// var data = $.parseJSON(
		// '[{"key":"Open Office","color":"#CACA9E","values":[{"x":"Floor 7
		// South","y":33,"color":"#CACA9E"},{"x":"Floor 7
		// 1South","y":23,"color":"#CACA9E"}]},{"key":"Conference
		// Room","color":"#75B000","values":[{"x":"Floor 7
		// South","y":33,"color":"#75B000"},{"x":"Floor 7
		// 1South","y":23,"color":"#CACA9E"}]},{"key":"Closed
		// Corridor","color":"#F05E27","values":[{"x":"Floor 7
		// South","y":33,"color":"#F05E27"},{"x":"Floor 7
		// 1South","y":23,"color":"#CACA9E"}]}]');

		s.datum(data).call(chart);
		if (dataLen < 5) {
			barSpacing = 6;
			setTimeout(function() {
				//alert(chart.yAxis.rangeBand());
				//alert(chart.xAxis.rangeBand()+':/:'+barSpacing);
				d3.selectAll(".nv-bar > rect").attr("height",
						chart.xAxis.rangeBand() / barSpacing);
				d3.selectAll(".nv-bar > rect").attr("height",
						chart.yAxis.rangeBand() / barSpacing);
			}, 400)
		}

		nv.utils.windowResize(chart.update);

		try {
			// add zoom handler
			function addZoom(options) {
				// scaleExtent
				var scaleExtent = 10;

				// parameters
				var yAxis = options.yAxis;
				var xAxis = options.xAxis;
				var xDomain = options.xDomain || xAxis.scale().domain;
				var yDomain = options.yDomain || yAxis.scale().domain;
				var redraw = options.redraw;
				var svg = options.svg;
				var discrete = options.discrete;

				// scales
				var xScale = xAxis.scale();
				var yScale = yAxis.scale();

				// min/max boundaries
				var x_boundary = xScale.domain().slice();
				var y_boundary = yScale.domain().slice();

				// create d3 zoom handler
				var d3zoom = d3.behavior.zoom();

				// ensure nice axis
				xScale.nice();
				yScale.nice();

				// fix domain
				function fixDomain(domain, boundary) {
					if (discrete) {
						domain[0] = parseInt(domain[0]);
						domain[1] = parseInt(domain[1]);
					}
					domain[0] = Math.min(Math.max(domain[0], boundary[0]),
							boundary[1] - boundary[1] / scaleExtent);
					domain[1] = Math.max(boundary[0] + boundary[1]
							/ scaleExtent, Math.min(domain[1], boundary[1]));
					return domain;
				}
				;

				// zoom event handler
				function zoomed() {
					yDomain(fixDomain(yScale.domain(), y_boundary));
					xDomain(fixDomain(xScale.domain(), x_boundary));
					redraw();
				}
				;

				// zoom event handler
				function unzoomed() {
					xDomain(x_boundary);
					yDomain(y_boundary);
					redraw();
					d3zoom.scale(1);
					d3zoom.translate([ 0, 0 ]);
				}
				;

				// initialize wrapper
				d3zoom.x(xScale).y(yScale).scaleExtent([ 1, scaleExtent ]).on(
						'zoom', zoomed);

				// add handler
				svg.call(d3zoom).on('dblclick.zoom', unzoomed);
			}
			;

			// add zoom
			addZoom({
				xAxis : chart.xAxis,
				yAxis : chart.yAxis,
				yDomain : chart.yDomain,
				xDomain : chart.xDomain,
				redraw : function() {
					chart.update()
				},
				svg : s,
			});
			nv.utils.windowResize(chart.update);
		} catch (err) {
			console.log(err);
		}

		return chart;
	}, function() {
		$('.nv-series').click(
				function() {
					var skipElements = $(
							'#nonFloorbarChart .nv-legend .disabled').text();
					renderNonFloorSensorDonutChart(jsonData, skipElements);
					renderNonFloorTotalSqFtDonutChart(jsonData, skipElements);
					renderNonFloorUtilizationGraphGrid(jsonData, skipElements);
				})
	});
}

function renderNonFloorSensorDonutChart(jsonData, clickedElement) {
	var c = generateColorRangeForDonut(jsonData, clickedElement);

	var w = $("#nonFloorsensorDonutChart").width();
	var h = $("#nonFloorsensorDonutChart").height();
	var radW = 170;
	var radH = 170;
	var radWHRatio = radW / radH;
	nv.addGraph(function() {
		var chart = nv.models.pieChart().x(function(d) {
			return d.label
		}).y(function(d) {
			return Number(d.value)
		}).color(c).showLegend(false).showLabels(true) // Display pie labels
		.labelThreshold(0.05) // Configure the minimum slice size for labels
		// to show up
		.labelType("percent") // Configure what type of data to show in the
		// label. Can be "key", "value" or "percent"
		.donut(true) // Turn on Donut mode. Makes pie chart look tasty!
		.donutRatio(0.35) // Configure how big you want the donut hole size to
		// be.
		.width(radW * w / 250).height(radH * h / 175);
		var radMminWH = Math.min(radW, radH);
		d3.select("#nonFloorsensorDonutChart svg").attr(
				"viewBox",
				(45 * (radW / 200) * w / 250) + "  "
						+ (49 * (radH / 200) * h / 175) + "  "
						+ (115 * (radW / 200) * w / 250) + " "
						+ (115 * (radH / 200) * h / 175));
		$("#nonFloorsensorDonutChart svg").text('');
		d3.select("#nonFloorsensorDonutChart svg").append("text").attr("x",
				"86%").attr("y", (radH / 2) + 10).attr("text-anchor", "middle")
				.style("font-size", (radMminWH * 30 / 100) + "%").style(
						"font-weight", "bold").text("Sensor Count");

		d3.select("#nonFloorsensorDonutChart svg").datum(
				generateSensorDonutChartData(jsonData, clickedElement))
				.transition().duration(350).call(chart);

		$(window).resize(function() {
			renderNonFloorSensorDonutChart(jsonData, clickedElement);
		});
		return chart;
	});
}

function renderNonFloorTotalSqFtDonutChart(jsonData, clickedElement) {
	var c = generateColorRangeForDonut(jsonData, clickedElement);

	var w = $("#nonFloortotalsqftDonutChart").width();
	var h = $("#nonFloortotalsqftDonutChart").height();
	var radW = 170;
	var radH = 170;
	var radWHRatio = radW / radH;
	nv.addGraph(function() {
		var chart = nv.models.pieChart().x(function(d) {
			return d.label
		}).y(function(d) {
			return Number(d.value)
		}).color(c).showLegend(false).showLabels(true) // Display pie labels
		.labelThreshold(.05) // Configure the minimum slice size for labels
		// to show up
		.labelType("percent") // Configure what type of data to show in the
		// label. Can be "key", "value" or "percent"
		.donut(true) // Turn on Donut mode. Makes pie chart look tasty!
		.donutRatio(0.35) // Configure how big you want the donut hole size to
		// be.
		.width(radW * w / 250).height(radH * h / 175);
		var radMminWH = Math.min(radW, radH);
		d3.select("#nonFloortotalsqftDonutChart svg").attr(
				"viewBox",
				(45 * (radW / 200) * w / 250) + "  "
						+ (49 * (radH / 200) * h / 175) + "  "
						+ (115 * (radW / 200) * w / 250) + " "
						+ (115 * (radH / 200) * h / 175));
		$("#nonFloortotalsqftDonutChart svg").text('');
		d3.select("#nonFloortotalsqftDonutChart svg").append("text").attr("x",
				"86%").attr("y", (radH / 2) + 10).attr("text-anchor", "middle")
				.style("font-size", (radMminWH * 30 / 100) + "%").style(
						"font-weight", "bold").text("Area Sq Ft");

		d3.select("#nonFloortotalsqftDonutChart svg").datum(
				generateTotalSqFtDonutChartData(jsonData, clickedElement))
				.transition().duration(350).call(chart);

		$(window).resize(function() {
			renderTotalSqFtDonutChart(jsonData, clickedElement);
		});

		return chart;
	});
}

var jqgridParamNonFloorClickedElement = '';
var jqgridParamNonFloorJsonData = '';
var jqgridParamNonFoorAfterSortingFlag = false;
function renderNonFloorUtilizationGraphGrid(jsonData, clickedElement,
		afterSortingFlag) {
	jqgridParamNonFloorClickedElement = clickedElement;
	jqgridParamNonFloorJsonData = jsonData;
	if (afterSortingFlag == undefined) {
		jqgridParamNonFoorAfterSortingFlag = false;
	} else {
		jqgridParamNonFoorAfterSortingFlag = afterSortingFlag;
	}
	var tableName = '#nonFloorutilizationGraphTable';

	// var noOfRows = jQuery(tableName).jqGrid('getGridParam', 'records');
	// for(var j=1; j <= noOfRows; j++){
	// $(tableName).jqGrid('delRowData', j);
	// }
	$(tableName).jqGrid('GridUnload');

	if (clickedElement != undefined) {
		// $(tableName).jqGrid('delRowData', (cnt+1));
	}
	var cols = [ "Space Type" ];
	var colMod = [ {
		name : 'spaceType',
		width : '10%',
		editable : false,
		index : 'spaceType',
	} ];
	var spaceTylen = jsonData.allSpaceTypes.length;
	console.log("spaceTylen " + spaceTylen);
	var jqGridData = [];
	generateJqGridDataFromIP_ADV(jsonData, clickedElement, tableName, cols, colMod,
			spaceTylen, jqGridData,true);
	cols.push('Reporting Sensors');
	cols.push('Installed Sensors');
	cols.push('Total Sq Ft');
	colMod.push({
		name : 'noOfSensors',
		editable : false,
		width : '6%',
		index : 'noOfSensors',
	});
	colMod.push({
		name : 'totalSensors',
		editable : false,
		width : '6%',
		index : 'totalSensors',
	});
	colMod.push({
		name : 'totalSqFt',
		editable : false,
		width : '6%',
		index : 'totalSqFt',
	});

	jQuery(tableName)
			.jqGrid(
					{
						data : jqGridData,
						datatype : "local",
						localReader : {
							autowidth : true,
							scrollOffset : 0,
							repeatitems : true,
							cell : "",
							id : "0",
							root : "rows",
							page : "page",
							total : "total",
							records : "records"
						},
						loadComplete : function(data) {
							forceFitoNonFloorChartHealthTableWidth();
						},
						gridComplete : function() {
							if (!jqgridParamNonFoorAfterSortingFlag) {
								var rowId = getRowIdFromSpaceType(
										'#nonFloorutilizationGraphTable',
										jqgridParamNonFloorClickedElement);
								var spaceTylen = jqgridParamNonFloorJsonData.allSpaceTypes.length;
								for ( var cnt = 0; cnt < spaceTylen; cnt++) {
									var rowIdCnt = cnt + 1;
									if (contains(rowId, rowIdCnt)) {
										$("#" + rowIdCnt).hide();
										continue;
									} else {
										$("#" + rowIdCnt).show();
									}
								}

							}
						},
						colNames : cols,
						colModel : colMod,
						pager : '#p' + "nonFloorutilizationGraphTable",
						rowNum : 10000,
						rowList : [ 10000 ],
						caption : "",
					});
}
function forceFitoNonFloorChartHealthTableWidth() {
	var jgrid = jQuery("#nonFloorutilizationGraphTable");
	var containerHeight = $("body").height();
	var outerDivHeight = 0;// $("#outerDiv").height();
	var gridHeaderHeight = $("div.ui-jqgrid-hdiv").height();
	var gridFooterHeight = $("#pnonFloorutilizationGraphTable").height();
	jgrid.jqGrid("setGridHeight", 100);
	$("#nonFloorutilizationGraphTable").setGridWidth($(window).width() - 25);
}

// ////////////////////////////////////////NON FLOOR LEVEL CHART FUNCTIONS - END
// ///////////////////////////////////////////////////////////////////////////////////

////////////////////////////////////////////////////////////////////////////////////////////////
///////////START MOST Occupied/LEAST Occupied
///////////////////////////////////////////////////////////////////////////////////////////////

var lpath;
var otreenodetype;
var otreenodeid;
function generateLeastMostOccupiedTable(){
	lpath=parent.path;
	$("#nonFloorbreadscrumHeader").text(lpath);
	var initialSelectedNode = $.cookie('uem_facilites_jstree_select',{ path: '/' });
	initialSelectedNode = initialSelectedNode.replace("#","");
	var nodeDetails = getFacilityNodeDetails(initialSelectedNode);
	otreenodetype = nodeDetails[0];
	otreenodeid = nodeDetails[1];
	$("#otreenodetype").val(otreenodetype);
	$("#otreenodeid").val(otreenodeid);
	var levelHeader = "Campus";
	if(otreenodetype=='campus'){
		levelHeader = "Building";
	}else if(otreenodetype=='building'){
		levelHeader = "Floor";
	}
	$("#tableTitle").text('');
	$("#tableTitle").append(levelHeader+" wise "+occType+" occupancy");
	
	$("#mostOccTable").jqGrid('GridUnload');
	drawNonFloorUtilizationGrapth(800, 525);
	jQuery("#mostOccTable").jqGrid({
		datatype: "local",
		autoencode: true,
		hoverrows: false,
		autowidth: false,
		scrollOffset: 0,
		height:120,
		forceFit: true,
		colNames:['Sr.No',levelHeader,'Reporting Sensors','Total Sq Ft',occType+' Occupancy %'],
	   	colModel:[
	   		{name:'srno',index:'srno', width:'10%'},
	   		{name:'levelName',index:'levelName', width:'30%'},
	   		{name:'nos',index:'nos', width:'20%', align:"right"},
	   		{name:'sqft',index:'sqft', width:'20%', align:"right"},	
	   		{name:'occupPercent',index:'occupPercent',sortable:true,sorttype:'number',width:'20%',align:"right"}		
	   	],
	 	cmTemplate: { title: false },
	    page: 1,
	    sortorder: "desc",
	   	sortname: "occupPercent",
	    hidegrid: false,
	    viewrecords: true,
	   	loadui: "block",
	   	toolbar: [false,"top"],
	   	loadComplete: function(data) {
	   	}
	});
	
	var baseUrl = $('input#baseUrlChildFacilityOccReport').val();;
	var customerId = $('input#customerId').val();
	var facilityType =  $('input#otreenodetype').val();
	var levelId =  $('input#otreenodeid').val();
	var path = baseUrl.concat(occType).concat("/").concat(facilityType).concat("/").concat(customerId).concat("/").concat(levelId); 
	
	$.ajax({
		type : 'GET',
		dataType : 'json',
		url : path,
		async : true,
		success : function(response, textStatus, xhr) {
			$.each(response,function(index){
				this["id"]=index+1;
				this["srno"]=index+1;
				jQuery("#mostOccTable").jqGrid('addRowData',index+1,this);
			});
			$("#mostOccTable").trigger("reloadGrid");
		},
		error : function(xhr, textStatus, errorThrown) {
			alert("Error occurred while setting dependet values" + errorThrown
					+ "TextStatus:" + textStatus)
					+ "xhr" + xhr;
		}
	});
	forceFitMostOccTableHeight();
	
	$(window).resize(function() {
		forceFitMostOccTableHeight();
		$("#nonFloorutilizationGraphTable").setGridWidth($(window).width() - 25);
	});
}
	
function forceFitMostOccTableHeight(){
	$("#mostOccTable").setGridWidth(($(window).width())-20);
}
var getFacilityNodeDetails=function(name){
		var arr=$(name.split('_'));		
		return arr;				
}





