package com.github.multi_axis;

import fj.data.Stream;
import fj.data.List;

import static fj.Ord.longOrd;
import static fj.data.Stream.iterateWhile;

public abstract class Utils {

  public static final <A> Stream<A> toStream(final List<A> list) {
    final Stream<A> nil = Stream.nil();
    return list.foldRightC( (A a, Stream<A> stream)  -> stream.cons(a),
                            nil
                          ).run(); }


  public static final Long daySecs = Long.valueOf(24 * 60 * 60);
  public static final Long weekSecs = Long.valueOf(7 * 24 * 60 * 60);

  public static final <A> Option<A>
    first(final A[] arr) {
      return Array.array(arr).toOption(); }
  

  public static Stream<TimedValue<BigDecimal>>
    plot( final F<BigDecimal,BigDecimal> fun,
          final Long from, final Long to, final Long step) {
      return  
        range(from,to,step)
          .map(x  -> timedVal(x.longValue(),
                              fun.f(BigDecimal.valueOf(x.longValue())))); }

  public static Stream<Long>
    range(final Long from, final Long to, final Long step) {
      return  iterateWhile( (Long i)  -> add(i,step),
                            (Long i)  -> !longOrd.isGreaterThan(i,to),
                            from); }



  private Utils() {}
}
