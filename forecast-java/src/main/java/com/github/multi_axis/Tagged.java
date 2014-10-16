package com.github.multi_axis;

public final class Tagged<T,V> {

  public final T tag;
  public final V val;

  public Tagged(T tag, V val) {
    this.tag = tag;
    this.val = val;
  }

  public static <T,V> Tagged<T,V> tag(T tag, V val) {
    return new Tagged<T,V>(tag,val);
  }
}
