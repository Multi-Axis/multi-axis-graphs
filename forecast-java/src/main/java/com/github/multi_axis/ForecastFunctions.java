package com.github.multi_axis;


import java.math.BigDecimal;

import fj.F;
import fj.P2;
//import fj.data.List;
import fj.data.Option;
import fj.data.Stream;
import fj.data.TreeMap;
import fj.Ord;

import org.joda.time.LocalDate;

import com.github.multi_axis.TimedValue;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;

import static fj.Ord.longOrd;

import static com.github.multi_axis.TimedValue.timedVal;
import static com.github.multi_axis.Time.date;
import static com.github.multi_axis.Time.dateOrd;
import static com.github.multi_axis.Utils.*;


public abstract class ForecastFunctions {


  public static F<Stream<TimedValue<BigDecimal>>,
                  Stream<TimedValue<BigDecimal>>>
    weekOfDailyMaximumsLeastSquares =
      (tvs  -> weekOfDailyMaximumsLeastSquares(tvs));

  //TODO Fix ugly hacks and refactor a bunch of stuff out of here.
  //  - Should return some error rather than empty Stream.
  public static Stream<TimedValue<BigDecimal>>
    weekOfDailyMaximumsLeastSquares(Stream<TimedValue<BigDecimal>> data) {

      final Stream<TimedValue<BigDecimal>>
        dmaxs = dailyMaximums(data);

      final Stream<Long> 
        clocks = dmaxs.map(tv  -> Long.valueOf(tv.clock));

      final Option<Long> firstO =  minimum(longOrd,clocks);
      final Option<Long> lastO  =  maximum(longOrd,clocks);

      final Stream<BigDecimal>
        outTimes =
          firstO.bind( first  ->
          lastO.map( last  ->
            range(first, add(last,weekSecs), daySecs)
              .map(x  -> BigDecimal.valueOf(x.longValue())))
          ).orSome(Stream.<BigDecimal>nil());

      //TODO FIXME Ugly bad place for these checks.
      if (length(dmaxs).compareTo(ONE) > 0) {
        if (variance(dmaxs.map(tv  -> BigDecimal.valueOf(tv.clock)))
            .compareTo(ZERO) > 0) {

              final F<BigDecimal,BigDecimal> 
                prediction = timedValsLeastSquares(dmaxs);

              return 
                outTimes.map(t  -> timedVal(t.longValue(),
                                            prediction.f(t))); }

        else { return Stream.<TimedValue<BigDecimal>>nil(); } } //FIXME hack.
      else { return Stream.<TimedValue<BigDecimal>>nil(); } 
  }

      

  public static Long
    add(Long a, Long b) {
      return Long.valueOf(a.longValue() + b.longValue()); }

  public static <A> Option<A> 
    maximum(Ord<A> ord, Stream<A> as) {
      return as.foldLeft(
                  (Option<A> maxO, A a)  ->
                    maxO.map( max  -> ord.max.f(max).f(a)),
                  as.toOption()); }

  public static <A> Option<A> 
    minimum(Ord<A> ord, Stream<A> as) {
      return as.foldLeft(
                  (Option<A> minO, A a)  ->
                    minO.map( min  -> ord.min.f(min).f(a)),
                  as.toOption()); }

  
  //TODO FIXME This will fail given insufficient / insufficiently variant data!
  //  - Though, FWIW, for daily maxes, just checking for length should suffice
  //    since there won't be repeats of clock.
  public static F<BigDecimal, BigDecimal>
    timedValsLeastSquares(Stream<TimedValue<BigDecimal>> data) {

      final Stream<BigDecimal>
        times = data.map(tv  -> BigDecimal.valueOf(tv.clock));

      final Stream<BigDecimal>
        vals = data.map(tv  -> tv.value);

      return simpleLeastSquares(times,vals); }

  //TODO THINK these could perhaps use some code reuse?
  public static Stream<TimedValue<BigDecimal>>
    dailyMaximums(Stream<TimedValue<BigDecimal>> data) {
      return
        toStream(
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
            ).values()); }

  public static Stream<TimedValue<BigDecimal>>
    dailyMinimums(Stream<TimedValue<BigDecimal>> data) {
      return
        toStream(
          data
            .foldLeft(
              (TreeMap<LocalDate,TimedValue<BigDecimal>>  dmins,
               TimedValue<BigDecimal>                     tval)  ->
                  dmins.update( 
                    date(tval.clock),
                    oldtval  -> 
                      timedVal(tval.clock, oldtval.value.min(tval.value)),
                    tval),
              TreeMap.empty(dateOrd)
            ).values()); }

  public static Stream<TimedValue<BigDecimal>>
    dailyAverages(Stream<TimedValue<BigDecimal>> data) {

      final TreeMap<LocalDate
      return
        toStream(
          data
            .foldLeft(
              (TreeMap< LocalDate,
                        P2<Long,Stream<BigDecimal>>>  dvals,
               TimedValue<BigDecimal>                 tval)  ->
                  dvals.update( 
                    date(tval.clock),
                    cAndVs  ->  p(cAndVs._1(),
                                  cAndVs._2().cons(tval.value)),
                    p(epochSecs(date(tval.clock)),list(tval.value))),
              TreeMap.empty(dateOrd)
            ).map(cAndVs  -> timedVal(cAndVs._1(),mean(cAndVs._2())))
            .values()); }

  public static F<BigDecimal,BigDecimal>
    simpleLeastSquares(Stream<BigDecimal> as, Stream<BigDecimal> bs) {
      
      //TODO FIXME THINK Better number type for this kind of arithmetic?
      final BigDecimal
        slope = covariance(as,bs).divide(variance(as),20,HALF_EVEN);

      final BigDecimal
        intercept = mean(bs).subtract(slope.multiply(mean(as)));

      return (x  -> intercept.add(slope.multiply(x))); }


  public static BigDecimal
    covariance(Stream<BigDecimal> as, Stream<BigDecimal> bs) {

      final BigDecimal amean = mean(as);
      final BigDecimal bmean = mean(bs);

      final Stream<P2<BigDecimal,BigDecimal>> asbs = as.zip(bs);

      final BigDecimal n = length(asbs);

      //TODO FIXME THINK Better number type for this kind of arithmetic?
      return  sum(
                asbs.map(ab  -> 
                  ab._1().subtract(amean).multiply(
                  ab._2().subtract(bmean)))
              ).divide(
                  n.subtract(ONE),20,HALF_EVEN);}

  public static BigDecimal variance(Stream<BigDecimal> as) {
    return covariance(as,as); }

  //TODO FIXME THINK Better number type for this kind of arithmetic?
  public static BigDecimal
    mean(Stream<BigDecimal> as) {
      return sum(as).divide(length(as),20,HALF_EVEN); }

  public static BigDecimal
    sum(Stream<BigDecimal> as) {
      return as.foldLeft( (BigDecimal acc, BigDecimal a)  ->
                            acc.add(a),
                          ZERO); }

  public static <A> BigDecimal
    length(Stream<A> as) {
      return BigDecimal.valueOf(as.length()); }


  private ForecastFunctions() {}
}
