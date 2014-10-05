package com.github.multi-axis;

//TODO write Matcher interfaces and add runMatch methods.
//TODO write exception handling Validation methods for proper creation of JsonReaders

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


  private ForecastClasses() {}

}
