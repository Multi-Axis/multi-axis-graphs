package com.github.multi_axis;

import java.io.IOException;

import fj.Unit;
import fj.data.IO;

import com.github.multi_axis.Conf;

import static com.github.multi_axis.ForecastProcess.forecastAlts2;

public class DummyApp {

  private static IO<Unit> 
    app = forecastAlts2(System.in,
                        System.out,
                        Conf.Reader.zabReader,
                        Conf.Writer.zab0Writer,
                        Conf.Writer.zab3Writer,
                        (x  -> x),
                        (x  -> x));

  public static void main(String[] args) throws IOException {
    app.run(); }

}
