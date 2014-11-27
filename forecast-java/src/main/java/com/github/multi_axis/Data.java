package com.github.multi_axis;

public final class Data<META,DATA> {

  public final META meta;
  public final DATA data;

  public static <META,DATA> Data<META,DATA>
    data(final META meta, final DATA data) {
      return new Data<META,DATA>(meta,data); }

  public Data(final META meta, final DATA data) {
    this.meta = meta;
    this.data = data; }
}


