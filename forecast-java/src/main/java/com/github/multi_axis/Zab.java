package com.github.multi_axis;

import fj.data.List;

public final class Zab {

  public static Zab zab(final Type type, final Long start, final Long end,
                        final List<String> filters) {
    return new Zab(type,start,end,filters); }

  public final Type   type;
  public final Bounds bounds;
  public final List<String> filters;

  public static enum Type { zab0, zab3 }

  public static class Bounds {
    public final Long start;
    public final Long end;
    public Bounds(final Long start, final Long end) {
      this.start = start;
      this.end = end; }
  }

  public static Bounds bounds(final Long start, final Long end) {
    return new Bounds(start,end); }

  public Zab( final Type type, final Long start, final Long end,
              final List<String> filters) {
    this.type = type;
    this.bounds = bounds(start,end);
    this.filters = filters; }
}
