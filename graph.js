var allText = $.getJSON("http://localhost:8080/data_sample.txt", function(data) {
	draw(data);
});

function draw(chartData) {
    var forecast = [{"time":1440693352,"val":834920448},{"time":1440694252,"val":834973696},{"time":1440695152,"val":834985984},{"time":1440696052,"val":835014656},{"time":1440696952,"val":834887680},{"time":1440697852,"val":834953216},{"time":1440698752,"val":834936832},{"time":1440699652,"val":834912256},{"time":1440700492,"val":834985984},{"time":1440701392,"val":834990080},{"time":1440702292,"val":834940928},{"time":1440703192,"val":835006464},{"time":1440704092,"val":834957312},{"time":1440704992,"val":834863104},{"time":1440705892,"val":834912256},{"time":1440706792,"val":834875392},{"time":1440707692,"val":834965504},{"time":1440708592,"val":834879488},{"time":1440709492,"val":834871296},{"time":1440710392,"val":834834432},{"time":1440711292,"val":834805760},{"time":1440712192,"val":829677568},{"time":1440713092,"val":834838528},{"time":1440713992,"val":834871296},{"time":1440714892,"val":834789376},{"time":1440715792,"val":834854912},{"time":1440716692,"val":834838528},{"time":1440717592,"val":834781184},{"time":1440718492,"val":834670592},{"time":1440719392,"val":834748416},{"time":1440720292,"val":832782336},{"time":1440721192,"val":833286144},{"time":1440722092,"val":834113536},{"time":1440722992,"val":833196032},{"time":1440723892,"val":832626688},{"time":1440724792,"val":834068480},{"time":1440725692,"val":834056192},{"time":1440726592,"val":833961984},{"time":1440727492,"val":834076672},{"time":1440728392,"val":834088960},{"time":1440729292,"val":834113536},{"time":1440730192,"val":832151552},{"time":1440731092,"val":832245760},{"time":1440731992,"val":832249856},{"time":1440732892,"val":832200704},{"time":1440733792,"val":828522496},{"time":1440734692,"val":833875968},{"time":1440735592,"val":833818624},{"time":1440736492,"val":833736704},{"time":1440737392,"val":833748992},{"time":1440738292,"val":833777664},{"time":1440739192,"val":833732608},{"time":1440740092,"val":833708032},{"time":1440740992,"val":833703936},{"time":1440741892,"val":833703936},{"time":1440742792,"val":833617920},{"time":1440743692,"val":833646592},{"time":1440744592,"val":833736704},{"time":1440745492,"val":833691648},{"time":1440746392,"val":833585152},{"time":1440747292,"val":833687552},{"time":1440748192,"val":833638400},{"time":1440749032,"val":831803392},{"time":1440749932,"val":833400832},{"time":1440750832,"val":833376256},{"time":1440751732,"val":833380352},{"time":1440752632,"val":833302528},{"time":1440753532,"val":833388544},{"time":1440754432,"val":833310720},{"time":1440755332,"val":833302528},{"time":1440756232,"val":833347584},{"time":1440757132,"val":833372160}];
    // forecast = JSON.stringify(forecast);
    // chartData = chartData.concat(forecast);
    console.log(JSON.stringify(forecast));
    console.log(JSON.stringify(chartData));
    var width = 900;
    var height = 700;
    var margin = {"left": 50, "right":20, "top": 30, "bottom":30};    

    var xMin = d3.min(chartData, function(d){return Math.min(d.time)});
    var xMax = d3.max(chartData, function(d){return Math.max(d.time)});

    var yMin = d3.min(chartData, function(d){return Math.min(d.val)});
    var yMax = d3.max(chartData, function(d){return Math.max(d.val)});

    var foreYMin = d3.min(forecast, function(d){return Math.min(d.val)});
    var foreYMax = d3.max(forecast, function(d){return Math.max(d.val)});

    var forexMin = d3.min(forecast, function(d){return Math.min(d.time)});
    var forexMax = d3.max(forecast, function(d){return Math.max(d.time)});
    // xMin = (Math.min(xMin,forexMin));
    // xMax = (Math.max(xMax,forexMax));
    yMin = (Math.min(yMin,foreYMin));
    yMax = (Math.max(yMax,foreYMax));
    // console.log(" yMin: " + yMin + " ymax: " + yMax);

    var today = (width * (2/3) ) - margin.right;

    var historyXScale = d3.time.scale().domain([xMin, xMax]).range([margin.left, today]);
    var forecastXScale = d3.time.scale().domain([forexMin, forexMax]).range([today, width - margin.right]);

    var xAxisScale = d3.time.scale().domain([xMin,xMax]).range([margin.left,width]);
    var yScale = d3.scale.linear().domain([yMax,1]).range([margin.bottom,height+margin.top])
    var foreYScale = d3.scale.linear().domain([foreYMax,1]).range([margin.bottom,height+margin.top])

    var valueFormat = (function(d) {return yScale(d.val)});

    var historyLineFunc = d3.svg.line()
                        .x(function(d, i) {return historyXScale(d.time)})
                        .y(function(d) {return yScale(d.val)})
                        .interpolate("none");

    var forecastLineFunc = d3.svg.line()
                        .x(function(d, i) {return forecastXScale(d.time)})
                        .y(function(d) {return yScale(d.val)})
                        .interpolate("none");

    var svgContainer = d3.select("body").append("svg:svg").attr("width", width).attr("height", height).append("svg:g");

    
    svgContainer.append("svg:path").attr("d", historyLineFunc(chartData)).attr("stroke","blue").attr("fill","none");
    svgContainer.append("svg:path").attr("d", forecastLineFunc(forecast)).attr("stroke","red").attr("fill","none")//.style("stroke-dasharray", ("3,3"));

    var chartXAxis = d3.svg.axis()
                          .scale(historyXScale)
                          .orient("bottom")
                          .ticks(4)
                          .tickFormat(function(d) { return d3.time.format('%b %d, %H:%M')(new Date(d)); });

    var forecastXAxis = d3.svg.axis()
                          .scale(forecastXScale)
                          .orient("bottom")
                          .ticks(3)
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

    svgContainer.append("g")
                .attr("class", "futureAxis")
                .attr("transform", "translate(" + 0 + "," + (height-margin.bottom) + ")")
                .call(forecastXAxis);
}
