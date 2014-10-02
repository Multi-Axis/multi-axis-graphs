package com.github.multi-axis;

//TODO write Matcher interfaces and add runMatch methods.
//TODO read about Jackson types

public abstract class ForecastClasses {

  public static final class Zab0 {
    private Zab0() {}
  }
  public static final class Zab3 {
    private Zab3() {}
  }

  public static final class TimedValue<V> {
    public final long clock;
    public final V value;

    public TimedValue(long clock, V value) {
      this.clock = clock;
      this.value = value;
    }
  }

  public static TimedValue<V> timedVal(long clock, V value) {
    return new TimedValue(clock, value);
  }

  public static abstract class ForecastFunction<V> {

    private static final class Zab0ForecastFunction 
      extends ForecastFunction<Zab0> {

      // Function from  List<TimedValue<BigDecimal>>
      //          to    List<TimedValue<BigDecimal>>
      private final F<List<TimedValue<BigDecimal>>,
                      List<TimedValue<BigDecimal>>> func;

      private Zab0ForecastFunction(
        final F<List<TimedValue<BigDecimal>>,
                List<TimedValue<BigDecimal>>> func) {

        this.func = func;
      }

      public ForecastFunction<Zab0> zab0FcastFunc(
        final F<List<TimedValue<BigDecimal>>,
                List<TimedValue<BigDecimal>>> func) {

        return new Zab0ForecastFunction(func);
      }
    }

    private static final class Zab3ForecastFunction 
      extends ForecastFunction<Zab3> {

      private final F<List<TimedValue<BigDecimal>>,
                      List<TimedValue<BigDecimal>>> func;

      private Zab3ForecastFunction(
        final F<List<TimedValue<BigDecimal>>,
                List<TimedValue<BigDecimal>>> func) {

        this.func = func;
      }

      public ForecastFunction<Zab0> zab3FcastFunc(
        final F<List<TimedValue<BigDecimal>>,
                List<TimedValue<BigDecimal>>> func) {

        return new Zab3ForecastFunction(func);
      }
    }
    
    private ForecastFunction() {}
  }

  public static abstract class ForecastValueParser<V> {

    private static final class Zab0ValueParser
      extends ForecastValueParser<Zab0> {

      // TODO handle exceptions
      private BigDecimal parse(String s) {
        return new BigDecimal(s)
      }

      public Zab0ValueParser() {}
    }

    private static final class Zab3ValueParser
      extends ForecastValueParser<Zab3> {

      // TODO handle exceptions
      private BigDecimal parse(String s) {
        return new BigDecimal(s)
      }

      public Zab3ValueParser() {}
    }

    public static final ForecastValueParser<Zab0> zab0ValParser() {
      return new Zab0ValueParser();
    }

    public static final ForecastValueParser<Zab3> zab3ValParser() {
      return new Zab3ValueParser();
    }

    private ForecastValueParser() {}
  }

  public static abstract class ForecastValuePrinter<V> {

    private static final class Zab0ValuePrinter
      extends ForecastValuePrinter<Zab0> {

      private F<BigDecimal,String> print =
        (value)  -> //TODO implement
      }

      public Zab0ValuePrinter() {}
    }

    private static final class Zab3ValuePrinter
      extends ForecastValuePrinter<Zab3> {

      private F<BigDecimal,String> print =
        (value)  -> //TODO implement
      }

      public Zab3ValuePrinter() {}
    }

    public ForecastValuePrinter<Zab0> zab0ValPrinter() {
      return new Zab0ValuePrinter();
    }

    public ForecastValuePrinter<Zab3> zab3ValPrinter() {
      return new Zab3ValuePrinter();
    }

    private ForecastValuePrinter() {}
  }



  // TODO THINK Are these actually not needed? I just need to wrap
  // e.g. String -> BigDecimal in their own parametric wrappers!
  public static abstract class ForecastValue<V> {

    private static final class Zab0Value extends ForecastValue<Zab0> {

      private final BigDecimal value;

      public Zab0Value(final BigDecimal value) {
        this.value = value;
      }
    }

    private static final class Zab3Value extends ForecastValue<Zab3> {

      private final BigDecimal value;

      public Zab3Value(final BigDecimal value) {
        this.value = value;
      }
    }

    private ForecastValue() {}
  }

  public static final ForecastValue<Zab0> zab0val(BigDecimal value) {
    return new ForecastValue.Zab0Value(value);
  }
  public static final ForecastValue<Zab3> zab3val(BigDecimal value) {
    return new ForecastValue.Zab3Value(value);
  }
  

  private ForecastClasses() {}

}
