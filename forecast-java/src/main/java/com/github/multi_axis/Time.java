package com.github.multi_axis;


import org.joda.time.LocalDate;

import fj.Ord;

import static fj.Ordering.LT;
import static fj.Ordering.EQ;
import static fj.Ordering.GT;
import static fj.Ord.ord;


public abstract class Time {

  public static LocalDate 
    date(long epochseconds) {
      return new LocalDate(epochseconds * 1000); }

  public static final Ord<LocalDate> 
    dateOrd = 
      ord(a  ->  (b  -> ( a.compareTo(b) <  0 ) ? LT : (
                        ( a.compareTo(b) == 0 ) ? EQ :
                                                  GT )));

  private Time() {}
}
