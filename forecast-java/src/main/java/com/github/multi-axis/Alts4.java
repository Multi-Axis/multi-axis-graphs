
package com.github.multi_axis;


public abstract class Alts4<A,B,C,D> {

  public interface Alts4Matcher<A,B,C,D,R> {
    public R caseAlt1(A a);
    public R caseAlt2(B b);
    public R caseAlt3(C c);
    public R caseAlt4(D d);
  }

  public static final <A,B,C,D> Alts4<A,B,C,D> alt1(A a) { return new Alt1(a); }
  public static final <A,B,C,D> Alts4<A,B,C,D> alt2(B b) { return new Alt2(b); }
  public static final <A,B,C,D> Alts4<A,B,C,D> alt3(C c) { return new Alt3(c); }
  public static final <A,B,C,D> Alts4<A,B,C,D> alt4(D d) { return new Alt4(d); }


  public abstract <R> R runMatch(Alts4Matcher<A,B,C,D,R> m);


  private static final class Alt1<A,B,C,D> extends Alts4<A,B,C,D> {
    private final A a;

    public final <R> R runMatch(Alts4Matcher<A,B,C,D,R> m) {
      return m.caseAlt1(a);
    }
    public Alt1(A a) { this.a = a; }
  }
 
  private static final class Alt2<A,B,C,D> extends Alts4<A,B,C,D> {
    private final B b;

    public final <R> R runMatch(Alts4Matcher<A,B,C,D,R> m) {
      return m.caseAlt2(b);
    }
    public Alt2(B b) { this.b = b; }
  }
 
  private static final class Alt3<A,B,C,D> extends Alts4<A,B,C,D> {
    private final C c;

    public final <R> R runMatch(Alts4Matcher<A,B,C,D,R> m) {
      return m.caseAlt3(c);
    }
    public Alt3(C c) { this.c = c; }
  }
 
  private static final class Alt4<A,B,C,D> extends Alts4<A,B,C,D> {
    private final D d;

    public final <R> R runMatch(Alts4Matcher<A,B,C,D,R> m) {
      return m.caseAlt4(d);
    }
    public Alt4(D d) { this.d = d; }
  }
 
  private Alts4() {}
}
