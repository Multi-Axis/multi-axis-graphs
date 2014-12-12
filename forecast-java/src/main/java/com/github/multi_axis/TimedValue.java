package com.github.multi_axis;


public final class TimedValue<V> {

  public final long clock;
  public final V value;

  public static <V> TimedValue<V> timedVal(long clock, V value) {
    return new TimedValue(clock, value);
  }

  public TimedValue(long clock, V value) {
    this.clock = clock;
    this.value = value;
  }
}
