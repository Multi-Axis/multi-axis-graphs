package com.github.multi_axis;

import java.math.BigDecimal;
import java.lang.Long;
import java.math.RoundingMode;
import java.io.InputStream;

import fj.F;
import fj.data.Option;
import fj.data.Stream;
import fj.data.Validation;
import fj.data.IO;
import fj.data.IOFunctions;
import fj.data.Java;
import fj.data.List;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonString;

import javax.json.stream.JsonParsingException;
import javax.json.JsonException;
import java.lang.ClassCastException;

import com.github.multi_axis.TimedValue;
import com.github.multi_axis.Zab;


import static fj.data.Validation.success;
import static fj.data.Validation.fail;
import static fj.data.Java.JUList_List;
import static fj.data.Option.fromNull;
import static fj.data.List.list;
//import static fj.data.IOFunctions.lazy;

import static javax.json.Json.createReader;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createArrayBuilder;


import static com.github.multi_axis.Utils.toStream;
import static com.github.multi_axis.Errors.*;
import static com.github.multi_axis.TimedValue.timedVal;
import static com.github.multi_axis.Zab.zab;
import static com.github.multi_axis.Zab.Type.*;
import static com.github.multi_axis.JsonUtils.*;
import static com.github.multi_axis.Data.data;

public abstract class ZabReaderImpl {

  public static final 
    F<JsonObject,
      Validation<Errors,Data<Zab,Stream<TimedValue<BigDecimal>>>>>
      read = json  -> zabFromJson(json);


  // NOTE that the 'filters' part is written to use List, i.e. assuming the
  // json format will change into something that may have many filters.
  private static final  
    Validation<Errors, Data<Zab,Stream<TimedValue<BigDecimal>>>>
      zabFromJson(final JsonObject json) {

        final Validation<Errors,Zab.Type>
          ztypev =  getJsonNumber(json, "value_type")
                    .bind((JsonNumber jnum)  -> zabType(jnum));

        final Validation<Errors, Stream<Long>>
          clocksv =  getJsonArray(json, "clocks")
                    .bind(arr  -> jsonNumbers(arr))
                    .bind(nums  -> longs(nums));

        final Validation<Errors, Stream<BigDecimal>>
          valuesv = getJsonArray(json, "values")
                    .bind(arr  -> jsonNumbers(arr))
                    .bind(nums  -> bigDecimals(nums));

        final Validation<Errors, List<String>>
          filtersv =  getJsonObject(json, "params")
                      .bind(obj  -> getJsonString(obj, "preFilter"))
                      .map(jstring  -> list(jstring.getString()));

        final Validation<Errors, Stream<Long>>
          boundsv = getJsonArray(json, "draw_future")
                    .bind(arr  -> jsonNumbers(arr))
                    .bind(nums  -> longs(nums))
                    .bind(bounds  -> 
                      (bounds.length() == 2)  
                        ? success(bounds)
                        : fail(badBounds()));

        return 
          ztypev.bind(    ztype  ->
          clocksv.bind(   clocks  ->
          valuesv.bind(   values  ->
          filtersv.bind(  filters  ->
          boundsv.map(    bounds  ->
          data( zab(ztype, bounds.index(0), bounds.index(1), filters),
                clocks.zip(values)
                  .map(cv  -> timedVal(cv._1().longValue(), cv._2()))))))));

        /* 
        final Validation<Errors, Data<Zab,Stream<TimedValue<BigDecimal>>>>
          datas =
            getJsonNumber(json, "value_type")
              .bind((JsonNumber jnum)  -> zabType(jnum))
              .bind(                                (Zab.Type zabtype)  ->
            getJsonArray(json, "clocks")
              .bind(arr  -> jsonNumbers(arr))
              .bind(nums  -> longs(nums)).bind(               clocks  ->
            getJsonArray(json, "values")
              .bind(arr  -> jsonNumbers(arr))
              .bind(nums  -> bigDecimals(nums)).bind(         values  ->
            getJsonObject(json, "params")
              .bind(obj  -> getJsonString(obj, "preFilter"))
              .map((JsonString jstring)  ->
                     list(jstring.getString())).bind(         filters  ->
            getJsonArray(json, "draw_future")
              .bind(arr  -> jsonNumbers(arr))
              .bind(nums  -> longs(nums)).bind(               bounds  -> 
            success(Boolean.valueOf(bounds.length() == 2))
              .bind(                                          check  ->
            (check.booleanValue() ? success(bounds.index(0)) 
                                  : fail(badBounds())).bind(  start  ->
            success(bounds.index(1)).map(                     end  ->
            data( zab(zabtype,start,end,filters),
                  clocks.zip(values)
                    .map(cv  -> timedVal(cv._1(), cv._2())))))))))));

        return datas; 
        */
      }


  private static final Validation<Errors,Zab.Type> 
    zabType(final JsonNumber jn) {
      final BigDecimal num = jn.bigDecimalValue();
      if      (num.compareTo(BigDecimal.valueOf(0)) == 0) { 
        return success(zab0); }
      else if (num.compareTo(BigDecimal.valueOf(3)) == 0) {
        return success(zab3); }
      else { 
        return fail(badZabType()); } }

  private static final Validation<Errors,java.util.List<javax.json.JsonNumber>>
    jsonNumbers(final JsonArray arr) {
      try { 
        return success(arr.getValuesAs(JsonNumber.class)); }
      catch (ClassCastException e) { 
        return fail(nonNumberInArray(e)); } }

  private static final Validation<Errors,java.util.List<javax.json.JsonString>>
    jsonStrings(final JsonArray arr) {
      try {
        return success(arr.getValuesAs(JsonString.class)); }
      catch (ClassCastException e) {
        return fail(nonStringInArray(e)); } }

  private static final Validation<Errors,Stream<BigDecimal>>
    bigDecimals(final java.util.List<JsonNumber> jsonNums) {
      try {
        return  success(toStream(Java.<JsonNumber>JUList_List().f(jsonNums))
                          .map(n  ->  n.bigDecimalValue())); }
      catch (ClassCastException e) {
        return  fail(nonNumberInArray(e)); } }

  private static final Validation<Errors,Stream<Long>>
    longs(final java.util.List<JsonNumber> jsonNums) {
      try {
        return  success(toStream(Java.<JsonNumber>JUList_List().f(jsonNums))
                          .map(n  ->  Long.valueOf(n.longValue()))); }
      catch (ClassCastException e) {
        return fail(nonNumberInArray(e)); } }


  private ZabReaderImpl() {}
}

