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

import com.github.multi_axis.Alts2;
import com.github.multi_axis.Tagged;
import com.github.multi_axis.TimedValue;
import com.github.multi_axis.Zab;


import static fj.data.Validation.success;
import static fj.data.Validation.fail;
import static fj.data.Java.JUList_List;
import static fj.data.Option.fromNull;
//import static fj.data.IOFunctions.lazy;

import static javax.json.Json.createReader;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createArrayBuilder;


import static com.github.multi_axis.Utils.toStream;
import static com.github.multi_axis.Errors.*;
import static com.github.multi_axis.TimedValue.timedVal;
import static com.github.multi_axis.Zab.Type.*;
import static com.github.multi_axis.JsonUtils.*
import static com.github.multi_axis.Data.data;

public abstract class ZabReaderImpl {

  public static final 
    F<JsonObject,
      Validation<Errors,Data<Zab,TimedValue<BigDecimal>>>>
      read = json  -> zabFromJson(json);


  // NOTE that the 'filters' part is written to use List, i.e. assuming the
  // json format will change into something that may have many filters.
  private static final  
    Validation<Errors, Data<Zab,Stream<TimedValue<BigDecimal>>>>
      zabFromJson(final JsonObject json) {
        return
          getJsonNumber(json, "value_type")
            .bind(jnum  -> zabType(jnum)).bind(               zabtype  ->
          getJsonArray(json, "clocks")
            .bind(arr  -> jsonNumbers(arr))
            .bind(nums  -> longs(nums)).bind(                 clocks  ->
          getJsonArray(json, "values")
            .bind(arr  -> jsonNumbers(arr))
            .bind(nums  -> bigDecimals(nums)).bind(           values  ->
          getJsonObject(json, "params")
            .bind(obj  -> getJsonString(obj, "preFilter"))
            .map(jstring  -> list(jstring.getString())).bind( filters  ->
          getJsonArray(json, "draw_future")
            .bind(arr  -> jsonNumbers(arr))
            .bind(nums  -> longs(nums)).bind(                 bounds  -> 
          success((bounds.length() == 2)).bind(               check  ->
          (check  ? success(bounds.head()) 
                  : fail(badBounds())).bind(                  start  ->
          success(bounds.tail().head()).map(                  end  ->
          data( zab(zabtype,start,end,filters),
                clocks.zip(values)
                  .map(cvs  -> timedVal(cvs._1(), cvs._2()))))))))))); }


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

  /*
  private static final
    Validation<Errors,Data<Zab,TimedValue<BigDecimal<>>
      zabTimedVals(
        final JsonNumber jZabType,
        final JsonArray  jClocks,
        final JsonArray  jValues) {

          final Validation<Errors,Stream<Long>> 
            clocksV = jsonNumbers(jClocks).bind(ns  -> longs(ns));

          final Validation<Errors,Stream<BigDecimal>>
            valsV = jsonNumbers(jValues).bind(ns  -> bigDecimals(ns));

          final ZEnum 
            zabType = zabType(jZabType);

          return  
            clocksV.bind( clocks  -> 
            valsV.map(    vals  -> 
                    clocks.zip(vals).map( cval  ->
                      timedVal(
                        cval._1().longValue(),
                        cval._2()))           
                  ).bind( tvals  -> 
            zabType == ZEnum.ZAB0 ? success(alt1(tag(zab0,tvals))) : (
            zabType == ZEnum.ZAB3 ? success(alt2(tag(zab3,tvals))) : 
                                    fail(badZabType()) ))); }

  private static final Validation<Errors,JsonNumber>
    zabTypeJsonNum(final JsonObject jsonObj) {
      try {
        return  fromNull(jsonObj.getJsonNumber("value_type"))
                  .map(x  -> Validation.<Errors,JsonNumber>success(x))
                  .orSome(fail(noJsonField("value_type"))); }
      catch (ClassCastException e) {
        return fail(valueTypeNotNumber(e)); } }

  private static final Validation<Errors,JsonObject>
    params(final JsonObject jsonObj) {
      try {
        return  fromNull(jsonObj.getJsonObject("params"))
                  .map(x  -> Validation.<Errors,JsonObject>success(x))
                  .orSome(fail(noJsonField("params"))); }
      catch (ClassCastException e) {
        return fail(notJsonObject(e)); } }


  private static final Validation<Errors,JsonArray>
    zabValues(final JsonObject jsonObj) {
      try {
        return  fromNull(jsonObj.getJsonArray("values"))
                  .map(x  -> Validation.<Errors,JsonArray>success(x))
                  .orSome(fail(noJsonField("values"))); }
      catch (ClassCastException e) {
        return fail(notJsonArray(e)); } }

  private static final Validation<Errors,JsonArray>
    zabClocks(final JsonObject jsonObj) {
      try {
        return  fromNull(jsonObj.getJsonArray("clocks"))
                  .map(x  -> Validation.<Errors,JsonArray>success(x))
                  .orSome(fail(noJsonField("clocks"))); }
      catch (ClassCastException e) {
        return fail(notJsonArray(e)); } }
  */


  private ZabReaderImpl() {}
}

