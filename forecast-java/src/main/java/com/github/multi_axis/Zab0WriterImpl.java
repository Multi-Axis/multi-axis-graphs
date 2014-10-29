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


public abstract class Zab0WriterImpl {

  public static final F<Stream<TimedValue<BigDecimal>>,JsonObject>
    write = vals  -> writeZab0Json(vals);

  public static final JsonObject
    writeZab0Json(Stream<TimedValue<BigDecimal>> vals) {
      return  timedValsDetailsJson(
              vals,
              zab0ify,
              createObjectBuilder().build()); }

  private static final F<BigDecimal,BigDecimal> zab0ify =
    val  -> val.setScale(4, RoundingMode.HALF_EVEN);


  private Zab0WriterImpl() {}
}
