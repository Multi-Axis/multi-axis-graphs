package com.github.multi_axis;

import fj.F;

import com.github.multi_axis.NothingAppliedException;

public abstract class Nothing {

  public static <R> F<Nothing,R> nothingFunction() {
    return new F<Nothing,R>() {
      public R f(Nothing nothing) { throw new NothingAppliedException(); } } }

  private Nothing() {}

}
