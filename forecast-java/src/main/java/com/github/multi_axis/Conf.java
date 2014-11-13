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

    // TODO THINK Obsolete?
    public static final <FT,IN> Writer<Alt<Nothing,FT,IN>>
      altWriter(final FT ftag, final Writer<Tagged<FT,IN>> w) {
        return writer(altFun( ftag,
                              (IN in)  -> w.write(tag(ftag,in)))); }

    public static final <OTHERS,PREVFT,PREVIN,FT,IN> 
      Writer<Alt<Alt<OTHERS,PREVFT,PREVIN>,FT,IN>>
        altWriter(final Writer<Alt<OTHERS,PREVFT,PREVIN>> prevw,
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

    private Writer() {}
  }

  private Conf() {}
}
/*
public abstract class Conf<ROLE, ROLEPARAMS, FT> {

  public abstract class Role {
    public static final class FType extends Role {}
    public static final class Read extends Role {}
    public static final class Write extends Role {}

    private Role() {}
  }

  public abstract class NA {} // Empty role params for FType

  public abstract class ReadP<OUT,READER> {} // Role params for Read

  public abstract class WriteP<IN,WRITER> {} // Role params for Write

  //TODO THINK What in these? Are these useful?

  private abstract class Reader<OUT,READER,FT>
    extends Conf<Read, ReadP<OUT,READER>, FT> {}

  private abstract class Writer<IN,READER,FT>
    extends Conf<Write, WriteP<IN,WRITER>, FT> {}
    


  public interface ForecastTypeMatcher<R> {
    public R caseZab0();
    public R caseZab3();
  }

  public static abstract class ForecastType<FT> extends Conf<FType,FT> {

    //TODO THINK Will I ultimately need this at all?
    public abstract <R> R runMatch(ForecastTypeMatcher<R> m);

    //TODO THINK I don't think I can sensibly have Conf<Read,Foo> have a member with
    //  a concrete ReaderImpl (or whatever) in it. I should however be able to
    //  define a method that takes specifically Conf<Read,Foo> and returns the
    //  appropriate ReaderImpl, no?
    //  - Or can I?
    private Conf<Read,FT> reader 

    //TODO THINK Should the zab0() etc methods actually return the subtype?
    //  - Or might I use F-boundedness, if something like that is indeed
    //    necessary?

    public static final class Zab0 extends ForecastType<Zab0> {
      public static final ForecastType<Zab0>
        zab0() { return new Zab0(); }
      public <R> R
        runMatch(ForecasTypeMatcher<R> m) { m.caseZab0() }
      private Zab0() {}
    }

    public static final class Zab3 extends ForecastType<Zab3> {
      public static final ForecastType<Zab3>
        zab3() { return new Zab3(); }
      public <R> R
        runMatch(ForecasTypeMatcher<R> m) { m.caseZab3() }
      private Zab3() {}
    }

    private ForecastType() {}
  }

  //TODO THINK OkReader is something that has to be sealed somehow, no?

  public static abstract class OkReader<FT,OUT,R> {



    private OkReader() {}
  }


  private static final ForecastTypeMatcher<OkReader<FT,OUT,R>>
    readerMatcher = new ForecastTypeMatcher<OkReader<FT,OUT,R>> {
      public OkReader<caseZab0 //TODO THINK STOP PRESS -- Component<ROLE,FT> ?

  public static <FT,OUT,R> OkReader<FT,OUT,R> 
    reader(ForecastType<FT> t) {

  private ForecastSetup() {}

}
*/
