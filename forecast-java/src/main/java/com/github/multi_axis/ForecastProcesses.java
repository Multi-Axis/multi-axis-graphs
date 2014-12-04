package com.github.multi_axis;

import java.io.InputStream;
import java.io.OutputStream;

import fj.F;
import fj.data.IO;
import fj.data.IOFunctions;
import fj.Unit;
import fj.Ord;
import fj.data.List;
import fj.data.TreeMap;
import fj.data.Validation;

import javax.json.JsonObject;

import static fj.data.Validation.success;
import static fj.data.Validation.fail;

import static com.github.multi_axis.ForecastProcess.forecastProcess;
import static com.github.multi_axis.JsonUtils.readJsonObject;
import static com.github.multi_axis.JsonUtils.writeJsonObject;
import static com.github.multi_axis.Errors.*;

/**
 * A ForecastProcesses contains a mapping from String to ForecastProcess and
 * provides a method for running a given ForecastProcess -- taking its json input
 * from a given InputStream and writing the result JsonObject into the given
 * OutputStream. A ForecastProcesses object uses the same error writer function
 * for all its constituent ForecastProcess objects. */

public final class ForecastProcesses {

  private final TreeMap<String,ForecastProcess> processes;
  private final F<Errors,JsonObject>            writeError;

  /** Produce a new ForecastProcesses object where a new ForecastProcess object
   * is constructed using the given arguments and added to the current mapping
   * with the given process name as its key.
   *
   * @param <META>      The type of metadata to be read from the input by the
   *                    reader. The resulting metadata object will be used for
   *                    selecting filters to be used on the data before
   *                    processing as well as for any formatting parameters to be
   *                    used by the writer.
   *
   * @param <DATA>      The type of the data to be used as input for calculating
   *                    a foreacast.
   *
   * @param <RESULT>    The type of the result of the forecast, which will be
   *                    passed to the writer together with the metadata.
   *
   * @param processName A name for the ForecastProcess.
   *
   * @param reader      A reader object used for producing a data and a
   *                    metadata object from a JsonObject.
   *
   * @param getFilters  A function used for producing a list of data filters
   *                    based on the metadata.
   *
   * @param model       A function for producing a forecast result from the
   *                    data.
   *
   * @param writer      A writer object used for producing a JsonObject from
   *                    the forecast result and the metadata. */

  public <META,DATA,RESULT> ForecastProcesses
    with( final String                                        processName,
          final Formats.Reader<META,DATA>                     reader,
          final F<META,Validation<Errors,List<F<DATA,DATA>>>> getFilters,
          final F<DATA,RESULT>                                model,
          final Formats.Writer<META,RESULT>                   writer) {
      return  
        forecastProcesses(
          processes.set(
            processName,
            forecastProcess(reader,getFilters,model,writeError,writer)),
          writeError); }

  /** Run the ForecastProcess with the given name, using the given InputStream
   *  for its input and the given OutputStream for its output. */

  public IO<Unit>
    run(final String processName, 
        final InputStream inStream,
        final OutputStream outStream) {
      
      final Validation<Errors,ForecastProcess>
        processV =  
          processes.get(processName)
          .<Validation<Errors,ForecastProcess>>map(p  -> success(p))
          .orSome(fail(noSuchForecastProcess(processName)));

      final IO<Validation<Errors,JsonObject>>
        resultIOV = 
          processV.validation(
            err  ->  
              IOFunctions.<Validation<Errors,JsonObject>>unit(fail(err)),
            proc  -> 
              IOFunctions.< Validation<Errors,JsonObject>,
                            Validation<Errors,JsonObject>>map(
                readJsonObject(inStream),
                jsonV  -> jsonV.map(proc.run)));

      return  
        IOFunctions.bind(
          resultIOV,
          resultV  ->
            resultV.validation(
              err      -> writeJsonObject(outStream, writeError.f(err)),
              result   -> writeJsonObject(outStream, result))); }


  /** Produce a ForecastProcess object with the given error writer function
   *  and the given String to ForecastProcess mapping. */

  public static ForecastProcesses
    forecastProcesses(
      final TreeMap<String,ForecastProcess> processes,
      final F<Errors,JsonObject> writeError) {
        return new ForecastProcesses(processes, writeError); }


  /** Produce an empty ForecastProcesses (i.e. one that does not yet contain
   *  any ForecastProcess objects in its String to ForecastProcess mapping).
   *  The given error writer function will be used by any ForecastProcess
   *  objects that are added afterwards. */

  public static ForecastProcesses
    forecastProcesses(final F<Errors,JsonObject> writeError) {
      return new ForecastProcesses(TreeMap.empty(Ord.stringOrd), writeError); }

  private ForecastProcesses(
    final TreeMap<String,ForecastProcess> processes,
    final F<Errors,JsonObject> writeError) {
      this.processes = processes;
      this.writeError = writeError; }

}
