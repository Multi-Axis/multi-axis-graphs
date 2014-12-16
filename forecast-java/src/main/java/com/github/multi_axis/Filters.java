package com.github.multi_axis;

import java.math.BigDecimal;

import fj.F;
import fj.data.List;
import fj.data.Stream;
import fj.data.TreeMap;
import fj.data.Validation;
import fj.data.Option;

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

/** Functions for extracting filters from metadata. At the moment only deals
 *  with 'Zab'-type metadata. */

public abstract class Filters {

  // The type inference has been very ornery here for some reason.
  // Excuse the verbosity.

  public static TreeMap<String, F<Stream<TimedValue<BigDecimal>>,
                                  Stream<TimedValue<BigDecimal>>>>
    filters = TreeMap.<String,F<Stream<TimedValue<BigDecimal>>,
                                Stream<TimedValue<BigDecimal>>>>
              empty(stringOrd)
                .set("DailyMax", dailyMaximums)
                .set("DailyMin", dailyMinimums)
                .set("DailyAvg", dailyAverages);

  public static Validation<Errors,List<F< Stream<TimedValue<BigDecimal>>,
                                          Stream<TimedValue<BigDecimal>>>>>
    filtersFromZab(final Zab zab) {

      return        
        zab.filters.map( name  ->
          filters.get(name)
            .<Validation<Errors,F<Stream<TimedValue<BigDecimal>>,
                                  Stream<TimedValue<BigDecimal>>>>>
            map( f  -> success(f))
            .orSome(fail(noSuchFilter(name))))
        .foldLeft(
          (vfuns,vfun)  -> 
            vfuns.bind( funs  -> 
            vfun.map( fun  -> funs.snoc(fun))),
          success(List.<F<Stream<TimedValue<BigDecimal>>,
                          Stream<TimedValue<BigDecimal>>>>nil())); }

  public static F<Zab,
                  Validation< Errors,
                              List<F< Stream<TimedValue<BigDecimal>>,
                                      Stream<TimedValue<BigDecimal>>>>>>
    filtersFromZab = zab  -> filtersFromZab(zab);
              

  private Filters() {}
}
