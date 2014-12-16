package com.github.multi_axis;

import java.io.IOException;

import fj.Unit;
import fj.data.IO;

import static com.github.multi_axis.Utils.first;
import static com.github.multi_axis.Formats.Reader.zabReader;
import static com.github.multi_axis.Formats.Writer.zabWriter;
import static com.github.multi_axis.ForecastFunctions.timedValsLeastSquares;
import static com.github.multi_axis.ForecastProcesses.forecastProcesses;
import static com.github.multi_axis.Filters.filtersFromZab;

/** An application that runs the least squares process from STDIN to STDOUT. */

public class LeastSquaresApp {

  private static IO<Unit> 
    run(final String arg) { 

      final ForecastProcesses fcasts =
        forecastProcesses(ErrorWriterImpl.write)
          .with("leastSquares",
                zabReader,
                filtersFromZab,
                timedValsLeastSquares,
                zabWriter);

      return fcasts.run(arg, System.in, System.out); }

  public static void main(String[] args) throws IOException {
    run("leastSquares").run(); }

}
