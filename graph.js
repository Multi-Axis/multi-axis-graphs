var allText = $.getJSON("http://85.23.130.197:8080/data_sample.txt", function(data) {
	draw(data);
});

function draw(chartData) {
    var width = 800;
    var lineFunc = d3.svg.line()
                        .x(function(d, i) {return i * (width / chartData.length) + 30 })
                        .y(function(d) {return width - ((d.val/10000000)*8 - 30)})
                        .interpolate("none");

    var svgContainer = d3.select("body").append("svg:svg").attr("width", width).attr("height", 810).append("svg:g");

    
    svgContainer.append("svg:path").attr("d",lineFunc(chartData)).attr("stroke","blue").attr("fill","none");

    var axisScaleX = d3.scale.linear().domain([0,10]).range([20,780]);
    var axisScaleY = d3.scale.linear().domain([100,0]).range([20,780])

    var chartXAxis = d3.svg.axis()
                          .scale(axisScaleX)
                          .orient("bottom")
                          .ticks(5);

    var chartYAxis = d3.svg.axis()
                          .scale(axisScaleY)
                          .orient("left")
                          .ticks(5);

    svgContainer.append("g")
                .attr("class", "axis")
                .attr("transform", "translate(30,0)")
                .call(chartYAxis);
    svgContainer.append("g")
                .attr("class", "axis")
                .attr("transform", "translate(0," + (width-30) + ")")
                .call(chartXAxis);
}
