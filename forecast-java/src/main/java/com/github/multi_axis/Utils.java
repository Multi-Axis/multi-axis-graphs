package com.github.multi_axis;

import fj.data.Stream;
import fj.data.List;

public abstract class Utils {

  public static final <A> Stream<A> toStream(final List<A> list) {
    final Stream<A> nil = Stream.nil();
    return list.foldRightC( (A a, Stream<A> stream)  -> stream.cons(a),
                            nil
                          ).run(); }


  private Utils() {}
}
