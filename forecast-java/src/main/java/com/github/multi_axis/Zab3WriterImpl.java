package com.github.multi_axis;

import java.io.OutputStream;

import java.math.BigDecimal;
import java.math.RoundingMode;

import fj.F;
import fj.F2;
import fj.Unit;
import fj.data.Stream;
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

public abstract class Zab3WriterImpl {

  public static final F2< Stream<TimedValue<BigDecimal>>,
                          OutputStream,
                          IO<Unit>>
    write = (vals, out)  -> writeZab3Json(vals,out);

  public static final IO<Unit>
    writeZab3Json(
      Stream<TimedValue<BigDecimal>> vals,
      OutputStream out) {
      
      return new IO<Unit>() {
        public Unit run() {

          final JsonWriter w =  createWriter(out);
            
          w.write(timedValsDetailsJson(
            vals,
            zab3ify,
            createObjectBuilder().build()));

          w.close();

          return unit();
        }
      };
  }

  public static final F<BigDecimal,BigDecimal> zab3ify =
    val  -> val.setScale(0, RoundingMode.HALF_EVEN);


  private Zab3WriterImpl() {}
}
