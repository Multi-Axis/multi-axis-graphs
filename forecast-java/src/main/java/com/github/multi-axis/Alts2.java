package com.github.multi_axis;


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
 
  private Alts2() {}
}
