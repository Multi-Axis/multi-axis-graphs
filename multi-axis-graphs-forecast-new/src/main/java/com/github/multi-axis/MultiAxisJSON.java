package com.github.multi-axis;

public abstract class MultiAxisJSONClasses {

  public interface MultiAxisJSON<V> {

    public String maJson(V value);

  }


  private MultiAxisJSONClasses() {}

}
