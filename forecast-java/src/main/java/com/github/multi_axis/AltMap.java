package com.github.multi_axis;

import fj.F;

public final class AltMap<A,B> {

  public final F<A,B> fun;

  public <INTAG,IN,OUTTAG,OUT> AltMap<Alt<A,INTAG,IN>, Alt<B,OUTTAG,OUT>>
    add(final INTAG intag, final OUTTAG outtag, final F<IN,OUT> f) {
      return altMap(this,intag,outtag,f); } 
  
  public static <INS,INTAG,IN,OUTS,OUTTAG,OUT> AltMap<Alt<INS,INTAG,IN>,
                                                      Alt<OUTS,OUTTAG,OUT>>
    altMap( final AltMap<INS,OUTS> othersmap,
            final INTAG intag,
            final OUTTAG outtag,
            final F<IN,OUT> fun) {
      return makeAltMap(othersmap.fun, intag, outtag, fun); }

  public static <INTAG,IN,OUTTAG,OUT> AltMap< Alt<Nothing,INTAG,IN>,
                                              Alt<Nothing,OUTTAG,OUT>>
    altMap(final INTAG intag, final OUTTAG outtag, final F<IN,OUT> fun) {
      return makeAltMap(Nothing.<Nothing>nothingFunction(),
                        intag, outtag, fun); }

  private static <INS,INTAG,IN,OUTS,OUTTAG,OUT> AltMap< Alt<INS,INTAG,IN>,
                                                        Alt<OUTS,OUTTAG,OUT>>
    makeAltMap( final F<INS,OUTS> othersfun,
                final INTAG intag,
                final OUTTAG outtag,
                final F<IN,OUT> fun) {
      return new AltMap<Alt<INS,INTAG,IN>,Alt<OUTS,OUTTAG,OUT>>(alt  ->
          alt.match(
            others  -> Alt.other(othersfun.f(others)),
            intag,
            in     -> Alt.val(outtag, fun.f(in)))); }


  private AltMap(final F<A,B> fun) {
    this.fun = fun; }
}


