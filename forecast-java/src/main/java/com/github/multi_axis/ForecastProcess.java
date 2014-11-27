package com.github.multi_axis;

import java.io.InputStream;
import java.io.OutputStream;

import fj.F;
import fj.Unit;
import fj.data.List;
import fj.data.IO;
import fj.data.IOFunctions;
import fj.data.Validation;

import javax.json.JsonObject;

import com.github.multi_axis.Errors;
import com.github.multi_axis.Conf;

import static com.github.multi_axis.JsonUtils.readJsonObject;
import static com.github.multi_axis.JsonUtils.writeJsonObject;


public final class ForecastProcess {

  public final F<JsonObject,JsonObject> run;

  public static <META,DATA,RESULT> ForecastProcess
    forecastProcess(
      final Conf.Reader<META,DATA>                        reader,
      final F<META,Validation<Errors,List<F<DATA,DATA>>>> getFilters,
      final F<DATA,RESULT>                                model,
      final F<Errors,JsonObject>                          writeError,
      final Conf.Writer<META,RESULT>                      writer) {

      return new ForecastProcess( json  ->
        reader.read(json).bind( datas  ->
        getFilters.f(datas.meta)
          .map(filters  -> filters.foldLeft((data,fun)  -> fun.f(data),
                                            datas.data))
          .map(model)
          .map(result  -> writer.write(datas.meta,result)))
        .validation(writeError,
                    x  -> x)); }


  private ForecastProcess(F<JsonObject,JsonObject> run) {
    this.run = run; }
}
