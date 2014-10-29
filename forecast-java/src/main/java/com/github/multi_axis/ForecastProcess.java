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
import static com.github.multi_axis.JsonUtils.readJsonObject;
import static com.github.multi_axis.JsonUtils.writeJsonObject;


//TODO THINK different modifiers?
public final class ForecastProcess {


  public static <FT,IN,OUT> IO<Unit> 
    forecast( InputStream                 inStream,
              OutputStream                outStream,
              Conf.Reader<Tagged<FT,IN>>  reader,
              Conf.Writer<Tagged<FT,OUT>> writer,
              F<IN,OUT>                   func) {
      return 
        IOFunctions.bind(
          IOFunctions.map(
            IOFunctions.map(readJsonObject(inStream),(jsonObjV  -> 
              jsonObjV.bind( jsonObject  -> 
              reader.read(jsonObject)))),
            timedValsV  ->
              errorWriter(writer)
                .write(timedValsV.map(x  -> tag(x.tag, func.f(x.val))))),
          jsonObject  ->
            writeJsonObject(outStream,jsonObject)); }


  public static <FA,FB,INA,INB,OUTA,OUTB> IO<Unit>
    forecastAlts2(InputStream                         inStream,
                  OutputStream                        outStream,
                  Conf.Reader<Alts2<Tagged<FA,INA>,
                                    Tagged<FB,INB>>>  reader,
                  Conf.Writer<Tagged<FA,OUTA>>        writerA,
                  Conf.Writer<Tagged<FB,OUTB>>        writerB,
                  F<INA,OUTA>                         funcA,
                  F<INB,OUTB>                         funcB) {
      return 
        IOFunctions.bind(
          IOFunctions.map(
            IOFunctions.map(
              readJsonObject(inStream),
              jsonObjV  ->
                jsonObjV.bind( jsonObject  ->
                reader.read(jsonObject))),
            altsV  ->
              errorWriter(writerAlts2(writerA,writerB))
                .write(altsV.map(map2((x  -> tag(x.tag, funcA.f(x.val))),
                                      (x  -> tag(x.tag, funcB.f(x.val))))))),
          jsonObject  ->
            writeJsonObject(outStream,jsonObject)); }
   

  private ForecastProcess() {}

}
