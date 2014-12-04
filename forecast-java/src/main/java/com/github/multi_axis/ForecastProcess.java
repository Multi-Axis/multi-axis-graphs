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
import com.github.multi_axis.Formats;

import static com.github.multi_axis.JsonUtils.readJsonObject;
import static com.github.multi_axis.JsonUtils.writeJsonObject;

/**
 * A ForecastProcess contains a function for turning an input JsonObject
 * into a result JsonObject. These are constructed from components given
 * to the forecastProcess method. */

public final class ForecastProcess {

  /** A function that turns an input JsonObject into a result JsonObject */
  public final F<JsonObject,JsonObject> run;

  /** Builds a ForecastProcess from the given components.
   *
   *  @param <META>     The type of metadata to be read from the input by the
   *                    reader. The resulting metadata object will be used for
   *                    selecting filters to be used on the data before
   *                    processing as well as for any formatting parameters to be
   *                    used by the writer.
   *
   *  @param <DATA>     The type of the data to be used as input for calculating
   *                    a foreacast.
   *
   *  @param <RESULT>   The type of the result of the forecast, which will be
   *                    passed to the writer together with the metadata.
   *
   *  @param reader     A reader object used for producing a data and a
   *                    metadata object from a JsonObject.
   *
   *  @param getFilters A function used for producing a list of data filters
   *                    based on the metadata.
   *
   *  @param model      A function for producing a forecast result from the
   *                    data.
   *
   *  @param writeError The function used for producing a resulting JsonObject
   *                    in case of an error anywhere in the process.
   *
   *  @param writer     A writer object used for producing a JsonObject from
   *                    the forecast result and the metadata. */

  public static <META,DATA,RESULT> ForecastProcess
    forecastProcess(
      final Formats.Reader<META,DATA>                     reader,
      final F<META,Validation<Errors,List<F<DATA,DATA>>>> getFilters,
      final F<DATA,RESULT>                                model,
      final F<Errors,JsonObject>                          writeError,
      final Formats.Writer<META,RESULT>                   writer) {

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
