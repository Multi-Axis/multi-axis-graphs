package com.github.multi_axis;

import java.math.BigDecimal;
import java.math.RoundingMode;

import fj.F;
import fj.F2;
import fj.data.Stream;

import javax.json.JsonObject;

import com.github.multi_axis.TimedValue;
import com.github.multi_axis.ZabWriterUtils;

import static javax.json.Json.createObjectBuilder;

import static com.github.multi_axis.ZabWriterUtils.timedValsDetailsJson;
import static com.github.multi_axis.ZabWriterUtils.formatter;
import static com.github.multi_axis.Utils.*;

public abstract class ZabWriterImpl {

  public static final F2<Zab, F<BigDecimal,BigDecimal>, JsonObject>
    write = (meta,result)  -> writeZabJson(meta,result);

  public static final JsonObject
    writeZabJson(Zab meta, F<BigDecimal,BigDecimal> result) {
      return  timedValsDetailsJson(
                plot(result, meta.bounds.start, meta.bounds.end, daySecs),
                formatter(meta.type),
                createObjectBuilder().build()); }


  private ZabWriterImpl() {}

} 

