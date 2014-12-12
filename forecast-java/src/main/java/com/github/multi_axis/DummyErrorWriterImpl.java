package com.github.multi_axis;

import fj.F;

import javax.json.JsonObject;

import com.github.multi_axis.Errors;

import static javax.json.Json.createObjectBuilder;

public final class DummyErrorWriterImpl {

  public static final F<Errors,JsonObject>
    write = err  -> writeError(err);

  public static final JsonObject
    writeError(Errors err) {
      //TODO THINK Something cleverer?
      return createObjectBuilder().add("error","error").build(); }


  private DummyErrorWriterImpl() {}

}
