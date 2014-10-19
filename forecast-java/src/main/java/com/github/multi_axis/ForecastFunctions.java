package com.github.multi_axis;


import java.math.BigDecimal;

import fj.data.List;
import fj.data.TreeMap;

import org.joda.time.LocalDate;

import com.github.multi_axis.TimedValue;


import static org.github.multi_axis.TimedValue.timedVal;
import static org.github.multi_axis.Time.date;
import static org.github.multi_axis.Time.dateOrd;


public abstract class ForecastFunctions {


  public static List<TimedValue<BigDecimal>>
    dailyMaximums(List<TimedValue<BigDecimal>> data) {
      return  data
                .foldLeft(
                  (dmaxs,tval)  ->
                    dmaxs.update( 
                      date(tval.clock),
                      oldtval  -> timedVal( tval.clock, 
                                            max(oldtval.value, tval.value))),
                  TreeMap.empty(dateOrd)
                ).values(); }


  private ForecastFunctions() {}

