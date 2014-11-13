package com.github.multi_axis;

import fj.F;

public final class AltFun<A,R> {

  // private?
  public final F<A,R> fun;

  public <TAG,VAL> AltFun<Alt<A,TAG,VAL>, R>
    addCase(final TAG tag, final F<VAL,R> f) {
      return altFun(this,tag,f); }

  public static <OTHERS,TAG,VAL,R> AltFun<Alt<OTHERS,TAG,VAL>, R>
    altFun(final AltFun<OTHERS,R> othersfun, final TAG tag, final F<VAL,R> fun) {
      return makeAltFun(othersfun.fun, tag, fun); }

  public static <TAG,VAL,R> AltFun<Alt<Nothing,TAG,VAL>, R> //FIXME can't work.
    altFun(final TAG tag, final F<VAL,R> fun) {
      return makeAltFun(Nothing.<R>nothingFunction(), tag, fun); }

  private static <OTHERS,TAG,VAL,R> AltFun<Alt<OTHERS,TAG,VAL>,R>
    makeAltFun(final F<OTHERS,R> othersfun, final TAG tag, final F<VAL,R> fun) {
      return new  AltFun<Alt<OTHERS,TAG,VAL>,R>(alt  ->
                    alt.match(
                      others   -> othersfun.f(others),
                      tag,
                      val      -> fun.f(val))); }

  private AltFun(final F<A,R> fun) {
    this.fun = fun; }
}
