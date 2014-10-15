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

import com.github.multi_axis.ZabReaderImpl;
import com.github.multi_axis.Zab;
import com.github.multi_axis.Zab.Zab0;
import com.github.multi_axis.Zab.Zab3;

//TODO Write Matcher interfaces and add runMatch methods.

public abstract class Conf {

  //TODO THINK Should these have a common supertype, and methods, after all?

  public static abstract class Reader<FT,OUT> {

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


    private static final <FT,OUT> Reader<FT,OUT> 
      reader(F<InputStream, IO<Validation<Errors,OUT>>> r) {
        return new Reader<FT,OUT>() {
          private final F<InputStream, IO<Validation<Errors,OUT>>> 
            read = r;
        };
    }

    private Reader() {}
  }

  public static abstract class ReaderAlts2<FA,FB,OA,OB> {

    private F<InputStream, IO<Validation<Errors, Alts2<OA,OB>>>> read;


    public final IO<Validation<Errors, Alts2<OA,OB>>> 
      read(InputStream in) { return read.f(in); }

    public final F<InputStream, IO<Validation<Errors, Alts2<OA,OB>>>>
      read() { return read; }

    private static final <FA,FB,OA,OB> ReaderAlts2<FA,FB,OA,OB>
      reader(F<InputStream, IO<Validation<Errors, Alts2<OA,OB>>>> r) {
        return new ReaderAlts2<FA,FB,OA,OB>() {
          private final F<InputStream, IO<Validation<Errors,Alts2<OA,OB>>>>
            read = r;
        };
    }

    private ReaderAlts2() {}
  }

  public static abstract class ReaderAlts3<FA,FB,FC,OA,OB,OC> {

    private F<InputStream, IO<Validation<Errors, Alts3<OA,OB,OC>>>> read;


    public final IO<Validation<Errors, Alts3<OA,OB,OC>>> 
      read(InputStream in) { return read.f(in); }

    public final F<InputStream, IO<Validation<Errors, Alts3<OA,OB,OC>>>>
      read() { return read; }

    private static final <FA,FB,FC,OA,OB,OC> ReaderAlts3<FA,FB,FC,OA,OB,OC>
      reader(F<InputStream, IO<Validation<Errors, Alts3<OA,OB,OC>>>> r) {
        return new ReaderAlts3<FA,FB,FC,OA,OB,OC>() {
          private final F<InputStream, IO<Validation<Errors,Alts3<OA,OB,OC>>>>
            read = r;
        };
    }

    private ReaderAlts3() {}
  }

  public static abstract class ReaderAlts4<FA,FB,FC,FD,OA,OB,OC,OD> {

    private F<InputStream, IO<Validation<Errors, Alts4<OA,OB,OC,OD>>>> read;


    public final IO<Validation<Errors, Alts4<OA,OB,OC,OD>>> 
      read(InputStream in) { return read.f(in); }

    public final F<InputStream, IO<Validation<Errors, Alts4<OA,OB,OC,OD>>>>
      read() { return read; }

    private static final <FA,FB,FC,FD,OA,OB,OC,OD> ReaderAlts4<FA,FB,FC,FD,OA,OB,OC,OD>
      reader(F<InputStream, IO<Validation<Errors, Alts4<OA,OB,OC,OD>>>> r) {
        return new ReaderAlts4<FA,FB,FC,FD,OA,OB,OC,OD>() {
          private final F<InputStream, IO<Validation<Errors,Alts4<OA,OB,OC,OD>>>>
            read = r;
        };
    }

    private ReaderAlts4() {}
  }

  public static abstract class ReaderAlts5<FA,FB,FC,FD,FE,OA,OB,OC,OD,OE> {

    private F<InputStream, IO<Validation<Errors, Alts5<OA,OB,OC,OD,OE>>>> read;


    public final IO<Validation<Errors, Alts5<OA,OB,OC,OD,OE>>> 
      read(InputStream in) { return read.f(in); }

    public final F<InputStream, IO<Validation<Errors, Alts5<OA,OB,OC,OD,OE>>>>
      read() { return read; }

    private static final <FA,FB,FC,FD,FE,OA,OB,OC,OD,OE> ReaderAlts5<FA,FB,FC,FD,FE,OA,OB,OC,OD,OE>
      reader(F<InputStream, IO<Validation<Errors, Alts5<OA,OB,OC,OD,OE>>>> r) {
        return new ReaderAlts5<FA,FB,FC,FD,FE,OA,OB,OC,OD,OE>() {
          private final F<InputStream, IO<Validation<Errors,Alts5<OA,OB,OC,OD,OE>>>>
            read = r;
        };
    }

    private ReaderAlts5() {}
  }

  public static abstract class Writer<FT,IN> {

    private F2<Validation<Errors,IN>, OutputStream, IO<Unit>> write;


    public final IO<Unit>
      write(Validation<Errors,IN> in, OutputStream out) { return write.f(in,out); }

    public final F2<Validation<Errors,IN>, OutputStream, IO<Unit>>
      write() { return write; }


    public static final Writer<Zab0, List<TimedValue<BigDecimal>>>
      zab0Writer 
      = Writer.<Zab0, List<TimedValue<BigDecimal>>>writer(Zab0WriterImpl.write);

    public static final Writer<Zab3, List<TimedValue<BigDecimal>>>
      zab3Writer 
      = Writer.<Zab3, List<TimedValue<BigDecimal>>>writer(Zab3WriterImpl.write);


    private static final <FT,IN> Writer<FT,IN> 
      writer(F2<Validation<Errors,IN>, OutputStream, IO<Unit>> w) {
        return new Writer<FT,IN>() {
          private final 
            F2<Validation<Errors,IN>, OutputStream, IO<Unit>>
              write = w;
        };
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
