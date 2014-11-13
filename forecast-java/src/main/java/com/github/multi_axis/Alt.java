package com.github.multi_axis;

import fj.F;

//  - TODO THINK Recall -- what is the role for this and is it actually
//    necessary?
//    - This vs. plain 'Alt', that is.
//    - That which I am trying to do, could I achieve simply by having
//      properly limited constructors in one unified class?

public abstract class Alt<OTHERS,TAG,VAL> {

  public abstract <R> R 
    match(F<OTHERS,R> othersfun,
          TAG tag,
          F<VAL,R> fun);

  public static <OTHERS,TAG,VAL> Alt<OTHERS,TAG,VAL>
    other(OTHERS others) {
      return new Other<OTHERS,TAG,VAL>(others); }

  public static <OTHERS,TAG,VAL> Alt<OTHERS,TAG,VAL>
    val(TAG tag, VAL val) {
      return new Val<OTHERS,TAG,VAL>(val); }

  private static final class Other<OTHERS,TAG,VAL> 
    extends Alt<OTHERS,TAG,VAL> {

    private OTHERS other;

    public <R> R
      match(F<OTHERS,R> othersfun,
            TAG tag,
            F<VAL,R>    fun) {
        return othersfun.f(other); }

    public Other(OTHERS other) {
      this.other = other; }
  }

  private static final class Val<OTHERS,TAG,VAL>
    extends Alt<OTHERS,TAG,VAL> {

    private VAL val;

    public <R> R
      match(F<OTHERS,R> othersfun,
            TAG tag,
            F<VAL,R>    fun) {
        return fun.f(val); }

    public Val(final VAL val) {
      this.val = val; }
  }


  private Alt() {}
}
