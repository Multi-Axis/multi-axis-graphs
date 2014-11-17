package com.github.multi_axis;

import java.math.BigDecimal;
import java.io.InputStream;
import java.io.OutputStream;

import fj.F;
import fj.F2;
import fj.Unit;
import fj.data.Stream;
import fj.data.IO;
import fj.data.Validation;

import javax.json.JsonObject;

import com.github.multi_axis.Tagged;
import com.github.multi_axis.Alt;
import com.github.multi_axis.Nothing;
import com.github.multi_axis.Alts2;
import com.github.multi_axis.Alts2.Alts2Matcher;
import com.github.multi_axis.Alts3;
import com.github.multi_axis.Alts3.Alts3Matcher;
import com.github.multi_axis.Alts4;
import com.github.multi_axis.Alts4.Alts4Matcher;
import com.github.multi_axis.Alts5;
import com.github.multi_axis.Alts5.Alts5Matcher;
import com.github.multi_axis.ZabReaderImpl;
import com.github.multi_axis.Zab;
import com.github.multi_axis.Zab.Zab0;
import com.github.multi_axis.Zab.Zab3;

import static com.github.multi_axis.AltFun.altFun;
import static com.github.multi_axis.Tagged.tag;
import static com.github.multi_axis.Zab.zab0;
import static com.github.multi_axis.Zab.zab3;


//TODO Write Matcher interfaces and add runMatch methods.

public abstract class Conf {

  public static class Reader<OUT> {

    private F<JsonObject, Validation<Errors,OUT>> read;

    //-------------------------------------------------------------------------
    //  Instance methods.
    //-------------------------------------------------------------------------

    public final Validation<Errors,OUT>
      read(JsonObject json) { return read.f(json); }

    public final F<JsonObject,Validation<Errors,OUT>> 
      read() { return read; }

    //-------------------------------------------------------------------------
    //  Static readers. Add your own here.
    //-------------------------------------------------------------------------

    public static final Reader<Alts2<
          Tagged<Zab0,Stream<TimedValue<BigDecimal>>>,
          Tagged<Zab3,Stream<TimedValue<BigDecimal>>>>>

      zabReader 
        = Reader.<Alts2< //TODO see if this declaration can be omitted.
            Tagged<Zab0,Stream<TimedValue<BigDecimal>>>,
            Tagged<Zab3,Stream<TimedValue<BigDecimal>>>>>
          reader(ZabReaderImpl.read);

    //-------------------------------------------------------------------------
    //  Private.
    //-------------------------------------------------------------------------

    private static final <OUT> Reader<OUT> 
      reader(F<JsonObject, Validation<Errors,OUT>> r) {
        return new Reader<OUT>(r); }

    private Reader(F<JsonObject, Validation<Errors,OUT>> read) {
      this.read = read; }

    private Reader() {}
  }

  public static class Writer<IN> {

    private AltFun<IN, JsonObject> write;

    //-------------------------------------------------------------------------
    //  Instance methods.
    //-------------------------------------------------------------------------

    public final JsonObject
      write(IN in) { return write.fun.f(in); }

    public final AltFun<IN,JsonObject>
      write() { return write; }

    public final <FT,A> Writer<Alt<IN,FT,A>> 
      extend(final FT ftag, final Writer<Alt<Nothing,FT,A>> w) {
        return writer(this,ftag,w); }

    //-------------------------------------------------------------------------
    //  Static writers and writer construction methods
    //-------------------------------------------------------------------------

    //-------------------------------------------------
    //  The various actual writers. Add your own here.

    public static final Writer<Alt<Nothing,Zab0,Stream<TimedValue<BigDecimal>>>>
      zab0Writer 
        = writer(zab0, Zab0WriterImpl.write);

    public static final Writer<Alt<Nothing,Zab3,Stream<TimedValue<BigDecimal>>>>
      zab3Writer 
        = writer(zab3, Zab3WriterImpl.write);


    //-----------------------------------------------
    //  Construction methods for derivative writers.

    public static final <OTHERS,PREVFT,PREVIN,FT,IN> 
      Writer<Alt<Alt<OTHERS,PREVFT,PREVIN>,FT,IN>>
        writer(final Writer<Alt<OTHERS,PREVFT,PREVIN>> prevw,
               final FT ftag,
               final Writer<Alt<Nothing,FT,IN>> w) {
          return  writer(altFun(prevw.write(),
                                ftag,
                                in  -> w.write().fun.f(altVal(in)))); }


    // TODO THINK These will be obsolete?

    public static final <A,B> Writer<Alts2<A,B>>
      writerAlts2(Writer<A> wa, Writer<B> wb) {
        return writer(alts  -> alts.runMatch(
          new Alts2Matcher<A,B,JsonObject>() {
            public JsonObject caseAlt1(A a) { return wa.write(a); }
            public JsonObject caseAlt2(B b) { return wb.write(b); } } ) ); }

    public static final <A,B,C> Writer<Alts3<A,B,C>>
      writerAlts3(Writer<A> wa, Writer<B> wb, Writer<C> wc) {
        return writer(alts  -> alts.runMatch(
          new Alts3Matcher<A,B,C,JsonObject>() {
            public JsonObject caseAlt1(A a) { return wa.write(a); }
            public JsonObject caseAlt2(B b) { return wb.write(b); }
            public JsonObject caseAlt3(C c) { return wc.write(c); } } ) ); }

    public static final <A,B,C,D> Writer<Alts4<A,B,C,D>>
      writerAlts4(Writer<A> wa, Writer<B> wb, Writer<C> wc, Writer<D> wd) {
        return writer(alts  -> alts.runMatch(
          new Alts4Matcher<A,B,C,D,JsonObject>() {
            public JsonObject caseAlt1(A a) { return wa.write(a); }
            public JsonObject caseAlt2(B b) { return wb.write(b); }
            public JsonObject caseAlt3(C c) { return wc.write(c); }
            public JsonObject caseAlt4(D d) { return wd.write(d); } } ) ); }

    public static final <A,B,C,D,E> Writer<Alts5<A,B,C,D,E>>
      writerAlts5(Writer<A> wa, Writer<B> wb, Writer<C> wc, Writer<D> wd,
                  Writer<E> we) {
        return writer(alts  -> alts.runMatch(
          new Alts5Matcher<A,B,C,D,E,JsonObject>() {
            public JsonObject caseAlt1(A a) { return wa.write(a); }
            public JsonObject caseAlt2(B b) { return wb.write(b); }
            public JsonObject caseAlt3(C c) { return wc.write(c); }
            public JsonObject caseAlt4(D d) { return wd.write(d); }
            public JsonObject caseAlt5(E e) { return we.write(e); } } ) ); }


                          
    //-------------------------------------------------------------------------
    //  Private
    //-------------------------------------------------------------------------

    private static final <IN> Writer<IN>
      writer(final AltFun<IN,JsonObject> w) { return new Writer<IN>(w); }

    private static final <FT,IN> Writer<Alt<Nothing,FT,IN>> 
      writer(final FT ftag, final F<IN,JsonObject> w) {
        return writer(altFun(ftag,w)); }

    private Writer(AltFun<IN,JsonObject> write) {
      this.write = write; }
  }

  private Conf() {}
}
