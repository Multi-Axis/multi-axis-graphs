package com.github.multi_axis;


import java.math.BigDecimal;

import fj.P2;
import fj.data.Stream;
import fj.data.TreeMap;

import org.joda.time.LocalDate;

import com.github.multi_axis.TimedValue;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;

import static com.github.multi_axis.TimedValue.timedVal;
import static com.github.multi_axis.Time.date;
import static com.github.multi_axis.Time.dateOrd;


public abstract class ForecastFunctions {

  public static Stream<TimedValue<BigDecimal>
    weekOfDailyMaximumsLeastSquares(Stream<TimedValue<BigDecimal>> data) {

      final long end = //TODO write...

  public static F<BigDecimal, BigDecimal>
    dailyMaximumsLeastSquares(Stream<TimedValue<BigDecimal>> data) {
      
      final Stream<TimedValue<BigDecimal>> 
        maxs = dailyMaximums(data);

      final Stream<BigDecimal>
        times = maxs.map(tv  -> BigDecimal.valueOf(tv.clock));

      final Stream<BigDecimal>
        vals = maxs.map(tv  -> tv.value);

      return simpleLeastSquares(times,vals); }

  public static Stream<TimedValue<BigDecimal>>
    dailyMaximums(Stream<TimedValue<BigDecimal>> data) {
      return
        data
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

  public static F<BigDecimal,BigDecimal>
    simpleLeastSquares(Stream<BigDecimal> as, Stream<BigDecimal> bs) {
      
      final BigDecimal
        slope = covariance(as,bs).divide(variance(as));

      final BigDecimal
        intercept = mean(as).subtract(slope.multiply(mean(bs)));

      return (x  -> intercept.add(slope.multiply(x))); }


  //TODO FIXME I'm not sure this is right!
  //  - Rewrite from proper defn of covariance.
  public static BigDecimal
    covariance(Stream<BigDecimal> as, Stream<BigDecimal> bs) {

      final BigDecimal amean = mean(a);
      final BigDecimal bmean = mean(b);

      final P2<BigDecimal,BigDecimal> asbs = as.zip(bs);

      final BigDecimal n = asbs.length()

      return  sum(
                asbs.map(ab  -> 
                  ab_1().subtract(amean).multiply(
                  ab_2().subtract(bmean)))
              ).divide(
                  n.subtract(ONE));}

  public static BigDecimal variance(Stream<BigDecimal> as) {
    return covariance(as,as); }

  public static BigDecimal
    mean(Stream<BigDecimal> as) {
      return sum(as).divide(length(as)); }

  public static BigDecimal
    sum(Stream<BigDecimal> as) {
      return as.foldLeft( (BigDecimal acc, BigDecimal a)  ->
                            acc.add(a),
                          ZERO); }

  public static BigDecimal
    length(Stream<BigDecimal> as) {
      return valueOf(as.length()); }


  private ForecastFunctions() {}
}
