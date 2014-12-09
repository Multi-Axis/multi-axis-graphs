var chartData;
var period = [];
var chart;
var wholeData;
var originalChart;
var zoomRange = [];
var temp;
var zoomed;

$(function(){

  document.getElementById('zoomReset').addEventListener('click', function() {
    document.getElementById('zoomReset').style.visibility = 'hidden'
    draw();
  })

  $("#periodFrom").bind('input', function() {
    updatePeriod($("#periodFrom").val(), 0)
  })

  $("#periodTo").bind('input', function() {
    updatePeriod($("#periodTo").val(), 1)
  })

  document.getElementById('trendForecast').addEventListener('click', function() {
    wholeData.params.stop_lower = 14 * -86400;
    var days = document.getElementById('trendDays').value;
    if (!isNaN(parseInt(days))) {
      wholeData.params.stop_lower = days * -86400;
    }
    wholeData.params.stop_upper = null;
    console.log(wholeData.params)
    postData(wholeData.params, wholeData.threshold.value);
  });

  $.getJSON("/api/" + getCurrentFutid(), function(data) {
    drawAndSetData(data);
  });
})

function postData(params, threshold, doPost) {
  f=doPost ? $.post : $.get;
  f("/api/" + getCurrentFutid(), {
      'params': JSON.stringify(params),
      'threshold':threshold
  }, function(data) {
    drawAndSetData(data)
  }, "json")
}

function getCurrentFutid() {
  return $("#itemhost").data("futid");
}

function setData(data) {
  chartData = [
    {
      key: "History",
      values: data.history
    },
    {
      key: "Future",
      values: data.future,
      color: "green"
    }
  ];
  $("#modelSelect").val(data.model);
	$("#details").text(JSON.stringify(data.details));
	$("#itemhost").text(data.metric + " @ " + data.host);
  $("#params").val(JSON.stringify(data.params));
  $("#threshold_lower").prop('checked', data.threshold.lower);
  $("#threshold_higher").prop('checked', !data.threshold.lower);
}

function updateParams() {
  $("#params").val(JSON.stringify(wholeData.params));
}

var timeFormat = function(d) {
  var date = new Date(0);
  date.setUTCSeconds(d);
  return d3.time.format('%Y %b %d, %H:%M')(date);
};

var sortAscending = function(a, b) {
  return a-b;
}

Array.min = function(array) {
    return Math.min.apply(Math, array.map(function(x){return x.val;}));
}

Array.max = function(array) {
    return Math.max.apply(Math, array.map(function(x){return x.val;}));
}



function draw() {
  nv.addGraph(function(data) {
    //chart configurations
    chart = nv.models.lineChart()
                  .margin({left: 100})
                  .useInteractiveGuideline(true)
                  .transitionDuration(350)
                  .showLegend(true)
                  .showYAxis(true)
                  .showXAxis(true);

    //set up X and Y-axis
    chart.xAxis.axisLabel('Time').tickFormat(timeFormat);
    chart.yAxis.axisLabel('Values').tickFormat(d3.format('.02f'));

    //set domain based on history, future and threshold
    chart.yDomain(getYDomain(chartData, wholeData.threshold.value))

    //render the chart
    d3.select('#chart svg').datum(chartData).call(chart);

    renderThreshold(chart);

    //if the period comes from db it should be rendered
    chart.interactiveLayer.clearPeriodLines();
    chart.interactiveLayer.renderPosition(wholeData.params.stop_lower)
    chart.interactiveLayer.renderPosition(wholeData.params.stop_upper)
    period = [];
    if (wholeData.params.stop_upper != null) {
      period.push(wholeData.params.stop_upper)
    }
    if (wholeData.params.stop_lower != null) {
      period.push(wholeData.params.stop_lower);
    }
    appendStartAndEnd(period)
    
    //Update the chart when window resizes.
    nv.utils.windowResize(function() { 
      chart.update();
      renderThreshold(chart);
    });

    chart.interactiveLayer.dispatch.on('elementMouseup', function(e) {
      var zoomPeriod = [e.xValue, e.x2Value];
      zoomPeriod.sort(sortAscending);
      drawZoomedChart(zoomPeriod);
      chart.interactiveLayer.clearZoomCurtain();
    })

    // draw a line when chart is clicked
    chart.interactiveLayer.dispatch.on('elementClick', function(e) {
                                                          if (e != undefined && period.length<2) {
                                                            chart.interactiveLayer.renderPosition(e.pointXValue)
                                                            period.push(e.pointXValue);
                                                            period.sort(sortAscending);

                                                            if (period.length == 2) {appendStartAndEnd(period)}
                                                        }});
    //clear start and end points
    document.getElementById('clearPeriods').addEventListener('click', function() {
                                                                        chart.interactiveLayer.clearPeriodLines();
                                                                        period = [];
                                                                        wholeData.params.stop_lower = null;
                                                                        wholeData.params.stop_upper = null;
                                                                      });
    
    document.getElementById('threshold').value = wholeData.threshold.value;
    return chart;
  });
}



//Clear the previous threshold line and render the given threshold
function appendStartAndEnd(period) {
  wholeData.params.stop_lower = Math.round(period[0]);
  wholeData.params.stop_upper = Math.round(period[1]);
  $("#periodFrom").val(timeFormat(period[0]));
  $("#periodTo").val(timeFormat(period[1]));
 
  updateParams();
}

function renderThreshold(chart) {
  chart.interactiveLayer.clearThresholdLineAndText();
  chart.interactiveLayer.renderThreshold(chart.yScale()(wholeData.threshold.value));
}


function drawZoomedChart(data) {
  var newHistory = chartData[0].values.filter(function(d) {return d.time > data[0] && d.time < data[1]})
  var newFuture = chartData[1].values.filter(function(d) {return d.time > data[0] && d.time < data[1]})
  var tempChartData = JSON.parse(JSON.stringify(chartData));
  if (newHistory.length == 0) {
    chartData[0] = chartData[1];
    chartData.pop();
  } else {
    chartData[0].values = newHistory;
  }
  if (newFuture.length == 0) {
    chartData.pop();
  }
  draw();
  document.getElementById('zoomReset').style.visibility = 'visible';
  //the draw above needs this time to draw before chartData is returned back to its original state
  setTimeout(function() {
    chartData = tempChartData
  }, 1000)
}

function drawAndSetData(data) {
  period = [];
  wholeData = data;
  setData(data);
  draw();
}

function setPeriodParams() {
  if(period.length == 2) {
    wholeData.params.stop_lower = period[0];
    wholeData.params.stop_upper = period[1];
    updateParams();
  }
}

function updatePeriod(value, i) {

  var epoch = new Date(value).getTime()/1000;
  console.log(epoch)
  if (!isNaN(epoch)) {
    console.log("ss")
    period[i] = epoch;
    console.log(period)
    chart.interactiveLayer.clearPeriodLines();
    chart.interactiveLayer.renderPosition(period[0])
    chart.interactiveLayer.renderPosition(period[1])
  }
}

function getYDomain(chartData, threshold) {
  if (threshold !== undefined) {
    var domain = [];
    //we don't always know how many data objects there are in the chart
    for (i = 0; i < chartData.length; i++) {
      domain.push(Math.min(threshold, Array.min(chartData[i].values)));
      domain.push(Math.max(threshold, Array.max(chartData[i].values)));
    }
    domain.sort(sortAscending);
    //After sorting all the potential domain max and min the real domain
    //is in the first and last position in the array.
    return [domain.slice(0)[0]-1, domain.slice(-1)[0]*1.3];
  } else {
    return undefined;
  }
}
