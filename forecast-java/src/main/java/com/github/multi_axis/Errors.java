package com.github.multi_axis;

import java.math.BigDecimal;
import java.lang.ClassCastException;
import javax.json.stream.JsonParsingException;
import javax.json.JsonException;


public abstract class Errors {

  public interface ErrorMatcher<R> {
    public R caseMiscJsonError(JsonException e);
    public R caseJsonParsingError(JsonParsingException e);
    public R caseNonNumberInArray(ClassCastException e);
    public R caseNonStringInArray(ClassCastException e);
    public R caseNotJsonArray(ClassCastException e);
    public R caseNotJsonString(ClassCastException e);
    public R caseNotJsonNumber(ClassCastException e);
    public R caseNotJsonObject(ClassCastException e);
    public R caseBadZabType();
    public R caseValueTypeNotNumber(ClassCastException e);
    public R caseNotEnoughData();
    public R caseNoJsonField(String name);
    public R caseNoSuchForecastProcess(String name);
    public R caseBadBounds();
    public R caseNoSuchFilter(String name);
  }

  public static final Errors miscJsonError(final JsonException e) {
    return new MiscJsonError(e); }

  public static final Errors jsonParsingError(final JsonParsingException e) {
    return new JsonParsingError(e); }

  public static final Errors nonNumberInArray(final ClassCastException e) {
    return new NonNumberInArray(e); }

  public static final Errors nonStringInArray(final ClassCastException e) {
    return new NonStringInArray(e); }

  public static final Errors notJsonArray(final ClassCastException e) {
    return new NotJsonArray(e); }

  public static final Errors notJsonString(final ClassCastException e) {
    return new NotJsonString(e); }

  public static final Errors notJsonNumber(final ClassCastException e) {
    return new NotJsonNumber(e); }

  public static final Errors notJsonObject(final ClassCastException e) {
    return new NotJsonObject(e); }

  public static final Errors badZabType() {
    return new BadZabType(); }

  public static final Errors valueTypeNotNumber(final ClassCastException e) {
    return new ValueTypeNotNumber(e); }

  public static final Errors notEnoughData() {
    return new NotEnoughData(); }

  public static final Errors noJsonField(final String name) {
    return new NoJsonField(name); }

  public static final Errors noSuchForecastProcess(final String name) {
    return new NoSuchForecastProcess(name); }

  public static final Errors badBounds() {
    return new BadBounds(); }

  public static final Errors noSuchFilter(final String name) {
    return new NoSuchFilter(name); }


  public abstract <R> R runMatch(ErrorMatcher<R> m);


  private static final class MiscJsonError extends Errors {

    private final JsonException e;

    public MiscJsonError(final JsonException e) { this.e = e; }

    public final <R> R runMatch(final ErrorMatcher<R> m) {
      return m.caseMiscJsonError(e); }
  }

  private static final class JsonParsingError extends Errors {

    private final JsonParsingException e;

    public JsonParsingError(final JsonParsingException e) { this.e = e; }

    public final <R> R runMatch(final ErrorMatcher<R> m) {
      return m.caseJsonParsingError(e); }
  }

  private static final class NonNumberInArray extends Errors {

    private final ClassCastException e;

    public NonNumberInArray(final ClassCastException e) { this.e = e; }

    public final <R> R runMatch(final ErrorMatcher<R> m) {
      return m.caseNonNumberInArray(e); }
  }

  private static final class NonStringInArray extends Errors {

    private final ClassCastException e;

    public NonStringInArray(final ClassCastException e) {this.e = e; }

    public final <R> R runMatch(final ErrorMatcher<R> m) {
      return m.caseNonStringInArray(e); }
  }

  private static final class NotJsonArray extends Errors {

    private final ClassCastException e;

    public NotJsonArray(final ClassCastException e) { this.e = e; }

    public final <R> R runMatch(final ErrorMatcher<R> m) {
      return m.caseNotJsonArray(e); }
  }

  private static final class NotJsonString extends Errors {

    private final ClassCastException e;

    public NotJsonString(final ClassCastException e) { this.e = e; }

    public final <R> R runMatch(final ErrorMatcher<R> m) {
      return m.caseNotJsonString(e); }
  }

  private static final class NotJsonNumber extends Errors {

    private final ClassCastException e;

    public NotJsonNumber(final ClassCastException e) { this.e = e; }

    public final <R> R runMatch(final ErrorMatcher<R> m) {
      return m.caseNotJsonNumber(e); }
  }

  private static final class NotJsonObject extends Errors {

    private final ClassCastException e;

    public NotJsonObject(final ClassCastException e) { this.e = e; }

    public final <R> R runMatch(final ErrorMatcher<R> m) {
      return m.caseNotJsonObject(e); }
  }

  private static final class BadZabType extends Errors {

    //private final BigDecimal n;

    public BadZabType() {}

    public final <R> R runMatch(final ErrorMatcher<R> m) {
      return m.caseBadZabType(); }
  }

  private static final class ValueTypeNotNumber extends Errors {

    private final ClassCastException e;

    public ValueTypeNotNumber(final ClassCastException e) { this.e = e; }

    public final <R> R runMatch(final ErrorMatcher<R> m) {
      return m.caseValueTypeNotNumber(e); }
  }

  private static final class NotEnoughData extends Errors {

    public NotEnoughData() {}

    public final <R> R runMatch(final ErrorMatcher<R> m) {
      return m.caseNotEnoughData(); }
  }

  private static final class NoJsonField extends Errors {

    private final String fieldName;

    public NoJsonField(final String fieldName) {
      this.fieldName = fieldName; }

    public final <R> R runMatch(final ErrorMatcher<R> m) {
      return m.caseNoJsonField(fieldName); }
  }

  private static final class NoSuchForecastProcess extends Errors {

    private final String name;

    public NoSuchForecastProcess(final String name) {
      this.name = name; }

    public final <R> R runMatch(final ErrorMatcher<R> m) {
      return m.caseNoSuchForecastProcess(name); }
  }

  private static final class BadBounds extends Errors {

    public BadBounds() {}

    public final <R> R runMatch(final ErrorMatcher<R> m) {
      return m.caseBadBounds(); }
  }

  private static final class NoSuchFilter extends Errors {

    private final String name;

    public NoSuchFilter(final String name) {
      this.name = name; }

    public final <R> R runMatch(final ErrorMatcher<R> m) {
      return m.caseNoSuchFilter(name); }
  }

  private Errors() {}
}
