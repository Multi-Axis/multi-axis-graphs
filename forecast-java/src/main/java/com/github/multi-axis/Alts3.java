package com.github.multi_axis;


public abstract class Alts3<A,B,C> {

  public interface Alts3Matcher<A,B,C,R> {
    public R caseAlt1(A a);
    public R caseAlt2(B b);
    public R caseAlt3(C c);
  }

  public static final <A,B,C> Alts3<A,B,C> alt1(A a) { return new Alt1(a); }
  public static final <A,B,C> Alts3<A,B,C> alt2(B b) { return new Alt2(b); }
  public static final <A,B,C> Alts3<A,B,C> alt3(C c) { return new Alt3(c); }


  public abstract <R> R runMatch(Alts3Matcher<A,B,C,R> m);


  private static final class Alt1<A,B,C> extends Alts3<A,B,C> {
    private final A a;

    public final <R> R runMatch(Alts3Matcher<A,B,C,R> m) {
      return m.caseAlt1(a);
    }
    public Alt1(A a) { this.a = a; }
  }
 
  private static final class Alt2<A,B,C> extends Alts3<A,B,C> {
    private final B b;

    public final <R> R runMatch(Alts3Matcher<A,B,C,R> m) {
      return m.caseAlt2(b);
    }
    public Alt2(B b) { this.b = b; }
  }
 
  private static final class Alt3<A,B,C> extends Alts3<A,B,C> {
    private final C c;

    public final <R> R runMatch(Alts3Matcher<A,B,C,R> m) {
      return m.caseAlt3(c);
    }
    public Alt3(C c) { this.c = c; }
  }
 
  private Alts3() {}
}
