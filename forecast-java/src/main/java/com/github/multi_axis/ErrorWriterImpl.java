package com.github.multi_axis;

import java.lang.ClassCastException;
import javax.json.stream.JsonParsingException;
import javax.json.JsonException;

import fj.F;

import javax.json.JsonObject;

import com.github.multi_axis.Errors;

import static javax.json.Json.createObjectBuilder;

public final class ErrorWriterImpl {

  public static final F<Errors,JsonObject>
    write = err  -> writeError(err);

  public static JsonObject
    writeError(final Errors err) {
      //TODO THINK Something cleverer?
      return createObjectBuilder().add("error", errorString(err)).build(); }

  public static String
    errorString(final Errors err) {
      return err.runMatch(new Errors.ErrorMatcher<String>() {

        public String caseMiscJsonError(final JsonException e) {
          return "Miscellaneous JSON error"; }

        public String caseJsonParsingError(final JsonParsingException e) {
          return "JSON parsing exception"; }

        public String caseNonNumberInArray(final ClassCastException e) {
          return "Non-number in array"; }

        public String caseNonStringInArray(final ClassCastException e) {
          return "Non-string in array"; }

        public String caseNotJsonArray(final ClassCastException e) {
          return "Not a JSON array"; }

        public String caseNotJsonString(final ClassCastException e) {
          return "Not a JSON string"; }

        public String caseNotJsonNumber(final ClassCastException e) {
          return "Not a JSON number"; }

        public String caseNotJsonObject(final ClassCastException e) {
          return "Not a JSON object"; }

        public String caseBadZabType() {
          return "Bad zab type"; }

        public String caseValueTypeNotNumber(final ClassCastException e) {
          return "Value type is not a number"; }

        public String caseNotEnoughData() {
          return "Not enough data"; }

        public String caseNoJsonField(final String name) {
          return ("No JSON field" + name); }

        public String caseNoSuchForecastProcess(final String name) {
          return ("No such forecast process" + name); }

        public String caseBadBounds() {
          return "Bad bounds"; } // Not actually used now after draw_future fix.

        public String caseNoSuchFilter(final String name) {
          return ("No such filter" + name); } 

      } ); }

  private ErrorWriterImpl() {}

}
