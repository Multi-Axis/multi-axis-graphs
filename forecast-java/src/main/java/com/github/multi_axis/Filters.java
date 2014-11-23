package com.github.multi_axis;

import java.math.BigDecimal;

import fj.F;
import fj.data.Stream;
import fj.data.TreeMap;
import fj.data.Validation;

import com.github.multi_axis.TimedValue;
import com.github.multi_axis.Zab;
import com.github.multi_axis.Errors;

import static fj.Ord.stringOrd;
import static fj.data.Validation.success;
import static fj.data.Validation.fail;

import static com.github.multi_axis.Errors.*;
import static com.github.multi_axis.ForecastFunctions.dailyMaximums;
import static com.github.multi_axis.ForecastFunctions.dailyMinimums;
import static com.github.multi_axis.ForecastFunctions.dailyAverages;

public abstract class Filters {

  public static TreeMap<String, F<Stream<TimedValue<BigDecimal>,
                                  Stream<TimedValue<BigDecimal>>>
    filters = TreeMap.empty(stringOrd)
                .set("DailyMax", data  -> dailyMaximums(data))
                .set("DailyMin", data  -> dailyMinimums(data))
                .set("DailyAvg", data  -> dailyAverages(data))

  public static F<Zab,
                  Validation< Errors,
                              List<F< Stream<TimedValue<BigDecimal>>,
                                      Stream<TimedValue<BigDecimal>>>>>>
    filtersFromZab = 
      zab  -> zab.filters
                .foldLeft(    (funsV,name)  -> 
                  funsV.bind( funs  ->
                    filters.get(name).map(fun  -> success(funs.snoc(fun)))
                                      .orSome(fail(noSuchFilter(name)))),
                  success(List.<F<Stream<TimedValue<BigDecimal>,
                                F<Stream<TimedValue<BigDecimal>>nil()));
      

  private Filters() {}
}
