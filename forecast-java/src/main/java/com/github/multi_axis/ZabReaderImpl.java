package com.github.multi_axis;

import java.math.BigDecimal;
import java.lang.Long;
import java.math.RoundingMode;
import java.io.InputStream;

import fj.F;
import fj.data.Option;
import fj.data.List;
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
//import static fj.data.IOFunctions.lazy;

import static javax.json.Json.createReader;
import static javax.json.Json.createObjectBuilder;
import static javax.json.Json.createArrayBuilder;


import static com.github.multi_axis.Tagged.tag;
import static com.github.multi_axis.Alts2.alt1;
import static com.github.multi_axis.Alts2.alt2;
import static com.github.multi_axis.Tagged.tag;
import static com.github.multi_axis.Errors.*;
import static com.github.multi_axis.TimedValue.timedVal;
import static com.github.multi_axis.Zab.zab0;
import static com.github.multi_axis.Zab.zab3;

public abstract class ZabReaderImpl {

  public static final F<InputStream, IO<Validation<Errors, Alts2<
                        Tagged<Zab0,List<TimedValue<BigDecimal>>>,
                        Tagged<Zab3,List<TimedValue<BigDecimal>>>>>>>
    read = in  -> readZabJson(in);


  public static final IO<Validation<Errors, Alts2<
                        Tagged<Zab0,List<TimedValue<BigDecimal>>>,
                        Tagged<Zab3,List<TimedValue<BigDecimal>>>>>>
    readZabJson(InputStream in) {
      //return lazy(x  -> zab0Or3Json());
      return new  IO<Validation<Errors, Alts2<
                    Tagged<Zab0,List<TimedValue<BigDecimal>>>,
                    Tagged<Zab3,List<TimedValue<BigDecimal>>>>>>() {

        public  Validation<Errors, Alts2<
                  Tagged<Zab0,List<TimedValue<BigDecimal>>>,
                  Tagged<Zab3,List<TimedValue<BigDecimal>>>>> 
          run() { return zabJson(in); } }; }


  //TODO THINK should there be some abstraction here?
  private static final  Validation<Errors, Alts2<
                          Tagged<Zab0,List<TimedValue<BigDecimal>>>,
                          Tagged<Zab3,List<TimedValue<BigDecimal>>>>>
    zabJson(InputStream in) {

      final JsonReader jsonReader = createReader(in);

      final Validation<Errors,JsonObject> jsonObjV = jsonObject(jsonReader);

      final Validation<Errors,Alts2<
              Tagged<Zab0,List<TimedValue<BigDecimal>>>,
              Tagged<Zab3,List<TimedValue<BigDecimal>>>>>
        
        zabTimedValsV = zabTimedVals(
                          zabTypeJsonNum(jsonObjV),
                          zabClocks(jsonObjV),
                          zabValues(jsonObjV));

      jsonReader.close();

      return zabTimedValsV; }

  private static final Validation<Errors,JsonObject>
    jsonObject(JsonReader r) {
      try {
        return success(r.readObject()); }
      catch (JsonParsingException e) {
        return fail(jsonParsingError(e)); }
      catch (JsonException e) {
        return fail(miscJsonError(e)); } }

  private enum ZEnum { ZAB0, ZAB3, NONE }

  private static final  Validation<Errors,Alts2<
                          Tagged<Zab0,List<TimedValue<BigDecimal>>>,
                          Tagged<Zab3,List<TimedValue<BigDecimal>>>>>
    zabTimedVals(
      Validation<Errors,JsonNumber> jsonZabTypeV,
      Validation<Errors,JsonArray> jsonClocksV,
      Validation<Errors,JsonArray> jsonValuesV) {

        final Validation<Errors,ZEnum> 
          zabTypeV = jsonZabTypeV.map(jzt  -> zabType(jzt));

        final Validation<Errors,List<Long>> 
          clocksV = jsonClocksV.bind(cs  -> jsonNumbers(cs))
                      .bind(ns  -> longs(ns));

        final Validation<Errors,List<BigDecimal>>
          valsV = jsonValuesV.bind(vs  -> jsonNumbers(vs))
                    .bind(ns  -> bigDecimals(ns));

        return  
          zabTypeV.bind(                      ztype  -> 
          clocksV.bind(                       clocks  -> 
          valsV.map(                          vals  -> 
            clocks.zip(vals).map(             cval  ->
              timedVal(
                cval._1().longValue(),
                cval._2()))           ).bind( tvals  -> 

          ztype == ZEnum.ZAB0 ? success(alt1(tag(zab0,tvals))) : (
          ztype == ZEnum.ZAB3 ? success(alt2(tag(zab3,tvals))) : 
                                fail(badZabType()) )))); }

  private static final ZEnum 
    zabType(JsonNumber jn) {
      final BigDecimal num = jn.bigDecimalValue();
      if      (num.compareTo(BigDecimal.valueOf(0)) == 0) { 
        return ZEnum.ZAB0; }
      else if (num.compareTo(BigDecimal.valueOf(3)) == 0) {
        return ZEnum.ZAB3; }
      else { 
        return ZEnum.NONE; } }

  private static final Validation<Errors,java.util.List<javax.json.JsonNumber>>
    jsonNumbers(JsonArray arr) {
      try { 
        return success(arr.getValuesAs(JsonNumber.class)); }
      catch (ClassCastException e) { 
        return fail(nonNumberInArray(e)); } }

  private static final Validation<Errors,List<BigDecimal>>
    bigDecimals(java.util.List<JsonNumber> jsonNums) {
      try {
        return  success(Java.<JsonNumber>JUList_List().f(jsonNums)
                  .map(n  ->  n.bigDecimalValue())); }
      catch (ClassCastException e) {
        return  fail(nonNumberInArray(e)); } }

  private static final Validation<Errors,List<Long>>
    longs(java.util.List<JsonNumber> jsonNums) {
      try {
        return success(Java.<JsonNumber>JUList_List().f(jsonNums)
                .map(n  ->  Long.valueOf(n.longValue()))); }
      catch (ClassCastException e) {
        return fail(nonNumberInArray(e)); } }

  //TODO FIXME These don't look for nulls on missing field!
  private static final Validation<Errors,JsonNumber>
    zabTypeJsonNum(Validation<Errors,JsonObject> jsonObjV) {
      try {
        return jsonObjV.map(jsonObj -> jsonObj.getJsonNumber("value_type")); }
      catch (ClassCastException e) {
        return fail(valueTypeNotNumber(e)); } }

  private static final Validation<Errors,JsonArray>
    zabValues(Validation<Errors,JsonObject> jsonObjV) {
      try {
        return jsonObjV.map(jsonObj  -> jsonObj.getJsonArray("values")); }
      catch (ClassCastException e) {
        return fail(notJsonArray(e)); } }

  private static final Validation<Errors,JsonArray>
    zabClocks(final Validation<Errors,JsonObject> jsonObjV) {
      try {
        return jsonObjV.map(jsonObj  -> jsonObj.getJsonArray("clocks")); }
      catch (ClassCastException e) {
        return fail(notJsonArray(e)); } }


  private ZabReaderImpl() {}
}

