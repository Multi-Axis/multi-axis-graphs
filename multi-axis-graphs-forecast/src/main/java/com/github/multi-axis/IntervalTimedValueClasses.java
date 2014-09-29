package com.github.multi-axis;

import static fj.Ordering;
import static fj.Ord;
import static fj.data.Validation;

public abstract class IntervalTimedValueClasses {

  public static final class IntervalTimedValue<T,V> {
  
    public final T from;
    public final T to;
    public final V val;

    public static <T,V> IntervalTimedValue<T,V> itv(T from, T to, V val) {
      return new IntervalTimedValue<T,V>(from,to,val);
    }
  
    public interface ItvMatch<T,V,R> {
      public R caseItv(T from, T to, V val);
    }
  
    public <R> R runMatch(ItvMatch<T,V,R> m) {
      return m.caseItv(from,to,val);
    }

    public IntervalTimedValue(T from, T to, V val) {
      this.from = from;
      this.to   = to;
      this.val  = val;
    }
  }

  public static final class NegativeIntervalError<T,V> {

    public final IntervalTimedValue<T,V> negItv;

    public static <T,V> NegativeIntervalError<T,V> 
      negIntervalError(IntervalTimedValue<T,V> negItv) {
        return new NegativeIntervalError<T,V>(negItv);
    }
  
    public interface NegIntervalErrorMatch<T,V,R> {
      public R caseIntervalError(IntervalTimedValue<T,V> negItv);
    }
  
    public <R> R runMatch(NegIntervalErrorMatch<T,V,R> m) {
      return m.caseIntervalError(negItv);
    }

    public IntervalError(IntervalTimedValue<T,V> negItv) {
      this.negItv = negItv;
    }
  }

  public static final <T,V> 
    Validation<
      NegativeIntervalError<T,V>,
      IntervalTimedValue<T,V>>

    validateItv(Ord<T> ordT, T from, T to, V value) {

      if (ordT.isGreaterThan(from,to) {
        return fail(negIntervalError(itv(from,to,value)));
      } else {
        return success(itv(from,to,value));
      }
  }


  private IntervalTimedValueClasses() {}

}
