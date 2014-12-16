package com.github.multi_axis;

import fj.data.List;
import fj.data.Stream;

/** Metadata for the 'zab' format. */

public final class Zab {

  /** Return a Zab with the given fields. */
  public static Zab zab(final Type type, final Stream<Long> drawFuture,
                        final List<String> filters) {
    return new Zab(type,drawFuture,filters); }

  /** Type of the number formatting of the numbers in the JSON output.
   *  zab0 -> 4 decimals,
   *  zab3 -> 0 decimals. */
  public final Type               type;

  /** A stream of points in time onto which the result function is to be
   *  plotted, */
  public final Stream<Long>       drawFuture;

  /** A list of names of filters to be used on the data before the actual model
   *  is run. */
  public final List<String>       filters;

  public static enum Type { zab0, zab3 }

  public Zab( final Type type, final Stream<Long> drawFuture,
              final List<String> filters) {
    this.type = type;
    this.drawFuture = drawFuture;
    this.filters = filters; }
}
