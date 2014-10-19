package com.github.multi_axis;

import java.io.OutputStream;

import fj.Unit;
import fj.F2;
import fj.data.IO;

import javax.json.JsonWriter;

import com.github.multi_axis.Errors;

import static fj.Unit.unit;

import static javax.json.Json.createWriter;
import static javax.json.Json.createObjectBuilder;

public final class ErrorWriterImpl {


  public static final F2<Errors,OutputStream,IO<Unit>>
    write = (err, out)  -> writeError(err,out);

  public static final IO<Unit>
    writeError(Errors err, OutputStream out) {

      return new IO<Unit>() {

        public Unit run() {

          final JsonWriter w = createWriter(out);

          //TODO THINK Something cleverer?
          w.write(createObjectBuilder().add("error","error").build());

          w.close();

          return unit();
        }
      };
  }


  private ErrorWriterImpl() {}

}
