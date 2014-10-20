package com.github.multi_axis;

import java.math.BigDecimal;
import java.lang.Long;
import java.math.RoundingMode;

import fj.F;
import fj.data.Option;
import fj.data.Stream;
import fj.data.Validation;
import fj.data.IO;
import fj.data.IOFunctions;
import fj.data.Java;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;

import javax.json.stream.JsonParsingException;
import javax.json.JsonException;
import java.lang.ClassCastException;

import com.github.multi_axis.TimedValue;
import com.github.multi_axis.Errors;

import static fj.data.Validation.success;
import static fj.data.Validation.fail;
import static fj.data.Java.JUList_List;
//import static fj.data.IOFunctions.lazy;

import static javax.json.Json.createReader;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createArrayBuilder;

import static com.github.multi_axis.Errors.*;
import static com.github.multi_axis.TimedValue.timedVal;

public abstract class JsonUtils {


  //---------------------------------------------------------------------------
  //  READING
  //---------------------------------------------------------------------------

  public static final IO<Validation<Errors, Stream<TimedValue<BigDecimal>>>>
    readZab0Or3Json() {
      //return lazy(x  -> zab0Or3Json());
      return new IO<Validation<Errors, Stream<TimedValue<BigDecimal>>>>() {
        public Validation<Errors, Stream<TimedValue<BigDecimal>>> run() {
          return zab0Or3Json();
        }
      };
  }


  //TODO THINK should there be some abstraction here?
  private static final Validation<Errors, Stream<TimedValue<BigDecimal>>>
    zab0Or3Json() {

      final JsonReader jsonReader = createReader(System.in);

      final Validation<Errors,JsonObject> jsonObjV = zab0Or3Object(jsonReader);

      final Validation<Errors, Stream<TimedValue<BigDecimal>>> timedValsV = 
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

  private static final Validation<Errors,Stream<TimedValue<BigDecimal>>>
    zab0Or3TimedVals(
      Validation<Errors,JsonArray> jsonClocksV,
      Validation<Errors,JsonArray> jsonValuesV) {

        final Validation<Errors,Stream<Long>> clocksV
          = jsonClocksV.bind(cs  -> jsonNumbers(cs)).bind(ns  -> longs(ns));

        final Validation<Errors,Stream<BigDecimal>> valsV
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

  private static final Validation<Errors,Stream<BigDecimal>>
    bigDecimals(java.util.List<JsonNumber> jsonNums) {
      try {
        return success( Java.<JsonNumber>JUList_List().f(jsonNums)
                          .toStream().map(n  ->  n.bigDecimalValue()));
      } catch (ClassCastException e) {
        return fail(nonNumberInArray(e));
      }
  }

  private static final Validation<Errors,Stream<Long>>
    longs(java.util.List<JsonNumber> jsonNums) {
      try {
        return success( Java.<JsonNumber>JUList_List().f(jsonNums)
                          .toStream().map(n  ->  Long.valueOf(n.longValue())));
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
    zab0Or3Clocks(final Validation<Errors,JsonObject> jsonObjV) {
      try {
        return jsonObjV.map(jsonObj  -> jsonObj.getJsonArray("clocks"));
      } catch (ClassCastException e) {
        return fail(notJsonArray(e));
      }
  }

  //---------------------------------------------------------------------------
  //  WRITING
  //---------------------------------------------------------------------------

  public static final F<BigDecimal,BigDecimal> zab0ify =
    val  -> val.setScale(4, RoundingMode.HALF_EVEN);

  public static final F<BigDecimal,BigDecimal> zab3ify =
    val  -> val.setScale(0, RoundingMode.HALF_EVEN);

  public static final JsonArray bigDecimalJsonArray(final Stream<BigDecimal> vals) {
    final JsonArrayBuilder b = createArrayBuilder();
    for (BigDecimal val : vals) {
      b.add(val);
    }
    return b.build();
  }

  public static final JsonArray longJsonArray(final Stream<Long> vals) {
    final JsonArrayBuilder b = createArrayBuilder();
    for (Long val : vals) {
      b.add(val.longValue());
    }
    return b.build();
  }


  //TODO THINK should details also be a Validation?
  //  - It should probably be incorporated into the same input as what now is
  //    timedValsV.
  //  |
  //  - As it stands this is now probably not right.
  //    - At least the use site has to be clever/careful.
  //    - 'details' has to depend on validity of 'timedValsV'
  //  - Hm; perhaps Validation<JsonValue,JsonValue>?
  public static final JsonObject
    timedValsDetailsJson(
      Validation<Errors,Stream<TimedValue<BigDecimal>>> timedValsV,
      F<BigDecimal,BigDecimal>                        valFormat,
      JsonValue                                       details) {

        //  TODO THINK something cleverer?
        final F<Errors,JsonObject> onFail = 
          fail  -> createObjectBuilder().add("error","error").build();

        final F<Stream<TimedValue<BigDecimal>>,JsonObject> onSuccess =
          success  -> createObjectBuilder()
          .add(
            "clocks",
            longJsonArray(
              success.map(tv  -> Long.valueOf(tv.clock))))
          .add(
            "values",
            bigDecimalJsonArray(
              success.map(tv  -> tv.value).map(valFormat)))
          .add("details",details)
          .build();

        return timedValsV.validation(onFail,onSuccess);
  }



  private JsonUtils() {}

}
