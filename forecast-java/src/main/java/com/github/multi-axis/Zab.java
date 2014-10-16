package com.github.multi_axis;



public abstract class Zab {

  public interface ZabMatcher<R> {
    public R caseZab0();
    public R caseZab3();
  }

  public abstract <R> R runMatch(ZabMatcher<R> m);
  
  public static final class Zab0 extends Zab { 
    public <R> R runMatch(ZabMatcher<R> m) { return m.caseZab0(); }
    public Zab0() {}
  }
  public static final class Zab3 extends Zab {
    public <R> R runMatch(ZabMatcher<R> m) { return m.caseZab3(); }
    public Zab3() {}
  }

  public static final Zab0 zab0 = new Zab0();
  public static final Zab3 zab3 = new Zab3();

  private Zab() {}
}
