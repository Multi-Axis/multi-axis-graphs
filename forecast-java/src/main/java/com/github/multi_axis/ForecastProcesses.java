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

import com.github.multi_axis.ForecastProcess;

import static fj.data.Validation.success;
import static fj.data.Validation.fail;

import static com.github.multi_axis.ForecastProcess.forecastProcess;
import static com.github.multi_axis.JsonUtils.readJsonObject;
import static com.github.multi_axis.JsonUtils.writeJsonObject;
import static com.github.multi_axis.Errors.*;

public final class ForecastProcesses {

  private final TreeMap<String,ForecastProcess> processes;
  private final F<Errors,JsonObject>            writeError;

  public <META,DATA,RESULT> ForecastProcesses
    with( final String                                        processName,
          final Conf.Reader<META,DATA>                        reader,
          final F<META,Validation<Errors,List<F<DATA,DATA>>>> getFilters,
          final F<DATA,RESULT>                                model,
          final Conf.Writer<META,RESULT>                      writer) {
      return  
        forecastProcesses(
          processes.set(
            processName,
            forecastProcess(reader,getFilters,model,writeError,writer)),
          writeError); }

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

  public static ForecastProcesses
    forecastProcesses(
      final TreeMap<String,ForecastProcess> processes,
      final F<Errors,JsonObject> writeError) {
        return new ForecastProcesses(processes, writeError); }

  public static ForecastProcesses
    forecastProcesses(final F<Errors,JsonObject> writeError) {
      return new ForecastProcesses(TreeMap.empty(Ord.stringOrd), writeError); }

  private ForecastProcesses(
    final TreeMap<String,ForecastProcess> processes,
    final F<Errors,JsonObject> writeError) {
      this.processes = processes;
      this.writeError = writeError; }

}
