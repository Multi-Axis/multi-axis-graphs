package com.github.multi_axis;

//import java.math.BigDecimal;

//import fj.F;
//import fj.data.List;

//TODO Write Matcher interfaces and add runMatch methods.

//TODO THINK ConfParams interface needs to be written elsewhere, no?
//  - OTOH Do I even need to declare that extension here?

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
