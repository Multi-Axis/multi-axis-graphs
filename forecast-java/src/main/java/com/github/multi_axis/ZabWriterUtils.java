package com.github.multi_axis;

import java.math.BigDecimal;
import java.lang.Long;

import fj.F;
import fj.data.Stream;
//import fj.data.Validation;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;
import javax.json.JsonObject;

import com.github.multi_axis.Errors;
import com.github.multi_axis.TimedValue;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;

//import static fj.data.Validation.success;
//import static fj.data.Validation.fail;

import static com.github.multi_axis.Errors.*;
import static com.github.multi_axis.TimedValue.timedVal;

public abstract class ZabWriterUtils {

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
      Stream<TimedValue<BigDecimal>> timedVals,
      F<BigDecimal,BigDecimal>     valFormat,
      JsonValue                    details) {


        // TODO Errors now to be done in a separate handler.
        //  TODO THINK something cleverer?
        //final F<Errors,JsonObject> onFail = 
        //  fail  -> createObjectBuilder().add("error","error").build();


        final JsonObject jo =
          createObjectBuilder()
          .add(
            "clocks",
            longJsonArray(timedVals.map(tv  -> Long.valueOf(tv.clock))))
          .add(
            "values",
            bigDecimalJsonArray(timedVals.map(tv  -> tv.value).map(valFormat)))
          .add("details",details)
          .build();

        return jo;
  }

  private ZabWriterUtils() {}
}

