var width = 1200;
var height = 700;
var margin = {"left": 50, "right":40, "top": 30, "bottom":30};
var svgContainer = d3.select("div").append("svg:svg").attr("width", width).attr("height", height);
var rightData;
var xScale;

$.getJSON(window.location.href, function(data) {
	draw(data.history, data.future, svgContainer);
    rightData = data;
    // console.log(JSON.stringify(allData))
})

var timeFormat = function(d) {
  var date = new Date(0);
  date.setUTCSeconds(d);
  return d3.time.format('%Y %b %d, %H : %M')(date);
};

function draw(leftGrapHistoryhData, leftGraphFutureData, svgContainer) {

    // x scale
    xScale = d3.time.scale()
      .domain( d3.extent(leftGrapHistoryhData.concat(leftGraphFutureData),
            function(d) { return d.time; }))
      .range([margin.left, width - margin.right]);

    // y scale
    var historyYMin = d3.min(leftGrapHistoryhData, function(d){ return d.val });
    var historyYMax = d3.max(leftGrapHistoryhData, function(d){ return d.val });
    var futureYMin  = d3.min(leftGraphFutureData, function(d){ return d.val });
    var futureYMax  = d3.max(leftGraphFutureData, function(d){ return d.val });

    var yMin = Math.min(historyYMin, futureYMin);
    var yMax = Math.max(historyYMax, futureYMax);

    var yScale = d3.scale.linear()
      .domain([1.1 * yMax, yMin - 1])
      .range([margin.bottom, height - margin.top]);

    var historyGraphFunc = d3.svg.line()
                                .x(function(d, i) { return xScale(d.time); })
                                .y(function(d) { return yScale(d.val); })
                                .interpolate("none");

    var futureGraphFunc = d3.svg.line()
                                .x(function(d, i) { return xScale(d.time); })
                                .y(function(d) { return yScale(d.val); })
                                .interpolate("none");

    svgContainer
      .append("svg:path")
      .attr("d", historyGraphFunc(leftGrapHistoryhData)).attr("stroke","blue").attr("fill","none");

    svgContainer
      .append("svg:path")
      .attr("d", futureGraphFunc(leftGraphFutureData)).attr("stroke","red").attr("fill","none");

    var xAxis = d3.svg.axis()
      .scale(xScale)
      .orient("bottom")
      .ticks(5)
      .tickFormat(timeFormat);

    var yAxis = d3.svg.axis()
      .scale(yScale)
      .orient("left")
      .ticks(6)
      .tickFormat(function(d) { return d });

    svgContainer.append("g")
                .attr("class", "axisleft")
                .attr("transform", "translate("+ margin.left +",0)")
                .call(yAxis);

    svgContainer.append("g")
                .attr("class", "axis")
                .attr("transform", "translate(0," + (height-margin.bottom) + ")")
                .call(xAxis);
}

function appendGraph(data, svg) {

    var rightYMin = d3.min(data, function(d){return Math.min(d.val)});
    var rightYMax = d3.max(data, function(d){return Math.max(d.val)});

    var leftYMin = d3.min(rightData.history, function(d){return Math.min(d.val)});
    var leftYMax = d3.max(rightData.history, function(d){return Math.max(d.val)});

    var totMin = Math.min(rightYMin,leftYMin);
    var totMax = Math.max(rightYMax,leftYMax);

    var yScale = d3.scale.linear().domain([(1.5*totMax),(totMin-30)]).range([0,height+margin.top])

    var rightGraphFunc = d3.svg.line()
                            .x(function(d, i) {return xScale(d.time)})
                            .y(function(d) {return yScale(d.val) })
                            .interpolate("none");

    svgContainer.append("path")
      .attr("d", rightGraphFunc(data))
      .attr("stroke","black")
      .attr("fill","none");

    var rightYAxis =  d3.svg.axis().scale(yScale)
                     .orient("right")
                     .ticks(6)
                     .tickFormat(function(d) {return (d)});

    svgContainer.append("g")
                .attr("class", "axisleft")
                .attr("transform", "translate("+ (width-margin.right) +",0)")
                .call(rightYAxis);
}

// #newGraph button
document.getElementById("newGraph").addEventListener("click", function() {
  $.getJSON("/%3A10105%3Avfs.fs.size%5B%2F%2Cpfree%5D", function(data) {
    appendGraph(data.history, svgContainer)
  });
});
