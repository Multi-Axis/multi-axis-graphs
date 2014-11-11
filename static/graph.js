var chartData;
var period = [];
var chart;
var wholeData;

$(function(){
  $.getJSON(window.location.href, function(data) {
    drawAndSetData(data);
  })
})

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
	$("#details").val(JSON.stringify(data.details));
	$("#itemhost").text(data.metric + " @ " + data.host);
}

var timeFormat = function(d) {
  var date = new Date(0);
  date.setUTCSeconds(d);
  return d3.time.format('%Y %b %d, %H : %M')(date);
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
    chart.yDomain(getYDomain(chartData[0].values, chartData[1].values, wholeData.threshold.value))

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
    return chart;
  });
}

function appendStartAndEnd(period) {
  document.getElementById('from').innerHTML = "From: " + timeFormat(period[0]) + " To: " + timeFormat(period[1]);
}

function postData(params, threshold) {
  $.get(window.location.href, {'params': JSON.stringify(params), 'threshold':threshold}, function(data) {
    drawAndSetData(data)
  }, "json")
}

document.getElementById('sendPeriod').addEventListener('click', function() {
  var threshold = document.getElementById('threshold').value;
  setPeriodParams();
  postData(wholeData.params, threshold);
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
  }
}

function getYDomain(history, future, threshold) {
  if (threshold !== undefined) {
    var domain = [];
    var historyMax = Array.max(history);
    var futureMax = Array.max(future);
    var historyMin = Array.min(history);
    var futureMin =Array.min(future)
    domain.push(Math.min(threshold,Math.min(historyMin,futureMin)) - 1);
    domain.push(Math.max(threshold,Math.max(historyMax,futureMax)) * 1.3);
    return domain;
  } else {
    return undefined;
  }
}
