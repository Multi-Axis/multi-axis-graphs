package com.github.multi_axis;

import java.io.InputStream;
import java.io.OutputStream;

import fj.F;
import fj.Unit;
import fj.data.IO;
import fj.data.IOFunctions;
import fj.data.Validation;

import com.github.multi_axis.Errors;
import com.github.multi_axis.Conf;
import com.github.multi_axis.Alts2;
import com.github.multi_axis.Tagged;

import static com.github.multi_axis.Tagged.tag;
import static com.github.multi_axis.Alts2.map2;
import static com.github.multi_axis.Conf.Writer.errorWriter;
import static com.github.multi_axis.Conf.Writer.writerAlts2;



//TODO THINK different modifiers?
public final class ForecastProcess {

  
  public static <FT,IN,OUT> IO<Unit> 
    forecast( InputStream                 inStream,
              OutputStream                outStream,
              Conf.Reader<Tagged<FT,IN>>  reader,
              Conf.Writer<Tagged<FT,OUT>> writer,
              F<IN,OUT>                   func) {
      
      return  IOFunctions.bind(reader.read(inStream),(validation  -> 
                errorWriter(writer)
                  .write(validation.map(x  -> tag(x.tag, func.f(x.val))),
                         outStream)));
  }

  public static <FA,FB,INA,INB,OUTA,OUTB> IO<Unit>
    forecastAlts2(InputStream                         inStream,
                  OutputStream                        outStream,
                  Conf.Reader<Alts2<Tagged<FA,INA>,
                                    Tagged<FB,INB>>>  reader,
                  Conf.Writer<Tagged<FA,OUTA>>        writerA,
                  Conf.Writer<Tagged<FB,OUTB>>        writerB,
                  F<INA,OUTA>                         funcA,
                  F<INB,OUTB>                         funcB) {

      return IOFunctions.bind(reader.read(inStream),(validation  ->
          errorWriter(writerAlts2(writerA,writerB))
            .write(
              validation.map(map2(
                (x  -> tag(x.tag, funcA.f(x.val))),
                (x  -> tag(x.tag, funcB.f(x.val))))),
              outStream)));
  }
   

  private ForecastProcess() {}

}
