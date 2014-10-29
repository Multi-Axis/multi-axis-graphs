package com.github.multi_axis;

import java.math.BigDecimal;
import java.math.RoundingMode;

import fj.F;
import fj.data.Stream;

import javax.json.JsonObject;

import com.github.multi_axis.TimedValue;
import com.github.multi_axis.ZabWriterUtils;

import static javax.json.Json.createObjectBuilder;

import static com.github.multi_axis.ZabWriterUtils.timedValsDetailsJson;


public abstract class Zab3WriterImpl {

  public static final F<Stream<TimedValue<BigDecimal>>,JsonObject>
    write = vals  -> writeZab3Json(vals);

  public static final JsonObject
    writeZab3Json(Stream<TimedValue<BigDecimal>> vals) {
      return  timedValsDetailsJson(
                vals,
                zab3ify,
                createObjectBuilder().build()); }

  public static final F<BigDecimal,BigDecimal> zab3ify =
    val  -> val.setScale(0, RoundingMode.HALF_EVEN);


  private Zab3WriterImpl() {}
}
