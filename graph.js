var allText = $.getJSON("http://localhost:8080/data_sample.txt", function(data) {
	draw(data);
});

function draw(chartData) {
    var width = 900;
    var height = 700;
    var margin = {"left": 50, "right":20, "top": 30, "bottom":30};    

    var xMin = d3.min(chartData, function(d){return Math.min(d.time)});
    var xMax = d3.max(chartData, function(d){return Math.max(d.time)});

    var yMin = d3.min(chartData, function(d){return Math.min(d.val)});
    var yMax = d3.max(chartData, function(d){return Math.max(d.val)});

    // console.log(" yMin: " + yMin + " ymax: " + yMax);

    var xScale = d3.time.scale().domain([xMin, xMax]).range([margin.left, width - margin.right]);

    var xAxisScale = d3.time.scale().domain([xMin,xMax]).range([margin.left,width]);
    var yScale = d3.scale.linear().domain([yMax,1]).range([margin.bottom,height+margin.top])

    var valueFormat = (function(d) {return yScale(d.val)});

    var lineFunc = d3.svg.line()
                        .x(function(d, i) {return xScale(d.time)})
                        .y(valueFormat)
                        .interpolate("none");

    var svgContainer = d3.select("body").append("svg:svg").attr("width", width).attr("height", height).append("svg:g");

    
    svgContainer.append("svg:path").attr("d",lineFunc(chartData)).attr("stroke","blue").attr("fill","none");


    var chartXAxis = d3.svg.axis()
                          .scale(xAxisScale)
                          .orient("bottom")
                          .ticks(10)
                          .tickFormat(function(d) { return d3.time.format('%b %d, %H:%M')(new Date(d)); });


    var chartYAxis = d3.svg.axis()
                          .scale(yScale)
                          .orient("left")
                          .ticks(7)
                          .tickFormat(function(d) {return (d/1000000) + " MB"});

    svgContainer.append("g")
                .attr("class", "axis")
                .attr("transform", "translate("+ margin.left +",0)")
                .call(chartYAxis);
    svgContainer.append("g")
                .attr("class", "axis")
                .attr("transform", "translate(0," + (height-margin.bottom) + ")")
                .call(chartXAxis);
}
