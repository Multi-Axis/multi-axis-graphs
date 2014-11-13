package com.github.multi_axis;

import fj.F;
import fj.data.Validation;

import com.github.multi_axis.Conf;

public final class ErrorWriter<ERR,VALID> {

  private F<ERR,JsonObject> writeError;

  private Conf.Writer<VALID> writeValid;

  public JsonObject 
    write(final Validation<ERR,VALID> input) {
      return input.validation(writeError, writeValid.write().fun); }

  public static <ERR,VALID> ErrorWriter<ERR,VALID>
    errorWriter(final F<ERR,JsonObject> writeError,
                final Conf.Writer<VALID> writeValid) {
      return new ErrorWriter<ERR,VALID> (writeError, writeValid); }

  private ErrorWriter(final F<ERR,JsonObject> writeError,
                      final Conf.Writer<VALID> writeValid) {
    this.writeError = writeError;
    this.writeValid = writeValid; }
}



