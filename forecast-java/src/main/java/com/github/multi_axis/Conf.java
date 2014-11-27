package com.github.multi_axis;

import java.math.BigDecimal;

import fj.F;
import fj.F2;
import fj.Unit;
import fj.data.Stream;
import fj.data.IO;
import fj.data.Validation;

import javax.json.JsonObject;

import com.github.multi_axis.Zab;
import com.github.multi_axis.ZabReaderImpl;
import com.github.multi_axis.ZabWriterImpl;

//TODO Write Matcher interfaces and add runMatch methods.

public abstract class Conf {

  public static class Reader<META,DATA> {

    private F<JsonObject, Validation<Errors,Data<META,DATA>>> read;

    //-------------------------------------------------------------------------
    //  Instance methods.
    //-------------------------------------------------------------------------

    public final Validation<Errors,Data<META,DATA>>
      read(JsonObject json) { return read.f(json); }

    public final F<JsonObject,Validation<Errors,Data<META,DATA>>> 
      read() { return read; }

    //-------------------------------------------------------------------------
    //  Static readers. Add your own here.
    //-------------------------------------------------------------------------

    public static final Reader<Zab,Stream<TimedValue<BigDecimal>>>
      zabReader = reader(ZabReaderImpl.read);

    //-------------------------------------------------------------------------
    //  Private.
    //-------------------------------------------------------------------------

    private static final <META,DATA> Reader<META,DATA> 
      reader(F<JsonObject, Validation<Errors,Data<META,DATA>>> r) {
        return new Reader<META,DATA>(r); }

    private Reader(F<JsonObject, Validation<Errors,Data<META,DATA>>> read) {
      this.read = read; }

    private Reader() {}
  }

  public static class Writer<META,RESULT> {

    private F2<META,RESULT,JsonObject> write;

    //-------------------------------------------------------------------------
    //  Instance methods.
    //-------------------------------------------------------------------------

    public final JsonObject
      write(META meta, RESULT result) { return write.f(meta,result); }

    public final F2<META,RESULT,JsonObject>
      write() { return write; }

    //-------------------------------------------------------------------------
    //  Static writers and writer construction methods
    //-------------------------------------------------------------------------

    //-------------------------------------------------
    //  The various actual writers. Add your own here.

    public static final Writer<Zab,F<BigDecimal,BigDecimal>>
      zabWriter = writer(ZabWriterImpl.write);

                          
    //-------------------------------------------------------------------------
    //  Private
    //-------------------------------------------------------------------------

    private static final <META,RESULT> Writer<META,RESULT>
      writer(final F2<META,RESULT,JsonObject> w) {
        return new Writer<META,RESULT>(w); }

    private Writer(F2<META,RESULT,JsonObject> write) {
      this.write = write; }
  }

  private Conf() {}
}
