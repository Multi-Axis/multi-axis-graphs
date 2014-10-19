package com.github.multi_axis;

import java.math.BigDecimal;
import java.io.InputStream;
import java.io.OutputStream;

import fj.F;
import fj.F2;
import fj.Unit;
import fj.data.List;
import fj.data.IO;
import fj.data.Validation;

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

//TODO Write Matcher interfaces and add runMatch methods.

public abstract class Conf {

  public static class Reader<OUT> {

    private F<InputStream, IO<Validation<Errors,OUT>>> read;


    public final IO<Validation<Errors,OUT>>
      read(InputStream in) { return read.f(in); }

    public final F<InputStream,IO<Validation<Errors,OUT>>> 
      read() { return read; }


   // public static final Reader<Zab0, List<TimedValue<BigDecimal>>>
   //   zab0Reader 
   //   = Reader.<Zab0, List<TimedValue<BigDecimal>>>reader(ZabReaderImpl.read);

   // public static final Reader<Zab3, List<TimedValue<BigDecimal>>>
   //   zab3Reader 
   //   = Reader.<Zab3, List<TimedValue<BigDecimal>>>reader(ZabReaderImpl.read);

    public static final Reader<Alts2<
          Tagged<Zab0,List<TimedValue<BigDecimal>>>,
          Tagged<Zab3,List<TimedValue<BigDecimal>>>>>

      zabReader 
        = Reader.<Alts2< //TODO see if this declaration can be omitted.
            Tagged<Zab0,List<TimedValue<BigDecimal>>>,
            Tagged<Zab3,List<TimedValue<BigDecimal>>>>>
          reader(ZabReaderImpl.read);

    private static final <OUT> Reader<OUT> 
      reader(F<InputStream, IO<Validation<Errors,OUT>>> r) {
        return new Reader<OUT>(r);
    }

    private Reader(F<InputStream, IO<Validation<Errors,OUT>>> read) {
      this.read = read;
    }

    private Reader() {}
  }

  public static class Writer<IN> {

    private F2<IN, OutputStream, IO<Unit>> write;


    public final IO<Unit>
      write(IN in, OutputStream out) { return write.f(in,out); }

    public final F2<IN, OutputStream, IO<Unit>>
      write() { return write; }


    public static final Writer<Tagged<Zab0,List<TimedValue<BigDecimal>>>>
      zab0Writer 
        = Writer.<Zab0, List<TimedValue<BigDecimal>>>
            taggedWriter(Zab0WriterImpl.write);

    public static final Writer<Tagged<Zab3,List<TimedValue<BigDecimal>>>>
      zab3Writer 
        = Writer.<Zab3, List<TimedValue<BigDecimal>>>
            taggedWriter(Zab3WriterImpl.write);


    private static final <IN> Writer<IN>
      writer(F2<IN, OutputStream, IO<Unit>> w) { return new Writer<IN>(w); }


    private static final <FT,IN> Writer<Tagged<FT,IN>> 
      taggedWriter(F2<IN, OutputStream, IO<Unit>> w) {
        return writer((tagged,out)  -> w.f(tagged.val,out)); }

    public static final <A> Writer<Validation<Errors,A>>
      errorWriter(Writer<A> wa) {
        return writer((vea,out)  -> 
                        vea.validation(
                          (err  -> ErrorWriterImpl.write.f(err,out)),
                          (a    -> wa.write(a,out)))); }
              

    public static final <A,B> Writer<Alts2<A,B>>
      writerAlts2(Writer<A> wa, Writer<B> wb) {
        return writer((alts,out)  -> alts.runMatch(
          new Alts2Matcher<A,B,IO<Unit>>() {
            public IO<Unit> caseAlt1(A a) { return wa.write(a,out); }
            public IO<Unit> caseAlt2(B b) { return wb.write(b,out); } } ) ); }

    public static final <A,B,C> Writer<Alts3<A,B,C>>
      writerAlts3(Writer<A> wa, Writer<B> wb, Writer<C> wc) {
        return writer((alts,out)  -> alts.runMatch(
          new Alts3Matcher<A,B,C,IO<Unit>>() {
            public IO<Unit> caseAlt1(A a) { return wa.write(a,out); }
            public IO<Unit> caseAlt2(B b) { return wb.write(b,out); }
            public IO<Unit> caseAlt3(C c) { return wc.write(c,out); } } ) ); }

    public static final <A,B,C,D> Writer<Alts4<A,B,C,D>>
      writerAlts4(Writer<A> wa, Writer<B> wb, Writer<C> wc, Writer<D> wd) {
        return writer((alts,out)  -> alts.runMatch(
          new Alts4Matcher<A,B,C,D,IO<Unit>>() {
            public IO<Unit> caseAlt1(A a) { return wa.write(a,out); }
            public IO<Unit> caseAlt2(B b) { return wb.write(b,out); }
            public IO<Unit> caseAlt3(C c) { return wc.write(c,out); }
            public IO<Unit> caseAlt4(D d) { return wd.write(d,out); } } ) ); }

    public static final <A,B,C,D,E> Writer<Alts5<A,B,C,D,E>>
      writerAlts5(Writer<A> wa, Writer<B> wb, Writer<C> wc, Writer<D> wd,
                  Writer<E> we) {
        return writer((alts,out)  -> alts.runMatch(
          new Alts5Matcher<A,B,C,D,E,IO<Unit>>() {
            public IO<Unit> caseAlt1(A a) { return wa.write(a,out); }
            public IO<Unit> caseAlt2(B b) { return wb.write(b,out); }
            public IO<Unit> caseAlt3(C c) { return wc.write(c,out); }
            public IO<Unit> caseAlt4(D d) { return wd.write(d,out); }
            public IO<Unit> caseAlt5(E e) { return we.write(e,out); } } ) ); }


    private Writer(F2<IN, OutputStream, IO<Unit>> write) {
      this.write = write;
    }

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
