package com.github.multi_axis;

import fj.F;
import fj.F2;

public abstract class Alts2<A,B> {

  public interface Alts2Matcher<A,B,R> {
    public R caseAlt1(A a);
    public R caseAlt2(B b);
  }

  public static final <A,B> Alts2<A,B> alt1(A a) { return new Alt1(a); }
  public static final <A,B> Alts2<A,B> alt2(B b) { return new Alt2(b); }


  public abstract <R> R runMatch(Alts2Matcher<A,B,R> m);


  private static final class Alt1<A,B> extends Alts2<A,B> {
    private final A a;

    public final <R> R runMatch(Alts2Matcher<A,B,R> m) {
      return m.caseAlt1(a);
    }
    public Alt1(A a) { this.a = a; }
  }
 
  private static final class Alt2<A,B> extends Alts2<A,B> {
    private final B b;

    public final <R> R runMatch(Alts2Matcher<A,B,R> m) {
      return m.caseAlt2(b);
    }
    public Alt2(B b) { this.b = b; }
  }

  private static final <A,B,C,D> Alts2Matcher<A,B,Alts2<C,D>>
    mapMatcher(F<A,C> fac, F<B,D> fbd) {
      return new Alts2Matcher<A,B,Alts2<C,D>>() {
        public Alts2<C,D> caseAlt1(A a) { return alt1(fac.f(a)); }
        public Alts2<C,D> caseAlt2(B b) { return alt2(fbd.f(b)); }
      };
  }

  public static final <A,B,C,D> F<Alts2<A,B>, Alts2<C,D>>
    map2(F<A,C> fac, F<B,D> fbd) {
      return (altsAB  -> altsAB.runMatch(mapMatcher(fac,fbd)));
  }

  public static final <A,B,C,D> F2<F<A,C>, F<B,D>, F<Alts2<A,B>,Alts2<C,D>>>
    map2() { return (fac, fbd)  -> map2(fac,fbd); }

 
  private Alts2() {}
}
