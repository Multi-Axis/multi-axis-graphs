package com.github.multi_axis;

import java.math.BigDecimal;

import fj.F;
import fj.data.List;

//TODO Write Matcher interfaces and add runMatch methods.

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

  public static <V> TimedValue<V> timedVal(long clock, V value) {
    return new TimedValue(clock, value);
  }


  private ForecastClasses() {}

}
