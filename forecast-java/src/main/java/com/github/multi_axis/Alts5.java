

package com.github.multi_axis;


public abstract class Alts5<A,B,C,D,E> {

  public interface Alts5Matcher<A,B,C,D,E,R> {
    public R caseAlt1(A a);
    public R caseAlt2(B b);
    public R caseAlt3(C c);
    public R caseAlt4(D d);
    public R caseAlt5(E f);
  }

  public static final <A,B,C,D,E> Alts5<A,B,C,D,E> alt1(A a) { return new Alt1(a); }
  public static final <A,B,C,D,E> Alts5<A,B,C,D,E> alt2(B b) { return new Alt2(b); }
  public static final <A,B,C,D,E> Alts5<A,B,C,D,E> alt3(C c) { return new Alt3(c); }
  public static final <A,B,C,D,E> Alts5<A,B,C,D,E> alt4(D d) { return new Alt4(d); }
  public static final <A,B,C,D,E> Alts5<A,B,C,D,E> alt5(E e) { return new Alt5(e); }


  public abstract <R> R runMatch(Alts5Matcher<A,B,C,D,E,R> m);


  private static final class Alt1<A,B,C,D,E> extends Alts5<A,B,C,D,E> {
    private final A a;

    public final <R> R runMatch(Alts5Matcher<A,B,C,D,E,R> m) {
      return m.caseAlt1(a);
    }
    public Alt1(A a) { this.a = a; }
  }
 
  private static final class Alt2<A,B,C,D,E> extends Alts5<A,B,C,D,E> {
    private final B b;

    public final <R> R runMatch(Alts5Matcher<A,B,C,D,E,R> m) {
      return m.caseAlt2(b);
    }
    public Alt2(B b) { this.b = b; }
  }
 
  private static final class Alt3<A,B,C,D,E> extends Alts5<A,B,C,D,E> {
    private final C c;

    public final <R> R runMatch(Alts5Matcher<A,B,C,D,E,R> m) {
      return m.caseAlt3(c);
    }
    public Alt3(C c) { this.c = c; }
  }
 
  private static final class Alt4<A,B,C,D,E> extends Alts5<A,B,C,D,E> {
    private final D d;

    public final <R> R runMatch(Alts5Matcher<A,B,C,D,E,R> m) {
      return m.caseAlt4(d);
    }
    public Alt4(D d) { this.d = d; }
  }
 
  private static final class Alt5<A,B,C,D,E> extends Alts5<A,B,C,D,E> {
    private final E e;

    public final <R> R runMatch(Alts5Matcher<A,B,C,D,E,R> m) {
      return m.caseAlt5(e);
    }
    public Alt5(E e) { this.e = e; }
  }
 
  private Alts5() {}
}
