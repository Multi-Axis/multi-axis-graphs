var width = 1200;
var height = 700;
var svgContainer = d3.select("div").append("svg:svg").attr("width", width).attr("height", height);
var allData;

$.getJSON(window.location.href, function(data) {
	draw(data.history, data.future, svgContainer);
    allData = data;
    // console.log(JSON.stringify(allData))
})

function draw(leftGrapHistoryhData, leftGraphFutureData, svgContainer) {
    
    var margin = {"left": 50, "right":20, "top": 30, "bottom":30};    
    var today = (width * (2/3) ) - margin.right;

    var historyXMin = d3.min(leftGrapHistoryhData, function(d){return Math.min(d.time)});
    var historyXMax = d3.max(leftGrapHistoryhData, function(d){return Math.max(d.time)});
    var historyYMin = d3.min(leftGrapHistoryhData, function(d){return Math.min(d.val)});
    var historyYMax = d3.max(leftGrapHistoryhData, function(d){return Math.max(d.val)});
    var historyXScale = d3.time.scale().domain([historyXMin, historyXMax]).range([margin.left, today]);

    var futureYMin = d3.min(leftGraphFutureData, function(d){return Math.min(d.val)});
    var futureYMax = d3.max(leftGraphFutureData, function(d){return Math.max(d.val)});
    var futureXMin = d3.min(leftGraphFutureData, function(d){return Math.min(d.time)});
    var futureXMax = d3.max(leftGraphFutureData, function(d){return Math.max(d.time)});
    var futureXScale = d3.time.scale().domain([futureXMin, futureXMax]).range([today, width - margin.right]);

    var yMin = Math.min(historyYMin, futureYMin);
    var yMax = (Math.max(historyYMax, futureYMax));
    var yScale = d3.scale.linear().domain([(1.5*yMax),(yMin-30)]).range([0,height+margin.top])

    var historyGraphFunc = d3.svg.line()
                                .x(function(d, i) {return historyXScale(d.time)})
                                .y(function(d) {return yScale(d.val) })
                                .interpolate("none");

    var futureGraphFunc = d3.svg.line()
                                .x(function(d, i) {return futureXScale(d.time)})
                                .y(function(d) {return yScale(d.val) })
                                .interpolate("none");

    
    svgContainer.append("svg:path").attr("d", historyGraphFunc(leftGrapHistoryhData)).attr("stroke","blue").attr("fill","none");
    svgContainer.append("svg:path").attr("d", futureGraphFunc(leftGraphFutureData)).attr("stroke","red").attr("fill","none")

    var chartXAxis = d3.svg.axis()
                          .scale(historyXScale)
                          .orient("bottom")
                          .ticks(3)
                          .tickFormat(function(d) { return d3.time.format('%Y %b %d, %H : %M')(new Date(d)); });

    var futureXAxis = d3.svg.axis()
                          .scale(futureXScale)
                          .orient("bottom")
                          .ticks(3)
                          .tickFormat(function(d) { return d3.time.format('%b %d, %H:%M')(new Date(d)); });

    var chartYAxis = d3.svg.axis()
                          .scale(yScale)
                          .orient("left")
                          .ticks(6)
                          .tickFormat(function(d) {return (d)});

    svgContainer.append("g")
                .attr("class", "axis")
                .attr("transform", "translate("+ margin.left +",0)")
                .call(chartYAxis);
    svgContainer.append("g")
                .attr("class", "axis")
                .attr("transform", "translate(0," + (height-margin.bottom) + ")")
                .call(chartXAxis);
    svgContainer.append("g")
                .attr("class", "futureAxis")
                .attr("transform", "translate(" + 0 + "," + (height-margin.bottom) + ")")
                .call(futureXAxis);
}
