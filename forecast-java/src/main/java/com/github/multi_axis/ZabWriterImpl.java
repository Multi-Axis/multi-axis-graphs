package com.github.multi_axis;

import java.math.BigDecimal;
import java.math.RoundingMode;

import fj.F;
import fj.F2;
import fj.data.Stream;
import fj.data.Option;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import static javax.json.Json.createObjectBuilder;

import static com.github.multi_axis.ZabWriterUtils.timedValsDetailsJson;
import static com.github.multi_axis.ZabWriterUtils.formatter;
import static com.github.multi_axis.Utils.*;

public abstract class ZabWriterImpl {

  public static final F2<Zab, Option<F<BigDecimal,BigDecimal>>, JsonObject>
    write = (meta,result)  -> writeZabJson(meta,result);

  public static final JsonObject
    writeZabJson(Zab meta, Option<F<BigDecimal,BigDecimal>> result) {

      final JsonObjectBuilder jsonbldr = createObjectBuilder();

      return  
        timedValsDetailsJson(
          result.map( fun  -> 
            meta.drawFuture.map(time  ->
              timedVal( time.longValue(),
                        fun(BigDecimal.valueOf(time.longValue()))))
            ).orSome(Stream.nil()),
          formatter(meta.type),
          result.map(fun  -> jsonbldr.build())
                .orSome(jsonbldr
                          .add("warning", "Not enough data for forecast.")
                          .build())); }

  private ZabWriterImpl() {}

} 

