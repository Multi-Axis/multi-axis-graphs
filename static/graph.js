var chartData;
var period = [];
var chart;
var wholeData;
var originalChart;
var zoomRange = [];
var temp;

$(function(){
  document.getElementById('sendPeriod').addEventListener('click', function() {
    var threshold = document.getElementById('threshold').value;
    console.log(threshold)
    setPeriodParams();
    postData(wholeData.params, threshold, true);
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
  })

  $.getJSON("/api/" + getCurrentFutid(), function(data) {
    drawAndSetData(data);
  })
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

function initSlider(data) {
    $("#slider").slider({
      range: true,
      min: data.history[0].time,
      max: data.future.slice(-1)[0].time,
      values: [0, data.future.slice(-1)[0].time],
      slide: function( event, ui ) {
        $("#zoomRange").val(timeFormat(ui.values[0]) + " - " + timeFormat(ui.values[1]));
        zoomRange = ui.values
      },
      stop: function(event, ui) {
        drawZoomedChart(zoomRange);
      }
    })
    zoomRange = $('#slider').slider("values");
    $("#zoomRange").val(timeFormat($("#slider").slider("values", 0)) + " - " + timeFormat($("#slider").slider("values", 1)));
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
	$("#details").text(JSON.stringify(data.details));
	$("#itemhost").text(data.metric + " @ " + data.host);
  $("#params").val(JSON.stringify(data.params));
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

    //Clear the previous threshold line and render the given threshold
    chart.interactiveLayer.clearThresholdLineAndText();
    chart.interactiveLayer.renderThreshold(chart.yScale()(wholeData.threshold.value));

    //if the period comes from db it should be rendered
    if (!$.isEmptyObject(wholeData) && wholeData.params.stop_lower != undefined && wholeData.params.stop_upper != undefined) {
      chart.interactiveLayer.renderPosition(wholeData.params.stop_lower)
      chart.interactiveLayer.renderPosition(wholeData.params.stop_upper)
      period.push(wholeData.params.stop_lower);
      period.push(wholeData.params.stop_upper);
      appendStartAndEnd(period)
    }
    
    //Update the chart when window resizes.
    nv.utils.windowResize(function() { chart.update() });

    //draw a line when chart is clicked
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
                                                                        document.getElementById('from').innerHTML = '';
                                                                      });



    document.getElementById('threshold').value = wholeData.threshold.value;
    $("#threshold_lower").prop('checked', wholeData.threshold.lower);
    return chart;
  });
}

function appendStartAndEnd(period) {
  $('#period').text("From: " + timeFormat(period[0]) + " To: " + timeFormat(period[1]));
}




function drawZoomedChart(data) {
  console.log(chartData)
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
  //the draw above needs this time to draw before chartData is returned back to its original state
  setTimeout(function() { chartData = tempChartData }, 1000)
}

function drawAndSetData(data) {
  period = [];
  wholeData = data;
  setData(data);
  initSlider(data)
  draw();
}

function setPeriodParams() {
  if(period.length == 2) {
    wholeData.params.stop_lower = period[0];
    wholeData.params.stop_upper = period[1];
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
