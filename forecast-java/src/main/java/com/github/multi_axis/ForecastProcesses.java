package com.github.multi_axis;

import fj.data.IO;
import fj.Unit;
import fj.Ord;
import fj.data.TreeMap;

import com.github.multi_axis.ForecastProcess;

import static com.github.multi_axis.JsonUtils.readJsonObject;
import static com.github.multi_axis.JsonUtils.writeJsonObject;
import static com.github.multi_axis.Errors.*;

public final class ForecastApp {

  private final TreeMap<String,ForecastProcess> processes;
  private final F<Errors,JsonObject>            writeError;

  public <META,DATA,RESULT> ForecastApp
    with( final String                                        processName,
          final Conf.Reader<META,DATA>                        reader,
          final F<META,Validation<Errors,List<F<DATA,DATA>>>  getFilters,
          final F<DATA,RESULT>                                model,
          final Conf.Writer<META,RESULT>                      writer) {
      return  processes.set(
                processName,
                forecastProcess(reader,getFilters,model,writeError,writer); }

  public IO<Unit>
    run(final String processName, 
        final InputStream inStream,
        final OutputStream outStream) {
      
      final Validation<Error,ForecastProcess>
        processV =  processes.get(processName)
                      .map(p  -> success(p))
                      .orSome(fail(noSuchForecastProcess(processName)));

      final Validation<Error,IO<JsonObject>>
        resultV = processV.bind( proc  -> 
                    readJsonObject(inStream).map(proc.run));

      return  resultV.validation(
                err      -> writeJsonObject(writeError(err)),
                result   -> writeJsonObject(result)); }


  private ForecastApp(final F<Errors,JsonObject> writeError) {
    this.processes = TreeMap.empty(Ord.stringOrd);
    this.writeError = writeError; }

}
