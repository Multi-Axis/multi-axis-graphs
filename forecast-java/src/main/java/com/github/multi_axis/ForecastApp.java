package com.github.multi_axis;

import java.io.IOException;

import fj.Unit;
import fj.data.IO;

import static fj.data.IOFunctions.stdoutPrintln;

import static com.github.multi_axis.Utils.first;
import static com.github.multi_axis.Formats.Reader.zabReader;
import static com.github.multi_axis.Formats.Writer.zabWriter;
import static com.github.multi_axis.ForecastFunctions.timedValsLeastSquares;
import static com.github.multi_axis.Filters.filtersFromZab;
import static com.github.multi_axis.ForecastProcesses.forecastProcesses;

/** An application that takes a label for a forecast process as a command
 *  line argument and runs that process from STDIN to STDOUT.
 *  At the moment only handles the "leastSquares" process,
 *  but can be extended by adding more .with(...) calls to the fcasts field. */

public class ForecastApp {

  private static final ForecastProcesses
    fcasts =
      forecastProcesses(ErrorWriterImpl.write)
        .with("leastSquares",
              zabReader,
              filtersFromZab,
              timedValsLeastSquares,
              zabWriter);

  private static IO<Unit> 
    run(final String arg) { 

      return fcasts.run(arg, System.in, System.out); }

  public static void main(String[] args) throws IOException {
    first(args)
      .map(arg  -> run(arg))
      .orSome(stdoutPrintln("No argument given."))
      .run(); }
}
