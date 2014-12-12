package com.github.multi_axis;

import fj.data.List;

public final class Zab {

  public static Zab zab(final Type type, final Stream<Long> drawFuture,
                        final List<String> filters) {
    return new Zab(type,drawFuture,filters); }

  public final Type               type;
  public final Stream<Long> drawFuture;
  public final List<String>       filters;

  public static enum Type { zab0, zab3 }

  public Zab( final Type type, final Stream<Long> drawFuture,
              final List<String> filters) {
    this.type = type;
    this.drawFuture = drawFuture;
    this.filters = filters; }
}
