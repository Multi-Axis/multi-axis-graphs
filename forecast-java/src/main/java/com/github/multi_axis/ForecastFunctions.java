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

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;

import static fj.Ord.longOrd;
import static fj.data.Stream.stream;
import static fj.P.p;
import static fj.Option.none;
import static fj.Option.some;

import static com.github.multi_axis.TimedValue.timedVal;
import static com.github.multi_axis.Time.date;
import static com.github.multi_axis.Time.epochSecs;
import static com.github.multi_axis.Time.dateOrd;
import static com.github.multi_axis.Utils.*;


public abstract class ForecastFunctions {

  //TODO FIXME Add Javadocs for these.
  public static <A> Option<A> 
    maximum(final Ord<A> ord, final Stream<A> as) {
      return as.foldLeft(
                  (Option<A> maxO, A a)  ->
                    maxO.map( max  -> ord.max.f(max).f(a)),
                  as.toOption()); }

  public static <A> Option<A> 
    minimum(final Ord<A> ord, final Stream<A> as) {
      return as.foldLeft(
                  (Option<A> minO, A a)  ->
                    minO.map( min  -> ord.min.f(min).f(a)),
                  as.toOption()); }

  
  public static F<Stream<TimedValue<BigDecimal>>,
                  Option<F<BigDecimal,BigDecimal>>>
    timedValsLeastSquares =
      data  -> timedValsLeastSquares(data);
      
  //TODO FIXME Add Javadocs for these.
  public static Option<F<BigDecimal, BigDecimal>>
    timedValsLeastSquares(final Stream<TimedValue<BigDecimal>> data) {

      final Stream<BigDecimal>
        times = data.map(tv  -> BigDecimal.valueOf(tv.clock));

      final Stream<BigDecimal>
        vals = data.map(tv  -> tv.value);

      return simpleLeastSquares(times,vals); }

  //TODO THINK these could perhaps use some code reuse?
  //TODO FIXME Add Javadocs for these.
  public static Stream<TimedValue<BigDecimal>>
    dailyMaximums(final Stream<TimedValue<BigDecimal>> data) {
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

  public static F<Stream<TimedValue<BigDecimal>>,
                  Stream<TimedValue<BigDecimal>>>
    dailyMaximums = data  -> dailyMaximums(data);

  public static Stream<TimedValue<BigDecimal>>
    dailyMinimums(final Stream<TimedValue<BigDecimal>> data) {
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

  public static F<Stream<TimedValue<BigDecimal>>,
                  Stream<TimedValue<BigDecimal>>>
    dailyMinimums = data  -> dailyMinimums(data);

  public static Stream<TimedValue<BigDecimal>>
    dailyAverages(final Stream<TimedValue<BigDecimal>> data) {

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
                    p(epochSecs(date(tval.clock)),stream(tval.value))),
              TreeMap.empty(dateOrd) // some() below is ugly but should work.
            ).map(cAndVs  -> timedVal(cAndVs._1(),mean(cAndVs._2().some())))
            .values()); }

  public static F<Stream<TimedValue<BigDecimal>>,
                  Stream<TimedValue<BigDecimal>>>
    dailyAverages = data  -> dailyAverages(data);

  /** Return the linear function fitted into the data with least squares error.
   * */
  public static Option<F<BigDecimal,BigDecimal>>
    simpleLeastSquares( final Stream<BigDecimal> as,
                        final Stream<BigDecimal> bs) {
      
      //TODO FIXME THINK Better number type for this kind of arithmetic?
      final Option<BigDecimal>
        slopeO = 
          covariance(as,bs).bind( cov  ->
          variance(as).bind(      var  ->
          var.compareTo(ZERO != 0)
            ? some(cov.divide(var,20,HALF_EVEN))
            : none()));

      final BigDecimal
        intercept = mean(bs).subtract(slope.multiply(mean(as)));

      return (x  -> intercept.add(slope.multiply(x))); }


  /** Return the covariance of the values in the two streams. Values are
   *  paired index by index. In case of uneven length, the leftovers at the
   *  tail end of the longer stream are ignored. */
  public static Option<BigDecimal>
    covariance(final Stream<BigDecimal> as, final Stream<BigDecimal> bs) {

      final Option<BigDecimal> ameanO = mean(as);
      final Option<BigDecimal> bmeanO = mean(bs);

      final Stream<P2<BigDecimal,BigDecimal>> asbs = as.zip(bs);

      final BigDecimal n = length(asbs);

      //TODO FIXME THINK Better number type for this kind of arithmetic?
      return
        mean(as).bind(  amean  ->
        mean(bs).bind(  bmean  ->
        asbs.isNotEmpty()
          ? some( sum(
                    asbs.map(ab  -> 
                      ab._1().subtract(amean).multiply(
                      ab._2().subtract(bmean)))
                  ).divide(
                    n.subtract(ONE),20,HALF_EVEN))
          : none();}

  /** Return the variance of the values in the stream. */
  public static BigDecimal variance(Stream<BigDecimal> as) {
    return covariance(as,as); }

  //TODO FIXME THINK Better number type for this kind of arithmetic?
  /** Return the mean of the values in the stream. */
  public static Option<BigDecimal>
    mean(final Stream<BigDecimal> as) {
      return  as.isNotEmpty()
                ? some(sum(as).divide(length(as),20,HALF_EVEN))
                : none(); }

  /** Return the sum of the numbers in the stream. */
  public static Option<BigDecimal>
    sum(final Stream<BigDecimal> as) {
      return  as.isNotEmpty()
                ? some(as.foldLeft( (BigDecimal acc, BigDecimal a)  ->
                                      acc.add(a),
                                    ZERO))
                : none(); }

  /** Return the length of a stream. */
  public static <A> BigDecimal
    length(final Stream<A> as) {
      return BigDecimal.valueOf(as.length()); }


  private ForecastFunctions() {}
}
