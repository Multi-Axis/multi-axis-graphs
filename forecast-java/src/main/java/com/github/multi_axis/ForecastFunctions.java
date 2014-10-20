package com.github.multi_axis;


import java.math.BigDecimal;

import fj.data.Stream;
import fj.data.TreeMap;

import org.joda.time.LocalDate;

import com.github.multi_axis.TimedValue;


import static com.github.multi_axis.TimedValue.timedVal;
import static com.github.multi_axis.Time.date;
import static com.github.multi_axis.Time.dateOrd;


public abstract class ForecastFunctions {

  public static Stream<TimedValue<BigDecimal>>
    dailyMaximums(Stream<TimedValue<BigDecimal>> data) {
      return  data
                .foldLeft(
                  (TreeMap<LocalDate,TimedValue<BigDecimal>>  dmaxs,
                   TimedValue<BigDecimal>                     tval)  ->
                      dmaxs.update( 
                        date(tval.clock),
                        oldtval  -> 
                          timedVal(tval.clock, oldtval.value.max(tval.value)),
                        tval),
                  TreeMap.empty(dateOrd)
                ).values().toStream(); }

  //TODO implement
  //public static F<Long,BigDecimal>
  //  simpleLeastSquares(Stream<TimedValue<BigDecimal>> data) { }
  
  // public static covariance // TODO ...



  private ForecastFunctions() {}
}
