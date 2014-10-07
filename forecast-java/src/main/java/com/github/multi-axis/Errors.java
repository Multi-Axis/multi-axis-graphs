package com.github.multi_axis;

import javax.json.stream.JsonParsingException;
import javax.json.JsonException;
import java.lang.ClassCastException;


public abstract class Errors {

  public interface ErrorMatcher<R> {
    public R caseMiscJsonError(JsonException e);
    public R caseJsonParsingError(JsonParsingException e);
    public R caseNonNumberInArray(ClassCastException e);
    public R caseNotJsonArray(ClassCastException e);
  }

  public static final Errors miscJsonError(JsonException e) {
    return new MiscJsonError(e);
  }
  public static final Errors jsonParsingError(JsonParsingException e) {
    return new JsonParsingError(e);
  }
  public static final Errors nonNumberInArray(ClassCastException e) {
    return new NonNumberInArray(e);
  }
  public static final Errors notJsonArray(ClassCastException e) {
    return new NotJsonArray(e);
  }

  public abstract <R> R runMatch(ErrorMatcher<R> m);

  private static final class MiscJsonError extends Errors {

    private final JsonException e;

    public MiscJsonError(JsonException e) {
      this.e = e;
    }

    public final <R> R runMatch(ErrorMatcher<R> m) {
      return m.caseMiscJsonError(e);
    }
  }

  private static final class JsonParsingError extends Errors {

    private final JsonParsingException e;

    public JsonParsingError(JsonParsingException e) {
      this.e = e;
    }

    public final <R> R runMatch(ErrorMatcher<R> m) {
      return m.caseJsonParsingError(e);
    }
  }

  private static final class NonNumberInArray extends Errors {

    private final ClassCastException e;

    public NonNumberInArray(ClassCastException e) {
      this.e = e;
    }

    public final <R> R runMatch(ErrorMatcher<R> m) {
      return m.caseNonNumberInArray(e);
    }
  }

  private static final class NotJsonArray extends Errors {

    private final ClassCastException e;

    public NotJsonArray(ClassCastException e) {
      this.e = e;
    }

    public final <R> R runMatch(ErrorMatcher<R> m) {
      return m.caseNotJsonArray(e);
    }
  }

  private Errors() {}
}
