package com.github.multi_axis;

import java.io.IOException;

import fj.Unit;
import fj.data.IO;

import com.github.multi_axis.Conf;

import static com.github.multi_axis.ForecastProcess.forecastAlts2;
import static com.github.multi_axis.ForecastFunctions.weekOfDailyMaximumsLeastSquares;

public class DailyMaxLeastSquaresApp {

  private static IO<Unit> 
    app() { 
      return
        forecastAlts2(System.in,
                      System.out,
                      Conf.Reader.zabReader,
                      Conf.Writer.zab0Writer,
                      Conf.Writer.zab3Writer,
                      weekOfDailyMaximumsLeastSquares,
                      weekOfDailyMaximumsLeastSquares); }

  public static void main(String[] args) throws IOException {
    app().run(); }

}
