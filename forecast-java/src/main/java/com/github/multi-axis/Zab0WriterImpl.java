package com.github.multi_axis;

import java.io.OutputStream;

import java.math.BigDecimal;
import java.math.RoundingMode;

import fj.F;
import fj.F2;
import fj.Unit;
import fj.data.List;
import fj.data.Validation;
import fj.data.IO;

import javax.json.Json;
import javax.json.JsonWriter;

import com.github.multi_axis.Errors;
import com.github.multi_axis.TimedValue;
import com.github.multi_axis.ZabWriterUtils;

import static fj.Unit.unit;

import static javax.json.Json.createWriter;
import static javax.json.Json.createObjectBuilder;

import static com.github.multi_axis.ZabWriterUtils.timedValsDetailsJson;

public abstract class Zab0WriterImpl {

  public static final F2< Validation<Errors, List<TimedValue<BigDecimal>>>,
                          OutputStream,
                          IO<Unit>>
    write = (vals, out)  -> writeZab0Json(vals,out);

  public static final IO<Unit>
    writeZab0Json(
      Validation<Errors, List<TimedValue<BigDecimal>>> vals,
      OutputStream out) {
      
      return new IO<Unit>() {
        public Unit run() {

          final JsonWriter w = createWriter(System.out);
            
          w.write(timedValsDetailsJson(
            vals,
            zab0ify,
            createObjectBuilder().build()));

          w.close();

          return unit();
        }
      };
  }

  private static final F<BigDecimal,BigDecimal> zab0ify =
    val  -> val.setScale(4, RoundingMode.HALF_EVEN);


  private Zab0WriterImpl() {}
}
