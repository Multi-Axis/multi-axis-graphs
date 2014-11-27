package com.github.multi_axis;

import java.io.IOException;

import fj.Unit;
import fj.data.IO;

import com.github.multi_axis.DummyErrorWriterImpl;

import static com.github.multi_axis.Utils.first;
import static com.github.multi_axis.Conf.Reader.zabReader;
import static com.github.multi_axis.Conf.Writer.zabWriter;
import static com.github.multi_axis.ForecastFunctions.timedValsLeastSquares;
import static com.github.multi_axis.ForecastProcesses.forecastProcesses;
import static com.github.multi_axis.Filters.filtersFromZab;

public class LeastSquaresApp {

  private static IO<Unit> 
    run(final String arg) { 

      final ForecastProcesses fcasts =
        forecastProcesses(DummyErrorWriterImpl.write)
          .with("leastSquares",
                zabReader,
                filtersFromZab,
                data  -> timedValsLeastSquares(data),
                zabWriter);

      return fcasts.run(arg, System.in, System.out); }

  public static void main(String[] args) throws IOException {
    run("leastSquares").run(); }

}
