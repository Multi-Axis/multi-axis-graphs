package com.github.multi_axis;

public abstract class Nothing {

  public static <R> F<Nothing,R> nothingFunction() {
    return new F<Nothing,R>() {
      public f(Nothing nothing) { throw new NothingAppliedException(); } } }

  private Nothing() {}

}
