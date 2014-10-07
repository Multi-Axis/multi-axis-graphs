package com.github.multi_axis;

import java.math.BigDecimal;
import java.lang.Long;

import fj.data.Option;
import fj.data.List;
import fj.data.Validation;
import fj.data.IO;
import fj.data.IOFunctions;
import fj.data.Java;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonObject;
import javax.json.JsonArray;
import javax.json.JsonNumber;

import javax.json.stream.JsonParsingException;
import javax.json.JsonException;
import java.lang.ClassCastException;

import com.github.multi_axis.ForecastClasses.*;

import static fj.data.Validation.success;
import static fj.data.Validation.fail;
import static fj.data.Java.JUList_List;
//import static fj.data.IOFunctions.lazy;

import static javax.json.Json.createReader;

import static com.github.multi_axis.Errors.*;
import static com.github.multi_axis.ForecastClasses.timedVal;

public abstract class JsonUtils {


  public static final IO<Validation<Errors, List<TimedValue<BigDecimal>>>>
    readZab0Or3Json() {
      //return lazy(x  -> zab0Or3Json());
      return new IO<Validation<Errors, List<TimedValue<BigDecimal>>>>() {
        public Validation<Errors, List<TimedValue<BigDecimal>>> run() {
          return zab0Or3Json();
        }
      };
  }


  //TODO THINK should there be some abstraction here?
  private static final Validation<Errors, List<TimedValue<BigDecimal>>>
    zab0Or3Json() {

      final JsonReader jsonReader = createReader(System.in);

      final Validation<Errors,JsonObject> jsonObjV = zab0Or3Object(jsonReader);

      final Validation<Errors, List<TimedValue<BigDecimal>>> timedValsV = 
        zab0Or3TimedVals(zab0Or3Clocks(jsonObjV), zab0Or3Values(jsonObjV));

      jsonReader.close();

      return timedValsV;
  }

  private static final Validation<Errors,JsonObject> zab0Or3Object(JsonReader r) {
    try {
      return success(r.readObject());
    } catch (JsonParsingException e) {
      return fail(jsonParsingError(e));
    } catch (JsonException e) {
      return fail(miscJsonError(e));
    }
  }

  private static final Validation<Errors,List<TimedValue<BigDecimal>>>
    zab0Or3TimedVals(
      Validation<Errors,JsonArray> jsonClocksV,
      Validation<Errors,JsonArray> jsonValuesV) {

        final Validation<Errors,List<Long>> clocksV
          = jsonClocksV.bind(cs  -> jsonNumbers(cs)).bind(ns  -> longs(ns));

        final Validation<Errors,List<BigDecimal>> valsV
          = jsonValuesV.bind(vs  -> jsonNumbers(vs)).bind(ns  -> bigDecimals(ns));

        return  clocksV.bind(
          cs  -> valsV.bind(
            vs  -> success(cs.zip(vs).map(
              cv  -> timedVal(cv._1().longValue(), cv._2())
                ))));
  }


  private static final Validation<Errors,java.util.List<javax.json.JsonNumber>>
    jsonNumbers(JsonArray arr) {
      try {
        return success(arr.getValuesAs(JsonNumber.class));
      } catch (ClassCastException e) {
        return fail(nonNumberInArray(e));
      }
  }

  private static final Validation<Errors,List<BigDecimal>>
    bigDecimals(java.util.List<JsonNumber> jsonNums) {
      try {
        return success(Java.<JsonNumber>JUList_List().f(jsonNums).map(n  ->  n.bigDecimalValue()));
      } catch (ClassCastException e) {
        return fail(nonNumberInArray(e));
      }
  }

  private static final Validation<Errors,List<Long>>
    longs(java.util.List<JsonNumber> jsonNums) {
      try {
        return success(Java.<JsonNumber>JUList_List().f(jsonNums).map(n  ->  Long.valueOf(n.longValue())));
      } catch (ClassCastException e) {
        return fail(nonNumberInArray(e));
      }
  }

  private static final Validation<Errors,JsonArray>
    zab0Or3Values(Validation<Errors,JsonObject> jsonObjV) {
      try {
        return jsonObjV.map(jsonObj  -> jsonObj.getJsonArray("values"));
      } catch (ClassCastException e) {
        return fail(notJsonArray(e));
      }
  }

  private static final Validation<Errors,JsonArray>
    zab0Or3Clocks(Validation<Errors,JsonObject> jsonObjV) {
      try {
        return jsonObjV.map(jsonObj  -> jsonObj.getJsonArray("clocks"));
      } catch (ClassCastException e) {
        return fail(notJsonArray(e));
      }
  }

  private JsonUtils() {}

}
