package com.github.multi_axis;

import java.math.BigDecimal;

import fj.F;
import fj.F2;
import fj.Unit;
import fj.data.Stream;
import fj.data.IO;
import fj.data.Validation;
import fj.data.Option;

import javax.json.JsonObject;


public abstract class Formats {

  /** A Reader defines a way of turning a JsonObject into data and metadata
   *  objects of the given types.
   *
   *  @param  <META>  Type of the metadata to be extracted from a JsonObject.
   *
   *  @param  <DATA>  Type of the forecast input data to be extracted from a
   *                  JsonObject. */

  public static class Reader<META,DATA> {

    private F<JsonObject, Validation<Errors,Data<META,DATA>>> read;

    //-------------------------------------------------------------------------
    //  Instance methods.
    //-------------------------------------------------------------------------

    /** Read a JsonObject into a Data object containing the forecast input
     *  data and metadata, or produce an error for erroneous input. */
    public final Validation<Errors,Data<META,DATA>>
      read(JsonObject json) { return read.f(json); }

    /** Read a JsonObject into a Data object containing the forecast input
     *  data and metadata, or produce an error for erroneous input.
     *  First-class function version.*/
    public final F<JsonObject,Validation<Errors,Data<META,DATA>>> 
      read() { return read; }

    //-------------------------------------------------------------------------
    //  Static readers. Add your own here.
    //-------------------------------------------------------------------------

    /** A reader for reading json input of the format specified at TODO
     *  ADD URL HERE. */
    public static final Reader<Zab,Stream<TimedValue<BigDecimal>>>
      zabReader = reader(ZabReaderImpl.read);

    //-------------------------------------------------------------------------
    //  Private.
    //-------------------------------------------------------------------------

    /** Produce a reader from the given function.
     *
     *  @param  <META>  Type of the metadata to be extracted from a JsonObject.
     *
     *  @param  <DATA>  Type of the forecast input data to be extracted from a
     *                  JsonObject.
     *
     *  @param  r       A function for reading a JsonObject into a Data object
     *                  containing the forecast input data and metadata, or
     *                  an Errors object if the input is erroneous. */

    private static final <META,DATA> Reader<META,DATA> 
      reader(F<JsonObject, Validation<Errors,Data<META,DATA>>> r) {
        return new Reader<META,DATA>(r); }

    private Reader(F<JsonObject, Validation<Errors,Data<META,DATA>>> read) {
      this.read = read; }

    private Reader() {}
  }

  /** A Writer defines a way of producing a JsonObject from an object of a
   *  given forecast result data type and from additional information from an
   *  object of a given metadata type.
   *
   *  @param  <META>    Type of the metadata.
   *
   *  @param  <RESULT>  Type of the result of the forecast. */

  public static class Writer<META,RESULT> {

    private F2<META,RESULT,JsonObject> write;

    //-------------------------------------------------------------------------
    //  Instance methods.
    //-------------------------------------------------------------------------

    /** Produce a JsonObject from the given forecast result and metadata */
    public final JsonObject
      write(META meta, RESULT result) { return write.f(meta,result); }

    /** Produce a JsonObject from the given forecast result and metadata
     *  First-class function version. */
    public final F2<META,RESULT,JsonObject>
      write() { return write; }

    //-------------------------------------------------------------------------
    //  Static writers and writer construction methods
    //-------------------------------------------------------------------------

    //-------------------------------------------------
    //  The various actual writers. Add your own here.


    /** A writer for producing JsonObject objects of the format specified at 
     * TODO ADD URL HERE. */
    public static final Writer<Zab,Option<F<BigDecimal,BigDecimal>>>
      zabWriter = writer(ZabWriterImpl.write);

                          
    //-------------------------------------------------------------------------
    //  Private
    //-------------------------------------------------------------------------


    /** Produce a writer from the given function.
     *
     *  @param  <META>    Type of the metadata.
     *
     *  @param  <RESULT>  Type of the forecast result.
     *
     *  @param  w         A writer function for producing a JsonObject from a
     *                    forecast result and metadata. */

    private static final <META,RESULT> Writer<META,RESULT>
      writer(final F2<META,RESULT,JsonObject> w) {
        return new Writer<META,RESULT>(w); }

    private Writer(F2<META,RESULT,JsonObject> write) {
      this.write = write; }
  }

  private Formats() {}
}
