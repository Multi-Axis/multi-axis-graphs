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

import javax.json.stream.JsonParsingException;
import javax.json.JsonException;
import java.lang.ClassCastException;

import com.github.multi_axis.Alts2;
import com.github.multi_axis.Tagged;
import com.github.multi_axis.TimedValue;
import com.github.multi_axis.Zab.Zab0;
import com.github.multi_axis.Zab.Zab3;


import static fj.data.Validation.success;
import static fj.data.Validation.fail;
import static fj.data.Java.JUList_List;
import static fj.data.Option.fromNull;
//import static fj.data.IOFunctions.lazy;

import static javax.json.Json.createReader;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createArrayBuilder;


import static com.github.multi_axis.Utils.toStream;
import static com.github.multi_axis.Tagged.tag;
import static com.github.multi_axis.Alts2.alt1;
import static com.github.multi_axis.Alts2.alt2;
import static com.github.multi_axis.Tagged.tag;
import static com.github.multi_axis.Errors.*;
import static com.github.multi_axis.TimedValue.timedVal;
import static com.github.multi_axis.Zab.zab0;
import static com.github.multi_axis.Zab.zab3;

public abstract class ZabReaderImpl {

  public static final F<JsonObject, Validation<Errors, Alts2<
                        Tagged<Zab0,Stream<TimedValue<BigDecimal>>>,
                        Tagged<Zab3,Stream<TimedValue<BigDecimal>>>>>>
    read = json  -> zabFromJson(json);


  private static final  Validation<Errors, Alts2<
                          Tagged<Zab0,Stream<TimedValue<BigDecimal>>>,
                          Tagged<Zab3,Stream<TimedValue<BigDecimal>>>>>
    zabFromJson(final JsonObject json) {

      return
        zabTypeJsonNum(json).bind(  jZabType  ->
        zabClocks(json).bind(       jClocks  ->
        zabValues(json).bind(       jVals  -> 
        zabTimedVals(jZabType,jClocks,jVals)))); }


  private static final  Validation<Errors,Alts2<
                          Tagged<Zab0,Stream<TimedValue<BigDecimal>>>,
                          Tagged<Zab3,Stream<TimedValue<BigDecimal>>>>>
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


  private enum ZEnum { ZAB0, ZAB3, NONE }

  private static final ZEnum 
    zabType(final JsonNumber jn) {
      final BigDecimal num = jn.bigDecimalValue();
      if      (num.compareTo(BigDecimal.valueOf(0)) == 0) { 
        return ZEnum.ZAB0; }
      else if (num.compareTo(BigDecimal.valueOf(3)) == 0) {
        return ZEnum.ZAB3; }
      else { 
        return ZEnum.NONE; } }

  private static final Validation<Errors,java.util.List<javax.json.JsonNumber>>
    jsonNumbers(final JsonArray arr) {
      try { 
        return success(arr.getValuesAs(JsonNumber.class)); }
      catch (ClassCastException e) { 
        return fail(nonNumberInArray(e)); } }

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

  private static final Validation<Errors,JsonNumber>
    zabTypeJsonNum(final JsonObject jsonObj) {
      try {
        return  fromNull(jsonObj.getJsonNumber("value_type"))
                  .map(x  -> Validation.<Errors,JsonNumber>success(x))
                  .orSome(fail(noJsonField("value_type"))); }
      catch (ClassCastException e) {
        return fail(valueTypeNotNumber(e)); } }

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


  private ZabReaderImpl() {}
}

