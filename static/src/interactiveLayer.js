/* Utility class to handle creation of an interactive layer.
This places a rectangle on top of the chart. When you mouse move over it, it sends a dispatch
containing the X-coordinate. It can also render a vertical line where the mouse is located.

dispatch.elementMousemove is the important event to latch onto.  It is fired whenever the mouse moves over
the rectangle. The dispatch is given one object which contains the mouseX/Y location.
It also has 'pointXValue', which is the conversion of mouseX to the x-axis scale.
*/
nv.interactiveGuideline = function() {
	"use strict";
	var tooltip = nv.models.tooltip();
	//Public settings
  var zoomStartX;
  var mousedown;
	var width = null
	, height = null
    //Please pass in the bounding chart's top and left margins
    //This is important for calculating the correct mouseX/Y positions.
	, margin = {left: 0, top: 0}
	, xScale = d3.scale.linear()
	, yScale = d3.scale.linear()
	, dispatch = d3.dispatch('elementMousemove', 'elementMouseout','elementDblclick', 'elementClick', 'elementMousedown', 'elementMouseup')
	, showGuideLine = true
	, svgContainer = null  

    //Must pass in the bounding chart's <svg> container.
    //The mousemove event is attached to this container.
	;

	//Private variables
	var isMSIE = navigator.userAgent.indexOf("MSIE") !== -1  //Check user-agent for Microsoft Internet Explorer.
	;


	function layer(selection) {
		selection.each(function(data) {
				var container = d3.select(this);
				
        
				var availableWidth = (width || 960), availableHeight = (height || 400);

				var wrap = container.selectAll("g.nv-wrap.nv-interactiveLineLayer").data([data]);
				var wrapEnter = wrap.enter().append("g").attr("class", " nv-wrap nv-interactiveLineLayer");
        wrapEnter.append("g").attr("class", 'nv-text');
				wrapEnter.append("g").attr("class", "nv-line");				
				wrapEnter.append("g").attr("class", "nv-guideline")
        wrapEnter.append("g").attr("class", "nv-zoomCurtain")
				wrapEnter.append("g").attr("class","nv-interactiveGuideLine");
				
				if (!svgContainer) {
					return;
				}

                function mouseHandler() {
                      var d3mouse = d3.mouse(this);
                      var mouseX = d3mouse[0];
                      var mouseY = d3mouse[1];
                      var subtractMargin = true;
                      var mouseOutAnyReason = false;
                      if (isMSIE) {
                         /*
                            D3.js (or maybe SVG.getScreenCTM) has a nasty bug in Internet Explorer 10.
                            d3.mouse() returns incorrect X,Y mouse coordinates when mouse moving
                            over a rect in IE 10.
                            However, d3.event.offsetX/Y also returns the mouse coordinates
                            relative to the triggering <rect>. So we use offsetX/Y on IE.  
                         */
                         mouseX = d3.event.offsetX;
                         mouseY = d3.event.offsetY;

                         /*
                            On IE, if you attach a mouse event listener to the <svg> container,
                            it will actually trigger it for all the child elements (like <path>, <circle>, etc).
                            When this happens on IE, the offsetX/Y is set to where ever the child element
                            is located.
                            As a result, we do NOT need to subtract margins to figure out the mouse X/Y
                            position under this scenario. Removing the line below *will* cause 
                            the interactive layer to not work right on IE.
                         */
                         if(d3.event.target.tagName !== "svg")
                            subtractMargin = false;

                         if (d3.event.target.className.baseVal.match("nv-legend"))
                         	mouseOutAnyReason = true;
                          
                      }

                      if(subtractMargin) {
                         mouseX -= margin.left;
                         mouseY -= margin.top;
                      }

                      /* If mouseX/Y is outside of the chart's bounds,
                      trigger a mouseOut event.
                      */
                      if (mouseX < 0 || mouseY < 0 
                        || mouseX > availableWidth || mouseY > availableHeight
                        || (d3.event.relatedTarget && d3.event.relatedTarget.ownerSVGElement === undefined)
                        || mouseOutAnyReason
                        ) 
                      {
                      		if (isMSIE) {
                      			if (d3.event.relatedTarget 
                      				&& d3.event.relatedTarget.ownerSVGElement === undefined
                      				&& d3.event.relatedTarget.className.match(tooltip.nvPointerEventsClass)) {
                      				return;
                      			}
                      		}
                            dispatch.elementMouseout({
                               mouseX: mouseX,
                               mouseY: mouseY
                            });
                            layer.renderGuideLine(null); //hide the guideline
                            return;
                      }
                      
                      var pointXValue = xScale.invert(mouseX);
                      dispatch.elementMousemove({
                            mouseX: mouseX,
                            mouseY: mouseY,
                            pointXValue: pointXValue
                      });
                      
                      if (d3.event.type == 'mousedown') {
                        mousedown = pointXValue;
                        zoomStartX = mouseX;
                        dispatch.elementMousedown({
                          mouseX: mouseX,
                          mouseY: mouseY,
                          pointXValue: pointXValue
                        })
                      }

                      if (d3.event.type == 'mouseup') {
                        zoomStartX = false;
                        dispatch.elementMouseup({
                          mouseX: mouseX,
                          mouseY: mouseY,
                          xValue: pointXValue,
                          x2Value: mousedown
                        })
                      }

                      //If user double clicks the layer, fire a elementDblclick dispatch.
                      if (d3.event.type === "dblclick") {
                        dispatch.elementDblclick({
                            mouseX: mouseX,
                            mouseY: mouseY,
                            pointXValue: pointXValue
                        });                     
                      }
                      if (d3.event.type === 'click') {
                        dispatch.elementClick({
                            pointXValue: pointXValue,
                            pointXLocation: mouseX
                        });
                      }
                }

				svgContainer
				      .on("mousemove",mouseHandler, true)
				      .on("mouseout" ,mouseHandler,true)
              .on("dblclick" ,mouseHandler)
              .on("click", mouseHandler)
              .on("mousedown", mouseHandler)
              .on("mouseup", mouseHandler)
				      ;

				 //Draws a vertical guideline at the given X postion.
				layer.renderGuideLine = function(x) {
				 	if (!showGuideLine) return;
				 	var line = wrap.select(".nv-interactiveGuideLine")
				 	      .selectAll("line")
				 	      .data((x != null) ? [nv.utils.NaNtoZero(x)] : [], String);

				 	line.enter()
				 		.append("line")
				 		.attr("class", "nv-guideline")
				 		.attr("x1", function(d) { return d;})
				 		.attr("x2", function(d) { return d;})
				 		.attr("y1", availableHeight)
				 		.attr("y2",0)
				 		;
				 	line.exit().remove();

				}

        layer.renderZoomCurtain = function(x) {
          if (!zoomStartX) {
            clearZoomCurtain();
            return;
          }
          clearZoomCurtain();
          var curtain = wrap.select(".nv-zoomCurtain").
          selectAll("rect")
          .data((x != null) ? [nv.utils.NaNtoZero(x)] : [], String);
          
          var xPos;
          if (x > zoomStartX) {
            xPos = zoomStartX;
          } else {
            xPos = x;
          }
          curtain.enter()
                .append("rect")
                .attr("class", "nv-zoomCurtain")
                .attr("x", xPos)
                .attr("y", 0)
                .attr("width", Math.abs(zoomStartX - x))
                .attr("height", availableHeight)
                .attr("opacity", 0.1)   
        }

        function clearZoomCurtain() {
          var curtains = wrap.select(".nv-zoomCurtain").selectAll("rect");
          curtains.remove();
        }

        layer.clearPeriodLines = function() {
          var line = wrap.select(".nv-guideline").selectAll("line");
          line.remove();
        }

        layer.clearThresholdLineAndText = function() {
          var line = wrap.select(".nv-line").selectAll("line");
          var text = wrap.select(".nv-text").selectAll("text");
          text.remove();
          line.remove();
        }

        layer.renderPosition = function(x) {
          var line = wrap.select(".nv-guideline")
                .selectAll("line")
                .data((x != null) ? [nv.utils.NaNtoZero(x)] : [], String);

          line.enter()
            .append("line")
            .attr("stroke", "black")
            .attr("x1", xScale(x))
            .attr("x2", xScale(x))
            .attr("y1", availableHeight)
            .attr("y2",0);    
        }

        layer.renderThreshold = function(x) {
          if (isNaN(x)) return;
          
          var line = wrap.select(".nv-line")
                .selectAll("line")
                .data((x != null) ? [nv.utils.NaNtoZero(x)] : [], String);

          var text = wrap.select(".nv-text").selectAll("text").data("Threshold")
          text.enter()
              .append("text")
              .text("Threshold")
              .style("text-anchor", "middle")  
              .attr("y", x-4)
              .attr("x", availableWidth/2);

          line.enter()
            .append("line")
            .attr("stroke", "red")
            .style("stroke-dasharray", ("3, 3"))
            .attr("x1", 0)
            .attr("x2", availableWidth)
            .attr("y1", x)
            .attr("y2", x)
        }
		});
	}

	layer.dispatch = dispatch;
	layer.tooltip = tooltip;

	layer.margin = function(_) {
	    if (!arguments.length) return margin;
	    margin.top    = typeof _.top    != 'undefined' ? _.top    : margin.top;
	    margin.left   = typeof _.left   != 'undefined' ? _.left   : margin.left;
	    return layer;
    };

	layer.width = function(_) {
		if (!arguments.length) return width;
		width = _;
		return layer;
	};

	layer.height = function(_) {
		if (!arguments.length) return height;
		height = _;
		return layer;
	};

	layer.xScale = function(_) {
		if (!arguments.length) return xScale;
		xScale = _;
		return layer;
	};

  layer.yScale = function(_) {
    if (!arguments.length) return yScale;
    yScale = _;
    return layer;
  };

	layer.showGuideLine = function(_) {
		if (!arguments.length) return showGuideLine;
		showGuideLine = _;
		return layer;
	};

	layer.svgContainer = function(_) {
		if (!arguments.length) return svgContainer;
		svgContainer = _;
		return layer;
	};


	return layer;
};

/* Utility class that uses d3.bisect to find the index in a given array, where a search value can be inserted.
This is different from normal bisectLeft; this function finds the nearest index to insert the search value.

For instance, lets say your array is [1,2,3,5,10,30], and you search for 28. 
Normal d3.bisectLeft will return 4, because 28 is inserted after the number 10.  But interactiveBisect will return 5
because 28 is closer to 30 than 10.

Unit tests can be found in: interactiveBisectTest.html

Has the following known issues:
   * Will not work if the data points move backwards (ie, 10,9,8,7, etc) or if the data points are in random order.
   * Won't work if there are duplicate x coordinate values.
*/
nv.interactiveBisect = function (values, searchVal, xAccessor) {
	  "use strict";
      if (! values instanceof Array) return null;
      if (typeof xAccessor !== 'function') xAccessor = function(d,i) { return d.x;}

      var bisect = d3.bisector(xAccessor).left;
      var index = d3.max([0, bisect(values,searchVal) - 1]);
      var currentValue = xAccessor(values[index], index);
      if (typeof currentValue === 'undefined') currentValue = index;

      if (currentValue === searchVal) return index;  //found exact match

      var nextIndex = d3.min([index+1, values.length - 1]);
      var nextValue = xAccessor(values[nextIndex], nextIndex);
      if (typeof nextValue === 'undefined') nextValue = nextIndex;

      if (Math.abs(nextValue - searchVal) >= Math.abs(currentValue - searchVal))
          return index;
      else
          return nextIndex
};

/*
Returns the index in the array "values" that is closest to searchVal.
Only returns an index if searchVal is within some "threshold".
Otherwise, returns null.
*/
nv.nearestValueIndex = function (values, searchVal, threshold) {
      "use strict";
      var yDistMax = Infinity, indexToHighlight = null;
      values.forEach(function(d,i) {
         var delta = Math.abs(searchVal - d);
         if ( delta <= yDistMax && delta < threshold) {
            yDistMax = delta;
            indexToHighlight = i;
         }
      });
      return indexToHighlight;
};